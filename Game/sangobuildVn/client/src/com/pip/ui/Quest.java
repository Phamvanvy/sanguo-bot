package com.pip.ui;


import java.io.IOException;
import java.util.Vector;

import com.pip.common.Tool;
import com.pip.sanguo.GameNpc;
import com.pip.sanguo.GameWorld;
import com.pip.sanguo.GameMain;


/**
 * 本类存储一个游戏任务在客户端保存的信息，包括：ID、名称、类型、任务目标、客户端VM，超出此范围的信息需要在使用时 到服务器请求下载。
 * 同时，本类还提供一组静态方法来维护客户端的任务列表。
 * 为了减少每个循环调用VM指令的次数，我们通过一组事件掩码来优化任务执行过程。在任务编译时我们判断每个触发器被触发
 * 所必须的事件，并在调用VM的CYCLE之前判断本循环是否满足至少一个触发器被触发的条件，如果所有触发器条件都不满足， 就不需要调用VM的CYCLE了。
 * 
 * @author lighthu
 */
public class Quest{
    /** 游戏循环事件，每个CYCLE这个事件都会触发。 */
    public static final int EVENT_MASK_CYCLE = 1;
    /** 用户位置改变事件。 */
    public static final int EVENT_MASK_POSITION = 1 << 1;
    /** 用户和NPC对话事件。 */
    public static final int EVENT_MASK_TOUCHNPC = 1 << 2;
    /** 用户死亡事件。 */
    public static final int EVENT_MASK_DIE = 1 << 3;
    /** 用户发送聊天消息事件。 */
    public static final int EVENT_MASK_CHAT = 1 << 4;
    /** 用户打开界面事件。 */
    public static final int EVENT_MASK_OPENUI = 1 << 5;
    /** 用户关闭任务中触发的对话（包括对话、消息或提问）事件。 */
    public static final int EVENT_MASK_CLOSECHAT = 1 << 6;

    /** 本循环触发事件掩码，每个循环后都会重置为EVENT_MASK_CYCLE。 */
    public static int eventMask;
    /** 当前客户端运行的任务。 */
    public static Vector quests = new Vector();
    /** npc到quest的查找表 key（npcId)，value(Hashtable relateTable) */
//    public static Hashtable npcRelateQuest = new Hashtable();

    /** 当前对话的npcid 每个cycle自动清除 **/
    public static int touchNpcId = -1;
    public static int touchNpcInstanceId = -1;
    
    /** 任务ID */
    public int id;
    /** 任务类型：0 - 普通任务、1 - 场景任务 */
    public int type;
    /** 任务状态 */
    public byte state;    
    /** 任务VM */
    public VM vm;
    /** 任务的事件掩码 */
    protected int questEventMask;
    /** 事件的返回数据 每回合自动清除为null*/
    public int[] resultData = new int[3];

    /** 任务为可接受状态 */
    public static final byte QUEST_STSATE_CAN_ACCEPT = 0;
    /** 任务为进行中状态 */
    public static final byte QUEST_STATE_DOING = 1;
    /** 任务为可完成未交状态 */
    public static final byte QUEST_STATE_CAN_FINISH = 2;
    /** 任务为循环任务状态 */
    public static final byte QUEST_STATE_REPEAT = 3;

    private static VM gameWorldVM;
    
    /**
     * 创建一个新的任务对象。
     * 
     * @param id
     *            任务ID
     * @param type
     *            任务类型
     * @param name
     *            任务名称
     * @param targets
     *            任务目标的描述
     * @param etf
     *            任务的客户端ETF内容（未压缩）
     * @exception 如果ETF文件不合法
     *                ，抛出IOException
     */
    public Quest(int id, int type, byte[] etf) throws IOException{
        this.id = id;
        this.type = type;
        this.vm = new VM(this);
        this.vm.init(etf);
        this.vm.link();
        this.vm.execute(VM.INIT);
    }

