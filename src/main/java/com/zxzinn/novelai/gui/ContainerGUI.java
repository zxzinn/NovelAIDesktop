package com.zxzinn.novelai.gui;

import com.zxzinn.novelai.config.ConfigManager;
import com.zxzinn.novelai.gui.filewindow.FileManagerTab;
import com.zxzinn.novelai.gui.generation.GenerationGUI;
import com.zxzinn.novelai.utils.I18nManager;
import com.zxzinn.novelai.utils.UIComponent;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@Log4j2
public class ContainerGUI extends JFrame implements UIComponent {
    private static final ConfigManager config = ConfigManager.getInstance();
    public static final int WINDOW_WIDTH = config.getInteger("ui.window.width");
    public static final int WINDOW_HEIGHT = config.getInteger("ui.window.height");

    private JTabbedPane mainTabbedPane;
    private GenerationGUI generationGUI;
    private FileManagerTab fileManagerTab;

    public ContainerGUI() {
        setTitle(config.getString("application.name") + " v" + config.getString("application.version"));
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initializeComponents();
        layoutComponents();
        bindEvents();
        setupWindowListener();

        log.info("ContainerGUI initialized");
    }

    @Override
    public void initializeComponents() {
        mainTabbedPane = new JTabbedPane();
        generationGUI = new GenerationGUI();
        fileManagerTab = new FileManagerTab();
    }

    @Override
    public void layoutComponents() {
        setLayout(new BorderLayout());
        mainTabbedPane.addTab(I18nManager.getString("tab.generator"), generationGUI);
        mainTabbedPane.addTab(I18nManager.getString("tab.fileManager"), fileManagerTab);
        add(mainTabbedPane, BorderLayout.CENTER);
    }

    @Override
    public void bindEvents() {
    }

    private void setupWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                log.info("Application closing");
                saveAllCache();
                fileManagerTab.shutdownFileWatcher();
                generationGUI.shutdown();
                log.info("Application closed");
            }
        });
    }

    private void saveAllCache() {
        generationGUI.saveAllCache();
        fileManagerTab.saveWatchedFolders();
        log.info("Cache saved");
    }
}