package com.powerapp.service.usecase;

import com.powerapp.util.SprintMapper;
import com.powerapp.service.SprintCapacityService;
import com.powerapp.dto.SprintRequest;
import com.powerapp.dto.SprintResponse;
import com.powerapp.entity.DomainCycle;
import com.powerapp.entity.Sprint;
import com.powerapp.entity.User;
import com.powerapp.repository.DomainCycleRepository;
import com.powerapp.repository.SprintRepository;
import com.powerapp.repository.TeamMemberRepository;
import com.powerapp.util.MessageService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CreateSprintUseCase {
    private final SprintRepository sprints;
    private final TeamMemberRepository teamMembers;
    private final SprintCapacityService capacityService;
    private final DomainCycleRepository domainCycles;
    private final SprintMapper mapper = new SprintMapper();
    private final MessageService messages;

    public CreateSprintUseCase(SprintRepository sprints,
                               TeamMemberRepository teamMembers,
                               SprintCapacityService capacityService,
                               DomainCycleRepository domainCycles,
                               MessageService messages) {
        this.sprints = sprints;
        this.teamMembers = teamMembers;
        this.capacityService = capacityService;
        this.domainCycles = domainCycles;
        this.messages = messages;
    }

    public SprintResponse execute(SprintRequest request, User owner) {
        Sprint sprint = persistWithTransaction(request, owner);
        return mapper.toResponse(sprint);
    }

    @jakarta.transaction.Transactional
    Sprint persistWithTransaction(SprintRequest request, User owner) {
        DomainCycle dc = null;
        if (request.domainCycleId != null) {
            dc = domainCycles.findById(request.domainCycleId);
        }
        final DomainCycle domainCycle = dc;
        Sprint sprint = sprints.findByNameAndOwner(request.name, owner)
                .map(existing -> mapper.toNewEntity(request, owner, existing, teamMembers.findByOwner(owner), domainCycle))
                .orElseGet(() -> mapper.toNewEntity(request, owner, null, teamMembers.findByOwner(owner), domainCycle));
        if (sprint.getId() == null) {
            sprints.persist(sprint);
        }
        capacityService.recalc(sprint, owner);
        return sprint;
    }
}
