package com.example.binggu.user.dto.response;

import com.example.binggu.user.entity.Route;
import com.example.binggu.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;


public class UserResponsse {
    @Getter
    @Builder
    public static class UserInfo{
        private Long id;
        private Map<Long, Route> routes;

        public static UserInfo from(User user){
            return UserInfo.builder()
                    .id(user.getId())
                    .routes(user.getRoutes())
                    .build();

        }
    }
    @Getter
    @Builder
    public static class UserCreateRes{
        private Long id;

        public static UserCreateRes from(Long numOfUser){
            return UserCreateRes.builder()
                    .id(numOfUser).build();
        }
    }
}
