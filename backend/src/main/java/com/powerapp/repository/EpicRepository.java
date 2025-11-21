package com.powerapp.repository;

import com.powerapp.model.Epic;
import com.powerapp.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class EpicRepository implements PanacheRepository<Epic> {
    public List<Epic> findByOwner(User owner) {
        return list("owner", owner);
    }

    public Optional<Epic> findByKeyAndOwner(String key, User owner) {
        return find("epicKey = ?1 and owner = ?2", key, owner).firstResultOptional();
    }
}
