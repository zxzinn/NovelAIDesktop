package com.zxzinn.novelai.gui.generation;

import com.zxzinn.novelai.generation.prompt.PromptProcessor;
import com.zxzinn.novelai.generation.prompt.EmbedPromptProcessor;
import com.zxzinn.novelai.utils.Cache;
import com.zxzinn.novelai.utils.I18nManager;
import com.zxzinn.novelai.utils.UIComponent;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.*;

@Log4j2
public class PromptPanel extends JPanel implements UIComponent {
    private final PromptProcessor promptProcessor;
    private final Cache cache;
    private JTextArea positivePromptArea;
    private JTextArea negativePromptArea;
    @Getter private JTextArea positivePreviewArea;
    @Getter private JTextArea negativePreviewArea;
    private JButton refreshPositiveButton;
    private JButton refreshNegativeButton;
    private JToggleButton modeToggleButton;
    @Getter private boolean previewMode = false;

    public PromptPanel() {
        this.promptProcessor = new EmbedPromptProcessor();
        this.cache = Cache.getInstance();
        setLayout(new GridBagLayout());

        initializeComponents();
        layoutComponents();
        bindEvents();
        loadCachedPrompts();
    }

    @Override
    public void initializeComponents() {
        positivePromptArea = createPromptArea();
        negativePromptArea = createPromptArea();
        positivePreviewArea = createPromptArea();
        negativePreviewArea = createPromptArea();

        positivePromptArea.setBorder(BorderFactory.createTitledBorder(I18nManager.getString("prompt.positive")));
        negativePromptArea.setBorder(BorderFactory.createTitledBorder(I18nManager.getString("prompt.negative")));
        positivePreviewArea.setBorder(BorderFactory.createTitledBorder(I18nManager.getString("prompt.positivePreview")));
        negativePreviewArea.setBorder(BorderFactory.createTitledBorder(I18nManager.getString("prompt.negativePreview")));

        refreshPositiveButton = new JButton(I18nManager.getString("button.refreshPositive"));
        refreshNegativeButton = new JButton(I18nManager.getString("button.refreshNegative"));
        modeToggleButton = new JToggleButton(I18nManager.getString("button.togglePreviewMode"));
    }

    private JTextArea createPromptArea() {
        JTextArea area = new JTextArea(5, 40);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        return area;
    }

    @Override
    public void layoutComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JScrollPane(positivePromptArea), gbc);

        gbc.gridx = 1;
        add(new JScrollPane(positivePreviewArea), gbc);

        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        add(refreshPositiveButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        add(new JScrollPane(negativePromptArea), gbc);

        gbc.gridx = 1;
        add(new JScrollPane(negativePreviewArea), gbc);

        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        add(refreshNegativeButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        add(modeToggleButton, gbc);

        updateComponentVisibility();
    }

    @Override
    public void bindEvents() {
        refreshPositiveButton.addActionListener(e -> refreshPreview(true));
        refreshNegativeButton.addActionListener(e -> refreshPreview(false));
        modeToggleButton.addActionListener(e -> {
            previewMode = modeToggleButton.isSelected();
            updateComponentVisibility();
        });
    }

    private void updateComponentVisibility() {
        positivePreviewArea.setVisible(previewMode);
        negativePreviewArea.setVisible(previewMode);
        refreshPositiveButton.setVisible(previewMode);
        refreshNegativeButton.setVisible(previewMode);
        revalidate();
        repaint();
    }

    private void refreshPreview(boolean isPositive) {
        JTextArea sourceArea = isPositive ? positivePromptArea : negativePromptArea;
        JTextArea previewArea = isPositive ? positivePreviewArea : negativePreviewArea;
        String processedPrompt = processPrompt(sourceArea.getText());
        previewArea.setText(processedPrompt);
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    public String getPositivePrompt() {
        return previewMode ? positivePreviewArea.getText() : processPrompt(positivePromptArea.getText());
    }

    public String getNegativePrompt() {
        return previewMode ? negativePreviewArea.getText() : processPrompt(negativePromptArea.getText());
    }

    private String processPrompt(String rawPrompt) {
        return promptProcessor.processPrompt(rawPrompt);
    }

    private void loadCachedPrompts() {
        positivePromptArea.setText(cache.getPrompt("positive", ""));
        negativePromptArea.setText(cache.getPrompt("negative", ""));
    }

    public void saveToCache() {
        cache.setPrompt("positive", positivePromptArea.getText());
        cache.setPrompt("negative", negativePromptArea.getText());
        cache.saveCache();
    }
}