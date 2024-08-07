package com.zxzinn.novelai.gui.generation;

import com.zxzinn.novelai.event.PromptUpdateEvent;
import com.zxzinn.novelai.event.PromptUpdateListener;
import com.zxzinn.novelai.generation.prompt.PromptProcessor;
import com.zxzinn.novelai.generation.prompt.EmbedPromptProcessor;
import com.zxzinn.novelai.utils.Cache;
import com.zxzinn.novelai.utils.I18nManager;
import com.zxzinn.novelai.utils.UIComponent;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Getter
public class PromptPanel extends JPanel implements UIComponent {
    private final PromptProcessor promptProcessor;
    private final Cache cache;
    private final PromptPreviewManager previewManager;

    private JTextArea positivePromptArea;
    private JTextArea negativePromptArea;
    private JTextArea positivePreviewArea;
    private JTextArea negativePreviewArea;

    private JButton refreshButton;
    private JToggleButton lockButton;
    private JPanel promptContainer;

    private Timer updateTimer;

    private boolean isLocked = false;

    private final List<PromptUpdateListener> promptUpdateListeners = new ArrayList<>();

    public PromptPanel() {
        this.promptProcessor = new EmbedPromptProcessor();
        this.cache = Cache.getInstance();
        this.previewManager = new PromptPreviewManager(promptProcessor);

        updateTimer = new Timer(500, e -> updatePreviews());
        updateTimer.setRepeats(false);

        setLayout(new BorderLayout());

        initializeComponents();
        layoutComponents();
        bindEvents();
        loadCachedPrompts();
    }

    @Override
    public void initializeComponents() {
        positivePromptArea = createPromptArea("prompt.positive");
        negativePromptArea = createPromptArea("prompt.negative");
        positivePreviewArea = createPromptArea("prompt.positivePreview");
        negativePreviewArea = createPromptArea("prompt.negativePreview");

        refreshButton = new JButton(I18nManager.getString("button.refreshEmbed"));
        lockButton = new JToggleButton(I18nManager.getString("button.lockEmbed"));

        promptContainer = new JPanel(new GridBagLayout());
    }

    private JTextArea createPromptArea(String borderTitle) {
        JTextArea area = new JTextArea(5, 40);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        area.setBorder(BorderFactory.createTitledBorder(I18nManager.getString(borderTitle)));
        return area;
    }

    @Override
    public void layoutComponents() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        controlPanel.add(refreshButton);
        controlPanel.add(lockButton);

        add(controlPanel, BorderLayout.NORTH);
        add(promptContainer, BorderLayout.CENTER);

        updatePromptLayout();
    }

    private void updatePromptLayout() {
        promptContainer.removeAll();
        GridBagConstraints gbc = createGridBagConstraints();

        gbc.weightx = 0.5;
        promptContainer.add(new JScrollPane(positivePromptArea), gbc);

        gbc.gridx = 1;
        promptContainer.add(new JScrollPane(positivePreviewArea), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        promptContainer.add(new JScrollPane(negativePromptArea), gbc);

        gbc.gridx = 1;
        promptContainer.add(new JScrollPane(negativePreviewArea), gbc);

        promptContainer.revalidate();
        promptContainer.repaint();
    }

    private GridBagConstraints createGridBagConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        return gbc;
    }

    @Override
    public void bindEvents() {
        refreshButton.addActionListener(e -> updatePreviews());
        lockButton.addActionListener(e -> isLocked = lockButton.isSelected());

        ComponentAdapter resizeListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updatePromptLayout();
            }
        };
        addComponentListener(resizeListener);

        previewManager.addPreviewUpdateListener(this::handlePreviewUpdate);

        SimpleDocumentListener documentListener = this::schedulePreviewUpdate;
        positivePromptArea.getDocument().addDocumentListener(documentListener);
        negativePromptArea.getDocument().addDocumentListener(documentListener);
    }

    private void handlePreviewUpdate(boolean isPositive, String previewText) {
        SwingUtilities.invokeLater(() -> {
            if (isPositive) {
                positivePreviewArea.setText(previewText);
            } else {
                negativePreviewArea.setText(previewText);
            }
        });
    }

    private void schedulePreviewUpdate() {
        updateTimer.restart();
    }

    public void addPromptUpdateListener(PromptUpdateListener listener) {
        promptUpdateListeners.add(listener);
    }

    private void firePromptUpdateEvent(PromptUpdateEvent event) {
        for (PromptUpdateListener listener : promptUpdateListeners) {
            listener.onPromptUpdate(event);
        }
    }

    private void updatePreviews() {
        String positivePrompt = positivePromptArea.getText();
        String negativePrompt = negativePromptArea.getText();
        previewManager.updatePreview(true, positivePrompt);
        previewManager.updatePreview(false, negativePrompt);
        firePromptUpdateEvent(new PromptUpdateEvent(this, positivePrompt, negativePrompt));
    }

    private void loadCachedPrompts() {
        positivePromptArea.setText(cache.getPrompt("positive", ""));
        negativePromptArea.setText(cache.getPrompt("negative", ""));
        updatePreviews();
    }

    public void saveToCache() {
        cache.setPrompt("positive", positivePromptArea.getText());
        cache.setPrompt("negative", negativePromptArea.getText());
        cache.saveCache();
    }

    public CompletableFuture<PromptResult> preparePromptForGeneration() {
        return CompletableFuture.supplyAsync(() -> {
            String positivePrompt = positivePreviewArea.getText();
            String negativePrompt = negativePreviewArea.getText();
            return new PromptResult(positivePrompt, negativePrompt);
        });
    }

    public CompletableFuture<PromptResult> reprocessEmbed() {
        return CompletableFuture.supplyAsync(() -> {
            String positivePreview = previewManager.processPrompt(positivePromptArea.getText());
            String negativePreview = previewManager.processPrompt(negativePromptArea.getText());
            return new PromptResult(positivePreview, negativePreview);
        });
    }

    public void updatePreviewAreas(String positivePreview, String negativePreview) {
        SwingUtilities.invokeLater(() -> {
            positivePreviewArea.setText(positivePreview);
            negativePreviewArea.setText(negativePreview);
        });
    }

    public static class PromptResult {
        public final String positivePrompt;
        public final String negativePrompt;

        public PromptResult(String positivePrompt, String negativePrompt) {
            this.positivePrompt = positivePrompt;
            this.negativePrompt = negativePrompt;
        }
    }
}