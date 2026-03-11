package com.citu.nasync_backend.service;

import com.citu.nasync_backend.dto.response.AuthResponse;
import com.citu.nasync_backend.dto.response.UserResponse;
import com.citu.nasync_backend.entity.RefreshToken;
import com.citu.nasync_backend.entity.User;
import com.citu.nasync_backend.repository.RefreshTokenRepository;
import com.citu.nasync_backend.repository.UserRepository;
import com.citu.nasync_backend.security.JwtUtil;
import org.springframework.security.oauth2.core.user.OAuth2User;
import com.citu.nasync_backend.enums.Role;

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

    public AuthResponse authenticateWithGoogleOAuth2(OAuth2User oAuth2User) {
    String email = oAuth2User.getAttribute("email");
    String googleId = oAuth2User.getAttribute("sub");
    String firstName = oAuth2User.getAttribute("given_name");
    String lastName = oAuth2User.getAttribute("family_name");
    
    if (email == null) {
        throw new IllegalArgumentException("Email not provided by Google");
    }
    
    // Check if user exists by personalGmail or email
    User user = userRepository.findByPersonalGmail(email)
            .orElseGet(() -> userRepository.findByEmail(email).orElse(null));
    
    if (user == null) {
        // create new user account automatically
        user = new User();
        user.setEmail(email);
        user.setPersonalGmail(email);
        user.setFirstName(firstName != null ? firstName : "Google");
        user.setLastName(lastName != null ? lastName : "User");
        user.setSchoolId("google_" + googleId.substring(0, 8)); //generate google id as school id but can be changed later by scholar
        user.setPasswordHash(null);
        user.setRole(Role.SCHOLAR);
        user.setActive(true);
        user.setFirstTimeLogin(true);
        user.setOauthProvider("google");
        user.setOauthSubject(googleId);
        
        userRepository.save(user);
    } else {
        // Existing user
        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Admin accounts cannot use Google login");
        }
        
        if (!user.isActive()) {
            throw new IllegalArgumentException("Account is deactivated");
        }
        
        // Update OAuth info if not already set
        if (user.getOauthSubject() == null) {
            user.setOauthProvider("google");
            user.setOauthSubject(googleId);
            userRepository.save(user);
        }
    }
    
    return buildAuthResponse(user);
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