package com.docutools.scheduler;

import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;

/**
 * Configuration class for Quartz Scheduling
 */
@Configuration
public class SchedulerConfig {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerConfig.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(JobFactory jobFactory) {
        SchedulerFactoryBean bean = new SchedulerFactoryBean();
        bean.setApplicationContextSchedulerContextKey("applicationContext");
        bean.setApplicationContext(applicationContext);
        bean.setSchedulerContextAsMap(new HashMap());
        bean.setWaitForJobsToCompleteOnShutdown(true);
        bean.setTransactionManager(transactionManager);
        bean.setDataSource(dataSource);
        bean.setConfigLocation(new ClassPathResource("quartz.properties"));
        bean.setJobFactory(jobFactory);
        return bean;

    }

    /**
     * Job Factory
     *
     * @param context Spring Application Context
     * @return SpringBeanJobFactory
     */
    @Bean
    public SpringBeanJobFactory jobFactory(ApplicationContext context) {
        JobSchedulerFactory jobSchedulerFactory = new JobSchedulerFactory();
        jobSchedulerFactory.setApplicationContext(context);
        return jobSchedulerFactory;
    }

    /**
     * Scheduler to be used to schedule new jobs.
     *
     * @param schedulerFactoryBean To get a scheduler
     * @return Scheduler
     */
    @Bean
    public Scheduler scheduler(SchedulerFactoryBean schedulerFactoryBean) {
        return schedulerFactoryBean.getObject();
    }


    /**
     * Job SchedulerFactory
     */
    private class JobSchedulerFactory extends SpringBeanJobFactory implements ApplicationContextAware {

        private transient AutowireCapableBeanFactory beanFactory;


        @Override
        public void setApplicationContext(ApplicationContext context) throws BeansException {
            beanFactory = context.getAutowireCapableBeanFactory();

        }

        @Override
        public Job newJob(TriggerFiredBundle bundle, Scheduler ignored) throws SchedulerException {
            try {
                final Object job = super.createJobInstance(bundle);
                beanFactory.autowireBean(job);
                return (Job) job;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

}
