package com.pip.itimes.server.world;

import com.pip.itimes.server.bean.Petmanager;

import com.pip.itimes.server.dao.*;

/**
 * @author sky
 * @version 1.0
 */
public class PetmanagerService {

    private PetmanagerDao dao;

    public static final int ibuytype_shop = 1;
    public static final int ibuytype_face = 2;
    public static final int ibuytype_house = 3;
    public static final int ibuytype_housestyle = 4;
    public static final int ibuytype_part = 5;
    public static final int ibuytype_waiter = 6;
    
    
    public PetmanagerService(PetmanagerDao dao) {
        this.dao = dao;
    }

    public void addPet (Petmanager petmanager) throws BuyException {
        try {
            dao.makePersistent(petmanager);
        } catch (DataAccessException ex) {
            throw new BuyException(ex + "宠物修炼记录错误");
        }
    }

    public void leavepet (Petmanager petmanager) throws BuyException {
        try {
            dao.deletePet(petmanager.getPetId(), petmanager.getPlayerId());
        } catch (DataAccessException ex) {
            throw new BuyException(ex + "宠物修炼领取宠物记录错误");
        }
    }
    
    public Petmanager[] getPets (int playerId) {
        try {
            return (Petmanager[]) dao.getPets(playerId);
        } catch (DataAccessException ex) {
            return null;
        }
    }
    public Petmanager getPet (int Id) {
        try {
            return (Petmanager) dao.getPet(Id);
        } catch (DataAccessException ex) {
            return null;
        }
    }
    public boolean getHungryPet (int playerId) {
        try {
            return dao.getHungryPets(playerId);
        } catch (DataAccessException ex) {
            return false;
        }
    }
    
    public boolean checkPet (int petId, int playerId) {
    	try {
            return dao.checkPet(petId, playerId);
        } catch (DataAccessException ex) {
            return false;
        }
    }
    
    public void deletePet (int petId, int playerId) throws BuyException {
        try {
            dao.deletePet(petId, playerId);
        } catch (DataAccessException ex) {
            throw new BuyException(ex + "删除宠物数据错误");
        }
    }
    
    // 比较当前时间与玩家选择的宠物修炼时间，如果够则提示，如果超过5小时则提示疲劳
    public Petmanager[] getPetmanagerInfo (int playerId) {
    	Petmanager[] petmanager = getPets(playerId);
    	Petmanager[] ret = new Petmanager[petmanager.length];
    	for (int i = 0; i < petmanager.length; i ++) {
    		ret[i] = petmanager[i];
    	}
    	if (petmanager != null) {
    		return ret;
    	} else {
    		return null;
    	}
    }
    
    public boolean updateTime (int id, long time) {
    	return dao.updatePracticeTime(id, time);
    }
    
    public Petmanager[] getPetData (int petId) {
    	try {
    		return (Petmanager[]) dao.getPetData(petId);
		} catch (DataAccessException e) {
			return null;
		}
    }
    
    public void deletePetmanager (int id) {
    	try {
			dao.deletePetmanager(id);
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
