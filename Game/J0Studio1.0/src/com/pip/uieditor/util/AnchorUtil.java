package com.pip.uieditor.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import com.pip.uieditor.model.Anchor;
import com.pip.uieditor.model.AnchorPoint;

public class AnchorUtil {
	public static String anchorPointListToText(List<AnchorPoint> l) {
		StringBuilder sb = new StringBuilder(200);
		for(int i = 0; i < l.size(); i++) {
			AnchorPoint ap = l.get(i);
			sb.append(anchorPointToText(ap));
			if(i != l.size() - 1) {
				sb.append("\n");
			}
		}
		return sb.toString();
	}
	
	public static List<AnchorPoint> textToAnchorPointList(String text) throws Exception{
		String[] ss = text.split("\r\n");
		List<AnchorPoint> l = new ArrayList<AnchorPoint>(ss.length);
		for(int i = 0; i < ss.length; i++) {
			l.add(textToAnchorPoint(ss[i]));
		}
		return l;
	}
	
	public static AnchorPoint textToAnchorPoint(String text) throws Exception{
		String[] ss = text.split(",");
		if(ss.length != 2 && ss.length != 4)
			throw new Exception("");
		if(ss.length == 2)
			return new AnchorPoint(textToAnchor(ss[0]), textToAnchor(ss[1]));
		if(ss.length == 4){
			int x = 0;
			int y = 0;
			try {
				x = Integer.parseInt(ss[2]);
				y = Integer.parseInt(ss[3]);
			} catch (Exception e) {
				throw e;
			}
			return new AnchorPoint(textToAnchor(ss[0]), textToAnchor(ss[1]), new org.eclipse.draw2d.geometry.Point(x, y));
		}
		throw new Exception();
	}
	
	public static final String[] ANCHOR_TEXT = { "CENTER", "TOPLEFT", "TOP",
			"TOPRIGHT", "RIGHT", "BOTTOMRIGHT", "BOTTOM", "BOTTOMLEFT", "LEFT" };

	public static int textToAnchor(String text) throws Exception {
		for (int i = 0; i < ANCHOR_TEXT.length; i++) {
			if (ANCHOR_TEXT[i].equals(text))
				return i;
		}
		throw new Exception();
	}
	
	public static String anchorToText(int anchor) {
		return ANCHOR_TEXT[anchor];
	}
	
	public static String anchorPointToText(AnchorPoint anchorPoint) {
		StringBuilder sb = new StringBuilder(50);
		sb.append(anchorToText(anchorPoint.getAnchor())).append(",");
		sb.append(anchorToText(anchorPoint.getRelativeAnchor()));
		if(anchorPoint.getOffsetX() !=0 || anchorPoint.getOffsetY() !=0 ) {
			sb.append(",");
			sb.append(anchorPoint.getOffsetX()).append(",");
			sb.append(anchorPoint.getOffsetY());
		}
		return sb.toString();
	}
	
	public static Point calcAnchorPoint(int anchor, Rectangle rect, Point offset) {
		switch(anchor) {
			case Anchor.CENTER:
				return rect.getCenter().getTranslated(offset);
			case Anchor.TOPLEFT:
				return rect.getTopLeft().getTranslated(offset);
			case Anchor.TOP:
				return rect.getTop().getTranslated(offset);
			case Anchor.TOPRIGHT:
				return rect.getTopRight().getTranslated(offset);
			case Anchor.RIGHT:
				return rect.getRight().getTranslated(offset);
			case Anchor.BOTTOMRIGHT:
				return rect.getBottomRight().getTranslated(offset);
			case Anchor.BOTTOM:
				return rect.getBottom().getTranslated(offset);
			case Anchor.BOTTOMLEFT:
				return rect.getBottomLeft().getTranslated(offset);
			case Anchor.LEFT:
				return rect.getLeft().getTranslated(offset);
		}
		return null;
	}
}
