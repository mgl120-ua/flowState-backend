# Demo: reclamaciones de clientes por comisiones bancarias.
#
# Crea una entidad de prueba con un administrador y un gestor, define el flujo de reclamaciones,
# clasifica las reclamaciones de reclamaciones.json con un modelo local (Ollama) y hace que el gestor
# apruebe o desestime cada una. Al final muestra la traza, el historial y el consumo de tokens.
#
# Requisitos:
#   - Ollama arrancado con el modelo descargado:  ollama pull qwen3.5:4b
#   - Backend arrancado en localhost:8085 con SPRING_MAIL_PASSWORD configurado
#     (al ejecutar una transición manual el backend envía un email de aviso).
#
# Uso, desde la carpeta del repositorio:
#   powershell -ExecutionPolicy Bypass -File demo\reclamaciones\demo-reclamaciones.ps1
#   powershell -ExecutionPolicy Bypass -File demo\reclamaciones\demo-reclamaciones.ps1 -Model qwen2.5:3b
param(
  [string]$Model = "qwen3.5:4b",
  [string]$Api = "http://localhost:8085",
  [string]$Ollama = "http://localhost:11434"
)
$ErrorActionPreference = "Stop"
$Dir = $PSScriptRoot

function Call($method, $path, $body, $token) {
  $headers = @{}
  if ($token) { $headers["Authorization"] = "Bearer $token" }
  $params = @{ Method = $method; Uri = "$Api$path"; Headers = $headers; ContentType = "application/json; charset=utf-8" }
  if ($null -ne $body) { $params.Body = [System.Text.Encoding]::UTF8.GetBytes(($body | ConvertTo-Json -Depth 20 -Compress)) }
  try {
    # Se lee como UTF-8 a mano: Windows PowerShell 5.1 descodifica mal las tildes si la respuesta no indica charset,
    # y esos datos se vuelven a enviar al guardar la decisión del gestor.
    $r = Invoke-WebRequest @params -UseBasicParsing
    $text = [System.Text.Encoding]::UTF8.GetString($r.RawContentStream.ToArray())
    if (-not $text) { return $null }
    try { return $text | ConvertFrom-Json } catch { return $text }
  }
  catch {
    $detail = $_.ErrorDetails.Message
    if (-not $detail) { $detail = $_.Exception.Message }
    Write-Host "  ERROR $method $path -> $detail" -ForegroundColor Red
    return $null
  }
}

function StatusOf($method, $path, $token) {
  try {
    Invoke-WebRequest -Method $method -Uri "$Api$path" -Headers @{ Authorization = "Bearer $token" } -UseBasicParsing | Out-Null
    return 200
  } catch {
    return [int]$_.Exception.Response.StatusCode
  }
}

function ReadJson($name) {
  return Get-Content -Raw -Encoding UTF8 (Join-Path $Dir $name) | ConvertFrom-Json
}

Write-Host "1. Ollama"
$names = (Invoke-RestMethod "$Ollama/api/tags").models | ForEach-Object { $_.name }
if (-not ($names -contains $Model)) { throw "Falta el modelo: ejecuta  ollama pull $Model" }
Write-Host "  modelo: $Model"

Write-Host "2. Entidad, administrador y gestor"
$n = Get-Random -Maximum 1000000
$adminEmail = "admin$n@banco-demo.test"
$gestorEmail = "gestor$n@banco-demo.test"
$pass = "secreto123"
Call POST "/auth/register/admin" @{ name = "Administración"; email = $adminEmail; companyName = "Banco Demo $n"; password = $pass } | Out-Null
$admin = (Call POST "/auth/login" @{ email = $adminEmail; password = $pass }).token
if (-not $admin) { throw "No se pudo iniciar sesión. ¿Está arrancado el backend en ${Api}?" }
$rolGestor = (Call POST "/roles" @{ name = "GESTOR"; description = "Resuelve reclamaciones de clientes" } $admin).id
$code = (Call POST "/invitations" @{ roleId = $rolGestor; email = $gestorEmail } $admin).code
Call POST "/auth/register/employee" @{ name = "Gestora de reclamaciones"; email = $gestorEmail; password = $pass; invitationCode = $code } | Out-Null
$gestor = (Call POST "/auth/login" @{ email = $gestorEmail; password = $pass }).token
Write-Host "  admin: $adminEmail"
Write-Host "  gestor: $gestorEmail (rol GESTOR)"

