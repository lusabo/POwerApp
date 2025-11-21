package com.powerapp.service;

import com.powerapp.dto.CapacityResponse;
import com.powerapp.model.Holiday;
import com.powerapp.model.Sprint;
import com.powerapp.model.TeamMember;
import com.powerapp.repository.HolidayRepository;
import com.powerapp.repository.TeamMemberRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class CapacityService {
    @Inject
    HolidayRepository holidays;

    @Inject
    TeamMemberRepository teamMembers;

    /**
     * Basic capacity model: working days excluding weekends/holidays multiplied by team size.
     */
    public CapacityResponse calculate(Sprint sprint) {
        List<Holiday> holidayList = holidays.findByOwner(sprint.getOwner());
        Set<LocalDate> holidayDates = new HashSet<>();
        holidayList.forEach(h -> holidayDates.add(h.getDate()));

        int workingDays = 0;
        int holidayDays = 0;
        LocalDate date = sprint.getStartDate();
        while (!date.isAfter(sprint.getEndDate())) {
            boolean weekend = date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
            if (!weekend) {
                if (holidayDates.contains(date)) {
                    holidayDays++;
                } else {
                    workingDays++;
                }
            }
            date = date.plusDays(1);
        }
        int memberCount = teamMembers.findByOwner(sprint.getOwner()).size();
        int capacity = workingDays * Math.max(memberCount, 1);
        return new CapacityResponse(sprint.getId(), capacity, workingDays, holidayDays);
    }
}
