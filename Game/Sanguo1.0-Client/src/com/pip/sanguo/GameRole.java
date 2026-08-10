package com.pip.sanguo;

import java.io.IOException;

import javax.microedition.lcdui.Graphics;

import com.pip.common.Tool;
import com.pip.common.Utilities;
import com.pip.io.UASegment;
import com.pip.ui.Quest;
import com.pip.ui.VM;
import com.pip.ui.VMGame;

public class GameRole extends GameSprite{
    private int lastPostionTime;
    public GameSprite target; //TODO change to invisible
    public boolean autoSelect; //自动选择关闭
    public boolean battleMode; //是否在战斗状态
    public boolean needSendStop; //是否发过移动
    public boolean afterChangeTargetHasMoved; //强切目标后是否移动过
    public int positionLimit;
    private int lastX;
    private int lastY;
    private int pointMoveDir = Tool.DIR_NONE;

    public final boolean LAYOUT_2468_YES = true;
    public final boolean LAYOUT_2468_NO = false;
    // 2468键模式
    private boolean layout2468 = false;
    private boolean dirKeyValid = true; //方向键是否可用
    private boolean fireKeyValid = true; //fire键是否可用

    public static int enemyNpcAutoDist = 32;
    public static int allyNpcAutoDist = 16;
    public static int enemyPlayerAutoDist = 0;
    public static int allyPlayerAutoDist = 0;
    public static int teamerAutoDist = 0;
    public static int enemyNpcForceDist = 40;
    public static int allyNpcForceDist = 16;
    public static int enemyPlayerForceDist = 120;
    public static int allyPlayerForceDist = 0;
    public static int teamerForceDist = 0;
    
    public static final int SELECT_TYPE_ALL = 0;
    public static final int SELECT_TYPE_PK = 1;
    public static final String VAR_SELECT_MODE = "varSelectMode";
    public static final String VAR_WHO_LAST_ATTACKED_ME = "varLastAttacker";


    private GameRole(int type, int id){
        super(type, id, id);
    }

    public static GameRole createRole(int id, String name){
        GameRole gameRole = new GameRole(Tool.SPRITE_TYPE_ROLE, id);
        gameRole.setName(name);
        gameRole.vm = VMGame.getVMGame("game_role").getVM();
        gameRole.sendCommand(VMGame.GAME_COMMAND_CREATE_SPRITE, new Integer(id));
        gameRole.sendCommand(VMGame.GAME_COMMAND_SPRITE_LOAD_ANIMATE, null);
        gameRole.autoSelect = true;
        gameRole.infoRecved = true;
        gameRole.positionLimit = GameMain.positionLimit;

        GameMain.resourceManager.currentUpdateTable.clear();
        return gameRole;
    }

    public void vm_role_test_and_change_target(int enemyInstanceId){
        GameSprite enemySprite = GameWorld.getSprite(enemyInstanceId);

        if(enemySprite != null && enemySprite != this){
            if(target == null || !target.canAttack){
                changeTarget(enemySprite);
                autoSelect = false;
            }
        }
    }
    
	public static boolean isCivilPlayer(GameSprite sp){
		return GameWorld.player.faction == sp.faction && sp.getType() == Tool.SPRITE_TYPE_PLAYER && sp.canAttack == false;
	}
	
	public static boolean isSelectAllMode(){
		return Tool.getGlobalInt(GameRole.VAR_SELECT_MODE) == GameRole.SELECT_TYPE_ALL;
	}
	
	private void searchNextTarget(){
        autoSelect = false;

        if(GameWorld.gameView != null){
            if(afterChangeTargetHasMoved){
                GameSprite nearTarget = GameWorld.gameView.findNearTarget(false);
                if(nearTarget != target){
                    changeTarget(nearTarget);
                }else{
                    changeTarget(GameWorld.gameView.findNextTarget(target));
                }
            }else{
                changeTarget(GameWorld.gameView.findNextTarget(target));
            }
        }else{
            changeTarget(null);
        }

        afterChangeTargetHasMoved = false;		
	}
	
	/**
	 * 长按*键后，自动选中最近的NPC
	 */
	public void searchNearestNpc(){
		autoSelect = false;

        if(GameWorld.gameView != null){
            GameSprite nearTarget = GameWorld.gameView.findNearNPC();
            if(nearTarget != null){
                changeTarget(nearTarget);
            }else{
                changeTarget(GameWorld.gameView.findNextTarget(target));
            }
        }else{
            changeTarget(null);
        }

        afterChangeTargetHasMoved = false;	
	}
	
