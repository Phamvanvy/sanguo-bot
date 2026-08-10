package com.pip.uieditor.model;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.jdom.Attribute;
import org.jdom.Element;

import com.pip.uieditor.model.persist.PersistMapping;



/**
 * ÊÖ»úÆÁÄ»
 * @author Jeffrey
 *
 */
public class Screen extends Container{
	
	public static final String[] SCREEN_SIZE_STRING = {
		"960*640", "640*960", "960*540", "540*960", "845*480", "480*845", "800*480", "480*800", "480*320", "320*480", "360*640", "640*360"
	};
	
	public static final int[][] SCREEN_SIZE = {
		{960,640}, {640,960}, {960,540}, {540,960}, {845,480}, {480,845}, {800,480}, {480,800}, {480,320}, {320,480}, {360,640}, {640,360}
	};
	
	private static final int DEFAULT_WIDTH = 800;
	private static final int DEFAULT_HEIGHT = 480;
	
	private boolean dirty;
	
	private String script = "";
	
	public static final String PROPERTY_DIRTY = "dirty";
	
	public static final IPropertyDescriptor[] EMPTY_DESC = new IPropertyDescriptor[0];
	
	public Screen() {
		this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}
	
	public Screen(int width, int height) {
		super("Screen");
		setName("scr");
		this.size = new Dimension(width, height);
	}	
	
	void setDirty() {
		if(!this.dirty) {
			this.dirty = true;
			firePropertyChange(PROPERTY_DIRTY, false, true);
		}
	}
	
	public void clearDirty() {
		if(this.dirty) {
			this.dirty = false;
			firePropertyChange(PROPERTY_DIRTY, true, false);
		}
	}
	
	public boolean isDirty() {
		return this.dirty;
	}
	
	public static int getScreenSizeIndex(int width, int height) {
		for(int i = 0; i < SCREEN_SIZE.length; i++) {
			if(SCREEN_SIZE[i][0] == width && SCREEN_SIZE[i][1] == height) {
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return EMPTY_DESC;
	}
	
	
	public void setScript(String script) {
		this.script = script;
	}
	
	public String getScript() {
		return this.script;
	}
	
	@Override
	public Element toXml(PersistMapping mapping) throws Exception {
		Element el = super.toXml(mapping);
		el.setAttribute(new Attribute("script", script == null ? "" : script));
		return el;
	}
	
	@Override
	public void load(Object parent, Element element, PersistMapping mapping)
			throws Exception {
		super.load(parent, element, mapping);
		Attribute att = element.getAttribute("script");
		if(att != null) {
			this.script = att.getValue();
		}
	}
	

}
