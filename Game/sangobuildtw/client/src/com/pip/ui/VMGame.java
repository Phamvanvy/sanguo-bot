package com.pip.ui;


import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Graphics;

import com.pip.common.Tool;
import com.pip.common.Utilities;
import com.pip.gui.GContainer;
import com.pip.gui.GWidget;
import com.pip.gui.GWindow;
import com.pip.io.UASegment;
import com.pip.io.UASocketConnection;
import com.pip.resource.ResourceManager;
import com.pip.sanguo.GameMain;
import com.pip.sanguo.GameWorld;
import com.pip.util.SortHashtable;

public class VMGame{
    public static final short CONN_VM_DATA = -10000;
    public static final short CONN_VM_COMMAND = -10001;
    
    //从java发起调用Sprite控制机
    public static final int GAME_COMMAND_CREATE_SPRITE = 10005;
    public static final int GAME_COMMAND_SPRITE_LOAD_ANIMATE = 10010;
    public static final int GAME_COMMAND_SPRITE_ANIMATE_OK = 10020;
    public static final int GAME_COMMAND_SPRITE_CHANGED = 10030; //阵营改变
    public static final int GAME_COMMAND_SPRITE_SET_ANIMATE = 10040;
    public static final int GAME_COMMAND_SPRITE_ANIMATE_PLAY_END = 10050;
    public static final int GAME_COMMAND_SPRITE_PLAY_ATTACK = 10060;
    public static final int GAME_COMMAND_SPRITE_PLAY_PRE_ATTACK = 10061;
    public static final int GAME_COMMAND_SPRITE_PLAY_ATTACKED = 10070;
    public static final int GAME_COMMAND_SPRITE_PLAY_DIE = 10080;
    public static final int GAME_COMMAND_SPRITE_GET_UNIT_INFO = 10090;
    public static final int GAME_COMMAND_SPRITE_TARGETED = 10100;
    public static final int GAME_COMMAND_SPRITE_REFRESH_HEAD_STRING = 10110;
    public static final int GAME_COMMAND_SPRITE_CHASE_END = 10120;  //正常终止
    public static final int GAME_COMMAND_SPRITE_CHASE_STOP = 10121; //被其他操作中断(方向键和指针)
    public static final int GAME_COMMAND_SPRITE_PLAY_ATTACK_FAIL = 10130;
    public static final int GAME_COMMAND_SPRITE_FIRE = 10140;
    public static final int GAME_COMMAND_SPRITE_START_CHASE = 10150;
    public static final int GAME_COMMAND_SPRITE_CANCEL_ATTACK = 10160;
    public static final int GAME_COMMAND_SPRITE_PENDING_STOP = 10170;
    public static final int GAME_COMMAND_SPRITE_DESTROY = 10180;
    public static final int GAME_COMMAND_SPRITE_LOAD_HOSRE_ANIMATE = 10190; //载入坐骑图片
    
    //从java发起调用world控制机
    public static final int GAME_COMMAND_WORLD_SYNC_TARGET = 14020;
    public static final int GAME_COMMAND_WORLD_QUEST_ACTION = 14030;
    public static final int GAME_COMMAND_WORLD_PLAYER_OUT_VIEW = 14040;
    public static final int GAME_COMMAND_WORLD_PLAYER_IN_VIEW = 14041;
    public static final int GAME_COMMAND_WORLD_POINT_PRESSED = 14042;
    public static final int GAME_COMMAND_WORLD_POINT_RELEASED = 14043;
    public static final int GAME_COMMAND_WORLD_POINT_DRAGGED = 14044;

    //从java发起调用游戏面板控制机
    public static final int GAME_COMMAND_PANEL_INIT = 15010;
    public static final int GAME_COMMAND_PANEL_CHANGE_TARGET = 15020;
    public static final int GAME_COMMAND_PANEL_SYNC_CD = 15030;
    public static final int GAME_COMMAND_ROLE_MOVE = 15040;
    public static final int GAME_COMMAND_PANEL_REFRESH_TARGET = 15050;
    public static final int GAME_COMMAND_PANEL_CLEAR_ROLL = 15060;

    //在vmData里临时存储AnimatePlayer的key前缀
    public static final String VM_DATA_PREFIX_ANIMATE = "vmDataAnimate";

    //java回掉控制机的回掉函数
    public static final String CALLBACK_GAME_COMMAND = "GameCommand";
    public static final String CALLBACK_SCREEN_SIZE_CHANGED = "ScreenSizeChanged";

