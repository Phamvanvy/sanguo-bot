package com.pip.uieditor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.jdom.Attribute;
import org.jdom.Element;

import com.pip.uieditor.model.annotation.Property;
import com.pip.uieditor.model.classic.type.ARGBPropertyType;
import com.pip.uieditor.model.classic.type.BooleanPropertyType;
import com.pip.uieditor.model.classic.type.EnumPropertyType;
import com.pip.uieditor.model.classic.type.IntegerPropertyType;
import com.pip.uieditor.model.classic.type.PropertyDef;
import com.pip.uieditor.model.classic.type.StringPropertyType;
import com.pip.uieditor.model.persist.PersistMapping;
import com.pip.uieditor.model.persist.XmlUtil;
import com.pip.uieditor.model.propertydescriptor.BooleanPropertyDescriptor;
import com.pip.uieditor.model.propertydescriptor.CanvasPropertyDescriptor;
import com.pip.uieditor.model.propertydescriptor.ColorPropertyDescriptor;
import com.pip.uieditor.model.propertydescriptor.FloatPropertyDescriptor;
import com.pip.uieditor.model.propertydescriptor.InsetsPropertyDescriptor;
import com.pip.uieditor.model.propertydescriptor.IntPropertyDescriptor;
import com.pip.uieditor.model.propertydescriptor.StatePropertyDescriptor;

/**
 * Widget所有的控件类的基类
 * @author Jeffrey
 *
 */
public class  Widget extends UIObject{
	
	public static Widget PROPERTY = new Widget("");
	
	public static final int CANVAS_UI = 0;
	public static final int CANVAS_SCREEN = 1;
	
	private static final String[] EMPTY_EVENTS = new String[0];
	
	public static final int LAYER_BACKGROUND = 0;
	public static final int LAYER_BORDER = 1;
	public static final int LAYER_ARTWORK = 2;
	public static final int LAYER_OVERLAY = 3;
	
	public static final int MAX_LAYER_COUNT = 4;
	
	//state
	public static final int DISABLED = 0x00000002;
	public static final int PUSHED =  0x00000004;
	public static final int HIGHLIGHT = 0x00000008;
	public static final int SELECTED = 0x000000010;
	public static final int FOCUSED = 0x000000020;
	public static final int CHECKED = 0x000000040;
	
	public static final int STATE_CUSTOM1 = 0x10000000;
	public static final int STATE_CUSTOM2 = 0x20000000;
	public static final int STATE_CUSTOM3 = 0x40000000;
	public static final int STATE_CUSTOM4 = 0x80000000;
	
	
	//widgetFlags
	static final int FOCUSABLE = 0x00000001;
	static final int INVISIBLE = 0x00000004;
	static final int SCROLLBARS_HORIZONTAL = 0x00000100;
	static final int SCROLLBARS_VERTICAL = 0x00000200;
	static final int CLICKABLE = 0x00004000;
	static final int LONG_CLICKABLE = 0x00200000;
	static final int SCROLL_CONTAINER = 0x00080000;
	
	
//	Container parent;
//	
//	private String type;
	
	private Map<PropertyDef,Object> properties;
	
	@Property(type=BooleanPropertyDescriptor.class)
	protected boolean relocation = false;
	
	@Property(type=BooleanPropertyDescriptor.class)
	protected boolean resize = true;
	
	@Property(type=InsetsPropertyDescriptor.class)
	private Insets border = null;
	
	@Property(type=StatePropertyDescriptor.class)
	private int state;
	
	private List subWidgets = null;
	
	private List<Region> regions = null;
	
	private List<Widget> widgets = new ArrayList<Widget>();
	
	@Property(type=IntPropertyDescriptor.class)
	private int scrollBarWidth = 10;
	
	@Property(type=ColorPropertyDescriptor.class)
	private ARGB scrollBarColor = new ARGB(0x80, 0x80, 0x80, 0x80);
	
