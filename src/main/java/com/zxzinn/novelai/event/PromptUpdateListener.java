package com.zxzinn.novelai.event;

import java.util.EventListener;

public interface PromptUpdateListener extends EventListener {
    void onPromptUpdate(PromptUpdateEvent event);
}
