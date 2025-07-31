package com.example.demo.service;

import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.config.JwtUtil;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.UpdateEmailRequest;
import com.example.demo.dto.UpdateUserRequest;
import com.example.demo.dto.VerifyOtpRequest;
import com.example.demo.exception.EmailAlreadyExistsException;
import com.example.demo.exception.OtpValidationException;
import com.example.demo.model.Role;
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
    private final JwtUtil jwtUtil;

    @Override
    public User register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already in use: " + request.getEmail());
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
                .role(Role.USER) // FIXED: Correct enum usage
                .otp(otp)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);
        emailService.sendOtpEmail(savedUser.getEmail(), otp); // Make sure this exists
        return savedUser;
    }

    private String generateOtp() {
        return String.format("%06d", new java.security.SecureRandom().nextInt(1_000_000));
    }

    public String verifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.isEmailVerified()) {
            return jwtUtil.generateToken(user.getEmail());
        }

        if (request.getOtp().equals(user.getOtp())) {
            user.setEmailVerified(true);
            user.setOtp(null); // Clear OTP
            userRepository.save(user);
            return jwtUtil.generateToken(user.getEmail());
        } else {
            throw new OtpValidationException("Invalid OTP");
        }
    }

    @Override
    public String login(String email, String password) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!user.isEmailVerified()) {
            throw new OtpValidationException("Email not verified. Please verify OTP.");
        }

        return jwtUtil.generateToken(user.getEmail());
    }

    @Override
    public User updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getRole() != null) {
            user.setRole(Role.valueOf(request.getRole().toUpperCase())); // Assumes valid values like "ADMIN"
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

        String otp = generateOtp();
        user.setOtp(otp);

        User updated = userRepository.save(user);
        emailService.sendOtpEmail(updated.getEmail(), otp);
        return updated;
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

	@Override
	public User getUserById(String email) {
		Optional<User> byId = userRepository.findById(null);
		return byId.get();
	}

	@Override
	public User getUserByEmail(String email) {
		Optional<User> byEmail = userRepository.findByEmail(email);
		return byEmail.get();
	}
}
