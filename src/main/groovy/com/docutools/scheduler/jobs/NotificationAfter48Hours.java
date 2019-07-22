package com.docutools.scheduler.jobs;

import com.docutools.emails.PersonalNotificationService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.UUID;

@DisallowConcurrentExecution
public class NotificationAfter48Hours extends DocutoolsJob {

    private static final Logger logger = LoggerFactory.getLogger(NotificationAfter48Hours.class);


    public NotificationAfter48Hours() {
    }

    public NotificationAfter48Hours(String name, UUID id) {
        super(name, id);
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        try {
            logger.info(String.format("Personal Notification Email After 48Hours scheduler for user id:<%s>", this.getId()));
            ApplicationContext applicationContext =
                (ApplicationContext) jobContext.getScheduler().getContext().get("applicationContext");
            applicationContext.getBean(PersonalNotificationService.class).sendPersonalNotification(this.getId());
        } catch (SchedulerException e) {
            logger.error("Error while running: Personal Notification Email After 48Hours scheduler", e);
        }
    }
}
