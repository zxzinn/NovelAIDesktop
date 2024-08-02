package com.zxzinn.novelai.gui.generation;

import com.zxzinn.novelai.utils.I18nManager;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class GenerationControlPanel extends JPanel {
    private final JButton generateButton;
    private final JComboBox<String> generationCountComboBox;

    public GenerationControlPanel(Consumer<Integer> onGenerate) {
        setLayout(new FlowLayout(FlowLayout.CENTER));

        generateButton = new JButton(I18nManager.getString("button.generate"));
        styleGenerateButton();

        String[] countOptions = {"1", "2", "3", "4", I18nManager.getString("option.infinite")};
        generationCountComboBox = createGenerationCountComboBox(countOptions);

        add(generateButton);
        add(Box.createHorizontalStrut(10));
        add(generationCountComboBox);

        generateButton.addActionListener(e -> {
            String countSelection = (String) generationCountComboBox.getSelectedItem();
            int count = countSelection.equals(I18nManager.getString("option.infinite")) ? Integer.MAX_VALUE : Integer.parseInt(countSelection);
            onGenerate.accept(count);
        });
    }

    private void styleGenerateButton() {
        generateButton.setFont(new Font(generateButton.getFont().getName(), Font.BOLD, 16));
        generateButton.setBackground(new Color(70, 130, 180));
        generateButton.setForeground(Color.WHITE);
        generateButton.setPreferredSize(new Dimension(200, 40));
        generateButton.setFocusPainted(false);
    }

    private JComboBox<String> createGenerationCountComboBox(String[] options) {
        JComboBox<String> comboBox = new JComboBox<>(options);
        comboBox.setFont(new Font(comboBox.getFont().getName(), Font.BOLD, 14));
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(Color.BLACK);
        comboBox.setPreferredSize(new Dimension(80, 40));
        ((JLabel)comboBox.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        return comboBox;
    }

    public void updateState(boolean isGenerating) {
        generateButton.setText(isGenerating ? I18nManager.getString("button.stop") : I18nManager.getString("button.generate"));
        generateButton.setEnabled(!isGenerating || !generateButton.getText().equals(I18nManager.getString("button.stopping")));
    }
}