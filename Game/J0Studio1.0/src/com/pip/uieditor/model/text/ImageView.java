package com.pip.uieditor.model.text;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.pip.uieditor.model.text.Document.Line;
import com.pipimage.image.PipImage;
import com.pipimage.image.PipImageDraw;


public class ImageView extends View {
	
	public ImageView(Element element, Line line, int width, int height) {
		super(element, line, width, height);
	}

	@Override
	public void draw(DrawContext context) {
		ImageElement el = (ImageElement)getElement();
		PipImage image = el.getImage();
		PipImageDraw draw = image.getImageDraw(el.getImageData().getFrame());
		Image im = draw.createSWTImage(Display.getCurrent(), el.getTrans());
		Rectangle rect = im.getBounds();
		int realw = (int)(rect.width * el.getScale());
		int realh = (int)(rect.height * el.getScale());
		context.gc.drawImage(im, 0, 0, rect.width, rect.height, context.x + el.getHgap(), context.y - realh, realw, realh);
		im.dispose();
//		image.drawFrame(context.gc, el.imageData.index, context.x, context.y, el.imageData.trans, Graphics.LEFT | Graphics.BOTTOM);
	}

}
