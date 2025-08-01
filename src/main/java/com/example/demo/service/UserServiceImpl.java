package com.example.demo.service;

import java.util.Optional;

import org.springframework.context.annotation.Lazy; // Import @Lazy
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Added for UserDetails
import org.springframework.security.core.userdetails.UserDetails; // Added for UserDetails
import org.springframework.security.core.userdetails.UserDetailsService; // Added for UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.config.JwtUtil;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.UpdateEmailRequest;
import com.example.demo.dto.UpdateUserRequest;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.VerifyOtpRequest;
import com.example.demo.exception.EmailAlreadyExistsException;
import com.example.demo.exception.OtpValidationException;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.response.LoginResponse;
import com.example.demo.exception.UserNotFoundException; // Assuming this is correct path

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {
	
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;


    @Override
    public String register(RegisterRequest request) {
        // ... (your existing register method)
        Optional<User> existingUserOpt = userRepository.findByEmail(request.getEmail());

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            if (existingUser.isEmailVerified()) {
                throw new EmailAlreadyExistsException("Email already in use: " + request.getEmail());
            } else {
                // User exists but not verified - resend OTP
                String otp = generateOtp();
                existingUser.setOtp(otp);
                userRepository.save(existingUser);
                emailService.sendOtpEmail(existingUser.getEmail(), otp);
                return "OTP re-sent to existing unverified user.";
            }
        }

        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new RuntimeException("Phone number already in use: " + request.getPhone());
        }

        String otp = generateOtp();

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .otp(otp)
                .emailVerified(false)
                .build();

        userRepository.save(user);
        emailService.sendOtpEmail(user.getEmail(), otp);
        return "OTP sent. Please verify to complete registration.";
    }

    private String generateOtp() {
        return String.format("%06d", new java.security.SecureRandom().nextInt(1_000_000));
    }

    @Override
    public String verifyOtp(VerifyOtpRequest request) {
        // ... (your existing verifyOtp method)
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.isEmailVerified()) {
            return "Email already verified. Please login.";
        }

        if (request.getOtp().equals(user.getOtp())) {
            user.setEmailVerified(true);
            user.setOtp(null);
            userRepository.save(user);
            return "OTP verified successfully. Please login to continue.";
        } else {
            throw new OtpValidationException("Invalid OTP");
        }
    }

    @Override
    public User updateUser(Long id, UpdateUserRequest request) {
        // ... (your existing updateUser method)
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getRole() != null) {
            user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        }

        return userRepository.save(user);
    }

    @Override
    public User updateEmail(Long id, UpdateEmailRequest request) {
        // ... (your existing updateEmail method)
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (userRepository.findByEmail(request.getNewEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already taken: " + request.getNewEmail());
        }

        user.setEmail(request.getNewEmail());
        user.setEmailVerified(false);

        String otp = generateOtp();
        user.setOtp(otp);

        User updated = userRepository.save(user);
        emailService.sendOtpEmail(updated.getEmail(), otp);
        return updated;
    }

    public LoginResponse login(String email, String password) {
        // DO NOT authenticate here, authenticate in controller or via filter!
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (!user.isEmailVerified()) {
            throw new OtpValidationException("Email not verified. Please verify OTP.");
        }
        String token = jwtUtil.generateToken(user.getEmail());
        return new LoginResponse(token, new UserDTO(user));
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    // Implementation for UserDetailsService
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        // Return a Spring Security UserDetails object:
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isEmailVerified(), // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

    @Override
    public LoginResponse loginWithoutAuthentication(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!user.isEmailVerified()) {
            throw new OtpValidationException("Email not verified. Please verify OTP.");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new LoginResponse(token, new UserDTO(user));
    }
    
    

}