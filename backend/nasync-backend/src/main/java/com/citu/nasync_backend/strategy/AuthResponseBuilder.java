package com.citu.nasync_backend.strategy;

import com.citu.nasync_backend.dto.response.AuthResponse;
import com.citu.nasync_backend.dto.response.UserResponse;
import com.citu.nasync_backend.entity.RefreshToken;
import com.citu.nasync_backend.entity.User;
import com.citu.nasync_backend.repository.RefreshTokenRepository;
import com.citu.nasync_backend.security.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class AuthResponseBuilder {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public AuthResponse build(User user) {
        String accessToken    = jwtUtil.generateToken(user.getSchoolId(), user.getRole().name());
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