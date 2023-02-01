package ru.practicum.shareit.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.util.Objects;

import static ru.practicum.shareit.log.Logger.logWarnException;

@RestControllerAdvice
public class ErrorHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ValidationException.class, MethodArgumentNotValidException.class,
            MissingRequestHeaderException.class, ConstraintViolationException.class,
            HttpRequestMethodNotSupportedException.class, MethodArgumentTypeMismatchException.class})
    public ErrorResponse handleValidationException(Exception e) {
        logWarnException(e);
        String message;
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException eValidation = (MethodArgumentNotValidException) e;
            message = Objects.requireNonNull(eValidation.getBindingResult().getFieldError()).getDefaultMessage();
        } else if (e instanceof HttpRequestMethodNotSupportedException) {
            HttpRequestMethodNotSupportedException eHttpRequestMethod = (HttpRequestMethodNotSupportedException) e;
            message = eHttpRequestMethod.getLocalizedMessage();
        } else if (e instanceof MethodArgumentTypeMismatchException) {
            MethodArgumentTypeMismatchException eMethodArgumentTypeMismatch = (MethodArgumentTypeMismatchException) e;
            message = eMethodArgumentTypeMismatch.getLocalizedMessage();
        } else {
            message = e.getMessage();
        }
        return new ErrorResponse(400, "Bad Request", message);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({EntityNotFoundException.class, NotFoundException.class})
    public ErrorResponse handleNotFoundException(Exception e) {
        logWarnException(e);
        return new ErrorResponse(404, "Not Found", e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    public ErrorResponse handleConflictException(Exception e) {
        logWarnException(e);
        String message;
        if (e instanceof DataIntegrityViolationException) {
            DataIntegrityViolationException eDataIntegrityViolation = (DataIntegrityViolationException) e;
            message = eDataIntegrityViolation.getMostSpecificCause().getLocalizedMessage();
        } else {
            message = e.getMessage();
        }
        return new ErrorResponse(409, "Conflict", message);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleThrowable(final Throwable e) {
        logWarnException(e);
        return new ErrorResponse(500, "Internal Server Error", "An unexpected error has occurred");
    }
}
