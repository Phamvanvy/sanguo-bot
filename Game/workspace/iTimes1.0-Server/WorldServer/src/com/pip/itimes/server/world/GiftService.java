package com.pip.itimes.server.world;


import java.util.Date;
import java.util.Vector;

import javax.swing.text.Segment;

import org.apache.log4j.Logger;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.bean.Gift;
import com.pip.itimes.server.bean.Master;
import com.pip.itimes.server.dao.DataAccessException;
import com.pip.itimes.server.dao.GiftDao;
import com.pip.itimes.server.gift.GiftData;
import com.pip.itimes.server.gift.GiftGroup;
import com.pip.itimes.server.gift.GiftGroups;
import com.pip.itimes.server.gift.OnlyGiftGroup;
import com.pip.itimes.server.gift.OnlyGiftGroups;


/**
 * @author leo
 * @version 1.0
 */
public class GiftService{
    private static final Logger log = Logger.getLogger(GiftService.class);
    private GiftDao dao;
    
    private StageService stageService;

    public GiftService(GiftDao dao) throws Exception{
        this.dao = dao;
    }
    
    public void setStageService(StageService stageService){
        this.stageService = stageService;
    }
    //常规奖品发放 jwp add start
    public GiftData getPlayerOnlyGift(int groupId, WorldPlayer player){
        GiftData[] result = getPlayerOnlyGifts(new int[]{groupId}, player);
        
        if(result != null && result.length > 0){
            return result[0];
        }else{
            return null;
        }
    }
    
    public GiftData[] getPlayerOnlyGifts(int[] groupIds, WorldPlayer player){
        try{
            Vector<GiftData> gData = new Vector<GiftData>();
            
            for(int i = 0; i < groupIds.length; i++){
                OnlyGiftGroup giftGroup = OnlyGiftGroups.getOnlyGiftGroup(groupIds[i]);
                
                if(giftGroup == null){
                    return null;
                }
                GiftData giftData = player.getGiftData(groupIds[i]);
                
                if(giftData == null){
                    Gift gift = dao.getGift(groupIds[i], player.getId());
                    
                    if(gift == null){
                        gift = createGift(groupIds[i], player.getId());
                    }
                    
                    giftData = new GiftData(gift, giftGroup.getId());
                    player.addOnlyGiftData(giftData);
                }
                
                gData.add(giftData);
            }
            
            GiftData[] result = new GiftData[gData.size()];
            gData.toArray(result);
            
            return result; 
        }catch(DataAccessException e){
            log.error(e, e);
        }
        
        return null;
    }
    //jwp add end
  //mengjie add  按照账户id来判断发奖
    public GiftData getPlayerOnlyGiftbyaccountid(int groupId, WorldPlayer player,int accountid){
        GiftData[] result = getPlayerOnlyGiftsbyaccountid(new int[]{groupId}, player,accountid);
        
        if(result != null && result.length > 0){
            return result[0];
        }else{
            return null;
        }
    }
    
    public GiftData[] getPlayerOnlyGiftsbyaccountid(int[] groupIds, WorldPlayer player,int accountid){
        try{
            Vector<GiftData> gData = new Vector<GiftData>();
            
            for(int i = 0; i < groupIds.length; i++){
                OnlyGiftGroup giftGroup = OnlyGiftGroups.getOnlyGiftGroup(groupIds[i]);
                
                if(giftGroup == null){
                    return null;
                }
                GiftData giftData = player.getGiftData(groupIds[i]);
                
                if(giftData == null){
                    Gift gift = dao.getGift(groupIds[i], accountid);
                    
                    if(gift == null){
                        gift = createGift(groupIds[i], accountid);
                    }
                    
                    giftData = new GiftData(gift, giftGroup.getId());
                    player.addOnlyGiftData(giftData);
                }
                
                gData.add(giftData);
            }
            
            GiftData[] result = new GiftData[gData.size()];
            gData.toArray(result);
            
            return result; 
        }catch(DataAccessException e){
            log.error(e, e);
        }
        
        return null;
    }
    //jwp add end
    public GiftData getPlayerGift(int groupId, WorldPlayer player){
        GiftData[] result = getPlayerGifts(new int[]{groupId}, player);
        
        if(result != null && result.length > 0){
            return result[0];
        }else{
            return null;
        }
    }
    
