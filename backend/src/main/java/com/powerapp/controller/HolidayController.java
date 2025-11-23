package com.powerapp.controller;

import com.powerapp.dto.HolidayRequest;
import com.powerapp.entity.Holiday;
import com.powerapp.entity.User;
import com.powerapp.repository.HolidayRepository;
import com.powerapp.config.CurrentUser;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
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

@Path("/holidays")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class HolidayController {
    private static final Logger log = LoggerFactory.getLogger(HolidayController.class);

    private final HolidayRepository holidays;
    private final CurrentUser currentUser;

    public HolidayController(HolidayRepository holidays, CurrentUser currentUser) {
        this.holidays = holidays;
        this.currentUser = currentUser;
    }

    @POST
    @Transactional
    public Response create(HolidayRequest request) {
        log.info("Iniciando método create(request)");
        User user = currentUser.get();
        Holiday holiday = new Holiday();
        holiday.setDescription(request.description);
        holiday.setDate(request.date);
        holiday.setOwner(user);
        holidays.persist(holiday);
        Response response = Response.status(Response.Status.CREATED).entity(holiday).build();
        log.info("Finalizando método create com retorno: 201 id={}", holiday.getId());
        return response;
    }

    @GET
    public List<Holiday> list() {
        log.info("Iniciando método list()");
        List<Holiday> result = holidays.findByOwner(currentUser.get());
        log.info("Finalizando método list com retorno: {} registros", result.size());
        return result;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        log.info("Iniciando método delete(id={})", id);
        Holiday holiday = holidays.findById(id);
        if (holiday == null || !holiday.getOwner().getId().equals(currentUser.get().getId())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        holidays.delete(holiday);
        Response response = Response.noContent().build();
        log.info("Finalizando método delete com status {}", response.getStatus());
        return response;
    }
}
