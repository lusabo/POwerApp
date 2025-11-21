package com.powerapp.security;

import com.powerapp.model.User;
import com.powerapp.repository.UserRepository;
import io.quarkus.security.UnauthorizedException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

@RequestScoped
public class CurrentUser {
    @Inject
    JsonWebToken jwt;

    @Inject
    UserRepository users;

    private User cached;

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
        return cached;
    }
}
