package com.pip.uieditor.model;

import java.util.List;

import org.eclipse.jface.text.projection.ChildDocument;
import org.jdom.Attribute;
import org.jdom.Element;

import com.pip.uieditor.model.annotation.Property;
import com.pip.uieditor.model.persist.PersistMapping;
import com.pip.uieditor.model.persist.XmlUtil;
import com.pip.uieditor.model.propertydescriptor.IntPropertyDescriptor;


public class Slider extends Widget{
	
	public static final Slider PROTOTYPE = new Slider();
	
	@Property(type=IntPropertyDescriptor.class)
	private int min = 0;
	
	@Property(type=IntPropertyDescriptor.class)
	private int max = 100;
	
	@Property(type=IntPropertyDescriptor.class)
	private int value = 50;
	
	@Property(type=IntPropertyDescriptor.class)
	private int thumbWidth = 10;
	
	@Property(type=IntPropertyDescriptor.class)
	private int thumbHeight = 10;
	
	private Label head, tail;
	private Button thumb;
	
	public Slider() {
		super("Slider");
		this.head = new Label();
		this.head.setName("");
		this.tail = new Label();
		this.tail.setName("");
		this.thumb = new Button();
		this.thumb.setName("");
		addChild(this.head);
		addChild(this.tail);
		addChild(this.thumb);
	}
	
	
	
	public int getMin() {
		return min;
	}



	public void setMin(int min) {
		if(this.min != min) {
			int old = this.min;
			this.min = min;
			firePropertyChange("min", old, this.min);
			layoutWidgets();
		}
	}



	public int getMax() {
		return max;
	}



	public void setMax(int max) {
		if(this.max != max) {
			int old = this.max;
			this.max = max;
			firePropertyChange("max", old, this.max);
			layoutWidgets();
		}
	}



	public int getValue() {
		return value;
	}



	public void setValue(int value) {
		if(this.value != value) {
			int old = this.value;
			this.value = value;
			firePropertyChange("value", old, this.value);
			layoutWidgets();
		}
	}



	public int getThumbWidth() {
		return thumbWidth;
	}



	public void setThumbWidth(int thumbWidth) {
		if(this.thumbWidth != thumbWidth) {
			int old = this.thumbWidth;
			this.thumbWidth = thumbWidth;
			firePropertyChange("thumbWidth", old, this.thumbWidth);
			layoutWidgets();
		}
	}



	public int getThumbHeight() {
		return thumbHeight;
	}



	public void setThumbHeight(int thumbHeight) {
		if(this.thumbHeight != thumbHeight) {
			int old = this.thumbHeight;
			this.thumbHeight = thumbHeight;
			firePropertyChange("thumbHeight", old, this.thumbHeight);
			layoutWidgets();
		}
	}
	
	



	public Label getHead() {
		return head;
	}



	public Label getTail() {
		return tail;
	}



	public Button getThumb() {
		return thumb;
	}



	@Override
	public Slider clone() {
		Slider ret = new Slider();
		ret.min = min;
		ret.max = max;
		ret.value = value;
		ret.thumbWidth = thumbWidth;
		ret.thumbHeight = thumbHeight;
		fillCloneWidget(ret);
		ret.init(head.clone(), tail.clone(), thumb.clone());
		return ret;
	}
	
	protected void init(Label head, Label tail, Button thumb) {
		removeChildren();
		this.head = head;
		this.tail = tail;
		this.thumb = thumb;
		addChild(head);
		addChild(tail);
		addChild(thumb);
	}
	
	@Override
	protected void layoutWidgets() {
		float v = (float) (value - min) / (max - min);
		int w = (int) ((getClientAreaWidth() - thumbWidth) * v);
		int h = getClientAreaHeight();
		head.setBounds(thumbWidth / 2, 0, w, h);
		int w1 = (getClientAreaWidth() - thumbWidth) - w;
		tail.setBounds(thumbWidth / 2 + w, 0, w1, h);
		thumb.setBounds(w , 0, thumbWidth, thumbHeight);
	}
	
	@Override
	public String getDefaultName() {
		return "sld";
	}
	
	@Override
	public Element toXml(PersistMapping mapping) throws Exception {
		Element element = super.toXml(mapping);
		element.setAttribute(new Attribute("min", String.valueOf(this.min)));
		element.setAttribute(new Attribute("max", String.valueOf(this.max)));
		element.setAttribute(new Attribute("thumbWidth", String.valueOf(this.thumbWidth)));
		element.setAttribute(new Attribute("thumbHeight", String.valueOf(this.thumbHeight)));
		element.addContent(head.toXml(mapping));
		element.addContent(tail.toXml(mapping));
		element.addContent(thumb.toXml(mapping));
		return element;
	}
	
	@Override
	public void load(Object parent, Element element, PersistMapping mapping)
			throws Exception {
		super.load(parent, element, mapping);
		this.min = XmlUtil.getIntValue(element, "min", this.min);
		this.max = XmlUtil.getIntValue(element, "max", this.max);
		this.thumbWidth = XmlUtil.getIntValue(element, "thumbWidth", this.thumbWidth);
		this.thumbHeight = XmlUtil.getIntValue(element, "thumbHeight", this.thumbHeight);
		List list = element.getChildren("Label");
		head.load(this, (Element)list.get(0), mapping);
		tail.load(this, (Element)list.get(1), mapping);
		thumb.load(this, element.getChild("Button"), mapping);
	}	
}
