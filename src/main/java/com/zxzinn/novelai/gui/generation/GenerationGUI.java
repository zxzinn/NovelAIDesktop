package com.zxzinn.novelai.gui.generation;

import com.zxzinn.novelai.GenerationState;
import com.zxzinn.novelai.api.GenerationRequest;
import com.zxzinn.novelai.api.GenerationRequestBuilder;
import com.zxzinn.novelai.api.NAIConstants;
import com.zxzinn.novelai.event.ImageReceivedEvent;
import com.zxzinn.novelai.event.ImageReceivedListener;
import com.zxzinn.novelai.event.PromptUpdateEvent;
import com.zxzinn.novelai.event.PromptUpdateListener;
import com.zxzinn.novelai.gui.common.ImagePreviewPanel;
import com.zxzinn.novelai.service.ImageGenerationService;
import com.zxzinn.novelai.utils.Cache;
import com.zxzinn.novelai.utils.I18nManager;
import com.zxzinn.novelai.utils.UIComponent;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Getter
public class GenerationGUI extends JPanel implements UIComponent, ImageReceivedListener, PromptUpdateListener {
    private volatile boolean isGenerating = false;
    private final Object generationLock = new Object();

    private PromptPanel promptPanel;
    private AbstractParametersPanel currentParametersPanel;
    private GenerationParametersPanel generationParametersPanel;
    private Img2ImgParametersPanel img2ImgParametersPanel;
    private ImagePreviewPanel imagePreviewPanel;
    private HistoryPanel historyPanel;
    private GenerationControlPanel generationControlPanel;

    private JPanel leftPanel;
    private JPanel parameterPanel;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JComboBox<String> actionComboBox;

    private final Cache cache;
    private final ImageGenerationService imageGenerationService;
    private PromptPanel.PromptResult currentPromptResult;

    public GenerationGUI() {
        this.cache = Cache.getInstance();
        this.imageGenerationService = new ImageGenerationService();

        initializeComponents();
        layoutComponents();
        bindEvents();

        log.info("GenerationGUI initialized");
    }

    @Override
    public void initializeComponents() {
        promptPanel = new PromptPanel();
        promptPanel.addPromptUpdateListener(this);

        generationParametersPanel = new GenerationParametersPanel();
        img2ImgParametersPanel = new Img2ImgParametersPanel();
        currentParametersPanel = generationParametersPanel;

        imagePreviewPanel = new ImagePreviewPanel();
        historyPanel = new HistoryPanel(imagePreviewPanel);

        generationControlPanel = new GenerationControlPanel();

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

        add(generatorPanel, BorderLayout.CENTER);
        add(leftPanel, BorderLayout.WEST);
    }

    @Override
    public void bindEvents() {
        actionComboBox.addActionListener(e -> updateParametersPanel());
        generationControlPanel.setOnGenerateRequested(this::startImageGeneration);
        generationControlPanel.addImageReceivedListener(this);
    }

    private void startImageGeneration() {
        if (isGenerating) {
            log.info("已經在生成圖像中，忽略新的請求");
            return;
        }

        isGenerating = true;
        generationControlPanel.updateState(GenerationState.GENERATING);

        CompletableFuture.runAsync(() -> {
            try {
                currentPromptResult = promptPanel.preparePromptForGeneration().get();
                String countSelection = (String) generationControlPanel.getGenerationCountComboBox().getSelectedItem();
                assert countSelection != null;
                int count = countSelection.equals(I18nManager.getString("option.infinite")) ? Integer.MAX_VALUE : Integer.parseInt(countSelection);
                generationControlPanel.setLastingCount(count);
                generateNextImage();
            } catch (Exception e) {
                log.error("準備提示詞時出錯", e);
                handleError("準備提示詞時出錯: " + e.getMessage());
                isGenerating = false;
                generationControlPanel.updateState(GenerationState.IDLE);
            }
        });
    }

    private void generateNextImage() {
        if (!isGenerating) {
            return;
        }

        try {
            String action = (String) actionComboBox.getSelectedItem();
            GenerationRequest request = GenerationRequestBuilder.buildRequest(
                    action,
                    currentPromptResult.positivePrompt,
                    currentPromptResult.negativePrompt,
                    currentParametersPanel
            );

            String apiKey = currentParametersPanel.getApiKeyField().getText();
            log.info("發送API請求以生成圖像");
            imageGenerationService.generateImage(request, apiKey)
                    .thenAccept(generationControlPanel::onImageReceived)
                    .exceptionally(e -> {
                        handleError("生成圖像時出錯: " + e.getMessage());
                        isGenerating = false;
                        generationControlPanel.updateState(GenerationState.IDLE);
                        return null;
                    });
        } catch (Exception e) {
            handleError("準備生成請求時出錯: " + e.getMessage());
            isGenerating = false;
            generationControlPanel.updateState(GenerationState.IDLE);
        }
    }

    private void refreshPromptPreview() {
        CompletableFuture.runAsync(() -> {
            try {
                PromptPanel.PromptResult nextPromptResult = promptPanel.reprocessEmbed().get();
                SwingUtilities.invokeLater(() -> promptPanel.updatePreviewAreas(nextPromptResult.positivePrompt, nextPromptResult.negativePrompt));
            } catch (Exception e) {
                log.error("刷新提示詞預覽時出錯", e);
            }
        });
    }

    @Override
    public void onImageReceived(ImageReceivedEvent event) {
        BufferedImage image = event.getImage();
        SwingUtilities.invokeLater(() -> {
            try {
                imagePreviewPanel.setImage(image);
                imagePreviewPanel.fitToPanel();
                historyPanel.addImage(image);
                log.debug("圖像已添加到HistoryPanel");

                String outputDir = currentParametersPanel.getOutputDirField().getText();
                imageGenerationService.saveImage(image, outputDir);

                log.info("圖像處理完成");
                if (!promptPanel.isLocked()) {
                    refreshPromptPreview();
                }

                if (generationControlPanel.decrementLastingCount()) {
                    generateNextImage();
                } else {
                    isGenerating = false;
                    generationControlPanel.updateState(GenerationState.IDLE);
                }
            } catch (Exception e) {
                handleError("處理生成的圖像時出錯: " + e.getMessage());
                isGenerating = false;
                generationControlPanel.updateState(GenerationState.IDLE);
            }
        });
    }

    private void handleError(String errorMessage) {
        log.error(errorMessage);
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, errorMessage, "錯誤", JOptionPane.ERROR_MESSAGE));
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
            switchPanel(generationParametersPanel, "generate");
        } else if ("img2img".equals(selectedAction)) {
            switchPanel(img2ImgParametersPanel, "img2img");
        }
    }

    private void switchPanel(AbstractParametersPanel newPanel, String cardName) {
        currentParametersPanel.saveToCache();
        cardLayout.show(cardPanel, cardName);
        currentParametersPanel = newPanel;
        currentParametersPanel.loadCachedValues();
        revalidate();
        repaint();
    }

    public void saveAllCache() {
        promptPanel.saveToCache();
        currentParametersPanel.saveToCache();
        cache.setParameter("action", (String) actionComboBox.getSelectedItem());
        cache.saveCache();
        log.info("Generation GUI cache saved");
    }

    public void shutdown() {
        promptPanel.getPreviewManager().shutdown();
        log.info("Generation GUI shutdown completed");
    }

    @Override
    public void onPromptUpdate(PromptUpdateEvent event) {
        currentPromptResult = new PromptPanel.PromptResult(event.getPositivePrompt(), event.getNegativePrompt());
    }
}