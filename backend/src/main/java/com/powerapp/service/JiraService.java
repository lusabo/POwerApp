package com.powerapp.service;

import com.powerapp.dto.EpicProgressResponse;
import com.powerapp.model.ProjectConfig;
import com.powerapp.model.User;
import com.powerapp.repository.ProjectConfigRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class JiraService {
    private static final int MAX_RESULTS = 1000;

    @ConfigProperty(name = "jira.api.base-url")
    String baseUrl;

    @ConfigProperty(name = "jira.api.token")
    Optional<String> defaultToken;

    @ConfigProperty(name = "jira.api.email")
    Optional<String> defaultEmail;

    @ConfigProperty(name = "jira.api.effort-field")
    Optional<String> effortSizeField;

    @ConfigProperty(name = "jira.api.feature-team-field")
    Optional<String> featureTeamField;

    @ConfigProperty(name = "jira.api.parent-link-field")
    Optional<String> parentLinkField;

    @ConfigProperty(name = "jira.api.story-points-field")
    Optional<String> storyPointsField;

    @Inject
    ProjectConfigRepository configs;

    @Inject
    ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public EpicProgressResponse fetchEpicProgress(String epicKey, User user) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new WebApplicationException("Configure o JIRA_CLOUD_ID/JIRA_API_KEY/JIRA_API_EMAIL para habilitar a busca no Jira", Response.Status.BAD_REQUEST);
        }
        JiraCredentials credentials = resolveCredentials(user);
        ProjectConfig config = configs.findByOwner(user)
                .orElseThrow(() -> new WebApplicationException("Configure o projeto antes de buscar épicos", Response.Status.BAD_REQUEST));

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
            JsonNode fields = issue.path("fields");
            String statusCategory = fields.path("status").path("statusCategory").path("key").asText("");
            if ("done".equalsIgnoreCase(statusCategory)) {
                completedIssues++;
            }
        }

        double progressPercentage = totalIssues == 0 ? 0d : (completedIssues * 100d) / totalIssues;
        return new EpicProgressResponse(
                epicKey,
                completedIssues,
                totalIssues,
                progressPercentage,
                credentials.source()
        );
    }

    private JsonNode fetchEpic(String epicKey, JiraCredentials credentials) {
        Map<String, String> query = new HashMap<>();
        effortSizeField.filter(v -> !v.isBlank()).ifPresent(field -> query.put("fields", field));
        return get("/issue/" + epicKey, query, credentials);
    }

    private JsonNode fetchIssues(String epicKey, String featureTeam, String storyPointsFieldName, JiraCredentials credentials) {
        String featureTeamFieldName = featureTeamField.filter(v -> !v.isBlank()).orElse("Feature Team");
        String parentLinkFieldName = parentLinkField.filter(v -> !v.isBlank()).orElse("Parent Link");
        String jql = "\"" + parentLinkFieldName + "\" = " + epicKey
                + " AND \"" + featureTeamFieldName + "\" = \"" + featureTeam + "\" "
                + "AND ((issuetype in (Story, Task, Defect) "
                + "AND (resolution NOT IN (\"Won't Do\", \"Duplicate\") OR resolution IS EMPTY)) "
                + "OR (issuetype NOT IN (Story, Task, Defect)))";

        Map<String, String> query = new HashMap<>();
        query.put("jql", jql);
        query.put("fields", String.join(",", "issuetype", "status", storyPointsFieldName, featureTeamFieldName));
        query.put("maxResults", String.valueOf(MAX_RESULTS));
        return get("/search", query, credentials);
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
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            if (statusCode == 404) {
                throw new WebApplicationException("Epic ou issues não encontrados no Jira", Response.Status.NOT_FOUND);
            }
            if (statusCode >= 400) {
                throw new WebApplicationException("Erro ao buscar dados no Jira (" + statusCode + ")", Response.Status.BAD_GATEWAY);
            }
            return objectMapper.readTree(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WebApplicationException("Falha ao comunicar com o Jira", Response.Status.BAD_GATEWAY);
        } catch (IOException e) {
            throw new WebApplicationException("Falha ao comunicar com o Jira", Response.Status.BAD_GATEWAY);
        }
    }

    private JiraCredentials resolveCredentials(User user) {
        String email = Optional.ofNullable(user.getJiraApiEmail())
                .filter(v -> !v.isBlank())
                .orElseGet(() -> defaultEmail.orElse(null));
        String token = Optional.ofNullable(user.getJiraApiToken())
                .filter(v -> !v.isBlank())
                .orElseGet(() -> defaultToken.orElse(null));

        if (email == null || token == null) {
            throw new WebApplicationException("Configure o e-mail e token do Jira nas configurações do usuário", Response.Status.BAD_REQUEST);
        }
        String header = "Basic " + Base64.getEncoder().encodeToString((email + ":" + token).getBytes(StandardCharsets.UTF_8));
        String source = user.getJiraApiEmail() != null ? "jira:user" : "jira:env";
        return new JiraCredentials(header, source);
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

    private record JiraCredentials(String authorizationHeader, String source) {
    }
}
