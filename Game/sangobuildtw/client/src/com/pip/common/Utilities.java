package com.pip.common;


import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import com.pip.io.UASegment;
import com.pip.io.UASocketConnection;
import com.pip.sanguo.GameMain;
import com.pip.sanguo.SanguoMIDlet;
import com.pip.ui.VM;
import com.pip.util.SortHashtable;


/**
 * 
 * @author Frank
 */
public class Utilities implements Runnable{
    /** 建立的连接 */
    public static UASocketConnection connection = null;
    /** 接收到的网络包 */
    public static Vector segments = new Vector();

    public static final int KEY_NUM0 = 48;
    public static final int KEY_NUM1 = 49;
    public static final int KEY_NUM2 = 50;
    public static final int KEY_NUM3 = 51;
    public static final int KEY_NUM4 = 52;
    public static final int KEY_NUM5 = 53;
    public static final int KEY_NUM6 = 54;
    public static final int KEY_NUM7 = 55;
    public static final int KEY_NUM8 = 56;
    public static final int KEY_NUM9 = 57;
    public static final int KEY_STAR = 42;
    public static final int KEY_POUND = 35;

    // 机型键值
    //#if KeyCodeType == Motorola
    //# public static final byte KEY_UP = 1;
    //# public static final byte KEY_DOWN = 6;
    //# public static final byte KEY_LEFT = 2;
    //# public static final byte KEY_RIGHT = 5;
    //# public static final byte KEY_FIRE = 20;
    //# public static final byte KEY_LEFT_SOFT = 21;
    //# public static final byte KEY_RIGHT_SOFT = 22;
    //#elif KeyCodeType == MotorolaE2
    //# public static final byte KEY_UP = 1;
    //# public static final byte KEY_DOWN = 2;
    //# public static final byte KEY_LEFT = 3;
    //# public static final byte KEY_RIGHT = 4;
    //# public static final byte KEY_FIRE = 5;
    //# public static final byte KEY_LEFT_SOFT = 21;
    //# public static final byte KEY_RIGHT_SOFT = 22;
    //#else
    public static final byte KEY_UP = 1;
    public static final byte KEY_DOWN = 2;
    public static final byte KEY_LEFT = 3;
    public static final byte KEY_RIGHT = 4;
    public static final byte KEY_FIRE = 5;
    public static final byte KEY_LEFT_SOFT = 6;
    public static final byte KEY_RIGHT_SOFT = 7;
    //#endif

    // define constant to handle game key states
    public static final byte UP_PRESSED = 0;
    public static final byte DOWN_PRESSED = 1;
    public static final byte LEFT_PRESSED = 2;
    public static final byte RIGHT_PRESSED = 3;
    public static final byte FIRE_PRESSED = 4;
    public static final byte GAME_A_PRESSED = 5;
    public static final byte GAME_B_PRESSED = 6;
    public static final byte GAME_C_PRESSED = 7;
    public static final byte GAME_D_PRESSED = 8;

    // define constant to handle soft key states
    public static final byte SOFT_FIRST_PRESSED = 9;
    public static final byte SOFT_LAST_PRESSED = 10;

    // define constant to handle number key states
    public static final byte KEY_NUM0_PRESSED = 11;
    public static final byte KEY_NUM1_PRESSED = 12;
    public static final byte KEY_NUM2_PRESSED = 13;
    public static final byte KEY_NUM3_PRESSED = 14;
    public static final byte KEY_NUM4_PRESSED = 15;
    public static final byte KEY_NUM5_PRESSED = 16;
    public static final byte KEY_NUM6_PRESSED = 17;
    public static final byte KEY_NUM7_PRESSED = 18;
    public static final byte KEY_NUM8_PRESSED = 19;
    public static final byte KEY_NUM9_PRESSED = 20;
    public static final byte KEY_POUND_PRESSED = 21;
    public static final byte KEY_STAR_PRESSED = 22;

