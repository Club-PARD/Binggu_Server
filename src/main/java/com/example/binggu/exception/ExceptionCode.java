package com.example.binggu.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ExceptionCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,"user id에 맞는 유저정보가 없습니다.");

    private HttpStatus httpStatus;
    private String message;
}
