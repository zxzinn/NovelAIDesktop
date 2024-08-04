package com.zxzinn.novelai.event;

import java.util.EventListener;

public interface GenerationStateChangeListener extends EventListener {
    void onGenerationStateChange(GenerationStateChangeEvent event);
}
