package com.pip.uieditor.model;

import com.pipimage.image.PipImage;
import com.pipimage.image.PipImageData;

public class ImageUtil {
	
	public static int getWidth(PipImage image, int frame, int trans) {
		PipImageData data = image.getImageData(frame);
		if(trans >=4) {
			return data.height;
		} else {
			return data.width;
		}
	}
	
	public static int getHeight(PipImage image, int frame, int trans) {
		PipImageData data = image.getImageData(frame);
		if(trans >=4) {
			return data.width;
		} else {
			return data.height;
		}
		
	}
}
