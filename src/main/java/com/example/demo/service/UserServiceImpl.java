package com.example.demo.service;

import java.util.Collections;
import java.util.Optional;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.response.LoginResponse;

import jakarta.validation.Valid;
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
        if (request.getPhone().length() != 10) {
            throw new IllegalArgumentException("Phone number must be 10 digits.");
        }

        Optional<User> existingUserOpt = userRepository.findByEmail(request.getEmail());
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            if (existingUser.isEmailVerified()) {
                throw new EmailAlreadyExistsException("Email already in use: " + request.getEmail());
            } else {
                String otp = generateOtp();
                existingUser.setOtp(otp);
                existingUser.setOtpGeneratedTime(System.currentTimeMillis());
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
                .otpGeneratedTime(System.currentTimeMillis())
                .build();

        userRepository.save(user);
        emailService.sendOtpEmail(user.getEmail(), otp);
        return "OTP sent to " + user.getEmail() + ". Please verify to complete registration.";
    }

    private String generateOtp() {
        return String.format("%06d", new java.security.SecureRandom().nextInt(1_000_000));
    }

    @Override
    public LoginResponse verifyOtp(@Valid VerifyOtpRequest request) {
        // Try finding user by current email
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        // If not found, try by pending email (email update scenario)
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByPendingEmail(request.getEmail());
        }

        User user = userOpt.orElseThrow(() ->
            new UsernameNotFoundException("User not found with email: " + request.getEmail())
        );

        long currentTime = System.currentTimeMillis();
        long expiry = 3 * 60 * 1000; // 3 minutes

        if (user.getOtpGeneratedTime() == null || (currentTime - user.getOtpGeneratedTime()) > expiry) {
            throw new OtpValidationException("OTP has expired. Please request a new one.");
        }

        if (!request.getOtp().equals(user.getOtp())) {
            throw new OtpValidationException("Invalid OTP");
        }

        // ✅ Commit email change if pending
        if (user.getPendingEmail() != null) {
            Optional<User> emailTaken = userRepository.findByEmail(user.getPendingEmail());
            if (emailTaken.isPresent() && !emailTaken.get().getId().equals(user.getId())) {
                throw new EmailAlreadyExistsException("Email already taken: " + user.getPendingEmail());
            }

            user.setEmail(user.getPendingEmail());
            user.setPendingEmail(null);
        }

        user.setEmailVerified(true);
        user.setOtp(null);
        user.setOtpGeneratedTime(null);

        User savedUser = userRepository.save(user);

        String token = jwtUtil.generateToken(savedUser.getEmail());
        UserDTO userDTO = new UserDTO(savedUser.getName(), savedUser.getEmail(), savedUser.getPhone());

        return new LoginResponse(token, userDTO);
    }


    @Override
    public User updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (request.getName() != null) user.setName(request.getName());
        if (request.getRole() != null) user.setRole(Role.valueOf(request.getRole().toUpperCase()));

        return userRepository.save(user);
    }

    @Override
    public User updateEmail(Long id, UpdateEmailRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Optional<User> emailOwner = userRepository.findByEmail(request.getNewEmail());
        if (emailOwner.isPresent() && !emailOwner.get().getId().equals(id)) {
            throw new EmailAlreadyExistsException("Email already taken: " + request.getNewEmail());
        }

        // Set as pending
        user.setPendingEmail(request.getNewEmail());
        user.setEmailVerified(false);
        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpGeneratedTime(System.currentTimeMillis());

        User updatedUser = userRepository.save(user);
        emailService.sendOtpEmail(updatedUser.getPendingEmail(), otp);
        return updatedUser;
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

    @Override
    public LoginResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        // Cannot login if not verified
        if (!user.isEmailVerified()) {
            throw new OtpValidationException("Email not verified. Please verify OTP.");
        }
        // Cannot login with pendingEmail and before OTP verification
        if (user.getPendingEmail() != null) {
            throw new OtpValidationException("Email update in progress. Please verify OTP sent to your new email to login.");
        }
        String token = jwtUtil.generateToken(user.getEmail());
        return new LoginResponse(token, new UserDTO(user.getName(), user.getEmail(), user.getPhone()));
    }

    @Override
    public LoginResponse loginWithoutAuthentication(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!user.isEmailVerified()) {
            throw new OtpValidationException("Email not verified. Please verify OTP.");
        }
        if (user.getPendingEmail() != null) {
            throw new OtpValidationException("Email update in progress. Please verify OTP sent to your new email to login.");
        }
        String token = jwtUtil.generateToken(user.getEmail());
        return new LoginResponse(token, new UserDTO(user.getName(), user.getEmail(), user.getPhone()));
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Only block if email is NOT verified AND no pending change (i.e., registration pending)
        if (!user.isEmailVerified() && user.getPendingEmail() == null) {
            throw new UsernameNotFoundException("Email not verified. Please verify OTP.");
        }

        // ✅ Allow access if:
        // - Email is verified
        // - OR email is verified but pendingEmail is set (email update in progress)
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                true, // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
