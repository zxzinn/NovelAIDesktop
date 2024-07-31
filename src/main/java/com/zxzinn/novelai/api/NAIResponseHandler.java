package com.zxzinn.novelai.api;

public interface NAIResponseHandler {
    void handleResponse(byte[] response);
    void handleError(String errorMessage);
}