package com.pip.itimes.server.world;

import org.quartz.*;
import java.util.Date;

public class StartIslandAuctionJob implements Job {

    public void execute(JobExecutionContext context) throws JobExecutionException {
        Date beginAuction = (Date)context.getJobDetail().getJobDataMap().get("beginauction");
        Date endAuction = (Date)context.getJobDetail().getJobDataMap().get("endauction");
        Date endIsland = (Date)context.getJobDetail().getJobDataMap().get("endisland");
        Server.instance.tongService.startIslandAuction(beginAuction,endAuction,endIsland);
    }
}
