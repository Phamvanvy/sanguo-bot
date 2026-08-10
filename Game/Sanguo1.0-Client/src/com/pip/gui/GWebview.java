//android--newui--com--pip--gui--GWebview.java替代这个文件

package com.pip.gui;

//#if ModelID == AndroidAuto || ModelID == AndroidLarge || ModelID == Android || ModelID == AndroidSmall
//# import android.os.Handler;
//# import android.os.Looper;
//# import android.os.Message;
//# import android.view.Gravity;
//# import android.view.KeyEvent;
//# import android.view.MotionEvent;
//# import android.view.View;
//# import android.view.View.OnTouchListener;
//# import android.webkit.WebView;
//# import android.webkit.WebViewClient;
//# import android.widget.FrameLayout;
//# import android.widget.PopupWindow;
//# import com.pip.android.PipActivity;
//#endif
import com.pip.sanguo.GameMain;
import com.pip.sanguo.GameWorld;
import com.pip.ui.VMGame;

public class GWebview  extends GWidget {
	public boolean isLoading;
	//#if ModelID == AndroidAuto || ModelID == AndroidLarge || ModelID == Android || ModelID == AndroidSmall
	//# private WebView webview;
	//# PopupWindow _popWindow = null;
	//#endif
	
	public GWebview(VMGame _vmGame , int self, int[] vmData, String name, int x0, int y0, int width0, int height0,final String url) {
		super(_vmGame, self, vmData, name);
		isLoading = true;
		//#if ModelID == AndroidAuto || ModelID == AndroidLarge || ModelID == Android || ModelID == AndroidSmall
		//#if NewUI2
		//# float scale = GameMain.getScale();
		//#else
		//# float scale = 1.0f;
		//#endif
		//# final int pX = (int)(x0 * scale);
		//# final int pY = (int)(x0 * scale);
		//# int width = (int)(width0 * scale);
		//# int height = (int)(height0 * scale);
		//实例化WebView对象 
		//# sendMSCMessage(new IFBackListener(){
		//# 	public void processBack(Object pObj, int x, int y, int width, int height) {
		//# 		_popWindow = new PopupWindow(PipActivity.DEFAULT_ACTIVITY.mainView, width, height);
		//# 		final FrameLayout fl = new FrameLayout(PipActivity.DEFAULT_ACTIVITY){
		//# 			public boolean onTouchEvent(MotionEvent event) {	//当触摸popupwindow以外区域时调用
		//# 				event.setLocation(pX + event.getX(), pY + event.getY());	//重设触屏坐标，因为event为_popWindow的相对坐标，当传入到主view事件需要相对主view的坐标，在此做转换
		//# 				return GameMain.instance.onTouch(null, event);	//回调主acitivity view
		//# 			}
		//# 		};
		//# 		webview = new WebView(PipActivity.DEFAULT_ACTIVITY);
		//# 		webview.setWebViewClient(new WebViewClient() {
		//# 			//重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
		//# 			public boolean shouldOverrideUrlLoading(WebView view, String url){
		//#                    view.loadUrl(url);
		//#                   return true;
		//#           }
		//# 		});
		//# 		webview.getSettings().setJavaScriptEnabled(true);
		//# 		webview.loadUrl(url);
		//# 		fl.addView(webview);
		//# 		_popWindow.setContentView(fl);
		//# 		_popWindow.setFocusable(true); //设置PopupWindow可获得焦点
		//# 		_popWindow.setWidth(width);
		//# 		_popWindow.setHeight(height);
		//# 		_popWindow.showAtLocation(PipActivity.DEFAULT_ACTIVITY.mainView, Gravity.TOP | Gravity.LEFT, x, y);
		//# 		_popWindow.update();
		//# 		isLoading = false;
		//# 	}
		//# }, new int[]{pX, pY, width, height});
		//#endif
	}
	
	//#if ModelID == AndroidAuto || ModelID == AndroidLarge || ModelID == Android || ModelID == AndroidSmall
	//# private static MyHandler myHandler = new MyHandler(Looper.getMainLooper());
	//# 
	//# public static void sendMSCMessage(IFBackListener backlistener, Object exInfo){
	//# 	Message m = Message.obtain();
	//# 	m.what = 0;
	//# 	m.obj = new Object[]{backlistener,exInfo};
	//# 	myHandler.sendMessage(m);
	//# }
	//# 
	//# private static class MyHandler extends Handler{
	//# 	MyHandler(Looper looper){
	//# 		super(looper);
	//# 	}
	//# 	public void handleMessage(Message m) {//处理消息
	//# 		Object[] pars = (Object[])m.obj;
	//#			if(pars[0] instanceof IFBackListener){
	//#				if(pars[1] instanceof int[]){
	//#					int[] params = (int[])pars[1];
	//#					((IFBackListener)pars[0]).processBack(pars[1], params[0], params[1], params[2], params[3]);
	//#				}else{
	//#					((IFBackListener)pars[0]).processBack(pars[1],0,0,0,0);
	//#				}
	//#			}
	//# 	}
	//# }
	//# 	
	//# public static interface IFBackListener{
	//# 	void processBack(Object pObj, int x, int y, int width, int height);
	//# }
	//# 	
	//# public void goBack() {
	//# 	sendMSCMessage(new IFBackListener() {
	//# 		public void processBack(Object pObj, int x, int y, int width, int height) {
	//# 			if(_popWindow != null){
	//# 				_popWindow.dismiss();
	//# 				webview = null;
	//# 				_popWindow = null;
	//# 			}
	//# 		}
	//# 	}, null);
	//# }
	//#endif
	
	public void setBounds(int _x, int _y, int _w, int _h) {
		super.setBounds(_x, _y, _w, _h);
	}
	
	public boolean isLoading() {
		return this.isLoading;
	}
}
