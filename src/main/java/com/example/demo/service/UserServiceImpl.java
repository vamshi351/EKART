package com.example.demo.service;

//UserServiceImpl.java


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.UpdateEmailRequest;
import com.example.demo.dto.UpdateUserRequest;
import com.example.demo.exception.EmailAlreadyExistsException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.userservice.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

 private final UserRepository userRepository;
 private final PasswordEncoder passwordEncoder;
 private final AuthenticationManager authManager;
 private final EmailService emailService;

 @Override
 public User register(RegisterRequest request) {
     if (userRepository.findByEmail(request.getEmail()).isPresent()) {
         throw new EmailAlreadyExistsException("Email already in use: " + request.getEmail());
     }

     User user = User.builder()
             .name(request.getName())
             .email(request.getEmail())
             .password(passwordEncoder.encode(request.getPassword()))
             .role(User.Role.USER)
             .build();

     User savedUser = userRepository.save(user);
     emailService.sendVerificationEmail(savedUser.getEmail());
     return savedUser;
 }

 @Override
 public String login(String email, String password) {
     authManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
     UserDetails userDetails = loadUserByUsername(email);
     return jwtUtil.generateToken(email);
 }

 @Override
 public User updateUser(Long id, UpdateUserRequest request) {
     User user = userRepository.findById(id)
             .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

     if (request.getName() != null) {
         user.setName(request.getName());
     }
     if (request.getRole() != null) {
         user.setRole(User.Role.valueOf(request.getRole().toUpperCase()));
     }
     return userRepository.save(user);
 }

 @Override
 public User updateEmail(Long id, UpdateEmailRequest request) {
     User user = userRepository.findById(id)
             .orElseThrow(() -> new UserNotFoundException("User not found"));

     if (userRepository.findByEmail(request.getNewEmail()).isPresent()) {
         throw new EmailAlreadyExistsException("Email already taken: " + request.getNewEmail());
     }

     user.setEmail(request.getNewEmail());
     user.setEmailVerified(false);
     User updated = userRepository.save(user);
     emailService.sendVerificationEmail(updated.getEmail());
     return updated;
 }

 @Override
 public User getUserById(Long id) {
     return userRepository.findById(id)
             .orElseThrow(() -> new UserNotFoundException("User not found"));
 }

 @Override
 public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
     return userRepository.findByEmail(email)
             .orElseThrow(() -> new UsernameNotFoundException("User not found"));
 }
}