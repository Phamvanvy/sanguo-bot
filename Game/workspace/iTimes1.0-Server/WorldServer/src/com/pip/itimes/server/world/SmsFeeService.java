package com.pip.itimes.server.world;


import com.pip.itimes.server.bean.SmsFee;
import com.pip.itimes.server.dao.DataAccessException;
import com.pip.itimes.server.dao.SmsFeeDao;


public class SmsFeeService{
    private SmsFeeDao dao;

    public SmsFeeService(SmsFeeDao smsFeeDao){
        this.dao = smsFeeDao;
    }

    public void addSmsFee(SmsFee smsFee) throws BuyException{
        try{
            dao.makePersistent(smsFee);
        }catch(DataAccessException ex){
            throw new BuyException("Ìí¼Ó¶ÌÐÅ¹ºÂò¼ÇÂ¼´íÎó");
        }
    }
    
    public int getMonthSmsFee(String phone){
        return dao.getMonthAmountByPhone(phone);
    }
}
