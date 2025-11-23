package com.powerapp.dto;

import com.powerapp.entity.ProjectConfig;

public class ProjectConfigResponse {
    public String projectName;
    public String jiraKey;
    public String board;
    public Long boardId;
    public Integer ceremoniesDays;
    public String featureTeam;

    public ProjectConfigResponse() {
    }

    public ProjectConfigResponse(String projectName, String jiraKey, String board, Long boardId, Integer ceremoniesDays, String featureTeam) {
        this.projectName = projectName;
        this.jiraKey = jiraKey;
        this.board = board;
        this.boardId = boardId;
        this.ceremoniesDays = ceremoniesDays;
        this.featureTeam = featureTeam;
    }

    public static ProjectConfigResponse fromEntity(ProjectConfig config) {
        return new ProjectConfigResponse(
                config.getProjectName(),
                config.getJiraKey(),
                config.getBoard(),
                config.getBoardId(),
                config.getCeremoniesDays(),
                config.getFeatureTeam()
        );
    }
}
