package pip;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

//#if polish.midp2
import javax.microedition.lcdui.game.GameCanvas;
//#endif

//#ifdef polish.api.nokia-ui
//# import com.nokia.mid.ui.*;
//#endif

public class CMCCExitCanvas extends
//#if (Directory == SE-K500) || (Directory == SE-S700)
                //# GameCanvas
                //#define GameCanvas
//#elif polish.api.nokia-ui
                //# FullCanvas
                //#elif polish.midp2
                GameCanvas
                //#define GameCanvas
                //#else
                //#   Canvas
                //#endif
                //#if CommandEmu == true
                implements CommandListener
                //#endif
{
	private int state;
	private Command cmd1, cmd2;
	private Image bgImg;
	private Image quitGameImage;
	private Image scrollImage;
	private int[] start;
	private int scroll;
	private boolean upOrDownScroll;
	//private String msgs;
	public static final String updateUrl = "http://go.i139.cn/gcomm1/portal/spchannel.do?url=http://gamepie.i139.cn/wap/s.do?j=3channel";
	public static String updateUrl1 = null;
	
	public CMCCExitCanvas() {
		//#ifdef GameCanvas
        super(false);
        setFullScreenMode(true);
        //#else
        //# super();
        //#endif
        //#if CommandEmu == true
        //# cmd1 = new Command("是", Command.CANCEL, 1);
        //# cmd2 = new Command("否", Command.OK, 1);
        //# addCommand(cmd1);
        //# addCommand(cmd2);
        //#endif
        
        //#if (Revision == PIP) || (Revision == SOHU) || (Revision == DOWNJOY) || (Revision == JIANGSUN)
        try {
			quitGameImage = Image.createImage("/quitgame.png");
			scrollImage = Image.createImage("/scrollimage.png");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
		}
		//#if (TouchScreen == true) || (Directory == NK-BigScreen) || (Directory == NK-Nokia403Big) || (Directory == MT-General) || (M-Name == SAM_L288) || (Directory == SE-S700) || (Directory == ZTE_U860)
		start = new int[]{14,236};
		//#elif Directory == NK-E61
		//# start = new int[]{80,185};
		//#elif (Directory == Midp2-General) || (Directory == NK-60-2) || (Directory == NK-6681) || (Directory == NK-NGage) || (Directory == NK3250) || (Directory == SE-K700)
		//# start = new int[]{12,138};
		//#elif (M-Name == NK_5500)
		//# start = new int[]{28,140};
		//#elif (Directory == Nokia403) || (Directory == SE-K500)
		//# start = new int[]{9,112};
		//#elif Directory == SE-K300
		//# start = new int[]{8,95};
		//#else
		//# start = new int[]{12,138};
		//#endif
		
		upOrDownScroll = true;
		scroll = 0;
        state = 1;
        //#else
        try {
        	bgImg = Image.createImage("/cmcc_last.png");
        } catch (Exception e) {
        }
        //#endif
	}
	
	public void paint(Graphics g) {
		try{
		
		if(scrollImage != null){ 
			if(upOrDownScroll){
				scroll ++;
				if(scroll >= scrollImage.getHeight()){
					upOrDownScroll =false;
					Thread.sleep(2000);	
				}
			}else{
				scroll --;
				if(scroll <= 0){
					upOrDownScroll =true;
					Thread.sleep(2000);
					
				}
			}
			
		}
		g.setClip(0, 0, World.viewWidth, World.viewHeight);
		g.setColor(0x000000);
		g.setFont(GameState.font);
		g.fillRect(0, 0, World.viewWidth, World.viewHeight);
		if (bgImg != null) {
			g.drawImage(bgImg, World.viewWidth / 2, World.viewHeight / 2 - bgImg.getHeight() / 2, g.HCENTER | g.TOP);
		}
		g.setColor(0xFFFFFF);
		int fh = GameState.font.getHeight();
		//#if TouchScreen == true
        StaticUtils.beginButtonSetting();
        //#endif
		if (state == 0) {
			
			g.drawString("确认退出",  World.viewWidth / 2, World.viewHeight / 2 - 20, Graphics.TOP | Graphics.HCENTER);
			//#if CommandEmu == false
			g.drawString("否", 0, World.viewHeight - fh, Graphics.TOP | Graphics.LEFT);
			g.drawString("是", World.viewWidth, World.viewHeight - fh, Graphics.TOP | Graphics.RIGHT);
				//#if TouchScreen == true
	            StaticUtils.removeAllButton();
	            StaticUtils.addButton(2000, 0, World.viewHeight - GameState.font.getHeight(), GameState.CHAR_WIDTH *2,  GameState.font.getHeight());
	            StaticUtils.addButton(2001, World.viewWidth-GameState.CHAR_WIDTH *2, World.viewHeight - GameState.font.getHeight(), GameState.CHAR_WIDTH *2,  GameState.font.getHeight());
	            //#endif
			//#endif
		} else {
	        //#if (Revision == PIP) || (Revision == SOHU) || (Revision == DOWNJOY) || (Revision == JIANGSUN)

			if(quitGameImage != null && scrollImage != null){//画背景图和滚动图
				g.drawImage(quitGameImage, World.viewWidth /2, World.viewHeight / 2, Graphics.HCENTER | Graphics.VCENTER);
				g.drawImage(scrollImage, start[0], start[1]+scrollImage.getHeight()-scroll, Graphics.TOP | Graphics.LEFT);
				g.setColor(0x000000);
				g.fillRect(0,  start[1]+scrollImage.getHeight(), World.viewWidth, World.viewHeight);
			}else{
			    String[] tmp = World.splitString(GameState.pushString == null? GameState.PUSH_DEFAULT_STRING: GameState.pushString, 1000, GameState.font);
		        g.drawString(tmp[1], World.viewWidth / 2, World.viewHeight / 2 - 20 - fh - 2, Graphics.TOP | Graphics.HCENTER);
		        g.drawString(tmp[2], World.viewWidth / 2, World.viewHeight / 2 - 20, Graphics.TOP | Graphics.HCENTER);
		        g.drawString(tmp[3], World.viewWidth / 2, World.viewHeight / 2 - 20 + fh + 2, Graphics.TOP | Graphics.HCENTER);
		        if(GameState.pushString == null){
		        	g.drawString(tmp[4], World.viewWidth / 2, World.viewHeight / 2 - 20 + fh*2 + 2, Graphics.TOP | Graphics.HCENTER);
		        }
			}
		    //#else
			//# g.drawString("更多精彩游戏", World.viewWidth / 2, World.viewHeight / 2 - 20 - fh - 2, Graphics.TOP | Graphics.HCENTER);
			//# g.drawString("尽在游戏频道", World.viewWidth / 2, World.viewHeight / 2 - 20, Graphics.TOP | Graphics.HCENTER);
			//# g.drawString("wap.xjoys.com", World.viewWidth / 2, World.viewHeight / 2 - 20 + fh + 2, Graphics.TOP | Graphics.HCENTER);
	        //#endif

			//#if CommandEmu == false
    	        //#if (Revision == PIP) || (Revision == SOHU) || (Revision == DOWNJOY) || (Revision == JIANGSUN)
				g.setColor(0xffffff);
				World.draw3DString(g, "进入" ,GameState.CHAR_WIDTH,World.viewHeight -GameState.LINE_HEIGHT, Graphics.TOP | Graphics.HCENTER, 0xffffff, 0x000000);
				World.draw3DString(g, "离开" ,World.viewWidth - GameState.CHAR_WIDTH, World.viewHeight - GameState.LINE_HEIGHT, Graphics.TOP | Graphics.HCENTER,0xffffff, 0x000000);
    	        //g.drawString("进入", 0, World.viewHeight - GameState.font.getHeight(), Graphics.TOP | Graphics.LEFT);
                //g.drawString("离开", World.viewWidth, World.viewHeight - GameState.font.getHeight(), Graphics.TOP | Graphics.RIGHT);
    	        //#else
    	        //# g.drawString("确认", 0, World.viewHeight - GameState.font.getHeight(), Graphics.TOP | Graphics.LEFT);
                //# g.drawString("退出", World.viewWidth, World.viewHeight - GameState.font.getHeight(), Graphics.TOP | Graphics.RIGHT);
    	        //#endif
                
                //#if TouchScreen == true
                StaticUtils.removeAllButton();
                StaticUtils.addButton(2000, 0, World.viewHeight - GameState.font.getHeight(), GameState.CHAR_WIDTH *2,  GameState.font.getHeight());
                StaticUtils.addButton(2001, World.viewWidth-GameState.CHAR_WIDTH *2, World.viewHeight - GameState.font.getHeight(), GameState.CHAR_WIDTH *2,  GameState.font.getHeight());
                //#endif
			//#endif
		}
		//#if TouchScreen == true
        StaticUtils.endButtonSetting();
        //#endif
        repaint();
		}catch(Throwable ex){
        	
//            if(msgs == null){
//            	msgs = "";
//            }
//            msgs += ex.toString();
//            msgs = ex.toString();
//           
//            ex.printStackTrace();
        }finally{
//            g.setFont(GameState.font);
//            g.setColor(0x000000);
//            g.fillRect(0, 100, GameState.font.stringWidth(msgs), GameState.CHAR_HEIGHT);
//            g.setColor(0xffffff);
//            g.drawString(msgs + World.viewWidth + " , " + World.viewHeight, 0, 100, Graphics.TOP | Graphics.LEFT);
        }
	}

	protected void keyPressed(int keyCode) {
//#if JBlend == true
		//# if (keyCode == -21 || keyCode == 21) {
//#else
		if (keyCode == -6) {
//#endif
			confirm();
//#if JBlend == true
		//# } else if (keyCode == -22 || keyCode == 22) {
//#else
		} else if (keyCode == -7) {	
//#endif
			cancel();
		}
	}
	//#if TouchScreen == true
	 public void pointerPressed(int x, int y){
		 StaticUtils.pointerPressed(x, y);
	}
	 public void pointerReleased(int x, int y) {
	    StaticUtils.pointerReleased(x, y);
	    int pressButton = StaticUtils.getPressedButton();
	    if(-1 != pressButton && 2000 <= pressButton ){
	    	if(2000 == pressButton ){
	    		confirm();
	    	}else if(2001 == pressButton){
	    		cancel();
	    	}
	    }
	 }
	  //#endif

	
	private void confirm() {
		if (state == 0) {
			World.RecordPreousDisplay(World.instance);
			//World.display.setCurrent(World.instance);
		} else {
            try{
            	//#if Directory != NK-NGage
                    //#if (Revision == PIP) || (Revision == SOHU) || (Revision == DOWNJOY) || (Revision == JIANGSUN)
                    String[] tmp = World.splitString(GameState.pushString == null? GameState.PUSH_DEFAULT_STRING: GameState.pushString, 1000, GameState.font);
                    iTimesMIDlet.instance.platformRequest(tmp[0]);
                    //#else
                    //# if(updateUrl1 != null && updateUrl1.trim().length() > 0){
                    //#     iTimesMIDlet.instance.platformRequest(updateUrl1);
                    //# }else{
                    //#     iTimesMIDlet.instance.platformRequest(updateUrl);
                    //# }
                    //#endif
                //#endif
                Thread.sleep(500);
                GameState.closeConnection();
                iTimesMIDlet.instance.exitGame();
            }catch(Exception e){
                e.printStackTrace();
            }
		}
	}
	
	private void cancel() {
		if (state == 0) {
			state = 1;
			repaint();
			//#if CommandEmu == true
			//# this.removeCommand(cmd1);
			//# this.removeCommand(cmd2);
    	        //#if (Revision == PIP) || (Revision == SOHU) || (Revision == DOWNJOY) || (Revision == JIANGSUN)
    			//# addCommand(new Command("进入", Command.OK, 1));
    			//# addCommand(new Command("退出", Command.CANCEL, 1));
			    //#else
                //# addCommand(new Command("确认", Command.OK, 1));
                //# addCommand(new Command("退出", Command.CANCEL, 1));
			    //#endif
			//#endif
		} else {
			GameState.closeConnection();
            iTimesMIDlet.instance.exitGame();
		}
	}
	
	public void commandAction(Command arg0, Displayable arg1) {
		if (arg0.getCommandType() == Command.OK) {
			confirm();
		} else {
			cancel();
		}
	}
}
