package com.pip.sanguo;


import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import com.pip.common.Tool;
import com.pip.common.Utilities;
import com.pip.engine.IVMGameProcessor;
import com.pip.image.ImageSet;
import com.pip.io.UASegment;
import com.pip.ui.VM;
import com.pip.ui.VMGame;
import com.pip.util.SortHashtable;
import com.pip.util.VMCounter;

public class GamePanel implements IVMGameProcessor{
    private VM vm;
    private Hashtable gameData = new Hashtable();
    
    public byte state;

    private ImageSet[] images;
    private Hashtable templateTable;
    private Hashtable itemTable;
    private Vector itemList;
    /** 获得物品等待队列 */
    private Vector itemQueue;
    /** 获得物品 */
    private GamePanelItem obtainItem;
    /** 获得数值等待队列 */
    private Vector valueQueue;
    /** 获得物品 */
    private GamePanelItem obtainValue;
    /** 可点击的组件列表 */
    private Vector pointItems;
    
    public GamePanelItem hMessageItem;
    public GamePanelItem vMessageItem;
    
    //#if ModelID == Lenovo || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == AndroidAuto
    //聊天消息的字体
  //#  public Font hMsgFont;
  //#  public Font vMsgFont;
  //#  public Font landMarkFont;
    //#endif
    
    //landmark
    private GamePanelItem landmark;
    /** 当前cycle运行次数 */
    private int tick;
    private boolean needSort;
    /**
     * 用于存放冷却组id和剩余时间key的映射
     */
    private Hashtable cdKeyMap;
    
    private Tool idKey = new Tool();
    private Tool templateIDKey = new Tool();

    public static final byte GAME_PANEL_STATE_NOT_READY = 0;
    public static final byte GAME_PANEL_STATE_INIT = 1;
    public static final byte GAME_PANEL_STATE_SHOWING = 2;
    public static final byte GAME_PANEL_STATE_SHOW = 3;
    public static final byte GAME_PANEL_STATE_HIDING = 4;
    public static final byte GAME_PANEL_STATE_HIDE = 5;
    
    /** 动画icon */
    public static final byte GAME_PANEL_ITEM_TYPE_ANIMATE_ICON = 0;
    /** 图片icon */
    public static final byte GAME_PANEL_ITEM_TYPE_IMAGE_ICON = 1;
    /** 图片 */
    public static final byte GAME_PANEL_ITEM_TYPE_IMAGE = 2;
    public static final byte GAME_PANEL_ITEM_TYPE_BOX = 3;
    public static final byte GAME_PANEL_ITEM_TYPE_FILL_BOX = 4;
    public static final byte GAME_PANEL_ITEM_TYPE_LINE = 5;
    public static final byte GAME_PANEL_ITEM_TYPE_STATUS_BAR = 6;
    public static final byte GAME_PANEL_ITEM_TYPE_NUM = 7;
    public static final byte GAME_PANEL_ITEM_TYPE_MINI_ANIMATE = 8;
    public static final byte GAME_PANEL_ITEM_TYPE_ANIMATE = 9;
    public static final byte GAME_PANEL_ICON = 10;
    public static final byte GAME_PANEL_SKILL_ANIMATE = 11;
    public static final byte GAME_PANEL_HMSG_BAR = 12;
    public static final byte GAME_PANEL_VMSG_BAR = 13;
    public static final byte GAME_PANEL_ITEM_COUNTDOWN = 14;
    public static final byte GAME_PANEL_ITEM_OBTAIN = 15;
    public static final byte GAME_PANEL_VALUE_OBTAIN = 16;
    public static final byte GAME_PANEL_LANDMARK = 17;
    public static final byte GAME_PANEL_STRING= 18;
    public static final byte GAME_PANEL_ITEM_TYPE_ALPHA_BOX = 19;
    public static final byte GAME_PANEL_ITEM_TYPE_OPPOSITE_IMAGE = 20;
    
    public static final byte ICON_TICKER_TIME = 7;
    
    public static final byte HMSG_STEP = 4;       //横向滚动的速度
    public static final byte VMSG_STEP = 2;       //纵向滚动的速度
    public static final byte VMSG_STOP_TIME = 20;  //纵向滚动的停留时间
    public static final byte VMSG_STATE_STOP = 0; //纵向滚动的停留状态
    public static final byte VMSG_STATE_MOVE = 1; //纵向滚动的滚动状态
    
    public static final byte GAME_PANEL_ACTION_STATE_NON_ACTIVE = 0;
    public static final byte GAME_PANEL_ACTION_STATE_ACTIVE = 1;
    
    public static final byte GAME_PANEL_SKILL_COLD = 0;
    public static final byte GAME_PANEL_SKILL_NEED_COLD = 1;
    public static final byte GAME_PANEL_SKILL_READY_COLD = 2;

    private static final int STATUS_BAR_PRECISION = 10000;
    
    /** 绘制获得物品时的缩进 */
    private int offset;
    
    private int vMessageId;
    private int hMessageId;
    private int msgOldY;
    private static Tool keyMaker = new Tool();
    
    public GamePanel(){
        templateTable = new Hashtable();
        images = new ImageSet[0];
        itemTable = new Hashtable();
        itemList = new Vector();
        itemQueue = new Vector();
        valueQueue = new Vector();
        cdKeyMap = new Hashtable();
        pointItems = new Vector();
        needSort = true;
        landmark = new GamePanelItem(idKey.nextKey(), GAME_PANEL_LANDMARK);
        //测试使用值，需要动态赋值
        //#if ModelID == AndroidAuto
        //# if (GameMain.getUIModel().equals(GameMain.ANDROID_LARGE))
    	//# {
        //# offset = 6;
        //# }
        //# else
        //# {
        //# offset = 3;	
        //# }
        //#elif DoubleScreen == true
        //# offset = 6;
        //#else
        offset = 3;
        //#endif
    }

    public void init(){
        VMGame.loadVMGame("game_panel", VMGame.VM_TYPE_GAME, true);
        vm = VMGame.getVMGame("game_panel").getVM();
    }

    public synchronized void sendCommand(int command, Object commandData){
        synchronized(vm){
            int[] params = new int[3];

            params[0] = vm.makeTempObject(this);
            params[1] = command;
            params[2] = vm.makeTempObject(commandData);
            vm.callback(VMGame.CALLBACK_GAME_COMMAND, params);
        }
    }

    public void game_panel_set_state(int state){
        this.state = (byte)state;
    }
    
    public int game_panel_reg_image(ImageSet image){
        ImageSet[] temp = new ImageSet[images.length + 1];
        System.arraycopy(images, 0, temp, 0, images.length);
        temp[images.length] = image;
        images = temp;
        
        return images.length - 1;
    }
    
    public void game_panel_release_image(int imageIndex){
        images[imageIndex] = null;
    }
    
    public int game_panel_add_item_animate_icon(int layer, int iconIndex){
        GamePanelItem item = new GamePanelItem(idKey.nextKey(), GAME_PANEL_ITEM_TYPE_ANIMATE_ICON);
        item.layer = layer;
        item.intData1 = iconIndex;
        addItem(item);
        
        return item.id;
    }
    
    public int game_panel_add_item_image_icon(int layer, int iconIndex, int x, int y, int w, int h){
        GamePanelItem item = new GamePanelItem(idKey.nextKey(), GAME_PANEL_ITEM_TYPE_IMAGE_ICON);
        item.layer = layer;
        item.intData1 = iconIndex;
        item.x = (short) x;
        item.y = (short) y;
        item.w = (short) w;
        item.h = (short) h;
        addItem(item);
        
        return item.id;
    }
    
    public int game_panel_add_item_image(int layer, int imageIndex, int frameIndex, int x, int y, int trans, int anchor){
        GamePanelItem item = new GamePanelItem(idKey.nextKey(), GAME_PANEL_ITEM_TYPE_IMAGE);
        item.layer = layer;
        item.intData1 = imageIndex;
        item.intData2 = frameIndex;
        item.x = (short) x;
        item.y = (short) y;
        item.trans = (byte) trans;
        item.anchor = (byte) anchor;
        addItem(item);
        
        return item.id;
    }
    
    public int game_panel_add_item_opposite_image(int layer, int imageIndex, int frameIndex, int x, int y, int trans, int anchor){
        GamePanelItem item = new GamePanelItem(idKey.nextKey(), GAME_PANEL_ITEM_TYPE_OPPOSITE_IMAGE);
        item.layer = layer;
        item.intData1 = imageIndex;
        item.intData2 = frameIndex;
        item.x = (short) x;
        item.y = (short) y;
        item.trans = (byte) trans;
        item.anchor = (byte) anchor;
        addItem(item);
        
        return item.id;
    }
    
    public int[] game_panel_get_opposite_image_box(int itemId){
        GamePanelItem item = (GamePanelItem)itemTable.get(new Integer(itemId));
        int[] result = new int[]{
                        -1, -1, -1, -1
        };
        
        if(item != null && item.intData1 < images.length && item.intData2 < images[item.intData1].getFrameCount()){
            result[0] = item.x;
            result[1] = item.y;
            result[2] = images[item.intData1].getFrameWidth(item.intData2);
            result[3] = images[item.intData1].getFrameHeight(item.intData2);
            
            GamePanelItem hMsgItem = (GamePanelItem)itemTable.get(new Integer(hMessageId));
            GamePanelItem vMsgItem = (GamePanelItem)itemTable.get(new Integer(vMessageId));
            
            if(hMsgItem != null && ((Vector)hMsgItem.objData).size() > 0){
                result[1] -= hMsgItem.h;
            }
            
            if(vMsgItem != null && ((Vector)vMsgItem.objData).size() > 0){
                result[1] -= vMsgItem.h;
            }
        }
        
        return result;
    }
    
    public boolean game_panel_point_in_box(int pointX, int pointY, int[] box){
        return Tool.rectIn(box[0], box[1], box[2], box[3], pointX, pointY);
    }
        
    public void game_panel_change_item_image(int id, int imageIndex, int startIndex, int offsetX, int offsetY){
        GamePanelItem item = (GamePanelItem)itemTable.get(new Integer(id));
        
        if(item != null){
            item.intData1 = imageIndex;
            item.intData2 = startIndex;
            item.x += offsetX;
            item.y += offsetY;
        }
    }
    
    public int game_panel_add_item_box(int layer, int color, int x, int y, int w, int h){
        GamePanelItem item = new GamePanelItem(idKey.nextKey(), GAME_PANEL_ITEM_TYPE_BOX);
        item.layer = layer;
        item.intData1 = color;
        item.x = (short) x;
        item.y = (short) y;
        item.w = (short) w;
        item.h = (short) h;
        addItem(item);
        
        return item.id;
    }
    