    /**
     * 创建一个未接受状态的初始任务
     * @param id 任务id
     * @param type 任务类型
     * @param name 任务名称
     * @param startNpcId 起始npcId
     * @param endNpcId 结束npcId
     * @param questLevel 任务级别
     */
    public Quest(int id, int type){
        this.id = id;
        this.type = type;
        this.vm = null;
    }

    /**
     * 设置任务的状态
     * @param state
     */
    public void setState(byte state){
        this.state = state;
    }
    
    public byte getState(){
        return state;
    }

    /**
     * 接受任务时服务器下发任务的etf数据
     * @param id 任务ID
     * @param etf 任务的客户端ETF内容（未压缩）
     * @throws IOException 如果ETF文件不合法
     */
    public static void updateQuestEtf(int id, byte[] etf) throws IOException{
        Quest quest = findQuest(id, true);

        if(quest != null){
            quest.vm = new VM(quest);
            quest.vm.init(etf);
            quest.vm.link();
            quest.vm.execute(VM.INIT);
        }
    }

    /**
     * 设置当前touch的npcid，每个cycle都会重置为-1，并且设置EventMask的touchNpc标志
     * @param npcId
     */
    public static void touchNpc(int npcId, int instanceId){
        //调用touchNpc
        VM vm = VMGame.getVMGame("game_world").getVM();
        
        boolean isTouch = false;
        synchronized(vm){
        	isTouch = vm.callback(VMGame.CALLBACK_QUEST_TOUCH_NPC, new int[]{npcId, instanceId}) == VM.TRUE;
        }
        
        if(isTouch) {
            touchNpcId = npcId;
            touchNpcInstanceId = instanceId;
            setEventMask(EVENT_MASK_TOUCHNPC);
        }

    }
    
    /**
     * 设置此任务被触发所需要的事件掩码。
     * 
     * @param mask
     */
    public void setQuestEventMask(int mask){
        questEventMask = mask;
    }

