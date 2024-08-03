package com.zxzinn.novelai.gui.generation;

import com.zxzinn.novelai.generation.prompt.PromptProcessor;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
public class PromptPreviewManager {
    private final PromptProcessor promptProcessor;
    private final ExecutorService executorService;
    private final List<PreviewUpdateListener> listeners;

    public PromptPreviewManager(PromptProcessor promptProcessor) {
        this.promptProcessor = promptProcessor;
        this.executorService = Executors.newSingleThreadExecutor();
        this.listeners = new ArrayList<>();
    }

    public void updatePreview(boolean isPositive, String rawPrompt) {
        executorService.submit(() -> {
            try {
                String processedPrompt = processPrompt(rawPrompt);
                notifyListeners(isPositive, processedPrompt);
            } catch (Exception e) {
                log.error("Error processing prompt", e);
            }
        });
    }

    public String processPrompt(String rawPrompt) {
        return promptProcessor.processPrompt(rawPrompt);
    }

    public void addPreviewUpdateListener(PreviewUpdateListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners(boolean isPositive, String previewText) {
        for (PreviewUpdateListener listener : listeners) {
            listener.onPreviewUpdated(isPositive, previewText);
        }
    }

    public void shutdown() {
        executorService.shutdownNow();
    }

    public interface PreviewUpdateListener {
        void onPreviewUpdated(boolean isPositive, String previewText);
    }
}