    //任务相关
    public static final String CALLBACK_QUEST_TOUCH_NPC = "QuestTouchNpc";
    public static final String CALLBACK_PQE_CHAT = "QuestChat";
    public static final String CALLBACK_PQE_MESSAGE = "QuestMessage";
    public static final String CALLBACK_PQE_QUESTION = "QuestQuestion";

    public static final String CALLBACK_LOAD_ETF_START = "LoadEtfStart";
    public static final String CALLBACK_LOAD_ETF_END = "LoadEtfEnd";
    public static final String CALLBACK_LOAD_ETF_END1 = "LoadEtfEnd1";
    public static final String CALLBACK_DIS_CONNECTED = "DisConnected";
    public static final String CALLBACK_UNIT_MOVE_FORTH = "UnitMoveForthChanged";
    public static final String CALLBACK_UNIT_MOVE_FIFTH = "UnitMoveFifthChanged";
    public static final String CALLBACK_CHANGED_ON_HORSE = "ChangedOnHorse";
    public static final String CALLBACK_REFRESH_TEAM_PANEL = "RefreshTeamPanel";
    public static final String CALLBACK_LOADING_FINISH = "LoadingFinish";
    public static final String CALLBACK_VMGAME_DESTROY_NOTIFY = "VMGameDestroyNotify";
    public static final String CALLBACK_GAME_WORLD_ADD_GAME_EVENT = "GameWorldAddGameEvent";
    public static final String CALLBACK_GAME_WORLD_GET_VMGAME_KEY = "GameWorldGetVMGame";
    //
    public static final String CALLBACK_QUEST_GET_ITEM_COUNT = "QuestGetItemCount";

    //由vm传过来的UASegment包中，应该按照那种数据类型读取
    public static final int VM_UASEGMENT_TYPE_INT = 0;
    public static final int VM_UASEGMENT_TYPE_STRING = 1;

    /**     对于界面vm需要一个有序的hashtable */
    private static SortHashtable sortHt = new SortHashtable();

    public static final byte VM_TYPE_GAME = 0;
    public static final byte VM_TYPE_UI = 1;
    public static final byte VM_TYPE_LIB = 2;

    /** 空闲：没有UI等待装载。 */
    public static final byte STATE_IDLE = -1;
    /** 正在请求UI：装载UI的线程正在运行 */
    public static final byte STATE_REQUESTING_VMUI = 1;
    /** 当前装载状态 */
    public static byte state = STATE_IDLE;

    /** 等待装载的脚本的ID */
    private static Hashtable loadingVMID = new Hashtable();

    /** 脚本启动参数，主要用在服务器push脚本上，已脚本名字为key存储服务器下发的参数，脚本读取后则删除 */
    private static Hashtable vmparams = new Hashtable();
    
    /**
     * 常用脚本cache
     */
    private static Hashtable vmCache = new Hashtable();

    private byte vmType = VM_TYPE_GAME;
    private String vmId;
    private Integer vmKey;
    private static Tool keyMaker = new Tool();
    private boolean isSingleton;  //是否为单例(不允许多次载入)
    public static int gameWorldVMGameKey;
    
    /** 附着的VM对象 */
    public VM gtvm;

    private SortHashtable vmContainers = new SortHashtable();

    /** 是否透明 */
    private boolean transparent;
    /** 是否已关闭 */
    private boolean closed;
    /** 是否截获输入 */
    private boolean catchInput;

    private int oldVMsgPanelY = -1000;
    
    /**  存放所有的GWidget对象 */
    private static Hashtable gWidgets = new Hashtable();
    /**  存放本脚本内的GWidget对象， 便于脚本关闭时释放剩余的GWidget */
    private Hashtable vmGWidgets = new Hashtable();
    
    public static void clear(){
        Object[] vgs = (Object[]) sortHt.values();

        if(vgs != null){
            for(int i = 0; i < vgs.length; i++){
            	((VMGame) vgs[i]).vmGWidgets.clear();
                ((VMGame) vgs[i]).gtvm.destroy();
            }
        }

        loadingVMID.clear();
        sortHt.clear();
        vmCache.clear();
        VM.globalVMData.clear();
        gWidgets.clear();
    }
    
    public static void clearExclude(String excludeVmId){
        Object[] vgs = (Object[]) sortHt.values();

        if(vgs != null){
            for(int i = 0; i < vgs.length; i++){
            	VMGame vmGame = (VMGame) vgs[i];
            	if(excludeVmId.equals(vmGame.vmId) == false) {
            		vmGame.vmGWidgets.clear();
            		vmGame.gtvm.destroy();
                    
                    sortHt.remove(vmGame.vmKey);
        		} else {
        			int ii = 0;
        			ii = 0;
        		}
            }
        }
        
        loadingVMID.clear();
        vmCache.clear();
    }

