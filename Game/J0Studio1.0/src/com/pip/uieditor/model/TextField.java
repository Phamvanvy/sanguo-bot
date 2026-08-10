package com.pip.uieditor.model;

import org.jdom.Attribute;
import org.jdom.Element;

import com.pip.uieditor.model.annotation.Property;
import com.pip.uieditor.model.persist.PersistMapping;
import com.pip.uieditor.model.persist.XmlUtil;
import com.pip.uieditor.model.propertydescriptor.IntPropertyDescriptor;

public class TextField extends Widget{
	public static final TextField PROTOTYPE = new TextField();
	
	@Property(type=IntPropertyDescriptor.class)
	private int maxLen;
	
	public TextField() {
		super("TextField");
		Region region = new StringRegion();
		region.setId("_TEXT");
		region.setRequire(true);
		region.addAnchorPoint(new AnchorPoint(Anchor.LEFT, Anchor.LEFT));
		region.setLayer(LAYER_OVERLAY);
		addRegion(region);
	}
	
	@Override
	public void initFlags() {
		setClickable(true);
		setLongClickable(false);
		setHorizontalScrollBarEnabled(false);
		setVerticalScrollBarEnabled(false);
		setScrollContainer(false);
		setFocusable(false);
	}
	
	@Override
	public String getDefaultName() {
		return "tf";
	}
	
	@Override
	public TextField clone() {
		TextField ret = new TextField();
		fillCloneWidget(ret);
		ret.maxLen = maxLen;
		return ret;
	}
	
	
	
	public int getMaxLen() {
		return maxLen;
	}

	public void setMaxLen(int maxLen) {
		this.maxLen = maxLen;
	}

	@Override
	public void load(Object parent, Element element, PersistMapping mapping)
			throws Exception {
		super.load(parent, element, mapping);
		this.maxLen = XmlUtil.getIntValue(element, "maxLen", this.maxLen);
	}
	
	@Override
	public Element toXml(PersistMapping mapping) throws Exception {
		Element element = super.toXml(mapping);
		element.setAttribute(new Attribute("maxLen", String.valueOf(this.maxLen)));
		return element;
	}
}
