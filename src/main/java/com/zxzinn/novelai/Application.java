package com.zxzinn.novelai;

import com.zxzinn.novelai.api.NAIRequest;
import com.zxzinn.novelai.config.ConfigManager;
import com.zxzinn.novelai.gui.MainGUI;
import com.zxzinn.novelai.gui.generationwindow.ImageGenerator;
import com.zxzinn.novelai.utils.I18nManager;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.image.BufferedImage;

@Log4j2
public class Application {
    private final ConfigManager config;
    private final MainGUI mainGUI;
    private final ImageGenerator imageGenerator;

    public Application() {
        config = ConfigManager.getInstance();
        imageGenerator = new ImageGenerator();
        mainGUI = new MainGUI(imageGenerator);

        setupCallbacks();
    }

    private void setupCallbacks() {
        imageGenerator.setCallbacks(
                this::handleGeneratedImage,
                this::handleError,
                this::appendToConsole,
                this::onGenerationStopped
        );

        mainGUI.getGenerateButton().addActionListener(e -> toggleGeneration());
    }

    public void start() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                log.error("Error setting Look and Feel", e);
            }
            mainGUI.setVisible(true);
            log.info("Application started");
        });
    }

    private void toggleGeneration() {
        if (imageGenerator.isGenerating()) {
            mainGUI.getGenerateButton().setEnabled(false);
            mainGUI.getGenerateButton().setText(I18nManager.getString("button.stopping"));
            imageGenerator.requestStop();
        } else {
            mainGUI.getGenerateButton().setText(I18nManager.getString("button.stop"));
            String countSelection = (String) mainGUI.getGenerationCountComboBox().getSelectedItem();
            assert countSelection != null;
            int count = countSelection.equals(I18nManager.getString("option.infinite")) ? Integer.MAX_VALUE : Integer.parseInt(countSelection);

            NAIRequest request = mainGUI.buildRequest();
            String apiKey = mainGUI.getCurrentParametersPanel().getApiKeyField().getText();
            String outputDir = mainGUI.getCurrentParametersPanel().getOutputDirField().getText().trim();

            imageGenerator.toggleGeneration(request, apiKey, count, outputDir);
        }
    }

    private void handleGeneratedImage(BufferedImage image) {
        mainGUI.handleGeneratedImage(image);
    }

    private void handleError(String errorMessage) {
        mainGUI.handleError(errorMessage);
    }

    private void appendToConsole(String message) {
        mainGUI.appendToConsole(message);
    }

    private void onGenerationStopped() {
        SwingUtilities.invokeLater(() -> {
            mainGUI.getGenerateButton().setText(I18nManager.getString("button.generate"));
            mainGUI.getGenerateButton().setEnabled(true);
        });
    }

    public static void main(String[] args) {
        Application app = new Application();
        app.start();
    }
}