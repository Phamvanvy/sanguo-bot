package com.pipimage.data;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import org.eclipse.swt.graphics.Image;

/**
 * 图片描述文件结构。".s"。
 */
public class ImageDescription{
    public static final byte VERSION_1 = 1;

    public static final byte VERSION_2 = 2;

    public static final byte VERSION_3 = 3;
    
    public static final byte VERSION_4 = 4;

    public static final byte T_NONE = 0;

    public static final byte T_HORIZONTAL = 2;

    public static final byte T_VERTICAL = 1;

    public static final byte T_BOTH = 3;

    // 缩略图调色板
    public static final int[] palette = new int[]{
                    0x000000, 0x808080, 0xC0C0C0, 0xFFFFFF, 0xFF0000, 0xFFFF00, 0x00FF00, 0x00FFFF, 0x0000FF, 0xFF00FF, 0xFFFF80, 0x00FF80, 0x80FFFF, 0x8080FF, 0xFF0080, 0xFF8040
    };

    public static final int[] paletteClr = new int[]{
                    0x000000, 0x808080, 0xC0C0C0, 0xFFFFFF, 0xFF0000, 0xFFFF00, 0x00FF00, 0x00FFFF,
                    0x0000FF, 0xFF00FF, 0xFFFF80, 0x00FF80, 0x80FFFF, 0x8080FF, 0xFF0080, 0xFF8040
    };

    public byte type;

    // Version 1
    public int tileWidth;

    public int tileHeight;

    public ArrayList<TileInfo1> tileList1 = new ArrayList<TileInfo1>();

    // Version 2
    public ArrayList<TileInfo2> tileList2 = new ArrayList<TileInfo2>();

    //version3
    public ArrayList<TileInfo3> tileList3 = new ArrayList<TileInfo3>();

    public int getTileCount(){
        if(type == VERSION_1){
            return tileList1.size();
        }else if(type == VERSION_2 || type == VERSION_4){
            return tileList2.size();
        }else if(type == VERSION_3){
            return tileList3.size();
        }else
            return 0;
    }

    public void newTileInfoVersion3(byte id, byte param, byte parentid){
        TileInfo3 info = new TileInfo3();
        info.imageID = id;
        info.param = param;
        info.parentID = parentid;
        tileList3.add(info);
    }

