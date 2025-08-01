package com.example.demo.service;

import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.UpdateEmailRequest;
import com.example.demo.dto.UpdateUserRequest;
import com.example.demo.dto.VerifyOtpRequest;
import com.example.demo.model.User;
import com.example.demo.response.LoginResponse;

import jakarta.validation.Valid;

public interface UserService
{
    String register(RegisterRequest request);
    LoginResponse login(String email, String password);
    User updateUser(Long id, UpdateUserRequest request);
    User updateEmail(Long id, UpdateEmailRequest request);
    User getUserById(Long id);
    User getUserByEmail(String email);
    String verifyOtp(@Valid VerifyOtpRequest request);
	LoginResponse loginWithoutAuthentication(String email);
}
