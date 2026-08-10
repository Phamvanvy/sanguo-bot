package com.pip.itimes.server.world.battle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.IPlayerData;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.game.GameMap;
import com.pip.itimes.server.world.toplist.PlayerTopList;
import com.pip.itimes.server.bean.Player;
import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.Buf;
import com.pip.itimes.server.stage.DropGroup;
import com.pip.itimes.server.stage.DropGroups;
import com.pip.itimes.server.stage.DropItem;
import com.pip.itimes.server.stage.Grid;
import com.pip.itimes.server.stage.IEquipment;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.IItemTemplate;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.stage.Items;

public class WarPkBattle extends AbstractMultiPkBattle {

	private static int PK_RATE = 30;		// 宣战时，可以有百分之三十的几率掉落一级宝石
	private static int CLAUS_SOCKS_ID = 201081;			// 圣诞老人穿过的袜子Id
	private final static int sameCmpPlayer = 10;		//杀死同一阵营的玩家，扣除2荣誉点数
	private static boolean isforcepk = false;
	private WorldPlayer currentplayer = null;
	
    public WarPkBattle(int id, BattleService2 service,
                              BattleStrategy strategy, boolean force,
                              IPlayerData[] players1, IPlayerData[] players2,
                              int serial,boolean forcepk) {
        super(id, service, strategy, force, players1, players2, serial);
//        this.wager = wager;
        this.isforcepk = forcepk;
        currentplayer = (WorldPlayer) players1[0];//此处为发起战斗的那个人
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
    	if(isforcepk){//强制pk扣道具
    		 if(currentplayer != null){
    			 Changed changed = new Changed();
    			 currentplayer.completeRemoveItem(201609, 1, changed);
    			 service.getConnectService().sendGetItem(changed, currentplayer.getId(), (byte)0);
    			 currentplayer.setForcePkClock(new Date());
    		 }
    	}
    	checkDie();
        if (failure != null) {
//            UWAPSegment seg = new UWAPSegment(ClientConstants.GET_ITEM);
//            Changed changed = new Changed();
//            winner[0].addMoney(wager * 2, changed);
//            service.getConnectService().sendGetItem(changed, winner[0].getId(),
//                                                    (byte) 3);
//            service.getChatService().sendMapMessage(winner[0].getMapId(), -1, "系统",
//                                                    winner[0].getPlayerName() +
//                                                    "在决斗中战胜了" +
//                                                    failure[0].getPlayerName());
            Changed[] changed = new Changed[failure.length];

            for(int i=0;i<failure.length;i++){
                changed[i] = new Changed();
                if(failure[i].getMap()!=null&&failure[i].getMap().getPkType()==GameMap.PK_TYPE_HALF){
                    //failure[i].incDeadTime();
                	if(failure[i].getMap().getSafeType() == 0){
                		failure[i].incCampDeadTime(failure[i].getLevel());
                	}
                    if(failure[i].hasBuf(Buf.GUARD)){
                    	changed[i].setProperty(Changed.GUARDSTATE,1);
                    }
                }
                service.getBufService().checkBattleBuff(failure[i],changed[i]);

            }
            Changed[] changed1 = new Changed[winner.length];
            for(int i=0;i<winner.length;i++){
                changed1[i] = new Changed();
                service.getBufService().checkBattleBuff(winner[i],changed1[i]);
                service.getConnectService().sendGetItem(changed1[i],winner[i].getId(),(byte)3);
            }
            if(isSide1(failure)){  //偷袭方失败
                for(int i=0;i<winner.length;i++){
                	
                	//如果有每日的杀人任务，则进行杀人计数
            		byte campType = winner[i].getCamp();
                	//遍历偷袭方并开始计算杀人数
                	for(int k = 0; k < failure.length; k++){
                		if(failure[k].getCamp() == Utils.NO_CAMP){
                			continue;
                		}else{
//                			if(winner[i].getLevel() - failure[k].getLevel() <= 10){
                				int itemCampId  = winner[i].hasCampLoopTask();
                				if(itemCampId != 0){
                					//胜利者增加物品
                					
                					if(winner[i].getCamp() != failure[k].getCamp() && failure[k].getCamp() != Utils.NO_CAMP){
	                					IItemTemplate it = Items.getTemplate(itemCampId);
	                					winner[i].addItem(it, 1,
	                							changed1[i], winner[i].getClientDataVersion());
	                					
	                					winner[i].addCampWin(1);
                					}
                				}
                				itemCampId  = failure[k].hasCampLoopTask();
                				if(itemCampId != 0){
                					if(winner[i].getCamp() != failure[k].getCamp() || failure[k].getCamp() != Utils.NO_CAMP){
                						failure[k].addCampLost(1);
                					}
                				}
//                			}
                		}
                	}
	            		
                	
                	/*//计算荣誉
                	if(winner[i].getLevel() - failure[0].getLevel() > 10){*/
            		//进行荣誉计算
                	if(winner[i].getCamp() != failure[0].getCamp() && winner[i].getCamp() != Utils.NO_CAMP){
                		int winCredit = getWinCredit(failure[0].getLevel(),winner[i].getLevel());
                		if(winCredit>0){
                        	int credit = failure[0].decCredit(winCredit + 1, changed[0]);
                        	if (credit >= 1) {
                        		winner[i].addCredit((credit - 1), changed1[i]);
                            }
                        }else{  //如果等级相差太多要扣荣誉的
                            int difLevel = failure[0].getLevel() - winner[i].getLevel();
                            if (difLevel >= 7 || difLevel <= -7){
                                failure[0].decCredit(10, changed[0]);
                            }
                        }
                	}else{ //同一阵营的
                		winner[i].decCredit(sameCmpPlayer, changed1[i]);
                	}
                	
                    
                    	
                	/*}*/
                	
                	
                	/*//进行荣誉计算
            		int winCredit = getWinCredit(failure[0].getLevel(),winner[i].getLevel());
//                      boolean isSneak = false;
//                      if(service.getTopListService().playerTopList.isSneaker(failure[0].getId())){
//                          isSneak = true;
//                      }
                    if(winCredit>0){
                    	int credit = failure[0].decCredit(winCredit + 1, changed[0]);
//                          if(isSneak){
//                              credit = credit * 2 /3;
//                          }
                    	if (credit >= 1) {
                    		winner[i].addCredit(credit - 1, changed1[i]);
                        }
                    }else{  //如果等级相差太多要扣荣誉的
                        int difLevel = failure[0].getLevel() - winner[i].getLevel();

                        if (difLevel >= 7 || difLevel <= -7){
                            failure[0].decCredit(10, changed[0]);
                        }
                    }*/
                }
                
                //失败者计数
                
//                changed[0].setProperty(Changed.CREDIT,-failureCredit);
            }else{  //偷袭方胜利
                long current = System.currentTimeMillis();
                //进行杀人计数
            	for(int i=0;i<winner.length;i++){
            		//如果有每日的杀人任务，则进行杀人计数
            		byte campType = winner[i].getCamp();
                	//遍历偷袭方并开始计算杀人数
                	for(int k = 0; k < failure.length; k++){
                		if(failure[k].getCamp() == Utils.NO_CAMP){
                			continue;
                		}else{
//                			if(winner[i].getLevel() - failure[k].getLevel() <= 10){
                				int itemCampId  = winner[i].hasCampLoopTask();
                				if(itemCampId != 0){
                					//胜利者增加物品
                					if(winner[i].getCamp() != failure[k].getCamp() && failure[k].getCamp() != Utils.NO_CAMP){
	                					IItemTemplate it = Items.getTemplate(itemCampId);
	                					winner[i].addItem(it, 1,
	                							changed1[i], winner[i].getClientDataVersion());
	                					
	                					winner[i].addCampWin(1);
                					}
                				}
                				itemCampId  = failure[k].hasCampLoopTask();
                				if(itemCampId != 0){
                					if(winner[i].getCamp() != failure[k].getCamp() && failure[k].getCamp() != Utils.NO_CAMP){
                						failure[k].addCampLost(1);
                					}
                				}
//                			}
                		}
                	}
            		
            	}
            	//计算荣誉和加敌人，已经排行榜
                for(int i=0;i<failure.length;i++){
                	if(winner[0].getCamp() != failure[i].getCamp()){
                		int winCredit = getWinCredit(failure[i].getLevel(),winner[0].getLevel());
                        int lostCredit = winCredit+1;
                        if(winCredit>0){
                            if(service.getTopListService().playerTopList.isSneaker(failure[i].getId())){
                                lostCredit *= 3;
                                winCredit *= 2;
                            }
                            int credit = failure[i].decCredit(lostCredit, changed[i]);
                            if (credit >= 1) {
                                winner[0].addCredit(Math.min(winCredit,credit), changed1[0]);
                            }
                        }
                        int difLevel = winner[0].getLevel()-failure[i].getLevel();
                        if(difLevel>=7){
                            winner[0].decCredit(10,changed1[0]);
                        }
                    	/*}*/
                        failure[i].addEnemy(winner[0].getId(),winner[0].getPlayerName(),current);
                	}else{	//同一阵营的
                		winner[0].decCredit(sameCmpPlayer, changed1[0]);
                	}
                    //mengjie add 20110520 接取10141任务后，被杀 33%概率掉落 100704任务物品
                	Random rnd = new Random();
                	if (failure[i].hasTask((short)10141) && Utils.hit(rnd, 33, 100) && (failure[i].getLevel() > 49)){
	                	IItemTemplate it = Items.getTemplate(100704);
	                	failure[i].addItem(it, 1,changed[i], failure[i].getClientDataVersion());
	                	log.info("ID[" + failure[i].getId() + "]WarPkBattle fail-addItem itemID[" + 100704 + "]");
                	}
                }

                for(int i = 0; i < winner.length; i++){
                    winner[i].addSneaks(1);
                }
//                changed1[0].setProperty(Changed.CREDIT,totalCredit);
            }
            // 掉落宝石,掉袜子
            missItem(changed1, changed);
            for(int i=0;i<winner.length;i++){
            	//清除连续死亡记录    lisen add
            	winner[i].setDeadTime(0);
            	//mengjie add 掉耐久
                //ItemUtils.removeDurability(winner[i], false, changed1[i]);
            	boolean[] allDurability = null;
                if(winner[i] instanceof WorldPlayer){
                	WorldPlayer wp = (WorldPlayer)winner[i];
                	allDurability = ItemUtils.getAllDuragbility(wp);
                	ItemUtils.removeDurability(wp, false, changed1[i]);
                }
//                boolean[] allDurability = ItemUtils.getAllDuragbility(winner[i]);
//                ItemUtils.removeDurability(winner[i], false, changed1[i]);
                /*boolean[] allDownDurability = ItemUtils.getAllDownDuragbility(winner[i]);
                
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
                service.getConnectService().sendGetItem(changed1[i],winner[i].getId(),(byte)3);
                Utils.log(log, winner[i].getId(),
                        ClientConstants.PK_ROUND_END,
                        "MapId[" + winner[i].getMapId() +
                        "] Status[end] Total Money[" + winner[i].getMoeny() +
                        "] Total Credit[" + winner[i].getCredit() +
                        "] After the Victory of Pk");
            }
            
            List equDiamondList = new ArrayList(3); //默认玩家身上最多一个
            for(int i=0;i<failure.length;i++){
            	//mengjie add 掉耐久
                //ItemUtils.removeDurability(failure[i], true, changed[i]);
            	boolean[] allDurability = null;
                if(failure[i] instanceof WorldPlayer){
                	WorldPlayer wp = (WorldPlayer)failure[i];
                	allDurability = ItemUtils.getAllDuragbility(wp);
                	ItemUtils.removeDurability(wp, false, changed[i]);
                }
//            	boolean[] allDurability = ItemUtils.getAllDuragbility(failure[i]);
//                ItemUtils.removeDurability(failure[i], true, changed[i]);
              /*  boolean[] allDownDurability = ItemUtils.getAllDownDuragbility(failure[i]);
                
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
            	//搜索玩家身上没有固话的装备，并准备转移
                if(Server.player_Delay.containsKey(failure[i].getId())){
                	Map<IItem, Integer> itemMap = Server.player_Delay.get(failure[i].getId()).getEquDiamondTimeMap();
    	    		for(Map.Entry<IItem, Integer> equDiamond: itemMap.entrySet()){
    	    			IItem item = equDiamond.getKey();
    	    			Grid grid = failure[i].getEquipmentByInstanceid(item.getId());
    	    			if(grid != null){
    	    				if(failure[i].completeRemoveItem(grid.item, grid.item.getId(), changed[i]) != null){
    	    					service.getChatService().sendPrivateMessage(-1, "系统", failure[i].getId(), "由于你战斗失败了，物品" + grid.item.getName() + "消失了");
    	    					equDiamondList.add(grid.item);
    	    					log.info("ID["+failure[i].getId()+"] battle fail3 drop equDiamond["+Utils.getHexdump(grid.item.toDbBytes())+"]");
    	    				}
    	    			}else{
    	    				equDiamondList.add(item);
    	    				log.info("ID["+failure[i].getId()+"] battle fail4 drop equDiamond["+Utils.getHexdump(item.toDbBytes())+"]");
    	    			}
    	    		}
    	    		Server.player_Delay.remove(failure[i].getId());
                }
            	
                service.getConnectService().sendGetItem(changed[i],failure[i].getId(),(byte)3);
                
                Utils.log(log, failure[i].getId(),
                        ClientConstants.PK_ROUND_END,
                        "MapId[" + failure[i].getMapId() +
                        "] Status[end] Total Money[" + failure[i].getMoeny() +
                        "] Total Credit[" + failure[i].getCredit() +
                        "] After the Defeat of Pk");
            }
            if(equDiamondList.size() > 0){
	            IEquipment[] equs = new IEquipment[equDiamondList.size()];
	        	equDiamondList.toArray(equs);
	        	service.getFallService().addFalls(winner, equs);
            }
        	
            service.removeBattle(this);
        }else{
            service.removeBattle(this);
        }
        if(winner!=null&&failure!=null){ //是偷袭
        	
            byte sendCampFlag = 0; //0为默认不发正营  胜利者 1为发黑暗勇士 2为发光明勇士 3为黑暗名人 4为光明名人
        	List<Player> sneaks = PlayerTopList.playerSneaksCache;
    		for(int k = 0; k < sneaks.size(); k++){
    			Player player = sneaks.get(k);
    			for(int i = 0; i < failure.length; i++){
    				if(player.getId() == failure[i].getId()){
    					sendCampFlag = failure[i].getCamp();
    					break;
    				}
    			}
    		}
        	
        	if(sendCampFlag == 0){
        		List<Player> ibuy = PlayerTopList.playerIbuyCache;
        		for(int k = 0; k < ibuy.size(); k++){
        			Player player = ibuy.get(k);
        			for(int i = 0; i < failure.length; i++){
        				if(player.getId() == failure[i].getId()){
        					sendCampFlag = (byte) (failure[i].getCamp() + 2);
        					break;
        				}
        			}
        		}
        	}
        	
        	if(sendCampFlag != 0){//开始发送阵营信息
        		if(sendCampFlag == Utils.CAMP_DARK || sendCampFlag == Utils.CAMP_DARK + 2){
                	StringBuffer sendmessage1 = new StringBuffer();
                	sendmessage1.append( winner[0].getPlayerName());
                	for(int i = 1; i < winner.length; i++){
                		sendmessage1.append(",");
                		sendmessage1.append(winner[i].getPlayerName());
                	}
                	StringBuffer sendmessage2 = new StringBuffer();
                	sendmessage2.append( failure[0].getPlayerName());
                	for(int i = 1; i < failure.length; i++){
                		sendmessage2.append(",");
                		sendmessage2.append(failure[i].getPlayerName());
                	} 
                	String msg = "本阵营的";
                	if(sendCampFlag == Utils.CAMP_DARK){
                		msg += "勇士";
                	}else{
                		msg += "名人";
                	}
                	msg += sendmessage2.toString();
                	msg += "被敌方阵营的";
                	msg += sendmessage1.toString();
                	msg += "击败了！谁去帮帮他？";
//                	service.getChatService().sendCampMessage(-1, "系统", msg, Utils.CAMP_DARK);
                	
                	msg = "敌方阵营的匪首";
                	msg += sendmessage2.toString();
                	msg += "在本阵营的勇士";
                	msg += sendmessage1.toString();
                	msg += "面前一败涂地了！";
//                	service.getChatService().sendCampMessage(-1, "系统", msg, Utils.CAMP_BRIGHT);
                	
                	
        		}else if(sendCampFlag == Utils.CAMP_BRIGHT || sendCampFlag == Utils.CAMP_BRIGHT + 2){
                	StringBuffer sendmessage1 = new StringBuffer();
                	sendmessage1.append( winner[0].getPlayerName());
                	for(int i = 1; i < winner.length; i++){
                		sendmessage1.append(",");
                		sendmessage1.append(winner[i].getPlayerName());
                	}
                	StringBuffer sendmessage2 = new StringBuffer();
                	sendmessage2.append( failure[0].getPlayerName());
                	for(int i = 1; i < failure.length; i++){
                		sendmessage2.append(",");
                		sendmessage2.append(failure[i].getPlayerName());
                	} 
                	String msg = "本阵营的";
                	if(sendCampFlag == Utils.CAMP_BRIGHT){
                		msg += "勇士";
                	}else{
                		msg += "名人";
                	}
                	msg += sendmessage2.toString();
                	msg += "被敌方阵营的";
                	msg += sendmessage1.toString();
                	msg += "击败了！谁去帮帮他？";
//                	service.getChatService().sendCampMessage(-1, "系统", msg, Utils.CAMP_BRIGHT);
                	
                	msg = "敌方阵营的匪首";
                	msg += sendmessage2.toString();
                	msg += "在本阵营的勇士";
                	msg += sendmessage1.toString();
                	msg += "面前一败涂地了！";
//                	service.getChatService().sendCampMessage(-1, "系统", msg, Utils.CAMP_DARK);
        		}
        		
        	}
        	killKing();
        	//无条件发地区信息
        	StringBuffer sendmessage = new StringBuffer();
        	//先检索失败者里面有没有名人，或者恶人榜上的
        	sendmessage.append( winner[0].getPlayerName());
        	for(int i = 1; i < winner.length; i++){
        		sendmessage.append(",");
        		sendmessage.append(winner[i].getPlayerName());
        	}
        	sendmessage.append("在宣战中战胜了");
        	sendmessage.append( failure[0].getPlayerName());
        	for(int i = 1; i < failure.length; i++){
        		sendmessage.append(",");
        		sendmessage.append(failure[i].getPlayerName());
        	} 
        	//2012年9月13日11:40:42 去掉杀人公告
//        	service.getChatService().sendMapMessage(winner[0].getMapId(), -1, "系统",
//                         sendmessage.toString());
          
        }
        
        
//        log.info("ID[" + side1[0].player.getId() + "]Money[" +
//                 side1[0].player.getMoeny() +
//                 "]Dest[" + side2[0].player.getId() + "]Money[" +
//                side2[0].player.getMoeny() + "]Wager[" + wager + "]ENDED");
    }
    
    private void checkDie() {
        if(side1.length > 1){ //多人组队状态
            for(int i = 0; i < side1.length; i++){
                if(side1[i].getDebufStatus() == Skill.STATUS_DIE){
                    service.changeTeamStateToNormal(side1[i]);
                }
            }
        }
        if(side2.length > 1){ //多人组队状态
            for(int i = 0; i < side2.length; i++){
                if(side2[i].getDebufStatus() == Skill.STATUS_DIE){
                    service.changeTeamStateToNormal(side2[i]);
                }
            }
        }
    }
    
    /**
     * 本阵营的国王被击杀，则发阵营聊
     */
    private void killKing(){
    	//国王被击杀，发阵营聊
    	StringBuffer sendmessage1 = new StringBuffer();
    	sendmessage1.append( winner[0].getPlayerName());
    	for(int i = 1; i < winner.length; i++){
    		sendmessage1.append(",");
    		sendmessage1.append(winner[i].getPlayerName());
    	}
    	for(int i = 0; i < failure.length; i++){
    		int camp = failure[i].getCamp();
    		if(camp == 1 || camp == 2){
    			int kingID = service.getCampMainService().getKingId(camp);
    			String campName="光明阵营";
    			int enemyCamp = 1;												//敌对阵营为黑暗阵营
    			if(camp == 1){
    				enemyCamp = 2;
    				campName="黑暗阵营";
    			}
    			if(kingID == failure[i].getId()){
    				service.getChatService().sendCampMessage(-1, "系统", "本阵营的领袖"+failure[i].getPlayerName()+"被敌方阵营的"+sendmessage1.toString()+"击败了！谁去帮帮他？",camp);
    				service.getChatService().sendCampMessage(-1, "系统", "敌方阵营的领袖"+failure[i].getPlayerName()+"在本阵营的勇士"+sendmessage1.toString()+"面前一败涂地了！",enemyCamp);
    				break;
    			}
    		}	
		}
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

    private int getWinCredit(int failureLevel,int winLevel){
        int l = failureLevel-winLevel;
        if(l>=-3&&l<=3)
            return 3;
        if(l>=4&&l<=6)
            return 6;
        if(l>=7)
            return 9;
        if(l>=-6&&l<=-4)
            return 2;
        if(l<=-7)
            return 0;
        return 0;
    }
    
    
    /**
     * @param failureLevel
     * @param winLevel
     * @return阵营荣誉
     */
    private int getWinCampCredit(int failureLevel,int winLevel){
    	int l = failureLevel-winLevel;
    	if(l <= -10){
    		l = 0;
    	}
        return l;
    }

    
    protected void ok(int playerId) {

    }

    protected void refuse(int playerId, byte code, String cause) {

    }

    public void start() {
       lastTime = System.currentTimeMillis();
       status = STATUS.wait_start;
//       log.info("ID[" + side1[0].player.getId() + "]Money[" +
//         side1[0].player.getMoeny() +
//         "]Dest[" + side2[0].player.getId() + "]Money[" +
//         side2[0].player.getMoeny() + "]Wager[" + wager + "]BEGIN");
//       if (wager > 0) {
//           side1[0].player.setMoeny(side1[0].player.getMoeny() - wager);
//           side2[0].player.setMoeny(side2[0].player.getMoeny() - wager);
//           Changed changed = new Changed();
//           changed.setProperty(Changed.MONEY, -wager);
//           service.getConnectService().sendGetItem(changed, side1[0].id,
//                   (byte) 7);
//           service.getConnectService().sendGetItem(changed, side2[0].id,
//                   (byte) 7);
//       }
       sendPkStart();
       lastTime = System.currentTimeMillis();
       status = STATUS.wait_fight;
//       log.info("ID[" + side1[0].player.getId() + "]Money[" +
//                side1[0].player.getMoeny() +
//                "]Dest[" + side2[0].player.getId() + "]Money[" +
//                side2[0].player.getMoeny() + "]Wager[" + wager + "]BEGIN");


    }
    
    public void missItem(Changed[] changed1, Changed[] changed) {
    	// 掉落宝石
        Random rnd = new Random();
        for (int k = 0; k < winner.length; k ++) {
        	if (winner[k].getCamp() == Utils.NO_CAMP) {
        		continue;
        	}
        	for (int i = 0; i < failure.length; i ++) {
        		
        		if (failure[i].getLevel() > 50 && failure[i].getCamp() != winner[k].getCamp()) {
        			if (!Utils.hit(PK_RATE, 100)) {
        				continue;
        			}
        			if (!service.isGetGem(failure[i].getId(),winner[k].getId())) {
        				continue;
        			}
        			IItemTemplate dropitem = Items.getTemplate(201612);//宝石粉末
        			if(dropitem != null){
        				service.setPlayer2Gem(failure[i].getId(),winner[k].getId());
    		            winner[k].addItem(dropitem, 1,
    							changed1[k], winner[k].getClientDataVersion());
    		            log.info("(BattleField)Player ID[" + winner[k] + "]get item id[" +
    		            		dropitem.getItemId()+ "]");
        			}
//        			DropGroup group = DropGroups.getDropGroup(247, 1);
//    	    		if (group != null) {
//    		    		int rate = rnd.nextInt(group.getRate());
//    		            DropItem dropItem = group.calcDropItem(
//    		                    rate);
//    		            service.setPlayer2Gem(failure[i].getId(),winner[k].getId());
//    		            winner[k].addItem(dropItem.getItem(), 1,
//    							changed1[k], winner[k].getClientDataVersion());
//    		            log.info("(BattleField)Player ID[" + winner[k] + "]get Diamond id[" +
//    		            		dropItem.getItem().getItemId()+ "]");
//    	    		}
        		}
        	}
        }
        
        // 掉落圣诞老人袜
        boolean mark = false;
    	for (int i = 0; i < failure.length; i ++) {
    		if (failure[i].getLevel() >= 50 && failure[i].hasItem(CLAUS_SOCKS_ID)) {
    			int rate = getAddIndex(rnd, 0, winner.length - 1, winner, failure[i], mark);
    			if (rate >= 0) {
    				for (int j = 0; j < winner.length; j ++) {
    					if (rate == j) {
    						IItem item = Items.getTemplate(CLAUS_SOCKS_ID).newInstance();
    						failure[i].completeRemoveItem(item, 1, changed[i]);
    						IItemTemplate template = Items.getTemplate(CLAUS_SOCKS_ID);
    						winner[j].addItem(template, 1, changed1[j], winner[j].getClientDataVersion());
    						break;
    					}
    				}
    			} else {
    				break;
    			}
        	}
    	}
    }
    
    /**
     * 返回可以获得袜子的winner下标
     * @param rnd
     * @param min
     * @param max
     * @param winner
     * @param failure
     * @return
     */
    public int getAddIndex (Random rnd, int min, int max, IPlayerData[] winner, IPlayerData failure, boolean mark) {
    	for (int i = 0; i < winner.length; i ++) {
    		if (winner[i].getCamp() > Utils.NO_CAMP
    				&& failure.getCamp() != winner[i].getCamp() && winner[i].getLevel() >= 50) {
    			mark = true;
    			break;
    		}
    	}
    	if (mark) {
    		IntList index = new ArrayIntList(1);
    		while (index.size() < 1) {
    			int v = Utils.getCount(rnd, min, max);
    			if (!index.contains(v) && winner[v].getCamp() != Utils.NO_CAMP
    					 && winner[v].getLevel() >= 50 && failure.getCamp() != winner[v].getCamp()) {
    				index.add(v);
    			}
    		}
    		return index.get(0);
    	} else {
    		return -1;
    	}
    }
}
