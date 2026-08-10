package com.pip.uieditor.commands;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.commands.Command;

import com.pip.uieditor.model.Container;
import com.pip.uieditor.model.Widget;

/**
 * 删除Widget的Command，每一个Command只负责删除一个Widget，如果一次选中多个，那么将会被分解
 * @author Jeffrey
 *
 */
public class DeleteWidgetCommand extends Command {
	
	private GraphicalEditPart part;
	
	private Widget parent;
	private Widget widget;
	
	public DeleteWidgetCommand(GraphicalEditPart part) {
		this.part = part;
	}

	@Override
	public void execute() {
		redo();
	}

	@Override
	public void redo() {
		widget = (Widget)part.getModel();
		parent = (Widget)widget.getParent();
		parent.removeChild(widget);
		
	}

	@Override
	public void undo() {
		parent.addChild(widget);
	}
}
