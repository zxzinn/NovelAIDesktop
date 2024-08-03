package com.zxzinn.novelai.gui.generation;

import com.zxzinn.novelai.utils.I18nManager;

import javax.swing.*;
import java.awt.*;

public class GenerationControlPanel extends JPanel {
    private final JButton controlButton;
    private final JComboBox<String> generationCountComboBox;

    public GenerationControlPanel() {
        setLayout(new FlowLayout(FlowLayout.CENTER));

        controlButton = new JButton(I18nManager.getString("button.generate"));

        String[] countOptions = {"1", "2", "3", "4", I18nManager.getString("option.infinite")};
        generationCountComboBox = new JComboBox<>(countOptions);

        add(controlButton);
        add(Box.createHorizontalStrut(10));
        add(generationCountComboBox);

        controlButton.addActionListener(e -> {
            String countSelection = (String) generationCountComboBox.getSelectedItem();
            assert countSelection != null;
        });
    }

    public void updateState(boolean isGenerating) {
        controlButton.setText(isGenerating ? I18nManager.getString("button.stop") : I18nManager.getString("button.generate"));
        controlButton.setEnabled(!isGenerating || !controlButton.getText().equals(I18nManager.getString("button.stopping")));
    }
}