	/**
	 * 使用技能时自动攻击最近的可攻击目标
	 */
	public GameSprite selectNearestCreature(){
		autoSelect = false;
		afterChangeTargetHasMoved = false;
		GameSprite nearTarget = null;
        if(GameWorld.gameView != null){
            nearTarget = GameWorld.gameView.findNearCreature();
            if(nearTarget != null){
                changeTarget(nearTarget);
            }
        }else{
            changeTarget(null);
        }
        return nearTarget;
	}

    public void vm_role_change_target(){
    	//#if NewUI2
    	/*目标为可攻击目标（敌对玩家，敌方可攻击NPC,怪物，），
		 * 受到攻击，不自动切换目标。当玩家使用切换键目标将切换到攻击者身上。*/
		int lastAttackedMeInstId = Tool.getGlobalInt(VAR_WHO_LAST_ATTACKED_ME);
		if(lastAttackedMeInstId != 0){
			Tool.deleteGlobalVar(VAR_WHO_LAST_ATTACKED_ME);
			vm_role_test_and_change_target(lastAttackedMeInstId);
		} else {
			searchNextTarget();
		}
    	//#else
    	//# if(isSelectAllMode()){//全部目标模式
		//# 	searchNextTarget();
		//# } else {//pk模式
    		/*目标为可攻击目标（敌对玩家，敌方可攻击NPC,怪物，），
    		 * 受到攻击，不自动切换目标。当玩家使用切换键目标将切换到攻击者身上。*/
		//# 	int lastAttackedMeInstId = Tool.getGlobalInt(VAR_WHO_LAST_ATTACKED_ME);
		//# 	if(lastAttackedMeInstId != 0){
		//# 		Tool.deleteGlobalVar(VAR_WHO_LAST_ATTACKED_ME);
		//# 		vm_role_test_and_change_target(lastAttackedMeInstId);
		//# 	} else {
		//# 		searchNextTarget();
		//# 	}
		//# }
    	//#endif
    	
    }

    public void vm_role_set_auto_select(boolean _autoSelect){
        this.autoSelect = _autoSelect;
    }

    public int vm_role_get_target_type(){
        if(target != null){
            return target.getType();
        }else{
            return -1;
        }
    }

    public int vm_role_get_target_id(){
        if(target != null){
            return target.getId();
        }else{
            return -1;
        }
    }

    public String vm_role_get_target_name(){
        if(target != null){
            return target.getName();
        }else{
            return null;
        }
    }

    public GameSprite vm_role_get_target(){
        return target;
    }

    public int vm_role_get_target_instanceid(){
        if(target != null){
            return target.getInstanceId();
        }else{
            return -1;
        }
    }

    public void vm_set_game_role_change_2468_mode(boolean yesorno){
        this.layout2468 = yesorno;
    }

    public boolean vm_sprite_get_dir_key_valid(){
        return dirKeyValid;
    }

    public void vm_sprite_set_dir_key_valid(boolean _dirKeyValid){
        this.dirKeyValid = _dirKeyValid;
    }

    public boolean vm_sprite_get_fire_key_valid(){
        return fireKeyValid;
    }

    public void vm_sprite_set_fire_key_valid(boolean _fireKeyValid){
        this.fireKeyValid = _fireKeyValid;
    }

    public void vm_game_role_set_battle_mode(boolean _inBattle){
        battleMode = _inBattle;

        if(battleMode){
            positionLimit = GameMain.battleModePositionTime;
        }else{
            positionLimit = GameMain.positionLimit;
        }
    }

    public void vm_role_clear_target(){
        clearTarget();
    }

    public void vm_game_role_set_target(int _instanceId){
        GameSprite targetSprite = GameWorld.getSprite(_instanceId);
        
        if(GameWorld.gameView != null){
        	targetSprite = GameWorld.gameView.checkTarget(targetSprite);
        }

        changeTarget(targetSprite);
    }

    public void vm_game_role_set_select_const(int enemyNpcAutoDist, int allyNpcAutoDist, int enemyPlayerAutoDist, int allyPlayerAutoDist, int teamerAutoDist, int enemyNpcForceDist,
                    int allyNpcForceDist, int enemyPlayerForceDist, int allyPlayerForceDist, int teamerForceDist){
        GameRole.enemyNpcAutoDist = enemyNpcAutoDist;
        GameRole.allyNpcAutoDist = allyNpcAutoDist;
        GameRole.enemyPlayerAutoDist = enemyPlayerAutoDist;
        GameRole.allyPlayerAutoDist = allyPlayerAutoDist;
        GameRole.teamerAutoDist = teamerAutoDist;
        GameRole.enemyNpcForceDist = enemyNpcForceDist;
        GameRole.allyNpcForceDist = allyNpcForceDist;
        GameRole.enemyPlayerForceDist = enemyPlayerForceDist;
        GameRole.allyPlayerForceDist = allyPlayerForceDist;
        GameRole.teamerForceDist = teamerForceDist;
    }
    
