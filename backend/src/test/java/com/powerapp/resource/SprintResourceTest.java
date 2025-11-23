package com.powerapp.resource;

import static org.mockito.Mockito.*;

import com.powerapp.application.usecase.CreateSprintUseCase;
import com.powerapp.application.usecase.GetSprintUseCase;
import com.powerapp.application.usecase.ListSprintsUseCase;
import com.powerapp.application.usecase.ReloadSprintUseCase;
import com.powerapp.application.port.JiraGateway;
import com.powerapp.dto.SprintRequest;
import com.powerapp.dto.SprintResponse;
import com.powerapp.security.CurrentUser;
import com.powerapp.model.User;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class SprintResourceTest {

    @Mock
    CreateSprintUseCase createSprint;
    @Mock
    ReloadSprintUseCase reloadSprint;
    @Mock
    ListSprintsUseCase listSprints;
    @Mock
    GetSprintUseCase getSprint;
    @Mock
    JiraGateway jiraGateway;
    @Mock
    CurrentUser currentUser;

    @InjectMocks
    SprintResource resource;

    @Test
    void createShouldDelegateToUseCase() {
        User user = new User();
        user.setId(1L);
        when(currentUser.get()).thenReturn(user);

        SprintRequest request = new SprintRequest();
        request.name = "Sprint X";

        SprintResponse expected = new SprintResponse();
        expected.id = 10L;
        expected.name = "Sprint X";
        when(createSprint.execute(request, user)).thenReturn(expected);

        var response = resource.create(request);

        verify(createSprint).execute(request, user);
        assert response.getStatus() == 201;
    }
}
