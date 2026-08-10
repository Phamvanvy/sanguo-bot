package com.pip.uieditor.parts;

import java.util.List;

import com.pip.uieditor.model.Screen;

public class ScreenTreePart extends ContainerTreePart {

	public ScreenTreePart(Screen model) {
		super(model);
	}
	
	@Override
	public Screen getModel() {
		return (Screen)super.getModel();
	}
	
	@Override
	public String getText() {
		return "Screen";
	}
	
	@Override
	public List getModelChildren() {
		return getModel().getChildren();
	}
}
