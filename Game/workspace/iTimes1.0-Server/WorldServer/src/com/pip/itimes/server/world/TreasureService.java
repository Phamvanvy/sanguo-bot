package com.pip.itimes.server.world;

import com.pip.itimes.server.bean.Blog;
import com.pip.itimes.server.bean.Treasure;
import com.pip.itimes.server.dao.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.pip.itimes.server.stage.Buf;
import com.pip.itimes.server.util.Utils;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class TreasureService {

    private TreasureDao dao;
    private Random rnd = new Random();

    public TreasureService(TreasureDao dao) {
        this.dao = dao;
    }

    public Treasure getTreasure(int playerId){
        try {
        	List list= dao.getTreasure(playerId);
        	Treasure treasure = null;
        	if(list !=null && list.size() >0){
        		treasure = (Treasure) list.get(0);
        	}
            return treasure;
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public Treasure getTreasure_bykey(int playerId,int shovelId){
        try {
        	List list= dao.getTreasure(playerId,shovelId);
        	Treasure treasure = null;
        	if(list !=null && list.size() >0){
        		treasure = (Treasure) list.get(0);
        	}
            return treasure;
        } catch (DataAccessException ex) {
            return null;
        }
    }
    public Treasure getTreasure(int playerId,short mapId){
        try {
        	List list= dao.getTreasure(playerId, mapId);
        	Treasure treasure = null;
        	if(list !=null && list.size() >0){
        		treasure = (Treasure) list.get(0);
        	}
            return treasure;
        } catch (DataAccessException ex) {
            return null;
        }
    }
    public Treasure[] getTreasures(int playerId){
        try {
        	List list= dao.getTreasure(playerId);
        	Treasure treasure = null;
        	Treasure[] treasures = new Treasure[list.size()];
        	for (int i = 0; i < list.size() ; i++) {
        		treasures[i] = (Treasure) list.get(i);;
            }
            return treasures;
        } catch (DataAccessException ex) {
            return null;
        }
    }
    private Treasure createTreasure0(int playerId, short mapId, short minX, short maxX, short minY, short maxY, int itemGroupId, int shovelId){
        Treasure treasure = new Treasure();
        treasure.setPlayerId(playerId);
        treasure.setMapId(mapId);

        treasure.setX((short)Utils.getCount(rnd,minX,maxX));
        treasure.setY((short)Utils.getCount(rnd,minY,maxY));
        treasure.setItemGroupId(itemGroupId);
        treasure.setCreateTime(new Date());
        treasure.setShovelId(shovelId);
        try {
            dao.makePersistent(treasure);
            return treasure;
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public void deleteTreasure(Treasure treasure){
        try {
            dao.makeTransient(treasure);
        } catch (DataAccessException ex) {
            ex.printStackTrace();
        }
    }

    public Treasure createTreasure(int playerId, short mapId, short minX, short maxX, short minY, short maxY, int itemGroupId, int shovelId) throws TreasureException{
        if (shovelId == -1){
        	if (getTreasure(playerId) != null) {
            	Treasure[] trea = getTreasures(playerId);
            	for (int i = 0; i < trea.length ; i++) {
            		deleteTreasure(trea[i]);
                }
            }
        }else{
        	if (getTreasure_bykey(playerId,shovelId) != null) {
            	Treasure trea = getTreasure_bykey(playerId,shovelId);
            	deleteTreasure(trea);
            }
        }
    	
        return createTreasure0(playerId, mapId, minX, maxX, minY, maxY, itemGroupId, shovelId);
    }
}
