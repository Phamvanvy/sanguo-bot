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
package com.pip.gtleditor;


import org.eclipse.jface.text.*;
import org.eclipse.swt.graphics.Point;

import com.pip.j0ide.editors.GTLEditor;

/**
 * Example implementation for an <code>ITextHover</code> which hovers over Java code.
 */
public class GTLTextHover implements ITextHover, ITextHoverExtension2 {
	protected GTLEditorImpl editor;

	public GTLTextHover(GTLEditorImpl editor) {
		this.editor = editor;
	}
	
	/* (non-Javadoc)
	 * Method declared on ITextHover
	 */
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		return String.valueOf(getHoverInfo2(textViewer, hoverRegion));
	}
	
	public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
		if (hoverRegion != null) {
//			try {
				if (hoverRegion.getLength() > -1) {
					return ((GTLEditor)editor).getHintInfo(hoverRegion.getOffset());
//					String varName = textViewer.getDocument().get(hoverRegion.getOffset(), hoverRegion.getLength());
//					return editor.getHintInfo(varName);
				}
//			} catch (BadLocationException x) {
//			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * Method declared on ITextHover
	 */
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		Point selection= textViewer.getSelectedRange();
		if (selection.x <= offset && offset < selection.x + selection.y) {
			return new Region(selection.x, selection.y);
		} else {
			try {
				int[] word = getWord(textViewer.getDocument(), offset);
				return new Region(word[0], word[1] - word[0]);
			} catch (Exception e) {
			}
		}
		return new Region(offset, 0);
	}
	
	public static int[] getWord(IDocument doc, int offset) throws Exception {
		int start = offset - 1;
		int end = offset + 1;
		int total = doc.getLength();
		while (start >= 0) {
			char ch = doc.getChar(start);
			if (isSeparator(ch)) {
				start++;
				break;
			}
			start--;
		}
		while (end < total) {
			char ch = doc.getChar(end);
			if (isSeparator(ch)) {
				break;
			}
			end++;
		}
		return new int[] { start, end };
	}
	
	public static boolean isSeparator(char ch) {
		if (Character.isWhitespace(ch)) {
			return true;
		}
		switch (ch) {
		case '(':
		case ')':
		case '{':
		case '}':
		case ';':
		case ',':
		case '[':
		case ']':
		case '.':
		case ':':
		case '=':
		case '>':
		case '<':
		case '!':
		case '+':
		case '-':
		case '*':
		case '/':
		case '%':
		case '&':
		case '|':
		case '\'':
		case '\"':
			return true;
		}
		return false;
	}
}
