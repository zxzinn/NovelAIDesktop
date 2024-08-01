package com.zxzinn.novelai.gui;

import com.zxzinn.novelai.api.NAIConstants;
import com.zxzinn.novelai.api.NAIGenerate;
import com.zxzinn.novelai.api.NAIImg2Img;
import com.zxzinn.novelai.api.NAIRequest;
import com.zxzinn.novelai.config.ConfigManager;
import com.zxzinn.novelai.gui.filewindow.FileManagerTab;
import com.zxzinn.novelai.gui.common.ImagePreviewPanel;
import com.zxzinn.novelai.gui.generationwindow.HistoryPanel;
import com.zxzinn.novelai.gui.generationwindow.ImageGenerator;
import com.zxzinn.novelai.gui.generationwindow.AbstractParametersPanel;
import com.zxzinn.novelai.gui.generationwindow.GenerationParametersPanel;
import com.zxzinn.novelai.gui.generationwindow.Img2ImgParametersPanel;
import com.zxzinn.novelai.gui.generationwindow.PromptPanel;
import com.zxzinn.novelai.utils.Cache;
import com.zxzinn.novelai.utils.I18nManager;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

@Log4j2
public class MainGUI extends JFrame {
    private static final ConfigManager config = ConfigManager.getInstance();
    public static final int WINDOW_WIDTH = config.getInteger("ui.window.width");
    public static final int WINDOW_HEIGHT = config.getInteger("ui.window.height");

    private JTabbedPane mainTabbedPane;
    @Getter private PromptPanel promptPanel;
    @Getter private AbstractParametersPanel currentParametersPanel;
    private GenerationParametersPanel generationParametersPanel;
    private Img2ImgParametersPanel img2ImgParametersPanel;
    @Getter private ImagePreviewPanel imagePreviewPanel;
    private HistoryPanel historyPanel;
    @Getter private JButton generateButton;
    @Getter private JComboBox<String> generationCountComboBox;
    @Getter private JTextArea consoleArea;
    @Getter private FileManagerTab fileManagerTab;

    @Getter private final ImageGenerator imageGenerator;
    private final Cache cache;

    private JPanel leftPanel;
    private JPanel parameterPanel;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JComboBox<String> actionComboBox;

