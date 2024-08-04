package com.zxzinn.novelai.event;

import com.zxzinn.novelai.GenerationState;
import lombok.Getter;

import java.util.EventObject;

@Getter
public class GenerationStateChangeEvent extends EventObject {
    private final GenerationState newState;

    public GenerationStateChangeEvent(Object source, GenerationState newState) {
        super(source);
        this.newState = newState;
    }

}

