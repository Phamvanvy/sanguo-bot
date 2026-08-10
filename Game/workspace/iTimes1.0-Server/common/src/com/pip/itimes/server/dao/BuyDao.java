package com.pip.itimes.server.dao;

import com.pip.itimes.server.bean.Buy;
import java.util.List;

/**
 * @author Jeffery
 * @version 1.0
 */
public class BuyDao extends BaseDao {
    public BuyDao() {
        super();
    }

    public Buy[] getBuys(int shopId, int begin, int count) throws
            DataAccessException {
        List l = getLimitedList("from Buy b where b.shopId=" + shopId, begin, count);
        Buy[] ret = new Buy[l.size()];
        l.toArray(ret);
        return ret;
    }

    public int getCount(int shopId) throws DataAccessException{
        return getCount("from Buy b where b.shopId="+shopId);
    }

    public Buy[] getBuys(short areaId, byte type, String name, int begin,
                         int count) throws DataAccessException {
        String hql = "from Buy b where b.state=0 and b.areaId="+areaId;
        if(type!=-1){
            hql += " and b.type="+type;
        }
        if(name.length()!=0){
            hql += " and b.name like '%"+name+"%'";
        }
        hql += " order by b.price desc";
        List l = getLimitedList(hql,begin,count);
        Buy[] ret = new Buy[l.size()];
        l.toArray(ret);
        return ret;
    }

    public int getCount(short areaId, byte type, String name) throws
            DataAccessException {
        String hql = "from Buy b where b.state=0 and b.areaId=" + areaId;
        if (type != -1) {
            hql += " and b.type=" + type;
        }
        if (name.length() != 0) {
            hql += " and b.name like '%" + name + "%'";
        }
        return getCount(hql);
    }

    public void setState(int shopId,byte state) throws DataAccessException{
        String hql = "update Buy b set b.state="+state+" where b.shopId="+shopId;
        query(hql);
    }
}
