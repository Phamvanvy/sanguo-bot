package com.pip.itimes.server.world.stat;

//import com.pip.bubble.PromptBubble;

import java.util.Calendar;

import org.apache.log4j.Logger;

import com.pip.bubble.PromptBubble;
import com.pip.itimes.server.world.PlayerService;
import com.pip.itimes.server.world.Server;

/**
 * 实现数据实时统计功能。
 * @author leo
 */
public class RealtimeStatService implements Runnable{
    private Logger log = Logger.getLogger(RealtimeStatService.class);

    public int loginCounter;
    public int logoutCounter;
    public int registerCounter;
    public int chargeCounter;
    public int imoneyUseCounter;
    public int moneyUseCounter;
    public int fightCounter;
    public int chatCounter;
    public int mailCounter;
    public int questCounter;
    public int bossKillCounter;
    private long lastReportTime = System.currentTimeMillis();

    private long todayStart = -1;
    
    private static final long DAY_MILLS = 24L * 3600 * 1000;
    
    private PlayerService playerService;
    
    public void start(){
        new Thread(this).start();
        todayStart = getTodayStart();
    }
    
    public void setPlayerService(PlayerService playerService){
        this.playerService = playerService;
    }
    
    private void clear(){
        chargeCounter = 0;
        imoneyUseCounter = 0;
        moneyUseCounter = 0;
        fightCounter = 0;
        chatCounter = 0;
        mailCounter = 0;
        questCounter = 0;
        bossKillCounter = 0;
    }
    
    private long getTodayStart(){
        Calendar cal = Calendar.getInstance();

        cal.setTimeInMillis(System.currentTimeMillis());

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime().getTime();
    }
    
    private static String getGameRegion(){
        if(Server.revisionName.equals("PIP")){
            return "hx_" + Server.getGameCode();
        }else if(Server.revisionName.equals("CMCC")){
            return "hx_yd_" + Server.getGameCode();
        }else{
            return "hx_tx_" + Server.getGameCode();
        }
    }

    public void reportChargeDetail(String accountid, String success, int amount, String channel){
        //实时report给平台充值成功记录
        PromptBubble.bubbleChargeDetialData(getGameRegion(), Integer.parseInt(accountid), channel, amount, "true".equals(success), null);
    }

    private void report(){
        PromptBubble.bubbleOnlineData(getGameRegion(), playerService.getPlayerCount(), loginCounter, logoutCounter, registerCounter);
        PromptBubble.bubbleChargeData(getGameRegion(), chargeCounter);
        PromptBubble.bubbleConsumeData(getGameRegion(), imoneyUseCounter, moneyUseCounter);
        PromptBubble.bubbleActivitiesData(getGameRegion(), fightCounter, chatCounter, mailCounter, questCounter, bossKillCounter);
        loginCounter = 0;
        logoutCounter = 0;
        registerCounter = 0;
    }

    public void run(){
        while(true){
            try{
                long curr = System.currentTimeMillis();
                
                //凌晨清零
                if(curr - todayStart > DAY_MILLS){
                    todayStart = getTodayStart();
                    lastReportTime = System.currentTimeMillis();
                    report();
                    clear();
                }else{
                    //每10分钟report一次
                    if(System.currentTimeMillis() - lastReportTime > 600000){
                        lastReportTime = System.currentTimeMillis();
                        report();
                    }
                }
                
                Thread.sleep(60000);
            }catch(Exception e){
                log.error(e, e);
            }
        }
    }
}
