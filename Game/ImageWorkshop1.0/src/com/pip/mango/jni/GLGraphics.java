package com.pip.mango.jni;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * 对应C++类CGLGraphics。
 * @author light.hu
 */
public class GLGraphics {
	public static final int H_CENTER = 1;
	public static final int V_CENTER = 2;
	public static final int TOP = 16;
	public static final int LEFT = 4;
	public static final int RIGHT = 8;
	public static final int BOTTOM = 32;

	public static final int GL_ZERO = 0;
	public static final int GL_ONE = 1;
	public static final int GL_SRC_COLOR = 0x0300;
	public static final int GL_ONE_MINUS_SRC_COLOR = 0x0301;
	public static final int GL_SRC_ALPHA = 0x0302;
	public static final int GL_ONE_MINUS_SRC_ALPHA = 0x0303;
	public static final int GL_DST_ALPHA = 0x0304;
	public static final int GL_ONE_MINUS_DST_ALPHA = 0x0305;
	public static final int GL_DST_COLOR = 0x0306;
	public static final int GL_ONE_MINUS_DST_COLOR = 0x0307;
	public static final int GL_SRC_ALPHA_SATURATE = 0x0308;
	
	public static final int GL_FUNC_ADD = 0x8006;
	public static final int GL_FUNC_SUBTRACT = 0x800A;
	public static final int GL_FUNC_REVERSE_SUBTRACT = 0x800B;
	
	// C对象的指针
	public long handle;
	
	public GLGraphics() {
		create();
	}
	
	public long getHandle() {
		return handle;
	}
	
	public void setColor(Color clr) {
		setColor(0xFF, clr.getRed(), clr.getGreen(), clr.getBlue());
	}
	
	public Point textExtent(String str) {
		return new Point(stringWidth(str), getFontHeight());
	}
	
	public void drawText(String str, int x, int y) {
		drawString(str, x, y, TOP | LEFT);
	}
	
	public void drawRect(Rectangle rect) {
		drawRect(rect.x, rect.y, rect.width, rect.height);
	}

	public void fillRect(Rectangle rect) {
		fillRect(rect.x, rect.y, rect.width, rect.height);
	}
	
	// 创建C对象
	public native void create();
	
	// 设置绘图参数
	public native void setPaintOption(boolean writeDepth, int srcBlend, int dstBlend, int equation);
	// 设置字体大小
	public native void setFont(int fontHeight);
	// 设置坐标原点偏移
	public native void translate(int x, int y);
	// 设置坐标原点偏移
	public native void translate(float x, float y, float z);
	// 获取坐标原点偏移
	public native int getTranslateX();
	// 获取坐标原点偏移
	public native int getTranslateY();
	// 设置缩放比例
	public native void setScale(float scale);
	// 获取当前缩放比例
	public native float getScale();
	// 获取是否启用alpha混合标志
	public native boolean getBlend();
	// 设置是否启用alpha混合标志
	public native void setBlend(boolean flag);
	// 获取是否启用alpha测试标志
	public native boolean getAlphaTest();
	// 设置是否启用alpha测试标志
	public native void setAlphaTest(boolean flag);
	// 获取当前绘图Z值
	public native float getZ();
	// 设置绘图Z值
	public native void setZ(float value);
	// 获取剪裁区域位置
	public native int getClipX();
	// 获取剪裁区域位置
	public native int getClipY();
	// 获取剪裁区域位置
	public native int getClipWidth();
	// 获取剪裁区域位置
	public native int getClipHeight();
	// 设置图片绘制颜色转换值
	public native void setColorFilter(int f);
	// 获取当前绘图颜色
	public native int getColor();
	// 设置绘图颜色
	public native void setColor(int alpha, int red, int green, int blue);
	// 设置绘图颜色
	public native void setColor(int rgb);
	// 设置剪裁区域
	public native void clipRect(int x, int y, int w, int h);
	// 设置剪裁区域
	public native void setClip(int x, int y, int w, int h);
	// 用当前绘图颜色填充矩形。
	public native void fillRect(int x, int y, int width, int height);
	// 用当前绘图颜色填充圆角矩形
	public native void fillRoundRect(int x, int y, int width, int height, int rx, int ry);
	// 画线
	public native void drawLine(int x1, int y1, int x2, int y2);
	// 画矩形边框
	public native void drawRect(int x, int y, int width, int height);
	// 画圆角矩形边框
	public native void drawRoundRect(int x, int y, int width, int height, int rx, int ry);
	// 绘制字符串
	public native void drawString(String str, int x, int y, int anchor);
	// 绘制3D字符串
	public native void draw3DString(String str, int x, int y, int anchor, int bkColor);
	// 计算当前字体的字符串宽度
	public native int stringWidth(String str);
	// 计算当前字体的字符串高度
	public native int getFontHeight();
	// 绘制圆弧
	public native void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle);
	// 填充圆弧
	public native void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle);
	// 填充三角形
	public native void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3);

	// 批量绘制另外一个CGLGraphics对象中缓存的所有绘图操作，带上偏离量。clip，translate和scale数据都用本GLGraphics中的当前设定。
	public native void drawBatch(long paint, float offx, float offy);
	// 绘制一个材质图片中的一帧。
	public native void drawTexture(long texture, int index, int trans, float destx, float desty, int colorTrans);
	// 绘制一个材质图片中的一帧。
	public native void drawTexture(long texture, int index, int trans, float destx, float desty);
	// 绘制一个材质图片中的一帧。
	// 注意，destw和desth是指翻转前的宽度和高度。如果trans>=4，实际绘制的宽度和高度是反过来的。
	public native void drawTexture(long texture, int index, int trans, float destx, float desty, float destw, float desth, int colorTrans);
	// 绘制一个材质图片中的一帧。
	// 注意，destw和desth是指翻转前的宽度和高度。如果trans>=4，实际绘制的宽度和高度是反过来的。
	public native void drawTexture(long texture, int index, int trans, float destx, float desty, float destw, float desth);
	// 旋转绘制一个材质图片中的一帧。
	public native void drawTextureRotate(long texture, int index, int anchorx, int anchory, int angle, float destx, float desty);
	// 根据指定的顶点绘制材质图片中的一帧。
	public native void drawTextureFree(long texture, int index, float x1, float y1, float x2, float y2, float x3, float y3,
			float x4, float y4, int color);

	// 清除缓存
	public native void clear();
	// 删除对象
	public native void dispose();
}