	@Property(type=BooleanPropertyDescriptor.class)
	private boolean longClickable;
	
	@Property(type=BooleanPropertyDescriptor.class)
	private boolean initVisible = true;
	
	@Property(type=BooleanPropertyDescriptor.class)
	private boolean initAddToParent = true;
	
	@Property(type=CanvasPropertyDescriptor.class)
	private int ownerCanvas = 0; //默认是属于uicanvas
	
	@Property(type=FloatPropertyDescriptor.class)
	private float verticalFlipFactor = 0.5f;
	
	@Property(type=FloatPropertyDescriptor.class)
	private float horizontalFlipFactor = 0.5f;
	
	private int widgetFlags;
	
	public Widget(String type) {
		super(); 
		setType(type);
		this.border = new Insets();
		this.regions = new ArrayList<Region>(5);
		this.subWidgets = new ArrayList();
		this.state = 0;
		initFlags();
	}
	
	protected void initFlags() {
		
	}
	
	
	public int getOwnerCanvas() {
		return ownerCanvas;
	}

	public void setOwnerCanvas(int ownerCanvas) {
		this.ownerCanvas = ownerCanvas;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return super.getPropertyDescriptors();
//		IPropertyDescriptor[] ret = cache.get(getClass().getName() + "$" + type.getName());
//		if(ret == null) {
//			List<IPropertyDescriptor> descs = new ArrayList<IPropertyDescriptor>();
//			for(PropertyDef def : type.getPropertyDefs()) {
//				descs.add(createPropertyDescriptor(def));
//			}
//			for(IPropertyDescriptor desc : PropertyHelper.createPropertyDescriptors(getClass())) {
//				descs.add(desc);
//			}
//			ret = descs.toArray(new IPropertyDescriptor[descs.size()]);
//			cache.put(getClass().getName() + "$" + type.getName(), ret);
//		}
//		return ret;
			
	}

	@Override
	public Object getPropertyValue(Object id) {
		return super.getPropertyValue(id);
//		PropertyDef def = type.getPropertyDef((String) id);
//		if (def != null) {
//			Object ret = properties.get(def);
//			if (ret == null) {
//				ret = def.getDefaultValue();
//			}
//			return ret;
//		} else {
//			return super.getPropertyValue(id);
//		}
	}


	@Override
	public void setPropertyValue(Object id, Object value) {
		Object o = getPropertyValue(id);
		if (o != null && value != null && o.equals(value))
			return;
//		PropertyDef def = type.getPropertyDef((String) id);
//		if (def != null) {
//			properties.put(def, value);
//			firePropertyChange(def.getId(), o, value);
//		} else {
			super.setPropertyValue(id, value);
//		}

	}
	
	
	protected void calcAlign() {
		if(getParent() == null)
			return;
//		if(v_align == Align.TOP_MIDDLE) {
//			setVParam1(calcParam(bounds.y, getParent().getHeight(), v_unit1));
//			setVParam2(calcParam(bounds.y + getHeight()/2, getParent().getHeight(), v_unit2));
//		}
//		if(v_align == Align.MIDDLE_BOTTOM) {
//			setVParam1(calcParam(bounds.y + getHeight()/2, getParent().getHeight(), v_unit1));
//			setVParam2(calcParam(bounds.y + getHeight(), getParent().getHeight(), v_unit2));
//		}
//		if(v_align == Align.TOP_BOTTOM) {
//			setVParam1(calcParam(bounds.y, getParent().getHeight(), v_unit1));
//			setVParam2(calcParam(bounds.y + getHeight(), getParent().getHeight(), v_unit2));
//		}
//		if(h_align == Align.LEFT_CENTER) {
//			setHParam1(calcParam(bounds.x, getParent().getWidth(), h_unit1));
//			setHParam2(calcParam(bounds.x + getWidth()/2, getParent().getWidth(), h_unit2));
//		}
//		if(h_align == Align.CENTER_RIGHT) {
//			setHParam1(calcParam(bounds.x + getWidth()/2, getParent().getWidth(), h_unit1));
//			setHParam2(calcParam(bounds.x + getWidth(), getParent().getWidth(), h_unit2));
//		}
//		if(h_align == Align.LEFT_RIGHT) {
//			setHParam1(calcParam(bounds.x, getParent().getWidth(), h_unit1));
//			setHParam2(calcParam(bounds.x + getWidth(), getParent().getWidth(), h_unit2));
//		}
		fireDirty();
	}
	
