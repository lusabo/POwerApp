package com.powerapp.repository;

import com.powerapp.model.TeamMember;
import com.powerapp.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class TeamMemberRepository implements PanacheRepository<TeamMember> {
    public List<TeamMember> findByOwner(User owner) {
        return list("owner", owner);
    }
}
