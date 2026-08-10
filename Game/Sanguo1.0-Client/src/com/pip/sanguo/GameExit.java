package com.pip.sanguo;

import javax.microedition.lcdui.Graphics;

import com.pip.common.Tool;
import com.pip.ui.VMGame;

public class GameExit extends GameSprite{
    public boolean touching = false;
    
    public GameExit(int type, int id, int instancId){
        super(type, id, instancId);
    }

    public static GameExit createGameExit(String name, int x, int y, int index){
        GameExit gameExit = new GameExit(Tool.SPRITE_TYPE_EXIT, index, GameWorld.currentMap.exitIDs[index]);
        gameExit.setName(name);
        gameExit.vm = VMGame.getVMGame("game_exit").getVM();
        gameExit.sendCommand(VMGame.GAME_COMMAND_CREATE_SPRITE, new int[]{
                        x, y
        });
        gameExit.sendCommand(VMGame.GAME_COMMAND_SPRITE_LOAD_ANIMATE, null);

        return gameExit;
    }

    public void cycle(){
        super.cycle();
        sprite.cycle();
    }

    public void draw(Graphics g, int viewX, int viewY){
        drawAnimate(g, viewX, viewY, true);
        sprite.draw(g, viewX, viewY);
        drawAnimate(g, viewX, viewY, false);
    }

    public boolean isHumanAnimate(){
        return false;
    }
}
