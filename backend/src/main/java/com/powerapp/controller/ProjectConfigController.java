package com.powerapp.controller;

import com.powerapp.dto.ProjectConfigRequest;
import com.powerapp.dto.ProjectConfigResponse;
import com.powerapp.entity.ProjectConfig;
import com.powerapp.entity.User;
import com.powerapp.repository.ProjectConfigRepository;
import com.powerapp.config.CurrentUser;
import com.powerapp.service.jira.JiraService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/project-config")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class ProjectConfigController {
    private static final Logger log = LoggerFactory.getLogger(ProjectConfigController.class);

    private final ProjectConfigRepository configs;
    private final CurrentUser currentUser;
    private final JiraService jiraService;

    public ProjectConfigController(ProjectConfigRepository configs, CurrentUser currentUser, JiraService jiraService) {
        this.configs = configs;
        this.currentUser = currentUser;
        this.jiraService = jiraService;
    }

    @GET
    public ProjectConfigResponse get() {
        log.info("Iniciando método get()");
        ProjectConfigResponse response = configs.findByOwner(currentUser.get())
                .map(ProjectConfigResponse::fromEntity)
                .orElse(new ProjectConfigResponse(null, null, null, null, null, null));
        log.info("Finalizando método get com retorno: projectName={}", response.projectName);
        return response;
    }

    @POST
    @Transactional
    public Response save(ProjectConfigRequest request) {
        log.info("Iniciando método save(request)");
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Request body required").build();
        }
        User user = currentUser.get();
        ProjectConfig config = configs.findByOwner(user).orElseGet(ProjectConfig::new);
        config.setOwner(user);
        config.setProjectName(request.projectName);
        config.setJiraKey(request.jiraKey);
        config.setBoard(request.board);
        // Resolve board id via Jira Agile API when board name is provided.
        if (request.board != null && !request.board.isBlank()) {
            Long resolvedBoardId = jiraService.resolveBoardIdByName(request.board, config);
            config.setBoardId(resolvedBoardId);
        } else {
            config.setBoardId(null);
        }
        config.setCeremoniesDays(request.ceremoniesDays);
        config.setFeatureTeam(request.featureTeam);
        if (config.getId() == null) {
            configs.persist(config);
        }
        Response response = Response.ok(ProjectConfigResponse.fromEntity(config)).build();
        log.info("Finalizando método save com status {}", response.getStatus());
        return response;
    }
}
