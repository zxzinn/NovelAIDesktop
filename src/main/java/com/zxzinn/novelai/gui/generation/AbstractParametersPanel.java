package com.zxzinn.novelai.gui.generation;

import com.zxzinn.novelai.api.NAIConstants;
import com.zxzinn.novelai.config.ConfigManager;
import com.zxzinn.novelai.event.ParameterChangeEvent;
import com.zxzinn.novelai.event.ParameterChangeListener;
import com.zxzinn.novelai.utils.Cache;
import com.zxzinn.novelai.utils.I18nManager;
import com.zxzinn.novelai.utils.UIComponent;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
public abstract class AbstractParametersPanel extends JPanel implements UIComponent {
    protected static final ConfigManager config = ConfigManager.getInstance();
    protected static final Cache cache = Cache.getInstance();

    // Common components
    protected JComboBox<String> modelComboBox;
    protected JTextField apiKeyField;
    protected JTextField outputDirField;
    protected JButton apiKeyHelpButton;
    protected JButton outputDirHelpButton;
    protected JButton outputDirBrowseButton;

    public abstract JTextField getApiKeyField();
    public abstract JTextField getOutputDirField();
    public abstract Map<String, Object> getParameters();

    private final List<ParameterChangeListener> parameterChangeListeners = new ArrayList<>();

    public AbstractParametersPanel() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setPreferredSize(new Dimension(300, 500));

        initializeComponents();
        layoutComponents();
        bindEvents();
        loadCachedValues();
    }

    @Override
    public void initializeComponents() {
        modelComboBox = createComboBox(NAIConstants.MODELS);
        apiKeyField = createTextField();
        outputDirField = createTextField();

        apiKeyHelpButton = createHelpButton("tooltip.apiKey");
        outputDirHelpButton = createHelpButton("tooltip.outputDir");
        outputDirBrowseButton = createBrowseButton(outputDirField);

        initSpecificComponents();
    }

    protected abstract void initSpecificComponents();

    @Override
    public void layoutComponents() {
        GridBagConstraints gbc = createGridBagConstraints();

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel apiSettingsPanel = createApiSettingsPanel();
        JPanel outputSettingsPanel = createOutputSettingsPanel();
        tabbedPane.addTab("API Settings", apiSettingsPanel);
        tabbedPane.addTab("Output Settings", outputSettingsPanel);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.2;
        gbc.fill = GridBagConstraints.BOTH;
        add(tabbedPane, gbc);

        JPanel specificSettingsPanel = createSpecificSettingsPanel();
        gbc.gridy = 1;
        gbc.weighty = 0.8;
        add(specificSettingsPanel, gbc);
    }

    protected abstract JPanel createSpecificSettingsPanel();

    private JPanel createApiSettingsPanel() {
        JPanel panel = createTitledPanel("API Settings");
        addComponentWithLabel(I18nManager.getString("param.apiKey"), apiKeyField, apiKeyHelpButton, panel);
        addComponentWithLabel(I18nManager.getString("param.model"), modelComboBox, null, panel);
        return panel;
    }

    private JPanel createOutputSettingsPanel() {
        JPanel panel = createTitledPanel("Output Settings");
        addComponentWithLabel(I18nManager.getString("param.outputDir"), outputDirField, outputDirHelpButton, outputDirBrowseButton, panel);
        return panel;
    }

    protected JTextField createTextField() {
        return new JTextField(20);
    }

    protected JComboBox<String> createComboBox(String[] items) {
        return new JComboBox<>(items);
    }

    protected JCheckBox createCheckBox(String text) {
        return new JCheckBox(text);
    }

    protected GridBagConstraints createGridBagConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        gbc.gridy = -1;
        return gbc;
    }

    @Override
    public void bindEvents() {
        // Common event bindings can be added here
        modelComboBox.addActionListener(e -> fireParameterChangeEvent(new ParameterChangeEvent(this, "model", modelComboBox.getSelectedItem())));

        apiKeyField.getDocument().addDocumentListener((SimpleDocumentListener) () -> fireParameterChangeEvent(new ParameterChangeEvent(this, "apiKey", apiKeyField.getText())));

        outputDirField.getDocument().addDocumentListener((SimpleDocumentListener) () -> fireParameterChangeEvent(new ParameterChangeEvent(this, "outputDir", outputDirField.getText())));

        bindSpecificEvents();
    }

    protected void fireParameterChangeEvent(ParameterChangeEvent event) {
        for (ParameterChangeListener listener : parameterChangeListeners) {
            listener.onParameterChange(event);
        }
    }

    protected abstract void bindSpecificEvents();

    public void loadCachedValues() {
        modelComboBox.setSelectedItem(cache.getParameter("model", "nai-diffusion-3"));
        apiKeyField.setText(cache.getParameter("apiKey", ""));
        outputDirField.setText(cache.getParameter("outputDir", config.getString("output.directory")));
        loadSpecificCachedValues();
    }

    protected abstract void loadSpecificCachedValues();

    public void saveToCache() {
        cache.setParameter("model", (String) modelComboBox.getSelectedItem());
        cache.setParameter("apiKey", apiKeyField.getText());
        cache.setParameter("outputDir", outputDirField.getText());
        saveSpecificToCache();
    }

    protected abstract void saveSpecificToCache();

    protected JButton createHelpButton(String tooltipKey) {
        JButton helpButton = new JButton("?");
        helpButton.setToolTipText(I18nManager.getString(tooltipKey));
        // 去除按鈕的焦點繪製，使其視覺更加簡潔
        helpButton.setFocusPainted(false);
        helpButton.setMargin(new Insets(0, 0, 0, 0));
        helpButton.setPreferredSize(new Dimension(20, 20));
        return helpButton;
    }

    protected JButton createBrowseButton(JTextField textField) {
        JButton browseButton = new JButton("...");
        // 去除按鈕的焦點繪製，使其視覺更加簡潔
        browseButton.setFocusPainted(false);
        browseButton.setMargin(new Insets(0, 0, 0, 0));
        browseButton.setPreferredSize(new Dimension(20, 20));

        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        return browseButton;
    }

    JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        return panel;
    }

    protected void addComponentWithLabel(String labelText, JComponent component, JComponent helpButton, JPanel panel) {
        addComponentWithLabel(labelText, component, panel);
        GridBagConstraints gbc = new GridBagConstraints();

        if (helpButton != null) {
            gbc.gridx = 2;
            gbc.weightx = 0.0;
            panel.add(helpButton, gbc);
        }
    }

    static void addComponentWithLabel(String labelText, JComponent component, JPanel panel) {
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

    protected void addComponentWithLabel(String labelText, JComponent component, JComponent helpButton, JComponent browseButton, JPanel panel) {
        addComponentWithLabel(labelText, component, helpButton, panel);
        if (browseButton != null) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridx = 3;
            gbc.weightx = 0.0;
            panel.add(browseButton, gbc);
        }
    }
}