Write-Host "3. Flujo de reclamaciones"
$flow = (Call POST "/flows" @{ name = "Reclamaciones por comisiones" } $admin).id
function State($name, $type) { (Call POST "/flows/$flow/states" @{ name = $name; type = $type } $admin).id }
function Transition($name, $from, $to, $cond, $auto) {
  (Call POST "/flows/$flow/transitions" @{ action = $name; sourceStateId = $from; targetStateId = $to; condition = $cond; automatic = $auto } $admin).id
}
$recibida = State "Recibida" "INITIAL"
$clasificada = State "Clasificada" "NORMAL"
$prioritaria = State "Revisión prioritaria" "NORMAL"
$ordinaria = State "Revisión ordinaria" "NORMAL"
$aprobada = State "Devolución aprobada" "FINAL"
$desestimada = State "Reclamación desestimada" "FINAL"

# Automáticas: la IA clasifica y el motor enruta. Un cargo no reconocido (posible fraude), una urgencia alta
# o una confianza baja van a revisión prioritaria, aunque el modelo no lo pida.
$tClasificar = Transition "Clasificar" $recibida $clasificada $null $true
$reglaPrioritaria = "clasificacion.body.tipo == 'cargo_no_reconocido' or clasificacion.body.urgencia == 'alta' or clasificacion.body.confianza < 0.7"
Transition "A revisión prioritaria" $clasificada $prioritaria $reglaPrioritaria $true | Out-Null
Transition "A revisión ordinaria" $clasificada $ordinaria "true" $true | Out-Null

# Manuales: solo el rol GESTOR puede ejecutarlas, y solo si su decisión está registrada en los datos.
$decisiones = @{}
foreach ($origen in @($prioritaria, $ordinaria)) {
  $decisiones["$origen"] = @{
    aprobar = Transition "Aprobar devolución" $origen $aprobada "revision.decision == 'aprobar'" $false
    desestimar = Transition "Desestimar" $origen $desestimada "revision.decision == 'desestimar'" $false
  }
  foreach ($t in $decisiones["$origen"].Values) {
    Call POST "/permissions/grant" @{ transitionId = $t; rolId = $rolGestor } $admin | Out-Null
  }
}

$schema = ReadJson "esquema-clasificacion.json"
$action = Call POST "/actions" @{ name = "Clasificar reclamación"; type = "LLM"; config = @{
  model = $Model
  system = "Eres analista de reclamaciones de clientes de un banco español. Clasifica la reclamación por el tipo de comisión o cargo. " +
           "Usa cargo_no_reconocido si el cliente no reconoce el cargo o sospecha fraude. " +
           "Urgencia alta si hay posible fraude o el cliente puede seguir perdiendo dinero; media si reclama un cobro que considera contrario a su contrato; baja si se queja de una comisión que conocía o aceptó. " +
           "procede_devolucion es tu propuesta: true si, por lo que cuenta el cliente, el cobro parece indebido. " +
           "importe_reclamado es el total en euros que reclama, o null si no lo indica. " +
           "confianza (de 0 a 1) debe ser baja si falta información para decidir. Explica el motivo en una o dos frases."
  prompt = "Cliente: {{cliente}}`nProducto: {{producto}}`nCanal: {{canal}}`nReclamación: {{descripcion}}"
  outputSchema = $schema; maxTokens = 400; timeoutMs = 300000; saveAs = "clasificacion" } } $admin
Call POST "/flows/$flow/transitions/$tClasificar/actions" @{ actionId = $action.id } $admin | Out-Null
Write-Host "  estados: Recibida -> Clasificada -> Revisión prioritaria | Revisión ordinaria -> Devolución aprobada | Reclamación desestimada"
Write-Host "  regla de prioridad: $reglaPrioritaria"

