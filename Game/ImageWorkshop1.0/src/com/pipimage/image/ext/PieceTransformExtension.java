package com.pipimage.image.ext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateFramePiece;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipAnimateSetExtension;
import com.pipimage.image.PipImage;
import com.pipimage.image.PipImageData;

/**
 * 动画图块变形数据。每一帧中的任何一个动画图块，都可以进行旋转和缩放操作，旋转和缩放的参数统一保存在这个extension中。
 * @author lighthu
 */
public class PieceTransformExtension implements PipAnimateSetExtension {
	// 保存每个图块的旋转参考点（相对于这个图块本身）
	// key的高16位是pip图片在动画中的索引，低16位是图片内的帧索引
	// value的高16位是旋转参考点X位置，低16位是Y位置（相对于图片左上角）
	protected Map<Integer, Integer> imagePieceAnchor = new HashMap<Integer, Integer>();
	
	// 保存帧中图块的旋转和缩放信息
	// key的高16位是帧序号，低16位是帧中的图块序号
	// value的4个值依次是：旋转角度0-359、x方向缩放比例（单位1%）、y方向缩放比例（单位1%）、颜色变换
	protected Map<Integer, int[]> pieceTransform = new HashMap<Integer, int[]>();
	
	protected PipAnimateSet owner;
	
	public PieceTransformExtension(PipAnimateSet owner) {
		this.owner = owner;
	}
	
	/**
	 * 取得扩展信息类型（4字符字符串）。
	 * @return
	 */
	public String getTypeID() {
		return "PTFM";
	}
	
	/**
	 * 判断是否为空。
	 */
	public boolean isEmpty() {
		return imagePieceAnchor.isEmpty() && pieceTransform.isEmpty();
	}
	
	public void syncData() {
		// 从动画中提取要保存的数据
		imagePieceAnchor.clear();
		pieceTransform.clear();
		
		// 提取图片帧的anchor点数据
		for (int imgIndex = 0; imgIndex < owner.getFileCount(); imgIndex++) {
			PipImage pimg = owner.getSourceImage(imgIndex);
			for (int f = 0; f < pimg.getImgCount(); f++) {
				PipImageData id = pimg.getImageData(f);
				if (id.anchorx != 0 || id.anchory != 0) {
					int key = (imgIndex << 16) | f;
					imagePieceAnchor.put(key, (id.anchorx << 16) | (id.anchory & 0xFFFF));
				}
			}
		}
		// 提取旋转、缩放、变色数据
		for (int frameIndex = 0; frameIndex < owner.getFrameCount(); frameIndex++) {
			PipAnimateFrame frame = owner.getFrame(frameIndex);
			for (int pieceIndex = 0; pieceIndex < frame.getPieceCount(); pieceIndex++) {
				PipAnimateFramePiece piece = frame.getPiece(pieceIndex);
				if (piece.rotate != 0 || piece.scalex != 100 || piece.scaley != 100 || piece.color != 0xFFFFFFFF) {
					int key = (frameIndex << 16) | pieceIndex;
					pieceTransform.put(key, new int[] { piece.rotate, piece.scalex, piece.scaley, piece.color });
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
		Object[] keys = imagePieceAnchor.keySet().toArray();
		dos.writeInt(keys.length);
		for (Object key : keys) {
			dos.writeInt((Integer)key);
			dos.writeInt(imagePieceAnchor.get(key));
		}
		keys = pieceTransform.keySet().toArray();
		dos.writeInt(keys.length);
		for (Object key : keys) {
			dos.writeInt((Integer)key);
			int[] value = pieceTransform.get(key);
			dos.writeShort(value[0]);
			dos.writeShort(value[1]);
			dos.writeShort(value[2]);
			dos.writeInt(value[3]);
		}
		dos.flush();
		dos.close();
		return bos.toByteArray();
	}
	
	/**
	 * 从字节读取。
	 */
	public void fromByteArray(byte[] data) throws Exception {
		imagePieceAnchor.clear();
		pieceTransform.clear();
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
		int count = dis.readInt();
		for (int i = 0; i < count; i++) {
			imagePieceAnchor.put(dis.readInt(), dis.readInt());
		}
		count = dis.readInt();
		for (int i = 0; i < count; i++) {
			int key = dis.readInt();
			int[] value = new int[4];
			value[0] = dis.readShort();
			value[1] = dis.readShort();
			value[2] = dis.readShort();
			value[3] = dis.readInt();
			pieceTransform.put(key, value);
		}
		
		// 把载入的信息保存到动画对象里去
		for (int key : imagePieceAnchor.keySet()) {
			int pipIndex = (key >> 16) & 0xFFFF;
			int frameIndex = key & 0xFFFF;
			int value = imagePieceAnchor.get(key);
			int anchorx = (short)(value >> 16);
			int anchory = (short)value;
			owner.getSourceImage(pipIndex).getImageData(frameIndex).anchorx = anchorx;
			owner.getSourceImage(pipIndex).getImageData(frameIndex).anchory = anchory;
		}
		for (int key : pieceTransform.keySet()) {
			int frameIndex = (key >> 16) & 0xFFFF;
			int pieceIndex = key & 0xFFFF;
			int[] value = pieceTransform.get(key);
			owner.getFrame(frameIndex).getPiece(pieceIndex).rotate = value[0];
			owner.getFrame(frameIndex).getPiece(pieceIndex).scalex = value[1];
			owner.getFrame(frameIndex).getPiece(pieceIndex).scaley = value[2];
			owner.getFrame(frameIndex).getPiece(pieceIndex).color = value[3];
		}
	}
}
