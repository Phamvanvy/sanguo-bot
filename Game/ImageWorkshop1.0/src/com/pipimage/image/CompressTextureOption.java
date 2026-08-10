package com.pipimage.image;

import java.io.*;

/**
 * 定义压缩纹理格式。
 * @author light.hu
 */
public class CompressTextureOption {
	public static final String PVRTC_4BPP = "pvrtc4";
	public static final String PVRTC_4BPP2 = "pvrtc42";
	public static final String ETC1 = "etc1";
	public static final String ETC2 = "etc2";
	
	public String format;
	public int borderWidth;
	public int sizeWidth;
	public int sizeHeight;
	
	public CompressTextureOption(String f, int sizeWidth, int sizeHeight) {
		this(f);
		this.sizeWidth = sizeWidth;
		this.sizeHeight = sizeHeight;
	}
	
	public CompressTextureOption(String f) {
		format = f;
	}
	
	public void load(DataInputStream dis) throws IOException {
		borderWidth = dis.readByte() & 0xFF;
	}
	
	public void save(DataOutputStream dos) throws IOException {
		dos.write(borderWidth);
	}
}
