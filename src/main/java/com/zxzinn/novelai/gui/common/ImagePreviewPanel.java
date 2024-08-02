package com.zxzinn.novelai.gui.common;

import com.zxzinn.novelai.utils.UIComponent;
import lombok.extern.log4j.Log4j2;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Log4j2
public class ImagePreviewPanel extends JPanel implements UIComponent, MouseWheelListener, MouseListener, MouseMotionListener {
    private BufferedImage image;
    private double scale = 1.0;
    private int translateX = 0;
    private int translateY = 0;
    private Point lastPoint;

    public ImagePreviewPanel() {
        initializeComponents();
        layoutComponents();
        bindEvents();
    }

    @Override
    public void initializeComponents() {
        setBackground(Color.DARK_GRAY);
    }

    @Override
    public void layoutComponents() {
        setLayout(new BorderLayout());
    }

    @Override
    public void bindEvents() {
        addMouseWheelListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        resetView();
    }

    public void loadImage(File file) throws IOException {
        try {
            this.image = ImageIO.read(file);
            resetView();
        } catch (IOException e) {
            log.error("Error loading image: " + e.getMessage());
            throw e;
        }
    }

    private void resetView() {
        if (image != null) {
            scale = 1.0;
            translateX = 0;
            translateY = 0;
            fitToPanel();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int imageWidth = (int) (image.getWidth() * scale);
            int imageHeight = (int) (image.getHeight() * scale);

            int x = (panelWidth - imageWidth) / 2 + translateX;
            int y = (panelHeight - imageHeight) / 2 + translateY;

            g2d.translate(x, y);
            g2d.scale(scale, scale);
            g2d.drawImage(image, 0, 0, this);
            g2d.dispose();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (image != null) {
            Point mousePoint = e.getPoint();
            double oldScale = scale;

            int notches = e.getWheelRotation();
            double scaleFactor = 1 - notches * 0.1;
            scale *= scaleFactor;
            scale = Math.max(0.1, Math.min(scale, 5.0));

            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int imageWidth = (int) (image.getWidth() * oldScale);
            int imageHeight = (int) (image.getHeight() * oldScale);
            int imageX = (panelWidth - imageWidth) / 2 + translateX;
            int imageY = (panelHeight - imageHeight) / 2 + translateY;

            double relativeX = (mousePoint.x - imageX) / oldScale;
            double relativeY = (mousePoint.y - imageY) / oldScale;

            translateX += (int) (relativeX * (oldScale - scale));
            translateY += (int) (relativeY * (oldScale - scale));

            repaint();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        lastPoint = e.getPoint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (image != null) {
            int dx = e.getX() - lastPoint.x;
            int dy = e.getY() - lastPoint.y;
            translateX += dx;
            translateY += dy;
            lastPoint = e.getPoint();
            repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {}

    public void fitToPanel() {
        if (image != null) {
            double scaleX = (double) getWidth() / image.getWidth();
            double scaleY = (double) getHeight() / image.getHeight();
            scale = Math.min(scaleX, scaleY);
            translateX = 0;
            translateY = 0;
            repaint();
        }
    }

    public void setScale(double scale) {
        this.scale = scale;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(400, 400);
    }
}