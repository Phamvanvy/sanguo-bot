package com.pip.gui;

import java.util.Vector;

import com.pip.common.Tool;
import com.pip.common.Utilities;
import com.pip.sanguo.GameMain;
import com.pip.sanguo.GameView;
import com.pip.ui.VM;
import com.pip.ui.VMGame;

public class GContainer extends GWidget{
	public Vector children = new Vector();
	
	public boolean needScrollBar;   //需要滚动条
	GScrollBar gsb;
	public int firstInViewIndex = -1;  //第一个可显示的组件索引
	public int lastInViewIndex = -1;   //最后一个可显示的组件索引
	public boolean isJavaPaint;   //container内部的组件都是在java中paint的，则paint栈中只加入container的paint
		
	public boolean isIntersectView; //是否为矩形相交关系时才视为可见， 否则包含时才可见

	//layout类型
	public final static int GW_LAYOUT_TYPE_NONE        =  0x00;
	public final static int GW_LAYOUT_TYPE_H           =  0x01;
	public final static int GW_LAYOUT_TYPE_V           =  0x02;
	public final static int GW_LAYOUT_TYPE_GRID        =  0x04;
	public final static int GW_LAYOUT_TYPE_GRID2       =  0x08;
	public final static int GW_LAYOUT_TYPE_GRID3       =  0x10;
	public final static int GW_LAYOUT_TYPE_BORDER      =  0x11;

	//layout的对齐方式
	public final static int GW_LAYOUT_ALIGN_NONE       =  0x0000;
	public final static int GW_LAYOUT_ALIGN_FILL       =  0x0100;
	public final static int GW_LAYOUT_ALIGN_HCENTER    =  0x0200;
	public final static int GW_LAYOUT_ALIGN_VCENTER    =  0x0400;
	
	public final static int GW_BORDER_LAYOUT_NORTH     = 0;
	public final static int GW_BORDER_LAYOUT_SOUTH     = 1;
	public final static int GW_BORDER_LAYOUT_WEST      = 2;
	public final static int GW_BORDER_LAYOUT_EAST      = 3;
	public final static int GW_BORDER_LAYOUT_CENTER    = 4;
	
	//布局模式
	public int[] layoutData = new int[6];
	public static final int L_MODE = 0;
	
	public static final int L_HGAP = 1;
	public static final int L_VGAP = 2;
	public static final int L_ALIGN = 3;
	
	public static final int L_ROWS = 4;
	public static final int L_COLS = 5;
	public static final int L_GRID_W = 4;
	public static final int L_GRID_H = 5;
	
	//对BorderLayout有效
	public static final int L_UP_GAP = 1;
	public static final int L_DOWN_GAP = 2;
	public static final int L_LEFT_GAP = 3;
	public static final int L_RIGHT_GAP = 4;
	
	/**
	 * @param self
	 * @param vmData
	 * @param name
	 */
	public GContainer(VMGame _vmGame , int self, int[] vmData, String name) {
		super(_vmGame, self, vmData, name);
		isScale = true;
		//gLayout = new GLayout(this);
	}
	
	void setCloneData(VMGame _vmGame, GContainer gContainer) {
		super.setCloneData(gContainer);
		System.arraycopy(layoutData, 0, gContainer.layoutData, 0, layoutData.length);

		gContainer.needScrollBar = this.needScrollBar;
		if(this.gsb != null) {
			gContainer.gsb = (GScrollBar)this.gsb.getClone(_vmGame);
		}
		gContainer.firstInViewIndex = this.firstInViewIndex;
		gContainer.lastInViewIndex = this.lastInViewIndex;
		gContainer.isJavaPaint = this.isJavaPaint;
		gContainer.isIntersectView = this.isIntersectView;
		
		int count = this.children.size();
		for(int i=0; i<count; i++) {
			gContainer.add(((GWidget)children.elementAt(i)).getClone(_vmGame));
		}
	}
	
	public GWidget getClone(VMGame _vmGame) {
		GContainer gContainer = new GContainer(_vmGame, 0, getVMDataCopy(), null);
		setCloneData( gContainer);
	
		return gContainer;
	}
	
	public Object[] getChildren() {
		int count = this.children.size();
		Object[] _children = new Object[count];
		for(int i=0; i<count; i++) {
			_children[i] = ((GWidget)children.elementAt(i)).vmData;
		}
		
		return _children;
	}
	
	public Object[] getJavaChildren() {
		int count = this.children.size();
		Object[] _children = new Object[count];
		children.copyInto(_children);
		
		return _children;
	}
	
	public Object getChild(int index) {		
		return ((GWidget)children.elementAt(index)).vmData;
	}
	
	public Object getJavaChild(int index) {		
		return children.elementAt(index);
	}
	
	//批处理添加
	public void batchAdd(GWidget gWidget, int count) {
		for(int i=0; i<count; i++) {
			GWidget cloneWidget = gWidget.getClone(vmGame);
			this.add(cloneWidget);
			cloneWidget.vmData[GW_VM_ID] = i;
		}
	}
	
	/**
	 * 置顶
	 * @param index
	 */
	public void toTop(int index) {
		GWidget gWidget = (GWidget)children.elementAt(index);
		children.removeElementAt(index);
		add(gWidget);
	}
	
	/**
	 * 置底
	 * @param index
	 */
	public void toBottom(int index) {
		GWidget gWidget = (GWidget)children.elementAt(index);
		children.removeElementAt(index);
		insert(gWidget, 0);
	}
	
	/**
	 * 添加滚动条
	 * @param gsb
	 */
	public void addScrollBar(GScrollBar gsb) {
		this.gsb = gsb;
		gsb.parent = this;
	}
	
	public GScrollBar getScrollBar() {
		return this.gsb;
	}
	
	public void add(GWidget child, int borderLayoutType) {
		add(child);
		child.borderLayoutType = borderLayoutType;
	}
	
	/**
	 * @param child
	 */
	public void add(GWidget child) {
		children.addElement(child);
		initChild(child);
	}
	
	/**
	 * @param child
	 * @param index
	 */
	public void insert(GWidget child, int index) {
		children.insertElementAt(child, index);
		initChild(child);
	}
	
	/**
	 * 获得组件在容器中的索引
	 * @param gWidget
	 * @return
	 */
	public int getIndex(GWidget gWidget) {
		int _count = children.size();
		for(int i=0; i<_count; i++) {
			if(gWidget == children.elementAt(i)) {
				return i;
			}
		}
		
		return -1;
	}
	
	/**
	 * 初始化设置孩子
	 * @param child
	 */
	private void initChild(GWidget child){
		child.parent = this;
		child.vmData[GW_VM_Z_ORDER] = this.vmData[GW_VM_Z_ORDER] + 1;
		reCreateStack();
		
	}
	
	public void move(int offsetX, int offsetY) {
		super.move(offsetX, offsetY);
		this.setAbs();
	}
	
	/**
	 * 仅适用垂直滚动
	 * 
	 * @param gtvm
	 * @param oldFocusIndex
	 */
	public void setSrollBar(GWidget oldFocusWidget, GWidget newFocusWidget) {
		if(oldFocusWidget == newFocusWidget) {
			return;
		}
		
		boolean needScrollToo = false;
		if(this.isIntersectView) {
			if((newFocusWidget.vmData[GW_VM_Y] < 0 && newFocusWidget.vmData[GW_VM_Y] + newFocusWidget.vmData[GW_VM_H] > 0) ||
					(newFocusWidget.vmData[GW_VM_Y] < this.vmData[GW_VM_H] && newFocusWidget.vmData[GW_VM_Y] + newFocusWidget.vmData[GW_VM_H] > this.vmData[GW_VM_H])) {
				//新焦点控件卡在上下边界上了
				needScrollToo = true;
			}
		}
		if(newFocusWidget.isOutView() || needScrollToo) {
//			int offsetX = 0;
			int offsetY = 0;
			
			GWidget firstWidget = (GWidget)children.elementAt(firstInViewIndex);
			GWidget lastWidget = (GWidget)children.elementAt(lastInViewIndex);
			
			if(children.elementAt(0) == newFocusWidget) {
				//到第一个
				if(gsb != null) {
					offsetY = gsb.scrollPos;
				}
			} else if(newFocusWidget == children.elementAt(children.size() - 1)) {
				//从第一个切换到最后一个
				int h = lastWidget.vmData[GW_VM_Y] + lastWidget.vmData[GW_VM_H] - firstWidget.vmData[GW_VM_Y];
				offsetY = h - newFocusWidget.vmData[GW_VM_Y] - newFocusWidget.vmData[GW_VM_H] + ((GWidget)children.elementAt(firstInViewIndex)).vmData[GW_VM_Y];
			} else {
				if(newFocusWidget.vmData[GW_VM_Y] < oldFocusWidget.vmData[GW_VM_Y]) {
					//向下移动
					if(oldFocusWidget.isOutView()) {
						//拖动后，用按键切换焦点了
						if(newFocusWidget.vmData[GW_VM_Y] < oldFocusWidget.vmData[GW_VM_Y]) {
							offsetY = -newFocusWidget.vmData[GW_VM_Y] + firstWidget.vmData[GW_VM_Y] + firstWidget.vmData[GW_VM_H];	
						} else {
							offsetY = -oldFocusWidget.vmData[GW_VM_Y] + lastWidget.vmData[GW_VM_Y];
						}
											
					} else {
						if(needScrollToo) {
							offsetY = - newFocusWidget.vmData[GW_VM_Y];	
						} else {
							offsetY = - newFocusWidget.vmData[GW_VM_Y] + firstWidget.vmData[GW_VM_Y];
						}
					}								
				} else {
					//向上移动
					if(oldFocusWidget.isOutView()) {
						//拖动后，用按键切换焦点了
						if(newFocusWidget.vmData[GW_VM_Y] > oldFocusWidget.vmData[GW_VM_Y]) {
							offsetY = - oldFocusWidget.vmData[GW_VM_Y] + firstWidget.vmData[GW_VM_Y];
						} else {
							offsetY = - oldFocusWidget.vmData[GW_VM_Y] + lastWidget.vmData[GW_VM_Y] + lastWidget.vmData[GW_VM_H];
						}
							
					} else {
						if(needScrollToo) {
							offsetY = - newFocusWidget.vmData[GW_VM_Y] - newFocusWidget.vmData[GW_VM_H] + vmData[GW_VM_H];
						} else {
							int h = lastWidget.vmData[GW_VM_Y] + lastWidget.vmData[GW_VM_H];
							offsetY = - newFocusWidget.vmData[GW_VM_Y] - newFocusWidget.vmData[GW_VM_H] + h;
						}
					}					
				}				
			}
			
			setChildrenOffset(0, offsetY);
		}
	
	}
	
