package com.example.binggu.bus.dto.response;

import com.example.binggu.bus.dto.request.BusRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class BusResponse {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StationResponse{
        private String stationId;
        private String stationName;
        private Long routeNum;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusArrivalResonse{
        private String routeNumber;
        private String routeType;
        private String busType;
        private Long numOfStationBeforeArrival;
        private Long arrivalMin;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusRouteResponse{
        private Double latitude;
        private Double longitude;
        private String stationName;
        private Long stationNum;
        private Long upDown;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteNumList {
        private List<String> routeNumList;

        public static RouteNumList from(List<String> req) {
            return new RouteNumList(req);
        }
    }
}
