package com.pip.uieditor.model;

import org.jdom.Attribute;
import org.jdom.Element;

import com.pip.uieditor.model.annotation.Property;
import com.pip.uieditor.model.persist.PersistMapping;
import com.pip.uieditor.model.persist.XmlUtil;
import com.pip.uieditor.model.propertydescriptor.AnchorPropertyDescriptor;
import com.pip.uieditor.model.propertydescriptor.BooleanPropertyDescriptor;
import com.pip.uieditor.model.propertydescriptor.FloatPropertyDescriptor;

public class GameSpriteRegion extends Region {
	
	public static final GameSpriteRegion PROPERTY = new GameSpriteRegion();
	
//	@Property(type=AnchorPropertyDescriptor.class)
//	private int alignment;
	
	@Property(type=FloatPropertyDescriptor.class)
	private float scale = 1.0f;
	
	public GameSpriteRegion() {
		super("");
//		alignment = 1;
	}
	

//	public int getAlignment() {
//		return alignment;
//	}
//
//
//
//	public void setAlignment(int alignment) {
//		this.alignment = alignment;
//	}

	


	@Override
	public Region clone() {
		GameSpriteRegion ret = new GameSpriteRegion();
		fillCloneRegion(ret);
		ret.scale = scale;
//		ret.alignment = alignment;
		return ret;
	}

	public float getScale() {
		return scale;
	}


	public void setScale(float scale) {
		this.scale = scale;
	}


	@Override
	public boolean generateEquals(Region region) {
		if(region == null)
			return false;
		GameSpriteRegion ir = (GameSpriteRegion)region;
		return scale == ir.scale 
				&& super.generateEquals(region);
	}
	
	@Override
	public Element toXml(PersistMapping mapping) throws Exception {
		Element el = super.toXml(mapping);
		el.setAttribute(new Attribute("scale", String.valueOf(this.scale)));
		return el;
	}

	@Override
	public void load(Object parent, Element element, PersistMapping mapping)
			throws Exception {
		super.load(parent, element, mapping);
//		this.alignment = XmlUtil.getIntValue(element, "alignment", this.alignment);
		this.scale = XmlUtil.getFloatValue(element, "scale", this.scale);
	}
}
