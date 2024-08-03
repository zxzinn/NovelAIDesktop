package com.zxzinn.novelai.gui;

import com.zxzinn.novelai.api.NAIConstants;
import com.zxzinn.novelai.api.GenerationRequest;
import com.zxzinn.novelai.api.GenerationRequestBuilder;
import com.zxzinn.novelai.config.ConfigManager;
import com.zxzinn.novelai.controller.MainController;
import com.zxzinn.novelai.gui.common.ImagePreviewPanel;
import com.zxzinn.novelai.gui.filewindow.FileManagerTab;
import com.zxzinn.novelai.gui.generation.*;
import com.zxzinn.novelai.utils.Cache;
import com.zxzinn.novelai.utils.I18nManager;
import com.zxzinn.novelai.utils.UIComponent;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

@Log4j2
@Getter
@Setter
public class MainGUI extends JFrame implements UIComponent {
    private static final ConfigManager config = ConfigManager.getInstance();
    public static final int WINDOW_WIDTH = config.getInteger("ui.window.width");
    public static final int WINDOW_HEIGHT = config.getInteger("ui.window.height");

    private JTabbedPane mainTabbedPane;
    private PromptPanel promptPanel;
    private AbstractParametersPanel currentParametersPanel;
    private GenerationParametersPanel generationParametersPanel;
    private Img2ImgParametersPanel img2ImgParametersPanel;
    private ImagePreviewPanel imagePreviewPanel;
    private HistoryPanel historyPanel;
    private GenerationControlPanel generationControlPanel;
    private JTextArea consoleArea;
    private FileManagerTab fileManagerTab;

    private JPanel leftPanel;
    private JPanel parameterPanel;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JComboBox<String> actionComboBox;

    private final Cache cache;
    private MainController controller;

