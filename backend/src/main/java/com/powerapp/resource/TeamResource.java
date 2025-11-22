package com.powerapp.resource;

import com.powerapp.dto.TeamMemberRequest;
import com.powerapp.model.TeamMember;
import com.powerapp.model.User;
import com.powerapp.repository.TeamMemberRepository;
import com.powerapp.security.CurrentUser;
import jakarta.annotation.security.RolesAllowed;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/team")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class TeamResource {
    private static final Logger log = LoggerFactory.getLogger(TeamResource.class);

    private final TeamMemberRepository teamMembers;
    private final CurrentUser currentUser;

    public TeamResource(TeamMemberRepository teamMembers, CurrentUser currentUser) {
        this.teamMembers = teamMembers;
        this.currentUser = currentUser;
    }

    @POST
    @Transactional
    public Response create(TeamMemberRequest request) {
        log.info("Iniciando método create(request)");
        User user = currentUser.get();
        TeamMember member = new TeamMember();
        member.setName(request.name);
        member.setRole(request.role);
        member.setWeeklyLoadHours(request.weeklyLoadHours);
        member.setOwner(user);
        teamMembers.persist(member);
        Response response = Response.status(Response.Status.CREATED).entity(member).build();
        log.info("Finalizando método create com retorno: 201 id={}", member.getId());
        return response;
    }

    @GET
    public List<TeamMember> list() {
        log.info("Iniciando método list()");
        List<TeamMember> result = teamMembers.findByOwner(currentUser.get());
        log.info("Finalizando método list com retorno: {} registros", result.size());
        return result;
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") Long id, TeamMemberRequest request) {
        log.info("Iniciando método update(id={})", id);
        TeamMember member = teamMembers.findById(id);
        if (member == null || !member.getOwner().getId().equals(currentUser.get().getId())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        member.setName(request.name);
        member.setRole(request.role);
        member.setWeeklyLoadHours(request.weeklyLoadHours);
        Response response = Response.ok(member).build();
        log.info("Finalizando método update com status {}", response.getStatus());
        return response;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        log.info("Iniciando método delete(id={})", id);
        TeamMember member = teamMembers.findById(id);
        if (member == null || !member.getOwner().getId().equals(currentUser.get().getId())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        teamMembers.delete(member);
        Response response = Response.noContent().build();
        log.info("Finalizando método delete com status {}", response.getStatus());
        return response;
    }
}