    public GiftData[] getPlayerGifts(int[] groupIds, WorldPlayer player){
        try{
            Vector<GiftData> gData = new Vector<GiftData>();
            
            for(int i = 0; i < groupIds.length; i++){
                GiftGroup giftGroup = GiftGroups.getGiftGroup(groupIds[i]);
                
                if(giftGroup == null){
                    return null;
                }else if(giftGroup.getEndTime().getTime() < System.currentTimeMillis()){
                    //礼物已过期
                    continue;
                }
                
                GiftData giftData = player.getGiftData(groupIds[i]);
                
                if(giftData == null){
                    Gift gift = dao.getGift(groupIds[i], player.getId());
                    
                    if(gift == null){
                        gift = createGift(groupIds[i], player.getId());
                    }
                    
                    giftData = new GiftData(gift, giftGroup.getId());
                    player.addGiftData(giftData);
                }
                gData.add(giftData);
            }
            
            GiftData[] result = new GiftData[gData.size()];
            gData.toArray(result);
            
            return result; 
        }catch(DataAccessException e){
            log.error(e, e);
        }
        
        return null;
    }
//    //mengjie add
//    public Date getPlayerLastGetTime(int groupId, WorldPlayer player){
//        try{
//        	
//            GiftGroup giftGroup = GiftGroups.getGiftGroup(groupId);
//            
//            if(giftGroup == null){
//                return null;
//            }else if(giftGroup.getEndTime().getTime() < System.currentTimeMillis()){
//                //礼物已过期
//
//            }
//            Gift gift = dao.getGift(groupId, player.getId());
//            if(gift == null){
//                return null;
//            }else{
//            	return gift.getModifytime();
//            }
//
//        }catch(DataAccessException e){
//            log.error(e, e);
//        }
//        
//        return null;
//    }
    //获取单一的天赋数据
    public UWAPSegment getGiftGroupSegemntOnly(GiftData gDatas, int serial, int session){
        Vector<GiftData> showGroup = new Vector<GiftData>();
        
  
        long nowTime = System.currentTimeMillis();
        GiftGroup group = gDatas.getGiftGroup();
        UWAPSegment seg = null;
        if(group.getEndTime().getTime() < nowTime){
                //已过期
            seg = new UWAPSegment(ClientConstants.MESSAGE, serial, session);
            seg.writeString("活动已经结束啦");
        }
            
       if(group.getBeginTime().getTime() > nowTime && group.getCanSeeType() == GiftGroup.CAN_SEE_ALL){
                //未到时间
           seg = new UWAPSegment(ClientConstants.MESSAGE, serial, session);
           seg.writeString("活动还未开始哦");
       }
            
        showGroup.add(gDatas);
        
       
        
        if(showGroup.size() > 0){
            String[] ret = new String[showGroup.size() + 4];
            ret[0] = (showGroup.size() + 1) + "";
            ret[1] = "1";
            StringBuilder sb = new StringBuilder();
            sb.append("请您选择?");
            
            int i = 0;
            
            for (; i < showGroup.size(); i++) {
                sb.append("\n");
                sb.append(i + 1);
                sb.append(".");
                sb.append(showGroup.get(i).getGiftGroup().getMessage_group(null, null, null));
            }
            
            sb.append("\n");
            sb.append(i + 1);
            sb.append(".");
            sb.append("一会再说");
            
            ret[2] = sb.toString();
            
            i = 0;
            
            for (; i < showGroup.size(); i++) {
                ret[i + 3] = "get_gift_define " + showGroup.get(i).getGiftGroup().getId();
            }
            
            ret[i + 3] = "ok";
            
            byte[] bytes = stageService.getTaskBytes((short) 31010, ret);
            seg = new UWAPSegment(ClientConstants.GET_FILE_OK, serial, session);
            seg.writeShort((short) 31010);
            seg.writeShort((short) 2);
            seg.write(bytes);
        }else{
            seg = new UWAPSegment(ClientConstants.MESSAGE, serial, session);
            seg.writeString("现在还不能领取礼物");
        }
        
        return seg;
    }
    
