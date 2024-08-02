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

    public void addObserver(Consumer<MainViewModel> observer) {
        observers.add(observer);
    }

    public void removeObserver(Consumer<MainViewModel> observer) {
        observers.remove(observer);
    }

    public void addGeneratedImage(BufferedImage image) {
        generatedImages.add(image);
        notifyObservers();
    }

    public void appendConsoleText(String text) {
        consoleText += text + "\n";
        notifyObservers();
    }

    public void setGenerating(boolean generating) {
        this.isGenerating = generating;
        notifyObservers();
    }

    private void notifyObservers() {
        for (Consumer<MainViewModel> observer : observers) {
            observer.accept(this);
        }
    }
}