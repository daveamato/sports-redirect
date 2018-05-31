package com.sportspf.redirect.cache;

import com.sportspf.redirect.dtos.ResponseDTO;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class M3U8Cache {
    public static final Map<String, ResponseDTO> M3U8_RESPONSE_CACHE = new ConcurrentHashMap<>();

    public static final Map<String, Boolean> CHECK_DOWNLOAD_M3U8_FILE_CACHE = new ConcurrentHashMap<>();
}
