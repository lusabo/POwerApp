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
import jakarta.inject.Inject;
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

@Path("/sprints")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class SprintResource {
    @Inject
    SprintRepository sprints;

    @Inject
    DomainCycleRepository domainCycles;

    @Inject
    CurrentUser currentUser;

    @Inject
    CapacityService capacityService;

    @Inject
    UnplannedRepository unplannedItems;

    @POST
    @Transactional
    public Response create(SprintRequest request) {
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
        return Response.status(Response.Status.CREATED).entity(sprint).build();
    }

    @GET
    public List<Sprint> list() {
        return sprints.findByOwner(currentUser.get());
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") Long id) {
        return sprints.findByIdAndOwner(id, currentUser.get())
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @GET
    @Path("/{id}/capacity")
    public Response capacity(@PathParam("id") Long id) {
        Sprint sprint = sprints.findById(id);
        if (sprint == null || !sprint.getOwner().getId().equals(currentUser.get().getId())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        CapacityResponse response = capacityService.calculate(sprint);
        return Response.ok(response).build();
    }

    @GET
    @Path("/{id}/unplanned")
    public Response unplanned(@PathParam("id") Long id) {
        Sprint sprint = sprints.findById(id);
        if (sprint == null || !sprint.getOwner().getId().equals(currentUser.get().getId())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(unplannedItems.findBySprint(sprint)).build();
    }
}
