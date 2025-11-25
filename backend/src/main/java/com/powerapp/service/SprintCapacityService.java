package com.powerapp.service;

import com.powerapp.dto.CapacityResponse;
import com.powerapp.entity.Sprint;
import com.powerapp.entity.User;
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

    public CapacityResponse recalc(Sprint sprint, User user) {
        CapacityResponse base = capacityService.calculate(sprint);
        int ceremonyDays = configs.findByOwner(user)
                .map(c -> c.getCeremoniesDays() != null ? c.getCeremoniesDays() : 0)
                .orElse(0);
        int workingDays = base.workingDays; // dias úteis (segunda a sexta), incluindo feriados
        int holidayDays = base.holidayDays;
        // capacidade líquida: dias úteis - feriados - cerimônias
        int netCapacity = workingDays - holidayDays - ceremonyDays;
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
        sprint.setTeamSize(teamSize);

        CapacityResponse enriched = new CapacityResponse(sprint.getId(), totalCapacity, workingDays, base.holidayDays);
        enriched.netCapacityDays = netCapacity;
        enriched.absencesDays = absencesDays;
        enriched.ceremonyDays = ceremonyDays;
        enriched.capacityPercent = totalPercent;
        enriched.capacityFinal = finalCapacity;
        enriched.capacityFinalPercent = finalPercent;
        return enriched;
    }
}
