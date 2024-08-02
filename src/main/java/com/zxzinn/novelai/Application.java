package com.zxzinn.novelai;

import com.zxzinn.novelai.config.ConfigManager;
import com.zxzinn.novelai.controller.MainController;
import com.zxzinn.novelai.gui.MainGUI;
import com.zxzinn.novelai.gui.generationwindow.ImageGenerator;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;

@Log4j2
public class Application {
    private final ConfigManager config;
    private final MainGUI mainGUI;
    private final ImageGenerator imageGenerator;
    private final MainController mainController;

    public Application() {
        config = ConfigManager.getInstance();
        imageGenerator = new ImageGenerator();
        mainGUI = new MainGUI();
        mainController = new MainController(mainGUI, imageGenerator);
        mainGUI.setController(mainController);
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

    public static void main(String[] args) {
        Application app = new Application();
        app.start();
    }
}