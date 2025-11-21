package com.powerapp.resource;

import com.powerapp.dto.UnplannedItemRequest;
import com.powerapp.model.Sprint;
import com.powerapp.model.UnplannedItem;
import com.powerapp.repository.SprintRepository;
import com.powerapp.repository.UnplannedRepository;
import com.powerapp.security.CurrentUser;
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

@Path("/unplanned")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class UnplannedResource {
    @Inject
    UnplannedRepository unplannedItems;

    @Inject
    SprintRepository sprints;

    @Inject
    CurrentUser currentUser;

    @POST
    @Transactional
    public Response create(UnplannedItemRequest request) {
        Sprint sprint = sprints.findById(request.sprintId);
        if (sprint == null || !sprint.getOwner().getId().equals(currentUser.get().getId())) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid sprint").build();
        }
        UnplannedItem item = new UnplannedItem();
        item.setTitle(request.title);
        item.setStoryPoints(request.storyPoints);
        item.setSprint(sprint);
        item.setOwner(currentUser.get());
        unplannedItems.persist(item);
        return Response.status(Response.Status.CREATED).entity(item).build();
    }

    @GET
    @Path("/sprint/{id}")
    public Response bySprint(@PathParam("id") Long sprintId) {
        Sprint sprint = sprints.findById(sprintId);
        if (sprint == null || !sprint.getOwner().getId().equals(currentUser.get().getId())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        List<UnplannedItem> list = unplannedItems.findBySprint(sprint);
        return Response.ok(list).build();
    }
}
