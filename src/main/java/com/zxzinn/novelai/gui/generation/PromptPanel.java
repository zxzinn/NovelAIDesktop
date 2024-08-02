package com.zxzinn.novelai.gui.generation;

import com.zxzinn.novelai.generation.prompt.PromptProcessor;
import com.zxzinn.novelai.generation.prompt.EmbedPromptProcessor;
import com.zxzinn.novelai.utils.Cache;
import com.zxzinn.novelai.utils.I18nManager;
import com.zxzinn.novelai.utils.UIComponent;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.*;

@Log4j2
public class PromptPanel extends JPanel implements UIComponent {
    private final PromptProcessor promptProcessor;
    private final Cache cache;
    private JTextArea positivePromptArea;
    private JTextArea negativePromptArea;

    public PromptPanel() {
        this.promptProcessor = new EmbedPromptProcessor();
        this.cache = Cache.getInstance();
        setLayout(new GridBagLayout());

        initializeComponents();
        layoutComponents();
        loadCachedPrompts();
    }

    @Override
    public void initializeComponents() {
        positivePromptArea = createPromptArea();
        negativePromptArea = createPromptArea();

        // 使用 JTextArea 的 setLineWrap 和 setWrapStyleWord 方法來實現文字換行
        positivePromptArea.setLineWrap(true);
        positivePromptArea.setWrapStyleWord(true);
        negativePromptArea.setLineWrap(true);
        negativePromptArea.setWrapStyleWord(true);

        // 使用 BorderFactory 創建帶有標題的邊框，提升視覺效果
        positivePromptArea.setBorder(BorderFactory.createTitledBorder(I18nManager.getString("prompt.positive")));
        negativePromptArea.setBorder(BorderFactory.createTitledBorder(I18nManager.getString("prompt.negative")));
    }

    private JTextArea createPromptArea() {
        JTextArea area = new JTextArea(5, 40);
        // 設置字體為等寬字體，提升輸入體驗
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

        gbc.gridy = 1;
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
        return processPrompt(positivePromptArea.getText());
    }

    public String getNegativePrompt() {
        return processPrompt(negativePromptArea.getText());
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