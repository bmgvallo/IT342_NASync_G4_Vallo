package com.citu.nasync_backend.strategy;

import com.citu.nasync_backend.dto.response.AuthResponse;
import com.citu.nasync_backend.entity.User;
import com.citu.nasync_backend.enums.Role;
import com.citu.nasync_backend.repository.UserRepository;
import com.citu.nasync_backend.security.OAuth2UserAdapter;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class GoogleOAuthStrategy implements AuthenticationStrategy {

    private final UserRepository userRepository;
    private final OAuth2UserAdapter oAuth2UserAdapter;
    private final AuthResponseBuilder authResponseBuilder;

    public GoogleOAuthStrategy(UserRepository userRepository, OAuth2UserAdapter oAuth2UserAdapter, AuthResponseBuilder authResponseBuilder) {
        this.userRepository = userRepository;
        this.oAuth2UserAdapter = oAuth2UserAdapter;
        this.authResponseBuilder = authResponseBuilder;
    }

    @Override
    public AuthResponse authenticate(Object credentials) {
        OAuth2User oAuth2User = (OAuth2User) credentials;

        String email = oAuth2UserAdapter.extractEmail(oAuth2User);
        String googleId = oAuth2UserAdapter.extractGoogleId(oAuth2User);
        String firstName = oAuth2UserAdapter.extractFirstName(oAuth2User);
        String lastName = oAuth2UserAdapter.extractLastName(oAuth2User);

        if (email == null) {
            throw new IllegalArgumentException("Email not provided by Google");
        }

        User user = userRepository.findByPersonalGmail(email)
                .orElseGet(() -> userRepository.findByEmail(email).orElse(null));

        if (user == null) {
            user = new User.Builder()
                    .schoolId("google_" + googleId.substring(0, 8))
                    .firstName(firstName != null ? firstName : "Google")
                    .lastName(lastName != null ? lastName : "User")
                    .email(email)
                    .personalGmail(email)
                    .passwordHash(null)
                    .role(Role.SCHOLAR)
                    .active(true)
                    .firstTimeLogin(true)
                    .oauthProvider("google")
                    .oauthSubject(googleId)
                    .build();
            userRepository.save(user);
        } else {
            if (user.getRole() == Role.ADMIN) {
                throw new IllegalArgumentException("Admin accounts cannot use Google login");
            }
            if (!user.isActive()) {
                throw new IllegalArgumentException("Account is deactivated");
            }
            if (user.getOauthSubject() == null) {
                user.setOauthProvider("google");
                user.setOauthSubject(googleId);
                userRepository.save(user);
            }
        }

        return authResponseBuilder.build(user);
    }
}