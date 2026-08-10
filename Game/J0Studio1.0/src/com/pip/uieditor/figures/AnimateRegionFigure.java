package com.pip.uieditor.figures;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import com.pip.uieditor.model.AnimateRegion;
import com.pip.uieditor.util.AnchorUtil;
import com.pipimage.image.PipAnimate;

public class AnimateRegionFigure extends RegionFigure {
	
	public AnimateRegionFigure(AnimateRegion region) {
		super(region);
	}
	
	@Override
	protected void paintFigure(Graphics gc) {
		AnimateRegion region = (AnimateRegion) getRegion();
		if (region.isAvaliable() && region.IsInParentState()) {
			PipAnimate animate = region.getAnimateSet().getAnimate(
					region.getAnimateData().getIndex());
			Image im = new Image(null, region.getSize().width,
					region.getSize().height);
			GC g = new GC(im);
			Point p = AnchorUtil.calcAnchorPoint(region.getHookAnchor(),
					new Rectangle(new Point(0, 0), region.getSize()),
					region.getHookPoint());
			animate.drawFrame(g, 0, p.x, p.y, region.getScale() * 1.0f / 100);
			gc.drawImage(im, getLocation().x, getLocation().y);
			im.dispose();
		}
	}
}
