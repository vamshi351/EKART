package com.example.demo.controller;

//AuthController.java

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@CrossOrigin
public class AuthController {

 @Autowired
 private UserService userService;

 @PostMapping("/register")
 public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
     String message = userService.register(request);
     return ResponseEntity.ok(message);
 }


 @Autowired
 private AuthenticationManager authenticationManager;

 @PostMapping("/login")
 public LoginResponse login(@RequestBody LoginRequest request) {
     authenticationManager.authenticate(
         new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
     );
     return userService.loginWithoutAuthentication(request.getEmail());
 }


 
 @PostMapping("/verify-otp")
 public ResponseEntity<LoginResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
     LoginResponse jwtToken = userService.verifyOtp(request);
     return ResponseEntity.ok(jwtToken);
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