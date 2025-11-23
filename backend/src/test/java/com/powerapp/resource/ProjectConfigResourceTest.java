package com.powerapp.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.powerapp.dto.ProjectConfigRequest;
import com.powerapp.dto.ProjectConfigResponse;
import com.powerapp.model.ProjectConfig;
import com.powerapp.model.User;
import com.powerapp.repository.ProjectConfigRepository;
import com.powerapp.security.CurrentUser;
import com.powerapp.service.JiraService;
import jakarta.ws.rs.core.Response;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class ProjectConfigResourceTest {

    @Mock
    ProjectConfigRepository configs;

    @Mock
    CurrentUser currentUser;

    @Mock
    JiraService jiraService;

    @InjectMocks
    ProjectConfigResource resource;

    private User user;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        when(currentUser.get()).thenReturn(user);
    }

    @Test
    void saveShouldResolveBoardIdAndCeremonies() {
        when(configs.findByOwner(user)).thenReturn(Optional.empty());
        when(jiraService.resolveBoardIdByName(eq("BOARD"), any())).thenReturn(123L);

        ProjectConfigRequest request = new ProjectConfigRequest();
        request.projectName = "Projeto";
        request.jiraKey = "JIRA";
        request.board = "BOARD";
        request.ceremoniesDays = 2;
        request.featureTeam = "FT";

        Response response = resource.save(request);
        assertEquals(200, response.getStatus());

        ArgumentCaptor<ProjectConfig> captor = ArgumentCaptor.forClass(ProjectConfig.class);
        verify(configs).persist(captor.capture());
        ProjectConfig saved = captor.getValue();
        assertEquals(123L, saved.getBoardId());
        assertEquals(2, saved.getCeremoniesDays());

        ProjectConfigResponse body = (ProjectConfigResponse) response.getEntity();
        assertEquals(123L, body.boardId);
        assertEquals(2, body.ceremoniesDays);
    }

    @Test
    void getShouldReturnConfigResponse() {
        ProjectConfig cfg = new ProjectConfig();
        cfg.setProjectName("P");
        cfg.setJiraKey("JK");
        cfg.setBoard("B");
        cfg.setBoardId(10L);
        cfg.setCeremoniesDays(3);
        cfg.setFeatureTeam("FT");
        when(configs.findByOwner(user)).thenReturn(Optional.of(cfg));

        ProjectConfigResponse resp = resource.get();

        assertEquals("P", resp.projectName);
        assertEquals(10L, resp.boardId);
        assertEquals(3, resp.ceremoniesDays);
        assertEquals("FT", resp.featureTeam);
    }
}
