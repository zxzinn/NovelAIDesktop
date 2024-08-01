package com.zxzinn.novelai.gui.generationwindow;

import com.zxzinn.novelai.api.NAIConstants;
import com.zxzinn.novelai.utils.I18nManager;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;

@Getter
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

    public GenerationParametersPanel() {
        super();
        initComponents();
        layoutComponents();
        loadCachedValues();
    }

    private void initComponents() {
        widthField = new JTextField(20);
        heightField = new JTextField(20);
        scaleField = new JTextField(20);
        samplerComboBox = new JComboBox<>(NAIConstants.SAMPLERS);
        stepsField = new JTextField(20);
        seedField = new JTextField(20);
        nSamplesField = new JTextField(20);
        smeaCheckbox = new JCheckBox("SMEA");
        smeaDynCheckbox = new JCheckBox("SMEA DYN");
    }

    private void layoutComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;

        int gridy = 3; // Start after common components
        addComponentWithLabel(I18nManager.getString("param.width"), widthField, gbc, gridy++);
        addComponentWithLabel(I18nManager.getString("param.height"), heightField, gbc, gridy++);
        addComponentWithLabel(I18nManager.getString("param.scale"), scaleField, gbc, gridy++);
        addComponentWithLabel(I18nManager.getString("param.sampler"), samplerComboBox, gbc, gridy++);
        addComponentWithLabel(I18nManager.getString("param.steps"), stepsField, gbc, gridy++);
        addComponentWithLabel(I18nManager.getString("param.seed"), seedField, gbc, gridy++);
        addComponentWithLabel(I18nManager.getString("param.n_samples"), nSamplesField, gbc, gridy++);

        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkboxPanel.add(smeaCheckbox);
        checkboxPanel.add(smeaDynCheckbox);
        gbc.gridwidth = 2;
        gbc.gridy = gridy;
        add(checkboxPanel, gbc);
    }

    @Override
    public void loadCachedValues() {
        super.loadCommonCachedValues();
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
    public void saveToCache() {
        super.saveCommonToCache();
        cache.setParameter("width", widthField.getText());
        cache.setParameter("height", heightField.getText());
        cache.setParameter("scale", scaleField.getText());
        cache.setParameter("sampler", (String) samplerComboBox.getSelectedItem());
        cache.setParameter("steps", stepsField.getText());
        cache.setParameter("seed", seedField.getText());
        cache.setParameter("nSamples", nSamplesField.getText());
        cache.setParameter("smea", String.valueOf(smeaCheckbox.isSelected()));
        cache.setParameter("smeaDyn", String.valueOf(smeaDynCheckbox.isSelected()));
        cache.saveCache();
    }
}