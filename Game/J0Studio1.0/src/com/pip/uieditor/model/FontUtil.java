package com.pip.uieditor.model;

import org.eclipse.swt.graphics.FontData;

import com.pip.j0ide.Settings;

public abstract class FontUtil {
	
	public static FontData getFontData(String fontName) {
		if(fontName != null && fontName.length() > 0) {
			return Settings.fonts.get(fontName);
		}
		return Settings.defaultFont; 
//		FontData[] font = new FontData[]{Settings.smallFont, Settings.mediumFont, Settings.largeFont};
//		int v = Integer.MAX_VALUE;
//		int index = -1;
//		for(int i = 0 ; i < font.length; i++) {
//			int v1 = Math.abs(font[i].getHeight() - height);
//			if(v1 < v) {
//				v = v1;
//				index = i;
//			}
//			if(v == 0)
//				break;
//		}
//		return font[index];
	}
}
