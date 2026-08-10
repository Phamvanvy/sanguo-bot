package com.pip.uieditor.model.text;

import java.io.File;
import java.io.IOException;

import org.eclipse.swt.graphics.Rectangle;

import com.pip.j0ide.Settings;
import com.pip.uieditor.editor.MacroManager;
import com.pip.uieditor.model.AnimateData;
import com.pip.uieditor.model.ImageData;
import com.pip.uieditor.model.text.Document.FormatContext;
import com.pip.uieditor.model.text.Document.Line;
import com.pip.uieditor.util.ResourceLoader;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipImage;
import com.pipimage.image.PipImageDraw;

public class AnimateElement implements Element{

	private AnimateData animateData;
	private float scale;
	private int hgap;
	private Document doc;
	
	private PipAnimateSet animate;
	
	private Link link;
	
	public AnimateElement(Document doc, AnimateData animateData, float scale, int hgap) {
		this.doc = doc;
		this.animateData = animateData;
		this.scale = scale;
		this.hgap = hgap;
	}
	
	@Override
	public Document getDocument() {
		return this.doc;
	}
	
	public AnimateData getAnimateData() {
		return animateData;
	}
	
	public String getName() {
		return ANIMATE;
	}
	
	public PipAnimateSet getAnimate() {
		return this.animate;
	}
	
	public float getScale() {
		return scale;
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
		if (this.animate == null) {
			loadAnimate();
		}
		Rectangle rect = animate.getAnimate(animateData.getIndex()).getBounds();
		int width = (int)(rect.width * scale);
		int height = (int)(rect.height * scale);
		Line line = context.line;
		width += 2 * hgap;
		if(!context.incOffset(width)) {
			line = context.newLine();
			context.incOffset(width);
		}
		if(line.height < height) {
			line.height = height;
		}
		AnimateView view = new AnimateView(this, line, width, height);
		context.addView(view);
	}
	
	protected void loadAnimate() {
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