	public void setName(String name) {
		if (getScreen() != null) {
			Widget w = getScreen().findWidget(name);
			if (w != null && w != this) {
				throw new IllegalArgumentException("Already used");
			}
		}
		super.setName(name);
	}
	
	public int getState() {
		return this.state;
	}
	
	public void setState(int state) {
		if(this.state != state) {
			int old = this.state;
			this.state = state;
			firePropertyChange("state", old, this.state);
		}
	}
	
	public void setIntValue(String id, int value) {
		setPropertyValue(id, value);
	}
	
	public boolean isRelocation() {
		return relocation;
	}

	public void setRelocation(boolean relocation) {
		this.relocation = relocation;
	}

	public boolean isResize() {
		return resize;
	}

	public void setResize(boolean resize) {
		this.resize = resize;
	}

	public void setBooleanValue(String id, boolean value) {
		setPropertyValue(id, value ? 1 : 0);
	}
	
	public boolean getBooleanValue(String id) {
		return ((Integer)getPropertyValue(id)) == 0 ? false : true;
	}
	
	private IPropertyDescriptor createPropertyDescriptor(PropertyDef def) {
		if(def.getPropertyType() instanceof StringPropertyType)  {
			TextPropertyDescriptor desc = new TextPropertyDescriptor(def.getId(), def.getName());
			desc.setCategory(def.getCategory());
			return desc;
		} 
		if(def.getPropertyType() instanceof ARGBPropertyType) {
			ColorPropertyDescriptor desc = new ColorPropertyDescriptor(def.getId(), def.getName());
			desc.setCategory(def.getCategory());
			return desc;
		}
		if(def.getPropertyType() instanceof IntegerPropertyType)  {
			IntPropertyDescriptor desc = new IntPropertyDescriptor(def.getId(), def.getName());
			desc.setCategory(def.getCategory());
			return desc;
		} 
		if(def.getPropertyType() instanceof BooleanPropertyType) {
			BooleanPropertyDescriptor desc = new BooleanPropertyDescriptor(def.getId(), def.getName());
			desc.setCategory(def.getCategory());
			return desc;
		}
		if(def.getPropertyType() instanceof EnumPropertyType) {
			ComboBoxPropertyDescriptor desc = new ComboBoxPropertyDescriptor(def.getId(), def.getName(), ((EnumPropertyType)def.getPropertyType()).getItems());
			desc.setCategory(def.getCategory());
			return desc;
		}
		throw new IllegalArgumentException();
	}
	
	
	protected void fireDirty() {
		Screen screen = getScreen();
		if(screen != null) {
			screen.setDirty();
		}
	}
	

	@Override
	public void setLocation(Point location) {
		if(!this.location.equals(location)) {
			Point old = this.location;
			this.location = location.getCopy();
			firePropertyChange("location", old, location);
			layoutChildren();
		}
	}
	
	@Override
	public void setSize(Dimension size) {
		if(!size.equals(this.size)) {
			Dimension old = this.size;
			this.size = size.getCopy();
			firePropertyChange("size", old, size);
			layoutChildren();
		}
	}
	
	
	public void setScrollBarWidth(int scrollBarWidth) {
		if(this.scrollBarWidth != scrollBarWidth) {
			int old = this.scrollBarWidth;
			this.scrollBarWidth = scrollBarWidth;
			firePropertyChange("scrollBarWidth", old, this.scrollBarWidth);
		}
	}
	
