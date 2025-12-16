package com.sistemasweb.back_end_barber.service;

import com.sistemasweb.back_end_barber.dto.UserResponse;
import com.sistemasweb.back_end_barber.model.Admin;
import com.sistemasweb.back_end_barber.model.User;
import com.sistemasweb.back_end_barber.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
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

