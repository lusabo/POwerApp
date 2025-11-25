package com.powerapp.controller;

import com.powerapp.dto.EpicProgressResponse;
import com.powerapp.dto.EpicRequest;
import com.powerapp.dto.EpicResponse;
import com.powerapp.entity.DomainCycle;
import com.powerapp.entity.User;
import com.powerapp.repository.DomainCycleRepository;
import com.powerapp.repository.EpicRepository;
import com.powerapp.config.CurrentUser;
import com.powerapp.service.jira.JiraService;
import com.powerapp.util.MessageService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/epics")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class EpicController {
    private static final Logger log = LoggerFactory.getLogger(EpicController.class);

    private final CurrentUser currentUser;
    private final JiraService jiraService;
    private final EpicRepository epics;
    private final DomainCycleRepository domainCycles;
    private final MessageService messages;

    public EpicController(CurrentUser currentUser, JiraService jiraService, EpicRepository epics, DomainCycleRepository domainCycles, MessageService messages) {
        this.currentUser = currentUser;
        this.jiraService = jiraService;
        this.epics = epics;
        this.domainCycles = domainCycles;
        this.messages = messages;
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

    @GET
    public List<EpicResponse> list(@QueryParam("domainCycleId") Long domainCycleId) {
        User user = currentUser.get();
        return epics.findByOwnerAndDomainCycle(user, domainCycleId).stream()
                .map(EpicResponse::fromEntity)
                .toList();
    }

    @POST
    @Transactional
    public Response save(EpicRequest request) {
        if (request == null || request.epicKey == null || request.epicKey.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(messages.get("error.epic.keyRequired")).build();
        }
        User user = currentUser.get();
        DomainCycle dc = null;
        if (request.domainCycleId != null) {
            dc = domainCycles.findById(request.domainCycleId);
            if (dc == null || !dc.getOwner().getId().equals(user.getId())) {
                return Response.status(Response.Status.BAD_REQUEST).entity(messages.get("error.epic.domainCycleInvalid")).build();
            }
        }
        JiraService.EpicStats stats = jiraService.fetchEpicStats(request.epicKey, user);

        var epic = epics.findByKeyAndOwner(request.epicKey, user).orElseGet(com.powerapp.entity.Epic::new);
        epic.setOwner(user);
        epic.setEpicKey(request.epicKey);
        epic.setName(stats.epicName());
        epic.setEffortSize(stats.effortSize());
        epic.setIssuesCount(stats.issuesCount());
        epic.setStoryPointsSum(BigDecimal.valueOf(stats.storyPointsSum()));
        epic.setDomainCycle(dc);
        if (epic.getId() == null) {
            epics.persist(epic);
        }
        return Response.ok(EpicResponse.fromEntity(epic)).build();
    }

    @POST
    @Path("/reload")
    @Transactional
    public Response reload() {
        User user = currentUser.get();
        List<com.powerapp.entity.Epic> all = epics.findByOwner(user);
        for (com.powerapp.entity.Epic epic : all) {
            try {
                JiraService.EpicStats stats = jiraService.fetchEpicStats(epic.getEpicKey(), user);
                epic.setName(stats.epicName());
                epic.setEffortSize(stats.effortSize());
                epic.setIssuesCount(stats.issuesCount());
                epic.setStoryPointsSum(BigDecimal.valueOf(stats.storyPointsSum()));
            } catch (WebApplicationException e) {
                log.error("Falha ao atualizar épico {}: {}", epic.getEpicKey(), e.getMessage());
            }
        }
        return Response.ok(all.stream().map(EpicResponse::fromEntity).toList()).build();
    }
}
