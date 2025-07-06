CREATE TABLE company (
 id BIGSERIAL PRIMARY KEY,
 name VARCHAR UNIQUE NOT NULL
);

CREATE TABLE rol (
 id BIGSERIAL PRIMARY KEY,
 name VARCHAR NOT NULL,
 description VARCHAR,
 company_id BIGINT NOT NULL REFERENCES company(id) ON DELETE CASCADE
);

CREATE TABLE app_user (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR NOT NULL,
  email VARCHAR UNIQUE NOT NULL,
  password VARCHAR NOT NULL,
  company_id BIGINT NOT NULL REFERENCES company(id) ON DELETE CASCADE,
  role_id BIGINT REFERENCES rol(id) ON DELETE SET NULL
);

CREATE TABLE workflow (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR NOT NULL,
  company_id BIGINT NOT NULL REFERENCES company(id) ON DELETE CASCADE
);

CREATE TABLE state (
   id BIGSERIAL PRIMARY KEY,
   name VARCHAR NOT NULL,
   type VARCHAR(10) DEFAULT 'NORMAL' CHECK (type IN ('INITIAL', 'NORMAL', 'FINAL')),
   workflow_id BIGINT NOT NULL REFERENCES workflow(id) ON DELETE CASCADE
);

CREATE TABLE transition (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR NOT NULL,
    condition VARCHAR,
    source_state_id BIGINT NOT NULL REFERENCES state(id) ON DELETE CASCADE,
    target_state_id BIGINT NOT NULL REFERENCES state(id) ON DELETE CASCADE,
    workflow_id BIGINT NOT NULL REFERENCES workflow(id) ON DELETE CASCADE
);

CREATE TABLE transition_permission (
   id BIGSERIAL PRIMARY KEY,
   transition_id BIGINT NOT NULL REFERENCES transition(id) ON DELETE CASCADE,
   role_id BIGINT NOT NULL REFERENCES rol(id) ON DELETE CASCADE
);

CREATE TABLE action (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    type VARCHAR NOT NULL,
    config JSON
);

CREATE TABLE transition_action (
   id BIGSERIAL PRIMARY KEY,
   action_id BIGINT NOT NULL REFERENCES action(id) ON DELETE CASCADE,
   transition_id BIGINT NOT NULL REFERENCES transition(id) ON DELETE CASCADE
);

CREATE TABLE instance (
  id BIGSERIAL PRIMARY KEY,
  data JSONB,
  date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  state_id BIGINT NOT NULL REFERENCES state(id) ON DELETE CASCADE,
  workflow_id BIGINT NOT NULL REFERENCES workflow(id) ON DELETE CASCADE,
  user_id BIGINT REFERENCES app_user(id) ON DELETE SET NULL
);

CREATE TABLE instance_history (
  id BIGSERIAL PRIMARY KEY,
  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  instance_id BIGINT NOT NULL REFERENCES instance(id) ON DELETE CASCADE,
  from_state_id BIGINT REFERENCES state(id) ON DELETE SET NULL,
  to_state_id BIGINT REFERENCES state(id) ON DELETE SET NULL,
  transition_id BIGINT REFERENCES transition(id) ON DELETE SET NULL,
  changed_by BIGINT REFERENCES app_user(id) ON DELETE SET NULL
);
-- ==============================
-- EMPRESAS
-- ==============================
INSERT INTO company (name) VALUES
                               ('FlowTech Solutions'),
                               ('InnovaCorp'),
                               ('GreenWave Systems'),
                               ('DataNova'),
                               ('NextGen Corp'),
                               ('AlfaOmega Tech'),
                               ('Nimbus Group'),
                               ('QuantumEdge');

-- ==============================
-- WORKFLOWS POR EMPRESA
-- ==============================
INSERT INTO workflow (name, company_id) VALUES
                                            ('FlowTech - Proceso 1', 1),
                                            ('FlowTech - Proceso 2', 1),
                                            ('FlowTech - Proceso 3', 1),
                                            ('FlowTech - Proceso 4', 1),

                                            ('InnovaCorp - Proceso 1', 2),
                                            ('InnovaCorp - Proceso 2', 2),
                                            ('InnovaCorp - Proceso 3', 2),
                                            ('InnovaCorp - Proceso 4', 2),

                                            ('GreenWave - Proceso 1', 3),
                                            ('GreenWave - Proceso 2', 3),
                                            ('GreenWave - Proceso 3', 3),
                                            ('GreenWave - Proceso 4', 3),
                                            ('GreenWave - Proceso 5', 3),

                                            ('DataNova - Proceso 1', 4),
                                            ('DataNova - Proceso 2', 4),
                                            ('DataNova - Proceso 3', 4),
                                            ('DataNova - Proceso 4', 4),

                                            ('NextGen - Proceso 1', 5),
                                            ('NextGen - Proceso 2', 5),
                                            ('NextGen - Proceso 3', 5),

                                            ('AlfaOmega - Proceso 1', 6),
                                            ('AlfaOmega - Proceso 2', 6),
                                            ('AlfaOmega - Proceso 3', 6),
                                            ('AlfaOmega - Proceso 4', 6),

                                            ('Nimbus - Proceso 1', 7),
                                            ('Nimbus - Proceso 2', 7),
                                            ('Nimbus - Proceso 3', 7),

                                            ('QuantumEdge - Proceso 1', 8),
                                            ('QuantumEdge - Proceso 2', 8),
                                            ('QuantumEdge - Proceso 3', 8),
                                            ('QuantumEdge - Proceso 4', 8),
                                            ('QuantumEdge - Proceso 5', 8);

