-- Fase 2: acción LLM. Traza de modelo, tokens y coste de cada llamada, y límite mensual de tokens por empresa

ALTER TABLE action_execution
    ADD COLUMN model VARCHAR,
    ADD COLUMN input_tokens INT,
    ADD COLUMN output_tokens INT,
    ADD COLUMN cost_usd NUMERIC(12, 6),
    ADD COLUMN prompt VARCHAR,
    ADD COLUMN stop_reason VARCHAR(50);

-- Para sumar el consumo del mes sin recorrer toda la tabla
CREATE INDEX idx_action_execution_started_at ON action_execution(started_at);

-- NULL = sin límite
ALTER TABLE company ADD COLUMN llm_monthly_token_limit BIGINT CHECK (llm_monthly_token_limit >= 0);