	public void moveUp() {
		if(lastInViewIndex > -1 && lastInViewIndex + 1 < children.size()) {
			GWidget _gWidget1 = (GWidget)children.elementAt(lastInViewIndex);
			GWidget _gWidget2 = (GWidget)children.elementAt(lastInViewIndex + 1);
			setChildrenOffset(0, _gWidget1.vmData[GW_VM_Y] - _gWidget2.vmData[GW_VM_Y]);
		}
	}
	
	public void moveDown() {
		if(firstInViewIndex > -1 && firstInViewIndex > 0) {
			GWidget _gWidget1 = (GWidget)children.elementAt(firstInViewIndex - 1);
			GWidget _gWidget2 = (GWidget)children.elementAt(firstInViewIndex);
			setChildrenOffset(0, _gWidget2.vmData[GW_VM_Y] - _gWidget1.vmData[GW_VM_Y]);
		}
	}
	
	public void moveUpPage() {
		
	}
	public void moveDownPage() {
		
	}
	
	//#if ScreenCanReset == true
	public void resetScrollBar() {
		GWindow parentWin = this.getParentWindow();
		if(parentWin == null && this instanceof GWindow) {
			parentWin = (GWindow)this;
		}
		if(parentWin.focusWidget == null){
			return;
		}
		
		int count = children.size();
		for(int i=0; i<count; i++) {
			GWidget gWidget = (GWidget)children.elementAt(i);
			if(gWidget instanceof GContainer) {
				GContainer gContainer = (GContainer)gWidget;
				if(gContainer.children.contains(parentWin.focusWidget)) {
					gContainer.setScrollbar(parentWin.focusWidget);
				} else if(gContainer.children.size() > 0){
					gContainer.setScrollbar((GWidget)gContainer.children.elementAt(0));	
				}
				gContainer.resetScrollBar();
			}
		}
	}
	
	//屏幕尺寸改变时调用
	public void setScrollbar(GWidget focusGWidget) {
		if(gsb != null && gsb.maxScrollDis > 0) {
			gsb.scrollPos = 0;
			setSrollBar((GWidget)this.children.elementAt(0), focusGWidget);
		}		
	}
	//#endif
	/**
	 * 设置容器内组件的位置
	 * @param offsetX
	 * @param offsetY
	 */
	public void setChildrenOffset(int offsetX, int offsetY) {
		firstInViewIndex = -1;
		lastInViewIndex = -1;
		
		//设置滚动位置
		if(gsb != null) {
			if(gsb.scrollPos - offsetY > gsb.maxScrollDis) {
				offsetY = gsb.scrollPos - gsb.maxScrollDis;
				gsb.scrollPos = gsb.maxScrollDis;
			}else {
				int oldScrollPos = gsb.scrollPos;
				gsb.scrollPos -= offsetY;	
				if(gsb.scrollPos < 0) {
					gsb.scrollPos = 0;
					offsetY = oldScrollPos;
				}
			}			
		}
		
		int count = children.size();
		for(int i=0; i<count; i++){
			GWidget gWidget = (GWidget)children.elementAt(i);
//			gWidget.vmData[GW_VM_X] += offsetX;
//			gWidget.vmData[GW_VM_Y] += offsetY;	
			gWidget.setPos(gWidget.vmData[GW_VM_X] + offsetX, gWidget.vmData[GW_VM_Y] + offsetY);
			
			boolean _isInView = false;
			if(isIntersectView) {
				_isInView = Tool.rectIntersect(0, 0, this.vmData[GW_VM_W], this.vmData[GW_VM_H], gWidget.vmData[GW_VM_X], gWidget.vmData[GW_VM_Y], gWidget.vmData[GW_VM_W], gWidget.vmData[GW_VM_H]);
			} else {
				_isInView = Tool.rectContain(0, 0, this.vmData[GW_VM_W], this.vmData[GW_VM_H], gWidget.vmData[GW_VM_X], gWidget.vmData[GW_VM_Y], gWidget.vmData[GW_VM_W], gWidget.vmData[GW_VM_H]);
			}
			if(_isInView) {
				gWidget.setOutView(false);
				if(firstInViewIndex == -1) {
					firstInViewIndex = i;									
				}
				if(i == count - 1 && lastInViewIndex == -1) {
					lastInViewIndex = i - 1;
				}
			} else {
				gWidget.setOutView(true);
				if(lastInViewIndex == -1 && firstInViewIndex != -1) {
					lastInViewIndex = i - 1;
				}
			}
		}
		
		this.setAbs();		
	}	
	
	/**
	 * @param child
	 */
	public void remove(GWidget child) {
		children.removeElement(child);		
		
		if(child instanceof GContainer) {
			((GContainer) child).destroy();
		} else {
			if(child.vmData[GW_VM_FUNC_DESTROY] > 0) {
				synchronized (vmGame.gtvm) {
					vmGame.gtvm.callback(child.vmData[GW_VM_FUNC_DESTROY], new int[] { 
							child.vmData[GW_VM_SELF]
						});
				}
			}
			
			GWindow parentWindow = child.getParentWindow();
			if(parentWindow!= null && parentWindow.focusWidget == child) {
				parentWindow.focusWidget = null;
			}
		}
		
		reCreateStack();
		
		child.freeVMObj();
		vmGame.removeGWidget(child);
	}
	
	public void clear() {
		destroyChild(getParentWindow());	
		
		if(gsb != null) {
			gsb.reset();
		}
		reCreateStack();
	}
				
	public void setBounds(int _x, int _y, int _w, int _h) {
		super.setBounds(_x, _y, _w, _h);		
		setAbs();
		
		//设置滚动条位置
		if(gsb != null) {
			switch(gsb.align) {
			case GScrollBar.GSB_ALIGN_CENTER:
				gsb.setBounds((vmData[GW_VM_W] - gsb.vmData[GW_VM_MIN_WIDTH])/2, 0, gsb.vmData[GW_VM_MIN_WIDTH], vmData[GW_VM_H]);
				break;
			case GScrollBar.GSB_ALIGN_RIGHT:
				gsb.setBounds(vmData[GW_VM_W] - gsb.vmData[GW_VM_MIN_WIDTH], 0, gsb.vmData[GW_VM_MIN_WIDTH], vmData[GW_VM_H]);
				break;
			}
		}
	}

	public void setPos(int x, int y) {
		super.setPos(x, y);
		setAbs();
		
		//设置滚动条位置
		if(gsb != null) {
			switch(gsb.align) {
			case GScrollBar.GSB_ALIGN_CENTER:
				gsb.setPos((vmData[GW_VM_W] - gsb.vmData[GW_VM_MIN_WIDTH])/2, 0);
				break;
			case GScrollBar.GSB_ALIGN_RIGHT:
				gsb.setPos(vmData[GW_VM_W] - gsb.vmData[GW_VM_MIN_WIDTH], 0);
				break;
			}
		}
	}
	/**
	 * 设置绝对坐标
	 */
	public void setAbs() {
		int _count = children.size();
	
		for(int i=0; i<_count; i++) {
			GWidget _gWidget = (GWidget)children.elementAt(i);
			_gWidget.vmData[GW_VM_XX] = _gWidget.getAbsX();
			_gWidget.vmData[GW_VM_YY] = _gWidget.getAbsY();		
			
			if(_gWidget instanceof GContainer) {
				GContainer gContainer = (GContainer)_gWidget;
				gContainer.setAbs();
				
				if(gContainer.gsb != null) {
					gContainer.gsb.vmData[GW_VM_XX] = gContainer.gsb.getAbsX();
					gContainer.gsb.vmData[GW_VM_YY] = gContainer.gsb.getAbsY();
				}
			}
		}	
	}
		
	/**
	 * 
	 * 处理子组件
	 * 
	 * @param gtvm
	 * @param type
	 * @param vmObjStack
	 * @param vmFuncStack
	 */
	public void setCallStack(int type, Vector vmObjStack, Vector vmFuncStack, Vector paintTypeStack) {
		int count = children.size();
        if(count > 0){        	
            for(int i = 0; i < count; i++){
            	GWidget gWidget = (GWidget)children.elementAt(i);

            	switch(type){
                case VM.CYCLE:
                	addCycleObj(gWidget, vmObjStack, vmFuncStack);
                	break;
                case VM.PAINT:
                	addPaintObj(gWidget, vmObjStack, vmFuncStack, paintTypeStack);
                	break;
                case VM.PROCESSPACKET:
                	addPacketObj(gWidget, vmObjStack, vmFuncStack);
                    break;
            	}
            }
        }
	}
	
	/**
	 * 添加paint对象
	 * 
	 * @param _gWidget
	 * @param vmObjStack
	 * @param vmFuncStack
	 */
	private void addPaintObj(GWidget _gWidget, Vector vmObjStack, Vector vmFuncStack, Vector paintTypeStack) {
		if(_gWidget.isShow && _gWidget.isOutView() == false) {	
			if(_gWidget.vmData[GWidget.GW_VM_FUNC_PAINT_BEFORE] > 0) {
				vmObjStack.addElement(_gWidget);
            	vmFuncStack.addElement(new Integer(_gWidget.vmData[GWidget.GW_VM_FUNC_PAINT_BEFORE]));
            	paintTypeStack.addElement(new Integer(PAINT_TYPE_IN_VM));
			}
			
			if(_gWidget instanceof IGPaint) {
				vmObjStack.addElement(_gWidget);
            	vmFuncStack.addElement(new Integer(0));
            	paintTypeStack.addElement(new Integer(PAINT_TYPE_GIPAINT));            	
			}
			
			if(_gWidget.vmData[GWidget.GW_VM_FUNC_PAINT] > 0) {
				vmObjStack.addElement(_gWidget);
            	vmFuncStack.addElement(new Integer(_gWidget.vmData[GWidget.GW_VM_FUNC_PAINT]));
            	paintTypeStack.addElement(new Integer(PAINT_TYPE_IN_VM));
			}
			
			if(_gWidget instanceof GContainer) {
				GContainer _gContiner = (GContainer)_gWidget;
				if(_gContiner.isJavaPaint) {
					vmObjStack.addElement(_gWidget);
	            	vmFuncStack.addElement(new Integer(0));
        			paintTypeStack.addElement(new Integer(PAINT_TYPE_IN_JAVA));					
				} else {
					_gContiner.paintChild( vmObjStack, vmFuncStack, paintTypeStack);
        		}
				
				
				//添加滚动条
				if(_gContiner.needScrollBar && _gContiner.gsb != null && _gContiner.gsb.vmData[GW_VM_FUNC_PAINT] > 0) {
					vmObjStack.addElement(_gContiner.gsb);
	            	vmFuncStack.addElement(new Integer(_gContiner.gsb.vmData[GWidget.GW_VM_FUNC_PAINT]));
	            	paintTypeStack.addElement(new Integer(PAINT_TYPE_IN_VM));
				}
			}
			
			if(_gWidget.vmData[GWidget.GW_VM_FUNC_PAINT_AFTER] > 0) {
				vmObjStack.addElement(_gWidget);
            	vmFuncStack.addElement(new Integer(_gWidget.vmData[GWidget.GW_VM_FUNC_PAINT_AFTER]));
            	paintTypeStack.addElement(new Integer(PAINT_TYPE_AFTER));
			}
		}
	}
	
