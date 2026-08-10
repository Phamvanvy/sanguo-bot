package com.pip.server.auth.cmcc;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

import com.pip.server.auth.Server;
import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Sequence;
import com.sleepycat.je.SequenceConfig;
import com.sleepycat.je.Transaction;

/**
 * 卓望平台登录用户缓冲。这个Cache用一个Berkeley DB来保存用户和Key的关系。
 */
public class CmccUserCache implements Runnable {
    private static final Logger log = Logger.getLogger(CmccUserCache.class);

    /*
     * 数据库环境
     */
    private Environment dbEnv;
    /*
     * 保存用户信息的数据库
     */
    private Database userDB;
    /*
     * Sequence数据库
     */
    private Database seqDB;
    /*
     * 生成短信计费请求ID的Sequence
     */
    private Sequence smsReqSequence;
    /*
     * 保存短信计费请求的数据库
     */
    private Database smsReqDB;
    /*
     * 保存在线帐号和平台帐号关系的数据库
     */
    private Database loginInfoDB;
    /*
     * 记录每个用户当月购买的金额。这个表里key是用户ID，value是当月消费金额（分）。key为"LastClear"
     * 的记录的value是上次清除此数据库的时间（long）。
     */
    private Database consumeAmountDB;
    /*
     * 上次清除consumeAmountDB的时间。
     */
    private long lastClearAmountTime;
    /*
     * 消费历史数据库
     */
    private Database buyRecordDB;
    /*
     * 平台接口服务，只用于用户登出通知
     */
    private CmccService service;
    
    public CmccUserCache() throws Exception {
        // 创建数据库环境
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setTransactional(true);
        envConfig.setAllowCreate(true);
        File dbRoot = new File(Server.instance.getConfiguration().getString("dbdir"));
        dbEnv = new Environment(dbRoot, envConfig);

        // 打开数据库
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setTransactional(false);
        dbConfig.setAllowCreate(true);
        dbConfig.setDeferredWrite(true);
        userDB = dbEnv.openDatabase(null, "UserKeyDB", dbConfig);
        seqDB = dbEnv.openDatabase(null, "SequenceDB", dbConfig);
        smsReqDB = dbEnv.openDatabase(null, "SMSReqDB", dbConfig);
        SequenceConfig seqConfig = new SequenceConfig();
        seqConfig.setAllowCreate(true);
        DatabaseEntry key = new DatabaseEntry("smsreqid".getBytes("UTF-8"));
        smsReqSequence = seqDB.openSequence(null, key, seqConfig);
        loginInfoDB = dbEnv.openDatabase(null, "LoginInfoDB", dbConfig);
        consumeAmountDB = dbEnv.openDatabase(null, "ConsumeAmountDB", dbConfig);
        buyRecordDB = dbEnv.openDatabase(null, "BuyRecordDB", dbConfig);
        
        // 查询上次清除消费记录表的时间
        lastClearAmountTime = System.currentTimeMillis();
        try {
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry dataEntry = new DatabaseEntry();
            StringBinding.stringToEntry("LastClear", keyEntry);
            if (userDB.get(null, keyEntry, dataEntry, null) != OperationStatus.NOTFOUND) {
                lastClearAmountTime = LongBinding.entryToLong(dataEntry);
            }
        } catch (Exception e) {
            log.error(e, e);
        }
        
        new Thread(this).start();
    }
    
    public void setCmccService(CmccService s) {
        service = s;
    }
    
