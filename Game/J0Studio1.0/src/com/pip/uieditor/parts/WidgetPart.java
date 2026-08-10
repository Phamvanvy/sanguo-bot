package com.pip.uieditor.parts;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;

import com.pip.gtl.compiler.GTLPreCompiler;
import com.pip.gtl.compiler.GTLPreCompiler.FunctionDef;
import com.pip.gtleditor.GTLEditorImpl;
import com.pip.j0ide.Settings;
import com.pip.uieditor.figures.TableFigure;
import com.pip.uieditor.figures.TextAreaFigure;
import com.pip.uieditor.figures.WidgetFigure;
import com.pip.uieditor.model.Table;
import com.pip.uieditor.model.TextArea;
import com.pip.uieditor.model.Widget;
import com.pip.uieditor.policies.DeleteWidgetPolicy;
import com.pip.uieditor.policies.WidgetLayoutEditPolicy;

public class WidgetPart extends UIObjectPart {
	
	public WidgetPart(Widget model) {
		super(model);
	}
	
	@Override
	public Widget getModel() {
		return (Widget)super.getModel();
	}

	@Override
	protected IFigure createFigure() {
		Figure figure = null;
		if(getModel() instanceof Table) {
			figure = new TableFigure((Table)getModel());
		} else if(getModel() instanceof TextArea) {
			figure = new TextAreaFigure((TextArea)getModel());
		} else {
			figure = new WidgetFigure(getModel());
		}
		figure.setBounds(getModel().getBoundsWithBorder());
		return figure;
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new WidgetLayoutEditPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new DeleteWidgetPolicy());
	}

	@Override
	public List getModelChildren() {
		List subWidgets = getModel().getSubWidgets();
		List regions = getModel().getRegions();
		List widgets = getModel().getChildren();
		List ret = new ArrayList(subWidgets.size() + regions.size() + widgets.size());
		for(Object o : regions) {
			ret.add(o);
		}
		for(Object o : subWidgets) {
			ret.add(o);
		}
		for(Object o : widgets) {
			ret.add(o);
		}
		return ret;
	}
	
	@Override
	protected void refreshChildren() {
		super.refreshChildren();
		List children = getChildren();
		for(int i = 0; i < children.size(); i++) {
			Object o = children.get(i);
			if(o instanceof WidgetPart) {
				((WidgetPart)o).syncWithBorder();
			}
		}
		getModel().layoutSubWidgets();
	}
	
	protected void syncWithBorder() {
		Point p = getModel().getLocationWithBorder();
		if(!getFigure().getBounds().getLocation().equals(p)) {
			getFigure().setLocation(p);
		}
	}
	
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("location")) {
			getFigure().setLocation(getModel().getLocationWithBorder());
			getFigure().repaint();
		} else if(evt.getPropertyName().equals("size")) {
			getFigure().setSize((Dimension)evt.getNewValue());
			getFigure().repaint();
		}
		if(evt.getPropertyName().equals("state")) {
			getFigure().repaint();
		}
		if(evt.getPropertyName().equals("child")) {
			refresh();
			return;
		}
		if(evt.getPropertyName().equals("inset")) {
			refresh();
			getFigure().repaint();
		}
		if(evt.getPropertyName().equals("border")) {
			refresh();
			getFigure().repaint();
		}
		if(evt.getPropertyName().equals("showInEditing")) {
			getFigure().setVisible((Boolean)evt.getNewValue());
		}
		if(evt.getPropertyName().equals("content")) {
			getFigure().repaint();
		}
		if(evt.getPropertyName().equals("textColor")) {
			getFigure().repaint();
		}
		if(evt.getPropertyName().equals("shadowColor")) {
			getFigure().repaint();
		}
		if(evt.getPropertyName().equals("linkColor")) {
			getFigure().repaint();
		}
		if(evt.getPropertyName().equals("shadow")) {
			getFigure().repaint();
		}
		if(evt.getPropertyName().equals("lineGap")) {
			getFigure().repaint();
		}
	}
	
	@Override
	public void performRequest(Request req) {
		if (RequestConstants.REQ_OPEN.equals(req.getType())) {
			String[] events = getModel().getEvents();
			if(events.length == 0)
				return;
			String script = getModel().getScreen().getScript();
			if (script != null && script.length() > 0) {
				File file = new File(Settings.workingDir, script);
				if (file.exists()) {
					GTLPreCompiler compiler = new GTLPreCompiler();
					IFileStore filestore = EFS.getLocalFileSystem().getStore(
							file.toURI());
					FileStoreEditorInput input = new FileStoreEditorInput(
							filestore);
					GTLEditorImpl editor = (GTLEditorImpl) PlatformUI
							.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().findEditor(input);
					String content = null;
					if(editor != null) {
						content = editor.getDocument().get();
					} else {
						try {
							content = GTLPreCompiler.loadFileContent(file);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					try {
						compiler.parse(file, content);
						FunctionDef[] functions = compiler.getFunctions();
						int lineno = -1;
						for(int i = 0; i < functions.length; i++) {
							if(functions[i].name.equals(events[0])) {
								lineno = functions[i].lineNo;
								break;
							}
						}
						if (lineno > 0) {
							lineno = compiler.getLineOfLine(lineno);
							editor = (GTLEditorImpl) IDE.openEditorOnFileStore(
									PlatformUI.getWorkbench()
											.getActiveWorkbenchWindow()
											.getActivePage(), filestore);
							editor.jumpToLine(lineno);
							
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
