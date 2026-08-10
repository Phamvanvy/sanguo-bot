package com.pip.uieditor.model.text;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.pip.uieditor.model.text.Document.Line;
import com.pip.util.SWTUtils;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipImage;
import com.pipimage.image.PipImageDraw;


public class AnimateView extends View {
	
	public AnimateView(Element element, Line line, int width, int height) {
		super(element, line, width, height);
	}

	@Override
	public void draw(DrawContext context) {
		AnimateElement el = (AnimateElement)getElement();
		PipAnimateSet animateSet = el.getAnimate();
		PipAnimate animate = animateSet.getAnimate(el.getAnimateData().getIndex());
		Rectangle rect = animate.getBounds();
		int realw = (int)(rect.width * el.getScale());
		int realh = (int)(rect.height * el.getScale());
		
		// 动画画到一个图片上，再绘制到屏幕
		Image bufferImg = new Image(Display.getCurrent(), rect.width, rect.height);
		GC bufferGC = new GC(bufferImg);
		bufferGC.setClipping(0, 0, rect.width, rect.height);
		animate.drawAnimateFrame(bufferGC, (int)((System.currentTimeMillis() / 40) % Integer.MAX_VALUE), -rect.x, -rect.y, 1.0f, null);
		bufferGC.dispose();
		context.gc.setClip(new org.eclipse.draw2d.geometry.Rectangle(0, 0, 960, 640));
		context.gc.drawImage(bufferImg, 0, 0, rect.width, rect.height, context.x + el.getHgap(), context.y - realh, realw, realh);
		bufferImg.dispose();
	}

}
