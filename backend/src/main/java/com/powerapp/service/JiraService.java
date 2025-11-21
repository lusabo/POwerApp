package com.powerapp.service;

import com.powerapp.dto.EpicProgressResponse;
import com.powerapp.model.User;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JiraService {
    /**
     * Stub integration. Replace with reactive REST client to Jira API using user token/email.
     */
    public EpicProgressResponse fetchEpicProgress(String epicKey, User user) {
        String source = user.getJiraApiEmail() != null ? "jira" : "mock";
        // Placeholder progress numbers.
        return new EpicProgressResponse(epicKey, 5, 10, 50d, source);
    }
}
