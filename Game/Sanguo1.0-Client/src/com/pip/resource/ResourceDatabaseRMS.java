package com.pip.resource;

import com.pip.common.Tool;

public class ResourceDatabaseRMS extends ResourceDatabase{
    public ResourceDatabaseRMS(byte dbCount, int dbLimit, String preFix, boolean supportDeleteFile){
        super(dbCount, dbLimit, preFix, supportDeleteFile);
    }

    protected void deleteFileData(String dbName, int recordId){
        Tool.deleteRecord(dbName, recordId);
    }

    protected void deleteWholeFileData(String dbName){
        Tool.deleteRMSFile(dbName);
    }

    protected byte[] readFileData(String dbName, int recordId){
        return Tool.loadRecord(dbName, recordId);
    }
    
    protected int saveFileData(String dbName, byte[] data){
        return Tool.addRecord(dbName, data);
    }

    protected byte[] readInfoData(String dbInfoName){
        return Tool.getData(dbInfoName, (byte)0);
    }

    protected void saveInfoData(String dbInfoName, byte[] data){
        Tool.saveData(dbInfoName, data, (byte)0);
    }

    protected void loadWholeDatabase(){
    }
    
    protected void saveWholeDatabase(){
    }
}
