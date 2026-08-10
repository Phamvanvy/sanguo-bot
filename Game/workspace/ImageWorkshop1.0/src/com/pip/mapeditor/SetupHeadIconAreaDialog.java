package com.pip.mapeditor;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.pip.mapeditor.data.NPCImageInfo;
import com.pipimage.image.PipAnimate;

/**
 * 
 * @author jhkang
 *
 */
public class SetupHeadIconAreaDialog extends SetupCollisionDialog {

	public SetupHeadIconAreaDialog(Shell parentShell, PipAnimate animate, NPCImageInfo npcInfo) {
		super(parentShell, animate, npcInfo);
	}
	@Override
	protected Control createDialogArea(Composite parent) {
		Control ret = super.createDialogArea(parent);
		editArea.setAreaCnt(1);
		editArea.setAreaSize(npcInfo.cw[0], npcInfo.ch[0]);
		return ret;
	}
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("…Ë÷√Õ∑œÒ«¯”Ú");
	}
}
