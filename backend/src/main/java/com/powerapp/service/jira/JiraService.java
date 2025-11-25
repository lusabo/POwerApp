package com.powerapp.service.jira;

import com.powerapp.dto.EpicProgressResponse;
import com.powerapp.dto.SprintJiraResponse;
import com.powerapp.service.jira.model.JiraField;
import com.powerapp.service.jira.model.JiraFieldResolver;
import com.powerapp.entity.ProjectConfig;
import com.powerapp.entity.User;
import com.powerapp.repository.ProjectConfigRepository;
import com.powerapp.util.MessageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class JiraService {
    private static final int MAX_RESULTS = 1000;
    private static final String FIELDS = "fields";
    private static final Logger log = LoggerFactory.getLogger(JiraService.class);

    private final String baseUrl;
    private final ProjectConfigRepository configs;
    private final JiraFieldResolver fieldResolver;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String agileBaseUrl;
    private final MessageService messages;

    public JiraService(
            @ConfigProperty(name = "jira.api.base-url") String baseUrl,
            ProjectConfigRepository configs,
            JiraFieldResolver fieldResolver,
            ObjectMapper objectMapper,
            MessageService messages) {
        this.baseUrl = baseUrl;
        this.agileBaseUrl = deriveAgileBaseUrl(baseUrl);
        this.configs = configs;
        this.fieldResolver = fieldResolver;
        this.objectMapper = objectMapper;
        this.messages = messages;
    }

    public SprintJiraResponse fetchSprintSummary(String sprintName, User user) {
        log.info("Iniciando método fetchSprintSummary(sprintName={}, userId={})", sprintName, user != null ? user.getId() : null);
        String normalizedName = sprintName != null ? sprintName.trim() : null;
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new WebApplicationException(messages.get("error.jira.configure.base"), Response.Status.BAD_REQUEST);
        }
        ProjectConfig config = configs.findByOwner(user)
                .orElseThrow(() -> new WebApplicationException(messages.get("error.jira.configure.project"), Response.Status.BAD_REQUEST));
        if (config.getBoard() == null || config.getBoard().isBlank()) {
            throw new WebApplicationException(messages.get("error.jira.configure.board"), Response.Status.BAD_REQUEST);
        }
        JiraCredentials credentials = resolveCredentials(config);
        Long boardId = config.getBoardId();
        if (boardId == null || boardId == 0) {
            boardId = getBoardIdExact(config.getBoard(), credentials);
        }
        SprintMeta sprintMeta = getSprintExact(boardId, normalizedName, credentials);
        if (sprintMeta == null || sprintMeta.id() == null) {
            log.warn("Sprint não encontrada no board após paginação completa para o nome {}", normalizedName);
            throw new WebApplicationException(messages.get("error.sprint.notFound"), Response.Status.NOT_FOUND);
        }
        SprintDates dates = getSprintDates(sprintMeta.id(), credentials);
        double storyPoints = getCompletedStoryPoints(config.getBoard(), normalizedName, credentials);
        SprintJiraResponse response = new SprintJiraResponse(
                sprintMeta.id(),
                normalizedName,
                dates.startDate(),
                dates.endDate(),
                dates.completeDate(),
                sprintMeta.state(),
                dates.goal(),
                storyPoints
        );
        log.info("Finalizando método fetchSprintSummary com retorno: sprintId={} storyPoints={}", sprintMeta.id(), storyPoints);
        return response;
    }

    public EpicProgressResponse fetchEpicProgress(String epicKey, User user) {
        log.info("Iniciando método fetchEpicProgress(epicKey={}, userId={})", epicKey, user != null ? user.getId() : null);
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new WebApplicationException(messages.get("error.jira.configure.base"), Response.Status.BAD_REQUEST);
        }
        ProjectConfig config = configs.findByOwner(user)
                .orElseThrow(() -> new WebApplicationException(messages.get("error.jira.configure.project.epic"), Response.Status.BAD_REQUEST));
        JiraCredentials credentials = resolveCredentials(config);

        String featureTeam = Optional.ofNullable(config.getFeatureTeam())
                .filter(v -> !v.isBlank())
                .orElseThrow(() -> new WebApplicationException(messages.get("error.jira.configure.featureTeam"), Response.Status.BAD_REQUEST));

        String storyPointsFieldName = resolveStoryPointsField();

        // Ensure the epic exists before proceeding.
        fetchEpic(epicKey, credentials);
        JsonNode issues = fetchIssues(epicKey, featureTeam, storyPointsFieldName, credentials);

        int totalIssues = issues.path("issues").size();
        int completedIssues = 0;

        for (JsonNode issue : issues.path("issues")) {
            JsonNode fields = issue.path(FIELDS);
            String statusCategory = fields.path("status").path("statusCategory").path("key").asText("");
            if ("done".equalsIgnoreCase(statusCategory)) {
                completedIssues++;
            }
        }

        double progressPercentage = totalIssues == 0 ? 0d : (completedIssues * 100d) / totalIssues;
        EpicProgressResponse response = new EpicProgressResponse(
                epicKey,
                completedIssues,
                totalIssues,
                progressPercentage
        );
        log.info("Finalizando método fetchEpicProgress com retorno: progress={}% issues={}/{}", progressPercentage, completedIssues, totalIssues);
        return response;
    }

    public EpicStats fetchEpicStats(String epicKey, User user) {
        log.info("Iniciando método fetchEpicStats(epicKey={}, userId={})", epicKey, user != null ? user.getId() : null);
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new WebApplicationException(messages.get("error.jira.configure.base"), Response.Status.BAD_REQUEST);
        }
        ProjectConfig config = configs.findByOwner(user)
                .orElseThrow(() -> new WebApplicationException(messages.get("error.jira.configure.project.epic"), Response.Status.BAD_REQUEST));
        JiraCredentials credentials = resolveCredentials(config);

        String featureTeam = Optional.ofNullable(config.getFeatureTeam())
                .filter(v -> !v.isBlank())
                .orElseThrow(() -> new WebApplicationException(messages.get("error.jira.configure.featureTeam"), Response.Status.BAD_REQUEST));

        String storyPointsFieldName = resolveStoryPointsField();
        
        JsonNode epicNode = fetchEpic(epicKey, credentials);
        JsonNode issues = fetchIssues(epicKey, featureTeam, storyPointsFieldName, credentials);

        int issuesCount = issues.path("issues").size();
        double spSum = 0d;
        for (JsonNode issue : issues.path("issues")) {
            JsonNode fields = issue.path(FIELDS);
            spSum += fields.path(storyPointsFieldName).asDouble(0d);
        }
        String effortSizeFieldName = resolveEffortSizeField();
        String effortSize = extractFieldText(epicNode.path("fields").path(effortSizeFieldName));
        String epicName = epicNode.path("fields").path("summary").asText(epicKey);
        EpicStats stats = new EpicStats(epicName, effortSize, issuesCount, spSum);
        log.info("Finalizando método fetchEpicStats: issues={} spSum={}", issuesCount, spSum);
        return stats;
    }

    /**
     * Resolve board id from Jira Agile API by exact name match.
     */
    public Long resolveBoardIdByName(String boardName, ProjectConfig config) {
        JiraCredentials credentials = resolveCredentials(config);
        return getBoardIdExact(boardName, credentials);
    }

    private JsonNode fetchEpic(String epicKey, JiraCredentials credentials) {
        log.debug("Iniciando método fetchEpic(epicKey={})", epicKey);
        Map<String, String> query = new HashMap<>();
        String effortField = resolveEffortSizeField();
        if (effortField != null && !effortField.isBlank()) {
            query.put(FIELDS, String.join(",", effortField, "summary"));
        } else {
            query.put(FIELDS, "summary");
        }
        JsonNode response = get("/issue/" + epicKey, query, credentials);
        log.debug("Finalizando método fetchEpic");
        return response;
    }

    public Long getBoardIdExact(String boardName, JiraCredentials credentials) {
        log.info("Iniciando método getBoardIdExact(boardName={})", boardName);
        Map<String, String> query = new HashMap<>();
        query.put("name", boardName);
        JsonNode body = getAgile("/board", query, credentials);
        JsonNode values = body.path("values");
        Long foundId = null;
        for (JsonNode board : values) {
            String name = board.path("name").asText();
            if (boardName.equals(name)) {
                if (foundId != null) {
                    log.error("Erro em getBoardIdExact: múltiplos boards com nome {}", boardName);
                    throw new WebApplicationException(messages.get("error.jira.multipleBoards"), Response.Status.BAD_REQUEST);
                }
                foundId = board.path("id").isIntegralNumber() ? board.path("id").asLong() : null;
            }
        }
        if (foundId == null) {
            throw new WebApplicationException(messages.get("error.jira.boardNotFound"), Response.Status.NOT_FOUND);
        }
        log.info("Finalizando método getBoardIdExact com boardId={}", foundId);
        return foundId;
    }

    public SprintMeta getSprintExact(Long boardId, String sprintName, JiraCredentials credentials) {
        log.info("Iniciando método getSprintExact(boardId={}, sprintName={})", boardId, sprintName);
        long startAt = 0;
        while (true) {
            Map<String, String> query = new HashMap<>();
            // Buscar sprints em qualquer estado relevante
            query.put("state", "active, future, closed");
            query.put("startAt", String.valueOf(startAt));
            JsonNode body = getAgile("/board/" + boardId + "/sprint", query, credentials);
            JsonNode values = body.path("values");
            for (JsonNode sprint : values) {
                String name = sprint.path("name").asText();
                if (sprintName != null && sprintName.trim().equals(name.trim())) {
                    Long sprintId = sprint.path("id").isIntegralNumber() ? sprint.path("id").asLong() : null;
                    String state = sprint.path("state").asText(null);
                    log.info("Finalizando método getSprintExact com sprintId={} state={}", sprintId, state);
                    return new SprintMeta(sprintId, state);
                }
            }
            boolean isLast = body.path("isLast").asBoolean(true);
            int maxResults = body.path("maxResults").asInt(0);
            if (isLast) {
                log.warn("Sprint não encontrada no board após paginação completa");
                return null;
            }
            startAt += maxResults;
        }
    }

    public SprintDates getSprintDates(Long sprintId, JiraCredentials credentials) {
        log.info("Iniciando método getSprintDates(sprintId={})", sprintId);
        JsonNode body = getAgile("/sprint/" + sprintId, Map.of(), credentials);
        String start = body.path("startDate").asText(null);
        String end = body.path("endDate").asText(null);
        String complete = body.path("completeDate").asText(null);
        String goal = body.path("goal").asText(null);
        log.info("Finalizando método getSprintDates com start={} end={} complete={} goal={}", start, end, complete, goal);
        return new SprintDates(start, end, complete, goal);
    }

    public double getCompletedStoryPoints(String boardName, String sprintName, JiraCredentials credentials) {
        log.info("Iniciando método getCompletedStoryPoints(boardName={}, sprintName={})", boardName, sprintName);
        String jql = "issueFunction in completeInSprint(\"" + boardName + "\", \"" + sprintName + "\")";
        String query = baseUrl + "/search?jql=" + URLEncoder.encode(jql, StandardCharsets.UTF_8)
                + "&fields=" + resolveStoryPointsField() + ",issuetype&maxResults=" + MAX_RESULTS;
        JsonNode body = httpGet(query, credentials);
        double total = 0d;
        for (JsonNode issue : body.path("issues")) {
            JsonNode fields = issue.path("fields");
            double sp = fields.path(resolveStoryPointsField()).asDouble(0d);
            total += sp;
        }
        log.info("Finalizando método getCompletedStoryPoints com total={}", total);
        return total;
    }

    private JsonNode fetchIssues(String epicKey, String featureTeam, String storyPointsFieldName, JiraCredentials credentials) {
        log.debug("Iniciando método fetchIssues(epicKey={}, featureTeam={})", epicKey, featureTeam);
        String featureTeamFieldName = fieldResolver.getFriendlyName(JiraField.FEATURE_TEAM);
        String parentLinkFieldName = fieldResolver.getFriendlyName(JiraField.PARENT_LINK);
        String jql = "\"" + parentLinkFieldName + "\" = " + epicKey
                + " AND \"" + featureTeamFieldName + "\" = \"" + featureTeam + "\" "
                + "AND ((issuetype in (Story, Task, Defect) "
                + "AND (resolution NOT IN (\"Won't Do\", \"Duplicate\") OR resolution IS EMPTY)) "
                + "OR (issuetype NOT IN (Story, Task, Defect)))";

        String featureTeamFieldId = fieldResolver.getId(JiraField.FEATURE_TEAM);
        Map<String, String> query = new HashMap<>();
        query.put("jql", jql);
        query.put(FIELDS, String.join(",", "issuetype", "status", storyPointsFieldName, featureTeamFieldId));
        query.put("maxResults", String.valueOf(MAX_RESULTS));
        JsonNode response = get("/search", query, credentials);
        log.debug("Finalizando método fetchIssues");
        return response;
    }

    private JsonNode get(String path, Map<String, String> queryParams, JiraCredentials credentials) {
        String encodedQuery = buildQueryString(queryParams);
        URI uri = URI.create(baseUrl + path + encodedQuery);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .header("Authorization", credentials.authorizationHeader())
                .GET();

        try {
            log.info("Iniciando método get(uri={})", uri);
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            JsonNode body = safeParse(response.body());

            if (statusCode == 400) {
                String details = summarizeErrorMessages(body);
                String message = details != null
                        ? messages.get("error.jira.apiError", details)
                        : messages.get("error.jira.apiError", "400. Verifique campos customizados configurados.");
                log.error("Erro em JiraService.get: {} (uri={})", message, uri);
                throw new WebApplicationException(message, Response.Status.BAD_REQUEST);
            }
            if (statusCode == 404) {
                log.warn("Recurso não encontrado no Jira (uri={})", uri);
                throw new WebApplicationException(messages.get("error.jira.epicNotFound"), Response.Status.NOT_FOUND);
            }
            if (statusCode >= 400) {
                log.error("Erro em JiraService.get: status {} body={}", statusCode, response.body());
                throw new WebApplicationException(messages.get("error.jira.apiError", statusCode), Response.Status.BAD_GATEWAY);
            }
            JsonNode parsed = body != null ? body : objectMapper.readTree(response.body());
            log.info("Finalizando método get(uri={}) com status {}", uri, statusCode);
            return parsed;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Erro em JiraService.get: interrupção ao chamar {}", uri, e);
            throw new WebApplicationException(messages.get("error.jira.communication"), Response.Status.BAD_GATEWAY);
        } catch (IOException e) {
            log.error("Erro em JiraService.get: I/O ao chamar {}", uri, e);
            throw new WebApplicationException(messages.get("error.jira.communication"), Response.Status.BAD_GATEWAY);
        }
    }

    private JsonNode getAgile(String path, Map<String, String> queryParams, JiraCredentials credentials) {
        String encodedQuery = buildQueryString(queryParams);
        URI uri = URI.create(agileBaseUrl + path + encodedQuery);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .header("Authorization", credentials.authorizationHeader())
                .GET();
        try {
            log.info("Iniciando método getAgile(uri={})", uri);
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            JsonNode body = safeParse(response.body());
            if (statusCode >= 400) {
                log.error("Erro em JiraService.getAgile: status {} body={}", statusCode, response.body());
                throw new WebApplicationException(messages.get("error.jira.agileApiError", statusCode), Response.Status.BAD_GATEWAY);
            }
            JsonNode parsed = body != null ? body : objectMapper.readTree(response.body());
            log.info("Finalizando método getAgile(uri={}) com status {}", uri, statusCode);
            return parsed;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Erro em JiraService.getAgile: interrupção ao chamar {}", uri, e);
            throw new WebApplicationException(messages.get("error.jira.agileCommunication"), Response.Status.BAD_GATEWAY);
        } catch (IOException e) {
            log.error("Erro em JiraService.getAgile: I/O ao chamar {}", uri, e);
            throw new WebApplicationException(messages.get("error.jira.agileCommunication"), Response.Status.BAD_GATEWAY);
        }
    }

    private JsonNode httpGet(String url, JiraCredentials credentials) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("Authorization", credentials.authorizationHeader())
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status >= 400) {
                log.error("Erro em httpGet: status {} body={}", status, response.body());
                throw new WebApplicationException(messages.get("error.jira.queryError", status), Response.Status.BAD_GATEWAY);
            }
            return objectMapper.readTree(response.body());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.error("Erro em httpGet para url {}", url, e);
            throw new WebApplicationException(messages.get("error.jira.queryCommunication"), Response.Status.BAD_GATEWAY);
        }
    }

    private JiraCredentials resolveCredentials(ProjectConfig config) {
        log.debug("Iniciando método resolveCredentials(projectConfigId={})", config != null ? config.getId() : null);
        String token = Optional.ofNullable(config.getJiraKey())
                .filter(v -> !v.isBlank())
                .orElse(null);

        if (token == null) {
            throw new WebApplicationException(messages.get("error.jira.tokenMissing"), Response.Status.BAD_REQUEST);
        }
        String header = "Bearer " + token;
        log.debug("Finalizando método resolveCredentials");
        return new JiraCredentials(header);
    }

    private String deriveAgileBaseUrl(String apiBaseUrl) {
        if (apiBaseUrl == null) {
            return "/rest/agile/1.0";
        }
        String withoutApi = apiBaseUrl.replaceFirst("/rest/api/2/?$", "");
        if (withoutApi.equals(apiBaseUrl)) {
            return apiBaseUrl + "/rest/agile/1.0";
        }
        return withoutApi + "/rest/agile/1.0";
    }

    private String buildQueryString(Map<String, String> params) {
        if (params.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder("?");
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                builder.append("&");
            }
            first = false;
            builder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return builder.toString();
    }

    private JsonNode safeParse(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(body);
        } catch (IOException e) {
            return null;
        }
    }

    private String summarizeErrorMessages(JsonNode body) {
        if (body == null) {
            return null;
        }
        JsonNode errors = body.get("errorMessages");
        if (errors instanceof ArrayNode array && array.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < array.size(); i++) {
                if (i > 0) {
                    sb.append("; ");
                }
                sb.append(array.get(i).asText());
            }
            return sb.toString();
        }
        return null;
    }

    private record JiraCredentials(String authorizationHeader) {
    }

    private record SprintDates(String startDate, String endDate, String completeDate, String goal) {
    }

    public record EpicStats(String epicName, String effortSize, int issuesCount, double storyPointsSum) {
    }

    public record SprintMeta(Long id, String state) {
    }

    private String resolveStoryPointsField() {
        String id = fieldResolver.getId(JiraField.STORY_POINTS);
        if (id == null || id.isBlank()) {
            throw new WebApplicationException(messages.get("error.jira.storyPointsField"), Response.Status.BAD_REQUEST);
        }
        return id;
    }

    public boolean isSprintClosed(Long sprintId, User user) {
        if (sprintId == null) {
            return false;
        }
        JiraCredentials credentials = resolveCredentials(
                configs.findByOwner(user)
                        .orElseThrow(() -> new WebApplicationException(messages.get("error.jira.configure.project"), Response.Status.BAD_REQUEST))
        );
        JsonNode node = getAgile("/sprint/" + sprintId, Map.of(), credentials);
        String state = node.path("state").asText("");
        return "closed".equalsIgnoreCase(state);
    }

    private String resolveEffortSizeField() {
        String id = fieldResolver.getId(JiraField.EFFORT);
        if (id == null || id.isBlank()) {
            throw new WebApplicationException(messages.get("error.jira.effortField"), Response.Status.BAD_REQUEST);
        }
        return id;
    }

    private String extractFieldText(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        if (node.has("value")) {
            return node.path("value").asText(null);
        }
        return node.asText(null);
    }
}
