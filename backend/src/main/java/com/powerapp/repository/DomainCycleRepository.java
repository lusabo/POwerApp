package com.powerapp.repository;

import com.powerapp.model.DomainCycle;
import com.powerapp.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class DomainCycleRepository implements PanacheRepository<DomainCycle> {
    public List<DomainCycle> findByOwner(User owner) {
        return list("owner", owner);
    }
}
