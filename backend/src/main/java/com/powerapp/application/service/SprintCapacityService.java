package com.powerapp.application.service;

import com.powerapp.dto.CapacityResponse;
import com.powerapp.model.Sprint;
import com.powerapp.model.User;
import com.powerapp.repository.ProjectConfigRepository;
import com.powerapp.repository.TeamMemberRepository;
import com.powerapp.service.CapacityService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SprintCapacityService {
    private final CapacityService capacityService;
    private final ProjectConfigRepository configs;
    private final TeamMemberRepository teamMembers;

    public SprintCapacityService(CapacityService capacityService,
                                 ProjectConfigRepository configs,
                                 TeamMemberRepository teamMembers) {
        this.capacityService = capacityService;
        this.configs = configs;
        this.teamMembers = teamMembers;
    }

    public void recalc(Sprint sprint, User user) {
        CapacityResponse base = capacityService.calculate(sprint);
        int ceremonyDays = configs.findByOwner(user)
                .map(c -> c.getCeremoniesDays() != null ? c.getCeremoniesDays() : 0)
                .orElse(0);
        int workingDaysRaw = base.workingDays + base.holidayDays; // úteis sem feriado
        int holidayDays = base.holidayDays;
        int netCapacity = workingDaysRaw - ceremonyDays - holidayDays;
        int teamSize = teamMembers.findByOwner(user).size();
        int absencesDays = sprint.getAbsences().stream()
                .map(a -> a.getDays() != null ? a.getDays() : 0)
                .mapToInt(Integer::intValue)
                .sum();
        int totalCapacity = netCapacity > 0 ? (netCapacity * Math.max(teamSize, 1)) - absencesDays : 0;
        double totalPercent = netCapacity > 0 && teamSize > 0 ? (double) totalCapacity / (netCapacity * teamSize) : 0d;
        int operations = sprint.getOperationsSpikesDays() != null ? sprint.getOperationsSpikesDays() : 0;
        int finalCapacity = totalCapacity - operations;
        double finalPercent = netCapacity > 0 && teamSize > 0 ? (double) finalCapacity / (netCapacity * teamSize) : 0d;

        sprint.setWorkingDays(workingDaysRaw);
        sprint.setHolidayDays(holidayDays);
        sprint.setCeremonyDays(ceremonyDays);
        sprint.setNetCapacityDays(netCapacity);
        sprint.setTeamSize(teamSize);
        sprint.setAbsencesDays(absencesDays);
        sprint.setCapacityTotal(totalCapacity);
        sprint.setCapacityPercent(totalPercent);
        sprint.setCapacityFinal(finalCapacity);
        sprint.setCapacityFinalPercent(finalPercent);
    }
}
