package com.zxzinn.novelai.gui.filewindow;

import com.zxzinn.novelai.utils.I18nManager;
import com.zxzinn.novelai.utils.UIComponent;

import javax.swing.*;
import java.awt.*;

public class ControlPanel extends JPanel implements UIComponent {
    private JTextField pathField;
    private JButton addPathButton;
    private JButton removePathButton;
    private JButton clearMetadataButton;
    private JButton constructDatabaseButton;
    private final FileManagerTab fileManagerTab;

    public ControlPanel(FileManagerTab fileManagerTab) {
        this.fileManagerTab = fileManagerTab;

        initializeComponents();
        layoutComponents();
        bindEvents();
    }

    @Override
    public void initializeComponents() {
        pathField = new JTextField(15);
        addPathButton = new JButton("+");
        addPathButton.setToolTipText(I18nManager.getString("fileManager.addPath"));
        removePathButton = new JButton("-");
        removePathButton.setToolTipText(I18nManager.getString("fileManager.removePath"));
        clearMetadataButton = new JButton(I18nManager.getString("fileManager.clearMetadata"));
        constructDatabaseButton = new JButton(I18nManager.getString("fileManager.constructDatabase"));
    }

    @Override
    public void layoutComponents() {
        setLayout(new GridBagLayout());
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

    @Override
    public void bindEvents() {
        addPathButton.addActionListener(e -> fileManagerTab.addPath(pathField.getText().trim()));
        removePathButton.addActionListener(e -> fileManagerTab.removePath());
        clearMetadataButton.addActionListener(e -> fileManagerTab.clearMetadata());
        constructDatabaseButton.addActionListener(e -> fileManagerTab.constructDatabase());
    }

    @Override
    public JComponent getComponent() {
        return this;
    }
}