-- ==============================
-- ROLES POR EMPRESA
-- ==============================
INSERT INTO rol (name, description, company_id) VALUES
                                                    ('Administrador', 'Acceso total al sistema', 1),
                                                    ('Supervisor', 'Puede revisar y aprobar procesos', 1),
                                                    ('Empleado', 'Puede iniciar procesos', 1),

                                                    ('Administrador', 'Acceso total al sistema', 2),
                                                    ('Supervisor', 'Puede revisar y aprobar procesos', 2),
                                                    ('Empleado', 'Puede iniciar procesos', 2),

                                                    ('Administrador', 'Acceso total al sistema', 3),
                                                    ('Supervisor', 'Puede revisar y aprobar procesos', 3),
                                                    ('Empleado', 'Puede iniciar procesos', 3),

                                                    ('Administrador', 'Acceso total al sistema', 4),
                                                    ('Supervisor', 'Puede revisar y aprobar procesos', 4),
                                                    ('Empleado', 'Puede iniciar procesos', 4),

                                                    ('Administrador', 'Acceso total al sistema', 5),
                                                    ('Supervisor', 'Puede revisar y aprobar procesos', 5),
                                                    ('Empleado', 'Puede iniciar procesos', 5),

                                                    ('Administrador', 'Acceso total al sistema', 6),
                                                    ('Supervisor', 'Puede revisar y aprobar procesos', 6),
                                                    ('Empleado', 'Puede iniciar procesos', 6),

                                                    ('Administrador', 'Acceso total al sistema', 7),
                                                    ('Supervisor', 'Puede revisar y aprobar procesos', 7),
                                                    ('Empleado', 'Puede iniciar procesos', 7),

                                                    ('Administrador', 'Acceso total al sistema', 8),
                                                    ('Supervisor', 'Puede revisar y aprobar procesos', 8),
                                                    ('Empleado', 'Puede iniciar procesos', 8);


-- ==============================
-- USUARIOS POR EMPRESA
-- ==============================
INSERT INTO app_user (name, email, password, company_id, role_id) VALUES
                                                                      ('Administrador Empresa1', 'administrador.1@empresa1.com', '$2a$10$ThSxvHpV6m1ZoF7cOEES4uDR4k3ek6UEBpjpYBoInZlYxCR9GcSJe', 1, 1),
                                                                      ('Supervisor Empresa1', 'supervisor.1@empresa1.com', '$2a$10$ThSxvHpV6m1ZoF7cOEES4uDR4k3ek6UEBpjpYBoInZlYxCR9GcSJe', 1, 2),
                                                                      ('Empleado Empresa1', 'empleado.1@empresa1.com', '$2a$10$ThSxvHpV6m1ZoF7cOEES4uDR4k3ek6UEBpjpYBoInZlYxCR9GcSJe', 1, 3),

                                                                      ('Administrador Empresa2', 'administrador.2@empresa2.com', '$2a$10$ThSxvHpV6m1ZoF7cOEES4uDR4k3ek6UEBpjpYBoInZlYxCR9GcSJe', 2, 4),
                                                                      ('Supervisor Empresa2', 'supervisor.2@empresa2.com', '$2a$10$ThSxvHpV6m1ZoF7cOEES4uDR4k3ek6UEBpjpYBoInZlYxCR9GcSJe', 2, 5),
                                                                      ('Empleado Empresa2', 'empleado.2@empresa2.com', '$2a$10$ThSxvHpV6m1ZoF7cOEES4uDR4k3ek6UEBpjpYBoInZlYxCR9GcSJe', 2, 6),

                                                                      ('Administrador Empresa3', 'administrador.3@empresa3.com', '$2a$10$ThSxvHpV6m1ZoF7cOEES4uDR4k3ek6UEBpjpYBoInZlYxCR9GcSJe', 3, 7),
                                                                      ('Supervisor Empresa3', 'supervisor.3@empresa3.com', '$2a$10$ThSxvHpV6m1ZoF7cOEES4uDR4k3ek6UEBpjpYBoInZlYxCR9GcSJe', 3, 8),
                                                                      ('Empleado Empresa3', 'empleado.3@empresa3.com', '$2a$10$ThSxvHpV6m1ZoF7cOEES4uDR4k3ek6UEBpjpYBoInZlYxCR9GcSJe', 3, 9),

                                                                      ('Administrador Empresa4', 'administrador.4@empresa4.com', '$2a$10$ThSxvHpV6m1ZoF7cOEES4uDR4k3ek6UEBpjpYBoInZlYxCR9GcSJe', 4, 10),
                                                                      ('Supervisor Empresa4', 'supervisor.4@empresa4.com', '$2a$10$ThSxvHpV6m1ZoF7cOEES4uDR4k3ek6UEBpjpYBoInZlYxCR9GcSJe', 4, 11),
                                                                      ('Empleado Empresa4', 'empleado.4@empresa4.com', '$2a$10$ThSxvHpV6m1ZoF7cOEES4uDR4k3ek6UEBpjpYBoInZlYxCR9GcSJe', 4, 12),

                                                                      ('Administrador Empresa5', 'administrador.5@empresa5.com', '$2a$10$ThSxvHpV6m1ZoF7cOEES4uDR4k3ek6UEBpjpYBoInZlYxCR9GcSJe', 5, 13),
                                                                      ('Supervisor Empresa5', 'supervisor.5@empresa5.com', '$2a$10$ThSxvHpV6m1ZoF7cOEES4uDR4k3ek6UEBpjpYBoInZlYxCR9GcSJe', 5, 14),
                                                                      ('Empleado Empresa5', 'empleado.5@empresa5.com', '$2a$10$ThSxvHpV6m1ZoF7cOEES4uDR4k3ek6UEBpjpYBoInZlYxCR9GcSJe', 5, 15),

                                                                      ('Administrador Empresa6', 'administrador.6@empresa6.com', '$2a$10$ThSxvHpV6m1ZoF7cOEES4uDR4k3ek6UEBpjpYBoInZlYxCR9GcSJe', 6, 16),
                                                                      ('Supervisor Empresa6', 'supervisor.6@empresa6.com', '$2a$10$ThSxvHpV6m1ZoF7cOEES4uDR4k3ek6UEBpjpYBoInZlYxCR9GcSJe', 6, 17),
                                                                      ('Empleado Empresa6', 'empleado.6@empresa6.com', '$2a$10$ThSxvHpV6m1ZoF7cOEES4uDR4k3ek6UEBpjpYBoInZlYxCR9GcSJe', 6, 18),

                                                                      ('Administrador Empresa7', 'administrador.7@empresa7.com', '$2a$10$ThSxvHpV6m1ZoF7cOEES4uDR4k3ek6UEBpjpYBoInZlYxCR9GcSJe', 7, 19),
                                                                      ('Supervisor Empresa7', 'supervisor.7@empresa7.com', '$2a$10$ThSxvHpV6m1ZoF7cOEES4uDR4k3ek6UEBpjpYBoInZlYxCR9GcSJe', 7, 20),
                                                                      ('Empleado Empresa7', 'empleado.7@empresa7.com', '$2a$10$ThSxvHpV6m1ZoF7cOEES4uDR4k3ek6UEBpjpYBoInZlYxCR9GcSJe', 7, 21),

                                                                      ('Administrador Empresa8', 'administrador.8@empresa8.com', '$2a$10$ThSxvHpV6m1ZoF7cOEES4uDR4k3ek6UEBpjpYBoInZlYxCR9GcSJe', 8, 22),
                                                                      ('Supervisor Empresa8', 'supervisor.8@empresa8.com', '$2a$10$ThSxvHpV6m1ZoF7cOEES4uDR4k3ek6UEBpjpYBoInZlYxCR9GcSJe', 8, 23),
                                                                      ('Empleado Empresa8', 'empleado.8@empresa8.com', '$2a$10$ThSxvHpV6m1ZoF7cOEES4uDR4k3ek6UEBpjpYBoInZlYxCR9GcSJe', 8, 24);