    public void vm_game_role_point_move(int pointMoveDir){
        this.pointMoveDir = pointMoveDir;
    }

    public void draw(Graphics g, int viewX, int viewY){
        drawAnimate(g, viewX, viewY, true);
        sprite.draw(g, viewX, viewY);
        drawAnimate(g, viewX, viewY, false);
    }

    public void changeTarget(GameSprite targetSprite){
        if(targetSprite != target){
            int sendType = -1;
            int sendId = -1;

            if(targetSprite != null){
                GameSprite targetSprite2 = (GameSprite) targetSprite;
                //#if ModelID == AndroidAuto
	               //# if (!GameMain.getUIModel().equals(GameMain.ANDROID_SMALL))
                  //# {
                    //# if(targetSprite2.getType() == Tool.SPRITE_TYPE_GATHER_NPC || targetSprite2.getType() == Tool.SPRITE_TYPE_PLAYER || targetSprite2.getType() == Tool.SPRITE_TYPE_NPC)
                    //# {
                    //# sendType = targetSprite2.getType();
                    //# sendId = targetSprite2.getId();	
		            //# }
                  //# }
                   //# else
                  //# {
                    //# if(targetSprite2.getType() == Tool.SPRITE_TYPE_PLAYER || targetSprite2.getType() == Tool.SPRITE_TYPE_NPC)
                    //# {
                     //# sendType = targetSprite2.getType();
                     //# sendId = targetSprite2.getId();     	
                    //# }
                  //# }
                //#else
                switch(targetSprite2.getType()){
	                //#if ModelID == iPhone || ModelID == NokiaS60V5 || ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == Android	
	            	//# case Tool.SPRITE_TYPE_GATHER_NPC:
	                //#endif    
	            	case Tool.SPRITE_TYPE_PLAYER:
                    case Tool.SPRITE_TYPE_NPC:
                        sendType = targetSprite2.getType();
                        sendId = targetSprite2.getId();
//# 
                        break;
                }
                //#endif
            }

            GameWorld.panel.sendCommand(VMGame.GAME_COMMAND_PANEL_CHANGE_TARGET, new int[]{
                            sendType, sendId
            });

            if(target != null){
                target.sendCommand(VMGame.GAME_COMMAND_SPRITE_TARGETED, new Integer(VM.FALSE));
            }

            if(targetSprite != null){
                targetSprite.sendCommand(VMGame.GAME_COMMAND_SPRITE_TARGETED, new Integer(VM.TRUE));
            }

            target = targetSprite;
        }
    }

    public void cycle(){
        super.cycle();
        sprite.cycle();

        if(forceWayPointMode || VMGame.state != VMGame.STATE_IDLE){
            processNotifyServer(sprite.getMove(), this.onHorse, sprite.getDir());
        }else{
            int newDir = sprite.getDir();
            boolean newMoving = false;
            boolean newOnHorse = onHorse;
            boolean newHolding = hold;

            if(this.dirKeyValid){ //跟随状态下，以下按键无效
                if(((Utilities.isKeyPressed(Utilities.KEY_NUM8_PRESSED, false) || pointMoveDir == Tool.DIR_DOWN) && this.layout2468) || Utilities.isKeyPressed(Utilities.DOWN_PRESSED, false)){
                    newDir = Tool.DIR_DOWN;
                    newMoving = true;
                }else if(((Utilities.isKeyPressed(Utilities.KEY_NUM4_PRESSED, false) || pointMoveDir == Tool.DIR_LEFT) && this.layout2468) || Utilities.isKeyPressed(Utilities.LEFT_PRESSED, false)){
                    newDir = Tool.DIR_LEFT;
                    newMoving = true;
                }else if(((Utilities.isKeyPressed(Utilities.KEY_NUM6_PRESSED, false) || pointMoveDir == Tool.DIR_RIGHT) && this.layout2468) || Utilities.isKeyPressed(Utilities.RIGHT_PRESSED, false)){
                    newDir = Tool.DIR_RIGHT;
                    newMoving = true;
                }else if(((Utilities.isKeyPressed(Utilities.KEY_NUM2_PRESSED, false) || pointMoveDir == Tool.DIR_UP) && this.layout2468) || Utilities.isKeyPressed(Utilities.UP_PRESSED, false)){
                    newDir = Tool.DIR_UP;
                    newMoving = true;
                }
            }

            if(this.fireKeyValid && Utilities.isKeyPressed(Utilities.FIRE_PRESSED, true)){
                if(target != null){
                    GameSprite spriteTarget = (GameSprite) target;
                    spriteTarget.sendCommand(VMGame.GAME_COMMAND_SPRITE_FIRE, null);
                }
            }

            if(newMoving){
                afterChangeTargetHasMoved = true;
            }

            if(sprite.getMove() || chaseMode){
                processTargetSelect();
            }

            if(newMoving && chaseMode){
                int[] callBackPara = chaseCallbackPara;
                clearChase();

                //用于寻路被中断
                if(callBackPara != null){
                    sendCommand(VMGame.GAME_COMMAND_SPRITE_CHASE_STOP, callBackPara);
                }
            }

            if(newMoving && !sprite.getMove() && this.dirKeyValid){
                GameWorld.panel.sendCommand(VMGame.GAME_COMMAND_ROLE_MOVE, null);
            }

            processNotifyServer(newMoving, newOnHorse, newDir);
            processAction(newDir, newMoving, newOnHorse, newHolding);
        }
    }