	/**
	 * 画每个组件
	 * 
	 * @param gtvm
	 * @param vmObjStack
	 * @param vmFuncStack
	 */
	private void paintChild(Vector vmObjStack, Vector vmFuncStack, Vector paintTypeStack) {	
		int _count = children.size();
		
		for(int i=0; i<_count; i++) {
			GWidget _gWidget = (GWidget)children.elementAt(i);
			
			addPaintObj(_gWidget, vmObjStack, vmFuncStack, paintTypeStack);
		}
		
	}
	
	/**
	 * 添加cycle对象
	 * 
	 * @param _gWidget
	 * @param vmObjStack
	 * @param vmFuncStack
	 */
	private void addCycleObj(GWidget _gWidget, Vector vmObjStack, Vector vmFuncStack) {
		if(_gWidget.isShow && _gWidget.isOutView() == false) {
			if(_gWidget instanceof IGCycle) {
				if(((IGCycle)_gWidget).needExecuteCycle()) {
					//直接在java中运行cycle了
					((IGCycle)_gWidget).cycle();
				}
			}
			
			if(_gWidget.vmData[GWidget.GW_VM_FUNC_CYCLE] > 0) {				
				//在脚本中也可以运行cycle
				vmObjStack.addElement(_gWidget);
            	vmFuncStack.addElement(new Integer(_gWidget.vmData[GWidget.GW_VM_FUNC_CYCLE]));
        		
			}		
			
			if(_gWidget instanceof GContainer) {
				GContainer _gContiner = (GContainer)_gWidget;
				_gContiner.cycleChild(vmObjStack, vmFuncStack);
				
				//添加滚动条
				if(_gContiner.needScrollBar && _gContiner.gsb != null) {
					_gContiner.gsb.cycle();
				}
			}
		}
	}
	
	/**
	 * @param gtvm
	 * @param vmObjStack
	 * @param vmFuncStack
	 */
	private void cycleChild(Vector vmObjStack, Vector vmFuncStack) {		
		int _count = children.size();

		for(int i=0; i<_count; i++) {
			GWidget _gWidget = (GWidget)children.elementAt(i);
			addCycleObj(_gWidget, vmObjStack, vmFuncStack);

		}	
	}
		
	/**
	 * @param _gWidget
	 * @param vmObjStack
	 * @param vmFuncStack
	 */
	private void addPacketObj(GWidget _gWidget, Vector vmObjStack, Vector vmFuncStack) {	
		if(_gWidget.vmData[GWidget.GW_VM_FUNC_PACKET] > 0) {
			vmObjStack.addElement(_gWidget);
        	vmFuncStack.addElement(new Integer(_gWidget.vmData[GWidget.GW_VM_FUNC_PACKET]));
		}
		
		if(_gWidget instanceof GContainer) {
			((GContainer)_gWidget).handlePacket(vmObjStack, vmFuncStack);
		}
	}
		
	/**
	 * @param gtvm
	 * @param vmObjStack
	 * @param vmFuncStack
	 */
	private void handlePacket(Vector vmObjStack, Vector vmFuncStack) {	
		int _count = children.size();

		for(int i=0; i<_count; i++) {
			GWidget _gWidget = (GWidget)children.elementAt(i);			
			addPacketObj(_gWidget, vmObjStack, vmFuncStack);
		}	
	}
	
	public void destroy() {
		GWindow parentWindow = this.getParentWindow();
		destroyChild(parentWindow);
		
		if(vmData[GW_VM_FUNC_DESTROY] > 0) {
			synchronized (vmGame.gtvm) {
				vmGame.gtvm.callback(vmData[GW_VM_FUNC_DESTROY], new int[] { 
						vmData[GW_VM_SELF]
					});
			}
		}
		
		if(parentWindow!= null && parentWindow.focusWidget == this) {
			parentWindow.focusWidget = null;
		}
		this.freeVMObj();
		vmGame.removeGWidget(this);
	}
	
	private void destroyChild(GWindow parentWindow) {
		int _count = children.size();
		
		for(int i=0; i<_count; i++) {
			GWidget _gWidget = (GWidget)children.elementAt(i);	
			
			//先destroy孩子
			if(_gWidget instanceof GContainer) {
				GContainer gContainer = (GContainer)_gWidget;
				gContainer.destroyChild(parentWindow);
				
				//销毁滚动条
				if(gContainer.gsb != null) {
					if(gContainer.gsb.vmData[GW_VM_FUNC_DESTROY] > 0) {
						synchronized (vmGame.gtvm) {
							vmGame.gtvm.callback(gContainer.gsb.vmData[GW_VM_FUNC_DESTROY], new int[] { 
									gContainer.gsb.vmData[GW_VM_SELF]
								});
						}
					}
					
					if(parentWindow!= null && parentWindow.focusWidget == gsb) {
						parentWindow.focusWidget = null;
					}
					
					gContainer.gsb.freeVMObj();
					vmGame.removeGWidget(gContainer.gsb);
				}
			}
			
			if(_gWidget.vmData[GW_VM_FUNC_DESTROY] > 0) {
				synchronized (vmGame.gtvm) {
					vmGame.gtvm.callback(_gWidget.vmData[GW_VM_FUNC_DESTROY], new int[] { 
							_gWidget.vmData[GW_VM_SELF]
						});
				}
			}
			
			if(_gWidget instanceof GGameIcon) {
				if(((GGameIcon)_gWidget).gi != null) {
					((GGameIcon)_gWidget).gi.vm_free_icon();
				}
			}
			
			if(parentWindow!= null && parentWindow.focusWidget == _gWidget) {
				parentWindow.focusWidget = null;
			}
			
			_gWidget.freeVMObj();
			vmGame.removeGWidget(_gWidget);
		}
		
		children.removeAllElements();		
	}
		
	public void setIsJavaPaint(boolean isJavaPaint) {
		this.isJavaPaint = isJavaPaint;
	}
	
	//直接画container
	public void paintContainer() {
		if(this instanceof IGPaint) {
			((IGPaint)this).paint();
		}
		
		paintChildren();
	}
	
	//画container内的组件
	private void paintChildren() {
		int count = children.size();
		for(int i=0; i<count; i++) {
			GWidget _gWidget = (GWidget)children.elementAt(i);
			
			if(_gWidget.isOutView() == false) {
				//先画自己
				if(_gWidget instanceof IGPaint) {
					((IGPaint)_gWidget).paint();
				}
				
				//再画孩子
				if(_gWidget instanceof GContainer) {
					GContainer gContainer = (GContainer)_gWidget;
					gContainer.paintChildren();
					
				}
			}			
		}
	}
	
	public GWidget serchWdiget(int x, int y) {
		GWidget sWidget = null;
		int count = children.size();
		for(int i=0; i<count; i++) {
			GWidget gWidget = (GWidget)children.elementAt(i);
			
//#if ModelID == Android  || ModelID == Lenovo || ModelID == AndroidLarge || ModelID == IPhone4 || ModelID == IPad || ModelID == LenovoU1 						
			//# String _vmId = VMGame.getTopUIVMId();
			//# if(_vmId.equals("ui_chat_check_msg")){
			//# 	if(gWidget.isShow == false) {
			//# 		continue;
			//# 	}
			//# }
//#elif ModelID == AndroidAuto
        	//# if (!GameMain.getUIModel().equals(GameMain.ANDROID_SMALL))
        	//# {
			//# String _vmId = VMGame.getTopUIVMId();
			//# if(_vmId.equals("ui_chat_check_msg")){
			//# 	if(gWidget.isShow == false) {
			//# 		continue;
			//# 	}
			//#  }
			//# }
//#endif
			
			if(gWidget instanceof GContainer && gWidget.isShow) {
				GContainer gContainer = ((GContainer)gWidget);
				sWidget = gContainer.serchWdiget(x, y);
				if(sWidget == null && gContainer.needScrollBar && gContainer.gsb != null) {
					if((gContainer.gsb.vmData[GW_VM_CAN_MOUSE_CLICKED] == 1 || gContainer.gsb.vmData[GW_VM_CAN_MOUSE_DRAGGED] == 1) && Tool.rectIn(gContainer.gsb.vmData[GW_VM_XX], gContainer.gsb.vmData[GW_VM_YY], gContainer.gsb.vmData[GW_VM_W], gContainer.gsb.vmData[GW_VM_H], x, y)) {
						return gContainer.gsb;
					}
				}
			}
			
			if(sWidget == null) {
				if((gWidget.vmData[GW_VM_CAN_MOUSE_CLICKED] == 1  || gWidget.vmData[GW_VM_CAN_MOUSE_DRAGGED] == 1) && gWidget.isOutView() == false && Tool.rectIn(gWidget.vmData[GW_VM_XX], gWidget.vmData[GW_VM_YY], gWidget.vmData[GW_VM_W], gWidget.vmData[GW_VM_H], x, y)) {
					return gWidget;
				}
			} else {
				return sWidget;
			}
		}
		
		return null;
	}
	
