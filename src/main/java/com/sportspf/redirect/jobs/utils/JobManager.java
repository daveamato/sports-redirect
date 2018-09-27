package com.sportspf.redirect.jobs.utils;

import com.sportspf.redirect.jobs.ClearCacheJob;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class JobManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobManager.class);

    private final Scheduler scheduler;

    @Autowired
    public JobManager(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @PostConstruct
    public void initializeJobs() {
        runClearCache();
    }

    private void runClearCache() {
        JobKey jobKey = new JobKey("ClearCacheName", "ClearCacheGroup");
        try {
            if (scheduler.checkExists(jobKey)) {
                return;
            }
            LOGGER.info("Clear cache job is creating...");

            JobDetail job = JobBuilder.newJob(ClearCacheJob.class)
                    .withIdentity("ClearCacheName", "ClearCacheGroup")
                    .build();
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("ClearCacheTrigger", "ClearCacheGroup")
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInMinutes(10)
                            .repeatForever())
                    .build();
            scheduler.scheduleJob(job, trigger);
            LOGGER.info("Scheduler for clear cache is started.");
        } catch (SchedulerException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
