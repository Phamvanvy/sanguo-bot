package com.pipimage.image;

import java.io.IOException;

import org.eclipse.swt.graphics.Image;

/**
 * 定义用于解析压缩的纹理图片的接口。
 * @author light.hu
 */
public interface CompressedTextureHandler {
	public Image decodeTexture(String format, byte[] textureData, int width, int height) throws IOException;
	public byte[] encodeTexture(String format, Image image) throws IOException;
}
