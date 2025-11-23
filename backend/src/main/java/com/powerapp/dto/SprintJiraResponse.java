package com.powerapp.dto;

public class SprintJiraResponse {
    public Long sprintId;
    public String sprintName;
    public String startDate;
    public String endDate;
    public String completeDate;
    public Double storyPointsDelivered;

    public SprintJiraResponse(Long sprintId, String sprintName, String startDate, String endDate, String completeDate, Double storyPointsDelivered) {
        this.sprintId = sprintId;
        this.sprintName = sprintName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.completeDate = completeDate;
        this.storyPointsDelivered = storyPointsDelivered;
    }
}
