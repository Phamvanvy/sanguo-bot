package com.pip.uieditor.model;

import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.jdom.Attribute;
import org.jdom.Element;

import com.pip.uieditor.model.annotation.Property;
import com.pip.uieditor.model.persist.PersistMapping;
import com.pip.uieditor.model.persist.XmlUtil;
import com.pip.uieditor.model.propertydescriptor.IntPropertyDescriptor;
import com.pip.uieditor.model.propertydescriptor.TabBarStylePropertyDescriptor;

public class TabBar extends Widget {
	public static final TabBar PROTOTYPE = new TabBar();
	
	@Property(type=IntPropertyDescriptor.class)
	private int gap;
	
	@Property(type=TabBarStylePropertyDescriptor.class)
	private int style = 1;
	
	private static final int LEFT = 0;
	private static final int TOP = 1;
	private static final int RIGHT = 2;
	private static final int BOTTOM = 3;
	
	public TabBar() {
		super("TabBar");
	}
	
	@Override
	public TabBar clone() {
		TabBar ret = new TabBar();
		fillCloneWidget(ret);
		cloneTabButtons(ret);
		ret.gap = gap;
		ret.style = style;
		return ret;
	}
	
	protected void cloneTabButtons(TabBar bar) {
		for(int i = 0; i < getChildCount(); i++) {
			TabButton button = (TabButton)getChild(i);
			bar.addChild(button.clone());
		}
	}
	
	
	public int getGap() {
		return gap;
	}

	public void setGap(int gap) {
		if(this.gap != gap) {
			int old = this.gap;
			this.gap = gap;
			firePropertyChange("gap", old, this.gap);
			layoutWidgets();
		}
	}

	public int getStyle() {
		return style;
	}

	public void setStyle(int style) {
		if(this.style != style) {
			int old = this.style;
			this.style = style;
			firePropertyChange("style", old, this.style);
			layoutWidgets();
		}
	}
	
	@Override
	public void addChild(Widget widget) {
		super.addChild(widget);
		layoutWidgets();
	}
	
	@Override
	public void removeChild(Widget widget) {
		super.removeChild(widget);
		layoutWidgets();
	}

	@Override
	public void load(Object parent, Element element, PersistMapping mapping)
			throws Exception {
		super.load(parent, element, mapping);
		this.gap = XmlUtil.getIntValue(element, "gap", this.gap);
		this.style = XmlUtil.getIntValue(element, "style", this.style);
		List list = element.getChildren("TabButton");
		for(int i = 0; i < list.size(); i++) {
			Element el = (Element)list.get(i);
			TabButton button = new TabButton();
			button.load(this, el, mapping);
			addChild(button);
		}
	}
	
	@Override
	public Element toXml(PersistMapping mapping) throws Exception {
		Element element = super.toXml(mapping);
		element.setAttribute(new Attribute("gap", String.valueOf(this.gap)));
		element.setAttribute(new Attribute("style", String.valueOf(this.style)));
		for(int i = 0; i< getChildCount(); i++) {
			TabButton button = (TabButton)getChild(i);
			element.addContent(button.toXml(mapping));
		}
		return element;
	}
	
	@Override
	public String getDefaultName() {
		return "tbar";
	}
	
	@Override
	protected void layoutWidgets() {
		if(style == LEFT) {
			int y = 0;
			for(int i = 0; i < getChildCount(); i++) {
				Widget widget = getChild(i);
				widget.setLocation(new Point(getClientAreaWidth() - widget.getSize().width,y));
				y += (widget.getSize().height + gap);
			}
		} else if(style == TOP) {
			int x = 0;
			for(int i = 0; i < getChildCount(); i++) {
				Widget widget = getChild(i);
				widget.setLocation(new Point(x, getClientAreaHeight() - widget.getSize().height));
				x += (widget.getSize().width + gap);
			}
		} else if(style == RIGHT) {
			int y = 0;
			for(int i = 0; i < getChildCount(); i++) {
				Widget widget = getChild(i);
				widget.setLocation(new Point(0,y));
				y += (widget.getSize().height + gap);
			}
		} else if(style == BOTTOM) {
			int x = 0;
			for(int i = 0; i < getChildCount(); i++) {
				Widget widget = getChild(i);
				widget.setLocation(new Point(x, 0));
				x += (widget.getSize().width + gap);
			}
		}
	}
	
	public void layoutSubWidgets() {
		List columns = getSubWidgets();
		if(columns.size() > 0) {
			int total = getClientAreaWidth();
			int fliexibleCount = 0;
			int totalPreferredWidth = 0;
			for(int i = 0; i < columns.size(); i++) {
				TableColumn column = (TableColumn)columns.get(i);
				if(column.isFlexible()) {
					fliexibleCount ++;
				}
				totalPreferredWidth += column.getPreferredWidth();
			}
			int offset = 0;
			if(fliexibleCount > 0)
				offset = (total - totalPreferredWidth) / fliexibleCount;
			int start = 0;
			for(int i = 0; i< columns.size(); i++) {
				TableColumn column = (TableColumn)columns.get(i);
				int width = column.getPreferredWidth();
				if(column.isFlexible()) {
					width += offset;
				}
				column.setBounds(start, 0, width, getClientAreaHeight());
				start += width;
			}
		}
	}
}
