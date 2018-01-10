package com.livexsports.redirect.jobs;

import com.livexsports.redirect.cache.RedirectFileCache;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Map;

public class ClearRedirectOldFileJob implements Job {
    private final Logger LOGGER = LoggerFactory.getLogger(ClearRedirectOldFileJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("Run clear redirect old file..");
        LocalDateTime now = LocalDateTime.now().minusMinutes(1);
        for (Map.Entry<String, LocalDateTime> entry : RedirectFileCache.getRedirectFileCache().entrySet()) {
            try {
                if (entry.getValue().compareTo(now) < 0) {
                    File file = new File(entry.getKey());
                    boolean isDeleted = file.delete();
                    if (isDeleted) {
                        RedirectFileCache.getRedirectFileCache().remove(entry.getKey());
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }
    }
}