    public UWAPSegment getGiftGroupSegemnt(GiftData[] gDatas, int serial, int session){
        Vector<GiftData> showGroup = new Vector<GiftData>();
        UWAPSegment seg = null;
        if(gDatas.length == 0){
        	seg = new UWAPSegment(ClientConstants.MESSAGE, serial, session);
            seg.writeString("活动已过期！");
            return seg;
        }
        for(int i = 0; i < gDatas.length; i++){
            long nowTime = System.currentTimeMillis();
            GiftGroup group = gDatas[i].getGiftGroup();
            
            if(group.getEndTime().getTime() < nowTime){
                //已过期
                continue;
            }
            
            if(group.getBeginTime().getTime() > nowTime && group.getCanSeeType() == GiftGroup.CAN_SEE_ALL){
                //未到时间
                continue;
            }
            
            showGroup.add(gDatas[i]);
        }
        
        
        
        if(showGroup.size() > 0){
        	int size = showGroup.size();
        	int i = 0;
        	for(; i< showGroup.size(); i++){
        		if(showGroup.get(i).getGiftGroup().getMessage_about(null, null, null) != null){
                    size ++;
                }
        	}
            String[] ret = new String[size + 4];
            ret[0] = (size + 1) + "";
            ret[1] = "1";
            StringBuilder sb = new StringBuilder();
            sb.append("请您选择?");
            
            int index = 1;
        	i = 0;
            for (; i < showGroup.size(); i++) {
                sb.append("\n");
                sb.append(index++);
                sb.append(".");
                sb.append(showGroup.get(i).getGiftGroup().getMessage_group(null, null, null));
                if(showGroup.get(i).getGiftGroup().getMessage_about(null, null, null) != null){
                	sb.append("\n");
                    sb.append(index++);
                    sb.append(".");
                    sb.append(showGroup.get(i).getGiftGroup().getMessage_about(null, null, null));
                }
            }
            
            sb.append("\n");
            sb.append(index++);
            sb.append(".");
            sb.append("一会再说");
            
            ret[2] = sb.toString();
            
            i = 0;
            index = 0;
            for (; i < showGroup.size(); i++) {
                ret[index + 3] = "get_gift_define " + showGroup.get(i).getGiftGroup().getId();
                index++;
                if(showGroup.get(i).getGiftGroup().getMessage_about(null, null, null) != null){
                	ret[index + 3] = "get_gift_define " + showGroup.get(i).getGiftGroup().getId() + " about";
                	index++;
                }
            }
            
            ret[index + 3] = "ok";
            
            byte[] bytes = stageService.getTaskBytes((short) 31010, ret);
            seg = new UWAPSegment(ClientConstants.GET_FILE_OK, serial, session);
            seg.writeShort((short) 31010);
            seg.writeShort((short) 2);
            seg.write(bytes);
        }else{
            seg = new UWAPSegment(ClientConstants.MESSAGE, serial, session);
            seg.writeString("现在还不能领取礼物");
        }
        
        return seg;
    }
    
    private Gift createGift(int groupId, int playerId){
        Gift gift = new Gift();
        gift.setGroupid(groupId);
        gift.setPlayerid(playerId);
        gift.setCreatetime(new Date(System.currentTimeMillis()));
        gift.setModifytime(new Date(System.currentTimeMillis()));
        gift.setRcount(0);
        gift.setCount(0);
        
        try{
            dao.addGift(gift);
        }catch(DataAccessException ex){
        }
        
        return gift;
    }

    public void savePlayerGift(Gift gift){
        try{
            dao.makePersistent(gift);
        }catch(DataAccessException ex){
        }
    }
}
