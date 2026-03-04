package com.citu.nasync_backend.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.citu.nasync_backend.dto.response.AuthResponse;
import com.citu.nasync_backend.dto.response.UserResponse;
import com.citu.nasync_backend.entity.RefreshToken;
import com.citu.nasync_backend.entity.User;
import com.citu.nasync_backend.repository.RefreshTokenRepository;
import com.citu.nasync_backend.repository.UserRepository;
import com.citu.nasync_backend.security.JwtUtil;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${google.client-id:placeholder}")
    private String googleClientId;

    public AuthResponse login(String schoolId, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(schoolId, password)
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid school ID or password");
        } catch (AuthenticationException e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }

        User user = userRepository.findBySchoolId(schoolId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isActive()) {
            throw new RuntimeException("Account is deactivated. Contact OAS Admin.");
        }

        return buildAuthResponse(user);
    }

    public UserResponse getCurrentUser(String schoolId) {
        User user = userRepository.findBySchoolId(schoolId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return UserResponse.from(user);
    }

    // logout
    public void logout(String schoolId) {
        userRepository.findBySchoolId(schoolId)
                .ifPresent(user -> refreshTokenRepository.deleteByUser(user));
    }

    // check if username (schoolId) exists
    public boolean usernameExists(String schoolId) {
        return userRepository.findBySchoolId(schoolId).isPresent();
    }

    // check if email exists
    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent() ||
                userRepository.findByPersonalGmail(email).isPresent();
    }


    @Transactional
    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtUtil.generateToken(
                user.getSchoolId(), user.getRole().name());

        String rawRefreshToken = UUID.randomUUID().toString();
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();
        RefreshToken refreshToken = new RefreshToken(
                user,
                rawRefreshToken,
                LocalDateTime.now().plusDays(7)
        );
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(accessToken, rawRefreshToken, UserResponse.from(user));
    }
}