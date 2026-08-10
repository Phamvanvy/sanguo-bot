package peony.net;

import java.util.HashSet;
import java.util.Set;

import peony.game.OpCode;

public class DispatchPacket {

	public int id;
	public Packet packet;
	public int accountId;
	public int playerId;
	
	public static final short[] URGENT_TYPES = { OpCode.SYNC_TIME_SERVER,
			OpCode.SKILL_ATTACK_SERVER, OpCode.SKILL_ATTACKED_SERVER,
			OpCode.SKILL_PREPARE_ATTACK_SERVER, OpCode.COOLDOWN_SERVER,
			OpCode.COOLDOWN_END_SERVER };
	
	public static final Set<Short> URGENT_SET = new HashSet<Short>();
	static{
		for(int i=0;i<URGENT_TYPES.length;i++){
			URGENT_SET.add(URGENT_TYPES[i]);
		}
	}

	public static final byte[] HEAD = { 'D', 'A' };

	public DispatchPacket(int id, Packet packet) {
		this.id = id;
		if (URGENT_SET.contains(packet.opCode)) {
			packet.opCode |= 1 << 15;
		}
		this.packet = packet;
	}
}
