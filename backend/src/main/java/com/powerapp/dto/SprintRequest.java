package com.powerapp.dto;

import java.util.List;

public class SprintRequest {
    public String name;
    /**
     * Optional sprint goal/description.
     */
    public String goal;
    /**
     * ISO-8601 date (yyyy-MM-dd).
     */
    public String startDate;
    /**
     * ISO-8601 date (yyyy-MM-dd).
     */
    public String endDate;
    /**
     * Optional domain cycle identifier the sprint belongs to.
     */
    public Long domainCycleId;
    public Integer operationsSpikesDays;
    public List<SprintAbsenceRequest> absences;
}
