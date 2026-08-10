package com.pip.uieditor.model.text;

import java.io.File;
import java.io.IOException;

import org.eclipse.swt.graphics.Rectangle;

import com.pip.j0ide.Settings;
import com.pip.uieditor.model.ImageData;
import com.pip.uieditor.model.text.Document.FormatContext;
import com.pip.uieditor.model.text.Document.Line;
import com.pip.uieditor.util.ResourceLoader;
import com.pipimage.image.PipImage;
import com.pipimage.image.PipImageDraw;

public class ImageElement implements Element{

	private ImageData imageData;
	private int trans;
	private float scale;
	private int hgap;
	private Document doc;
	
	private PipImage image;
	
	private Link link;
	
	public ImageElement(Document doc, ImageData imageData, int trans, float scale, int hgap) {
		this.doc = doc;
		this.imageData = imageData;
		this.trans = trans;
		this.scale = scale;
		this.hgap = hgap;
	}
	
	@Override
	public Document getDocument() {
		return this.doc;
	}
	
	public ImageData getImageData() {
		return imageData;
	}
	
	public String getName() {
		return IMAGE;
	}
	
	public PipImage getImage() {
		return this.image;
	}
	
	public float getScale() {
		return scale;
	}

	public int getTrans() {
		return trans;
	}

	public void setTrans(int trans) {
		this.trans = trans;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public int getHgap() {
		return hgap;
	}

	public void setHgap(int hgap) {
		this.hgap = hgap;
	}

	public Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}

	@Override
	public void format(FormatContext context) {
		if(this.image == null) {
			loadImage();
		}
		PipImageDraw drawer = image.getImageDraw(imageData.getFrame());
		Rectangle rect = drawer.getBounds(0);
		int width = (int)(rect.width * scale);
		int height = (int)(rect.height * scale);
		if (trans >= 4) {
			int t = height;
			height = width;
			width = t;
		}
		width += 2 * hgap;
		Line line = context.line;
		if(!context.incOffset(width)) {
			line = context.newLine();
			context.incOffset(width);
		}
		if(line.height < height) {
			line.height = height;
		}
		ImageView view = new ImageView(this, line, width, height);
		context.addView(view);
	}
	
	protected void loadImage() {
		if(this.imageData != null && this.imageData.getFile() != null) {
			try {
				image = ResourceLoader.loadImage(Settings.uiResourceDir + File.separator + imageData.getFile());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[IMAGE]");
		if(link != null) {
			sb.append("[link]").append(link.url);
		}
		return sb.toString();
	}
}

