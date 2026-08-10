package com.pip.server.util;

import com.pip.server.auth.bean.IDBean;
import com.pip.server.auth.dao.*;

/**
 * @author Jeffery
 * @version 1.0
 */
public class IDGenerator {
    public static final IDDao dao = new IDDao();
    private static int accountName = 1;
    private static int accountMaxName = 0;

    private static Object accountLock = new Object();

    public static String getAccountName(){
        synchronized(accountLock){
            if (accountName >= accountMaxName) {
                getNewAccountName();
                return "" + accountName++;
            } else {
                return "" + accountName++;
            }
        }
    }

    public static void getNewAccountName(){
        try {
            IDBean bean = dao.getAccountNameBean();
            int usedId = bean.getUsedId();
            bean.setUsedId(usedId + 20);
            dao.makePersistent(bean);
            accountName = usedId;
            accountMaxName = usedId + 20;
        } catch (DataAccessException ex) {
            ex.printStackTrace();
        }
    }
}
