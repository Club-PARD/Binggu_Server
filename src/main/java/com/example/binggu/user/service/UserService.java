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
    public UserResponsse.UserCreateRes addUser(UserRequest.SignInUser req) {
        userRepo.save(UserRequest.SignInUser.from(req));
        return UserResponsse.UserCreateRes.from(userRepo.count());
    }

    @Transactional
    public void addRoute(Long id,UserRequest.AddRoutes req){
        User u = userRepo.findById(id).orElseThrow(() -> new CommonException(ExceptionCode.USER_NOT_FOUND));
        u.addRoute(req.getRouteId(),req.getRoute());
        userRepo.save(u);
    }
}
