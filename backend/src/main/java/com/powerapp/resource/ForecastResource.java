package com.powerapp.resource;

import com.powerapp.dto.AlertResponse;
import com.powerapp.dto.ForecastResponse;
import com.powerapp.model.User;
import com.powerapp.security.CurrentUser;
import com.powerapp.service.ForecastService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/forecast")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class ForecastResource {
    @Inject
    ForecastService forecastService;

    @Inject
    CurrentUser currentUser;

    @GET
    @Path("/sprint/{id}")
    public ForecastResponse forecastSprint(@PathParam("id") Long id) {
        User user = currentUser.get();
        return forecastService.forecastSprint(id, user);
    }

    @GET
    @Path("/epic/{epicKey}")
    public ForecastResponse forecastEpic(@PathParam("epicKey") String epicKey) {
        return forecastService.forecastEpic(epicKey, currentUser.get());
    }

    @GET
    @Path("/dc/{dcId}")
    public ForecastResponse forecastDomainCycle(@PathParam("dcId") Long dcId) {
        return forecastService.forecastDomainCycle(dcId, currentUser.get());
    }

    @GET
    @Path("/alerts/epics/{dcId}")
    public List<AlertResponse> epicAlerts(@PathParam("dcId") Long dcId) {
        return forecastService.alertsForDomainCycle(dcId, currentUser.get());
    }
}
