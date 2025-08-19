package com.example.demo.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter implements WebFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // ✅ Allow swagger, docs, auth, actuator without JWT
        if (path.startsWith("/swagger-ui")
                || path.equals("/swagger-ui.html")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/user-service/v3/api-docs")
                || path.startsWith("/product-service/v3/api-docs")
                || path.startsWith("/webjars")
                || path.startsWith("/api/auth")
                || path.startsWith("/actuator")) {
            return chain.filter(exchange);
        }


        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return this.onError(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            return this.onError(exchange, "Invalid or expired JWT token");
        }

        return chain.filter(exchange);
    }



    private Mono<Void> onError(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
