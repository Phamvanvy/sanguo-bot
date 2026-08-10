package com.pip.uieditor.model;

import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.jdom.Attribute;
import org.jdom.Element;

import com.pip.j0ide.Settings;
import com.pip.uieditor.model.annotation.Property;
import com.pip.uieditor.model.persist.PersistMapping;
import com.pip.uieditor.model.persist.XmlUtil;
import com.pip.uieditor.model.propertydescriptor.BooleanPropertyDescriptor;
import com.pip.uieditor.model.propertydescriptor.ColorPropertyDescriptor;
import com.pip.uieditor.model.propertydescriptor.IntPropertyDescriptor;
import com.pip.uieditor.model.propertydescriptor.RichTextPropertyDescriptor;
import com.pip.uieditor.model.text.RichTextParser;

public class TextArea extends Widget {
	
	public static final TextArea PROTOTYPE = new TextArea();
	
	@Property(type=TextPropertyDescriptor.class)
	private String fontName = "";
	
	@Property(type=ColorPropertyDescriptor.class)
	private ARGB textColor = new ARGB(0xff, 0, 0, 0);
	
	@Property(type=BooleanPropertyDescriptor.class)
	private boolean shadow = false;
	
	@Property(type=ColorPropertyDescriptor.class)
	private ARGB linkColor = new ARGB(0xff, 0, 0, 0);
	
	@Property(type=ColorPropertyDescriptor.class)
	private ARGB shadowColor = new ARGB(0xff, 0xff, 0xff, 0xff);
	
	@Property(type=IntPropertyDescriptor.class)
	private int lineGap;
	
	@Property(type=RichTextPropertyDescriptor.class)
	private String content;
	
	public TextArea() {
		super("TextArea");
		this.content = "";
	}
	
	@Override
	public void initFlags() {
		setClickable(false);
		setLongClickable(false);
		setHorizontalScrollBarEnabled(false);
		setVerticalScrollBarEnabled(true);
		setScrollContainer(false);
		setFocusable(true);
	}
	
	@Override
	public String getDefaultName() {
		return "ta";
	}
	
	@Override
	public TextArea clone() {
		TextArea ret = new TextArea();
		fillCloneWidget(ret);
		ret.content = new String(this.content);
		ret.fontName = fontName;
		ret.textColor = this.textColor.getCopy();
		ret.shadow = this.shadow;
		ret.linkColor = this.linkColor.getCopy();
		ret.lineGap = this.lineGap;
		ret.shadowColor = this.shadowColor.getCopy();
		return ret;
	}
	
	public String getFontName() {
		return this.fontName;
	}
	
	public void setFontName(String fontName) {
		if(this.fontName == fontName)
			return;
		if(this.fontName != null && this.fontName.equals(fontName))
			return;
		if(fontName != null && fontName.equals(this.fontName))
			return;
		String oldFontName = this.fontName;
		this.fontName = fontName;
		firePropertyChange("font", oldFontName, fontName);		
	}


	public ARGB getTextColor() {
		return textColor;
	}

	public void setTextColor(ARGB textColor) {
		if(!this.textColor.equals(textColor)) {
			ARGB old = this.textColor;
			this.textColor = textColor;
			firePropertyChange("textColor", old, this.textColor);
		}
	}

	public boolean isShadow() {
		return shadow;
	}

	public void setShadow(boolean shadow) {
		if(this.shadow != shadow) {
			this.shadow = shadow;
			firePropertyChange("shadow", !this.shadow, this.shadow);
		}
	}

	public ARGB getLinkColor() {
		return linkColor;
	}

	public void setLinkColor(ARGB linkColor) {
		if(!this.linkColor.equals(linkColor)) {
			ARGB old = this.linkColor;
			this.linkColor = linkColor;
			firePropertyChange("linkColor", old, this.linkColor);
		}
	}

	public ARGB getShadowColor() {
		return shadowColor;
	}

	public void setShadowColor(ARGB shadowColor) {
		if(!this.shadowColor.equals(shadowColor)) {
			ARGB old = this.shadowColor;
			this.shadowColor = shadowColor;
			firePropertyChange("shadowColor", old, this.shadowColor);
		}
	}

	public int getLineGap() {
		return lineGap;
	}

	public void setLineGap(int lineGap) {
		if(this.lineGap != lineGap) {
			int old = this.lineGap;
			this.lineGap = lineGap;
			firePropertyChange("lineGap", old, this.lineGap);
		}
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		String old = this.content;
		this.content = content;
		firePropertyChange("content", old, this.content);
	}
	
	
	@Override
	public Element toXml(PersistMapping mapping) throws Exception {
		Element el = super.toXml(mapping);
		if(this.content != null)
			el.setAttribute(new Attribute("content", RichTextParser.escapeString(this.content)));
		el.setAttribute(XmlUtil.getARGBAttribute("textColor", this.textColor));
		el.setAttribute(XmlUtil.getARGBAttribute("linkColor", this.linkColor));
		el.setAttribute(XmlUtil.getARGBAttribute("shadowColor", this.shadowColor));
		el.setAttribute(new Attribute("lineGap", String.valueOf(this.lineGap)));
		if(this.fontName != null && this.fontName.length() > 0) {
			el.setAttribute(new Attribute("fontName", this.fontName));
		}
		el.setAttribute(XmlUtil.getBooleanAttribute("shadow", this.shadow));
		return el;
	}

	@Override
	public void load(Object parent, Element element, PersistMapping mapping)
			throws Exception {
		super.load(parent, element, mapping);
		this.content = XmlUtil.getStringValue(element, "content", null);
		this.content = RichTextParser.parseEscapedString(this.content);
		this.textColor = XmlUtil.getARGB(element, "textColor", this.textColor);
		this.linkColor = XmlUtil.getARGB(element, "linkColor", this.linkColor);
		this.shadowColor = XmlUtil.getARGB(element, "shadowColor", this.shadowColor);
		this.lineGap = XmlUtil.getIntValue(element, "lineGap", this.lineGap);
		this.fontName = XmlUtil.getStringValue(element, "fontName", this.fontName);
		this.shadow = XmlUtil.getBooleanValue(element, "shadow", this.shadow);
	}
}
