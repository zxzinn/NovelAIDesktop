package com.zxzinn.novelai.gui.common;

import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

@Log4j2
public class DocumentPreviewPanel extends JPanel {
    private final SyntaxHighlightTextArea syntaxHighlightTextArea;
    private final JComboBox<SyntaxHighlightTextArea.Theme> themeSelector;
    private File currentFile;

    public DocumentPreviewPanel() {
        setLayout(new BorderLayout());

        syntaxHighlightTextArea = new SyntaxHighlightTextArea();
        add(new JScrollPane(syntaxHighlightTextArea), BorderLayout.CENTER);

        themeSelector = new JComboBox<>(SyntaxHighlightTextArea.getAvailableThemes());
        themeSelector.addActionListener(e -> {
            SyntaxHighlightTextArea.Theme selectedTheme = (SyntaxHighlightTextArea.Theme) themeSelector.getSelectedItem();
            assert selectedTheme != null;
            syntaxHighlightTextArea.applyTheme(selectedTheme);
        });

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.add(new JLabel("Theme: "));
        controlPanel.add(themeSelector);
        add(controlPanel, BorderLayout.NORTH);
    }

    public void loadFile(File file) throws IOException {
        currentFile = file;
        syntaxHighlightTextArea.loadFile(file);
    }

    public void refreshContent() {
        if (currentFile != null && currentFile.exists()) {
            try {
                loadFile(currentFile);
            } catch (IOException e) {
                log.error("Error refreshing file content: " + e.getMessage());
            }
        }
    }

    public void setThemeSelectorVisible(boolean visible) {
        themeSelector.setVisible(visible);
    }
}