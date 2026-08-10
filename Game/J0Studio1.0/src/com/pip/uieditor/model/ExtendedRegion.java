package com.pip.uieditor.model;

import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.jdom.Attribute;
import org.jdom.Element;

import com.pip.uieditor.model.annotation.Property;
import com.pip.uieditor.model.persist.PersistMapping;
import com.pip.uieditor.model.persist.XmlUtil;

public class ExtendedRegion extends Region{
	public static final CustomeRegion PROTOTYPE = new CustomeRegion();
	
	@Property(type=TextPropertyDescriptor.class)
	private String prefix="";
	
	public ExtendedRegion() {
		this("");
	}	
	
	ExtendedRegion(String id) {
		super(id);
	}

	
	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public Region clone() {
		ExtendedRegion ret = new ExtendedRegion(getId());
		ret.prefix = prefix;
		return ret;
	}
	
	@Override
	public Element toXml(PersistMapping mapping) throws Exception {
		Element el = super.toXml(mapping);
		el.setAttribute(new Attribute("prefix", String.valueOf(this.prefix)));
		return el;
	}

	@Override
	public void load(Object parent, Element element, PersistMapping mapping)
			throws Exception {
		super.load(parent, element, mapping);
		this.prefix = XmlUtil.getStringValue(element, "prefix", null);
	}
	
	@Override
	public boolean generateEquals(Region region) {
		return false;
	}
}
