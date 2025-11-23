package com.powerapp.service.jira.port;

import com.powerapp.dto.SprintJiraResponse;
import com.powerapp.entity.User;

public interface JiraGateway {
    SprintJiraResponse fetchSprintSummary(String sprintName, User user);
    boolean isSprintClosed(Long sprintId, User user);
}
