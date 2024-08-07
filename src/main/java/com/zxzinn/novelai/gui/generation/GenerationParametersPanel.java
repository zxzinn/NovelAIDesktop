package com.zxzinn.novelai.gui.generation;

import com.zxzinn.novelai.api.NAIConstants;
import com.zxzinn.novelai.utils.I18nManager;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class GenerationParametersPanel extends AbstractParametersPanel {
    private JTextField widthField;
    private JTextField heightField;
    private JTextField scaleField;
    private JComboBox<String> samplerComboBox;
    private JTextField stepsField;
    private JTextField seedField;
    private JTextField nSamplesField;
    private JCheckBox smeaCheckbox;
    private JCheckBox smeaDynCheckbox;

    @Override
    protected void initSpecificComponents() {
        widthField = createTextField();
        heightField = createTextField();
        scaleField = createTextField();
        samplerComboBox = createComboBox(NAIConstants.SAMPLERS);
        stepsField = createTextField();
        seedField = createTextField();
        nSamplesField = createTextField();
        smeaCheckbox = createCheckBox("SMEA");
        smeaDynCheckbox = createCheckBox("SMEA DYN");
    }

    @Override
    protected JPanel createSpecificSettingsPanel() {
        JPanel specificSettingsPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = createGridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        specificSettingsPanel.add(createImageSettingsPanel(), gbc);

        gbc.gridy = 1;
        specificSettingsPanel.add(createSamplingSettingsPanel(), gbc);

        return specificSettingsPanel;
    }

    private JPanel createImageSettingsPanel() {
        JPanel panel = createTitledPanel("Image Settings");
        addComponentWithLabel(I18nManager.getString("param.width"), widthField, panel);
        addComponentWithLabel(I18nManager.getString("param.height"), heightField, panel);
        addComponentWithLabel(I18nManager.getString("param.scale"), scaleField, panel);
        addComponentWithLabel(I18nManager.getString("param.n_samples"), nSamplesField, panel);
        return panel;
    }

    private JPanel createSamplingSettingsPanel() {
        JPanel panel = createTitledPanel("Sampling Settings");
        addComponentWithLabel(I18nManager.getString("param.sampler"), samplerComboBox, panel);
        addComponentWithLabel(I18nManager.getString("param.steps"), stepsField, panel);
        addComponentWithLabel(I18nManager.getString("param.seed"), seedField, panel);
        panel.add(smeaCheckbox);
        panel.add(smeaDynCheckbox);
        return panel;
    }

    public static void addComponentWithLabel(String labelText, JComponent component, JPanel panel) {
        Img2ImgParametersPanel.addComponentWithLabel(labelText, component, panel);
    }

    @Override
    protected void bindSpecificEvents() {
        // Add any specific event bindings here
    }

    @Override
    protected void loadSpecificCachedValues() {
        widthField.setText(cache.getParameter("width", String.valueOf(config.getInteger("image.width"))));
        heightField.setText(cache.getParameter("height", String.valueOf(config.getInteger("image.height"))));
        scaleField.setText(cache.getParameter("scale", String.valueOf(config.getDouble("image.scale"))));
        samplerComboBox.setSelectedItem(cache.getParameter("sampler", "k_euler"));
        stepsField.setText(cache.getParameter("steps", String.valueOf(config.getInteger("image.steps"))));
        seedField.setText(cache.getParameter("seed", "0"));
        nSamplesField.setText(cache.getParameter("nSamples", String.valueOf(config.getInteger("image.samples"))));
        smeaCheckbox.setSelected(Boolean.parseBoolean(cache.getParameter("smea", "false")));
        smeaDynCheckbox.setSelected(Boolean.parseBoolean(cache.getParameter("smeaDyn", "false")));
    }

    @Override
    protected void saveSpecificToCache() {
        cache.setParameter("width", widthField.getText());
        cache.setParameter("height", heightField.getText());
        cache.setParameter("scale", scaleField.getText());
        cache.setParameter("sampler", (String) samplerComboBox.getSelectedItem());
        cache.setParameter("steps", stepsField.getText());
        cache.setParameter("seed", seedField.getText());
        cache.setParameter("nSamples", nSamplesField.getText());
        cache.setParameter("smea", String.valueOf(smeaCheckbox.isSelected()));
        cache.setParameter("smeaDyn", String.valueOf(smeaDynCheckbox.isSelected()));
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("model", modelComboBox.getSelectedItem());
        params.put("width", Integer.parseInt(widthField.getText()));
        params.put("height", Integer.parseInt(heightField.getText()));
        params.put("scale", Double.parseDouble(scaleField.getText()));
        params.put("sampler", samplerComboBox.getSelectedItem());
        params.put("steps", Integer.parseInt(stepsField.getText()));
        params.put("seed", Long.parseLong(seedField.getText()));
        params.put("n_samples", Integer.parseInt(nSamplesField.getText()));
        params.put("sm", smeaCheckbox.isSelected());
        params.put("sm_dyn", smeaDynCheckbox.isSelected());
        return params;
    }

    @Override
    public JTextField getApiKeyField() {
        return apiKeyField;
    }

    @Override
    public JTextField getOutputDirField() {
        return outputDirField;
    }
}