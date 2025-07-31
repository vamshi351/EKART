package com.example.demo.controller;

//UserController.java

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.UpdateEmailRequest;
import com.example.demo.dto.UpdateUserRequest;
import com.example.demo.service.UserService;

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

 @PutMapping("/update-email/{id}")
 public ResponseEntity<?> updateEmail(@PathVariable Long id,
                                      @RequestBody UpdateEmailRequest request) {
     return ResponseEntity.ok(userService.updateEmail(id, request));
 }

 @GetMapping("/me")
 public ResponseEntity<?> getMe() {
     // In real app: extract from SecurityContext
     return ResponseEntity.ok("Authenticated user info");
 }
}