package com.pip.uieditor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.jdom.Attribute;
import org.jdom.Element;

import com.pip.uieditor.model.annotation.Property;
import com.pip.uieditor.model.persist.PersistMapping;
import com.pip.uieditor.model.persist.XmlUtil;
import com.pip.uieditor.model.propertydescriptor.AnchorsPropertyDescriptor;
import com.pip.uieditor.model.propertydescriptor.RegionStatePropertyDescriptor;

public abstract class Region extends UIObject{
	
	public static final String LAYER_STRING[] = {"BACKGROUND", "BORDER", "ARTWORK", "OVERLAY"};
	
	@Property(type=TextPropertyDescriptor.class)
	private String id;
	
	@Property(type=AnchorsPropertyDescriptor.class)
	private List<AnchorPoint> anchorPoints;
	
	private boolean valid;
	
	
	private int layer;
	
	private boolean required;
	
	private boolean visible = true;
	
	@Property(type=RegionStatePropertyDescriptor.class)
	private long mask = 0;
	
	public Region(String id) {
		this.id = id;
		this.anchorPoints = new ArrayList<AnchorPoint>();
	}
	
	public void setLayer(int layer) {
		if(this.layer != layer) {
			this.layer = layer;
		}
	}
	
	
	public Widget getParent() {
		return (Widget)super.getParent();
	}
	
	public int getLayer() {
		return layer;
	}
	
	public String getLayerString() {
		return LAYER_STRING[this.layer];
	}
	
	public void setId(String id) {
		String oldId = this.id;
		this.id = id;
		firePropertyChange("id", oldId, this.id);
	}
	
	public String getId() {
		return this.id;
	}
	
	
	public boolean isValid() {
		return this.valid;
	}
	
	public void invalidate() {
		this.valid = false;
	}
	
	public void validate() {
		layout();
		this.valid = true;
	}
	
	public boolean isVisible() {
		return this.visible;
	}
	
	public void setVisible(boolean visible) {
		if(this.visible != visible) {
			this.visible = visible;
			firePropertyChange("visible", !this.visible, this.visible);
		}
	}
	
	public void setMask(long mask) {
		if(this.mask != mask) {
			long old = this.mask;
			this.mask = mask;
			firePropertyChange("mask", old, this.mask);
		}
	}
	
	public long getMask() {
		return this.mask;
	}
	
	public void setAnchorPoints(List<AnchorPoint> anchorPoints) {
		List<AnchorPoint> old = this.anchorPoints;
		this.anchorPoints = anchorPoints;
		invalidate();
		firePropertyChange("anchorPoints", old, this.anchorPoints);
	}
	
	public void addAnchorPoint(AnchorPoint anchorPoint) {
		if(anchorPoint == null)
			throw new IllegalArgumentException();
		if(getAnchorPoint(anchorPoint.getAnchor()) != null) 
			throw new IllegalArgumentException();
		List<AnchorPoint> old = Collections.unmodifiableList(this.anchorPoints);
		this.anchorPoints.add(anchorPoint);
		invalidate();
		firePropertyChange("anchorPoints", old, this.anchorPoints);
	}
	
	public AnchorPoint getAnchorPoint(int point) {
		for(AnchorPoint ap : anchorPoints) {
			if(ap.getAnchor() == point)
				return ap;
		}
		return null;
	}
	
	public int getAnchorPointCount() {
		return anchorPoints.size();
	}
	
	public List<AnchorPoint> getAnchorPoints() {
		return Collections.unmodifiableList(anchorPoints);
	}
	
	
	public AnchorPoint removeAnchorByPoint(byte point) {
		Iterator<AnchorPoint> ite = anchorPoints.iterator();
		while(ite.hasNext()) {
			AnchorPoint ap = ite.next();
			if(ap.getAnchor() == point) {
				ite.remove();
				return ap;
			}
		}
		return null;
	}
	
	public AnchorPoint removeAnchorByteIndex(int index) {
		if(anchorPoints.size() <= index)
			throw new IllegalArgumentException();
		return anchorPoints.remove(index);
	}
	
	
	@Override
	protected void fireDirty() {
		if(getParent() != null) {
			getParent().fireDirty();
		}
//		getParent().fireDirty();
	}
	
