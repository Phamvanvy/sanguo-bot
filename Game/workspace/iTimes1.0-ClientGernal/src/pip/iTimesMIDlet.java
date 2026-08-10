package pip;


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;


public class iTimesMIDlet extends MIDlet{
    public boolean isRunning = false;
    public static iTimesMIDlet instance = null;
    public static Display display = null;
    public static String channelId = null;

    public iTimesMIDlet(){
        instance = this;
    }

    public void destroyApp(boolean _boolean) throws MIDletStateChangeException{
        GameState.logouting = true;
        GameState.isMapLoadOk = false;
        GameState.closeConnection();
        isRunning = false;
        World.release();
    }

    public void pauseApp(){
        if(World.nowBattle <0){
            boolean flg = true;
            for(int i = 0; i < World.events.size(); i++){
                GameEvent e = (GameEvent)World.events.elementAt(i);
                if(e.getType() == GameEvent.EVENT_PAUSE){
                    flg = false;
                    break;
                }
            }
            //#if Revision == CMCC || (Revision == JIANGSUNCMCC) 
            if(flg){
                GameEvent event = new GameEvent(GameEvent.EVENT_PAUSE, 0, 0);
                World.addEvent(event);
            }
            //#endif
        }
        World.clearKeyStates();
    }

    public final void startApp() throws MIDletStateChangeException{
        if(!isRunning){
            isRunning = true;
            instance = this;
            channelId = getAppProperty("ChannelID");

            if(channelId == null){
                channelId = "";
            }

            //#if Directory == NK-3650
            //# try{
            //#    Thread.sleep(3000);
            //# }
            //# catch(InterruptedException ex){
            //# }
            //#endif

            display = Display.getDisplay(this);
            
            World w = new World();
            World.display = display;

            //#if Revision == CMCC || (Revision == JIANGSUNCMCC) 
            //# new cmcc.CMCCSplashCanvas(this, w);
            //#else
            display.setCurrent(w);
            new Thread(w).start();
            //#endif
        }
    }

    public final void exitGame(){
        try{
            destroyApp(true);
        }catch(Exception ex){
        }
        
        try{
            notifyDestroyed();
        }catch(Exception ex){
        }
    }
    
    //#if Revision == QQ
    public static String getQQId(){
        //#if TestVersion == true
        //# return GameState.name;
        //#else
        return "999";
        //#endif
    }
    
    public static String getSID(){
        //#if TestVersion == true
        //# return GameState.password;
        //#else
        return "888";
        //#endif
    }
    
    public static void buyQQGood(Displayable preWin,String linkid,int goodId,int count){
    }
    
    public static void smsBuyGood(Displayable preWin,int areaId,int goodId,int gameId,int channelId){
    }
    
    public static void shenZhouFu(Displayable preWin,byte cpId,int goodId,String linkId){
    }
    //#endif
    
    //#if Directory == MT-V300
    //# private static final String[] fileNamesInPkg = {
    //#             "!.p", "!.s", "attrnum.p", "attrnum.s", "bag.png", "battleIcon.p", "battleIcon.s", "body.p", "body.s", "btn.p", "btn.s", "buf.p", "buf.s", "chars.p", "chars.s", "da_female.p",
    //#             "da_female.s", "da_female_weapon.p", "da_female_weapon.s", "da_male.p", "da_male.s", "da_male_weapon.p", "da_male_weapon.s", "defaultArmy.p", "defaultArmy.s", "defaultItemNpc.p",
    //#             "defaultItemNpc.s", "defaultMonster.p", "defaultMonster.s", "defaultNpc.p", "defaultNpc.s", "die.p", "die.s", "door.p", "door.s", "edges.p", "edges.s", "effect.p", "effect.s",
    //#             "equips.p", "equips.s", "hmp.p", "hmp.s", "icon.png", "mail.png", "menubtn.s", "message.png", "pet.p", "pet.s", "res0.p", "res0.s", "res1.p", "res1.s", "res2.p", "res2.s",
    //#             "res3.p", "res3.s", "topbar.png", "_female.p", "_female.s", "_male.p", "_male.s",
    //# };

    //# private static final short[][] offsetsInPkg = {
    //# { 0, 353 },{ 353, 62 },{ 415, 249 },{ 664, 36 },{ 700, 254 },{ 954, 499 },{ 1453, 26 },{ 1479, 252 },{ 1731, 26 },
    //# { 1757, 246 },{ 2003, 50 },{ 2053, 459 },{ 2512, 34 },{ 2546, 152 },{ 2698, 26 },{ 2724, 934 },{ 3658, 62 },{ 3720, 1091 },
    //# { 4811, 92 },{ 4903, 775 },{ 5678, 62 },{ 5740, 1010 },{ 6750, 92 },{ 6842, 623 },{ 7465, 20 },{ 7485, 292 },{ 7777, 6 },
    //# { 7783, 405 },{ 8188, 8 },{ 8196, 924 },{ 9120, 6 },{ 9126, 352 },{ 9478, 38 },{ 9516, 567 },{ 10083, 8 },{ 10091, 212 },
    //# { 10303, 12 },{ 10315, 1001 },{ 11316, 82 },{ 11398, 342 },{ 11740, 22 },{ 11762, 133 },{ 11895, 10 },{ 11905, 391 },
    //# { 12296, 146 },{ 12442, 12 },{ 12454, 182 },{ 12636, 1533 },{ 14169, 146 },{ 14315, 307 },{ 14622, 22 },{ 14644, 380 },
    //# { 15024, 22 },{ 15046, 509 },{ 15555, 22 },{ 15577, 284 },{ 15861, 22 },{ 15883, 321 },{ 16204, 2132 },{ 18336, 28 },
    //# { 18364, 2054 },{ 20418, 28 },
    //# };
    //# public static Hashtable resourceCache = new Hashtable();

    //# public static byte[] loadResource(String name) throws IOException{
    //# if(resourceCache.get(name) != null){
    //#     return (byte[])resourceCache.get(name);
    //# }
    //# for(int i = 0; i < fileNamesInPkg.length; i++){
    //#     if(fileNamesInPkg[i].equals(name)){
    //#         InputStream is = instance.getClass().getResourceAsStream("/1.pkg");
    //#         DataInputStream dis = new DataInputStream(is);
    //#         dis.skip(offsetsInPkg[i][0]);
    //#         byte[] ret = new byte[offsetsInPkg[i][1]];
    //#         dis.readFully(ret);
    //#          dis.close();
    //#         return ret;
    //#     }
    //# }
    //# throw new IOException();
    //# }

    //# public static Image createImageFromResource(String name) throws IOException{
    //# byte[] data = loadResource(name);
    //# return Image.createImage(data, 0, data.length);
    //# }
    //#endif

}
