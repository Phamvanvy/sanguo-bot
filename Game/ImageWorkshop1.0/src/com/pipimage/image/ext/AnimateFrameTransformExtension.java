package com.pipimage.image.ext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Map;

import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateFrameRef;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipAnimateSetExtension;

/**
 * 动画帧渐变数据。每一个动画中的某一帧，都可以指定使用渐变方式转换到下一帧。中间帧会通过插值算法自动计算。
 * @author lighthu
 */
public class AnimateFrameTransformExtension implements PipAnimateSetExtension {
	// 保存每个动画帧的渐变信息。
	// key的高16位是动画序列编号，低16位是动画序列中动画帧的编号
	// value是是否插值标志、动画帧变换速度因子、延迟帧数、旋转角度0-359、x方向缩放比例（单位1%）、y方向缩放比例（单位1%）、颜色变换
	protected Map<Integer, int[]> frameTransform = new HashMap<Integer, int[]>();
	
	protected PipAnimateSet owner;
	
	public AnimateFrameTransformExtension(PipAnimateSet owner) {
		this.owner = owner;
	}
	
	/**
	 * 取得扩展信息类型（4字符字符串）。
	 * @return
	 */
	public String getTypeID() {
		return "AFTF";
	}
	
	/**
	 * 判断是否为空。
	 */
	public boolean isEmpty() {
		return frameTransform.isEmpty();
	}
	
	public void syncData() {
		// 从动画中提取要保存的数据
		frameTransform.clear();
		
		for (int i = 0; i < owner.getAnimateCount(); i++) {
			PipAnimate pa = owner.getAnimate(i);
			for (int j = 0; j < pa.getFrameCount(); j++) {
				PipAnimateFrameRef fr = pa.getFrame(j);
				if (fr.enableTransform || fr.getDelay() > 15 || fr.rotate != 0 || fr.scalex != 100 || fr.scaley != 100 || fr.color != 0xFFFFFFFF) {
					int key = (i << 16) | j;
					frameTransform.put(key, new int[] { fr.enableTransform ? 1 : 0, fr.speedFactor, fr.getDelay(), fr.rotate, fr.scalex, fr.scaley, fr.color });
				}
			}
		}
	}
	
	/**
	 * 转换为字节表示。
	 * @return
	 */
	public byte[] toByteArray() throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		Object[] keys = frameTransform.keySet().toArray();
		dos.writeInt(keys.length);
		for (Object key : keys) {
			dos.writeInt((Integer)key);
			int[] value = frameTransform.get(key);
			dos.write(value[0]);
			dos.writeInt(value[1]);
			dos.writeInt(value[2]);
			dos.writeShort(value[3]);
			dos.writeShort(value[4]);
			dos.writeShort(value[5]);
			dos.writeInt(value[6]);
		}
		dos.flush();
		dos.close();
		return bos.toByteArray();
	}
	
	/**
	 * 从字节读取。
	 */
	public void fromByteArray(byte[] data) throws Exception {
		frameTransform.clear();
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
		int count = dis.readInt();
		
		// 兼容新旧2种格式
		if (data.length == 4 + count * 13) {
			for (int i = 0; i < count; i++) {
				frameTransform.put(dis.readInt(), new int[] { dis.read(), dis.readInt(), dis.readInt(), 0, 100, 100, 0xFFFFFFFF });
			}
		} else {
			for (int i = 0; i < count; i++) {
				frameTransform.put(dis.readInt(), new int[] { dis.read(), dis.readInt(), dis.readInt(), dis.readShort(), dis.readShort(), dis.readShort(), dis.readInt() });
			}
		}
		
		// 把载入的信息保存到动画对象里去
		for (int key : frameTransform.keySet()) {
			int animateIndex = (key >> 16) & 0xFFFF;
			int frameIndex = key & 0xFFFF;
			int[] value = frameTransform.get(key);
			PipAnimateFrameRef frame = owner.getAnimate(animateIndex).getFrame(frameIndex);
			frame.enableTransform = value[0] == 1;
			frame.speedFactor = value[1];
			frame.setDelay(value[2]);
			frame.rotate = value[3];
			frame.scalex = value[4];
			frame.scaley = value[5];
			frame.color = value[6];
		}
	}
}
