package com.pip.itimes.server.world.game;

import java.util.*;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.stage.*;
import com.pip.itimes.server.world.*;
import com.pip.itimes.server.world.boss.BossDefineLoader;
import com.pip.itimes.server.world.boss.BossService;
//import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import org.apache.commons.collections.primitives.IntList;
import org.apache.commons.collections.primitives.ArrayIntList;
import com.pip.itimes.server.util.Utils;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class FallService2 {


    private static final Logger log = Logger.getLogger(FallService2.class);

    private int id = 1;

    private ConcurrentHashMap id2falls = new ConcurrentHashMap();

    private Random rnd = new Random();

    private ChatService chatService;
    private ConnectService connectService;
    private RefreshService refreshService = new RefreshService();
    private StageService stageService;
    private PlayerService playerService;
    private BossService bossService;
    
    public BossService getBossService() {
		return bossService;
	}

	public void setBossService(BossService bossService) {
		this.bossService = bossService;
	}

	public FallService2() {
        refreshService.start();
    }

    public void setChatService(ChatService chatService) {
        this.chatService = chatService;
    }

    public void setConnectService(ConnectService connectService) {
        this.connectService = connectService;
    }

    public void setStageService(StageService stageService){
        this.stageService = stageService;
    }

    public void setPlayerService(PlayerService playerService){
        this.playerService = playerService;
    }

    public void addFalls(IPlayerData[] allplayers, IItem[] equs) {
        synchronized (this) {
            if(equs.length>3){
                return;
            }
            
            ArrayList<WorldPlayer> lstPlayer = new ArrayList<WorldPlayer>();
            for(int i=0; i<allplayers.length; i++){
            	if(allplayers[i] instanceof WorldPlayer){
            		lstPlayer.add((WorldPlayer)allplayers[i]);
            	}
            }
            WorldPlayer[] players = new WorldPlayer[lstPlayer.size()];
            lstPlayer.toArray(players);
            
            Fall[] falls = new Fall[equs.length];
            for(int i=0;i<equs.length;i++){
                falls[i] = new Fall(++id,equs[i],players);
                id2falls.put(new Integer(falls[i].getId()),falls[i]);
                refreshService.queue(falls[i],120*(i+1));
                StringBuffer buffer = new StringBuffer(300);
                for (int j = 0; j < players.length; j++) {
                    buffer.append("ID[");
                    buffer.append(players[j].getId());
                    buffer.append("]");
                }
                buffer.append("AddFall[");
                buffer.append(falls[i].getId());
                buffer.append("]Item[");
                buffer.append(Utils.getHexdump(equs[i].toDbBytes()));
                buffer.append("]");
                log.info(buffer.toString());
            }
//            Fall fall = new Fall(++id, equ, players);
//            id2falls.put(new Integer(fall.getId()), fall);
//            refreshService.queue(fall, 120);
            byte[] bytes = null;
            /* short id = 31003;
            if(falls.length==1){
                bytes = stageService.getTaskBytes((short) 31003,
                                                  new String[] {ItemUtils.
                                                  getEquipmentString(falls[0].
                        getEquipment()) +
                                                  "\n1.需求\n2.放弃",
                                                  "roll " + falls[0].getId(),
                                                  "roll_cancel " + falls[0].getId()});
            }*/
            short id = 31050;
            if(falls.length==1){
                bytes = stageService.getTaskBytes((short) 31050,
                                                  new String[] {ItemUtils.
                		getItemString(falls[0].
                        getEquipment()) +
                                                  "\n1.需求\n2.放弃",
                                                  "roll " + falls[0].getId(),
                                                  "roll_cancel " + falls[0].getId()});
            }else if(falls.length==2){
                bytes = stageService.getTaskBytes((short) 31013,
                                                  new String[] {ItemUtils.
                		getItemString(falls[0].getEquipment()) +
                                                  "\n1.需求\n2.放弃",
                                                  "roll " + falls[0].getId(),
                                                  "roll_cancel " + falls[0].getId(),ItemUtils.
                                                  getItemString(falls[1].getEquipment()) +
                                                  "\n1.需求\n2.放弃",
                                                  "roll " + falls[1].getId(),
                                                  "roll_cancel " + falls[1].getId()});
                id = 31013;
            } else if (falls.length == 3) {
                bytes = stageService.getTaskBytes((short) 31014,
                                                  new String[] {ItemUtils.getItemString(falls[0].getEquipment()) +
                                                  "\n1.需求\n2.放弃",
                                                  "roll " + falls[0].getId(),
                                                  "roll_cancel " +
                                                  falls[0].getId(),
                                                  ItemUtils.getItemString(falls[1].getEquipment()) +
                                                  "\n1.需求\n2.放弃",
                                                  "roll " + falls[1].getId(),
                                                  "roll_cancel " +
                                                  falls[1].getId(), ItemUtils.
                                                  getItemString(falls[2].getEquipment()) +
                                                  "\n1.需求\n2.放弃",
                                                  "roll " + falls[2].getId(),
                                                  "roll_cancel " +
                                                  falls[2].getId()
                });
                id = 31014;
            }
            UWAPSegment seg = new UWAPSegment(ClientConstants.
                                              GET_FILE_OK);
            seg.writeShort(id);
            seg.writeShort((short) 2);
            seg.write(bytes);
            for (int i = 0; i < players.length; i++) {
                connectService.writeTo(seg, players[i].getId());
            }
        }
    }
    
    public void addRoll(WorldPlayer player, int id) {
    	Fall fall = (Fall) id2falls.get(new Integer(id));
        if (fall != null) {
            try {
                int roll = rnd.nextInt(100);
                RollPoint rollPoint = fall.addRoll(player, roll, RollPoint.ROLL);
                int[] players = fall.getPlayersId();
                if (rollPoint != null) {
                    Changed changed = new Changed();
                    WorldPlayer p = playerService.getWorldPlayer(rollPoint.
                            playerId);
                    if (p != null) {
                        IItem item = p.completeAddItem(fall.getEquipment(), 1,
                                changed, p.getClientDataVersion());
                        if (item == null) {
                        	if(BossDefineLoader.bossEquMap.containsKey(fall.getEquipment().getItemId())){
                        		bossService.resetWorldBossRefresh(fall.getEquipment().getItemId());
                        	}
                        	connectService.sendMessage(p.getId(), "你由于背包满无法获得" + fall.getEquipment().getName());
                            log.info("ID[" + p.getId() +
                                     "]GridOverLostRollItem[" +
                                     Utils.
                                     getHexdump(fall.getEquipment().toDbBytes()) +
                                     "]");
                        } else {
                        	//检测是是否是boss的
                        	if(BossDefineLoader.bossEquMap.containsKey(item.getItemId())){
                        		if(Server.player_Delay.containsKey(p.getId())){
                        			WorldBossEquipInfo info = Server.player_Delay.get(p.getId());
                        			Map<IItem, Integer> equMap = info.getEquDiamondTimeMap();
                        			equMap.put(item,  p.equDiamondTime);
                        			info.setOnline(true);
                        		}else{
                        			WorldBossEquipInfo info = new WorldBossEquipInfo();
                        			Map<IItem, Integer> equMap = info.getEquDiamondTimeMap();
                        			equMap.put(item, p.equDiamondTime);
                        			info.setOnline(true);
                        			Server.player_Delay.put(p.getId(), info);
                        			
                        		}
	                            chatService.sendWorldMessage(-1, "系统", "玩家"+ p.getPlayerName()+ "抢夺到了"+ item.getName() + ", 他能坚持到最后、赢得宝石和荣耀吗？让我们拭目以待！");
                        	}
                        	connectService.sendGetItem(changed, p.getId(),
                                    (byte) 22);
                        	
                        	 Grid[] gridchange = changed.getChangedItems();
                             for (int k = 0; k < gridchange.length; k++) {
                             	int item_id = 0;
                                 item_id = gridchange[k].item.getItemId();
                                 String item_msg = Items.getMessage(item_id,2,p.getPlayerName(),gridchange[k].item.getName(),"怪物那里");
                                 if (item_msg != null){
                                	 chatService.sendWorldMessage(-1, "系统", item_msg);
                                 }
                             }
                        	
                        	
                            log.info("ID[" + p.getId() + "]GetRollItem[" +
                                     Utils.getHexdump(fall.getEquipment().
                                    toDbBytes()) + "]");
                        }
                    } else {
                        log.info("ID[" + rollPoint.playerId +
                                 "]Lost Roll Item[" +
                                 Utils.getHexdump(fall.getEquipment().toDbBytes()) +
                                 "]");
                    }
                    fall.release();
                    String s = fall.getWinnerString();
                    for (int i = 0; i < players.length; i++) {
                      /*  chatService.sendPrivateMessage( -1, "系统",
                                players[i],
                                s);*/
                    	if(p !=null){
                    		if(players[i] != p.getId()){
                    			connectService.sendMessage(players[i], s);
                    		}
                    	}else{
                    		chatService.sendPrivateMessage( -1, "系统",
                                    players[i],
                                    s);
                    	}
                    }

                }
            } catch (FallException ex) {
                log.error(ex, ex);
            }
        }
    }
    
    public void cancelRoll(WorldPlayer player, int id) {
        Fall fall = (Fall) id2falls.get(new Integer(id));
        if (fall != null) {
            try {
                int roll = rnd.nextInt(100);
                RollPoint rollPoint = fall.addRoll(player, roll,
                        RollPoint.CANCEL);
                int[] players = fall.getPlayersId();
                if (rollPoint != null) {
                    Changed changed = new Changed();
                    WorldPlayer p = playerService.getWorldPlayer(rollPoint.
                            playerId);
                    if (p != null) {
                        IItem item = p.completeAddItem(fall.getEquipment(), 1,
                                changed, p.getClientDataVersion());
                        if (item == null) {
                        	if(BossDefineLoader.bossEquMap.containsKey(fall.getEquipment().getItemId())){
                        		bossService.resetWorldBossRefresh(fall.getEquipment().getItemId());
                        	}
                        	connectService.sendMessage(p.getId(), "你由于背包满无法获得" + fall.getEquipment().getName());
                            log.info("ID[" + p.getId() +
                                     "]GridOverLostRollItem[" +
                                     Utils.
                                     getHexdump(fall.getEquipment().toDbBytes()) +
                                     "]");
                        } else {
                        	if(BossDefineLoader.bossEquMap.containsKey(item.getItemId())){
                        		if(Server.player_Delay.containsKey(p.getId())){
                        			WorldBossEquipInfo info = Server.player_Delay.get(p.getId());
                        			Map<IItem, Integer> equMap = info.getEquDiamondTimeMap();
                        			equMap.put(item,  p.equDiamondTime);
                        			info.setOnline(true);
                        		}else{
                        			WorldBossEquipInfo info = new WorldBossEquipInfo();
                        			Map<IItem, Integer> equMap = info.getEquDiamondTimeMap();
                        			equMap.put(item, p.equDiamondTime);
                        			info.setOnline(true);
                        			Server.player_Delay.put(p.getId(), info);
                        			
                        		}
	                        	chatService.sendWorldMessage(-1, "系统", "玩家"+ p.getPlayerName()+ "抢夺到了"+ item.getName() + ",他能坚持到最后、赢得宝石和荣耀吗？让我们拭目以待！");
                        	}
                            connectService.sendGetItem(changed, p.getId(),
                                    (byte) 22);
                            
                            Grid[] gridchange = changed.getChangedItems();
                            for (int k = 0; k < gridchange.length; k++) {
                            	int item_id = 0;
                                item_id = gridchange[k].item.getItemId();
                                String item_msg = Items.getMessage(item_id,2,p.getPlayerName(),gridchange[k].item.getName(),"怪物那里");
                                if (item_msg != null){
                               	 chatService.sendWorldMessage(-1, "系统", item_msg);
                                }
                            }
                            
                            log.info("ID[" + p.getId() + "]GetRollItem[" +
                                     Utils.getHexdump(fall.getEquipment().
                                    toDbBytes()) + "]");
                        }
                    } else {
                        log.info("ID[" + rollPoint.playerId +
                                 "]Lost Roll Item[" +
                                 Utils.getHexdump(fall.getEquipment().toDbBytes()) +
                                 "]");
                    }
                    fall.release();
                    String s = fall.getWinnerString();
                   /* for (int i = 0; i < players.length; i++) {
                        chatService.sendPrivateMessage( -1, "系统",
                                players[i],
                                s);
                    }*/
                    for (int i = 0; i < players.length; i++) {
                        /*  chatService.sendPrivateMessage( -1, "系统",
                                  players[i],
                                  s);*/
                      	if(p !=null){
                      		if(players[i] != p.getId()){
                      			connectService.sendMessage(players[i], s);
                      		}
                      	}else{
                      		chatService.sendPrivateMessage( -1, "系统",
                                      players[i],
                                      s);
                      	}
                     }

                }
            } catch (FallException ex) {
                log.error(ex, ex);
            }
        }
    }

    public void notifyTimeOut(Fall fall) {
        RollPoint rollPoint = fall.getWinner();
        int[] players = fall.getPlayersId();
        if (rollPoint != null) {
            Changed changed = new Changed();
            WorldPlayer p = playerService.getWorldPlayer(rollPoint.playerId);
            if(p!=null){
                IItem item = p.completeAddItem(fall.getEquipment(), 1, changed, p.getClientDataVersion());
                if(item==null){
                	if(BossDefineLoader.bossEquMap.containsKey(fall.getEquipment().getItemId())){
                		bossService.resetWorldBossRefresh(fall.getEquipment().getItemId());
                	}
                	connectService.sendMessage(p.getId(), "你由于背包满无法获得" + fall.getEquipment().getName());
                    log.info("ID["+p.getId()+"]GridOverLostRollItem["+Utils.getHexdump(fall.getEquipment().toDbBytes())+"]");
                }else{
                	if(BossDefineLoader.bossEquMap.containsKey(item.getItemId())){
                		if(Server.player_Delay.containsKey(p.getId())){
                			WorldBossEquipInfo info = Server.player_Delay.get(p.getId());
                			Map<IItem, Integer> equMap = info.getEquDiamondTimeMap();
                			equMap.put(item,  p.equDiamondTime);
                			info.setOnline(true);
                		}else{
                			WorldBossEquipInfo info = new WorldBossEquipInfo();
                			Map<IItem, Integer> equMap = info.getEquDiamondTimeMap();
                			equMap.put(item, p.equDiamondTime);
                			info.setOnline(true);
                			Server.player_Delay.put(p.getId(), info);
                			
                		}
	                	chatService.sendWorldMessage(-1, "系统", "玩家"+ p.getPlayerName()+ "抢夺到了"+ item.getName() + ", 他能坚持到最后、赢得宝石和荣耀吗？让我们拭目以待！");
                	}
                    connectService.sendGetItem(changed, p.getId(), (byte) 22);
                    log.info("ID["+p.getId()+"]GetRollItem["+Utils.getHexdump(fall.getEquipment().toDbBytes())+"]");
                }

            }else{
                log.info("ID[" + rollPoint.playerId +
                         "]Lost Roll Item[" +
                         Utils.getHexdump(fall.getEquipment().toDbBytes()) +
                                 "]");
            }
            fall.release();
            String s = fall.getWinnerString();
           /* for (int i = 0; i < players.length; i++) {
                String s = fall.getWinnerString();
                chatService.sendPrivateMessage( -1, "系统", players[i],
                                               s);
            }*/
            for (int i = 0; i < players.length; i++) {
                /*  chatService.sendPrivateMessage( -1, "系统",
                          players[i],
                          s);*/
              	if(p !=null){
              		if(players[i] != p.getId()){
              			connectService.sendMessage(players[i], s);
              		}
              	}else{
              		chatService.sendPrivateMessage( -1, "系统",
                              players[i],
                              s);
              	}
             }
        }
    }
    

    public class Fall implements IRefresh {

        private IItem item;
        private IntList players = new ArrayIntList(3);
        private List playersName = new ArrayList(3);
        private int id;
        private Map rolls = new HashMap(3);
        private boolean isOver = false;
        private boolean released = false;
        private RollPoint winner;

        public Fall(int id, IItem item, IPlayerData[] players) {
            this.id = id;
            this.item = item;
            for (int i = 0; i < players.length; i++) {
                this.players.add(players[i].getId());
                this.playersName.add(players[i].getPlayerName());
            }
        }

        public int getId() {
            return id;
        }

        public RollPoint addRoll(WorldPlayer player, int roll,int type) throws
                FallException {
            return addRoll(player.getId(),player.getPlayerName(),roll,type);
        }

        public RollPoint addRoll(int playerId,String playerName,int roll,int type) throws FallException{
            synchronized (this) {
                if (!isOver) {
                    if (players.contains(playerId)) {
                        RollPoint r = new RollPoint();
                        r.playerId = playerId;
                        r.playerName = playerName;
                        r.point = roll;
                        r.type = type;
                        rolls.put(new Integer(playerId), r);
                    } else {
                        log.info("ID["+playerId+"]Roll Error");
                    }
                    if (rolls.size() == players.size()) {
                        isOver = true;
                        return getWinner();
                    }
                }
                return null;
            }
        }

        public IItem getEquipment() {
            return item;
        }

        public RollPoint getWinner() {
            if(rolls.size()!=players.size())
                return null;
            else{
                Iterator ite = rolls.values().iterator();
                while(ite.hasNext()){
                    RollPoint point = (RollPoint)ite.next();
                    if(winner==null)
                        winner = point;
                    else{
                        if(winner.type>point.type){
                            winner = point;
                        }
                        else if(winner.type==point.type){
                            if(winner.point<point.point){
                                winner = point;
                            }
                        }
                    }
                }
            }
            return winner;
        }

        public String getWinnerString() {
            StringBuffer buff = new StringBuffer(200);
            if (winner.type == RollPoint.CANCEL) {
                buff.append("所有人都放弃了,");
                buff.append(winner.playerName);
                buff.append("获得了");
                buff.append(item.getName());
                buff.append(".");
                Iterator ite = rolls.values().iterator();
                while (ite.hasNext()) {
                    RollPoint p = (RollPoint) ite.next();
                    buff.append(p.playerName);
                    buff.append("(");
                    buff.append(p.point);
                    buff.append(")");
                }
                return buff.toString();
            } else {
                buff.append(winner.playerName);
                buff.append("获得了");
                buff.append(item.getName());
                buff.append(".");
                Iterator ite = rolls.values().iterator();
                while (ite.hasNext()) {
                    RollPoint p = (RollPoint) ite.next();
                    if (p.type == RollPoint.ROLL){
                        buff.append(p.playerName);
                        buff.append("(");
                        buff.append(p.point);
                        buff.append(")");
                    }
                }
                return buff.toString();
            }
        }

        public int[] getPlayersId() {
            return players.toArray();
        }

        public void release() {
            synchronized(this){
                if(!released){
                    id2falls.remove(new Integer(id));
                }else{
                    log.info("Release Again Error");
                }
            }
        }

        public void refresh() {
            synchronized (this) {
                if (!isOver) {
                    timeout();
                    notifyTimeOut(this);
                    isOver = true;
                }
            }
        }

        public void timeout(){
            for(int i=0;i<players.size();i++){
                if(!rolls.containsKey(new Integer(players.get(i)))){
                    try {
                        RollPoint roll = addRoll(players.get(i),
                                                 (String) playersName.get(i),
                                                 rnd.nextInt(100),
                                                 RollPoint.CANCEL);
                        if(roll!=null)
                            break;
                    } catch (FallException ex) {
                        log.error(ex,ex);
                    }
                }
            }
        }

    }
}

class RollPoint{
    final static int ROLL = 0;
    final static int CANCEL = 1;

    int playerId;
    String playerName;
    int type;
    int point;
}
