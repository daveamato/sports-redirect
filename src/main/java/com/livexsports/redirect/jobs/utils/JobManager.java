package com.livexsports.redirect.jobs.utils;

import com.livexsports.redirect.jobs.ClearRedirectOldFileJob;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;

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
        runClearRedirectOldFile();
    }

    private void runClearRedirectOldFile() {
        File dir = new File(System.getProperty("user.dir"));
        if (dir.isDirectory()) {
            File files[] = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    try {
                        if (file.isFile() && file.getName().endsWith(".ts")) {
                            file.delete();
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }
        JobKey jobKey = new JobKey("ClearRedirectOldFileName", "ClearRedirectOldFileGroup");
        try {
            if (scheduler.checkExists(jobKey)) {
                return;
            }
            LOGGER.info("Clear redirect old file job is creating...");

            JobDetail job = JobBuilder.newJob(ClearRedirectOldFileJob.class)
                .withIdentity("ClearRedirectOldFileName", "ClearRedirectOldFileGroup")
                .build();
            Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("ClearRedirectOldFileTrigger", "ClearRedirectOldFileGroup")
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInMinutes(1)
                    .repeatForever())
                .build();
            scheduler.scheduleJob(job, trigger);
            LOGGER.info("Scheduler for clear redirect old file is started.");
        } catch (SchedulerException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
