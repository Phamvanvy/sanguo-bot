package com.pip.itimes.server.world.sports;

import org.quartz.JobDetail;
import org.quartz.CronTrigger;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.Job;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionException;
import org.quartz.impl.StdSchedulerFactory;

import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;

public class SportsTimer {
    private static final Logger log = Logger.getLogger(SportsTimer.class);

    private static SportsService sportsService;

    public static Scheduler scheduler = null;

    private static SportSchedule[] schedules;

    public static void setSportsService(SportsService service){
        sportsService = service;
    }

    public static void cancel() {
        if (scheduler != null) {
            try {
                scheduler.shutdown();
            } catch (SchedulerException ex) {
                log.error(ex, ex);
            }
        }
    }

    public static void setSchedules(SportSchedule[] s) {
        schedules = s;
    }

    public static void start() throws Exception {
        if (schedules == null)
            return;
        if(scheduler==null){
            SchedulerFactory sf = new StdSchedulerFactory();
            scheduler = sf.getScheduler();
        }
        for (int i = 0; i < schedules.length; i++) {
            schedule(schedules[i],i);
        }
        if(!scheduler.isStarted())
            scheduler.start();
    }

    private static void schedule(SportSchedule schedule,int c) throws Exception {
        JobDetail job = scheduler.getJobDetail("job"+c, "battlefield");
        boolean exist = true;
        if(job==null){
            job = new JobDetail("job" + c, "battlefield", SportJob.class);
            exist = false;
        }
        SportTask task = new SportTask(schedule.start, schedule.end, schedule.interval,schedule.type,schedule.bbsId, sportsService);
        job.getJobDataMap().put("task", task);
        CronTrigger trigger = new CronTrigger("job"+c, "battlefield", schedule.cron);
        if(exist){
            trigger.setJobName("job" + c);
            trigger.setJobGroup("battlefield");
            scheduler.rescheduleJob("job"+c, "battlefield",trigger);
        }else
            scheduler.scheduleJob(job, trigger);
    }

    public static class SportTask {
        private int start;
        private int end;
        private int interval;
        private String type;
        private int bbsId;
        private SportsService sportsService;

        public SportTask(int start, int end, int interval,String type,int bbsId,SportsService sportService) {
            this.start = start;
            this.interval = interval;
            this.end = end;
            this.type = type;
            this.bbsId = bbsId;
            this.sportsService = sportService;
        }

        public int getStart() {
            return start;
        }

        public int getInterval() {
            return interval;
        }

        public int getEnd() {
            return end;
        }

        public SportsService getSportsService(){
            return sportsService;
        }


        public String getType(){
            return type;
        }

        public int getBbsId(){
            return bbsId;
        }
    }


    public static class SportJob implements Job {

        private Logger log = Logger.getLogger(SportJob.class);

        public void execute(JobExecutionContext context) throws
                JobExecutionException {
            SportTask task = (SportTask) context.getJobDetail().
                                   getJobDataMap().get("task");
            long current = System.currentTimeMillis();
            task.getSportsService().start(current + 60L * 1000 * task.getStart(),
                                          current + 60L * 1000 * task.getEnd(),
                                          60L * 1000 * task.getInterval(), task.getType(),task.bbsId);

        }

    }
}
