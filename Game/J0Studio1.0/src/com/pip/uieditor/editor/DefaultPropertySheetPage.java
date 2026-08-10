package com.pip.uieditor.editor;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.properties.UndoablePropertySheetPage;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.views.properties.IPropertySheetEntry;
import org.eclipse.ui.views.properties.PropertySheetSorter;

/**
 * 为了覆盖UndoablePropertySheetPage的sort派生的一个PropertySheetPage。其中的属性排序按照加入的顺序
 * @author Jeffrey
 *
 */
public class DefaultPropertySheetPage extends UndoablePropertySheetPage {

	public DefaultPropertySheetPage(CommandStack commandStack,
			IAction undoAction, IAction redoAction) {
		super(commandStack, undoAction, redoAction);
		setSorter(new DefaultSorter());
	}
	
	
	static class DefaultSorter extends PropertySheetSorter {

		@Override
		public int compare(IPropertySheetEntry entryA,
				IPropertySheetEntry entryB) {
			return -1;
		}

		@Override
		public int compareCategories(String categoryA, String categoryB) {
			return -1;
		}
		
	}
}
