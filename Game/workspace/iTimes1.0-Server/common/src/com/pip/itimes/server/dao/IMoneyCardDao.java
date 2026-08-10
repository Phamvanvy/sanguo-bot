package com.pip.itimes.server.dao;

import java.util.List;

import com.pip.itimes.server.bean.IMoneyCard;

public class IMoneyCardDao extends BaseDao{
    public IMoneyCardDao(){
        super();
    }

    public void addIMoneyCard(IMoneyCard iMoneyCard) throws DataAccessException{
        makePersistent(iMoneyCard);
    }

    public void updateIMoneyCard(IMoneyCard iMoneyCard) throws DataAccessException{
        makePersistent(iMoneyCard);
    }

    public IMoneyCard[] getAvailableIMoneyCardList() throws DataAccessException{
        List l = getList("from IMoneyCard i where i.status = 0");
        IMoneyCard[] ret = new IMoneyCard[l.size()];
        l.toArray(ret);
        return ret;
    }
}
