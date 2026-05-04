package com.citu.nasync_backend.features.auth.strategy;

import com.citu.nasync_backend.features.auth.dto.AuthResponse;

public interface AuthenticationStrategy {
    AuthResponse authenticate(Object credentials);
}