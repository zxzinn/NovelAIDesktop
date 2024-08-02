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
    private JTextArea positivePreviewArea;
    private JTextArea negativePreviewArea;
    private JButton refreshPositiveButton;
    private JButton refreshNegativeButton;
    private JTabbedPane modeTabbedPane;
    private JPanel autoModePanel;
    private JPanel manualModePanel;
    @Getter
    private boolean isAutoMode = true;

    public PromptPanel() {
        this.promptProcessor = new EmbedPromptProcessor();
        this.cache = Cache.getInstance();
        setLayout(new BorderLayout());

        initializeComponents();
        layoutComponents();
        bindEvents();
        loadCachedPrompts();
    }

    @Override
    public void initializeComponents() {
        modeTabbedPane = new JTabbedPane();
        autoModePanel = new JPanel(new GridBagLayout());
        manualModePanel = new JPanel(new GridBagLayout());

        positivePromptArea = createPromptArea("prompt.positive");
        negativePromptArea = createPromptArea("prompt.negative");
        positivePreviewArea = createPromptArea("prompt.positivePreview");
        negativePreviewArea = createPromptArea("prompt.negativePreview");

        refreshPositiveButton = new JButton(I18nManager.getString("button.refreshPositive"));
        refreshNegativeButton = new JButton(I18nManager.getString("button.refreshNegative"));
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
        layoutAutoModePanel();
        layoutManualModePanel();

        modeTabbedPane.addTab(I18nManager.getString("tab.autoMode"), autoModePanel);
        modeTabbedPane.addTab(I18nManager.getString("tab.manualMode"), manualModePanel);

        add(modeTabbedPane, BorderLayout.CENTER);
    }

    private void layoutAutoModePanel() {
        GridBagConstraints gbc = createGridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        autoModePanel.add(new JLabel(I18nManager.getString("prompt.positive")), gbc);

        gbc.gridy = 1;
        gbc.weighty = 1.0;
        autoModePanel.add(new JScrollPane(positivePromptArea), gbc);

        gbc.gridy = 2;
        gbc.weighty = 0.0;
        autoModePanel.add(new JLabel(I18nManager.getString("prompt.negative")), gbc);

        gbc.gridy = 3;
        gbc.weighty = 1.0;
        autoModePanel.add(new JScrollPane(negativePromptArea), gbc);
    }

    private void layoutManualModePanel() {
        GridBagConstraints gbc = createGridBagConstraints();

        // 正面提示詞
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weighty = 0.0;
        manualModePanel.add(new JLabel(I18nManager.getString("prompt.positive")), gbc);

        gbc.gridy = 1;
        gbc.weighty = 1.0;
        manualModePanel.add(new JScrollPane(positivePromptArea), gbc);

        gbc.gridx = 1;
        manualModePanel.add(new JScrollPane(positivePreviewArea), gbc);

        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        manualModePanel.add(refreshPositiveButton, gbc);

        // 負面提示詞
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        manualModePanel.add(new JLabel(I18nManager.getString("prompt.negative")), gbc);

        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        manualModePanel.add(new JScrollPane(negativePromptArea), gbc);

        gbc.gridx = 1;
        manualModePanel.add(new JScrollPane(negativePreviewArea), gbc);

        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        manualModePanel.add(refreshNegativeButton, gbc);
    }

    private GridBagConstraints createGridBagConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        return gbc;
    }

    @Override
    public void bindEvents() {
        refreshPositiveButton.addActionListener(e -> refreshPreview(true));
        refreshNegativeButton.addActionListener(e -> refreshPreview(false));
        modeTabbedPane.addChangeListener(e -> {
            isAutoMode = modeTabbedPane.getSelectedIndex() == 0;
            updateComponentVisibility();
        });
    }

    private void updateComponentVisibility() {
        positivePreviewArea.setVisible(!isAutoMode);
        negativePreviewArea.setVisible(!isAutoMode);
        refreshPositiveButton.setVisible(!isAutoMode);
        refreshNegativeButton.setVisible(!isAutoMode);
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
        return isAutoMode ? positivePromptArea.getText() : positivePreviewArea.getText();
    }

    public String getNegativePrompt() {
        return isAutoMode ? negativePromptArea.getText() : negativePreviewArea.getText();
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