package com.pip.gui;

import javax.microedition.lcdui.Font;

import com.pip.common.Tool;
import com.pip.common.Utilities;
import com.pip.sanguo.GameMain;
import com.pip.ui.VM;
import com.pip.ui.VMGame;

public class GWidget{	
	//脚本中对应的对象结构
	public final static int GW_VM_SYS_TYPE                    = 0;  //编译器产生的系统typeid
	public final static int GW_VM_TYPE                        = 1;
	public final static int GW_VM_VERAION                     = 2;
	public final static int GW_VM_X                           = 3;
	public final static int GW_VM_Y                           = 4;
	public final static int GW_VM_W                           = 5;
	public final static int GW_VM_H                           = 6;
	public final static int GW_VM_XX                          = 7;  //绝对x坐标
	public final static int GW_VM_YY                          = 8;  //绝对y坐标
	public final static int GW_VM_BORDERLEFT                  = 9;
	public final static int GW_VM_BORDERTOP                   = 10;
	public final static int GW_VM_BORDERRIGHT                 = 11;
	public final static int GW_VM_BORDERBOTTOM                = 12;
	
	public final static int GW_VM_MAX_WIDTH                   = 13;  //最大宽度
	public final static int GW_VM_MIN_WIDTH                   = 14;  //最小宽度
	public final static int GW_VM_MAX_HEIGHT                  = 15;  //最大高度
	public final static int GW_VM_MIN_HEIGHT                  = 16;  //最小高度

	public final static int GW_VM_Z_ORDER                     = 17;  //	
	
	public final static int GW_VM_FUNC_CYCLE                  = 18;
	public final static int GW_VM_FUNC_CYCLEUI                = 19;
	public final static int GW_VM_FUNC_PAINT                  = 20;
	public final static int GW_VM_FUNC_PACKET                 = 21;
	public final static int GW_VM_FUNC_DESTROY                = 22;
	public final static int GW_VM_FUNC_SEND_EVENT             = 23;
	public final static int GW_VM_FUNC_GET_PERFECT_WIDTH      = 24;  //最佳宽
	public final static int GW_VM_FUNC_GET_PERFECT_HEIGHT     = 25;  //最佳高
	public final static int GW_VM_FUNC_RESIZE                 = 26;
	
	public final static int GW_VM_CAN_MOUSE_CLICKED           = 27;  //是否能被鼠标点击
	public final static int GW_VM_CAN_MOUSE_DRAGGED           = 28;  //是否能被鼠标拖动
	
	public final static int GW_VM_JAVA_GWIDGET                = 29;
	public final static int GW_VM_ID                          = 30;
	public final static int GW_VM_OFFSET_X                    = 31;
	public final static int GW_VM_OFFSET_Y                    = 32;
	public final static int GW_VM_SELF                        = 33; //窗口本身的引用	
	public final static int GW_VM_FUNC_PAINT_BEFORE           = 34;
	public final static int GW_VM_FUNC_PAINT_AFTER            = 35;
	
	public int[] vmData; //存放脚本中的对象
	
	//事件定义
	public final static int GW_EVENT_GET_FOCUS = 0;
	public final static int GW_EVENT_LOST_FOCUS = 1;
	public final static int GW_EVENT_MOUSE_CLICKED = 2;
	
	public final static int PAINT_TYPE_IN_VM    = 0; //脚本中绘制
	public final static int PAINT_TYPE_IN_JAVA  = 1; //容器的绘制都在java中
	public final static int PAINT_TYPE_AFTER    = 2; //容器绘制完后绘制
	public final static int PAINT_TYPE_GIPAINT  = 3; //java中绘制
	
	public GContainer parent;
		
	public String name;
	
	public boolean isScale = true; //是否能自动缩放
	
	boolean noNeedLayout;
		
	boolean isFocus;
	boolean enableFocus = true;
	
	private boolean isOutView; //layout时跃出边界了
	int realHeight;  //真实的高度
	int outHeight;   //出边界的高度
	
	int[] grid3Data;
	//鼠标按下时的坐标
	int pressX;
	int pressY;
	
	public boolean isShow = true;
	
	private static final Tool keyMaker = new Tool();
	public VMGame vmGame;
	public int borderLayoutType;
	public Font font = Utilities.font;
	
	int[] getVMDataCopy() {
		int[] vmDataCopy = new int[vmData.length];
		System.arraycopy(this.vmData, 0, vmDataCopy, 0, vmDataCopy.length);
		return vmDataCopy;
	}
	
	void setCloneData(GWidget gWidget) {		
		gWidget.enableFocus = this.enableFocus;
		gWidget.parent = this.parent;
		gWidget.name = this.name;
		gWidget.noNeedLayout = this.noNeedLayout;
		gWidget.isOutView = this.isOutView;
		gWidget.isFocus = this.isFocus;
		gWidget.isScale = this.isScale;
		gWidget.pressX = this.pressX;
		gWidget.pressY = this.pressY;
		gWidget.isShow = this.isShow;
		gWidget.borderLayoutType = this.borderLayoutType;
		if(this.grid3Data != null) {
			gWidget.grid3Data = new int[this.grid3Data.length];
			System.arraycopy(this.grid3Data, 0, gWidget.grid3Data, 0, gWidget.grid3Data.length);
		}
		gWidget.realHeight = this.realHeight;
		gWidget.outHeight = this.outHeight;		
	}
	
