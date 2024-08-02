package com.zxzinn.novelai.utils;

import javax.swing.*;

public interface UIComponent {
    void initializeComponents();
    void layoutComponents();
    void bindEvents();
    JComponent getComponent();
}