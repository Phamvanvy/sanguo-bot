package com.pip.uieditor.tool;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.tools.SelectionTool;

import com.pip.j0ide.Activator;
import com.pip.uieditor.commands.CreateRegionCommand;
import com.pip.uieditor.editor.NewRegionDialog;
import com.pip.uieditor.model.Region;
import com.pip.uieditor.model.Screen;
import com.pip.uieditor.model.Widget;

public class CreateRegionTool extends SelectionTool {
	
	private Class<? extends Region> type;
	
	public CreateRegionTool(Class<? extends Region> type) {
		this.type = type;
	}

	@Override
	protected boolean handleButtonDown(int button) {
		EditPart editPart = getTargetEditPart();
		Object model = editPart.getModel();
		if(model instanceof Widget && !(model instanceof Screen)) {
			NewRegionDialog dlg = new NewRegionDialog(Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell());
			if(dlg.open() == NewRegionDialog.OK) {
				String regionId = dlg.getId();
				int layer = dlg.getLayer();
				if(regionId.length() > 0) {
					CreateRegionCommand command  = new CreateRegionCommand();
					command.setRegionId(regionId);
					command.setLayer(layer);
					command.setType(type);
					command.setWidget((Widget)model);
					getDomain().getCommandStack().execute(command);
				}
			}
		}
		setState(STATE_TERMINAL);
		handleFinished();
		return true;
	}

	@Override
	protected String getCommandName() {
		return "Create Region";
	}
	
}
