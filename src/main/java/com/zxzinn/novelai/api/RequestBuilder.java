package com.zxzinn.novelai.api;

import com.zxzinn.novelai.gui.generationwindow.AbstractParametersPanel;
import com.zxzinn.novelai.gui.generationwindow.GenerationParametersPanel;
import com.zxzinn.novelai.gui.generationwindow.Img2ImgParametersPanel;
import com.zxzinn.novelai.gui.generationwindow.PromptPanel;

import java.util.Map;

public class RequestBuilder {

    public static NAIRequest buildRequest(String action, PromptPanel promptPanel, AbstractParametersPanel currentParametersPanel) {
        String positivePrompt = promptPanel.getPositivePrompt();
        String negativePrompt = promptPanel.getNegativePrompt();
        Map<String, Object> params = currentParametersPanel.getParameters();

        if ("generate".equals(action)) {
            return buildGenerateRequest(positivePrompt, negativePrompt, params);
        } else if ("img2img".equals(action)) {
            return buildImg2ImgRequest(positivePrompt, negativePrompt, params);
        }

        throw new IllegalStateException("Unknown action: " + action);
    }

    private static NAIGenerate buildGenerateRequest(String positivePrompt, String negativePrompt, Map<String, Object> params) {
        return NAIGenerate.builder()
                .input(positivePrompt)
                .model((String) params.get("model"))
                .action("generate")
                .width((Integer) params.get("width"))
                .height((Integer) params.get("height"))
                .scale((Double) params.get("scale"))
                .sampler((String) params.get("sampler"))
                .steps((Integer) params.get("steps"))
                .seed((Long) params.get("seed"))
                .n_samples((Integer) params.get("n_samples"))
                .negative_prompt(negativePrompt)
                .sm((Boolean) params.get("sm"))
                .sm_dyn((Boolean) params.get("sm_dyn"))
                .build();
    }

    private static NAIImg2Img buildImg2ImgRequest(String positivePrompt, String negativePrompt, Map<String, Object> params) {
        String base64Image = (String) params.get("image");
        if (base64Image == null || base64Image.isEmpty()) {
            throw new IllegalStateException("No image uploaded for img2img");
        }
        return NAIImg2Img.builder()
                .input(positivePrompt)
                .model((String) params.get("model"))
                .action("img2img")
                .width((Integer) params.get("width"))
                .height((Integer) params.get("height"))
                .scale((Double) params.get("scale"))
                .sampler((String) params.get("sampler"))
                .steps((Integer) params.get("steps"))
                .seed((Long) params.get("seed"))
                .n_samples((Integer) params.get("n_samples"))
                .negative_prompt(negativePrompt)
                .sm((Boolean) params.get("sm"))
                .sm_dyn((Boolean) params.get("sm_dyn"))
                .extra_noise_seed((Long) params.get("extra_noise_seed"))
                .image(base64Image)
                .build();
    }
}