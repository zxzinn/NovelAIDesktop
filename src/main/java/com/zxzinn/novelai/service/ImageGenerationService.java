package com.zxzinn.novelai.service;

import com.zxzinn.novelai.api.ApiService;
import com.zxzinn.novelai.api.APIClient;
import com.zxzinn.novelai.api.NAIRequest;
import com.zxzinn.novelai.utils.ImageProcessor;
import lombok.extern.log4j.Log4j2;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Log4j2
public class ImageGenerationService {
    private final ApiService apiService;
    private final ImageProcessor imageProcessor;
    private final ExecutorService executorService;

    public ImageGenerationService() {
        this.apiService = new APIClient();
        this.imageProcessor = new ImageProcessor();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public CompletableFuture<BufferedImage> generateImage(NAIRequest request, String apiKey) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                apiService.setApiKey(apiKey);
                byte[] response = apiService.sendRequest(request);
                return imageProcessor.extractImageFromZip(response);
            } catch (IOException | InterruptedException e) {
                log.error("Error generating image: {}", e.getMessage());
                throw new RuntimeException("Image generation failed", e);
            }
        }, executorService);
    }

    public void saveImage(BufferedImage image, String outputDir, Consumer<File> onSaved) {
        CompletableFuture.runAsync(() -> {
            try {
                File savedFile = imageProcessor.saveImage(image, outputDir);
                onSaved.accept(savedFile);
            } catch (IOException e) {
                log.error("Error saving image: {}", e.getMessage());
            }
        }, executorService);
    }

    public void shutdown() {
        executorService.shutdownNow();
    }
}