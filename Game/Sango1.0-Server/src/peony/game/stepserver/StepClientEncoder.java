package peony.game.stepserver;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerUtil;
import peony.game.Server;
import peony.game.Skills;
import peony.net.DispatchClientSession;
import peony.net.DispatchPacket;
import peony.net.Packet;

public class StepClientEncoder extends ProtocolEncoderAdapter {

	public static byte type0fPacket = 0;
	public static byte type0fPlayer = 1;
	
	public void encode(IoSession session, Object obj, ProtocolEncoderOutput out)
			throws Exception {
		if (obj instanceof DispatchPacket) {
			DispatchPacket dp = (DispatchPacket) obj;
			Packet packet = dp.packet;
			ByteBuffer data = packet.getData();
			int len = 17 + 4 + 2 + 4 + data.remaining();
			ByteBuffer buf = ByteBuffer.allocate(len);
			buf.put(DispatchPacket.HEAD);
			buf.putInt(len);
			buf.put(type0fPacket);
			buf.putInt(dp.accountId);
			buf.putInt(dp.playerId);
			buf.putInt(dp.id);
			buf.put(Packet.HEAD);
			buf.putInt(len -17 - 4 - 2 - 4);
			buf.putShort(packet.getOpCode());
			buf.put(data);
			buf.flip();
			out.write(buf);
		}else if(obj instanceof Player){
			Player player = (Player)obj;
			ByteBuffer data = getPlayerByteBuffer(player);
			int len = 4 + 2 + 1 + 4 + 4 + data.remaining();
			ByteBuffer buf = ByteBuffer.allocate(len);
			buf.put(DispatchPacket.HEAD);
			buf.putInt(len);
			buf.put(type0fPlayer);
			buf.putInt(player.accountId);
			buf.putInt(player.id);
			buf.put(data);
//			data.flip();
			buf.flip();
			out.write(buf);
		}
	}
	
	public ByteBuffer getPlayerByteBuffer(Player player){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			int sessionId = (int) ((DispatchClientSession)player.session).id;
			dos.writeInt(sessionId);
			dos.writeInt(player.id);
			dos.writeInt(player.accountId);
			dos.writeUTF(player.name);
			dos.write(player.sex);
			dos.write(player.level);
			dos.write(player.clazz);
			dos.write(player.faction);
			dos.writeShort(player.maxhp);
			dos.writeShort(player.maxmp);
			dos.writeShort(player.hp);
			dos.writeShort(player.mp);
			dos.writeShort(player.strength);
			dos.writeShort(player.agility);
			dos.writeShort(player.stamina);
			dos.writeShort(player.intellect);
			dos.writeShort(Math.round(player.attackpowerup));
			dos.writeShort(Math.round(player.attackpowerdown));
			dos.writeShort(Math.round(player.spellpower));
			dos.writeShort(Math.round(player.spellheal));
			dos.writeShort(Math.round(player.defense));
			dos.writeShort(Math.round(player.spelldefense));
			dos.writeShort(Math.round(player.critical * 100));
			dos.writeShort(Math.round(player.spellcritical * 100));
			dos.writeShort(Math.round(player.hit * 100));
			dos.writeShort(Math.round(player.spellhit * 100));
			dos.writeShort(Math.round(player.dodge * 100));
			dos.writeShort(Math.round(player.spelldodge * 100));
			dos.writeShort(Math.round(player.anticrit * 100));
			dos.writeShort(Math.round(player.defensePercent * 100));
			dos.writeShort(player.healthrestore);
			dos.writeShort(player.manarestore);
			dos.writeShort(player.skillPoint);
			dos.writeShort(player.propertyPoint);
			dos.writeInt(player.exp);
			dos.writeInt((int) PlayerUtil.getUpLevelExp(player.level, player.level + 1));
			dos.writeInt(player.money);
			dos.writeShort(player.map.id);
			if (player.getVMap() == null) {
				dos.writeInt(-1);
			} else {
				dos.writeInt(player.getVMap().getInstanceId());
			}
			dos.writeShort(player.x);
			dos.writeShort(player.y);
			dos.writeShort(player.direct);
			dos.writeShort(player.state);
			dos.writeInt(player.getCredit());
			dos.writeUTF(player.getCreditString());
			dos.writeUTF(player.getGuildName());
			dos.writeShort(player.strengthAdded);
			dos.writeShort(player.agilityAdded);
			dos.writeShort(player.staminaAdded);
			dos.writeShort(player.intellectAdded);
			dos.writeUTF(Server.server.gameCode);
			byte[] bags = ItemUtil.getBagDBBytes(player.bag);
			dos.writeInt(bags.length);
			dos.write(bags);
			byte[] equps = ItemUtil.getEquipmentsDBBytes(player.equipments);
			dos.writeInt(equps.length);
			dos.write(equps);
			byte[] buffss = player.buffs.toDBBytes();
			dos.writeInt(buffss.length);
			dos.write(buffss);
			byte[] horseBags = player.horseBag.toDBBytes();
			dos.writeInt(horseBags.length);
			dos.write(horseBags);
			byte[] titless = player.titles.toDBBytes();
			dos.writeInt(titless.length);
			dos.write(titless);
			byte[] attendantBags = player.attendantBag.toDBBytes();
			dos.writeInt(attendantBags.length);
			dos.write(attendantBags);
			byte[] skillss = Skills.getDBBytes(player.skills);
			dos.writeInt(skillss.length);
			dos.write(skillss);
			byte[] cardss = player.cards.toDBBytes();
			dos.writeInt(cardss.length);
			dos.write(cardss);
			byte[] bookss = player.books.toDBBytes();
			dos.writeInt(bookss.length);
			dos.write(bookss);
			dos.writeUTF(player.pool.toString());
			dos.writeInt(player.coolDowns.toDBBytes().length);
			dos.write(player.coolDowns.toDBBytes());
			dos.writeByte(player.stepType);
			dos.writeUTF(player.getAccount().getModel());
			dos.writeInt(player.actionBarOptions.length);
			dos.write(player.actionBarOptions);
			dos.write(player.vipLevel);
			
			byte[] alchemys=player.alchemy.toDBBytes();
			dos.writeInt(alchemys.length);
			dos.write(alchemys);
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] data = baos.toByteArray();
		return ByteBuffer.wrap(data);
	}

}
