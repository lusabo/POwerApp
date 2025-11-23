package com.powerapp.controller;

import com.powerapp.dto.UnplannedItemRequest;
import com.powerapp.entity.Sprint;
import com.powerapp.entity.UnplannedItem;
import com.powerapp.repository.SprintRepository;
import com.powerapp.repository.UnplannedRepository;
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

@Path("/unplanned")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class UnplannedController {
    private static final Logger log = LoggerFactory.getLogger(UnplannedController.class);

    private final UnplannedRepository unplannedItems;
    private final SprintRepository sprints;
    private final CurrentUser currentUser;

    public UnplannedController(UnplannedRepository unplannedItems, SprintRepository sprints, CurrentUser currentUser) {
        this.unplannedItems = unplannedItems;
        this.sprints = sprints;
        this.currentUser = currentUser;
    }

    @POST
    @Transactional
    public Response create(UnplannedItemRequest request) {
        log.info("Iniciando método create(request)");
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
        Response response = Response.status(Response.Status.CREATED).entity(item).build();
        log.info("Finalizando método create com retorno: 201 id={}", item.getId());
        return response;
    }

    @GET
    @Path("/sprint/{id}")
    public Response bySprint(@PathParam("id") Long sprintId) {
        log.info("Iniciando método bySprint(sprintId={})", sprintId);
        Sprint sprint = sprints.findById(sprintId);
        if (sprint == null || !sprint.getOwner().getId().equals(currentUser.get().getId())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        List<UnplannedItem> list = unplannedItems.findBySprint(sprint);
        Response response = Response.ok(list).build();
        log.info("Finalizando método bySprint com status {}", response.getStatus());
        return response;
    }
}
