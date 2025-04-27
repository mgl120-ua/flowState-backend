-- Script corregido con ON DELETE CASCADE donde aplica

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
                          data JSON,
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

-- Insertar datos

-- EMPRESAS
INSERT INTO company (name) VALUES
                               ('Tenologia Soluciones'),
                               ('Corp');

-- ROLES
INSERT INTO rol (name, description, company_id) VALUES
                                                    ('Administrador', 'Acceso total al sistema', 1),
                                                    ('Supervisor', 'Puede revisar procesos', 1),
                                                    ('Empleado', 'Solo puede iniciar procesos', 1),
                                                    ('Administrador', 'Acceso total al sistema', 2),
                                                    ('Colaborador', 'Acceso parcial', 2);

-- USUARIOS
INSERT INTO app_user (name, email, company_id, role_id) VALUES
                                                            ('Marta Ruiz', 'marta.ruiz@flowtech.com', 1, 1),
                                                            ('Carlos López', 'carlos.lopez@flowtech.com', 1, 2),
                                                            ('Irene Sánchez', 'irene.sanchez@flowtech.com', 1, 3),
                                                            ('Ana Vidal', 'ana.vidal@innovatech.com', 2, 4),
                                                            ('Luis Ortega', 'luis.ortega@innovatech.com', 2, 5);

-- PROCESOS
INSERT INTO workflow (name, company_id) VALUES
                                            ('Contratación de Empleados', 1),
                                            ('Solicitud de Vacaciones', 1),
                                            ('Publicación de Contenido', 2);

-- ESTADOS
-- Contratación
INSERT INTO state (name, workflow_id) VALUES
                                          ('Registro inicial', 1),
                                          ('Verificación de datos', 1),
                                          ('Cuenta creada', 1);

-- Vacaciones
INSERT INTO state (name, workflow_id) VALUES
                                          ('Solicitud enviada', 2),
                                          ('Aprobación pendiente', 2),
                                          ('Aprobado', 2),
                                          ('Rechazado', 2);

-- Publicación
INSERT INTO state (name, workflow_id) VALUES
                                          ('Borrador', 3),
                                          ('Revisión editorial', 3),
                                          ('Publicado', 3);

-- TRANSICIONES
INSERT INTO transition (action, condition, source_state_id, target_state_id, workflow_id) VALUES
                                                                                              ('Verificar datos', 'si todos los campos están completos', 1, 2, 1),
                                                                                              ('Crear cuenta', 'si datos verificados', 2, 3, 1),
                                                                                              ('Revisar solicitud', 'si tiene días disponibles', 4, 5, 2),
                                                                                              ('Aprobar vacaciones', 'si jefe aprueba', 5, 6, 2),
                                                                                              ('Rechazar solicitud', 'si no cumple requisitos', 5, 7, 2),
                                                                                              ('Enviar a revisión', 'cuando editor esté disponible', 8, 9, 3),
                                                                                              ('Publicar', 'si revisión aprobada', 9, 10, 3);

-- PERMISOS EN TRANSICIONES
INSERT INTO transition_permission (transition_id, role_id) VALUES
                                                               (1, 2), (2, 1),
                                                               (3, 2), (4, 1), (5, 1),
                                                               (6, 5), (7, 4);

-- ACCIONES
INSERT INTO action (name, type, config) VALUES
                                            ('Enviar email de bienvenida', 'email', '{"template": "bienvenida.html"}'),
                                            ('Generar credenciales', 'script', '{"endpoint": "/generate/credentials"}'),
                                            ('Notificar a recursos humanos', 'email', '{"template": "notificacion_rrhh.html"}'),
                                            ('Crear entrada en calendario', 'api_call', '{"url": "https://calendar.example.com/api/v1/event"}');

-- ASOCIAR ACCIONES A TRANSICIONES
INSERT INTO transition_action (action_id, transition_id) VALUES
                                                             (1, 2),
                                                             (2, 2),
                                                             (3, 4),
                                                             (4, 4);

-- INSTANCIAS DE PROCESO
INSERT INTO instance (data, state_id, workflow_id, user_id) VALUES
                                                                ('{"nombre": "Lucas García", "email": "lucas@demo.com"}', 1, 1, 3),
                                                                ('{"empleado": "Carlos López", "dias": 5}', 4, 2, 2),
                                                                ('{"titulo": "Nuevo producto 2025"}', 8, 3, 5);

-- HISTORIAL DE TRANSICIONES
INSERT INTO instance_history (instance_id, from_state_id, to_state_id, transition_id, changed_by) VALUES
                                                                                                      (1, NULL, 1, NULL, 3),
                                                                                                      (1, 1, 2, 1, 2),
                                                                                                      (1, 2, 3, 2, 1),
                                                                                                      (2, NULL, 4, NULL, 2),
                                                                                                      (2, 4, 5, 3, 2),
                                                                                                      (3, NULL, 8, NULL, 5);