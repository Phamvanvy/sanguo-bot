package peony.game.stepserver;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import peony.alchemy.AlchemyLevelData;
import peony.game.CoolDownList;
import peony.game.GameObject;
import peony.game.HorseBag;
import peony.game.ItemUtil;
import peony.game.LogUtil;
import peony.game.Player;
import peony.game.PlayerUtil;
import peony.game.PropertyPool;
import peony.game.Skills;
import peony.game.Titles;
import peony.game.attendant.AttendantBag;
import peony.game.buff.Buff;
import peony.game.buff.Buffs;
import peony.net.DispatchPacket;
import peony.net.Packet;
import peony.service.cards.Cards;
import peony.service.read.Books;

public class StepServerDecoder extends ProtocolDecoderAdapter {

	private static final Logger log = Logger.getLogger(StepServerDecoder.class);
	private static final String BUFFER = ".UABuffer";
	private static final ByteBuffer EMPTY = ByteBuffer.allocate(0);

	public void decode(IoSession session, ByteBuffer in,
			ProtocolDecoderOutput out) throws Exception {
		boolean useSessionBuffer = false;
		ByteBuffer buf = (ByteBuffer)session.getAttribute(BUFFER);
		if(buf!=null){
			buf.put(in);
			buf.flip();
			useSessionBuffer = true;
		}else{
			buf = in;
		}
		for(;;){
			if(buf.remaining()>19){
				int pos = buf.position();
				byte byte1 = buf.get();
				byte byte2 = buf.get();
				if(byte1==68&&byte2==65){ //'D'&'A'
					int len = buf.getInt();
					byte type = buf.get();
					if(type==StepClientEncoder.type0fPacket){
						int accountId = 0;
						try{accountId = buf.getInt();}catch(Exception e){}
						int playerId = 0;
						try{playerId = buf.getInt();}catch(Exception e){}
						int sessionId = buf.getInt();
						if(buf.remaining()>=(len-19)){  //去掉head以及len一共10个字节
							if (buf.get() == 85 && buf.get() == 65) {
								int packetLen = buf.getInt();
								short opCode = buf.getShort();
								ByteBuffer data = EMPTY;
								byte[] bytes = new byte[packetLen];
								buf.get(bytes);
								data = ByteBuffer.wrap(bytes);
								Packet packet = new Packet(opCode, data);
								DispatchPacket dp = new DispatchPacket(sessionId,packet);
								dp.accountId = accountId;
								dp.playerId = playerId;
								out.write(dp);
							}else{
								session.setAttribute(BUFFER,null);
								throw new IOException("UA head error");
							}
						}else{
							buf.position(pos);
							break;
						}
					}else if(type==StepClientEncoder.type0fPlayer){
						int accountId = 0;
						try{accountId = buf.getInt();}catch(Exception e){}
						int playerId = 0;
						try{playerId = buf.getInt();}catch(Exception e){}
						byte[] bytes = new byte[len - 15];
						if(buf.remaining()>=(len-15)){
							buf.get(bytes);
							Player player = generatePlayer(bytes);
							player.accountId = accountId;
							player.id = playerId;
							out.write(player);
						}else{
							buf.position(pos);
							break;
						}
					}
				}else{
					buf.position(pos + 1);
					continue;
				}
			}else{
//				buf.clear();
//				buf.flip();
				break;
			}
		}
		if (buf.hasRemaining()) {
			storeRemainingInSession(buf,session);
		}else{
			if(useSessionBuffer)
				session.setAttribute(BUFFER,null);
		}
	}
	
    private void storeRemainingInSession(ByteBuffer buf, IoSession session) {
        ByteBuffer remainingBuf = ByteBuffer.allocate(buf.capacity());
        remainingBuf.setAutoExpand(true);
        remainingBuf.order(buf.order());
        remainingBuf.put(buf);
        session.setAttribute(BUFFER, remainingBuf);
    }
    
