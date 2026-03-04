package com.citu.nasync_backend.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.citu.nasync_backend.dto.request.LoginRequest;
import com.citu.nasync_backend.dto.request.RegisterUserRequest;
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

    // School ID Password Login
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

    // google oauth login
    public AuthResponse googleLogin(String googleIdToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(googleIdToken);
            if (idToken == null) {
                throw new RuntimeException("Invalid Google token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String subject = payload.getSubject();

            // find user by personal_gmail
            User user = userRepository.findByPersonalGmail(email)
                    .orElseThrow(() -> new RuntimeException(
                            "No NASify account linked to this Google account. " +
                                    "Contact OAS Admin to register your personal Gmail."));

            if (!user.isActive()) {
                throw new RuntimeException("Account is deactivated.");
            }

            // store oauth_subject for future faster matching
            if (user.getOauthSubject() == null) {
                user.setOauthProvider("google");
                user.setOauthSubject(subject);
                userRepository.save(user);
            }

            return buildAuthResponse(user);

        } catch (Exception e) {
            throw new RuntimeException("Google login failed: " + e.getMessage());
        }
    }

    // register User
    /*
    public User register(RegisterUserRequest request) {
        // Check if school ID already exists
        if (userRepository.findBySchoolId(request.getSchoolId()).isPresent()) {
            throw new RuntimeException("School ID already exists");
        }

        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setSchoolId(request.getSchoolId());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPersonalGmail(request.getPersonalGmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setActive(true);
        user.setMustChangePassword(true); // Force password change on first login

        // Set department if provided
        if (request.getDeptId() != null) {
            // You'll need to inject DepartmentRepository and fetch the department
            // departmentRepository.findById(request.getDeptId()).ifPresent(user::setDepartment);
        }

        // Set branch if provided
        if (request.getBranchId() != null) {
            // branchRepository.findById(request.getBranchId()).ifPresent(user::setBranch);
        }

        // Set scholar-specific fields
        if (request.getRole() == Role.SCHOLAR) {
            user.setShift(request.getShift());
            user.setExpectedTimeIn(request.getExpectedTimeIn());
            user.setExpectedTimeOut(request.getExpectedTimeOut());
        }

        return userRepository.save(user);
    }
    */

    // get Current User (/auth/me)
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

        // generate refresh token and store hashed version
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