package com.pip.uieditor.model;

import java.util.List;

import org.jdom.Element;

import com.pip.uieditor.model.annotation.Property;
import com.pip.uieditor.model.persist.PersistMapping;
import com.pip.uieditor.model.persist.XmlUtil;
import com.pip.uieditor.model.propertydescriptor.BooleanPropertyDescriptor;

/**
 * Container可以容纳多个Widget
 * @author Jeffrey
 *
 */
public class Container extends Widget {

	public static final Container PROTOTYPE = new Container();
	
	@Property(type=BooleanPropertyDescriptor.class)
	private boolean scrollPage = false;
	
	@Property(type=BooleanPropertyDescriptor.class)
	private boolean scrollHorizontal = true;
	
	@Property(type=BooleanPropertyDescriptor.class)
	private boolean scrollVertical = true;
	
	public Container() {
		this("Container");
	}
	
	public Container(String type) {
		super(type);
	}
	
	@Override
	protected void initFlags() {
		setClickable(false);
		setLongClickable(false);
		setHorizontalScrollBarEnabled(true);
		setVerticalScrollBarEnabled(true);
		setScrollContainer(false);
		setFocusable(false);
	}
	
	
	@Override
	public Element toXml(PersistMapping mapping) throws Exception {
		Element element = super.toXml(mapping);
		element.setAttribute(XmlUtil.getBooleanAttribute("scrollPage", scrollPage));
		element.setAttribute(XmlUtil.getBooleanAttribute("scrollHorizontal", scrollHorizontal));
		element.setAttribute(XmlUtil.getBooleanAttribute("scrollVertical", scrollVertical));
		for(int i = 0; i < getChildCount() ; i++) {
			Widget widget = getChild(i);
			element.addContent(widget.toXml(mapping));
		}
		return element;
	}

	@Override
	public void load(Object parent, Element element, PersistMapping mapping)
			throws Exception {
		super.load(parent, element, mapping);
		this.scrollPage = XmlUtil.getBooleanValue(element, "scrollPage", this.scrollPage);
		this.scrollHorizontal = XmlUtil.getBooleanValue(element, "scrollHorizontal", this.scrollHorizontal);
		this.scrollVertical = XmlUtil.getBooleanValue(element, "scrollVertical", this.scrollVertical);
		List l = element.getChildren();
		for(int i = 0; i < l.size(); i++) {
			Element el = (Element)l.get(i);
			if(!el.getName().endsWith("Region")) {
				addChild((Widget)loadUIObject(this, el, mapping));
			}
				
		}
	}
	
	public Container clone() {
		Container ret = new Container();
		fillCloneWidget(ret);
		ret.scrollPage = scrollPage;
		ret.scrollHorizontal = scrollHorizontal;
		ret.scrollVertical = scrollVertical;
		for(Widget widget : getChildren()) {
			Widget cloned = widget.clone();
			ret.addChild(cloned);
		}
		return ret;
	}
	
	protected void fillCloneContainer(Container c) {
		fillCloneWidget(c);
		for(Widget widget : getChildren()) {
			Widget cloned = widget.clone();
			c.addChild(cloned);
		}
	}
	
	@Override
	public String getDefaultName() {
		return "con";
	}

	public boolean isScrollHorizontal() {
		return scrollHorizontal;
	}

	public void setScrollHorizontal(boolean scrollHorizontal) {
		this.scrollHorizontal = scrollHorizontal;
		setHorizontalScrollBarEnabled(scrollHorizontal);
	}

	public boolean isScrollVertical() {
		return scrollVertical;
	}

	public void setScrollVertical(boolean scrollVertical) {
		this.scrollVertical = scrollVertical;
		setVerticalScrollBarEnabled(scrollVertical);
	}

	public boolean isScrollPage() {
		return scrollPage;
	}

	public void setScrollPage(boolean scrollPage) {
		this.scrollPage = scrollPage;
	}
	
	
	
	
//	public Widget findWidget(String name) {
//		for(Widget widget : getChildren()) {
//			if(name.equals(widget.getName()))
//				return widget;
//			if(widget instanceof Container) {
//				Widget ret = ((Container)widget).findWidget(name);
//				if(ret != null)
//					return ret;
//			}
//		}
//		return null;
//	}
}
