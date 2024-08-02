package com.zxzinn.novelai.gui.generation;

import com.zxzinn.novelai.gui.common.ImagePreviewPanel;
import com.zxzinn.novelai.utils.UIComponent;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

@Log4j2
public class HistoryPanel extends JPanel implements UIComponent {
    private final JPanel thumbnailPanel;
    private final ImagePreviewPanel imagePreviewPanel;
    private static final int THUMBNAIL_WIDTH = 100;
    private static final int PANEL_WIDTH = 120;

    public HistoryPanel(ImagePreviewPanel imagePreviewPanel) {
        this.imagePreviewPanel = imagePreviewPanel;
        thumbnailPanel = new JPanel();

        initializeComponents();
        layoutComponents();
        bindEvents();
    }

    @Override
    public void initializeComponents() {
        setPreferredSize(new Dimension(PANEL_WIDTH, 0));
        thumbnailPanel.setLayout(new BoxLayout(thumbnailPanel, BoxLayout.Y_AXIS));
    }

    @Override
    public void layoutComponents() {
        setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(thumbnailPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void bindEvents() {
        // No specific events to bind in this panel
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    public void addImage(BufferedImage image) {
        log.debug("Adding image to HistoryPanel");
        ImageIcon thumbnail = createThumbnail(image);
        JButton button = new JButton(thumbnail);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        button.setContentAreaFilled(false);
        button.addActionListener(e -> {
            imagePreviewPanel.setImage(image);
            imagePreviewPanel.fitToPanel();
        });
        thumbnailPanel.add(button);
        thumbnailPanel.revalidate();
        thumbnailPanel.repaint();
        log.debug("Image added to HistoryPanel successfully");
    }

    private ImageIcon createThumbnail(BufferedImage image) {
        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();

        double scale = (double) THUMBNAIL_WIDTH / originalWidth;
        int scaledHeight = (int) (originalHeight * scale);

        Image scaledImage = image.getScaledInstance(THUMBNAIL_WIDTH, scaledHeight, Image.SCALE_SMOOTH);
        BufferedImage thumbnail = new BufferedImage(THUMBNAIL_WIDTH, scaledHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = thumbnail.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();

        return new ImageIcon(thumbnail);
    }
}