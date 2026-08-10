package com.pip.uieditor.figures;

import org.eclipse.draw2d.Graphics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.pip.uieditor.model.ImageRegion;
import com.pipimage.image.PipImage;
import com.pipimage.image.PipImageDraw;

public class ImageRegionFigure extends RegionFigure {
	
	public ImageRegionFigure(ImageRegion region) {
		super(region);
	}
	
	@Override
	protected void paintFigure(Graphics gc) {
		ImageRegion region = (ImageRegion) getRegion();
		if (region.isAvaliable()  && region.IsInParentState()) {
			PipImage image = region.getImage();
			PipImageDraw draw = image.getImageDraw(region.getImageData().getFrame());
			Image im = draw.createSWTImage(Display.getCurrent(), region.getTrans());
			if(region.isFill()) {
				int regionWidth = region.getSize().width;
				int regionHeight = region.getSize().height;
				int imageWidth = im.getBounds().width;
				int imageHeight = im.getBounds().height;
				int col = regionWidth / imageWidth + (regionWidth % imageWidth == 0 ? 0 : 1);
				int row = regionHeight / imageHeight + (regionHeight % imageHeight == 0 ? 0 : 1);
				if(col == 1 && row == 1) {
					gc.drawImage(im, getLocation().x, getLocation().y);
				} else {
					for(int i =  0, x = 0; i < col; i++) {
						for(int j = 0, y = 0; j < row; j++) {
							gc.drawImage(im, getLocation().x + x, getLocation().y + y);
							y += imageHeight;
						}
						x += imageWidth;
					}
				}
			} else {
				if (region.isScale()) {
					gc.drawImage(im, 0, 0, im.getImageData().width,
							im.getImageData().height, getLocation().x,
							getLocation().y, region.getSize().width,
							region.getSize().height);
				} else {
					gc.drawImage(im, getLocation().x, getLocation().y);
				}
			}
			im.dispose();
		}
	}
}
