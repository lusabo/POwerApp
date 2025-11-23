package com.powerapp.service.usecase;

import com.powerapp.util.SprintMapper;
import com.powerapp.service.SprintCapacityService;
import com.powerapp.dto.SprintResponse;
import com.powerapp.entity.Sprint;
import com.powerapp.entity.User;
import com.powerapp.repository.SprintRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class GetSprintUseCase {
    private final SprintRepository sprints;
    private final SprintCapacityService capacityService;
    private final SprintMapper mapper = new SprintMapper();

    public GetSprintUseCase(SprintRepository sprints, SprintCapacityService capacityService) {
        this.sprints = sprints;
        this.capacityService = capacityService;
    }

    public Optional<SprintResponse> execute(Long id, User owner) {
        Sprint sprint = sprints.findById(id);
        if (sprint == null || sprint.getOwner() == null || !sprint.getOwner().getId().equals(owner.getId())) {
            return Optional.empty();
        }
        capacityService.recalc(sprint, owner);
        return Optional.of(mapper.toResponse(sprint));
    }
}