    // define constant to handle menu&ok&back key states
    //#if KeyCodeType == Motorola || KeyCodeType == MotorolaE2
    //# public static final byte BUTTON_MENU_PRESSED = SOFT_FIRST_PRESSED;
    //# public static final byte BUTTON_OK_PRESSED = SOFT_LAST_PRESSED;
    //# public static final byte BUTTON_BACK_PRESSED = SOFT_FIRST_PRESSED;
    //#elif KeyCodeType == SonyEricsson
    //# public static final byte BUTTON_MENU_PRESSED = SOFT_LAST_PRESSED;
    //# public static final byte BUTTON_OK_PRESSED = SOFT_FIRST_PRESSED;
    //# public static final byte BUTTON_BACK_PRESSED = SOFT_LAST_PRESSED;
    //#else
    public static final byte BUTTON_MENU_PRESSED = SOFT_FIRST_PRESSED;
    public static final byte BUTTON_OK_PRESSED = SOFT_FIRST_PRESSED;
    public static final byte BUTTON_BACK_PRESSED = SOFT_LAST_PRESSED;
    //#endif

    // 每两位表示一个键，高位表示是否按下（1为按下），低位表示是否处理（1表示未处理）
    public static long keyFlag;
    public static long keyFlag2;

    //字体
    //#if FontSize == large
    //# public static final Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    //#elif FontSize == medium
    //# public static final Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    //#else 
    public static Font font;
    static {
    	font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    	if (font.getHeight() < 12) {
    		font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    		if (font.getHeight() < 12) {
    			font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    		}
    	}
    }
    //#endif

    // 字体大小
    public static int CHAR_HEIGHT = font.getHeight();
    public static int CHAR_OFFSET = 0;
    public static int LINE_HEIGHT = CHAR_HEIGHT;
    public static int CHAR_WIDTH = font.stringWidth("国");

    public static Display display;
    public static Displayable canvas;
    public static Graphics graphics;

    /** 退出游戏 */
    public static boolean isExitGame = false;

    /** 记录游戏启动时间 */
    public static long appStartTime = System.currentTimeMillis();

    /** 上次收到服务器时间同步的时间 **/
    public static long lastSyncServerTime;
    /** 服务器时间 **/
    public static int serverTime;

    /** 取得从游戏开始到现在的间隔时间 */
    public static int getTimeStamp(){
        long now = System.currentTimeMillis();
        if(now < appStartTime){
            appStartTime = now;
        }
        return (int)(now - appStartTime);
    }

    /** 设置服务器时间 */
    public static void setServerTime(int time){
        lastSyncServerTime = System.currentTimeMillis();
        serverTime = time;
    }

    /** 取得服务器时间 */
    public static int getServerTime(){
        return serverTime + (int)(System.currentTimeMillis() - lastSyncServerTime);
    }

    /**
     * 菜单按钮是否在左下角
     * @return
     */
    public static boolean isButtonMenuOnLeft(){
        return BUTTON_MENU_PRESSED == SOFT_FIRST_PRESSED;
    }

    /**
     * OK按钮是否在左下角
     * @return
     */
    public static boolean isButtonOkOnLeft(){
        return BUTTON_OK_PRESSED == SOFT_FIRST_PRESSED;
    }

    /**
     * Back按钮是否在左下角
     * @return
     */
    public static boolean isButtonBackOnLeft(){
        return BUTTON_BACK_PRESSED == SOFT_FIRST_PRESSED;
    }

    public static void setDisplay(Display dp, Canvas cn){
        display = dp;
        canvas = cn;
    }

    private String requestURL; // VM请求URL
    public static final int THREAD_HTTP = 0;
    public static final int THREAD_UWAP = 1;
    public static final int THREAD_SMS = 2;
    private int threadMode; // 0 - 取HTTP内容，1 - 建立UWAP连接, 2 - 发送短信
    public VM listenVM; // 监听VM
    public byte[] lastDownloadData; // 上次请求返回的数据
    public byte[] postData;         // POST数据（可选）
    public SortHashtable requestProperties;    // 附加请求属性

    private boolean async; //是否为异步请求
    public int state; //请求状态
    
