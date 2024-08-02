package com.zxzinn.novelai.api;

import com.zxzinn.novelai.gui.generationwindow.AbstractParametersPanel;
import com.zxzinn.novelai.gui.generationwindow.PromptPanel;

import java.util.Map;

public class RequestBuilder {

    public static NAIRequest buildRequest(String action, PromptPanel promptPanel, AbstractParametersPanel parametersPanel) {
        String positivePrompt = promptPanel.getPositivePrompt();
        String negativePrompt = promptPanel.getNegativePrompt();
        Map<String, Object> params = parametersPanel.getParameters();

        NAIRequest.NAIRequestBuilder builder = NAIRequest.builder()
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