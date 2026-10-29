-- Invitaciones para registrar empleados: las crea un ADMIN para un rol de su empresa
CREATE TABLE invitation (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(64) UNIQUE NOT NULL,
    email VARCHAR,
    company_id BIGINT NOT NULL REFERENCES company(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES rol(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP
);
