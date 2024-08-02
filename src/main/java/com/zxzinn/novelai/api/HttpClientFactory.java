package com.zxzinn.novelai.api;

import com.zxzinn.novelai.config.ConfigManager;
import lombok.extern.log4j.Log4j2;

import javax.net.ssl.*;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.Duration;

@Log4j2
public class HttpClientFactory {
    private static final ConfigManager config = ConfigManager.getInstance();

    public static HttpClient createHttpClient(boolean useProxy) {
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
                log.info("Creating HTTP client with proxy: {}:{}", proxyHost, proxyPort);
            } else {
                log.info("Creating direct HTTP client");
            }

            return builder.build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("Error creating HTTP client", e);
            throw new RuntimeException("Unable to create HTTP client", e);
        }
    }

    private static TrustManager[] createTrustManager() {
        return new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };
    }

    private static SSLParameters createSSLParameters() {
        SSLParameters sslParams = new SSLParameters();
        sslParams.setEndpointIdentificationAlgorithm(null);
        return sslParams;
    }
}