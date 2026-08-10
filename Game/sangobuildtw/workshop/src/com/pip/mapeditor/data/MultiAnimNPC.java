/**
 * 
 */
package com.pip.mapeditor.data;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author jhkang
 *
 */
public class MultiAnimNPC extends MapNPC {
	
	private ArrayList<MapNPC> children;
	
	public MultiAnimNPC(){
		super.animateSetRef = -1;
		super.animate = -1;
		children = new ArrayList<MapNPC>();
	}
	public void addNPC(Collection<MapNPC> npcs){
		children.addAll(npcs);
		update();
	}
	public void addNPC(MapNPC npc){
		children.add(npc);
		update();
	}
	public void update() {
		MapNPC topNpc = children.get(children.size() - 1);
		super.x = topNpc.x;
		super.y = topNpc.y;
	}
	public ArrayList<MapNPC> getChildren(){
		ArrayList<MapNPC> ret = new ArrayList<MapNPC>();
		ret.addAll(children);
		return ret;
	}
	public void setChildren(ArrayList<MapNPC> list){
		children = list;
	}
}
