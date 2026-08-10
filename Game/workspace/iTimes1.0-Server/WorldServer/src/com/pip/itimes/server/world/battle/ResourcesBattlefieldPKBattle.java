package com.pip.itimes.server.world.battle;

import java.util.Random;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

import com.pip.itimes.server.stage.Buf;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.IItemTemplate;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.IPlayerData;
import com.pip.itimes.server.world.InstanceDefinition;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.game.BattleForResourcesInstanceModel;
import com.pip.itimes.server.world.game.CampBattlefieldInstance;
import com.pip.itimes.server.world.game.CampBattlefieldPlayer;
import com.pip.itimes.server.world.game.GameMap;
import com.pip.itimes.server.world.game.Instance;

/**
 * 阵营战场战斗类：资源争夺战
 * @author hchen
 *
 */
public class ResourcesBattlefieldPKBattle extends AbstractMultiPkBattle {
	protected Instance instance;
    protected BattleForResourcesInstanceModel model;
    protected int loseRate;
    protected int resourcesID;

	public ResourcesBattlefieldPKBattle (int id, BattleService2 service, BattleStrategy strategy, boolean force,
			IPlayerData[] players1, IPlayerData[] players2, int serial, Instance instance) {
		super(id, service, strategy, force, players1, players2, serial);
		this.instance = instance;
		this.model = (BattleForResourcesInstanceModel) instance.getDefinition().getModel();
		this.loseRate = instance.getDefinition().getGoodsLoseRate();
		this.resourcesID = instance.getDefinition().getCompetingGoodsID();
	}

	protected void ok (int playerId) {

	}

	protected void refuse (int playerId, byte code, String cause) {

	}

	public void start () {
		model.addBattle(this);
		lastTime = System.currentTimeMillis();
		status = STATUS.wait_start;
		sendPkStart();
		lastTime = System.currentTimeMillis();
		status = STATUS.wait_fight;
	}

	public void end () {
		checkDie();
		if (failure != null) {
			Changed[] changed = new Changed[failure.length];
			Changed[] changed1 = new Changed[winner.length];
			for(int i=0;i<failure.length;i++){
                changed[i] = new Changed();
                service.getBufService().checkBattleBuff(failure[i],changed[i]);
            }
			for (int i = 0; i < winner.length; i++) {
				changed1[i] = new Changed();
				service.getBufService().checkBattleBuff(winner[i], changed1[i]);
				service.getConnectService().sendGetItem(changed1[i], winner[i].getId(), (byte)3);
			}
			missResources(changed1, changed);
			for (int i = 0; i < winner.length; i++) {
				winner[i].setDeadTime(0);
				service.getConnectService().sendGetItem(changed1[i], winner[i].getId(), (byte)3);
			}
			InstanceDefinition idf = instance.getDefinition();
			for (int i = 0; i < failure.length; i++) {
				synchronized (failure[i]) {
					int campTeam = Utils.NO_CAMP;
					CampBattlefieldInstance cbi = model.getInstance(failure[i], instance.getId());
					if(cbi != null){
						CampBattlefieldPlayer cbp = cbi.getBattlefieldPlayer(failure[i].getId());
						if(cbp != null){
							campTeam = cbp.getCampTeam();
						}
					}
					service.getConnectService().sendGetItem(changed[i], failure[i].getId(), (byte)3);
					if(campTeam != Utils.NO_CAMP){
						if(campTeam == Utils.CAMP_BRIGHT){
							service.getCampBattlefieldService().sendGotoMap(failure[i].getId(), idf.getBrightEntrance(),
									idf.getBrightEntranceX(), idf.getBrightEntranceY());
						}else if(campTeam == Utils.CAMP_DARK){
							service.getCampBattlefieldService().sendGotoMap(failure[i].getId(), idf.getDarkEntrance(),
									idf.getDarkEntranceX(), idf.getDarkEntranceY());
						}
					}else{
						if (failure[i].getCamp() == Utils.CAMP_BRIGHT) {
							service.getCampBattlefieldService().sendGotoMap(failure[i].getId(), idf.getBrightEntrance(),
									idf.getBrightEntranceX(), idf.getBrightEntranceY());
						} else if (failure[i].getCamp() == Utils.CAMP_DARK) {
							service.getCampBattlefieldService().sendGotoMap(failure[i].getId(), idf.getDarkEntrance(),
									idf.getDarkEntranceX(), idf.getDarkEntranceY());
						}
					}
				}
			}
			model.battleEnded(this);
			service.removeBattle(this);
		}else{
			service.removeBattle(this);
		}
	}

	public void abort () {
		for (int i = 0; i < side1.length; i++) {
            sendAbort(side1[i], pet1[i], serial);
        }
        for (int i = 0; i < side2.length; i++) {
            sendAbort(side2[i], pet2[i], serial);
        }
        status = STATUS.end;
        service.removeBattle(this);
	}

	public IPlayerData[] getPlayers () {
		IPlayerData[] ret = new IPlayerData[side1.length + side2.length];
        int i = 0;
        for (; i < side1.length; i ++) {
            ret[i] = side1[i].player;
        }
        for (int j = 0; j<side2.length; i++, j++) {
            ret[i] = side2[j].player;
        }
        return ret;
	}

	private void checkDie () {
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
	
	private void missResources (Changed[] changed1, Changed[] changed) {
        Random rnd = new Random();
        boolean mark = false;
    	for (int i = 0; i < failure.length; i ++) {
    		if (!Utils.hit(loseRate, 100)) {
				continue;
			}
    		int count = failure[i].getItemCount(resourcesID);
    		if (count > 0) {
    			int rate = getAddIndex(rnd, 0, winner.length - 1, winner, failure[i], mark);
    			if (rate >= 0) {
    				for (int j = 0; j < winner.length; j ++) {
    					if (rate == j) {
    						IItem item = Items.getTemplate(resourcesID).newInstance();
    						failure[i].completeRemoveItem(item, count, changed[i]);
    						IItemTemplate template = Items.getTemplate(resourcesID);
    						winner[j].addItem(template, count, changed1[j], winner[j].getClientDataVersion());
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
     * 返回可以获得资源的winner下标
     * @param rnd
     * @param min
     * @param max
     * @param winner
     * @param failure
     * @return
     */
    private int getAddIndex (Random rnd, int min, int max, IPlayerData[] winner, IPlayerData failure, boolean mark) {
    	for (int i = 0; i < winner.length; i ++) {
    		if (winner[i].getCamp() > Utils.NO_CAMP) {
    			mark = true;
    			break;
    		}
    	}
    	if (mark) {
    		IntList index = new ArrayIntList(1);
    		while (index.size() < 1) {
    			int v = Utils.getCount(rnd, min, max);
    			if (!index.contains(v) && winner[v].getCamp() != Utils.NO_CAMP) {
    				index.add(v);
    			}
    		}
    		return index.get(0);
    	} else {
    		return -1;
    	}
    }
    
    public synchronized void cancel(){
        if (status != STATUS.end) {
        	for (int i = 0; i < side1.length; i++) {
                sendAbort(side1[i], pet1[i], serial);
            }
            for(int i = 0; i < side2.length; i++) {
                sendAbort(side2[i], pet2[i], serial);
            }
            status = STATUS.end;
            model.battleEnded(this);
            service.removeBattle(this);
        }
    }
}
