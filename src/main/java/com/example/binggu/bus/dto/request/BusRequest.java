package com.example.binggu.bus.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class BusRequest {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusStationRequest{
        private float latitude; //위도
        private float longtitude; //경도
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteNumRequest{
        private String stationId;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusArrivalRequest{
        private String stationId;
    }
    @Getter
    public static class BusRouteRequest{
        private String routeNum;
    }


}
