package com.powerapp.service;

import com.powerapp.dto.EpicProgressResponse;
import com.powerapp.model.ProjectConfig;
import com.powerapp.model.User;
import com.powerapp.repository.ProjectConfigRepository;
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
    private final Optional<String> effortSizeField;
    private final Optional<String> featureTeamField;
    private final Optional<String> parentLinkField;
    private final Optional<String> storyPointsField;
    private final ProjectConfigRepository configs;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String agileBaseUrl;

    public JiraService(
            @ConfigProperty(name = "jira.api.base-url") String baseUrl,
            @ConfigProperty(name = "jira.api.effort-field") Optional<String> effortSizeField,
            @ConfigProperty(name = "jira.api.feature-team-field") Optional<String> featureTeamField,
            @ConfigProperty(name = "jira.api.parent-link-field") Optional<String> parentLinkField,
            @ConfigProperty(name = "jira.api.story-points-field") Optional<String> storyPointsField,
            ProjectConfigRepository configs,
            ObjectMapper objectMapper) {
        this.baseUrl = baseUrl;
        this.agileBaseUrl = deriveAgileBaseUrl(baseUrl);
        this.effortSizeField = effortSizeField;
        this.featureTeamField = featureTeamField;
        this.parentLinkField = parentLinkField;
        this.storyPointsField = storyPointsField;
        this.configs = configs;
        this.objectMapper = objectMapper;
    }

    public EpicProgressResponse fetchEpicProgress(String epicKey, User user) {
        log.info("Iniciando método fetchEpicProgress(epicKey={}, userId={})", epicKey, user != null ? user.getId() : null);
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new WebApplicationException("Configure a base URL do Jira e o token do projeto para habilitar a busca no Jira", Response.Status.BAD_REQUEST);
        }
        ProjectConfig config = configs.findByOwner(user)
                .orElseThrow(() -> new WebApplicationException("Configure o projeto antes de buscar épicos", Response.Status.BAD_REQUEST));
        JiraCredentials credentials = resolveCredentials(config);

        String featureTeam = Optional.ofNullable(config.getFeatureTeam())
                .filter(v -> !v.isBlank())
                .orElseThrow(() -> new WebApplicationException("Configure a Feature Team antes de buscar épicos", Response.Status.BAD_REQUEST));

        String storyPointsFieldName = storyPointsField.filter(v -> !v.isBlank()).orElse("Story Points");

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

    /**
     * Resolve board id from Jira Agile API by exact name match.
     */
    public Long resolveBoardIdByName(String boardName, ProjectConfig config) {
        log.info("Iniciando método resolveBoardIdByName(boardName={})", boardName);
        JiraCredentials credentials = resolveCredentials(config);
        Map<String, String> query = new HashMap<>();
        query.put("name", boardName);
        JsonNode body = getAgile("/board", query, credentials);
        JsonNode values = body.path("values");
        Long foundId = null;
        for (JsonNode board : values) {
            String name = board.path("name").asText();
            if (boardName.equals(name)) {
                if (foundId != null) {
                    log.error("Erro em JiraService.resolveBoardIdByName: múltiplos boards com mesmo nome {}", boardName);
                    throw new WebApplicationException("Múltiplos boards encontrados com o mesmo nome", Response.Status.BAD_REQUEST);
                }
                foundId = board.path("id").isIntegralNumber() ? board.path("id").asLong() : null;
            }
        }
        if (foundId == null) {
            log.error("Erro em JiraService.resolveBoardIdByName: board não encontrado {}", boardName);
            throw new WebApplicationException("Board não encontrado no Jira", Response.Status.NOT_FOUND);
        }
        log.info("Finalizando método resolveBoardIdByName com boardId={}", foundId);
        return foundId;
    }

    private JsonNode fetchEpic(String epicKey, JiraCredentials credentials) {
        log.debug("Iniciando método fetchEpic(epicKey={})", epicKey);
        Map<String, String> query = new HashMap<>();
        effortSizeField.filter(v -> !v.isBlank()).ifPresent(field -> query.put(FIELDS, field));
        JsonNode response = get("/issue/" + epicKey, query, credentials);
        log.debug("Finalizando método fetchEpic");
        return response;
    }

    private JsonNode fetchIssues(String epicKey, String featureTeam, String storyPointsFieldName, JiraCredentials credentials) {
        log.debug("Iniciando método fetchIssues(epicKey={}, featureTeam={})", epicKey, featureTeam);
        String featureTeamFieldName = featureTeamField.filter(v -> !v.isBlank()).orElse("Feature Team");
        String parentLinkFieldName = parentLinkField.filter(v -> !v.isBlank()).orElse("Parent Link");
        String jql = "\"" + parentLinkFieldName + "\" = " + epicKey
                + " AND \"" + featureTeamFieldName + "\" = \"" + featureTeam + "\" "
                + "AND ((issuetype in (Story, Task, Defect) "
                + "AND (resolution NOT IN (\"Won't Do\", \"Duplicate\") OR resolution IS EMPTY)) "
                + "OR (issuetype NOT IN (Story, Task, Defect)))";

        Map<String, String> query = new HashMap<>();
        query.put("jql", jql);
        query.put(FIELDS, String.join(",", "issuetype", "status", storyPointsFieldName, featureTeamFieldName));
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
                        ? "Erro ao buscar dados no Jira: " + details
                        : "Erro ao buscar dados no Jira (400). Verifique campos customizados configurados.";
                log.error("Erro em JiraService.get: {} (uri={})", message, uri);
                throw new WebApplicationException(message, Response.Status.BAD_REQUEST);
            }
            if (statusCode == 404) {
                log.warn("Recurso não encontrado no Jira (uri={})", uri);
                throw new WebApplicationException("Epic ou issues não encontrados no Jira", Response.Status.NOT_FOUND);
            }
            if (statusCode >= 400) {
                log.error("Erro em JiraService.get: status {} body={}", statusCode, response.body());
                throw new WebApplicationException("Erro ao buscar dados no Jira (" + statusCode + ")", Response.Status.BAD_GATEWAY);
            }
            JsonNode parsed = body != null ? body : objectMapper.readTree(response.body());
            log.info("Finalizando método get(uri={}) com status {}", uri, statusCode);
            return parsed;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Erro em JiraService.get: interrupção ao chamar {}", uri, e);
            throw new WebApplicationException("Falha ao comunicar com o Jira", Response.Status.BAD_GATEWAY);
        } catch (IOException e) {
            log.error("Erro em JiraService.get: I/O ao chamar {}", uri, e);
            throw new WebApplicationException("Falha ao comunicar com o Jira", Response.Status.BAD_GATEWAY);
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
                throw new WebApplicationException("Erro ao buscar dados no Jira Agile (" + statusCode + ")", Response.Status.BAD_GATEWAY);
            }
            JsonNode parsed = body != null ? body : objectMapper.readTree(response.body());
            log.info("Finalizando método getAgile(uri={}) com status {}", uri, statusCode);
            return parsed;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Erro em JiraService.getAgile: interrupção ao chamar {}", uri, e);
            throw new WebApplicationException("Falha ao comunicar com o Jira Agile", Response.Status.BAD_GATEWAY);
        } catch (IOException e) {
            log.error("Erro em JiraService.getAgile: I/O ao chamar {}", uri, e);
            throw new WebApplicationException("Falha ao comunicar com o Jira Agile", Response.Status.BAD_GATEWAY);
        }
    }

    private JiraCredentials resolveCredentials(ProjectConfig config) {
        log.debug("Iniciando método resolveCredentials(projectConfigId={})", config != null ? config.getId() : null);
        String token = Optional.ofNullable(config.getJiraKey())
                .filter(v -> !v.isBlank())
                .orElse(null);

        if (token == null) {
            throw new WebApplicationException("Configure o token do Jira no cadastro do projeto", Response.Status.BAD_REQUEST);
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
}