    public MainGUI() {
        this.cache = Cache.getInstance();

        setTitle(config.getString("application.name") + " v" + config.getString("application.version"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);

        initializeComponents();
        layoutComponents();
        bindEvents();
        setupWindowListener();

        log.info("MainGUI initialized");
    }

    @Override
    public void initializeComponents() {
        mainTabbedPane = new JTabbedPane();
        promptPanel = new PromptPanel();
        generationParametersPanel = new GenerationParametersPanel();
        img2ImgParametersPanel = new Img2ImgParametersPanel();
        currentParametersPanel = generationParametersPanel;
        imagePreviewPanel = new ImagePreviewPanel();
        historyPanel = new HistoryPanel(imagePreviewPanel);
        generationControlPanel = new GenerationControlPanel(this::onGenerate);

        consoleArea = new JTextArea(5, 20);
        consoleArea.setEditable(false);
        consoleArea.setLineWrap(true);
        consoleArea.setWrapStyleWord(true);

        fileManagerTab = new FileManagerTab();

        leftPanel = new JPanel(new BorderLayout());
        parameterPanel = new JPanel(new BorderLayout());
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.add(generationParametersPanel, "generate");
        cardPanel.add(img2ImgParametersPanel, "img2img");

        actionComboBox = new JComboBox<>(NAIConstants.ACTIONS);
        styleActionComboBox();
    }

    @Override
    public void layoutComponents() {
        setLayout(new BorderLayout());

        parameterPanel.add(actionComboBox, BorderLayout.NORTH);
        parameterPanel.add(cardPanel, BorderLayout.CENTER);
        parameterPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        leftPanel.add(parameterPanel, BorderLayout.CENTER);
        leftPanel.add(generationControlPanel, BorderLayout.SOUTH);

        JPanel generatorPanel = new JPanel(new BorderLayout());
        generatorPanel.add(promptPanel, BorderLayout.NORTH);
        generatorPanel.add(new JScrollPane(imagePreviewPanel), BorderLayout.CENTER);
        generatorPanel.add(historyPanel, BorderLayout.EAST);

        JScrollPane consoleScrollPane = new JScrollPane(consoleArea);

        JPanel mainGeneratorPanel = new JPanel(new BorderLayout());
        mainGeneratorPanel.add(generatorPanel, BorderLayout.CENTER);
        mainGeneratorPanel.add(leftPanel, BorderLayout.WEST);
        mainGeneratorPanel.add(consoleScrollPane, BorderLayout.SOUTH);

        mainTabbedPane.addTab(I18nManager.getString("tab.generator"), mainGeneratorPanel);
        mainTabbedPane.addTab(I18nManager.getString("tab.fileManager"), fileManagerTab);

        add(mainTabbedPane, BorderLayout.CENTER);
    }

    @Override
    public void bindEvents() {
        actionComboBox.addActionListener(e -> updateParametersPanel());
    }

    private void styleActionComboBox() {
        actionComboBox.setFont(new Font(actionComboBox.getFont().getName(), Font.BOLD, 16));
        actionComboBox.setBackground(new Color(70, 130, 180));
        actionComboBox.setForeground(Color.WHITE);
        actionComboBox.setPreferredSize(new Dimension(200, 40));
        ((JLabel)actionComboBox.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void updateParametersPanel() {
        String selectedAction = (String) actionComboBox.getSelectedItem();
        if ("generate".equals(selectedAction)) {
            switchToGenerationPanel();
        } else if ("img2img".equals(selectedAction)) {
            switchToImg2ImgPanel();
        }
    }

    private void switchToGenerationPanel() {
        if (currentParametersPanel != generationParametersPanel) {
            switchPanel(generationParametersPanel, "generate");
        }
    }

    private void switchToImg2ImgPanel() {
        if (currentParametersPanel != img2ImgParametersPanel) {
            switchPanel(img2ImgParametersPanel, "img2img");
        }
    }

    private void switchPanel(AbstractParametersPanel newPanel, String cardName) {
        currentParametersPanel.saveToCache();
        cardLayout.show(cardPanel, cardName);
        currentParametersPanel = newPanel;
        currentParametersPanel.loadCachedValues();
        updateUIForCurrentPanel();
    }

    private void updateUIForCurrentPanel() {
        revalidate();
        repaint();
    }

    private void setupWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveAllCache();
                fileManagerTab.shutdownFileWatcher();
                promptPanel.getPreviewManager().shutdown();  // 關閉PromptPreviewManager
                log.info("Application closing, cache saved, FileWatcher and PromptPreviewManager shutdown");
            }
        });
    }

    private void saveAllCache() {
        promptPanel.saveToCache();
        currentParametersPanel.saveToCache();
        fileManagerTab.saveWatchedFolders();
        cache.setParameter("action", (String) actionComboBox.getSelectedItem());
        cache.saveCache();
    }

    public void handleGeneratedImage(BufferedImage image) {
        SwingUtilities.invokeLater(() -> {
            imagePreviewPanel.setImage(image);
            imagePreviewPanel.fitToPanel();
            historyPanel.addImage(image);
            log.debug("Image added to HistoryPanel");
        });
    }

    public void handleError(String errorMessage) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
            appendToConsole("Error: " + errorMessage);
        });
    }

    public void appendToConsole(String message) {
        SwingUtilities.invokeLater(() -> {
            consoleArea.append(message + "\n");
            consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
        });
    }

    private void onGenerate(int count) {
        generateImages(count);
    }

    private void generateImages(int totalCount) {
        generateImage(totalCount, 0);
    }

    private void generateImage(final int totalCount, final int currentCount) {
        // 每次生成之前都重新處理 Embed
        promptPanel.resetFirstGeneration();
        promptPanel.preparePromptForGeneration().thenAccept(promptResult -> {
            SwingUtilities.invokeLater(() -> {
                // 更新提示詞預覽
                promptPanel.updatePreviewAreas(promptResult.positivePrompt, promptResult.negativePrompt);

                GenerationRequest request = buildRequest(promptResult);
                String apiKey = currentParametersPanel.getApiKeyField().getText();
                String outputDir = currentParametersPanel.getOutputDirField().getText().trim();

                // 更新生成控制面板狀態
                updateGenerationControlPanel(true);

                controller.generateImage(request, apiKey, outputDir, image -> {
                    handleGeneratedImage(image);
                    continueGeneration(totalCount, currentCount + 1);
                });
            });
        });
    }

    private void continueGeneration(int totalCount, int nextCount) {
        if (nextCount < totalCount) {
            generateImage(totalCount, nextCount);
        } else {
            // 生成完成，更新控制面板狀態
            updateGenerationControlPanel(false);
        }
    }

    private GenerationRequest buildRequest(PromptPanel.PromptResult promptResult) {
        String action = (String) actionComboBox.getSelectedItem();
        return GenerationRequestBuilder.buildRequest(action, promptResult.positivePrompt, promptResult.negativePrompt, currentParametersPanel);
    }

    public void updateGenerationControlPanel(boolean isGenerating) {
        SwingUtilities.invokeLater(() -> {
            generationControlPanel.updateState(isGenerating);
        });
    }
}