	public int getScrollBarWidth() {
		return this.scrollBarWidth;
	}
	
	public void setScrollBarColor(ARGB color) {
		if(!this.scrollBarColor.equals(color)) {
			ARGB old = this.scrollBarColor;
			this.scrollBarColor = color;
			firePropertyChange("scrollBarColor", old, this.scrollBarColor);
		}
	}
	
	public ARGB getScrollBarColor() {
		return this.scrollBarColor;
	}
	
	protected void layoutChildren() {
		layoutRegions();
		layoutSubWidgets();
		layoutWidgets();
	}
	
	protected void layoutWidgets() {
		
	}
	
	protected void layoutRegions() {
		for(int i = 0; i < getRegionCount(); i++) {
			Region r = getRegion(i);
			r.layout();
		}
	}
	
	
	public Widget getChild(int index) {
		return widgets.get(index);
	}
	
	public void addChild(Widget comp) {
		widgets.add(comp);
		comp.setParent(this);
		firePropertyChange(PROPERTY_CHILD, null, comp);
	}
	
	public List<Widget> getChildren() {
		return this.widgets == null ? Collections.EMPTY_LIST : new ArrayList<Widget>(widgets);
	}
	
	public boolean hashChild(Class childClass) {
		if(this.getClass() == childClass) {
			return true;
		}
		for(int i = 0; i < this.widgets.size(); i++) {
			Widget widget = this.widgets.get(i);
			if(widget.hashChild(childClass))
				return true;
		}
		return false;
	}
	
	public int getChildCount() {
		return this.widgets == null ? 0 : widgets.size();
	}
	
	
	public void removeChild(Widget comp) {
		if(widgets.remove(comp)) {
			comp.setParent(null);
			firePropertyChange(PROPERTY_CHILD, comp, null);
		}
	}
	
	public void removeChildren() {
		widgets.clear();
	}
	
	public void layoutSubWidgets() {
		
	}
	
	public Rectangle getBoundsWithBorder() {
		return new Rectangle(location.x + getParentClientAreaX(), location.y + getParentClientAreaY() , size.width, size.height);
	}
	
	public Point getLocationWithBorder() {
		return location.getTranslated(getParentClientAreaX(), getParentClientAreaY());
	}
	
	@Override
	public Widget getParent() {
		return (Widget)super.getParent();
	}
	
	public int getParentClientAreaX() {
		if(getParent() == null)
			return 0;
		return getParent().getClientAreaX();
	}
	
	public int getParentClientAreaY() {
		if(getParent() == null)
			return 0;
		return getParent().getClientAreaY();
	}
	
	public Point getAbsoluteLocation() {
		Point p = this.location.getCopy();
		Widget c = getParent();
		while (c != null) {
			p.translate(c.getBounds().getLocation());
			c = c.getParent();
		}
		return p;
	}
	
	public Screen getScreen() {
		if (this instanceof Screen)
			return (Screen) this;
		if (getParent() == null) {
			return null;
		}
		if (getParent() instanceof Screen) {
			return (Screen) getParent();
		} else {
			return getParent().getScreen();
		}
	}

	public Rectangle getAbsoluteBounds() {
		return new Rectangle(getAbsoluteLocation(), size);
	}
	
	public boolean isInitVisible() {
		return initVisible;
	}

	public void setInitVisible(boolean initVisible) {
		this.initVisible = initVisible;
	}

	public boolean isInitAddToParent() {
		return initAddToParent;
	}

	public void setInitAddToParnet(boolean initAddToParent) {
		this.initAddToParent = initAddToParent;
	}

