package com.pip.uieditor.model;

import org.eclipse.draw2d.geometry.Point;
import org.jdom.Attribute;
import org.jdom.Element;

import com.pip.uieditor.model.annotation.Property;
import com.pip.uieditor.model.persist.PersistMapping;
import com.pip.uieditor.model.persist.XmlUtil;
import com.pip.uieditor.model.propertydescriptor.AnchorPropertyDescriptor;
import com.pip.uieditor.model.propertydescriptor.PointPropertyDescriptor;

public class ModelRegion extends Region{
	
	public static final ModelRegion PROTOTYPE = new ModelRegion();
	
	@Property(type=PointPropertyDescriptor.class)
	private Point hookPoint;
	
	@Property(type=AnchorPropertyDescriptor.class)
	private int hookAnchor;
	
	public ModelRegion() {
		super("");
		this.hookPoint = new Point(0, 0);
	}
	
	public void setHookPoint(Point hookPoint) {
		if(!this.hookPoint.equals(hookPoint)) {
			Point old = this.hookPoint;
			this.hookPoint = hookPoint.getCopy();
			firePropertyChange("hookPoint", old, this.hookPoint);
		}
	}
	
	public Point getHookPoint() {
		return hookPoint.getCopy();
	}
	
	public int getHookAnchor() {
		return this.hookAnchor;
	}
	
	public void setHookAnchor(int hookAnchor) {
		if(hookAnchor < Anchor.CENTER || hookAnchor > Anchor.LEFT) {
			throw new IllegalArgumentException();
		}
		if(this.hookAnchor != hookAnchor) {
			int old = this.hookAnchor;
			this.hookAnchor = hookAnchor;
			firePropertyChange("hookAnchor", old, this.hookAnchor);
		}
	}
	
	public ModelRegion clone() {
		ModelRegion ret = new ModelRegion();
		fillCloneRegion(ret);
		ret.hookPoint = this.hookPoint.getCopy();
		ret.hookAnchor = this.hookAnchor;
		return ret;
	}
	
	@Override
	public boolean generateEquals(Region region) {
		if(region == null)
			return false;
		ModelRegion ir = (ModelRegion)region;
		return hookPoint.equals(ir.hookPoint)
				&& super.generateEquals(region);
	}
	
	
	@Override
	public Element toXml(PersistMapping mapping) throws Exception {
		Element el = super.toXml(mapping);
		el.setAttribute(XmlUtil.getPointAttribute("hookPoint", this.hookPoint));
		el.setAttribute(new Attribute("hookAnchor", String.valueOf(this.hookAnchor)));
		return el;
	}

	@Override
	public void load(Object parent, Element element, PersistMapping mapping)
			throws Exception {
		super.load(parent, element, mapping);
		this.hookPoint = XmlUtil.getPoint(element, "hookPoint", this.hookPoint);
		this.hookAnchor = XmlUtil.getIntValue(element, "hookAnchor", this.hookAnchor);
	}	
}