	public GWidget serchWdiget2(int x, int y) {
		GWidget sWidget = null;
		int count = children.size();
		for(int i=0; i<count; i++) {
			GWidget gWidget = (GWidget)children.elementAt(i);
			
//#if ModelID == Android  || ModelID == Lenovo || ModelID == AndroidLarge || ModelID == IPhone4 || ModelID == IPad || ModelID == LenovoU1 			
			//# String _vmId = VMGame.getTopUIVMId();
			//# if(_vmId.equals("ui_chat_check_msg")){
			//# 	if(gWidget.isShow == false) {
			//# 		continue;
			//# 	}
			//# }
//#elif ModelID == AndroidAuto
			//# if (!GameMain.getUIModel().equals(GameMain.ANDROID_SMALL))
        	//# {
			//# String _vmId = VMGame.getTopUIVMId();
			//# if(_vmId.equals("ui_chat_check_msg")){
			//# 	if(gWidget.isShow == false) {
			//# 		continue;
			//# 	}
			//#  }
			//# }
//#endif
			
			if(gWidget instanceof GContainer && gWidget.isShow) {
				GContainer gContainer = ((GContainer)gWidget);
				sWidget = gContainer.serchWdiget2(x, y);
				if(sWidget == null && gContainer.needScrollBar && gContainer.gsb != null) {
					if(Tool.rectIn(gContainer.gsb.vmData[GW_VM_XX], gContainer.gsb.vmData[GW_VM_YY], gContainer.gsb.vmData[GW_VM_W], gContainer.gsb.vmData[GW_VM_H], x, y)) {
						return gContainer.gsb;						
					}
				}
			}
			
			if(sWidget == null) {
				if(gWidget.isOutView() == false && Tool.rectIn(gWidget.vmData[GW_VM_XX], gWidget.vmData[GW_VM_YY], gWidget.vmData[GW_VM_W], gWidget.vmData[GW_VM_H], x, y)) {
					if(gWidget instanceof GContainer) {
						sWidget = ((GContainer)gWidget).serchWdiget2(x, y);
						if(sWidget == null && ((GContainer)gWidget).needScrollBar) {
							return gWidget;	
						}
					} else {
						return gWidget;
					}
				}
			} else {
				return sWidget;
			}
		}
		
		return null;
	}
	
	/***** Layout ***************************************************************/
	/**
	 * @param layoutMode
	 * @param hgap
	 * @param vgap
	 * @param align
	 * @param rows
	 * @param cols
	 */
	public void setLayoutMode(int layoutMode, int data1, int data2, int data3, int data4, int data5) {
		this.layoutData[L_MODE] = layoutMode;
		this.layoutData[1] = data1;
		this.layoutData[2] = data2;
		this.layoutData[3] = data3;
		this.layoutData[4] = data4;
		this.layoutData[5] = data5;
	}
	
	public void setHLayout(int hgap, int  align) {
		this.layoutData[L_MODE] = GW_LAYOUT_TYPE_H;
		this.layoutData[L_HGAP] = hgap;
		this.layoutData[L_ALIGN] =  align;
	}
	
	public void setVLayout(int vgap, int  align) {
		this.layoutData[L_MODE] = GW_LAYOUT_TYPE_V;
		this.layoutData[L_VGAP] = vgap;
		this.layoutData[L_ALIGN] = align;
	}
	
	public void setGridLayout(int rows, int cols) {
		this.layoutData[L_MODE] = GW_LAYOUT_TYPE_GRID;
		this.layoutData[L_ROWS] = rows;
		this.layoutData[L_COLS] = cols;
	}
	
	public void setGrid2Layout(int hgap, int vgap, int gridW, int gridH) {
		this.layoutData[L_MODE] = GW_LAYOUT_TYPE_GRID2;
		this.layoutData[L_HGAP] = hgap;
		this.layoutData[L_VGAP] = vgap;
		this.layoutData[L_GRID_W] = gridW;
		this.layoutData[L_GRID_H] = gridH;
		this.layoutData[L_ALIGN] = GW_LAYOUT_ALIGN_FILL;
	}
	
	public void setGrid3Layout(int rows, int cols) {
		this.layoutData[L_MODE] = GW_LAYOUT_TYPE_GRID3;
		this.layoutData[L_ROWS] = rows;
		this.layoutData[L_COLS] = cols;
	}
	
	public void setBorderLayout(int upGap, int downGap, int leftGap, int rightGap) {
		this.layoutData[L_MODE]       = GW_LAYOUT_TYPE_BORDER;
		this.layoutData[L_UP_GAP]     = upGap;
		this.layoutData[L_DOWN_GAP]   = downGap;
		this.layoutData[L_LEFT_GAP]   = leftGap;
		this.layoutData[L_RIGHT_GAP]  = rightGap;		
	}
	
	/**
	 * 获得指定位置的GWidget
	 * @param borderLayoutData
	 * @return
	 */
	private GWidget getBorderLayoutGWidget(int borderLayoutType) {
		int count = children.size();
		for(int i=0; i<count; i++) {
			GWidget gWidget = (GWidget)children.elementAt(i);
			if(gWidget.borderLayoutType== borderLayoutType) {
				return gWidget;
			}
		}
		return null;
	}
	
	public void layout() {
		layout2();
		align();
	}
	
	/**
	 * 对齐
	 * 
	 * @param gtvm
	 */
	public void  align() {
		int _count = children.size();
		//#if ModelID == Android || ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad
		//#  int nScrollBarWidth = 0;
		//#  int nScreenHeight = GameMain.viewHeight;
		//#  if(needScrollBar){
		//#     //#if ModelID == AndroidLarge
		//#       nScrollBarWidth = 54;
		//#     //#elif ModelID == Android
		//#       nScrollBarWidth = 27;
		//#     //#elif ModelID == AndroidSmall
		//#      nScrollBarWidth = 0;
		//#     //#else
		//#  	if(nScreenHeight == 480){
		//#  		nScrollBarWidth = 54;
		//#  	}
		//#  	else if(nScreenHeight == 320){
		//#  		nScrollBarWidth = 27;
		//#  	}
		//#  	else{
		//#  		nScrollBarWidth = 0;
		//#  	}
		//#    //#endif 
		//#  }
		//#elif ModelID == AndroidAuto
		//# int nScrollBarWidth = 0;
		//# int nScreenHeight = GameMain.viewHeight;
		//# if (!GameMain.getUIModel().equals(GameMain.ANDROID_SMALL))
    	//# {
		//#  nScrollBarWidth = 0;
		//#  nScreenHeight = GameMain.viewHeight;
		//#  if(needScrollBar){
		//#  	if(GameMain.getUIModel().equals(GameMain.ANDROID_LARGE)){
		//#  		nScrollBarWidth = 54;
		//#  	}
		//#  	else if(GameMain.getUIModel().equals(GameMain.ANDROID_NORMAL)){
		//#  		nScrollBarWidth = 27;
		//#  	}
		//#  	else{
		//#  		nScrollBarWidth = 0;
		//#  	}
		//#  }
		//# }
		//#endif
		switch(layoutData[L_ALIGN]) {
			case GW_LAYOUT_ALIGN_NONE:
				for(int i=0; i<_count; i++) {
					GWidget _gWidget = (GWidget)children.elementAt(i);
					//不需要做layout
					if(_gWidget.noNeedLayout) {
						continue;
					}
					if(_gWidget instanceof GContainer) {
						((GContainer) _gWidget).align();
					}
				}
				break;
			case GW_LAYOUT_ALIGN_VCENTER:
				for(int i=0; i<_count; i++) {
					GWidget _gWidget = (GWidget)children.elementAt(i);
					//不需要做layout
					if(_gWidget.noNeedLayout) {
						continue;
					}
					_gWidget.setPos(_gWidget.vmData[GWidget.GW_VM_X], _gWidget.vmData[GWidget.GW_VM_Y] + (vmData[GWidget.GW_VM_H] - vmData[GWidget.GW_VM_BORDERTOP] - vmData[GWidget.GW_VM_BORDERBOTTOM] - _gWidget.vmData[GWidget.GW_VM_H]) / 2);
					
					if(_gWidget instanceof GContainer) {
						((GContainer) _gWidget).align();
					}
				}
				break;
			case GW_LAYOUT_ALIGN_HCENTER:
				for(int i=0; i<_count; i++) {
					GWidget _gWidget = (GWidget)children.elementAt(i);
					//不需要做layout
					if(_gWidget.noNeedLayout) {
						continue;
					}
					//#if ModelID == Android || ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad
					//#  _gWidget.setBounds(
					//#  		_gWidget.vmData[GWidget.GW_VM_X] + (vmData[GWidget.GW_VM_W] - vmData[GWidget.GW_VM_BORDERLEFT] - vmData[GWidget.GW_VM_BORDERRIGHT] - _gWidget.vmData[GWidget.GW_VM_W]) / 2,
					//#  		_gWidget.vmData[GWidget.GW_VM_Y],
					//#  		_gWidget.vmData[GWidget.GW_VM_W] - nScrollBarWidth,
					//#  		_gWidget.vmData[GWidget.GW_VM_H]);
					//#elif ModelID == AndroidAuto
					//# if (!GameMain.getUIModel().equals(GameMain.ANDROID_SMALL))
			    	//# {
					//#  _gWidget.setBounds(
					//#  		_gWidget.vmData[GWidget.GW_VM_X] + (vmData[GWidget.GW_VM_W] - vmData[GWidget.GW_VM_BORDERLEFT] - vmData[GWidget.GW_VM_BORDERRIGHT] - _gWidget.vmData[GWidget.GW_VM_W]) / 2,
					//#  		_gWidget.vmData[GWidget.GW_VM_Y],
					//#  		_gWidget.vmData[GWidget.GW_VM_W] - nScrollBarWidth,
					//#  		_gWidget.vmData[GWidget.GW_VM_H]);
					//# }
					//# else
					//# {
					//#	_gWidget.setPos(_gWidget.vmData[GWidget.GW_VM_X] + (vmData[GWidget.GW_VM_W] - vmData[GWidget.GW_VM_BORDERLEFT] - vmData[GWidget.GW_VM_BORDERRIGHT] - _gWidget.vmData[GWidget.GW_VM_W]) / 2, _gWidget.vmData[GWidget.GW_VM_Y] );
					//# }
					//#else
					_gWidget.setPos(_gWidget.vmData[GWidget.GW_VM_X] + (vmData[GWidget.GW_VM_W] - vmData[GWidget.GW_VM_BORDERLEFT] - vmData[GWidget.GW_VM_BORDERRIGHT] - _gWidget.vmData[GWidget.GW_VM_W]) / 2, _gWidget.vmData[GWidget.GW_VM_Y] );
					//#endif
					if(_gWidget instanceof GContainer) {
						((GContainer) _gWidget).align();
					}
				}
				break;
			case GW_LAYOUT_ALIGN_FILL:
				int perWidth = 0;				
				if(layoutData[L_MODE] == GW_LAYOUT_TYPE_H) {
					perWidth = ((vmData[GWidget.GW_VM_W] - vmData[GWidget.GW_VM_BORDERLEFT] - vmData[GWidget.GW_VM_BORDERRIGHT]) - (_count - 1) * layoutData[L_HGAP]) / _count;
					
				}
				for(int i=0; i<_count; i++) {
					GWidget _gWidget = (GWidget)children.elementAt(i);
					//不需要做layout
					if(_gWidget.noNeedLayout) {
						continue;
					}
					if(layoutData[L_MODE] == GW_LAYOUT_TYPE_V ) {
						//#if ModelID == Android || ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad
						//#  _gWidget.setBounds(
						//#  		_gWidget.vmData[GWidget.GW_VM_X],
						//#  		_gWidget.vmData[GWidget.GW_VM_Y],
						//#  		vmData[GWidget.GW_VM_W] - vmData[GWidget.GW_VM_BORDERLEFT] - vmData[GWidget.GW_VM_BORDERRIGHT] - nScrollBarWidth,
						//#  		_gWidget.vmData[GWidget.GW_VM_H]);
						//#elif ModelID == AndroidAuto
						//# if (!GameMain.getUIModel().equals(GameMain.ANDROID_SMALL))
				    	//# {
						//#  _gWidget.setBounds(
						//#  		_gWidget.vmData[GWidget.GW_VM_X],
						//#  		_gWidget.vmData[GWidget.GW_VM_Y],
						//#  		vmData[GWidget.GW_VM_W] - vmData[GWidget.GW_VM_BORDERLEFT] - vmData[GWidget.GW_VM_BORDERRIGHT] - nScrollBarWidth,
						//#  		_gWidget.vmData[GWidget.GW_VM_H]);
						//# }
						//# else
						//# {
						//#	_gWidget.setBounds(
						//#			_gWidget.vmData[GWidget.GW_VM_X],
						//#			_gWidget.vmData[GWidget.GW_VM_Y],
						//#			vmData[GWidget.GW_VM_W] - vmData[GWidget.GW_VM_BORDERLEFT] - vmData[GWidget.GW_VM_BORDERRIGHT],
						//#			_gWidget.vmData[GWidget.GW_VM_H]);	
						//# }
						//#else
						_gWidget.setBounds(
								_gWidget.vmData[GWidget.GW_VM_X],
								_gWidget.vmData[GWidget.GW_VM_Y],
								vmData[GWidget.GW_VM_W] - vmData[GWidget.GW_VM_BORDERLEFT] - vmData[GWidget.GW_VM_BORDERRIGHT],
								_gWidget.vmData[GWidget.GW_VM_H]);
						//#endif
					} else if(layoutData[L_MODE] == GW_LAYOUT_TYPE_H) {
						int perX = vmData[GWidget.GW_VM_BORDERLEFT] + (perWidth + layoutData[L_HGAP]) * i + (perWidth - _gWidget.vmData[GWidget.GW_VM_W]) / 2;
						int realWidth = _gWidget.vmData[GWidget.GW_VM_W];
						if(isScale) {
							perX = vmData[GWidget.GW_VM_BORDERLEFT] + (perWidth + layoutData[L_HGAP]) * i;
							realWidth = perWidth;
						}
						
						_gWidget.setBounds(
								perX,
								_gWidget.vmData[GWidget.GW_VM_Y],
								realWidth,
								_gWidget.vmData[GWidget.GW_VM_H]);
					}					
					
					if(_gWidget instanceof GContainer) {
						((GContainer) _gWidget).align();
					}
				}
				break;
		}
		
		//设置滚动条位置
		if(gsb != null) {
			switch(gsb.align) {
			case GScrollBar.GSB_ALIGN_CENTER:
				gsb.setBounds((vmData[GWidget.GW_VM_W] - gsb.vmData[GWidget.GW_VM_MIN_WIDTH])/2, 0, gsb.vmData[GWidget.GW_VM_MIN_WIDTH], vmData[GWidget.GW_VM_H]);
				break;
			case GScrollBar.GSB_ALIGN_RIGHT:
				gsb.setBounds(vmData[GWidget.GW_VM_W] - gsb.vmData[GWidget.GW_VM_MIN_WIDTH], 0, gsb.vmData[GWidget.GW_VM_MIN_WIDTH], vmData[GWidget.GW_VM_H]);
				break;
			}
		}
	}
	
