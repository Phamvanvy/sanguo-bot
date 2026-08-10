package com.pip.uieditor.commands;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.commands.Command;

import com.pip.uieditor.model.Widget;

/**
 * 改变Widget的bounds的Command
 * @author Jeffrey
 *
 */
public class AdjustConstraintCommand extends Command {
	
	private GraphicalEditPart part;
	private Rectangle oldBounds, newBounds;
	
	public AdjustConstraintCommand(GraphicalEditPart part, Rectangle constraint) {
		this.part = part;
		this.newBounds = constraint;
		this.oldBounds = part.getFigure().getBounds().getCopy();
	}
	
	@Override
	public void execute() {
		redo();
	}
	
	@Override
	public void redo() {
		Widget widget = (Widget)part.getModel();
		widget.setBounds(newBounds.getTranslated(-widget.getParentClientAreaX(), -widget.getParentClientAreaY()));
	}
	
	@Override
	public void undo() {
		Widget widget = (Widget)part.getModel();
		widget.setBounds(oldBounds);
	}
}
