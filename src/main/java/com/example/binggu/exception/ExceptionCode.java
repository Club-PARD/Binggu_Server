package com.example.binggu.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ExceptionCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,"user id에 맞는 유저정보가 없습니다."),
    Route_NOT_FOUND(HttpStatus.NOT_FOUND, "출발지에서 도착지까지 이어지는 경로가 없습니다"),
    STATION_NOT_EXIST(HttpStatus.EXPECTATION_FAILED, "노선에 출발 정류장과 도착 정류장이 없습니다");

    private HttpStatus httpStatus;
    private String message;
}
