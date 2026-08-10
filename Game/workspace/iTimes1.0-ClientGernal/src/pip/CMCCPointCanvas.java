package pip;

import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.TextField;

//#if polish.midp2
import javax.microedition.lcdui.game.GameCanvas;
//#endif

import pip.io.UWAPSegment;

//#ifdef polish.api.nokia-ui
//# import com.nokia.mid.ui.*;
//#endif

public class CMCCPointCanvas 

//#if Revision == CMCC || (Revision == JIANGSUNCMCC) 
extends
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
              //   //#if CommandEmu == true
                ,CommandListener
              //  //#endif
//#endif
{
    
//#if Revision == CMCC || (Revision == JIANGSUNCMCC) 
	private int state;
	private Command cmd1, cmd2;
	
	private Vector items = new Vector();
	
	private String[] _menu;
	private int _menuIndex;
	
	private String _message1;
	private String _message2;
	private Vector _list = new Vector();
	private int listMarginLeft =  GameState.CHAR_WIDTH + GameState.SCREEN_MARGIN;
	private int listMarginRight =  GameState.CHAR_WIDTH + GameState.SCREEN_MARGIN;
	
	private int listIndex;
	private int listPage;
	String listStartDate;
	String listEndDate;
	int listPageSize;
	
	private int listScrollOffset;
    private int listScrollDir;
    private int listScrollMax;
    private boolean listNeedScroll;
	
	private int inputNumber;
    private int queryTime;
    private int queryType;
	private int helpIndex;
	private int helpMaxIndex;
	
	private boolean connectionExist;
	
	private long lastSyncTime;
	
	public static final String MENU_CHARE = "帐户充值";
	public static final String MENU_QUERY = "查询余额";
	public static final String MENU_HELP = "点数帮助";
	public static final String MENU_RECHARE = "继续充值";
	public static final String MENU_CONFIRM = "确定";
	public static final String MENU_POINT = "点数专区";
	public static final String MENU_BACK = "返回";
	public static final String MENU_DETAIL = "明细查询";
	public static final String MENU_CHARGE_HISTORY = "点数充值记录";
	public static final String MENU_BUY_HISTORY = "点数消费记录";
	public static final String MENU_NEXT_PAGE = "下一页";
	
	public static final int ITEM_TYPE_MASSAGE = 0;
	public static final int ITEM_TYPE_COMMAND = 1;
	public static final int ITEM_TYPE_INPUT = 3;
	public static final int ITEM_TYPE_LIST = 4;

	public static final int STATE_ERROR = -1;
	public static final int STATE_MAIN = 0;
	public static final int STATE_CHARE = 1;
	public static final int STATE_CHARE_OVER = 2;
	public static final int STATE_QUERY_OVER = 3;
	public static final int STATE_HELP = 4;
	public static final int STATE_CHARGE_HISTORY = 5;
	public static final int STATE_BUY_HISTORY = 6;
	public static final int STATE_DETAIL = 7;
	
	public static final int LAYOUT_LEFT = 0;
	public static final int LAYOUT_CENTER = 1;
	public static final int LAYOUT_RIGHT = 2;
	public static final int LAYOUT_SCROLL = 3;
	
	public static final byte CONN_CMCC_HISTORY = (byte)217;
	public static final byte CONN_CMCC_HISTORY_OK = (byte)217;
	
	private boolean running = false;
	private String[] Urls = null;
	
	public static final String helpString = "游戏点数：游戏点数是移动梦网游戏频道的为用户提供的一种游戏支付方式，用户可以使用账户中的点数来购买游戏或消费游戏道具。" +
	                                          "\n\n点数充值：每个用户在移动梦网游戏频道都有自己的账户，用户可以通过话费购买点数的方式给自己的账户充值，1元话费可购买100点点数，每月最多可以购买100元即10000点点数。" +
	                                          "\n\n<cff0000>点数账户：每个用户的手机号码在移动梦网游戏频道都有一个固定的点数账户，用户可以使用账户内的点数购买游戏和在不同的游戏中购买道具，点数账户并不是用户在某款游戏中的游戏账户。</c>";
	public CMCCPointCanvas() {
		//#ifdef GameCanvas
        super(false);
        setFullScreenMode(true);
        //#else
        //# super();
        //#endif
        //#if CommandEmu == true
        //# cmd1 = new Command("是", Command.OK, 1);
        //# cmd2 = new Command("否", Command.CANCEL, 1);
        //# addCommand(cmd1);
        //# addCommand(cmd2);
        //#endif
        state = STATE_MAIN;
        
        listPageSize = GameState.BBS_PAGE_COUNT;
        long curTime = System.currentTimeMillis();
        listStartDate = getDateString(curTime - (long)7 * 24 * 3600 * 1000);
        listEndDate = getDateString(curTime) ;
        
        String _message = null;
        
        if(GameState.connection == null){
            connectionExist = false;
            makeConnect();
        }else{
            connectionExist = true;
        }
        
        if(!GameState.cmccLogin()){
            _message = "登录平台失败。请您使用“移动梦网(CMWAP)”方式连接，设置说明请用浏览器登陆游戏专区查看";
            state = STATE_ERROR;
        }

        makeMenu();
        makeMessage(_message);
        createControl();
        
        lastSyncTime = System.currentTimeMillis();
	}
	
	private String getDateString(long timeMills){
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(new Date(timeMills));
        
	    int year = cal.get(Calendar.YEAR);
	    int month = cal.get(Calendar.MONTH) + 1;
	    //int day = cal.get(Calendar.DAY_OF_MONTH);
	    
	    String result = String.valueOf(year);
	    
	    if(month < 10){
	        result += "0";
	    }
	    
	    result += String.valueOf(month);
	    
//	    if(day < 10){
//            result += "0";
//        }
//        
//        result += String.valueOf(day);
        
	    return result;
	}
	
	public void cycle(){
	    switch(state){
	        case STATE_CHARGE_HISTORY:
	        case STATE_BUY_HISTORY:
	            if(listNeedScroll){
                    if(listScrollDir == 0){
                        listScrollOffset++;
        
                        if(listScrollOffset > listScrollMax){
                            listScrollOffset = listScrollMax;
                            listScrollDir = 1;
                        }
                    }else{
                        listScrollOffset--;
        
                        if(listScrollOffset < 0){
                            listScrollOffset = 0;
                            listScrollDir = 0;
                        }
                    }
                }
	            
	            repaint();
	            serviceRepaints();
	            
	            break;
	    }
	    
	    if(System.currentTimeMillis() - lastSyncTime > 120 * 1000){
	        World.requestTestNetSpeed();
	        lastSyncTime = System.currentTimeMillis();
	    }
	}
	
	public void makeConnect(){
        try{
            if(Urls == null){
                GameState.downloadServerList();
            }
            
            GameState.URLs = GameState.serverURLs[0];
            GameState.url = null;
            GameState.closeConnection();
            GameState.createConnection();
        }catch(Exception e){
            //#debug
            e.printStackTrace();
        }
	}
	
	public void cycleSegment(UWAPSegment segment){
	    String _message = null;
	    
	    switch(segment.type){
	        case GameState.CONN_ERROR:
	            segment.readByte();
	            _message = segment.readString();
	            
	            break;
	        case GameState.CMCC_CHARGE_OK:
	            _message = segment.readString();
	            
	            break;
	        case GameState.CONN_GENERIC_LIST:
	            segment.readShort();
	            _message = segment.readString();
	            segment.readByte();
	            int count = segment.readShort();
	            
	            for(int i = 0; i < count; i++){
	                int point = segment.readInt();
	                String listItem = segment.readString();
	                int color = segment.readInt();
	                
	                _list.addElement("<c" + Integer.toHexString(color) + ">" + (i+1) + "." + listItem + "</c>");
	            }
	            
	            break;
	    }
	    
	    if(_message != null){
	        rebuildUI(_message);
            running = false;
        }
	}
	
	public void run(){
	    String _message = null;
	    running = true;
	    
	    switch(state){
	        case STATE_QUERY_OVER:
	            try{
	                if(!GameState.cmccLogin()){
                        _message = "登录平台失败。请您使用“移动梦网(CMWAP)”方式连接，设置说明请用浏览器登陆游戏专区查看";
                        state = STATE_ERROR;
                        
                        break;
                    }
	                
                    _message = "正在提交请求";

                    World.sendRequest(GameState.CMCC_CHARGE, new Object[]{
                                    GameState.cmccUserID, GameState.cmccKey, new Integer(0)
                    }, false);
                }catch(Exception ex1){
                    //#debug
                    ex1.printStackTrace();
                    _message = "网络错误，稍候再试";
                }
                
                break;
	        case STATE_CHARE_OVER:
                try{
                    if(!GameState.cmccLogin()){
                        _message = "登录平台失败。请您使用“移动梦网(CMWAP)”方式连接，设置说明请用浏览器登陆游戏专区查看";
                        state = STATE_ERROR;
                        
                        break;
                    }
                    
                    _message = "正在提交请求";

                    World.sendRequest(GameState.CMCC_CHARGE, new Object[]{
                                    GameState.cmccUserID, GameState.cmccKey, new Integer(inputNumber)
                    }, false);
                }catch(Exception ex1){
                    //#debug
                    ex1.printStackTrace();
                    _message = "网络错误，稍候再试";
                }
                
                break;
	        case STATE_CHARGE_HISTORY:
	        case STATE_BUY_HISTORY:
	            try{
	                if(!GameState.cmccLogin()){
                        _message = "登录平台失败。请您使用“移动梦网(CMWAP)”方式连接，设置说明请用浏览器登陆游戏专区查看";
                        state = STATE_ERROR;
                        
                        break;
                    }
	                
                    _message = "正在提交请求";

                    int historyType = 1;
                    
                    if(state == STATE_CHARGE_HISTORY){
                        historyType = 2;
                    }
                    
                    World.sendRequest(CONN_CMCC_HISTORY, new Object[]{
                         GameState.cmccUserID, GameState.cmccKey, new Integer(historyType),  listStartDate, listEndDate, new Integer(listPage * listPageSize + 1),  new Integer(listPageSize)
                    , new Integer(queryTime), new Integer(queryType)}, false);
                }catch(Exception ex1){
                    //#debug
                    ex1.printStackTrace();
                    _message = "网络错误，稍候再试";
                }
                
                break;
	        case STATE_DETAIL:
	            try{
	                if(!GameState.cmccLogin()){
                        _message = "登录平台失败。请您使用“移动梦网(CMWAP)”方式连接，设置说明请用浏览器登陆游戏专区查看";
                        state = STATE_ERROR;
                        
                        break;
                    }
	                
                    _message = "正在提交请求";

                    World.sendRequest(GameState.CMCC_CHARGE, new Object[]{
                                    GameState.cmccUserID, GameState.cmccKey, new Integer(0)
                    }, false);
                }catch(Exception ex1){
                    //#debug
                    ex1.printStackTrace();
                    _message = "网络错误，稍候再试";
                }
                
                break;
	    }
	    
	    if(_message != null){
	        makeMessage(_message);
	        createControl();
	        
            if(!"正在提交请求".equals(_message)){
                running = false;
            }
	           
	        repaint();
	        serviceRepaints();
	    }
	}

	public void paint(Graphics g) {
	    try{
	    	//#if TouchScreen == true
	        StaticUtils.beginButtonSetting();
	        //#endif
    		g.setColor(0xFFFFFF);
    		g.setFont(GameState.font);
    		g.fillRect(0, 0, World.viewWidth, World.viewHeight);
    		g.setColor(0x0);
    
    		int x = 0;
    		int y = 0;
    		
    		g.setClip(0, 0, World.viewWidth, World.viewHeight - GameState.CHAR_HEIGHT);
    		
    		 int e680 = 0;//为e680专门设置变量
    		 //#if Directory == ClientTouch-E680
    		 //e680 = GameState.CHAR_HEIGHT;
    		 //#endif 
    		 for(int i = 0,k = 0; i < items.size(); i++){
        		Object[] tmp = (Object[])items.elementAt(i);
        		int type = ((Integer)tmp[0]).intValue();
        		int layout = LAYOUT_LEFT;
        		
        		switch(type){
        		    case ITEM_TYPE_MASSAGE:
        		        layout = LAYOUT_LEFT;
        		        
        		        break;
        		    case ITEM_TYPE_COMMAND:
        		        layout = LAYOUT_CENTER;
        		        
        		        break;
        		    case ITEM_TYPE_INPUT:
        		        layout = LAYOUT_CENTER;
        		        
        		        break;
        		    case ITEM_TYPE_LIST:
        		        layout = LAYOUT_SCROLL;
        		        
        		        break;
        		}
        		
                if(type == ITEM_TYPE_INPUT){
                    g.setColor(0x0);
                    g.drawRect(GameState.SCREEN_MARGIN * 2, y - 1, World.viewWidth - GameState.SCREEN_MARGIN * 4, GameState.CHAR_HEIGHT);
                }
              
                if(state == STATE_HELP){
                	//#if TouchScreen == true
                    if(type == ITEM_TYPE_COMMAND){
                 	   StaticUtils.addButton(2000+k, x, y + e680, World.viewWidth, GameState.CHAR_HEIGHT);
                 	   k++;
                    }
                    //#endif 
                    y = drawItem(g, (Vector)tmp[1], x, y, layout, helpIndex);
                }else{
                	//#if TouchScreen == true
                    if(type == ITEM_TYPE_COMMAND){
                 	   StaticUtils.addButton(2000+k, x, y + e680, World.viewWidth, GameState.CHAR_HEIGHT);
                  	   k++;
                    }
                    //#endif  
                    y = drawItem(g, (Vector)tmp[1], x, y, layout, 0);
                }
    		}
    		
    		drawBackCommand(g);
    		//drawOkCommand(g);//画确认命令
    		
    		//#if TouchScreen == true
            if(state == STATE_CHARE){
    		    g.setClip(0, 0, World.viewWidth, World.viewHeight);
    		    g.setColor(0x0);
    		    
    		    x = GameState.SCREEN_MARGIN * 2;
    		    y = World.viewHeight - GameState.CHAR_HEIGHT * 5;
    		    int w = World.viewWidth - GameState.SCREEN_MARGIN * 4;
    		    int h = GameState.CHAR_HEIGHT * 2;
    		    
    		    for(int i = 0; i < 10; i++){
    		        int bw = w / 5;
    		        int bh = h / 2;
    		        
    		        int bx = x + (i % 5) * bw;
    		        int by = y + (i / 5 + 1) * bh;
    		        g.drawRect(bx, by , bw, bh);
    		        
    		        g.drawString("" + i, bx + bw /2, by, Graphics.TOP | Graphics.LEFT);
    		        StaticUtils.addButton(4000+i, bx, by, bw, bh);
    		    }
            }
    		
            StaticUtils.endButtonSetting();
            //#endif

	    }catch(Throwable e){
	        //#debug
	        e.printStackTrace();
	    }
	}
	
	private void initList(){
	    _list.removeAllElements();
	    listIndex = 0;
	}
	
	private void addInputNumber(){
	    String msg = "";
	    
	    if(inputNumber >= 0){
	        msg += Integer.toString(inputNumber);
	    }
	    
	    Vector vec = World.formatString(msg, World.viewWidth, GameState.font);
        Object[] result = new Object[2];
        
        result[0] = new Integer(ITEM_TYPE_INPUT);
        result[1] = vec;
        
        items.addElement(result);
	}
	
	private void drawBackCommand(Graphics g){
	    g.setClip(0, World.viewHeight - GameState.CHAR_HEIGHT, World.viewWidth, World.viewHeight - GameState.CHAR_HEIGHT);
	    Vector vec = World.formatString("<c0000ff>" + MENU_BACK + "</c>", World.viewWidth, GameState.font);
	    
	    //#if JBlend == true
	    //# drawItem(g, vec, 0, World.viewHeight - GameState.CHAR_HEIGHT, LAYOUT_LEFT, 0);
        //#if TouchScreen == true
        //StaticUtils.addButton(3000, World.viewWidth - GameState.CHAR_WIDTH *2 , World.viewHeight - GameState.CHAR_HEIGHT, GameState.CHAR_WIDTH *2, GameState.CHAR_HEIGHT);
	    StaticUtils.addButton(3001, 0, World.viewHeight - GameState.CHAR_HEIGHT, GameState.CHAR_WIDTH *2, GameState.CHAR_HEIGHT);
        //#endif
	    //#else
	    drawItem(g, vec, 0, World.viewHeight - GameState.CHAR_HEIGHT, LAYOUT_RIGHT, 0);
        //#if TouchScreen == true
        StaticUtils.addButton(3001, World.viewWidth - GameState.CHAR_WIDTH *2 , World.viewHeight - GameState.CHAR_HEIGHT, GameState.CHAR_WIDTH *2, GameState.CHAR_HEIGHT);
        //#endif
	    //#endif
	}
//	private void drawOkCommand(Graphics g){
//	    g.setClip(0, World.viewHeight - GameState.CHAR_HEIGHT, World.viewWidth, World.viewHeight - GameState.CHAR_HEIGHT);
//	    Vector vec = World.formatString("<c0000ff>" + MENU_CONFIRM + "</c>", World.viewWidth, GameState.font);
//	    
//	    //#if JBlend == true
//	    //# drawItem(g, vec, 0, World.viewHeight - GameState.CHAR_HEIGHT, LAYOUT_RIGHT, 0);
//	    //#if TouchScreen == true
//        StaticUtils.addButton(3001, 0, World.viewHeight - GameState.CHAR_HEIGHT, GameState.CHAR_WIDTH *2, GameState.CHAR_HEIGHT);
//        //#endif
//	    //#else
//	    drawItem(g, vec, 0, World.viewHeight - GameState.CHAR_HEIGHT, LAYOUT_LEFT, 0);
//	    //#if TouchScreen == true
//        StaticUtils.addButton(3000, 0, World.viewHeight - GameState.CHAR_HEIGHT, GameState.CHAR_WIDTH *2, GameState.CHAR_HEIGHT);
//        //#endif
//	    //#endif
//	}
	
	private int addMessage(String msg){
	    Vector vec = World.formatString(msg, World.viewWidth, GameState.font);
	    Object[] result = new Object[2];
        
        result[0] = new Integer(ITEM_TYPE_MASSAGE);
        result[1] = vec;
        
        items.addElement(result);
        
        return vec.size();
	}
	
	private int addList(Vector list){
	    Vector vec = new Vector();

	    for(int i = 0; i < list.size(); i++){
	        Object[] objs = (Object[])World.formatString((String)list.elementAt(i), Integer.MAX_VALUE, GameState.font).firstElement();
	        objs[0] = new Integer(i);
	        vec.addElement(objs);
	    }
	    
	    Object[] result = new Object[2];
	    
	    result[0] = new Integer(ITEM_TYPE_LIST);
	    result[1] = vec;
	    
	    items.addElement(result);
	    
	    return vec.size();
	}
	
	private void addCommand(String command, int color, boolean current){
	    String strColor = Integer.toHexString(color);
	    String msg = "<c" + strColor + ">";
	    
	    if(current){
	        msg += "【" + command + "】";
	    }else{
	        msg += command;
	    }
	    
	    msg += "</c>";
        Vector vec = World.formatString(msg, World.viewWidth, GameState.font);

        Object[] result = new Object[2];
        
        result[0] = new Integer(ITEM_TYPE_COMMAND);
        result[1] = vec;
        
        items.addElement(result);
	}

	private synchronized void makeMessage(String msg){
	    switch(state){
	        case STATE_ERROR:
	              _message1 = msg;
	              _message2 = null;
	              break;
            case STATE_MAIN:
                _message1 = null;
                _message2 = null;
                
                break;
            case STATE_CHARE:
                _message1 = "请输入“1-100元”任意整数金额";
                _message2 = "[提示：1元钱可充值100点点数，<cff0000>每月充值上限为10000点</c>]";
                
                break;
            case STATE_CHARE_OVER:
            case STATE_QUERY_OVER:
            case STATE_DETAIL:
                if(msg == null){
                    _message1 = "请稍候...";
                }else{
                    _message1 = msg;
                }
                
                _message2 = msg;
                
                break;
            case STATE_HELP:
                _message1 = helpString;
                _message2 = null;
                
                break;
            case STATE_CHARGE_HISTORY:
            case STATE_BUY_HISTORY:
                if(msg == null){
                    _message1 = "请稍候...";
                }else{
                    _message1 = msg;
                }
                
                break;
        }
	}
	
	private synchronized void makeMenu(){
	    switch(state){
	        case STATE_ERROR:
	            _menu = new String[0];
	            
	            break;
            case STATE_MAIN:
                _menu = new String[]{
                                MENU_CHARE,
                                MENU_QUERY,
                                MENU_DETAIL,
                                MENU_HELP
                };
                
                break;
            case STATE_CHARE:
                _menu = new String[]{
                                MENU_CONFIRM
                };
                
                break;
            case STATE_CHARE_OVER:
                _menu = new String[]{
                                MENU_RECHARE,
                                MENU_QUERY,
                                MENU_DETAIL,
                                MENU_POINT
                };
                
                break;
            case STATE_QUERY_OVER:
                _menu = new String[]{
                                MENU_CHARE,
                                MENU_DETAIL,
                                MENU_POINT
                };
                
                break;
            case STATE_HELP:
                _menu = new String[0];
                
                break;
            case STATE_CHARGE_HISTORY:
            case STATE_BUY_HISTORY:
                if(_list.size() < GameState.BBS_PAGE_COUNT){
                    _menu = new String[0];
                }else{
                    _menu = new String[]{
                                    MENU_NEXT_PAGE
                    };
                }
                
                break;
            case STATE_DETAIL:
                _menu = new String[]{
                                MENU_CHARGE_HISTORY,
                                MENU_BUY_HISTORY
                };
                
                break;
        }
	    
	    _menuIndex = 0;
	}
	
	private void createMenu(){
	    for(int i = 0; i < _menu.length; i++){
            boolean curr = false;
            
            if(i == _menuIndex){
                curr = true;
            }
            
            addCommand(_menu[i], 0x0000FF, curr);
        }
	}
	
	private synchronized void createControl(){
	    items.removeAllElements();
	    
	    switch(state){
	        case STATE_ERROR:
	        case STATE_MAIN:
	        case STATE_CHARE_OVER:
	        case STATE_QUERY_OVER:
	        case STATE_DETAIL:
	            if(_message1 != null){
                    addMessage(_message1);
                }
                
                createMenu();
                
                break;
	        case STATE_HELP:
	            if(_message1 != null){
	                helpMaxIndex = addMessage(_message1);
	            }
	            
	            createMenu();
	            
	            break;
	        case STATE_CHARE:
                if(_message1 != null){
                    addMessage(_message1);
                }
                
                addInputNumber();
                createMenu();
                
                if(_message2 != null){
                    addMessage(_message2);
                }
	            
	            break;
	        case STATE_CHARGE_HISTORY:
	        case STATE_BUY_HISTORY:
	            if(_message1 != null){
	                addMessage(_message1);
	            }
	            
	            addList(_list);
	            
	            createMenu();
	            
	            break;
	    }
	}
	
	private int drawItem(Graphics g, Vector item, int x, int y, int layout, int offset){
	    int lastLine = -1;
        int currLine = 0;
        int clr = 0x0;
        int rx = x;
        int ry = y;
        
        for(int i = 0; i < item.size(); i++){
            if(i < offset){
                continue;
            }
            
            Object msg = item.elementAt(i);
            
            Object[] strObj = (Object[])msg;
            int strLine = ((Integer)strObj[0]).intValue();

            if(lastLine == -1){
                lastLine = strLine;
            }else if(strLine != lastLine){
                currLine++;
                lastLine = strLine;
            }
            
            String str = (String)strObj[2];
            
            switch(layout){
                case LAYOUT_LEFT:
                    World.drawFormatedString(g, strObj, GameState.font, rx, ry, currLine, clr);
                    break;
                case LAYOUT_SCROLL:
                    if(i == listIndex){
                        g.setColor(clr);
                        g.drawString("【", GameState.SCREEN_MARGIN, ry + currLine * GameState.CHAR_HEIGHT, Graphics.TOP | Graphics.LEFT);
                        g.drawString("】", World.viewWidth - GameState.SCREEN_MARGIN, ry + currLine * GameState.CHAR_HEIGHT, Graphics.TOP | Graphics.RIGHT);
                    }
                    
                    g.setClip(listMarginLeft, ry + currLine * GameState.CHAR_HEIGHT, World.viewWidth - (listMarginLeft + listMarginRight), GameState.CHAR_HEIGHT);
                    
                    if(i == listIndex){
                        World.drawFormatedString(g, strObj, GameState.font, listMarginLeft - listScrollOffset, ry, currLine, clr);
                    }else{
                        World.drawFormatedString(g, strObj, GameState.font, listMarginLeft, ry, currLine, clr);
                    }
                    //#if TouchScreen == true
                 
                 	StaticUtils.addButton(2000+i, GameState.SCREEN_MARGIN, ry + currLine * GameState.CHAR_HEIGHT, GameState.CHAR_WIDTH * 15, GameState.CHAR_HEIGHT);

                    //#endif 
                    g.setClip(0, 0, World.viewWidth, World.viewHeight);
                    
                    break;
                case LAYOUT_CENTER:
                    rx = (World.viewWidth - str.length() * GameState.CHAR_WIDTH) / 2;
                    World.drawFormatedString(g, strObj, GameState.font, rx, ry, currLine, clr);
                    break;
                case LAYOUT_RIGHT:
                    rx = World.viewWidth - str.length() * GameState.CHAR_WIDTH;
                    World.drawFormatedString(g, strObj, GameState.font, rx, ry, currLine, clr);
                    
                    break;
            }
        }
        
        return y + (currLine + 1) * GameState.CHAR_HEIGHT;
	}

	protected void keyPressed(int keyCode) {
	    int id = World.keyToGame(keyCode);
	    int id1 = World.keyToNum(keyCode);
	    
	    if(id < 0){
	        id = id1;
	    }else{
	        if(state == STATE_CHARE && id1 >= World.KEY_NUM0_PRESSED && id1 <= World.KEY_NUM9_PRESSED){
	            id = id1;
	        }
	    }
	    
	    handkey(id);
	}
	
	private void handkey(int code){
	    switch(code){
            case World.SOFT_FIRST_PRESSED:
            case World.FIRE_PRESSED:
                confirm();
                
                break;
            case World.SOFT_LAST_PRESSED:
                cancel();
                
                break;
            case World.UP_PRESSED:
            case World.DOWN_PRESSED:
                move(code);
                
                break;
            case World.KEY_NUM0_PRESSED:
            case World.KEY_NUM1_PRESSED:
            case World.KEY_NUM2_PRESSED:
            case World.KEY_NUM3_PRESSED:
            case World.KEY_NUM4_PRESSED:
            case World.KEY_NUM5_PRESSED:
            case World.KEY_NUM6_PRESSED:
            case World.KEY_NUM7_PRESSED:
            case World.KEY_NUM8_PRESSED:
            case World.KEY_NUM9_PRESSED:
                input(code);
                
                break;
        }
	}
	
	private void move(int key){
	    switch(state){
	        case STATE_MAIN:
            case STATE_CHARE:
            case STATE_CHARE_OVER:
            case STATE_QUERY_OVER:
            case STATE_DETAIL:
                if(key == World.UP_PRESSED){
                    _menuIndex--;
                    
                    if(_menuIndex < 0){
                        _menuIndex = _menu.length - 1;
                    }
                }else{
                    _menuIndex++;
                    
                    if(_menuIndex >= _menu.length){
                        _menuIndex = 0;
                    }
                }
                
                createControl();
                repaint();
                serviceRepaints();
                
                break;
            case STATE_HELP:
                if(key == World.UP_PRESSED){
                    helpIndex--;
                    
                    if(helpIndex < 0){
                        helpIndex = 0;
                    }
                }else{
                    helpIndex++;
                    int screenLines = (World.viewHeight - GameState.CHAR_HEIGHT) / GameState.CHAR_HEIGHT;
                    if(helpIndex >= helpMaxIndex - screenLines){
                        helpIndex = helpMaxIndex - screenLines;
                    }
                }
                
                createControl();
                repaint();
                serviceRepaints();
                
                break;
            case STATE_CHARGE_HISTORY:
            case STATE_BUY_HISTORY:
                if(key == World.UP_PRESSED){
                    listIndex--;
                    
                    if(listIndex < 0){
                        listIndex = 0;
                    }
                }else{
                    listIndex++;

                    if(listIndex >= _list.size() - 1){
                        listIndex = _list.size() - 1;
                    }
                }
                
                if(_list.size() > 0 && GameState.font.stringWidth((String)_list.elementAt(listIndex)) > World.viewWidth - (listMarginLeft + listMarginRight)){
                    listNeedScroll = true;
                    listScrollDir = 0;
                    listScrollMax = GameState.font.stringWidth((String)_list.elementAt(listIndex)) - (World.viewWidth - (listMarginLeft + listMarginRight));
                    listScrollOffset = 0;
                }else{
                    listNeedScroll = false;
                    listScrollDir = 0;
                    listScrollMax = 0;
                    listScrollOffset = 0;
                }
                
                break;
	            
	    }
	}
	
	private void input(int key){
	    if(state == STATE_CHARE){
	        int num = key - World.KEY_NUM0_PRESSED;
	        
	        if(inputNumber < 0){
	            inputNumber = 0;
	        }
	        
	        inputNumber = inputNumber * 10 + num;
	        
	        if(inputNumber > 100){
	            inputNumber = inputNumber % 10;
	        }
	        
	        if(inputNumber == 0){
	            inputNumber = 1;
	        }
	        
	        createControl();
	        repaint();
	        serviceRepaints();
	    }
	}
	    
	private int handleMenu(String menu){
	    int result = state;
	    
	    if(menu.equals(MENU_CHARE)){
	        inputNumber = 1;
	        result = STATE_CHARE;
	    }else if(menu.equals(MENU_QUERY)){
	        result = STATE_QUERY_OVER;
	    }else if(menu.equals(MENU_HELP)){
	        helpIndex = 0;
	        helpMaxIndex = 0;
	        result = STATE_HELP;
	    }else if(menu.equals(MENU_RECHARE)){
	        result = STATE_CHARE;
	    }else if(menu.equals(MENU_CONFIRM)){
	        result = STATE_CHARE_OVER;
        }else if(menu.equals(MENU_POINT)){
            result = STATE_MAIN;
        }else if(menu.equals(MENU_CHARGE_HISTORY)){
            initList();
            listPage = 0;
            result = STATE_CHARGE_HISTORY;
            
            showDateForm("点数充值历史记录", (short) 1);
        }else if(menu.equals(MENU_BUY_HISTORY)){
            initList();
            listPage = 0;
            result = STATE_BUY_HISTORY;
            
            showDateForm("点数消费历史记录", (short) 2);
        }else if(menu.equals(MENU_NEXT_PAGE)){
            initList();
            listPage++;
            
            new Thread(this).start();
        }else if(menu.equals(MENU_DETAIL)){
            result = STATE_DETAIL;
        }
	    
	    return result;
	}
	
	private void confirm(){
	    if(!running && _menuIndex < _menu.length){
    	    state = handleMenu(_menu[_menuIndex]);
    	    
    	    switch(state){
    	        case STATE_CHARE_OVER:
    	        case STATE_QUERY_OVER:
    	        case STATE_DETAIL:
    	            new Thread(this).start();
                    
                    break;
    	    }
    	    
    	   rebuildUI(null);
	    }
	}

	private void cancel() {
	    if(!running){
    	    switch(state){
    	        case STATE_ERROR:
    	        case STATE_MAIN:
    	            GameState.cmccPoint = null;
    	            
    	            if(!connectionExist){
    	                GameState.closeConnection();
    	            }
    	            
    	            World.RecordPreousDisplay(World.instance);
    	            
    	            break;
    	        case STATE_CHARE:
    	        case STATE_CHARE_OVER:
    	        case STATE_QUERY_OVER:
    	        case STATE_HELP:
    	        case STATE_DETAIL:
    	            state = STATE_MAIN;
    	            rebuildUI(null);
                    
    	            break;
    	        case STATE_CHARGE_HISTORY:
                case STATE_BUY_HISTORY:
                    state = STATE_DETAIL;
                    rebuildUI(_message2);

                    break;
    	    }
	    }
	}
	
	private void rebuildUI(String message){
	    makeMenu();
        makeMessage(message);
        createControl();
        repaint();
        serviceRepaints();
	}
	
	private void showDateForm(String title ,short type){
		// 1 为充值，2为消费 3,为日期
	    Form _form = new Form(title);
	    if(type < 3){
	    //_form.append(new String("查询时间"));
	    //#if MIDP-VERSION == MIDP-2.0
	    ChoiceGroup choiceGroup  = new ChoiceGroup("查询时间",ChoiceGroup.POPUP);
	    //#else
	    //# ChoiceGroup choiceGroup  = new ChoiceGroup("查询时间",Choice.EXCLUSIVE);
	    //#endif
	    choiceGroup.append("今天", null);
	    choiceGroup.append("指定月", null);
	    choiceGroup.append("十天内", null);
	    //#if MIDP-VERSION == MIDP-1.0
	    choiceGroup.setSelectedIndex(0, true);
	    //#endif
	    _form.append(choiceGroup);

	    //_form.append(new String("查询类型"));
	    //#if MIDP-VERSION == MIDP-2.0
	    ChoiceGroup choiceTypeGroup  = new ChoiceGroup("查询类型",ChoiceGroup.POPUP);
	    //#else
	    //# ChoiceGroup choiceTypeGroup  = new ChoiceGroup("查询类型",Choice.EXCLUSIVE);
	    //#endif
	    if(1 == type){	
		    choiceTypeGroup.append("查询所有充值记录", null);
		    _form.append(choiceTypeGroup);
		    
	    }else if(2 == type){
			 choiceTypeGroup.append("查询所有客户端网游道具消费", null);
			 choiceTypeGroup.append("查询所有WAP网游道具消费", null);
			 choiceTypeGroup.append("查询幻想i时代的道具消费", null);
			 //#if MIDP-VERSION == MIDP-1.0
			 choiceGroup.setSelectedIndex(0, true);
			 //#endif
			 _form.append(choiceTypeGroup);
 

	    }
	    	    /*List list =  new List("",Choice.IMPLICIT );
	    Command dayCommand = new Command("今天", Command.ITEM, 1);
	    Command monthCommand = new Command("选择月份", Command.ITEM, 2);
	    Command tenDayCommand = new Command("十天内", Command.ITEM, 3);
	    list.addCommand(dayCommand);
	    list.addCommand(monthCommand);
	    list.addCommand(dayCommand);
	    int size = list.size();
	    _form.append(list);
	    
	    for(int i=0;i<size;i++)
	    {
	      if(i==0)
	      {
	         list.setSelectedIndex(i, true);
	      }else{
	        list.setSelectedIndex(i, false);
	      }

	     }*/ 

        //_form.append(new TextField("起始日期:", listStartDate, 8, TextField.NUMERIC));
        //_form.append(new TextField("结束日期:", listEndDate, 8, TextField.NUMERIC));
	    _form.addCommand(new Command("查询", Command.ITEM, 0));
	    }else if(3 == type){
	    	_form.append(new TextField("输入月份（例如2009年9月输入200909）", listStartDate, 6, TextField.NUMERIC));
	    	_form.addCommand(new Command("查询月份", Command.ITEM, 0));
	    }
        
        _form.addCommand(new Command("返回", Command.BACK, 0));
        
       // ChoiceGroup choiceGroup = new ChoiceGroup();
        _form.setCommandListener(this);
        
        //World.display.setCurrent(_form);
        World.RecordPreousDisplay(_form);
	}
	
	public void commandAction(Command command, Displayable display) {
	    String choice = command.getLabel();
	    
		if (choice.equals("是")) {
			confirm();
		} else if(choice.equals("否")){
			cancel();
		}else if(choice.equals("查询")){
		    Form form = (Form)display;
		    
		    queryTime = (short) ((ChoiceGroup)form.get(0)).getSelectedIndex();
		    queryType = (short) ((ChoiceGroup)form.get(1)).getSelectedIndex();
		   // listStartDate = ((TextField)form.get(1)).getString().trim();
		   // listEndDate = ((TextField)form.get(2)).getString().trim();
		    if(1 == queryTime){
		    	showDateForm("输入月份",(short)3);
		    	return;
		    }
		    //World.display.setCurrent(this);
		    World.RecordPreousDisplay(this);
		    World.clearKeyStates();
		    new Thread(this).start();
		}else if(choice.equals("返回")){
		    //World.display.setCurrent(this);
		    World.RecordPreousDisplay(this);
		    World.clearKeyStates();
		    cancel();
		}else if(choice.equals("查询月份")){
			Form form = (Form)display;
			listStartDate = ((TextField)form.get(0)).getString().trim();
		    //World.display.setCurrent(this);
		    World.RecordPreousDisplay(this);
		    World.clearKeyStates();
		    new Thread(this).start();
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
	    	if(2000 <= pressButton && 3000 > pressButton ){
	    		switch(state){
	 	        case STATE_MAIN:
	             case STATE_CHARE:
	             case STATE_CHARE_OVER:
	             case STATE_QUERY_OVER:
	             case STATE_DETAIL:
	            	 _menuIndex = pressButton - 2000; 
	            	 handkey(World.SOFT_FIRST_PRESSED);
	            	 break;
	             case STATE_CHARGE_HISTORY:
	             case STATE_BUY_HISTORY:
	                  listIndex = pressButton - 2000; 
	                  if(_list.size() > 0 && GameState.font.stringWidth((String)_list.elementAt(listIndex)) > World.viewWidth - (listMarginLeft + listMarginRight)){
	                      listNeedScroll = true;
	                      listScrollDir = 0;
	                      listScrollMax = GameState.font.stringWidth((String)_list.elementAt(listIndex)) - (World.viewWidth - (listMarginLeft + listMarginRight));
	                      listScrollOffset = 0;
	                  }else{
	                      listNeedScroll = false;
	                      listScrollDir = 0;
	                      listScrollMax = 0;
	                      listScrollOffset = 0;
	                  }
	                  break;
	    		}	
	    	}else if(pressButton == 3000){
	    		handkey(World.SOFT_FIRST_PRESSED);
	    	}else if(pressButton == 3001){
	    		handkey(World.SOFT_LAST_PRESSED);
	    	}else if( pressButton >= 4000  && pressButton <= 4009 ){
	    		handkey(pressButton - 4000 + 11);
	    	}
	    }

	 }
	  //#endif
//#endif
}
