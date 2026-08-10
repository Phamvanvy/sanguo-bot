package com.pip.sanguo;

import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
//#if ModelID == AndroidAuto

//# import android.graphics.Bitmap;
//# import android.graphics.Matrix;
//#if opengl == true
//# import com.pip.android.opengl.GLGraphics;
//#endif
//#endif
import com.pip.common.Tool;
import com.pip.engine.AnimatePlayer;
import com.pip.engine.FlyingStringInfo;
import com.pip.engine.GameMap;
import com.pip.engine.LandformImage;
import com.pip.image.ImageSet;
import com.pip.image.PipAnimateSet;
import com.pip.io.UASegment;
import com.pip.ui.Quest;
import com.pip.ui.VMGame;
import com.pip.util.SortHashtable;

public class GameView {
	public GameMap map;

	private ImageSet tileImage = null;
	private byte[][] tinfo = null;

	private LandformImage[] landformImages = null;
	private byte[][] landformTileInfos = null;
	//#if MemoryMode == "Small"
	//# private int[] mapDataBufferList = null;
	//# private Object[] mapDataBuffer = null;
	//#else
	private int[][] mapDataBuffer = null;
	//#endif
	private boolean mapDataBufferReleased = true;
	private byte[][] mapCollisonData = null;

	private int[] miniMapData = null;
	private int miniMapWidth;
	private int miniMapHeight;
	private int[] miniMapProcData = null; // int[2], 0:next flash tick,
											// 1:current color index

	//#if ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == Android || ModelID == Nokia5800 || ModelID == AndroidAuto
	//# public int miniMapMaxWidth;
	//# public int miniMapMaxHeight;
	//#endif

	public static boolean mapNpcAnimateNeedLoad = true;
	public static PipAnimateSet mapNpcAnimateSet = null;
	private int[][] mapNpcCollision = null;

	public int tileWidth;
	public int tileHeight;
	private int tileXCount;
	private int tileYCount;
	public int pathTileWidth;
	public int pathTileHeight;
	private int pathTileXCount;
	private int pathTileYCount;

	//#if (ModelID == Nokia6681)
	//# private boolean useImageBuffer = false;
	//#else
//#if NewUI2
	//# 	private boolean useImageBuffer = false;
//#else
	private boolean useImageBuffer = true;
//#endif
	//#endif
	//#if ModelID == LenovoU1 || UseMapPatch == true
	//# private ImageSet bgPatchImg = null;
	//#endif
	
	//#if ModelID == AndroidLarge
	//# private static Image bgImg = null;
	//#else 
	private Image bgImg = null;
	//#endif
	
	public boolean isFirstBgImage = true;
	private Graphics gg;
	private int oldStartX, oldStartY, bgCellW, bgCellH;
	private int bgWidth, bgHeight;
	private int oldEndX, oldEndY;
	public int vx, vy; // 使屏幕震动

	private short[] yOrder = null; // 每次循环生成，按Y轴顺序排列，处理遮挡问题，格式 [0] type，[1]
									// index，[2] y [3] x
	private int yOrderCount = 0;
	private short[] autoSelectOrder = null; // 自动选中队列每次循环生成，按距离玩家远近排序 [0] type,
											// [1] index [2] distance [3] 留用
	private short[] forceSelectOrder = null; // 自动选中队列每次循环生成，按距离玩家远近排序 [0] type,
												// [1] index [2] distance [3] 留用
	private int autoSelectOrderCount = 0;
	private int forceSelectOrderCount = 0;
	private Vector pendingItems = new Vector();
	private Vector pendingItemsTop = new Vector();
	private Vector mapNpcDirtyData = new Vector();

	private static int[] thumbColors;
	public static int[] miniMapConfig;
	public static boolean miniMapShow = false;

	public static final byte MINI_MAP_CONIFG_ALPHA = 0;
	private static final byte MINI_MAP_CONIFG_FLASH = 1;
	private static final byte MINI_MAP_CONIFG_FLASH_COLOR_COUNT = 2;
	private static final byte MINI_MAP_CONIFG_ICON_SIZE = 3;
	private static final byte MINI_MAP_CONFIG_BOX1_COLOR = 4;
	private static final byte MINI_MAP_CONFIG_BOX2_COLOR = 5;
	private static final byte MINI_MAP_CONIFG_SPHERE1 = 6;
	private static final byte MINI_MAP_CONIFG_SCALE1 = 7;
	private static final byte MINI_MAP_CONIFG_SPHERE2 = 8;
	private static final byte MINI_MAP_CONIFG_SCALE2 = 9;
	private static final byte MINI_MAP_CONIFG_SPHERE3 = 10;
	private static final byte MINI_MAP_CONIFG_SCALE3 = 11;
	private static final byte MINI_MAP_CONIFG_X = 12;
	private static final byte MINI_MAP_CONIFG_Y = 13;
	private static final byte MINI_MAP_CONIFG_NET_WIDTH = 14;
	private static final byte MINI_MAP_CONIFG_NET_HEIGHT = 15;

	public static final byte MINI_MAP_NET_COLOR_FAST = 0;
	public static final byte MINI_MAP_NET_COLOR_NORMAL = 1;
	public static final byte MINI_MAP_NET_COLOR_SLOW = 2;
	public static final byte MINI_MAP_NET_COLOR_BAD = 3;
	public static int currNetColor = MINI_MAP_NET_COLOR_NORMAL;

	private static final byte MAP_CAN_PASS = 0;
	private static final byte MAP_NOT_PASS = 1;

	private static final byte PATH_SHIFT = 1;
	
    /**
     * View区域的左上角x坐标
     */
    public static int viewX;
    /**
     * View区域的左上角y坐标
     */
    public static int viewY;
	
    /**
     * 实际显示范围高度（在scale不等于1时不等于viewHeight）
     */
    public static int showHeight;
    /**
     * 实际显示范围宽度（在scale不等于1时不等于viewWidth）
     */
    public static int showWidth;
	
//#if opengl == true
    //# // opengl模式优化变量
	//# private int openglMapGridSize;
	//# private GLGraphics[][] openglMapGridPaint; 
  //# private GLGraphics groundMapNpcPaint = new GLGraphics();
//#endif

	public static boolean showMapNpcAnimate = true;

	public static int[] sortTable = { 1, 4, 10, 23, 57, 132, 301, 701, 1577, 3548, 7983, 17961, 40412, 90927, 204585,
			460316, 1035711, 2330349 };
	
	public static final SortHashtable drawItemPanel = new SortHashtable();
	private Tool idKey = new Tool();

	public GameView(GameMap map) {
		this.map = map;
		rebuildViewData();
		//#if NewUI2
		//10.22
		//# showWidth = GameMain.virtualScreenWidthMap;
		//# showHeight = GameMain.virtualScreenHeightMap;
		//#else
		showWidth = GameMain.viewWidth;
		showHeight = GameMain.viewHeight;
		//#endif
	}
	
	public int addDrawItem(PipAnimateSet pas, ImageSet image, int frame, int x, int y, int trans, int anchor){
		int id = idKey.nextKey();
		DrawItem di = new DrawItem(pas, image, frame, x, y, trans, anchor);
		drawItemPanel.put(new Integer(id), di);
		return id;
	}
	
	public void removeDrawItem(int key){
		drawItemPanel.remove(new Integer(key));
	}
	
	public void clearDrawItem(){
		drawItemPanel.clear();
	}

	public GameSprite findNearTarget(boolean autoList) {
		short[] orderList = forceSelectOrder;
		int orderCount = forceSelectOrderCount;

		if (autoList) {
			orderList = autoSelectOrder;
			orderCount = autoSelectOrderCount;
		}

		if (orderList == null) {
			return null;
		}

		GameSprite result = null;
		GameSprite current = null;
		boolean isSelectAll = GameRole.isSelectAllMode();
		for (int i = 0; i < orderCount; i += 4) {
			int type = orderList[i];
			int idx = orderList[i + 1];

			if (orderList[i + 2] > GameMain.autoSelectDistance) {
				continue;
			}

			current = transSelectTarget(type, idx);
			current = checkTarget(current);

			if (current != null) {
				if(isSelectAll){//全部模式
					result = current;
					break;
				} else {//PK模式
					//当使用切换键将过滤掉所有我方玩家目标，我方玩家目标无法被选中。
					if(GameRole.isCivilPlayer(current)){
						continue;
					}
					result = current;
					break;
				}

			}
		}

		return result;
	}
	
	/**
	 * 搜最近的功能NPC
	 * @return
	 */
	
	public GameSprite findNearNPC() {
		GameWorld.player.clearTarget();
		short[] orderList = forceSelectOrder;
		int orderCount = forceSelectOrderCount;

		if (orderList == null) {
			return null;
		}

		GameSprite result = null;
		GameSprite current = null;
		boolean isSelectAll = GameRole.isSelectAllMode();
		for (int i = 0; i < orderCount; i += 4) {
			int type = orderList[i];
			int idx = orderList[i + 1];
			if(type != Tool.DRAW_ITEMS_NPC){
				continue;
			}
			if (orderList[i + 2] > GameMain.autoSelectDistance) {
				continue;
			}

			current = transSelectTarget(type, idx);
//			int[] npcInfo = (int[])current.readGameData("npc_data");
//			if(npcInfo[1] != 1){//非功能NPC
//				continue;
//			}
			if(current != null && current.canAttack){
				continue;
			}
			current = checkTarget(current);

			if (current != null) {
				if(isSelectAll){//全部模式
					result = current;
					break;
				} else {//PK模式
					//当使用切换键将过滤掉所有我方玩家目标，我方玩家目标无法被选中。
					if(GameRole.isCivilPlayer(current)){
						continue;
					}
					result = current;
					break;
				}

			}
		}

		return result;
	}
	
	public GameSprite findNextTarget(GameSprite oldTarget) {
		if (forceSelectOrder == null) {
			return null;
		}

		GameSprite result = null;
		GameSprite current = null;
		int oldIndex = -4;
		boolean isSelectAll = GameRole.isSelectAllMode();
		if (oldTarget != null) {
			for (int i = 0; i < forceSelectOrderCount; i += 4) {
				int type = forceSelectOrder[i];
				int idx = forceSelectOrder[i + 1];
				current = transSelectTarget(type, idx);
				current = checkTarget(current);

				if (current == oldTarget) {
					oldIndex = i;

					break;
				}
			}
		} else {
			oldIndex = -4;
		}

		for (int i = oldIndex + 4; i < forceSelectOrderCount; i += 4) {
			int type = forceSelectOrder[i];
			int idx = forceSelectOrder[i + 1];
			current = transSelectTarget(type, idx);
			current = checkTarget(current);

			if (current != null) {
				if(isSelectAll){//全部模式
					result = current;
					break;
				} else {//PK模式
					//当使用切换键将过滤掉所有我方玩家目标，我方玩家目标无法被选中。
					if(GameRole.isCivilPlayer(current)){
						continue;
					}
					result = current;
					break;
				}

			}

		}

		if (result == null) {
			for (int i = 0; i < oldIndex; i += 4) {
				int type = forceSelectOrder[i];
				int idx = forceSelectOrder[i + 1];
				current = transSelectTarget(type, idx);
				current = checkTarget(current);

				if (current != null) {
					if(isSelectAll){//全部模式
						result = current;
						break;
					} else {//PK模式
						//当使用切换键将过滤掉所有我方玩家目标，我方玩家目标无法被选中。
						if(GameRole.isCivilPlayer(current)){
							continue;
						}
						result = current;
						break;
					}

				}
			}
		}

		return result;
	}
	
	/**
	 * 搜最近的可攻击的怪
	 * @return
	 */
	public GameSprite findNearCreature() {
		GameWorld.player.clearTarget();
		short[] orderList = forceSelectOrder;
		int orderCount = forceSelectOrderCount;

		if (orderList == null) {
			return null;
		}
		GameSprite current = null;
		for (int i = 0; i < orderCount; i += 4) {
			int type = orderList[i];
			int idx = orderList[i + 1];
			if(type != Tool.DRAW_ITEMS_NPC){
				continue;
			}
			if (orderList[i + 2] > GameMain.autoSelectDistance) {
				continue;
			}
			//current = transSelectTarget(type, idx);
			
			if(type == Tool.DRAW_ITEMS_NPC){
				if(idx < GameWorld.gameSprites.size()){
					current = (GameSprite) GameWorld.gameSprites.elementAt(idx);
					if(current != null && !current.die && current.canAttack){
						return current;
					}
				}
			}
		}
		return current;
	}

