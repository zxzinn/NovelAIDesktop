package com.zxzinn.novelai.gui.generationwindow;

import com.zxzinn.novelai.api.NAIConstants;
import com.zxzinn.novelai.utils.Cache;
import com.zxzinn.novelai.utils.I18nManager;
import com.zxzinn.novelai.config.ConfigManager;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class AbstractParametersPanel extends JPanel {
    protected static final ConfigManager config = ConfigManager.getInstance();
    protected static final Cache cache = Cache.getInstance();

    // Common components
    protected JComboBox<String> modelComboBox;
    protected JTextField apiKeyField;
    protected JTextField outputDirField;

    public abstract Map<String, Object> getParameters();

    public AbstractParametersPanel() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setPreferredSize(new Dimension(300, 500)); // 設置固定的首選大小
        initCommonComponents();
        layoutCommonComponents();
        loadCommonCachedValues();
    }

    protected void initCommonComponents() {
        modelComboBox = new JComboBox<>(NAIConstants.MODELS);
        apiKeyField = new JTextField(20);
        outputDirField = new JTextField(20);
    }

    protected void layoutCommonComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;

        int gridy = 0;
        addComponentWithLabel(I18nManager.getString("param.apiKey"), apiKeyField, gbc, gridy++);
        addComponentWithLabel(I18nManager.getString("param.model"), modelComboBox, gbc, gridy++);
        addComponentWithLabel(I18nManager.getString("param.outputDir"), outputDirField, gbc, gridy);
    }

    protected void loadCommonCachedValues() {
        modelComboBox.setSelectedItem(cache.getParameter("model", "nai-diffusion-3"));
        apiKeyField.setText(cache.getParameter("apiKey", ""));
        outputDirField.setText(cache.getParameter("outputDir", config.getString("output.directory")));
    }

    public void saveCommonToCache() {
        cache.setParameter("model", (String) modelComboBox.getSelectedItem());
        cache.setParameter("apiKey", apiKeyField.getText());
        cache.setParameter("outputDir", outputDirField.getText());
    }

    protected void addComponentWithLabel(String labelText, JComponent component, GridBagConstraints gbc, int y) {
        gbc.gridy = y;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        add(component, gbc);
    }

    public abstract void saveToCache();
    public abstract void loadCachedValues();
}