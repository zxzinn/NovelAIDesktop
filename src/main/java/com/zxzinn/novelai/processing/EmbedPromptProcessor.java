package com.zxzinn.novelai.processing;

import com.zxzinn.novelai.processing.embed.EmbedProcessor;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class EmbedPromptProcessor implements PromptProcessor {
    private final EmbedProcessor embedProcessor;

    public EmbedPromptProcessor() {
        this.embedProcessor = new EmbedProcessor();
    }

    @Override
    public String processPrompt(String rawPrompt) {
        log.debug("Processing prompt: {}", rawPrompt);
        String processedPrompt = embedProcessor.processPrompt(rawPrompt);
        log.debug("Processed prompt: {}", processedPrompt);
        return processedPrompt;
    }
}