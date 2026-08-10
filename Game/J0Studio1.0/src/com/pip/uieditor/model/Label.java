package com.pip.uieditor.model;

public class Label extends Widget {
	
	public static final Label PROTOTYPE = new Label();
	
	public Label() {
		super("Label");
		Region region = new StringRegion();
		region.setId("_TEXT");
		region.setRequire(true);
		region.addAnchorPoint(new AnchorPoint(Anchor.LEFT, Anchor.LEFT));
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
		return "lbl";
	}
	
	@Override
	public Label clone() {
		Label ret = new Label();
		fillCloneWidget(ret);
		return ret;
	}
}
