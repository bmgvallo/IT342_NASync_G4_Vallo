package com.citu.nasync_backend.security;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class OAuth2UserAdapter {

    public String extractEmail(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute("email");
    }

    public String extractGoogleId(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute("sub");
    }

    public String extractFirstName(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute("given_name");
    }

    public String extractLastName(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute("family_name");
    }
}