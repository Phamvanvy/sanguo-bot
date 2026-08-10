package com.pip.itimes.server.auth;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.pip.itimes.server.bean.FreeUser;
import com.pip.itimes.server.dao.FreeUserDao;

public class FreeUserService {

	private static Logger log = Logger.getLogger(FreeUserService.class);

	private Map freeUsers;

        public FreeUserDao dao = null;
        public FreeUserService(FreeUserDao dao){
            this.dao = dao;
            init();
        }

    public void init() {
        try {
            List l = dao.getAllFreeUser();
            freeUsers = new HashMap();
            Iterator ite = l.iterator();
            while (ite.hasNext()) {
                FreeUser user = (FreeUser) ite.next();
                Integer key = new Integer(user.getId());
                freeUsers.put(key, user);
            }
        } catch (Exception ex) {
            log.debug(ex, ex);
        }
    }
}
