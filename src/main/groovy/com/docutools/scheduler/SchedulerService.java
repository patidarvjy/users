package com.docutools.scheduler;

import com.docutools.scheduler.jobs.NotificationAfter48Hours;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;

@Service
public class SchedulerService {

    @Autowired
    private Scheduler scheduler;

    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);

    public void schedule(NotificationAfter48Hours job) {
        JobBuilder jobBuilder = JobBuilder.newJob().withIdentity(job.getId().toString()).ofType(NotificationAfter48Hours.class);
        if (job.getPropertiesMap() != null) {
            job.getPropertiesMap().forEach(jobBuilder::usingJobData);
        }
        JobDetail jobDetail = jobBuilder.build();
        Calendar after48Hours = Calendar.getInstance();
        after48Hours.add(Calendar.HOUR,48);
        SimpleTrigger trigger = TriggerBuilder.newTrigger().withSchedule(SimpleScheduleBuilder
            .repeatSecondlyForTotalCount(1)).startAt(after48Hours.getTime()).build();
        try {
            scheduler.scheduleJob(jobDetail, trigger);
            logger.info(String.format("Scheduled a new Job : %s", job));
        } catch (SchedulerException e) {
            logger.error("Error in task scheduler", e);
        }
    }
}
