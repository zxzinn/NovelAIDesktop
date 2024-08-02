package com.zxzinn.novelai.gui;

import com.zxzinn.novelai.api.NAIConstants;
import com.zxzinn.novelai.api.NAIRequest;
import com.zxzinn.novelai.api.RequestBuilder;
import com.zxzinn.novelai.config.ConfigManager;
import com.zxzinn.novelai.gui.common.ImagePreviewPanel;
import com.zxzinn.novelai.gui.filewindow.FileManagerTab;
import com.zxzinn.novelai.gui.generationwindow.*;
import com.zxzinn.novelai.utils.Cache;
import com.zxzinn.novelai.utils.I18nManager;
import com.zxzinn.novelai.utils.UIComponent;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

@Log4j2
public class MainGUI extends JFrame implements UIComponent {
    private static final ConfigManager config = ConfigManager.getInstance();
    public static final int WINDOW_WIDTH = config.getInteger("ui.window.width");
    public static final int WINDOW_HEIGHT = config.getInteger("ui.window.height");

    private JTabbedPane mainTabbedPane;
    private PromptPanel promptPanel;
    @Getter
    private AbstractParametersPanel currentParametersPanel;
    private GenerationParametersPanel generationParametersPanel;
    private Img2ImgParametersPanel img2ImgParametersPanel;
    private ImagePreviewPanel imagePreviewPanel;
    private HistoryPanel historyPanel;
    @Getter
    private JButton generateButton;
    @Getter
    private JComboBox<String> generationCountComboBox;
    private JTextArea consoleArea;
    private FileManagerTab fileManagerTab;

    private final ImageGenerator imageGenerator;
    private final Cache cache;

    private JPanel leftPanel;
    private JPanel parameterPanel;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JComboBox<String> actionComboBox;

    public MainGUI(ImageGenerator imageGenerator) {
        this.imageGenerator = imageGenerator;
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

        generateButton = new JButton(I18nManager.getString("button.generate"));
        generateButton.setFont(new Font(generateButton.getFont().getName(), Font.BOLD, 16));
        generateButton.setBackground(new Color(70, 130, 180));
        generateButton.setForeground(Color.WHITE);
        generateButton.setPreferredSize(new Dimension(200, 40));
        generateButton.setFocusPainted(false);

        String[] countOptions = {"1", "2", "3", "4", I18nManager.getString("option.infinite")};
        generationCountComboBox = new JComboBox<>(countOptions);

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

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(generateButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(generationCountComboBox);
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);

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
        generateButton.addActionListener(e -> toggleGeneration());
    }

    @Override
    public JComponent getComponent() {
        return (JComponent) getContentPane();
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
            currentParametersPanel.saveToCache();
            cardLayout.show(cardPanel, "generate");
            currentParametersPanel = generationParametersPanel;
            currentParametersPanel.loadCachedValues();
            updateUIForCurrentPanel();
        }
    }

    private void switchToImg2ImgPanel() {
        if (currentParametersPanel != img2ImgParametersPanel) {
            currentParametersPanel.saveToCache();
            cardLayout.show(cardPanel, "img2img");
            currentParametersPanel = img2ImgParametersPanel;
            currentParametersPanel.loadCachedValues();
            updateUIForCurrentPanel();
        }
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
                log.info("Application closing, cache saved and FileWatcher shutdown");
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

    public NAIRequest buildRequest() {
        String action = (String) actionComboBox.getSelectedItem();
        return RequestBuilder.buildRequest(action, promptPanel, currentParametersPanel);
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

            NAIRequest request = buildRequest();
            String apiKey = currentParametersPanel.getApiKeyField().getText();
            String outputDir = currentParametersPanel.getOutputDirField().getText().trim();

            imageGenerator.toggleGeneration(request, apiKey, count, outputDir);
        }
    }

    // Make these methods public
    public void loadCachedValues() {
        currentParametersPanel.loadCachedValues();
    }

    public void saveToCache() {
        currentParametersPanel.saveToCache();
    }
}