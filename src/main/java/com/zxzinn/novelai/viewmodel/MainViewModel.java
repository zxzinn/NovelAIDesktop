package com.zxzinn.novelai.viewmodel;

import lombok.Getter;
import lombok.Setter;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MainViewModel {
    @Getter
    private boolean isGenerating;
    @Getter
    private final List<BufferedImage> generatedImages = new ArrayList<>();
    @Getter @Setter
    private String consoleText = "";

    private final List<Consumer<MainViewModel>> observers = new ArrayList<>();
}