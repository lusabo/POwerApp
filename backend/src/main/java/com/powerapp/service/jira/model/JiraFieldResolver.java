package com.powerapp.service.jira.model;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class JiraFieldResolver {

    @ConfigProperty(name = "jira.api.effort-field")
    String effortFieldId;

    @ConfigProperty(name = "jira.api.feature-team-field")
    String featureTeamFieldId;

    @ConfigProperty(name = "jira.api.parent-link-field")
    String parentLinkFieldId;

    @ConfigProperty(name = "jira.api.story-points-field")
    String storyPointsFieldId;

    public String getId(JiraField field) {
        return switch (field) {
            case EFFORT -> effortFieldId;
            case FEATURE_TEAM -> featureTeamFieldId;
            case PARENT_LINK -> parentLinkFieldId;
            case STORY_POINTS -> storyPointsFieldId;
        };
    }

    public String getFriendlyName(JiraField field) {
        return field.getFriendlyName();
    }
}
