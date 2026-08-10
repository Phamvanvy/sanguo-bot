package com.pip.uieditor.commands;

import org.eclipse.gef.commands.Command;

import com.pip.uieditor.model.AnchorPoint;
import com.pip.uieditor.model.Region;
import com.pip.uieditor.model.Widget;

public class CreateRegionCommand extends Command {
	
	private Class<? extends Region> type;
	private Widget widget;
	private String regionId;
	private int layer;
	private Region region;
	
	public void setType(Class<? extends Region> type) {
		this.type = type;
	}
	
	public void setWidget(Widget widget) {
		this.widget = widget;
	}
	
	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}
	
	public void setLayer(int layer) {
		this.layer = layer;
	}
	
	
	@Override
	public void execute() {
		redo();
	}


	@Override
	public void redo() {
		try {
			region = type.newInstance();
			region.setId(regionId);
			region.setLayer(layer);
			AnchorPoint[] anchors = region.getDefaultAnchorPoints();
			for(int i = 0; i < anchors.length; i++) {
				region.addAnchorPoint(anchors[i]);
			}
			widget.addRegion(region);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	public void undo() {
		if(region != null) {
			widget.removeRegion(region);
		}
	}
	
}