    public int game_panel_add_item_fill_box(int layer, int color, int x, int y, int w, int h){
        GamePanelItem item = new GamePanelItem(idKey.nextKey(), GAME_PANEL_ITEM_TYPE_FILL_BOX);
        item.layer = layer;
        item.intData1 = color;
        item.x = (short) x;
        item.y = (short) y;
        item.w = (short) w;
        item.h = (short) h;
        addItem(item);
        
        return item.id;
    }
    
    public int game_panel_add_item_line(int layer, int color, int x1, int y1, int x2, int y2){
        GamePanelItem item = new GamePanelItem(idKey.nextKey(), GAME_PANEL_ITEM_TYPE_LINE);
        item.layer = layer;
        item.intData1 = color;
        item.x = (short) x1;
        item.y = (short) y1;
        item.w = (short) (x2 - x1);
        item.h = (short) (y2 - y1);
        addItem(item);
        
        return item.id;
    }
    
    public int game_panel_add_item_status_bar(int layer, int color, int x, int y, int w, int h){
        GamePanelItem item = new GamePanelItem(idKey.nextKey(), GAME_PANEL_ITEM_TYPE_STATUS_BAR);
        item.layer = layer;
        item.intData1 = color;
        item.intData2 = STATUS_BAR_PRECISION;
        item.x = (short) x;
        item.y = (short) y;
        item.w = (short) w;
        item.h = (short) h;
        addItem(item);
        
        return item.id;
    }
    
    public void game_panel_change_item_status_bar(int id, int curValue, int maxValue){
        GamePanelItem item = (GamePanelItem)itemTable.get(new Integer(id));
        
        if(item != null && maxValue > 0 && curValue <= maxValue){
            item.intData2 = (int)((long)curValue * STATUS_BAR_PRECISION / maxValue);
        }
    }
    
    public void game_panel_remove_item(int id){
        removeItem(id);
    }
    
    public void game_panel_clear_item(){
        itemList.removeAllElements();
        itemTable.clear();
    }
    
    public int game_panel_add_item_num(int layer, int imageIndex, int startIndex, int x, int y, int space, int anchor, int num){
        GamePanelItem item = new GamePanelItem(idKey.nextKey(), GAME_PANEL_ITEM_TYPE_NUM);
        item.layer = layer;
        item.intData1 = imageIndex;
        item.intData2 = startIndex;
        item.x = (short) x;
        item.y = (short) y;
        item.trans = (byte) space;
        item.anchor = (byte) anchor;
        item.objData = String.valueOf(num);
        addItem(item);
        
        return item.id;
    }
    
    public void game_panel_change_item_num(int id, int num){
        GamePanelItem item = (GamePanelItem)itemTable.get(new Integer(id));
        
        if(item != null){
            item.objData = String.valueOf(num);
        }
    }
    
    public int game_panel_add_item_mini_animate(int layer, int imageIndex, int startIndex, int x, int y, int trans, int anchor, int moveType, int totalDistance, int totalTicks){
        GamePanelItem item = new GamePanelItem(idKey.nextKey(), GAME_PANEL_ITEM_TYPE_MINI_ANIMATE);
        item.layer = layer;
        item.intData1 = imageIndex;
        item.intData2 = startIndex;
        item.x = (short) x;
        item.y = (short) y;
        item.trans = (byte) trans;
        item.anchor = (byte) anchor;

        int gcd = Tool.gcd(totalDistance, totalTicks);

        //小动画类型的obj参数为int数组 int[0]为标志x还是y方面发生位移(等于0为x方向，其余为y方向），int[1]每次移动的步长
        //int[2]每隔多少tick进行一次移动，int[3]总位移量,  int[4]位移的目标位置, int[5]下次需要进行移动的tick数
        int[] tmp = new int[6];
        tmp[0] = moveType;
        tmp[1] = totalDistance / gcd;
        tmp[2] = totalTicks / gcd;
        tmp[3] = totalDistance;

        if(tmp[0] == 0){
            tmp[4] = item.x + tmp[3];
        }else{
            tmp[4] = item.y + tmp[3];
        }

        tmp[5] = tick;
        item.objData = tmp;
        addItem(item);
        
        return item.id;
    }
    
    public int game_panel_add_item_animate(int layer, int imageIndex, int x, int y, int trans, int anchor, int ticks, int[] startIndex){
        GamePanelItem item = new GamePanelItem(idKey.nextKey(), GAME_PANEL_ITEM_TYPE_ANIMATE);
        item.layer = layer;
        item.intData1 = imageIndex;
        item.x = (short) x;
        item.y = (short) y;
        item.trans = (byte) trans;
        item.anchor = (byte) anchor;
        item.intData2 = ticks;

        int[] sequences = new int[startIndex.length];
        System.arraycopy(startIndex, 0, sequences, 0, startIndex.length);

        item.objData = sequences;
        item.ready = true;
        item.tick = tick;
        addItem(item);
        
        return item.id;
    }
    
    public int game_panel_add_item_icon(int state, int x, int y, int imageIndex, int frameIndex, int numImageIndex, int numStart, int key){
        GamePanelItem item = new GamePanelItem(idKey.nextKey(), GAME_PANEL_ICON);

        /* 状态 */
        item.intData1 = state;

        item.x = (short) x;
        item.y = (short) y;
        int[] tmp = new int[5];
        /* 图片索引 */
        tmp[0] = imageIndex;
        /* 帧索引 */
        tmp[1] = frameIndex;
        /* 数字图片索引 */
        tmp[2] = numImageIndex;
        /* 数字帧索引 */
        tmp[3] = numStart;
        /* 快捷键 */
        tmp[4] = key;

        item.objData = tmp;
        item.tick = tick;
        item.anchor = Graphics.TOP | Graphics.RIGHT;
        addItem(item);
        
        return item.id;
    }
    
    public void game_panel_action_state_change(int _state){
        int count = itemList.size();
        
        for(int i = 0; i < count; i++){
            GamePanelItem item = (GamePanelItem) itemList.elementAt(i);
            if(item.type == GAME_PANEL_ICON){
                item.intData1 = _state;
            }
        }
    }
    
    public int game_panel_add_skill_animate(int layer, int imageIndex, int startIndex, int x, int y, int anchor, int coldGroup, int coldState, int coldHeight, int color){
        GamePanelItem item = new GamePanelItem(idKey.nextKey(), GAME_PANEL_SKILL_ANIMATE);
        item.layer = layer;
        item.intData1 = imageIndex;
        item.intData2 = startIndex;
        item.x = (short) x;
        item.y = (short) y;
        item.anchor = (byte) anchor;

        //计算技能图片位置
        item.w = (short) images[item.intData1].getFrameWidth(item.intData2);
        item.h = (short) images[item.intData1].getFrameHeight(item.intData2);
        if((item.anchor & Graphics.HCENTER) > 0){
            item.x -= item.w / 2;
        }else if((item.anchor & Graphics.RIGHT) > 0){
            item.x -= item.w;
        }
        if((item.anchor & Graphics.VCENTER) > 0){
            item.y -= item.h / 2;
        }else if((item.anchor & Graphics.BOTTOM) > 0){
            item.y -= item.h;
        }

        /** 
         * 冷却组id
         */
        int[] arr = new int[4];
        arr[0] = coldGroup; //cd groupId
        arr[1] = coldState; //coldState
        arr[2] = coldHeight; //coldHeight
        arr[3] = color; //蒙板颜色
        item.objData = arr;
        addItem(item);
        
        return item.id;
    }
    
    public void game_panel_change_skill_cold_group(int coldDownId, int coldStartTime, int duration){
        addCdKey(coldDownId, coldStartTime, duration);
    }
    
    public int game_panel_add_item_countdown(int layer, int imageIndex, int frameIndex, int x, int y, int anchor, int countId, int rollId){
        GamePanelItem item = new GamePanelItem(idKey.nextKey(), GAME_PANEL_ITEM_COUNTDOWN);
        item.layer = layer;
        item.intData1 = imageIndex;
        item.intData2 = frameIndex;
        item.x = (short) x;
        item.y = (short) y;
        item.anchor = (byte) anchor;
        item.objData = new Integer(countId);
        int[] rollData = new int[1];
        rollData[0] = rollId;
        item.objData2 = rollData;
        addItem(item);
        
        return item.id;
    }
    
    public void game_panel_add_obtain_item(int _imageIndex, int _frameIndex, int _color, String _name, int _layer){
    	game_panel_add_obtain_item2(_imageIndex, _frameIndex, _color, _name, _layer,-1,-1,-1);
    }
    
    public void game_panel_add_obtain_item2(int _imageIndex, int _frameIndex, int _color, String _name, int _layer,int numImageIndex, int numIndex, int count){
        GamePanelItem item = new GamePanelItem(idKey.nextKey(), GAME_PANEL_ITEM_OBTAIN);
        item.intData1 = _imageIndex;
        item.intData2 = _frameIndex;
        item.objData2 = new Integer(_color);
        item.objData = _name;
        item.layer = _layer;
        if(numImageIndex != -1){
        	item.objData3 = new int[]{numImageIndex,numIndex,count};
        }
        addObtainItem(item);
    }
    
    public void game_panel_add_obtain_value(int imageIndex, int frameIndex, int numImageIndex, int numIndex, int value, int layer){
        GamePanelItem item = new GamePanelItem(idKey.nextKey(), GAME_PANEL_VALUE_OBTAIN);
        item.intData1 = imageIndex;
        item.intData2 = frameIndex;

        int[] dataArr = new int[3];
        dataArr[0] = numImageIndex;//数字id
        dataArr[1] = numIndex;//数字索引
        dataArr[2] = value;//数值
        item.objData = dataArr;
        item.layer = layer;
        addObtainValue(item);
    }
    
    public int game_panel_add_item_hmessage_bar(int x, int y, int w, int h, int layer, int edge, int alpha){
        hMessageItem = new GamePanelItem(idKey.nextKey(), GAME_PANEL_HMSG_BAR);
        hMessageItem.layer = layer;
        hMessageItem.x = (short) x;
        hMessageItem.y = (short) y;
        hMessageItem.w = (short) w;
        hMessageItem.h = (short) h;
        hMessageItem.frame = edge; //描边
        hMessageItem.intData1 = alpha; //alpha值
        hMessageItem.objData = new Vector();
        hMessageItem.objData2 = new Vector();
        //#if ScreenCanReset == true
        hMessageItem.objData3 = new Vector(); //存放每条滚动行的key
        hMessageItem.objData4 = new SortHashtable(); //存放每条记录
        //#endif
        addItem(hMessageItem);
        hMessageId = hMessageItem.id;
        
        return hMessageItem.id;
    }
    
