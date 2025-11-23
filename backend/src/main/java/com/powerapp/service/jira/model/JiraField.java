package com.powerapp.service.jira.model;

public enum JiraField {
    EFFORT("jira.api.effort-field", "Effort Size"),
    FEATURE_TEAM("jira.api.feature-team-field", "Feature Team"),
    PARENT_LINK("jira.api.parent-link-field", "Parent Link"),
    STORY_POINTS("jira.api.story-points-field", "Story Points");

    private final String propertyKey;
    private final String friendlyName;

    JiraField(String propertyKey, String friendlyName) {
        this.propertyKey = propertyKey;
        this.friendlyName = friendlyName;
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public String getFriendlyName() {
        return friendlyName;
    }
}
