package com.example.demo.controller;

//UserController.java

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.UpdateEmailRequest;
import com.example.demo.dto.UpdateUserRequest;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.VerifyOtpRequest;
import com.example.demo.exception.OtpValidationException;
import com.example.demo.model.User;
import com.example.demo.response.LoginResponse;
import com.example.demo.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

 @Autowired
 private UserService userService;

 @GetMapping("/{id}")
 @PreAuthorize("hasRole('ADMIN')")
 public ResponseEntity<?> getUser(@PathVariable Long id) {
     return ResponseEntity.ok(userService.getUserById(id));
 }

 @PutMapping("/edit/{id}")
 @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
 public ResponseEntity<?> editUser(@PathVariable Long id,
                                   @RequestBody UpdateUserRequest request) {
     return ResponseEntity.ok(userService.updateUser(id, request));
 }

 @PutMapping("/update-email")
 public ResponseEntity<?> updateEmail(
         @RequestBody @Valid UpdateEmailRequest request,
         Authentication authentication) {

     String currentEmail = authentication.getName(); // Current logged-in user's email
     User user = userService.getUserByEmail(currentEmail);

     User updatedUser = userService.updateEmail(user.getId(), request);
     return ResponseEntity.ok("OTP sent to new email: " + updatedUser.getPendingEmail());
 }

 @PostMapping("/verify-otp-mail-updation")
 public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
     try {
         LoginResponse result = userService.verifyOtp(request); // Handles OTP + JWT regeneration
         return ResponseEntity.ok(result); // New JWT and User info
     } catch (OtpValidationException | UsernameNotFoundException e) {
         return ResponseEntity.badRequest().body(e.getMessage());
     }
 }


 @GetMapping("/me")
 public ResponseEntity<?> getMe(Authentication authentication) {
     if (authentication == null || authentication.getName() == null) {
         return ResponseEntity.status(401).body("Unauthorized");
     }
     User user = userService.getUserByEmail(authentication.getName());
     return ResponseEntity.ok(new UserDTO(user.getName(), user.getEmail(), user.getPhone()));
 }


}