    public static final int DOWNLOAD_STATE_ERROR = 0;
    public static final int DOWNLOAD_STATE_RUNNING = 1;
    public static final int DOWNLOAD_STATE_OK = 2;
    
    /**
     * 创建一个网络连接任务。
     * @param uu 请求URL
     * @param tm 请求模式
     * @param lvm 监听VM
     */
    public Utilities(String uu, int tm, VM lvm, boolean async){
    	//#ifdef buildtest
        if (uu.startsWith("socket:") && tm == THREAD_UWAP) {
            String s = SanguoMIDlet.instance.getAppProperty("Private-Server");
            if (s != null) {
                uu = s;
            }
        }
        //#endif
        requestURL = uu;
        threadMode = tm;
        listenVM = lvm;
        this.async = async;
        state = DOWNLOAD_STATE_RUNNING;
    }

    public void run(){
        boolean result;
        try {
            if (threadMode == THREAD_HTTP) {
            	while (true) {
	                HttpConnection httpConnection = null;
	                try {
	                    // 建立连接
	                	boolean useProxy;
	                    if (requestURL.startsWith("p")) {
	                    	useProxy = true;
	                        httpConnection = UASegment.getConnection(requestURL.substring(1), true);
	                    } else {
	                    	useProxy = false;
	                        httpConnection = UASegment.getConnection(requestURL, false);
	                    }
	                    if (postData != null) {
	                        httpConnection.setRequestMethod(HttpConnection.POST);
	                    } else {
	                        httpConnection.setRequestMethod(HttpConnection.GET);
	                    }
	                    
	                    // 设置附加连接属性
	                    if (this.requestProperties != null) {
	                        Object[] keys = requestProperties.keys();
	                        for(int i = 0; i < keys.length; i++){
	                            String key = (String)keys[i];
	                            String value = (String)requestProperties.get(key);
	                            httpConnection.setRequestProperty(key, value);
	                        }
	                    }
	                    
	                    // 如果有POST数据，写出
	                    if (postData != null) {
	                        OutputStream os = httpConnection.openOutputStream();
	                        os.write(postData);
	                        os.close();
	                    }
	     
	                    int code = httpConnection.getResponseCode();
	     
	                    // 处理返回结果
	                    if (code >= 200 && code < 300) {
	                        DataInputStream in = httpConnection.openDataInputStream();
	                        lastDownloadData = Utilities.getBytesFromInput(in);
	                        in.close();
	                        break;
	                    } else if (code == 302) {
	                    	requestURL = httpConnection.getHeaderField("Location");
	                    	if (useProxy) {
	                    		requestURL = "p" + requestURL;
	                    	}
	                    } else {
	                        throw new IOException();
	                    }
	                } catch (Throwable ex2) {
	                    throw ex2;
	                } finally {
	                    if (httpConnection != null) {
	                        try {
	                            httpConnection.close();
	                        } catch (IOException ex3) {
	                        }
	                    }
	                }
            	}
            } else if (threadMode == THREAD_UWAP) {
                Utilities.createConnection(requestURL);
            } else if (threadMode == THREAD_SMS) {
                int pos = requestURL.indexOf('\n');
                String number = requestURL.substring(0, pos);
                String content = requestURL.substring(pos + 1);
                //#if SupportSMS == true
                // 发送短信
                MessageConnection connection = null;
                connection = (MessageConnection)Connector.open("sms://" + number);
                TextMessage message = (TextMessage)connection.newMessage(MessageConnection.TEXT_MESSAGE);
                message.setPayloadText(content);
                connection.send(message);
                //#else
                throw new IOException();
                //#endif
            }
            
            state = DOWNLOAD_STATE_OK;
            result = true;
        } catch (Throwable e) {
        	//#ifdef buildtest
            e.printStackTrace();
            System.out.println(requestURL);
          //#endif
            result = false;
            state = DOWNLOAD_STATE_ERROR;
        }
        if (listenVM != null && !async) {
            listenVM.continueProcess(result ? VM.TRUE : VM.FALSE);
        }
    }

