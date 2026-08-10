package com.pip.itimes.server.world.battle;

import java.util.Date;

import com.pip.itimes.server.world.IPlayerData;
import com.pip.itimes.server.world.InstanceDefinition;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.game.Instance;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.Grid;
import com.pip.itimes.server.stage.IEquipment;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.world.game.BattleInstanceModel;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class CreditPkBattle extends AbstractPkBattle {

//    protected int credit;
    protected Instance instance;
    protected BattleInstanceModel model;

    public CreditPkBattle(int id, BattleService2 service,
                         BattleStrategy strategy,
                         WorldPlayer p1, WorldPlayer p2, int serial, int credit,Instance instance,BattleInstanceModel model) {
        super(id, service, strategy, false, p1, p2, serial);
//        this.credit = credit;
        this.instance = instance;
        this.model = model;
    }

    public IPlayerData[] getPlayers(){
        return new IPlayerData[]{side1[0].player,side2[0].player};
    }

    public void abort() {
        sendAbort(side1[0],pet1[0],serial);
        sendAbort(side2[0],pet2[0],serial);
        status = STATUS.end;
        model.battleEnded(this);
        service.removeBattle(this);
    }

    public synchronized void cancel(){
        if(status != STATUS.end){
            sendAbort(side1[0], pet1[0], serial);
            sendAbort(side2[0], pet2[0], serial);
            status = STATUS.end;
            if(side1[0].hp>side2[0].hp){
                winner = side1[0].player;
                failure = side2[0].player;
            }else{
                winner = side2[0].player;
                failure = side1[0].player;
            }
            InstanceDefinition idf = instance.getDefinition();
            service.sendGotoMap(failure.getId(),idf.getEntrance(),idf.getEntranceX(),idf.getEntranceY());
            service.sendGotoMap(winner.getId(),idf.getEntrance(),idf.getEntranceX(),idf.getEntranceY());
            model.battleEnded(this);
            service.removeBattle(this);
        }
    }

    public void end() {
        if(failure!=null){
            Changed changed1 = new Changed();
            Changed changed2 = new Changed();
            service.getBufService().checkBattleBuff(winner,changed1);
            service.getBufService().checkBattleBuff(failure,changed2);
//            winner.addCredit(credit*2, changed1);
//            Changed changed2 = new Changed();
//            failure.decCredit(credit, changed2);
            //mengjie add 掉耐久
            //ItemUtils.removeDurability(winner, false, changed1);
            boolean[] allDurability = null;
            if(winner instanceof WorldPlayer){
            	WorldPlayer wp = (WorldPlayer)winner;
            	allDurability = ItemUtils.getAllDuragbility(wp);
            	ItemUtils.removeDurability(wp, false, changed1);
            }
           /* boolean[] allDownDurability = ItemUtils.getAllDownDuragbility(winner);
            
            for(int k= 0; k < allDurability.length; k++ ){
            	if(allDurability[k]){//需要发聊的无论是发一次还是每次都发
            		if(allDownDurability[k]){//满走先前》5现在《5，或一直是0
            			Grid grid = winner.getLimitUsedEquipments(k);
                		IEquipment iEquipment=(IEquipment) grid.item;
                		//如果是过期则不发私聊 
                		if(iEquipment != null && (new Date()).getTime() > iEquipment.getFAILURE_TIME()){//当日已超过过期日期
                			if (iEquipment.getFAILURE_TIME() != -1){
                				continue;
                			}
                		}
                		service.getChatService().sendPrivateMessage(-1, "系统", winner.getId(), "你的装备"+iEquipment.getName()+"耐久度为"
                				+ iEquipment.getCurrentDurability() + "，为了你的正常使用请拿去修理");
            		}
            	}
            }*/
            
            if(failure instanceof WorldPlayer){
            	WorldPlayer wp = (WorldPlayer)failure;
            	allDurability = ItemUtils.getAllDuragbility(wp);
            	ItemUtils.removeDurability(wp, true, changed2);
            }
           /* allDownDurability = ItemUtils.getAllDownDuragbility(failure);
            
            for(int k= 0; k < allDurability.length; k++ ){
            	if(allDurability[k]){//需要发聊的无论是发一次还是每次都发
            		if(allDownDurability[k]){//满走先前》5现在《5，或一直是0
            			Grid grid = failure.getLimitUsedEquipments(k);
                		IEquipment iEquipment=(IEquipment) grid.item;
                		//如果是过期则不发私聊 
                		if(iEquipment != null && (new Date()).getTime() > iEquipment.getFAILURE_TIME()){//当日已超过过期日期
                			if (iEquipment.getFAILURE_TIME() != -1){
                				continue;
                			}
                		}
                		service.getChatService().sendPrivateMessage(-1, "系统", failure.getId(), "你的装备"+iEquipment.getName()+"耐久度为"
                				+ iEquipment.getCurrentDurability() + "，为了你的正常使用请拿去修理");
            		}
            	}
            }*/
            //ItemUtils.removeDurability(failure, true, changed2);
            service.getConnectService().sendGetItem(changed1, winner.getId(),
                    (byte) 0);
            service.getConnectService().sendGetItem(changed2,failure.getId(),(byte)0);
//            service.getConnectService().sendGetItem(changed2, failure.getId(),
//                    (byte) 0);
            InstanceDefinition idf = instance.getDefinition();
            service.sendFailureGotoMap(failure.getId(),idf.getEntrance(),idf.getEntranceX(),idf.getEntranceY());
            model.battleEnded(this);
            service.removeBattle(this);
        }
    }

    protected void ok(int playerId) {
    }

    protected void refuse(int playerId, byte code, String cause) {
    }

    public void start() {
        sendPkStart();
//        Changed changed1 = new Changed();
//        side1[0].player.decCredit(credit,changed1);
//        Changed changed2 = new Changed();
//        side2[0].player.decCredit(credit,changed2);
//        service.getConnectService().sendGetItem(changed1,side1[0].id,(byte)0);
//        service.getConnectService().sendGetItem(changed2,side2[0].id,(byte)0);
        status = STATUS.wait_fight;
        lastTime = System.currentTimeMillis();
    }

//    public synchronized void catchToBattle(int playerId) {
//
//    }

}
