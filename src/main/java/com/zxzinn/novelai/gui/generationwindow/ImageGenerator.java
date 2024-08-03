package com.zxzinn.novelai.gui.generationwindow;

import com.zxzinn.novelai.api.GenerationRequest;
import com.zxzinn.novelai.service.ImageGenerationService;
import com.zxzinn.novelai.utils.I18nManager;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Log4j2
public class ImageGenerator {
    private final ImageGenerationService generationService;
    private final AtomicBoolean isGenerating;
    private final AtomicBoolean stopRequested;

    private Consumer<BufferedImage> imageConsumer;
    private Consumer<String> errorConsumer;
    private Consumer<String> logConsumer;
    private Runnable onGenerationStopped;

    public ImageGenerator() {
        this.generationService = new ImageGenerationService();
        this.isGenerating = new AtomicBoolean(false);
        this.stopRequested = new AtomicBoolean(false);
    }

    public void setCallbacks(Consumer<BufferedImage> imageConsumer, Consumer<String> errorConsumer,
                             Consumer<String> logConsumer, Runnable onGenerationStopped) {
        this.imageConsumer = imageConsumer;
        this.errorConsumer = errorConsumer;
        this.logConsumer = logConsumer;
        this.onGenerationStopped = onGenerationStopped;
    }

    public void toggleGeneration(GenerationRequest request, String apiKey, int count, String outputDir) {
        if (isGenerating.get()) {
            requestStop();
        } else {
            startGeneration(request, apiKey, count, outputDir);
        }
    }

    public void generateSingleImage(GenerationRequest request, String apiKey, String outputDir, Consumer<BufferedImage> onImageGenerated) {
        CompletableFuture.runAsync(() -> {
            try {
                BufferedImage image = generationService.generateImage(request, apiKey).join();
                handleGeneratedImage(image, outputDir);
                SwingUtilities.invokeLater(() -> onImageGenerated.accept(image));
            } catch (Exception e) {
                handleError(I18nManager.getString("error.apiRequest", e.getMessage()));
            }
        });
    }

    private void startGeneration(GenerationRequest request, String apiKey, int count, String outputDir) {
        if (isGenerating.get()) {
            return;
        }
        isGenerating.set(true);
        stopRequested.set(false);

        CompletableFuture.runAsync(() -> {
            for (int i = 0; i < count && !stopRequested.get(); i++) {
                final int currentIteration = i + 1;
                logMessage(I18nManager.getString("log.generationStarted", currentIteration));

                try {
                    BufferedImage image = generationService.generateImage(request, apiKey).join();
                    handleGeneratedImage(image, outputDir);
                    logMessage(I18nManager.getString("log.generationCompleted", currentIteration,
                            count == Integer.MAX_VALUE ? I18nManager.getString("option.infinite") : count));

                    if (stopRequested.get()) {
                        logMessage(I18nManager.getString("log.stoppingAfterCurrentIteration"));
                        break;
                    }
                } catch (Exception e) {
                    handleError(I18nManager.getString("error.apiRequest", e.getMessage()));
                    break;
                }
            }
        }).whenComplete((result, exception) -> completeGeneration());
    }

    private void handleGeneratedImage(BufferedImage image, String outputDir) {
        SwingUtilities.invokeLater(() -> {
            if (imageConsumer != null) {
                imageConsumer.accept(image);
            }
        });

        if (outputDir != null && !outputDir.isEmpty()) {
            generationService.saveImage(image, outputDir, this::logImageSaved);
        }
    }

    private void logImageSaved(File file) {
        logMessage(I18nManager.getString("log.imageSaved", file.getAbsolutePath()));
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

    public void handleError(String errorMessage) {
        log.error("Error: {}", errorMessage);
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