	@Override
	public Element toXml(PersistMapping mapping) throws Exception {
		String name = mapping.getMappingName(getClass());
		if(name == null)
			throw new Exception();
		Element element = new Element(name);
		element.setAttribute(XmlUtil.getPointAttribute("location", location));
		element.setAttribute(XmlUtil.getDimensionAttribute("size", size));
		element.setAttribute(XmlUtil.getBooleanAttribute("relocation", relocation));
		element.setAttribute(XmlUtil.getBooleanAttribute("resize", resize));
		element.setAttribute(XmlUtil.getStringAttribute("name", this.name));
		element.setAttribute(XmlUtil.getInsetsAttribute("border", border));
		element.setAttribute(new Attribute("widgetFlags", String.valueOf(this.widgetFlags)));
		element.setAttribute(new Attribute("scrollBarWidth", String.valueOf(this.scrollBarWidth)));
		element.setAttribute(XmlUtil.getARGBAttribute("scrollBarColor", this.scrollBarColor));
		element.setAttribute(XmlUtil.getBooleanAttribute("longClickable", this.longClickable));
		element.setAttribute(XmlUtil.getBooleanAttribute("initVisible", this.initVisible));
		element.setAttribute(XmlUtil.getBooleanAttribute("initAddToParent", this.initAddToParent));
		element.setAttribute(new Attribute("ownerCanvas", String.valueOf(this.ownerCanvas)));
		element.setAttribute(new Attribute("verticalFlipFactor", String.valueOf(this.verticalFlipFactor)));
		element.setAttribute(new Attribute("horizontalFlipFactor", String.valueOf(this.horizontalFlipFactor)));
		for(int i = 0; i < getRegionCount(); i++) {
			Region region = getRegion(i);
			element.addContent(region.toXml(mapping));
		}
		return element;
	}

	@Override
	public void load(Object parent, Element element, PersistMapping mapping)
			throws Exception {
		this.location = XmlUtil.getPoint(element, "location", new Point(0, 0));
		this.size = XmlUtil.getDimension(element, "size", new Dimension(0, 0));
		this.relocation = XmlUtil.getBooleanValue(element, "relocation", this.relocation);
		this.resize = XmlUtil.getBooleanValue(element, "resize", this.resize);
		this.name = XmlUtil.getStringValue(element, "name", name);
		this.border = XmlUtil.getInsetsValue(element, "border", this.border);
		this.widgetFlags = XmlUtil.getIntValue(element, "widgetFlags", this.widgetFlags);
		this.scrollBarWidth = XmlUtil.getIntValue(element, "scrollBarWidth", this.scrollBarWidth);
		this.scrollBarColor = XmlUtil.getARGB(element, "scrollBarColor", this.scrollBarColor);
		this.longClickable = XmlUtil.getBooleanValue(element, "longClickable", this.longClickable);
		this.initVisible = XmlUtil.getBooleanValue(element, "initVisible", this.initVisible);
		this.initAddToParent = XmlUtil.getBooleanValue(element, "initAddToParent", this.initAddToParent);
		this.ownerCanvas = XmlUtil.getIntValue(element, "ownerCanvas", this.ownerCanvas);
		this.verticalFlipFactor = XmlUtil.getFloatValue(element, "verticalFlipFactor", this.verticalFlipFactor);
		this.horizontalFlipFactor = XmlUtil.getFloatValue(element, "horizontalFlipFactor", this.horizontalFlipFactor);
		List l = element.getChildren();
		for(int i = 0; i < l.size(); i++) {
			Element el = (Element)l.get(i);
			if(el.getName().endsWith("Region")) {
				Region region = (Region)loadUIObject(this, el, mapping);
				if(!replaceRegion(region)){
					addRegion(region);
				}
			}
		}
	}
	
	
	
	public float getVerticalFlipFactor() {
		return verticalFlipFactor;
	}

	public void setVerticalFlipFactor(float verticalFlipFactor) {
		this.verticalFlipFactor = verticalFlipFactor;
	}

	public float getHorizontalFlipFactor() {
		return horizontalFlipFactor;
	}

	public void setHorizontalFlipFactor(float horizontalFlipFactor) {
		this.horizontalFlipFactor = horizontalFlipFactor;
	}

