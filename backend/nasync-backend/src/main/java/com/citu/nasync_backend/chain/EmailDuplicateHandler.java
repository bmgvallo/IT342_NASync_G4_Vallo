package com.citu.nasync_backend.chain;

import com.citu.nasync_backend.dto.request.RegisterUserRequest;
import com.citu.nasync_backend.repository.UserRepository;

public class EmailDuplicateHandler extends UserRegistrationHandler {

    private final UserRepository userRepository;

    public EmailDuplicateHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void handle(RegisterUserRequest request, RegistrationContext context) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (next != null) next.handle(request, context);
    }
}