    public void processTargetSelect(){
        if(target != null && target != this && !target.die){
            if(Tool.distance(target.sprite.getX(), target.sprite.getY(), sprite.getX(), sprite.getY()) > GameMain.lostSelectDistance){
                changeTarget(null);
                autoSelect = true;
            }
        }else{
            autoSelect = true;
        }

        if(autoSelect){
            GameSprite targetSprite = null;

            if(GameWorld.gameView != null){
                targetSprite = GameWorld.gameView.findNearTarget(true);
            }

            changeTarget(targetSprite);
        }
    }

    public void clearTarget(){
        if(target != null){
            GameWorld.panel.sendCommand(VMGame.GAME_COMMAND_PANEL_CHANGE_TARGET, new int[]{
                            -1, -1
            });

            target.sendCommand(VMGame.GAME_COMMAND_SPRITE_TARGETED, new Integer(VM.FALSE));
            target = null;
        }

        autoSelect = true;
    }

    public void changeHorse(UASegment segment){
        boolean newOnHorse = segment.readBoolean();

        processNotifyServer(false, newOnHorse, sprite.getDir());
        processAction(sprite.getDir(), false, newOnHorse, hold);
    }

    public void processNotifyServer(boolean newMoving, boolean newOnHorse, int newDir){
        int[] newPos = sprite.getPosition(0);
        int dx = Math.abs(newPos[1] - lastX);
        int dy = Math.abs(newPos[2] - lastY);
        boolean needSend = false;

        if(newMoving != sprite.getMove()){
            if(newMoving){
                this.state |= 0x1;
            }else{
                this.state &= 0xFFFE;
            }
        }

        if(newOnHorse != this.onHorse){
            if(newOnHorse){
                this.state = this.state | 0x4;
            }else{
                this.state = this.state & 0xFFFB;
            }

            Tool.sendHorseAction(newDir, newPos[1], newPos[2], newOnHorse);
            this.lastPostionTime = Utilities.getTimeStamp();
        }

        if(this.isFollowing && (Utilities.getTimeStamp() - lastPostionTime) > GameMain.followingNotifyServerTime){
            needSend = true;
        }else if((dx > GameMain.positionDistance || dy > GameMain.positionDistance || (Utilities.getTimeStamp() - lastPostionTime) > GameMain.positionTime || sprite.getMove() != newMoving || sprite
                        .getDir() != newDir)
                        && (Utilities.getTimeStamp() - lastPostionTime) > positionLimit){
            needSend = true;
        }

        if(needSendStop && !newMoving){
            needSend = true;
        }

        if(needSend){
            if(newMoving){
                needSendStop = true;
            }else{
                needSendStop = false;
            }
            
            //跟随时，没有移动到卡死位置时才发postion(但是防外挂规则要求两次sendpos间隔不大于4sec)
            //故将GameWorld.gameView.canMove(sprite.getX(), sprite.getY())限制去掉
            if(this.followOwner != null) {
//            	if(GameWorld.gameView.canMove(sprite.getX(), sprite.getY())) {
            		Tool.sendPosition(newDir, newPos[1], newPos[2], state);	
//            	}            	
            } else {
            	Tool.sendPosition(newDir, newPos[1], newPos[2], state);
            }
            
            Quest.setEventMask(Quest.EVENT_MASK_POSITION);
            this.lastPostionTime = Utilities.getTimeStamp();
            lastX = newPos[1];
            lastY = newPos[2];
        }
    }

    public boolean isHumanAnimate(){
        return true;
    }
}
