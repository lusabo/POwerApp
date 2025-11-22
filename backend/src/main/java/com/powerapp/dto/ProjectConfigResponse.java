package com.powerapp.dto;

import com.powerapp.model.ProjectConfig;

public class ProjectConfigResponse {
    public String projectName;
    public String jiraKey;
    public String board;
    public String featureTeam;

    public ProjectConfigResponse() {
    }

    public ProjectConfigResponse(String projectName, String jiraKey, String board, String featureTeam) {
        this.projectName = projectName;
        this.jiraKey = jiraKey;
        this.board = board;
        this.featureTeam = featureTeam;
    }

    public static ProjectConfigResponse fromEntity(ProjectConfig config) {
        return new ProjectConfigResponse(
                config.getProjectName(),
                config.getJiraKey(),
                config.getBoard(),
                config.getFeatureTeam()
        );
    }
}
