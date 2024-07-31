package com.zxzinn.novelai.gui.filewindow;

import com.zxzinn.novelai.utils.I18nManager;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;

public class ControlPanel extends JPanel {
    private JTextField pathField;
    private JButton addPathButton;
    private JButton removePathButton;
    @Getter
    private JButton clearMetadataButton;
    @Getter
    private JButton constructDatabaseButton;
    private final FileManagerTab fileManagerTab;

    public ControlPanel(FileManagerTab fileManagerTab) {
        this.fileManagerTab = fileManagerTab;
        setLayout(new GridBagLayout());
        initComponents();
        layoutComponents();
    }

    private void initComponents() {
        pathField = new JTextField(15);
        addPathButton = new JButton("+");
        addPathButton.setToolTipText(I18nManager.getString("fileManager.addPath"));
        removePathButton = new JButton("-");
        removePathButton.setToolTipText(I18nManager.getString("fileManager.removePath"));
        clearMetadataButton = new JButton(I18nManager.getString("fileManager.clearMetadata"));
        constructDatabaseButton = new JButton(I18nManager.getString("fileManager.constructDatabase"));

        addPathButton.addActionListener(e -> fileManagerTab.addPath(pathField.getText().trim()));
        removePathButton.addActionListener(e -> fileManagerTab.removePath());
        clearMetadataButton.addActionListener(e -> fileManagerTab.clearMetadata());
        constructDatabaseButton.addActionListener(e -> fileManagerTab.constructDatabase());
    }

    private void layoutComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);

        add(pathField, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.0;
        add(addPathButton, gbc);

        gbc.gridx = 2;
        add(removePathButton, gbc);

        gbc.gridx = 0;
        gbc.gridwidth = 3;

        gbc.gridy = 1;
        add(clearMetadataButton, gbc);

        gbc.gridy = 2;
        add(constructDatabaseButton, gbc);
    }

}