package com.pip.itimes.server.world.farm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import com.pip.itimes.server.bean.Farm;
import com.pip.itimes.server.util.PropertyPool;

public class FarmData {
	/**
	 * 庄园数据
	 */
	private static final Logger log = Logger.getLogger(FarmData.class);
	private Farm farm;
	private String title;
	private PropertyPool otherPool = new PropertyPool();
	private final String STEALPLAYER = "STEALPLAYER";	//玩家数据
	
	private HashMap<Integer, FarmLandInfo> landInfo = new HashMap<Integer, FarmLandInfo>();
	
	private HashMap<Integer, FarmStealPlayer> stealPlayers = new HashMap<Integer, FarmStealPlayer>();
	
	
	public FarmData(Farm farm) throws Exception{
		this.farm = farm;
		init();
	}
	
	public void init() throws Exception{
		byte[] bytelandInfo = farm.getLandinfo(); 
		if(bytelandInfo != null && bytelandInfo.length > 0){
			ByteArrayInputStream bis = new ByteArrayInputStream(bytelandInfo);
            DataInputStream dis = new DataInputStream(bis);
            int version = dis.readInt();		//版本号
			for(int i=0; i<farm.getLandcount(); i++){
				int index = dis.readInt();
				int level = dis.readInt();
				int seed = dis.readInt();
				long createTime = dis.readLong();
				byte fertilize = dis.readByte();
				boolean results = dis.readBoolean();
				int fruitCurrentCount = dis.readInt();
				int resultsCount = dis.readInt();
				FarmLandInfo info = new FarmLandInfo(level, seed, createTime, fertilize);
				info.setResults(results);
				info.setFruitCurrentCount(fruitCurrentCount);
				info.setResultsCount(resultsCount);
				//窃取的玩家的个数
				int filchCount = dis.readInt();
				for(int f=0; f<filchCount; f++){
					info.addStealPlayer(dis.readInt());
				}
				landInfo.put(index, info);
			}
		}
		initOtherPool();
	}
	
	private void initOtherPool() throws Exception{
		if(farm.getOtherPool() != null){
			String stealplayerdata = otherPool.getString(STEALPLAYER);
			initStealPlayer(stealplayerdata);
		}
	}
	
	private void initStealPlayer(String data){
		if(data == null || data.equals("")){
			return;
		}
		String[] tmpdata = data.split(";");
		if(tmpdata.length <= 0){
			return;
		}
		int count = Integer.parseInt(tmpdata[0]);
		stealPlayers.clear();
		if(count > 0){
			try{
				int index = 1;
				int readcount = 0;
				while(readcount < count){
					FarmStealPlayer fsp = new FarmStealPlayer();
					fsp.setstealId(Integer.parseInt(tmpdata[index]));
					fsp.setPlayerName(tmpdata[index + 1]);
					fsp.setstealTime(Long.parseLong(tmpdata[index + 2]));
					stealPlayers.put(fsp.getstealId(), fsp);
					index += 3;
					readcount ++;
				}
			}catch(Exception e){
			}
		}
	}
	
	public void resetStealPlayer(){
		if(stealPlayers.size() == 0){
			otherPool.setString(STEALPLAYER, "");
		}else{
			StringBuilder sb = new StringBuilder();
			sb.append(stealPlayers.size());
			Iterator<FarmStealPlayer> iter = stealPlayers.values().iterator();
			while(iter.hasNext()){
				FarmStealPlayer fsp = iter.next();
				sb.append(";");
				sb.append(fsp.getstealId());
				sb.append(";");
				sb.append(fsp.getPlayerName());
				sb.append(";");
				sb.append(fsp.getstealTime());
			}
			otherPool.setString(STEALPLAYER, sb.toString());
		}
	}
	
	public static byte[] createDefaultLandInfo(byte landCount) throws Exception{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(0);
        for(int i=0; i<landCount; i++){
        	dos.writeInt(i);
        	dos.writeInt(0);
        	dos.writeInt(0);
        	dos.writeLong(0);
        	dos.writeByte((byte)0);
        	dos.writeInt(0);
        	dos.writeBoolean(false);
        	dos.writeInt(0);
        	dos.writeInt(0);
        }
        return bos.toByteArray();
	}
	