    /**
     * 建立连接。
     * 
     * @throws Exception
     */
    public static void createConnection(String url) throws Exception{
        // 建立Socket连接。
        if(connection == null){
            //url = "socket://127.0.0.1:7000";
            //url = "socket://221.179.216.50:29999";
            //url = "socket://192.168.0.55:7001#C0A800371B5B";
            
        	//#ifdef buildtest
            System.out.println("url = " + url);
          //#endif
            
            connection = new UASocketConnection(url);
            connection.start();
        }
    }

    /**
     * 尝试重连。
     */
    public static void tryReconnect(){
        closeConnection();
    }

    /**
     * 关闭连接。
     */
    public static void closeConnection(){
        if(connection != null){
            try{
                UASocketConnection temp = connection;
                connection = null;
                temp.close();
            }catch(Exception e){
            	//#ifdef buildtest
                e.printStackTrace();
              //#endif
            }
        }

        System.gc();
    }

    /**
     * 向服务器发送请求。
     * 
     * @param segment 发送的包。
     */
    public static int sendRequest(UASegment segment){
        if(connection != null){
            UASocketConnection.writeSegment(segment);
           // System.out.println("request sent:"+segment.type);
        }else{
        	System.out.println("==============warning: connection is null. request not sent");
        }
        return segment.serial;
    }

    /**
     * 保存服务器回传的包。
     * 
     * @param segment 服务器回传的包。
     */
    public static void addSegment(UASegment segment){
        segments.addElement(segment);
    }

