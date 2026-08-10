package com.pip.mango.jni;

/**
 * 对应C++对象ParticleSystemManager
 * @author light.hu
 */
public class ParticleSystemManager {
	public long handle;

	public ParticleSystemManager() {
		create();
	}
	
	public long getHandle() {
		return handle;
	}
	
	// 创建C对象
	public native void create();
	// 摧毁C对象
	public native void destroy();
	// 更新时间轴
	public native void update(float interval);
	// 加载一个psdata文件
	public native void loadTemplates(String file);
	// 取得顶级模板列表（顶级模板，是没有被任何其他模板作为child引用的模板）
	// 返回模板名称的列表，以,分隔。
	public native String getRootNames();
	// 取得某一个模板包含的子系统列表
	public native String getMoNames(String name);
}
