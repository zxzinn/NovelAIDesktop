package com.zxzinn.novelai.gui.generationwindow;

import com.zxzinn.novelai.processing.embed.EmbedProcessor;
import com.zxzinn.novelai.utils.Cache;
import com.zxzinn.novelai.utils.I18nManager;
import com.zxzinn.novelai.utils.UIComponent;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.*;

@Log4j2
public class PromptPanel extends JPanel implements UIComponent {
    private final EmbedProcessor embedProcessor;
    private final Cache cache;
    private JTextArea positivePromptArea;
    private JTextArea negativePromptArea;

    public PromptPanel() {
        setLayout(new GridBagLayout());
        embedProcessor = new EmbedProcessor();
        cache = Cache.getInstance();

        initializeComponents();
        layoutComponents();
        bindEvents();
        loadCachedPrompts();
    }

    @Override
    public void initializeComponents() {
        positivePromptArea = new JTextArea(5, 40);
        negativePromptArea = new JTextArea(5, 40);

        positivePromptArea.setLineWrap(true);
        positivePromptArea.setWrapStyleWord(true);
        negativePromptArea.setLineWrap(true);
        negativePromptArea.setWrapStyleWord(true);
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
        add(new JLabel(I18nManager.getString("prompt.positive")), gbc);

        gbc.gridy = 1;
        gbc.weighty = 1.0;
        add(new JScrollPane(positivePromptArea), gbc);

        gbc.gridy = 2;
        gbc.weighty = 0.0;
        add(new JLabel(I18nManager.getString("prompt.negative")), gbc);

        gbc.gridy = 3;
        gbc.weighty = 1.0;
        add(new JScrollPane(negativePromptArea), gbc);
    }

    @Override
    public void bindEvents() {
        // No events to bind in this panel
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    public String getPositivePrompt() {
        String rawPrompt = positivePromptArea.getText();
        String processedPrompt = embedProcessor.processPrompt(rawPrompt);
        log.debug("Raw positive prompt: {}", rawPrompt);
        log.debug("Processed positive prompt: {}", processedPrompt);
        return processedPrompt;
    }

    public String getNegativePrompt() {
        String rawPrompt = negativePromptArea.getText();
        String processedPrompt = embedProcessor.processPrompt(rawPrompt);
        log.debug("Raw negative prompt: {}", rawPrompt);
        log.debug("Processed negative prompt: {}", processedPrompt);
        return processedPrompt;
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