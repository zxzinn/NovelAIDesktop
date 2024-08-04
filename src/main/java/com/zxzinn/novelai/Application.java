package com.zxzinn.novelai;

import com.zxzinn.novelai.config.ConfigManager;
import com.zxzinn.novelai.gui.ContainerGUI;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;

@Log4j2
public class Application {
    private final ConfigManager config;
    private final ContainerGUI containerGUI;

    public Application() {
        config = ConfigManager.getInstance();
        containerGUI = new ContainerGUI();
    }

    public void start() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                log.error("設置外觀和感覺時出錯", e);
            }
            containerGUI.setVisible(true);
            log.info("應用程序已啟動");
        });
    }

    public static void main(String[] args) {
        Application app = new Application();
        app.start();
    }
}