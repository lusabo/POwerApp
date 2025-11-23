package com.powerapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

@Entity
@Table(name = "sprints")
public class Sprint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "sp_completed")
    private Integer storyPointsCompleted;

    @Column(name = "jira_sprint_id")
    private Long jiraSprintId;

    @Column(name = "operations_spikes_days")
    private Integer operationsSpikesDays;

    @Column(name = "working_days")
    private Integer workingDays;

    @Column(name = "ceremony_days")
    private Integer ceremonyDays;

    @Column(name = "holiday_days")
    private Integer holidayDays;

    @Column(name = "net_capacity_days")
    private Integer netCapacityDays;

    @Column(name = "team_size")
    private Integer teamSize;

    @Column(name = "absences_days")
    private Integer absencesDays;

    @Column(name = "capacity_total")
    private Integer capacityTotal;

    @Column(name = "capacity_percent")
    private Double capacityPercent;

    @Column(name = "capacity_final")
    private Integer capacityFinal;

    @Column(name = "capacity_final_percent")
    private Double capacityFinalPercent;

    @OneToMany(mappedBy = "sprint", fetch = FetchType.LAZY, cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<SprintAbsence> absences = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_cycle_id")
    private DomainCycle domainCycle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getStoryPointsCompleted() {
        return storyPointsCompleted;
    }

    public void setStoryPointsCompleted(Integer storyPointsCompleted) {
        this.storyPointsCompleted = storyPointsCompleted;
    }

    public Long getJiraSprintId() {
        return jiraSprintId;
    }

    public void setJiraSprintId(Long jiraSprintId) {
        this.jiraSprintId = jiraSprintId;
    }

    public Integer getOperationsSpikesDays() {
        return operationsSpikesDays;
    }

    public void setOperationsSpikesDays(Integer operationsSpikesDays) {
        this.operationsSpikesDays = operationsSpikesDays;
    }

    public Integer getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(Integer workingDays) {
        this.workingDays = workingDays;
    }

    public Integer getCeremonyDays() {
        return ceremonyDays;
    }

    public void setCeremonyDays(Integer ceremonyDays) {
        this.ceremonyDays = ceremonyDays;
    }

    public Integer getHolidayDays() {
        return holidayDays;
    }

    public void setHolidayDays(Integer holidayDays) {
        this.holidayDays = holidayDays;
    }

    public Integer getNetCapacityDays() {
        return netCapacityDays;
    }

    public void setNetCapacityDays(Integer netCapacityDays) {
        this.netCapacityDays = netCapacityDays;
    }

    public Integer getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(Integer teamSize) {
        this.teamSize = teamSize;
    }

    public Integer getAbsencesDays() {
        return absencesDays;
    }

    public void setAbsencesDays(Integer absencesDays) {
        this.absencesDays = absencesDays;
    }

    public Integer getCapacityTotal() {
        return capacityTotal;
    }

    public void setCapacityTotal(Integer capacityTotal) {
        this.capacityTotal = capacityTotal;
    }

    public Double getCapacityPercent() {
        return capacityPercent;
    }

    public void setCapacityPercent(Double capacityPercent) {
        this.capacityPercent = capacityPercent;
    }

    public Integer getCapacityFinal() {
        return capacityFinal;
    }

    public void setCapacityFinal(Integer capacityFinal) {
        this.capacityFinal = capacityFinal;
    }

    public Double getCapacityFinalPercent() {
        return capacityFinalPercent;
    }

    public void setCapacityFinalPercent(Double capacityFinalPercent) {
        this.capacityFinalPercent = capacityFinalPercent;
    }

    public List<SprintAbsence> getAbsences() {
        return absences;
    }

    public void setAbsences(List<SprintAbsence> absences) {
        this.absences = absences;
    }

    public DomainCycle getDomainCycle() {
        return domainCycle;
    }

    public void setDomainCycle(DomainCycle domainCycle) {
        this.domainCycle = domainCycle;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}
