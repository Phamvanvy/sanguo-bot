package com.pip.mango.jni;

/**
 * 对应C++类CGLTextureManager。
 * @author light.hu
 */
public class GLTextureManager {
	// 注册一个图像数据到材质池中。返回CGLTextureWrapper对象的指针。
	public static native long registerImage(String pool, String name, int[] rgbdata, int width, int height);
	// 注销一个图像数据。
	public static native void unregisterImage(String pool, String name, long ptr);
}
