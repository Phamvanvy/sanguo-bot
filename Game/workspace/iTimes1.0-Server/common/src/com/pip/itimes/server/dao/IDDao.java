package com.pip.itimes.server.dao;

import com.pip.itimes.server.bean.IDBean;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class IDDao extends BaseDao{
    public IDDao() {
        super();
    }

    public IDBean getEquipmentIdBean() throws DataAccessException{
        return (IDBean)getObject(IDBean.class,new Integer(1));
    }

    public IDBean getPetIdBean() throws DataAccessException{
        return (IDBean)getObject(IDBean.class,new Integer(2));
    }

    public IDBean getAccountNameBean() throws DataAccessException{
        return (IDBean)getObject(IDBean.class,new Integer(3));
    }
}
