package com.powerapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "epics")
public class Epic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "epic_key", nullable = false)
    private String epicKey;

    @Column(nullable = false)
    private String name;

    @Column(name = "effort_size")
    private String effortSize;

    @Column(name = "issues_count")
    private Integer issuesCount;

    @Column(name = "story_points_sum")
    private BigDecimal storyPointsSum;

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

    public String getEpicKey() {
        return epicKey;
    }

    public void setEpicKey(String epicKey) {
        this.epicKey = epicKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEffortSize() {
        return effortSize;
    }

    public void setEffortSize(String effortSize) {
        this.effortSize = effortSize;
    }

    public Integer getIssuesCount() {
        return issuesCount;
    }

    public void setIssuesCount(Integer issuesCount) {
        this.issuesCount = issuesCount;
    }

    public BigDecimal getStoryPointsSum() {
        return storyPointsSum;
    }

    public void setStoryPointsSum(BigDecimal storyPointsSum) {
        this.storyPointsSum = storyPointsSum;
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
