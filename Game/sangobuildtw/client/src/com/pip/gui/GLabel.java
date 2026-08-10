package com.pip.gui;

import javax.microedition.lcdui.Font;

import com.pip.common.Tool;
import com.pip.common.Utilities;
import com.pip.image.ImageSet;
import com.pip.sanguo.GameMain;
import com.pip.ui.VM;
import com.pip.ui.VMGame;

public class GLabel extends GWidget implements IGPaint, IGCycle{
	String label;
	public boolean is3d;
	public int color;
	public int bgColor;
	public int anchor;
	
	public int[] colors;
	ImageSet cornerRes;
	public int index;
	
	boolean needExecuteCycle;
	boolean needScroll;
	int strWidth;
	int speed = 3;
	boolean isMixStr;
	int scrollOffset;  //滚动的偏移量
	boolean isRightScroll;
	boolean singleDirScroll = true;
	
	//#if ScreenCanReset == true
	private boolean forceScroll;
	public int lineHeight;
	//#endif
	
	public GLabel(VMGame _vmGame, int self, int[] vmData, String name) {
		super(_vmGame, self, vmData, name);		
	}
	
	public GWidget getClone(VMGame _vmGame) {
		GLabel gLabel = new GLabel(_vmGame, 0, getVMDataCopy(), name);
		setCloneData(gLabel);
		gLabel.label = this.label;
		gLabel.is3d = this.is3d;
		gLabel.color = this.color;
		gLabel.bgColor = this.bgColor;
		gLabel.anchor = this.anchor;
		gLabel.cornerRes = this.cornerRes;
		gLabel.index = this.index;
		gLabel.needExecuteCycle = gLabel.needExecuteCycle;
		gLabel.needScroll = this.needScroll;
		gLabel.strWidth = this.strWidth;
		gLabel.speed = this.speed;
		gLabel.isMixStr = this.isMixStr;
		gLabel.scrollOffset = this.scrollOffset;
		gLabel.isRightScroll = this.isRightScroll;
		gLabel.singleDirScroll = this.singleDirScroll;
		//#if ScreenCanReset == true
		//# gLabel.forceScroll = this.forceScroll;
		//# gLabel.lineHeight = this.lineHeight;
		//#endif
		if(this.colors != null) {
			gLabel.colors = new int[this.colors.length];
			System.arraycopy(this.colors, 0, gLabel.colors, 0, gLabel.colors.length);
		}
		
		return gLabel;
	}
	
	public void setData(String label, boolean is3d, int color, int bgColor, int anchor) {
		this.label = label;
		this.is3d = is3d;
		this.color = color;
		this.bgColor = bgColor;
		this.anchor = anchor;
		
		resetSize();
	}
	
	public void setFont(Font font) {
		super.setFont(font);
		resetSize();
	}
	
	/* (non-Javadoc)
	 * @see com.pip.gui.GWidget#setBounds(com.pip.ui.VM, int, int, int, int)
	 */
	public void setBounds(int _x, int _y, int _w, int _h) {
		super.setBounds(_x, _y, _w, _h);
		setScroll();
	}
	
	private void setScroll() {
		//被固定尺寸时，需要滚动显示
		if(vmData[GW_VM_W] - vmData[GW_VM_BORDERLEFT] - vmData[GW_VM_BORDERRIGHT] < strWidth) {
			needScroll = true;
		} else {
			needScroll = false;
		}
		
		//#if ScreenCanReset == true
		//# scrollOffset = 0;
		//# isRightScroll = false;
		//# if(this.forceScroll) {
		//# 	needScroll = true;
		//# }
		//#endif		
		
	}
	
	//#if ScreenCanReset == true
	public void setForceScroll(boolean _forceScroll) {
		this.forceScroll = _forceScroll;
		if(_forceScroll) {
			needScroll = true;
		}
	}
	//#endif	
	
	/**
	 * 设置背景
	 * 
	 * @param colors
	 * @param cornerResName
	 * @param index
	 */
	public void setBack(int[] colors, String cornerResName, int index) {
		this.colors = colors;
		if(cornerResName != null) {
			this.cornerRes = (ImageSet) Tool.getGlobalObject(cornerResName);
			this.index = index;			
		}
	}
	
	public String getText() {
		return label;
	}
	
	public void setText(String text) {
		this.label = text;
		
		if(isMixStr) {
			setIsMixStr(isMixStr);
		} else {
			resetSize();
		}		
		
		setScroll();		

	}
	
	private void resetSize() {
		if(is3d) {
			this.vmData[GW_VM_MIN_HEIGHT] = Tool.get3DStringHeightEx(font);
			this.vmData[GW_VM_MIN_WIDTH] = Tool.get3DStringWidthEx(label, font);
		} else {
			this.vmData[GW_VM_MIN_HEIGHT] = font.getHeight();
			this.vmData[GW_VM_MIN_WIDTH] = font.stringWidth(label);
		}		
		
		//#if ScreenCanReset == true
		//# lineHeight = this.vmData[GW_VM_MIN_HEIGHT];
		//#endif
		
		strWidth = this.vmData[GW_VM_MIN_WIDTH];
	}
	
	/**
	 * 设置是否为混合字符串
	 * @param isMixStr
	 */
	public void setIsMixStr(boolean isMixStr) {
		this.isMixStr = isMixStr;
		
		if(label != null) {
			this.vmData[GW_VM_MIN_WIDTH] = Tool.drawMixedText(null, label, 0, 0, 0, 0, true, 0);
			strWidth = this.vmData[GW_VM_MIN_WIDTH];
		}		
	}
 	
