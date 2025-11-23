package com.powerapp.repository;

import com.powerapp.entity.Holiday;
import com.powerapp.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class HolidayRepository implements PanacheRepository<Holiday> {
    private static final Logger log = LoggerFactory.getLogger(HolidayRepository.class);

    public List<Holiday> findByOwner(User owner) {
        log.info("Iniciando método findByOwner(ownerId={})", owner != null ? owner.getId() : null);
        List<Holiday> result = list("owner", owner);
        log.info("Finalizando método findByOwner com retorno: {} registros", result.size());
        return result;
    }
}