	public void reset(){
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
        	int version = 0;
        	dos.writeInt(version);
        	int count = landInfo.size();
        	synchronized (landInfo) {
        		Iterator<Entry<Integer, FarmLandInfo>> iter = landInfo.entrySet().iterator();
        		while(iter.hasNext()){
        			Entry<Integer, FarmLandInfo> entry = iter.next();
        			FarmLandInfo info = (FarmLandInfo)entry.getValue();
        			dos.writeInt(entry.getKey());
	        		dos.writeInt(info.getLevel());
	        		dos.writeInt(info.getSeed());
	        		dos.writeLong(info.getCreateTime());
	        		dos.writeByte(info.getFertilize());
	        		dos.writeBoolean(info.getResults());
	        		dos.writeInt(info.getFruitCurrentCount());
	        		dos.writeInt(info.getResultsCount());
	        		HashMap<Integer, Integer> filchPlayer = info.getStealPlayer();
	        		if(filchPlayer == null){
	        			dos.writeInt(0);
	        		}else{
		        		Iterator<Integer> players = filchPlayer.keySet().iterator();
		        		dos.writeInt(filchPlayer.size());
		        		while(players.hasNext()){
		        			Integer playerid = players.next();
		        			dos.writeInt(playerid);
		        		}
	        		}
	        	}
        	}
        	farm.setLandinfo(bos.toByteArray());
        	resetOtherPool();
        	setOtherPool(otherPool);
        }catch (Exception ex) {
        	log.info(ex, ex);
        }
	}
	
	private void resetOtherPool(){
		resetStealPlayer();
	}
	
	public PropertyPool getOtherPool(){
    	return otherPool;
    }
    
	public void setOtherPool (PropertyPool otherPool) {
    	farm.setOtherPool(otherPool);
    }

	public FarmLandInfo getLandInfo(int index){
		return landInfo.get(index);
	}
	
	public HashMap<Integer, FarmLandInfo> getLandInfo(){
		return landInfo;
	}
	
	public FarmStealPlayer getstealplayer(int playerid){
		return stealPlayers.get(playerid);
	}
	
	public HashMap<Integer, FarmStealPlayer> getStealplayer(){
		return stealPlayers;
	}
	
	private	int limit = 10;
	public void addStealPlayer(FarmStealPlayer fsp){
		if(stealPlayers.containsKey(fsp.getstealId())){
			FarmStealPlayer tmpFsp = stealPlayers.get(fsp.getstealId());
			tmpFsp.setstealTime(fsp.getstealTime());
		}else{
			if(stealPlayers.size() >= limit){
				Iterator<FarmStealPlayer> iter = stealPlayers.values().iterator();
				FarmStealPlayer tmpminplayer = null;
				while(iter.hasNext()){
					FarmStealPlayer tmpplayer = iter.next();
					if(tmpminplayer == null){
						tmpminplayer = tmpplayer;
					}else{
						if(tmpplayer.getstealTime() < tmpminplayer.getstealTime()){
							tmpminplayer = tmpplayer;
						}
					}
				}
				stealPlayers.remove(tmpminplayer.getstealId());
			}
			stealPlayers.put(fsp.getstealId(), fsp);
		}
	}
	
	
	public Farm getFarm(){
		return farm;
	}
	
	public String getTitle(){
		if(title == null){
			title = farm.getPlayerName() + "的庄园";
		}
		return title;
	}
	
	
	public String getPlayerName(){
		return farm.getPlayerName();
	}
	
	public int getPlayerID(){
		return farm.getPlayerid();
	}
	
	public void addLandInfo(int index, FarmLandInfo farmLandInfo){
		landInfo.put(index, farmLandInfo);
	}
	
	public void addStealplayer(int playerid,FarmStealPlayer stealplayer){
		stealPlayers.put(playerid, stealplayer);
	}
	
	public HashMap<Integer, FarmStealPlayer> getStealPlayers(){
		return stealPlayers;
	}
	
	
	public boolean checkInvade(long oldStart){
		Iterator<FarmLandInfo> iter = landInfo.values().iterator();
		while(iter.hasNext()){
			FarmLandInfo farmLandInfo = iter.next();
			if(!farmLandInfo.getResults() && farmLandInfo.getSeed() > 0 && farmLandInfo.getCreateTime() <= oldStart){
				return true;
			}
		}
		return false;
	}
	
	public int getLevelLandCount(int level){
		int count = 0;
		Iterator<FarmLandInfo> iter = landInfo.values().iterator();
		while(iter.hasNext()){
			FarmLandInfo farmLandInfo = iter.next();
			if(farmLandInfo.getLevel() == level){
				count ++;
			}
		}
		return count;
	}
}
