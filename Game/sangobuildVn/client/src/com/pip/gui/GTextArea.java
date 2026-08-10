package com.pip.gui;

import javax.microedition.lcdui.Font;

import com.pip.common.Tool;
import com.pip.common.Utilities;
import com.pip.image.ImageSet;
import com.pip.sanguo.GameMain;
import com.pip.ui.VM;
import com.pip.ui.VMGame;

public class GTextArea extends GContainer implements IGPaint{
	String text;    //原始文本
	String[] lines; //按宽度分割好的文本
	public int color;
	public boolean is3d;
	
	public int showLines; //显示的行数
    public int totalLines; //总行数
    
    public int lastPageLine; //最后一屏的起始行
    public int currentLine; //当前屏的起始行
    public int curPage;
    public int totalPage;
	
    //背景资源
    public int[] colors;
	ImageSet cornerRes;
	public int index;
	
	public int lineSpace;  //行间距
	//#if ScreenCanReset == true
	public int lineHeight; //行高
	//#endif
	
	public GTextArea(VMGame _vmGame, int self, int[] vmData, String name) {
		super(_vmGame, self, vmData, name);	
		
		this.vmData[GW_VM_MIN_HEIGHT] = font.getHeight();
		//#if ScreenCanReset == true
		//# lineHeight = this.vmData[GW_VM_MIN_HEIGHT];
		//#endif
	}

	public GWidget getClone(VMGame _vmGame) {
		GTextArea gTextArea = new GTextArea(_vmGame, 0, getVMDataCopy(), name);
		setCloneData(gTextArea);
		gTextArea.text = this.text;
		gTextArea.color = this.color;
		gTextArea.is3d = this.is3d;
		gTextArea.showLines = this.showLines;
		gTextArea.totalLines = this.totalLines;
		gTextArea.lastPageLine = this.lastPageLine;
		gTextArea.currentLine = this.currentLine;
		gTextArea.curPage = this.curPage;
		gTextArea.totalPage = this.totalPage;
		
		gTextArea.lineSpace = this.lineSpace;
		//#if ScreenCanReset == true
		//# gTextArea.lineHeight = this.lineHeight;
		//#endif
		if(this.lines != null) {
			gTextArea.lines = new String[this.lines.length];
			System.arraycopy(this.lines, 0, gTextArea.lines, 0, gTextArea.lines.length);
		}
		if(this.colors != null) {
			gTextArea.colors = new int[this.colors.length];
			System.arraycopy(this.colors, 0, gTextArea.colors, 0, gTextArea.colors.length);
		}
		
		return gTextArea;
	}
	
	/**
	 * 初始化数据
	 * @param text
	 * @param color
	 * @param is3d
	 */
	public void setData(String text, int color, boolean is3d) {
		this.text   = text;
		this.color  = color;
		this.is3d   = is3d;
	}
	
	public void setBounds(int _x, int _y, int _w, int _h) {
		super.setBounds(_x, _y, _w, _h);
				
		setLines();
	}
	
	public void setLineSpace(int lineSpace) {
		this.lineSpace = lineSpace;
	}
	
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
	
	/**
	 * 设置文本
	 * @param text
	 */
	public void setText(String text) {
		this.text = text;
		setLines();
	}	
	
	public void setFont(Font font) {
		super.setFont(font);
		setLines();
	}
	
	/**
	 * 重置数据
	 */
	private void setLines() {
	    //生成显示数据，计算总行数和显示行数
		currentLine = 0;
		curPage = 0;
		
		lines = Tool.formatText(text, vmData[GW_VM_W] - vmData[GW_VM_BORDERLEFT] - vmData[GW_VM_BORDERRIGHT], font);
	    totalLines = lines.length;
	    showLines = (vmData[GW_VM_H] - vmData[GW_VM_BORDERTOP] - vmData[GW_VM_BORDERBOTTOM] + lineSpace) / (font.getHeight() + lineSpace);
	    if(showLines > 0){
		    totalPage = (totalLines + showLines - 1)/showLines;
		    needScrollBar = true;
	    }else{
	    	totalPage = 1;
	    }
	    
	    if(showLines > totalLines){
	        showLines = totalLines;
	    }
	    
	    lastPageLine = totalLines - showLines;
	   
	    if(gsb != null) {
	    	gsb.reset();
			gsb.maxScrollDis = lastPageLine * font.getHeight();
	    }		
	}
	