-- ==============================
-- ESTADOS MEJORADOS POR WORKFLOW
-- ==============================
INSERT INTO state (name, type, workflow_id) VALUES
                                                ('FlowTech Flujo 1 - Inicio', 'INITIAL', 1),
                                                ('FlowTech Flujo 1 - Ejecución', 'NORMAL', 1),
                                                ('FlowTech Flujo 1 - Revisión', 'NORMAL', 1),
                                                ('FlowTech Flujo 1 - Fin', 'FINAL', 1),

                                                ('FlowTech Flujo 2 - Inicio', 'INITIAL', 2),
                                                ('FlowTech Flujo 2 - Confirmación', 'NORMAL', 2),
                                                ('FlowTech Flujo 2 - Fin', 'FINAL', 2),

                                                ('FlowTech Flujo 3 - Inicio', 'INITIAL', 3),
                                                ('FlowTech Flujo 3 - Revisión', 'NORMAL', 3),
                                                ('FlowTech Flujo 3 - Ejecución', 'NORMAL', 3),
                                                ('FlowTech Flujo 3 - Aprobación', 'NORMAL', 3),
                                                ('FlowTech Flujo 3 - Fin', 'FINAL', 3),

                                                ('FlowTech Flujo 4 - Inicio', 'INITIAL', 4),
                                                ('FlowTech Flujo 4 - Revisión', 'NORMAL', 4),
                                                ('FlowTech Flujo 4 - Ejecución', 'NORMAL', 4),
                                                ('FlowTech Flujo 4 - Aprobación', 'NORMAL', 4),
                                                ('FlowTech Flujo 4 - Fin', 'FINAL', 4),

                                                ('FlowTech Flujo 5 - Inicio', 'INITIAL', 5),
                                                ('FlowTech Flujo 5 - Revisión', 'NORMAL', 5),
                                                ('FlowTech Flujo 5 - Ejecución', 'NORMAL', 5),
                                                ('FlowTech Flujo 5 - Aprobación', 'NORMAL', 5),
                                                ('FlowTech Flujo 5 - Fin', 'FINAL', 5),

                                                ('InnovaCorp Flujo 1 - Inicio', 'INITIAL', 6),
                                                ('InnovaCorp Flujo 1 - Revisión', 'NORMAL', 6),
                                                ('InnovaCorp Flujo 1 - Ejecución', 'NORMAL', 6),
                                                ('InnovaCorp Flujo 1 - Fin', 'FINAL', 6),

                                                ('InnovaCorp Flujo 2 - Inicio', 'INITIAL', 7),
                                                ('InnovaCorp Flujo 2 - Confirmación', 'NORMAL', 7),
                                                ('InnovaCorp Flujo 2 - Fin', 'FINAL', 7),

                                                ('InnovaCorp Flujo 3 - Inicio', 'INITIAL', 8),
                                                ('InnovaCorp Flujo 3 - Aprobación', 'NORMAL', 8),
                                                ('InnovaCorp Flujo 3 - Fin', 'FINAL', 8),

                                                ('NextSolutions Flujo 1 - Inicio', 'INITIAL', 9),
                                                ('NextSolutions Flujo 1 - Validación', 'NORMAL', 9),
                                                ('NextSolutions Flujo 1 - Fin', 'FINAL', 9),

                                                ('NextSolutions Flujo 2 - Inicio', 'INITIAL', 10),
                                                ('NextSolutions Flujo 2 - Revisión', 'NORMAL', 10),
                                                ('NextSolutions Flujo 2 - Aprobación', 'NORMAL', 10),
                                                ('NextSolutions Flujo 2 - Fin', 'FINAL', 10),

                                                ('NextSolutions Flujo 3 - Inicio', 'INITIAL', 11),
                                                ('NextSolutions Flujo 3 - Ejecución', 'NORMAL', 11),
                                                ('NextSolutions Flujo 3 - QA', 'NORMAL', 11),
                                                ('NextSolutions Flujo 3 - Fin', 'FINAL', 11),

                                                ('NextSolutions Flujo 4 - Inicio', 'INITIAL', 12),
                                                ('NextSolutions Flujo 4 - Confirmación', 'NORMAL', 12),
                                                ('NextSolutions Flujo 4 - Fin', 'FINAL', 12),

                                                ('NextSolutions Flujo 5 - Inicio', 'INITIAL', 13),
                                                ('NextSolutions Flujo 5 - Aprobación', 'NORMAL', 13),
                                                ('NextSolutions Flujo 5 - Fin', 'FINAL', 13),

                                                ('VisionaryApps Flujo 1 - Inicio', 'INITIAL', 14),
                                                ('VisionaryApps Flujo 1 - Análisis', 'NORMAL', 14),
                                                ('VisionaryApps Flujo 1 - Desarrollo', 'NORMAL', 14),
                                                ('VisionaryApps Flujo 1 - Fin', 'FINAL', 14),

                                                ('VisionaryApps Flujo 2 - Inicio', 'INITIAL', 15),
                                                ('VisionaryApps Flujo 2 - QA', 'NORMAL', 15),
                                                ('VisionaryApps Flujo 2 - Aprobación', 'NORMAL', 15),
                                                ('VisionaryApps Flujo 2 - Fin', 'FINAL', 15),

                                                ('VisionaryApps Flujo 3 - Inicio', 'INITIAL', 16),
                                                ('VisionaryApps Flujo 3 - Revisión', 'NORMAL', 16),
                                                ('VisionaryApps Flujo 3 - Fin', 'FINAL', 16),

                                                ('VisionaryApps Flujo 4 - Inicio', 'INITIAL', 17),
                                                ('VisionaryApps Flujo 4 - Análisis', 'NORMAL', 17),
                                                ('VisionaryApps Flujo 4 - Desarrollo', 'NORMAL', 17),
                                                ('VisionaryApps Flujo 4 - Fin', 'FINAL', 17),

                                                ('VisionaryApps Flujo 5 - Inicio', 'INITIAL', 18),
                                                ('VisionaryApps Flujo 5 - Ejecución', 'NORMAL', 18),
                                                ('VisionaryApps Flujo 5 - QA', 'NORMAL', 18),
                                                ('VisionaryApps Flujo 5 - Fin', 'FINAL', 18),

                                                ('QuantumEdge Flujo 1 - Inicio', 'INITIAL', 19),
                                                ('QuantumEdge Flujo 1 - Revisión', 'NORMAL', 19),
                                                ('QuantumEdge Flujo 1 - Aprobación', 'NORMAL', 19),
                                                ('QuantumEdge Flujo 1 - Fin', 'FINAL', 19),

                                                ('QuantumEdge Flujo 2 - Inicio', 'INITIAL', 20),
                                                ('QuantumEdge Flujo 2 - Confirmación', 'NORMAL', 20),
                                                ('QuantumEdge Flujo 2 - Fin', 'FINAL', 20),

                                                ('QuantumEdge Flujo 3 - Inicio', 'INITIAL', 21),
                                                ('QuantumEdge Flujo 3 - QA', 'NORMAL', 21),
                                                ('QuantumEdge Flujo 3 - Fin', 'FINAL', 21),

                                                ('QuantumEdge Flujo 4 - Inicio', 'INITIAL', 22),
                                                ('QuantumEdge Flujo 4 - Revisión', 'NORMAL', 22),
                                                ('QuantumEdge Flujo 4 - Desarrollo', 'NORMAL', 22),
                                                ('QuantumEdge Flujo 4 - Fin', 'FINAL', 22),

                                                ('SysNova Flujo 1 - Inicio', 'INITIAL', 23),
                                                ('SysNova Flujo 1 - Confirmación', 'NORMAL', 23),
                                                ('SysNova Flujo 1 - Fin', 'FINAL', 23),

                                                ('SysNova Flujo 2 - Inicio', 'INITIAL', 24),
                                                ('SysNova Flujo 2 - QA', 'NORMAL', 24),
                                                ('SysNova Flujo 2 - Validación', 'NORMAL', 24),
                                                ('SysNova Flujo 2 - Fin', 'FINAL', 24),

                                                ('SysNova Flujo 3 - Inicio', 'INITIAL', 25),
                                                ('SysNova Flujo 3 - Revisión', 'NORMAL', 25),
                                                ('SysNova Flujo 3 - Aprobación', 'NORMAL', 25),
                                                ('SysNova Flujo 3 - Fin', 'FINAL', 25),

                                                ('SysNova Flujo 4 - Inicio', 'INITIAL', 26),
                                                ('SysNova Flujo 4 - Evaluación', 'NORMAL', 26),
                                                ('SysNova Flujo 4 - Fin', 'FINAL', 26),

                                                ('SysNova Flujo 5 - Inicio', 'INITIAL', 27),
                                                ('SysNova Flujo 5 - Aprobación', 'NORMAL', 27),
                                                ('SysNova Flujo 5 - Fin', 'FINAL', 27),

                                                ('DataBridge Flujo 1 - Inicio', 'INITIAL', 28),
                                                ('DataBridge Flujo 1 - Procesado', 'NORMAL', 28),
                                                ('DataBridge Flujo 1 - Fin', 'FINAL', 28),

                                                ('DataBridge Flujo 2 - Inicio', 'INITIAL', 29),
                                                ('DataBridge Flujo 2 - Validación', 'NORMAL', 29),
                                                ('DataBridge Flujo 2 - Aprobación', 'NORMAL', 29),
                                                ('DataBridge Flujo 2 - Fin', 'FINAL', 29),

                                                ('DataBridge Flujo 3 - Inicio', 'INITIAL', 30),
                                                ('DataBridge Flujo 3 - Análisis', 'NORMAL', 30),
                                                ('DataBridge Flujo 3 - QA', 'NORMAL', 30),
                                                ('DataBridge Flujo 3 - Fin', 'FINAL', 30),

                                                ('DataBridge Flujo 4 - Inicio', 'INITIAL', 31),
                                                ('DataBridge Flujo 4 - Confirmación', 'NORMAL', 31),
                                                ('DataBridge Flujo 4 - Ejecución', 'NORMAL', 31),
                                                ('DataBridge Flujo 4 - Fin', 'FINAL', 31),

                                                ('QuantumEdge Flujo 5 - Inicio', 'INITIAL', 32),
                                                ('QuantumEdge Flujo 5 - Revisión', 'NORMAL', 32),
                                                ('QuantumEdge Flujo 5 - QA', 'NORMAL', 32),
                                                ('QuantumEdge Flujo 5 - Aprobación', 'NORMAL', 32),
                                                ('QuantumEdge Flujo 5 - Fin', 'FINAL', 32),
                                                ('QuantumEdge Flujo 5 - Publicado', 'FINAL', 32),
                                                ('QuantumEdge Flujo 5 - Confirmación', 'NORMAL', 32),
                                                ('QuantumEdge Flujo 5 - Fin', 'FINAL', 32);