    public int game_panel_add_item_vmessage_bar(int x, int y, int w, int h, int layer, int edge, int alpha){
        vMessageItem = new GamePanelItem(idKey.nextKey(), GAME_PANEL_VMSG_BAR);
        vMessageItem.layer = layer;
        vMessageItem.x = (short) x;
        vMessageItem.y = (short) y;
        vMessageItem.w = (short) w;
        vMessageItem.h = (short) h;
        vMessageItem.frame = edge; //描边
        vMessageItem.intData1 = alpha; //颜色，包含alpha值
        vMessageItem.objData = new Vector();
        vMessageItem.objData2 = new Vector();
        //#if ScreenCanReset == true
        vMessageItem.objData3 = new Vector(); //存放每条滚动行的key
        vMessageItem.objData4 = new SortHashtable(); //存放每条记录
        //#endif
        
        addItem(vMessageItem);
        vMessageId = vMessageItem.id;
        msgOldY = vMessageItem.y;
        
        return vMessageItem.id;
    }
    
    public void game_panel_change_hmessage_alpha(int alpha){
        hMessageItem.intData1 = alpha;
    }
    
    public void game_panel_change_vmessage_alpha(int alpha){
        vMessageItem.intData1 = alpha;
    }
    
    public void game_panel_post_hmessage(int id, String msg, int color, int maxRecord){
        int _count = GameWorld.instance.vm_game_get_hmsg_count();

        if(_count < maxRecord){
            hMessageItem = (GamePanelItem) itemTable.get(new Integer(id));

            String hmsgStr = msg;
            Vector hmsgs = (Vector) hMessageItem.objData;
            int count = hmsgs.size();
            for(int i = 0; i < count; i++){
                //如果该消息正在播放，则不添加
                if(hmsgStr.equals(hmsgs.elementAt(i))){
                    return;
                }
            }

            if(((Vector) hMessageItem.objData).size() == 0){
                hMessageItem.tick = tick;
            }
            //#if ScreenCanReset == true
            Long key = new Long(keyMaker.nextKey() | color << 32);
            ((SortHashtable) vMessageItem.objData4).put(key, hmsgStr);
            ((Vector) hMessageItem.objData3).addElement(key);
            //#endif
            ((Vector) hMessageItem.objData).addElement(hmsgStr);
            ((Vector) hMessageItem.objData2).addElement(new Integer(color));

            if(((Vector) vMessageItem.objData).size() > 0){
                hMessageItem.y = (short) (vMessageItem.y - hMessageItem.h);
            }else{
                hMessageItem.y = (short) (vMessageItem.y + vMessageItem.h - hMessageItem.h);
            }
        }
    }
    
    public void game_panel_post_vmessage(int id, String msg, int color, int maxRecord){
        int _count = GameWorld.instance.vm_game_get_vmsg_count();

        if(_count < maxRecord){
            vMessageItem = (GamePanelItem) itemTable.get(new Integer(id));
    
            String srcString = msg;
          //#if ScreenCanReset == true
            Long key = new Long(keyMaker.nextKey() | ((long)color << 32));
            ((SortHashtable) vMessageItem.objData4).put(key, srcString);
            //#endif
            
            String[] destStrings = Tool.formatText(srcString, vMessageItem.w, Utilities.font);
            Vector[] destVecs = new Vector[destStrings.length];
            for (int i = 0; i < destVecs.length; i++) {
            	//#if ModelID == Lenovo || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == AndroidAuto
            	//# if(this.vMsgFont != null) {
            	//# 	destVecs[i] = Tool.formatString(destStrings[i], 100000, vMsgFont, true);
            	//# } else {
            	//# 	destVecs[i] = Tool.formatString(destStrings[i], 100000, Utilities.font, true);
            	//# }
            	//#else
            	destVecs[i] = Tool.formatString(destStrings[i], 100000, Utilities.font, true);
            	//#endif   
            }
    
            for(int i = 0; i < destStrings.length; i++){
                ((Vector) vMessageItem.objData).addElement(destVecs[i]);
                ((Vector) vMessageItem.objData2).addElement(new Integer(color));
            	//#if ScreenCanReset == true
            	((Vector) vMessageItem.objData3).addElement(key);
            	//#endif
            }
    
            vMessageItem.tick = tick;
    
            hMessageItem.y = (short) (vMessageItem.y - hMessageItem.h);
        }
    }
    
    //#if ScreenCanReset == true
    public SortHashtable game_panel_get_vmessage() {
    	return ((SortHashtable) vMessageItem.objData4);
    }
    
    public SortHashtable game_panel_get_hmessage() {
    	return ((SortHashtable) hMessageItem.objData4);
    }
    //#endif
    
    public void game_panel_set_landmark_config(int _x, int _y, int _color, int _bgColor, int _archor, int _layer){
        landmark.x = (short) _x;
        landmark.y = (short) _y;
        landmark.objData = getCurrentLandStr();
        landmark.intData1 = _color; //forground
        landmark.intData2 = _bgColor; //background
        landmark.anchor = (byte)_archor;
        landmark.layer = _layer;
    }
    
    public int game_panel_add_stringEx(String str, int x, int y, int offset,int anchor, int is3d, int isTopOfMsg, int forColor, int bgColor, int layer, Font font){
        GamePanelItem item = new GamePanelItem(idKey.nextKey(), GAME_PANEL_STRING);
        item.objData = str;
        item.x = (short) x;
        item.y = (short) y;
        item.anchor = (byte)anchor;
        int[] strData = new int[5];
        strData[0] = is3d;//is3d
        strData[1] = isTopOfMsg;//is top of msg
        strData[2] = forColor;//forground color
        strData[3] = bgColor;//background color
        strData[4] = offset;//offset from msg
        item.objData2 = strData;
        
      //#if ScreenCanReset == true
        if(VM.getApiVersion() >= 2) {
        	item.objData3 = font;
        }
        //#endif
        item.layer = layer;
        addItem(item);
        
        return item.id;
    }
    
    public int game_panel_add_string(String str, int x, int y, int offset,int anchor, int is3d, int isTopOfMsg, int forColor, int bgColor, int layer){
        return game_panel_add_stringEx(str, x, y, offset, anchor, is3d, isTopOfMsg, forColor, bgColor, layer, Utilities.font);
    }
    
    public void game_panel_remove_skill_cold_group(int coldDownId){
        removeCdKey(coldDownId);
    }
    
    public int game_panel_add_item_alpha_fill_box(int layer, int color, int x, int y, int w, int h){
        GamePanelItem item = new GamePanelItem(idKey.nextKey(), GAME_PANEL_ITEM_TYPE_ALPHA_BOX);
        item.layer = layer;
        item.intData1 = color;
        item.x = (short) x;
        item.y = (short) y;
        item.w = (short) w;
        item.h = (short) h;
        addItem(item);
        
        return item.id;
    }
    
    public void game_panel_change_item_pos(int id, short x, short y) {
        GamePanelItem item = (GamePanelItem)itemTable.get(new Integer(id));
        item.x = x;
        item.y = y;
    }
    
    public void game_panel_clear_array(int[] arr,int padding){
    	for (int i = 0; i < arr.length; i++) {
			arr[i] = padding;
		}
    }

    public void game_panel_reg_point_item(int notifyId, int x, int y, int w, int h, boolean opposite, int notifyData, int anchor){
        GamePanelPointItem pointItem = new GamePanelPointItem();
        pointItem.notifyId = notifyId;
        pointItem.opposite = opposite;
        pointItem.effect = true;
        pointItem.notifyData = notifyData;
        
        switch(anchor){
            case Tool.G_BOTTOM:
                y -= h;
                break;
            case Tool.G_HCENTER:
                x -= w / 2;
                break;
            case Tool.G_LEFT:
                break;
            case Tool.G_RIGHT:
                x -= w;
                break;
            case Tool.G_TOP:
                break;
            case Tool.G_VCENTER:
                y -= h / 2;
                break;
            case Tool.G_TOPLEFT:
                break;
            case Tool.G_CENTER:
                x -= w / 2;
                y -= h / 2;
                break;
            case Tool.G_TOPCENTER:
                x -= w / 2;
                break;
            case Tool.G_TOPRIGHT:
                x -= w;
                break;
            case Tool.G_BOTTOMLEFT:
                y -= h;
                break;
            case Tool.G_BOTTOMRIGHT:
                x -= w;
                y -= h;
                break;
            case Tool.G_BOTTOMCENTER:
                x -= w / 2;
                y -= h;
                break;
            case Tool.G_LEFTCENTER:
                y -= h / 2;
                break;
            case Tool.G_RIGHTCENTER:
                x -= w;
                y -= h / 2;
                break;
        }
        
        pointItem.x = x;
        pointItem.y = y;
        pointItem.w = w;
        pointItem.h = h;
        pointItems.addElement(pointItem);
    }
    
    public void game_panel_remove_point_item(int notifyId){
        int count = pointItems.size();
        Vector restItems = new Vector();
        
        for(int i = 0; i < count; i++){
            GamePanelPointItem pointItem = (GamePanelPointItem)pointItems.elementAt(i);
            
            if(pointItem.notifyId != notifyId){
                restItems.addElement(pointItem);
            }
        }
        
        pointItems = restItems;
    }
    
    public void game_panel_set_point_item_effect(int notifyId, boolean effect){
        int count = pointItems.size();

        for(int i = 0; i < count; i++){
            GamePanelPointItem pointItem = (GamePanelPointItem)pointItems.elementAt(i);
            
            if(pointItem.notifyId == notifyId){
                pointItem.effect = effect;
            }
        }
    }
    
    public void game_panel_clear_point_item(){
        pointItems.removeAllElements();
    }
    
