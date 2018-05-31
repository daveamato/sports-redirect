package com.sportspf.redirect.jobs;

import com.sportspf.redirect.cache.RedirectFileCache;
import com.sportspf.redirect.dtos.RedirectFileDTO;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClearRedirectOldFileJob implements Job {
    private final Logger LOGGER = LoggerFactory.getLogger(ClearRedirectOldFileJob.class);
    private static final List<String> FILE_NAMES = new ArrayList<>();
    private static int index = 0;

    @Override
    public void execute(JobExecutionContext context) {
        LOGGER.info("Run clear redirect old file..");
        Set<Map.Entry<String, RedirectFileDTO>> entries = RedirectFileCache.REDIRECT_FILE_CACHE.entrySet();
        RedirectFileCache.REDIRECT_FILE_CACHE = new ConcurrentHashMap<>();
        for (Map.Entry<String, RedirectFileDTO> entry : entries) {
            FILE_NAMES.add(entry.getValue().getFile().getName());
        }
        if (index < 4) {
            index++;
        } else {
            for (String fileName : FILE_NAMES) {
                try {
                    new File(fileName).delete();
                } catch (Exception e) {
                    // ignore
                }
            }
            FILE_NAMES.clear();
            index = 0;
        }
    }
}
