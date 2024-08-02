package com.zxzinn.novelai.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zxzinn.novelai.config.ConfigManager;
import com.zxzinn.novelai.utils.I18nManager;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Log4j2
public class APIClient implements ApiService {
    private static final ConfigManager config = ConfigManager.getInstance();

    private final HttpClient directClient;
    private final HttpClient proxyClient;
    private final Gson gson;
    private String apiKey;

    public APIClient() {
        this.directClient = HttpClientFactory.createHttpClient(false);
        this.proxyClient = HttpClientFactory.createHttpClient(true);
        this.gson = new GsonBuilder().disableHtmlEscaping().create();
    }

    @Override
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public byte[] sendRequest(GenerationRequest request) throws IOException, InterruptedException {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException(I18nManager.getString("error.nullAPIKey"));
        }

        String jsonBody = createJsonBody(request);
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(config.getString("api.url")))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // First try direct connection
        try {
            HttpResponse<byte[]> response = directClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 200) {
                log.info("Direct connection successful");
                return response.body();
            }
            log.warn("Direct connection failed, status code: {}", response.statusCode());
        } catch (IOException e) {
            log.warn("Direct connection failed, trying proxy", e);
        }

        // If direct connection fails, try proxy
        HttpResponse<byte[]> response = proxyClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 200) {
            String errorBody = new String(response.body());
            log.error("API request failed, status code: {}. Error message: {}", response.statusCode(), errorBody);
            throw new IOException("API request failed, status code: " + response.statusCode() +
                    ". Error message: " + errorBody);
        }
        log.info("Proxy connection successful");
        return response.body();
    }

    private String createJsonBody(GenerationRequest request) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("input", request.getInput());
        jsonMap.put("model", request.getModel());
        jsonMap.put("action", request.getAction());

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("width", request.getWidth());
        parameters.put("height", request.getHeight());
        parameters.put("scale", request.getScale());
        parameters.put("sampler", request.getSampler());
        parameters.put("steps", request.getSteps());
        parameters.put("seed", request.getSeed());
        parameters.put("n_samples", request.getN_samples());
        parameters.put("negative_prompt", request.getNegative_prompt());

        if (request.getImage() != null) {
            parameters.put("image", request.getImage());
            parameters.put("extra_noise_seed", request.getExtra_noise_seed());
        }

        jsonMap.put("parameters", parameters);

        return gson.toJson(jsonMap);
    }
}