    public static int[] getPointItem(int pointX, int pointY){
        if(GameWorld.panel != null){
            GamePanel panelInstance = GameWorld.panel;
            int count = panelInstance.pointItems.size();
            
            for(int i = count - 1; i >= 0; i--){
                GamePanelPointItem pointItem = (GamePanelPointItem)panelInstance.pointItems.elementAt(i);
                
                if(!pointItem.effect){
                    continue;
                }else{
                    if(pointItem.opposite){
                        int my = pointItem.y;
                        
                        GamePanelItem hMsgItem = (GamePanelItem)panelInstance.itemTable.get(new Integer(panelInstance.hMessageId));
                        GamePanelItem vMsgItem = (GamePanelItem)panelInstance.itemTable.get(new Integer(panelInstance.vMessageId));
                        
                        if(hMsgItem != null && ((Vector)hMsgItem.objData).size() > 0){
                            my -= hMsgItem.h;
                        }
                        
                        if(vMsgItem != null && ((Vector)vMsgItem.objData).size() > 0){
                            my -= vMsgItem.h;
                        }
                        
                        if(Tool.rectIn(pointItem.x, my, pointItem.w, pointItem.h, pointX, pointY)){
                            return new int[]{
                                            -1, pointItem.notifyId, pointItem.x, my, pointItem.w, pointItem.h, pointItem.notifyData
                            };
                        }
                    }else{
                        if(Tool.rectIn(pointItem.x, pointItem.y, pointItem.w, pointItem.h, pointX, pointY)){
                            return new int[]{
                                            -1, pointItem.notifyId, pointItem.x, pointItem.y, pointItem.w, pointItem.h, pointItem.notifyData
                            };
                        }
                    }
                }
            }
        }
            
        if(GameWorld.gameView != null){
            int spriteCount = GameWorld.gameSprites.size();
            int realX = pointX + GameWorld.viewX;
            int realY = pointY + GameWorld.viewY;
            
            //选择了自己
            if(GameWorld.player != null) {
            	int[] box2 = GameWorld.player.vm_sprite_get_animate_box();
            	 if(Tool.rectIn(box2[0], box2[1], box2[2], box2[3], realX, realY)){
                     return new int[]{
                    		 GameWorld.player.getInstanceId(), 0, 0, 0, 0, 0, 0
                     };
                 }
            }
            
            for(int i = 0; i < spriteCount; i++){
                GameSprite gameSprite = (GameSprite)GameWorld.gameSprites.elementAt(i);
                
                if(GameWorld.gameView.checkTarget(gameSprite) != null){
                    int[] box = gameSprite.vm_sprite_get_animate_box();
    
                    if(Tool.rectIn(box[0], box[1], box[2], box[3], realX, realY)){
                        return new int[]{
                                        gameSprite.getInstanceId(), 0, 0, 0, 0, 0, 0
                        };
                    }
                }
            }
        }
        
        return new int[]{
                        -1, -1, 0, 0, 0, 0, 0
        };
    }

    public String getCurrentLandStr(){
    	if(GameWorld.currentMap == null)
    		return "";
    	String landName = GameWorld.currentMap.name;
        int extraIdx = landName.indexOf('|');
        
        if(extraIdx >= 0){
        	landName = landName.substring(0, extraIdx);
        }
        int playerX = 0;
    	int playerY = 0;
    	//#if ModelID == AndroidAuto
        //# if (GameMain.getUIModel().equals(GameMain.ANDROID_LARGE))
    	//# {
    	//# playerX = GameWorld.player.sprite.getX() / 16;
    	//# playerY = GameWorld.player.sprite.getY() / 16;
    	//# }
    	//# else
    	//# {
    	//# playerX = GameWorld.player.sprite.getX() / 8;
    	//# playerY = GameWorld.player.sprite.getY() / 8;	
    	//# }
    	//#elif DoubleScreen == true
    	//# playerX = GameWorld.player.sprite.getX() / 16;
    	//# playerY = GameWorld.player.sprite.getY() / 16;
    	//#else
    	playerX = GameWorld.player.sprite.getX() / 8;
    	playerY = GameWorld.player.sprite.getY() / 8;
    	//#endif
    	StringBuffer sb = new StringBuffer(landName);
    	sb.append("(");
    	sb.append(playerX);
    	sb.append(",");
    	sb.append(playerY);
    	sb.append(")");
    	String str = sb.toString();
    	sb = null;
    	return str;
    }

	public Object readGameData(String dataName){
        return gameData.get(dataName);
    }

    public void removeGameData(String dataName){
        gameData.remove(dataName);
    }

    public void saveGameData(String dataName, Object data){
        gameData.put(dataName, data);
    }
    
    private int getCdKey(int cdId){
        Integer cdKey = (Integer)cdKeyMap.get(new Integer(cdId));
        if(cdKey == null){
            return -1;
        }
        else{
            return cdKey.intValue();
        }
    }
    
    private void addCdKey(int cdId, int startTime, int counterTime){
    	Integer groupId = new Integer(cdId);
    	if(cdKeyMap.containsKey(groupId)){
    		int key = ((Integer)cdKeyMap.get(groupId)).intValue();
    		VMCounter.setCounter(key, startTime, counterTime);
    	} else {
    		int key = VMCounter.createVMCounter(startTime,counterTime);
    		cdKeyMap.put(groupId, new Integer(key));
    	}
    }
    
    private void removeCdKey(int cdId){
    	Integer groupId = new Integer(cdId);
    	if(cdKeyMap.containsKey(groupId)){
    		int key = ((Integer)cdKeyMap.get(groupId)).intValue();
    		VMCounter.removeVMCounter(key);
    		cdKeyMap.remove(groupId);
    		
    		sendCommand(VMGame.GAME_COMMAND_PANEL_SYNC_CD, new int[]{cdId});
    	}
    }
    
    private void addItem(GamePanelItem item){
        itemTable.put(new Integer(item.id), item);
        itemList.addElement(item);
        needSort = true;
    }
    
    /**
     * 向等待队列中添加一个获得的物品
     * @param item
     */
    private void addObtainItem(GamePanelItem item){
        synchronized (itemQueue) {
            itemQueue.addElement(item);
        }
        
        try {
            Thread.sleep(5);
        }
        catch (InterruptedException e) {
        }
    }
    
    /**
     * 向等待队列中添加一个获得的数值
     * @param value
     */
    private void addObtainValue(GamePanelItem value){
        synchronized (valueQueue) {
            valueQueue.addElement(value);
        }
        
        try {
            Thread.sleep(5);
        }
        catch (InterruptedException e) {
        }
    }

    private void removeItem(int itemId){
        itemTable.remove(new Integer(itemId));

        for(int i = 0; i < itemList.size(); i++){
            GamePanelItem item = (GamePanelItem)itemList.elementAt(i);

            if(item.id == itemId){
                itemList.removeElementAt(i);
                break;
            }
        }

        needSort = true;
    }

    public void cycle(){
        tick++;

        if(needSort){
            sort();
            needSort = false;
        }

        switch(state){
            case GAME_PANEL_STATE_NOT_READY:
                break;
            case GAME_PANEL_STATE_INIT:
                sendCommand(VMGame.GAME_COMMAND_PANEL_INIT, null);
                break;
            case GAME_PANEL_STATE_SHOWING:
                break;
            case GAME_PANEL_STATE_SHOW:
                updateColdDown();
                updateObtains();
                updateLandmark();
                break;
            case GAME_PANEL_STATE_HIDING:
                break;
            case GAME_PANEL_STATE_HIDE:
                break;
        }
    }
    
    private void updateLandmark() {
		landmark.objData = getCurrentLandStr();
	}

	public void draw(Graphics g,int low,int high){
        switch(state){
            case GAME_PANEL_STATE_NOT_READY:
                break;
            case GAME_PANEL_STATE_INIT:
                break;
            case GAME_PANEL_STATE_SHOWING:
                break;
            case GAME_PANEL_STATE_SHOW:
            	//System.out.println("GAME_PANEL_STATE_SHOW");
                drawPanel(g, 0,low,high);
                if(obtainItem != null)
                  drawItem(g, obtainItem,low,high);
                if(obtainValue != null)
                  drawItem(g, obtainValue,low,high);
                if(landmark != null){
                  //System.out.println("draw landmark:");
                  drawItem(g, landmark,low,high);    
                }
                
                break;
            case GAME_PANEL_STATE_HIDING:
                break;
            case GAME_PANEL_STATE_HIDE:
                break;
        }
           
        drawMsgPanel(g,low,high);
        
    }
    
    public void drawMsgPanel(Graphics g,int low,int high) {
    	drawItem(g, hMessageItem,low,high);
        drawItem(g, vMessageItem,low,high);
    	
    }
    
    /**
     * 设置底边的位置
     * @param y
     */
    public void setMsgPanelY(int y){
    	vMessageItem.y = (short) (y - vMessageItem.h);
    	
        if(((Vector)vMessageItem.objData).size() > 0) {
        	hMessageItem.y = (short) (vMessageItem.y - hMessageItem.h);
        } else{                    	
        	hMessageItem.y = (short) (vMessageItem.y + vMessageItem.h - hMessageItem.h);
        }
    	
    }
    
    public int getMsgPanelY() {
    	return msgOldY + vMessageItem.h;	
    }
    
    private void updateColdDown(){
        int size = itemList.size();

        for(int i = 0; i < size; i++){
            GamePanelItem item = (GamePanelItem)itemList.elementAt(i);
            
            switch(item.type){
                case GAME_PANEL_SKILL_ANIMATE:{
                    int[] arr = (int[])item.objData;
                    switch(arr[1]){
                        case GAME_PANEL_SKILL_COLD:{
                            int cdKey = getCdKey(arr[0]);
                            if(cdKey != -1){
                                int rate = VMCounter.getProcess(cdKey);
                                if(rate >= 0){
                                    arr[1] = GAME_PANEL_SKILL_NEED_COLD;
                                }
                                else{
                                    //清上次残余冷却组
                                    removeCdKey(arr[0]);
                                }
                            }
                            break;
                        }
                        case GAME_PANEL_SKILL_NEED_COLD:{
                            int cdKey = getCdKey(arr[0]);
                            if(cdKey != -1){
                                int rate = VMCounter.getProcess(cdKey);
                                if(rate >= 0){
                                    arr[2] = item.h * (100 - rate) / 100;
                                }
                                else{
                                    item.frame = 0;
                                    arr[1] = GAME_PANEL_SKILL_READY_COLD;
                                    removeCdKey(arr[0]);
                                }
                            } else {
                            	item.frame = 0;
                                arr[1] = GAME_PANEL_SKILL_READY_COLD;	
                            }
                            break;
                        }
                        case GAME_PANEL_SKILL_READY_COLD:{
                            if(item.frame > 6){
                                arr[1] = GAME_PANEL_SKILL_COLD;
                            }
                            item.frame++;
                            break;
                        }
                    }
                    break;
                }
            }
        }
        
        
    }
       
