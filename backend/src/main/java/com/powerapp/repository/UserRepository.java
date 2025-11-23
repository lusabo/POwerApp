package com.powerapp.repository;

import com.powerapp.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {
    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);

    public Optional<User> findByEmail(String email) {
        log.info("Iniciando método findByEmail(email)");
        Optional<User> result = find("email", email).firstResultOptional();
        log.info("Finalizando método findByEmail com retorno presente: {}", result.isPresent());
        return result;
    }
}
