package com.powerapp.repository;

import com.powerapp.model.Epic;
import com.powerapp.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class EpicRepository implements PanacheRepository<Epic> {
    private static final Logger log = LoggerFactory.getLogger(EpicRepository.class);

    public List<Epic> findByOwner(User owner) {
        log.info("Iniciando método findByOwner(ownerId={})", owner != null ? owner.getId() : null);
        List<Epic> result = list("owner", owner);
        log.info("Finalizando método findByOwner com retorno: {} registros", result.size());
        return result;
    }

    public Optional<Epic> findByKeyAndOwner(String key, User owner) {
        log.info("Iniciando método findByKeyAndOwner(key={}, ownerId={})", key, owner != null ? owner.getId() : null);
        Optional<Epic> result = find("epicKey = ?1 and owner = ?2", key, owner).firstResultOptional();
        log.info("Finalizando método findByKeyAndOwner com retorno presente: {}", result.isPresent());
        return result;
    }

    public List<Epic> findByOwnerAndDomainCycle(User owner, Long domainCycleId) {
        if (domainCycleId == null) {
            return find("owner", owner).list();
        }
        return find("owner = ?1 and domainCycle.id = ?2", owner, domainCycleId).list();
    }
}
