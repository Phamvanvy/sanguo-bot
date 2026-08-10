package pip;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.TextField;

//#if polish.midp2
import javax.microedition.lcdui.game.GameCanvas;
//#else
//# import javax.microedition.lcdui.Canvas;
//#endif

import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;

import pip.io.UWAPSegment;
import pip.io.UWAPSocketConnection;


//#ifdef polish.api.nokia-ui
//# import com.nokia.mid.ui.*;
//#endif

public class World extends
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
                implements Runnable
                //#if CommandEmu == true
                //# , CommandListener
                //#endif
                {

    /**
     * 按键的时间,用于计算按键之间的时间差
     */
    public long keyTime;
    /**
     * 记录是否为连续动作
     */
    public static boolean autoRun = false;

    public static World instance;
    public static Display display;
    public static GameState gameState;

    private static boolean needRepaint;
    public static long _elapsedTime;

    private static final short SENDPOSITIONTIME_LIMIT = 10000;
    private static short sendPositionTime;

    public static long tick;
    public static final int MILLIS_PRE_UPDATE = 75;

    public static GTVM _gtvm;
    public static Sprite player;
    public static Vector events = new Vector();

    public static int lastAnswer = -1;
    public static String lastInput = "";
    public static boolean formInputOver = false;

    private static byte baseTile;

    private static short tileXCount;
    private static short tileYCount;
    public static short tileWidth;
    public static short tileHeight;

    public static short viewX;
    public static short viewY;
    public static short viewWidth;
    public static short viewHeight;
    private static short viewMaxX;
    private static short viewMaxY;

    public static Hashtable finishedTask = new Hashtable();
    public static Hashtable unFinishedTask = new Hashtable();

    public static int resourceIndex;
    public static boolean resourceGame;
    public static boolean resourceLockOk;
    public static Displayable  displayFirst ;
    /**
     * 地图数据<br>
     * 精细地图:<br>
     * 每个byte对应该位置的tile ID<br>
     * 模糊地图:<br>
     * 每个byte存储3分数据<br>
     * 7-6位 第一层地形ID<br>
     * 5-4位 第二层地形ID<br>
     * 3-0位 tile ID,如果为0则表示该位置不画,否则画id-1的tile，如果两层地形ID相等，则只画第一层，否则画两层
     */
    private static byte map[][];
    private static byte baseMap[][];

    //====功能选项开关变量====================

    //public static boolean closeConnection = false;

    public static final short MINI_MAP_OPTION_NORMAL = 0;
    public static final short MINI_MAP_OPTION_SIMPLE = 1;
    public static final short MINI_MAP_OTPION_CLOSE = 2;

    /**
     * 0 = 全部显示<br>
     * 1 = 只显示头像<br>
     * 2 = 只显示状态<br>
     * 3 = 全部隐藏<br>
     */
    public static short topbarFlag = 0;
    public static short miniMapOption = MINI_MAP_OPTION_NORMAL;
    public static boolean closeMonIconDownload = false;
    public static boolean closeNpcImgDownload = false;
    public static boolean closeMonImageDownload = false;
    public static boolean closeAutoMove = false;


    /**
     * 0 = 全部显示<br>
     * 1 = 只显示人物<br>
     * 2 = 只显示名字<br>
     */
    public static short nameFlag = 0;

    /**
     * 0 = 显示公会<br>
     * 1 = 显示称号<br>
     * 2 = 显示荣誉<br>
     * 3 = 显示荣誉点数<br>
     * 4 = 不显示<br>
     */
    public static short titleFlag = 0;

    //====测试网速====================
    private long requestTestNetSpeedTime;
    private static final long NETSPEED_TEST_INTERVAL = 180000; //测试网速间隔
    /**
     * 0 = 快<br>
     * 1 = 普通<br>
     * 2 = 慢<br>
     * 3 = 差<br>
     */
    public static byte netSpeedLevel;
    private static int[] netSpeedShowColor = {
                    0x00ff33, 0xffff00, 0xff9900, 0xff0000
    };

    public static void parseSystemOption(){
        //closeConnection = systemOption[0] == 1;
        topbarFlag = systemOption[1];
        miniMapOption = (systemOption[2] < 0 || systemOption[2] > 2)? 0: systemOption[2];
        closeMonIconDownload = systemOption[3] == 1;
        closeNpcImgDownload = closeMonImageDownload = systemOption[4] == 1;
        closeAutoMove = systemOption[5] == 1;
        nameFlag = systemOption[6];
        titleFlag = systemOption[7];

        if(systemOption[8] == 0){
            draw3DStringMode = defaultDraw3DStringMode;
        }else{
            draw3DStringMode = (byte)systemOption[8];
        }

        if(systemOption[9] == 0){
            drawNetPlayerSize = defaultDrawNetPlayerSize;
        }else{
            switch(systemOption[9]){
                case DRAW_NET_PLAYER_5:
                    drawNetPlayerSize = 5;

                    break;
                case DRAW_NET_PLAYER_10:
                    drawNetPlayerSize = 10;

                    break;
                case DRAW_NET_PLAYER_15:
                    drawNetPlayerSize = 15;

                    break;
                case DRAW_NET_PLAYER_20:
                    drawNetPlayerSize = 20;

                    break;
            }
        }
    }

    //=====================================

    public static short[] systemOption = new short[10];
    public static int systemOptionCount = 10;

    public static final byte DRAW_3D_STRING_3D = 1;
    public static final byte DRAW_3D_STRING_SHADOW = 2;
    public static final byte DRAW_3D_STRING_NORMAL = 3;

    public static final byte DRAW_NET_PLAYER_5 = 1;
    public static final byte DRAW_NET_PLAYER_10 = 2;
    public static final byte DRAW_NET_PLAYER_15 = 3;
    public static final byte DRAW_NET_PLAYER_20 = 4;

    //#if (Directory == SE-S700)
    //# public static final byte defaultDraw3DStringMode = DRAW_3D_STRING_SHADOW;
    //#else
    public static final byte defaultDraw3DStringMode = DRAW_3D_STRING_3D;
    //#endif
    public static final byte defaultDrawNetPlayerSize = 20;

    public static byte draw3DStringMode = defaultDraw3DStringMode;
    public static byte drawNetPlayerSize = defaultDrawNetPlayerSize;

    public static final int MINIMAP_MAX_WIDTH = 64;
    public static final int MINIMAP_MAX_HEIGHT = 64;


    private static final int MINI_COLOR_NPC_NO_TASK = 0x0000FF;
    private static final int MINI_COLOR_NPC_HAS_TASK = 0xFFA000;
    private static final int MINI_COLOR_NPC_DOING_TASK = 0x808080;
    private static final int MINI_COLOR_NPC_FINISH_TASK = 0xFFFF00;
    private static final int MINI_COLOR_MONSTER = 0xFF0000;
    private static final int MINI_COLOR_DOOR = 0x00FF00;

    //#if (ModelID != NK-60-1) && (ModelID != NK-60-2)
    private static Image miniMapImage;
    //#endif
    private static int miniMapPosX = 30;
    private static int miniMapPosY = 30;
    private static int[] miniMapPlayerColor = new int[]{
                    0x000000, 0xFFFFFF
    };
    private static int[] miniMapHasTaskNpcColor = new int[]{
                    0x0000FF, MINI_COLOR_NPC_HAS_TASK
    };
    private static int[] miniMapFinishTaskNpcColor = new int[]{
        0x0000FF, MINI_COLOR_NPC_FINISH_TASK
    };
    private static int[] miniMapDoorColor = new int[]{
        0xFF0000, MINI_COLOR_DOOR
    };

    private static int miniMapEdgeColor = 0xFFFF00;
    private static int miniMapAlpha = 0x80000000;
    private static int miniMapPlayerColorIndex = 0;
    private static int miniMapFlashFreq = 4;
    private static int miniMapFlashCycle = 0;
    private static short[][] randomMapData;
    private static int randomMapSeed;
    private static int randomMapWidth;
    private static int randomMapHeight;
    private static int randomMapBufferLines = 1000;
    private static int randomMapX;
    private static int randomMapY;

    private static byte[] landMapping;

    public static short currMapId = -1;
    public static String currMapName = "";
    public static short areaId = -1;
    private static byte _mapId = -1;
    public static byte _defaultMapId = -1;
    public static byte _defaultX = -1;
    public static byte _defaultY = -1;
    private static byte _mapType = 0;
    private static short[][] mapNpcData = null;
    public static byte _mapPkType = 0;
    private static MonsterSprite[] npcs = null;
    public static MonsterSprite[] monsters = null;
    public static Hashtable monsterRefreshPool = new Hashtable();
    private static Hashtable npcIdTable = new Hashtable();

    public static final String[] MAP_PK_TYPE = {
                    " [自由地区]", " [安全地区]", " [争夺地区]"
    };

    /**
     * [0] x, [1] y, [2] 目标地图ID, [3] dx, [4] dy, [5]~[6] CollisionBox x & y, [7] showname
     */
    private static short[][] mapDoor = null;
    private static String[] mapTargetName = null; // 目标地图名称

    private static int touchingNpcId = -1;

    private static int npcHintStamp = 0;
    public static boolean npcNeedHint = false;

    public static final int NPC_HINT_FREQ = 5;

    public static boolean chat_input_doing = false;
    public static String chat_input_uncompelte_message = null;
    //#if (Revision != JIANGSUNCMCC)
    //#  public static final String[] HOTKEYS = {
    //#    "发起聊天", "周围玩家", "", "物品背包", "", GameState.IMONEY_SALE,"", "地图开关", "", "改变称号", "回复密聊", "聊天记录"
    //# };

    //# public static final String[] TEAMMODE_HOTKEYS = {
    //#   "发起聊天", "", "", "", "", "", "", "地图开关", "", "改变称号", "回复密聊", ""
    //#  };
    //#else
    public static final String[] HOTKEYS = {
        "发起聊天", "周围玩家", "", "物品背包", "", GameState.IMONEY_SALE,"", "地图开关", "", "改变称号", "江苏好友推荐", "聊天记录"
    };

    public static final String[] TEAMMODE_HOTKEYS = {
        "发起聊天", "", "", "", "", "", "", "地图开关", "", "改变称号", "好友推荐", ""
    };
    //#endif
    

    /**
     * [0] id, [1] x, [2] y, [3] level, [4] type, [5] needGame, [6] visible [7] itemid
     */
    private static int[][] mapResource = null;

    private static int[] resourceColor = {
                    0xff0000, 0xFF7200, 0xffff00, 0x00ff00, 0x808080
    };

    private static short[] yOrder = null; //每次循环生成，按Y轴顺序排列，处理遮挡问题，格式 [0] type，[1] index，[2] y
    private static int yOrderCount = 0;

    private static byte[] shapeTile = new byte[]{
                    -1, 9, 11, 1, 10, 3, -1, 5, 12, -1, 4, 7, 2, 6, 8, 0
    };

    private static int[] sortTable = {
                    1, 4, 10, 23, 57, 132, 301, 701, 1577, 3548, 7983, 17961, 40412, 90927, 204585, 460316, 1035711, 2330349
    };

    private static boolean useImageBuffer = true;

    private static boolean isFirstBgImage = true;
    private static Image bgImg;
    private static Graphics gg;
    private static int oldStartX, oldStartY, bgCellW, bgCellH;
    private static int bgWidth, bgHeight;
    private static int oldEndX, oldEndY;
    private static int vx, vy; //使屏幕震动

    //#if ResourceCache == TRUE
    private static Hashtable _localCache = new Hashtable();
    //#endif
    private static Hashtable _packageCache = new Hashtable();

    private static Hashtable _imageCache = new Hashtable();

    private static ImageSet[] mapImageSet = null;

    public static final byte REFRESH_TYPE_NPC = 0;
    public static final byte REFRESH_TYPE_MONSTER = 1;
    public static final byte REFRESH_TYPE_RESOURCE = 2;
    /**
     * 默认图片ImageSet，0- 默认NPC图片  1- 默认怪物图片 2- 默认资源图片 3-默认怪物图标 4-默认物品npc图片
     */
    public static Image chatAlpha = null;

    public static ImageSet[] defaultImageSet = new ImageSet[5];

    // 下面的变量存储所有形象相关的图片，包括：行走图片、战斗图片、战斗光效图片
    public static byte[] faceIndex = new byte[64]; // 存储形象ID->图片数组下标对应关系，1表示第1个，2表示第2个，0表示不存在

    public static int getFaceIndex(int face, boolean isBattle){
        //#if (Directory == NK-6681) || (Directory == MT-V300)
        //# return face & 0x01;
        //#else
        if(!isBattle && useCommonFace){
            return face & 0x01;
        }
        if(faceIndex[face] > 0){
            return faceIndex[face] - 1;
        }else{
            return face & 0x01;
        }
        //#endif
    }

    public static ImageSet getPlayerImage(int index){
        if(playerImageSet[index] == null && faceData[index] != null){
            Object[] tmp = (Object[])faceData[index];
            byte[] pdata = (byte[])tmp[0];
            byte[] sdata = (byte[])tmp[1];
            try{
                playerImageSet[index] = ImageSet.createImageSet(Image.createImage(pdata, 0, pdata.length), new DataInputStream(new ByteArrayInputStream(sdata)), true);
            }catch(Exception e){
            }
        }
        return playerImageSet[index];
    }

    public static boolean useCommonFace = false;
    public static Object[] faceData = new Object[5]; // 下载形象图片数组，数组元素是一个byte[]的数组，其中固定包括8个元素，
    // 分别存储形象相关的4个图片(行走,战斗,头像,战斗光效)的.p文件和.s文件。
    public static ImageSet[] playerImageSet = new ImageSet[5]; // 行走图片。目前版本最多存储5个形象
    public static ImageSet[] attackImg = new ImageSet[5]; // 战斗图片
    public static ImageSet[] attackWeaponImg = new ImageSet[5]; // 战斗光效图片
    public static Image playerHead;

    public static ImageSet charImageSet;
    public static ImageSet bodyImg;

    public static ImageSet taskHint;

    public static Hashtable netPlayers = new Hashtable();
    public static Vector netPlayersVector = new Vector();
    public static Hashtable netFriends = new Hashtable();
    public static Hashtable blackList = new Hashtable();
    public static boolean netFriendsNeedShowStatus = false;

    public static final byte NETPLAYERS_LIMIT = 20;

    //TODO delete it
    public static ImageSet dieImageSet = null;
    public static ImageSet effectImageSet = null;
    public static ImageSet[] resourceImage;
    public static ImageSet doorImageSet = null;
    public static ImageSet petImageSet = null;
    public static Image sealImage = null;
    public static Image protectImage = null;
    public static int protectImageWidth = 0;

    public static Image[] hintImage = null;
    public static byte[] hintStatus = new byte[3];
    public static short[] hintDrawOffset = new short[3];
    public static long[] hintShowTime = new long[3];
    public static long[] hintFlashTime = new long[3];

    public static final byte HINT_MAIL = 0;
    public static final byte HINT_MESSAGE = 1;
    public static final byte HINT_BAGFULL = 2;

    public static final byte HINT_HIDE = 0;
    public static final byte HINT_SHOWING_DRAW = 1;
    public static final byte HINT_SHOWING_NO_DRAW = 2;

    public static final int HINT_DELAY = 300000;
    public static final int HINT_FLASH_DELAY = 500;

    //聊天信息数据
    public static byte[] net_chat_priority_option = {
                    1, 1, 1, 2, 2, 2, 2, 2
    };

    public static byte[] net_chat_color_option = {
                    3, 7, 8, 5, 15, 13, 4, 9
    };

    public static boolean[] net_chat_can_change_option = {
                    true, true, true, true, true, true, false, true
    };

    public static Vector net_chat_high_priority_message = new Vector();
    public static Vector net_chat_low_priority_message = new Vector();
    public static String[] net_chat_current_message = null;
    public static boolean net_chat_showing = false;
    public static int net_chat_current_color;
    public static int net_chat_current_offset;
    public static int net_chat_scroll_flag;
    public static int net_chat_current_channel;

    public static final int NET_CHAT_LASTMSGAMOUNT = 5;
    public static Vector net_chat_lastMsgSource = new Vector(NET_CHAT_LASTMSGAMOUNT);

    public static final int NET_CHAT_SCROLL_SPEED = 0;
    public static final int NET_CHAT_QUEUE_SIZE = 20;

    public static final byte NET_CHAT_TYPE_WORLD = -1;
    public static final byte NET_CHAT_TYPE_STAGE = -2;
    public static final byte NET_CHAT_TYPE_FACTION = -3;
    public static final byte NET_CHAT_TYPE_GROUP = -4;
    public static final byte NET_CHAT_TYPE_TEAM = -5;
    public static final byte NET_CHAT_TYPE_CIRCLE = -6;
    public static final byte NET_CHAT_TYPE_SYSTEM = -7;
    public static final byte NET_CHAT_TYPE_PRIVATE = -8;

    public static final String[] NET_CHAT_NAME = {
                    "[世界]", "[地区]", "[公会]", "[团队]", "[小队]", "[圈]", "[系统]", "[私聊]"
    };

    public static final String[] NET_CHAT_PRIORITY = {
                    "关闭", "普通", "优先"
    };

    private static final byte MAP_RANDOM = 0;
    private static final byte MAP_PRECISION = 1;

    private static final byte DRAW_ITMES_PLAYER = 0;
    private static final byte DRAW_ITMES_MAPNPC = 1;
    private static final byte DRAW_ITEMS_NPC = 2;
    private static final byte DRAW_ITEMS_MONSTERICON = 3;
    private static final byte DRAW_ITEMS_RESOURCE = 4;
    private static final byte DRAW_ITEMS_DOOR = 5;
    private static final byte DRAW_NETPLAYER = 6;
    private static final byte DRAW_TEAMMEMBER = 7;
    private static final byte DRAW_ITEMS_PET = 8;
    private static final byte DRAW_ITEMS_MEMBER_PET = 9;

    //#if (polish.identifier == Nokia/Series40Midp2) || (polish.identifier == NK-6255) || (Directory == SE-K500) || (Directory == SE-K300) || (Directory == Nokia403)
    //# public static int MESSAGEBOX_WIDTH = 110;
    //# public static final int MESSAGEBOX_HEIGHT = 59;
    //#elif (Directory == NK-BigScreen) || (Directory == NK-Nokia403Big) || (Directory == SE-S700)
    //# public static int MESSAGEBOX_WIDTH = 220;
    //# public static final int MESSAGEBOX_HEIGHT = 59;
    //#else
    public static int MESSAGEBOX_WIDTH = 160;
    public static final int MESSAGEBOX_HEIGHT = 59;
    //#endif
    public static final int TALKBOX_HEIGHT = 36;

    //define constant to handle game key states
    public static final byte UP_PRESSED = 0;
    public static final byte DOWN_PRESSED = 1;
    public static final byte LEFT_PRESSED = 2;
    public static final byte RIGHT_PRESSED = 3;
    public static final byte FIRE_PRESSED = 4;
    public static final byte GAME_A_PRESSED = 5;
    public static final byte GAME_B_PRESSED = 6;
    public static final byte GAME_C_PRESSED = 7;
    public static final byte GAME_D_PRESSED = 8;

    //define constant to handle soft key states
    public static final byte SOFT_FIRST_PRESSED = 9;
    public static final byte SOFT_LAST_PRESSED = 10;

    //define constant to handle number key states
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

    // 每两位表示一个键，高位表示是否按下（1为按下），低位表示是否处理（1表示未处理）
    public static long keyFlag;
    public static long keyFlag2;

    //    private static final boolean OFFLINE_MODE = false;

    public static boolean GOD_MODE = false;
    //    public static boolean SHOW_FPS = true;
    public static boolean SHOW_BATTLELOG = true;

    public static Random randGen = new Random(System.currentTimeMillis());

    public static final byte NPC_NORMAL = 0;
    public static final byte NPC_TYPE_BBS = 1;
    public static final byte NPC_TYPE_PRODUCT_TEACHER = 2;
    public static final byte NPC_TYPE_BATTLE_TEACHER = 3;
    public static final byte NPC_TYPE_BUSINESS = 4;
    public static final byte NPC_TYPE_STORE = 5;
    public static final byte NPC_TYPE_ITEMNPC = 6;
    public static final byte NPC_TYPE_TONG = 7;
    public static final byte NPC_TYPE_PET = 8;
    public static final byte NPC_TYPE_INSTANCE = 9;
    public static final byte NPC_TYPE_REPAIR = 10;

    public static final short TASK_ID_BBS = 30001;
    public static final short TASK_ID_PRODUCT_TEACHER = 30002;
    public static final short TASK_ID_BATTLE_TEACHER = 30003;
    public static final short TASK_ID_PRODUCE_ITEM = 30004;
    public static final short TASK_ID_VIEW_BATTLE_SKILL = 30005;
    public static final short TASK_ID_RESOURCE_GAME = 30006;
    public static final short TASK_ID_CHAT_CIRCLE_OPTION = 30007;
    public static final short TASK_ID_TASK_VIEW = 30008;
    public static final short TASK_ID_PLAYER_VIEW = 30009;
    public static final short TASK_ID_FRIEND_VIEW = 30010;
    public static final short TASK_ID_MAIL_VIEW = 30011;
    public static final short TASK_ID_SHOP = 30012;
    public static final short TASK_ID_AUCTION = 30013;
    public static final short TASK_ID_BUY_MATERIAL = 30014;
    public static final short TASK_ID_OEM = 30015;
    public static final short TASK_ID_STORE = 30016;
    public static final short TASK_ID_TONG_LIST = 30017;
    public static final short TASK_ID_CHAT_OPTION = 30018;
    public static final short TASK_ID_PET_OPTION = 30019;
    public static final short TASK_ID_PET_TRADE = 30020;
    public static final short TASK_ID_SYSTEM_OPTION = 30021;
    public static final short TASK_ID_REPAIR = 30022;
    public static final short TASK_ID_FAQ = 30023;
    public static final short TASK_ID_HELP = 30024;

    //-------------ImageSet Cache-------------//
    public static final int CACHE_LENGTH = 8;
    public static CacheImageSet[] caches = new CacheImageSet[CACHE_LENGTH];
    public static byte cachePointer = -1;
    public static boolean cacheChanged = false;

    /*---------------RMS--------------*/
    public static final String CACHE_DB = "cache";
    public static final String TASKUI_DATA = "taskui_data";
    public static final String RMS_DATA = "itimes_data";
    public static final String DOWNLOAD_CODE_DATA = "itimes_dc";
    public static Hashtable taskUICache = new Hashtable();

    /*---------------Team--------------*/

    public static final byte TEAM_STATUS_NOTFOLLOW = 0;
    public static final byte TEAM_STATUS_FOLLOW = 1;
    public static final byte TEAM_STATUS_LEAVE = 2;

    public static final byte TEAM_INVITE_ACCEPT = 0;
    public static final byte TEAM_INVITE_REFUSE = 1;

    public static boolean teamMode = false;
    public static boolean teamLeader = false;
    public static byte teamStatus = TEAM_STATUS_LEAVE;
    public static int teamId = -1;
    public static int teamLeaderId = -1;

    public static Vector teamMembers = new Vector();

    public static MonsterSprite findTeamMember(int playerID){
        MonsterSprite result = null;
        for(int i = 0; i < teamMembers.size(); i++){
            MonsterSprite teamMember = (MonsterSprite)teamMembers.elementAt(i);

            if(teamMember.id == playerID){
                result = teamMember;

                break;
            }
        }

        return result;
    }

    public static void addTeamMember(MonsterSprite addTeamMember){
        MonsterSprite teamMember = findTeamMember(addTeamMember.id);

        if(teamMember != null){
            teamMember.teamRole = addTeamMember.teamRole;
            teamMember.followMode = addTeamMember.followMode;
            teamMember.playerName = addTeamMember.playerName;
            teamMember.imageSet = addTeamMember.imageSet;
            teamMember.petCurrent = addTeamMember.petCurrent;
            teamMember.iconID = addTeamMember.iconID;
        }else{
            teamMembers.addElement(addTeamMember);
        }
    }

    public static void removeTeamMember(int playerID){
        MonsterSprite teamMember = findTeamMember(playerID);

        if(teamMember != null){
            MonsterSprite netPlayer = findInNetPlayers(playerID);

            if(netPlayer != null){
                processNetPlayerLeaveTeam(netPlayer, teamMember);
            }

            teamMembers.removeElement(teamMember);
        }
    }

    public static void clearTeamMember(){
        for(int i = 0; i < teamMembers.size(); i++){
            MonsterSprite teamMember = (MonsterSprite)teamMembers.elementAt(i);
            MonsterSprite netPlayer = findInNetPlayers(teamMember.id);

            if(netPlayer != null){
                processNetPlayerLeaveTeam(netPlayer, teamMember);
            }
        }

        teamMembers.removeAllElements();
    }

    public static void processNetPlayerLeaveTeam(MonsterSprite netPlayer, MonsterSprite teamMember){
        if(teamMember.alertRange != currMapId){
            removeNetPlayerData(teamMember.id);
        }else{
            netPlayer.teamRole = MonsterSprite.TEAM_ROLE_NONE;
            netPlayer.followMode = false;
            netPlayer.rx = teamMember.rx;
            netPlayer.ry = teamMember.ry;
            netPlayer.x = teamMember.x;
            netPlayer.y = teamMember.y;
            netPlayer.alertRange = teamMember.alertRange;
            netPlayer.wpList = null;
        }
    }

    public static void reGroupTeam(){
        if(teamMembers.size() >= 3){
            MonsterSprite leader = (MonsterSprite)teamMembers.elementAt(0);
            MonsterSprite member1 = (MonsterSprite)teamMembers.elementAt(1);
            MonsterSprite member2 = (MonsterSprite)teamMembers.elementAt(2);

            if(!member1.followMode && member2.followMode){
                teamMembers.removeAllElements();

                teamMembers.addElement(leader);
                teamMembers.addElement(member2);
                teamMembers.addElement(member1);
            }
        }
    }

    public static void updateTeamMemberInfo(MonsterSprite teamMember, short mapId, int x, int y){
        teamMember.alertRange = mapId;
        teamMember.rx = x;
        teamMember.ry = y;
        teamMember.x = -1;
        teamMember.y = -1;
        teamMember.wpList = null;

        if(teamMember.petCurrent != null && teamMember.id != player.playerID){
            teamMember.petCurrent.x = (short)x;
            teamMember.petCurrent.y = (short)y;
        }
    }

    public static void resetNetPlayer(){
        Vector needAdd = new Vector();

        for(int i = 0; i < teamMembers.size(); i++){
            MonsterSprite teamMember = (MonsterSprite)teamMembers.elementAt(i);
            MonsterSprite netPlayer = findInNetPlayers(teamMember.id);

            if(teamMember.id == World.player.playerID){
                continue;
            }

            if(netPlayer != null){
                needAdd.addElement(netPlayer);
            }else{
                needAdd.addElement(teamMember);
            }
        }

        netPlayers.clear();
        netPlayersVector.removeAllElements();

        for(int i = 0; i < needAdd.size(); i++){
            MonsterSprite netPlayer = (MonsterSprite)needAdd.elementAt(i);
            addNetPlayerData(netPlayer, true);
        }
    }

    public static void moveTeamFollowToMap(short mapId, int x, int y){
        for(int i = 0; i < teamMembers.size(); i++){
            MonsterSprite teamMember = (MonsterSprite)teamMembers.elementAt(i);

            if(teamMember.followMode){
                updateTeamMemberInfo(teamMember, mapId, x, y);
            }
        }
    }

    public static void updateTeamMemberLocation(){
        MonsterSprite destPlayer = (MonsterSprite)teamMembers.elementAt(0);

        if(teamLeader){
            destPlayer.rx = World.player.x;
            destPlayer.ry = World.player.y;
        }else if(destPlayer.alertRange != currMapId){
            return;
        }

        for(int i = 1; i < teamMembers.size(); i++){
            MonsterSprite teamMember = (MonsterSprite)teamMembers.elementAt(i);

            if(!teamMember.followMode){
                continue;
            }else{
                teamMember.wpList = null;
            }

            if(teamMember.alertRange != destPlayer.alertRange || destPlayer.alertRange == -1){
                if(destPlayer.alertRange != -1){
                    updateTeamMemberInfo(teamMember, destPlayer.alertRange, destPlayer.rx, destPlayer.ry);
                }

                continue;
            }

            int[] dest = null;
            byte destDir = -1;

            if(destPlayer.id == player.playerID){
                dest = player.getBackXY();
                destDir = player.direct;
            }else{
                dest = destPlayer.getBackXY();
                destDir = destPlayer.dir;
            }

            if(dest != null){
                if(teamMember.id == player.playerID){
                    int dx = 0, dy = 0;

                    dx = player.x - dest[0];
                    dx *= dx;

                    dy = player.y - dest[1];
                    dy *= dy;

                    if(dx + dy > (viewWidth / 2) * (viewWidth / 2)){
                        player.x = (short)dest[0];
                        player.y = (short)dest[1];
                        player.direct = destDir;

                        if(player.frameSequence != Sprite.FRAMESEQUENCE_STAND[destDir]){
                            player.frameSequence = Sprite.FRAMESEQUENCE_STAND[destDir];
                        }

                        player.wpList = null;
                        bgImg = null;

                        moveMap();
                    }else if(player.wpMoveTo(dest[0], dest[1])){
                        if(player.frameSequence != Sprite.FRAMESEQUENCE_STAND[player.direct]){
                            player.frameSequence = Sprite.FRAMESEQUENCE_STAND[player.direct];
                        }
                    }else{
                        if(player.frameSequence != Sprite.FRAMESEQUENCE_WALK[player.direct]){
                            player.frameSequence = Sprite.FRAMESEQUENCE_WALK[player.direct];
                        }
                    }
                }else{
                    if(teamMember.moveTo(dest[0], dest[1])){
                        if(teamMember.frameSequence != Sprite.FRAMESEQUENCE_STAND){
                            teamMember.setFrameSequence(Sprite.FRAMESEQUENCE_STAND);
                        }
                    }else{
                        if(teamMember.frameSequence != Sprite.FRAMESEQUENCE_WALK){
                            teamMember.setFrameSequence(Sprite.FRAMESEQUENCE_WALK);
                        }
                    }
                }
            }

            destPlayer = teamMember;
        }
    }

    public static MonsterSprite findInNetPlayers(int playerID){
        return (MonsterSprite)netPlayers.get(new Integer(playerID));
    }

    /*---------- Game Functions Begin----------*/

    public World(){
        //#ifdef GameCanvas
        super(false);
        setFullScreenMode(true);
        //#else
        //# super();
        //#endif

        instance = this;
        //#if M-Name == LG_876
        //#     viewWidth = (short)176;
        //#     viewHeight = (short)208;
        //#elif (M-Name == NK_5500)
        //#     viewWidth = (short)208;
        //#     viewHeight = (short)208;  
        //#elif Directory == NK-E61
        //#     viewWidth = (short)320;
        //#     viewHeight = (short)240;
        //#elif Directory == NK-BigScreen
        //#     viewWidth = (short)240;
        //#     viewHeight = (short)320;
        //#elif (Directory == NK-Nokia403Big) || (Directory == SE-S700)
        //#     viewWidth = (short)240;
        //#     viewHeight = (short)320;
        //#elif Directory == MT-General
        //# viewWidth = (short)getWidth();
        //# if(viewWidth == 176)
        //# 	viewHeight = 204;
        //# else if(viewWidth ==240)
        //# 	viewHeight = 320;
        //# else
        //# 	viewHeight = (short)getHeight();
        //#elif (Directory == Midp2-General) || (MIDP2Common == true)
        viewWidth = (short)getWidth();
        viewHeight = (short)getHeight();
        //#elif polish.identifier == Nokia/Series60
        //# viewWidth = (short)176;
        //# viewHeight = (short)208;
        //#elif polish.identifier == Motorola/V300
        //#     viewWidth = (short)176;
        //#     viewHeight = (short)204;
        //#elif (polish.identifier == Nokia/Series40Midp2) || (Directory == SE-K300)
        //#     viewWidth = (short)128;
        //#     viewHeight = (short)128;
        //#elif polish.identifier == Sony-Ericsson/K700
        //#     viewWidth = (short)176;
        //#     viewHeight = (short)220;
        //#elif (Directory == SE-K500) || (Directory == Nokia403)
        //#     viewWidth = (short)128;
        //#     viewHeight = (short)160;
        //#elif (Directory == ClientTouch-E680) || (Directory == ClientTouch-SE-General)
        //#     viewWidth = (short)240;
        //#     viewHeight = (short)320;
        //#elif (Directory == ClientTouch-Nokia5800)
        //#  	viewWidth = (short)360;
        //#   	viewHeight = (short)640;
        //#elif polish.identifier == Nokia/Series60Midp2
        //#     viewWidth = (short)176;
        //#     viewHeight = (short)208;
        //#else
        //#     viewWidth = (short)getWidth();
        //#     viewHeight = (short)getHeight();
        //#endif

        viewMaxX = -1;
        viewMaxY = -1;
        
        MESSAGEBOX_WIDTH = viewWidth * 9 / 10;

        //#if Revision == QQ
        //# GameState.name = "";
        //# GameState.password = "";
        //#else
        byte[] tmp = World.getData(RMS_DATA, (byte)1);

        if(tmp != null){
            GameState.name = bytesToString(tmp);

            if(GameState.name != null && GameState.name.trim().length() > 0){
                GameState.GAME_MENU[0] = GameState.GAMEMENU_FASTLOGIN;
            }else{
                GameState.GAME_MENU[0] = GameState.GAMEMENU_FASTREG;
            }
        }

        //#if (Revision == PIP) || (Revision == SOHU) || (Revision == DOWNJOY) || (Revision == JIANGSUN)
        if(GameState.downloadCode.trim().length() == 0){
            tmp = World.getData(DOWNLOAD_CODE_DATA, (byte)1);

            if(tmp != null){
                GameState.downloadCode = bytesToString(tmp);
            }else{
                GameState.downloadCode = String.valueOf(System.currentTimeMillis());
                tmp = stringToBytes(GameState.downloadCode);
                World.saveData(DOWNLOAD_CODE_DATA, tmp, (byte)1);
            }
        }
        //#endif

        //#endif

        //#if Revision == CMCC || (Revision == JIANGSUNCMCC) || (Revision == PIP) ||( Revision == DOWNJOY)
        gameState = new GameState(GameState.STATE_GAMEMENU);
        //#elif Revision == QQ ||(Revision == SOHU) || (Revision == JIANGSUN)
        //# gameState = new GameState(GameState.STATE_SPLASH);
        
        //#endif

        //#if CommandEmu == true
        //# addCommands(commandOK, commandBack);
        //#endif
      //#if Revision == QQ && TestVersion == true
      //# for(int i=0;i<14000;i++){
      //# k700Test[i]=1;
      //# }
        //#endif
      //#if TouchScreen == true
        try {
			ok = Image.createImage("/ok.png");
			chat = Image.createImage("/chat.png");
			autobattle = Image.createImage("/auto.png");
        	skilllevel = Image.createImage("/skilllevel.png");
        	up = Image.createImage("/up.png");
        	down = Image.createImage("/down.png");
        	left = Image.createImage("/left.png");
        	right = Image.createImage("/right.png");
        	players = Image.createImage("/players.png");
        	bag = Image.createImage("/bag1.png");
        	smap = Image.createImage("/map.png");
        	name = Image.createImage("/name.png");
        	//#if Revision != JIANGSUNCMCC 
        	recall = Image.createImage("/recall.png");
        	//#else
        	//# recall = Image.createImage("/remmend.png");
        	//#endif
        	msg = Image.createImage("/msg.png");
        	//增加图片
        	menu = Image.createImage("/menu.png");
        	turn = Image.createImage("/cancel.png");
        	leftSpread = Image.createImage("/leftspread.png");
        	rightSpread = Image.createImage("/rightspread.png");
        	imoney = Image.createImage("/imoney.png");
        	sayhello = Image.createImage("/sayhello.png");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//#endif
    }

    public static void init(){
        try{

            initCommonImageSet();
            initTaskUIData();
            initPlayer(100, 100);

            //#if CacheImage == TRUE
            loadCacheFromRMS();
            //#endif

        }catch(Exception e){
            //#debug
            e.printStackTrace();
        }
    }

    public static void initTaskUIData(){
        byte[] taskUIIndex = getData(TASKUI_DATA, (byte)0);

        if(taskUIIndex == null || true){
            try{
                RecordStore.deleteRecordStore(TASKUI_DATA);
            }catch(Exception e){
                //#debug
                e.printStackTrace();
            }

            taskUICache.clear();
            taskUIIndex = getTaskUIIndexBytes();
            saveData(TASKUI_DATA, taskUIIndex, (byte)0);
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(taskUIIndex);
        DataInputStream dis = new DataInputStream(bis);

        try{
            int count = dis.readShort();

            for(int i = 0; i < count; i++){
                Integer taskId = new Integer(dis.readShort());
                Integer taskIndex = new Integer(dis.readShort());

                taskUICache.put(taskId, taskIndex);
            }
        }catch(Exception e){
            //#debug
            e.printStackTrace();
        }finally{
            try{
                dis.close();
            }catch(Exception e){
            }
        }
    }

    public static short getTaskUIMaxIndex(){
        short maxIndex = 0;

        Enumeration emu = taskUICache.elements();

        while(emu.hasMoreElements()){
            Integer index = (Integer)emu.nextElement();

            if(index.intValue() > maxIndex){
                maxIndex = (short)index.intValue();
            }
        }

        return maxIndex;
    }

    public static byte[] getTaskUIIndexBytes(){
        short count;
        short taskId;
        short taskIndex;

        count = (short)taskUICache.size();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        try{
            dos.writeShort(count);

            Enumeration emu = taskUICache.keys();

            while(emu.hasMoreElements()){
                Integer id = (Integer)emu.nextElement();
                Integer index = (Integer)taskUICache.get(id);

                taskId = (short)id.intValue();
                taskIndex = (short)index.intValue();

                dos.writeShort(taskId);
                dos.writeShort(taskIndex);
            }

            return bos.toByteArray();
        }catch(Exception e){
            //#debug
            e.printStackTrace();
        }finally{
            try{
                dos.close();
            }catch(Exception e){
            }
        }

        return null;
    }

    public static void initPlayer(int x, int y){
        player = new Sprite(playerImageSet[0], (short)x, (short)y);
        instance.playerSprite = new Sprite[]{
                        player, null, null
        };
    }

    public static void initVM(byte[] data){
        _gtvm = new GTVM();
        _gtvm.init(data);
    }

    public static void initBattleImageSet(){
        effectImageSet = getImageSetFromLocal("effect.p");
        dieImageSet = getImageSetFromLocal("die.p");
        World.attackImg[0] = getImageSetFromLocal("da_male");
        World.attackImg[1] = getImageSetFromLocal("da_female");
        World.attackWeaponImg[0] = getImageSetFromLocal("da_male_weapon");
        //#if (Directory == NK-6681) || (Directory == MT-V300)
        //# World.attackWeaponImg[1] = World.attackWeaponImg[0];
        //#else
        World.attackWeaponImg[1] = getImageSetFromLocal("da_female_weapon");
        //#endif
    }

    public static void initCommonImageSet(){
        defaultImageSet[0] = getImageSetFromLocal("defaultNpc.p");
        defaultImageSet[1] = getImageSetFromLocal("defaultArmy.p");
        defaultImageSet[3] = getImageSetFromLocal("defaultMonster.p");
        defaultImageSet[4] = getImageSetFromLocal("defaultItemNpc.p");
        playerImageSet[0] = getImageSetFromLocal("_male");
        playerImageSet[1] = getImageSetFromLocal("_female");
        doorImageSet = getImageSetFromLocal("door.p");
        petImageSet = getImageSetFromLocal("pet.p");
        charImageSet = getImageSetFromLocal("chars");
        bodyImg = World.getImageSetFromLocal("body");

        //#if (Directory == SE-K700) || (Directory == SE-K500) || (Directory == SE-K300) || (Directory == SE-S700)
        /*_localCache.remove("defaultNpc.p");
        _localCache.remove("defaultArmy.p");
        _localCache.remove("defaultMonster.p");
        _localCache.remove("defaultItemNpc.p");
        _localCache.remove("pet.p");*/
        //#endif
        try{
            hintImage = new Image[3];

            hintImage[0] = Image.createImage("/mail.png");
            hintImage[1] = Image.createImage("/message.png");
            hintImage[2] = Image.createImage("/bag.png");

            hintDrawOffset[0] = (short)hintImage[0].getWidth();
            hintDrawOffset[1] = (short)hintImage[1].getWidth();
            hintDrawOffset[2] = (short)hintImage[2].getWidth();

            sealImage = Image.createImage("/seal.png");
            protectImage = Image.createImage("/pt.png");
            protectImageWidth = protectImage.getWidth();
        }catch(Exception e){
            //#debug
            e.printStackTrace();
        }
        initDefaultImageSet();
        //#if LoadAllImage == TRUE
        //# initBattleImageSet();
        //#endif
    }

    //#if LoadAllImage == FALSE
    public static void clearBattleImageSet(){
        effectImageSet = null;
        dieImageSet = null;
        World.attackImg = new ImageSet[5];
        World.attackWeaponImg = new ImageSet[5];
    }

    public static void clearDefaultImageSet(){
        Sprite.bufIcon = null;
        resourceImage = new ImageSet[4];
        taskHint = null;
    }

    //#endif

    public static void initDefaultImageSet(){
        Sprite.bufIcon = getImageSetFromLocal("buf");
        try{
            resourceImage = new ImageSet[4];

            for(int i = 0; i < resourceImage.length; i++){
                resourceImage[i] = getImageSetFromLocal("res" + i + ".p");
            }
        }catch(Exception e){
            //#debug
            e.printStackTrace();
        }

        taskHint = getImageSetFromLocal("!");
    }

    public static void release(){
        GameState.isMapLoadOk = false;
        instance.endBattle();
        //effectImageSet = null;
        events.removeAllElements();
        //#if CacheImage == TRUE
        saveCacheToRMS();
        //#endif
        cachePointer = -1;
        caches = new CacheImageSet[CACHE_LENGTH];

        player = null;
        _packageCache.clear();
        _imageCache.clear();
        map = null;
        mapDoor = null;
        mapImageSet = null;
        mapNpcData = null;
        areaId = -1;
        currMapId = -1;
        yOrder = null;
        yOrderCount = 0;
        npcs = null;
        randomMapData = null;
        npcIdTable.clear();
        mapResource = null;
        finishedTask.clear();
        unFinishedTask.clear();
        monsterSetPlayerBattle = true ;
        monsterSetPetBattle =false;
        releaseTeam();

        netPlayers.clear();
        netPlayersVector.removeAllElements();

        netFriends.clear();
        netFriendsNeedShowStatus = false;

        blackList.clear();

        //#if (ModelID != NK-60-1) && (ModelID != NK-60-2)
        miniMapImage = null;
        //#else
        //# miniMapArrayShort = null;
        //#endif

        playerHead = null;
        faceIndex = new byte[64];

        useCommonFace = false;
        faceData = new Object[5];
        attackImg = new ImageSet[5];
        attackWeaponImg = new ImageSet[5];

        //bodyImg = null;

        npcHintStamp = 0;
        npcNeedHint = false;
        chat_input_doing = false;
        clearNetChatMessage();

        GameState.touchNpcInfo = null;

        if(_gtvm != null){
            _gtvm.release();
        }
        clearHint();
        System.gc();

        try{
            Thread.sleep(1000);
        }catch(InterruptedException ex){
        }
    }

    public static void releaseTeam(){
        chat_input_doing = false;
        teamMode = false;
        teamLeader = false;
        teamStatus = TEAM_STATUS_LEAVE;
        teamId = -1;
        teamLeaderId = -1;

        clearTeamMember();

        if(player != null){
            player.leaveParty = true;
        }
    }

    //  每次状态切换的时候最后调用此方法清除所有按键状态
    public static void clearKeyStates(){
        keyFlag = keyFlag2 = 0;
    }

    public static int keyToGame(int keyCode){
    	 //#if Directory == SE-K300
        if(keyCode == -6 || keyCode == -7){
            return -1;
        }
        //#elif (Directory == SE-K500) || (Directory == SE-S700)
        //# if(keyCode == -6|| keyCode == -7){
        //#     return -1;
        //# }
        //#elif JBlend == false
        //# if(keyCode == -6|| keyCode == -7){
        //#     return -1;
        //# }
        //#endif

        //#if CommandEmu == true
        //# if(keyCode == -6 || keyCode == -7){
        //#     return -1;
        //# }
        //#endif
        
        //#if M-Name == NK_E62
		//# if (keyCode == 'j') {
		//# return -1;
		//# }
		//# if(keyCode == 't'){
		//# return UP_PRESSED;
		//# }else if(keyCode == 'b'){
		//# return DOWN_PRESSED;
		//# }else if(keyCode == 'f'){
		//# return LEFT_PRESSED;
		//# }else if(keyCode == 'h'){
		//# return RIGHT_PRESSED;
		//# }
        //#endif
        if(nowBattle >= 0 && /*battleState == BATTLESTATE_SHOWSKILLLIST &&*/keyCode >= KEY_NUM0 && keyCode <= KEY_NUM9){
            return -1;
        }

        int ga = instance.getGameAction(keyCode);

        if(ga == UP){
            return UP_PRESSED;
        }else if(ga == DOWN){
            return DOWN_PRESSED;
        }else if(ga == LEFT){
            return LEFT_PRESSED;
        }else if(ga == RIGHT){
            return RIGHT_PRESSED;
        }else if(ga == FIRE){
            return FIRE_PRESSED;
        }else if(ga == GAME_A){
            return GAME_A_PRESSED;
        }else if(ga == GAME_B){
            return GAME_B_PRESSED;
        }else if(ga == GAME_C){
            return GAME_C_PRESSED;
        }else if(ga == GAME_D){
            return GAME_D_PRESSED;
        }

        return -1;
    }

    public static int keyToNum(int keyCode){
    	//#if M-Name == NK_E62
    	//#        switch(keyCode){
    	//#        case KEY_POUND:
    	//#        case 'j':
    	//#            return KEY_POUND_PRESSED;
    	//#        case KEY_STAR:
    	//#        case 'u':
    	//#            return KEY_STAR_PRESSED;
    	//#        case -6:
    	//#            return SOFT_FIRST_PRESSED;
    	//#        case -7:
    	//#            return SOFT_LAST_PRESSED;
    	//#        case 'm':
    	//#        	return KEY_NUM0_PRESSED;
    	//#        case 'r':
    	//#        	return KEY_NUM1_PRESSED;
    	//#        case 't':
    	//#        	return KEY_NUM2_PRESSED;
    	//#        case 'y':
    	//#        	return KEY_NUM3_PRESSED;
    	//#        case 'f':
    	//#        	return KEY_NUM4_PRESSED;
    	//#        case 'g':
    	//#        	return KEY_NUM5_PRESSED;
    	//#        case 'h':
    	//#        	return KEY_NUM6_PRESSED;
    	//#        case 'v':
    	//#        	return KEY_NUM7_PRESSED;
    	//#        case 'b':
    	//#        	return KEY_NUM8_PRESSED;
    	//#        case 'n':
    	//#        	return KEY_NUM9_PRESSED;
    	//#        default:
    	//#            if(keyCode >= KEY_NUM0 && keyCode <= KEY_NUM9){
    	//#                return KEY_NUM0_PRESSED + (keyCode - KEY_NUM0);
    	//#            }
    	//#            return -1;
    	//#     }

    	//#else
        switch(keyCode){
            case KEY_POUND:
                return KEY_POUND_PRESSED;
            case KEY_STAR:
                return KEY_STAR_PRESSED;
                //#if JBlend == true
                //#        case -22:
                //#        case 22:
                //#            return SOFT_FIRST_PRESSED;
                //#        case -21:
                //#        case 21:
                //#            return SOFT_LAST_PRESSED;
                //#else
            case -6:
                return SOFT_FIRST_PRESSED;
            case -7:
                return SOFT_LAST_PRESSED;
                //#endif
            default:
                if(keyCode >= KEY_NUM0 && keyCode <= KEY_NUM9){
                    return KEY_NUM0_PRESSED + (keyCode - KEY_NUM0);
                }

                return -1;
        }
        //#endif
    }

    public static int lastKey = -999;

    protected void keyPressed(int keyCode){

        try{
            int id = keyToGame(keyCode);

            if(closeAutoMove){
                autoRun = false;
            }else{
                if(gameState == null && (World.player.state == Sprite.STATE_MOVING || World.player.state == Sprite.STATE_IDLE)){
                    if(autoRun){
                        autoRun = false;

                        if(World.player.state == Sprite.STATE_MOVING){
                            World.player.setState(Sprite.STATE_IDLE);
                        }

                        lastKey = -999;
                    }else{
                        if(id >= 0 && id <= 3){
                            if(lastKey == keyCode && System.currentTimeMillis() - keyTime < 1000){
                                autoRun = true;
                            }
                        }

                        lastKey = keyCode;
                    }

                    keyTime = System.currentTimeMillis();
                }
            }

            if(id >= 0){
                keyFlag |= 3L << (id << 1);

            }

            id = keyToNum(keyCode);

            if(id >= 0){
                keyFlag |= 3L << (id << 1);
            }
        }catch(Exception ex){
        }
    }

    protected void keyReleased(int keyCode){
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
        }
    }

    public static final boolean isKeyPressed(int key, boolean clear){
        // 判断按键是否按下
        long k = 3L << (key << 1);
        boolean ret = (keyFlag2 & k) != 0;

        if(clear){
            if(ret){
                keyFlag &= ~k;
            }
        }

        return ret;
    }

    public static final boolean isKeyPressedVM(int key, boolean clear){
        if(key == 999){
            clearKeyStates();

            return false;
        }

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

    public static final boolean isAnyKeyPressed(){
        return keyFlag2 != 0;
    }

    public static int counter = 0;

    //    String msg = "";

    String t = "";

    public void paint(Graphics g){
        try{
        	//#if TouchScreen == true
            StaticUtils.beginButtonSetting();
            //#endif
            draw(g);
          //#if TouchScreen == true
           drawShortcuts(g);
           g.setClip(0, 0, viewWidth, viewHeight);
           StaticUtils.drawFocusButton(g);
           StaticUtils.endButtonSetting();
         //#endif
        }catch(Throwable ex){
            //if(msg == null){
            //   msg = "";
            //}
            //msg += ex.toString();
            //msg = ex.toString();
            //#debug
            ex.printStackTrace();
        }finally{
            //g.setFont(GameState.font);
            //g.setColor(0x000000);
            //g.fillRect(0, 100, GameState.font.stringWidth(msg), GameState.CHAR_HEIGHT);
            //g.setColor(0xffffff);
            //g.drawString("" + viewWidth + " , " + viewHeight, 0, 100, Graphics.TOP | Graphics.LEFT);
        }
    }

    public void run(){
        long currTime = System.currentTimeMillis();

        while(iTimesMIDlet.instance.isRunning){
            try{
            	//#if Revision == CMCC || (Revision == JIANGSUNCMCC)
                if(GameState.cmccPoint != null){
                    GameState.cmccPoint.cycle();
                }
                //#endif
                
                keyFlag2 = keyFlag;
                keyFlag &= 0xAAAAAAAAAAAAAAAAL;
                currTime = System.currentTimeMillis();
                needRepaint = true;

                GTVM.endTaskProcessed = false;

                cycle();

                if(GTVM.endTaskProcessed){
                    byte[] taskSave = World._gtvm.save();

                    GameState.logouting = false;
                    World.sendRequest(GameState.CONN_UPLOAD, new Object[]{
                                    new Short(World.currMapId), new Short(World.player.x), new Short(World.player.y), taskSave, new Boolean(false)
                    }, false);

                    //#debug
                    System.out.println("UPLOAD Task save");

                    try{
                        Thread.sleep(50);
                    }catch(InterruptedException e){
                    }
                }

                if(needRepaint || net_chat_showing){
                    if(gameState == null || gameState.paintBackGroud){
                        if(nowBattle < 0){
                            createYOrder();
                        }
                    }
                    if(useImageBuffer && bgImg == null && GameState.isMapLoadOk){
                        rebuildBackBuffer();
                    }
                    repaint();
                    serviceRepaints();
                }

                tick++;
                _elapsedTime = System.currentTimeMillis() - currTime;

                if(player != null){
                    if(player.runAwayTime != -1){
                        if(_elapsedTime < MILLIS_PRE_UPDATE){
                            player.runAwayTime -= MILLIS_PRE_UPDATE;
                        }else{
                            player.runAwayTime -= _elapsedTime;
                        }
                        if(player.runAwayTime <= 0)
                            player.runAwayTime = -1;
                    }
                }
            }catch(Throwable ex){
                if(useImageBuffer && bgImg == null && GameState.isMapLoadOk){
                    rebuildBackBuffer();
                }

                repaint();

                //#debug
                ex.printStackTrace();
            }finally{
                if(_elapsedTime < MILLIS_PRE_UPDATE){
                    try{
                        Thread.sleep(MILLIS_PRE_UPDATE - _elapsedTime);
                    }catch(InterruptedException ex1){
                    }
                }else{
                    try{
                        Thread.sleep(1);
                    }catch(InterruptedException ex1){
                    }
                }
            }
        }
    }

    public static void setGameState(GameState state){
        if(instance != null){
            if(!instance.isShown()){
                World.RecordPreousDisplay(instance);
            }
        }

        gameState = state;
        System.gc();

        //#if CommandEmu == true
        //# processCommand();
        //#endif
    }

    //#if CommandEmu == true
    //# public static void processCommand(){
    //#     if(World.gameState == null){
    //#         Command left = commandMenu;
    //#         Command right = null;

    //#         if(teamMode && nowBattle < 0){
    //#             if(teamLeader){
    //#             }else if(teamStatus == TEAM_STATUS_FOLLOW){
    //#                 left = null;
    //#                 right = commandLeave;
    //#             }else if(teamStatus == TEAM_STATUS_NOTFOLLOW){
    //#                 right = commandFollow;
    //#             }
    //#         }else if(nowBattle >= 0){
    //#             left = commandOK;
    //#             right = commandBack;
    //#         }

    //#         instance.addCommands(left, right);
    //#     }else{
    //#         instance.addCommands(commandOK, commandBack);
    //#     }
    //# }
    //#endif

    public void cycle(){
        try{
            if(GameState.connection != null){
                GameState.connection.cycleSegmentsDoingQueue();
            }

            touchingNpcId = -1;

            //#if CacheImage == TRUE
            orderCache();
            //#endif
            GameState.cycleSegments();
            processHint();

            if(player != null && player.bagSize != 0 && player.getItemTotalCount() == player.bagSize){
                if(hintStatus[HINT_BAGFULL] == HINT_HIDE){
                    showHint(HINT_BAGFULL);
                }
            }else{
                hideHint(HINT_BAGFULL);
            }

            if(gameState != null){
                handleEvents(true);

                if(gameState != null){
                    gameState.cycle();
                }

                needRepaint = GameState.repaintNextTime;

                return;
            }

            if(chat_input_doing){
                return;
            }

            if(nowBattle >= 0){
                cycleBattle();
            }else{

                byte block = handleEvents(false);

                if(block == GameEvent.EVENT_BLOCK_CYCLE || block == GameEvent.EVENT_BLOCK_DOWNLOAD){
                    return;
                }

                if(block != GameEvent.EVENT_BLOCK_PLAYER)
                    handleKey();

                if(!teamMode || teamLeader || teamStatus == World.TEAM_STATUS_NOTFOLLOW){
                    _gtvm.step(false);
                }else{
                    _gtvm.step(true);
                }

                player.cycle(_elapsedTime, this);

                if(npcs != null){
                    for(int i = 0; i < npcs.length; i++){
                        npcs[i].cycle(_elapsedTime, this);
                    }
                }

                if(monsters != null){
                    for(int i = 0; i < monsters.length; i++){
                        monsters[i].cycle(_elapsedTime, this);
                    }
                }

                Enumeration emu = netPlayers.elements();

                while(emu.hasMoreElements()){
                    MonsterSprite netplayr = (MonsterSprite)emu.nextElement();

                    if(netplayr.teamRole == MonsterSprite.TEAM_ROLE_NONE){
                        netplayr.cycle(_elapsedTime, this);
                    }
                }

                if(teamMode){
                    updateTeamMemberLocation();
                }

                for(int i = 0; i < teamMembers.size(); i++){
                    MonsterSprite teamMember = (MonsterSprite)teamMembers.elementAt(i);

                    teamMember.cycle(_elapsedTime, this);
                }

                moveMap();

                if(!teamMode || teamLeader || teamStatus == World.TEAM_STATUS_NOTFOLLOW){
                    processDoor();
                    checkNPC();
                    checkResource();
                }
            }

            //测试网速
            long sc = System.currentTimeMillis();
            if(sc - requestTestNetSpeedTime > NETSPEED_TEST_INTERVAL){
                requestTestNetSpeedTime = sc;
                requestTestNetSpeed();
            }
        }finally{
            processMonsterRefresh();
            processPosition();
            cycleNetChatMessage();
        }
    }

    public static void processPosition(){
        if(!GameState.isMapLoadOk){
            return;
        }

        boolean flag = false;
        sendPositionTime += _elapsedTime < MILLIS_PRE_UPDATE? MILLIS_PRE_UPDATE: _elapsedTime;

        if(sendPositionTime > SENDPOSITIONTIME_LIMIT){
            flag = true;
        }else{
            int distance = Sprite.STEP * 10;

            if(Math.abs(player.lastPositionX - player.x) > distance || Math.abs(player.lastPositionY - player.y) > distance || player.lastMapId != currMapId){
                flag = true;
            }
        }

        if(flag){
            requestSendPosition();
        }
    }

    public static void processTouchNpcOvertime(){
        if(GameState.touchNpcInfo != null){
            GameState.touchNpcInfo[2] -= _elapsedTime < MILLIS_PRE_UPDATE? MILLIS_PRE_UPDATE: _elapsedTime;
            if(GameState.touchNpcInfo[2] < 0){
                //超时，清除npcInfo
                GameState.touchNpcInfo = null;
            }

        }
    }

    public static void processMonsterRefresh(){
        if(monsterRefreshPool == null){
            return;
        }

        Enumeration emu = monsterRefreshPool.keys();

        while(emu.hasMoreElements()){
            Integer id = (Integer)emu.nextElement();
            int rt = ((Integer)monsterRefreshPool.get(id)).intValue();
            if(rt > 0){
                rt -= _elapsedTime < MILLIS_PRE_UPDATE? MILLIS_PRE_UPDATE: _elapsedTime;
                monsterRefreshPool.put(id, new Integer(rt));
            }
        }

        for(int i = 0; monsters != null && i < monsters.length; i++){
            MonsterSprite mon = monsters[i];
            if(mon == null)
                continue;
            Integer rtime = (Integer)monsterRefreshPool.get(new Integer(mon.id));
            if(rtime != null){
                int rt = rtime.intValue();
                if(rt <= 0){
                    //refresh
                    mon.visible = true;
                    mon.wpPointer = 1;
                    mon.wpDir = 1;
                    //mon.refreshTimer = 0;
                    if(mon.wpList != null){
                        if(rt != -2){
                            int dest = ((Integer)(mon.wpList.elementAt(0))).intValue();

                            mon.rx = dest >> 16;
                            mon.ry = dest & 0x0000ffff;
                        }
                        if(World.tileWidth != 0){
                            mon.x = (byte)(mon.rx / World.tileWidth);
                            mon.y = (byte)(mon.ry / World.tileHeight);
                        }
                    }
                    monsterRefreshPool.remove(new Integer(mon.id));

                    //#debug
                    log("monster[" + mon.id + "] refreshed", true);
                }
            }
        }
    }

    public static void getFormInput(String title, byte type, boolean canEmpty){
        GameEvent e = new GameEvent(GameEvent.EVENT_FORM_INPUT, 1, 1);

        e.idata[0] = 0;
        e.sdata[0] = title;

        addEvent(e);
    }

    public static int requestUseItem(GameItem item, byte useType, int count){
        int result = -1;
        try{
            UWAPSegment segment = item.getUseSegment(useType, count);
            segment.flush();
            GameState.sendRequest(segment);
            result = segment.serial;
        }catch(IOException e){
            //#debug
            e.printStackTrace();
        }
        return result;
    }

    public static int sendRequest(byte type, Object[] param, boolean asyncronized){
        return sendRequest(type, param, false, 0);
    }

    public static int sendRequest(byte type, Object[] param, boolean asyncronized, long asyncSign){
        UWAPSegment segment = new UWAPSegment(type);

        try{
            for(int i = 0; i < param.length; i++){
                Object obj = param[i];
                if(obj instanceof Long){
                    segment.writeLong(((Long)obj).longValue());

                }else if(obj instanceof long[]){
                    segment.writeLongs((long[])obj);

                }else if(obj instanceof Integer){
                    segment.writeInt(((Integer)obj).intValue());

                }else if(obj instanceof int[]){
                    segment.writeInts((int[])obj);

                }else if(obj instanceof Short){
                    segment.writeShort(((Short)obj).shortValue());

                }else if(obj instanceof short[]){
                    segment.writeShorts((short[])obj);

                }else if(obj instanceof Byte){
                    segment.writeByte(((Byte)obj).byteValue());

                }else if(obj instanceof byte[]){
                    segment.writeBytes((byte[])obj);

                }else if(obj instanceof Boolean){
                    segment.writeBoolean(((Boolean)obj).booleanValue());

                }else if(obj instanceof boolean[]){
                    segment.writeBooleans((boolean[])obj);

                }else if(obj instanceof Character){
                    segment.writeChar(((Character)obj).charValue());

                }else if(obj instanceof char[]){
                    segment.writeChars((char[])obj);

                }else if(obj instanceof String){
                    segment.writeString((String)obj);

                }else if(obj instanceof String[]){
                    segment.writeStrings((String[])obj);
                }
            }

            segment.flush();

            if(asyncronized){
                segment.asyncSign = asyncSign;
                ASyncRequestThread.sendUWAPSegment(segment);
            }else{
                GameState.sendRequest(segment);
                return segment.serial;
            }
        }catch(IOException e){
            //#debug
            e.printStackTrace();
        }

        return -1;
    }

    public static void saveChatOption(){
        byte[] tmp = new byte[16];

        for(int i = 0; i < net_chat_priority_option.length; i++){
            tmp[i * 2] = (byte)net_chat_priority_option[i];
            tmp[i * 2 + 1] = (byte)net_chat_color_option[i];
            //#debug
            System.out.println(tmp[i * 2] + " , " + tmp[i * 2 + 1]);
        }

        try{
            UWAPSegment segment = new UWAPSegment(GameState.CONN_CHAT_OPTION);

            segment.writeBytes(tmp);
            segment.flush();

            GameState.sendRequest(segment);
        }catch(IOException e){
        }
    }

    public void cycleNetChatMessage(){

        if(!GameState.isMapLoadOk){
            return;
        }

        if(net_chat_current_message == null){
            GameEvent msg = null;

            msg = getNetChatMessage(net_chat_high_priority_message);

            if(msg == null){
                msg = getNetChatMessage(net_chat_low_priority_message);

                if(msg == null){
                    if(net_chat_showing){
                        needRepaint = true;
                    }

                    net_chat_showing = false;

                    return;
                }
            }

            net_chat_current_message = splitString(msg.sdata[0] + "：" + msg.sdata[1], viewWidth - 20, GameState.font);
            net_chat_current_color = ImageSet.COLOR_TABLE[msg.idata[3]];
            net_chat_current_offset = -TALKBOX_HEIGHT + GameState.font.getHeight() / 2;
            net_chat_scroll_flag = 0;
            net_chat_showing = true;
        }

        net_chat_scroll_flag++;

        if(net_chat_scroll_flag > NET_CHAT_SCROLL_SPEED){
            net_chat_scroll_flag = 0;
            net_chat_current_offset++;
        }

        if(net_chat_current_offset > net_chat_current_message.length * GameState.font.getHeight()){
            net_chat_current_message = null;
        }
    }

    public static void addNetChatMessage(Vector queue, GameEvent msg){
        if(queue.size() > NET_CHAT_QUEUE_SIZE){
            queue.removeElementAt(0);
        }

        queue.addElement(msg);
    }

    public static void addFriend(Integer id, short degree, String name){
        netFriends.put(id, new String[]{
                        name, "离线", String.valueOf(degree)
        });
    }

    public static void addBlackList(Integer id, String name){
        blackList.put(id, name);
    }

    public static boolean inBlackList(int id){
        String name = (String)blackList.get(new Integer(id));
        if(name != null)
            return true;
        return false;
    }

    public static void deleteBlackList(Integer id){
        blackList.remove(id);
    }

    public static void changeFriendStatus(int id, short degree, boolean online, boolean hasDegree){
        Integer tmpId = new Integer(id);

        String[] friendData = (String[])netFriends.get(tmpId);

        if(friendData != null){
            if(online){
                friendData[1] = "在线";
            }else{
                friendData[1] = "离线";
            }

            if(hasDegree){
                friendData[2] = String.valueOf(degree);
            }
        }
    }

    public static String getFriendsStatus(int id){
        String result = null;
        Integer tmpId = new Integer(id);

        String[] friendData = (String[])netFriends.get(tmpId);

        if(friendData != null){
            if(friendData[1].equals("在线")){
                result = friendData[0] + "上线了";
            }else{
                result = friendData[0] + "下线了";
            }
        }

        return result;
    }

    public static int getFriendDegree(int id){
        int degree = 1;
        Integer tmpId = new Integer(id);

        String[] friendData = (String[])netFriends.get(tmpId);

        if(friendData != null){
            degree = Integer.parseInt(friendData[2]);
        }
        return degree;
    }

    public static void deleteFriend(Integer id){
        netFriends.remove(id);
    }

    public static Object getItemFromHashTable(Hashtable inData, int index, boolean getKey){
        Object result = null;

        if(index < inData.size()){
            Enumeration emu = null;

            if(getKey){
                emu = inData.keys();
            }else{
                emu = inData.elements();
            }

            int tmp = 0;

            while(emu.hasMoreElements()){
                Object tmpObject = emu.nextElement();

                if(tmp == index){
                    result = tmpObject;

                    break;
                }

                tmp++;
            }
        }

        return result;
    }

    public static void showHint(byte hintType){
        hintStatus[hintType] = HINT_SHOWING_DRAW;
        hintShowTime[hintType] = HINT_DELAY;
        hintFlashTime[hintType] = HINT_DELAY;
        hintDrawOffset[hintType] = (short)hintImage[hintType].getWidth();
    }

    public static void hideHint(byte hintType){
        hintStatus[hintType] = HINT_HIDE;
        hintShowTime[hintType] = 0;
        hintFlashTime[hintType] = 0;
        hintDrawOffset[hintType] = 0;
    }

    public static void processHint(){
        for(int i = 0; i < hintStatus.length; i++){
            if(hintStatus[i] != HINT_HIDE){
                hintShowTime[i] -= _elapsedTime > MILLIS_PRE_UPDATE? _elapsedTime: MILLIS_PRE_UPDATE;

                if(hintShowTime[i] <= 0){
                    hideHint((byte)i);

                    continue;
                }

                if(hintFlashTime[i] - hintShowTime[i] >= HINT_FLASH_DELAY){
                    if(hintStatus[i] == HINT_SHOWING_DRAW){
                        hintStatus[i] = HINT_SHOWING_NO_DRAW;
                    }else{
                        hintStatus[i] = HINT_SHOWING_DRAW;
                    }

                    hintFlashTime[i] = hintShowTime[i];
                }
            }
        }

        if(npcHintStamp == 0){
            npcNeedHint = false;
        }

        npcHintStamp++;

        if(npcHintStamp > NPC_HINT_FREQ){
            npcHintStamp = 0;
            npcNeedHint = true;

            if(npcs == null){
                return;
            }

            for(int i = 0; i < npcs.length; i++){
                MonsterSprite npc = npcs[i];
                npc.clearHint();
            }
        }
    }

    //    private static final int BAG_TOP = 15;
    //    private static final int BAG_LEFT = 88;

    public static void drawHint(Graphics g){
        int offset = 0;

        for(int i = 0; i < hintStatus.length; i++){
            if(hintStatus[i] != HINT_HIDE){
                offset += hintDrawOffset[i] + 2;

                if(hintStatus[i] == HINT_SHOWING_DRAW){
                    g.drawImage(hintImage[i], viewWidth - offset, 0, Graphics.TOP | Graphics.LEFT);
                }
            }
        }

    }

    public static void clearHint(){
        for(int i = 0; i < hintStatus.length; i++){
            hideHint((byte)i);
        }
    }

    public static void clearNetChatMessage(){
        net_chat_high_priority_message.removeAllElements();
        net_chat_low_priority_message.removeAllElements();
        net_chat_current_message = null;
        net_chat_showing = false;
    }

    public static GameEvent getNetChatMessage(Vector queue){
        GameEvent msg = null;

        for(int i = 0; i < queue.size(); i++){
            msg = (GameEvent)queue.elementAt(i);

            if(msg.idata[4] == 0){
                msg.idata[4] = 1;

                break;
            }else{
                msg = null;
            }
        }

        return msg;
    }

    public void drawNetChatMessage(Graphics g){
        
    	//#if TouchScreen == true
        int y = getHeight() - TALKBOX_HEIGHT-ok.getHeight();
        //#else
        //# int y = getHeight() - TALKBOX_HEIGHT;
        //#endif

        //#if (AlphaMethod == alpha) || (AlphaMethod == image) ||(AlphaMethod == drawRGB)
        GameState.fileAlphaBox(g, 0xA0000000, 0, y, viewWidth, TALKBOX_HEIGHT);
        //#else
        //# g.setColor(0xA0000000);
        //# for(int i = 0; i < viewWidth; i++){
        //# for(int j = 0; j < TALKBOX_HEIGHT; j++){
        //#     if((i % 2 == 0 && j % 2 == 0) || (i % 2 != 0 && j % 2 != 0)){
        //#         g.drawLine(i, y + j, i, y + j);
        //#     }
        //# }
        //# }
        //#endif

        if(net_chat_current_message != null){
            int cx, cy, cw, ch;
            int minY, maxY;

            cx = g.getClipX();
            cy = g.getClipY();
            cw = g.getClipWidth();
            ch = g.getClipHeight();

            g.setClip(0, y + 2, viewWidth, TALKBOX_HEIGHT - 4);
            g.setFont(GameState.font);
            minY = y - GameState.font.getHeight();
            maxY = getHeight();

            g.setFont(GameState.font);
            for(int i = 0, size = net_chat_current_message.length; i < size; i++){
                int ty = y - net_chat_current_offset + i * GameState.font.getHeight();

                if(ty > maxY || ty < minY){
                    continue;
                }
                g.setColor(net_chat_current_color);
                g.drawString(net_chat_current_message[i], 9, ty, Graphics.TOP | Graphics.LEFT);
            }

            g.setClip(cx, cy, cw, ch);
        }
    }

    private void handleKey(){
    	//#if TouchScreen == true
    	if (StaticUtils.getPressedButton() == 4000) {
    		shortcutsState = 1 - shortcutsState;
    	}
    	//#endif
        if(teamMode && GameState.isMapLoadOk){
            if(isKeyPressed(SOFT_LAST_PRESSED, true) && !World.player.leaveParty){
                if(teamLeader){
                }else if(teamStatus == TEAM_STATUS_FOLLOW){
                    sendRequest(GameState.CONN_TEAM_LEAVE, new Object[]{
                                    new Integer(World.teamId), new Integer(0), new Byte(World.TEAM_STATUS_NOTFOLLOW)
                    }, false);
                }else if(teamStatus == TEAM_STATUS_NOTFOLLOW){
                    sendRequest(GameState.CONN_TEAM_LEAVE, new Object[]{
                                    new Integer(World.teamId), new Integer(0), new Byte(World.TEAM_STATUS_FOLLOW)
                    }, false);
                }
            }
        }

        if(!teamMode || teamLeader || teamStatus == World.TEAM_STATUS_NOTFOLLOW){
            for(int i = 0; i < HOTKEYS.length; i++){
                if(World.isKeyPressed(World.KEY_NUM0_PRESSED + i, true)){
                    String command = HOTKEYS[i];
                    if(!command.equals("")){
                        GameState state = new GameState(GameState.STATE_MAINMENU);
                        state.handleMainMenuCommand(command);
                    }
                }
            }

            if(isKeyPressed(FIRE_PRESSED, true)){
                if(MonsterSprite.dimID > 0){
                    sendRequest(GameState.CONN_COMMAND, new Object[]{
                        "touch " + String.valueOf(MonsterSprite.dimID)
                    }, false);
                }
            }

            if(isKeyPressed(SOFT_FIRST_PRESSED, true)){
                GameState gameState = new GameState(GameState.STATE_MAINMENU);
                setGameState(gameState);
            }

            //#mdebug
            if(isKeyPressed(SOFT_LAST_PRESSED, true)){
                if(monsterRefreshPool != null){
                    Enumeration emu = monsterRefreshPool.keys();
                    while(emu.hasMoreElements()){
                        Integer id = (Integer)emu.nextElement();
                        int rt = ((Integer)monsterRefreshPool.get(id)).intValue();
                        rt = -1;
                        monsterRefreshPool.put(id, new Integer(rt));
                    }
                }
            }

            //#enddebug
            if(player != null)
                player.handleKey();
        }else{
            for(int i = 0; i < TEAMMODE_HOTKEYS.length; i++){
                if(World.isKeyPressed(World.KEY_NUM0_PRESSED + i, true)){
                    String command = TEAMMODE_HOTKEYS[i];
                    if(!command.equals("")){
                        GameState state = new GameState(GameState.STATE_MAINMENU);
                        state.handleMainMenuCommand(command);
                    }
                }
            }
        }
    }

    public void leaveTeam(){
        String msg = "您已经离开队伍";

        if(teamLeader){
            msg += "，您的队伍已解散";
        }else{
            player.leaveParty = true;
        }

        sendRequest(GameState.CONN_TEAM_LEAVE, new Object[]{
                        new Integer(World.teamId), new Integer(0), new Byte(World.TEAM_STATUS_LEAVE)
        }, false);

        releaseTeam();
        teamStatus = TEAM_STATUS_LEAVE;

        showMessage(msg, (byte)0);
    }

    public static byte handleEvents(boolean gameStateDoing){
        if(events.size() == 0 || nowBattle > -1){
            return GameEvent.EVENT_BLOCK_NONE;
        }

        GameEvent e = (GameEvent)events.elementAt(0);

        if(gameStateDoing && !e.isTaskUIEvent){
            return GameEvent.EVENT_BLOCK_NONE;
        }

        if(e.getBlockType() == GameEvent.EVENT_BLOCK_NONE){
            int[] eBox = e.getCollisionBox();

            if(eBox != null){
                int[] pBox = player.getCollisionBox();

                if(!rectIntersect(eBox[0], eBox[1], eBox[2], eBox[3], pBox[0], pBox[1], pBox[2], pBox[3])){
                    removeEvent(e);

                    return GameEvent.EVENT_BLOCK_NONE;
                }
            }
        }

        switch(e.getType()){
            case GameEvent.EVENT_GOTO_MAP_LOCAL:
                if(e.idata[2] == 5){
                    viewMaxX = -1;
                    viewMaxY = -1;
                    bgImg = null;
                    player.x = (short)(e.idata[0] * tileWidth);
                    player.y = (short)(e.idata[1] * tileHeight);
                    moveMap();
                }
                e.idata[2]--;
                if(e.idata[2] < 0){
                    removeEvent(e);
                }
                break;
            case GameEvent.EVENT_GOTO_MAP: {
                GameState state = new GameState(GameState.STATE_LOADING);
                state.oldMapId = currMapId;
                state.loadMapId = (short)e.idata[0];

                if(state.oldMapId == state.loadMapId){
                    GameEvent event = new GameEvent(GameEvent.EVENT_GOTO_MAP_LOCAL, 3, 0);
                    event.idata[0] = e.idata[1];
                    event.idata[1] = e.idata[2];
                    event.idata[2] = 10;
                    addEvent(event);
                }else{
                    if(e.idata[1] < 0 && e.idata[2] < 0){
                        state.startX = -1;
                        state.startY = -1;
                    }else{
                        state.startX = (short)(e.idata[1]);
                        state.startY = (short)(e.idata[2]);
                        state.isMapXY = e.idata[3] == 1;
                    }

                    byte[] taskSave = _gtvm.save();

                    GameState.logouting = false;

                    World.sendRequest(GameState.CONN_UPLOAD, new Object[]{
                                    new Short(World.currMapId), new Short(World.player.x), new Short(World.player.y), taskSave, new Boolean(false)
                    }, false);
                    try{
                        Thread.sleep(50);
                    }catch(InterruptedException ex){
                    }

                    resetNetPlayer();

                    GameState.isMapLoadOk = false;

                    setGameState(state);
                    World.RecordPreousDisplay(World.instance);
                }

                removeEvent(e);
            }

                break;
            case GameEvent.EVENT_CHAT:
                if(World.isKeyPressed(World.FIRE_PRESSED, true) || World.isKeyPressed(World.SOFT_FIRST_PRESSED, true)){
                    int x = e.idata[0] + e.idata[2];

                    e.idata[2] = getFormatedStringLine((Object[])e.odata[1], x, 3);

                    if(x >= ((Object[])e.odata[1]).length/*e.sdata.length*/){
                        removeEvent(e);
                        clearKeyStates();
                    }else{
                        e.idata[0] = x;
                    }
                }

                break;
            case GameEvent.EVENT_MESSAGE:
            {
                if (World.isKeyPressed(World.SOFT_FIRST_PRESSED, true) || World.isKeyPressed(World.FIRE_PRESSED, true)) {
                    Object[] data = e.odata;
                    int lineCount = 0;
                    if (data.length > 0) {
                        lineCount = ((Integer)((Object[])data[data.length - 1])[0]).intValue() + 1;
                    }
                    if (e.idata[1] + GameState.BBS_PAGE_COUNT >= lineCount) {
                        removeEvent(e);
                    } else {
                        e.idata[1] += GameState.BBS_PAGE_COUNT;
                    }
                    clearKeyStates();
                } else if (e.idata[0] < 0) {
                    removeEvent(e);
                    clearKeyStates();
                }else{
                    e.idata[0] -= _elapsedTime > MILLIS_PRE_UPDATE? _elapsedTime: MILLIS_PRE_UPDATE;
                }
                break;
            }
            case GameEvent.EVENT_QUESTION:
                if(World.isKeyPressed(World.KEY_NUM1_PRESSED, true)){
                    if(e.idata[0] >= 1){
                        lastAnswer = 1;
                        removeEvent(e);
                    }
                }else if(World.isKeyPressed(World.KEY_NUM2_PRESSED, true)){
                    if(e.idata[0] >= 2){
                        lastAnswer = 2;
                        removeEvent(e);
                    }
                }else if(World.isKeyPressed(World.KEY_NUM3_PRESSED, true)){
                    if(e.idata[0] >= 3){
                        lastAnswer = 3;
                        removeEvent(e);
                    }
                }else if(World.isKeyPressed(World.KEY_NUM4_PRESSED, true)){
                    if(e.idata[0] >= 4){
                        lastAnswer = 4;
                        removeEvent(e);
                    }
                }else if(World.isKeyPressed(World.KEY_NUM5_PRESSED, true)){
                    if(e.idata[0] >= 5){
                        lastAnswer = 5;
                        removeEvent(e);
                    }
                }else if(World.isKeyPressed(World.KEY_NUM6_PRESSED, true)){
                    if(e.idata[0] >= 6){
                        lastAnswer = 6;
                        removeEvent(e);
                    }
                }else if(World.isKeyPressed(World.KEY_NUM7_PRESSED, true)){
                    if(e.idata[0] >= 7){
                        lastAnswer = 7;
                        removeEvent(e);
                    }
                }else if(World.isKeyPressed(World.KEY_NUM8_PRESSED, true)){
                    if(e.idata[0] >= 8){
                        lastAnswer = 8;
                        removeEvent(e);
                    }
                }else if(World.isKeyPressed(World.KEY_NUM9_PRESSED, true)){
                    if(e.idata[0] >= 9){
                        lastAnswer = 9;
                        removeEvent(e);
                    }
                }else if(World.isKeyPressed(World.KEY_NUM0_PRESSED, true)){
                    if(e.idata[0] >= 10){
                        lastAnswer = 0;
                        removeEvent(e);
                    }
                }else if(World.isKeyPressed(World.SOFT_FIRST_PRESSED, true)){
                    Object[] data = e.odata;
                    int lineCount = 0;
                    if (data.length > 0) {
                        lineCount = ((Integer)((Object[])data[data.length - 1])[0]).intValue() + 1;
                    }
                    if (e.idata[1] + GameState.BBS_PAGE_COUNT >= lineCount) {
                        e.idata[1] = 0;
                    } else {
                        e.idata[1] += GameState.BBS_PAGE_COUNT;
                    }
                }

                break;
            case GameEvent.EVENT_SHOWGETITEM:
                if(World.isKeyPressed(World.FIRE_PRESSED, true) || World.isKeyPressed(World.SOFT_FIRST_PRESSED, true) || e.idata[0] < 0){
                    removeEvent(e);
                    clearKeyStates();
                }else{
                    e.idata[0] -= _elapsedTime > MILLIS_PRE_UPDATE? _elapsedTime: MILLIS_PRE_UPDATE;
                }
                break;
            case GameEvent.EVENT_BATTLE: {
                battleMonsterIndex = e.idata[0];
                GameState state = new GameState(GameState.STATE_LOADING);

                if(monsters[e.idata[0]].serverRefresh || ((teamMode && teamStatus != World.TEAM_STATUS_NOTFOLLOW) && teamLeader))
                    state.subState = GameState.SS_LOADING_SERVERBATTLE;
                else
                    state.subState = GameState.SS_LOADING_BATTLE;
                state.param = e.idata[0];
                setGameState(state);
                removeEvent(e);
            }

                break;
            case GameEvent.EVENT_DIE_CONFIRM: {
                e.idata[0] -= _elapsedTime > MILLIS_PRE_UPDATE? _elapsedTime: MILLIS_PRE_UPDATE;
                if(e.idata[0] < 0 && (teamLeader || !teamMode || teamStatus != TEAM_STATUS_FOLLOW)){
                    removeEvent(e);
                    gotoMap((short)e.idata[1], (short)e.idata[2], (short)e.idata[3], true);
                }else if(e.idata[4] >= 2){
                    instance.leaveTeam();
                }else if(e.idata[0] < -4000){
                    //requestChangeTeamStatus(TEAM_STATUS_NOTFOLLOW);

                    sendRequest(GameState.CONN_TEAM_LEAVE, new Object[]{
                                    new Integer(World.teamId), new Integer(0), new Byte(World.TEAM_STATUS_NOTFOLLOW)
                    }, false);

                    e.idata[0] = -1;
                    e.idata[4]++;
                }

                break;
            }
            case GameEvent.EVENT_NPC_CONFIRM:
                if(World.isKeyPressed(World.FIRE_PRESSED, true) || World.isKeyPressed(World.SOFT_FIRST_PRESSED, true)){
                    removeEvent(e);
                    clearKeyStates();

                    touchingNpcId = e.idata[1];

                    MonsterSprite npc = getUnit(touchingNpcId);

                    if(npc.serverRefresh){
                        if(npc.politics == NPC_TYPE_BBS){
                            createTaskUIEvent(GameEvent.EVENT_BBS, e.idata[1], "");
                        }else{
                            GameState.touchNpc(e.idata[1]);
                        }
                    }
                }else if(isKeyPressed(SOFT_LAST_PRESSED, true)){
                    removeEvent(e);
                    clearKeyStates();
                }

                break;
            case GameEvent.EVENT_TASK_CONFIRM:
                if(World.isKeyPressed(World.SOFT_FIRST_PRESSED, true)){
                    addTaskAsUnFinish(e.idata[0], null, true);
                    World.requestDownloadTask((short)e.idata[0]);
                    removeEvent(e);
                    clearKeyStates();

                }else if(World.isKeyPressed(World.SOFT_LAST_PRESSED, true)){
                    removeEvent(e);
                    clearKeyStates();
                }
                break;
            case GameEvent.EVENT_RESOURCE_CONFIRM:
                if(World.isKeyPressed(World.FIRE_PRESSED, true) || World.isKeyPressed(World.SOFT_FIRST_PRESSED, true)){
                    removeEvent(e);
                    clearKeyStates();

                    if(e.idata[3] == 0){
                        break;
                    }

                    GameEvent tmpEvent = new GameEvent(GameEvent.EVENT_RESOURCE_GATHER, 2, 0);
                    tmpEvent.idata[0] = 0;
                    tmpEvent.idata[1] = 0;

                    tmpEvent.sdata = new String[]{
                                    "正在采集......", ""
                    };

                    int[] eBox = e.getCollisionBox();
                    tmpEvent.setCollisionBox(eBox[0], eBox[1], eBox[2], eBox[3]);

                    addEvent(tmpEvent);

                    resourceIndex = e.idata[0];
                    resourceGame = e.idata[1] == 1? true: false;
                    tmpEvent.serial = sendRequest(GameState.CONN_GATHER, new Object[]{
                        new Integer(e.idata[0])
                    }, false);

                    //requestLockResource(e.idata[0]);
                    resourceLockOk = false;
                }else if(World.isKeyPressed(SOFT_LAST_PRESSED, true)){
                    removeEvent(e);
                    clearKeyStates();
                }

                break;
            case GameEvent.EVENT_RESOURCE_GATHER:
                if(resourceLockOk){
                    if(resourceGame){
                        removeEvent(e);
                        e.setBlockType(GameEvent.EVENT_BLOCK_CYCLE);
                        clearKeyStates();

                        requestDownloadTask(TASK_ID_RESOURCE_GAME);

                        GameEvent tmpEvent = new GameEvent(GameEvent.EVENT_RESOURCE_GAME, 1, 0);
                        tmpEvent.idata[0] = 0;

                        addEvent(tmpEvent);
                    }else{
                        e.idata[0] += _elapsedTime > MILLIS_PRE_UPDATE? _elapsedTime: MILLIS_PRE_UPDATE;

                        int percent = e.idata[0] * 100 / 5000;

                        if(percent > 100){
                            percent = 100;
                        }

                        e.sdata[1] = "" + percent + "%";

                        if(e.idata[0] > 5000){

                            World.sendRequest(GameState.CONN_GATHER_RESULT, new Object[]{
                                            new Integer(World.resourceIndex), new Integer(500)
                            }, false);

                            //World.requestGatherResource(World.resourceIndex, 500);
                            for(int j = 0; j < mapResource.length; j++){
                                if(mapResource[j][0] == resourceIndex){
                                    mapResource[j][6] = 0;
                                }
                            }
                            removeEvent(e);
                            clearKeyStates();
                        }
                    }
                }else{
                    if(e.idata[1] == 1){
                        //资源已被采集
                        if(isAnyKeyPressed()){
                            removeEvent(e);
                            clearKeyStates();
                        }
                    }else{
                        return GameEvent.EVENT_BLOCK_NONE;
                    }
                }

                break;
            case GameEvent.EVENT_RESOURCE_GAME:
                if(_gtvm.hasTask(TASK_ID_RESOURCE_GAME)){
                    e.setBlockType(GameEvent.EVENT_BLOCK_CYCLE);
                    if(e.idata[0] == 0){
                        GameState.taskUIGameRequest = true;
                        e.idata[0] = 1;
                    }

                    GameState.repaintNextTime = false;
                    _gtvm.step(false);

                    if(!GameState.taskUIGameRequest){

                        World.sendRequest(GameState.CONN_GATHER_RESULT, new Object[]{
                                        new Integer(resourceIndex), new Integer(GameState.taskUIGameScore)
                        }, false);

                        resourceGame = false;
                        removeEvent(e);
                    }
                }else{
                    e.setBlockType(GameEvent.EVENT_BLOCK_DOWNLOAD);
                    return GameEvent.EVENT_BLOCK_DOWNLOAD;
                }

                break;
            case GameEvent.EVENT_CHANGEATTRIBUTE_CONFIRM:
                if(e.odata != null){
                    if(isAnyKeyPressed()){
                        removeEvent(e);
                    }
                    GameState.clearChangeProUI();
                    break;
                }

                if(GameState.pro_data_ok){
                    if(GameState.pro_data_error){
                        e.odata = new Object[]{
                            GameState.taskUIErrorMessage
                        };

                        player.restoreBaseAttrBackup();

                        player.reCalculateAttributes();
                        player.clearAttributesBackup();

                    }else{
                        player.clearAttributesBackup();
                        removeEvent(e);
                        GameState.clearChangeProUI();
                        World.showMessage("修改已成功", (byte)3);
                    }
                }

                break;
            case GameEvent.EVENT_CHANGEEQUIP_CONFIRM:
                if(e.odata != null){
                    if(isAnyKeyPressed()){
                        removeEvent(e);
                    }
                    GameState.clearChangeEquipUI();
                    break;
                }

                if(GameState.equip_data_ok){
                    if(GameState.equip_data_error){
                        e.odata = new Object[]{
                            GameState.taskUIErrorMessage
                        };

                        player.restoreEquipsBackup();

                        player.reCalculateAttributes();
                        player.clearAttributesBackup();

                    }else{
                        player.clearAttributesBackup();
                        removeEvent(e);
                        GameState.clearChangeEquipUI();
                        World.showMessage("换装已成功", (byte)3);
                    }
                }

                break;
            case GameEvent.EVENT_PK_REQUEST:
                if(_elapsedTime < MILLIS_PRE_UPDATE){
                    e.idata[5] -= MILLIS_PRE_UPDATE;
                }else{
                    e.idata[5] -= _elapsedTime;
                }

                if(World.isKeyPressed(World.KEY_NUM7_PRESSED, true)){
                    sendRequest(GameState.CONN_PK_OK, new Object[]{
                                    new Integer(e.idata[4]), new Short(player.level)
                    }, false);

                    World.player_PKID = e.idata[4];
                    removeEvent(e);
                    clearKeyStates();
                }else if(World.isKeyPressed(World.KEY_NUM9_PRESSED, true) || e.idata[5] < 0){
                    removeEvent(e);
                    clearKeyStates();

                    sendRequest(GameState.CONN_PK_REFUSE, new Object[]{
                                    new Byte((byte)0), "我正忙", new Integer(e.idata[4])
                    }, false);
                }

                break;
            case GameEvent.EVENT_TEAM_INVITE:
                if(_elapsedTime < MILLIS_PRE_UPDATE){
                    e.idata[0] -= MILLIS_PRE_UPDATE;
                }else{
                    e.idata[0] -= _elapsedTime;
                }

                if(e.idata[0] < 0){

                    World.sendRequest(GameState.CONN_TEAM_INVIT_RESULT, new Object[]{
                                    new Integer(World.teamId), new Byte(World.TEAM_INVITE_REFUSE), "无响应"
                    }, false);

                    removeEvent(e);
                    clearKeyStates();
                    break;
                }

                if(World.isKeyPressed(World.KEY_NUM1_PRESSED, true)){

                    World.sendRequest(GameState.CONN_TEAM_INVIT_RESULT, new Object[]{
                                    new Integer(World.teamId), new Byte(World.TEAM_INVITE_ACCEPT), ""
                    }, false);

                    requestSendPosition();
                    removeEvent(e);
                    clearKeyStates();
                }else if(World.isKeyPressed(World.KEY_NUM2_PRESSED, true)){
                    World.sendRequest(GameState.CONN_TEAM_INVIT_RESULT, new Object[]{
                                    new Integer(World.teamId), new Byte(World.TEAM_INVITE_REFUSE), ""
                    }, false);

                    removeEvent(e);
                    clearKeyStates();
                }

                break;
            case GameEvent.EVENT_FORM_INPUT: {
                if(e.idata[0] == 0){
                    e.idata[0] = 1;
                    formInputOver = false;

                    GameState state = new GameState(GameState.STATE_INPUT_FORM);

                    GameState._formTitle = e.sdata[0];
                    state._form = new Form(GameState._formTitle);

                    state._form.append(new TextField("请输入:", null, 200, TextField.ANY));

                    state._form.addCommand(new Command("完成", Command.ITEM, 0));
                    state._form.addCommand(new Command("返回", Command.BACK, 0));
                    state._form.setCommandListener(state);

                    lastInput = "";
                    World.RecordPreousDisplay(state._form);
                }else if(formInputOver){
                    removeEvent(e);
                }
            }

                break;
            case GameEvent.EVENT_BBS:
            case GameEvent.EVENT_PRODUCT_TEACHER:
            case GameEvent.EVENT_BATTLE_TEACHER:
            case GameEvent.EVENT_VIEW_BATTLE_SKILL:
            case GameEvent.EVENT_PRODUCE_ITEM:
            case GameEvent.EVENT_CHAT_CIRCLE_OPTION:
            case GameEvent.EVENT_TASK_VIEW:
            case GameEvent.EVENT_PLAYER_VIEW:
            case GameEvent.EVENT_FRIEND_VIEW:
            case GameEvent.EVENT_MAIL_VIEW:
            case GameEvent.EVENT_AUCTION:
            case GameEvent.EVENT_BUY_MATERIAL:
            case GameEvent.EVENT_OEM:
            case GameEvent.EVENT_STORE:
            case GameEvent.EVENT_TONG_LIST:
            case GameEvent.EVENT_CHAT_OPTION:
            case GameEvent.EVENT_PET_OPTION:
            case GameEvent.EVENT_PET_TRADE:
            case GameEvent.EVENT_SYSTEM_OPTION:
            case GameEvent.EVENT_REPAIR:
            case GameEvent.EVENT_SHOP:
            case GameEvent.EVENT_FAQ:
            case GameEvent.EVENT_CALLGM:
            case GameEvent.EVENT_BLACKLIST_VIEW:
            case GameEvent.EVENT_GENERIC_LIST:
            case GameEvent.EVENT_ISHOP_LIST:
            case GameEvent.EVENT_VIEW_EQUIP:
                int taskId = e.getEventTaskId();

                if(_gtvm.hasTask(taskId)){
                    e.setBlockType(GameEvent.EVENT_BLOCK_CYCLE);

                    if(e.idata[1] == 0){
                        GameState.uiRequestTaskId = taskId;
                        GameState.uiDataRequest = true;
                        GameState.uiDataId = e.idata[0];
                        GameState.uiDataTitle = (String)e.odata[0];
                        e.idata[1] = 1;
                    }

                    if(!GameState.taskUIListNeedScroll && !GameState.taskUITitleNeedScroll){
                        GameState.repaintNextTime = false;
                    }

                    if(GameState.uiDataRequest){
                        _gtvm.step(false);
                    }else{
                        removeEvent(e);
                    }
                }else{
                    e.setBlockType(GameEvent.EVENT_BLOCK_DOWNLOAD);
                    return GameEvent.EVENT_BLOCK_DOWNLOAD;
                }

                break;
            case GameEvent.EVENT_PAUSE:
                if(isAnyKeyPressed()){                	
                    removeEvent(e);
                    World.clearKeyStates();
                }
                break;
        }

        return e.getBlockType();
    }

    public static final int TEAM_MEMBER_DRAW_TOP = 30;

    public void draw(Graphics g){
        g.setClip(0, 0, viewWidth, viewHeight);
        g.setColor(0);
        g.fillRect(0, 0, viewWidth, viewHeight);

        try{
            if(gameState == null || gameState.paintBackGroud){
                if(player == null){

                    String[] msg = World.splitString("请稍候...", World.MESSAGEBOX_WIDTH, GameState.font);
                    GameState.drawMsgTip(g, -1, -1, msg, null, null, GameState.BTNTYPE_NONE);

                    return;
                }

                if(nowBattle >= 0){
                    drawBattle(g);
                }else{

                    //msg = "drawMap";
                    drawMap(g);
                    //msg = "drawYOrder";
                    drawYOrder(g);
                    if(topbarFlag != 3){
                        player.drawTopBar(g);
                    }
                }

                if(miniMapOption != 1 && nowBattle < 0 && (gameState == null || gameState.paintMiniMap)){
                    drawMiniMap(g, miniMapOption);
                }
            }

            if(gameState != null){
                gameState.drawState(g);

                return;
            }

            if(teamMode && nowBattle < 0){
                int x, y, w, h;

                //#if font == native
                w = GameState.CHAR_WIDTH * 4 + 10;
                h = GameState.CHAR_HEIGHT + 10;
                //#elif font == image
                //# w = 2*12+10;
                //# h = 12+10;
                //#endif

                //#if JBlend == true
                //# x = World.viewWidth - w;
                //#else
                x = World.viewWidth - w;
                //#endif
                y = World.viewHeight - h;

                int tmy = TEAM_MEMBER_DRAW_TOP;
                int clr;

                for(int i = 0; i < teamMembers.size(); i++){
                    MonsterSprite teamMember = (MonsterSprite)teamMembers.elementAt(i);

                    if(teamMember.id != player.playerID){
                        if(teamMember.teamRole == MonsterSprite.TEAM_ROLE_LEADER){
                            clr = GameItem.CLR_RED;
                        }else{
                            if(teamMember.followMode){
                                clr = GameItem.CLR_BLUE;
                            }else{
                                clr = GameItem.CLR_GREEN;
                            }
                        }

                        draw3DString(g, teamMember.playerName, 4, tmy, Graphics.TOP | Graphics.LEFT, clr);
                        tmy += GameState.LINE_HEIGHT;
                    }
                }

                if(teamLeader){
                }else if(teamStatus == TEAM_STATUS_FOLLOW){
                    GameState.drawBoxWithString(g, x, y, w, h, "独立行动", true);
                  //#if TouchScreen == true
                    StaticUtils.addButton(SOFT_LAST_PRESSED, x, y, w, h);
                    //#endif
                }else if(teamStatus == TEAM_STATUS_NOTFOLLOW){
                    GameState.drawBoxWithString(g, x, y, w, h, "跟随队长", true);
                  //#if TouchScreen == true
                    StaticUtils.addButton(SOFT_LAST_PRESSED, x, y, w, h);
                    //#endif
                }
            }

            if(events.size() > 0 && nowBattle < 0){ //画事件,必须在画完背景以后
                GameEvent e = (GameEvent)events.elementAt(0);
                g.setClip(0, 0, viewWidth, viewHeight);
                drawEvent(g, e);

            //#if CommandEmu == true
            //#     commandEventProc = true;
            //# }else{
            //#     if(commandEventProc){
            //#         processCommand();
            //#         commandEventProc = false;
            //#     }
            //#endif
            }
        }finally{
            if(net_chat_showing){
                drawNetChatMessage(g);
            }

            drawHint(g);
        }
    }

    public void drawEvent(Graphics g, GameEvent e){
        int type = e.getType();
        g.setFont(GameState.font);

        if(e.getBlockType() == GameEvent.EVENT_BLOCK_DOWNLOAD){
            GameState.drawMsgTip(g, -1, -1, new String[]{
                "请稍候..."
            }, null, null, GameState.BTNTYPE_NONE);
            return;
        }

        if(type == GameEvent.EVENT_CHAT){
            ImageSet imageSet = (ImageSet)e.odata[0];

            Object[] strObj = (Object[])e.odata[1];

            int size = strObj.length - e.idata[0];
            if(size > e.idata[2])
                size = e.idata[2];
            Object[] msg = new Object[size];
            System.arraycopy(strObj, e.idata[0], msg, 0, size);
            GameState.drawMsgTip(g, -1, -1, msg, null, imageSet, GameState.BTNTYPE_FIRE);
        }else if(type == GameEvent.EVENT_MESSAGE || type == GameEvent.EVENT_QUESTION){
            Object[] data = e.odata;
            int lineCount = 0;
            if (data.length > 0) {
                lineCount = ((Integer)((Object[])data[data.length - 1])[0]).intValue() + 1;
            }
            int startLine = e.idata[1];
            int endLine = startLine + GameState.BBS_PAGE_COUNT;
            if (endLine > lineCount) {
                endLine = lineCount;
            }
            Vector vec = new Vector();
            for (int i = 0; i < data.length; i++) {
                int lineNo = ((Integer)((Object[])data[i])[0]).intValue();
                if (lineNo >= startLine && lineNo < endLine) {
                    vec.addElement(data[i]);
                }
            }
            data = new Object[vec.size()];
            vec.copyInto(data);
            GameState.drawMsgTip(g, -1, -1, data, null, null, 
                type == GameEvent.EVENT_QUESTION ? GameState.BTNTYPE_SOFT_LEFT : GameState.BTNTYPE_FIRE);
        }else if(type == GameEvent.EVENT_SHOWGETITEM){
            int n = 1;
            int i = 0;
            g.setFont(GameState.font);
            for(n = 1, i = 0; i < e.odata.length; i++){
                if(e.odata[i] instanceof int[]){
                    int[] v = (int[])e.odata[i];

                    if(v[0] >= 4 && v[0] <= 21){
                        n++;
                    }
                }else{
                    n++;
                }
            }

            Object[] msgObj = new Object[n];
            Hashtable colorTable = new Hashtable();
            String mstr = "";

            int p = 0;
            msgObj[p++] = "获得:";

            for(i = 0; i < e.odata.length; i++){
                if(e.odata[i] instanceof int[]){
                    int[] v = (int[])e.odata[i];
                    if(v[0] == 4){
                        msgObj[p] = "恭喜你升级了！";
                        if((tick / 5) % 2 != 0){
                            colorTable.put(new Integer(p), new Integer(0xff0000));
                        }
                        p++;
                    }else if(v[0] == 19){
                        msgObj[p] = "恭喜你的宠物升级了！";
                        if((tick / 5) % 2 != 0){
                            colorTable.put(new Integer(p), new Integer(0xff0000));
                        }
                        p++;
                    }else{
                        if(v[0] == 17 || v[0] == 20){
                            mstr = Sprite.ATTRIBUTENAMES[v[0] - 1];
                        }else{
                            mstr = Sprite.ATTRIBUTENAMES[v[0] - 1] + ":" + v[1];
                        }

                        if((v[0] >= 9 && v[0] <= 16) || v[0] == 21){
                            colorTable.put(new Integer(p), new Integer(0x0000ff));
                        }
                        if(v[0] == 17 || v[0] == 20){
                            colorTable.put(new Integer(p), new Integer(0xff0000));
                        }

                        msgObj[p++] = mstr;
                    }
                }else if(e.odata[i] instanceof PetSprite){
                    PetSprite ps = (PetSprite)e.odata[i];
                    msgObj[p] = ps.name + " " + ps.level + "级";
                    colorTable.put(new Integer(p), new Integer(0xff0000));
                    p++;
                }else{
                    msgObj[p++] = e.odata[i];
                }
            }

            GameState.drawMsgTip(g, -1, 10, msgObj, colorTable, null, GameState.BTNTYPE_FIRE);
        }else if(type == GameEvent.EVENT_DIE_CONFIRM){
            Vector vec = formatString(e.sdata[0], viewWidth - 20, GameState.font);
            Object[] obj = new Object[vec.size()];
            vec.copyInto(obj);
            GameState.drawMsgTip(g, -1, -1, obj, null, null, GameState.BTNTYPE_NONE);
        }else if(type == GameEvent.EVENT_NPC_CONFIRM){
            GameState.drawMsgTip(g, -1, -1, e.sdata, null, null, GameState.BTNTYPE_SOFT_LEFTANDRIGHT);
        }else if(type == GameEvent.EVENT_TASK_CONFIRM){
            GameState.drawMsgTip(g, -1, -1, new String[]{
                "是否接受任务？"
            }, null, null, GameState.BTNTYPE_SOFT_LEFTANDRIGHT);
        }else if(type == GameEvent.EVENT_RESOURCE_CONFIRM){
            Hashtable colorTable = new Hashtable();

            for(int i = 0; i < e.sdata.length; i++){
                colorTable.put(new Integer(i), new Integer(resourceColor[e.idata[3]]));
            }

            GameState.drawMsgTip(g, -1, -1, e.sdata, colorTable, null, e.idata[3] == 0? GameState.BTNTYPE_SOFT_RIGHT: GameState.BTNTYPE_SOFT_LEFTANDRIGHT);
        }else if(type == GameEvent.EVENT_RESOURCE_GATHER){
            int btnType = GameState.BTNTYPE_NONE;
            if(e.idata[1] == 1){
                btnType = GameState.BTNTYPE_FIRE;
            }
            GameState.drawMsgTip(g, -1, -1, e.sdata, null, null, btnType);
        }else if(type == GameEvent.EVENT_GOTO_MAP_LOCAL){
            String msg = "传送中";

            for(int i = 0; i < tick / 5 % 5; i++){
                msg += ".";
            }

            GameState.drawMsgTip(g, -1, -1, new String[]{
                msg
            }, null, null, GameState.BTNTYPE_NONE);
        }else if(type == GameEvent.EVENT_CHANGEATTRIBUTE_CONFIRM || type == GameEvent.EVENT_CHANGEEQUIP_CONFIRM){
            if(e.odata != null){
                GameState.drawMsgTip(g, -1, -1, new String[]{
                    e.odata[0].toString()
                }, null, null, GameState.BTNTYPE_FIRE);
            }
        }else if(type == GameEvent.EVENT_PK_REQUEST){
            GameState.drawMsgTip(g, -1, -1, e.sdata, null, null, GameState.BTNTYPE_SOFT_LEFTANDRIGHT);
        }else if(type == GameEvent.EVENT_TEAM_INVITE){
            GameState.drawMsgTip(g, -1, -1, e.sdata, null, null, GameState.BTNTYPE_SOFT_LEFTANDRIGHT);
        }else if(type == GameEvent.EVENT_PAUSE){
        	//#if Revision == CMCC || (Revision == JIANGSUNCMCC)
            g.setColor(0x000000);
            g.fillRect(0, 0, viewWidth,viewHeight);
            g.setClip(0, 0, viewWidth,viewHeight);
          //#endif
            String msg = "按任意键继续";
            
            for(int i = 0; i < tick / 6 % 4; i++){
                msg += ".";
            }
            GameState.drawMsgTip(g, -1, -1, new String[]{
                msg
            }, null, null, GameState.BTNTYPE_FIRE);
        }
    }

    public static void showQuestion(String msg, int count){
        player.setState(Sprite.STATE_IDLE);

        GameEvent e = new GameEvent(GameEvent.EVENT_QUESTION, 4, 0);
        e.idata[0] = count;
        e.idata[1] = 0;
        e.idata[2] = 0;
        e.idata[3] = 0;

        Vector vec = formatString(msg, MESSAGEBOX_WIDTH, GameState.font);
        Object[] strObj = new Object[vec.size()];
        vec.copyInto(strObj);
        e.odata = strObj;

        addEvent(e);
    }

    public static void showMessage(String msg, byte timeout){
        GameEvent e = new GameEvent(GameEvent.EVENT_MESSAGE, 3, 0);

        if(timeout == 0){
            timeout = 3;
        }

        e.idata[0] = timeout * 1000;
        e.idata[1] = 0;

        Vector vec = formatString(msg, MESSAGEBOX_WIDTH, GameState.font);
        Object[] strObj = new Object[vec.size()];
        vec.copyInto(strObj);
        e.odata = strObj;

        addEvent(e);
    }

    public static int getLastAnswer(){
        int ret = lastAnswer;
        lastAnswer = -1;

        return ret;
    }

    public int collisionMap(int x, int y, int w, int h, int direct, int step, int oldX, int oldY){
        int result = step;

        if(x < 0 || y < 0 || x + w > tileXCount * tileWidth || y + h > tileYCount * tileHeight){
            result = 0;

            switch(direct){
                case Sprite.UP:
                    if(y < 0){
                        result = oldY - (step + y);
                    }

                    break;
                case Sprite.DOWN:
                    if(y + h > tileYCount * tileHeight){
                        result = oldY + (step - y);
                    }

                    break;
                case Sprite.LEFT:
                    if(x < 0){
                        result = oldX - (step + x);
                    }

                    break;
                case Sprite.RIGHT:
                    if(x + w > tileXCount * tileWidth){
                        result = oldX + (step - x);
                    }

                    break;
            }

            if(result < 0){
                result = 0;
            }

            return result;
        }
        //#mdebug
        //        if(1 == 1){
        //            return step;
        //        }
        //#enddebug

        int oldResult;
        int x1 = 0, y1 = 0, w1 = 0, h1 = 0;
        boolean flag;

        int startX = getTileX((short)x);
        int startY = getTileY((short)y);
        int endX = getTileX((short)(x + w)) + ((x + w) % tileWidth == 0? 0: 1);
        int endY = getTileY((short)(y + h)) + ((y + h) % tileHeight == 0? 0: 1);

        if(endX >= tileXCount){
            endX = tileXCount - 1;
        }

        if(endY >= tileYCount){
            endY = tileYCount - 1;
        }

        for(int i = startY; i <= endY; i++){
            for(int j = startX; j <= endX; j++){
                oldResult = result;

                x1 = j * tileWidth;
                y1 = i * tileHeight;
                w1 = tileWidth;
                h1 = tileHeight;

                if(!rectIntersect(x1, y1, w1, h1, x, y, w, h)){
                    continue;
                }

                if(_mapType == MAP_RANDOM){
                    int rTileX = j - randomMapX;
                    int rTileY = i - randomMapY;
                    byte mapItem = map[rTileY][rTileX];

                    int idx1 = ((mapItem & 0xff) >> 6) + 1;
                    int idx2 = (((mapItem & 0xff) >> 4) & 3) + 1;

                    int tile = (mapItem & 0xf) - 1;

                    if(tile >= 0){
                        idx1 = landMapping[idx1];
                        idx2 = landMapping[idx2];
                    }

                    flag = false;
                    if(tile < 0){
                        if(!canPassTile((byte)baseTile, (byte)0)){
                            flag = true;
                        }
                    }else{
                        if(idx2 != idx1){
                            if(!canPassTile((byte)idx2, (byte)tile)){
                                flag = true;
                            }
                        }else{
                            if(!canPassTile((byte)idx1, (byte)tile)){
                                flag = true;
                            }
                        }
                    }

                    if(flag){
                        result = calculateDistance(rTileX * tileWidth, rTileY * tileHeight, tileWidth, tileHeight, oldX, oldY, w, tileHeight, direct);
                    }
                }else{
                    byte stagep = (byte)(mapImageSet.length - 1);
                    int sub = map[i][j] & 0xFF;

                    if(!canPassTile(stagep, sub)){
                        result = calculateDistance(x1, y1, w1, h1, oldX, oldY, w, h, direct);
                    }
                }

                result = Math.min(oldResult, result);
            }
        }

        ImageSet mapNpcImage = getImageSet("npc.p");
        int idx = 0, imgIndex = 0;
        int[] cBox;

        for(int i = 0; i < yOrderCount; i += 4){
            oldResult = result;
            flag = false;
            idx = yOrder[i + 1];

            switch(yOrder[i]){
                case DRAW_ITEMS_NPC:
                    cBox = npcs[idx].getCollisionBox();
                    x1 = cBox[0];
                    y1 = cBox[1];
                    w1 = cBox[2];
                    h1 = cBox[3];

                    if(rectIntersect(x1, y1, w1, h1, x, y, w, h)){
                        flag = true;
                    }

                    break;
                case DRAW_ITMES_MAPNPC:
                    imgIndex = mapNpcData[idx][2];

                    if(mapNpcImage.collision[imgIndex] != null){
                        x1 = mapNpcData[idx][0] * tileWidth + mapNpcImage.collision[imgIndex][0];
                        y1 = (mapNpcData[idx][1] + 1) * tileHeight - mapNpcImage.getHeight(imgIndex) + mapNpcImage.collision[imgIndex][1];
                        w1 = mapNpcImage.collision[imgIndex][2];
                        h1 = mapNpcImage.collision[imgIndex][3];
                    }else{
                        x1 = mapNpcData[idx][0] * tileWidth;
                        y1 = (mapNpcData[idx][1] + 1) * tileHeight - mapNpcImage.getHeight(imgIndex) / 2;
                        w1 = mapNpcImage.getWidth(imgIndex);
                        h1 = mapNpcImage.getHeight(imgIndex) / 2;
                    }

                    if(rectIntersect(x1, y1, w1, h1, x, y, w, h)){
                        flag = true;
                    }

                    break;
                case DRAW_ITEMS_RESOURCE:
                    int res[] = mapResource[idx];

                    x1 = res[1] * tileWidth + resourceImage[getResourceImageID(res[4])].collision[(int)(tick / 5 % 2)][0];
                    y1 = (res[2] + 1) * tileHeight - resourceImage[getResourceImageID(res[4])].getHeight((int)(tick / 5 % 2))
                                    + resourceImage[getResourceImageID(res[4])].collision[(int)(tick / 5 % 2)][1];
                    w1 = resourceImage[getResourceImageID(res[4])].collision[(int)(tick / 5 % 2)][2];
                    h1 = resourceImage[getResourceImageID(res[4])].collision[(int)(tick / 5 % 2)][3];

                    if(rectIntersect(x1, y1, w1, h1, x, y, w, h)){
                        flag = true;
                    }

                    break;
            }

            if(flag){
                result = calculateDistance(x1, y1, w1, h1, oldX, oldY, w, h, direct);
            }

            result = Math.min(result, oldResult);
        }

        return result;
    }

    public int calculateDistance(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2, int direct){
        int result = 0;

        switch(direct){
            case Sprite.UP:
                result = y2 - (y1 + h1);

                break;
            case Sprite.DOWN:
                result = y1 - (y2 + h2);

                break;
            case Sprite.LEFT:
                result = x2 - (x1 + w1);

                break;
            case Sprite.RIGHT:
                result = x1 - (x2 + w2);

                break;
        }

        if(result < 0){
            result = 0;
        }

        return result;
    }

    ///#debug
    //private static long yOrderTime = 0;

    private void createYOrder(){
        ///#debug
        //yOrderTime = System.currentTimeMillis();
        int v1 = 0;
        int v2 = getTileX(viewX);
        int startX = v1 > v2? v1: v2;

        v2 = getTileY(viewY);
        int startY = v1 > v2? v1: v2;

        v1 = tileXCount;
        v2 = getTileX((short)(viewX + viewWidth)) + 1;
        int endX = v1 < v2? v1: v2;

        v1 = tileYCount;
        v2 = getTileY((short)(viewY + viewHeight)) + 1;
        int endY = v1 < v2? v1: v2;

        int mapX, mapY;

        int yOrderPoint = 0;
        int count = 0;
        if(nowBattle < 0){
            yOrder[yOrderPoint++] = DRAW_ITMES_PLAYER;
            yOrder[yOrderPoint++] = 0;
            yOrder[yOrderPoint++] = player.y;
            yOrder[yOrderPoint++] = 0;
            if(player.petCurrent != null){
                count++;
                yOrder[yOrderPoint++] = DRAW_ITEMS_PET;
                yOrder[yOrderPoint++] = -1;
                yOrder[yOrderPoint++] = player.petCurrent.y;
                yOrder[yOrderPoint++] = 0;
            }
        }else{
            count = -1;
        }

        ImageSet mapNpcImage = getImageSet("npc.p");

        for(int i = 0; i < mapNpcData.length; i++){
            int x1, y1, w1, h1;

            x1 = mapNpcData[i][0] * tileWidth;
            y1 = (mapNpcData[i][1] + 1) * tileHeight - mapNpcImage.getHeight(mapNpcData[i][2]);
            w1 = mapNpcImage.getWidth(mapNpcData[i][2]);
            h1 = mapNpcImage.getHeight(mapNpcData[i][2]);

            if(rectIntersect(x1, y1, w1, h1, viewX, viewY, viewWidth, viewHeight)){
                count++;

                yOrder[yOrderPoint++] = DRAW_ITMES_MAPNPC;
                yOrder[yOrderPoint++] = (short)i;
                yOrder[yOrderPoint++] = (short)((mapNpcData[i][1] + 1) * tileHeight);
                yOrder[yOrderPoint++] = 0;
            }
        }

        if(nowBattle < 0){
            //非战斗状态下画矿点
            for(int i = 0; i < mapResource.length; i++){
                mapX = mapResource[i][1];
                mapY = mapResource[i][2];

                if(mapX >= startX && mapX < endX && mapY >= startY && mapY < endY && mapResource[i][6] != 0){
                    count++;

                    yOrder[yOrderPoint++] = DRAW_ITEMS_RESOURCE;
                    yOrder[yOrderPoint++] = (short)i;
                    yOrder[yOrderPoint++] = (short)((mapResource[i][2] + 1) * tileHeight);
                    yOrder[yOrderPoint++] = 0;
                }
            }

            //非战斗状态下画npc
            for(int i = 0; i < npcs.length; i++){
                MonsterSprite npc = npcs[i];
                npc.iconID = 0; // inYOrder
                mapX = npc.rx;
                mapY = npc.ry;

                if(mapX >= startX && mapX < endX && mapY >= startY && mapY < endY && npc.visible){
                    count++;

                    yOrder[yOrderPoint++] = DRAW_ITEMS_NPC;
                    yOrder[yOrderPoint++] = (short)i;
                    yOrder[yOrderPoint++] = (short)((npc.ry + 1) * tileHeight);
                    yOrder[yOrderPoint++] = 0;

                    npc.iconID = 1;

                    if(npc.imageSet.equals(defaultImageSet[0]) || npc.imageSet.equals(defaultImageSet[4])){
                        if(!closeNpcImgDownload){
                            World.sendRequest(GameState.CONN_GET_FILE, new Object[]{
                                            GameState.getModel(), new Short((short)-1), new Short((short)GameState.GET_FILE_NPC), new Short(npc.alertRange)
                            }, true, ASyncRequestThread.makeASyncSign(GameState.CONN_GET_FILE, GameState.GET_FILE_NPC, npc.alertRange));
                        }
                    }
                }
            }

            //非战斗状态下画monster
            for(int i = 0; i < monsters.length; i++){
                MonsterSprite mon = monsters[i];
                mapX = mon.x;
                mapY = mon.y;

                if(mapX >= startX && mapX < endX && mapY >= startY && mapY < endY && mon.visible){
                    count++;

                    yOrder[yOrderPoint++] = DRAW_ITEMS_MONSTERICON;
                    yOrder[yOrderPoint++] = (short)i;
                    yOrder[yOrderPoint++] = (short)((mon.y + 1) * tileHeight);
                    yOrder[yOrderPoint++] = 0;

                    if(mon.imageSet.equals(defaultImageSet[3]) && !closeMonIconDownload){
                        World.sendRequest(GameState.CONN_GET_FILE, new Object[]{
                                        GameState.getModel(), new Short((short)-1), new Short((short)GameState.GET_FILE_MGROUP), new Short(mon.iconID)
                        }, true, ASyncRequestThread.makeASyncSign(GameState.CONN_GET_FILE, GameState.GET_FILE_MONSTER, mon.iconID));
                    }
                }
            }

            for(int i = 0; i < mapDoor.length; i++){
                short doorX, doorY;
                doorX = mapDoor[i][0];
                doorY = mapDoor[i][1];

                if(doorX >= startX && doorX < endX && doorY >= startY && doorY < endY /*&& destMapId == currMapId*/){
                    count++;
                    yOrder[yOrderPoint++] = DRAW_ITEMS_DOOR;
                    yOrder[yOrderPoint++] = (short)i;
                    yOrder[yOrderPoint++] = (short)((doorY + 1) * tileHeight);
                    yOrder[yOrderPoint++] = 0;
                }
            }

            int l = 50;

            int netPlayerCount = 0;
            MonsterSprite.dimID = -1;
            for(int i = 0; i < netPlayersVector.size(); i++){
                MonsterSprite mon = (MonsterSprite)netPlayersVector.elementAt(i);

                if(mon.type == MonsterSprite.TYPE_NETPLAYER){
                    int wx = (player.x - mon.rx > 0)? (player.x - mon.rx): (mon.rx - player.x);
                    int wy = (player.y - mon.ry > 0)? (player.y - mon.ry): (mon.ry - player.y);
                    if(wx + wy < l){
                        l = wx + wy;
                        MonsterSprite.dimID = mon.id;
                    }
                }

                if(mon.teamRole != MonsterSprite.TEAM_ROLE_NONE){
                    continue;
                }

                mapX = mon.x;
                mapY = mon.y;

                if(mon.alertRange == currMapId && mapX >= startX && mapX < endX && mapY >= startY && mapY < endY){
                    count++;
                    netPlayerCount++;

                    if(netPlayerCount > drawNetPlayerSize){
                        break;
                    }

                    yOrder[yOrderPoint++] = DRAW_NETPLAYER;
                    yOrder[yOrderPoint++] = (short)i;
                    yOrder[yOrderPoint++] = (short)mon.ry;
                    yOrder[yOrderPoint++] = 0;

                    if(mon.petCurrent != null){
                        count++;
                        yOrder[yOrderPoint++] = DRAW_ITEMS_PET;
                        yOrder[yOrderPoint++] = (short)i;
                        yOrder[yOrderPoint++] = mon.petCurrent.y;
                        yOrder[yOrderPoint++] = 0;
                    }
                }
            }

            for(int i = 0; i < teamMembers.size(); i++){
                MonsterSprite mon = (MonsterSprite)teamMembers.elementAt(i);

                if(mon.id != player.playerID){
                    if(mon != null){

                        if(mon.x == -1){
                            mon.x = (byte)((mon.rx + mon.getWidth() / 2) / World.tileWidth);
                        }
                        if(mon.y == -1){
                            mon.y = (byte)(mon.ry / World.tileHeight);
                        }
                        mapX = mon.x;
                        mapY = mon.y;

                        if(mon.alertRange == currMapId && mapX >= startX && mapX < endX && mapY >= startY && mapY < endY){
                            count++;

                            yOrder[yOrderPoint++] = DRAW_TEAMMEMBER;
                            yOrder[yOrderPoint++] = (short)i;
                            yOrder[yOrderPoint++] = (short)mon.ry;
                            yOrder[yOrderPoint++] = 0;

                            if(mon.petCurrent != null){
                                count++;
                                yOrder[yOrderPoint++] = DRAW_ITEMS_MEMBER_PET;
                                yOrder[yOrderPoint++] = (short)i;
                                yOrder[yOrderPoint++] = mon.petCurrent.y;
                                yOrder[yOrderPoint++] = 0;
                            }
                        }
                    }
                }
            }
        }

        if(count == -1){
            yOrderCount = 0;
        }else{
            yOrderCount = yOrderPoint;
            sort(yOrder, (yOrderCount >> 2));
        }

        ///#debug
        //yOrderTime = System.currentTimeMillis() - yOrderTime;
    }

    private int compareYOrder(short[] v1, short[] v2){
        if(v1[2] == v2[2]){
            return getYOrderXPoint(v1) - getYOrderXPoint(v2);
        }

        return v1[2] - v2[2];
    }

    private int getYOrderXPoint(short[] in){
        int type = in[0];
        int idx = in[1];
        int result = 0;

        switch(type){
            case DRAW_ITMES_PLAYER:
                result = player.x;

                break;
            case DRAW_ITMES_MAPNPC:
                result = (short)(mapNpcData[idx][0] * tileWidth);

                break;
            case DRAW_ITEMS_NPC:
                result = (short)(npcs[idx].rx * tileWidth);

                break;
        }

        return result;
    }

    /*---------- Game Functions End ----------*/

    /*---------- Map Functions Begin----------*/

    //    String msg = "";
    private void drawMap(Graphics g){
        if(useImageBuffer && bgImg != null){
            int startX = viewX / tileWidth;
            int endX = startX + bgCellW - 1;
            int startY = viewY / tileHeight;
            int endY = startY + bgCellH - 1;

            if(isFirstBgImage){
                isFirstBgImage = false;
                gg.setColor(0);
                gg.fillRect(0, 0, bgWidth, bgHeight);
                drawCellMap(startX, startY, endX, endY);
                oldStartX = startX;
                oldEndX = endX;
                oldStartY = startY;
                oldEndY = endY;
            }

            if(oldStartX != startX){
                int sx, ex;

                //地图向右移动
                if(startX < oldStartX){
                    sx = startX;
                    ex = oldStartX - 1;

                    if(ex > endX){
                        ex = endX;
                    }
                }else{ //地图向左移动
                    sx = oldEndX + 1;
                    ex = endX;

                    if(sx < startX){
                        sx = startX;
                    }
                }

                drawCellMap(sx, oldStartY, ex, oldEndY);

                oldStartX = startX;
                oldEndX = endX;
            }

            if(oldStartY != startY){
                int sy, ey;

                //地图向下移动
                if(startY < oldStartY){
                    sy = startY;
                    ey = oldStartY - 1;

                    if(ey > endY){
                        ey = endY;
                    }
                }else{ //地图向上移动
                    sy = oldEndY + 1;
                    ey = endY;

                    if(sy < startY){
                        sy = startY;
                    }
                }

                drawCellMap(oldStartX, sy, oldEndX, ey);

                oldStartY = startY;
                oldEndY = endY;
            }

            int sMapX = (viewX) % bgWidth;
            int eMapX = (viewX + viewWidth) % bgWidth;
            int sMapY = (viewY) % bgHeight;
            int eMapY = (viewY + viewHeight) % bgHeight;

            if(eMapX > sMapX){
                if(eMapY > sMapY){
                    g.drawImage(bgImg, -sMapX + vx, -sMapY + vy, 0);
                }else{
                    g.setClip(vx, vy, viewWidth, bgHeight - sMapY);
                    g.drawImage(bgImg, -sMapX + vx, -sMapY + vy, 0);
                    g.setClip(vx, bgHeight - sMapY + vy, viewWidth, eMapY);
                    g.drawImage(bgImg, -sMapX + vx, bgHeight - sMapY + vy, 0);
                }
            }else if(eMapY > sMapY){
                g.setClip(vx, vy, bgWidth - sMapX, viewHeight);
                g.drawImage(bgImg, -sMapX + vx, -sMapY + vy, 0);
                g.setClip(bgWidth - sMapX + vx, vy, eMapX, viewHeight);
                g.drawImage(bgImg, bgWidth - sMapX + vx, -sMapY + vy, 0);
            }else{
                g.setClip(vx, vy, bgWidth - sMapX, bgHeight - sMapY);
                g.drawImage(bgImg, -sMapX + vx, -sMapY + vy, 0);
                g.setClip(bgWidth - sMapX + vx, vy, eMapX, bgHeight - sMapY);
                g.drawImage(bgImg, bgWidth - sMapX + vx, -sMapY + vy, 0);
                g.setClip(vx, bgHeight - sMapY + vy, bgWidth - sMapX, eMapY);
                g.drawImage(bgImg, -sMapX + vx, bgHeight - sMapY + vy, 0);
                g.setClip(bgWidth - sMapX + vx, bgHeight - sMapY + vy, eMapX, eMapY);
                g.drawImage(bgImg, bgWidth - sMapX + vx, bgHeight - sMapY + vy, 0);
            }

            g.setClip(0, 0, viewWidth, viewHeight);
        }else{
            drawMapNoBuffer(g);
        }
    }

    public void drawMapNoBuffer(Graphics g){
        int startX = getTileX(viewX);
        int startY = getTileY(viewY);

        if(startX < 0){
            startX = 0;
        }

        if(startY < 0){
            startY = 0;
        }

        int endX = Math.min(tileXCount, getTileX((short)(viewX + viewWidth)) + 1);
        int endY = Math.min(tileYCount, getTileY((short)(viewY + viewHeight)) + 1);

        drawMapSub(g, startX, startY, endX, endY, viewX, viewY);
    }

    public void drawCellMap(int startX, int startY, int endX, int endY){
        ImageSet mapImage = null;
        int sx, sy;

        if(_mapType == MAP_RANDOM){
            rebuildRandomMap();
        }else if(_mapType == MAP_PRECISION){
            mapImage = getImageSet("stage.p");
        }

        for(int i = startX; i <= endX; i++){
            if(i >= 0 && i < tileXCount){
                sx = (i % bgCellW) * tileWidth;

                for(int j = startY; j <= endY; j++){
                    if(j >= 0 && j < tileYCount){
                        sy = (j % bgCellH) * tileHeight;

                        if(_mapType == MAP_RANDOM){
                            drawRandomMapTile(gg, sx, sy, i, j);
                        }else if(_mapType == MAP_PRECISION){
                            mapImage.drawFrame(gg, map[j][i] & 0xff, sx, sy, Graphics.TOP | Graphics.LEFT);
                        }
                    }
                }
            }
        }
    }

    private void drawMapSub(Graphics g, int startX, int startY, int endX, int endY, int posX, int posY){
        ImageSet mapImage = null;

        if(_mapType == MAP_RANDOM){
            rebuildRandomMap();
        }else if(_mapType == MAP_PRECISION){
            mapImage = getImageSet("stage.p");
        }

        for(int i = startY; i < endY; i++){
            for(int j = startX; j < endX; j++){
                if(j < 0 || i < 0 || j >= tileXCount || i >= tileYCount){
                    continue;
                }

                int x = j * tileWidth - posX;
                int y = i * tileHeight - posY;

                if(_mapType == MAP_RANDOM){
                    drawRandomMapTile(g, x, y, j, i);
                }else if(_mapType == MAP_PRECISION){
                    mapImage.drawFrame(g, map[i][j] & 0xff, x, y, Graphics.TOP | Graphics.LEFT);
                }
            }
        }
    }

    private void drawRandomMapTile(Graphics g, int x, int y, int tileX, int tileY){
        int mapItem = map[tileY - randomMapY][tileX - randomMapX];
        ImageSet img1, img2, img0;

        int idx1 = ((mapItem & 0xff) >> 6) + 1;
        int idx2 = (((mapItem & 0xff) >> 4) & 3) + 1;
        int tile = (mapItem & 0xf) - 1;

        img0 = mapImageSet[baseTile];
        int baseTileId = 0;

        if(baseMap != null){
            baseTileId = baseMap[tileY - randomMapY][tileX - randomMapX];
        }

        if(tile < 0){
            img0.drawFrame(g, baseTileId, x, y, Graphics.TOP | Graphics.LEFT);
            return;
        }

        idx1 = landMapping[idx1];
        idx2 = landMapping[idx2];
        img1 = mapImageSet[idx1];
        img2 = mapImageSet[idx2];

        try{
            if(idx1 == idx2){
                if(tile != 0){
                    img0.drawFrame(g, baseTileId, x, y, Graphics.TOP | Graphics.LEFT);
                }
                img1.drawFrame(g, tile, x, y, Graphics.TOP | Graphics.LEFT);
            }else{
                if(tile != 0){
                    img1.drawFrame(g, 0, x, y, Graphics.TOP | Graphics.LEFT);
                }
                img2.drawFrame(g, tile, x, y, Graphics.TOP | Graphics.LEFT);
            }
        }catch(Throwable e){
            e.printStackTrace();
        }
    }

    private void drawYOrder(Graphics g){
        short type, idx;

        if(yOrder == null)
            return;

        ImageSet mapNpcImage = getImageSet("npc.p");

        for(int i = 0; i < yOrderCount; i += 4){
            type = yOrder[i];
            idx = yOrder[i + 1];
            int x, y;

            switch(type){
                case DRAW_ITMES_PLAYER:
                    player.draw(g, viewX, viewY);

                    break;
                case DRAW_ITEMS_PET:
                    if(idx == -1){
                        player.petCurrent.draw(g);
                    }else{
                        if(idx < netPlayersVector.size()){
                            MonsterSprite mon = (MonsterSprite)netPlayersVector.elementAt(idx);
                            mon.petCurrent.draw(g, true);
                        }
                    }
                    break;
                case DRAW_ITEMS_MEMBER_PET: {
                    if(idx < teamMembers.size()){
                        MonsterSprite mon = (MonsterSprite)teamMembers.elementAt(idx);

                        if(mon.petCurrent != null){
                            mon.petCurrent.draw(g, true);
                        }
                    }
                }
                    break;
                case DRAW_ITMES_MAPNPC:
                    x = mapNpcData[idx][0] * tileWidth - viewX;
                    y = mapNpcData[idx][1] * tileHeight - viewY;
                    drawMapNpc(g, x, y, mapNpcImage, mapNpcData[idx][2]);

                    break;
                case DRAW_ITEMS_NPC:
                    MonsterSprite npc = npcs[idx];
                    npc.paint(g);

                    break;
                case DRAW_ITEMS_MONSTERICON: {
                    MonsterSprite mon = monsters[idx];
                    mon.paint(g);

                    break;
                }
                case DRAW_NETPLAYER: {
                    if(idx < netPlayersVector.size()){
                        MonsterSprite mon = (MonsterSprite)netPlayersVector.elementAt(idx);
                        mon.paint(g);
                    }

                    break;
                }
                case DRAW_TEAMMEMBER: {
                    if(idx < teamMembers.size()){
                        MonsterSprite mon = (MonsterSprite)teamMembers.elementAt(idx);

                        if(mon != null){
                            mon.paint(g);
                        }
                    }

                    break;
                }
                case DRAW_ITEMS_RESOURCE:
                    x = mapResource[idx][1] * tileWidth - viewX;
                    y = (mapResource[idx][2] + 1) * tileHeight - viewY;
                    int f = (int)(tick / 5 % 2);

                    x += (tileWidth - resourceImage[getResourceImageID(mapResource[idx][4])].getWidth(f)) / 2;
                    y += (tileHeight - resourceImage[getResourceImageID(mapResource[idx][4])].getHeight(f)) / 2;

                    resourceImage[getResourceImageID(mapResource[idx][4])].drawFrame(g, f, x, y, Graphics.BOTTOM | Graphics.LEFT);

                    break;
                case DRAW_ITEMS_DOOR:

                    x = mapDoor[idx][0] * tileWidth - viewX - (doorImageSet.getWidth(0) - tileWidth) / 2;
                    y = mapDoor[idx][1] * tileHeight - viewY - (doorImageSet.getHeight(0) - tileHeight) / 2;

                    doorImageSet.drawFrame(g, (int)(tick / 5 % 2), x, y, Graphics.TOP | Graphics.LEFT);

                    if(mapDoor[idx][7] == 1){
                        int clr = GameItem.CLR_GREEN;
                        int aid = mapDoor[idx][2] >> 4;
                        if(aid != areaId){
                            clr = GameItem.CLR_BLUE;
                        }
                        int fw = GameState.font.stringWidth(mapTargetName[idx]);
                        y -= GameState.LINE_HEIGHT + 10;
                        if(y < 10){
                            y = 10;
                        }
                        x = x + doorImageSet.getWidth(0) / 2 - fw / 2;
                        if(x < 0){
                            x = 0;
                        }
                        if(x + fw + 8 > viewWidth){
                            x = viewWidth - fw - 8;
                        }
                        g.setFont(GameState.font);

                        GameState.drawBackGround(g, x - 4, y, fw + 8, GameState.LINE_HEIGHT + 2, false);
                        draw3DString(g, mapTargetName[idx], x, y + (GameState.LINE_HEIGHT + 2) / 2 - GameState.CHAR_HEIGHT / 2, Graphics.LEFT | Graphics.TOP, clr);
                    }

                    break;
            }
        }
    }

    public void drawMapNpc(Graphics g, int x, int y, ImageSet npcImageSet, int frame){
        npcImageSet.drawFrame(g, frame, x, y + tileHeight, Graphics.BOTTOM | Graphics.LEFT);
    }

    public int mapNameLeft = 0;

    private void drawMiniMap(Graphics g, short drawType){
        g.setColor(miniMapEdgeColor);

        int x = player.getXPoint() + miniMapPosX;
        int y = player.getYPoint() + miniMapPosY;

        int px = player.getXPoint();
        int py = player.getYPoint();

        if(tileWidth > tileHeight){
            x = player.getXPoint() * 2 + miniMapPosX;
            px = player.getXPoint() * 2;
        }

        //#if (ModelID == NK-60-1) || (ModelID == NK-60-2)
        //# int miniMapX = px - MINIMAP_MAX_WIDTH / 2;
        //# int miniMapY = py - MINIMAP_MAX_HEIGHT / 2;

        //# if(px + MINIMAP_MAX_WIDTH / 2 > miniMapWidth){
        //# miniMapX = miniMapWidth - MINIMAP_MAX_WIDTH;
        //# }
        //# if(py + MINIMAP_MAX_HEIGHT / 2 > miniMapHeight){
        //# miniMapY = miniMapHeight - MINIMAP_MAX_HEIGHT;
        //# }

        //# if(miniMapX < 0)
        //# miniMapX = 0;
        //# if(miniMapY < 0)
        //# miniMapY = 0;

        //# int width = miniMapWidth > MINIMAP_MAX_WIDTH? MINIMAP_MAX_WIDTH: miniMapWidth;
        //# int height = miniMapHeight > MINIMAP_MAX_HEIGHT? MINIMAP_MAX_HEIGHT: miniMapHeight;

        //# DirectGraphics dg = DirectUtils.getDirectGraphics(g);

        //# g.drawRect(miniMapPosX - 1, miniMapPosY - 1, width + 1, height + 1);

        // g.drawRect(miniMapPosX - 1, miniMapPosY - 1, miniMapWidth + 1, miniMapHeight + 1);
        //# dg.drawPixels(miniMapArrayShort, true, miniMapX + miniMapY * miniMapWidth, miniMapWidth, miniMapPosX, miniMapPosY, width, height, 0, DirectGraphics.TYPE_USHORT_4444_ARGB);
        // dg.drawPixels(miniMapArray, true, 0, miniMapWidth, miniMapPosX, miniMapPosY, miniMapWidth, miniMapHeight, 0, DirectGraphics.TYPE_INT_8888_ARGB);
        //# g.setClip(miniMapPosX, miniMapPosY, width, height);
        //#else

        int miniMapX = px - MINIMAP_MAX_WIDTH / 2;
        int miniMapY = py - MINIMAP_MAX_HEIGHT / 2;

        if(px + MINIMAP_MAX_WIDTH / 2 > miniMapImage.getWidth()){
            miniMapX = miniMapImage.getWidth() - MINIMAP_MAX_WIDTH;
        }
        if(py + MINIMAP_MAX_HEIGHT / 2 > miniMapImage.getHeight()){
            miniMapY = miniMapImage.getHeight() - MINIMAP_MAX_HEIGHT;
        }

        if(miniMapX < 0)
            miniMapX = 0;
        if(miniMapY < 0)
            miniMapY = 0;

        int width = miniMapImage.getWidth() > MINIMAP_MAX_WIDTH? MINIMAP_MAX_WIDTH: miniMapImage.getWidth();
        int height = miniMapImage.getHeight() > MINIMAP_MAX_HEIGHT? MINIMAP_MAX_HEIGHT: miniMapImage.getHeight();

        g.drawRect(miniMapPosX - 1, miniMapPosY - 1, width + 1, height + 1);

        g.setClip(miniMapPosX, miniMapPosY, width, height);

        g.drawImage(miniMapImage, miniMapPosX - miniMapX, miniMapPosY - miniMapY, Graphics.TOP | Graphics.LEFT);
        //g.drawImage(miniMapImage, miniMapPosX, miniMapPosY, Graphics.TOP | Graphics.LEFT);
        //#endif
        miniMapFlashCycle++;

        if(miniMapFlashCycle % miniMapFlashFreq == 0){
            miniMapPlayerColorIndex++;

            if(miniMapPlayerColorIndex >= miniMapPlayerColor.length){
                miniMapPlayerColorIndex = 0;
            }
        }

        g.setColor(miniMapPlayerColor[miniMapPlayerColorIndex]);

        if(_mapType == MAP_RANDOM){
            g.fillRect(x - randomMapX - miniMapX, y - randomMapY - miniMapY, 3, 3);
        }else if(_mapType == MAP_PRECISION){
            g.fillRect(x - miniMapX - 1, y - miniMapY - 2, 3, 3);
        }

        if(drawType > 1){
            for(int i = 0; i < monsters.length; i++){
                MonsterSprite mon = monsters[i];
                if(!mon.visible || mon.viewRange == 0)
                    continue;
                x = mon.x + miniMapPosX;
                y = mon.y + miniMapPosY;

                g.setColor(MINI_COLOR_MONSTER);

                if(tileWidth > tileHeight){
                    x = mon.x * 2 + miniMapPosX;
                }
                x -= miniMapX;
                y -= miniMapY;

                if(_mapType == MAP_RANDOM){
                    g.fillRect(x - randomMapX, y - randomMapY, 3, 3);
                }else if(_mapType == MAP_PRECISION){
                    g.fillRect(x - 2, y - 2, 3, 3);
                }
            }
        }

        for(int i = 0; i < npcs.length; i++){
            MonsterSprite npc = npcs[i];

            if(!npc.visible){
                continue;
            }

            x = npc.rx + miniMapPosX;
            y = npc.ry + miniMapPosY;

            switch(npc.arrayIndex){
                case MonsterSprite.NPC_HINT_NONE:
                    if(drawType <= 1){
                        continue;
                    }

                    g.setColor(MINI_COLOR_NPC_NO_TASK);

                    break;
                case MonsterSprite.NPC_HINT_HAS_TASK:
                    g.setColor(miniMapHasTaskNpcColor[miniMapPlayerColorIndex]);

                    break;
                case MonsterSprite.NPC_HINT_DOING_TASK:
                    if(drawType <= 1){
                        continue;
                    }

                    g.setColor(MINI_COLOR_NPC_DOING_TASK);

                    break;
                case MonsterSprite.NPC_HINT_FINISH_TASK:
                    g.setColor(miniMapFinishTaskNpcColor[miniMapPlayerColorIndex]);

                    break;
            }

            if(tileWidth > tileHeight){
                x = npc.rx * 2 + miniMapPosX;
            }

            x -= miniMapX;
            y -= miniMapY;

            if(_mapType == MAP_RANDOM){
                g.fillRect(x - randomMapX, y - randomMapY, 3, 3);
            }else if(_mapType == MAP_PRECISION){
                g.fillRect(x - 2, y - 2, 3, 3);
            }
        }

        for(int i = 0; i < mapDoor.length; i++){
            x = mapDoor[i][0] + miniMapPosX;
            y = mapDoor[i][1] + miniMapPosY;
            if(tileWidth > tileHeight){
                x = mapDoor[i][0] * 2 + miniMapPosX;
            }
            x -= miniMapX;
            y -= miniMapY;
            g.setColor(miniMapDoorColor[miniMapPlayerColorIndex]);
            if(_mapType == MAP_RANDOM){
                g.fillRect(x - randomMapX, y - randomMapY, 3, 3);
            }else if(_mapType == MAP_PRECISION){
                g.fillRect(x - 1, y - 1, 3, 3);
            }
        }

        String mapname = currMapName + "(" + getTileX(player.x) + " , " + getTileY(player.y) + ")";

        //#debug
        mapname += (GOD_MODE? "(GOD_MODE)": "");

        int sw = GameState.font.stringWidth(mapname);
        if(mapNameLeft + sw < miniMapPosX - 10){
            mapNameLeft = viewWidth + 10;
        }else{
            mapNameLeft -= 2;
        }

        //#if (ModelID == NK-60-1) || (ModelID == NK-60-2)
        //# g.setClip(miniMapPosX, miniMapPosY, miniMapWidth, miniMapHeight);
        //#else
        g.setClip(miniMapPosX, miniMapPosY, miniMapImage.getWidth(), miniMapImage.getHeight());
        //#endif

        draw3DString(g, mapname, mapNameLeft, 1, Graphics.LEFT | Graphics.TOP, MonsterSprite.CLR_NPCNAME);

        //测试网速 绘制网络情况
        g.setClip(miniMapPosX - 1, miniMapPosY + height + 1, width + 1, 3);
        g.setColor(netSpeedShowColor[netSpeedLevel]);
        g.fillRect(miniMapPosX - 1, miniMapPosY + height + 1, width + 1, 3);
        g.setColor(0);
        g.drawRect(miniMapPosX - 1, miniMapPosY + height + 1, width, 2);

        g.setClip(0, 0, viewWidth, viewHeight);
    }

    private void processDoor(){
        short mapX, mapY;
        short doorX, doorY, destMapId, destX, destY;

        mapX = player.getXPoint();
        mapY = player.getYPoint();

        for(int i = 0; i < mapDoor.length; i++){
            doorX = mapDoor[i][0];
            doorY = mapDoor[i][1];
            destMapId = mapDoor[i][2];
            destX = mapDoor[i][3];
            destY = mapDoor[i][4];

            int px = World.player.getX() + World.player.getWidth() / 2;
            int py = World.player.getY();

            int d = (px - mapDoor[i][5]) * (px - mapDoor[i][5]) + (py - mapDoor[i][6]) * (py - mapDoor[i][6]);

            if(d < GameState.NPC_SHOWNAME_SPACE * GameState.NPC_SHOWNAME_SPACE){
                mapDoor[i][7] = 1;
            }else{
                mapDoor[i][7] = 0;
            }

            int[] pbox = player.getCollisionBox();

            if(rectIntersect(pbox[0], pbox[1], pbox[2], pbox[3], mapDoor[i][5], mapDoor[i][6], tileWidth, tileHeight)){
                gotoMap(destMapId, destX, destY, true);

                //#debug
                log("Move to Map : " + destMapId + " , " + destX + " , " + destY, true);
            }
        }
    }

    private static void moveMap(){
        viewX = (short)(player.x - viewWidth / 2 + 12);
        viewY = (short)(player.y - viewHeight / 2 - 20);

        if(viewX < 0){
            viewX = 0;
        }

        if(viewY < 0){
            viewY = 0;
        }

        if(viewMaxX < 0 || viewMaxY < 0){
            viewMaxX = (short)((tileWidth * tileXCount - viewWidth) & 0xFFFF);
            viewMaxY = (short)((tileHeight * tileYCount - viewHeight) & 0xFFFF);
        }

        if(viewX > viewMaxX){
            viewX = viewMaxX;
        }

        if(viewY > viewMaxY){
            viewY = viewMaxY;
        }
    }

    public short getTileX(short x){
        return (short)(x / tileWidth);
    }

    public short getTileY(short y){
        return (short)(y / tileHeight);
    }

    public short getTileWidth(){
        return tileWidth;
    }

    public short getTileHeight(){
        return tileWidth;
    }

    private static void initPackageAndStage(InputStream is) throws IOException{
        clearStageResource();
        DataInputStream in = null;

        short checkCRC = 0;
        byte[] head = new byte[12];
        byte[] body = null;
        byte[] testHead = new byte[]{
                        'R', 'P', 'G'
        };

        try{
            in = new DataInputStream(is);

            //head “RPG” + 1字节返回码 + 4字节文件长度（包括文件头）+ 2字节版本号 + 2字节CRC
            in.readFully(head);
            //info = "read head";
            for(int i = 0; i < testHead.length; i++){
                if(head[i] != testHead[i]){
                    throw new IOException("Invalid Package File");
                }
            }

            if(head[3] != 0){
                throw new IOException("Error Return Code");
            }

            int flength = (int)getNumber(head, 4, 4);
            checkCRC = (short)(((head[10] & 0xFF) << 8) | (head[11] & 0xFF));

            body = new byte[flength - 12];
            in.readFully(body);
            in.close();

            short crc = 0;
            crc = checkCRC(head, crc, 0, 10);
            crc = checkCRC(body, crc, 0, body.length);

            if(crc != checkCRC){
                //throw new IOException("Error CRC");
                //#debug
                log("Error CRC : " + Integer.toHexString(checkCRC) + " , " + Integer.toHexString(crc), true);
            }

            in = new DataInputStream(new ByteArrayInputStream(body));

            short offset = (short)12;

            //segments 两字节数据长度 + 数据
            short slen;

            //segment 1 两字节关卡号 + 关卡名称（字符串）+ 1字节背景音乐ID + 1字节缺省地图ID + 1字节缺省X位置 +１字节缺省Ｙ位置
            slen = in.readShort();
            offset += slen + 2;
            areaId = in.readShort(); //stage Id
            in.readUTF(); //stage Name
            in.readByte(); //music Id
            _defaultMapId = _mapId = in.readByte();
            currMapId = (short)((areaId << 4) | (_mapId & 0xF));
            _defaultX = in.readByte(); //Default X
            _defaultY = in.readByte(); //Default Y

            //segment 2 两字节文件个数 + 文件名（字符串） + 文件名（字符串）+ ……
            slen = in.readShort();
            offset += slen + 2;
            int fileCount = in.readShort();
            String[] fnames = new String[fileCount];
            byte[] fdata = null;

            //info = "read file name";
            for(int i = 0; i < fileCount; i++){
                fnames[i] = in.readUTF();

                //#mdebug
                if(fnames[i].endsWith(".etf")){
                    System.out.println("get etffile:" + fnames[i]);
                }
                //#enddebug
            }

            //World.gameState._message = "文件列表读取完毕";

            //info = "init packageCache";
            _packageCache.clear();
            _imageCache.clear();

            //segment > 2 每个数据段是一个文件的数据
            for(int i = 0; i < fileCount; i++){
                slen = in.readShort();
                fdata = new byte[slen];
                in.readFully(fdata);
                _packageCache.put(fnames[i], fdata);
            }
            //World.gameState._message = "文件缓存完毕";

            in.close();

            //处理图像文件，将_packageCache中所存的.s文件删掉，将.p文件的数据替换成ImageSet，并将l打头的.p文件（地图tile）放到mapImageSet数组中以加速绘图速度。
            //处理ETF文件，将脚本放入虚拟机中

            //#if LoadAllImage == TRUE
            mapImageSet = new ImageSet[fileCount];
            int mapImageSetCount = 0;
            //#endif

            for(int i = 0; i < fileCount; i++){
                String pname = fnames[i];

                //#if LoadAllImage == TRUE
                if(pname.endsWith(".p")){
                    int idx = pname.indexOf(".p");
                    String sname = pname.substring(0, idx);
                    sname += ".s";
                    byte[] pdata = (byte[])_packageCache.get(pname);
                    byte[] sdata = (byte[])_packageCache.get(sname);
                    DataInputStream sStream = new DataInputStream(new ByteArrayInputStream(sdata));

                    try{
                        Image pimg = Image.createImage(pdata, 0, pdata.length);
                        //info = "create imageset " + pname;
                        ImageSet pImgSet = ImageSet.createImageSet(pimg, sStream, true);

                        _packageCache.remove(pname);
                        _packageCache.remove(sname);
                        _packageCache.put(pname, pImgSet);

                        //#debug
                        System.out.println("load image: " + pname);

                        if(pname.startsWith("l")){
                            String tmp = pname.substring(0, pname.indexOf(".p"));
                            tmp = tmp.substring(1);

                            int imageSetNum = Integer.parseInt(tmp);
                            mapImageSet[imageSetNum] = pImgSet;
                            mapImageSetCount++;
                        }
                    }catch(Throwable e){
                        //#debug
                        e.printStackTrace();
                    }finally{
                        if(sStream != null){
                            try{
                                sStream.close();
                            }catch(Exception e){
                            }
                        }
                    }
                }else
                //#endif

                if(pname.endsWith(".etf")){
                    byte[] etfData = (byte[])_packageCache.get(pname);
                    ETFFile etf = ETFFile.load(new ByteArrayInputStream(etfData));

                    //#debug
                    System.out.println("load task " + pname);

                    _gtvm.addTask(etf);
                }else{
                    continue;
                }
            }
            //#if LoadAllImage == TRUE
            mapImageSet[mapImageSetCount] = getImageSet("stage.p");
            mapImageSetCount++;

            //整理mapImageSet数组
            if(mapImageSetCount > 0){
                ImageSet[] tmpArray = new ImageSet[mapImageSetCount];
                System.arraycopy(mapImageSet, 0, tmpArray, 0, tmpArray.length);
                mapImageSet = tmpArray;
            }else{
                mapImageSet = null;
            }
            //#endif

            /*byte[] monsterSkills = (byte[])_packageCache.get("ms.d");
             Skill.addSkills(monsterSkills);*/

            //如果下载包中包含主角形象，则替换
            playerImageSet = new ImageSet[5];
            if(getImageSet("_common.p") != null){
                //#debug
                System.out.println("load common image");
                playerImageSet[0] = getImageSet("_common.p");
                playerImageSet[1] = playerImageSet[0];
                useCommonFace = true;
            }else{
                useCommonFace = false;
                if(getImageSet("_male.p") != null){
                    playerImageSet[0] = getImageSet("_male.p");
                    useCommonFace = true;
                }else{
                    playerImageSet[0] = getImageSetFromLocal("_male");
                }
                //info = "load male.p";

                if(getImageSet("_female.p") != null){
                    playerImageSet[1] = getImageSet("_female.p");
                    useCommonFace = true;
                }else{
                    playerImageSet[1] = getImageSetFromLocal("_female");
                }
            }

            for(int i = 0; i < netPlayersVector.size(); i++){
                MonsterSprite p = (MonsterSprite)netPlayersVector.elementAt(i);
                p.imageSet = getPlayerImage(getFaceIndex(p.iconID, false));
            }

            for(int i = 0; i < teamMembers.size(); i++){
                MonsterSprite p = (MonsterSprite)teamMembers.elementAt(i);
                p.imageSet = getPlayerImage(getFaceIndex(p.iconID, false));
            }

            if(player != null){
                player.refreshImageSet();
            }
        }catch(IOException ex){
            //#debug
            ex.printStackTrace();
            throw ex;
        }finally{
            if(in != null){
                try{
                    in.close();
                }catch(IOException ex1){
                    //#debug
                    ex1.printStackTrace();
                }
            }
        }
    }

    public static void clearMapResource(){
        _imageCache.clear();
        map = null;
        baseMap = null;
        //#if (ModelID != NK-60-1) && (ModelID != NK-60-2)
        miniMapImage = null;
        //#endif
        randomMapData = null;
        mapNpcData = null;
        npcs = null;
        monsters = null;
        npcIdTable.clear();
        mapDoor = null; //[0] x, [1] y, [2] 目标地图ID, [3] dx, [4] dy, [5]~[6] CollisionBox x & y
        mapResource = null;
        yOrder = null;
        yOrderCount = 0;
    }

    public static void clearStageResource(){
        clearMapResource();
        _packageCache.clear();
        _imageCache.clear();
        mapImageSet = null;
    }

    public static void loadMap(int nMap, short x, short y, boolean isMapXY) throws IOException{
        clearMapResource();
        DataInputStream in = new DataInputStream(getPackageFileAsStream("map_" + nMap + ".m"));

        _mapType = in.readByte();

        if(_mapType == MAP_RANDOM){ //模糊地图
            tileXCount = in.readByte();
            tileYCount = in.readByte();
            currMapName = in.readUTF();
            in.readShort(); //地形块数据长度
            int mSeed = in.readInt();
            baseTile = in.readByte();
            short randArea = in.readShort();
            ImageSet basep = getImageSet("l" + baseTile + ".p");
            tileWidth = (short)(basep.width & 0xFF);
            tileHeight = (short)(basep.height & 0xFF);
            //#debug
            log(_mapType + "[" + currMapName + "] , " + mSeed + " , " + tileXCount + " , " + tileYCount + " , " + baseTile + " , " + randArea, true);

            randomMapWidth = viewWidth / tileWidth + randomMapBufferLines * 2;
            randomMapHeight = viewHeight / tileHeight + randomMapBufferLines * 2;

            if(randomMapWidth > tileXCount){
                randomMapWidth = tileXCount;
            }

            if(randomMapHeight > tileYCount){
                randomMapHeight = tileYCount;
            }

            //load Random Area;
            short[][] rmData = new short[randArea][6];
            //#debug
            log("Random Area : " + randArea, true);

            for(int i = 0; i < randArea; i++){
                if(i < 1000){
                    rmData[i][0] = (short)(in.readByte() & 0xFF);
                    rmData[i][1] = (short)(in.readByte() & 0xFF);
                    rmData[i][2] = (short)(in.readByte() & 0xFF);
                    rmData[i][3] = (short)(in.readByte() & 0xFF);
                    rmData[i][4] = (short)(in.readByte() & 0xFF);
                    rmData[i][5] = (short)(in.readByte() & 0xFF);

                    //log(rmData[i][0] + " , " + rmData[i][1] + " , " + rmData[i][2] + " , " + rmData[i][3] + " , " + rmData[i][4] + " , " + rmData[i][5], true);
                }
            }

            randomMapData = rmData;
            randomMapSeed = mSeed;

            //#if LoadAllImage == FALSE
            //# mapImageSet = new ImageSet[_packageCache.size()];
            //# int mapImageSetCount = 0;

            //# Enumeration emu = _packageCache.keys();

            //# while(emu.hasMoreElements()){
            //# String pname = (String)emu.nextElement();
            //# if(pname.startsWith("l") && pname.endsWith(".p")){
            //#     String tmp = pname.substring(0, pname.indexOf(".p"));
            //#     tmp = tmp.substring(1);
            //#     int imageSetNum = Integer.parseInt(tmp);
            //#     mapImageSet[imageSetNum] = getImageSet(pname);
            //#     mapImageSetCount++;
            //# }
            //# }

            //整理mapImageSet数组
            //# if(mapImageSetCount > 0){
            //# ImageSet[] tmpArray = new ImageSet[mapImageSetCount];
            //# System.arraycopy(mapImageSet, 0, tmpArray, 0, tmpArray.length);
            //# mapImageSet = tmpArray;
            //# }else{
            //# mapImageSet = null;
            //# }
            //#endif

        }else if(_mapType == MAP_PRECISION){ //精确地图
            ImageSet stagep = getImageSet("stage.p");

            //#if LoadAllImage == FALSE
            //# mapImageSet = new ImageSet[1];
            //# mapImageSet[0] = stagep;
            //#endif

            tileWidth = stagep.width;
            tileHeight = stagep.height;

            tileXCount = in.readByte();
            tileYCount = in.readByte();
            currMapName = in.readUTF();
            map = new byte[tileYCount][tileXCount];
            //#debug
            log("Precision Map[" + currMapName + "] , " + tileXCount + " , " + tileYCount, true);

            byte[] data = new byte[in.readShort()];
            in.readFully(data);
            boolean[] usage = new boolean[256];
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            for(int i = 0; i < data.length; i++){
                int b = data[i] & 0xFF;

                if(b < 208){
                    out.write(b);
                    usage[b & 0xFF] = true;
                }else{
                    int n = b - 205;
                    i++;
                    b = data[i];

                    usage[b & 0xFF] = true;

                    for(int j = 0; j < n; j++){
                        out.write(b);
                    }
                }
            }

            data = out.toByteArray();

            for(int i = 0; i < tileYCount; i++){
                for(int j = 0; j < tileXCount; j++){
                    map[i][j] = data[i * tileXCount + j];
                }
            }
        }

        //load Map NPC
        short nMapNpc = in.readShort();
        mapNpcData = new short[nMapNpc][3];
        //#debug
        log("map NPC : " + nMapNpc, true);

        for(int i = 0; i < nMapNpc; i++){
            mapNpcData[i][0] = (short)(in.readByte() & 0xFF);
            mapNpcData[i][1] = (short)(in.readByte() & 0xFF);
            mapNpcData[i][2] = (short)(in.readByte() & 0xFF);
        }

        //loadMonster
        short nMonster = in.readShort();
        //        monsters = new MonsterSprite[1];
        monsters = new MonsterSprite[nMonster];
        //#debug
        log("", true);
        //#debug
        log("Monster : " + nMonster, true);

        for(int i = 0; i < nMonster; i++){
            MonsterSprite mon = new MonsterSprite(MonsterSprite.TYPE_MONSTER);
            mon.load(in);
            mon.setImageSet(getSpriteImageSet((short)3, mon.iconID));
            mon.arrayIndex = i;
            monsters[i] = mon;

            //#debug
            log("Monster[" + mon.id + "] refresh time:" + mon.refreshTime, true);
        }

        if(monsterRefreshPool == null)
            monsterRefreshPool = new Hashtable();

        //loadNpc
        short nNpc = in.readShort();
        npcs = new MonsterSprite[nNpc];
        npcIdTable = new Hashtable();
        //#debug
        log("Npc : " + nNpc, true);

        for(int i = 0; i < nNpc; i++){
            MonsterSprite nnpc = new MonsterSprite(MonsterSprite.TYPE_NPC);

            nnpc.id = in.readInt();
            nnpc.alertRange = in.readShort();
            nnpc.playerName = in.readUTF();
            nnpc.rx = in.readByte();
            nnpc.ry = in.readByte();
            nnpc.refreshTime = in.readShort();
            nnpc.politics = in.readByte();
            nnpc.flag = in.readByte();
            nnpc.parseFlag();

            if(nnpc.politics == NPC_TYPE_ITEMNPC){
                nnpc.imageSet = getSpriteImageSet((short)4, nnpc.alertRange);
            }else{
                nnpc.imageSet = getSpriteImageSet(nnpc.parseID()[1], nnpc.alertRange);
            }

            int idx = nnpc.playerName.indexOf("&");

            if(idx >= 0){
                nnpc.questionName = nnpc.playerName.substring(idx + 1);
                nnpc.playerName = nnpc.playerName.substring(0, idx);
            }

            npcs[i] = nnpc;

            Integer npcIdx = new Integer(nnpc.id);
            npcIdTable.put(npcIdx, npcs[i]);
            //#debug
            log(nnpc.playerName + " = " + nnpc.politics, true);
        }

        //loadResource
        short nResource = in.readShort();
        //#debug
        log("Resource = " + nResource, true);
        mapResource = new int[nResource][8];

        for(int i = 0; i < nResource; i++){
            mapResource[i][0] = in.readInt(); //id
            mapResource[i][1] = in.readByte() & 0xFF; //mapX
            mapResource[i][2] = in.readByte() & 0xFF; //mapY
            mapResource[i][3] = in.readByte() & 0xFF; //level

            mapResource[i][4] = in.readByte() & 0xFF; //flag
            mapResource[i][5] = ((mapResource[i][4] & 0x10) == 0)? 0: 1; //needGame
            mapResource[i][6] = ((mapResource[i][4] & 0x80) == 0)? 0: 1; //visible
            mapResource[i][4] = mapResource[i][4] & 0x0F; //type

            mapResource[i][7] = in.readByte() & 0xFF; //itemid

            mapResource[i][6] = 0; //init invisible, wait servere refresh

            in.skip(2); //refresh time
            //#debug
            System.out.println("load resource:" + mapResource[i][0] + " " + mapResource[i][4]);
        }

        //loadDoor
        int nDoor = in.readByte() & 0xFF;
        //#debug
        log("Doors = " + nDoor, true);

        if(nDoor > 0){
            mapDoor = new short[nDoor][8];
            mapTargetName = new String[nDoor];
            for(int i = 0; i < nDoor; i++){
                mapDoor[i][0] = (short)(in.readByte() & 0xFF);
                mapDoor[i][1] = (short)(in.readByte() & 0xFF);
                mapDoor[i][2] = in.readShort();
                mapDoor[i][3] = (short)(in.readByte() & 0xFF);
                mapDoor[i][4] = (short)(in.readByte() & 0xFF);

                mapDoor[i][5] = (short)(mapDoor[i][0] * tileWidth);
                mapDoor[i][6] = (short)(mapDoor[i][1] * tileHeight);
                mapDoor[i][7] = 0;
                mapTargetName[i] = in.readUTF();

                //#debug
                System.out.println("door " + i + ":" + mapDoor[i][0] + "," + mapDoor[i][1]);
            }
        }else{
            mapDoor = new short[0][0];
        }

        if(_mapType == MAP_RANDOM){
            try{
                byte mcount = in.readByte();
                landMapping = new byte[mcount];
                in.read(landMapping);
            }catch(Exception e){
                //#debug
                System.out.println("地形映射读取错误，使用默认映射");

                landMapping = new byte[]{
                                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
                };
            }
            makeRadomMap(0, 0);
        }

        //读取地图PK类型
        _mapPkType = in.readByte();

        buildMiniMap();

        bgImg = null;

        yOrder = null;
        yOrderCount = 0;
        events.removeAllElements();
        viewMaxX = (short)((tileWidth * tileXCount - viewWidth) & 0xFFFF);
        viewMaxY = (short)((tileHeight * tileYCount - viewHeight) & 0xFFFF);

        if(isMapXY){
            player.x = (short)(x * tileWidth);
            player.y = (short)(y * tileHeight);
        }else{
            player.x = x;
            player.y = y;
        }
        moveMap();

        clearKeyStates();

        sendPositionTime = SENDPOSITIONTIME_LIMIT + 1;
        showMessage(currMapName + MAP_PK_TYPE[_mapPkType], (byte)5);
        player.runAwayTime = 5000;

        if(player.petCurrent != null){
            int xy[] = player.getBackXY();
            player.petCurrent.x = (short)xy[0];
            player.petCurrent.y = (short)xy[1];
        }

        GameState.isMapLoadOk = true;
        yOrderCount = 4 * (2 + mapNpcData.length + npcs.length + monsters.length + mapResource.length + mapDoor.length + 23 * 2 + 3 * 2);
        yOrder = new short[yOrderCount];
    }

    public static void rebuildBackBuffer(){
        if(useImageBuffer){
            System.gc();

            bgCellW = (viewWidth / tileWidth) + 1;
            bgCellH = (viewHeight / tileHeight) + 1;
            if(viewWidth % tileWidth != 0){
                bgCellW++;
            }
            if(viewHeight % tileHeight != 0){
                bgCellH++;
            }
            bgWidth = bgCellW * tileWidth;
            bgHeight = bgCellH * tileHeight;
            bgImg = Image.createImage(bgWidth, bgHeight);
            gg = bgImg.getGraphics();
            isFirstBgImage = true;
        }
    }

    private void rebuildRandomMap(){
        int viewTileX, viewTileY;
        int newmx, newmy;

        newmx = randomMapX;
        newmy = randomMapY;
        viewTileX = viewX / tileWidth;
        viewTileY = viewY / tileHeight;

        if(viewTileX - randomMapX >= randomMapBufferLines * 2 - 1 || viewTileX - randomMapX < 1){
            newmx = viewTileX - randomMapBufferLines;
        }

        if(viewTileY - randomMapY >= randomMapBufferLines * 2 - 1 || viewTileY - randomMapY < 1){
            newmy = viewTileY - randomMapBufferLines;
        }

        if(newmx < 0){
            newmx = 0;
        }

        if(newmy < 0){
            newmy = 0;
        }

        if(newmx != randomMapX || newmy != randomMapY){
            newmx = viewTileX - randomMapBufferLines;
            newmy = viewTileY - randomMapBufferLines;

            if(newmx < 0){
                newmx = 0;
            }

            if(newmy < 0){
                newmy = 0;
            }

            makeRadomMap(newmx, newmy);
            buildMiniMap();
        }
    }

    private static short getMappingID(int realLand){
        for(int i = 0; i < landMapping.length; i++){
            if(landMapping[i] == realLand){
                if(i >= 5){
                    i = -1;
                }
                return (short)i;
            }
        }
        return 0;
    }

    private static void makeRadomMap(int mx, int my){
        Random rnd = new Random(randomMapSeed);
        short rmType;
        short rmLandId;
        short rmX;
        short rmY;
        short rmW;
        short rmH;

        randomMapX = mx;
        randomMapY = my;
        map = new byte[randomMapHeight][randomMapWidth];

        if(mapImageSet[baseTile].hasChildren()){
            baseMap = new byte[randomMapHeight][randomMapWidth];
            ImageSet img = mapImageSet[baseTile];
            for(int i = 0; i < randomMapHeight; i++){
                for(int j = 0; j < randomMapWidth; j++){
                    if(img.childs[0] != null){
                        int r = rnd.nextInt(9);

                        if(r < 2){
                            if(img.childs[0].length == 1){
                                r = 0;
                            }else{
                                r = rnd.nextInt(img.childs[0].length - 1);
                            }
                            baseMap[i][j] = img.childs[0][r];
                        }
                    }
                }
            }
        }else{
            baseMap = null;
        }

        //rect [0] x , [1] y, [2] w, [3] h
        int[] rectLr = new int[4];
        int[] rectR = new int[4];
        int[] rectTr = new int[4];

        for(int i = 0; i < randomMapData.length; i++){
            rmType = randomMapData[i][0];
            rmLandId = getMappingID(randomMapData[i][1]);
            rmX = randomMapData[i][2];
            rmY = randomMapData[i][3];
            rmW = randomMapData[i][4];
            rmH = randomMapData[i][5];

            if(rmLandId == -1 || !rectIntersect(rmX, rmY, rmW, rmH, randomMapX, randomMapY, randomMapWidth, randomMapHeight)){
                continue;
            }

            if(rmType == 0){
                rectLr[0] = rmX * tileWidth + tileWidth / 2;
                rectLr[1] = rmY * tileHeight + tileHeight / 2;
                rectLr[2] = rectLr[0] + rmW * tileWidth;
                rectLr[3] = rectLr[1] + rmH * tileHeight;

                for(int y = rmY; y <= rmY + rmH; y++){
                    for(int x = rmX; x <= rmX + rmW; x++){
                        rectR[0] = x * tileWidth;
                        rectR[1] = y * tileHeight;
                        rectR[2] = rectR[0] + tileWidth;
                        rectR[3] = rectR[1] + tileHeight;

                        rectTr[0] = rectR[0];
                        rectTr[1] = rectR[1];
                        rectTr[2] = rectR[2];
                        rectTr[3] = rectR[3];

                        if(rectTr[0] < rectLr[0]){
                            rectTr[0] = rectLr[0];
                        }

                        if(rectTr[1] < rectLr[1]){
                            rectTr[1] = rectLr[1];
                        }

                        if(rectTr[2] > rectLr[2]){
                            rectTr[2] = rectLr[2];
                        }

                        if(rectTr[3] > rectLr[3]){
                            rectTr[3] = rectLr[3];
                        }

                        setRandomMapTile(x, y, x, y, (short)((rmLandId << 8) | checkRect(rectR, rectTr)));
                    }
                }
            }else if(rmType == 1){
                for(int y = 0; y < (rmH + 1) / 2; y++){
                    int cx = rmX + (rmW + 1) / 2 - (y + 1);

                    for(int x = 0; x < (y + 1) * 2; x++){
                        byte ti = 0;

                        if(x == 0){
                            ti = 1;
                        }else if(x == (y + 1) * 2 - 1){
                            ti = 2;
                        }else if(x == 1){
                            ti = 7;
                        }else if(x == (y + 1) * 2 - 2){
                            ti = 11;
                        }else{
                            ti = 15;
                        }

                        setRandomMapTile(cx, rmY + y, cx, rmY + y, (short)((rmLandId << 8) | ti));
                        cx++;
                    }
                }

                for(int y = rmH; y >= (rmH + 1) / 2; y--){
                    int cx = rmX + y - (rmH + 1) / 2;

                    for(int x = 0; x < (rmH - y + 1) * 2; x++){
                        byte ti = 0;

                        if(x == 0){
                            ti = 4;
                        }else if(x == (rmH - y + 1) * 2 - 1){
                            ti = 8;
                        }else if(x == 1){
                            ti = 13;
                        }else if(x == (rmH - y + 1) * 2 - 2){
                            ti = 14;
                        }else{
                            ti = 15;
                        }

                        setRandomMapTile(cx, rmY + y, cx, rmY + y, (short)((rmLandId << 8) | ti));
                        cx++;
                    }
                }
            }
        }

        for(int i = 0; i < map.length; i++){
            for(int j = 0; j < map[i].length; j++){
                try{
                    if((map[i][j] & 0x0f) == 0){
                        continue;
                    }
                    int file1 = ((map[i][j] & 0xFF) >> 6);
                    int file2 = (((map[i][j] & 0xFF) >> 4) & 0x3);
                    if(file1 >= mapImageSet.length){
                        file1 = 0;
                    }
                    if(file2 >= mapImageSet.length){
                        file2 = 0;
                    }
                    ImageSet img1 = mapImageSet[landMapping[file1 + 1]];
                    ImageSet img2 = mapImageSet[landMapping[file2 + 1]];
                    int tile, newTile;
                    tile = map[i][j] & 0xF;
                    newTile = 0;
                    if(tile > 0){
                        newTile = replaceTile(file1 + 1, tile, j + randomMapX, i + randomMapY, rnd, img1);
                    }
                    if(file2 != file1){
                        newTile = replaceTile(file2 + 1, tile, j + randomMapX, i + randomMapY, rnd, img2);
                    }
                    newTile = newTile + 1;
                    if(newTile > 15){
                        newTile = 15;
                    }
                    map[i][j] = (byte)(file1 << 6 | file2 << 4 | (newTile & 0x0f));
                }catch(Throwable e){
                    //#debug
                    e.printStackTrace();
                }
            }
        }

        randomMapData = null;

    }

    private static byte addTile(byte otile, short addTile){
        int mapL1 = (otile & 0xff) >> 6;
        int mapL2 = ((otile & 0xff) >> 4) & 3;
        int tileID = otile & 0xf;

        int addLand = addTile >> 8;

        int addTileID = addTile & 0xff;

        if(tileID == 0){
            mapL1 = addLand - 1;
        }else if(mapL1 == mapL2){
            if(addLand - 1 == mapL1){
                addTileID |= tileID;
            }
        }else{
            if(addLand - 1 == mapL2){
                addTileID |= tileID;
            }else{
                mapL1 = mapL2;
            }
        }
        mapL2 = addLand - 1;
        tileID = addTileID;
        return (byte)(mapL1 << 6 | mapL2 << 4 | (tileID & 0x0f));
    }

    private static byte checkRect(int[] r1, int[] r2){
        byte ret = (byte)0x0f;
        int dx = r2[0] - r1[0];
        int dy = r2[1] - r1[1];
        int dr = r2[2] - r1[2];
        int db = r2[3] - r1[3];

        if(dx != 0){
            ret &= 5;
        }
        if(dy != 0){
            ret &= 3;
        }
        if(dr != 0){
            ret &= 10;
        }
        if(db != 0){
            ret &= 12;
        }

        return ret;
    }

    private static void setRandomMapTile(int x, int y, int x1, int y1, short tile){
        if(x >= randomMapX && x < randomMapX + randomMapWidth && y >= randomMapY && y < randomMapY + randomMapHeight){
            if(x1 >= randomMapX && x1 < randomMapX + randomMapWidth && y1 >= randomMapY && y1 < randomMapY + randomMapHeight){
                map[y - randomMapY][x - randomMapX] = addTile(map[y1 - randomMapY][x1 - randomMapX], tile);
            }
        }
    }

    private static byte replaceTile(int file, int tile, int mapX, int mapY, Random rand, ImageSet img){
        int s = tile;
        int fix = 0;

        if(s != 0){
            if(mapX == 0){
                if((s & 4) != 0){
                    fix |= 8;
                }
                if((s & 1) != 0){
                    fix |= 2;
                }
            }
            if(mapX == tileXCount - 1){
                if((s & 8) != 0){
                    fix |= 4;
                }
                if((s & 2) != 0){
                    fix |= 1;
                }
            }
            if(mapY == 0){
                if((s & 2) != 0){
                    fix |= 8;
                }
                if((s & 1) != 0){
                    fix |= 4;
                }
            }
            if(mapY == tileYCount - 1){
                if((s & 8) != 0){
                    fix |= 2;
                }
                if((s & 4) != 0){
                    fix |= 1;
                }
            }

            s |= fix;
        }

        byte rtile = 0;

        rtile = shapeTile[s];

        if(rtile >= 0){
            try{
                if(rtile < img.childs.length && img.childs[rtile] != null){
                    int r = rand.nextInt(9);

                    if(r < 2){
                        if(img.childs[rtile].length == 1){
                            r = 0;
                        }else{
                            r = rand.nextInt(img.childs[rtile].length - 1);
                        }

                        rtile = img.childs[rtile][r];
                    }
                }
            }catch(Exception e){
                //#debug
                e.printStackTrace();
            }
        }

        return rtile;
    }

    //#if (ModelID == NK-60-1) || (ModelID == NK-60-2)
    //# private static int miniMapWidth, miniMapHeight;
    //# private static short[] miniMapArrayShort;
    //#endif

    private static void buildMiniMap(){
        //#if (ModelID != NK-60-1) && (ModelID != NK-60-2)
        int miniMapWidth, miniMapHeight;
        //#endif
        if(_mapType == MAP_RANDOM){
            miniMapWidth = Math.min(tileXCount, randomMapWidth);
            miniMapHeight = Math.min(tileYCount, randomMapHeight);
        }else{
            miniMapWidth = tileXCount;
            miniMapHeight = tileYCount;

            if(tileWidth > tileHeight){
                miniMapWidth = tileXCount * 2;
            }
        }

        int[] miniMapArray = new int[miniMapWidth * miniMapHeight];
        ImageSet miniMapImageSet = null;

        for(int i = 0; i < miniMapHeight; i++){
            for(int j = 0; j < miniMapWidth; j++){
                if(_mapType == MAP_RANDOM){
                    try{
                        int mapItem, idx1, idx2, tile;
                        mapItem = map[i][j];

                        idx1 = ((mapItem & 0xff) >> 6) + 1;
                        idx2 = (((mapItem & 0xff) >> 4) & 3) + 1;

                        tile = (mapItem & 0xf) - 1;

                        if(tile >= 0){
                            idx1 = landMapping[idx1];
                            idx2 = landMapping[idx2];
                        }

                        if(tile < 0){
                            miniMapImageSet = mapImageSet[baseTile];
                            miniMapArray[i * miniMapWidth + j] = miniMapImageSet.getMiniMapColor(0) | miniMapAlpha;
                        }else if(idx1 == idx2){
                            miniMapImageSet = mapImageSet[idx1];
                            miniMapArray[i * miniMapWidth + j] = miniMapImageSet.getMiniMapColor(tile) | miniMapAlpha;
                        }else{
                            miniMapImageSet = mapImageSet[idx2];
                            miniMapArray[i * miniMapWidth + j] = miniMapImageSet.getMiniMapColor(tile) | miniMapAlpha;
                        }
                    }catch(RuntimeException e){
                        e.printStackTrace();
                    }
                }else if(_mapType == MAP_PRECISION){
                    miniMapImageSet = getImageSet("stage.p");
                    if(tileWidth > tileHeight){
                        miniMapArray[i * miniMapWidth + j] = miniMapImageSet.getMiniMapColor(map[i][j / 2] & 0xff) | miniMapAlpha;
                        j++;
                        miniMapArray[i * miniMapWidth + j] = miniMapImageSet.getMiniMapColor(map[i][j / 2] & 0xff) | miniMapAlpha;
                    }
                }
            }
        }

        //#if (ModelID == NK-60-1) || (ModelID == NK-60-2)
        //# //miniMapImage = DirectUtils.createImage(miniMapWidth, miniMapHeight, 0);
        //# //Graphics g = miniMapImage.getGraphics();
        //# //DirectGraphics dg = DirectUtils.getDirectGraphics(g);
        //# //dg.drawPixels(miniMapArray, true, 0, miniMapWidth, 0, 0, miniMapWidth, miniMapHeight, 0, DirectGraphics.TYPE_INT_8888_ARGB);
        //#else
        miniMapImage = Image.createRGBImage(miniMapArray, miniMapWidth, miniMapHeight, true);
        //#endif

        //#if (ModelID == NK-60-1) || (ModelID == NK-60-2)
        //# int width = miniMapWidth > MINIMAP_MAX_WIDTH? MINIMAP_MAX_WIDTH:miniMapWidth;
        //# miniMapPosX = viewWidth - width - 1;
        //#else
        //miniMapPosX = viewWidth - miniMapImage.getWidth() - 1;

        int width = miniMapImage.getWidth() > MINIMAP_MAX_WIDTH? MINIMAP_MAX_WIDTH: miniMapImage.getWidth();
        miniMapPosX = viewWidth - width - 1;

        //#endif
        miniMapPosY = 1;
        //        miniMapPosY = GameState.CHAR_HEIGHT + 2;

        //#if (ModelID == NK-60-1) || (ModelID == NK-60-2)
        //# miniMapArrayShort = new short[miniMapArray.length];

        //# for(int i = 0; i < miniMapArray.length; i++){
        //#     byte a, r, g, b;

        //#     a = (byte)((miniMapArray[i] >> 24) & 0xFF);
        //#     r = (byte)((miniMapArray[i] >> 16) & 0xFF);
        //#     g = (byte)((miniMapArray[i] >> 8) & 0xFF);
        //#     b = (byte)(miniMapArray[i] & 0xFF);

        //#     a = (byte)((a >> 4) & 0xF);
        //#     r = (byte)((r >> 4) & 0xF);
        //#     g = (byte)((g >> 4) & 0xF);
        //#     b = (byte)((b >> 4) & 0xF);

        //#     miniMapArrayShort[i] = (short)(((a << 12) | (r << 8) | (g << 4) | b) & 0xFFFF);
        //# }
        //#endif

    }

    public static void addEvent(GameEvent event){
        if(events.size() > 0){
            GameEvent e;
            int pri = event.getPriority();

            for(int i = 0; i < events.size(); i++){
                e = (GameEvent)events.elementAt(i);
                if(e.getType() == GameEvent.EVENT_BATTLE && event.getType() == GameEvent.EVENT_BATTLE){
                    return;
                }
              //#if Revision == JIANGSUNCMCC
                //#if Directory == NK-BigScreen
                if(e.getType() == GameEvent.EVENT_PAUSE){
                    return;
                }
                //#endif
                //#endif
                if(e.getPriority() < pri){
                    removeEvent(e);
                }
            }
        }

        events.addElement(event);
    }

    public static boolean hasEventDoing(GameEvent event){
        boolean result = false;

        if(events.size() > 0){
            GameEvent e;
            int pri = event.getPriority();

            for(int i = 0; i < events.size(); i++){
                e = (GameEvent)events.elementAt(i);

                if(e.getPriority() >= pri){
                    result = true;

                    break;
                }
            }
        }

        return result;
    }

    public static void removeEvent(GameEvent event){
        events.removeElement(event);
        GameEvent.eventRemove(event);
    }

    public static GameEvent getEvent(int eventType){
        for(int i = 0; i < events.size(); i++){
            GameEvent e = (GameEvent)events.elementAt(i);
            if(e.getType() == eventType){
                return e;
            }
        }
        return null;
    }

    public static void gotoMap(short mapId, short x, short y, boolean isMapXY){
        GameEvent event = new GameEvent(GameEvent.EVENT_GOTO_MAP, 4, 0);
        event.idata[0] = mapId;
        event.idata[1] = x;
        event.idata[2] = y;
        event.idata[3] = isMapXY? 1: 0;
        addEvent(event);
    }

    /*---------- Map Functions End ----------*/

    /*---------- Net Functions Begin----------*/
    private static MonsterSprite np = new MonsterSprite(MonsterSprite.TYPE_NETPLAYER);

    //    public static String info = null;

    public static void cycleSegment(UWAPSegment segment){
        switch(segment.type){
            case GameState.CONN_GOTO:
                ByteArrayInputStream bis = null;

                short id = segment.readShort(); //关卡ID
                short type = segment.readShort(); //下载类型
                byte[] data = null;

                try{
                    switch(type){
                        case GameState.GET_FILE_STAGE:

                            //info = "get state";
                            data = segment.readBytes();
                            //info = "read data";
                            byte[] taskData = segment.readBytes();
                            initVM(taskData);

                            short[] finTasks = segment.readShorts();
                            short[] unfinTaks = segment.readShorts();
                            String[] unfinName = segment.readStrings();

                            GameState.serverTip = segment.readString();

                            for(int i = 0; i < finTasks.length; i++){
                                Integer tmp = new Integer(finTasks[i]);
                                finishedTask.put(tmp, tmp);
                            }

                            for(int i = 0; i < unfinTaks.length; i++){
                                Integer tmp = new Integer(unfinTaks[i]);
                                unFinishedTask.put(tmp, unfinName[i]);
                            }

                            bis = new ByteArrayInputStream(data);

                            initPackageAndStage(bis);
                            //info = "init package";

                            _mapId = (byte)(gameState.loadMapId & 0xF);

                            currMapId = gameState.loadMapId;

                            //info = "loading map";
                            loadMap(_mapId, gameState.startX, gameState.startY, gameState.isMapXY);
                            //info = "loadmap finish";

                            if(teamMode){
                                MonsterSprite me = World.findTeamMember(World.player.playerID);

                                if(me.alertRange != World.currMapId){
                                    World.updateTeamMemberInfo(me, World.currMapId, World.player.x, World.player.y);
                                }
                            }

                            World.setGameState(null);
                            _gtvm.start();

                            GameState.gameIsOk = true;
                            GameState.logouting = false;

                            //#if (Revision == PIP) || (Revision == SOHU)  || (Revision == DOWNJOY) || (Revision == JIANGSUN)
                            if(GameState.pushString == null){
                                GameState state = new GameState(GameState.STATE_OPENURL);
                                state._message = GameState.pushUrl;
                                if(state.thread == null){
                                    state.thread = new Thread(state);
                                    state.thread.start();
                                }
                            }
                            //#endif

                            break;
                        case GameState.GET_FILE_MGROUP:
                        case GameState.GET_FILE_NPC: {
                            data = segment.readBytes(); //Npc .p file
                            byte[] pdata = data;

                            //#if ModelID == NK-60-1
                            //# Image ptmp = Image.createImage(pdata, 0, pdata.length);
                            //#else
                            bis = new ByteArrayInputStream(data);
                            Image ptmp = Image.createImage(bis);
                            //#endif

                            data = segment.readBytes(); //Npc .s file
                            byte[] sdata = data;
                            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
                            ImageSet stmp = ImageSet.createImageSet(ptmp, dis, true);
                            //#debug
                            World.log("receive segement " + segment.serial, true);
                            if(type == GameState.TYPE_MONSTERICON){
                                replaceMGroupImageSet(id, stmp, pdata, sdata);
                            }else{
                                replaceNpcImageSet(id, stmp, pdata, sdata);
                            }
                            break;
                        }
                        case GameState.GET_FILE_MONSTER: {
                            data = segment.readBytes(); //Npc .p file
                            byte[] pdata = data;

                            //#if ModelID == NK-60-1
                            //# Image ptmp = Image.createImage(pdata, 0, pdata.length);
                            //#else
                            bis = new ByteArrayInputStream(data);
                            Image ptmp = Image.createImage(bis);
                            //#endif

                            data = segment.readBytes(); //Npc .s file
                            byte[] sdata = data;
                            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
                            ImageSet stmp = ImageSet.createImageSet(ptmp, dis, true);

                            if(instance.armySprite != null){
                                for(int i = 0; i < instance.armySprite.length; i++){
                                    if(instance.armySprite[i] == null)
                                        continue;
                                    if(instance.armySprite[i].imageID == id){
                                        instance.armySprite[i].imageSet = stmp;
                                    }
                                }
                                //#if CacheImage == TRUE
                                addImageSet(GameState.TYPE_MONSTER, id, stmp, pdata, sdata);
                                //#endif
                            }
                            break;
                        }
                        case GameState.GET_FILE_TASK:
                            data = segment.readBytes(); //etf
                            bis = new ByteArrayInputStream(data);
                            ETFFile etfFile = ETFFile.load(bis);
                            //#debug
                            System.out.println("add task : " + etfFile.taskID);

                            if(!_gtvm.hasTask(etfFile.taskID, true)){
                                _gtvm.addTask(etfFile);
                            }

                            if(etfFile.taskID < GTVM.localTaskIdStart){
                                showMessage("接受任务:" + etfFile.taskName, (byte)3);
                            }else{
                                if(etfFile.taskID == TASK_ID_PET_TRADE){
                                    createTaskUIEvent(GameEvent.EVENT_PET_TRADE, 0, "");
                                }else{
                                    saveTaskToRMS(etfFile.taskID, data);
                                }
                            }

                            break;

                    }
                }catch(Throwable e){
                    //#debug
                    e.printStackTrace();
                }finally{
                    if(bis != null){
                        try{
                            bis.close();
                        }catch(Exception e){
                            //#debug
                            e.printStackTrace();
                        }
                    }
                }

                break;
            case GameState.CONN_REFRESH:
                //刷新怪物\NPC\矿点
                int count = segment.readShort();

                for(int i = 0; i < count; i++){
                    boolean visible = segment.readByte() == 1;
                    int refreshId = segment.readInt();
                    byte refreshType = (byte)((refreshId >> 13) & 0x07);
                    int point = segment.readInt();
                    int x = (point >> 16) & 0xffff;
                    int y = point & 0xffff;

                    switch(refreshType){
                        case REFRESH_TYPE_NPC:
                            //#debug
                            World.log("refresh npc :" + (refreshId & 0x1fff) + " , " + refreshId + " , " + visible, true);

                            MonsterSprite npc = getUnit(refreshId);

                            if(npc != null){
                                npc.visible = visible;
                            }

                            if(x > 0 && y > 0 && npc != null){
                                npc.rx = x / World.tileWidth;
                                npc.ry = y / World.tileHeight;
                            }

                            break;
                        case REFRESH_TYPE_MONSTER:
                            //#debug
                            World.log("refresh Monster :" + (refreshId & 0x1fff) + " , " + refreshId + " , " + visible, true);

                            MonsterSprite mon = null;

                            for(int j = 0; j < monsters.length; j++){
                                if(monsters[j].id == refreshId){
                                    mon = monsters[j];

                                    break;
                                }
                            }

                            if(x > 0 && y > 0 && mon != null){
                                mon.rx = x;
                                mon.ry = y;
                            }

                            if(mon != null && mon.serverRefresh){
                                mon.visible = visible;
                            }else if(mon != null && visible){
                                monsterRefreshPool.put(new Integer(mon.id), new Integer(-2));
                            }

                            break;
                        case REFRESH_TYPE_RESOURCE:
                            //#debug
                            World.log("refresh Resource :" + (refreshId & 0x1fff) + " , " + refreshId + " , " + visible, true);

                            for(int j = 0; j < mapResource.length; j++){
                                if(mapResource[j][0] == refreshId){
                                    mapResource[j][6] = visible? 1: 0;

                                    if(x > 0 && y > 0){
                                        mapResource[j][1] = x / World.tileWidth;
                                        mapResource[j][2] = y / World.tileHeight;
                                    }
                                }
                            }

                            break;
                    }
                }

                break;

            case GameState.CONN_SEND_POSITION:
                try{
                    np.loadNetPlayer(segment);
                    np.teamRole = MonsterSprite.TEAM_ROLE_NONE;
                    np.followMode = false;
                }catch(IOException e){
                    //#debug
                    e.printStackTrace();
                }

                addNetPlayer(np);

                //#debug
                System.out.println("net player size : " + netPlayers.size() + " , " + netPlayersVector.size());

                break;
            case GameState.CONN_BATTLE_ROUND_END:
            case GameState.CONN_PK_ROUND_END:
                if(World.chat_input_doing){
                    World.instance.saveNetChatInputData();
                    World.setGameState(null);
                }

                byte sycnType = segment.readByte();

                if(sycnType == 1){
                    segment.readInt();
                    syncSegment = segment;
                }else if(nowBattle == segment.readInt()){
                    processRoundEnd(segment, false);
                }

                break;
            case GameState.CONN_BATTLE_ABORT:
                battleState = BATTLESTATE_GETBATTLEMOVIE;
                battleMoviePointer = 0;
                instance.battleMovie.removeAllElements();
                battleOver = 3;

                showMessage("战斗异常......", (byte)0);

                break;
        }
    }

    public static void processRoundEnd(UWAPSegment segment, boolean specialSync){
        battleBout = segment.readShort();

        if(!specialSync){
            int n = segment.readByte();
            instance.battleMovie = new Vector(n);

            for(int i = 0; i < n; i++){
                instance.battleMovie.addElement(segment.readInts());
            }
        }

        int pcount = 0;
        for(int i = 0; i < instance.playerSprite.length; i++){
            if(instance.playerSprite[i] != null)
                pcount++;
        }

        for(int i = 0; i < instance.armySprite.length; i++){
            if(instance.armySprite[i] != null)
                pcount++;
        }

        serverSyncIndex = new int[pcount];
        serverSyncHp = new int[pcount];
        serverSyncMp = new int[pcount];
        serverSyncStatus = new int[pcount];
        serverSyncCanAction = new boolean[pcount];
        serverSyncItemUse = new int[pcount];
        serverSyncSkillName = new String[pcount];

        serverSyncPetHp = new int[pcount];
        serverSyncPetMp = new int[pcount];
        serverSyncPeteStatus = new int[pcount];
        serverSyncPetCanAction = new boolean[pcount];
        serverSyncPetSkillName = new String[pcount];

        for(int i = 0; i < pcount; i++){
            byte index = segment.readByte();
            int userState = segment.readInt();
            int rhp = segment.readInt();
            int rmp = segment.readInt();
            boolean canAction = segment.readBoolean();
            int itemUse = segment.readInt();
            serverSyncIndex[i] = index;
            serverSyncHp[i] = rhp;
            serverSyncMp[i] = rmp;
            serverSyncStatus[i] = userState;
            serverSyncCanAction[i] = canAction;
            serverSyncItemUse[i] = itemUse;
            serverSyncSkillName[i] = segment.readString();

            int petHp = segment.readInt();
            int petMp = segment.readInt();
            int petStatus = segment.readInt();
            boolean petCanAction = segment.readBoolean();

            serverSyncPetHp[i] = petHp;
            serverSyncPetMp[i] = petMp;
            serverSyncPeteStatus[i] = petStatus;
            serverSyncPetCanAction[i] = petCanAction;
            serverSyncPetSkillName[i] = segment.readString();
        }

        if(!specialSync){
            battleState = BATTLESTATE_GETBATTLEMOVIE;
            battleOver = segment.readByte();
        }

        syncWithServer(specialSync);

        if(!specialSync){
            if(battleOver == 1 && !pkBattle){
                battleMonsterId = segment.readInt();
            }
        }

        player.skillId = Skill.SKILL_NOT_READY;
        player.target = null;
        player.targetIndex = -1;
        player.targetType = 0;
        player.usedMp = 0;

        if(player.petCurrent != null){
            player.petCurrent.skillId = Skill.SKILL_NOT_READY;
            player.petCurrent.target = null;
            player.petCurrent.targetIndex = -1;
            player.petCurrent.targetType = 0;
            player.petCurrent.usedMp = 0;
        }

        if(!specialSync){
            //#debug
            instance.printBattleMovie(instance.playerSprite, instance.armySprite, instance.playerPet, instance.armyPet, instance.battleMovie, battleBout, battleOver != 0);
        }
    }

    public static void syncWithServer(boolean specialSync){
        if(serverSyncIndex != null){
            for(int i = 0; i < serverSyncIndex.length; i++){
                BattleSprite s = null;
                PetSprite pets = null;
                int index = serverSyncIndex[i];

                if(index > 0){
                    s = instance.playerSprite[index - 1];
                    pets = instance.playerPet[index - 1];
                }else if(index < 0){
                    s = instance.armySprite[(-index) - 1];
                    pets = instance.armyPet[(-index) - 1];
                }

                s.hp = serverSyncHp[i];
                s.mp = serverSyncMp[i];
                s.debufStatus = serverSyncStatus[i] & 0xFF;
                s.useSkillName = serverSyncSkillName[i];

                if(pets != null){
                    pets.hp = serverSyncPetHp[i];
                    pets.mp = serverSyncPetMp[i];
                    pets.debufStatus = serverSyncPeteStatus[i] & 0xFF;
                    pets.useSkillName = serverSyncPetSkillName[i];
                }

                if(specialSync){
                    s.hpShow = s.hp;
                    s.mpShow = s.mp;

                    if(s.debufStatus != Skill.STATUS_NORMAL){
                        s.debufID = (byte)s.debufStatus;
                    }

                    if(pets != null){
                        pets.hpShow = pets.hp;
                        pets.mpShow = pets.mp;

                        if(pets.debufStatus != Skill.STATUS_NORMAL){
                            pets.debufID = (byte)pets.debufStatus;
                        }
                    }

                    if(s.debufStatus == Skill.STATUS_DIE){
                        s.setDie();
                        s.showHp = false;
                    }

                    if(pets != null && pets.debufStatus == Skill.STATUS_DIE){
                        pets.setDie();
                        pets.showHp = false;
                    }
                }

                if(s == player){
                    if(serverSyncItemUse[i] != 0){
                        int itemid = serverSyncItemUse[i] >> 16;
                        short count = (short)(serverSyncItemUse[i] & 0xffff);

                        GameItem item = new GameItem(GameItem.TYPE_BASIC);
                        item.itemId = itemid;
                        item.count = (short)(-count);
                        player.addItemToBag(item);
                    }

                    World.battleSkillSeal = (byte)((serverSyncStatus[i] >> 16) & 0xFF);
                    World.battlePetSkillSeal = (byte)((serverSyncPeteStatus[i] >> 16) & 0xFF);
                    battleCanAction = serverSyncCanAction[i];
                    battlePetCanAction = serverSyncPetCanAction[i];
                }
            }

            serverSyncIndex = null;
        }
    }

    public static void removeNetPlayerData(int playerId){
        MonsterSprite netPlayer = (MonsterSprite)netPlayers.remove(new Integer(playerId));
        netPlayersVector.removeElement(netPlayer);
    }

    public static void addNetPlayer(MonsterSprite np){
        if(np.id == World.player.playerID){
            return;
        }

        MonsterSprite netPlayer = findInNetPlayers(np.id);
        MonsterSprite teamMember = null;

        if(teamMode){
            teamMember = findTeamMember(np.id);
        }

        if(teamMember != null){
            if(netPlayer != null){
                if(np.needWholeUpdate){
                    try{
                        netPlayer.loadNetPlayer(np, true);
                    }catch(IOException e){
                        //#debug
                        e.printStackTrace();
                    }
                }else{
                    netPlayer.alertRange = np.alertRange;
                    netPlayer.rx = np.rx;
                    netPlayer.ry = np.ry;
                    netPlayer.wpList = null;
                }
            }

            if(np.needWholeUpdate){
                try{
                    teamMember.loadNetPlayer(np, false);
                }catch(IOException e){
                    //#debug
                    e.printStackTrace();
                }
            }

            if(netPlayer == null){
                addNetPlayerData(teamMember, true);
            }

            if(np.alertRange == currMapId){
                if(teamMember.alertRange != currMapId){
                    updateTeamMemberInfo(teamMember, np.alertRange, np.rx, np.ry);
                }

                if(teamMember.teamRole == MonsterSprite.TEAM_ROLE_LEADER || !teamMember.followMode){
                    teamMember.addWayPoint((short)np.rx, (short)np.ry);
                }
            }else{
                if(np.alertRange != -1){
                    if(teamStatus == TEAM_STATUS_FOLLOW){
                        if(teamMember.teamRole == MonsterSprite.TEAM_ROLE_LEADER){
                            teamMember.wpList = null;
                            moveTeamFollowToMap(np.alertRange, np.rx, np.ry);
                            gotoMap(np.alertRange, (short)np.rx, (short)np.ry, false);
                            World.instance.saveNetChatInputData();
                        }
                    }
                }

                updateTeamMemberInfo(teamMember, np.alertRange, np.rx, np.ry);
            }
        }else if(np.alertRange == currMapId){
            if(netPlayer != null){
                if(np.needWholeUpdate){
                    try{
                        netPlayer.loadNetPlayer(np, false);
                    }catch(IOException e){
                        //#debug
                        e.printStackTrace();
                    }
                }

                netPlayer.addWayPoint((short)np.rx, (short)np.ry);
            }else{
                if(netPlayers.size() < NETPLAYERS_LIMIT + 3){
                    addNetPlayerData(np, false);
                }
            }
        }else{
            removeNetPlayerData(np.id);
        }
    }

    public static void addNetPlayerData(MonsterSprite netplayer, boolean updateTeamInfo){
        MonsterSprite newNetPlayer = new MonsterSprite(MonsterSprite.TYPE_NETPLAYER);

        try{
            newNetPlayer.loadNetPlayer(netplayer, true);
        }catch(IOException e){
            //#debug
            e.printStackTrace();
        }

        if(updateTeamInfo){
            newNetPlayer.teamRole = netplayer.teamRole;
            newNetPlayer.followMode = netplayer.followMode;
        }

        netPlayers.put(new Integer(netplayer.id), newNetPlayer);
        netPlayersVector.addElement(newNetPlayer);
    }

    public static ETFFile loadTaskFromRMS(short id){
        ETFFile result = null;

        Integer taskId = new Integer(id);
        Integer taskIndex = (Integer)taskUICache.get(taskId);

        if(taskIndex != null){
            byte[] data = getData(TASKUI_DATA, (byte)taskIndex.intValue());

            try{
                result = ETFFile.load(new DataInputStream(new ByteArrayInputStream(data)));
            }catch(Exception e){
                result = null;
                //#debug
                e.printStackTrace();
            }
        }

        return result;
    }

    public static void saveTaskToRMS(short taskId, byte[] taskData){
        if(taskId < GTVM.localTaskIdStart || taskId >= GTVM.serverPushTaskIdStart){
            return;
        }

        short maxIndex = (short)(getTaskUIMaxIndex() + 1);

        saveData(TASKUI_DATA, taskData, (byte)maxIndex);
        taskUICache.put(new Integer(taskId), new Integer(maxIndex));

        byte[] indexData = getTaskUIIndexBytes();
        saveData(TASKUI_DATA, indexData, (byte)0);
    }

    public static int requestDownloadTask(short id){
        ETFFile taskData = loadTaskFromRMS(id);

        if(taskData != null){
            _gtvm.addTask(taskData);

            return -1;
        }

        UWAPSegment segment = new UWAPSegment(GameState.CONN_GET_FILE);

        try{
            segment.writeString(GameState.getModel()); //客户端机型
            segment.writeShort((short)-1); //用户等级 用来判断下载的关卡任务。如果客户端还不知道用户级别，可填-1。
            segment.writeShort((short)2); //下载类型 1 为关卡2为任务 3 怪物组 4 npc 5 怪物
            segment.writeShort((short)id); //下载序号 下载关卡时为关卡号，下载任务时为任务ID
            segment.flush();
            GameState.sendRequest(segment);

            return segment.serial;
        }catch(IOException ex){
        }

        return -1;
    }

    public static int requestTestNetSpeed(){ //测试网速
        long st = System.currentTimeMillis();
        byte[] stb = new byte[8];
        stb[0] = (byte)(st >> 56);
        stb[1] = (byte)(st >> 48);
        stb[2] = (byte)(st >> 40);
        stb[3] = (byte)(st >> 32);
        stb[4] = (byte)(st >> 24);
        stb[5] = (byte)(st >> 16);
        stb[6] = (byte)(st >> 8);
        stb[7] = (byte)st;
        UWAPSegment segment = new UWAPSegment(GameState.CONN_NETSPEED_TEST);
        try{
            segment.writeBytes(stb);
            segment.flush();
            GameState.sendRequest(segment);
            //#debug
            System.out.println("test net speed send time : " + st);
            return segment.serial;
        }catch(IOException ex){
        }
        return -1;
    }

    public static int requestSendPosition(){
        sendPositionTime = 0;
        player.lastPositionX = player.x;
        player.lastPositionY = player.y;
        player.lastMapId = currMapId;

        UWAPSegment segment = new UWAPSegment(GameState.CONN_SEND_POSITION);

        try{
            segment.writeShort((short)currMapId); //关卡ID
            segment.writeShort((short)player.getX()); //X坐标
            segment.writeShort((short)player.getY()); //Y坐标
            segment.writeBoolean((boolean)true); //是否开启网络消息
            segment.flush();
            GameState.sendRequest(segment);
            //#debug
            System.out.println("sned position : " + currMapId + " , " + player.getX() + " , " + player.getY());

            return segment.serial;
        }catch(IOException ex){
        }

        return -1;
    }

    public int requestSendBattleResult(byte result){
        UWAPSegment segment = new UWAPSegment(GameState.CONN_BATTLE_RESULT);

        try{
            segment.writeByte(result);

            if(player.testDie()){
                segment.writeInt(-1);
            }else{
                segment.writeInt(player.hp);
            }

            segment.writeInt(player.mp);

            if(player.petCurrent != null){
                if(player.petCurrent.testDie()){
                    segment.writeInt(-1);
                }else{
                    segment.writeInt(player.petCurrent.hp);
                }

                segment.writeInt(player.petCurrent.mp);
            }else{
                segment.writeInt(0);
                segment.writeInt(0);
            }

            segment.writeInt(monsters[battleMonsterIndex].id);

            byte count = 0;

            for(int i = 0; i < armySprite.length; i++){
                if(armySprite[i] != null)
                    count++;
            }

            segment.writeByte(count);

            for(int i = 0; i < armySprite.length; i++){
                if(armySprite[i] != null){
                    segment.writeByte(armySprite[i].id);

                    if(armySprite[i].testDie()){
                        segment.writeByte((byte)1);
                    }else if(armySprite[i].testRunAway()){
                        segment.writeByte((byte)2);
                    }else if(armySprite[i].testCatched()){
                        segment.writeByte((byte)3);
                    }else{
                        segment.writeByte((byte)2);
                    }
                }
            }

            long parm = 0;

            parm |= (World.player.attributes[BattleSprite.ATTR_STR] & 0xFFFF);
            parm <<= 16;
            parm |= (World.player.attributes[BattleSprite.ATTR_AGI] & 0xFFFF);
            parm <<= 16;
            parm |= (World.player.attributes[BattleSprite.ATTR_VIT] & 0xFFFF);
            parm <<= 16;
            parm |= World.player.attributes[BattleSprite.ATTR_INT] & 0xFFFF;

            segment.writeLong(parm);

            segment.flush();
            GameState.sendRequest(segment);

            return segment.serial;
        }catch(IOException e){
            //#debug
            e.printStackTrace();
        }
        return -1;
    }

    private static short checkCRC(byte[] byteArray, short inCRC, int start, int length){
        short tmp;
        int i;

        if(byteArray != null){
            for(i = start; i < start + length - 2; i += 2){
                tmp = (short)(((byteArray[i] & 0xFF) << 8) | (byteArray[i + 1] & 0xFF));

                inCRC ^= tmp;
            }

            if(i == start + length - 2){
                tmp = (short)(((byteArray[i] & 0xFF) << 8) | (byteArray[i + 1] & 0xFF));
            }else{
                tmp = (short)(((0 & 0xFF) << 8) | (byteArray[i] & 0xFF));
            }

            inCRC ^= tmp;
        }

        return inCRC;
    }

    private static long getNumber(byte[] buf, int off, int len){
        long l = 0;

        for(int i = 0; i < len; i++){
            l <<= 8;
            l += ((int)buf[off + i]) & 0xff;
        }

        return l;
    }

    /*---------- Net Functions End ----------*/

    /*---------- Cache Functions Begin----------*/

    //#if CacheImage == TRUE
    public static CacheImageSet addImageSet(int type, int imageID, ImageSet data, byte[] pdata, byte[] sdata){
        cacheChanged = true;
        CacheImageSet find = searchCache(type, imageID);

        if(find != null){
            find.hitCount++;

            return find;
        }

        if(cachePointer == CACHE_LENGTH - 1){
            caches[cachePointer] = null;
            cachePointer--;
            caches[cachePointer] = null;
            cachePointer--;
        }

        cachePointer++;
        CacheImageSet cache = new CacheImageSet();
        cache.imageID = imageID;
        cache.type = type;
        cache.data = data;
        cache.hitCount = 1;
        cache.pdata = pdata;
        cache.sdata = sdata;

        caches[cachePointer] = cache;

        return cache;
    }

    public static CacheImageSet searchCache(int type, int imageID){
        for(int i = 0; i < cachePointer; i++){
            if(caches[i].imageID == imageID && caches[i].type == type){
                return caches[i];
            }
        }

        return null;
    }

    public static void orderCache(){
        if(!cacheChanged){
            return;
        }
        //#debug
        log("order cache", true);

        try{
            for(int i = 0; i <= cachePointer; i++){
                for(int j = i + 1; j <= cachePointer; j++){
                    if(caches[j].hitCount > caches[i].hitCount){
                        CacheImageSet tmp = caches[j];
                        caches[j] = caches[i];
                        caches[i] = tmp;
                    }
                }
            }
        }catch(NullPointerException e){
            //#debug
            e.printStackTrace();
        }

        cacheChanged = false;
    }

    public static void clearHitCount(){
        for(int i = 0; i < CACHE_LENGTH; i++){
            caches[i].hitCount = 0;
        }
    }

    public static void clearImageSetCache(){
        for(int i = 0; i < CACHE_LENGTH; i++){
            caches[i] = null;
        }

        cachePointer = -1;
        cacheChanged = false;
    }

    public static byte[] convertCacheToByteArray(){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        try{
            dos.writeByte(cachePointer);

            for(int i = 0; i <= cachePointer; i++){
                caches[i].save(dos);
            }

            return bos.toByteArray();
        }catch(IOException e){
            //#debug
            e.printStackTrace();
        }finally{
            try{
                dos.close();
            }catch(IOException e){
            }
        }

        return null;
    }

    public static void parseCacheFromByteArray(byte[] data){
        if(data == null){
            return;
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bis);

        clearImageSetCache();
        try{
            cachePointer = dis.readByte();

            for(int i = 0; i <= cachePointer; i++){
                caches[i] = new CacheImageSet();
                caches[i].load(dis);
            }
        }catch(IOException e){
            //#debug
            e.printStackTrace();
        }finally{
            try{
                dis.close();
            }catch(Exception e){
            }
        }
    }

    //#endif
    /*---------- Cache Functions End ----------*/

    /*---------- RMS Functions Begin----------*/

    //#if CacheImage == TRUE
    public static boolean saveCacheToRMS(){
        byte[] data = convertCacheToByteArray();
        return writeToRMS(CACHE_DB, data);
    }

    public static void loadCacheFromRMS(){
        parseCacheFromByteArray(loadFromRMS(CACHE_DB));
    }

    //#endif

    public static boolean writeToRMS(String dbName, byte[] data){
        RecordStore rs = null;

        try{
            rs = RecordStore.openRecordStore(dbName, true);
            RecordEnumeration re = rs.enumerateRecords(null, null, false);
            int recordId = -1;

            while(re.hasNextElement()){
                int id = re.nextRecordId();
                byte[] record = rs.getRecord(id);

                if(record.length > 0){
                    recordId = id;

                    break;
                }
            }
            re.destroy();

            if(recordId == -1){
                rs.addRecord(data, 0, data.length);
            }else{
                rs.setRecord(recordId, data, 0, data.length);
            }
        }catch(Exception e){
            //#debug
            e.printStackTrace();

            return false;
        }finally{
            if(rs != null){
                try{
                    rs.closeRecordStore();
                }catch(Exception e){
                }
            }
        }

        return true;
    }

    public static byte[] loadFromRMS(String dbName){
        RecordStore rs = null;
        byte[] ret = null;

        try{
            rs = RecordStore.openRecordStore(dbName, false);
            RecordEnumeration re = rs.enumerateRecords(null, null, false);

            while(re.hasNextElement()){
                byte[] record = re.nextRecord();

                if(record.length > 0){
                    ret = record;

                    break;
                }
            }

            re.destroy();

            return ret;
        }catch(Exception e){
            return null;
        }finally{
            if(rs != null){
                try{
                    rs.closeRecordStore();
                }catch(Exception e){
                }
            }
        }
    }

    public static boolean saveData(String rmsName, byte[] data, byte index){
        RecordStore rs = null;

        try{
            rs = RecordStore.openRecordStore(rmsName, true);
            RecordEnumeration re = rs.enumerateRecords(null, null, false);
            int recordId = -1;

            while(re.hasNextElement()){
                int id = re.nextRecordId();
                byte[] record = rs.getRecord(id);

                if(record.length > 0 && record[0] == index){
                    recordId = id;

                    break;
                }
            }

            re.destroy();
            byte[] newdata = new byte[data.length + 1];
            System.arraycopy(data, 0, newdata, 1, data.length);
            newdata[0] = index;

            if(recordId == -1){
                rs.addRecord(newdata, 0, newdata.length);
            }else{
                rs.setRecord(recordId, newdata, 0, newdata.length);
            }

            return true;
        }catch(Exception e){
            return false;
        }finally{
            if(rs != null){
                try{
                    rs.closeRecordStore();
                }catch(Exception e){
                }
            }
        }
    }

    public static byte[] getData(String rmsName, byte index){
        RecordStore rs = null;
        byte[] ret = null;

        try{
            rs = RecordStore.openRecordStore(rmsName, false);
            RecordEnumeration re = rs.enumerateRecords(null, null, false);

            while(re.hasNextElement()){
                byte[] record = re.nextRecord();

                if(record.length > 0 && record[0] == index){
                    ret = record;

                    break;
                }
            }

            re.destroy();

            if(ret != null){
                byte[] ret1 = new byte[ret.length - 1];
                System.arraycopy(ret, 1, ret1, 0, ret.length - 1);
                ret = ret1;
            }

            return ret;
        }catch(Exception e){
            return null;
        }finally{
            if(rs != null){
                try{
                    rs.closeRecordStore();
                }catch(Exception e){
                }
            }
        }
    }

    public static boolean deleteData(String rmsName, byte index){
        RecordStore rs = null;

        try{
            rs = RecordStore.openRecordStore(rmsName, true);
            RecordEnumeration re = rs.enumerateRecords(null, null, false);
            int recordId = -1;

            while(re.hasNextElement()){
                int id = re.nextRecordId();
                byte[] record = rs.getRecord(id);

                if(record.length > 0 && record[0] == index){
                    recordId = id;

                    break;
                }
            }

            re.destroy();

            if(recordId != -1){
                rs.deleteRecord(recordId);
            }

            return true;
        }catch(Exception e){
            return false;
        }finally{
            if(rs != null){
                try{
                    rs.closeRecordStore();
                }catch(Exception e){
                }
            }
        }
    }

    /*---------- RMS Functions End ----------*/

    /*---------- VM Functions Begin ----------*/

    public static MonsterSprite getUnit(int unitId){
        MonsterSprite npc = (MonsterSprite)npcIdTable.get(new Integer(unitId));

        if(npc != null){
            return npc;
        }else{
            return null;
        }
    }

    public static int[] getTouchUnitBox(MonsterSprite npc){
        int[] npcBox = npc.getCollisionBox();
        int step = Sprite.STEP;

        npcBox[0] -= step;
        npcBox[1] -= step;
        npcBox[2] += step * 2;
        npcBox[3] += step * 2;

        return npcBox;
    }

    public static void setNpcHint(int npcId, byte type){
        MonsterSprite npc = getUnit(npcId);

        if(npc != null){
            npc.setTaskHint(type);
        }
    }

    public static boolean touchNpc(int unitId){
        return touchingNpcId == unitId;
    }

    public static boolean touchUnit(int unitId){
        MonsterSprite npc = getUnit(unitId);

        if(npc == null || !npc.visible || npc.iconID == 0){
            return false;
        }

        int x1, y1, w1, h1, x2, y2, w2, h2;
        int[] npcBox = getTouchUnitBox(npc);
        int[] playerOldBox = player.getOldCollisionBox();

        x1 = npcBox[0];
        y1 = npcBox[1];
        w1 = npcBox[2];
        h1 = npcBox[3];

        x2 = playerOldBox[0];
        y2 = playerOldBox[1];
        w2 = playerOldBox[2];
        h2 = playerOldBox[3];

        if(rectIntersect(x1, y1, w1, h1, x2, y2, w2, h2)){
            return false;
        }else{
            int x3, y3, w3, h3;
            int[] playerBox = player.getCollisionBox();

            x3 = playerBox[0];
            y3 = playerBox[1];
            w3 = playerBox[2];
            h3 = playerBox[3];

            if(rectIntersect(x1, y1, w1, h1, x3, y3, w3, h3)){
                return true;
            }
        }

        return false;
    }

    public static void checkNPC(){
        if(yOrder == null){
            return;
        }

        short type, idx;

        for(int i = 0; i < yOrderCount; i += 4){
            type = yOrder[i];

            switch(type){
                case DRAW_ITEMS_NPC:
                    idx = yOrder[i + 1];
                    MonsterSprite npc = npcs[idx];

                    if(touchUnit(npc.id)){
                        showNpcConfirm(npc, "和" + npc.playerName + "对话?");
                    }

                    break;
            }
        }
    }

    public static void checkResource(){
        if(yOrder == null){
            return;
        }

        short type, idx;

        for(int i = 0; i < yOrderCount; i += 4){
            type = yOrder[i];

            switch(type){
                case DRAW_ITEMS_RESOURCE:
                    idx = yOrder[i + 1];

                    if(touchResource(idx)){
                        showResourceConfirm(idx);
                    }

                    break;
            }
        }
    }

    public static int getResourceImageID(int resType){
        switch(resType){
            case 5:
                return 0;
            case 3:
                return 1;
            case 4:
                return 2;
            case 7:
                return 3;
        }
        return 0;
    }

    public static int[] getTouchResourceBox(int ridx){
        int[] res = mapResource[ridx];
        int[] resBox = new int[4];

        int step = player.STEP;

        if(res[4] == 7){
            step *= 3;
        }

        resBox[0] = res[1] * tileWidth + resourceImage[getResourceImageID(res[4])].collision[(int)(tick / 5 % 2)][0];
        resBox[1] = (res[2] + 1) * tileHeight - resourceImage[getResourceImageID(res[4])].getHeight((int)(tick / 5 % 2)) + resourceImage[getResourceImageID(res[4])].collision[(int)(tick / 5 % 2)][1];
        resBox[2] = resourceImage[getResourceImageID(res[4])].collision[(int)(tick / 5 % 2)][2];
        resBox[3] = resourceImage[getResourceImageID(res[4])].collision[(int)(tick / 5 % 2)][3];

        resBox[0] -= step;
        resBox[1] -= step;
        resBox[2] += step * 2;
        resBox[3] += step * 2;

        return resBox;
    }

    public static boolean touchResource(int ridx){
        int x1, y1, w1, h1, x2, y2, w2, h2;

        int[] resBox = getTouchResourceBox(ridx);
        int[] playerOldBox = player.getOldCollisionBox();

        x1 = resBox[0];
        y1 = resBox[1];
        w1 = resBox[2];
        h1 = resBox[3];

        x2 = playerOldBox[0];
        y2 = playerOldBox[1];
        w2 = playerOldBox[2];
        h2 = playerOldBox[3];

        if(rectIntersect(x1, y1, w1, h1, x2, y2, w2, h2)){
            return false;
        }else{
            int x3, y3, w3, h3;
            int[] playerBox = player.getCollisionBox();

            x3 = playerBox[0];
            y3 = playerBox[1];
            w3 = playerBox[2];
            h3 = playerBox[3];

            if(rectIntersect(x1, y1, w1, h1, x3, y3, w3, h3)){
                return true;
            }
        }

        return false;
    }

    public static void showChat(int unitId, String msg, int block){
        player.setState(Sprite.STATE_IDLE);

        GameEvent e = new GameEvent(GameEvent.EVENT_CHAT, 3, 0);

        e.setBlockType((byte)block);
        e.idata[0] = 0;
        e.odata = new Object[2];

        if(unitId == (byte)0){
            e.odata[0] = player.imageSet;
            e.idata[1] = 9;
        }else{
            MonsterSprite npc = getUnit(unitId);

            if(npc != null){
                e.odata[0] = npc.imageSet;
                e.idata[1] = npc.frame;
            }else{
                e.odata[0] = null;
            }

            msg = npc.playerName + " : \n" + msg;

            int[] npcBox = getTouchUnitBox(npc);

            e.setCollisionBox(npcBox[0], npcBox[1], npcBox[2], npcBox[3]);
        }

        Vector vec = formatString(msg, MESSAGEBOX_WIDTH - GameState.BOX_MARGIN * 2 - GameState.EDGE_WIDTH * 2 - 30, GameState.font);
        Object[] obj = new Object[vec.size()];
        vec.copyInto(obj);
        e.odata[1] = obj;
        e.idata[2] = getFormatedStringLine(obj, 0, 3);

        addEvent(e);
    }

    public static int getFormatedStringLine(Object[] obj, int startLine, int line){
        int lines = 0;
        int lastLine = -1;
        int copyLines = -1;
        for(int i = startLine; i < obj.length; i++){
            Object[] strObj = (Object[])obj[i];
            int strLine = ((Integer)strObj[0]).intValue();
            if(lastLine == -1){
                lines = 1;
                lastLine = strLine;
            }else if(lastLine != strLine){
                lines++;
                lastLine = strLine;
            }
            if(lines == line + 1){
                copyLines = i - startLine;
                break;
            }
        }

        if(copyLines == -1 || (copyLines + startLine + 1) == obj.length){
            copyLines = obj.length - startLine;
        }
        return copyLines;
    }

    public static void showTaskConfirm(short taskId){
        player.setState(Sprite.STATE_IDLE);

        GameEvent e = new GameEvent(GameEvent.EVENT_TASK_CONFIRM, 1, 0);
        e.idata[0] = taskId;

        addEvent(e);

    }

    public static void addTaskAsUnFinish(int taskId, String taskName, boolean forceAdd){
        Integer tmpTaskId = new Integer(taskId);

        if(World.unFinishedTask.containsKey(tmpTaskId)){
            String tmpName = (String)World.unFinishedTask.get(tmpTaskId);

            if(tmpName == null || tmpName.trim().length() == 0){
                if(taskName == null){
                    World.unFinishedTask.put(tmpTaskId, "");
                }else{
                    World.unFinishedTask.put(tmpTaskId, taskName);
                }
            }
        }else if(forceAdd){
            if(taskName == null || taskName.trim().length() == 0){
                World.unFinishedTask.put(tmpTaskId, "");
            }else{
                World.unFinishedTask.put(tmpTaskId, taskName);
            }
        }

    }

    public static void showNpcConfirm(MonsterSprite npc, String msg){
        if(GameState.touchNpcInfo != null){
            return;
        }

        player.setState(Sprite.STATE_IDLE);

        GameEvent e = new GameEvent(GameEvent.EVENT_NPC_CONFIRM, 2, 0);
        e.idata[0] = npc.politics;
        e.idata[1] = npc.id;

        int[] npcBox = getTouchUnitBox(npc);

        e.setCollisionBox(npcBox[0], npcBox[1], npcBox[2], npcBox[3]);

        String newMsg = msg;

        if(npc.questionName != null){
            newMsg = npc.questionName;
        }

        String[] msgs = splitString(newMsg, MESSAGEBOX_WIDTH - GameState.BOX_MARGIN * 2 - GameState.EDGE_WIDTH * 2 - 30, GameState.font);
        e.sdata = msgs;

        switch(npc.politics){
            case NPC_TYPE_BATTLE_TEACHER:
            case NPC_TYPE_PRODUCT_TEACHER:
                e.odata = new String[1];
                e.odata[0] = npc.playerName.substring(0, 2) + "技能学习";

                break;
        }

        addEvent(e);
    }

    public static void showDieConfirm(int delay, int mapId, int posTileX, int posTileY, String msg){
        player.setState(Sprite.STATE_IDLE, true);
        player.wpList = null;

        GameEvent e = new GameEvent(GameEvent.EVENT_DIE_CONFIRM, 5, 1);
        e.idata[0] = delay;
        e.idata[1] = mapId;
        e.idata[2] = posTileX;
        e.idata[3] = posTileY;
        e.idata[4] = 0;
        e.sdata[0] = msg;
        addEvent(e);
    }

    public static void showResourceConfirm(int ridx){
        player.setState(Sprite.STATE_IDLE);

        GameEvent e = new GameEvent(GameEvent.EVENT_RESOURCE_CONFIRM, 4, 0);
        e.idata[0] = mapResource[ridx][0];
        e.idata[1] = mapResource[ridx][5];
        e.idata[2] = mapResource[ridx][7];

        int levelSub = Sprite.getSkillLevel(player.productSkill[mapResource[ridx][4]]) - mapResource[ridx][3] + 1;

        if(levelSub < 0)
            levelSub = 0;

        if(levelSub > 4)
            levelSub = 4;

        e.idata[3] = levelSub;

        int[] resBox = getTouchResourceBox(ridx);

        e.setCollisionBox(resBox[0], resBox[1], resBox[2], resBox[3]);

        String msg = (e.idata[3] == 0? "资源 ": "采集资源 ") + GameItem.resourceNames[e.idata[2] - GameItem.resourceBegin] + (e.idata[3] == 0? "": "？") + (e.idata[1] == 0? "": "(需要玩游戏)")
                        + (e.idata[3] == 0? "\n技能等级不够": "");

        String[] msgs = splitString(msg, MESSAGEBOX_WIDTH - GameState.BOX_MARGIN * 2 - GameState.EDGE_WIDTH * 2 - 30, GameState.font);
        e.sdata = msgs;

        addEvent(e);
    }

    /*---------- VM Functions End ----------*/

    /*---------- Tools Functions Begin----------*/

    public static ImageSet getImageSetFromLocal(String file){
        String pfile, sfile;

        if(file.endsWith(".p")){
            int idx = file.indexOf(".p");
            pfile = file.substring(0, idx);
            sfile = pfile + ".s";
            pfile = pfile + ".p";
        }else{
            pfile = file + ".p";
            sfile = file + ".s";
        }

        ImageSet tmp = null;

        //#if ResourceCache == TRUE
        tmp = (ImageSet)_localCache.get(pfile);
        if(tmp != null)
            return tmp;
        //#endif

        try{
            Image img = Image.createImage("/" + pfile);
            DataInputStream sfileStream = new DataInputStream(iTimesMIDlet.instance.getClass().getResourceAsStream("/" + sfile));

            tmp = ImageSet.createImageSet(img, sfileStream, true);

            //#if ResourceCache == TRUE
            _localCache.put(pfile, tmp);
            //#endif

        }catch(Exception e){
            //#debug
            e.printStackTrace();
        }

        return tmp;
    }

    public static ImageSet getImageSet(String file){
        //#if LoadAllImage == FALSE
        //# ImageSet pImgSet = (ImageSet)_imageCache.get(file);
        //# if(pImgSet == null){
        //# int idx = file.indexOf(".p");
        //# String sname = file.substring(0, idx);
        //# sname += ".s";
        //# byte[] pdata = (byte[])_packageCache.get(file);
        //# byte[] sdata = (byte[])_packageCache.get(sname);

        //# if(pdata == null || sdata == null){
        //#     return null;
        //# }

        //# DataInputStream sStream = new DataInputStream(new ByteArrayInputStream(sdata));

        //# try{
        //#     Image pimg = Image.createImage(pdata, 0, pdata.length);
        //#     pImgSet = ImageSet.createImageSet(pimg, sStream, true);

        //#     _imageCache.put(file, pImgSet);

        //#debug
        //#     System.out.println("load image: " + file);
        //# }catch(Throwable e){
        //#debug
        //#     e.printStackTrace();
        //# }finally{
        //#     if(sStream != null){
        //#         try{
        //#             sStream.close();
        //#         }catch(Exception e){
        //#         }
        //#     }
        //# }
        //# }

        //# return pImgSet;
        //#else
        return (ImageSet)_packageCache.get(file);
        //#endif
    }

    private static InputStream getPackageFileAsStream(String s){
        byte[] bytes = (byte[])_packageCache.get(s);
        return new ByteArrayInputStream(bytes);
    }

    public static void replaceNpcImageSet(short imageid, ImageSet set, byte[] pdata, byte[] sdata){
        for(int i = 0; i < npcs.length; i++){
            if(npcs[i].alertRange == imageid){
                npcs[i].imageSet = set;

                //#if CacheImage == TRUE
                if(npcs[i].type == NPC_TYPE_ITEMNPC){
                    addImageSet(GameState.TYPE_ITEMNPC, imageid, set, pdata, sdata);
                }else{
                    addImageSet(GameState.TYPE_NPC, imageid, set, pdata, sdata);
                }
                //#endif
            }
        }
    }

    public static void replaceMGroupImageSet(short imageid, ImageSet set, byte[] pdata, byte[] sdata){
        for(int i = 0; i < monsters.length; i++){
            if(monsters[i].iconID == imageid){
                monsters[i].imageSet = set;

                //#if CacheImage == TRUE
                addImageSet(GameState.TYPE_MONSTERICON, imageid, set, pdata, sdata);
                //#endif
            }
        }
    }

    public static ImageSet getSpriteImageSet(short spriteType, short imgid){
        //#if CacheImage == TRUE
        CacheImageSet is = searchCache(spriteType, imgid);

        if(is != null){
            return is.getImageSet();
        }
        //#endif
        if(spriteType >= 0 && spriteType < defaultImageSet.length){
            return defaultImageSet[spriteType];
        }else{
            return null;
        }
    }

    //#mdebug
    public static void log(String s, boolean newLine){
        if(s == null){
            System.out.println();
        }else{
            if(newLine){
                System.out.println(s);
            }else{
                System.out.print(s);
            }
        }
    }

    //#enddebug

    public static long sqrt(long x){
        long y = 0;
        long b = (~Long.MAX_VALUE) >>> 1;

        while(b > 0){
            if(x >= y + b){
                x -= y + b;
                y >>= 1;
                y += b;
            }else{
                y >>= 1;
            }

            b >>= 2;
        }

        return y;
    }

    public static void drawFormatedString(Graphics g, Vector strObj, Font font, int x, int y, int color){
        for(int i = 0; i < strObj.size(); i++){
            Object[] obj = (Object[])strObj.elementAt(i);
            drawFormatedString(g, obj, font, x, y, -1, color);
        }
    }

    public static void drawFormatedString(Graphics g, Object[] obj, Font font, int x, int y, int line, int color){
        int oline = 0;
        int fcolor = 0;
        String dstr;
        int xOffset = 0;
        g.setFont(font);
        oline = ((Integer)obj[0]).intValue();
        if(obj[1] == null){
            fcolor = color;
        }else{
            fcolor = ((Integer)obj[1]).intValue();
        }
        dstr = (String)obj[2];
        xOffset = ((Integer)obj[3]).intValue();
        g.setColor(fcolor);
        g.drawString(dstr, x + xOffset, y + (line == -1? oline: line) * GameState.CHAR_HEIGHT, Graphics.TOP | Graphics.LEFT);
    }

    public static Vector formatString(String s, int width, Font font){
        Vector ret = new Vector();

        int start = 0, end = 0;
        int length = s.length();
        int curWidth = 0;
        char ch;
        int sPos = 0;
        Integer currColor = null;
        int line = 0;
        int xOffset = 0;
        String currStr = "";

        //[0] = line [1] = color [2] = String [3] = x offset
        Object[] strObj = null;

        for(int pos = 0; pos < length; pos++){
            ch = s.charAt(pos);
            if(ch == '<'){
                //tag start
                if(s.charAt(pos + 1) == 'c'){
                    //<cFFFFFF>
                    pos += 2;
                    String clrStr = "";
                    while(s.charAt(pos) != '>'){
                        clrStr += s.charAt(pos);
                        pos++;
                    }

                    if(!currStr.equals("")){
                        strObj = new Object[4];
                        strObj[0] = new Integer(line);
                        strObj[1] = currColor;
                        strObj[2] = currStr;
                        strObj[3] = new Integer(xOffset);
                        xOffset += font.stringWidth(currStr);
                        ret.addElement(strObj);
                    }
                    currColor = new Integer(Integer.parseInt(clrStr, 16));
                    currStr = "";
                    continue;
                }else if(s.charAt(pos + 1) == '/'){
                    //</c>
                    pos += 3;

                    if(!currStr.equals("")){
                        strObj = new Object[4];
                        strObj[0] = new Integer(line);
                        strObj[1] = currColor;
                        strObj[2] = currStr;
                        strObj[3] = new Integer(xOffset);
                        xOffset += font.stringWidth(currStr);
                        currStr = "";
                        ret.addElement(strObj);
                    }
                    currColor = null;
                    continue;
                }else{
                    //not a tag
                }
            }
            ch = s.charAt(pos);

            if(ch != '\n')
                currStr += ch;
            if(xOffset + font.stringWidth(currStr) + GameState.CHAR_WIDTH >= width || ch == '\n' || pos == s.length() - 1){
                strObj = new Object[4];
                strObj[0] = new Integer(line);
                strObj[1] = currColor;
                strObj[2] = currStr;
                strObj[3] = new Integer(xOffset);
                xOffset = 0;
                currStr = "";
                ret.addElement(strObj);
                line++;
            }
        }
        if(ret.size() != 0){
            for(int j= 0; j<ret.size(); j++){
            	strObj = (Object[]) ret.elementAt(j);
            	if(strObj[2].toString().length() == 0 || strObj[2].toString().equals("")){
            		ret.removeElementAt(j);
            	}
            }
        }
        return ret;
    }

    private static final String punctation = ",.?:\"!;，。？：“”！；";

    public static String[] splitString(String text, int width, Font font){
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

    public static boolean rectIntersect(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2){
        int rx1, rx2, ry1, ry2;

        rx1 = x1 > x2? x1: x2;
        ry1 = y1 > y2? y1: y2;
        rx2 = x1 + w1 < x2 + w2? x1 + w1: x2 + w2;
        ry2 = y1 + h1 < y2 + h2? y1 + h1: y2 + h2;

        if(ry1 >= ry2 || rx1 >= rx2){
            return false;
        }else{
            return true;
        }
    }

    private void sort(short[] items, int itemsCount){
        int h, i, j, t;
        short[] temp = null;
        int n = itemsCount;

        for(t = 7; t < 17 && sortTable[t] <= n / 9; t++){
        }

        for(; t >= 0; t--){
            h = sortTable[t];

            for(i = h; i < n; i++){
                int id = i << 2;
                temp = new short[]{
                                items[id], items[id + 1], items[id + 2]
                };

                for(j = i - h; j >= 0 && compareYOrder(new short[]{
                                items[(j << 2)], items[(j << 2) + 1], items[(j << 2) + 2]
                }, temp) > 0; j -= h){
                    int id1 = (j + h) << 2;
                    int id2 = j << 2;

                    items[id1] = items[id2];
                    items[id1 + 1] = items[id2 + 1];
                    items[id1 + 2] = items[id2 + 2];
                }
                id = (j + h) << 2;
                items[id] = temp[0];
                items[id + 1] = temp[1];
                items[id + 2] = temp[2];
            }
        }
    }

    private boolean canPassTile(byte tile, int tileInfo){
        if(tileInfo < 0){
            return true;
        }

        ImageSet img = mapImageSet[tile];

        byte i = img.desc[tileInfo];

        if((i & 0x80) != 0){
            return false;
        }

        return true;
    }

    public static void draw3DString(Graphics g, String text, int x, int y, int archor, int color){
        draw3DString(g, text, x, y, archor, color, 0x000000);
    }

    public static void draw3DString(Graphics g, String text, int x, int y, int archor, int color, int bgColor){
        if(draw3DStringMode == DRAW_3D_STRING_SHADOW){
            GameState.drawShadowString(g, text, x, y, archor, color);
        }else if(draw3DStringMode == DRAW_3D_STRING_NORMAL){
            g.setColor(color);
            g.drawString(text, x, y, archor);
        }else{
            g.setColor(bgColor);
            g.drawString(text, x, y, archor);
            g.drawString(text, x + 2, y, archor);
            g.drawString(text, x + 1, y - 1, archor);
            g.drawString(text, x + 1, y + 1, archor);

            g.setColor(color);
            g.drawString(text, x + 1, y, archor);
        }

        return;
    }

    public static byte[] stringToBytes(String value){
        try{
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream os = new DataOutputStream(bos);
            os.writeUTF(value);

            return bos.toByteArray();
        }catch(IOException ex){
            return null;
        }
    }

    public static String bytesToString(byte[] value){
        try{
            ByteArrayInputStream bis = new ByteArrayInputStream(value);
            DataInputStream is = new DataInputStream(bis);

            return is.readUTF();
        }catch(IOException ex){
            return null;
        }
    }

    public static void Alert(Displayable pre, String title, String msg){
        //#if JBlend == true
        //# FormAlert alert = new FormAlert(pre,title,msg);
        //# World.RecordPreousDisplay(alert);
        //#else
        Alert alert = new Alert(title, msg, null, AlertType.WARNING);
        display.setCurrent(alert, pre);
        //#endif
    }

    public static boolean getPercentRate(int percent){
        int ran = randGen.nextInt(100);

        if(ran <= percent){
            return true;
        }else{
            return false;
        }
    }

    public static int random(int min, int max){
        int value = randGen.nextInt() % (max - min + 1);
        if(value < 0){
            value = -value;
        }
        return min + value;
    }

    /*---------- Tools Functions End ----------*/

    /*---------- Battle Functions Begin ---------*/

    public ArmySprite[] armySprite = null;
    public Sprite[] playerSprite = null;

    //TODO delete
    public PetSprite[] playerPet = new PetSprite[3];
    public PetSprite[] armyPet = new PetSprite[3];
    //TODO end

    public static ImageSet battleIcon;

    //#if Directory == NK-E61
    //# public static final short BATTLEICON_X = 160;
    //# public static final short BATTLEICON_Y = 126;
    //# public static final short BATTLEICON_OFFSET = 26;
    //# public static final short BATTLEICON_OFFSET_PET = 36;
    //# public static final short BATTLEICON_STEP = 8;
    //# public static final short BATTLEICON_STEP_PET = 12;
    //# public static final short BATTLEICON_FRAME_WIDTH = 320;
    //# public static final short BATTLEICON_FRAME_HEIGHT = 28;
    //#elif (Directory == NK-BigScreen) || (Directory == NK-Nokia403Big) || (Directory == SE-S700)
    //# public static final short BATTLEICON_X = 120;
    //# public static final short BATTLEICON_Y = 166;
    //# public static final short BATTLEICON_OFFSET = 26;
    //# public static final short BATTLEICON_OFFSET_PET = 36;
    //# public static final short BATTLEICON_STEP = 8;
    //# public static final short BATTLEICON_STEP_PET = 12;
    //# public static final short BATTLEICON_FRAME_WIDTH = 240;
    //# public static final short BATTLEICON_FRAME_HEIGHT = 28;
    //#elif (Directory == MT-General) || (Directory == Midp2-General) || (Directory == SE-K500) || (Directory == SE-K300) || (Directory == Nokia403) || (MIDP2Common == true) || (Directory == ClientTouch-E680) || (Directory == ClientTouch--Midp2-General) || (Directory == ClientTouch-SE-General) || (Directory == ClientTouch-Nokia5800) 
    public static short BATTLEICON_X = -1/*= 88*/;
    public static short BATTLEICON_Y /*= 126*/;
    public static short BATTLEICON_OFFSET = 26;
    public static short BATTLEICON_OFFSET_PET = 36;
    public static short BATTLEICON_STEP = 8;
    public static short BATTLEICON_STEP_PET = 12;
    public static short BATTLEICON_FRAME_WIDTH/* = 176*/;
    public static short BATTLEICON_FRAME_HEIGHT = 28;
    //#else
    //# public static final short BATTLEICON_X = 88;
    //# public static final short BATTLEICON_Y = 126;
    //# public static final short BATTLEICON_OFFSET = 24;
    //# public static final short BATTLEICON_OFFSET_PET = 36;
    //# public static final short BATTLEICON_STEP = 8;
    //# public static final short BATTLEICON_STEP_PET = 12;
    //# public static final short BATTLEICON_FRAME_WIDTH = 176;
    //# public static final short BATTLEICON_FRAME_HEIGHT = 28;
    //#endif

    public static final byte BATTLEICON_ATTACK = 0;
    public static final byte BATTLEICON_SKILL = 1;
    public static final byte BATTLEICON_CATCH = 2;
    public static final byte BATTLEICON_ITEM = 3;
    public static final byte BATTLEICON_RUN = 4;
    public static final byte[] BATTLEICON_CURSOR = {
                    5, 6
    };
    public static final byte BATTLEICON_PETHEAD = 7;
    public static final byte[] BATTLEICON_PLAYERHEAD = {
                    8, 9
    };
    public static final byte BATTLEICON_DEF = 10;
    public static final byte BATTLEICON_HMSTR = 11;

    //#if (Directory == SE-K500) || (Directory == SE-K300) || (Directory == Nokia403)
    //# public static final byte LOCATION_LEFT = 5;
    //#else
    public static final byte LOCATION_LEFT = 20;
    //#endif
    public static final byte LOCATION_TOP = 10;

    //#if (Directory == MT-General) || (Directory == Midp2-General) || (Directory == SE-K500) || (Directory == SE-K300) || (Directory == Nokia403) || (MIDP2Common == true) || (Directory == ClientTouch-E680) || (Directory == ClientTouch--Midp2-General) || (Directory == ClientTouch-SE-General) || (Directory == ClientTouch-Nokia5800)
    public static int LOCATION_HEIGHT;
    //#else
    //# public static final byte LOCATION_HEIGHT =
    //#if (Directory == NK-BigScreen) || (Directory == NK-Nokia403Big) || (Directory == SE-S700)
    //# 70
    //#else
    //# 50
    //#endif
    //# ;
    //#endif

    public static final byte LOCATION_INDEX_MIDDLE = 2;
    public static final byte LOCATION_INDEX_BOTTOM = 3;
    public static final byte LOCATION_INDEX_TOP = 1;

    public static final byte BATTLESTATE_SHOWMENU = 0;
    public static final byte BATTLESTATE_MENU = 1;
    public static final byte BATTLESTATE_PROCESSORDER = 2;
    public static final byte BATTLESTATE_CALCULATE = 3;
    public static final byte BATTLESTATE_BATTLEMOVIE = 4;

    public static final byte BATTLESTATE_SHOWSKILLLIST = 5;
    public static final byte BATTLESTATE_GETBATTLEMOVIE = 6;
    public static final byte BATTLESTATE_PLAYBATTLEMOVIE = 7;
    public static final byte BATTLESTATE_SHOWSKILLLEVEL = 8;

    public static final byte BATTLESTATE_SHOWITEMLIST = 9;

    public static final byte BATTLESTATE_CHOOSEENEMY = 10;
    public static final byte BATTLESTATE_CHOOSEFRIEND = 11;

    public static final byte BATTLESTATE_WAITING = 12;

    public static final byte BATTLESTATE_SHOWMSGBOX = 13;

    public static final byte BATTLESTATE_PETMENU = 14;
    public static final byte BATTLESTATE_MENUCREATEOK = 15;
    public static final byte BATTLESTATE_PETMENUCREATEOK = 16;

    public static final String BATTLEMENU_ATTACK = "攻击";
    public static final String BATTLEMENU_SKILL = "技能";
    public static final String BATTLEMENU_ITEM = "道具";
    public static final String BATTLEMENU_CATCH = "捕捉";
    public static final String BATTLEMENU_RUNAWAY = "逃跑";
    public static final String BATTLEMENU_DEF = "防御";
    public static final String BATTLEMENU_DISABLE = "禁用";

    public static String battleMenuText[] = null;
    public static byte battleMenuIconIdx[] = null;
    public static byte battleMenuSelMap[][] = null;

    public static final byte SEAL_SKILL_ATTACK = (byte)0x1;
    public static final byte SEAL_SKILL_SKILL = (byte)0x2;
    public static final byte SEAL_SKILL_ITEM = (byte)0x4;
    public static final byte SEAL_SKILL_CATCH = (byte)0x8;
    public static final byte SEAL_SKILL_RUNAWAY = (byte)0x10;
    public static final byte SEAL_SKILL_DEF = (byte)0x20;
    public static final byte SEAL_SKILL_ALL = (byte)0x80;

    public static byte battleSkillSeal = (byte)0xFF;
    public static byte battlePetSkillSeal = (byte)0xFF;

    public static final int CLR_MENU = 0xffccdd;

    public static final int CLR_HPDEC = 0xff0000;

    public static final int CLR_MPDEC = 0x0000ff;

    public static final int CLR_HPINC = 0x00ff00;

    public static final int CLR_MPINC = 0x0000ff;

    public static final int CLR_CRI = 0xffff00;

    /**
     * -1无战斗，0本地战斗,>0服务器战斗PK ID
     **/
    public static int nowBattle = -1;
    public static int player_PKID = 0;
    public static boolean pkBattle = false;

    public static int[] serverSyncIndex;
    public static int[] serverSyncHp;
    public static int[] serverSyncMp;
    public static int[] serverSyncStatus;
    public static boolean[] serverSyncCanAction;
    public static int[] serverSyncItemUse;
    public static String[] serverSyncSkillName;
    public static int[] serverSyncPetHp;
    public static int[] serverSyncPetMp;
    public static int[] serverSyncPeteStatus;
    public static boolean[] serverSyncPetCanAction;
    public static String[] serverSyncPetSkillName;

    public static boolean battleBackgroundInitOver;

    public static byte battleState;

    public static byte lastBattleState;

    public static short iconOffset = 0;

    public static byte playerMenuSelect = 0;

    public static byte petMenuSelect = 0;

    public static byte battleMenuSelectIndex = 0;

    public static byte battleSkillSelectIndex = 0;

    public static byte battleSkillLevelIndex = 0;

    public static byte battleItemSelectIndex = 0;

    public static int skillSelect;
    public static int petSkillSelect;

    public static byte skillSelectIdx;
    public static byte petSkillSelectIdx;

    public static byte targetSelect = 0;

    public static short battleBout = 0;
    public static UWAPSegment syncSegment;

    public static boolean battleHasCanSelectSkill;
    public static boolean battleCanAction;
    public static boolean battlePetCanAction;

    /**
     * 选择对象类型 0=无选择 1=队友 2=敌人
     */
    public static byte targetType = 0;

    public static boolean canSelectDie = false;

    /**
     *
     */
    public static byte targetSelectType = 0;

    public static byte battleMoviePointer = 0;

    public static int battleMonsterIndex;

    /**
     *  0 = 玩家行动 1 = 玩家宠物行动
     */
    public static int currentActionType = 0;

    public static BattleSprite source;

    /**
     * 0 – 未结束、1 – 战斗胜利、2 – 战斗失败、3 – 对方全逃、4 – 本方全逃
     */
    public static int battleOver;

    /**
     * 组队战斗下，所战斗的怪物组id
     */
    public static int battleMonsterId;

    public static String[] battleSkillsName;
    public static String[] showBattleSkillsName;
    public static short battleSkillsPageNo=0;
    public static short pageSkillnumber=0;
    public static short showBattleSkillSelectIndex=0;
    public static short[] battleSkillsID;
    public static boolean autoBattle=false;  //是否自动战斗
    public static boolean serverBattle=false;
    public static boolean friendSelect = false;//是否非低方
    public static short friendSelectIndex = 0;//非低方位置
    public static boolean uniqueBatttle =false;
    public static boolean monsterSetPlayerBattle =false;
    public static boolean monsterSetPetBattle =false;
    public static short teamNumber=1;
    public static short tempTeamNumber = 0;
    public static String battleMsg;
  //#if TouchScreen == true
    public static boolean autoDrawBattleImage=false;  //是否画自动战斗图标
    public static boolean autoDrawSkillLeveLImage=false;
    //#endif
    //#if Revision == QQ && TestVersion == true
    public static byte[] k700Test = new byte[15000];
    //#endif
    public static Object[] battleItems;

    public static String skillString;

    public static boolean directChoose;
    public static boolean directFirendChoose;
    private int getMenuIconIndex(){
        for(int i = 0; i < battleMenuIconIdx.length; i++){
            if(battleMenuIconIdx[i] == battleMenuSelectIndex){
                return i;
            }
        }
        return -1;
    }

    private int[] getMenuSelLine(){
        for(int line = 0; line < battleMenuSelMap.length; line++){
            for(int i = 0; i < battleMenuSelMap[line].length; i++){
                if(battleMenuSelMap[line][i] == battleMenuSelectIndex){
                    return new int[]{
                                    line, i
                    };
                }
            }
        }
        return new int[]{
                        0, 0
        };
    }

    public void cycleBattle(){
        if(syncSegment != null){
            processRoundEnd(syncSegment, true);
            syncSegment = null;
        }

        player.handleKey();

        int armyNum = 0;
        if(armySprite != null){
            for(int i = 0; i < armySprite.length; i++){
                if(armySprite[i] != null && !armySprite[i].testCannotBattle()){
                    armyNum++;
                }
            }
        }

        if(armyPet != null){
            for(int i = 0; i < armyPet.length; i++){
                if(armyPet[i] != null && !armyPet[i].testCannotBattle()){
                    armyNum++;
                }
            }
        }
        if(playerSprite != null){
            for(int i = 0; i < playerSprite.length; i++){
                if(playerSprite[i] != null){
                    playerSprite[i].groupIndex = i;
                    playerSprite[i].cycle(_elapsedTime, this);
                    playerSprite[i].showName = false;
                    tempTeamNumber++;
                }
            }
        }
        if(1 == tempTeamNumber){
        	if(serverBattle){
        		if(nowBattle == 0){
        			monsterSetPlayerBattle = true;
        			teamNumber = 1;
        			serverBattle = false;
        		}
        	}
        }
        tempTeamNumber=0;
        if(armySprite != null){
            for(int i = 0; i < armySprite.length; i++){
                if(armySprite[i] != null){
                    armySprite[i].groupIndex = i;
                    armySprite[i].cycle(_elapsedTime, this);

                    if(!directChoose){
                        armySprite[i].showName = false;
                    }
                }
            }
        }

        if(playerPet != null){
            for(int i = 0; i < playerPet.length; i++){
                if(playerPet[i] != null){
                    playerPet[i].groupIndex = i;
                    playerPet[i].cycle(_elapsedTime, this);
                    playerPet[i].showName = false;
                }
            }
        }

        if(armyPet != null){
            for(int i = 0; i < armyPet.length; i++){
                if(armyPet[i] != null){
                    armyPet[i].groupIndex = i;
                    armyPet[i].cycle(_elapsedTime, this);

                    if(!directChoose){
                        armyPet[i].showName = false;
                    }
                }
            }
        }
        directChoose = false;
        directFirendChoose =false;
        if(armyNum == 1){
            directChoose = true;
        }
        if(autoBattle){
        	directChoose = true;
        	directFirendChoose =true;
        }
        switch(battleState){
            case BATTLESTATE_PETMENU: {
                skillString = null;
                if(!player.petCurrent.canAction() || (nowBattle > 0 && !battlePetCanAction)){
                    //不可选
                    battleState = BATTLESTATE_CALCULATE;
                    targetType = 0;
                    petSkillSelect = Skill.SKILL_STAY;
                    monsterSetPetBattle = true;
                    if(player.petCurrent.showDie){
                		monsterSetPetBattle = false;
                	}
                    break;
                }
                battleMenuText = new String[]{
                                null, BATTLEMENU_SKILL, null, BATTLEMENU_DEF, null, BATTLEMENU_ATTACK
                };

                battleMenuIconIdx = new byte[]{
                                -1, BATTLEICON_SKILL, -1, BATTLEICON_DEF, -1, BATTLEICON_ATTACK
                };

                battleMenuSelMap = new byte[][]{
                    {
                                    BATTLEICON_SKILL, BATTLEICON_ATTACK, BATTLEICON_DEF
                    }

                };

                battleState = BATTLESTATE_PETMENUCREATEOK;

                break;
            }
            case BATTLESTATE_MENU: {
                skillString = null;
                if(!player.canAction() || (nowBattle > 0 && !battleCanAction)){
                    //不可选
                    battleState = BATTLESTATE_CALCULATE;
                    targetType = 0;
                    skillSelect = Skill.SKILL_STAY;
                    monsterSetPlayerBattle = true;
                    break;
                }

                //                if(nowBattle == 0){
                if(player.level < 11 || pkBattle){
                    battleMenuText = new String[]{
                                    null, BATTLEMENU_SKILL, null, BATTLEMENU_ITEM, null, BATTLEMENU_ATTACK, null, BATTLEMENU_DISABLE, null, BATTLEMENU_RUNAWAY
                    };

                }else{
                    battleMenuText = new String[]{
                                    null, BATTLEMENU_SKILL, null, BATTLEMENU_ITEM, null, BATTLEMENU_ATTACK, null, BATTLEMENU_CATCH, null, BATTLEMENU_RUNAWAY
                    };
                }

                battleMenuIconIdx = new byte[]{
                                -1, BATTLEICON_SKILL, -1, BATTLEICON_ITEM, -1, BATTLEICON_ATTACK, -1, BATTLEICON_CATCH, -1, BATTLEICON_RUN
                };

                battleMenuSelMap = new byte[][]{
                                {
                                                BATTLEICON_SKILL, BATTLEICON_ITEM
                                }, {
                                    BATTLEICON_ATTACK
                                }, {
                                                BATTLEICON_CATCH, BATTLEICON_RUN
                                }

                };

                battleState = BATTLESTATE_MENUCREATEOK;

                break;
            }
            case BATTLESTATE_MENUCREATEOK: {
                iconOffset += BATTLEICON_STEP;
                short offset = BATTLEICON_OFFSET;

                if(iconOffset >= offset){
                    iconOffset = offset;
                    battleState = BATTLESTATE_SHOWMENU;
                }
            }

                break;
            case BATTLESTATE_PETMENUCREATEOK: {
                iconOffset += BATTLEICON_STEP_PET;
                short offset = BATTLEICON_OFFSET_PET;

                if(iconOffset >= offset){
                    iconOffset = offset;
                    battleState = BATTLESTATE_SHOWMENU;
                }
            }

                break;
            case BATTLESTATE_SHOWMENU:
                String menuCmd = null;

                battleHasCanSelectSkill = false;

                if(sealBattleSkill(battleMenuSelectIndex) && battleMenuSelMap != null){
                    for(int i = 0; i < battleMenuSelMap.length; i++){
                        for(int j = 0; j < battleMenuSelMap[i].length; j++){
                            if(!sealBattleSkill(battleMenuSelMap[i][j])){
                                battleMenuSelectIndex = battleMenuSelMap[i][j];
                                battleHasCanSelectSkill = true;

                                break;
                            }
                        }

                        if(battleHasCanSelectSkill){
                            break;
                        }
                    }
                }else{
                    battleHasCanSelectSkill = true;
                }

                if(!battleHasCanSelectSkill){
                    //无可选技能

                    break;
                }

                for(int i = 0; i < battleMenuText.length; i++){
                    if(battleMenuText[i] != null && isKeyPressed(KEY_NUM0_PRESSED + i, true)){
                        if(!sealBattleSkill(battleMenuIconIdx[i])){
                            battleMenuSelectIndex = battleMenuIconIdx[i];
                            menuCmd = battleMenuText[i];
                            directChoose = false;
                            directFirendChoose = false;
                            uniqueBatttle=true;
                            autoBattle = false;
                            if( 0 == currentActionType)
                            {
                            	monsterSetPlayerBattle = false;
                            }else{
                            	monsterSetPetBattle = false;
                            }
                        }
                    }
                }

                //#if TouchScreen == true
                           boolean directTouchChoose = false;
                           int focusButton = StaticUtils .getDragOverButton();
                           int index = focusButton-2000;
                           if(focusButton != -1 && focusButton>=2000){
                        	   getTouchMenuIconIndex(index);
                           }
                           int pressedButton = StaticUtils.getPressedButton();
                           if(-1 != pressedButton && pressedButton>=2000){
                        	   index = pressedButton - 2000;
                        	   getTouchMenuIconIndex(index);
                        	   directTouchChoose = true;
                           }
                //#endif 
                    //#if TouchScreen == true
                 if(World.isKeyPressed(World.FIRE_PRESSED, true) || directTouchChoose){
                    //#else
                    //# if(World.isKeyPressed(World.FIRE_PRESSED, true)){
                    //#endif 
                    menuCmd = battleMenuText[getMenuIconIndex()];
                    directChoose = false;
                    directFirendChoose = false;
                    uniqueBatttle=true;
                    autoBattle = false;
                  //#if TouchScreen == true
                    autoDrawBattleImage = false ;
                    //#endif
                    if( 0 == currentActionType)
                    {
                    	monsterSetPlayerBattle = false;
                    }else{
                    	monsterSetPetBattle = false;
                    }
                }else if(isKeyPressed(UP_PRESSED, true)){
                    if(battleMenuSelMap.length > 1){
                        int line = getMenuSelLine()[0];
                        int oldLine = line;

                        do{
                            line--;

                            if(line < 0){
                                line = battleMenuSelMap.length - 1;
                            }

                            for(int i = 0; i < battleMenuSelMap[line].length; i++){
                                if(!sealBattleSkill(battleMenuSelMap[line][i])){
                                    battleMenuSelectIndex = battleMenuSelMap[line][i];
                                    line = oldLine;

                                    break;
                                }
                            }
                        }while(line != oldLine);
                    }
                }else if(isKeyPressed(DOWN_PRESSED, true)){
                    if(battleMenuSelMap.length > 1){
                        int line = getMenuSelLine()[0];
                        int oldLine = line;

                        do{
                            line++;

                            if(line >= battleMenuSelMap.length){
                                line = 0;
                            }

                            for(int i = 0; i < battleMenuSelMap[line].length; i++){
                                if(!sealBattleSkill(battleMenuSelMap[line][i])){
                                    battleMenuSelectIndex = battleMenuSelMap[line][i];
                                    line = oldLine;

                                    break;
                                }
                            }
                        }while(line != oldLine);
                    }
                }else if(isKeyPressed(LEFT_PRESSED, true)){
                    int[] info = getMenuSelLine();
                    int line = info[0];
                    int sel = info[1] - 1;
                    int oldLine = line;

                    do{
                        boolean found = false;

                        for(int i = sel; i >= 0; i--){
                            if(!sealBattleSkill(battleMenuSelMap[line][i])){
                                battleMenuSelectIndex = battleMenuSelMap[line][i];
                                found = true;

                                break;
                            }
                        }

                        if(found){
                            break;
                        }

                        line--;

                        if(line < 0){
                            line = battleMenuSelMap.length - 1;
                        }

                        sel = battleMenuSelMap[line].length - 1;
                    }while(line != oldLine);
                }else if(isKeyPressed(RIGHT_PRESSED, true)){
                    int[] info = getMenuSelLine();
                    int line = info[0];
                    int sel = info[1] + 1;
                    int oldLine = line;

                    do{
                        boolean found = false;

                        for(int i = sel; i < battleMenuSelMap[line].length; i++){
                            if(!sealBattleSkill(battleMenuSelMap[line][i])){
                                battleMenuSelectIndex = battleMenuSelMap[line][i];
                                found = true;

                                break;
                            }
                        }

                        if(found){
                            break;
                        }

                        line++;

                        if(line >= battleMenuSelMap.length){
                            line = 0;
                        }

                        sel = 0;
                    }while(line != oldLine);
                }else  if(World.isKeyPressed(World.SOFT_LAST_PRESSED, true)){

                		battleState=BATTLESTATE_MENU;
                         currentActionType = 0;
                         iconOffset = 0;
                        battleMenuSelectIndex=1;
                		clearKeyStates();
                		break;
                }else if(World.isKeyPressed(World.KEY_STAR_PRESSED, true)){
                	if(uniqueBatttle || monsterSetPlayerBattle || monsterSetPetBattle || skillSelect==0||pkBattle){
                		battleMsg = "当前战斗禁止使用";
                        battleState = BATTLESTATE_SHOWMSGBOX;
                        clearKeyStates();
                		break;
                	}
                	if(player.petCurrent != null && player.petCurrent.canBattle() )
                	{
                		if(0 ==petSkillSelect){
                			battleMsg = "当前战斗禁止使用";
                            battleState = BATTLESTATE_SHOWMSGBOX;
                            clearKeyStates();
                			break;
                		}
                	}
                	autoBattle = true;
                //	System.out.println("测试人物技能"+skillSelect);
                //	System.out.println("测试 宠物技能"+petSkillSelect);
                	BattleSprite[] target = armySprite;
//                	if(friendSelect){
//                		targetSelect = (byte) friendSelectIndex;
//                	}else
//                	{
                		targetSelect = (byte) friendSelectIndex;
//                	}
            		directChoose = true;
            		directFirendChoose = true;
            		toProcessOrder();
            		break;
                }
                 
               //#if TouchScreen == true
                 autoDrawSkillLeveLImage = false;
                 //#endif
                if(menuCmd != null){
                    if(currentActionType == 0){
                        playerMenuSelect = battleMenuSelectIndex;
                    }else{
                        petMenuSelect = battleMenuSelectIndex;
                    }

                    if(menuCmd.equals(BATTLEMENU_ATTACK)){
                        //攻击
                        if(currentActionType == 0){
                            skillSelect = Skill.SKILL_ATTACK;
                        }else{
                            petSkillSelect = Skill.SKILL_ATTACK;
                        }
                        toProcessOrder();
                    }else if(menuCmd.equals(BATTLEMENU_SKILL)){
                        //技能
                        battleState = BATTLESTATE_SHOWSKILLLIST;
                      //#if TouchScreen == true
                        autoDrawSkillLeveLImage = true;
                        //#endif
                        if(currentActionType == 0){
                            createBattleSkillsInfo();
                        }else{
                            createPetSkillsInfo();
                        }
                        clearKeyStates();
                        if(battleSkillsID.length == 0){
                            //无可用技能
                            battleMsg = "无可用技能";
                            battleState = BATTLESTATE_SHOWMSGBOX;
                        }else{
                        	pageSkillnumber = (short) ((viewHeight-20-(GameState.LINE_HEIGHT/2)*3) / GameState.LINE_HEIGHT);
	                        if(battleSkillsID.length <= pageSkillnumber ){
	                        	pageSkillnumber = (short) battleSkillsID.length;
	                        }
	                        if( pageSkillnumber >= 9){
	                        	pageSkillnumber = 9;
	                        }
	                        showBattleSkillsName = new String[pageSkillnumber];
                        	if(currentActionType == 0){
                        		battleSkillSelectIndex = skillSelectIdx;
                        	}else{
                        		battleSkillSelectIndex = petSkillSelectIdx;
                        	}
                        	if(battleSkillSelectIndex>=battleSkillsID.length)
                        	{
                        		battleSkillSelectIndex=0;
                        	}
                        	battleSkillsPageNo = (short) (battleSkillSelectIndex/pageSkillnumber);
                        	showBattleSkillSelectIndex = (short) (battleSkillSelectIndex - pageSkillnumber*battleSkillsPageNo);
                        	int k ;
     		                for(int i = 0; i<pageSkillnumber; i++){
     			                    if(pageSkillnumber*battleSkillsPageNo+i >= battleSkillsName.length){
     			                       break;
     			                      }
     			                   showBattleSkillsName[i] = battleSkillsName[pageSkillnumber*battleSkillsPageNo+i];
     			                   k=showBattleSkillsName[i].indexOf('.');
     			                   if(k>=1){
     			                	  showBattleSkillsName[i]=(i+1)+showBattleSkillsName[i].substring(k);
   		                			}
     			                   }
                            }
                    }else if(menuCmd.equals(BATTLEMENU_ITEM)){
                        //道具
                        battleItemSelectIndex = 0;
                        skillSelect = Skill.SKILL_ITEM;
                        battleState = BATTLESTATE_SHOWITEMLIST;
                        createBattleItemListInfo();
                        clearKeyStates();

                        if(battleItems.length == 0){
                            //无可用技能
                            battleMsg = "无可用物品";
                            battleState = BATTLESTATE_SHOWMSGBOX;
                        }
                    }else if(menuCmd.equals(BATTLEMENU_CATCH)){
                        //捕捉
                        skillSelect = Skill.SKILL_CATCH;
                        if(player.petBag.size() == player.petBagSize){
                            //宠物栏满了
                            battleMsg = "宠物栏已满";
                            battleState = BATTLESTATE_SHOWMSGBOX;
                        }else{
                            toProcessOrder();
                        }
                    }else if(menuCmd.equals(BATTLEMENU_RUNAWAY)){
                        //逃跑
                        skillSelect = Skill.SKILL_RUN;
                        toProcessOrder();
                    }else if(menuCmd.equals(BATTLEMENU_DEF)){
                        petSkillSelect = Skill.SKILL_STAY;
                        battleState = BATTLESTATE_CALCULATE;
                        targetType = 0;
                    }else if(menuCmd.equals(BATTLEMENU_DISABLE)){
                        //禁用
                        battleMsg = "当前战斗禁止使用";
                        battleState = BATTLESTATE_SHOWMSGBOX;
                    }
                }else{
                    if(isKeyPressed(KEY_NUM0_PRESSED, true)){
                        if(!World.chat_input_doing){
                            GameState state = new GameState(GameState.STATE_MAINMENU);
                            state.showSendChatForm(false);
                            World.chat_input_doing = true;
                        }
                    }else if(isKeyPressed(KEY_POUND_PRESSED, true)){
                        if(!World.chat_input_doing){
                            GameState state = new GameState(GameState.STATE_MAINMENU);
                            state.showSendChatForm(true);
                            World.chat_input_doing = true;
                        }
                    }
                }
                break;
            case BATTLESTATE_PROCESSORDER: {
                BattleSprite target = null;
                int sk = skillSelect;
                if(currentActionType == 0){
                    target = player;
                }else{
                    target = player.petCurrent;
                    sk = petSkillSelect;
                }

                int[] choose = Skill.getSkillStatus(target, sk);

                /**TODO change, battleCanAction is wrong from server **/
                //if(choose[0] == Skill.CANNOT_SELECT_SKILL || (nowBattle > 0 && !battleCanAction)){
                if(choose[0] == Skill.CANNOT_SELECT_SKILL){
                    //不可选
                    battleState = BATTLESTATE_CALCULATE;
                    targetType = 0;
                    target.setSkill(Skill.SKILL_STAY);
                    break;
                }

                if(choose[2] > 0){
                    battleMsg = "魔法不足！";
                    battleState = BATTLESTATE_SHOWMSGBOX;
                    autoBattle = false;
                    break;
                }

                switch(choose[3]){
                    case Skill.CHOOSE_NONE:
                        battleState = BATTLESTATE_CALCULATE;
                        targetType = 0;
                        break;
                    case Skill.CHOOSE_ENEMY:
                        battleState = BATTLESTATE_CHOOSEENEMY;
//                        targetSelect = 0;
                        if(friendSelect){
                        	targetSelect = 0;
                        }else{
                        	targetSelect = (byte) friendSelectIndex;
                        }
                        if(currentActionType == 0){
                        	friendSelect = false;
                        }
                        targetType = 2;
                        break;
                    case Skill.CHOOSE_FRIEND:
                        battleState = BATTLESTATE_CHOOSEFRIEND;
                        targetSelect = 0;
                        if(!friendSelect){
                        	friendSelect = true;

                        }else{
                        	targetSelect = (byte) friendSelectIndex;
                        }
                        targetType = 1;
                        break;
                    case Skill.CHOOSE_FRIEND_ALL:
                        battleState = BATTLESTATE_CHOOSEFRIEND;
                        targetSelect = 0;
                        if(!friendSelect){
                        	friendSelect = true;

                        }else{
                        	targetSelect = (byte) friendSelectIndex;
                        }
                        targetType = 1;
                        canSelectDie = true;
                        break;
                    case Skill.CHOOSE_OWNER:
                        battleState = BATTLESTATE_CALCULATE;
                        targetType = 1;
                        player.petCurrent.setTarget(player, player.groupIndex, targetType);

                        break;

                }
                break;
            }
            case BATTLESTATE_CALCULATE:

                if(currentActionType == 0){
                    player.setSkill(skillSelect);
                    updateSkillLevel();

                    if(player.petCurrent != null && player.petCurrent.canBattle() /*&& (nowBattle == 0 || nowBattle > 0 && playerPet[0] != null)*/){
                    	 if(!autoBattle){
                         	battleState = BATTLESTATE_PETMENU;
                             currentActionType = 1;
                             battleMenuSelectIndex = petMenuSelect;
                             battleSkillSelectIndex = petSkillSelectIdx;
                             iconOffset = 0;
                             }else{
     	                        currentActionType = 1;
     	                        iconOffset = 0;
     	                        toProcessOrder();
                             }

                    }else{
                        battleState = BATTLESTATE_BATTLEMOVIE;
                    }
                }else{
                    player.petCurrent.setSkill(petSkillSelect);
                    battleState = BATTLESTATE_BATTLEMOVIE;
                }

                break;
            case BATTLESTATE_BATTLEMOVIE:
                if(nowBattle == 0){
                    //本地战斗，本地计算
                    //TODO change to add both pet data
                    if(battleBout(playerSprite, armySprite, playerPet, armyPet, battleBout))
                        battleOver = 1;
                    else
                        battleOver = 0;
                    //#debug
                    printBattleMovie(playerSprite, armySprite, playerPet, armyPet, battleMovie, battleBout, battleOver != 0);
                    battleState = BATTLESTATE_GETBATTLEMOVIE;
                }else{
                    byte ts = 0;
                    int action = player.skillId;

                    byte petTs = 0;
                    int petAction = player.petCurrent == null? 0: player.petCurrent.skillId;

                    if(player.targetType == 1){
                        if(player.target.bsType == BattleSprite.TYPE_PLAYER_PET){
                            ts = (byte)(player.targetIndex + 11);
                        }else{
                            ts = (byte)(player.targetIndex + 1);
                        }
                    }else if(player.targetType == 2){
                        if(player.target.bsType == BattleSprite.TYPE_MONSTER_PET){
                            ts = (byte)(-(player.targetIndex + 11));
                        }else{
                            ts = (byte)(-(player.targetIndex + 1));
                        }
                    }

                    if(player.petCurrent != null && player.petCurrent.canBattle()){
                        if(player.petCurrent.targetType == 1){
                            if(player.petCurrent.target.bsType == BattleSprite.TYPE_PLAYER_PET){
                                petTs = (byte)(player.petCurrent.targetIndex + 11);
                            }else{
                                petTs = (byte)(player.petCurrent.targetIndex + 1);
                            }
                        }else if(player.petCurrent.targetType == 2){
                            if(player.petCurrent.target.bsType == BattleSprite.TYPE_MONSTER_PET){
                                petTs = (byte)(-(player.petCurrent.targetIndex + 11));
                            }else{
                                petTs = (byte)(-(player.petCurrent.targetIndex + 1));
                            }
                        }
                    }

                    if(player.skillId == Skill.SKILL_ITEM){
                        action = (((GameItem)battleItems[battleItemSelectIndex]).itemId << 16) | ((short)player.skillId) & 0xffff;
                    }

                    World.sendRequest(pkBattle? GameState.CONN_PK_FIGHT: GameState.CONN_BATTLE_FIGHT, new Object[]{
                                    new Integer(nowBattle), new Short((short)(battleBout + 1)), new Integer(action), new Byte(ts), new Integer(petAction), new Byte(petTs)
                    }, false);
                  //药品处理
                    if(player.skillId == Skill.SKILL_ITEM){
	                    GameItem tmpBasicItem;
	                    tmpBasicItem = (GameItem)battleItems[battleItemSelectIndex];
						if(tmpBasicItem.count <= 1){
							monsterSetPlayerBattle = true;
						}
                    }
                    battleState = BATTLESTATE_WAITING;
                }
                break;
            case BATTLESTATE_GETBATTLEMOVIE:
                if(battleMoviePointer == battleMovie.size()){
                    battleMoviePointer = 0;

                    if(nowBattle == 0){
                        battleBout++;
                    }

                    iconOffset = 0;
                    battleState = BATTLESTATE_MENU;
                    autoBattle = false;
                    uniqueBatttle=false;
                    battleMenuSelectIndex = playerMenuSelect;
                    battleSkillSelectIndex = skillSelectIdx;
                    canSelectDie = false;
                    currentActionType = 0;
                  //#if TouchScreen == true
                    autoDrawSkillLeveLImage = false;
                    //#endif
                    if(battleOver != 0){
                        battleBout = 0;
                        byte bs;
                        if(nowBattle == 0){
                            bs = getBattleResult();
                            requestSendBattleResult(bs);
                        }else{
                            bs = (byte)battleOver;
                        }

                        if(bs == 1){
                            if(!pkBattle){
                                if(nowBattle == 0){
                                    monsters[battleMonsterIndex].visible = false;

                                    if(!monsters[battleMonsterIndex].serverRefresh){
                                        monsterRefreshPool.put(new Integer(monsters[battleMonsterIndex].id), new Integer(monsters[battleMonsterIndex].refreshTime * 1000));
                                    }
                                }else{
                                    for(int i = 0; i < monsters.length; i++){
                                        if(monsters[i].id == battleMonsterId){
                                            monsters[i].visible = false;

                                            if(!monsters[i].serverRefresh){
                                                monsterRefreshPool.put(new Integer(monsters[i].id), new Integer(monsters[i].refreshTime * 1000));

                                                break;
                                            }
                                        }
                                    }
                                }

                                player.runAwayTime = 3000;
                            }
                        }else if(bs == 2){
                            player.runAwayTime = 5000;
                        }else if(bs == 3){
                            player.runAwayTime = 5000;
                        }else if(bs == 4){
                            player.runAwayTime = 5000;
                        }
                        //#if (Directory == SE-K700) || (Directory == SE-K500) || (Directory == SE-K300) || (Directory == SE-S700)

                        //# World.playerImageSet[0] = World.getImageSetFromLocal("_male.p");
                        //# World.playerImageSet[1] = World.getImageSetFromLocal("_female.p");
                        //#endif
                        GameState state = new GameState(GameState.STATE_LOADING);
                        state.subState = GameState.SS_LOADING_LEAVEBATTLE;
                        setGameState(state);
                    }
                    break;
                }

                int[] movie = (int[])battleMovie.elementAt(battleMoviePointer);

                if(movie[0] == BattleSprite.TYPE_MONSTER){
                    //monster
                    source = armySprite[movie[1]];
                }else if(movie[0] == BattleSprite.TYPE_PLAYER){
                    //player
                    source = playerSprite[movie[1]];
                }else if(movie[0] == BattleSprite.TYPE_NET_PLAYER){
                    //net player;
                    source = playerSprite[movie[1]];
                }else if(movie[0] == BattleSprite.TYPE_PLAYER_PET){
                    //player pet
                    source = playerPet[movie[1]];
                }else if(movie[0] == BattleSprite.TYPE_MONSTER_PET){
                    //monster pet
                    source = armyPet[movie[1]];
                }

                BattleSprite dest = null;
                if(movie[2] == BattleSprite.TYPE_MONSTER){
                    //monster
                    dest = armySprite[movie[3]];
                }else if(movie[2] == BattleSprite.TYPE_PLAYER){
                    //player
                    dest = playerSprite[movie[3]];
                }else if(movie[2] == BattleSprite.TYPE_NET_PLAYER){
                    //net player;
                    dest = playerSprite[movie[3]];
                }else if(movie[2] == BattleSprite.TYPE_PLAYER_PET){
                    //player pet
                    dest = playerPet[movie[3]];
                }else if(movie[2] == BattleSprite.TYPE_MONSTER_PET){
                    //monster pet
                    dest = armyPet[movie[3]];
                }

                if(movie[4] == Skill.SKILL_RUN){
                    skillString = "正在逃跑";
                }else{
                    if(source != null && source.useSkillName.length() > 0){
                        skillString = source.useSkillName;
                    }else{
                        if(movie[4] > Skill.SKILL_NOT_READY){
                            skillString = Skill.getSkillName(player, movie[4], Skill.SHOW_MPUSE_NONE, true);
                        }else{
                            skillString = "";
                        }
                    }
                }

                if(movie[6] == 0){
                    source.addCommand(BattleSprite.makeCommand(BattleSprite.COMMAND_MOVETOTARGET, new Object[]{
                        dest
                    }));
                }

                switch(movie[4]){
                    case Skill.SKILL_LIFE_MAGIC:
                        source.addCommand(BattleSprite.makeCommand(BattleSprite.COMMAND_BEATED, new Object[]{
                                        String.valueOf(Math.abs(movie[12])), new Integer(movie[11] == Skill.ATTACK_NO_CRI? CLR_HPDEC: CLR_CRI), new Integer(movie[12]), new Integer(movie[10]),
                                        new Integer(CLR_MPDEC), new Integer(movie[13])
                        }));
                        break;
                    case Skill.SKILL_UPDATE_STATUS:
                        source.debufID = (byte)movie[10];
                        break;
                }

                switch(movie[5]){
                    case Skill.ANIMATE_RUNAWAY:
                        source.addCommand(BattleSprite.makeCommand(BattleSprite.COMMAND_PLAYANIMATE, new Object[]{
                                        new Byte((byte)movie[5]), new Integer(movie[9])
                        }));
                        break;
                    case Skill.ANIMATE_PHY_ATK:
                        source.addCommand(BattleSprite.makeCommand(BattleSprite.COMMAND_PHY_ATTACK, new Object[]{
                                        dest, new Integer(movie[9]), new Integer(movie[11]), new Integer(movie[14]), new Integer(movie[15]), new Integer(movie[10])
                        }));
                        break;
                    case Skill.ANIMATE_MGC_ATK:
                        source.addCommand(BattleSprite.makeCommand(BattleSprite.COMMAND_MGC_ATTACK, new Object[]{
                                        dest, new Integer(movie[9]), new Integer(movie[11]), new Integer(movie[14]), new Integer(movie[15]), new Integer(movie[10])
                        }));
                        break;
                    case Skill.ANIMATE_NONE:
                        break;
                    case Skill.ANIMATE_HURT:
                        source.addCommand(BattleSprite.makeCommand(BattleSprite.COMMAND_BEATED, new Object[]{
                                        String.valueOf(Math.abs(movie[12])), new Integer(movie[11] == Skill.ATTACK_NO_CRI? CLR_HPDEC: CLR_CRI), new Integer(movie[12]), new Integer(movie[10])
                        }));
                        break;
                    default:

                        int ani = movie[5];
                        switch(ani){
                            case Skill.ANIMATE_INC_MGC:
                                dest.addCommand(BattleSprite.makeCommand(BattleSprite.COMMAND_PLAYANIMATE, new Object[]{
                                                new Byte((byte)movie[5]), new Integer(movie[14]), new Integer(CLR_HPINC), new Integer(movie[15]), new Integer(CLR_MPINC)
                                }));
                                break;
                            case Skill.ANIMATE_STS_ATK:

                                int miss = movie[9];

                                if(miss == Skill.HIT_MISS){
                                    dest.addFlyString("miss", 0x0000ff);
                                }else{
                                    dest.addCommand(BattleSprite.makeCommand(BattleSprite.COMMAND_BEATED, new Object[]{
                                                    "", new Integer(0), new Integer(0), new Integer(0)
                                    }));
                                }
                                if(movie[10] <= Skill.STATUS_FAINT)
                                    source.addCommand(BattleSprite.makeCommand(BattleSprite.COMMAND_PLAYANIMATE, new Object[]{
                                                    new Byte((byte)movie[5]), dest, new Integer(movie[10])
                                    }));

                                if(movie[10] == Skill.STATUS_CATCHED){
                                    //被抓了
                                    source.addCommand(BattleSprite.makeCommand(BattleSprite.COMMAND_IDLE, new Object[]{
                                        new Integer(2)
                                    }));
                                    source.addCommand(BattleSprite.makeCommand(BattleSprite.COMMAND_HIDE, new Object[]{
                                        dest
                                    }));
                                }

                                break;
                            case Skill.ANIMATE_SAV_MGC:
                                source.addCommand(BattleSprite.makeCommand(BattleSprite.COMMAND_PLAYANIMATE, new Object[]{
                                                new Byte(Skill.ANIMATE_NOTIFY_SAV_MGC), dest
                                }));
                                break;
                            case Skill.ANIMATE_UNS_MGC:
                                source.addCommand(BattleSprite.makeCommand(BattleSprite.COMMAND_PLAYANIMATE, new Object[]{
                                                new Byte(Skill.ANIMATE_NOTIFY_UNS_MGC), dest, new Integer(movie[10]), new Integer(movie[9])
                                }));
                                break;
                            default:
                                source.addCommand(BattleSprite.makeCommand(BattleSprite.COMMAND_PLAYANIMATE, new Object[]{
                                                new Byte((byte)movie[5]), new Integer(movie[12]), new Integer(movie[13])
                                }));
                        }
                }

                if(movie[7] == 0){
                    source.addCommand(BattleSprite.makeCommand(BattleSprite.COMMAND_IDLE, new Object[]{
                        new Integer(5)
                    }));
                    source.addCommand(BattleSprite.makeCommand(BattleSprite.COMMAND_MOVEBACK, null));
                }

                battleState = BATTLESTATE_PLAYBATTLEMOVIE;
                break;
            case BATTLESTATE_PLAYBATTLEMOVIE:
                boolean flg = true;
                for(int i = 0; i < playerSprite.length; i++){
                    if(playerSprite[i] != null && !playerSprite[i].cycleCommand()){
                        flg = false;
                    }
                }

                for(int i = 0; i < armySprite.length; i++){
                    if(armySprite[i] != null && !armySprite[i].cycleCommand()){
                        flg = false;
                    }
                }

                for(int i = 0; i < playerPet.length; i++){
                    if(playerPet[i] != null && !playerPet[i].cycleCommand()){
                        flg = false;
                    }
                }

                for(int i = 0; i < armyPet.length; i++){
                    if(armyPet[i] != null && !armyPet[i].cycleCommand()){
                        flg = false;
                    }
                }

                if(flg){
                    battleMoviePointer++;
                    battleState = BATTLESTATE_GETBATTLEMOVIE;
                }
                break;
            case BATTLESTATE_SHOWSKILLLEVEL:
            	//#if TouchScreen == true
            	directChoose = false;
                focusButton = StaticUtils .getDragOverButton();
                if(focusButton != -1 && focusButton>=2000){
                	battleSkillLevelIndex = (byte) (focusButton-2000);
                }
                pressedButton = StaticUtils.getPressedButton();
                if(-1 != pressedButton && pressedButton>=2000){
                	battleSkillLevelIndex = (byte) (pressedButton - 2000);
                	directChoose = true;
                }
                //#endif 
                if(isKeyPressed(UP_PRESSED, true)){
                    battleSkillLevelIndex--;
                    if(battleSkillLevelIndex < 0)
                        battleSkillLevelIndex = (byte)(battleSkillsID.length - 1);
                }else if(isKeyPressed(DOWN_PRESSED, true)){
                    battleSkillLevelIndex++;
                    if(battleSkillLevelIndex == battleSkillsID.length)
                        battleSkillLevelIndex = 0;
                }else if(isKeyPressed(SOFT_LAST_PRESSED, true)){
                    //battleSkillSelectIndex = 0;
                    battleState = BATTLESTATE_SHOWSKILLLIST;

                    if(currentActionType == 0){
                        createBattleSkillsInfo();
                    }else{
                        createPetSkillsInfo();
                    }
                }else 
                	//#if TouchScreen == true
                    if(World.isKeyPressed(World.FIRE_PRESSED, true) || World.isKeyPressed(World.SOFT_FIRST_PRESSED, true) || directChoose){
                    	//#else
                    	//# if(World.isKeyPressed(World.FIRE_PRESSED, true) || World.isKeyPressed(World.SOFT_FIRST_PRESSED, true)){
                    	//#endif  
                	//if(isKeyPressed(SOFT_FIRST_PRESSED, true) || isKeyPressed(FIRE_PRESSED, true)){
                    if(currentActionType == 0){
                        skillSelect = battleSkillsID[battleSkillLevelIndex];
                    }else{
                        petSkillSelect = battleSkillsID[battleSkillLevelIndex];
                    }
                    toProcessOrder();
                }

                for(int i = 0; i < battleSkillsID.length; i++){
                	if(i > 9){
                		break;
                	}
                    if(isKeyPressed(KEY_NUM1_PRESSED + i, true)){
                        if(i <= battleSkillsID.length){
                            battleSkillLevelIndex = (byte)i;
                            if(currentActionType == 0){
                                skillSelect = battleSkillsID[battleSkillLevelIndex];
                            }else{
                                petSkillSelect = battleSkillsID[battleSkillLevelIndex];
                            }
                            toProcessOrder();
                        }
                        break;
                    }
                }
                break;
            case BATTLESTATE_SHOWSKILLLIST:

                //#if TouchScreen == true
                           directTouchChoose = false;   
        					pressedButton = StaticUtils.getPressedButton();
                           if(-1 != pressedButton && pressedButton>=2000){
                        	   if(pressedButton>=3000){
                        		   if(pressedButton == 3000){
                        			   if(battleSkillSelectIndex >= pageSkillnumber){
                                           battleSkillSelectIndex = (byte)(battleSkillSelectIndex - pageSkillnumber);
                                           battleSkillsPageNo--;
                                       }
                        		  }else{
                        			  if(battleSkillsID.length != pageSkillnumber){
                                          if(battleSkillSelectIndex <(battleSkillsID.length-1)){
                                          	battleSkillSelectIndex = (byte)(battleSkillSelectIndex + pageSkillnumber);
                                          	battleSkillsPageNo++;
	                                           	if(battleSkillSelectIndex >=(battleSkillsID.length-1)){
	                                           		battleSkillSelectIndex = (byte) (battleSkillsID.length-1);
	                                           		battleSkillsPageNo = (short) (battleSkillSelectIndex / pageSkillnumber);
	                                           	}
                                          }
                                  	}
                        		   }
                        	   }else{
                        		   battleSkillSelectIndex = (byte) (pageSkillnumber*battleSkillsPageNo+pressedButton - 2000);
                        		   directTouchChoose = true;
                        	   }
                    	   }
                          
                //#endif 
            	if(isKeyPressed(UP_PRESSED, true)){
                    battleSkillSelectIndex--;
                    if(battleSkillSelectIndex < pageSkillnumber*battleSkillsPageNo){
                    	battleSkillsPageNo--;
                    }
                    if(battleSkillSelectIndex < 0){
                        battleSkillSelectIndex = (byte)(battleSkillsID.length - 1);
                        battleSkillsPageNo = (short) (battleSkillSelectIndex / pageSkillnumber);
                    }
                }else if(isKeyPressed(DOWN_PRESSED, true)){
                    battleSkillSelectIndex++;
                    if(battleSkillSelectIndex >=pageSkillnumber*(battleSkillsPageNo+1)){
                    	battleSkillsPageNo++;
                    }
                    if(battleSkillSelectIndex >= battleSkillsID.length){
                        battleSkillSelectIndex = 0;
                        battleSkillsPageNo=0;
                    }
                }else  if(isKeyPressed(LEFT_PRESSED, true)){
                        if(battleSkillSelectIndex >= pageSkillnumber){
                            battleSkillSelectIndex = (byte)(battleSkillSelectIndex - pageSkillnumber);
                            battleSkillsPageNo--;
                        }
                }else if(isKeyPressed(RIGHT_PRESSED, true)){
                	if(battleSkillsID.length != pageSkillnumber){
                        if(battleSkillSelectIndex <(battleSkillsID.length-1)){
                        	battleSkillSelectIndex = (byte)(battleSkillSelectIndex + pageSkillnumber);
                        	battleSkillsPageNo++;
                        	if(battleSkillSelectIndex >=(battleSkillsID.length-1)){
                        		battleSkillSelectIndex = (byte) (battleSkillsID.length-1);
                        		battleSkillsPageNo = (short) (battleSkillSelectIndex / pageSkillnumber);
                        	}
                        }
                	}
                }else  if(isKeyPressed(SOFT_LAST_PRESSED, true)){
                    //                    battleSkillSelectIndex = 0;
                    battleState = BATTLESTATE_SHOWMENU;
                }else            
                //#if TouchScreen == true
                    if(World.isKeyPressed(World.FIRE_PRESSED, true) || World.isKeyPressed(World.SOFT_FIRST_PRESSED, true) || directTouchChoose){
                    	//#else
                    	//# if(World.isKeyPressed(World.FIRE_PRESSED, true) || World.isKeyPressed(World.SOFT_FIRST_PRESSED, true)){
                    	//#endif 
                    if(currentActionType == 0){
                        skillSelectIdx = battleSkillSelectIndex;
                        skillSelect = battleSkillsID[battleSkillSelectIndex];
                    }else{
                        petSkillSelectIdx = battleSkillSelectIndex;
                        petSkillSelect = battleSkillsID[battleSkillSelectIndex];
                    }
                    toProcessOrder();
                }else if(battleState == BATTLESTATE_SHOWSKILLLIST && isKeyPressed(KEY_NUM0_PRESSED, true)){
                    if(currentActionType == 0){
                        battleState = BATTLESTATE_SHOWSKILLLEVEL;
                        createBattleSkillsLevelInfo();
                    }
                }
            	showBattleSkillsName = new String[pageSkillnumber];
            	int k;
            	for(int i = 0; i<pageSkillnumber; i++){
            		if(pageSkillnumber*battleSkillsPageNo+i >= battleSkillsName.length){
            			break;
            		}
            		showBattleSkillsName[i] = battleSkillsName[pageSkillnumber*battleSkillsPageNo+i];
            		k=showBattleSkillsName[i].indexOf('.');
		            if(k>=1){
		                showBattleSkillsName[i]=(i+1)+showBattleSkillsName[i].substring(k);
               		}
            	}
            	showBattleSkillSelectIndex = (short) (battleSkillSelectIndex - pageSkillnumber*battleSkillsPageNo);
                for(int i = 0; i < battleSkillsID.length && i < 9 && i<pageSkillnumber; i++){
                	if(i >= 9|| pageSkillnumber*battleSkillsPageNo+i>(battleSkillsID.length-1)){
                		break;
                	}
                    if(isKeyPressed(KEY_NUM1_PRESSED + i, true)){
                        if(i <= battleSkillsID.length){
                            battleSkillsPageNo = (short) (battleSkillSelectIndex/pageSkillnumber);
                            battleSkillSelectIndex = (byte) (pageSkillnumber*battleSkillsPageNo+i);
                        	showBattleSkillSelectIndex = (short) (battleSkillSelectIndex - pageSkillnumber*battleSkillsPageNo);
                            if(currentActionType == 0){
                                skillSelectIdx = battleSkillSelectIndex;
                                skillSelect = battleSkillsID[battleSkillSelectIndex];
                            }else{
                                petSkillSelectIdx = battleSkillSelectIndex;
                                petSkillSelect = battleSkillsID[battleSkillSelectIndex];
                            }
                            toProcessOrder();
                        }
                        break;
                    }
                }
                break;
            case BATTLESTATE_SHOWITEMLIST:
            	//#if TouchScreen == true
                directTouchChoose = false;
                focusButton = StaticUtils .getDragOverButton();
                if(focusButton != -1 && focusButton>=2000){
                	battleItemSelectIndex = (byte) (focusButton-2000);
                }
                pressedButton = StaticUtils.getPressedButton();
                if(-1 != pressedButton && pressedButton>=2000){
                	battleItemSelectIndex = (byte) (pressedButton - 2000);
                	directTouchChoose = true;
                }
                //#endif 
 
                if(isKeyPressed(UP_PRESSED, true)){
                    battleItemSelectIndex--;
                    if(battleItemSelectIndex < 0)
                        battleItemSelectIndex = (byte)(battleItems.length - 1);
                }else if(isKeyPressed(DOWN_PRESSED, true)){
                    battleItemSelectIndex++;
                    if(battleItemSelectIndex == battleItems.length)
                        battleItemSelectIndex = 0;
                }else if(isKeyPressed(SOFT_LAST_PRESSED, true)){
                    battleState = BATTLESTATE_SHOWMENU;

                }else 
                //#if TouchScreen == true
                    if(isKeyPressed(World.FIRE_PRESSED, true) || isKeyPressed(World.SOFT_FIRST_PRESSED, true) || directTouchChoose){
                    	//#else
                    	//# if(isKeyPressed(SOFT_FIRST_PRESSED, true) || isKeyPressed(FIRE_PRESSED, true)){
                    	//#endif 
                    toProcessOrder();
                }

                break;
            case BATTLESTATE_CHOOSEENEMY: {

                BattleSprite[] target = armySprite;

                if(targetSelectType == BattleSprite.TYPE_MONSTER_PET){
                    target = armyPet;
                }

                if(targetSelect >= target.length)
                    targetSelect = 0;
                targetSelect = (byte)setTargetFocus(target, targetSelect, false, SETTARGETTYPE_NONE);
                target[targetSelect].showName = true;
                
              //#if TouchScreen == true
                directTouchChoose = false;
                focusButton = StaticUtils .getDragOverButton();
                if(focusButton != -1 && focusButton>=2000){
                	targetSelect  = (byte) (focusButton - 2000);
                }
                pressedButton = StaticUtils.getPressedButton();
                if(-1 != pressedButton && pressedButton>=2000){
                	targetSelect = (byte) (pressedButton - 2000);
             	   	directTouchChoose = true;
                }
                //#endif 
                if(isKeyPressed(UP_PRESSED, true) || isKeyPressed(KEY_NUM2_PRESSED, true)){
                    targetSelect = (byte)setTargetFocus(target, targetSelect, false, SETTARGETTYPE_BACKWARD);
                }else if(isKeyPressed(DOWN_PRESSED, true) || isKeyPressed(KEY_NUM8_PRESSED, true)){
                    targetSelect = (byte)setTargetFocus(target, targetSelect, false, SETTARGETTYPE_FORWARD);
                }else if(isKeyPressed(RIGHT_PRESSED, true) || isKeyPressed(KEY_NUM6_PRESSED, true)){
                    targetSelectType = BattleSprite.TYPE_MONSTER_PET;
                    target = armyPet;
                    byte sel = setTargetFocus(target, targetSelect, false, SETTARGETTYPE_NONE);
                    if(sel == -1){
                        target = armySprite;
                        targetSelect = setTargetFocus(target, targetSelect, false, SETTARGETTYPE_NONE);
                        targetSelectType = BattleSprite.TYPE_MONSTER;
                    }
                }else if(isKeyPressed(LEFT_PRESSED, true) || isKeyPressed(KEY_NUM4_PRESSED, true)){
                    targetSelectType = BattleSprite.TYPE_MONSTER;
                }else if(isKeyPressed(SOFT_LAST_PRESSED, true)){
                    targetSelect = 0;
                    battleState = lastBattleState;
                }else 
                //if(isKeyPressed(SOFT_FIRST_PRESSED, true) || isKeyPressed(FIRE_PRESSED, true) || isKeyPressed(KEY_NUM5_PRESSED, true) || directChoose){
                    //#if TouchScreen == true
                    if(World.isKeyPressed(World.FIRE_PRESSED, true) || World.isKeyPressed(World.SOFT_FIRST_PRESSED, true) || isKeyPressed(KEY_NUM5_PRESSED, true) || directChoose || directTouchChoose){
                    	//#else
                    	//# if(World.isKeyPressed(World.FIRE_PRESSED, true) || World.isKeyPressed(World.SOFT_FIRST_PRESSED, true)|| isKeyPressed(KEY_NUM5_PRESSED, true) || directChoose){
                    	//#endif 
                    if(currentActionType == 0){
                        if(skillSelect == Skill.SKILL_CATCH){
                            if(target[targetSelect].level < 11){
                                //不能捕捉
                                battleMsg = "不能捕捉10级以内的宠物";
                                battleState = BATTLESTATE_SHOWMSGBOX;

                                break;
                            }else if(target[targetSelect] instanceof ArmySprite){
                                if(((ArmySprite)target[targetSelect]).petType == ArmySprite.PET_TYPE_CANNOT_CATCH){
                                    battleMsg = "目标不能捕捉";
                                    battleState = BATTLESTATE_SHOWMSGBOX;

                                    break;
                                }else if(target[targetSelect].level > player.level){
                                    battleMsg = "不能捕捉等级高于自己的宠物";
                                    battleState = BATTLESTATE_SHOWMSGBOX;

                                    break;
                                }
                            }else if(target[targetSelect] instanceof PetSprite){
                                if(((PetSprite)target[targetSelect]).canCatch == ArmySprite.PET_TYPE_CANNOT_CATCH){
                                    battleMsg = "目标不能捕捉";
                                    battleState = BATTLESTATE_SHOWMSGBOX;

                                    break;
                                }else if(target[targetSelect].level > player.level){
                                    battleMsg = "不能捕捉等级高于自己的宠物";
                                    battleState = BATTLESTATE_SHOWMSGBOX;

                                    break;
                                }
                            }
                        }
                        friendSelectIndex = targetSelect;
                        player.setTarget(target[targetSelect], targetSelect, targetType);
                    }else{
                        player.petCurrent.setTarget(target[targetSelect], targetSelect, targetType);
                    }
                    battleState = BATTLESTATE_CALCULATE;
                }
            }
                break;
            case BATTLESTATE_CHOOSEFRIEND: {
                BattleSprite[] target = playerSprite;
                
                //#if TouchScreen == true
                directTouchChoose = false;
                focusButton = StaticUtils .getDragOverButton();
                if(focusButton != -1 && focusButton>=2000){
                	if(targetSelectType == BattleSprite.TYPE_PLAYER || targetSelectType == BattleSprite.TYPE_PLAYER_PET){
                		if(focusButton%2 == 0){//玩家
                    		targetSelect  = (byte) ((byte) (focusButton - 2000)/2);
                    		targetSelectType = BattleSprite.TYPE_PLAYER;
                    	}else{
                    		targetSelect  = (byte) ((byte) (focusButton - 2000)/2);
                    		targetSelectType = BattleSprite.TYPE_PLAYER_PET;
                    	}
                	}
               
                }
                pressedButton = StaticUtils.getPressedButton();
                if(-1 != pressedButton && pressedButton>=2000){
                	if(targetSelectType == BattleSprite.TYPE_PLAYER || targetSelectType == BattleSprite.TYPE_PLAYER_PET){
	                	if(pressedButton%2 == 0){
	                		targetSelect  = (byte) ((byte) (pressedButton - 2000)/2);
	                		targetSelectType = BattleSprite.TYPE_PLAYER;
	                	}else{
	                		
	                		targetSelect  = (byte) ((byte) (pressedButton - 2000)/2);
	                		targetSelectType = BattleSprite.TYPE_PLAYER_PET;
	                	}
                	}
             	   	directTouchChoose = true;
                }
                //#endif 
                if(targetSelectType == BattleSprite.TYPE_PLAYER_PET){
                    target = playerPet;
                }

                if(targetSelect >= target.length)
                    targetSelect = 0;
                targetSelect = (byte)setTargetFocus(target, targetSelect, canSelectDie, SETTARGETTYPE_NONE);
                target[targetSelect].showName = true;
              //  System.out.println("targetSelect" + targetSelect);
                if(isKeyPressed(UP_PRESSED, true) || isKeyPressed(KEY_NUM2_PRESSED, true)){
                    targetSelect = (byte)setTargetFocus(target, targetSelect, canSelectDie, SETTARGETTYPE_BACKWARD);
               //     System.out.println("选择上后的targetSelect" + targetSelect);
                }else if(isKeyPressed(DOWN_PRESSED, true) || isKeyPressed(KEY_NUM8_PRESSED, true)){
                    targetSelect = (byte)setTargetFocus(target, targetSelect, canSelectDie, SETTARGETTYPE_FORWARD);
                }else if(isKeyPressed(LEFT_PRESSED, true) || isKeyPressed(KEY_NUM4_PRESSED, true)){
                    targetSelectType = BattleSprite.TYPE_PLAYER_PET;
                    target = playerPet;
                    byte sel = setTargetFocus(target, targetSelect, canSelectDie, SETTARGETTYPE_NONE);

                    if(sel == -1){
                        target = playerSprite;
                        targetSelect = setTargetFocus(target, targetSelect, canSelectDie, SETTARGETTYPE_NONE);
                        targetSelectType = BattleSprite.TYPE_PLAYER;
                    }
                }else if(isKeyPressed(RIGHT_PRESSED, true) || isKeyPressed(KEY_NUM6_PRESSED, true)){
                    targetSelectType = BattleSprite.TYPE_PLAYER;
                }else if(isKeyPressed(SOFT_LAST_PRESSED, true)){
                    targetSelect = 0;
                    battleState = lastBattleState;
                }else 

                    //#if TouchScreen == true
                      if(World.isKeyPressed(World.FIRE_PRESSED, true) || World.isKeyPressed(World.SOFT_FIRST_PRESSED, true) || isKeyPressed(KEY_NUM5_PRESSED, true) || directFirendChoose || directTouchChoose){
                      	//#else
                      	//# if(World.isKeyPressed(World.FIRE_PRESSED, true) || World.isKeyPressed(World.SOFT_FIRST_PRESSED, true)){
                      	//#endif 
                	//if(isKeyPressed(SOFT_FIRST_PRESSED, true) || isKeyPressed(FIRE_PRESSED, true) || isKeyPressed(KEY_NUM5_PRESSED, true) || directFirendChoose){
                    if(currentActionType == 0){
                    	friendSelectIndex = targetSelect;
                        player.setTarget(target[targetSelect], targetSelect, targetType);
                    }else{
                    	friendSelectIndex = targetSelect;
                        player.petCurrent.setTarget(target[targetSelect], targetSelect, targetType);
                    }
                    if(skillSelect == Skill.SKILL_ITEM){
                        target[targetSelect].setItem((GameItem)battleItems[battleItemSelectIndex]);
                      //药品处理
                        GameItem tmpBasicItem;
						tmpBasicItem = (GameItem)battleItems[battleItemSelectIndex];
						if(tmpBasicItem.count <= 1){
							monsterSetPlayerBattle = true;
						}
                    }
                    battleState = BATTLESTATE_CALCULATE;
                }
            }
                break;
            case BATTLESTATE_WAITING:
                if(isKeyPressed(KEY_NUM0_PRESSED, true)){
                    if(!World.chat_input_doing){
                        GameState state = new GameState(GameState.STATE_MAINMENU);
                        state.showSendChatForm(false);
                        World.chat_input_doing = true;
                    }
                }else if(isKeyPressed(KEY_POUND_PRESSED, true)){
                    if(!World.chat_input_doing){
                        GameState state = new GameState(GameState.STATE_MAINMENU);
                        state.showSendChatForm(true);
                        World.chat_input_doing = true;
                    }
                }

                break;
            case BATTLESTATE_SHOWMSGBOX:
                if(isAnyKeyPressed()){
                	 iconOffset += BATTLEICON_STEP;
                     short offset = BATTLEICON_OFFSET;

                     if(iconOffset >= offset){
                         iconOffset = offset;
                         battleState = BATTLESTATE_SHOWMENU;
                    }
//                  battleState = BATTLESTATE_SHOWMENU;
                    if( 1 == currentActionType){
                    	battleState = BATTLESTATE_PETMENU ;
                    }
                    clearKeyStates();
                }
                break;
        }
    }

    /**
     * 判断战斗胜利还是失败
     * @return 1=胜利 2=失败 3=逃跑
     */
    public byte getBattleResult(){
        boolean armyAllDie = true;
        boolean playerAllDie = true;
        for(int i = 0; i < armySprite.length; i++){
            if(armySprite[i] != null && !armySprite[i].testCannotBattle()){
                armyAllDie = false;
                break;
            }
        }
        for(int i = 0; i < playerSprite.length; i++){
            if(playerSprite[i] != null && !playerSprite[i].testDie()){
                playerAllDie = false;
                break;
            }
        }
        if(armyAllDie)
            return 1;
        else if(playerAllDie)
            return 2;
        else
            return 3;
    }

    public void updateSkillLevel(){
        if(skillSelect > Skill.SKILL_NOT_READY){
            Skill skill = Skill.getSkill(skillSelect);
            player.skillTable[battleSkillSelectIndex][2] = skill.level;
        }
    }

    public void toProcessOrder(){
        lastBattleState = battleState;
        battleState = BATTLESTATE_PROCESSORDER;
        if(autoBattle){
	        String menuCmd = null;
	        if(currentActionType == 0){
	        	battleMenuSelectIndex = playerMenuSelect ;
		        menuCmd = battleMenuText[getMenuIconIndex()];
				if(menuCmd.equals(BATTLEMENU_CATCH)){
					if(player.level < 11){
                        battleMsg = "当前战斗禁止使用";
                        monsterSetPlayerBattle = true;
                        battleState = BATTLESTATE_SHOWMSGBOX;
					 }else if(player.petBag.size() == player.petBagSize){
		                 //宠物栏满了
		                 battleMsg = "宠物栏已满";
		                 monsterSetPlayerBattle = true;
		                 battleState = BATTLESTATE_SHOWMSGBOX;
		             }else  if((battleSkillSeal & SEAL_SKILL_CATCH) != 0){
		            	 	battleMsg = "当前战斗禁止使用";
	                        monsterSetPlayerBattle = true;
	                        battleState = BATTLESTATE_SHOWMSGBOX;
	                    }
		        }else if(menuCmd.equals(BATTLEMENU_ITEM)){
		        	 createBattleItemListInfo();
                     clearKeyStates();
                     GameItem tmpBasicItem;
 					 tmpBasicItem = (GameItem)player.basicItems.elementAt(battleItemSelectIndex);
 					 if(tmpBasicItem.count <= 1){
 						monsterSetPlayerBattle = true;
 					}
 					if((battleSkillSeal & SEAL_SKILL_ITEM) != 0){
	            	 	battleMsg = "当前战斗禁止使用";
                        monsterSetPlayerBattle = true;
                        battleState = BATTLESTATE_SHOWMSGBOX;
                    }
		        } else if(menuCmd.equals(BATTLEMENU_DEF)){
                    battleState = BATTLESTATE_CALCULATE;
		        }else  if(menuCmd.equals(BATTLEMENU_RUNAWAY)){
		        	if((battleSkillSeal & SEAL_SKILL_RUNAWAY) != 0){
		        		battleMsg = "当前战斗禁止使用";
                        monsterSetPlayerBattle = true;
                        battleState = BATTLESTATE_SHOWMSGBOX;
                 }
	        }
        }
    }
    }

    public static byte[] getBytesFromInput(DataInputStream in) throws IOException{
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

    public void alphaBackBuffer(){
        Graphics bg = bgImg.getGraphics();
        createYOrder();
        drawYOrder(bg);

        //#if (AlphaMethod == alpha) || (AlphaMethod == image) ||(AlphaMethod == drawRGB)
        GameState.fileAlphaBox(bg, 0x90ffffff, 0, 0, bgWidth, bgHeight);
        //#else
        //# bg.setColor(0xffffff);
        //# for(int i = 0; i < bgWidth; i++){
        //# for(int j = 0; j < bgHeight; j++){
        //#     if((i % 2 == 0 && j % 2 == 0) || (i % 2 != 0 && j % 2 != 0)){
        //#         bg.drawLine(i, j, i, j);
        //#     }
        //# }
        //# }
        //#endif
    }

    public void drawBattle(Graphics g){
        g.setFont(GameState.font);

        if(bgImg != null){
            if(!battleBackgroundInitOver){
                drawMapNoBuffer(gg);
                alphaBackBuffer();

                battleBackgroundInitOver = true;
            }
            g.drawImage(bgImg, 0, 0, 0);
        }else{
            drawMap(g);
        }

        BattleSprite topLayer = null;

        if(playerSprite != null){
            for(int i = 0; i < playerSprite.length; i++){
                if(playerSprite[i] != null){
                    if(playerSprite[i] == source){
                        topLayer = source;
                    }else{
                        playerSprite[i].draw(g, (short)0, (short)0);
                    }
                }
            }
        }

        if(armySprite != null){
            for(int i = 0; i < armySprite.length; i++){
                if(armySprite[i] != null && armySprite[i].show){
                    if(armySprite[i] == source){
                        topLayer = source;
                    }else{
                        armySprite[i].draw(g);
                    }
                }
            }
        }

        if(playerPet != null){
            for(int i = 0; i < playerPet.length; i++){
                if(playerPet[i] != null){
                    if(playerPet[i] == source){
                        topLayer = source;
                    }else{
                        playerPet[i].draw(g);
                    }
                }
            }
        }

        if(armyPet != null){
            for(int i = 0; i < armyPet.length; i++){
                if(armyPet[i] != null){
                    if(armyPet[i] == source){
                        topLayer = source;
                    }else{
                        armyPet[i].draw(g);
                    }
                }
            }
        }

        if(topLayer != null){
            if(topLayer instanceof Sprite){
                ((Sprite)topLayer).draw(g, (short)0, (short)0);
            }else if(topLayer instanceof ArmySprite){
                ((ArmySprite)topLayer).draw(g);
            }else if(topLayer instanceof PetSprite){
                ((PetSprite)topLayer).draw(g);
            }
        }

        if(playerSprite != null){
            for(int i = 0; i < playerSprite.length; i++){
                if(playerSprite[i] != null){
                    playerSprite[i].drawEffect(g);
                }
            }
        }

        if(armySprite != null){
            for(int i = 0; i < armySprite.length; i++){
                if(armySprite[i] != null)
                    armySprite[i].drawEffect(g);
            }
        }

        if(playerPet != null){
            for(int i = 0; i < playerPet.length; i++){
                if(playerPet[i] != null){
                    playerPet[i].drawEffect(g);

                }
            }
        }

        if(armyPet != null){
            for(int i = 0; i < armyPet.length; i++){
                if(armyPet[i] != null){
                    armyPet[i].drawEffect(g);

                }
            }
        }

        if(skillString != null && !skillString.equals("")){
            int fw = GameState.font.stringWidth(skillString) + 4;
            int fh = GameState.CHAR_HEIGHT;

            GameState.drawBackGround(g, (viewWidth - fw) / 2, 1, fw, fh, false);
            g.setColor(0);
            g.drawString(skillString, viewWidth / 2, 2, Graphics.TOP | Graphics.HCENTER);

        }

        boolean draw = true;
        boolean drawGameMenu = false;
        boolean drawItemMenu = false;
        boolean drawSkillMenu = false;
        switch(battleState){
            case BATTLESTATE_PETMENU:
            case BATTLESTATE_PETMENUCREATEOK:
                if(!player.petCurrent.canAction() || (nowBattle > 0 && !battlePetCanAction)){
                    draw = false;
                }
            case BATTLESTATE_MENU:
            case BATTLESTATE_MENUCREATEOK:
                if(!player.canAction() || (nowBattle > 0 && !battleCanAction)){
                    draw = false;
                }
                if(draw){
                    drawGameMenu(g, iconOffset, battleState == BATTLESTATE_SHOWMENU);
                }
                break;
            case BATTLESTATE_SHOWMENU:
                drawGameMenu(g, iconOffset, battleState == BATTLESTATE_SHOWMENU);
              //#if TouchScreen == true
                autoDrawBattleImage=false;
                //#endif
                //添加字符串
                //#if (Directory == MT-General) || (Directory == Midp2-General) || (Directory == SE-K500) || (Directory == SE-K300) || (Directory == Nokia403) || (MIDP2Common == true)
                BATTLEICON_X = (short)(viewWidth / 2);
                BATTLEICON_Y = (short)(viewHeight * 2 / 3);
                //#endif

                //#if (Directory == SE-K500) || (Directory == SE-K300) || (Directory == Nokia403)
                BATTLEICON_Y = (short)(viewHeight >> 1);
                //#endif
	            if(currentActionType == 0){
	            	if(pkBattle){
	            		break;
	            	}
	                if(uniqueBatttle || monsterSetPlayerBattle || monsterSetPetBattle || (skillSelect==0)){
	                	break;
	                }else{
	                	if(player.petCurrent != null && player.petCurrent.canBattle() )
	                    {
	                    	if(!(0 ==petSkillSelect)){
	                            //#if TouchScreen == true
	                    		autoDrawBattleImage=true;
	                              	//#else
	                              	//# draw3DString(g, "*键重复操作", BATTLEICON_X-GameState.CHAR_HEIGHT*2, BATTLEICON_Y - GameState.CHAR_HEIGHT-iconOffset, Graphics.LEFT | Graphics.BOTTOM, 0xFFFF00);
	                              	//#endif 
	                    	}
	                    }else{
	                    	 //#if TouchScreen == true
                    		autoDrawBattleImage=true;
                              	//#else
                              	//# draw3DString(g, "*键重复操作", BATTLEICON_X-GameState.CHAR_HEIGHT*2, BATTLEICON_Y - GameState.CHAR_HEIGHT-iconOffset, Graphics.LEFT | Graphics.BOTTOM, 0xFFFF00);
                              	//#endif 
	                    }
	                }
	            }
                break;
            case BATTLESTATE_PROCESSORDER:
                break;
            case BATTLESTATE_CALCULATE:
                break;
            case BATTLESTATE_BATTLEMOVIE:
                break;
            case BATTLESTATE_SHOWITEMLIST:
                drawGameMenu = true;
                drawItemMenu = true;
                break;
            case BATTLESTATE_SHOWSKILLLEVEL:
            case BATTLESTATE_SHOWSKILLLIST:
                drawGameMenu = true;
                drawSkillMenu = true;
                break;
            case BATTLESTATE_CHOOSEENEMY: {
                BattleSprite[] target = armySprite;
                if(targetSelectType == BattleSprite.TYPE_MONSTER_PET){
                    target = armyPet;
                }
                targetSelect = (byte)setTargetFocus(target, targetSelect, false, SETTARGETTYPE_NONE);
                if(targetSelect == -1){
                    targetSelect = (byte)setTargetFocus(armySprite, 0, false, SETTARGETTYPE_NONE);
                    targetSelectType = BattleSprite.TYPE_MONSTER;
                }

                int left = target[targetSelect].battleX;
                int top = target[targetSelect].battleY - target[targetSelect].getHeight(0);
                int width = target[targetSelect].getWidth(0);
                int height = target[targetSelect].getHeight(0);
                int x = left + width + (int)((tick / 5) % 2) * 5;
                int y = top + (height - GameState.getBtnImg().getHeight((byte)4)) / 2;
                GameState.drawButtons(g, (byte)4, x, y);
                //#if TouchScreen == true
                for (int i = 0; i < target.length ; i++){
                	if(target[i]!=null){
                		StaticUtils.addButton(2000+i, target[i].battleX, target[i].battleY-target[i].getHeight(0), target[i].getWidth(0), target[i].getHeight(0));
                	}
                }
                //#endif
                break;
            }
            case BATTLESTATE_CHOOSEFRIEND: {
                BattleSprite[] target = playerSprite;
                if(targetSelectType == BattleSprite.TYPE_PLAYER_PET){
                    target = playerPet;
                }
                targetSelect = (byte)setTargetFocus(target, targetSelect, canSelectDie, SETTARGETTYPE_NONE);
                if(targetSelect == -1){
                    targetSelect = (byte)setTargetFocus(playerSprite, 0, canSelectDie, SETTARGETTYPE_NONE);
                    targetSelectType = BattleSprite.TYPE_PLAYER;
                }

                int left = target[targetSelect].battleX;
                int top = target[targetSelect].battleY - target[targetSelect].getHeight(0);
                int height = target[targetSelect].getHeight(0);
                int x = left - GameState.getBtnImg().getWidth(6) - (int)((tick / 5) % 2) * 5;
                int y = top + (height - GameState.getBtnImg().getHeight((byte)4)) / 2;
                GameState.drawButtons(g, (byte)6, x, y);
                //#if TouchScreen == true
                for (int i = 0, k =0; i < target.length ; i++, k = k+2){
                	if(target[i]!=null){
                		if(targetSelectType == BattleSprite.TYPE_PLAYER){
                		StaticUtils.addButton(2000+k, target[i].battleX, target[i].battleY-target[i].getHeight(0), target[i].getWidth(0), target[i].getHeight(0));
                		if( playerPet[i]!= null){
                			StaticUtils.addButton(2000+k+1, target[i].battleX-target[i].getWidth(0), target[i].battleY-target[i].getHeight(0), target[i].getWidth(0), target[i].getHeight(0));
                		}
                		}else if(targetSelectType == BattleSprite.TYPE_PLAYER_PET){
                			StaticUtils.addButton(2000+k, target[i].battleX+target[i].getWidth(0)+5, target[i].battleY-target[i].getHeight(0), target[i].getWidth(0)+5, target[i].getHeight(0)+10);
                			StaticUtils.addButton(2000+k+1, target[i].battleX-5, target[i].battleY-target[i].getHeight(0), target[i].getWidth(0)+5, target[i].getHeight(0)+10);
                    		
                		}
                	}
                }
                //#endif
                break;
            }
            case BATTLESTATE_WAITING:
                GameState.drawMsgTip(g, -1, -1, new String[]{
                    "请稍候..."
                }, null, null, GameState.BTNTYPE_NONE);
                break;
            case BATTLESTATE_SHOWMSGBOX:
                GameState.drawMsgTip(g, -1, -1, splitString(battleMsg, viewWidth, GameState.font), null, null, GameState.BTNTYPE_FIRE);
                break;
        }

        drawPlayerInfo(g, Graphics.BOTTOM, DRAWPLAYERINFO_TYPE_ALL);
        if(drawGameMenu){
        	//#if TouchScreen == true
        	StaticUtils.removeAllButton();
        	//#endif
        	drawGameMenu(g, iconOffset, true);
        }
            
        if(drawItemMenu){
        	//#if TouchScreen == true
        	StaticUtils.removeAllButton();
        	//#endif
            drawItemMenu(g);
        }
        if(drawSkillMenu){
        	//#if TouchScreen == true
        	StaticUtils.removeAllButton();
        	//#endif
            drawSkillMenu(g);
        }
           
    	if(battleState == BATTLESTATE_SHOWSKILLLIST){
       	 	if(battleSkillsID.length > pageSkillnumber ){
       		 	drawLeftShadeArrow(g, showBattleSkillsName, pageSkillnumber );
       		}
   		}

    }

    public final static int DRAWPLAYERINFO_TYPE_PLAYER = 1;
    public final static int DRAWPLAYERINFO_TYPE_PET = 2;
    public final static int DRAWPLAYERINFO_TYPE_ALL = DRAWPLAYERINFO_TYPE_PLAYER | DRAWPLAYERINFO_TYPE_PET;

    public static void drawPlayerInfo(Graphics g, int anchor, int drawType){
        int x = 5;
        int y = 0;
        g.setFont(GameState.font);
        if(anchor == Graphics.TOP){
            y = 0;
        }else if(anchor == Graphics.BOTTOM){
            y = viewHeight - BATTLEICON_FRAME_HEIGHT;
        }

        if(battleIcon == null){
            battleIcon = getImageSetFromLocal("battleIcon");
        }

        GameState.drawEdge(g, 0, 0, y, BATTLEICON_FRAME_WIDTH, BATTLEICON_FRAME_HEIGHT, true, 0x990001);

        if((drawType & DRAWPLAYERINFO_TYPE_PLAYER) != 0){
            //#if (Directory == SE-K500) || (Directory == SE-K300) || (Directory == Nokia403)
            //# x += 1;
            //#else
            battleIcon.drawFrame(g, BATTLEICON_PLAYERHEAD[player.sex], x, y + BATTLEICON_FRAME_HEIGHT / 2, Graphics.LEFT | Graphics.VCENTER);
            x += 18;
            //#endif
            battleIcon.drawFrame(g, BATTLEICON_HMSTR, x, y + BATTLEICON_FRAME_HEIGHT / 2, Graphics.LEFT | Graphics.VCENTER);
            x += 8;
            y += BATTLEICON_FRAME_HEIGHT / 4 + 2;
            int hp;
            int mp;
            if(nowBattle >= 0){
                hp = player.hpShow;
                mp = player.mpShow;
            }else{
                hp = player.hp;
                mp = player.mp;
            }

            GameState.drawAttrNum(hp + "/" + player.attributes[BattleSprite.ATTR_HPMAX], g, x, y, Graphics.VCENTER | Graphics.LEFT);
            y += BATTLEICON_FRAME_HEIGHT / 2 - 4;
            GameState.drawAttrNum(mp + "/" + player.attributes[BattleSprite.ATTR_MPMAX], g, x, y, Graphics.VCENTER | Graphics.LEFT);
        }
        if((drawType & DRAWPLAYERINFO_TYPE_PET) != 0){
            if(player.petCurrent != null && (nowBattle < 0 || nowBattle >= 0 && player.petCurrent.canBattle())){
                if((drawType & DRAWPLAYERINFO_TYPE_PLAYER) != 0){
                    x = BATTLEICON_FRAME_WIDTH / 2 + 1;
                }else{
                    x = 5;
                }

                if(anchor == Graphics.TOP){
                    y = 0;
                }else if(anchor == Graphics.BOTTOM){
                    y = viewHeight - BATTLEICON_FRAME_HEIGHT;
                }

                //#if (Directory == SE-K500) || (Directory == SE-K300) || (Directory == Nokia403)
                //# x += 1;
                //#else
                battleIcon.drawFrame(g, BATTLEICON_PETHEAD, x, y + BATTLEICON_FRAME_HEIGHT / 2, Graphics.LEFT | Graphics.VCENTER);
                x += 18;
                //#endif
                battleIcon.drawFrame(g, BATTLEICON_HMSTR, x, y + BATTLEICON_FRAME_HEIGHT / 2, Graphics.LEFT | Graphics.VCENTER);
                x += 8;
                y += BATTLEICON_FRAME_HEIGHT / 4 + 2;

                int php;
                int pmp;
                if(nowBattle >= 0){
                    php = player.petCurrent.hpShow;
                    pmp = player.petCurrent.mpShow;
                }else{
                    php = player.petCurrent.hp;
                    pmp = player.petCurrent.mp;
                }

                String hp = php + "/" + player.petCurrent.attributes[BattleSprite.ATTR_HPMAX];
                String mp = pmp + "/" + player.petCurrent.attributes[BattleSprite.ATTR_MPMAX];
                GameState.drawAttrNum(hp, g, x, y, Graphics.VCENTER | Graphics.LEFT);
                y += BATTLEICON_FRAME_HEIGHT / 2 - 4;
                GameState.drawAttrNum(mp, g, x, y, Graphics.VCENTER | Graphics.LEFT);

                if((drawType & DRAWPLAYERINFO_TYPE_PLAYER) == 0){
                    int hpLen = hp.length() * GameState.attrNum.getWidth(0);
                    int mpLen = mp.length() * GameState.attrNum.getWidth(0);
                    x += 5 + (hpLen > mpLen? hpLen: mpLen);
                    if(anchor == Graphics.TOP){
                        y = 0;
                    }else if(anchor == Graphics.BOTTOM){
                        y = viewHeight - BATTLEICON_FRAME_HEIGHT;
                    }
                    y += BATTLEICON_FRAME_HEIGHT - 2 - GameState.CHAR_HEIGHT;
                    g.setFont(GameState.font);
                    //#if (Directory == SE-K500) || (Directory == SE-K300) || (Directory == Nokia403)
                    //# draw3DString(g, "忠:" + player.petCurrent.fealty , x-5, y, Graphics.TOP | Graphics.LEFT, 0x000000, 0xffffff);
                    //#else
                    draw3DString(g, "忠诚度：" + player.petCurrent.fealty /*+ "  " + PetSprite.FEALTY_NAMES[player.petCurrent.getFealtyLevel()]*/, x, y, Graphics.TOP | Graphics.LEFT, 0x000000, 0xffffff);
                    //#endif
                }
            }
        }
    }

    private static final byte SETTARGETTYPE_FORWARD = 1;
    private static final byte SETTARGETTYPE_BACKWARD = 2;
    private static final byte SETTARGETTYPE_NONE = 0;

    private byte setTargetFocus(BattleSprite[] target, int currentSelect, boolean relive, byte setType){
        byte ret = -1;

        if(relive){
            if(target[currentSelect] != null /*&& target[currentSelect].testDie()*/){
                ret = (byte)currentSelect;
            }
        }else{
            if(target[currentSelect] != null && !target[currentSelect].testDie() && !target[currentSelect].testCatched()){
                ret = (byte)currentSelect;
            }
        }

        if(setType == SETTARGETTYPE_NONE){
            if(ret == currentSelect){
                return ret;
            }else{
                currentSelect = 0;
            }
        }

        int i = currentSelect;
        int inci = 1;

        if(setType == SETTARGETTYPE_FORWARD){
            i++;
        }else if(setType == SETTARGETTYPE_BACKWARD){
            i--;
            inci = -1;
        }

        for(; (setType == SETTARGETTYPE_FORWARD || setType == SETTARGETTYPE_NONE)? (i < target.length): (i >= 0); i += inci){
            if(target[i] == null){
                continue;
            }else{
                if(relive){
                    /*if(target[i].testDie()){*/
                    ret = (byte)i;
                    break;
                    /*}*/
                }else{
                    if(!target[i].testDie() && !target[i].testCatched()){
                        ret = (byte)i;
                        break;
                    }
                }
            }
        }

        return (byte)ret;
    }

    public void drawGameMenu(Graphics g, int iconOffset, boolean showSelect){
        //#if (Directory == MT-General) || (Directory == Midp2-General) || (Directory == SE-K500) || (Directory == SE-K300) || (Directory == Nokia403) || (MIDP2Common == true) || (Directory == ClientTouch-E680) || (Directory == ClientTouch--Midp2-General) || (Directory == ClientTouch-SE-General) || (Directory == ClientTouch-Nokia5800)
        BATTLEICON_X = (short)(viewWidth / 2);
        BATTLEICON_Y = (short)(viewHeight * 2 / 3);
        BATTLEICON_FRAME_WIDTH = viewWidth;
        //#endif

        //#if (Directory == SE-K500) || (Directory == SE-K300) || (Directory == Nokia403)
        BATTLEICON_Y = (short)(viewHeight >> 1);
        //#endif

        int[] iconX;
        int[] iconY;
        int[] iconFrame;

        if(currentActionType == 0){
            iconX = new int[5];
            iconY = new int[5];
            iconFrame = new int[5];

            iconX[0] = BATTLEICON_X - iconOffset;
            iconY[0] = BATTLEICON_Y - iconOffset;
            iconFrame[0] = BATTLEICON_SKILL;

            iconX[1] = BATTLEICON_X + iconOffset;
            iconY[1] = BATTLEICON_Y - iconOffset;
            iconFrame[1] = BATTLEICON_ITEM;

            iconX[2] = BATTLEICON_X - iconOffset;
            iconY[2] = BATTLEICON_Y + iconOffset;
            iconFrame[2] = BATTLEICON_CATCH;

            iconX[3] = BATTLEICON_X + iconOffset;
            iconY[3] = BATTLEICON_Y + iconOffset;
            iconFrame[3] = BATTLEICON_RUN;

            iconX[4] = BATTLEICON_X;
            iconY[4] = BATTLEICON_Y;
            iconFrame[4] = BATTLEICON_ATTACK;

        }else{

            iconX = new int[3];
            iconY = new int[3];
            iconFrame = new int[3];

            iconX[0] = BATTLEICON_X - iconOffset;
            iconY[0] = BATTLEICON_Y;
            iconFrame[0] = BATTLEICON_SKILL;

            iconX[1] = BATTLEICON_X + iconOffset;
            iconY[1] = BATTLEICON_Y;
            iconFrame[1] = BATTLEICON_DEF;

            iconX[2] = BATTLEICON_X;
            iconY[2] = BATTLEICON_Y;
            iconFrame[2] = BATTLEICON_ATTACK;
        }

        for(int i = 0; i < iconFrame.length; i++){
            battleIcon.drawFrame(g, iconFrame[i], iconX[i], iconY[i], Graphics.VCENTER | Graphics.HCENTER);
            //#if TouchScreen == true
            if(!sealBattleSkill(iconFrame[i])){
            	StaticUtils.addButton(2000+i, iconX[i]-12, iconY[i]-12, sealImage.getWidth(), sealImage.getWidth());
            }
            //#endif
            if(battleMenuSelectIndex == iconFrame[i]){
                int f = (int)(tick / 5) % 2;
                f = BATTLEICON_CURSOR[f];
                battleIcon.drawFrame(g, f, iconX[i], iconY[i], Graphics.VCENTER | Graphics.HCENTER);
            }

            if(sealBattleSkill(iconFrame[i])){
                g.drawImage(sealImage, iconX[i], iconY[i], Graphics.VCENTER | Graphics.HCENTER);
            }
        }
    }

    public boolean sealBattleSkill(int frame){
        boolean result = false;

        if(currentActionType == 0){
            switch(frame){
                case BATTLEICON_SKILL:
                    if((battleSkillSeal & SEAL_SKILL_SKILL) != 0){
                        result = true;
                    }

                    break;
                case BATTLEICON_ITEM:
                    if((battleSkillSeal & SEAL_SKILL_ITEM) != 0){
                        result = true;
                    }

                    break;
                case BATTLEICON_CATCH:
                    if((battleSkillSeal & SEAL_SKILL_CATCH) != 0){
                        result = true;
                    }

                    break;
                case BATTLEICON_RUN:
                    if((battleSkillSeal & SEAL_SKILL_RUNAWAY) != 0){
                        result = true;
                    }

                    break;
                case BATTLEICON_ATTACK:
                    if((battleSkillSeal & SEAL_SKILL_ATTACK) != 0){
                        result = true;
                    }

                    break;
            }
        }else{
            switch(frame){
                case BATTLEICON_SKILL:
                    if((battlePetSkillSeal & SEAL_SKILL_SKILL) != 0){
                        result = true;
                    }

                    break;
                case BATTLEICON_DEF:
                    if((battlePetSkillSeal & SEAL_SKILL_DEF) != 0){
                        result = true;
                    }

                    break;
                case BATTLEICON_ATTACK:
                    if((battlePetSkillSeal & SEAL_SKILL_ATTACK) != 0){
                        result = true;
                    }

                    break;
            }
        }

        return result;
    }

    public static int skillWidth = 0;
    public static int skillHeight = 0;
    public static int SKILLMENU_TOP = 30;

    public void drawSkillMenu(Graphics g){
    	//#if TouchScreen == true
    	StaticUtils.removeAllButton();
    	StaticUtils.beginButtonSetting();
    	  //#endif
        drawMenu(g, battleState == BATTLESTATE_SHOWSKILLLIST? showBattleSkillsName: battleSkillsName, -1, -1, battleState == BATTLESTATE_SHOWSKILLLIST? showBattleSkillSelectIndex: battleSkillLevelIndex, 0);
    }

    public void drawItemMenu(Graphics g){
        int left = -1/*viewWidth - skillWidth - 10*/;
        int mtop = -1/*SKILLMENU_TOP*/;

        drawMenu(g, battleItems, left, mtop, battleItemSelectIndex, 0, true);
    }

    public static int menuShowBegin;

    public static void drawMenu(Graphics g, Object[] menuItems, int left, int top, int selectID, int dir){
        drawMenu(g, menuItems, left, top, selectID, dir, false);
    }

    public static void drawMenu(Graphics g, Object[] menuItems, int left, int top, int selectID, int dir, boolean showNum){
        int width = 0;
        int height = 0;
        int clipSize = -1;
        boolean autoCalcY = false;
        boolean autoCalcX = false;
        for(int i = 0; i < menuItems.length; i++){
            if(menuItems[i] instanceof String){
                int sw = GameState.font.stringWidth((String)menuItems[i]);
                if(width < sw){
                    width = sw;
                }
            }else if(menuItems[i] instanceof GameItem){
                GameItem item = (GameItem)menuItems[i];
                int sw = GameState.font.stringWidth(item.getName(showNum, -1));
                if(width < sw){
                    width = sw;
                }
            }
        }

        width += 30;

        int lh = GameState.LINE_HEIGHT;
        height = menuItems.length * lh + 6;

        if(top == -1){
            autoCalcY = true;
            top = (viewHeight - height) / 2;
        }

        if(top < 0)
            top = 0;

        if(left == -1){
            autoCalcX = true;
            left = (viewWidth - width) / 2;
        }

        if(left < 0)
            left = 0;

        if(left + width > viewWidth){
            width = viewWidth - left;
        }

        if((top + height) > viewHeight - 20){
            height = (viewHeight - top - 20) / lh * lh;
            if(height > viewHeight - 20){
                height = (viewHeight - 20) / lh * lh;
            }

            clipSize = height / lh;
            height += 6;
            if(top + height > viewHeight){
                top = viewHeight - height;
            }
            if(autoCalcY){
                top = (viewHeight - height) / 2;
            }
        }

        if(clipSize == -1){
            clipSize = menuItems.length;
        }

        menuShowBegin = 0;

        if(selectID == 0){
            menuShowBegin = 0;
        }else if(selectID >= menuShowBegin + clipSize){
            menuShowBegin = selectID - clipSize + 1;
        }else if(selectID < menuShowBegin){
            menuShowBegin = selectID;
        }

        int arrowTop = -1;
        int arrowBottom = -1;
        if(menuShowBegin > 0){
            arrowTop = top + 4 + ((int)(tick / 5) % 2) * 5;
            width += World.bodyImg.getWidth(6) + 4;
            if(autoCalcX){
                left = (viewWidth - width) / 2;
            }
        }

        if(menuShowBegin + clipSize < menuItems.length){
            arrowBottom = top + height - 4 - ((int)(tick / 5) % 2) * 5;
            if(arrowTop == -1)
                width += World.bodyImg.getWidth(6) + 4;
            if(autoCalcX){
                left = (viewWidth - width) / 2;
            }
        }

        g.setColor(GameState.EDGE_COLOR[4]);
        g.fillRect(left + 2, top + 2, width - 4, height - 4);

        g.setFont(GameState.font);
        for(int i = menuShowBegin; i < menuShowBegin + clipSize; i++){
            if(i == selectID){
                g.setColor(GameState.EDGE_COLOR[5]);
                int t = top + lh * (i - menuShowBegin) + 3;
                int h = lh;
                if(i == menuShowBegin){
                    t -= 3;
                    h += 3;
                }else if(i == menuShowBegin + clipSize - 1){
                    h += 3;
                }
                g.fillRect(left + 3, t, width - 6, h);
            }
            if(menuItems[i] instanceof String){
                draw3DString(g, menuItems[i].toString(), left + 15, top + lh * (i - menuShowBegin) + 3 + (GameState.LINE_HEIGHT - GameState.CHAR_HEIGHT) / 2, Graphics.TOP | Graphics.LEFT, 0x000000,
                                0xffffff);
              //#if TouchScreen == true
               StaticUtils.addButton(2000+menuShowBegin+i, left + 15, top + lh * (i - menuShowBegin) + 3 + (GameState.LINE_HEIGHT - GameState.CHAR_HEIGHT) / 2,  GameState.CHAR_WIDTH*(menuItems[i].toString().length()-1), GameState.CHAR_HEIGHT);
                //#endif
            }else if(menuItems[i] instanceof GameItem){
                GameItem item = (GameItem)menuItems[i];
                item.drawName(g, left + 15, top + lh * (i - menuShowBegin) + 3 + (GameState.LINE_HEIGHT - GameState.CHAR_HEIGHT) / 2, showNum, false, width - 6, false);
              //#if TouchScreen == true
                StaticUtils.addButton(2000+menuShowBegin+i, left + 15, top + lh * (i - menuShowBegin) + 3 + (GameState.LINE_HEIGHT - GameState.CHAR_HEIGHT) / 2, GameState.CHAR_WIDTH*(item.getName(showNum, width-6).length()), GameState.CHAR_HEIGHT);
                //#endif
            }
        }

        if(arrowTop != -1)
            World.bodyImg.drawFrame(g, 6, left + width - 4, arrowTop, Graphics.TOP | Graphics.RIGHT);
        if(arrowBottom != -1)
            World.bodyImg.drawFrame(g, 7, left + width - 4, arrowBottom, Graphics.BOTTOM | Graphics.RIGHT);

        GameState.drawEdge(g, 0, left, top, width, height, false, 0);

    }

    /**
     * 计算出现的部队，放入battleSprite数组中
     * @param index
     */
    public void initBattleSprites(int index) throws IOException{
        armySprite = new ArmySprite[3];
        MonsterSprite ms = monsters[index];

        int id[] = new int[3];
        for(int i = 0; i < ms.armys.length; i++){
            int rnd = random(0, 100);
            if(rnd <= ms.armys[i][1]){
                id[i] = ms.armys[i][0];
            }else{
                id[i] = -1;
            }
        }

        ByteArrayInputStream bis = new ByteArrayInputStream((byte[])_packageCache.get("m.d"));
        DataInputStream dis = new DataInputStream(bis);

        int n = dis.readByte();
        ArmySprite[] m = new ArmySprite[n];
        for(int i = 0; i < n; i++){
            m[i] = new ArmySprite();
            m[i].load(dis);

        }

        int anum = 0;
        for(int i = 0; i < armySprite.length; i++){
            if(id[i] >= 0){
                anum++;
                armySprite[i] = new ArmySprite(m[id[i]]);
                armySprite[i].id = (byte)id[i];
                armySprite[i].setLocalIndex((byte)(i + 1));
            }
        }

        if(anum == 1){
            for(int i = 0; i < armySprite.length; i++){
                if(armySprite[i] != null){
                    armySprite[i].setLocalIndex((byte)2);
                }
            }
        }

        //TODO delete
        for(int i = 0; i < playerSprite.length; i++){
            if(playerSprite[i] != null){
                if(playerSprite[i] instanceof Sprite){
                    if(playerSprite[i].petCurrent != null && playerSprite[i].petCurrent.canBattle()){
                        playerPet[i] = playerSprite[i].petCurrent;
                    }else{
                        playerPet[i] = null;
                    }
                }
            }
        }
    }

    public void readyBattle(int pkid){
        //#if LoadAllImage == FALSE
        clearDefaultImageSet();
        initBattleImageSet();
        //#endif
        //#if (Directory == SE-K700) || (Directory == SE-K500) || (Directory == SE-K300) || (Directory == SE-S700)
      //# Sprite.bufIcon = null;
      //# resourceImage = new ImageSet[4];
      //# taskHint = null;
      //# bodyImg =null;
      //# charImageSet = null;
      //# doorImageSet = null;
      //# effectImageSet = getImageSetFromLocal("effect.p");
      //# dieImageSet = getImageSetFromLocal("die.p");
      //# World.attackImg[0] = getImageSetFromLocal("da_male");
      //# World.attackImg[1] = getImageSetFromLocal("da_female");
      //# World.attackWeaponImg[0] = getImageSetFromLocal("da_male_weapon");
        //#if (Directory == NK-6681) || (Directory == MT-V300)
        //# World.attackWeaponImg[1] = World.attackWeaponImg[0];
        //#else
      //# World.attackWeaponImg[1] = getImageSetFromLocal("da_female_weapon");
        //#endif
        //#endif
        player.battleStart(Sprite.LEFT);
        player.setLocalIndex(LOCATION_INDEX_MIDDLE);
        if(player.petCurrent != null){
            player.petCurrent.battleStart(Sprite.LEFT);
            player.petCurrent.setLocalIndex(LOCATION_INDEX_MIDDLE);
        }

        battleState = BATTLESTATE_MENU;
        targetSelect = 0;
        iconOffset = 0;
        battleMenuSelectIndex = playerMenuSelect;
        nowBattle = pkid;
        battleBackgroundInitOver = false;
        battleBout = 0;
        battleCanAction = true;
        battlePetCanAction = true;

        battleIcon = getImageSetFromLocal("battleIcon");
      //#if (Directory == SE-K700) || (Directory == SE-K500) || (Directory == SE-K300) || (Directory == SE-S700)
      //# playerImageSet[0] = null;
      //# playerImageSet[1] = null;
      //# netPlayers.clear();
      //# netPlayersVector.removeAllElements();
      //#endif

        if(monsters != null){
            for(int i = 0; i < monsters.length; i++){
                monsters[i].cycle(_elapsedTime, this);
            }
        }
    }

    public void saveNetChatInputData(){
        if(chat_input_doing){
            Displayable curr = World.display.getCurrent();

            if(curr instanceof Form){
                Form form = (Form)curr;

                if(GameState._formTitle != null && GameState._formTitle.equals("发起聊天") || GameState._formTitle.equals("回复密聊")){
                    String info = ((TextField)form.get(0)).getString().trim();
                    int channel = ((ChoiceGroup)form.get(1)).getSelectedIndex();

                    World.chat_input_uncompelte_message = info;
                    World.net_chat_current_channel = channel;
                }

            }

            chat_input_doing = false;
        }
    }

    public void createBattleItemListInfo(){
        Vector vec = new Vector();
        for(int i = 0; i < player.basicItems.size(); i++){
            GameItem gi = (GameItem)player.basicItems.elementAt(i);
            if(gi.canUse() && gi.type == GameItem.TYPE_BASIC){
                vec.addElement(gi);
            }
        }
        battleItems = new Object[vec.size()];

        vec.copyInto(battleItems);

        if(battleItemSelectIndex >= battleItems.length){
            battleItemSelectIndex = 0;
        }

    }

    public void createPetSkillsInfo(){
        if(player.petCurrent == null){
            battleSkillsID = new short[0];
            battleSkillsName = new String[0];

            return;
        }

        battleSkillsID = new short[player.petCurrent.skillList.length];
        battleSkillsName = new String[player.petCurrent.skillList.length];

        if(battleSkillSelectIndex >= battleSkillsID.length){
            battleSkillSelectIndex = 0;
        }

        for(int i = 0; i < player.petCurrent.skillList.length; i++){
            int id = player.petCurrent.skillList[i];
            battleSkillsID[i] = (short)id;
            battleSkillsName[i] = (i + 1) + "." + Skill.getSkillName(player.petCurrent, battleSkillsID[i], Skill.SHOW_MPUSE_LIST, false);
            int nw = GameState.font.stringWidth("9." + battleSkillsName[i]) + 5;

            if(nw > skillWidth){
                skillWidth = nw;
            }
        }
    }

    public void createPetSkillsLevelInfo(){
        return;
    }

    public void createBattleSkillsInfo(){
        battleSkillsID = new short[player.skillTable.length];
        battleSkillsName = new String[player.skillTable.length];

        if(battleSkillSelectIndex >= battleSkillsID.length){
            battleSkillSelectIndex = 0;
        }

        for(int i = 0; i < player.skillTable.length; i++){
            int id = player.skillTable[i][4];
            int maxLv = player.skillTable[i][1];
            int curLv = player.skillTable[i][2];
            id = id - (maxLv - curLv);
            battleSkillsID[i] = (short)id;
            battleSkillsName[i] = (i + 1) + "." + Skill.getSkillName(player, battleSkillsID[i], Skill.SHOW_MPUSE_LIST, true);
            int nw = GameState.font.stringWidth("9." + battleSkillsName[i]) + 5;
            if(nw > skillWidth)
                skillWidth = nw;
        }
    }

    public void createBattleSkillsLevelInfo(){
        int maxLevel = player.skillTable[battleSkillSelectIndex][1];
        short maxID = player.skillTable[battleSkillSelectIndex][4];
        battleSkillLevelIndex = (byte)(player.skillTable[battleSkillSelectIndex][2] - 1);

        battleSkillsID = new short[maxLevel];
        battleSkillsName = new String[maxLevel];

        for(int i = maxLevel; i > 0; i--){
            battleSkillsID[i - 1] = maxID--;
            battleSkillsName[i - 1] = i + "." + Skill.getSkillName(player, battleSkillsID[i - 1], Skill.SHOW_MPUSE_LIST, true);
            int nw = GameState.font.stringWidth("9." + battleSkillsName[i - 1]) + 5;
            if(nw > skillWidth)
                skillWidth = nw;
        }
    }

    public void startBattle(int index) throws IOException{
        if(monsters[index] != null){
            GameEvent event = new GameEvent(GameEvent.EVENT_BATTLE, 1, 0);
            event.idata[0] = index;
            addEvent(event);
        }
    }

    public void endBattle(){
        nowBattle = -1;
        bgImg = null;

        for(int i = 0; i < playerSprite.length; i++){
            if(playerSprite[i] != null){
                playerSprite[i].battleEnd();
                if(playerSprite[i].petCurrent != null){
                    playerSprite[i].petCurrent.battleEnd();
                }
            }
        }

        for(int i = 0; i < playerPet.length; i++){
            if(playerPet[i] != null){
                playerPet[i].battleEnd();
            }
        }

        playerSprite = new Sprite[]{
                        player, null, null
        };

        playerPet = new PetSprite[3];

        armyPet = new PetSprite[3];

        if(pkBattle){
            player.showHp = true;
        }

        armySprite = null;

        battleSkillsID = null;
        battleSkillsName = null;

        pkBattle = false;
        World.player_PKID = 0;
        battleIcon = null;
    }

    public Vector battleMovie = new Vector();
    public static Hashtable spriteDoneSkill = new Hashtable();

    public static BattleSprite getSpriteFromOrder(int oppGroup, int oppIndex, BattleSprite[] our, BattleSprite[] them, BattleSprite[] ourPet, BattleSprite[] themPet){
        BattleSprite result = null;

        switch(oppGroup){
            case BattleSprite.GROUP_OUR:
                result = our[oppIndex];

                break;
            case BattleSprite.GROUP_THEM:
                result = them[oppIndex];

                break;
            case BattleSprite.GROUP_OUR_PET:
                result = ourPet[oppIndex];

                break;
            case BattleSprite.GROUP_THEM_PET:
                result = themPet[oppIndex];

                break;
        }

        return result;
    }

    public static boolean testOurSideFromOrder(int oppGroup){
        if(oppGroup == BattleSprite.GROUP_OUR || oppGroup == BattleSprite.GROUP_OUR_PET){
            return true;
        }else{
            return false;
        }
    }

    public boolean battleBout(BattleSprite[] our, BattleSprite[] them, BattleSprite[] ourPet, BattleSprite[] themPet, int bout){
        try{
            Skill.players = our;
            Skill.playerPets = ourPet;
            Skill.monsters = them;
            Skill.monsterPets = themPet;

            battleMovie.removeAllElements();
            spriteDoneSkill.clear();

            int[][] battleOrder = new int[our.length + them.length + ourPet.length + themPet.length][2]; //[0] 0：our，1：them，2: ourPete, 3: themPet, [1] index
            int offset = 0;
            boolean battleBoutOver = false;

            for(int i = 0; i < our.length; i++){
                battleOrder[i][0] = BattleSprite.GROUP_OUR;
                battleOrder[i][1] = i - offset;

                if(our[i - offset] != null){
                    our[i - offset].groupIndex = i - offset;
                }
            }

            offset += our.length;

            for(int i = offset; i < offset + them.length; i++){
                battleOrder[i][0] = BattleSprite.GROUP_THEM;
                battleOrder[i][1] = i - offset;

                if(them[i - offset] != null){
                    them[i - offset].groupIndex = i - offset;
                }
            }

            offset += them.length;

            for(int i = offset; i < offset + ourPet.length; i++){
                battleOrder[i][0] = BattleSprite.GROUP_OUR_PET;
                battleOrder[i][1] = i - offset;

                if(ourPet[i - offset] != null){
                    ourPet[i - offset].groupIndex = i - offset;
                }
            }

            offset += ourPet.length;

            for(int i = offset; i < offset + themPet.length; i++){
                battleOrder[i][0] = BattleSprite.GROUP_THEM_PET;
                battleOrder[i][1] = i - offset;

                if(themPet[i - offset] != null){
                    themPet[i - offset].groupIndex = i - offset;
                }
            }

            battleBoutOver = false;

            for(int i = 0; i < battleOrder.length; i++){
                boolean flag = testOurSideFromOrder(battleOrder[i][0]);

                BattleSprite bs = getSpriteFromOrder(battleOrder[i][0], battleOrder[i][1], our, them, ourPet, themPet);

                if(bs == null){
                    continue;
                }

                if(bs.skillId == Skill.SKILL_NOT_READY && !bs.testCannotBattle()){
                    if(flag){
                        battleBoutOver = Skill.chooseSkill(bs, battleOrder[i][1], our, them, ourPet, themPet, battleMovie);
                    }else{
                        battleBoutOver = Skill.chooseSkill(bs, battleOrder[i][1], them, our, themPet, ourPet, battleMovie);
                    }
                }

                if(battleBoutOver){
                    break;
                }
            }

            if(battleBoutOver){
                return true;
            }

            for(int i = 0; i < battleOrder.length - 1; i++){
                for(int j = i; j < battleOrder.length; j++){
                    BattleSprite t1, t2;

                    t1 = getSpriteFromOrder(battleOrder[i][0], battleOrder[i][1], our, them, ourPet, themPet);
                    t2 = getSpriteFromOrder(battleOrder[j][0], battleOrder[j][1], our, them, ourPet, themPet);

                    if(t1 == null || t2 == null){
                        if(t1 == null){
                            int[] tmp = battleOrder[i];
                            battleOrder[i] = battleOrder[j];
                            battleOrder[j] = tmp;
                        }

                        continue;
                    }

                    int speed1 = t1.getSpeed();
                    int speed2 = t2.getSpeed();

                    if(speed1 < speed2){
                        if((t1.canAction() && t2.canAction()) || (!t1.canAction() && !t2.canAction())){
                            int[] tmp = battleOrder[i];
                            battleOrder[i] = battleOrder[j];
                            battleOrder[j] = tmp;
                        }else if(!t1.canAction()){
                            int[] tmp = battleOrder[i];
                            battleOrder[i] = battleOrder[j];
                            battleOrder[j] = tmp;
                        }
                    }else if(speed1 == speed2){
                        if(World.getPercentRate(50)){
                            int[] tmp = battleOrder[i];
                            battleOrder[i] = battleOrder[j];
                            battleOrder[j] = tmp;
                        }
                    }
                }
            }

            battleBoutOver = false;

            for(int i = 0; i < battleOrder.length; i++){
                boolean flag = testOurSideFromOrder(battleOrder[i][0]);

                BattleSprite bs = getSpriteFromOrder(battleOrder[i][0], battleOrder[i][1], our, them, ourPet, themPet);

                if(bs == null){
                    continue;
                }

                if(bs.skillId != Skill.SKILL_NOT_READY && !bs.testCannotBattle()){
                    if(flag){
                        battleBoutOver = Skill.doPoisonFrost(bs, battleOrder[i][1], our, them, ourPet, themPet, battleMovie);
                    }else{
                        battleBoutOver = Skill.doPoisonFrost(bs, battleOrder[i][1], them, our, themPet, ourPet, battleMovie);
                    }

                    if(bs.testCannotBattle()){
                        continue;
                    }else{
                        if(!bs.canAction()){
                            if(flag){
                                battleBoutOver = Skill.chooseSkill(bs, battleOrder[i][1], our, them, ourPet, themPet, battleMovie);
                            }else{
                                battleBoutOver = Skill.chooseSkill(bs, battleOrder[i][1], them, our, themPet, ourPet, battleMovie);
                            }

                            if(battleBoutOver){
                                break;
                            }
                        }

                        if(flag){
                            battleBoutOver = Skill.doSkill(bs, our, them, ourPet, themPet, battleMovie, bout);
                        }else{
                            battleBoutOver = Skill.doSkill(bs, them, our, themPet, ourPet, battleMovie, bout);
                        }

                        if(battleBoutOver){
                            break;
                        }
                    }
                }

                if(battleBoutOver){
                    break;
                }
            }

            if(battleBoutOver){
                return true;
            }

            battleBoutOver = true;

            for(int i = 0; i < our.length; i++){
                if(our[i] == null){
                    continue;
                }

                if(!our[i].testCannotBattle()){
                    battleBoutOver = false;

                    break;
                }
            }

            if(battleBoutOver){
                return true;
            }

            battleBoutOver = true;

            for(int i = 0; i < them.length; i++){
                if(them[i] == null){
                    continue;
                }

                if(!them[i].testCannotBattle()){
                    battleBoutOver = false;

                    break;
                }
            }

            if(battleBoutOver){
                return true;
            }

            return false;
        }finally{
            Enumeration emu = spriteDoneSkill.keys();

            while(emu.hasMoreElements()){
                BattleSprite bs = (BattleSprite)emu.nextElement();
                Integer groupIndex = (Integer)spriteDoneSkill.get(bs);

                bs.processBattleBuf(battleMovie, groupIndex.intValue());
            }

            for(int i = 0; i < our.length; i++){
                BattleSprite bs = our[i];

                if(bs == null){
                    continue;
                }

                bs.clearBout(battleMovie, i);
            }

            for(int i = 0; i < them.length; i++){
                BattleSprite bs = them[i];

                if(bs == null){
                    continue;
                }

                bs.clearBout(battleMovie, i);
            }

            for(int i = 0; i < ourPet.length; i++){
                BattleSprite bs = ourPet[i];

                if(bs == null){
                    continue;
                }

                bs.clearBout(battleMovie, i);
            }

            for(int i = 0; i < themPet.length; i++){
                BattleSprite bs = themPet[i];

                if(bs == null){
                    continue;
                }

                bs.clearBout(battleMovie, i);
            }

            Skill.players = null;
            Skill.playerPets = null;
            Skill.monsters = null;
            Skill.monsterPets = null;
        }
    }

    public static void spriteDoneSkill(BattleSprite bs, int index, boolean force){
        if(force || ((bs.getDebufStatus() != Skill.STATUS_NORMAL || bs.getBufStatus() != Skill.STATUS_NORMAL || bs.bufTable.size() > 2) && !bs.testCannotBattle())){
            spriteDoneSkill.put(bs, new Integer(index));
        }
    }

    //#mdebug
    public void printBattleMovie(BattleSprite our[], BattleSprite them[], BattleSprite ourPet[], BattleSprite themPet[], Vector movie, int bout, boolean over){
        if(!SHOW_BATTLELOG)
            return;
        System.out.println();
        System.out.println("第" + bout + "回合");

        for(int i = 0; i < our.length; i++){
            if(our[i] == null){
                continue;
            }
            System.out.println("主角方 " + i + "号：生命 " + our[i].hp + " , 魔法 " + our[i].mp + " , 状态 " + Skill.getStatusName(our[i].getDebufStatus()) + " 附加有益状态 "

            + Skill.getStatusName(our[i].getBufStatus()));

            System.out.println("  属性加成：　物攻 " + our[i].attackAdd + " 魔攻 " + our[i].magicAttackAdd + " 物防 " + our[i].defenceAdd + " 魔防 " + our[i].magicDefenceAdd + " 伤害 "

            + (our[i].phyDamageAdd + our[i].mgcDamageAdd) + " 命中 "

            + our[i].hitAdd + " 闪避 " + our[i].fleeAdd + " 暴击 " + our[i].criRateAdd);

            System.out.println();
        }
        for(int i = 0; i < ourPet.length; i++){
            if(ourPet[i] == null){
                continue;
            }
            System.out.println("主角方宠物 " + i + "号：生命 " + ourPet[i].hp + " , 魔法 " + ourPet[i].mp + " , 状态 " + Skill.getStatusName(ourPet[i].getDebufStatus()) + " 附加有益状态 "

            + Skill.getStatusName(ourPet[i].getBufStatus()));

            System.out.println("  属性加成：　物攻 " + ourPet[i].attackAdd + " 魔攻 " + ourPet[i].magicAttackAdd + " 物防 " + ourPet[i].defenceAdd + " 魔防 " + ourPet[i].magicDefenceAdd + " 伤害 "

            + (ourPet[i].phyDamageAdd + ourPet[i].mgcDamageAdd) + " 命中 " + ourPet[i].hitAdd + " 闪避 " + ourPet[i].fleeAdd + " 暴击 " + ourPet[i].criRateAdd);

            System.out.println();

        }
        for(int i = 0; i < them.length; i++){
            if(them[i] == null){

                continue;

            }
            System.out.println("怪物方 " + i + "号：生命 " + them[i].hp + " , 魔法 " + them[i].mp + " , 状态 " + Skill.getStatusName(them[i].getDebufStatus()) + " 附加有益状态 "

            + Skill.getStatusName(them[i].getBufStatus()));

            System.out.println("  属性加成：　物攻 " + them[i].attackAdd + " 魔攻 " + them[i].magicAttackAdd + " 物防 " + them[i].defenceAdd + " 魔防 " + them[i].magicDefenceAdd + " 伤害 "

            + (them[i].phyDamageAdd + them[i].mgcDamageAdd)

            + " 命中 " + them[i].hitAdd + " 闪避 " + them[i].fleeAdd + " 暴击 " + them[i].criRateAdd);

            System.out.println();
        }
        for(int i = 0; i < themPet.length; i++){
            if(themPet[i] == null){
                continue;
            }
            System.out.println("怪物方宠物 " + i + "号：生命 " + themPet[i].hp + " , 魔法 " + themPet[i].mp + " , 状态 " + Skill.getStatusName(themPet[i].getDebufStatus()) + " 附加有益状态 "

            + Skill.getStatusName(themPet[i].getBufStatus()));

            System.out.println("  属性加成：　物攻 " + themPet[i].attackAdd + " 魔攻 " + themPet[i].magicAttackAdd + " 物防 " + themPet[i].defenceAdd + " 魔防 " + themPet[i].magicDefenceAdd + " 伤害 "

            + (themPet[i].phyDamageAdd + themPet[i].mgcDamageAdd) + " 命中 " + themPet[i].hitAdd + " 闪避 " + themPet[i].fleeAdd + " 暴击 " + themPet[i].criRateAdd);

            System.out.println();

        }
        System.out.println();
        for(int i = 0; i < battleMovie.size(); i++){
            String group1 = "";
            String group2 = "";
            String skillName = "";
            String pos = "";
            String back = "";
            String speed = "";
            String hit = "";
            String status = "";
            String cri = "";
            String animate = "";
            int[] tmp = (int[])battleMovie.elementAt(i);
            if(tmp[0] == BattleSprite.TYPE_PLAYER || tmp[0] == BattleSprite.TYPE_NET_PLAYER){

                group1 = "主角方 ";
            }else if(tmp[0] == BattleSprite.TYPE_PLAYER_PET){

                group1 = "主角方宠物 ";

            }else if(tmp[0] == BattleSprite.TYPE_MONSTER_PET){

                group1 = "怪物方宠物 ";

            }else{
                group1 = "怪物方 ";
            }
            group1 += tmp[1] + "号";
            if(tmp[2] == BattleSprite.TYPE_PLAYER || tmp[2] == BattleSprite.TYPE_NET_PLAYER){

                group2 = "主角方 ";

            }else if(tmp[2] == BattleSprite.TYPE_PLAYER_PET){
                group2 = "主角方宠物 ";
            }else if(tmp[2] == BattleSprite.TYPE_MONSTER_PET){
                group2 = "怪物方宠物 ";
            }else{
                group2 = "怪物方 ";
            }
            group2 += tmp[3] + "号";
            skillName = Skill.getSkillName(null, tmp[4], Skill.SHOW_MPUSE_NONE, true);

            animate = Skill.getAnimateName(tmp[5]);

            pos = tmp[6] == Skill.POSITION_DEST? "冲过去": "在原地";

            back = tmp[7] == Skill.OVER_POSITION_BACK? "后回到原地": "后留在那里";

            speed = tmp[8] == Skill.MOVIE_SPEED_FAST? "快速动画": "正常动画";

            hit = tmp[9] == Skill.HIT_HIT? "命中了": "没有命中";

            status = Skill.getStatusName(tmp[10]);

            cri = tmp[11] == Skill.ATTACK_CRI? "暴击": "没有暴击";

            System.out.println(group1 + pos + "对 " + group2 + "使用了" + skillName + back);

            System.out.println("结果 " + cri + " " + hit + " " + group2 + " " + status + "了");

            System.out.println(group1 + "的生命变化 " + tmp[12] + " 魔法变化 " + tmp[13]);

            System.out.println(group2 + "的生命变化 " + tmp[14] + " 魔法变化 " + tmp[15]);

            System.out.println("动画播放为：" + animate + " , " + speed);

            System.out.println();

        }
        bout++;
        if(over){
            System.out.println("战斗结束");
        }
        System.out.println();
    }

    //#enddebug

    protected void hideNotify(){
    	if(World.nowBattle <0){
            for(int i = 0; i < World.events.size(); i++){
                GameEvent e = (GameEvent)World.events.elementAt(i);
                if(e.getType() == GameEvent.EVENT_PAUSE){
                    return;
                }
            }
            //#if Revision == CMCC || (Revision == JIANGSUNCMCC)
            if(displayFirst == this){
                GameEvent event = new GameEvent(GameEvent.EVENT_PAUSE, 0, 0);
                World.addEvent(event);
            }
            //#endif
        }
        World.clearKeyStates();
    }

    protected void showNotify(){
        if(nowBattle > -1){
            for(int i = 0; i < events.size(); i++){
                GameEvent e = (GameEvent)events.elementAt(i);
                if(e.getType() == GameEvent.EVENT_PAUSE){
                    removeEvent(e);
                    break;
                }
            }
        }
    }

    /*---------- Battle Functions End -----------*/

    public static Image getPlayerHead(){
        if(playerHead == null){
            int face = 0;
            if(player != null){
                face = player.face;
            }
            try{
                if(World.playerHead == null){
                    int index = getFaceIndex(face, false);
                    if(index == 0){
                        playerHead = Image.createImage("/dh_male.png");
                    }else if(index == 1){
                        playerHead = Image.createImage("/dh_female.png");
                    }else{
                        Object[] tmp = (Object[])faceData[index];
                        byte[] idata = (byte[])tmp[4];
                        playerHead = Image.createImage(idata, 0, idata.length);
                    }
                }
            }catch(IOException e){
                //#debug
                e.printStackTrace();
            }
        }
        return playerHead;
    }

    public static void startServerPushTaskUI(byte connCode, int taskUINpcId){
        int eventType = -1;
        String uiTitle = "";

        switch(connCode){
            case GameState.CONN_ABILITY_LIST:
                eventType = GameEvent.EVENT_BATTLE_TEACHER;

                break;
            case GameState.CONN_SKILL_LIST:
                eventType = GameEvent.EVENT_PRODUCT_TEACHER;
                uiTitle = "训练生活技能";

                break;
            case GameState.CONN_SHOP_LIST:
                eventType = GameEvent.EVENT_SHOP;

                break;
            case GameState.CONN_AUCTION_TYPE_LIST:
                eventType = GameEvent.EVENT_AUCTION;

                break;
            case GameState.CONN_BUY_MATERIAL_TYPE_LIST:
                eventType = GameEvent.EVENT_BUY_MATERIAL;

                break;
            case GameState.CONN_OEM_TYPE_LIST:
                eventType = GameEvent.EVENT_OEM;

                break;
            case GameState.CONN_STORE_ITEM_LIST:
                eventType = GameEvent.EVENT_STORE;

                break;
            case GameState.CONN_REPAIRE_LIST:
                eventType = GameEvent.EVENT_REPAIR;

                break;
            case GameState.CONN_GENERIC_LIST:
                eventType = GameEvent.EVENT_GENERIC_LIST;

                break;
            case GameState.CONN_ISHOP_LIST:
                eventType = GameEvent.EVENT_ISHOP_LIST;

                break;
        }

        if(eventType > 0){
            createTaskUIEvent(eventType, taskUINpcId, uiTitle);
        }
    }

    public static void createTaskUIEvent(int eventType, int taskUINpcId, String uiTitle){
        GameEvent tmpEvent = new GameEvent(eventType, 2, 0);
        tmpEvent.idata[0] = taskUINpcId;
        tmpEvent.idata[1] = 0;
        tmpEvent.odata = new String[]{
            uiTitle
        };

        int taskId = tmpEvent.getEventTaskId();

        if(!World._gtvm.hasTask(taskId)){
            World.requestDownloadTask((short)taskId);
            //World.addLocalTask(taskId);
        }

        World.addEvent(tmpEvent);
    }

    public static void addLocalTask(int name){
        try{
            String etfName = "/" + String.valueOf(name) + "_v0.etf";
            World._gtvm.addTask(ETFFile.load(iTimesMIDlet.instance.getClass().getResourceAsStream(etfName)));
        }catch(Exception e){
            //#debug
            e.printStackTrace();
        }
    }

    public static void moveToEffectPosition(){
        if(player != null && currMapId > 0){
            String msg = "正在计算位置，请稍候......";

            if(player.getXPoint() > tileXCount || player.getYPoint() > tileYCount || player.x < 0 || player.y < 0){
                showDieConfirm(4000, (short)((areaId << 4) | (_defaultMapId & 0xF)), _defaultX, _defaultY, msg);
            }else{
                if(testCanMove(player.x, player.y)){
                    if(player.wpList != null){
                        showDieConfirm(4000, currMapId, player.getXPoint(), player.getYPoint(), msg);
                    }else{
                        showMessage("您没有卡死", (byte)1);
                    }
                }else{
                    int newTileX, newTileY;
                    int newX, newY;
                    int playerTileX, playerTileY;
                    int searchWidth;
                    boolean canMove = false;

                    playerTileX = player.getXPoint();
                    playerTileY = player.getYPoint();
                    newTileX = playerTileX;
                    newTileY = playerTileY;

                    for(searchWidth = 1; searchWidth < 5; searchWidth++){
                        for(newTileX = playerTileX - searchWidth; newTileX <= playerTileX + searchWidth; newTileX++){
                            for(newTileY = playerTileY - searchWidth; newTileY <= playerTileY + searchWidth; newTileY++){
                                if(Math.abs(newTileX - playerTileX) < searchWidth || Math.abs(newTileY - playerTileY) < searchWidth){
                                    continue;
                                }

                                newX = newTileX * tileWidth;
                                newY = newTileY * tileHeight;

                                if(testCanMove(newX, newY)){
                                    canMove = true;

                                    break;
                                }
                            }

                            if(canMove){
                                break;
                            }
                        }

                        if(canMove){
                            break;
                        }
                    }

                    if(canMove){
                        showDieConfirm(4000, currMapId, newTileX, newTileY, msg);
                    }else{
                        showDieConfirm(4000, (short)((areaId << 4) | (_defaultMapId & 0xF)), _defaultX, _defaultY, msg);
                    }
                }
            }
        }
    }

    public static boolean testCanMove(int x, int y){
        int xx, yy;
        int x1, y1;
        boolean canMove = false;
        ;

        for(int dir = Sprite.UP; dir <= Sprite.RIGHT; dir++){
            x1 = 0;
            y1 = 0;

            switch(dir){
                case UP:
                    x1 = 0;
                    y1 = -Sprite.STEP;

                    break;
                case DOWN:
                    x1 = 0;
                    y1 = Sprite.STEP;

                    break;
                case LEFT:
                    x1 = -Sprite.STEP;
                    y1 = 0;

                    break;
                case RIGHT:
                    x1 = Sprite.STEP;
                    y1 = 0;

                    break;
            }

            xx = x1 + x;
            yy = y1 + y;

            int realStep = instance.collisionMap(xx, yy - tileHeight, player.getWidth(), tileHeight, dir, Sprite.STEP, x, y - tileHeight);

            if(realStep > 1){
                canMove = true;

                break;
            }
        }

        return canMove;
    }
    public static void  drawLeftShadeArrow(Graphics g,String[] src ,short number){
           	int tempWidth =0;
           	for(int i = 0; i < src.length && src[i]!= null; i++){
           		int sw = GameState.font.stringWidth((String)src[i]);
           		if(tempWidth < sw){
           			tempWidth = sw;
                   }
           	}
           	tempWidth= tempWidth+30;
           	int tempLeft = (viewWidth - tempWidth) / 2;
           	if(tempLeft<0){
           		tempLeft=0;
           	}
           	int tempRight =tempWidth+ tempLeft;
           	if(tempRight>viewWidth){
           		tempRight=viewWidth;
           	}
           	if(tick%20==0){
           		g.setColor(0xF9FAFA);
           	}else{
           		g.setColor(GameState.EDGE_COLOR[0]);
           	}
           	for(int i = 0; i < 6; i++){
                   g.drawLine(tempLeft+1+i , viewHeight/2 - i, tempLeft+1+i ,  viewHeight/2 + i);
                   g.drawLine(tempRight-7+i , viewHeight/2-5 +i, tempRight-7 +i,  viewHeight/2+5-i);

            }
            //#if TouchScreen == true
            StaticUtils.addButton(3000, tempLeft, viewHeight/2-6,ok.getWidth() , ok.getHeight());
            StaticUtils.addButton(3001, tempRight-12, viewHeight/2-5 , ok.getWidth() , ok.getHeight());
            //#endif

    }
    //#if CommandEmu == true
    //# public static final Command commandMenu = new Command("菜单", Command.OK, 1);
    //# public static final Command commandOK = new Command("确定", Command.OK, 1);
    //# public static final Command commandBack = new Command("返回", Command.BACK, 2);
    //# public static final Command commandFollow = new Command("跟随队长", Command.BACK, 2);
    //# public static final Command commandLeave = new Command("独立行动", Command.BACK, 2);
    //# public static boolean commandEventProc = false;

    //# public void commandAction(Command command, Displayable display){
    //#     if(command.getCommandType() == Command.OK){
    //#         keyPressed(-6);
    //#         keyReleased(-6);
    //#     }else if(command.getCommandType() == Command.BACK){
    //#         keyPressed(-7);
    //#         keyReleased(-7);
    //#     }
    //# }

    //# public void addCommands(Command cmdLeft, Command cmdRight){
    //#     removeCommand(commandMenu);
    //#     removeCommand(commandOK);
    //#     removeCommand(commandBack);
    //#     removeCommand(commandFollow);
    //#     removeCommand(commandLeave);

    //#     if(cmdLeft != null){
    //#         addCommand(cmdLeft);
    //#     }

    //#     if(cmdRight != null){
    //#         addCommand(cmdRight);
    //#     }

    //#     setCommandListener(this);
    //# }
    //#endif
  
    
    //#if TouchScreen == true
    public static int shortcutsState = 0;		// 0 - 收起，1 - 展开
    public static Image ok ;//菜单选中 
    public static Image menu;
    public static Image turn ;//菜单返回
    public static Image chat ;
    public static Image autobattle;
    public static Image skilllevel;
    public static Image up;
    public static Image down;
    public static Image left;
    public static Image right;
    public static Image players;
    public static Image bag;
    public static Image smap;
    public static Image name;
    public static Image recall;
    public static Image msg;
    public static Image leftSpread;//左伸展 
    public static Image rightSpread;//右伸展
    public static Image imoney;//i币商店
    public static Image sayhello;
    
    
    /**
     * 绘制游戏中的热键菜单。包括打开菜单、发起聊天、周围玩家、地图开关、改变称号、回复密聊、聊天记录等。
     * @param g
     */
    public void drawShortcuts(Graphics g) {
    	int[] btnIcons= null;
    	int[] btnKeys = null;
    	int bw = ok.getWidth();
    	int bh = ok.getHeight();
    	g.setClip(0, viewHeight-24, viewWidth, viewHeight);
    	int y = viewHeight-bh/2;
    	int x = bw/2;
    	Image[] frameImage = null;
    	if (!isShortcutShown()) {//脚本和事件状态   
    		if((/*gameState.subState != GameState.STATE_SPLASH && gameState.type == GameState.STATE_GAMEMENU || */gameState.type == GameState.STATE_MAINMENU) || gameState.type == GameState.STATE_EDITATTR || gameState.type == GameState.STATE_EDITEQUIPS || gameState.type == GameState.STATE_EDITITEM || gameState.type == GameState.STATE_TASKUI || gameState.type == GameState.STATE_CHATLIST){
    	    	//选中，上，下，左，右,撤销
    			btnIcons = new int[] { 0, 1, 2, 3, 4, 5};
    			btnKeys = new int[] { SOFT_FIRST_PRESSED,UP_PRESSED,DOWN_PRESSED, LEFT_PRESSED,RIGHT_PRESSED,SOFT_LAST_PRESSED};
    			frameImage =new Image[]{ok, up, down,left,right,turn};
    			//g.drawImage(ok, x, y,Graphics.HCENTER|Graphics.VCENTER);//选中
    			//g.drawImage(ok, x += bw, y,Graphics.HCENTER|Graphics.VCENTER);//撤销
    			//g.drawImage(up, x += bw, y,Graphics.HCENTER|Graphics.VCENTER);//上
    			//g.drawImage(down, x += bw, y,Graphics.HCENTER|Graphics.VCENTER);//下
    			for (int i = 0; i < btnIcons.length; i++) {
		    		g.drawImage(frameImage[i],x,y,Graphics.HCENTER|Graphics.VCENTER);
		    		StaticUtils.addButton(btnKeys[i], x-12, y-12, bw, bh);
		    		x += bw;
		    	}
	
    		}else if((gameState.subState != GameState.STATE_SPLASH && gameState.subState != GameState.SS_REGISTER_MESSAGE) && gameState.type == GameState.STATE_GAMEMENU){//游戏进入菜单为防止遮住单画
    			btnIcons = new int[] { 0, 1, 2, 5};
    			btnKeys = new int[] { SOFT_FIRST_PRESSED,UP_PRESSED,DOWN_PRESSED,SOFT_LAST_PRESSED};
    			frameImage =new Image[]{ok, up, down,turn};
    			g.drawImage(frameImage[0], x, y,Graphics.HCENTER|Graphics.VCENTER);//选中
    			g.drawImage(frameImage[1], x + bw, y,Graphics.HCENTER|Graphics.VCENTER);//撤销
    			g.drawImage(frameImage[2], viewWidth -x-bw, y,Graphics.HCENTER|Graphics.VCENTER);//上
    			g.drawImage(frameImage[3], viewWidth -x, y,Graphics.HCENTER|Graphics.VCENTER);//下
    			StaticUtils.addButton(btnKeys[0], x-12, y-12, bw, bh);
    			StaticUtils.addButton(btnKeys[1], x + bw-12, y-12, bw, bh);
    			StaticUtils.addButton(btnKeys[2], viewWidth -bw*2, y-12, bw, bh);
    			StaticUtils.addButton(btnKeys[3], viewWidth -bw, y-12, bw, bh);
    		}else if(gameState.subState == GameState.SS_REGISTER_MESSAGE && gameState.type == GameState.STATE_GAMEMENU){//注册密码挡住单写
    			btnIcons = new int[] { 0, 1};
    			btnKeys = new int[] { SOFT_FIRST_PRESSED,SOFT_LAST_PRESSED};
    			frameImage =new Image[]{ok, turn};
    			g.drawImage(frameImage[0], x, y,Graphics.HCENTER|Graphics.VCENTER);//选中
    			g.drawImage(frameImage[1], viewWidth -x, y,Graphics.HCENTER|Graphics.VCENTER);//下
    			StaticUtils.addButton(btnKeys[0], x-12, y-12, bw, bh);
    			StaticUtils.addButton(btnKeys[1], viewWidth -bw, y-12, bw, bh);
    		}
    		return;
    	}
    	
    	
    	if(nowBattle<0){//非战斗状态
	    	if (shortcutsState == 1) {//展开模式
	    		if (!teamMode || teamLeader || teamStatus == World.TEAM_STATUS_NOTFOLLOW) {
		    		// 独立行动模式: 打开菜单、发起聊天、周围玩家、背包。地图开关、改变称号、回复密聊、聊天记录
	    			btnIcons = new int[] { 0, 1 ,2, 3, 4, 5, 6, 7 ,8 ,9};
		    		btnKeys = new int[] { SOFT_FIRST_PRESSED,KEY_NUM0_PRESSED, KEY_NUM1_PRESSED, KEY_NUM3_PRESSED,KEY_NUM7_PRESSED,KEY_NUM9_PRESSED, KEY_POUND_PRESSED,KEY_STAR_PRESSED,FIRE_PRESSED,4000};
		    		frameImage =new Image[]{menu, chat, players, bag, smap, name, recall, msg, sayhello ,leftSpread};
		    	}else{// 跟随模式：发起聊天、地图开关、改变称号、回复密聊
		    		btnIcons = new int[] { 0, 1, 2, 3, 4 };
		    		btnKeys = new int[] { KEY_NUM0_PRESSED ,KEY_NUM7_PRESSED,KEY_NUM9_PRESSED,KEY_POUND_PRESSED,4000};
		    		frameImage =new Image[]{chat, smap, name, msg, leftSpread};
		    	}

	    	} else {//非展开模式
	    		if (!teamMode || teamLeader || teamStatus == World.TEAM_STATUS_NOTFOLLOW) {//非组队模式
	    			btnIcons = new int[] { 0, 1, 2, 3};
	    			btnKeys = new int[] { SOFT_FIRST_PRESSED, KEY_NUM0_PRESSED, KEY_NUM5_PRESSED, 4000 };//菜单，聊天，伸缩
	    			frameImage =new Image[]{menu, chat, imoney, rightSpread};//伸缩箭头
	    			
	    		} else {
	    			btnIcons = new int[] { 0,1 };
	    			btnKeys = new int[] { KEY_NUM0_PRESSED ,4000};
	    			frameImage =new Image[]{chat,rightSpread};
	    		}
	    	} 
	    	
		    	for (int i = 0; i < btnIcons.length && (i+1)*World.ok.getWidth() <= viewWidth ; i++) {
		    		g.drawImage(frameImage[i],x,y,Graphics.HCENTER|Graphics.VCENTER);
		    		StaticUtils.addButton(btnKeys[i], x-bw/2, y-bh/2, bw, bh);
		    		x += bw;
		    	}
		    	
	    	}else{//战斗状态
	    		g.setClip(viewWidth-40, 0, 40, 70);
	    		if(!autoDrawSkillLeveLImage){
            		g.drawImage(chat,viewWidth-20,20,Graphics.HCENTER|Graphics.VCENTER);
            		
            	}else{
            		g.drawImage(skilllevel,viewWidth-20,20,Graphics.HCENTER|Graphics.VCENTER);
            	}
	    		btnKeys = new int[] {KEY_NUM0_PRESSED,SOFT_LAST_PRESSED};
        		StaticUtils.addButton(btnKeys[0], viewWidth-bw*2, bh/2, bw*3/2,bh);
        		StaticUtils.addButton(btnKeys[1], viewWidth-bw, viewHeight-bh, bw,bh);
            	if(autoDrawBattleImage){
            		g.drawImage(autobattle,viewWidth-20,50,Graphics.HCENTER|Graphics.VCENTER);
            		btnKeys = new int[] {KEY_STAR_PRESSED};
            		StaticUtils.addButton(btnKeys[0], viewWidth-bw*2, bh*2, bw*3/2, bh);
            	}
            	g.setClip(viewWidth-bw, viewHeight-bh, bw,bh);//战斗的返回键
	    		g.drawImage(turn,viewWidth-bw/2,viewHeight-bh/2,Graphics.HCENTER|Graphics.VCENTER);
	    	}
    }
    
    /**
     * 判断当前是否应该显示游戏中的热键菜单。
     */
    public boolean isShortcutShown() {
    	// 如果当前打开了界面或状态，不画
    	if (gameState != null) {
    		return false;
    	}
    	return true;
    }
    public void pointerPressed(int x, int y){
    	if (gameState == null && nowBattle < 0 && StaticUtils.getButtonAt(x, y) == -1) {
    		// 如果在游戏中，没有打开脚本界面，没有阻塞事件，并且没有点中按钮，那么，这个触笔事件可以用来移动人物
    		controlPlayerWalk(x, y);
    	}else{
    		StaticUtils.pointerPressed(x, y);
    	}
    }
    public void pointerReleased(int x, int y) {
    	if (gameState == null && nowBattle < 0 && StaticUtils.getButtonAt(x, y) == -1) {
    		clearKeyStates();
    	}
    	//player.state = player.STATE_IDLE;
    	StaticUtils.pointerReleased(x, y);
    }
    public void pointerDragged(int x, int y) {
    	if (gameState == null && nowBattle < 0 && StaticUtils.getButtonAt(x, y) == -1) {
    		// 如果在游戏中，没有打开脚本界面，没有阻塞事件，并且没有点中按钮，那么，这个触笔事件可以用来移动人物
    		controlPlayerWalkDragg(x, y);
    	}else{
    		StaticUtils.pointerDragged(x, y);
    	}
    }
    private static byte dic;//触摸屏,行走时记录方向
    //通过点击坐标的位置进行人物的走动控制具体通过按键方式进行。（哈哈这方面不熟悉，改天慢慢研究）
    public void controlPlayerWalk(int x,int y){
    	int playerx = player.x - viewX;
    	int playery = player.y - viewY;
    	
    	if(playerx < x){
    		if(y < (playery + x - playerx) && y > (playery - x + playerx)){
    			dic = 4;
    			keyPressed(-4);
    		} else if(y > (playery + x - playerx)){
    			keyPressed(-2);
    			dic = 2;
    		} else if(y < (playery - x + playerx)){
    			keyPressed(-1);
    			dic = 1;
    		}
    	} else if (playerx > x){
    		if(y < (playery + playerx - x) && y > (playery - playerx +x)){
    			keyPressed(-3);
    			dic = 3;
    		} else if(y > (playery + playerx - x)){
    			keyPressed(-2);
    			dic = 2;
    		} else if (y < (playery - playerx + x)){
    			keyPressed(-1);
    			dic = 1;
    		}
    	}
    }
    public void controlPlayerWalkDragg(int x,int y){
    	int playerx = player.x - viewX;
    	int playery = player.y - viewY;
    	if(playerx < x){
    		if(y < (playery + x - playerx) && y > (playery - x + playerx)){
    			if (dic != 4){
    				keyReleased(0-dic);
    				dic = 4;
        			keyPressed(-4);
    			}
    		} else if(y > (playery + x - playerx)){
    			if (dic != 2){
					keyReleased(0-dic);
    				keyPressed(-2);
    				dic = 2;
    			}
    		} else if(y < (playery - x + playerx)){
    			if (dic != 1){
					keyReleased(0-dic);
        			keyPressed(-1);
        			dic = 1;	
    			}
    		}
    	} else if (playerx > x){
    		if(y < (playery + playerx - x) && y > (playery - playerx +x)){
    			if (dic != 3){
					keyReleased(0-dic);
    				keyPressed(-3);
    				dic = 3;
    			}
    		} else if(y > (playery + playerx - x)){
    			if (dic != 2){
					keyReleased(0-dic);
    				keyPressed(-2);
    				dic = 2;
    			}
    		} else if (y < (playery - playerx + x)){
    			if (dic != 1){
					keyReleased(0-dic);
    				keyPressed(-1);
    				dic = 1;
    			}
    		}
    	}
    }
    private int getTouchMenuIconIndex(int i){


//        menuCmd = battleMenuText[getMenuIconIndex()];
		if (currentActionType==0){
			switch(i){
			case 0:
				battleMenuSelectIndex = 1;
				break;
			case 1:
				battleMenuSelectIndex = 3;
				break;
			case 2:
				battleMenuSelectIndex = 2;
				break;
			case 3:
				battleMenuSelectIndex = 4;
				break;
			case 4:
				battleMenuSelectIndex = 0;
				break;
			}
		} else {
			switch(i){
			case 0:
				battleMenuSelectIndex = 1;
				break;
			case 1:
				battleMenuSelectIndex = 10;
				break;
			case 2:
				battleMenuSelectIndex = 0;
				break;
			}
		}
        return -1;
        
    }
  //#endif
    public static void RecordPreousDisplay(Displayable displayPreous){
    	display.setCurrent(displayPreous);
    	displayFirst = displayPreous;
    }
}
