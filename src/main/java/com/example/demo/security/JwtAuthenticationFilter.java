package com.example.demo.security;

//JwtAuthenticationFilter.java

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.service.UserService;
import com.example.userservice.config.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

 @Autowired
 private JwtUtil jwtUtil;

 @Autowired
 private UserService userService;

 @Override
 protected void doFilterInternal(HttpServletRequest request,
                                 HttpServletResponse response,
                                 FilterChain chain) throws ServletException, IOException {

     String token = extractToken(request);
     if (token != null && jwtUtil.validateToken(token)) {
         String email = jwtUtil.extractEmail(token);
         UserDetails userDetails = userService.loadUserByUsername(email);

         UsernamePasswordAuthenticationToken authToken =
                 new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

         authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
         SecurityContextHolder.getContext().setAuthentication(authToken);
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
}