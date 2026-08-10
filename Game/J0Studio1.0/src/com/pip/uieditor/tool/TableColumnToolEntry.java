package com.pip.uieditor.tool;

import org.eclipse.gef.SharedCursors;
import org.eclipse.gef.Tool;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.jface.resource.ImageDescriptor;

public class TableColumnToolEntry extends ToolEntry {


	public TableColumnToolEntry(String label, String shortDesc,
			ImageDescriptor iconSmall, ImageDescriptor iconLarge) {
		super(label, shortDesc, iconSmall, iconLarge);
	}
	
	@Override
	public Tool createTool() {
		TableColumnTool tool = new TableColumnTool();
		tool.setUnloadWhenFinished(true);
		tool.setDefaultCursor(SharedCursors.CURSOR_TREE_ADD);
		tool.setDisabledCursor(SharedCursors.NO);
		return tool;
	}
}