    /**
     * 解释一个任务中出现的带变量的文本，把其中的变量引用替换成变量的实际值。带变量的文本可能出现在：任务目标
     * 的描述，Message/Question/Chat函数的参数。
     * 
     * @param str
     * @return
     */
    public static String translateText(int questId, String str){    	
        // 扫描字符串查找变量名进行替换，变量的格式为：${v0}（任务变量）、${GetItemCount(1)}（物品数量）、${_LEVEL}（系统变量）
        char[] arr = str.toCharArray();
        int count = arr.length;
        int state = 0;
        StringBuffer buf = new StringBuffer();
        StringBuffer retBuf = new StringBuffer();
        for(int i = 0; i < count; i++){
            char ch = arr[i];
            switch(state) {
            case 0:
                if(ch == '$' && i < count - 1 && arr[i + 1] == '{'){
                    i++;
                    state = 1;
                    buf.setLength(0);
                } else if(ch == '<' && i < count - 1 && arr[i + 1] == 'n') {
                	i++;
                    state = 100; //解析npc tag
                    buf.setLength(0);
                } else if(ch == '<' && i < count - 1 && arr[i + 1] == 'l') {
                	i++;
                    state = 101; //解析坐标 tag
                    buf.setLength(0);
                } else if(ch == '<' && i < count - 1 && arr[i + 1] == 'm') {
                	i++;
                    state = 102; //解析金钱 tag
                    buf.setLength(0);
                } else {
                    retBuf.append(ch);
                }
            	break;
            case 1:
                if(ch == '}'){
                    // 变量引用部分结束，替换
                    String exprStr = buf.toString().trim();
                    if(exprStr.startsWith("v")){
                        // v开头的一定是任务局部变量
                    	Quest quest = findQuest(questId, true);
                    	if(quest != null) {
                    		retBuf.append(quest.getVariableValue(Integer.parseInt(exprStr.substring(1))));
                    	} else {
                    		return null;
                    	}
                        
                    }else if(exprStr.startsWith("_")){
                        // _开头的一定是全局变量
                    	if(exprStr.equals("_X")) {
                    		retBuf.append(GameWorld.player.sprite.getX());
                    	} else if(exprStr.equals("_Y")) {
                    		retBuf.append(GameWorld.player.sprite.getY());
                    	} else {
                    		retBuf.append(Tool.getGlobalObject(exprStr));
                    	}
                        
                    }else if(exprStr.startsWith("GetItemCount(")){
                        // 只支持一个函数：GetItemCount
                        exprStr = exprStr.substring("GetItemCount(".length());
                        int pos = exprStr.indexOf(')');
                        exprStr = exprStr.substring(0, pos);
                        
                        Quest quest = findQuest(questId, true);
                    	if(quest != null) {
                    		int itemCount = quest.syscall((short)0x400A, new int[]{
                                    Integer.parseInt(exprStr)
                                });
                                retBuf.append(itemCount);
                    	} else {
                    		return null;
                    	}
                        
                    } 
                    state = 0;
                }else{
                    buf.append(ch);
                }
            
            	break;
            case 100:
                if(ch == '>'){
                	String exprStr = buf.toString().trim();
                	if(exprStr.endsWith("</n")) {
                		exprStr = buf.toString().trim().substring(1, exprStr.length() - 3);
                		int firstComma = exprStr.indexOf(',');
                		//NpcId
                		int npcId = Integer.parseInt(exprStr.substring(0, firstComma));
                		retBuf.append("<cff0000>");
                		retBuf.append(exprStr.substring(firstComma + 1, exprStr.length()));
                		retBuf.append("</c>");
                		
                		state = 0;
                	} else {
                		buf.append(ch);
                	}                	
                	
                } else {
                    buf.append(ch);
                }
            	break;
            case 101:
                if(ch == '>'){
                	String exprStr = buf.toString().trim();
                	if(exprStr.endsWith("</l")) {
                		exprStr = buf.toString().trim().substring(1, exprStr.length() - 3);
                		int firstComma = exprStr.indexOf(',');
                		//场景Id
                		int SceneId = Integer.parseInt(exprStr.substring(0, firstComma));
                		retBuf.append("<cff0000>");
                		retBuf.append(exprStr.substring(firstComma + 1, exprStr.length()));
                		retBuf.append("</c>");
                		
                		state = 0;
                	} else {
                		buf.append(ch);
                	}                	
                	
                } else {
                    buf.append(ch);
                }
            	break;
        	case 102:
	            if(ch == '>'){
	            	String exprStr = buf.toString().trim();
	            	if(exprStr.endsWith("</m")) {
	            		exprStr = buf.toString().trim().substring(1, exprStr.length() - 3);
	            		retBuf.append("{#VarUIRes,43}");
                		retBuf.append("<i>");
                		retBuf.append(exprStr);
                		retBuf.append("</i>");
	            		
	            		state = 0;
	            	} else {
	            		buf.append(ch);
	            	}                	
	            	
	            } else {
	                buf.append(ch);
	            }
	        	break;
	        
            }

        }
  
        if(state == 1){
            retBuf.append("${");
            retBuf.append(buf.toString());
        }
                
        return retBuf.toString();
    }

    /**
     * 取得某一个任务目标当前的状态。
     * 
     * @param index
     *            任务目标索引
     * @return 如果任务目标已达成，返回true，否则setQuestEventMask返回false
     */
    public boolean getTargetStatus(int index){
        // 调用回调函数取得任务目标状态
        synchronized(vm){
            return vm.callback("target" + index, new int[0]) == VM.TRUE;
        }
    }

    /**
     * 设置一个任务变量的值。当服务器通知客户端任务变量值改变时调用此方法同步。
     * 
     * @param index
     *            任务变量在变量表中的索引
     * @param newValue
     *            变量新值
     */
    public static void setVariableValue(int questId, int index, int newValue){
    	Quest quest = findQuest(questId, true);
    	quest.vm.memSave(index, newValue);
    }

