package com.powerapp.repository;

import com.powerapp.entity.TeamMember;
import com.powerapp.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class TeamMemberRepository implements PanacheRepository<TeamMember> {
    private static final Logger log = LoggerFactory.getLogger(TeamMemberRepository.class);

    public List<TeamMember> findByOwner(User owner) {
        log.info("Iniciando método findByOwner(ownerId={})", owner != null ? owner.getId() : null);
        List<TeamMember> result = list("owner", owner);
        log.info("Finalizando método findByOwner com retorno: {} registros", result.size());
        return result;
    }
}
