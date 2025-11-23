package com.powerapp.repository;

import com.powerapp.entity.Sprint;
import com.powerapp.entity.SprintAbsence;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class SprintAbsenceRepository implements PanacheRepository<SprintAbsence> {
    public void deleteBySprint(Sprint sprint) {
        delete("sprint", sprint);
    }

    public List<SprintAbsence> findBySprint(Sprint sprint) {
        return list("sprint", sprint);
    }
}
