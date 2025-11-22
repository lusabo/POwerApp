package com.powerapp.dto;

import com.powerapp.model.ProjectConfig;

public class ProjectConfigResponse {
    public String projectName;
    public String jiraKey;
    public String board;
    public Long boardId;
    public String featureTeam;

    public ProjectConfigResponse() {
    }

    public ProjectConfigResponse(String projectName, String jiraKey, String board, Long boardId, String featureTeam) {
        this.projectName = projectName;
        this.jiraKey = jiraKey;
        this.board = board;
        this.boardId = boardId;
        this.featureTeam = featureTeam;
    }

    public static ProjectConfigResponse fromEntity(ProjectConfig config) {
        return new ProjectConfigResponse(
                config.getProjectName(),
                config.getJiraKey(),
                config.getBoard(),
                config.getBoardId(),
                config.getFeatureTeam()
        );
    }
}
