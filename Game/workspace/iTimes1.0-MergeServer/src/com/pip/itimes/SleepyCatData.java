package com.pip.itimes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Sequence;
import com.sleepycat.je.SequenceConfig;
import com.sleepycat.je.SequenceStats;

public class SleepyCatData{
    private static final Logger log = Logger.getLogger(SleepyCatData.class);

    protected Environment dbEnv;

    public Database seqDB;
    public Database kickedPlayersDB;

    protected Sequence itemSequence;
    protected Sequence horseSequence;

    public SleepyCatData(String rootDir) throws Exception{
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setTransactional(true);
        envConfig.setAllowCreate(true);
        File dbRoot = new File(rootDir);
        dbEnv = new Environment(dbRoot, envConfig);
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setTransactional(false);
        dbConfig.setAllowCreate(true);
        dbConfig.setDeferredWrite(false);

        seqDB = dbEnv.openDatabase(null, "SequenceDB", dbConfig);

        SequenceConfig seqConfig = new SequenceConfig();
        seqConfig.setAllowCreate(true);
        seqConfig.setInitialValue(1L);

        DatabaseEntry key = new DatabaseEntry("itemid".getBytes("UTF-8"));
        itemSequence = seqDB.openSequence(null, key, seqConfig);
        DatabaseEntry key1 = new DatabaseEntry("horseid".getBytes("UTF-8"));
        horseSequence = seqDB.openSequence(null, key1, seqConfig);

        kickedPlayersDB = dbEnv.openDatabase(null, "kickedDB", dbConfig);
    }

    public int getCurrentItemId() throws Exception{
        SequenceStats stats = itemSequence.getStats(null);

        return (int) stats.getCurrent();
    }

    public void setItemId(int value) throws Exception{
        SequenceConfig seqConfig = new SequenceConfig();
        seqConfig.setAllowCreate(true);
        seqConfig.setInitialValue(1L);

        DatabaseEntry key = new DatabaseEntry("itemid".getBytes("UTF-8"));
        seqDB.delete(null, key);
        itemSequence = seqDB.openSequence(null, key, seqConfig);

        if(value > 1){
            itemSequence.get(null, value - 1);
        }

        dbEnv.sync();
    }

    public int getCurrentHorseId() throws Exception{
        SequenceStats stats = horseSequence.getStats(null);

        return (int) stats.getCurrent();
    }

    public void setHorseId(int value) throws Exception{
        SequenceConfig seqConfig = new SequenceConfig();
        seqConfig.setAllowCreate(true);
        seqConfig.setInitialValue(1L);

        DatabaseEntry key = new DatabaseEntry("horseid".getBytes("UTF-8"));
        seqDB.delete(null, key);
        horseSequence = seqDB.openSequence(null, key, seqConfig);

        if(value > 1){
            horseSequence.get(null, value - 1);
        }

        dbEnv.sync();
    }

    public void addKickedPlayer(int playerId, long time) throws Exception{
        DatabaseEntry idEntry = new DatabaseEntry();
        IntegerBinding.intToEntry(playerId, idEntry);
        DatabaseEntry timeEntry = new DatabaseEntry();
        LongBinding.longToEntry(time, timeEntry);
        kickedPlayersDB.put(null, idEntry, timeEntry);
    }

    public void deleteKickedPlayer(int playerId) throws Exception{
        DatabaseEntry idEntry = new DatabaseEntry();
        IntegerBinding.intToEntry(playerId, idEntry);
        kickedPlayersDB.delete(null, idEntry);
    }

    public List<KickedPlayer> getKickedPlayerList() throws Exception{
        List<KickedPlayer> list = new ArrayList<KickedPlayer>();

        Cursor cursor = kickedPlayersDB.openCursor(null, null);

        DatabaseEntry foundKey = new DatabaseEntry();
        DatabaseEntry foundData = new DatabaseEntry();

        while(cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS){
            Integer keyInteger = IntegerBinding.entryToInt(foundKey);
            Long dataLong = LongBinding.entryToLong(foundData);
            list.add(new KickedPlayer(keyInteger, dataLong));
        }

        return list;
    }

    public void close(){
        try{
            horseSequence.close();
        }catch(DatabaseException e1){
        }

        try{
            itemSequence.close();
        }catch(DatabaseException e1){
        }

        try{
            seqDB.close();
        }catch(DatabaseException e1){
        }

        try{
            kickedPlayersDB.close();
        }catch(DatabaseException e1){
        }

        try{
            dbEnv.close();
        }catch(Exception e){
        }
    }
}
