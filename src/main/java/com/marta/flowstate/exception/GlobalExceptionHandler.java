package com.marta.flowstate.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Traduce las excepciones de la aplicación a respuestas HTTP con formato ProblemDetail (RFC 9457).
 * Los errores de Spring MVC (validación, ResponseStatusException, método no permitido...) los gestiona
 * ResponseEntityExceptionHandler con el mismo formato.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnauthorized(UnauthorizedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    //Una acción de la transición ha fallado (API externa caída, timeout...): la transición se ha deshecho
    @ExceptionHandler(ActionExecutionException.class)
    public ProblemDetail handleActionFailed(ActionExecutionException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY,
                "Ha fallado la acción '" + ex.getActionName() + "': " + ex.getMessage());
        problem.setProperty("action", ex.getActionName());
        problem.setProperty("reason", ex.getMessage());
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadRequest(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleConflict(DataIntegrityViolationException ex) {
        logger.warn("Violación de integridad: {}", ex.getMostSpecificCause().getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "El recurso entra en conflicto con datos existentes");
    }

    //Sin este manejador, el de Exception convertiría los @PreAuthorize denegados en 500
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "No tienes permisos para esta operación");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        logger.error("Error no controlado", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }
}
