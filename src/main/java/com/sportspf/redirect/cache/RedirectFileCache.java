package com.sportspf.redirect.cache;

import com.sportspf.redirect.dtos.RedirectFileDTO;

import java.util.HashMap;
import java.util.Map;

public class RedirectFileCache {
    public static Map<String, RedirectFileDTO> REDIRECT_FILE_CACHE = new HashMap<>();

    public static final Map<String, Boolean> CHECK_DOWNLOAD_REDIRECT_FILE_CACHE = new HashMap<>();
}
