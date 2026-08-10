package com.pip.server.auth.net;

import org.apache.commons.collections.primitives.ByteList;


public class ByteListUtil {

	public static void addBoolean(ByteList list,boolean b){
		ByteListUtil.addByte(list,b?(byte)1:(byte)0);
	}

	public static void addBooleans(ByteList list,boolean[] b){
		for(int i=0;i<b.length;i++){
			ByteListUtil.addBoolean(list,b[i]);
		}
	}

	public static void addByte(ByteList list,byte b){
		list.add(b);
	}

	public static void addBytes(ByteList list,byte[] b){
		for(int i=0;i<b.length;i++){
			list.add(b[i]);
		}
	}

        public static void addBytes(ByteList list,byte[] b,int begin,int len){
            for(int i=0;i<len;i++){
                list.add(b[i+begin]);
            }
        }

	public static void addChar(ByteList list,char value){
		list.add((byte)((value>>8)&0xFF));
		list.add((byte)(value&0xFF));
	}

	public static void addChars(ByteList list,char[] value){
		for(int i=0;i<value.length;i++){
			addChar(list,value[i]);
		}
	}

	public static void addShort(ByteList list,short value){
		list.add((byte)((value>>8)&0xFF));
		list.add((byte)(value&0xFf));
	}

	public static void addShorts(ByteList list,short[] value){
		for(int i=0;i<value.length;i++){
			addShort(list,value[i]);
		}
	}

	public static void addInt(ByteList list,int value){
		list.add((byte)((value>>24)&0xFF));
		list.add((byte)((value>>16)&0xFF));
		list.add((byte)((value>>8)&0xFF));
		list.add((byte)(value&0xFF));
	}

	public static void setInt(ByteList list,int pos,int value){
		list.set(pos++,(byte)((value>>24)&0xFF));
		list.set(pos++,(byte)((value>>16)&0xFF));
		list.set(pos++,(byte)((value>>8)&0xFF));
		list.set(pos,(byte)(value&0xFF));
	}

	public static void addInts(ByteList list,int[] value){
		for(int i=0;i<value.length;i++){
			addInt(list,value[i]);
		}
	}

	public static void addLong(ByteList list,long value){
		list.add((byte)((value>>56)&0xFF));
		list.add((byte)((value>>48)&0xFF));
		list.add((byte)((value>>40)&0xFF));
		list.add((byte)((value>>32)&0xFF));
		list.add((byte)((value>>24)&0xFF));
		list.add((byte)((value>>16)&0xFF));
		list.add((byte)((value>>8)&0xFf));
		list.add((byte)(value&0xFf));
	}

	public static void addLongs(ByteList list,long[] value){
		for(int i=0;i<value.length;i++){
			addLong(list,value[i]);
		}
	}

	public static void addString(ByteList list,String str){
		int strlen = str.length();
		int utflen = 0;
	 	char[] charr = new char[strlen];
		int c;

		str.getChars(0, strlen, charr, 0);

		for (int i = 0; i < strlen; i++) {
		    c = charr[i];
		    if ((c >= 0x0001) && (c <= 0x007F)) {
			utflen++;
		    } else if (c > 0x07FF) {
			utflen += 3;
		    } else {
			utflen += 2;
		    }
		}
		list.add((byte)((utflen >>> 8) & 0xFF));
		list.add((byte)((utflen >>> 0) & 0xFF));
		for (int i = 0; i < strlen; i++) {
		    c = charr[i];
		    if ((c >= 0x0001) && (c <= 0x007F)) {
		    	list.add((byte)c);
		    } else if (c > 0x07FF) {
		    	list.add((byte) (0xE0 | ((c >> 12) & 0x0F)));
		    	list.add((byte) (0x80 | ((c >>  6) & 0x3F)));
		    	list.add((byte) (0x80 | ((c >>  0) & 0x3F)));
		    } else {
		    	list.add((byte) (0xC0 | ((c >>  6) & 0x1F)));
		    	list.add((byte) (0x80 | ((c >>  0) & 0x3F)));
		    }
		}
	}

	public static void addStrings(ByteList list,String[] str){
		for(int i=0;i<str.length;i++){
			addString(list,str[i]);
		}
	}
}

