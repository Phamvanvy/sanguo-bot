package com.pip.itimes.server.dao;

import java.util.List;
import com.pip.itimes.server.bean.House;
import com.pip.itimes.server.bean.Tong;

public class HouseDao extends BaseDao{
    public HouseDao() {
    }
    //mengjie add
    
    public House getOneHouses(int playerid) throws DataAccessException{
    	
        return (House)uniqueResult("from House t where t.playerId = " + playerid);
    }

    public List getAllHouses() throws DataAccessException{
        List l = getList("from House");
        return l;
    }

    public void saveHouse(House house) throws DataAccessException{
        makePersistent(house);
    }
    
    public House[] getHouseVisitedOrder(int limit) throws DataAccessException{
        String hql = "from House t order by t.visitedTimes desc, t.id";
        List l = getLimitedList(hql, 0, limit);
        House[] ret = new House[l.size()];
        l.toArray(ret);
        
        return ret;
    }
    
    public int getVisitedOrder(House house) throws DataAccessException{
        String hql = "from House t where t.visitedTimes > " + house.getVisitedTimes() + " or (t.visitedTimes = " + house.getVisitedTimes() + " and t.id > " + house.getId() + ")";
        
        return getCount(hql);
    }
    
    public House[] getHouseUsediMoneyOrder(int limit) throws DataAccessException{
        String hql = "from House t order by t.usediMoney desc, t.id";
        List l = getLimitedList(hql, 0, limit);
        House[] ret = new House[l.size()];
        l.toArray(ret);
        
        return ret;
    }
    
    public int getUsediMoneyOrder(House house) throws DataAccessException{
        String hql = "from House t where t.usediMoney > " + house.getUsediMoney() + " or (t.usediMoney = " + house.getUsediMoney() + " and t.id > " + house.getId() + ")";
        
        return getCount(hql);
    }
    
    public House[] getHouseLeaveMessageOrder(int limit) throws DataAccessException{
        String hql = "from House t order by t.leaveMessageTimes desc, t.id";
        List l = getLimitedList(hql, 0, limit);
        House[] ret = new House[l.size()];
        l.toArray(ret);
        
        return ret;
    }
    
    public int getLeaveMessageOrder(House house) throws DataAccessException{
        String hql = "from House t where t.leaveMessageTimes > " + house.getLeaveMessageTimes() + " or (t.leaveMessageTimes = " + house.getLeaveMessageTimes() + " and t.id > " + house.getId() + ")";
        
        return getCount(hql);
    }
}
