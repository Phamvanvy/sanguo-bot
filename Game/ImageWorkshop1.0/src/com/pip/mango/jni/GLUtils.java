package com.pip.mango.jni;

import java.util.HashMap;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import com.pip.image.workshop.editor.ImageViewer;

public class GLUtils {
	// 全局标志，是否启用opengl模式
	public static boolean glEnabled = false;
	
	private static HashMap<Image, String> imageNameMap = new HashMap<Image, String>();
	private static HashMap<String, Long> nameTextureMap = new HashMap<String, Long>();
	public static String poolName = "default2";  // "default"是不平滑缩放的，"default2"是平滑缩放的
	
	/**
	 * 注册一个swt图片，返回GLTextureWapper的句柄。这个方法保证了一个图片多次注册返回同样的结果。
	 * @param img
	 * @return
	 */
	public static synchronized long loadImage(Image img) {
		if (imageNameMap.containsKey(img)) {
			return nameTextureMap.get(imageNameMap.get(img));
		}
		Rectangle rect = img.getBounds();
		int[] idata = ImageViewer.getImageData2(img, rect);
		String name = String.valueOf(img.hashCode());
		while (nameTextureMap.containsKey(name)) {
			// 名字重复了，换一个
			name = name + ".";
		}
		long texture = GLTextureManager.registerImage(poolName, name, idata, rect.width, rect.height);
		imageNameMap.put(img, name);
		nameTextureMap.put(name, texture);
		return texture;
	}
	
	/**
	 * 把一个图片从显存卸载（如果已经加载的话）并且销毁它。在整个项目中应该用这个方法来替代Image.dispose()
	 * @param img
	 */
	public static synchronized void unloadImage(Image img) {
		if (imageNameMap.containsKey(img)) {
			String name = imageNameMap.remove(img);
			long texture = nameTextureMap.remove(name);
			GLTextureManager.unregisterImage(poolName, name, texture);
		}
		img.dispose();
	}
	
	/**
	 * 清空所有缓存的材质对象。
	 */
	public static synchronized void clearTextureInfo() {
		Object[] arr = imageNameMap.keySet().toArray();
		for (int i = 0; i < arr.length; i++) {
			String name = imageNameMap.remove((Image)arr[i]);
			long texture = nameTextureMap.remove(name);
			GLTextureManager.unregisterImage(poolName, name, texture);
		}
	}
	
	/**
	 * 矩阵乘法。
	 * @param mat1
	 * @param mat2
	 * @return
	 */
	public static double[][] mul(double[][] mat1, double[][] mat2) {
		if (mat1[0].length != mat2.length) {
			throw new IllegalArgumentException();
		}
		double[][] ret = new double[mat1.length][mat1[0].length];
		for (int i = 0; i < mat1.length; i++) {
			for (int j = 0; j < mat1[i].length; j++) {
				for (int k = 0; k < mat1[i].length; k++) {
					ret[i][j] += mat1[i][k] * mat2[k][j];
				}
			}
		}
		return ret;
	}
	
	public static int getInternValue(int start, int end, double percent) {
		return (int)(start + (end - start) * percent);
	}
	
	public static int getInternColor(int start, int end, double percent) {
		int a = getInternValue((start >> 24) & 0xFF, (end >> 24) & 0xFF, percent);
		int r = getInternValue((start >> 16) & 0xFF, (end >> 16) & 0xFF, percent);
		int g = getInternValue((start >> 8) & 0xFF, (end >> 8) & 0xFF, percent);
		int b = getInternValue((start >> 0) & 0xFF, (end >> 0) & 0xFF, percent);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}
	
	public static int mulColor(int color1, int color2) {
		int a1 = (color1 >> 24) & 0xFF;
		int r1 = (color1 >> 16) & 0xFF;
		int g1 = (color1 >> 8) & 0xFF;
		int b1 = (color1 >> 0) & 0xFF;
		int a2 = (color2 >> 24) & 0xFF;
		int r2 = (color2 >> 16) & 0xFF;
		int g2 = (color2 >> 8) & 0xFF;
		int b2 = (color2 >> 0) & 0xFF;
		int a = mulComp(a1, a2);
		int r = mulComp(r1, r2);
		int g = mulComp(g1, g2);
		int b = mulComp(b1, b2);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}
	
	public static int mulComp(int v1, int v2) {
		return (v1 * v2) / 255;
	}
	
	public static double[][] rotate(double[][] mat1, int r) {
		double angle = ((r % 360) + 360) % 360;
		angle = angle * Math.PI / 180;
		double sin = Math.sin(angle);
		double cos = Math.cos(angle);
		return GLUtils.mul(mat1, new double[][] {
				{ cos, -sin, 0 },
				{ sin, cos, 0 },
				{ 0, 0, 1}
		});
	}
}
