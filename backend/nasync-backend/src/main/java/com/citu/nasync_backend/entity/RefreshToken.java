package com.citu.nasync_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")

public class RefreshToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long tokenId;
 
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
 
    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;
 
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
 
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
 
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
 
    // constructors
    public RefreshToken() {}
 
    public RefreshToken(User user, String tokenHash, LocalDateTime expiresAt) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }
 
    // getters
    public Long getTokenId() { return tokenId; }
    public User getUser() { return user; }
    public String getTokenHash() { return tokenHash; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
 
    // setters
    public void setTokenId(Long tokenId) { this.tokenId = tokenId; }
    public void setUser(User user) { this.user = user; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }


}
