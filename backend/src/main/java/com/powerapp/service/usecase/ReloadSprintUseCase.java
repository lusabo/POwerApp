package com.powerapp.service.usecase;

import com.powerapp.util.SprintMapper;
import com.powerapp.service.jira.port.JiraGateway;
import com.powerapp.service.SprintCapacityService;
import com.powerapp.dto.SprintJiraResponse;
import com.powerapp.dto.SprintResponse;
import com.powerapp.entity.Sprint;
import com.powerapp.entity.User;
import com.powerapp.repository.SprintRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ReloadSprintUseCase {
    private final JiraGateway jiraGateway;
    private final SprintRepository sprints;
    private final SprintCapacityService capacityService;
    private final SprintMapper mapper = new SprintMapper();

    public ReloadSprintUseCase(JiraGateway jiraGateway,
                               SprintRepository sprints,
                               SprintCapacityService capacityService) {
        this.jiraGateway = jiraGateway;
        this.sprints = sprints;
        this.capacityService = capacityService;
    }

    public SprintResponse execute(Long id, User owner) {
        Sprint sprint = sprints.findById(id);
        if (sprint == null || sprint.getOwner() == null || !sprint.getOwner().getId().equals(owner.getId())) {
            throw new IllegalArgumentException("Sprint não encontrada");
        }
        if (!jiraGateway.isSprintClosed(sprint.getJiraSprintId(), owner)) {
            throw new IllegalStateException("Sprint não está fechada");
        }
        SprintJiraResponse summary = jiraGateway.fetchSprintSummary(sprint.getName(), owner);
        Sprint updated = persistWithTransaction(sprint, summary, owner);
        return mapper.toResponse(updated);
    }

    @jakarta.transaction.Transactional
    Sprint persistWithTransaction(Sprint sprint, SprintJiraResponse summary, User owner) {
        mapper.updateFromJira(sprint, summary);
        capacityService.recalc(sprint, owner);
        return sprint;
    }
}
