package com.pip.mango.jni;

/**
 * 实现在一个windows窗口中的opengl环境。
 * @author light.hu
 */
public class GLWindow {
	// C对象的指针
	public long handle;
	
	/**
	 * 在指定窗口中创建opengl环境。
	 * @param hwnd
	 */
	public GLWindow(int hwnd) {
		create(hwnd);
	}
	
	/**
	 * 在指定窗口中创建opengl环境（Linux环境下，一个handle是long类型）。
	 * @param hwnd
	 */
	public GLWindow(long hwnd) {
		create((int)hwnd);
	}
	
	/**
	 * 摧毁opengl环境。
	 */
	public void dispose() {
		int remain = disposeImpl();
		if (remain == 0) {
			// 如果最后一个窗口被关闭，材质会自动删除，所以需要清空缓存
			GLUtils.clearTextureInfo();
		}
	}

	/**
	 * 把缓存的一组绘图操作绘制到屏幕上。
	 * @param g
	 */
	public void draw(GLGraphics g) {
		draw(g.getHandle());
	}
	
	private native int disposeImpl();
	private native void draw(long g);
	private native void create(int hwnd);
}
