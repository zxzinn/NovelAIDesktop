package com.zxzinn.novelai.gui.common;

import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class SyntaxHighlightTextArea extends JPanel {
    private final RSyntaxTextArea textArea;
    private final RTextScrollPane scrollPane;

    public enum Theme {
        INTELLIJ_DARK, MONOKAI, DARCULA;

        @Override
        public String toString() {
            return name().charAt(0) + name().substring(1).toLowerCase().replace('_', ' ');
        }
    }

    public SyntaxHighlightTextArea() {
        setLayout(new BorderLayout());
        textArea = new RSyntaxTextArea(20, 60);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);

        scrollPane = new RTextScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        applyTheme(Theme.INTELLIJ_DARK); // 默認使用 IntelliJ Dark 主題
    }

    public void applyTheme(Theme theme) {
        try {
            String themeXml = "/org/fife/ui/rsyntaxtextarea/themes/" + theme.name().toLowerCase().replace('_', '-') + ".xml";
            org.fife.ui.rsyntaxtextarea.Theme rsyntaxTheme = org.fife.ui.rsyntaxtextarea.Theme.load(getClass().getResourceAsStream(themeXml));
            rsyntaxTheme.apply(textArea);

            // 額外的自定義設置
            textArea.setCurrentLineHighlightColor(new Color(44, 45, 48));
            textArea.setSelectionColor(new Color(33, 66, 131));
            textArea.setCaretColor(new Color(187, 187, 187));

            // 設置字體
            Font font = new Font("JetBrains Mono", Font.PLAIN, 14);
            textArea.setFont(font);

        } catch (IOException e) {
            log.error("Failed to load or apply theme", e);
        }
    }

    public static Theme[] getAvailableThemes() {
        return Theme.values();
    }

    public void loadFile(File file) throws IOException {
        String content = Files.readString(file.toPath());
        textArea.setText(content);
        textArea.setCaretPosition(0);
        setSyntaxStyle(file.getName());
    }

    private void setSyntaxStyle(String fileName) {
        String extension = getFileExtension(fileName);
        String style = switch (extension.toLowerCase()) {
            case "java" -> SyntaxConstants.SYNTAX_STYLE_JAVA;
            case "py" -> SyntaxConstants.SYNTAX_STYLE_PYTHON;
            case "js" -> SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
            case "html" -> SyntaxConstants.SYNTAX_STYLE_HTML;
            case "css" -> SyntaxConstants.SYNTAX_STYLE_CSS;
            case "xml" -> SyntaxConstants.SYNTAX_STYLE_XML;
            case "json" -> SyntaxConstants.SYNTAX_STYLE_JSON;
            case "yml", "yaml" -> SyntaxConstants.SYNTAX_STYLE_YAML;
            case "md" -> SyntaxConstants.SYNTAX_STYLE_MARKDOWN;
            case "sql" -> SyntaxConstants.SYNTAX_STYLE_SQL;
            case "sh" -> SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL;
            case "properties" -> SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE;
            default -> SyntaxConstants.SYNTAX_STYLE_NONE;
        };
        textArea.setSyntaxEditingStyle(style);
    }

    private String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return fileName.substring(lastIndexOf + 1);
    }
}