    public void load(File file) throws IOException{
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(file);
            DataInputStream dis = new DataInputStream(new BufferedInputStream(fis));
            load(dis);
        }catch(IOException e){
            throw e;
        }finally{
            try{
                fis.close();
            }catch(IOException e){
            }
        }
    }

    public void save(File file) throws IOException{
        FileOutputStream fos = null;
        try{
            fos = new FileOutputStream(file);
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos));
            save(dos);
            dos.flush();
        }catch(IOException e){
            throw e;
        }finally{
            try{
                fos.close();
            }catch(IOException e){
            }
        }
    }

    public void load(DataInputStream dis) throws IOException{
        tileList1.clear();
        tileList2.clear();
        tileList3.clear();
        type = dis.readByte();
        if(type == VERSION_1){
            int tileCount = dis.readByte() & 0xFF;
            tileWidth = dis.readByte() & 0xFF;
            tileHeight = dis.readByte() & 0xFF;
            for(int i = 0; i < tileCount; i++){
                TileInfo1 info = new TileInfo1();
                info.imageID = dis.readByte();
                info.param = dis.readByte();
                tileList1.add(info);
            }
        }else if(type == VERSION_2 || type == VERSION_4){
            int tileCount;
            if (type == VERSION_2) {
            	tileCount = dis.readByte() & 0xFF;
            } else {
            	tileCount = dis.readShort() & 0xFFFF;
            }
            for(int i = 0; i < tileCount; i++){
                TileInfo2 info = new TileInfo2();
                if (type == VERSION_2) {
	                info.x = dis.readByte() & 0xFF;
	                info.y = dis.readByte() & 0xFF;
	                info.width = dis.readByte() & 0xFF;
	                info.height = dis.readByte() & 0xFF;
                } else {
	                info.x = dis.readShort() & 0xFFFF;
	                info.y = dis.readShort() & 0xFFFF;
	                info.width = dis.readShort() & 0xFFFF;
	                info.height = dis.readShort() & 0xFFFF;
                }
                info.param = dis.readByte();
                info.collision = dis.readByte();

                if((info.param & 0x80) != 0){
                    info.alwaysOnTop = true;
                    info.param &= 0x7f;
                }

                if(info.collision == 1){
                    info.collX = dis.readByte() & 0xFF;
                    info.collY = dis.readByte() & 0xFF;
                    info.collWidth = dis.readByte() & 0xFF;
                    info.collHeight = dis.readByte() & 0xFF;
                }
                tileList2.add(info);
            }
        }else if(type == VERSION_3){
            int tileCount = dis.readByte() & 0xFF;
            tileWidth = dis.readByte() & 0xFF;
            tileHeight = dis.readByte() & 0xFF;
            for(int i = 0; i < tileCount; i++){
                TileInfo3 info = new TileInfo3();
                info.imageID = dis.readByte();
                info.param = dis.readByte();
                info.parentID = dis.readByte();
                tileList3.add(info);
            }
        }else{
            throw new IOException("非法描述文件！");
        }
    }

    public void save(DataOutputStream dos) throws IOException{
        dos.writeByte(type);
        if(type == VERSION_1){
            dos.writeByte(tileList1.size());
            dos.writeByte(tileWidth);
            dos.writeByte(tileHeight);
            for(int i = 0; i < tileList1.size(); i++){
                TileInfo1 info = (TileInfo1)tileList1.get(i);
                dos.writeByte(info.imageID);
                dos.writeByte(info.param);
            }
        }else if(type == VERSION_2 || type == VERSION_4){
        	if (type == VERSION_2) {
        		dos.writeByte(tileList2.size());
        	} else {
        		dos.writeShort(tileList2.size());
        	}
            for(int i = 0; i < tileList2.size(); i++){
                TileInfo2 info = (TileInfo2)tileList2.get(i);
                if (type == VERSION_2) {
                    dos.writeByte(info.x);
                    dos.writeByte(info.y);
                    dos.writeByte(info.width);
                    dos.writeByte(info.height);
                } else {
                    dos.writeShort(info.x);
                    dos.writeShort(info.y);
                    dos.writeShort(info.width);
                    dos.writeShort(info.height);
                }
                dos.writeByte(info.param | (info.alwaysOnTop? 0x80: 0));
                dos.writeByte(info.collision);
                if(info.collision == 1){
                    dos.writeByte(info.collX);
                    dos.writeByte(info.collY);
                    dos.writeByte(info.collWidth);
                    dos.writeByte(info.collHeight);
                }
            }
        }else if(type == VERSION_3){
            dos.writeByte(tileList3.size());
            dos.writeByte(tileWidth);
            dos.writeByte(tileHeight);
            for(int i = 0; i < tileList3.size(); i++){
                TileInfo3 info = (TileInfo3)tileList3.get(i);
                dos.writeByte(info.imageID);
                dos.writeByte(info.param);
                dos.writeByte(info.parentID);
            }
        }else{
            throw new IOException("非法描述文件！");
        }
    }

    public void convertToVersion2(Image imgSrc){
        if(type == VERSION_1){
            for(int i = 0; i < tileList1.size(); i++){
                TileInfo1 info1 = (TileInfo1)tileList1.get(i);
                TileInfo2 info = new TileInfo2();
                int id = info1.imageID & 0xFF;
                int tw = imgSrc.getBounds().width / tileWidth;
                int tx = id % tw;
                int ty = id / tw;
                info.x = tx * tileWidth;
                info.y = ty * tileHeight;
                info.width = tileWidth;
                info.height = tileHeight;
                info.param = info1.param;
                tileList2.add(info);
            }
            tileList1.clear();
            type = VERSION_2;
        }
    }

    public Object[] getTileList(){
        if(type == VERSION_1){
            return tileList1.toArray();
        }else if(type == VERSION_2 || type == VERSION_4){
            return tileList2.toArray();
        }else if(type == VERSION_3){
            return tileList3.toArray();
        }else{
            return tileList2.toArray();
        }
    }

    public static byte makeParam(byte cross, byte thumbColor, byte horiFlip, byte vertFlip){
        byte ret = (byte)((cross << 7) | (thumbColor << 3) | horiFlip | vertFlip);
        return ret;
    }
}
