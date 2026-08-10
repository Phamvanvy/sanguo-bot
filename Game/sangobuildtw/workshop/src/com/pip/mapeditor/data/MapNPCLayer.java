package com.pip.mapeditor.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Rectangle;
import org.jdom.*;

import com.pip.mapeditor.MapEditor;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateSet;

/**
 * 放置地图NPC的层。一个地图NPC是地图动画文件中的一个动画序列。
 * @author lighthu
 */
public class MapNPCLayer implements IMapLayer {
	/** 所属地图 */
	public GameMap parent;
    /** 地图层名称 */
    private String name;
	/** 地图NPC列表 */
	private ArrayList<MapNPC> npcs;
	
	private boolean forceAddOrderDraw;
	
    /**
	 * 缺省构造方法。
	 * @param parent 所属地图，地图的基本信息必须已经确定。
	 */
	public MapNPCLayer(GameMap parent) {
		this.parent = parent;
		npcs = new ArrayList<MapNPC>();
	}
	
	/**
     * 设置所属地图。
     */
    public void setParent(GameMap parent) {
    	this.parent = parent;
    }
    
    /**
     * 复制。
     */
    public IMapLayer dup() {
    	MapNPCLayer ret = new MapNPCLayer(parent);
    	ret.name = name;
    	for (MapNPC npc : npcs) {
    		ret.npcs.add(npc.dup());
    	}
    	return ret;
    }
    
    /**
     * 按指定范围剪裁。
     */
    public void cut(int x, int y, int w, int h) {
    	PipAnimateSet animates = parent.parent.getAnimates();
		PipAnimate animate;
		for (int i = 0; i < npcs.size(); i++) {
			MapNPC npc = (MapNPC)npcs.get(i);
	    	if (parent.parent.isLibMode) {
	    		animate = parent.parent.getAnimate(npc.animateSetRef, npc.animate);
	    	} else {
	    		animate = animates.getAnimate(npc.animate);
	    	}
	    	Rectangle rect = animate.getBounds();
	    	rect.x += npc.x;
	    	rect.y += npc.y;
	    	if (rect.intersects(x, y, w, h)) {
	    		npc.x -= x;
	    		npc.y -= y;
	    	} else {
	    		npcs.remove(i);
	    		i--;
	    	}
		}
    }
	
    /**
     * 从XML文档中载入
     * @param elem
     */
    public void load(Element elem) {
        name = elem.getAttributeValue("name");
        List npcList = elem.getChildren("npc");
        for (int i = 0; i < npcList.size(); i++) {
            Element npcElem = (Element)npcList.get(i);
            MapNPC newNPC = createMapNPCFromNode(npcElem);
            npcs.add(newNPC);
        }
    }
    private MapNPC createMapNPCFromNode(Element npcElem){
    	int animateId = Integer.parseInt(npcElem.getAttributeValue("animate"));
    	int animateSetRefId = 0;
        if(npcElem.getAttributeValue("animateRef")!=null){
        	animateSetRefId = Integer.parseInt(npcElem.getAttributeValue("animateRef"));
		}
    	MapNPC newNPC = null;
    	if(animateSetRefId == -1){
    		newNPC = new MultiAnimNPC();
    		List npcList = npcElem.getChildren("npc");
            for (int i = 0; i < npcList.size(); i++) {
                Element elem = (Element)npcList.get(i);
                MapNPC mNpc = createMapNPCFromNode(elem);
                ((MultiAnimNPC)newNPC).addNPC(mNpc);
            }
    	}else{
    		newNPC = new MapNPC();
    	}
        newNPC.animate = animateId;
        newNPC.animateSetRef = animateSetRefId;
        newNPC.x = Integer.parseInt(npcElem.getAttributeValue("x"));
        newNPC.y = Integer.parseInt(npcElem.getAttributeValue("y"));
        return newNPC;
    }
    /**
     * 保存为XML文档
     * @return
     */
    public void save(Element elem) {
        elem.addAttribute("name", name);
        for (MapNPC npc : npcs) {
            Element elem1 = createNpcNode(npc);
            elem.getMixedContent().add(elem1);
            if(npc instanceof MultiAnimNPC){
            	for(MapNPC mNpc:((MultiAnimNPC) npc).getChildren()){
            		elem1.getMixedContent().add(createNpcNode(mNpc));
            	}
            }
        }
    }

