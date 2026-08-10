package com.pip.gui;

import java.util.Vector;

import com.pip.sanguo.GameView;
import com.pip.ui.VM;
import com.pip.ui.VMGame;

public class GWindow extends GContainer{		
	public boolean isTransparent;
	
	public String funcCycle;
	public String funcCycleUI;
	public String funcPaint;
	public String funcPacket;
	
	Object[][] inJavaObjStack = new Object[4][];
	Object[][] inVmObjStack = new Object[4][];
	int[][] funcStack = new int[4][];
	int[] paintType;
	    
	public GWidget focusWidget;

	public static GWidget pressWidget; //鼠标选中的控件
	public static GWidget dropTargetWidget; //鼠标拖放时的目标对象
	
	boolean reCreateStack = true;
	
	public static boolean isDragging; //系统是否在拖拽状态
	public boolean catchInput;
	public boolean ignorePauseUICycle; //cycleui不理会PauseUICycle
    
	//#if ScreenCanReset == true
    //# //脚本界面的最大宽高
    //# public static int uiMaxWidth;
    //# public static int uiMaxHeight;
    //# public static int uiLeft;
    //# public static int uiTop;
    //# public static boolean forcePaintWorld; //不管界面是否全屏，都绘制游戏世界
	//# public boolean fullScreen;
    //#endif
    
	public GWindow(VMGame _vmGame, int self, int[] vmData, boolean isTransparent, String name) {
		super(_vmGame, self, vmData, name);
		this.isTransparent = isTransparent;
	}
		
	public boolean isTransparent() {
		return isTransparent;
	}
	
	public void setReCreateStack() {
		this.reCreateStack = true;		
	}
		
	public void setCatchInput(boolean catchInput) {
		this.catchInput = catchInput;
	}
	
	public void setFocus(GWidget gWidget) {
		if(gWidget != null) {
			GWidget oldFocusWidget = focusWidget;
			focusWidget = gWidget;
			
			if(oldFocusWidget!= null && focusWidget != null && focusWidget.parent != null) {
				focusWidget.parent.setSrollBar(oldFocusWidget, gWidget);
			}
			
			if(oldFocusWidget != null) {
				sendFocusEvent(oldFocusWidget, false);
			}
			
			sendFocusEvent(focusWidget, true);
			
		}
	}
	
	public void move(int offsetX, int offsetY) {
		this.vmData[GW_VM_XX] += offsetX;
		this.vmData[GW_VM_YY] += offsetY;
		
		this.setPos(vmData[GW_VM_X] + offsetX, vmData[GW_VM_Y] += offsetY);
		
	}
	
	private void sendFocusEvent(GWidget gWidget, boolean isFocus) {
		gWidget.isFocus = isFocus;
		int eventType = isFocus ? GW_EVENT_GET_FOCUS : GW_EVENT_LOST_FOCUS;
		//处理resize事件
		if(gWidget.vmData[GW_VM_FUNC_SEND_EVENT] != 0) {
			/*
			 * send_event(
			 *         GWidget _gWidget, 
			 *         int _eventType, 
			 *         int _param0, 
			 *         int[] _param2, 
			 *         Object _param1,
			 *         Object[] _param3
			 *       )
			 */
			synchronized (vmGame.gtvm) {
				vmGame.gtvm.callback(gWidget.vmData[GW_VM_FUNC_SEND_EVENT], new int[] { 
						gWidget.vmData[GW_VM_SELF], 
						eventType,
						0,
						0,
						0,
						0
					});
			}
		}
	}
	
	/**
	 * 处理window的回调机制
	 */
	public void handleCaller(int type, boolean blocked) {
		//对象栈
		Vector objStack = new Vector();
		//脚本函数栈
		Vector vmFuncStack = new Vector();
		//脚本paint方法类型栈
		Vector paintTypeStack = new Vector();
		
		String funcName = null;        
        int funcId = 0;
        
        int curStackId = type - 1;
        switch(type){
            case VM.CYCLE:
            	funcName = funcCycle;
                funcId = vmData[GW_VM_FUNC_CYCLE];                
                break;
            case VM.CYCLEUI:
                //处理PauseUICycle()
                if((!blocked || ignorePauseUICycle) && this.isShow){
                	funcName = funcCycleUI;
                    funcId = vmData[GW_VM_FUNC_CYCLEUI];
                    handleCycleUI(focusWidget, objStack, vmFuncStack);
                    resetCallStack(type, objStack, vmFuncStack, paintTypeStack, curStackId);
                } else {
                	return;
                }
                break;
            case VM.PAINT:
            	if(this.isShow) {
            		if(isJavaPaint) {
                		this.paintContainer();
                	} else {
                		funcName = funcPaint;
                        funcId = vmData[GW_VM_FUNC_PAINT];
                	}
            	} else {
            		return;
            	}
            	
                break;
            case VM.PROCESSPACKET:
            	funcName = funcPacket;
                funcId = vmData[GW_VM_FUNC_PACKET];
                break;
        }

        if(type == VM.PAINT && reCreateStack) {
        	createCallStack(type, funcId, objStack, vmFuncStack, paintTypeStack);    		
        	resetCallStack(type, objStack, vmFuncStack, paintTypeStack, curStackId);
        	reCreateStack = false;
        	
        } else if(type == VM.CYCLE || type == VM.PROCESSPACKET){
        	createCallStack(type, funcId, objStack, vmFuncStack, paintTypeStack);    		
        	resetCallStack(type, objStack, vmFuncStack, paintTypeStack, curStackId);
        }

        if(funcName != null) {
        	VM gtvm = vmGame.gtvm;
        	//回调独立window, 没有孩子的window
    		if (this.children.size() == 0) {    			
    			synchronized (gtvm) {
    				gtvm.callback(funcName, new int[] { vmData[GW_VM_SELF] });
    			}
    		} else if(inJavaObjStack[curStackId] != null && inJavaObjStack[curStackId].length > 0){
    			//回调复合window
    			if(type == VM.PAINT) {
        			synchronized (gtvm) {
        				gtvm.callback(funcName, new int[] { 
        						vmData[GW_VM_SELF], 
        						gtvm.makeTempObject(inJavaObjStack[curStackId]),
        						gtvm.makeTempObject(inVmObjStack[curStackId]), 
        						gtvm.makeTempObject(funcStack[curStackId]),
        						gtvm.makeTempObject(paintType),
        						inJavaObjStack[curStackId].length
        					});
        			}
        			//#if buildtest == true
//        			paintGWidgetDebug();
        			//#endif
    			} else {
        			synchronized (gtvm) {
        				gtvm.callback(funcName, new int[] { 
        						vmData[GW_VM_SELF], 
        						gtvm.makeTempObject(inJavaObjStack[curStackId]),
        						gtvm.makeTempObject(inVmObjStack[curStackId]), 
        						gtvm.makeTempObject(funcStack[curStackId]),
        						inJavaObjStack[curStackId].length
        					});
        			}
    			}

    		}
        }
	}
	
