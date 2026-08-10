package com.pip.uieditor.model;

import java.util.Vector;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.jdom.Attribute;
import org.jdom.Element;

import com.pip.uieditor.model.annotation.Property;
import com.pip.uieditor.model.persist.PersistMapping;
import com.pip.uieditor.model.persist.XmlUtil;
import com.pip.uieditor.model.propertydescriptor.BooleanPropertyDescriptor;
import com.pip.uieditor.model.propertydescriptor.ColorPropertyDescriptor;
import com.pip.uieditor.model.propertydescriptor.IntPropertyDescriptor;
import com.pip.uieditor.model.propertydescriptor.RichTextPropertyDescriptor;
import com.pip.uieditor.model.text.Document;
import com.pip.uieditor.model.text.Document.FormatContext;
import com.pip.uieditor.model.text.Document.Line;
import com.pip.uieditor.model.text.Paragraph;
import com.pip.uieditor.model.text.RichTextParser;
import com.pip.uieditor.model.text.View;
import com.pip.uieditor.util.ObjectUtil;

public class StringRegion extends Region {
	
	public static final StringRegion PROTOTYPE = new StringRegion();
	
	@Property(type=RichTextPropertyDescriptor.class)
	private String text;
	
	@Property(type=ColorPropertyDescriptor.class)
	private ARGB color;
	
	@Property(type=ColorPropertyDescriptor.class)
	private ARGB backgroundColor;
	
	@Property(type=BooleanPropertyDescriptor.class)
	private boolean lineWrap = false;
	
	@Property(type=TextPropertyDescriptor.class)
	private String fontName = "";
	
	@Property(type=BooleanPropertyDescriptor.class)
	private boolean shadow;
	
	@Property(type=ColorPropertyDescriptor.class)
	private ARGB linkColor = new ARGB(255, 0, 0, 0);
	
	@Property(type=ColorPropertyDescriptor.class)
	private ARGB shadowColor;
	
	@Property(type=IntPropertyDescriptor.class)
	private int lineGap = 2;
	
	public Vector views = new Vector();
	
	Document doc = new Document();
	
	public StringRegion() {
		this("","","", new ARGB(255,0,0,0), new ARGB(255,255,255,255));
	}
	
	
	public StringRegion(String id, String text, String fontName, ARGB color, ARGB backgroundColor) {
		super(id);
		this.text = text;
		this.color = color;
		this.fontName = fontName;
		this.backgroundColor = backgroundColor;
		this.shadowColor = new ARGB(255,0,0,0);
	}
	
	protected void calcStringExtent() {
		if (this.text != null) {
			GC gc = new GC(Display.getCurrent());
			formatContent(doc, gc);
			if(!lineWrap) {
				int width = 0;
				int height = 0;
				int currentLineHeight = 0;
				Line line = null;
				int maxWidth = 0;
				for(int i = 0; i< views.size(); i++) {
					View view =  (View)views.elementAt(i);
					if(line != view.getLine()) {
						line = view.getLine();
						height += currentLineHeight;
						currentLineHeight = 0;
						if (width > maxWidth) {
							maxWidth = width;
						}
						width = 0;
						if (i > 0) {
							height += lineGap;
						}
					}
					width += view.getWidth();
					if(currentLineHeight < view.getHeight()) {
						currentLineHeight = view.getHeight();
					}
				}
				if (width > maxWidth) {
					maxWidth = width;
				}
				height += currentLineHeight;
				setSize(new Dimension(maxWidth, height));
			}
//			GC gc = new GC(Display.getDefault());
//			Font f = new Font(Display.getDefault(), getFontData());
//			gc.setFont(f);
//			Point p = gc.stringExtent(this.text);
//			setSize(new Dimension(p.x, p.y));
//			f.dispose();
		}
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		String old = this.text;
		this.text = text;
		this.doc.setText(text);
		firePropertyChange("text", old, this.text);
		invalidate();
	}
	
