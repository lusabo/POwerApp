package com.powerapp.dto;

public class CapacityResponse {
    public Long sprintId;
    public Integer capacity;
    public Integer workingDays;
    public Integer holidayDays;

    public CapacityResponse(Long sprintId, Integer capacity, Integer workingDays, Integer holidayDays) {
        this.sprintId = sprintId;
        this.capacity = capacity;
        this.workingDays = workingDays;
        this.holidayDays = holidayDays;
    }
}
