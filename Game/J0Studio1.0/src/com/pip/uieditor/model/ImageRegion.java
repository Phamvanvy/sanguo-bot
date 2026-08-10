package com.pip.uieditor.model;

import java.io.File;
import java.io.IOException;

import org.eclipse.draw2d.geometry.Dimension;
import org.jdom.Attribute;
import org.jdom.Element;

import com.pip.j0ide.Settings;
import com.pip.uieditor.editor.MacroManager;
import com.pip.uieditor.model.annotation.Property;
import com.pip.uieditor.model.persist.PersistMapping;
import com.pip.uieditor.model.persist.XmlUtil;
import com.pip.uieditor.model.propertydescriptor.DrawModePropertyDescriptor;
import com.pip.uieditor.model.propertydescriptor.ImagePropertyDescriptor;
import com.pip.uieditor.model.propertydescriptor.TransPropertyDescriptor;
import com.pip.uieditor.util.ObjectUtil;
import com.pip.uieditor.util.ResourceLoader;
import com.pipimage.image.PipImage;

public class ImageRegion extends Region {
	
	public static final ImageRegion PROTOTYPE = new ImageRegion();
	
	@Property(type=ImagePropertyDescriptor.class)
	private ImageData imageData;
	
	private PipImage image;
	
	@Property(type=TransPropertyDescriptor.class)
	private int trans = 0;
	
	
	@Property(type=DrawModePropertyDescriptor.class)
	private int mode = 0;
	
	public ImageRegion() {
		this("", null);
	}
	
	public ImageRegion(String id, ImageData imageData) {
		super(id);
		this.imageData = imageData;
	}
	
	public void setImageData(ImageData imageData) {
		ImageData oldValue = this.imageData;
		this.imageData = imageData;
		firePropertyChange("imageData", oldValue, this.imageData);
		loadPipImage();
	}
	
	public void setTrans(int trans) {
		if(this.trans != trans) {
			int oldtrans = this.trans;
			this.trans = trans;
			firePropertyChange("trans", oldtrans, this.trans);
		}
	}
	
	public int getTrans() {
		return this.trans;
	}

	public void setMode(int mode) {
		if(this.mode != mode) {
			int oldMode = this.mode;
			this.mode = mode;
			firePropertyChange("mode", oldMode, this.mode);
		}
	}
	
	public int getMode() {
		return this.mode;
	}
	
	public boolean isFill() {
		return mode == 1;
	}
	
	public boolean isScale() {
		return mode == 2;
	}
	
	private void loadPipImage() {
		if(this.imageData != null && this.imageData.getFile() != null) {
			try {
				String file = this.imageData.getFile();
				if(MacroManager.instance().isMacro(file)) {
					file = MacroManager.instance().findFileName(file);
				}
				image = ResourceLoader.loadImage(Settings.uiResourceDir + File.separator + file);
				calcImageSize();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void validate() {
		calcImageSize();
		super.validate();
	}

	protected void calcImageSize() {
		if (image != null) {
			setSize(new Dimension(ImageUtil.getWidth(image,
					imageData.getFrame(), this.trans), ImageUtil.getHeight(
					image, imageData.getFrame(), this.trans)));
		}
	}
	
	public ImageData getImageData() {
		return imageData;
	}
	
	@Override
	public boolean isAvaliable() {
		return image != null & super.isAvaliable();
	}
	
	public PipImage getImage() {
		return image;
	}
	
	public ImageRegion clone() {
		ImageRegion ret = new ImageRegion();
		fillCloneRegion(ret);
		if(this.imageData != null) {
			ret.imageData = this.imageData.getCopy();
			ret.image = this.image;
			ret.trans = this.trans;
			ret.mode = this.mode;
		}
		return ret;
	}
	
	@Override
	public boolean generateEquals(Region region) {
		if(region == null)
			return false;
		ImageRegion ir = (ImageRegion)region;
		return ObjectUtil.equals(imageData, ir.imageData)
				&& mode == ir.mode && trans == ir.trans
				&& super.generateEquals(region);
	}
	
	
	@Override
	public Element toXml(PersistMapping mapping) throws Exception {
		Element el = super.toXml(mapping);
		if(this.imageData != null) {
			el.setAttribute(new Attribute("file",this.imageData.getFile()));
			el.setAttribute(new Attribute("frame", this.imageData.getFrame()+""));
		}
		el.setAttribute(new Attribute("trans", this.trans + ""));
		el.setAttribute(new Attribute("mode", this.mode + ""));
		return el;
	}

	@Override
	public void load(Object parent, Element element, PersistMapping mapping)
			throws Exception {
		super.load(parent, element, mapping);
		String file = XmlUtil.getStringValue(element, "file", null);
		if(file != null) {
			ImageData imageData = new ImageData(file, XmlUtil.getIntValue(element, "frame", 0));
			this.imageData = imageData;
		}
		this.trans = XmlUtil.getIntValue(element, "trans", this.trans);
		
		if(element.getAttribute("fill") != null) {
			boolean fill = XmlUtil.getBooleanValue(element, "fill", false);
			mode = fill ? 1 : 0;
		}
		if(element.getAttribute("mode") != null) {
			mode = XmlUtil.getIntValue(element, "mode", this.mode);
		}
		
		
		loadPipImage();
	}
}