	public void paint() {	
		int[] rect;
		if(parentNeedScroll()) {
			rect = getIntersect();		
			Utilities.graphics.setClip(rect[0], rect[1], rect[2], rect[3]);
		} else {
			rect = new int[]{0, 0, GameMain.viewWidth, GameMain.viewHeight};
		}
		
		//绘制背景
		if(colors != null) {
			Tool.drawFrameBox(
					Utilities.graphics, 
					vmData[GW_VM_XX], 
					vmData[GW_VM_YY], 
					vmData[GW_VM_W], 
					vmData[GW_VM_H], 
					colors);
		}		
		
		//绘制四角
		if(cornerRes != null) {
			Tool.drawBoxCorner(
					Utilities.graphics, 
					vmData[GW_VM_XX], 
					vmData[GW_VM_YY], 
					vmData[GW_VM_W], 
					vmData[GW_VM_H], 
					cornerRes,
					index);
		}
		
		if(label != null) {
			int _x = this.vmData[GW_VM_OFFSET_X];
			int _y = this.vmData[GW_VM_OFFSET_Y];
			int _w = vmData[GW_VM_W] - vmData[GW_VM_BORDERLEFT] - vmData[GW_VM_BORDERRIGHT];
			int _h = vmData[GW_VM_H] - vmData[GW_VM_BORDERTOP] - vmData[GW_VM_BORDERBOTTOM];
			int realAnchor = anchor;
			switch(anchor) {
				case Tool.G_TOPCENTER:
					_x += scrollOffset + vmData[GW_VM_XX] + vmData[GW_VM_BORDERLEFT] + _w / 2;
					_y += vmData[GW_VM_YY] + vmData[GW_VM_BORDERTOP];
					break;
				case Tool.G_CENTER:
					_x += scrollOffset + vmData[GW_VM_XX] + vmData[GW_VM_BORDERLEFT] + _w / 2;
					_y += vmData[GW_VM_YY] + vmData[GW_VM_BORDERTOP] + (_h - font.getHeight())/ 2;
					//kemulator不支持G_CENTER
					realAnchor = Tool.G_TOPCENTER;
					break;
				case Tool.G_LEFTCENTER:
					_x += scrollOffset + vmData[GW_VM_XX] + vmData[GW_VM_BORDERLEFT];
					_y += vmData[GW_VM_YY] + vmData[GW_VM_BORDERTOP];
					break;
				default:   //case Tool.G_TOPLEFT:
					_x += scrollOffset + vmData[GW_VM_XX] + vmData[GW_VM_BORDERLEFT];
					_y += vmData[GW_VM_YY] + vmData[GW_VM_BORDERTOP];
					break;
			}
			//脚本里设置最小高度，避免setclip时，切掉图片的上半部分
			rect = Tool.rectGetIntersection(rect[0], rect[1], rect[2], rect[3], this.vmData[GW_VM_OFFSET_X] + vmData[GW_VM_XX] + vmData[GW_VM_BORDERLEFT], this.vmData[GW_VM_OFFSET_Y] + vmData[GW_VM_YY] + vmData[GW_VM_BORDERTOP], _w, _h);
			Utilities.graphics.setClip(rect[0], rect[1], rect[2], rect[3]);
			
			Utilities.graphics.setFont(font);
			
			if(isMixStr) {
				Tool.drawMixedText(Utilities.graphics, label, _x, _y, color, bgColor, is3d, realAnchor, font);
			} else {
				if(is3d) {
					Tool.draw3DString2(Utilities.graphics, label, _x, _y, color, bgColor, realAnchor);
				} else {
					Utilities.graphics.setColor(color);
					Utilities.graphics.drawString(label, _x, _y, realAnchor);
				}
			}

			Utilities.graphics.setClip(0, 0, GameMain.viewWidth, GameMain.viewHeight);
			
			Utilities.graphics.setFont(Utilities.font);
		}		
	}

	//需要滚动时处理
	public void cycle() {
		if(needExecuteCycle && needScroll) {
			computeScroll();
		}
	}

	private void computeScroll() {
    	if(isRightScroll) {
        	if(scrollOffset >5) {
        		isRightScroll = false;
        	} else {
        		scrollOffset += speed;
        	}
    	} else {
        	if(! singleDirScroll && scrollOffset < vmData[GW_VM_W] - strWidth - vmData[GW_VM_BORDERLEFT] - vmData[GW_VM_BORDERRIGHT] - 5) {
       			isRightScroll = true;
        	} else {
        		scrollOffset -= speed;
        		if( scrollOffset + strWidth< 0 && singleDirScroll ){
        			scrollOffset = vmData[ GW_VM_W ] - vmData[GW_VM_BORDERLEFT] - vmData[GW_VM_BORDERRIGHT];
        		}
        	}
    	}
	}
	
	public boolean needExecuteCycle() {
		return needExecuteCycle && needScroll;
	}

	public void setNeedExecuteCycle(boolean needExecuteCycle) {
		this.needExecuteCycle = needExecuteCycle;
		if(! needExecuteCycle ){
			resetOffSet();
		}
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public void resetOffSet() {
		this.scrollOffset = 0; 
	}

	public boolean isSingleDirScroll() {
		return singleDirScroll;
	}

	public void setSingleDirScroll(boolean singleDirScroll) {
		this.singleDirScroll = singleDirScroll;
	}
	
	
	/**
	 * 设置Offset所在的比例,百分比
	 * @param scale
	 */
	public void setOffsetScale(int scale) {
		scrollOffset = (vmData[ GW_VM_W ] - vmData[GW_VM_BORDERLEFT] - vmData[GW_VM_BORDERRIGHT]) * scale / 100;
	}
	
	public void setScrollOffset(int scrollOffset) {
		this.scrollOffset = scrollOffset;
	}
	
	public int getScrollOffset() {
		return scrollOffset;
	}

}
