package com.powerapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
