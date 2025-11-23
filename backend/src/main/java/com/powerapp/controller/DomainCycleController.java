package com.powerapp.controller;

import com.powerapp.dto.DomainCycleRequest;
import com.powerapp.entity.DomainCycle;
import com.powerapp.entity.User;
import com.powerapp.repository.DomainCycleRepository;
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

@Path("/domain-cycles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class DomainCycleController {
    private static final Logger log = LoggerFactory.getLogger(DomainCycleController.class);

    private final DomainCycleRepository domainCycles;
    private final CurrentUser currentUser;

    public DomainCycleController(DomainCycleRepository domainCycles, CurrentUser currentUser) {
        this.domainCycles = domainCycles;
        this.currentUser = currentUser;
    }

    @POST
    @Transactional
    public Response create(DomainCycleRequest request) {
        log.info("Iniciando método create(request)");
        User user = currentUser.get();
        DomainCycle cycle = new DomainCycle();
        cycle.setName(request.name);
        cycle.setStartDate(request.startDate);
        cycle.setEndDate(request.endDate);
        cycle.setOwner(user);
        domainCycles.persist(cycle);
        Response response = Response.status(Response.Status.CREATED).entity(cycle).build();
        log.info("Finalizando método create com retorno: 201 id={}", cycle.getId());
        return response;
    }

    @GET
    public List<DomainCycle> list() {
        log.info("Iniciando método list()");
        List<DomainCycle> result = domainCycles.findByOwner(currentUser.get());
        log.info("Finalizando método list com retorno: {} registros", result.size());
        return result;
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") Long id) {
        log.info("Iniciando método get(id={})", id);
        Response response = domainCycles.findByIdOptional(id)
                .filter(dc -> dc.getOwner().getId().equals(currentUser.get().getId()))
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
        log.info("Finalizando método get com status {}", response.getStatus());
        return response;
    }
}
