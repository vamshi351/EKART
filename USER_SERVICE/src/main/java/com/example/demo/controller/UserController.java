package com.example.demo.controller;

import java.util.List;

//UserController.java

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.config.JwtUtil;
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
 
 @Autowired
 private JwtUtil jwtUtil;
 
 

 @GetMapping("/{id}")
 @PreAuthorize("hasRole('ADMIN')")
 public ResponseEntity<?> getUser(@PathVariable Long id) {
     return ResponseEntity.ok(userService.getUserById(id));
 }

 @PutMapping("/edit")
 @PreAuthorize("hasAnyRole('USER', 'ADMIN','SELLER')")
 public ResponseEntity<?> editUser(@RequestBody UpdateUserRequest request, Authentication authentication) {
     String username = authentication.getName(); // Extract from JWT
     return ResponseEntity.ok(userService.updateUserByUsername(username, request));
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
 
 @DeleteMapping("/delete/{id}")
 @PreAuthorize("hasRole('ADMIN')")
 public ResponseEntity<?> deleteUser(@PathVariable Long id) {
     userService.deleteUserById(id);
     return ResponseEntity.ok("User with ID " + id + " deleted successfully.");
 }

 @GetMapping("/me")
 public ResponseEntity<?> getMe(Authentication authentication) {
     if (authentication == null || authentication.getName() == null) {
         return ResponseEntity.status(401).body("Unauthorized");
     }
     User user = userService.getUserByEmail(authentication.getName());
     return ResponseEntity.ok(new UserDTO(user.getName(),user.getPhone(),user.getEmail()));
 }
 
 @GetMapping("/all")
 @PreAuthorize("hasRole('ADMIN')")
 public ResponseEntity<?> getListOfUsers(Authentication authentication) {
     if (authentication == null || authentication.getName() == null) {
         return ResponseEntity.status(401).body("Unauthorized");
     }
     List<User> users = userService.findAllUsers();
     return ResponseEntity.ok(users);
 }
 
//UserController.java

 @PostMapping("/request-seller-role")
 @PreAuthorize("hasRole('USER')") // only USER can request
 public ResponseEntity<?> requestSellerRole(Authentication authentication) {
     String email = authentication.getName();
     try {
         userService.requestSellerRole(email);

         // fetch updated user
         User updatedUser = userService.getUserByEmail(email);

         // generate new token with SELLER role
         String newToken = jwtUtil.generateToken(
                 updatedUser.getEmail(),
                 List.of(updatedUser.getRole().name()) // ensure it's a String
         );

         return ResponseEntity.ok(
             new LoginResponse(newToken, updatedUser.getName(),
                     updatedUser.getEmail(), updatedUser.getRole().name())
         );

     } catch (IllegalStateException e) {
         return ResponseEntity.badRequest().body(e.getMessage());
     }
 }


 
 @GetMapping("/email/{email}")
 @PreAuthorize("hasAnyRole('USER', 'ADMIN','SELLER')")
 public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
     User user = userService.getUserByEmail(email);
     UserDTO userDTO = new UserDTO(user.getName(), user.getPhone(), user.getEmail());
     return ResponseEntity.ok(userDTO);
 }



}