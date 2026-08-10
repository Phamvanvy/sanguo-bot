package com.pip.itimes.server.world;

import java.io.File;
import java.util.*;

import org.apache.log4j.Logger;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPData;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.ITimesException;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.Door;
import com.pip.itimes.server.stage.MonsterGroup;
import com.pip.itimes.server.stage.Npc;
import com.pip.itimes.server.stage.Scene;
import com.pip.itimes.server.stage.WorldMap;
import com.pip.itimes.server.world.trace.BasicPassData;
import com.pip.itimes.server.world.trace.Entrices;
import com.pip.itimes.server.world.trace.InterestedPoint;
import com.pip.itimes.server.world.trace.Teleport;
import com.pip.itimes.server.world.trace.WorldNavigation;
import com.pip.itimes.server.world.trace.SimplePoint;

public class AutoTraceService {
    private static final Logger log = Logger.getLogger(AutoTraceService.class);
//    HashMap<Integer,MapPath> maps = new HashMap<Integer,MapPath>();
    private ConnectService connectService;
    protected ChatService chatService;
    private File pkgDir;
    public HashMap<Integer,BasicPassData> passData = new HashMap<Integer,BasicPassData>();
    public AutoTraceService(File pkgDir) throws Exception{
        this.pkgDir = pkgDir;
        reloadData();
    }
    public void reloadData() {
    	passData.clear();
    	File ddir = new File(pkgDir, "Areas/pass");
    	for (File file : ddir.listFiles()) {
    		String s = file.getName();
    		if (s.endsWith(".xml")) {
    			try {
    				if (s.equals("Highway.xml")) {
    					WorldNavigation.load(file);
    				} else {
	    				int id = Integer.parseInt(s.substring(0, s.length() - 4));
	    				BasicPassData data = new BasicPassData();
	    				data.load(file);
	    				passData.put(Integer.valueOf(data.id), data);
    				}
    			} catch (NumberFormatException e) {
    				e.printStackTrace();
    			}
    		}
    	}
    	
    	
    }
    public void setConnectService(ConnectService connectService){
        this.connectService = connectService;
    }
    public void setChatService(ChatService chatService){
        this.chatService = chatService;
    }
    
    public void requestGotoTrace(UWAPData data, WorldPlayer player) throws Exception{
    	Scene scene = player.getMap().getScene();
    	int x = player.getX();
    	int y = player.getY();
    	int mapId = scene.getMapId();
		BasicPassData bpd = passData.get(Integer.valueOf(mapId));
		int ySft = (bpd.getTh() == 16? 4: 3);
		if (bpd == null) {
			//chatService.sendNewMessage("附近未开通导航系统", player.getId());
			//return;
			throw new ITimesException("附近未开通导航系统", data.getSerial(), data.getSessionId(),
                    data.getAppType());
		}
    	
		int id = data.readInt();
		int targetx = data.readShort();
		int targety = data.readShort();

		ArrayList<InterestedPoint> points = bpd.getTargetPoints();
		InterestedPoint telePoint = null;
		int tx = -1;
		int ty = -1;
		if (id != mapId) {
			int tk = WorldNavigation.getNearCityIdTo(mapId, id);
			for (InterestedPoint p : points) {
	    		if (p instanceof Entrices && ((Entrices)p).targetMapId == tk) {
	    			tx = p.x << 4;
	    			ty = p.y << ySft;
	    			telePoint = p;
	    			break;
	    		} else if (p instanceof Teleport && ((Teleport)p).targetMapId == tk) {
	    			tx = p.x << 4;
	    			ty = p.y << ySft;
	    			telePoint = p;
	    			break;
	    		}
	    	}
		} else {
			tx = targetx;
			ty = targety;
		}
    	if (tx == -1) {
    		throw new ITimesException("您找的目标不在导航范围内", data.getSerial(), data.getSessionId(),
                    data.getAppType());
    		//chatService.sendNewMessage("您找的目标不在导航范围内。", player.getId());
    		
    	} else {
    		ArrayList<SimplePoint> ret = null;
    		
    		try{
    		    ret = bpd.getRoad(x, y, tx, ty);
    		}catch(Throwable e){
    		    log.error("ID[" + player.getId() + "] Find Path SourceMapId[" + mapId + "] SourceX[" + x + "] SourceY[" + y + "] TargetMapId[" + id + "] TargetX[" + tx + "] TargetY[" + ty + "] Error.", e);
    		}
    		
    		if (ret == null || ret.size() == 0) {
    			
    			//chatService.sendNewMessage("未找到路径，可能是您站的位置有屏蔽。", player.getId());
    			throw new ITimesException("请手动移动到其他坐标点后再尝试寻路。", data.getSerial(), data.getSessionId(),
                        data.getAppType());
    		} else {
    			SimplePoint firstPoint = ret.get(0);
    			if (firstPoint.x == x && firstPoint.y == y) {
    				// 可能进入死循环
    				//chatService.sendNewMessage("朋友，就是这里了哦!!", player.getId());
    				throw new ITimesException("朋友，就是这里了哦!!", data.getSerial(), data.getSessionId(),
                            data.getAppType());
    			} else {
					UWAPSegment seg = new UWAPSegment(ClientConstants.SEND_WAYPOSITION,data.getSerial(), data.getSessionId());
//					if (mp != mapId) {
//						ret.add(new SimplePoint(0x8000 | (id >> 16), id & 0xffff));
//					}
		    		seg.write((byte)(ret.size() + 1));
		    		seg.writeShort((short)x);
		    		seg.writeShort((short)y);
		    		for (SimplePoint p : ret) {
		                seg.writeShort((short)p.x);
		                seg.writeShort((short)p.y);
		    		}
		    		if (id != mapId && telePoint != null) {
		    			seg.write((byte)1);
		    			if (telePoint instanceof Entrices) {
		    				Entrices e = (Entrices)telePoint;
		    				seg.writeShort((short)e.targetMapId);
		    				seg.writeShort((short)e.tx);
		    				seg.writeShort((short)e.ty);
		    			} else if (telePoint instanceof Teleport) {
		    				Teleport t = (Teleport)telePoint;
		    				seg.writeShort((short)t.targetMapId);
		    				seg.writeShort((short)t.tx);
		    				seg.writeShort((short)t.ty);
		    			}
		    		} else {
		    			seg.write((byte)0);
		    		}
		    		connectService.writeTo(seg, player.getId());
    			}
    		}
    	}
    }
}
