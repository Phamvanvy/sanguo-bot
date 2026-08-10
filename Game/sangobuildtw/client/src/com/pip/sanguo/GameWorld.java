package com.pip.sanguo;


import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import com.pip.common.Tool;
import com.pip.common.Utilities;
import com.pip.engine.AnimateCache;
import com.pip.engine.GameMap;
import com.pip.engine.GamePackage;
import com.pip.engine.IVMGameProcessor;
import com.pip.io.UASegment;
import com.pip.ui.Quest;
import com.pip.ui.VM;
import com.pip.ui.VMGame;
import com.pip.util.SortHashtable;

public class GameWorld implements IVMGameProcessor{
    public static GameWorld instance;
    public static GamePanel panel;
    public static byte[] pkgData = null;
    public static SortHashtable teamInfo;
    
    public static boolean inLoading = false;
    public static boolean netplayerNameNearShow = true;
    
    private Hashtable gameData = new Hashtable();
    public VM vm;

    public GameIcon vm_create_icon(int _type, int _id, int _animateIndex){
        GameSprite father = (GameSprite)gameSpriteTable.get(Tool.getSpriteKey(_type, _id));
        
        if(father != null){
            GameIcon icon = GameIcon.createGameIcon(father, _animateIndex);

            if(icon != null){
                gameIcons.put(new Integer(icon.getId()), icon);
            }
            
            return icon;
        }
        
        return null;
    }
    
    public void vm_close_event(Quest _questProcessor, int[] _data){
        _questProcessor.eventClosed(_data);
    }
    
    public GameIcon vm_create_icon2(GameSprite _processor, int _animateIndex){
        if(_processor != null){
            GameIcon icon = GameIcon.createGameIcon(_processor, _animateIndex);

            if(icon != null){
                gameIcons.put(new Integer(icon.getId()), icon);
            }
            
            return icon;
        }
        
        return null;
    }
    
    public void vm_world_show_map_npc_animate(boolean show){
        GameView.showMapNpcAnimate = show;
        
        if(gameView != null){
            gameView.isFirstBgImage = true;
        }
    }
    
    public void vm_world_set_3dstring_level(int level){
        GameMain.draw3DStringLevel = level;
    }
    
    public void vm_world_set_mini_map_config(UASegment _seg){
        _seg.flush();
        _seg.reset();
        GameView.initMiniMapConfig(_seg);
    }
    
    public void vm_game_set_mini_map_show(boolean show){
        GameView.miniMapShow = show;
    }
    
    public void vm_game_set_mini_map_alpha(int alpha){
        GameView.miniMapConfig[GameView.MINI_MAP_CONIFG_ALPHA] = alpha;
        
        if(GameWorld.gameView != null){
            GameWorld.gameView.rebuildMiniMap();
        }
    }
    
    public Vector vm_game_get_netplayer_list(){
        Vector ret = new Vector();
        
        for(int i=0; i<gameSprites.size(); i++) {
            GameSprite gs = (GameSprite)gameSprites.elementAt(i);
            if(gs.getType() == Tool.SPRITE_TYPE_PLAYER) {
                ret.addElement(gs);
            }
        }
        
        return ret;
    }
    
    public int vm_world_get_target_distance(){
        if(player.target == null){
            return -1;
        }else{
            int x1 = player.sprite.getX();
            int y1 = player.sprite.getY();
            int x2 = ((GameSprite)player.target).sprite.getX();
            int y2 = ((GameSprite)player.target).sprite.getY();
            int distance = Math.abs(Tool.distance(x1, y1, x2, y2));
            
            return distance;
        }
    }
    
    public void vm_game_to_map(int _mapId, int _mapInstanceId, int _x, int _y){
        goMap(_mapId, _mapInstanceId, _x, _y);
    }
     
    public void vm_game_add_follower(int _ownerInstanceId, int _followerInstanceId){
        GameSprite owner = GameWorld.getSprite(_ownerInstanceId);
        GameSprite follower = GameWorld.getSprite(_followerInstanceId);
        
        if(owner != null && follower != null){
            owner.addFollower(follower);
        }
    }
    
    public void vm_game_del_follower(){
        GameSprite owner = GameWorld.player.followOwner;
        
        if(owner != null){
            owner.removeFollower(GameWorld.player);
        }
    }
    