	/**
	 * @param gtvm
	 */
	private void layout2() {
		switch(layoutData[L_MODE]) {
			case GW_LAYOUT_TYPE_NONE:
				int _count = children.size();
				for(int i=0; i<_count; i++) {
					GWidget _gWidget = (GWidget)children.elementAt(i);
					//不需要做layout
					if(_gWidget.noNeedLayout) {
						continue;
					}
					if(_gWidget instanceof GContainer) {
						((GContainer) _gWidget).layout2();
					}
				}
				break;
			case GW_LAYOUT_TYPE_H:
				layoutH();
				break;
			case GW_LAYOUT_TYPE_V:
				layoutV();
				break;		
			case GW_LAYOUT_TYPE_GRID:
				layoutG();
				break;
			case GW_LAYOUT_TYPE_GRID2:
				layoutG2();
				break;
			case GW_LAYOUT_TYPE_BORDER:
				borderLayout();
				break;
		}
	}

	private int getPerfectWidth(GWidget _gWidget, int _layoutW) {
		int _perfectWidth = 0;
		//获得最佳宽
		if(_gWidget.vmData[GWidget.GW_VM_FUNC_GET_PERFECT_WIDTH] != 0) {
			synchronized (vmGame.gtvm) {
				_perfectWidth = vmGame.gtvm.callback(_gWidget.vmData[GWidget.GW_VM_FUNC_GET_PERFECT_WIDTH], new int[] { 
						_gWidget.vmData[GW_VM_SELF], _gWidget.vmData[GWidget.GW_VM_MAX_WIDTH]
					});
			}
		} else {
			_perfectWidth = _gWidget.vmData[GWidget.GW_VM_MIN_WIDTH] + _gWidget.vmData[GWidget.GW_VM_BORDERLEFT] + _gWidget.vmData[GWidget.GW_VM_BORDERRIGHT];
		}
		if(_perfectWidth > _layoutW && _layoutW > 0) {
			_perfectWidth = _layoutW;
		}
		return _perfectWidth;
	}
	
	private int getPerfectHeight(GWidget _gWidget, int width, int _layoutHeight) {
		int _perfectHeight = 0;
		//获得最佳高
		if(_gWidget.vmData[GWidget.GW_VM_FUNC_GET_PERFECT_HEIGHT] != 0) {
			synchronized (vmGame.gtvm) {
				_perfectHeight = vmGame.gtvm.callback(_gWidget.vmData[GWidget.GW_VM_FUNC_GET_PERFECT_HEIGHT], new int[] { 
						_gWidget.vmData[GW_VM_SELF], width, _gWidget.vmData[GWidget.GW_VM_MAX_HEIGHT]
					});
			}
		} else {
			_perfectHeight = _gWidget.vmData[GWidget.GW_VM_MIN_HEIGHT] + _gWidget.vmData[GWidget.GW_VM_BORDERTOP] + _gWidget.vmData[GWidget.GW_VM_BORDERBOTTOM];
		}
		
		if(_perfectHeight > _layoutHeight && _layoutHeight > 0) {
			_perfectHeight = _layoutHeight;
		}
		
		return _perfectHeight;
	}
	
	/**
	 * 水平布局
	 * 
	 * @param gtvm
	 */
	private void layoutH() {
		//layout的有效范围
		int _count = children.size();
		int _nextX = vmData[GWidget.GW_VM_BORDERLEFT];
		int _nextY = vmData[GWidget.GW_VM_BORDERTOP];
		int _perfectWidth = 0;
		int _perfectHeight = 0;
		int _maxH = 0;
		for(int i=0; i<_count; i++) {
			GWidget _gWidget = (GWidget)children.elementAt(i);
			//不需要做layout
			if(_gWidget.noNeedLayout) {
				continue;
			}
			
			//递归，先做里层的layout
			if(_gWidget instanceof GContainer) {
				((GContainer)_gWidget).layout2();
			}
			
			_perfectWidth = getPerfectWidth(_gWidget, 0);
			_perfectHeight = getPerfectHeight(_gWidget, _perfectWidth, 0);
			
			_gWidget.setBounds(_nextX, _nextY, _perfectWidth, _perfectHeight);			
			_nextX += _perfectWidth + layoutData[L_HGAP];
			
			_maxH = Math.max(_maxH, _perfectHeight);
		}
		
		if(isScale) {
			_nextX -= layoutData[L_HGAP];
			setSize(_nextX + vmData[GWidget.GW_VM_BORDERRIGHT], _maxH + vmData[GWidget.GW_VM_BORDERTOP] + vmData[GWidget.GW_VM_BORDERBOTTOM]);
			setMinSize(_nextX - vmData[GWidget.GW_VM_BORDERLEFT], _maxH - vmData[GWidget.GW_VM_BORDERTOP]);
			
		}
	
	}

