package com.citu.nasync_backend.features.auth.chain;

import com.citu.nasync_backend.features.auth.dto.RegisterUserRequest;
import com.citu.nasync_backend.shared.repository.UserRepository;

public class EmailDuplicateHandler extends UserRegistrationHandler {

    private final UserRepository userRepository;

    public EmailDuplicateHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void handle(RegisterUserRequest request, RegistrationContext context) {
        if (request.getEmail() != null &&
            userRepository.existsByEmail(request.getEmail())) {

            throw new RuntimeException("School email already exists.");
        }

        if (request.getPersonalGmail() != null &&
            userRepository.existsByPersonalGmail(request.getPersonalGmail())) {

            throw new RuntimeException("Personal Gmail already exists.");
        }

        if (next != null) {
            next.handle(request, context);
        }
    }
}