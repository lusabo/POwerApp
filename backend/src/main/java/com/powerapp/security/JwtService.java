package com.powerapp.security;

import com.powerapp.model.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    public String generate(User user) {
        log.info("Iniciando método generate(userId={})", user != null ? user.getId() : null);
        Instant now = Instant.now();
        String token = Jwt.issuer("powerapp")
                .subject(String.valueOf(user.getId()))
                .upn(user.getEmail())
                .groups(Set.of("user"))
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofHours(12)))
                .sign();
        log.info("Finalizando método generate com retorno: token gerado");
        return token;
    }
}
