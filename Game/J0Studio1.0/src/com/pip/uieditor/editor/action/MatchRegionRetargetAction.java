package com.pip.uieditor.editor.action;

import org.eclipse.gef.internal.InternalImages;
import org.eclipse.ui.actions.LabelRetargetAction;

public class MatchRegionRetargetAction extends LabelRetargetAction {
	
	public MatchRegionRetargetAction() {
		super(null, null);
		setId(MatchRegionAction.ID);
		setText("Match");
		setToolTipText("");
		setImageDescriptor(InternalImages.DESC_MATCH_SIZE);
		setDisabledImageDescriptor(InternalImages.DESC_MATCH_SIZE_DIS);
	}
}
