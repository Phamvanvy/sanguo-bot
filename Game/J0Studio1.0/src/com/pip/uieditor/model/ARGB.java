package com.pip.uieditor.model;

import org.eclipse.swt.graphics.RGB;

public class ARGB {
	public int alpha;
	
	public int red;
	
	public int green;
	
	public int blue;
	
	public ARGB(int alpha, int red, int green, int blue) {
		if (alpha < 0 || alpha > 255 || red < 0 || red > 255 || green < 0
				|| green > 255 || blue < 0 || blue > 255)
			throw new IllegalArgumentException();
		this.alpha = alpha;
		this.red = red;
		this.green = green;
		this.blue = blue;
	}
	
	public ARGB(int color) {
		this.alpha = (color >> 24) & 0xFF;
		this.red = (color >> 16) & 0xFF;
		this.green = (color >> 8) & 0xFF;
		this.blue = (color) & 0xFF;
	}
	
	public RGB getRGB() {
		return new RGB(red, green, blue);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof ARGB))
			return false;
		ARGB argb = (ARGB) o;
		return argb.alpha == alpha && argb.red == red && argb.green == green
				&& argb.blue == blue;
	}
	
	public String toString() {
		return "ARGB{" + alpha + ", " + red + ", " + green +", " + blue +"}";
	}
	
	public ARGB getCopy() {
		return new ARGB(this.alpha, this.red, this.green, this.blue);
	}
	
	public int toInt() {
		return this.alpha << 24 | this.red << 16 | this.green << 8 | this.blue; 
	}
}
