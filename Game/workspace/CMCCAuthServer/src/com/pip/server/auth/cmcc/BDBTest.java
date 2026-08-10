package com.pip.server.auth.cmcc;

import java.io.File;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.sleepycat.bind.ByteArrayBinding;
import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.*;

public class BDBTest {
    Environment dbEnv;
    Database userDB;
    Database seqDB;
    Sequence seq;
    Random rnd = new Random();
    AtomicInteger testCounter = new AtomicInteger(0);
    AtomicInteger timeCounter = new AtomicInteger(0);
    long startTime = System.currentTimeMillis();
    boolean transaction = true;
    boolean active = true;
    
    public static void main(String[] args) throws Exception {
        new BDBTest().go();
    }
    
    public void go() throws Exception {
        // 创建数据库环境
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setTransactional(true);
        envConfig.setAllowCreate(true);
        File dbRoot = new File("c:/temp");
        dbEnv = new Environment(dbRoot, envConfig);

        // 打开数据库
        Transaction txn = null;
        if (transaction) {
            txn = dbEnv.beginTransaction(null, null);
        }
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setTransactional(transaction);
        dbConfig.setAllowCreate(true);
        dbConfig.setDeferredWrite(!transaction);
        userDB = dbEnv.openDatabase(txn, "TestDB", dbConfig);
        seqDB = dbEnv.openDatabase(txn, "SeqDB", dbConfig);
        DatabaseEntry key = new DatabaseEntry("id1".getBytes("UTF-8"));
        SequenceConfig config = new SequenceConfig();
        config.setAllowCreate(true);
        seq = seqDB.openSequence(txn, key, config);
        if (transaction) {
            txn.commit();
        }
        testSeq(true);
        
        for (int i = 0; i < 200; i++) {
            new TestThread().start();
        }
        new PrintThread().start();
        
        System.in.read();
        active = false;
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
        }
        testSeq(true);
        System.out.println(userDB.count());
        seq.close();
        seqDB.close();
        userDB.close();
        System.exit(0);
    }
    
    class PrintThread extends Thread {
        public void run() {
            while (true) {
                try {
                    Thread.sleep(10000);
                } catch (Exception e) {
                }
                int n1 = testCounter.get();
                int n2 = timeCounter.get();
                long usedTime = System.currentTimeMillis() - startTime;
                int sec = (int)(usedTime / 1000);
                System.out.print("第" + sec + "秒：");
                System.out.println("共" + n1 + "次，" + (n1 / sec) + "次/秒");
                System.out.println("执行时间：共" + (n2 / 1000) + "s，平均" + (n2 / n1) + "ms");
            }
        }
    }
    
    void testAdd(int key, byte[] data) {
        Transaction txn = null;
        try {
            if (transaction) {
                txn = dbEnv.beginTransaction(null, null);
            }
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry dataEntry = new DatabaseEntry();
            IntegerBinding.intToEntry(key, keyEntry);
            new ByteArrayBinding().objectToEntry(data, dataEntry);
            userDB.put(txn, keyEntry, dataEntry);
            if (transaction) {
                txn.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (txn != null) {
                try {
                    txn.abort();
                } catch (Exception e1) {
                }
            }
        }
    }
    
    void testRead(int key) {
        Transaction txn = null;
        try {
            if (transaction) {
                txn = dbEnv.beginTransaction(null, null);
            }
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry dataEntry = new DatabaseEntry();
            IntegerBinding.intToEntry(key, keyEntry);
            if (userDB.get(txn, keyEntry, dataEntry, null) == OperationStatus.NOTFOUND) {
                System.err.println("data not found: " + key);
            }
            if (transaction) {
                txn.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (txn != null) {
                try {
                    txn.abort();
                } catch (Exception e1) {
                }
            }
        }
    }
    
    ConcurrentHashMap<Long, Long> usedIDs = new ConcurrentHashMap<Long, Long>();
    void testSeq(boolean print) {
        Transaction txn = null;
        try {
            if (transaction) {
                txn = dbEnv.beginTransaction(null, null);
            }
            long newid = seq.get(txn, 1);
            if (print) {
                System.out.println(newid);
            }
            if (usedIDs.containsKey(newid)) {
                System.err.println("sequence error");
            }
            usedIDs.put(newid, newid);
            if (transaction) {
                txn.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (txn != null) {
                try {
                    txn.abort();
                } catch (Exception e1) {
                }
            }
        }
    }
    
    class TestThread extends Thread {
        public void run() {
            while (active) {
                try {
                    int key = testCounter.incrementAndGet();
                    int len = 8000 + rnd.nextInt(4000);
                    byte[] data = new byte[len];
                    rnd.nextBytes(data);
                    
                    long time = System.currentTimeMillis();
                    testAdd(key, data);
//                    testRead(key);
//                    for (int i = 0; i < 1; i++) {
//                        testSeq(false);
//                    }
                    timeCounter.addAndGet((int)(System.currentTimeMillis() - time));
                    
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
