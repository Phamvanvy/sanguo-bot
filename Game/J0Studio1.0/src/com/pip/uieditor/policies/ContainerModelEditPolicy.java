package com.pip.uieditor.policies;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;

import com.pip.uieditor.commands.AdjustConstraintCommand;
import com.pip.uieditor.commands.UnableCreateCommand;
import com.pip.uieditor.commands.WidgetCreateCommand;
import com.pip.uieditor.model.Container;
import com.pip.uieditor.model.Widget;

public class ContainerModelEditPolicy extends XYLayoutEditPolicy {

//	@Override
//	protected EditPolicy createChildEditPolicy(EditPart child) {
//		return new NonResizableEditPolicy();
//	}

	@Override
	protected Command getCreateCommand(CreateRequest request) {
		if(request.getLocation() == null)
			return UnableCreateCommand.INSTANCE;
		Container container =  (Container)getHost().getModel();
		WidgetCreateCommand command = new WidgetCreateCommand();
		command.setContainer(container);
		command.setLocation(request.getLocation());
		command.setSize(request.getSize());
		command.setWidget((Widget)request.getNewObject());
		return command;
	}

	@Override
	protected Command createChangeConstraintCommand(
			ChangeBoundsRequest request, EditPart child, Object constraint) {
		return new AdjustConstraintCommand((GraphicalEditPart)child, (Rectangle)constraint);
	}

	@Override
	public EditPart getTargetEditPart(Request request) {
		if(request.getType().equals("Create Region"))
			return getHost();
		return super.getTargetEditPart(request);
	}
}
