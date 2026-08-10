package extendSDK;


import com.pip.common.Tool;
import com.pip.sanguo.GameMain;
import com.pip.sanguo.SanguoMIDlet;
import com.pip.ui.VM;
import com.pip.ui.VMGame;

//#if ChannelCode == UC_CHANNEL_JAVA
//# import com.uc.gsdk.javame.api.*;
//#endif

public class ucSDK {
	//#if ChannelCode == UC_CHANNEL_JAVA
	//# static String  msg;
	//# static boolean  sdkInited;
	//# public static ucSDK ucSDK;
//# 	
//# 	
	//# public static void initUC(){
		//# //获取UCGameSdk单例
		//# UCGameSdk sdk = UCGameSdk.defaultSdk();
		//# Tool.setGlobalValue("initUCCode", -1);
		//# //设置游戏相关参数cpid\gameid\serverid\channelid
		//#ifdef buildtest
		//# // int kindsID[]={12,822,826};//测试
	    //#endif
		//# int kindsID[]={12,822,1034,2};//正式
		//# sdk.cpId = kindsID[0]; // 设置游戏合作商编号，该编号在游戏接入时由UC分配
		//# sdk.gameId = kindsID[1]; // 设置游戏编号，该编号在游戏接入时由UC分配
		//# sdk.serverId = kindsID[2]; // 游戏服务器编号，也即分区编号，此编号由UC游戏平台分配，作为游戏分区标识。
		//# sdk.channelId = "2"; // 设置发行渠道编号，该编号在游戏接入时由UC分配
		//# sdk.isDebug = false; //是否为联调模式，true=联调模式，false=正式模式。SDK根据此属性的值决定连接UC的生产环境或联调环境。
		//# sdk.logLevel = UCLogLevel.ERROR; //设置SDK日志级别，SDK将根据不同级别的日志打印出日志信息。
		//# sdk.gameMidlet = SanguoMIDlet.instance;//设置游戏的Midlet
		//# sdk.gameCanvas = GameMain.instance; //设置游戏的Canvas
		//# //初始化SDK
		//# sdk.initSDK(new UCSdkListener(){
			//# public void onApiFinished(UCSdkEvent evt) {
				//# try{
					//# if (!evt.success) {
						//# msg = evt.msg; 
						//# Tool.setGlobalValue("initUCCode", -1);
						//# return;
					//# }
					//# msg = "SDK初始化成功!";
					//# sdkInited = true;
					//# Tool.setGlobalValue("initUCCode", 0);
				//# }catch(Exception e){
					//# e.printStackTrace();
				//# }
				//# //通evt获取初始化结果
			//# }}
		//# );
	//# }
//# 	
	//# public static void UCLogin(){
		//# UCGameSdk.defaultSdk().login(
			//# new UCSdkListener(){
				//# public void onApiFinished(UCSdkEvent evt) {
				//# //通过evt获取登录结果
				//# if (evt.success) {
					//# msg = "登录成功!";
		        	//#ifdef buildtest
					//# System.out.println("sid---->"+getSId());
		            //#endif
					//# VMGame vmg = VMGame.getVMGame("ui_mainmenu");
					//# vmg.gtvm.continueProcess(VM.TRUE);
					//# return;
				//# }
					//# VMGame vmg = VMGame.getVMGame("ui_mainmenu");
					//# vmg.gtvm.continueProcess(VM.FALSE);
					//# msg = evt.msg;
				//# }
			//# },false,null
		//# );
	//# }
//# 	
	//# public static String getSId() {
		//# return UCGameSdk.defaultSdk().getSid();
	//# }
//# 	
	//# public static void UCCharge(){
//# //		UCGameSdk.defaultSdk().serverId = 826; // 重新设置当前的游戏服务器编号（分区），以能够针对该服务器进行充值。
		//# //设置充值信息
		//# UCPayParam payParam = new UCPayParam();
		//# //获取UCGameSdk单例调用登录方法pay
		//# UCGameSdk.defaultSdk().pay(
			//# new UCSdkListener(){
				//# public void onApiFinished(UCSdkEvent evt){
		//# //		state=STATE_MENU;
				//# if(evt.success){
					//# try{
					//# UCPayOrderInfo order=(UCPayOrderInfo)evt.data;
					//# System.out.println("充值订单号："+order.orderId);
					//# //游戏自有处理逻辑
					//# //可通过order.orderId、order.amount、order.payWayName 获取对应订单信息
					//# }catch(Exception e){// no order info.
					//# //异常处理逻辑
					//# }
					//# return;
				//# }
				//# //充值失败处理
				//# }
			//# },payParam
		//# );
	//# }
	//#endif
}
