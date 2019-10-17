package com.sportspf.redirect.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReplayCache {
    public static final Map<String, Object> NBA_CACHE = new ConcurrentHashMap<>();
}
