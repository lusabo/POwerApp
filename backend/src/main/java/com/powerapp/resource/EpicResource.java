package com.powerapp.resource;

import com.powerapp.dto.EpicProgressResponse;
import com.powerapp.model.User;
import com.powerapp.security.CurrentUser;
import com.powerapp.service.JiraService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/epics")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class EpicResource {
    @Inject
    CurrentUser currentUser;

    @Inject
    JiraService jiraService;

    @GET
    @Path("/{epicKey}")
    public EpicProgressResponse get(@PathParam("epicKey") String epicKey) {
        User user = currentUser.get();
        return jiraService.fetchEpicProgress(epicKey, user);
    }
}