    public void putGWidget(GWidget gWidget) {
    	if(gWidget != null) {
    		Integer _key = new Integer(gWidget.vmData[GWidget.GW_VM_JAVA_GWIDGET]);
        	gWidgets.put(_key, gWidget);
        	vmGWidgets.put(_key, gWidget);
    	}    	
    }
    
    public void removeGWidget(GWidget gWidget) {
    	if(gWidget != null) {
    		Integer _key = new Integer(gWidget.vmData[GWidget.GW_VM_JAVA_GWIDGET]);
        	gWidgets.remove(_key);
        	vmGWidgets.remove(_key);
    	}    	
    }
    
    public byte getVMType(){
        return vmType;
    }

    public String getVMId(){
        return vmId;
    }

    public GWindow createWindow(int vmObj, int[] vmData, boolean isTransparent, String name){
        GWindow window = new GWindow(this, vmObj, vmData, isTransparent, name);
        vmContainers.put(window, window);
        window.isShow = false;
        return window;
    }

    public void vmDestroyWindow(GWindow gWindow){
        vmContainers.remove(gWindow);
        gWindow.destroy();
    }

    public void vmCloseWindow(GWindow gWindow){
    	gWindow.isShow = false;
    }

    //把一个container添加到另一个container中
    public void vmContainerAdd(GContainer parent, GWidget child){
        parent.add(child);
    }
    
    public void vmContainerInsert(GContainer parent, GWidget child, int index){
        parent.insert(child, index);
    }
    
    public void vmContainerDel(GContainer parent, GWidget child){
        parent.remove(child);
    }

    public void vmShowWindow(GWindow gWindow){
    	vmContainers.remove(gWindow);
        vmContainers.put(gWindow, gWindow);
        gWindow.isShow = true;
    }

    public SortHashtable getGWindows() {
    	return vmContainers;
    }
    
    public void addCommonCallback(int type, GWindow gWindow, String funcName){
        switch(type){
            case VM.CYCLE:
            	gWindow.funcCycle = funcName;
                break;
            case VM.CYCLEUI:
            	gWindow.funcCycleUI = funcName;
                break;
            case VM.PAINT:
            	gWindow.funcPaint = funcName;
                break;
            case VM.PROCESSPACKET:
            	gWindow.funcPacket = funcName;
                break;
        }
    }

    public void removeCommonCallback(int type, GWindow gWindow){
        switch(type){
	        case VM.CYCLE:
	        	gWindow.funcCycle = null;
	            break;
	        case VM.CYCLEUI:
	        	gWindow.funcCycleUI = null;
	            break;
	        case VM.PAINT:
	        	gWindow.funcPaint = null;
	            break;
	        case VM.PROCESSPACKET:
	        	gWindow.funcPacket = null;
	            break;
	    }
    }

    public void processCommonCallback(int type){
        int containerCount = vmContainers.size();

        if(containerCount > 0){
            Object[] values = vmContainers.values();
            int firstIndex = 0;

            // 从最上层开始向下查找第一个不透明的UI
            int count = vmContainers.size();
            firstIndex = count - 1;
            while(firstIndex >= 0){
                GWindow container = (GWindow) vmContainers.values()[firstIndex];
                if(container == null){
                    break;
                }
                if(type == VM.PROCESSPACKET || container.isTransparent() || container.vmData[GWidget.GW_VM_FUNC_PAINT] == 0){
                    firstIndex--;
                }else{
                    break;
                }
            }

            // 从下向上执行
            if(firstIndex < 0){
                firstIndex = 0;
            }

            if(type == VM.CYCLEUI) {
				for(int i = containerCount - 1; i >= firstIndex; i--){
					GWindow gWindow = (GWindow) values[i];
					handleCaller(gWindow, type);
					if(gWindow.isShow && gWindow.catchInput) {
						Utilities.keyFlag2 = 0;
					}
				}
            } else {
            	for(int i = firstIndex; i < containerCount; i++){
                    handleCaller((GWindow) values[i], type);
                }
            }
          
        }
    }
    
    private void handleCaller(GWindow gWindow, int type) {
        if(gWindow == null || ((type == VM.CYCLE || type == VM.CYCLEUI || type == VM.PAINT) && gWindow.isShow == false)){
            return;
        }        
        gWindow.handleCaller(type, gtvm.blocked);
    }
    
