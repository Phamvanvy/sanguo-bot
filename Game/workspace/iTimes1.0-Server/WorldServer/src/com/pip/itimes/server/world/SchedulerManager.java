package com.pip.itimes.server.world;

import org.quartz.Scheduler;
import org.quartz.*;
import org.quartz.impl.DirectSchedulerFactory;


public class SchedulerManager {
    public static Scheduler scheduler = null;

    static{
        try {
            DirectSchedulerFactory.getInstance().createVolatileScheduler(3);
            scheduler = DirectSchedulerFactory.getInstance().getScheduler();
            scheduler.start();
        } catch (SchedulerException ex) {
            ex.printStackTrace();
        }
    }

    public static void addJob(JobDetail job,Trigger trigger) throws SchedulerException{
        job.setDurability(true);
        scheduler.addJob(job,true);
        scheduler.scheduleJob(trigger);
    }

}
