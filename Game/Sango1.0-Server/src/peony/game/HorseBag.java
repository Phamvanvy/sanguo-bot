package peony.game;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.apache.log4j.Logger;
import peony.game.changed.ChangedItem;
import peony.game.changed.HorseBagChangedItem;
import peony.game.chat.ChatMessage;
import peony.game.chat.ItemChatAttachment;
import peony.net.Packet;
import peony.service.account.Account;
import peony.service.account.AccountService;
import peony.service.fame.Fame;
import peony.service.fame.FameService;
import peony.service.stat.StatService;

public class HorseBag {
	
	/** 合成失败保留副坐骑扣除I币代物品 */
	public static int decIMoneyItem = 4033;
	
	private static final Logger log = Logger.getLogger(HorseBag.class);
	
	public static Random rand = new Random();
	/** 0-10对应的期望消耗*/
	public static float[] fixLevelValue = { 1, 2, 3.15f, 4.66f, 6.66f,  9.69f, 13.69f, 18.96f,  27.29f, 52.28f, 98,
		//11-15期望消耗
		148, 248, 373, 523, 708,
		//16-20期望消耗
		1174, 1827, 2712, 4179, 6462
	};

	public int maxSize;
	public List<Horse> horses;
	public Unit owner;

	public HorseBag(Unit owner) {
		this(owner, 5);
	}

	public HorseBag(Unit owner, int maxSize) {
		this.owner = owner;
		this.maxSize = maxSize;
		horses = new ArrayList<Horse>(maxSize);
	}
	
	public List<Horse> getAgentHorses(){
		List<Horse> ret = new ArrayList<Horse>(3);
		for(Horse h:horses){
			if(h.agentHorse==1){
				ret.add(h);
			}
		}
		return ret;
	}
	
	public void autoActiveHorse(Player player){
		try{
			for(Horse h:horses){
				if(h.itemId == Horse.freeHorse[player.clazz] && h.freeHorseEndTime>0 && System.currentTimeMillis() > h.freeHorseEndTime){
					AccountService as = Server.server.getServiceRegistry()
					.getAccountService();
					Account account = as.getAccount(player.accountId);
					long iMoney = account.getLongIMoney() / 100;
					if(iMoney>=100*36){
						h.freeHorseEndTime = 0;
						h.setActive();
						h.addIntPropertyChangedItem(player.changed, ChangedItem.HORSE_STATE, h.state, false);
						h.addStringPropertyChangedItem(player.changed, ChangedItem.HORSE_NAME, h.name, false);
						break;
					}
				}
			}
		}catch(Exception e){
			
		}
	}

	@Override
	public HorseBag clone(){
		HorseBag bag = new HorseBag(owner,maxSize);
		for(Horse h:horses){
			bag.horses.add(h);
		}
		return bag;
	}
	
	public boolean isFull(){
		return horses.size()>=maxSize;
	}
	
	public void addHorse(Horse h){
		horses.add(h);
		if(owner.changed!=null){
			owner.changed.addChangedItem(new HorseBagChangedItem(h,1));
		}
	}
	
	public boolean removeHorse(int instanceId){
		Iterator<Horse> ite = horses.iterator();
		while(ite.hasNext()){
			Horse h = ite.next();
			if(h.instanceId == instanceId){
				ite.remove();
				if(owner.changed!=null){
					owner.changed.addChangedItem(new HorseBagChangedItem(h,0));
				}
				if(h.equs!=null)
					log.info("[MUTEREMOVEHORSE]HID["+h.instanceId+"]HASEQUIP["+h.equs.hasEquipments()+"]");
				return true;
			}
		}
		return false;
	}
	
	public Horse getHorse(int instanceId) {
		for (Horse h : horses) {
			if (h.instanceId == instanceId)
				return h;
		}
		return null;
	}

