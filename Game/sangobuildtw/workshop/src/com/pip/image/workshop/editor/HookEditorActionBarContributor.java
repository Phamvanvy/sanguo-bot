package com.pip.image.workshop.editor;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.*;

public class HookEditorActionBarContributor extends EditorActionBarContributor implements IPartListener, IPropertyListener, IPropertyChangeListener {
	private HookPositionEditor activeEditor;
	private IAction undoAction, redoAction;
	private IWorkbenchPage workbench;
	
	public void setActiveEditor(IEditorPart targetEditor) {
		super.setActiveEditor(targetEditor);
		workbench = targetEditor.getSite().getWorkbenchWindow().getActivePage();
		workbench.addPartListener(this);
		targetEditor.addPropertyListener(this);
	}
	
	public void init(IActionBars bars) {
		super.init(bars);
		IMenuManager menuMgr = bars.getMenuManager();
		IMenuManager editMenu = menuMgr.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
		if (editMenu != null) {
			IContributionItem[] items = editMenu.getItems();
			undoAction = ((ActionContributionItem)items[0]).getAction();
			redoAction = ((ActionContributionItem)items[1]).getAction();
			undoAction.addPropertyChangeListener(this);
			redoAction.addPropertyChangeListener(this);
		}
	}
	
	public void partActivated(IWorkbenchPart part) {
		if (part instanceof HookPositionEditor) {
			activeEditor = (HookPositionEditor)part;
			undoAction.setEnabled(activeEditor.canUndo());
			redoAction.setEnabled(activeEditor.canRedo());
		}
	}

	public void partBroughtToTop(IWorkbenchPart part) {}

	public void partClosed(IWorkbenchPart part) {}

	public void partDeactivated(IWorkbenchPart part) {
		if (part instanceof AnimateEditor) {
			activeEditor = null;
		}
		undoAction.setEnabled(false);
		redoAction.setEnabled(false);
	}

	public void partOpened(IWorkbenchPart part) {}
	
	public void propertyChanged(Object source, int propId) {
		if (source == activeEditor) {
			if (propId == AnimateEditor.PROPERTY_CANUNDO) {
				undoAction.setEnabled(activeEditor.canUndo());
			} else if (propId == AnimateEditor.PROPERTY_CANREDO) {
				redoAction.setEnabled(activeEditor.canRedo());
			}
		}
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (activeEditor == null) {
			return;
		}
		if (event.getSource() == undoAction) {
			if (event.getProperty().equals("chosen")) {
				activeEditor.undo();
			}
		} else if (event.getSource() == redoAction) {
			if (event.getProperty().equals("chosen")) {
				activeEditor.redo();
			}
		}
	}

	public void dispose() {
		super.dispose();
		undoAction.removePropertyChangeListener(this);
		redoAction.removePropertyChangeListener(this);
		if (workbench != null) {
            workbench.removePartListener(this);
        }
	}
}
