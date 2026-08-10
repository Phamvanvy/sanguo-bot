package com.pip.mapeditor.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.pip.util.Utils;

/**
 * 简单格点地图。
 * @author lighthu
 */
public class CellMap {
    /** 版本号 */
    public int version = 0;
    /** 地图宽度 */
    public int width;
    /** 地图高度 */
    public int height;
    /** 格点值深度(格点的取值可以是0到depth-1) */
    public int depth;
    /** 格点数据 */
    public byte[][] data;
    
    
    /**
     * 创建一个地图对象。
     */
    public CellMap(int w, int h, int d) {
        this.width = w;
        this.height = h;
        this.depth = d;
        this.data = new byte[height][width];
    }
    
    /**
     * 改变地图的宽度。
     */
    public void setWidth(int w) {
        int mw = w < width ? w : width;
        byte[][] newdata = new byte[height][w];
        for (int y = 0; y < height; y++) {
            System.arraycopy(data[y], 0, newdata[y], 0, mw);
        }
        this.width = w;
        this.data = newdata;
    }
    
    /**
     * 改变地图的高度。
     */
    public void setHeight(int h) {
        int mh = h < height ? h : height;
        byte[][] newdata = new byte[height][];
        System.arraycopy(data, 0, newdata, 0, mh);
        this.height = h;
        this.data = newdata;
    }
    
    /**
     * 改变地图的格点深度。
     */
    public void setDepth(int d) {
        if (d > 16) {
            throw new IllegalArgumentException();
        }
        if (d < this.depth) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (data[y][x] >= d) {
                        data[y][x] = (byte)(d - 1);
                    }
                }
            }
        }
        this.depth = d;
    }
    
    /**
     * 从文件中载入。
     */
    public void load(File file) throws IOException {
        load(Utils.loadFileData(file));
    }

    /**
     * 从文件中读取。
     * @param data
     * @throws IOException
     */
    public void load(byte[] fileData) throws IOException {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(fileData));
        version = dis.readInt();
        width = dis.readInt();
        height = dis.readInt();
        depth = dis.readInt();
        data = new byte[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                data[y][x] = dis.readByte();
            }
        }
    }
    
    /**
     * 保存到文件中。
     */
    public void save(File f) throws IOException {
        Utils.saveFileData(f, save());
    }
    
    /**
     * 保存到字符数组中。
     */
    public byte[] save() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        
        dos.writeInt(version);
        dos.writeInt(width);
        dos.writeInt(height);
        dos.writeInt(depth);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                dos.writeByte(data[y][x]);
            }
        }
        
        dos.close();
        return bos.toByteArray();
    }
}