    public Player generatePlayer(byte[] data){
    	StringBuilder strb = new StringBuilder();
    	ByteArrayInputStream bios = new ByteArrayInputStream(data);
		DataInputStream dis = new DataInputStream(bios);
		Player player = PlayerUtil.createPlayer("", 0, 0, 1, 0);
		try {
			strb.append("[STEPBATTLEPLAYERBYTES]");
			int sessionId = dis.readInt();
			player.stepSessionId = sessionId;
			strb.append("[SESSIONID]"+sessionId);
			player.id = dis.readInt();
			strb.append("[ID]"+player.id);
			player.accountId = dis.readInt();
			strb.append("[ACC]"+player.accountId);
			player.instanceId = player.id;
			player.type = GameObject.TYPE_PLAYER;
			player.name = dis.readUTF();
			strb.append("[NAME]"+player.name);
			player.sex = dis.read();
			player.level = dis.read();
			strb.append("[LEVEL]"+player.level);
			player.clazz = dis.read();
			strb.append("[CLAZZ]"+player.clazz);
			player.faction = dis.read();
			strb.append("[FACTION]"+player.faction);
			player.maxhp = dis.readShort();
			strb.append("[MAXHP]"+player.maxhp);
			player.maxmp = dis.readShort();
			strb.append("[MAXMP]"+player.maxmp);
			player.hp = dis.readShort();
			strb.append("[HP]"+player.hp);
			player.mp = dis.readShort();
			strb.append("[ID]"+player.mp);
			player.strength = dis.readShort();
			strb.append("[STRENGH]"+player.strength);
			player.agility = dis.readShort();
			strb.append("[AGILITY]"+player.agility);
			player.stamina = dis.readShort();
			strb.append("[STAMINA]"+player.stamina);
			player.intellect = dis.readShort();
			strb.append("[INTELLECT]"+player.intellect);
			player.attackpowerup = dis.readShort();
			strb.append("[ATTACKUP]"+player.attackpowerup);
			player.attackpowerdown = dis.readShort();
			strb.append("[ATTDOWN]"+player.attackpowerdown);
			player.spellpower = dis.readShort();
			strb.append("[SPELLPOWER]"+player.spellpower);
			player.spellheal = dis.readShort();
			strb.append("[SPELLHEAL]"+player.spellheal);
			player.defense = dis.readShort();
			strb.append("[DEFENCE]"+player.defense);
			player.spelldefense = dis.readShort();
			strb.append("[SPELLDEFENCE]"+player.spelldefense);
			player.critical = dis.readShort()/100;
			strb.append("[CRITICAL]"+player.critical);
			player.spellcritical = dis.readShort()/100;
			strb.append("[SPELLCRITICAL]"+player.spellcritical);
			player.hit = dis.readShort()/100;
			strb.append("[HIT]"+player.hit);
			player.spellhit = dis.readShort()/100;
			strb.append("[SPELLHIT]"+player.spellhit);
			player.dodge = dis.readShort()/100;
			strb.append("[DODGE]"+player.dodge);
			player.spelldodge = dis.readShort()/100;
			strb.append("[SPELLDODGE]"+player.spelldodge);
			player.anticrit = dis.readShort()/100;
			strb.append("[ANTICRIT]"+player.anticrit);
			player.defensePercent = dis.readShort()/100;
			strb.append("[DEFENCEPERCENT]"+player.defensePercent);
			player.healthrestore = dis.readShort();
			strb.append("[HEALTHRESTORE]"+player.healthrestore);
			player.manarestore = dis.readShort();
			strb.append("[MANARESTORE]"+player.manarestore);
			player.skillPoint = dis.readShort();
			strb.append("[SKILLPOINT]"+player.skillPoint);
			player.propertyPoint = dis.readShort();
			strb.append("[PROPERTYPOINT]"+player.propertyPoint);
			player.exp = dis.readInt();
			strb.append("[EXP]"+player.exp);
			dis.readInt();
			player.money = dis.readInt();
			strb.append("[MONEY]"+player.money);
			int mapId = dis.readShort();
			strb.append("[MAPID]"+mapId);
			player.tempMapId = mapId;
			int mapInstanceId = dis.readInt();
			strb.append("[MAPINSTANCEID]"+mapInstanceId);
			player.x = dis.readShort();
			strb.append("[X]"+player.x);
			player.y = dis.readShort();
			strb.append("[Y]"+player.y);
			player.direct = (byte)dis.readShort();
			player.state = dis.readShort();
			strb.append("[STATE]"+player.state);
			dis.readInt();
			dis.readUTF();
			player.setGuildName(dis.readUTF());
			player.strengthAdded = dis.readShort();
			strb.append("[STRENGTHADD]"+player.strengthAdded);
			player.agilityAdded = dis.readShort();
			strb.append("[AGILITYADD]"+player.agilityAdded);
			player.staminaAdded = dis.readShort();
			strb.append("[STAMINAADD]"+player.staminaAdded);
			player.intellectAdded = dis.readShort();
			strb.append("[INTELLECTADD]"+player.intellectAdded);
			player.gameCode = dis.readUTF();
			strb.append("[GAMECODE]"+player.gameCode);
			int bagBytesLength = dis.readInt();
			byte[] bagBytes = new byte[bagBytesLength];
			dis.read(bagBytes);
			player.bag = ItemUtil.getTransactionBagFromDB(bagBytes, player);
			strb.append("[BAG]");
			LogUtil.getBinaryString(strb, bagBytes);
			int equipmentsByteLength = dis.readInt();
			byte[] equipmentBytes = new byte[equipmentsByteLength];
			dis.read(equipmentBytes);
			player.equipments = ItemUtil.getEquipmentsFromDB(equipmentBytes, player);
			strb.append("[EQUIP]");
			LogUtil.getBinaryString(strb, equipmentBytes);
			int buffByteLength = dis.readInt();
			byte[] buffBytes = new byte[buffByteLength];
			dis.read(buffBytes);
			Buff[] bs = Buffs.getBuffs(buffBytes, player);
			Buffs buffs = new Buffs(player);
			for(Buff buff:bs){
				buffs.addBuff(buff);
			}
			player.buffs = buffs;
			strb.append("[BUFF]");
			LogUtil.getBinaryString(strb, buffBytes);
			int horseBagSize = dis.readInt();
			byte[] horseBagBytes = new byte[horseBagSize];
			dis.read(horseBagBytes);
			try {
				player.horseBag = HorseBag.fromDBBytes(horseBagBytes, player);
			} catch (Exception e) {
				e.printStackTrace();
			}
			strb.append("[HORSEBAG]");
			LogUtil.getBinaryString(strb, horseBagBytes);
			int titlesSize = dis.readInt();
			byte[] titlesBytes = new byte[titlesSize];
			dis.read(titlesBytes);
			player.titles = Titles.fromDBBytes(titlesBytes, player);
			strb.append("[TITLE]");
			LogUtil.getBinaryString(strb, titlesBytes);
			int attendantBagSize = dis.readInt();
			byte[] attendantBagBytes = new byte[attendantBagSize];
			dis.read(attendantBagBytes);
			player.attendantBag = AttendantBag.fromDBBytes(attendantBagBytes, player, null);
			strb.append("[ATTENDANTBAG]");
			LogUtil.getBinaryString(strb, attendantBagBytes);
			int skillsSize = dis.readInt();
			byte[] skillsBytes = new byte[skillsSize];
			dis.read(skillsBytes);
			player.skills = Skills.getSkillsFromDB(skillsBytes, player);
			strb.append("[SKILL]");
			LogUtil.getBinaryString(strb, skillsBytes);
			int cardsSize = dis.readInt();
			byte[] cardsBytes = new byte[cardsSize];
			dis.read(cardsBytes);
			player.cards = Cards.getFromDBBytes(cardsBytes, player);
			strb.append("[CARDS]");
			LogUtil.getBinaryString(strb, cardsBytes);
			int booksSize = dis.readInt();
			byte[] booksBytes = new byte[booksSize];
			dis.read(booksBytes);
			player.books = Books.fromDBBytes(booksBytes, player);
			strb.append("[BOOK]");
			LogUtil.getBinaryString(strb, booksBytes);
			String poolString = dis.readUTF();
			PropertyPool ret = new PropertyPool();
			if (poolString != null)
				ret.parse(poolString);
			player.pool = ret;
			int coolSize = dis.readInt();
			byte[] coolBytes = new byte[coolSize];
			dis.read(coolBytes);
			player.coolDowns = CoolDownList.fromDBBytes(coolBytes);
			strb.append("[COOLDOWNS]");
			LogUtil.getBinaryString(strb, coolBytes);
			log.info(strb.toString());
			player.stepType=dis.readByte();
			strb.append("[STEPTYPE]");
			LogUtil.getBinaryString(strb, new byte[]{(byte)player.stepType});
			player.accountModel=dis.readUTF();
			strb.append("[MODEL]");
			int actionOptionBarsSize = dis.readInt();
			byte[] actionOptionBarBytes = new byte[actionOptionBarsSize];
			dis.read(actionOptionBarBytes);
			player.actionBarOptions = actionOptionBarBytes;
			log.info(strb.toString());
			player.vipLevel=dis.read();
			log.info("[VIPLEVEL]");
			
			int alchemySize = dis.readInt();
			byte[] alchemyBytes = new byte[alchemySize];
			dis.read(alchemyBytes);
			ByteArrayInputStream bais = new ByteArrayInputStream(alchemyBytes);
			DataInputStream disAlchemy = new DataInputStream(bais);
			player.alchemy = AlchemyLevelData.getFromDBBytes(disAlchemy, player);
			strb.append("[ALCHEMY]");
			LogUtil.getBinaryString(strb, alchemyBytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return player;
    }

}
