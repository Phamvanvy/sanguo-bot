package com.pip.sanguo;


import javax.microedition.lcdui.Graphics;
import com.pip.io.UASegment;
import com.pip.resource.ResourceManager;
import com.pip.ui.VM;
import com.pip.ui.VMGame;


public class GameNpc extends GameSprite{
    private String animateName;
    public int questId = -2; //只有资源NPC使用
    public boolean needCollision = false;
    public boolean animateRequested = false;

    public GameNpc(int type, int id, int instanceId){
        super(type, id, instanceId);
    }

    public static GameNpc createGameNpc(byte type, int id, int instanceId, int imageId){
        GameNpc gameNpc = new GameNpc(type, id, instanceId);
        gameNpc.vm = VMGame.getVMGame("game_npc").getVM();
        gameNpc.sendCommand(VMGame.GAME_COMMAND_CREATE_SPRITE, new Integer(instanceId));
        gameNpc.animateName = String.valueOf(imageId) + ResourceManager.POSTFIX_CTN;

        return gameNpc;
    }

    public void setImageId(int ImageId){
        animateName = String.valueOf(ImageId) + ResourceManager.POSTFIX_CTN;
        sendCommand(VMGame.GAME_COMMAND_SPRITE_LOAD_ANIMATE, null);
    }
    
    public void vm_sprite_set_can_attack(boolean _canAttact){
        super.vm_sprite_set_can_attack(_canAttact);
        
        //立即设置红圈篮圈
        if(GameWorld.player.target == this){
            sendCommand(VMGame.GAME_COMMAND_SPRITE_TARGETED, new Integer(VM.TRUE));
        }
    }

    public String vm_game_npc_get_animate_name(){
        return animateName;
    }
    
    public void vm_game_set_npc_image_id(int _imageId){
        setImageId(_imageId);
    }
    
    public void vm_game_set_npc_quest_id(int _questId){
        this.questId = _questId;
    }
    
    public int vm_game_get_npc_quest_id(){
        return questId;
    }
    
    public boolean vm_game_npc_is_human(){
        return isHumanAnimate();
    }
    
    public int vm_game_npc_get_animate_count(){
        if(animateName != null){
            return sprite.getAnimatePlayer(animateName).getAnimateCount();
        }else{
            return 0;
        }
    }
    
    public void vm_game_npc_set_need_collision(boolean _needCollision){
        this.needCollision = _needCollision;
    }

    public void draw(Graphics g, int viewX, int viewY){
        if(!animateRequested){
            sendCommand(VMGame.GAME_COMMAND_SPRITE_LOAD_ANIMATE, null);
            animateRequested = true;
        }
        
        drawAnimate(g, viewX, viewY, true);
        sprite.draw(g, viewX, viewY);
        drawAnimate(g, viewX, viewY, false);
    }

    public void cycle(){ 
    	super.cycle();
        sprite.cycle();
    }

    public boolean isHumanAnimate(){
        if(animateName != null){
            return sprite.isHumanAnimate(animateName);
        }else{
            return false;
        }
    }
}
