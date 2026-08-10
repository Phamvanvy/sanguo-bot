package com.pipimage.image;


import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import com.pipimage.utils.GZIP;


public class PipImageData{

    private static final byte[] HEAD = {
                    'D', 'A', 'T', 'A'
    };
    
    private static final byte[] HEAD_NONZIP = {
                    'D', 'U', 'N', 'Z'
    };

    /**
     * 翻转信息 
     * 0 不翻转，数据使用data
     * 1 垂直翻转
     * 2 水平翻转
     * 3 水平+垂直翻转
     */
    public byte flip;

    public byte frame;

    public short width;
    public short height;

    public int[] data;

//    private int x;
//    private int y;
    private byte[] collision;
    
    public int anchorx = 0;
    public int anchory = 0;

    public byte getFlip(){
        return flip;
    }

    public void setFlip(byte flip){
        this.flip = flip;
    }

    public short getWidth(){
        return width;
    }

    public void setWidth(short width){
        this.width = width;
    }

    public short getHeight(){
        return height;
    }

    public void setHeight(short height){
        this.height = height;
    }

    public int[] getData(){
        return data;
    }

    public void setData(int[] data){
        this.data = data;
    }

    public byte getFrame(){
        return frame;
    }

    public void setFrame(byte frame){
        this.frame = frame;
    }

//    public int getX(){
//        return x;
//    }
//
//    public void setX(int x){
//        this.x = x;
//    }
//
//    public int getY(){
//        return y;
//    }
//
//    public void setY(int y){
//        this.y = y;
//    }

    public byte[] getCollision(){
        return collision;
    }

    public void setCollision(byte[] collision){
        this.collision = collision;
    }

    public int[] make(PipImagePalette palette){
    	if (palette == null) {
    		return data;
    	}
        int w = width;
        int h = height;
        int[] rgb = new int[w * h];
        
        int id = 0;
        for(int y = 0; y < h; y++){
            for(int x = 0; x < w; x++){
                id = y * w + x;
                rgb[id] = palette.getColor(data[id] & 0xffff);
            }
        }
        return rgb;
    }

    public void read(DataInputStream dis, boolean twoBytes, boolean trueColor) throws IOException{
        int len = dis.readInt() - 6;

        byte[] head = new byte[4];
        dis.read(head);

        flip = dis.readByte();
        frame = dis.readByte();
        width = dis.readShort();
        height = dis.readShort();

        byte f = dis.readByte();
        if(f != 0){
            collision = new byte[4];
            dis.readFully(collision);
        }else{
            collision = null;
        }

        byte[] zdata = new byte[len];
        dis.read(zdata);
        if (isCompress(head))
            zdata = GZIP.inflate(zdata);
        if (trueColor) {
        	data = new int[zdata.length / 4];
        	for (int i = 0; i < zdata.length / 4; i++) {
        		int b1 = zdata[i * 4];
        		int b2 = zdata[i * 4 + 1];
        		int b3 = zdata[i * 4 + 2];
        		int b4 = zdata[i * 4 + 3];
        		data[i] = ((b1 & 0xFF) << 24) | ((b2 & 0xFF) << 16) | ((b3 & 0xFF) << 8) | (b4 & 0xFF);
        	}
        } else if (twoBytes) {
            data = new int[zdata.length / 2];
            for (int i = 0; i < zdata.length / 2; i++) {
                int b1 = zdata[i * 2];
                int b2 = zdata[i * 2 + 1];
                data[i] = ((b1 & 0xFF) << 8) | (b2 & 0xFF);
            }
        } else {
            data = new int[zdata.length];
            for (int i = 0; i < zdata.length; i++) {
                data[i] = zdata[i] & 0xFF;
            }
        }
    }

    public void save(DataOutputStream dos, boolean compress, boolean twoBytes, boolean trueColor) throws IOException{
        byte[] zdata;
        if (trueColor) {
        	zdata = new byte[data.length * 4];
        	for (int i = 0; i < data.length; i++) {
        		zdata[i * 4] = (byte)(data[i] >> 24);
        		zdata[i * 4 + 1] = (byte)(data[i] >> 16);
        		zdata[i * 4 + 2] = (byte)(data[i] >> 8);
        		zdata[i * 4 + 3] = (byte)data[i];
        	}
        } else if (twoBytes) {
            zdata = new byte[data.length * 2];
            for (int i = 0; i < data.length; i++) {
                zdata[i * 2] = (byte)(data[i] >> 8);
                zdata[i * 2 + 1] = (byte)data[i];
            }
        } else {
            zdata = new byte[data.length];
            for (int i = 0; i < data.length; i++) {
                zdata[i] = (byte)data[i];
            }
        }
        if (compress) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream zos = new GZIPOutputStream(bos);
            DataOutputStream zdos = new DataOutputStream(zos);
            zdos.write(zdata);
            zdos.close();
            zdata = bos.toByteArray();
        }

