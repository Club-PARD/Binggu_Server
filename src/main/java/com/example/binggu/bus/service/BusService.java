package com.example.binggu.bus.service;

import com.example.binggu.bus.dto.request.BusRequest;
import com.example.binggu.bus.dto.response.BusResponse;
import com.example.binggu.exception.CommonException;
import com.example.binggu.exception.ExceptionCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.awt.geom.Point2D.distance;

@Service
@RequiredArgsConstructor
public class BusService {

    @Value("${api.service.key}")
    private String apiServiceKey;

//    대구 버스 정보 데이타 받아오는 url 만드는 method
    private StringBuilder urlDaeguAppend(StringBuilder url, int pageNo) throws UnsupportedEncodingException {
        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append(URLEncoder.encode("serviceKey", "UTF-8")).append("=").append(apiServiceKey);
        urlBuilder.append("&").append(URLEncoder.encode("pageNo", "UTF-8")).append("=").append(URLEncoder.encode(String.valueOf(pageNo), "UTF-8"));
        urlBuilder.append("&").append(URLEncoder.encode("numOfRows", "UTF-8")).append("=").append(URLEncoder.encode("20", "UTF-8"));
        urlBuilder.append("&").append(URLEncoder.encode("_type", "UTF-8")).append("=").append(URLEncoder.encode("json", "UTF-8"));
        urlBuilder.append("&").append(URLEncoder.encode("cityCode", "UTF-8")).append("=").append(URLEncoder.encode("22", "UTF-8"));

        return urlBuilder;
    }

//    url로 json값 받아오기

    public String makeStringJsonResponse(String finalUrl) throws IOException {
        URL url = new URL(finalUrl);
        HttpURLConnection conn = (HttpURLConnection)
                url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        BufferedReader rd;
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();
        String jsonResponse = sb.toString();

        return jsonResponse;
    }

//    json값중 item에 들어있는 값만 가져오기
    public JsonNode getJsonNodeItems(String jsonResponse) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        return rootNode.path("response").path("body").path("items").path("item");
    }

//    출발지의  위도경도와 제일 가까운 정류장 찾기
    private BusResponse.BusRouteResponse findClosestStartStation(List<BusResponse.BusRouteResponse> stations, double latitude, double longitude) {
        BusResponse.BusRouteResponse startStation = stations.get(0); // 초기값: 첫 번째 정류소
        double minDistance = distance(startStation.getLatitude(), startStation.getLongitude(), latitude, longitude);

        for (BusResponse.BusRouteResponse station : stations) {
            double currentDistance = distance(station.getLatitude(), station.getLongitude(), latitude, longitude);
            if (currentDistance < minDistance) {
                startStation = station;
                minDistance = currentDistance;
            }
        }
        return startStation;
    }

//    도착지의 위도경도와 제일 가까운 정류장 찾기
    private BusResponse.BusRouteResponse findClosestEndStation(List<BusResponse.BusRouteResponse> stations, double latitude, double longitude) {
        BusResponse.BusRouteResponse endStation = stations.get(stations.size() - 1); // 초기값: 마지막 정류소
        double minDistance = distance(endStation.getLatitude(), endStation.getLongitude(), latitude, longitude);

        for (BusResponse.BusRouteResponse station : stations) {
            double currentDistance = distance(station.getLatitude(), station.getLongitude(), latitude, longitude);
            if (currentDistance < minDistance) {
                endStation = station;
                minDistance = currentDistance;
            }
        }
        return endStation;
    }

