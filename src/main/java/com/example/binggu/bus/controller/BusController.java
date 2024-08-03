package com.example.binggu.bus.controller;

import com.example.binggu.bus.dto.request.BusRequest;
import com.example.binggu.bus.dto.response.BusResponse;
import com.example.binggu.bus.service.BusService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bus")
public class BusController {
    private final BusService busService;

    @PostMapping("/station")
    @Operation(description = "사용자의 위도경도로 근방 500m안에 있는 정류장의 stationId가져오기")
    public ResponseEntity<List<BusResponse.StationResponse>> getBusStationInfo(@RequestBody BusRequest.BusStationRequest req) throws IOException {
        List<BusResponse.StationResponse> res = busService.buildBusStationUrl(req);
        return ResponseEntity.ok().body(res);
    }

    @PostMapping("/arrivalTime")
    @Operation(description = "stationId로 정류장까지 도착시간 가져오기")
    public ResponseEntity<List<BusResponse.BusArrivalResonse>> getBusArrivalTime(@RequestBody BusRequest.BusArrivalRequest req) throws IOException{
        List<BusResponse.BusArrivalResonse> res = busService.getBusArrivalTime(req);
        return ResponseEntity.ok().body(res);
    }

    @PostMapping("/route")
    @Operation(description = "노선번호로 해당 노선의 노선 경로 가져오기")
    public ResponseEntity<List<BusResponse.BusRouteResponse>> getBusRoute(@RequestBody BusRequest.BusRouteRequest req) throws IOException{
        List<BusResponse.BusRouteResponse> res = busService.getBusRoute(req);
        return ResponseEntity.ok().body(res);
    }

    @PostMapping("/routeNum")
    @Operation(description = "stationId로 정류장의 노선번호들 가져오기")
    public ResponseEntity<BusResponse.RouteNumList> getRouteNum(@RequestBody BusRequest.RouteNumRequest req) throws IOException {
       BusResponse.RouteNumList res = busService.getRouteByStationId(req);
        return ResponseEntity.ok().body(res);
    }
    @PostMapping("/stationXY")
    public ResponseEntity<BusResponse.busNumStationId> getStationXY(@RequestBody BusRequest.BusStationXYRequest req) throws IOException{
        BusResponse.busNumStationId res = busService.getStationXY(req);
        return ResponseEntity.ok().body(res);
    }

    @PostMapping("/busNum")
    public ResponseEntity<BusResponse.busNum> getBusNum(@RequestBody BusRequest.BusNumRequest req) throws IOException {
        BusResponse.busNum res = busService.getBusNum(req);
        return ResponseEntity.ok().body(res);
    }

}
