package com.powerapp.dto;

public class CapacityResponse {
    public Long sprintId;
    public Integer capacity;
    public Integer workingDays;
    public Integer holidayDays;

    // derived fields for view only
    public Integer ceremonyDays;
    public Integer netCapacityDays;
    public Integer absencesDays;
    public Double capacityPercent;
    public Integer capacityFinal;
    public Double capacityFinalPercent;

    public CapacityResponse(Long sprintId, Integer capacity, Integer workingDays, Integer holidayDays) {
        this.sprintId = sprintId;
        this.capacity = capacity;
        this.workingDays = workingDays;
        this.holidayDays = holidayDays;
    }
}
