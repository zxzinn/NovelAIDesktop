package com.zxzinn.novelai.gui.generationwindow;

import com.zxzinn.novelai.api.NAIConstants;
import com.zxzinn.novelai.config.ConfigManager;
import com.zxzinn.novelai.utils.Cache;
import com.zxzinn.novelai.utils.I18nManager;
import com.zxzinn.novelai.utils.UIComponent;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

@Log4j2
public abstract class AbstractParametersPanel extends JPanel implements UIComponent {
    protected static final ConfigManager config = ConfigManager.getInstance();
    protected static final Cache cache = Cache.getInstance();

    // Common components
    protected JComboBox<String> modelComboBox;
    protected JTextField apiKeyField;
    protected JTextField outputDirField;

    public abstract JTextField getApiKeyField();
    public abstract JTextField getOutputDirField();

    public abstract Map<String, Object> getParameters();


    public AbstractParametersPanel() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setPreferredSize(new Dimension(300, 500));

        initializeComponents();
        layoutComponents();
        bindEvents();
        loadCachedValues();
    }

    @Override
    public void initializeComponents() {
        modelComboBox = new JComboBox<>(NAIConstants.MODELS);
        apiKeyField = new JTextField(20);
        outputDirField = new JTextField(20);
        initSpecificComponents();
    }

    protected abstract void initSpecificComponents();

    @Override
    public void layoutComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;

        int gridy = 0;
        addComponentWithLabel(I18nManager.getString("param.apiKey"), apiKeyField, gbc, gridy++);
        addComponentWithLabel(I18nManager.getString("param.model"), modelComboBox, gbc, gridy++);
        addComponentWithLabel(I18nManager.getString("param.outputDir"), outputDirField, gbc, gridy++);

        layoutSpecificComponents(gbc, gridy);
    }

    protected abstract void layoutSpecificComponents(GridBagConstraints gbc, int startGridy);

    @Override
    public void bindEvents() {
        // Common event bindings can be added here
        bindSpecificEvents();
    }

    protected abstract void bindSpecificEvents();

    @Override
    public JComponent getComponent() {
        return this;
    }

    public void loadCachedValues() {
        modelComboBox.setSelectedItem(cache.getParameter("model", "nai-diffusion-3"));
        apiKeyField.setText(cache.getParameter("apiKey", ""));
        outputDirField.setText(cache.getParameter("outputDir", config.getString("output.directory")));
        loadSpecificCachedValues();
    }

    protected abstract void loadSpecificCachedValues();

    public void saveToCache() {
        cache.setParameter("model", (String) modelComboBox.getSelectedItem());
        cache.setParameter("apiKey", apiKeyField.getText());
        cache.setParameter("outputDir", outputDirField.getText());
        saveSpecificToCache();
    }

    protected abstract void saveSpecificToCache();

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
}