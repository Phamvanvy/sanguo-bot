package com.pipimage.image.ext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipAnimateSetExtension;

/**
 * 帧对称关系数据。
 * @author lighthu
 */
public class MirrorSettingExtension implements PipAnimateSetExtension {
	protected PipAnimateSet owner;
	// 保存双向对称关系
	protected Map<PipAnimateFrame, PipAnimateFrame> mirrorMap;
	
	public MirrorSettingExtension(PipAnimateSet owner) {
		this.owner = owner;
		mirrorMap = new HashMap<PipAnimateFrame, PipAnimateFrame>();
	}
	
	/**
	 * 查询一帧的对称帧。
	 * @param ani
	 * @return
	 */
	public PipAnimateFrame getMirror(PipAnimateFrame ani) {
		return mirrorMap.get(ani);
	}
	
	/**
	 * 设置对称关系。
	 * @param ani1
	 * @param ani2
	 */
	public void setMirror(PipAnimateFrame ani1, PipAnimateFrame ani2) {
		mirrorMap.remove(ani1);
		mirrorMap.remove(ani2);
		mirrorMap.put(ani1, ani2);
		mirrorMap.put(ani2, ani1);
	}
	
	/**
	 * 删除一帧的对称关系。
	 * @param ani
	 */
	public void removeMirror(PipAnimateFrame ani) {
		PipAnimateFrame ani2 = mirrorMap.get(ani);
		if (ani2 != null) {
			mirrorMap.remove(ani);
			mirrorMap.remove(ani2);
		}
	}
	
	/**
	 * 判断数据是否为空，如果为空不保存。
	 * @return
	 */
	public boolean isEmpty() {
		return mirrorMap.size() == 0;
	}
	
	/**
	 * 取得扩展信息类型（4字符字符串）。
	 * @return
	 */
	public String getTypeID() {
		return "MIRR";
	}
	
	/**
	 * 转换为字节表示。
	 * @return
	 */
	public byte[] toByteArray() throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeInt(mirrorMap.size() / 2);
		Object[] objs = mirrorMap.keySet().toArray();
		Set<PipAnimateFrame> written = new HashSet<PipAnimateFrame>();
		for (Object obj : objs) {
			PipAnimateFrame pa = (PipAnimateFrame)obj;
			if (written.contains(pa)) {
				continue;
			}
			PipAnimateFrame pa2 = mirrorMap.get(pa);
			dos.writeShort(owner.findFrame(pa));
			dos.writeShort(owner.findFrame(pa2));
			written.add(pa);
			written.add(pa2);
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
			int ind1 = dis.readShort();
			int ind2 = dis.readShort();
			setMirror(owner.getFrame(ind1), owner.getFrame(ind2));
		}
	}
}
