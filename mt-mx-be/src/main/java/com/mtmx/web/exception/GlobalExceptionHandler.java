package com.mtmx.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleGlobalException(Exception ex, WebRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Beklenmeyen bir hata oluştu";
        ErrorDto errorDetails = new ErrorDto(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                message,
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDto> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        String message = "Parametre türü uyumsuzluğu: " + ex.getName() + " için geçersiz değer: " + ex.getValue();
        ErrorDto errorDetails = new ErrorDto(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                message,
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorDto> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, WebRequest request) {
        String message = "Eksik parametre: " + ex.getParameterName() + " (" + ex.getParameterType() + ")";
        ErrorDto errorDetails = new ErrorDto(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                message,
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDto> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
        String message = "Geçersiz JSON formatı veya okunamayan mesaj";
        ErrorDto errorDetails = new ErrorDto(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                message,
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorDto> handleBindException(BindException ex, WebRequest request) {
        String message = "Validasyon hatası: " + ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        ErrorDto errorDetails = new ErrorDto(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                message,
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorDto> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, WebRequest request) {
        String message = "Desteklenmeyen HTTP metodu: " + ex.getMethod() + ". Desteklenen metodlar: " + ex.getSupportedHttpMethods();
        ErrorDto errorDetails = new ErrorDto(
                LocalDateTime.now(),
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "Method Not Allowed",
                message,
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDto> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Geçersiz argüman";
        ErrorDto errorDetails = new ErrorDto(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                message,
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorDto> handleRuntimeException(RuntimeException ex, WebRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Çalışma zamanı hatası";
        ErrorDto errorDetails = new ErrorDto(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                message,
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
} 