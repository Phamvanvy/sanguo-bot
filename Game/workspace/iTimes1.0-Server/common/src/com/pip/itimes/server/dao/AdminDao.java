package com.pip.itimes.server.dao;

import com.pip.itimes.server.bean.Admin;
import java.util.*;
/**
 * @author Jeffrey
 * @version 1.0
 */
public class AdminDao extends BaseDao{
    public AdminDao() {
    }

    public Admin getAdmin(String name,String password){
        try {
            String hql = "from Admin a where a.name='" + name + "' and a.password='" +
                         password + "'";
            return (Admin) uniqueResult(hql);
        } catch (DataAccessException ex) {
            return null;
        }
    }
    public List getAdminList() {
    	try {
			return getList("from Admin a");
		} catch (DataAccessException e) {
			return null;
		}
    }

    public void addAdmin(Admin admin){
        try {
            makePersistent(admin);
        } catch (DataAccessException ex) {
        }
    }
}