-- ==============================
-- TRANSICIONES ENTRE ESTADOS
-- ==============================
INSERT INTO transition (action, condition, source_state_id, target_state_id, workflow_id) VALUES
                                                                                              ('Transición 1', 'true', 1, 2, 1),
                                                                                              ('Transición 2', 'true', 2, 3, 1),
                                                                                              ('Transición 3', 'true', 3, 4, 1),
                                                                                              ('Transición 1', 'true', 5, 6, 2),
                                                                                              ('Transición 2', 'true', 6, 7, 2),
                                                                                              ('Transición 1', 'true', 8, 9, 3),
                                                                                              ('Transición 2', 'true', 9, 10, 3),
                                                                                              ('Transición 3', 'true', 10, 11, 3),
                                                                                              ('Transición 4', 'true', 11, 12, 3),
                                                                                              ('Transición 1', 'true', 13, 14, 4),
                                                                                              ('Transición 2', 'true', 14, 15, 4),
                                                                                              ('Transición 3', 'true', 15, 16, 4),
                                                                                              ('Transición 4', 'true', 16, 17, 4),
                                                                                              ('Transición 1', 'true', 18, 19, 5),
                                                                                              ('Transición 2', 'true', 19, 20, 5),
                                                                                              ('Transición 3', 'true', 20, 21, 5),
                                                                                              ('Transición 4', 'true', 21, 22, 5),
                                                                                              ('Transición 1', 'true', 23, 24, 6),
                                                                                              ('Transición 2', 'true', 24, 25, 6),
                                                                                              ('Transición 3', 'true', 25, 26, 6),
                                                                                              ('Transición 1', 'true', 27, 28, 7),
                                                                                              ('Transición 2', 'true', 28, 29, 7),
                                                                                              ('Transición 1', 'true', 30, 31, 8),
                                                                                              ('Transición 2', 'true', 31, 32, 8),
                                                                                              ('Transición 1', 'true', 33, 34, 9),
                                                                                              ('Transición 2', 'true', 34, 35, 9),
                                                                                              ('Transición 1', 'true', 36, 37, 10),
                                                                                              ('Transición 2', 'true', 37, 38, 10),
                                                                                              ('Transición 3', 'true', 38, 39, 10),
                                                                                              ('Transición 1', 'true', 40, 41, 11),
                                                                                              ('Transición 2', 'true', 41, 42, 11),
                                                                                              ('Transición 3', 'true', 42, 43, 11),
                                                                                              ('Transición 1', 'true', 44, 45, 12),
                                                                                              ('Transición 2', 'true', 45, 46, 12),
                                                                                              ('Transición 1', 'true', 47, 48, 13),
                                                                                              ('Transición 2', 'true', 48, 49, 13),
                                                                                              ('Transición 1', 'true', 50, 51, 14),
                                                                                              ('Transición 2', 'true', 51, 52, 14),
                                                                                              ('Transición 3', 'true', 52, 53, 14),
                                                                                              ('Transición 1', 'true', 54, 55, 15),
                                                                                              ('Transición 2', 'true', 55, 56, 15),
                                                                                              ('Transición 3', 'true', 56, 57, 15),
                                                                                              ('Transición 1', 'true', 58, 59, 16),
                                                                                              ('Transición 2', 'true', 59, 60, 16),
                                                                                              ('Transición 1', 'true', 61, 62, 17),
                                                                                              ('Transición 2', 'true', 62, 63, 17),
                                                                                              ('Transición 3', 'true', 63, 64, 17),
                                                                                              ('Transición 1', 'true', 65, 66, 18),
                                                                                              ('Transición 2', 'true', 66, 67, 18),
                                                                                              ('Transición 3', 'true', 67, 68, 18),
                                                                                              ('Transición 1', 'true', 69, 70, 19),
                                                                                              ('Transición 2', 'true', 70, 71, 19),
                                                                                              ('Transición 3', 'true', 71, 72, 19),
                                                                                              ('Transición 1', 'true', 73, 74, 20),
                                                                                              ('Transición 2', 'true', 74, 75, 20),
                                                                                              ('Transición 1', 'true', 76, 77, 21),
                                                                                              ('Transición 2', 'true', 77, 78, 21),
                                                                                              ('Transición 1', 'true', 79, 80, 22),
                                                                                              ('Transición 2', 'true', 80, 81, 22),
                                                                                              ('Transición 3', 'true', 81, 82, 22),
                                                                                              ('Transición 1', 'true', 83, 84, 23),
                                                                                              ('Transición 2', 'true', 84, 85, 23),
                                                                                              ('Transición 1', 'true', 86, 87, 24),
                                                                                              ('Transición 2', 'true', 87, 88, 24),
                                                                                              ('Transición 3', 'true', 88, 89, 24),
                                                                                              ('Transición 1', 'true', 90, 91, 25),
                                                                                              ('Transición 2', 'true', 91, 92, 25),
                                                                                              ('Transición 3', 'true', 92, 93, 25),
                                                                                              ('Transición 1', 'true', 94, 95, 26),
                                                                                              ('Transición 2', 'true', 95, 96, 26),
                                                                                              ('Transición 1', 'true', 97, 98, 27),
                                                                                              ('Transición 2', 'true', 98, 99, 27),
                                                                                              ('Transición 1', 'true', 100, 101, 28),
                                                                                              ('Transición 2', 'true', 101, 102, 28),
                                                                                              ('Transición 1', 'true', 103, 104, 29),
                                                                                              ('Transición 2', 'true', 104, 105, 29),
                                                                                              ('Transición 3', 'true', 105, 106, 29),
                                                                                              ('Transición 1', 'true', 107, 108, 30),
                                                                                              ('Transición 2', 'true', 108, 109, 30),
                                                                                              ('Transición 3', 'true', 109, 110, 30),
                                                                                              ('Transición 1', 'true', 111, 112, 31),
                                                                                              ('Transición 2', 'true', 112, 113, 31),
                                                                                              ('Transición 3', 'true', 113, 114, 31),
                                                                                              ('Transición 1', 'true', 115, 116, 32),
                                                                                              ('Transición 2', 'true', 116, 117, 32),
                                                                                              ('Transición 3', 'true', 117, 118, 32),
                                                                                              ('Transición 4', 'true', 118, 119, 32),
                                                                                              ('Transición 5', 'true', 119, 120, 32),
                                                                                              ('Transición 1', 'true', 120, 121, 32),
                                                                                              ('Transición 2', 'true', 121, 122, 32);

