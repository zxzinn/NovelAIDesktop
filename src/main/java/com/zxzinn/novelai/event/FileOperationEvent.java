package com.zxzinn.novelai.event;

import lombok.Getter;

import java.io.File;
import java.util.EventListener;
import java.util.EventObject;

@Getter
public class FileOperationEvent extends EventObject {
    public enum OperationType {
        ADDED, DELETED, MODIFIED
    }

    private final File file;
    private final OperationType operationType;

    public FileOperationEvent(Object source, File file, OperationType operationType) {
        super(source);
        this.file = file;
        this.operationType = operationType;
    }

}