    private void updateObtains(){
        if(obtainItem == null){
            synchronized (itemQueue) {
                //获得下一个等待显示的物品
                if(itemQueue.size() > 0){
                    obtainItem = (GamePanelItem)itemQueue.elementAt(0);
                    itemQueue.removeElementAt(0);
                }
            }
            
            if(obtainItem != null){
                //图标宽度
                int itemWidth = images[obtainItem.intData1].getFrameWidth(obtainItem.intData2);
                itemWidth += Utilities.font.stringWidth((String)obtainItem.objData);
//#if  ModelID == Android                
                //# obtainItem.x = (short)(GameMain.viewWidth - itemWidth * 3);
//#elif  ModelID == Lenovo || ModelID == AndroidLarge
               //# obtainItem.x = (short)(GameMain.viewWidth - itemWidth * 2 - 15);
//#elif ModelID == AndroidAuto
              //# if (GameMain.getUIModel().equals(GameMain.ANDROID_NORMAL))
              //# {
              //# obtainItem.x = (short)(GameMain.viewWidth - itemWidth * 3);
              //# }
              //# else if (GameMain.getUIModel().equals(GameMain.ANDROID_LARGE))
              //# {
              //# obtainItem.x = (short)(GameMain.viewWidth - itemWidth * 2 - 15);
              //# }
              //# else
              //# {
              //#  obtainItem.x = (short)(GameMain.viewWidth - itemWidth);
              //# }             
//#else
                obtainItem.x = (short)(GameMain.viewWidth - itemWidth);
//#endif                 
                obtainItem.w = (short)itemWidth;
                obtainItem.h = (short)(Utilities.CHAR_HEIGHT + 2);
                obtainItem.frame = 0;
            }
        }
        else{
            obtainItem.frame++;
            if(obtainItem.frame > 30){
                if(obtainItem.x < GameMain.viewWidth){
                    obtainItem.x += offset;
                }
                else{
                    obtainItem = null;
                }
            }
        }
        
        if(obtainValue == null){
            synchronized (valueQueue) {
                //获得下一个等待显示的物品
                if(valueQueue.size() > 0){
                    obtainValue = (GamePanelItem)valueQueue.elementAt(0);
                    valueQueue.removeElementAt(0);
                }
            }
            
            if(obtainValue != null){
                int[] dataArr = (int[])obtainValue.objData;
                int frameWidth = images[dataArr[0]].getFrameWidth(dataArr[1]);
                //数字的宽度
                int itemWidth = String.valueOf(dataArr[2]).length() * frameWidth;
                //加号宽度
                itemWidth += images[dataArr[0]].getFrameWidth(dataArr[1] + 10);
                //图标宽度
                itemWidth += images[obtainValue.intData1].getFrameWidth(obtainValue.intData2);
//#if  ModelID == Android                 
                //# obtainValue.x = (short)(GameMain.viewWidth - itemWidth * 3);
//#elif  ModelID == Lenovo || ModelID == AndroidLarge    
               //# obtainValue.x = (short)(GameMain.viewWidth - itemWidth * 2 - 15);
//#elif ModelID == AndroidAuto
                //# if (GameMain.getUIModel().equals(GameMain.ANDROID_NORMAL))
                //# {
                //# obtainValue.x = (short)(GameMain.viewWidth - itemWidth * 3);
                //# }
                //# else if (GameMain.getUIModel().equals(GameMain.ANDROID_LARGE))
                //# {
                //# obtainValue.x = (short)(GameMain.viewWidth - itemWidth * 2 - 15);
                //# }
                //# else
                //# {
                //# obtainValue.x = (short)(GameMain.viewWidth - itemWidth);
                //# }                     
//#else
                obtainValue.x = (short)(GameMain.viewWidth - itemWidth);
//#endif               
                obtainValue.w = (short)itemWidth;
                obtainValue.h = (short)(Utilities.CHAR_HEIGHT + 2);
                obtainValue.frame = 0;
            }
        }
        else{
            obtainValue.frame++;
            if(obtainValue.frame > 30){
            	obtainValue = null;
                /*if(obtainValue.x < GameMain.viewWidth){
                    obtainValue.x += offset;
                }
                else{
                    obtainValue = null;
                }*/
            }
        }
    }

    public void drawPanel(Graphics g, int offset,int low,int high){
    	if(itemList != null){
            int size = itemList.size();

            for(int i = 0; i < size; i++){
                drawItem(g, (GamePanelItem)itemList.elementAt(i),low,high);
            }
    	}
    }
    
