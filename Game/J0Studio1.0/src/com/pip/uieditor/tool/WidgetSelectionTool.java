package com.pip.uieditor.tool;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.tools.SelectionTool;

import com.pip.uieditor.model.Region;

public class WidgetSelectionTool extends SelectionTool {
	protected EditPartViewer.Conditional getTargetingConditional() {
		return new EditPartViewer.Conditional() {
			public boolean evaluate(EditPart editpart) {
				EditPart targetEditPart = editpart
						.getTargetEditPart(getTargetRequest());
				return targetEditPart != null && targetEditPart.isSelectable() && !(targetEditPart.getModel() instanceof Region);
			}
		};
	}
}
