package com.citu.nasync_backend.chain;

import com.citu.nasync_backend.dto.request.RegisterUserRequest;
import com.citu.nasync_backend.repository.UserRepository;

public class SchoolIdDuplicateHandler extends UserRegistrationHandler {

    private final UserRepository userRepository;

    public SchoolIdDuplicateHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void handle(RegisterUserRequest request, RegistrationContext context) {
        if (userRepository.existsBySchoolId(request.getSchoolId())) {
            throw new RuntimeException("School ID already exists");
        }
        if (next != null) next.handle(request, context);
    }
}