// 출발자의 위도경도 500M안에 있는 정류장 리스트 돌려주는 메서드
    public List<BusResponse.StationResponse> buildBusStationUrl(BusRequest.BusStationRequest req) throws IOException {

        //            공공데이터 포털에서 받은 Response 중 버스정류장 이름과 Id만 dto로 만들어 프런트에 보내준다
        List<BusResponse.StationResponse> nodeInfoList = new ArrayList<>();
//            공공데이터 포털에 요청할 url 만들기
        String urlBuilder = "http://apis.data.go.kr/1613000/BusSttnInfoInqireService/getCrdntPrxmtSttnList?" +
                URLEncoder.encode("serviceKey", "UTF-8") + "=" + apiServiceKey +
                "&" + URLEncoder.encode("pageNo", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("1", StandardCharsets.UTF_8) +
                "&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("20", "UTF-8") +
                "&" + URLEncoder.encode("_type", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8") +
                "&" + URLEncoder.encode("gpsLati", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(req.getLatitude()), "UTF-8") +
                "&" + URLEncoder.encode("gpsLong", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(req.getLongtitude()), "UTF-8");

        String jsonRouteNum = makeStringJsonResponse(urlBuilder);
        JsonNode items = getJsonNodeItems(jsonRouteNum);

        // 결과중 정류장Id,정류장 이름만 Dto로 만든후 List<DTO>에 추가
        if (items.isArray()) {
            for (JsonNode item : items) {
                String nodeno = item.path("nodenm").asText();
                String nodenm = item.path("nodeid").asText();
                Long routeno = item.path("routeno").asLong();
                BusResponse.StationResponse nodeInfoDto = new BusResponse.StationResponse(nodenm, nodeno, routeno);
                nodeInfoList.add(nodeInfoDto);
            }
        }
        return nodeInfoList;
    }

    //        버스 경로 가져오려면 정류소 Id로 노선 번호를 뽑음, 그걸로 버스 노선 번호(routeid)들을 뽑고, routeid로 정류장 목록조회
    public BusResponse.RouteNumList getRouteByStationId(BusRequest.RouteNumRequest req) throws IOException {
        List<String> routes = new ArrayList<>();

        StringBuilder stringBuilderRouteNo = new StringBuilder("http://apis.data.go.kr/1613000/BusSttnInfoInqireService/getSttnThrghRouteList?");
        String routeNo = urlDaeguAppend(stringBuilderRouteNo, 1).append("&").append(URLEncoder.encode("nodeid", "UTF-8")).append("=").append(URLEncoder.encode(req.getStationId(), "UTF-8")).toString();

        String jsonResponse = makeStringJsonResponse(routeNo);
        JsonNode items = getJsonNodeItems(jsonResponse);

        if (items.isArray()) {
            for (JsonNode item : items) {
                String routeid = item.path("routeid").asText();
                routes.add(routeid);
            }
        }

        //            도착지의 위도 경도로 도착지 근처 station의 id를 찾음
        List<BusResponse.StationResponse> str = buildBusStationUrl(new BusRequest.BusStationRequest().from(req.getDestLatitude(), req.getDestLongtitude()));
        List<String> destStationIds = new ArrayList<>();
        for (BusResponse.StationResponse stationResponse : str) {
            destStationIds.add(stationResponse.getStationId());
        }

        List<String> destRoutesIds = new ArrayList<>();

        for (int i = 0; i < destStationIds.size(); i++) {
            StringBuilder destBuilderRouteNo = new StringBuilder("http://apis.data.go.kr/1613000/BusSttnInfoInqireService/getSttnThrghRouteList?");
            String destRouteNo = urlDaeguAppend(destBuilderRouteNo,1).append("&").append(URLEncoder.encode("nodeid", "UTF-8")).append("=").append(URLEncoder.encode(destStationIds.get(i), "UTF-8")).toString();

            String jResponse = makeStringJsonResponse(destRouteNo);
            JsonNode jItems = getJsonNodeItems(jResponse);

            if (jItems.isArray()) {
                for (JsonNode item : items) {
                    String routeid = item.path("routeid").asText();

                    destRoutesIds.add(routeid);
                }
            }
        }

        Set<String> routesSet = new HashSet<>(routes);
        routesSet.retainAll(destRoutesIds);

//            출발지에서 도착지까지 공통 노선 번호
        List<String> commonRoutes = new ArrayList<>(routesSet);

        return BusResponse.RouteNumList.from(commonRoutes);
    }

//    정류장 Id로 버스 도착시간 찾는 메서드
    public BusResponse.BusArrivalResonse getBusArrivalTime(BusRequest.BusArrivalRequest req) throws IOException {

        BusResponse.BusArrivalResonse ret = null;

        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1613000/ArvlInfoInqireService/getSttnAcctoSpcifyRouteBusArvlPrearngeInfoList?");
        String finalUrl = urlDaeguAppend(urlBuilder, 1).append("&").append(URLEncoder.encode("nodeId", "UTF-8")).append("=").append(URLEncoder.encode(req.getStationId(), "UTF-8"))
                .append("&").append(URLEncoder.encode("routeId","UTF-8")).append("=").append(URLEncoder.encode(req.getRouteId(),"UTF-8")).toString();

        String jsonResponse = makeStringJsonResponse(finalUrl);
        JsonNode items = getJsonNodeItems(jsonResponse);

        if(items.isEmpty()){
            throw new CommonException(ExceptionCode.NO_BUS_COMMING);
        }

        if (items.isArray()) {
            for (JsonNode item : items) {
                String routeNumber = item.path("routeno").asText();
                String routeType = item.path("routetp").asText();
                String busType = item.path("vehicletp").asText();
                Long numOfStationBeforeArrival = item.path("arrprevstationcnt").asLong();
                Long arrivalMin = item.path("arrtime").asLong();

                if (Objects.equals(busType, "저상버스")) {
                    ret = new BusResponse.BusArrivalResonse(routeNumber, routeType, busType, numOfStationBeforeArrival, arrivalMin / 60);
                    break;
                }
            }
        }
        if(ret==null){
            throw new CommonException(ExceptionCode.NO_BUS_COMMING);
        }

        return ret;
    }

//    출발지에서 도착지까지 정류장들 찾는 메서드
    public List<BusResponse.BusRouteResponse> getBusRoute(BusRequest.BusRouteRequest req) throws IOException {

        List<BusResponse.BusRouteResponse> ret = new ArrayList<>();
        List<BusResponse.BusRouteResponse> allStations = new ArrayList<>();

        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1613000/BusRouteInfoInqireService/getRouteAcctoThrghSttnList?");
        urlBuilder.append(URLEncoder.encode("serviceKey", "UTF-8")).append("=").append(apiServiceKey);
        urlBuilder.append("&").append(URLEncoder.encode("pageNo", "UTF-8")).append("=").append(URLEncoder.encode("1", "UTF-8"));
        urlBuilder.append("&").append(URLEncoder.encode("numOfRows", "UTF-8")).append("=").append(URLEncoder.encode("100", "UTF-8"));
        urlBuilder.append("&").append(URLEncoder.encode("_type", "UTF-8")).append("=").append(URLEncoder.encode("json", "UTF-8"));
        urlBuilder.append("&").append(URLEncoder.encode("cityCode", "UTF-8")).append("=").append(URLEncoder.encode("22", "UTF-8"));
        String finalUrl = urlBuilder.append("&").append(URLEncoder.encode("routeId", "UTF-8")).append("=").append(URLEncoder.encode(req.getRouteNum(), "UTF-8")).toString();

        String jsonResponse = makeStringJsonResponse(finalUrl);
        JsonNode items = getJsonNodeItems(jsonResponse);

        if (items.isArray()) {
            for (JsonNode item : items) {
                Double latitude = item.path("gpslati").asDouble();
                Double longitude = item.path("gpslong").asDouble();
                String nodeid = item.path("nodeid").asText();
                String stationName = item.path("nodenm").asText();
                Long stationNum = item.path("nodeord").asLong();
                Long upDown = item.path("updowncd").asLong();
                BusResponse.BusRouteResponse bsi = new BusResponse.BusRouteResponse(latitude, longitude, nodeid, stationName, stationNum, upDown);
                allStations.add(bsi);
            }
        }

        if (allStations.isEmpty()) {
            throw new CommonException(ExceptionCode.Route_NOT_FOUND);
        }

        BusResponse.BusRouteResponse startStation = findClosestStartStation(allStations, req.getStartLati(), req.getStartLong());
        BusResponse.BusRouteResponse endStation = findClosestEndStation(allStations, req.getDestLatitude(), req.getDestLongtitude());

        boolean recording = false;
        boolean startFound = false;

        // 경로에 있는 모든 정류장 중 출발정류장~도착 정류장을 프런트에게 돌려준다.
        for (BusResponse.BusRouteResponse station : allStations) {
            if(station.getNodeid().trim().intern().equals(startStation.getNodeid().trim().intern())) {
                recording = true;
                startFound = true;
                ret.add(station);
                continue;
            }

            if (recording) {
                ret.add(station);
            }

            if (startFound && station.getNodeid().trim().intern().equals(endStation.getNodeid().trim().intern())){
                ret.add(station);
                break;
            }
        }

//        경로중에 출발지와 도착지가 없으면 StartFound는 False
        if(!startFound){
            throw new CommonException(ExceptionCode.STATION_NOT_EXIST);
        }
        return ret;
    }

//    경로에 있는 정류장들 위도경도 찾기
    public BusResponse.busNumStationId getStationXY(BusRequest.BusStationXYRequest req) throws IOException {
        int pageNo = 1;
        boolean found = false;
        BusResponse.busNumStationId ret = null;

        while (!found && pageNo <= 10) { // 페이지 번호가 10을 초과하면 루프 종료
            StringBuilder stringBuilder = new StringBuilder("http://apis.data.go.kr/1613000/BusRouteInfoInqireService/getRouteAcctoThrghSttnList?");
            String finalUrl = urlDaeguAppend(stringBuilder, pageNo).append("&").append(URLEncoder.encode("routeId", "UTF-8")).append("=").append(URLEncoder.encode(req.getRouteId(), "UTF-8")).toString();

            String jsonResponse = makeStringJsonResponse(finalUrl);
            JsonNode items = getJsonNodeItems(jsonResponse);

            if (items.isArray()) {
                for (JsonNode item : items) {
                    Double gpslati = item.path("gpslati").asDouble();
                    Double gpslong = item.path("gpslong").asDouble();
                    String stId = item.path("nodeid").asText();

                    if (stId.equals(req.getStationId())) {
                        ret = BusResponse.busNumStationId.builder()
                                .stationId(stId)
                                .latitude(gpslati)
                                .longitude(gpslong)
                                .build();

                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                pageNo++;
            }
        }

        //        경로중에 출발지와 도착지가 없으면 StartFound는 False
        if(!found){
            throw new CommonException(ExceptionCode.STATION_NOT_EXIST);
        }
        return ret;
    }

//    버스 번호 찾는 메서드
    public BusResponse.busNum getBusNum(BusRequest.BusNumRequest req) throws IOException {
        BusResponse.busNum ret = null;
        StringBuilder stringBuilder = new StringBuilder("http://apis.data.go.kr/1613000/BusRouteInfoInqireService/getRouteInfoIem?");
        String finalUrl = urlDaeguAppend(stringBuilder,1).append("&").append(URLEncoder.encode("routeId", "UTF-8")).append("=").append(URLEncoder.encode(req.getRouteId(), "UTF-8")).toString();
        System.out.println(finalUrl);

        String jsonResponse = makeStringJsonResponse(finalUrl);
        JsonNode items = getJsonNodeItems(jsonResponse);
        System.out.println(items);

        String busNum = items.path("routeno").asText();
        String num = busNum.replaceAll("\\[.*?\\]", "").trim(); //한글 제외하고 번호만
        ret = BusResponse.busNum.from(num);

        return ret;
    }
}
