package com.zxzinn.novelai.gui.generationwindow;

import com.zxzinn.novelai.api.APIClient;
import com.zxzinn.novelai.api.NAIGenerate;
import com.zxzinn.novelai.api.NAIResponseHandler;
import com.zxzinn.novelai.utils.ImageUtils;
import com.zxzinn.novelai.utils.I18nManager;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Log4j2
public class ImageGenerator implements NAIResponseHandler {
    private final APIClient apiClient;
    private final ImageUtils imageUtils;
    private final AtomicBoolean isGenerating;
    private final AtomicBoolean stopRequested;
    private final ExecutorService executorService;

    private Consumer<BufferedImage> imageConsumer;
    private Consumer<String> errorConsumer;
    private Consumer<String> logConsumer;
    private Runnable onGenerationStopped;

    public ImageGenerator() {
        apiClient = new APIClient();
        imageUtils = new ImageUtils();
        isGenerating = new AtomicBoolean(false);
        stopRequested = new AtomicBoolean(false);
        executorService = Executors.newSingleThreadExecutor();
    }

    public void setCallbacks(Consumer<BufferedImage> imageConsumer, Consumer<String> errorConsumer,
                             Consumer<String> logConsumer, Runnable onGenerationStopped) {
        this.imageConsumer = imageConsumer;
        this.errorConsumer = errorConsumer;
        this.logConsumer = logConsumer;
        this.onGenerationStopped = onGenerationStopped;
    }

    public void toggleGeneration(NAIGenerate request, String apiKey, int count, String outputDir) {
        if (isGenerating.get()) {
            requestStop();
        } else {
            startGeneration(request, apiKey, count, outputDir);
        }
    }

    private void startGeneration(NAIGenerate request, String apiKey, int count, String outputDir) {
        if (isGenerating.get()) {
            return; // 防止重複啟動
        }
        isGenerating.set(true);
        stopRequested.set(false);
        apiClient.setApiKey(apiKey);

        CompletableFuture.runAsync(() -> {
            for (int i = 0; i < count && !stopRequested.get(); i++) {
                final int currentIteration = i + 1;
                logMessage(I18nManager.getString("log.generationStarted", currentIteration));
                try {
                    log.info("發送請求: {}", request);
                    byte[] response = apiClient.sendRequest(request);
                    handleResponse(response, outputDir);

                    logMessage(I18nManager.getString("log.generationCompleted", currentIteration,
                            count == Integer.MAX_VALUE ? I18nManager.getString("option.infinite") : count));

                    if (stopRequested.get()) {
                        logMessage(I18nManager.getString("log.stoppingAfterCurrentIteration"));
                        break;
                    }
                } catch (IOException | InterruptedException e) {
                    handleError(I18nManager.getString("error.apiRequest", e.getMessage()));
                    break;
                }
            }
        }, executorService).whenComplete((result, exception) -> {
            completeGeneration();
        });
    }

    public void requestStop() {
        if (isGenerating.get()) {
            stopRequested.set(true);
            logMessage(I18nManager.getString("log.stopRequested"));
        }
    }

    private void completeGeneration() {
        isGenerating.set(false);
        stopRequested.set(false);
        SwingUtilities.invokeLater(() -> {
            logMessage(I18nManager.getString("log.generationStopped"));
            if (onGenerationStopped != null) {
                onGenerationStopped.run();
            }
        });
    }

    @Override
    public void handleResponse(byte[] response) {
        handleResponse(response, null);
    }

    private void handleResponse(byte[] response, String outputDir) {
        try {
            BufferedImage image = imageUtils.getImageFromZip(response);
            SwingUtilities.invokeLater(() -> {
                if (imageConsumer != null) {
                    imageConsumer.accept(image);
                }
            });

            if (outputDir != null && !outputDir.isEmpty()) {
                File outputFile = imageUtils.saveImage(image, outputDir);
                logMessage(I18nManager.getString("log.imageSaved", outputFile.getAbsolutePath()));
            }
        } catch (IOException e) {
            handleError("處理響應時出錯: " + e.getMessage());
        }
    }

    @Override
    public void handleError(String errorMessage) {
        log.error("錯誤: {}", errorMessage);
        SwingUtilities.invokeLater(() -> {
            if (errorConsumer != null) {
                errorConsumer.accept(errorMessage);
            }
        });
    }

    private void logMessage(String message) {
        log.info(message);
        SwingUtilities.invokeLater(() -> {
            if (logConsumer != null) {
                logConsumer.accept(message);
            }
        });
    }

    public boolean isGenerating() {
        return isGenerating.get();
    }

}