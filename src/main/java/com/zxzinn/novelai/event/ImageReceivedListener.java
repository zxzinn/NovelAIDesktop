package com.zxzinn.novelai.event;

import java.util.EventListener;

public interface ImageReceivedListener extends EventListener {
    void onImageReceived(ImageReceivedEvent event);
}
