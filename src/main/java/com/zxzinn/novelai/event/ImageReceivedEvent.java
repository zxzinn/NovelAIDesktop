package com.zxzinn.novelai.event;

import lombok.Getter;

import java.awt.image.BufferedImage;
import java.util.EventListener;
import java.util.EventObject;

@Getter
public class ImageReceivedEvent extends EventObject {
    private final BufferedImage image;

    public ImageReceivedEvent(Object source, BufferedImage image) {
        super(source);
        this.image = image;
    }

}

