package com.pipimage.image;

import org.eclipse.swt.graphics.GC;

import com.pip.mango.jni.GLGraphics;

/*
 * 用于扩展帧绘制过程。
 */
public interface DrawFrameListener {
	public boolean beforeDrawFrame(PipAnimateSet as, PipAnimateFrame frame, GC g, int x, int y, double ratio);
	public void afterDrawFrame(PipAnimateSet as, PipAnimateFrame frame, GC g, int x, int y, double ratio);
	public boolean beforeDrawFrame(PipAnimateSet as, PipAnimateFrame frame, GLGraphics g, int x, int y, double ratio);
	public void afterDrawFrame(PipAnimateSet as, PipAnimateFrame frame, GLGraphics g, int x, int y, double ratio);
}
