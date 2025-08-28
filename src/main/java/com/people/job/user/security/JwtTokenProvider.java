package com.people.job.user.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;              // Base64 권장(>=32바이트)

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private Key key;

    @PostConstruct
    public void init() {
        byte[] keyBytes;
        try { keyBytes = Decoders.BASE64.decode(secret); }
        catch (IllegalArgumentException e) { keyBytes = secret.getBytes(StandardCharsets.UTF_8); }
        key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(String userid, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(userid)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)              // 0.12.x
                .compact();
    }

    public String getUserid(String token) { return parseClaims(token).getSubject(); }
    public String getRole(String token) {
        Object role = parseClaims(token).get("role");
        return role != null ? role.toString() : null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith((SecretKey) key).build()
                    .parseSignedClaims(stripBearer(token));     // 0.12.x
            return true;
        } catch (JwtException | IllegalArgumentException e) { return false; }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith((SecretKey) key).build()
                .parseSignedClaims(stripBearer(token))      // 0.12.x
                .getPayload();
    }

    private String stripBearer(String token) {
        return token != null && token.startsWith("Bearer ") ? token.substring(7) : token;
    }
}
