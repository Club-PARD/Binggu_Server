package com.example.binggu.bus.controller;

import com.example.binggu.bus.dto.request.BusRequest;
import com.example.binggu.bus.dto.response.BusResponse;
import com.example.binggu.bus.service.BusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bus")
public class BusController {
    private final BusService busService;

    @PostMapping("/station")
    public ResponseEntity<List<BusResponse.StationResponse>> getBusStationInfo(@RequestBody BusRequest.BusStationRequest req) throws IOException {
        List<BusResponse.StationResponse> res = busService.buildBusStationUrl(req);
        return ResponseEntity.ok().body(res);
    }

    @PostMapping("/arrivalTime")
    public ResponseEntity<List<BusResponse.BusArrivalResonse>> getBusArrivalTime(@RequestBody BusRequest.BusArrivalRequest req) throws IOException{
        List<BusResponse.BusArrivalResonse> res = busService.getBusArrivalTime(req);
        return ResponseEntity.ok().body(res);
    }
}
