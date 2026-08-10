package com.pip.itimes.server.world.battle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.server.bean.Player;
import com.pip.itimes.server.stage.Buf;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.DropGroup;
import com.pip.itimes.server.stage.DropGroups;
import com.pip.itimes.server.stage.DropItem;
import com.pip.itimes.server.stage.Grid;
import com.pip.itimes.server.stage.IEquipment;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.IItemTemplate;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.IPlayerData;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.battle.Battle2.STATUS;
import com.pip.itimes.server.world.game.GameMap;
import com.pip.itimes.server.world.toplist.PlayerTopList;

public class WarTeamBattle extends AbstractMultiPkBattle {

	private static int PK_RATE = 30;		// 宣战时，可以有百分之三十的几率掉落一级宝石
	private static int CLAUS_SOCKS_ID = 201081;			// 圣诞老人穿过的袜子Id
	private final static int sameCmpPlayer = 10;		//杀死同一阵营的玩家，扣除2荣誉点数
	private static boolean isforcepk = false;
	private WorldPlayer currentplayer = null;
	
	 public WarTeamBattle(int id, BattleService2 service,
             BattleStrategy strategy, boolean force,
             IPlayerData[] players1, IPlayerData[] players2,
             int serial,boolean forcepk) {
		 super(id, service, strategy, force, players1, players2, serial);
		 this.isforcepk = forcepk;
		 currentplayer = (WorldPlayer) players1[0]; 
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
	@Override
	protected void ok(int playerId) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void refuse(int playerId, byte code, String cause) {
		// TODO Auto-generated method stub

	}

	@Override
	public void end(){
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
            Changed[] changed = new Changed[failure.length];		//失败方发送

            for(int i=0;i<failure.length;i++){
                changed[i] = new Changed();
                if(failure[i].getMap()!=null&&failure[i].getMap().getPkType()==GameMap.PK_TYPE_HALF){
                	if(failure[i].getMap().getSafeType() == 0){
                		failure[i].incCampDeadTime(failure[i].getLevel());
                	}
                    if(failure[i].hasBuf(Buf.GUARD)){
                    	changed[i].setProperty(Changed.GUARDSTATE,1);
                    }
                }
                service.getBufService().checkBattleBuff(failure[i],changed[i]);
            }
            Changed[] changed1 =  new Changed[winner.length];   //获胜方发送
            for(int i=0;i<winner.length;i++){
                changed1[i] = new Changed();
                service.getBufService().checkBattleBuff(winner[i],changed1[i]);
                service.getConnectService().sendGetItem(changed1[i],winner[i].getId(),(byte)3);
            }
            int[][] failerNum = new int[winner.length][failure.length];					// 失败方的损失的荣誉
            if(isSide1(failure)){  //偷袭方失败
                for(int i=0;i<winner.length;i++){
                	int qs = 0;	// 杀了本阵营的玩家，除固定的点数q。
                	int winCredit = 0;
                	//如果有每日的杀人任务，则进行杀人计数
            		byte campType = winner[i].getCamp();		//阵营ID
            		if(campType == Utils.NO_CAMP){
            			//胜方没有阵营，不获得荣誉和任务物品
            			continue;
            		}
                	//遍历偷袭方并开始计算杀人数
                	for(int k = 0; k < failure.length; k++){
                		if(failure[k].getCamp() == Utils.NO_CAMP){	//被杀人没有阵营
                			//进行荣誉计算
                			if(failure[k].getCredit() != 0){
                				int temp = getWinCredit(failure[k].getLevel(),winner[i].getLevel());
                        		if(temp > 0 ){
                        			failerNum[i][k] = temp + 1;			//失败方扣除的荣誉
                        			winCredit += temp;
                        		}else{
                        			int difLevel = failure[k].getLevel() - winner[i].getLevel();
                                    if (difLevel >= 7 || difLevel <= -7){
                                    	failerNum[i][k] = 10;		//扣除宣战方的10点荣誉
                                    }
                        		}
                			}
                			continue;
                		}if(failure[k].getCamp() == campType){		//被杀人同阵营
                			qs += sameCmpPlayer;
                			failerNum[i][k] = 0;					//误杀同意阵营的玩家，不产出荣誉
                			continue;
                		}else{
                			//进行荣誉计算
                			if(failure[k].getCredit() != 0){
                				int temp = getWinCredit(failure[k].getLevel(),winner[i].getLevel());
                        		if(temp > 0 ){
                        			failerNum[i][k] = temp + 1;			//失败方扣除的荣誉
                        			winCredit += temp;
                        		}else{
                        			int difLevel = failure[k].getLevel() - winner[i].getLevel();
                                    if (difLevel >= 7 || difLevel <= -7){
                                    	failerNum[i][k] = 10;		//扣除宣战方的10点荣誉
                                    }
                        		}
                			}
                    		
                    		// 物品的计算
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
                		}
                	}
            		//进行荣誉计算
                	winCredit = (winCredit - qs)/winner.length;
                    if(winCredit>0){
                    	winner[i].addCredit(winCredit, changed1[i]);
                    }else{
                    	winner[i].decCredit(winCredit*(-1), changed1[i]);
                    }
                }
                for(int i = 0; i < failure.length; i ++)		// 失败方的荣誉
                {	
                	int temp = 0;
                	for(int j = 0; j < winner.length; j++ )
                	{
                		temp += failerNum[j][i];
                	}
                	if(temp < 0){
                		temp = 0;
                	}
                	failure[i].decCredit(temp/winner.length, changed[i]);
                }
            }else{  //偷袭方胜利
                long current = System.currentTimeMillis();
                //进行杀人计数
            	for(int i=0;i<winner.length;i++){
            		int qs = 0;	// 杀了本阵营的玩家，除固定的点数q。
            		int winCredit = 0;
            		//如果有每日的杀人任务，则进行杀人计数
            		byte campType = winner[i].getCamp();
                	//遍历偷袭方并开始计算杀人数
            		if(campType == Utils.NO_CAMP){
            			continue;		//胜方没有阵营，不获得荣誉和任务物品
            		}
                	for(int k = 0; k < failure.length; k++){
                		if(failure[k].getCamp() == Utils.NO_CAMP){
                			continue;
                		}else if(campType == failure[k].getCamp()){
                			continue;
                		}else{
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
                		}
                	}
                	// 荣誉的计算
            		for( int fail = 0; fail < failure.length; fail++ )
            		{
            			if(failure[fail].getCredit() == 0){
            				continue;
            			}
            			if(campType == failure[fail].getCamp()){
            				// 同一阵营的玩家
                			qs += sameCmpPlayer;
                			failerNum[i][fail] = 0;
            				continue;
            			}
            			int winCreditTep = getWinCredit(failure[fail].getLevel(),winner[i].getLevel());
            			int lostCredit = winCreditTep+1;
            			if(winCreditTep>0){
                            if(service.getTopListService().playerTopList.isSneaker(failure[fail].getId())){
                            	failerNum[i][fail] = lostCredit * 3;
                            	winCreditTep *= 2;
                            }else{
                            	failerNum[i][fail] = lostCredit;
                            }
                            winCredit += winCreditTep;			//荣誉的计算
                        }

                        int difLevel = winner[i].getLevel()-failure[fail].getLevel();
                        if(difLevel>=7){
                        	winCredit -= 10;
                        	failerNum[i][fail] = 0;
                        }
                        failure[fail].addEnemy(winner[i].getId(),winner[i].getPlayerName(),current);
            		}
            		winCredit = winCredit - qs;
            		if(winCredit < 0){
            			winner[i].decCredit((-1)*winCredit/winner.length, changed1[i]);
            		}else{
            			winner[i].addCredit(winCredit/winner.length, changed1[i]);
            		}
            		
            	}
            	//计算荣誉和加敌人，已经排行榜
                for(int i = 0; i < failure.length; i++){
                	int temp = 0;
                    for(int k =0; k < winner.length;k ++)
                    {
                    	temp += failerNum[k][i];
                    }
                    if(temp >= 0){
                    	failure[i].decCredit(temp/winner.length, changed[i]);
                    }  
                }
                for(int i = 0; i < winner.length; i++){
                    winner[i].addSneaks(1);
                }
            }
            
            // 掉落宝石,掉袜子
            missItem(changed1, changed);
            
            for(int i=0;i<winner.length;i++){
            	//清除连续死亡记录    lisen add
            	winner[i].setDeadTime(0);
            	//掉耐久
            	boolean[] allDurability = null;
                if(winner[i] instanceof WorldPlayer){
                	WorldPlayer wp = (WorldPlayer)winner[i];
                	allDurability = ItemUtils.getAllDuragbility(wp);
                	ItemUtils.removeDurability(wp, false, changed1[i]);
                }
//                boolean[] allDurability = ItemUtils.getAllDuragbility(winner[i]);
//                ItemUtils.removeDurability(winner[i], false, changed1[i]);
               
                service.getConnectService().sendGetItem(changed1[i],winner[i].getId(),(byte)3);
                
                Utils.log(log, winner[i].getId(),
                        ClientConstants.PK_ROUND_END,
                        "MapId[" + winner[i].getMapId() +
                        "] Status[end] Total Money[" + winner[i].getMoeny() +
                        "] Total Credit[" + winner[i].getCredit() +
                        "] After the Victory of MultiPk");
            }
            List equDiamondList = new ArrayList(3); //默认玩家身上最多一个
            for(int i=0;i<failure.length;i++){
            	// add 掉耐久
            	 if(Server.player_Delay.containsKey(failure[i].getId())){
                 	Map<IItem, Integer> itemMap = Server.player_Delay.get(failure[i].getId()).getEquDiamondTimeMap();
     	    		for(Map.Entry<IItem, Integer> equDiamond: itemMap.entrySet()){
     	    			IItem item = equDiamond.getKey();
     	    			Grid grid = failure[i].getEquipmentByInstanceid(item.getId());
     	    			if(grid != null){
     	    				if(failure[i].completeRemoveItem(grid.item, grid.item.getId(), changed[i]) != null){
	         					equDiamondList.add(grid.item);
	         					service.getChatService().sendPrivateMessage(-1, "系统", failure[i].getId(), "由于你战斗失败了，物品" + grid.item.getName() + "消失了");
	         					log.info("ID["+failure[i].getId()+"] battle fail drop equDiamond["+Utils.getHexdump(grid.item.toDbBytes())+"]");
     	    				}
     	    			}else{
    	    				equDiamondList.add(item);
    	    				log.info("ID["+failure[i].getId()+"] battle fail2 drop equDiamond["+Utils.getHexdump(item.toDbBytes())+"]");
    	    			}
     	    		}
     	    		Server.player_Delay.remove(failure[i].getId());
                 }
            	 boolean[] allDurability = null;
                 if(failure[i] instanceof WorldPlayer){
                 	WorldPlayer wp = (WorldPlayer)failure[i];
                 	allDurability = ItemUtils.getAllDuragbility(wp);
                 	ItemUtils.removeDurability(wp, false, changed[i]);
                 }
//            	boolean[] allDurability = ItemUtils.getAllDuragbility(failure[i]);
//                ItemUtils.removeDurability(failure[i], true, changed[i]);
                service.getConnectService().sendGetItem(changed[i],failure[i].getId(),(byte)3);
                
                Utils.log(log, failure[i].getId(),
                        ClientConstants.PK_ROUND_END,
                        "MapId[" + failure[i].getMapId() +
                        "] Status[end] Total Money[" + failure[i].getMoeny() +
                        "] Total Credit[" + failure[i].getCredit() +
                        "] After the Defeat of MultiPk");
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
//        	service.getChatService().sendMapMessage(winner[0].getMapId(), -1, "系统",
//                         sendmessage.toString());
          
        }             
    
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
    
	@Override
	public IPlayerData[] getPlayers() {
		// TODO Auto-generated method stub
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
    
	@Override
	public void start() {
		// TODO Auto-generated method stub

	       lastTime = System.currentTimeMillis();
	       status = STATUS.wait_start;
	       sendPkStart();
	       lastTime = System.currentTimeMillis();
	       status = STATUS.wait_fight;
	    
	}

	public void missItem (Changed[] changed1, Changed[] changed) {
		// 掉落宝石
		Random rnd = new Random();
        for (int  k =0; k< winner.length;k++ ) {
        	if (winner[k].getCamp() == Utils.NO_CAMP) {
        		continue;
        	}
        	for (int i =0; i < failure.length; i ++) {
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
//    		            log.info("(BattleField)Player ID[" + winner[k].getId() + "]get Diamond id[" +
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
    		if (winner[i].getCamp() > Utils.NO_CAMP &&
    				failure.getCamp() != winner[i].getCamp() && winner[i].getLevel() >= 50) {
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
