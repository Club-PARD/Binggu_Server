package com.example.binggu.bus.service;

import com.example.binggu.bus.dto.request.BusRequest;
import com.example.binggu.bus.dto.response.BusResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BusService {

    @Value("${api.service.key}")
    private String apiServiceKey;

        public List<BusResponse.StationResponse> buildBusStationUrl(BusRequest.BusStationRequest req) throws IOException {
//            공공데이터 포털에 요청할 url 만들기
            String urlBuilder = "http://apis.data.go.kr/1613000/BusSttnInfoInqireService/getCrdntPrxmtSttnList?" +
                    URLEncoder.encode("serviceKey", "UTF-8") + "=" + apiServiceKey +
                    "&" + URLEncoder.encode("pageNo", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("1", StandardCharsets.UTF_8) +
                    "&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("10", "UTF-8") + /*한 페이지 결과 수*/
                    "&" + URLEncoder.encode("_type", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8") + /*데이터 타입(xml, json)*/
                    "&" + URLEncoder.encode("gpsLati", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(req.getLatitude()), "UTF-8") + /*WGS84 위도 좌표*/
                    "&" + URLEncoder.encode("gpsLong", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(req.getLongtitude()), "UTF-8");

//            요청받기
            URL url = new URL(urlBuilder);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/json");
            System.out.println("Response code: " + conn.getResponseCode());

//            공공데이터 포털에서 받은 Response 중 버스정류장 이름과 Id만 dto로 만들어 프런트에 보내준다
            List<BusResponse.StationResponse> nodeInfoList = new ArrayList<>();
            try (BufferedReader rd = new BufferedReader(new InputStreamReader(
                    conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300 ? conn.getInputStream() : conn.getErrorStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
                String jsonResponse = sb.toString();
                System.out.println(jsonResponse);

                // JSON response 파싱
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(jsonResponse);
                JsonNode items = rootNode.path("response").path("body").path("items").path("item");

                // 결과중 정류장Id,정류장 이름만 Dto로 만든후 List<DTO>에 추가
                if (items.isArray()) {
                    for (JsonNode item : items) {
                        String nodeno = item.path("nodenm").asText();
                        String nodenm = item.path("nodeid").asText();
                        BusResponse.StationResponse nodeInfoDto = new BusResponse.StationResponse(nodenm, nodeno);
                        nodeInfoList.add(nodeInfoDto);
                    }
                }
            } finally {
                conn.disconnect();
            }

            return nodeInfoList;
        }

        public List<BusResponse.BusArrivalResonse> getBusArrivalTime(BusRequest.BusArrivalRequest req) throws IOException{
            StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1613000/ArvlInfoInqireService/getSttnAcctoArvlPrearngeInfoList?");
            urlBuilder.append(URLEncoder.encode("serviceKey", "UTF-8")).append("=").append(apiServiceKey);
            urlBuilder.append("&").append(URLEncoder.encode("pageNo", "UTF-8")).append("=").append(URLEncoder.encode("1", "UTF-8"));
            urlBuilder.append("&").append(URLEncoder.encode("numOfRows", "UTF-8")).append("=").append(URLEncoder.encode("10", "UTF-8"));
            urlBuilder.append("&").append(URLEncoder.encode("_type", "UTF-8")).append("=").append(URLEncoder.encode("json", "UTF-8"));
            urlBuilder.append("&").append(URLEncoder.encode("cityCode", "UTF-8")).append("=").append(URLEncoder.encode("22", "UTF-8"));
            urlBuilder.append("&").append(URLEncoder.encode("nodeId", "UTF-8")).append("=").append(URLEncoder.encode(req.getStationId(), "UTF-8"));

            String finalUrl = urlBuilder.toString();
            System.out.println(finalUrl);
            URL url = new URL(finalUrl);
            HttpURLConnection conn = (HttpURLConnection)
            url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/json");
            System.out.println("Response code: " + conn.getResponseCode());
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
            System.out.println(sb.toString());

            String jsonResponse = sb.toString();

            List<BusResponse.BusArrivalResonse> ret = new ArrayList<>();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode items = rootNode.path("response").path("body").path("items").path("item");

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
    }



