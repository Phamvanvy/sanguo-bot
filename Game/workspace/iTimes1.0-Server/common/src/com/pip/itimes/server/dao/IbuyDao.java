package com.pip.itimes.server.dao;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import com.pip.itimes.server.bean.Ibuy;
import com.pip.itimes.server.bean.Player;


public class IbuyDao extends BaseDao {
    public IbuyDao() {
        super();
    }

    public void addIbuy(Ibuy ibuy) throws DataAccessException {
        makePersistent(ibuy);
    }

    public Ibuy[] getItmes(int playerId,int accountId) throws DataAccessException{
        List l = getLimitedList("from Ibuy i where i.playerid="+playerId+" and i.accountid="
        			+accountId+" and i.type=1 order by buytime desc" ,0,10);
        Ibuy[] ret = new Ibuy[l.size()];
        l.toArray(ret);
        return ret;
    }
    public Ibuy[] getItmesbytime(int playerId,int accountId,Date Btime,Date Etime) throws DataAccessException{
        List l = getLimitedList("from Ibuy i where i.playerid="+playerId+" and i.accountid="+accountId+
        					" and i.buytime>='"+Btime+"' and i.buytime<='"+Etime+"' and i.type=1 order by buytime desc",0,10);
        Ibuy[] ret = new Ibuy[l.size()];
        l.toArray(ret);
        return ret;
    }
    public int getmonthibuy(int playerId,int accountId) throws DataAccessException{
    	Date date1 = new Date();
    	Date new_date = new Date(date1.getYear(),date1.getMonth(),1);
    	String str = DateFormat.getDateTimeInstance().format(new_date);
    	Long ret = (Long) uniqueResult(
                "select sum(imoney) from Ibuy t where t.playerid = " + playerId + 
                	" and t.giftflag = 0 and t.accountid = " + accountId + 
                	" and t.buytime > '" + str + "'");
        if(ret!=null)
            return ret.intValue();
        return 0;
    }
    public List getPlayerTop10(int limit,String begin,String end) throws DataAccessException{
        String hql = "select DISTINCT playerid ,sum(imoney) from Ibuy t where giftflag = 0 and buytime > '";
        hql = hql + begin;
        hql = hql + "' and buytime < '" + end + "' group by playerid order by 2 desc";
        List l = getLimitedList(hql, 0, limit);
        
        return l;
    }
}
