package com.pip.itimes.server.world;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ChatJob implements Job {
    public ChatJob() {
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        String channel = context.getJobDetail().getJobDataMap().getString("channel");
        String message = context.getJobDetail().getJobDataMap().getString("message");
        if(channel!=null&&message!=null){
            if(channel.equals("world")){
                Server.instance.chatService.sendWorldMessage(-1,"ϵͳ",message);
            }
            else if(channel.equals("system")){
                Server.instance.chatService.sendSystemMessage(message);
            }
        }
    }
}