	public FontData getFontData() {
		return FontUtil.getFontData(fontName);
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
	
	public String getFontName() {
		return this.fontName;
	}
	
	public ARGB getColor() {
		return this.color;
	}
	
	public void setColor(ARGB color) {
		if(!this.color.equals(color)) {
			ARGB old = this.color;
			this.color = color;
			firePropertyChange("color", old, this.color);
		}
	}
	
	public ARGB  getBackgroundColor() {
		return this.backgroundColor;
	}
	
	public void setBackgroundColor(ARGB backgroundColor) {
		if(!this.backgroundColor.equals(backgroundColor)) {
			ARGB old = this.backgroundColor;
			this.backgroundColor = backgroundColor;
			firePropertyChange("backgroundColor", old, this.backgroundColor);
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

	@Override
	public boolean isAvaliable() {
		return super.isAvaliable() && getText() != null && getText().length() !=0;
	}
	
	@Override
	public void validate() {
		calcStringExtent();
		super.validate();
	}

	@Override
	public Element toXml(PersistMapping mapping) throws Exception {
		
		Element el = super.toXml(mapping);
		if(this.text != null)
			el.setAttribute(new Attribute("text",RichTextParser.escapeString(this.text)));
		el.setAttribute(XmlUtil.getARGBAttribute("color", this.color));
		el.setAttribute(XmlUtil.getARGBAttribute("backgroundColor", this.backgroundColor));
		el.setAttribute(XmlUtil.getARGBAttribute("shadowColor", this.shadowColor));
		el.setAttribute(XmlUtil.getBooleanAttribute("lineWrap", this.lineWrap));
		if(this.fontName != null && this.fontName.length() > 0) {
			el.setAttribute(new Attribute("fontName", this.fontName));
		}
		el.setAttribute(XmlUtil.getBooleanAttribute("shadow", this.shadow));
		el.setAttribute(new Attribute("lineGap", String.valueOf(this.lineGap)));
		el.setAttribute(XmlUtil.getARGBAttribute("linkColor", this.linkColor));
		return el;
	}

	@Override
	public void load(Object parent, Element element, PersistMapping mapping)
			throws Exception {
		super.load(parent, element, mapping);
		this.text = XmlUtil.getStringValue(element, "text", null);{
			if(this.text != null) {
				this.text = RichTextParser.parseEscapedString(this.text);
				doc.setText(this.text);
			}
		}
		this.color = XmlUtil.getARGB(element, "color", this.color);
		this.backgroundColor = XmlUtil.getARGB(element, "backgroundColor", this.backgroundColor);
		this.shadowColor = XmlUtil.getARGB(element, "shadowColor", this.shadowColor);
		this.lineWrap = XmlUtil.getBooleanValue(element, "lineWrap", this.lineWrap);
		this.fontName = XmlUtil.getStringValue(element, "fontName", this.fontName);
		this.shadow = XmlUtil.getBooleanValue(element, "shadow", this.shadow);
		this.lineGap = XmlUtil.getIntValue(element, "lineGap", this.lineGap);
		this.linkColor =  XmlUtil.getARGB(element, "linkColor", this.linkColor);
	}
	
	public StringRegion clone() {
		StringRegion ret = new StringRegion();
		fillCloneRegion(ret);
		ret.text = new String(this.text);
		ret.doc.setText(ret.text);
		ret.color = this.color.getCopy();
		ret.fontName = this.fontName;
		ret.backgroundColor = this.backgroundColor.getCopy();
		ret.lineGap = lineGap;
		ret.lineWrap = lineWrap;
		ret.shadow = shadow;
		ret.linkColor = linkColor.getCopy();
		return ret;
	}
	
	@Override
	public boolean generateEquals(Region region) {
		if(region == null)
			return false;
		if(!(region instanceof StringRegion))
			return false;
		StringRegion r = (StringRegion)region;
		return ObjectUtil.equals(this.color, r.color)
				&& ObjectUtil.equals(this.backgroundColor,r.backgroundColor)
				&& this.fontName == r.fontName
				&& this.lineGap == r.lineGap
				&& ObjectUtil.equals(this.shadowColor, r.shadowColor)
				&& this.shadow == r.shadow
				&& ObjectUtil.equals(this.linkColor, r.linkColor)
				&& ObjectUtil.equals(text, r.text) && super.generateEquals(r);
	}
	
	public void setLineWrap(boolean lineWrap) {
		if(this.lineWrap != lineWrap) {
			this.lineWrap = lineWrap;
			invalidate();
		}
	}
	
	public boolean isLineWrap() {
		return this.lineWrap;
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
	
	protected void formatContent(Document doc, GC gc) {
		int width = lineWrap ? this.getSize().width : Integer.MAX_VALUE;
		FormatContext context = new FormatContext(gc, width, getFontData());
		for(int i = 0; i < doc.getParagraphCount(); i++) {
			Paragraph paragraph = doc.getParagraph(i);
			for(int j = 0; j < paragraph.getElementCount(); j++) {
				com.pip.uieditor.model.text.Element element = paragraph.getElement(j);
				element.format(context);
			}
			context.newLine();
		}
		views = context.views;
	}
}
