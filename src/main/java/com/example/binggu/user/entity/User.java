package com.example.binggu.user.entity;

import com.example.binggu.user.dto.response.UserResponsse;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Map;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ElementCollection
    @CollectionTable(name = "ROUTE", joinColumns = @JoinColumn(name = "USER_ID"))
    @MapKeyColumn(name = "route_id")
    private Map<Long, Route> routes;

    public void addRoute(Long routeId, Route route) {
        this.routes.put(routeId, route);
    }

    public static User from(User u,Map<Long, Route> route){
        return User.builder()
                .id(u.getId())
                .name(u.getName())
                .routes(route)
                .build();
    }
}
