package com.powerapp.service;

import com.powerapp.dto.AlertResponse;
import com.powerapp.dto.ForecastResponse;
import com.powerapp.model.DomainCycle;
import com.powerapp.model.Sprint;
import com.powerapp.model.User;
import com.powerapp.repository.DomainCycleRepository;
import com.powerapp.repository.SprintRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ForecastService {
    private static final Logger log = LoggerFactory.getLogger(ForecastService.class);

    private final SprintRepository sprints;
    private final DomainCycleRepository domainCycles;
    private final CapacityService capacityService;

    public ForecastService(SprintRepository sprints, DomainCycleRepository domainCycles, CapacityService capacityService) {
        this.sprints = sprints;
        this.domainCycles = domainCycles;
        this.capacityService = capacityService;
    }

    public ForecastResponse forecastSprint(Long sprintId, User owner) {
        log.info("Iniciando método forecastSprint(sprintId={}, ownerId={})", sprintId, owner != null ? owner.getId() : null);
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
        ForecastResponse response = new ForecastResponse("sprint-" + sprintId, forecast, "sp_average * (capacity_next / capacity_avg)");
        log.info("Finalizando método forecastSprint com retorno: forecast={} capacityNext={} capacityAvg={}", forecast, nextCapacity, capacityAverage);
        return response;
    }

    public ForecastResponse forecastEpic(String epicKey, User owner) {
        log.info("Iniciando método forecastEpic(epicKey={}, ownerId={})", epicKey, owner != null ? owner.getId() : null);
        // Placeholder: integrate Jira metrics for completed vs total SP inside epic.
        ForecastResponse response = new ForecastResponse("epic-" + epicKey, 0d, "connect Jira to calculate remaining SP");
        log.info("Finalizando método forecastEpic com retorno: {}", response.forecast);
        return response;
    }

    public ForecastResponse forecastDomainCycle(Long dcId, User owner) {
        log.info("Iniciando método forecastDomainCycle(dcId={}, ownerId={})", dcId, owner != null ? owner.getId() : null);
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
        ForecastResponse response = new ForecastResponse("domain-cycle-" + dcId, spAverage, "average SP across domain cycle");
        log.info("Finalizando método forecastDomainCycle com retorno: {}", response.forecast);
        return response;
    }

    public List<AlertResponse> alertsForDomainCycle(Long dcId, User owner) {
        log.info("Iniciando método alertsForDomainCycle(dcId={}, ownerId={})", dcId, owner != null ? owner.getId() : null);
        List<AlertResponse> alerts = new ArrayList<>();
        DomainCycle dc = domainCycles.findById(dcId);
        if (dc == null || !dc.getOwner().getId().equals(owner.getId())) {
            alerts.add(new AlertResponse("Domain cycle not found"));
            return alerts;
        }
        // Simplified alert: if total SP > naive capacity threshold.
        int expectedCapacity = sprints.findByOwner(owner).size() * 100;
        alerts.add(new AlertResponse("Validate epic sizes vs capacity " + expectedCapacity + " for DC " + dc.getName()));
        log.info("Finalizando método alertsForDomainCycle com retorno: {} alertas", alerts.size());
        return alerts;
    }
}
