package com.zxzinn.novelai.event;

import java.util.EventListener;

public interface FileOperationListener extends EventListener {
    void onFileOperation(FileOperationEvent event);
}
