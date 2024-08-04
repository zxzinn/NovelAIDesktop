package com.zxzinn.novelai.gui.generation;

import com.zxzinn.novelai.GenerationState;
import com.zxzinn.novelai.event.ImageReceivedEvent;
import com.zxzinn.novelai.event.ImageReceivedListener;
import com.zxzinn.novelai.utils.I18nManager;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@EqualsAndHashCode(callSuper = true)
@Log4j2
@Data
public class GenerationControlPanel extends JPanel {
    private final JButton controlButton;
    private final JComboBox<String> generationCountComboBox;
    private final List<ImageReceivedListener> imageReceivedListeners = new ArrayList<>();

    private GenerationState currentState;
    private final AtomicInteger lastingCount = new AtomicInteger();
    private Consumer<BufferedImage> onImageReceived;
    private Runnable onGenerateRequested;

    public GenerationControlPanel() {
        setLayout(new FlowLayout(FlowLayout.CENTER));

        controlButton = new JButton(I18nManager.getString("button.generate"));
        currentState = GenerationState.IDLE;

        String[] countOptions = {"1", "2", "3", "4", I18nManager.getString("option.infinite")};
        generationCountComboBox = new JComboBox<>(countOptions);

        add(controlButton);
        add(Box.createHorizontalStrut(10));
        add(generationCountComboBox);

        controlButton.addActionListener(e -> handleControlButtonClick());
    }

    private void handleControlButtonClick() {
        switch (currentState) {
            case IDLE:
                requestGenerate();
                break;
            case GENERATING:
                requestStop();
                break;
            case STOPPING:
                // 停止狀態下不執行操作
                break;
        }
    }

    public boolean shouldContinueGenerating() {
        return currentState == GenerationState.GENERATING && lastingCount.get() > 0;
    }

    public boolean decrementLastingCount() {
        int newCount = lastingCount.decrementAndGet();
        log.info("剩餘生成次數: {}", newCount);
        return newCount > 0;
    }

    private void requestGenerate() {
        String countSelection = (String) generationCountComboBox.getSelectedItem();
        assert countSelection != null;
        int count = countSelection.equals(I18nManager.getString("option.infinite")) ? Integer.MAX_VALUE : Integer.parseInt(countSelection);
        lastingCount.set(count);
        updateState(GenerationState.GENERATING);
        log.info("請求生成圖像。計數: {}", count);
        if (onGenerateRequested != null) {
            onGenerateRequested.run();
        }
    }

    private void requestStop() {
        log.info("請求停止生成圖像");
        lastingCount.set(0);
        updateState(GenerationState.STOPPING);
    }

    public void updateState(GenerationState newState) {
        currentState = newState;
        SwingUtilities.invokeLater(() -> {
            switch (newState) {
                case IDLE:
                    controlButton.setText(I18nManager.getString("button.generate"));
                    controlButton.setEnabled(true);
                    break;
                case GENERATING:
                    controlButton.setText(I18nManager.getString("button.stop"));
                    controlButton.setEnabled(true);
                    break;
                case STOPPING:
                    controlButton.setText(I18nManager.getString("button.stopping"));
                    controlButton.setEnabled(false);
                    break;
            }
        });
    }

    public void addImageReceivedListener(ImageReceivedListener listener) {
        imageReceivedListeners.add(listener);
    }

    public void removeImageReceivedListener(ImageReceivedListener listener) {
        imageReceivedListeners.remove(listener);
    }

    public void onImageReceived(BufferedImage image) {
        ImageReceivedEvent event = new ImageReceivedEvent(this, image);
        for (ImageReceivedListener listener : imageReceivedListeners) {
            listener.onImageReceived(event);
        }
    }
}