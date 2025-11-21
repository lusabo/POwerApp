package com.powerapp.repository;

import com.powerapp.model.Sprint;
import com.powerapp.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class SprintRepository implements PanacheRepository<Sprint> {
    public List<Sprint> findByOwner(User owner) {
        return list("owner", owner);
    }

    public Optional<Sprint> findByIdAndOwner(Long id, User owner) {
        return find("id = ?1 and owner = ?2", id, owner).firstResultOptional();
    }
}
