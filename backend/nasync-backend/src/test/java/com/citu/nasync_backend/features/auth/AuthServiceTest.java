package com.citu.nasync_backend.features.auth;

import com.citu.nasync_backend.shared.entity.User;
import com.citu.nasync_backend.shared.enums.Role;
import com.citu.nasync_backend.features.auth.strategy.AuthResponseBuilder;
import com.citu.nasync_backend.features.auth.strategy.FormLoginCredentials;
import com.citu.nasync_backend.features.auth.strategy.FormLoginStrategy;
import com.citu.nasync_backend.shared.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private AuthResponseBuilder authResponseBuilder;

    private FormLoginStrategy formLoginStrategy;

    @BeforeEach
    void setUp() {
        formLoginStrategy = new FormLoginStrategy(authenticationManager, userRepository, authResponseBuilder);
    }

    private User activeUser() {
        return new User.Builder()
                .schoolId("2021-00001")
                .firstName("Test")
                .lastName("User")
                .email("test@cit.edu")
                .passwordHash("$hashed$")
                .role(Role.SCHOLAR)
                .active(true)
                .firstTimeLogin(false)
                .build();
        }

    // AUTH-01: valid credentials succeed
    @Test
    void login_validCredentials_returnsAuthResponse() {
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(userRepository.findBySchoolId("2021-00001")).thenReturn(Optional.of(activeUser()));
        when(authResponseBuilder.build(any())).thenReturn(null);

        assertDoesNotThrow(() ->
                formLoginStrategy.authenticate(new FormLoginCredentials("2021-00001", "password123")));

        verify(authResponseBuilder).build(any());
    }

    // AUTH-02: wrong password throws with correct message
    @Test
    void login_wrongPassword_throwsInvalidCredentials() {
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                formLoginStrategy.authenticate(new FormLoginCredentials("2021-00001", "wrongpass")));
        assertEquals("Invalid school ID or password", ex.getMessage());
    }

    // AUTH-03: non-existent user — auth passes but repo returns empty
    @Test
    void login_userNotFound_throwsException() {
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(userRepository.findBySchoolId("9999-99999")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                formLoginStrategy.authenticate(new FormLoginCredentials("9999-99999", "any")));
    }

    // AUTH-04: inactive user throws after auth succeeds
    @Test
    void login_inactiveUser_throwsDeactivatedMessage() {
        User inactive = new User.Builder()
                .schoolId("2021-00001")
                .firstName("Test")
                .lastName("User")
                .email("test@cit.edu")
                .passwordHash("$hashed$")
                .role(Role.SCHOLAR)
                .active(false)
                .firstTimeLogin(false)
                .build();

        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(userRepository.findBySchoolId("2021-00001")).thenReturn(Optional.of(inactive));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                formLoginStrategy.authenticate(new FormLoginCredentials("2021-00001", "password123")));
        assertTrue(ex.getMessage().toLowerCase().contains("deactivated") ||
                   ex.getMessage().toLowerCase().contains("inactive"));
    }
}
