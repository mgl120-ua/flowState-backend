# FlowState Backend

Backend desarrollado con **Java y Spring Boot** que implementa un sistema de automatización de procesos empresariales basado en workflows y control de estados.

Este proyecto simula el funcionamiento de sistemas BPM utilizados en entornos reales, permitiendo gestionar procesos complejos con reglas de negocio, control de permisos y persistencia de datos.

---

## What it does

FlowState permite:

- Definir procesos de negocio con múltiples estados  
- Gestionar transiciones entre estados según reglas  
- Controlar permisos mediante roles  
- Ejecutar y monitorizar workflows  
- Persistir datos dinámicos en base de datos  

---

## Architecture

Arquitectura en capas:

- **Controller** → API REST  
- **Service** → lógica de negocio  
- **Repository** → acceso a datos  

Principios aplicados:

- Separación de responsabilidades  
- Diseño RESTful  
- Código modular y escalable  

---

## Tech Stack

- Java  
- Spring Boot  
- Spring Data JPA  
- PostgreSQL  
- JWT Authentication  
- Docker / Docker Compose  
- Swagger / OpenAPI  

---

## Security

Sistema de autenticación y autorización basado en JWT:

- Generación de tokens tras login  
- Validación en cada request  
- Control de acceso mediante roles  
- Restricción de transiciones según permisos  

---

## Workflow Engine

El núcleo del sistema es un motor de workflows basado en:

- Máquina de estados (FSM)  
- Transiciones configurables  
- Validación de reglas de negocio  
- Trazabilidad de estados  

---

## Example Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET    | /processes | Get all processes |
| POST   | /processes | Create new process |
| GET    | /processes/{id} | Get process by id |
| PUT    | /processes/{id} | Update process |
| DELETE | /processes/{id} | Delete process |

---

## Running the Project

```bash
git clone https://github.com/mgl120-ua/flowState-backend.git
cd flowState-backend
mvn spring-boot:run
