package com.pip.server.account.stub;

import com.pip.server.account.bean.Sequence;
import com.pip.server.account.dao.SequenceDAO;

public class SequenceGenerator {
	private static int accountId = 1;
	private static int accountMaxId = 0;
	
    private static Object accountLock = new Object();
    
    private static SequenceDAO dao = new SequenceDAO();
    

    public static String getAccountName() throws Exception{
        synchronized(accountLock){
            if (accountId >= accountMaxId) {
                getNewAccountName();
                return "" + accountId++;
            } else {
                return "" + accountId++;
            }
        }
    }

    public static void getNewAccountName() throws Exception {
		Sequence bean = dao.getSequence(1);
		int usedId = bean.getUsedId();
		bean.setUsedId(usedId + 20);
		dao.makePersistent(bean);
		accountId = usedId;
		accountMaxId = usedId + 20;

	}
	
}
