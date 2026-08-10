package com.pip.itimes.server.world;

import org.quartz.Job;
import org.quartz.JobExecutionException;
import org.quartz.JobExecutionContext;
import java.util.Date;
import java.util.Random;

public class StartIslandJob implements Job {

    public StartIslandJob() {
    }


    public void execute(JobExecutionContext context) throws JobExecutionException {
        Date prepare = new Date(new Date().getTime()+30*1000L);
        Date beginAuction = new Date(prepare.getTime()+30*60*1000L);
        Date preparedEndAuction = new Date(prepare.getTime()+150*60*1000L);
        Random rnd = new Random();
        int m = rnd.nextInt(60)+1;
        Date endAuction = new Date(prepare.getTime()+(150+m)*60*1000L);
        Date endIsland = new Date(prepare.getTime()+(10079)*60*1000L); //一周少一分钟
        try {
            Server.instance.tongService.prepareStartIslandAuction(prepare, beginAuction, preparedEndAuction, endAuction,
                    endIsland);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
