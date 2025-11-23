package com.powerapp.dto;

import com.powerapp.model.Epic;
import java.math.BigDecimal;

public class EpicResponse {
    public Long id;
    public String epicKey;
    public String name;
    public String effortSize;
    public Integer issuesCount;
    public BigDecimal storyPointsSum;
    public Long domainCycleId;
    public String domainCycleName;

    public static EpicResponse fromEntity(Epic epic) {
        EpicResponse resp = new EpicResponse();
        resp.id = epic.getId();
        resp.epicKey = epic.getEpicKey();
        resp.name = epic.getName();
        resp.effortSize = epic.getEffortSize();
        resp.issuesCount = epic.getIssuesCount();
        resp.storyPointsSum = epic.getStoryPointsSum();
        if (epic.getDomainCycle() != null) {
            resp.domainCycleId = epic.getDomainCycle().getId();
            resp.domainCycleName = epic.getDomainCycle().getName();
        }
        return resp;
    }
}
