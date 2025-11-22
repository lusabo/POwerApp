package com.powerapp.resource;

import com.powerapp.dto.AlertResponse;
import com.powerapp.dto.ForecastResponse;
import com.powerapp.model.User;
import com.powerapp.security.CurrentUser;
import com.powerapp.service.ForecastService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/forecast")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class ForecastResource {
    private static final Logger log = LoggerFactory.getLogger(ForecastResource.class);

    private final ForecastService forecastService;
    private final CurrentUser currentUser;

    public ForecastResource(ForecastService forecastService, CurrentUser currentUser) {
        this.forecastService = forecastService;
        this.currentUser = currentUser;
    }

    @GET
    @Path("/sprint/{id}")
    public ForecastResponse forecastSprint(@PathParam("id") Long id) {
        log.info("Iniciando método forecastSprint(id={})", id);
        User user = currentUser.get();
        ForecastResponse response = forecastService.forecastSprint(id, user);
        log.info("Finalizando método forecastSprint com retorno: {}", response.forecast);
        return response;
    }

    @GET
    @Path("/epic/{epicKey}")
    public ForecastResponse forecastEpic(@PathParam("epicKey") String epicKey) {
        log.info("Iniciando método forecastEpic(epicKey={})", epicKey);
        ForecastResponse response = forecastService.forecastEpic(epicKey, currentUser.get());
        log.info("Finalizando método forecastEpic com retorno: {}", response.forecast);
        return response;
    }

    @GET
    @Path("/dc/{dcId}")
    public ForecastResponse forecastDomainCycle(@PathParam("dcId") Long dcId) {
        log.info("Iniciando método forecastDomainCycle(dcId={})", dcId);
        ForecastResponse response = forecastService.forecastDomainCycle(dcId, currentUser.get());
        log.info("Finalizando método forecastDomainCycle com retorno: {}", response.forecast);
        return response;
    }

    @GET
    @Path("/alerts/epics/{dcId}")
    public List<AlertResponse> epicAlerts(@PathParam("dcId") Long dcId) {
        log.info("Iniciando método epicAlerts(dcId={})", dcId);
        List<AlertResponse> response = forecastService.alertsForDomainCycle(dcId, currentUser.get());
        log.info("Finalizando método epicAlerts com retorno: {} alertas", response.size());
        return response;
    }
}
