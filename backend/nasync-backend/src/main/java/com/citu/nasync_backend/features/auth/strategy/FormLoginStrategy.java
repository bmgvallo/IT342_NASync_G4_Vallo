package com.citu.nasync_backend.features.auth.strategy;

import com.citu.nasync_backend.features.auth.dto.AuthResponse;
import com.citu.nasync_backend.entity.User;
import com.citu.nasync_backend.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;

public class FormLoginStrategy implements AuthenticationStrategy {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final AuthResponseBuilder authResponseBuilder;

    public FormLoginStrategy(AuthenticationManager authenticationManager, UserRepository userRepository, AuthResponseBuilder authResponseBuilder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.authResponseBuilder = authResponseBuilder;
    }

    @Override
    public AuthResponse authenticate(Object credentials) {
        FormLoginCredentials creds = (FormLoginCredentials) credentials;

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            creds.getSchoolId(), creds.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid school ID or password");
        } catch (AuthenticationException e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }

        User user = userRepository.findBySchoolId(creds.getSchoolId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isActive()) {
            throw new RuntimeException("Account is deactivated. Contact OAS Admin.");
        }

        return authResponseBuilder.build(user);
    }
}