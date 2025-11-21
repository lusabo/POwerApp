package com.powerapp.security;

import com.powerapp.model.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@ApplicationScoped
public class JwtService {
    public String generate(User user) {
        Instant now = Instant.now();
        return Jwt.issuer("powerapp")
                .subject(String.valueOf(user.getId()))
                .upn(user.getEmail())
                .groups(Set.of("user"))
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofHours(12)))
                .sign();
    }
}
