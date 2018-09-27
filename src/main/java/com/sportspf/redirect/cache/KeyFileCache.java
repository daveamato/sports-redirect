package com.sportspf.redirect.cache;

import com.sportspf.redirect.dtos.ResponseDTO;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KeyFileCache {
    public static final Map<String, ResponseDTO> KEY_FILE_RESPONSE_CACHE = new ConcurrentHashMap<>();

    public static final Map<String, Boolean> CHECK_DOWNLOAD_KEY_FILE_CACHE = new ConcurrentHashMap<>();
}
