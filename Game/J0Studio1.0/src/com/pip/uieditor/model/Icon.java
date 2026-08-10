package com.pip.uieditor.model;

public class Icon extends Widget {
	
	public static final Icon PROTOTYPE = new Icon();
	
	public Icon() {
		super("Icon");
		Region region = new ImageRegion();
		region.setId("_ICON");
		region.setRequire(true);
		region.addAnchorPoint(new AnchorPoint(Anchor.CENTER, Anchor.CENTER));
		region.setLayer(LAYER_OVERLAY);
		addRegion(region);
	}
	
	@Override
	public void initFlags() {
		setClickable(false);
		setLongClickable(false);
		setHorizontalScrollBarEnabled(false);
		setVerticalScrollBarEnabled(false);
		setScrollContainer(false);
		setFocusable(false);
	}
	
	@Override
	public String getDefaultName() {
		return "icn";
	}
	
	@Override
	public Icon clone() {
		Icon ret = new Icon();
		fillCloneWidget(ret);
		return ret;
	}
}
