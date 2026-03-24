package com.example.emos.wx.config;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.annotation.PostConstruct;

@Configuration
public class QuartzConfig {

    private final Scheduler scheduler;

    public QuartzConfig(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @PostConstruct
    public void startScheduler() {
        try {
            if (scheduler != null && !scheduler.isStarted()) {
                scheduler.start();
                System.out.println("Quartz scheduler started successfully");
            }
        } catch (SchedulerException e) {
            System.err.println("Failed to start Quartz scheduler: " + e.getMessage());
            e.printStackTrace();
        }
    }
}