package com.pip.itimes.server.dao;


import java.util.Calendar;
import java.util.Date;

import org.hibernate.Query;

import com.pip.itimes.server.bean.SmsFee;


public class SmsFeeDao extends BaseDao{
    public SmsFeeDao(){
    }

    public SmsFee getSmsFee(int id){
        try{
            return (SmsFee)getObject(SmsFee.class, new Integer(id));
        }catch(DataAccessException ex){
            ex.printStackTrace();
            return null;
        }
    }

    public int getMonthAmountByPhone(String phone){
        try{
            String sql = "select sum(b.amount) from SmsFee b where b.phone = '" + phone + "' and b.charged = true and b.createTime >= ? and b.createTime < ?";
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1, 0, 0, 0);
            Query query = getSession().createQuery(sql);
            query.setDate(0, cal.getTime());
            cal.add(Calendar.MONTH, 1);
            query.setDate(1, cal.getTime());
            Long ret = (Long)query.uniqueResult();
            
            if(ret == null){
                return 0;
            }else{
                return ret.intValue();
            }
        }catch(Exception ex){
            ex.printStackTrace();
            return 0;
        }finally{
            closeSession();
        }
    }
    
    public void saveSmsFee(SmsFee smsFee){
        try {
            super.makePersistent(smsFee);
        } catch (DataAccessException ex) {
            ex.printStackTrace();
        }
    }
}
