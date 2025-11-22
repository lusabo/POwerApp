package com.powerapp.repository;

import com.powerapp.model.Sprint;
import com.powerapp.model.UnplannedItem;
import com.powerapp.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class UnplannedRepository implements PanacheRepository<UnplannedItem> {
    private static final Logger log = LoggerFactory.getLogger(UnplannedRepository.class);

    public List<UnplannedItem> findBySprint(Sprint sprint) {
        log.info("Iniciando método findBySprint(sprintId={})", sprint != null ? sprint.getId() : null);
        List<UnplannedItem> result = list("sprint", sprint);
        log.info("Finalizando método findBySprint com retorno: {} registros", result.size());
        return result;
    }

    public List<UnplannedItem> findByOwner(User owner) {
        log.info("Iniciando método findByOwner(ownerId={})", owner != null ? owner.getId() : null);
        List<UnplannedItem> result = list("owner", owner);
        log.info("Finalizando método findByOwner com retorno: {} registros", result.size());
        return result;
    }
}
