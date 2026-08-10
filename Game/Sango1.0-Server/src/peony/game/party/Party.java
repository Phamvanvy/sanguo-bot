package peony.game.party;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import peony.game.Actor;
import peony.game.ChatOption;
import peony.game.ErrorHandler;
import peony.game.GameObject;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PropertyPool;
import peony.game.Server;
import peony.game.VMap;
import peony.game.buff.Buff;
import peony.game.chat.ChatMessage;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.ServiceEvent;
import peony.service.friend.PlayerRelation;
import peony.service.tong.TongException;

public class Party {

	private static final AtomicInteger ids = new AtomicInteger(0);

	public PartyService service;

	public int max;

	public PartyMember leader;

	public List<PartyMember> members;

	public int id;

	public PropertyPool pool;

	public Party(PartyService service, Player leader, int serial) {
		this(service, leader, 6, serial);
	}

	public Party(PartyService service, Player leader, int max, int serial) {
		this.id = ids.incrementAndGet();
		this.service = service;
		this.max = max;
		this.leader = new PartyMember(leader, true);
		this.pool = new PropertyPool();
		members = new ArrayList<PartyMember>(6);
		members.add(this.leader);
		leader.party = this;
		leader.state |= GameObject.STATE_PARTY;
		leader.state |= GameObject.STATE_PARTY_LEADER;
		Packet pt = new Packet(OpCode.PARTY_CREATE_SERVER);
		pt.putInt(serial);
		leader.send(pt);
		sendPartyInfo();
		service.partyCreated(this);
		service.memberAdded(this, this.leader);
	}

	public int getCount() {
		return members.size();
	}

	public synchronized PartyMember addMember(Player player) throws PartyFullException {
		if (members.size() >= max)
			throw new PartyFullException();
		PartyMember member = new PartyMember(player, false);
		members.add(member);
		player.party = this;
		player.state |= GameObject.STATE_PARTY;
		sendPartyInfo();
		service.memberAdded(this, member);
		for (PartyMember m : members) {
			if (m.player != player) {
				Server.server.getEventManager().addEvent(
						new ServiceEvent(ServiceEvent.EVENT_INTERACT, m.player,
								player, PlayerRelation.INTERACT_TEAM));
			}
		}
		return member;
	}

	public synchronized void sendPartyInfo() {
		Packet pt = new Packet(OpCode.PARTY_INFO_SERVER);
		pt.put(members.size());
		for (PartyMember member : members) {
			pt.putInt(member.getId());
			pt.putString(member.getName());
			pt.put(member.getLevel());
			pt.put(member.getClazz());
			pt.put(member.getSex());
			pt.put(member.getHpPercent());
			pt.put(member.getMpPercent());
			pt.put(member.getStatus());
		}
		broadcast(pt);
	}
	
	public synchronized void sendPartyInfo(Player p){
		Packet pt = new Packet(OpCode.PARTY_INFO_SERVER);
		pt.put(members.size());
		for (PartyMember member : members) {
			pt.putInt(member.getId());
			pt.putString(member.getName());
			pt.put(member.getLevel());
			pt.put(member.getClazz());
			pt.put(member.getSex());
			pt.put(member.getHpPercent());
			pt.put(member.getMpPercent());
			pt.put(member.getStatus());
		}
		p.send(pt);
	}

	public synchronized void kick(int id) {
		remove(id, true);
	}

	public synchronized List<Buff> getAreaBuffs(VMap map) {
		List<Buff> l = new ArrayList<Buff>();
		for (PartyMember member : members) {
			Player p = member.player;
			if (p.systemState == Player.SYSTEMSTATE_READY && p.isAlive()
					&& map != null && p.getVMap() == map) {
				for (Buff buff : p.skills.getAreaBuffs()) {
					l.add(buff);
				}
			}
		}
		return l;
	}

