package com.livexsports.redirect.cache;

import com.livexsports.redirect.dtos.RedirectFileDTO;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RedirectFileCache {
    public static Map<String, RedirectFileDTO> REDIRECT_FILE_CACHE = new ConcurrentHashMap<>();
}
