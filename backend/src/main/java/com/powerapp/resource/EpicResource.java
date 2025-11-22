package com.powerapp.resource;

import com.powerapp.dto.EpicProgressResponse;
import com.powerapp.model.User;
import com.powerapp.security.CurrentUser;
import com.powerapp.service.JiraService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/epics")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class EpicResource {
    private static final Logger log = LoggerFactory.getLogger(EpicResource.class);

    private final CurrentUser currentUser;
    private final JiraService jiraService;

    public EpicResource(CurrentUser currentUser, JiraService jiraService) {
        this.currentUser = currentUser;
        this.jiraService = jiraService;
    }

    @GET
    @Path("/{epicKey}")
    public EpicProgressResponse get(@PathParam("epicKey") String epicKey) {
        log.info("Iniciando método get(epicKey={})", epicKey);
        User user = currentUser.get();
        EpicProgressResponse response = jiraService.fetchEpicProgress(epicKey, user);
        log.info("Finalizando método get com retorno: progress={}", response.progressPercentage);
        return response;
    }
}
