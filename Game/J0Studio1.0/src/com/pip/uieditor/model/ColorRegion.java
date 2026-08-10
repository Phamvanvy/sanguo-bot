package com.pip.uieditor.model;

import org.jdom.Element;

import com.pip.uieditor.model.annotation.Property;
import com.pip.uieditor.model.persist.PersistMapping;
import com.pip.uieditor.model.persist.XmlUtil;
import com.pip.uieditor.model.propertydescriptor.ColorPropertyDescriptor;
import com.pip.uieditor.util.ObjectUtil;

public class ColorRegion extends Region {
	
	public static final ColorRegion PROTOTYPE = new ColorRegion();
	
	@Property(type=ColorPropertyDescriptor.class)
	private ARGB color;
	
	public ColorRegion() {
		this("", new ARGB(255, 255 , 255, 255));
	}
	
	public ColorRegion(String id, ARGB color) {
		super(id);
		this.color = color;
	}
	
	public void setColor(ARGB color) {
		ARGB oldColor = this.color;
		this.color = color;
		firePropertyChange("color", oldColor, this.color);
	}
	
	public ARGB getColor() {
		return this.color;
	}

	@Override
	public Element toXml(PersistMapping mapping) throws Exception {
		Element el = super.toXml(mapping);
		el.setAttribute(XmlUtil.getARGBAttribute("color", color));
		return el;
	}

	@Override
	public void load(Object parent, Element element, PersistMapping mapping)
			throws Exception {
		super.load(parent, element, mapping);
		setColor(XmlUtil.getARGB(element, "color", new ARGB(0, 0, 0, 0)));
	}
	
	public ColorRegion clone() {
		ColorRegion ret = new ColorRegion();
		fillCloneRegion(ret);
		ret.color = this.color.getCopy();
		return ret;
	}
	
	public boolean generateEquals(ColorRegion region) {
		return super.generateEquals(region) && ObjectUtil.equals(color, region.color) ;
	}
	
	@Override
	public AnchorPoint[] getDefaultAnchorPoints() {
		return new AnchorPoint[]{new AnchorPoint(Anchor.TOPLEFT, Anchor.TOPLEFT), new AnchorPoint(Anchor.BOTTOMRIGHT, Anchor.BOTTOMRIGHT)};
	}
}
