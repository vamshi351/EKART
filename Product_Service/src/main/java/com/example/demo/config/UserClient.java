package com.example.demo.config;

import com.example.demo.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "api-gateway", path = "/api/users")
public interface UserClient {

    @GetMapping("/me")
    UserDTO getMe(@RequestHeader("Authorization") String token);
}