	private boolean replaceRegion(Region region) {
		for(int i = 0 ; i< regions.size(); i++) {
			Region r = (Region)regions.get(i);
			if(r.getId().equals(region.getId())) {
				region.setParent(this);
				region.setRequire(true);
				this.regions.set(i, region);
				return true;
			}
		}
		return false;
	}
	
	public void addRegion(Region region) {
		int index = 0;
		for(int i = 0; i < getRegionCount(); i++) {
			Region r = getRegion(i);
			if(r.getLayer() > region.getLayer()) {
				index = i;
				break;
			} else {
				index++;
			}
		}
		addRegion(index, region);
	}
	
	public void addRegion(int index, Region region) {
		region.setParent(this);
		regions.add(index, region);
		firePropertyChange("child", null, region);
	}
	
	public void removeRegion(Region region) {
		regions.remove(region);
		firePropertyChange("child", region, null);
	}
	
	public void upLayer(Region region) {
		if(region.getLayer() < Widget.LAYER_OVERLAY && !region.isRequire()) {
			removeRegion(region);
			region.setLayer(region.getLayer() + 1);
			addRegion(region);
		}
	}
	
	public void downLayer(Region region) {
		if(region.getLayer() > Widget.LAYER_BACKGROUND && !region.isRequire()) {
			removeRegion(region);
			region.setLayer(region.getLayer() - 1);
			addRegion(region);
		}
	}
	
	public void upRegion(Region region) {
		if(!region.isRequire()) {
			int index = getRegionIndex(region);
			if(index > 0) {
				Region r = getRegion(index - 1);
				if(!r.isRequire() && r.getLayer() == region.getLayer()) {
					removeRegion(region);
					
					addRegion(index - 1, region);
				}
			}
		}
	}
	
	public void downRegion(Region region) {
		if(!region.isRequire()) {
			int index = getRegionIndex(region);
			if(regions.size() >= 2 && index < regions.size() - 1) {
				Region r = getRegion(index + 1);
				if(!r.isRequire() && r.getLayer() == region.getLayer()) {
					removeRegion(region);
					addRegion(index + 1, region);
				}
			}
		}
	}
	
	public Region getRegion(String id) {
		for(int i = 0; i < regions.size(); i++) {
			Region region = regions.get(i);
			if(id.equals(region.getId())) 
				return region;
		}
		return null;
	}
	
	public Region getRegion(int index) {
		return regions.get(index);
	}
	
	public int getRegionCount() {
		return regions.size();
	}
	
	public int getRegionIndex(Region region) {
		return regions.indexOf(region);
	}
	
	public List<Region> getRegions() {
		return Collections.unmodifiableList(regions);
	}
	
	
	
	public Widget clone() {
		throw new UnsupportedOperationException();
	}
	
	public void fillCloneWidget(Widget widget) {
		widget.location = this.location.getCopy();
		widget.size = this.size.getCopy();
		widget.name = name;
		widget.regions.clear();
		for(Region region : regions) {
			Region clone = region.clone();
			widget.regions.add(clone);
			clone.setParent(widget);
		}
		widget.relocation =  this.relocation;
		widget.resize = this.resize;
		widget.border = new Insets(this.border);
		widget.widgetFlags = this.widgetFlags;
		widget.scrollBarColor = this.scrollBarColor.getCopy();
		widget.scrollBarWidth = this.scrollBarWidth;
		widget.longClickable = this.longClickable;
		widget.initVisible = this.initVisible;
		widget.initAddToParent = this.initAddToParent;
		widget.ownerCanvas = this.ownerCanvas;
		widget.horizontalFlipFactor = this.horizontalFlipFactor;
		widget.verticalFlipFactor = this.verticalFlipFactor;
	}
	
	public String getDefaultName() {
		throw new UnsupportedOperationException();
	}
	
	public int getClientAreaX() {
		return border.left;
	}
	