        dos.writeInt(zdata.length + 6);
        if(compress){
            dos.write(HEAD);
        }else{
            dos.write(HEAD_NONZIP);
        }
        dos.writeByte(flip);
        dos.writeByte(frame);
        dos.writeShort(width);
        dos.writeShort(height);

        if(collision == null)
            dos.writeByte(0);
        else{
            dos.writeByte(1);
            dos.write(collision);
        }
        dos.write(zdata);

    }

    public String toString(){
        return "第" + frame + "帧";
    }

    public static boolean isCompress(byte[] head){
        if(head[1] == 'A'){
            return true;
        }else if(head[1] == 'U'){
            return false;
        }
        return true;
    }

    public void hflip() {
    	int pos = 0;
    	for (int i = 0; i < height; i++) {
    		int start = pos;
    		int end = pos + width - 1;
    		while (start < end) {
    			int t = data[start];
    			data[start] = data[end];
    			data[end] = t;
    			start++;
    			end--;
    		}
    		pos += width;
    	}
    }
    
    public void vflip() {
    	int[] tmp = new int[width];
    	int start = 0;
    	int end = height - 1;
    	while (start < end) {
    		System.arraycopy(data, start * width, tmp, 0, width);
    		System.arraycopy(data, end * width, data, start * width, width);
    		System.arraycopy(tmp, 0, data, end * width, width);
    		start++;
    		end--;
    	}
    }
    
    public PipImageData duplicate() {
    	PipImageData ret = new PipImageData();
    	ret.width = width;
    	ret.height = height;
    	ret.data = new int[data.length];
    	System.arraycopy(data, 0, ret.data, 0, data.length);
    	ret.anchorx = anchorx;
    	ret.anchory = anchory;
    	return ret;
    }
    
    /**
     * 放大一倍。
     */
    public void enlarge() {
    	int[] newdata = new int[width * height * 4];
        for (int y = 0; y < height; y++) {
            int lineHead1 = y * width;
            int lineHead2 = y * width * 4;
            for (int x = 0; x < width; x++) {
                newdata[lineHead2 + x * 2] = data[lineHead1 + x];
                newdata[lineHead2 + x * 2 + 1] = data[lineHead1 + x];
            }
            System.arraycopy(newdata, lineHead2, newdata, lineHead2 + width * 2, width * 2);
        }
        data = newdata;
        width *= 2;
        height *= 2;
        if (collision != null) {
            for (int i = 0; i < collision.length; i++) {
                collision[i] <<= 1;
            }
        }
    }
    
    /**
     * 缩小一倍。
     */
    public void smaller() {
    	int newWidth = (width + 1) / 2;
    	int newHeight = (height + 1) / 2;
    	int[] newdata = new int[newWidth * newHeight];
    	for (int y = 0; y < newHeight; y++) {
    		int lineHead1;
    		if (y * 2 >= height) {
    			lineHead1 = (height - 1) * width;
    		} else {
    			lineHead1 = y * 2 * width;
    		}
    		int lineHead2 = y * newWidth;
    		for (int x = 0; x < newWidth; x++) {
    			if (x * 2 >= width) {
    				newdata[lineHead2 + x] = data[lineHead1 + width - 1];
    			} else {
    				newdata[lineHead2 + x] = data[lineHead1 + x * 2];
    			}
    		}
    	}
        data = newdata;
        width = (short)newWidth;
        height = (short)newHeight;
        if (collision != null) {
            for (int i = 0; i < collision.length; i++) {
                collision[i] >>= 1;
            }
        }
    }
    
    public PipImageData getPart(int x, int y, int w, int h) {
    	PipImageData ret = new PipImageData();
    	ret.flip = flip;
    	ret.frame = frame;
    	ret.width = (short)w;
    	ret.height = (short)h;
    	ret.data = new int[w * h];
    	for (int y1 = 0; y1 < h; y1++) {
    		System.arraycopy(data, (y + y1) * width + x, ret.data, y1 * w, w);
    	}
    	return ret;
    }
}
