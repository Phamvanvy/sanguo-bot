package com.pip.sanguo;


import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import com.pip.common.Utilities;
import com.pip.ui.VMGame;
//#if NewUI2
//# import com.pip.android.LogoCanvas;
//#endif

public class SanguoMIDlet extends MIDlet{
    public static SanguoMIDlet instance;
    public static boolean isRun;
    public static Display display;
    GameMain gameMain;
	//#if NewUI2
    //# LogoCanvas logoCanvas;
    //#endif
    public static Thread mainThread;
    //#if Revision == TAIWAN
	//#if ModelID == Android || ModelID == AndroidLarge || ModelID == AndroidSmall || ModelID == AndroidAuto
    //# public static joymaster.igb.billing.IGBKernel igbKernel;
    //#else
    //# public static billing.IGBKernel igbKernel;
    //#endif
    //#endif
    public SanguoMIDlet(){
        instance = this;
    }

    protected void startApp() throws MIDletStateChangeException{
        if(!isRun){
            isRun = true;

            display = Display.getDisplay(this);
			//#if NewUI2
            //# logoCanvas = new LogoCanvas();
			//#else
			gameMain = new GameMain(display);
            Utilities.setDisplay(display, gameMain);
			//#endif
            
            //#if Revision == CMCC && ModelID != AndroidAuto
            //# new cmcc.CMCCSplashCanvas(this, gameMain);
            //#else
			//#if NewUI2
            //# gameMain = new GameMain(display);
			//#endif
            display.setCurrent(gameMain);
            //#endif
            mainThread = new Thread(gameMain);
            mainThread.start();
        }
    }

    public void destroyApp(boolean arg0) throws MIDletStateChangeException{
        isRun = false;
        Utilities.closeConnection();
    }

    protected void pauseApp(){
    }

    /**
     * ³ÌÐòÍË³ö
     */
    public static void exit(){
        try{
            Utilities.isExitGame = false;
            instance.destroyApp(true);
            instance.notifyDestroyed();
            instance = null;
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
