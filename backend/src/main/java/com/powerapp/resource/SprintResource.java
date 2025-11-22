package com.powerapp.resource;

import com.powerapp.dto.CapacityResponse;
import com.powerapp.dto.SprintRequest;
import com.powerapp.model.DomainCycle;
import com.powerapp.model.Sprint;
import com.powerapp.model.User;
import com.powerapp.repository.DomainCycleRepository;
import com.powerapp.repository.SprintRepository;
import com.powerapp.repository.UnplannedRepository;
import com.powerapp.security.CurrentUser;
import com.powerapp.service.CapacityService;
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
public class SprintResource {
    private static final Logger log = LoggerFactory.getLogger(SprintResource.class);

    private final SprintRepository sprints;
    private final DomainCycleRepository domainCycles;
    private final CurrentUser currentUser;
    private final CapacityService capacityService;
    private final UnplannedRepository unplannedItems;

    public SprintResource(SprintRepository sprints,
                          DomainCycleRepository domainCycles,
                          CurrentUser currentUser,
                          CapacityService capacityService,
                          UnplannedRepository unplannedItems) {
        this.sprints = sprints;
        this.domainCycles = domainCycles;
        this.currentUser = currentUser;
        this.capacityService = capacityService;
        this.unplannedItems = unplannedItems;
    }

    @POST
    @Transactional
    public Response create(SprintRequest request) {
        log.info("Iniciando método create(request)");
        User user = currentUser.get();
        Sprint sprint = new Sprint();
        sprint.setName(request.name);
        sprint.setStartDate(request.startDate);
        sprint.setEndDate(request.endDate);
        sprint.setCapacity(request.capacity);
        sprint.setStoryPointsCompleted(request.storyPointsCompleted);
        sprint.setOwner(user);
        if (request.domainCycleId != null) {
            DomainCycle cycle = domainCycles.findById(request.domainCycleId);
            if (cycle != null && cycle.getOwner().getId().equals(user.getId())) {
                sprint.setDomainCycle(cycle);
            }
        }
        sprints.persist(sprint);
        Response response = Response.status(Response.Status.CREATED).entity(sprint).build();
        log.info("Finalizando método create com retorno: 201 id={}", sprint.getId());
        return response;
    }

    @GET
    public List<Sprint> list() {
        log.info("Iniciando método list()");
        List<Sprint> result = sprints.findByOwner(currentUser.get());
        log.info("Finalizando método list com retorno: {} registros", result.size());
        return result;
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") Long id) {
        log.info("Iniciando método get(id={})", id);
        Response response = sprints.findByIdAndOwner(id, currentUser.get())
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
        log.info("Finalizando método get com status {}", response.getStatus());
        return response;
    }

    @GET
    @Path("/{id}/capacity")
    public Response capacity(@PathParam("id") Long id) {
        log.info("Iniciando método capacity(id={})", id);
        Sprint sprint = sprints.findById(id);
        if (sprint == null || !sprint.getOwner().getId().equals(currentUser.get().getId())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        CapacityResponse response = capacityService.calculate(sprint);
        Response httpResponse = Response.ok(response).build();
        log.info("Finalizando método capacity com status {}", httpResponse.getStatus());
        return httpResponse;
    }

    @GET
    @Path("/{id}/unplanned")
    public Response unplanned(@PathParam("id") Long id) {
        log.info("Iniciando método unplanned(id={})", id);
        Sprint sprint = sprints.findById(id);
        if (sprint == null || !sprint.getOwner().getId().equals(currentUser.get().getId())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        Response response = Response.ok(unplannedItems.findBySprint(sprint)).build();
        log.info("Finalizando método unplanned com status {}", response.getStatus());
        return response;
    }
}
