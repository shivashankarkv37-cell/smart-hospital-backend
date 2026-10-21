package com.example.smart_hospital_backend.exception;

import com.example.smart_hospital_backend.dto.ApiError;
import com.example.smart_hospital_backend.assistant.AssistantUnavailableException;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError validation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(
                error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return error(400, "Validation failed", errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError constraint(ConstraintViolationException exception) {
        return error(400, "Validation failed", Map.of());
    }

    @ExceptionHandler(DuplicateEmailException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError duplicate(DuplicateEmailException exception) {
        return error(409, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError invalidCredentials(InvalidCredentialsException exception) {
        return error(401, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(DuplicateDepartmentException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError duplicateDepartment(DuplicateDepartmentException exception) {
        return error(409, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError conflict(ConflictException exception) {
        return error(409, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError malformed(HttpMessageNotReadableException exception) {
        return error(400, "Malformed JSON request", Map.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError invalidParameter(MethodArgumentTypeMismatchException exception) {
        return error(400, "Invalid request parameter", Map.of(exception.getName(), "Invalid value"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError invalidArgument(IllegalArgumentException exception) {
        return error(400, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError notFound(ResourceNotFoundException exception) {
        return error(404, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(AppointmentConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError appointmentConflict(AppointmentConflictException exception) {
        return error(409, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError forbidden(ForbiddenOperationException exception) {
        return error(403, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError constraintConflict(DataIntegrityViolationException exception) {
        return error(409, "Request conflicts with existing data", Map.of());
    }

    @ExceptionHandler(AssistantUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiError assistantUnavailable(AssistantUnavailableException exception) {
        return error(503, "Hospital FAQ assistant is unavailable", Map.of());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError unexpected(Exception exception) {
        return error(500, "An unexpected error occurred", Map.of());
    }

    private ApiError error(int status, String message, Map<String, String> errors) {
        return new ApiError(Instant.now(), status, message, errors);
    }
}
