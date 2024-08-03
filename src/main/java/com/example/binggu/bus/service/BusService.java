package com.example.binggu.bus.service;

import com.example.binggu.bus.dto.request.BusRequest;
import com.example.binggu.bus.dto.response.BusResponse;
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

@Service
@RequiredArgsConstructor
public class BusService {

    @Value("${api.service.key}")
    private String apiServiceKey;

    private StringBuilder urlDaeguAppend(StringBuilder url) throws UnsupportedEncodingException {
        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append(URLEncoder.encode("serviceKey", "UTF-8")).append("=").append(apiServiceKey);
        urlBuilder.append("&").append(URLEncoder.encode("pageNo", "UTF-8")).append("=").append(URLEncoder.encode("1", "UTF-8"));
        urlBuilder.append("&").append(URLEncoder.encode("numOfRows", "UTF-8")).append("=").append(URLEncoder.encode("10", "UTF-8"));
        urlBuilder.append("&").append(URLEncoder.encode("_type", "UTF-8")).append("=").append(URLEncoder.encode("json", "UTF-8"));
        urlBuilder.append("&").append(URLEncoder.encode("cityCode", "UTF-8")).append("=").append(URLEncoder.encode("22", "UTF-8"));

        return urlBuilder;
    }

    public String makeStringJsonResponse(String finalUrl) throws IOException {
        URL url = new URL(finalUrl);
        HttpURLConnection conn = (HttpURLConnection)
                url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        BufferedReader rd;
        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
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

    public JsonNode getJsonNodeItems(String jsonResponse) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        return rootNode.path("response").path("body").path("items").path("item");
    }

        public List<BusResponse.StationResponse> buildBusStationUrl(BusRequest.BusStationRequest req) throws IOException {

            //            공공데이터 포털에서 받은 Response 중 버스정류장 이름과 Id만 dto로 만들어 프런트에 보내준다
            List<BusResponse.StationResponse> nodeInfoList = new ArrayList<>();
//            공공데이터 포털에 요청할 url 만들기
            String urlBuilder = "http://apis.data.go.kr/1613000/BusSttnInfoInqireService/getCrdntPrxmtSttnList?" +
                    URLEncoder.encode("serviceKey", "UTF-8") + "=" + apiServiceKey +
                    "&" + URLEncoder.encode("pageNo", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("1", StandardCharsets.UTF_8) +
                    "&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("10", "UTF-8") + /*한 페이지 결과 수*/
                    "&" + URLEncoder.encode("_type", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8") + /*데이터 타입(xml, json)*/
                    "&" + URLEncoder.encode("gpsLati", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(req.getLatitude()), "UTF-8") + /*WGS84 위도 좌표*/
                    "&" + URLEncoder.encode("gpsLong", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(req.getLongtitude()), "UTF-8");

            String jsonRouteNum = makeStringJsonResponse(urlBuilder);
            JsonNode items = getJsonNodeItems(jsonRouteNum);

                // 결과중 정류장Id,정류장 이름만 Dto로 만든후 List<DTO>에 추가
                if (items.isArray()) {
                    for (JsonNode item : items) {
                        String nodeno = item.path("nodenm").asText();
                        String nodenm = item.path("nodeid").asText();
                        Long routeno = item.path("nodeno").asLong();
                        BusResponse.StationResponse nodeInfoDto = new BusResponse.StationResponse(nodenm, nodeno,routeno);
                        nodeInfoList.add(nodeInfoDto);
                    }
                }

            return nodeInfoList;
        }

//        버스 경로 가져오려면 정류소 Id로 노선 번호를 뽑음, 그걸로 버스 노선 번호(routeid)들을 뽑고, routeid로 정류장 목록조회
        public BusResponse.RouteNumList getRouteByStationId(String stationId) throws IOException {
            List<String> routes = new ArrayList<>();

            StringBuilder stringBuilderRouteNo = new StringBuilder("http://apis.data.go.kr/1613000/BusSttnInfoInqireService/getSttnThrghRouteList?");
            String routeNo = urlDaeguAppend(stringBuilderRouteNo).append("&").append(URLEncoder.encode("nodeid", "UTF-8")).append("=").append(URLEncoder.encode(stationId, "UTF-8")).toString();

            String jsonResponse = makeStringJsonResponse(routeNo);
            JsonNode items = getJsonNodeItems(jsonResponse);

            if(items.isArray()){
                for(JsonNode item : items){
                    String routeid = item.path("routeid").asText();

                    routes.add(routeid);
                }
            }
            return BusResponse.RouteNumList.from(routes);
        }

        public List<BusResponse.BusArrivalResonse> getBusArrivalTime(BusRequest.BusArrivalRequest req) throws IOException{

            List<BusResponse.BusArrivalResonse> ret = new ArrayList<>();

            StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1613000/ArvlInfoInqireService/getSttnAcctoArvlPrearngeInfoList?");
            String finalUrl = urlDaeguAppend(urlBuilder).append("&").append(URLEncoder.encode("nodeId", "UTF-8")).append("=").append(URLEncoder.encode(req.getStationId(), "UTF-8")).toString();

            String jsonResponse = makeStringJsonResponse(finalUrl);
            JsonNode items = getJsonNodeItems(jsonResponse);

            if(items.isArray()){
                for(JsonNode item : items){
                    String routeNumber = item.path("routeno").asText();
                    String routeType = item.path("routetp").asText();
                    String busType = item.path("vehicletp").asText();
                    Long numOfStationBeforeArrival= item.path("arrprevstationcnt").asLong();
                    Long arrivalMin = item.path("arrtime").asLong();

                    if(Objects.equals(busType, "저상버스")) {
                        BusResponse.BusArrivalResonse res = new BusResponse.BusArrivalResonse(routeNumber, routeType, busType, numOfStationBeforeArrival, arrivalMin/60);
                        ret.add(res);
                    }
                }
            }
            return ret;
        }

        public void getBusMovingTime(){}

        public List<BusResponse.BusRouteResponse> getBusRoute(BusRequest.BusRouteRequest req) throws IOException{

            List<BusResponse.BusRouteResponse> ret = new ArrayList<>();

            StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1613000/BusRouteInfoInqireService/getRouteAcctoThrghSttnList?");
            String finalUrl = urlDaeguAppend(urlBuilder).append("&").append(URLEncoder.encode("routeId", "UTF-8")).append("=").append(URLEncoder.encode(req.getRouteNum(), "UTF-8")).toString();
            String jsonResponse = makeStringJsonResponse(finalUrl);
            JsonNode items = getJsonNodeItems(jsonResponse);

            if(items.isArray()){
                for(JsonNode item : items){
                    Double latitude = item.path("gpslati").asDouble();
                    Double longitude = item.path("gpslong").asDouble();
                    String stationName = item.path("nodenm").asText();
                    Long stationNum = item.path("nodeord").asLong();
                    Long upDown = item.path("updowncd").asLong();
                    BusResponse.BusRouteResponse bsi = new BusResponse.BusRouteResponse(latitude, longitude, stationName,stationNum, upDown);
                    ret.add(bsi);
                }
            }
            return ret;
        }
    }