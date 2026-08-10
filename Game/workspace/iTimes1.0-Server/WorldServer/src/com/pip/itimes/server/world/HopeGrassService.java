package com.pip.itimes.server.world;

import com.pip.itimes.server.bean.HopeGrass;
import com.pip.itimes.server.dao.HopeGrassDao;
import java.util.Date;
import com.pip.itimes.server.dao.*;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class HopeGrassService {

    private HopeGrassDao dao;

    public HopeGrassService(HopeGrassDao dao) {
        this.dao = dao;
    }

    public HopeGrass createHopeGrass(int playerId,int itemGroupId,short mapId,short x,short y,int validTime,int obsoleteTime,int grassType,int ratio,int grouprnd){
        HopeGrass grass = new HopeGrass();
        grass.setItemGroupId(itemGroupId);
        grass.setPlayerId(playerId);
        grass.setMapId(mapId);
        grass.setX(x);
        grass.setY(y);
        grass.setCreateTime(new Date());
        grass.setValidTime(new Date(System.currentTimeMillis()+validTime*1000));
        grass.setObsoleteTime(new Date(System.currentTimeMillis()+obsoleteTime*1000) );
        grass.setGrassType(grassType);
        grass.setRatio(ratio);
        grass.setGrouprnd(grouprnd);
        try {
            dao.makePersistent(grass);
            return grass;
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public List getHopeGrass(int playerId,short mapId,short x,short y){
        try {
            return dao.getHopeGrass(playerId, mapId, x, y);
        } catch (DataAccessException ex) {
            return new ArrayList(0);
        }
    }

    public List getHopeGrass(short mapId,short x,short y,int grassType){
        try {
            return dao.getHopeGrass(mapId, x, y,grassType);
        } catch (DataAccessException ex) {
            return new ArrayList(0);
        }
    }

    public void deleteHopeGrass(HopeGrass grass){
        try {
            dao.makeTransient(grass);
        } catch (DataAccessException ex) {
            ex.printStackTrace();
        }
    }
}
