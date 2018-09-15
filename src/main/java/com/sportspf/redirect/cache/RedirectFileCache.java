package com.sportspf.redirect.cache;

import com.sportspf.redirect.dtos.RedirectFileDTO;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RedirectFileCache {
    public static Map<String, RedirectFileDTO> REDIRECT_FILE_CACHE = new ConcurrentHashMap<>();

    public static final Map<String, Boolean> CHECK_DOWNLOAD_REDIRECT_FILE_CACHE = new ConcurrentHashMap<>();
}
