package com.pip.uieditor.model;

import org.eclipse.draw2d.geometry.Point;

public class AnchorPoint implements Anchor {
	
	private int anchor,relativeAnchor;
	private Point offset;
	
	public AnchorPoint(int anchor, int relativeAnchor) {
		this(anchor, relativeAnchor, null);
	}
	
	public AnchorPoint(int anchor, int relativeAnchor, Point offset) {
		this.anchor = anchor;
		this.relativeAnchor = relativeAnchor;
		this.offset = offset;
		if(this.offset == null) {
			this.offset = new Point(0,0);
		}
	}
	
	public void setAnchor(int anchor) {
		this.anchor = anchor;
	}
	
	public int getAnchor() {
		return anchor;
	}
	
	public void setRelativeAnchor(byte relativeAnchor) {
		this.relativeAnchor = relativeAnchor;
	}
	
	public int getRelativeAnchor() {
		return this.relativeAnchor;
	}
	
	public Point getOffset() {
		return offset;
	}
	
	public void setOffset(Point offset) {
		if(offset == null)
			throw new IllegalArgumentException();
		this.offset = offset;
	}
	
	public int getOffsetX() {
		if(offset == null)
			return 0;
		return this.offset.x;
	}
	
	public int getOffsetY() {
		if(offset == null)
			return 0;
		return this.offset.y;
	}
	
	public AnchorPoint getCopy() {
		return new AnchorPoint(this.anchor, this.relativeAnchor, this.offset.getCopy());
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof AnchorPoint) {
			AnchorPoint p = (AnchorPoint)o;
			return p.anchor == this.anchor && p.relativeAnchor == this.relativeAnchor && p.offset.equals(this.offset);
		}
		return false;
	}
}
