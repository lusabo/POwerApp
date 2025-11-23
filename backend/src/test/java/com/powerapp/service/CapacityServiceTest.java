package com.powerapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.powerapp.dto.CapacityResponse;
import com.powerapp.model.Holiday;
import com.powerapp.model.Sprint;
import com.powerapp.model.User;
import com.powerapp.repository.HolidayRepository;
import com.powerapp.repository.TeamMemberRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class CapacityServiceTest {

    @Mock
    HolidayRepository holidays;

    @Mock
    TeamMemberRepository teamMembers;

    private CapacityService service;
    private Sprint sprint;
    private User owner;

    @BeforeEach
    void setup() {
        service = new CapacityService(holidays, teamMembers);
        owner = new User();
        sprint = new Sprint();
        sprint.setOwner(owner);
        sprint.setStartDate(LocalDate.of(2025, 1, 6)); // Monday
        sprint.setEndDate(LocalDate.of(2025, 1, 10));   // Friday
    }

    @Test
    void shouldCalculateCapacityExcludingHolidaysAndWeekends() {
        Holiday holiday = new Holiday();
        holiday.setDate(LocalDate.of(2025, 1, 8)); // Wednesday
        when(holidays.findByOwner(owner)).thenReturn(List.of(holiday));
        when(teamMembers.findByOwner(owner)).thenReturn(List.of(new com.powerapp.model.TeamMember(), new com.powerapp.model.TeamMember()));

        CapacityResponse response = service.calculate(sprint);

        // 5 working days in range, minus 1 holiday = 4 days; 2 members -> capacity = 8
        assertEquals(8, response.capacity);
        assertEquals(4, response.workingDays);
        assertEquals(1, response.holidayDays);
    }
}
