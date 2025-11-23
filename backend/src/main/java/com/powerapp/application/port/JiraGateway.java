package com.powerapp.application.port;

import com.powerapp.dto.SprintJiraResponse;
import com.powerapp.model.User;

public interface JiraGateway {
    SprintJiraResponse fetchSprintSummary(String sprintName, User user);
    boolean isSprintClosed(Long sprintId, User user);
}
