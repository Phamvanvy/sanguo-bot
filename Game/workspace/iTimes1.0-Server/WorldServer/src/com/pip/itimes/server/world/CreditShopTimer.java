package com.pip.itimes.server.world;

import com.pip.itimes.server.stage.CreditShop;
import org.apache.log4j.*;
import org.quartz.*;
import org.quartz.impl.*;

public class CreditShopTimer {

    private static final Logger log = Logger.getLogger(CreditShopTimer.class);

    private static Scheduler scheduler = null;
    private static AuctionService auctionService;
    public static void setAuctionService(AuctionService auctionService){
    	CreditShopTimer.auctionService = auctionService;
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

    public static void start() throws Exception {
        if (CreditShop.getCount() == -1)
            return;
        if (scheduler == null){
        	SchedulerFactory sf = new StdSchedulerFactory();
        	scheduler = sf.getScheduler();
        }
        CreditShop[] cshop = new CreditShop[CreditShop.getCount()];
        for (int i = 0; i < CreditShop.getCount(); i++) {
        	cshop[i] = CreditShop.getCreditShop(i);
        }
        schedule(cshop);
        if(!scheduler.isStarted()){
        	scheduler.start();
        }
    }

    private static void schedule(CreditShop schedule[]) throws Exception {
    	for (int i = 0; i < CreditShop.getCount(); i++) {
	        //JobDetail job = new JobDetail("job"+i, "creditshop", CreditShopJob.class);
	        JobDetail job = scheduler.getJobDetail("job"+i, "creditshop");
            boolean exist = true;
            if(job==null){
                job = new JobDetail("job"+i, "creditshop", CreditShopJob.class);
                exist = false;
            }
	        CreditShop Schedule = schedule[i];
	        job.getJobDataMap().put("creditshop", Schedule);
	        CronTrigger trigger = new CronTrigger("job"+i, "creditshop", schedule[i].getCorn());
	        //scheduler.scheduleJob(job, trigger);
	        if(exist){
                trigger.setJobName("job"+i);
                trigger.setJobGroup("creditshop");
                scheduler.rescheduleJob("job"+i, "creditshop", trigger);
            }else{
                scheduler.scheduleJob(job, trigger);
            }
    	}
    }
    
    public static class CreditShopJob implements Job {

        private Logger log = Logger.getLogger(CreditShopJob.class);

        public CreditShopJob() {
        }

        public void execute(JobExecutionContext context) throws
                JobExecutionException {
        	CreditShop creditshop = (CreditShop) context.getJobDetail().
                                   getJobDataMap().get("creditshop");
            long current = System.currentTimeMillis();
            try {
            	auctionService.start(creditshop);
            } catch (AuctionException ex) {
                log.error(ex, ex);
            }

        }

    }
}
