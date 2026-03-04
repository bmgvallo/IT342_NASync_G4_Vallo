package com.citu.nasync_backend.security;

        import io.jsonwebtoken.*;
        import io.jsonwebtoken.security.Keys;
        import org.springframework.beans.factory.annotation.Value;
        import org.springframework.stereotype.Component;
        import java.security.Key;
        import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationMs; // e.g. 3600000 = 1 hour

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // generate a new access token for a user
    public String generateToken(String schoolId, String role) {
        return Jwts.builder()
                .setSubject(schoolId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // extract school_id from token
    public String extractSchoolId(String token) {
        return getClaims(token).getSubject();
    }

    // extract role from token
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // validate token — returns false if expired or tampered
    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}