	public String getText() {
		return text;
	}
		
	public void moveUp() {
	     currentLine++;
         
         if(currentLine > lastPageLine){
             currentLine = lastPageLine;             
         }
         if(gsb != null) {
        	 gsb.scrollPos = currentLine * font.getHeight();	 
         }
         
	}
	
	public void moveDown() {
	     currentLine--;
         
         if(currentLine < 0){
             currentLine = 0;             
         }
         if(gsb != null) {
        	 gsb.scrollPos = currentLine * font.getHeight();	 
         }         
	}
	
	public void moveUpPage() {
	    currentLine += showLines;
	    curPage ++;
		if(currentLine >= totalLines){
			currentLine = 0;
			curPage = 0;
	    }
		
		if(gsb != null) {
			gsb.scrollPos = currentLine * font.getHeight();	
		}		
	}
	
	public void paint() {
//		int[] rect = getIntersect();
//		Utilities.graphics.setClip(rect[0], rect[1], rect[2], rect[3]);
		
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
		
	    int _count = currentLine + showLines;
	    if(_count>=totalLines){
	    	_count = totalLines;
	    }
	    
	    Utilities.graphics.setColor(color);
		
		//脚本里设置最小高度，避免setclip时，切掉图片的上半部分
	    int _y = this.vmData[GW_VM_OFFSET_Y] + vmData[GW_VM_YY] + vmData[GW_VM_BORDERTOP] + 1;
	    
	    Utilities.graphics.setFont(font);
	    
	    for(int i = currentLine; i < _count; i++){
	        Tool.drawMixedText( 
	        		Utilities.graphics, 
	        		lines[i], 
	        		this.vmData[GW_VM_OFFSET_X] + vmData[GW_VM_XX] + vmData[GW_VM_BORDERLEFT],
	        		//#if ScreenCanReset == true
	        		//# _y + (i - currentLine) * (lineHeight + lineSpace), 
	        		//#else
	        		_y + (i - currentLine) * (font.getHeight() + lineSpace),
	        		//#endif
	        		color, 
	        		0x0,
	        		is3d, 
	        		Tool.G_TOPLEFT, 
	        		font);
	    }
	    
	    Utilities.graphics.setFont(Utilities.font);
//	    Utilities.graphics.setClip(0, 0, GameMain.viewWidth, GameMain.viewHeight);
	}
	
	//测试显示宽度
	public int testWidth(){
	    String[] tmp = Tool.formatText(text, vmData[GW_VM_MAX_WIDTH] - vmData[GW_VM_BORDERLEFT] - vmData[GW_VM_BORDERRIGHT], font);
	    return Tool.getStringsMaxWidth(tmp, true) + vmData[GW_VM_BORDERLEFT] + vmData[GW_VM_BORDERRIGHT];
	}

	//测试显示高度
	public int testHeight(int _width){
		String[] tmp = Tool.formatText(text, _width - vmData[GW_VM_BORDERLEFT] - vmData[GW_VM_BORDERRIGHT], font);
	    int _totalLines = tmp.length;
	    int _realHeight = (font.getHeight() + lineSpace) * _totalLines - lineSpace + vmData[GW_VM_BORDERTOP] + vmData[GW_VM_BORDERBOTTOM];
	    
	    if(_realHeight > vmData[GW_VM_MAX_HEIGHT]){
	        return vmData[GW_VM_MAX_HEIGHT];
	    }else{
	        return _realHeight;
	    }
	}
	
}
