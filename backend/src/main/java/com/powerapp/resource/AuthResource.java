package com.powerapp.resource;

import com.powerapp.dto.AuthRequest;
import com.powerapp.dto.AuthResponse;
import com.powerapp.dto.RegisterRequest;
import com.powerapp.dto.UserSettingsRequest;
import com.powerapp.model.User;
import com.powerapp.repository.UserRepository;
import com.powerapp.security.CurrentUser;
import com.powerapp.security.JwtService;
import io.quarkus.security.UnauthorizedException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.mindrot.jbcrypt.BCrypt;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    @Inject
    UserRepository users;

    @Inject
    JwtService jwtService;

    @Inject
    CurrentUser currentUser;

    @POST
    @Path("/register")
    @Transactional
    public Response register(RegisterRequest request) {
        if (request == null || request.email == null || request.password == null || request.name == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing fields").build();
        }
        if (users.findByEmail(request.email).isPresent()) {
            return Response.status(Response.Status.CONFLICT).entity("Email already registered").build();
        }
        User user = new User();
        user.setName(request.name);
        user.setEmail(request.email.toLowerCase());
        user.setPasswordHash(BCrypt.hashpw(request.password, BCrypt.gensalt()));
        users.persist(user);

        String token = jwtService.generate(user);
        return Response.ok(new AuthResponse(token, user.getName(), user.getEmail())).build();
    }

    @POST
    @Path("/login")
    @Transactional
    public Response login(AuthRequest request) {
        User user = users.findByEmail(request.email.toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (!BCrypt.checkpw(request.password, user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        String token = jwtService.generate(user);
        return Response.ok(new AuthResponse(token, user.getName(), user.getEmail())).build();
    }

    @GET
    @Path("/me")
    @RolesAllowed("user")
    public AuthResponse me() {
        User user = currentUser.get();
        return new AuthResponse(null, user.getName(), user.getEmail());
    }

    @POST
    @Path("/settings")
    @RolesAllowed("user")
    @Transactional
    public Response updateSettings(UserSettingsRequest request) {
        User user = currentUser.get();
        user.setJiraApiEmail(request.jiraApiEmail);
        user.setJiraApiToken(request.jiraApiToken);
        return Response.ok().build();
    }
}
