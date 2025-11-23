package com.powerapp.infrastructure.jira;

import com.powerapp.application.port.JiraGateway;
import com.powerapp.dto.SprintJiraResponse;
import com.powerapp.model.User;
import com.powerapp.service.JiraService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JiraGatewayAdapter implements JiraGateway {
    private final JiraService jiraService;

    public JiraGatewayAdapter(JiraService jiraService) {
        this.jiraService = jiraService;
    }

    @Override
    public SprintJiraResponse fetchSprintSummary(String sprintName, User user) {
        return jiraService.fetchSprintSummary(sprintName, user);
    }

    @Override
    public boolean isSprintClosed(Long sprintId, User user) {
        return jiraService.isSprintClosed(sprintId, user);
    }
}
