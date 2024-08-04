package com.example.binggu.user.dto.request;

import com.example.binggu.user.entity.Route;
import com.example.binggu.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


public class UserRequest {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignInUser{
        private String name;

        public static User from(SignInUser req){
            return User.builder()
                    .name(req.name)
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddRoutes{
        private Long routeId;
        private Route route;
        private String busNum;
    }
}
