package com.powerapp.service.usecase;

import com.powerapp.service.SprintCapacityService;
import com.powerapp.dto.SprintJiraResponse;
import com.powerapp.dto.SprintResponse;
import com.powerapp.entity.Sprint;
import com.powerapp.entity.User;
import com.powerapp.repository.SprintRepository;
import com.powerapp.service.jira.port.JiraGateway;
import com.powerapp.util.MessageService;
import com.powerapp.util.SprintMapper;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.OffsetDateTime;
import java.time.LocalDate;

@ApplicationScoped
public class ReloadSprintUseCase {
    private final SprintRepository sprints;
    private final SprintCapacityService capacityService;
    private final JiraGateway jiraGateway;
    private final MessageService messages;

    public ReloadSprintUseCase(SprintRepository sprints,
                               SprintCapacityService capacityService,
                               JiraGateway jiraGateway,
                               MessageService messages) {
        this.sprints = sprints;
        this.capacityService = capacityService;
        this.jiraGateway = jiraGateway;
        this.messages = messages;
    }

    public SprintResponse execute(Long id, User owner) {
        Sprint sprint = sprints.findById(id);
        if (sprint == null || sprint.getOwner() == null || !sprint.getOwner().getId().equals(owner.getId())) {
            throw new IllegalArgumentException(messages.get("error.sprint.notFound"));
        }
        SprintJiraResponse summary = jiraGateway.fetchSprintSummary(sprint.getName(), owner);
        Sprint updated = persistWithTransaction(sprint, summary, owner);
        return new SprintMapper().toResponse(updated);
    }

    @jakarta.transaction.Transactional
    Sprint persistWithTransaction(Sprint sprint, SprintJiraResponse summary, User owner) {
        if (summary != null) {
            sprint.setJiraSprintId(summary.sprintId);
            if (summary.startDate != null) {
                sprint.setStartDate(parseDate(summary.startDate));
            }
            if (summary.endDate != null) {
                sprint.setEndDate(parseDate(summary.endDate));
            }
            if (summary.storyPointsDelivered != null) {
                sprint.setStoryPointsCompleted((int) Math.round(summary.storyPointsDelivered));
            }
            if (summary.goal != null && !summary.goal.isBlank()) {
                sprint.setGoal(summary.goal);
            }
            if (summary.state != null && !summary.state.isBlank()) {
                sprint.setSprintState(summary.state);
            }
        }
        capacityService.recalc(sprint, owner);
        return sprint;
    }

    private LocalDate parseDate(String value) {
        return value != null ? OffsetDateTime.parse(value).toLocalDate() : null;
    }
}
