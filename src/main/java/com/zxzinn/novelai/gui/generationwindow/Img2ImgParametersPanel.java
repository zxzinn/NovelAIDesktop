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

@Getter
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
    @Getter private JButton uploadImageButton;
    @Getter
    private String base64Image;

    public Img2ImgParametersPanel() {
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
        extraNoiseSeedField = new JTextField(20);
        smeaCheckbox = new JCheckBox("SMEA");
        smeaDynCheckbox = new JCheckBox("SMEA DYN");
        uploadImageButton = new JButton(I18nManager.getString("button.uploadImage"));
        uploadImageButton.addActionListener(e -> uploadImage());
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
        addComponentWithLabel(I18nManager.getString("param.extraNoiseSeed"), extraNoiseSeedField, gbc, gridy++);

        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkboxPanel.add(smeaCheckbox);
        checkboxPanel.add(smeaDynCheckbox);
        gbc.gridwidth = 2;
        gbc.gridy = gridy++;
        add(checkboxPanel, gbc);

        gbc.gridy = gridy;
        add(uploadImageButton, gbc);
    }

    @Override
    public void loadCachedValues() {
        super.loadCommonCachedValues();
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
    public void saveToCache() {
        super.saveCommonToCache();
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
        cache.saveCache();
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
}