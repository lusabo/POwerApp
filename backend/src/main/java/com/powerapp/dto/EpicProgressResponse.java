package com.powerapp.dto;

public class EpicProgressResponse {
    public String epicKey;
    public Integer completedIssues;
    public Integer totalIssues;
    public Double progressPercentage;
    public String source;

    public EpicProgressResponse(String epicKey, Integer completedIssues, Integer totalIssues, Double progressPercentage, String source) {
        this.epicKey = epicKey;
        this.completedIssues = completedIssues;
        this.totalIssues = totalIssues;
        this.progressPercentage = progressPercentage;
        this.source = source;
    }
}
