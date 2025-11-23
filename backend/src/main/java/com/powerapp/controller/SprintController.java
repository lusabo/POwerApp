package com.powerapp.controller;

import com.powerapp.service.usecase.CreateSprintUseCase;
import com.powerapp.service.usecase.GetSprintUseCase;
import com.powerapp.service.usecase.ListSprintsUseCase;
import com.powerapp.service.usecase.ReloadSprintUseCase;
import com.powerapp.service.jira.port.JiraGateway;
import com.powerapp.dto.SprintRequest;
import com.powerapp.dto.SprintResponse;
import com.powerapp.dto.SprintJiraRequest;
import com.powerapp.dto.SprintJiraResponse;
import com.powerapp.config.CurrentUser;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/sprints")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class SprintController {
    private static final Logger log = LoggerFactory.getLogger(SprintController.class);

    private final CreateSprintUseCase createSprint;
    private final ReloadSprintUseCase reloadSprint;
    private final ListSprintsUseCase listSprints;
    private final GetSprintUseCase getSprint;
    private final JiraGateway jiraGateway;
    private final CurrentUser currentUser;

    public SprintController(CreateSprintUseCase createSprint,
                          ReloadSprintUseCase reloadSprint,
                          ListSprintsUseCase listSprints,
                          GetSprintUseCase getSprint,
                          JiraGateway jiraGateway,
                          CurrentUser currentUser) {
        this.createSprint = createSprint;
        this.reloadSprint = reloadSprint;
        this.listSprints = listSprints;
        this.getSprint = getSprint;
        this.jiraGateway = jiraGateway;
        this.currentUser = currentUser;
    }

    @POST
    public Response create(SprintRequest request) {
        log.info("Iniciando método create(request)");
        try {
            SprintResponse resp = createSprint.execute(request, currentUser.get());
            log.info("Finalizando método create com retorno: 201 id={}", resp.id);
            return Response.status(Response.Status.CREATED).entity(resp).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @GET
    public List<SprintResponse> list() {
        log.info("Iniciando método list()");
        List<SprintResponse> result = listSprints.execute(currentUser.get());
        log.info("Finalizando método list com retorno: {} registros", result.size());
        return result;
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") Long id) {
        log.info("Iniciando método get(id={})", id);
        return getSprint.execute(id, currentUser.get())
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @POST
    @Path("/{id}/reload")
    public Response reload(@PathParam("id") Long id) {
        log.info("Iniciando método reload(id={})", id);
        try {
            SprintResponse resp = reloadSprint.execute(id, currentUser.get());
            Response response = Response.ok(resp).build();
            log.info("Finalizando método reload com status {}", response.getStatus());
            return response;
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/jira")
    public Response jiraSprint(SprintJiraRequest request) {
        String sprintName = request != null ? request.sprintName : null;
        log.info("Iniciando método jiraSprint(name={})", sprintName);
        if (sprintName == null || sprintName.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Informe o nome exato da sprint").build();
        }
        SprintJiraResponse summary = jiraGateway.fetchSprintSummary(sprintName, currentUser.get());
        Response response = Response.ok(summary).build();
        log.info("Finalizando método jiraSprint com status {}", response.getStatus());
        return response;
    }
}