    public static void setToTop(String vmId){
    	VMGame vg = getLastVMGame(vmId);
    	if(vg==null){
    		return;
    	}
    	deleteVMGame(vg.vmKey);
    	addVMGame(vg);
    }
    private static void addVMGame(VMGame vg){
        sortHt.put(vg.vmKey, vg);
    }

    private static void deleteVMGame(Integer vmKey){
    	sortHt.remove(vmKey);      
    }

    private void vmCycle(){
        if(closed){
            return;
        }

        if(gtvm != null){
            processCommonCallback(VM.CYCLE);
            gtvm.execute(VM.CYCLE);
        }
    }

    public static void cycle(){
        if(state == STATE_REQUESTING_VMUI){
            Enumeration enu = loadingVMID.keys();
            Hashtable rest = new Hashtable();
            
            while(enu.hasMoreElements()){
                String id = (String)enu.nextElement();
                Long startTime = (Long)loadingVMID.get(id);
                
                if(System.currentTimeMillis() - startTime.longValue() < UASocketConnection.TIME_SEND_TIMEOUT){
                    rest.put(id, startTime);
                }
            }
            
            loadingVMID = rest;
            checkLoading();
            
            return;
        }

        // 处理所有打开的界面
        int vmcount = sortHt.size();

        if(state == STATE_IDLE && vmcount > 0){
            // 只有最顶端的UI可以得到机会执行cycleUI，并可以接受键盘消息，其他UI只能执行cycle。如果UI设置catchInput为false则不会清除键盘状态
            for(int i = vmcount - 1; i >= 0; i--){
                VMGame mn = (VMGame) sortHt.values()[i];

                switch(mn.vmType){
                    case VM_TYPE_GAME:
                        mn.vmCycle();
                        break;
                    case VM_TYPE_UI:
                        try{
                            mn.vmCycle();
                        }catch(Exception e){
                        	//#ifdef buildtest
                            System.out.println("error ui : " + mn.getVMId() + "e=" + e);
                          //#endif
                            
                            mn.close();
                        }
                        if(isTopUI(mn.vmId)){
                            try{
                                mn.cycleUI();
                            }catch(Exception e){
                                mn.close();
                            }

                            if(mn.isCatchInput()){
                                Utilities.keyFlag2 = 0;
                            }
                        }
                        if(mn.isClosed()){
                            mn.destroy();
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * 是否是最上面的UI
     * @param vmId
     * @return
     */
    private static boolean isTopUI(String vmId){
    	VMGame vg = getLastVMGame(vmId);
        int vmcount = sortHt.size();
        int startIndex = sortHt.index(vg.vmKey);
        if(startIndex >= 0){
            for(int i = startIndex + 1; i < vmcount; i++){
                VMGame mn = (VMGame) sortHt.values()[i];
                if(mn.vmType == VM_TYPE_UI && mn.isCatchInput()){
                    return false;
                }
            }
        }else{
            return false;
        }

        return true;
    }

    /**
     * 启动一个UI，此方式主要用于服务器push脚本，push的时候服务器会随带添加启动参数
     * @param vmId 脚本名
     * @param param 参数字符串
     */
    public static int openUI(String vmId, Object param){
        vmparams.put(vmId, param);
        return loadVMGame(vmId, VM_TYPE_UI, true);
    }

    /**
     * 供脚本调用，读取指定脚本的参数，读取并删除
     * @param vmId 脚本名
     * @return 参数
     */
    public static Object getVMParam(String vmId){
        return vmparams.remove(vmId);
    }

    public void cycleUI(){
        if(closed){
            return;
        }

        if(gtvm != null){
            processCommonCallback(VM.CYCLEUI);
            gtvm.execute(VM.CYCLEUI);
        }
    }

    public int callback(String funcName, Object[] data){
        VM vm = getVMGame(vmId).gtvm;

        if(vm != null){
            synchronized(vm){
                int[] params = null;
                if(data != null){
                    params = new int[data.length];
    
                    for(int i = 0; i < data.length; i++){
                        if(data[i] instanceof Integer){
                            params[i] = ((Integer) data[i]).intValue();
                        }else{
                            params[i] = vm.makeTempObject(data[i]);
                        }
                    }
                }
    
                return vm.callback(funcName, params);
            }
        }else{
            return -1;
        }
    }
    
    public static VMGame getVMGameByVMKey(int vmKey){
    	return (VMGame)sortHt.get(new Integer(vmKey));
    }

    public static VMGame getVMGame(String vmId){
    	Object[] vgs = (Object[]) sortHt.values();
    	for(int i=0; i<vgs.length; i++) {
    		if(((VMGame)vgs[i]).vmId.equals(vmId)) {
    			return (VMGame)vgs[i];
    		}
    	}
    	
        return null;
    }
    
    public static VMGame getLastVMGame(String vmId){
    	Object[] vgs = (Object[]) sortHt.values();
    	VMGame ret = null;
    	for(int i=0; i<vgs.length; i++) {
    		if(((VMGame)vgs[i]).vmId.equals(vmId)) {
    			ret = (VMGame)vgs[i];
    		}
    	}
    	
        return ret;
    }

    public VM getVM(){
        return gtvm;
    }
    
    /**
     * 创建一个ETF脚本驱动的VMUI对象。
     * @param id
     * @param data
     * @param etdData
     * @throws Exception
     */
    private VMGame(String id, byte[] data, byte[] etdData, byte vmType) throws Exception{
        vmId = id;
        this.vmType = vmType;
        if(vmType == VM_TYPE_UI){
            catchInput = true;            
        }

        //TODO delete by jy
        //etdData = null;

        //#ifdef buildtest
        if(etdData != null){
            gtvm = new DebugVM(this);
            ((DebugVM) gtvm).init(data, etdData);
            gtvm.link();
            this.vmKey = new Integer(keyMaker.nextKey());
            this.isSingleton = true;
            if("game_world".equals(this.vmId)) {
            	gameWorldVMGameKey = this.vmKey.intValue();
            }
            return;
        }
        //#endif

        gtvm = new VM(this);
        gtvm.init(data);
        gtvm.link();
        
        this.vmKey = new Integer(keyMaker.nextKey());
        this.isSingleton = true;
        
        if("game_world".equals(this.vmId)) {
        	gameWorldVMGameKey = this.vmKey.intValue();
        }
    }

    public static int loadVMGame(String vmId, byte vmType, boolean sync){
        VMGame result = getVMGame(vmId);
        
        //如果已经载入，先卸载
        if(result != null && result.isSingleton){
        	result.close();
        	result.destroy();
        }

        //本地已经cache
        byte[] etfData = (byte[])vmCache.get(vmId);
        
        int ret = 0;
        if(etfData == null){        	
            etfData = GameMain.resourceManager.findResource(vmId + ResourceManager.POSTFIX_ETF);

            switch(vmType){
                case VM_TYPE_GAME:
                    if(etfData != null){
                        try{
                        	ret = addUI(vmId, Tool.inflate(etfData), VM_TYPE_GAME);
                        }catch(Exception e){
                        	//#ifdef buildtest
                            e.printStackTrace();
                          //#endif
                        }
                    }
                    break;
                case VM_TYPE_UI:
                    if(etfData != null){
                        try{
                        	ret = addUI(vmId, Tool.inflate(etfData), VM_TYPE_UI);
                        }catch(Exception e){
                        	//#ifdef buildtest
                            e.printStackTrace();
                          //#endif
                        }
    
                        return ret;
                    }
    
                    GameMain.resourceManager.requestResource(vmId + ResourceManager.POSTFIX_ETF);
                    loadingVMID.put(vmId, new Long(System.currentTimeMillis()));
    
                    if(state == STATE_IDLE){
                        state = STATE_REQUESTING_VMUI;
                        VM vm = VMGame.getVMGame("game_world").getVM();
                        
                        synchronized(vm){
                            vm.callback(CALLBACK_LOAD_ETF_START, new int[]{vm.makeTempObject(vmId)});
                        }
                    }
                    break;
                default:
                    break;
            }            
        }else{
            switch(vmType){
                case VM_TYPE_GAME:
                    try{
                    	ret = addUI(vmId, etfData, VM_TYPE_GAME);
                    }catch(Exception e){
                    	//#ifdef buildtest
                        e.printStackTrace();
                      //#endif
                    }
                    break;
                case VM_TYPE_UI:
                    try{
                    	ret = addUI(vmId, etfData, VM_TYPE_UI);
                    }catch(Exception e){
                    	//#ifdef buildtest
                        e.printStackTrace();
                      //#endif
                    }
                    break;
            }            
        }
        
        return ret;
    }

    public static void recvEtfData(String vmId, byte[] etfData){
    	int etfVMKey = 0;
        try{
        	etfVMKey = addUI(vmId, Tool.inflate(etfData), VM_TYPE_UI);
        	VMGame gameWorld = VMGame.getVMGame("game_world");
        	if(gameWorld != null) {
        		 VM vm = gameWorld.getVM();                                
                 synchronized(vm){
                     vm.callback(VMGame.CALLBACK_LOAD_ETF_END1, new int[]{vm.makeTempObject(vmId), etfVMKey});
                 }
        	}
        }catch(Exception e){
        	//#ifdef buildtest
            e.printStackTrace();
          //#endif
        }

        loadingVMID.remove(vmId);
        checkLoading();
    }
    
    private static void checkLoading(){
        if(loadingVMID.size() == 0){
            state = STATE_IDLE;

            VM vm = VMGame.getVMGame("game_world").getVM();
            
            synchronized(vm){
                vm.callback(CALLBACK_LOAD_ETF_END, null);
            }
        }
    }

    /**
     * 在界面顶层添加一个UI。
     * @param uiID 启动文件名或者脚本名称
     * @param etfData 脚本数据
     * @throws Exception
     */
    private static int addUI(String vmId, byte[] etfData, byte vmType) throws Exception{
        if(GameMain.needCacheVm.containsKey(vmId)){
            vmCache.put(vmId, etfData);
        }
        
        // TODO：暂时从本地载入所有的库
        {
            VM tmpVM = new VM(null);
            tmpVM.loadETF(etfData);
            for (int i = 0; i < tmpVM.libNames.length; i++) {
                String libName = tmpVM.libNames[i];
                if (getVMGame(libName) != null) {
                    continue;
                }
                byte[] tmpData = GameMain.resourceManager.findResource(libName + ResourceManager.POSTFIX_ETF);
                addUI(libName, Tool.inflate(tmpData), VM_TYPE_LIB);
            }
        }
        
        String etdFile;

        //#ifdef buildtest
        etdFile = vmId + "_" + GameMain.getUIModel() + ".etd";
        byte[] etdData = Tool.loadLocalResource(etdFile);
        VMGame mn = new VMGame(vmId, etfData, etdData, vmType);
        etdData = null;
        //#else
        //# VMGame mn = new VMGame(vmId, etfData, null, vmType);
        //#endif
        
        etfData = null;

        if (vmType != VM_TYPE_LIB) {
            // 库不需要执行INIT
            mn.gtvm.execute(VM.INIT);
        }

        //面板中消息的位置
        if(vmType == VM_TYPE_UI && GameWorld.panel != null && !mn.transparent) {
        	mn.oldVMsgPanelY = GameWorld.panel.getMsgPanelY();
        	if(mn.oldVMsgPanelY > 0) {
        		GameWorld.panel.setMsgPanelY(GameMain.viewHeight);	
        	}
        }
        
        addVMGame(mn);

        if(vmType == VM_TYPE_UI){
            Quest.setEventMask(Quest.EVENT_MASK_OPENUI);
        }
        
        return mn.vmKey.intValue();
    }

    public static void removeVMGame(String vmId){
    	VMGame vg = getLastVMGame(vmId);

        if(vg != null){
            vg.gtvm.execute(VM.DESTROY);
            vg.gtvm.destroy();
        	sortHt.remove(vg.vmKey);
        }
    }

    /**
    * 处理网络包。
    * @param segment
    */
    public static void handleSegment(UASegment segment){
        Object[] vgs = (Object[]) sortHt.values();

        if(vgs != null){
        	//提高命中速度
            for(int i = vgs.length - 1; i >= 0 ; i--){
                VMGame vg = (VMGame) vgs[i];

                if(vg.vmType != VM_TYPE_LIB && !vg.closed){
                    segment.reset();
                    vg.gtvm.execute(VM.PROCESSPACKET);

                    if(segment.handled){
                        return;
                    }

                    vg.processCommonCallback(VM.PROCESSPACKET);

                    if(segment.handled){
                        return;
                    }
                }
            }
        }

    }

    public void draw(Graphics g){
        if(closed){
            return;
        }

        //#if ScreenCanReset == true
        if(!transparent && !GWindow.forcePaintWorld){
            drawDefaultBackground(g);
        }
        //#else
        //# if(!transparent){
        //#    drawDefaultBackground(g);
        //# }
        //#endif
        if(gtvm != null){
            gtvm.execute(VM.PAINT);
            processCommonCallback(VM.PAINT);
        }       
        
    }

    /**
     * 绘制所有VMUI。
     * @param g
     */
    public static void drawAll(Graphics g,int low,int high){
        // 从最上层开始向下查找第一个不透明的UI
        int uiCount = sortHt.size();
        int firstIndex = uiCount - 1;
        while(firstIndex >= 0){
            VMGame mn = (VMGame) sortHt.values()[firstIndex];
            if(mn == null){
                break;
            }
            if(mn.isTransparent() || mn.vmType != VM_TYPE_UI){
                firstIndex--;
            }else{
                break;
            }
        }

        // 从下向上绘制UI
        if(firstIndex < 0){
            firstIndex = 0;
        }
        while(firstIndex < uiCount){
            VMGame mn = (VMGame) sortHt.values()[firstIndex];
            if(mn.vmType == VM_TYPE_UI){
                mn.draw(g);
            }else if (mn.vmType == VM_TYPE_GAME){
                mn.gtvm.execute(VM.PAINT);
            }
            firstIndex++;
        }
        
    }

    public static void drawDefaultBackground(Graphics g){
        Tool.drawBack(g, 0, 0, GameMain.viewWidth, GameMain.viewHeight);
    }

    /**
     * 关闭所有UI。
     * @param refUI 参考UI
     * @param type 如果为1，则只关闭指定UI之上的UI
     */
    public static void closeAllUI(VMGame refUI, int type){
        Object[] vgs = (Object[]) sortHt.values();

        if(vgs != null){
            for(int i = 0; i < vgs.length; i++){
                VMGame vg = (VMGame) vgs[i];

                if(vg.getVMType() == VM_TYPE_UI){
                    if(type == 1 && vg == refUI){
                        break;
                    }
                    vg.close();
                }
            }
        }
    }

    /**
     * 关闭一个指定的VM
     */
    public static void closeVM(String vmId) {
    	VMGame vg = getVMGame(vmId);
    	
    	if(vg != null) {
    		vg.close();
    	}

    }
    
    public static void closeVM(int vmKey) {
    	VMGame vg = (VMGame)sortHt.get(new Integer(vmKey));
    	if(vg != null) {
    		vg.close();
    	}
    }
    
    /**
     * 判断当前是否已经或者正在打开某个UI。
     */
    public static boolean hasUI(String uiID){
        VMGame vg = getVMGame(uiID);
        if(vg.getVMId().equals(uiID) && !vg.isClosed()){
            return true;
        }

        if(loadingVMID.containsKey(uiID)){
            return true;
        }

        return false;
    }

    public void close(){
        closed = true;
        
        if(vmType == VM_TYPE_UI && GameWorld.panel != null && !transparent && oldVMsgPanelY > 0) {
        	GameWorld.panel.setMsgPanelY(oldVMsgPanelY);	
        }
        
    }

    public boolean isClosed(){
        return closed;
    }

    public boolean isTransparent(){
        return transparent;
    }

    /**
     * 判断所有UI是否都是透明的。
     * @return
     */
    public static boolean isAllTransparent(boolean chectCatchInput){
        Object[] vgs = (Object[]) sortHt.values();

        if(vgs != null){
            for(int i = 0; i < vgs.length; i++){
                VMGame vg = (VMGame) vgs[i];

                if(vg.vmType == VM_TYPE_UI && (!vg.isTransparent() || (chectCatchInput && vg.isCatchInput()))){
                    return false;
                }
            }
        }

        return true;
    }

    public void setTransparent(boolean transparent){
        this.transparent = transparent;
                
    }
    
    public void setSingleton(boolean isSingleton){
        this.isSingleton = isSingleton;
                
    }
    
    public boolean getSingleton(){
        return isSingleton;                
    }
    
    public boolean isCatchInput(){
        return catchInput;
    }

    public void setCatchInput(boolean catchInput){
        this.catchInput = catchInput;
    }

    public void destroy(){
        if(gtvm != null){
            gtvm.execute(VM.DESTROY);
            gtvm.destroy();
            gtvm = null;
            deleteVMGame(this.vmKey);
            
            VMGame gvm = VMGame.getVMGameByVMKey(VMGame.gameWorldVMGameKey);
            if(gvm != null) {
        		VM vm = gvm.getVM();
        		synchronized(vm){
        			vm.callback(VMGame.CALLBACK_VMGAME_DESTROY_NOTIFY,
                        new int[]{ 
        					this.vmKey.intValue(), vm.makeTempObject(gvm), vm.makeTempObject(this.vmId), this.vmType
                        });
                }
        		
        		Enumeration emu = vmGWidgets.keys();

                while(emu.hasMoreElements()){
                    Integer _key = (Integer)emu.nextElement();
                    gWidgets.remove(_key);                    
                }
                vmGWidgets.clear();
            }
        }
    }
    
    public static SortHashtable getVMGames() {
    	return sortHt;
    }
    
    public static int getCommonKey() {
    	return keyMaker.nextKey();
    }
    
//    public static GWindow selGWindow;
//    public static void pointerPressed(int x, int y) {
//    	selGWindow = getTopGWindow();
//    	if(selGWindow != null) {
//    		selGWindow.pressWidget = getPointerWidget(selGWindow, x, y);	
//    	}
//    }
    
//    public static void pointerReleased(int x, int y) {
//    	if(selGWindow != null) {
//        	if(selGWindow.pressWidget != null) {
//        		GWidget releaseWidget = getPointerWidget(selGWindow, x, y);
//            	if(selGWindow.pressWidget == releaseWidget) {    		
//            		selGWindow.pressWidget.mouseClicked(selGWindow.getVM());
//            	}
//        	}
//        	selGWindow.pressWidget = null;
//        	selGWindow = null;
//    	}
//    	
//    }
    
    
    /**
     * 获得顶层ui脚本id
     * @return
     */
    public static String getTopUIVMId() {
        int vmcount = sortHt.size();
        for(int i = vmcount - 1; i >= 0; i--){
            VMGame mn = (VMGame) sortHt.values()[i];
            if(mn.vmType == VM_TYPE_UI){
                return mn.vmId;
            }
        }

        return null;
    }
    
    //#if ScreenCanReset == true
    /**
     * 获得顶层ui脚本
     * @return
     */
    public static VMGame getTopUIVM() {
        int vmcount = sortHt.size();
        for(int i = vmcount - 1; i >= 0; i--){
            VMGame mn = (VMGame) sortHt.values()[i];
            if(mn.vmType == VM_TYPE_UI){
                return mn;
            }
        }

        return null;
    }
    //#endif    
    
    /**
     * 获得顶层窗口
     * 
     * @return 
     */
    public static GWindow getTopGWindow() {
    	Object[] vgs = sortHt.values();
    	//遍历所有脚本
    	for(int i = vgs.length - 1; i >= 0; i--) {
    		VMGame vg = (VMGame)vgs[i];
        	
    		//遍历所有window
        	if(vg.vmContainers != null && vg.vmContainers.size() > 0) {
        		Object[] gWindows = vg.vmContainers.values();
        		for(int j = gWindows.length - 1; j >= 0; j--) {
        			GWindow gWindow = (GWindow)gWindows[j];
        			if(gWindow.isShow) {
        				return gWindow;
        			}
        		}
        	}
    	}    	
    	
    	return null;
    }
    
    /**
     * 获得鼠标点击的窗口
     * 
     * @return 
     */
    public static GWindow getMouseTopGWindow(int x, int y) {
    	Object[] vgs = sortHt.values();
    	//遍历所有脚本
    	for(int i = vgs.length - 1; i >= 0; i--) {
    		VMGame vg = (VMGame)vgs[i];
        	
    		//遍历所有window
        	if(vg.vmContainers != null && vg.vmContainers.size() > 0) {
        		Object[] gWindows = vg.vmContainers.values();
        		for(int j = gWindows.length - 1; j >= 0; j--) {
        			GWindow gWindow = (GWindow)gWindows[j];
        			if(gWindow.isShow) {
        				if(Tool.rectIn(gWindow.vmData[GWidget.GW_VM_XX], gWindow.vmData[GWidget.GW_VM_YY], gWindow.vmData[GWidget.GW_VM_W], gWindow.vmData[GWidget.GW_VM_H], x, y)) {
        					return gWindow;
        				} else if(gWindow.catchInput) {
                			return gWindow;
        				}
        			
        			}        			
        		}
        	}
    	}    	
    	
    	return null;
    }
    
    /**
     * 获得x,y位置的组件
     * 
     * @param x
     * @param y
     * @return
     */
    public static GWidget getPointerWidget(int x, int y) {
    	GWindow gTopWindow = getTopGWindow();
    	if(gTopWindow != null) {
    		return gTopWindow.serchWdiget2(x, y);
    	} else {
    		return null;
    	}
    }
    
    /**
     * 获得x,y位置的组件
     * 
     * @param gTopWindow
     * @param x
     * @param y
     * @return
     */
    public static GWidget getPointerWidget(GWindow gTopWindow, int x, int y) {
    	if(gTopWindow != null) {
    		return gTopWindow.serchWdiget(x, y);
    	} else {
    		return null;
    	}
    }
    
    public static GWidget getGWidget(int key) {
    	return (GWidget)gWidgets.get(new Integer(key));
    }
}
