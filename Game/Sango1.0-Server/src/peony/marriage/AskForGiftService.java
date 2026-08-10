package peony.marriage;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import ch.javasoft.util.intcoll.IntHashMap;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;
import peony.service.shop.ShopService;

public class AskForGiftService implements Service{
	
	protected AtomicInteger ids = new AtomicInteger(0);
    protected IntHashMap<AskForGiftRequest> id2request = new IntHashMap<AskForGiftRequest>();
    protected Map<Integer,Integer> player2Request = new HashMap<Integer,Integer>();
    
	public AskForGiftRequest newAppRequest(Player player,Player target){
		AskForGiftRequest request = new AskForGiftRequest(ids.incrementAndGet(),Time.currTime,player.ref(),target.ref());
		id2request.put(request.id, request);
		return request;
	}

	public void shutdown() {
		
		
	}

	public void startup() throws Exception {
		
		
	}
	
	/** 索要请求 */
	public void askForGiftInvite(ClientSession session,Packet packet){
		int serial = packet.getInt();
		int targetId = packet.getInt();
		int itemId = packet.getInt();
		int count = packet.getInt();
		Player p = (Player)session.getClient();
		GameItem item = ObjectAccessor.createGameItem(itemId);
		ShopService shopService = Server.server.getServiceRegistry().getShopService();
		if(p!=null && item !=null){
			if(canRequest(p)){
				Player targetPlayer = ObjectAccessor.getPlayer(targetId);
				String message = "";
				if(p.relations!=null){
					if(p.relations.mateId==targetId){
						if(p.sex == 0){
							message = MessageFormat.format(peony.Messages.STRING_00607, item.template.name);
						} else {
							message = MessageFormat.format(peony.Messages.STRING_00608, item.template.name);
						}
					}
				}
				if(targetPlayer != null){
					AskForGiftRequest request = newAppRequest(p, targetPlayer);
					player2Request.put(p.id, request.id);
					Packet pt = new Packet(OpCode.PAYFORME_INVIT__SERVER);
					pt.putInt(serial);
					pt.putInt(request.id);
					pt.putString(message);
					pt.putInt(itemId);
					pt.putString(item.template.name);
					int tempPrice = Math.round(shopService.getItemPrice(itemId)/36);
					float temp = shopService.getItemPrice(itemId)/36;
					DecimalFormat fnum =new DecimalFormat("##0.00");  
					String  pr=fnum.format(temp);    
//					String price = String.valueOf(pr);
					pt.putString(tempPrice<0?"0":pr);
//					pt.putInt(tempPrice<0?0:tempPrice);
					pt.putInt(count);
					pt.putString(fnum.format(temp*count));
					targetPlayer.send(pt);
				} else {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.PAYFORME_INVIT_CLIENT, peony.Messages.STRING_00609);
				}
			} else {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.PAYFORME_INVIT_CLIENT, peony.Messages.STRING_00610);
			}
	    } else {
	    	ErrorHandler.sendErrorMessage(session, serial, OpCode.PAYFORME_INVIT_CLIENT, peony.Messages.STRING_00611);
	    }
	}
	

	/** 玩家拒绝请求 */
	public void askForGiftReject(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		int requestId = packet.getInt();
		Player player = (Player) session.getClient();
		if (player != null) {
			AskForGiftRequest request = getAndRemoveRequest(requestId);
			if (request != null) {
				Player source = (Player) ObjectAccessor.getGameObject(request.ref);
				String message = "";
				if (source != null) {
					if(player.relations!=null && player.relations.mateId == source.id){
						if(source.sex == 0){
						    message = peony.Messages.STRING_00612;
						} else {
							message = peony.Messages.STRING_00613;
						}
					} 
					id2request.remove(source.id);
					source.message(-1, message, -1, -1);
//					ErrorHandler.sendErrorMessage(source.session, -1,
//							OpCode.PAYFORME_REJECT_CLIENT,MessageFormat.format("{0}",
//									message));
				}
			}
		}
	}
	
	protected AskForGiftRequest getAndRemoveRequest(int requestId) {
		return id2request.remove(requestId);
	}
	
	protected boolean canRequest(Player p){
		if(player2Request.containsKey(p.id)){
			AskForGiftRequest request = id2request.get((player2Request.get(p.id)));
			if(request!=null){
				Player targetPlayer = ObjectAccessor.getPlayer(request.targetRef.id);
				if(targetPlayer == null){
					player2Request.remove(p.id);
					return true;
				}else{
					return false;
				}
			}
		}
		return true;
	}
	
	public void removeRequest(Player p){
		Iterator<Integer> it = id2request.keySet().iterator();
		while(it.hasNext()){
			int requestId = it.next();
			AskForGiftRequest request = id2request.get(requestId);
			if(request!=null){
				if(p.id == request.targetRef.id){
			    	player2Request.remove(request.ref.id);
			    }
		   }
	   }
	}
}
