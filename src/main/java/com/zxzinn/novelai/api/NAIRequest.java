package com.zxzinn.novelai.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NAIRequest {
    private String input;
    private String model;
    private String action;
    private int width;
    private int height;
    private double scale;
    private String sampler;
    private int steps;
    private long seed;
    private int n_samples;
    private String negative_prompt;
    private String image;
    private long extra_noise_seed;

    // Other fields can be added as needed
}