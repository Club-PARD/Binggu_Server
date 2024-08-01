package com.example.binggu.user.controller;

import com.example.binggu.user.dto.request.UserRequest;
import com.example.binggu.user.dto.response.UserResponsse;
import com.example.binggu.user.entity.User;
import com.example.binggu.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponsse.UserInfo> getUser(@PathVariable Long id){
        UserResponsse.UserInfo ret = userService.find(id);
        return ResponseEntity.ok().body(ret);
    }

    @PostMapping("")
    public ResponseEntity<UserResponsse.UserCreateRes> addUser(){
        return ResponseEntity.ok().body(userService.addUser());
    }

    @PostMapping("/{id}")
    public void addRoutes(@PathVariable Long id,@RequestBody UserRequest.AddRoutes req){
        userService.addRoute(id,req);
    }
}
