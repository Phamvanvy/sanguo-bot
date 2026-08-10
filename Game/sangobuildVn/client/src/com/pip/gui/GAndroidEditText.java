package com.pip.gui;

import javax.microedition.lcdui.Font;
import javax.microedition.midlet.MIDlet;

//#if ModelID == Lenovo || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == AndroidAuto
//# import android.widget.AbsoluteLayout;
//# import android.widget.EditText;
//# import com.pip.android.PipActivity;
//#endif

import com.pip.common.Utilities;
import com.pip.sanguo.GameMain;
import com.pip.ui.VMGame;

public class GAndroidEditText extends GWidget{
	//#if ModelID == Lenovo || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == AndroidAuto
	//# EditText et;
	//# AbsoluteLayout.LayoutParams lp;
	//#endif
	boolean hasAddScreen;
	
	public GAndroidEditText(VMGame game, int self, int[] vmData, String name) {
		super(game, self, vmData, name);
		
		//#if ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidAuto
		//# et = new EditText(MIDlet.DEFAULT_MIDLET.getActivity());		
		this.vmData[GW_VM_H] = Utilities.font.getHeight();
		this.vmData[GW_VM_MIN_HEIGHT] = this.vmData[GW_VM_H];
		this.vmData[GW_VM_MAX_HEIGHT] = this.vmData[GW_VM_H];
		
		//# lp = new AbsoluteLayout.LayoutParams(10, this.vmData[GW_VM_H], 0, 0);
		//# et.setLayoutParams(lp);
		//# et.setVisibility(EditText.INVISIBLE);
		//#endif
	}
	
	public GWidget getClone(VMGame _vmGame) {
		GAndroidEditText gAib = new GAndroidEditText(_vmGame, 0, getVMDataCopy(), name);
		//#if ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidAuto
		//# gAib.et.setText(this.et.getText());
		//#endif
		return gAib;
	}
	
	public void setShow(final boolean isShow) {
		super.setShow(isShow);
		
		//#if ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidAuto
		//# ((PipActivity)MIDlet.DEFAULT_ACTIVITY).invokeAndWait(new Runnable() {
		//# 	@Override
		//# 	public void run() {
		//#			if(hasAddScreen == false) {
		//#				addToScreen();
		//#				hasAddScreen = true;
		//#			}
		//# 		et.setVisibility(isShow ? EditText.VISIBLE : EditText.INVISIBLE);
		//# 	}			
		//# });
		//#endif
		
	}
	
	//因为是系统组件，所以要手动调用添加到屏幕上
	private void addToScreen() {
		//#if ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidAuto
		//# ((PipActivity)MIDlet.DEFAULT_ACTIVITY).invokeAndWait(new Runnable() {
		//# 	@Override
		//# 	public void run() {
		//# 		GameMain.instance.getView().addView(et);
		//# 	}			
		//# });		
		//#endif
	}
	
	private void removeFromScreen() {
		hasAddScreen = false;
		//#if ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidAuto
		//# ((PipActivity)MIDlet.DEFAULT_ACTIVITY).invokeAndWait(new Runnable() {
		//# 	@Override
		//# 	public void run() {
		//# 		GameMain.instance.getView().removeView(et);
		//# 	}			
		//# });	
		//#endif
	}
		
	public void setText(String text) {
		//#if ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidAuto
		//# et.setText(text);
		//#endif
	}
	
	//#if ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidAuto
	//# public String getText(){		
	//# 	return et.getText().toString();		
	//# }
	//#endif
	
	public void setPos(int x, int y) {
		super.setPos(x, y);
		
		//#if ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidAuto
		//# lp.x = this.vmData[GW_VM_XX];
		//# lp.y = this.vmData[GW_VM_YY];
		//#endif
	}
	
	public void setSize(int w, int h) {
		super.setSize(w, h);
		
		//#if ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidAuto
		//# lp.width = w;
		//# lp.height = h;
		//#endif
	}
	
	public void setBounds(int _x, int _y, int _w, int _h) {
		super.setBounds(_x, _y, _w, _h);
		
		//#if ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidAuto
		//# lp.width  = _x;
		//# lp.height = _y;
		//# lp.width  = _w;
		//# lp.height = _h;
		//#endif
	}
	
	public void move(int offsetX, int offsetY) {
		super.move(offsetX, offsetY);
		
		//#if ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidAuto
		//# lp.x = this.vmData[GW_VM_XX];
		//# lp.y = this.vmData[GW_VM_YY];
		//#endif
	}
	
	public void aibGetFocus() {
		//#if ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidAuto
		//# ((PipActivity)MIDlet.DEFAULT_ACTIVITY).invokeAndWait(new Runnable() {
			//# 	@Override
			//# 	public void run() {
				//# 		et.requestFocus();
				//# 	}			
			//# });
		//#endif
	}
	
	public void aibLostFocus() {
		//#if ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidAuto
		//# ((PipActivity)MIDlet.DEFAULT_ACTIVITY).invokeAndWait(new Runnable() {
		//# 	@Override
			//# 	public void run() {
			//# 		GameMain.instance.getCanvasView().requestFocus();
			//# 	}			
			//# });		
		//#endif
	}
	
	//#if ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidAuto
	//# public int getCaretPosition() {
	//# 	return et.getSelectionStart();
	//# }
	//#endif
	
	public void freeVMObj() {
		super.freeVMObj();
		
		removeFromScreen();
	}
}
