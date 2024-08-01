package com.example.binggu.bus.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class BusResponse {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StationResponse{
        private String stationId;
        private String stationName;
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
}
