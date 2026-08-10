package com.pip.sanguo;

import javax.microedition.lcdui.Graphics;

import com.pip.common.Tool;
import com.pip.common.Utilities;
import com.pip.engine.AnimatePlayer;
import com.pip.io.UASegment;
import com.pip.ui.VMGame;

public class GameIcon extends GameSprite{
    private int[] clip;
    private AnimatePlayer animatePlayer;
    private boolean playAnimate;
    private GameSprite father;
    
    private static final Tool idKey = new Tool(); 
    
    public GameIcon(int type, int id){
        super(type, id, -2);        
    }
    
    public static GameIcon createGameIcon(GameSprite father, int animateIndex){
        if(father == null || father.sprite == null || father.sprite.getIconAnimate() == null){
            return null;
        }
        
        GameIcon gameIcon = new GameIcon(Tool.SPRITE_TYPE_ICON, idKey.nextKey());
        gameIcon.vm = VMGame.getVMGame("game_icon").getVM();
        
        gameIcon.setName(father.getName());
        gameIcon.father = father;
        gameIcon.playAnimate = true;
        gameIcon.animatePlayer = father.sprite.getIconAnimate();
        gameIcon.sprite.addAnimate(gameIcon.animatePlayer.getCopy());
        gameIcon.sprite.setAnimateIndex(gameIcon.animatePlayer.getAnimateName(), animateIndex, Tool.ANIMATE_PLAY_TYPE_ALWAYS, Tool.NO_CALL_BACK, null, true);
        gameIcon.sprite.showAnimate(gameIcon.animatePlayer.getAnimateName());
        
        gameIcon.sprite.setPosition(0, 0);
        return gameIcon;
    }

    public void vm_free_icon(){
        GameWorld.gameIcons.remove(new Integer(getId()));
    }
    
    public void vm_set_icon_position(int x, int y){
        sprite.setPosition(x, y);
    }
    
    public void vm_set_icon_clip(int x, int y, int w, int h){
        clip = new int[]{
                        x, y, w, h
        };
    }
    
    public void vm_set_icon_show(boolean show){
        sprite.setShow(show);
    }
    
    public void vm_set_icon_index(int index){
        sprite.setAnimateIndex(animatePlayer.getAnimateName(), index, Tool.ANIMATE_PLAY_TYPE_ALWAYS, Tool.NO_CALL_BACK, null, true);
    }
    
    public void vm_set_icon_play_animate(boolean playAnimate){
        this.playAnimate = playAnimate;
    }
    
    public int vm_get_icon_id(){
        return getId();
    }
    
    public GameSprite vm_get_icon_father(){
        return father;
    }

    public void cycle(){
        if(playAnimate){
            sprite.cycle();
        }
    }
    
    public void drawImageIcon(Graphics g, int x, int y){
    	int clipX = 0;
		int clipY = 0;
		int clipW = 0;
		int clipH = 0;
        if(clip != null){
        	clipX = g.getClipX();
			clipY = g.getClipY();
			clipW = g.getClipWidth();
			clipH = g.getClipHeight();
            g.setClip(clip[0], clip[1], clip[2], clip[3]);
        }
        
        animatePlayer.drawSingleFrame(g, 0, x, y);
        
        if(clip != null){
            g.setClip(clipX, clipY, clipW, clipH);
        }
    }

    public void draw(Graphics g, int viewX, int viewY){
    	int clipX = 0;
		int clipY = 0;
		int clipW = 0;
		int clipH = 0;
        if(clip != null){
        	clipX = g.getClipX();
			clipY = g.getClipY();
			clipW = g.getClipWidth();
			clipH = g.getClipHeight();
             int[] animateBox = sprite.getAnimateBox();
             g.setClip(clip[0] + animateBox[0], clip[1] + animateBox[1], clip[2], clip[3]);
        }
        
        sprite.draw(g, viewX, viewY);
        
        if(clip != null){
            g.setClip(clipX, clipY, clipW, clipH);
        }
    }

    public boolean isHumanAnimate(){
        return father.isHumanAnimate();
    }
}
