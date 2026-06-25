package com.citu.nasync_backend.features.auth;

import com.citu.nasync_backend.features.auth.dto.AuthResponse;
import com.citu.nasync_backend.shared.dto.response.UserResponse;
import com.citu.nasync_backend.shared.entity.User;
import com.citu.nasync_backend.features.auth.repository.RefreshTokenRepository;
import com.citu.nasync_backend.shared.repository.UserRepository;
import com.citu.nasync_backend.shared.security.JwtUtil;
import com.citu.nasync_backend.shared.security.OAuth2UserAdapter;
import com.citu.nasync_backend.features.auth.strategy.AuthResponseBuilder;
import com.citu.nasync_backend.features.auth.strategy.AuthenticationStrategy;
import com.citu.nasync_backend.features.auth.strategy.FormLoginCredentials;
import com.citu.nasync_backend.features.auth.strategy.FormLoginStrategy;
import com.citu.nasync_backend.features.auth.strategy.GoogleOAuthStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private OAuth2UserAdapter oAuth2UserAdapter;
    @Autowired private AuthResponseBuilder authResponseBuilder;
    @Autowired private PasswordEncoder passwordEncoder;

    public AuthResponse login(String schoolId, String password) {
        AuthenticationStrategy strategy = new FormLoginStrategy(
                authenticationManager, userRepository, authResponseBuilder
        );
        return strategy.authenticate(new FormLoginCredentials(schoolId, password));
    }

    public AuthResponse authenticateWithGoogleOAuth2(OAuth2User oAuth2User) {
        AuthenticationStrategy strategy = new GoogleOAuthStrategy(
                userRepository, oAuth2UserAdapter, authResponseBuilder
        );
        return strategy.authenticate(oAuth2User);
    }

    public UserResponse getCurrentUserFromToken(String token) {
        String schoolId = jwtUtil.extractSchoolId(token);
        return getCurrentUser(schoolId);
    }

    public UserResponse getCurrentUser(String schoolId) {
        User user = userRepository.findBySchoolId(schoolId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return UserResponse.from(user);
    }

    public void logout(String schoolId) {
        userRepository.findBySchoolId(schoolId)
                .ifPresent(user -> refreshTokenRepository.deleteByUser(user));
    }

    public void changePassword(String schoolId, String currentPassword, String newPassword) {
        User user = userRepository.findBySchoolId(schoolId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect.");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        if (Boolean.TRUE.equals(user.getFirstTimeLogin())) {
            user.setFirstTimeLogin(false);
        }
        userRepository.save(user);
    }

    public UserResponse updatePersonalGmail(String schoolId, String newGmail) {
        User user = userRepository.findBySchoolId(schoolId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (newGmail != null && !newGmail.isBlank()) {
            boolean takenByOther = userRepository.findByPersonalGmail(newGmail)
                    .map(existing -> !existing.getSchoolId().equals(schoolId))
                    .orElse(false);
            if (takenByOther) {
                throw new RuntimeException("This Gmail is already in use by another account.");
            }
            user.setPersonalGmail(newGmail);
        } else {
            user.setPersonalGmail(null);
        }
        userRepository.save(user);
        return UserResponse.from(user);
    }

    public boolean usernameExists(String schoolId) {
        return userRepository.findBySchoolId(schoolId).isPresent();
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent() ||
               userRepository.findByPersonalGmail(email).isPresent();
    }
}