	public GWidget getClone(VMGame _vmGame) {		
		GWidget gWidget = new GWidget(_vmGame, 0, getVMDataCopy(), "");		
		setCloneData(gWidget);
		return gWidget;
	}
	
	public static Object[] getCloneArray(VMGame _vmGame, GWidget gWidget, int count) {
		Object[] cloneArray = new Object[count];
		for(int i=0; i<count; i++) {
			cloneArray[i] = gWidget.getClone(_vmGame).vmData;
		}
		
		return cloneArray;
	}

	/**
	 * @param self
	 * @param vmData
	 * @param name
	 */
	public GWidget(VMGame _vmGame, int self, int[] vmData, String name) {
		this.vmData = vmData;
		this.name = name;
		this.vmGame = _vmGame;
		
		if(vmData[GW_VM_MAX_WIDTH] == 0) {
			vmData[GW_VM_MAX_WIDTH] = Integer.MAX_VALUE;	
		}
		if(vmData[GW_VM_MAX_HEIGHT] == 0) {
			vmData[GW_VM_MAX_HEIGHT] = Integer.MAX_VALUE;
		}		
		
		vmData[GW_VM_JAVA_GWIDGET] = keyMaker.nextKey();
		vmGame.putGWidget(this);
		
		vmData[GW_VM_TYPE] = vmData[GW_VM_SYS_TYPE]; 

		synchronized(vmGame.gtvm) {
			vmData[GW_VM_SELF] = vmGame.gtvm.getRealizeAdrr(vmGame.gtvm.makeTempObject(vmData));			
		}
	}
	
	public void setFont(Font font) {
		this.font = font;
	}
	
	public Font getFont() {
		return this.font;
	}
	
	public boolean getShow() {
		return isShow;
	}
	
	public void setShow(boolean isShow) {
		this.isShow = isShow;
		
		reCreateStack();
	}
	
	public void SetNeedLayout(boolean needLayout) {
		noNeedLayout = !needLayout;
//		if(!needLayout) {
//			isScale = false;	
//		}
		
	}
	
	public void setPressXY(int x, int y){
		this.pressX = x;
		this.pressY = y;
	}
	
	public int getPressX() {
		return this.pressX;
	}
	
	public int getPressY() {
		return this.pressY;
	}
 	
	public void move(int offsetX, int offsetY) {
		vmData[GW_VM_X] += offsetX;
		vmData[GW_VM_Y] += offsetY;
		
		//获得绝对坐标
		vmData[GW_VM_XX] += offsetX;
		vmData[GW_VM_YY] += offsetY;	
	}
	
	/**
	 * 获得自己的所属window
	 * @param gWidget
	 * @return
	 */
	public GWindow getParentWindow() {	
		if(this.parent instanceof GWindow) {
			return (GWindow)this.parent;
		} else {
			if(this.parent != null) {
				return this.parent.getParentWindow();
			} else {
				return null;
			}
			
		}
	}
	/**
	 * 设置grid3layout的数据
	 * @param gridX x轴grid 
	 * @param gridY y轴grid        
	 * @param gridHCount 横向占用格数
	 * @param gridVCount 纵向占用格数
	 * @param borderTop 
	 * @param borderBottom
	 * @param borderLeft
	 * @param borderRight
	 */
	public void setGrid3Data(int gridX, int gridY, int gridHCount, int gridVCount, int borderTop, int borderBottom, int borderLeft, int borderRight) {
		grid3Data = new int[8];
		grid3Data[0] = gridX;
		grid3Data[1] = gridY;
		grid3Data[2] = gridHCount;
		grid3Data[3] = gridVCount;
		grid3Data[4] = borderTop;
		grid3Data[5] = borderBottom;
		grid3Data[6] = borderLeft;
		grid3Data[7] = borderRight;		
	}
		
	public boolean isFocus() {
		return isFocus;
	}
	
	public void setScale(boolean isScale) {
		this.isScale = isScale;
	}
		
	public void setEnableFocus(boolean enableFocus) {
		this.enableFocus = enableFocus;
	}
	
	/**
	 * 
	 * @param gtvm
	 * @param _x
	 * @param _y
	 * @param _w
	 * @param _h
	 */
	public void setBounds(int _x, int _y, int _w, int _h) {
		this.setPos(_x, _y);
		//#if ScreenCanReset == false
		if(_x+_w>GameMain.viewWidth){
			_w = GameMain.viewWidth - _x;
		}
		//#else
		//# if(this instanceof GWindow && ((GWindow)this).fullScreen || (this.getParentWindow() != null && this.getParentWindow().fullScreen)) {
		//#	if(_x+_w>GameMain.viewWidth){
		//#		_w = GameMain.viewWidth - _x;
		//#	}
		//# } else if(_w > GWindow.uiMaxWidth){
		//#	_w = GWindow.uiMaxWidth;
		//# }
		//#endif
		vmData[GW_VM_W] = _w;
		vmData[GW_VM_H] = _h;
				
		//处理resize事件
		if(this.isScale && vmData[GW_VM_FUNC_RESIZE] != 0) {
			synchronized (vmGame.gtvm) {
				vmGame.gtvm.callback(vmData[GW_VM_FUNC_RESIZE], new int[] { 
						vmData[GW_VM_SELF], _x, _y, _w, _h
					});
			}
		}			
	}
		
