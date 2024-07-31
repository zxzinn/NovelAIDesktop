package com.zxzinn.novelai.gui.filewindow;

import com.zxzinn.novelai.utils.I18nManager;
import com.zxzinn.novelai.utils.MetadataReader;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Optional;

@Log4j2
public class MetadataDisplayPanel extends JPanel {
    @Getter
    private File currentFile;

    private JTextArea metadataArea;
    private JTextArea commentArea;
    private JButton actionButton;
    private JScrollPane commentScrollPane;
    private String rawMetadata;

    public MetadataDisplayPanel() {
        setLayout(new BorderLayout());
        initComponents();
        layoutComponents();
    }

    private void initComponents() {
        metadataArea = new JTextArea(20, 30);
        metadataArea.setEditable(false);

        commentArea = new JTextArea(10, 60);
        commentArea.setEditable(false);
        commentScrollPane = new JScrollPane(commentArea);
        commentScrollPane.setVisible(false);

        actionButton = new JButton(I18nManager.getString("metadata.showComment"));
        actionButton.addActionListener(e -> toggleCommentVisibility());

        actionButton.setEnabled(false);
    }

    private void layoutComponents() {
        add(new JScrollPane(metadataArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(actionButton);
        add(buttonPanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(new JScrollPane(metadataArea), BorderLayout.CENTER);
        mainPanel.add(commentScrollPane, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    public void refreshMetadata() {
        if (currentFile != null) {
            displayMetadata(currentFile);
        }
    }

    public void displayMetadata(File file) {
        currentFile = file;
        log.debug("Displaying metadata for file: {}", file.getAbsolutePath());
        Optional<String> metadataOpt = MetadataReader.extractMetadata(file);
        if (metadataOpt.isPresent()) {
            rawMetadata = metadataOpt.get();
            metadataArea.setText(rawMetadata);
            actionButton.setEnabled(true);
            log.debug("Metadata extracted and displayed");
        } else {
            metadataArea.setText("No metadata available or unable to read metadata");
            actionButton.setEnabled(false);
            log.debug("No metadata available or unable to read metadata");
        }
    }

    private void toggleCommentVisibility() {
        boolean isVisible = commentScrollPane.isVisible();
        commentScrollPane.setVisible(!isVisible);
        actionButton.setText(isVisible ?
                I18nManager.getString("metadata.showComment") :
                I18nManager.getString("metadata.hideComment"));
        if (!isVisible) {
            Optional<String> formattedCommentOpt = MetadataReader.formatCommentJson(rawMetadata);
            if (formattedCommentOpt.isPresent()) {
                commentArea.setText(formattedCommentOpt.get());
            } else {
                commentArea.setText("No comment data found or unable to format comment");
            }
        }
        revalidate();
        repaint();
    }
}