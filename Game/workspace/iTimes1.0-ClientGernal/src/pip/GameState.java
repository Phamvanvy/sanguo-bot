package pip;


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.TextField;

import pip.io.UWAPConnection;
import pip.io.UWAPHttpConnection;
import pip.io.UWAPSegment;
import pip.io.UWAPSocketConnection;


//#ifdef polish.api.nokia-ui
//# import com.nokia.mid.ui.*;
//#endif

public class GameState implements Runnable, CommandListener{
    //#if (Directory == DOPOD-585)
    //# public static final Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    //#else
    public static final Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    //#endif
    //    public static final Font largeFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);

    //#if polish.identifier == Nokia/Series40Midp2
    //# public static final int CHAR_HEIGHT = font.getHeight()+2;
    //#else
    public static final int CHAR_HEIGHT = font.getHeight();
    //#endif

    public static final byte STATE_SPLASH = 0;
    public static final byte STATE_GAMEMENU = 1;
    public static final byte STATE_LOADING = 2;
    public static final byte STATE_MAINMENU = 3;
    public static final byte STATE_TASKUI = 4;
    public static final byte STATE_RELOGIN = 5;

    public static final byte STATE_EDITITEM = 6;
    public static final byte STATE_EDITEQUIPS = 7;
    public static final byte STATE_EDITATTR = 8;

    public static final byte STATE_CHATLIST = 11;

    public static final byte STATE_INPUT_FORM = 12;

    public static final byte STATE_OPENURL = 20;
    //#if Revision == QQ
    public static final byte STATE_QQBILL = 21;
    //#endif
    public static final byte STATE_TESTNET = 22;        // 测试连接线程
    public static final byte STATE_TESTSERVERLIST = 23; // 测试服务器列表线程
    public static final byte STATE_GETLISTFAIL = 24;    // 获取服务器列表失败

    public static final byte SS_CHOICE = 0;
    public static final byte SS_LOGIN = 1;
    public static final byte SS_LOGINING = 2;
    public static final byte SS_REGISTER = 3;
    public static final byte SS_REGISTERING = 4;
    public static final byte SS_HELP = 7;
    public static final byte SS_ABOUT = 8;
    public static final byte SS_MESSAGE = 9;
    public static final byte SS_LOADING_ERROR = 10;
    public static final byte SS_ACTOR_SELECT = 11;
    public static final byte SS_ACTOR_SELECT_SHOWMENU = 110;
    public static final byte SS_ACTOR_SELECT_DELETE = 111;
    public static final byte SS_ACTOR_SELECT_DELETE_CONFIRM = 112;
    public static final byte SS_ACTOR_SELECT_DELETE_CONFIRM2 = 113;
    public static final byte SS_ACTOR_SELECT_DELETE_DOING = 114;
    public static final byte SS_ACTOR_SELECT_DELETE_SUCCESS = 115;
    public static final byte SS_ACTOR_SELECT_DELETE_FAILED = 116;
    public static final byte SS_ACTOR_CREATING = 12;
    public static final byte SS_ACTOR_DELETE = 13;
    public static final byte SS_ACTOR_LOGINING = 15;
    public static final byte SS_ACTOR_GET_LIST = 16;
    public static final byte SS_GETSERVERLIST = 17;
    public static final byte SS_SERVERLIST = 18;
    public static final byte SS_FAST_REG = 19;
    public static final byte SS_FAST_LOGIN = 20;
    public static final byte SS_FAST_GET_ACTORLIST = 21;
    public static final byte SS_FAST_PLAYER_LOGIN = 22;
    public static final byte SS_REGISTER_MESSAGE = 23;

    public final static byte SS_LOADING_BATTLE = 50;
    public final static byte SS_LOADING_LEAVEBATTLE = 51;
    public static final byte SS_LOADING_SERVERBATTLE = 52;

    //#debug
    public static final int LINE_HEIGHT = CHAR_HEIGHT;
    //#= public static final int LINE_HEIGHT = CHAR_HEIGHT + 4;
    public static final int CHAR_WIDTH = font.charWidth('白');
    public static final int MENU_CHAR_HEIGHT = font.getHeight();
    public static final int MENU_LINE_HEIGHT = MENU_CHAR_HEIGHT + 2;
    public static final int MENU_CHAR_WIDTH = font.stringWidth("属");
    public static final int BOX_MARGIN = 1;
    public static final int SCREEN_MARGIN = 5;
    public static final int EDGE_WIDTH = 3;
    public static final int TAB_HEIGHT = CHAR_HEIGHT + BOX_MARGIN * 2 + EDGE_WIDTH * 2;
    public static final int TITLE_HEIGHT = CHAR_HEIGHT + BOX_MARGIN * 2 + EDGE_WIDTH * 2;

    //#if (polish.identifier == Nokia/Series40Midp2)
    //# private static final int GAMEMENU_OFFSET = 96;
    //#elif (Directory == NK-BigScreen) || (Directory == NK-Nokia403Big) || (Directory == SE-S700)
    //# private static final int GAMEMENU_OFFSET = 320 - CHAR_HEIGHT * 3 - 5;
    //#elif (Directory == MT-General) || (Directory == Midp2-General) || (Directory == SE-K500) || (Directory == SE-K300) || (Directory == Nokia403) || (MIDP2Common == true) || (Directory == ClientTouch-E680) || (Directory == ClientTouch--Midp2-General) || (Directory == ClientTouch-SE-General) || (Directory == ClientTouch-Nokia5800)
    private static int GAMEMENU_OFFSET;
    //#else
    //# private static final int GAMEMENU_OFFSET = 208 - CHAR_HEIGHT * 3 - 5;
    //#endif

    public static Vector segments = new Vector();

    public static final byte CONN_ERROR = -1;
    public static final byte CONN_ACCOUNTREG = 1;
    public static final byte CONN_ACCOUNTREG_OK = 2;
    public static final byte CONN_ACTORCREATE = 3;
    public static final byte CONN_ACTORCREATE_OK = 4;
    public static final byte CONN_GET_ACTORLIST = 5;
    public static final byte CONN_GET_ACTORLIST_OK = 6;
    public static final byte CONN_PALYER_LOGIN = 7;
    public static final byte CONN_PALYER_LOGIN_OK = 8;
    public static final byte CONN_RELOGIN = 9;
    public static final byte CONN_RELOGIN_RESULT = 10;
    public static final byte CONN_GET_FILE = 11;
    public static final byte CONN_GOTO = 11;
    public static final byte CONN_SEND_POSITION = 12;
    public static final byte CMCC_CHARGE = 13;
    public static final byte CMCC_CHARGE_OK = 13;
    public static final byte CONN_BBS_POST = 14;
    public static final byte CONN_BBS_POST_OK = 15;
    public static final byte CONN_BBS_GET_LIST = 16;
    public static final byte CONN_GET_BBS_LIST = 16;
    public static final byte CONN_GET_BBS_CONTENT = 17;
    public static final byte CONN_BBS_CONTENT = 18;
    public static final byte CONN_FACE_LIST = 19;
    public static final byte CONN_TOUCH_NPC = 23;

    public static final byte CONN_MAIL_POST = 24;
    public static final byte CONN_MAIL_POST_OK = 24;
    public static final byte CONN_MAIL_GET_LIST = 25;
    public static final byte CONN_MAIL_LIST = 25;
    public static final byte CONN_GET_MAIL_CONTENT = 26;
    public static final byte CONN_MAIL_CONTENT = 26;
    public static final byte CONN_GET_ATTACHMENT = 27;
    public static final byte CONN_GET_ATTACHMENT_OK = 27;
    public static final byte CONN_DELETE_MAIL = 28;
    public static final byte CONN_NEWMAIL = 29;
    public static final byte CONN_QUICK_REG = 30;
    public static final byte CONN_REQUEST_FRIEND_LIST = 31;

    public static final byte CONN_USE_ITEM = 33;

    public static final byte CONN_BATTLE_RESULT = 34;
    public static final byte CONN_ADD_PROPERTY_POINT = 36;

    public static final byte CONN_TEAM_CREATE = 38;
    public static final byte CONN_TEAM_CREATE_OK = 39;
    public static final byte CONN_TEAM_INVITE = 40;
    public static final byte CONN_TEAM_INVIT_RESULT = 41;
    public static final byte CONN_TEAM_JOIN_OK = 42;
    public static final byte CONN_TEAM_JOIN_FAIL = 43;
    public static final byte CONN_TEAM_LEAVE = 44;

    public static final byte CONN_BATTLE_REQUEST = 45;
    public static final byte CONN_BATTLE_INIT = 46;
    public static final byte CONN_BATTLE_JOIN = 47;
    public static final byte CONN_BATTLE_JOIN_RESULT = 48;
    public static final byte CONN_BATTLE_START = 49;
    public static final byte CONN_BATTLE_ABORT = 50;
    public static final byte CONN_BATTLE_FIGHT = 51;
    public static final byte CONN_BATTLE_ROUND_END = 52;
    public static final byte CONN_UPLOAD = 55;
    public static final byte CONN_UPLOAD_OK = 56;

    public static final byte CONN_ISHOP_LIST = 57;
    public static final byte CONN_ISHOP_TRADE = 58;

    public static final byte CONN_SNEAK_ATTACK = 59;

    public static final byte CONN_PK_REQUEST = 60;
    public static final byte CONN_PK_CREATED = 61;
    public static final byte CONN_PK_CANCEL = 62;
    public static final byte CONN_PK_REFUSE = 63;
    public static final byte CONN_PK_OK = 64;
    public static final byte CONN_PK_START = 65;
    public static final byte CONN_PK_FIGHT = 66;
    public static final byte CONN_PK_ROUND_END = 67;

    public static final byte CONN_GENERIC_LIST = 68;
    public static final byte CONN_GENERIC_CONTENT = 69;

    public static final byte CONN_EQU_CHANGED = 70;
    public static final byte CONN_EQU_CHANGED_OK = 71;

    public static final byte CONN_CHAT_MESSAGE = 72;
    public static final byte CONN_CHAT_OPTION = 73;
    public static final byte CONN_CHATFAVORITE_LIST = 74;
    public static final byte CONN_CHATFAVORITE_DESC = 75;
    public static final byte CONN_CHANGE_CHATFAVORITE = 76;

    public static final byte CONN_LOGIN = 77;
    public static final byte CONN_LOGINOK = 78;
    public static final byte CONN_REFRESH = 80;
    public static final byte CONN_COMMAND = 81;
    public static final byte CONN_MESSAGE = 82;

    public static final byte CONN_LOOK_EQU = 83;
    public static final byte CONN_LOOK_EQU_OK = 83;

    public static final byte CONN_REQUEST_ITEM_LINK = 84;

    public static final byte CONN_STORE_ITEM_LIST = 85;
    public static final byte CONN_STORE_TRADE = 86;
    public static final byte CONN_STORE_TRADE_OK = 86;

    public static final byte CONN_ADD_FRIEND = 87;
    public static final byte CONN_ADD_FRIEND_OK = 87;

    public static final byte CONN_REQUEST_TASK_DESC = 88;
    public static final byte CONN_REQUEST_TASK_DESC_OK = 88;

    public static final byte CONN_ADD_POINT = 89;
    public static final byte CONN_ADD_POINT_OK = 89;

    public static final byte CONN_SYNCTIME = 90;

    public static final byte CONN_ADD_BLACK = 91;

    public static final byte CONN_GET_ITEM = 94;
    public static final byte CONN_GATHER = 95;
    public static final byte CONN_GATHER_OK = 96;
    public static final byte CONN_GATHER_RESULT = 97;
    public static final byte CONN_LEARN_SKILL = 98;
    public static final byte CONN_LEARN_SKILL_OK = 99;
    public static final byte CONN_SKILL_LIST = 100;
    public static final byte CONN_GET_DESC = 101;
    public static final byte CONN_DESC = 102;
    public static final byte CONN_PRODUCT = 103;
    public static final byte CONN_ABILITY_LIST = 105;
    public static final byte CONN_LEARN_ABILITY = 106;
    public static final byte CONN_LEARN_ABILITY_OK = 107;
    public static final byte CONN_TASK_COMPLETED = 108;
    public static final byte CONN_TASK_COMPLETED_OK = 108;

    public static final byte CONN_SHOP_CREATE_OK = 110;
    public static final byte CONN_SHOP_LIST = 111;
    public static final byte CONN_REQUEST_SHOP_ITEM_LIST = 112;
    public static final byte CONN_SHOP_ITEM_LIST = 112;
    public static final byte CONN_SHOP_ADD_ITEM = 113;
    public static final byte CONN_SHOP_ADD_ITEM_OK = 113;
    public static final byte CONN_SHOP_REMOVE_ITEM = 114;
    public static final byte CONN_SHOP_REMOVE_ITEM_OK = 114;
    public static final byte CONN_SHOP_MONEY_CHANGE = 115;
    public static final byte CONN_SHOP_MONEY_CHANGE_OK = 115;
    public static final byte CONN_SHOP_CHANGE = 116;
    public static final byte CONN_SHOP_CHANGE_OK = 116;

    public static final byte CONN_AUCTION_TYPE_LIST = 117;
    public static final byte CONN_REQUEST_AUCTION_LIST = (byte)118;
    public static final byte CONN_AUCTION_LIST = (byte)118;
    public static final byte CONN_AUCTION_REQUEST_ITEM_DESC = (byte)119;
    public static final byte CONN_AUCTION_ITEM_DESC = (byte)119;
    public static final byte CONN_AUCTION_PRICE = (byte)120;
    public static final byte CONN_AUCTION_PRICE_OK = (byte)120;
    public static final byte CONN_AUCTION_ITEM = (byte)121;
    public static final byte CONN_AUCTION_ITEM_OK = (byte)121;

    public static final byte CONN_BUY_MATERIAL_TYPE_LIST = (byte)122;
    public static final byte CONN_REQUEST_BUY_MATERIAL_LIST = (byte)123;
    public static final byte CONN_BUY_MATERIAL_LIST = (byte)123;
    public static final byte CONN_SELL_MATERIAL = (byte)124;
    public static final byte CONN_SELL_MATERIAL_OK = (byte)124;
    public static final byte CONN_OEM_TYPE_LIST = (byte)125;
    public static final byte CONN_REQUEST_OEM_LIST = (byte)126;
    public static final byte CONN_OEM_LIST = (byte)126;
    public static final byte CONN_OEM = (byte)127;
    public static final byte CONN_OEM_OK = (byte)127;

    public static final byte CONN_TONG_CREATE_OK = (byte)130;
    public static final byte CONN_REQUEST_TONG_MEMBERS = (byte)131;
    public static final byte CONN_TONG_MEMBERS_LIST = (byte)131;
    public static final byte CONN_TONG_GRANT = (byte)132;
    public static final byte CONN_TONG_GRANT_OK = (byte)132;
    public static final byte CONN_TONG_MODIFY_TITLE = (byte)133;

    public static final byte CONN_FRIEND_STATUS = (byte)134;

    public static final byte CONN_TASK_ABANDON = (byte)135;
    public static final byte CONN_TASK_ABANDON_RESULT = (byte)135;

    public static final byte CONN_ADD_PET_POINT = (byte)136;
    public static final byte CONN_ADD_PET_POINT_OK = (byte)136;
    public static final byte CONN_BUY_PET_POINT = (byte)137;
    public static final byte CONN_USE_PET = (byte)138;
    public static final byte CONN_USE_PET_OK = (byte)138;
    public static final byte CONN_PET_FEED = (byte)139;
    public static final byte CONN_DELETE_USER = (byte)140;
    public static final byte CONN_DELETE_USER_OK = (byte)140;
    public static final byte CONN_CHANGE_OPTION = (byte)141;
    public static final byte CONN_REPAIRE_LIST = (byte)142;
    public static final byte CONN_REPAIRE = (byte)143;
    public static final byte CONN_REPAIRE_OK = (byte)143;

    public static final byte CONN_NETSPEED_TEST = (byte)177; //测试网速

    public static final byte CONN_KILL_SERVER = (byte)190;
    public static final byte CONN_BILLING_OK = (byte)205;

    public static final short GET_FILE_STAGE = 1;
    public static final short GET_FILE_TASK = 2;
    public static final short GET_FILE_MGROUP = 3;
    public static final short GET_FILE_NPC = 4;
    public static final short GET_FILE_MONSTER = 5;

    public static final short NPC_SHOWNAME_SPACE = 50; //主角和NPC之间间隔距离为NPC_SHOWNAME_SPACE时显示NPC名字

    public static final byte TYPE_NPC = 0;
    public static final byte TYPE_MONSTER = 1;
    public static final byte TYPE_RESOURCE = 2;
    public static final byte TYPE_MONSTERICON = 3;
    public static final byte TYPE_ITEMNPC = 4;

    private long _time;
    public static ImageSet edgeImageSet = null;

    private Image _image = null;
  //#if ((Directory == Nokia403) || (Directory == SE-K300) || (Directory == SE-K500) || (Directory == DOPOD-585))
    private ImageSet _movingArrow = null;
    //#else
    private ImageSet _movingMenu = null;
    //#endif
    public static int version = 0;
    //#if !((Directory == Nokia403) || (Directory == SE-K300) || (Directory == SE-K500) || (Directory == DOPOD-585))
    private short cycleCount =0;   //确定要转的5次；
    private int menuCycleInstance;//单个菜单之间的距离
    private boolean upCycle;//控制上还是下循环
    private int[] menuCycleStart;//控制7个菜单的起始距离
    private boolean insteadOldMenu;//替换老菜单
    private int leftIsntance;//用于208遮挡
    //#endif
    private boolean gameMenuCycle =false;//是否循环
    public static String url = null;
    public static int reloginTimes = 0;
    public static boolean gameIsOk = false;
    public boolean reloginStarted = false;

    private String[] _stringItems = null;
    private int blankHeight;
    private int blankStartX;
    private int blankStartY;
    private int startIndex;
    private int gameMenuRowNum;

    public static final String GAMEMENU_FASTREG = "快速注册";
    public static final String GAMEMENU_FASTLOGIN = "快速进入";
    public static final String GAMEMENU_LOGIN = "进入游戏";

    //#if EnableReg == TRUE
    public static final String GAMEMENU_REG = "注册用户";
    //#endif

    //#if Directory != NK-NGage
    public static final String GAMEMENU_UPDATE = "更新版本";
    public static final String GAMEMENU_PORTAL = "登录官网";
    //#if Revision == SOHU
    public static final String GAMEMENU_SOHU = "搜狐游戏中心";
    //#endif
    //#endif

    public static final String GAMEMENU_HELP = "游戏帮助";
    public static final String GAMEMENU_ABOUT = "关于游戏";
    public static final String GAMEMENU_EXIT = "退出游戏";
    public static final String GAMEMENU_MOREGAME = "更多游戏";
    public static final String GAMEMENU_POINT = "点数专区";
    
    public static final String GAMEMENU_QQGAME = "QQ游戏中心";

    public static final String ACTORMENU_LOGIN = "进入游戏";
    public static final String ACTORMENU_DELETE = "删除人物";

    public static final String[] ACTOR_MENU = {
                    ACTORMENU_LOGIN, ACTORMENU_DELETE
    };
    //#if !((Directory == Nokia403) || (Directory == SE-K300) || (Directory == SE-K500) || (Directory == DOPOD-585))
    public static final String[] GAME_MENU = {
                      
                      //#if  Revision == SOHU || (Revision == DOWNJOY) || (Revision == PIP) || (Revision == JIANGSUN)
          			//ngage专门加3个菜单用来控制菜单默认选项为第四个
          	 		//#if (Directory == NK-NGage)
          			//# GAMEMENU_LOGIN,
              		//#endif
    	//#  GAMEMENU_EXIT,
          	  			
                          
                          
                          
    	//# GAMEMENU_ABOUT,
                       //#if (Directory != NK-NGage) && (Revision != CMCC)
    	//# GAMEMENU_UPDATE,
                     //#endif
                       
    	//# GAMEMENU_LOGIN,
                          //#if Revision == QQ
                          
                          //#else
    	//# GAMEMENU_FASTREG,
                          //#if EnableReg == TRUE
    	//# GAMEMENU_REG,
                          //#endif
                          //#endif

                          //#if (Directory != NK-NGage) && (Revision != CMCC)
                          //#if Revision != DOWNJOY
    	//#  GAMEMENU_PORTAL,
                          //#endif 
                        //#if Revision == SOHU
    	//#  GAMEMENU_SOHU,

                          //#endif
                        //#endif 

                        //ngage专门加3个菜单用来控制菜单默认选项为第四个
                	 		//#if (Directory == NK-NGage)
                			//# GAMEMENU_LOGIN,
                    		//#endif
    	//# GAMEMENU_EXIT,

    	//#  GAMEMENU_ABOUT,
                             //#if (Directory != NK-NGage) && (Revision != CMCC)
    	//# GAMEMENU_UPDATE,
                           //#endif
                             
    	//#  GAMEMENU_LOGIN,
                                //#if Revision == QQ
                                
                                //#else
    	//# GAMEMENU_FASTREG,
                                //#if EnableReg == TRUE
    	//# GAMEMENU_REG,
                                //#endif
                                //#endif

                              //#if (Directory != NK-NGage) && (Revision != CMCC)
                                //#if Revision != DOWNJOY
    	//#  GAMEMENU_PORTAL,
                                //#endif 
                              //#if Revision == SOHU
    	//# GAMEMENU_SOHU,

                                //#endif
                              //#endif 

                      
                      //#endif
                                
                      //#if Revision == QQ
                                //#  GAMEMENU_ABOUT,GAMEMENU_EXIT,
                                  
                                //#if (Directory != NK-NGage) && (Revision != CMCC)
                                //# GAMEMENU_UPDATE, 
                                //#if Revision != DOWNJOY
                                //# GAMEMENU_PORTAL,
                                //#endif
                                //#endif
                               

                                //# GAMEMENU_LOGIN,
                               
                                
                                //#if Revision == QQ && (Directory != NK-NGage)
                                //# GAMEMENU_QQGAME,
                                //#endif   
                                
                              //# GAMEMENU_ABOUT,GAMEMENU_EXIT,
                                
                                //#if (Directory != NK-NGage) && (Revision != CMCC)
                                //# GAMEMENU_UPDATE, 
                                //#if Revision != DOWNJOY
                                //# GAMEMENU_PORTAL,
                                //#endif
                                //#endif
                               

                                //# GAMEMENU_LOGIN,
                               
                                
                                //#if Revision == QQ && (Directory != NK-NGage)
                                //# GAMEMENU_QQGAME
                                //#endif   
                                      
                                
                      //#endif
                                
                                
                                
                                //#if Revision == CMCC || (Revision == JIANGSUNCMCC) 
                                  GAMEMENU_ABOUT, GAMEMENU_EXIT,
                                //#if EnableReg == TRUE
                                 GAMEMENU_REG,
                                //#endif
                                 GAMEMENU_LOGIN,
                                  GAMEMENU_FASTREG,
                                
                                //#if (Directory != NK-NGage)
                                 GAMEMENU_MOREGAME,
                                //#endif
                                 GAMEMENU_POINT,   
                                GAMEMENU_ABOUT, GAMEMENU_EXIT,
                                //#if EnableReg == TRUE
                                GAMEMENU_REG,
                                //#endif
                                GAMEMENU_LOGIN,
                                  GAMEMENU_FASTREG,
                                
                                //#if (Directory != NK-NGage)
                                 GAMEMENU_MOREGAME,
                                //#endif
                                 GAMEMENU_POINT
                               

                      //#endif
                                
    };
    //#else
  //# public static final String[] GAME_MENU = {
    
  //#if Revision == QQ
  //# GAMEMENU_LOGIN,
  //#else
  //#  GAMEMENU_FASTREG, GAMEMENU_LOGIN,
  //#if EnableReg == TRUE
  //# GAMEMENU_REG,
  //#endif
  //#endif

  //#if Revision == CMCC || (Revision == JIANGSUNCMCC) 
  //#if (Directory != NK-NGage)
  //# GAMEMENU_MOREGAME,
  //#endif
  //# GAMEMENU_POINT,
  //#endif

  //#if Revision == QQ && (Directory != NK-NGage)
  //# GAMEMENU_QQGAME,
  //#endif
        
  //#if (Directory != NK-NGage) && (Revision != CMCC)
  //# GAMEMENU_UPDATE, 
  //#if Revision != DOWNJOY
  //# GAMEMENU_PORTAL,
  //#endif
  //#if Revision == SOHU
  //# GAMEMENU_SOHU,
  //#endif
  //#endif

  //#  //GAMEMENU_HELP,
  //# GAMEMENU_ABOUT, GAMEMENU_EXIT
  //# };
  //#endif
    private final static String[] MAIN_MENU = {
                    "系统功能", "辅助功能", "聊天功能"
    };

    //#if Revision == CMCC || (Revision == JIANGSUNCMCC) 
    //# public static final String IMONEY_ADD = "点数充值";
    //# public static final String IMONEY_SALE = "道具卖场";
    //#elif Revision == QQ
    //# public static final String IMONEY_ADD = "购买元宝";
    //# public static final String IMONEY_SALE = "道具卖场";
    //#else
    public static final String IMONEY_ADD = "i币充值";
    public static final String IMONEY_SALE = "i币卖场";
    //#endif

    private final static String[][] SUB_MENU = {
                    {
                                    IMONEY_ADD, IMONEY_SALE, "人物属性", "物品背包", "武器装备", "任务查看", "聊天记录"
                    },
                    {
                                    "战斗技能", "生活技能", "宠物驯养", "精灵速递",
                                    //#if Revision == CMCC || (Revision == JIANGSUNCMCC) 
                                    "好友推荐", 
                                    //#endif
                    }, {
                                    "聊天设置", "圈设置", "系统设置", "脱离卡死", /*"常见问题",*/"联机帮助", "呼叫GM", "更换角色", "退出游戏"
                    }, {
                                    "周围玩家", "好友列表", "公会查看", "创建队伍", /*"创建团队",*/"发起聊天", "回复密聊", "黑名单"
                    }
    };

    public byte type;
    public String _message = "";
    public int _index;
    public int subState;
    private int _subIndex;
    private int _subMenuIndex;
    private int _delState;
    private int deleteTimer;
    public Thread thread;
    public Form _form;
    public static String _formTitle;
    private String[] infoStrings = null;
    private int _scroll;
    public boolean paintBackGroud = false;
    public boolean paintMiniMap = false;

    public static final String maintenanceCode = "none=";
    public static String[] serverGroups;
    public static String[][] serverURLs;
    public static String[] URLs;
    public static String serverName;
    public static String tempServerName;
    public static boolean serverPosSeted;
    public static byte[][] serverLoads;
    public static String serverTip = "";
    
    public int testNetResult = 0;    // 0 - 未完成，1 - 成功， 2 - 失败 
    public UWAPConnection testNetConnection;    // 建立成功的连接

    public int nextState;

    public static UWAPConnection connection = null;

    public static int connectionId = -1;
    public static long serverTime = System.currentTimeMillis();
    public static long lastSyncTime = System.currentTimeMillis();
    public final  String phone = "15902943606";
    public static String recommend;
    public int serial;

    public static String name;
    public static String password;
    public static String actorName;
    public static int actorSex;
    public static int actorMaxLevel;
    public Vector actorList = null;
    public Vector actorNameList = null;
    public static boolean logouting = false;
    public static boolean fastWay = false;
    public static boolean canFastChecked = false;

    public static boolean isMapLoadOk = false;

    public short oldMapId = -1;
    public short loadMapId = -1;
    public short startX = -1;
    public short startY = -1;
    public boolean isMapXY = true;

    public static boolean repaintNextTime = true;

    //#if Revision == CMCC || (Revision == JIANGSUNCMCC) 
    //# private static final byte[] splash_time = {
    //#     3
    //# };
    //# private static final String[] splash_name = {
    //#     "/logoCo.png"
    //# };
    //# private static final int[] splash_bgcolor = {
    //#     0
    //# };
    //#elif Revision == QQ
    private static final byte[] splash_time = {
         3
     };
    private static final String[] splash_name = {
         "/logoQQ.png"
     };
     private static final int[] splash_bgcolor = {
         0xFF9000
     };
    //#elif Revision == SOHU
  //# private static final byte[] splash_time = {
  //# 3, 3
  //# };
  //# private static final String[] splash_name = {
  //# "/logoSohu.png", "/logoCo.png"
  //# };
  //# private static final int[] splash_bgcolor = {
  //#    0xFFFFFF, 0
  //# };
     //#elif Revision == JIANGSUN
     //# private static final byte[] splash_time = {
     //# 3, 3
     //# };
     //# private static final String[] splash_name = {
     //# "/logoJiangsun.png", "logoCo.png"
     //# };
     //# private static final int[] splash_bgcolor = {
     //#    0xFFFFFF, 0
     //# };
    //#else
//    private static final byte[] splash_time = {
//        4
//    };
//    private static final String[] splash_name = {
//        "/logoCo.png"
//    };
//    private static final int[] splash_bgcolor = {
//        0
//    };
    //#endif

    //#if Revision == CMCC || (Revision == JIANGSUNCMCC) 
    //# public static String wapUrl = "http://go.i139.cn/gcomm1/portal/spchannel.do?url=http://gamepie.i139.cn/wap/s.do?j=3channel";
    //#else
    public static String wapUrl = "http://wap.pipgame.cn/quickdownload?";
    //#endif
    
    //#if (Revision == PIP) || (Revision == SOHU) || (Revision == DOWNJOY) || (Revision == JIANGSUN)
    public static String pushUrl = "http://wap.pipgame.com:7070/pipgamewap/reference?from=itimes";
    public static String pushString = null;
    public static final String PUSH_DEFAULT_STRING = "http://wap.pipgame.cn/\n您身上如果带着浴场券，\n可以到浴场去离线升级\n更多精彩游戏,请进入\npipgame.cn";
    //#endif

    public static final String versionString = iTimesMIDlet.instance.getAppProperty("MIDlet-Version") == null? "3.1": iTimesMIDlet.instance.getAppProperty("MIDlet-Version");

    //#if Revision == CMCC || (Revision == JIANGSUNCMCC) 
    //# public static String entryURL = "http://221.179.216.49:8872/itimesipd/serverlist?type=all&format=groupnewcmcc&version=" + versionString;
    //测试服务器地址
    //public static String entryURL = "http://218.206.80.185:7070/testipd/serverlist?type=all&format=groupnewcmcc&version=" + versionString;
    //#elif Revision == QQ
    //# public static String entryURL = "http://119.147.16.18:8080/qqitimesipd/serverlist?type=all&format=groupnewqq&version=" + versionString;
    //# public static String qqOfficalWeb = "http://csg.3g.qq.com/g/s?aid=g_netSubject&cpId=903&gameId=001&cid=wy_isd_client";
    //# public static String qqUpdateWeb = "http://119.147.16.18:8080/qqitimesipd/itimes_update.jsp?";
    //#else
    public static String entryURL = "http://218.206.80.185:7070/itimesipd/serverlist?type=all&format=groupnew&version=" + versionString;
    //#endif

    //#debug
    public static final String channelCode = "CCCCYYXW";

    //#= public static final String channelCode = "${ChannelCode}";

    public static String downloadCode = "                    ".trim();
    public static final String jvmCode =  "/" + System.getProperty("microedition.platform");
    
    //#if Revision == CMCC || (Revision == JIANGSUNCMCC) || (Revision == QQ)
    //# public static final String officalWeb = "";
    //#else
    public static final String officalWeb = "hx.pipgame.cn";
    //#endif

//    public static final String helpString = "\n  百万年前，强大的龙族在内部战争中毁灭，六个信仰各种力量的龙族首领化身为弗埃蒂斯世界的大陆，他们的精神意志一直影响着这片土地上的万物。在后来的几十万年里，兽族、地精、魔族、人类和精灵逐渐繁盛，地精和魔族对自然资源的贪婪掠夺引发了世界战争，人类和精灵为捍卫自己领土及维护族群的安全付出了巨大的代价。最终，地精和魔族封印在黑暗的地下。随着人类的逐渐复苏，地精和魔族也开始躁动。你，人类的新成员，有着无数的挑战等待去征服......\n\n"
//                    + "快捷键介绍\n"
//                    + "【1】键：查看周围玩家列表\n"
//                    + "【2】键：同方向键上键\n"
//                    + "【3】键：进入物品背包\n"
//                    + "【4】键：同方向键左键\n"
//                    + "【5】键：确认功能\n"
//                    + "【6】键：同方向键右键\n"
//                    + "【7】键：小地图开关\n"
//                    + "【8】键：同方向键下键\n"
//                    + "【9】键：称号设置\n"
////#if Directory == MT-General
//                    //# + "【0】键：开启聊天输入\n" + "【*】键：聊天记录\n" + "【#】键：快速回复聊天\n" + "【左软键】：取消或返回\n" + "【右软键】：弹出菜单或确定\n";
////#else
//                    + "【0】键：开启聊天输入\n" + "【*】键：聊天记录\n" + "【#】键：快速回复聊天\n" + "【左软键】：弹出菜单或确定\n" + "【右软键】：取消或返回\n";
////#endif
    //#if Revision == CMCC || (Revision == JIANGSUNCMCC) || (Revision == QQ)
	    //# public static final String aboutString = "北京掌上明珠信息技术有限公司出品\n客服电话：010-64465123\n客服Email：support@pipfit.com\n版本：" + versionString
		  //# + "\n快捷键介绍\n"
		  //# + "【1】键：查看周围玩家列表\n"
		  //# + "【2】键：同方向键上键\n"
		  //# + "【3】键：进入物品背包\n"
		  //# + "【4】键：同方向键左键\n"
		  //# + "【5】键：确认功能\n"
		  //#  + "【6】键：同方向键右键\n"
		  //#  + "【7】键：小地图开关\n"
		  //#  + "【8】键：同方向键下键\n"
		  //# + "【9】键：称号设置\n"
				//#if Directory == MT-General
				    //# + "【0】键：开启聊天输入\n" + "【*】键：聊天记录\n" + "【#】键：快速回复聊天\n" + "【左软键】：取消或返回\n" + "【右软键】：弹出菜单或确定\n";
				//#else
		  //# + "【0】键：开启聊天输入\n" + "【*】键：聊天记录\n" + "【#】键：快速回复聊天\n" + "【左软键】：弹出菜单或确定\n" + "【右软键】：取消或返回\n";
				//#endif
    //#else
    public static final String aboutString = "北京掌上明珠信息技术有限公司出品\n客服电话：010-64465123\n客服Email：support@pipfit.com\n官方网站：hx.pipgame.cn\n版本：" + versionString
	    + "\n快捷键介绍\n"
	    + "【1】键：查看周围玩家列表\n"
	    + "【2】键：同方向键上键\n"
	    + "【3】键：进入物品背包\n"
	    + "【4】键：同方向键左键\n"
	    + "【5】键：确认功能\n"
	    + "【6】键：同方向键右键\n"
	    + "【7】键：小地图开关\n"
	    + "【8】键：同方向键下键\n"
	    + "【9】键：称号设置\n"
		//#if Directory == MT-General
		    //# + "【0】键：开启聊天输入\n" + "【*】键：聊天记录\n" + "【#】键：快速回复聊天\n" + "【左软键】：取消或返回\n" + "【右软键】：弹出菜单或确定\n";
		//#else
		    + "【0】键：开启聊天输入\n" + "【*】键：聊天记录\n" + "【#】键：快速回复聊天\n" + "【左软键】：弹出菜单或确定\n" + "【右软键】：取消或返回\n";
		//#endif
    //#endif

    private static ImageSet buttonImg = null;

    public static final byte BUTTON_LEFT = 1;
    public static final byte BUTTON_RIGHT = 2;
    public static final byte BUTTON_LEFT_RIGHT = 3;

    public byte waitType;

    public final static byte WAIT_NONE = 0;
    public final static byte WAIT_WAITPRESS = 1;
    public final static byte WAIT_WAITRESULT = 2;
    public final static byte WAIT_VIEW_ATTACHMENT = 3;
    public final static byte WAIT_GAME_PRESS = 4;
    public final static byte WAIT_79CONFIRM = 5;

    //#if Directory == SE-K700
    //# public static final int BBS_PAGE_COUNT = 7;
    //#elif (Directory == NK-40-2)
    //# public static final int BBS_PAGE_COUNT = 6;
    //#elif Directory == MT-V300
    //# public static final int BBS_PAGE_COUNT = 7;
    //#elif Directory == NK-BigScreen
    //# public static final int BBS_PAGE_COUNT = 10;
    //#elif (Directory == NK-Nokia403Big)
    //# public static final int BBS_PAGE_COUNT = 13;
    //#elif (Directory == SE-S700)
    //# public static final int BBS_PAGE_COUNT = 9;
    //#elif (Directory == MT-General) || (Directory == Midp2-General) || (Directory == SE-K500) || (Directory == SE-K300) || (Directory == Nokia403) || (MIDP2Common == true) || (Directory == ClientTouch-E680) || (Directory == ClientTouch--Midp2-General) || (Directory == ClientTouch-SE-General) || (Directory == ClientTouch-Nokia5800)
    public static int BBS_PAGE_COUNT;
    //#else
    //# public static final int BBS_PAGE_COUNT = 9;
    //#endif

    public final static int TIME_OUT = 30 * 1000;
    public long clock = 0;
    public int backState = 0;
    public int _backState;
    public boolean needExit;
    public boolean canCancel;
    public byte showType = SHOW_MESSAGE;
    public boolean actorPageChange=false; //角色登陆换页
    public boolean changDirect = false;//字体回转翻
    public final static byte SHOW_MESSAGE = 1;
    public final static byte SHOW_CUSTOM = 2;

    public int param;

    public static int[] touchNpcInfo = null;

    public static final int[] EDGE_COLOR = {
                    0x000000, 0xD09C2F, 0x004E5C, 0xBFCFBF, 0x8A988A, 0xBAD99B
    };

    public static final String TASK_DIVID = "#";
    public static final byte INPUT_PARA_NUMBER = 1;
    public static final byte INPUT_PARA_ANY = 2;
    public static final int TASK_UI_DEFAULT_SELECT_COLOR = EDGE_COLOR[5];

    public static boolean taskUIReady = false;
    public static int taskUIBackState = -1;
    public static int taskUIType = 0; //0: list, 1: content
    public static String taskUITitle = null;
    public static String[] taskUIList = null;
    public static String[] taskUICommand = null;
    public static int taskUITitleColor;
    public static int[] taskUIListColor = null;
    public static int[] taskUICommandColor = null;

    public static int taskUICommandNormalColor;
    public static int taskUICommandCurrentSelect;

    public static boolean taskUICommandShowing = false;
    public static String[] taskUIInputPara = null;
    public static String[] taskUIInputResult = null;
    public static byte taskUIInputStatus = 0;
    public static String taskUIErrorMessage = null;
    public static int taskUIListScrollOffset;
    public static int taskUIListScrollDir;
    public static int taskUIListScrollMax;
    public static boolean taskUIListNeedScroll;
    public static int taskUITitleScrollOffset;
    public static int taskUITitleScrollDir;
    public static int taskUITitleScrollMax;
    public static boolean taskUITitleNeedScroll;
    public static int taskUIListSelect;
    //#if TouchScreen == true
    public static boolean taskUIListSelectTarget;//按了一次的判断
    public static boolean firstPressed; //用以区分需要按两次还是一次
    public static boolean autoArrive;//按下的目标是否到达 
    public static boolean pressedVM;//判断是又按键响应，还是 触摸相应
    //#endif

    public static int taskUISelectColor = TASK_UI_DEFAULT_SELECT_COLOR;

    public static GameItem showGameItem = null;
    public static GameItem attachmentTypeItem = null;

    public static boolean taskUIGameRequest = false;
    public static int taskUIGameWidth;
    public static int taskUIGameHeight;
    public static int taskUIGameBaseX;
    public static int taskUIGameBaseY;
    public static int taskUIGameBackColor;
    public static int taskUIGameItemCount;
    public static int[][] taskUIGameItemInfo; //[0] x, [1] y, [2] w, [3] h, [4] type, [5] number or frame index [6] backColor [7] color
    public static String[] taskUIGameItemString;
    public static int taskUIGameScore;

    public static final byte TASK_UI_INPUT_NONE = 0;
    public static final byte TASK_UI_INPUT_DOING = 1;
    public static final byte TASK_UI_INPUT_BACK = 2;
    public static final byte TASK_UI_INPUT_DONE = 3;

    public static final byte TASK_UI_LIST = 0;
    public static final byte TASK_UI_CONTENT = 1;
    public static final byte TASK_UI_GAME = 2;

    public static final byte ATTR_TYPE_UIMODE = 0;
    public static final byte ATTR_TYPE_TASKUI = 1;
    public static final byte ATTR_TYPE_BBS = 2;
    public static final byte ATTR_TYPE_BATTLE_SKILL = 3;
    public static final byte ATTR_TYPE_PRODUCT_SKILL = 4;
    public static final byte ATTR_TYPE_CHAT_CIRCLE = 5;
    public static final byte ATTR_TYPE_TASK_VIEW = 6;
    public static final byte ATTR_TYPE_PLAYER_VIEW = 7;
    public static final byte ATTR_TYPE_FRIEND_VIEW = 8;
    public static final byte ATTR_TYPE_MAIL = 9;
    public static final byte ATTR_TYPE_SHOP = 10;
    public static final byte ATTR_TYPE_AUCTION = 11;
    public static final byte ATTR_TYPE_BUY_MATERIAL = 12;
    public static final byte ATTR_TYPE_OEM = 13;
    public static final byte ATTR_TYPE_STORE = 14;
    public static final byte ATTR_TYPE_VIEW_BATTLE_SKILL = 15;
    public static final byte ATTR_TYPE_PRODUCT_ITEM = 16;
    public static final byte ATTR_TYPE_TONGLIST = 17;
    public static final byte ATTR_TYPE_CHATOPTION = 18;
    public static final byte ATTR_TYPE_PET = 19;
    public static final byte ATTR_TYPE_PET_TRADE = 20;
    public static final byte ATTR_TYPE_SYSTEM_OPTION = 21;
    public static final byte ATTR_TYPE_REPAIR = 22;
    public static final byte ATTR_TYPE_BLACK_VIEW = 23;
    public static final byte ATTR_TYPE_VIEW_EQUIP = 24;
    public static final byte ATTR_TYPE_GENERIC_LIST = 25;
    public static final byte ATTR_TYPE_ISTORE = 26;

    public static final byte ATTR_TYPE_PLAYER_DATA = 99;

    public static final byte ATTR_INDEX_PLAYER_DATA_LEVEL = 0;
    public static final byte ATTR_INDEX_PLAYER_DATA_EXP = 1;
    public static final byte ATTR_INDEX_PLAYER_DATA_MONEY = 2;
    public static final byte ATTR_INDEX_PLAYER_DATA_SEX = 3;
    public static final byte ATTR_INDEX_PLAYER_DATA_HP = 4;
    public static final byte ATTR_INDEX_PLAYER_DATA_MP = 5;
    public static final byte ATTR_INDEX_PLAYER_DATA_HPLIMIT = 6;
    public static final byte ATTR_INDEX_PLAYER_DATA_MPLIMIT = 7;
    public static final byte ATTR_INDEX_PLAYER_DATA_CLEAR_TOUCH_NPC = 8;
    public static final byte ATTR_INDEX_PLAYER_DATA_TONG_NAME = 9;
    public static final byte ATTR_INDEX_PLAYER_DATA_TONG_DUTY = 10;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_TONG_DUTY_NAME = 11;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_NEED_REFRESH_HINT = 12;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_SERVER_TIME_HOUR = 13;

    public static final byte ATTR_INDEX_PLAYER_DATA_GET_SERVER_TIME_DAY = 100;

    public static final byte ATTR_INDEX_PLAYER_DATA_GET_SKILL_COUNT = 14;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_SKILL = 15;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_REST_ABILITY = 16;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_SKILL_NAME = 17;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_TYPE_ABILITY = 18;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_TYPE_NAME = 19;
    public static final byte ATTR_INDEX_PLAYER_DATA_ADD_SKILL = 20;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_SKILL_TYPE = 21;
    public static final byte ATTR_INDEX_PLAYER_DATA_CHANGE_MONEY = 22;
    public static final byte ATTR_INDEX_PLAYER_DATA_CHANGE_ABILITY = 23;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_PRODUCT_SKILL_COUNT = 24;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_PRODUCT_SKILL_LIST = 25;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_PRODUCT_SKILL_NAME_LIST = 26;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_UNFINISH_TASK_COUNT = 27;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_UNFINISH_TASK_ID = 28;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_UNFINISH_TASK_NAME = 29;
    public static final byte ATTR_INDEX_PLAYER_DATA_ABANDON_TASK = 30;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_NET_PLAYER_COUNT = 31;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_NET_PLAYER_ID_LIST = 32;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_NET_PLAYER_NAME_LIST = 33;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_NET_PLAYER_SEX = 34;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_NET_PLAYER_LEVEL = 35;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_NET_PLAYER_TEAM_MODE = 36;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_NET_PLAYER_TONG_NAME = 37;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_TEAM_LEADER = 38;
    public static final byte ATTR_INDEX_PLAYER_DATA_ADD_FRIEND = 39;
    public static final byte ATTR_INDEX_PLAYER_DATA_CHANGE_FRIEND_ONLINE = 40;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_FRIEND_COUNT = 41;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_FRIEND_ID_LIST = 42;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_FRIEND_NAME_LIST = 43;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_FRIEND_ONLINE_LIST = 44;
    public static final byte ATTR_INDEX_PLAYER_DATA_DELETE_FRIEND = 45;
    public static final byte ATTR_INDEX_PLAYER_DATA_CLEAR_NEW_MAIL_HINT = 46;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_VIT = 47;
    public static final byte ATTR_INDEX_PLAYER_DATA_CHANGE_VIT = 48;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_CHAT_OPTION_COUNT = 49;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_CHAT_OPTION_PRIORITY = 50;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_CHAT_OPTION_COLOR = 51;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_CHAT_OPTION_NAME = 52;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_CHAT_OPTION_CAN_CHANGE = 53;
    public static final byte ATTR_INDEX_PLAYER_DATA_CHANGE_CHAT_OPTION_PRIORITY = 54;
    public static final byte ATTR_INDEX_PLAYER_DATA_CHANGE_CHAT_OPTION_COLOR = 55;
    public static final byte ATTR_INDEX_PLAYER_DATA_COMMIT_CHAT_OPTION = 56;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_PET_COUNT = 57;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_PET_ID = 58;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_PET_TYPE = 59;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_PET_FEALTY = 60;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_PET_NAME = 61;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_PET_VIT = 62;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_PET_STR = 63;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_PET_AGI = 64;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_PET_INT = 65;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_PET_TYPE_NAME = 66;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_PET_FEALTY_NAME = 67;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_PET_IS_CURRENT = 68;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_PET_REST_POINT = 69;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_PET_UNTRADE_POINT = 70;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_PET_LEVEL = 71;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_PET_EXP = 72;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_PET_UPGRADE_EXP = 73;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_PET_HP = 74;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_PET_MP = 75;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_PET_MAXHP = 76;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_PET_MAXMP = 77;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_PET_FEALTY_LEVEL = 78;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_PET_SKILL_COUNT = 79;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_PET_SKILL_ID = 80;
    public static final byte ATTR_INDEX_PLAYER_DATA_SET_CURRENT_PET = 81;
    public static final byte ATTR_INDEX_PLAYER_DATA_RELEASE_PET = 82;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_CURRENT_PET_ID = 83;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_SYSTEM_OPTION_COUNT = 84;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_SYSTEM_OPTION_ID = 85;
    public static final byte ATTR_INDEX_PLAYER_DATA_SET_SYSTEM_OPTIN = 86;
    public static final byte ATTR_INDEX_PLAYER_DATA_CONFIRM_SYSTEM_OPTION = 87;
    public static final byte ATTR_INDEX_PLAYER_DATA_OFF_ALL_EQUIP = 89;

    public static final byte ATTR_INDEX_PLAYER_DATA_OPEN_URL = 90;
    public static final byte ATTR_INDEX_PLAYER_DATA_SHOW_DEBUG_MSG = 99;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_BLACK_ID_LIST = 101;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_BLACK_NAME_LIST = 102;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_FRIEND_DEGREE_LIST = 103;
    public static final byte ATTR_INDEX_PLAYER_DATA_ADD_BLACK = 104;
    public static final byte ATTR_INDEX_PLAYER_DATA_DELETE_BLACK = 105;
    public static final byte ATTR_INDEX_PLAYER_DATA_GET_BLACK_COUNT = 106;

    public static final byte ATTR_INDEX_PLAYER_DATA_OPEN_WEB = 107;

    public static final byte UIMODE_UNSET = 0;
    public static final byte UIMODE_SET_BLOCK = 1;
    public static final byte UIMODE_SET_NOBLOCK = 2;

    public static final byte ATTR_INDEX_TASKUI_TITLE = 0;
    public static final byte ATTR_INDEX_TASKUI_LIST = 1;
    public static final byte ATTR_INDEX_TASKUI_WAIT = 2;
    public static final byte ATTR_INDEX_TASKUI_INPUT_PARA = 3;
    public static final byte ATTR_INDEX_TASKUI_START_INPUT = 4;
    public static final byte ATTR_INDEX_TASKUI_INPUT_RESULT = 5;
    public static final byte ATTR_INDEX_TASKUI_READY = 6;
    public static final byte ATTR_INDEX_TASKUI_TYPE = 7;
    public static final byte ATTR_INDEX_TASKUI_COMMAND = 8;
    public static final byte ATTR_INDEX_TASKUI_SHOW_COMMAND = 9;
    public static final byte ATTR_INDEX_TASKUI_SET_TITLE_COLOR = 10;
    public static final byte ATTR_INDEX_TASKUI_SET_LIST_COLOR = 11;
    public static final byte ATTR_INDEX_TASKUI_SET_COMMAND_COLOR = 12;
    public static final byte ATTR_INDEX_TASKUI_GET_INPUT_STATUS = 13;
    public static final byte ATTR_INDEX_TASKUI_SHOW_ERROR_MSG = 14;

    public static final byte ATTR_INDEX_TASKUI_GAME_SET_WIDTH = 15;
    public static final byte ATTR_INDEX_TASKUI_GAME_SET_HEIGHT = 16;
    public static final byte ATTR_INDEX_TASKUI_GAME_SET_BACK_COLOR = 17;
    public static final byte ATTR_INDEX_TASKUI_GAME_SET_ITEM_COUNT = 18;
    public static final byte ATTR_INDEX_TASKUI_GAME_SET_ITEM_INFO = 19;
    public static final byte ATTR_INDEX_TASKUI_GAME_REQUEST = 20;
    public static final byte ATTR_INDEX_TASKUI_GAME_SET_ITEM_FRAME = 21;
    public static final byte ATTR_INDEX_TASKUI_GAME_SET_SCORE = 22;

    public static final byte ATTR_INDEX_TASKUI_SET_SELECT = 23;
    public static final byte ATTR_INDEX_TASKUI_SET_SELECT_COLOR = 27;

    public static final byte ATTR_INDEX_TASKUI_NEW_UI_DATA = 50;
    public static final byte ATTR_INDEX_TASKUI_READ_UI_DATA = 51;
    public static final byte ATTR_INDEX_TASKUI_GET_SEGMENT_DATA = 52;
    public static final byte ATTR_INDEX_TASKUI_GET_SEGMENT_DATA_STRING = 53;
    public static final byte ATTR_INDEX_TASKUI_SET_UI_DATA = 54;
    public static final byte ATTR_INDEX_TASKUI_CLEAR_SEGMENT = 55;
    public static final byte ATTR_INDEX_TASKUI_RESET_SEGMENT = 56;
    public static final byte ATTR_INDEX_TASKUI_READ_ITEM_FROM_BYTES = 57;
    public static final byte ATTR_INDEX_TASKUI_GET_ITEM_EQUIP_TYPE_NAME = 58;
    public static final byte ATTR_INDEX_TASKUI_GET_ITEM_NAME = 59;
    public static final byte ATTR_INDEX_TASKUI_SHOW_ITEM = 60;
    public static final byte ATTR_INDEX_TAKSUI_GET_ITEM_EQUIP_Q = 61;
    public static final byte ATTR_INDEX_TASKUI_GET_ITEM_ITEM_TYPE = 62;
    public static final byte ATTR_INDEX_TASKUI_GET_ITEM_ITEM_ID = 63;
    public static final byte ATTR_INDEX_TASKUI_GET_ITEM_ID_OR_COUNT = 64;
    public static final byte ATTR_INDEX_TASKUI_GET_ITEM_PRICE = 65;
    public static final byte ATTR_INDEX_TASKUI_READ_ATTACHMENT_FROM_BYTES = 66;
    public static final byte ATTR_INDEX_TASKUI_BUILD_BAG_ITEM_LIST = 67;
    public static final byte ATTR_INDEX_TASKUI_BUILD_SEND_ATTACHMENT = 68;
    public static final byte ATTR_INDEX_TASKUI_DELETE_ATTACHMENT_ITEM = 69;
    public static final byte ATTR_INDEX_TASKUI_COPY_UI_DATA = 70;
    public static final byte ATTR_INDEX_TASKUI_BUILD_STREAM_FROM_BYTES = 71;
    public static final byte ATTR_INDEX_TASKUI_GET_UI_DATA_FROM_STREAM = 72;
    public static final byte ATTR_INDEX_TASKUI_GET_UI_STRING_FROM_STREAM = 73;
    public static final byte ATTR_INDEX_TASKUI_CLEAR_STREAM = 74;
    public static final byte ATTR_INDEX_TASKUI_CREATE_GAMEITEM = 75;
    public static final byte ATTR_INDEX_TASKUI_ADD_ITEM_TO_BAG = 76;
    public static final byte ATTR_INDEX_TASKUI_GET_MATERIAL_BEGIN_ID = 77;
    public static final byte ATTR_INDEX_TASKUI_GET_MATERIAL_END_ID = 78;
    public static final byte ATTR_INDEX_TASKUI_HAS_ITEM = 79;
    public static final byte ATTR_INDEX_TASKUI_GET_ERROR_MESSAGE = 80;
    public static final byte ATTR_INDEX_TASKUI_CLEAR_ERROR = 81;
    public static final byte ATTR_INDEX_TASKUI_SET_EQUIP_REPAIR_FEE = 82;
    public static final byte ATTR_INDEX_TASKUI_SET_EQUIP_CURRENT_EQUIP = 83;
    public static final byte ATTR_INDEX_TASKUI_REPAIR_EQUIPS = 84;
    public static final byte ATTR_INDEX_TASKUI_GET_ITEM_REPAIR_FEE = 85;
    public static final byte ATTR_INDEX_TASKUI_GET_ITEM_CURRENT_EQUIP = 86;
    public static final byte ATTR_INDEX_TASKUI_CONFIRM_REPAIR = 87;
    public static final byte ATTR_INDEX_TASKUI_GET_ITEM_EQUIP_COLOR = 88;

    public static final byte ATTR_INDEX_BBS_GET_PAGECOUNT = 0;
    public static final byte ATTR_INDEX_BBS_GET_LIST_COUNT = 1;
    public static final byte ATTR_INDEX_BBS_GET_LIST = 2;
    public static final byte ATTR_INDEX_BBS_GET_CONTENT = 3;
    public static final byte ATTR_INDEX_BBS_PREPARE_NET = 4;
    public static final byte ATTR_INDEX_BBS_DATA_OK = 5;
    public static final byte ATTR_INDEX_BBS_GET_TOTAL_PAGE = 6;
    public static final byte ATTR_INDEX_BBS_GET_PAGE_NO = 7;
    public static final byte ATTR_INDEX_BBS_GET_BBSID = 8;
    public static final byte ATTR_INDEX_BBS_GET_LIST_ID = 9;
    public static final byte ATTR_INDEX_BBS_REQUEST = 10;
    public static final byte ATTR_INDEX_BBS_DATA_ERROR = 11;

    public static final byte ATTR_INDEX_BATTLE_SKILL_GET_PAGECOUNT = 0;
    public static final byte ATTR_INDEX_BATTLE_SKILL_GET_NPCID = 1;
    public static final byte ATTR_INDEX_BATTLE_SKILL_GET_LIST_COUNT = 2;
    public static final byte ATTR_INDEX_BATTLE_SKILL_GET_LIST_ID = 3;
    public static final byte ATTR_INDEX_BATTLE_SKILL_GET_LIST_NAME = 4;
    public static final byte ATTR_INDEX_BATTLE_SKILL_DATA_OK = 5;
    public static final byte ATTR_INDEX_BATTLE_SKILL_REQUEST = 6;
    public static final byte ATTR_INDEX_BATTLE_SKILL_DATA_ERROR = 7;
    public static final byte ATTR_INDEX_BATTLE_SKILL_PREPARE_NET = 8;
    public static final byte ATTR_INDEX_BATTLE_SKILL_GET_TITLE = 9;
    public static final byte ATTR_INDEX_BATTLE_SKILL_GET_CONTENT = 10;
    public static final byte ATTR_INDEX_BATTLE_SKILL_GET_LIST_CAN_LEARN = 11;
    public static final byte ATTR_INDEX_BATTLE_SKILL_GET_LIST_PRICE = 12;
    public static final byte ATTR_INDEX_BATTLE_SKILL_GET_LIST_LEVEL = 13;
    public static final byte ATTR_INDEX_BATTLE_SKILL_GET_TYPE = 14;

    public static final byte ATTR_INDEX_PRODUCT_SKILL_GET_PAGECOUNT = 0;
    public static final byte ATTR_INDEX_PRODUCT_SKILL_GET_NPCID = 1;
    public static final byte ATTR_INDEX_PRODUCT_SKILL_GET_LIST_COUNT = 2;
    public static final byte ATTR_INDEX_PRODUCT_SKILL_GET_LIST_ID = 3;
    public static final byte ATTR_INDEX_PRODUCT_SKILL_GET_LIST_NAME = 4;
    public static final byte ATTR_INDEX_PRODUCT_SKILL_DATA_OK = 5;
    public static final byte ATTR_INDEX_PRODUCT_SKILL_REQUEST = 6;
    public static final byte ATTR_INDEX_PRODUCT_SKILL_DATA_ERROR = 7;
    public static final byte ATTR_INDEX_PRODUCT_SKILL_PREPARE_NET = 8;
    public static final byte ATTR_INDEX_PRODUCT_SKILL_GET_TITLE = 9;
    public static final byte ATTR_INDEX_PRODUCT_SKILL_GET_CONTENT = 10;

    public static final byte ATTR_INDEX_CHAT_CIRCLE_GET_PAGECOUNT = 0;
    public static final byte ATTR_INDEX_CHAT_CIRCLE_GET_LIST_COUNT = 1;
    public static final byte ATTR_INDEX_CHAT_CIRCLE_GET_LIST_NAME = 2;
    public static final byte ATTR_INDEX_CHAT_CIRCLE_DATA_OK = 3;
    public static final byte ATTR_INDEX_CHAT_CIRCLE_REQUEST = 4;
    public static final byte ATTR_INDEX_CHAT_CIRCLE_DATA_ERROR = 5;
    public static final byte ATTR_INDEX_CHAT_CIRCLE_PREPARE_NET = 6;
    public static final byte ATTR_INDEX_CHAT_CIRCLE_GET_CONTENT = 7;

    public static final byte ATTR_INDEX_TASK_VIEW_GET_PAGECOUNT = 0;
    public static final byte ATTR_INDEX_TASK_VIEW_GET_LIST_COUNT = 1;
    public static final byte ATTR_INDEX_TASK_VIEW_GET_LIST_ID = 2;
    public static final byte ATTR_INDEX_TASK_VIEW_GET_LIST_NAME = 3;
    public static final byte ATTR_INDEX_TASK_VIEW_DATA_OK = 4;
    public static final byte ATTR_INDEX_TASK_VIEW_REQUEST = 5;
    public static final byte ATTR_INDEX_TASK_VIEW_DATA_ERROR = 6;
    public static final byte ATTR_INDEX_TASK_VIEW_PREPARE_NET = 7;
    public static final byte ATTR_INDEX_TASK_VIEW_GET_CONTENT = 8;
    public static final byte ATTR_INDEX_TASK_VIEW_GET_ABANDON_RESULT = 9;
    public static final byte ATTR_INDEX_TASK_VIEW_ABANDON_TASK = 10;
    public static final byte ATTR_INDEX_TASK_VIEW_BUILD_LIST = 11;

    public static final byte ATTR_INDEX_PLAYER_VIEW_GET_PAGECOUNT = 0;
    public static final byte ATTR_INDEX_PLAYER_VIEW_GET_LIST_COUNT = 1;
    public static final byte ATTR_INDEX_PLAYER_VIEW_GET_LIST_ID = 2;
    public static final byte ATTR_INDEX_PLAYER_VIEW_GET_LIST_NAME = 3;
    public static final byte ATTR_INDEX_PLAYER_VIEW_DATA_OK = 4;
    public static final byte ATTR_INDEX_PLAYER_VIEW_REQUEST = 5;
    public static final byte ATTR_INDEX_PLAYER_VIEW_DATA_ERROR = 6;
    public static final byte ATTR_INDEX_PLAYER_VIEW_PREPARE_NET = 7;
    public static final byte ATTR_INDEX_PLAYER_VIEW_GET_CONTENT = 8;
    public static final byte ATTR_INDEX_PLAYER_VIEW_GET_NAME = 9;
    public static final byte ATTR_INDEX_PLAYER_VIEW_GET_EQUIP_COUNT = 10;
    public static final byte ATTR_INDEX_PLAYER_VIEW_GET_EQUIP_LIST = 11;
    public static final byte ATTR_INDEX_PLAYER_VIEW_GET_EQUIP_BYTES_COUNT = 12;
    public static final byte ATTR_INDEX_PLAYER_VIEW_GET_FRIEND_ONLINE_RESULT = 13;

    public static final byte ATTR_INDEX_FRIEND_VIEW_GET_PAGECOUNT = 0;
    public static final byte ATTR_INDEX_FRIEND_VIEW_GET_LIST_COUNT = 1;
    public static final byte ATTR_INDEX_FRIEND_VIEW_GET_LIST_ID = 2;
    public static final byte ATTR_INDEX_FRIEND_VIEW_GET_LIST_NAME = 3;
    public static final byte ATTR_INDEX_FRIEND_VIEW_DATA_OK = 4;
    public static final byte ATTR_INDEX_FRIEND_VIEW_REQUEST = 5;
    public static final byte ATTR_INDEX_FRIEND_VIEW_DATA_ERROR = 6;
    public static final byte ATTR_INDEX_FRIEND_VIEW_PREPARE_NET = 7;
    public static final byte ATTR_INDEX_FRIEND_VIEW_GET_CONTENT = 8;
    public static final byte ATTR_INDEX_FRIEND_VIEW_GET_NAME = 9;
    public static final byte ATTR_INDEX_FRIEND_VIEW_GET_ONLINE = 10;
    public static final byte ATTR_INDEX_FRINED_VIEW_GET_ADDING_PLAYER_ID = 11;
    public static final byte ATTR_INDEX_FRIEND_VIEW_GET_ADDING_PLAYER_ONLINE = 12;

    public static final byte ATTR_INDEX_BLACK_VIEW_GET_PAGECOUNT = 0;
    public static final byte ATTR_INDEX_BLACK_VIEW_GET_LIST_COUNT = 1;
    public static final byte ATTR_INDEX_BLACK_VIEW_GET_LIST_ID = 2;
    public static final byte ATTR_INDEX_BLACK_VIEW_GET_LIST_NAME = 3;
    public static final byte ATTR_INDEX_BLACK_VIEW_DATA_OK = 4;
    public static final byte ATTR_INDEX_BLACK_VIEW_REQUEST = 5;
    public static final byte ATTR_INDEX_BLACK_VIEW_DATA_ERROR = 6;
    public static final byte ATTR_INDEX_BLACK_VIEW_PREPARE_NET = 7;
    public static final byte ATTR_INDEX_BLACK_VIEW_GET_CONTENT = 8;
    public static final byte ATTR_INDEX_BLACK_VIEW_GET_NAME = 9;
    public static final byte ATTR_INDEX_BLACK_VIEW_GET_ONLINE = 10;
    public static final byte ATTR_INDEX_BLACK_VIEW_GET_ADDING_PLAYER_ID = 11;
    public static final byte ATTR_INDEX_BLACK_VIEW_GET_ADDING_PLAYER_ONLINE = 12;

    public static final byte ATTR_INDEX_MAIL_GET_PAGECOUNT = 0;
    public static final byte ATTR_INDEX_MAIL_GET_LIST_COUNT = 1;
    public static final byte ATTR_INDEX_MAIL_GET_LIST = 2;
    public static final byte ATTR_INDEX_MAIL_GET_CONTENT = 3;
    public static final byte ATTR_INDEX_MAIL_PREPARE_NET = 4;
    public static final byte ATTR_INDEX_MAIL_DATA_OK = 5;
    public static final byte ATTR_INDEX_MAIL_GET_TOTAL_PAGE = 6;
    public static final byte ATTR_INDEX_MAIL_GET_PAGE_NO = 7;
    public static final byte ATTR_INDEX_MAIL_GET_LIST_ID = 8;
    public static final byte ATTR_INDEX_MAIL_REQUEST = 9;
    public static final byte ATTR_INDEX_MAIL_DATA_ERROR = 10;
    public static final byte ATTR_INDEX_MAIL_GET_ATTACH_FLAG = 11;
    public static final byte ATTR_INDEX_MAIL_GET_READ_FLAG = 12;
    public static final byte ATTR_INDEX_MAIL_GET_PRICE = 13;
    public static final byte ATTR_INDEX_MAIL_GET_AUTHOR = 14;
    public static final byte ATTR_INDEX_MAIL_GET_SEND_ITEM_TYPE_LIST = 15;
    public static final byte ATTR_INDEX_MAIL_GET_SEND_ITEM_ID_LIST = 16;
    public static final byte ATTR_INDEX_MAIL_GET_SEND_ITEM_ID_OR_COUNT_LIST = 17;
    public static final byte ATTR_INDEX_MAIL_GET_SEND_ITEM_NAME_LIST = 18;
    public static final byte ATTR_INDEX_MAIL_GET_SEND_ITEM_LIST_COUNT = 19;
    public static final byte ATTR_INDEX_MAIL_GET_ATTACHMENT_BYTES_COUNT = 20;
    public static final byte ATTR_INDEX_MAIL_GET_BUILD_ITEMS_COUNT = 21;

    public static final byte ATTR_INDEX_SHOP_GET_PAGECOUNT = 0;
    public static final byte ATTR_INDEX_SHOP_DATA_OK = 1;
    public static final byte ATTR_INDEX_SHOP_GET_TOTAL_PAGE = 2;
    public static final byte ATTR_INDEX_SHOP_GET_PAGE_NO = 3;
    public static final byte ATTR_INDEX_SHOP_REQUEST = 4;
    public static final byte ATTR_INDEX_SHOP_DATA_ERROR = 5;
    public static final byte ATTR_INDEX_SHOP_PREPARE_NET = 6;
    public static final byte ATTR_INDEX_SHOP_GET_SHOP_LIST_COUNT = 7;
    public static final byte ATTR_INDEX_SHOP_GET_SHOP_ID_LIST = 8;
    public static final byte ATTR_INDEX_SHOP_GET_SHOP_NAME_LIST = 9;
    public static final byte ATTR_INDEX_SHOP_GET_SHOP_CONTENT = 10;
    public static final byte ATTR_INDEX_SHOP_GET_ITEM_LIST_COUNT = 11;
    public static final byte ATTR_INDEX_SHOP_GET_ITEM_ID_LIST = 12;
    public static final byte ATTR_INDEX_SHOP_GET_ITEM_NAME_LIST = 13;
    public static final byte ATTR_INDEX_SHOP_GET_ITEM_CONTENT = 14;
    public static final byte ATTR_INEX_SHOP_GET_TOUCH_NPC_ID = 15;
    public static final byte ATTR_INDEX_SHOP_GET_ITEM_BUY_GET_LIST_COUNT = 16;
    public static final byte ATTR_INDEX_SHOP_GET_ITEM_BUY_GET_LIST = 17;
    public static final byte ATTR_INDEX_SHOP_GET_ITEM_BUY_GET_ID_LIST = 18;
    public static final byte ATTR_INDEX_SHOP_GET_STORE_ITEM_TYPE_LIST = 19;
    public static final byte ATTR_INDEX_SHOP_GET_STORE_ITEM_ID_LIST = 20;
    public static final byte ATTR_INDEX_SHOP_GET_STORE_ITEM_ID_OR_COUNT_LIST = 21;
    public static final byte ATTR_INDEX_SHOP_GET_STORE_ITEM_NAME_LIST = 22;
    public static final byte ATTR_INDEX_SHOP_GET_STORE_ITEM_LIST_COUNT = 23;
    public static final byte ATTR_INDEX_SHOP_GET_STORE_ITEM_COUNT = 24;
    public static final byte ATTR_INDEX_SHOP_GET_STORE_ITEM_TYPE = 25;
    public static final byte ATTR_INDEX_SHOP_GET_STORE_ITEM_ID = 26;
    public static final byte ATTR_INDEX_SHOP_GET_STORE_ITEM_Q = 27;

    public static final byte ATTR_INDEX_BUY_MATERIAL_GET_PAGECOUNT = 0;
    public static final byte ATTR_INDEX_BUY_MATERIAL_DATA_OK = 1;
    public static final byte ATTR_INDEX_BUY_MATERIAL_GET_TOTAL_PAGE = 2;
    public static final byte ATTR_INDEX_BUY_MATERIAL_GET_PAGE_NO = 3;
    public static final byte ATTR_INDEX_BUY_MATERIAL_REQUEST = 4;
    public static final byte ATTR_INDEX_BUY_MATERIAL_DATA_ERROR = 5;
    public static final byte ATTR_INDEX_BUY_MATERIAL_PREPARE_NET = 6;
    public static final byte ATTR_INDEX_BUY_MATERIAL_GET_AREA_ID = 7;
    public static final byte ATTR_INDEX_BUY_MATERIAL_GET_TYPE_LIST_COUNT = 8;
    public static final byte ATTR_INDEX_BUY_MATERIAL_GET_TYPE_ID_LIST = 9;
    public static final byte ATTR_INDEX_BUY_MATERIAL_GET_TYPE_NAME_LIST = 10;
    public static final byte ATTR_INDEX_BUY_MATERIAL_GET_MATERIAL_LIST_COUNT = 11;
    public static final byte ATTR_INDEX_BUY_MATERIAL_GET_MATERIAL_ID_LIST = 12;
    public static final byte ATTR_INDEX_BUY_MATERIAL_GET_MATERIAL_NAME_LIST = 13;
    public static final byte ATTR_INDEX_BUY_MATERIAL_GET_MATERIAL_CONTENT = 14;
    public static final byte ATTR_INDEX_BUY_MATERIAL_GET_MATERIAL_ITEM_ID_LIST = 15;
    public static final byte ATTR_INDEX_BUY_MATERIAL_GET_TOUCH_NPC_ID = 16;

    public static final byte ATTR_INDEX_OEM_GET_PAGECOUNT = 0;
    public static final byte ATTR_INDEX_OEM_DATA_OK = 1;
    public static final byte ATTR_INDEX_OEM_GET_TOTAL_PAGE = 2;
    public static final byte ATTR_INDEX_OEM_GET_PAGE_NO = 3;
    public static final byte ATTR_INDEX_OEM_REQUEST = 4;
    public static final byte ATTR_INDEX_OEM_DATA_ERROR = 5;
    public static final byte ATTR_INDEX_OEM_PREPARE_NET = 6;
    public static final byte ATTR_INDEX_OEM_GET_AREA_ID = 7;
    public static final byte ATTR_INDEX_OEM_GET_TYPE_LIST_COUNT = 8;
    public static final byte ATTR_INDEX_OEM_GET_TYPE_ID_LIST = 9;
    public static final byte ATTR_INDEX_OEM_GET_TYPE_NAME_LIST = 10;
    public static final byte ATTR_INDEX_OEM_GET_OEM_LIST_COUNT = 11;
    public static final byte ATTR_INDEX_OEM_GET_OEM_ID_LIST = 12;
    public static final byte ATTR_INDEX_OEM_GET_OEM_NAME_LIST = 13;
    public static final byte ATTR_INDEX_OEM_GET_OEM_CONTENT = 14;
    public static final byte ATTR_INDEX_OEM_GET_TOUCH_NPC_ID = 15;
    public static final byte ATTR_INDEX_OEM_GET_CAN_MAKE = 16;
    public static final byte ATTR_INDEX_OEM_GET_NEED_GAME = 17;

    public static final byte ATTR_INDEX_STORE_GET_PAGECOUNT = 0;
    public static final byte ATTR_INDEX_STORE_DATA_OK = 1;
    public static final byte ATTR_INDEX_STORE_GET_TOTAL_PAGE = 2;
    public static final byte ATTR_INDEX_STORE_GET_PAGE_NO = 3;
    public static final byte ATTR_INDEX_STORE_REQUEST = 4;
    public static final byte ATTR_INDEX_STORE_DATA_ERROR = 5;
    public static final byte ATTR_INDEX_STORE_PREPARE_NET = 6;
    public static final byte ATTR_INDEX_STORE_GET_TOUCH_NPC_ID = 7;
    public static final byte ATTR_INDEX_STORE_GET_LIST_COUNT = 8;
    public static final byte ATTR_INDEX_STORE_GET_ID_LIST = 9;
    public static final byte ATTR_INDEX_STORE_GET_NAME_LIST = 10;
    public static final byte ATTR_INDEX_STORE_GET_CONTENT = 11;
    public static final byte ATTR_INDEX_STORE_GET_SELL_ITME_TYPE_LIST = 12;
    public static final byte ATTR_INDEX_STORE_GET_SELL_ITEM_ID_LIST = 13;
    public static final byte ATTR_INDEX_STORE_GET_ID_OR_COUNT_LIST = 14;
    public static final byte ATTR_INDEX_STORE_GET_SELL_NAME_LIST = 15;
    public static final byte ATTR_INDEX_STORE_GET_SELL_LIST_COUNT = 16;
    public static final byte ATTR_INDEX_STORE_GET_TRADE_RESULT_BYTES_COUNT = 17;
    public static final byte ATTR_INDEX_STORE_GET_PRICE_LIST = 18;

    public static final byte ATTR_INDEX_VIEW_BATTLE_SKILL_GET_PAGECOUNT = 0;
    public static final byte ATTR_INDEX_VIEW_BATTLE_SKILL_DATA_OK = 1;
    public static final byte ATTR_INDEX_VIEW_BATTLE_SKILL_REQUEST = 2;
    public static final byte ATTR_INDEX_VIEW_BATTLE_SKILL_DATA_ERROR = 3;
    public static final byte ATTR_INDEX_VIEW_BATTLE_SKILL_PREPARE_NET = 4;
    public static final byte ATTR_INDEX_VIEW_BATTLE_SKILL_GET_TYPE_LIST_COUNT = 5;
    public static final byte ATTR_INDEX_VIEW_BATTLE_SKILL_GET_TYPE_LIST_NAME = 6;
    public static final byte ATTR_INDEX_VIEW_BATTLE_SKILL_GET_SKILL_LIST_COUNT = 7;
    public static final byte ATTR_INDEX_VIEW_BATTLE_SKILL_GET_SKILL_LIST_ID = 8;
    public static final byte ATTR_INDEX_VIEW_BATTLE_SKILL_GET_SKILL_LIST_NAME = 9;
    public static final byte ATTR_INDEX_VIEW_BATTLE_SKILL_GET_SKILL_CONTENT = 10;
    public static final byte ATTR_INDEX_VIEW_BATTLE_SKILL_GET_SKILL_TYPE = 11;

    public static final byte ATTR_INDEX_PRODUCT_ITEM_GET_PAGECOUNT = 0;
    public static final byte ATTR_INDEX_PRODUCT_ITEM_DATA_OK = 1;
    public static final byte ATTR_INDEX_PRODUCT_ITEM_REQUEST = 2;
    public static final byte ATTR_INDEX_PRODUCT_ITEM_DATA_ERROR = 3;
    public static final byte ATTR_INDEX_PRODUCT_ITEM_PREPARE_NET = 4;
    public static final byte ATTR_INDEX_PRODUCT_ITEM_GET_TYPE_LIST_COUNT = 5;
    public static final byte ATTR_INDEX_PRODUCT_ITEM_GET_TYPE_LIST_NAME = 6;
    public static final byte ATTR_INDEX_PRODUCT_ITEM_GET_SKILL_LIST_COUNT = 7;
    public static final byte ATTR_INDEX_PRODUCT_ITEM_GET_SKILL_LIST_ID = 8;
    public static final byte ATTR_INDEX_PRODUCT_ITEM_GET_SKILL_LIST_NAME = 9;
    public static final byte ATTR_INDEX_PRODUCT_ITEM_GET_SKILL_CONTENT = 10;
    public static final byte ATTR_INDEX_PRODUCT_ITEM_GET_TYPE_LIST_ID = 11;

    public static final byte ATTR_INDEX_TONGLIST_GET_PAGECOUNT = 0;
    public static final byte ATTR_INDEX_TONGLIST_DATA_OK = 1;
    public static final byte ATTR_INDEX_TONGLIST_GET_TOTAL_PAGE = 2;
    public static final byte ATTR_INDEX_TONGLIST_GET_PAGE_NO = 3;
    public static final byte ATTR_INDEX_TONGLIST_REQUEST = 4;
    public static final byte ATTR_INDEX_TONGLIST_DATA_ERROR = 5;
    public static final byte ATTR_INDEX_TONGLIST_PREPARE_NET = 6;
    public static final byte ATTR_INDEX_TONGLIST_GET_LIST_COUNT = 7;
    public static final byte ATTR_INDEX_TONGLIST_GET_ID_LIST = 8;
    public static final byte ATTR_INDEX_TONGLIST_GET_NAME_LIST = 9;
    public static final byte ATTR_INDEX_TONGLIST_GET_DUTY_LIST = 10;
    public static final byte ATTR_INDEX_TONGLIST_GET_LEVEL_LIST = 11;
    public static final byte ATTR_INDEX_TONGLIST_GET_TITLE_LIST = 12;
    public static final byte ATTR_INDEX_TONGLIST_GET_ONLINE_LIST = 13;

    public static final byte ATTR_INDEX_CHATOPTION_GET_PAGECOUNT = 0;
    public static final byte ATTR_INDEX_CHATOPTION_DATA_OK = 1;
    public static final byte ATTR_INDEX_CHATOPTION_REQUEST = 2;
    public static final byte ATTR_INDEX_CHATOPTION_DATA_ERROR = 3;
    public static final byte ATTR_INDEX_CHATOPTION_PREPARE_NET = 4;
    public static final byte ATTR_INDEX_CHATOPTION_GET_LIST_COUNT = 5;
    public static final byte ATTR_INDEX_CHATOPTION_GET_ID_LIST = 6;
    public static final byte ATTR_INDEX_CHATOPTION_GET_TYPE_LIST = 7;
    public static final byte ATTR_INDEX_CHATOPTION_GET_COLOR_LIST = 8;
    public static final byte ATTR_INDEX_CHATOPTION_GET_NAME_LIST = 9;

    public static final byte ATTR_INDEX_PET_GET_PAGECOUNT = 0;
    public static final byte ATTR_INDEX_PET_DATA_OK = 1;
    public static final byte ATTR_INDEX_PET_REQUEST = 2;
    public static final byte ATTR_INDEX_PET_DATA_ERROR = 3;
    public static final byte ATTR_INDEX_PET_PREPARE_NET = 4;
    public static final byte ATTR_INDEX_PET_GET_LIST_COUNT = 5;
    public static final byte ATTR_INDEX_PET_GET_ID_LIST = 6;
    public static final byte ATTR_INDEX_PET_GET_TYPE_LIST = 7;
    public static final byte ATTR_INDEX_PET_GET_FEALTY_LIST = 8;
    public static final byte ATTR_INDEX_PET_GET_NAME_LIST = 9;
    public static final byte ATTR_INDEX_PET_GET_VIT = 10;
    public static final byte ATTR_INDEX_PET_GET_STR = 11;
    public static final byte ATTR_INDEX_PET_GET_AGI = 12;
    public static final byte ATTR_INDEX_PET_GET_INT = 13;
    public static final byte ATTR_INDEX_PET_GET_TYPE_NAME = 14;
    public static final byte ATTR_INDEX_PET_GET_FEALTY_NAME = 15;
    public static final byte ATTR_INDEX_PET_GET_IS_CURRENT = 16;
    public static final byte ATTR_INDEX_PET_GET_REST_POINT = 17;
    public static final byte ATTR_INDEX_PET_GET_UNTRADE_POINT = 18;
    public static final byte ATTR_INDEX_PET_GET_LEVEL = 19;
    public static final byte ATTR_INDEX_PET_GET_EXP = 20;
    public static final byte ATTR_INDEX_PET_GET_UPGRADE_EXP = 21;
    public static final byte ATTR_INDEX_PET_GET_HP = 22;
    public static final byte ATTR_INDEX_PET_GET_MP = 23;
    public static final byte ATTR_INDEX_PET_GET_MAXHP = 24;
    public static final byte ATTR_INDEX_PET_GET_MAXMP = 25;
    public static final byte ATTR_INDEX_PET_GET_FEED_ITEM_ID_LIST = 26;
    public static final byte ATTR_INDEX_PET_GET_FEED_COUNT_LIST = 27;
    public static final byte ATTR_INDEX_PET_GET_FEED_NAME_LIST = 28;
    public static final byte ATTR_INDEX_PET_GET_FEED_LIST_COUNT = 29;
    public static final byte ATTR_INDEX_PET_GET_SKILL_LIST_COUNT = 30;
    public static final byte ATTR_INDEX_PET_GET_SKILL_ID_LIST = 31;
    public static final byte ATTR_INDEX_PET_GET_SKILL_NAME_LIST = 32;
    public static final byte ATTR_INDEX_PET_GET_SKILL_CONTENT = 33;

    public static final byte ATTR_INDEX_PET_TRADE_GET_PAGECOUNT = 0;
    public static final byte ATTR_INDEX_PET_TRADE_DATA_OK = 1;
    public static final byte ATTR_INDEX_PET_TRADE_REQUEST = 2;
    public static final byte ATTR_INDEX_PET_TRADE_DATA_ERROR = 3;
    public static final byte ATTR_INDEX_PET_TRADE_PREPARE_NET = 4;
    public static final byte ATTR_INDEX_PET_TRADE_GET_TRADE_ITEM_ID_LIST = 5;
    public static final byte ATTR_INDEX_PET_TRADE_GET_TRADE_ID_LIST = 6;
    public static final byte ATTR_INDEX_PET_TRADE_GET_TRADE_NAME_LIST = 7;
    public static final byte ATTR_INDEX_PET_TRADE_GET_TRADE_LIST_COUNT = 8;

    public static final byte ATTR_INDEX_AUCTION_GET_PAGECOUNT = 0;
    public static final byte ATTR_INDEX_AUCTION_DATA_OK = 1;
    public static final byte ATTR_INDEX_AUCTION_GET_TOTAL_PAGE = 2;
    public static final byte ATTR_INDEX_AUCTION_GET_PAGE_NO = 3;
    public static final byte ATTR_INDEX_AUCTION_REQUEST = 4;
    public static final byte ATTR_INDEX_AUCTION_DATA_ERROR = 5;
    public static final byte ATTR_INDEX_AUCTION_PREPARE_NET = 6;
    public static final byte ATTR_INDEX_AUCTION_GET_TYPE_LIST_COUNT = 7;
    public static final byte ATTR_INDEX_AUCTION_GET_TYPE_ID_LIST = 8;
    public static final byte ATTR_INDEX_AUCTION_GET_TYPE_NAME_LIST = 9;
    public static final byte ATTR_INDEX_AUCTION_GET_AEAR_ID = 10;
    public static final byte ATTR_INDEX_AUCTION_GET_ITEM_LIST_COUNT = 11;
    public static final byte ATTR_INDEX_AUCTION_GET_ITEM_ID_LIST = 12;
    public static final byte ATTR_INDEX_AUCTION_GET_ITEM_TITLE_LIST = 13;
    public static final byte ATTR_INDEX_AUCTION_GET_ITEM_SOURCE_LIST = 14;
    public static final byte ATTR_INDEX_AUCTION_GET_ITEM_TIME_LIST = 15;
    public static final byte ATTR_INDEX_AUCTION_GET_ITEM_START_PRICE_LIST = 16;
    public static final byte ATTR_INDEX_AUCTION_GET_ITEM_CURRENT_PRICE_LIST = 17;
    public static final byte ATTR_INDEX_AUCTION_GET_ITEM_END_PRICE_LIST = 18;
    public static final byte ATTR_INDEX_AUCTION_GET_ITEM_CONTENT = 19;
    public static final byte ATTR_INDEX_AUCTION_GET_AUCTION_ITEM_TYPE_LIST = 20;
    public static final byte ATTR_INDEX_AUCTION_GET_AUCTION_ITEM_ID_LIST = 21;
    public static final byte ATTR_INDEX_AUCTION_GET_AUCTION_ITEM_ID_OR_COUNT_LIST = 22;
    public static final byte ATTR_INDEX_AUCTION_GET_AUCTION_ITEM_NAME_LIST = 23;
    public static final byte ATTR_INDEX_AUCTION_GET_AUCTION_ITEM_LIST_COUNT = 24;

    public static final byte ATTR_INDEX_SYSTEM_OPTION_GET_PAGECOUNT = 0;
    public static final byte ATTR_INDEX_SYSTEM_OPTION_DATA_OK = 1;
    public static final byte ATTR_INDEX_SYSTEM_OPTION_REQUEST = 2;
    public static final byte ATTR_INDEX_SYSTEM_OPTION_DATA_ERROR = 3;
    public static final byte ATTR_INDEX_SYSTEM_OPTION_PREPARE_NET = 4;
    public static final byte ATTR_INDEX_SYSTEM_OPTION_GET_OPTION_LIST_COUNT = 5;
    public static final byte ATTR_INDEX_SYSTEM_OPTION_GET_OPTION_ID_LIST = 6;

    public static final byte ATTR_INDEX_REPAIR_GET_PAGECOUNT = 0;
    public static final byte ATTR_INDEX_REPAIR_DATA_OK = 1;
    public static final byte ATTR_INDEX_REPAIR_REQUEST = 2;
    public static final byte ATTR_INDEX_REPAIR_DATA_ERROR = 3;
    public static final byte ATTR_INDEX_REPAIR_PREPARE_NET = 4;
    public static final byte ATTR_INDEX_REPAIR_GET_REPAIR_ITEM_ID_LIST = 5;
    public static final byte ATTR_INDEX_REPAIR_GET_REPAIR_ID_LIST = 6;
    public static final byte ATTR_INDEX_REPAIR_GET_REPAIR_NAME_LIST = 7;
    public static final byte ATTR_INDEX_REPAIR_GET_REPAIR_LIST_COUNT = 8;
    public static final byte ATTR_INDEX_REPAIR_GET_REPAIR_FEE = 9;
    public static final byte ATTR_INDEX_REPAIR_GET_REPAIR_EQUIP_LIST = 10;
    public static final byte ATTR_INDEX_REPAIR_GET_REPAIR_DATA_COUNT = 11;
    public static final byte ATTR_INDEX_REPAIR_GET_REPAIR_DATA_LIST = 12;

    public static final byte ATTR_INDEX_GENERIC_LIST_GET_PAGECOUNT = 0;
    public static final byte ATTR_INDEX_GENERIC_LIST_DATA_OK = 1;
    public static final byte ATTR_INDEX_GENERIC_LIST_REQUEST = 2;
    public static final byte ATTR_INDEX_GENERIC_LIST_DATA_ERROR = 3;
    public static final byte ATTR_INDEX_GENERIC_LIST_PREPARE_NET = 4;

    public static final byte ATTR_INDEX_ISHOP_GET_PAGECOUNT = 0;
    public static final byte ATTR_INDEX_ISHOP_DATA_OK = 1;
    public static final byte ATTR_INDEX_ISHOP_REQUEST = 2;
    public static final byte ATTR_INDEX_ISHOP_DATA_ERROR = 3;
    public static final byte ATTR_INDEX_ISHOP_PREPARE_NET = 4;

    public static final byte TOUCH_NPC_PRODUCT_SKILL = 1;
    public static final byte TOUCH_NPC_BATTLE_SKILL = 2;

    public static final byte NET_CHAT_PLAYER_VIEW = 1;
    public static final byte NET_CHAT_FRIEND_VIEW = 2;

    public static final byte REPAIR_ALL = 0;
    public static final byte REPAIR_EQUIP = 1;
    public static final byte REPAIR_BAG = 2;

    //UI Data
    public static int uiRequestTaskId = 0;
    public static int uiDataId = 0;
    public static String uiDataTitle = null;
    public static boolean uiDataRequest = false;
    public static boolean uiDataDataOk = false;
    public static boolean uiDataDataError = false;
    public static boolean[][] uiDataBoolean = new boolean[10][];
    public static byte[][] uiDataByte = new byte[10][];
    public static short[][] uiDataShort = new short[10][];
    public static int[][] uiDataInt = new int[10][];
    public static String[][] uiDataString = new String[10][];
    public static GameItem[][] uiDataGameItem = new GameItem[10][];
    public static UWAPSegment uiDataSegment = null;
    public static DataInputStream uiDataStream = null;
    public static Hashtable uiDataNetRequest = new Hashtable();

    public static final byte UI_DATA_REQUEST_TYPE = 0;
    public static final byte UI_DATA_TYPE_BOOLEAN = 1;
    public static final byte UI_DATA_TYPE_BYTE = 2;
    public static final byte UI_DATA_TYPE_SHORT = 3;
    public static final byte UI_DATA_TYPE_INT = 4;
    public static final byte UI_DATA_TYPE_STRING = 5;
    public static final byte UI_DATA_TYPE_GAMEITEM = 6;
    public static final byte UI_DATA_TYPE_PAGE_SIZE = 7;
    public static final byte UI_DATA_TYPE_ID = 8;
    public static final byte UI_DATA_TYPE_TITLE = 9;
    public static final byte UI_DATA_REQUEST = 10;
    public static final byte UI_DATA_DATA_OK = 11;
    public static final byte UI_DATA_DATA_ERROR = 12;
    public static final byte UI_DATA_TYPE_BOOLEANS = 13;
    public static final byte UI_DATA_TYPE_BYTES = 14;
    public static final byte UI_DATA_TYPE_SHORTS = 15;
    public static final byte UI_DATA_TYPE_INTS = 16;
    public static final byte UI_DATA_TYPE_STRINGS = 17;

    public static final byte UI_DATA_CONTROL_COUNT = 0;
    public static final byte UI_DATA_CONTROL_INDEX = 1;
    public static final byte UI_DATA_CONTROL_PRECISION = 2;
    public static final byte UI_DATA_CONTROL_TRUE = 3;
    public static final byte UI_DATA_CONTROL_FALSE = 4;

    public static final byte UI_DATA_BBS_INDEX_DATA = 0;
    public static final byte UI_DATA_BBS_INDEX_ID_LIST = 1;
    public static final byte UI_DATA_BBS_INDEX_LIST = 0;
    public static final byte UI_DATA_BBS_INDEX_CONTENT = 1;
    public static final byte UI_DATA_BBS_COUNT_DATA = 2;
    public static final byte UI_DATA_BBS_SUB_INDEX_PAGE_NO = 0;
    public static final byte UI_DATA_BBS_SUB_INDEX_TOTAL_PAGE = 1;

    public static final int[][] uiDefine = new int[][]{
                    //ATTR_TYPE_UIMODE no ues = 0
                    {
                        0
                    },

                    //ATTR_TYPE_TASKUI no use = 1
                    {
                        0
                    },

                    //ATTR_TYPE_BBS = 2
                    {
                                    //ATTR_INDEX_BBS_GET_PAGECOUNT = 0;
                                    UI_DATA_TYPE_PAGE_SIZE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_BBS_GET_LIST_COUNT = 1;
                                    UI_DATA_TYPE_INT << 24 | UI_DATA_BBS_INDEX_ID_LIST << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_BBS_GET_LIST = 2;
                                    UI_DATA_TYPE_STRING << 24 | UI_DATA_BBS_INDEX_LIST << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_BBS_GET_CONTENT = 3;
                                    UI_DATA_TYPE_STRING << 24 | UI_DATA_BBS_INDEX_CONTENT << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_BBS_PREPARE_NET = 4;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_FALSE,
                                    //ATTR_INDEX_BBS_DATA_OK = 5;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_BBS_GET_TOTAL_PAGE = 6;
                                    UI_DATA_TYPE_INT << 24 | UI_DATA_BBS_INDEX_DATA << 16 | UI_DATA_BBS_SUB_INDEX_TOTAL_PAGE << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_BBS_GET_PAGE_NO = 7;
                                    UI_DATA_TYPE_INT << 24 | UI_DATA_BBS_INDEX_DATA << 16 | UI_DATA_BBS_SUB_INDEX_PAGE_NO << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_BBS_GET_BBSID = 8;
                                    UI_DATA_TYPE_ID << 24 | 0x00 << 16 | 0x02 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_BBS_GET_LIST_ID = 9;
                                    UI_DATA_TYPE_INT << 24 | UI_DATA_BBS_INDEX_ID_LIST << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_BBS_REQUEST = 10;
                                    UI_DATA_REQUEST << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_BBS_DATA_ERROR = 11;
                                    UI_DATA_DATA_ERROR << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION
                    },

                    //ATTR_TYPE_BATTLE_SKILL = 3
                    {
                                    //ATTR_INDEX_BATTLE_SKILL_GET_PAGECOUNT = 0;
                                    UI_DATA_TYPE_PAGE_SIZE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_BATTLE_SKILL_GET_NPCID = 1;
                                    UI_DATA_TYPE_ID << 24 | 0x00 << 16 | 0x02 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_BATTLE_SKILL_GET_LIST_COUNT = 2;
                                    UI_DATA_TYPE_SHORT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_BATTLE_SKILL_GET_LIST_ID = 3;
                                    UI_DATA_TYPE_SHORT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_BATTLE_SKILL_GET_LIST_NAME = 4;
                                    UI_DATA_TYPE_STRING << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_BATTLE_SKILL_DATA_OK = 5;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_BATTLE_SKILL_REQUEST = 6;
                                    UI_DATA_REQUEST << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_BATTLE_SKILL_DATA_ERROR = 7;
                                    UI_DATA_DATA_ERROR << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_BATTLE_SKILL_PREPARE_NET = 8;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_FALSE,
                                    //ATTR_INDEX_BATTLE_SKILL_GET_TITLE = 9;
                                    UI_DATA_TYPE_TITLE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_BATTLE_SKILL_GET_CONTENT = 10;
                                    UI_DATA_TYPE_STRING << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_BATTLE_SKILL_GET_LIST_CAN_LEARN = 11;
                                    UI_DATA_TYPE_BOOLEAN << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_BATTLE_SKILL_GET_LIST_PRICE = 12;
                                    UI_DATA_TYPE_INT << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_BATTLE_SKILL_GET_LIST_LEVEL = 13;
                                    UI_DATA_TYPE_BYTE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_BATTLE_SKILL_GET_TYPE = 14;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION
                    },

                    //ATTR_TYPE_PRODUCT_SKILL = 4
                    {
                                    //ATTR_INDEX_PRODUCT_SKILL_GET_PAGECOUNT = 0;
                                    UI_DATA_TYPE_PAGE_SIZE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_PRODUCT_SKILL_GET_NPCID = 1;
                                    UI_DATA_TYPE_ID << 24 | 0x00 << 16 | 0x02 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_PRODUCT_SKILL_GET_LIST_COUNT = 2;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_PRODUCT_SKILL_GET_LIST_ID = 3;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PRODUCT_SKILL_GET_LIST_NAME = 4;
                                    UI_DATA_TYPE_STRING << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PRODUCT_SKILL_DATA_OK = 5;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_PRODUCT_SKILL_REQUEST = 6;
                                    UI_DATA_REQUEST << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_PRODUCT_SKILL_DATA_ERROR = 7;
                                    UI_DATA_DATA_ERROR << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_PRODUCT_SKILL_PREPARE_NET = 8;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_FALSE,
                                    //ATTR_INDEX_PRODUCT_SKILL_GET_TITLE = 9;
                                    UI_DATA_TYPE_TITLE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_PRODUCT_SKILL_GET_CONTENT = 10;
                                    UI_DATA_TYPE_STRING << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION
                    },
                    //ATTR_TYPE_CHAT_CIRCLE = 5;
                    {
                                    //ATTR_INDEX_CHAT_CIRCLE_GET_PAGECOUNT = 0;
                                    UI_DATA_TYPE_PAGE_SIZE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_CHAT_CIRCLE_GET_LIST_COUNT = 1;
                                    UI_DATA_TYPE_STRING << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_CHAT_CIRCLE_GET_LIST_NAME = 2;
                                    UI_DATA_TYPE_STRING << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_CHAT_CIRCLE_DATA_OK = 3;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_CHAT_CIRCLE_REQUEST = 4;
                                    UI_DATA_REQUEST << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_CHAT_CIRCLE_DATA_ERROR = 5;
                                    UI_DATA_DATA_ERROR << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_CHAT_CIRCLE_PREPARE_NET = 6;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_FALSE,
                                    //ATTR_INDEX_CHAT_CIRCLE_GET_CONTENT = 7;
                                    UI_DATA_TYPE_STRING << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION
                    },
                    //ATTR_TYPE_TASK_VIEW = 6;
                    {
                                    //ATTR_INDEX_TASK_VIEW_GET_PAGECOUNT = 0;
                                    UI_DATA_TYPE_PAGE_SIZE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_TASK_VIEW_GET_LIST_COUNT = 1;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_TASK_VIEW_GET_LIST_ID = 2;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_TASK_VIEW_GET_LIST_NAME = 3;
                                    UI_DATA_TYPE_STRING << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_TASK_VIEW_DATA_OK = 4;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_TASK_VIEW_REQUEST = 5;
                                    UI_DATA_REQUEST << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_TASK_VIEW_DATA_ERROR = 6;
                                    UI_DATA_DATA_ERROR << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_TASK_VIEW_PREPARE_NET = 7;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_FALSE,
                                    //ATTR_INDEX_TASK_VIEW_GET_CONTENT = 8;
                                    UI_DATA_TYPE_STRING << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_TASK_VIEW_GET_ABANDON_RESULT = 9;
                                    UI_DATA_TYPE_BOOLEAN << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION
                    },
                    //ATTR_TYPE_PLAYER_VIEW = 7;
                    {
                                    //ATTR_INDEX_PLAYER_VIEW_GET_PAGECOUNT = 0;
                                    UI_DATA_TYPE_PAGE_SIZE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_PLAYER_VIEW_GET_LIST_COUNT = 1;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_PLAYER_VIEW_GET_LIST_ID = 2;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PLAYER_VIEW_GET_LIST_NAME = 3;
                                    UI_DATA_TYPE_STRING << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PLAYER_VIEW_DATA_OK = 4;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_PLAYER_VIEW_REQUEST = 5;
                                    UI_DATA_REQUEST << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_PLAYER_VIEW_DATA_ERROR = 6;
                                    UI_DATA_DATA_ERROR << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_PLAYER_VIEW_PREPARE_NET = 7;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_FALSE,
                                    //ATTR_INDEX_PLAYER_VIEW_GET_CONTENT = 8;
                                    UI_DATA_TYPE_STRING << 24 | 0x02 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_PLAYER_VIEW_GET_NAME = 9;
                                    UI_DATA_TYPE_STRING << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PLAYER_VIEW_GET_EQUIP_COUNT = 10;
                                    UI_DATA_TYPE_STRING << 24 | 0x03 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_PLAYER_VIEW_GET_EQUIP_LIST = 11;
                                    UI_DATA_TYPE_STRING << 24 | 0x03 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PLAYER_VIEW_GET_EQUIP_BYTES_COUNT = 12;
                                    UI_DATA_TYPE_BYTE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_PLAYER_VIEW_GET_FRIEND_ONLINE_RESULT = 13;
                                    UI_DATA_TYPE_BOOLEAN << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION
                    },
                    //ATTR_TYPE_FRIEND_VIEW = 8;
                    {
                                    //ATTR_INDEX_FRIEND_VIEW_GET_PAGECOUNT = 0;
                                    UI_DATA_TYPE_PAGE_SIZE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_FRIEND_VIEW_GET_LIST_COUNT = 1;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_FRIEND_VIEW_GET_LIST_ID = 2;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_FRIEND_VIEW_GET_LIST_NAME = 3;
                                    UI_DATA_TYPE_STRING << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_FRIEND_VIEW_DATA_OK = 4;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_FRIEND_VIEW_REQUEST = 5;
                                    UI_DATA_REQUEST << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_FRIEND_VIEW_DATA_ERROR = 6;
                                    UI_DATA_DATA_ERROR << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_FRIEND_VIEW_PREPARE_NET = 7;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_FALSE,
                                    //ATTR_INDEX_FRIEND_VIEW_GET_CONTENT = 8;
                                    UI_DATA_TYPE_STRING << 24 | 0x02 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_FRIEND_VIEW_GET_NAME = 9;
                                    UI_DATA_TYPE_STRING << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_FRIEND_VIEW_GET_ONLINE = 10;
                                    UI_DATA_TYPE_BOOLEAN << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_FRINED_VIEW_GET_ADDING_PLAYER_ID = 11;
                                    UI_DATA_TYPE_INT << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_FRIEND_VIEW_GET_ADDING_PLAYER_ONLINE = 12;
                                    UI_DATA_TYPE_BOOLEAN << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                    },
                    //ATTR_TYPE_MAIL = 9;
                    {
                                    //ATTR_INDEX_MAIL_GET_PAGECOUNT = 0;
                                    UI_DATA_TYPE_PAGE_SIZE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_MAIL_GET_LIST_COUNT = 1;
                                    UI_DATA_TYPE_INT << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_MAIL_GET_LIST = 2;
                                    UI_DATA_TYPE_STRING << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_MAIL_GET_CONTENT = 3;
                                    UI_DATA_TYPE_STRING << 24 | 0x02 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_MAIL_PREPARE_NET = 4;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_FALSE,
                                    //ATTR_INDEX_MAIL_DATA_OK = 5;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_MAIL_GET_TOTAL_PAGE = 6;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x01 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_MAIL_GET_PAGE_NO = 7;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_MAIL_GET_LIST_ID = 8;
                                    UI_DATA_TYPE_INT << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_MAIL_REQUEST = 9;
                                    UI_DATA_REQUEST << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_MAIL_DATA_ERROR = 10;
                                    UI_DATA_DATA_ERROR << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_MAIL_GET_ATTACH_FLAG = 11;
                                    UI_DATA_TYPE_BOOLEAN << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_MAIL_GET_READ_FLAG = 12;
                                    UI_DATA_TYPE_BOOLEAN << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_MAIL_GET_PRICE = 13;
                                    UI_DATA_TYPE_INT << 24 | 0x02 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_MAIL_GET_AUTHOR = 14;
                                    UI_DATA_TYPE_STRING << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_MAIL_GET_SEND_ITEM_TYPE_LIST = 15;
                                    UI_DATA_TYPE_INT << 24 | 0x03 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_MAIL_GET_SEND_ITEM_ID_LIST = 16;
                                    UI_DATA_TYPE_INT << 24 | 0x04 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_MAIL_GET_SEND_ITEM_ID_OR_COUNT_LIST = 17;
                                    UI_DATA_TYPE_INT << 24 | 0x05 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_MAIL_GET_SEND_ITEM_NAME_LIST = 18;
                                    UI_DATA_TYPE_STRING << 24 | 0x03 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_MAIL_GET_SEND_ITEM_LIST_COUNT = 19;
                                    UI_DATA_TYPE_INT << 24 | 0x03 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_MAIL_GET_ATTACHMENT_BYTES_COUNT = 20
                                    UI_DATA_TYPE_BYTE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_MAIL_GET_BUILD_ITEMS_COUNT = 21;
                                    UI_DATA_TYPE_GAMEITEM << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT
                    },
                    //ATTR_TYPE_SHOP = 10;
                    {
                                    //ATTR_INDEX_SHOP_GET_PAGECOUNT = 0;
                                    UI_DATA_TYPE_PAGE_SIZE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_SHOP_DATA_OK = 1;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_SHOP_GET_TOTAL_PAGE = 2;
                                    UI_DATA_TYPE_INT << 24 | 0x09 << 16 | 0x01 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_SHOP_GET_PAGE_NO = 3;
                                    UI_DATA_TYPE_INT << 24 | 0x09 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_SHOP_REQUEST = 4;
                                    UI_DATA_REQUEST << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_SHOP_DATA_ERROR = 5;
                                    UI_DATA_DATA_ERROR << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_SHOP_PREPARE_NET = 6;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_FALSE,
                                    //ATTR_INDEX_SHOP_GET_SHOP_LIST_COUNT = 7;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x0 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_SHOP_GET_SHOP_ID_LIST = 8;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x0 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_SHOP_GET_SHOP_NAME_LIST = 9;
                                    UI_DATA_TYPE_STRING << 24 | 0x00 << 16 | 0x0 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_SHOP_GET_SHOP_CONTENT = 10;
                                    UI_DATA_TYPE_STRING << 24 | 0x01 << 16 | 0x0 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_SHOP_GET_ITEM_LIST_COUNT = 11;
                                    UI_DATA_TYPE_INT << 24 | 0x01 << 16 | 0x0 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_SHOP_GET_ITEM_ID_LIST = 12;
                                    UI_DATA_TYPE_INT << 24 | 0x01 << 16 | 0x0 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_SHOP_GET_ITEM_NAME_LIST = 13;
                                    UI_DATA_TYPE_STRING << 24 | 0x02 << 16 | 0x0 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_SHOP_GET_ITEM_CONTENT = 14;
                                    UI_DATA_TYPE_STRING << 24 | 0x03 << 16 | 0x0 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INEX_SHOP_GET_TOUCH_NPC_ID = 15;
                                    UI_DATA_TYPE_ID << 24 | 0x00 << 16 | 0x0 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_SHOP_GET_ITEM_BUY_GET_LIST_COUNT = 16;
                                    UI_DATA_TYPE_INT << 24 | 0x02 << 16 | 0x0 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_SHOP_GET_ITEM_BUY_GET_LIST = 17;
                                    UI_DATA_TYPE_STRING << 24 | 0x04 << 16 | 0x0 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_SHOP_GET_ITEM_BUY_GET_ID_LIST = 18;
                                    UI_DATA_TYPE_INT << 24 | 0x02 << 16 | 0x0 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_SHOP_GET_STORE_ITEM_TYPE_LIST = 19;
                                    UI_DATA_TYPE_INT << 24 | 0x03 << 16 | 0x0 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_SHOP_GET_STORE_ITEM_ID_LIST = 20;
                                    UI_DATA_TYPE_INT << 24 | 0x04 << 16 | 0x0 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_SHOP_GET_STORE_ITEM_ID_OR_COUNT_LIST = 21;
                                    UI_DATA_TYPE_INT << 24 | 0x05 << 16 | 0x0 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_SHOP_GET_STORE_ITEM_NAME_LIST = 22;
                                    UI_DATA_TYPE_STRING << 24 | 0x05 << 16 | 0x0 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_SHOP_GET_STORE_ITEM_LIST_COUNT = 23;
                                    UI_DATA_TYPE_INT << 24 | 0x03 << 16 | 0x0 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_SHOP_GET_STORE_ITEM_COUNT = 24;
                                    UI_DATA_TYPE_SHORT << 24 | 0x00 << 16 | 0x0 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_SHOP_GET_STORE_ITEM_TYPE = 25;
                                    UI_DATA_TYPE_SHORT << 24 | 0x01 << 16 | 0x0 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_SHOP_GET_STORE_ITEM_ID = 26;
                                    UI_DATA_TYPE_INT << 24 | 0x06 << 16 | 0x0 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_SHOP_GET_STORE_ITEM_Q = 27;
                                    UI_DATA_TYPE_SHORT << 24 | 0x02 << 16 | 0x0 << 8 | UI_DATA_CONTROL_INDEX
                    },
                    //ATTR_TYPE_AUCTION = 11;
                    {
                                    //ATTR_INDEX_AUCTION_GET_PAGECOUNT = 0;
                                    UI_DATA_TYPE_PAGE_SIZE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_AUCTION_DATA_OK = 1;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_AUCTION_GET_TOTAL_PAGE = 2;
                                    UI_DATA_TYPE_INT << 24 | 0x09 << 16 | 0x01 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_AUCTION_GET_PAGE_NO = 3;
                                    UI_DATA_TYPE_INT << 24 | 0x09 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_AUCTION_REQUEST = 4;
                                    UI_DATA_REQUEST << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_AUCTION_DATA_ERROR = 5;
                                    UI_DATA_DATA_ERROR << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_AUCTION_PREPARE_NET = 6;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_FALSE,
                                    //ATTR_INDEX_AUCTION_GET_TYPE_LIST_COUNT = 7;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_AUCTION_GET_TYPE_ID_LIST = 8;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_AUCTION_GET_TYPE_NAME_LIST = 9;
                                    UI_DATA_TYPE_STRING << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_AUCTION_GET_AEAR_ID = 10;
                                    UI_DATA_TYPE_INT << 24 | 0x09 << 16 | 0x02 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_AUCTION_GET_ITEM_LIST_COUNT = 11;
                                    UI_DATA_TYPE_INT << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_AUCTION_GET_ITEM_ID_LIST = 12;
                                    UI_DATA_TYPE_INT << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_AUCTION_GET_ITEM_TITLE_LIST = 13;
                                    UI_DATA_TYPE_STRING << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_AUCTION_GET_ITEM_SOURCE_LIST = 14;
                                    UI_DATA_TYPE_STRING << 24 | 0x02 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_AUCTION_GET_ITEM_TIME_LIST = 15;
                                    UI_DATA_TYPE_STRING << 24 | 0x03 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_AUCTION_GET_ITEM_START_PRICE_LIST = 16;
                                    UI_DATA_TYPE_INT << 24 | 0x02 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_AUCTION_GET_ITEM_CURRENT_PRICE_LIST = 17;
                                    UI_DATA_TYPE_INT << 24 | 0x03 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_AUCTION_GET_ITEM_END_PRICE_LIST = 18;
                                    UI_DATA_TYPE_INT << 24 | 0x04 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_AUCTION_GET_ITEM_CONTENT = 19;
                                    UI_DATA_TYPE_STRING << 24 | 0x04 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_AUCTION_GET_AUCTION_ITEM_TYPE_LIST = 20;
                                    UI_DATA_TYPE_INT << 24 | 0x05 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_AUCTION_GET_AUCTION_ITEM_ID_LIST = 21;
                                    UI_DATA_TYPE_INT << 24 | 0x06 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_AUCTION_GET_AUCTION_ITEM_ID_OR_COUNT_LIST = 22;
                                    UI_DATA_TYPE_INT << 24 | 0x07 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_AUCTION_GET_AUCTION_ITEM_NAME_LIST = 23;
                                    UI_DATA_TYPE_STRING << 24 | 0x05 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_AUCTION_GET_AUCTION_ITEM_LIST_COUNT = 24;
                                    UI_DATA_TYPE_INT << 24 | 0x05 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT
                    },
                    //ATTR_TYPE_BUY_MATERIAL = 12;
                    {
                                    //ATTR_INDEX_BUY_MATERIAL_GET_PAGECOUNT = 0;
                                    UI_DATA_TYPE_PAGE_SIZE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_BUY_MATERIAL_DATA_OK = 1;
                                    UI_DATA_DATA_OK << 24 | 0x09 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_BUY_MATERIAL_GET_TOTAL_PAGE = 2;
                                    UI_DATA_TYPE_INT << 24 | 0x09 << 16 | 0x01 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_BUY_MATERIAL_GET_PAGE_NO = 3;
                                    UI_DATA_TYPE_INT << 24 | 0x09 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_BUY_MATERIAL_REQUEST = 4;
                                    UI_DATA_REQUEST << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_BUY_MATERIAL_DATA_ERROR = 5;
                                    UI_DATA_DATA_ERROR << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_BUY_MATERIAL_PREPARE_NET = 6;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_FALSE,
                                    //ATTR_INDEX_BUY_MATERIAL_GET_AREA_ID = 7;
                                    UI_DATA_TYPE_INT << 24 | 0x09 << 16 | 0x02 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_BUY_MATERIAL_GET_TYPE_LIST_COUNT = 8;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_BUY_MATERIAL_GET_TYPE_ID_LIST = 9;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_BUY_MATERIAL_GET_TYPE_NAME_LIST = 10;
                                    UI_DATA_TYPE_STRING << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_BUY_MATERIAL_GET_MATERIAL_LIST_COUNT = 11;
                                    UI_DATA_TYPE_INT << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_BUY_MATERIAL_GET_MATERIAL_ID_LIST = 12;
                                    UI_DATA_TYPE_INT << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_BUY_MATERIAL_GET_MATERIAL_NAME_LIST = 13;
                                    UI_DATA_TYPE_STRING << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_BUY_MATERIAL_GET_MATERIAL_CONTENT = 14;
                                    UI_DATA_TYPE_STRING << 24 | 0x02 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_BUY_MATERIAL_GET_MATERIAL_ITEM_ID_LIST = 15;
                                    UI_DATA_TYPE_INT << 24 | 0x02 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_BUY_MATERIAL_GET_TOUCH_NPC_ID = 16;
                                    UI_DATA_TYPE_ID << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION
                    },
                    //ATTR_TYPE_OEM = 13;
                    {
                                    //ATTR_INDEX_OEM_GET_PAGECOUNT = 0;
                                    UI_DATA_TYPE_PAGE_SIZE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_OEM_DATA_OK = 1;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_OEM_GET_TOTAL_PAGE = 2;
                                    UI_DATA_TYPE_INT << 24 | 0x09 << 16 | 0x01 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_OEM_GET_PAGE_NO = 3;
                                    UI_DATA_TYPE_INT << 24 | 0x09 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_OEM_REQUEST = 4;
                                    UI_DATA_REQUEST << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_OEM_DATA_ERROR = 5;
                                    UI_DATA_DATA_ERROR << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_OEM_PREPARE_NET = 6;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_FALSE,
                                    //ATTR_INDEX_OEM_GET_AREA_ID = 7;
                                    UI_DATA_TYPE_INT << 24 | 0x09 << 16 | 0x02 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_OEM_GET_TYPE_LIST_COUNT = 8;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_OEM_GET_TYPE_ID_LIST = 9;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_OEM_GET_TYPE_NAME_LIST = 10;
                                    UI_DATA_TYPE_STRING << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_OEM_GET_OEM_LIST_COUNT = 11;
                                    UI_DATA_TYPE_INT << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_OEM_GET_OEM_ID_LIST = 12;
                                    UI_DATA_TYPE_INT << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_OEM_GET_OEM_NAME_LIST = 13;
                                    UI_DATA_TYPE_STRING << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_OEM_GET_OEM_CONTENT = 14;
                                    UI_DATA_TYPE_STRING << 24 | 0x02 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_OEM_GET_TOUCH_NPC_ID = 15;
                                    UI_DATA_TYPE_ID << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_OEM_GET_CAN_MAKE = 16;
                                    UI_DATA_TYPE_BOOLEAN << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_OEM_GET_NEED_GAME = 17;
                                    UI_DATA_TYPE_BOOLEAN << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX
                    },
                    //ATTR_TYPE_STORE = 14;
                    {
                                    //ATTR_INDEX_STORE_GET_PAGECOUNT = 0;
                                    UI_DATA_TYPE_PAGE_SIZE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_STORE_DATA_OK = 1;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_STORE_GET_TOTAL_PAGE = 2;
                                    UI_DATA_TYPE_INT << 24 | 0x09 << 16 | 0x01 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_STORE_GET_PAGE_NO = 3;
                                    UI_DATA_TYPE_INT << 24 | 0x09 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_STORE_REQUEST = 4;
                                    UI_DATA_REQUEST << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_STORE_DATA_ERROR = 5;
                                    UI_DATA_DATA_ERROR << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_STORE_PREPARE_NET = 6;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_FALSE,
                                    //ATTR_INDEX_STORE_GET_TOUCH_NPC_ID = 7;
                                    UI_DATA_TYPE_ID << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_STORE_GET_LIST_COUNT = 8;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_STORE_GET_ID_LIST = 9;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_STORE_GET_NAME_LIST = 10;
                                    UI_DATA_TYPE_STRING << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_STORE_GET_CONTENT = 11;
                                    UI_DATA_TYPE_STRING << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_STORE_GET_SELL_ITME_TYPE_LIST = 12;
                                    UI_DATA_TYPE_INT << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_STORE_GET_SELL_ITEM_ID_LIST = 13;
                                    UI_DATA_TYPE_INT << 24 | 0x02 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_STORE_GET_ID_OR_COUNT_LIST = 14;
                                    UI_DATA_TYPE_INT << 24 | 0x03 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_STORE_GET_SELL_NAME_LIST = 15;
                                    UI_DATA_TYPE_STRING << 24 | 0x02 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_STORE_GET_SELL_LIST_COUNT = 16;
                                    UI_DATA_TYPE_INT << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_STORE_GET_TRADE_RESULT_BYTES_COUNT = 17;
                                    UI_DATA_TYPE_BYTE << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_STORE_GET_PRICE_LIST = 18;
                                    UI_DATA_TYPE_INT << 24 | 0x04 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                    },
                    //ATTR_TYPE_VIEW_BATTLE_SKILL = 15;
                    {
                                    //ATTR_INDEX_VIEW_BATTLE_SKILL_GET_PAGECOUNT = 0;
                                    UI_DATA_TYPE_PAGE_SIZE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_VIEW_BATTLE_SKILL_DATA_OK = 1;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_VIEW_BATTLE_SKILL_REQUEST = 2;
                                    UI_DATA_REQUEST << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_VIEW_BATTLE_SKILL_DATA_ERROR = 3;
                                    UI_DATA_DATA_ERROR << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_VIEW_BATTLE_SKILL_PREPARE_NET = 4;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_FALSE,
                                    //ATTR_INDEX_VIEW_BATTLE_SKILL_GET_TYPE_LIST_COUNT = 5;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_VIEW_BATTLE_SKILL_GET_TYPE_LIST_NAME = 6;
                                    UI_DATA_TYPE_STRING << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_VIEW_BATTLE_SKILL_GET_SKILL_LIST_COUNT = 7;
                                    UI_DATA_TYPE_INT << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_VIEW_BATTLE_SKILL_GET_SKILL_LIST_ID = 8;
                                    UI_DATA_TYPE_INT << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_VIEW_BATTLE_SKILL_GET_SKILL_LIST_NAME = 9;
                                    UI_DATA_TYPE_STRING << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_VIEW_BATTLE_SKILL_GET_SKILL_CONTENT = 10;
                                    UI_DATA_TYPE_STRING << 24 | 0x02 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_VIEW_BATTLE_SKILL_GET_SKILL_TYPE = 11;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX
                    },
                    //ATTR_TYPE_PRODUCT_ITEM = 16;
                    {
                                    //ATTR_INDEX_PRODUCT_ITEM_GET_PAGECOUNT = 0;
                                    UI_DATA_TYPE_PAGE_SIZE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_PRODUCT_ITEM_DATA_OK = 1;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_PRODUCT_ITEM_REQUEST = 2;
                                    UI_DATA_REQUEST << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_PRODUCT_ITEM_DATA_ERROR = 3;
                                    UI_DATA_DATA_ERROR << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_PRODUCT_ITEM_PREPARE_NET = 4;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_FALSE,
                                    //ATTR_INDEX_PRODUCT_ITEM_GET_TYPE_LIST_COUNT = 5;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_PRODUCT_ITEM_GET_TYPE_LIST_NAME = 6;
                                    UI_DATA_TYPE_STRING << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PRODUCT_ITEM_GET_SKILL_LIST_COUNT = 7;
                                    UI_DATA_TYPE_INT << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_PRODUCT_ITEM_GET_SKILL_LIST_ID = 8;
                                    UI_DATA_TYPE_INT << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PRODUCT_ITEM_GET_SKILL_LIST_NAME = 9;
                                    UI_DATA_TYPE_STRING << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PRODUCT_ITEM_GET_SKILL_CONTENT = 10;
                                    UI_DATA_TYPE_STRING << 24 | 0x02 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_PRODUCT_ITEM_GET_TYPE_LIST_ID = 11;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX
                    },
                    //ATTR_TYPE_TONGLIST = 17;
                    {
                                    //ATTR_INDEX_TONGLIST_GET_PAGECOUNT = 0;
                                    UI_DATA_TYPE_PAGE_SIZE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_TONGLIST_DATA_OK = 1;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_TONGLIST_GET_TOTAL_PAGE = 2;
                                    UI_DATA_TYPE_INT << 24 | 0x09 << 16 | 0x01 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_TONGLIST_GET_PAGE_NO = 3;
                                    UI_DATA_TYPE_INT << 24 | 0x09 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_TONGLIST_REQUEST = 4;
                                    UI_DATA_REQUEST << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_TONGLIST_DATA_ERROR = 5;
                                    UI_DATA_DATA_ERROR << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_TONGLIST_PREPARE_NET = 6;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_FALSE,
                                    //ATTR_INDEX_TONGLIST_GET_LIST_COUNT = 7;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_TONGLIST_GET_ID_LIST = 8;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_TONGLIST_GET_NAME_LIST = 9;
                                    UI_DATA_TYPE_STRING << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_TONGLIST_GET_DUTY_LIST = 10;
                                    UI_DATA_TYPE_INT << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_TONGLIST_GET_LEVEL_LIST = 11;
                                    UI_DATA_TYPE_INT << 24 | 0x02 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_TONGLIST_GET_TITLE_LIST = 12;
                                    UI_DATA_TYPE_STRING << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_TONGLIST_GET_ONLINE_LIST = 13;
                                    UI_DATA_TYPE_BOOLEAN << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX
                    },
                    //ATTR_TYPE_CHATOPTION = 18;
                    {
                                    //ATTR_INDEX_CHATOPTION_GET_PAGECOUNT = 0;
                                    UI_DATA_TYPE_PAGE_SIZE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_CHATOPTION_DATA_OK = 1;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_CHATOPTION_REQUEST = 2;
                                    UI_DATA_REQUEST << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_CHATOPTION_DATA_ERROR = 3;
                                    UI_DATA_DATA_ERROR << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_CHATOPTION_PREPARE_NET = 4;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_FALSE,
                                    //ATTR_INDEX_CHATOPTION_GET_LIST_COUNT = 5;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_CHATOPTION_GET_ID_LIST = 6;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_CHATOPTION_GET_TYPE_LIST = 7;
                                    UI_DATA_TYPE_INT << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_CHATOPTION_GET_COLOR_LIST = 8;
                                    UI_DATA_TYPE_INT << 24 | 0x02 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_CHATOPTION_GET_NAME_LIST = 9;
                                    UI_DATA_TYPE_STRING << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX
                    },
                    //ATTR_TYPE_PET = 19;
                    {
                                    //ATTR_INDEX_PET_GET_PAGECOUNT = 0;
                                    UI_DATA_TYPE_PAGE_SIZE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_PET_DATA_OK = 1;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_PET_REQUEST = 2;
                                    UI_DATA_REQUEST << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_PET_DATA_ERROR = 3;
                                    UI_DATA_DATA_ERROR << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_PET_PREPARE_NET = 4;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_FALSE,
                                    //ATTR_INDEX_PET_GET_LIST_COUNT = 5;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_PET_GET_ID_LIST = 6;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PET_GET_TYPE_LIST = 7;
                                    UI_DATA_TYPE_BYTE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PET_GET_FEALTY_LIST = 8;
                                    UI_DATA_TYPE_BYTE << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PET_GET_NAME_LIST = 9;
                                    UI_DATA_TYPE_STRING << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PET_GET_VIT = 10;
                                    UI_DATA_TYPE_SHORT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PET_GET_STR = 11;
                                    UI_DATA_TYPE_SHORT << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PET_GET_AGI = 12;
                                    UI_DATA_TYPE_SHORT << 24 | 0x02 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PET_GET_INT = 13;
                                    UI_DATA_TYPE_SHORT << 24 | 0x03 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PET_GET_TYPE_NAME = 14;
                                    UI_DATA_TYPE_STRING << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PET_GET_FEALTY_NAME = 15;
                                    UI_DATA_TYPE_STRING << 24 | 0x02 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PET_GET_IS_CURRENT = 16;
                                    UI_DATA_TYPE_BOOLEAN << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PET_GET_REST_POINT = 17;
                                    UI_DATA_TYPE_SHORT << 24 | 0x04 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PET_GET_UNTRADE_POINT = 18;
                                    UI_DATA_TYPE_SHORT << 24 | 0x05 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PET_GET_LEVEL = 19;
                                    UI_DATA_TYPE_SHORT << 24 | 0x06 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PET_GET_EXP = 20;
                                    UI_DATA_TYPE_INT << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PET_GET_UPGRADE_EXP = 21;
                                    UI_DATA_TYPE_INT << 24 | 0x02 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PET_GET_HP = 22;
                                    UI_DATA_TYPE_INT << 24 | 0x03 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PET_GET_MP = 23;
                                    UI_DATA_TYPE_INT << 24 | 0x04 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PET_GET_MAXHP = 24;
                                    UI_DATA_TYPE_INT << 24 | 0x05 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PET_GET_MAXMP = 25;
                                    UI_DATA_TYPE_INT << 24 | 0x06 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PET_GET_FEED_ITEM_ID_LIST = 26;
                                    UI_DATA_TYPE_INT << 24 | 0x07 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PET_GET_FEED_COUNT_LIST = 27;
                                    UI_DATA_TYPE_SHORT << 24 | 0x07 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PET_GET_FEED_NAME_LIST = 28;
                                    UI_DATA_TYPE_STRING << 24 | 0x03 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PET_GET_FEED_LIST_COUNT = 29;
                                    UI_DATA_TYPE_INT << 24 | 0x07 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_PET_GET_SKILL_LIST_COUNT = 30;
                                    UI_DATA_TYPE_INT << 24 | 0x08 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_PET_GET_SKILL_ID_LIST = 31;
                                    UI_DATA_TYPE_INT << 24 | 0x08 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PET_GET_SKILL_NAME_LIST = 32;
                                    UI_DATA_TYPE_STRING << 24 | 0x04 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PET_GET_SKILL_CONTENT = 33;
                                    UI_DATA_TYPE_STRING << 24 | 0x05 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                    },
                    //ATTR_TYPE_PET_TRADE = 20;
                    {
                                    //ATTR_INDEX_PET_TRADE_GET_PAGECOUNT = 0;
                                    UI_DATA_TYPE_PAGE_SIZE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_PET_TRADE_DATA_OK = 1;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_PET_TRADE_REQUEST = 2;
                                    UI_DATA_REQUEST << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_PET_TRADE_DATA_ERROR = 3;
                                    UI_DATA_DATA_ERROR << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_PET_TRADE_PREPARE_NET = 4;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_FALSE,
                                    //ATTR_INDEX_PET_TRADE_GET_TRADE_ITEM_ID_LIST = 5;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PET_TRADE_GET_TRADE_ID_LIST = 6;
                                    UI_DATA_TYPE_INT << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PET_TRADE_GET_TRADE_NAME_LIST = 7;
                                    UI_DATA_TYPE_STRING << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_PET_TRADE_GET_TRADE_LIST_COUNT = 8;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                    },
                    //ATTR_TYPE_SYSTEM_OPTION = 21;
                    {
                                    //ATTR_INDEX_SYSTEM_OPTION_GET_PAGECOUNT = 0;
                                    UI_DATA_TYPE_PAGE_SIZE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_SYSTEM_OPTION_DATA_OK = 1;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_SYSTEM_OPTION_REQUEST = 2;
                                    UI_DATA_REQUEST << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_SYSTEM_OPTION_DATA_ERROR = 3;
                                    UI_DATA_DATA_ERROR << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_SYSTEM_OPTION_PREPARE_NET = 4;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_FALSE,
                                    //ATTR_INDEX_SYSTEM_OPTION_GET_OPTION_LIST_COUNT = 5;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_SYSTEM_OPTION_GET_OPTION_ID_LIST = 6;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                    },
                    //ATTR_TYPE_REPAIR = 22;
                    {
                                    //ATTR_INDEX_REPAIR_GET_PAGECOUNT = 0;
                                    UI_DATA_TYPE_PAGE_SIZE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_REPAIR_DATA_OK = 1;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_REPAIR_REQUEST = 2;
                                    UI_DATA_REQUEST << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_REPAIR_DATA_ERROR = 3;
                                    UI_DATA_DATA_ERROR << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_REPAIR_PREPARE_NET = 4;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_FALSE,
                                    //ATTR_INDEX_REPAIR_GET_REPAIR_ITEM_ID_LIST = 5;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_REPAIR_GET_REPAIR_ID_LIST = 6;
                                    UI_DATA_TYPE_INT << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_REPAIR_GET_REPAIR_NAME_LIST = 7;
                                    UI_DATA_TYPE_STRING << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_REPAIR_GET_REPAIR_LIST_COUNT = 8;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_REPAIR_GET_REPAIR_FEE = 9;
                                    UI_DATA_TYPE_INT << 24 | 0x02 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_REPAIR_GET_REPAIR_EQUIP_LIST = 10;
                                    UI_DATA_TYPE_INT << 24 | 0x03 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_REPAIR_GET_REPAIR_DATA_COUNT = 11;
                                    UI_DATA_TYPE_INT << 24 | 0x09 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_REPAIR_GET_REPAIR_DATA_LIST = 12;
                                    UI_DATA_TYPE_INT << 24 | 0x09 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX
                    },
                    //ATTR_TYPE_BLACK_VIEW = 23;
                    {
                                    //ATTR_INDEX_BLACK_VIEW_GET_PAGECOUNT = 0;
                                    UI_DATA_TYPE_PAGE_SIZE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_BLACK_VIEW_GET_LIST_COUNT = 1;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT,
                                    //ATTR_INDEX_BLACK_VIEW_GET_LIST_ID = 2;
                                    UI_DATA_TYPE_INT << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_BLACK_VIEW_GET_LIST_NAME = 3;
                                    UI_DATA_TYPE_STRING << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_FRIEND_VIEW_DATA_OK = 4;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_BLACK_VIEW_REQUEST = 5;
                                    UI_DATA_REQUEST << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_BLACK_VIEW_DATA_ERROR = 6;
                                    UI_DATA_DATA_ERROR << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_BLACK_VIEW_PREPARE_NET = 7;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_FALSE,
                                    //ATTR_INDEX_BLACK_VIEW_GET_CONTENT = 8;
                                    UI_DATA_TYPE_STRING << 24 | 0x02 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_BLACK_VIEW_GET_NAME = 9;
                                    UI_DATA_TYPE_STRING << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_BLACK_VIEW_GET_ONLINE = 10;
                                    UI_DATA_TYPE_BOOLEAN << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_BLACK_VIEW_GET_ADDING_PLAYER_ID = 11;
                                    UI_DATA_TYPE_INT << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_BLACK_VIEW_GET_ADDING_PLAYER_ONLINE = 12;
                                    UI_DATA_TYPE_BOOLEAN << 24 | 0x01 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                    },
                    //ATTR_TYPE_VIEW_EQUIP = 24;
                    {
                                    //ATTR_INDEX_VIEW_EQUIP_GET_PAGECOUNT = 0;
                                    UI_DATA_TYPE_PAGE_SIZE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_VIEW_EQUIP_DATA_OK = 1;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_VIEW_EQUIP_REQUEST = 2;
                                    UI_DATA_REQUEST << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_VIEW_EQUIP_DATA_ERROR = 3;
                                    UI_DATA_DATA_ERROR << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_VIEW_EQUIP_PREPARE_NET = 4;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_FALSE,
                                    //ATTR_INDEX_VIEW_EQUIP_GET_EQUIP_NAME_LIST = 5;
                                    UI_DATA_TYPE_STRING << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_INDEX,
                                    //ATTR_INDEX_VIEW_EQUIP_GET_EQUIP_LIST_COUNT = 6;
                                    UI_DATA_TYPE_GAMEITEM << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_COUNT
                    },
                    //ATTR_TYPE_GENERIC_LSIT_VIEW = 25;
                    {
                                    //ATTR_INDEX_GENERIC_LIST_GET_PAGECOUNT = 0;
                                    UI_DATA_TYPE_PAGE_SIZE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_GENERIC_LIST_DATA_OK = 1;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_GENERIC_LIST_REQUEST = 2;
                                    UI_DATA_REQUEST << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_GENERIC_LIST_DATA_ERROR = 3;
                                    UI_DATA_DATA_ERROR << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_GENERIC_LIST_PREPARE_NET = 4;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_FALSE,
                    },
                    //ATTR_TYPE_ISHOP_VIEW = 26;
                    {
                                    //ATTR_INDEX_ISHOP_GET_PAGECOUNT = 0;
                                    UI_DATA_TYPE_PAGE_SIZE << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_ISHOP_DATA_OK = 1;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_ISHOP_REQUEST = 2;
                                    UI_DATA_REQUEST << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_ISHOP_DATA_ERROR = 3;
                                    UI_DATA_DATA_ERROR << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_PRECISION,
                                    //ATTR_INDEX_ISHOP_PREPARE_NET = 4;
                                    UI_DATA_DATA_OK << 24 | 0x00 << 16 | 0x00 << 8 | UI_DATA_CONTROL_FALSE,
                    }
    };

    //Change Equip Data
    public static boolean equip_data_ok = false; //装备修改
    public static boolean equip_data_error = false;
    public static boolean equip_request = false;
    public static Hashtable equip_net_request = new Hashtable(); //装备修改

    //Change Pro  Data
    public static boolean pro_data_ok = false; //属性修改
    public static boolean pro_data_error = false;
    public static boolean pro_request = false;
    public static Hashtable pro_net_request = new Hashtable(); //属性修改

    //delete actor data
    public static boolean actorDelete_ok = false;
    public static boolean actorDelete_error = false;
    public static boolean actorDelete_request = false;
    public static Hashtable actorDelete_net_request = new Hashtable();

    //Shop Data
    public static final byte SHOP_ITEM_TYPE_SELL = 1;
    public static final byte SHOP_ITEM_TYPE_BUY = 2;
    public static final byte SHOP_ITEM_TYPE_MAKE = 3;
    public static final byte SHOP_ITEM_TYPE_STORE = 4;

    /*---------- Game Functions Begin----------*/

    public static void clearTaskUI(){
        taskUIReady = false;

        taskUIReady = false;
        taskUIBackState = -1;
        taskUIType = 0; //0: list, 1: content
        taskUITitle = null;
        taskUIList = null;
        taskUICommand = null;
        taskUIListColor = null;
        taskUICommandColor = null;
        taskUICommandShowing = false;
        taskUIInputPara = null;
        taskUIInputResult = null;
        taskUIInputStatus = 0;
        taskUIErrorMessage = null;
        taskUIListScrollOffset = 0;
        taskUIListSelect = 0;
        taskUIListScrollDir = 0;
        taskUIListScrollMax = 0;
        taskUIListNeedScroll = false;
        taskUITitleScrollOffset = 0;
        taskUITitleScrollDir = 0;
        taskUITitleScrollMax = 0;
        taskUITitleNeedScroll = false;
        //#if TouchScreen == true
        taskUIListSelectTarget= false;
        StaticUtils.pressedDoubleButton = false;
        StaticUtils.setFocusButton(-1);
        pressedVM = false;
        firstPressed = false;
        //#endif
        taskUISelectColor = TASK_UI_DEFAULT_SELECT_COLOR;

        taskUIGameRequest = false;
        taskUIGameItemCount = 0;
        taskUIGameItemInfo = null;
        taskUIGameItemString = null;

        uiRequestTaskId = 0;
        uiDataId = 0;
        uiDataTitle = null;
        uiDataRequest = false;
        uiDataDataOk = false;
        uiDataDataError = false;
        uiDataBoolean = new boolean[10][];
        uiDataByte = new byte[10][];
        uiDataShort = new short[10][];
        uiDataInt = new int[10][];
        uiDataString = new String[10][];
        uiDataGameItem = new GameItem[10][];
        uiDataSegment = null;
        uiDataStream = null;
        uiDataNetRequest.clear();

        equip_data_error = false;
        equip_data_ok = false;
        equip_request = false;
        equip_net_request.clear();

        pro_data_error = false;
        pro_data_ok = false;
        pro_request = false;
        pro_net_request.clear();

        actorDelete_error = false;
        actorDelete_ok = false;
        actorDelete_request = false;
        actorDelete_net_request.clear();

        World.setGameState(null);
        World.instance.saveNetChatInputData();

        System.gc();
    }

    public static void clearChangeEquipUI(){
        equip_data_error = false;
        equip_data_ok = false;
        equip_request = false;
        equip_net_request.clear();

    }

    public static void clearChangeProUI(){
        pro_data_error = false;
        pro_data_ok = false;
        pro_request = false;
        pro_net_request.clear();
    }

    public static void taskSetAttr(byte type, byte index, int valueI, String valueS){
        String[] tempStr;

        try{
            switch(type){
                case ATTR_TYPE_UIMODE:
                    switch(valueI){
                        case UIMODE_UNSET:
                            clearTaskUI();

                            break;
                        case UIMODE_SET_BLOCK:
                        case UIMODE_SET_NOBLOCK:
                            if(World.gameState == null){
                                taskUIBackState = -1;
                            }else{
                                taskUIBackState = World.gameState.type;
                            }

                            GameState state = new GameState(STATE_TASKUI);
                            state.paintBackGroud = true;
                            World.setGameState(state);

                            break;
                    }

                    break;
                case ATTR_TYPE_TASKUI:
                    switch(index){
                        case ATTR_INDEX_TASKUI_TITLE:
                            taskUITitleColor = ImageSet.COLOR_TABLE[valueI];
                            taskUITitle = valueS;

                            if(taskUITitle != null && font.stringWidth(taskUITitle) > World.viewWidth - SCREEN_MARGIN * 2 - EDGE_WIDTH * 2){
                                taskUITitleNeedScroll = true;
                                taskUITitleScrollDir = 0;
                                taskUITitleScrollMax = font.stringWidth(taskUITitle) - (World.viewWidth - SCREEN_MARGIN * 2 - EDGE_WIDTH * 2 - 16);
                                taskUITitleScrollOffset = 0;
                            }else{
                                taskUITitleNeedScroll = false;
                                taskUITitleScrollDir = 0;
                                taskUITitleScrollMax = 0;
                                taskUITitleScrollOffset = 0;
                            }

                            break;
                        case ATTR_INDEX_TASKUI_LIST:
                            taskUIList = new String[valueI];
                            taskUIListColor = new int[valueI];
                            splitTaskColorString(valueI, valueS, taskUIList, taskUIListColor);

                            if(taskUIType == TASK_UI_CONTENT){
                                String[] ss = World.splitString(taskUIList[0], World.viewWidth - SCREEN_MARGIN * 2 - EDGE_WIDTH * 2 - BOX_MARGIN * 2, GameState.font);
                                taskUIList = ss;
                                World.gameState._scroll = 0;
                            }

                            break;
                        case ATTR_INDEX_TASKUI_WAIT:
                            tempStr = splitTaskString(valueI, valueS);

                            boolean needExit = Integer.parseInt(tempStr[0]) == 1? true: false;
                            boolean canCancel = Integer.parseInt(tempStr[1]) == 1? true: false;
                            int uiType = Integer.parseInt(tempStr[2]);

                            setTaskUIWait(needExit, canCancel, taskUIBackState, uiType, tempStr[3]);

                            break;
                        case ATTR_INDEX_TASKUI_INPUT_PARA:
                            tempStr = splitTaskString(valueI, valueS);
                            taskUIInputPara = tempStr;
                            taskUIInputResult = new String[(taskUIInputPara.length - 3) / 3];

                            break;
                        case ATTR_INDEX_TASKUI_START_INPUT:
                            taskUIInputStatus = TASK_UI_INPUT_DOING;

                            Form form = new Form(taskUIInputPara[0]);

                            for(int i = 0; i < taskUIInputResult.length; i++){
                                int inputType = Integer.parseInt(taskUIInputPara[i * 3 + 3]) == INPUT_PARA_NUMBER? TextField.NUMERIC: TextField.ANY;

                                form.append(new TextField(taskUIInputPara[i * 3 + 1], "", Integer.parseInt(taskUIInputPara[i * 3 + 2]), inputType));
                            }

                            form.addCommand(new Command(taskUIInputPara[taskUIInputPara.length - 2], Command.ITEM, 0));
                            form.addCommand(new Command(taskUIInputPara[taskUIInputPara.length - 1], Command.BACK, 0));
                            form.setCommandListener(World.gameState);
                            World.RecordPreousDisplay(form);

                            break;
                        case ATTR_INDEX_TASKUI_READY:
                            taskUIReady = valueI == 1? true: false;

                            break;
                        case ATTR_INDEX_TASKUI_TYPE:
                            taskUIType = valueI;

                            break;
                        case ATTR_INDEX_TASKUI_COMMAND:
                            taskUICommand = new String[valueI];
                            taskUICommandColor = new int[valueI];
                            splitTaskColorString(valueI, valueS, taskUICommand, taskUICommandColor);

                            taskUICommandNormalColor = taskUICommandColor[0];

                            break;
                        case ATTR_INDEX_TASKUI_SHOW_COMMAND:
                            taskUICommandShowing = valueI == 1? true: false;
                            taskUICommandCurrentSelect = 0;
                            //#if TouchScreen == true
                            StaticUtils.setFocusButton(-1);
                            //#endif
                            break;
                        case ATTR_INDEX_TASKUI_SET_TITLE_COLOR:
                            taskUITitleColor = ImageSet.COLOR_TABLE[valueI];

                            break;
                        case ATTR_INDEX_TASKUI_SET_LIST_COLOR: {
                            tempStr = splitTaskString(valueI, valueS);

                            int idx = Integer.parseInt(tempStr[0]);
                            int clr = Integer.parseInt(tempStr[1]);

                            if(taskUIListColor != null && taskUIListColor.length > 0){
                                if(clr < ImageSet.COLOR_TABLE.length && clr >= 0){
                                    taskUIListColor[idx] = ImageSet.COLOR_TABLE[clr];
                                }else{
                                    taskUIListColor[idx] = clr;
                                }
                            }
                        }

                            break;
                        case ATTR_INDEX_TASKUI_SET_COMMAND_COLOR: {
                            tempStr = splitTaskString(valueI, valueS);

                            int idx = Integer.parseInt(tempStr[0]);
                            int clr = Integer.parseInt(tempStr[1]);

                            if(taskUICommandColor != null && taskUICommandColor.length > 0){
                                taskUICommandColor[idx] = ImageSet.COLOR_TABLE[clr];

                                if(taskUICommandColor[idx] != taskUICommandNormalColor){
                                    taskUICommandCurrentSelect = idx;
                                }

                            }
                        }

                            break;
                        case ATTR_INDEX_TASKUI_SHOW_ERROR_MSG:
                            tempStr = splitTaskString(valueI, valueS);

                            setTaskUIWait(false, false, taskUIBackState, WAIT_WAITPRESS, tempStr[0] + " : " + taskUIErrorMessage);

                            break;
                        case ATTR_INDEX_TASKUI_SET_SELECT:
                            tempStr = splitTaskString(valueI, valueS);

                            taskUIListSelect = Integer.parseInt(tempStr[0]);

                            if(taskUIList.length > 0 && font.stringWidth(taskUIList[taskUIListSelect]) > World.viewWidth - SCREEN_MARGIN * 2 - EDGE_WIDTH * 2 - 8){
                                taskUIListNeedScroll = true;
                                taskUIListScrollDir = 0;
                                taskUIListScrollMax = font.stringWidth(taskUIList[taskUIListSelect]) - (World.viewWidth - SCREEN_MARGIN * 2 - EDGE_WIDTH * 2 - 16);
                                taskUIListScrollOffset = 0;
                            }else{
                                taskUIListNeedScroll = false;
                                taskUIListScrollDir = 0;
                                taskUIListScrollMax = 0;
                                taskUIListScrollOffset = 0;
                            }

                            break;
                        case ATTR_INDEX_TASKUI_SET_SELECT_COLOR: {
                            tempStr = splitTaskString(valueI, valueS);
                            int color = Integer.parseInt(tempStr[0]);
                            int alpha = Integer.parseInt(tempStr[1]);

                            taskUISelectColor = alpha << 24 | color;

                        }

                            break;
                        case ATTR_INDEX_TASKUI_GAME_SET_WIDTH:
                            tempStr = splitTaskString(valueI, valueS);

                            taskUIGameWidth = Integer.parseInt(tempStr[0]);
                            taskUIGameBaseX = (World.viewWidth - taskUIGameWidth) / 2;

                            break;
                        case ATTR_INDEX_TASKUI_GAME_SET_HEIGHT:
                            tempStr = splitTaskString(valueI, valueS);

                            taskUIGameHeight = Integer.parseInt(tempStr[0]);
                            taskUIGameBaseY = (World.viewWidth - taskUIGameHeight) / 2 + TITLE_HEIGHT;

                            break;
                        case ATTR_INDEX_TASKUI_GAME_SET_BACK_COLOR:
                            tempStr = splitTaskString(valueI, valueS);

                            taskUIGameBackColor = ImageSet.COLOR_TABLE[Integer.parseInt(tempStr[0])];

                            break;
                        case ATTR_INDEX_TASKUI_GAME_SET_ITEM_COUNT:
                            tempStr = splitTaskString(valueI, valueS);

                            taskUIGameItemCount = Integer.parseInt(tempStr[0]);
                            taskUIGameItemInfo = new int[taskUIGameItemCount][8];
                            taskUIGameItemString = new String[taskUIGameItemCount];

                            int w;
                            int h;
                            int iw;
                            int ih;
                            int idx;

                            iw = Integer.parseInt(tempStr[1]);
                            ih = Integer.parseInt(tempStr[2]);
                            w = taskUIGameWidth / iw;
                            h = taskUIGameHeight / ih;

                            idx = 0;

                            for(int i = 0; i < ih; i++){
                                for(int j = 0; j < iw; j++){
                                    taskUIGameItemInfo[idx][0] = j * w + taskUIGameBaseX;
                                    taskUIGameItemInfo[idx][1] = i * h + taskUIGameBaseY;
                                    taskUIGameItemInfo[idx][2] = w;
                                    taskUIGameItemInfo[idx][3] = h;

                                    idx++;
                                }
                            }

                            break;
                        case ATTR_INDEX_TASKUI_GAME_SET_ITEM_INFO: {
                            tempStr = splitTaskString(valueI, valueS);
                            int itemIndex = Integer.parseInt(tempStr[0]);

                            taskUIGameItemInfo[itemIndex][4] = Integer.parseInt(tempStr[1]);
                            taskUIGameItemInfo[itemIndex][5] = Integer.parseInt(tempStr[2]);
                            taskUIGameItemInfo[itemIndex][6] = ImageSet.COLOR_TABLE[Integer.parseInt(tempStr[3])];
                            taskUIGameItemInfo[itemIndex][7] = ImageSet.COLOR_TABLE[Integer.parseInt(tempStr[4])];
                        }
                            break;
                        case ATTR_INDEX_TASKUI_GAME_SET_ITEM_FRAME: {
                            tempStr = splitTaskString(valueI, valueS);
                            int itemIndex = Integer.parseInt(tempStr[0]);
                            int frame = Integer.parseInt(tempStr[1]);

                            taskUIGameItemInfo[itemIndex][5] = frame;
                        }
                            break;
                        case ATTR_INDEX_TASKUI_GAME_SET_SCORE:
                            tempStr = splitTaskString(valueI, valueS);
                            taskUIGameScore = Integer.parseInt(tempStr[0]);

                            break;
                        case ATTR_INDEX_TASKUI_NEW_UI_DATA: {
                            tempStr = splitTaskString(valueI, valueS);
                            byte dataType = (byte)Integer.parseInt(tempStr[0]);
                            int dataIndex = Integer.parseInt(tempStr[1]);
                            int dataCount = Integer.parseInt(tempStr[2]);

                            switch(dataType){
                                case UI_DATA_TYPE_BOOLEAN:
                                    uiDataBoolean[dataIndex] = new boolean[dataCount];

                                    break;
                                case UI_DATA_TYPE_BYTE:
                                    uiDataByte[dataIndex] = new byte[dataCount];

                                    break;
                                case UI_DATA_TYPE_SHORT:
                                    uiDataShort[dataIndex] = new short[dataCount];

                                    break;
                                case UI_DATA_TYPE_INT:
                                    uiDataInt[dataIndex] = new int[dataCount];

                                    break;
                                case UI_DATA_TYPE_STRING:
                                    uiDataString[dataIndex] = new String[dataCount];

                                    break;
                                case UI_DATA_TYPE_GAMEITEM:
                                    uiDataGameItem[dataIndex] = new GameItem[dataCount];

                                    break;
                            }
                        }

                            break;
                        case ATTR_INDEX_TASKUI_READ_UI_DATA: {
                            tempStr = splitTaskString(valueI, valueS);
                            byte dataType = (byte)Integer.parseInt(tempStr[0]);
                            int dataIndex = Integer.parseInt(tempStr[1]);
                            int dataSubIndex = Integer.parseInt(tempStr[2]);

                            switch(dataType){
                                case UI_DATA_TYPE_BOOLEAN:
                                    if(dataSubIndex < 0){
                                        uiDataBoolean[dataIndex] = uiDataSegment.readBooleans();
                                    }else{
                                        uiDataBoolean[dataIndex][dataSubIndex] = uiDataSegment.readBoolean();
                                    }

                                    break;
                                case UI_DATA_TYPE_BYTE:
                                    if(dataSubIndex < 0){
                                        uiDataByte[dataIndex] = uiDataSegment.readBytes();
                                    }else{
                                        uiDataByte[dataIndex][dataSubIndex] = uiDataSegment.readByte();
                                    }

                                    break;
                                case UI_DATA_TYPE_SHORT:
                                    if(dataSubIndex < 0){
                                        uiDataShort[dataIndex] = uiDataSegment.readShorts();
                                    }else{
                                        uiDataShort[dataIndex][dataSubIndex] = uiDataSegment.readShort();
                                    }

                                    break;
                                case UI_DATA_TYPE_INT:
                                    if(dataSubIndex < 0){
                                        uiDataInt[dataIndex] = uiDataSegment.readInts();
                                    }else{
                                        uiDataInt[dataIndex][dataSubIndex] = uiDataSegment.readInt();
                                    }

                                    break;
                                case UI_DATA_TYPE_STRING:
                                    if(dataSubIndex < 0){
                                        uiDataString[dataIndex] = uiDataSegment.readStrings();
                                    }else{
                                        uiDataString[dataIndex][dataSubIndex] = uiDataSegment.readString();
                                    }

                                    break;
                            }
                        }

                            break;
                        case ATTR_INDEX_TASKUI_SET_UI_DATA: {
                            tempStr = splitTaskString(valueI, valueS);
                            byte dataType = (byte)Integer.parseInt(tempStr[0]);
                            int dataIndex = Integer.parseInt(tempStr[1]);
                            int dataSubIndex = Integer.parseInt(tempStr[2]);

                            switch(dataType){
                                case UI_DATA_TYPE_BOOLEAN:
                                    uiDataBoolean[dataIndex][dataSubIndex] = Integer.parseInt(tempStr[3]) != 0;

                                    break;
                                case UI_DATA_TYPE_BYTE:
                                    uiDataByte[dataIndex][dataSubIndex] = (byte)Integer.parseInt(tempStr[3]);

                                    break;
                                case UI_DATA_TYPE_SHORT:
                                    uiDataShort[dataIndex][dataSubIndex] = (short)Integer.parseInt(tempStr[3]);

                                    break;
                                case UI_DATA_TYPE_INT:
                                    uiDataInt[dataIndex][dataSubIndex] = Integer.parseInt(tempStr[3]);

                                    break;
                                case UI_DATA_TYPE_STRING:
                                    uiDataString[dataIndex][dataSubIndex] = tempStr[3];

                                    break;
                                case UI_DATA_TYPE_TITLE:
                                    uiDataTitle = tempStr[3];

                                    break;
                            }
                        }

                            break;
                        case ATTR_INDEX_TASKUI_CLEAR_SEGMENT:
                            uiDataSegment = null;

                            break;
                        case ATTR_INDEX_TASKUI_RESET_SEGMENT:
                            uiDataSegment.reset();

                            break;
                        case ATTR_INDEX_TASKUI_READ_ITEM_FROM_BYTES: {
                            tempStr = splitTaskString(valueI, valueS);
                            int itemIndex = Integer.parseInt(tempStr[0]);
                            int itemSubIndex = Integer.parseInt(tempStr[1]);
                            byte itemType = (byte)Integer.parseInt(tempStr[2]);
                            int bytesIndex = Integer.parseInt(tempStr[3]);

                            ByteArrayInputStream bis = new ByteArrayInputStream(uiDataByte[bytesIndex]);
                            DataInputStream dis = new DataInputStream(bis);

                            uiDataGameItem[itemIndex][itemSubIndex] = Sprite.readItemsData(dis, itemType);
                        }

                            break;
                        case ATTR_INDEX_TASKUI_SHOW_ITEM: {
                            tempStr = splitTaskString(valueI, valueS);
                            int itemIndex = Integer.parseInt(tempStr[0]);
                            int itemSubIndex = Integer.parseInt(tempStr[1]);

                            showGameItem = uiDataGameItem[itemIndex][itemSubIndex];

                            setTaskUIWait(false, false, taskUIBackState, WAIT_VIEW_ATTACHMENT, "");
                        }

                            break;
                        case ATTR_INDEX_TASKUI_READ_ATTACHMENT_FROM_BYTES: {
                            tempStr = splitTaskString(valueI, valueS);
                            int itemIndex = Integer.parseInt(tempStr[0]);
                            int itemSubIndex = Integer.parseInt(tempStr[1]);
                            int bytesIndex = Integer.parseInt(tempStr[2]);

                            uiDataGameItem[itemIndex][itemSubIndex] = GameItem.getAttachment(uiDataByte[bytesIndex]);
                        }

                            break;
                        case ATTR_INDEX_TASKUI_BUILD_BAG_ITEM_LIST: {
                            tempStr = splitTaskString(valueI, valueS);
                            boolean itemNeed = Integer.parseInt(tempStr[0]) != 0;
                            boolean taskItemNeed = Integer.parseInt(tempStr[1]) != 0;
                            boolean bagEquipItemNeed = Integer.parseInt(tempStr[2]) != 0;
                            boolean equipItemNeed = Integer.parseInt(tempStr[3]) != 0;
                            boolean allowBind = Integer.parseInt(tempStr[4]) != 0;
                            boolean repairFeeNeed = Integer.parseInt(tempStr[5]) != 0;
                            int idBegin = Integer.parseInt(tempStr[6]);
                            int idEnd = Integer.parseInt(tempStr[7]);

                            Vector items = new Vector();

                            if(itemNeed){
                                for(int i = 0; i < World.player.basicItems.size(); i++){
                                    items.addElement(World.player.basicItems.elementAt(i));
                                }
                            }

                            if(taskItemNeed){
                                for(int i = 0; i < World.player.taskItems.size(); i++){
                                    items.addElement(World.player.taskItems.elementAt(i));
                                }
                            }

                            if(bagEquipItemNeed){
                                for(int i = 0; i < World.player.equipsInBag.size(); i++){
                                    items.addElement(World.player.equipsInBag.elementAt(i));
                                }
                            }

                            if(equipItemNeed){
                                for(int i = 0; i < World.player.playerEquips.length; i++){
                                    if(World.player.playerEquips[i].type != GameItem.TYPE_NULL){
                                        items.addElement(World.player.playerEquips[i]);
                                    }
                                }
                            }

                            Vector needRemove = new Vector();

                            for(int i = 0; i < items.size(); i++){
                                GameItem tmpItem = (GameItem)items.elementAt(i);

                                if(!allowBind && tmpItem.bind){
                                    needRemove.addElement(tmpItem);
                                }else if(repairFeeNeed && tmpItem.repairFee <= 0){
                                    needRemove.addElement(tmpItem);
                                }else if(idBegin > 0 && (tmpItem.itemId < idBegin || tmpItem.itemId > idEnd)){
                                    needRemove.addElement(tmpItem);
                                }
                            }

                            for(int i = 0; i < needRemove.size(); i++){
                                items.removeElement(needRemove.elementAt(i));
                            }

                            uiDataGameItem[0] = new GameItem[items.size()];
                            items.copyInto(uiDataGameItem[0]);
                        }

                            break;
                        case ATTR_INDEX_TASKUI_BUILD_SEND_ATTACHMENT: {
                            tempStr = splitTaskString(valueI, valueS);
                            byte attType = (byte)Integer.parseInt(tempStr[0]);
                            int itemIndex = Integer.parseInt(tempStr[1]);
                            int count = Integer.parseInt(tempStr[2]);

                            if(attType == 1){
                                attachmentTypeItem = new GameItem(GameItem.TYPE_MONEY);
                                attachmentTypeItem.price = count;
                            }else if(attType == 2){
                                if(uiDataGameItem[0][itemIndex].type == GameItem.TYPE_EQUIP){
                                    count = 1;
                                }

                                attachmentTypeItem = new GameItem(uiDataGameItem[0][itemIndex].type);
                                attachmentTypeItem.itemId = uiDataGameItem[0][itemIndex].itemId;
                                attachmentTypeItem.id = uiDataGameItem[0][itemIndex].id;
                                attachmentTypeItem.name = uiDataGameItem[0][itemIndex].name;
                                attachmentTypeItem.count = (short)count;
                            }else{
                                attachmentTypeItem = new GameItem(GameItem.TYPE_NULL);
                            }
                        }

                            break;
                        case ATTR_INDEX_TASKUI_DELETE_ATTACHMENT_ITEM:
                            World.player.deleteAttachmentItem(attachmentTypeItem);

                            break;
                        case ATTR_INDEX_TASKUI_COPY_UI_DATA: {
                            tempStr = splitTaskString(valueI, valueS);
                            byte dataType = (byte)Integer.parseInt(tempStr[0]);
                            int dataIndexSrc = Integer.parseInt(tempStr[1]);
                            int dataIndexDest = Integer.parseInt(tempStr[2]);

                            switch(dataType){
                                case UI_DATA_TYPE_BOOLEAN:
                                    uiDataBoolean[dataIndexDest] = new boolean[uiDataBoolean[dataIndexSrc].length];
                                    System.arraycopy(uiDataBoolean[dataIndexSrc], 0, uiDataBoolean[dataIndexDest], 0, uiDataBoolean[dataIndexSrc].length);

                                    break;
                                case UI_DATA_TYPE_BYTE:
                                    uiDataByte[dataIndexDest] = new byte[uiDataByte[dataIndexSrc].length];
                                    System.arraycopy(uiDataByte[dataIndexSrc], 0, uiDataByte[dataIndexDest], 0, uiDataByte[dataIndexSrc].length);

                                    break;
                                case UI_DATA_TYPE_SHORT:
                                    uiDataShort[dataIndexDest] = new short[uiDataShort[dataIndexSrc].length];
                                    System.arraycopy(uiDataShort[dataIndexSrc], 0, uiDataShort[dataIndexDest], 0, uiDataShort[dataIndexSrc].length);

                                    break;
                                case UI_DATA_TYPE_INT:
                                    uiDataInt[dataIndexDest] = new int[uiDataInt[dataIndexSrc].length];
                                    System.arraycopy(uiDataInt[dataIndexSrc], 0, uiDataInt[dataIndexDest], 0, uiDataInt[dataIndexSrc].length);

                                    break;
                                case UI_DATA_TYPE_STRING:
                                    uiDataString[dataIndexDest] = new String[uiDataString[dataIndexSrc].length];
                                    System.arraycopy(uiDataString[dataIndexSrc], 0, uiDataString[dataIndexDest], 0, uiDataString[dataIndexSrc].length);

                                    break;
                                case UI_DATA_TYPE_GAMEITEM:
                                    uiDataGameItem[dataIndexDest] = new GameItem[uiDataGameItem[dataIndexSrc].length];
                                    System.arraycopy(uiDataGameItem[dataIndexSrc], 0, uiDataGameItem[dataIndexDest], 0, uiDataGameItem[dataIndexSrc].length);

                                    break;
                            }
                        }

                            break;
                        case ATTR_INDEX_TASKUI_BUILD_STREAM_FROM_BYTES:
                            tempStr = splitTaskString(valueI, valueS);
                            int bytesIndex = Integer.parseInt(tempStr[0]);

                            ByteArrayInputStream bis = new ByteArrayInputStream(uiDataByte[bytesIndex]);
                            DataInputStream dis = new DataInputStream(bis);

                            uiDataStream = dis;

                            break;
                        case ATTR_INDEX_TASKUI_CLEAR_STREAM:
                            try{
                                uiDataStream.close();
                            }catch(IOException e){
                                //#debug
                                e.printStackTrace();
                            }

                            break;
                        case ATTR_INDEX_TASKUI_CREATE_GAMEITEM: {
                            tempStr = splitTaskString(valueI, valueS);
                            int itemIndex = Integer.parseInt(tempStr[0]);
                            int itemSubIndex = Integer.parseInt(tempStr[1]);
                            byte itemType = (byte)Integer.parseInt(tempStr[2]);
                            int itemItemId = Integer.parseInt(tempStr[3]);
                            int itemId = Integer.parseInt(tempStr[4]);
                            short itemCount = (short)Integer.parseInt(tempStr[5]);
                            String itemName = tempStr[6];

                            uiDataGameItem[itemIndex][itemSubIndex] = new GameItem(itemType);
                            uiDataGameItem[itemIndex][itemSubIndex].itemId = itemItemId;
                            uiDataGameItem[itemIndex][itemSubIndex].id = itemId;
                            uiDataGameItem[itemIndex][itemSubIndex].count = (short)itemCount;
                            uiDataGameItem[itemIndex][itemSubIndex].name = itemName;
                        }

                            break;
                        case ATTR_INDEX_TASKUI_ADD_ITEM_TO_BAG: {
                            tempStr = splitTaskString(valueI, valueS);
                            int itemIndex = Integer.parseInt(tempStr[0]);
                            int itemSubIndex = Integer.parseInt(tempStr[1]);

                            World.player.addItemToBag(uiDataGameItem[itemIndex][itemSubIndex]);
                        }

                            break;
                        case ATTR_INDEX_TASKUI_CLEAR_ERROR:
                            uiDataDataError = false;

                            break;
                        case ATTR_INDEX_TASKUI_SET_EQUIP_REPAIR_FEE: {
                            tempStr = splitTaskString(valueI, valueS);
                            int itemIndex = Integer.parseInt(tempStr[0]);
                            int itemSubIndex = Integer.parseInt(tempStr[1]);
                            int repairFee = Integer.parseInt(tempStr[2]);

                            uiDataGameItem[itemIndex][itemSubIndex].repairFee = repairFee;
                        }

                            break;
                        case ATTR_INDEX_TASKUI_SET_EQUIP_CURRENT_EQUIP: {
                            tempStr = splitTaskString(valueI, valueS);
                            int itemIndex = Integer.parseInt(tempStr[0]);
                            int itemSubIndex = Integer.parseInt(tempStr[1]);
                            boolean currentEquip = Integer.parseInt(tempStr[2]) != 0;

                            uiDataGameItem[itemIndex][itemSubIndex].currentEquip = currentEquip;
                        }

                            break;
                        case ATTR_INDEX_TASKUI_REPAIR_EQUIPS: {
                            tempStr = splitTaskString(valueI, valueS);

                            int[] repairData = new int[tempStr.length];

                            for(int i = 0; i < tempStr.length; i++){
                                repairData[i] = Integer.parseInt(tempStr[i]);
                            }

                            int sendSerial = World.sendRequest(CONN_REPAIRE, new Object[]{
                                repairData
                            }, false);

                            uiDataDataError = false;
                            uiDataNetRequest.put(new Integer(sendSerial), new Integer(sendSerial));
                        }

                            break;
                        case ATTR_INDEX_TASKUI_CONFIRM_REPAIR: {
                            tempStr = splitTaskString(valueI, valueS);

                            for(int i = 0; i < tempStr.length / 2; i++){
                                GameItem tmpItem = new GameItem(GameItem.TYPE_EQUIP);

                                tmpItem.itemId = Integer.parseInt(tempStr[i * 2]);
                                tmpItem.id = Integer.parseInt(tempStr[i * 2 + 1]);

                                GameItem foundItem = World.player.hasItem(tmpItem, false);

                                if(foundItem != null){
                                    foundItem.currentDurability = foundItem.durability;
                                    foundItem.repairFee = 0;
                                }
                            }

                            World.player.reCalculateAttributes();
                        }

                            break;
                    }

                    break;
                case ATTR_TYPE_PLAYER_DATA:
                    switch(index){
                        case ATTR_INDEX_PLAYER_DATA_CLEAR_TOUCH_NPC:
                            touchNpcInfo = null;

                            break;
                        case ATTR_INDEX_PLAYER_DATA_ADD_SKILL:
                            tempStr = splitTaskString(valueI, valueS);
                            short[] tmpTable = new short[World.player.skillList.length + 1];
                            System.arraycopy(World.player.skillList, 0, tmpTable, 0, World.player.skillList.length);
                            tmpTable[tmpTable.length - 1] = (short)Integer.parseInt(tempStr[0]);
                            World.player.skillList = tmpTable;
                            World.player.initSkillTable();

                            break;
                        case ATTR_INDEX_PLAYER_DATA_CHANGE_MONEY:
                            tempStr = splitTaskString(valueI, valueS);
                            int money = Integer.parseInt(tempStr[0]);

                            World.player.money += money;

                            break;
                        case ATTR_INDEX_PLAYER_DATA_CHANGE_ABILITY:
                            tempStr = splitTaskString(valueI, valueS);
                            int skillType = Integer.parseInt(tempStr[0]);
                            int restInc = Integer.parseInt(tempStr[1]);
                            int abilityInc = Integer.parseInt(tempStr[2]);

                            World.player.restAbility += restInc;
                            World.player.ability[skillType] += abilityInc;

                            break;
                        case ATTR_INDEX_PLAYER_DATA_ABANDON_TASK:
                            tempStr = splitTaskString(valueI, valueS);
                            short taskId = (short)Integer.parseInt(tempStr[0]);

                            World.unFinishedTask.remove(new Integer(taskId));
                            World._gtvm.removeTask(taskId, false);

                            break;
                        case ATTR_INDEX_PLAYER_DATA_ADD_FRIEND: {
                            tempStr = splitTaskString(valueI, valueS);
                            Integer id = Integer.valueOf(tempStr[0]);
                            String name = tempStr[1];

                            World.addFriend(id, (short)1, name);
                            break;
                        }
                        case ATTR_INDEX_PLAYER_DATA_ADD_BLACK: {
                            tempStr = splitTaskString(valueI, valueS);
                            Integer id = Integer.valueOf(tempStr[0]);
                            String name = tempStr[1];

                            World.addBlackList(id, name);
                            break;
                        }
                        case ATTR_INDEX_PLAYER_DATA_CHANGE_FRIEND_ONLINE: {
                            tempStr = splitTaskString(valueI, valueS);
                            int playerId = Integer.parseInt(tempStr[0]);
                            boolean online = (Integer.parseInt(tempStr[1]) != 0);

                            if(playerId >= 0){
                                World.changeFriendStatus(playerId, (short)0, online, false);
                            }
                        }

                            break;
                        case ATTR_INDEX_PLAYER_DATA_DELETE_FRIEND: {
                            tempStr = splitTaskString(valueI, valueS);
                            Integer id = Integer.valueOf(tempStr[0]);

                            World.deleteFriend(id);
                        }

                            break;
                        case ATTR_INDEX_PLAYER_DATA_DELETE_BLACK: {
                            tempStr = splitTaskString(valueI, valueS);
                            Integer id = Integer.valueOf(tempStr[0]);
                            World.deleteBlackList(id);
                        }
                            break;
                        case ATTR_INDEX_PLAYER_DATA_CLEAR_NEW_MAIL_HINT:
                            World.hideHint(World.HINT_MAIL);

                            break;
                        case ATTR_INDEX_PLAYER_DATA_CHANGE_VIT:
                            tempStr = splitTaskString(valueI, valueS);
                            int vit = Integer.parseInt(tempStr[0]);

                            World.player.productVitality += vit;

                            break;
                        case ATTR_INDEX_PLAYER_DATA_SHOW_DEBUG_MSG:
                            tempStr = splitTaskString(valueI, valueS);
                            //#debug
                            System.out.println(tempStr[0]);

                            break;
                        case ATTR_INDEX_PLAYER_DATA_OPEN_URL:
                            tempStr = splitTaskString(valueI, valueS);

                            GameState state = new GameState(STATE_OPENURL);
                            state._message = tempStr[0];
                            if(state.thread == null){
                                state.thread = new Thread(state);
                                state.thread.start();
                            }

                            break;
                        case ATTR_INDEX_PLAYER_DATA_CHANGE_CHAT_OPTION_PRIORITY: {
                            tempStr = splitTaskString(valueI, valueS);
                            int optionIndex = Integer.parseInt(tempStr[0]);
                            byte priority = (byte)Integer.parseInt(tempStr[1]);

                            World.net_chat_priority_option[optionIndex] = priority;
                        }

                            break;
                        case ATTR_INDEX_PLAYER_DATA_CHANGE_CHAT_OPTION_COLOR: {
                            tempStr = splitTaskString(valueI, valueS);
                            int optionIndex = Integer.parseInt(tempStr[0]);
                            byte color = (byte)Integer.parseInt(tempStr[1]);

                            World.net_chat_color_option[optionIndex] = color;
                        }

                            break;
                        case ATTR_INDEX_PLAYER_DATA_COMMIT_CHAT_OPTION:
                            World.saveChatOption();

                            break;
                        case ATTR_INDEX_PLAYER_DATA_SET_CURRENT_PET: {
                            tempStr = splitTaskString(valueI, valueS);
                            int petId = Integer.parseInt(tempStr[0]);

                            World.player.petCurrent = World.player.getPet(petId);
                            World.monsterSetPlayerBattle = true;
                            World.monsterSetPetBattle = false ;
                        }

                            break;
                        case ATTR_INDEX_PLAYER_DATA_RELEASE_PET: {
                            tempStr = splitTaskString(valueI, valueS);
                            int petId = Integer.parseInt(tempStr[0]);

                            if(World.player.petCurrent == World.player.getPet(petId)){
                                World.player.petCurrent = null;
                            }

                            World.player.petBag.remove(new Integer(petId));
                        }

                            break;
                        case ATTR_INDEX_PLAYER_DATA_SET_SYSTEM_OPTIN: {
                            tempStr = splitTaskString(valueI, valueS);
                            int optionIndex = Integer.parseInt(tempStr[0]);
                            int option = Integer.parseInt(tempStr[1]);

                            World.systemOption[optionIndex] = (short)option;
                        }

                            break;
                        case ATTR_INDEX_PLAYER_DATA_CONFIRM_SYSTEM_OPTION:
                            World.parseSystemOption();

                            World.sendRequest(CONN_CHANGE_OPTION, new Object[]{
                                World.systemOption
                            }, false);

                            break;

                        case ATTR_INDEX_PLAYER_DATA_OFF_ALL_EQUIP:
                            int equipCount = 0;

                            for(int i = 0; i < World.player.playerEquips.length; i++){
                                if(World.player.playerEquips[i] != null && World.player.playerEquips[i].type != GameItem.TYPE_NULL){
                                    equipCount++;
                                }
                            }

                            if(World.player.getItemTotalCount() + equipCount < World.player.bagSize){
                                World.player.backupEquips();

                                for(int i = 0; i < World.player.playerEquips.length; i++){
                                    if(World.player.playerEquips[i] != null && World.player.playerEquips[i].type != GameItem.TYPE_NULL){
                                        World.player.equipsInBag.addElement(World.player.playerEquips[i]);
                                        World.player.playerEquips[i] = GameItem.createNullEquip((byte)i);
                                    }
                                }

                                requestChangeEquips();
                            }else{
                                World.showMessage("背包已满", (byte)5);
                            }

                            break;
                        case ATTR_INDEX_PLAYER_DATA_OPEN_WEB:
                            //#if Directory != NK-NGage
                            try{
                                tempStr = splitTaskString(valueI, valueS);
                                String openUrl = tempStr[0];
                                boolean closeGame = Integer.parseInt(tempStr[1]) == 1;

                                iTimesMIDlet.instance.platformRequest(openUrl);
                                Thread.sleep(500);

                                if(closeGame){
                                    closeConnection();
                                    iTimesMIDlet.instance.exitGame();
                                }
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                            //#else
                            World.showMessage("你的手机不支持此功能", (byte)10);
                            //#endif

                            break;

                    }

                    break;
                default:
                    tempStr = splitTaskString(valueI, valueS);
                    setUIData(uiDefine[type][index], tempStr);

                    break;
            }
        }catch(Throwable e){
            //#debug
            e.printStackTrace();
        }

        repaintNextTime = true;
    }

    public static int taskGetAttrI(byte type, byte subType, int index){
        int result = -1;

        switch(type){
            case ATTR_TYPE_TASKUI:
                switch(subType){
                    case ATTR_INDEX_TASKUI_GET_INPUT_STATUS:
                        result = taskUIInputStatus;

                        break;
                    case ATTR_INDEX_TASKUI_GAME_REQUEST:
                        result = taskUIGameRequest? 1: 0;

                        break;
                    case ATTR_INDEX_TASKUI_GET_SEGMENT_DATA:
                        switch(index){
                            case UI_DATA_TYPE_BOOLEAN:
                                result = uiDataSegment.readBoolean()? 1: 0;

                                break;
                            case UI_DATA_TYPE_BYTE:
                                result = uiDataSegment.readByte();

                                break;
                            case UI_DATA_TYPE_SHORT:
                                result = uiDataSegment.readShort();

                                break;
                            case UI_DATA_TYPE_INT:
                                result = uiDataSegment.readInt();

                                break;
                        }

                        break;
                    case ATTR_INDEX_TAKSUI_GET_ITEM_EQUIP_Q:
                        result = uiDataGameItem[0][index].equipLevel;

                        break;
                    case ATTR_INDEX_TASKUI_GET_ITEM_EQUIP_COLOR:
                        result = GameItem.CLR_EQUIP[index];

                        break;
                    case ATTR_INDEX_TASKUI_GET_ITEM_ITEM_TYPE:
                        result = uiDataGameItem[0][index].type;

                        break;
                    case ATTR_INDEX_TASKUI_GET_ITEM_REPAIR_FEE:
                        result = uiDataGameItem[0][index].repairFee;

                        break;
                    case ATTR_INDEX_TASKUI_GET_ITEM_CURRENT_EQUIP:
                        result = uiDataGameItem[0][index].currentEquip? 1: 0;

                        break;
                    case ATTR_INDEX_TASKUI_GET_ITEM_ITEM_ID:
                        result = uiDataGameItem[0][index].itemId;

                        break;
                    case ATTR_INDEX_TASKUI_GET_ITEM_ID_OR_COUNT:
                        if(uiDataGameItem[0][index].type == GameItem.TYPE_EQUIP){
                            result = uiDataGameItem[0][index].id;
                        }else{
                            result = uiDataGameItem[0][index].count;
                        }

                        break;
                    case ATTR_INDEX_TASKUI_GET_ITEM_PRICE:
                        result = uiDataGameItem[0][index].price;

                        break;
                    case ATTR_INDEX_TASKUI_GET_UI_DATA_FROM_STREAM: {
                        try{
                            switch(index){
                                case UI_DATA_TYPE_BOOLEAN:
                                    result = uiDataStream.readBoolean()? 1: 0;

                                    break;
                                case UI_DATA_TYPE_BYTE:
                                    result = uiDataStream.readByte();

                                    break;
                                case UI_DATA_TYPE_SHORT:
                                    result = uiDataStream.readShort();

                                    break;
                                case UI_DATA_TYPE_INT:
                                    result = uiDataStream.readInt();

                                    break;
                            }
                        }catch(IOException e){
                            //#debug
                            e.printStackTrace();
                        }
                    }

                        break;
                    case ATTR_INDEX_TASKUI_GET_MATERIAL_BEGIN_ID:
                        result = GameItem.resourceBegin;

                        break;
                    case ATTR_INDEX_TASKUI_GET_MATERIAL_END_ID:
                        result = GameItem.resourceBegin + GameItem.resourceNames.length;

                        break;
                    case ATTR_INDEX_TASKUI_HAS_ITEM:
                        if(World.player.hasItem(uiDataGameItem[0][index], true) != null){
                            result = 1;
                        }else{
                            result = 0;
                        }

                        break;
                }

                break;
            case ATTR_TYPE_PLAYER_DATA:
                switch(subType){
                    case ATTR_INDEX_PLAYER_DATA_LEVEL:
                        result = World.player.level;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_EXP:
                        result = World.player.exp;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_MONEY:
                        result = World.player.money;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_SEX:
                        result = World.player.sex;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_HP:
                        result = World.player.hp;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_MP:
                        result = World.player.mp;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_HPLIMIT:
                        result = World.player.getAttribute(BattleSprite.ATTR_HPMAX);

                        break;
                    case ATTR_INDEX_PLAYER_DATA_MPLIMIT:
                        result = World.player.getAttribute(BattleSprite.ATTR_MPMAX);

                        break;
                    case ATTR_INDEX_PLAYER_DATA_TONG_DUTY:
                        result = World.player.tongDuty;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_NEED_REFRESH_HINT:
                        result = World.npcNeedHint? 1: 0;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_SERVER_TIME_HOUR:
                        result = (int)((serverTime % 86400) / 3600);

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_SERVER_TIME_DAY:
                        Calendar rightNow = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
                        rightNow.setTime(new Date(serverTime * 1000));
                        result = rightNow.get(Calendar.DAY_OF_WEEK);

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_SKILL_COUNT:
                        result = World.player.skillList.length;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_SKILL:
                        result = World.player.skillList[index];

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_REST_ABILITY:
                        result = World.player.restAbility;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_TYPE_ABILITY:
                        result = World.player.ability[index];

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_SKILL_TYPE:
                        result = Skill.getSkill(index).type;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_PRODUCT_SKILL_COUNT:
                        result = World.player.productSkill.length;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_PRODUCT_SKILL_LIST:
                        result = World.player.productSkill[index];

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_UNFINISH_TASK_COUNT:
                        result = World.unFinishedTask.size();

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_UNFINISH_TASK_ID:
                        result = ((Integer)World.getItemFromHashTable(World.unFinishedTask, index, true)).intValue();

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_NET_PLAYER_COUNT:
                        result = World.netPlayers.size();

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_NET_PLAYER_ID_LIST:
                        try{
                            result = ((MonsterSprite)World.netPlayersVector.elementAt(index)).id;
                        }catch(Exception e){
                            result = 0;
                        }

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_NET_PLAYER_SEX:
                        try{
                            result = ((MonsterSprite)World.netPlayersVector.elementAt(index)).iconID & 0x01;
                        }catch(Exception e){
                            result = 0;
                        }

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_NET_PLAYER_LEVEL:
                        try{
                            result = ((MonsterSprite)World.netPlayersVector.elementAt(index)).refreshTime;
                        }catch(Exception e){
                            result = 0;
                        }

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_NET_PLAYER_TEAM_MODE:
                        try{
                            result = ((MonsterSprite)World.netPlayersVector.elementAt(index)).flag;
                        }catch(Exception e){
                            result = 0;
                        }

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_TEAM_LEADER:
                        result = (World.teamMode && World.teamLeader)? 1: 0;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_FRIEND_COUNT:
                        result = World.netFriends.size();

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_BLACK_COUNT:
                        result = World.blackList.size();

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_FRIEND_ID_LIST:
                        result = ((Integer)World.getItemFromHashTable(World.netFriends, index, true)).intValue();

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_BLACK_ID_LIST:
                        result = ((Integer)World.getItemFromHashTable(World.blackList, index, true)).intValue();

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_FRIEND_ONLINE_LIST:
                        result = ((String[])World.getItemFromHashTable(World.netFriends, index, false))[1].equals("在线")? 1: 0;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_FRIEND_DEGREE_LIST:
                        result = Integer.parseInt(((String[])World.getItemFromHashTable(World.netFriends, index, false))[2]);

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_VIT:
                        result = World.player.productVitality;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_CHAT_OPTION_COUNT:
                        result = World.net_chat_priority_option.length;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_CHAT_OPTION_PRIORITY:
                        result = World.net_chat_priority_option[index];

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_CHAT_OPTION_COLOR:
                        result = World.net_chat_color_option[index];

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_CHAT_OPTION_CAN_CHANGE:
                        result = World.net_chat_can_change_option[index]? 1: 0;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_PET_COUNT:
                        result = World.player.petBag.size();

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_PET_ID:
                        result = ((Integer)World.getItemFromHashTable(World.player.petBag, index, true)).intValue();

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_PET_TYPE:
                        result = World.player.getPet(index).petType;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_PET_FEALTY:
                        result = World.player.getPet(index).fealty;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_PET_VIT:
                        result = World.player.getPet(index).attributes[BattleSprite.ATTR_VIT];

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_PET_STR:
                        result = World.player.getPet(index).attributes[BattleSprite.ATTR_STR];

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_PET_AGI:
                        result = World.player.getPet(index).attributes[BattleSprite.ATTR_AGI];

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_PET_INT:
                        result = World.player.getPet(index).attributes[BattleSprite.ATTR_INT];

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_PET_IS_CURRENT:
                        result = World.player.getPet(index) == World.player.petCurrent? 1: 0;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_PET_REST_POINT:
                        result = World.player.getPet(index).restPoint;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_PET_UNTRADE_POINT:
                        result = World.player.getPet(index).unTradePoint;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_PET_LEVEL:
                        result = World.player.getPet(index).level;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_PET_EXP:
                        result = World.player.getPet(index).exp;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_PET_UPGRADE_EXP:
                        result = World.player.getPet(index).getUpgradeExp();

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_PET_HP:
                        result = World.player.getPet(index).hp;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_PET_MP:
                        result = World.player.getPet(index).mp;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_PET_MAXHP:
                        result = World.player.getPet(index).attributes[BattleSprite.ATTR_HPMAX];

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_PET_MAXMP:
                        result = World.player.getPet(index).attributes[BattleSprite.ATTR_MPMAX];

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_PET_FEALTY_LEVEL:
                        result = World.player.getPet(index).getFealtyLevel();

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_PET_SKILL_COUNT:
                        result = World.player.getPet(index).skillList.length;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_PET_SKILL_ID: {
                        int petId = (index >> 3) & 0x1FFFFFFF;
                        int skillIndex = (index & 0x7);

                        result = World.player.getPet(petId).skillList[skillIndex];
                    }

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_CURRENT_PET_ID:
                        if(World.player.petCurrent == null){
                            result = -1;
                        }else{
                            result = World.player.petCurrent.petId;
                        }

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_SYSTEM_OPTION_COUNT:
                        result = World.systemOptionCount;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_SYSTEM_OPTION_ID:
                        result = World.systemOption[index];

                        break;

                }

                break;
            default:
                result = getUIDataInt(uiDefine[type][subType], index);

                break;
        }

        return result;
    }

    public static void setUIData(int uiCommand, String[] commandData){
        byte type = (byte)(uiCommand >> 24);
        byte index = (byte)((uiCommand >> 16) & 0xFF);
        byte subIndex = (byte)((uiCommand >> 8) & 0xFF);
        byte dataType = (byte)(uiCommand & 0xFF);

        switch(type){
            case UI_DATA_TYPE_BOOLEAN:
                uiDataBoolean[index][subIndex] = (dataType == UI_DATA_CONTROL_TRUE);

                break;
            case UI_DATA_REQUEST:
                uiDataRequest = (dataType == UI_DATA_CONTROL_TRUE);

                break;
            case UI_DATA_DATA_OK:
                uiDataDataOk = (dataType == UI_DATA_CONTROL_TRUE);

                break;
            case UI_DATA_DATA_ERROR:
                uiDataDataError = (dataType == UI_DATA_CONTROL_TRUE);

                break;
        }
    }

    public static int getUIDataInt(int uiCommand, int dataIndex){
        int result = 0;

        byte type = (byte)(uiCommand >> 24);
        byte index = (byte)((uiCommand >> 16) & 0xFF);
        byte subIndex = (byte)((uiCommand >> 8) & 0xFF);
        byte dataType = (byte)(uiCommand & 0xFF);

        switch(type){
            case UI_DATA_TYPE_BOOLEAN:
                if(dataType == UI_DATA_CONTROL_COUNT){
                    result = uiDataBoolean[index].length;
                }else{
                    if(dataType == UI_DATA_CONTROL_PRECISION){
                        result = uiDataBoolean[index][subIndex]? 1: 0;
                    }else{
                        result = uiDataBoolean[index][subIndex + dataIndex]? 1: 0;
                    }
                }

                break;
            case UI_DATA_TYPE_BYTE:
                if(dataType == UI_DATA_CONTROL_COUNT){
                    result = uiDataByte[index].length;
                }else{
                    if(dataType == UI_DATA_CONTROL_PRECISION){
                        result = uiDataByte[index][subIndex];
                    }else{
                        result = uiDataByte[index][subIndex + dataIndex];
                    }
                }

                break;
            case UI_DATA_TYPE_SHORT:
                if(dataType == UI_DATA_CONTROL_COUNT){
                    result = uiDataShort[index].length;
                }else{
                    if(dataType == UI_DATA_CONTROL_PRECISION){
                        result = uiDataShort[index][subIndex];
                    }else{
                        result = uiDataShort[index][subIndex + dataIndex];
                    }
                }

                break;
            case UI_DATA_TYPE_INT:
                if(dataType == UI_DATA_CONTROL_COUNT){
                    result = uiDataInt[index].length;
                }else{
                    if(dataType == UI_DATA_CONTROL_PRECISION){
                        result = uiDataInt[index][subIndex];
                    }else{
                        result = uiDataInt[index][subIndex + dataIndex];
                    }
                }

                break;
            case UI_DATA_TYPE_STRING:
                if(dataType == UI_DATA_CONTROL_COUNT){
                    result = uiDataString[index].length;
                }

                break;
            case UI_DATA_TYPE_GAMEITEM:
                if(dataType == UI_DATA_CONTROL_COUNT){
                    result = uiDataGameItem[index].length;
                }

                break;
            case UI_DATA_TYPE_PAGE_SIZE:
                result = BBS_PAGE_COUNT;

                break;
            case UI_DATA_TYPE_ID:
                result = uiDataId;

                break;
            case UI_DATA_REQUEST:
                result = uiDataRequest? 1: 0;

                break;
            case UI_DATA_DATA_OK:
                result = uiDataDataOk? 1: 0;

                break;
            case UI_DATA_DATA_ERROR:
                result = uiDataDataError? 1: 0;

                break;
        }

        return result;
    }

    public static String getUIDataString(int uiCommand, int dataIndex){
        String result = null;

        byte type = (byte)((uiCommand >> 24) & 0xFF);
        byte index = (byte)((uiCommand >> 16) & 0xFF);
        byte subIndex = (byte)((uiCommand >> 8) & 0xFF);
        byte dataType = (byte)(uiCommand & 0xFF);

        if(type == UI_DATA_TYPE_TITLE){
            result = uiDataTitle;
        }else{
            if(dataType == UI_DATA_CONTROL_PRECISION){
                result = uiDataString[index][subIndex];
            }else{
                result = uiDataString[index][subIndex + dataIndex];
            }
        }

        return result;
    }

    public static String taskGetAttrS(byte type, byte subType, int index){
        String result = null;

        switch(type){
            case ATTR_TYPE_TASKUI: {
                switch(subType){
                    case ATTR_INDEX_TASKUI_INPUT_RESULT:
                        result = taskUIInputResult[index];

                        break;
                    case ATTR_INDEX_TASKUI_GET_SEGMENT_DATA_STRING:
                        result = uiDataSegment.readString();

                        break;
                    case ATTR_INDEX_TASKUI_GET_ITEM_EQUIP_TYPE_NAME:
                        result = GameItem.EQUIP_TYPE_NAME[uiDataGameItem[0][index].equipType];

                        break;
                    case ATTR_INDEX_TASKUI_GET_ITEM_NAME:
                        if(index < 0){
                            result = uiDataGameItem[0][-(index + 1)].getName(false, -1);
                        }else{
                            result = uiDataGameItem[0][index].getName(true, -1);
                        }

                        break;
                    case ATTR_INDEX_TASKUI_GET_UI_STRING_FROM_STREAM:
                        try{
                            result = uiDataStream.readUTF();
                        }catch(IOException e){
                            //#debug
                            e.printStackTrace();
                        }

                        break;
                    case ATTR_INDEX_TASKUI_GET_ERROR_MESSAGE:
                        result = taskUIErrorMessage;

                        if(result == null){
                            result = "";
                        }

                        break;
                }
            }

                break;
            case ATTR_TYPE_PLAYER_DATA:
                switch(subType){
                    case ATTR_INDEX_PLAYER_DATA_TONG_NAME:
                        result = World.player.tongName;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_TONG_DUTY_NAME:
                        result = Sprite.getTongDutyName(index);

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_SKILL_NAME: {
                        short skillId = (short)((index >> 16) & 0xFFFF);
                        byte showMpUseMode = (byte)((index >> 8) & 0xFF);
                        boolean showLevel = ((index & 0xFF) != 0);

                        result = Skill.getSkillName(World.player, skillId, showMpUseMode, showLevel);
                    }

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_TYPE_NAME:
                        result = Skill.skillTypeName[index];

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_PRODUCT_SKILL_NAME_LIST:
                        result = Sprite.productSkillName[index];

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_UNFINISH_TASK_NAME:
                        result = (String)World.getItemFromHashTable(World.unFinishedTask, index, false);

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_NET_PLAYER_NAME_LIST:
                        try{
                            result = ((MonsterSprite)World.netPlayersVector.elementAt(index)).playerName;
                        }catch(Exception e){
                            result = "";
                        }

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_NET_PLAYER_TONG_NAME:
                        try{
                            result = ((MonsterSprite)World.netPlayersVector.elementAt(index)).tongName;
                        }catch(Exception e){
                            result = null;
                        }

                        result = result == null? "": result;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_FRIEND_NAME_LIST:
                        result = ((String[])World.getItemFromHashTable(World.netFriends, index, false))[0];

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_BLACK_NAME_LIST:
                        result = (String)World.getItemFromHashTable(World.blackList, index, false);

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_CHAT_OPTION_NAME:
                        result = World.NET_CHAT_NAME[index];

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_PET_NAME:
                        result = World.player.getPet(index).name;

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_PET_TYPE_NAME:
                        result = PetSprite.PET_TYPE_NAMES[index - 1];

                        break;
                    case ATTR_INDEX_PLAYER_DATA_GET_PET_FEALTY_NAME:
                        result = PetSprite.FEALTY_NAMES[index];

                        break;
                }

                break;
            default:
                result = getUIDataString(uiDefine[type][subType], index);

                break;
        }

        return result;
    }

    public static String[] splitTaskString(int count, String value){
        String[] result = new String[count];
        int idx;

        for(int i = 0; i < count; i++){
            idx = value.indexOf(TASK_DIVID);
            result[i] = value.substring(0, idx);
            value = value.substring(idx + 1);
        }

        return result;
    }

    public static void splitTaskColorString(int count, String value, String[] strs, int[] colors){
        int idx;

        for(int i = 0; i < count; i++){
            idx = value.indexOf(TASK_DIVID);
            strs[i] = value.substring(0, idx);
            value = value.substring(idx + 1);
            idx = value.indexOf(TASK_DIVID);
            colors[i] = Integer.parseInt(value.substring(0, idx));

            if(colors[i] < ImageSet.COLOR_TABLE.length && colors[i] >= 0){
                colors[i] = ImageSet.COLOR_TABLE[colors[i]];
            }

            value = value.substring(idx + 1);
        }
    }

    public static void taskSendRequst(byte type, byte count, String value){
        UWAPSegment segment = new UWAPSegment(type);

        String[] parms = splitTaskString(count, value);

        try{
            switch(type){
                case CONN_GET_BBS_LIST: {
                    int bbsid = Integer.parseInt(parms[0]);
                    int pageCount = Integer.parseInt(parms[1]);
                    int pageNo = Integer.parseInt(parms[2]);

                    segment.writeInt(bbsid);
                    segment.writeShort((short)pageCount);
                    segment.writeInt(pageNo);
                }

                    break;
                case CONN_GET_BBS_CONTENT: {
                    int postId = Integer.parseInt(parms[0]);

                    segment.writeInt(postId);
                }

                    break;
                case CONN_BBS_POST: {
                    int bbsid = Integer.parseInt(parms[0]);
                    String title = parms[1];
                    String content = parms[2];

                    segment.writeInt(bbsid); //BBSID
                    segment.writeString(title); //标题
                    segment.writeString(content); //内容
                }

                    break;
                case CONN_TOUCH_NPC: {
                    int npcid = Integer.parseInt(parms[0]);

                    segment.writeInt(npcid); //NPCID
                    //#debug
                    World.log("Touch NPC : " + npcid, true);
                }

                    break;
                case CONN_LEARN_ABILITY: {
                    int skillId = Integer.parseInt(parms[0]);

                    segment.writeShort((short)skillId);
                }

                    break;
                case CONN_LEARN_SKILL: {
                    int skillId = Integer.parseInt(parms[0]);

                    segment.writeInt(skillId);
                }

                    break;
                case CONN_GET_DESC: {
                    byte subType = (byte)Integer.parseInt(parms[0]);
                    int skillId = Integer.parseInt(parms[1]);

                    segment.writeByte(subType);
                    segment.writeInt(skillId);

                    if(uiRequestTaskId == World.TASK_ID_VIEW_BATTLE_SKILL || (uiRequestTaskId == World.TASK_ID_PRODUCE_ITEM) || uiRequestTaskId == World.TASK_ID_PET_OPTION){
                        segment.writeByte((byte)2);
                    }else{
                        segment.writeByte((byte)1);
                    }
                }

                    break;
                case CONN_SKILL_LIST: {
                    byte skillTypeIndex = (byte)Integer.parseInt(parms[0]);
                    byte skillType = (byte)uiDataInt[0][skillTypeIndex];
                    segment.writeByte(skillType);
                }

                    break;
                case CONN_PRODUCT: {
                    int skillId = Integer.parseInt(parms[0]);
                    segment.writeInt(skillId);
                }

                    break;
                case CONN_CHATFAVORITE_DESC: {
                    byte index = (byte)Integer.parseInt(parms[0]);
                    segment.writeByte(index);
                    //#debug
                    System.out.println("get desc : " + index);
                }

                    break;
                case CONN_CHANGE_CHATFAVORITE: {
                    byte index = (byte)Integer.parseInt(parms[0]);
                    segment.writeByte(index);
                    //#debug
                    System.out.println("set index : " + index);
                }

                    break;
                case CONN_CHAT_MESSAGE: {
                    int id = Integer.parseInt(parms[0]);
                    String msg = parms[1];

                    if(msg.trim().length() == 0){
                        return;
                    }

                    segment.writeInt(0);
                    segment.writeString("");
                    segment.writeInt(id);
                    segment.writeString(msg);
                    segment.flush();
                    //#debug
                    System.out.println("chat : " + id + " , " + msg);
                }

                    break;
                case CONN_PK_REQUEST: {
                    int id = Integer.parseInt(parms[0]);
                    int money = Integer.parseInt(parms[1]);

                    segment.writeInt(0);
                    segment.writeString("");
                    segment.writeInt(id);
                    segment.writeShort(World.player.level);
                    segment.writeShort((short)money);
                    segment.writeInt(0);
                    segment.flush();
                    //#debug
                    System.out.println("PK Request : " + id + " , " + money);
                }

                    break;
                case CONN_TEAM_INVITE: {
                    int playerId = Integer.parseInt(parms[0]);

                    segment.writeInt(World.teamId);
                    segment.writeInt(0);
                    segment.writeString("");
                    segment.writeInt(playerId);
                    //#debug
                    System.out.println("Team Invite : " + playerId);
                }

                    break;
                case CONN_MAIL_GET_LIST: {
                    int pageSize = Integer.parseInt(parms[0]);
                    int pageNumber = Integer.parseInt(parms[1]);

                    segment.writeShort((short)pageSize);
                    segment.writeInt(pageNumber);
                }
                    break;
                case CONN_GET_MAIL_CONTENT:
                case CONN_DELETE_MAIL:
                case CONN_GET_ATTACHMENT: {
                    int mailId = Integer.parseInt(parms[0]);

                    segment.writeInt(mailId);
                }

                    break;
                case CONN_MAIL_POST: {
                    String name = parms[0];
                    String title = parms[1];
                    String content = parms[2];
                    int fee = Integer.parseInt(parms[3]);

                    segment.writeInt(0);
                    segment.writeString("");
                    segment.writeString(name);
                    segment.writeString(title);
                    segment.writeString(content);
                    segment.writeBytes(GameItem.getMailBytes(attachmentTypeItem));
                    segment.writeInt(fee);
                }

                    break;
                case CONN_LOOK_EQU: {
                    int playerId = Integer.parseInt(parms[0]);

                    segment.writeInt(playerId);
                    break;
                }
                case CONN_REQUEST_AUCTION_LIST: {
                    short areadId = (short)Integer.parseInt(parms[0]);
                    byte searchType = (byte)Integer.parseInt(parms[1]);
                    short pageSize = (short)Integer.parseInt(parms[2]);
                    int pageNo = Integer.parseInt(parms[3]);
                    String name = parms[4];
                    byte quality = (byte)Integer.parseInt(parms[5]);
                    int level = Integer.parseInt(parms[6]);

                    segment.writeShort(areadId);
                    segment.writeByte(searchType);
                    segment.writeShort(pageSize);
                    segment.writeInt(pageNo);
                    segment.writeString(name);
                    segment.writeByte(quality);
                    segment.writeInt(level);
                }

                    break;

                case CONN_AUCTION_ITEM: {
                    short areadId = (short)Integer.parseInt(parms[0]);
                    byte itemType = (byte)Integer.parseInt(parms[1]);
                    int itemId = Integer.parseInt(parms[2]);
                    int itemIdOrCount = Integer.parseInt(parms[3]);
                    int startPrice = Integer.parseInt(parms[4]);
                    int endPrice = Integer.parseInt(parms[5]);

                    segment.writeShort(areadId);
                    segment.writeByte(itemType);
                    segment.writeInt(itemId);
                    segment.writeInt(itemIdOrCount);
                    segment.writeInt(startPrice);
                    segment.writeInt(endPrice);

                    break;
                }

                case CONN_AUCTION_REQUEST_ITEM_DESC: {
                    int id = Integer.parseInt(parms[0]);
                    segment.writeInt(id);

                    break;
                }

                case CONN_AUCTION_PRICE: {
                    int id = Integer.parseInt(parms[0]);
                    int money = Integer.parseInt(parms[1]);
                    segment.writeInt(id);
                    segment.writeInt(money);

                    break;
                }

                case CONN_REQUEST_SHOP_ITEM_LIST: {
                    int shopId = Integer.parseInt(parms[0]);
                    byte requestType = (byte)Integer.parseInt(parms[1]);
                    short pageSize = (short)Integer.parseInt(parms[2]);
                    int pageNumber = Integer.parseInt(parms[3]);

                    segment.writeInt(shopId);
                    segment.writeByte(requestType);
                    segment.writeShort(pageSize);
                    segment.writeInt(pageNumber);
                }

                    break;
                case CONN_SHOP_ADD_ITEM: {
                    int shopId = Integer.parseInt(parms[0]);
                    byte requestType = (byte)Integer.parseInt(parms[1]);
                    int itemId = Integer.parseInt(parms[2]);
                    int countOrId = Integer.parseInt(parms[3]);
                    int parm1 = Integer.parseInt(parms[4]);
                    int parm2 = Integer.parseInt(parms[5]);

                    segment.writeInt(shopId);
                    segment.writeByte(requestType);
                    segment.writeInt(itemId);
                    segment.writeInt(countOrId);
                    segment.writeInt(parm1);
                    segment.writeInt(parm2);
                }

                    break;
                case CONN_SHOP_REMOVE_ITEM: {
                    int shopId = Integer.parseInt(parms[0]);
                    byte requestType = (byte)Integer.parseInt(parms[1]);
                    int itemId = Integer.parseInt(parms[2]);
                    int countOrId = Integer.parseInt(parms[3]);

                    segment.writeInt(shopId);
                    segment.writeByte(requestType);
                    segment.writeInt(itemId);
                    segment.writeInt(countOrId);
                }

                    break;
                case CONN_SHOP_MONEY_CHANGE: {
                    int shopId = Integer.parseInt(parms[0]);
                    int money = Integer.parseInt(parms[1]);

                    segment.writeInt(shopId);
                    segment.writeInt(money);
                }

                    break;
                case CONN_SHOP_CHANGE: {
                    int shopId = Integer.parseInt(parms[0]);
                    byte changeType = (byte)Integer.parseInt(parms[1]);

                    segment.writeInt(shopId);
                    segment.writeByte(changeType);
                }

                    break;
                case CONN_REQUEST_BUY_MATERIAL_LIST: {
                    short areaId = (short)Integer.parseInt(parms[0]);
                    byte serachType = (byte)Integer.parseInt(parms[1]);
                    String name = parms[2];
                    short pageSize = (short)Integer.parseInt(parms[3]);
                    int pageNo = Integer.parseInt(parms[4]);

                    segment.writeShort(areaId);
                    segment.writeByte(serachType);
                    segment.writeString(name);
                    segment.writeShort(pageSize);
                    segment.writeInt(pageNo);
                }

                    break;
                case CONN_SELL_MATERIAL: {
                    int id = Integer.parseInt(parms[0]);
                    short sellCount = (short)Integer.parseInt(parms[1]);

                    segment.writeInt(id);
                    segment.writeShort(sellCount);
                }

                    break;
                case CONN_REQUEST_OEM_LIST: {
                    short areaId = (short)Integer.parseInt(parms[0]);
                    byte serachType = (byte)Integer.parseInt(parms[1]);
                    String name = parms[2];
                    short pageSize = (short)Integer.parseInt(parms[3]);
                    int pageNo = Integer.parseInt(parms[4]);
                    boolean canMake = Integer.parseInt(parms[5]) == 1;

                    segment.writeShort(areaId);
                    segment.writeByte(serachType);
                    segment.writeString(name);
                    segment.writeShort(pageSize);
                    segment.writeInt(pageNo);
                    segment.writeBoolean(canMake);
                }

                    break;
                case CONN_OEM: {
                    int id = Integer.parseInt(parms[0]);
                    int score = Integer.parseInt(parms[1]);

                    segment.writeInt(id);
                    segment.writeInt(score);
                }

                    break;
                case CONN_STORE_TRADE: {
                    byte storeType = (byte)Integer.parseInt(parms[0]);
                    int npcId = Integer.parseInt(parms[1]);
                    byte tradeType = (byte)Integer.parseInt(parms[2]);
                    int itemId = Integer.parseInt(parms[3]);
                    int countOrId = Integer.parseInt(parms[4]);

                    segment.writeByte(storeType);
                    segment.writeInt(npcId);
                    segment.writeByte(tradeType);
                    segment.writeInt(itemId);
                    segment.writeInt(countOrId);
                }

                    break;
                case CONN_ADD_FRIEND: {
                    byte opType = (byte)Integer.parseInt(parms[1]);
                    byte w = (byte)Integer.parseInt(parms[2]);
                    segment.writeByte(w);
                    segment.writeString(parms[0]);
                    segment.writeByte(opType);
                }
                    break;
                case CONN_REQUEST_TASK_DESC: {
                    short taskId = (short)Integer.parseInt(parms[0]);
                    segment.writeShort(taskId);
                }

                    break;
                case CONN_ADD_POINT: {
                    byte skillType = (byte)Integer.parseInt(parms[0]);
                    segment.writeByte(skillType);
                }

                    break;
                case CONN_REQUEST_ITEM_LINK: {
                    int skillId = Integer.parseInt(parms[0]);
                    segment.writeInt(skillId);
                }

                    break;
                case CONN_REQUEST_TONG_MEMBERS: {
                    int pageNo = Integer.parseInt(parms[0]);
                    byte listType = (byte)Integer.parseInt(parms[1]);

                    segment.writeInt(pageNo);
                    segment.writeByte(listType);
                }

                    break;
                case CONN_TONG_GRANT: {
                    String name = parms[0];
                    byte doType = (byte)Integer.parseInt(parms[1]);

                    segment.writeString(name);
                    segment.writeByte(doType);
                }

                    break;
                case CONN_TONG_MODIFY_TITLE: {
                    String name = parms[0];
                    String title = parms[1];

                    segment.writeString(name);
                    segment.writeString(title);
                }

                    break;
                case CONN_TASK_ABANDON: {
                    short taskId = (short)Integer.parseInt(parms[0]);
                    segment.writeShort(taskId);
                }

                    break;
                case CONN_USE_PET: {
                    int petId = Integer.parseInt(parms[0]);
                    byte useType = (byte)Integer.parseInt(parms[1]);

                    segment.writeInt(petId);
                    segment.writeByte(useType);
                }

                    break;
                case CONN_PET_FEED: {
                    int petId = Integer.parseInt(parms[0]);
                    int itemId = Integer.parseInt(parms[1]);

                    segment.writeInt(petId);
                    segment.writeInt(itemId);
                }

                    break;
                case CONN_ADD_PET_POINT: {
                    int petId = Integer.parseInt(parms[0]);
                    byte[] attr = new byte[4];

                    attr[0] = (byte)Integer.parseInt(parms[1]);
                    attr[1] = (byte)Integer.parseInt(parms[2]);
                    attr[2] = (byte)Integer.parseInt(parms[3]);
                    attr[3] = (byte)Integer.parseInt(parms[4]);

                    segment.writeInt(petId);
                    segment.writeBytes(attr);
                }

                    break;
                case CONN_BUY_PET_POINT: {
                    int petId = Integer.parseInt(parms[0]);
                    int itemId = Integer.parseInt(parms[1]);
                    int id = Integer.parseInt(parms[2]);

                    segment.writeInt(itemId);
                    segment.writeInt(id);
                    segment.writeInt(petId);
                }

                    break;
                case CONN_REQUEST_FRIEND_LIST:
                    break;
                case CONN_GENERIC_CONTENT: {
                    int listId = Integer.parseInt(parms[0]);
                    short listIndex = (short)Integer.parseInt(parms[1]);

                    segment.writeInt(listId);
                    segment.writeShort(listIndex);
                }

                    break;
                case CONN_SNEAK_ATTACK: {
                    int id = Integer.parseInt(parms[0]);

                    segment.writeInt(id);
                }

                    break;
                case CONN_ISHOP_LIST:
                    break;
                case CONN_ISHOP_TRADE: {
                    int itemId = Integer.parseInt(parms[0]);
                    int itemCount = Integer.parseInt(parms[1]);

                    segment.writeInt(itemId);
                    segment.writeInt(itemCount);
                }

                    break;

            }

            segment.flush();
            sendRequest(segment);

            if(World.gameState != null){
                World.gameState.serial = segment.serial;
            }

            Integer iSerial = new Integer(segment.serial);

            uiDataDataError = false;
            uiDataNetRequest.put(iSerial, iSerial);
        }catch(Exception e){
            //#debug
            e.printStackTrace();
        }
    }

    public GameState(byte type){
        this.type = type;

        switch(type){
        //#if (Revision == QQ) || (Revision == SOHU) || (Revision == JIANGSUN)
            case STATE_SPLASH:
                try{

                    World.battleBout = 2;//复用，记录是否执行过World.init()方法，避免重复调用

                    subState = 0;
                    _image = Image.createImage(splash_name[subState]);
                    
                    _time = System.currentTimeMillis();
                }catch(IOException ex){
                }

                break;
                //#endif
            case STATE_GAMEMENU:
                try{
                	//#if Revision == PIP ||( Revision == DOWNJOY) || (Revision == CMCC) || (Revision == JIANGSUNCMCC)
                	World.battleBout = 2;//复用，记录是否执行过World.init()方法，避免重复调用
                	//#endif
                    _image = Image.createImage("/gamemenu.png");

                  //#if ((Directory == Nokia403) || (Directory == SE-K300) || (Directory == SE-K500) || (Directory == DOPOD-585))
                    _movingArrow = World.getImageSetFromLocal("movingarrow.p");
                  //#else
                    _movingMenu = World.getImageSetFromLocal("movingmenu.p");
                   //#endif
                    subState = SS_CHOICE;
                    _delState = SS_ACTOR_SELECT_DELETE_CONFIRM;

                  //#if !((Directory == Nokia403) || (Directory == SE-K300) || (Directory == SE-K500) || (Directory == DOPOD-585))
                    blankHeight = _image.getHeight() >> 2;
                    //图片右角
                    //blankStartX = World.viewWidth -(World.viewWidth - _image.getWidth())/2 - CHAR_WIDTH - font.stringWidth(GAME_MENU[0])/2;
                    //blankStartY = blankHeight + CHAR_HEIGHT;
                    //图片左角

//        			blankStartY = blankHeight;
//        			menuCycleInstance = CHAR_HEIGHT; 
//                    if(World.viewHeight == 320){
//                    	 //#if !(Directory == NK-BigScreen)
//                    	blankStartY = blankStartY +CHAR_HEIGHT;
//                    	menuCycleInstance =CHAR_HEIGHT *3/2;
//                    	//#endif
//                    }else if(World.viewHeight == 208){
//                    	blankStartY = blankStartY - CHAR_HEIGHT;
//                    	blankStartX = blankStartX - CHAR_WIDTH/2;
//                    }
//                    startIndex = 0;
//                    gameMenuRowNum = 5;
//                    if (gameMenuRowNum > GAME_MENU.length) {
//                        gameMenuRowNum = GAME_MENU.length;
//                    }
//                    _index = 2;
        			menuCycleInstance = 11;
        			//#if M-Name == DOPOD_S700
        			//# menuCycleStart = new int[]{-100,75,   -100,75,   -30,85,   20,115,  25,150,    20,185,  -30,215,   -100,225,  -100,225};
        			//#elif (TouchScreen == true) || (Directory == NK-BigScreen) || (Directory == NK-Nokia403Big) || (Directory == MT-General) || (M-Name == SAM_L288) || (Directory == SE-S700) || (Directory == ZTE_U860)
        			menuCycleStart = new int[]{-100,100,   -100,100,   -15,110,   20,140,  25,190,    20,235,  -15,265,   -100,275,  -100,275};
        			//#elif Directory == NK-E61
        			//# menuCycleStart = new int[]{-100,75,   -100,75,   -30,85,   20,115,  25,150,    20,185,  -30,215,   -100,225,  -100,225};
        			//#elif (M-Name == NK_5500) || (Directory == Midp2-General) || (Directory == NK-60-2) || (Directory == NK-6681) || (Directory == NK-NGage) || (Directory == NK3250) || (Directory == SE-K700)
        			//# menuCycleStart = new int[]{-100,55,   -100,55,   -35,60,   20,85,  25,115,    20,145,  -30,170,   -100,175,  -100,175};
        			//# menuCycleInstance = 9;
        			//#else 
        			//# menuCycleStart = new int[]{-100,100,   -100,100,   -15,110,   20,140,  25,190,    20,235,  -15,265,   -100,275,  -100,275};
        			//#endif
        			if(World.viewWidth<240){
        				leftIsntance = 4;
        			}
                    startIndex = 0;
                    _index =4;
                  gameMenuRowNum = 9;
                  if (gameMenuRowNum > GAME_MENU.length) {
                      gameMenuRowNum = GAME_MENU.length;
                  }
                   //#else
                  //# blankHeight = _image.getHeight() >> 2;
                  //# blankStartX =  CHAR_WIDTH + font.stringWidth(GAME_MENU[0])/2;
                  //# blankStartY = blankHeight + CHAR_HEIGHT;

                  //# if(World.viewWidth > 176){
                  //#  blankStartX -= CHAR_WIDTH;
                  //#   blankStartY += CHAR_HEIGHT;
                  //# }else if(World.viewWidth < 176){
                  //#  if(World.viewHeight <= 128){
                  //# blankStartY += CHAR_HEIGHT / 2;
                  //# }
                  //# }

                  //#  startIndex = 0;
                  //#  gameMenuRowNum = (_image.getHeight() - blankHeight) / CHAR_HEIGHT - 2;
                  //#if (Directory == NK-BigScreen) || (Directory == SE-S700) || (Directory == SE-K500) || (Directory == SE-K300) || (Directory == Nokia403)
                  //# gameMenuRowNum = gameMenuRowNum -1;
                  //#endif
                  //# if (gameMenuRowNum > GAME_MENU.length) {
                  //# gameMenuRowNum = GAME_MENU.length;
                  //# }
                  //# _index = 0;
                    //#endif
                    createGameMenu();
                    GameState.repaintNextTime = true;
                }catch(IOException ex){
                    //#debug
                    ex.printStackTrace();
                }
                break;
            case STATE_OPENURL:
                _message = "";
                break;
            case STATE_LOADING:
                _message = "正在载入数据...";
                if(subState >= SS_LOADING_BATTLE){
                    serial = 0;
                }

                rndX = -1;
                rndY = -1;
                dir = Sprite.LEFT;
                frame = 0;

                break;
            case STATE_MAINMENU:
                _index = 0;
                _subIndex = 0;
                paintBackGroud = true;

                menuXY = new short[4][2];

                if(menuLocation == null){
                    menuLocation = new short[4][2];
                    menuStartLocation = new short[4][2];

                    int middx = World.viewWidth / 2;
                    int middy = World.viewHeight / 2;

                    /*menu四个按钮的终点位置*/
                    menuLocation[0][0] = (short)(middx - 15/* 按钮高度(28)*0.5 */);
                    menuLocation[0][1] = (short)(middy - 52/* 按钮宽度(28)*1.5 - 14 */);
                    menuLocation[1][0] = (short)(middx + 8);
                    menuLocation[1][1] = (short)(middy - 15);
                    menuLocation[2][0] = (short)(middx - 15);
                    menuLocation[2][1] = (short)(middy + 8);
                    menuLocation[3][0] = (short)(middx - 52);
                    menuLocation[3][1] = (short)(middy - 15);

                    menuStartLocation[0][0] = (short)(menuLocation[0][0] - LONGLINE);
                    menuStartLocation[0][1] = (short)(menuLocation[0][1] + SHORTLINE);

                    menuStartLocation[1][0] = (short)(menuLocation[1][0] - SHORTLINE);
                    menuStartLocation[1][1] = (short)(menuLocation[1][1] - LONGLINE);

                    menuStartLocation[2][0] = (short)(menuLocation[2][0] + LONGLINE);
                    menuStartLocation[2][1] = (short)(menuLocation[2][1] - SHORTLINE);

                    menuStartLocation[3][0] = (short)(menuLocation[3][0] + SHORTLINE);
                    menuStartLocation[3][1] = (short)(menuLocation[3][1] + LONGLINE);

                }

                //                World.instance.alphaBackBuffer();
                repaintNextTime = true;
                resetMenuXY(menuStartLocation);

                break;
            case STATE_EDITEQUIPS:
                paintBackGroud = true;
                World.player.backupAttributes(0);
                World.player.backupEquips();

                attrShowBegin = 4;
                attrSelected = 4;

                break;
            case STATE_EDITATTR:
                attrSelected = 0;
                attrShowBegin = 0;
            case STATE_EDITITEM:
                World.player.backupAttributes(1);
                paintBackGroud = true;
                World.battleBout = 1;

                break;
            case STATE_CHATLIST:
                initChatList(0);
                _scroll = 0;
                _index = 0;
                paintBackGroud = true;

                break;
            case STATE_RELOGIN:
                gameIsOk = false;
                reloginTimes++;

                if(reloginTimes > 3){
                    _message = "重新连接失败";
                }else{
                    _message = "与服务器失去联系，正在尝试重连......第" + GameState.reloginTimes + "次";
                    reloginStarted = false;
                    paintBackGroud = true;
                }

                repaintNextTime = true;

                GameState.clearTaskUI();

                for(int i = 0; i < World.events.size(); i++){
                    GameEvent e = (GameEvent)World.events.elementAt(i);
                    GameEvent.eventRemove(e);
                }

                World.events.removeAllElements();

                break;
        }
    }

    public static void exitToGameMenu(String s, boolean backToActorList){
        if(World.gameState != null && World.gameState.type == STATE_GAMEMENU){
            if(s == null){
                s = "您身上如果带着浴场券，可以到浴场去离线升级...";
            }

            World.gameState._message = s;

            return;
        }

        GameState.logouting = true;
        gameIsOk = false;
        closeConnection();

        try{
            Thread.sleep(500);
        }catch(InterruptedException ex){
        }

        World.release();

        GameState state = new GameState(STATE_GAMEMENU);

        if(s != null){
            state.subState = SS_MESSAGE;
            state._message = s;
        }

        if(backToActorList){
            state.subState = SS_LOGINING;
            state._message = "正在连接...";
        }

        World.setGameState(state);
        World.clearKeyStates();

        reloginTimes = 0;
    }

    public void cycle(){
        if(waitType == WAIT_GAME_PRESS){
            if(World.isKeyPressed(World.FIRE_PRESSED, true)){
                if(needExit)
                    World.setGameState(null);
                else{
                    waitType = WAIT_NONE;
                }
            }
            return;
        }

        switch(type){
        //#if (Revision == QQ) || (Revision == SOHU) || (Revision == JIANGSUN)
            case STATE_SPLASH:
                cycleSplash();
               
                break;
                //#endif
            case STATE_GAMEMENU:
                cycleGameMenu();

                break;
            case STATE_LOADING:
                cycleLoading();

                break;
            case STATE_MAINMENU:
                cycleMainMenu();

                break;
            case STATE_TASKUI:
                cycleTaskUI();

                break;
            case STATE_EDITEQUIPS:
                cycleEditEquips();

                break;
            case STATE_EDITATTR:
                cycleEditAttr();

                break;
            case STATE_EDITITEM:
                cycleEditItem();

                break;
            case STATE_CHATLIST:
                cycleChatList();

                break;
            case STATE_RELOGIN:
                cycleRelogin();

                break;
            case STATE_GETLISTFAIL:
                if (World.isKeyPressed(World.FIRE_PRESSED, true) || 
                        World.isKeyPressed(World.SOFT_FIRST_PRESSED, true) || 
                        World.isKeyPressed(World.SOFT_LAST_PRESSED, true)) {
                    iTimesMIDlet.instance.exitGame();
                } else if (World.isKeyPressed(World.UP_PRESSED, true)) {
                    if (_scroll > 0) {
                        _scroll -= CHAR_HEIGHT;
                    }
                } else if (World.isKeyPressed(World.DOWN_PRESSED, true)) {
                    if (_scroll < this.infoStrings.length * CHAR_HEIGHT + LINE_HEIGHT + 4 - World.viewHeight) {
                        _scroll += CHAR_HEIGHT;
                    }
                }
                break;
        }
    }

    private void cycleRelogin(){
        if(!reloginStarted){
            reloginStarted = true;

            if(reloginTimes > 3){
                exitToGameMenu("重新连接失败", false);

                return;
            }

            try {
                closeConnection();
                //#if Revision == CMCC || (Revision == JIANGSUNCMCC) 
                //# createConnection();
                //# World.sendRequest(CONN_RELOGIN, new Object[]{
                        //# name, password, actorName, getModel(), getClientVersion(), new Byte((byte)0),
                        //# cmccUserID, cmccKey
                //# }, false);
                //#elif Revision == QQ
                //# createConnection();
                //# World.sendRequest(CONN_RELOGIN, new Object[]{
                //# name, password, actorName, getModel(), getClientVersion(), new Byte((byte)0),
                //# iTimesMIDlet.getQQId(), iTimesMIDlet.getSID()
                //# }, false);
                //#else
                createConnection();
                World.sendRequest(CONN_RELOGIN, new Object[]{
                    name, password, actorName, getModel(), getClientVersion(), new Byte((byte)0), cmccUserID, cmccKey
                }, false);
                //#endif
                //GameState.requestRelogin();
                ASyncRequestThread.init(GameState.connection);

                //#debug
                System.out.println("try reconnect..........." + GameState.reloginTimes);
            }catch(Exception e){
                //#debug
                e.printStackTrace();

                if(GameState.gameIsOk){
                    GameState state = new GameState(GameState.STATE_RELOGIN);

                    World.setGameState(state);
                }else{
                    GameState.exitToGameMenu("与服务器失去联系", false);
                }
            }finally{
                try{
                    Thread.sleep(50);
                }catch(Exception e){
                }
            }
        }
    }
  //#if (Revision == QQ) || (Revision == SOHU) || (Revision == JIANGSUN)
    private void cycleSplash(){
        long currTime = System.currentTimeMillis();

        World.battleBout--;
        if(World.battleBout == 0){
            World.init();
            //#if (Directory == MT-General) || (Directory == Midp2-General) || (Directory == SE-K500) || (Directory == SE-K300) || (Directory == Nokia403) || (MIDP2Common == true) || (Directory == ClientTouch-E680) || (Directory == ClientTouch--Midp2-General) || (Directory == ClientTouch-SE-General) || (Directory == ClientTouch-Nokia5800)
            initLocation();
            //#endif
        }

        if((currTime - _time) >= splash_time[subState] * 1000){
            _time = currTime;
            subState++;

            if(subState >= splash_time.length){
                GameState state = new GameState(GameState.STATE_GAMEMENU);
                World.setGameState(state);
            }else{
                try{
                    _image = Image.createImage(splash_name[subState]);
                    System.gc();
                }

                catch(IOException ex){
                }
            }
        }
    }
    //#endif

//#if Directory != NK-NGage
    private void beginUpdate() {
        try{
            String tmp = wapUrl;
            
            //#if Revision == CMCC || (Revision == JIANGSUNCMCC) 
            if(CMCCExitCanvas.updateUrl1 != null && CMCCExitCanvas.updateUrl1.trim().length() > 0){
                tmp = CMCCExitCanvas.updateUrl1;
            }else{
                tmp = CMCCExitCanvas.updateUrl;
            }
            //#else
            tmp += "game=itimes&model=" + getModel() + "&channel=" + channelCode+"&version=" + versionString ;
            //#endif
            
            iTimesMIDlet.instance.platformRequest(tmp);
            Thread.sleep(500);
            closeConnection();
            iTimesMIDlet.instance.exitGame();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
//#endif

    private void cycleGameMenu(){
    	  World.battleBout--;
          if(World.battleBout == 0){
              World.init();
              //#if (Directory == MT-General) || (Directory == Midp2-General) || (Directory == SE-K500) || (Directory == SE-K300) || (Directory == Nokia403) || (MIDP2Common == true) || (Directory == ClientTouch-E680) || (Directory == ClientTouch--Midp2-General) || (Directory == ClientTouch-SE-General) || (Directory == ClientTouch-Nokia5800)
              initLocation();
              //#endif
          }
//#if Directory != NK-NGage
    	if (needUpdate && World.isAnyKeyPressed()) {
    		beginUpdate();
    		return;
    	}
//#endif
        switch(subState){
            case SS_CHOICE:
              //#if TouchScreen == true
                boolean directChoose = false;
                boolean upChoose = false;
                boolean downChoose = false;
                int focusButton = StaticUtils .getDragOverButton();
                if(focusButton != -1 && focusButton>=2000){
                	
                	_index = focusButton-2000;
                	createGameMenu();
                }
                int pressedButton = StaticUtils.getPressedButton();
                if(-1 != pressedButton && pressedButton>=2000){
                	if(pressedButton >=3000){
                		if(pressedButton - 3000 ==0){
                			upChoose = true;
                		}else{
                			downChoose = true;
                		}
                	}else{
	                	_index = pressedButton - 2000;
	                	if(_index >= GAME_MENU.length){
	                		_index = _index - GAME_MENU.length;
	                	}else if(_index < 0){
	                			_index =  _index+GAME_MENU.length;
	                	}
	                	createGameMenu();
	                	directChoose = true;
                	}
                }
                //#endif 
              
              
              //#if TouchScreen == true
                if(World.isKeyPressed(World.FIRE_PRESSED, true) || World.isKeyPressed(World.SOFT_FIRST_PRESSED, true) || directChoose){
                	//#else
                	//# if(World.isKeyPressed(World.FIRE_PRESSED, true) || World.isKeyPressed(World.SOFT_FIRST_PRESSED, true)){
                	//#endif 
                    String cmd = GAME_MENU[_index];
                    if(cmd.equals(GAMEMENU_FASTREG) || cmd.equals(GAMEMENU_FASTLOGIN)){
                        subState = SS_GETSERVERLIST;
                        _message = "正在读取服务器列表...";
                        nextState = SS_FAST_REG;
                    }else if(cmd.equals(GAMEMENU_LOGIN)){
                        subState = SS_GETSERVERLIST;
                        _message = "正在读取服务器列表...";

                        //#if Revision == QQ && TestVersion == false
                        //# nextState = SS_LOGINING;
                        //#else
                        nextState = SS_LOGIN;
                        //#endif
                        
                        //subState = SS_LOGIN;
                        //#if EnableReg == TRUE
                    }else if(cmd.equals(GAMEMENU_REG)){
                        subState = SS_GETSERVERLIST;
                        _message = "正在读取服务器列表...";
                        nextState = SS_REGISTER;
                        //subState = SS_REGISTER;
                        //#endif
                    }
                    //#if Directory != NK-NGage
                        //#if Revision == QQ
                        //# else if(cmd.equals(GAMEMENU_UPDATE)){
                        //#     try{
                        //#         iTimesMIDlet.instance.platformRequest(qqUpdateWeb + "version=" + versionString + "&model=" + getModel());
                        //#         Thread.sleep(500);
                        //#         closeConnection();
                        //#         iTimesMIDlet.instance.exitGame();
                        //#     }catch(Exception e){
                        //#         e.printStackTrace();
                        //#     }
                        //#     break;
                        //# }else if(cmd.equals(GAMEMENU_PORTAL)){
                        //#     try{
                        //#         iTimesMIDlet.instance.platformRequest(qqOfficalWeb + "version=" + versionString + "&model=" + getModel());
                        //#         Thread.sleep(500);
                        //#         closeConnection();
                        //#         iTimesMIDlet.instance.exitGame();
                        //#     }catch(Exception e){
                        //#         e.printStackTrace();
                        //#     }
                        //#     break;
                        //# }else if(cmd.equals(GAMEMENU_QQGAME)){
                        //#     try{
                        //#         iTimesMIDlet.instance.platformRequest("http://g.qq.com/g/s?aid=g_index&cid=kjavagame");
                        //#         Thread.sleep(500);
                        //#         closeConnection();
                        //#         iTimesMIDlet.instance.exitGame();
                        //#     }catch(Exception e){
                        //#         e.printStackTrace();
                        //#     }
                        //#     break;
                        //# }
                        //#else
                    else if(cmd.equals(GAMEMENU_UPDATE)){
                    	beginUpdate();
                        break;
                    }else if(cmd.equals(GAMEMENU_PORTAL)){
                        try{
                            iTimesMIDlet.instance.platformRequest("http://wap.pipfit.cn/");
                            Thread.sleep(500);
                            closeConnection();
                            iTimesMIDlet.instance.exitGame();
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        break;
                    }
                        //#if Revision == SOHU
                    else if(cmd.equals(GAMEMENU_SOHU)){
                        try{
                            iTimesMIDlet.instance.platformRequest("http://wap.sohu.com/game?ch=zs_001");
                            Thread.sleep(500);
                            closeConnection();
                            iTimesMIDlet.instance.exitGame();
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        break;
                    }
                        //#endif
                        //#endif
                    //#endif
//                    else if(cmd.equals(GAMEMENU_HELP)){
//                        subState = SS_HELP;
//                        infoStrings = World.splitString(helpString, World.viewWidth - SCREEN_MARGIN * 2, font);
//                                   }
                    else if(cmd.equals(GAMEMENU_ABOUT)){
                        subState = SS_ABOUT;
                        infoStrings = World.splitString(aboutString, World.viewWidth - SCREEN_MARGIN * 2, font);
                    }else if(cmd.equals(GAMEMENU_EXIT)){
                    	//#if Revision == CMCC || (Revision == JIANGSUNCMCC) || (Revision == PIP) || (Revision == SOHU) || (Revision == JIANGSUN)
                    	World.RecordPreousDisplay(new CMCCExitCanvas());
                    	//#else
                        //# closeConnection();
                        //# iTimesMIDlet.instance.exitGame();
                        //#endif
                    }
                    //#if Revision == CMCC || (Revision == JIANGSUNCMCC) 
                    else if(cmd.equals(GAMEMENU_POINT)){
                        GameState.cmccPoint = new CMCCPointCanvas();
                        World.RecordPreousDisplay(GameState.cmccPoint);
                    //#if Directory != NK-NGage
                    } else if (cmd.equals(GAMEMENU_MOREGAME)) {
                    	try{
                    	    if(CMCCExitCanvas.updateUrl1 != null && CMCCExitCanvas.updateUrl1.trim().length() > 0){
                                iTimesMIDlet.instance.platformRequest(CMCCExitCanvas.updateUrl1);
                            }else{
                                iTimesMIDlet.instance.platformRequest(CMCCExitCanvas.updateUrl);
                            }
                            Thread.sleep(500);
                            closeConnection();
                            iTimesMIDlet.instance.exitGame();
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        break;
                    //#endif
                    }
                    //#endif
              
                   //#if TouchScreen == true
                }else if(World.isKeyPressed(World.UP_PRESSED, true) || upChoose){
                	//#else
                	//# }else if(World.isKeyPressed(World.UP_PRESSED, true)){
                	//#endif 
                //}else if(World.isKeyPressed(World.UP_PRESSED, true)){
                	 //#if ((Directory == Nokia403) || (Directory == SE-K300) || (Directory == SE-K500) || (Directory == DOPOD-585))
                	//# if (_index > 0) {
                	//#  _index--;
                	//# }
                	//# if (startIndex > 0 && (_index-startIndex) < ((gameMenuRowNum-1)>>1)) {
                	//# startIndex--;
                	//# }
                	//# createGameMenu();
                	//#else

                       if (_index > 0) {
                           _index--;
                       }else{
                       	_index = GAME_MENU.length - 1;
                       }
                       if (startIndex > 0 ) {
                           startIndex--;
                       }else{
                       	startIndex = GAME_MENU.length - 1;
                       }
                      upCycle = false;
                      gameMenuCycle = true;
                      //#endif
                	
                
                 //#if TouchScreen == true
                }else if(World.isKeyPressed(World.DOWN_PRESSED, true) || downChoose){
                	//#else
                	//# }else if(World.isKeyPressed(World.DOWN_PRESSED, true)){
                	//#endif  
                //}else if(World.isKeyPressed(World.DOWN_PRESSED, true)){

              //#if ((Directory == Nokia403) || (Directory == SE-K300) || (Directory == SE-K500) || (Directory == DOPOD-585))
                	//# if (_index < GAME_MENU.length - 1) {
                	//# _index++;
                	//# }
                	//# if (startIndex + gameMenuRowNum < GAME_MENU.length
                	//#          && (_index-startIndex) > ((gameMenuRowNum-1)>>1)) {
                	//# startIndex++;
                	//# }
                	//# createGameMenu();
              //#else
                	
             	   if (_index < GAME_MENU.length - 1) {
                       _index++;
                   }else{
                  	_index = 0;
                   }
                   
                   if (startIndex < GAME_MENU.length - 1) {
                   	startIndex++;
                   }else{
                  	startIndex = 0;
                   }
                  
                    upCycle = true;
                    gameMenuCycle = true;
                  //#endif
                }
                //#if !((Directory == Nokia403) || (Directory == SE-K300) || (Directory == SE-K500) || (Directory == DOPOD-585))
                if(insteadOldMenu){
                	createGameMenu();
                	insteadOldMenu = false;
                }
                //#endif
                break;
            case SS_LOGIN:
                if(_form == null){
                    if(name == null || password == null){
                        byte[] bytes = World.getData(World.RMS_DATA, (byte)1);
                        byte[] bytes1 = World.getData(World.RMS_DATA, (byte)2);
                        name = "";
                        password = "";

                        if(bytes != null && bytes.length != 0){
                            name = World.bytesToString(bytes);

                            if(bytes1 != null && bytes.length != 0){
                                password = World.bytesToString(bytes1);
                            }
                        }
                    }

                    _form = new Form("登录");
                    _formTitle = "登录";
                    _form.append(new TextField("用户名:", name, 20, TextField.ANY));
                    _form.append(new TextField("密码：", password, 20, TextField.PASSWORD));
                    _form.addCommand(new Command("登录", Command.ITEM, 0));
                    _form.addCommand(new Command("返回", Command.BACK, 0));
                    _form.setCommandListener(this);
                    World.RecordPreousDisplay(_form);
                }

                break;
            case SS_FAST_REG:
            case SS_FAST_LOGIN:
            case SS_FAST_GET_ACTORLIST:
            case SS_FAST_PLAYER_LOGIN:
                fastWay = true;

                if(subState == SS_FAST_REG && !canFastChecked){
                    String tmpStr[] = new String[3];

                    for(int i = 0; i < tmpStr.length; i++){
                        byte[] tmp = World.getData(World.RMS_DATA, (byte)(i + 1));

                        if(tmp != null){
                            tmpStr[i] = World.bytesToString(tmp);
                        }else{
                            tmpStr[i] = "";
                        }
                    }

                    name = tmpStr[0];
                    password = tmpStr[1];
                    actorName = tmpStr[2];

                    if(name.trim().length() > 0){
                        subState = SS_FAST_LOGIN;
                    }

                    canFastChecked = true;
                }

                if(thread == null){
                    thread = new Thread(this);
                    thread.start();
                }else{
                    if(World.isKeyPressed(World.SOFT_LAST_PRESSED, true)){
                        closeConnection();

                        _form = null;
                        thread = null;

                        subState = SS_CHOICE;
                        fastWay = false;
                        canFastChecked = false;
                    }
                }

                break;
            case SS_LOGINING:
                if(thread == null){
                    thread = new Thread(this);
                    thread.start();
                }else{
                    if(World.isKeyPressed(World.SOFT_LAST_PRESSED, true)){
                        thread = null;
                        
                        //#if Revision == QQ && TestVersion == false
                        //# subState = SS_CHOICE;
                        //#else
                        subState = SS_LOGIN;
                        //#endif

                        closeConnection();

                        _form = null;
                        thread = null;
                    }
                }

                break;
            case SS_GETSERVERLIST:
                if(thread == null){
                    _subIndex = 0;
                    serverPosSeted = false;
                    thread = new Thread(this);
                    thread.start();
                }else{
                    if(World.isKeyPressed(World.SOFT_LAST_PRESSED, true)){
                        thread = null;
                        subState = SS_CHOICE;
                    }
                }
                break;
            case SS_SERVERLIST:
                {
                	//#if TouchScreen == true
                    directChoose = false;
                    focusButton = StaticUtils .getDragOverButton();
                    if(focusButton != -1 && focusButton>=2000){
                    	_subIndex = focusButton-2000;
                    }
                    pressedButton = StaticUtils.getPressedButton();
                    if(-1 != pressedButton && pressedButton>=2000){
                    	_subIndex = pressedButton - 2000;
                    	directChoose = true;
                    }
                    //#endif 
                    if(!serverPosSeted){
                       /* for(int i = 0; i < serverGroups.length; i++){
                            if(serverName != null && serverGroups[i] != null && serverName.equals(serverGroups[i])){
                                _subIndex = i;
                                
                                break;
                            }
                        }
                        */
                    	_subIndex = 0;
                        serverPosSeted = true;
                    }else if(World.isKeyPressed(World.UP_PRESSED, true)){
                        _subIndex--;
                        if(_subIndex < 0){
                            _subIndex = serverGroups.length - 1;
                        }
                    }else if(World.isKeyPressed(World.DOWN_PRESSED, true)){
                        _subIndex++;
                        if(_subIndex >= serverGroups.length){
                            _subIndex = 0;
                        }
                      //#if TouchScreen == true
                    }else if(World.isKeyPressed(World.SOFT_FIRST_PRESSED, true) || World.isKeyPressed(World.FIRE_PRESSED, true) || directChoose){
                    	//#else
                    	//# }else if(World.isKeyPressed(World.SOFT_FIRST_PRESSED, true) || World.isKeyPressed(World.FIRE_PRESSED, true)){
                    	//#endif 

                        URLs = serverURLs[_subIndex];
                        if( (0==_subIndex) && serverName != null && serverName.trim().length() > 0 && (serverGroups[0].indexOf(tempServerName)>=0 && serverGroups[0].indexOf("上次登录:")>=0) ){
                        	serverName=tempServerName;
                        }else{
                        	serverName = serverGroups[_subIndex];
                        }
                        url = null;
                        subState = nextState;

                        _message = "正在连接...";
                    }else if(World.isKeyPressed(World.SOFT_LAST_PRESSED, true)){
                        subState = SS_CHOICE;
                        _subIndex = 0;
                    }
                }
                break;
            case SS_REGISTER:
                if(_form == null){
                    _form = new Form("注册");
                    _formTitle = "注册";

                    _form.append(new TextField("用户名:", name, 20, TextField.ANY));
                    //#if Revision != CMCC
                    //_form.append(new TextField("手机号:", phone, 20, TextField.ANY));
                    _form.append(new TextField("推荐人角色名（" + serverName + "）（如果没有可以不填，填写后，你和推荐人都可以获得丰厚奖励）：", recommend, 20, TextField.ANY));
                    //#endif

                    _form.addCommand(new Command("注册", Command.ITEM, 0));
                    _form.addCommand(new Command("返回", Command.BACK, 0));
                    _form.setCommandListener(this);
                    World.RecordPreousDisplay(_form);
                }

                break;
            case SS_REGISTERING:
                if(thread == null){
                    thread = new Thread(this);
                    thread.start();
                }else{
                    if(World.isKeyPressed(World.SOFT_LAST_PRESSED, true)){
                        thread = null;
                        subState = SS_REGISTER;

                        closeConnection();

                        _form = null;
                        thread = null;
                    }

                }

                break;
            case SS_ACTOR_GET_LIST:
                if(thread == null){
                    World.playerImageSet[0] = World.getImageSetFromLocal("_male");
                    World.playerImageSet[1] = World.getImageSetFromLocal("_female");
                    thread = new Thread(this);
                    thread.start();
                }else{
                    if(World.isKeyPressed(World.SOFT_LAST_PRESSED, true)){
                        thread = null;
                        subState = SS_CHOICE;

                        closeConnection();

                        _form = null;
                        thread = null;
                    }

                }

                break;
            case SS_ACTOR_SELECT_DELETE:
                switch(_delState){
                    case SS_ACTOR_SELECT_DELETE_CONFIRM:
                        if(World.isKeyPressed(World.FIRE_PRESSED, true) || World.isKeyPressed(World.SOFT_FIRST_PRESSED, true)){
                            _delState = SS_ACTOR_SELECT_DELETE_CONFIRM2;
                        }else if(World.isKeyPressed(World.SOFT_LAST_PRESSED, true)){
                            subState = SS_ACTOR_SELECT;
                        }
                        break;
                    case SS_ACTOR_SELECT_DELETE_CONFIRM2:
                        if(World.isAnyKeyPressed()){
                            subState = SS_ACTOR_SELECT;
                        }
                        deleteTimer -= World._elapsedTime < World.MILLIS_PRE_UPDATE? World.MILLIS_PRE_UPDATE: World._elapsedTime;
                        if(deleteTimer < 0){
                            //requestDeleteActor(actorName);

                            int serial = World.sendRequest(CONN_DELETE_USER, new Object[]{
                                actorName
                            }, false);

                            actorDelete_request = true;
                            actorDelete_ok = false;
                            actorDelete_error = false;
                            actorDelete_net_request.put(new Integer(serial), new Integer(serial));

                            _delState = SS_ACTOR_SELECT_DELETE_DOING;
                        }
                        break;
                    case SS_ACTOR_SELECT_DELETE_DOING:
                        if(actorDelete_ok){
                            _delState = SS_ACTOR_SELECT_DELETE_SUCCESS;
                        }else if(actorDelete_error){
                            _delState = SS_ACTOR_SELECT_DELETE_FAILED;
                        }
                        break;
                    case SS_ACTOR_SELECT_DELETE_SUCCESS:
                        subState = SS_ACTOR_GET_LIST;
                        break;
                    case SS_ACTOR_SELECT_DELETE_FAILED:
                        if(World.isAnyKeyPressed()){
                            subState = SS_ACTOR_SELECT;
                        }
                        break;
                }
                break;
            case SS_ACTOR_SELECT_SHOWMENU:
            	//#if TouchScreen == true
            	directChoose = false;
                focusButton = StaticUtils .getDragOverButton();
                if(focusButton != -1 && focusButton>=2000){
                	_subMenuIndex = focusButton-2000;
                }
                pressedButton = StaticUtils.getPressedButton();
                if(-1 != pressedButton && pressedButton>=2000){
                	_subMenuIndex = pressedButton - 2000;
                	directChoose = true;
                }
                //#endif 
                if(World.isKeyPressed(World.UP_PRESSED, true)){
                    _subMenuIndex--;
                    if(_subMenuIndex < 0){
                        _subMenuIndex = ACTOR_MENU.length - 1;
                    }
                }else if(World.isKeyPressed(World.DOWN_PRESSED, true)){
                    _subMenuIndex++;
                    if(_subMenuIndex >= ACTOR_MENU.length){
                        _subMenuIndex = 0;
                    }
                }else 
                //#if TouchScreen == true
                	if(World.isKeyPressed(World.FIRE_PRESSED, true) || World.isKeyPressed(World.SOFT_FIRST_PRESSED, true) || directChoose){
                		                	//#else
                		                	//# if(World.isKeyPressed(World.FIRE_PRESSED, true) || World.isKeyPressed(World.SOFT_FIRST_PRESSED, true)){
                		                	//#endif 
                	//if(World.isKeyPressed(World.FIRE_PRESSED, true) || World.isKeyPressed(World.SOFT_FIRST_PRESSED, true)){
                    if(ACTOR_MENU[_subMenuIndex].equals(ACTORMENU_LOGIN)){
                        _message = "正在登录角色...";
                        subState = SS_ACTOR_LOGINING;
                    }else if(ACTOR_MENU[_subMenuIndex].equals(ACTORMENU_DELETE)){
                        subState = SS_ACTOR_SELECT_DELETE;
                        deleteTimer = 10000;
                        _delState = SS_ACTOR_SELECT_DELETE_CONFIRM;
                    }
                }else if(World.isKeyPressed(World.SOFT_LAST_PRESSED, true)){
                    subState = SS_ACTOR_SELECT;
                }
                break;
            case SS_ACTOR_SELECT:
            	
            	//#if TouchScreen == true
            	directChoose = false;
                focusButton = StaticUtils .getDragOverButton();
                if(focusButton != -1 && focusButton>=2000){
                	_subIndex = focusButton-2000;
                }
                pressedButton = StaticUtils.getPressedButton();
                if(-1 != pressedButton && pressedButton>=2000){
                	_subIndex = pressedButton - 2000;
                	directChoose = true;
                }
                //#endif 
                if(World.isKeyPressed(World.UP_PRESSED, true)){
                    if(_subIndex > 0){
                    	actorPageChange = false;
                        _subIndex --;
                        dir = Sprite.DOWN;
                        World.iconOffset = 0;
                        World.battleBout = 1;
                    }
                }else if(World.isKeyPressed(World.DOWN_PRESSED, true)){
                    if(_subIndex <(actorList.size()-1)){
                    	actorPageChange = false;
                        _subIndex ++;
                     //   System.out.println("sub"+_subIndex);
                        dir = Sprite.DOWN;
                        World.iconOffset = 0;
                        World.battleBout = 1;
                    }
                    	
                }else 
                	if(World.isKeyPressed(World.LEFT_PRESSED, true)){
                    
                		actorPageChange = true;
                		if(_subIndex > 3){
                    	 _subIndex=_subIndex-4;
	                   	 if(!((_subIndex - itemShowBegin)>=4)){
	                     		itemShowBegin=(byte) (itemShowBegin-4);
	                     		if(itemShowBegin<0)
	                     			itemShowBegin=0;	
	                     	}
	                     	
	                    	dir = Sprite.DOWN;           
	                        World.iconOffset = 0;
	                        World.battleBout = 1;
                		}else{
                			itemShowBegin=0;
                			dir = Sprite.DOWN;           
                            World.iconOffset = 0;
                            World.battleBout = 1;
                		}
                	
                }else if(World.isKeyPressed(World.RIGHT_PRESSED, true)){
                    
                	actorPageChange = true;
                	if(actorList.size()>3){
	                	if(_subIndex < (actorList.size() - 4) ){
	                			_subIndex=_subIndex+4;
	                       if((_subIndex - itemShowBegin)>=4){
	                    		itemShowBegin=(byte) (itemShowBegin+4);
	                    	}
	                        if(itemShowBegin>(actorList.size()-4))
	                        {
	                        	itemShowBegin=(byte) (actorList.size() - 4);
	                        }
	                        dir = Sprite.DOWN;                      
	                        World.iconOffset = 0;
	                        World.battleBout = 1;
	                	}
	                   else{
	                    	itemShowBegin=(byte) (actorList.size() - 4);
	                    	dir = Sprite.DOWN;                      
	                        World.iconOffset = 0;
	                        World.battleBout = 1;
	                	   
	                    }
                	}
                	
                }else    
                	//#if TouchScreen == true
                    if(World.isKeyPressed(World.FIRE_PRESSED, true) || World.isKeyPressed(World.SOFT_FIRST_PRESSED, true) || directChoose){
                    		                	//#else
                    		                	//# if(World.isKeyPressed(World.FIRE_PRESSED, true) || World.isKeyPressed(World.SOFT_FIRST_PRESSED, true)){
                    		                	//#endif 
                    String[] actor = (String[])actorList.elementAt(_subIndex);

                    if(actor[1]!= null && actor[1].equals("")){
                        if(_form == null){
                            _form = new Form("新建角色");
                            _formTitle = "新建角色";
                            _form.append(new TextField("角色名:", null, 20, TextField.ANY));
                /*            ChoiceGroup choice = new ChoiceGroup("性别", Choice.EXCLUSIVE);
                            choice.append("男", null);
                            choice.append("女", null);
                            _form.append(choice);
					*/
                            _form.addCommand(new Command("创建", Command.ITEM, 0));
                            _form.addCommand(new Command("返回", Command.BACK, 0));
                            _form.setCommandListener(this);
                            World.RecordPreousDisplay(_form);
                        }
                    }else{
                        if(World.isKeyPressed(World.SOFT_FIRST_PRESSED, true)){
                            actorName = actor[0];
                            actorSex = Integer.parseInt(actor[2]);
                            subState = SS_ACTOR_SELECT_SHOWMENU;
                        }else{
                            _message = "正在登录角色...";
                            actorName = actor[0];
                            actorSex = Integer.parseInt(actor[2]);
                            subState = SS_ACTOR_LOGINING;
                        }
                    }
                }else if(World.isKeyPressed(World.SOFT_LAST_PRESSED, true)){
                    thread = null;
                    subState = SS_CHOICE;
                    _subIndex = 0;
                    World.iconOffset = 0;
                    World.battleBout = 1;

                    closeConnection();

                    _form = null;
                    thread = null;
                }

                break;
            case SS_ACTOR_CREATING:
                if(thread == null){
                    thread = new Thread(this);
                    thread.start();
                }else{
                    if(World.isKeyPressed(World.SOFT_LAST_PRESSED, true)){
                        subState = SS_ACTOR_SELECT;
                        _form = null;
                        thread = null;
                    }
                }

                break;
            case SS_ACTOR_LOGINING:
                if(thread == null){
                    _image = null;
                    thread = new Thread(this);
                    thread.start();
                }else{
                    if(World.isKeyPressed(World.SOFT_LAST_PRESSED, true)){
                        thread = null;
                        subState = SS_LOGINING;

                        closeConnection();

                        _form = null;
                        thread = null;
                        try{
                            _image = Image.createImage("/gamemenu.png");
                        }catch(IOException e){
                            //#debug
                            e.printStackTrace();
                        }
                    }
                }

                break;
            case SS_HELP:
            case SS_ABOUT:
                if(World.isKeyPressed(World.SOFT_LAST_PRESSED, true)){
                    subState = SS_CHOICE;
                    _scroll = 0;
                }else if(World.isKeyPressed(World.UP_PRESSED, true)){
                    if(_scroll > 0){
                        _scroll -= CHAR_HEIGHT;
                        _scroll = _scroll > 0? _scroll: 0;
                    }
                }else if(World.isKeyPressed(World.DOWN_PRESSED, true)){
                    int h = World.viewHeight - SCREEN_MARGIN * 2;
                    int maxScroll = infoStrings.length * CHAR_HEIGHT - h;

                    if(_scroll < maxScroll){
                        _scroll += 10;
                        _scroll = Math.min(_scroll, maxScroll);
                    }
                }

                break;
            case SS_MESSAGE:
                if(World.isKeyPressed(World.SOFT_LAST_PRESSED, true)){
                    subState = SS_CHOICE;
                }

                break;
            case SS_REGISTER_MESSAGE:
                if(World.isKeyPressed(World.SOFT_FIRST_PRESSED, true) || World.isKeyPressed(World.SOFT_LAST_PRESSED, true)){
                    subState = SS_LOGINING;
                }

                break;
        }
    }

    private void cycleLoading(){
        if(subState != SS_LOADING_ERROR){
            if(thread == null){
                thread = new Thread(this);
                thread.start();

                if(subState == SS_LOADING_BATTLE || subState == SS_LOADING_SERVERBATTLE){
                    _message = "正在载入战斗...";

                    if(subState == SS_LOADING_BATTLE){
                        World.battleSkillSeal = (byte)0x0;
                        World.battlePetSkillSeal = (byte)0x0;
                    }else{
                        World.battleSkillSeal = (byte)0xFF;
                        World.battlePetSkillSeal = (byte)0xFF;
                    }

                    World.battleCanAction = true;
                    World.battlePetCanAction = true;

                    initBattleSpriteSkillName(World.instance.playerSprite);
                    initBattleSpriteSkillName(World.instance.playerPet);
                    initBattleSpriteSkillName(World.instance.armySprite);
                    initBattleSpriteSkillName(World.instance.armyPet);
                }else{
                    _message = "正在载入地图...";
                }
            }
        }else{
            if(World.isAnyKeyPressed()){
                exitToGameMenu(null, false);
            }
        }
    }

    private void initBattleSpriteSkillName(BattleSprite[] in){
        if(in != null){
            for(int i = 0; i < in.length; i++){
                if(in[i] != null){
                    in[i].useSkillName = "";
                }
            }
        }
    }

    private void cycleMainMenu(){
    	//#if TouchScreen == true
        boolean directChoose = false;
        if(subState == SS_MAINMENU_NORMAL || subState == SS_MAINMENU_SHOWSUBMENU ){
	        int focusButton = StaticUtils .getDragOverButton();
	        if(focusButton != -1 && focusButton>=2000){
	        	if(subState == SS_MAINMENU_NORMAL){
	        		_index = focusButton-2000;
	        		subState = SS_MAINMENU_FLYING_OUT;
	        		toState = SS_MAINMENU_SHOWSUBMENU;
	        	}else if(subState == SS_MAINMENU_SHOWSUBMENU){
	        		_subIndex = focusButton-2000;
	        	}
	        }
	        int pressedButton = StaticUtils.getPressedButton();
	        if(-1 != pressedButton && pressedButton>=2000){
	        	if(subState == SS_MAINMENU_NORMAL){
		        	_index = pressedButton - 2000;
		        	directChoose = true;
		        	subState = SS_MAINMENU_FLYING_OUT;
		        	toState = SS_MAINMENU_SHOWSUBMENU;
	        	}else if(subState == SS_MAINMENU_SHOWSUBMENU){
	        		_subIndex = pressedButton - 2000;
	            	directChoose = true;
	        	}
	        }
        }
        //#endif 
        if(World.isKeyPressed(World.SOFT_LAST_PRESSED, true)){
            if(subState == SS_MAINMENU_SHOWSUBMENU){
                subState = SS_MAINMENU_FLYING_IN;
            }else{
                subState = SS_MAINMENU_FLYING_OUT;
                toState = SS_MAINMENU_EXIT;
            }
            repaintNextTime = true;
        }else              
         //#if TouchScreen == true
            if(World.isKeyPressed(World.FIRE_PRESSED, true) || World.isKeyPressed(World.SOFT_FIRST_PRESSED, true) || directChoose){
            	//#else
            	//# if(World.isKeyPressed(World.FIRE_PRESSED, true) || World.isKeyPressed(World.SOFT_FIRST_PRESSED, true)){
            	//#endif 
            if(subState == SS_MAINMENU_SHOWSUBMENU){
                handleMainMenuCommand(SUB_MENU[_index][_subIndex]);
                //}else{
                //subState = SS_MAINMENU_FLYING_OUT;
                //toState = SS_MAINMENU_SHOWSUBMENU;
            }
            repaintNextTime = true;
        }else if(World.isKeyPressed(World.UP_PRESSED, true) || World.isKeyPressed(World.KEY_NUM2_PRESSED, true)){
            if(subState == SS_MAINMENU_SHOWSUBMENU){
                _subIndex--;
                if(_subIndex < 0)
                    _subIndex = SUB_MENU[_index].length - 1;
            }else if(subState != SS_MAINMENU_FLYING_OUT){
                _index = 0;
                _subIndex = 0;
                subState = SS_MAINMENU_FLYING_OUT;
                toState = SS_MAINMENU_SHOWSUBMENU;
            }
            repaintNextTime = true;
        }else if(World.isKeyPressed(World.DOWN_PRESSED, true) || World.isKeyPressed(World.KEY_NUM8_PRESSED, true)){
            if(subState == SS_MAINMENU_SHOWSUBMENU){
                _subIndex++;
                if(_subIndex > (SUB_MENU[_index].length - 1))
                    _subIndex = 0;
            }else if(subState != SS_MAINMENU_FLYING_OUT){
                _index = 2;
                _subIndex = 0;
                subState = SS_MAINMENU_FLYING_OUT;
                toState = SS_MAINMENU_SHOWSUBMENU;
            }
            repaintNextTime = true;
        }else if(World.isKeyPressed(World.RIGHT_PRESSED, true) || World.isKeyPressed(World.KEY_NUM6_PRESSED, true)){
            if(subState != SS_MAINMENU_SHOWSUBMENU && subState != SS_MAINMENU_FLYING_OUT){
                _index = 1;
                _subIndex = 0;
                subState = SS_MAINMENU_FLYING_OUT;
                toState = SS_MAINMENU_SHOWSUBMENU;
            }
            repaintNextTime = true;
        }else if(World.isKeyPressed(World.LEFT_PRESSED, true) || World.isKeyPressed(World.KEY_NUM4_PRESSED, true)){
            if(subState != SS_MAINMENU_SHOWSUBMENU && subState != SS_MAINMENU_FLYING_OUT){
                _index = 3;
                _subIndex = 0;
                subState = SS_MAINMENU_FLYING_OUT;
                toState = SS_MAINMENU_SHOWSUBMENU;
            }
            repaintNextTime = true;
        }
    }

    public static void addChatMsgSource(String name){
        if(name.equals(World.player.name) || name.equals("系统")){
            return;
        }

        if(World.net_chat_lastMsgSource.contains(name)){
            World.net_chat_lastMsgSource.removeElement(name);
        }else{
            if(World.net_chat_lastMsgSource.size() == World.NET_CHAT_LASTMSGAMOUNT){
                World.net_chat_lastMsgSource.removeElementAt(0);
            }
        }
        World.net_chat_lastMsgSource.addElement(name);
    }

    public void handleMainMenuCommand(String command){
        if(command.equals("改变称号")){
            //#debug
            World.GOD_MODE = !World.GOD_MODE;

            World.titleFlag++;
            World.titleFlag %= 5;
        }else if(command.equals("地图开关")){
            World.miniMapOption++;

            if(World.miniMapOption > 2){
                World.miniMapOption = 0;
            }
        }else if(command.equals("武器装备")){
            //#if (Directory == SE-K500) || (Directory == SE-K300) || (Directory == Nokia403)
            //# World.setGameState(null);
            //# World.createTaskUIEvent(GameEvent.EVENT_VIEW_EQUIP, 0, "");
            //#else
            GameState state = new GameState(GameState.STATE_EDITEQUIPS);
            World.setGameState(state);
            //#endif
        }else if(command.equals("人物属性")){
            World.player.reCalculateAttributes();
            GameState state = new GameState(GameState.STATE_EDITATTR);
            World.setGameState(state);
        }else if(command.equals("物品背包")){
            GameState state = new GameState(GameState.STATE_EDITITEM);
            World.setGameState(state);
        }else if(command.equals("战斗技能")){
            World.setGameState(null);
            World.createTaskUIEvent(GameEvent.EVENT_VIEW_BATTLE_SKILL, 0, "");
        }else if(command.equals("生活技能")){
            World.setGameState(null);
            World.createTaskUIEvent(GameEvent.EVENT_PRODUCE_ITEM, 0, "");
        }else if(command.equals("退出游戏")){
            byte[] taskSave = World._gtvm.save();

            GameState.logouting = true;
            World.sendRequest(CONN_UPLOAD, new Object[]{
                            new Short(World.currMapId), new Short(World.player.x), new Short(World.player.y), taskSave, new Boolean(true)
            }, false);
            try{
                Thread.sleep(1000);
            }catch(InterruptedException e){
            }

            exitToGameMenu(null, false);
        }else if(command.equals("聊天设置")){
            World.setGameState(null);
            World.createTaskUIEvent(GameEvent.EVENT_CHAT_OPTION, 0, "");
        }else if(command.equals("圈设置")){
            World.setGameState(null);
            World.createTaskUIEvent(GameEvent.EVENT_CHAT_CIRCLE_OPTION, 0, "");
        }else if(command.equals("发起聊天")){
            if(!World.chat_input_doing){
                showSendChatForm(false);
                World.chat_input_doing = true;
            }
        }else if(command.equals("回复密聊")){
            if(!World.chat_input_doing){
                showSendChatForm(true);
                World.chat_input_doing = true;
            }
        }else if(command.equals("聊天记录")){
            GameState state = new GameState(GameState.STATE_CHATLIST);
            World.setGameState(state);

            World.hideHint(World.HINT_MESSAGE);
        }else if(command.equals("任务查看")){
            World.setGameState(null);
            World.createTaskUIEvent(GameEvent.EVENT_TASK_VIEW, 0, "");
        }else if(command.equals("周围玩家")){
            World.setGameState(null);
            World.createTaskUIEvent(GameEvent.EVENT_PLAYER_VIEW, 0, "");
        }else if(command.equals("好友列表")){
            World.setGameState(null);
            World.createTaskUIEvent(GameEvent.EVENT_FRIEND_VIEW, 0, "");
        }else if(command.equals("黑名单")){
            World.setGameState(null);
            World.createTaskUIEvent(GameEvent.EVENT_BLACKLIST_VIEW, 0, "");
        }else if(command.equals("公会查看")){
            World.setGameState(null);
            World.createTaskUIEvent(GameEvent.EVENT_TONG_LIST, 0, "");
        }else if(command.equals("创建队伍")){
            World.sendRequest(GameState.CONN_TEAM_CREATE, new Object[0], false);
            //World.requestTeamCreate();
            World.setGameState(null);
        }else if(command.equals("离开队伍")){
            String msg = "您已经离开队伍";
            if(World.teamLeader){
                msg += "，您的队伍已解散";
            }

            World.sendRequest(GameState.CONN_TEAM_LEAVE, new Object[]{
                            new Integer(World.teamId), new Integer(0), new Byte(World.TEAM_STATUS_LEAVE)
            }, false);

            World.releaseTeam();

            World.showMessage(msg, (byte)0);
            World.player.leaveParty = true;
            World.setGameState(null);
        }else if(command.equals("精灵速递")){
            World.setGameState(null);
            World.createTaskUIEvent(GameEvent.EVENT_MAIL_VIEW, 0, "");
        }else if(command.equals("宠物驯养")){
            World.setGameState(null);
            World.createTaskUIEvent(GameEvent.EVENT_PET_OPTION, 0, "");
        }else if(command.equals("系统设置")){
            World.setGameState(null);
            World.createTaskUIEvent(GameEvent.EVENT_SYSTEM_OPTION, 0, "");
        }else if(command.equals("脱离卡死")){
            World.setGameState(null);
            World.moveToEffectPosition();
            /*
             }else if(command.equals("常见问题")){
             World.setGameState(null);

             World.createTaskUIEvent(GameEvent.EVENT_FAQ, -2, "");
             */
        }else if(command.equals("联机帮助")){
            World.setGameState(null);

            if(!World._gtvm.hasTask(World.TASK_ID_HELP)){
                World.requestDownloadTask((short)World.TASK_ID_HELP);
                //World.addLocalTask(World.TASK_ID_HELP);
            }
        }else if(command.equals("呼叫GM")){
            World.setGameState(null);
            World.createTaskUIEvent(GameEvent.EVENT_CALLGM, 0, "");
        }else if(command.equals(IMONEY_ADD)){
            World.setGameState(null);

            //#if Revision == CMCC || (Revision == JIANGSUNCMCC) 
            //# World.sendRequest(CONN_TOUCH_NPC, new Object[]{
            //#                 new Integer(92340225)
            //#             }, false);
            //#elif Revision == QQ
            //# World.sendRequest(CONN_TOUCH_NPC, new Object[]{
            //#                 new Integer(92340226)
            //#             }, false);
            //#else
            World.sendRequest(CONN_TOUCH_NPC, new Object[]{
                new Integer(92340224)
            }, false);
            //#endif
        }else if(command.equals(IMONEY_SALE)){
            World.setGameState(null);
            World.createTaskUIEvent(GameEvent.EVENT_ISHOP_LIST, 0, "");
        }else if(command.equals("更换角色")){
            byte[] taskSave = World._gtvm.save();

            GameState.logouting = true;
            World.sendRequest(CONN_UPLOAD, new Object[]{
                            new Short(World.currMapId), new Short(World.player.x), new Short(World.player.y), taskSave, new Boolean(true)
            }, false);
            try{
                Thread.sleep(1000);
            }catch(InterruptedException e){
            }

            exitToGameMenu(null, true);
        }
        //#if Revision == CMCC || (Revision == JIANGSUNCMCC) 
        else if(command.equals("好友推荐")){
        	 World.setGameState(null);
        	 World.sendRequest(CONN_TOUCH_NPC, new Object[]{
                     new Integer(234946564)
                 }, false);
        }else if(command.equals("江苏好友推荐")){
        	//System.out.println("天才姜姜");
        	World.setGameState(null);
        	GameState state = new GameState(GameState.STATE_INPUT_FORM);
        	_formTitle = "江苏好友推荐";
        	state._form = new Form(GameState._formTitle);
            state._form.append(new TextField("请输入手机号:", null, 11, TextField.NUMERIC));
            state._form.addCommand(new Command("推荐", Command.ITEM, 0));
            state._form.addCommand(new Command("返回", Command.BACK, 0));
            state._form.setCommandListener(state);
            World.RecordPreousDisplay(state._form);
            
        }
        //#endif
    }

    public void showSendChatForm(boolean r){
        _formTitle = "发起聊天";
        _form = new Form(_formTitle);

        String txt = World.chat_input_uncompelte_message;

        if(r && World.net_chat_lastMsgSource.size() != 0){
            //txt = "/" + World.net_chat_lastMsgSource.elementAt(World.net_chat_lastMsgSource.size() - 1) + " ";
            _formTitle = "回复密聊";
        }

        _form.append(new TextField("内容:", txt, 200, TextField.ANY));
        ChoiceGroup choice = null;
        if(r && World.net_chat_lastMsgSource.size() != 0){
            choice = new ChoiceGroup("最近联系人", Choice.EXCLUSIVE);
            for(int i = World.net_chat_lastMsgSource.size() - 1; i >= 0; i--){
                choice.append((String)World.net_chat_lastMsgSource.elementAt(i), null);
            }
        }else{
            choice = new ChoiceGroup("频道", Choice.EXCLUSIVE);
            choice.append("地区", null);
            choice.append("公会", null);
            choice.append("团队", null);
            choice.append("小队", null);
            choice.append("圈", null);
            choice.append("世界", null);
            choice.setSelectedIndex(World.net_chat_current_channel, true);
        }

        _form.append(choice);

        _form.addCommand(new Command("发送", Command.ITEM, 0));
        _form.addCommand(new Command("返回", Command.BACK, 0));
        _form.setCommandListener(this);

        World.RecordPreousDisplay(_form);
    }

    private void cycleEditEquips(){
        repaintNextTime = World.tick % 5 == 0;
        //#if TouchScreen == true
    	boolean directChoose = false;
        int focusButton = StaticUtils .getDragOverButton();
        if(focusButton != -1 && focusButton>=2000){
        
        	if(showPartEquip){
        		equSelect = focusButton-2000;
        	}else{
        		partSelect =focusButton-2000;
        	}
        	//_subIndex = focusButton-2000;
        }
        int pressedButton = StaticUtils.getPressedButton();
        if(-1 != pressedButton && pressedButton>=2000){
        	if(showPartEquip){
        		equSelect = pressedButton-2000;
        	}else{
        		partSelect =pressedButton-2000;
        	}
        	//_subIndex = pressedButton - 2000;
        	directChoose = true;
        }
        //#endif 
        if(World.isKeyPressed(World.UP_PRESSED, true)){
            if(showPartEquip){
                equSelect--;
                if(equSelect < 0)
                    equSelect = (byte)listEquips.length - 1;
            }else{
                partSelect--;
                if(partSelect < 0)
                    partSelect = (byte)(GameItem.EQUIP_TYPE_NAME.length - 1);
            }
            repaintNextTime = true;
        }else if(World.isKeyPressed(World.DOWN_PRESSED, true)){
            if(showPartEquip){
                equSelect++;
                if(equSelect == listEquips.length)
                    equSelect = 0;
            }else{
                partSelect++;
                if(partSelect >= GameItem.EQUIP_TYPE_NAME.length)
                    partSelect = 0;
            }
            repaintNextTime = true;
        }else if(World.isKeyPressed(World.KEY_STAR_PRESSED, true)){
            showEquipInfo = !showEquipInfo;
            repaintNextTime = true;
        }else if(World.isKeyPressed(World.KEY_NUM0_PRESSED, true)){
            showDifferent = !showDifferent;
            repaintNextTime = true;

        }else 

            //#if TouchScreen == true
              if(World.isKeyPressed(World.FIRE_PRESSED, true) || directChoose){
              	//#else
              	//# if(World.isKeyPressed(World.FIRE_PRESSED, true) ){
              	//#endif 
        	//if(World.isKeyPressed(World.FIRE_PRESSED, true)){
            if(showPartEquip){
                if(equSelect == 0){
                    GameItem equ = World.player.playerEquips[(byte)partSelect];

                    if(equ.type != GameItem.TYPE_NULL){
                        World.player.playerEquips[(byte)partSelect] = GameItem.createNullEquip((byte)partSelect);
                        World.player.equipsInBag.addElement(equ);
                        World.player.reCalculateAttributes();
                    }
                }else{
                    GameItem equ = (GameItem)listEquips[equSelect];
                    GameItem nowEqu = World.player.playerEquips[(byte)partSelect];
                    if(nowEqu.type != GameItem.TYPE_NULL){
                        World.player.equipsInBag.addElement(nowEqu);
                    }
                    World.player.equipsInBag.removeElement(equ);
                    World.player.playerEquips[(byte)partSelect] = equ;
                    World.player.reCalculateAttributes();
                }
                showPartEquip = false;
                showEquipInfo = false;
                repaintNextTime = true;
                return;
            }

            showPartEquip = !showPartEquip;
            if(showPartEquip){
                listEquips = World.player.filteEquips((byte)partSelect);
                Object[] obj = new Object[listEquips.length + 1];
                obj[0] = new String("卸下装备");
                System.arraycopy(listEquips, 0, obj, 1, listEquips.length);
                listEquips = obj;

                menuWidth = 0;
                for(int i = 0; i < listEquips.length; i++){
                    int w = 0;
                    if(listEquips[i] instanceof String){
                        w = font.stringWidth((String)listEquips[i]);
                    }else{
                        w = font.stringWidth(((GameItem)listEquips[i]).getName(false, -1)) + 5;
                    }
                    if(menuWidth < w)
                        menuWidth = w;
                }
                if(menuWidth + EQU_MENU_LEFT > World.viewWidth)
                    menuWidth = World.viewWidth - EQU_MENU_LEFT;
                if(equSelect >= listEquips.length){
                    equSelect = 0;
                }
            }else
                showEquipInfo = false;
            repaintNextTime = true;
        }else if(World.isKeyPressed(World.SOFT_LAST_PRESSED, true)){
            if(showPartEquip){
                showPartEquip = false;
                equSelect = 0;
            }else{
                World.player.restoreEquipsBackup();
                World.setGameState(null);
            }
            repaintNextTime = true;
        }else if(World.isKeyPressed(World.SOFT_FIRST_PRESSED, true)){
            if(!showPartEquip){
                World.setGameState(null);
                requestChangeEquips();
                repaintNextTime = true;
            }
        }else if(World.isKeyPressed(World.KEY_NUM1_PRESSED, true)){
            attrSelected--;
            if(attrSelected < 4)
                attrSelected = (byte)(drawAttrs.length - 1);

            if(attrSelected < attrShowBegin){
                attrShowBegin = attrSelected;
            }else if(attrSelected - ATTR_SHOWNUM >= attrShowBegin){
                attrShowBegin = (byte)(attrSelected - ATTR_SHOWNUM + 1);
            }

            repaintNextTime = true;
        }else if(World.isKeyPressed(World.KEY_NUM7_PRESSED, true)){
            attrSelected++;
            if(attrSelected >= drawAttrs.length)
                attrSelected = 4;

            if(attrSelected >= attrShowBegin + ATTR_SHOWNUM){
                attrShowBegin = (byte)(attrSelected - ATTR_SHOWNUM + 1);
            }else if(attrSelected < attrShowBegin){
                attrShowBegin = attrSelected;
            }

            repaintNextTime = true;
        }else if(World.isKeyPressed(World.LEFT_PRESSED, true)){
            if(!showPartEquip){
                if(partSelect < 4 || partSelect == 6){
                    partSelect = 4;
                }else if(partSelect == 4){
                    partSelect = 7;
                }else if(partSelect == 8){
                    partSelect = 5;
                }else if(partSelect == 5){
                    partSelect = 3;
                }
                repaintNextTime = true;
            }
        }else if(World.isKeyPressed(World.RIGHT_PRESSED, true)){
            if(!showPartEquip){
                if(partSelect < 4 || partSelect == 6){
                    partSelect = 5;
                }else if(partSelect == 5){
                    partSelect = 8;
                }else if(partSelect == 7){
                    partSelect = 4;
                }else if(partSelect == 4){
                    partSelect = 3;
                }
                repaintNextTime = true;
            }
        }
    }

    private void cycleEditAttr(){
        repaintNextTime = World.tick % 5 == 0;
      //#if TouchScreen == true
       // boolean directChoose = false;
        boolean leftDirectChoose = false;
        boolean rightDirectChoose = false;
        int focusButton = StaticUtils .getDragOverButton();
        if(focusButton != -1 && focusButton >= 2000){
        	if(focusButton >= 3000){
        		attrSelected = (byte) ((focusButton-3000)/2+attrShowBegin);
        		/*if((focusButton - 3000)%2 != 0){
        			rightDirectChoose = true;
        			leftDirectChoose =false;
        		}else{
        			leftDirectChoose =true;
        			rightDirectChoose =false;
        		}*/
        	}else{
        		attrSelected = (byte) (focusButton -2000+attrShowBegin);
        	}
        	//_subIndex = focusButton-2000;
        }
        int pressedButton = StaticUtils.getPressedButton();
        if(-1 != pressedButton && pressedButton>=2000){
        	if(pressedButton >= 3000){
        		attrSelected = (byte) ((pressedButton-3000)/2+attrShowBegin);
        		if((pressedButton - 3000)%2 != 0){
        			rightDirectChoose = true;
        			leftDirectChoose =false;
        		}else{
        			leftDirectChoose =true;
        			rightDirectChoose =false;;
        		}
        	}else{
        		attrSelected = (byte) (pressedButton -2000+attrShowBegin);
        		
        	}
        	//_subIndex = pressedButton - 2000;
        	//directChoose = true;
        }
        //#endif
        if(World.isKeyPressed(World.SOFT_LAST_PRESSED, true)){
            World.setGameState(null);
            repaintNextTime = true;
            World.player.restoreBaseAttrBackup();
            World.player.reCalculateAttributes();
        }else if(World.isKeyPressed(World.UP_PRESSED, true)){
            attrSelected--;
            if(attrSelected < 0)
                attrSelected = (byte)(drawAttrs.length - 1);

            if(attrSelected < attrShowBegin){
                attrShowBegin = attrSelected;
            }else if(attrSelected - ATTR_SHOWNUM >= attrShowBegin){
                attrShowBegin = (byte)(attrSelected - ATTR_SHOWNUM + 1);
            }

            repaintNextTime = true;
        }else if(World.isKeyPressed(World.DOWN_PRESSED, true)){
            attrSelected++;
            if(attrSelected >= drawAttrs.length)
                attrSelected = 0;

            if(attrSelected >= attrShowBegin + ATTR_SHOWNUM){
                attrShowBegin = (byte)(attrSelected - ATTR_SHOWNUM + 1);
            }else if(attrSelected < attrShowBegin){
                attrShowBegin = attrSelected;
            }

            repaintNextTime = true;
        }else 
        	   //#if TouchScreen == true
            if(World.isKeyPressed(World.LEFT_PRESSED, true) || leftDirectChoose){
            	//#else
            	//# if(World.isKeyPressed(World.LEFT_PRESSED, true)){
            	//#endif 
        	//if(World.isKeyPressed(World.LEFT_PRESSED, true)){
            if(attrSelected < 4){
                Sprite s = World.player;
                if(s.baseAttribute[attrSelected] > s.attributeBackup[attrSelected]){
                    s.baseAttribute[attrSelected]--;
                    s.learnPoint++;
                    s.reCalculateAttributes();
                }

                repaintNextTime = true;
            }
        }else 
        	 //#if TouchScreen == true
            if(World.isKeyPressed(World.RIGHT_PRESSED, true) || rightDirectChoose){
            	//#else
            	//# if(World.isKeyPressed(World.RIGHT_PRESSED, true)){
            	//#endif 
        	//if(World.isKeyPressed(World.RIGHT_PRESSED, true)){
            if(attrSelected < 4){
                Sprite s = World.player;
                if(s.learnPoint != 0){
                    s.baseAttribute[attrSelected]++;
                    s.learnPoint--;
                    s.reCalculateAttributes();
                }
                repaintNextTime = true;
            }
        }else if(World.isKeyPressed(World.SOFT_FIRST_PRESSED, true)){
            World.setGameState(null);
            repaintNextTime = true;
            Sprite s = World.player;
            requestChangeAttribute(s.baseAttribute[0] - s.attributeBackup[0], s.baseAttribute[1] - s.attributeBackup[1], s.baseAttribute[2] - s.attributeBackup[2], s.baseAttribute[3]
                            - s.attributeBackup[3]);

        }
    }

    private void buildEditItemMenu(){
        Vector bag = getCurrentBag();
        if(bag.size() > 0){
            itemShowUseMenu = true;
            //itemMenuSelected = 0;
            
            if(itemSelected >= bag.size()){
                itemSelected = (byte)Math.max(bag.size() - 1, 0);
            }
            
            GameItem gi = (GameItem)bag.elementAt(itemSelected);
            if(gi.canUse()){
                if(World.player.petCurrent != null && gi.type == GameItem.TYPE_BASIC){
                    itemMenu = new String[]{
                                    "使用", "查看", "给宠物使用", "丢弃", "丢弃全部"
                    };
                }else{
                    if(gi.isPetFood()){
                        if(World.player.petCurrent != null){
                            itemMenu = new String[]{
                                            "查看", "给宠物喂食", "丢弃", "丢弃全部"
                            };
                        }else{
                            itemMenu = new String[]{
                                            "查看", "丢弃", "丢弃全部"
                            };
                        }
                    }else{
                        itemMenu = new String[]{
                                        "使用", "查看", "丢弃", "丢弃全部"
                        };
                    }
                }
            }else{
                if(gi.type == GameItem.TYPE_EQUIP){
                    if(gi.requiredLevel > World.player.level){
                        itemMenu = new String[]{
                                        "查看", "丢弃"
                        };
                    }else{
                        itemMenu = new String[]{
                                        "查看", "装备", "丢弃"
                        };
                    }
                }else{
                    itemMenu = new String[]{
                                    "查看", "丢弃", "丢弃全部"
                    };
                }
            }

            if(itemMenuSelected >= itemMenu.length){
                itemMenuSelected = 0;
            }
            
            itemDropItemConfirm = false;
        }else{
            itemMenuSelected = 0;
        }
    }
    
    private void cycleEditItem(){
    	
    	//#if TouchScreen == true
    	boolean directChoose = false;
    	leftKeyDirectChoose = false;
    	rightKeyDirectChoose = false;
        int focusButton = StaticUtils .getDragOverButton();
        if(focusButton != -1 && focusButton>=2000){
        	if(focusButton>=3000&& focusButton<=3002){
        		if(!itemShowUseMenu){//主页面显示
	        		if((focusButton-3000)-itemBagSelected>=1){
	        			if((focusButton-3000)-itemBagSelected>=2){
	        				leftKeyDirectChoose =true;
	        				rightKeyDirectChoose = false;
	        			}else{
	        				rightKeyDirectChoose = true;
	        				leftKeyDirectChoose =false;
	        			}
	        		}else if((focusButton-3000)-itemBagSelected <= -1){
	        			if((focusButton-3000)-itemBagSelected <= -2){
	        				leftKeyDirectChoose =false;
	        				rightKeyDirectChoose = true;
	        			}else{
	        				rightKeyDirectChoose = false;
	        				leftKeyDirectChoose =true;
	        			}
	        		}
	        	}else if(itemShowThrowCount){//丢弃
	        			if((focusButton-3000)-selectNumID>=1){ 			
		        				rightKeyDirectChoose = true;
		        				leftKeyDirectChoose =false;
		        		}else if((focusButton-3000)-selectNumID <= -1){
		        				rightKeyDirectChoose = false;
		        				leftKeyDirectChoose =true;
		        		}
	        		}
        	}else{
        		if(!itemShowUseMenu){
        			itemSelected = (byte) (focusButton-2000);
        		}else if(!itemShowThrowCount){
        			itemMenuSelected = (byte) (focusButton-2000); 
        		}
        		//itemSelected = (byte) (focusButton-2000);
        	}
        	//_subIndex = focusButton-2000;
        }
        int pressedButton = StaticUtils.getPressedButton();
        if(-1 != pressedButton && pressedButton>=2000){
        	if(pressedButton>=3000&& pressedButton<=3002){
        		if(!itemShowUseMenu){
	        		if((pressedButton-3000)-itemBagSelected>=1){
	        			if((pressedButton-3000)-itemBagSelected>=2){
	        				leftKeyDirectChoose =true;
	        				rightKeyDirectChoose = false;
	        			}else{
	        				rightKeyDirectChoose = true;
	        				leftKeyDirectChoose =false;
	        			}
	        		}else if((pressedButton-3000)-itemBagSelected <= -1){
	        			if((pressedButton-3000)-itemBagSelected <= -2){
	        				leftKeyDirectChoose =false;
	        				rightKeyDirectChoose = true;
	        			}else{
	        				rightKeyDirectChoose = false;
	        				leftKeyDirectChoose =true;
	        			}
	        		}
        		}else if(itemShowThrowCount){
        			if((pressedButton-3000)-selectNumID>=1){ 			
        				rightKeyDirectChoose = true;
        				leftKeyDirectChoose =false;
	        		}else if((pressedButton-3000)-selectNumID <= -1){
	        				rightKeyDirectChoose = false;
	        				leftKeyDirectChoose =true;
	        		}
        		}
        	}
        	else{
        		if(!itemShowUseMenu){
        			itemSelected = (byte) (pressedButton-2000);
        		}else if(!itemShowThrowCount){
        			itemMenuSelected = (byte) (pressedButton-2000); 
        		}
        		directChoose = true;
        	}
        	//directChoose = true;
        }
        //#endif 
        if(itemShowUseMenu && itemShowThrowCount){
            cycleSelectNum();
            if(World.isKeyPressed(World.SOFT_FIRST_PRESSED, true) || World.isKeyPressed(World.FIRE_PRESSED, true)){
                Vector bag = getCurrentBag();
                if(bag.size() > 0){
                    GameItem gi = (GameItem)bag.elementAt(itemSelected);
                    World.requestUseItem(gi, GameItem.USETYPE_THROW, selectNum);

                    GameItem throwItem = new GameItem(gi.type);
                    throwItem.itemId = gi.itemId;
                    throwItem.id = gi.id;
                    throwItem.name = gi.name;
                    throwItem.count = (short)-selectNum;
                    World.player.addItemToBag(throwItem);

                }
                itemShowUseMenu = false;
                itemShowThrowCount = false;
            }else if(World.isKeyPressed(World.SOFT_LAST_PRESSED, true)){
                itemShowThrowCount = false;
            }
            return;
        }

        repaintNextTime = World.tick % 5 == 0;
      //#if TouchScreen == true
        if(World.isKeyPressed(World.FIRE_PRESSED, true) || World.isKeyPressed(World.SOFT_FIRST_PRESSED, true) || directChoose){
        	//#else
        	//# if(World.isKeyPressed(World.FIRE_PRESSED, true) || World.isKeyPressed(World.SOFT_FIRST_PRESSED, true)){
        	//#endif 
       // if(World.isKeyPressed(World.SOFT_FIRST_PRESSED, true) || World.isKeyPressed(World.FIRE_PRESSED, true)){
            if(itemShowUseMenu){
                Vector bag = getCurrentBag();
                if(bag.size() > 0){
                    GameItem gi = (GameItem)bag.elementAt(itemSelected);
                    String cmd = (String)itemMenu[itemMenuSelected];
                    if(cmd.equals("使用")){
                        //使用
                        gi.use(World.player);
                        itemShowUseMenu = false;
                    }else if(cmd.equals("给宠物使用")){
                        gi.use(World.player.petCurrent);
                        itemShowUseMenu = false;
                    }else if(cmd.equals("给宠物喂食")){

                        World.sendRequest(CONN_PET_FEED, new Object[]{
                                        new Integer(World.player.petCurrent.petId), new Integer(gi.itemId)
                        }, false);

                        //requestPetFeed(World.player.petCurrent.petId, gi.itemId);
                        repaintNextTime = true;
                        itemShowUseMenu = false;
                    }else if(cmd.equals("丢弃")){
                        //丢弃

                        if(gi.type == GameItem.TYPE_EQUIP){
                            //丢弃全部
                            itemMenu = new String[]{
                                            "取消丢弃", "确认丢弃"
                            };
                            itemDropItemConfirmOldSelected = itemMenuSelected;
                            itemMenuSelected = 0;
                            itemDropItemConfirm = true;
                        }else{
                            initSelectNum(1, gi.count);
                            itemShowThrowCount = true;
                        }
                    }else if(cmd.equals("丢弃全部")){
                        //丢弃全部
                        itemMenu = new String[]{
                                        "取消丢弃", "确认丢弃"
                        };
                        itemDropItemConfirmOldSelected = itemMenuSelected;
                        itemMenuSelected = 0;
                        itemDropItemConfirm = true;
                    }else if(cmd.equals("查看")){
                        itemShowUseMenu = false;
                        itemShowInfo = !itemShowInfo;
                        itemShowCurrentEquip = false;
                        repaintNextTime = true;
                    }else if(cmd.equals("装备")){
                        if(gi.type == GameItem.TYPE_EQUIP){
                            World.player.backupEquips();

                            if(World.player.playerEquips[gi.equipType].type != GameItem.TYPE_NULL){
                                bag.addElement(World.player.playerEquips[gi.equipType]);
                            }

                            World.player.playerEquips[gi.equipType] = gi;
                            bag.removeElement(gi);

                            requestChangeEquips();

                            World.setGameState(null);
                        }

                        itemShowUseMenu = false;
                        itemShowCurrentEquip = false;
                        itemShowInfo = false;
                        repaintNextTime = true;
                    }else if(cmd.equals("取消丢弃")){
                        itemMenuSelected = itemDropItemConfirmOldSelected;
                        buildEditItemMenu();
                    }else if(cmd.equals("确认丢弃")){
                        //确认丢弃
                        
                        if(gi.type == GameItem.TYPE_EQUIP){
                            World.requestUseItem(gi, GameItem.USETYPE_THROW, selectNum);
                            GameItem throwItem = new GameItem(gi.type);
                            throwItem.itemId = gi.itemId;
                            throwItem.name = gi.name;
                            throwItem.id = gi.id;
                            throwItem.count = (short)-1;
                            World.player.addItemToBag(throwItem);
                            itemShowUseMenu = false;
                            itemShowThrowCount = false;
                        }else{
                            World.requestUseItem(gi, GameItem.USETYPE_THROW, gi.count);
                            bag.removeElement(gi);
                        }

                        itemMenuSelected = itemDropItemConfirmOldSelected;
                        buildEditItemMenu();
                        itemShowUseMenu = false;
                    }
                }
            }else{
                buildEditItemMenu();
            }

            repaintNextTime = true;
        }else if(World.isKeyPressed(World.UP_PRESSED, true)){

            if(itemShowUseMenu){
                itemMenuSelected--;

                if(itemMenuSelected < 0){
                    itemMenuSelected = (byte)(itemMenu.length - 1);
                }
            }else{
                itemSelected--;
                if(itemSelected < 0){
                    itemSelected = (byte)(getCurrentBag().size() - 1);
                    if(itemSelected >= getCurrentBag().size()){
                        itemSelected = (byte)(getCurrentBag().size() - 1);
                    }
                    if(itemSelected < 0){
                        itemSelected = 0;
                    }
                }
            }
            repaintNextTime = true;
        }else 
        //#if TouchScreen == true
            if(World.isKeyPressed(World.LEFT_PRESSED, true) || leftKeyDirectChoose){
            	//#else
            	//# if(World.isKeyPressed(World.LEFT_PRESSED, true)){
            	//#endif 
        	//if(World.isKeyPressed(World.LEFT_PRESSED, true)){
            itemBagSelected--;
            if(itemBagSelected < 0)
                itemBagSelected = (byte)(BAGNAME.length - 1);
            if(itemShowUseMenu){
                itemShowUseMenu = false;
                itemMenuSelected = 0;
            }
            repaintNextTime = true;
        }else if(World.isKeyPressed(World.KEY_STAR_PRESSED, true)){
            if(!itemShowUseMenu){
                itemShowInfo = !itemShowInfo;
                itemShowCurrentEquip = false;
            }
            repaintNextTime = true;
        }else 
        	//#if TouchScreen == true
            if(World.isKeyPressed(World.RIGHT_PRESSED, true) || rightKeyDirectChoose){
            	//#else
            	//# if(World.isKeyPressed(World.RIGHT_PRESSED, true)){
            	//#endif 
        	//if(World.isKeyPressed(World.RIGHT_PRESSED, true)){
            itemBagSelected++;
            if(itemBagSelected == BAGNAME.length)
                itemBagSelected = 0;
            if(itemShowUseMenu){
                itemShowUseMenu = false;
                itemMenuSelected = 0;
            }
            repaintNextTime = true;
        }else if(World.isKeyPressed(World.DOWN_PRESSED, true)){
            if(itemShowUseMenu){
                itemMenuSelected++;
                if(itemMenuSelected == itemMenu.length)
                    itemMenuSelected = 0;
            }else{
                itemSelected++;
                if(itemSelected == getCurrentBag().size()){
                    itemSelected = 0;
                }
            }
            repaintNextTime = true;
        }else if(World.isKeyPressed(World.SOFT_LAST_PRESSED, true)){
            if(itemShowUseMenu){
                if(!itemDropItemConfirm){
                    itemShowUseMenu = false;
                }else{
                    itemMenuSelected = itemDropItemConfirmOldSelected;
                    buildEditItemMenu();
                }
            }else{
                World.setGameState(null);

            }

            repaintNextTime = true;
        }else if(World.isKeyPressed(World.KEY_POUND_PRESSED, true)){
            if(!itemShowUseMenu){
                itemShowInfo = false;

                Vector bag = getCurrentBag();

                if(bag.size() > 0){
                    GameItem gi = (GameItem)bag.elementAt(itemSelected);

                    if(gi.type == GameItem.TYPE_EQUIP){
                        itemShowCurrentEquip = !itemShowCurrentEquip;
                    }
                }
            }

            repaintNextTime = true;
        }
    }

    public Vector chatList = null;
    public int chatListLines = 0;

    private void initChatList(int type){
        Vector vec = null;

        if(type == 0){
            vec = World.net_chat_low_priority_message;
        }else{
            vec = World.net_chat_high_priority_message;
        }

        chatList = new Vector(vec.size());

        for(int i = 0; i < vec.size(); i++){
            GameEvent chat = (GameEvent)vec.elementAt(i);
            GameEvent tmp = new GameEvent(GameEvent.NET_CHAT_MESSAGE, 1, 0);

            tmp.idata[0] = ImageSet.COLOR_TABLE[chat.idata[3]];
            String[] ss = World.splitString(chat.sdata[0] + "：" + chat.sdata[1], World.viewWidth - SCREEN_MARGIN * 2 - EDGE_WIDTH * 2 - BOX_MARGIN * 2, font);
            chatListLines += ss.length;
            tmp.sdata = ss;

            chatList.insertElementAt(tmp, 0);
        }
    }

    private void cycleChatList(){
    	  //#if TouchScreen == true
        int pressedButton = StaticUtils.getPressedButton();
        if(-1 != pressedButton && pressedButton>=2000){
        	if(pressedButton-2000 != _index){
        		_index = 1 - _index;
        		initChatList(_index);
        	}
        }
        //#endif 
        if(World.isKeyPressed(World.UP_PRESSED, true)){
            if(_scroll > 0){
                _scroll -= 20;
                _scroll = _scroll > 0? _scroll: 0;

            }
        }else if(World.isKeyPressed(World.DOWN_PRESSED, true)){
            int h = World.viewHeight - TITLE_HEIGHT - 3 - TAB_HEIGHT - SCREEN_MARGIN - EDGE_WIDTH * 2 - BOX_MARGIN * 2;
            int maxScroll = chatListLines * CHAR_HEIGHT - h;

            if(_scroll < maxScroll){
                _scroll += 20;
                _scroll = Math.min(_scroll, maxScroll);
            }
        }else if(World.isKeyPressed(World.LEFT_PRESSED, true)){
            if(_index == 1){
                _index = 0;
                initChatList(_index);
            }
        }else if(World.isKeyPressed(World.RIGHT_PRESSED, true)){
            if(_index == 0){
                _index = 1;
                initChatList(_index);
            }
        }else if(World.isKeyPressed(World.SOFT_LAST_PRESSED, true)){
            World.setGameState(null);
        }
    }

    public static void drawTab(Graphics g, String[] tabs, int x, int y, int w_char, int index){
        for(int i = 0; i < tabs.length; i++){
            //#if font == native
            int w = tabs[i].length() * CHAR_WIDTH + 6;
            //#elif font == image
            //# int w = tabs[i].length()*12+6;
            //#endif
            drawBoxWithString(g, x, y, w, TAB_HEIGHT, tabs[i], index == i);
            //#if TouchScreen == true
            StaticUtils.addButton(2000+i, x, y, w, TAB_HEIGHT);
            //#endif
            x += w;
        }
    }

    public static void drawBoxWithString(Graphics g, int x, int y, int w, int h, String s, boolean selected){
        drawEdge(g, 0, x, y, w, h, true, EDGE_COLOR[4]);
        drawString(g, s, x + EDGE_WIDTH + BOX_MARGIN, y + EDGE_WIDTH + BOX_MARGIN, selected);
    }

    private void drawReconnect(Graphics g){
        Vector vec = World.formatString(_message, World.viewWidth - 20, font);
        Object[] obj = new Object[vec.size()];
        vec.copyInto(obj);
        drawMsgTip(g, -1, -1, obj, null, null, BTNTYPE_NONE);
    }

    private void drawChatList(Graphics g){
        drawTitle(g, "查看消息");

        int clipX = g.getClipX();
        int clipY = g.getClipY();
        int clipW = g.getClipWidth();
        int clipH = g.getClipHeight();
        int x = SCREEN_MARGIN;
        int y = TITLE_HEIGHT + 3;

        drawTab(g, new String[]{
                        "普通", "优先"
        }, x, y, 2, _index);
        y += TAB_HEIGHT;

        int w = World.viewWidth - SCREEN_MARGIN * 2;
        int h = World.viewHeight - TITLE_HEIGHT - 3 - SCREEN_MARGIN - TAB_HEIGHT;

        //drawBox(g, x, y, w, h);
        drawEdge(g, 0, x, y, w, h, true, EDGE_COLOR[4]);
        g.setClip(x + EDGE_WIDTH, y + EDGE_WIDTH, w - EDGE_WIDTH * 2, h - EDGE_WIDTH * 2);
        y += (BOX_MARGIN + EDGE_WIDTH);
        y -= _scroll;

        for(int i = 0; i < chatList.size(); i++){
            GameEvent item = (GameEvent)chatList.elementAt(i);
            String[] ss = item.sdata;

            for(int j = 0; j < ss.length; j++){
                drawShadowString(g, ss[j], x + EDGE_WIDTH + BOX_MARGIN, y + 1, Graphics.LEFT | Graphics.TOP, item.idata[0]);
                y += CHAR_HEIGHT;
            }
        }

        g.setClip(clipX, clipY, clipW, clipH);
        drawButtons(g, BUTTON_RIGHT, false);
    }

    private void cycleTaskUI(){
        if(taskUIListNeedScroll){
            if(taskUIListScrollDir == 0){
                taskUIListScrollOffset++;

                if(taskUIListScrollOffset > taskUIListScrollMax){
                    taskUIListScrollOffset = taskUIListScrollMax;
                    taskUIListScrollDir = 1;
                }
            }else{
                taskUIListScrollOffset--;

                if(taskUIListScrollOffset < 0){
                    taskUIListScrollOffset = 0;
                    taskUIListScrollDir = 0;
                }
            }
        }

        if(taskUITitleNeedScroll){
            if(taskUITitleScrollDir == 0){
                taskUITitleScrollOffset++;

                if(taskUITitleScrollOffset > taskUITitleScrollMax){
                    taskUITitleScrollOffset = taskUITitleScrollMax;
                    taskUITitleScrollDir = 1;
                }
            }else{
                taskUITitleScrollOffset--;

                if(taskUITitleScrollOffset < 0){
                    taskUITitleScrollOffset = 0;
                    taskUITitleScrollDir = 0;
                }
            }
        }

        if(taskUIType == TASK_UI_CONTENT || taskUIList != null){
            if(World.isKeyPressed(World.UP_PRESSED, true)){
                if(_scroll > 0){
                    _scroll -= 10;
                    _scroll = _scroll > 0? _scroll: 0;
                    repaintNextTime = true;

                }
            }else if(World.isKeyPressed(World.DOWN_PRESSED, true)){
                int h = World.viewHeight - SCREEN_MARGIN - TITLE_HEIGHT - (EDGE_WIDTH + BOX_MARGIN) * 2 - 3;
                int maxScroll = taskUIList.length * CHAR_HEIGHT - h;

                if(_scroll < maxScroll){
                    _scroll += 10;
                    _scroll = Math.min(_scroll, maxScroll);
                    repaintNextTime = true;
                }
            }
        }

        if(waitType != WAIT_NONE){
            if(waitType == WAIT_VIEW_ATTACHMENT){
                if(showGameItem.type != GameItem.TYPE_MONEY){
                    if(showGameItem.request_desc && showGameItem.download_desc_state < 2){
                        repaintNextTime = true;
                    }
                }
            }
        }
        //#if TouchScreen == true
        int focusButton = StaticUtils .getDragOverButton();
        int id = 0;
        autoArrive=false;
        //int  firstPressButton = 0;
        if(focusButton != -1 && focusButton>=2000){
        	int select = 0;
        	if(!taskUICommandShowing){
        		select = taskUIListSelect;
	            StaticUtils.pressedDoubleButton = true;
        	}else{
        		//focusButton = 2000;
        		select = taskUICommandCurrentSelect;
        		//StaticUtils.pressedDoubleButton = false;
        	}
        		
        		if( focusButton-2000>select && pressedVM){
        			id = -2;
        		}else if(focusButton-2000<select && pressedVM){
        			id = -1;
        		}else {
        			autoArrive= true;
        		}
        		World.instance.keyPressed(id);
        		/*id = World.keyToGame(id);

                if(id >= 0){
                		World.keyFlag |= 3L << (id << 1);
                }*/
        }
        int pressedButton = StaticUtils.getPressedButton();
        if(-1 != pressedButton && pressedButton>=2000){
        	if(!taskUICommandShowing){//需要二次点击的
	        	if( !taskUIListSelectTarget){//需要二次点击的
	        		//taskUIListSelect = pressedButton - 2000;
	        		/*id = 0 ;
	        		if( pressedButton-2000>taskUIListSelect){
	        			id = -2;
	        		}else if(pressedButton-2000<taskUIListSelect)
	        		{
	        			id = -1;
	        		}
	        		id = World.keyToGame(id);

	                if(id >= 0){
	                	World.keyFlag |= 3L << (id << 1);
	                }
	               
	                */
	        		 taskUIListSelectTarget = true;
	        		 pressedVM = true;

	        	}else{//确认是第二次的选中，模拟按下确认键
	        		if(taskUIListSelect == pressedButton - 2000 ){
	        			taskUIListSelectTarget =false;
	        			pressedVM = false;
		        		StaticUtils.setFocusButton(-1);
	        			World.instance.keyPressed(-5);
	            		/*
		        		id = World.keyToGame(-5);
		        		if(id >= 0){
		                    World.keyFlag |= 3L << (id << 1);   
		                }
		        		
		        		taskUIListSelectTarget =false;
		        		firstPressed = false;
		        		StaticUtils.setFocusButton(-1);
		        		*/
	        		}
	        	}
        	}else{//一次确认即可
        		firstPressed = true;
        		pressedVM = true;
        	}
        }else{
        	if(autoArrive==true){
        		if(firstPressed){
        			//System.out.println("天才");
        			World.instance.keyPressed(-5);
            		/*
	        		id = World.keyToGame(-5);
	        		if(id >= 0){
	                    World.keyFlag |= 3L << (id << 1);   
	                }
	        		*/
        			firstPressed = false;
        			pressedVM = false;
        		}
        	}
        }
        //#endif
    }

    private static void setTaskUIWait(boolean needExit, boolean canCancel, int backState, int type, String msg){
        World.gameState.needExit = needExit;
        World.gameState.canCancel = canCancel;
        World.gameState.backState = backState;
        World.gameState.waitType = (byte)type;
        World.gameState._message = msg;
        World.gameState.showType = SHOW_MESSAGE;

        if(type == GameState.WAIT_WAITRESULT){
            World.gameState.clock = System.currentTimeMillis();
        }else if(type == GameState.WAIT_NONE){
            World.clearKeyStates();
        }
    }

    public void run(){
        try{
            switch(type){
                case STATE_OPENURL:
                    HttpConnection httpConnection = null;
                    int code = 0;
                    try{
                        httpConnection = UWAPSegment.getConnection(_message, UWAPSegment.useProxyGlobal);
                        code = httpConnection.getResponseCode();
                        
                        //#if (Revision == PIP) || (Revision == SOHU) || (Revision == DOWNJOY) || (Revision == JIANGSUN)
                        if(_message.equals(pushUrl)){
                            DataInputStream in = httpConnection.openDataInputStream();
                            byte[] data = World.getBytesFromInput(in);

                            try{
                                pushString = new String(data, "UTF-8");
                            }catch(Exception e){
                                //#debug
                                e.printStackTrace();
                                pushString = null;
                            }
                        }
                        //#endif
                    }catch(Exception ex2){
                    }finally{
                        if(httpConnection != null){
                            try{
                                httpConnection.close();
                            }catch(IOException ex3){
                            }
                        }
                        //if(code != 200){
                        //    exitToGameMenu("网络错误，稍候再试");
                        //}
                    }

                    break;
                case STATE_GAMEMENU:
                    runGameMenu();

                    break;
                case STATE_LOADING:

                    switch(subState){
                        case SS_LOADING_BATTLE:
                            runLoadBattle();
                            break;
                        case SS_LOADING_SERVERBATTLE:
                            runLoadServerBattle();
                            break;
                        case SS_LOADING_LEAVEBATTLE:
                            runLeaveBattle();
                            break;
                        default:
                            runLoading();
                            break;
                    }
                    break;
                //#if Revision == QQ
                //#if (Directory == SE-K300) || (Directory == SE-K500) || (Directory == SE-K700)
                //#else
                case STATE_QQBILL:
                	runQQBill();
                	break;
                //#endif
                //#endif
                case STATE_TESTNET:
                    runTestNet();
                    break;
                case STATE_TESTSERVERLIST:
                    runTestServerList();
                    break;
            }
        }catch(Exception ex){
            //#debug
            ex.printStackTrace();
        }
    }
    
    private void runTestServerList() {
        boolean useProxy = canCancel;       // 重用canCancel表示是否使用代理
        HttpConnection httpConnection = null;

        // 建立连接
        for (int j = 0; j < 10 && testNetResult == 0; j++) {
            try {
                httpConnection = UWAPSegment.getConnection(entryURL, useProxy);
                httpConnection.setRequestMethod(HttpConnection.GET);
    
                int code = httpConnection.getResponseCode();
    
                // 处理返回结果
                if (code == 200 || code == 302) {
                    DataInputStream in = httpConnection.openDataInputStream();
                    byte[] data = World.getBytesFromInput(in);
                    String s = null;
    
                    try {
                        s = new String(data, "UTF-8");
                    } catch(Exception e) {
                        //#debug
                        e.printStackTrace();
                        s = "xml";
                    }
    
                    in.close();
    
                    if (s.indexOf("xml") >= 0 || s.indexOf("wml") >= 0) {
                        // 处理移动PUSH页面
                        continue;
                    } else {
                        // 查看页面是否合法的服务器列表
                        String tmp = s;
                        int idx = tmp.indexOf("\n");
                        if (idx > 0) {
                            tmp = s.substring(0, idx);
                        }
                        if (tmp.length() > 0 && tmp.indexOf("=") < 0) {
                            continue;
                        }
                    }
    
                    //#mdebug
                    if (s.length() > 0) {
                        s += "\n";
                    }
                    s ="测试服务器186=socket://218.206.80.186:7799,0\n测试服务器54=socket://192.168.0.54:2259,0\n本机服务器=socket://192.168.0.181:7777,0\n"+s;
                    //#enddebug
    
                    if (testNetResult == 0) {
                        _message = splitServerList(s);
                        testNetResult = 1;
                    }
    
                    break;
                } else {
                    throw new IOException();
                }
            } catch (Exception ex) {
                // 连接失败
                testNetResult = 2;
                break;
            } finally {
                if(httpConnection != null) {
                    try{
                        httpConnection.close();
                    } catch(IOException ex3) {
                    }
                    httpConnection= null;
                }
            }
        }
    }
    
    private void runTestNet() {
        try {
            String url = _message;
            if (url.startsWith("socket")) {
                testNetConnection = new UWAPSocketConnection(url);
            }
            //#if Revision != QQ 
            else{
                testNetConnection = new UWAPHttpConnection(url);
            }
            //#endif
            testNetResult = 1;
        } catch(Exception e) {
            testNetResult = 2;

            //#debug
            e.printStackTrace();
        } finally {
            if (testNetResult == 2) {
                if(testNetConnection != null){
                    testNetConnection.close();
                }
            }
        }
    }

    public static void downloadServerList() throws Exception {
        // 从RMS中取优先连接设置
        byte[] savedData = World.getData("proxy", (byte)0);
        //#if UseProxy == false
        boolean initValue = false;
        //#else
        //# boolean initValue = true;
        //#endif
        boolean useProxy = initValue;
        if (savedData != null &&  savedData.length == 1) {
            useProxy = (savedData[0] == 1);
        }
        
        // 尝试使用优先连接设置
        GameState t = new GameState(STATE_TESTSERVERLIST);
        t.canCancel = useProxy;
        long st = System.currentTimeMillis();
        new Thread(t).start();
        while (t.testNetResult == 0) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }

            if (System.currentTimeMillis() - st > 90000) {
                t.testNetResult = 2;
            }
        }
        
        // 如果超时，提醒用户使用备用网络设置
        if (t.testNetResult != 1) {
            World.saveData("proxy", new byte[] { (byte)(useProxy ? 0 : 1) }, (byte)0);
            String firstName;
            String secondName;
            if (useProxy == initValue) {
                // 主要网络设置失败
                firstName = "缺省";
                secondName = "备用";
            } else {
                // 次要网络设置失败
                firstName = "备用";
                secondName = "缺省";
            }
            t = new GameState(STATE_GETLISTFAIL);
            t._message = "使用" + firstName + "网络连接方式读取服务器列表失败，将启用" + secondName + 
                    "网络连接方式。游戏会自动关闭，需要您手动打开游戏重试。\n注：如多次尝试均无法登录，建议您建议关闭手机重启以确保新的网络设置生效。";
            t.infoStrings = World.splitString(t._message, World.viewWidth - SCREEN_MARGIN * 2, font);
            World.setGameState(t);
            throw new Exception(t._message);
        } else {
            // 如果成功，把此设置保存为优先设置
            World.saveData("proxy", new byte[] { (byte)(useProxy ? 1 : 0) }, (byte)0);
            UWAPSegment.useProxyGlobal = useProxy;
            
            if (serverGroups.length == 0) {
                if (t._message.length() > 0) {
                    // 维护信息
                    throw new Exception(t._message);
                } else {
                    throw new Exception("没有可用服务器");
                }
            }
        }
    }

    public static void initServerList(){
        serverGroups = new String[0];
        serverURLs = new String[0][];
        serverLoads = new byte[0][];
        byte[] tmp = World.getData(World.RMS_DATA, (byte)5);

        if(tmp == null || tmp.length <= 0){
            serverName = "";
        }else{
            serverName = World.bytesToString(tmp);
        }
    }
    
    public static String decodeASCII(String str) {
        StringBuffer buf = new StringBuffer();
        int count = str.length();
        for (int i = 0; i < count; i++) {
            char ch = str.charAt(i);
            if (ch == '\\' && i < count - 5 && str.charAt(i + 1) == 'u') {
                try {
                    int cc = Integer.parseInt(str.substring(i + 2, i + 6), 16);
                    buf.append((char)cc);
                } catch (Exception e) {
                }
                i += 5;
            } else {
                buf.append(ch);
            }
        }
        return buf.toString();
    }

    public static String splitServerList(String list){
        list = decodeASCII(list);
        list = decodeASCII(list);
        
        initServerList();

        if(list == null || list.trim().length() == 0){
            return null;
        }

        if(list.indexOf(maintenanceCode) >= 0){
            int idx = list.indexOf(maintenanceCode);

            String tmp = list.substring(idx + maintenanceCode.length());

            idx = tmp.indexOf("\n");

            if(idx >= 0){
                tmp = tmp.substring(0, idx);
            }

            return tmp;
        }
        
        //#if (Revision == PIP) || (Revision == SOHU) || (Revision == DOWNJOY) || (Revision == JIANGSUN)
        // Light: PIP版本也从分配器下载移动平台登录地址
        cmccLoginURL1 = null;
        String tmpcmcc = list;
        int cmccIdx = list.indexOf("cmccLogin=");
        String cmccStr;
        if (cmccIdx >= 0) {
            list = list.substring(0, cmccIdx);
            cmccStr = tmpcmcc.substring(cmccIdx + "cmccLogin=".length());
            cmccIdx = cmccStr.indexOf("\n");
            if(cmccIdx >= 0){
                cmccStr = cmccStr.substring(0, cmccIdx);
            }
            cmccLoginURL1 = cmccStr;
        }
        //#endif

        //#if Revision == CMCC || (Revision == JIANGSUNCMCC) 
        //# cmccLoginURL1 = null;
        //# CMCCExitCanvas.updateUrl1 = null;
        //# String tmpcmcc = list;
        //# int cmccIdx = list.indexOf("cmccLogin=");
        //# String cmccStr;
        //# if (cmccIdx >= 0){
            //# list = list.substring(0, cmccIdx);
            //# cmccStr = tmpcmcc.substring(cmccIdx + "cmccLogin=".length());
            //# cmccIdx = cmccStr.indexOf("\n");
            //# if(cmccIdx >= 0){
                //# cmccStr = cmccStr.substring(0, cmccIdx);
            //# }
            //# cmccLoginURL1 = cmccStr;
        //# }
        //# cmccIdx = tmpcmcc.indexOf("cmccUpdate=");
        //# if (cmccIdx >= 0) {
            //# cmccStr = tmpcmcc.substring(cmccIdx + "cmccUpdate=".length());
            //# cmccIdx = cmccStr.indexOf("\n");
            //# if(cmccIdx >= 0){
                //# cmccStr = cmccStr.substring(0, cmccIdx);
            //# }
            //# CMCCExitCanvas.updateUrl1 = cmccStr;
        //# }
        //#endif
        
        int groupCount = 0;
        String tmp = list;
        int idx = tmp.indexOf('=');

        while(idx >= 0){
            groupCount++;
            tmp = tmp.substring(idx + 1);
            idx = tmp.indexOf('=');
        }

        serverGroups = new String[groupCount];

        tmp = list;
        String firstServerGroup="";
        tempServerName = serverName ;
        int serverPositionIndex=-1;
        for(int i = 0; i < groupCount; i++){
            idx = tmp.indexOf('\n');

            if(idx >= 0){
                serverGroups[i] = tmp.substring(0, idx);
                tmp = tmp.substring(idx + 1);
            }else{
                serverGroups[i] = tmp;
            }
            //添加第一个服务器的连接数据
        	   if(serverName != null && serverName.trim().length() > 0){
        		   serverPositionIndex=serverGroups[i].indexOf(serverName);
	        	   if(serverPositionIndex>=0){
	        		   serverPositionIndex = serverGroups[i].indexOf('=');
	        		   String temps="";
	        		   temps= serverGroups[i].substring(0, serverPositionIndex);
	        		   if(temps.equals(serverName)){
	        			   firstServerGroup = "上次登录:"+serverGroups[i];
	        		   }
	        	   }
        	   }
        }
      //添加服务器名称换后的 支持
        if(!(firstServerGroup!=null && firstServerGroup.equals(""))){
        	groupCount++;
        	serverGroups = new String[groupCount];
            tmp = list;

            for(int i = 0; i < groupCount; i++){
                idx = tmp.indexOf('\n');

                if(idx >= 0){
                    serverGroups[i] = tmp.substring(0, idx);
                    tmp = tmp.substring(idx + 1);
                }else{
                    serverGroups[i] = tmp;
                }
            }
          //排序添加第一组上次登陆的数据
            for(int i = groupCount-2; i >= 0; i--){
	        	serverGroups[i+1]=serverGroups[i];
	        }
	        serverGroups[0]=firstServerGroup;
        }
        
        serverURLs = new String[groupCount][];
        serverLoads = new byte[groupCount][];
        for(int i = 0; i < serverGroups.length; i++){
            tmp = serverGroups[i].trim();

            idx = tmp.indexOf('=');
            serverGroups[i] = tmp.substring(0, idx);
            tmp = tmp.substring(idx + 1);

            String tmp1 = tmp;

            idx = tmp1.indexOf(',');
            int urlCount = 0;

            while(idx >= 0){
                tmp1 = tmp1.substring(idx + 1);
                idx = tmp1.indexOf(',');

                if(idx >= 0){
                    tmp1 = tmp1.substring(idx + 1);
                    idx = tmp1.indexOf(',');
                }

                urlCount++;
            }

            serverURLs[i] = new String[urlCount];
            serverLoads[i] = new byte[urlCount];

            for(int j = 0; j < urlCount; j++){
                idx = tmp.indexOf(',');

                serverURLs[i][j] = tmp.substring(0, idx);
                tmp = tmp.substring(idx + 1);

                idx = tmp.indexOf(',');

                if(idx >= 0){
                    serverLoads[i][j] = (byte)Integer.parseInt(tmp.substring(0, idx));
                    tmp = tmp.substring(idx + 1);
                }else{
                    serverLoads[i][j] = (byte)Integer.parseInt(tmp);
                }
            }
        }

        byte[] tmpData = World.getData(World.RMS_DATA, (byte)4);
        String oldUrl = null;

        if(tmpData != null){
            oldUrl = World.bytesToString(tmpData);

            if(oldUrl != null && oldUrl.startsWith("http")){
                oldUrl = null;
            }
        }

        //#mdebug
        System.out.println("saved URL : " + oldUrl);

        for(int i = 0; i < serverGroups.length; i++){
            System.out.println(serverGroups[i]);

            for(int j = 0; j < serverURLs[i].length; j++){
                System.out.println(serverURLs[i][j]);
                System.out.println(serverLoads[i][j]);
            }
        }
        //#enddebug

        for(int i = 0; i < serverGroups.length; i++){
            for(int j = 0; j < serverURLs[i].length - 1; j++){
                for(int k = j + 1; k < serverURLs[i].length; k++){
                    if(oldUrl != null && serverURLs[i][k].equals(oldUrl) && serverLoads[i][k] != 3){
                        switchServer(i, j, k);
                    }else{
                        if(serverLoads[i][j] > serverLoads[i][k] && (oldUrl == null || !serverURLs[i][k].equals(oldUrl)) && !serverURLs[i][k].startsWith("http")){
                            switchServer(i, j, k);
                        }
                    }
                }
            }
        }

        //#mdebug
        System.out.println();

        for(int i = 0; i < serverGroups.length; i++){
            System.out.println(serverGroups[i]);

            for(int j = 0; j < serverURLs[i].length; j++){
                System.out.println(serverURLs[i][j]);
                System.out.println(serverLoads[i][j]);
            }
        }
        //#enddebug

        return null;
    }

    public static void switchServer(int i, int j, int k){
        byte t1 = serverLoads[i][j];
        String t2 = serverURLs[i][j];

        serverLoads[i][j] = serverLoads[i][k];
        serverURLs[i][j] = serverURLs[i][k];

        serverLoads[i][k] = t1;
        serverURLs[i][k] = t2;
    }
    
    public static String getClientVersion() {
        //#if Revision == QQ
        //# return versionString + "-" + channelCode;
        //#else
        return versionString + "-" + channelCode + (downloadCode.length() > 0? "-" + downloadCode : "");
        //#endif
    }

    public void runGameMenu(){
        switch(subState){
            case SS_GETSERVERLIST:
                try{
                    downloadServerList();
                    if(thread != null){
                        subState = SS_SERVERLIST;
                        thread = null;
                    }
                }catch(Exception ex){
                    _message = ex.getMessage();
                }
                break;
            case SS_LOGINING:
                try{
                    //#if Revision == CMCC || (Revision == JIANGSUNCMCC) 
                	//# if (!cmccLogin() && !(name.startsWith("test") || name.startsWith("gm"))) {
                		//# _message = "登录平台失败。请您使用“移动梦网(CMWAP)”方式连接，设置说明请用浏览器登陆游戏专区查看";
                		//# break;
                	//# }
                    //# phoneLogin();
                    //# GameState.createConnection();
                    //# serial = World.sendRequest(CONN_LOGIN, new Object[]{
                            //# name, password, getModel(), getClientVersion(), cmccUserID, cmccKey, clientPhone
                    //# }, false);
                    //#elif Revision == QQ
                    //# phoneLogin();
                    //# GameState.createConnection();
                    //# serial = World.sendRequest(CONN_LOGIN, new Object[]{
                    //# name, password, getModel(), getClientVersion(), iTimesMIDlet.getQQId(), iTimesMIDlet.getSID(), clientPhone
                    //# }, false);
                	//#elif Revision == JIANGSUN
                	//# cmccLogin();
                	//# GameState.createConnection();
                	//# serial = World.sendRequest(CONN_LOGIN, new Object[]{
                	//# name, password, getModel(), getClientVersion(), cmccUserID, cmccKey, clientPhone
                	//# }, false);
                    //#else
                    cmccLogin();
                    phoneLogin();
                    GameState.createConnection();
                    serial = World.sendRequest(CONN_LOGIN, new Object[]{
                        name, password, getModel(), getClientVersion(), cmccUserID, cmccKey, clientPhone
                    }, false);
                    //#endif
                    //serial = requestLogin(name, password);
                }catch(Exception ex){
                    //#debug
                    ex.printStackTrace();
                    _message = "网络错误，稍候再试";
                }

                break;
            case SS_REGISTERING: {
                try{
                    password = "";
                    actorName = "";

                    //#if Revision == CMCC || (Revision == JIANGSUNCMCC) 
                    //# if (!cmccLogin()) {
                    	//# _message = "登录平台失败。请您使用“移动梦网(CMWAP)”方式连接，设置说明请用浏览器登陆游戏专区查看";
                    	//# break;
                    //# }
                    //# phoneLogin();
                    //# GameState.createConnection();
                    //# serial = World.sendRequest(CONN_ACCOUNTREG, new Object[]{
                            //# name, "", "", getModel() + jvmCode, getClientVersion(), new Boolean(true),
                            //# cmccUserID, cmccKey, clientPhone
                    //# }, false);
                    //#elif Revision == QQ
                    //# phoneLogin();
                    //# GameState.createConnection();
                    //# serial = World.sendRequest(CONN_ACCOUNTREG, new Object[]{
                    //# name, "", "", getModel() + jvmCode, getClientVersion(), new Boolean(true),
                    //# iTimesMIDlet.getQQId(), iTimesMIDlet.getSID(), clientPhone
                    //# }, false);
                    //#elif Revision == JIANGSUN
                    //# cmccLogin();
                    //# GameState.createConnection();
                    //# serial = World.sendRequest(CONN_ACCOUNTREG, new Object[]{
                    //# name, phone, recommend, getModel() + jvmCode, getClientVersion(), new Boolean(true),
                    //# cmccUserID, cmccKey, clientPhone
                    //# }, false);
                    //#else
                    cmccLogin();
                    phoneLogin();
                    GameState.createConnection();
                    serial = World.sendRequest(CONN_ACCOUNTREG, new Object[]{
                        name, phone, recommend, getModel() + jvmCode, getClientVersion(), new Boolean(true),
                        cmccUserID, cmccKey, clientPhone
                    }, false);
                    //#endif

                    World.RecordPreousDisplay(World.instance);
                }catch(Exception ex1){
                    //#debug
                    ex1.printStackTrace();
                    _message = "网络错误，稍候再试";
                }
            }

                break;
            case SS_FAST_REG: {
                try{
                    _message = "正在连接...";

                    //#if Revision == CMCC || (Revision == JIANGSUNCMCC) 
                    //# if (!cmccLogin()) {
                    	//# _message = "登录平台失败。请您使用“移动梦网(CMWAP)”方式连接，设置说明请用浏览器登陆游戏专区查看";
                    	//# break;
                    //# }
                    //# phoneLogin();
                    //# GameState.createConnection();
                    //# World.sendRequest(CONN_QUICK_REG, new Object[]{
                                    //# "", getClientVersion(), getModel() + jvmCode,
                                    //# cmccUserID, cmccKey, clientPhone
                    //# }, false);
                    //#elif Revision == QQ
                    //# phoneLogin();
                    //# GameState.createConnection();
                    //# World.sendRequest(CONN_QUICK_REG, new Object[]{
                        //# "", getClientVersion(), getModel() + jvmCode,
                        //# iTimesMIDlet.getQQId(), iTimesMIDlet.getSID(), clientPhone
                    //# }, false);
                  //#elif Revision == JIANGSUN
                  //# cmccLogin();
                  //# GameState.createConnection();
                  //# World.sendRequest(CONN_QUICK_REG, new Object[]{
                  //#  "", getClientVersion(), getModel() + jvmCode, cmccUserID, cmccKey, clientPhone
                  //#  }, false);
                    //#else
                    cmccLogin();
                    phoneLogin();
                    GameState.createConnection();
                    World.sendRequest(CONN_QUICK_REG, new Object[]{
                        "", getClientVersion(), getModel() + jvmCode, cmccUserID, cmccKey, clientPhone
                    }, false);
                    //#endif
                }catch(Exception ex2){
                    //#debug
                    ex2.printStackTrace();

                    _message = "网络错误，稍候再试";
                }
            }

                break;
            case SS_FAST_LOGIN:
                try{
                    _message = "正在登录帐户...";

                    //#if Revision == CMCC || (Revision == JIANGSUNCMCC) 
                    //# if (!cmccLogin() && !(name.startsWith("test") || name.startsWith("gm"))) {
                        //# _message = "登录平台失败。请您使用“移动梦网(CMWAP)”方式连接，设置说明请用浏览器登陆游戏专区查看";
                        //# break;
                    //# }
                    //# phoneLogin();
                    //# GameState.createConnection();
                    //# serial = World.sendRequest(CONN_LOGIN, new Object[]{
                            //# name, password, getModel(), getClientVersion(), cmccUserID, cmccKey, clientPhone
                    //# }, false);
                    //#elif Revision == QQ
                    //# phoneLogin();
                    //# GameState.createConnection();
                    //# serial = World.sendRequest(CONN_LOGIN, new Object[]{
                    //# name, password, getModel(), getClientVersion(), iTimesMIDlet.getQQId(), iTimesMIDlet.getSID(), clientPhone
                    //# }, false);
                    //#elif Revision == JIANGSUN
                    //# cmccLogin();
                    //# GameState.createConnection();
                    //# serial = World.sendRequest(CONN_LOGIN, new Object[]{
                    //# name, password, getModel(), getClientVersion(), cmccUserID, cmccKey, clientPhone
                    //#  }, false);
                    //#else
                    cmccLogin();
                    phoneLogin();
                    GameState.createConnection();
                    serial = World.sendRequest(CONN_LOGIN, new Object[]{
                        name, password, getModel(), getClientVersion(), cmccUserID, cmccKey, clientPhone
                    }, false);
                    //#endif

                    //#debug
                    System.out.println("fast login : " + name + " , " + password + " , " + actorName);
                }catch(Exception ex){
                    //#debug
                    ex.printStackTrace();
                    _message = "网络错误，稍候再试";
                }

                break;
            case SS_FAST_GET_ACTORLIST:
                try{
                    GameState.createConnection();

                    serial = World.sendRequest(CONN_GET_ACTORLIST, new Object[]{
                                    name, password
                    }, false);
                }catch(Exception ex1){
                    //#debug
                    ex1.printStackTrace();
                    _message = "网络错误，稍候再试";
                }

                break;
            case SS_FAST_PLAYER_LOGIN:
                try{
                    _message = "正在登录角色...";
                    GameState.createConnection();

                    if(actorNameList.size() > 0){
                        if(actorName == null || actorName.trim().length() == 0 || !actorNameList.contains(actorName)){
                            actorName = ((String[])actorList.elementAt(0))[0];
                        }

                        serial = World.sendRequest(CONN_PALYER_LOGIN, new Object[]{
                            actorName
                        }, false);
                    }else{
                        actorName = "";
                        World.saveData(World.RMS_DATA, World.stringToBytes(actorName), (byte)3);
                        _message = "您在本服没有角色";
                    }

                    World.RecordPreousDisplay(World.instance);
                }catch(Exception ex1){
                    //#debug
                    ex1.printStackTrace();
                }

                break;
            case SS_ACTOR_GET_LIST:
                try{
                    GameState.createConnection();

                    serial = World.sendRequest(CONN_GET_ACTORLIST, new Object[]{
                                    name, password
                    }, false);
                    //serial = requestGetActorList(name, password);
                    World.RecordPreousDisplay(World.instance);
                }catch(Exception ex1){
                    //#debug
                    ex1.printStackTrace();
                    _message = "网络错误，稍候再试";
                }

                break;
            case SS_ACTOR_CREATING:
                try{
                    GameState.createConnection();
                    serial = World.sendRequest(CONN_ACTORCREATE, new Object[]{
                                    actorName, new Byte((byte)actorSex), new Integer(version)
                    }, false);
                    //requestCreateActor(actorName, actorSex);
                    World.RecordPreousDisplay(World.instance);
                }catch(Exception ex1){
                    //#debug
                    ex1.printStackTrace();
                    _message = "网络错误，稍候再试";
                }

                break;
            case SS_ACTOR_LOGINING:
                try{
                    GameState.createConnection();
                    serial = World.sendRequest(CONN_PALYER_LOGIN, new Object[]{
                        actorName
                    }, false);
                    //requestActorLogin(actorName);
                    World.RecordPreousDisplay(World.instance);
                }catch(Exception ex1){
                    //#debug
                    ex1.printStackTrace();
                    _message = "网络错误，稍候再试";
                }

                break;
        }
    }

    public void runLoading(){
        try{
            short areaId = -1;

            if(loadMapId != -1){
                areaId = (short)((loadMapId >> 4) & 0x0FFF);
            }

            if(loadMapId != -1 && areaId == World.areaId){ //只是转换地图
                World.loadMap(loadMapId & 0x000F, startX, startY, isMapXY);
                World.areaId = areaId;
                World.currMapId = loadMapId;
                World.setGameState(null);

                if(World.teamLeader){
                    World.moveTeamFollowToMap(World.currMapId, World.player.x, World.player.y);
                }else if(World.teamMode){
                    MonsterSprite me = World.findTeamMember(World.player.playerID);

                    if(me.alertRange != World.currMapId){
                        World.updateTeamMemberInfo(me, World.currMapId, World.player.x, World.player.y);
                    }
                }

                return;
            }else{
                gameIsOk = false;

                World.sendRequest(GameState.CONN_GET_FILE, new Object[]{
                                getModel(), new Short((short)-1), new Short((short)1), new Short(areaId)
                }, false);

                World.player.wpList = null;
                World.player.setState(Sprite.STATE_IDLE, true);

                _message += "\n" + serverTip;
            }
        }catch(IOException ex){
            //#debug debug
            ex.printStackTrace();
            _message = ex.getMessage();
            subState = SS_LOADING_ERROR;
        }
    }

    public void runLoadBattle(){
        World world = World.instance;
        try{
            //World.info = "fm:" + Runtime.getRuntime().freeMemory();
            world.initBattleSprites(param);
            //World.info = "after initbattle";
            world.readyBattle(0);
            World.setGameState(null);
        }catch(Throwable e){
            //ex += e.toString();
            //#debug
            e.printStackTrace();
            World.nowBattle = -1;
            World.setGameState(null);

            World.instance.endBattle();
            World.showMessage("载入战斗异常"
            //#mdebug
                            + "\n" + e.toString()
            //#enddebug
                            , (byte)10);
            World.player.runAwayTime = 5000;
        }
    }

    public int loadServerBattleState;
    public int pkid;
    public int teamid;

    public static final byte LSBS_REQUEST = 0;
    public static final byte LSBS_INIT = 1;
    public static final byte LSBS_JOIN = 2;
    public static final byte LSBS_JOIN_SUCCESS = 3;
    public static final byte LSBS_JOIN_FAILED = 4;
    public static final byte LSBS_FINISHED = 5;
    public static final byte LSBS_WAITING = -1;

    public void runLoadServerBattle(){
        while(loadServerBattleState != LSBS_FINISHED){
            switch(loadServerBattleState){
                case LSBS_REQUEST:
                    serial = World.sendRequest(CONN_BATTLE_REQUEST, new Object[]{
                                    new Integer(World.teamId), new Integer(World.monsters[param].id)
                    }, false);

                    //World.requestServerBattle(param);
                    loadServerBattleState = LSBS_WAITING;
                    _message = "正在请求战斗...";
                    break;
                case LSBS_INIT:

                    break;
                case LSBS_JOIN:
                    serial = World.sendRequest(CONN_BATTLE_JOIN, new Object[]{
                                    new Integer(pkid), new Integer(World.teamId), new Integer(World.player.playerID)
                    }, false);

                    //World.requestJoinBattle(pkid, teamid);
                    //loadServerBattleState = LSBS_WAITING;
                    //_message = "准备进入战斗";
                    loadServerBattleState = LSBS_JOIN_SUCCESS;

                    break;
                case LSBS_JOIN_SUCCESS:
                    World.instance.readyBattle(pkid);
                    World.setGameState(null);
                    return;
                case LSBS_JOIN_FAILED:
                    _message = "无法进入战斗，按任意键继续";
                    World.monsters[param].visible = false;
                    if(World.isAnyKeyPressed()){
                        runLeaveBattle();
                        return;
                    }
                    break;
                case LSBS_WAITING:
                    try{
                        Thread.sleep(50);
                    }catch(Exception e){
                        //#debug
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    public void runLeaveBattle(){
        if(World.player.testDie() && !World.pkBattle){
            //角色死亡 && 非PK
            World.showDieConfirm(4000, (short)((World.areaId << 4) | (World._defaultMapId & 0xF)), World._defaultX, World._defaultY, "你已死亡, 稍候传送回复活点...");
        }

        World.player.reCalculateAttributes();

        World world = World.instance;
        world.endBattle();

        //#if LoadAllImage == FALSE
        World.clearBattleImageSet();
        World.initDefaultImageSet();
        //#endif
        
      //#if (Directory == SE-K700) || (Directory == SE-K500) || (Directory == SE-K300) || (Directory == SE-S700)
      //# World.effectImageSet = null;
      //# World.dieImageSet = null;
      //# World.attackImg = new ImageSet[5];
      //# World.attackWeaponImg = new ImageSet[5];
      //# Sprite.bufIcon = world.getImageSetFromLocal("buf");
      //# try{
      //# World.resourceImage = new ImageSet[4];

      //# for(int i = 0; i < world.resourceImage.length; i++){
      //#  	world.resourceImage[i] = world.getImageSetFromLocal("res" + i + ".p");
      //# }
      //# }catch(Exception e){
            //#debug
      //# e.printStackTrace();
      //# }
	  //#	World.taskHint = World.getImageSetFromLocal("!");
	  //#	World.doorImageSet = World.getImageSetFromLocal("door.p");
	  //#	World.bodyImg = World.getImageSetFromLocal("body.p");
	  //#	World.charImageSet = World.getImageSetFromLocal("chars.p");
      //#endif
        
        World.setGameState(null);
    }

    public static String replaceString(String in, String src, String dest){
        int idx = in.indexOf(src);
        String result = in;

        while(idx >= 0){
            String str1 = result.substring(0, idx);
            String str2 = result.substring(idx + 1);

            result = str1 + new String(dest) + str2;
            idx = result.indexOf(src);
        }

        return result;
    }

    public void commandAction(Command command, Displayable displayable){
        Form form = (Form)displayable;
        //#if Revision == QQ
        //#if (Directory == SE-K300) || (Directory == SE-K500) || (Directory == SE-K700)
        //#else
        if (shenZhouFuCommandAction(form, command)) {
        	return;
        }
        //#endif
        //#endif

        if(type == STATE_TASKUI){
            if(command.getCommandType() == Command.BACK){
                taskUIInputStatus = TASK_UI_INPUT_BACK;
            }else{
                for(int i = 0; i < taskUIInputResult.length; i++){
                    TextField field = (TextField)form.get(i);

                    taskUIInputResult[i] = field.getString().trim();
                    taskUIInputResult[i] = replaceString(taskUIInputResult[i], TASK_DIVID, " ");

                    if(field.getConstraints() == TextField.NUMERIC && taskUIInputResult[i].length() == 0){
                        taskUIInputResult[i] = "0";
                    }
                }

                taskUIInputStatus = TASK_UI_INPUT_DONE;
            }

            World.RecordPreousDisplay(World.instance);

            return;
        }

        if(_form == null){
            return;
        }

        String title = _formTitle;
        String choice = command.getLabel();

        if(type == STATE_INPUT_FORM){
            if(choice.equals("返回")){
                _form = null;
                World.formInputOver = true;
                World.lastInput = "";
                World.RecordPreousDisplay(World.instance);
            }else if(choice.equals("完成")){
                TextField t = (TextField)form.get(0);
                World.lastInput = t.getString();
                World.formInputOver = true;
                World.RecordPreousDisplay(World.instance);
            }
            //#if Revision == JIANGSUNCMCC
            if(title.equals("江苏好友推荐")){
            	if(choice.equals("推荐")){
            		 TextField t = (TextField)form.get(0);
            		 String telphone = t.getString().trim();
            		 if(telphone != null){
                   	  	World.sendRequest(GameState.CONN_COMMAND, new Object[]{
                                 "recommended 0 " + telphone
                             }, false);
            		 }
                     World.RecordPreousDisplay(World.instance);
            	}else{
            		 _form = null;
            		 World.RecordPreousDisplay(World.instance);
            	}
            }
            //#endif
            return;
        }

        if(title.equals("登录")){
            if(choice.equals("返回")){
                _form = null;
                subState = SS_CHOICE;
                World.RecordPreousDisplay(World.instance);
            }else if(choice.equals("登录")){
                TextField t = (TextField)form.get(0);
                name = t.getString();
                t = (TextField)form.get(1);
                password = t.getString();
                _message = "正在连接...";
                _form = null;

                if(choice.equals("登录")){
                    subState = SS_LOGINING;
                }else{
                    subState = SS_REGISTERING;
                }

                World.RecordPreousDisplay(World.instance);
                World.saveData(World.RMS_DATA, World.stringToBytes(name), (byte)1);
                World.saveData(World.RMS_DATA, World.stringToBytes(password), (byte)2);
            }
        }else if(title.equals("注册")){
            if(choice.equals("返回")){
                _form = null;
                subState = SS_CHOICE;
                World.RecordPreousDisplay(World.instance);
            }else if(choice.equals("注册")){
                String name = ((TextField)form.get(0)).getString().trim();
                //#if Revision != CMCC
                //String phone = ((TextField)form.get(1)).getString().trim();
                String recommand = ((TextField)form.get(1)).getString().trim();
                //#endif

                GameState.name = name;
                //#if Revision != CMCC
                //GameState.phone = phone;
                GameState.recommend = recommand;
                //#endif

                _message = "正在注册...";
                subState = SS_REGISTERING;
                _form = null;

                World.RecordPreousDisplay(World.instance);
            }
        }else if(title.equals("新建角色")){
            if(choice.equals("返回")){
                _form = null;
                subState = SS_ACTOR_SELECT;
                World.RecordPreousDisplay(World.instance);
            }else if(choice.equals("创建")){
                String name = ((TextField)form.get(0)).getString().trim();
                int sex = 1;

                if(name.length() == 0){
                    World.Alert(_form, "警告", "角色名错误");

                    return;
                }

                GameState.actorName = name;
                GameState.actorSex = sex;
                _message = "正在创建角色...";
                subState = SS_ACTOR_CREATING;
                _form = null;
                World.RecordPreousDisplay(World.instance);
            }
        }else if(title.equals("发起聊天")){
            _form = null;

            String info = ((TextField)form.get(0)).getString().trim();

            //#mdebug
            String cmd[] = splitString(info, ' ');
            if(runCommand(cmd)){
                World.chat_input_doing = false;
                _form = null;
                World.setGameState(null);
                World.RecordPreousDisplay(World.instance);
                return;
            }
            //#enddebug
            int channel = ((ChoiceGroup)form.get(1)).getSelectedIndex();

            if(choice.equals("发送")){
                World.chat_input_uncompelte_message = null;
                World.net_chat_current_channel = channel;

                channel = -(channel + 2);
                if(channel == -7)
                    channel = -1;

                if(info.trim().length() > 0)
                    World.sendRequest(GameState.CONN_CHAT_MESSAGE, new Object[]{
                                    new Integer(0), "", new Integer(channel), info
                    }, false);
            }else{
                World.chat_input_uncompelte_message = info;
                World.net_chat_current_channel = channel;
            }

            World.chat_input_doing = false;
            _form = null;
            World.setGameState(null);
            World.RecordPreousDisplay(World.instance);
        }else if(title.equals("回复密聊")){
            _form = null;

            String info = ((TextField)form.get(0)).getString().trim();

            ChoiceGroup cg = (ChoiceGroup)form.get(1);
            String target = cg.getString(cg.getSelectedIndex());

            if(choice.equals("发送")){
                World.chat_input_uncompelte_message = null;

                info = "/" + target + " " + info;
                if(info.trim().length() > 0)
                    World.sendRequest(GameState.CONN_CHAT_MESSAGE, new Object[]{
                                    new Integer(0), "", new Integer(World.NET_CHAT_TYPE_PRIVATE), info
                    }, false);
            }else{
                World.chat_input_uncompelte_message = info;
            }

            World.chat_input_doing = false;
            _form = null;
            World.setGameState(null);
            World.RecordPreousDisplay(World.instance);
        }

    }

    //#mdebug
    public String[] splitString(String str, char split){
        Vector strs = new Vector();
        int begin = 0;
        for(int i = 0; i < str.length(); i++){
            if(str.charAt(i) == split){
                strs.addElement(str.substring(begin, i));
                begin = i + 1;
            }
            if(i == str.length() - 1){
                strs.addElement(str.substring(begin, i + 1));
            }
        }
        String[] ret = new String[strs.size()];
        strs.copyInto(ret);
        return ret;
    }

    public boolean runCommand(String[] cmd){
        boolean ret = false;

        if(cmd.length == 0){
            return false;
        }

        if(cmd[0].toLowerCase().equals("/go")){
            if(cmd.length == 4){
                short mapID = Short.parseShort(cmd[1]);
                short x = Short.parseShort(cmd[2]);
                short y = Short.parseShort(cmd[3]);
                World.gotoMap(mapID, x, y, true);
            }
            ret = true;
        }else if(cmd[0].toLowerCase().equals("/renew")){
            World.sendRequest(GameState.CONN_COMMAND, new Object[]{
                "property renew " + World.currMapId
            }, false);
            ret = true;
        }else if(cmd[0].toLowerCase().equals("/battle")){
            int battleCount = 1;
            int monsterId = 0;
            if(cmd.length > 2){
                battleCount = Integer.parseInt(cmd[1]);
                monsterId = Integer.parseInt(cmd[2]);
            }else if(cmd.length > 1){
                battleCount = Integer.parseInt(cmd[1]);
            }
            for(int c = 0; c < battleCount; c++){
                UWAPSegment segment = new UWAPSegment(GameState.CONN_BATTLE_RESULT);

                try{
                    segment.writeByte((byte)1);
                    segment.writeInt(World.player.hp);
                    segment.writeInt(World.player.mp);

                    if(World.player.petCurrent != null){
                        segment.writeInt(World.player.petCurrent.hp);
                        segment.writeInt(World.player.petCurrent.mp);
                    }else{
                        segment.writeInt(0);
                        segment.writeInt(0);
                    }

                    if(monsterId != 0){
                        segment.writeInt(monsterId);
                    }else{
                        segment.writeInt(World.monsters[0].id);
                    }

                    byte count = 1;

                    segment.writeByte((count));

                    for(int i = 0; i < count; i++){
                        segment.writeByte((byte)0);
                        segment.writeByte((byte)1);
                    }

                    segment.flush();
                    GameState.sendRequest(segment);
                    ret = true;
                }catch(IOException e){
                    //#debug
                    e.printStackTrace();
                }
            }
        }else if(cmd[0].toLowerCase().equals("/clearcache")){
            World.clearImageSetCache();
            addChat("clear image cache");
        }else if(cmd[0].toLowerCase().equals("/task")){
            short taskID1 = Short.parseShort(cmd[1]);
            short taskID2 = Short.parseShort(cmd[2]);
            for(short taskID = taskID1; taskID <= taskID2; taskID++)
                World._gtvm.assignTask(taskID, (byte)0);
            ret = true;
        }

        return ret;
    }

    private void addChat(String chat){

        GameEvent msg = new GameEvent(GameEvent.NET_CHAT_MESSAGE, 5, 2);

        msg.idata[0] = -1; //Source id
        msg.sdata[0] = "Log"; //Source name
        msg.idata[1] = -1; //message type
        msg.sdata[1] = chat; //content

        int idx = msg.idata[1] * (-1) - 1;
        msg.idata[2] = World.net_chat_priority_option[idx];
        msg.idata[3] = World.net_chat_color_option[idx];
        msg.sdata[0] = World.NET_CHAT_NAME[idx] + msg.sdata[0];

        msg.idata[4] = 0; //shown
        World.addNetChatMessage(World.net_chat_high_priority_message, msg);
    }

    //#enddebug

    public void drawState(Graphics g){
        g.setFont(font);

        switch(type){
      //#if (Revision == QQ) || (Revision == SOHU) || (Revision == JIANGSUN)
           case STATE_SPLASH:
                drawSplash(g);

                break;
             //#endif
            case STATE_GAMEMENU:
                drawGameMenu(g);

                break;
            case STATE_LOADING:
                drawLoading(g);

                break;
            case STATE_MAINMENU:
                drawMainMenu(g);

                break;
            case STATE_TASKUI:
                drawTaskUI(g);

                break;
            case STATE_EDITEQUIPS:
                drawEditEquips(g);

                break;
            case STATE_EDITATTR:
                drawEditAttr(g);

                break;
            case STATE_EDITITEM:
                drawEditItem(g);

                break;
            case STATE_CHATLIST:
                drawChatList(g);

                break;
            case STATE_RELOGIN:
                drawReconnect(g);

                break;
            case STATE_GETLISTFAIL:
            {
                g.setColor(0x000000);
                g.fillRect(0, 0, World.viewWidth, World.viewHeight);

                g.setClip(0, 0, World.viewWidth, World.viewHeight - LINE_HEIGHT - 4);
                for (int i = 0; i < infoStrings.length; i++) {
                    drawShadowString(g, infoStrings[i], SCREEN_MARGIN, -_scroll + SCREEN_MARGIN + i * CHAR_HEIGHT, true);
                }
                g.setClip(0, 0, World.viewWidth, World.viewHeight);
                
                if ((World.tick % 8) < 4) {
                    String t = "按确认键退出...";
                    int tw = font.stringWidth(t);
                    drawShadowString(g, t, World.viewWidth / 2 - tw / 2, World.viewHeight - LINE_HEIGHT, true);
                }
                break;
            }
        }

        if(waitType != WAIT_NONE){
            if(waitType == WAIT_WAITPRESS || waitType == WAIT_79CONFIRM){
                Vector vec = World.formatString(_message, World.viewWidth - 20, font);
                Object[] obj = new Object[vec.size()];
                vec.copyInto(obj);
                drawMsgTip(g, -1, -1, obj, null, null, BTNTYPE_FIRE);
            }
            //drawMessageBox(g, _message);
            else if(waitType == WAIT_WAITRESULT){
                if(showType == SHOW_MESSAGE){
                    //drawMessageBox(g, _message);
                    Vector vec = World.formatString(_message, World.viewWidth - 20, font);
                    Object[] obj = new Object[vec.size()];
                    vec.copyInto(obj);
                    drawMsgTip(g, -1, -1, obj, null, null, BTNTYPE_FIRE);
                    if(canCancel){
                        drawButtons(g, BUTTON_RIGHT, true);
                    }
                }
            }else if(waitType == WAIT_VIEW_ATTACHMENT){
                showGameItem.drawInfoTip(g, 0, 0, true, false, true);

                if(showGameItem.type != GameItem.TYPE_MONEY){
                    if(showGameItem.request_desc && showGameItem.download_desc_state < 2){
                        showGameItem.download_desc_state = 2;
                    }
                }
            }
        }
    }
  //#if (Revision == QQ) || (Revision == SOHU) || (Revision == JIANGSUN)
   private void drawSplash(Graphics g){
        g.setColor(splash_bgcolor[subState]);
        g.fillRect(0, 0, World.viewWidth, World.viewHeight);
        g.drawImage(_image, World.viewWidth / 2, World.viewHeight / 2, Graphics.HCENTER | Graphics.VCENTER);
    }
   //#endif

    public int partSelect;
    public int equSelect;
    public boolean showPartEquip;
    public boolean showEquipInfo;
    public boolean showDifferent;
    public Object[] listEquips;
    public int menuWidth;

    public static final byte EQU_MENU_LEFT = 25;

    public static int getValueColor(int v){
        int clrGreen = GameItem.CLR_GREEN;
        int clrRed = GameItem.CLR_RED;
        if(v >= 0)
            return clrGreen;
        else
            return clrRed;
    }

    //#if Directory == NK-E61
    //# public final static int BODY_FRAME_TOP = LINE_HEIGHT + 8;
    //# public final static int BODY_FRAME_WIDTH = 241;
    //# public final static int BODY_FRAME_HEIGHT = 64;

    //# public final static int BASEATTR_FRAME_TOP = LINE_HEIGHT + 8;
    //# public final static int BASEATTR_FRAME_LEFT = BODY_FRAME_WIDTH + 6;
    //# public final static int BASEATTR_FRAME_WIDTH = 73;
    //# public final static int BASEATTR_FRAME_HEIGHT = 91;

    //# public final static int EQUIP_FRAME_LEFT = 0;
    //# public final static int EQUIP_FRAME_TOP = BODY_FRAME_TOP + BODY_FRAME_HEIGHT + 2;
    //# public final static int EQUIP_FRAME_WIDTH = 241;
    //# public final static int EQUIP_FRAME_HEIGHT = BASEATTR_FRAME_TOP + BASEATTR_FRAME_HEIGHT - EQUIP_FRAME_TOP;

    //# public final static int EXTATTR_FRAME_TOP = BASEATTR_FRAME_TOP + BASEATTR_FRAME_HEIGHT + 2;

    //# public final static int IMG_SWORD_LEFT = 76;
    //# public final static int IMG_SWORD_TOP = 54;
    //# public final static int IMG_BODY_LEFT = 99;
    //# public final static int IMG_BODY_TOP = 31;
    //# public final static int IMG_SHIELD_LEFT = 128;
    //# public final static int IMG_SHIELD_TOP = 56;

    //# public final static int[] BODYIMG = {
    //#                 IMG_BODY_LEFT, IMG_BODY_TOP, IMG_SWORD_LEFT, IMG_SWORD_TOP, IMG_SHIELD_LEFT, IMG_SHIELD_TOP
    //# };

    //# public final static int[] EQUIP_PART_X = {
    //#                 108, 108, 108, 108, 99, 118, 108, 84, 133
    //# };
    //# public final static int[] EQUIP_PART_Y = {
    //#                 33, 42, 51, 60, 58, 64, 78, 62, 62
    //# };

    //#elif (Directory == NK-BigScreen) || (Directory == NK-Nokia403Big) || (Directory == SE-S700)
    //# public final static int BODY_FRAME_TOP = LINE_HEIGHT + 8;
    //# public final static int BODY_FRAME_WIDTH = 161;
    //# public final static int BODY_FRAME_HEIGHT = 83;

    //# public final static int BASEATTR_FRAME_TOP = LINE_HEIGHT + 8;
    //# public final static int BASEATTR_FRAME_LEFT = BODY_FRAME_WIDTH + 6;
    //# public final static int BASEATTR_FRAME_WIDTH = 83;
    //# public final static int BASEATTR_FRAME_HEIGHT = 110;

    //# public final static int EQUIP_FRAME_LEFT = 0;
    //# public final static int EQUIP_FRAME_TOP = BODY_FRAME_TOP + BODY_FRAME_HEIGHT + 2;
    //# public final static int EQUIP_FRAME_WIDTH = 151;
    //# public final static int EQUIP_FRAME_HEIGHT = BASEATTR_FRAME_TOP + BASEATTR_FRAME_HEIGHT - EQUIP_FRAME_TOP;

    //# public final static int EXTATTR_FRAME_TOP = BASEATTR_FRAME_TOP + BASEATTR_FRAME_HEIGHT + 2;

    //# public final static int IMG_SWORD_LEFT = 46;
    //# public final static int IMG_SWORD_TOP = 69;
    //# public final static int IMG_BODY_LEFT = 69;
    //# public final static int IMG_BODY_TOP = 46;
    //# public final static int IMG_SHIELD_LEFT = 98;
    //# public final static int IMG_SHIELD_TOP = 71;

    //# public final static int[] BODYIMG = {
    //#                 IMG_BODY_LEFT, IMG_BODY_TOP, IMG_SWORD_LEFT, IMG_SWORD_TOP, IMG_SHIELD_LEFT, IMG_SHIELD_TOP
    //# };

    //# public final static int[] EQUIP_PART_X = {
    //#                 78, 78, 78, 78, 69, 88, 78, 54, 103
    //# };
    //# public final static int[] EQUIP_PART_Y = {
    //#                 48, 57, 66, 75, 73, 79, 83, 77, 77
    //# };

    //#elif (Directory == MT-General) || (Directory == Midp2-General) || (Directory == SE-K500) || (Directory == SE-K300) || (Directory == Nokia403) || (MIDP2Common == true)  || (Directory == ClientTouch-E680) || (Directory == ClientTouch--Midp2-General) || (Directory == ClientTouch-SE-General) || (Directory == ClientTouch-Nokia5800)
    public static int BODY_FRAME_TOP = LINE_HEIGHT + 8;
    public static int BODY_FRAME_WIDTH;
    public static int BODY_FRAME_HEIGHT;

    public static int BASEATTR_FRAME_TOP = LINE_HEIGHT + 8;
    public static int BASEATTR_FRAME_LEFT/* = BODY_FRAME_WIDTH + 6*/;
    public static int BASEATTR_FRAME_WIDTH = 73;
    public static int BASEATTR_FRAME_HEIGHT;

    public static int EQUIP_FRAME_LEFT = 0;
    public static int EQUIP_FRAME_TOP /*= BODY_FRAME_TOP + BODY_FRAME_HEIGHT + 2*/;
    public static int EQUIP_FRAME_WIDTH /*= 97*/;
    public static int EQUIP_FRAME_HEIGHT /*= BASEATTR_FRAME_TOP + BASEATTR_FRAME_HEIGHT - EQUIP_FRAME_TOP*/;

    public static int EXTATTR_FRAME_TOP /*= BASEATTR_FRAME_TOP + BASEATTR_FRAME_HEIGHT + 2*/;

    public static int IMG_SWORD_LEFT /*= 16*/;
    public static int IMG_SWORD_TOP /*= 54*/;
    public static int IMG_BODY_LEFT /*= 39*/;
    public static int IMG_BODY_TOP /*= 31*/;
    public static int IMG_SHIELD_LEFT /*= 68*/;
    public static int IMG_SHIELD_TOP /*= 56*/;

    public static int[] BODYIMG;

    public static int[] EQUIP_PART_X;
    public static int[] EQUIP_PART_Y;

    public static void initLocation(){
        int w = World.viewWidth;
        int h = World.viewHeight;

        BODY_FRAME_WIDTH = w - BASEATTR_FRAME_WIDTH - 6;
        BODY_FRAME_HEIGHT = World.bodyImg.getHeight(0) + 8;
        BASEATTR_FRAME_LEFT = BODY_FRAME_WIDTH + 6;
        BASEATTR_FRAME_HEIGHT = LINE_HEIGHT * 4 + 8;

        EQUIP_FRAME_WIDTH = BODY_FRAME_WIDTH;
        EQUIP_FRAME_HEIGHT = LINE_HEIGHT + 10;

        if(BASEATTR_FRAME_HEIGHT > EQUIP_FRAME_HEIGHT + BODY_FRAME_HEIGHT + 2){
            BODY_FRAME_HEIGHT = BASEATTR_FRAME_HEIGHT - EQUIP_FRAME_HEIGHT - 2;
        }else{
            BASEATTR_FRAME_HEIGHT = EQUIP_FRAME_HEIGHT + BODY_FRAME_HEIGHT + 2;
        }

        EQUIP_FRAME_TOP = BODY_FRAME_TOP + BODY_FRAME_HEIGHT + 2;

        EXTATTR_FRAME_TOP = BASEATTR_FRAME_TOP + BASEATTR_FRAME_HEIGHT + 2;

        IMG_BODY_LEFT = 23;
        IMG_BODY_TOP = BODY_FRAME_TOP + (BODY_FRAME_HEIGHT - World.bodyImg.getHeight(0)) / 2;
        IMG_SWORD_LEFT = 0;
        IMG_SWORD_TOP = IMG_BODY_TOP + 23;
        IMG_SHIELD_LEFT = 52;
        IMG_SHIELD_TOP = IMG_BODY_TOP + 25;

        int offset = (BODY_FRAME_WIDTH - (IMG_SHIELD_LEFT + World.bodyImg.getWidth(2))) / 2;
        IMG_BODY_LEFT += offset;
        IMG_SWORD_LEFT += offset;
        IMG_SHIELD_LEFT += offset;

        BODYIMG = new int[]{
                        IMG_BODY_LEFT, IMG_BODY_TOP, IMG_SWORD_LEFT, IMG_SWORD_TOP, IMG_SHIELD_LEFT, IMG_SHIELD_TOP
        };

        EQUIP_PART_X = new int[]{
                        offset + 32, offset + 32, offset + 32, offset + 32, offset + 23, offset + 42, offset + 32, offset + 8, offset + 57
        };

        EQUIP_PART_Y = new int[]{
                        IMG_BODY_TOP + 2, IMG_BODY_TOP + 11, IMG_BODY_TOP + 20, IMG_BODY_TOP + 29, IMG_BODY_TOP + 27, IMG_BODY_TOP + 33, IMG_BODY_TOP + 47, IMG_BODY_TOP + 31, IMG_BODY_TOP + 31
        };
        
        GAMEMENU_OFFSET = World.viewHeight - CHAR_HEIGHT * 3 - 5;
 

        World.LOCATION_HEIGHT = (h - 50) / 3;
        
        //#if TouchScreen == true
        BBS_PAGE_COUNT = (h - 20 - LINE_HEIGHT-World.ok.getHeight()) / LINE_HEIGHT;
        	//#else
        	//# BBS_PAGE_COUNT = (h - 20 - LINE_HEIGHT) / LINE_HEIGHT;
        	//#endif 
        //BBS_PAGE_COUNT = (h - 20 - LINE_HEIGHT) / LINE_HEIGHT;

        World.BATTLEICON_FRAME_WIDTH = World.viewWidth;

    }

    //#else
    //# public final static int BODY_FRAME_TOP = LINE_HEIGHT + 8;
    //# public final static int BODY_FRAME_WIDTH = 97;
    //# public final static int BODY_FRAME_HEIGHT = 64;

    //# public final static int BASEATTR_FRAME_TOP = LINE_HEIGHT + 8;
    //# public final static int BASEATTR_FRAME_LEFT = BODY_FRAME_WIDTH + 6;
    //# public final static int BASEATTR_FRAME_WIDTH = 73;
    //# public final static int BASEATTR_FRAME_HEIGHT = 91;

    //# public final static int EQUIP_FRAME_LEFT = 0;
    //# public final static int EQUIP_FRAME_TOP = BODY_FRAME_TOP + BODY_FRAME_HEIGHT + 2;
    //# public final static int EQUIP_FRAME_WIDTH = 97;
    //# public final static int EQUIP_FRAME_HEIGHT = BASEATTR_FRAME_TOP + BASEATTR_FRAME_HEIGHT - EQUIP_FRAME_TOP;

    //# public final static int EXTATTR_FRAME_TOP = BASEATTR_FRAME_TOP + BASEATTR_FRAME_HEIGHT + 2;

    //# public final static int IMG_SWORD_LEFT = 16;
    //# public final static int IMG_SWORD_TOP = 54;
    //# public final static int IMG_BODY_LEFT = 39;
    //# public final static int IMG_BODY_TOP = 31;
    //# public final static int IMG_SHIELD_LEFT = 68;
    //# public final static int IMG_SHIELD_TOP = 56;

    //# public final static int[] BODYIMG = {
    //#             IMG_BODY_LEFT, IMG_BODY_TOP, IMG_SWORD_LEFT, IMG_SWORD_TOP, IMG_SHIELD_LEFT, IMG_SHIELD_TOP
    //# };

    //# public final static int[] EQUIP_PART_X = {
    //#             48, 48, 48, 48, 39, 58, 48, 24, 73
    //# };
    //# public final static int[] EQUIP_PART_Y = {
    //#             33, 42, 51, 60, 58, 64, 78, 62, 62
    //# };
    //#endif

    public final static int PARTSEL_CLR = 0x00ff00;

    private void drawSelectPart(Graphics g){

        int offset = (int)((World.tick / 5) % 2) + 1;

        int x = EQUIP_PART_X[partSelect] - offset;
        int y = EQUIP_PART_Y[partSelect] - offset;
        int width = World.bodyImg.getWidth(3) + offset * 2;
        int height = World.bodyImg.getHeight(3) + offset * 2;

        g.setColor(PARTSEL_CLR);

        g.drawLine(x, y, x + 3, y);
        g.drawLine(x, y, x, y + 3);

        g.drawLine(x + width - 1, y, x + width - 4, y);
        g.drawLine(x + width - 1, y, x + width - 1, y + 3);

        g.drawLine(x, y + height - 1, x + 3, y + height - 1);
        g.drawLine(x, y + height - 1, x, y + height - 4);

        g.drawLine(x + width - 1, y + height - 1, x + width - 4, y + height - 1);
        g.drawLine(x + width - 1, y + height - 1, x + width - 1, y + height - 4);

    }

    //#if Directory == MT-V300

    //# public void drawEditEquips(Graphics g){
    //# g.setColor(EDGE_COLOR[4]);
    //# g.fillRect(0, 0, World.viewWidth, World.viewHeight);
    //# Sprite s = World.player;
    //# int lh = LINE_HEIGHT + 6;
    //# drawEdge(g, 0, 0, 0, World.viewWidth, lh, true, EDGE_COLOR[3]);
    //# World.draw3DString(g, "武器装备", World.viewWidth / 2, 3 + (LINE_HEIGHT - CHAR_HEIGHT) / 2, Graphics.TOP | Graphics.HCENTER, 0xffffff);
    //# drawEdge(g, 0, EQUIP_FRAME_LEFT, EQUIP_FRAME_TOP, EQUIP_FRAME_WIDTH, EQUIP_FRAME_HEIGHT, true, EDGE_COLOR[5]);
    //# int x = 0;
    //# s.playerEquips[partSelect].drawName(g, EQUIP_FRAME_LEFT + 5, EQUIP_FRAME_TOP + 3 + (LINE_HEIGHT - CHAR_HEIGHT) / 2, false, true, EQUIP_FRAME_WIDTH - 14, false);
    //# x = 4;
    //# int y = EXTATTR_FRAME_TOP + 8;
    //# for(int i = attrShowBegin; i < attrShowBegin + ATTR_SHOWNUM; i++){
    //#     x = 8;
    //#     int av = 0;
    //#     String attrStr = null;
    //#     av = s.getShowAttribute(drawAttrs[i]);
    //#     if(i == attrSelected){
    //#         g.setColor(EDGE_COLOR[5]);
    //#         g.fillRect(3, y + (i - attrShowBegin) * LINE_HEIGHT, World.viewWidth, LINE_HEIGHT);
    //#     }
    //#     World.draw3DString(g, Sprite.ATTR_NAMES[drawAttrs[i]] + "：", x, y + (i - attrShowBegin) * LINE_HEIGHT + (LINE_HEIGHT - CHAR_HEIGHT) / 2, Graphics.TOP | Graphics.LEFT, 0xff0000, 0xffffff);
    //#     x += font.stringWidth(Sprite.ATTR_NAMES[drawAttrs[i]] + "：");
    //#     attrStr = String.valueOf(av);
    //#     if(drawAttrs[i] == BattleSprite.ATTR_PMIN){
    //#         attrStr += " ~ " + s.getAttribute(BattleSprite.ATTR_PMAX);
    //#     }else if(drawAttrs[i] == BattleSprite.ATTR_MMIN){
    //#         attrStr += " ~ " + s.getAttribute(BattleSprite.ATTR_MMAX);
    //#     }
    //#     drawAttrNum(attrStr, g, x, y + (i - attrShowBegin) * LINE_HEIGHT + LINE_HEIGHT / 2, Graphics.VCENTER | Graphics.LEFT);
    //# }
    //# drawEdge(g, 0, 0, EXTATTR_FRAME_TOP, World.viewWidth, World.viewHeight - EXTATTR_FRAME_TOP, false, 0);
    //# x = World.viewWidth - 3 - World.bodyImg.getWidth(2);
    //# y = EXTATTR_FRAME_TOP + 5 + (int)((World.tick / 5) % 2) * 3;
    //# World.bodyImg.drawFrame(g, 2, x, y, Graphics.TOP | Graphics.LEFT);
    //# y = World.viewHeight - World.bodyImg.getHeight(3) - 5 - (int)((World.tick / 5) % 2) * 3;
    //# World.bodyImg.drawFrame(g, 3, x, y, Graphics.TOP | Graphics.LEFT);
    //# if(showPartEquip){
    //#     int left = EQUIP_FRAME_LEFT;
    //#     int top = EQUIP_FRAME_TOP + EQUIP_FRAME_HEIGHT;
    //#     World.drawMenu(g, listEquips, left, top, equSelect, 0);
    //# }
    //# if(showEquipInfo){
    //#     GameItem item = null;
    //#     if(showPartEquip){
    //#         if(equSelect > 0){
    //#             item = (GameItem)listEquips[equSelect];
    //#         }
    //#     }else{
    //#          item = World.player.playerEquips[partSelect];
    //#     }
    //#     if(item == null || item.type == GameItem.TYPE_NULL)
    //#         return;
    //#     int top = EQUIP_FRAME_TOP + EQUIP_FRAME_HEIGHT;

    //#     int[] wh = item.calculateInfoTip(false);
    //#     if(top + wh[1] > World.viewHeight){
    //#         top = World.viewHeight - wh[1];
    //#     }
    //#     int left = World.viewWidth - wh[0] - 5;
    //#     item.drawInfoTip(g, left, top, false, false, false);
    //# }
    //# }

    //#else

    public void drawEditEquips(Graphics g){
        g.setColor(EDGE_COLOR[4]);
        g.fillRect(0, 0, World.viewWidth, World.viewHeight);

        Sprite s = World.player;

        int lh = LINE_HEIGHT + 6;
        drawEdge(g, 0, 0, 0, World.viewWidth, lh, true, EDGE_COLOR[3]);

        World.draw3DString(g, "武器装备", World.viewWidth / 2, 3 + (LINE_HEIGHT - CHAR_HEIGHT) / 2, Graphics.TOP | Graphics.HCENTER, 0xffffff);

        drawEdge(g, 0, 0, BODY_FRAME_TOP, BODY_FRAME_WIDTH, BODY_FRAME_HEIGHT, false, 0);
        drawEdge(g, 0, BASEATTR_FRAME_LEFT, BASEATTR_FRAME_TOP, BASEATTR_FRAME_WIDTH, BASEATTR_FRAME_HEIGHT, false, 0);
        drawEdge(g, 0, EQUIP_FRAME_LEFT, EQUIP_FRAME_TOP, EQUIP_FRAME_WIDTH, EQUIP_FRAME_HEIGHT, true, EDGE_COLOR[5]);

        for(int i = 0; i < 6; i += 2){
            World.bodyImg.drawFrame(g, i / 2, BODYIMG[i], BODYIMG[i + 1], Graphics.TOP | Graphics.LEFT);
        }

        for(int i = 0; i < EQUIP_PART_X.length; i++){
            GameItem item = s.playerEquips[i];
            if(item.type != GameItem.TYPE_NULL){
                int clr = item.getColor(false);
                g.setColor(clr);
                g.fillRect(EQUIP_PART_X[i], EQUIP_PART_Y[i], World.bodyImg.getWidth(3), World.bodyImg.getHeight(3));
            }
            World.bodyImg.drawFrame(g, 3, EQUIP_PART_X[i], EQUIP_PART_Y[i], Graphics.TOP | Graphics.LEFT);
          //#if TouchScreen == true
            StaticUtils.addButton(2000+i, EQUIP_PART_X[i], EQUIP_PART_Y[i], World.bodyImg.getWidth(3), World.bodyImg.getHeight(3));
            //#endif
        }

        drawSelectPart(g);

        int x = 0;
        for(int i = 0; i < 4; i++){
            x = BASEATTR_FRAME_LEFT + 4;
            World.draw3DString(g, BattleSprite.ATTR_NAMES[i] + "：", x, BASEATTR_FRAME_TOP + 8 + i * LINE_HEIGHT, Graphics.LEFT | Graphics.TOP, 0xff0000, 0xffffff);
            x += font.stringWidth(BattleSprite.ATTR_NAMES[i] + "：") + 1;
            drawAttrNum(String.valueOf(s.getAttribute(i)), g, x, BASEATTR_FRAME_TOP + 8 + i * LINE_HEIGHT + LINE_HEIGHT / 2, Graphics.VCENTER | Graphics.LEFT);
        }

        s.playerEquips[partSelect].drawName(g, EQUIP_FRAME_LEFT + 5, EQUIP_FRAME_TOP + 3 + (LINE_HEIGHT - CHAR_HEIGHT) / 2, false, true, EQUIP_FRAME_WIDTH - 14, false);

        x = 4;
        int y = BASEATTR_FRAME_TOP + BASEATTR_FRAME_HEIGHT + 8;

        //#if (Directory == MT-General) || (Directory == Midp2-General) || (Directory == SE-K500) || (Directory == SE-K300) || (Directory == Nokia403) || (MIDP2Common == true)
        ATTR_SHOWNUM = (World.viewHeight - y - 10) / LINE_HEIGHT;
        //#endif
      //#if TouchScreen == true && ( (Directory == ClientTouch-E680) || (Directory == ClientTouch--Midp2-General) || (Directory == ClientTouch-SE-General) || (Directory == ClientTouch-Nokia5800))
        ATTR_SHOWNUM = (World.viewHeight - y - 10 - LINE_HEIGHT) / LINE_HEIGHT;
        //#endif

        for(int i = attrShowBegin; i < attrShowBegin + ATTR_SHOWNUM; i++){
            if(i >= drawAttrs.length){
                continue;
            }

            x = 8;
            int av = 0;
            String attrStr = null;
            av = s.getShowAttribute(drawAttrs[i]);

            if(i == attrSelected){
                g.setColor(EDGE_COLOR[5]);
                g.fillRect(3, y + (i - attrShowBegin) * LINE_HEIGHT, World.viewWidth, LINE_HEIGHT);
            }

            World.draw3DString(g, Sprite.ATTR_NAMES[drawAttrs[i]] + "：", x, y + (i - attrShowBegin) * LINE_HEIGHT + (LINE_HEIGHT - CHAR_HEIGHT) / 2, Graphics.TOP | Graphics.LEFT, 0xff0000, 0xffffff);
            x += font.stringWidth(Sprite.ATTR_NAMES[drawAttrs[i]] + "：");

            attrStr = String.valueOf(av);
            if(drawAttrs[i] == BattleSprite.ATTR_PMIN){
                attrStr += " ~ " + s.getAttribute(BattleSprite.ATTR_PMAX);
            }else if(drawAttrs[i] == BattleSprite.ATTR_MMIN){
                attrStr += " ~ " + s.getAttribute(BattleSprite.ATTR_MMAX);
            }

            drawAttrNum(attrStr, g, x, y + (i - attrShowBegin) * LINE_HEIGHT + LINE_HEIGHT / 2, Graphics.VCENTER | Graphics.LEFT);
        }

        drawEdge(g, 0, 0, EXTATTR_FRAME_TOP, World.viewWidth, World.viewHeight - EXTATTR_FRAME_TOP, false, 0);

        x = World.viewWidth - 3 - World.bodyImg.getWidth(4);
        y = BASEATTR_FRAME_TOP + BASEATTR_FRAME_HEIGHT + 5 + (int)((World.tick / 5) % 2) * 3;
        World.bodyImg.drawFrame(g, 4, x, y, Graphics.TOP | Graphics.LEFT);
      //#if TouchScreen == true
        StaticUtils.addButton(World.KEY_NUM1_PRESSED, x, y, World.bodyImg.getWidth(4), World.bodyImg.getHeight(4));
        //#endif
        y = World.viewHeight - World.bodyImg.getHeight(5) - 5 - (int)((World.tick / 5) % 2) * 3;
        World.bodyImg.drawFrame(g, 5, x, y, Graphics.TOP | Graphics.LEFT);
      //#if TouchScreen == true
        StaticUtils.addButton(World.KEY_NUM7_PRESSED, x, y, World.bodyImg.getWidth(5), World.bodyImg.getHeight(5));
        //#endif
        if(showPartEquip){
            int left = EQUIP_FRAME_LEFT;
            int top = EQUIP_FRAME_TOP + EQUIP_FRAME_HEIGHT;
            
          //#if TouchScreen == true
            StaticUtils.removeAllButton();
            //#endif

            World.drawMenu(g, listEquips, left, top, equSelect, 0);

        }

        if(showEquipInfo){
            GameItem item = null;
            if(showPartEquip){
                if(equSelect > 0){
                    item = (GameItem)listEquips[equSelect];
                }
            }else{
                item = World.player.playerEquips[partSelect];
            }
            if(item == null || item.type == GameItem.TYPE_NULL)
                return;
            int top = EQUIP_FRAME_TOP + EQUIP_FRAME_HEIGHT;

            int[] wh = item.calculateInfoTip(false);
            if(top + wh[1] > World.viewHeight){
                top = World.viewHeight - wh[1];
            }

            int left = World.viewWidth - wh[0] - 5;

            item.drawInfoTip(g, left, top, false, false, false);

        }

    }

    //#endif

    public byte attrSelected = 0;
    public byte attrShowBegin = 0;

    public static final int[] ATTR_HP_CLR = {
                    0xDA3300, 0xD0B59B, 0xFFF078, 0xFFAE35, 0xFF8336
    };

    public static final int[] ATTR_MP_CLR = {
                    0x143DB2, 0x96BFFF, 0xD8FFD4, 0x9BE298, 0x6079FF
    };

    public static ImageSet hmp = World.getImageSetFromLocal("hmp");

    public static ImageSet attrNum = World.getImageSetFromLocal("attrnum");

    public static void drawAttrNum(String str, Graphics g, int x, int y, int anchor){
        int offsetx = 0;
        for(int i = 0; i < str.length(); i++){
            String sn = str.substring(i, i + 1);

            int frameIndex = 0;
            if(sn.equals(" ")){
                offsetx += 6;
                continue;
            }else if(sn.equals("/")){
                frameIndex = 10;
            }else if(sn.equals("<")){
                frameIndex = 11;
            }else if(sn.equals(">")){
                frameIndex = 12;
            }else if(sn.equals("(")){
                frameIndex = 13;
            }else if(sn.equals(")")){
                frameIndex = 14;
            }else if(sn.equals("~")){
                frameIndex = 15;
            }else{
                try{
                    frameIndex = Integer.parseInt(sn);
                }catch(NumberFormatException e){
                    offsetx += 6;
                    continue;
                }
            }
            attrNum.drawFrame(g, frameIndex, x + offsetx, y, anchor);
            offsetx += attrNum.getWidth(frameIndex);
        }
    }

    private static final byte[] drawAttrs = {
                    0, 1, 2, 3, 4, 6, 7, 9, 10, 11, 12, 13, 14
    };

    //#if (Directory == SE-K500) || (Directory == SE-K300) || (Directory == Nokia403)
    //# public static final int ATTRFRAME_UP_HEIGHT = 81;
    //#else
    public static final int ATTRFRAME_UP_HEIGHT = 91;
    //#endif

    //#if (Directory == MT-General) || (Directory == Midp2-General) || (Directory == SE-K500) || (Directory == SE-K300) || (Directory == Nokia403) || (MIDP2Common == true) || (Directory == ClientTouch-E680) || (Directory == ClientTouch--Midp2-General) || (Directory == ClientTouch-SE-General) || (Directory == ClientTouch-Nokia5800)
    public static int ATTR_SHOWNUM;
    //#else
    //# public static final int ATTR_SHOWNUM =
    //#if Directory == NK-E61
    //# 5
    //#elif Directory == NK-BigScreen
    //# 6
    //#elif Directory == SE-K700
    //# 3
    //#elif (Directory == NK-Nokia403Big)
    //# 7
    //#elif (Directory == SE-S700)
    //# 6
    //#else
    //# 4
    //#endif
    ;

    //#endif

    public void drawEditAttr(Graphics g){
        //        boolean alpha = true;
        Sprite s = World.player;
        g.setFont(font);
        int lh = LINE_HEIGHT;

        g.setColor(EDGE_COLOR[4]);
        g.fillRect(0, 0, World.viewWidth, World.viewHeight);
        //#if (Directory == SE-K500) || (Directory == SE-K300) || (Directory == Nokia403)
        //# int y = -2;
        //#else
        g.setColor(EDGE_COLOR[3]);
        drawEdge(g, 0, 0, 0, World.viewWidth, lh, true, EDGE_COLOR[3]);
        World.draw3DString(g, s.name, World.viewWidth / 2, 2, Graphics.TOP | Graphics.HCENTER, 0xffffff);
        int y = lh;
        //#endif
        int x = 4;
        int bky = y;

        y += 2;
        drawEdge(g, 0, 0, y, World.viewWidth, ATTRFRAME_UP_HEIGHT, false, 0);

        bky = y;
        //draw head image
        y += 7;
        x = 7;
        //#if (Directory != SE-K500) && (Directory != SE-K300) && (Directory != Nokia403)
        g.setColor(0xffffff);
        g.drawRect(x, y, World.getPlayerHead().getWidth() + 1, World.getPlayerHead().getHeight() + 1);
        g.drawImage(World.getPlayerHead(), x + 1, y + 1, Graphics.TOP | Graphics.LEFT);
        //draw HP & MP Bar
        x += World.getPlayerHead().getWidth() + 8;
        //#endif

        y += 2;
        g.setColor(0);
        g.drawLine(x, y, x + 45, y);
        g.drawLine(x, y + 7, x + 45, y + 7);
        g.drawLine(x, y, x, y + 7);
        g.drawLine(x + 46, y + 1, x + 46, y + 6);

        int hpWidth = 33;
        int hw = s.hp * 1000 / s.getAttribute(BattleSprite.ATTR_HPMAX) * hpWidth;
        hw /= 1000;
        hw += 12;

        for(int i = 0; i < ATTR_HP_CLR.length + 1; i++){
            int clrID = i;
            if(i == ATTR_HP_CLR.length){
                clrID = 0;
            }
            g.setColor(ATTR_HP_CLR[clrID]);
            g.drawLine(x + 1, y + 1 + i, x + hw, y + 1 + i);
        }

        hmp.drawFrame(g, 0, x + 1, y + 1, Graphics.TOP | Graphics.LEFT);
        hmp.drawFrame(g, 2, x + 7, y + 1, Graphics.TOP | Graphics.LEFT);

        int hpx = x + 50;
        String hpStr = s.hp + "/" + s.getAttribute(BattleSprite.ATTR_HPMAX);
        drawAttrNum(hpStr, g, hpx, y, Graphics.TOP | Graphics.LEFT);

        y += 10;

        g.setColor(0);
        g.drawLine(x, y, x + 45, y);
        g.drawLine(x, y + 7, x + 45, y + 7);
        g.drawLine(x, y, x, y + 7);
        g.drawLine(x + 46, y + 1, x + 46, y + 6);

        hw = s.mp * 1000 / s.getAttribute(BattleSprite.ATTR_MPMAX) * hpWidth;
        hw /= 1000;
        hw += 12;

        for(int i = 0; i < ATTR_MP_CLR.length + 1; i++){
            int clrID = i;
            if(i == ATTR_MP_CLR.length){
                clrID = 0;
            }
            g.setColor(ATTR_MP_CLR[clrID]);
            g.drawLine(x + 1, y + 1 + i, x + hw, y + 1 + i);
        }

        hmp.drawFrame(g, 1, x + 1, y + 1, Graphics.TOP | Graphics.LEFT);
        hmp.drawFrame(g, 2, x + 7, y + 1, Graphics.TOP | Graphics.LEFT);

        hpStr = s.mp + "/" + s.getAttribute(BattleSprite.ATTR_MPMAX);
        drawAttrNum(hpStr, g, hpx, y, Graphics.TOP | Graphics.LEFT);

        x = 8;
        //#if (Directory == SE-K500) || (Directory == SE-K300) || (Directory == Nokia403)
        //# y += 9;
        //#else
        y += 19;
        //#endif
        g.setColor(0xffffff);

        World.draw3DString(g, "级别：", x, y + (LINE_HEIGHT - CHAR_HEIGHT) / 2, Graphics.TOP | Graphics.LEFT, 0xffffff);
        x += font.stringWidth("级别：");

        drawAttrNum(String.valueOf(s.level), g, x, y + lh / 2, Graphics.VCENTER | Graphics.LEFT);

        x += 10 + String.valueOf(s.level).length() * 6;

        World.draw3DString(g, "属性点：", x, y + (LINE_HEIGHT - CHAR_HEIGHT) / 2, Graphics.TOP | Graphics.LEFT, 0xffffff);
        x += font.stringWidth("属性点：");

        drawAttrNum(String.valueOf(s.learnPoint), g, x, y + lh / 2, Graphics.VCENTER | Graphics.LEFT);

        x = 8;
        y += lh;

        World.draw3DString(g, "经验：", x, y + (LINE_HEIGHT - CHAR_HEIGHT) / 2, Graphics.TOP | Graphics.LEFT, 0xffffff);
        x += font.stringWidth("经验：");


        //#if (Directory == SE-K500) || (Directory == SE-K300) || (Directory == Nokia403)
        //# int expWidth = 60;
        //# y += lh / 4;
        //# g.setColor(0);
        //# g.drawLine(x, y, x + expWidth-1, y);
        //# g.drawLine(x, y + 7, x + expWidth-1, y + 7);
        //# g.drawLine(x, y, x, y + 7);
        //# g.drawLine(x + expWidth, y + 1, x + expWidth, y + 6);
        //# hw = s.exp * expWidth / s.upLevelExp;
        //# for(int i = 0; i < ATTR_MP_CLR.length + 1; i++){
        //#   int clrID = i;
        //#   if(i == ATTR_MP_CLR.length){
        //#       clrID = 0;
        //#    }
        //#   g.setColor(ATTR_HP_CLR[clrID]);
        //#   g.drawLine(x + 1, y + 1 + i, x + expWidth, y + 1 + i);
        //#    if (s.exp > 0) {
        //#        g.setColor(ATTR_MP_CLR[clrID]);
        //#        g.drawLine(x + 1, y + 1 + i, x + hw, y + 1 + i);
        //#    }
        //#  }
        //#   y -= lh / 4;
        //#else
        drawAttrNum(s.exp + "/" + s.upLevelExp, g, x, y + lh / 2, Graphics.VCENTER | Graphics.LEFT);
        //#endif

        y = bky;
        //#if (Directory == SE-K500) || (Directory == SE-K300) || (Directory == Nokia403)
        //# y += ATTRFRAME_UP_HEIGHT;
        //#else
        y += ATTRFRAME_UP_HEIGHT + 3;
        //#endif
        bky = y;

        y += 3;
        g.setColor(0xffffff);
        g.setFont(font);

        //#if (Directory == MT-General) || (Directory == Midp2-General) || (Directory == SE-K500) || (Directory == SE-K300) || (Directory == Nokia403) || (MIDP2Common == true) || (Directory == ClientTouch-E680) || (Directory == ClientTouch--Midp2-General) || (Directory == ClientTouch-SE-General) || (Directory == ClientTouch-Nokia5800)
        ATTR_SHOWNUM = (World.viewHeight - y - 10) / LINE_HEIGHT;
        //#endif

        for(int i = attrShowBegin; i < attrShowBegin + ATTR_SHOWNUM && i < drawAttrs.length; i++){
            x = 8;
            int av = 0;
            int equValue = 0;
            String attrStr = null;
            av = s.getShowAttribute(drawAttrs[i]);
            if(i < 4){
                equValue = s.baseAttribute[drawAttrs[i]];
                equValue = av - equValue;
            }

            if(i == attrSelected){
                g.setColor(EDGE_COLOR[5]);
                g.fillRect(3, y + (i - attrShowBegin) * lh, World.viewWidth - 6, lh);
            }

            World.draw3DString(g, Sprite.ATTR_NAMES[drawAttrs[i]] + "：", x, y + (i - attrShowBegin) * lh + (lh - CHAR_HEIGHT) / 2, Graphics.TOP | Graphics.LEFT, 0xff0000, 0xffffff);
            x += font.stringWidth(Sprite.ATTR_NAMES[drawAttrs[i]] + "：");

            if(i < 4){
                String left = "  ", right = "  ";

                if(s.baseAttribute[drawAttrs[i]] > s.attributeBackup[drawAttrs[i]])
                    left = "< ";

                if(s.learnPoint > 0)
                    right = " >";

                attrStr = left + s.baseAttribute[i] + right + " (" + equValue + ")";
                //#if TouchScreen == true
                if(left!=null && !left.equals("  ")){
                	StaticUtils.addButton(3000+i*2, x, y + (i - attrShowBegin) * lh + lh / 2-10, CHAR_WIDTH, CHAR_HEIGHT);
                } 
                if(right!=null && !right.equals("  ")){
                	StaticUtils.addButton(3000+i*2+1, x+CHAR_WIDTH*2, y + (i - attrShowBegin) * lh + lh / 2-10, CHAR_WIDTH, CHAR_HEIGHT);
                }
                //#endif
            }else{
                attrStr = "  " + String.valueOf(av);
                if(drawAttrs[i] == BattleSprite.ATTR_PMIN){
                    attrStr += " ~ " + s.getAttribute(BattleSprite.ATTR_PMAX);
                }else if(drawAttrs[i] == BattleSprite.ATTR_MMIN){
                    attrStr += " ~ " + s.getAttribute(BattleSprite.ATTR_MMAX);
                }
            }
            drawAttrNum(attrStr, g, x, y + (i - attrShowBegin) * lh + lh / 2, Graphics.VCENTER | Graphics.LEFT);
          //#if TouchScreen == true
            StaticUtils.addButton(2000+i, 0, y + (i - attrShowBegin) * lh + lh / 2-4, World.viewWidth-World.ok.getWidth(), CHAR_HEIGHT);
            //#endif

        }
        drawEdge(g, 0, 0, bky, World.viewWidth, World.viewHeight - bky, false, 0);

        boolean drawUpArrow = false;
        boolean drawDownArrow = false;

        if(attrShowBegin > 0){
            drawUpArrow = true;
        }

        if(attrShowBegin + 4 < drawAttrs.length){
            drawDownArrow = true;
        }

        if(drawUpArrow){
            int ax = World.viewWidth - 3 - World.bodyImg.getWidth(4);
            int ay = bky + 5 + (int)((World.tick / 5) % 2) * 3;
            World.bodyImg.drawFrame(g, 6, ax, ay, Graphics.TOP | Graphics.LEFT);
          //#if TouchScreen == true
            StaticUtils.addButton(World.UP_PRESSED, ax, ay, World.bodyImg.getWidth(4), World.bodyImg.getHeight(4));
            //#endif
        }
        if(drawDownArrow){
            int ax = World.viewWidth - 3 - World.bodyImg.getWidth(4);
            int ay = World.viewHeight - World.bodyImg.getHeight(5) - 5 - (int)((World.tick / 5) % 2) * 3;
            World.bodyImg.drawFrame(g, 7, ax, ay, Graphics.TOP | Graphics.LEFT);

          //#if TouchScreen == true
            StaticUtils.addButton(World.DOWN_PRESSED, ax, ay, World.bodyImg.getWidth(4), World.bodyImg.getHeight(4));
            //#endif
        }

    }

    private static final String[] BAGNAME = new String[]{
                    "基本", "任务", "装备"
    };

    private boolean itemShowInfo = false;
    private boolean itemShowUseMenu = false;
    private boolean itemDropItemConfirm = false;
    private byte itemDropItemConfirmOldSelected;
    private boolean itemShowThrowCount;
    private byte itemBagSelected = 0;
    private byte itmeBagSelecting = 0;
    private byte itemSelected = 0;
    private byte itemMenuSelected = 0;
    private byte itemShowBegin = 0;
    //触摸屏左直选和右直选
    private  static boolean leftKeyDirectChoose = false;
    private  static boolean rightKeyDirectChoose = false;
    private Object[] itemMenu = null;

    private boolean itemShowCurrentEquip = false;

    public void drawEditItem(Graphics g){
        g.setFont(font);
        g.setColor(EDGE_COLOR[4]);
        g.fillRect(0, 0, World.viewWidth, World.viewHeight);
        int x = 8;
        int y = 0;
        int bky = 0;

        Sprite s = World.player;

        int height = LINE_HEIGHT + 6;
        drawEdge(g, 0, 0, y, World.viewWidth, height, true, EDGE_COLOR[3]);
        y = 3;
        World.draw3DString(g, "背包", x, y, Graphics.TOP | Graphics.LEFT, 0xffffff, 0x000000);

        int moneyWidth = String.valueOf(s.money).length() * attrNum.getWidth(0);
        int gWidth = font.stringWidth("金币：");

        x = World.viewWidth - (moneyWidth + 8 + gWidth);
        World.draw3DString(g, "金币：", x, y, Graphics.TOP | Graphics.LEFT, 0xffffff, 0x000000);
        x += gWidth;
        drawAttrNum(String.valueOf(s.money), g, x, height / 2, Graphics.VCENTER | Graphics.LEFT);

        y = height + 2;

        //#if (Directory == Nokia403) || (Directory == SE-K300) || (Directory == SE-K500)
        //# int width = 4 * 8 + font.stringWidth(BAGNAME[itemBagSelected])/* + font.stringWidth(BAGNAME[1]) + font.stringWidth(BAGNAME[2])*/;
        //# drawEdge(g, 0, 0, y, width, height, false, 0);
        //# x = 8;
        //# World.draw3DString(g, BAGNAME[itemBagSelected], x, y + 3 + (LINE_HEIGHT - CHAR_HEIGHT) / 2, Graphics.TOP | Graphics.LEFT, 0xff0000, 0xffffff);
        //#else
        int width = 4 * 8 + font.stringWidth(BAGNAME[0]) + font.stringWidth(BAGNAME[1]) + font.stringWidth(BAGNAME[2]);

        drawEdge(g, 0, 0, y, width, height, false, 0);
        x = 8;
        for(int i = 0; i < BAGNAME.length; i++){
            int clr = 0xffffff;
            int bgClr = 0x000000;
            if(i == itemBagSelected){
                clr = 0xff0000;
                bgClr = 0xffffff;
            }
            World.draw3DString(g, BAGNAME[i], x, y + 3 + (LINE_HEIGHT - CHAR_HEIGHT) / 2, Graphics.TOP | Graphics.LEFT, clr, bgClr);
          //#if TouchScreen == true
            StaticUtils.addButton(3000+i, x, y + 3 + (LINE_HEIGHT - CHAR_HEIGHT) / 2, BAGNAME[i].length()* CHAR_WIDTH, LINE_HEIGHT);
            //#endif
            x += font.stringWidth(BAGNAME[i]) + 8;
        }
        //#endif

        String bagstr = s.getItemTotalCount() + "/" + s.bagSize + "(" + s.petBagSize + ")";
        x = World.viewWidth - bagstr.length() * attrNum.getWidth(0) - 8;
        drawAttrNum(bagstr, g, x, y + height / 2, Graphics.VCENTER | Graphics.LEFT);

        y += height + 2;

        bky = y;

        x = 10;
        y += 3;

        Vector bag = getCurrentBag();
        if(itemSelected >= bag.size()){
            itemSelected = 0;
        }

        int count = (World.viewHeight - bky) / LINE_HEIGHT;
        //#if (Directory == Nokia403) || TouchScreen == true
        count--;
        //#endif

        if(itemSelected == 0){
            itemShowBegin = 0;
        }else if(itemSelected > itemShowBegin + count - 1){
            itemShowBegin = (byte)(itemSelected - count + 1);
        }else if(itemSelected < itemShowBegin){
            itemShowBegin = itemSelected;
        }

        for(int i = itemShowBegin; i < itemShowBegin + count && i < bag.size(); i++){
            GameItem gi = (GameItem)bag.elementAt(i);
            if(i == itemSelected){
                g.setColor(EDGE_COLOR[5]);
                g.fillRect(3, y, World.viewWidth - 6, LINE_HEIGHT);
            }
            gi.drawName(g, x, y + (LINE_HEIGHT - CHAR_HEIGHT) / 2, true, false, World.viewWidth, /*i == itemSelected*/false);
          //#if TouchScreen == true
            StaticUtils.addButton(2000+i, x, y + (LINE_HEIGHT - CHAR_HEIGHT) / 2, gi.getName(true, y-6).length() * CHAR_WIDTH, LINE_HEIGHT);
            //#endif
            y += LINE_HEIGHT;
        }

        drawEdge(g, 0, 0, bky, World.viewWidth, World.viewHeight - bky, false, 0);

        boolean drawUpArrow = false;
        boolean drawDownArrow = false;

        if(itemShowBegin > 0){
            drawUpArrow = true;
        }

        if(itemShowBegin + count < bag.size()){
            drawDownArrow = true;
        }

        if(drawUpArrow){
            int ax = World.viewWidth - 3 - World.bodyImg.getWidth(4);
            int ay = bky + 5 + (int)((World.tick / 5) % 2) * 3;
            World.bodyImg.drawFrame(g, 6, ax, ay, Graphics.TOP | Graphics.LEFT);
          //#if TouchScreen == true
            StaticUtils.addButton(World.UP_PRESSED, ax, ay, World.ok.getWidth(), World.ok.getHeight());
            //#endif
        }
        if(drawDownArrow){
            int ax = World.viewWidth - 3 - World.bodyImg.getWidth(4);
            int ay = World.viewHeight - World.bodyImg.getHeight(5) - 5 - (int)((World.tick / 5) % 2) * 3;
            World.bodyImg.drawFrame(g, 7, ax, ay, Graphics.TOP | Graphics.LEFT);
            
          //#if TouchScreen == true
            StaticUtils.addButton(World.DOWN_PRESSED, ax, ay, World.ok.getWidth(), World.ok.getHeight());
            //#endif
        }

        y = bky;
        if((itemShowInfo || itemShowCurrentEquip) && bag.size() > 0){
            GameItem gi = (GameItem)bag.elementAt(itemSelected);

            if(itemShowCurrentEquip && World.player.playerEquips[gi.equipType].type != GameItem.TYPE_NULL){
                gi = World.player.playerEquips[gi.equipType];
            }

            if(itemShowInfo || (itemShowCurrentEquip && World.player.playerEquips[gi.equipType].type != GameItem.TYPE_NULL)){
                int top = y + itemSelected * LINE_HEIGHT + LINE_HEIGHT / 2;
                int[] wh = gi.calculateInfoTip(true);
                if(top + wh[1] > World.viewHeight){
                    top = World.viewHeight - wh[1];
                }

                int left = font.stringWidth(gi.getName(true, -1) + 10);
                if(left + wh[0] > World.viewWidth)
                    left = World.viewWidth - wh[0];
                gi.drawInfoTip(g, left, top, true, true, false);
            }
        }
        if(itemShowUseMenu){
        	//#if TouchScreen == true
            StaticUtils.removeAllButton();
            //#endif
            if(itemShowThrowCount){
                drawSelectNum(g);
            }else{
                World.battleBout = 12;
                World.drawMenu(g, itemMenu, -1, -1, itemMenuSelected, 0);
            }
        }

        if(World.battleBout > 1){
            World.battleBout--;
            int anchor = Graphics.TOP;
            //int anchor = Graphics.BOTTOM;
            //if(itemSelected - itemShowBegin > count / 2){
            //    anchor = Graphics.TOP;
            //}
            int type = -1;
            if(itemMenu[itemMenuSelected].equals("给宠物使用") || itemMenu[itemMenuSelected].equals("给宠物喂食")){
                type = World.DRAWPLAYERINFO_TYPE_PET;
            }else if(itemMenu[itemMenuSelected].equals("使用")){
                type = World.DRAWPLAYERINFO_TYPE_PLAYER;
            }
            if(type != -1){
                World.drawPlayerInfo(g, anchor, type);
            }
        }
    }

    private Vector getCurrentBag(){
        Vector bag = null;
        switch(itemBagSelected){
            case 0:
                //基本
                bag = World.player.basicItems;
                break;
            //            case 1:
            //                bag = World.player.extendItems;
            //                break;
            case 1:
                bag = World.player.taskItems;
                break;
            case 2:
                bag = World.player.equipsInBag;
                break;
        }
        return bag;
    }

    private int frame;
    private int dir;

    public static final String[] movieMessage = new String[]{
                    ".", "..", "..."
    };

    public static byte movieMessageIndex = 0;
    public static byte movieMessageTick = 0;
    public static final byte movieMessageTickCount = 5;
    private static boolean needUpdate = false;

    private void drawGameMenu(Graphics g){
    	needUpdate = false;
        switch(subState){
            case SS_CHOICE:
            	
                g.fillRect(0, 0, World.viewWidth, World.viewHeight);
               
                if(_image != null)
                    g.drawImage(_image, World.viewWidth / 2, World.viewHeight / 2, Graphics.HCENTER | Graphics.VCENTER);
	              //#if !((Directory == Nokia403) || (Directory == SE-K300) || (Directory == SE-K500) || (Directory == DOPOD-585))
	               //上下按钮
	                int clr = 0xffffff;
	                int bgClr = 0x000000;
	                int lineClr1 = 0xffb86b;
	  			  	int lineClr2 = 0xff9209;
	  			  	int lineClr3 = 0xff9308;
	  			  	//确定5次转一周
	                if(gameMenuCycle){
	                		cycleCount++;
	                		if(5 == cycleCount){
	                			insteadOldMenu = true;
	                		}
		                	if(6 == cycleCount){
		                		cycleCount =0;
		                		gameMenuCycle = false;
		                	}
	                }
	                int startX;
	                int startY;
	                int extendStep = 0;//控制选中的增加距离
	                int insteadFrame =0; //非静态情况下 换帧加4
	                int lineSpreadRate =0; //控制其左右水平伸缩的速度
	                int lineStandRate =0;  //控制其竖直方向的速度
	                	g.setColor(0x000000);
	                    g.fillRect(0, 0, World.viewWidth, World.viewHeight);
	                    
	                    if(_image != null)
	                        g.drawImage(_image, World.viewWidth / 2, World.viewHeight / 2, Graphics.HCENTER | Graphics.VCENTER);
		                for(int i = 1; i < 8; i++){
		                	if(!gameMenuCycle){
		                		if(i==4){
		                			extendStep = 7;
		                			insteadFrame = 4;
		                			clr = 0xffffff;
		                			bgClr = 0x0D13FF;
		                			lineClr1 = 0xff4bab;
		              			  	lineClr2 = 0xff30d8;
		              			  	lineClr3 = 0xff30d1;
		                		}else{
		                			extendStep = 0;
		                			insteadFrame = 0;
		                			clr = 0xffffff;
		                            bgClr = 0x000000;
		                            lineClr1 = 0xffb86b;
		              			  	lineClr2 = 0xff9209;
		              			  	lineClr3 = 0xff9308;
		                		}
		                    }else{//确定旋转速度
		                    	if(upCycle){//取绝对值然后相加/5做为速度
		                    		lineSpreadRate = -(menuCycleStart[i*2] - menuCycleStart[i*2-2])/5;
			                    	lineStandRate = -(menuCycleStart[i*2+1] - menuCycleStart[i*2-1])/5;
		                    	}else{
		                    		lineSpreadRate = (menuCycleStart[i*2+2] - menuCycleStart[i*2])/5;
			                    	lineStandRate = (menuCycleStart[i*2+3] - menuCycleStart[i*2+1])/5;
		                    	}
		                    	
		                    
		                    }
		                	startX= menuCycleStart[i*2];
		                	startY= menuCycleStart[i*2+1];
		                	
		                	_movingMenu.drawFrame(g, 4 + insteadFrame, startX + (lineSpreadRate*cycleCount), startY+lineStandRate*cycleCount-2, Graphics.BOTTOM | Graphics.RIGHT);
		                	_movingMenu.drawFrame(g, 6+ insteadFrame, startX + lineSpreadRate*cycleCount, startY + menuCycleInstance+lineStandRate*cycleCount+2, Graphics.BOTTOM | Graphics.RIGHT);
		                	_movingMenu.drawFrame(g, 5+ insteadFrame, startX+(_stringItems[i].length() +1)* CHAR_WIDTH+ extendStep *2 + lineSpreadRate*cycleCount-leftIsntance*3, startY +lineStandRate*cycleCount-2, Graphics.BOTTOM | Graphics.RIGHT);
		                	_movingMenu.drawFrame(g, 7+ insteadFrame, startX+(_stringItems[i].length() +1) * CHAR_WIDTH+ extendStep *2 + lineSpreadRate*cycleCount-leftIsntance*3, startY + menuCycleInstance+lineStandRate*cycleCount+2, Graphics.BOTTOM | Graphics.RIGHT);
		                	if(!gameMenuCycle){
			                	if(i==4){//画选中框和上下浮动按钮
			                		_movingMenu.drawFrame(g, 12, startX+extendStep-(leftIsntance==0?0:leftIsntance -2), startY+menuCycleInstance-5, Graphics.BOTTOM | Graphics.RIGHT);  
			                		_movingMenu.drawFrame(g, 13, startX+(_stringItems[i].length()+1) * CHAR_WIDTH+ extendStep-leftIsntance*3, startY+menuCycleInstance-5, Graphics.BOTTOM | Graphics.RIGHT);
			                		int skipRate =0;
			                		if(World.tick%5==0){
			                			skipRate = 2;
			                		}
			                		_movingMenu.drawFrame(g, 14, startX+(_stringItems[i].length()/2) * CHAR_WIDTH +CHAR_WIDTH/2+ extendStep, startY-9-skipRate, Graphics.BOTTOM | Graphics.RIGHT);  
			                		_movingMenu.drawFrame(g, 15, startX+(_stringItems[i].length()/2) * CHAR_WIDTH +CHAR_WIDTH/2+ extendStep, startY+20+skipRate, Graphics.BOTTOM | Graphics.RIGHT);
			                		//#if TouchScreen == true
				                    StaticUtils.addButton(3000,startX+(_stringItems[i].length()/2) * CHAR_WIDTH +CHAR_WIDTH/2+ extendStep -CHAR_WIDTH,startY-9-skipRate-9, CHAR_WIDTH+5, 15);
				                    StaticUtils.addButton(3001,startX+(_stringItems[i].length()/2) * CHAR_WIDTH +CHAR_WIDTH/2+ extendStep-CHAR_WIDTH,startY+20+skipRate-9, CHAR_WIDTH+5, 15);	
				                    
				        		  	//#endif
			                	}
		                	}
		                	g.setColor(lineClr1);
		          		  	g.drawLine(startX+ lineSpreadRate*cycleCount, startY-8+lineStandRate*cycleCount-2, startX+(_stringItems[i].length()/2 +2)* CHAR_WIDTH + extendStep *2 + lineSpreadRate*cycleCount+6-leftIsntance*3 + (_stringItems[i].length() ==6?CHAR_WIDTH:0 ),startY-8+lineStandRate*cycleCount-2);
		          		  	g.setColor(lineClr2);
		          		  	g.drawLine(startX+ lineSpreadRate*cycleCount, startY-7+lineStandRate*cycleCount-2, startX+(_stringItems[i].length()/2 +2)* CHAR_WIDTH + extendStep *2 + lineSpreadRate*cycleCount+6-leftIsntance*3+ (_stringItems[i].length() ==6?CHAR_WIDTH:0 ) ,startY-7+lineStandRate*cycleCount-2);
		          		  	g.setColor(lineClr3);
		          		  	g.drawLine(startX+ lineSpreadRate*cycleCount, startY-5+lineStandRate*cycleCount-2, startX+(_stringItems[i].length()/2 +2)* CHAR_WIDTH + extendStep *2 + lineSpreadRate*cycleCount+6-leftIsntance*3+ (_stringItems[i].length() ==6?CHAR_WIDTH:0 ) ,startY-5+lineStandRate*cycleCount-2);
		          		  	g.setColor(lineClr3);
		        		  	g.drawLine(startX+ lineSpreadRate*cycleCount, startY+menuCycleInstance-4+lineStandRate*cycleCount+2, startX+(_stringItems[i].length()/2 +2)* CHAR_WIDTH + extendStep *2 + lineSpreadRate*cycleCount+6-leftIsntance*3+ (_stringItems[i].length() ==6?CHAR_WIDTH:0 ) ,startY+menuCycleInstance-4+lineStandRate*cycleCount+2);
		        		  	g.setColor(lineClr2);
		        		  	g.drawLine(startX+ lineSpreadRate*cycleCount, startY+menuCycleInstance-2+lineStandRate*cycleCount+2, startX+(_stringItems[i].length()/2 +2)* CHAR_WIDTH + extendStep *2 + lineSpreadRate*cycleCount+6-leftIsntance*3+ (_stringItems[i].length() ==6?CHAR_WIDTH:0 ) ,startY+menuCycleInstance-2+lineStandRate*cycleCount+2);
		        		  	g.setColor(lineClr1);
		        		  	g.drawLine(startX+ lineSpreadRate*cycleCount, startY+menuCycleInstance-1+lineStandRate*cycleCount+2, startX+(_stringItems[i].length()/2 +2)* CHAR_WIDTH + extendStep *2 + lineSpreadRate*cycleCount+6-leftIsntance*3+ (_stringItems[i].length() ==6?CHAR_WIDTH:0 ),startY+menuCycleInstance-1+lineStandRate*cycleCount+2);
		        		  	//#if Revision == CMCC || (Revision == JIANGSUNCMCC) 
	                         if (_stringItems[i].equals(GAMEMENU_MOREGAME)) {
		        		  		  bgClr = 0xFFFFFF;	
	                        	 clr = 0xFF0000;
	                         }else{
	                        	 if(i!= 4){
		                        	 clr = 0xffffff;
			                         bgClr = 0x000000;
	                        	 }else if(i == 4){
			                		clr = 0xffffff;
			                		bgClr = 0x0D13FF; 
	                        	 }
	                         }
	                    	//#endif
		        		  	World.draw3DString(g, _stringItems[i],startX + CHAR_WIDTH * _stringItems[i].length()/2 + extendStep + lineSpreadRate*cycleCount -leftIsntance ,startY-6 -leftIsntance/2+lineStandRate*cycleCount  , Graphics.TOP | Graphics.HCENTER, clr, bgClr);
		        		  	//#if TouchScreen == true
		                    StaticUtils.addButton(2000+startIndex + i,startX + CHAR_WIDTH * _stringItems[i].length()/2 + extendStep + lineSpreadRate*cycleCount -leftIsntance -CHAR_WIDTH *2,startY-6 -leftIsntance/2+lineStandRate*cycleCount, CHAR_WIDTH*4, CHAR_HEIGHT);	
		        		  	//#endif
		                }
	
	                     
	                     //#else
	//为小于208手机屏幕做的保留
	              //# int clr = 0x2D4E9D;
	              //# int bgClr = 0xffffff;
	               
	               //获得最宽的菜单长度
	              //# int maxLength = 0; 
	              //# for(int i = 0; i < _stringItems.length; i++){
	                   //# if(maxLength < _stringItems[i].length()){
	                   //# 	maxLength = _stringItems[i].length();
	                   //# }
	                   //#  }
	                   //# for(int i = 0; i < _stringItems.length; i++){
	                   //#  if(startIndex + i == _index){
	                   //# clr = 0xfcfdfe;
	                   //# bgClr = 0x0021ff ;
	                   //#   if(World.tick%10>=0 && World.tick%10 < 5){
	                   //# 	_movingArrow.drawFrame(g, 2, blankStartX - _stringItems[i].length()/2*CHAR_WIDTH, blankStartY + CHAR_HEIGHT * i+CHAR_HEIGHT*3/4, Graphics.BOTTOM | Graphics.RIGHT);
	                   //# 	_movingArrow.drawFrame(g, 0, blankStartX + _stringItems[i].length()/2*CHAR_WIDTH + CHAR_WIDTH/2, blankStartY + CHAR_HEIGHT * i+CHAR_HEIGHT*3/4, Graphics.BOTTOM | Graphics.RIGHT);
	                   //#  }else if(World.tick%10>=5 && World.tick%10 <= 9){
	                   //# 	_movingArrow.drawFrame(g, 1, blankStartX - _stringItems[i].length()/2*CHAR_WIDTH, blankStartY + CHAR_HEIGHT * i+CHAR_HEIGHT/2, Graphics.BOTTOM | Graphics.RIGHT);
	                   //# 	_movingArrow.drawFrame(g, 1, blankStartX + _stringItems[i].length()/2*CHAR_WIDTH + CHAR_WIDTH/2, blankStartY + CHAR_HEIGHT * i+CHAR_HEIGHT/2, Graphics.BOTTOM | Graphics.RIGHT);
	                   //#   }
	                    	//#if Revision == CMCC || (Revision == JIANGSUNCMCC) 
	                        //# if (_stringItems[i].indexOf(GAMEMENU_MOREGAME) >= 0) {
	                        	//# clr = 0xFF0000;
	                        //# }
	                    	//#endif
	                   //#  }else{
	                   //# clr = 0x000000;
	                   //#  bgClr = 0xFFFFFF;
	                    	//#if Revision == CMCC || (Revision == JIANGSUNCMCC) 
	                        //# if (_stringItems[i].equals(GAMEMENU_MOREGAME)) {
	                        	//# clr = 0xFF0000;
	                        //# }
	                    	//#endif
	                   //#  }
	                  //#if TouchScreen == true
	                   //#  StaticUtils.addButton(2000+startIndex + i, blankStartX-2*CHAR_WIDTH, blankStartY + CHAR_HEIGHT * i, CHAR_WIDTH*4, CHAR_HEIGHT);
	                    //#endif
	                   //#   World.draw3DString(g, _stringItems[i], blankStartX, blankStartY + CHAR_HEIGHT * i, Graphics.TOP | Graphics.HCENTER, clr, bgClr);
	                   //#  }
	                //#endif
               break;
            case SS_LOGINING:
            case SS_REGISTERING:
            case SS_MESSAGE:
            case SS_ACTOR_GET_LIST:
            case SS_ACTOR_CREATING:
            case SS_ACTOR_LOGINING:
            case SS_GETSERVERLIST:
            case SS_FAST_REG:
            case SS_FAST_LOGIN:
            case SS_FAST_GET_ACTORLIST:
            case SS_FAST_PLAYER_LOGIN:
            case SS_REGISTER_MESSAGE:
                g.setColor(0x000000);
                g.fillRect(0, 0, World.viewWidth, World.viewHeight);
                if(_image != null)
                    g.drawImage(_image, World.viewWidth / 2, World.viewHeight / 2, Graphics.HCENTER | Graphics.VCENTER);
                //#if (Directory == NK-BigScreen) || (Directory == NK-Nokia403Big) || (Directory == SE-S700)
                //# g.setColor(0x000000);
                //#else
                g.setColor(0xffffff);
                //#endif

                if (_message.indexOf("版本需要更新") >= 0) {
                	needUpdate = true;
                }
                if(subState == SS_ACTOR_LOGINING){
                    String[] msg = World.splitString(_message, World.MESSAGEBOX_WIDTH, font);
                    drawMsgTip(g, -1, -1, msg, null, null, BTNTYPE_FIRE);
                }else{
                    movieMessageTick++;

                    if(movieMessageTick > movieMessageTickCount){
                        movieMessageTick = 0;

                        movieMessageIndex++;

                        if(movieMessageIndex > movieMessage.length - 1){
                            movieMessageIndex = 0;
                        }
                    }

                    repaintNextTime = true;

                    String mstr;
                    if(_message.endsWith("...")){
                        mstr = _message.substring(0, _message.length() - 3);
                    }else{
                        mstr = _message;
                    }
                    String[] mls = World.splitString(mstr, World.viewWidth - 8, font);
                    g.setFont(font);
                    int mly = World.viewHeight - (LINE_HEIGHT + 2) * mls.length - 4;

                    String str;
                    for(int i = 0; i < mls.length; i++){
                        int w = font.stringWidth(mls[i]);
                        str = mls[i];
                        if(i == mls.length - 1){
                            if(_message.endsWith("...")){
                                str += movieMessage[movieMessageIndex];
                            }
                        }

                        World.draw3DString(g, str, (World.viewWidth - w) / 2, mly, Graphics.TOP | Graphics.LEFT, 0x2D4E9D, 0xffffff);

                        mly += LINE_HEIGHT;
                    }
                }

                break;
            case SS_SERVERLIST:
                g.setColor(0x000000);
                g.fillRect(0, 0, World.viewWidth, World.viewHeight);

                String serverListTip = "请选择服务器";
                String nameTip = "";

                if(name != null && name.trim().length() > 0){
                    nameTip += "帐户名：" + name;
                }
                /*
                String serverTip = "";

                if(serverName != null && serverName.trim().length() > 0){
                    serverTip += "上次：" + serverName;
                }
				*/
                World.draw3DString(g, serverListTip,  (World.viewWidth - font.charsWidth(serverListTip.toCharArray(), 0, serverListTip.toCharArray().length)) / 2, 5, Graphics.TOP | Graphics.LEFT, 0xFFFFFF);

                if(nameTip.length() > 0){
                    World.draw3DString(g, nameTip,  (World.viewWidth - font.charsWidth(nameTip.toCharArray(), 0, nameTip.toCharArray().length)) / 2, 5 + CHAR_HEIGHT, Graphics.TOP | Graphics.LEFT, 0xFFFFFF);
                }
                /*
                if(serverTip.length() > 0){
                    World.draw3DString(g, serverTip,  (World.viewWidth - font.charsWidth(serverTip.toCharArray(), 0, serverTip.toCharArray().length)) / 2, 5 + CHAR_HEIGHT * 2, Graphics.TOP | Graphics.LEFT, 0xFFFFFF);
                }
				*/
                
                World.drawMenu(g, serverGroups, -1, -1, _subIndex, 0);
                //#if TouchScreen == false
	                //#if JBlend == false
	                GameState.drawBoxWithString(g, 2, World.viewHeight - CHAR_HEIGHT-8, CHAR_WIDTH * 2+8, CHAR_HEIGHT+8, "确定", true);
	                GameState.drawBoxWithString(g, World.viewWidth - CHAR_WIDTH * 2-8, World.viewHeight - CHAR_HEIGHT-8, CHAR_WIDTH * 2+8, CHAR_HEIGHT+8, "返回", true);
	                //#else
	                //# GameState.drawBoxWithString(g, 2, World.viewHeight - CHAR_HEIGHT-8, CHAR_WIDTH * 2+8, CHAR_HEIGHT+8, "返回", true);
	                //# GameState.drawBoxWithString(g, World.viewWidth - CHAR_WIDTH * 2-8, World.viewHeight - CHAR_HEIGHT-8, CHAR_WIDTH * 2+8, CHAR_HEIGHT+8, "确定", true);
	                //#endif
                //#endif
                break;
            case SS_ACTOR_SELECT_SHOWMENU:
            case SS_ACTOR_SELECT:
            case SS_ACTOR_SELECT_DELETE:
                g.setColor(EDGE_COLOR[4]);
                g.fillRect(0, 0, World.viewWidth, World.viewHeight);

                g.setColor(EDGE_COLOR[3]);
                g.fillRect(2, 2, World.viewWidth - 4, CHAR_HEIGHT+4);

                String[] actor;
                if(!actorPageChange){
	                if(itemShowBegin >= actorList.size()){
	                	itemShowBegin = 0;
	                }
	                if(_subIndex == 0){
	                	itemShowBegin = 0;
	                }else if(_subIndex > itemShowBegin + 4 - 1){
	                	itemShowBegin = (byte)(_subIndex - 4 + 1);
	                }else if(_subIndex < itemShowBegin){
	                	itemShowBegin = (byte) _subIndex;
	                } 
                }
                 
             
                boolean drawUpArrow = false;
                boolean drawDownArrow = false;
                if(itemShowBegin > 0){
                    drawUpArrow = true;
                }

                if(itemShowBegin  + 4 < actorList.size()){
                    drawDownArrow = true;
                }
      
                int j=0;
                for(j = 0 ;j < 4 && (itemShowBegin+j) < actorList.size();j++){      
                    actor = (String[])actorList.elementAt(itemShowBegin+j);
                    int sex = 0;
                    if(!(actor[1]!= null && actor[1].equals(""))){
                        sex = Integer.parseInt(actor[2]);
                    }

                   int left =  2;
                   int top = CHAR_HEIGHT +6+j* ((World.viewHeight - CHAR_HEIGHT - 4) / 4);
                   int width = World.viewWidth - 5;
                   int height = (World.viewHeight - CHAR_HEIGHT - 4) / 4;

                        if(_subIndex == itemShowBegin+j%4){
                        g.setColor(EDGE_COLOR[5]);
                        g.fillRect(left, top, width, height);
                        if(!actor[0].equals("新建角色")){
                           // World.getPlayerImage(World.getFaceIndex(sex, false)).drawFrame(g, Sprite.FRAMESEQUENCE_WALK[dir][frame], x, y, Graphics.BOTTOM | Graphics.HCENTER);
                            frame++;

                            if(World.tick % 50 == 0){
                                frame = 0;
                                switch(dir){
                                    case Sprite.DOWN:
                                        dir = Sprite.LEFT;
                                        break;
                                    case Sprite.LEFT:
                                        dir = Sprite.UP;
                                        break;
                                    case Sprite.UP:
                                        dir = Sprite.RIGHT;
                                        break;
                                    case Sprite.RIGHT:
                                        dir = Sprite.DOWN;
                                        break;
                                }
                            }

                            if(frame >= Sprite.FRAMESEQUENCE_WALK[Sprite.DOWN].length){
                                frame = 0;
                            }
                        }
                    }                   
                   g.setClip(left, top, width, height);
                        
	                  	int sw = font.stringWidth(actor[0]);
	                  	if(actor[1]!= null && actor[1].equals(""))
	                  	{
	                  		sw=sw-CHAR_WIDTH;
	                  	}
	                  	
//	                  	sw=sw+font.stringWidth(actor[1])+CHAR_WIDTH *6;
	            
//	                  	System.out.println(sw);
	                  	if(_subIndex == itemShowBegin+j%4){	                  		
	                  		
		                    if(sw > World.viewWidth-CHAR_WIDTH *7){	
		                    	if(!changDirect){
		                    		taskUITitleScrollOffset++;
			                    	if(taskUITitleScrollOffset > (sw -(World.viewWidth-CHAR_WIDTH *8)))
			                        {
			                    		changDirect=true;
			                        }
			                    	  
		                    	}
		                    	else{
		                    		taskUITitleScrollOffset--;
		                    		if(taskUITitleScrollOffset < 0)
			                        {
		                    			changDirect=false;
			                        }
			                    	
		                    	}
		                  
		                    	World.draw3DString(g, actor[0], CHAR_WIDTH -taskUITitleScrollOffset,
		                    			CHAR_HEIGHT*3/2 +4+(j+1) * ( (World.viewHeight - CHAR_HEIGHT - 4) / 4 )-(World.viewHeight - CHAR_HEIGHT - 4) / 8, Graphics.LEFT | Graphics.BOTTOM, 0xffffff);
//		                    	World.draw3DString(g, actor[1],CHAR_WIDTH *3+font.stringWidth(actor[0])+CHAR_WIDTH-taskUITitleScrollOffset,
//		                        		CHAR_HEIGHT +4+(j+1 )* ((World.viewHeight - CHAR_HEIGHT - 4) / 4 ), Graphics.LEFT | Graphics.BOTTOM, 0xffffff);
		                    	left = World.viewWidth-CHAR_WIDTH *6;
		                    	g.setColor(EDGE_COLOR[5]);
		                        g.fillRect(left, top, width, height);
		                    	
                        World.iconOffset -= World.battleBout;
                        if(World.iconOffset + sw + 10 < width){
                            World.battleBout = -1;
                        }else if(World.iconOffset > 10){
                            World.battleBout = 1;
                        }
                    }else{
                        World.draw3DString(g, actor[0],CHAR_WIDTH ,
                        		CHAR_HEIGHT*3/2 +4+(j+1) * ( (World.viewHeight - CHAR_HEIGHT - 4) / 4 )-(World.viewHeight - CHAR_HEIGHT - 4) / 8, Graphics.LEFT | Graphics.BOTTOM, 0xffffff);
		                    }
	                  	}
	                  	else{
	                        World.draw3DString(g, actor[0],CHAR_WIDTH ,
	                        		CHAR_HEIGHT*3/2 +4+(j+1) * ( (World.viewHeight - CHAR_HEIGHT - 4) / 4 )-(World.viewHeight - CHAR_HEIGHT - 4) / 8, Graphics.LEFT | Graphics.BOTTOM, 0xffffff);
	                        left = World.viewWidth-CHAR_WIDTH *6;
	                    	g.setColor(EDGE_COLOR[4]);
	                        g.fillRect(left, top, width, height);
                 
	                    }
		                 if(!(actor[1]!= null && actor[1].equals(""))){
		                     if(0==World.getFaceIndex(sex, false)){
		                        World.draw3DString(g, "男", World.viewWidth-CHAR_WIDTH *5+2, CHAR_HEIGHT*3/2 +4+(j+1) * ( (World.viewHeight - CHAR_HEIGHT - 4) / 4 )-(World.viewHeight - CHAR_HEIGHT - 4) / 8, Graphics.HCENTER
			                                  | Graphics.BOTTOM, 0xffffff);
		                        	
		                       }else{
		                    	World.draw3DString(g, "女", World.viewWidth-CHAR_WIDTH *5+2, CHAR_HEIGHT*3/2 +4+(j+1) * ( (World.viewHeight - CHAR_HEIGHT - 4) / 4 )-(World.viewHeight - CHAR_HEIGHT - 4) / 8, Graphics.HCENTER
			                                   | Graphics.BOTTOM, 0xffffff);
		                       }
	                     }
	                        
	                    World.draw3DString(g, actor[1], World.viewWidth-CHAR_WIDTH *3+4, CHAR_HEIGHT*3/2 +4+(j+1) * ( (World.viewHeight - CHAR_HEIGHT - 4) / 4 )-(World.viewHeight - CHAR_HEIGHT - 4) / 8, Graphics.HCENTER
		                                   | Graphics.BOTTOM, 0xffffff);
	                    g.setClip(0, 0, World.viewWidth, World.viewHeight);
                 
                 		
                }
                
                
                if(drawUpArrow){
                    int ax = World.viewWidth - 3 - World.bodyImg.getWidth(4);
                    int ay = CHAR_HEIGHT +4 + 5 + (int)((World.tick / 5) % 2) * 3;
                    World.bodyImg.drawFrame(g, 6, ax, ay, Graphics.TOP | Graphics.LEFT);
                }
                if(drawDownArrow){
                    int ax = World.viewWidth - 3 - World.bodyImg.getWidth(4);
                    int ay = World.viewHeight - World.bodyImg.getHeight(5) - 5 - (int)((World.tick / 5) % 2) * 3;
                    World.bodyImg.drawFrame(g, 7, ax, ay, Graphics.TOP | Graphics.LEFT);
                }   
                int tempSize=0;
                if(actorList.size() <= 4){
                	tempSize = actorList.size()-1;
                	if( actorList.size()==4 ){
	                	actor = (String[])actorList.elementAt(3);
	                    if(!(actor[1]!= null && actor[1].equals(""))){
	                    	tempSize++;
	                    }
                	}
                }else{
                	tempSize = actorList.size();
                }
                if( _subIndex+1 <= tempSize){
                	World.draw3DString(g, "角色选择"+"("+(_subIndex+1)+"/"+tempSize+")", World.viewWidth / 2, 4, Graphics.HCENTER | Graphics.TOP, 0xffffff);
                }else{
                	World.draw3DString(g, "新建角色", World.viewWidth / 2, 4, Graphics.HCENTER | Graphics.TOP, 0xffffff);
                }
                drawEdge(g, 0, 0, 0, World.viewWidth, CHAR_HEIGHT+8, false, 0);
                drawEdge(g, 0, 0, CHAR_HEIGHT + 7, World.viewWidth , (World.viewHeight - CHAR_HEIGHT - 4) / 4, false, 0);
                drawEdge(g, 0, 0, CHAR_HEIGHT + 6+ ((World.viewHeight - CHAR_HEIGHT - 4) / 4 ), World.viewWidth , (World.viewHeight - CHAR_HEIGHT - 4) / 4, false, 0);           
                drawEdge(g, 0, 0, CHAR_HEIGHT + 5+ ((World.viewHeight - CHAR_HEIGHT - 4) / 4 )*2, World.viewWidth , (World.viewHeight - CHAR_HEIGHT - 4) / 4, false, 0);
                drawEdge(g, 0, 0, CHAR_HEIGHT + 4+ ((World.viewHeight - CHAR_HEIGHT - 4) / 4 )*3, World.viewWidth , (World.viewHeight - CHAR_HEIGHT - 4) / 4, false, 0);
              //#if TouchScreen == true
                if(subState == SS_ACTOR_SELECT){
	                for(int i=0;i<actorList.size();i++){
	                	if(actorList.elementAt(i)!=null){
		                StaticUtils.addButton(2000+itemShowBegin+i, 0, CHAR_HEIGHT + 7-i+((World.viewHeight - CHAR_HEIGHT - 4) / 4 )*i,World.viewWidth, (World.viewHeight - CHAR_HEIGHT - 4)/ 4);
		                //StaticUtils.addButton(2001, 0, CHAR_HEIGHT + 6+ ((World.viewHeight - CHAR_HEIGHT - 4) / 4 ), World.viewWidth, (World.viewHeight - CHAR_HEIGHT - 4)/ 4);
		               // StaticUtils.addButton(2002, 0, CHAR_HEIGHT + 5+ ((World.viewHeight - CHAR_HEIGHT - 4) / 4 )*2, World.viewWidth, (World.viewHeight - CHAR_HEIGHT - 4)/ 4);
		                //StaticUtils.addButton(2003, 0, CHAR_HEIGHT + 4+ ((World.viewHeight - CHAR_HEIGHT - 4) / 4 )*3, World.viewWidth, (World.viewHeight - CHAR_HEIGHT - 4)/ 4);
	                	}
	                }
        		}
               //#endif
                if(subState == SS_ACTOR_SELECT_SHOWMENU){
                    World.drawMenu(g, ACTOR_MENU, -1, -1, _subMenuIndex, 0);
                }else if(subState == SS_ACTOR_SELECT_DELETE){
                    switch(_delState){
                        case SS_ACTOR_SELECT_DELETE_CONFIRM: {
                            Vector vec = World.formatString("确定要<cff0000>删除角色</c> \"" + actorName + "\" 吗？", World.MESSAGEBOX_WIDTH, font);
                            Object[] strObj = new Object[vec.size()];
                            vec.copyInto(strObj);
                            drawMsgTip(g, -1, -1, strObj, null, null, BTNTYPE_SOFT_LEFTANDRIGHT);
                        }
                            break;
                        case SS_ACTOR_SELECT_DELETE_CONFIRM2: {
                            Vector vec = World.formatString(deleteTimer / 1000 + "秒后将<cff0000>删除角色</c> \"" + actorName + "\" ，按任意键取消！", World.MESSAGEBOX_WIDTH, font);
                            Object[] strObj = new Object[vec.size()];
                            vec.copyInto(strObj);
                            drawMsgTip(g, -1, -1, strObj, null, null, BTNTYPE_NONE);
                        }
                            break;
                        case SS_ACTOR_SELECT_DELETE_DOING:
                            drawMsgTip(g, -1, -1, new String[]{
                                "正在删除角色..."
                            }, null, null, BTNTYPE_NONE);
                            break;
                        case SS_ACTOR_SELECT_DELETE_FAILED:
                            drawMsgTip(g, -1, -1, World.splitString("删除失败:" + taskUIErrorMessage, World.MESSAGEBOX_WIDTH, font), null, null, BTNTYPE_SOFT_LEFTANDRIGHT);
                            break;
                    }
                }
                break;
            case SS_HELP:
            case SS_ABOUT:
                g.setColor(0x000000);
                g.fillRect(0, 0, World.viewWidth, World.viewHeight);

                for(int i = 0; i < infoStrings.length; i++){
                    drawShadowString(g, infoStrings[i], SCREEN_MARGIN, -_scroll + SCREEN_MARGIN + i * CHAR_HEIGHT, true);
                }

                break;
        }
    }

    private int rndX;
    private int rndY;

    private void drawLoading(Graphics g){
        g.setColor(0x000000);
        g.fillRect(0, 0, World.viewWidth, World.viewHeight);
        g.setColor(0xffffff);

        String[] msg = World.splitString(_message, World.MESSAGEBOX_WIDTH, font);
        drawMsgTip(g, -1, -1, msg, null, null, BTNTYPE_FIRE);
        //g.drawString(_message, World.viewWidth / 2, (World.viewHeight - CHAR_HEIGHT) / 2, Graphics.HCENTER | Graphics.TOP);

        if(rndX == -1 && rndY == -1){
            rndY = World.random(World.playerImageSet[0].getHeight(0), World.viewHeight);
            if(dir == Sprite.RIGHT){
                rndX = World.viewWidth;
                dir = Sprite.LEFT;
            }else{
                rndX = 0;
                dir = Sprite.RIGHT;
            }
        }
        if(dir == Sprite.LEFT)
            rndX -= Sprite.STEP;
        else
            rndX += Sprite.STEP;

        int sex = 0;

        if(World.player == null){
            sex = actorSex;
        }else{
            sex = World.player.face;
        }

        World.getPlayerImage(World.getFaceIndex(sex, false)).drawFrame(g, Sprite.FRAMESEQUENCE_WALK[dir][frame], rndX, rndY, Graphics.LEFT | Graphics.BOTTOM);
        frame++;

        if(frame >= Sprite.FRAMESEQUENCE_WALK[dir].length){
            frame = 0;
        }

        if(rndX > World.viewWidth || rndX < -World.playerImageSet[0].getWidth(0)){
            rndX = -1;
            rndY = -1;
            frame = 0;
        }

        World.draw3DString(g, officalWeb, World.viewWidth - CHAR_WIDTH, World.viewHeight - CHAR_HEIGHT, Graphics.BOTTOM | Graphics.RIGHT, 0xffffb6);
    }

    private void createGameMenu(){
	        //#if Revision == QQ
        //#else
		    	//#if ((Directory == Nokia403) || (Directory == SE-K300) || (Directory == SE-K500) || (Directory == DOPOD-585))
		    	//# if(name != null && name.trim().length() > 0){
		    	//#   GAME_MENU[0] = GAMEMENU_FASTLOGIN;
		    	//# }else{
		    	//#    GAME_MENU[0] = GAMEMENU_FASTREG;
		    	//# }
    			//#endif
    	//#endif

        _stringItems = new String[gameMenuRowNum];

        for (int i=0; i<gameMenuRowNum; i++) {
        	 //#if ((Directory == Nokia403) || (Directory == SE-K300) || (Directory == SE-K500) || (Directory == DOPOD-585))
        	//#  _stringItems[i] = "";
        	//# if (startIndex+i == _index) {
        	//# _stringItems[i] =GAME_MENU[_index];
        	//# }else {
        	//# _stringItems[i] = GAME_MENU[startIndex+i];
        	//# }
        	 //#else
        	
        	_stringItems[i] = "";
        	 if(startIndex+i >= GAME_MENU.length){
        		 _stringItems[i] = GAME_MENU[startIndex+i - GAME_MENU.length];
        	 }else{
        		 _stringItems[i] = GAME_MENU[startIndex+i];
        	 }

        	 if(name != null && name.trim().length() > 0){
        		 if(_stringItems[i].equals(GAMEMENU_FASTREG)){
        			 _stringItems[i] = GAMEMENU_FASTLOGIN;
                 }
        	 }
        	 //#endif
        }
    }

    private static ImageSet menuButton;

    private int toState;

    public static final int SS_MAINMENU_FLYING_IN = 0;
    public static final int SS_MAINMENU_NORMAL = 1;
    public static final int SS_MAINMENU_FLYING_OUT = 2;
    public static final int SS_MAINMENU_SHOWSUBMENU = 3;

    public static final int SS_MAINMENU_EXIT = -1;

    /**
     * 斜率K,扩大1000倍
     */
    public static final int K_HORIZONTAL = 200;

    public static final int FLY_STEP = 20;

    public static final int ROUND_STEP = 7;

    public static final int[][] MENUXYSTEP = {
                    {
                                    FLY_STEP * 1000, FLY_STEP * K_HORIZONTAL
                    }, {
                                    FLY_STEP * K_HORIZONTAL, FLY_STEP * 1000
                    }, {
                                    FLY_STEP * 1000, FLY_STEP * K_HORIZONTAL
                    }, {
                                    FLY_STEP * K_HORIZONTAL, FLY_STEP * 1000
                    }
    };

    public static final short LONGLINE = 140;

    public static final short SHORTLINE = 28;

    public static short[][] menuLocation;

    public static short[][] menuStartLocation;

    public static final byte[][] MENUFLYDIR = {
                    {
                                    1, -1
                    }, {
                                    1, 1
                    }, {
                                    -1, 1
                    }, {
                                    -1, -1
                    }
    };

    public static final byte[][] MENUROUNDDIR_RIGHT = {
                    {
                                    1, 1
                    }, {
                                    -1, 1
                    }, {
                                    -1, -1
                    }, {
                                    1, -1
                    }
    };

    public short[][] menuXY;

    private void resetMenuXY(short[][] xy){
        for(int i = 0; i < menuXY.length; i++){
            menuXY[i][0] = xy[i][0];
            menuXY[i][1] = xy[i][1];
        }
    }

    private void drawMainMenu(Graphics g){
        if(menuButton == null){
            menuButton = World.getImageSetFromLocal("menubtn");

        }

        //        g.drawImage(World.backBuffer, World.backBufferX - World.viewX, World.backBufferY - World.viewY, Graphics.TOP | Graphics.LEFT);

        if(menuButton != null){
            if(subState != SS_MAINMENU_SHOWSUBMENU && subState != SS_MAINMENU_EXIT){
                Vector vec = World.formatString("按上下左右或2846键调用相应菜单", World.MESSAGEBOX_WIDTH - GameState.BOX_MARGIN * 2 - GameState.EDGE_WIDTH * 2 - 30, GameState.font);
                Object[] obj = new Object[vec.size()];
                vec.copyInto(obj);

                GameState.drawMsgTip(g, -1, menuLocation[0][1] - obj.length * (CHAR_HEIGHT + 3), obj, null, null, GameState.BTNTYPE_NONE);
            }

            switch(subState){
                case SS_MAINMENU_FLYING_IN: {

                    byte flg = 0;

                    for(int i = 0; i < menuXY.length; i++){
                        menuButton.drawFrame(g, i, menuXY[i][0], menuXY[i][1], Graphics.TOP | Graphics.LEFT);
                        //g.drawImage(menuButton, menuXY[i][0], menuXY[i][1], Graphics.TOP | Graphics.LEFT);
                        if(menuXY[i][0] != menuLocation[i][0] && menuXY[i][1] != menuLocation[i][1]){
                            menuXY[i][0] = (short)((menuXY[i][0] * 1000 + MENUXYSTEP[i][0] * MENUFLYDIR[i][0]) / 1000);
                            menuXY[i][1] = (short)((menuXY[i][1] * 1000 + MENUXYSTEP[i][1] * MENUFLYDIR[i][1]) / 1000);
                        }else{
                            flg |= 1 << i;
                        }

                        if(flg == 0xf){
                            resetMenuXY(menuLocation);
                            subState = SS_MAINMENU_NORMAL;
                        }
                    }

                    break;
                }
                case SS_MAINMENU_FLYING_OUT: {

                    byte flg = 0;

                    for(int i = 0; i < menuXY.length; i++){
                        menuButton.drawFrame(g, i, menuXY[i][0], menuXY[i][1], Graphics.TOP | Graphics.LEFT);
                        //g.drawImage(menuButton, menuXY[i][0], menuXY[i][1], Graphics.TOP | Graphics.LEFT);
                        if(menuXY[i][0] != menuStartLocation[i][0] && menuXY[i][1] != menuStartLocation[i][1]){
                            menuXY[i][0] = (short)((menuXY[i][0] * 1000 - MENUXYSTEP[i][0] * MENUFLYDIR[i][0]) / 1000);
                            menuXY[i][1] = (short)((menuXY[i][1] * 1000 - MENUXYSTEP[i][1] * MENUFLYDIR[i][1]) / 1000);
                        }else{
                            flg |= 1 << i;
                        }

                        if(flg == 0xf){
                            resetMenuXY(menuStartLocation);
                            subState = toState;
                        }
                    }
                    break;
                }
                case SS_MAINMENU_SHOWSUBMENU: {

                    String[] items = SUB_MENU[_index];

                    //#if font == image
                    //# int perfectWid = iFont.stringWidth("左右穴道");
                    //#else
                    int perfectWid = font.stringWidth("左右穴道>");
                    //#endif

                    if(_index == 3){
                        if(World.teamMode){
                            items[3] = "离开队伍";
                        }else{
                            items[3] = "创建队伍";
                        }
                    }

                    World.drawMenu(g, items, -1, -1, _subIndex, 0);
                    break;
                }
                case SS_MAINMENU_NORMAL:
                    for(int i = 0; i < menuXY.length; i++){
                        menuButton.drawFrame(g, i, menuXY[i][0], menuXY[i][1], Graphics.TOP | Graphics.LEFT);
                    }
                    //#if TouchScreen == true
                    for(int i = 0; i < menuXY.length; i++){
                    	StaticUtils.addButton(2000+i, menuXY[i][0], menuXY[i][1], menuButton.getWidth(0), menuButton.getHeight(0));
                    }
                    //#endif
                    break;
                case SS_MAINMENU_EXIT:
                    World.setGameState(null);
                    break;
            }
        }
    }

    private void drawTaskUI(Graphics g){
        if(taskUIReady){
            //drawBox(g, 0, 0, World.viewWidth, TITLE_HEIGHT);
            drawEdge(g, 0, 0, 0, World.viewWidth, TITLE_HEIGHT, true, EDGE_COLOR[3]);

            if(taskUITitle != null){
                if(taskUITitleNeedScroll){
                    int cx, cy, cw, ch, w, h;

                    cx = g.getClipX();
                    cy = g.getClipY();
                    cw = g.getClipWidth();
                    ch = g.getClipHeight();

                    g.setClip(SCREEN_MARGIN + EDGE_WIDTH, BOX_MARGIN + EDGE_WIDTH, World.viewWidth - SCREEN_MARGIN * 2 - EDGE_WIDTH * 2, LINE_HEIGHT);
                    drawShadowString(g, taskUITitle, SCREEN_MARGIN + EDGE_WIDTH + 8 - taskUITitleScrollOffset, BOX_MARGIN + EDGE_WIDTH, Graphics.TOP | Graphics.LEFT, taskUITitleColor);

                    g.setClip(cx, cy, cw, ch);
                }else{
                    drawShadowString(g, taskUITitle, World.viewWidth / 2, BOX_MARGIN + EDGE_WIDTH, Graphics.TOP | Graphics.HCENTER, taskUITitleColor);
                }
            }

            int y = TITLE_HEIGHT + 3;

            if(taskUIType == TASK_UI_LIST){
                g.setColor(EDGE_COLOR[4]);
                g.fillRect(SCREEN_MARGIN + 2, y + 2, World.viewWidth - SCREEN_MARGIN * 2 - 2, World.viewHeight - y - SCREEN_MARGIN - 2);
                y += EDGE_WIDTH + BOX_MARGIN;

                if(taskUIList != null){
                    int cx, cy, cw, ch, w, h;

                    cx = g.getClipX();
                    cy = g.getClipY();
                    cw = g.getClipWidth();
                    ch = g.getClipHeight();
                    w = World.viewWidth - SCREEN_MARGIN * 2 - EDGE_WIDTH * 2;
                    h = font.getHeight();

                    for(int i = 0; i < taskUIList.length; i++){
                        g.setClip(SCREEN_MARGIN + EDGE_WIDTH, y, w, LINE_HEIGHT);

                        if(i == taskUIListSelect){
                            g.setColor(taskUISelectColor);
                            g.fillRect(SCREEN_MARGIN + EDGE_WIDTH, y, w, LINE_HEIGHT);

                            g.setClip(SCREEN_MARGIN + EDGE_WIDTH, y, w, LINE_HEIGHT);
                            World.draw3DString(g, taskUIList[i], SCREEN_MARGIN + EDGE_WIDTH + 8 - taskUIListScrollOffset, y + (LINE_HEIGHT - CHAR_HEIGHT) / 2, Graphics.TOP | Graphics.LEFT,
                                            taskUIListColor[i]);
                        }else{
                            World.draw3DString(g, taskUIList[i], SCREEN_MARGIN + EDGE_WIDTH + 8, y + (LINE_HEIGHT - CHAR_HEIGHT) / 2, Graphics.TOP | Graphics.LEFT, taskUIListColor[i]);
                        }
                      //#if TouchScreen == true
                        StaticUtils.addButton(2000+i, SCREEN_MARGIN + EDGE_WIDTH + 8, y,taskUIList[i].length()*CHAR_WIDTH, LINE_HEIGHT);
                        //#endif

                        y += LINE_HEIGHT;
                    }

                    g.setClip(cx, cy, cw, ch);
                }

                drawEdge(g, 0, SCREEN_MARGIN, TITLE_HEIGHT + 3, World.viewWidth - SCREEN_MARGIN * 2, World.viewHeight - (TITLE_HEIGHT + 3) - SCREEN_MARGIN, false, 0);
            }else if(taskUIType == TASK_UI_CONTENT){
                if(taskUIList != null){
                    drawStringBox(g, SCREEN_MARGIN, y, World.viewWidth - SCREEN_MARGIN * 2, World.viewHeight - y - SCREEN_MARGIN, taskUIList, _scroll);
                }
            }else if(taskUIType == TASK_UI_GAME){
                drawEdge(g, 0, SCREEN_MARGIN, y, World.viewWidth - SCREEN_MARGIN * 2, World.viewHeight - y - SCREEN_MARGIN, true, EDGE_COLOR[4]);

                g.setColor(taskUIGameBackColor);
                g.fillRect(taskUIGameBaseX, taskUIGameBaseY, taskUIGameWidth, taskUIGameHeight);

                for(int i = 0; i < taskUIGameItemCount; i++){
                    int x1, y1, w1, h1;
                    String str;

                    x1 = taskUIGameItemInfo[i][0];
                    y1 = taskUIGameItemInfo[i][1];
                    w1 = taskUIGameItemInfo[i][2];
                    h1 = taskUIGameItemInfo[i][3];

                    g.setColor(taskUIGameItemInfo[i][6]);
                    g.fillRect(x1, y1, w1, h1);
                    g.setColor(0xFF0000);
                    g.drawRect(x1, y1, w1, h1);

                    if(taskUIGameItemInfo[i][5] == 0){
                        continue;
                    }

                    str = "" + taskUIGameItemInfo[i][5];
                    g.setColor(taskUIGameItemInfo[i][7]);
                    g.drawString(str, x1 + w1 / 2, y1 + (h1 - font.getHeight()) / 2, Graphics.TOP | Graphics.HCENTER);
                }
            }

            if(taskUICommandShowing){
                //#if TouchScreen == true
                StaticUtils.removeAllButton();
                //#endif
                drawTaskUIPopMenu(g);
                drawButtons(g, BUTTON_LEFT_RIGHT, false);
            }else{
                drawButtons(g, BUTTON_LEFT_RIGHT, false);
            }
        }
    }

    private static int selectNumMin;
    private static int selectNumMax;
    private static int selectNum;
    private static int selectNumID;

    private static final int SELECTNUM_CHAR_WIDTH = font.stringWidth("0") + 4;
    private static final int SELECTNUM_CHAR_HEIGHT = font.getHeight() + 24;

    public static void initSelectNum(int min, int max){
        selectNumMin = selectNum = min;
        selectNumMax = max;
        selectNumID = 0;
    }

    public static int[] splitNum(int num){
        int calcNum = selectNum;
        int len = String.valueOf(selectNumMax).length();
        int[] n = new int[len];
        for(int i = 0; i < len; i++){
            n[len - i - 1] = calcNum % 10;
            calcNum /= 10;
        }
        return n;
    }

    public static int mergeNum(int[] num){
        int ret = 0;
        for(int i = 0; i < num.length; i++){
            ret += num[i];
            if(i < num.length - 1)
                ret *= 10;
        }
        return ret;
    }

    public static void drawSelectNum(Graphics g){
        int[] n = splitNum(selectNum);
        int len = String.valueOf(selectNumMax).length();
        int bw = SELECTNUM_CHAR_WIDTH * len + 8;
        int bh = SELECTNUM_CHAR_HEIGHT;

        int bx = (World.viewWidth - bw) / 2;
        int by = (World.viewHeight - bh) / 2;

        drawEdge(g, 0, bx, by, bw, bh, true, EDGE_COLOR[4]);

        for(int i = 0; i < n.length; i++){
            World.draw3DString(g, String.valueOf(n[i]), bx + 4 + SELECTNUM_CHAR_WIDTH * i, by + 12, Graphics.TOP | Graphics.LEFT, 0xffffff);
          //#if TouchScreen == true
            StaticUtils.addButton(3000 + i,bx + 4 + SELECTNUM_CHAR_WIDTH * i,  by + 12,12, 24);
            //#endif
        }

        int color = 0xff0000;

        g.setColor(color);
        g.drawRect(bx + 2 + SELECTNUM_CHAR_WIDTH * selectNumID, by + 10, SELECTNUM_CHAR_WIDTH + 2, bh - 20);

        drawButtons(g, (byte)2, bx + 4 + SELECTNUM_CHAR_WIDTH * selectNumID, by + 2 + (int)(World.tick / 5 % 2) * 2);
        drawButtons(g, (byte)3, bx + 4 + SELECTNUM_CHAR_WIDTH * selectNumID, by + bh - 10 + 2 + (int)(1 - World.tick / 5 % 2) * 2);
      //#if TouchScreen == true
        StaticUtils.addButton(World.UP_PRESSED, bx + 4 + SELECTNUM_CHAR_WIDTH * selectNumID, by + 2 + (int)(World.tick / 5 % 2) * 2,
        		12, 12);
        StaticUtils.addButton(World.DOWN_PRESSED, bx + 4 + SELECTNUM_CHAR_WIDTH * selectNumID, by + bh - 10 + 2 + (int)(1 - World.tick / 5 % 2) * 2,
        		12, 12);
        //#endif
    }

    public static void cycleSelectNum(){
        if(World.isKeyPressed(World.UP_PRESSED, true)){
            int[] n = splitNum(selectNum);

            n[selectNumID]++;

            if(n[selectNumID] > 9){
                n[selectNumID] = 0;
            }

            selectNum = mergeNum(n);

            if(selectNum > selectNumMax){
                selectNum = selectNumMax;
            }
        }else if(World.isKeyPressed(World.DOWN_PRESSED, true)){
            int[] n = splitNum(selectNum);

            n[selectNumID]--;

            if(n[selectNumID] < 0){
                n[selectNumID] = 9;
            }

            selectNum = mergeNum(n);

            if(selectNum < selectNumMin){
                selectNum = selectNumMin;
            }

            if(selectNum > selectNumMax){
                selectNum = selectNumMax;
            }
        }else 
        	//#if TouchScreen == true
            if(World.isKeyPressed(World.RIGHT_PRESSED, true) || rightKeyDirectChoose ){
            	//#else
            	//# if(World.isKeyPressed(World.RIGHT_PRESSED, true)){
            	//#endif 
        	//if(World.isKeyPressed(World.RIGHT_PRESSED, true)){
            int[] n = splitNum(selectNum);

            selectNumID++;

            if(selectNumID == n.length){
                selectNumID = 0;
            }
        }else 
        //#if TouchScreen == true
            if(World.isKeyPressed(World.LEFT_PRESSED, true) || leftKeyDirectChoose){
            	//#else
            	//# if(World.isKeyPressed(World.RIGHT_PRESSED, true)){
            	//#endif 
        	//if(World.isKeyPressed(World.LEFT_PRESSED, true)){
            int[] n = splitNum(selectNum);

            selectNumID--;

            if(selectNumID < 0){
                selectNumID = n.length - 1;
            }
        }
    }

    /*---------- Game Functions End----------*/

    /*---------- Net Functions Begin----------*/

    public static final boolean DIRECT_CONNECT = false;

    public static void createConnection() throws Exception{
        if(connection == null){
        	//url = "http://221.179.216.54:7014";
            //url = "socket://119.147.11.91:14002#1779310121E61";
            //#debug
        	//url="socket://221.179.216.50:30001#DDB3D8327533";
            //url = "socket://192.168.50.55:7777";
        //url = "socket://192.168.10.64:7777";
            //url = "socket://192.168.0.54:2259";
            //url = "socket://127.0.0.1:7777";
            //url = "socket://192.168.0.30:7777";
        //1区
            //url = "socket://221.179.216.54:20002";
            //url = "http://221.179.216.54:7001"; 
        //2区
            //url = "socket://221.179.216.54:20003";
            //url = "http://221.179.216.54:7003";
        //3区
            //url = "socket://221.179.216.50:30000";
            //url = "http://221.179.216.54:7005";
        //4区
            //url = "socket://218.206.80.188:33330#DACE50BB6979";
            //url = "http://218.206.80.188:8870";
        //6区
            //url = "socket://218.206.80.188:25001";
            //url = "http://218.206.80.188:25051";
        	//url = "socket://218.206.80.188:33330#DACE50BB61A9";
        //7区
            //url = "socket://221.179.216.50:20001";
            //url = "http://221.179.216.54:7006";
        //8区
            //url = "socket://221.179.216.54:20004";
            //url = "http://221.179.216.54:7014";

            //url = "socket://121.14.74.29:14000";
            //url = "socket://221.179.216.49:29999";

            if(url == null){
                boolean connected = false;

                for(int i = 0; i < URLs.length; i++){
                    url = URLs[i];

                    try{
                        tryConnect(url);
                        connected = true;

                        break;
                    }catch(Exception e){
                        //#debug
                        e.printStackTrace();
                    }
                }

                if(!connected){
                    throw new Exception("f");
                }
            }else{
                tryConnect(url);
            }

            if(url != null && url.startsWith("socket")){
                World.saveData(World.RMS_DATA, World.stringToBytes(url), (byte)4);
            }

            World.saveData(World.RMS_DATA, World.stringToBytes(serverName), (byte)5);
        }
    }

    public static void tryConnect(String inUrl) throws Exception{
        //#debug
        System.out.println("try connect to " + inUrl);
        
        GameState state = new GameState(STATE_TESTNET);
        state._message = inUrl;
        new Thread(state).start();

        long bt = System.currentTimeMillis();

        while (state.testNetResult == 0){
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }

            if(System.currentTimeMillis() - bt > 15000){
                state.testNetResult = 2;
            }
        }

        if (state.testNetResult == 1) {
            connection = state.testNetConnection;
            connection.start();
            ASyncRequestThread.init(GameState.connection);
        } else {
            throw new Exception("f");
        }
    }

    public static void closeConnection(){
        if(connection != null){
            UWAPConnection tmp = connection;
            connection = null;
            tmp.close();
        }
    }

    public static void addSegment(UWAPSegment segment){
        segments.addElement(segment);
    }

    public static void sendRequest(UWAPSegment segment){
        if(GameState.connection != null){
            connection.writeSegment(segment);
        }
    }

    public static final byte DESCTYPE_SKILL = 1;
    public static final byte DESCTYPE_BATTLESKILL = 2;
    public static final byte DESCTYPE_ITEM = 3;

    public static final byte GETTYPE_LEARN = 1;
    public static final byte GETTYPE_VIEW = 2;

    public static int requestChangeEquips(){
        try{
            UWAPSegment segment = new UWAPSegment(CONN_EQU_CHANGED);
            Sprite p = World.player;
            int[] send = new int[18];
            for(int i = 0; i < p.playerEquips.length; i++){
                if(p.playerEquips[i] == null || p.playerEquips[i].type == GameItem.TYPE_NULL){
                    send[i * 2] = -1;
                    send[i * 2 + 1] = -1;
                }else{
                    send[i * 2] = p.playerEquips[i].itemId;
                    send[i * 2 + 1] = p.playerEquips[i].id;
                }
            }
            segment.writeInts(send);
            segment.flush();
            connection.writeSegment(segment);
            equip_net_request.put(new Integer(segment.serial), new Integer(segment.serial));
            equip_request = true;
            GameEvent e = new GameEvent(GameEvent.EVENT_CHANGEEQUIP_CONFIRM, 0, 0);
            World.addEvent(e);
            return segment.serial;
        }catch(IOException e){
            //#debug
            e.printStackTrace();
        }
        return -1;
    }

    public static int requestChangeAttribute(int stradd, int agiadd, int vitadd, int intadd){
        try{
            UWAPSegment segment = new UWAPSegment(CONN_ADD_PROPERTY_POINT);

            byte[] send = new byte[]{
                            (byte)vitadd, (byte)stradd, (byte)agiadd, (byte)intadd
            };
            segment.writeBytes(send);
            segment.flush();
            connection.writeSegment(segment);
            pro_request = true;
            pro_net_request.put(new Integer(segment.serial), new Integer(segment.serial));
            GameEvent e = new GameEvent(GameEvent.EVENT_CHANGEATTRIBUTE_CONFIRM, 0, 0);
            World.addEvent(e);
            return segment.serial;
        }catch(IOException e){
            //#debug
            e.printStackTrace();
        }
        return -1;
    }

    public static void touchNpc(int npcId){
        touchNpcInfo = new int[3];
        touchNpcInfo[0] = World.sendRequest(CONN_TOUCH_NPC, new Object[]{
            new Integer(npcId)
        }, false);
        touchNpcInfo[1] = npcId;
        touchNpcInfo[2] = 60000;
    }

    public static void cycleSegments(){
        while(segments.size() > 0){
            UWAPSegment segment = (UWAPSegment)segments.elementAt(0);
            segments.removeElementAt(0);
            ASyncRequestThread.removeFromSendedList(segment);
            
            //#if Revision == CMCC || (Revision == JIANGSUNCMCC) 
            if(cmccPoint != null){
                cmccPoint.cycleSegment(segment);
                continue;
            }
            //#endif
            
            cycleSegment(segment);
        }
    }

    public static void cycleSegment(UWAPSegment segment){
        GameState state = World.gameState;
        Integer iSerial = new Integer(segment.serial);
        switch(segment.type){
            case CONN_GOTO:
            case CONN_REFRESH:
            case CONN_SEND_POSITION:
            case CONN_BATTLE_ROUND_END:
            case CONN_PK_ROUND_END:
            case CONN_BATTLE_ABORT:
                World.cycleSegment(segment);

                break;

            case CONN_NETSPEED_TEST: //测试网速
                byte[] ostb = segment.readBytes();
                long feedbackTime = System.currentTimeMillis() - UWAPSegment.getNumber(ostb, 0, ostb.length);
                if(feedbackTime < 1000){
                    World.netSpeedLevel = 0;
                }else if(feedbackTime < 2000){
                    World.netSpeedLevel = 1;
                }else if(feedbackTime < 4000){
                    World.netSpeedLevel = 2;
                }else{
                    World.netSpeedLevel = 3;
                }
                break;

            case CONN_QUICK_REG:
                name = segment.readString();
                password = segment.readString();
                actorName = segment.readString();

                byte quick_type = segment.readByte();

                if(quick_type == 0 || quick_type == 1){
                    state.subState = SS_FAST_LOGIN;
                    state.thread = null;
                }

                break;
            case CONN_SYNCTIME:
                serverTime = segment.readInt();
                lastSyncTime = System.currentTimeMillis();

                break;
            case CONN_ERROR:
                segment.readByte();
                String cause = segment.readString();
                //#debug
                System.out.println(cause);

                if(state != null && state.serial == segment.serial){
                    if(state.type == STATE_GAMEMENU){
                        state._message = cause;
                    }
                }

                for(int i = 0; i < World.events.size(); i++){
                    GameEvent event = (GameEvent)World.events.elementAt(i);
                    if(event.serial == segment.serial){
                        switch(event.getType()){
                            case GameEvent.EVENT_RESOURCE_GATHER: {
                                event.sdata[1] = cause;
                                event.idata[1] = 1;
                                break;
                            }
                        }
                    }
                }

                if(pro_request){
                    if(pro_net_request.containsKey(iSerial)){
                        pro_net_request.remove(iSerial);
                        pro_data_error = true;
                        pro_data_ok = true;
                        taskUIErrorMessage = cause;
                    }
                }

                if(actorDelete_request){
                    if(actorDelete_net_request.containsKey(iSerial)){
                        actorDelete_net_request.remove(iSerial);
                        actorDelete_error = true;
                        actorDelete_ok = false;
                        taskUIErrorMessage = cause;
                    }
                }

                if(equip_request){
                    if(equip_net_request.containsKey(iSerial)){
                        equip_net_request.remove(iSerial);
                        equip_data_error = true;
                        equip_data_ok = true;
                        taskUIErrorMessage = cause;
                    }
                }

                if(uiRequestTaskId > 0){
                    if(uiDataNetRequest.containsKey(iSerial)){
                        uiDataNetRequest.remove(iSerial);
                        uiDataDataOk = true;
                        uiDataDataError = true;
                        taskUIErrorMessage = cause;
                    }
                }

                if(World._gtvm != null){
                    World._gtvm.endTaskResult(-1, segment.serial, false);
                }

                if(touchNpcInfo != null && touchNpcInfo[0] == segment.serial){
                    touchNpcInfo = null;
                }

                break;
            case CONN_ACCOUNTREG_OK:
                password = segment.readString();
                state._message = segment.readString();
                state.subState = SS_REGISTER_MESSAGE;
                state.thread = null;
                reloginTimes = 0;
                gameIsOk = false;

                World.saveData(World.RMS_DATA, World.stringToBytes(name), (byte)1);
                World.saveData(World.RMS_DATA, World.stringToBytes(password), (byte)2);

                break;
            case CONN_LOGINOK:
                if(fastWay){
                    state.subState = SS_FAST_GET_ACTORLIST;
                    state.thread = null;
                }else{
                    state.subState = SS_ACTOR_GET_LIST;
                    state.thread = null;
                }

                state._message = "正在下载角色信息...";

                break;
            case CONN_GET_ACTORLIST_OK: {
                String actorName;
                String actorLevel;
                byte actorSex;
                byte actorReborn;

                state.actorList = new Vector();
                state.actorNameList = new Vector();
                byte count = segment.readByte();

                for(int i = 0; i < count; i++){
                    actorName = segment.readString();
                    actorLevel = " " + String.valueOf((int)segment.readShort()) + "级";
                    actorSex = segment.readByte();
                    actorReborn = segment.readByte();

                    state.actorList.addElement(new String[]{
                                    actorName, actorLevel, String.valueOf(actorSex), String.valueOf(actorReborn)
                    });
                    state.actorNameList.addElement(actorName);
                }

                actorName = "新建角色";
                actorLevel = "";

                state.actorList.addElement(new String[]{
                                actorName, actorLevel
                });
                //人物 超过 4个则 不能新建
                if(state.actorList.size()>4)
                	state.actorList.removeElementAt(state.actorList.size()-1);
                if(fastWay){
                    state.subState = SS_FAST_PLAYER_LOGIN;
                    state.thread = null;
                }else{
                    state.subState = SS_ACTOR_SELECT;
                    state._subIndex = 0;
                    state.thread = null;
                }

                state._message = "下载角色信息成功";
            }

                break;
            case CONN_ACTORCREATE_OK:
                segment.readInt(); //PlayerId Skip
                state.actorName = segment.readString();
                segment.readBytes(); //Player Data skip
                connectionId = segment.readInt();
                state.actorMaxLevel = segment.readInt();

                state._message = "正在登录角色...";
                state.subState = SS_ACTOR_LOGINING;
                state._subIndex = 0;
                state.thread = null;

                break;
            case CONN_PALYER_LOGIN_OK: {
                state.thread = null;
                fastWay = false;
                canFastChecked = false;

                state = new GameState(GameState.STATE_LOADING);
                int pid = segment.readInt(); //PlayerId Skip
                actorName = segment.readString();
                segment.readInt(); //Modify name times Skip
                state.loadMapId = segment.readShort();
                state.isMapXY = false;
                state.startX = segment.readShort();
                state.startY = segment.readShort();

                //#debug
                System.out.println("LOGIN " + state.loadMapId + " , " + state.startX + " , " + state.startY);

                World.initPlayer(state.startX, state.startY);

                byte[] playerData = segment.readBytes();
                World.player.initPlayerData(actorName, playerData, false);

                connectionId = segment.readInt();
                actorMaxLevel = segment.readInt();

                byte[] skills = segment.readBytes();
                Skill.addSkills(skills);

                World.player.initSkillTable();
                World.player.playerID = pid;

                serverTip = segment.readString();

                state._message = "登录角色成功";

                World.setGameState(state);

                World.saveData(World.RMS_DATA, World.stringToBytes(name), (byte)1);
                World.saveData(World.RMS_DATA, World.stringToBytes(password), (byte)2);
                World.saveData(World.RMS_DATA, World.stringToBytes(actorName), (byte)3);
            }

                break;
            case CONN_RELOGIN_RESULT: {
                byte reloginResult = segment.readByte();
                //#debug
                System.out.println("Relogin Result: " + reloginResult);

                if(reloginResult == 0){
                    state = new GameState(GameState.STATE_LOADING);
                    int pid = segment.readInt(); //PlayerId Skip
                    actorName = segment.readString();
                    segment.readInt(); //Modify name times;
                    state.loadMapId = segment.readShort();
                    state.isMapXY = false;
                    state.startX = segment.readShort();
                    state.startY = segment.readShort();

                    byte[] playerData = segment.readBytes();
                    World.player.initPlayerData(actorName, playerData, true);

                    connectionId = segment.readInt();
                    actorMaxLevel = segment.readInt();

                    byte[] skills = segment.readBytes();
                    Skill.addSkills(skills);

                    World.player.initSkillTable();

                    World.player.playerID = pid;

                    state._message = "重连成功";

                    reloginTimes = 0;
                    gameIsOk = true;

                    World.requestSendPosition();

                    World.sendRequest(GameState.CONN_TEAM_LEAVE, new Object[]{
                                    new Integer(World.teamId), new Integer(0), new Byte(World.TEAM_STATUS_LEAVE)
                    }, false);

                    World.releaseTeam();

                    try{
                        if(World.nowBattle >= 0){
                            state.runLeaveBattle();
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                    World.setGameState(null);
                }else{
                    GameState.closeConnection();
                    state = new GameState(STATE_RELOGIN);
                    World.setGameState(state);
                }
            }

                break;
            case CONN_UPLOAD_OK:
                if(logouting){
                    exitToGameMenu(null, false);
                }

                break;
            case CONN_GET_ITEM: {
                segment.readByte();
                byte count = segment.readByte();
                //#debug
                World.log("获得物品" + count + "个：", true);

                Vector vec = new Vector();
                try{
                    for(int i = 0; i < count; i++){
                        byte[] dropInfo = segment.readBytes();
                        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(dropInfo));
                        byte type = dis.readByte();
                        Vector di = null;

                        switch(type){
                            case 1:
                                //属性
                                di = World.player.updateAttributes(dis);
                                for(int j = 0; j < di.size(); j++){
                                    vec.addElement(di.elementAt(j));
                                }

                                break;
                            case 2:
                                //基本物品
                                di = World.player.readBasicItems(dis, true);
                                for(int j = 0; j < di.size(); j++){
                                    vec.addElement(di.elementAt(j));
                                }
                                break;
                            case 3:
                                //任务物品
                                di = World.player.readTaskItems(dis, true);
                                for(int j = 0; j < di.size(); j++){
                                    vec.addElement(di.elementAt(j));
                                }
                                break;
                            case 4:
                                //扩展物品
                                di = World.player.readExtendItems(dis, true);
                                for(int j = 0; j < di.size(); j++){
                                    vec.addElement(di.elementAt(j));
                                }
                                break;
                            case 5:
                                //装备
                                di = World.player.readEquItems(dis, true);
                                for(int j = 0; j < di.size(); j++){
                                    vec.addElement(di.elementAt(j));
                                }
                                break;
                            case 6:
                                //buf
                                World.player.readBuf(dis);
                                break;
                            case 7:
                                //移除装备
                                byte equipCount = dis.readByte();
                                //#debug
                                System.out.println("移除：" + equipCount + "件装备");

                                for(int k = 0; k < equipCount; k++){
                                    int equipItemId = dis.readInt();
                                    int equipId = dis.readInt();
                                    GameItem needRemove = null;

                                    for(int j = 0; j < World.player.equipsInBag.size(); j++){
                                        GameItem tmpEquip = (GameItem)World.player.equipsInBag.elementAt(j);

                                        if(tmpEquip.itemId == equipItemId && tmpEquip.id == equipId){
                                            needRemove = tmpEquip;

                                            break;
                                        }
                                    }

                                    if(needRemove != null){
                                        World.player.equipsInBag.removeElement(needRemove);
                                    }
                                    //#debug
                                    System.out.println("移除装备：" + needRemove.name + " , " + needRemove.itemId + " , " + needRemove.id);
                                }

                                break;
                            case 8:
                                //添加宠物
                                di = World.player.readPet(dis);
                                for(int j = 0; j < di.size(); j++){
                                    vec.addElement(di.elementAt(j));
                                }
                                break;
                            case 9:
                                //宠物逃跑
                                int petCount = dis.readByte();

                                for(int j = 0; j < petCount; j++){
                                    int petId = dis.readInt();

                                    PetSprite runAwayPet = World.player.getPet(petId);

                                    if(runAwayPet != null){
                                        //#debug
                                        System.out.println("你的宠物：" + runAwayPet.name + " , " + runAwayPet.petId + " 离开你了");
                                        World.showMessage("你的宠物：" + runAwayPet.name + " 离开你了", (byte)5);
                                    }

                                    World.player.petBag.remove(new Integer(petId));
                                    World.monsterSetPetBattle = false ;
                                    if(World.player.petCurrent != null && World.player.petCurrent.petId == petId){
                                        World.player.petCurrent = null;
                                    }
                                }

                                break;
                            case 10: //装备损耗
                                int durabilityCount = dis.readByte();

                                for(int j = 0; j < durabilityCount; j++){
                                    GameItem tmpItem = new GameItem(GameItem.TYPE_EQUIP);

                                    tmpItem.itemId = dis.readInt();
                                    tmpItem.id = dis.readInt();
                                    tmpItem.currentDurability = dis.readShort();

                                    GameItem foundEquip = World.player.hasItem(tmpItem, false);

                                    if(foundEquip != null){
                                        foundEquip.currentDurability = tmpItem.currentDurability;
                                        foundEquip.repairFee = 0;

                                        //#debug
                                        System.out.println(foundEquip.getName(false, -1) + " , " + foundEquip.currentDurability + "/" + foundEquip.durability + " , " + foundEquip.repairFee);
                                    }
                                }

                                if(World.nowBattle < 0){
                                    World.player.reCalculateAttributes();
                                }

                                break;
                            case 11: //装备绑定
                                int bindCount = dis.readByte();

                                for(int j = 0; j < bindCount; j++){
                                    int itemId = dis.readInt();
                                    int id = dis.readInt();

                                    for(int k = 0; k < World.player.equipsInBag.size(); k++){
                                        GameItem tmpItem = (GameItem)World.player.equipsInBag.elementAt(k);

                                        if(tmpItem != null && tmpItem.itemId == itemId && tmpItem.id == id){
                                            tmpItem.bind = true;
                                        }
                                    }

                                    for(int k = 0; k < World.player.playerEquips.length; k++){
                                        if(World.player.playerEquips[k] != null && World.player.playerEquips[k].type != GameItem.TYPE_NULL && World.player.playerEquips[k].itemId == itemId
                                                        && World.player.playerEquips[k].id == id){
                                            World.player.playerEquips[k].bind = true;
                                        }
                                    }
                                }

                                break;
                        }
                    }

                    if(vec.size() != 0){
                        Object objs[] = new Object[vec.size()];
                        vec.copyInto(objs);

                        for(int i = 0; i < objs.length; i++){
                            for(int j = i + 1; j < objs.length; j++){
                                if(objs[j] instanceof int[] && !(objs[i] instanceof int[])){
                                    Object tmp = objs[i];
                                    objs[i] = objs[j];
                                    objs[j] = tmp;
                                    continue;
                                }else if(objs[j] instanceof GameItem && objs[i] instanceof GameItem){
                                    GameItem gi = (GameItem)objs[i];
                                    GameItem gj = (GameItem)objs[j];
                                    if(gj.type < gi.type){
                                        Object tmp = objs[i];
                                        objs[i] = objs[j];
                                        objs[j] = tmp;
                                    }
                                }else if(objs[i] instanceof PetSprite && !(objs[j] instanceof PetSprite)){
                                    Object tmp = objs[i];
                                    objs[i] = objs[j];
                                    objs[j] = tmp;
                                    continue;
                                }
                            }
                        }

                        GameEvent e = new GameEvent(GameEvent.EVENT_SHOWGETITEM, 1, 0);
                        e.idata[0] = 10000;
                        e.odata = objs;

                        GameEvent event = World.getEvent(GameEvent.EVENT_SHOWGETITEM);

                        if(event == null){
                            World.addEvent(e);
                        }else{
                            event.idata[0] = 10000;
                            boolean found = false;
                            for(int i = 0; i < objs.length; i++){
                                found = false;
                                if(objs[i] instanceof GameItem){
                                    GameItem item = (GameItem)objs[i];
                                    for(int j = 0; j < event.odata.length; j++){
                                        if(event.odata[j] instanceof GameItem){
                                            if(item.equals(event.odata[j])){
                                                GameItem oldItem = (GameItem)event.odata[j];
                                                //TODO think about it
                                                oldItem.count += item.count;
                                                found = true;
                                                break;
                                            }
                                        }
                                    }
                                }else if(objs[i] instanceof int[]){
                                    int[] v = (int[])objs[i];
                                    for(int j = 0; j < event.odata.length; j++){
                                        if(event.odata[j] instanceof int[]){
                                            int[] ov = (int[])event.odata[j];
                                            if(v[0] == ov[0]){
                                                ov[1] += v[1];
                                                found = true;
                                                break;
                                            }
                                        }
                                    }
                                }

                                if(!found){
                                    Object[] oldObjs = event.odata;
                                    event.odata = new Object[event.odata.length + 1];

                                    if(objs[i] instanceof GameItem){
                                        System.arraycopy(oldObjs, 0, event.odata, 0, oldObjs.length);
                                        event.odata[event.odata.length - 1] = objs[i];
                                    }else if(objs[i] instanceof PetSprite){
                                        System.arraycopy(oldObjs, 0, event.odata, 0, oldObjs.length);
                                        event.odata[event.odata.length - 1] = objs[i];
                                    }else{
                                        System.arraycopy(oldObjs, 0, event.odata, 1, oldObjs.length);
                                        event.odata[0] = objs[i];
                                    }
                                }
                            }
                        }
                    }
                }catch(IOException e){
                    //#debug
                    e.printStackTrace();
                }

                if(uiDataRequest && uiRequestTaskId != World.TASK_ID_MAIL_VIEW){
                    if(uiDataNetRequest.containsKey(iSerial)){
                        uiDataNetRequest.remove(iSerial);
                        uiDataDataError = false;
                        uiDataDataOk = true;
                    }
                }
            }

                break;
            case CONN_EQU_CHANGED_OK:
                equip_data_ok = true;
                equip_net_request.remove(iSerial);

                break;
            case CONN_DESC: {
                byte skillType = segment.readByte();

                switch(skillType){
                    case TOUCH_NPC_PRODUCT_SKILL:
                    case TOUCH_NPC_BATTLE_SKILL:
                        uiDataSegment = segment;
                        uiDataDataOk = true;
                        uiDataNetRequest.remove(iSerial);

                        break;
                    default:
                        String desc = segment.readString();
                        GameItem item = GameItem.getItemFromRequestMap(segment.serial);
                        if(item != null){
                            item.desc = desc;
                            item.download_desc_state = 1;
                        }

                        break;
                }
            }

                break;
            case CONN_GATHER_OK:
                World.resourceLockOk = true;

                break;
            case CONN_ADD_PROPERTY_POINT:
                pro_data_ok = true;
                pro_net_request.remove(iSerial);

                break;
            case CONN_CHAT_MESSAGE:
                GameEvent msg = new GameEvent(GameEvent.NET_CHAT_MESSAGE, 5, 2);

                msg.idata[0] = segment.readInt(); //Source id
                msg.sdata[0] = segment.readString(); //Source name
                msg.idata[1] = segment.readInt(); //message type
                msg.sdata[1] = segment.readString(); //content

                if(World.inBlackList(msg.idata[0]))
                    break;

                //#debug
                System.out.println("聊天信息：" + msg.sdata[1]);

                if(msg.idata[1] >= 0){
                    msg.idata[1] = World.NET_CHAT_TYPE_PRIVATE;
                }

                if(msg.idata[1] == World.NET_CHAT_TYPE_PRIVATE){
                    addChatMsgSource(msg.sdata[0]);
                }

                int idx = msg.idata[1] * (-1) - 1;
                msg.idata[2] = World.net_chat_priority_option[idx];
                msg.idata[3] = World.net_chat_color_option[idx];
                msg.sdata[0] = World.NET_CHAT_NAME[idx] + msg.sdata[0];

                msg.idata[4] = 0; //shown

                if(msg.idata[2] == 1){
                    World.addNetChatMessage(World.net_chat_low_priority_message, msg);
                }else if(msg.idata[2] == 2){
                    World.addNetChatMessage(World.net_chat_high_priority_message, msg);
                    World.showHint(World.HINT_MESSAGE);
                }

                break;

            case CONN_BATTLE_INIT:
                if(state == null || segment.serial == state.serial){
                    World.instance.saveNetChatInputData();

                    if(state == null){
                        state = new GameState(GameState.STATE_LOADING);
                        state.subState = GameState.SS_LOADING_SERVERBATTLE;
                        state.loadServerBattleState = LSBS_WAITING;
                        state.serial = segment.serial;
                        World.setGameState(state);
                    }

                    state.pkid = segment.readInt(); //pkid
                    state.teamid = segment.readInt(); //team id

                    int n = segment.readByte();
                    World w = World.instance;

                    w.armySprite = new ArmySprite[3];

                    byte canCatch;
                    short level;
                    int hp, mp, hpMax, mpMax, pngid;
                    String name;

                    byte petType, petCanCatch;
                    short petLevel;
                    int petHp, petMp, petHpMax, petMpMax;
                    String petName;

                    for(int i = 0; i < n; i++){
                        hp = segment.readInt();
                        mp = segment.readInt();
                        hpMax = segment.readInt();
                        mpMax = segment.readInt();
                        pngid = segment.readInt();
                        name = segment.readString();
                        level = segment.readShort();
                        canCatch = segment.readByte();

                        petType = segment.readByte();
                        petHp = segment.readInt();
                        petMp = segment.readInt();
                        petHpMax = segment.readInt();
                        petMpMax = segment.readInt();
                        petName = segment.readString();
                        petLevel = segment.readShort();
                        petCanCatch = segment.readByte();

                        w.armySprite[i] = new ArmySprite(hp, mp, (short)pngid, name);
                        w.armySprite[i].setLocalIndex(n == 1? World.LOCATION_INDEX_MIDDLE: (byte)(i + 1));
                        w.armySprite[i].id = (byte)i;
                        w.armySprite[i].petType = canCatch;
                        w.armySprite[i].level = level;
                        w.armySprite[i].attributes[BattleSprite.ATTR_HPMAX] = hpMax;
                        w.armySprite[i].attributes[BattleSprite.ATTR_MPMAX] = mpMax;

                        if(petType < 0){
                            w.armyPet[i] = null;
                        }else{
                            w.armyPet[i] = new PetSprite(w.armySprite[i], petType, petHp, petMp, petName);
                            w.armyPet[i].canCatch = petCanCatch;
                            w.armyPet[i].level = petLevel;
                            w.armyPet[i].attributes[BattleSprite.ATTR_HPMAX] = petHpMax;
                            w.armyPet[i].attributes[BattleSprite.ATTR_MPMAX] = petMpMax;
                        }
                    }

                    World.instance.playerSprite[0] = World.player;

                    if(World.player.petCurrent != null && World.player.petCurrent.canBattle()){
                        World.instance.playerPet[0] = World.player.petCurrent;
                    }else{
                        World.instance.playerPet[0] = null;
                    }

                    World.battleBout = 0;

                    state.loadServerBattleState = LSBS_JOIN;
                }

                break;
            case CONN_BATTLE_JOIN_RESULT:
                if(segment.serial == state.serial){
                    byte errCode = segment.readByte();
                    cause = segment.readString();
                    if(errCode == 0){
                        state.loadServerBattleState = LSBS_JOIN_SUCCESS;
                    }else{
                        state.loadServerBattleState = LSBS_JOIN_FAILED;
                        state._message = cause;
                    }
                }

                break;
            case CONN_BATTLE_START: {
                int pkid = segment.readInt();
                int teamid = segment.readInt();

                //if(pkid == state.pkid && teamid == state.teamid /*&& segment.serial == state.serial*/){
                int n = segment.readByte();
                int[] userid = new int[n];
                Sprite s;
                byte weapon;

                byte petType;
                int petIndex = 0, petHp, petMp, petMaxHp, petMaxMp;
                String petName;

                for(int i = 0; i < n; i++){
                    userid[i] = segment.readInt();
                    s = null;

                    for(int j = 0; j < World.instance.playerSprite.length; j++){
                        if(World.instance.playerSprite[j] != null && World.instance.playerSprite[j].playerID == userid[i]){
                            s = World.instance.playerSprite[j];
                            petIndex = j;
                        }
                    }

                    if(s == null){
                        s = new Sprite(null, (short)0, (short)0);
                        s.playerID = userid[i];

                        for(int id = 0; id < World.teamMembers.size(); id++){
                            MonsterSprite teamMember = (MonsterSprite)World.teamMembers.elementAt(id);

                            if(teamMember.id == s.playerID){
                                s.sex = (byte)teamMember.iconID;
                                s.face = s.sex;
                                s.name = teamMember.playerName;
                                s.imageSet = teamMember.imageSet;

                                break;
                            }
                        }

                        if(World.instance.playerSprite[1] == null){
                            World.instance.playerSprite[1] = s;
                            petIndex = 1;
                            s.setLocalIndex(World.LOCATION_INDEX_TOP);
                            s.battleStart(Sprite.LEFT);
                        }else{
                            World.instance.playerSprite[2] = s;
                            petIndex = 2;
                            s.setLocalIndex(World.LOCATION_INDEX_BOTTOM);
                            s.battleStart(Sprite.LEFT);
                        }
                    }

                    s.hp = s.hpShow = segment.readInt();
                    s.mp = s.mpShow = segment.readInt();
                    s.attributes[BattleSprite.ATTR_HPMAX] = segment.readInt();
                    s.attributes[BattleSprite.ATTR_MPMAX] = segment.readInt();

                    weapon = segment.readByte();

                    if(weapon >= 0){
                        s.setWeaponSequence(Sprite.FRAMESEQUENCE_WEAPON[weapon + 1]);
                    }

                    int sealSkill = segment.readInt();
                    boolean sealAllSkill = segment.readBoolean();

                    if(s.playerID == World.player.playerID){
                        World.battleSkillSeal = (byte)((sealSkill >> 16) & 0xFF);
                        World.battleCanAction = sealAllSkill;
                    }

                    s.debufStatus = sealSkill & 0xFF;

                    petType = segment.readByte();
                    petHp = segment.readInt();
                    petMp = segment.readInt();
                    petMaxHp = segment.readInt();
                    petMaxMp = segment.readInt();
                    petName = segment.readString();

                    sealSkill = segment.readInt();
                    sealAllSkill = segment.readBoolean();

                    if(petType < 0){
                        World.instance.playerPet[petIndex] = null;
                    }else{
                        PetSprite tmpPet;

                        if(World.player.playerID == userid[i]){
                            tmpPet = World.player.petCurrent;
                        }else{
                            tmpPet = new PetSprite(s, petType, 0, 0, petName);
                        }

                        if(tmpPet != null){
                            tmpPet.hp = tmpPet.hpShow = petHp;
                            tmpPet.mp = tmpPet.mpShow = petMp;
                            tmpPet.attributes[BattleSprite.ATTR_HPMAX] = petMaxHp;
                            tmpPet.attributes[BattleSprite.ATTR_MPMAX] = petMaxMp;
                            World.instance.playerPet[petIndex] = tmpPet;

                            tmpPet.battleStart(Sprite.LEFT);

                            if(petIndex == 0){
                                tmpPet.setLocalIndex(World.LOCATION_INDEX_MIDDLE);
                            }else if(petIndex == 1){
                                tmpPet.setLocalIndex(World.LOCATION_INDEX_TOP);
                            }else{
                                tmpPet.setLocalIndex(World.LOCATION_INDEX_BOTTOM);
                            }

                            if(s.playerID == World.player.playerID){
                                World.battlePetSkillSeal = (byte)((sealSkill >> 16) & 0xFF);
                                World.battlePetCanAction = sealAllSkill;
                            }

                            tmpPet.debufStatus = sealSkill & 0xFF;
                        }
                    }
                }

                Sprite[] news = {
                                null, null, null
                };

                PetSprite[] newPets = {
                                null, null, null
                };

                for(int i = 0; i < userid.length; i++){
                    for(int j = 0; j < 3; j++){
                        if(World.instance.playerSprite[j] != null && World.instance.playerSprite[j].playerID == userid[i]){
                            news[i] = World.instance.playerSprite[j];
                            newPets[i] = World.instance.playerPet[j];
                        }
                    }
                }

                World.instance.playerSprite = news;
                World.instance.playerPet = newPets;
                World.tempTeamNumber = 0;
                if(World.instance.playerSprite != null){
                    for(int i = 0; i < World.instance.playerSprite.length; i++){
                        if(World.instance.playerSprite[i] != null ){
                        	World.tempTeamNumber ++;
                        }
                    }
                }
                if(World.tempTeamNumber != World.teamNumber){
                	World.monsterSetPlayerBattle = true;
                	World.teamNumber = World.tempTeamNumber;
                }
                World.tempTeamNumber = 0 ;
                World.serverBattle = true ;
                World.battleBout = 0;
            }

                break;
            case CONN_PK_CREATED:
                if(World.nowBattle >= 0){
                    World.instance.endBattle();
                }

                uiDataSegment = segment;
                uiDataDataOk = true;
                uiDataNetRequest.remove(iSerial);

                World.player_PKID = segment.readInt();

                break;
            case CONN_PK_REQUEST: {
                GameEvent tmpEvent = new GameEvent(GameEvent.EVENT_PK_REQUEST, 6, 1);
                tmpEvent.idata[0] = segment.readInt(); //source id
                tmpEvent.sdata[0] = segment.readString(); //source name
                tmpEvent.idata[1] = segment.readInt(); //target id
                tmpEvent.idata[2] = segment.readShort(); //target level
                tmpEvent.idata[3] = segment.readShort(); //money
                tmpEvent.idata[4] = segment.readInt(); //PK id
                tmpEvent.idata[5] = ASyncRequestThread.TIME_SEND_TIMEOUT;

                tmpEvent.sdata[0] += " " + tmpEvent.idata[2] + "级 向你发出PK请求，赌注 " + tmpEvent.idata[3] + "J。\n" + "7.同意\n9.拒绝";

                String[] msgs = World.splitString(tmpEvent.sdata[0], World.MESSAGEBOX_WIDTH - GameState.BOX_MARGIN * 2 - GameState.EDGE_WIDTH * 2, GameState.font);
                tmpEvent.sdata = msgs;

                if((World.nowBattle < 0 && !World.hasEventDoing(tmpEvent)) && (World.teamStatus != World.TEAM_STATUS_FOLLOW || World.teamLeader)){
                    World.addEvent(tmpEvent);
                }else{
                    //World.requestPKRefuse(tmpEvent.idata[4]);
                    World.sendRequest(GameState.CONN_PK_REFUSE, new Object[]{
                                    new Byte((byte)0), "我正忙", new Integer(tmpEvent.idata[4])
                    }, false);
                }
            }

                break;
            case CONN_PK_REFUSE:
                uiDataSegment = segment;
                uiDataDataOk = true;
                uiDataNetRequest.remove(iSerial);

                segment.readByte();
                taskUIErrorMessage = segment.readString();
                //#debug
                System.out.println(taskUIErrorMessage);
                segment.readInt();

                uiDataDataError = true;

                break;
            case CONN_PK_START: {
                World.instance.armySprite = new ArmySprite[3];
                World.instance.playerSprite = new Sprite[3];

                World.player_PKID = segment.readInt(); //pkId

                byte count;

                count = segment.readByte();

                int[] side = new int[count];
                boolean ourSide = false;

                for(int i = 0; i < count; i++){
                    side[i] = segment.readInt();

                    if(side[i] == World.player.playerID){
                        ourSide = true;
                    }
                }

                int[] sideOur = null;

                for(int i = 0; i < 2; i++){
                    count = segment.readByte();

                    if(ourSide){
                        sideOur = new int[count];
                    }

                    for(int j = 0; j < count; j++){
                        int id = segment.readInt(); //id

                        if(ourSide){
                            sideOur[j] = id;
                        }

                        readPkStartInfo(segment, ourSide, id);
                    }

                    ourSide = !ourSide;
                }

                Sprite[] news = new Sprite[3];
                PetSprite[] newPets = new PetSprite[3];

                for(int i = 0; i < sideOur.length; i++){
                    for(int j = 0; j < 3; j++){
                        if(World.instance.playerSprite[j] != null && World.instance.playerSprite[j].playerID == sideOur[i]){
                            news[i] = World.instance.playerSprite[j];
                            newPets[i] = World.instance.playerPet[j];
                        }
                    }
                }

                World.instance.playerSprite = news;
                World.instance.playerPet = newPets;

                //#debug
                System.out.println("PK id = " + World.player_PKID);

                World.pkBattle = true;

                clearTaskUI();
                World.instance.readyBattle(World.player_PKID);
                World.setGameState(null);

                uiDataSegment = segment;
                uiDataDataOk = true;
                uiDataNetRequest.remove(iSerial);
            }

                break;
            case CONN_TEAM_CREATE_OK:
                World.teamId = segment.readInt();
                World.teamMode = true;
                World.teamLeader = true;
                World.teamStatus = World.TEAM_STATUS_FOLLOW;
                World.teamLeaderId = World.player.playerID;

                MonsterSprite me = new MonsterSprite(MonsterSprite.TYPE_NETPLAYER);

                me.id = World.teamLeaderId;
                me.followMode = true;
                me.teamRole = MonsterSprite.TEAM_ROLE_LEADER;
                me.alertRange = World.currMapId;
                me.rx = World.player.x;
                me.ry = World.player.y;
                me.petCurrent = World.player.petCurrent;
                me.playerName = World.player.name;
                me.imageSet = World.player.imageSet;
                me.iconID = World.player.face;

                World.addTeamMember(me);

                break;
            case CONN_TEAM_INVITE: {
                World.teamId = segment.readInt();
                World.teamLeaderId = segment.readInt();
                String teamLeaderName = segment.readString();
                segment.readInt(); //Target Id

                GameEvent tmpEvent = new GameEvent(GameEvent.EVENT_TEAM_INVITE, 1, 1);
                tmpEvent.sdata[0] = teamLeaderName + "邀请你加入队伍。\n1.同意\n2.拒绝";
                tmpEvent.idata[0] = ASyncRequestThread.TIME_SEND_TIMEOUT;

                String[] msgs = World.splitString(tmpEvent.sdata[0], World.MESSAGEBOX_WIDTH - GameState.BOX_MARGIN * 2 - GameState.EDGE_WIDTH * 2, GameState.font);
                tmpEvent.sdata = msgs;

                if(World.nowBattle < 0 || !World.hasEventDoing(tmpEvent)){
                    World.addEvent(tmpEvent);
                }else{
                    World.sendRequest(GameState.CONN_TEAM_INVIT_RESULT, new Object[]{
                                    new Integer(World.teamId), new Byte(World.TEAM_INVITE_REFUSE), ""
                    }, false);
                }
            }

                break;
            case CONN_TEAM_JOIN_OK: {
                World.teamId = segment.readInt();//队伍ID
                int count = segment.readByte();//当前队伍成员个数

                for(int i = 0; i < count; i++){
                    int id = segment.readInt();
                    byte status = segment.readByte();
                    String name = segment.readString();

                    MonsterSprite netPlayer = World.findInNetPlayers(id);
                    MonsterSprite teamMember = new MonsterSprite(MonsterSprite.TYPE_NETPLAYER);

                    if(id == World.player.playerID && World.teamLeader){
                        continue;
                    }

                    if(netPlayer != null){
                        try{
                            teamMember.loadNetPlayer(netPlayer, true);
                        }catch(Exception e){
                            //#debug
                            e.printStackTrace();
                        }
                    }else{
                        teamMember.id = id;

                        if(id == World.player.playerID){
                            teamMember.alertRange = World.currMapId;
                            teamMember.rx = World.player.x;
                            teamMember.ry = World.player.y;
                            teamMember.petCurrent = World.player.petCurrent;
                            teamMember.imageSet = World.player.imageSet;
                            teamMember.iconID = World.player.face;
                        }else{
                            teamMember.alertRange = -1;
                            teamMember.rx = 0;
                            teamMember.ry = 0;
                            teamMember.imageSet = World.playerImageSet[0];
                            teamMember.iconID = 0;
                        }
                    }

                    if(i == 0){
                        World.teamLeaderId = id;
                        teamMember.teamRole = MonsterSprite.TEAM_ROLE_LEADER;
                    }else{
                        teamMember.teamRole = MonsterSprite.TEAM_ROLE_MEMBER;
                    }

                    if(status == World.TEAM_STATUS_NOTFOLLOW){
                        teamMember.followMode = false;
                    }else{
                        teamMember.followMode = true;
                    }

                    teamMember.playerName = name;

                    World.addTeamMember(teamMember);

                    if(netPlayer == null){
                        if(id != World.player.playerID){
                            World.addNetPlayerData(teamMember, true);
                        }
                    }else{
                        netPlayer.teamRole = teamMember.teamRole;
                        netPlayer.followMode = teamMember.followMode;
                    }
                }

                World.reGroupTeam();

                if(World.teamMode){
                    uiDataSegment = segment;
                    uiDataDataOk = true;
                    uiDataNetRequest.remove(iSerial);

                    if(uiDataRequest){
                        clearTaskUI();
                    }
                }else{
                    World.teamMode = true;
                    World.teamLeader = false;
                    World.teamStatus = World.TEAM_STATUS_NOTFOLLOW;
                }

                //#if CommandEmu == true
                //# World.processCommand();
                //#endif
            }
                break;
            case CONN_TEAM_JOIN_FAIL:
                World.showMessage("加入队伍失败", (byte)0);

                break;
            case CONN_TEAM_LEAVE: {
                segment.readInt(); //team id
                int userId = segment.readInt(); //user id
                byte status = segment.readByte();

                if(status == World.TEAM_STATUS_LEAVE){
                    MonsterSprite teamMember = World.findTeamMember(userId);

                    if(teamMember != null){
                        String message = "" + teamMember.playerName + "已经离开队伍";

                        if(teamMember.id == World.teamLeaderId){
                            message += "，您的队伍已解散";

                            if(teamMember.alertRange == World.currMapId && World.teamStatus == World.TEAM_STATUS_FOLLOW){
                                World.player.addWayPoint((short)teamMember.rx, (short)teamMember.ry);

                                //#debug
                                System.out.println("add way point " + teamMember.rx + "_" + teamMember.ry);
                            }

                            World.releaseTeam();
                        }else{
                            World.removeTeamMember(teamMember.id);
                        }

                        World.showMessage(message, (byte)0);
                    }
                }else{
                    MonsterSprite teamMember = World.findTeamMember(userId);

                    if(teamMember != null){
                        if(status == World.TEAM_STATUS_FOLLOW){
                            teamMember.followMode = true;
                            teamMember.wpList = null;
                        }else{
                            teamMember.followMode = false;

                            MonsterSprite leader = World.findTeamMember(World.teamLeaderId);

                            if(leader != null && leader.alertRange == World.currMapId){
                                if(userId == World.player.playerID){
                                    World.player.wpList = null;
                                    World.player.addWayPoint((short)leader.rx, (short)leader.ry);
                                    World.player.leaveParty = true;
                                }
                            }
                        }

                        if(userId == World.player.playerID){
                            if(status == World.TEAM_STATUS_FOLLOW){
                                World.teamStatus = World.TEAM_STATUS_FOLLOW;
                                World.player.setState(Sprite.STATE_WAYPOINT);
                            }else{
                                World.teamStatus = World.TEAM_STATUS_NOTFOLLOW;
                                World.player.leaveParty = true;
                            }
                        }
                    }
                }

                //#if CommandEmu == true
                //# World.processCommand();
                //#endif

                World.reGroupTeam();
            }

                break;
            case CONN_TEAM_INVIT_RESULT: {
                segment.readInt(); //teamId
                byte type = segment.readByte(); //type
                String reason = segment.readString(); //reason;

                uiDataSegment = segment;
                uiDataDataOk = true;
                uiDataNetRequest.remove(iSerial);

                if(type == World.TEAM_INVITE_ACCEPT){
                    uiDataDataError = false;
                }else{
                    uiDataDataError = true;
                    taskUIErrorMessage = reason;
                }
            }

                break;
            case CONN_MESSAGE: {
                String message = segment.readString();

                World.showMessage(message, (byte)10);

                if(touchNpcInfo != null && touchNpcInfo[0] == segment.serial){
                    touchNpcInfo = null;
                }
            }

                break;
            case CONN_SHOP_CREATE_OK: {
                segment.readInt(); //shop id
                String shopName = segment.readString();
                int money = segment.readInt();

                World.player.money -= money;
                World.showMessage("您的商铺\"" + shopName + "\"设立成功\n费用" + money + "J", (byte)0);
            }

                break;
            case CONN_NEWMAIL: {
                //#debug
                System.out.println("new mail : ");
                World.showHint(World.HINT_MAIL);
            }

                break;
            case CONN_TONG_CREATE_OK: {
                String name = segment.readString();
                byte duty = segment.readByte();
                int money = segment.readInt();

                World.player.tongName = name;
                World.player.tongDuty = duty;
                World.player.money -= money;

                String message = "建立公会成功：" + name + " , " + "费用" + " , " + money + "J";
                World.showMessage(message, (byte)0);
                //#debug
                System.out.println(message);
            }

                break;
            case CONN_TONG_GRANT_OK: {
                int id = segment.readInt();
                String name = segment.readString();
                byte duty = segment.readByte();

                if(id == World.player.playerID){
                    World.player.tongName = name;
                    World.player.tongDuty = duty;

                    clearTaskUI();
                }
                //#debug
                System.out.println("公会变更：" + id + " , " + name + " , " + Sprite.getTongDutyName(duty));
            }

                break;
            case CONN_FRIEND_STATUS: {
                int count = segment.readByte();

                for(int i = 0; i < count; i++){
                    int playerId = segment.readInt();
                    boolean online = segment.readBoolean();
                    short friendDegree = segment.readShort();

                    World.changeFriendStatus(playerId, friendDegree, online, true);

                    if(World.netFriendsNeedShowStatus){
                        String friendMessage = World.getFriendsStatus(playerId);

                        if(friendMessage != null){
                            World.showMessage(friendMessage, (byte)3);
                        }
                    }
                }

                World.netFriendsNeedShowStatus = true;

                uiDataSegment = segment;
                uiDataDataOk = true;
                uiDataNetRequest.remove(iSerial);
            }

                break;
            case CONN_TASK_COMPLETED_OK: {
                short taskId = segment.readShort();
                segment.readShort(); //分支id
                boolean allEnd = segment.readBoolean();

                World._gtvm.endTaskResult(taskId, segment.serial, allEnd);
            }

                break;
            case CONN_DELETE_USER_OK: {
                actorDelete_ok = true;
                actorDelete_net_request.remove(iSerial);

                actorName = "";
                World.saveData(World.RMS_DATA, World.stringToBytes(actorName), (byte)3);
            }

                break;
            case CONN_SNEAK_ATTACK: {
                int pkId = segment.readInt();

                if(World.nowBattle >= 0){
                    World.sendRequest(GameState.CONN_PK_REFUSE, new Object[]{
                                    new Byte((byte)0), "对方正在战斗状态", new Integer(pkId)
                    }, false);
                }else{
                    uiDataSegment = segment;
                    uiDataDataOk = true;
                    uiDataNetRequest.remove(iSerial);

                    World.player_PKID = pkId;

                    if(uiDataRequest){
                        clearTaskUI();
                    }

                    World.sendRequest(GameState.CONN_PK_OK, new Object[]{
                                    new Integer(World.player_PKID), new Short(World.player.level)
                    }, false);
                }
            }

                break;
            case CONN_ABILITY_LIST:
            case CONN_SKILL_LIST:
            case CONN_SHOP_LIST:
            case CONN_AUCTION_TYPE_LIST:
            case CONN_BUY_MATERIAL_TYPE_LIST:
            case CONN_OEM_TYPE_LIST:
            case CONN_STORE_ITEM_LIST:
            case CONN_REPAIRE_LIST:
            case CONN_GENERIC_LIST:
            case CONN_ISHOP_LIST:
                uiDataSegment = segment;
                uiDataDataOk = true;
                uiDataNetRequest.remove(iSerial);

                if(touchNpcInfo != null && touchNpcInfo[0] == segment.serial && !uiDataRequest){
                    World.startServerPushTaskUI(segment.type, touchNpcInfo[1]);
                }else if(segment.type == CONN_GENERIC_LIST){
                    World.startServerPushTaskUI(segment.type, 0);
                }

                touchNpcInfo = null;

                break;
            case CONN_BBS_GET_LIST:
            case CONN_BBS_CONTENT:
            case CONN_BBS_POST_OK:
            case CONN_MAIL_POST_OK:
            case CONN_GET_ATTACHMENT_OK:
            case CONN_MAIL_LIST:
            case CONN_MAIL_CONTENT:
            case CONN_LEARN_ABILITY_OK:
            case CONN_LEARN_SKILL_OK:
            case CONN_CHATFAVORITE_LIST:
            case CONN_CHATFAVORITE_DESC:
            case CONN_LOOK_EQU_OK:
            case CONN_SHOP_ITEM_LIST:
            case CONN_SHOP_ADD_ITEM_OK:
            case CONN_SHOP_REMOVE_ITEM_OK:
            case CONN_SHOP_MONEY_CHANGE_OK:
            case CONN_SHOP_CHANGE_OK:
            case CONN_AUCTION_ITEM_OK:
            case CONN_AUCTION_LIST:
            case CONN_AUCTION_ITEM_DESC:
            case CONN_AUCTION_PRICE_OK:
            case CONN_BUY_MATERIAL_LIST:
            case CONN_SELL_MATERIAL_OK:
            case CONN_OEM_LIST:
            case CONN_OEM_OK:
            case CONN_STORE_TRADE_OK:
            case CONN_ADD_FRIEND_OK:
            case CONN_REQUEST_TASK_DESC_OK:
            case CONN_TASK_ABANDON_RESULT:
            case CONN_ADD_POINT_OK:
            case CONN_TONG_MEMBERS_LIST:
            case CONN_USE_PET_OK:
            case CONN_ADD_PET_POINT_OK:
            case CONN_REPAIRE_OK:
            case CONN_GENERIC_CONTENT:
            case CONN_ISHOP_TRADE:
                uiDataSegment = segment;
                uiDataDataOk = true;
                uiDataNetRequest.remove(iSerial);

                break;
            case CONN_FACE_LIST: {
                byte faceCount = segment.readByte();
                short[] faceList = new short[faceCount];
                for(int kk = 0; kk < faceCount; kk++){
                    faceList[kk] = segment.readShort();
                }
                for(int kk = 0; kk < faceCount; kk++){
                    // 读取形象图片
                    Object[] data = new Object[8];
                    for(int kkk = 0; kkk < 4; kkk++){
                        byte type = segment.readByte();
                        data[type << 1] = segment.readBytes();
                        data[(type << 1) + 1] = segment.readBytes();
                    }

                    // 把新形象图片插入到数组中，如果已经满了，则丢弃
                    if(World.faceIndex[faceList[kk]] != 0){
                        continue;
                    }
                    for(int kkk = 2; kkk < World.faceData.length; kkk++){
                        if(World.faceData[kkk] == null){
                            World.faceData[kkk] = data;
                            World.faceIndex[faceList[kk]] = (byte)(kkk + 1);
                            break;
                        }
                    }
                }

                //更新主角形象图片
                World.player.refreshImageSet();

                break;
            }
            //#if Revision == QQ
            //#if (Directory == SE-K300) || (Directory == SE-K500) || (Directory == SE-K700)
            //#else
            case (byte)152:
            	qqOKMsg = segment.readString();
                break;
            //#endif
            //#endif
        }
    }

    public static void readPkStartInfo(UWAPSegment segment, boolean ourSide, int id){
        int hp, mp, hpLimit, mpLimit, sex;
        String name;
        byte weapon;

        byte petType;
        int petHp, petMp, petMaxHp, petMaxMp;
        String petName;

        byte index = 0;

        name = segment.readString(); //name
        hp = segment.readInt(); //hp
        mp = segment.readInt(); //mp
        hpLimit = segment.readInt(); //hpLimit
        mpLimit = segment.readInt(); //mpLimit
        sex = segment.readByte(); //sex

        weapon = segment.readByte();

        int sealSkill = segment.readInt();
        boolean sealAllSkill = segment.readBoolean();

        petType = segment.readByte();
        petHp = segment.readInt();
        petMp = segment.readInt();
        petMaxHp = segment.readInt();
        petMaxMp = segment.readInt();
        petName = segment.readString();

        int sealPetSkill = segment.readInt();
        boolean sealPetAllSkill = segment.readBoolean();

        if(ourSide){
            Sprite s = null;

            if(id == World.player.playerID){
                s = World.player;
                World.battleSkillSeal = (byte)((sealSkill >> 16) & 0xFF);
                World.battleCanAction = sealAllSkill;
                index = 0;
            }else{
                s = new Sprite(World.player.imageSet, (short)0, (short)0);

                if(World.instance.playerSprite[1] == null){
                    index = 1;
                }else{
                    index = 2;
                }
            }

            if(s != World.player){
                s.name = name;
                s.sex = (byte)sex;
                s.face = s.sex;
            }

            s.playerID = id;
            s.hp = s.hpShow = hp;
            s.mp = s.mpShow = mp;
            s.attributes[BattleSprite.ATTR_HPMAX] = hpLimit;
            s.attributes[BattleSprite.ATTR_MPMAX] = mpLimit;

            s.debufStatus = sealSkill & 0xFF;

            if(weapon >= 0){
                s.setWeaponSequence(Sprite.FRAMESEQUENCE_WEAPON[weapon + 1]);
            }

            PetSprite tmpPet = null;

            if(petType < 0){
                tmpPet = null;
            }else{
                if(index == 0){
                    if(World.player.petCurrent == null){
                        tmpPet = null;
                    }else{
                        tmpPet = World.player.petCurrent;
                    }
                }else{
                    tmpPet = new PetSprite(s, petType, 0, 0, petName);
                }

                if(tmpPet != null){
                    tmpPet.hp = tmpPet.hpShow = petHp;
                    tmpPet.mp = tmpPet.mpShow = petMp;
                    tmpPet.attributes[BattleSprite.ATTR_HPMAX] = petMaxHp;
                    tmpPet.attributes[BattleSprite.ATTR_MPMAX] = petMaxMp;

                    if(id == World.player.playerID){
                        World.battlePetSkillSeal = (byte)((sealPetSkill >> 16) & 0xFF);
                        World.battlePetCanAction = sealPetAllSkill;
                    }

                    tmpPet.debufStatus = sealPetSkill & 0xFF;
                }
            }

            World.instance.playerSprite[index] = s;
            World.instance.playerPet[index] = tmpPet;

            s.battleStart(Sprite.LEFT);

            if(tmpPet != null){
                tmpPet.battleStart(Sprite.LEFT);
            }

            if(index == 0){
                s.setLocalIndex(World.LOCATION_INDEX_MIDDLE);

                if(tmpPet != null){
                    tmpPet.setLocalIndex(World.LOCATION_INDEX_MIDDLE);
                }
            }else if(index == 1){
                s.setLocalIndex(World.LOCATION_INDEX_TOP);

                if(tmpPet != null){
                    tmpPet.setLocalIndex(World.LOCATION_INDEX_TOP);
                }
            }else{
                s.setLocalIndex(World.LOCATION_INDEX_BOTTOM);

                if(tmpPet != null){
                    tmpPet.setLocalIndex(World.LOCATION_INDEX_BOTTOM);
                }
            }
        }else{
            if(World.instance.armySprite[0] == null){
                index = 0;
            }else if(World.instance.armySprite[1] == null){
                index = 1;
            }else{
                index = 2;
            }

            ArmySprite s = new ArmySprite(hp, mp, hpLimit, mpLimit, sex, name);
            s.id = (byte)0;
            s.debufStatus = sealSkill & 0xFF;

            if(weapon >= 0){
                s.setWeaponSequence(ArmySprite.FRAMESEQUENCE_WEAPON[weapon + 1]);
            }else{
                s.setWeaponSequence(null);
            }

            PetSprite tmpPet = null;

            if(petType >= 0){
                tmpPet = new PetSprite(s, petType, 0, 0, petName);
                tmpPet.hp = tmpPet.hpShow = petHp;
                tmpPet.mp = tmpPet.mpShow = petMp;
                tmpPet.attributes[BattleSprite.ATTR_HPMAX] = petMaxHp;
                tmpPet.attributes[BattleSprite.ATTR_MPMAX] = petMaxMp;
                tmpPet.debufStatus = sealPetSkill & 0xFF;
            }

            World.instance.armySprite[index] = s;
            World.instance.armyPet[index] = tmpPet;

            if(tmpPet != null){
                tmpPet.battleStart(Sprite.RIGHT);
            }

            if(index == 0){
                s.setLocalIndex(World.LOCATION_INDEX_MIDDLE);

                if(tmpPet != null){
                    tmpPet.setLocalIndex(World.LOCATION_INDEX_MIDDLE);
                }
            }else if(index == 1){
                s.setLocalIndex(World.LOCATION_INDEX_TOP);

                if(tmpPet != null){
                    tmpPet.setLocalIndex(World.LOCATION_INDEX_TOP);
                }
            }else{
                s.setLocalIndex(World.LOCATION_INDEX_BOTTOM);

                if(tmpPet != null){
                    tmpPet.setLocalIndex(World.LOCATION_INDEX_BOTTOM);
                }
            }
        }
    }

    /*---------- Net Functions End----------*/

    /*---------- Tools Functions Begin----------*/

    public static String getModel(){
        //#if M-Name == DOPOD_D600
        //# return "NK-Big";
    	//#elif M-Name == NK_5500
        //# return "Nokia5500";
    	//#elif M-Name == NK_5200
        //# return "Nokia5200";
    	//#elif M-Name == Midp2_Touch
        //# return "Midp2Touch";
    	//#elif M-Name == Midp2_General
        //# return "Midp2Small";
    	//#elif M-Name == SAM_L288
        //# return "SAML288";
    	//#elif M-Name == ZTE_U981
        //# return "ZTEU981";
    	//#elif Directory == ZTE_U860
    	//# return "ZTE_U860";
        //#elif M-Name == DOPOD_S700
        //# return "DOPODS700";
    	//#elif Directory == NK_5800
        //# return "Nokia5800";
    	
    	
        //#elif Directory == NK-60-2
        return "NK-6600";
        
        //#elif Directory == NK-BigScreen
        //# return "NK-Big";
        //#elif Directory == Nokia403
        //# return "NK-403S";
        //#elif Directory == NK-Nokia403Big
        //# return "NK-403";
        //#elif Directory == NK3250
        //# return "NK-3250";
        //#elif Directory == NK-E61
        //# return "NK-Big";
        //#elif Directory == NK-6681
        //# return "NK-6681";
        //#elif Directory == NK-NGage
        //# return "NK-NGage";
        //#elif Directory == SE-S700
        //# return "SE-S700";
        //#elif Directory == SE-K700
        //# return "SE-K700";
        //#elif Directory == SE-K500
        //# return "SE-K500";
        //#elif Directory == SE-K300
        //# return "SE-K300";
        //#elif Directory == MT-V300
        //# return "MotoV300";
        //#elif Directory == NK402
        //# return "NK-40-2";
        //#elif Directory == NK-6255
        //# return "NK-6255";
        //#elif Directory == MT-General
        //# return "Moto-All";
        //#elif Directory == Midp2-General
        //# return "Midp2Small";
        //#elif Directory == SAM_L288
        //# return "SAML288";
        //#elif Directory == DOPOD-585
        //# return "DOPOD-585";
        //#elif Directory == ZTE_U860
        //# return "NK-NGage";
        //#elif Directory == ZTE_U980
        //# return "NK-NGage";
        //#elif Directory == ClientTouch-E680
        //# return "MotoE680";
        //#elif Directory == ClientTouch--Midp2-General
        //# return "Midp2Touch";
        //#elif Directory == ClientTouch-SE-General
        //# return "ClientTouch-SE-General";
        //#elif Directory == ClientTouch-Nokia5800
        //# return "Nokia5800";
        //#endif
    }

    public static void drawStringBox(Graphics g, int x, int y, int w, int h, String[] ss, int scroll){
        //drawBox(g, x, y, w, h);
        drawEdge(g, 0, x, y, w, h, true, EDGE_COLOR[4]);

        int clipX = g.getClipX();
        int clipY = g.getClipY();
        int clipW = g.getClipWidth();
        int clipH = g.getClipHeight();
        g.setClip(x + EDGE_WIDTH + BOX_MARGIN, y + EDGE_WIDTH + BOX_MARGIN, w - (EDGE_WIDTH + BOX_MARGIN) * 2, h - (EDGE_WIDTH + BOX_MARGIN) * 2);
        y -= scroll;

        if(ss != null){
            for(int i = 0, size = ss.length; i < size; i++){
                drawShadowString(g, ss[i], x + EDGE_WIDTH + BOX_MARGIN, y + EDGE_WIDTH + BOX_MARGIN, true);
                y += CHAR_HEIGHT;
            }
        }

        g.setClip(clipX, clipY, clipW, clipH);
    }

    public static void drawShadowString(Graphics g, String s, int x, int y, boolean selected){
        drawShadowString(g, s, x, y, selected? 0xFFFF00: 0xFFFFFF);
    }

    public static void drawShadowString(Graphics g, String s, int x, int y, int color){
        drawShadowString(g, s, x, y, Graphics.LEFT | Graphics.TOP, color);
    }

    public static void drawShadowString(Graphics g, String s, int x, int y, int anchor, int color){
        g.setFont(font);

        if(((color & 0xFF) > 80) || (((color >> 8) & 0xFF) > 80) || (((color >> 16) & 0xFF) > 10)){
            g.setColor(0x000000);
            g.drawString(s, x + 1, y + 1, anchor);
        }

        g.setColor(color);
        g.drawString(s, x, y, anchor);
    }

    public static void drawString(Graphics g, String s, int x, int y, boolean selected){
        //#if font == native
        drawShadowString(g, s, x, y, selected);
        //#elif font == image
        //#     drawImageString(g,s,x,y,selected);
        //#endif
    }

    public static void drawTitle(Graphics g, String title){
        //drawBox(g, 0, 0, World.viewWidth, TITLE_HEIGHT);
        drawEdge(g, 0, 0, 0, World.viewWidth, TITLE_HEIGHT, true, EDGE_COLOR[3]);
        drawString(g, title, BOX_MARGIN + EDGE_WIDTH, BOX_MARGIN + EDGE_WIDTH, true);
    }

    public static void drawTitle(Graphics g, String title, String title2){
        drawTitle(g, title);

        if(title2 != null){
            int width = 0;
            //#if font == native
            width = font.stringWidth(title2);
            //#elif font == image
            //# width = iFont.stringWidth(title2);
            //#endif
            drawString(g, title2, World.viewWidth - width - BOX_MARGIN - EDGE_WIDTH, BOX_MARGIN + EDGE_WIDTH, true);
        }
    }

    public void drawButtons(Graphics g, byte type, boolean force){
        if(!force && waitType != WAIT_NONE)
            return;
        if(buttonImg == null){
            try{
                buttonImg = World.getImageSetFromLocal("btn");
            }catch(Exception e){
            }
            if(buttonImg == null){
                return;
            }
        }

        Command left = null;
        Command right = null;

        if((type & 0x01) != 0){
            drawButtons(g, type, 0, World.viewHeight - 9);

            //#if CommandEmu == true
            //# left = World.commandOK;
            //#endif
        }
        if((type & 0x02) != 0){
            drawButtons(g, type, World.viewWidth - 9, World.viewHeight - 9);

            //#if CommandEmu == true
            //# right = World.commandBack;
            //#endif
        }

        //#if CommandEmu == true
        //# if(left != null || right != null){
        //#     World.instance.addCommands(left, right);
        //# }
        //#endif
    }

    public static void drawButtons(Graphics g, byte frame, int x, int y){
        buttonImg = getBtnImg();
        buttonImg.drawFrame(g, frame, x, y, Graphics.LEFT | Graphics.TOP);
    }

    public static void drawNumber(Graphics g, int num, int x, int y){
        String n = String.valueOf(num);
        String drawChar = null;
        for(int i = 0; i < n.length(); i++){
            drawChar = n.substring(i, i + 1);
            World.charImageSet.drawFrame(g, Integer.parseInt(drawChar), x + i * World.charImageSet.getWidth(0), y, Graphics.TOP | Graphics.LEFT);
        }
    }

    public static ImageSet getBtnImg(){
        if(buttonImg == null){
            try{
                buttonImg = World.getImageSetFromLocal("btn");
            }catch(Exception e){
            }
        }
        return buttonImg;
    }

    public static void drawTaskUIPopMenu(Graphics g){
        int maxWidth = CHAR_WIDTH * 4;

        for(int i = 0; i < taskUICommand.length; i++){
            int width = font.stringWidth(taskUICommand[i]);

            if(width > maxWidth){
                maxWidth = width;
            }
        }

        World.drawMenu(g, taskUICommand, -1, -1, taskUICommandCurrentSelect, 0);
    }

    public static ImageSet img = World.getImageSetFromLocal("edges");

    public static void drawEdge(Graphics g, int style, int x, int y, int width, int height, boolean drawBackGround, int bgColor){
        int clipX = g.getClipX();
        int clipY = g.getClipY();
        int clipW = g.getClipWidth();
        int clipH = g.getClipHeight();

        g.setClip(x, y, width, height);

        if(drawBackGround){
            g.setColor(bgColor);
            g.fillRect(x + 2, y + 2, width - 4, height - 4);
        }

        for(int i = 0; i < 3; i++){
            g.setColor(EDGE_COLOR[i]);
            g.drawLine(x + img.getWidth(0), y + i, x + width - img.getWidth(0), y + i);
            g.drawLine(x + i, y + img.getHeight(0), x + i, y + height - img.getHeight(0));
            g.drawLine(x + img.getWidth(0), y + height - i - 1, x + width - img.getWidth(0), y + height - i - 1);
            g.drawLine(x + width - i - 1, y + img.getHeight(0), x + width - i - 1, y + height - img.getHeight(0));
        }

        img.drawFrame(g, 0, x, y, Graphics.TOP | Graphics.LEFT);
        img.drawFrame(g, 1, x + width, y, Graphics.TOP | Graphics.RIGHT);
        img.drawFrame(g, 2, x, y + height, Graphics.BOTTOM | Graphics.LEFT);
        img.drawFrame(g, 3, x + width, y + height, Graphics.BOTTOM | Graphics.RIGHT);

        g.setClip(clipX, clipY, clipW, clipH);

    }

    private static ImageSet btnImgSet;

    private static final byte BUTTON_FRAME_TOPLEFT = 0;
    private static final byte BUTTON_FRAME_LEFT = 1;
    private static final byte BUTTON_FRAME_BOTTOMLEFT = 2;
    private static final byte BUTTON_FRAME_TOPRIGHT = 3;
    private static final byte BUTTON_FRAME_RIGHT = 4;
    private static final byte BUTTON_FRAME_BOTTOMRIGHT = 5;
    private static final byte BUTTON_FRAME_TOP = 6;
    private static final byte BUTTON_FRAME_BOTTOM = 7;
    private static final byte BUTTON_FRAME_BACKGROUND = 8;
    private static final byte BUTTON_FRAME_ARROW_LEFT = 9;
    private static final byte BUTTON_FRAME_ARROW_RIGHT = 10;

    private static final byte BUTTON_FRAME_TOPLEFT_HIGHTLIGHT = 11;
    private static final byte BUTTON_FRAME_LEFT_HIGHTLIGHT = 12;
    private static final byte BUTTON_FRAME_BOTTOMLEFT_HIGHTLIGHT = 13;
    private static final byte BUTTON_FRAME_TOPRIGHT_HIGHTLIGHT = 14;
    private static final byte BUTTON_FRAME_RIGHT_HIGHTLIGHT = 15;
    private static final byte BUTTON_FRAME_BOTTOMRIGHT_HIGHTLIGHT = 16;
    private static final byte BUTTON_FRAME_TOP_HIGHTLIGHT = 17;
    private static final byte BUTTON_FRAME_BOTTOM_HIGHTLIGHT = 18;
    private static final byte BUTTON_FRAME_BACKGROUND_HIGHTLIGHT = 19;
    private static final byte BUTTON_FRAME_ARROW_LEFT_HIGHTLIGHT = 20;
    private static final byte BUTTON_FRAME_ARROW_RIGHT_HIGHTLIGHT = 21;

    private static final byte[] BUTTON_FRAMES = {
                    0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
    };

    private static final byte[] BUTTON_FRAMES_HIGHTLIGHT = {
                    11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21
    };

    private static final int[] BUTTON_BACKGROUND_COLOR = {
                    0x7A5800, 0x968100
    };

    public static void drawButton(Graphics g, int x, int y, int width, int height, boolean select){
        if(btnImgSet == null)
            btnImgSet = World.getImageSetFromLocal("buttons");

        int clipX = g.getClipX();
        int clipY = g.getClipY();
        int clipW = g.getClipWidth();
        int clipH = g.getClipHeight();

        int x0 = x + 3;
        int y0 = y + 4;
        int w0 = width - 6;
        int h0 = height - 8;

        g.setClip(x0, y0, w0, h0);

        byte frames[] = select? BUTTON_FRAMES_HIGHTLIGHT: BUTTON_FRAMES;

        g.setColor(BUTTON_BACKGROUND_COLOR[select? 1: 0]);
        g.fillRect(x0, y0, w0, h0);

        g.setClip(x, y, width, height);

        btnImgSet.drawFrame(g, frames[BUTTON_FRAME_TOPLEFT], x, y, Graphics.TOP | Graphics.LEFT);
        btnImgSet.drawFrame(g, frames[BUTTON_FRAME_TOPRIGHT], x + width, y, Graphics.TOP | Graphics.RIGHT);
        btnImgSet.drawFrame(g, frames[BUTTON_FRAME_BOTTOMLEFT], x, y + height, Graphics.BOTTOM | Graphics.LEFT);
        btnImgSet.drawFrame(g, frames[BUTTON_FRAME_BOTTOMRIGHT], x + width, y + height, Graphics.BOTTOM | Graphics.RIGHT);

        g.setClip(x0, y, w0, height);
        for(int i = 0; i < w0; i += btnImgSet.getWidth(frames[BUTTON_FRAME_TOP])){
            btnImgSet.drawFrame(g, frames[BUTTON_FRAME_TOP], x0 + i, y, Graphics.TOP | Graphics.LEFT);
            btnImgSet.drawFrame(g, frames[BUTTON_FRAME_BOTTOM], x0 + i, y + height, Graphics.BOTTOM | Graphics.LEFT);
        }

        g.setClip(x, y0, width, h0);
        for(int i = 0; i < h0; i += btnImgSet.getHeight(frames[BUTTON_FRAME_TOP])){
            btnImgSet.drawFrame(g, frames[BUTTON_FRAME_LEFT], x, y0 + i, Graphics.TOP | Graphics.LEFT);
            btnImgSet.drawFrame(g, frames[BUTTON_FRAME_RIGHT], x + width, y0 + i, Graphics.TOP | Graphics.RIGHT);
        }

        g.setClip(x, y, width, height);

        btnImgSet.drawFrame(g, frames[BUTTON_FRAME_ARROW_LEFT], x + 2, y + (height - btnImgSet.getHeight(frames[BUTTON_FRAME_ARROW_LEFT])) / 2, Graphics.TOP | Graphics.LEFT);
        btnImgSet.drawFrame(g, frames[BUTTON_FRAME_ARROW_RIGHT], x + width - 2, y + (height - btnImgSet.getHeight(frames[BUTTON_FRAME_ARROW_RIGHT])) / 2, Graphics.TOP | Graphics.RIGHT);

        g.setClip(clipX, clipY, clipW, clipH);
    }

    private static Image alphaImage = null;

    private static Hashtable alphaImageMap = new Hashtable();

    public static void drawAlphaBox(Graphics g, int color, int x, int y, int width, int height){
    	fileAlphaBox(g, color, x, y, width, 1);
    	fileAlphaBox(g, color, x, y + height, width, 1);
    	fileAlphaBox(g, color, x, y, 1, height);
    	fileAlphaBox(g, color, x + width, y, 1, height);
    }
    
    public static void fileAlphaBox(Graphics g, int color, int x, int y, int width, int height){
        //#if (JBlend == true) && (Directory != MT-General) && (TouchScreen != true)
        // Moto手机不支持半透明，采用实心块
        //# g.setColor(color);
        //# g.fillRect(x, y, width, height);
        //#elif (polish.identifier == Nokia/Series40Midp2)
        //# DirectGraphics dg = DirectUtils.getDirectGraphics(g);
        //# dg.setARGBColor(color);
        //# g.fillRect(x, y, width, height);
        //#elif AlphaMethod == image

        //# alphaImage = (Image)alphaImageMap.get(new Integer(color));
        //# if(alphaImage == null){
        //#     try{
        //#         alphaImage = Image.createImage("/" + color);
        //#         alphaImageMap.put(new Integer(color), alphaImage);
        //#     }catch(IOException e){
        //#     }
        //# }

        //# if(alphaImage == null){
        //#     g.setColor(color);
        //#     g.fillRect(x, y, width, height);
        //# }else{
        //#     g.setClip(x, y, width, height);
        //#     for(int j = 0; j < height; j += alphaImage.getHeight()){
        //#         g.drawImage(alphaImage, x, y + j, Graphics.TOP | Graphics.LEFT);
        //#     }
        //#     g.setClip(0, 0, width, height);
        //# }
        //#elif AlphaMethod == drawRGB

        //# int[] tmp = new int[width * height];
        //# for(int i = 0; i < tmp.length; i++){
        //#     tmp[i] = color;
        //# }
        //# g.drawRGB(tmp, 0, width, x, y, width, height, true);
        //#elif polish.midp2
        alphaImage = (Image)alphaImageMap.get(new Integer(color));

        if(alphaImage == null){
            int[] tmp = new int[width];
            for(int i = 0; i < width; i++){
                tmp[i] = color;
            }
            alphaImage = Image.createRGBImage(tmp, width, 1, true);
            alphaImageMap.put(new Integer(color), alphaImage);
        }

        g.setClip(x, y, width, height);

        for(int j = 0; j < height; j++){
            g.drawImage(alphaImage, x, y + j, Graphics.TOP | Graphics.LEFT);
        }
        g.setClip(0, 0, width, height);
        //#elif polish.api.nokia-ui
        //#        short[] tmp = new short[5*width];
        //#        for (int i = 0; i < 5*width; i++) {
        //#            byte ca, cr, cg, cb;

        //#            ca = (byte)((color >> 24) & 0xFF);
        //#            cr = (byte)((color >> 16) & 0xFF);
        //#            cg = (byte)((color >> 8) & 0xFF);
        //#            cb = (byte)(color & 0xFF);

        //#            ca = (byte)((ca >> 4) & 0xF);
        //#            cr = (byte)((cr >> 4) & 0xF);
        //#            cg = (byte)((cg >> 4) & 0xF);
        //#            cb = (byte)((cb >> 4) & 0xF);

        //#            tmp[i] = (short)(((ca << 12) | (cr << 8) | (cg << 4) | cb) & 0xFFFF);
        //#        }
        //#        DirectGraphics dg = DirectUtils.getDirectGraphics(g);
        //#        int yy = y;
        //#        while(yy<y+height){
        //#            int h = y+height-yy;
        //#            if(h>5) h = 5;
        //#            dg.drawPixels(tmp,true,0,width,x,yy,width,h,0,DirectGraphics.TYPE_USHORT_4444_ARGB);
        //#           yy += 5;
        //#        }
        //#endif
    }

    public static final byte BTNTYPE_NONE = 0;
    public static final byte BTNTYPE_SOFT_LEFT = 1;
    public static final byte BTNTYPE_SOFT_RIGHT = 2;
    public static final byte BTNTYPE_FIRE = 4;

    public static final byte BTNTYPE_SOFT_LEFTANDRIGHT = BTNTYPE_SOFT_LEFT | BTNTYPE_SOFT_RIGHT;

    public static void drawBackGround(Graphics g, int x, int y, int width, int height, boolean drawLine){
        g.setColor(0x000000);
        g.drawRect(x, y, width, height);
        g.setColor(0xffffe1/*0xffff8b*/);
        g.setColor(0xffffb6/*0xffff8b*/);
        g.fillRect(x + 1, y + 1, width - 1, height - 1);

        if(drawLine){
            g.setColor(0x888888);
            g.drawLine(x + 1, y + height - getBtnImg().getHeight(0) - 3 -CHAR_HEIGHT/2, x + width - 1, y + height - getBtnImg().getHeight(0) - 3-CHAR_HEIGHT/2);
        }
    }

    public static void drawMsgTip(Graphics g, int x, int y, Object[] msg, Hashtable color, ImageSet icon, int btnType){

        int maxWidth = 0;

        for(int i = 0; i < msg.length; i++){

            if(msg[i] instanceof String){
                if(font.stringWidth((String)msg[i]) > maxWidth){
                    maxWidth = font.stringWidth((String)msg[i]);
                }
            }else if(msg[i] instanceof GameItem){
                GameItem item = (GameItem)msg[i];
                int len = font.stringWidth(item.getName(true, World.viewWidth));
                if(len > maxWidth){
                    maxWidth = len;
                }
            }else if(msg[i] instanceof Object[]){
                Object[] obj = (Object[])msg[i];
                int len = font.stringWidth((String)obj[2]);
                int line = ((Integer)obj[0]).intValue();

                for(int j = 0; j < msg.length; j++){
                    if(msg[j] instanceof Object[]){
                        Object[] other = (Object[])msg[j];
                        int oline = ((Integer)other[0]).intValue();
                        if(oline == line && !other[2].equals(obj[2])){
                            len += font.stringWidth((String)other[2]);
                        }
                    }
                }

                if(len > maxWidth){
                    maxWidth = len;
                }
            }
        }

        if(maxWidth > World.viewWidth - 10){
            maxWidth = World.viewWidth - 10;
        }

        int maxLen = maxWidth / GameState.CHAR_WIDTH + 1;

        int width = maxLen * GameState.CHAR_WIDTH + BOX_MARGIN * 2 + EDGE_WIDTH * 2;

        int lines = 0;
        int lastOline = -1;

        for(int j = 0; j < msg.length; j++){
            if(msg[j] instanceof Object[]){
                Object[] other = (Object[])msg[j];
                int oline = ((Integer)other[0]).intValue();
                if(lastOline != oline){
                    lines++;
                    lastOline = oline;
                }
            }else{
                lines++;
            }
        }

        int height = lines * GameState.CHAR_HEIGHT + BOX_MARGIN * 2 + 2 +CHAR_HEIGHT/2;

        if(btnType != BTNTYPE_NONE){
            height += getBtnImg().getHeight(0);
        }

        if(icon != null){
            width += icon.getWidth(0) + 4;
            if(icon.getHeight(0) + BOX_MARGIN * 2 + getBtnImg().getHeight(0) + 2 > height){
                while(icon.getHeight(0) + BOX_MARGIN * 2 + getBtnImg().getHeight(0) + 2 > height){
                    height += GameState.CHAR_HEIGHT;
                }
            }
        }

        if(x == -1)
            x = (World.viewWidth - width) / 2;
        if(y == -1)
            y = (World.viewHeight - height) / 2;

        if(x + width > World.viewWidth){
            x = World.viewWidth - width;
        }
        if(y + height > World.viewHeight){
            y = World.viewHeight - height;
        }

        if(x + width >= World.viewWidth){
            width = World.viewWidth - x - 1;
        }

        if(x < 0){
            x = 0;
        }

        if(y < 0){
            y = 0;
        }

        if(btnType != BTNTYPE_NONE){
            drawBackGround(g, x, y, width, height, true);
        }else{
            drawBackGround(g, x, y, width, height, false);
        }

        int lastLine = -1;
        int currLine = 0;

        int clr = 0x000000;

        for(int i = 0; i < msg.length; i++){
            if(color != null){
                Integer c = (Integer)color.get(new Integer(i));
                if(c == null){
                    clr = 0x000000;
                }else{
                    clr = c.intValue();
                }
            }else{
                clr = 0x000000;
            }
            g.setColor(clr);
            g.setFont(GameState.font);
            if(icon == null){
                if(msg[i] instanceof String){
                    if(/*(clr & 0x0000ff) <= 0xff && clr >= 0xffff00*/clr != 0){
                        World.draw3DString(g, (String)msg[i], x + 3, y + 1 + currLine * GameState.CHAR_HEIGHT, Graphics.TOP | Graphics.LEFT, clr);
                    }else{
                        g.drawString((String)msg[i], x + 3, y + 1 + currLine * GameState.CHAR_HEIGHT, Graphics.TOP | Graphics.LEFT);
                    }
                    //#if TouchScreen == true
                    String ts = null;
                   /* if(msg.length>=2){
                        if(i==msg.length -1 || i==msg.length-2){
                        	ts = (String)msg[i];
                        	//int index =  ts.indexOf('.');
                        	//System.out.println(index);
                        	int answer = -1;
                        	if(!(ts !=null && ts.equals(""))){
    	                        	answer = ts.charAt(0)-48;
    	                    }
                        	//int answer = (int)((String)msg[i]).charAt(0)-48;
                        	if(answer >= 1 && answer <= 9){
                        		StaticUtils.addButton(answer+11, x + 3, y + 1 + currLine * GameState.CHAR_HEIGHT, ((String)msg[i]).length()*GameState.CHAR_WIDTH, GameState.CHAR_HEIGHT);
                        	}
                        }
                    }else if(msg.length == 1){
                    	ts = (String)msg[0];
                    	//int index =  ts.indexOf('.');
                    	//System.out.println(index);
                    	
                    }*/
                    ts = (String)msg[i];
                    int answer = -1;
                	if(!(ts ==null ||  (ts.equals("") && ts.length() == 0 ))){
                        	answer = ts.charAt(0)-48;
                    }
                	//int answer = (int)((String)msg[i]).charAt(0)-48;
                	if(answer >= 1 && answer <= 9){
                		StaticUtils.addButton(answer+11, x + 3, y + 1 + currLine * GameState.CHAR_HEIGHT, width -CHAR_WIDTH/*((String)msg[i]).length()*GameState.CHAR_WIDTH*/, GameState.CHAR_HEIGHT);
                	}
                    //#endif
                    currLine++;
                }else if(msg[i] instanceof Object[]){
                    Object[] strObj = (Object[])msg[i];
                    int strLine = ((Integer)strObj[0]).intValue();

                    if(lastLine == -1){
                        lastLine = strLine;
                    }else if(strLine != lastLine){
                        currLine++;
                        lastLine = strLine;
                    }
                    World.drawFormatedString(g, strObj, font, x + 3, y + 1, currLine, clr);
                  //#if TouchScreen == true
                    Object[] temp = null;
                    temp= (Object[])msg[i];
/*                    if(msg.length>=2){
                    	temp= (Object[])msg[i];
                    	//System.out.println(ts);
                    	String ts=(String)temp[2];
                    	//System.out.println(ts);
                    	//int index = ts.indexOf('.');
                    	//if(ts !=null && ts.equals(""))
                    	//System.out.println(index);
                    	if(!(ts !=null && ts.equals(""))){
	                        	int answer = ts.charAt(0)-48;
	                        	//System.out.println(answer);
	                        	//int answer = ts.charAt(0)-48;
	                        	//int answer = (int)((String)msg[i]).charAt(0)-48;
	                        	if(answer >= 1 && answer <= 9){
	                        		StaticUtils.addButton(answer+11,  x + 3, y + 1 + currLine * GameState.CHAR_HEIGHT, ts.length()*GameState.CHAR_WIDTH, GameState.CHAR_HEIGHT);
	                        	}
                    	}
                    }else if(msg.length == 1){
                    	temp= (Object[])msg[0];
                   
                    }*/
                 	//System.out.println(ts);
                	String ts=(String)temp[2];
                	//System.out.println(ts);
                	//int index = ts.indexOf('.');
                	//if(ts !=null && ts.equals(""))
                	//System.out.println(index);
                	if(!(ts ==null || (ts.equals("") && ts.length() == 0 ))){
                        	int answer = ts.charAt(0)-48;
                        	//System.out.println(answer);
                        	//int answer = ts.charAt(0)-48;
                        	//int answer = (int)((String)msg[i]).charAt(0)-48;
                        	if(answer >= 1 && answer <= 9){
                        		StaticUtils.addButton(answer+11,  x + 3, y + 1 + currLine * GameState.CHAR_HEIGHT, width -CHAR_WIDTH/*ts.length()*GameState.CHAR_WIDTH*/, GameState.CHAR_HEIGHT);
                        	}
                	}
                    //#endif
                    if(i < msg.length - 1 && !(msg[i + 1] instanceof Object[])){
                        currLine++;
                    }

                }else if(msg[i] instanceof GameItem){
                    GameItem item = (GameItem)msg[i];
                    boolean sn = true;
                    if(item.count == 1){
                        sn = false;
                    }
                    item.drawName(g, x + 3, y + 1 + currLine * GameState.CHAR_HEIGHT, sn, false, width, false);
                    currLine++;
                }
            }else{
                if(msg[i] instanceof String){
                    g.drawString((String)msg[i], x + icon.getWidth(0) + 2, y + 1 + currLine * GameState.CHAR_HEIGHT, Graphics.TOP | Graphics.LEFT);
                    //#if TouchScreen == true
                    String ts = null;
                    ts = (String)msg[i];
                   /* if(msg.length>=2){
                        if(i==msg.length -1 || i==msg.length-2){
                        	ts = (String)msg[i];
                        	//int index =  ts.indexOf('.');
                        	//System.out.println(index);
                        	int answer = -1;
                        	if(!(ts !=null && ts.equals(""))){
    	                        	answer = ts.charAt(0)-48;
    	                    }
                        	//int answer = (int)((String)msg[i]).charAt(0)-48;
                        	if(answer >= 1 && answer <= 9){
                        		StaticUtils.addButton(answer+11, x + 3, y + 1 + currLine * GameState.CHAR_HEIGHT, ((String)msg[i]).length()*GameState.CHAR_WIDTH, GameState.CHAR_HEIGHT);
                        	}
                        }
                    }else if(msg.length == 1){
                    	ts = (String)msg[0];
                    	//int index =  ts.indexOf('.');
                    	//System.out.println(index);
                    
                    }*/
                	int answer = -1;
                	if(!(ts ==null || (ts.equals("") && ts.length() == 0 ))){
                        	answer = ts.charAt(0)-48;
                    }
                	//int answer = (int)((String)msg[i]).charAt(0)-48;
                	if(answer >= 1 && answer <= 9){
                		StaticUtils.addButton(answer+11, x + 3, y + 1 + currLine * GameState.CHAR_HEIGHT, width -CHAR_WIDTH/*((String)msg[i]).length()*GameState.CHAR_WIDTH*/, GameState.CHAR_HEIGHT);
                	}
                    //#endif
                    currLine++;
                }else if(msg[i] instanceof Object[]){
                    Object[] strObj = (Object[])msg[i];

                    int strLine = ((Integer)strObj[0]).intValue();

                    if(lastLine == -1){
                        lastLine = strLine;
                    }else if(strLine != lastLine){
                        currLine++;
                        lastLine = strLine;
                    }
                    World.drawFormatedString(g, strObj, font, x + icon.getWidth(0) + 2, y + 1, currLine, clr);
                    //#if TouchScreen == true
                    Object[] temp = null;
                    temp= (Object[])msg[0];
                   /* if(msg.length>=2){
                    	temp= (Object[])msg[i];
                    
                    }else if(msg.length == 1){
                    	temp= (Object[])msg[0];
                    	//System.out.println(ts);
                    	String ts=(String)temp[2];
                    	//System.out.println(ts);
                    	//int index = ts.indexOf('.');
                    	//System.out.println(index);
                    	if(!(ts !=null && ts.equals(""))){
	                        	int answer = ts.charAt(0)-48;
	                        	//System.out.println(answer);
	                        	//int answer = ts.charAt(0)-48;
	                        	//int answer = (int)((String)msg[i]).charAt(0)-48;
	                        	if(answer >= 1 && answer <= 9){
	                        		StaticUtils.addButton(answer+11,  x + 3, y + 1 + currLine * GameState.CHAR_HEIGHT, ts.length()*GameState.CHAR_WIDTH, GameState.CHAR_HEIGHT);
	                        	}
                    	}
                    }*/
                	//System.out.println(ts);
                	String ts=(String)temp[2];
                	//System.out.println(ts);
                	//int index = ts.indexOf('.');
                	//System.out.println(index);
                	if(!(ts ==null || (ts.equals("") && ts.length() == 0 ))){
                        	int answer = ts.charAt(0)-48;
                        	//System.out.println(answer);
                        	//int answer = ts.charAt(0)-48;
                        	//int answer = (int)((String)msg[i]).charAt(0)-48;
                        	if(answer >= 1 && answer <= 9){
                        		StaticUtils.addButton(answer+11,  x + 3, y + 1 + currLine * GameState.CHAR_HEIGHT, width -CHAR_WIDTH/*ts.length()*GameState.CHAR_WIDTH*/, GameState.CHAR_HEIGHT);
                        	}
                	}
                    //#endif
                    if(i < msg.length - 1 && !(msg[i + 1] instanceof Object[])){
                        currLine++;
                    }

                }else if(msg[i] instanceof GameItem){
                    GameItem item = (GameItem)msg[i];
                    boolean sn = true;
                    if(item.count == 1){
                        sn = false;
                    }
                    item.drawName(g, x + icon.getWidth(0) + 2, y + 1 + currLine * GameState.CHAR_HEIGHT, sn, false, width, false);
                    currLine++;
                }
            }
        }

        if(icon != null){
            icon.drawFrame(g, 0, x + 2, y + height / 2, Graphics.LEFT | Graphics.VCENTER);
        }

        Command left = null;
        Command right = null;

        int btnx = 0;
        if((btnType & BTNTYPE_SOFT_LEFT) != 0 || (btnType & BTNTYPE_FIRE) != 0){
            //#if JBlend == true
        	//# btnx = x + width - getBtnImg().getWidth(0) - 2 -CHAR_WIDTH/2*3;
            //#else
        	 btnx = x + 2;
            //#endif

            //drawButtons(g, (byte)0, btnx, y + height - getBtnImg().getHeight(0) - 1);
            int clrs = 0xffffff;
            int bgClr = 0x3d1b0b;
            World.draw3DString(g,"确定",btnx +CHAR_WIDTH, y + height - getBtnImg().getHeight(0) -CHAR_HEIGHT/2 , Graphics.TOP | Graphics.HCENTER, clrs, bgClr);
            //#if TouchScreen == true
           
            StaticUtils.addButton(World.SOFT_FIRST_PRESSED, btnx - 2,  y + height - getBtnImg().getHeight(0) -CHAR_HEIGHT/2, CHAR_WIDTH*2, CHAR_HEIGHT);

            //#endif
            
            //#if CommandEmu == true
            //# left = World.commandOK;
            //#endif
        }

        if((btnType & BTNTYPE_SOFT_RIGHT) != 0){
            //#if JBlend == true
        	//# btnx = x + 2 ;
            //#else
        	 btnx = x + width - getBtnImg().getWidth(0) - 2 -CHAR_WIDTH/2*3;
            //#endif
            //drawButtons(g, (byte)1, btnx, y + height - getBtnImg().getHeight(0) - 1);
            int clrs = 0xffffff;
            int bgClr = 0x3d1b0b;
            World.draw3DString(g,"返回",btnx+ CHAR_WIDTH, y + height - getBtnImg().getHeight(0) -CHAR_HEIGHT/2 , Graphics.TOP | Graphics.HCENTER, clrs, bgClr);
          //#if TouchScreen == true
            
            StaticUtils.addButton(World.SOFT_LAST_PRESSED, btnx -2, y + height - getBtnImg().getHeight(0) -CHAR_HEIGHT /2, CHAR_WIDTH*2, CHAR_HEIGHT);

            //#endif
            //#if CommandEmu == true
            //# right = World.commandBack;
            //#endif
        }

        //#if CommandEmu == true
        //# if(left != null || right != null){
        //#     World.instance.addCommands(left, right);
        //# }
        //#endif
    }
    /*---------- Tools Functions End----------*/


    /* 移动版本和PIP版本登录游戏平台代码 */

//#if Revision != QQ
//#if Revision == CMCC || (Revision == JIANGSUNCMCC) 
     public static final String cmccLoginURL = "http://gmp.i139.cn/bizcontrol/LoginOnlineGame?sender=202&cpId=C00005&cpServiceId=120120433000&fid=";
//#else
  //# public static final String cmccLoginURL = null;
//#endif
    public static String cmccLoginURL1 = null;
    // public static final String cmccSender = "202";
    // public static final String cmccCPID = "C00005";
    // public static final String cmccServiceID = "120120433000";
    // public static final String cmccChannel = "15001000";
    public static String cmccUserID = "";
    public static String cmccKey = "";
    public static long cmccLoginTime;
    public static CMCCPointCanvas cmccPoint = null;

    // 登录移动平台，取回用户ID和KEY。如果登录失败，返回false。一次启动客户端只需要登录一次平台。
    public static boolean cmccLogin() {
        if (cmccUserID.length() > 0 && System.currentTimeMillis() - cmccLoginTime < 10 * 60000L) {
            // 一次登录2小时内不需要重新登录
            return true;
        }
        HttpConnection httpConnection = null;
        try {
            // 建立连接
            String url = null;
            
            if (cmccLoginURL1 != null && cmccLoginURL1.trim().length() > 0) {
                url = cmccLoginURL1;
            } else {
                url = cmccLoginURL;
            }
            if (url == null) {
                return false;
            }
            //测试cmcc服务器地址
            //url = "http://218.206.80.185:7070/testipd/test/LoginOnlineGame.jsp?";
            
        	url += channelCode.substring(channelCode.length() - 4);
            httpConnection = UWAPSegment.getConnection(url, UWAPSegment.useProxyGlobal);
            httpConnection.setRequestMethod(HttpConnection.GET);

            int code = httpConnection.getResponseCode();

            // 处理返回结果
            if (code == 200) {
                DataInputStream in = httpConnection.openDataInputStream();
                byte[] data = World.getBytesFromInput(in);
                in.close();
                String s = new String(data);
                Vector lines = World.formatString(s, 10000, GameState.font);
                int hRet = 1;
                String uid = null;
                String key = null;
                for (int i = 0; i < lines.size(); i++) {
                	String line = (String)((Object[])lines.elementAt(i))[2];
                	if (line.startsWith("hRet=")) {
                		hRet = Integer.parseInt(line.substring(5).trim());
                	} else if (line.startsWith("userId=")) {
                		uid = line.substring(7).trim();
                	} else if (line.startsWith("key=")) {
                		key = line.substring(4).trim();
                	}
                }
                if (hRet == 0) {
                	cmccUserID = uid;
                	cmccKey = key;
                	cmccLoginTime = System.currentTimeMillis();
                	return true;
                }
            }
        } catch (Exception ex2) {
        	//#debug
        	ex2.printStackTrace();
        } finally {
            if (httpConnection != null)
                try {
                    httpConnection.close();
                } catch(IOException ex3) {
                }
        }
        return false;
    }
//#endif
    
    //#if Revision != QQ
    public static boolean phoneLoginTry = false;
    //#else
    //# public static boolean phoneLoginTry = true;
    //#endif
    public static String clientPhone = "";
    
    // 尝试获取手机号。
    public static void phoneLogin() {
        // 只尝试一次
        if (phoneLoginTry) {
            return;
        }
        phoneLoginTry = true;
        
        HttpConnection httpConnection = null;
        try {
            // 建立连接
            String postData = "<config><client type=\"J2ME\" version=\"-1\" platform=\"runtimeEnv\"/><servers version=\"106\"/><service-no version=\"49\"/><parameters version=\"59\"/><hints version=\"51\"/><client-config version=\"86\"/><http-applications version=\"87\"/></config>";
            String url = "http://nav.fetion.com.cn/nav/getsystemconfig.aspx";
            httpConnection = UWAPSegment.getConnection(url, UWAPSegment.useProxyGlobal);
            httpConnection.setRequestMethod(HttpConnection.POST);
            httpConnection.setRequestProperty("Content-Type", "text/xml");
            OutputStream os = httpConnection.openOutputStream();
            os.write(postData.getBytes());
            os.close();

            int code = httpConnection.getResponseCode();

            // 处理返回结果
            if (code >= 200 && code <= 299) {
                DataInputStream in = httpConnection.openDataInputStream();
                byte[] data = World.getBytesFromInput(in);
                in.close();
                clientPhone = new String(data, "UTF-8");
                int pos1 = clientPhone.indexOf("<config", 0);
                int pos2 = -1;
                if (pos1 != -1) {
                    pos2 = clientPhone.indexOf(">", pos1);
                }
                if (pos1 != -1 && pos2 != -1) {
                    clientPhone = clientPhone.substring(pos1, pos2 + 1);
                }
            }
        } catch (Exception ex2) {
            //#debug
            ex2.printStackTrace();
        } finally {
            if (httpConnection != null)
                try {
                    httpConnection.close();
                } catch(IOException ex3) {
                }
        }
    }
    
    /* QQ版本增加内容 */
//#if Revision == QQ
    //#if (Directory == SE-K300) || (Directory == SE-K500) || (Directory == SE-K700)
    // K300,K500,K700三款机型内存无法承载新功能
    //#else
    public static void enterShenZhouFu() {
    	Form form = new Form("神州行充值卡");
    	form.append(new TextField("充值卡卡号：", "", 20, 2));
    	form.append(new TextField("充值卡密码：", "", 20, 2));
    	form.append(new ChoiceGroup("充值卡金额：", 4, new String[] { "--请选择--", "30元", "50元", "100元" }, null));
    	form.addCommand(new Command("提交", 4, 4));
    	form.addCommand(new Command("返回游戏", 4, 5));
    	form.setCommandListener(new GameState(STATE_QQBILL));
    	World.RecordPreousDisplay(form);
    }
    
    private Displayable qqLastView;
    private String qqCardNO;
    private String qqCardPass;
    private String qqCardAmount;
    public static String qqOKMsg = "支付请求发送成功，请稍后查询该卡是否已经被扣费！如扣费成功，您购买的物品将很快增加到您的游戏帐户。";
    public boolean shenZhouFuCommandAction(Form form, Command cmd) {
    	String title = form.getTitle();
        if (title.equals("神州行充值卡")) {
        	if ("提交".equals(cmd.getLabel())) {
        		String cardno = ((TextField)form.get(0)).getString();
        		String cardpass = ((TextField)form.get(1)).getString();
        		int amountIndex = ((ChoiceGroup)form.get(2)).getSelectedIndex();
        		String amount = null;
        		if (amountIndex == 1) {
        		    amount = "30";
        		} else if (amountIndex == 2) {
        		    amount = "50";
        		} else if (amountIndex == 3) {
        		    amount = "100";
        		}
        		if ((cardno == null) || (cardno.length() <= 0)) {
        			showShenZhouFuError("请输入神州付卡号", form);
        			return true;
			    }
        		if ((cardno == null || cardno.length() < 10)) {
        			showShenZhouFuError("神州付卡号位数不正确，请重新输入", form);
        			return true;
			    }
        		if ((cardpass == null) || (cardpass.length() <= 0)) {
        			showShenZhouFuError("请输入神州付卡密码", form);
        			return true;
			    }
        		if ((cardpass == null || cardpass.length() < 10)) {
        			showShenZhouFuError("神州付卡密码位数不正确，请重新输入", form);
        			return true;
			    }
        		if (amountIndex < 1) {
        			showShenZhouFuError("请输入神州付卡金额", form);
        			return true;
        		}
        		form = new Form("短信提示");
        		form.append("正在提交服务器，请稍候...");
        		World.RecordPreousDisplay(form);
        		qqCardNO = cardno;
        		qqCardPass = cardpass;
        		qqCardAmount = amount;
        		new Thread(this).start();
        	} else if ("返回游戏".equals(cmd.getLabel())) {
        		World.RecordPreousDisplay(World.instance);
        	}
        	return true;
        } else if (title.equals("错误提示")) {
        	World.RecordPreousDisplay(qqLastView);
        	return true;
        } else if (title.equals("提交成功")) {
        	World.RecordPreousDisplay(World.instance);
        	return true;
        }
        return false;
    }
    
    public void showShenZhouFuError(String msg, Displayable ret) {
    	Form form = new Form("错误提示");
    	form.append(msg);
    	form.addCommand(new Command("返回", 4, 4));
    	qqLastView = ret;
    	form.setCommandListener(this);
    	World.RecordPreousDisplay(form);
    }
    
    public void runQQBill() throws Exception {
    	try {
    		Thread.sleep(2000);
    	} catch (Exception e) {
    	}
    	try {
	    	UWAPSegment seg = new UWAPSegment((byte)152);
	    	seg.writeString(qqCardNO);
	    	seg.writeString(qqCardPass);
	    	seg.writeString(qqCardAmount);
	    	seg.flush();
	    	sendRequest(seg);
    	} catch (Exception e) {
    	}
    	Form form = new Form("提交成功");
    	form.append(qqOKMsg);
    	form.addCommand(new Command("返回游戏", 4, 5));
    	form.setCommandListener(this);
    	World.RecordPreousDisplay(form);
    }
    //#endif
//#endif
}