    /**
     * 获取一个任务变量的当前值。
     * 
     * @param index
     *            任务变量在变量表中的索引
     * @return 变量当前值
     */
    public int getVariableValue(int index){    	
        return vm.memLoad(index);
    }

    /**
     * 向任务列表中新插入一个任务。如果指定的任务在任务列表中已经存在了，则原来的任务被删除。
     * 
     * @param id
     *            任务ID
     * @param type
     *            任务类型
     * @param name
     *            任务名称
     * @param targets
     *            任务目标的描述
     * @param etf
     *            任务客户端ETF文件
     */
    public static Quest addQuest(int id, int type, int startNpcId, int endNpcId, byte[] etf) throws IOException{
        removeQuest(id, startNpcId, endNpcId);
        Quest quest = new Quest(id, type, etf);
        quests.addElement(quest);

        return quest;
    }

    /**
     * 向任务列表中新插入一个可接受的任务
     * @param id 任务id
     * @param type 任务类型
     * @param name 任务名称
     * @param startNpcId 起始npcId
     * @param endNpcId 结束npcId
     * @param questLevel 任务级别
     * @return
     */
    public static Quest addQuest(int id, int type, int startNpcId, int endNpcId){
        removeQuest(id, startNpcId, endNpcId);
        Quest quest = new Quest(id, type);
        quests.addElement(quest);
        return quest;
    }

    /**
     * 从任务列表中删除一个任务。如果指定的任务在列表中不存在，则什么都不做。
     * 
     * @param id
     */
    public static void removeQuest(int id, int startNpcId, int endNpcId){
        int size = quests.size();
        for(int i = 0; i < size; i++){
            Quest quest = (Quest)quests.elementAt(i);
            if(quest.id == id){
                quests.removeElementAt(i);
                break;
            }
        }
    }

    /**
     * 清除所有场景任务。当进入新关卡时需要清除旧场景任务并等待服务器下发新场景任务。
     */
    public static void clearSceneQuests(){
        int size = quests.size();
        for(int i = 0; i < size; i++){
            Quest quest = (Quest)quests.elementAt(i);
            if(quest.type == 1){
                quests.removeElementAt(i);
                i--;
                size--;
                break;
            }
        }
    }
    
    /**
     * 清理所有数据，准备客户端重置
     */
    public static void clear(){
        quests.removeAllElements();
        touchNpcId = -1;
    }

    /**
     * 在任务列表中查找一个任务对象。如果指定的任务不存在，返回null。
     * 
     * @param id
     * @param includeCanAccept
     * @return
     */
    public static Quest findQuest(int id, boolean includeCanAccept){
        int size = quests.size();
        for(int i = 0; i < size; i++){
            Quest quest = (Quest)quests.elementAt(i);
            if(quest.id == id){
                if(includeCanAccept || quest.state != QUEST_STSATE_CAN_ACCEPT){
                    return quest;
                }
            }
        }
        return null;
    }

    /**
     * 设置一个事件为已发生。
     * 
     * @param mask
     *            事件掩码
     */
    public static void setEventMask(int mask){
        eventMask |= mask;
    }

    /**
     * 处理当前载入的所有任务的主循环。
     */
    public static void cycle(){
        int size = quests.size();
        for(int i = 0; i < size; i++){
            Quest quest = (Quest)quests.elementAt(i);
            if(quest.state == QUEST_STSATE_CAN_ACCEPT || (quest.questEventMask & eventMask) == 0){ //对于未接受的任务不参与循环
                continue;
            }
            quest.vm.execute(VM.CYCLE);
        }
        eventMask = EVENT_MASK_CYCLE;
        touchNpcId = -1;
    }

