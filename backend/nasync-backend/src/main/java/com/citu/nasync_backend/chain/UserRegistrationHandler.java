package com.citu.nasync_backend.chain;

import com.citu.nasync_backend.dto.request.RegisterUserRequest;

public abstract class UserRegistrationHandler {

    protected UserRegistrationHandler next;

    public UserRegistrationHandler setNext(UserRegistrationHandler next) {
        this.next = next;
        return next;
    }

    
    public abstract void handle(RegisterUserRequest request, RegistrationContext context);
}