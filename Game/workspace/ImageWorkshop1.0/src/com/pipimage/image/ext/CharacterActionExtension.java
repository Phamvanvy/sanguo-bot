package com.pipimage.image.ext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.pipimage.image.PipAnimateSetExtension;

/**
 * 动作描述。
 * @author lighthu
 */
public class CharacterActionExtension implements PipAnimateSetExtension {
	public List<String> actionNames = new ArrayList<String>();
	public List<Integer> actionAnimate = new ArrayList<Integer>();
	
	/**
	 * 取得扩展信息类型（4字符字符串）。
	 * @return
	 */
	public String getTypeID() {
		return "CACT";
	}
	
	/**
	 * 转换为字节表示。
	 * @return
	 */
	public byte[] toByteArray() throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeByte(actionNames.size());
		for (int i = 0; i < actionNames.size(); i++) {
			for (int j = 0; j < 4; j++) {
				dos.writeByte(actionNames.get(i).charAt(j));
			}
			dos.writeByte(actionAnimate.get(i));
		}
		dos.flush();
		dos.close();
		return bos.toByteArray();
	}
	
	/**
	 * 从字节读取。
	 */
	public void fromByteArray(byte[] data) throws Exception {
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
		actionNames.clear();
		actionAnimate.clear();
		int size = dis.readByte() & 0xFF;
		for (int i = 0; i < size; i++) {
			char[] arr = new char[4];
			for (int j = 0; j < 4; j++) {
				arr[j] = (char)(dis.readByte() & 0xFF);
			}
			int index = dis.readByte() & 0xFF;
			actionNames.add(new String(arr));
			actionAnimate.add(index);
		}
	}
}
