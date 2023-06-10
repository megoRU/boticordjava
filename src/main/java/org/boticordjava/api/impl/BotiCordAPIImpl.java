package org.boticordjava.api.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Config;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.exceptions.MeilisearchException;
import com.meilisearch.sdk.model.SearchResult;
import okhttp3.HttpUrl;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.boticordjava.api.entity.ErrorResponse;
import org.boticordjava.api.entity.ErrorResponseToMany;
import org.boticordjava.api.entity.bot.botinfo.BotInfo;
import org.boticordjava.api.entity.bot.botssearch.BotsSearch;
import org.boticordjava.api.entity.bot.stats.BotStats;
import org.boticordjava.api.entity.servers.serverinfo.ServerInfo;
import org.boticordjava.api.entity.users.profile.UserProfile;
import org.boticordjava.api.entity.users.usercommentsearch.UsersCommentSearch;
import org.boticordjava.api.io.DefaultResponseTransformer;
import org.boticordjava.api.io.ResponseTransformer;
import org.boticordjava.api.io.UnsuccessfulHttpException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BotiCordAPIImpl implements BotiCordAPI {

    private final HttpUrl baseUrl;

    private final Gson gson;
    private final String token;
    private final String searchApiKey;
    private final boolean devMode;

    protected BotiCordAPIImpl(String token, String searchApiKey, boolean devMode) {
        this.token = token;
        this.searchApiKey = searchApiKey;
        this.devMode = devMode;

        baseUrl = new HttpUrl.Builder()
                .scheme("https")
                .host("api.arbuz.pro")
//                .addPathSegment("v3")
                .build();

        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public BotInfo setBotStats(@NotNull String botId, BotStats botStats) throws UnsuccessfulHttpException {
        HttpUrl url = baseUrl.newBuilder()
                .addPathSegment("bots")
                .addPathSegment(botId)
                .addPathSegment("stats")
                .build();

        JSONObject json = new JSONObject();

        try {
            json.put("members", botStats.getMembers());
            json.put("guilds", botStats.getGuilds());
            json.put("shards", botStats.getShards());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return post(url, json, new DefaultResponseTransformer<>(gson, BotInfo.class)).getResult();
    }

    @Override
    public BotInfo getBotInfo(@NotNull String botId) throws UnsuccessfulHttpException {
        HttpUrl url = baseUrl.newBuilder()
                .addPathSegment("bots")
                .addPathSegment(botId)
                .build();
        return get(url, new DefaultResponseTransformer<>(gson, BotInfo.class)).getResult();
    }

    @Override
    public ServerInfo getServerInfo(@NotNull String serverId) throws UnsuccessfulHttpException {
        HttpUrl url = baseUrl.newBuilder()
                .addPathSegment("servers")
                .addPathSegment(serverId)
                .build();
        return get(url, new DefaultResponseTransformer<>(gson, ServerInfo.class)).getResult();
    }

    @Override
    public List<ServerInfo> searchServers(@NotNull String text) throws MeilisearchException, IllegalArgumentException, JsonProcessingException {
        if (searchApiKey == null) throw new IllegalArgumentException("SearchApiKey is NULL!");
        Client client = new Client(new Config("https://api.arbuz.pro/search/", searchApiKey));
        Index index = client.index("servers");
        SearchResult searchResult = index.search(text);
        ArrayList<HashMap<String, Object>> hits = searchResult.getHits();
        List<ServerInfo> serverInfoList = new ArrayList<>(hits.size() + 1);
        for (HashMap<String, Object> hit : hits) {
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(hit);
            ServerInfo serverInfo = gson.fromJson(json, ServerInfo.class);
            serverInfoList.add(serverInfo);
        }
        return serverInfoList;
    }

    @Override
    public List<BotsSearch> searchBots(@NotNull String text) throws MeilisearchException, IllegalArgumentException, JsonProcessingException {
        if (searchApiKey == null) throw new IllegalArgumentException("SearchApiKey is NULL!");
        Client client = new Client(new Config("https://api.arbuz.pro/search/", searchApiKey));
        Index index = client.index("bots");
        SearchResult searchResult = index.search(text);
        ArrayList<HashMap<String, Object>> hits = searchResult.getHits();
        List<BotsSearch> botsSearchList = new ArrayList<>(hits.size() + 1);
        for (HashMap<String, Object> hit : hits) {
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(hit);
            BotsSearch botsSearch = gson.fromJson(json, BotsSearch.class);
            botsSearchList.add(botsSearch);
        }
        return botsSearchList;
    }

    @Override
    public List<UsersCommentSearch> searchUserComments(@NotNull String text) throws MeilisearchException, IllegalArgumentException, JsonProcessingException {
        if (searchApiKey == null) throw new IllegalArgumentException("SearchApiKey is NULL!");
        Client client = new Client(new Config("https://api.arbuz.pro/search/", searchApiKey));
        Index index = client.index("comments");
        SearchResult searchResult = index.search(text);
        ArrayList<HashMap<String, Object>> hits = searchResult.getHits();
        List<UsersCommentSearch> usersCommentSearchesList = new ArrayList<>(hits.size() + 1);
        for (HashMap<String, Object> hit : hits) {
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(hit);
            UsersCommentSearch usersCommentSearch = gson.fromJson(json, UsersCommentSearch.class);
            usersCommentSearchesList.add(usersCommentSearch);
        }
        return usersCommentSearchesList;
    }

    @Override
    public UserProfile getUserProfile(@NotNull String userId) throws UnsuccessfulHttpException {
        HttpUrl url = baseUrl.newBuilder()
                .addPathSegment("users")
                .addPathSegment(userId)
                .build();
        return get(url, new DefaultResponseTransformer<>(gson, UserProfile.class)).getResult();
    }

    private String tokenHandler() {
        return this.token;
    }

    private <E> E get(HttpUrl url, ResponseTransformer<E> responseTransformer) throws UnsuccessfulHttpException {
        HttpGet request = new HttpGet(url.uri());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        request.addHeader(HttpHeaders.AUTHORIZATION, tokenHandler());

        return execute(request, responseTransformer);
    }

    private <E> E post(HttpUrl url, JSONObject jsonBody, ResponseTransformer<E> responseTransformer) throws UnsuccessfulHttpException {
        HttpPost request = new HttpPost(url.uri());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        request.addHeader(HttpHeaders.AUTHORIZATION, tokenHandler());

        HttpEntity stringEntity = new StringEntity(jsonBody.toString(), ContentType.APPLICATION_JSON);
        request.setEntity(stringEntity);
        return execute(request, responseTransformer);
    }

    private <E> E execute(ClassicHttpRequest request, ResponseTransformer<E> responseTransformer) throws UnsuccessfulHttpException {
        CloseableHttpClient httpClient = HttpClients
                .custom()
                .setConnectionReuseStrategy(((requests, response, context) -> false))
                .useSystemProperties()
                .build();

        try {
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getCode();
                HttpEntity entity = response.getEntity();
                String body = entity != null ? EntityUtils.toString(entity) : null;
                if (body == null) body = "{}";

                logResponse(response, body);

                switch (statusCode) {
                    case 201:
                    case 200: {
                        return responseTransformer.transform(body);
                    }
                    case 400:
                    case 401:
                    case 403:
                    case 404: {
                        ErrorResponse result = gson.fromJson(body, ErrorResponse.class);
                        throw new UnsuccessfulHttpException(result.getErrors()[0].getCode(), result.getErrors()[0].getMessage());
                    }
                    case 429: {
                        ErrorResponseToMany result = gson.fromJson(body, ErrorResponseToMany.class);
                        throw new UnsuccessfulHttpException(result.getStatusCode(), result.getMessage());
                    }
                    case 502: {
                        body = "{\n" +
                                "  \"error\": {\n" +
                                "    \"code\": 502,\n" +
                                "    \"message\": \"Bad Gateway\"\n" +
                                "  }\n" +
                                "}";
                        ErrorResponse result = gson.fromJson(body, ErrorResponse.class);
                        throw new UnsuccessfulHttpException(502, result.getErrors()[0].getMessage());
                    }
                    default:
                        ErrorResponse result = gson.fromJson(body, ErrorResponse.class);
                        throw new UnsuccessfulHttpException(result.getErrors()[0].getCode(), result.getErrors()[0].getMessage());
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        throw new RuntimeException();
    }

    private void logResponse(ClassicHttpResponse response, String body) {
        if (!devMode) return;
        String status = String.format("StatusCode: %s Reason: %s", response.getCode(), response.getReasonPhrase());
        System.out.println(status);
//        System.out.println(body);
        JsonElement jsonElement = JsonParser.parseString(body);
        String prettyJsonString = gson.toJson(jsonElement);
        System.out.println(prettyJsonString);
    }
}