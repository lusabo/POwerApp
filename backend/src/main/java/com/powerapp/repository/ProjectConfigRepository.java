package com.powerapp.repository;

import com.powerapp.model.ProjectConfig;
import com.powerapp.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class ProjectConfigRepository implements PanacheRepository<ProjectConfig> {
    public Optional<ProjectConfig> findByOwner(User owner) {
        return find("owner", owner).firstResultOptional();
    }
}
