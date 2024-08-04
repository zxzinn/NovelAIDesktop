package com.zxzinn.novelai.event;

import java.util.EventListener;

public interface ParameterChangeListener extends EventListener {
    void onParameterChange(ParameterChangeEvent event);
}