	public boolean isAvaliable() {
		return getAnchorPointCount() > 0;
	}
	
	public boolean isFixedSize() {
		return false;
	}
	
	@Override
	public Element toXml(PersistMapping mapping) throws Exception {
		String name = mapping.getMappingName(getClass());
		if(name == null)
			throw new Exception();
		Element element = new Element(name);
		element.setAttribute(new Attribute("id", String.valueOf(this.id)));
		element.setAttribute(new Attribute("layer", String.valueOf(this.layer)));
		element.setAttribute(new Attribute("mask", String.valueOf(this.getMask())));
		for(int i = 0; i < this.anchorPoints.size(); i++) {
			element.addContent(XmlUtil.getAnchorPointElement(this.anchorPoints.get(i)));
		}
		return element;
	}

	@Override
	public void load(Object parent, Element element, PersistMapping mapping)
			throws Exception {
		this.id = XmlUtil.getStringValue(element, "id", null);
		this.layer = XmlUtil.getIntValue(element, "layer", this.layer);
		this.mask = XmlUtil.getLongValue(element, "mask", this.mask);
		List l = element.getChildren("AnchorPoint");
		for(int i = 0; i < l.size(); i++) {
			Element el = (Element)l.get(i);
			this.anchorPoints.add(XmlUtil.getAnchorPoint(el));
		}
	}
	
//	protected static final int[][] POSITION = {
//		{Integer.MIN_VALUE, Integer.MIN_VALUE},
//		{Integer.MIN_VALUE, Integer.MIN_VALUE},
//		{Integer.MIN_VALUE, Integer.MIN_VALUE},
//		{Integer.MIN_VALUE, Integer.MIN_VALUE},
//		{Integer.MIN_VALUE, Integer.MIN_VALUE},
//		{Integer.MIN_VALUE, Integer.MIN_VALUE},
//		{Integer.MIN_VALUE, Integer.MIN_VALUE},
//		{Integer.MIN_VALUE, Integer.MIN_VALUE},
//		{Integer.MIN_VALUE, Integer.MIN_VALUE},
//	};
	
