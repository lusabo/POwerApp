package com.powerapp.resource;

import com.powerapp.dto.TeamMemberRequest;
import com.powerapp.model.TeamMember;
import com.powerapp.model.User;
import com.powerapp.repository.TeamMemberRepository;
import com.powerapp.security.CurrentUser;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/team")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class TeamResource {
    @Inject
    TeamMemberRepository teamMembers;

    @Inject
    CurrentUser currentUser;

    @POST
    @Transactional
    public Response create(TeamMemberRequest request) {
        User user = currentUser.get();
        TeamMember member = new TeamMember();
        member.setName(request.name);
        member.setRole(request.role);
        member.setWeeklyLoadHours(request.weeklyLoadHours);
        member.setOwner(user);
        teamMembers.persist(member);
        return Response.status(Response.Status.CREATED).entity(member).build();
    }

    @GET
    public List<TeamMember> list() {
        return teamMembers.findByOwner(currentUser.get());
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") Long id, TeamMemberRequest request) {
        TeamMember member = teamMembers.findById(id);
        if (member == null || !member.getOwner().getId().equals(currentUser.get().getId())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        member.setName(request.name);
        member.setRole(request.role);
        member.setWeeklyLoadHours(request.weeklyLoadHours);
        return Response.ok(member).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        TeamMember member = teamMembers.findById(id);
        if (member == null || !member.getOwner().getId().equals(currentUser.get().getId())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        teamMembers.delete(member);
        return Response.noContent().build();
    }
}
