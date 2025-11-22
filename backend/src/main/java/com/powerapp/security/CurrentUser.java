package com.powerapp.security;

import com.powerapp.model.User;
import com.powerapp.repository.UserRepository;
import io.quarkus.security.UnauthorizedException;
import jakarta.enterprise.context.RequestScoped;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class CurrentUser {
    private static final Logger log = LoggerFactory.getLogger(CurrentUser.class);

    private final JsonWebToken jwt;
    private final UserRepository users;

    private User cached;

    public CurrentUser(JsonWebToken jwt, UserRepository users) {
        this.jwt = jwt;
        this.users = users;
    }

    public User get() {
        if (cached != null) {
            return cached;
        }
        if (jwt == null || jwt.getSubject() == null) {
            throw new UnauthorizedException("Missing token");
        }
        Long userId = Long.parseLong(jwt.getSubject());
        cached = users.findByIdOptional(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        log.info("Finalizando método get com usuário {}", cached.getEmail());
        return cached;
    }
}
