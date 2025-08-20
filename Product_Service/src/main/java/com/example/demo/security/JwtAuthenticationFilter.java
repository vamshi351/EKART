// ===== 1. JwtAuthenticationFilter.java =====
package com.example.demo.security;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.config.UserClient;
import com.example.demo.dto.UserDTO;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserClient userClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        
        // Skip authentication for public endpoints
        String path = request.getRequestURI();
        if (path.contains("/v3/api-docs") || path.contains("/swagger-ui") || 
            path.contains("/actuator") || path.contains("/health")) {
            chain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);

        if (token != null) {
            try {
                String email = jwtUtil.extractEmail(token);
                
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    if (jwtUtil.isTokenValid(token)) {
                        // Fetch user info from user-service
                        UserDTO userDTO = userClient.getMe("Bearer " + token);
                        
                        if (userDTO != null) {
                            // Create authorities based on user type
                            List<SimpleGrantedAuthority> authorities = createAuthoritiesFromUser(userDTO, email);
                            
                            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                email, null, authorities);
                            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                            
                            System.out.println("Authentication successful for user: " + email + " with authorities: " + authorities);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("JWT Authentication failed: " + e.getMessage());
                // Clear any existing authentication
                SecurityContextHolder.clearContext();
            }
        }
        
        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
    
    private List<SimpleGrantedAuthority> createAuthoritiesFromUser(UserDTO userDTO, String email) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        // Default role assignment - you might want to enhance UserDTO to include actual roles
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        
        // For admin users (you can modify this logic based on your requirements)
        if (email.contains("admin")) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        
        return authorities;
    }
}