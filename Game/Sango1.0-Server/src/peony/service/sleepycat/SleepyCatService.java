package peony.service.sleepycat;

import java.io.File;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import peony.service.Service;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.Sequence;
import com.sleepycat.je.SequenceConfig;

public class SleepyCatService implements Service {

	private static final Logger log = Logger.getLogger(SleepyCatService.class);
	
	protected Environment dbEnv;
	protected DatabaseConfig dbConfig;
	
	public Database seqDB;
	public Database kickedPlayersDB;
	public Database kickAccountDB;
	public Database accountStatDB;
	public Database notificationDB;
	public Database anniversaryDB;
	public Database stepBattleDB;
	public Database tenthAnniversaryServiceDB;
	
	protected BulkSequence itemSequence;
	protected BulkSequence horseSequence;
	protected BulkSequence attendantSequence;
	
	protected Thread syncThread;
	
	public SleepyCatService(Configuration config) throws Exception{
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setTransactional(true);
        envConfig.setAllowCreate(true);
        File dbRoot = new File(config.getString("sleepycat"));
        dbEnv = new Environment(dbRoot, envConfig);
        dbConfig = new DatabaseConfig();
        dbConfig.setTransactional(false);
        dbConfig.setAllowCreate(true);
        dbConfig.setDeferredWrite(false);
        seqDB = dbEnv.openDatabase(null, "SequenceDB", dbConfig);
        
        // 处理旧格式的sequence数据（如果存在的话）
        SequenceConfig seqConfig = new SequenceConfig();
        seqConfig.setAllowCreate(false);
        seqConfig.setInitialValue(1L); //从1开始
        try {
        	DatabaseEntry key = new DatabaseEntry("itemid".getBytes("UTF-8"));
        	Sequence seq = seqDB.openSequence(null, key, seqConfig);
        	int next = (int)seq.get(null, 1);
        	itemSequence = new BulkSequence(seqDB, "itemid_seq", next);
        	seqDB.delete(null, key);
        } catch (Exception e) {
        	itemSequence = new BulkSequence(seqDB, "itemid_seq", 1);
        }
        try {
        	DatabaseEntry key = new DatabaseEntry("horseid".getBytes("UTF-8"));
        	Sequence seq = seqDB.openSequence(null, key, seqConfig);
        	int next = (int)seq.get(null, 1);
        	horseSequence = new BulkSequence(seqDB, "horseid_seq", next);
        	seqDB.delete(null, key);
        } catch (Exception e) {
        	horseSequence = new BulkSequence(seqDB, "horseid_seq", 1);
        }
        try {
	        DatabaseEntry key = new DatabaseEntry("attendantid".getBytes("UTF-8"));
	        Sequence seq = seqDB.openSequence(null, key, seqConfig);
	        int next = (int)seq.get(null, 1);
	        attendantSequence = new BulkSequence(seqDB, "attendantid_seq", next);
	        seqDB.delete(null, key);
        } catch (Exception e) {
        	attendantSequence = new BulkSequence(seqDB, "attendantid_seq", 1);
        }
        
        kickedPlayersDB = dbEnv.openDatabase(null, "kickedDB", dbConfig);
        kickAccountDB = dbEnv.openDatabase(null, "kickaccountdb", dbConfig);
        accountStatDB = dbEnv.openDatabase(null, "accountStatDB", dbConfig);
        notificationDB = dbEnv.openDatabase(null, "notificationDB", dbConfig);
        anniversaryDB = dbEnv.openDatabase(null, "anniversaryDB", dbConfig);
        stepBattleDB = dbEnv.openDatabase(null, "stepBattleDB", dbConfig);
        tenthAnniversaryServiceDB = dbEnv.openDatabase(null, "tenthAnniversaryServiceDB", dbConfig);
        syncThread = new Thread(new IdSyncer(),"IDSYNC");
        syncThread.start();
	}
	
	public Database openDatabase(String name) throws DatabaseException {
		return dbEnv.openDatabase(null, name, dbConfig);
	}
	
	public void removeDatabase(String name) {
		try {
			dbEnv.removeDatabase(null, name);
		} catch (Exception e) {
			log.error(e, e);
		}
	}
	
	public int generatorHorseId(){
		return horseSequence.getNext();
	}
	
	public int generateAttendantInstanceId(){
		return attendantSequence.getNext();
	}
	
	public int generatorItemId() {
		return itemSequence.getNext();
	}
	
	public void shutdown() {
		syncThread.interrupt();
		horseSequence.clearBuffer();
		itemSequence.clearBuffer();
		attendantSequence.clearBuffer();
		try {
			seqDB.close();
		} catch (DatabaseException e1) {
		}
		try{
			kickedPlayersDB.close();
		} catch (DatabaseException e1) {
		}
		try{
			kickAccountDB.close();
		} catch (DatabaseException e1) {
		}
		try{
			accountStatDB.close();
		} catch (DatabaseException e1) {
		}
		try {
			notificationDB.close();
		} catch (DatabaseException e1) {
		}
		try {
			anniversaryDB.close();
		} catch (DatabaseException e) {
		}
		try {
			stepBattleDB.close();
		} catch (DatabaseException e) {
		}
		try {
			tenthAnniversaryServiceDB.close();
		} catch (DatabaseException e) {
		}
        try {
            dbEnv.close();
        } catch (Exception e) {
        }
	}

	public void startup() throws Exception {
		
	}

	class IdSyncer implements Runnable{
		public void run(){
			while (true) {
				if(dbEnv!=null){
					try {
						dbEnv.sync();
						log.info("[IDSYNC]");
					} catch (DatabaseException e) {
						e.printStackTrace();
					}
				}
				try {
					Thread.sleep(30*1000L);
				} catch (InterruptedException e) {
					log.info("SYNCBREAK");
					break;
				}
			}
			
		}
	}
}
