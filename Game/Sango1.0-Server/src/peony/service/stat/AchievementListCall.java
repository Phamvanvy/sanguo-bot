package peony.service.stat;

import java.util.ArrayList;
import java.util.List;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.duel.DuelService;

public class AchievementListCall extends ClientSessionAsyncCall{
	
	int serial;
	int personId;
	Player player = null;
	
	public AchievementListCall(ClientSession session,Packet packet) {
		super(session);
		serial = packet.getInt();
		personId = packet.getInt();
		player = (Player) session.getClient();
		
	}

	public void callFinish() throws Exception {
		if(success){
			int count = 0;
			int pointCount = 0;
			StatService service = Server.server.getServiceRegistry().getStatService();
			int size = service.type2AchiveId.size();
			if (player != null) {
				Packet pt = new Packet(OpCode.PERSONAL_ACHIEVEMENT_SERVER);
				pt.putInt(serial);
				pt.putShort(size);
				String[] catagoryNames = StatService.catagoryNames;
				Player person = (Player)ObjectAccessor.getPlayer(personId);
				if(person == null){
					person =  Server.server.getServiceRegistry().getFameService().getStatue(personId);
				}
				DuelService duelService = Server.server.getServiceRegistry().getDuelService();
				if(person == null && duelService!=null)
					person = duelService.getStatue(personId);
				if(person == null){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.PERSONAL_ACHIEVEMENT_CLIENT, peony.Messages.STRING_00088);
					return;
				}
				for (int i:service.type2AchiveId.keySet()) {
					int finished = 0;
					int point = 0;
					List<Integer> list = service.type2AchiveId.get(i);
					if(i == 5){
						List<Integer> achs = new ArrayList<Integer>();
						for(int id : list){
							if(id == 138 || id == 139 || id == 140){
								continue;
							}
							achs.add(id);
						}
						for(int j=0;j<service.factionIndex.length;j++){
							if(person.faction!=service.faction[j]){
								achs.add(service.factionIndex[j]);
							}
						}
						list = new ArrayList<Integer>(achs);
					}
					pt.putString(catagoryNames[i]);
					pt.putInt(list.size());
					if(person.id>0){
						service.processQuest(person);
						service.countJewelOnEquipment(person,false);
					}
					for (int achId : list) {
						Achievement ach = service.getAchievementById(achId);
						String finishTime = service.getTime(person, ach);
						ach.acomplish = (byte)(finishTime.equals("")?0:1);
						if (ach.acomplish == 1){
							count++;
							finished++;
							point += ach.point;
						    pointCount += ach.point;
						}
					}
					pt.putInt(finished);
					pt.putInt(point);
				}
				if(person.id>0){
					//完成成就点数的成就
					service.finishAchievePoint(person,pointCount);
					person.pool.setInt(Player.PROPERTY_ACHIEVEMENT_POINT, pointCount);
				}
				pt.putString(catagoryNames[catagoryNames.length-2]);
				pt.putInt(2);
				pt.putString(catagoryNames[catagoryNames.length-1]);
				pt.putInt(2);
				pt.putInt(count);
				pt.putInt(pointCount);
				player.send(pt);
				catagoryNames = null;
			}
		}
	}

	public void run() {
		addToClientSession();
	}

}
