package com.powerapp.service.usecase;

import com.powerapp.util.SprintMapper;
import com.powerapp.service.jira.port.JiraGateway;
import com.powerapp.service.SprintCapacityService;
import com.powerapp.dto.SprintJiraResponse;
import com.powerapp.dto.SprintRequest;
import com.powerapp.dto.SprintResponse;
import com.powerapp.entity.Sprint;
import com.powerapp.entity.User;
import com.powerapp.repository.SprintRepository;
import com.powerapp.repository.TeamMemberRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CreateSprintUseCase {
    private final JiraGateway jiraGateway;
    private final SprintRepository sprints;
    private final TeamMemberRepository teamMembers;
    private final SprintCapacityService capacityService;
    private final SprintMapper mapper = new SprintMapper();

    public CreateSprintUseCase(JiraGateway jiraGateway,
                               SprintRepository sprints,
                               TeamMemberRepository teamMembers,
                               SprintCapacityService capacityService) {
        this.jiraGateway = jiraGateway;
        this.sprints = sprints;
        this.teamMembers = teamMembers;
        this.capacityService = capacityService;
    }

    public SprintResponse execute(SprintRequest request, User owner) {
        SprintJiraResponse summary = jiraGateway.fetchSprintSummary(request.name, owner);
        if (summary.startDate == null || summary.endDate == null) {
            throw new IllegalArgumentException("Sprint sem datas na API do Jira");
        }
        Sprint sprint = persistWithTransaction(request, owner, summary);
        return mapper.toResponse(sprint);
    }

    @jakarta.transaction.Transactional
    Sprint persistWithTransaction(SprintRequest request, User owner, SprintJiraResponse summary) {
        Sprint sprint = sprints.findByNameAndOwner(request.name, owner)
                .map(existing -> mapper.toNewEntity(request, owner, existing, teamMembers.findByOwner(owner)))
                .orElseGet(() -> mapper.toResponseEntityWithJira(request, owner, summary, teamMembers.findByOwner(owner)));
        mapper.updateFromJira(sprint, summary);
        if (sprint.getId() == null) {
            sprints.persist(sprint);
        }
        capacityService.recalc(sprint, owner);
        sprints.persist(sprint);
        return sprint;
    }
}
