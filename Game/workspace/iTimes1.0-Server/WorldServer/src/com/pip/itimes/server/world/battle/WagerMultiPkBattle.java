package com.pip.itimes.server.world.battle;

import java.util.Date;

import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.Grid;
import com.pip.itimes.server.stage.IEquipment;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.world.IPlayerData;
import com.pip.itimes.server.world.WorldPlayer;

public class WagerMultiPkBattle extends AbstractMultiPkBattle {

    protected int wager = 0;

    public WagerMultiPkBattle(int id, BattleService2 service,
                              BattleStrategy strategy, boolean force,
                              IPlayerData[] players1, IPlayerData[] players2,
                              int serial,int wager) {
        super(id, service, strategy, force, players1, players2, serial);
        this.wager = wager;
    }

    public void abort() {
        for(int i=0;i<side1.length;i++){
            sendAbort(side1[i], pet1[i], serial);
        }
        for(int i=0;i<side2.length;i++){
            sendAbort(side2[i],pet2[i],serial);
        }
        status = STATUS.end;
        service.removeBattle(this);
    }

    public void end() {
        if (wager > 0 && winner != null && failure != null) {
//            UWAPSegment seg = new UWAPSegment(ClientConstants.GET_ITEM);
            Changed[] changed = new Changed[winner.length];
            for(int i=0;i<changed.length;i++){
                changed[i] = new Changed();
            }
            if(winner[0].getMoeny()>=0&&failure[0].getMoeny()>=0){
                winner[0].addMoney(wager * 2, changed[0]);
            }else{
                log.info("ID["+winner[0].getId()+"]Dest["+failure[0].getId()+"]CHEAT");
            }
            for(int i=0;i<winner.length;i++){
                service.getBufService().checkBattleBuff(winner[i],changed[i]);
                //mengjie add 掉耐久
                //ItemUtils.removeDurability(winner[i], false, changed[i]);
                
                boolean[] allDurability = null;
                if(winner[i] instanceof WorldPlayer){
                	WorldPlayer wp = (WorldPlayer)winner[i];
                	allDurability = ItemUtils.getAllDuragbility(wp);
                	ItemUtils.removeDurability(wp, false, changed[i]);
                }
//                boolean[] allDurability = ItemUtils.getAllDuragbility(winner[i]);
//                ItemUtils.removeDurability(winner[i], false, changed[i]);
               /* boolean[] allDownDurability = ItemUtils.getAllDownDuragbility(winner[i]);
                
                for(int k= 0; k < allDurability.length; k++ ){
                	if(allDurability[k]){//需要发聊的无论是发一次还是每次都发
                		if(allDownDurability[k]){//满走先前》5现在《5，或一直是0
                			Grid grid = winner[i].getLimitUsedEquipments(k);
                    		IEquipment iEquipment=(IEquipment) grid.item;
                    		//如果是过期则不发私聊 
                    		if(iEquipment != null && (new Date()).getTime() > iEquipment.getFAILURE_TIME()){//当日已超过过期日期
                    			if (iEquipment.getFAILURE_TIME() != -1){
                    				continue;
                    			}
                    		}
                    		service.getChatService().sendPrivateMessage(-1, "系统", winner[i].getId(), "你的装备"+iEquipment.getName()+"耐久度为"
                    				+ iEquipment.getCurrentDurability() + "，为了你的正常使用请拿去修理");
                		}
                	}
                }*/
                service.getConnectService().sendGetItem(changed[i], winner[i].getId(),
                                                    (byte) 3);
                
            }
            Changed[] changed1 = new Changed[failure.length];
            for (int i = 0; i < failure.length; i++) {
                changed1[i] = new Changed();
                service.getBufService().checkBattleBuff(failure[i], changed1[i]);
                //mengjie add 掉耐久
                //ItemUtils.removeDurability(failure[i], true, changed1[i]);
                boolean[] allDurability = null;
                if(failure[i] instanceof WorldPlayer){
                	WorldPlayer wp = (WorldPlayer)failure[i];
                	allDurability = ItemUtils.getAllDuragbility(wp);
                	ItemUtils.removeDurability(wp, false, changed1[i]);
                }
//                boolean[] allDurability = ItemUtils.getAllDuragbility(failure[i]);
//                ItemUtils.removeDurability(failure[i], true, changed1[i]);
               /* boolean[] allDownDurability = ItemUtils.getAllDownDuragbility(failure[i]);
                
                for(int k= 0; k < allDurability.length; k++ ){
                	if(allDurability[k]){//需要发聊的无论是发一次还是每次都发
                		if(allDownDurability[k]){//满走先前》5现在《5，或一直是0
                			Grid grid = failure[i].getLimitUsedEquipments(k);
                    		IEquipment iEquipment=(IEquipment) grid.item;
                    		//如果是过期则不发私聊 
                    		if(iEquipment != null && (new Date()).getTime() > iEquipment.getFAILURE_TIME()){//当日已超过过期日期
                    			if (iEquipment.getFAILURE_TIME() != -1){
                    				continue;
                    			}
                    		}
                    		service.getChatService().sendPrivateMessage(-1, "系统", failure[i].getId(), "你的装备"+iEquipment.getName()+"耐久度为"
                    				+ iEquipment.getCurrentDurability() + "，为了你的正常使用请拿去修理");
                		}
                	}
                }*/
                service.getConnectService().sendGetItem(changed1[i], failure[i].getId(),
                        (byte) 3);                
            }
            service.getChatService().sendMapMessage(winner[0].getMapId(), -1, "系统",
                                                    winner[0].getPlayerName() +
                                                    "在决斗中战胜了" +
                                                    failure[0].getPlayerName());
            service.removeBattle(this);
        }else{
            service.removeBattle(this);
        }
        log.info("ID[" + side1[0].player.getId() + "] Money[" +
                 side1[0].player.getMoeny() +
                 "] Credit[" + side1[0].player.getCredit() + "] Dest[" + side2[0].player.getId() + "] Money[" +
                side2[0].player.getMoeny() + "] Credit[" + side2[0].player.getCredit() + "] Wager[" + wager + "]ENDED");
    }

