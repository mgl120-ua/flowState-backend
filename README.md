# flowState-backend

API REST para la gestión de flujos de trabajo y estados con autenticación JWT.

## Descripción

Este proyecto es un backend Spring Boot que implementa:

- Autenticación y autorización con JWT
- Gestión de usuarios, roles y permisos
- Control de flujos de trabajo (`Workflow`, `State`, `Transition`)
- Ejecución de transiciones/instancias con validación de condiciones
- Integración con PostgreSQL
- Documentación OpenAPI / Swagger

## Tecnologías

- Java 21
- Spring Boot 3.4.4
- Spring Web
- Spring Data JPA
- Spring Security
- PostgreSQL
- Swagger / Springdoc OpenAPI
- JJWT para JWT
- Lombok
- JUnit + Spring Boot Test

## Requisitos

- Java 21
- Maven (se usa el wrapper incluido `./mvnw.cmd`)
- PostgreSQL

## Configuración

La configuración principal se encuentra en `src/main/resources/application.properties`.

Variables de entorno requeridas:

- `SPRING_DATASOURCE_PASSWORD` - contraseña de la base de datos PostgreSQL
- `SPRING_MAIL_PASSWORD` - contraseña del servicio SMTP
- `JWT_SECRET` - clave secreta para firmar JWT

Variables opcionales:

- `FLOWSTATE_HTTP_ALLOW_PRIVATE_HOSTS` - `true` permite que las acciones HTTP llamen a direcciones internas (`localhost`, redes privadas). Por defecto `false`; actívalo solo para pruebas locales

Perfiles de Spring disponibles:

- `local`
- `prod`

Puedes seleccionar el perfil con:

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
./mvnw.cmd spring-boot:run
```

## Base de datos

La URL de conexión por defecto es:

```text
jdbc:postgresql://localhost:5432/flowState
```

Asegúrate de crear la base de datos y el usuario con los permisos necesarios antes de ejecutar la aplicación.

El esquema se gestiona con Flyway: al arrancar, la aplicación aplica las migraciones de `src/main/resources/db/migration` y Hibernate solo valida que las entidades coinciden (`ddl-auto=validate`). Con el perfil `local` se cargan además los datos de ejemplo de `src/main/resources/db/demo`.

Para cambiar el esquema, añade un nuevo archivo `V<n>__descripcion.sql` en `db/migration`; no modifiques las migraciones ya aplicadas.

Si tienes una base de datos creada antes de usar Flyway, recréala vacía (con Docker: `docker compose down -v`) para que las migraciones se apliquen desde cero.

## Ejecución

Para compilar y ejecutar el proyecto:

```powershell
./mvnw.cmd clean package
./mvnw.cmd spring-boot:run
```

## Ejecución con Docker

Las credenciales se leen de un archivo `.env` que no se sube al repositorio:

```powershell
Copy-Item .env.example .env   # y rellena los valores
docker compose up --build
```

Si falta alguna variable obligatoria, `docker compose` se detiene indicando cuál.

## Registro de usuarios

- `POST /auth/register/admin` crea una empresa nueva y su usuario administrador (rol `ADMIN`).
- Los empleados solo pueden registrarse con una invitación. Un `ADMIN` la crea con `POST /invitations` (`roleId` de un rol de su empresa y, opcionalmente, el `email` del invitado) y comparte el `code` devuelto. El empleado se registra con `POST /auth/register/employee` enviando `name`, `email`, `password` e `invitationCode`.
- Cada invitación sirve una sola vez y caduca a los 7 días. `GET /invitations` lista las de la empresa y `DELETE /invitations/{id}` revoca una.

## Acciones y transiciones automáticas

Las acciones se ejecutan al pasar por una transición. Las gestiona un `ADMIN` y pertenecen a su empresa.

- `POST /actions` crea una acción (`name`, `type`, `config`). `GET`, `PUT` y `DELETE /actions/{id}` la consultan, editan o borran.
- `POST /flows/{flowId}/transitions/{transitionId}/actions` asocia una acción a una transición (`actionId` y `position`, que fija el orden). `DELETE .../actions/{actionId}` la desasocia.
- `GET /flows/{flowId}/instances/{instanceId}/executions` devuelve la traza de las acciones ejecutadas en una instancia (estado, código HTTP, duración y error).

Tipos de acción:

- `EMAIL`: `{"to", "subject", "body"}`.
- `HTTP`: `{"method", "url", "headers", "body", "timeoutMs", "saveAs"}`. Solo `url` es obligatoria; el método por defecto es `GET` y el timeout 10 s (máximo 30 s). Si se indica `saveAs`, la respuesta se guarda en los datos de la instancia como `{"status": 200, "body": ...}`. Se considera fallo cualquier respuesta que no sea 2xx.

En `url`, cabeceras, cuerpo y en los campos del email se pueden usar variables con los datos de la instancia: `{{cliente}}` o `{{validacion.body.id}}`.

Una transición con `"automatic": true` la ejecuta el motor sin intervención al llegar a su estado origen, si se cumple su condición. Si hay varias, se ejecuta la primera que se cumpla (por orden de creación), y así sucesivamente hasta llegar a un estado `FINAL`, no tener ninguna aplicable o encadenar 20 seguidas. Las condiciones admiten rutas con punto, por ejemplo `validacion.body.ok == true`.

Si una acción falla, la transición se deshace por completo (estado, historial y datos), la API responde `502` indicando la acción y el motivo, y el fallo queda en la traza. Al crear una instancia también se ejecutan sus transiciones automáticas; si una falla, la instancia se queda en el estado inicial.

## Swagger / Documentación

Después de arrancar la aplicación, la documentación Swagger está disponible en:

```text
http://localhost:8085/swagger-ui.html
```

## Pruebas

Ejecuta las pruebas con:

```powershell
./mvnw.cmd test
```

## Buenas prácticas de uso

- Nunca subas el valor de `JWT_SECRET` ni contraseñas al repositorio.
- Usa variables de entorno o un gestor de secretos para las credenciales.
- Revisa los perfiles `local` y `prod` antes de desplegar.

## Notas

Este repositorio contiene un servicio de backend para gestionar el ciclo de vida de instancias en un sistema de workflows, incluyendo control de permisos, condiciones de transición y auditoría de instancias.
