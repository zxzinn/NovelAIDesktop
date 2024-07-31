package com.zxzinn.novelai.api;

import java.io.IOException;

public interface NAIRequestHandler {
    byte[] sendRequest(NAIGenerate request) throws IOException, InterruptedException;
}