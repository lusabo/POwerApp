package com.powerapp.resource;

import com.powerapp.dto.ProjectConfigRequest;
import com.powerapp.dto.ProjectConfigResponse;
import com.powerapp.model.ProjectConfig;
import com.powerapp.model.User;
import com.powerapp.repository.ProjectConfigRepository;
import com.powerapp.security.CurrentUser;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/project-config")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class ProjectConfigResource {
    @Inject
    ProjectConfigRepository configs;

    @Inject
    CurrentUser currentUser;

    @GET
    public ProjectConfigResponse get() {
        return configs.findByOwner(currentUser.get())
                .map(ProjectConfigResponse::fromEntity)
                .orElse(new ProjectConfigResponse(null, null, null, null));
    }

    @POST
    @Transactional
    public Response save(ProjectConfigRequest request) {
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
        return Response.ok(ProjectConfigResponse.fromEntity(config)).build();
    }
}
