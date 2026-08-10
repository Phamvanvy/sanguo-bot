package com.pip.gtleditor;


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPaintPositionManager;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;

import com.swtdesigner.SWTResourceManager;

public class GTLEditorPainter implements IPainter, LineBackgroundListener {
	private GTLEditorImpl editor;
	private final ITextViewer fViewer;
	private int lastErrorLine = -1;
	private int[] lastBreakpointLine = new int[0];
	private Color errorColor;
	private Color breakpointColor;
	private Color activeBreakpointColor;
	private Color caretLineColor;

	public GTLEditorPainter(GTLEditorImpl ed, ITextViewer textViewer) {
		this.editor = ed;
		fViewer= textViewer;
		caretLineColor = SWTResourceManager.getColor(0xE8, 0xF2, 0xFE);
	}

	public void setBreakpointColor(Color breakpointColor) {
		this.breakpointColor = breakpointColor;
	}

	public void setActiveBreakpointColor(Color activeBreakpointColor) {
		this.activeBreakpointColor = activeBreakpointColor;
	}

	/*
	 * @see LineBackgroundListener#lineGetBackground(LineBackgroundEvent)
	 */
	public void lineGetBackground(LineBackgroundEvent event) {
		StyledText textWidget= fViewer.getTextWidget();
		if (textWidget != null) {
			int lineNum = textWidget.getLineAtOffset(event.lineOffset);
			if (lineNum == editor.getErrorLine()) {
				event.lineBackground = errorColor;
			} else if (editor.getBreakpointRuler().isActiveLine(lineNum)) {
				event.lineBackground = activeBreakpointColor;
			} else if (editor.getBreakpointRuler().isBreakpoint(lineNum)) {
				event.lineBackground = breakpointColor;
			} else {
				try {
					int caretOff = textWidget.getCaretOffset();
					int caretLine = textWidget.getLineAtOffset(caretOff);
					if (caretLine == lineNum) {
						event.lineBackground = caretLineColor;
					}
				} catch (Exception e) {
				}
			}
		}
	}

	private void redrawLine(int lineNum) {
		if (lineNum < 0) {
			return;
		}
		StyledText textWidget= fViewer.getTextWidget();
		try {
			int lineStart = textWidget.getOffsetAtLine(lineNum);
			Point upperLeft = textWidget.getLocationAtOffset(lineStart);
			int width = textWidget.getClientArea().width  + textWidget.getHorizontalPixel();
			int height = textWidget.getLineHeight(lineStart);
			textWidget.redraw(0, upperLeft.y, width, height, false);
		} catch (Exception e) {
		}
	}

	public void deactivate(boolean redraw) {}

	public void dispose() {}

	public void paint(int reason) {
		if (fViewer.getDocument() == null) {
			return;
		}

		StyledText textWidget= fViewer.getTextWidget();
		textWidget.removeLineBackgroundListener(this);
		textWidget.addLineBackgroundListener(this);
		
		redrawLine(lastErrorLine);
		lastErrorLine = editor.getErrorLine();
		redrawLine(lastErrorLine);
		
		for (int i = 0; i < lastBreakpointLine.length; i++) {
			redrawLine(lastBreakpointLine[i]);
		}
		lastBreakpointLine = editor.getBreakpointRuler().getHightlightLines();
		for (int i = 0; i < lastBreakpointLine.length; i++) {
			redrawLine(lastBreakpointLine[i]);
		}
	}

	public void setPositionManager(IPaintPositionManager manager) {
	}

	public void setErrorColor(Color errorColor) {
		this.errorColor = errorColor;
	}
}
