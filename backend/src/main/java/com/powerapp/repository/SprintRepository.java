package com.powerapp.repository;

import com.powerapp.entity.Sprint;
import com.powerapp.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class SprintRepository implements PanacheRepository<Sprint> {
    private static final Logger log = LoggerFactory.getLogger(SprintRepository.class);

    public List<Sprint> findByOwner(User owner) {
        log.info("Iniciando método findByOwner(ownerId={})", owner != null ? owner.getId() : null);
        List<Sprint> result = list("owner", owner);
        log.info("Finalizando método findByOwner com retorno: {} registros", result.size());
        return result;
    }

    public Optional<Sprint> findByIdAndOwner(Long id, User owner) {
        log.info("Iniciando método findByIdAndOwner(id={}, ownerId={})", id, owner != null ? owner.getId() : null);
        Optional<Sprint> result = find("id = ?1 and owner = ?2", id, owner).firstResultOptional();
        log.info("Finalizando método findByIdAndOwner com retorno presente: {}", result.isPresent());
        return result;
    }

    public Optional<Sprint> findByNameAndOwner(String name, User owner) {
        log.info("Iniciando método findByNameAndOwner(name={}, ownerId={})", name, owner != null ? owner.getId() : null);
        Optional<Sprint> result = find("name = ?1 and owner = ?2", name, owner).firstResultOptional();
        log.info("Finalizando método findByNameAndOwner com retorno presente: {}", result.isPresent());
        return result;
    }
}
