package com.livexsports.redirect.jobs;

import com.livexsports.redirect.cache.KeyFileCache;
import com.livexsports.redirect.cache.M3U8Cache;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@DisallowConcurrentExecution
public class ClearCacheJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        KeyFileCache.getKeyFileResponseCache().clear();
        M3U8Cache.getM3u8ResponseCache().clear();
    }
}
