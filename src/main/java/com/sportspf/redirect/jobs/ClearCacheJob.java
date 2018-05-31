package com.sportspf.redirect.jobs;

import com.sportspf.redirect.cache.KeyFileCache;
import com.sportspf.redirect.cache.M3U8Cache;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

public class ClearCacheJob implements Job {
    @Override
    public void execute(JobExecutionContext context) {
        KeyFileCache.KEY_FILE_RESPONSE_CACHE.clear();
        KeyFileCache.CHECK_DOWNLOAD_KEY_FILE_CACHE.clear();
        M3U8Cache.M3U8_RESPONSE_CACHE.clear();
        M3U8Cache.CHECK_DOWNLOAD_M3U8_FILE_CACHE.clear();
    }
}
