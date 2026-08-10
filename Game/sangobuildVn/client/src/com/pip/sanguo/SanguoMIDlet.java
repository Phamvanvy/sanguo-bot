package com.pip.sanguo;


import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import com.pip.common.Utilities;
import com.pip.ui.VMGame;


public class SanguoMIDlet extends MIDlet{
    public static SanguoMIDlet instance;
    public static boolean isRun;
    public static Display display;
    GameMain gameMain;
    public static Thread mainThread;
    //#if Revision == TAIWAN
    public static billing.IGBKernel igbKernel;
    //#endif
    
    public SanguoMIDlet(){
        instance = this;
    }

    protected void startApp() throws MIDletStateChangeException{
        if(!isRun){
            isRun = true;

            display = Display.getDisplay(this);
            gameMain = new GameMain(display);
            Utilities.setDisplay(display, gameMain);

            //#if Revision == CMCC
            //# new cmcc.CMCCSplashCanvas(this, gameMain);
            //#else
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