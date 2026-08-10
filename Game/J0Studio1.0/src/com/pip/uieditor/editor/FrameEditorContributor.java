package com.pip.uieditor.editor;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.internal.GEFMessages;
import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.gef.ui.actions.AlignmentRetargetAction;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.MatchHeightRetargetAction;
import org.eclipse.gef.ui.actions.MatchSizeRetargetAction;
import org.eclipse.gef.ui.actions.MatchWidthRetargetAction;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.gef.ui.actions.ZoomInRetargetAction;
import org.eclipse.gef.ui.actions.ZoomOutRetargetAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.RetargetAction;

import com.pip.uieditor.editor.action.AttachScriptAction;
import com.pip.uieditor.editor.action.GenerateCodeAction;
import com.pip.uieditor.editor.action.MatchRegionAction;
import com.pip.uieditor.editor.action.MatchRegionRetargetAction;
import com.pip.uieditor.model.Screen;

public class FrameEditorContributor extends ActionBarContributor {

	
	private Screen curScreen;
	
	public FrameEditorContributor() {
//		changeScreenSizeAction =  new ControlContribution("com.pip.uieditor.switchsize") {
//			@Override
//			protected Control createControl(Composite parent) {
//				Combo c  = new Combo(parent, SWT.READ_ONLY);
//				c.setItems(Screen.SCREEN_SIZE_STRING.clone());
//				c.select(Screen.getScreenSizeIndex(curScreen.getSize().width, curScreen.getSize().height));
//				return c;
//			}			
//		};
	}
	
	@Override
	protected void buildActions() {
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.LEFT));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.CENTER));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.RIGHT));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.TOP));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.MIDDLE));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.BOTTOM));
		addRetargetAction(new MatchHeightRetargetAction());
		addRetargetAction(new MatchWidthRetargetAction());
		addRetargetAction(new MatchSizeRetargetAction());
		addRetargetAction(new ZoomInRetargetAction());
		addRetargetAction(new ZoomOutRetargetAction());
		addRetargetAction(new RetargetAction(
				GEFActionConstants.TOGGLE_GRID_VISIBILITY,
				GEFMessages.ToggleGrid_Label, IAction.AS_CHECK_BOX));
		addRetargetAction(new MatchRegionRetargetAction());
		addRetargetAction(new RetargetAction(GenerateCodeAction.ID, "Generate Code"));
		addRetargetAction(new RetargetAction(AttachScriptAction.ID, "Attach"));
//		addRetargetAction(new DeleteRegionRetargetAction());
//		addRetargetAction(new RetargetAction(
//				GEFActionConstants.TOGGLE_SNAP_TO_GEOMETRY,
//				GEFMessages.ToggleSnapToGeometry_Label, IAction.AS_CHECK_BOX));
//		addRetargetAction(new RetargetAction(
//				GEFActionConstants.TOGGLE_RULER_VISIBILITY,
//				GEFMessages.ToggleRulerVisibility_Label, IAction.AS_CHECK_BOX));
	}

	@Override
	protected void declareGlobalActionKeys() {
		addGlobalActionKey(ActionFactory.UNDO.getId());
		addGlobalActionKey(ActionFactory.REDO.getId());
		addGlobalActionKey(ActionFactory.DELETE.getId());
		addGlobalActionKey(ActionFactory.COPY.getId());
		addGlobalActionKey(ActionFactory.PASTE.getId());
	}
	
	public void contributeToToolBar(IToolBarManager tbm) {
		tbm.add(new Separator());
		tbm.add(getAction(GEFActionConstants.ALIGN_LEFT));
		tbm.add(getAction(GEFActionConstants.ALIGN_CENTER));
		tbm.add(getAction(GEFActionConstants.ALIGN_RIGHT));
		tbm.add(new Separator());
		tbm.add(getAction(GEFActionConstants.ALIGN_TOP));
		tbm.add(getAction(GEFActionConstants.ALIGN_MIDDLE));
		tbm.add(getAction(GEFActionConstants.ALIGN_BOTTOM));
		tbm.add(new Separator());
		tbm.add(getAction(GEFActionConstants.MATCH_WIDTH));
		tbm.add(getAction(GEFActionConstants.MATCH_HEIGHT));
		tbm.add(getAction(GEFActionConstants.MATCH_SIZE));
		tbm.add(new Separator());
		tbm.add(getAction(GEFActionConstants.ZOOM_IN));
		tbm.add(getAction(GEFActionConstants.ZOOM_OUT));
		tbm.add(new ZoomComboContributionItem(getPage()));
		tbm.add(new Separator());
		tbm.add(getAction(GEFActionConstants.TOGGLE_GRID_VISIBILITY));
		tbm.add(getAction(MatchRegionAction.ID));
		tbm.add(getAction(GenerateCodeAction.ID));
		tbm.add(getAction(AttachScriptAction.ID));
//		tbm.add(getAction(DeleteRegionAction.ID));
		tbm.add(new Separator());
//		tbm.add(new ControlContribution("com.pip.uieditor.switchsize") {
//			@Override
//			protected Control createControl(Composite parent) {
//				Combo c  = new Combo(parent, SWT.READ_ONLY);
//				c.setItems(Screen.SCREEN_SIZE_STRING.clone());
//				c.select(Screen.getScreenSizeIndex(curScreen.getSize().width, curScreen.getSize().height));
//				return c;
//			}			
//		});
	}

	@Override
	public void setActiveEditor(IEditorPart editor) {
		super.setActiveEditor(editor);
		curScreen = ((FrameEditor)editor).getScreen();
	}
	
	
}
