package com.powerapp.service.jira.adapter;

import com.powerapp.service.jira.port.JiraGateway;
import com.powerapp.dto.SprintJiraResponse;
import com.powerapp.entity.User;
import com.powerapp.service.jira.JiraService;
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
