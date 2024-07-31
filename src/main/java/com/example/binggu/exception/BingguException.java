package com.example.binggu.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class BingguException extends ResponseEntityExceptionHandler {
    @ExceptionHandler(CommonException.class)
    public ResponseEntity<ExceptionResponse> handleBingguException(CommonException e){
        return new ResponseEntity<>(ExceptionResponse.builder()
                .httpStatus(e.getExceptionCode().getHttpStatus())
                .message(e.getExceptionCode().getMessage())
                .build(),e.getExceptionCode().getHttpStatus());
    }
}
