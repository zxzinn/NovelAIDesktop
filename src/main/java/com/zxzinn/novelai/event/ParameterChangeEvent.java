package com.zxzinn.novelai.event;

import lombok.Getter;

import java.util.EventObject;

@Getter
public class ParameterChangeEvent extends EventObject {
    private final String parameterName;
    private final Object newValue;

    public ParameterChangeEvent(Object source, String parameterName, Object newValue) {
        super(source);
        this.parameterName = parameterName;
        this.newValue = newValue;
    }

}

