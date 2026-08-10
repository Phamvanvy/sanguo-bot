package com.pip.uieditor.model;

public class Frame extends Container {
	
	public final static Frame PROTOTYPE = new Frame();
	
	public Frame() {
		super("Frame");
//		Region region = new ColorRegion();
//		region.setId("_BACKGROUND_C");
//		region.addAnchorPoint(new AnchorPoint(Anchor.TOPLEFT, Anchor.TOPLEFT));
//		region.addAnchorPoint(new AnchorPoint(Anchor.BOTTOMRIGHT, Anchor.BOTTOMRIGHT));
//		region.setRequire(true);
//		addRegion(LAYER_BACKGROUND, region);
		
		Region region = new ImageRegion();
		region.setId("_TITLE_I");
		region.addAnchorPoint(new AnchorPoint(Anchor.TOP, Anchor.TOP));
		region.setRequire(true);
		region.setLayer(LAYER_OVERLAY);
		addRegion(region);
		
		region = new StringRegion();
		region.setId("_TITLE_S");
		region.addAnchorPoint(new AnchorPoint(Anchor.TOP, Anchor.TOP));
		region.setRequire(true);
		region.setLayer(LAYER_OVERLAY);
		addRegion(region);
		
		region = new ImageRegion();
		region.setId("_CLOSEBUTTON_NORMAL_I");
		region.addAnchorPoint(new AnchorPoint(Anchor.TOPRIGHT, Anchor.TOPRIGHT));
		region.setRequire(true);
		region.setLayer(LAYER_OVERLAY);
		addRegion(region);
		
		region = new ImageRegion();
		region.setId("_CLOSEBUTTON_PUSHED_I");
		region.addAnchorPoint(new AnchorPoint(Anchor.TOPRIGHT, Anchor.TOPRIGHT));
		region.setRequire(true);
		region.setVisible(false);
		region.setLayer(LAYER_OVERLAY);
		addRegion(region);
	}
	
	@Override
	public String getDefaultName() {
		return "frm";
	}
	
	public Frame clone() {
		Frame ret = new Frame();
		fillCloneContainer(ret);
		return ret;
	}
}
