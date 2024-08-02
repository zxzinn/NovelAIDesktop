package com.zxzinn.novelai.api;

import java.io.IOException;

public interface ApiService {
    byte[] sendRequest(GenerationRequest request) throws IOException, InterruptedException;
    void setApiKey(String apiKey);
}