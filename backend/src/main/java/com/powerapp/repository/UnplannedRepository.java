package com.powerapp.repository;

import com.powerapp.model.Sprint;
import com.powerapp.model.UnplannedItem;
import com.powerapp.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class UnplannedRepository implements PanacheRepository<UnplannedItem> {
    public List<UnplannedItem> findBySprint(Sprint sprint) {
        return list("sprint", sprint);
    }

    public List<UnplannedItem> findByOwner(User owner) {
        return list("owner", owner);
    }
}
