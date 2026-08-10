package com.pip.uieditor.tool;

import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.tools.CreationTool;

import com.pip.uieditor.model.TabButton;

public class TabButtonTool extends CreationTool {

	public TabButtonTool() {
		super(new TabButtonCreationFactory());
	}

	@Override
	protected Request createTargetRequest() {
		CreateRequest request = new CreateRequest("Create TabButton");
		request.setFactory(getFactory());
		return request;
	}
	
	@Override
	protected String getCommandName() {
		return "Create TabButton";
	}
	
	@Override
	protected boolean handleButtonDown(int button) {
		if (button != 1) {
			setState(STATE_INVALID);
			handleInvalidInput();
			return true;
		}
		if (stateTransition(STATE_INITIAL, STATE_DRAG)) {
			getCreateRequest().setLocation(getLocation());
			lockTargetEditPart(getTargetEditPart());
		}
		return true;
	}
	
	@Override
	protected boolean handleMove() {
		if(!isTargetLocked()) {
			updateTargetRequest();
			updateTargetUnderMouse();
			Command command = getCommand();
			setCurrentCommand(command);
			showTargetFeedback();
			
		} 
		return true;
	}
	
	@Override
	protected boolean handleDragInProgress() {
		return true;
	}
	
	protected void updateTargetRequest() {
		CreateRequest createRequest = getCreateRequest();
		createRequest.setSize(null);
		createRequest.setLocation(getLocation());
		createRequest.setSnapToEnabled(false);
	}
}

class TabButtonCreationFactory implements CreationFactory{

	@Override
	public Object getNewObject() {
		return new TabButton();
	}

	@Override
	public Object getObjectType() {
		return null;
	}
	
}

