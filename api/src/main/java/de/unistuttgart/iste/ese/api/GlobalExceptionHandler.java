package de.unistuttgart.iste.ese.api;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

/**
 * Centralised exception handling for all REST controllers.
 *
 * <p>Translates exceptions into RFC 7807 ({@code application/problem+json})
 * {@link ProblemDetail} responses carrying the appropriate HTTP status codes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles illegal arguments (e.g. manually parsed invalid enum values).
     *
     * @param ex the thrown exception
     * @return a {@link ProblemDetail} with status {@code 400 Bad Request}
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        detail.setTitle("Bad Request");
        return detail;
    }

    /**
     * Handles malformed or unreadable request bodies (e.g. invalid JSON or
     * date strings that cannot be deserialised).
     *
     * @param ex the thrown exception
     * @return a {@link ProblemDetail} with status {@code 400 Bad Request}
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Malformed request body");
        detail.setTitle("Bad Request");
        return detail;
    }

    /**
     * Handles bean-validation failures on {@code @RequestBody} arguments.
     *
     * <p>The individual field errors are collected into an {@code errors}
     * property of the problem detail.
     *
     * @param ex the thrown exception
     * @return a {@link ProblemDetail} with status {@code 400 Bad Request} listing the field errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        detail.setTitle("Bad Request");
        String errors = ex.getBindingResult().getFieldErrors().stream()
            .map(this::formatFieldError)
            .collect(Collectors.joining("; "));
        detail.setProperty("errors", errors);
        return detail;
    }

    /**
     * Formats a single field validation error as {@code field: message}.
     *
     * @param error the field error; must not be {@code null}
     * @return the formatted string
     */
    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    /**
     * Handles database integrity violations (e.g. unique-constraint breaches).
     *
     * @param ex the thrown exception
     * @return a {@link ProblemDetail} with status {@code 409 Conflict}
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Data integrity violation");
        detail.setTitle("Conflict");
        return detail;
    }

    /**
     * Passes through {@link ResponseStatusException}s, preserving their status
     * code and reason as an RFC 7807 problem detail.
     *
     * @param ex the thrown exception
     * @return a {@link ProblemDetail} carrying the original status and reason
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatusException(ResponseStatusException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(ex.getStatusCode(), ex.getReason());
        detail.setTitle(ex.getStatusCode().toString());
        return detail;
    }

    /**
     * Catch-all handler for any otherwise unhandled exception.
     *
     * @param ex the thrown exception
     * @return a {@link ProblemDetail} with status {@code 500 Internal Server Error}
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        detail.setTitle("Internal Server Error");
        return detail;
    }
}
