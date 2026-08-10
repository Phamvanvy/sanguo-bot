package cmcc;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.midlet.*;

import com.pip.common.Tool;
import com.pip.sanguo.GameMain;

//#ifdef polish.api.nokia-ui
//# import com.nokia.mid.ui.*;
//#endif

public class CMCCSplashCanvas extends
//#if CanvasType == FullCanvas
		//# FullCanvas
		//#elif CanvasType == GameCanvas
		GameCanvas
//#else
		//# Canvas
		//#endif
		implements Runnable {
	int screenWidth, screenHeight;
	int deviceScreenWidth, deviceScreenHeight;

	MIDlet theMidlet;
	Displayable backCanvas;

	public static boolean showLogoEffect = true;
	public static boolean showGameLogo = true;
	Image gamelogo;

	public CMCCSplashCanvas(MIDlet midlet, Displayable d) {
		//#if CanvasType == GameCanvas
		super(false);
		setFullScreenMode(true);
		//#else
		//# super();
		//#endif
		theMidlet = midlet;
		backCanvas = d;

		deviceScreenWidth = GameMain.viewWidth;
		deviceScreenHeight = GameMain.viewHeight;

		screenWidth = deviceScreenWidth;
		screenHeight = deviceScreenHeight;
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

		//
		// g.setClip(0, 0, deviceScreenWidth, deviceScreenHeight);
		// g.setColor(0);
		// g.fillRect(0, 0, deviceScreenWidth, deviceScreenHeight);
		// g.translate((deviceScreenWidth - screenWidth)/2 ,
		// (deviceScreenHeight - screenHeight)/2);
		g.setClip(0, 0, screenWidth, screenHeight);

		if (showGameLogo) {
			g.setColor(0);
			g.fillRect(0, 0, screenWidth, screenHeight);
			g.drawImage(gamelogo, screenWidth / 2, screenHeight / 2 - gamelogo.getHeight() / 2, Graphics.HCENTER
					| Graphics.TOP);
			return;
		}

		if (this.showLogoEffect) {
			if (screenWidth < 176) {
				this.drawLogoSmall(g);
			} else {
				this.drawLogo(g);
			}
			return;
		} else {
			g.setColor(0);
			g.fillRect(0, 0, screenWidth, screenHeight);
		}

		// g.translate((screenWidth - deviceScreenWidth)/2 ,
		// (screenHeight - deviceScreenHeight)/2);

	}

	private volatile Thread sendThread;

	synchronized public void StopThread() {
		if (sendThread != null)
			sendThread = null;
	}

	long lastTime = 0;
	public static int sleeptime = 40;

	public void run() {
		try {

			if (showGameLogo) {
				// Display.getDisplay(theMidlet).setCurrent(this);
				repaint();
				serviceRepaints();
				Thread.sleep(2000);
				showGameLogo = false;
			}

			while (showLogoEffect) {

				repaint();
				serviceRepaints();
				if (screenWidth < 176) {
					Thread.sleep(1200);
					index++;
					if (index >= 3) {
						showLogoEffect = false;
					}
				} else {
					Thread.sleep(logoInfo[index][6]);
					index++;

					if (lastTime == 0)
						lastTime = Tool.getSystemTime();
					else {
						long waitTime = Tool.getSystemTime() - lastTime;
						if (waitTime < sleeptime)
							Thread.sleep(sleeptime - waitTime);
						lastTime = Tool.getSystemTime();
					}

					if (index >= logoInfo.length) // /index = 0;
						showLogoEffect = false;
				}
				continue;
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

	Image transImg1 = null;
	Image transImg2 = null;

	public void init() {
		try {
			//#if ModelID == AndroidAuto
			//# if (screenWidth < 176) {
			//#  	gamelogo = Image.createImage(CMCCSplashCanvas.class.getResourceAsStream("/" + GameMain.getModel() + "/cmcc_gameLogo.png"));
			//#  	logo = new Image[3];
			//#  	logo[0] = Image.createImage(CMCCSplashCanvas.class.getResourceAsStream("/" + GameMain.getModel() + "/cmcc_logo2.png"));
			//#  	logo[1] = Image.createImage(CMCCSplashCanvas.class.getResourceAsStream("/" + GameMain.getModel() + "/cmcc_logo3.png"));
			//#  	logo[2] = Image.createImage(CMCCSplashCanvas.class.getResourceAsStream("/" + GameMain.getModel() + "/cmcc_logo4.png"));
			//#  } else {
			//#  	gamelogo = Image.createImage(CMCCSplashCanvas.class.getResourceAsStream("/" + GameMain.getModel() + "/cmcc_gameLogo.png"));
			//#  	bg = Image.createImage(CMCCSplashCanvas.class.getResourceAsStream("/" + GameMain.getModel() + "/cmcc_bg.png"));
			//#  	logo = new Image[6];
			//#  	for (int i = 0; i < 6; i++)
			//# 		logo[i] = Image.createImage(CMCCSplashCanvas.class.getResourceAsStream("/" + GameMain.getModel() + "/cmcc_logo" + i + ".png"));
			//# 	transImg1 = Image.createImage(CMCCSplashCanvas.class.getResourceAsStream("/" + GameMain.getModel() + "/cmcc_logo51.png"));
			//#  	transImg2 = Image.createImage(CMCCSplashCanvas.class.getResourceAsStream("/" + GameMain.getModel() + "/cmcc_logo52.png"));
			//#  }
			//#else
			if (screenWidth < 176) {
				gamelogo = Image.createImage("/cmcc_gameLogo.png");
				logo = new Image[3];
				logo[0] = Image.createImage("/cmcc_logo2.png");
				logo[1] = Image.createImage("/cmcc_logo3.png");
				logo[2] = Image.createImage("/cmcc_logo4.png");
			} else {
				gamelogo = Image.createImage("/cmcc_gameLogo.png");
				bg = Image.createImage("/cmcc_bg.png");
				logo = new Image[6];
				for (int i = 0; i < 6; i++)
					logo[i] = Image.createImage("/cmcc_logo" + i + ".png");
				transImg1 = Image.createImage("/cmcc_logo51.png");
				transImg2 = Image.createImage("/cmcc_logo52.png");
			}
			//#endif

		} catch (Exception e) {
			System.out.println("there is a error when init");
		}
	}

	public void clear() {
		gamelogo = null;
		bg = null;
		logo = null;
		transImg1 = null;
		transImg2 = null;
	}

	Image bg = null;
	Image[] logo;

	public static int index = 0;

	int[] widoff = { 35, 88, 140 };
	int[] heioff0 = { 16, 14, 5, 1, -3, -2, 2 };
	int[] heioff2 = { 16, 12, 2, -8, -12 };
	int[] heioff1 = { 18, 15, -2, 0 };
	int hei5 = 72;
	int[][] logoInfo = {
			{ 2, heioff0[6], 0, heioff0[6], 1, heioff0[6], 500 },
			{ // 完全显示第1幕
			2, heioff0[2], 0, heioff0[2], 1, heioff0[2], sleeptime },
			{ // 第1幕开始淡出
			2, heioff0[1], 0, heioff0[1], 1, heioff0[1], sleeptime },
			{ 2, heioff0[0], 0, heioff0[0], 1, heioff0[0], sleeptime, 2 },

			{ -1, heioff1[0], -1, heioff1[0], -1, heioff1[0], sleeptime },
			{ // 第2幕开始出现
			-1, heioff1[0], 5, heioff1[0], -1, heioff1[0], sleeptime },
			{ -1, heioff1[0], 5, heioff1[1], -1, heioff1[0], sleeptime },
			{ -1, heioff1[0], 5, heioff1[2], -1, heioff1[0], sleeptime },
			{ -1, heioff1[0], 5, heioff1[3], -1, heioff1[0], sleeptime },
			{ -1, heioff1[0], 5, heioff1[0], -1, heioff1[0], sleeptime },
			{ -1, heioff1[0], 5, heioff1[1], 5, heioff1[0], sleeptime, 1 },
			{ -1, heioff1[0], 5, heioff1[2], 5, heioff1[1], sleeptime },
			{ 5, heioff1[0], 5, heioff1[3], 5, heioff1[2], sleeptime },
			{ 5, heioff1[1], 5, heioff1[0], 5, heioff1[3], sleeptime }
			// , { //repeat
			// 5, heioff1[2], 5, heioff1[1], 5, heioff1[0], sleeptime}
			// , {
			// 5, heioff1[3], 5, heioff1[2], 5, heioff1[1], sleeptime}
			// , {
			// 5, heioff1[0], 5, heioff1[3], 5, heioff1[2], sleeptime}
			// ,
			//
			// {
			// 5, heioff1[1], 5, heioff1[0], 5, heioff1[3], sleeptime}

			// , { //repeat
			// 5, heioff1[2], 5, heioff1[1], 5, heioff1[0], sleeptime}
			// , {
			// 5, heioff1[3], 5, heioff1[2], 5, heioff1[1], sleeptime}
			// , {
			// 5, heioff1[0], 5, heioff1[3], 5, heioff1[2], sleeptime}
			// , {
			// 5, heioff1[1], 5, heioff1[0], 5, heioff1[3], sleeptime}
			,
			{ // repeat
			5, heioff1[2], 5, heioff1[1], 5, heioff1[0], sleeptime },
			{ 5, heioff1[3], 5, heioff1[2], 5, heioff1[1], sleeptime },
			{ 5, heioff1[0], 5, heioff1[3], 5, heioff1[2], sleeptime },
			{ 5, heioff1[1], 5, heioff1[0], 5, heioff1[3], sleeptime }

			// show cmcc
			,
			{ 5, heioff1[2], 2, heioff0[0], 5, heioff1[0], sleeptime },
			{ 5, heioff1[3], 2, heioff0[1], 5, heioff1[1], sleeptime },
			{ 5, heioff1[0], 2, heioff0[2], 5, heioff1[2], sleeptime },
			{ 5, heioff1[1], 2, heioff0[3], 5, heioff1[3], sleeptime },
			{ 5, heioff1[2], 2, heioff0[4], 5, heioff1[0], sleeptime },
			{ 5, heioff1[3], 2, heioff0[5], 5, heioff1[1], sleeptime }
			// , {
			// 5, heioff1[0], 2, heioff0[6], 5, heioff1[2], sleeptime}
			// , {
			// 5, heioff1[1], 2, heioff0[6], 5, heioff1[3], sleeptime}
			// , {
			// 5, heioff1[2], 2, heioff0[6], 5, heioff1[0], sleeptime}
			// , {
			// 5, heioff1[3], 2, heioff0[6], 5, heioff1[1], sleeptime}

			// , {
			// 5, heioff1[0], 2, heioff0[6], 5, heioff1[2], sleeptime}
			// , {
			// 5, heioff1[1], 2, heioff0[6], 5, heioff1[3], sleeptime}

			// show cp
			, { 3, heioff0[0], 2, heioff0[6], 5, heioff1[2], sleeptime },
			{ 3, heioff0[1], 2, heioff0[6], 5, heioff1[3], sleeptime },
			{ 3, heioff0[2], 2, heioff0[6], 5, heioff1[0], sleeptime },
			{ 3, heioff0[3], 2, heioff0[6], 5, heioff1[1], sleeptime },
			{ 3, heioff0[4], 2, heioff0[6], 5, heioff1[2], sleeptime },
			{ 3, heioff0[5], 2, heioff0[6], 5, heioff1[3], sleeptime }, {
			// 3, heioff0[6], 2, heioff0[6], 5, heioff1[0], sleeptime}
					// , {
					// 3, heioff0[6], 2, heioff0[6], 5, heioff1[1], sleeptime}
					// , {
					// 3, heioff0[6], 2, heioff0[6], 5, heioff1[2], sleeptime}
					// , {
					// 3, heioff0[6], 2, heioff0[6], 5, heioff1[3], sleeptime}
					// , {

					// show sp
					4, heioff0[0], 2, heioff0[6], 3, heioff0[6], sleeptime },
			{ 4, heioff0[1], 2, heioff0[6], 3, heioff0[6], sleeptime },
			{ 4, heioff0[2], 2, heioff0[6], 3, heioff0[6], sleeptime }

			, { 4, heioff0[6], 2, heioff0[6], 3, heioff0[6], 1000 }
	// , {
	// 4, heioff0[3], 2, heioff0[6], 3, heioff0[6], sleeptime}
	// , {
	// 4, heioff0[4], 2, heioff0[6], 3, heioff0[6], sleeptime}
	// , {
	// 4, heioff0[5], 2, heioff0[6], 3, heioff0[6], sleeptime}
	// , {
	// 4, heioff0[6], 2, heioff0[6], 3, heioff0[6], 1000}

	};

	public void drawLogo(Graphics g) {

		int xoff = (screenWidth - bg.getWidth()) / 2;
		int yoff = (screenHeight - bg.getHeight()) / 2;
		g.setClip(0, 0, screenWidth, screenHeight);
		g.setColor(0);
		g.fillRect(0, 0, screenWidth, screenHeight);
		g.drawImage(bg, xoff, yoff, 20);
		for (int i = 0; i < 3; i++)
			if (logoInfo[index][i * 2] != -1) {
				int www = widoff[i];
				if (logoInfo[index][i * 2] == 3)
					www = widoff[0];
				else if (logoInfo[index][i * 2] == 4)
					www = widoff[2];
				if (logoInfo[index][i * 2] == 5) {

					int jiaozhengx[] = { 0, -1, -1 };
					if (logoInfo[index][i * 2 + 1] == heioff1[0])
						g.drawImage(transImg1, www + jiaozhengx[i] + xoff, 73 + yoff, g.HCENTER | g.TOP);
					else if (logoInfo[index][i * 2 + 1] == heioff1[1])
						g.drawImage(logo[logoInfo[index][i * 2]], www + xoff, 100 + yoff
								- logo[logoInfo[index][i * 2]].getHeight() / 2, g.HCENTER | g.TOP);
					else if (logoInfo[index][i * 2 + 1] == heioff1[2])
						g.drawImage(transImg2, www + jiaozhengx[i] + xoff, 72 + yoff, g.HCENTER | g.TOP);
				} else
					g.drawImage(logo[logoInfo[index][i * 2]], www + xoff, logoInfo[index][i * 2 + 1] + 100 + yoff
							- logo[logoInfo[index][i * 2]].getHeight() / 2, g.HCENTER | g.TOP);
			}
	}

	public void drawLogoSmall(Graphics g) {
		g.setClip(0, 0, screenWidth, screenHeight);
		g.setColor(0);
		g.fillRect(0, 0, screenWidth, screenHeight);
		g.drawImage(logo[index], screenWidth / 2, screenHeight / 2 - logo[index].getHeight() / 2, g.HCENTER | g.TOP);
	}
}
