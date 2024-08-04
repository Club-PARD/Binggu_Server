package com.example.binggu.user.service;

import com.example.binggu.exception.CommonException;
import com.example.binggu.exception.ExceptionCode;
import com.example.binggu.user.dto.request.UserRequest;
import com.example.binggu.user.dto.response.UserResponsse;
import com.example.binggu.user.entity.Route;
import com.example.binggu.user.entity.User;
import com.example.binggu.user.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;

    @Transactional
    public UserResponsse.UserInfo find(Long id){
        return UserResponsse.UserInfo.from(userRepo.findById(id).orElseThrow(()->new CommonException(ExceptionCode.USER_NOT_FOUND)));
    }

    @Transactional
    public UserResponsse.UserCreateRes addUser() {
        userRepo.save(new User(userRepo.count()+1));
        return UserResponsse.UserCreateRes.from(userRepo.count());
    }

    @Transactional
    public void addRoute(Long id,UserRequest.AddRoutes req){
        User u = userRepo.findById(id).orElseThrow(() -> new CommonException(ExceptionCode.USER_NOT_FOUND));
        u.addRoute(req.getRouteId(),req.getRoute());
        userRepo.save(u);
    }

    @Transactional
    public void deleteRoute(Long userId, String busId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new CommonException(ExceptionCode.USER_NOT_FOUND));

        Long routeIdToDelete;
        try {
            routeIdToDelete = Long.parseLong(busId);
        } catch (NumberFormatException e) {
            throw new CommonException(ExceptionCode.INVALID_ROUTE_ID);
        }

        if (user.getRoutes().containsKey(routeIdToDelete)) {
            user.getRoutes().remove(routeIdToDelete);
            userRepo.save(user);
        } else {
            throw new CommonException(ExceptionCode.FAVORITE_ROUTE_NOT_FOUND);
        }
    }

}