-- ==============================
-- TRANSITION PERMISSION
-- ==============================
INSERT INTO transition_permission (transition_id, role_id) VALUES
                                                               (1, 1),
                                                               (1, 2),
                                                               (1, 3),
                                                               (2, 1),
                                                               (3, 1),
                                                               (4, 9),
                                                               (5, 10),
                                                               (5, 11),
                                                               (6, 12),
                                                               (6, 13),
                                                               (7, 14),
                                                               (7, 15),
                                                               (8, 16),
                                                               (8, 17),
                                                               (9, 18),
                                                               (9, 19),
                                                               (10, 20),
                                                               (10, 21),
                                                               (11, 22),
                                                               (11, 1),
                                                               (12, 2),
                                                               (12, 3),
                                                               (13, 4),
                                                               (13, 5),
                                                               (14, 6),
                                                               (15, 7),
                                                               (15, 8),
                                                               (16, 9),
                                                               (16, 10),
                                                               (17, 11),
                                                               (17, 12),
                                                               (18, 13),
                                                               (18, 14),
                                                               (19, 15),
                                                               (19, 16),
                                                               (20, 17),
                                                               (20, 18),
                                                               (21, 19),
                                                               (21, 20),
                                                               (22, 21),
                                                               (22, 22),
                                                               (23, 1),
                                                               (24, 2),
                                                               (24, 3),
                                                               (25, 4),
                                                               (25, 5),
                                                               (26, 6),
                                                               (26, 7),
                                                               (27, 8),
                                                               (27, 9),
                                                               (28, 10),
                                                               (28, 11),
                                                               (29, 12),
                                                               (29, 13),
                                                               (30, 14),
                                                               (30, 15),
                                                               (31, 16),
                                                               (31, 17),
                                                               (32, 18),
                                                               (32, 19),
                                                               (33, 20),
                                                               (33, 21),
                                                               (34, 22),
                                                               (35, 1),
                                                               (35, 2),
                                                               (36, 3),
                                                               (36, 4),
                                                               (37, 5),
                                                               (37, 6),
                                                               (38, 7),
                                                               (38, 8),
                                                               (39, 9),
                                                               (39, 10),
                                                               (40, 11),
                                                               (40, 12),
                                                               (41, 13),
                                                               (41, 14),
                                                               (42, 15),
                                                               (42, 16),
                                                               (43, 17),
                                                               (43, 18),
                                                               (44, 19),
                                                               (45, 20),
                                                               (45, 21),
                                                               (46, 22),
                                                               (47, 1),
                                                               (47, 2),
                                                               (48, 3),
                                                               (48, 4),
                                                               (49, 5),
                                                               (49, 6),
                                                               (50, 7),
                                                               (50, 8),
                                                               (51, 9),
                                                               (51, 10),
                                                               (52, 11),
                                                               (52, 12),
                                                               (53, 13),
                                                               (53, 14),
                                                               (54, 15),
                                                               (55, 16),
                                                               (55, 17),
                                                               (56, 18),
                                                               (56, 19),
                                                               (57, 20),
                                                               (57, 21),
                                                               (58, 22),
                                                               (59, 1),
                                                               (59, 2),
                                                               (60, 3),
                                                               (60, 4),
                                                               (61, 5),
                                                               (61, 6),
                                                               (62, 7),
                                                               (62, 8),
                                                               (63, 9),
                                                               (63, 10),
                                                               (64, 11),
                                                               (64, 12),
                                                               (65, 13),
                                                               (65, 14),
                                                               (66, 15),
                                                               (66, 16),
                                                               (67, 17),
                                                               (67, 18),
                                                               (68, 19),
                                                               (68, 20),
                                                               (69, 21),
                                                               (69, 22),
                                                               (70, 1),
                                                               (70, 2),
                                                               (71, 3),
                                                               (71, 4),
                                                               (72, 5),
                                                               (72, 6),
                                                               (73, 7),
                                                               (73, 8),
                                                               (74, 9),
                                                               (74, 10),
                                                               (75, 11),
                                                               (75, 12),
                                                               (76, 13),
                                                               (76, 14),
                                                               (77, 15),
                                                               (77, 16),
                                                               (78, 17),
                                                               (78, 18),
                                                               (79, 19),
                                                               (79, 20),
                                                               (80, 21),
                                                               (80, 22),
                                                               (81, 1),
                                                               (81, 2),
                                                               (82, 3),
                                                               (82, 4),
                                                               (83, 5),
                                                               (83, 6),
                                                               (84, 7),
                                                               (84, 8),
                                                               (85, 9),
                                                               (85, 10),
                                                               (86, 11),
                                                               (86, 12),
                                                               (87, 13),
                                                               (87, 14),
                                                               (88, 15),
                                                               (88, 16),
                                                               (89, 17),
                                                               (89, 18),
                                                               (90, 19),
                                                               (90, 20),
                                                               (90, 21),
                                                               (90, 22);


