package com.pipimage.image.ext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipAnimateSetExtension;

/**
 * 挂接点描述。
 * @author lighthu
 */
public class HookPointExtension implements PipAnimateSetExtension {
	protected PipAnimateSet owner;
	
	public static class Position {
		public int x;
		public int y;
		public int direction;    // 0-360表示先画，1000-1360表示后画
	}

	public static class HookPoint {
		public String name;
		public HashMap<PipAnimateFrame, Position> posList = new HashMap<PipAnimateFrame, Position>();
	}
	
	public List<HookPoint> hooks = new ArrayList<HookPoint>();
	
	public HookPointExtension(PipAnimateSet owner) {
		this.owner = owner;
	}
	
	public boolean isEmpty() {
		return hooks.size() == 0;
	}
	
	public HookPoint findHookPoint(String name) {
		for (HookPoint hp : hooks) {
			if (hp.name.equals(name)) {
				return hp;
			}
		}
		return null;
	}
	
	/**
	 * 取得扩展信息类型（4字符字符串）。
	 * @return
	 */
	public String getTypeID() {
		return "HOOK";
	}
	
	/**
	 * 转换为字节表示。
	 * @return
	 */
	public byte[] toByteArray() throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeByte(hooks.size());
		for (HookPoint hook : hooks) {
			dos.writeUTF(hook.name);
			dos.writeShort(owner.getFrameCount());
			for (int i = 0; i < owner.getFrameCount(); i++) {
				PipAnimateFrame paf = owner.getFrame(i);
				Position pos = hook.posList.get(paf);
				if (pos == null) {
					dos.writeByte(0);
				} else {
					dos.writeByte(1);
					dos.writeShort(pos.x);
					dos.writeShort(pos.y);
					dos.writeShort(pos.direction);
				}
			}
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
		int count = dis.readByte() & 0xFF;
		hooks.clear();
		for (int i = 0; i < count; i++) {
			HookPoint hook = new HookPoint();
			hook.name = dis.readUTF();
			int fcount = dis.readShort() & 0xFFFF;
			for (int j = 0; j < fcount; j++) {
				byte b = dis.readByte();
				if (b == (byte)1) {
					Position pos = new Position();
					pos.x = dis.readShort();
					pos.y = dis.readShort();
					pos.direction = dis.readShort();
					if (j < owner.getFrameCount()) {
						hook.posList.put(owner.getFrame(j), pos);
					}
				}
			}
			hooks.add(hook);
		}
	}
}
