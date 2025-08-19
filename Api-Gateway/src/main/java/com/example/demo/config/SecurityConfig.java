package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
	    return http
	            .csrf(ServerHttpSecurity.CsrfSpec::disable)
	            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)  // ✅ Disable Basic Auth popup
	            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)  // ✅ Disable login form
	            .authorizeExchange(exchanges -> exchanges
	                    .pathMatchers(
	                            "/swagger-ui.html",
	                            "/swagger-ui/**",
	                            "/v3/api-docs/**",
	                            "/user-service/v3/api-docs",
	                            "/product-service/v3/api-docs",
	                            "/webjars/**",
	                            "/api/auth/**",
	                            "/actuator/**"
	                    ).permitAll()
	                    .anyExchange().authenticated()
	            )
	            .build();
	}

}

