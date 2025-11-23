package com.powerapp.application.usecase;

import com.powerapp.application.mapper.SprintMapper;
import com.powerapp.application.service.SprintCapacityService;
import com.powerapp.dto.SprintResponse;
import com.powerapp.model.Sprint;
import com.powerapp.model.User;
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
                .peek(s -> capacityService.recalc(s, owner))
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
}