    private void drawItem(Graphics g, GamePanelItem item,int low,int high){
    	if(item == null){
        	return ;
        }
        if(item.layer > high || item.layer < low){
        	//#if ModelID == Android || ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad
        	//#  if(item.type != GAME_PANEL_HMSG_BAR && item.type != GAME_PANEL_VMSG_BAR)
        	//#  {
        	//#  	return;
    		//#  }
        	//#elif ModelID == AndroidAuto
        	//# if (!GameMain.getUIModel().equals(GameMain.ANDROID_SMALL))
        	//# {
        	//#  if(item.type != GAME_PANEL_HMSG_BAR && item.type != GAME_PANEL_VMSG_BAR)
        	//#  {
        	//#  	return;
    		//#  }
        	//# }
        	//# else
        	//# {
        	//#	return ;
        	//# }
        	//#else
        	return ;
        	//#endif
        }
        switch(item.type){
            case GAME_PANEL_ITEM_TYPE_ANIMATE_ICON: {
                GameIcon icon = (GameIcon)GameWorld.gameIcons.get(new Integer(item.intData1));
                icon.draw(g, 0, 0);
            }
                break;
            case GAME_PANEL_ITEM_TYPE_IMAGE_ICON: {
                GameIcon icon = (GameIcon)GameWorld.gameIcons.get(new Integer(item.intData1));
                icon.drawImageIcon(g, item.x + (item.w >> 1), item.y + (item.h >> 1));
            }
                break;
            case GAME_PANEL_ITEM_TYPE_IMAGE:
                if(item.intData1 < images.length && item.intData2 < images[item.intData1].getFrameCount()){
                    
                    images[item.intData1].drawFrame(g, item.intData2, item.x, item.y, item.trans, item.anchor);
                }
                break;
            case GAME_PANEL_ITEM_TYPE_OPPOSITE_IMAGE:
                if(item.intData1 < images.length && item.intData2 < images[item.intData1].getFrameCount()){
                    int my = item.y;
                    
                    GamePanelItem hMsgItem = (GamePanelItem)itemTable.get(new Integer(hMessageId));
                    GamePanelItem vMsgItem = (GamePanelItem)itemTable.get(new Integer(vMessageId));
                    
                    if(hMsgItem != null && ((Vector)hMsgItem.objData).size() > 0){
                        my -= hMsgItem.h;
                    }
                    
                    if(vMsgItem != null && ((Vector)vMsgItem.objData).size() > 0){
                        my -= vMsgItem.h;
                    }

                    images[item.intData1].drawFrame(g, item.intData2, item.x, my, item.trans, item.anchor);
                }
                break;
            case GAME_PANEL_ITEM_TYPE_BOX: {
                g.setColor(item.intData1);
                g.drawRect(item.x, item.y, item.w, item.h);
            }
                break;
            case GAME_PANEL_ITEM_TYPE_FILL_BOX: {
                g.setColor(item.intData1);
                g.fillRect(item.x, item.y, item.w, item.h);
            }
                break;
            case GAME_PANEL_ITEM_TYPE_ALPHA_BOX:{
            	Tool.fillAlphaRect(g, item.intData1, item.x, item.y, item.w, item.h);
            }
                break;
            case GAME_PANEL_ITEM_TYPE_LINE: {
                g.setColor(item.intData1);
                g.drawLine(item.x, item.y, item.x + item.w, item.y + item.h);
            }
                break;
            case GAME_PANEL_ITEM_TYPE_STATUS_BAR: {
                g.setColor(item.intData1);
                g.fillRect(item.x, item.y, item.w * item.intData2 / STATUS_BAR_PRECISION, item.h);
            }
                break;
            case GAME_PANEL_ITEM_TYPE_NUM: {
                Tool.drawImageNumber(g, images[item.intData1], item.intData2, (String)item.objData, item.x, item.y, item.trans, item.anchor);
            }
                break;
            case GAME_PANEL_ITEM_TYPE_MINI_ANIMATE: {
                images[item.intData1].drawFrame(g, item.intData2, item.x, item.y, item.trans, item.anchor);
                int[] animatePara = (int[])item.objData;

                if(tick >= animatePara[5]){
                    animatePara[5] = tick + animatePara[2]; //设置下次移动的tick

                    if(animatePara[0] == 0){ //x方向的移动
                        item.x += animatePara[1];

                        if((animatePara[1] > 0 && item.x >= animatePara[4]) || (animatePara[1] < 0 && item.x <= animatePara[4])){ //已到达端点，让移动反向
                            item.x = (short)animatePara[4];
                            animatePara[1] = -animatePara[1]; //步长反向
                            animatePara[4] -= animatePara[3]; //目标位置减去总位移量
                            animatePara[3] = -animatePara[3]; //总位移量反向
                        }
                    }else{ //y方向的移动
                        item.y += animatePara[1];

                        if((animatePara[1] > 0 && item.y >= animatePara[4]) || (animatePara[1] < 0 && item.y <= animatePara[4])){ //已到达端点，让移动反向
                            item.y = (short)animatePara[4];
                            animatePara[1] = -animatePara[1]; //步长反向
                            animatePara[4] -= animatePara[3]; //目标位置减去总位移量
                            animatePara[3] = -animatePara[3]; //总位移量反向
                        }
                    }
                }
            }
                break;
            case GAME_PANEL_ITEM_TYPE_ANIMATE: {
                int[] sequences = (int[])item.objData;
                
                
                if(tick > item.tick + item.intData2){
                    if(item.frame < sequences.length - 1){
                        item.frame++;
                    }
                    else{
                        item.frame = 0;
                    }
                    item.tick = tick;
                }
                //System.out.println("id = "+item.id + "item.intData1 = "+item.intData1 + "   ----  item.intData2 = "+ item.intData2);
                if(item.ready && item.intData1 >= 0 && sequences[item.frame] >= 0){
                    images[item.intData1].drawFrame(g, sequences[item.frame], item.x, item.y, item.trans, item.anchor);
                }
            }
            break;
            case GAME_PANEL_ICON:{
                /* 绘制右侧图标 */
                int[] sequences = (int[])item.objData;
                
                if(tick - item.tick > ICON_TICKER_TIME){
                    item.tick = tick;
                    if(item.frame > 21){
                        item.frame = 0;
                    } else {
                        item.frame++;
                    } 
                }
                
                
                //图标和快捷键交替闪烁
                if(item.intData1 == GAME_PANEL_ACTION_STATE_NON_ACTIVE){
                    if((item.frame >= 0 && item.frame <= 6) || sequences[1] == -1){
                        //sequences[1] == -1 时不用闪烁，用于队友和自身状态
                        //其他情况下交替显示图标和快捷键
                        int frameWidth = images[sequences[2]].getFrameWidth(sequences[3] + sequences[4]);
                        // 画#
                        images[sequences[2]].drawFrame(g, sequences[3] + 10, item.x - frameWidth, item.y + item.h, item.trans, item.anchor);
                        // 画数字
                        images[sequences[2]].drawFrame(g, sequences[3] + sequences[4], item.x, item.y + item.h, item.trans, item.anchor);
                    } else if(item.frame >= 7 && item.frame <= 13){
                    	images[sequences[0]].drawFrame(g, sequences[1], item.x, item.y , item.trans, item.anchor);
                    } else {
                    }
                }
                else{
                    //闪烁快捷键
                    // 画#
                    int frameWidth = 0;
                    if(sequences[1] != -1){
                        images[sequences[0]].drawFrame(g, sequences[1], item.x, item.y + item.h, item.trans, item.anchor);
                        frameWidth = images[sequences[0]].getFrameWidth(sequences[1]);
                    }
                    // 画数字
                    if(item.frame % 2 == 0){
                        images[sequences[2]].drawFrame(g, sequences[3] + sequences[4], item.x - frameWidth, item.y + item.h + 1, item.trans, item.anchor);
                    }
                }
                break;
            }
            case GAME_PANEL_SKILL_ANIMATE:{
                images[item.intData1].drawFrame(g, item.intData2, item.x, item.y, item.trans, Graphics.TOP|Graphics.LEFT);
                int[] arr = (int[])item.objData;
                switch(arr[1]){
                    case GAME_PANEL_SKILL_READY_COLD:{
                        if(item.frame < 2){
                            g.setColor(0xFFFFFF);
                            g.drawRect(item.x, item.y, item.w - 1, item.h - 1);
                            g.drawRect(item.x + 1, item.y + 1, item.w - 3, item.h - 3);
                        }
                        else if(item.frame < 4){
                            g.setColor(0xFFFFFF);
                            g.drawRect(item.x - 1, item.y - 1, item.w + 1, item.h + 1);
                            g.drawRect(item.x, item.y, item.w - 1, item.h - 1);
                        }
                        else if(item.frame < 6){
                            g.setColor(0xFFFFFF);
                            g.drawRect(item.x - 1, item.y - 1, item.w + 1, item.h + 1);
                        }
                        break;
                    }
                    case GAME_PANEL_SKILL_NEED_COLD:{//0xCA000000
                        Tool.fillAlphaRect(g, arr[3], item.x, item.y + item.h - arr[2], item.w, arr[2]);
                        break;
                    }
                }
                break;
            }
            case GAME_PANEL_HMSG_BAR: {
                Vector msgs = (Vector)item.objData;
                Vector colors = (Vector)item.objData2;
                
                if(msgs.size() > 0) {
                	//#if ModelID == Lenovo || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == AndroidAuto
                	//# if(hMsgFont != null) {
                	//# 	g.setFont(hMsgFont);
                	//# }
                	//#endif
                    int clipx = g.getClipX();
                    int clipy = g.getClipY();
                    int clipw = g.getClipWidth();
                    int cliph = g.getClipHeight();
                    g.setClip(item.x, item.y, item.w, item.h);
                    //g.setColor(0x00FF00);
                    //g.drawRect(item.x, item.y, item.w - 1, item.h - 1);
                    
                    Tool.fillAlphaRect(g, item.intData1, item.x, item.y, item.w, item.h);
                    
                    //g.setColor(item.frame);
                    //g.drawRect(item.x - 1, item.y, item.w + 2, item.h - 1);
                    Object ii = msgs.elementAt(0);
                    Vector vec;
                    if (ii instanceof String) {
                    	//#if ModelID == Lenovo || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == AndroidAuto
                    	//# if(hMsgFont != null) {
                    	//# 	vec = Tool.formatString((String)ii, 100000, hMsgFont, true);
                    	//# } else {
                    	//# 	vec = Tool.formatString((String)ii, 100000, Utilities.font, true);
                    	//# } 
                    	//#else
                    	vec = Tool.formatString((String)ii, 100000, Utilities.font, true);
                    	//#endif
                    	msgs.setElementAt(vec, 0);
                    } else {
                    	vec = (Vector)ii;
                    }
                    int color = ((Integer)colors.elementAt(0)).intValue();
                    
                    int strWidth = 0;
                    g.setClip(item.x, item.y, item.w, item.h);
                    //#if ModelID == Lenovo || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == AndroidAuto
                  //# if(hMsgFont != null) {
                  //# 	 strWidth = Tool.drawMixedText(g, vec, item.x + item.w - (tick - item.tick)*HMSG_STEP, item.y + (item.h >> 1) - (hMsgFont.getHeight() >>1), color, 0, false, Tool.G_TOPLEFT, hMsgFont);
                  //# } else {
                  //# 	 strWidth = Tool.drawMixedText(g, vec, item.x + item.w - (tick - item.tick)*HMSG_STEP, item.y + (item.h >> 1) - (Utilities.font.getHeight() >>1), color, 0, false, Tool.G_TOPLEFT);	
                  //# }                   
                    //#else
                    strWidth = Tool.drawMixedText(g, vec, item.x + item.w - (tick - item.tick)*HMSG_STEP, item.y + (item.h >> 1) - (Utilities.font.getHeight() >>1), color, 0, false, Tool.G_TOPLEFT);
                    //#endif
                    
                    if((tick - item.tick)*HMSG_STEP > strWidth + item.w - 20) {
                        msgs.removeElementAt(0);
                        colors.removeElementAt(0);
                        //#if ScreenCanReset == true  
                        Long key = (Long)((Vector)item.objData3).elementAt(0);
                        ((Vector)item.objData3).removeElementAt(0);
                        ((SortHashtable)item.objData4).remove(key);
                        //#endif
                        item.tick = tick;
                    }
                                            
                    g.setClip(clipx, clipy, clipw, cliph);
                    
                	//#if ModelID == Lenovo || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == AndroidAuto
                  //# if(hMsgFont != null) {
                  //# 	g.setFont(Utilities.font);
                  //# }
                	//#endif
                }
                
                break;
            }                   
            case GAME_PANEL_VMSG_BAR: {   
                Vector msgs = (Vector)item.objData;
                Vector colors = (Vector)item.objData2;                  
                
                if(msgs.size() > 0) {
                	//#if ModelID == Lenovo || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == AndroidAuto
                	//# if(vMsgFont != null) {
                	//# 	g.setFont(vMsgFont);
                	//# }
                	//#endif
                    Tool.fillAlphaRect(g, item.intData1, item.x, item.y, item.w, item.h);

                    int clipx = g.getClipX();
                    int clipy = g.getClipY();
                    int clipw = g.getClipWidth();
                    int cliph = g.getClipHeight();
                    g.setClip(item.x, item.y, item.w, item.h);
                    //g.setColor(0x00FF00);
                    //g.drawRect(item.x, item.y, item.w - 1, item.h - 1);
                    
                    //g.setColor(item.frame);
                    //g.drawRect(item.x - 1, item.y, item.w + 2, item.h - 1);
                    Vector str = (Vector)msgs.elementAt(0);
                    int color = ((Integer)colors.elementAt(0)).intValue();
                    
                    if(item.intData2 == VMSG_STATE_STOP) {
                        //停留状态
                    	//#if ModelID == Lenovo || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == AndroidAuto
                    	//# if(vMsgFont != null) {
                    	//#  	Tool.drawMixedText(g, str, item.x, item.y + (item.h >> 2) - (vMsgFont.getHeight() >>1) + 2, color, 0, false, Tool.G_TOPLEFT, vMsgFont);
                    	//# } else {
                    	//# 	Tool.drawMixedText(g, str, item.x, item.y + (item.h >> 2) - (Utilities.font.getHeight() >>1) + 2, color, 0, false, Tool.G_TOPLEFT);
                    	//# }
                    	//#else
                    	Tool.drawMixedText(g, str, item.x, item.y + (item.h >> 2) - (Utilities.font.getHeight() >>1) + 2, color, 0, false, Tool.G_TOPLEFT);
                        //#endif
                        if(msgs.size() > 1) {
                            str = (Vector)msgs.elementAt(1);
                            color = ((Integer)colors.elementAt(1)).intValue();
                        	//#if ModelID == Lenovo || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == AndroidAuto
                          //# if(vMsgFont != null) {
                          //# 	Tool.drawMixedText(g, str, item.x, item.y + item.h/2 + (item.h >> 2) - (vMsgFont.getHeight() >>1) + 2, color, 0, false, Tool.G_TOPLEFT, vMsgFont);
                          //# } else {
                          //# 	Tool.drawMixedText(g, str, item.x, item.y + item.h/2 + (item.h >> 2) - (Utilities.font.getHeight() >>1) + 2, color, 0, false, Tool.G_TOPLEFT);
                          //# }
                        	//#else
                            	Tool.drawMixedText(g, str, item.x, item.y + item.h/2 + (item.h >> 2) - (Utilities.font.getHeight() >>1) + 2, color, 0, false, Tool.G_TOPLEFT);
                            //#endif
                        }
                        if(tick - item.tick > VMSG_STOP_TIME ) {
                            item.intData2 = VMSG_STATE_MOVE;
                            item.tick = tick;
                        }
                    } else {
                        //滚动状态     
                    	//#if ModelID == Lenovo || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == AndroidAuto
                    	//# if(vMsgFont != null) {
                    	//# 	Tool.drawMixedText(g, str, item.x, item.y - (tick - item.tick)*VMSG_STEP + (item.h >> 2) - (vMsgFont.getHeight() >>1) + 2, color, 0, false, Tool.G_TOPLEFT, vMsgFont);
                    	//# } else {
                    	//# 	Tool.drawMixedText(g, str, item.x, item.y - (tick - item.tick)*VMSG_STEP + (item.h >> 2) - (Utilities.font.getHeight() >>1) + 2, color, 0, false, Tool.G_TOPLEFT);	
                    	//# }
                    	//#else
                    	Tool.drawMixedText(g, str, item.x, item.y - (tick - item.tick)*VMSG_STEP + (item.h >> 2) - (Utilities.font.getHeight() >>1) + 2, color, 0, false, Tool.G_TOPLEFT);
                    	//#endif                    	
                        
                        if(msgs.size() > 1) {
                            str = (Vector)msgs.elementAt(1);
                            color = ((Integer)colors.elementAt(1)).intValue();
                        	//#if ModelID == Lenovo || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == AndroidAuto
                          //# if(vMsgFont != null) {
                          //# 	Tool.drawMixedText(g, str, item.x, item.y + item.h/2 - (tick - item.tick)*VMSG_STEP + (item.h >> 2) - (vMsgFont.getHeight() >>1) + 2, color, 0, false, Tool.G_TOPLEFT, vMsgFont);
                          //# } else {
                          //# 	Tool.drawMixedText(g, str, item.x, item.y + item.h/2 - (tick - item.tick)*VMSG_STEP + (item.h >> 2) - (Utilities.font.getHeight() >>1) + 2, color, 0, false, Tool.G_TOPLEFT);	
                          //# }
                        	//#else
                            Tool.drawMixedText(g, str, item.x, item.y + item.h/2 - (tick - item.tick)*VMSG_STEP + (item.h >> 2) - (Utilities.font.getHeight() >>1) + 2, color, 0, false, Tool.G_TOPLEFT);
                            //#endif
                        }
                        if(msgs.size() > 2) {
                            str = (Vector)msgs.elementAt(2);
                            color = ((Integer)colors.elementAt(2)).intValue();
                        	//#if ModelID == Lenovo || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == AndroidAuto
                          //# if(vMsgFont != null) {
                          //# 	Tool.drawMixedText(g, str, item.x, item.y + item.h - (tick - item.tick)*VMSG_STEP + (item.h >> 2) - (vMsgFont.getHeight() >>1) + 2 , color, 0, false, Tool.G_TOPLEFT, vMsgFont);
                          //# } else {
                          //# 	Tool.drawMixedText(g, str, item.x, item.y + item.h - (tick - item.tick)*VMSG_STEP + (item.h >> 2) - (Utilities.font.getHeight() >>1) + 2 , color, 0, false, Tool.G_TOPLEFT);;	
                          //# }
                        	//#else
                            	Tool.drawMixedText(g, str, item.x, item.y + item.h - (tick - item.tick)*VMSG_STEP + (item.h >> 2) - (Utilities.font.getHeight() >>1) + 2 , color, 0, false, Tool.G_TOPLEFT);
                            //#endif
                        }                           
                           
                        if((tick - item.tick)*VMSG_STEP > Utilities.font.getHeight() ) {
                            item.intData2 = VMSG_STATE_STOP;
                            //#if ScreenCanReset == true                            
                            if(msgs.size() > 1) {
                            	if(((SortHashtable)item.objData4).size() > 1) {
                            		Long key = (Long)((Vector)item.objData3).elementAt(0);
                                	Long key2 = (Long)((SortHashtable)item.objData4).getKey(1);
                                	if(key.equals(key2) == false) {
                                		((SortHashtable)item.objData4).remove(key);
                                	}
                            	}                            	
                            } else {
                            	((SortHashtable)item.objData4).clear();
                            }
                            ((Vector)item.objData3).removeElementAt(0);
                            //#endif
                            
                            msgs.removeElementAt(0);
                            colors.removeElementAt(0);                            
                            
                            if(msgs.size() == 0) {
                            	hMessageItem.y = (short) (vMessageItem.y + vMessageItem.h - hMessageItem.h);
                            }
                        }
                    }
                    
                    g.setClip(clipx, clipy, clipw, cliph);
                    
                	//#if ModelID == Lenovo || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == AndroidAuto
                  //# if(vMsgFont != null) {
                  //# 	g.setFont(Utilities.font);
                  //# }
                	//#endif
                }
                break;
            }                   
            case GAME_PANEL_ITEM_COUNTDOWN: {
                int overTime = VMCounter.getSaveTimeSec(((Integer)item.objData).intValue());
                if(overTime < 0){
                    overTime = 0;
                    
                }
                //if(overTime != 0){
                	Tool.drawImageNumber(g, images[item.intData1], item.intData2, String.valueOf(overTime), item.x, item.y, item.trans, item.anchor);
                    int offestX = String.valueOf(overTime).length() * images[item.intData1].getFrameWidth(item.intData2);
                    Tool.draw3DString(g, "'", item.x + offestX, item.y, Tool.CL_WHITE, Tool.CL_BLACK, item.anchor);
                //}
                if(overTime == 0){
                	removeItem(item.id);       
                	sendCommand( VMGame.GAME_COMMAND_PANEL_CLEAR_ROLL, item.objData2);
                }
                break;
            }                   
            case GAME_PANEL_ITEM_OBTAIN: {
                int my = 0;
                GamePanelItem hMsgItem = (GamePanelItem)itemTable.get(new Integer(hMessageId));
                GamePanelItem vMsgItem = (GamePanelItem)itemTable.get(new Integer(vMessageId));
                if(hMsgItem != null && ((Vector)hMsgItem.objData).size() > 0){
                    my = hMsgItem.y - item.h;
                }
                else if(vMsgItem != null && ((Vector)vMsgItem.objData).size() > 0){
                    my = vMsgItem.y - item.h;
                }
                else{
                    my = vMsgItem.y + vMsgItem.h - item.h;
                }
                
                if(item.frame > 1){
                    int w = images[item.intData1].getFrameWidth(item.intData2);
                    images[item.intData1].drawFrame(g, item.intData2, item.x, my, 0, Graphics.BOTTOM | Graphics.LEFT);
                    Tool.draw3DString(g, (String)item.objData, item.x + w, my, ((Integer)item.objData2).intValue(), Tool.CL_BLACK, Graphics.BOTTOM | Graphics.LEFT);
                    if(VM.getApiVersion() > 3 && item.objData3 != null){
                    	int[] dataArr = (int[])item.objData3;
                    	int ix = item.x;
                    	int iy = my + 1;
                    	//画符号
                        if (dataArr[2] > 0) {
                            images[dataArr[0]].drawFrame(g, dataArr[1] + 10, ix + w - (String.valueOf(dataArr[2]).length() * images[dataArr[0]].getFrameWidth(dataArr[1]) + images[dataArr[0]].getFrameWidth(dataArr[1] + 10)), iy, 0, Graphics.BOTTOM | Graphics.LEFT);
                        }
                        //画数字
                        Tool.drawImageNumber(g, images[dataArr[0]], dataArr[1], String.valueOf(dataArr[2]), ix + w, iy, 0, Graphics.BOTTOM | Graphics.RIGHT);
                    }
                }
                else if(item.frame >= 0){
                    g.setColor(Tool.CL_WHITE);
                    g.fillRect(item.x, my - item.h, item.w, item.h);
                }
                
                break;
            }                   
            case GAME_PANEL_VALUE_OBTAIN: {
                int my = 0;
                GamePanelItem hMsgItem = (GamePanelItem)itemTable.get(new Integer(hMessageId));
                GamePanelItem vMsgItem = (GamePanelItem)itemTable.get(new Integer(vMessageId));
                if(hMsgItem != null && ((Vector)hMsgItem.objData).size() > 0){
                    my = hMsgItem.y;
                }
                else if(vMsgItem != null && ((Vector)vMsgItem.objData).size() > 0){
                    my = vMsgItem.y;
                }
                else{
                    my = vMsgItem.y + vMsgItem.h;
                }
                
                if(item.frame > 1){
                    int[] dataArr = (int[])obtainValue.objData;
                    //画图标
                    int ix = item.x;
                    images[item.intData1].drawFrame(g, item.intData2, ix, my, 0, Graphics.BOTTOM | Graphics.LEFT);
                    ix += images[item.intData1].getFrameWidth(item.intData2);
                    if (dataArr[2] >= 0) {
                        //画加号
                        images[dataArr[0]].drawFrame(g, dataArr[1] + 10, ix, my, 0, Graphics.BOTTOM | Graphics.LEFT);
                        ix += images[dataArr[0]].getFrameWidth(dataArr[1] + 10);
                    }
                    //画数字
                    Tool.drawImageNumber(g, images[dataArr[0]], dataArr[1], String.valueOf(dataArr[2]), ix, my, 0, Graphics.BOTTOM | Graphics.LEFT);
                }
                else if(item.frame >= 0){
                    g.setColor(Tool.CL_WHITE);
                    g.fillRect(item.x, my - item.h, item.w, item.h);
                }
                
                break;
            }              
            case GAME_PANEL_LANDMARK:{
            	int my = (int)item.y;
            	//#if ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == Android
            	//# if(landMarkFont != null) {
            	//# 	g.setFont(landMarkFont);
            	//# } 
            	//#elif ModelID == AndroidAuto
            	//# if (!GameMain.getUIModel().equals(GameMain.ANDROID_SMALL))
            	//# {
            	//#  if(landMarkFont != null) {
            	//#  	g.setFont(landMarkFont);
            	//#   } 
            	//# }
            	//# else
            	//# {
                //#  GamePanelItem hMsgItem = (GamePanelItem)itemTable.get(new Integer(hMessageId));
                //#  GamePanelItem vMsgItem = (GamePanelItem)itemTable.get(new Integer(vMessageId));
                //#  if(hMsgItem != null && ((Vector)hMsgItem.objData).size() > 0){
                //#     my = hMsgItem.y - item.h;
                //#  }
                //#  else if(vMsgItem != null && ((Vector)vMsgItem.objData).size() > 0){
                //#     my = vMsgItem.y - item.h;
                //#  }
                //#  else{
                //#     my = vMsgItem.y + vMsgItem.h - item.h;
                //#  }	
            	//# }
                //#else
                GamePanelItem hMsgItem = (GamePanelItem)itemTable.get(new Integer(hMessageId));
                GamePanelItem vMsgItem = (GamePanelItem)itemTable.get(new Integer(vMessageId));
                if(hMsgItem != null && ((Vector)hMsgItem.objData).size() > 0){
                    my = hMsgItem.y - item.h;
                }
                else if(vMsgItem != null && ((Vector)vMsgItem.objData).size() > 0){
                    my = vMsgItem.y - item.h;
                }
                else{
                    my = vMsgItem.y + vMsgItem.h - item.h;
                }
                //#endif
            	g.setColor(item.intData1);
            	if(item.objData != null){
            		Tool.draw3DString(g, (String)item.objData, (int)item.x, my, item.intData1, item.intData2, item.anchor);
            	}
            	//#if ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == Android
            	//# if(landMarkFont != null) {
            	//# 	g.setFont(Utilities.font);
            	//# }
            	//#elif ModelID == AndroidAuto
            	//# if (!GameMain.getUIModel().equals(GameMain.ANDROID_SMALL))
            	//# {
            	//#  if(landMarkFont != null) {
            	//# 	g.setFont(Utilities.font);
            	//#  }
            	//# }
            	//#endif
            }
                break;
            case GAME_PANEL_STRING:{
            	//#if ScreenCanReset == true
            	if(VM.getApiVersion() >= 2) {
            		g.setFont((Font)item.objData3);
            	}
            	//#endif
            	int my = (int)item.y;
            	int[] strData = (int[])item.objData2;
            	if(strData[1] == 1){
            		GamePanelItem hMsgItem = (GamePanelItem)itemTable.get(new Integer(hMessageId));
                    GamePanelItem vMsgItem = (GamePanelItem)itemTable.get(new Integer(vMessageId));
                    if(hMsgItem != null && ((Vector)hMsgItem.objData).size() > 0){
                        my = hMsgItem.y - item.h - strData[4];
                    }
                    else if(vMsgItem != null && ((Vector)vMsgItem.objData).size() > 0){
                        my = vMsgItem.y - item.h - strData[4];
                    }
                    else{
                        my = vMsgItem.y + vMsgItem.h - item.h - strData[4];
                    }    		
            	}
            	
            	if(strData[0] == 1){//draw 3d string
            		Tool.draw3DString(g, (String)item.objData, (int)item.x, my, strData[2], strData[3], item.anchor);
            	} else {//draw 2d string
            		g.setColor(strData[2]);//forground color
            		Tool.drawString(g, (String)item.objData, item.x, item.y, item.anchor);
            	}
            	
            	if(VM.getApiVersion() >= 2) {
            		g.setFont(Utilities.font);
            	}
            }
                break;    
        }
    }

