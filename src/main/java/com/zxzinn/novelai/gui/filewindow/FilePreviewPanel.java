package com.zxzinn.novelai.gui.filewindow;

import com.zxzinn.novelai.gui.common.ImagePreviewPanel;
import com.zxzinn.novelai.gui.common.DocumentPreviewPanel;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

@Log4j2
public class FilePreviewPanel extends JPanel {
    private final ImagePreviewPanel imagePreviewPanel;
    private final DocumentPreviewPanel documentPreviewPanel;
    private final JLabel unsupportedFileLabel;
    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    @Getter
    private File currentFile;

    private static final String IMAGE_PANEL = "IMAGE_PANEL";
    private static final String DOCUMENT_PANEL = "DOCUMENT_PANEL";
    private static final String UNSUPPORTED_PANEL = "UNSUPPORTED_PANEL";

    public FilePreviewPanel() {
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        imagePreviewPanel = new ImagePreviewPanel();
        documentPreviewPanel = new DocumentPreviewPanel();
        unsupportedFileLabel = new JLabel("Unsupported file type", SwingConstants.CENTER);

        contentPanel.add(imagePreviewPanel, IMAGE_PANEL);
        contentPanel.add(documentPreviewPanel, DOCUMENT_PANEL);
        contentPanel.add(unsupportedFileLabel, UNSUPPORTED_PANEL);

        add(contentPanel, BorderLayout.CENTER);
    }

    public void previewFile(File file) {
        currentFile = file;
        if (file == null || !file.exists()) {
            showUnsupportedFile();
            return;
        }

        if (isImageFile(file)) {
            showImage(file);
        } else if (isTextOrCodeFile(file)) {
            showDocument(file);
        } else {
            showUnsupportedFile();
        }
    }

    public void refreshContent() {
        if (currentFile != null && currentFile.exists()) {
            if (isTextOrCodeFile(currentFile)) {
                documentPreviewPanel.refreshContent();
            } else {
                previewFile(currentFile);
            }
        }
    }

    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                name.endsWith(".gif") || name.endsWith(".bmp");
    }

    private boolean isTextOrCodeFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".txt") || name.endsWith(".java") || name.endsWith(".py") ||
                name.endsWith(".js") || name.endsWith(".html") || name.endsWith(".css") ||
                name.endsWith(".xml") || name.endsWith(".json") || name.endsWith(".yml") ||
                name.endsWith(".yaml") || name.endsWith(".md") || name.endsWith(".sql") ||
                name.endsWith(".sh") || name.endsWith(".properties");
    }

    private void showImage(File file) {
        try {
            imagePreviewPanel.loadImage(file);
            cardLayout.show(contentPanel, IMAGE_PANEL);
            documentPreviewPanel.setThemeSelectorVisible(false);
        } catch (IOException e) {
            log.error("Error loading image: " + e.getMessage());
            showUnsupportedFile();
        }
    }

    private void showDocument(File file) {
        try {
            documentPreviewPanel.loadFile(file);
            cardLayout.show(contentPanel, DOCUMENT_PANEL);
            documentPreviewPanel.setThemeSelectorVisible(true);
        } catch (IOException e) {
            log.error("Error reading text file: " + e.getMessage());
            showUnsupportedFile();
        }
    }

    private void showUnsupportedFile() {
        cardLayout.show(contentPanel, UNSUPPORTED_PANEL);
        documentPreviewPanel.setThemeSelectorVisible(false);
    }
}