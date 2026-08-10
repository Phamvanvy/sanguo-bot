package com.pip.uieditor.model.classic;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import com.pip.uieditor.model.ARGB;
import com.pip.uieditor.model.classic.type.Type;

public class GWidget {
	
	private GWidget parent;
	private HashMap<String, IProperty> properties = new HashMap<String, IProperty>();
	
	private Type type;
	
	private List<GWidget> children = new ArrayList<GWidget>();
	
	private Point location = new Point(0, 0);
	private Dimension size = new Dimension(0, 0);
	
	private Dimension preferedSize = new Dimension(-1, -1);
	
	private PropertyChangeSupport listeners;
	
	private boolean borderPainted;
	
	private boolean backgroundPainted;
	
	private boolean selectionPainted;
	
	private boolean visible = true;
	
	private boolean focusable = true;
	
	private boolean enabled = true;
	
	private boolean supportSelect;
	
	private ARGB backgroundColor = new ARGB(0, 0, 0, 0);
	
	private ARGB foregroundColor = new ARGB(0, 0, 0, 0);
	
	private ARGB borderColor = new ARGB(0, 0, 0, 0);
	
	private ARGB selectionColor = new ARGB(0, 0, 0, 0);
	
	private Rectangle border = new Rectangle();
	
	private Rectangle inset = new Rectangle();

	public GWidget(GWidget parent, Type type) {
		this.parent = parent;
		this.type = type;
	}
	
	public GWidget(Type type) {
		this(null, type);
	}
	
	public void addChild(GWidget widget) {
		children.add(widget);
		widget.setParent(this);
		firePropertyChange("child", null, widget);
	}
	
	
	public void addPropertyChangeListener(PropertyChangeListener l) {
		if (listeners == null)
			listeners = new PropertyChangeSupport(this);
		listeners.addPropertyChangeListener(l);
	}
	
	void firePropertyChange(String prop, Object old, Object newValue) {
		if (listeners != null)
			listeners.firePropertyChange(prop, old, newValue);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener l) {
		if (listeners != null)
			listeners.removePropertyChangeListener(l);
	}
	
	
	public void setLocation(Point point) {
		if(!this.location.equals(point)) {
			Point old = this.location;
			this.location = point.getCopy();
			firePropertyChange("location", old, this.location);
		}
	}
	
	public void setSize(Dimension dim) {
		if(this.size.equals(dim)) {
			Dimension old = this.size;
			this.size = dim.getCopy();
			firePropertyChange("size", old, this.location);
		}
	}
	
	public void setPreferedSize(Dimension dim) {
		if(!this.preferedSize.equals(dim)) {
			Dimension old = this.preferedSize;
			this.size = dim.getCopy();
			firePropertyChange("preferedSize", old, this.size);
		}
	}
	
	public List<IProperty> getPropertyList() {
		return new ArrayList<IProperty>(properties.values());
	}
	
	public IProperty getProperty(String name) {
		return properties.get(name);
	}
	
	public void addProperty(IProperty property) {
		properties.put(property.getName(), property);
		property.setOwner(this);
	}
	
	public String getName() {
		return type.getName();
	}
	
	public GWidget getParent() {
		return parent;
	}
	
	void setParent(GWidget parent) {
		this.parent = parent;
	}
	
	public boolean isContainer() {
		return type.isContainer();
	}

	public boolean isBorderPainted() {
		return borderPainted;
	}

	public void setBorderPainted(boolean borderPainted) {
		this.borderPainted = borderPainted;
	}

	public boolean isBackgroundPainted() {
		return backgroundPainted;
	}

	public void setBackgroundPainted(boolean backgroundPainted) {
		this.backgroundPainted = backgroundPainted;
	}

	public boolean isSelectionPainted() {
		return selectionPainted;
	}

	public void setSelectionPainted(boolean selectionPainted) {
		if(this.selectionPainted != selectionPainted) {
			this.selectionPainted = selectionPainted;
			firePropertyChange("selectionPainted", !this.selectionPainted, this.selectionPainted);
		}
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		if(this.visible != visible) {
			this.visible = visible;
			firePropertyChange("visible", !this.visible, this.visible);
		}
	}

	public boolean isFocusable() {
		return focusable;
	}

	public void setFocusable(boolean focusable) {
		if(this.focusable != focusable) {
			this.focusable = focusable;
			firePropertyChange("focusable", !this.focusable, this.focusable);
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		if(this.enabled != enabled) {
			this.enabled = enabled;
			firePropertyChange("enabled", !this.enabled , this.enabled);
		}
	}

	public boolean isSupportSelect() {
		return supportSelect;
	}

	public void setSupportSelect(boolean supportSelect) {
		if(this.supportSelect != supportSelect) {
			this.supportSelect = supportSelect;
			firePropertyChange("supportSelect", !this.supportSelect, this.supportSelect);
		}
	}

	public ARGB getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(ARGB backgroundColor) {
		if(!this.backgroundColor.equals(backgroundColor)) {
			ARGB old = this.backgroundColor;
			this.backgroundColor = backgroundColor;
			firePropertyChange("backgroundColor", old, this.backgroundColor);
		}
	}

	public ARGB getForegroundColor() {
		return foregroundColor;
	}

	public void setForegroundColor(ARGB foregroundColor) {
		if(!this.foregroundColor.equals(foregroundColor)) {
			ARGB old = this.foregroundColor;
			this.foregroundColor = foregroundColor;
			firePropertyChange("foregroundColor", old, this.foregroundColor);
		}
	}

	public ARGB getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(ARGB borderColor) {
		if(!this.borderColor.equals(borderColor)) {
			ARGB old = this.borderColor;
			this.borderColor = borderColor;
			firePropertyChange("borderColor", old, this.borderColor);
		}
	}

	public ARGB getSelectionColor() {
		return selectionColor;
	}

	public void setSelectionColor(ARGB selectionColor) {
		if(!this.selectionColor.equals(selectionColor)) {
			ARGB old = this.selectionColor;
			this.selectionColor = selectionColor;
			firePropertyChange("selectionColor", old, this.selectionColor);
		}
	}

	public Rectangle getBorder() {
		return border;
	}

	public void setBorder(Rectangle border) {
		if(!this.border.equals(border)) {
			Rectangle old = this.border;
			this.border = border;
			firePropertyChange("border", old, this.border);
		}
	}

	public Rectangle getInset() {
		return inset;
	}

	public void setInset(Rectangle inset) {
		if(!this.inset.equals(inset)) {
			Rectangle old = this.inset;
			this.inset = inset;
			firePropertyChange("inset", old, this.inset);
		}
	}
	
	public void setProperty(String name, Object value) {
		IProperty pro = getProperty(name);
		if(pro != null) {
			pro.setValue(value);
		}
	}
	
}
