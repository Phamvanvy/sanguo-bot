package com.pip.mapeditor;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import com.pip.mapeditor.data.NPCImageInfo;
import com.pip.util.AutoSelectAll;
import com.pipimage.image.PipAnimate;

/**
 * 编辑NPC图片碰撞区域的对话框。
 * @author lighthu
 */
public class SetupCollisionDialog extends Dialog {
	private PipAnimate animate;
	protected NPCImageInfo npcInfo;
	protected CollisionAreaEditor editArea;
	private Rectangle[] selectedArea;

    /**
	 * Create the dialog
	 * @param parentShell
	 * @param animate 目标动画序列
	 * @param npcInfo 如果不为null，则存有初始碰撞区域
	 */
	public SetupCollisionDialog(Shell parentShell, PipAnimate animate, NPCImageInfo npcInfo) {
		super(parentShell);
		this.animate = animate;
		this.npcInfo = npcInfo;
	}

	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new FillLayout());

		editArea = new CollisionAreaEditor(container, SWT.NONE);
		editArea.setInput(animate);
		if (npcInfo != null) {
		    Rectangle[] rects = new Rectangle[npcInfo.cx.length];
            Rectangle bounds = animate.getBounds();
		    for (int i = 0; i < rects.length; i++) {
		        rects[i] = new Rectangle(npcInfo.cx[i], npcInfo.cy[i], npcInfo.cw[i], npcInfo.ch[i]);
		        rects[i].x -= bounds.x;
		        rects[i].y -= bounds.y;
		    }
		    editArea.setSelectedArea(rects);
		} else {
		    editArea.setSelectedArea(null);
		}
		
		return container;
	}

	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "确定",
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				"取消", false);
	}

	/**
	 * Return the initial size of the dialog
	 */
	protected Point getInitialSize() {
		return new Point(483, 541);
	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("设置碰撞区域");
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
		    this.selectedArea = editArea.getSelectedArea();
		    if (this.selectedArea != null) {
		        Rectangle bounds = animate.getBounds();
		        for (Rectangle rect : selectedArea) {
		            rect.x += bounds.x;
		            rect.y += bounds.y;
		        }
		    }
		}
		super.buttonPressed(buttonId);
	}
	
	public Rectangle[] getSelectedArea() {
	    return this.selectedArea;
	}
}
