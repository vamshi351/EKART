// JwtUtil.java
package com.example.userservice.config;

import java.security.Key;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

//JwtUtil.java
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@Component
public class JwtUtil {

 private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

 @Value("${jwt.secret}")
 private String secret;

 @Value("${jwt.expiration.ms}")
 private long expirationMs;

 // Generate signing key from the secret
 private Key getSigningKey() {
     return Keys.hmacShaKeyFor(secret.getBytes());
 }

 // Generate JWT token
 public String generateToken(String email) {
     if (email == null || email.isBlank()) {
         throw new IllegalArgumentException("Email cannot be null or empty");
     }

     return Jwts.builder()
             .subject(email)
             .issuedAt(new Date())
             .expiration(new Date(System.currentTimeMillis() + expirationMs))
             .signWith(getSigningKey(), SignatureAlgorithm.HS256)
             .compact();
 }

 // Extract email from token
 public String extractEmail(String token) {
     if (token == null || token.isBlank()) {
         throw new MalformedJwtException("Token is null or empty");
     }

     try {
         Jws<Claims> claimsJws = Jwts.parser()
                 .verifyWith(getSigningKey())
                 .build()
                 .parseSignedClaims(token);

         return claimsJws.getPayload().getSubject();
     } catch (ExpiredJwtException e) {
         logger.warn("JWT token is expired: {}", e.getMessage());
         throw e;
     } catch (UnsupportedJwtException e) {
         logger.error("JWT token is unsupported: {}", e.getMessage());
         throw e;
     } catch (MalformedJwtException e) {
         logger.error("JWT token is invalid: {}", e.getMessage());
         throw e;
     } catch (SignatureException e) {
         logger.error("JWT signature is invalid: {}", e.getMessage());
         throw e;
     } catch (IllegalArgumentException e) {
         logger.error("JWT claims string is empty: {}", e.getMessage());
         throw e;
     }
 }

 // Validate token
 public boolean validateToken(String token) {
     try {
         Jwts.parser()
                 .verifyWith(getSigningKey())
                 .build()
                 .parseSignedClaims(token);
         return true;
     } catch (Exception e) {
         logger.warn("Invalid JWT token: {}", e.getMessage());
         return false;
     }
 }
}