    public IPlayerData[] getPlayers() {
    	IPlayerData[] ret = new IPlayerData[side1.length+side2.length];
        int i = 0;
        for(;i<side1.length;i++){
            ret[i] = side1[i].player;
        }
        for(int j=0;j<side2.length;i++,j++){
            ret[i] = side2[j].player;
        }
        return ret;
    }

    protected void ok(int playerId) {

    }

    protected void refuse(int playerId, byte code, String cause) {

    }

    public void start() {
       lastTime = System.currentTimeMillis();
       status = STATUS.wait_start;
       log.info("ID[" + side1[0].player.getId() + "]Money[" +
         side1[0].player.getMoeny() +
         "]Dest[" + side2[0].player.getId() + "]Money[" +
         side2[0].player.getMoeny() + "]Wager[" + wager + "]BEGIN");
       if (wager > 0) {
           if(side1[0].player.getMoeny()>=wager&&side2[0].player.getMoeny()>=wager){
               side1[0].player.setMoeny(side1[0].player.getMoeny() - wager);
               side2[0].player.setMoeny(side2[0].player.getMoeny() - wager);
               Changed changed = new Changed();
               changed.setProperty(Changed.MONEY, -wager);
               service.getConnectService().sendGetItem(changed, side1[0].id,
                       (byte) 7);
               service.getConnectService().sendGetItem(changed, side2[0].id,
                   (byte) 7);
           } else {
               wager = 0;
               log.info("ID[" + side1[0].player.getId() + "]Money[" +
                        side1[0].player.getMoeny() +
                        "]Dest[" + side2[0].player.getId() + "]Money[" +
                        side2[0].player.getMoeny() + "]Wager[" + wager + "]CHEATBEGIN"
                       );
           }

       }
       sendPkStart();
       lastTime = System.currentTimeMillis();
       status = STATUS.wait_fight;
//       log.info("ID[" + side1[0].player.getId() + "]Money[" +
//                side1[0].player.getMoeny() +
//                "]Dest[" + side2[0].player.getId() + "]Money[" +
//                side2[0].player.getMoeny() + "]Wager[" + wager + "]BEGIN");


    }
}
