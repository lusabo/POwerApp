package com.powerapp.resource;

import com.powerapp.dto.ProjectConfigRequest;
import com.powerapp.dto.ProjectConfigResponse;
import com.powerapp.model.ProjectConfig;
import com.powerapp.model.User;
import com.powerapp.repository.ProjectConfigRepository;
import com.powerapp.security.CurrentUser;
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
public class ProjectConfigResource {
    private static final Logger log = LoggerFactory.getLogger(ProjectConfigResource.class);

    private final ProjectConfigRepository configs;
    private final CurrentUser currentUser;

    public ProjectConfigResource(ProjectConfigRepository configs, CurrentUser currentUser) {
        this.configs = configs;
        this.currentUser = currentUser;
    }

    @GET
    public ProjectConfigResponse get() {
        log.info("Iniciando método get()");
        ProjectConfigResponse response = configs.findByOwner(currentUser.get())
                .map(ProjectConfigResponse::fromEntity)
                .orElse(new ProjectConfigResponse(null, null, null, null));
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
        config.setFeatureTeam(request.featureTeam);
        if (config.getId() == null) {
            configs.persist(config);
        }
        Response response = Response.ok(ProjectConfigResponse.fromEntity(config)).build();
        log.info("Finalizando método save com status {}", response.getStatus());
        return response;
    }
}
