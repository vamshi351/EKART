package com.example.demo.service;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.ResetPasswordRequest;
import com.example.demo.dto.UpdateEmailRequest;
import com.example.demo.dto.UpdateUserRequest;
import com.example.demo.dto.VerifyOtpRequest;
import com.example.demo.model.User;
import com.example.demo.response.LoginResponse;

public interface UserService extends UserDetailsService {
    String register(RegisterRequest request);
    LoginResponse verifyOtp(VerifyOtpRequest request);
    LoginResponse login(String email, String password);
    LoginResponse loginWithoutAuthentication(String email);
    User updateUser(Long id, UpdateUserRequest request);
    User updateEmail(Long id, UpdateEmailRequest request);
    User getUserById(Long id);
    User getUserByEmail(String email);
	User updateUserByUsername(String username, UpdateUserRequest request);
	void deleteUserById(Long id);
	String resendOtp(String email);
	String initiateForgotPassword(String email);
	String resetPassword(ResetPasswordRequest request);	
	List<User> findAllUsers();

}