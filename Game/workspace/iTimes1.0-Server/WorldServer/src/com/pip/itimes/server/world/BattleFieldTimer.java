package com.pip.itimes.server.world;

import com.pip.itimes.server.world.game.*;
import org.apache.log4j.*;
import org.quartz.*;
import org.quartz.impl.*;

public class BattleFieldTimer {

    private static final Logger log = Logger.getLogger(BattleFieldTimer.class);

    private static BattleFieldInstanceModel battleField;
    private static GuildBattleFieldInstanceModel guildBattleField;

//    private static Timer timer = null;
    private static Scheduler scheduler = null;

    private static BattleFieldSchedule[] schedules;

    public static void setBattleField(BattleFieldInstanceModel model) {
        battleField = model;
    }

    public static void setGuildBattleField(GuildBattleFieldInstanceModel model) {
        guildBattleField = model;
    }

    public static void cancel() {
        if (scheduler != null) {
            try {
                scheduler.shutdown();
            } catch (SchedulerException ex) {
                log.error(ex, ex);
            }
        }
//        if(timer!=null)
//            timer.cancel();
    }

    public static void setSchedules(BattleFieldSchedule[] s) {
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
            schedule(schedules[i]);
        }
        if(!scheduler.isStarted())
            scheduler.start();
    }

    private static void schedule(BattleFieldSchedule schedule) throws Exception {
        if ("battlefield".equals(schedule.type)) {
            JobDetail job = scheduler.getJobDetail("job", "battlefield");
            boolean exist = true;
            if(job==null){
                job = new JobDetail("job", "battlefield", BattleFieldJob.class);
                exist = false;
            }
            BattleFieldTask task = new BattleFieldTask(schedule.enter, schedule.enterfor, schedule.end, battleField);
            job.getJobDataMap().put("task", task);
            CronTrigger trigger = new CronTrigger("job", "battlefield", schedule.cron);
            if(exist){
                trigger.setJobName("job");
                trigger.setJobGroup("battlefield");
                scheduler.rescheduleJob("job", "battlefield", trigger);
            }
            else
                scheduler.scheduleJob(job, trigger);
        } else if ("guildbattlefield".equals(schedule.type)) {
            JobDetail job = scheduler.getJobDetail("job", "guildbattlefield");
            boolean exist = true;
            if(job==null){
                job = new JobDetail("job", "guildbattlefield", BattleFieldJob.class);
                exist = false;
            }
            BattleFieldTask task = new BattleFieldTask(schedule.enter, schedule.enterfor, schedule.end,
                    guildBattleField);
            job.getJobDataMap().put("task", task);
            CronTrigger trigger = new CronTrigger("job", "guildbattlefield", schedule.cron);
            if(exist){
                trigger.setJobName("job");
                trigger.setJobGroup("guildbattlefield");
                scheduler.rescheduleJob("job", "guildbattlefield", trigger);
            }
            else
                scheduler.scheduleJob(job, trigger);
        }
//        Calendar cal = Calendar.getInstance();
//        Calendar cal1 = Calendar.getInstance();
//        cal1.set(Calendar.HOUR_OF_DAY,schedule.hour);
//        cal1.set(Calendar.MINUTE,schedule.minute);
//        System.out.println(cal1.toString());
//        System.out.println(cal.toString());
//        if("battlefield".equals(schedule.type)){
//            if (cal1.compareTo(cal) > 0) {
//                BattleFieldTask task = new BattleFieldTask(schedule.enter, schedule.enterfor, schedule.end, battleField);
//                timer.schedule(task, cal1.getTime(), 24 * 3600 * 1000L);
//            } else {
//                BattleFieldTask task = new BattleFieldTask(schedule.enter, schedule.enterfor, schedule.end, battleField);
//                long start = cal1.getTimeInMillis() + 24 * 3600 * 1000L;
//                timer.schedule(task, new Date(start), 24 * 3600 * 1000L);
//            }
//        }
//        else if("guildbattlefield".equals(schedule.type)){
//            if (cal1.compareTo(cal) > 0) {
//                BattleFieldTask task = new BattleFieldTask(schedule.enter, schedule.enterfor, schedule.end, guildBattleField);
//                timer.schedule(task, cal1.getTime(), 24 * 3600 * 1000L);
//            } else {
//                BattleFieldTask task = new BattleFieldTask(schedule.enter, schedule.enterfor, schedule.end, guildBattleField);
//                long start = cal1.getTimeInMillis() + 24 * 3600 * 1000L;
//                timer.schedule(task, new Date(start), 24 * 3600 * 1000L);
//            }
//        }
    }

    public static class BattleFieldTask {
        private int enter;
        private int enterfor;
        private int end;
        private BattleInstanceModel battleField;

        public BattleFieldTask(int enter, int enterfor, int end,
                               BattleInstanceModel model) {
            this.enter = enter;
            this.enterfor = enterfor;
            this.end = end;
            this.battleField = model;
        }

        public int getEnterfor() {
            return enterfor;
        }

        public int getEnter() {
            return enter;
        }

        public int getEnd() {
            return end;
        }

        public void setBattleField(BattleInstanceModel battleField) {
            this.battleField = battleField;
        }

        public void setEnterfor(int enterfor) {
            this.enterfor = enterfor;
        }

        public void setEnter(int enter) {
            this.enter = enter;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public BattleInstanceModel getBattleField() {
            return battleField;
        }
    }


    public static class BattleFieldJob implements Job {

        private Logger log = Logger.getLogger(BattleFieldJob.class);

        public BattleFieldJob() {
        }

        public void execute(JobExecutionContext context) throws
                JobExecutionException {
            BattleFieldTask task = (BattleFieldTask) context.getJobDetail().
                                   getJobDataMap().get("task");
            long current = System.currentTimeMillis();
            try {
                task.getBattleField().start(current + 60 * 1000 * task.getEnter(),
                                            current + 60 * 1000 * task.getEnterfor(),
                                            current + 60 * 1000 * task.getEnd());
            } catch (BattleFieldException ex) {
                log.error(ex, ex);
            }

        }

    }
}
