package com.powerapp.repository;

import com.powerapp.model.Holiday;
import com.powerapp.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class HolidayRepository implements PanacheRepository<Holiday> {
    public List<Holiday> findByOwner(User owner) {
        return list("owner", owner);
    }
}
