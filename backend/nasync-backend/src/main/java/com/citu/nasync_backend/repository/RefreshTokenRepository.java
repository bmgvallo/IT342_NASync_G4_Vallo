package com.citu.nasync_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.citu.nasync_backend.entity.RefreshToken;
import com.citu.nasync_backend.entity.User;
import java.util.Optional;
import jakarta.transaction.Transactional;
 
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
 
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    Optional<RefreshToken> findByUser(User user);
 
    @Transactional
    void deleteByUser(User user);
}
