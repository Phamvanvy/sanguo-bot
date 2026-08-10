package com.pip.uieditor.model;

import org.jdom.Attribute;
import org.jdom.Element;

import com.pip.uieditor.model.annotation.Property;
import com.pip.uieditor.model.persist.PersistMapping;
import com.pip.uieditor.model.persist.XmlUtil;
import com.pip.uieditor.model.propertydescriptor.AnchorPropertyDescriptor;
import com.pip.uieditor.model.propertydescriptor.BooleanPropertyDescriptor;
import com.pip.uieditor.model.propertydescriptor.IntPropertyDescriptor;

public class TableColumn extends UIObject {
	
	@Property(type=IntPropertyDescriptor.class)
	private int preferredWidth;
	
	@Property(type=BooleanPropertyDescriptor.class)
	private boolean flexible;
	
	@Property(type=AnchorPropertyDescriptor.class)
	private int anchor;
	
	@Property(type=IntPropertyDescriptor.class)
	private int xoffset;
	
	@Property(type=BooleanPropertyDescriptor.class)
	private boolean canPush;
	
	private Table table;
	
	
	public TableColumn() {
		this(20, true);
	}
	
	public TableColumn(int preferredWidth, boolean flexible) {
		this.preferredWidth = preferredWidth;
		this.flexible = flexible;
		this.anchor = Anchor.CENTER;
		this.xoffset = 0;
		this.canPush = false;
	}
	
	public void setTable(Table table) {
		this.table = table;
	}
	
	public Table getTable() {
		return this.table;
	}

	@Override
	protected void fireDirty() {

	}
	
	
	public int getAnchor() {
		return anchor;
	}

	public void setAnchor(int anchor) {
		this.anchor = anchor;
	}
	
	public int getXoffset() {
		return xoffset;
	}
	
	public void setXoffset(int xoffset) {
		this.xoffset = xoffset;
	}
	
	public boolean isCanPush() {
		return canPush;
	}
	
	public void setCanPush(boolean canPush) {
		this.canPush = canPush;
	}

	public int getPreferredWidth() {
		return this.preferredWidth;
	}
	
	public void setPreferredWidth(int preferredWidth) {
		if(this.preferredWidth != preferredWidth) {
			int oldValue = this.preferredWidth;
			this.preferredWidth = preferredWidth;
			firePropertyChange("preferredWidth", oldValue, this.preferredWidth);
		}
	}
	
	public void setFlexible(boolean flexible) {
		if(this.flexible != flexible) {
			this.flexible = flexible;
			firePropertyChange("flexible", !this.flexible, this.flexible);
		}
	}
	
	public boolean isFlexible() {
		return this.flexible;
	}

	@Override
	public Element toXml(PersistMapping mapping) throws Exception {
		String name = mapping.getMappingName(getClass());
		if(name == null)
			throw new Exception();
		Element element = new Element(name);
		element.setAttribute(new Attribute("preferredWidth", String.valueOf(this.preferredWidth)));
		element.setAttribute(XmlUtil.getBooleanAttribute("flexible", this.flexible));
		element.setAttribute(new Attribute("anchor", String.valueOf(this.anchor)));
		element.setAttribute(new Attribute("xoffset", String.valueOf(this.xoffset)));
		element.setAttribute(XmlUtil.getBooleanAttribute("canPush", this.canPush));
		return element;
	}

	@Override
	public void load(Object parent, Element element, PersistMapping mapping)
			throws Exception {
		Table table = (Table)parent;
		this.preferredWidth = XmlUtil.getIntValue(element, "preferredWidth", this.preferredWidth);
		this.flexible = XmlUtil.getBooleanValue(element, "flexible", this.flexible);
		this.anchor = XmlUtil.getIntValue(element, "anchor", this.anchor);
		this.xoffset = XmlUtil.getIntValue(element, "xoffset", this.xoffset);
		this.canPush = XmlUtil.getBooleanValue(element, "canPush", this.canPush);
		table.addTableColumn(this);
	}
	
	
	public TableColumn clone() {
		TableColumn ret = new TableColumn(this.preferredWidth, this.flexible);
		ret.anchor = anchor;
		ret.xoffset = xoffset;
		ret.canPush = canPush;
		return ret;
	}
}
