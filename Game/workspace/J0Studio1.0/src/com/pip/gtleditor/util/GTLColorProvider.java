/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.pip.gtleditor.util;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Manager for colors used in the Java editor
 */
public class GTLColorProvider {

	public static final RGB MULTI_LINE_COMMENT= new RGB(0x3F, 0x7F, 0x5F);
	public static final RGB SINGLE_LINE_COMMENT= new RGB(0x3F, 0x7F, 0x5F);
	public static final RGB KEYWORD= new RGB(0x7F, 0x00, 0x55);
	public static final RGB TYPE= new RGB(0x7F, 0x00, 0x55);
	public static final RGB STRING= new RGB(0x2A, 0x00, 0xFF);
	public static final RGB SYSFUNC = new RGB(192, 0, 0);
	public static final RGB USERFUNC = new RGB(191, 0, 0);
    public static final RGB DEFAULT= new RGB(0, 0, 0);
	public static final RGB MACRO = new RGB(0x66, 0x33, 0x99);
	public static final RGB GLOBAL = new RGB(0, 0, 0xC0);
	
	protected Map fColorTable= new HashMap(10);

	/**
	 * Release all of the color resources held onto by the receiver.
	 */	
	public void dispose() {
		Iterator e= fColorTable.values().iterator();
		while (e.hasNext())
			 ((Color) e.next()).dispose();
	}
	
	/**
	 * Return the color that is stored in the color table under the given RGB
	 * value.
	 * 
	 * @param rgb the RGB value
	 * @return the color stored in the color table for the given RGB value
	 */
	public Color getColor(RGB rgb) {
		Color color= (Color) fColorTable.get(rgb);
		if (color == null) {
			color= new Color(Display.getCurrent(), rgb);
			fColorTable.put(rgb, color);
		}
		return color;
	}
}
