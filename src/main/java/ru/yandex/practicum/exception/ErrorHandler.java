package ru.yandex.practicum.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ErrorResponse handleValidation(MethodArgumentNotValidException e) {
        String description = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .peek(ex -> log.warn("Ошибка валидации — поле '{}': {}", ex.getField(), ex.getDefaultMessage()))
                .map(ex -> ex.getField() + ": " + ex.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return new ErrorResponse("Ошибка валидации", description);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler
    public ErrorResponse handleNotFound(NotFoundException e) {
        log.warn("Объект не найден: {}", e.getMessage(), e);
        return new ErrorResponse("Объект не найден", e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ErrorResponse handleValidationException(ValidationException e) {
        log.warn("Не заполнены необходимые поля: {}", e.getMessage(), e);
        return new ErrorResponse("Не заполнены необходимые поля", e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler
    public ErrorResponse handleUnexpected(Exception e) {
        log.error("Непредвиденная ошибка: {}", e.getMessage(), e);
        return new ErrorResponse("Непредвиденная ошибка", e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ErrorResponse handleConstraintViolation(ConstraintViolationException e) {
        log.warn("Некорректный параметр запроса: {}", e.getMessage(), e);
        return new ErrorResponse("Некорректный параметр", e.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler
    public ErrorResponse handleForbidden(ForbiddenException e) {
        log.warn("Ошибка с правами доступа: {}", e.getMessage(), e);
        return new ErrorResponse("Недостаточно прав на проведение операции", e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler
    public ErrorResponse handleConflict(ConflictException e) {
        log.warn("Конфликтующие параметры запроса: {}", e.getMessage(), e);
        return new ErrorResponse("Параметр запроса вступил в конфликт с существующими данными", e.getMessage());
    }
}
