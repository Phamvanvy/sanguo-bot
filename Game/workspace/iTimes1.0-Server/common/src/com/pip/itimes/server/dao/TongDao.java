package com.pip.itimes.server.dao;

import com.pip.itimes.server.bean.Player;
import com.pip.itimes.server.bean.Tong;

import java.util.Date;
import java.util.List;
import com.pip.itimes.server.stage.TongUser;
import java.util.ArrayList;

import org.hibernate.Query;

/**
 * @author Jeffery
 * @version 1.0
 */
public class TongDao extends BaseDao{
    public TongDao() {
    }

    public void addTong(Tong tong) throws DataAccessException{
        makePersistent(tong);
    }
    
    public Tong getTongByName(String name) throws DataAccessException {
        return (Tong) uniqueResult("from Tong t where t.tongName='" +
                                     name + "' and t.valid=true");
    }
    
    public TongUser[] getTongMembers(int tongId) {
        String hql = "select p.id,p.playerName,p.level,p.tongDuty,p.tongTitle,p.contribution from Player p where p.tongId=" +
                     tongId + " and valid = 1 order by p.playerName";
        try {
            List l = getList(hql);
            List tms = new ArrayList(l.size());
            for (int i = 0; i < l.size(); i++) {
                Object[] o = (Object[]) l.get(i);
                int id = ((Integer) o[0]).intValue();
                String name = (String) o[1];
                int level = ((Integer) o[2]).intValue();
                int duty = ((Integer) o[3]).intValue();
                String tongTitle = (String) o[4];
                int contribute = ((Integer)o[5]).intValue();
                TongUser user = new TongUser(id, name, level, duty, tongTitle, false,contribute);
                tms.add(user);
            }
            TongUser[] ret = new TongUser[tms.size()];
            tms.toArray(ret);
            return ret;
        } catch (DataAccessException ex) {
            ex.printStackTrace();
            return new TongUser[0];
        }
    }

    public Tong[] getTongOrder() throws DataAccessException{
        String hql = "from Tong t order by t.credit desc,t.id";
        List l = getLimitedList(hql,0,3);
        Tong[] ret = new Tong[l.size()];
        l.toArray(ret);
        return ret;
    }

    public int getOrder(Tong tong) throws DataAccessException{
        String hql = "from Tong t where t.credit>"+tong.getCredit()+" or (t.credit="+tong.getCredit()+" and t.id>"+tong.getId()+")";
        return getCount(hql);
    }

    public Tong[] getTongTopListHotOrder(int limit) throws DataAccessException{
        String hql = "from Tong t order by t.topListHot desc, t.id";
        List l = getLimitedList(hql, 0, limit);
        Tong[] ret = new Tong[l.size()];
        l.toArray(ret);

        return ret;
    }

    public int getTopListHotOrder(Tong tong) throws DataAccessException{
        String hql = "from Tong t where t.topListHot > " + tong.getTopListHot() + " or (t.topListHot = " + tong.getTopListHot() + " and t.id > " + tong.getId() + ")";

        return getCount(hql);
    }

    public Tong[] getTongTopListOnlineOrder(int limit) throws DataAccessException{
        String hql = "from Tong t order by t.topListOnline desc, t.id";
        List l = getLimitedList(hql, 0, limit);
        Tong[] ret = new Tong[l.size()];
        l.toArray(ret);

        return ret;
    }

    public int getTopListOnlineOrder(Tong tong) throws DataAccessException{
        String hql = "from Tong t where t.topListOnline > " + tong.getTopListOnline() + " or (t.topListOnline = " + tong.getTopListOnline() + " and t.id > " + tong.getId() + ")";

        return getCount(hql);
    }
}