    public static void setGameWorldVM(VM gwv) {
    	gameWorldVM = gwv;
    }
    /**
     * 处理和任务有关的系统函数调用。
     * 
     * @param funcID
     *            函数ID
     * @param params
     *            函数参数
     * @return
     */
    public int syscall(short funcID, int[] params){
        switch(funcID){
            case 0x4001: // 0x4001=boolean PQE_HasTask(int taskID)
                return findQuest(params[0], false) != null? VM.TRUE: VM.FALSE;
            case 0x4002: // 0x4002=boolean PQE_CanFinish()
                return (state == QUEST_STATE_CAN_FINISH)? VM.TRUE: VM.FALSE;
            case 0x4003: // 0x4003=void PQE_Chat(int npcID, String message, int notifyID)
                synchronized(gameWorldVM){
                    gameWorldVM.callback(VMGame.CALLBACK_PQE_CHAT, new int[]{
                            gameWorldVM.makeTempObject(this), params[0], gameWorldVM.makeTempObject(getText(params[1])), params[2], touchNpcInstanceId
                    });
                }
                break;
            case 0x4004: // 0x4004=void PQE_Message(String message, int timeout, int notifyID)
                synchronized(gameWorldVM){
                    gameWorldVM.callback(VMGame.CALLBACK_PQE_MESSAGE, new int[]{
                            gameWorldVM.makeTempObject(this), gameWorldVM.makeTempObject(getText(params[0])), params[1], params[2]
                    });
                }
                break;
            case 0x4005: // 0x4005=void PQE_Question(String message, String options, int notifyID)
                synchronized(gameWorldVM){
                    gameWorldVM.callback(VMGame.CALLBACK_PQE_QUESTION, new int[]{
                            gameWorldVM.makeTempObject(this), gameWorldVM.makeTempObject(getText(params[0])), gameWorldVM.makeTempObject(getText(params[1])), params[2]
                    });
                }
                break;
            case 0x4007: // 0x4007=void PQE_Flash(int frames)
                GameWorld.instance.sendCommand(VMGame.GAME_COMMAND_WORLD_QUEST_ACTION, new int[]{
                                0x4007, params[0]
                });
                break;
            case 0x4008: // 0x4008=void PQE_GotoMap(int mapID, int x, int y)
                GameWorld.instance.sendCommand(VMGame.GAME_COMMAND_WORLD_QUEST_ACTION, new int[]{
                                0x4008, params[0], params[1], params[2]
                });
                break;
            case 0x4009: // 0x4009=void PQE_Logout()
                GameWorld.instance.sendCommand(VMGame.GAME_COMMAND_WORLD_QUEST_ACTION, new int[]{
                                0x4009
                });
                break;
            case 0x400A: // 0x400A=int PQE_GetItemCount(int itemID)
            {
                VM vm = VMGame.getVMGame("game_role").getVM();
                
                synchronized(vm){
                    return vm.callback(VMGame.CALLBACK_QUEST_GET_ITEM_COUNT, new int[]{params[0]});
                }
            }
            case 0x400B: // 0x400B=boolean PQE_HasItem(int itemID, int count)
            {
                VM vm = VMGame.getVMGame("game_role").getVM();
                
                synchronized(vm){
                	int count = vm.callback(VMGame.CALLBACK_QUEST_GET_ITEM_COUNT, new int[]{params[0]});
                    return (count >= params[1]) ? VM.TRUE : VM.FALSE;
                }
            }
            case 0x400C: // 0x400C=boolean PQE_E_Approach(int mapID, int x, int y)
                if(GameWorld.currentMap != null && GameWorld.player != null){
                	//#if ModelID == AndroidAuto
                    //# if (GameMain.getUIModel().equals(GameMain.ANDROID_LARGE))
                	//# {
                		//# if(GameWorld.currentMap.id == params[0] && Math.abs(GameWorld.player.sprite.getX() - params[1]*2) <= 80 && Math.abs(GameWorld.player.sprite.getY() - params[2]*2) <= 80)
                		//# {
                		//# return VM.TRUE;
                		//# }
                		//# else
                		//# {
                		//# return VM.FALSE;
                		//# }
                	//# }
                	//# else
                	//# {
                		//# if(GameWorld.currentMap.id == params[0] && Math.abs(GameWorld.player.sprite.getX() - params[1]) <= 80 && Math.abs(GameWorld.player.sprite.getY() - params[2]) <= 80)
                		//# {
                		//# return VM.TRUE;
                		//# }
                		//# else
                		//# {
                		//# return VM.FALSE;
                		//# }	
                	//# }
                	//#elif DoubleScreen == true
                	//# if(GameWorld.currentMap.id == params[0] && Math.abs(GameWorld.player.sprite.getX() - params[1]*2) <= 80 && Math.abs(GameWorld.player.sprite.getY() - params[2]*2) <= 80)
                	//# {
                	//# return VM.TRUE;
                	//# }
                	//# else
                	//# {
                	//# return VM.FALSE;
                	//# }
                	//#else
                	//# if(GameWorld.currentMap.id == params[0] && Math.abs(GameWorld.player.sprite.getX() - params[1]) <= 80 && Math.abs(GameWorld.player.sprite.getY() - params[2]) <= 80)
                	//# {
                	//# return VM.TRUE;
                	//# }
                	//# else
                	//# {
                	//# return VM.FALSE;
                	//# }
                    //#endif
                }
                break;
            case 0x400D: // 0x400D=boolean PQE_E_EnterMap(int mapID)
                if(GameWorld.currentMap != null && GameWorld.currentMap.id == params[0]){
                    return VM.TRUE;
                }else{
                    return VM.FALSE;
                }
            case 0x400E: // 0x400E=boolean PQE_E_TouchNPC(int npcID)
                if(touchNpcId == params[0]){
                    return VM.TRUE;
                }else{
                    return VM.FALSE;
                }
            case 0x4012: // 0x4012=boolean PQE_E_OpenUI(String uiName)
                if(VMGame.getVMGame((String)vm.followPointer(params[0])) != null){
                    return VM.TRUE;
                }else{
                    return VM.FALSE;
                }
            case 0x4013: // 0x4013=boolean PQE_E_AnswerQuestion(int notifyID, int optionID)
                if(resultData[1] == params[0] && resultData[2] == params[1]){
                    return VM.TRUE;
                }else{
                    return VM.FALSE;
                }
            case 0x4014: // 0x4014=boolean PQE_E_CloseChat(int notifyID)
                System.out.println("Quest E_CloseChat : " + params[0]);
                if(resultData[1] == params[0]){
                    return VM.TRUE;
                }else{
                    return VM.FALSE;
                }
            case 0x4015: // 0x4015=boolean PQE_E_CloseMessage(int notifyID)
                System.out.println("Quest E_CloseMessage : " + params[0]);
                if(resultData[1] == params[0]){
                    return VM.TRUE;
                }else{
                    return VM.FALSE;
                }
            case 0x4016: // 0x4016=void PQE_Listen(int mask)
                setQuestEventMask(params[0]);
                break;
            case 0x4017: // 0x4017=int PQE_GetEventMask()
                return eventMask;
        }
        return 0;
    }

    public String getText(int vmData){
        return translateText(id, (String)vm.followPointer(vmData));
    }

    public Integer getInt(int vmData) {
    	return new Integer(vmData);
    }
    
    /**
     * 事件结束回调
     */
    public void eventClosed(Object result){
        int[] uiResult = (int[])result;

        System.arraycopy(uiResult, 0, resultData, 0, uiResult.length);
        setEventMask(EVENT_MASK_CLOSECHAT);
        Tool.sendNotifyServer(id, resultData[1], resultData[0], resultData[2]);
    }

    /**
     * 判断当前任务是否与指定npc关联
     * @param npcId
     * @return
     */
    public boolean interact(int npcId){
        synchronized(vm){
            return vm.callback("interact", new int[]{
                npcId
            }) != 0;
        }
    }

}
