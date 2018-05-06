package com.livexsports.redirect.cache;

import com.livexsports.redirect.dtos.ResponseDTO;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KeyFileCache {
    private static final Map<String, ResponseDTO> KEY_FILE_RESPONSE_CACHE = new ConcurrentHashMap<>();

    public static Map<String, ResponseDTO> getKeyFileResponseCache() {
        return KEY_FILE_RESPONSE_CACHE;
    }
}
