package com.sistemasweb.back_end_barber.service;

import com.sistemasweb.back_end_barber.dto.*;
import com.sistemasweb.back_end_barber.model.Admin;
import com.sistemasweb.back_end_barber.model.Scheduler;
import com.sistemasweb.back_end_barber.model.User;
import com.sistemasweb.back_end_barber.repository.UserRepository;
import com.sistemasweb.back_end_barber.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Get user from database
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail());

        // Build response
        return AuthResponse.builder()
                .success(true)
                .message("Login successful")
                .user(mapToUserResponse(user))
                .token(token)
                .build();
    }

    @Transactional
    public AuthResponse registerAdmin(RegisterAdminRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create admin user
        Admin admin = new Admin();
        admin.setEmail(request.getEmail());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setName(request.getName());
        admin.setPhone(request.getPhone());
        admin.setBarbershopName(request.getBarbershopName());
        admin.setAddress(request.getAddress());
        admin.setActive(true);

        // Save admin
        Admin savedAdmin = userRepository.save(admin);

        // Build response
        return AuthResponse.builder()
                .success(true)
                .message("Admin account created successfully")
                .user(mapToUserResponse(savedAdmin))
                .build();
    }

    @Transactional
    public AuthResponse registerScheduler(RegisterSchedulerRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create scheduler user
        Scheduler scheduler = new Scheduler();
        scheduler.setEmail(request.getEmail());
        scheduler.setPassword(passwordEncoder.encode(request.getPassword()));
        scheduler.setName(request.getName());
        scheduler.setPhone(request.getPhone());
        scheduler.setActive(true);

        // Save scheduler
        Scheduler savedScheduler = userRepository.save(scheduler);

        // Build response
        return AuthResponse.builder()
                .success(true)
                .message("Account created successfully")
                .user(mapToUserResponse(savedScheduler))
                .build();
    }

    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .type(user.getType())
                .createdAt(user.getCreatedAt());

        // Add admin-specific fields
        if (user instanceof Admin admin) {
            builder.barbershopName(admin.getBarbershopName())
                   .address(admin.getAddress());
        }

        return builder.build();
    }
}

