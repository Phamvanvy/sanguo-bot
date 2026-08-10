package com.pip.resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.pip.common.Tool;
import com.pip.io.UASegment;
import com.pip.sanguo.GameMain;

/**
 * 资源数据库基类
 * 实现数据库信息的管理和数据处理规则
 * 规则：
 *      数据库为多个，添加数据时优先选取序号最小且空间足够的库
 *      当同一名字的数据被重复添加时，视为更新旧数据，如果支持删除文件操作则删掉旧文件，如果不支持则进行简单废弃使数据库使用大小和有效大小不等
 *      当数据库全满后，再添加文件则查找一个空间浪费最严重的库，整体删除，并丢弃所有文件信息
 * @author leo
 *
 */
public abstract class ResourceDatabase{
    /**
     * 数据库数量
     */
    private byte dbCount;
    /**
     * 单个库的上限
     */
    private int dbLimit;

    /**
     * 文件查找表
     */
    private Hashtable fileTable;
    /**
     * 数据库名称表
     */
    protected String[] dbNames;
    /**
     * 信息库名称
     */
    private String dbInfoName;
    /**
     * 数据库使用大小表
     */
    private int[] dbSavedSizes;
    /**
     * 数据库有效大小表
     */
    private int[] dbRealSize;

    /**
     * 是否支持删除单个文件
     */
    private boolean supportDeleteFile;

    /**
     * 信息库是否有变化
     */
    private boolean dirty;

    /**
     * 创建并初始化数据库
     * @param dbCount
     * @param dbLimit
     * @param preFix
     */
    public ResourceDatabase(byte dbCount, int dbLimit, String preFix, boolean supportDeleteFile){
        this.dbCount = dbCount;
        this.dbLimit = dbLimit;
        this.supportDeleteFile = supportDeleteFile;
        dirty = false;

        fileTable = new Hashtable();
        dbNames = new String[dbCount];
        dbInfoName = preFix + "info";
        dbSavedSizes = new int[dbCount];
        dbRealSize = new int[dbCount];

        int len = String.valueOf(dbCount).length();

        for(int i = 0; i < dbCount; i++){
            dbNames[i] = preFix;
            dbSavedSizes[i] = 0;
            dbRealSize[i] = 0;

            String tmp = "0000" + (i + 1);
            dbNames[i] += tmp.substring(tmp.length() - len);
        }
    }

    public synchronized void clearDatabase(){
        Tool.deleteRMSFile(dbInfoName);

        for(int i = 0; i < dbCount; i++){
            Tool.deleteRMSFile(dbNames[i]);
        }
    }

    /**
     * 读取信息库
     */
    public synchronized boolean loadInformation(){
        //清除内存数据
        boolean ret = false;
        fileTable.clear();

        for(int i = 0; i < dbCount; i++){
            dbSavedSizes[i] = 0;
            dbRealSize[i] = 0;
        }

        loadWholeDatabase();

        //读取信息数据
        byte[] infoData = readInfoData(dbInfoName);

        if(infoData != null){
            int infoLen = (int) UASegment.getNumber(infoData, 0, 4);
            if(infoLen != infoData.length){
                //#ifdef buildtest
                System.out.println("dbInfo info error! clear database: " + infoLen + " , " + infoData.length);
                //#endif

                clearDatabase();
            }else{
                //#ifdef buildtest
                System.out.println("dbInfo Loaded : " + dbInfoName + " , " + infoData.length);
                //#endif

                ByteArrayInputStream bis = new ByteArrayInputStream(infoData, 4, infoData.length - 4);
                DataInputStream dis = new DataInputStream(bis);

                try{
                    String savedClientVersion = Tool.readUTF(dis);

                    if(!GameMain.getClientVersion().equals(savedClientVersion)){ //客户端升级，强制清除所有缓存
                        //#ifdef buildtest
                        System.out.println("new version client! clear database: " + infoLen + " , " + infoData.length);
                        //#endif

                        Tool.deleteRMSFile(dbInfoName);

                        for(int i = 0; i < dbCount; i++){
                            Tool.deleteRMSFile(dbNames[i]);
                        }
                        ret = true;
                    }else{
                        //读取数据库信息
                        for(int i = 0; i < dbCount; i++){
                            dbSavedSizes[i] = dis.readInt();
                            dbRealSize[i] = dis.readInt();

                            //#ifdef buildtest
                            System.out.println("Database Loaded : " + dbNames[i] + " , " + dbSavedSizes[i] + " , " + dbRealSize[i]);
                            //#endif
                        }

                        //读取file table
                        int fileCount = dis.readInt();

                        for(int i = 0; i < fileCount; i++){
                            DatabaseFile dbFile = new DatabaseFile();
                            dbFile.name = Tool.readUTF(dis);
                            dbFile.dbId = dis.readByte();
                            dbFile.recordId = dis.readInt();
                            dbFile.version = dis.readInt();
                            fileTable.put(dbFile.name, dbFile);
                        }
                    }
                }catch(Exception e){
                    //#ifdef buildtest
                    e.printStackTrace();
                    //#endif
                }finally{
                    try{
                        if(dis != null){
                            dis.close();
                        }
                    }catch(Exception e){
                    }
                }
            }
        }

        dirty = false;

        return ret;
    }