	/**
	 * 垂直布局
	 * 
	 * @param gtvm
	 */
	private void layoutV() {
		int _count = children.size();
		int _nextX = vmData[GWidget.GW_VM_BORDERLEFT];
		int _nextY = vmData[GWidget.GW_VM_BORDERTOP];
		int _perfectWidth = 0;
		int _perfectHeight = 0;
		int _maxW = 0;
		int _stopNextY = 0;
		firstInViewIndex = -1;
		lastInViewIndex = -1;
		boolean isOutOfContainer = false; //是否越界
		/*
		//#if ModelID == Android || ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad
		//#  needScrollBar = false;
		//#endif
		*/
		for(int i=0; i<_count; i++) {
			GWidget _gWidget = (GWidget)children.elementAt(i);
			_gWidget.setOutView(false);
			//不需要做layout
			if(_gWidget.noNeedLayout) {
				continue;
			}
			
			//递归，先做里层的layout
			if(_gWidget instanceof GContainer) {
				((GContainer)_gWidget).layout2();
			}
			
			_perfectWidth = getPerfectWidth(_gWidget, 0);
			_perfectHeight = getPerfectHeight(_gWidget, _perfectWidth, 0);
			
			_gWidget.setBounds(_nextX, _nextY, _perfectWidth, _perfectHeight);	
			
			//如果该组件越界，容器内从该组件开始的其余组件设为越界			
			if(_stopNextY == 0) {
				if(isIntersectView == false) {
					if(_nextY + _perfectHeight + vmData[GWidget.GW_VM_BORDERBOTTOM] > vmData[GWidget.GW_VM_MAX_HEIGHT]) {
						isOutOfContainer = true;
					}
				} else {
					if(_nextY + _gWidget.vmData[GWidget.GW_VM_MIN_HEIGHT]> vmData[GWidget.GW_VM_MAX_HEIGHT]) {
						isOutOfContainer = true;
					}
				}
			}
			
			if(isOutOfContainer) {
				GWidget _gWidget2 = (GWidget)children.elementAt(i);
				_gWidget2.setOutView(true);
			}
			
			if(isOutOfContainer && _stopNextY == 0) {				
				if(firstInViewIndex == -1) {
					firstInViewIndex = 0;
				}
				if(lastInViewIndex == -1) {
					lastInViewIndex = i - 1;
				}
								
				_stopNextY = _nextY;
			}
			
			_nextY += _perfectHeight + layoutData[L_VGAP];			
			_maxW = Math.max(_maxW, _perfectWidth);			

		}	
		if(firstInViewIndex == -1) {
			firstInViewIndex = 0;
		}
		if(lastInViewIndex == -1) {
			lastInViewIndex = _count - 1;
		}
		realHeight = _nextY - layoutData[L_VGAP] + vmData[GWidget.GW_VM_BORDERBOTTOM];
		if(_stopNextY > 0) {
			needScrollBar = true;
			outHeight = _nextY - _stopNextY;
			if(gsb != null) {
				gsb.maxScrollDis = outHeight;
			}
			_nextY = _stopNextY;
		} else {
			needScrollBar = false;
		}
		if(isScale) {
			_nextY -= layoutData[L_VGAP];
			int _width = _maxW + vmData[GWidget.GW_VM_BORDERLEFT] + vmData[GWidget.GW_VM_BORDERRIGHT];
			if(_width < vmData[GWidget.GW_VM_MIN_WIDTH] ) {
				_width = vmData[GWidget.GW_VM_MIN_WIDTH]; 
			} else if(_width > vmData[GWidget.GW_VM_MAX_WIDTH]) {
				_width = vmData[GWidget.GW_VM_MAX_WIDTH];
			}
			if(_nextY + vmData[GWidget.GW_VM_BORDERBOTTOM] > vmData[GWidget.GW_VM_MAX_HEIGHT]) {
				_nextY = vmData[GWidget.GW_VM_MAX_HEIGHT] - vmData[GWidget.GW_VM_BORDERBOTTOM];
			}
			
			synchronized(lock){
				setSize(_width, _nextY + vmData[GWidget.GW_VM_BORDERBOTTOM]);
			}			
			setMinSize(_maxW - vmData[GWidget.GW_VM_BORDERLEFT], _nextY - vmData[GWidget.GW_VM_BORDERRIGHT]);
			
		}
		/*
		//#if ModelID == Android || ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad
		//#  if(needScrollBar){
		//#  	LayoutV1();
		//#  }
		//#endif
		 */
	}
	
//#if ModelID == Android || ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad
	//#  /**
	//#   * 垂直布局
	//#   * 
	//#   * @param gtvm
	//#   */
	//#  private void LayoutV1() {
	//#  	int _count = children.size();
	//#  	int _nextX = vmData[GWidget.GW_VM_BORDERLEFT];
	//#  	int _nextY = vmData[GWidget.GW_VM_BORDERTOP];
	//#  	int _perfectWidth = 0;
	//#  	int _perfectHeight = 0;
	//#  	int _maxW = 0;
	//#  	int _stopNextY = 0;
	//#  	firstInViewIndex = -1;
	//#  	lastInViewIndex = -1;
	//#  	boolean isOutOfContainer = false; //是否越界
	//#  	for(int i=0; i<_count; i++) {
	//#  		GWidget _gWidget = (GWidget)children.elementAt(i);
	//#  		_gWidget.setOutView(false);
	//#  		//不需要做layout
	//#  		if(_gWidget.noNeedLayout) {
	//#  			continue;
	//#  		}
	//#  		
	//#  		//递归，先做里层的layout
	//#  		if(_gWidget instanceof GContainer) {
	//#  			((GContainer)_gWidget).layout2();
	//#  		}
	//#  		
	//#  		_perfectWidth = getPerfectWidth(_gWidget, 0);
	//#  		_perfectHeight = getPerfectHeight(_gWidget, _perfectWidth, 0);
	//#  		
	//#  		_gWidget.setBounds(_nextX, _nextY, _perfectWidth, _perfectHeight);	
	//#  		
	//#  		//如果该组件越界，容器内从该组件开始的其余组件设为越界			
	//#  		if(_stopNextY == 0) {
	//#  			if(isIntersectView == false) {
	//#  				if(_nextY + _perfectHeight + vmData[GWidget.GW_VM_BORDERBOTTOM] > vmData[GWidget.GW_VM_MAX_HEIGHT]) {
	//#  					isOutOfContainer = true;
	//#  				}
	//#  			} else {
	//#  				if(_nextY + _gWidget.vmData[GWidget.GW_VM_MIN_HEIGHT]> vmData[GWidget.GW_VM_MAX_HEIGHT]) {
	//#  					isOutOfContainer = true;
	//#  				}
	//#  			}
	//#  		}
	//#  		
	//#  		if(isOutOfContainer) {
	//#  			GWidget _gWidget2 = (GWidget)children.elementAt(i);
	//#  			_gWidget2.setOutView(true);
	//#  		}
	//#  		
	//#  		if(isOutOfContainer && _stopNextY == 0) {				
	//#  			if(firstInViewIndex == -1) {
	//#  				firstInViewIndex = 0;
	//#  			}
	//#  			if(lastInViewIndex == -1) {
	//#  				lastInViewIndex = i - 1;
	//#  			}
	//#  							
	//#  			_stopNextY = _nextY;
	//#  		}
	//#  		
	//#  		_nextY += _perfectHeight + layoutData[L_VGAP];			
	//#  		_maxW = Math.max(_maxW, _perfectWidth);			
	//#  
	//#  	}	
	//#  	if(firstInViewIndex == -1) {
	//#  		firstInViewIndex = 0;
	//#  	}
	//#  	if(lastInViewIndex == -1) {
	//#  		lastInViewIndex = _count - 1;
	//#  	}
	//#  	realHeight = _nextY - layoutData[L_VGAP] + vmData[GWidget.GW_VM_BORDERBOTTOM];
	//#  	if(_stopNextY > 0) {
	//#  		//needScrollBar = true;
	//#  		outHeight = _nextY - _stopNextY;
	//#  		if(gsb != null) {
	//#  			gsb.maxScrollDis = outHeight;
	//#  		}
	//#  		_nextY = _stopNextY;
	//#  	} else {
	//#  		//needScrollBar = false;
	//#  	}
	//#  	if(isScale) {
	//#  		_nextY -= layoutData[L_VGAP];
	//#  		int _width = _maxW + vmData[GWidget.GW_VM_BORDERLEFT] + vmData[GWidget.GW_VM_BORDERRIGHT];
	//#  		if(_width < vmData[GWidget.GW_VM_MIN_WIDTH] ) {
	//#  			_width = vmData[GWidget.GW_VM_MIN_WIDTH]; 
	//#  		} else if(_width > vmData[GWidget.GW_VM_MAX_WIDTH]) {
	//#  			_width = vmData[GWidget.GW_VM_MAX_WIDTH];
	//#  		}
	//#  		if(_nextY + vmData[GWidget.GW_VM_BORDERBOTTOM] > vmData[GWidget.GW_VM_MAX_HEIGHT]) {
	//#  			_nextY = vmData[GWidget.GW_VM_MAX_HEIGHT] - vmData[GWidget.GW_VM_BORDERBOTTOM];
	//#  		}
	//#  		
	//#  		synchronized(lock){
	//#  			setSize(_width, _nextY + vmData[GWidget.GW_VM_BORDERBOTTOM]);
	//#  		}			
	//#  		setMinSize(_maxW - vmData[GWidget.GW_VM_BORDERLEFT], _nextY - vmData[GWidget.GW_VM_BORDERRIGHT]);
	//#  		
	//#  	}
	//#  }
//#elif ModelID == AndroidAuto
	//#  /**
	//#   * 垂直布局
	//#   * 
	//#   * @param gtvm
	//#   */
	//# private void LayoutV1() {
	//# if (!GameMain.getUIModel().equals(GameMain.ANDROID_SMALL))
	//# {

