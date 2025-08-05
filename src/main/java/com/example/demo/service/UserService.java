package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.model.User;
import com.example.demo.response.LoginResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    String register(RegisterRequest request);
    LoginResponse verifyOtp(VerifyOtpRequest request);
    LoginResponse login(String email, String password);
    LoginResponse loginWithoutAuthentication(String email);
    User updateUser(Long id, UpdateUserRequest request);
    User updateEmail(Long id, UpdateEmailRequest request);
    User getUserById(Long id);
    User getUserByEmail(String email);
}