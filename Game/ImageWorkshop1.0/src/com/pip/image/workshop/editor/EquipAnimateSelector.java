package com.pip.image.workshop.editor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import com.pip.mango.jni.GLGraphics;
import com.pipimage.image.PipAnimateSet;

public class EquipAnimateSelector extends TileLibSelector {
	private EquipEditor editor;
	
	public EquipAnimateSelector(Composite parent, int style, EquipEditor editor) {
		super(parent, style);
		this.editor = editor;
	}
	
	protected void prepareMenu() {
		MenuManager mgr = new MenuManager();
        
        mgr.add(new Action("编辑此动画") {
            public void run() {
            	onEditAnimate();
            }
        });
        if (getMenu() != null) {
        	getMenu().dispose();
        }
        setMenu(mgr.createContextMenu(this));
	}
	
	protected void onEditAnimate() {
		int[] sels = getSelectedFrames();
		if (sels.length == 0) {
			return;
		}
		editor.editAnimate(sels[0]);
	}
	
	protected void drawInformation(GC gc) {
		super.drawInformation(gc);
		
		String info2 = "SHFIT+左  -  前移   SHIFT+右  -  后移";
        Point ts = gc.textExtent(info2);
        Point size = getSize();
        gc.drawText(info2, size.x / 2 - ts.x / 2, size.y - ts.y * 2 - 5);
	}
	
	protected void drawInformation(GLGraphics gc) {
		super.drawInformation(gc);
		
		String info2 = "SHFIT+左  -  前移   SHIFT+右  -  后移";
        Point ts = gc.textExtent(info2);
        Point size = getSize();
        gc.drawText(info2, size.x / 2 - ts.x / 2, size.y - ts.y * 2 - 5);
	}
	
	protected void onKeyDown(int keyCode) {
		super.onKeyDown(keyCode);
		
		switch (keyCode) {
		case SWT.ARROW_LEFT:
			if ((keyEventMask & SWT.SHIFT) != 0) {
				onMoveUp();
			}
			break;
		case SWT.ARROW_RIGHT:
			if ((keyEventMask & SWT.SHIFT) != 0) {
				onMoveDown();
			}
			break;
		}
		return;
	}
	
	protected void onMoveUp() {
		PipAnimateSet ani = (PipAnimateSet)input;
		if (selectedFrames.size() != 1) {
			MessageDialog.openInformation(getShell(), "错误", "请选择一个动画序列。");
			return;
		}
		int index = selectedFrames.get(0).intValue();
		if (index == 0) {
			return;
		}
		ani.swapAnimates(index - 1, index);
		selectedFrames.clear();
		selectedFrames.add(index - 1);
		redraw();
		this.fireContentChanged();
	}
	
	protected void onMoveDown() {
		PipAnimateSet ani = (PipAnimateSet)input;
		if (selectedFrames.size() != 1) {
			MessageDialog.openInformation(getShell(), "错误", "请选择一个动画序列。");
			return;
		}
		int index = selectedFrames.get(0).intValue();
		if (index == ani.getAnimateCount() - 1) {
			return;
		}
		ani.swapAnimates(index + 1, index);
		selectedFrames.clear();
		selectedFrames.add(index + 1);
		redraw();
		this.fireContentChanged();
	}
}
