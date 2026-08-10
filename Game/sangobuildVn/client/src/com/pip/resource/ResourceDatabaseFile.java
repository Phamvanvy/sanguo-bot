package com.pip.resource;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
/*
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
*/

import com.pip.common.Tool;


public class ResourceDatabaseFile extends ResourceDatabase{
    private Tool dbIdKey = new Tool();
    private Hashtable cache = new Hashtable();
    private byte[] infoCache;

    private static final String DATABASE_FILE_NAME = "file:///sanguo_res.db";

    public ResourceDatabaseFile(byte dbCount, int dbLimit, String preFix, boolean supportDeleteFile){
        super(dbCount, dbLimit, preFix, supportDeleteFile);

        for(int i = 0; i < dbCount; i++){
            cache.put(dbNames[i], new Hashtable());
        }
    }

    protected void deleteFileData(String dbName, int recordId){
        Hashtable db = (Hashtable)cache.get(dbName);
        db.remove(new Integer(recordId));
    }

    protected void deleteWholeFileData(String dbName){
        Hashtable db = (Hashtable)cache.get(dbName);
        db.clear();
    }

    protected byte[] readFileData(String dbName, int recordId){
        Hashtable db = (Hashtable)cache.get(dbName);
        return (byte[])db.get(new Integer(recordId));
    }

    protected int saveFileData(String dbName, byte[] data){
        int recordId = dbIdKey.nextKey();
        Hashtable db = (Hashtable)cache.get(dbName);
        db.put(new Integer(recordId), data);

        return recordId;
    }

    protected byte[] readInfoData(String dbInfoName){
        return infoCache;
    }

    protected void saveInfoData(String dbInfoName, byte[] data){
        infoCache = data;
    }

    protected void loadWholeDatabase(){
        /*
        FileConnection conn = null;

        try{
            conn = (FileConnection)Connector.open(DATABASE_FILE_NAME, Connector.READ);

            if(conn.exists()){
                InputStream is = conn.openInputStream();
                DataInputStream dis = new DataInputStream(is);

                try{
                    //读取信息库数据
                    int infoSize = dis.readInt();
                    infoCache = new byte[infoSize];
                    dis.read(infoCache);

                    //读取cache数据
                    int dbCount = dis.readInt();

                    for(int i = 0; i < dbCount; i++){
                        String dbName = Tool.readUTF(dis);
                        int fileCount = dis.readInt();
                        Hashtable db = (Hashtable)cache.get(dbName);

                        for(int j = 0; j < fileCount; j++){
                            int recordId = dis.readInt();
                            int dataSize = dis.readInt();
                            byte[] data = new byte[dataSize];
                            dis.read(data);

                            db.put(new Integer(recordId), data);
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
        }catch(Exception e){
            //#ifdef buildtest
            e.printStackTrace();
            //#endif
        }finally{
            try{
                if(conn != null){
                    conn.close();
                }
            }catch(Exception e){
            }
        }
        */
    }

    protected void saveWholeDatabase(){
        /*
        FileConnection conn = null;

        try{
            conn = (FileConnection)Connector.open(DATABASE_FILE_NAME, Connector.WRITE);

            if(!conn.exists()){
                conn.create();
            }

            OutputStream os = conn.openOutputStream();
            DataOutputStream dos = new DataOutputStream(os);

            try{
                //保存信息库数据
                dos.writeInt(infoCache.length);
                dos.write(infoCache);

                //保存cache数据
                dos.writeInt(cache.size());

                Enumeration emu1 = cache.keys();

                while(emu1.hasMoreElements()){
                    String dbName = (String)emu1.nextElement();
                    Hashtable db = (Hashtable)cache.get(dbName);

                    dos.writeUTF(dbName);
                    dos.writeInt(db.size());
                    Enumeration emu2 = db.keys();

                    while(emu2.hasMoreElements()){
                        Integer recordId = (Integer)emu2.nextElement();
                        byte[] data = (byte[])db.get(recordId);

                        dos.writeInt(recordId.intValue());
                        dos.writeInt(data.length);
                        dos.write(data);
                    }
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
        }catch(Exception e){
            //#ifdef buildtest
            e.printStackTrace();
            //#endif
        }finally{
            try{
                if(conn != null){
                    conn.close();
                }
            }catch(Exception e){
            }
        }
        */
    }
}