    public boolean vm_world_get_target_can_attack(){
        if(player.target != null && player.target.canAttack){
            return true;
        }else{
            return false;
        }
    }
    
    public Vector vm_game_get_gamesprite_list(){
        return gameSprites;
    }
    
    public int vm_game_get_map_id(){
        if(currentMap != null){
            return currentMap.id;
        }else{
            return -1;
        }
    }
    
    public void vm_game_do_touch_npc(GameSprite _processor){
        Quest.touchNpc(_processor.getId(), _processor.getInstanceId());
    }
    
    public void vm_request_destroy_sprite(GameSprite _sprite){
        requestDestorySprite(_sprite);
    }
    
    public String vm_game_current_landmark_name(){
        String landName;
        
        if(currentMap != null){
            landName = currentMap.name;
            int extraIdx = landName.indexOf('|');

            if(extraIdx >= 0){
                landName = landName.substring(0, extraIdx);
            }
        }else{
            landName = "";
        }
        
        return landName;
    }
    
    public int vm_game_get_hmsg_count(){
        if(panel != null){
            Vector _hmsgs = (Vector)panel.hMessageItem.objData;
            if(_hmsgs == null) {
                return 0;
            } else {
                return _hmsgs.size();
            }
        }
        
        return 0;
    }
    
    public int vm_game_get_vmsg_count(){
        if(panel != null){
            Vector _vmsgs = (Vector)panel.vMessageItem.objData;
            if(_vmsgs == null) {
                return 0;
            } else {
                return _vmsgs.size();
            }
        }
        
        return 0;
    }
    
    public int vm_game_get_dis(GameSprite _gameSprite1, GameSprite _gameSprite2){
        if(_gameSprite1 != null && _gameSprite2 != null){
            int x1 = _gameSprite1.sprite.getX();
            int y1 = _gameSprite1.sprite.getY();
            int x2 = _gameSprite2.sprite.getX();
            int y2 = _gameSprite2.sprite.getY();
            return Math.abs(Tool.distance(x1, y1, x2, y2));
        }else{
            return -1;
        }
    }
    
    public void vm_game_do_destroy_sprite(int _id, int _instanceId, boolean testLeaving){
        doDestorySprite(getSprite(_instanceId), testLeaving, false);
    }
    
    public void vm_world_set_in_loading(boolean _inLoading){
        GameWorld.inLoading = _inLoading;
    }
    
    public void vm_world_set_netplayer_name_near_show(boolean _nearShow){
        GameWorld.netplayerNameNearShow = _nearShow;
    }
    
    public int[] vm_game_get_mini_map_size(){
        int[] minisize = null;
        
        if(GameWorld.gameView != null){
            minisize = GameWorld.gameView.getMiniMapSize();
        }else{
            minisize = new int[]{
                            0, 0
            };
        }

        return minisize;
    }
    
    public boolean vm_world_get_is_team_member(int _instanceId){
        return teamInfo.get(new Integer(_instanceId)) != null;
    }
    
    public int vm_game_add_quest_etf(int _questId, int _type, int _startNpcId, int _endNpcId, byte[] _etf){
        try{
            Quest.addQuest(_questId, _type, _startNpcId, _endNpcId, _etf);
            
            return 1;
        }catch(Exception e){
        	//#ifdef buildtest
            e.printStackTrace();
          //#endif
            
            return -1;
        }
    }
    
    public void vm_game_add_quest(int _questId, int _type, int _startNpcId, int _endNpcId){
        Quest.addQuest(_questId, _type, _startNpcId, _endNpcId);
    }
    
    public void vm_game_remove_quest(int _questId, int _startNpcId, int _endNpcId){
        Quest.removeQuest(_questId, _startNpcId, _endNpcId);
    }
    
    public int vm_game_update_quest_etf(int _questId, byte[] _etf){
        try{
            Quest.updateQuestEtf(_questId, _etf);
            
            return 1;
        }catch(Exception e){
        	//#ifdef buildtest
            e.printStackTrace();
          //#endif
            
            return -1;
        }
    }
    
    public void vm_game_set_quest_var(int _questId, int _index, int _var){
        Quest.setVariableValue(_questId, _index, _var);
    }
    
    public String vm_game_translate_text(int _questId, String _text){
        return Quest.translateText(_questId, _text);
    }
    
