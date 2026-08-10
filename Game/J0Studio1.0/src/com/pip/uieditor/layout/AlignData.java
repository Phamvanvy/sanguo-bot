package com.pip.uieditor.layout;

import org.eclipse.core.runtime.Assert;
import org.eclipse.draw2d.geometry.Rectangle;

public class AlignData {
	
	public Align hAlign;
	public Align vAlign;
	
	public AlignData(Align hAlign, Align vAlign) {
		this.hAlign = hAlign;
		this.vAlign = vAlign;
	}
	
	public Rectangle getBounds(int width, int height) {
		Assert.isNotNull(hAlign);
		Assert.isNotNull(vAlign);
		int x = 0;
		int y = 0;
		int w = 0;
		int h = 0;
		if(hAlign.align == Align.LEFT_CENTER) {
			x = hAlign.getPixel1(width);
			w = (hAlign.getPixel2(width) - x) * 2;
		}
		if(hAlign.align == Align.CENTER_RIGHT) {
			int right = hAlign.getPixel2(width);
			int center = hAlign.getPixel1(width);
			w = (right - center) * 2;
			x = right -w ;
		}
		if(hAlign.align == Align.LEFT_RIGHT) {
			x = hAlign.getPixel1(width);
			w = hAlign.getPixel2(width) - x;
		}
		if(vAlign.align == Align.TOP_MIDDLE) {
			y = vAlign.getPixel1(height);
			h = (vAlign.getPixel2(height) - y) * 2;
		}
		if(vAlign.align == Align.MIDDLE_BOTTOM) {
			h = (vAlign.getPixel2(height) - vAlign.getPixel1(height)) * 2;
			y = vAlign.getPixel2(height) - h;
		}
		if(vAlign.align == Align.TOP_BOTTOM) {
			y = vAlign.getPixel1(height);
			h  =vAlign.getPixel2(height) - y;
		}
		return new Rectangle(x, y, w, h);
	}
}
