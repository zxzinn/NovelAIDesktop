package com.zxzinn.novelai.gui;

import com.zxzinn.novelai.api.NAIGenerate;
import com.zxzinn.novelai.config.ConfigManager;
import com.zxzinn.novelai.gui.filewindow.FileManagerTab;
import com.zxzinn.novelai.gui.common.ImagePreviewPanel;
import com.zxzinn.novelai.gui.generationwindow.HistoryPanel;
import com.zxzinn.novelai.gui.generationwindow.ImageGenerator;
import com.zxzinn.novelai.gui.generationwindow.ParametersPanel;
import com.zxzinn.novelai.gui.generationwindow.PromptPanel;
import com.zxzinn.novelai.utils.Cache;
import com.zxzinn.novelai.utils.I18nManager;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;

@Log4j2
public class MainGUI extends JFrame {
    private static final ConfigManager config = ConfigManager.getInstance();
    public static final int WINDOW_WIDTH = config.getInteger("ui.window.width");
    public static final int WINDOW_HEIGHT = config.getInteger("ui.window.height");

    private JTabbedPane mainTabbedPane;
    private PromptPanel promptPanel;
    private ParametersPanel parametersPanel;
    private ImagePreviewPanel imagePreviewPanel;
    private HistoryPanel historyPanel;
    private JButton generateButton;
    private JComboBox<String> generationCountComboBox;
    private JTextArea consoleArea;
    private FileManagerTab fileManagerTab;

    private final ImageGenerator imageGenerator;
    private final Cache cache;

    public MainGUI() {
        setTitle(config.getString("application.name") + " v" + config.getString("application.version"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);

        createLogDirectory();

        imageGenerator = new ImageGenerator();
        imageGenerator.setCallbacks(
                this::handleGeneratedImage,
                this::handleError,
                this::appendToConsole,
                this::onGenerationStopped
        );

        cache = Cache.getInstance();

        initComponents();
        layoutComponents();
        setupWindowListener();

        log.info("MainGUI initialized");
    }

    private void initComponents() {
        mainTabbedPane = new JTabbedPane();
        promptPanel = new PromptPanel();
        parametersPanel = new ParametersPanel();
        imagePreviewPanel = new ImagePreviewPanel();
        historyPanel = new HistoryPanel(imagePreviewPanel);

        generateButton = new JButton(I18nManager.getString("button.generate"));
        generateButton.addActionListener(e -> toggleGeneration());

        String[] countOptions = {"1", "2", "3", "4", I18nManager.getString("option.infinite")};
        generationCountComboBox = new JComboBox<>(countOptions);

        consoleArea = new JTextArea(5, 20);
        consoleArea.setEditable(false);

        fileManagerTab = new FileManagerTab();
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());

        JPanel generatorPanel = new JPanel(new BorderLayout());
        generatorPanel.add(promptPanel, BorderLayout.NORTH);
        generatorPanel.add(new JScrollPane(imagePreviewPanel), BorderLayout.CENTER);

        JScrollPane consoleScrollPane = new JScrollPane(consoleArea);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(historyPanel, BorderLayout.CENTER);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(parametersPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(generateButton, BorderLayout.CENTER);
        buttonPanel.add(generationCountComboBox, BorderLayout.EAST);
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);

        JPanel mainGeneratorPanel = new JPanel(new BorderLayout());
        mainGeneratorPanel.add(generatorPanel, BorderLayout.CENTER);
        mainGeneratorPanel.add(leftPanel, BorderLayout.WEST);
        mainGeneratorPanel.add(rightPanel, BorderLayout.EAST);
        mainGeneratorPanel.add(consoleScrollPane, BorderLayout.SOUTH);

        mainTabbedPane.addTab(I18nManager.getString("tab.generator"), mainGeneratorPanel);
        mainTabbedPane.addTab(I18nManager.getString("tab.fileManager"), fileManagerTab);

        add(mainTabbedPane, BorderLayout.CENTER);
    }

    private void toggleGeneration() {
        if (imageGenerator.isGenerating()) {
            generateButton.setEnabled(false);
            generateButton.setText(I18nManager.getString("button.stopping"));
            imageGenerator.requestStop();
        } else {
            generateButton.setText(I18nManager.getString("button.stop"));
            String countSelection = (String) generationCountComboBox.getSelectedItem();
            assert countSelection != null;
            int count = countSelection.equals(I18nManager.getString("option.infinite")) ? Integer.MAX_VALUE : Integer.parseInt(countSelection);

            NAIGenerate request = createNAIGenerateRequest();
            String apiKey = parametersPanel.getApiKeyField().getText();
            String outputDir = parametersPanel.getOutputDirField().getText().trim();

            imageGenerator.toggleGeneration(request, apiKey, count, outputDir);
        }
    }

    private void onGenerationStopped() {
        SwingUtilities.invokeLater(() -> {
            generateButton.setText(I18nManager.getString("button.generate"));
            generateButton.setEnabled(true);
        });
    }

    private NAIGenerate createNAIGenerateRequest() {
        String positivePrompt = promptPanel.getPositivePrompt();
        String negativePrompt = promptPanel.getNegativePrompt();

        return NAIGenerate.builder()
                .input(positivePrompt)
                .model((String) parametersPanel.getModelComboBox().getSelectedItem())
                .action((String) parametersPanel.getActionComboBox().getSelectedItem())
                .width(Integer.parseInt(parametersPanel.getWidthField().getText()))
                .height(Integer.parseInt(parametersPanel.getHeightField().getText()))
                .scale(Double.parseDouble(parametersPanel.getScaleField().getText()))
                .sampler((String) parametersPanel.getSamplerComboBox().getSelectedItem())
                .steps(Integer.parseInt(parametersPanel.getStepsField().getText()))
                .seed(Long.parseLong(parametersPanel.getSeedField().getText()))
                .n_samples(Integer.parseInt(parametersPanel.getNSamplesField().getText()))
                .negative_prompt(negativePrompt)
                .build();
    }

    private void handleGeneratedImage(BufferedImage image) {
        SwingUtilities.invokeLater(() -> {
            imagePreviewPanel.setImage(image);
            imagePreviewPanel.fitToPanel();
            historyPanel.addImage(image);
            log.debug("Image added to HistoryPanel");
        });
    }

    private void handleError(String errorMessage) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
            appendToConsole("Error: " + errorMessage);
        });
    }

    private void appendToConsole(String message) {
        SwingUtilities.invokeLater(() -> {
            consoleArea.append(message + "\n");
            consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
        });
    }

    private void createLogDirectory() {
        File logDir = new File("logs");
        if (!logDir.exists()) {
            boolean created = logDir.mkdir();
            if (created) {
                log.info("Log directory created successfully");
            } else {
                log.warn("Failed to create log directory");
            }
        }
    }

    private void setupWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveAllCache();
                fileManagerTab.shutdownFileWatcher();
                log.info("Application closing, cache saved and FileWatcher shutdown");
            }
        });
    }

    private void saveAllCache() {
        promptPanel.saveToCache();
        parametersPanel.saveToCache();
        fileManagerTab.saveWatchedFolders();  // 確保保存監視的資料夾
        cache.saveCache();
    }

    public static void main(String[] args) {
        ConfigManager.getInstance(); // Initialize configuration
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                log.error("Error setting Look and Feel", e);
            }
            MainGUI mainGUI = new MainGUI();
            mainGUI.setVisible(true);
            log.info("MainGUI started");
        });
    }
}