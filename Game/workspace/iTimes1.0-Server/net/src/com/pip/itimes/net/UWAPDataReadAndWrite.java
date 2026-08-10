package com.pip.itimes.net;

import org.apache.commons.collections.primitives.ArrayByteList;
import org.apache.commons.collections.primitives.ByteList;

/**
 * @author wpjiang
 *用于模拟一条消息，拓展了一下写的功能
 */
public class UWAPDataReadAndWrite extends UWAPData{
	
    
    private  static ByteList buffer;
    private  static byte numOfParameter2 = (byte) 0;
    
	public UWAPDataReadAndWrite(byte[] data, int serial, int sessionId,
			boolean needUncompress, int version) {
		super(data, serial, sessionId, needUncompress, version);
		// TODO Auto-generated constructor stub
	}
	public static void makeUWApDataReadAndWrite(byte type) {
		buffer = new ArrayByteList(128);
        ByteListUtil.addByte(buffer, type);
        ByteListUtil.addInt(buffer, 6);
        ByteListUtil.addByte(buffer, (byte) 0);
    }
	
	public static  void writeString(String insertString){
		ByteListUtil.addByte(buffer, (byte) 0x07);
        ByteListUtil.addString(buffer, insertString);
        setSize();
        numOfParameter2++;
        setNumOfParameter();
	}
	
	public static void writeByte(byte insertByte){
	        ByteListUtil.addByte(buffer, (byte) 0x02);
	        ByteListUtil.addByte(buffer, insertByte);
	        setSize();
	        numOfParameter2++;
	        setNumOfParameter();
		
	}
	 protected static  void setSize() {
		 ByteListUtil.setInt(buffer, 1, buffer.size());
	 }
	 
	 protected  static void setNumOfParameter() {
	        buffer.set(5, numOfParameter2);
	 }
	 
	 public static  byte[] getByteArray() {
        
		 return buffer.toArray();
	}
}
