/////////////////////////////////////////////////////////////////////////////
// @(#)IGB_TEST.java
/////////////////////////////////////////////////////////////////////////////
//
// 建 立 者: Frezzy
//
// 建立日期: 2009/05/27
//
// 維 護 者:
//
// 最後修改: 2009/05/27
//
// 說    明: 測試用的MIDlet
//
/////////////////////////////////////////////////////////////////////////////
//
//    Copyright (C) 2004 Joymaster Corporation. All Rights Reserved.
//
/////////////////////////////////////////////////////////////////////////////


import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

import ut.Utilities;

import java.util.*;
import billing.*;
/////////////////////////////////////////////////////////////////////////////

public class IGBC extends MIDlet implements CommandListener , Runnable
{
  public static IGBKernel IGBKernel;
  public static JQcommunity JQC;
  public BillingEventHandler billing;
  protected static IGBC runner;
  public UserData usd;  
  public Loging Loging;
  public static Display display;
  private static Canvas canvas;
  public Vector gamelist ;
  public Form test_form = new Form( "測試畫面" );
  private Command test1 = new Command("WAP儲值", Command.SCREEN, 1);
  private Command test2 = new Command("INGAME儲值", Command.SCREEN, 1);
  private Command test3 = new Command("連線失敗", Command.SCREEN, 1);
  private Command test8 = new Command("需要註冊", Command.SCREEN, 1);
  private Command test4 = new Command("付費成功 "  , Command.SCREEN, 2) ;
  private Command test5 = new Command("付費失敗", Command.SCREEN, 2);
  private Command test6 = new Command("取消測試模式", Command.SCREEN, 4);
  private Command test7 = new Command("開始執行", Command.SCREEN, 3);
  private Command test9 = new Command("使用測試用tsi", Command.SCREEN, 0);
  int testmode =0;
  int sendtestmode=0;
  boolean do_next = false;
  public static boolean use_test_tsi = false;
  /**
   * Start/Resume the session
   */
  protected void startApp()
  {  
      try
      {                
        
        display.setCurrent(test_form);
        test_form.addCommand(test1);
        test_form.addCommand(test2);
        test_form.addCommand(test3);
        test_form.addCommand(test4);
        test_form.addCommand(test5);
        test_form.addCommand(test6);
        test_form.addCommand(test7);
        test_form.addCommand(test8);
        test_form.addCommand(test9);
        test_form.setCommandListener(this);
        test_form.append("付費項目測試模式="+testmode+"\n");
        test_form.append("付款測試模式="+sendtestmode+"\n");  
        test_form.append("測試版tsi="+use_test_tsi+"\n");
        do_next = false;
        while(true)
        {
        	if(do_next)
            {
            	//display.setCurrent(this);
            	perform();
            	do_next = false;
            	use_test_tsi = false;
            	testmode =0;
            	sendtestmode=0;
            }
        }
        
        
      }
      catch( Exception e )
      {
        
      }   
  }
  public void perform()
  {
	  System.out.println("perform");
	  try
      {                
        //建立IGB機制核心物件,傳入MIDlet物件與Record宣告的大小
    	//測試參數區域    	
    	String tsi = "20100121860";
    	////////////////////////
    	////////////////核心區塊//////////////
    	IGBKernel = new IGBKernel(this);
    	System.out.println("紀錄使用者資料,可供社群與金流使用");
        usd = new UserData();
        System.out.println("生成登入介面,用於社群登入使用");
        Loging = new Loging();
        
        //進入付費金流區塊/////////////////
        gamelist = new Vector();
        billing = new BillingEventHandler();   
        
        //設定測試模式
        billing.setTestMode(testmode);//設定取得儲值時候的測試模式
        billing.setTestMode_sendPayChoose(sendtestmode);//設定傳送付費項目時候的測試模式
        //取得儲值用商品清單!如果為測試tsi模式則使用測試版本tsi!方便模擬器測試
        
        if( use_test_tsi )
        {
        	//使用測試tsi
        	gamelist = billing.getStoreType( tsi);
        }
        else
        {
        	//模擬登入行為取得tsi
        	Loging.synctsi();
        	System.out.println("更新完tsi資訊");
        	gamelist = billing.getStoreType( usd.syncDara[usd.syncDara_tsi]);
        }
        System.out.println("gamelist="+gamelist.toString());        
        String paymode = (String)gamelist.elementAt(billing.STORETYPEDATA_TYPE);
        if(  Integer.parseInt(paymode) == billing.DATATYPE_WAPSTORE)
        {
        	//回傳模式為需要開啟WAP儲值頁面
        	System.out.println("網頁URL="+(String)gamelist.elementAt(billing.STORETYPEDATA_VAULE));
        }
        else if(Integer.parseInt(paymode) == billing.DATATYPE_INGAMESTORE)
        {        	
        	//回傳模式可以進行IN GAME儲值動作!流程頁面由遊戲端進行控制
        	//回傳內容為商品清單
        	String[][] paylist = (String[][])gamelist.elementAt(billing.STORETYPEDATA_VAULE);
            for(int i =0;i<paylist.length;i++)
            {
            	for(int j=0;j<paylist[i].length;j++)
            	{
            		System.out.println("paylist["+i+"]["+j+"]="+paylist[i][j]); 
            	}
            		     
            }
            //假定玩家已完成選擇商品動作;進行儲值動作
            //預設先進行IN GAME儲值行為,倘若無法成功進行,則改變為發送簡訊方式進行儲值
            //回傳狀態會因為儲值方式不同有不同的回傳參數內容
            String testresport="";
            if( use_test_tsi )
            {
                testresport = billing.sendPayChoose( tsi, Integer.parseInt(paylist[0][0]) );
            }
            else
            {
            	 testresport = billing.sendPayChoose( usd.syncDara[usd.syncDara_tsi], Integer.parseInt(paylist[0][0]) );
            }        	
        	System.out.println("testresport="+testresport);
        	String[] resportdata = Utilities.stringTokenize( testresport, ',' );
        	if( Integer.parseInt(resportdata[0]) == billing.STOREREQUEST_SUCCESS_INGAME )
        	{
        		//如果IN GAME付費成功,取得收據
        		
        	}
        	else if(Integer.parseInt(resportdata[0]) == billing.STOREREQUEST_SUCCESS_SMS)
        	{
        		//SMS付費成功,不會有收據
        	}
        	else if(Integer.parseInt(resportdata[0]) == billing.STOREREQUEST_FAILED)
        	{
        		//儲值失敗!
        	}
        	       	
        }
        else if(Integer.parseInt(paymode) == billing.DATATYPE_ERROR)
        {
        	System.out.print("erroe="+(String)gamelist.elementAt(billing.STORETYPEDATA_VAULE));
        }
        else if(Integer.parseInt(paymode) == billing.DATATYPE_REGISTER)
        {
        	System.out.print("簡訊短碼="+(String)gamelist.elementAt(billing.STORETYPEDATA_VAULE));
        	String text1= (String)gamelist.elementAt(billing.STORETYPEDATA_VAULE);
        	billing.sendSMS(display, text1.substring(0, text1.indexOf(";")), text1.substring(text1.indexOf(";")+1, text1.length()));
        }
        ////////////////社群階段/////////////////////////////////////////
        JQC = new JQcommunity();//建立社群物件
        //if(IGBKernel.getCommunitySetting() == 1)//取得是否需要出現社群選項的判斷式
        {           
            
            JQC.perform(usd,Loging);//進入社群            
        }
        
      }
      catch( Exception e )
      {
        
      }   
  }
  public void commandAction(Command c, Displayable arg1) {
      if (c == test1) {
    	  testmode = 1;
      } else if (c == test2) {
    	  testmode = 2;
      } else if (c == test3) {
    	  testmode = 4;
      } else if (c == test4) {
    	  sendtestmode = 1;
      } else if (c == test5) {
    	  sendtestmode = 2;
      } else if (c == test6) {
    	  sendtestmode = 0;
    	  testmode = 0;
      } 
      else if (c == test7) {
    	  do_next = true;
      }else if (c == test8) {
    	  testmode = 3;
      } 
      else if(c == test9)
      {
    	  use_test_tsi = true;
      }
      test_form.deleteAll();
      test_form.append("付費項目測試模式="+testmode+"\n");
      test_form.append("付款測試模式="+sendtestmode+"\n");   
      test_form.append("測試版tsi="+use_test_tsi+"\n");
  }
  /**
   * Pause the session
   */
  protected void pauseApp()
  {
    
  }
  /**
   * Stop the session
   */
  protected void destroyApp( boolean unconditional )
  {
    
  }
  /**
   * Initialize the midlet/iappli.
   */
  public IGBC()
  {
      display = Display.getDisplay( this );
      canvas = new GameCanvas(); 
      
  }
  public void run()
  {        
  }
  ////////////////////////////////////////
}
 class GameCanvas extends javax.microedition.lcdui.game.GameCanvas
 {
    public GameCanvas()
    {
        super( false );
    }
           
    protected void hideNotify()
    {
        
    }
    protected void showNotify()
    {
        
    }
    public void paint( Graphics g )
    {
        g.fillRect( 0, 0, 300, 300);
    }
 }
/////////////////////////////////////////////////////////////////////////////
//
//    Copyright (C) 2004 Joymaster Corporation. All Rights Reserved.
//
/////////////////////////////////////////////////////////////////////////////
