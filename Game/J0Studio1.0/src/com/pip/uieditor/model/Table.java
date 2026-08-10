package com.pip.uieditor.model;

import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;

import com.pip.uieditor.model.annotation.Property;
import com.pip.uieditor.model.persist.PersistMapping;
import com.pip.uieditor.model.persist.XmlUtil;
import com.pip.uieditor.model.propertydescriptor.BooleanPropertyDescriptor;
import com.pip.uieditor.model.propertydescriptor.IntPropertyDescriptor;


public class Table extends Widget {
	
	public static final Table PROTOTYPE = new Table();
	
	@Property(type=IntPropertyDescriptor.class)
	private int rowHeight = 20;
	
	@Property(type=BooleanPropertyDescriptor.class)
	private boolean keepSelection = false;
	
	public Table() {
		super("Table");
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
	public Table clone() {
		Table ret = new Table();
		ret.rowHeight = rowHeight;
		fillCloneWidget(ret);
		cloneTableColumns(ret);
		return ret;
	}
	
	public void setRowHeight(int rowHeight) {
		this.rowHeight = rowHeight;
	}
	
	public int getRowHeight() {
		return this.rowHeight;
	}
	
	public void setKeepSelection(boolean value) {
		this.keepSelection = value;
	}
	
	public boolean isKeepSelection() {
		return this.keepSelection;
	}
	
	protected void cloneTableColumns(Table table) {
		List columns = getSubWidgets();
		for(int i = 0; i< columns.size(); i++) {
			TableColumn column = (TableColumn)columns.get(i);
			table.addTableColumn(column.clone());
		}
	}
	
	
	@Override
	public void load(Object parent, Element element, PersistMapping mapping)
			throws Exception {
		super.load(parent, element, mapping);
		this.rowHeight = XmlUtil.getIntValue(element, "rowHeight", this.rowHeight);
		this.keepSelection = XmlUtil.getBooleanValue(element, "keepSelection", this.keepSelection);
		List list = element.getChildren("TableColumn");
		for(int i = 0; i < list.size(); i++) {
			Element el = (Element)list.get(i);
			TableColumn column = new TableColumn();
			column.load(this, el, mapping);
		}
		layoutSubWidgets();
	}
	
	@Override
	public Element toXml(PersistMapping mapping) throws Exception {
		Element element = super.toXml(mapping);
		element.setAttribute(new Attribute("rowHeight", String.valueOf(this.rowHeight)));
		element.setAttribute(XmlUtil.getBooleanAttribute("keepSelection", this.keepSelection));
		List columns = getSubWidgets();
		for(int i = 0; i< columns.size(); i++) {
			TableColumn column = (TableColumn)columns.get(i);
			element.addContent(column.toXml(mapping));
		}
		return element;
	}
	
	@Override
	public String getDefaultName() {
		return "tbl";
	}
	
	public void addTableColumn(TableColumn column) {
		column.setTable(this);
		super.addSubWidget(column);
	}
	
	public void removeTableColumn(TableColumn column) {
		column.setTable(null);
		super.removeSubWidget(column);
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
