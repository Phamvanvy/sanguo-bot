package com.pip.uieditor.model;

import java.io.File;
import java.io.IOException;

import org.eclipse.draw2d.geometry.Point;
import org.jdom.Attribute;
import org.jdom.Element;

import com.pip.j0ide.Settings;
import com.pip.uieditor.editor.MacroManager;
import com.pip.uieditor.model.annotation.Property;
import com.pip.uieditor.model.persist.PersistMapping;
import com.pip.uieditor.model.persist.XmlUtil;
import com.pip.uieditor.model.propertydescriptor.AnchorPropertyDescriptor;
import com.pip.uieditor.model.propertydescriptor.AnimatePropertyDescriptor;
import com.pip.uieditor.model.propertydescriptor.BooleanPropertyDescriptor;
import com.pip.uieditor.model.propertydescriptor.IntPropertyDescriptor;
import com.pip.uieditor.model.propertydescriptor.PointPropertyDescriptor;
import com.pip.uieditor.util.ObjectUtil;
import com.pip.uieditor.util.ResourceLoader;
import com.pipimage.image.PipAnimateSet;

public class AnimateRegion extends Region {
	
	public static final AnimateRegion PROTOTYPE = new AnimateRegion();
	
	@Property(type=AnimatePropertyDescriptor.class)
	private AnimateData animateData;
	
	private PipAnimateSet animate;
	
	@Property(type=BooleanPropertyDescriptor.class)
	private boolean loop = false;
	
	@Property(type=PointPropertyDescriptor.class)
	private Point hookPoint;
	
	@Property(type=AnchorPropertyDescriptor.class)
	private int hookAnchor;
	
	@Property(type=IntPropertyDescriptor.class)
	private int scale = 100;

	public AnimateRegion() {
		this("", null);
	}
	
	public AnimateRegion(String id, AnimateData animateData) {
		super(id);
		this.animateData = animateData;
		this.hookPoint = new Point(0, 0);
	}
	
	public void setAnimateData(AnimateData imageData) {
		AnimateData oldValue = this.animateData;
		this.animateData = imageData;
		firePropertyChange("animateData", oldValue, this.animateData);
		loadAnimateSet();
	}
	
	public void setLoop(boolean loop) {
		if(this.loop != loop) {
			this.loop = loop;
			firePropertyChange("loop", !this.loop, this.loop);
		}
	}
	
	
	public boolean isLoop() {
		return this.loop;
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
	
	
	
	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		if(this.scale != scale) {
			int old = this.scale;
			this.scale = scale;
			firePropertyChange("scale", old, this.scale);
		}
	}

	private void loadAnimateSet() {
		if(this.animateData != null && this.animateData.getFile() != null) {
			try {
				String file = this.animateData.getFile();
				if(MacroManager.instance().isMacro(file)) {
					file = MacroManager.instance().findFileName(file);
				}
				animate = ResourceLoader.loadAnimate(new File(Settings.uiAnimateDir , file));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public AnimateData getAnimateData() {
		return animateData;
	}
	
	@Override
	public boolean isAvaliable() {
		return animate != null & super.isAvaliable();
	}
	
	public PipAnimateSet getAnimateSet() {
		return this.animate;
	}
	
	public AnimateRegion clone() {
		AnimateRegion ret = new AnimateRegion();
		fillCloneRegion(ret);
		if(this.animateData != null) {
			ret.animateData = this.animateData.getCopy();
			ret.animate = this.animate;
			ret.loop = this.loop;
			ret.hookPoint = this.hookPoint.getCopy();
			ret.hookAnchor = this.hookAnchor;
		}
		return ret;
	}
	
	@Override
	public boolean generateEquals(Region region) {
		if(region == null)
			return false;
		AnimateRegion ir = (AnimateRegion)region;
		return ObjectUtil.equals(animateData, ir.animateData)
				&& loop == ir.loop && hookPoint.equals(ir.hookPoint)
				&& super.generateEquals(region);
	}
	
	
	@Override
	public Element toXml(PersistMapping mapping) throws Exception {
		Element el = super.toXml(mapping);
		if(this.animateData != null) {
			el.setAttribute(new Attribute("file",this.animateData.getFile()));
			el.setAttribute(new Attribute("index", this.animateData.getIndex()+""));
		}
		el.setAttribute(XmlUtil.getPointAttribute("hookPoint", this.hookPoint));
		el.setAttribute(XmlUtil.getBooleanAttribute("loop", this.loop));
		el.setAttribute(new Attribute("hookAnchor", String.valueOf(this.hookAnchor)));
		el.setAttribute(new Attribute("scale", String.valueOf(this.scale)));
		return el;
	}

	@Override
	public void load(Object parent, Element element, PersistMapping mapping)
			throws Exception {
		super.load(parent, element, mapping);
		String file = XmlUtil.getStringValue(element, "file", null);
		if(file != null) {
			AnimateData animateData = new AnimateData(file, XmlUtil.getIntValue(element, "index", 0));
			this.animateData = animateData;
		}
		this.hookPoint = XmlUtil.getPoint(element, "hookPoint", this.hookPoint);
		this.loop = XmlUtil.getBooleanValue(element, "loop", this.loop);
		this.hookAnchor = XmlUtil.getIntValue(element, "hookAnchor", this.hookAnchor);
		this.scale =  XmlUtil.getIntValue(element, "scale", this.scale);
		loadAnimateSet();
	}
}
