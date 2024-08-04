package com.zxzinn.novelai.event;

import lombok.Getter;

import java.util.EventListener;
import java.util.EventObject;

@Getter
public class PromptUpdateEvent extends EventObject {
    private final String positivePrompt;
    private final String negativePrompt;

    public PromptUpdateEvent(Object source, String positivePrompt, String negativePrompt) {
        super(source);
        this.positivePrompt = positivePrompt;
        this.negativePrompt = negativePrompt;
    }

}

