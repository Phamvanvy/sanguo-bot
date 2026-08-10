package com.pip.gui;

import com.pip.common.Tool;
import com.pip.common.Utilities;
import com.pip.image.ImageSet;
import com.pip.sanguo.GameMain;
import com.pip.ui.VMGame;

public class GImageNumer extends GWidget implements IGPaint{
	ImageSet numerResName;
	String number;
	int iNumber;
	int plusStartIndex; //正数的起始索引
	int subStartIndex;  //负数的起始索引
	int space;
	int anchor;
	boolean isShowSign;    //是否显示正负号
	
	public GImageNumer(VMGame _vmGame, int self, int[] vmData, String name) {
		super(_vmGame, self, vmData, name);	
	}
	
	public GWidget getClone(VMGame _vmGame) {
		GImageNumer gImageNumer = new GImageNumer(_vmGame, 0, getVMDataCopy(), name);
		setCloneData(gImageNumer);
		gImageNumer.numerResName = this.numerResName;
		gImageNumer.number = this.number;
		gImageNumer.iNumber = this.iNumber;
		gImageNumer.plusStartIndex = this.plusStartIndex;
		gImageNumer.subStartIndex = this.subStartIndex;
		gImageNumer.space = this.space;
		gImageNumer.anchor = this.anchor;
		gImageNumer.isShowSign = this.isShowSign;
		
		return gImageNumer;
	}
	
	public void setData(String numerResName, boolean isShowSign, int plusStartIndex, int subStartIndex, int space, int anchor) {
		if(numerResName != null) {
			this.numerResName = (ImageSet) Tool.getGlobalObject(numerResName);
			this.plusStartIndex = plusStartIndex;
			this.subStartIndex = subStartIndex;
			this.space = space;
			this.anchor = anchor;
			
			this.isShowSign = isShowSign;
		}
	}
	
	public void setNumer(String number) {
		this.number = number;
		if(number != null) {
			iNumber = Integer.parseInt(number);
			if(iNumber >= 0) {
				if(isShowSign) {
					this.number = "+" + iNumber;
				} else {
					this.number = "" + iNumber;
				}			
			} else {
				if(isShowSign) {
					this.number = "-" + (-iNumber);
				} else {
					this.number = "" + (-iNumber);
				}		
			}
		}
		
	}

	public void paint() {
		//#if NewUI2
		//#else
		if(parentNeedScroll()) {
			getIntersect(rect);		
			Utilities.graphics.setClip(rect[0], rect[1], rect[2], rect[3]);
		}
		//#endif
		if(numerResName != null && number != null) {
			if(iNumber >=0 ) {
				Tool.drawImageNumber(Utilities.graphics, numerResName, plusStartIndex, number, this.vmData[GW_VM_OFFSET_X] + vmData[GW_VM_XX] + vmData[GW_VM_BORDERLEFT], this.vmData[GW_VM_OFFSET_Y] + vmData[GW_VM_YY] + vmData[GW_VM_BORDERTOP], space, anchor);	
			} else {
				Tool.drawImageNumber(Utilities.graphics, numerResName, subStartIndex, number, this.vmData[GW_VM_OFFSET_X] + vmData[GW_VM_XX] + vmData[GW_VM_BORDERLEFT], this.vmData[GW_VM_OFFSET_Y] + vmData[GW_VM_YY] + vmData[GW_VM_BORDERTOP], space, anchor);
			}
				
		}
		//#if NewUI2
		//#else
		Utilities.graphics.setClip(0, 0, GameMain.viewWidth, GameMain.viewHeight);
		//#endif
	}
	
	
}
