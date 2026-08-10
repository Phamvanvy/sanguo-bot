package com.pip.gui;

import javax.microedition.lcdui.Graphics;

import com.pip.common.Tool;
import com.pip.common.Utilities;
import com.pip.image.ImageSet;
import com.pip.sanguo.GameIcon;
import com.pip.sanguo.GameSprite;
import com.pip.sanguo.GameWorld;
import com.pip.ui.VM;
import com.pip.ui.VMGame;

public class GGameIcon extends GWidget implements IGPaint{
	GameIcon gi;
	public boolean iconShow;
	
	public GGameIcon(VMGame _vmGame, int self, int[] vmData, String name) {
		super(_vmGame, self, vmData, name);				 
	}
	
	public GWidget getClone(VMGame _vmGame) {
		GGameIcon gGameIcon = new GGameIcon(_vmGame, 0, getVMDataCopy(), name);
		setCloneData(gGameIcon);
		gGameIcon.gi = gi;
		gGameIcon.iconShow = this.iconShow;
		
		return gGameIcon;
	}
	
	/**
	 * …Ë÷√gameicon
	 * 
	 * @param colors
	 * @param cornerResName
	 * @param index
	 */
	public void setData1(int _type, int _id, int _animateIndex){
        GameSprite father = (GameSprite)GameWorld.gameSpriteTable.get(Tool.getSpriteKey(_type, _id));
        
        setData2(father, _animateIndex);
        
	}
	
	public void setData2(GameSprite _processor, int _animateIndex){
        if(_processor != null){
        	if(gi != null) {
        		GameWorld.gameIcons.remove(new Integer(gi.getId()));
        	}
        	gi = GameIcon.createGameIcon(_processor, _animateIndex);

            if(gi != null){
            	GameWorld.gameIcons.put(new Integer(gi.getId()), gi);
            	int[] box = gi.sprite.getAnimateBox();
            	this.vmData[GW_VM_W] = box[2];
            	this.vmData[GW_VM_H] = box[3];
            }
        }
	}
	
	public GameIcon getGameIcon() {
		return gi;
	}
	
	public void setShow(boolean iconShow) {
		this.iconShow = iconShow;
		if(gi != null) {
			gi.sprite.setShow(iconShow);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.pip.gui.IGPaint#paint()
	 */
	public void paint() {
		if(iconShow && gi != null) {			
			gi.vm_set_icon_position(this.vmData[GW_VM_OFFSET_X] + this.vmData[GW_VM_XX] + this.vmData[GW_VM_W]/2, this.vmData[GW_VM_OFFSET_Y]+this.vmData[GW_VM_YY] + this.vmData[GW_VM_H]);
			gi.draw(Utilities.graphics, 0, 0);
		}
	}
	
}
