package com.zxzinn.novelai.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zxzinn.novelai.config.ConfigManager;
import com.zxzinn.novelai.utils.I18nManager;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@RequiredArgsConstructor
public class APIClient implements NAIRequestHandler {
    private static final ConfigManager config = ConfigManager.getInstance();

    private HttpClient directClient;
    private HttpClient proxyClient;
    private final Gson gson;

    @Setter
    private String apiKey;

    public APIClient() {
        this.directClient = createHttpClient(false);
        this.proxyClient = createHttpClient(true);
        this.gson = new GsonBuilder().disableHtmlEscaping().create();
    }

    private HttpClient createHttpClient(boolean useProxy) {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, createTrustManager(), new java.security.SecureRandom());

            HttpClient.Builder builder = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .connectTimeout(Duration.ofSeconds(config.getInteger("api.timeout")))
                    .sslContext(sslContext)
                    .sslParameters(createSSLParameters());

            if (useProxy) {
                String proxyHost = config.getString("api.proxy.host");
                int proxyPort = config.getInteger("api.proxy.port");
                builder.proxy(ProxySelector.of(new InetSocketAddress(proxyHost, proxyPort)));
                log.info("創建使用代理的 HTTP 客戶端: {}:{}", proxyHost, proxyPort);
            } else {
                log.info("創建直接連接的 HTTP 客戶端");
            }

            return builder.build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("創建 HTTP 客戶端時發生錯誤", e);
            throw new RuntimeException("無法創建 HTTP 客戶端", e);
        }
    }


    private TrustManager[] createTrustManager() {
        return new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };
    }

    private SSLParameters createSSLParameters() {
        SSLParameters sslParams = new SSLParameters();
        sslParams.setEndpointIdentificationAlgorithm(null);
        return sslParams;
    }

    @Override
    public byte[] sendRequest(NAIGenerate request) throws IOException, InterruptedException {
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

        // 首先嘗試直接連接
        try {
            HttpResponse<byte[]> response = directClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 200) {
                log.info("使用直接連接成功");
                return response.body();
            }
            log.warn("直接連接失敗，狀態碼：{}", response.statusCode());
        } catch (IOException e) {
            log.warn("直接連接失敗，嘗試使用代理", e);
        }

        // 如果直接連接失敗，嘗試使用代理
        HttpResponse<byte[]> response = proxyClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 200) {
            String errorBody = new String(response.body());
            log.error("API 請求失敗，狀態碼：{}。錯誤訊息：{}", response.statusCode(), errorBody);
            throw new IOException("API 請求失敗，狀態碼：" + response.statusCode() +
                    "。錯誤訊息：" + errorBody);
        }
        log.info("使用代理連接成功");
        return response.body();
    }

    private String createJsonBody(NAIGenerate request) {
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

        jsonMap.put("parameters", parameters);

        return gson.toJson(jsonMap);
    }
}