    public void vm_game_set_quest_state(int _questId, int _state){
        Quest quest = Quest.findQuest(_questId, true);
        
        if(quest != null) {
            quest.setState((byte)_state);  
        }
    }
    
    public int vm_game_get_quest_state(int _questId){
        Quest quest = Quest.findQuest(_questId, true);
        
        if(quest != null){
            return quest.getState();
        }else{
            return -1;
        }
    }
    
    public void vm_game_clear_scene_quests(){
        Quest.clearSceneQuests();
    }
    
    public static Object vm_game_vm_callback(int msgId, Object msg, String vmids, String funcName){
        return vm_game_vm_callback2(msgId, msg, Tool.splitString(vmids), funcName);
    }
    
    public static Object vm_game_vm_callback2(int msgId, Object msg, String[] vmids, String funcName){
        int count = vmids.length;
        
        for(int i = 0; i < count; i++){
            VMGame vg = VMGame.getVMGame(vmids[i]);
            
            if(vg != null) {
                vg.callback(funcName, new Object[]{new Integer(msgId), msg });
            }
        }
        
        return null;
    }
    
    public int[] vm_game_build_random_pos_list(int orgX, int orgY, int minOffset, int maxOffset, int count){
        int[] result = new int[count * 2];
        int s = maxOffset - minOffset;
        
        for(int i = 0; i < count; i++){
            result[(i << 1)] = Tool.getNextRnd(0, s) + minOffset + orgX;
            result[(i << 1) + 1] = Tool.getNextRnd(0, s) + minOffset + orgY;
        }
        
        return result;
    }
    
