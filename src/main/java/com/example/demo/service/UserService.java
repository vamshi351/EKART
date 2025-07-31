package com.example.demo.service;

//UserService.java



import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.UpdateEmailRequest;
import com.example.demo.dto.UpdateUserRequest;
import com.example.demo.model.User;

public interface UserService  {
 User register(RegisterRequest request);
 String login(String email, String password);
 User updateUser(Long id, UpdateUserRequest request);
 User updateEmail(Long id, UpdateEmailRequest request);
 User getUserById(Long id);
 User getUserById(String email);
 User getUserByEmail(String email);
}