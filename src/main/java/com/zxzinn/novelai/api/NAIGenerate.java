package com.zxzinn.novelai.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NAIGenerate {
    private String input;
    private String model;
    private String action;

    // Create this in hashmaps
    private int params_version;
    private int width;
    private int height;
    private double scale;
    private String sampler;
    private int steps;
    private int n_samples;
    private int ucPreset;
    private boolean qualityToggle;
    private boolean sm;
    private boolean sm_dyn;
    private boolean dynamic_thresholding;
    private int controlnet_strength;
    private boolean legacy;
    private boolean add_original_image;
    private int cfg_rescale;
    private String noise_schedule;
    private boolean legacy_v3_extend;
    private long seed;
    private String negative_prompt;

    // Unknown and unused Data Type
    private Object[] reference_image_multiple;
    private Object[] reference_information_extracted_multiple;
    private Object[] reference_strength_multiple;

    // Lombok will generate all necessary methods
}