    /*
     * 检查是否已经跨月了，如果跨月则清除消费金额数据库。
     */
    private void checkClearAmountDB() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(lastClearAmountTime);
        int lastClearMon = cal.get(Calendar.YEAR) * 100 + cal.get(Calendar.MONTH);
        cal.setTimeInMillis(System.currentTimeMillis());
        int currMon = cal.get(Calendar.YEAR) * 100 + cal.get(Calendar.MONTH);
        if (lastClearMon != currMon) {
            lastClearAmountTime = System.currentTimeMillis();
            
            // 清除ConsumeAmountDB里的所有记录
            synchronized (this) {
                try {
                    // 关闭数据库
                    consumeAmountDB.close();
                    
                    // 清空
                    dbEnv.truncateDatabase(null, "ConsumeAmountDB", false);
                    
                    // 重新打开数据库
                    DatabaseConfig dbConfig = new DatabaseConfig();
                    dbConfig.setTransactional(false);
                    dbConfig.setAllowCreate(true);
                    dbConfig.setDeferredWrite(true);
                    consumeAmountDB = dbEnv.openDatabase(null, "ConsumeAmountDB", dbConfig);
                    
                    // 向数据库中写入最新清除时间
                    DatabaseEntry keyEntry = new DatabaseEntry();
                    DatabaseEntry dataEntry = new DatabaseEntry();
                    StringBinding.stringToEntry("LastClear", keyEntry);
                    LongBinding.longToEntry(System.currentTimeMillis(), dataEntry);
                    consumeAmountDB.put(null, keyEntry, dataEntry);
                } catch (Exception e) {
                    log.error(e, e);
                }
            }
        }
    }
    
    /**
     * 定时同步数据库。
     */
    public void run() {
        long lastCheckTime = System.currentTimeMillis();
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
            
            // 检查如果跨月了，就清除一次购买记录数据库
            checkClearAmountDB();
            
            try {
                // 每5分钟检查一次过期不活跃用户
                if (System.currentTimeMillis() - lastCheckTime > 300000) {
                    try {
                        List<CmccUserKey> timeoutKeys = findTimeoutUsers();
                        for (CmccUserKey key : timeoutKeys) {
                            // 删除用户记录
                            deleteUserKey(key.getUserId());
                            
                            // 向网游平台同步
                            service.logout(key.getUserId());
                        }
                    } catch (Exception e) {
                    }
                    lastCheckTime = System.currentTimeMillis();
                }
            } catch (Exception e) {
                log.error(e, e);
            }
        }
    }
    
    /**
     * 关闭服务
     */
    public void close() {
        try {
            loginInfoDB.close();
        } catch (Exception e) {
        }
        try {
            smsReqSequence.close();
        } catch (Exception e) {
        }
        try {
            seqDB.close();
        } catch (Exception e) {
        }
        try {
            smsReqDB.close();
        } catch (Exception e) {
        }
        try {
            userDB.close();
        } catch (Exception e) {
        }
        try {
            consumeAmountDB.close();
        } catch (Exception e) {
        }
        try {
            buyRecordDB.close();
        } catch (Exception e) {
        }
        try {
            dbEnv.close();
        } catch (Exception e) {
        }
    }

    /**
     * 添加/更新一个平台用户的信息
     * @param userKey
     */
    public void addUserKey(CmccUserKey userKey) {
        try {
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry dataEntry = new DatabaseEntry();
            StringBinding.stringToEntry(userKey.getUserId(), keyEntry);
            StringBinding.stringToEntry(userKey.getSaveKey(), dataEntry);
            userDB.put(null, keyEntry, dataEntry);
        } catch (Exception e) {
            log.error(e, e);
        }
    }
    
    /**
     * 刷新一个用户的最新活跃时间。10分钟不活跃的用户会被自动下线。
     * @param userKey
     */
    public void activeUserKey(CmccUserKey userKey) {
        userKey.refreshActiveTime();
        addUserKey(userKey);
    }
    
    /**
     * 删除一个用户的信息
     * @param userId
     */
    public void deleteUserKey(String userId) {
        try {
            DatabaseEntry keyEntry = new DatabaseEntry();
            StringBinding.stringToEntry(userId, keyEntry);
            userDB.delete(null, keyEntry);
        } catch (Exception e) {
            log.error(e, e);
        }
    }
    
    /**
     * 查询一个用户的平台用户信息
     * @param userId 
     * @return
     */
    public CmccUserKey getUserKey(String userId) {
        try {
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry dataEntry = new DatabaseEntry();
            StringBinding.stringToEntry(userId, keyEntry);
            if (userDB.get(null, keyEntry, dataEntry, null) == OperationStatus.NOTFOUND) {
                return null;
            }
            return new CmccUserKey(userId, StringBinding.entryToString(dataEntry));
        } catch (Exception e) {
            log.error(e, e);
            return null;
        }
    }

    /**
     * 检查客户端传上来的用户ID和KEY是否正确。
     * @param userId
     * @param key
     * @return
     */
    public boolean isValid(String userId, String key) {
        log.info("CMCC User Verify, UserID[" + userId + "]Key[" + key + "]");
        CmccUserKey u = getUserKey(userId);
        if (u == null)
            return false;
        if (u.getKey().equals(key)) {
            activeUserKey(u);
            return true;
        }
        return false;
    }
    
    /**
     * 生成一个短信购买请求记录。
     * @param requestId 世界服务器生成的请求ID
     * @param userKey 用户的平台对应信息
     * @param serverID 发起请求的用户所在的服务器
     * @param accountId 账户ID
     * @param playerId 用户ID
     * @param consumeCode 消费代码
     * @return String 生成购买请求代码（16位数字）
     */
    public String createSmsBuyReq(int requestId, CmccUserKey userKey, String serverID, 
            int accountId, int playerId, String consumeCode) {
        try {
            // 生成序列号
            long seq = smsReqSequence.get(null, 1);
            int idNum = (int)((seq % 1000000) + 1000000);
            String idStr = "000000000" + String.valueOf(idNum);
            
            // 保存请求
            String dataStr = requestId + "," + userKey.getUserId() + "," + serverID + "," + 
                accountId + "," + playerId + "," + consumeCode;
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry dataEntry = new DatabaseEntry();
            StringBinding.stringToEntry(idStr, keyEntry);
            StringBinding.stringToEntry(dataStr, dataEntry);
            smsReqDB.put(null, keyEntry, dataEntry);
            return idStr;
        } catch (Exception e) {
            log.error(e, e);
            return null;
        }
    }
    
    /**
     * 查找一个短信购买请求。如果找到，这个请求还会被删除（一个请求只能购买1次）。
     * @param token
     * @return 如果找不到，返回null
     */
    public CmccSmsBuyReq findSmsBuyReq(String token) {
        try {
            // 查找数据
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry dataEntry = new DatabaseEntry();
            StringBinding.stringToEntry(token, keyEntry);
            if (smsReqDB.get(null, keyEntry, dataEntry, null) == OperationStatus.NOTFOUND) {
                return null;
            }
            String str = StringBinding.entryToString(dataEntry);
            
            // 删除记录
            smsReqDB.delete(null, keyEntry);
            
            // 生成返回
            String[] secs = str.split(",");
            CmccSmsBuyReq ret = new CmccSmsBuyReq();
            ret.token = token;
            ret.requestId = Integer.parseInt(secs[0]);
            ret.userId = secs[1];
            ret.serverId = secs[2];
            ret.accountId = Integer.parseInt(secs[3]);
            ret.playerId = Integer.parseInt(secs[4]);
            ret.consumeCode = secs[5];
            return ret;
        } catch (Exception e) {
            log.error(e, e);
            return null;
        }
    }
    
    /**
     * 注册一个登录关系。
     * @param accountId 帐号ID
     * @param userId 平台用户ID
     */
    public void registerLoginInfo(int accountId, String userId) {
        try {
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry dataEntry = new DatabaseEntry();
            IntegerBinding.intToEntry(accountId, keyEntry);
            StringBinding.stringToEntry(userId, dataEntry);
            loginInfoDB.put(null, keyEntry, dataEntry);
        } catch (Exception e) {
            log.error(e, e);
        }
    }
    
    /**
     * 查找一个用户的登录平台用户信息。
     * @param accountId 帐号ID
     */
    public CmccUserKey getLoginInfo(int accountId) {
        try {
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry dataEntry = new DatabaseEntry();
            IntegerBinding.intToEntry(accountId, keyEntry);
            if (loginInfoDB.get(null, keyEntry, dataEntry, null) == OperationStatus.NOTFOUND) {
                return null;
            }
            String userId = StringBinding.entryToString(dataEntry);
            return getUserKey(userId);
        } catch (Exception e) {
            log.error(e, e);
            return null;
        }
    }
 
    /**
     * 查找所有超时不活跃用户（activeTime在10分钟以前的）。
     */
    public List<CmccUserKey> findTimeoutUsers() {
        List<CmccUserKey> ret = new ArrayList<CmccUserKey>();
        Cursor cursor = null;
        long checkTime = System.currentTimeMillis() - 10 * 60 * 1000L;
        try {
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry dataEntry = new DatabaseEntry();
            cursor = userDB.openCursor(null, new CursorConfig());
            while (cursor.getNext(keyEntry, dataEntry, null) != OperationStatus.NOTFOUND) {
                CmccUserKey key = new CmccUserKey(StringBinding.entryToString(keyEntry), 
                        StringBinding.entryToString(dataEntry));
                if (key.getActiveTime() < checkTime) {
                    // 过期了
                    ret.add(key);
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
        return ret;
    }
    
    /**
     * 查询一个用户本月消费金额。
     * @param userID
     * @return
     */
    public synchronized int getConsumeAmount(String userID) {
        try {
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry dataEntry = new DatabaseEntry();
            StringBinding.stringToEntry(userID, keyEntry);
            if (consumeAmountDB.get(null, keyEntry, dataEntry, null) == OperationStatus.NOTFOUND) {
                return 0;
            }
            return IntegerBinding.entryToInt(dataEntry);
        } catch (Exception e) {
            log.error(e, e);
            return 0;
        }
    }
    
    /**
     * 增加一个用户的本月消费金额记录。
     * @param userID
     * @param amount 金额（分）
     */
    public synchronized void addConsumeAmount(String userID, int amount) {
        try {
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry dataEntry = new DatabaseEntry();
            StringBinding.stringToEntry(userID, keyEntry);
            IntegerBinding.intToEntry(getConsumeAmount(userID) + amount, dataEntry);
            consumeAmountDB.put(null, keyEntry, dataEntry);
        } catch (Exception e) {
            log.error(e, e);
        }
        addBuyRecord(userID, amount);
    }
    
    /**
     * 查询一个用户在指定时间内的的消费金额。
     * @param userID
     * @param period 毫秒
     * @return
     */
    public synchronized int getBuyAmount(String userID, long period) {
        try {
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry dataEntry = new DatabaseEntry();
            StringBinding.stringToEntry(userID, keyEntry);
            if (buyRecordDB.get(null, keyEntry, dataEntry, null) == OperationStatus.NOTFOUND) {
                return 0;
            }
            CmccBuyRecord br = new CmccBuyRecord(userID, StringBinding.entryToString(dataEntry));
            int ret = br.getAmount(period);
            br.clearOldData(period);
            if (br.isModified()) {
                StringBinding.stringToEntry(br.getSaveData(), dataEntry);
                buyRecordDB.put(null, keyEntry, dataEntry);
            }
            return ret;
        } catch (Exception e) {
            log.error(e, e);
            return 0;
        }
    }
    
    /**
     * 记录一个用户的消费记录。
     */
    public synchronized void addBuyRecord(String userID, int amount) {
        try {
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry dataEntry = new DatabaseEntry();
            StringBinding.stringToEntry(userID, keyEntry);
            CmccBuyRecord br;
            if (buyRecordDB.get(null, keyEntry, dataEntry, null) == OperationStatus.NOTFOUND) {
                br = new CmccBuyRecord(userID, null);
            } else {
                br = new CmccBuyRecord(userID, StringBinding.entryToString(dataEntry));
            }
            br.addRecord(amount);
            StringBinding.stringToEntry(br.getSaveData(), dataEntry);
            buyRecordDB.put(null, keyEntry, dataEntry);
        } catch (Exception e) {
            log.error(e, e);
        }
    }
}
