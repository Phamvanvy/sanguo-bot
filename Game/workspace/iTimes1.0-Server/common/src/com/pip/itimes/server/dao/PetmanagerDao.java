package com.pip.itimes.server.dao;

import java.util.List;
import com.pip.itimes.server.bean.Petmanager;


public class PetmanagerDao extends BaseDao {
    public PetmanagerDao() {
        super();
    }

    public void addPet (Petmanager petmanager) throws DataAccessException {
        makePersistent(petmanager);
    }

    public Petmanager[] getPets (int playerId) throws DataAccessException {
        List l = getLimitedList("from Petmanager p where p.playerId="+playerId+" order by eattime" ,0,10);
        Petmanager[] ret = new Petmanager[l.size()];
        l.toArray(ret);
        return ret;
    }
    
    public Petmanager getPet (int Id) throws DataAccessException {
        return (Petmanager) uniqueResult("from Petmanager p where p.petId=" + Id);
    }
    
    public void deletePet (int Id, int playerid) throws DataAccessException {
    	query("delete Petmanager p where p.petId=" + Id + " and p.playerId=" + playerid);
    }
    
    public boolean getHungryPets (int playerId) throws DataAccessException {
    	String hql = "from Petmanager p where p.playerId=" + playerId + " and p.stone < 1";
        
        int count = getCount(hql);
        if (count > 0) {
        	return true;
        } else {
        	return false;
        }
    }
    
    public List getMaxTime (int playerId) throws DataAccessException {
        String hql = "select practiceTime from Petmanager p where p.playerId=" + playerId + "order by 1 desc";
        List l = getLimitedList(hql, 0, 1);
        return l;
    }
    
    public boolean checkPet (int petId, int playerId) throws DataAccessException {
    	String hql = "from Petmanager p where p.petId=" + petId + " and p.playerId=" + playerId;
    	int count = getCount(hql);
    	if (count > 0) {
        	return true;
        } else {
        	return false;
        }
    }
    
    public boolean updatePracticeTime (int id, long practiceTime) {
    	try {
			query("update Petmanager p set p.practiceTime=" + practiceTime + " where p.id=" + id);
			return true;
		} catch (DataAccessException e) {
			 return false;
		}
    }
    
    public Petmanager[] getPetData (int petId) throws DataAccessException {
    	String sql = "from Petmanager p where p.petId=" + petId + " order by p.practiceTime desc";
    	List l = getList(sql);
        Petmanager[] ret = new Petmanager[l.size()];
        l.toArray(ret);
        return ret;
    }
    
    public void deletePetmanager (int id) throws DataAccessException {
    	query("delete Petmanager p where p.id=" + id);
    }
}
