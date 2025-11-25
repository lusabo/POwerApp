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

    @Column(name = "goal")
    private String goal;
    @Column(name = "sp_completed")
    private Integer storyPointsCompleted;

    @Column(name = "jira_sprint_id")
    private Long jiraSprintId;

    @Column(name = "operations_spikes_days")
    private Integer operationsSpikesDays;

    @Column(name = "team_size")
    private Integer teamSize;

    @Column(name = "sprint_state")
    private String sprintState;

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

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
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

    public Integer getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(Integer teamSize) {
        this.teamSize = teamSize;
    }


    public String getSprintState() {
        return sprintState;
    }

    public void setSprintState(String sprintState) {
        this.sprintState = sprintState;
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
