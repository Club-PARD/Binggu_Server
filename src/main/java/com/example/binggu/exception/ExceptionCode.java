package com.example.binggu.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ExceptionCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,"user id에 맞는 유저정보가 없습니다."),
    Route_NOT_FOUND(HttpStatus.NOT_FOUND, "출발지에서 도착지까지 이어지는 경로가 없습니다"),
    STATION_NOT_EXIST(HttpStatus.EXPECTATION_FAILED, "노선에 출발 정류장과 도착 정류장이 없습니다"),
    FAVORITE_ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "bus id에 맞는 즐겨찾기 경로가 없습니다"),
    INVALID_ROUTE_ID(HttpStatus.NOT_FOUND, "해당 Id에 맞는 버스번호가 없습니다"),
    NO_BUS_COMMING(HttpStatus.NOT_FOUND,"해당 정류장에 오는 버스가 없습니다");

    private HttpStatus httpStatus;
    private String message;
}
