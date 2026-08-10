package com.pip.uieditor.tool;

import org.eclipse.gef.SharedCursors;
import org.eclipse.gef.Tool;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.jface.resource.ImageDescriptor;

import com.pip.uieditor.model.Region;

public class CreateRegionToolEntry extends ToolEntry {
	
	private Class<? extends Region> type;

	public CreateRegionToolEntry(Class<? extends Region> type, String label, String shortDesc,
			ImageDescriptor iconSmall, ImageDescriptor iconLarge) {
		super(label, shortDesc, iconSmall, iconLarge);
		this.type = type;
	}

	@Override
	public Tool createTool() {
		CreateRegionTool tool = new CreateRegionTool(type);
		tool.setUnloadWhenFinished(true);
		tool.setDefaultCursor(SharedCursors.HAND);
		return tool;
	}
	
	
	
}	
