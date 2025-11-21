package com.powerapp.service;

import com.powerapp.dto.AlertResponse;
import com.powerapp.dto.ForecastResponse;
import com.powerapp.model.DomainCycle;
import com.powerapp.model.Sprint;
import com.powerapp.model.User;
import com.powerapp.repository.DomainCycleRepository;
import com.powerapp.repository.SprintRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ForecastService {
    @Inject
    SprintRepository sprints;

    @Inject
    DomainCycleRepository domainCycles;

    @Inject
    CapacityService capacityService;

    public ForecastResponse forecastSprint(Long sprintId, User owner) {
        Sprint sprint = sprints.findById(sprintId);
        if (sprint == null || !sprint.getOwner().getId().equals(owner.getId())) {
            return new ForecastResponse("sprint", 0d, "not-found");
        }
        List<Sprint> owned = sprints.findByOwner(owner);
        double spAverage = owned.stream()
                .map(Sprint::getStoryPointsCompleted)
                .filter(v -> v != null && v > 0)
                .mapToDouble(Integer::doubleValue)
                .average()
                .orElse(0d);
        double capacityAverage = owned.stream()
                .map(Sprint::getCapacity)
                .filter(v -> v != null && v > 0)
                .mapToDouble(Integer::doubleValue)
                .average()
                .orElse(1d);
        double nextCapacity = capacityService.calculate(sprint).capacity;
        double forecast = spAverage * (nextCapacity / capacityAverage);
        return new ForecastResponse("sprint-" + sprintId, forecast, "sp_average * (capacity_next / capacity_avg)");
    }

    public ForecastResponse forecastEpic(String epicKey, User owner) {
        // Placeholder: integrate Jira metrics for completed vs total SP inside epic.
        return new ForecastResponse("epic-" + epicKey, 0d, "connect Jira to calculate remaining SP");
    }

    public ForecastResponse forecastDomainCycle(Long dcId, User owner) {
        DomainCycle dc = domainCycles.findById(dcId);
        if (dc == null || !dc.getOwner().getId().equals(owner.getId())) {
            return new ForecastResponse("domain-cycle", 0d, "not-found");
        }
        List<Sprint> filtered = new ArrayList<>();
        for (Sprint sprint : sprints.findByOwner(owner)) {
            if (sprint.getDomainCycle() != null && sprint.getDomainCycle().getId().equals(dcId)) {
                filtered.add(sprint);
            }
        }
        double spAverage = filtered.stream()
                .map(Sprint::getStoryPointsCompleted)
                .filter(v -> v != null && v > 0)
                .mapToDouble(Integer::doubleValue)
                .average()
                .orElse(0d);
        return new ForecastResponse("domain-cycle-" + dcId, spAverage, "average SP across domain cycle");
    }

    public List<AlertResponse> alertsForDomainCycle(Long dcId, User owner) {
        List<AlertResponse> alerts = new ArrayList<>();
        DomainCycle dc = domainCycles.findById(dcId);
        if (dc == null || !dc.getOwner().getId().equals(owner.getId())) {
            alerts.add(new AlertResponse("Domain cycle not found"));
            return alerts;
        }
        // Simplified alert: if total SP > naive capacity threshold.
        int expectedCapacity = sprints.findByOwner(owner).size() * 100;
        alerts.add(new AlertResponse("Validate epic sizes vs capacity " + expectedCapacity + " for DC " + dc.getName()));
        return alerts;
    }
}