	private void createCallStack(int type, int funcId, Vector objStack, Vector vmFuncStack, Vector paintTypeStack) {
		if(type == VM.PAINT && this.vmData[GW_VM_FUNC_PAINT_BEFORE] > 0) {
			objStack.addElement(this);
			vmFuncStack.addElement(new Integer(this.vmData[GW_VM_FUNC_PAINT_BEFORE]));
			paintTypeStack.addElement(new Integer(PAINT_TYPE_IN_VM));
		}
		
		//加入window自己的回调方法
        if(funcId != 0 && isShow){
        	objStack.addElement(this);
        	vmFuncStack.addElement(new Integer(funcId));
        	if(type == VM.PAINT) {
        		paintTypeStack.addElement(new Integer(PAINT_TYPE_IN_VM));	
        	}
        	
        }
        
        //加入子组件的回调方法
        setCallStack(type, objStack, vmFuncStack, paintTypeStack);
        
		if(type == VM.PAINT && this.vmData[GW_VM_FUNC_PAINT_AFTER] > 0) {
			objStack.addElement(this);
			vmFuncStack.addElement(new Integer(this.vmData[GW_VM_FUNC_PAINT_AFTER]));
			paintTypeStack.addElement(new Integer(PAINT_TYPE_AFTER));
		}
	}
	
	
	/**
	 * 重新设置调用栈
	 * 
	 * @param gtvm
	 * @param type
	 * @param vmObjStack
	 * @param vmFuncStack
	 */
	public void resetCallStack(int type, Vector objStack, Vector vmFuncStack, Vector paintTypeStack, int curStackId) {                
        int count = objStack.size();
        inJavaObjStack[curStackId] = new Object[count];
        inVmObjStack[curStackId] = new Object[count];
		funcStack[curStackId] = new int[count];
		VM gtvm = vmGame.getVM();
		if(type == VM.PAINT) {
			paintType = new int[count];
		}
		synchronized (gtvm) {
			for(int i=0; i<count; i++) {
				GWidget gw = (GWidget)objStack.elementAt(i);
				
				inJavaObjStack[curStackId][i] = gw;
				inVmObjStack[curStackId][i] = gw.vmData;

				funcStack[curStackId][i] = ((Integer)vmFuncStack.elementAt(i)).intValue();
				
				if(type == VM.PAINT) {
					paintType[i] = ((Integer)paintTypeStack.elementAt(i)).intValue();
				}
			}
		}		
        
	}
	
	/**
	 * 
	 * 沿焦点控件处理按键事件
	 * 
	 * @param gtvm
	 * @param vmObjStack
	 * @param vmFuncStack
	 */
	private void handleCycleUI(GWidget _focusWidget, Vector vmObjStack, Vector vmFuncStack) {		
		if(_focusWidget != null) {
			if(_focusWidget.vmData[GWidget.GW_VM_FUNC_CYCLEUI] > 0) {
        		vmObjStack.addElement(_focusWidget);
            	vmFuncStack.addElement(new Integer(_focusWidget.vmData[GWidget.GW_VM_FUNC_CYCLEUI]));
			}
			
			if(_focusWidget.parent != null) {
				handleCycleUI(_focusWidget.parent, vmObjStack, vmFuncStack);
			}
		}

	}
	
	public VMGame getVMGame() {
		return vmGame;
	}
	
	public boolean canHandleCycleUI(GWidget gWidget) {
		boolean canHandelCycleUI = false;
		if(this.focusWidget != null) {
			if(gWidget == focusWidget) {
				canHandelCycleUI = true;
			} else {
				GWidget _parent = focusWidget.parent;
				while(_parent != null) {
					if(_parent == gWidget) {
						canHandelCycleUI = true;
						break;
					} else {
						_parent = _parent.parent;
					}
					
				}
			}
		}
		return canHandelCycleUI;
	}
}
