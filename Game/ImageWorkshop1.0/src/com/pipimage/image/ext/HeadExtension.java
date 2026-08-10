package com.pipimage.image.ext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.pipimage.image.PipAnimateSetExtension;

/**
 * 动画头像描述。
 * @author lighthu
 */
public class HeadExtension implements PipAnimateSetExtension {
	public int headAnimateIndex;
	public int headX;
	public int headY;
	public int headWidth;
	public int headHeight;
	
	/**
	 * 取得扩展信息类型（4字符字符串）。
	 * @return
	 */
	public String getTypeID() {
		return "HEAD";
	}
	
	/**
	 * 转换为字节表示。
	 * @return
	 */
	public byte[] toByteArray() throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeByte(headAnimateIndex);
		dos.writeShort(headX);
		dos.writeShort(headY);
		dos.writeByte(headWidth);
		dos.writeByte(headHeight);
		dos.flush();
		dos.close();
		return bos.toByteArray();
	}
	
	/**
	 * 从字节读取。
	 */
	public void fromByteArray(byte[] data) throws Exception {
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
		headAnimateIndex = dis.readByte() & 0xFF;
		headX = dis.readShort();
		headY = dis.readShort();
		headWidth = dis.readByte() & 0xFF;
		headHeight = dis.readByte() & 0xFF;
	}
}
