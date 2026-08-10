package com.pip.uieditor.util;

import java.io.File;
import java.io.IOException;
import java.util.WeakHashMap;

import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipImage;

public class ResourceLoader {
	private static WeakHashMap<String, PipImage> imageCache = new WeakHashMap<String, PipImage>();
	private static WeakHashMap<File, PipAnimateSet> animateCache = new WeakHashMap<File, PipAnimateSet>();
	
	public static PipImage loadImage(String name) throws IOException{
		if(imageCache.containsKey(name)) {
			return imageCache.get(name);
		} else {
			PipImage image = new PipImage();
			image.load(name);
			imageCache.put(name, image);
			return image;
		}
	}
	
	public static PipAnimateSet loadAnimate(File name) throws IOException{
		if(animateCache.containsKey(name)) {
			return animateCache.get(name);
		} else {
			PipAnimateSet animate = new PipAnimateSet();
			animate.load(name, true);
			animateCache.put(name, animate);
			return animate;
		}
	}
}
