package com.example.demo.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.example.demo.dto.UserDTO;

@FeignClient(name = "user-service", path = "/api/users", configuration = FeignConfig.class)
public interface UserClient {

    @GetMapping("/me")
    UserDTO getMe(@RequestHeader("Authorization") String token);
}
