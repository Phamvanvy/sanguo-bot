package com.pip.sanguo;


import javax.microedition.lcdui.Graphics;

import com.pip.common.Tool;
import com.pip.io.UASegment;
import com.pip.ui.VM;
import com.pip.ui.VMGame;


public class GameNetPlayer extends GameSprite{
    public long lastSyncMoveTime;
    public boolean noNeedRemove; //队员或pk状态下，走出视野时是不需要删除的
    public boolean beSkiped; //是否被跳过
    
    public GameNetPlayer(int type, int id, int instanceId){
        super(type, id, instanceId);
    }

    public static GameNetPlayer createGameNetPlayer(int id, int instanceId){
        GameNetPlayer gameNetPlayer = new GameNetPlayer(Tool.SPRITE_TYPE_PLAYER, id, instanceId);

        gameNetPlayer.vm = VMGame.getVMGame("game_netplayer").getVM();
        gameNetPlayer.sendCommand(VMGame.GAME_COMMAND_CREATE_SPRITE, new Integer(instanceId));        
        gameNetPlayer.sendCommand(VMGame.GAME_COMMAND_SPRITE_LOAD_ANIMATE, null);
        gameNetPlayer.lastSyncMoveTime = System.currentTimeMillis();
        
        return gameNetPlayer;
    }
    
    public void vm_sprite_set_can_attack(boolean _canAttact){
        super.vm_sprite_set_can_attack(_canAttact);
        
        //立即设置红圈篮圈
        if(GameWorld.player.target == this){
            sendCommand(VMGame.GAME_COMMAND_SPRITE_TARGETED, new Integer(VM.TRUE));
        }
    }
    
    public void vm_player_set_no_need_remove(boolean _need){
        noNeedRemove = _need;
        
        if(noNeedRemove == false && isOutView) {
            GameWorld.doDestorySprite(this, false, false);
        }
    }
    
    public boolean vm_player_get_no_need_remove(){
        return noNeedRemove;
    }
    
    public boolean vm_player_get_be_skiped(){
        return beSkiped;
    }

    public void draw(Graphics g, int viewX, int viewY){
        drawAnimate(g, viewX, viewY, true);
        sprite.draw(g, viewX, viewY);
        drawAnimate(g, viewX, viewY, false);
    }

    public void cycle(){
    	super.cycle();
        sprite.cycle();
        this.setFollowersPosition();
        if(!noNeedRemove && System.currentTimeMillis() - lastSyncMoveTime > GameMain.dropNetplayerTime){
            GameWorld.requestDestorySprite(this);
        }
    }
    
    public boolean isHumanAnimate(){
        return true;
    }
}
