package com.powerapp.dto;

import java.time.LocalDate;

public class SprintRequest {
    public String name;
    public LocalDate startDate;
    public LocalDate endDate;
    public Integer capacity;
    public Integer storyPointsCompleted;
    public Long domainCycleId;
}