	/**
	 * @param x
	 * @param y
	 */
	public void setPos(int x, int y) {
		vmData[GW_VM_X] = x;
		vmData[GW_VM_Y] = y;
		
		//获得绝对坐标
		vmData[GW_VM_XX] = getAbsX();
		vmData[GW_VM_YY] = getAbsY();
	}
	
	/**
	 * @param w
	 * @param h
	 */
	public void setSize(int w, int h) {
		vmData[GW_VM_W] = w;
		vmData[GW_VM_H] = h;
	}
	
	/**
	 * @param w
	 * @param h
	 */
	public void setMinSize(int w, int h) {
		vmData[GW_VM_MIN_WIDTH] = w;
		vmData[GW_VM_MIN_HEIGHT] = h;
	}
	
	/**
	 * @param borderTop
	 * @param borderLeft
	 * @param borderRight
	 * @param borderBottom
	 */
	public void setBorder(int borderTop, int borderBottom, int borderLeft, int borderRight) {
		vmData[GW_VM_BORDERTOP]     =  borderTop    ;
		vmData[GW_VM_BORDERLEFT]    =  borderLeft   ;
		vmData[GW_VM_BORDERRIGHT]   =  borderRight  ;
		vmData[GW_VM_BORDERBOTTOM]  =  borderBottom ;
	}

	/**
	 * @return  绝对x坐标
	 */
	public int getAbsX() {
		int _x = vmData[GW_VM_X];
		
		GWidget _parent = this.parent;
		while(_parent != null) {			
			_x += _parent.vmData[GW_VM_X];
			_parent = _parent.parent;
			
		}
		
		return _x;
	}

	/**
	 * @return  绝对y坐标
	 */
	public int getAbsY() {
		int _y = vmData[GW_VM_Y];
		GWidget _parent = this.parent;
		while(_parent != null) {			
			_y += _parent.vmData[GW_VM_Y];
			_parent = _parent.parent;
			
		}
		
		return _y;
	}

	public void setOutView(boolean isOutView) {
		this.isOutView = isOutView;		
		reCreateStack();
	}

	public void reCreateStack() {
		if(this instanceof GWindow) {
			((GWindow)this).reCreateStack = true;
		} else {
			GWindow gParentWindow = this.getParentWindow();
			if(gParentWindow != null) {
				gParentWindow.reCreateStack = true;
			}
		}
		
	}
	
	public boolean isOutView() {
		return isOutView;
	}
	
	public int getX(){
		return vmData[GW_VM_X];
	}
	
	public int getY(){
		return vmData[GW_VM_Y];
	}
	
	public int getW(){
		return vmData[GW_VM_W];
	}
	
	public int getMinW(){
		return vmData[GW_VM_MIN_WIDTH];
	}
	
	public int getMaxW(){
		return vmData[GW_VM_MAX_WIDTH];
	}
	
	public void freeVMObj() {
		synchronized(vmGame.gtvm) {
			if(vmGame.gtvm.followPointer(this.vmData[GW_VM_SELF]) != null) {
				vmGame.gtvm.free(this.vmData[GW_VM_SELF]);	
			}			
		}
	}
	
	//遍历判断父组件是否需要滚动
	public boolean parentNeedScroll() {
		GContainer _parent = parent;
		while(_parent != null) {
			if(_parent.needScrollBar) {
				return true;
			} else {
				_parent = _parent.parent;
			}
		}
		
		return false;
		
	}
	
	//获取组件在容器的可视范围内的相交矩形
	public int[] getIntersect() {
		int[] ret = new int[4];
		if(this.parent != null) {
			int[] parentIntersectRect = this.parent.getIntersect();
			ret = Tool.rectGetIntersection(
					vmData[GW_VM_XX] + vmData[GW_VM_OFFSET_X], vmData[GW_VM_YY] + vmData[GW_VM_OFFSET_Y], vmData[GW_VM_W], vmData[GW_VM_H],
					parentIntersectRect[0], parentIntersectRect[1], parentIntersectRect[2], parentIntersectRect[3]);
		} else {
			ret[0] = this.vmData[GW_VM_XX] + vmData[GW_VM_OFFSET_X];
			ret[1] = this.vmData[GW_VM_YY] + vmData[GW_VM_OFFSET_Y];
			ret[2] = this.vmData[GW_VM_W];
			ret[3] = this.vmData[GW_VM_H];
		}
		
		return ret;
	}
}
