package com.zxzinn.novelai.api;

import java.io.IOException;

public interface ApiService {
    byte[] sendRequest(NAIRequest request) throws IOException, InterruptedException;
    void setApiKey(String apiKey);
}