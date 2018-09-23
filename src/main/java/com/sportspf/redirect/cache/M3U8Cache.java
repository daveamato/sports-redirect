package com.sportspf.redirect.cache;

import com.sportspf.redirect.dtos.ResponseDTO;

import java.util.HashMap;
import java.util.Map;

public class M3U8Cache {
    public static final Map<String, ResponseDTO> M3U8_RESPONSE_CACHE = new HashMap<>();

    public static final Map<String, Boolean> CHECK_DOWNLOAD_M3U8_FILE_CACHE = new HashMap<>();
}
