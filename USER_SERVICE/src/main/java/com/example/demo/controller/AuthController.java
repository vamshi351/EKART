package com.example.demo.controller;

//AuthController.java

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.config.JwtUtil;
import com.example.demo.dto.EmailRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.ResetPasswordRequest;
import com.example.demo.dto.VerifyOtpRequest;
import com.example.demo.response.LoginResponse;
import com.example.demo.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

 @Autowired
 private UserService userService;
 
 @Autowired
 private JwtUtil jwtUtil;
 
 

 @PostMapping("/register")
 public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
     String message = userService.register(request);
     return ResponseEntity.ok(message);
 }


 @Autowired
 private AuthenticationManager authenticationManager;

 @PostMapping("/login")
 public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
     LoginResponse response = userService.login(request.getEmail(), request.getPassword());
     System.err.println(response);
     return ResponseEntity.ok()
             .contentType(MediaType.APPLICATION_JSON)
             .body(response);
 }





 
 @PostMapping("/verify-otp")
 public ResponseEntity<LoginResponse> verifyOtp(@RequestBody VerifyOtpRequest request) {
     LoginResponse response = userService.verifyOtp(request);
     return ResponseEntity.ok(response);
 }
 
 @PostMapping("/forgot-password")
 public ResponseEntity<String> forgotPassword(@RequestBody EmailRequest email) {
	 System.err.println(email);
     String result = userService.initiateForgotPassword(email.getEmail());
     System.out.println(result);
     return ResponseEntity.ok(result);
    
 }
 
 @PostMapping("/reset-password")
 public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
     String result = userService.resetPassword(request);
     return ResponseEntity.ok(result);
 }

 
}