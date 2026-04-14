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

- Java 17
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

- Java 17
- Maven (se usa el wrapper incluido `./mvnw.cmd`)
- PostgreSQL

## Configuración

La configuración principal se encuentra en `src/main/resources/application.properties`.

Variables de entorno requeridas:

- `SPRING_DATASOURCE_PASSWORD` - contraseña de la base de datos PostgreSQL
- `SPRING_MAIL_PASSWORD` - contraseña del servicio SMTP
- `JWT_SECRET` - clave secreta para firmar JWT

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

## Ejecución

Para compilar y ejecutar el proyecto:

```powershell
./mvnw.cmd clean package
./mvnw.cmd spring-boot:run
```

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
