package com.pip.sanguo;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;
//#if ModelID == Android || ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == AndroidSmall || ModelID == AndroidAuto
//# import com.pip.android.PipActivity;
//#endif
import com.pip.common.Tool;
import com.pip.common.Utilities;
import com.pip.engine.AnimateCache;
import com.pip.engine.AnimatePlayer;
import com.pip.engine.Weather;
import com.pip.gui.GWindow;
import com.pip.image.ImageSet;
import com.pip.io.UASegment;
import com.pip.io.UASocketConnection;
import com.pip.resource.ResourceAsynLoader;
import com.pip.resource.ResourceManager;
import com.pip.sanguo.GameSprite;
import com.pip.ui.DebugVM;
import com.pip.ui.Quest;
import com.pip.ui.VM;
import com.pip.ui.VMGame;
import com.pip.util.VMCounter;
//#if ModelID == Android || ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == AndroidSmall || ModelID == AndroidAuto
//# import android.view.WindowManager;
//#endif

//#ifdef polish.api.nokia-ui
//# import com.nokia.mid.ui.*;
//#endif

/**
 * 游戏主屏幕以及主线程。
 * 
 * @author lighthu
 */
public class GameMain extends
//#if CanvasType == FullCanvas
                //# FullCanvas
                //#elif CanvasType == GameCanvas
                GameCanvas