    private void sort(){
        int h, i, j, t;
        GamePanelItem temp = null;
        int n = itemList.size();
        GamePanelItem[] items = new GamePanelItem[n];
        itemList.copyInto(items);

        for(t = 7; t < 17 && GameView.sortTable[t] <= n / 9; t++){
        }

        for(; t >= 0; t--){
            h = GameView.sortTable[t];

            for(i = h; i < n; i++){
                temp = items[i];

                for(j = i - h; j >= 0 && items[j].layer > temp.layer; j -= h){
                    items[j + h] = items[j];
                }

                items[j + h] = temp;
            }
        }

        itemList.removeAllElements();

        for(i = 0; i < n; i++){
            itemList.addElement(items[i]);
        }
    }
    
    /**
     * 创建一个新模板，并返回模板的ID。
     * @return
     */
    public int createTemplate() {
        GamePanelTemplate t = new GamePanelTemplate();
        t.id = templateIDKey.nextKey();
        templateTable.put(new Integer(t.id), t);
        return t.id;
    }
    
    /**
     * 向一个模板中注册一个组件。下面所有的参数中，如果说明是可支持参数引用的，则按下面的规则：
     * 1. 如果属性是整型的，则-1表示第一个整型参数，-2表示第二个整型参数，以此类推
     * 2. 如果属性是对象，则Integer(-1)表示第一个对象参数，Integer(-2)表示第二个对象参数，以此类推。
     * @param tid 模板ID
     * @param type 组件类型
     * @param layer 组件所在层
     * @param trans 翻转（支持参数引用）
     * @param anchor 链接点（支持参数引用）
     * @param x 相对位置
     * @param y 相对位置
     * @param w 宽度（支持参数引用）
     * @param h 宽度（支持参数引用）
     * @param intData1 整数数据1（支持参数引用）
     * @param intData2 整数数据2（支持参数引用）
     * @param objData 对象数据1（支持参数引用）
     * @param objData2 对象数据2（支持参数引用）
     * @param frame 帧号（支持参数引用）
     * @param tick 起始tick，相对当前tick
     */
    public void addTemplateItem(int tid, byte type, int layer, byte trans, byte anchor, short x,
            short y, short w, short h, int intData1, int intData2, Object objData, Object objData2,
            int frame, int tick) {
        GamePanelTemplate t = (GamePanelTemplate)templateTable.get(new Integer(tid));
        GamePanelItem newItem = new GamePanelItem(-1, type);
        newItem.layer = layer;
        newItem.trans = trans;
        newItem.anchor = anchor;
        newItem.x = x;
        newItem.y = y;
        newItem.w = w;
        newItem.h = h;
        newItem.intData1 = intData1;
        newItem.intData2 = intData2;
        newItem.objData = objData;
        newItem.objData2 = objData2;
        newItem.frame = frame;
        newItem.tick = tick;
        GamePanelItem[] arr = new GamePanelItem[t.items.length + 1];
        System.arraycopy(t.items, 0, arr, 0, t.items.length);
        arr[t.items.length] = newItem;
        t.items = arr;
    }
    
