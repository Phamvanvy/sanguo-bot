package com.pip.itimes.server.world;

import java.util.*;

import com.pip.itimes.server.world.fee.ChargePlan;
import com.pip.itimes.server.world.fee.FeePlan;
import com.pip.itimes.server.world.game.GameMap;
import com.pip.itimes.server.world.game.WorldService;

public class RobotService  implements Runnable {

    private PlayerService playerService;
    private WorldService worldService;
    private PositionService positionService;

    public int[] PLAYERIDS = new int[]{
                    95,
                    96,
                    97,
                    98,
                    99,
                    100,
                    101,
                    102,
                    103,
                    104,
                    105,
                    106,
                    107,
                    108,
                    109,
                    110,
                    111,
                    112,
                    113,
                    114,
                    115,
                    116,
                    117,
                    118,
                    119,
                    120,
                    121,
                    122,
                    123,
                    124,
                    125,
                    126,
                    127,
                    128,
                    129,
                    130,
                    131,
                    132,
                    133,
                    134,
                    135,
                    136,
                    137,
                    138,
                    139,
                    140,
                    141,
                    142,
                    143,
                    144,
                    145,
                    146,
                    147,
                    148,
                    149,
                    150,
                    151,
                    152,
                    153,
                    154,
                    155,
                    156,
                    157,
                    158,
                    159
    };
    
    private int robotCount = 0;

    private Hashtable<Integer,WorldPlayer> players = new Hashtable<Integer,WorldPlayer>();

    public RobotService() {
        new Thread(this).start();
    }

    public void setPlayerService(PlayerService playerService){
        this.playerService = playerService;
    }

    public void setWorldService(WorldService worldService){
        this.worldService = worldService;
    }

    public void setPositionService(PositionService positionService){
        this.positionService = positionService;
    }

    public boolean isRobot(WorldPlayer player){
        WorldPlayer robot = players.get(player.getId());
        
        if(robot != null){
            return true;
        }else{
            return false;
        }
    }
    
    public int loadPlayers(short mapId,short x,short y,short startX,short startY,short endX,short endY,int count){
        int c = 0;
        
        for(int i = robotCount;i < PLAYERIDS.length; i++){
            if(c >= count){
                break;
            }
            
            WorldPlayer player = playerService.getWorldPlayer(PLAYERIDS[i]);
            
            if(player==null){
                loginPlayer(PLAYERIDS[i],mapId,x,y,startX,startY,endX,endY);
                c++;
            }
        }
        
        robotCount += c;
        
        return c;
    }

    public void clearRobot(){
        Enumeration<Integer> emu = players.keys();

        while(emu.hasMoreElements()){
            int playerid = emu.nextElement();
            logoutPlayer(playerid);
        }
        
        robotCount = 0;
        players.clear();
    }

    public void logoutPlayer(int id){
        WorldPlayer player = playerService.getWorldPlayer(id);
        playerService.release(player);
        positionService.unRegistry(player);
        players.remove(player.getId());
    }

    public void loginPlayer(int id, short mapId, short x, short y, short startX, short startY, short endX, short endY) {
        WorldPlayer player = null;

        try {
            player = playerService.loadWorldPlayer(id);
        } catch (Exception ex) {
            return;
        }
        if (player != null) {
            ChargePlan[] chargePlan = ChargePlan.getChargePlans(new String[]{"wap"});
            FeePlan feePlan = FeePlan.getFeePlan("normal");

            player.setChargePlan(chargePlan);
            player.setFeePlan(feePlan);

            player.setState(WorldPlayer.ONLINE);
            player.setMaxLevel(100);
            player.setLastLoginTime(new Date());
            player.setModel("NK-6600");
            player.setAccountName("");
            player.setKey("");
            player.setPhone("");
            player.setModifyPasswordTimes(0);
            player.clearPosition();
            playerService.acquire(player);

            player.setIsFirstEnter(true);
            player.setIsOnce(true);
            player.setNeedRefreshPosition(true);
            player.setMapId(mapId);
            player.setX(x);
            player.setY(y);
            player.startX = startX;
            player.startY = startY;
            player.endX = endX;
            player.endY = endY;
            GameMap map = worldService.getMap(player, player.getMapId(), true);
            if (map != null) {
                map.addPlayer(player);
                if (map.getMapId() != player.getMapId()) {
                    if (player.getJumpMapId() != 0) {
                        player.setMapId(map.getMapId());
                        player.setX(player.getJumpX());
                        player.setY(player.getJumpY());
                    } else {
                        player.setMapId(map.getMapId());
                        player.setX(map.getDefaultX());
                        player.setY(map.getDefaultY());
                    }
                }
            } else {
                InstanceDefinition instance = worldService.
                                              getInstanceDefinition(player.
                        getMapId());
                if (instance == null) {
                    return;
                }

                player.setMapId(instance.getEntrance());
                player.setX(instance.getEntrancePixelX());
                player.setY(instance.getEntrancePixelY());
            }
            players.put(player.getId(),player);
            positionService.registry(player);
        }
    }


    public void position(WorldPlayer player,short x,short y){
        GameMap map = player.getMap();
        player.setX(x);
        player.setY(y);
        positionService.positionChanged(player,map,x,y);
    }

    public void run() {
        Random rand = new Random(System.currentTimeMillis());

        while (true) {
            try {
                Thread.sleep(10 * 1000L);

                Enumeration<Integer> emu = players.keys();

                while(emu.hasMoreElements()){
                    WorldPlayer player = players.get(emu.nextElement());

                    int oldx = player.getX();
                    int oldy = player.getY();
                    
                    if(rand.nextInt(100) < 50){
                        continue;
                    }
                    
                    for(int i = 0; i < 5; i++){
                        int newx = rand.nextInt(100) - 50 + oldx;
                        int newy = rand.nextInt(100) - 50 + oldy;
                        
                        if(newx >= player.startX && newx <= player.endX && newy >= player.startY && newy <= player.endY){
                            position(player, (short)newx, (short)newy);
                            
                            break;
                        }
                    }
                }
            } catch (InterruptedException ex) {
            }
        }
    }
}
