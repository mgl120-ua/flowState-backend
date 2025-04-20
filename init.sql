CREATE TABLE Company (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR UNIQUE NOT NULL
);

CREATE TABLE Rol (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    description VARCHAR,
    company_id BIGINT NOT NULL REFERENCES Company(id)
);

CREATE TABLE AppUser (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    email VARCHAR UNIQUE NOT NULL,
    company_id BIGINT NOT NULL REFERENCES Company(id),
    role_id BIGINT REFERENCES Rol(id)
);

CREATE TABLE Workflow (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    company_id BIGINT NOT NULL REFERENCES Company(id)
);

CREATE TABLE State (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    workflow_id BIGINT NOT NULL REFERENCES Workflow(id)
);

CREATE TABLE Transition (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR NOT NULL,
    condition VARCHAR,
    source_state_id BIGINT NOT NULL REFERENCES State(id),
    target_state_id BIGINT NOT NULL REFERENCES State(id),
    workflow_id BIGINT NOT NULL REFERENCES Workflow(id)
);

CREATE TABLE Transition_Permission (
    id BIGSERIAL PRIMARY KEY,
    transition_id BIGINT NOT NULL REFERENCES Transition(id),
    role_id BIGINT NOT NULL REFERENCES Rol(id)
);

CREATE TABLE Action (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    type VARCHAR NOT NULL,
    config JSON
);

CREATE TABLE Transition_Action (
    id BIGSERIAL PRIMARY KEY,
    action_id BIGINT NOT NULL REFERENCES Action(id),
    transition_id BIGINT NOT NULL REFERENCES Transition(id)
);

CREATE TABLE Instances (
    id BIGSERIAL PRIMARY KEY,
    data JSON,
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    state_id BIGINT NOT NULL REFERENCES State(id),
    workflow_id BIGINT NOT NULL REFERENCES Workflow(id),
    user_id BIGINT REFERENCES AppUser(id)
);

CREATE TABLE Instances_History (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    instance_id BIGINT NOT NULL REFERENCES Instances(id),
    from_state_id BIGINT REFERENCES State(id),
    to_state_id BIGINT REFERENCES State(id),
    transition_id BIGINT REFERENCES Transition(id),
    changed_by BIGINT REFERENCES AppUser(id)
);

-- Insertar datos

-- EMPRESAS
INSERT INTO Company (name) VALUES
    ('Tenologia Soluciones'),
    ('Corp');

-- ROLES
INSERT INTO Rol (name, description, company_id) VALUES
    ('Administrador', 'Acceso total al sistema', 1),
    ('Supervisor', 'Puede revisar procesos', 1),
    ('Empleado', 'Solo puede iniciar procesos', 1),
    ('Administrador', 'Acceso total al sistema', 2),
    ('Colaborador', 'Acceso parcial', 2);

-- USUARIOS
INSERT INTO AppUser (name, email, company_id, role_id) VALUES
    ('Marta Ruiz', 'marta.ruiz@flowtech.com', 1, 1),
    ('Carlos López', 'carlos.lopez@flowtech.com', 1, 2),
    ('Irene Sánchez', 'irene.sanchez@flowtech.com', 1, 3),
    ('Ana Vidal', 'ana.vidal@innovatech.com', 2, 4),
    ('Luis Ortega', 'luis.ortega@innovatech.com', 2, 5);

-- PROCESOS
INSERT INTO Workflow (name, company_id) VALUES
    ('Contratación de Empleados', 1),
    ('Solicitud de Vacaciones', 1),
    ('Publicación de Contenido', 2);

-- ESTADOS
-- Contratación
INSERT INTO State (name, workflow_id) VALUES
    ('Registro inicial', 1),
    ('Verificación de datos', 1),
    ('Cuenta creada', 1);

-- Vacaciones
INSERT INTO State (name, workflow_id) VALUES
    ('Solicitud enviada', 2),
    ('Aprobación pendiente', 2),
    ('Aprobado', 2),
    ('Rechazado', 2);

-- Publicación
INSERT INTO State (name, workflow_id) VALUES
    ('Borrador', 3),
    ('Revisión editorial', 3),
    ('Publicado', 3);

-- TRANSICIONES
INSERT INTO Transition (action, condition, source_state_id, target_state_id, workflow_id) VALUES
    -- Contratación
    ('Verificar datos', 'si todos los campos están completos', 1, 2, 1),
    ('Crear cuenta', 'si datos verificados', 2, 3, 1),

    -- Vacaciones
    ('Revisar solicitud', 'si tiene días disponibles', 4, 5, 2),
    ('Aprobar vacaciones', 'si jefe aprueba', 5, 6, 2),
    ('Rechazar solicitud', 'si no cumple requisitos', 5, 7, 2),

    -- Publicación
    ('Enviar a revisión', 'cuando editor esté disponible', 8, 9, 3),
    ('Publicar', 'si revisión aprobada', 9, 10, 3);

-- PERMISOS EN TRANSICIONES
INSERT INTO Transition_Permission (transition_id, role_id) VALUES
    (1, 2), (2, 1), -- Contratación
    (3, 2), (4, 1), (5, 1), -- Vacaciones
    (6, 5), (7, 4); -- Publicación

-- ACCIONES
INSERT INTO Action (name, type, config) VALUES
    ('Enviar email de bienvenida', 'email', '{"template": "bienvenida.html"}'),
    ('Generar credenciales', 'script', '{"endpoint": "/generate/credentials"}'),
    ('Notificar a recursos humanos', 'email', '{"template": "notificacion_rrhh.html"}'),
    ('Crear entrada en calendario', 'api_call', '{"url": "https://calendar.example.com/api/v1/event"}');

-- ASOCIAR ACCIONES A TRANSICIONES
INSERT INTO Transition_Action (action_id, transition_id) VALUES
    (1, 2),
    (2, 2),
    (3, 4),
    (4, 4);

-- INSTANCIAS DE PROCESO
INSERT INTO Instances (data, state_id, workflow_id, user_id) VALUES
    ('{"nombre": "Lucas García", "email": "lucas@demo.com"}', 1, 1, 3),
    ('{"empleado": "Carlos López", "dias": 5}', 4, 2, 2),
    ('{"titulo": "Nuevo producto 2025"}', 8, 3, 5);

-- HISTORIAL DE TRANSICIONES
INSERT INTO Instances_History (instance_id, from_state_id, to_state_id, transition_id, changed_by) VALUES
    (1, NULL, 1, NULL, 3),
    (1, 1, 2, 1, 2),
    (1, 2, 3, 2, 1),
    (2, NULL, 4, NULL, 2),
    (2, 4, 5, 3, 2),
    (3, NULL, 8, NULL, 5);
