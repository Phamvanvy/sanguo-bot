package com.pip.uieditor.policies;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;

import com.pip.uieditor.commands.CreateTabButtonCommand;
import com.pip.uieditor.commands.CreateTableColumnCommand;
import com.pip.uieditor.commands.UnableCreateCommand;
import com.pip.uieditor.model.TabBar;
import com.pip.uieditor.model.Table;
import com.pip.uieditor.model.TableColumn;

public class WidgetLayoutEditPolicy extends XYLayoutEditPolicy {

	@Override
	protected Command getCreateCommand(CreateRequest request) {
			return UnableCreateCommand.INSTANCE;
	}
	
	@Override
	public EditPart getTargetEditPart(Request request) {
		if("Create Region".equals(request.getType()))
			return getHost();
		if("Create TableColumn".equals(request.getType())) {
			if(!isTable()) {
				return null;
			} else{
				return getHost();
			}
		}
		if("Create TabButton".equals(request.getType())) {
			if(!isTabBar()) {
				return null;
			} else {
				return getHost();
			}
		}
		return super.getTargetEditPart(request);
	}
	
	protected boolean isTabBar() {
		return getHost().getModel() instanceof TabBar;
	}
	
	protected boolean isTable() {
		return getHost().getModel() instanceof Table;
	}
	
	@Override
	public void showTargetFeedback(Request request) {
		if("Create TableColumn".equals(request.getType())) {
			showCreateTableColumnFeedback(request);
		} else if("Create TabButton".equals(request.getType())) {
			showCreateTabButtonFeedback(request);
		}else {
			super.showTargetFeedback(request);
		}
	}
	
	@Override
	public Command getCommand(Request request) {
		if("Create TableColumn".equals(request.getType())) {
			if(isTable()) {
				CreateTableColumnCommand command = new CreateTableColumnCommand();
				command.setTable((Table)getHost().getModel());
				CreateRequest createRequest = (CreateRequest)request;
				TableColumn column = (TableColumn)createRequest.getNewObject();
				command.setTableColumn(column);
				return command;
			} else {
				return null;
			}
		} else if("Create TabButton".equals(request.getType())) {
			if(isTabBar()) {
				CreateTabButtonCommand command = new CreateTabButtonCommand();
				command.setTabBar((TabBar)getHost().getModel());
				return command;
			}
		}
		return super.getCommand(request);
	}
	
	protected void showCreateTableColumnFeedback(Request request) {
		if(getHost().getModel() instanceof Table) {
			
		}
	}
	
	protected void showCreateTabButtonFeedback(Request request) {
		
	}
	
//	@Override
//	protected IFigure createSizeOnDropFeedback(CreateRequest createRequest) {
//		ResizeFigure figure = new ResizeFigure();
//		figure.setBounds(new Rectangle(createRequest.getLocation(), createRequest.getSize()));
//		FigureUtilities.makeGhostShape((Shape) figure);
//		((Shape) figure).setLineStyle(Graphics.LINE_DASHDOT);
//		figure.setForegroundColor(ColorConstants.white);
//		addFeedback(figure);
//		return figure;
//	}
}
