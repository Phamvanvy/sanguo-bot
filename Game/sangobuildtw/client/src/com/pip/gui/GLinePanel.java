package com.pip.gui;

import com.pip.common.Tool;
import com.pip.common.Utilities;
import com.pip.image.ImageSet;
import com.pip.sanguo.GameMain;
import com.pip.ui.VM;
import com.pip.ui.VMGame;

public class GLinePanel extends GContainer implements IGPaint{
	int[] colors;
	ImageSet cornerRes;
	int index;
	
	public GLinePanel(VMGame _vmGame, int self, int[] vmData, String name) {
		super(_vmGame, self, vmData, name);		
	}
	
	public GWidget getClone(VMGame _vmGame) {
		GLinePanel gLinePanel = new GLinePanel(_vmGame, 0, getVMDataCopy(), name);
		setCloneData(gLinePanel);
		gLinePanel.cornerRes = this.cornerRes;
		gLinePanel.index = this.index;
		
		if(this.colors != null) {
			gLinePanel.colors = new int[this.colors.length];
			System.arraycopy(this.colors, 0, gLinePanel.colors, 0, gLinePanel.colors.length);
		}		
		
		return gLinePanel;
	}
	
	/**
	 * 设置颜色和四角的图片
	 * 
	 * @param colors
	 * @param cornerResName
	 * @param index
	 */
	public void setData(int[] colors, String cornerResName, int index){
		this.colors = colors;
		if(cornerResName != null) {
			this.cornerRes = (ImageSet) Tool.getGlobalObject(cornerResName);
			this.index = index;
			
			this.vmData[GW_VM_MIN_WIDTH] = cornerRes.getFrameWidth(index) * 2;
			this.vmData[GW_VM_MIN_HEIGHT] = cornerRes.getFrameHeight(index) * 2;
		}
	}
	
	/* (non-Javadoc)
	 * @see com.pip.gui.IGPaint#paint()
	 */
	public void paint() {
		int[] rect;
		if(parentNeedScroll()) {
			rect = getIntersect();		
			Utilities.graphics.setClip(rect[0], rect[1], rect[2], rect[3]);
		} else {
			Utilities.graphics.setClip(0, 0, GameMain.viewWidth, GameMain.viewHeight);
		}
		
		//绘制背景
		if(colors != null) {
			Tool.drawFrameBox(
					Utilities.graphics, 
					this.vmData[GW_VM_OFFSET_X] + vmData[GW_VM_XX], 
					this.vmData[GW_VM_OFFSET_Y] + vmData[GW_VM_YY], 
					vmData[GW_VM_W], 
					vmData[GW_VM_H], 
					colors);
		}		
		
		//绘制四角
		if(cornerRes != null) {
			Tool.drawBoxCorner(
					Utilities.graphics, 
					this.vmData[GW_VM_OFFSET_X] + vmData[GW_VM_XX], 
					this.vmData[GW_VM_OFFSET_Y] + vmData[GW_VM_YY], 
					vmData[GW_VM_W], 
					vmData[GW_VM_H], 
					cornerRes,
					index);
		}
		
//		Utilities.graphics.setClip(0, 0, GameMain.viewWidth, GameMain.viewHeight);
	}
	
	
}
