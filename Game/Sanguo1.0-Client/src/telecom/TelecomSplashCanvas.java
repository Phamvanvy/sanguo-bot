package telecom;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.midlet.MIDlet;

import com.pip.common.Tool;
import com.pip.sanguo.GameMain;

//#ifdef polish.api.nokia-ui
//# import com.nokia.mid.ui.*;
//#endif

public class TelecomSplashCanvas extends
//#if CanvasType == FullCanvas
		//# FullCanvas
		//#elif CanvasType == GameCanvas
		GameCanvas
//#else
		//# Canvas
		//#endif
		implements Runnable {
	int screenWidth, screenHeight;

	MIDlet theMidlet;
	Displayable backCanvas;
	Image logo;

	public TelecomSplashCanvas(MIDlet midlet, Displayable d) {
		//#if CanvasType ==  GameCanvas
		super(false);
		setFullScreenMode(true);
		//#else
		//# super();
		//#endif
		theMidlet = midlet;
		backCanvas = d;

		screenWidth = GameMain.viewWidth;
		screenHeight = GameMain.viewHeight;
		init();
		Display.getDisplay(theMidlet).setCurrent(this);
		sendThread = new Thread(this);
		sendThread.start();
	}

	int leftKey = -6;
	int rightKey = -7;

	public void keyPressed(int keyCode) {
	}

	public void keyReleased(int keyCode) {

	}

	public void paint(Graphics g) {
		g.setClip(0, 0, screenWidth, screenHeight);
		if (logo != null) {
			g.drawImage(logo, screenWidth / 2 - logo.getWidth() / 2, 
					screenHeight / 2 - logo.getHeight() / 2, Graphics.TOP | Graphics.LEFT);
		}
	}

	private volatile Thread sendThread;

	synchronized public void StopThread() {
		if (sendThread != null)
			sendThread = null;
	}

	long lastTime = 0;

	public void run() {
		try {
			while (true) {
				repaint();
				serviceRepaints();
				if (lastTime == 0) {
					lastTime = Tool.getSystemTime();
				} else if (Tool.getSystemTime() > lastTime + 3000) {
					return;
				}
				Thread.sleep(100);
			}
		} catch (Exception ex) {
		} finally {
			StopThread();
			if (backCanvas != null) {
				Display.getDisplay(theMidlet).setCurrent(backCanvas);
				clear();
			}
		}
	}

	public void init() {
		try {
			logo = Image.createImage("/logo.png");
		} catch (Exception e) {
			System.out.println("there is a error when init");
		}
	}

	public void clear() {
		logo = null;
	}
}
