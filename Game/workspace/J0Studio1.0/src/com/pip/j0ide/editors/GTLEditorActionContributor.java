package com.pip.j0ide.editors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

public class GTLEditorActionContributor extends BasicTextEditorActionContributor {
	private IAction compileAction;
	private IAction outlineAction;
	private IAction openDeclAction;
	private IAction toggleBreakpointAction;
	
	public void contributeToMenu(IMenuManager menu) {
		super.contributeToMenu(menu);
		IContributionItem[] items = menu.getItems();
		for (int i = 0; i < items.length; i++) {
			if (items[i] instanceof MenuManager && ((MenuManager)items[i]).getMenuText().equals("&GTL")) {
				MenuManager mm = (MenuManager)items[i];
				ActionContributionItem action = (ActionContributionItem)mm.getItems()[0];
				compileAction = action.getAction();
				compileAction.setEnabled(true);
				action = (ActionContributionItem)mm.getItems()[3];
				outlineAction = action.getAction();
				outlineAction.setEnabled(true);
				action = (ActionContributionItem)mm.getItems()[4];
				openDeclAction = action.getAction();
				openDeclAction.setEnabled(true);
			} else if (items[i] instanceof MenuManager && ((MenuManager)items[i]).getMenuText().equals("&Debug")) {
				MenuManager mm = (MenuManager)items[i];
				ActionContributionItem action = (ActionContributionItem)mm.getItems()[10];
				toggleBreakpointAction = action.getAction();
				toggleBreakpointAction.setEnabled(true);
			}
		}
	}
	
	public void dispose() {
		super.dispose();
		compileAction.setEnabled(false);
		outlineAction.setEnabled(false);
		openDeclAction.setEnabled(false);
		toggleBreakpointAction.setEnabled(false);
	}
}
