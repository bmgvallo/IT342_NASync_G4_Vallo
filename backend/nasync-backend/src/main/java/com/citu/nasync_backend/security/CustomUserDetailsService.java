package com.citu.nasync_backend.security;

import com.citu.nasync_backend.entity.User;
import com.citu.nasync_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String schoolId) throws UsernameNotFoundException {
        User user = userRepository.findBySchoolId(schoolId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with school ID: " + schoolId));

        return new org.springframework.security.core.userdetails.User(
                user.getSchoolId(),
                user.getPasswordHash() != null ? user.getPasswordHash() : "",
                user.isActive(),
                true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}