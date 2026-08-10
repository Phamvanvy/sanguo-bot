package com.pip.itimes.server.world;

import org.quartz.*;

public class EndIslandJob implements Job {

    public void execute(JobExecutionContext context) throws JobExecutionException {
        Server.instance.tongService.endIsland();
    }
}