    private Element createNpcNode(MapNPC npc) {
    	Element elem1 = new Element("npc");
        elem1.addAttribute("animateRef", String.valueOf(npc.animateSetRef));
        elem1.addAttribute("animate", String.valueOf(npc.animate));
        elem1.addAttribute("x", String.valueOf(npc.x));
        elem1.addAttribute("y", String.valueOf(npc.y));
        return elem1;
	}

	/**
     * 取得地图层中的NPC列表。
     * @return
     */
    public ArrayList<MapNPC> getNpcs() {
        return npcs;
    }
    
    /**
     * 处理动画序列被删除事件。
     * @param animate 被删除的动画序列的ID
     */
    public void onAnimateRemoved(int animate) {
        for (int i = npcs.size() - 1; i >= 0; i--) {
            MapNPC npc = npcs.get(i);
            if (npc.animate == animate) {
                npcs.remove(i);
            } else if (npc.animate > animate) {
                npc.animate--;
            }
        }
    }
    
    public void onAnimateInsert(int animate) {
        for (int i = npcs.size() - 1; i >= 0; i--) {
            MapNPC npc = npcs.get(i);
            if (npc.animate >= animate) {
                npc.animate++;
            }
        }
    }

    /**
     * 处理动画序列前移事件。
     * @param animate 被前移的动画序列的ID
     */
    public void onAnimateMoveUp(int animate) {
        for (int i = npcs.size() - 1; i >= 0; i--) {
            MapNPC npc = npcs.get(i);
            if (npc.animate == animate) {
                npc.animate = animate - 1;
            } else if (npc.animate == animate - 1) {
                npc.animate = animate;
            }
        }
    }

    /**
     * 获得层名称。
     */
    public String getName() {
        return name;
    }

    /**
     * 设置层名称。
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * 获得层的npc数量
     */
    public int getLayerCount(){
        return npcs.size();
    }
    
    /**
     * 放大一倍。
     */
    public void enlarge() {
        for (MapNPC npc : npcs) {
        	if (npc instanceof MultiAnimNPC) {
        		MultiAnimNPC mnpc = (MultiAnimNPC)npc;
        		for (MapNPC child : mnpc.getChildren()) {
        			child.x *= 2;
        			child.y *= 2;
        		}
        	} else {
        		npc.x *= 2;
        		npc.y *= 2;
        	}
        }
    }

    /**
     * 缩小一倍。
     */
    public void smaller() {
        for (MapNPC npc : npcs) {
        	if (npc instanceof MultiAnimNPC) {
        		MultiAnimNPC mnpc = (MultiAnimNPC)npc;
        		for (MapNPC child : mnpc.getChildren()) {
        			child.x /= 2;
        			child.y /= 2;
        		}
        	} else {
        		npc.x /= 2;
        		npc.y /= 2;
        	}
        }
    }

    public void onAnimateRefRemoved(int refResHashCode) {
		 int mvCnt = filtering(npcs, refResHashCode);
		 System.out.println("MapNPCLayer.onAnimateRefRemoved() removed npc:"+mvCnt);
	}
    
    private int filtering(ArrayList<MapNPC> list, int refResHashCode){
    	int removeCnt = 0;
    	for (int i = list.size() - 1; i >= 0; i--) {
    		MapNPC npc = list.get(i);
    		if(npc instanceof MultiAnimNPC){
				ArrayList<MapNPC> clist = ((MultiAnimNPC)npc).getChildren();
				int mvCnt = filtering(clist, refResHashCode);
				System.out.println("MapNPCLayer.filtering() removed child npc:"+mvCnt);
				if(clist.size()==0){
					list.remove(i);
				}
				((MultiAnimNPC)npc).setChildren(clist);
				continue;
			}
    		if (npc.animateSetRef == refResHashCode) {
				list.remove(i);
				removeCnt ++;
			}
    	}
    	return removeCnt;
    }

	public boolean getForceAddOrderDraw() {
		return forceAddOrderDraw;
	}

	public void setForceAddOrderDraw(boolean forceAddOrderDraw) {
		this.forceAddOrderDraw = forceAddOrderDraw;
	}
}
