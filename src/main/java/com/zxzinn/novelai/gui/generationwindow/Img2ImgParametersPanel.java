package com.zxzinn.novelai.gui.generationwindow;

import com.zxzinn.novelai.api.NAIConstants;
import com.zxzinn.novelai.utils.I18nManager;
import lombok.Getter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Img2ImgParametersPanel extends AbstractParametersPanel {
    private JTextField widthField;
    private JTextField heightField;
    private JTextField scaleField;
    private JComboBox<String> samplerComboBox;
    private JTextField stepsField;
    private JTextField seedField;
    private JTextField nSamplesField;
    private JTextField extraNoiseSeedField;
    private JCheckBox smeaCheckbox;
    private JCheckBox smeaDynCheckbox;
    private JButton uploadImageButton;
    @Getter
    private String base64Image;

    @Override
    protected void initSpecificComponents() {
        widthField = new JTextField(20);
        heightField = new JTextField(20);
        scaleField = new JTextField(20);
        samplerComboBox = new JComboBox<>(NAIConstants.SAMPLERS);
        stepsField = new JTextField(20);
        seedField = new JTextField(20);
        nSamplesField = new JTextField(20);
        extraNoiseSeedField = new JTextField(20);
        smeaCheckbox = new JCheckBox("SMEA");
        smeaDynCheckbox = new JCheckBox("SMEA DYN");
        uploadImageButton = new JButton(I18nManager.getString("button.uploadImage"));
    }

    @Override
    protected void layoutSpecificComponents(GridBagConstraints gbc, int startGridy) {
        int gridy = startGridy;

        JPanel imageSettingsPanel = createTitledPanel("Image Settings");
        addComponentWithLabel(I18nManager.getString("param.width"), widthField, imageSettingsPanel);
        addComponentWithLabel(I18nManager.getString("param.height"), heightField, imageSettingsPanel);
        addComponentWithLabel(I18nManager.getString("param.scale"), scaleField, imageSettingsPanel);
        addComponentWithLabel(I18nManager.getString("param.n_samples"), nSamplesField, imageSettingsPanel);

        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        add(imageSettingsPanel, gbc);

        JPanel samplingSettingsPanel = createTitledPanel("Sampling Settings");
        addComponentWithLabel(I18nManager.getString("param.sampler"), samplerComboBox, samplingSettingsPanel);
        addComponentWithLabel(I18nManager.getString("param.steps"), stepsField, samplingSettingsPanel);
        addComponentWithLabel(I18nManager.getString("param.seed"), seedField, samplingSettingsPanel);
        addComponentWithLabel(I18nManager.getString("param.extraNoiseSeed"), extraNoiseSeedField, samplingSettingsPanel);
        samplingSettingsPanel.add(smeaCheckbox);
        samplingSettingsPanel.add(smeaDynCheckbox);

        gbc.gridy = gridy++;
        add(samplingSettingsPanel, gbc);

        gbc.gridy = gridy;
        add(uploadImageButton, gbc);
    }

    private void addComponentWithLabel(String labelText, JComponent component, JPanel panel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.weightx = 0.0;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(component, gbc);
    }

    @Override
    protected void bindSpecificEvents() {
        uploadImageButton.addActionListener(e -> uploadImage());
    }

    @Override
    protected void loadSpecificCachedValues() {
        widthField.setText(cache.getParameter("img2img.width", String.valueOf(config.getInteger("image.width"))));
        heightField.setText(cache.getParameter("img2img.height", String.valueOf(config.getInteger("image.height"))));
        scaleField.setText(cache.getParameter("img2img.scale", String.valueOf(config.getDouble("image.scale"))));
        samplerComboBox.setSelectedItem(cache.getParameter("img2img.sampler", "k_euler"));
        stepsField.setText(cache.getParameter("img2img.steps", String.valueOf(config.getInteger("image.steps"))));
        seedField.setText(cache.getParameter("img2img.seed", "0"));
        nSamplesField.setText(cache.getParameter("img2img.nSamples", String.valueOf(config.getInteger("image.samples"))));
        extraNoiseSeedField.setText(cache.getParameter("img2img.extraNoiseSeed", "0"));
        smeaCheckbox.setSelected(Boolean.parseBoolean(cache.getParameter("img2img.smea", "false")));
        smeaDynCheckbox.setSelected(Boolean.parseBoolean(cache.getParameter("img2img.smeaDyn", "false")));
    }

    @Override
    protected void saveSpecificToCache() {
        cache.setParameter("img2img.width", widthField.getText());
        cache.setParameter("img2img.height", heightField.getText());
        cache.setParameter("img2img.scale", scaleField.getText());
        cache.setParameter("img2img.sampler", (String) samplerComboBox.getSelectedItem());
        cache.setParameter("img2img.steps", stepsField.getText());
        cache.setParameter("img2img.seed", seedField.getText());
        cache.setParameter("img2img.nSamples", nSamplesField.getText());
        cache.setParameter("img2img.extraNoiseSeed", extraNoiseSeedField.getText());
        cache.setParameter("img2img.smea", String.valueOf(smeaCheckbox.isSelected()));
        cache.setParameter("img2img.smeaDyn", String.valueOf(smeaDynCheckbox.isSelected()));
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
        params.put("extra_noise_seed", Long.parseLong(extraNoiseSeedField.getText()));
        params.put("image", getBase64Image());
        return params;
    }

    private void uploadImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                BufferedImage image = ImageIO.read(selectedFile);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                byte[] imageBytes = baos.toByteArray();
                base64Image = Base64.getEncoder().encodeToString(imageBytes);
                uploadImageButton.setText("Image uploaded: " + selectedFile.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error uploading image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
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