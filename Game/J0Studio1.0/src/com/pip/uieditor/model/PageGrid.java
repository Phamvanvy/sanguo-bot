package com.pip.uieditor.model;

import org.jdom.Attribute;
import org.jdom.Element;

import com.pip.uieditor.model.annotation.Property;
import com.pip.uieditor.model.persist.PersistMapping;
import com.pip.uieditor.model.persist.XmlUtil;
import com.pip.uieditor.model.propertydescriptor.PageGridStylePropertyDescriptor;

public class PageGrid extends Widget {
	
	public static final PageGrid PROTOTYPE = new PageGrid();
	
	@Property(type=PageGridStylePropertyDescriptor.class)
	private int style = 0;
	
	public PageGrid() {
		super("PageGrid");
	}
	
	@Override
	public String getDefaultName() {
		return "pg";
	}
	
	@Override
	public PageGrid clone() {
		PageGrid ret = new PageGrid();
		fillCloneWidget(ret);
		ret.style = style;
		return ret;
	}
	
	@Override
	public void initFlags() {
		setClickable(false);
		setLongClickable(false);
		setHorizontalScrollBarEnabled(false);
		setVerticalScrollBarEnabled(false);
		setScrollContainer(false);
		setFocusable(true);
	}
	
	
	public int getStyle() {
		return style;
	}

	public void setStyle(int style) {
		if(this.style != style) {
			int old = this.style;
			this.style = style;
			firePropertyChange("style", old, this.style);
		}
	}

	@Override
	public Element toXml(PersistMapping mapping) throws Exception {
		Element element = super.toXml(mapping);
		element.setAttribute(new Attribute("style", String.valueOf(this.style)));
		return element;
	}
	
	@Override
	public void load(Object parent, Element element, PersistMapping mapping)
			throws Exception {
		super.load(parent, element, mapping);
		this.style = XmlUtil.getIntValue(element, "style", this.style);
	}
}
