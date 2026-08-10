package com.pip.gameaccount.qq;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.OperationStatus;

public class QQBillingService implements Runnable {
    private static Logger log = Logger.getLogger(QQBillingService.class);
    
    /*
     * 数据库环境
     */
    private Environment dbEnv;
    /*
     * 保存支付请求信息的数据库
     */
    private Database billingDB;
	
	private FeeDAO feeDao;
	
	public QQBillingService(FeeDAO feeDao, String dbdir) throws Exception {
		this.feeDao = feeDao;
		
		// 创建数据库环境
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setTransactional(true);
        envConfig.setAllowCreate(true);
        File dbRoot = new File(dbdir);
        dbEnv = new Environment(dbRoot, envConfig);

        // 打开数据库
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setTransactional(false);
        dbConfig.setAllowCreate(true);
        dbConfig.setDeferredWrite(true);
        billingDB = dbEnv.openDatabase(null, "BillingDB", dbConfig);
        
        // 删除30天之前的记录
		deleteObsoleteBillingRecords(System.currentTimeMillis() - 30 * 86400 * 1000L);
		
		new Thread(this).start();
	}
	
	/**
     * 关闭服务
     */
    public void close() {
        try {
            billingDB.close();
        } catch (Exception e) {
        }
        try {
            dbEnv.close();
        } catch (Exception e) {
        }
    }
    
    /**
     * 定时同步数据库。
     */
    public void run() {
        while (true) {
            // 每30秒保存一次数据库
            try {
                Thread.sleep(30000);
            } catch (Exception e) {
            }
            try {
                dbEnv.sync();
            } catch (Exception e) {
            }
        }
    }
	
	public void addNewFee(Fee fee){
		feeDao.createFee(fee);
	}
	
	public synchronized void addBilling(QQBilling billing){
	    try {
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry dataEntry = new DatabaseEntry();
            StringBinding.stringToEntry(billing.getLinkId(), keyEntry);
            StringBinding.stringToEntry(billing.getSaveData(), dataEntry);
            billingDB.put(null, keyEntry, dataEntry);
        } catch (Exception e) {
            log.error(e, e);
        }
	}
	
	public synchronized QQBilling removeBilling(String linkId) {
		try {
            DatabaseEntry keyEntry = new DatabaseEntry();
            StringBinding.stringToEntry(linkId, keyEntry);
            DatabaseEntry dataEntry = new DatabaseEntry();
            if (billingDB.get(null, keyEntry, dataEntry, null) == OperationStatus.NOTFOUND) {
                return null;
            }
            billingDB.delete(null, keyEntry);
            return new QQBilling(linkId, StringBinding.entryToString(dataEntry));
        } catch (Exception e) {
            log.error(e, e);
            return null;
        }
	}
	
	private synchronized void deleteObsoleteBillingRecords(long valveTime) {
	    List<String> obsoleteIDs = new ArrayList<String>();
        Cursor cursor = null;
        try {
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry dataEntry = new DatabaseEntry();
            cursor = billingDB.openCursor(null, new CursorConfig());
            while (cursor.getNext(keyEntry, dataEntry, null) != OperationStatus.NOTFOUND) {
                QQBilling record = new QQBilling(StringBinding.entryToString(keyEntry), 
                        StringBinding.entryToString(dataEntry));
                if (record.getTime() < valveTime) {
                    obsoleteIDs.add(record.getLinkId());
                }
            }
        } catch (Exception e) {
            log.error(e, e);
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                }
            }
        }
        for (String id : obsoleteIDs) {
            removeBilling(id);
        }
	}
}
