package com.livexsports.redirect.cache;

import com.livexsports.redirect.dtos.ResponseDTO;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class M3U8Cache {
    private static final Map<String, ResponseDTO> M3U8_RESPONSE_CACHE = new ConcurrentHashMap<>();

    public static Map<String, ResponseDTO> getM3u8ResponseCache() {
        return M3U8_RESPONSE_CACHE;
    }
}
