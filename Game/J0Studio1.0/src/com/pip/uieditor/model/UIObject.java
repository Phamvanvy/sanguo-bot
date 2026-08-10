package com.pip.uieditor.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;

import org.apache.commons.beanutils.PropertyUtils;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.jdom.Element;

import com.pip.uieditor.model.annotation.Property;
import com.pip.uieditor.model.annotation.PropertyHelper;
import com.pip.uieditor.model.persist.PersistMapping;
import com.pip.uieditor.model.propertydescriptor.DimensionPropertyDescriptor;
import com.pip.uieditor.model.propertydescriptor.PointPropertyDescriptor;

public abstract class UIObject implements IPropertySource{
	
	public static final String PROPERTY_CHILD = "child";
	
	protected static HashMap<String, IPropertyDescriptor[]> cache = new HashMap<String, IPropertyDescriptor[]>();

	private PropertyChangeSupport listeners;
	
	private UIObject parent;
	
	private boolean showInEditing;
	
	@Property(type=TextPropertyDescriptor.class)
	protected String name;
	
	private String type;
	
	@Property(type=PointPropertyDescriptor.class)
	protected Point location;
	
	@Property(type=DimensionPropertyDescriptor.class)
	protected Dimension size;
	
	public UIObject() {
		this.location = new Point(0, 0);
		this.size = new Dimension(0, 0);
		this.showInEditing = true;
	}
	
	public void setBounds(Rectangle bounds) {
		setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
	}
	
	public void setBounds(int x, int y ,int width, int height) {
		setLocation(new Point(x, y));
		setSize(new Dimension(width, height));
	}
	
	public Point getLocation() {
		return this.location.getCopy();
	}
	
	public void setLocation(Point point) {
		if(!point.equals(location)) {
			Point old = location.getCopy();
			location = point.getCopy();
			firePropertyChange("location", old, location);
		}
	}
	
	public void setSize(Dimension size) {
		if(!size.equals(this.size)) {
			Dimension old = this.size.getCopy();
			this.size = size.getCopy();
			firePropertyChange("size", old, size);
		}
	}
	
	public Dimension getSize() {
		return this.size.getCopy();
	}
	
	public Rectangle getBounds() {
		return new Rectangle(location, size);
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return this.type;
	}
	
	public void setName(String name) {
		String old = this.name;
		this.name = name;
		firePropertyChange("name", old, this.name);
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setShowInEditing(boolean showInEditing) {
		if(this.showInEditing != showInEditing) {
			this.showInEditing = showInEditing;
			firePropertyChange("showInEditing", !this.showInEditing, this.showInEditing);
		}
	}
	
	public boolean isShowInEditing() {
		return this.showInEditing;
	}
	
	public UIObject getParent() {
		return this.parent;
	}
	
	public void setParent(UIObject parent) {
		this.parent = parent;
	}
	
//	public List<UIObject> getChildren() {
//		return children == null ? Collections.EMPTY_LIST:new ArrayList(children);
//	}
//	
//	public int getChildCount() {
//		return children == null ? 0 : children.size();
//	}
//	
//	public UIObject getChild(int index) {
//		return children.get(index);
//	}
//	
//	public UIObject removeChild(int index) {
//		UIObject ret = children.remove(index);
//		if(ret != null) {
//			firePropertyChange("child", ret, null);
//		}
//		return ret;
//	}
//	
//	public void removeChild(UIObject child) {
//		boolean succ = children.remove(child);
//		if(succ) {
//			firePropertyChange("child", child, null);
//		}
//	}
//	
//	public void addChild(UIObject child) {
//		children.add(child);
//		child.setParent(this);
//		firePropertyChange("child", null, child);
//	}
//	
//	public void addChild(int index, UIObject child) {
//		children.add(index, child);
//		child.setParent(this);
//		firePropertyChange("child", null, child);
//	}
	
	public void addPropertyChangeListener(PropertyChangeListener l) {
		if (listeners == null)
			listeners = new PropertyChangeSupport(this);
		listeners.addPropertyChangeListener(l);
	}
	
	protected void firePropertyChange(String prop, Object old, Object newValue) {
		if (listeners != null)
			listeners.firePropertyChange(prop, old, newValue);
		if(!prop.equals(Screen.PROPERTY_DIRTY))
			fireDirty();
	}
	
	protected abstract void fireDirty();

	public void removePropertyChangeListener(PropertyChangeListener l) {
		if (listeners != null)
			listeners.removePropertyChangeListener(l);
	}
	

	@Override
	public Object getEditableValue() {
		return this;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		IPropertyDescriptor[] ret = cache.get(getClass().getName());
		if(ret == null) {
			ret = PropertyHelper.createPropertyDescriptors(getClass());
			cache.put(getClass().getName(), ret);
		}
		return ret;
	}

	@Override
	public Object getPropertyValue(Object id) {
		try {
			return PropertyUtils.getProperty(this, (String)id);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean isPropertySet(Object id) {
		return false;
	}

	@Override
	public void resetPropertyValue(Object id) {
		
	}

	@Override
	public void setPropertyValue(Object id, Object value) {
		try {
			Object oldValue = PropertyUtils.getProperty(this, (String)id);
			PropertyUtils.setProperty(this, (String)id, value);
			firePropertyChange((String)id, oldValue, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public abstract Element toXml(PersistMapping mapping) throws Exception;
	
	public abstract void load(Object parent, Element element, PersistMapping mapping) throws Exception;
	
	protected static UIObject loadUIObject(Object parent, Element element, PersistMapping mapping) throws Exception{
		String name = element.getName();
		Class<? extends UIObject> klass = mapping.getMappingKlass(name);
		if(klass == null)
			throw new Exception();
		UIObject uo = klass.newInstance();
		uo.load(parent, element, mapping);
		return uo;
	}
}
