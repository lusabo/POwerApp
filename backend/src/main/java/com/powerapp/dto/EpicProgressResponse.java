package com.powerapp.dto;

public class EpicProgressResponse {
    public String epicKey;
    public Integer completedIssues;
    public Integer totalIssues;
    public Double progressPercentage;

    public EpicProgressResponse(String epicKey, Integer completedIssues, Integer totalIssues, Double progressPercentage) {
        this.epicKey = epicKey;
        this.completedIssues = completedIssues;
        this.totalIssues = totalIssues;
        this.progressPercentage = progressPercentage;
    }
}
