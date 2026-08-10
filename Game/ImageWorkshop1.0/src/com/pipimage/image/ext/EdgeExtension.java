package com.pipimage.image.ext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.pipimage.image.PipAnimateSetExtension;

/**
 * 动画边界描述。
 * @author lighthu
 */
public class EdgeExtension implements PipAnimateSetExtension {
	public static class Edge {
		// 起始动画序列（包含），-1表示不限制
		public int beginAnimateIndex;
		// 结束动画序列（包含），-1表示不限制
		public int endAnimateIndex;
		// 边界起始Y位置
		public int beginY;
		// 边界覆盖范围高度
		public int height;
		// 每一行的边界起始X（包含）
		public int[] beginX;
		// 每一行的边界结束X（不包含）
		public int[] endX;
	}
	public List<Edge> edges = new ArrayList<Edge>();
	
	/**
	 * 取得扩展信息类型（4字符字符串）。
	 * @return
	 */
	public String getTypeID() {
		return "EDGE";
	}
	
	/**
	 * 转换为字节表示。
	 * @return
	 */
	public byte[] toByteArray() throws Exception {
		// 检查需要用单字节还是双字节格式
		boolean mustUseTwoBytes = false;
		for (Edge edge : edges) {
			if (edge.beginY < -128 || edge.beginY > 127 || edge.height > 255) {
				mustUseTwoBytes = true;
				break;
			}
			for (int i = 0; i < edge.height; i++) {
				if (edge.beginX[i] < -128 || edge.beginX[i] > 127 || edge.endX[i] < -128 || edge.endX[i] > 127) {
					mustUseTwoBytes = true;
					break;
				}
			}
			if (mustUseTwoBytes) {
				break;
			}
		}
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeByte(edges.size());
		dos.writeByte(mustUseTwoBytes ? 2 : 1);
		for (Edge edge : edges) {
			dos.writeByte(edge.beginAnimateIndex);
			dos.writeByte(edge.endAnimateIndex);
			if (mustUseTwoBytes) {
				dos.writeShort(edge.beginY);
				dos.writeShort(edge.height);
			} else {
				dos.writeByte(edge.beginY);
				dos.writeByte(edge.height);
			}
			for (int i = 0; i < edge.height; i++) {
				if (mustUseTwoBytes) {
					dos.writeShort(edge.beginX[i]);
					dos.writeShort(edge.endX[i]);
				} else {
					dos.writeByte(edge.beginX[i]);
					dos.writeByte(edge.endX[i]);
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
		int byteCount = dis.readByte();
		edges.clear();
		for (int i = 0; i < count; i++) {
			Edge edge = new Edge();
			edge.beginAnimateIndex = dis.readByte();
			edge.endAnimateIndex = dis.readByte();
			if (byteCount == 2) {
				edge.beginY = dis.readShort();
				edge.height = dis.readShort() & 0xFFFF;
			} else {
				edge.beginY = dis.readByte();
				edge.height = dis.readByte() & 0xFF;
			}
			edge.beginX = new int[edge.height];
			edge.endX = new int[edge.height];
			for (int j = 0; j < edge.height; j++) {
				if (byteCount == 2) {
					edge.beginX[j] = dis.readShort();
					edge.endX[j] = dis.readShort();
				} else {
					edge.beginX[j] = dis.readByte();
					edge.endX[j] = dis.readByte();
				}
			}
			edges.add(edge);
		}
	}
}