	private GameSprite transSelectTarget(int type, int idx) {
		GameSprite result = null;

		try {
			switch (type) {
			case Tool.DRAW_ITEMS_PLAYER:
			case Tool.DRAW_ITEMS_NPC:
				result = (GameSprite) GameWorld.gameSprites.elementAt(idx);
				break;
			case Tool.DRAW_ITEMS_EXIT:
				result = (GameSprite) GameWorld.gameExits.elementAt(idx);
				break;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
		}
		return result;
	}

	public GameSprite checkTarget(GameSprite target) {
		GameSprite result = target;

		if (result != null && result.canSelect) {
			switch (result.getType()) {
			case Tool.SPRITE_TYPE_GATHER_NPC: {
				int questId = ((GameNpc) result).questId;

				switch (questId) {
				case -1: // 系统设置为非任务采集，默认可选
					break;
				case -2: // 采集数据未下载到
					result = null;
					break;
				default:// 没有相关采集任务，或采集任务已为可完成状态，则不可选择
					Quest quest = Quest.findQuest(questId, false);
					if (quest == null || quest.state == Quest.QUEST_STATE_CAN_FINISH
							|| quest.state == Quest.QUEST_STSATE_CAN_ACCEPT) {
						result = null;
					}
					break;
				}
			}
				break;
			case Tool.SPRITE_TYPE_NPC:
			case Tool.SPRITE_TYPE_PLAYER: {
				if (result.die && result.canAttack) {
					result = null;
				}
			}
				break;
			case Tool.SPRITE_TYPE_ATTENDANT:
				result = null;
				break;
			}
		} else {
			result = null;
		}
		return result;
	}

	private void processNearSprite() {
	    int[] roleBox = new int[4];
	    int[] exitBox = new int[4];
	    int[] spriteBox = new int[4];
	    
		roleBox = GameWorld.player.sprite.getCollisionBox(roleBox, false);
		boolean spriteFound = false;

		for (int i = 0; i < autoSelectOrderCount; i += 4) {
			int type = autoSelectOrder[i];
			int idx = autoSelectOrder[i + 1];

			switch (type) {
			case Tool.DRAW_ITEMS_EXIT: {
				GameExit gameExit = (GameExit) GameWorld.gameExits.elementAt(idx);

				if (gameExit != null) {
					exitBox = gameExit.sprite.getCollisionBox(exitBox, false);
					//#if ModelID == AndroidAuto
			        //# if (GameMain.getUIModel().equals(GameMain.ANDROID_LARGE))
			    	//# {
					//# exitBox[0] -= 16;
					//# exitBox[2] += 32;
					//# exitBox[3] += 24;
					//# }
					//# else
					//# {
					//# exitBox[0] -= 8;
					//# exitBox[2] += 16;
					//# exitBox[3] += 12;	
					//# }
					//#elif DoubleScreen == true
					//# exitBox[0] -= 16;
					//# exitBox[2] += 32;
					//# exitBox[3] += 24;
					//#else
					exitBox[0] -= 8;
					exitBox[2] += 16;
					exitBox[3] += 12;
					//#endif

					if (autoSelectOrder[i + 2] <= GameMain.autoSelectDistance) {
						gameExit.sprite.setHeadStringShow(true);
					} else {
						gameExit.sprite.setHeadStringShow(false);
					}

					if (Tool.rectIntersect(roleBox[0], roleBox[1], roleBox[2], roleBox[3], exitBox[0], exitBox[1],
							exitBox[2], exitBox[3])) {
						if (!gameExit.touching) {
							gameExit.sendCommand(VMGame.GAME_COMMAND_SPRITE_FIRE, null);
							gameExit.touching = true;
						}
					} else {
						gameExit.touching = false;
					}
				}
			}
				break;
			case Tool.DRAW_ITEMS_NPC:
			case Tool.DRAW_ITEMS_PLAYER: {
				GameSprite gameSprite = (GameSprite) GameWorld.gameSprites.elementAt(idx);

				if (gameSprite != null && GameWorld.netplayerNameNearShow && checkTarget(gameSprite) != null) {
					spriteBox = gameSprite.sprite.getCollisionBox(spriteBox, false);
					spriteBox[3] *= 2;

					if (!spriteFound && autoSelectOrder[i + 2] <= GameMain.netplayerShowNameDistance) {
						if (gameSprite != GameWorld.player.target) {
							gameSprite.sprite.setHeadStringShow(true);
						}

						spriteFound = true;
					} else {
						if (gameSprite != GameWorld.player.target) {
							gameSprite.sprite.setHeadStringShow(false);
						}
					}
				}
			}
				break;
			case Tool.DRAW_ITEMS_ATTENDANT:
				GameSprite gameSprite = (GameSprite) GameWorld.gameSprites.elementAt(idx);
				gameSprite.sprite.setHeadStringShow(true);
				break;
			}
		}
	}

	public int collisionMap(int x, int y, int w, int h, int direct, int step, int oldX, int oldY, int currentResult) {
		int result = currentResult;

		int oldResult;
		int x1 = 0, y1 = 0, w1 = 0, h1 = 0;
		
		int startX = 0, startY = 0, endX = 0, endY = 0;

		switch(direct){
		    case Tool.DIR_DOWN:
		        startX = x;
	            endX = x + w;
	            startY = oldY + h;
	            endY = y + h;
	            break;
		    case Tool.DIR_RIGHT:
		        startX = oldX + w;
	            endX = x + w;
	            startY = y;
	            endY = y + h;
	            break;
		    case Tool.DIR_LEFT:
		        startX = x;
	            endX = oldX;
	            startY = y;
	            endY = y + h;
	            break;
		    case Tool.DIR_UP:
		        startX = x;
	            endX = x + w;
	            startY = y;
	            endY = oldY;
		        break;
		}

//		int startX = x / pathTileWidth;
//		int startY = y / pathTileHeight;
//		int endX = (x + w + pathTileWidth - 1) / pathTileWidth;
//		int endY = (y + h + pathTileHeight - 1) / pathTileHeight;
		startX = startX / pathTileWidth;
		startY = startY / pathTileHeight;
		endX = endX / pathTileWidth;
		endY = endY / pathTileHeight;

		if (startX < 0) {
			startX = 0;
		}

		if (startY < 0) {
			startY = 0;
		}

		if (endX >= pathTileXCount) {
			endX = pathTileXCount - 1;
		}

		if (endY >= pathTileYCount) {
			endY = pathTileYCount - 1;
		}

		for (int i = startY; i <= endY; i++) {
			for (int j = startX; j <= endX; j++) {
				oldResult = result;
				x1 = j * pathTileWidth;
				y1 = i * pathTileHeight;
				w1 = pathTileWidth;
				h1 = pathTileHeight;

//				if (!Tool.rectIntersect(x1, y1, w1, h1, x, y, w, h)) {
//					continue;
//				}

				int testx = j;
				int ptx = testx & 0x07;
				testx >>= 3;
				int testy = i;

				if (((mapCollisonData[testy][testx] >> ptx) & 0x01) == MAP_NOT_PASS) {
					result = Tool.calculateDistance(x1, y1, w1, h1, oldX, oldY, w, h, direct);
				}

				result = Math.min(result, oldResult);
			}
		}

		return result;
	}

	public boolean canMove(int x, int y) {
		int testx = getTileX(x) << PATH_SHIFT;
		int ptx = testx & 0x07;
		testx >>= 3;
		int testy = getTileY(y) << PATH_SHIFT;

		if (testx >= 0 && testx < mapCollisonData[0].length && testy >= 0 && testy < mapCollisonData.length) {
			return ((mapCollisonData[testy][testx] >> ptx) & 0x01) == MAP_CAN_PASS;
		}

		return false;
	}

	public int collisionYOrder(int x, int y, int w, int h, int direct, int step, int oldX, int oldY, int currentResult) {
		int result = currentResult;

		int oldResult, idx, animateId;
		int[] box = new int[4];
		
		for (int i = 0; i < yOrderCount; i += 4) {
			oldResult = result;
			idx = yOrder[i + 1];
			short[][] npcData;

			switch (yOrder[i]) {
			case Tool.DRAW_ITEMS_GROUND_MAPNPC:
			case Tool.DRAW_ITEMS_ROLE_MAPNPC: {
				if (yOrder[i] == Tool.DRAW_ITEMS_GROUND_MAPNPC) {
					npcData = map.groundNPCs;
				} else {
					npcData = map.roleNPCs;
				}

				animateId = npcData[0][idx];

				int count = mapNpcCollision[animateId].length >> 1;

				for (int j = 0; j < count; j++) {
					oldResult = result;

					box[0] = (short) (mapNpcCollision[animateId][(j << 1)] >> 16) + npcData[1][idx];
					box[1] = (short) (mapNpcCollision[animateId][(j << 1)] & 0xFFFF) + npcData[2][idx];
					box[2] = mapNpcCollision[animateId][(j << 1) + 1] >> 16;
					box[3] = mapNpcCollision[animateId][(j << 1) + 1] & 0xFFFF;

					if (Tool.rectIntersect(box[0], box[1], box[2], box[3], x, y, w, h)) {
						result = Tool.calculateDistance(box[0], box[1], box[2], box[3], oldX, oldY, w, h, direct);
					}

					result = Math.min(result, oldResult);
				}
			}
				break;
			case Tool.DRAW_ITEMS_NPC: {
				GameNpc npcSprite = (GameNpc) GameWorld.gameSprites.elementAt(idx);

				if (npcSprite.needCollision) {
					box = npcSprite.sprite.getCollisionBox(box, true);

					if (Tool.rectIntersect(box[0], box[1], box[2], box[3], x, y, w, h)) {
						result = Tool.calculateDistance(box[0], box[1], box[2], box[3], oldX, oldY, w, h, direct);
					}

					result = Math.min(result, oldResult);
				}
			}
			}
		}

		return result;
	}

	public static void initMiniMapConfig(UASegment segment) {
		thumbColors = segment.readInts();
		miniMapConfig = segment.readInts();
	}

	//#if AlphaMethod == rgbimage
	private static Image miniMapImage = null;

	//#endif
	public void drawMiniMap(Graphics g, int x, int y) {
		if (mapDataBufferReleased) {
			return;
		}

		int iconSize = miniMapConfig[MINI_MAP_CONIFG_ICON_SIZE];

		g.setColor(miniMapConfig[MINI_MAP_CONFIG_BOX1_COLOR]);
		//#if ModelID == Lenovo || ModelID == Android || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad
		//# int drawMiniMapW = Math.min(miniMapMaxWidth, miniMapWidth);
		//# int drawMiniMapH = Math.min(miniMapMaxHeight, miniMapHeight);
		//# if(drawMiniMapW < miniMapWidth) {
		//# 	x += miniMapWidth - drawMiniMapW;
		//# }
		//#
		//# g.drawRect(x, y, drawMiniMapW - 1, drawMiniMapH - 1);
		//# g.setColor(miniMapConfig[MINI_MAP_CONFIG_BOX2_COLOR]);
		//#
		//# g.drawRect(x + 1, y + 1, drawMiniMapW - 3, drawMiniMapH - 3);
		//#elif ModelID == AndroidAuto
		//# int drawMiniMapW = Math.max(miniMapMaxWidth, miniMapWidth);
		//# int drawMiniMapH = Math.max(miniMapMaxHeight, miniMapHeight);
	 	//# if (!GameMain.getUIModel().equals(GameMain.ANDROID_SMALL))
    	//# {
		 //# drawMiniMapW = Math.max(miniMapMaxWidth, miniMapWidth);
		 //# drawMiniMapH = Math.max(miniMapMaxHeight, miniMapHeight);
		 //# if(drawMiniMapW < miniMapWidth) {
		 	//# x += miniMapWidth - drawMiniMapW;
		 //# }
		//#
		 //# g.drawRect(x, y, miniMapWidth - 1, miniMapHeight - 1);
		 //# g.setColor(miniMapConfig[MINI_MAP_CONFIG_BOX2_COLOR]);
		//#
		 //# g.drawRect(x + 1, y + 1, miniMapWidth - 3, miniMapHeight - 3);
		 //# }
		//# else
		//# {
		//# g.drawRect(x, y, miniMapWidth - 1, miniMapHeight - 1);
		//# g.setColor(miniMapConfig[MINI_MAP_CONFIG_BOX2_COLOR]);
		//# g.drawRect(x + 1, y + 1, miniMapWidth - 3, miniMapHeight - 3);	
		//# }
		//#else
		g.drawRect(x, y, miniMapWidth - 1, miniMapHeight - 1);
		g.setColor(miniMapConfig[MINI_MAP_CONFIG_BOX2_COLOR]);
		g.drawRect(x + 1, y + 1, miniMapWidth - 3, miniMapHeight - 3);
		//#endif

		x += 2;
		y += 2;

		int drawWidth = miniMapWidth - 4;
		int drawHeight = miniMapHeight - 4;
		int iconXRevise = tileXCount * tileWidth;
		int iconYRevise = tileYCount * tileHeight;
		int iconSizeRevise = iconSize / 2;

		//#if ModelID == Lenovo || ModelID == Android || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad
		//# if(drawMiniMapW < miniMapWidth || drawMiniMapH < miniMapHeight){
		//#  int px = GameWorld.player.sprite.getX() * drawWidth / iconXRevise - iconSizeRevise;
		//#  int py = GameWorld.player.sprite.getY() * drawHeight / iconYRevise - iconSizeRevise;
		//#  int nx = px - drawMiniMapW/2;
		//#  int ny = py - drawMiniMapH/2;
		//#  if(nx<0){
		//#  nx = 0;
		//#  }
		//#  if(ny<0){
		//#  ny = 0;
		//#  }
		//#  if(nx + drawMiniMapW > miniMapWidth){
		//#  nx = miniMapWidth - drawMiniMapW;
		//#  }
		//#  if(ny + drawMiniMapH > miniMapHeight){
		//#  ny = miniMapHeight - drawMiniMapH;
		//#  }
		//#  x -= nx;
		//#  y -= ny;
		//#  }
		//#elif ModelID == AndroidAuto
    	//# if (!GameMain.getUIModel().equals(GameMain.ANDROID_SMALL))
    	//# {
		 //# if(drawMiniMapW < miniMapWidth || drawMiniMapH < miniMapHeight){
		 //# int px = GameWorld.player.sprite.getX() * drawWidth / iconXRevise - iconSizeRevise;
		 //# int py = GameWorld.player.sprite.getY() * drawHeight / iconYRevise - iconSizeRevise;
		 //# int nx = px - drawMiniMapW/2;
		 //# int ny = py - drawMiniMapH/2;
		 //# if(nx<0){
		 //# nx = 0;
		 //# }
		 //# if(ny<0){
		 //# ny = 0;
		 //# }
		 //# if(nx + drawMiniMapW > miniMapWidth){
		 //# nx = miniMapWidth - drawMiniMapW;
		 //# }
		 //# if(ny + drawMiniMapH > miniMapHeight){
		 //# ny = miniMapHeight - drawMiniMapH;
		 //# }
		 //# x -= nx;
		 //# y -= ny;
		 //# }
		//# }
		//#endif

		if (miniMapConfig[MINI_MAP_CONIFG_ALPHA] == 0xFF000000) {
			g.drawRGB(miniMapData, 0, drawWidth, x, y, drawWidth, drawHeight, false);
		} else if (miniMapConfig[MINI_MAP_CONIFG_ALPHA] != 0x00000000) {
			//#if (AlphaMethod == rgbimage) && (ModelID != Nokia7610)
			if (miniMapImage == null) {
				miniMapImage = Image.createRGBImage(miniMapData, drawWidth, drawHeight, true);
			}
			g.drawImage(miniMapImage, x, y, Graphics.TOP | Graphics.LEFT);
			//#else
			//# g.drawRGB(miniMapData, 0, drawWidth, x, y, drawWidth, drawHeight, true);
			//#endif
		}
		// 画网络状态

		g.setColor(miniMapConfig[currNetColor + 16]);
		g.fillRect(x - 1, y + drawHeight + 2, miniMapConfig[MINI_MAP_CONIFG_NET_WIDTH] == 0 ? miniMapWidth - 2
				: miniMapConfig[MINI_MAP_CONIFG_NET_WIDTH], miniMapConfig[MINI_MAP_CONIFG_NET_HEIGHT]);

		int count = GameWorld.gameSprites.size();

		for (int i = 0; i < count; i++) {
			GameSprite gameSprite = (GameSprite) GameWorld.gameSprites.elementAt(i);
			int iconX = gameSprite.sprite.getX() * drawWidth / iconXRevise;
			int iconY = gameSprite.sprite.getY() * drawHeight / iconYRevise;
			if (gameSprite.miniMapShow && gameSprite.sprite.getMapId() == map.id) {
				if (gameSprite.miniMapImage == null) {// 任务标识：矩形
					g.setColor(gameSprite.miniMapColor[miniMapProcData[1]]);
					g.fillRect(iconX + x - iconSizeRevise, iconY + y - iconSizeRevise, iconSize, iconSize);
				}
			}

			if (gameSprite.sprite.getMapId() == map.id) {
				if (gameSprite.miniMapImage != null) {// 任务标识：图片!,?
					ImageSet is = (ImageSet) Tool.getGlobalObject((String) gameSprite.miniMapImage[0]);
					is.drawFrame(g, ((Integer) gameSprite.miniMapImage[1]).intValue(), iconX + x - iconSizeRevise,
							iconY + y - iconSizeRevise, 0, Graphics.HCENTER | Graphics.VCENTER);
				}
			}
		}

		count = GameWorld.gameExits.size();

		for (int i = 0; i < count; i++) {
			GameSprite gameSprite = (GameSprite) GameWorld.gameExits.elementAt(i);

			if (gameSprite.miniMapShow) {
				int iconX = gameSprite.sprite.getX() * drawWidth / iconXRevise;
				int iconY = gameSprite.sprite.getY() * drawHeight / iconYRevise;
				g.setColor(gameSprite.miniMapColor[miniMapProcData[1]]);
				g.fillRect(iconX + x - iconSizeRevise, iconY + y - iconSizeRevise, iconSize, iconSize);
			}
		}

		//#if ModelID == Lenovo || ModelID == Android || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad
		//# g.setClip(0, 0, GameMain.viewWidth, GameMain.viewHeight);
     	//#elif ModelID == AndroidAuto
    	//# if (!GameMain.getUIModel().equals(GameMain.ANDROID_SMALL))
    	//# {
		//# g.setClip(0, 0, GameMain.viewWidth, GameMain.viewHeight);
		//# }
		//#endif
	}

	public void cycle(int viewX, int viewY) {
		createYOrder(viewX, viewY);
		processNearSprite();

		if (miniMapShow && !mapDataBufferReleased) {
			if (GameMain.tick >= miniMapProcData[0]) {
				miniMapProcData[0] = GameMain.tick + miniMapConfig[MINI_MAP_CONIFG_FLASH];
				miniMapProcData[1]++;

				if (miniMapProcData[1] >= miniMapConfig[MINI_MAP_CONIFG_FLASH_COLOR_COUNT]) {
					miniMapProcData[1] = 0;
				}
			}
		}
	}
	
    /**
     * @brief 改变缩放比例并重新调整视图位置。
     * @param scale
     */
     
     /** 
      * @brief 根据主角的位置调整地图的可视区域 
      * @see GameView
      * @since 1.0.0
      * @note 没到Map边界时，角色用于保持在屏幕中心
      */
     public static void moveMap() {
         if (GameWorld.player == null) {
             return;
         }

         viewX = (short) (GameWorld.player.sprite.getX() - (GameView.showWidth >> 1));
         viewY = (short) (GameWorld.player.sprite.getY() - (GameView.showHeight >> 1));

         if (viewX < 0) {
             viewX = 0;
         }

         if (viewY < 0) {
             viewY = 0;
         }

         int viewMaxX = (short) ((GameWorld.gameView.map.width - GameView.showWidth) & 0xFFFF);
         int viewMaxY = (short) ((GameWorld.gameView.map.height - GameView.showHeight) & 0xFFFF);

         if (viewX > viewMaxX) {
             viewX = viewMaxX;
         }

         if (viewY > viewMaxY) {
             viewY = viewMaxY;
         }

         if (viewMaxX < 0) {
             viewX = (short) (viewMaxX / 2);
         }
         if (viewMaxY < 0) {
             viewY = (short) (viewMaxY / 2);
         }
     }

	public void draw(Graphics g, int viewX, int viewY) {
		int mx = 0;
		int my = 0;
		
		int mw = showWidth;
		int mh = showHeight;

		if (mw > map.width) {
			mx = -viewX;
			mw = map.width;
		}

		if (mh > map.height) {
			my = -viewY;
			mh = map.height;
		}

//#if opengl == true
		int clipX = 0; 
		int clipY = 0;
		int clipW = 0;
		int clipH = 0;
		//# if (!Canvas.openglMode) {
				clipX = g.getClipX();
				clipY = g.getClipY();
				clipW = g.getClipWidth();
				clipH = g.getClipHeight();
			//# g.setClip(mx, my, mw, mh);
		//# }
//#else
     g.setClip(mx, my, mw, mh);
//#endif
        
		//#if ModelID == LenovoU1 || UseMapPatch == true
		//# drawMapPatch(g, viewX , viewY);
		//#endif
		drawMap(g, viewX, viewY);
		drawItem(g, viewX, viewY);	//目前只在新手地图中用到（箭头指向）
		drawYOrder(g, viewX, viewY);
		drawPendingItems(g);
		

		if (miniMapShow) {
			drawMiniMap(g, miniMapConfig[MINI_MAP_CONIFG_X] - miniMapWidth, miniMapConfig[MINI_MAP_CONIFG_Y]);
		}
		

		
//#if opengl == true
		//# if (!Canvas.openglMode) {
			//# g.setClip(clipX, clipY, clipW, clipH);
		//# }
//#else
	  g.setClip(0, 0, GameMain.viewWidth, GameMain.viewHeight);
//#endif
		
		
	}
	
	//#if NewUI2
	//#if opengl == false
	//# public static Image minimap = null;
	//#else
	//# public static GLGraphics minimap = null;
	//#endif
	//# public static int smallMapX = 0;
	//# public static int smallMapY = 0;
	//# public static boolean showMap = false;
//# 	
	//# public static int mapR1 = 1;
	//# public static int mapR2 = 1;
//# 	
	//# public static int mapAlpha = 0xef000000;
//# 	
	//# public static int mapBoundX = 0;
	//# public static int mapBoundY = 0;
	//# public static int mapBoundW = 0;
	//# public static int mapBoundH = 0;

	//# /**
	 //# * 缓存新界面地图
	 //# */
	public void rebufferMiniMap() {
		if (yOrder == null) {
			return;
		}
		Graphics g = null;
		//#if opengl == true
		//# if (minimap != null) {
		//# 	minimap.destroy();
		//# }
		//# minimap = new GLGraphics();
		//# float scale = (float)((float)GameView.mapR1 / (float)GameView.mapR2);
		//适配非960x640机型
		//# minimap.setScale(scale * GameMain.getScale());
		//# g = minimap;
//#else
		//# 		Image minimapTmp = Image.createImage(map.width, map.height);
		//# 		g = minimapTmp.getGraphics();
//#endif
		int startX = 0;
		int startY = 0;

		// 缓存地图背景
		int endX = tileXCount;
		int endY = tileYCount;

		int viewX = 0;
		int viewY = 0;

		for (int i = startY; i < endY; i++) {
			if (i < 0 || i >= tileYCount) {
				continue;
			}
			Object lineData;
			if (map.backgroundType == 0) {
				lineData = map.mapData[i];
			} else {
				if (mapDataBufferReleased) {
					continue;
				}
				lineData = mapDataBuffer[i];
			}
			for (int j = startX; j < endX; j++) {
				if (j < 0 || j >= tileXCount) {
					continue;
				}
				int x = j * tileWidth - viewX;
				int y = i * tileHeight - viewY;
				drawMapTile(g, x, y, j, i, lineData);
			}
		}

		// 缓存地图npc
		int bgCellW = tileXCount;
		int bgCellH = tileYCount;

		int sx = 0, sy = 0;
		Vector mapNpcDrawData = new Vector();

		for (int j = startY; j <= endY; j++) {
			if (j >= 0 && j < tileYCount) {
				Object lineData;
				if (map.backgroundType == 0) {
					lineData = map.mapData[j];
				} else {
					if (mapDataBufferReleased) {
						continue;
					}
					lineData = mapDataBuffer[j];
				}
				sy = (j % bgCellH) * tileHeight;
				for (int i = startX; i <= endX; i++) {
					if (i >= 0 && i < tileXCount) {
						sx = (i % bgCellW) * tileWidth;
						drawMapTile(g, sx, sy, i, j, lineData);
						mergeMapNpcDrawBox(mapNpcDrawData, sx, sy, i, j);
					}
				}
			}
		}

		int[] box = new int[4];

		for (int i = 0; i < mapNpcDrawData.size(); i++) {
			int[] data = (int[]) mapNpcDrawData.elementAt(i);
			drawStillMapNpc(g, map.groundNPCs, data[4], data[5], data[6],
					data[7], data[0] - data[4], data[1] - data[5], box);
			drawStillMapNpc(g, map.roleNPCs, data[4], data[5], data[6],
					data[7], data[0] - data[4], data[1] - data[5], box);
			drawStillMapNpc(g, map.skyNPCs, data[4], data[5], data[6], data[7],
					data[0] - data[4], data[1] - data[5], box);
		}

		//缩放
		//#if ModelID == AndroidAuto
			//#if opengl == true
			//#else
		//# Image minimap0 = null;
		//# Matrix matrix = new Matrix();
		//# matrix.postScale((float)((float)mapR1 / (float)mapR2), (float)((float)mapR1 / (float)mapR2));
		//# Bitmap bitmap = Bitmap.createBitmap(minimapTmp.getBitmap(), 0, 0, minimapTmp.getWidth(), minimapTmp.getHeight(), matrix, true);
		//# minimap0 = new Image(bitmap);
		//# minimapTmp = null;
		//# minimap = minimap0;
			//#endif

		//#else

		//# int r1 = mapR1;
		//# int r2 = mapR2;
		//# Image minimap0 = zoomImage(minimapTmp, map.width * r1 / r2, map.height * r1 / r2);
		//# minimapTmp = null;

		//#endif

	}
	
	//# 	public void drawGroundMapNpcs(GLGraphics glg) {
	//# 		if (yOrder == null) {
	//# 			return;
	//# 		}
	//# 		Graphics g = null;
	//# 		g = glg;

	//# 		int startX = 0;
	//# 		int startY = 0;

		// 缓存地图背景
	//# 	int endX = tileXCount;
	//# 	int endY = tileYCount;

	//# 	int viewX = 0;
	//# 	int viewY = 0;

		// 缓存地图npc
	//# 		int bgCellW = tileXCount;
	//# 		int bgCellH = tileYCount;

	//# 	int sx = 0, sy = 0;
	//# 	Vector mapNpcDrawData = new Vector();

	//# 	for (int j = startY; j <= endY; j++) {
	//# 		if (j >= 0 && j < tileYCount) {
				//# 			Object lineData;
	//# 			if (map.backgroundType == 0) {
	//# 				lineData = map.mapData[j];
	//# 			} else {
					//# 				if (mapDataBufferReleased) {
	//# 					continue;
	//# 				}
	//# 				lineData = mapDataBuffer[j];
	//# 			}
	//# 			sy = (j % bgCellH) * tileHeight;
	//# 			for (int i = startX; i <= endX; i++) {
					//# 				if (i >= 0 && i < tileXCount) {
	//# 					sx = (i % bgCellW) * tileWidth;
	//# 					drawMapTile(g, sx, sy, i, j, lineData);
						//# 					mergeMapNpcDrawBox(mapNpcDrawData, sx, sy, i, j);
	//# 				}
	//# 			}
	//# 		}
	//# 	}

	//# 	int[] box = new int[4];

	//# 		for (int i = 0; i < mapNpcDrawData.size(); i++) {
	//# 			int[] data = (int[]) mapNpcDrawData.elementAt(i);
	//# 			drawStillMapNpc(g, map.groundNPCs, data[4], data[5], data[6],
	//# 					data[7], data[0] - data[4], data[1] - data[5], box);
	//# 		}

	//# 	}
	
	//# private Image zoomImage(Image srcImg, int desW, int desH) {
		//# int srcW = srcImg.getWidth(); // 原始图像宽
		//# int srcH = srcImg.getHeight(); // 原始图像高
		//# // 计算插值表
		//# int[] tabY = new int[desH];
		//# int[] tabX = new int[desW];
		//# int sb = 0;
		//# int db = 0;
		//# int tems = 0;
		//# int temd = 0;
		//# int distance = srcH > desH ? srcH : desH;
		//# for (int i = 0; i <= distance; i++) { /* 垂直方向 */
			//# tabY[db] = sb;
			//# tems += srcH;
			//# temd += desH;
			//# if (tems > distance) {
				//# tems -= distance;
				//# sb++;
			//# }
			//# if (temd > distance) {
				//# temd -= distance;
				//# db++;
			//# }
		//# }
		//# sb = 0;
		//# db = 0;
		//# tems = 0;
		//# temd = 0;
		//# distance = srcW > desW ? srcW : desW;
		//# for (int i = 0; i <= distance; i++) { /* 水平方向 */
			//# tabX[db] = sb;
			//# tems += srcW;
			//# temd += desW;
			//# if (tems > distance) {
				//# tems -= distance;
				//# sb++;
			//# }
			//# if (temd > distance) {
				//# temd -= distance;
				//# db++;
			//# }
		//# }
		//# System.out.println((Runtime.getRuntime().freeMemory() / 1024));
		//# // 生成放大缩小后图形像素buf
		//# Image desImg = Image.createImage(desW, desH);
		//# Graphics gs = desImg.getGraphics();
		//# int dx = 0;
		//# int oldy = -1;
		//# int[] srcBuf = new int[srcW];
		//# int[] desBuf = new int[desW];
		//# int[] lastRow = new int[desW];
		//# for (int i = 0; i < desH; i++) {
			//# if (oldy == tabY[i]) {
				//# // 当上一行与即将要生成的这一行相同时，就直接copy了
				//# System.arraycopy(lastRow, 0, desBuf, 0, desW);
			//# } else {// 插值算出新图片的一行
				//# dx = tabY[i];
				//# srcImg.getRGB(srcBuf, 0, srcW, 0, dx, srcW, 1);
				//# // 插值一行，就从原图中取一行数据
				//# for (int j = 0; j < desW; j++) {
					//# desBuf[j] = srcBuf[tabX[j]];
				//# }
				//# // dx++;
			//# }
			//# System.arraycopy(desBuf, 0, lastRow, 0, desW);
			//# oldy = tabY[i];
			//# gs.drawRGB(desBuf, 0, desW, 0, i, desW, 1, false);
		//# }
		//# return desImg;
	//# }
//# 	
	//#endif
	
	//#if ModelID == LenovoU1 || UseMapPatch == true
	//# public void rebuildMapPatchImage()
	//# {
		 //# byte[] data = GameMain.resourceManager.findResource("patch_"+String.valueOf(map.id)+".png");
		 //# if(data != null) 
		 //# {
			 //# try
			 //# {
				 //# bgPatchImg = new ImageSet(data);
			 //# }
			 //# catch(Exception e)
			 //# {
				 //# bgPatchImg = null;
				//#ifdef buildtest
	             //# e.printStackTrace();
	            //#endif 
			 //# }
		 //# }
		 //# else
		 //# {
			 //# bgPatchImg = null;
		 //# }
		 //# System.gc();
	//# }
	//#endif
	
	//#if ModelID == LenovoU1 || UseMapPatch == true
	//# public void drawMapPatch(Graphics g, int viewX , int viewY)
	//# {
		//# if(bgPatchImg != null)
		//# {
			//#if UseMapPatch == true
			//# int cx = g.getClipX();
			//# int cy = g.getClipY();
			//# int cw = g.getClipWidth();
			//# int ch = g.getClipHeight();
//# 			
			//# int w = ((bgPatchImg.getFrameWidth(0) - map.width) >> 1);
			//# int h = ((bgPatchImg.getFrameHeight(0) - map.height) >> 1);
			//# int x = -viewX - w;
			//# int y = -viewY - h;
//# 			
			//# g.setClip(0, 0, GameMain.viewWidth, GameMain.viewHeight);
			//# bgPatchImg.drawFrame(g, 0,x,y);
//# 			
			//# g.setClip(cx, cy, cw, ch);
			//#else
			//# int cx = g.getClipX();
			//# int cy = g.getClipY();
			//# int cw = g.getClipWidth();
			//# int ch = g.getClipHeight();
			//# int w = (GameMain.viewWidth - map.width) / 2;
			//# int h = (GameMain.viewHeight - map.height) / 2;
			//# if(map.width < GameMain.viewWidth && map.height < GameMain.viewHeight)
			//# {
			//# 	g.setClip(0, 0, GameMain.viewWidth, h);
			//# 	bgPatchImg.drawFrame(g, 0, 0, 0, 0, 0);
			//# 	g.setClip(0, h, w, GameMain.viewHeight - h );
			//# 	bgPatchImg.drawFrame(g, 0, 0, 0, 0, 0);
			//# 	g.setClip(w, GameMain.viewHeight - h, GameMain.viewWidth - w, h);
			//# 	bgPatchImg.drawFrame(g, 0, 0, 0, 0, 0);
			//# 	g.setClip(GameMain.viewWidth - w, h, w, GameMain.viewHeight - h * 2);
			//# 	bgPatchImg.drawFrame(g, 0, 0, 0, 0, 0);
			//# }
			//# else if(map.width < GameMain.viewWidth)
			//# {
			//# 	g.setClip(0, 0, w, GameMain.viewHeight);
			//# 	bgPatchImg.drawFrame(g, 0, 0, -viewY, 0, 0);
			//# 	g.setClip(GameMain.viewWidth - w, 0, w, GameMain.viewHeight);
			//# 	bgPatchImg.drawFrame(g, 0, 0, -viewY, 0, 0);
			//# }
			//# else if(map.height < GameMain.viewHeight)
			//# {
			//# 	g.setClip(0, 0, GameMain.viewWidth, h);
			//# 	bgPatchImg.drawFrame(g, 0, -viewX, 0, 0, 0);
			//# 	g.setClip(0, GameMain.viewHeight - h, GameMain.viewWidth, h);
			//# 	bgPatchImg.drawFrame(g, 0, -viewX, 0, 0, 0);
			//# }
			//# g.setClip(cx, cy, cw, ch);
			//#endif
		//# }
	//# }
	//#endif


	public void addPendingHeadString(String str, int x, int y, int color, int bgColor, int anchor, boolean is3D,
			boolean isTop) {
		PendingDrawItem item = new PendingDrawItem();
		item.type = PendingDrawItem.ITEM_TYPE_HEAD_STRING;
		item.objData = str;
		item.x = x;
		item.y = y;
		item.color = color;
		item.bgColor = bgColor;
		item.anchor = anchor;
		item.is3D = is3D;

		if (isTop) {
			pendingItemsTop.addElement(item);
		} else {
			pendingItems.addElement(item);
		}
	}

	public void addPendingImage(ImageSet image, int frame, int x, int y, int anchor, boolean isTop) {
		PendingDrawItem item = new PendingDrawItem();
		item.type = PendingDrawItem.ITEM_TYPE_IMAGE;
		item.objData = image;
		item.x = x;
		item.y = y;
		item.color = frame;
		item.anchor = anchor;

		if (isTop) {
			pendingItemsTop.addElement(item);
		} else {
			pendingItems.addElement(item);
		}
	}

	public void addPendingFlyString(FlyingStringInfo flyInfo, int x, int y, boolean isTop) {
		PendingDrawItem item = new PendingDrawItem();
		item.type = PendingDrawItem.ITEM_TYPE_FLY_STRING;
		item.objData = flyInfo;
		item.x = x;
		item.y = y;

		pendingItems.addElement(item);
	}

	public void addPendingAnimate(AnimatePlayer animatePlayer, int x, int y, boolean isTop) {
		PendingDrawItem item = new PendingDrawItem();
		item.type = PendingDrawItem.ITEM_TYPE_ANIMATE;
		item.objData = animatePlayer;
		item.x = x;
		item.y = y;

		if (isTop) {
			pendingItemsTop.addElement(item);
		} else {
			pendingItems.addElement(item);
		}
	}
	
	public void addPendingBubble(String[] text,int x,int y,boolean isTop){
		Bubble item = new Bubble(text,x,y);


		if (isTop) {
			pendingItemsTop.addElement(item);
		} else {
			pendingItems.addElement(item);
		}
	}

	private void drawPendingItems(Graphics g) {
		int count = pendingItems.size();

		for (int i = 0; i < count; i++) {
			PendingDrawItem item = (PendingDrawItem) pendingItems.elementAt(i);
			item.draw(g);
		}

		pendingItems.removeAllElements();

		count = pendingItemsTop.size();

		for (int i = 0; i < count; i++) {
			PendingDrawItem item = (PendingDrawItem) pendingItemsTop.elementAt(i);
			item.draw(g);
		}

		pendingItemsTop.removeAllElements();
	}
	
	private void drawItem(Graphics g, int viewX, int viewY){
		try {
			int count = drawItemPanel.size();
			for (int i = 0; i < count; i++) {
				DrawItem item = (DrawItem) drawItemPanel.getValue(i);
				item.draw(g, viewX, viewY);
			}
		} catch (Exception e) {
			//#ifdef buildtest
			e.printStackTrace();
			//#endif
		}
		
	}

	private void addMapNpcDirtyData(int[] box, int viewX, int viewY) {
		box[0] -= viewX + 8;
		box[1] -= viewY + 8;
		box[2] += 16;
		box[3] += 16;

		int size = mapNpcDirtyData.size();

		for (int i = 0; i < size; i++) {
			int[] tmp = (int[]) mapNpcDirtyData.elementAt(i);

			if(Tool.rectIntersect(tmp[0], tmp[1], tmp[2], tmp[3], box[0], box[1], box[2], box[3])){
			    Tool.mergeBox(tmp, box);
			}
		}

		int[] arr = new int[4];
		System.arraycopy(box, 0, arr, 0, 4);
		mapNpcDirtyData.addElement(arr);
	}

	private Vector getMapNpcDirtyList(int[] box, int viewX, int viewY) {
		Vector dirtyList = new Vector();
		box[0] -= viewX;
		box[1] -= viewY;
		int size = mapNpcDirtyData.size();

		for (int i = 0; i < size; i++) {
			int[] dirty = (int[]) mapNpcDirtyData.elementAt(i);

			if (Tool.rectIntersect(box[0], box[1], box[2], box[3], dirty[0], dirty[1], dirty[2], dirty[3])){
				dirtyList.addElement(dirty);
			}
		}

		return dirtyList.size() == 0 ? null : dirtyList;
	}

	private void drawYOrder(Graphics g, int viewX, int viewY) {
		//#if ModelID == LenovoU1
		//# int x = g.getClipX();
		//# int y = g.getClipY();
		//# int w = g.getClipWidth();
		//# int h = g.getClipHeight();
		//# g.setClip(0, 0, GameMain.viewWidth, GameMain.viewHeight);
		//#endif
		
		short type, idx;
		short[][] npcData = null;

		if (yOrder == null) {
			return;
		}

		mapNpcDirtyData.removeAllElements();
		int[] npcBox = new int[4];
		
		for (int i = 0; i < yOrderCount; i += 4) {
			type = yOrder[i];
			idx = yOrder[i + 1];

			switch (type) {
			case Tool.DRAW_ITEMS_ROLE:
				GameWorld.player.draw(g, viewX, viewY);
				addMapNpcDirtyData(GameWorld.player.sprite.getAnimateBox(), viewX, viewY);

				break;
			case Tool.DRAW_ITEMS_EXIT:
				GameExit gameExit = (GameExit) GameWorld.gameExits.elementAt(idx);
				gameExit.draw(g, viewX, viewY);
				addMapNpcDirtyData(gameExit.sprite.getAnimateBox(), viewX, viewY);

				break;
			case Tool.DRAW_ITEMS_GROUND_MAPNPC:
			case Tool.DRAW_ITEMS_ROLE_MAPNPC:
			case Tool.DRAW_ITEMS_SKY_MAPNPC:
				switch (type) {
				case Tool.DRAW_ITEMS_GROUND_MAPNPC:
					npcData = map.groundNPCs;
					break;
				case Tool.DRAW_ITEMS_ROLE_MAPNPC:
					npcData = map.roleNPCs;

					break;
				case Tool.DRAW_ITEMS_SKY_MAPNPC:
					npcData = map.skyNPCs;

					break;
				}
//#if opengl == true
				//# if (Canvas.openglMode) {
				//# if(type != Tool.DRAW_ITEMS_GROUND_MAPNPC){
						//# drawMapNpc(g, viewX, viewY, npcData, idx, true);
				//# }
					
				//# } else {
//#endif
					int animateId = npcData[0][idx];
					npcBox = mapNpcAnimateSet.getAnimateBox(npcBox, animateId);
					npcBox[0] += npcData[1][idx];
					npcBox[1] += npcData[2][idx];
	
					// 非动画npc在buffer上画不在yorder里画
					if (!showMapNpcAnimate || (mapNpcAnimateSet.getAnimateLength(animateId) <= 1 && useImageBuffer)) {
						// 人物层和天空层NPC在底部有其他精灵被绘制后，在yorder里重绘
						if (type != Tool.DRAW_ITEMS_GROUND_MAPNPC) {
							Vector drawData = getMapNpcDirtyList(npcBox, viewX, viewY);
	
							if (drawData != null) {
								int size = drawData.size() - 1;
	
								for (int j = 0; j <= size; j++) {
									int[] r = (int[]) drawData.elementAt(j);
									g.setClip(r[0], r[1], r[2], r[3]);
	
									if (j == size) {
										drawMapNpc(g, viewX, viewY, npcData, idx, false && !showMapNpcAnimate);
									} else {
										drawMapNpc(g, viewX, viewY, npcData, idx, true && !showMapNpcAnimate);
									}
								}
	
								g.setClip(0, 0, GameMain.viewWidth, GameMain.viewHeight);
							}
						}
					} else {
						addMapNpcDirtyData(npcBox, viewX, viewY);
						drawMapNpc(g, viewX, viewY, npcData, idx, true);
					}
//#if opengl == true				
				//# }
//#endif
				break;
			case Tool.DRAW_ITEMS_PLAYER:
			case Tool.DRAW_ITEMS_NPC: 
			case Tool.DRAW_ITEMS_ATTENDANT:{
				GameSprite drawSprite = (GameSprite) GameWorld.gameSprites.elementAt(idx);
				drawSprite.draw(g, viewX, viewY);
				addMapNpcDirtyData(drawSprite.sprite.getAnimateBox(), viewX, viewY);
			}
				break;
			case Tool.DRAW_ITEMS_LEAVING_SPRITE: {
				if (idx < GameWorld.leavingSprites.size()) {
					GameSprite drawSprite = (GameSprite) GameWorld.leavingSprites.elementAt(idx);
					drawSprite.draw(g, viewX, viewY);
					addMapNpcDirtyData(drawSprite.sprite.getAnimateBox(), viewX, viewY);
				}
			}
				break;

			}
		}
		//#if ModelID == LenovoU1
		//# g.setClip(x, y, w, h);
		//#endif
	}

	private void createYOrder(int viewX, int viewY) {
		int yOrderPoint = 0;
		int autoSelectOrderPoint = 0;
		int forceSelectOrderPoint = 0;
		int count = 0;
		int dx;
		int dy;
		int distance;
		int playerX = GameWorld.player.sprite.getX();
		int playerY = GameWorld.player.sprite.getY();
		int viewWidth = GameMain.viewWidth;
		int viewHeight = GameMain.viewHeight;

		yOrder[yOrderPoint++] = Tool.DRAW_ITEMS_ROLE;
		yOrder[yOrderPoint++] = 0;
		yOrder[yOrderPoint++] = (short) GameWorld.player.sprite.getY();
		yOrder[yOrderPoint++] = (short) GameWorld.player.sprite.getX();

		for (int i = 0; i < GameWorld.gameExits.size(); i++) {
			GameExit gameExit = (GameExit) GameWorld.gameExits.elementAt(i);

			int[] box = gameExit.sprite.getAnimateBox();

			if (Tool.rectIntersect(box[0], box[1], box[2], box[3], viewX, viewY, viewWidth, viewHeight)) {
				count++;

				yOrder[yOrderPoint++] = Tool.DRAW_ITEMS_EXIT;
				yOrder[yOrderPoint++] = (short) i;
				yOrder[yOrderPoint++] = (short) (gameExit.sprite.getY());
				yOrder[yOrderPoint++] = (short) gameExit.sprite.getX();

				dx = yOrder[yOrderPoint - 1];
				dy = yOrder[yOrderPoint - 2];
				distance = Tool.distance(dx, dy, playerX, playerY);

				if (distance <= GameMain.forceSelectDistance) {
					autoSelectOrder[autoSelectOrderPoint++] = Tool.DRAW_ITEMS_EXIT;
					autoSelectOrder[autoSelectOrderPoint++] = (short) i;
					autoSelectOrder[autoSelectOrderPoint++] = (short) Tool.distance(dx, dy, playerX, playerY);
					autoSelectOrder[autoSelectOrderPoint++] = 0;
					forceSelectOrder[forceSelectOrderPoint++] = Tool.DRAW_ITEMS_EXIT;
					forceSelectOrder[forceSelectOrderPoint++] = (short) i;
					forceSelectOrder[forceSelectOrderPoint++] = (short) Tool.distance(dx, dy, playerX, playerY);
					forceSelectOrder[forceSelectOrderPoint++] = 0;
				}
			}
		}

		short[][] npcData = null;
		short drawNpcType = Tool.DRAW_ITEMS_ROLE_MAPNPC;
		int drawNpcOffset = 0;

		for (int j = 0; j < 3; j++) {
			switch (j) {
			case 0:
				npcData = map.groundNPCs;
				drawNpcType = Tool.DRAW_ITEMS_GROUND_MAPNPC;
				drawNpcOffset = -map.height * 2;

				break;
			case 1:
				npcData = map.roleNPCs;
				drawNpcType = Tool.DRAW_ITEMS_ROLE_MAPNPC;
				drawNpcOffset = 0;

				break;
			case 2:
				npcData = map.skyNPCs;
				drawNpcType = Tool.DRAW_ITEMS_SKY_MAPNPC;
				drawNpcOffset = map.height;

				break;
			}

			int[] npcBox = new int[4];
			
			for (int i = 0; i < npcData[0].length; i++) {
				int animateId = npcData[0][i];

				npcBox = mapNpcAnimateSet.getAnimateBox(npcBox, animateId);
				npcBox[0] += npcData[1][i];
				npcBox[1] += npcData[2][i];

				if (Tool.rectIntersect(npcBox[0], npcBox[1], npcBox[2], npcBox[3], viewX, viewY, viewWidth, viewHeight)){
					count++;

					yOrder[yOrderPoint++] = drawNpcType;
					yOrder[yOrderPoint++] = (short) i;
					yOrder[yOrderPoint++] = (short) (npcData[2][i] + drawNpcOffset);
					yOrder[yOrderPoint++] = (short) npcData[1][i];
				}
			}
		}

		int netplayerCount = 0;
		int[] collisionBox = new int[4];
		
		for (int i = 0; i < GameWorld.gameSprites.size(); i++) {
			GameSprite gameSprite = (GameSprite) GameWorld.gameSprites.elementAt(i);
			int spriteType = gameSprite.getType();

			switch (spriteType) {
			case Tool.SPRITE_TYPE_PLAYER:
			case Tool.SPRITE_TYPE_NPC:
			case Tool.SPRITE_TYPE_GATHER_NPC:
			case Tool.SPRITE_TYPE_ATTENDANT:
			    collisionBox = gameSprite.sprite.getCollisionBox(collisionBox, false);

				if (Tool.rectIntersect(collisionBox[0], collisionBox[1], collisionBox[2], collisionBox[3], viewX, viewY, viewWidth, viewHeight)
						&& gameSprite.sprite.getMapId() == map.id
						&& gameSprite.sprite.getMapInstanceId() == GameWorld.player.sprite.getMapInstanceId()) {
					if (spriteType == Tool.SPRITE_TYPE_PLAYER) {
						if (netplayerCount >= GameMain.netplayerShowMaxCount) {
							((GameNetPlayer) gameSprite).beSkiped = true;

							continue;
						} else {
							((GameNetPlayer) gameSprite).beSkiped = false;
						}

						netplayerCount++;
						yOrder[yOrderPoint++] = Tool.DRAW_ITEMS_PLAYER;
					} else if(spriteType == Tool.SPRITE_TYPE_ATTENDANT){
						yOrder[yOrderPoint++] = Tool.DRAW_ITEMS_ATTENDANT;
					} else {
						yOrder[yOrderPoint++] = Tool.DRAW_ITEMS_NPC;
					}

					count++;

					yOrder[yOrderPoint++] = (short) i;
					yOrder[yOrderPoint++] = (short) gameSprite.sprite.getY();
					yOrder[yOrderPoint++] = (short) gameSprite.sprite.getX();

					dx = yOrder[yOrderPoint - 1];
					dy = yOrder[yOrderPoint - 2];
					distance = Tool.distance(dx, dy, playerX, playerY);

					if (distance <= GameMain.forceSelectDistance) {
						if (spriteType == Tool.SPRITE_TYPE_PLAYER) {
							autoSelectOrder[autoSelectOrderPoint++] = Tool.DRAW_ITEMS_PLAYER;
							forceSelectOrder[forceSelectOrderPoint++] = Tool.DRAW_ITEMS_PLAYER;
						} else if(spriteType == Tool.SPRITE_TYPE_ATTENDANT){
							autoSelectOrder[autoSelectOrderPoint++] = Tool.DRAW_ITEMS_ATTENDANT;
							forceSelectOrder[forceSelectOrderPoint++] = Tool.DRAW_ITEMS_ATTENDANT;
						} else {
							autoSelectOrder[autoSelectOrderPoint++] = Tool.DRAW_ITEMS_NPC;
							forceSelectOrder[forceSelectOrderPoint++] = Tool.DRAW_ITEMS_NPC;
						}

						autoSelectOrder[autoSelectOrderPoint++] = (short) i;
						forceSelectOrder[forceSelectOrderPoint++] = (short) i;

						int dist = Tool.distance(dx, dy, playerX, playerY);

						if (spriteType == Tool.SPRITE_TYPE_NPC || spriteType == Tool.SPRITE_TYPE_GATHER_NPC) {
							if (gameSprite.canAttack) {
								autoSelectOrder[autoSelectOrderPoint++] = (short) (dist - GameRole.enemyNpcAutoDist);
								forceSelectOrder[forceSelectOrderPoint++] = (short) (dist - GameRole.enemyNpcForceDist);
							} else {
								autoSelectOrder[autoSelectOrderPoint++] = (short) (dist - GameRole.allyNpcAutoDist);
								forceSelectOrder[forceSelectOrderPoint++] = (short) (dist - GameRole.allyNpcForceDist);
							}
						} else {
							if (gameSprite.canAttack) {
								autoSelectOrder[autoSelectOrderPoint++] = (short) (dist - GameRole.enemyPlayerAutoDist);
								forceSelectOrder[forceSelectOrderPoint++] = (short) (dist - GameRole.enemyPlayerForceDist);
							} else {
								if (GameWorld.teamInfo.get(new Integer(gameSprite.getInstanceId())) == null) {
									autoSelectOrder[autoSelectOrderPoint++] = (short) (dist - GameRole.allyPlayerAutoDist);
									forceSelectOrder[forceSelectOrderPoint++] = (short) (dist - GameRole.allyPlayerForceDist);
								} else {
									autoSelectOrder[autoSelectOrderPoint++] = (short) (dist - GameRole.teamerAutoDist);
									forceSelectOrder[forceSelectOrderPoint++] = (short) (dist - GameRole.teamerForceDist);
								}
							}
						}

						autoSelectOrder[autoSelectOrderPoint++] = 0;
						forceSelectOrder[forceSelectOrderPoint++] = 0;
					}
				}

				break;
			}
		}

		for (int i = 0; i < GameWorld.leavingSprites.size(); i++) {
			GameSprite gameSprite = (GameSprite) GameWorld.leavingSprites.elementAt(i);

			collisionBox = gameSprite.sprite.getCollisionBox(collisionBox, false);

			if (Tool.rectIntersect(collisionBox[0], collisionBox[1], collisionBox[2], collisionBox[3], viewX, viewY, viewWidth, viewHeight)) {
				yOrder[yOrderPoint++] = Tool.DRAW_ITEMS_LEAVING_SPRITE;
				yOrder[yOrderPoint++] = (short) i;
				yOrder[yOrderPoint++] = (short) gameSprite.sprite.getY();
				yOrder[yOrderPoint++] = (short) gameSprite.sprite.getX();
			}
		}

		if (count == -1) {
			yOrderCount = 0;
			autoSelectOrderCount = 0;
		} else {
			yOrderCount = yOrderPoint;
			sort(yOrder, (yOrderCount >> 2));

			autoSelectOrderCount = autoSelectOrderPoint;
			sort(autoSelectOrder, (autoSelectOrderCount >> 2));
			forceSelectOrderCount = forceSelectOrderPoint;
			sort(forceSelectOrder, (forceSelectOrderCount >> 2));
		}
	}

	private void sort(short[] items, int itemsCount) {
		int h, i, j, t;
		short[] temp = new short[4];
		int n = itemsCount;

		for (t = 7; t < 17 && sortTable[t] <= n / 9; t++) {
		}

		for (; t >= 0; t--) {
			h = sortTable[t];

			for (i = h; i < n; i++) {
				int id = i << 2;
				System.arraycopy(items, id, temp, 0, 4);

				for (j = i - h; j >= 0
						&& ((items[(j << 2) + 2] - temp[2]) == 0 ? (items[(j << 2) + 3] - temp[3])
								: (items[(j << 2) + 2] - temp[2])) > 0; j -= h) {
					int id1 = (j + h) << 2;
					int id2 = j << 2;

					System.arraycopy(items, id2, items, id1, 4);
				}

				id = (j + h) << 2;
				System.arraycopy(temp, 0, items, id, 4);
			}
		}
	}

	private void drawMapNpc(Graphics g, int viewX, int viewY, short[][] npcData, int idx, boolean changeFrame) {
		int animateId = npcData[0][idx];
		int x = npcData[1][idx] - viewX;
		int y = npcData[2][idx] - viewY;
		int frame = npcData[3][idx];

		mapNpcAnimateSet.drawAnimateFrame(g, animateId, frame, x, y);

		if (changeFrame) {
			if((GameMain.tick & 0x1) == 0x0){
				frame++;
			}

			if (frame >= mapNpcAnimateSet.getAnimateLength(animateId)) {
				frame = 0;
			}

			npcData[3][idx] = (short) frame;
		}
	}

	public void releaseMapDataBuffer() {
		if (!mapDataBufferReleased) {
			mapDataBufferReleased = true;
			//#if MemoryMode == "Small"
			//# mapDataBufferList = null;
			//# mapDataBuffer = null;
			//#else
			mapDataBuffer = null;
			//#endif
			miniMapData = null;
			miniMapProcData = null;
			System.gc();
		}
	}

	public void rebuildMapDataBuffer() {
		if (mapDataBufferReleased) {
			System.gc();
			switch (map.backgroundType) {
			case 1:
				makeMapDataBuffer();
				break;
			}

			rebuildMiniMap();
			mapDataBufferReleased = false;
			isFirstBgImage = true;
		}
	}

	private void makeMapDataBuffer() {
		//#if MemoryMode == "Small"
		//# int[][] tmpBuffer = map.createBlurMapBuffer(landformImages);
		//# mapDataBuffer = new Object[tmpBuffer.length];
//# 
		//# Vector tmpList = new Vector();
		//# Hashtable tmpTable = new Hashtable();
		//# boolean twoBytes = false;
		//# byte[] bline = null;
		//# short[] sline = null;
		//# int cols = tmpBuffer[0].length;
		//# aa: for (int i = 0; i < tmpBuffer.length; i++) {
			//# if (twoBytes) {
				//# sline = new short[cols];
				//# mapDataBuffer[i] = sline;
			//# } else {
				//# bline = new byte[cols];
				//# mapDataBuffer[i] = bline;
			//# }
			//# for (int j = 0; j < cols; j++) {
				//# Integer key = new Integer(tmpBuffer[i][j]);
				//# Integer idx = (Integer) tmpTable.get(key);
				//# if (idx == null) {
					//# idx = new Integer(tmpList.size());
					//# tmpList.addElement(key);
					//# tmpTable.put(key, idx);
					//# if (!twoBytes && tmpList.size() > 256) {
						//# twoBytes = true;
						//# i--;
						//# continue aa;
					//# }
				//# }
				//# if (twoBytes) {
					//# sline[j] = idx.shortValue();
				//# } else {
					//# bline[j] = idx.byteValue();
				//# }
			//# }
			//# tmpBuffer[i] = null;
		//# }
//# 
		//# mapDataBufferList = new int[tmpList.size()];
//# 
		//# for (int i = 0; i < tmpList.size(); i++) {
			//# mapDataBufferList[i] = ((Integer) tmpList.elementAt(i)).intValue();
		//# }
		//#else
		mapDataBuffer = map.createBlurMapBuffer(landformImages);
		//#endif
	}

	private void rebuildViewData() {
		try {
			switch (map.backgroundType) {
			case 0:
				tileWidth = map.owner.tileWidth;
				tileHeight = map.owner.tileHeight;
				tileImage = map.owner.loadTileImage();
				tinfo = map.owner.getTileInfo();

				break;
			case 1:
				tileWidth = map.owner.blurTileWidth;
				tileHeight = map.owner.blurTileHeight;
				landformImages = map.owner.loadAllLandformImage();
				landformTileInfos = map.owner.loadAllLandformTileInfof();

				makeMapDataBuffer();

				break;
			}

			mapDataBufferReleased = false;

			if (mapNpcAnimateNeedLoad) {
				mapNpcAnimateSet = null;
				System.gc();
				mapNpcAnimateSet = map.owner.loadNPCAnimates();
				mapNpcAnimateNeedLoad = false;
			}

			mapNpcCollision = map.owner.loadNPCCollision();
			//9.16
			//#if NewUI2
			GameWorld.currLoadMapPercent = 85;
			//#endif

			tileXCount = map.mapData[0].length;
			tileYCount = map.mapData.length;

			//9.16
			//#if NewUI2
			//#else
			rebuildMiniMap();
			//#endif

			yOrderCount = 4 * (map.exitIDs.length + map.groundNPCs[0].length + map.roleNPCs[0].length
					+ map.skyNPCs[0].length + 100);
			yOrder = new short[yOrderCount];
			autoSelectOrderCount = 4 * (map.exitIDs.length + 100);
			autoSelectOrder = new short[autoSelectOrderCount];
			forceSelectOrderCount = autoSelectOrderCount;
			forceSelectOrder = new short[forceSelectOrderCount];

			GameWorld.gameExits.removeAllElements();

			for (int i = 0; i < map.exitIDs.length; i++) {
				GameExit gameExit = GameExit.createGameExit(map.exitTargetMapNames[i], map.exitX[i], map.exitY[i], i);
				GameWorld.gameExits.addElement(gameExit);
			}

			pathTileWidth = tileWidth >> PATH_SHIFT;
			pathTileHeight = tileHeight >> PATH_SHIFT;
			pathTileXCount = tileXCount << PATH_SHIFT;
			pathTileYCount = tileYCount << PATH_SHIFT;

			rebuildMapCollisionData();
			//#if NewUI2
			//9.16
			GameWorld.currLoadMapPercent = 90;
			//#endif
			
			rebuildImageBuffer();
			
			//#if NewUI2
			//9.16
			GameWorld.currLoadMapPercent = 100;
			//#endif
			//#if ModelID == LenovoU1 || UseMapPatch == true
			//# if(map.width < GameMain.viewWidth || map.height < GameMain.viewHeight){
				//# rebuildMapPatchImage();
			//# }
			//#endif
			//#if ModelID == AndroidAuto && UseMapPatch != true
			//# needBuildEdgeMapNpc = true;
			//#endif

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void rebuildImageBuffer() {
		if (useImageBuffer) {
			bgCellW = (GameMain.viewWidth / tileWidth) + 1;
			bgCellH = (GameMain.viewHeight / tileHeight) + 1;

			if (GameMain.viewWidth % tileWidth != 0) {
				bgCellW++;
			}

			if (GameMain.viewHeight % tileHeight != 0) {
				bgCellH++;
			}

			bgWidth = bgCellW * tileWidth;
			bgHeight = bgCellH * tileHeight;
			bgImg = null;
			//#if ModelID == AndroidLarge
			//# if(bgImg == null || bgImg.getWidth() < bgWidth || bgImg.getHeight() < bgHeight )
			//# {
				//# bgImg = Image.createImage(bgWidth, bgHeight);
			//# }
			//#else
			bgImg = Image.createImage(bgWidth, bgHeight);
			//#endif
			gg = bgImg.getGraphics();
			isFirstBgImage = true;
		}
	}

	private void rebuildMapCollisionData() {
		mapCollisonData = new byte[pathTileYCount][(pathTileXCount >> 3) + 1];

		for (int i = 0; i < tileYCount; i++) {
			//#if MemoryMode == "Small"
			//# short[] sbuffer = null;
			//# byte[] bbuffer = null;
//# 
			//# if (map.backgroundType == 1) {
				//# if (mapDataBuffer[i] instanceof byte[]) {
					//# bbuffer = (byte[]) mapDataBuffer[i];
				//# } else {
					//# sbuffer = (short[]) mapDataBuffer[i];
				//# }
			//# }
			//#else
			int[] lineBuffer = null;
			if(map.backgroundType == 1){
			lineBuffer = mapDataBuffer[i];
			}
			//#endif
			for (int j = 0; j < tileXCount; j++) {
				int cx = j << PATH_SHIFT;
				int cy = i << PATH_SHIFT;
				boolean flag = false;

				if (map.backgroundType == 1) {
					int lfid, fid, layer, cc;

					//#if MemoryMode == "Small"
					//# if (sbuffer != null) {
						//# cc = mapDataBufferList[sbuffer[j] & 0xFFFF];
					//# } else {
						//# cc = mapDataBufferList[bbuffer[j] & 0xFF];
					//# }
					//#else
					cc = lineBuffer[j];
					//#endif

					// 通过性只判断最上层
					if ((cc & 0x7FF) != 0) {
						lfid = (cc >> 7) & 0x0F;
						fid = (cc & 0x1F) - 1;
					} else if ((cc & 0x3FF800) != 0) {
						lfid = (cc >> 18) & 0x0F;
						fid = ((cc >> 11) & 0x1F) - 1;
					} else {
						lfid = (cc >> 29) & 0x07;
						fid = ((cc >> 22) & 0x1F) - 1;
					}
					flag = (landformTileInfos[lfid][fid] & 0x1) == 1;
				} else {
					flag = ((tinfo[1][map.mapData[i][j] & 0xFF]) & 0x01) == 1;
				}

				if (flag) {
					int dd = 1 << PATH_SHIFT;
					for (int dy = 0; dy < dd; dy++) {
						for (int dx = 0; dx < dd; dx++) {
							int testx = cx + dx;
							int ptx = testx & 0x07;
							testx >>= 3;
							int testy = cy + dy;

							mapCollisonData[testy][testx] &= ~(0x1 << ptx);
							mapCollisonData[testy][testx] |= MAP_NOT_PASS << ptx;
						}
					}
				}
			}
		}

		int[] box = new int[4];
		
		for (int k = 0; k < 2; k++) {
			short[][] npcData = null;

			if (k == 0) {
				npcData = map.groundNPCs;
			} else {
				npcData = map.roleNPCs;
			}

			for (int i = 0; i < npcData[0].length; i++) {
				int animateId = npcData[0][i];
				int count = mapNpcCollision[animateId].length >> 1;
				int maxX = pathTileXCount - 1;
				int maxY = pathTileYCount - 1;

				for (int j = 0; j < count; j++) {
					box[0] = (short) (mapNpcCollision[animateId][(j << 1)] >> 16) + npcData[1][i];
					box[1] = (short) (mapNpcCollision[animateId][(j << 1)] & 0xFFFF) + npcData[2][i];
					box[2] = mapNpcCollision[animateId][(j << 1) + 1] >> 16;
					box[3] = mapNpcCollision[animateId][(j << 1) + 1] & 0xFFFF;

					int startX = box[0] / pathTileWidth;
					int startY = box[1] / pathTileHeight;
					int endX = (box[0] + box[2]) / pathTileWidth;
					int endY = (box[1] + box[3]) / pathTileHeight;

					startX = Math.max(0, startX);
					startY = Math.max(0, startY);
					endX = Math.max(0, endX);
					endY = Math.max(0, endY);
					startX = Math.min(maxX, startX);
					startY = Math.min(maxY, startY);
					endX = Math.min(maxX, endX);
					endY = Math.min(maxY, endY);

					for (int cy = startY; cy <= endY; cy++) {
						for (int cx = startX; cx <= endX; cx++) {
							int ptx = cx & 0x07;
							int testx = cx >> 3;
							int testy = cy;

							if (((mapCollisonData[testy][testx] >> ptx) & 0x01) != MAP_NOT_PASS) {
								mapCollisonData[testy][testx] &= ~(0x1 << ptx);
								mapCollisonData[testy][testx] |= MAP_NOT_PASS << ptx;
							}
						}
					}
				}
			}
		}
	}
	
	

	private int getProperScale() {
		int scale = miniMapConfig[MINI_MAP_CONIFG_SCALE1];
		int maxLength = map.width > map.height ? map.width : map.height;
		if (maxLength >= (miniMapConfig[MINI_MAP_CONIFG_SPHERE1] >> 16)
				&& maxLength < (miniMapConfig[MINI_MAP_CONIFG_SPHERE1] & 0xFFFF)) {
			scale = miniMapConfig[MINI_MAP_CONIFG_SCALE1];
		}
		if (maxLength >= (miniMapConfig[MINI_MAP_CONIFG_SPHERE2] >> 16)
				&& maxLength < (miniMapConfig[MINI_MAP_CONIFG_SPHERE2] & 0xFFFF)) {
			scale = miniMapConfig[MINI_MAP_CONIFG_SCALE2];
		}
		if (maxLength >= (miniMapConfig[MINI_MAP_CONIFG_SPHERE3] >> 16)
				&& maxLength < (miniMapConfig[MINI_MAP_CONIFG_SPHERE3] & 0xFFFF)) {
			scale = miniMapConfig[MINI_MAP_CONIFG_SCALE3];
		}
		return scale;
	}

	public int[] getMiniMapSize() {
		int[] size = new int[2];
		size[0] = miniMapWidth;
		size[1] = miniMapHeight;
		return size;
	}

	public void rebuildMiniMap() {
		int scale = getProperScale();
		int alpha = miniMapConfig[MINI_MAP_CONIFG_ALPHA];

		miniMapWidth = tileXCount * scale / 100;
		miniMapHeight = tileYCount * scale / 100;
		miniMapProcData = new int[2];

		if (alpha != 0x00000000) {
			miniMapData = new int[miniMapWidth * miniMapHeight];
			//#if AlphaMethod == rgbimage
			miniMapImage = null;
			//#endif

			for (int i = 0; i < miniMapHeight; i++) {
				int tileY = Math.min(i * 100 / scale, tileYCount - 1);
				short[] sbuffer = null;
				byte[] bbuffer = null;
				int[] lineBuffer = null;

				if (map.backgroundType == 1) {
					//#if MemoryMode == "Small"
					//# if (map.backgroundType == 1) {
						//# if (mapDataBuffer[tileY] instanceof byte[]) {
							//# bbuffer = (byte[]) mapDataBuffer[tileY];
						//# } else {
							//# sbuffer = (short[]) mapDataBuffer[tileY];
						//# }
					//# }
					//#else
					if (mapDataBuffer[tileY] instanceof int[]) {
					lineBuffer = mapDataBuffer[tileY];
					}
					//#endif
				}

				for (int j = 0; j < miniMapWidth; j++) {
					byte tileInfo = 0;
					int tileX = Math.min(j * 100 / scale, tileXCount - 1);

					if (map.backgroundType == 1) {
						int lfid, fid, cc;
						//#if MemoryMode == "Small"
						//# if (sbuffer != null) {
							//# cc = mapDataBufferList[sbuffer[tileX] & 0xFFFF];
						//# } else {
							//# cc = mapDataBufferList[bbuffer[tileX] & 0xFF];
						//# }
						//#else
						cc = lineBuffer[tileX];
						//#endif
						if ((cc & 0x7FF) != 0) {
							lfid = (cc >> 7) & 0x0F;
							fid = (cc & 0x1F) - 1;
						} else if ((cc & 0x3FF800) != 0) {
							lfid = (cc >> 18) & 0x0F;
							fid = ((cc >> 11) & 0x1F) - 1;
						} else {
							lfid = (cc >> 29) & 0x07;
							fid = ((cc >> 22) & 0x1F) - 1;
						}
						tileInfo = landformTileInfos[lfid][fid];
					} else {
						tileInfo = tinfo[1][map.mapData[tileY][tileX] & 0xFF];
					}

					miniMapData[i * miniMapWidth + j] = alpha | thumbColors[(tileInfo >> 1) & 0x1F];
				}
			}
		}

		miniMapWidth += 4;
		miniMapHeight += 4;
		miniMapProcData = new int[4];
	}

	private void drawMap(Graphics g, int viewX, int viewY) {
//#if opengl == true
    	 //# if (Canvas.openglMode) {
    		 //# if (openglMapGridPaint == null) {
    			 //# // 第一次绘制时，把地图按10x10个tile的区域划分成大块，每个大块创建一个批次
        		 //# openglMapGridSize = tileWidth * 10;
        		 //# int col = (map.width + openglMapGridSize - 1) / openglMapGridSize;
        		 //# int row = (map.height + openglMapGridSize - 1) / openglMapGridSize;
        		 //# openglMapGridPaint = new GLGraphics[row][col];
        		 //# for (int i = 0; i < row; i++) {
        			 //# for (int j = 0; j < col; j++) {
        				 //# GLGraphics gg = new GLGraphics();
        				 //# for(int ii = i * 10; ii < i * 10 + 10; ii++){
        		             //# if (ii >= tileYCount){
        		                 //# break;
        		             //# }
        		             //# Object lineData = mapDataBuffer[ii];
        		             //# for(int jj = j * 10; jj < j * 10 + 10; jj++){
        		                 //# if (jj >= tileXCount) {
        		                     //# break;
        		                 //# }
        		                 //# int x = jj * tileWidth;
        		                 //# int y = ii * tileHeight;
        		                 //# drawMapTile(gg, x, y, jj, ii, lineData);
//#         		                 
        		             //# }
        		         //# }
        				 //# openglMapGridPaint[i][j] = gg;
        			 //# }
        		 //# }
		//# groundMapNpcPaint.clear();
		//#  createYOrder(viewX, viewY);
		 
		//#  drawGroundMapNpcs(groundMapNpcPaint);
        	 //# }
    		 //# GLGraphics gg = (GLGraphics)g;
    		 //# gg.translate(-viewX, -viewY);
    		 //# // 按批次绘制在屏幕范围内的所有大块
    		 //# int startX = viewX / openglMapGridSize;
    		 //# int startY = viewY / openglMapGridSize;
    		 //# int endX = (viewX + GameView.showWidth + openglMapGridSize - 1) / openglMapGridSize;
    		 //# int endY = (viewY + GameView.showHeight + openglMapGridSize - 1) / openglMapGridSize;
    		 //# if (startX < 0) {
    			 //# startX = 0;
    		 //# }
    		 //# if (startY < 0) {
    			 //# startY = 0;
    		 //# }
    		 //# if (endX > openglMapGridPaint[0].length) {
    			 //# endX = openglMapGridPaint[0].length;
    		 //# }
    		 //# if (endY > openglMapGridPaint.length) {
    			 //# endY = openglMapGridPaint.length;
    		 //# }
    		 //# for (int i = startY; i < endY; i++) {
    			 //# for (int j = startX; j < endX; j++) {
    				 //# gg.drawBatch(openglMapGridPaint[i][j]);
    			 //# }
    		 //# }
		//# if(showMapNpcAnimate){
		//# 	 gg.drawBatch(groundMapNpcPaint);
		//#  }
    		 //# gg.translate(viewX, viewY);
    		 //# return;
    	 //# }
//#endif
		
		if (useImageBuffer && bgImg != null) {
			int startX = viewX / tileWidth;
			int endX = startX + bgCellW - 1;
			int startY = viewY / tileHeight;
			int endY = startY + bgCellH - 1;

			if (isFirstBgImage) {
				isFirstBgImage = false;
				gg.setColor(0);
				gg.fillRect(0, 0, bgWidth, bgHeight);
				drawCellMap(startX, startY, endX, endY);
				oldStartX = startX;
				oldEndX = endX;
				oldStartY = startY;
				oldEndY = endY;
			}

			if (oldStartX != startX) {
				int sx, ex;

				// 地图向右移动
				if (startX < oldStartX) {
					sx = startX;
					ex = oldStartX - 1;

					if (ex > endX) {
						ex = endX;
					}
				} else { // 地图向左移动
					sx = oldEndX + 1;
					ex = endX;

					if (sx < startX) {
						sx = startX;
					}
				}

				drawCellMap(sx, oldStartY, ex, oldEndY);

				oldStartX = startX;
				oldEndX = endX;
			}

			if (oldStartY != startY) {
				int sy, ey;

				// 地图向下移动
				if (startY < oldStartY) {
					sy = startY;
					ey = oldStartY - 1;

					if (ey > endY) {
						ey = endY;
					}
				} else { // 地图向上移动
					sy = oldEndY + 1;
					ey = endY;

					if (sy < startY) {
						sy = startY;
					}
				}

				drawCellMap(oldStartX, sy, oldEndX, ey);

				oldStartY = startY;
				oldEndY = endY;
			}

			int sMapX = viewX % bgWidth;
			int eMapX = (viewX + GameMain.viewWidth) % bgWidth;
			int sMapY = viewY % bgHeight;
			int eMapY = (viewY + GameMain.viewHeight) % bgHeight;

			int clipX = g.getClipX();
			int clipY = g.getClipY();
			int clipW = g.getClipWidth();
			int clipH = g.getClipHeight();

			if (eMapX > sMapX) {
				if (eMapY > sMapY) {
					g.drawImage(bgImg, -sMapX + vx, -sMapY + vy, 0);
				} else {
					g.clipRect(vx, vy, GameMain.viewWidth, bgHeight - sMapY);
					g.drawImage(bgImg, -sMapX + vx, -sMapY + vy, 0);
					g.setClip(clipX, clipY, clipW, clipH);

					g.clipRect(vx, bgHeight - sMapY + vy, GameMain.viewWidth, eMapY);
					g.drawImage(bgImg, -sMapX + vx, bgHeight - sMapY + vy, 0);
				}
			} else if (eMapY > sMapY) {
				g.clipRect(vx, vy, bgWidth - sMapX, GameMain.viewHeight);
				g.drawImage(bgImg, -sMapX + vx, -sMapY + vy, 0);
				g.setClip(clipX, clipY, clipW, clipH);

				g.clipRect(bgWidth - sMapX + vx, vy, eMapX, GameMain.viewHeight);
				g.drawImage(bgImg, bgWidth - sMapX + vx, -sMapY + vy, 0);
			} else {
				g.clipRect(vx, vy, bgWidth - sMapX, bgHeight - sMapY);
				g.drawImage(bgImg, -sMapX + vx, -sMapY + vy, 0);
				g.setClip(clipX, clipY, clipW, clipH);

				g.clipRect(bgWidth - sMapX + vx, vy, eMapX, bgHeight - sMapY);
				g.drawImage(bgImg, bgWidth - sMapX + vx, -sMapY + vy, 0);
				g.setClip(clipX, clipY, clipW, clipH);

				g.clipRect(vx, bgHeight - sMapY + vy, bgWidth - sMapX, eMapY);
				g.drawImage(bgImg, -sMapX + vx, bgHeight - sMapY + vy, 0);
				g.setClip(clipX, clipY, clipW, clipH);

				g.clipRect(bgWidth - sMapX + vx, bgHeight - sMapY + vy, eMapX, eMapY);
				g.drawImage(bgImg, bgWidth - sMapX + vx, bgHeight - sMapY + vy, 0);
			}
//			if(GameMain.ANDROID_LARGE.equals(GameMain.getUIModel())){
			if(GameMain.viewWidth > map.width || GameMain.viewHeight > map.height){
				g.setClip(0, 0, GameMain.viewWidth, GameMain.viewHeight);
				drawEdgeMapNpc(g, viewX, viewY);
			}
//			}
			g.setClip(clipX, clipY, clipW, clipH);
		} else {
			drawMapNoBuffer(g, viewX, viewY);
		}
	}
	
	private int oldViewX = 0;
	private int oldViewY = 0;
	private static boolean needBuildEdgeMapNpc = false;
	private static final Vector edgeNpcsOfGround = new Vector();
	private static final Vector edgeNpcsOfRole = new Vector();
	private static final Vector edgeNpcsOfSky = new Vector();
	/**
	 * 初始化边缘MAPNPC信息(AndroidLarge画全边缘mapnpc)
	 * @param npcData
	 * @param edgeNpcs
	 * @param viewX
	 * @param viewY
	 */
	private void buildEdgeNpcs(short[][] npcData, Vector edgeNpcs,int viewX,int viewY){
		edgeNpcs.removeAllElements();
		//相对坐标
		int mapBoxX = 0;
		int mapBoxY = 0;
		int mapBoxWidth = map.width;
		int mapBoxHeight = map.height;
		int offsetX = -viewX;
		int offsetY = -viewY;
		int[] box = new int[4];
		
		for (int i = 0; i < npcData[0].length; i++) {
			int animateId = npcData[0][i];

			box = mapNpcAnimateSet.getAnimateBox(box, animateId);
			box[0] += npcData[1][i];
			box[1] += npcData[2][i];
			
			if ((box[0] <= mapBoxX || box[0] + box[2]>= mapBoxX + mapBoxWidth || box[1] <= mapBoxY || box[1] + box[3] >= mapBoxY
					+ mapBoxHeight)) {
				edgeNpcs.addElement(new int[]{animateId, 0, npcData[1][i] + offsetX + mapBoxX, npcData[2][i] + offsetY + mapBoxY});
			}
		}
	}

	/**
	 * AndroidLarge补画边缘mapnpc
	 * @param viewX
	 * @param viewY
	 */
	private void drawEdgeMapNpc(Graphics g,int viewX,int viewY){
		if(needBuildEdgeMapNpc == true){
			needBuildEdgeMapNpc = false;
			buildEdgeNpcs(map.groundNPCs,edgeNpcsOfGround, viewX, viewY);
			buildEdgeNpcs(map.roleNPCs,edgeNpcsOfRole, viewX, viewY);
			buildEdgeNpcs(map.skyNPCs,edgeNpcsOfSky, viewX, viewY);
		} else if(oldViewX != viewX || oldViewY != viewY){
			oldViewX = viewX;
			oldViewY = viewY;
			buildEdgeNpcs(map.groundNPCs,edgeNpcsOfGround, viewX, viewY);
			buildEdgeNpcs(map.roleNPCs,edgeNpcsOfRole, viewX, viewY);
			buildEdgeNpcs(map.skyNPCs,edgeNpcsOfSky, viewX, viewY);
		}
		
		for (int i = 0; i < edgeNpcsOfGround.size(); i++) {
			int[] npcData = (int[])edgeNpcsOfGround.elementAt(i);
			mapNpcAnimateSet.drawAnimateFrame(g, npcData[0], npcData[1], npcData[2], npcData[3]);
		}
		for (int i = 0; i < edgeNpcsOfRole.size(); i++) {
			int[] npcData = (int[])edgeNpcsOfRole.elementAt(i);
			mapNpcAnimateSet.drawAnimateFrame(g, npcData[0], npcData[1], npcData[2], npcData[3]);
		}
		for (int i = 0; i < edgeNpcsOfSky.size(); i++) {
			int[] npcData = (int[])edgeNpcsOfSky.elementAt(i);
			mapNpcAnimateSet.drawAnimateFrame(g, npcData[0], npcData[1], npcData[2], npcData[3]);
		}
	}
	
	private void mergeMapNpcDrawBox(Vector drawData, int x, int y, int tileX, int tileY) {
		int count = drawData.size();
		boolean includeFound = false;

		for (int i = 0; i < count; i++) {
			int[] data = (int[]) drawData.elementAt(i);

			if (Tool.rectIntersect(data[0], data[1], data[2], data[3], x, y, tileWidth, tileHeight)) {
				includeFound = true;

				break;
			}
		}

		if (includeFound) {
			return;
		}

		boolean mergeFound = false;

		for (int i = 0; i < count; i++) {
			int[] data = (int[]) drawData.elementAt(i);

			if (data[0] + data[2] == x && data[1] <= y) {
				data[2] += tileWidth;
				data[6] += tileWidth;
				mergeFound = true;
			} else if (data[1] + data[3] == y && data[0] <= x) {
				data[3] += tileHeight;
				data[7] += tileHeight;
				mergeFound = true;
			}

			if (mergeFound) {
				break;
			}
		}

		if (!mergeFound) {
			drawData.addElement(new int[] { x, y, tileWidth, tileHeight, tileX * tileWidth, tileY * tileHeight,
					tileWidth, tileHeight });
		}
	}

	private void drawCellMap(int startX, int startY, int endX, int endY) {
		int sx = 0, sy = 0;
		Vector mapNpcDrawData = new Vector();

		for (int j = startY; j <= endY; j++) {
			if (j >= 0 && j < tileYCount) {
				Object lineData;
				if (map.backgroundType == 0) {
					lineData = map.mapData[j];
				} else {
					if (mapDataBufferReleased) {
						continue;
					}
					lineData = mapDataBuffer[j];
				}
				sy = (j % bgCellH) * tileHeight;
				for (int i = startX; i <= endX; i++) {
					if (i >= 0 && i < tileXCount) {
						sx = (i % bgCellW) * tileWidth;
						drawMapTile(gg, sx, sy, i, j, lineData);
						mergeMapNpcDrawBox(mapNpcDrawData, sx, sy, i, j);
					}
				}
			}
		}

		int[] box = new int[4];
		
		for (int i = 0; i < mapNpcDrawData.size(); i++) {
			int[] data = (int[]) mapNpcDrawData.elementAt(i);
			drawStillMapNpc(gg, map.groundNPCs, data[4], data[5], data[6], data[7], data[0] - data[4], data[1]
					- data[5], box);
			drawStillMapNpc(gg, map.roleNPCs, data[4], data[5], data[6], data[7], data[0] - data[4], data[1] - data[5], box);
			drawStillMapNpc(gg, map.skyNPCs, data[4], data[5], data[6], data[7], data[0] - data[4], data[1] - data[5], box);
		}
	}

	private void drawStillMapNpc(Graphics g, short[][] npcData, int mapBoxX, int mapBoxY, int mapBoxWidth,
			int mapBoxHeight, int offsetX, int offsetY, int[] box) {
//#if opengl == true
    	//# if (!Canvas.openglMode) {
    		//# g.setClip(mapBoxX + offsetX, mapBoxY + offsetY, mapBoxWidth, mapBoxHeight);
    	//# }
//#else
        g.setClip(mapBoxX + offsetX, mapBoxY + offsetY, mapBoxWidth, mapBoxHeight);
//#endif
		

		for (int i = 0; i < npcData[0].length; i++) {
			int animateId = npcData[0][i];

			if (mapNpcAnimateSet.getAnimateLength(animateId) > 1 && showMapNpcAnimate) {
				continue;
			}

			box = mapNpcAnimateSet.getAnimateBox(box, animateId);
			box[0] += npcData[1][i];
			box[1] += npcData[2][i];

			if (Tool.rectIntersect(box[0], box[1], box[2], box[3], mapBoxX, mapBoxY, mapBoxWidth, mapBoxHeight)){
				mapNpcAnimateSet.drawAnimateFrame(g, animateId, 0, npcData[1][i] + offsetX, npcData[2][i] + offsetY);
			}
		}
//#if opengl == true
    	//# if (!Canvas.openglMode) {
    		//# if (useImageBuffer) {
    			//# g.setClip(0, 0, bgWidth, bgHeight);
    		//# } else {
    			//# g.setClip(0, 0, GameMain.viewWidth, GameMain.viewHeight);
    		//# }
    	//# }
//#else
		if (useImageBuffer) {
			g.setClip(0, 0, bgWidth, bgHeight);
		} else {
			g.setClip(0, 0, GameMain.viewWidth, GameMain.viewHeight);
		}
//#endif

	}

	private void drawMapTile(Graphics g, int x, int y, int tileX, int tileY, Object lineData) {
		switch (map.backgroundType) {
		case 0: {
			byte[] ld = (byte[]) lineData;
			int tile = tinfo[0][ld[tileX] & 0xFF] & 0xFF;
			int trans = (tinfo[1][ld[tileX] & 0xFF] >> 6) & 0x03;
			tileImage.drawFrame(g, tile, x, y, trans);
		}
			break;
		case 1: {
			if (mapDataBufferReleased) {
				return;
			}

			int lfid, trans, fid, layer, cc;
			//#if MemoryMode == "Small"
			//# if (lineData instanceof short[]) {
				//# cc = mapDataBufferList[((short[]) lineData)[tileX] & 0xFFFF];
			//# } else {
				//# cc = mapDataBufferList[((byte[]) lineData)[tileX] & 0xFF];
			//# }
			//#else
			cc = ((int[])lineData)[tileX];
			//#endif
			if ((cc & 0xFFC00000) != 0) {
				lfid = (cc >> 29) & 0x07;
				trans = (cc >> 27) & 0x03;
				fid = ((cc >> 22) & 0x1F) - 1;
				landformImages[lfid].drawFrame(g, fid, x, y, trans);
				if ((cc & 0x3FF800) != 0) {
					lfid = (cc >> 18) & 0x0F;
					trans = (cc >> 16) & 0x03;
					fid = ((cc >> 11) & 0x1F) - 1;
					landformImages[lfid].drawFrame(g, fid, x, y, trans);
					if ((cc & 0x7FF) != 0) {
						lfid = (cc >> 7) & 0x0F;
						trans = (cc >> 5) & 0x03;
						fid = (cc & 0x1F) - 1;
						landformImages[lfid].drawFrame(g, fid, x, y, trans);
					}
				}
			}
		}
			break;
		}
	}

	private void drawMapNoBuffer(Graphics g, int viewX, int viewY) {
		int startX = getTileX(viewX);
		int startY = getTileY(viewY);

		if (startX < 0) {
			startX = 0;
		}

		if (startY < 0) {
			startY = 0;
		}

		int endX = Math.min(tileXCount, getTileX(viewX + GameMain.viewWidth) + 1);
		int endY = Math.min(tileYCount, getTileY(viewY + GameMain.viewHeight) + 1);

		for (int i = startY; i < endY; i++) {
			if (i < 0 || i >= tileYCount) {
				continue;
			}
			Object lineData;
			if (map.backgroundType == 0) {
				lineData = map.mapData[i];
			} else {
				if (mapDataBufferReleased) {
					continue;
				}
				lineData = mapDataBuffer[i];
			}
			for (int j = startX; j < endX; j++) {
				if (j < 0 || j >= tileXCount) {
					continue;
				}
				int x = j * tileWidth - viewX;
				int y = i * tileHeight - viewY;
				drawMapTile(g, x, y, j, i, lineData);
			}
		}

		// drawStillMapNpc(g, map.groundNPCs, startX * tileWidth, startY *
		// tileHeight, (endX - startX) * tileWidth, (endY - startY) *
		// tileHeight, -viewX, -viewY);
		// drawStillMapNpc(g, map.roleNPCs, startX * tileWidth, startY *
		// tileHeight, (endX - startX) * tileWidth, (endY - startY) *
		// tileHeight, -viewX, -viewY);
		// drawStillMapNpc(g, map.skyNPCs, startX * tileWidth, startY *
		// tileHeight, (endX - startX) * tileWidth, (endY - startY) *
		// tileHeight, -viewX, -viewY);
	}

	private int getTileX(int x) {
		return x / tileWidth;
	}

	private int getTileY(int y) {
		return y / tileHeight;
	}

	private class PendingDrawItem {
		public int type;
		public Object objData;
		public int x;
		public int y;
		public int color;
		public int bgColor;
		public boolean is3D;
		public int anchor;

		public static final int ITEM_TYPE_HEAD_STRING = 0;
		public static final int ITEM_TYPE_FLY_STRING = 1;
		public static final int ITEM_TYPE_ANIMATE = 2;
		public static final int ITEM_TYPE_IMAGE = 3;
		public static final int ITEM_TYPE_BUBBLE = 4;

		public void draw(Graphics g) {
			switch (type) {
			case ITEM_TYPE_HEAD_STRING: {
				if (is3D) {
					Tool.draw3DString(g, (String) objData, x, y, color, bgColor, anchor);
				} else {
					g.setColor(color);
					Tool.drawString(g, (String) objData, x, y, anchor);
				}
			}
				break;
			case ITEM_TYPE_FLY_STRING: {
				FlyingStringInfo fly = (FlyingStringInfo) objData;

				if (fly.isAcross) {
					// 计算侧漂轨迹
					if (fly.calculate <= fly.hCycleCount) {
						x = x + fly.hSpeed * fly.calculate * fly.dir;
						y = y - fly.hSpeed * fly.calculate;

						fly.drawFlying(g, x, y, fly.number, fly.color, 0, 0, 0);
					} else if (fly.calculate - fly.hCycleCount < fly.stopCycleCount) {
						x = x + fly.hSpeed * fly.hCycleCount * fly.dir;
						y = y - fly.hSpeed * fly.hCycleCount;
						fly.drawFlying(g, x, y, fly.number, fly.color, 0, 0, 0);
					} else {
						int vCycleCount = fly.calculate - fly.hCycleCount - fly.stopCycleCount;
						x = x + fly.hSpeed * fly.hCycleCount * fly.dir;
						y = y - fly.hSpeed * fly.hCycleCount - fly.vSpeed * vCycleCount;

						fly.drawFlying(g, x, y, fly.number, fly.color, 0, 0, 0);
					}

				} else {
					fly.drawFlying(g, x, y, fly.number, fly.color, fly.distance, fly.calculate * 100 / fly.time,
							fly.calculate - 1);
				}
			}
				break;
			case ITEM_TYPE_ANIMATE: {
				AnimatePlayer animatePlayer = (AnimatePlayer) objData;
				animatePlayer.draw(g, x, y);
			}
				break;
			case ITEM_TYPE_IMAGE: {
				ImageSet image = (ImageSet) objData;
				image.drawFrame(g, color, x, y, 0, anchor);
			}
				break;
			case ITEM_TYPE_BUBBLE:{
				Bubble bl = (Bubble)this;
				Tool.drawTip(g, bl.x, bl.y, bl.lines,bubbleImg);
			}
				break;
			}
		}
	}
	/**
	 * 泡泡图片资源
	 */
	public static ImageSet bubbleImg = null;
	/**
	 * 泡泡
	 */
	private class Bubble extends PendingDrawItem {
		public int width;
		public int counterKey = -1;
		public String[] lines;
		
		public Bubble(String[] text,int x,int y){
			this.x = x;
			this.y = y;
			this.type = ITEM_TYPE_BUBBLE;
			lines = text;
			if(bubbleImg == null){
				bubbleImg = (ImageSet)Tool.getGlobalObject("VarUIRes");
			}
		}
	}

	private static final byte[][] PATH_FIND = { { -1, 0, 2 }, { 1, 0, 2 }, { 0, -1, 2 }, { 0, 1, 2 }, { -1, -1, 3 },
			{ 1, -1, 3 }, { -1, 1, 3 }, { 1, 1, 3 } };

	public short[][] searchPath_AStar(int startX, int startY, int endX, int endY,int spriteType) {
		if (startX < 0 || startX >= pathTileXCount || startY < 0 || startY >= pathTileYCount || endX < 0
				|| endX >= pathTileXCount || endY < 0 || endY >= pathTileYCount) {
			return null;
		}

		if (startX == endX && startY == endY) {
			return null;
		}
		if (((mapCollisonData[endY][endX >> 3] >> (endX & 0x07)) & 0x01) != MAP_CAN_PASS) {
			return null;
		}
		short[][] pathLen = new short[pathTileYCount][pathTileXCount];
		int maxEdge = (pathTileYCount + pathTileXCount) << 4;
		short[] openNodes = new short[maxEdge];
		int openNodeStart = 0;
		int openNodeEnd = 1;
		pathLen[startY][startX] = 1;
		openNodes[0] = (short) ((startX << 8) | startY);
		boolean found = false;
		while (openNodeStart != openNodeEnd) {
			int thisX = (openNodes[openNodeStart] >> 8) & 0xFF;
			int thisY = openNodes[openNodeStart] & 0xFF;
			int thisLen = pathLen[thisY][thisX];
			openNodeStart++;
			if (openNodeStart >= maxEdge) {
				openNodeStart = 0;
			}
			if (thisX == endX && thisY == endY) {
				found = true;
				break;
			}

			for (int i = 0; i < 8; i++) {
				byte[] p = PATH_FIND[i];
				int checkX = thisX + p[0];
				int checkY = thisY + p[1];
				int step = p[2];
				if (checkX < 0 || checkX >= pathTileXCount || checkY < 0 || checkY >= pathTileYCount) {
					continue;
				}
				short t = pathLen[checkY][checkX];
				if (t == 0 || t > thisLen + step) {
					// 没有走过，或者从更差的路径走过，都重新open
					if (t == 0 && ((mapCollisonData[checkY][checkX >> 3] >> (checkX & 0x07)) & 0x01) != MAP_CAN_PASS) {
						// 不可通过，设为-1
						pathLen[checkY][checkX] = -1;
					} else {
						// 设置新路径长度数值，并加入open表
						pathLen[checkY][checkX] = (short) (thisLen + step);
						if ((checkY - thisY) * (endY - thisY) >= 0 && (checkX - thisX) * (endX - thisX) >= 0) {
							openNodeStart--;
							if (openNodeStart < 0) {
								openNodeStart = maxEdge - 1;
							}
							openNodes[openNodeStart] = (short) ((checkX << 8) | checkY);
						} else {
							openNodes[openNodeEnd] = (short) ((checkX << 8) | checkY);
							openNodeEnd++;
							if (openNodeEnd >= maxEdge) {
								openNodeEnd = 0;
							}
						}
					}
				}
			}
		}
		if (found) {
			int stepLen = pathLen[endY][endX];
			short[][] ret = new short[stepLen][2];
			int retp = stepLen - 1;
			while (endX != startX || endY != startY) {
				ret[retp][0] = (short) endX;
				ret[retp][1] = (short) endY;
				retp--;
				for (int i = 0; i < 8; i++) {
					byte[] p = PATH_FIND[i];
					int checkX = endX + p[0];
					int checkY = endY + p[1];
					int step = p[2];
					if (checkX < 0 || checkX >= pathTileXCount || checkY < 0 || checkY >= pathTileYCount) {
						continue;
					}
					if (pathLen[checkY][checkX] == stepLen - step) {
						endX = checkX;
						endY = checkY;
						stepLen -= step;
						break;
					}
				}
			}
			short[][] ret2 = new short[ret.length - retp - 1][2];
			System.arraycopy(ret, retp + 1, ret2, 0, ret2.length);
			return optimizePath(ret2,spriteType);
		} else {
			return null;
		}
	}

	/**
	 * 旧的寻路算法 public short[][] searchPath(int startX, int startY, int endX, int
	 * endY){ if(startX == endX && startY == endY){ return null; }
	 * 
	 * if(startX < 0 || startX >= pathTileXCount || startY < 0 || startY >=
	 * pathTileYCount){ return null; }
	 * 
	 * //用广度优先法搜索startX, startY 到 endX, endY最短路径，如无通路返回null short[][] openList =
	 * new short[2][pathTileXCount * pathTileYCount]; short[][] parentMapX = new
	 * short[pathTileYCount][pathTileXCount]; short[][] parentMapY = new
	 * short[pathTileYCount][pathTileXCount]; short[] xList = new short[8];
	 * short[] yList = new short[8]; byte[][] tmpMap = new
	 * byte[pathTileYCount][(pathTileXCount >> 2) + 1];
	 * 
	 * int openListHead, openListTail; int x, y, idx, tx, ty;
	 * 
	 * for(int i = 0; i < pathTileYCount; i++){
	 * System.arraycopy(mapCollisonData[i], 0, tmpMap[i], 0,
	 * mapCollisonData[i].length); }
	 * 
	 * openListHead = 0; openListTail = -1; openListTail++;
	 * openList[0][openListTail] = (short) startX; openList[1][openListTail] =
	 * (short) startY;
	 * 
	 * tmpMap[startY][startX >> 2] |= ~(0x3 << ((3 - (startX & 0x03)) << 1));
	 * tmpMap[startY][startX >> 2] |= MAP_FIND_PATH << ((3 - (startX & 0x03)) <<
	 * 1);
	 * 
	 * while(openListTail >= openListHead){ x = openList[0][openListHead]; y =
	 * openList[1][openListHead]; openListHead++;
	 * 
	 * if(x == endX && y == endY){ idx = 0;
	 * 
	 * while(parentMapX[y][x] != 0 || parentMapY[y][x] != 0){ idx++; tx =
	 * parentMapX[y][x]; ty = parentMapY[y][x]; x = tx; y = ty; }
	 * 
	 * short[][] path = new short[idx][2]; x = endX; y = endY;
	 * 
	 * for(int i = idx - 1; i >= 0; i--){ path[i][0] = (short) x; path[i][1] =
	 * (short) y; tx = parentMapX[y][x]; ty = parentMapY[y][x]; x = tx; y = ty;
	 * }
	 * 
	 * return optimizePath(path); }else{ idx = 0;
	 * 
	 * if(x - 1 >= 0){ if((tmpMap[y][(x - 1) >> 2] >> ((3 - ((x - 1) & 0x03) <<
	 * 1)) & 0x03) == MAP_CAN_PASS){ xList[idx] = (short) (x - 1); yList[idx] =
	 * (short) y; idx++; } }
	 * 
	 * if(y - 1 >= 0){ if((tmpMap[y - 1][x >> 2] >> ((3 - (x & 0x03) << 1)) &
	 * 0x03) == MAP_CAN_PASS){ xList[idx] = (short) x; yList[idx] = (short) (y -
	 * 1); idx++; } }
	 * 
	 * if(x + 1 < pathTileXCount){ if((tmpMap[y][(x + 1) >> 2] >> ((3 - ((x + 1)
	 * & 0x03) << 1)) & 0x03) == MAP_CAN_PASS){ xList[idx] = (short) (x + 1);
	 * yList[idx] = (short) y; idx++; } }
	 * 
	 * if(y + 1 < pathTileYCount){ if((tmpMap[y + 1][x >> 2] >> ((3 - (x & 0x03)
	 * << 1)) & 0x03) == MAP_CAN_PASS){ xList[idx] = (short) x; yList[idx] =
	 * (short) (y + 1); idx++; } }
	 * 
	 * if(x - 1 >= 0 && y - 1 >= 0){ if((tmpMap[y - 1][(x - 1) >> 2] >> ((3 -
	 * ((x - 1) & 0x03) << 1)) & 0x03) == MAP_CAN_PASS){ xList[idx] = (short) (x
	 * - 1); yList[idx] = (short) (y - 1); idx++; } }
	 * 
	 * if(x + 1 < pathTileXCount && y - 1 >= 0){ if((tmpMap[y - 1][(x + 1) >> 2]
	 * >> ((3 - ((x + 1) & 0x03) << 1)) & 0x03) == MAP_CAN_PASS){ xList[idx] =
	 * (short) (x + 1); yList[idx] = (short) (y - 1); idx++; } }
	 * 
	 * if(x - 1 >= 0 && y + 1 < pathTileYCount){ if((tmpMap[y + 1][(x - 1) >> 2]
	 * >> ((3 - ((x - 1) & 0x03) << 1)) & 0x03) == MAP_CAN_PASS){ xList[idx] =
	 * (short) (x - 1); yList[idx] = (short) (y + 1); idx++; } }
	 * 
	 * if(x + 1 < pathTileXCount && y + 1 < pathTileYCount){ if((tmpMap[y +
	 * 1][(x + 1) >> 2] >> ((3 - ((x + 1) & 0x03) << 1)) & 0x03) ==
	 * MAP_CAN_PASS){ xList[idx] = (short) (x + 1); yList[idx] = (short) (y +
	 * 1); idx++; } }
	 * 
	 * if(idx > 0){ for(int i = 0; i < idx; i++){ parentMapX[yList[i]][xList[i]]
	 * = (short) x; parentMapY[yList[i]][xList[i]] = (short) y;
	 * 
	 * tmpMap[yList[i]][xList[i] >> 2] &= ~(0x3 << ((3 - (xList[i] & 0x03)) <<
	 * 1)); tmpMap[yList[i]][xList[i] >> 2] |= MAP_FIND_PATH << ((3 - (xList[i]
	 * & 0x03)) << 1);
	 * 
	 * openListTail++; openList[0][openListTail] = xList[i];
	 * openList[1][openListTail] = yList[i]; } } } }
	 * 
	 * return null; }
	 */

	private short[][] optimizePath(short[][] path,int spriteType) {
		if (path == null) {
			return null;
		}

		Vector optPath = new Vector();
		int oldDx = 0;
		int oldDy = 0;

		// 缩减同一直线上的路点，只留两个端点
		for (int i = 0; i < path.length - 1; i++) {
			int dx = path[i][0] - path[i + 1][0];
			int dy = path[i][1] - path[i + 1][1];

			if (dx != oldDx || dy != oldDy) {
				oldDx = dx;
				oldDy = dy;
				optPath.addElement(path[i]);
			}
		}

		optPath.addElement(path[path.length - 1]);

		// 遍历路径中的路点，如果两点间的斜线路径上没有障碍则拿掉中间的直角拐点
		for (int i = 0; i < optPath.size() - 2; i++) {
			for (int j = i + 2; j < optPath.size(); j++) {
				short[] p1 = (short[]) optPath.elementAt(i);
				short[] p2 = (short[]) optPath.elementAt(j);

				if (testPath(p1[0], p1[1], p2[0], p2[1],spriteType)) {
					optPath.removeElementAt(i + 1);
					j--;
				} else {
					break;
				}
			}
		}

		// 输出新路径
		short[][] newPath = new short[optPath.size()][];
		optPath.copyInto(newPath);

		return newPath;
	}

	private boolean testPath(int x1, int y1, int x2, int y2,int spriteType){
		if(spriteType == Tool.SPRITE_TYPE_ROLE){
	        // 下面的算法速度更慢，但更准确,优化自动寻路
	        x1 *= this.pathTileWidth;
	        x1 += this.pathTileWidth / 2;
	        y1 *= this.pathTileHeight;
	        y1 += this.pathTileHeight / 2;
	        x2 *= this.pathTileWidth;
	        x2 += this.pathTileWidth / 2;
	        y2 *= this.pathTileHeight;
	        y2 += this.pathTileHeight / 2;
	
	        int x, y;
	        int dy = y2 - y1;
	        int dx = x2 - x1;
	        if (Math.abs(dx) > Math.abs(dy)) {
		        int minX = Math.min(x1, x2);
		        int maxX = Math.max(x1, x2);
	        	for (x= minX; x < maxX; x += 2){
	            	y = dy * (x - x1) / dx + y1;
		            if (((mapCollisonData[y / pathTileHeight][(x / pathTileWidth) >> 3] >> ((x / pathTileWidth) & 0x07)) & 0x01) != MAP_CAN_PASS) {
		                return false;
		            }
		        }
	        } else {
	        	int minY= Math.min(y1, y2);
		        int maxY = Math.max(y1, y2);
	        	for (y= minY; y < maxY; y += 2){
	        		x = dx * (y - y1) / dy + x1;
		            if (((mapCollisonData[(y / pathTileHeight)][(x / pathTileWidth) >> 3] >> ((x / pathTileWidth) & 0x07)) & 0x01) != MAP_CAN_PASS) {
		                return false;
		            }
		        }
	        }
	        
	        return true;
		} else {
	        //两点式直线方程
	        //y = (y2 - y1) * (x - x1) / (x2 - x1) + y1
	        
	        int dy = y2 - y1;
	        int dx = x2 - x1;

	        if (Math.abs(dx) > Math.abs(dy)) {
		        int minX = Math.min(x1, x2);
		        int maxX = Math.max(x1, x2);
		        for(int x = minX; x < maxX; x++){
		            int y = dy * (x - x1) / dx + y1;
		            if(((mapCollisonData[y][x >> 3] >> (x & 0x07)) & 0x01) != MAP_CAN_PASS){
		                return false;
		            }
		        }
	        } else {
	        	int minY = Math.min(y1, y2);
		        int maxY = Math.max(y1, y2);
		        for(int y = minY; y < maxY; y++){
		        	int x = dx * (y - y1) / dy + x1;
		            if(((mapCollisonData[y][x >> 3] >> (x & 0x07)) & 0x01) != MAP_CAN_PASS){
		                return false;
		            }
		        }
	        }
	        
	        return true;
		}
    }
	
}

class DrawItem{
	PipAnimateSet pas;
	ImageSet image;
	int frame;
	int x;
	int y;
	int trans;
	int anchor;
	
	public DrawItem(PipAnimateSet pas, ImageSet image, int frame, int x, int y, int trans, int anchor){
		this.pas = pas;
		this.image = image;
		this.frame = frame;
		this.x = x;
		this.y = y;
		this.trans = trans;
		this.anchor = anchor;
	}
	public void draw(Graphics g, int viewX, int viewY){
		if(pas == null){
			if(image != null && frame < image.getFrameCount()){
				image.drawFrame(g, frame, x - viewX, y - viewY, trans, anchor);
			}
		}else{
			if(frame < pas.getAnimateCount()){
				int len = pas.getAnimateLength(frame);
				pas.drawAnimateFrame(g, frame, GameMain.tick % len, x - viewX, y - viewY);
			}
		}
	}
}
