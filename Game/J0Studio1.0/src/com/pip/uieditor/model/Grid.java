package com.pip.uieditor.model;

import org.jdom.Attribute;
import org.jdom.Element;

import com.pip.uieditor.model.annotation.Property;
import com.pip.uieditor.model.persist.PersistMapping;
import com.pip.uieditor.model.persist.XmlUtil;
import com.pip.uieditor.model.propertydescriptor.IntPropertyDescriptor;

public class Grid extends Widget{
	
	public static final Grid PROTOTYPE = new Grid();
	
	@Property(type=IntPropertyDescriptor.class)
	private int columnCount = 3;
	
	@Property(type=IntPropertyDescriptor.class)
	private int cellWidth = 0;
	
	@Property(type=IntPropertyDescriptor.class)
	private int cellHeight = 0;
	
	public Grid() {
		super("Grid");
	}
	
	@Override
	public String getDefaultName() {
		return "grid";
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
	
	public int getColumnCount() {
		return columnCount;
	}

	public void setColumnCount(int columnCount) {
		this.columnCount = columnCount;
	}

	public int getCellWidth() {
		return cellWidth;
	}

	public void setCellWidth(int cellWidth) {
		this.cellWidth = cellWidth;
	}

	public int getCellHeight() {
		return cellHeight;
	}

	public void setCellHeight(int cellHeight) {
		this.cellHeight = cellHeight;
	}

	@Override
	public Grid clone() {
		Grid ret = new Grid();
		fillCloneWidget(ret);
		ret.columnCount = columnCount;
		ret.cellWidth = cellWidth;
		ret.cellHeight = cellHeight;
		return ret;
	}
	
	@Override
	public Element toXml(PersistMapping mapping) throws Exception {
		Element element = super.toXml(mapping);
		element.setAttribute(new Attribute("columnCount", String.valueOf(this.columnCount)));
		element.setAttribute(new Attribute("cellWidth", String.valueOf(this.cellWidth)));
		element.setAttribute(new Attribute("cellHeight", String.valueOf(this.cellHeight)));
		return element;
	}
	
	@Override
	public void load(Object parent, Element element, PersistMapping mapping)
			throws Exception {
		super.load(parent, element, mapping);
		this.columnCount = XmlUtil.getIntValue(element, "columnCount", this.columnCount);
		this.cellWidth = XmlUtil.getIntValue(element, "cellWidth", this.cellWidth);
		this.cellHeight = XmlUtil.getIntValue(element, "cellHeight", this.cellHeight);
	}
}
