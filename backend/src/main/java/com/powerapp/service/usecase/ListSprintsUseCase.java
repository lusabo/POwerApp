package com.powerapp.service.usecase;

import com.powerapp.util.SprintMapper;
import com.powerapp.service.SprintCapacityService;
import com.powerapp.dto.SprintResponse;
import com.powerapp.entity.Sprint;
import com.powerapp.entity.User;
import com.powerapp.repository.SprintRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ListSprintsUseCase {
    private final SprintRepository sprints;
    private final SprintCapacityService capacityService;
    private final SprintMapper mapper = new SprintMapper();

    public ListSprintsUseCase(SprintRepository sprints, SprintCapacityService capacityService) {
        this.sprints = sprints;
        this.capacityService = capacityService;
    }

    public List<SprintResponse> execute(User owner) {
        return sprints.findByOwner(owner).stream()
                .map(s -> {
                    var capacity = capacityService.recalc(s, owner);
                    SprintResponse resp = mapper.toResponse(s);
                    resp.workingDays = capacity.workingDays;
                    resp.ceremonyDays = capacity.ceremonyDays;
                    resp.holidayDays = capacity.holidayDays;
                    resp.netCapacityDays = capacity.netCapacityDays;
                    resp.absencesDays = capacity.absencesDays;
                    resp.capacityTotal = capacity.capacity;
                    resp.capacityPercent = capacity.capacityPercent;
                    resp.capacityFinal = capacity.capacityFinal;
                    resp.capacityFinalPercent = capacity.capacityFinalPercent;
                    return resp;
                })
                .collect(Collectors.toList());
    }
}
