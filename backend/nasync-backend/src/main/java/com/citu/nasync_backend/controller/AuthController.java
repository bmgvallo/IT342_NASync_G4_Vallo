package com.citu.nasync_backend.controller;

import com.citu.nasync_backend.dto.request.LoginRequest;
import com.citu.nasync_backend.dto.response.AuthResponse;
import com.citu.nasync_backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import com.citu.nasync_backend.security.TokenBlacklistService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(
                request.getSchoolId(),
                request.getPassword()
        );
        return ResponseEntity.ok(response);
    }

    // @PostMapping("/register")
    // public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
    //     try {
    //         User savedUser = authService.register(registerRequest);
    //         return ResponseEntity.ok(savedUser);
    //     } catch (RuntimeException e) {
    //         return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    //     }
    // }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                tokenBlacklistService.blacklistToken(token);
                return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid token"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Logout failed"));
        }
    }

    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        boolean exists = authService.usernameExists(username);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean exists = authService.emailExists(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}
