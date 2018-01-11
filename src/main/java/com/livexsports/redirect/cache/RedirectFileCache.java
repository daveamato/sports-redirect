package com.livexsports.redirect.cache;

import com.livexsports.redirect.dtos.RedirectFileDTO;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RedirectFileCache {
    private static final Map<String, RedirectFileDTO> REDIRECT_FILE_CACHE = new ConcurrentHashMap<>();

    public static Map<String, RedirectFileDTO> getRedirectFileCache() {
        return REDIRECT_FILE_CACHE;
    }
}
