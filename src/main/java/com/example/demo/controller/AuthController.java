package com.example.demo.controller;

//AuthController.java

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

 @Autowired
 private UserService userService;

 @PostMapping("/register")
 public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
     userService.register(request);
     return ResponseEntity.ok("User registered successfully");
 }

 @PostMapping("/login")
 public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) {
     String token = userService.login(request.getEmail(), request.getPassword());
     return ResponseEntity.ok(token);
 }
}