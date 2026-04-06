package com.citu.nasync_backend.strategy;

import com.citu.nasync_backend.dto.response.AuthResponse;

public interface AuthenticationStrategy {
    AuthResponse authenticate(Object credentials);
}