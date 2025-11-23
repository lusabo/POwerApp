package com.powerapp.util;

import com.powerapp.dto.SprintAbsenceRequest;
import com.powerapp.dto.SprintRequest;
import com.powerapp.dto.SprintResponse;
import com.powerapp.entity.Sprint;
import com.powerapp.entity.SprintAbsence;
import com.powerapp.entity.TeamMember;
import com.powerapp.entity.User;
import com.powerapp.dto.SprintJiraResponse;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SprintMapper {
    public Sprint toNewEntity(SprintRequest request, User owner, Sprint existing, List<TeamMember> members) {
        Sprint sprint = existing != null ? existing : new Sprint();
        sprint.setOwner(owner);
        sprint.setName(request.name);
        sprint.setOperationsSpikesDays(request.operationsSpikesDays != null ? request.operationsSpikesDays : 0);
        sprint.getAbsences().clear();
        for (SprintAbsenceRequest req : safeAbsences(request.absences)) {
            if (req == null || req.teamMemberId == null || req.days == null) {
                continue;
            }
            TeamMember member = members.stream()
                    .filter(m -> m.getId().equals(req.teamMemberId))
                    .findFirst()
                    .orElse(null);
            if (member == null) {
                continue;
            }
            SprintAbsence absence = new SprintAbsence();
            absence.setSprint(sprint);
            absence.setTeamMember(member);
            absence.setDays(req.days);
            sprint.getAbsences().add(absence);
        }
        return sprint;
    }

    public Sprint toResponseEntityWithJira(SprintRequest request,
                                           User owner,
                                           SprintJiraResponse summary,
                                           List<TeamMember> members) {
        Sprint sprint = toNewEntity(request, owner, new Sprint(), members);
        sprint.setJiraSprintId(summary.sprintId);
        sprint.setStartDate(parseDate(summary.startDate));
        sprint.setEndDate(parseDate(summary.endDate));
        sprint.setStoryPointsCompleted((int) Math.round(summary.storyPointsDelivered));
        return sprint;
    }

    public void updateFromJira(Sprint sprint, SprintJiraResponse summary) {
        sprint.setJiraSprintId(summary.sprintId);
        sprint.setStartDate(parseDate(summary.startDate));
        sprint.setEndDate(parseDate(summary.endDate));
        sprint.setStoryPointsCompleted((int) Math.round(summary.storyPointsDelivered));
    }

    private java.time.LocalDate parseDate(String value) {
        return value != null ? OffsetDateTime.parse(value).toLocalDate() : null;
    }

    private List<SprintAbsenceRequest> safeAbsences(List<SprintAbsenceRequest> absences) {
        return absences == null ? Collections.emptyList() : absences.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    public SprintResponse toResponse(Sprint sprint) {
        SprintResponse resp = new SprintResponse();
        resp.id = sprint.getId();
        resp.name = sprint.getName();
        resp.jiraSprintId = sprint.getJiraSprintId();
        resp.startDate = sprint.getStartDate() != null ? sprint.getStartDate().toString() : null;
        resp.endDate = sprint.getEndDate() != null ? sprint.getEndDate().toString() : null;
        resp.storyPointsCompleted = sprint.getStoryPointsCompleted();
        resp.operationsSpikesDays = sprint.getOperationsSpikesDays();
        resp.workingDays = sprint.getWorkingDays();
        resp.ceremonyDays = sprint.getCeremonyDays();
        resp.holidayDays = sprint.getHolidayDays();
        resp.netCapacityDays = sprint.getNetCapacityDays();
        resp.teamSize = sprint.getTeamSize();
        resp.absencesDays = sprint.getAbsencesDays();
        resp.capacityTotal = sprint.getCapacityTotal();
        resp.capacityPercent = sprint.getCapacityPercent();
        resp.capacityFinal = sprint.getCapacityFinal();
        resp.capacityFinalPercent = sprint.getCapacityFinalPercent();
        return resp;
    }
}
