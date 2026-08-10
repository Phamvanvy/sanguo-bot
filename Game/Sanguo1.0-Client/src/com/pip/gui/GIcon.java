package com.pip.gui;

import com.pip.common.Tool;
import com.pip.common.Utilities;
import com.pip.image.ImageSet;
import com.pip.sanguo.GameMain;
import com.pip.ui.VMGame;

public class GIcon extends GWidget implements IGPaint{
	int[] backColors;
	ImageSet iconRes;
	public int iconIndex;   //仅显示一个图标
	public int trans;
	public int anchor;
	
	ImageSet numberRes;
	public int numberIndex;
	public int numberSpace;
	public int numberX;
	public int numberY;
	public int number;
	
	//蒙版
	int maskRgb;
	boolean hasMask;
	
	public GIcon(VMGame _vmGame, int self, int[] vmData, String name) {
		super(_vmGame, self, vmData, name);				 
	}

	public GWidget getClone(VMGame _vmGame) {
		GIcon gIcon = new GIcon(_vmGame, 0, getVMDataCopy(), name);
		setCloneData(gIcon);
		if(this.backColors != null) {
			gIcon.backColors = new int[this.backColors.length];
			System.arraycopy(this.backColors, 0, gIcon.backColors, 0, gIcon.backColors.length);
		}		
		gIcon.iconRes = this.iconRes;
		gIcon.iconIndex = this.iconIndex;
		gIcon.trans = this.trans;
		gIcon.anchor = this.anchor;
		gIcon.numberRes = this.numberRes;
		gIcon.numberIndex = this.numberIndex;
		gIcon.numberSpace = this.numberSpace;
		gIcon.numberX = this.numberX;
		gIcon.numberY = this.numberY;
		gIcon.number = this.number;
		gIcon.maskRgb = this.maskRgb;
		gIcon.hasMask = this.hasMask;
		
		return gIcon;
	}
	
	/**
	 * 设置图片
	 * 
	 * @param colors
	 * @param cornerResName
	 * @param index
	 */
	public void setData(int[] backColors, String iconResName, int _iconIndex, int trans, int anchor){
		this.backColors = backColors;
		if(iconResName != null) {
			this.iconRes = (ImageSet) Tool.getGlobalObject(iconResName);
			setIconIndex(_iconIndex);
			
			this.trans = trans;
			this.anchor = anchor;
		}
	}
	
	/**
	 * 设置数字资源
	 * @param numberRes
	 */
	public void setNumberData(String numberResName, int numberIndex, int numberSpace, int numberX, int numberY) {
		this.numberRes    = (ImageSet) Tool.getGlobalObject(numberResName);
		this.numberIndex  = numberIndex;
		this.numberSpace  = numberSpace;
		this.numberX  = numberX;
		this.numberY  = numberY;
	}
	
	public void setBackColors(int[] backColors) {
		this.backColors = backColors;
	}
	
	public void setNumber(int number) {
		this.number = number;
	}
	
	public void setIconIndex(int _iconIndex) {
		iconIndex = _iconIndex;		
		
		if(iconIndex >= 0) {
			this.vmData[GW_VM_MIN_WIDTH] = iconRes.getFrameWidth(iconIndex);
			this.vmData[GW_VM_MIN_HEIGHT] = iconRes.getFrameHeight(iconIndex);	
			if(this.vmData[GW_VM_W] == 0) {
				this.vmData[GW_VM_W] = this.vmData[GW_VM_MIN_WIDTH];
				this.vmData[GW_VM_H] = this.vmData[GW_VM_MIN_HEIGHT];	
			}	
		}
		
	}
	
	public void setMask(boolean hasMask, int maskRgb) {
		this.hasMask = hasMask;
		this.maskRgb = maskRgb;
	}
	
	/* (non-Javadoc)
	 * @see com.pip.gui.IGPaint#paint()
	 */
	public void paint() {
		//#if NewUI2
		//#else
		if(parentNeedScroll()) {
			getIntersect(rect);
			Utilities.graphics.setClip(rect[0], rect[1], rect[2], rect[3]);
		}
		//#endif
		//绘制背景
		if(backColors != null) {
			Tool.drawFrameBox(
					Utilities.graphics, 
					this.vmData[GW_VM_OFFSET_X] + vmData[GW_VM_XX], 
					this.vmData[GW_VM_OFFSET_Y] + vmData[GW_VM_YY], 
					vmData[GW_VM_W], 
					vmData[GW_VM_H], 
					backColors);
		}

		
		//绘制icon
		if(iconRes != null && iconIndex >= 0) {
			iconRes.drawFrame(
					Utilities.graphics, 
					iconIndex,
					this.vmData[GW_VM_OFFSET_X] + vmData[GW_VM_XX] + vmData[GW_VM_W] / 2, 
					this.vmData[GW_VM_OFFSET_Y] + vmData[GW_VM_YY] + vmData[GW_VM_H] / 2, 
					trans,
					anchor);
		}
		if(numberRes != null && numberIndex >= 0 && number > 0) {
			Tool.drawImageNumber(Utilities.graphics, numberRes, numberIndex, (new Integer(number)).toString(), this.vmData[GW_VM_OFFSET_X] + vmData[GW_VM_XX] + vmData[GW_VM_W] - numberX,  this.vmData[GW_VM_OFFSET_Y] + vmData[GW_VM_YY] + vmData[GW_VM_H] - numberY, numberSpace, Tool.G_BOTTOMRIGHT);
		}
		
		if(hasMask) {
			Tool.fillAlphaRect(Utilities.graphics, maskRgb, this.vmData[GW_VM_OFFSET_X] + vmData[GW_VM_XX], this.vmData[GW_VM_OFFSET_Y] + vmData[GW_VM_YY], vmData[GW_VM_W], vmData[GW_VM_H]);
		}
		//#if NewUI2
		//#else
		Utilities.graphics.setClip(0, 0, GameMain.viewWidth, GameMain.viewHeight);
		//#endif
	}
	
}