-- ==============================
-- INSTANCIAS DE PRUEBA
-- ==============================
INSERT INTO instance (data, state_id, workflow_id, user_id) VALUES
                                                                ('{"proceso": "Demo 1", "iniciado_por": "empleado.1@empresa1.com"}', 1, 1, 3),
                                                                ('{"proceso": "Demo 2", "iniciado_por": "empleado.1@empresa1.com"}', 5, 2, 3),
                                                                ('{"proceso": "Demo 3", "iniciado_por": "empleado.1@empresa1.com"}', 8, 3, 3),
                                                                ('{"proceso": "Demo 4", "iniciado_por": "empleado.1@empresa1.com"}', 11, 4, 3),
                                                                ('{"proceso": "Demo 5", "iniciado_por": "empleado.2@empresa2.com"}', 18, 5, 6),
                                                                ('{"proceso": "Demo 6", "iniciado_por": "empleado.2@empresa2.com"}', 23, 6, 6),
                                                                ('{"proceso": "Demo 7", "iniciado_por": "empleado.2@empresa2.com"}', 27, 7, 6),
                                                                ('{"proceso": "Demo 8", "iniciado_por": "empleado.2@empresa2.com"}', 30, 8, 6),
                                                                ('{"proceso": "Demo 9", "iniciado_por": "empleado.3@empresa3.com"}', 33, 9, 9),
                                                                ('{"proceso": "Demo 10", "iniciado_por": "empleado.3@empresa3.com"}', 36, 10, 9),
                                                                ('{"proceso": "Demo 11", "iniciado_por": "empleado.3@empresa3.com"}', 40, 11, 9),
                                                                ('{"proceso": "Demo 12", "iniciado_por": "empleado.3@empresa3.com"}', 44, 12, 9),
                                                                ('{"proceso": "Demo 13", "iniciado_por": "empleado.4@empresa4.com"}', 47, 13, 12),
                                                                ('{"proceso": "Demo 14", "iniciado_por": "empleado.4@empresa4.com"}', 50, 14, 12),
                                                                ('{"proceso": "Demo 15", "iniciado_por": "empleado.4@empresa4.com"}', 54, 15, 12),
                                                                ('{"proceso": "Demo 16", "iniciado_por": "empleado.4@empresa4.com"}', 58, 16, 12),
                                                                ('{"proceso": "Demo 17", "iniciado_por": "empleado.5@empresa5.com"}', 61, 17, 15),
                                                                ('{"proceso": "Demo 18", "iniciado_por": "empleado.5@empresa5.com"}', 65, 18, 15),
                                                                ('{"proceso": "Demo 19", "iniciado_por": "empleado.5@empresa5.com"}', 69, 19, 15),
                                                                ('{"proceso": "Demo 20", "iniciado_por": "empleado.5@empresa5.com"}', 73, 20, 15),
                                                                ('{"proceso": "Demo 21", "iniciado_por": "empleado.6@empresa6.com"}', 76, 21, 18),
                                                                ('{"proceso": "Demo 22", "iniciado_por": "empleado.6@empresa6.com"}', 79, 22, 18),
                                                                ('{"proceso": "Demo 23", "iniciado_por": "empleado.6@empresa6.com"}', 83, 23, 18),
                                                                ('{"proceso": "Demo 24", "iniciado_por": "empleado.6@empresa6.com"}', 86, 24, 18),
                                                                ('{"proceso": "Demo 25", "iniciado_por": "empleado.7@empresa7.com"}', 90, 25, 21),
                                                                ('{"proceso": "Demo 26", "iniciado_por": "empleado.7@empresa7.com"}', 94, 26, 21),
                                                                ('{"proceso": "Demo 27", "iniciado_por": "empleado.7@empresa7.com"}', 97, 27, 21),
                                                                ('{"proceso": "Demo 28", "iniciado_por": "empleado.7@empresa7.com"}', 100, 28, 21),
                                                                ('{"proceso": "Demo 29", "iniciado_por": "empleado.8@empresa8.com"}', 103, 29, 24),
                                                                ('{"proceso": "Demo 30", "iniciado_por": "empleado.8@empresa8.com"}', 107, 30, 24),
                                                                ('{"proceso": "Demo 31", "iniciado_por": "empleado.8@empresa8.com"}', 111, 31, 24),
                                                                ('{"proceso": "Demo 32", "iniciado_por": "empleado.8@empresa8.com"}', 120, 32, 24);

