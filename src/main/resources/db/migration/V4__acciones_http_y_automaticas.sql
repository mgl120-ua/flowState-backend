-- Fase 1: acciones por empresa, orden de ejecución, transiciones automáticas y traza de acciones

-- Las acciones pasan a pertenecer a una empresa. Las existentes heredan la empresa del flujo
-- en el que se usan; las que no se usan en ningún flujo no se pueden asignar y se eliminan.
ALTER TABLE action ADD COLUMN company_id BIGINT REFERENCES company(id) ON DELETE CASCADE;
UPDATE action a SET company_id = (
    SELECT w.company_id
    FROM transition_action ta
    JOIN transition t ON t.id = ta.transition_id
    JOIN workflow w ON w.id = t.workflow_id
    WHERE ta.action_id = a.id
    ORDER BY ta.id
    LIMIT 1
);
DELETE FROM action WHERE company_id IS NULL;
ALTER TABLE action ALTER COLUMN company_id SET NOT NULL;

-- Orden de ejecución de las acciones de una transición, sin duplicados
ALTER TABLE transition_action ADD COLUMN position INT NOT NULL DEFAULT 0;
DELETE FROM transition_action a USING transition_action b
WHERE a.transition_id = b.transition_id AND a.action_id = b.action_id AND a.id > b.id;
ALTER TABLE transition_action ADD CONSTRAINT uq_transition_action UNIQUE (transition_id, action_id);

-- Transiciones que el motor ejecuta solo cuando se cumple su condición
ALTER TABLE transition ADD COLUMN automatic BOOLEAN NOT NULL DEFAULT false;

-- Traza de cada ejecución de una acción
CREATE TABLE action_execution (
    id BIGSERIAL PRIMARY KEY,
    instance_id BIGINT NOT NULL REFERENCES instance(id) ON DELETE CASCADE,
    transition_id BIGINT REFERENCES transition(id) ON DELETE SET NULL,
    action_id BIGINT REFERENCES action(id) ON DELETE SET NULL,
    action_name VARCHAR,
    action_type VARCHAR NOT NULL,
    status VARCHAR(10) NOT NULL CHECK (status IN ('SUCCESS', 'FAILED')),
    http_status INT,
    duration_ms BIGINT NOT NULL,
    error VARCHAR,
    request_summary VARCHAR,
    response_summary JSONB,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_action_execution_instance ON action_execution(instance_id);
