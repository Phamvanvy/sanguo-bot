package com.pip.uieditor.model;

import org.jdom.Element;

import com.pip.uieditor.model.persist.PersistMapping;

public class CustomeRegion extends Region{
	
	public static final CustomeRegion PROTOTYPE = new CustomeRegion();
	
	public CustomeRegion() {
		this("");
	}	
	
	CustomeRegion(String id) {
		super(id);
	}

	@Override
	public Region clone() {
		CustomeRegion ret = new CustomeRegion(getId());
		return ret;
	}
	
	@Override
	public Element toXml(PersistMapping mapping) throws Exception {
		Element el = super.toXml(mapping);
		return el;
	}

	@Override
	public void load(Object parent, Element element, PersistMapping mapping)
			throws Exception {
		super.load(parent, element, mapping);
	}
	
	@Override
	public boolean generateEquals(Region region) {
		return false;
	}
}