	public void throwHorse(int instanceId, int serial) throws Exception {
		Horse horse = null;
		Iterator<Horse> ite = horses.iterator();
		while (ite.hasNext()) {
			Horse h = ite.next();
			if (h.instanceId == instanceId) {
				horse = h;
				if(horse.equs.hasEquipments()){
					throw new Exception("该坐骑身上还有装备,请全部卸下后再放生");
				}
				ite.remove();
				if(owner.changed!=null){
					owner.changed.addChangedItem(new HorseBagChangedItem(h,0));
				}
				break;
			}
		}
		if (horse != null) {
			if (owner.horse == horse) {
				if(owner instanceof Player){
					((Player)owner).horseUnride(-1);
				}else{
					owner.horse = null;
					horse.unRide(owner);
				}
			}
			if (owner.type == GameObject.TYPE_PLAYER) {
				// 记录日志
				LogUtil.logThrowHorse((Player)owner, horse, "DEL");
				
				Packet pt = new Packet(OpCode.HORSE_THROW_SERVER);
				pt.putInt(serial);
				((Player)owner).send(pt);
			}
		}
	}
	
	
	/**
	 *坐骑幻化
	 */
	public void horseImageChange(Player p, int destInstId, int resInstId, int serial) {
		Horse destHorse = null;
		Horse resHorse = null;
		Iterator<Horse> ite = horses.iterator();
		while (ite.hasNext()) {
			Horse h = ite.next();
			if (h.instanceId == destInstId) {
				destHorse = h;
			}else if (h.instanceId == resInstId) {
				resHorse = h;
				ite.remove();
				owner.changed.addChangedItem(new HorseBagChangedItem(resHorse, 0));
			}
			if(destHorse != null && resHorse != null)
				break;
		}
		if (destHorse != null && resHorse != null) {
			if(destHorse.imageIdChange == -1)
				destHorse.imageIdChange = destHorse.imageId;
			destHorse.imageId = resHorse.imageId;
			if(destHorse.iconIdChange == -1)
			    destHorse.iconIdChange = destHorse.iconId;
			destHorse.iconId = resHorse.iconId;
			if(destHorse.iconImageChange == -1)
			    destHorse.iconImageChange = destHorse.iconImage;
			destHorse.iconImage = resHorse.iconImage;
			destHorse.horseChangeName(p);
			destHorse.itemIdChange = resHorse.itemId;
			if(owner.changed!=null)
				owner.changed.addChangedItem(new HorseBagChangedItem(destHorse,1));
			if (owner.type == GameObject.TYPE_PLAYER) {
				Packet pt = new Packet(OpCode.HORSE_CHANGE_SERVER);
				pt.putInt(serial);
				((Player)owner).send(pt);
				//统计坐骑幻化次数成就
				StatService statService = Server.server.getServiceRegistry().getStatService();
				statService.playerChangeHorse(p);
				// 记录日志
				LogUtil.logHorseImageChange((Player)owner, destHorse, resHorse);
			}
		}
	}
	
	/**
	 *解除坐骑幻化
	 */
	public void removehorseImageChange(Player p, int instId, int serial) {
		Horse horse = null;
		Iterator<Horse> ite = horses.iterator();
		while (ite.hasNext()) {
			Horse h = ite.next();
			if (h.instanceId == instId) {
				horse = h;
				break;
			}
		}
		if (horse != null) {
			horse.imageId = horse.imageIdChange;
			if(horse.iconIdChange!=-1)
			    horse.iconId = horse.iconIdChange;
			if(horse.iconImageChange!=-1)
			    horse.iconImage = horse.iconImageChange;
			horse.iconIdChange = -1;
			horse.iconImageChange = -1;
			horse.imageIdChange = -1;
			horse.horseChangeName(p);
			if(owner.changed!=null)
				owner.changed.addChangedItem(new HorseBagChangedItem(horse,1));
			if (owner.type == GameObject.TYPE_PLAYER) {
				Packet pt = new Packet(OpCode.REMOVE_HORSE_CHANGE_SERVER);
				pt.putInt(serial);
				((Player)owner).send(pt);
				// 记录日志
				LogUtil.logRemoveHorseImageChange((Player)owner, horse);
			}
		}
	}
	
