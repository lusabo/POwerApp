package com.powerapp.resource;

import com.powerapp.dto.DomainCycleRequest;
import com.powerapp.model.DomainCycle;
import com.powerapp.model.User;
import com.powerapp.repository.DomainCycleRepository;
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

@Path("/domain-cycles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class DomainCycleResource {
    @Inject
    DomainCycleRepository domainCycles;

    @Inject
    CurrentUser currentUser;

    @POST
    @Transactional
    public Response create(DomainCycleRequest request) {
        User user = currentUser.get();
        DomainCycle cycle = new DomainCycle();
        cycle.setName(request.name);
        cycle.setStartDate(request.startDate);
        cycle.setEndDate(request.endDate);
        cycle.setOwner(user);
        domainCycles.persist(cycle);
        return Response.status(Response.Status.CREATED).entity(cycle).build();
    }

    @GET
    public List<DomainCycle> list() {
        return domainCycles.findByOwner(currentUser.get());
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") Long id) {
        return domainCycles.findByIdOptional(id)
                .filter(dc -> dc.getOwner().getId().equals(currentUser.get().getId()))
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }
}
