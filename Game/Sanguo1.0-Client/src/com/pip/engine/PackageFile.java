package com.pip.engine;

//#if NewUI2
import java.io.BufferedInputStream;
//#endif
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Hashtable;

import com.pip.common.Tool;


/**
 * 打包文件类。
 */
public class PackageFile{
    /** 包裹名字 */
    public String name;
    /** 版本号 */
    public int version;
    /** 包含的文件名 */
    public String[] fileNames;
    /** 包含的文件内容 */
    public byte[][] fileContents;
    /** 文件名到文件索引的对照表 */
    protected Hashtable nameIndexMap;

    /**
     * 构造一个空的文件。
     */
    public PackageFile(){
        name = "";
        fileNames = new String[0];
        fileContents = new byte[0][];
        nameIndexMap = new Hashtable();
    }

    /**
     * 载入一个打包文件。
     * @param data
     * @throws IOException
     */
    public PackageFile(byte[] data) throws IOException{
    	//#if NewUI2
    	DataInputStream in = new DataInputStream(new BufferedInputStream(new ByteArrayInputStream(data)));
    	//#else
        //# DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
        //#endif
        name = Tool.readUTF(in); // name
        version = in.readInt(); // version
        int fileCount = in.readShort();
        fileNames = new String[fileCount];
        nameIndexMap = new Hashtable();
        for(int i = 0; i < fileCount; i++){
            fileNames[i] = Tool.readUTF(in);
            nameIndexMap.put("/" + fileNames[i], new Integer(i));
        }
        fileContents = new byte[fileCount][];
        for(int i = 0; i < fileCount; i++){
            int length = in.readInt();
            byte[] fileData = new byte[length];
            in.readFully(fileData);
            fileContents[i] = fileData;
        }
    }

    /**
     * 根据名字查找文件。
     * @param name
     * @return
     */
    public byte[] getFile(String name){
        Integer index = (Integer)nameIndexMap.get(name);
        if(index == null){
            return null;
        }else{
            return fileContents[index.intValue()];
        }
    }

    /**
     * 释放一个文件的空间。
     * @param name
     */
    public void releaseFile(String name){
        Integer index = (Integer)nameIndexMap.get(name);
        if(index != null){
            fileContents[index.intValue()] = null;
        }
    }

    /**
     * 添加一个文件。
     * @param name
     * @param data
     */
    public void addFile(String name, byte[] data){
        int fileCount = fileNames.length;

        String[] newNames = new String[fileCount + 1];
        System.arraycopy(fileNames, 0, newNames, 0, fileCount);
        newNames[fileCount] = name;
        fileNames = newNames;
        nameIndexMap.put(name, new Integer(fileCount));

        byte[][] newContents = new byte[fileCount + 1][];
        System.arraycopy(fileContents, 0, newContents, 0, fileCount);
        newContents[fileCount] = data;
        fileContents = newContents;
    }
}
