package com.docutools.scheduler.jobs;

import com.docutools.emails.SubscriptionNotifyService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Date;
import java.util.UUID;

@DisallowConcurrentExecution
public class TestPhaseExpireNotificationTask extends DocutoolsJob {

    private static final Logger logger = LoggerFactory.getLogger(TestPhaseExpireNotificationTask.class);


    public TestPhaseExpireNotificationTask() {
    }

    public TestPhaseExpireNotificationTask(String name, UUID id, String repeater, Date dueDate) {
        super(name, id);
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        try {
            logger.info(String.format("Test Phase Expire Notification scheduler :<%s>", this.getName()));
            ApplicationContext applicationContext =
                (ApplicationContext) jobContext.getScheduler().getContext().get("applicationContext");
            applicationContext.getBean(SubscriptionNotifyService.class).sendTestSubExpiryNotification();
        } catch (SchedulerException e) {
            logger.error("Error while running: Test Phase Expire Notification scheduler", e);
        }
    }
}