-- ==============================
-- INSTANCE HISTORY (ESTADO INICIAL)
-- ==============================
INSERT INTO instance_history (instance_id, from_state_id, to_state_id, transition_id, changed_by) VALUES
                                                                                                      (1, NULL, 1, NULL, 3),
                                                                                                      (2, NULL, 5, NULL, 3),
                                                                                                      (3, NULL, 8, NULL, 3),
                                                                                                      (4, NULL, 11, NULL, 3),
                                                                                                      (5, NULL, 18, NULL, 6),
                                                                                                      (6, NULL, 23, NULL, 6),
                                                                                                      (7, NULL, 27, NULL, 6),
                                                                                                      (8, NULL, 30, NULL, 6),
                                                                                                      (9, NULL, 33, NULL, 9),
                                                                                                      (10, NULL, 36, NULL, 9),
                                                                                                      (11, NULL, 40, NULL, 9),
                                                                                                      (12, NULL, 44, NULL, 9),
                                                                                                      (13, NULL, 47, NULL, 12),
                                                                                                      (14, NULL, 50, NULL, 12),
                                                                                                      (15, NULL, 54, NULL, 12),
                                                                                                      (16, NULL, 58, NULL, 12),
                                                                                                      (17, NULL, 61, NULL, 15),
                                                                                                      (18, NULL, 65, NULL, 15),
                                                                                                      (19, NULL, 69, NULL, 15),
                                                                                                      (20, NULL, 73, NULL, 15),
                                                                                                      (21, NULL, 76, NULL, 18),
                                                                                                      (22, NULL, 79, NULL, 18),
                                                                                                      (23, NULL, 83, NULL, 18),
                                                                                                      (24, NULL, 86, NULL, 18),
                                                                                                      (25, NULL, 90, NULL, 21),
                                                                                                      (26, NULL, 94, NULL, 21),
                                                                                                      (27, NULL, 97, NULL, 21),
                                                                                                      (28, NULL, 100, NULL, 21),
                                                                                                      (29, NULL, 103, NULL, 24),
                                                                                                      (30, NULL, 107, NULL, 24),
                                                                                                      (31, NULL, 111, NULL, 24),
                                                                                                      (32, NULL, 120, NULL, 24);
