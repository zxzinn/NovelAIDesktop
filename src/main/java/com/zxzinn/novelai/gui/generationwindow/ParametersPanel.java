package com.zxzinn.novelai.gui.generationwindow;

import com.zxzinn.novelai.api.NAIConstants;
import com.zxzinn.novelai.utils.Cache;
import com.zxzinn.novelai.utils.I18nManager;
import com.zxzinn.novelai.config.ConfigManager;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.swing.*;
import java.awt.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class ParametersPanel extends JPanel implements StaticInputPanel {
    private static final ConfigManager config = ConfigManager.getInstance();
    private static final Cache cache = Cache.getInstance();

    // JComboBox
    private JComboBox<String> modelComboBox;
    private JComboBox<String> actionComboBox;
    private JComboBox<String> samplerComboBox;

    // JTextField
    private JTextField apiKeyField;
    private JTextField widthField;
    private JTextField heightField;
    private JTextField scaleField;
    private JTextField stepsField;
    private JTextField seedField;
    private JTextField nSamplesField;
    private JTextField outputDirField;

    // JCheckBox
    private JCheckBox smeaCheckbox;
    private JCheckBox smeaDynCheckbox;

    public ParametersPanel() {
        setLayout(new GridBagLayout());
        initComponents();
        layoutComponents();
        loadCachedValues();
    }

    @Override
    public void initComponents() {
        int columns = 20;
        // JComboBox
        modelComboBox = new JComboBox<>(NAIConstants.MODELS);
        actionComboBox = new JComboBox<>(NAIConstants.ACTIONS);

        // JTextField
        apiKeyField = new JTextField(columns);
        widthField = new JTextField(columns);
        heightField = new JTextField(columns);
        scaleField = new JTextField(columns);
        samplerComboBox = new JComboBox<>(NAIConstants.SAMPLERS);
        stepsField = new JTextField(columns);
        seedField = new JTextField(columns);
        nSamplesField = new JTextField(columns);
        outputDirField = new JTextField(20);

        // JCheckBox
        smeaCheckbox = new JCheckBox("SMEA");
        smeaDynCheckbox = new JCheckBox("SMEA DYN");
    }

    private void loadCachedValues() {
        modelComboBox.setSelectedItem(cache.getParameter("model", "nai-diffusion-3"));
        actionComboBox.setSelectedItem(cache.getParameter("action", "generate"));
        apiKeyField.setText(cache.getParameter("apiKey", ""));
        widthField.setText(cache.getParameter("width", String.valueOf(config.getInteger("image.width"))));
        heightField.setText(cache.getParameter("height", String.valueOf(config.getInteger("image.height"))));
        scaleField.setText(cache.getParameter("scale", String.valueOf(config.getDouble("image.scale"))));
        samplerComboBox.setSelectedItem(cache.getParameter("sampler", "k_euler"));
        stepsField.setText(cache.getParameter("steps", String.valueOf(config.getInteger("image.steps"))));
        seedField.setText(cache.getParameter("seed", "0"));
        nSamplesField.setText(cache.getParameter("nSamples", String.valueOf(config.getInteger("image.samples"))));
        outputDirField.setText(cache.getParameter("outputDir", config.getString("output.directory")));
        smeaCheckbox.setSelected(Boolean.parseBoolean(cache.getParameter("smea", "false")));
        smeaDynCheckbox.setSelected(Boolean.parseBoolean(cache.getParameter("smeaDyn", "false")));
    }

    public void saveToCache() {
        cache.setParameter("model", (String) modelComboBox.getSelectedItem());
        cache.setParameter("action", (String) actionComboBox.getSelectedItem());
        cache.setParameter("apiKey", apiKeyField.getText());
        cache.setParameter("width", widthField.getText());
        cache.setParameter("height", heightField.getText());
        cache.setParameter("scale", scaleField.getText());
        cache.setParameter("sampler", (String) samplerComboBox.getSelectedItem());
        cache.setParameter("steps", stepsField.getText());
        cache.setParameter("seed", seedField.getText());
        cache.setParameter("nSamples", nSamplesField.getText());
        cache.setParameter("outputDir", outputDirField.getText());
        cache.setParameter("smea", String.valueOf(smeaCheckbox.isSelected()));
        cache.setParameter("smeaDyn", String.valueOf(smeaDynCheckbox.isSelected()));
        cache.saveCache();
    }

    @Override
    public void layoutComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 1;

        int gridy = 0;
        addComponentWithLabel(I18nManager.getString("param.apiKey"), apiKeyField, gbc, gridy++);
        addComponentWithLabel(I18nManager.getString("param.model"), modelComboBox, gbc, gridy++);
        addComponentWithLabel(I18nManager.getString("param.action"), actionComboBox, gbc, gridy++);
        addComponentWithLabel(I18nManager.getString("param.width"), widthField, gbc, gridy++);
        addComponentWithLabel(I18nManager.getString("param.height"), heightField, gbc, gridy++);
        addComponentWithLabel(I18nManager.getString("param.scale"), scaleField, gbc, gridy++);
        addComponentWithLabel(I18nManager.getString("param.sampler"), samplerComboBox, gbc, gridy++);
        addComponentWithLabel(I18nManager.getString("param.steps"), stepsField, gbc, gridy++);
        addComponentWithLabel(I18nManager.getString("param.seed"), seedField, gbc, gridy++);
        addComponentWithLabel(I18nManager.getString("param.n_samples"), nSamplesField, gbc, gridy++);

        // 修改 checkboxes 的添加方式
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        add(smeaCheckbox, gbc);
        gbc.gridx = 1;
        add(smeaDynCheckbox, gbc);

        // Reset gridwidth and add Output Directory
        gbc.gridwidth = 1;
        addComponentWithLabel(I18nManager.getString("param.outputDir"), outputDirField, gbc, gridy++);

        // Add vertical glue
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = gridy;
        add(Box.createVerticalGlue(), gbc);
    }

    private void addComponentWithLabel(String labelText, JComponent component, GridBagConstraints gbc, int y) {
        gbc.gridx = 0;
        gbc.gridy = y;
        if (labelText != null && !labelText.isEmpty()) {
            add(new JLabel(labelText), gbc);
            gbc.gridx = 1;
        }
        add(component, gbc);
    }
}