    public boolean vm_world_in_game_screen(){
        return VMGame.isAllTransparent(true) && GameWorld.gameView != null && GameWorld.player != null;
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

    public Object readGameData(String dataName){
        return gameData.get(dataName);
    }

    public void removeGameData(String dataName){
        gameData.remove(dataName);
    }

    public void saveGameData(String dataName, Object data){
        gameData.put(dataName, data);
    }

    /**
     * 图标Hashtable
     */
    public static Hashtable gameIcons = new Hashtable();
    
    /**
     * 地图出口
     */
    public static Vector gameExits = new Vector();

    /**
     * 精灵Vector
     */
    public static Vector gameSprites = new Vector();
    
    /**
     * 等待被释放的精灵
     */
    public static Vector waitRemoveSprites = new Vector();
    
    /**
     * 等待播放走出视野的精灵
     */
    public static Vector leavingSprites = new Vector();

    /**
     * 精灵hashtable，快速查找需要，key：long(type << 32 | id)，value：sprite
     */
    public static Hashtable gameSpriteTable = new Hashtable();

    //按照instanceId索引
    public static Hashtable gameSpriteTable2 = new Hashtable();
    /**
     * 地图边界的碰撞检测矩形，分上下左右四个方向
     */
    public static int[][] mapCollisionBox = null;

    public static GamePackage gamePackage;
    public static GameMap currentMap;
    public static int currentAreaId = -1;
    public static GameView gameView;
    public static int playerNextMap;
    public static int playerNextMapInstanceId = -1;
    public static int playerNextX;
    public static int playerNextY;

    public static int viewX;
    public static int viewY;
    public static GameRole player;

    public static void clear(){
        teamInfo = null;
        gamePackage = null;
        currentAreaId = -1;
        currentMap = null;
        gameView = null;
        player = null;
        gameSpriteTable.clear();
        gameSpriteTable2.clear();
        gameSprites.removeAllElements();
        gameIcons.clear();
        gameExits.removeAllElements();
        Tool.unitViewCache.clear();
        
        instance = null;
    }
    
    public static void moveMap(){
        if(player == null || currentMap == null){
            return;
        }

        viewX = (short)(player.sprite.getX() - GameMain.viewWidth / 2);
        viewY = (short)(player.sprite.getY() - GameMain.viewHeight / 2);

        if(viewX < 0){
            viewX = 0;
        }

        if(viewY < 0){
            viewY = 0;
        }

        int viewMaxX = (short)((currentMap.width - GameMain.viewWidth) & 0xFFFF);
        int viewMaxY = (short)((currentMap.height - GameMain.viewHeight) & 0xFFFF);

        if(viewX > viewMaxX){
            viewX = viewMaxX;
        }

        if(viewY > viewMaxY){
            viewY = viewMaxY;
        }

        if (viewMaxX < 0) {
            viewX = (short)(viewMaxX / 2);
        }
        if (viewMaxY < 0) {
            viewY = (short)(viewMaxY / 2);
        }
    }
    
    public GameWorld(){
        instance = this;
        VMGame.loadVMGame("game_world", VMGame.VM_TYPE_GAME, true);
        vm = VMGame.getVMGame("game_world").getVM();
        Quest.setGameWorldVM(vm);
        panel = new GamePanel();
        panel.init();
    }

    public void draw(Graphics g,int low,int high) throws Exception{
        if(gameView != null){
            gameView.draw(g, viewX, viewY);
            panel.draw(g,low,high);
        }
    }

    public void cycle(){
        //处理精灵的移除
        checkAndRemoveSprite();
        
        //将netplayer按lastSyncMoveTime排序
        if(GameMain.tick % 10 == 0){
            orderNetPlayer();
        }
        
        if(gameView != null){
            gameView.cycle(viewX, viewY);
        }

        // 处理sprite
        for(int i = 0; i < gameSprites.size(); i++){
        	GameSprite gs = ((GameSprite)gameSprites.elementAt(i));
        	gs.cycle();
        }

        // 处理icon
        Enumeration emu = gameIcons.elements();

        while(emu.hasMoreElements()){
            ((GameIcon)emu.nextElement()).cycle();
        }
        
        //处理出口
        for(int i = 0; i < gameExits.size(); i++){
            GameExit gs = ((GameExit)gameExits.elementAt(i));
            gs.cycle();

        }
        
        //处理走出视野的NPC
        for(int i = 0; i < leavingSprites.size(); i++){
            GameSprite gs = (GameSprite)leavingSprites.elementAt(i);
            gs.cycle();
            
            if(!gs.chaseMode){
                gs.destroy();
                leavingSprites.removeElementAt(i);
                i--;
            }
        }

        // 处理Quest
        Quest.cycle();

        // 处理游戏面板
        panel.cycle();
    }

    public void processPacket(){
        UASegment segment = GameMain.instance.nextPacket;

        switch(segment.type){
            case -1: {
                int s = segment.readInt();
                int t = segment.readShort();
                String msg = segment.readString();

                System.out.println("Unhandled Error found by GameWorld java: " + msg);
            }
                break;
            case Tool.CONN_LOGIN_SERVER: {
                //                player = Tool.recvLogin(segment);
                //                addSprite(player);
                segment.reset();
                System.out.println("length=" + segment.data.length);
                for(int i = 0; i < segment.data.length; i++){
                    System.out.print("0x" + Integer.toHexString(segment.data[i] & 0xFF));
                    System.out.print(",");
                }
                System.out.println();

            }
                break;
            case Tool.CONN_LOGOUT_SERVER: {
                Tool.recvLogout(segment);
            }
                break;
            case Tool.CONN_UNIT_INVISIBLE_SERVER: {
                Tool.recvInvisible(segment);
            }
                break;
            case Tool.CONN_SKILL_ATTACK_SERVER: {
                Tool.recvAttack(segment, false);
            }
                break;
            case Tool.CONN_SKILL_PREPARE_ATTACK_SERVER: {
                Tool.recvAttack(segment, true);
            }
                break;
            case Tool.CONN_SKILL_ATTACKED_SERVER: {
                Tool.recvAttacked(segment);
            }
                break;
            case Tool.CONN_ATTACK_FAIL_SERVER: {
                Tool.recvAttackFail(segment);
            }
                break;
            case Tool.CONN_GOMAP_ALLOW: {
                Tool.recvAllowGomap(segment);
            }
                break;
//            case Tool.CONN_NPC_CHAT_SERVER: {
//                /**
//                * npcId        int
//                * message      string
//                * notifyId     int
//                */
//                int npcId = segment.readInt();
//                String message = segment.readString();
//                int notifyId = segment.readInt();
//
//                GameEvent.addEvent(this, GameEvent.EVENT_CHAT, GameEvent.AUTO_PRIORITY, GameEvent.AUTO_TIME_OUT, new Object[]{
//                                new Integer(npcId), message, new Integer(notifyId)
//                });
//            }
//                break;
//            case Tool.CONN_MESSAGE_SERVER: {
//                /**
//                * message      string
//                * timeout      int
//                * notifyId     int
//                */
//                String message = segment.readString();
//                int timeout = segment.readInt();
//                int notifyId = segment.readInt();
//
//                GameEvent.addEvent(this, GameEvent.EVENT_MESSAGE, GameEvent.AUTO_PRIORITY, timeout, new Object[]{
//                                message, new Integer(notifyId)
//                });
//            }
//                break;
//            case Tool.CONN_QUESTION_SERVER: {
//                /**
//                * message      string
//                * options      string
//                * notifyId     int
//                */
//                String message = segment.readString();
//                String options = segment.readString();
//                int notifyId = segment.readInt();
//
//                GameEvent.addEvent(this, GameEvent.EVENT_QUESTION, GameEvent.AUTO_PRIORITY, GameEvent.AUTO_TIME_OUT, new Object[]{
//                                message, options, new Integer(notifyId)
//                });
//            }
//                break;
            case Tool.CONN_GETFILE_SERVER: {
                Tool.recvGetFile(segment);
            }
                break;
            case Tool.CONN_UNIT_REFRESH_SERVER:{
            	Tool.recvUnitView(segment);            	
            }
            	break;
            case Tool.CONN_UNIT_MULTI_REFRESH_SERVER:{
                Tool.recvMultiUnitView(segment);
            }
                break;
            case Tool.CONN_UNIT_MOVE_SERVER:{
            	Tool.recvUnitMove(segment);
            }
                break;
            case Tool.CONN_VERSION_COMPARE_SERVER:{
                 GameMain.resourceManager.recvSyncVersion(segment);
            }
                break;
            case Tool.CONN_SYNC_VERSION_SERVER:{
                 GameMain.resourceManager.syncVersion(true);
            }
                break;
            case Tool.CONN_CHASE_SERVER:{
                Tool.recvChaseServer(segment);
            }
                break;
            default:
                break;
        }
    }

    /**
     * 用1个方形区域与世界进行碰撞检测，输入方形区域移动的方向和步长，返回实际可移动的距离
     * @param x
     * @param y
     * @param w
     * @param h
     * @param direct
     * @param step
     * @param oldX
     * @param oldY
     * @return
     */
    public static int collisionWorld(int x, int y, int w, int h, int direct, int step, int oldX, int oldY){
        int result = step;

        int[] mapBox = getWorldCollisionBox(direct);

        if(mapBox != null){
            if(Tool.calculateDistance(mapBox[0], mapBox[1], mapBox[2], mapBox[3], x, y, w, h, direct) <= 0){
                result = Tool.calculateDistance(mapBox[0], mapBox[1], mapBox[2], mapBox[3], oldX, oldY, w, h, direct);
            }
        }else{
            result = 0;
        }

        if(result > step){
            result = step;
        }else if(result < 0){
            result = 0;
        }

        result = gameView.collisionMap(x, y, w, h, direct, step, oldX, oldY, result);
        result = gameView.collisionYOrder(x, y, w, h, direct, step, oldX, oldY, result);

        return result;
    }

    /**
     * 取得世界边界的碰撞检测box
     * @param direct 取哪个运动方向的屏幕边界
     * @return int[] 检测矩形 x y w h
     */
    private static int[] getWorldCollisionBox(int direct){
        int[] result = null;

        if(mapCollisionBox == null){
            mapCollisionBox = new int[4][];

            mapCollisionBox[Tool.DIR_DOWN] = new int[]{
                            Integer.MIN_VALUE >> 2, currentMap.height - 1, Integer.MAX_VALUE >> 2, Integer.MAX_VALUE >> 2
            };
            mapCollisionBox[Tool.DIR_LEFT] = new int[]{
                            Integer.MIN_VALUE >> 2, Integer.MIN_VALUE >> 2, 0, Integer.MAX_VALUE >> 2
            };
            mapCollisionBox[Tool.DIR_RIGHT] = new int[]{
                            currentMap.width - 1, Integer.MIN_VALUE >> 2, Integer.MAX_VALUE >> 2, Integer.MAX_VALUE >> 2
            };
            mapCollisionBox[Tool.DIR_UP] = new int[]{
                            Integer.MIN_VALUE >> 2, Integer.MIN_VALUE >> 2, Integer.MAX_VALUE >> 2, 0
            };

            for(int i = 0; i < mapCollisionBox.length; i++){
                mapCollisionBox[i][2] = mapCollisionBox[i][2] - mapCollisionBox[i][0];
                mapCollisionBox[i][3] = mapCollisionBox[i][3] - mapCollisionBox[i][1];
            }
        }

        switch(direct){
            case Tool.DIR_DOWN:
            case Tool.DIR_LEFT:
            case Tool.DIR_RIGHT:
            case Tool.DIR_UP:
                result = mapCollisionBox[direct];
        }

        return result;
    }

    public static void clearGameSprites(){
        Vector needRemove = new Vector();

        for(int i = 0; i < gameSprites.size(); i++){
            GameSprite gameSprite = (GameSprite)gameSprites.elementAt(i);

            int type = gameSprite.getType();
            int id = gameSprite.getId();

            if(type == Tool.SPRITE_TYPE_PLAYER){
            	if(((GameNetPlayer)gameSprite).noNeedRemove) {
            		gameSprite.isOutView = true;            		
            	} else {
            		needRemove.addElement(gameSprite);	
            	}
                
            }else if(type == Tool.SPRITE_TYPE_NPC || type == Tool.SPRITE_TYPE_GATHER_NPC){
                if(currentMap != null && currentMap.id == (id >> 12)){
                    needRemove.addElement(gameSprite);
                }
            }
        }

        for(int i = 0; i < needRemove.size(); i++){
            doDestorySprite((GameSprite)needRemove.elementAt(i), false, false);
        }
        
        gameExits.removeAllElements();
        gameIcons.clear();
        player.clearTarget();
    }

    public static GameNpc findNpcById(byte type, int id){
        return (GameNpc)getSprite(type, id);
    }

    public static GameNetPlayer findPlayerById(int id){
        return (GameNetPlayer)getSprite(Tool.SPRITE_TYPE_PLAYER, id);
    }
    
    public static GameNpc findNpcByInstanceId(int instanceId){
        return (GameNpc)getSprite(instanceId);
    }

    public static GameNetPlayer findPlayerByInstanceId(int instanceId){
        return (GameNetPlayer)getSprite(instanceId);
    }

    public static void playerLogout(int instanceId){
        GameNetPlayer np = (GameNetPlayer)getSprite(instanceId);

        if(np != null){
            requestDestorySprite(np);
        }
    }

    public static GameSprite getSprite(int type, int id){
        return (GameSprite)gameSpriteTable.get(Tool.getSpriteKey(type, id));
    }
    
    public static GameSprite getSprite(int instanceId){
        return (GameSprite)gameSpriteTable2.get(new Integer(instanceId));
    }

    public static void requestDestorySprite(GameSprite gameSprite){
        if(gameSprite == null){
            return;
        }

        waitRemoveSprites.addElement(gameSprite);
    }
    
    public static void doDestorySprite(GameSprite gameSprite, boolean testLeaving, boolean isWaitingRemove){
        if(gameSprite == null){
            return;
        }
        
        //删除跟随宿主对象
        if(gameSprite.followOwner != null){
            gameSprite.followOwner.removeFollower(gameSprite);
        }

        gameSprite.removeAllFollowers();
        
        if(isWaitingRemove) {
        	//gameSprites中有重复的，gameSpriteTable中不删除
            int count = gameSprites.size();
            boolean shouldDel = true;
            boolean isBreak = false;            
            for(int i=0; i<count; i++) {
            	if(isBreak) {
            		break;
            	}
            	GameSprite gs1 = (GameSprite)gameSprites.elementAt(i);            	
            	for(int j=i + 1; j<count; j++) {
            		GameSprite gs2 = (GameSprite)gameSprites.elementAt(j);
            		if(gs1.getType() == gs2.getType() && gs1.getId() == gs2.getId()) {
        				shouldDel = false;
            			isBreak = true;
            			break;
            		}
            	}
            }
            
            if(shouldDel) {
            	gameSpriteTable.remove(Tool.getSpriteKey(gameSprite.getType(), gameSprite.getId()));
            }
        } else {
        	gameSpriteTable.remove(Tool.getSpriteKey(gameSprite.getType(), gameSprite.getId()));
        }

        gameSprites.removeElement(gameSprite);
        gameSpriteTable2.remove(new Integer(gameSprite.getInstanceId()));
                
        if(player.target == gameSprite){
            player.clearTarget();
        }

//        if(testLeaving){
//            if(gameSprite.leavingPosition != null && !gameSprite.die){
//                gameSprite.clearChase();
//                gameSprite.startChase(gameSprite.sprite.getX(), gameSprite.sprite.getY(), gameSprite.leavingPosition[0], gameSprite.leavingPosition[1], 0, gameSprite.sprite.getSpeed() * GameMain.spriteLeavingSpeed, -1, null, false);
//                GameWorld.leavingSprites.addElement(gameSprite);                
//            }else{
//                gameSprite.destroy();
//            }
//        }else{
//            gameSprite.destroy();
//        }
        gameSprite.destroy();
    }
    
    public static void orderNetPlayer(){
        GameSprite g1 = null;
        GameSprite g2 = null;
        GameNetPlayer n1 = null;
        GameNetPlayer n2 = null;

        for(int i = 0; i < gameSprites.size() - 1; i++){
            g1 = (GameSprite)gameSprites.elementAt(i);
            n1 = null;
            
            if(g1.getType() == Tool.SPRITE_TYPE_PLAYER){
                n1 = (GameNetPlayer)g1;
            }else{
                continue;
            }
            
            for(int j = i + 1; j < gameSprites.size(); j++){
                g2 = (GameSprite)gameSprites.elementAt(j);
                
                if(g2.getType() == Tool.SPRITE_TYPE_PLAYER){
                    n2 = (GameNetPlayer)g2;
                    
                    if(n2.lastSyncMoveTime > n1.lastSyncMoveTime){
                        gameSprites.setElementAt(n2, i);
                        gameSprites.setElementAt(n1, j);
                        n1 = n2;
                    }
                }else{
                    continue;
                }
            }
        }
    }
    
    public static void checkAndRemoveSprite(){
        int count = waitRemoveSprites.size();
        Vector canRemove = new Vector();
        Vector restSprite = new Vector();
                
        for(int i=0; i<count; i++) {
        	 GameSprite gameSprite = (GameSprite)waitRemoveSprites.elementAt(i);
        	 if(gameSprite.canRemoved()){
                 canRemove.addElement(gameSprite);
             }else{
                 restSprite.addElement(gameSprite);
             }
        }
        
        count = canRemove.size();
        
        for(int i = 0; i < count; i++){
            doDestorySprite((GameSprite)canRemove.elementAt(i), false, true);
        }
        
        waitRemoveSprites = restSprite;
    }

    public static void addSprite(GameSprite gameSprite){
        gameSprites.addElement(gameSprite);
        gameSpriteTable.put(Tool.getSpriteKey(gameSprite.getType(), gameSprite.getId()), gameSprite);
        gameSpriteTable2.put(new Integer(gameSprite.getInstanceId()), gameSprite);
    }

    public static void BroadcastPacket(UASegment segment){
        segment.flush();
        segment.reset();
        segment.handled = false;
        Utilities.segments.addElement(segment);
    }
    
    public static void loadMap(){
        try{
            gameView = null;
            GameWorld.clearGameSprites();
            
            if(GameMain.animateCacheType == 1){
                AnimateCache.clearPendingReleaseAnimate();
            }
            
            GameWorld.currentMap = GameWorld.gamePackage.loadMap(GameWorld.playerNextMap & 0xF);
            gameView = new GameView(currentMap);
            GameWorld.gamePackage = null;
            currentMap.owner = null;
            GameWorld.player.clearChase();
            GameWorld.player.sprite.setMapId(currentMap.id);
            GameWorld.player.sprite.setMapInstanceId(playerNextMapInstanceId);
            GameWorld.player.clearTarget();
            GameWorld.player.sprite.setPosition(GameWorld.playerNextX, GameWorld.playerNextY);
            //Tool.sendPosition( GameWorld.player.sprite.getDir(),  GameWorld.player.sprite.getX(),  GameWorld.player.sprite.getY(),  GameWorld.player.state);
            GameWorld.mapCollisionBox = null;
            //Tool.sendLoadMapFinished();
            panel.state = GamePanel.GAME_PANEL_STATE_INIT;
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static void goMap(int nextMap, int nextMapInstanceId, int nextX, int nextY) {
        GameWorld.playerNextMap = nextMap;
        GameWorld.playerNextMapInstanceId = nextMapInstanceId;
        GameWorld.playerNextX = nextX;
        GameWorld.playerNextY = nextY;
    }
}