    public MainGUI(ImageGenerator imageGenerator) {
        this.imageGenerator = imageGenerator;
        setTitle(config.getString("application.name") + " v" + config.getString("application.version"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);

        cache = Cache.getInstance();

        initComponents();
        layoutComponents();
        setupWindowListener();

        log.info("MainGUI initialized");
    }

    private void initComponents() {
        mainTabbedPane = new JTabbedPane();
        promptPanel = new PromptPanel();
        generationParametersPanel = new GenerationParametersPanel();
        img2ImgParametersPanel = new Img2ImgParametersPanel();
        currentParametersPanel = generationParametersPanel; // Default to generation panel
        imagePreviewPanel = new ImagePreviewPanel();
        historyPanel = new HistoryPanel(imagePreviewPanel);

        generateButton = new JButton(I18nManager.getString("button.generate"));

        String[] countOptions = {"1", "2", "3", "4", I18nManager.getString("option.infinite")};
        generationCountComboBox = new JComboBox<>(countOptions);

        consoleArea = new JTextArea(5, 20);
        consoleArea.setEditable(false);

        fileManagerTab = new FileManagerTab();

        leftPanel = new JPanel(new BorderLayout());
        parameterPanel = new JPanel(new BorderLayout());
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.add(generationParametersPanel, "generate");
        cardPanel.add(img2ImgParametersPanel, "img2img");

        actionComboBox = new JComboBox<>(NAIConstants.ACTIONS);
        styleActionComboBox();
        setupActionComboBoxListener();
    }

    private void styleActionComboBox() {
        actionComboBox.setFont(new Font(actionComboBox.getFont().getName(), Font.BOLD, 16));
        actionComboBox.setBackground(new Color(70, 130, 180)); // Steel Blue
        actionComboBox.setForeground(Color.WHITE);
        actionComboBox.setPreferredSize(new Dimension(300, 40));
        ((JLabel)actionComboBox.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void setupActionComboBoxListener() {
        actionComboBox.addActionListener(e -> updateParametersPanel());
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());

        // Parameter panel layout
        parameterPanel.add(actionComboBox, BorderLayout.NORTH);
        parameterPanel.add(cardPanel, BorderLayout.CENTER);
        parameterPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Left panel layout
        leftPanel.add(parameterPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(generateButton, BorderLayout.CENTER);
        buttonPanel.add(generationCountComboBox, BorderLayout.EAST);
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Main layout
        JPanel generatorPanel = new JPanel(new BorderLayout());
        generatorPanel.add(promptPanel, BorderLayout.NORTH);
        generatorPanel.add(new JScrollPane(imagePreviewPanel), BorderLayout.CENTER);

        JScrollPane consoleScrollPane = new JScrollPane(consoleArea);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(historyPanel, BorderLayout.CENTER);

        JPanel mainGeneratorPanel = new JPanel(new BorderLayout());
        mainGeneratorPanel.add(generatorPanel, BorderLayout.CENTER);
        mainGeneratorPanel.add(leftPanel, BorderLayout.WEST);
        mainGeneratorPanel.add(rightPanel, BorderLayout.EAST);
        mainGeneratorPanel.add(consoleScrollPane, BorderLayout.SOUTH);

        mainTabbedPane.addTab(I18nManager.getString("tab.generator"), mainGeneratorPanel);
        mainTabbedPane.addTab(I18nManager.getString("tab.fileManager"), fileManagerTab);

        add(mainTabbedPane, BorderLayout.CENTER);
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
        String positivePrompt = promptPanel.getPositivePrompt();
        String negativePrompt = promptPanel.getNegativePrompt();
        String action = (String) actionComboBox.getSelectedItem();

        if ("generate".equals(action)) {
            GenerationParametersPanel panel = (GenerationParametersPanel) currentParametersPanel;
            return NAIGenerate.builder()
                    .input(positivePrompt)
                    .model((String) panel.getModelComboBox().getSelectedItem())
                    .action(action)
                    .width(Integer.parseInt(panel.getWidthField().getText()))
                    .height(Integer.parseInt(panel.getHeightField().getText()))
                    .scale(Double.parseDouble(panel.getScaleField().getText()))
                    .sampler((String) panel.getSamplerComboBox().getSelectedItem())
                    .steps(Integer.parseInt(panel.getStepsField().getText()))
                    .seed(Long.parseLong(panel.getSeedField().getText()))
                    .n_samples(Integer.parseInt(panel.getNSamplesField().getText()))
                    .negative_prompt(negativePrompt)
                    .sm(panel.getSmeaCheckbox().isSelected())
                    .sm_dyn(panel.getSmeaDynCheckbox().isSelected())
                    .build();
        } else if ("img2img".equals(action)) {
            Img2ImgParametersPanel panel = (Img2ImgParametersPanel) currentParametersPanel;
            String base64Image = panel.getBase64Image();
            if (base64Image == null || base64Image.isEmpty()) {
                throw new IllegalStateException("No image uploaded for img2img");
            }
            return NAIImg2Img.builder()
                    .input(positivePrompt)
                    .model((String) panel.getModelComboBox().getSelectedItem())
                    .action(action)
                    .width(Integer.parseInt(panel.getWidthField().getText()))
                    .height(Integer.parseInt(panel.getHeightField().getText()))
                    .scale(Double.parseDouble(panel.getScaleField().getText()))
                    .sampler((String) panel.getSamplerComboBox().getSelectedItem())
                    .steps(Integer.parseInt(panel.getStepsField().getText()))
                    .seed(Long.parseLong(panel.getSeedField().getText()))
                    .n_samples(Integer.parseInt(panel.getNSamplesField().getText()))
                    .negative_prompt(negativePrompt)
                    .sm(panel.getSmeaCheckbox().isSelected())
                    .sm_dyn(panel.getSmeaDynCheckbox().isSelected())
                    .extra_noise_seed(Long.parseLong(panel.getExtraNoiseSeedField().getText()))
                    .image(base64Image)
                    .build();
        }

        throw new IllegalStateException("Unknown action: " + action);
    }

}