	//#  	int _count = children.size();
	//#  	int _nextX = vmData[GWidget.GW_VM_BORDERLEFT];
	//#  	int _nextY = vmData[GWidget.GW_VM_BORDERTOP];
	//#  	int _perfectWidth = 0;
	//#  	int _perfectHeight = 0;
	//#  	int _maxW = 0;
	//#  	int _stopNextY = 0;
	//#  	firstInViewIndex = -1;
	//#  	lastInViewIndex = -1;
	//#  	boolean isOutOfContainer = false; //是否越界
	//#  	for(int i=0; i<_count; i++) {
	//#  		GWidget _gWidget = (GWidget)children.elementAt(i);
	//#  		_gWidget.setOutView(false);
	//#  		//不需要做layout
	//#  		if(_gWidget.noNeedLayout) {
	//#  			continue;
	//#  		}
	//#  		
	//#  		//递归，先做里层的layout
	//#  		if(_gWidget instanceof GContainer) {
	//#  			((GContainer)_gWidget).layout2();
	//#  		}
	//#  		
	//#  		_perfectWidth = getPerfectWidth(_gWidget, 0);
	//#  		_perfectHeight = getPerfectHeight(_gWidget, _perfectWidth, 0);
	//#  		
	//#  		_gWidget.setBounds(_nextX, _nextY, _perfectWidth, _perfectHeight);	
	//#  		
	//#  		//如果该组件越界，容器内从该组件开始的其余组件设为越界			
	//#  		if(_stopNextY == 0) {
	//#  			if(isIntersectView == false) {
	//#  				if(_nextY + _perfectHeight + vmData[GWidget.GW_VM_BORDERBOTTOM] > vmData[GWidget.GW_VM_MAX_HEIGHT]) {
	//#  					isOutOfContainer = true;
	//#  				}
	//#  			} else {
	//#  				if(_nextY + _gWidget.vmData[GWidget.GW_VM_MIN_HEIGHT]> vmData[GWidget.GW_VM_MAX_HEIGHT]) {
	//#  					isOutOfContainer = true;
	//#  				}
	//#  			}
	//#  		}
	//#  		
	//#  		if(isOutOfContainer) {
	//#  			GWidget _gWidget2 = (GWidget)children.elementAt(i);
	//#  			_gWidget2.setOutView(true);
	//#  		}
	//#  		
	//#  		if(isOutOfContainer && _stopNextY == 0) {				
	//#  			if(firstInViewIndex == -1) {
	//#  				firstInViewIndex = 0;
	//#  			}
	//#  			if(lastInViewIndex == -1) {
	//#  				lastInViewIndex = i - 1;
	//#  			}
	//#  							
	//#  			_stopNextY = _nextY;
	//#  		}
	//#  		
	//#  		_nextY += _perfectHeight + layoutData[L_VGAP];			
	//#  		_maxW = Math.max(_maxW, _perfectWidth);			
	//#  
	//#  	}	
	//#  	if(firstInViewIndex == -1) {
	//#  		firstInViewIndex = 0;
	//#  	}
	//#  	if(lastInViewIndex == -1) {
	//#  		lastInViewIndex = _count - 1;
	//#  	}
	//#  	realHeight = _nextY - layoutData[L_VGAP] + vmData[GWidget.GW_VM_BORDERBOTTOM];
	//#  	if(_stopNextY > 0) {
	//#  		//needScrollBar = true;
	//#  		outHeight = _nextY - _stopNextY;
	//#  		if(gsb != null) {
	//#  			gsb.maxScrollDis = outHeight;
	//#  		}
	//#  		_nextY = _stopNextY;
	//#  	} else {
	//#  		//needScrollBar = false;
	//#  	}
	//#  	if(isScale) {
	//#  		_nextY -= layoutData[L_VGAP];
	//#  		int _width = _maxW + vmData[GWidget.GW_VM_BORDERLEFT] + vmData[GWidget.GW_VM_BORDERRIGHT];
	//#  		if(_width < vmData[GWidget.GW_VM_MIN_WIDTH] ) {
	//#  			_width = vmData[GWidget.GW_VM_MIN_WIDTH]; 
	//#  		} else if(_width > vmData[GWidget.GW_VM_MAX_WIDTH]) {
	//#  			_width = vmData[GWidget.GW_VM_MAX_WIDTH];
	//#  		}
	//#  		if(_nextY + vmData[GWidget.GW_VM_BORDERBOTTOM] > vmData[GWidget.GW_VM_MAX_HEIGHT]) {
	//#  			_nextY = vmData[GWidget.GW_VM_MAX_HEIGHT] - vmData[GWidget.GW_VM_BORDERBOTTOM];
	//#  		}
	//#  		
	//#  		synchronized(lock){
	//#  			setSize(_width, _nextY + vmData[GWidget.GW_VM_BORDERBOTTOM]);
	//#  		}			
	//#  		setMinSize(_maxW - vmData[GWidget.GW_VM_BORDERLEFT], _nextY - vmData[GWidget.GW_VM_BORDERRIGHT]);
	//#  		
	//#  	}
	//#  }
	//# }
//#endif
	static Object lock = new Object();
	
	/**
	 * GRID 布局
	 * 
	 * @param gtvm
	 */
	private void layoutG() {		
		if(layoutData[L_ROWS] > 0 && layoutData[L_COLS] > 0) {
			//layout的有效范围
			int _layoutX = vmData[GWidget.GW_VM_BORDERLEFT];
			int _layoutY = vmData[GWidget.GW_VM_BORDERTOP];
			int _layoutW = vmData[GWidget.GW_VM_W] - vmData[GWidget.GW_VM_BORDERLEFT] - vmData[GWidget.GW_VM_BORDERRIGHT];
			int _layoutH = vmData[GWidget.GW_VM_H] - vmData[GWidget.GW_VM_BORDERTOP] - vmData[GWidget.GW_VM_BORDERBOTTOM];
			//格子宽
			int _gridW = (_layoutW + layoutData[L_HGAP])/ layoutData[L_COLS] - layoutData[L_HGAP];
			//格子高
			int _gridH = (_layoutH + layoutData[L_VGAP])/ layoutData[L_ROWS] - layoutData[L_VGAP];
						
			layoutGrid(_layoutX, _layoutY, _layoutW, _layoutH, _gridW, _gridH, layoutData[L_ROWS], layoutData[L_COLS]);
		}
	}
	
	private void layoutG2() {		
		//layout的有效范围
		//#if ModelID == Android || ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad
		//# int nWidth = 0;
		//#  VMGame vg = VMGame.getTopUIVM();
		//#  	int nScreenHeight = GameMain.viewHeight;
		//#  	if(needScrollBar){
		//#  		if(nScreenHeight == 480){
		//#  			nWidth = 54;
		//#  		}
		//#  		else if(nScreenHeight == 320){
		//#  			nWidth = 27;
		//#  		}
		//#  	}
		//#  if(vg.getVMId().equals("ui_shop"))
		//#  {
		//#  	System.out.println("###############ui_shop");
		//#  	if(nScreenHeight == 480){
		//#  		nWidth = 54;
		//#  	}
		//#  	else if(nScreenHeight == 320){
		//#  		nWidth = 27;
		//#  	}
		//#  }
		//#elif ModelID == AndroidAuto
		//# int nWidth = 0;
		//# if (!GameMain.getUIModel().equals(GameMain.ANDROID_SMALL))
    	//# {
		//#  nWidth = 0;
		//#  VMGame vg = VMGame.getTopUIVM();
		//#  	int nScreenHeight = GameMain.viewHeight;
		//#  	if(needScrollBar){
		//#  		if(nScreenHeight == 480){
		//#  			nWidth = 54;
		//#  		}
		//#  		else if(nScreenHeight == 320){
		//#  			nWidth = 27;
		//#  		}
		//#  	}
		//#  if(vg.getVMId().equals("ui_shop"))
		//#  {
		//#  	System.out.println("###############ui_shop");
		//#  	if(nScreenHeight == 480){
		//#  		nWidth = 54;
		//#  	}
		//#  	else if(nScreenHeight == 320){
		//#  		nWidth = 27;
		//#  	}
		//#  }
		//# }
		//#endif
		int _layoutX = vmData[GWidget.GW_VM_BORDERLEFT];
		int _layoutY =  vmData[GWidget.GW_VM_BORDERTOP];
		int _layoutW = vmData[GWidget.GW_VM_W] - vmData[GWidget.GW_VM_BORDERLEFT] - vmData[GWidget.GW_VM_BORDERRIGHT];
		int _layoutH = vmData[GWidget.GW_VM_H] - vmData[GWidget.GW_VM_BORDERTOP] - vmData[GWidget.GW_VM_BORDERBOTTOM];
		
		int _cols = (_layoutW + layoutData[L_HGAP]) / (layoutData[L_GRID_W] + layoutData[L_HGAP]);
		int _rows = (_layoutH + layoutData[L_VGAP]) / (layoutData[L_GRID_H] + layoutData[L_VGAP]);
		//#if ModelID == Android || ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad
		//#  layoutGrid(_layoutX, _layoutY, _layoutW - nWidth, _layoutH, layoutData[L_GRID_W], layoutData[L_GRID_H], _rows, _cols);
		//#elif ModelID == AndroidAuto
		//# if (!GameMain.getUIModel().equals(GameMain.ANDROID_SMALL))
    	//# {
		//#  layoutGrid(_layoutX, _layoutY, _layoutW - nWidth, _layoutH, layoutData[L_GRID_W], layoutData[L_GRID_H], _rows, _cols);
		//# }
		//# else
		//# {
		//# layoutGrid(_layoutX, _layoutY, _layoutW, _layoutH, layoutData[L_GRID_W], layoutData[L_GRID_H], _rows, _cols);
		//# }
		//#else
		layoutGrid(_layoutX, _layoutY, _layoutW, _layoutH, layoutData[L_GRID_W], layoutData[L_GRID_H], _rows, _cols);
		//#endif
	}
	
	private void layoutGrid(int _layoutX, int _layoutY, int _layoutW, int _layoutH, int _gridW, int _gridH, int _rows, int _cols) {
		//水平右侧剩余空隙
		int _hRightGap = _layoutW - (_gridW + layoutData[L_HGAP]) * _cols;
		_layoutX = _layoutX + _hRightGap / 2;
		_layoutW = _layoutW - _hRightGap;
		//垂直下侧剩余空隙
		int _vBottomGap = _layoutH - (_gridH + layoutData[L_VGAP]) * _rows;
		_layoutY = _layoutY + _vBottomGap / 2;	
		_layoutH = _layoutH - _vBottomGap;
		
		int _count = children.size();
		int realRows = _count / _cols;
		if(_count % _cols > 0) {
			realRows ++;
		}
		for(int i = 0; i < realRows; i++) {
			for(int j = 0; j < _cols; j++) {
				int _widgetIndex = i * _cols + j;
				if(_widgetIndex > _count - 1) {
					break;
				}
				int _startX = _layoutX + j * (_gridW + layoutData[L_HGAP]) + layoutData[L_HGAP] / 2;
				int _startY = _layoutY + i * (_gridH + layoutData[L_VGAP]) + layoutData[L_VGAP] / 2;
				
				GWidget _gWidget = (GWidget)children.elementAt(_widgetIndex);
				//_gWidget.setBounds(_startX, _startY, _gridW, _gridH);
				if(_gWidget.isScale) {					
					_gWidget.setBounds(_startX, _startY, _gridW, _gridH);
				} else {
					_gWidget.setBounds(_startX + (_gridW - _gWidget.vmData[GWidget.GW_VM_W]) / 2, _startY + (_gridH - _gWidget.vmData[GWidget.GW_VM_H]) / 2, _gWidget.vmData[GWidget.GW_VM_W], _gWidget.vmData[GWidget.GW_VM_H]);	
				}
			}
		}
		
		//超出的部分处理
		int count = children.size();
		int sum = _cols * _rows;
		if(sum < count) {
			needScrollBar = true;
			outHeight = (realRows - _rows) * (_gridH + layoutData[L_VGAP]);
			if(gsb != null) {
				gsb.maxScrollDis = outHeight;
			}
			firstInViewIndex = 0;
			lastInViewIndex = sum - 1;
			
			for(int i=sum; i<count; i++) {
				GWidget _gWidget = (GWidget)children.elementAt(i);
				_gWidget.setOutView(true);
			}
		} else {
			needScrollBar = false;
			if(gsb != null) {
				gsb.maxScrollDis = 0;
			}
			firstInViewIndex = 0;
			lastInViewIndex = count - 1;
			
			for(int i=0; i<count; i++) {
				GWidget _gWidget = (GWidget)children.elementAt(i);
				_gWidget.setOutView(false);
			}
		}
	}
	
