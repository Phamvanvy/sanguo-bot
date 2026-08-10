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
	public Database accountStatDB;
	
	protected Sequence itemSequence;
	protected Sequence horseSequence;
	protected Sequence attendantSequence;
	
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
        SequenceConfig seqConfig = new SequenceConfig();
        seqConfig.setAllowCreate(true);
        seqConfig.setInitialValue(1L); //´Ó1¿ªÊ¼
        DatabaseEntry key = new DatabaseEntry("itemid".getBytes("UTF-8"));
        itemSequence = seqDB.openSequence(null, key, seqConfig);
        DatabaseEntry key1 = new DatabaseEntry("horseid".getBytes("UTF-8"));
        horseSequence = seqDB.openSequence(null, key1, seqConfig);
        DatabaseEntry key2 = new DatabaseEntry("attendantid".getBytes("UTF-8"));
        attendantSequence = seqDB.openSequence(null, key2, seqConfig);
        kickedPlayersDB = dbEnv.openDatabase(null, "kickedDB", dbConfig);
        accountStatDB = dbEnv.openDatabase(null, "accountStatDB", dbConfig);
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
		try {
			return (int)horseSequence.get(null, 1);
		} catch (DatabaseException e) {
			log.error(e,e);
			throw new RuntimeException(e);
		}
		finally{
//			try {
//				dbEnv.sync();
//			} catch (DatabaseException e) {
//				e.printStackTrace();
//			}
		}
	}
	
	public int generateAttendantInstanceId(){
		try {
			return (int)attendantSequence.get(null, 1);
		} catch (DatabaseException e) {
			log.error(e,e);
			throw new RuntimeException(e);
		}
		finally{
			
		}
	}
	
	public int generatorItemId() {
		try {
			return (int)itemSequence.get(null, 1);
		} catch (DatabaseException e) {
			log.error(e,e);
			throw new RuntimeException(e);
		}
		finally{
//			try {
//				dbEnv.sync();
//			} catch (DatabaseException e) {
//				e.printStackTrace();
//			}
		}
	}
	
	public void shutdown() {
		syncThread.interrupt();
		try {
			horseSequence.close();
		} catch (DatabaseException e1) {
		}
		try {
			itemSequence.close();
		} catch (DatabaseException e1) {
		}
		try {
			seqDB.close();
		} catch (DatabaseException e1) {
		}
		try{
			kickedPlayersDB.close();
		} catch (DatabaseException e1) {
		}
		try{
			accountStatDB.close();
		} catch (DatabaseException e1) {
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
