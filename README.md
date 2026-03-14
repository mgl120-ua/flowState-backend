# FlowState Backend

Backend REST API for business process automation built with Spring Boot.

This project was developed as a final degree project in Computer Engineering and focuses on designing a scalable backend capable of managing and automating business workflows.

## Overview

FlowState is a backend system that allows the definition, execution and management of business processes through a RESTful API.

The goal of the project is to simulate how workflow automation systems work in enterprise environments.

The API enables:

- Creation and management of business processes
- Definition of process steps
- Execution and monitoring of workflows
- Interaction with relational databases

## Architecture

The project follows a layered architecture commonly used in Spring Boot applications:
Controller (REST API)
↓
Service Layer (Business Logic)
↓
Repository Layer (Data Access)
↓
Database

Key architectural principles:

- RESTful API design
- Separation of concerns
- Layered architecture
- Clean service structure

## Tech Stack

- **Java**
- **Spring Boot**
- **Spring Data JPA**
- **REST API**
- **SQL relational database**
- **Maven**

## Project Structure
src/main/java/com/flowstate

controllers → REST endpoints
services → business logic
repositories → database access
models → entities / domain objects
config → configuration classes



## Example API Endpoints

| Method | Endpoint | Description |
|------|------|------|
| GET | /processes | Get all processes |
| POST | /processes | Create new process |
| GET | /processes/{id} | Get process by id |
| PUT | /processes/{id} | Update process |
| DELETE | /processes/{id} | Delete process |

## Running the Project

Clone the repository:

git clone https://github.com/mgl120-ua/flowState-backend.git

Run with Maven:
mvn spring-boot:run

The API will start at:
http://localhost:8080

Future Improvements

Possible future enhancements:
- Authentication with Spring Security / JWT
- Docker containerization
- API documentation with Swagger/OpenAPI
- Automated testing (JUnit + Mockito)
- CI/CD pipeline

Author
Marta Grimaldos
Computer Engineering Graduate
Backend Developer (Java / Spring Boot)
