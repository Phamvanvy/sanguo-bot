package com.pipimage.image;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import com.pip.mango.jni.GLGraphics;

public class PipAnimateFrameRef {
	protected PipAnimate parent;
	protected int frame;
	protected int dx;
	protected int dy;
	protected int delay;
	public boolean enableTransform = false;   // 是否开启渐变
	public int speedFactor = 100;   // 如果开启渐变，变换过程中最后一跳速度和第一条速度的比值，单位是0.01
	// 旋转角度-32768-32767
	public int rotate = 0;
	// X方向缩放比例（单位1%）
	public int scalex = 100;
	// Y方向缩放比例（单位1%）
	public int scaley = 100;
	// 颜色变换
	public int color = 0xFFFFFFFF;
	
	public PipAnimateFrameRef(PipAnimate parent) {
		this.parent = parent;
	}
	
	public PipAnimateFrameRef(PipAnimate parent, int ff) {
		this.parent = parent;
		frame = ff;
	}

	public PipAnimate getParent() {
		return parent;
	}
	
	public void setParent(PipAnimate parent) {
		this.parent = parent;
	}
	
	public int getFrame() {
		return frame;
	}
	
	public void setFrame(int frame) {
		this.frame = frame;
	}
	
	public int getDx() {
		return dx;
	}
	
	public void setDx(int dx) {
		this.dx = dx;
	}
	
	public int getDy() {
		return dy;
	}
	
	public void setDy(int dy) {
		this.dy = dy;
	}
	
	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public void load(DataInputStream dis) throws IOException {
		int frameHead = 0;
		if (parent.parent.getVersion() == 3) {
			frameHead = dis.readByte() & 0xFF;
		}
		int i = dis.readInt();
		frame = (i >> 24) & 0xFF;
		frame = (frameHead << 8) | frame;
		dx = (i >> 14) & 0x3FF;
		if (dx > 511) {
			dx -= 1024;
		}
		dy = (i >> 4) & 0x3FF;
		if (dy > 511) {
			dy -= 1024;
		}
		delay = i & 0x0F;
	}
	
	public void save(DataOutputStream dos) throws IOException {
		if (parent.parent.getVersion() == 3){
			dos.writeByte(frame >> 8);
		} else if (frame > 255) {
			throw new IOException("动画序列中引用的动画帧索引不能超过255。");
		}
		if (Math.abs(dx) > 511 || Math.abs(dy) > 511) {
			throw new IOException("动画序列中帧偏移量的绝对值不能大于511。");
		}
//		if (!enableTransform && delay > 15) {
//			throw new IOException("停顿帧数不能大于15。");
//		}
		int i = 0;
		i |= frame<<24;
		i |= (dx & 0x3FF) << 14;
		i |= (dy & 0x3FF) << 4;
		i |= (delay & 0x0F);
		dos.writeInt(i);
	}
	
	public PipAnimateFrame realize() {
		return parent.getParent().getFrame(frame);
	}
	
	public void draw(GC g, int x, int y, double ratio, ImageDrawCache cache) {
		PipAnimateFrame f = realize();
    	int drawx = (int)(dx * ratio + x);
    	int drawy = (int)(dy * ratio + y);
    	f.draw(g, drawx, drawy, ratio, cache);
	}
	
	public void draw(GLGraphics g, int x, int y, double ratio, ImageDrawCache cache) {
		PipAnimateFrame f = realize();
    	int drawx = (int)(dx * ratio + x);
    	int drawy = (int)(dy * ratio + y);
    	f.draw(g, drawx, drawy, ratio, cache, rotate, scalex, scaley, color);
	}
	
	public void enlarge() {
	    dx <<= 1;
	    dy <<= 1;
	}
	
	public void smaller() {
	    dx >>= 1;
	    dy >>= 1;
	}
	
	public void update(PipAnimateFrameRef other) {
		frame = other.frame;
		dx = other.dx;
		dy = other.dy;
		delay = other.delay;
		enableTransform = other.enableTransform;
		speedFactor = other.speedFactor;
		rotate = other.rotate;
		scalex = other.scalex;
		scaley = other.scaley;
		color = other.color;
	}
}
