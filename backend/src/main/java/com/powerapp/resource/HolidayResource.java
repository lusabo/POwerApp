package com.powerapp.resource;

import com.powerapp.dto.HolidayRequest;
import com.powerapp.model.Holiday;
import com.powerapp.model.User;
import com.powerapp.repository.HolidayRepository;
import com.powerapp.security.CurrentUser;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
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

@Path("/holidays")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class HolidayResource {
    @Inject
    HolidayRepository holidays;

    @Inject
    CurrentUser currentUser;

    @POST
    @Transactional
    public Response create(HolidayRequest request) {
        User user = currentUser.get();
        Holiday holiday = new Holiday();
        holiday.setDescription(request.description);
        holiday.setDate(request.date);
        holiday.setOwner(user);
        holidays.persist(holiday);
        return Response.status(Response.Status.CREATED).entity(holiday).build();
    }

    @GET
    public List<Holiday> list() {
        return holidays.findByOwner(currentUser.get());
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        Holiday holiday = holidays.findById(id);
        if (holiday == null || !holiday.getOwner().getId().equals(currentUser.get().getId())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        holidays.delete(holiday);
        return Response.noContent().build();
    }
}
