package com.powerapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "project_config")
public class ProjectConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "jira_key")
    private String jiraKey;

    @Column(name = "board")
    private String board;

    @Column(name = "board_id")
    private Long boardId;

    @Column(name = "ceremonies_days")
    private Integer ceremoniesDays;

    @Column(name = "feature_team")
    private String featureTeam;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User owner;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getJiraKey() {
        return jiraKey;
    }

    public void setJiraKey(String jiraKey) {
        this.jiraKey = jiraKey;
    }

    public String getBoard() {
        return board;
    }

    public void setBoard(String board) {
        this.board = board;
    }

    public Long getBoardId() {
        return boardId;
    }

    public void setBoardId(Long boardId) {
        this.boardId = boardId;
    }

    public Integer getCeremoniesDays() {
        return ceremoniesDays;
    }

    public void setCeremoniesDays(Integer ceremoniesDays) {
        this.ceremoniesDays = ceremoniesDays;
    }

    public String getFeatureTeam() {
        return featureTeam;
    }

    public void setFeatureTeam(String featureTeam) {
        this.featureTeam = featureTeam;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}