	/**
	 * 只做本容器内的layout
	 * @param gtvm
	 */
	public void grid3Layout() {
		//layout的有效范围
		int _layoutX = vmData[GWidget.GW_VM_BORDERLEFT];
		int _layoutY = vmData[GWidget.GW_VM_BORDERTOP];
		int _layoutW = vmData[GWidget.GW_VM_W] - vmData[GWidget.GW_VM_BORDERLEFT] - vmData[GWidget.GW_VM_BORDERRIGHT];
		int _layoutH = vmData[GWidget.GW_VM_H] - vmData[GWidget.GW_VM_BORDERTOP] - vmData[GWidget.GW_VM_BORDERBOTTOM];
		
		int gridW = _layoutW / layoutData[L_COLS];
		int gridH = _layoutH / layoutData[L_ROWS];
		int realW = gridW * layoutData[L_COLS];
		int fixW = _layoutW % gridW+1;
		
		int _count = children.size();
		for(int i = 0; i < _count; i++) {
			GWidget _gWidget = (GWidget)children.elementAt(i);
			if(_gWidget.grid3Data != null) {
				int _w = _gWidget.grid3Data[2] * gridW - _gWidget.grid3Data[6] - _gWidget.grid3Data[7];
				int _h = _gWidget.grid3Data[3] * gridH - _gWidget.grid3Data[4] - _gWidget.grid3Data[5];
				int _x = _layoutX + _gWidget.grid3Data[0] * gridW + _gWidget.grid3Data[6];
				int _y = _layoutY + _gWidget.grid3Data[1] * gridH + _gWidget.grid3Data[4];
				if((_gWidget.grid3Data[0] + _gWidget.grid3Data[2])* gridW == realW) {
					_w += fixW;
				}
				if(_gWidget.isScale) {					
					_gWidget.setBounds(_x, _y, _w, _h);	
				} else {
					_gWidget.setBounds(_x + (_w - _gWidget.vmData[GWidget.GW_VM_W]) / 2, _y + (_h - _gWidget.vmData[GWidget.GW_VM_H]) / 2, _gWidget.vmData[GWidget.GW_VM_W], _gWidget.vmData[GWidget.GW_VM_H]);	
				}
				
			}			
		}
	}
	
	public void borderLayout() {
		int _layoutX = vmData[GWidget.GW_VM_BORDERLEFT];
		int _layoutY = vmData[GWidget.GW_VM_BORDERTOP];
		int _layoutW = vmData[GWidget.GW_VM_W] - vmData[GWidget.GW_VM_BORDERLEFT] - vmData[GWidget.GW_VM_BORDERRIGHT];
		int _layoutH = vmData[GWidget.GW_VM_H] - vmData[GWidget.GW_VM_BORDERTOP] - vmData[GWidget.GW_VM_BORDERBOTTOM];
		
		GWidget northGWidget = getBorderLayoutGWidget(GW_BORDER_LAYOUT_NORTH);
		GWidget southGWidget = getBorderLayoutGWidget(GW_BORDER_LAYOUT_SOUTH);
		GWidget westGWidget = getBorderLayoutGWidget(GW_BORDER_LAYOUT_WEST);
		GWidget eastGWidget = getBorderLayoutGWidget(GW_BORDER_LAYOUT_EAST);
		GWidget centerGWidget = getBorderLayoutGWidget(GW_BORDER_LAYOUT_CENTER);
		
		int _perfectWidth = _layoutW;
		int _perfectHeight = _layoutH;
		//layout的有效范围
		if(northGWidget != null) {
			northGWidget.vmData[GWidget.GW_VM_W] = _layoutW;
			//#if ScreenCanReset == true
			//# if(northGWidget instanceof GContainer && !northGWidget.noNeedLayout) {
			//#else
			if(northGWidget instanceof GContainer) {
			//#endif
				((GContainer)northGWidget).layout2();
			}
			_perfectHeight = getPerfectHeight(northGWidget, _layoutW, _layoutH);
			
			northGWidget.setBounds(_layoutX, _layoutY, _layoutW, _perfectHeight);			
			_layoutY += northGWidget.vmData[GWidget.GW_VM_H] + this.layoutData[L_UP_GAP];
			_layoutH = _layoutH - northGWidget.vmData[GWidget.GW_VM_H] - this.layoutData[L_UP_GAP];
			_perfectHeight = _layoutH;
		}
		
		if(southGWidget != null) {
			southGWidget.vmData[GWidget.GW_VM_W] = _layoutW;
			//#if ScreenCanReset == true
			//# if(southGWidget instanceof GContainer && !southGWidget.noNeedLayout) {
			//#else
			if(southGWidget instanceof GContainer) {
			//#endif
				((GContainer)southGWidget).layout2();
			}	
			_perfectHeight = getPerfectHeight(southGWidget, _layoutW, _layoutH);
			southGWidget.setBounds(_layoutX, vmData[GWidget.GW_VM_H] - _perfectHeight - vmData[GWidget.GW_VM_BORDERBOTTOM], _layoutW, _perfectHeight);
			_layoutH = _layoutH - southGWidget.vmData[GWidget.GW_VM_H] - this.layoutData[L_DOWN_GAP];
		
			_perfectHeight = _layoutH;
		}
		
		if(westGWidget != null) { 
			//#if ScreenCanReset == true
			//# if(westGWidget instanceof GContainer && !westGWidget.noNeedLayout) {
			//#else
			if(westGWidget instanceof GContainer) {
			//#endif
				((GContainer)westGWidget).layout2();
			}
			_perfectWidth = getPerfectWidth(westGWidget, _layoutW);
			if(_perfectHeight == 0) {
				_perfectHeight = getPerfectHeight(westGWidget, _perfectWidth, _layoutH);
			}
			westGWidget.setBounds(_layoutX, _layoutY, _perfectWidth, _perfectHeight);
			_layoutX = westGWidget.vmData[GWidget.GW_VM_W] + this.layoutData[L_LEFT_GAP];
			_layoutW -= _layoutX;
			_perfectWidth = _layoutW;
			_perfectHeight = _layoutH;
		}
		
		if(eastGWidget != null) {
			//#if ScreenCanReset == true
			//# if(eastGWidget instanceof GContainer && !eastGWidget.noNeedLayout) {
			//#else
			if(eastGWidget instanceof GContainer) {
			//#endif
				((GContainer)eastGWidget).layout2();
			}
			_perfectWidth = getPerfectWidth(eastGWidget, _layoutW);
			if(_perfectHeight == 0) {
				_perfectHeight = getPerfectHeight(eastGWidget, _perfectWidth, _layoutH);
			}
			eastGWidget.setBounds(vmData[GWidget.GW_VM_W] - _perfectWidth - vmData[GWidget.GW_VM_BORDERRIGHT], _layoutY, _perfectWidth, _perfectHeight);
			_layoutW = _layoutW - eastGWidget.vmData[GWidget.GW_VM_W] - this.layoutData[L_RIGHT_GAP];
		}
		
		if(centerGWidget != null) {
			centerGWidget.setBounds(_layoutX, _layoutY, _layoutW, _layoutH);
			//#if ScreenCanReset == true
			//# if(centerGWidget instanceof GContainer && !centerGWidget.noNeedLayout) {
			//#else
			if(centerGWidget instanceof GContainer) {
			//#endif
				((GContainer)centerGWidget).layout2();
			}
			centerGWidget.setBounds(_layoutX, _layoutY, _layoutW, _layoutH);
		}
		
		if(_layoutW == 0) {
			int _width = 0;
			if(northGWidget != null) {
				_width = northGWidget.vmData[GWidget.GW_VM_W];
			}
			if(southGWidget != null) {
				if(_width < southGWidget.vmData[GWidget.GW_VM_W]) {
					_width = southGWidget.vmData[GWidget.GW_VM_W];
				}
			}
			
			int _width2 = this.layoutData[L_LEFT_GAP] + this.layoutData[L_RIGHT_GAP];
			if(westGWidget != null) {
				_width2+= westGWidget.vmData[GWidget.GW_VM_W];
			}
			if(eastGWidget != null) {
				_width2+= eastGWidget.vmData[GWidget.GW_VM_W];
			}
			if(centerGWidget != null) {
				_width2+= centerGWidget.vmData[GWidget.GW_VM_W];
			}
			if(_width < _width2) {
				_width = _width2;
			}
			
			vmData[GWidget.GW_VM_MIN_WIDTH] = _width;
		}		

		if(_layoutH == 0) {
			int _height = 0;
			if(westGWidget != null) {
				_height = westGWidget.vmData[GWidget.GW_VM_H];
			}
			if(eastGWidget != null) {
				if(_height < eastGWidget.vmData[GWidget.GW_VM_H]) {
					_height = eastGWidget.vmData[GWidget.GW_VM_H];
				}
			}
			if(centerGWidget != null) {
				if(_height < centerGWidget.vmData[GWidget.GW_VM_H]) {
					_height = centerGWidget.vmData[GWidget.GW_VM_H];
				}
			}		
			_height += this.layoutData[L_UP_GAP] + this.layoutData[L_DOWN_GAP];
			if(northGWidget != null) {
				_height += northGWidget.vmData[GWidget.GW_VM_H];
			}
			if(southGWidget != null) {
				_height += southGWidget.vmData[GWidget.GW_VM_H];
			}			 
			
			vmData[GWidget.GW_VM_MIN_HEIGHT] = _height;
		}
	}
	
	//#if buildtest == true
	public void paintGWidgetDebug() {
		int count = this.children.size();
		for(int i = 0; i<count; i++) {
			
			GWidget _gWidget = (GWidget)this.children.elementAt(i);
				
			
//			if(_gWidget.name.endsWith("GListItemContainer")) {
				Utilities.graphics.setClip(0, 0, GameMain.viewWidth, GameMain.viewHeight);
				
				int[] rect2 = new int[4];
				rect2[0] = _gWidget.vmData[GWidget.GW_VM_XX] + _gWidget.vmData[GW_VM_OFFSET_X];
				rect2[1] = _gWidget.vmData[GWidget.GW_VM_YY] + _gWidget.vmData[GW_VM_OFFSET_Y];
				rect2[2] = _gWidget.vmData[GWidget.GW_VM_W];
				rect2[3] = _gWidget.vmData[GWidget.GW_VM_H];
				Utilities.graphics.setColor(0x0000FF);
//				Utilities.graphics.drawString(_gWidget.name, rect2[0], rect2[1], 0);
				Utilities.graphics.drawRect(rect2[0], rect2[1], rect2[2], rect2[3]);
//				}
			
			if(_gWidget instanceof GContainer) {
				((GContainer)_gWidget).paintGWidgetDebug();
			}
		}
	}
	//#endif
}
