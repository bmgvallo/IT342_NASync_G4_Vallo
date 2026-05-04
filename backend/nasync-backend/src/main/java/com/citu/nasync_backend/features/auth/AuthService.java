package com.citu.nasync_backend.features.auth;

import com.citu.nasync_backend.features.auth.dto.AuthResponse;
import com.citu.nasync_backend.dto.response.UserResponse;
import com.citu.nasync_backend.entity.User;
import com.citu.nasync_backend.features.auth.repository.RefreshTokenRepository;
import com.citu.nasync_backend.repository.UserRepository;
import com.citu.nasync_backend.security.JwtUtil;
import com.citu.nasync_backend.security.OAuth2UserAdapter;
import com.citu.nasync_backend.features.auth.strategy.AuthResponseBuilder;
import com.citu.nasync_backend.features.auth.strategy.AuthenticationStrategy;
import com.citu.nasync_backend.features.auth.strategy.FormLoginCredentials;
import com.citu.nasync_backend.features.auth.strategy.FormLoginStrategy;
import com.citu.nasync_backend.features.auth.strategy.GoogleOAuthStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private OAuth2UserAdapter oAuth2UserAdapter;
    @Autowired private AuthResponseBuilder authResponseBuilder;

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

    public boolean usernameExists(String schoolId) {
        return userRepository.findBySchoolId(schoolId).isPresent();
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent() ||
               userRepository.findByPersonalGmail(email).isPresent();
    }
}