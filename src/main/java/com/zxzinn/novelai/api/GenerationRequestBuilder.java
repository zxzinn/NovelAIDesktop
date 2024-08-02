package com.zxzinn.novelai.api;

import com.zxzinn.novelai.gui.generation.AbstractParametersPanel;
import com.zxzinn.novelai.gui.generation.PromptPanel;

import java.util.Map;

public class GenerationRequestBuilder {
    public static GenerationRequest buildRequest(String action, String positivePrompt, String negativePrompt, AbstractParametersPanel parametersPanel) {
        Map<String, Object> params = parametersPanel.getParameters();

        GenerationRequest.GenerationRequestBuilder builder = GenerationRequest.builder()
                .input(positivePrompt)
                .model((String) params.get("model"))
                .action(action)
                .width((Integer) params.get("width"))
                .height((Integer) params.get("height"))
                .scale((Double) params.get("scale"))
                .sampler((String) params.get("sampler"))
                .steps((Integer) params.get("steps"))
                .seed((Long) params.get("seed"))
                .n_samples((Integer) params.get("n_samples"))
                .negative_prompt(negativePrompt);

        if ("img2img".equals(action)) {
            builder.image((String) params.get("image"))
                    .extra_noise_seed((Long) params.get("extra_noise_seed"));
        }

        return builder.build();
    }
}