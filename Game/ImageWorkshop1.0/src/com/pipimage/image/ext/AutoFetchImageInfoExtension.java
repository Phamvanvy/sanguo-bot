package com.pipimage.image.ext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipAnimateSetExtension;

/**
 * 记录自动填充帧时，每个图块和原始图片文件的对应关系。
 * @author lighthu
 */
public class AutoFetchImageInfoExtension implements PipAnimateSetExtension {
	protected PipAnimateSet owner;
	// key的格式是：pip文件名_帧ID，value是原始图片文件名。
	protected Map<String, String> sourceFileMap;
	
	public AutoFetchImageInfoExtension(PipAnimateSet owner) {
		this.owner = owner;
		sourceFileMap = new HashMap<String, String>();
	}
	
	/**
	 * 取得一个PIP图片中一个图块的原始文件名。
	 * @param pipName
	 * @param frameID
	 * @return 如果没有原始文件，返回null。 
	 */
	public String getSourceFile(String pipName, int frameID) {
		return sourceFileMap.get(pipName + "_" + frameID);
	}
	
	/**
	 * 设置一个PIP图片中一个图块的原始文件名。
	 * @param pipName
	 * @param frameID
	 * @param sourceFile
	 */
	public void setSourceFile(String pipName, int frameID, String sourceFile) {
		sourceFileMap.put(pipName + "_" + frameID, sourceFile);
	}
	
	/**
	 * 删除一个PIP图片的所有记录。
	 * @param pipName
	 */
	public void removePipFile(String pipName) {
		Object[] arr = sourceFileMap.keySet().toArray();
		for (Object obj : arr) {
			String name = (String)obj;
			if (name.startsWith(pipName + "_")) {
				sourceFileMap.remove(name);
			}
		}
	}
	
	/**
	 * 重命名一个PIP图片的所有记录。
	 * @param pipName
	 */
	public void renamePipFile(String oldName, String newName) {
		Object[] arr = sourceFileMap.keySet().toArray();
		for (Object obj : arr) {
			String key = (String)obj;
			if (key.startsWith(oldName + "_")) {
				String value = sourceFileMap.remove(key);
				String newKey = newName + key.substring(oldName.length());
				sourceFileMap.put(newKey, value);
			}
		}
	}
	
	/**
	 * PIP内容改变后，根据帧序号对应关系表修改数据。
	 * @param pipName
	 * @param frameMap
	 */
	public void adjustByFrameMap(String pipName, Map<Integer, Integer> frameMap) {
		Object[] arr = sourceFileMap.keySet().toArray();
		List<String> tempKeyList = new ArrayList<String>();
		List<String> tempValueList = new ArrayList<String>();
		for (Object obj : arr) {
			String name = (String)obj;
			if (name.startsWith(pipName + "_")) {
				int id = Integer.parseInt(name.substring(pipName.length() + 1));
				String value = sourceFileMap.remove(name);
				if (frameMap.containsKey(id)) {
					tempKeyList.add(pipName + "_" + frameMap.get(id));
					tempValueList.add(value);
				}
			}
		}
		for (int i = 0; i < tempKeyList.size(); i++) {
			sourceFileMap.put(tempKeyList.get(i), tempValueList.get(i));
		}
	}
	
	/**
	 * PIP内容改变后，根据帧序号对应关系表修改数据。
	 * @param pipName
	 * @param frameMap
	 */
	public void adjustByFrameMap(Map<Integer, Integer> frameMap) {
		for (int key : frameMap.keySet()) {
			int value = frameMap.get(key);
			int srcImgIdx = key >> 16;
			String srcImgName = owner.getFileName(srcImgIdx);
			int srcFrame = key & 0xFFFF;
			int tgtImgIdx = value >> 16;
			String tgtImgName = owner.getFileName(tgtImgIdx);
			int tgtFrame = value & 0xFFFF;
			String srcKey = srcImgName + "_" + srcFrame;
			String tgtKey = tgtImgName + "_" + tgtFrame;
			if (sourceFileMap.containsKey(srcKey)) {
				sourceFileMap.put(tgtKey, sourceFileMap.get(srcKey));
				sourceFileMap.remove(srcKey);
			}
		}
	}
	
	/**
	 * 一个PIP的帧被改变后，修改对应关系表。
	 * @param pipName
	 * @param frame
	 * @param splitCount
	 */
	public void onImageSplit(String pipName, int frame, int splitCount) {
		Object[] arr = sourceFileMap.keySet().toArray();
		List<String> tempKeyList = new ArrayList<String>();
		List<String> tempValueList = new ArrayList<String>();
		for (Object obj : arr) {
			String name = (String)obj;
			if (name.startsWith(pipName + "_")) {
				int id = Integer.parseInt(name.substring(pipName.length() + 1));
				if (id > frame) {
					String value = sourceFileMap.remove(name);
					tempKeyList.add(pipName + "_" + (id + splitCount));
					tempValueList.add(value);
				}
			}
		}
		for (int i = 0; i < tempKeyList.size(); i++) {
			sourceFileMap.put(tempKeyList.get(i), tempValueList.get(i));
		}
	}
	
	public boolean isEmpty() {
		return sourceFileMap.isEmpty();
	}
	
	/**
	 * 取得扩展信息类型（4字符字符串）。
	 * @return
	 */
	public String getTypeID() {
		return "AFII";
	}
	
	/**
	 * 转换为字节表示。
	 * @return
	 */
	public byte[] toByteArray() throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeInt(sourceFileMap.size());
		for (String src : sourceFileMap.keySet()) {
			String dest = sourceFileMap.get(src);
			dos.writeUTF(src);
			dos.writeUTF(dest);
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
		int count = dis.readInt();
		for (int i = 0; i < count; i++) {
			String src = dis.readUTF();
			String dest = dis.readUTF();
			sourceFileMap.put(src, dest);
		}
	}
}
