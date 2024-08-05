package com.example.binggu.bus.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class BusRequest {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusStationRequest{
        private float latitude; //위도
        private float longtitude; //경도

        public BusStationRequest from(float latitude,float longtitude){
            return new BusStationRequest(latitude, longtitude);
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteNumRequest{
        private String stationId;
        private float destLatitude;
        private float destLongtitude;
        public RouteNumRequest(String stationId) {
            this.stationId = stationId;
            this.destLatitude = 0.0f; // 기본값 설정
            this.destLongtitude = 0.0f; // 기본값 설정
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusArrivalRequest{
        private String stationId;
        private String routeId;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusStationXYRequest{
        private String stationId;
        private String routeId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusRouteRequest{
        private String routeNum;
        private float startLati;
        private float startLong;
        private float destLatitude;
        private float destLongtitude;
    }

    @Getter
    @AllArgsConstructor
    public static class BusNumRequest{
        private String routeId;
        public static BusNumRequest of(String routeId){
            return new BusNumRequest(routeId);
        }
    }


}