	public int getClientAreaY() {
		return border.top;
	}
	
	public int getClientAreaWidth() {
		return this.size.width - border.left - border.right;
	}
	
	public int getClientAreaHeight() {
		return this.size.height - border.top - border.bottom;
	}
	
	public Insets getBorder() {
		return this.border;
	}
	
	public void setBorder(Insets border) {
		if(!this.border.equals(border)) {
			Insets oldValue = this.border;
			this.border = border;
			firePropertyChange("border", oldValue, this.border);
		}
	}
	
	public List getSubWidgets() {
		return this.subWidgets;
	}
	
	public void addSubWidget(Object subWidget) {
		this.subWidgets.add(subWidget);
		firePropertyChange("child", null, subWidget);
	}
	
	public void removeSubWidget(Object subWidget) {
		this.subWidgets.remove(subWidget);
		firePropertyChange("child", subWidget, null);
	}
	
	public int getSubWidgetCount() {
		return this.subWidgets.size();
	}
	
	public Object getSubWidget(int index) {
		return this.subWidgets.get(index);
	}
	
	public String[] getEvents() {
		return EMPTY_EVENTS;
	}
	
	public Widget findWidget(String name) {
		if(name.equals(this.name)) {
			return this;
		}
		for(int i = 0; i < getChildCount(); i++) {
			Widget widget = getChild(i);
			Widget ret = widget.findWidget(name);
			if(ret != null)
				return ret;
		}
		return null;
	}
	
	public boolean isScrollContainer() {
		return (this.widgetFlags & SCROLL_CONTAINER) != 0;
	}
	
	public void setScrollContainer(boolean scrollContainer) {
		if(isScrollContainer() != scrollContainer) {
			if(scrollContainer) {
				this.widgetFlags |= SCROLL_CONTAINER;
			} else {
				this.widgetFlags &= ~SCROLL_CONTAINER;
			}
		}
	}
	
	public boolean isLongClickable() {
		return this.longClickable;
	}
	
	public void setLongClickable(boolean clickable) {
		if(this.longClickable != clickable) {
			this.longClickable =  clickable;
			firePropertyChange("longClickable", !this.longClickable, this.longClickable);
		}
	}
	
	public boolean isClickable() {
		return (this.widgetFlags & CLICKABLE) != 0;
	}
	
	public void setClickable(boolean clickable) {
		if(isClickable() != clickable) {
			if(clickable) {
				this.widgetFlags |= CLICKABLE;
			} else {
				this.widgetFlags &= ~CLICKABLE;
			}
		}
	}
	
	public boolean isVerticalScrollBarEnabled() {
		return (this.widgetFlags & SCROLLBARS_VERTICAL) != 0;
	}
	
	public void setVerticalScrollBarEnabled(boolean enabled) {
		if(isVerticalScrollBarEnabled() != enabled) {
			if(enabled) {
				this.widgetFlags |= SCROLLBARS_VERTICAL;
			} else {
				this.widgetFlags &= ~SCROLLBARS_VERTICAL;
			}
		}
	}
	
	
	public boolean isHorizontalScrollBarEnabled() {
		return (this.widgetFlags & SCROLLBARS_HORIZONTAL) != 0;
	}
	
	public void setHorizontalScrollBarEnabled(boolean enabled) {
		if(isHorizontalScrollBarEnabled() != enabled) {
			if(enabled) {
				this.widgetFlags |= SCROLLBARS_HORIZONTAL;
			} else {
				this.widgetFlags &= ~SCROLLBARS_HORIZONTAL;
			}
		}
	}
	
	public boolean isFocusable() {
		return (this.widgetFlags & FOCUSABLE) != 0;
	}
	
	public void setFocusable(boolean focusable) {
		if(isFocusable() != focusable) {
			if(focusable) {
				this.widgetFlags &= ~FOCUSABLE;
			} else{
				this.widgetFlags |= FOCUSABLE;
			}
		}
	}
}