	public void layout() {
		if (isAvaliable()) {
			Dimension parentSize = getParent().getSize();
			int[][] rect = { { Integer.MIN_VALUE, Integer.MIN_VALUE },
					{ Integer.MIN_VALUE, Integer.MIN_VALUE },
					{ Integer.MIN_VALUE, Integer.MIN_VALUE },
					{ Integer.MIN_VALUE, Integer.MIN_VALUE },
					{ Integer.MIN_VALUE, Integer.MIN_VALUE },
					{ Integer.MIN_VALUE, Integer.MIN_VALUE },
					{ Integer.MIN_VALUE, Integer.MIN_VALUE },
					{ Integer.MIN_VALUE, Integer.MIN_VALUE },
					{ Integer.MIN_VALUE, Integer.MIN_VALUE }, };
			for (AnchorPoint anchorPoint : getAnchorPoints()) {
				calcAnchor(anchorPoint, rect);
			}
			int top = calcTop(rect);
			int bottom = calcBottom(rect);
			int left = calcLeft(rect);
			int right = calcRight(rect);
			int center = rect[Anchor.CENTER][0];
			int middle = rect[Anchor.CENTER][1];
			if (center == Integer.MIN_VALUE) {
				if (left != Integer.MIN_VALUE && right != Integer.MIN_VALUE) {
					setLeft(left);
					if (!isFixedSize())
						setWidth(right - left);
				} else if (left != Integer.MIN_VALUE
						&& right == Integer.MIN_VALUE) {
					setLeft(left);
				} else if (left == Integer.MIN_VALUE
						&& right != Integer.MIN_VALUE) {
					setLeft(right - getSize().width);
				} else if (left == Integer.MIN_VALUE
						&& right == Integer.MIN_VALUE) {
					if (rect[Anchor.TOP][0] != Integer.MIN_VALUE) {
						setLeft(rect[Anchor.TOP][0] - getSize().width() / 2);
					} else if (rect[Anchor.BOTTOM][0] != Integer.MIN_VALUE) {
						setLeft(rect[Anchor.BOTTOM][0] - getSize().width() / 2);
					}
				}
			} else {
				if (left != Integer.MIN_VALUE && right != Integer.MIN_VALUE) {
					setWidth(right - left);
				} else if (left != Integer.MIN_VALUE
						&& right == Integer.MIN_VALUE) {
					setLeft(left);
					if (!isFixedSize())
						setWidth(2 * (center - left));
				} else if (left == Integer.MIN_VALUE
						&& right != Integer.MIN_VALUE) {
					setLeft(right - 2 * (right - center));
					if (!isFixedSize())
						setWidth(2 * (right - center));
				} else if (left == Integer.MIN_VALUE
						&& right == Integer.MIN_VALUE) {
					setLeft(center - getSize().width / 2);
				}
			}
			if (middle == Integer.MIN_VALUE) {
				if (top != Integer.MIN_VALUE && bottom != Integer.MIN_VALUE) {
					setTop(top);
					if (!isFixedSize())
						setHeight(bottom - top);
				} else if (top != Integer.MIN_VALUE
						&& bottom == Integer.MIN_VALUE) {
					setTop(top);
				} else if (top == Integer.MIN_VALUE
						&& bottom != Integer.MIN_VALUE) {
					setTop(bottom - getSize().height);
				} else if (top == Integer.MIN_VALUE
						&& bottom == Integer.MIN_VALUE) {
					if (rect[Anchor.LEFT][1] != Integer.MIN_VALUE) {
						setTop(rect[Anchor.LEFT][1] - getSize().height / 2);
					} else if (rect[Anchor.RIGHT][1] != Integer.MIN_VALUE) {
						setTop(rect[Anchor.RIGHT][1] - getSize().height / 2);
					}
				}
			} else {
				if (top != Integer.MIN_VALUE && bottom != Integer.MIN_VALUE) {
					setHeight(bottom - top);
				} else if (top != Integer.MIN_VALUE
						&& bottom == Integer.MIN_VALUE) {
					setTop(top);
					if (!isFixedSize())
						setHeight(2 * (middle - top));
				} else if (top == Integer.MIN_VALUE
						&& bottom != Integer.MIN_VALUE) {
					setTop(bottom - 2 * (bottom - middle));
					if (!isFixedSize())
						setHeight(2 * (bottom - middle));
				} else if (top == Integer.MIN_VALUE
						&& bottom == Integer.MIN_VALUE) {
					setTop(middle - getSize().height / 2);
				}
			}
		}
	}
	
	private void setLeft(int left) {
		setLocation(new Point(left, getLocation().y));
	}
	
	private void setTop(int top) {
		setLocation(new Point(getLocation().x, top));
	}
	
	private void setWidth(int width) {
		setSize(new Dimension(width, getSize().height));
	}
	
	private void setHeight(int height) {
		setSize(new Dimension(getSize().width, height));
	}
	
	protected void calcAnchor(AnchorPoint anchor, int[][] rect) {
		int relX = 0;
		int relY = 0;
		switch(anchor.getRelativeAnchor()) {
			case Anchor.TOPLEFT:
				relX = 0;
				relY = 0;
				break;
			case Anchor.TOP:
				relX = getParent().getSize().width / 2;
				relY = 0;
				break;
			case Anchor.TOPRIGHT:
				relX = getParent().getSize().width;
				relY = 0;
				break;
			case Anchor.RIGHT:
				relX = getParent().getSize().width;
				relY = getParent().getSize().height / 2;
				break;
			case Anchor.BOTTOMRIGHT:
				relX = getParent().getSize().width;
				relY = getParent().getSize().height;
				break;
			case Anchor.BOTTOM:
				relX = getParent().getSize().width / 2;
				relY = getParent().getSize().height;
				break;
			case Anchor.BOTTOMLEFT:
				relX = 0;
				relY = getParent().getSize().height;
				break;
			case Anchor.LEFT:
				relX = 0;
				relY = getParent().getSize().height / 2;
				break;
			case Anchor.CENTER:
				relX = getParent().getSize().width / 2;
				relY = getParent().getSize().height / 2;
				break;
			default:
				throw new IllegalArgumentException();
		}
		int point = anchor.getAnchor();
		rect[point][0] = relX + anchor.getOffsetX();
		rect[point][1] = relY + anchor.getOffsetY();
	}
	
