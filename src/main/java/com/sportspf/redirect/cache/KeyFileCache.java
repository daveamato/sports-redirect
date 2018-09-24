package com.sportspf.redirect.cache;

import com.sportspf.redirect.dtos.ResponseDTO;

import java.util.HashMap;
import java.util.Map;

public class KeyFileCache {
    public static final Map<String, ResponseDTO> KEY_FILE_RESPONSE_CACHE = new HashMap<>();

    public static final Map<String, Boolean> CHECK_DOWNLOAD_KEY_FILE_CACHE = new HashMap<>();
}
