package com.pip.gtleditor;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.source.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.pip.j0ide.Activator;
import com.pip.util.Utils;

public class GTLBreakpointRuler implements MouseListener, PaintListener, IVerticalRulerColumn, IViewportListener {
	private GTLEditorImpl editor;
	private ISourceViewer viewer;
	private IVerticalRuler ruler;
	private Canvas fCanvas;
	private Image breakpointImg, activeBreakpointImg;
	private IGTLDebugManager debugManager;
	private String sourceFile;
	
	
	public GTLBreakpointRuler(GTLEditorImpl editor, ISourceViewer viewer, IVerticalRuler vr) {
		this.editor = editor;
		this.viewer = viewer;
		this.ruler = vr;
		
		breakpointImg = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/breakpoint.gif").createImage();
		activeBreakpointImg = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/activebreakpoint.gif").createImage();

		CompositeRuler crul = (CompositeRuler)ruler;
		crul.addDecorator(0, this);
		ruler.getControl().addMouseListener(this);
	}
	
	private String getSourceFile() {
		if (sourceFile == null) {
			FileStoreEditorInput input = (FileStoreEditorInput)editor.getEditorInput();
			URI url = input.getURI();
			sourceFile = Utils.urlToPath(url);
		}
		return sourceFile;
	}
	
	public void dispose() {
		breakpointImg.dispose();
		activeBreakpointImg.dispose();
	}

	public void paintControl(PaintEvent e) {
		if (debugManager == null) {
			return;
		}
		int startLine = viewer.getTopIndex();
		int endLine = viewer.getBottomIndex();
		StyledText text = viewer.getTextWidget();
		for (int i = startLine; i <= endLine; i++) {
			if (debugManager.isBreakpoint(getSourceFile(), i)) {
				int lineHead = text.getOffsetAtLine(i);
				Point lineOff = text.getLocationAtOffset(lineHead);
				int lineHei = text.getLineHeight(lineHead);
				e.gc.drawImage(breakpointImg, lineOff.x + 2, lineOff.y + (lineHei - breakpointImg.getBounds().height) / 2);
			}
			if (debugManager.isActiveLine(getSourceFile(), i)) {
				int lineHead = text.getOffsetAtLine(i);
				Point lineOff = text.getLocationAtOffset(lineHead);
				int lineHei = text.getLineHeight(lineHead);
				e.gc.drawImage(activeBreakpointImg, lineOff.x, lineOff.y + (lineHei - activeBreakpointImg.getBounds().height) / 2);
			}
		}
	}

	public void mouseDoubleClick(MouseEvent e) {
		int startLine = viewer.getTopIndex();
		int endLine = viewer.getBottomIndex();
		StyledText text = viewer.getTextWidget();
		for (int i = startLine; i <= endLine; i++) {
			int lineHead = text.getOffsetAtLine(i);
			Point lineOff = text.getLocationAtOffset(lineHead);
			int lineHei = text.getLineHeight(lineHead);
			if (e.y >= lineOff.y && e.y < lineOff.y + lineHei) {
				toggleBreakpoint(i);
				text.redraw(0, lineOff.y, text.getClientArea().width, lineHei, false);
				redraw();
				break;
			}
		}
	}

	public void mouseDown(MouseEvent e) {}

	public void mouseUp(MouseEvent e) {}
	
	public int[] getHightlightLines() {
		if (debugManager == null) {
			return new int[0];
		}
		return debugManager.getHightlightLines(getSourceFile());
	}
	
	public boolean isBreakpoint(int line) {
		if (debugManager == null) {
			return false;
		}
		return debugManager.isBreakpoint(getSourceFile(), line);
	}
	
	public boolean isActiveLine(int line) {
		if (debugManager == null) {
			return false;
		}
		return debugManager.isActiveLine(getSourceFile(), line);
	}
	
	public void toggleBreakpoint(int lineNum) {
		if (debugManager == null) {
			return;
		}
		debugManager.toggleBreakpoint(getSourceFile(), lineNum);
	}

	public Control createControl(CompositeRuler parentRuler,
			Composite parentControl) {
		fCanvas = new Canvas(parentControl, SWT.NONE);
		fCanvas.addPaintListener(this);
		viewer.addViewportListener(this);
		return fCanvas;
	}

	public Control getControl() {
		return fCanvas;
	}

	public int getWidth() {
		return 12;
	}

	public void redraw() {
		fCanvas.redraw();
	}

	public void setFont(Font font) {
	}

	public void setModel(IAnnotationModel model) {
	}

	public void viewportChanged(int verticalOffset) {
		redraw();
	}
	
	public void setGTLDebugManager(IGTLDebugManager p) {
		debugManager = p;
	}
}
