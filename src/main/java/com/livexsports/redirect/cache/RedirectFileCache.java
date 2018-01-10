package com.livexsports.redirect.cache;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RedirectFileCache {
    private static final Map<String, LocalDateTime> REDIRECT_FILE_CACHE = new ConcurrentHashMap<>();

    public static Map<String, LocalDateTime> getRedirectFileCache() {
        return REDIRECT_FILE_CACHE;
    }
}
