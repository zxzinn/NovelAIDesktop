package com.zxzinn.novelai.gui.generation;

import com.zxzinn.novelai.api.NAIConstants;
import com.zxzinn.novelai.config.ConfigManager;
import com.zxzinn.novelai.utils.Cache;
import com.zxzinn.novelai.utils.I18nManager;
import com.zxzinn.novelai.utils.UIComponent;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.*;
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
        apiKeyField = createTextField(20);
        outputDirField = createTextField(20);

        apiKeyHelpButton = createHelpButton("tooltip.apiKey");
        outputDirHelpButton = createHelpButton("tooltip.outputDir");
        outputDirBrowseButton = createBrowseButton(outputDirField);

        initSpecificComponents();
    }

    protected abstract void initSpecificComponents();

    @Override
    public void layoutComponents() {
        GridBagConstraints gbc = createGridBagConstraints();

        addSettingsPanel("API Settings", createApiSettingsPanel(), gbc);
        addSettingsPanel("Output Settings", createOutputSettingsPanel(), gbc);

        layoutSpecificComponents(gbc);
    }

    protected abstract void layoutSpecificComponents(GridBagConstraints gbc);

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

    protected JTextField createTextField(int columns) {
        return new JTextField(columns);
    }

    protected JComboBox<String> createComboBox(String[] items) {
        return new JComboBox<>(items);
    }

    protected JCheckBox createCheckBox(String text) {
        return new JCheckBox(text);
    }

    protected void addSettingsPanel(String title, JPanel panel, GridBagConstraints gbc) {
        gbc.gridy++;
        gbc.gridwidth = 2;
        add(panel, gbc);
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
        bindSpecificEvents();
    }

    protected abstract void bindSpecificEvents();

    @Override
    public JComponent getComponent() {
        return this;
    }

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

    protected void addComponentWithLabel(String labelText, JComponent component, GridBagConstraints gbc, int y) {
        gbc.gridy = y;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        add(component, gbc);
    }

    protected JButton createHelpButton(String tooltipKey) {
        JButton helpButton = new JButton("?");
        helpButton.setToolTipText(I18nManager.getString(tooltipKey));
        helpButton.setMargin(new Insets(0, 0, 0, 0));
        helpButton.setFocusPainted(false);
        helpButton.setPreferredSize(new Dimension(20, 20));
        return helpButton;
    }

    protected JButton createBrowseButton(JTextField textField) {
        JButton browseButton = new JButton("...");
        browseButton.setMargin(new Insets(0, 0, 0, 0));
        browseButton.setFocusPainted(false);
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

    protected void addComponentWithLabel(String labelText, JComponent component, JComponent helpButton, GridBagConstraints gbc, int y) {
        gbc.gridy = y;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        add(component, gbc);

        if (helpButton != null) {
            gbc.gridx = 2;
            gbc.weightx = 0.0;
            add(helpButton, gbc);
        }
    }

    protected void addComponentWithLabel(String labelText, JComponent component, JComponent helpButton, JComponent browseButton, GridBagConstraints gbc, int y) {
        addComponentWithLabel(labelText, component, helpButton, gbc, y);
        if (browseButton != null) {
            gbc.gridx = 3;
            gbc.weightx = 0.0;
            add(browseButton, gbc);
        }
    }

    JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        return panel;
    }

    protected void addComponentWithLabel(String labelText, JComponent component, JComponent helpButton, JPanel panel) {
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

        if (helpButton != null) {
            gbc.gridx = 2;
            gbc.weightx = 0.0;
            panel.add(helpButton, gbc);
        }
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