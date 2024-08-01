package com.zxzinn.novelai.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class NAIImg2Img extends NAIRequest {
    private String image;
    private long extra_noise_seed;
}