Write-Host "4. Reclamaciones (la primera tarda más mientras Ollama carga el modelo)"
$casos = (ReadJson "reclamaciones.json").reclamaciones
$aciertos = 0; $comprobados = 0; $ultima = $null
foreach ($caso in $casos) {
  Write-Host ""
  Write-Host "  $($caso.id) - $($caso.datos.cliente): $($caso.datos.descripcion)" -ForegroundColor Cyan
  $sw = [System.Diagnostics.Stopwatch]::StartNew()
  $inst = Call POST "/flows/$flow/instances" @{ data = $caso.datos } $admin
  $sw.Stop()
  if (-not $inst) { continue }
  $c = $inst.data.clasificacion.body
  $importe = if ($null -eq $c.importe_reclamado) { "sin importe" } else { "$($c.importe_reclamado) EUR" }
  Write-Host ("    IA: tipo {0}, urgencia {1}, devolución {2}, {3}, confianza {4} ({5:N1} s)" -f $c.tipo, $c.urgencia, $c.procede_devolucion, $importe, $c.confianza, $sw.Elapsed.TotalSeconds)
  Write-Host "    motivo: $($c.motivo)"
  Write-Host "    motor: -> $($inst.state.name)" -ForegroundColor Yellow

  foreach ($p in $caso.esperado.PSObject.Properties) {
    $comprobados++
    if ($c.($p.Name) -eq $p.Value) { $aciertos++ }
    else { Write-Host "    distinto de lo esperado: $($p.Name) = $($c.($p.Name)), se esperaba $($p.Value)" -ForegroundColor DarkYellow }
  }

  $tx = $decisiones["$($inst.state.id)"][$caso.decision_gestor]
  if ($caso.id -eq $casos[0].id) {
    $http = StatusOf POST "/flows/$flow/instances/$($inst.id)/transitions/$tx/execute" $admin
    Write-Host "    el administrador intenta resolverla: HTTP $http (no tiene el rol GESTOR)"
  }

  # El gestor registra su decisión y su nota en los datos y ejecuta la transición correspondiente.
  $data = $inst.data
  $data | Add-Member -NotePropertyName revision -Force -NotePropertyValue @{
    decision = $caso.decision_gestor; nota = $caso.nota_gestor; gestor = $gestorEmail }
  Call PUT "/flows/$flow/instances/$($inst.id)/data" $data $gestor | Out-Null
  $fin = Call POST "/flows/$flow/instances/$($inst.id)/transitions/$tx/execute" $null $gestor
  if ($fin) {
    $coincide = if (($caso.decision_gestor -eq "aprobar") -eq [bool]$c.procede_devolucion) { "coincide con la IA" } else { "corrige a la IA" }
    Write-Host "    gestor: $($caso.decision_gestor) ($coincide) -> $($fin.state.name)" -ForegroundColor Green
    Write-Host "    nota: $($caso.nota_gestor)"
  }

  $tr = Call GET "/flows/$flow/instances/$($inst.id)/executions" $null $admin | Where-Object { $_.actionType -eq "LLM" } | Select-Object -First 1
  if ($tr) { Write-Host "    traza: $($tr.status) $($tr.model), $($tr.inputTokens) + $($tr.outputTokens) tokens, coste $($tr.costUsd) USD, $($tr.durationMs) ms" }
  $ultima = $inst.id
}

Write-Host ""
Write-Host "5. Resultado"
if ($comprobados -gt 0) { Write-Host "  campos que coinciden con lo esperado: $aciertos de $comprobados" }
if ($ultima) {
  Write-Host "  historial de la última reclamación:"
  foreach ($h in (Call GET "/flows/$flow/instances/$ultima/history" $null $admin)) {
    $quien = if ($h.changed_by) { "usuario $($h.changed_by)" } else { "sistema" }
    Write-Host "    $($h.from_state.name) -> $($h.to_state.name) ($quien)"
  }
}
$u = Call GET "/usage/llm" $null $admin
if ($u) { Write-Host "  consumo: $($u.totals.calls) llamadas, $($u.totals.inputTokens) + $($u.totals.outputTokens) tokens, coste $($u.totals.costUsd) USD" }
