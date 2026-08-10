package com.pip.itimes.server.world.fee;

import java.util.Iterator;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.accountskeleton.AccountSkeleton;
import com.pip.itimes.server.world.*;
import com.pip.net.message.gameaccount.LegacyFee1Message;
import org.apache.log4j.Logger;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class FeeService implements Runnable {

    private static final Logger log = Logger.getLogger(FeeService.class);

    private PlayerService playerService;
    private ConnectService connectService;
    private StageService stageService;
    private ChatService chatService;
    private AccountSkeleton accountSkeleton;

    public FeeService() {
        new Thread(this).start();
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    public void setConnectService(ConnectService connectService) {
        this.connectService = connectService;
    }

    public void setStageService(StageService stageService) {
        this.stageService = stageService;
    }

    public void setChatService(ChatService chatService) {
        this.chatService = chatService;
    }

    public void setAccountSkeleton(AccountSkeleton skeleton){
        this.accountSkeleton = skeleton;
    }
    public void run() {
        while (true) {
            long sleep = checkFees();
            if (sleep > 0) {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    public void feeResult(int accountId, int feeId) {
        WorldPlayer player = playerService.getWorldPlaqerByAccountId(accountId);
        if (player != null) {
            FeePlan feePlan = player.getFeePlan();
            if(player.getLevel()==feePlan.getBeginLevel()){
                connectService.sendMessage(player.getId(),"恭喜你到达"+feePlan.getBeginLevel()+"级，请手机访问wap.pipfit.com，或与续费管理员对话续费。");
            }else{
                byte[] bytes = stageService.getTaskBytes((short) 31009,player.getLevel());
                UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK);
                seg.writeShort((short) 31009);
                seg.writeShort((short) 2);
                seg.write(bytes);
                connectService.writeTo(seg, player.getId());
            }
//            ChargePlan[] plan = player.getChargePlan();
//            String msg = player.getFeePlan().getContent();
//            String[] channelMsgs = new String[plan.length];
//            String[] urls = new String[plan.length];
//            for(int i=0;i<plan.length;i++){
//                channelMsgs[i] = plan[i].getContent();
//                urls[i] = plan[i].getServiceNo()+feeId;
//            }
////            String channelMsg = plan.getContent();
////            String url = plan.getServiceNo()+feeId;
//            if(plan.length==1){
//                byte[] bytes = stageService.getTaskBytes((short) 31006, new String[] {msg,
//                                                         channelMsgs[0], urls[0], "", "" + feeId});
//                UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK);
//                seg.writeShort((short) 31006);
//                seg.writeShort((short) 2);
//                seg.write(bytes);
//                connectService.writeTo(seg, player.getId());
//            }
//            else if(plan.length==2){
//                byte[] bytes = stageService.getTaskBytes((short) 31007, new String[] {msg,
//                                                         channelMsgs[0],channelMsgs[1], urls[0], "",""+feeId,urls[1],"", ""+feeId});
//                UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK);
//                seg.writeShort((short) 31007);
//                seg.writeShort((short) 2);
//                seg.write(bytes);
//                connectService.writeTo(seg, player.getId());
//            }
        }
    }

    public void notifyTime(int playerId, int mintues) {
        chatService.sendPrivateMessage( -1, "系统", playerId,
                                       "您的游戏时间还剩余" + mintues +
                                       "分钟，请登录wap.pipfit.com续费。");
    }


    public void synciMoney(int accountId, long iMoney,boolean isMonth,boolean isSubscribe) {
        WorldPlayer player = playerService.getWorldPlaqerByAccountId(accountId);
        if (player != null) {
            player.setLongIMoney(iMoney);
            player.setMonth(isMonth);
//            player.setSubscribe(isSubscribe);
//            if(needPay&&iMoney<=100&&player.getLevel()==){
//
//            }
            log.info("AccountID[" + player.getId() + "]ID[" + player.getId() +
                     "]SynciMoney[" + player.getLongIMoney() + "]");
        }
    }

    private long checkFees() {
        long begin = System.currentTimeMillis();
        Iterator ite = playerService.players();
        while (ite.hasNext()) {
            WorldPlayer player = (WorldPlayer) ite.next();
            try {
                checkFee(player);
            } catch (Exception ex) {
                log.error(ex, ex);
            }
        }
        long end = System.currentTimeMillis();
        return 60000L - (end - begin);
    }

    private void checkFee(WorldPlayer player) {
        if (player == null)
            return;
        long current = System.currentTimeMillis();
        if(player.isSubscribe())
            return;
        if (((player.getLastLifeTime() + 180L) >= current)) { //3分钟内有移动信息的用户认为是活动的
            FeePlan feePlan = player.getFeePlan();
            if(feePlan == null){
            	return;
            }
            if(player!=null&&feePlan!=null&&player.getLevel()==(feePlan.getBeginLevel()-1)&&player.getLongIMoney()<=0&&!player.isSubscribe()){
                connectService.sendMessage(player.getId(),"恭喜你到达"+player.getLevel()+"级，请手机访问wap.pipfit.com，或与续费管理员对话续费。");
            }
            if (player != null && player.getLevel() >= feePlan.getBeginLevel()) {
                if (player.getLongIMoney() <= 0) {
                    if (player.getLastFeeTime() + 60000L <= current) {

//                        byte[] bytes = stageService.getTaskBytes((short) 31009);
//                        UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK);
//                        seg.writeShort((short) 31009);
//                        seg.writeShort((short) 2);
//                        seg.write(bytes);
//                        connectService.writeTo(seg, player.getId());
                        player.setLastFeeTime(current);
                        int fee = feePlan.getFee() / 3;
                        long balance = player.getLongIMoney();
                        if(player.isNeedPay())
                            player.setLongIMoney(player.getLongIMoney() - fee);
                        LegacyFee1Message msg = new LegacyFee1Message(player.getAccountId(), player.getkey(),fee,(int)(balance-fee));
                        accountSkeleton.send(msg);
//                        UWAPSegment seg = new UWAPSegment(ServerConstants.FEE);
//                        seg.writeInt(player.getAccountId());
//                        seg.writeInt(fee);
//                        seg.writeInt(player.getiMoney());
//                        authSession.write(seg);
                        log.info("AccountID[" + player.getAccountId() + "]ID[" +
                                 player.getId() + "]Fee[" + fee +
                                 "]iMoney[" + player.getLongIMoney() + "]");
                    }
                }
                else if(player.getLongIMoney()<=10000&&player.getLevel()==feePlan.getBeginLevel()&&player.isNeedPay()){
                    connectService.sendMessage(player.getId(),"恭喜你到达"+feePlan.getBeginLevel()+"级，请手机访问wap.pipfit.com，或与续费管理员对话续费。");
                }else {
                    if ((player.getLastFeeTime() + 180000L) <= current) {
                        player.setLastFeeTime(current);
                        long balance = player.getLongIMoney();
                        int fee = feePlan.getFee();
                        if(player.isNeedPay())
                            player.setLongIMoney(player.getLongIMoney() - feePlan.getFee());
                        LegacyFee1Message msg = new LegacyFee1Message(player.getAccountId(), player.getkey(),feePlan.getFee(),(int)(balance-fee));
                        accountSkeleton.send(msg);
//                        UWAPSegment seg = new UWAPSegment(ServerConstants.FEE);
//                        seg.writeInt(player.getAccountId());
//                        seg.writeInt(feePlan.getFee());
//                        seg.writeInt(player.getiMoney());
//                        authSession.write(seg);
                        log.info("AccountID[" + player.getAccountId() + "]ID[" +
                                 player.getId() + "]Fee[" + feePlan.getFee() +
                                 "]iMoney[" + player.getLongIMoney() + "]");
                        if (player.getLongIMoney() <= 3000 &&
                            player.getLongIMoney() > 2700 &&
                            player.isNeedPay()) {
                            notifyTime(player.getId(), (int)(player.getLongIMoney() / 100));
                        } else if (player.getLongIMoney() <= 1500 &&
                                   player.getLongIMoney() > 1200 &&
                                   player.isNeedPay()) {
                            notifyTime(player.getId(), (int)(player.getLongIMoney() / 100));
                        } else if (player.getLongIMoney() <= 500 &&
                                   player.getLongIMoney() > 200 &&
                                   player.isNeedPay()) {
                            notifyTime(player.getId(), (int)(player.getLongIMoney() / 100));
                        }
                    }
                }
            }
//            if (player.getLevel() >= feePlan.getBeginLevel() &&
//                ((player.getLastFeeTime() + 180000L) < current)) {
//                player.setLastFeeTime(current);
//                player.setiMoney(player.getiMoney() - feePlan.getFee());
//                UWAPSegment seg = new UWAPSegment(ServerConstants.FEE);
//                seg.writeInt(player.getAccountId());
//                seg.writeInt(feePlan.getFee());
//                seg.writeInt(player.getiMoney());
//                authSession.write(seg);
//                log.info("AccountID[" + player.getAccountId() + "]ID[" +
//                         player.getId() + "]Fee[" + feePlan.getFee() +
//                         "]iMoney[" + player.getiMoney() + "]");
//            }
        }
    }

}
