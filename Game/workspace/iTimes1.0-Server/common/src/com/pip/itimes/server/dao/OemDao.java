package com.pip.itimes.server.dao;

import com.pip.itimes.server.bean.Oem;
import java.util.List;

/**
 * @author Jeffery
 * @version 1.0
 */
public class OemDao extends BaseDao{
    public OemDao() {
        super();
    }

    public int getCount(int shopId) throws DataAccessException{
        return getCount("from Oem o where o.shopId="+shopId);
    }

    public Oem[] getOems(int shopId, int begin, int count) throws
            DataAccessException {
        List l = getLimitedList("from Oem o where o.shopId="+shopId,begin,count);
        Oem[] oems = new Oem[l.size()];
        l.toArray(oems);
        return oems;
    }

    public Oem[] getOems(short areaId, byte type, String name, int begin,
                         int count) throws DataAccessException {
        String hql = "from Oem o where o.state=0 and o.areaId="+areaId;
        if(type!=-1){
            hql += " and o.type="+type;
        }
        if(name.length()!=0){
            hql += " and o.name like '%"+name+"%'";
        }
        hql += " order by o.pay desc";
        List l = getLimitedList(hql,begin,count);
        Oem[] oems = new Oem[l.size()];
        l.toArray(oems);
        return oems;
    }

    public int getCount(short areaId, byte type, String name) throws
            DataAccessException {
        String hql = "from Oem o where o.state=0 and o.areaId=" + areaId;
        if (type != -1) {
            hql += " and o.type=" + type;
        }
        if (name.length() != 0) {
            hql += " and o.name like '%" + name + "%'";
        }
        return getCount(hql);
    }

    public void setState(int shopId,byte state) throws DataAccessException{
        String hql = "update Oem o set o.state="+state+" where o.shopId="+shopId;
        query(hql);
    }

}
