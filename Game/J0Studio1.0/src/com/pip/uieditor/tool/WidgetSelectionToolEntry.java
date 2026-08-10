package com.pip.uieditor.tool;

import org.eclipse.gef.SharedImages;
import org.eclipse.gef.palette.ToolEntry;

public class WidgetSelectionToolEntry extends ToolEntry {

	/**
	 * Creates a new SelectionToolEntry.
	 */
	public WidgetSelectionToolEntry() {
		this(null);
	}

	/**
	 * Constructor for SelectionToolEntry.
	 * 
	 * @param label
	 *            the label
	 */
	public WidgetSelectionToolEntry(String label) {
		this(label, null);
	}

	/**
	 * Constructor for SelectionToolEntry.
	 * 
	 * @param label
	 *            the label
	 * @param shortDesc
	 *            the description
	 */
	public WidgetSelectionToolEntry(String label, String shortDesc) {
		super(label, shortDesc, SharedImages.DESC_SELECTION_TOOL_16,
				SharedImages.DESC_SELECTION_TOOL_24, WidgetSelectionTool.class);
		if (label == null || label.length() == 0)
			setLabel("Widget Select");
		setUserModificationPermission(PERMISSION_NO_MODIFICATION);
	}

}
