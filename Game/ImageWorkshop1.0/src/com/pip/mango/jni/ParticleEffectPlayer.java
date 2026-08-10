package com.pip.mango.jni;

/**
 * 对应C++类ParticleEffectPlayer
 * @author light.hu
 */
public class ParticleEffectPlayer {
	public long handle;
	public ParticleSystemManager manager;

	public ParticleEffectPlayer(ParticleSystemManager mgr, String name, int x, int y, boolean dynamicPos) {
		manager = mgr;
		synchronized(manager) {
			create(mgr.getHandle(), name, x, y, dynamicPos);
		}
	}
	
	public long getHandle() {
		return handle;
	}
	
	public void stop() {
		synchronized(manager) {
			setLoop(false);
			destroy();
		}
	}
	
	// 创建C对象
	public native void create(long mgr, String name, int x, int y, boolean dynamicPos);
	// 摧毁C对象
	public native void destroy();
	// 是否播放结束
	public native boolean isEnd();
	// 绘制
	public native void draw(long g, int offx, int offy);
	// 设置位置
	public native void setPosition(int x, int y);
	// 是否循环
	public native boolean isLoop();
	// 设置循环标志
	public native void setLoop(boolean loop);
	// 设置旋转
	public native void setRotation(int degree_x, int degree_y, int degree_z);
}
