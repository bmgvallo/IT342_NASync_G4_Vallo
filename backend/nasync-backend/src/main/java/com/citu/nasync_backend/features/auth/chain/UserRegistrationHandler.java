package com.citu.nasync_backend.features.auth.chain;

import com.citu.nasync_backend.features.auth.dto.RegisterUserRequest;

public abstract class UserRegistrationHandler {

    protected UserRegistrationHandler next;

    public UserRegistrationHandler setNext(UserRegistrationHandler next) {
        this.next = next;
        return next;
    }

    
    public abstract void handle(RegisterUserRequest request, RegistrationContext context);
}