package com.zxzinn.novelai.controller;

import com.zxzinn.novelai.api.GenerationRequest;
import com.zxzinn.novelai.gui.MainGUI;
import com.zxzinn.novelai.gui.generationwindow.ImageGenerator;
import lombok.extern.log4j.Log4j2;

import java.awt.image.BufferedImage;
import java.util.function.Consumer;

@Log4j2
public class MainController {
    private final MainGUI mainGUI;
    private final ImageGenerator imageGenerator;

    public MainController(MainGUI mainGUI, ImageGenerator imageGenerator) {
        this.mainGUI = mainGUI;
        this.imageGenerator = imageGenerator;
        setupCallbacks();
    }

    private void setupCallbacks() {
        imageGenerator.setCallbacks(
                this::handleGeneratedImage,
                this::handleError,
                this::appendToConsole,
                this::onGenerationStopped
        );
    }

    public void generateImage(GenerationRequest request, String apiKey, String outputDir, Consumer<BufferedImage> onImageGenerated) {
        imageGenerator.generateSingleImage(request, apiKey, outputDir, image -> {
            onImageGenerated.accept(image);
            mainGUI.updateGenerationControlPanel(false);
        });
    }

    private void handleGeneratedImage(BufferedImage image) {
        mainGUI.handleGeneratedImage(image);
    }

    private void handleError(String errorMessage) {
        mainGUI.handleError(errorMessage);
        mainGUI.updateGenerationControlPanel(false);
    }

    private void appendToConsole(String message) {
        mainGUI.appendToConsole(message);
    }

    private void onGenerationStopped() {
        mainGUI.updateGenerationControlPanel(false);
    }
}