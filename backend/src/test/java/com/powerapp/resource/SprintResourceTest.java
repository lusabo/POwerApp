package com.powerapp.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.powerapp.dto.SprintJiraResponse;
import com.powerapp.model.Sprint;
import com.powerapp.model.User;
import com.powerapp.repository.DomainCycleRepository;
import com.powerapp.repository.SprintRepository;
import com.powerapp.repository.UnplannedRepository;
import com.powerapp.security.CurrentUser;
import com.powerapp.service.CapacityService;
import com.powerapp.service.JiraService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class SprintResourceTest {

    @Mock
    SprintRepository sprints;
    @Mock
    DomainCycleRepository domainCycles;
    @Mock
    CurrentUser currentUser;
    @Mock
    CapacityService capacityService;
    @Mock
    UnplannedRepository unplannedItems;
    @Mock
    JiraService jiraService;

    @InjectMocks
    SprintResource resource;

    @Test
    void persistSprintFromSummaryShouldCreateNewSprint() {
        User user = new User();
        user.setId(1L);
        when(currentUser.get()).thenReturn(user);
        when(sprints.findByNameAndOwner("Sprint X", user)).thenReturn(Optional.empty());

        SprintJiraResponse summary = new SprintJiraResponse(
                999L,
                "Sprint X",
                "2025-01-01T00:00:00.000Z",
                "2025-01-15T00:00:00.000Z",
                "2025-01-15T00:00:00.000Z",
                21d
        );

        resource.persistSprintFromSummary(summary, user);

        ArgumentCaptor<Sprint> captor = ArgumentCaptor.forClass(Sprint.class);
        verify(sprints).persist(captor.capture());
        Sprint saved = captor.getValue();
        assertEquals("Sprint X", saved.getName());
        assertEquals(999L, saved.getJiraSprintId());
        assertEquals(21, saved.getStoryPointsCompleted());
        assertEquals(2025, saved.getStartDate().getYear());
        assertEquals(2025, saved.getEndDate().getYear());
    }
}