    /**
     * 保存信息库
     */
    public synchronized void saveInformation(boolean saveData){
        if(!dirty){
            return;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        try{
            //写入客户端版本信息
            dos.writeUTF(GameMain.getClientVersion());

            //写入数据库信息
            for(int i = 0; i < dbCount; i++){
                dos.writeInt(dbSavedSizes[i]);
                dos.writeInt(dbRealSize[i]);

                //#ifdef buildtest
                System.out.println("Database Saved : " + dbNames[i] + " , " + dbSavedSizes[i] + " , " + dbRealSize[i]);
                //#endif
            }

            //写入file table
            dos.writeInt(fileTable.size());
            Enumeration emu = fileTable.elements();

            while(emu.hasMoreElements()){
                DatabaseFile dbFile = (DatabaseFile) emu.nextElement();
                dos.writeUTF(dbFile.name);
                dos.writeByte(dbFile.dbId);
                dos.writeInt(dbFile.recordId);
                dos.writeInt(dbFile.version);
            }

            byte[] infoData = bos.toByteArray();
            byte[] infoData1 = new byte[infoData.length + 4];
            System.arraycopy(infoData, 0, infoData1, 4, infoData.length);
            infoData = null;
            UASegment.setNumber(infoData1.length, infoData1, 0, 4);

            saveInfoData(dbInfoName, infoData1);

            //#ifdef buildtest
            System.out.println("dbInfo Saved : " + dbInfoName + " , " + infoData1.length);
            //#endif

            if(saveData){
                saveWholeDatabase();
            }
        }catch(Exception e){
            //#ifdef buildtest
            e.printStackTrace();
            //#endif
        }finally{
            try{
                if(dos != null){
                    dos.close();
                }
            }catch(Exception e){
            }
        }

        dirty = false;
    }

    /**
     * 保存文件
     * @param name
     * @param data
     */
    public synchronized void saveFile(String name, byte[] data, int version){
        if(data.length > dbLimit){
            return;
        }

        DatabaseFile dbFile = (DatabaseFile) fileTable.get(name);

        if(dbFile != null){
            dbRealSize[dbFile.dbId] -= data.length;

            if(supportDeleteFile){
                dbSavedSizes[dbFile.dbId] -= data.length;
                deleteFileData(dbNames[dbFile.dbId], dbFile.recordId);
            }
        }else{
            dbFile = new DatabaseFile();
            dbFile.name = name;
        }

        dbFile.dbId = findNextDb(data.length);
        dbFile.version = version;
        dbFile.needUpdate = false;

        if(dbFile.dbId < 0){
            dbFile.dbId = findWasteDb();
            deleteWholeFileData(dbNames[dbFile.dbId]);
            clearDbFile(dbFile.dbId);
        }

        dbSavedSizes[dbFile.dbId] += data.length;
        dbRealSize[dbFile.dbId] += data.length;
        dbFile.recordId = saveFileData(dbNames[dbFile.dbId], data);

        if(dbFile.recordId >= 0){
            fileTable.put(name, dbFile);
        }else{
            fileTable.remove(name);
        }

        dirty = true;
    }

    /**
     * 读取文件
     * @param name
     * @return
     */
    public synchronized byte[] loadFile(String name){
        byte[] result = null;
        DatabaseFile dbFile = (DatabaseFile) fileTable.get(name);

        if(dbFile != null && !dbFile.needUpdate){
            result = readFileData(dbNames[dbFile.dbId], dbFile.recordId);
        }

        if(result == null){
            result = GameMain.resourceAsynLoader.getResource(name);
        }

        return result;
    }

    public Vector getFileNameList(){
        Vector result = new Vector();
        Enumeration emu = fileTable.keys();

        while(emu.hasMoreElements()){
            String name = (String) emu.nextElement();
            result.addElement(name);
        }

        return result;
    }

    public int getFileVersion(String name){
        DatabaseFile dbFile = (DatabaseFile) fileTable.get(name);

        if(dbFile != null){
            return dbFile.version;
        }else{
            return 0;
        }
    }

    public void updateFile(String name){
        DatabaseFile dbFile = (DatabaseFile) fileTable.get(name);

        if(dbFile != null){
            dbFile.needUpdate = true;
        }
    }
    
    public boolean hasFile(String name){
        DatabaseFile dbFile = (DatabaseFile) fileTable.get(name);

        if(dbFile != null){
            return true;
        }
        
        return false;
    }

    /**
     * 查找下一个可用数据库
     */
    private byte findNextDb(int size){
        byte result = -1;

        for(int i = 0; i < dbCount; i++){
            if(dbSavedSizes[i] + size <= dbLimit){
                result = (byte) i;
                break;
            }
        }

        return result;
    }

    /**
     * 超找空间浪费最严重的一个数据库
     * @return
     */
    private byte findWasteDb(){
        byte dbId = 0;
        int maxWasteSize = 0;

        for(int i = 0; i < dbCount; i++){
            if(dbSavedSizes[i] - dbRealSize[i] > maxWasteSize){
                maxWasteSize = dbSavedSizes[i] - dbRealSize[i];
                dbId = (byte) i;
            }
        }

        return dbId;
    }

    /**
     * 清除文件查找表中所有保存位置为指定dbId的文件
     * @param dbId
     */
    private void clearDbFile(int dbId){
        Hashtable restTable = new Hashtable();
        Enumeration emu = fileTable.elements();

        while(emu.hasMoreElements()){
            DatabaseFile dbFile = (DatabaseFile) emu.nextElement();

            if(dbFile.dbId != dbId){
                restTable.put(dbFile.name, dbFile);
            }
        }

        fileTable = restTable;
        dbSavedSizes[dbId] = 0;
        dbRealSize[dbId] = 0;
    }

    public boolean isDirty(){
        return dirty;
    }

    /**
     * 清除数据库中的所有数据
     */
    public void truncateDatabase(){
        Tool.deleteRMSFile(dbInfoName);

        for(int i = 0; i < dbCount; i++){
            Tool.deleteRMSFile(dbNames[i]);
        }

        loadInformation();
    }

    protected abstract byte[] readInfoData(String dbInfoName);

    protected abstract void saveInfoData(String dbInfoName, byte[] data);

    protected abstract byte[] readFileData(String dbName, int recordId);

    protected abstract int saveFileData(String dbName, byte[] data);

    protected abstract void deleteFileData(String dbName, int recordId);

    protected abstract void deleteWholeFileData(String dbName);

    protected abstract void loadWholeDatabase();

    protected abstract void saveWholeDatabase();
}

class DatabaseFile{
    String name;
    byte dbId;
    int recordId;
    int version;
    boolean needUpdate;
}
