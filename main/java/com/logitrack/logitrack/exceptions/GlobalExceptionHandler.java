package com.logitrack.logitrack.exceptions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex, HttpServletRequest req) {
        HttpStatus status = ex.getStatus() != null ? ex.getStatus() : HttpStatus.BAD_REQUEST;
        ApiError apiError = new ApiError(LocalDateTime.now(), status.value(), status.getReasonPhrase(), ex.getMessage(), req.getRequestURI(), null);
        return new ResponseEntity<>(apiError, new HttpHeaders(), status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.toList());

        HttpStatus status = HttpStatus.BAD_REQUEST;
        ApiError apiError = new ApiError(LocalDateTime.now(), status.value(), status.getReasonPhrase(), "Validation failed", req.getRequestURI(), errors);
        return new ResponseEntity<>(apiError, new HttpHeaders(), status);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiError> handleNotFound(NoSuchElementException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ApiError apiError = new ApiError(LocalDateTime.now(), status.value(), status.getReasonPhrase(), ex.getMessage(), req.getRequestURI(), null);
        return new ResponseEntity<>(apiError, new HttpHeaders(), status);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntime(RuntimeException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ApiError apiError = new ApiError(LocalDateTime.now(), status.value(), status.getReasonPhrase(), ex.getMessage(), req.getRequestURI(), null);
        return new ResponseEntity<>(apiError, new HttpHeaders(), status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ApiError apiError = new ApiError(LocalDateTime.now(), status.value(), status.getReasonPhrase(), "An unexpected error occurred", req.getRequestURI(), null);
        return new ResponseEntity<>(apiError, new HttpHeaders(), status);
    }
}
