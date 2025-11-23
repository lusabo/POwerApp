package com.powerapp.repository;

import com.powerapp.entity.DomainCycle;
import com.powerapp.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class DomainCycleRepository implements PanacheRepository<DomainCycle> {
    private static final Logger log = LoggerFactory.getLogger(DomainCycleRepository.class);

    public List<DomainCycle> findByOwner(User owner) {
        log.info("Iniciando método findByOwner(ownerId={})", owner != null ? owner.getId() : null);
        List<DomainCycle> result = list("owner", owner);
        log.info("Finalizando método findByOwner com retorno: {} registros", result.size());
        return result;
    }
}