	/**
	 *坐骑合成
	 */
	public boolean horseFix(Player p, int destInstId, int resInstId, int price, int serial) {
		boolean success = true;
		Horse destHorse = null;
		Horse resHorse = null;
		Iterator<Horse> ite = horses.iterator();
		while (ite.hasNext()) {
			Horse h = ite.next();
			if (h.instanceId == destInstId)
				destHorse = h;
			else if (h.instanceId == resInstId)
				resHorse = h;
			if(destHorse != null && resHorse != null)
				break;
		}
		if (destHorse != null && resHorse != null) {
			float curRand = fixLevelValue[resHorse.fixCount] /
					(fixLevelValue[destHorse.fixCount + 1] - fixLevelValue[destHorse.fixCount]);
			if(rand.nextFloat() > curRand)
				success = false;
			if(success){	//合成成功
				destHorse.fixCount++;
				boolean removeOk = removeHorse(resInstId);
				if(!removeOk)
					return false;
				destHorse.horseChangeName(p);
				destHorse.refreshProperties(false, p);
				if(owner.changed!=null)
					owner.changed.addChangedItem(new HorseBagChangedItem(destHorse,1));
				
				if(destHorse.fixCount >= 5){	//合成5级以上发世界聊
					GameItem item = ObjectAccessor.createGameItem(destHorse.itemId);
					ItemChatAttachment attItem = new ItemChatAttachment(item);
					String message;
					if(destHorse.fixCount <= 10){
						message = MessageFormat.format("{0}在坐骑合成幻化大师处合成了绝世良驹 /-1<cff0000>+{1}</c>，战斗力大幅增强", p.name, destHorse.fixCount);
					}else if(destHorse.fixCount > 10 && destHorse.fixCount <= 14){
						message = MessageFormat.format("{0}在坐骑合成幻化大师处神奇的合成了绝世良驹 /-1<cff0000>+{1}</c>，尽显霸者之气！", p.name, destHorse.fixCount);
					}else{
						message = MessageFormat.format("{0}在坐骑合成幻化大师处奇迹般的合成了绝世良驹 /-1<cff0000>+{1}</c>，神也不能阻挡他了！", p.name, destHorse.fixCount);
					}
					ChatMessage cm = new ChatMessage(ChatOption.WORLD, p.id, -1, peony.Messages.STRING_00004, message, attItem);
					Server.server.getServiceRegistry().getChatService().addChatMessage(cm);
				}
			}else{
				p.failHorseInst = resInstId;
			}
			
			if (owner.type == GameObject.TYPE_PLAYER) {
				Packet pt = new Packet(OpCode.HORSE_FIX_SERVER);
				pt.putInt(serial);
				pt.put(destHorse.fixCount);
				pt.putShort(success?0:price/36);
				((Player)owner).send(pt);
				// 记录日志
				LogUtil.logHorseFix((Player)owner, destHorse, resHorse, success);
			}
		}
		return success;
	}

	public byte[] toClientBytes() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.write(maxSize);
			dos.write(horses.size());
			for (Horse h : horses) {
				dos.write(h.toClientBytes(owner));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
	
	public byte[] toClientBytesAdmin() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.write(maxSize);
			dos.write(horses.size());
			for (Horse h : horses) {
				dos.write(h.toClientBytesAdmin(owner));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
	
	public byte[] toDBBytes(){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.write(1);//version
			dos.write(maxSize);
			dos.write(horses.size());
			for (Horse h : horses) {
				dos.write(h.toDBBytes());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
		
	}
	
	public static HorseBag fromDBBytes(byte[] bytes,Player owner) throws SQLException{
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);
		try{
			dis.read();
			int maxSize = dis.read();
			HorseBag bag = new HorseBag(owner,maxSize);
			int size = dis.read();
			for(int i=0;i<size;i++){
				Horse h = Horse.fromDBBytes(dis);
				Horse cHorse = bag.getHorse(h.instanceId);
				if (cHorse != null) { //曾经出现过一个bug，导致骑马的时候打宝石会复制马20100624
					for(int j=0;j<cHorse.equs.equs.length;j++){
						if(cHorse.equs.equs[j]==null&&h.equs.equs[j]!=null){
							cHorse.equs.equs[j] = h.equs.equs[j];
						}
					}
					LogUtil.logThrowHorse(owner, h, "BUG20100624");
				} else {
					try {
						h.refreshProperties(false, owner);
					} catch (Exception e) {
						
					}
					bag.horses.add(h);
				}
			}
			return bag;
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException(e);
		}
	}
	public static HorseBag fromDBBytes(byte[] bytes,Fame fame)throws SQLException{
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);
		Player owner = FameService.statuePlayer.get(fame.playerId);
		try{
			dis.read();
			int maxSize = dis.read();
			HorseBag bag = new HorseBag(owner,maxSize);
			int size = dis.read();
			for(int i=0;i<size;i++){
				Horse h = Horse.fromDBBytes(dis);
				Horse cHorse = bag.getHorse(h.instanceId);
				if (cHorse != null) { //曾经出现过一个bug，导致骑马的时候打宝石会复制马20100624
					for(int j=0;j<cHorse.equs.equs.length;j++){
						if(cHorse.equs.equs[j]==null&&h.equs.equs[j]!=null){
							cHorse.equs.equs[j] = h.equs.equs[j];
						}
					}
					LogUtil.logThrowHorse(owner, h, "BUG20100624");
				} else {
					try {
						h.refreshProperties(false, owner);
					} catch (Exception e) {
						
					}
					bag.horses.add(h);
				}
			}
			return bag;
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException(e);
		}
	}
	
	/**
	 * 扩展坐骑栏所需金钱
	 * @return
	 */
	public int getExtendHorsebagMoney(){
		switch(maxSize){
			case 5:
				return 200000;
			case 6:
				return 400000;
			case 7:
				return 800000;
			case 8:
				return 1600000;
			case 9:
				return 3200000;
		}
		return 3200000;
	}
}
