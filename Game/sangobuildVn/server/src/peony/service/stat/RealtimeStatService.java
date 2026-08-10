package peony.service.stat;

//import com.pip.bubble.PromptBubble;

import peony.game.DayListener;
import peony.game.ObjectAccessor;
import peony.game.Server;
import peony.service.Service;

import com.pip.bubble.PromptBubble;

/**
 * 实现数据实时统计功能。
 * @author lighthu
 */
public class RealtimeStatService implements Service, DayListener, Runnable{
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
    private boolean clearAfterReport;
    private long lastReportTime = System.currentTimeMillis();

    public void startup() throws Exception{
    }

    public void shutdown(){
    }

    public void dayChanged(){
        clearAfterReport = true;
        report();

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

    public void update(int diff){
        if(System.currentTimeMillis() - lastReportTime > 600000){
            lastReportTime = System.currentTimeMillis();
            report();
        }
    }

    private void report(){
        if(Server.supportRealtimeStatistics){
            new Thread(this).start();
        }
    }
    
    private String getGameRegion(){
        if(Server.server.revision.equals("PIP")){
            return "sg_" + Server.server.gameCode;
        }else{
            return "sg_yd_" + Server.server.gameCode;
        }
    }

    public void reportChargeDetail(String accountid, String success, int amount, String channel){
        //实时report给平台充值成功记录
        PromptBubble.bubbleChargeDetialData(getGameRegion(), Integer.parseInt(accountid), channel, amount, "true".equals(success), null);
    }

    public void run(){
        PromptBubble.bubbleOnlineData(getGameRegion(), ObjectAccessor.players.size(), loginCounter, logoutCounter, registerCounter);
        PromptBubble.bubbleChargeData(getGameRegion(), chargeCounter);
        PromptBubble.bubbleConsumeData(getGameRegion(), imoneyUseCounter, moneyUseCounter);
        PromptBubble.bubbleActivitiesData(getGameRegion(), fightCounter, chatCounter, mailCounter, questCounter, bossKillCounter);
        loginCounter = 0;
        logoutCounter = 0;
        registerCounter = 0;
        if(clearAfterReport){
            clear();
            clearAfterReport = false;
        }
    }
}
