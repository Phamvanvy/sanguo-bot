package com.pip.itimes.server.world;

import org.quartz.*;

public class EndIslandAuctionJob implements Job {

    public void execute(JobExecutionContext context) throws JobExecutionException {
        Server.instance.tongService.endIslandAuction();
    }
}
