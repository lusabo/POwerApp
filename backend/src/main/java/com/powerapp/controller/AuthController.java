package com.powerapp.controller;

import com.powerapp.dto.AuthRequest;
import com.powerapp.dto.AuthResponse;
import com.powerapp.dto.RegisterRequest;
import com.powerapp.dto.UserSettingsRequest;
import com.powerapp.entity.User;
import com.powerapp.repository.UserRepository;
import com.powerapp.config.CurrentUser;
import com.powerapp.config.JwtService;
import io.quarkus.security.UnauthorizedException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository users;
    private final JwtService jwtService;
    private final CurrentUser currentUser;

    public AuthController(UserRepository users, JwtService jwtService, CurrentUser currentUser) {
        this.users = users;
        this.jwtService = jwtService;
        this.currentUser = currentUser;
    }

    @POST
    @Path("/register")
    @Transactional
    public Response register(RegisterRequest request) {
        log.info("Iniciando método register(request)");
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
        Response response = Response.ok(new AuthResponse(token, user.getName(), user.getEmail())).build();
        log.info("Finalizando método register com retorno: 201 para email {}", user.getEmail());
        return response;
    }

    @POST
    @Path("/login")
    @Transactional
    public Response login(AuthRequest request) {
        log.info("Iniciando método login(request.email)");
        User user = users.findByEmail(request.email.toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (!BCrypt.checkpw(request.password, user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        String token = jwtService.generate(user);
        Response response = Response.ok(new AuthResponse(token, user.getName(), user.getEmail())).build();
        log.info("Finalizando método login com retorno: 200 para email {}", user.getEmail());
        return response;
    }

    @GET
    @Path("/me")
    @RolesAllowed("user")
    public AuthResponse me() {
        log.info("Iniciando método me()");
        User user = currentUser.get();
        AuthResponse response = new AuthResponse(null, user.getName(), user.getEmail());
        log.info("Finalizando método me com retorno: usuário {}", user.getEmail());
        return response;
    }

    @POST
    @Path("/settings")
    @RolesAllowed("user")
    @Transactional
    public Response updateSettings(UserSettingsRequest request) {
        log.info("Iniciando método updateSettings(request)");
        User user = currentUser.get();
        user.setJiraApiEmail(request.jiraApiEmail);
        user.setJiraApiToken(request.jiraApiToken);
        Response response = Response.ok().build();
        log.info("Finalizando método updateSettings com retorno: 200");
        return response;
    }
}
