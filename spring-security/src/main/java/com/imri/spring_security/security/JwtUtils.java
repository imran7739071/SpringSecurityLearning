package com.imri.spring_security.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component   // Spring manages this as a bean — injectable anywhere
@Slf4j       // Lombok gives us log.info(), log.error() etc.
public class JwtUtils {

    @Value("${app.jwt.secret}")
    private String jwtSecret;        // read from application.yml

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;      // milliseconds — 86400000 = 24 hours

    // ── Generate token from UserDetails ──────────────────────────────
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Add roles as a custom claim inside the token payload
        claims.put("roles", userDetails.getAuthorities()
                .stream()
                .map(a -> a.getAuthority())
                .toList());

        return Jwts.builder()
                .setClaims(claims)                          // custom data (roles)
                .setSubject(userDetails.getUsername())      // who the token belongs to
                .setIssuedAt(new Date())                    // when it was created
                .setExpiration(new Date(                    // when it expires
                        System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // sign it
                .compact();                                 // build the string
    }

    // ── Extract username from token ───────────────────────────────────
    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    // ── Validate token against UserDetails ────────────────────────────
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            String username = getUsernameFromToken(token);
            return username.equals(userDetails.getUsername())
                    && !isTokenExpired(token);
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("JWT token is malformed: {}", e.getMessage());
        } catch (SignatureException e) {
            log.error("JWT signature is invalid: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    // ── Private helpers ───────────────────────────────────────────────
    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    // Parse and return the payload (claims) of the token
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Convert the secret string into a cryptographic Key object
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}