    //按键处理
    public static void keyPressed(int keyCode){
        keyCode = Math.abs(keyCode);
        try{
            int id = keyToGame(keyCode);
            if(id >= 0){
                keyFlag |= 3L << (id << 1);
            }

            id = keyToNum(keyCode);
            if(id >= 0){
                keyFlag |= 3L << (id << 1);
            }
            
             if(keyCode == GameMain.debugKeyOrder[GameMain.debugTestIndex]){
                GameMain.debugTestIndex++;

                if(GameMain.debugTestIndex >= GameMain.debugKeyOrder.length - 1){
                    GameMain.debugMode = !GameMain.debugMode;
                    Tool.setGlobalValue("varDebugModel", new Integer(GameMain.debugMode? VM.TRUE: VM.FALSE));
                }
                
                if(GameMain.debugTestIndex >= GameMain.debugKeyOrder.length){
                    GameMain.debugTestIndex = 0;
                    UASocketConnection.offlineMode = !UASocketConnection.offlineMode;
                }
            }else{
                GameMain.debugTestIndex = 0;
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public static void keyReleased(int keyCode){
        keyCode = Math.abs(keyCode);
        try{
            int id = keyToGame(keyCode);

            if(id >= 0){
                keyFlag &= ~(2L << (id << 1));
            }

            id = keyToNum(keyCode);

            if(id >= 0){
                keyFlag &= ~(2L << (id << 1));
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }

    }
    
    /**
     * 模拟一个按键事件
     * @param keyId
     */
    public static void sendKeyPressed(int keyId) {
        if(keyId >= 0){
            keyFlag |= 3L << (keyId << 1);
        }
        if(keyId >= 0){
            keyFlag &= ~(2L << (keyId << 1));
        }
    }
    
    public static void sendKeyDown(int keyId) {
        if(keyId >= 0){
            keyFlag |= 3L << (keyId << 1);
        }
    }
    
    public static void sendKeyUp(int keyId) {
        if(keyId >= 0){
            keyFlag &= ~(2L << (keyId << 1));
        }
    }

    public static int keyToGame(int keyCode){
        switch(keyCode){
            case KEY_UP:
                return UP_PRESSED;
            case KEY_DOWN:
                return DOWN_PRESSED;
            case KEY_LEFT:
                return LEFT_PRESSED;
            case KEY_RIGHT:
                return RIGHT_PRESSED;
            case KEY_FIRE:
                return FIRE_PRESSED;
            default:
                return -1;
        }
    }

    public static int keyToNum(int keyCode){
        switch(keyCode){
            case KEY_POUND:
                return KEY_POUND_PRESSED;
            case KEY_STAR:
                return KEY_STAR_PRESSED;
            case KEY_LEFT_SOFT:
                return SOFT_FIRST_PRESSED;
            case KEY_RIGHT_SOFT:
                return SOFT_LAST_PRESSED;
            default:
                if(keyCode >= KEY_NUM0 && keyCode <= KEY_NUM9){
                    return KEY_NUM0_PRESSED + (keyCode - KEY_NUM0);
                }
                return -1;
        }
    }

    /**
     * 判断是否有按键按下。
     * 
     * @return 有按键按下时返回true，否则返回false。
     */
    public static final boolean isAnyKeyPressed(){
        return keyFlag2 != 0;
    }

    /**
     * 判断是指定按键是否被按下。
     * 
     * @param key 指定按键的code。
     * @param clear 是否清除按键。
     * @return 指定按键被按下时返回true，否则返回false。
     */
    public static final boolean isKeyPressed(int key, boolean clear){
        // 判断按键是否按下
        long k = 3L << (key << 1);
        boolean ret = (keyFlag2 & k) != 0;

        if(clear){
            if(ret){
                keyFlag &= ~k;
                keyFlag2 &= ~k;
            }
        }
        
        return ret;
    }

    /**
     * 清除按键状态。
     */
    public static void clearKeyStates(){
        keyFlag = keyFlag2 = 0;
    }

    /**
     * 功能键检查（1-9，左右软键，＊＃）
     * 返回－1则没有键按下
     * 否则返回第一个检测到的键值
     * @param clear
     */
    public static int multiKeyCheck(int[] keys, boolean clear){
        int count = keys.length;
        
        for(int i = 0; i < count; i++){
            if(isKeyPressed(keys[i], clear)){
                return keys[i];
            }
        }
        
        return -1;
    }

    public static byte[] getBytesFromInput(InputStream in) throws IOException{
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int rd = 0;
        int len = 0;
        byte[] buf = new byte[64];

        while((rd = in.read(buf)) != -1){
            len += rd;
            out.write(buf, 0, rd);
        }

        byte[] rt = out.toByteArray();
        out.close();

        return rt;
    }

    private static final String punctation = ",.?:\"!;，。？：“”！；";

    public static String[] formatText(String text, int width, Font font){
        Vector vec = new Vector();
        int lineStart = 0;
        int lineWid = 0;
        int charCount = text.length();

        // Loop to break the text into lines.
        int i = 0;

        while(i < charCount){
            char ch = text.charAt(i);

            if(ch == '\n'){
                // If new line is found, record current line information and
                // step to next line.
                if(i > 0 && text.charAt(i - 1) == '\r'){
                    vec.addElement(text.substring(lineStart, i - 1));
                }else{
                    vec.addElement(text.substring(lineStart, i));
                }

                lineStart = i + 1;
                lineWid = 0;
            }else{
                //#if "${UseImageFont}" == "true"
                //# int charWid = GameCanvas.instance.iFont.charWidth(ch);
                //#else
                int charWid = font.charWidth(ch);
                //#endif

                if(lineWid == 0 || lineWid + charWid <= width){
                    // If current character is the first in current line, or
                    // it doesn't exceed the given width, just add it into
                    // current line.
                    lineWid += charWid;
                }else{
                    // If current character exceed the given width, record
                    // current line information and add current character into
                    // the next line.

                    // Don't put punctation at the head of line
                    if(punctation.indexOf(ch) >= 0){
                        i--;
                        //#if "${UseImageFont}" == "true"
                        //# charWid += GameCanvas.instance.iFont.charWidth(text.charAt(i));
                        //#else
                        charWid += font.charWidth(text.charAt(i));
                        //#endif
                    }

                    vec.addElement(text.substring(lineStart, i));
                    lineStart = i;
                    lineWid = charWid;
                }
            }

            i++;
        }

        // Handle the last line.
        if(lineWid > 0){
            vec.addElement(text.substring(lineStart));
        }

        // Construct return values.
        String[] ret = new String[vec.size()];
        vec.copyInto(ret);

        return ret;
    }
}