	protected int calcTop(int[][] rect) {
		if(rect[Anchor.TOPLEFT][1] != Integer.MIN_VALUE) 
			return rect[Anchor.TOPLEFT][1];
		if(rect[Anchor.TOP][1] != Integer.MIN_VALUE)
			return rect[Anchor.TOP][1];
		if(rect[Anchor.TOPRIGHT][1] != Integer.MIN_VALUE)
			return rect[Anchor.TOPRIGHT][1];
		return Integer.MIN_VALUE;
	}
	
	protected int calcBottom(int[][] rect) {
		if(rect[Anchor.BOTTOMLEFT][1] != Integer.MIN_VALUE) 
			return rect[Anchor.BOTTOMLEFT][1];
		if(rect[Anchor.BOTTOM][1] != Integer.MIN_VALUE)
			return rect[Anchor.BOTTOM][1];
		if(rect[Anchor.BOTTOMRIGHT][1] != Integer.MIN_VALUE)
			return rect[Anchor.BOTTOMRIGHT][1];
		return Integer.MIN_VALUE;
	}
	
	protected int calcLeft(int[][] rect) {
		if(rect[Anchor.TOPLEFT][0] != Integer.MIN_VALUE) 
			return rect[Anchor.TOPLEFT][0];
		if(rect[Anchor.LEFT][0] != Integer.MIN_VALUE)
			return rect[Anchor.LEFT][0];
		if(rect[Anchor.BOTTOMLEFT][0] != Integer.MIN_VALUE)
			return rect[Anchor.BOTTOMLEFT][0];
		return Integer.MIN_VALUE;
	}
	
	protected int calcRight(int[][] rect) {
		if(rect[Anchor.TOPRIGHT][0] != Integer.MIN_VALUE) 
			return rect[Anchor.TOPRIGHT][0];
		if(rect[Anchor.RIGHT][0] != Integer.MIN_VALUE)
			return rect[Anchor.RIGHT][0];
		if(rect[Anchor.BOTTOMRIGHT][0] != Integer.MIN_VALUE)
			return rect[Anchor.BOTTOMRIGHT][0];
		return Integer.MIN_VALUE;
	}
	
	public void setRequire(boolean require) {
		this.required = require;
	}
	
	public boolean isRequire() {
		return this.required;
	}
	
	public abstract Region clone();
	
	public void fillCloneRegion(Region region) {
		region.id = this.id;
		region.anchorPoints.clear();
		for(AnchorPoint anchor: anchorPoints) {
			region.anchorPoints.add(anchor.getCopy());
		}
		region.setLocation(getLocation());
		region.setSize(this.getSize());
		region.valid = this.valid;
		region.layer = this.layer;
		region.required = this.required;
		region.visible = this.visible;
		region.mask = this.mask;
	}
	
	
	public static boolean anchorPointsEquals(List<AnchorPoint> l1, List<AnchorPoint> l2) {
		if(l1.size() != l2.size())
			return false;
		for(AnchorPoint ap : l1) {
			if(!l2.contains(ap)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean generateEquals(Region region) {
		if(region == null)
			return false;
		return anchorPointsEquals(region.getAnchorPoints(), anchorPoints) && region.visible == this.visible;
		
	}
	
	public boolean IsInParentState() {
		if(getParent() == null) {
			return false;
		} else {
			int state = getParent().getState();
			int m = (int)((this.mask >> 32) & 0xFFFFFFFF);
			int flag = (int)(this.mask & 0xFFFFFFFF);
			return (m & state) == (m & flag);
		}
	}
	
	public int getVisibleMask() {
		return (int)((this.mask >> 32) & 0xFFFFFFFF);
	}
	
	public int getVisbleFlag() {
		return (int)(this.mask & 0xFFFFFFFF);
	}
	
	public AnchorPoint[] getDefaultAnchorPoints() {
		return new AnchorPoint[]{new AnchorPoint(Anchor.CENTER, Anchor.CENTER)};
	}
}
