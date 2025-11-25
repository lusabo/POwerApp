package com.powerapp.util;

import com.powerapp.dto.SprintAbsenceRequest;
import com.powerapp.dto.SprintRequest;
import com.powerapp.dto.SprintResponse;
import com.powerapp.entity.DomainCycle;
import com.powerapp.entity.Sprint;
import com.powerapp.entity.SprintAbsence;
import com.powerapp.entity.TeamMember;
import com.powerapp.entity.User;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SprintMapper {
    public Sprint toNewEntity(SprintRequest request, User owner, Sprint existing, List<TeamMember> members, DomainCycle domainCycle) {
        Sprint sprint = existing != null ? existing : new Sprint();
        sprint.setOwner(owner);
        sprint.setName(request.name);
        sprint.setGoal(request.goal);
        sprint.setOperationsSpikesDays(request.operationsSpikesDays != null ? request.operationsSpikesDays : 0);
        sprint.setStartDate(parseLocalDate(request.startDate));
        sprint.setEndDate(parseLocalDate(request.endDate));
        sprint.setDomainCycle(domainCycle);
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
    private LocalDate parseLocalDate(String value) {
        return value != null && !value.isBlank() ? LocalDate.parse(value) : null;
    }

    private List<SprintAbsenceRequest> safeAbsences(List<SprintAbsenceRequest> absences) {
        return absences == null ? Collections.emptyList() : absences.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    public SprintResponse toResponse(Sprint sprint) {
        SprintResponse resp = new SprintResponse();
        resp.id = sprint.getId();
        resp.name = sprint.getName();
        resp.goal = sprint.getGoal();
        resp.jiraSprintId = sprint.getJiraSprintId();
        resp.startDate = sprint.getStartDate() != null ? sprint.getStartDate().toString() : null;
        resp.endDate = sprint.getEndDate() != null ? sprint.getEndDate().toString() : null;
        resp.storyPointsCompleted = sprint.getStoryPointsCompleted();
        resp.operationsSpikesDays = sprint.getOperationsSpikesDays();
        resp.teamSize = sprint.getTeamSize();
        resp.sprintState = sprint.getSprintState();
        if (sprint.getDomainCycle() != null) {
            resp.domainCycleId = sprint.getDomainCycle().getId();
            resp.domainCycleName = sprint.getDomainCycle().getName();
        }
        return resp;
    }
}