    /**
     * 使用模板创建一组组件。
     * @param tid 模板ID
     * @param x 位置
     * @param y 位置
     * @param iparam 整数参数
     * @param oparam 对象参数
     * @return 返回创建的第一个组件的ID。
     */
    public int createWithTemplate(int tid, short x, short y, int[] iparam, Object[] oparam) {
        GamePanelTemplate t = (GamePanelTemplate)templateTable.get(new Integer(tid));
        int len = t.items.length;
        int ret = -1;
        for (int i = 0; i < len; i++) {
            GamePanelItem titem = t.items[i];
            GamePanelItem nitem = new GamePanelItem(idKey.nextKey(), titem.type);
            nitem.templateID = t.id;
            nitem.templateItem = titem;
            configItem(nitem, x, y, iparam, oparam);
            addItem(nitem);
            if (ret == -1) {
                ret = nitem.id;
            }
        }
        return ret;
    }
    
    /**
     * 用新的参数重新配置一组根据模板创建出来的组件。
     * @param tid 模板ID
     * @param firstID 第一个组件的ID
     * @param x 位置
     * @param y 位置
     * @param iparam 整数参数
     * @param oparam 对象参数
     */
    public void reconfigTemplateItems(int tid, int firstID, short x, short y, int[] iparam, Object[] oparam) {
        GamePanelTemplate t = (GamePanelTemplate)templateTable.get(new Integer(tid));
        int minID = firstID;
        int maxID = firstID + t.items.length;
        int size = itemList.size();
        for (int i = 0; i < size; i++) {
            GamePanelItem item = (GamePanelItem)itemList.elementAt(i);
            if (item.id >= minID && item.id < maxID) {
                configItem(item, x, y, iparam, oparam);
            }
        }
    }
    
    /**
     * 删除一组根据模板创建出来的组件。
     * @param tid 模板ID 
     * @param firstID 第一个组件的ID
     */
    public void removeTemplateItems(int tid, int firstID) {
        GamePanelTemplate t = (GamePanelTemplate)templateTable.get(new Integer(tid));
        int minID = firstID;
        int maxID = firstID + t.items.length;
        int size = itemList.size();
        for (int i = size - 1; i >= 0; i--) {
            GamePanelItem item = (GamePanelItem)itemList.elementAt(i);
            if (item.id >= minID && item.id < maxID) {
                itemTable.remove(new Integer(item.id));
                itemList.removeElementAt(i);
            }
        }
        needSort = true;
    }
    
    //#if ScreenCanReset == true
    /**
     * 删除一组根据模板创建出来的组件（删除itemTable中对象，及模版中对象）。
     * @param tid 模板ID 
     * @param firstID 第一个组件的ID
     */
    public void removeTemplateItems2(int tid, int firstID) {
    	removeTemplateItems(tid,firstID);
    	GamePanelTemplate t = (GamePanelTemplate)templateTable.get(new Integer(tid));
        t.items = new GamePanelItem[0];
    }
    //#endif
    
    /*
     * 根据参数和模板数据重新配置一个组件的参数。
     */
    private void configItem(GamePanelItem nitem, short x, short y, int[] iparam, Object[] oparam) {
        GamePanelItem titem = nitem.templateItem;
        nitem.layer = titem.layer;
        if (titem.trans < 0) {
            nitem.trans = (byte)iparam[-1 - titem.trans];
        } else {
            nitem.trans = titem.trans;
        }
        if (titem.anchor < 0) {
            nitem.anchor = (byte)iparam[-1 - titem.anchor];
        } else {
            nitem.anchor = titem.anchor;
        }
        nitem.x = (short)(titem.x + x);
        nitem.y = (short)(titem.y + y);
        if (titem.w < 0) {
            nitem.w = (short)iparam[-1 - titem.w];
        } else {
            nitem.w = titem.w;
        }
        if (titem.h < 0) {
            nitem.h = (short)iparam[-1 - titem.h];
        } else {
            nitem.h = titem.h;
        }
        if (titem.intData1 < 0) {
            nitem.intData1 = iparam[-1 - titem.intData1];
        } else {
            nitem.intData1 = titem.intData1;
        }
        if (titem.intData2 < 0) {
            nitem.intData2 = iparam[-1 - titem.intData2];
        } else {
            nitem.intData2 = titem.intData2;
        }
        if (titem.objData != null && titem.objData instanceof Integer) {
            int index = ((Integer)titem.objData).intValue();
            if (index < 0) {
                nitem.objData = oparam[-1 - index];
            } else {
                nitem.objData = titem.objData;
            }
        } else {
            nitem.objData = titem.objData;
        }
        if (titem.objData2 != null && titem.objData2 instanceof Integer) {
            int index = ((Integer)titem.objData2).intValue();
            if (index < 0) {
                nitem.objData2 = oparam[-1 - index];
            } else {
                nitem.objData2 = titem.objData2;
            }
        } else {
            nitem.objData2 = titem.objData2;
        }
        if (titem.frame < 0) {
            nitem.frame = iparam[-1 - titem.frame];
        } else {
            nitem.frame = titem.frame;
        }
        nitem.tick = titem.tick + tick;
        nitem.ready = true;
    }
}

/**
 * 控件模板。一个模板定义了一组组件。在模板包含的组件数据中，可以通过指定相对位置和参数引用。
 * @author lighthu
 */
class GamePanelTemplate {
    int id;
    GamePanelItem[] items = new GamePanelItem[0];
}

class GamePanelItem {
    int id;
    /** 模板ID，-1表示没有模板 */
    int templateID = -1;
    /** 对应的模板项，null表示没有 */
    GamePanelItem templateItem;

    public byte type;
    public int layer;
    public byte trans;
    public byte anchor;

    public short x;
    public short y;
    public short w;
    public short h;
    
    /** 图片id */
    public int intData1;
    /** 图片中的索引位置 */
    public int intData2;
    /** 在动画状态时存储动画数据 */
    public Object objData;
    /**   每个条目的颜色数据 */
    public Object objData2;
    public Object objData3;
    //#if ScreenCanReset == true
    public Object objData4;
    //#endif
    /** 当前动画播放的frame */
    public int frame;
    /** 当前动画播放的tick */
    public int tick;
    
    /** 数据是否就绪 */
    public boolean ready = false;

    public GamePanelItem(int id, byte type){
        this.id = id;
        this.type = type;
    }
}

class GamePanelPointItem{
    int x;
    int y;
    int w;
    int h;
    int notifyId;
    int notifyData;
    boolean opposite;
    boolean effect;
}