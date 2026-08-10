package com.pip.uieditor.model.persist;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;

import com.pip.uieditor.model.ARGB;
import com.pip.uieditor.model.AnchorPoint;

public class XmlUtil {
	
	public static float getFloatValue(Element element, String attribute, float defaultValue) throws DataConversionException {
		Attribute att = element.getAttribute(attribute);
		if(att == null)
			return defaultValue;
		return att.getFloatValue();
	}
	public static int getIntValue(Element element, String attribute, int defaultValue) throws DataConversionException {
		Attribute att = element.getAttribute(attribute);
		if(att == null)
			return defaultValue;
		return att.getIntValue();
	}
	
	public static long getLongValue(Element element, String attribute, long defaultValue) throws DataConversionException {
		Attribute att = element.getAttribute(attribute);
		if(att == null)
			return defaultValue;
		return att.getLongValue();
	}
	
	public static boolean getBooleanValue(Element element, String attribute, boolean defaultValue) throws DataConversionException {
		Attribute att = element.getAttribute(attribute);
		if(att == null)
			return defaultValue;
		return att.getBooleanValue();
	}
	
	public static String getStringValue(Element element, String attribute, String defaultValue) {
		Attribute att = element.getAttribute(attribute);
		if(att == null)
			return defaultValue;
		return att.getValue();
	}
	
	public static Point getPoint(Element element, String attribute, Point defaultValue) throws DataConversionException{
		Attribute att = element.getAttribute(attribute);
		if(att == null) 
			return defaultValue;
		String v = att.getValue();
		String[] ss = v.split(",");
		if(ss.length != 2)
			throw new DataConversionException(attribute, "Point");
		try {
			return new Point(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]));
		} catch (NumberFormatException e) {
			throw new DataConversionException(attribute, "Point");
		}
	}
	
	public static AnchorPoint getAnchorPoint(Element element) throws DataConversionException{
		int anchor = getIntValue(element, "anchor", 0);
		int relativeAnchor = getIntValue(element, "relativeAnchor", 0);
		Point offset = getPoint(element, "offset", null);
		return new AnchorPoint(anchor, relativeAnchor, offset);
	}
	
	public static Element getAnchorPointElement(AnchorPoint anchorPoint) {
		Element element = new Element("AnchorPoint");
		element.setAttribute(new Attribute("anchor", String.valueOf(anchorPoint.getAnchor())));
		element.setAttribute(new Attribute("relativeAnchor", String.valueOf(anchorPoint.getRelativeAnchor())));
		element.setAttribute(getPointAttribute("offset", anchorPoint.getOffset()));
		return element;
	}
	
	public static Attribute getPointAttribute(String name, Point point) {
		return new Attribute(name, point.x +"," + point.y);
	}
	
	public static Dimension getDimension(Element element, String attribute,
			Dimension defaultValue) throws DataConversionException {
		Attribute att = element.getAttribute(attribute);
		if(att == null) 
			return defaultValue;
		String v = att.getValue();
		String[] ss = v.split(",");
		if(ss.length != 2)
			throw new DataConversionException(attribute, "Dimension");
		try {
			return new Dimension(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]));
		} catch (NumberFormatException e) {
			throw new DataConversionException(attribute, "Dimension");
		}
	}
	
	public static Attribute getDimensionAttribute(String name, Dimension dim) {
		return new Attribute(name, dim.width +"," + dim.height);
	}
	
	public static Attribute getBooleanAttribute(String name, boolean value) {
		return new Attribute(name, value ? "true" : "false");
	}
	
	public static Attribute getStringAttribute(String name, String value) {
		return new Attribute(name, value);
	}
	
	public static Attribute getInsetsAttribute(String name, Insets value) {
		return new Attribute(name, value.left + "," + value.top + "," + value.right + "," + value.bottom);
	}
	
	public static Insets getInsetsValue(Element element, String attribute,
			Insets defaultValue) throws DataConversionException {
		Attribute att = element.getAttribute(attribute);
		if (att == null)
			return defaultValue;
		String v = att.getValue();
		String[] ss = v.split(",");
		if (ss.length != 4)
			throw new DataConversionException(attribute, "Insets");
		try {
			return new Insets(Integer.parseInt(ss[1]), Integer.parseInt(ss[0]),
					Integer.parseInt(ss[3]), Integer.parseInt(ss[2]));
		} catch (NumberFormatException e) {
			throw new DataConversionException(attribute, "Point");
		}
	}
	
	public static ARGB getARGB(Element element, String attribute,
			ARGB defaultValue) throws DataConversionException {
		Attribute att = element.getAttribute(attribute);
		if(att == null) 
			return defaultValue;
		String v = att.getValue();
		String[] ss = v.split(",");
		if(ss.length != 4)
			throw new DataConversionException(attribute, "ARGB");
		try {
			return new ARGB(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]), Integer.parseInt(ss[2]), Integer.parseInt(ss[3]));
		} catch (NumberFormatException e) {
			throw new DataConversionException(attribute, "ARGB");
		}
	}
	
	public static Attribute getARGBAttribute(String name, ARGB color) {
		return new Attribute(name, color.alpha +"," + color.red + "," + color.green + "," + color.blue);
	}
}
