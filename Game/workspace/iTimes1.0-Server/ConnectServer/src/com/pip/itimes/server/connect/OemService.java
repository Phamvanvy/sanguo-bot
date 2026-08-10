package com.pip.itimes.server.connect;

import com.pip.itimes.server.dao.OemDao;
import com.pip.itimes.server.dao.*;
import com.pip.itimes.server.bean.Oem;
import java.util.List;

/**
 * @author Jeffery
 * @version 1.0
 */
public class OemService {

    private OemDao dao;

    public OemService(OemDao dao) {
        this.dao = dao;
    }

    public int getCount(int shopId) throws OemException{
        try {
            return dao.getCount(shopId);
        } catch (DataAccessException ex) {
            throw new OemException("查询错误");
        }
    }

    public Oem[] getOems(int shopId,int begin,int count) throws OemException{
        try {
            return dao.getOems(shopId, begin, count);
        } catch (DataAccessException ex) {
            throw new OemException("查询求做列表错误");
        }
    }

    public int getCount(short areaId,byte type,String name) throws OemException{
        try {
            return dao.getCount(areaId, type, name);
        } catch (DataAccessException ex) {
            throw new OemException("查询错误");
        }
    }

    public Oem[] getOems(short areaId, byte type, String name, int begin,
                         int count) throws
            OemException {
        try {
            return  dao.getOems(areaId, type, name, begin, count);
        } catch (DataAccessException ex) {
            throw new OemException("查询求做列表错误");
        }
    }
}