	/**
	 * 移除队员，kick代表是否是由队长提出队伍的还是队员自己离开
	 * 
	 * @param id
	 * @param kick
	 */
	protected synchronized void remove(int id, boolean kick) {
		PartyMember removed = null;
		if (members.size() == 2) { // 如果只剩两个人，有一个人要退出就解散队伍
			destory();
		} else {
			if (id == leader.player.id) { // 如果移除的是队长，如果队伍就一个人在线那么就解散，如果有多个人在线，那么选择顺位的一个做队长
				if (members.size() == 1) {
					destory();
					leader.player.state &= ~GameObject.STATE_PARTY;
					leader.player.state &= ~GameObject.STATE_PARTY_LEADER;
					leader.player.moveType |= GameObject.MOVE_STATE;
					removed = leader;
				} else {
					removed = leader;
					members.remove(leader);
					leader.player.state &= ~GameObject.STATE_PARTY;
					leader.player.state &= ~GameObject.STATE_PARTY_LEADER;
					leader.player.moveType |= GameObject.MOVE_STATE;
					leader = members.get(0);
					leader.player.state |= GameObject.STATE_PARTY;
					leader.player.state |= GameObject.STATE_PARTY_LEADER;
					leader.player.moveType |= GameObject.MOVE_STATE;
					leader.isLeader = true;
				}
			} else {
				Iterator<PartyMember> ite = members.iterator();
				while (ite.hasNext()) {
					PartyMember member = ite.next();
					if (member.getId() == id) {
						ite.remove();
						removed = member;
						member.player.state &= ~GameObject.STATE_PARTY;
						leader.player.moveType |= GameObject.MOVE_STATE;
						break;
					}
				}
			}
			if (removed != null) {
				removed.player.party = null;
				if (kick) {
					Packet pt = new Packet(OpCode.PARTY_KICK_SERVER);
					removed.player.send(pt);
				} else {
					Packet pt = new Packet(OpCode.PARTY_LEAVE_SERVER);
					removed.player.send(pt);
				}
				sendPartyInfo();
				service.memberLeaved(this, removed);
				if(leader.player.party != null){
					String msg = MessageFormat.format(peony.Messages.STRING_00039,removed.player.name);
					ChatMessage cm = new ChatMessage(ChatOption.PARTY, -1, -1,peony.Messages.STRING_00004,
							removed.player.id, msg, null);
					cm.sessions = leader.player.party.getSessions();
					Server.server.getServiceRegistry().getChatService().addChatMessage(cm);
				}	
			}
		}
	}

	public synchronized void leave(int id) {
		remove(id, false);
	}

	public synchronized void destory() {
		Packet pt = new Packet(OpCode.PARTY_DESTORY_SERVER);
		for (PartyMember member : members) {
			member.player.party = null;
			member.player.send(pt);
		}
		service.partyDestroied(this);
		for (PartyMember member : members) {
			member.player.state &= ~GameObject.STATE_PARTY;
			member.player.state &= ~GameObject.STATE_PARTY_LEADER;
			member.player.moveType |= GameObject.MOVE_STATE;
		}
		members.clear();
	}

	public synchronized void broadcast(Packet pt) {
		for (PartyMember member : members) {
			if (member.player != null) {
				member.player.send(pt);
			}
		}
	}

	public synchronized void broadcast(Packet pt, Player p) {
		for (PartyMember member : members) {
			if (member.player != null && member.player != p) {
				member.player.send(pt);
			}
		}
	}

	public synchronized ClientSession[] getSessions() {
		ClientSession[] ret = new ClientSession[members.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = members.get(i).player.session;
		}
		return ret;
	}

	public boolean isFull() {
		return members.size() >= max;
	}

	public synchronized boolean contains(int instanceId) {
		for (PartyMember member : members) {
			if (member.player.instanceId == instanceId)
				return true;
		}
		return false;
	}

	/*
	 * 获取在同一地图，并且在指定范围内的所有队员，并且要求队员的状态是Ready
	 */
	public synchronized List<Player> getPlayerInRange(VMap map, int range, int x, int y) {
		List<Player> l = new ArrayList<Player>(members.size());
		for (PartyMember member : members) {
			Player player = member.player;
			if (player.map.map == map
					&& player.systemState == Player.SYSTEMSTATE_READY
					&& Math.abs(player.x - x) < range
					&& Math.abs(player.y - y) < range) {
				l.add(player);
			}
		}
		return l;
	}

	public synchronized PartyMember memberInfo(int id) {
		for (PartyMember member : members) {
			if (member.getId() == id)
				return member;
		}
		return null;
	}

	public synchronized void transLeader(int id, int targetId)throws TransLeaderException {
		PartyMember source = memberInfo(id);
		PartyMember target = memberInfo(targetId);
		if(target==null||target.player.party!=source.player.party)
		{
			throw new TransLeaderException(peony.Messages.STRING_00040);
		}
//		leader=source;
		source.player.state |= GameObject.STATE_PARTY;
		source.player.state &= ~GameObject.STATE_PARTY_LEADER;
		source.player.moveType |= GameObject.MOVE_STATE;
		source.isLeader=false;
		leader = target;
		leader.player.state |= GameObject.STATE_PARTY;
		leader.player.state |= GameObject.STATE_PARTY_LEADER;
		leader.player.moveType |= GameObject.MOVE_STATE;
		leader.isLeader = true;
		sendPartyInfo();
	}
	
	public Player getPartyFriend(int playerId){
		for(PartyMember pm :members){
			if(pm.player.id != playerId){
				return pm.player;
			}
		}
		return null;
	}
}