//#else
                //# Canvas
                //#endif
                implements Runnable{
    /** 屏幕宽度 */
    public static int viewWidth;
    /** 屏幕高度 */
    public static int viewHeight;
//#if ModelID == AndroidAuto || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidLarge || ModelID == Lenovo || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == Nokia5800
    public static final String ANDROID_SMALL = "AndroidSmall";
    public static final String ANDROID_LARGE = "AndroidLarge";	
    public static final String ANDROID_NORMAL = "Android";
//#endif    		 
    /** Singleton */
    public static GameMain instance;
    /** Singleton */
    public static Display display;
    /** 系统帧计数器 */
    public static int tick;
    /** 每帧理想时间 */
    public static final int MILLIS_PRE_UPDATE = 80;

    /** 平均cycle时间 */
    public static final int AVERAGE_PARA = 1000;
    
    //#if (ModelID == Nokia7370)
    //# public static final int AVERAGE_LIMIT = 4;
    //#else
    public static final int AVERAGE_LIMIT = 3;
    //#endif
    
    public static int averageMillis = MILLIS_PRE_UPDATE;
    public static int totalMillis = 0;
    public static int[] cycleMills = new int[AVERAGE_PARA];
    public static int millsPointer = 0;
    public static boolean clientMoving = false;

    /** 本帧是否需要重绘 */
    private static boolean needRepaint;

    /** 上次向服务器发送心跳时间 */
    public long reportAliveTime;
    /** 向服务器发送心跳的时间间隔(毫秒) */
    public static final long REPORT_ALIVE_INTERVAL = 15000;

    /** 当前处理的UWAP数据包 */
    public UASegment nextPacket;

    /**
     * 是否处于强制更新模式，是则暂停游戏cycle，更新完毕后返回主界面
     */
    public static boolean forceUpdating = false;

    /**
     * 是否需要重置客户端
     */
    private static boolean needReset = false;

    /**
     * 重置客户端类型
     * 0：完全重置
     * 1：重置直接到主菜单
     */
    public static int resetType = 0;
    
    /**
     * 动画缓冲方式
     * 0：缓冲整个关卡的动画
     * 1：缓冲本地图的动画
     * 2：不做强制缓冲，NPC刷没就释放动画
     */
    //#if (ModelID == Nokia7370)
    //# public static int animateCacheType = 1;
    //#else
    public static int animateCacheType = 0;
    //#endif

    /**
     * 上次cycle的时间
     */
    public static long lastCycleTime;

    /**
     * 上次paint的时间
     */
    public static long lastPaintTime;

    public static String systemError;

    public static AnimatePlayer[] clientAnimates; //0：灰问号，1：黄叹号，2：黄问号，3：蓝叹号，4：蓝问号，5：红选框，6：蓝选框，7：阴影，8：传送门，9：灰选框
    public static AnimatePlayer[] specialAnimates; //特殊动画

    public static ImageSet numberImage;
    public static Hashtable needCacheVm = new Hashtable();
    public static int[] battleRemind = null;
    public static Hashtable javaWorldPacket = new Hashtable();
    
    public static int numberImageIndex;
    public static int flyNumberIndex;
    public static int flyNumberBlockCount;
    public static int humanAnimateIndex;
    public static int autoSelectDistance;
    public static int forceSelectDistance;
    public static int lostSelectDistance;
    public static int positionDistance;
    public static int positionTime;
    public static int positionLimit;
    public static int dropNetplayerTime;
    public static int dropflyingStringTime;
    public static int spriteFlyingStringDelay;
    public static int spritePlayAnimateDelay;
    public static int followingNotifyServerTime;
    public static int battleModePositionTime;
    public static int keepGoingDistance;
    public static int followMaxDis;  //最大跟随距离
    public static int animatePendingTick;
    public static int spriteLeavingSpeed;
    public static int netplayerShowNameDistance;
    public static int imageScalePercent = 100;
    
    //#if SupportSound == true
    public static boolean backBoundSwitch; //背景音乐
    public static boolean actionBoundSwitch; //音效
    //#endif
    
    public static int netplayerShowMaxCount = 20;
    
    //#if Draw3DString == best
    public static int draw3DStringLevel = 0; //0:best, 1:medium, 2:simple, 3:none
    //#elif Draw3DString == medium
    //# public static int draw3DStringLevel = 1; //0:best, 1:medium, 2:simple, 3:none
    //#elif Draw3DString == simple
    //# public static int draw3DStringLevel = 2; //0:best, 1:medium, 2:simple, 3:none
    //#else
    //# public static int draw3DStringLevel = 3; //0:best, 1:medium, 2:simple, 3:none
    //#endif
    
    public static int drawStringMoveDir;
    public static int iconOffset;

    //淡入淡出效果
    public static boolean fadeEffect = false;
    public static int[] fadeData = null;
    public static int fadeColor;
    public static int fadeStartAlpha;
    public static int fadeEndAlpha;
    public static int fadeAlphaStep;
    public static int fadeMaxCount;
    public static int fadeCurrentAlpha;
    public static int fadeCurrentCount;
    
    //震动效果
    public static boolean vibraEffect = false;
    public static int[] vibraData = null;
    public static int vibraTick;
    public static int vibraMaxCount;
    public static int vibraCurrentIndex;
    public static int vibraCurrentCount;
    
    //天气效果
    //#if SupportWeather == true
    public static Weather weather = null;
    //#endif
    
    public static GameWorld world;
    public static ResourceManager resourceManager = new ResourceManager();
    public static ResourceAsynLoader resourceAsynLoader = new ResourceAsynLoader();

    public static boolean initializing = false;
    
    public static int COLLISION_MAX_STEP = 10;
    public static int COLLISION_STEP_ADD = 1;
   
    //#if OptimizeMapDataBuffer == true
    //# public static boolean MAP_DATA_BUFFER_OPTIMIZE = true;
    //#else
    public static boolean MAP_DATA_BUFFER_OPTIMIZE = false;
    //#endif
	
    //#if TouchScreen == true
    static int pointPressedFlag;
    static int pointReleasedFlag;
    static int pointDraggedFlag;
    //#endif
    //#if ModelID == Android || ModelID == AndroidAuto || ModelID == AndroidLarge
    //# static int pointPressX;
    //# static int pointPressY;
    //# public static boolean PressedFlag;
    //# public static boolean ReleasedFlag;
    //# public static boolean DraggedFlag;
    //# static int pointDragX;
    //# static int pointDragY;
    //#endif
    public static long lastSyncTime = 0;
        
    public GameMain(Display dp){
        //#if CanvasType == GameCanvas
        super(false);
        setFullScreenMode(true);
        //#endif

        display = dp;
        instance = this;

        //#ifdef ScreenWidth
        //# viewWidth = ${ScreenWidth};
        //#else
        viewWidth = getWidth();
        //#endif

        //#ifdef ScreenHeight
        //# viewHeight = ${ScreenHeight};
        //#else
        viewHeight = getHeight();
        //#endif

        if(viewWidth == 320){
            if(viewHeight < 200){
                viewHeight = 240;
            }
        }else if(viewWidth == 240){
            if(viewHeight < 280){
                viewHeight = 320;
            }
        }else if(viewWidth == 176){
            if(viewHeight < 192){
                viewHeight = 208;
            }
        }
        
        //#if ScreenCanReset == true
        GWindow.uiMaxWidth = viewWidth;
        GWindow.uiMaxHeight = viewHeight;
        //#endif

        Tool.setGlobalValue("varDebugModel", new Integer(VM.FALSE));
        init();
    }

    public static void clear(){
        resourceAsynLoader.clear();
        resourceManager.clear();
        if(resetType != 3) {
	        AnimateCache.clear();
	        VMGame.clear();
        } else {
        	AnimateCache.clear();
        	VMGame.clearExclude("ui_update");
        }
        GameWorld.clear();
        Quest.clear();

        Utilities.clearKeyStates();
        //如果是更新状态，则不断开网络
        if(resetType != 3) {
        	Utilities.closeConnection();	
        }        
    }

    public static void resetClient(int type){
        needReset = true;
        resetType = type;
    }

    public static void reset(){
        needReset = false;
        GameMain.clear();
        GameMain.init();
    }

    public static void setUpdating(boolean updating){
        forceUpdating = updating;
    }
	public static boolean underDev = false;
    public static void init(){
        try{
            initializing = true;

            Tool.setGlobalValue("varModel", GameMain.getModel());
            Tool.setGlobalValue("varVersion", GameMain.getClientVersion());
            Tool.setGlobalValue("varUIModel", GameMain.getUIModel());
            Tool.setGlobalValue("varJVMCode", GameMain.getJVMCode());
            Tool.setGlobalValue("varChannelCode", GameMain.getChannelCode());
         
            //#if ModelID == AndroidAuto
            //# if (GameMain.getUIModel().equals(GameMain.ANDROID_LARGE))
        	//# {
            //# Font.SIZE_SMALL_FONT = 30;
            //# Font.SIZE_MEDIUM_FONT = 36;
            //# Font.SIZE_LARGE_FONT = 48;
            //# GameSprite.DEFAULT_SPEED = 90;
            //# }
            //# else
            //# {
        	//# Font.SIZE_SMALL_FONT = 14;
            //# Font.SIZE_MEDIUM_FONT = 16;
            //# Font.SIZE_LARGE_FONT = 18;
            //# GameSprite.DEFAULT_SPEED = 45;
            //# }
            //#endif
       
            
            
            //#debug
            Tool.setGlobalValue("varRevision", "PiP");
            //#= Tool.setGlobalValue("varRevision", "${Revision}");
            //#debug
            Tool.setGlobalValue("varRestrictIP", 0);
            //#= Tool.setGlobalValue("varRestrictIP", ${RestrictIP});

            totalMillis = 0;
            tick = 0;

            for(int i = 0; i < cycleMills.length; i++){
                cycleMills[i] = MILLIS_PRE_UPDATE;
                totalMillis += MILLIS_PRE_UPDATE;
            }

            resourceManager.loadResourceInfo();
//            if( System.getProperty("debug")!=null){
//            	VMGame.loadVMGame("dev_game_init", VMGame.VM_TYPE_UI, true);
//            	underDev = true;
//            }else{
            	VMGame.loadVMGame("game_init", VMGame.VM_TYPE_UI, true);
            	VMGame.setToTop("ui_update");
//            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static String getChannelCode(){
        //#ifdef buildtest
        return "CCCCCPiP";
        //#elif ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidAuto
        //# return System.getProperty("ChannelCode");
        //#else
        //#= return "${ChannelCode}";
        //#endif
    }

    /** 取得UI脚本的机型配置 */
    public static String getUIModel(){
        //#ifdef buildtest
        return "Midp2Touch";
        //#elif ModelID == AndroidAuto
        //# if (viewWidth >= 240 && viewWidth < 320 && viewHeight >= 320 && viewHeight < 450 || viewWidth >= 320 && viewWidth < 450 && viewHeight >= 240 && viewHeight < 320)
    	//# {
    	//#	 return "AndroidSmall";
    	//# }
    	//# else if(viewWidth >= 450 && viewWidth < 1000 && viewHeight >= 320 && viewHeight < 480|| viewWidth >= 320 && viewWidth < 480 && viewHeight >= 450 && viewHeight < 1000)
    	//# {
    	//#	return "Android";
    	//# }
    	//# else
    	//# {
    	//#	return "AndroidLarge";
    	//# }
        //#else
        //#= return "${UIModel}";
        //#endif
    }

    /** 取得手机的机型配置 */
    public static String getModel(){
        //#ifdef buildtest
        return "GenericMidp2";
        //#elif ModelID == AndroidAuto
    		 //# if (viewWidth == 240 && viewHeight == 320 || viewWidth == 320 && viewHeight == 240)
    		 //# {
    		 //# return "AndroidSmall";
    		 //# }
    		 //# else if(viewWidth == 480 && viewHeight == 320 || viewWidth == 320 && viewHeight == 480)
    		 //# {
    		 //# return "Android";
    		 //# }
    		 //# else
    		 //# {
    		 //# return "AndroidLarge";
    		 //# }
        //#else
        //#= return "${ModelID}";
        //#endif
    }

    public static String getJVMCode(){
        String jvmCode = System.getProperty("microedition.platform");

        if(jvmCode == null){
            jvmCode = GameMain.getModel();
        }

        return jvmCode;
    }

    public static String getClientVersion(){
        String ver = SanguoMIDlet.instance.getAppProperty("MIDlet-Version");
        if (ver == null) {
            //#ifdef buildtest
            return "0.0.1";
            //#else
            //#= return "${Version}";
            //#endif
        } else {
            return ver;
        }
    }

    /**
     * 绘图方法。
     */
    public void paint(Graphics g){
        Utilities.graphics = g;
        g.setFont(Utilities.font);

        try{
        	//#if ModelID == LenovoU1
            g.setColor(0x00000000);
            //#else
            g.setColor(0x606060);
            //#endif
            g.fillRect(0, 0, viewWidth, viewHeight);

            boolean forcePaintWorld = false;
            //#if ScreenCanReset == false
            forcePaintWorld = VMGame.isAllTransparent(false);
            //#elif ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == Android
            //# forcePaintWorld = VMGame.isAllTransparent(false);
        	//#elif ModelID == AndroidAuto
        	//# if (!GameMain.getUIModel().equals(GameMain.ANDROID_SMALL))
        	//# {
            //# forcePaintWorld = VMGame.isAllTransparent(false);
            //# }
            //# else
            //# {
            //# if(GWindow.forcePaintWorld) {
            //# 	forcePaintWorld = true;
            //#  }
            //# }
            //#else
            //# if(GWindow.forcePaintWorld) {
            //# 	forcePaintWorld = true;
            //# }
            //#endif
            if(forcePaintWorld && !initializing){
                if(GameMain.MAP_DATA_BUFFER_OPTIMIZE && GameWorld.gameView != null){
                    GameWorld.gameView.rebuildMapDataBuffer();
                }
                
                world.draw(g,0,99);
            }else{
                if(GameMain.MAP_DATA_BUFFER_OPTIMIZE && GameWorld.gameView != null){
                    GameWorld.gameView.releaseMapDataBuffer();
                }                
            }

            g.setClip(0, 0, viewWidth, viewHeight);
            
            //#if SupportWeather == true
            if(weather != null){
                weather.draw(g);
            }
            //#endif
            
            VMGame.drawAll(g,0,99);
            
            if(GameWorld.panel != null){
            	GameWorld.panel.draw(g, 100, 999);
            }

            g.setClip(0, 0, viewWidth, viewHeight);
            
            if(battleRemind != null){
                if(!forcePaintWorld && battleRemind[5] != 0){
                    g.setColor(battleRemind[battleRemind[6] % 2]);
                    
                    for(int i = 0; i < battleRemind[2]; i++){
                        g.drawRect(i, i, GameMain.viewWidth - i * 2 - 1, GameMain.viewHeight - i * 2 - 1);
                    }
                    
                    if(tick % battleRemind[3] == 0){
                        battleRemind[6]++;
                        
                        if(battleRemind[6] > battleRemind[4]){
                            battleRemind[5] = 0;
                        }
                    }
                }else{
                    battleRemind[5] = 0;
                    battleRemind[6] = 0;
                }
            }
            //#if ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == Android	 
            //#  if(battleRemind[5] == 0)
            //#  {
            //#  	GWindow.forcePaintWorld = true;
            //#  }
            //#elif ModelID == AndroidAuto
        	//# if (!GameMain.getUIModel().equals(GameMain.ANDROID_SMALL))
        	//# {
            //#  if(battleRemind[5] == 0)
            //#  {
            //#  	GWindow.forcePaintWorld = true;
            //#  }
            //# }
            //#endif
            if(fadeEffect){
                drawFadeEffect(g);
            }
        }catch(Throwable e){
            systemError = e.toString();
          //#ifdef buildtest
            e.printStackTrace();
          //#endif
        }finally{
            try{
                if(debugMode){
                    g.setClip(0, 0, viewWidth, viewHeight);
                    
                    /*
                    if(path != null && GameWorld.gameView != null){
                        g.setColor(0xFF0000);
        
                        for(int i = 0; i < path.length - 1; i++){
                            int x1 = path[i][0] * GameWorld.gameView.pathTileWidth - GameWorld.viewX;
                            int y1 = path[i][1] * GameWorld.gameView.pathTileHeight - GameWorld.viewY;
                            int x2 = path[i + 1][0] * GameWorld.gameView.pathTileWidth - GameWorld.viewX;
                            int y2 = path[i + 1][1] * GameWorld.gameView.pathTileHeight - GameWorld.viewY;
        
                            g.drawLine(x1, y1, x2, y2);
                            g.fillArc(x1 - 2, y1 - 2, 4, 4, 0, 360);
                            g.fillArc(x2 - 2, y2 - 2, 4, 4, 0, 360);
                        }
                    }*/
                                
                    if(systemError != null){
                        Tool.draw3DString(g, systemError, 10, viewHeight - Utilities.CHAR_HEIGHT * 10, 0xFFFFFF, 0x000000, Graphics.TOP | Graphics.LEFT);
                    }
        
                    Tool.draw3DString(g, " K: " + makeTimes, 10, viewHeight - Utilities.CHAR_HEIGHT * 9, 0xFFFFFF, 0x000000, Graphics.TOP | Graphics.LEFT);
                    makeTimes = 0;
                    Tool.draw3DString(g, " D: " + drawTimes, 10, viewHeight - Utilities.CHAR_HEIGHT * 8, 0xFFFFFF, 0x000000, Graphics.TOP | Graphics.LEFT);
                    drawTimes = 0;
                  //#ifdef buildtest
                    Tool.draw3DString(g, " E: " + DebugVM.getExecCount(), 10, viewHeight - Utilities.CHAR_HEIGHT * 7, 0xFFFFFF, 0x000000, Graphics.TOP | Graphics.LEFT);
                    Tool.draw3DString(g, " V: " + DebugVM.getDynamicHeapSize(), 10, viewHeight - Utilities.CHAR_HEIGHT * 6, 0xFFFFFF, 0x000000, Graphics.TOP | Graphics.LEFT);
                    //#endif
                    Tool.draw3DString(g, " N: " + GameWorld.gameSprites.size(), 10, viewHeight - Utilities.CHAR_HEIGHT * 5, 0xFFFFFF, 0x000000, Graphics.TOP | Graphics.LEFT);
                    Tool.draw3DString(g, " P: " + lastPaintTime, 10, viewHeight - Utilities.CHAR_HEIGHT * 4, 0xFFFFFF, 0x000000, Graphics.TOP | Graphics.LEFT);
                    Tool.draw3DString(g, " C: " + lastCycleTime, 10, viewHeight - Utilities.CHAR_HEIGHT * 3, 0xFFFFFF, 0x000000, Graphics.TOP | Graphics.LEFT);
                    long m = Runtime.getRuntime().freeMemory() / 1024;
                    Tool.draw3DString(g, " M: " + m + "K", 10, viewHeight - Utilities.CHAR_HEIGHT * 2, 0xFFFFFF, 0x000000, Graphics.TOP | Graphics.LEFT);
                    
                    /*
                    //输出发送数据量排序
                    if(tick % 10 == 0){
                        System.out.println(" ");
                    }
                    
                    Vector sendStat = new Vector();
                    Enumeration enuSend = UASocketConnection.sendStat.keys();
                    int allSendCount = 0;
                    int allSendSize = 0;
                    int sendHeight = 10;
                    while(enuSend.hasMoreElements()){
                        Integer sendKey = (Integer)enuSend.nextElement();
                        int[] sendSize = (int[])UASocketConnection.sendStat.get(sendKey);
                        allSendCount += sendSize[0];
                        allSendSize += sendSize[1];
                        int idx = 0;
                        for(int i = 0; i < sendStat.size(); i++){
                            idx = i;
                            
                            int[] sendData = (int[])sendStat.elementAt(i);
                            if(sendData[1] < sendSize[0]){
                                break;
                            }
                        }
                        sendStat.insertElementAt(new int[]{
                                        sendKey.intValue(), sendSize[0], sendSize[1]
                        }, idx);
                    }
                    sendStat.insertElementAt(new int[]{
                                    -999, allSendCount, allSendSize
                    }, 0);
                    for(int i = 0; i < sendStat.size(); i++){
                        int[] sendData = (int[])sendStat.elementAt(i);
                        String str = sendData[0] + "(" + sendData[1] + ") : ";
                        if(sendData[2] > 10240){
                            str += (sendData[2] / 1024) + "." + ((sendData[2] % 1024) * 10 / 1024) + "K";
                        }else{
                            str += sendData[2] + "B";
                        }
                        
                        if(i < 2){
                            Tool.draw3DString(g, str, viewWidth - Utilities.font.stringWidth(str), 10 + Utilities.CHAR_HEIGHT * i, 0xFFFFFF, 0x000000, Graphics.TOP | Graphics.LEFT);
                            sendHeight += Utilities.CHAR_HEIGHT;
                        }
                        
                        
                        if(tick % 10 == 0){
                            System.out.println("Send : " + str);
                        }
                    }
                    
                    //输出接收数据量排序
                    Vector recvStat = new Vector();
                    Enumeration enuRecv = UASocketConnection.recvStat.keys();
                    int allRecvCount = 0;
                    int allRecvSize = 0;
                    sendHeight += Utilities.CHAR_HEIGHT;
                    while(enuRecv.hasMoreElements()){
                        Integer recvKey = (Integer)enuRecv.nextElement();
                        int[] recvSize = (int[])UASocketConnection.recvStat.get(recvKey);
                        allRecvCount += recvSize[0];
                        allRecvSize += recvSize[1];
                        int idx = 0;
                        for(int i = 0; i < recvStat.size(); i++){
                            idx = i;
                            
                            int[] recvData = (int[])recvStat.elementAt(i);
                            if(recvData[1] < recvSize[0]){
                                break;
                            }
                        }
                        recvStat.insertElementAt(new int[]{
                                        recvKey.intValue(), recvSize[0], recvSize[1]
                        }, idx);
                    }
                    recvStat.insertElementAt(new int[]{
                                    -999, allRecvCount, allRecvSize
                    }, 0);
                    for(int i = 0; i < recvStat.size(); i++){
                        int[] recvData = (int[])recvStat.elementAt(i);
                        String str = recvData[0] + "(" + recvData[1] + ") : ";
                        if(recvData[2] > 10240){
                            str += (recvData[2] / 1024) + "." + ((recvData[2] % 1024) * 10 / 1024) + "K";
                        }else{
                            str += recvData[2] + "B";
                        }
                        
                        if(i < 10){
                            Tool.draw3DString(g, str, viewWidth - Utilities.font.stringWidth(str), sendHeight + Utilities.CHAR_HEIGHT * i, 0xFFFFFF, 0x000000, Graphics.TOP | Graphics.LEFT);
                        }
                        
                        if(tick % 10 == 0){
                            System.out.println("Recv : " + str);
                        }
                    }*/
                }else{
                    systemError = null;
                }
            }catch(Throwable e){
            	//#ifdef buildtest
                e.printStackTrace();
              //#endif
            }
        }
    }

    //TODO test
    public static int drawTimes = 0;
    public static int makeTimes = 0;
    public static short[][] path = null;

    /**
     * 游戏主线程，循环调用cycle和paint。
     */
    public void run(){
        long startTime = System.currentTimeMillis();

        while(SanguoMIDlet.isRun){
            SanguoMIDlet.isRun = !Utilities.isExitGame;

            try{
                clientMoving = false;

                Utilities.keyFlag2 = Utilities.keyFlag;
                Utilities.keyFlag &= 0xAAAAAAAAAAAAAAAAL;
                startTime = System.currentTimeMillis();
                needRepaint = true;
                tick++;

                if(needReset){
                    reset();
                    continue;
                }

                cycle();

                lastCycleTime = System.currentTimeMillis() - startTime;

                long paintTime = System.currentTimeMillis();

                if(isShown() && needRepaint){
                    repaint();
                    serviceRepaints();
                }

                lastPaintTime = System.currentTimeMillis() - paintTime;
            }catch(Throwable ex){
                systemError = ex.toString();
              //#ifdef buildtest
                ex.printStackTrace();
              //#endif
            }finally{
                long t = System.currentTimeMillis() - startTime;
                long st = (t < MILLIS_PRE_UPDATE)? (MILLIS_PRE_UPDATE - t): 1;

                try{
                    long sp = System.currentTimeMillis();
                    long sp2;
                    while((sp2 = System.currentTimeMillis()) - sp < st){
                        Thread.sleep(st + sp - sp2);
                    }
                }catch(InterruptedException e){
                }

                if(clientMoving){
                    calculateAverageMills((int) (System.currentTimeMillis() - startTime));
                }
            }
        }

        SanguoMIDlet.exit();
    }

    private static void calculateAverageMills(int mills){
        if(mills > MILLIS_PRE_UPDATE * AVERAGE_LIMIT  || mills < MILLIS_PRE_UPDATE){
            return;
        }

        cycleMills[millsPointer] = mills;
        totalMillis += cycleMills[millsPointer];
        millsPointer++;

        if(millsPointer >= AVERAGE_PARA){
            millsPointer = 0;
        }

        totalMillis -= cycleMills[millsPointer];
        averageMillis = totalMillis / AVERAGE_PARA;
        
        if(averageMillis > MILLIS_PRE_UPDATE * (AVERAGE_LIMIT - 1)){
            averageMillis = MILLIS_PRE_UPDATE * (AVERAGE_LIMIT - 1);
        }
    }

    /**
     * 游戏主循环。
     */
    public void cycle(){
        try{
            long now = System.currentTimeMillis();

            // 维持和服务器的心跳
            if(Utilities.connection != null && now > reportAliveTime + REPORT_ALIVE_INTERVAL){
            	UASegment seg = new UASegment(Tool.CONN_SYNC_CLIENT);
                seg.writeInt(Utilities.getServerTime());
                Utilities.sendRequest(seg);
                reportAliveTime = now;
            }

            //检测屏幕大小变化
            //#if ScreenCanReset == true && ModelID != Lenovo && ModelID != AndroidLarge && ModelID != LenovoU1 && ModelID != IPhone4 && ModelID != IPad && ModelID != Android && ModelID != AndroidSmall
            int _viewWidth = getWidth();
            int _viewHeight = getHeight();
            if(viewWidth != _viewWidth || viewHeight != _viewHeight) {
            	viewWidth = _viewWidth;
            	viewHeight = _viewHeight;
            	//#if AlphaMethod == rgbimage
            	Tool.clearAlphaImageMap();
            	//#endif
            	if(GameWorld.gameView != null) {
            		GameWorld.gameView.rebuildImageBuffer();            		
            	}
        		if(GameWorld.instance != null && GameWorld.instance.vm != null) {
                    synchronized(GameWorld.instance.vm){
                        int[] params = new int[]{viewWidth, viewHeight};
                
                        GameWorld.instance.vm.callback(VMGame.CALLBACK_SCREEN_SIZE_CHANGED, params);
                    }
        		}
          		VMGame gameInit = VMGame.getVMGame("game_init");
        		if(gameInit != null) {
        			synchronized(gameInit.getVM()){
                        int[] params = new int[]{viewWidth, viewHeight};
                
                        gameInit.getVM().callback(VMGame.CALLBACK_SCREEN_SIZE_CHANGED, params);
                    }
        		}
          		VMGame update = VMGame.getVMGame("ui_update");
        		if(update != null) {
        			synchronized(update.getVM()){
                        int[] params = new int[]{viewWidth, viewHeight};
                
                        update.getVM().callback(VMGame.CALLBACK_SCREEN_SIZE_CHANGED, params);
                    }
        		}
            }
            //#endif
            
            resourceManager.cycle();

            // 处理网络包

            if(!initializing){
                cycleSegments();
            }

            VMGame.cycle(); //控制机处理
            VMCounter.cycle(); //计时器处理

            if(forceUpdating == false && !initializing){
                world.cycle();
                GameWorld.moveMap();

                //处理动画下载完成队列
                AnimateCache.processAnimateReadyQueue();
            }
            
            if(clientAnimates != null){
                // 客户端动画
                for(int i = 0; i < clientAnimates.length; i++){
                    clientAnimates[i].cycle();
                }
            }
            
            if(fadeEffect){
                cycleFadeEffect();
            }
            
            if(vibraEffect){
                cycleVibraEffect();
            }
            
            //#if SupportWeather == true
            if(weather != null){
                weather.cycle();
            }
            //#endif
            
            //长按*键处理
            if(GameMain.starPressing){
            	GameMain.longPressedTick++;
            	if(longPressedTick > 6){
            		if(needSelectNearestNPC == false){
            			needSelectNearestNPC = true;
            			if(GameWorld.panel != null && GameWorld.panel.state == GamePanel.GAME_PANEL_STATE_SHOW){
                			if(GameWorld.player != null){
                        		GameWorld.player.searchNearestNpc();
                        	}
                		}
            		}
        		}
            }

          //#if TouchScreen == true
            int x = 0; int y = 0;
        	if(pointPressedFlag >> 31 == 0) {
        		int data = pointPressedFlag & 0x3FFFFFFF;
        		x = data & 0x7FFF;
        		y = data >> 15;
                int[] panelPointItem = GamePanel.getPointItem(x, y);
                
                if(GameWorld.instance != null && GameWorld.instance.vm != null) {
                	GameWorld.instance.sendCommand(VMGame.GAME_COMMAND_WORLD_POINT_PRESSED, new int[]{
                			x, y, GameWorld.viewX, GameWorld.viewY, panelPointItem[0], panelPointItem[1], panelPointItem[2], panelPointItem[3], panelPointItem[4], panelPointItem[5], panelPointItem[6]
                	});
                }        

                oldPointerX = x;
                oldPointerY = y;
                //#if ModelID == Android || ModelID == AndroidLarge
                //# SetPressedFlag(true);
                //# SetDraggedFlag(false);
                //#elif ModelID == AndroidAuto
                //# if (GameMain.getUIModel().equals(GameMain.ANDROID_NORMAL) || GameMain.getUIModel().equals(GameMain.ANDROID_LARGE))
            	//# {
                //# SetPressedFlag(true);
                //# SetDraggedFlag(false);
                //# }
                //#endif
                pointPressedFlag = 1 << 31;
                //#if ModelID == Android || ModelID == AndroidLarge
                //# SetPointPressX(x);
                //# SetPointPressY(y);
                //#elif ModelID == AndroidAuto
                //# if (GameMain.getUIModel().equals(GameMain.ANDROID_NORMAL) ||GameMain.getUIModel().equals(GameMain.ANDROID_LARGE) )
            	//# {
                //# SetPointPressX(x);
                //# SetPointPressY(y);
                //# }
                //#endif
        	}
        	        	
        	if(pointDraggedFlag >> 31 == 0) {
        		int data = pointDraggedFlag & 0x3FFFFFFF;
        		x = data & 0x7FFF;
        		y = data >> 15;
                int[] panelPointItem = GamePanel.getPointItem(x, y);
                
            	if(GameWorld.instance != null && GameWorld.instance.vm != null) {
        			GameWorld.instance.sendCommand(VMGame.GAME_COMMAND_WORLD_POINT_DRAGGED, new int[]{
        					x, y, GameWorld.viewX, GameWorld.viewY, panelPointItem[0], panelPointItem[1], panelPointItem[2], panelPointItem[3], panelPointItem[4], panelPointItem[5], panelPointItem[6]
        			});
            	}
            	pointDraggedFlag = 1 << 31;
            	//#if ModelID == Android || ModelID == AndroidLarge
            	//# SetDraggedFlag(true);
            	//# SetPointDragX(x);
            	//# SetPointDragY(y);
            	//#elif ModelID == AndroidAuto
                //# if (GameMain.getUIModel().equals(GameMain.ANDROID_NORMAL) || GameMain.getUIModel().equals(GameMain.ANDROID_LARGE))
            	//# {
            	//# SetDraggedFlag(true);
            	//# SetPointDragX(x);
            	//# SetPointDragY(y);
            	//# }
            	//#endif
        	}       	
        
        	if(pointReleasedFlag >> 31 == 0) {
        		int data = pointReleasedFlag & 0x3FFFFFFF;
        		x = data & 0x7FFF;
        		y = data >> 15;
                int[] panelPointItem = GamePanel.getPointItem(x, y);
                
            	if(GameWorld.instance != null && GameWorld.instance.vm != null) {
            		 GameWorld.instance.sendCommand(VMGame.GAME_COMMAND_WORLD_POINT_RELEASED, new int[]{
            				 x, y, GameWorld.viewX, GameWorld.viewY, panelPointItem[0], panelPointItem[1], panelPointItem[2], panelPointItem[3], panelPointItem[4], panelPointItem[5], panelPointItem[6]
            		 });
            	}
            	pointReleasedFlag = 1 << 31;
            	//#if ModelID == Android || ModelID == AndroidLarge
            	//# SetReleasedFlag(true);
            	//#elif ModelID == AndroidAuto
                //# if (GameMain.getUIModel().equals(GameMain.ANDROID_NORMAL) || GameMain.getUIModel().equals(GameMain.ANDROID_LARGE))
            	//# {
            	//# SetReleasedFlag(true);
            	//# }
            	//#endif
        	}
        	
            //#endif
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 处理收到的所有网络包。
     */
    public void cycleSegments(){
        UASegment[] segs;

        synchronized(Utilities.segments){
            segs = new UASegment[Utilities.segments.size()];
            Utilities.segments.copyInto(segs);
            Utilities.segments.removeAllElements();
        }

        int len = segs.length;

        for(int i = 0; i < len; i++){
            UASegment segment = segs[i];
            segs[i] = null;
            try {
                handleSegment(segment);
            } catch (Exception e) {
            	//#ifdef buildtest 
                e.printStackTrace();
              //#endif
            }
        }
    }

    /**
     * 处理一个网络包。
     * @param segment
     */
    public void handleSegment(UASegment segment){
        if(segment.type == Tool.CONN_SYNC_SERVER){
            int ctime = segment.readInt();
            int stime = segment.readInt();
            Utilities.setServerTime(stime);
            long feedbackTime = System.currentTimeMillis() - this.reportAliveTime;
            if(feedbackTime < 1000){
                GameView.currNetColor = GameView.MINI_MAP_NET_COLOR_FAST;
            }else if(feedbackTime < 2000){
            	GameView.currNetColor = GameView.MINI_MAP_NET_COLOR_NORMAL;
            }else if(feedbackTime < 4000){
            	GameView.currNetColor = GameView.MINI_MAP_NET_COLOR_SLOW;
            }else{
            	GameView.currNetColor = GameView.MINI_MAP_NET_COLOR_BAD;
            }
          //#ifdef buildtest
            System.out.println("Time gap : " + (stime - ctime));
          //#endif
            
            return;
        }

        nextPacket = segment;

        try{
            if(javaWorldPacket.containsKey(new Integer(segment.type))){
                world.processPacket();
            }else{
                VMGame.handleSegment(segment);
    
                if(!segment.handled){
                    segment.reset();
                    world.processPacket();
                }
            }
        }finally{
            nextPacket = null;
        }
    }

    public static final int[] debugKeyOrder = {
        Utilities.KEY_RIGHT, Utilities.KEY_LEFT, Utilities.KEY_DOWN, Utilities.KEY_UP, Utilities.KEY_NUM1, Utilities.KEY_NUM2, Utilities.KEY_NUM3
    };
    
    public static int debugTestIndex = 0;
    public static boolean debugMode = false;
    
    public static boolean starPressing = false;
    public static int longPressedTick = 0;
    public static boolean needSelectNearestNPC = false;
    
    // 按键处理
    protected void keyPressed(int keyCode){
    	if(keyCode == Utilities.KEY_STAR){
    		if(starPressing == false){
    			longPressedTick = 0;
    			starPressing = true;
    		}
    		return;
    	}
        Utilities.keyPressed(keyCode);
//#if ModelID == Android || ModelID == AndroidLarge
        //# Tool.setGlobalValue("InsideDirRectKey", 0);  // axq add
//#elif ModelID == AndroidAuto
    	//# if (GameMain.getUIModel().equals(GameMain.ANDROID_NORMAL)||GameMain.getUIModel().equals(GameMain.ANDROID_LARGE))
    	//# {
        //# Tool.setGlobalValue("InsideDirRectKey", 0);
        //# }
//#endif 
    }

    protected void keyReleased(int keyCode){
    	if(keyCode == Utilities.KEY_STAR){
    		starPressing = false;
    		if(needSelectNearestNPC == true){
    			needSelectNearestNPC = false;
    		} else {
    			Utilities.keyPressed(keyCode);
        		Utilities.keyReleased(keyCode);
    		}
    	}
        Utilities.keyReleased(keyCode);
    }

    protected void hideNotify(){
    }

    protected void showNotify(){
    }

    //TODO delete
    private static int oldPointerX = -1;
    private static int oldPointerY = -1;
    
    protected void pointerPressed(int x, int y){
        /*
        if(debugMode && Math.abs(oldPointerX - x) < 2 && Math.abs(oldPointerY - y) < 2){
            if(GameWorld.player != null && VMGame.isAllTransparent(true)){
                GameWorld.player.sprite.setPosition(x + GameWorld.viewX, y + GameWorld.viewY);
                GameWorld.player.clearChase();
            }
        }*/

        //#if TouchScreen == true
        pointPressedFlag = x;
        pointPressedFlag |= (y << 15);
        //#endif
    }
    
    protected void pointerReleased(int x, int y){
        //#if TouchScreen == true
    	pointReleasedFlag = x;
    	pointReleasedFlag |= (y << 15);
    	//pointPressedFlag = 1 << 31;
    	//pointDraggedFlag = 1 << 31;
        //#endif
    }
    
    protected void pointerDragged(int x, int y) {
        //#if TouchScreen == true
    	pointDraggedFlag = x;
    	pointDraggedFlag |= (y << 15);
    	//#endif
    }
    
    public static void drawFadeEffect(Graphics g){
        g.drawRGB(fadeData, 0, viewWidth, 0, 0, viewWidth, viewHeight, true);
    }
    
    public static void cycleFadeEffect(){
        int count = fadeData.length;
        int curColor = fadeColor | (fadeCurrentAlpha << 24);
        
        for(int i = 0; i < count; i++){
            fadeData[i] = curColor;
        }
        
        boolean inc = true;
        
        if(fadeStartAlpha > fadeEndAlpha){
            inc = false;
        }
        
        if(inc){
            fadeCurrentAlpha += fadeAlphaStep;
        }else{
            fadeCurrentAlpha -= fadeAlphaStep;
        }
        
        if((inc && fadeCurrentAlpha > fadeEndAlpha) || (!inc && fadeCurrentAlpha < fadeEndAlpha)){
            fadeCurrentCount++;
            
            if(fadeCurrentCount >= fadeMaxCount){
                fadeEffect = false;
                fadeData = null;
            }else{
                fadeCurrentAlpha = fadeStartAlpha;
            }
        }
    }
    
    public static void cycleVibraEffect(){
        if(tick % vibraTick == 0){
            int dataCount = vibraData.length >> 1;
            
            if(GameWorld.gameView != null){
                GameWorld.gameView.vx = vibraData[vibraCurrentIndex << 1];
                GameWorld.gameView.vy = vibraData[(vibraCurrentIndex << 1) + 1];
            }
            
            vibraCurrentIndex++;
            
            if(vibraCurrentIndex >= dataCount){
                vibraCurrentCount++;
                
                if(vibraCurrentCount >= vibraMaxCount){
                    vibraEffect = false;
                    vibraData = null;
                }else{
                    vibraCurrentIndex = 0;
                }
            }
        }
    }
    //#if ModelID == Android || ModelID == AndroidAuto || ModelID == AndroidLarge
    //# public static void SetPointPressX(int x){
    //# 	pointPressX = x;
    //# }
    //# public static void SetPointPressY(int y){
    //# 	pointPressY = y;
    //# }
    //# public static int GetPointPressX(){
    //# 	return pointPressX;
    //# }
    //# public static int GetPointPressY(){
    //# 	return pointPressY;
    //# }
    //# public static void SetPointDragX(int x){
    //# 	pointDragX = x;
    //# }
    //# public static void SetPointDragY(int y){
    //# 	pointDragY = y;
    //# }
    //# public static int GetPointDragX(){
    //# 	return pointDragX;
    //# }
    //# public static int GetPointDragY(){
    //# 	return pointDragY;
    //# }
    //# public static int GetPointPressFlag(){
    //# 	return pointPressedFlag;
    //# }
    //# public static int GetPointReleaseFlag(){
    //# 	return pointReleasedFlag;
    //# }
    //# public static int GetPointDragFlag(){
    //# 	return pointDraggedFlag;
    //# }
    //# public static void SetPointPressFlag(int flag){
    //# 	pointPressedFlag = flag;
    //# }
    //# public static void SetPointReleaseFlag(int flag){
    //# 	pointReleasedFlag = flag;
    //# }
    //# public static void SetPointDragFlag(int flag){
    //# 	pointDraggedFlag = flag;
    //# }
    //# public static void SetPressedFlag(boolean bPressedFlag){
    //# 	PressedFlag = bPressedFlag;
    //# }
    //# public static void SetReleasedFlag(boolean bReleasedFlag){
    //# 	ReleasedFlag = bReleasedFlag;
    //# }
    //# public static void SetDraggedFlag(boolean bDraggedFlag){
    //# 	DraggedFlag = bDraggedFlag;
    //# }
    //# public static boolean GetPressedFlag(){
    //# 	return PressedFlag;
    //# }
    //# public static boolean GetReleasedFlag(){
    //# 	return ReleasedFlag;
    //# }
    //# public static boolean GetDraggedFlag(){
    //# 	return DraggedFlag;
    //# }
    //#endif
}