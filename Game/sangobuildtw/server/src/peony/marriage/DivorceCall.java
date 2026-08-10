package peony.marriage;

import java.text.MessageFormat;

import org.apache.log4j.Logger;
import peony.common.ClientSessionAsyncCall;
import peony.game.ChatOption;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.chat.ChatMessage;
import peony.game.chat.ChatService;
import peony.game.mail.MailService;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.player.ActorCacheService;

public class DivorceCall extends ClientSessionAsyncCall {

	protected final Logger log = Logger.getLogger(DivorceCall.class);
	protected Player player;
	int annotherPersonId;
	protected int itemId;
	protected int instanceId;
	protected int type;
	public DivorceCall(ClientSession session, int playerId, int annotherPersonId, int itemId, int instanceId, int type) {
		super(session);
		this.player = ObjectAccessor.getPlayer(playerId);
		this.annotherPersonId = annotherPersonId;
		this.itemId = itemId;
		this.instanceId = instanceId;
		this.type = type;
	}

	public void callFinish() throws Exception {
		if(success){
			Packet packet0 = new Packet(OpCode.MAIL_NEW_SERVER); // 新邮件通知
			if(player != null){
				ChatService chatService = Server.server.getServiceRegistry().getChatService();
				ActorCacheService service = Server.server.getServiceRegistry().getActorCacheService();
				Packet packet = new Packet(OpCode.MARRIAGE_DIVORCE_SERVER);
				packet.putString(MessageFormat.format("尊敬的{0}: 您已經提出申請和您的伴侶{1}解除婚姻關系,經判定,离婚申請獲得批准.", service.find(player.id).name,service.find(annotherPersonId).name));
				player.send(packet);
				player.send(packet0);
				MailService mailService = Server.server.getServiceRegistry().getMailService();
				mailService.sendSystemMail(player.id, "系統", "您已离婚", MessageFormat.format("您与{0}婚姻宣告破裂", service.find(annotherPersonId).name), 0, null, 0, "");
				if(ObjectAccessor.getPlayer(annotherPersonId) != null){
					Packet packet1 = new Packet(OpCode.MARRIAGE_DIVORCE_SERVER);
					packet1.putString(MessageFormat.format("尊敬的{0}: 您的伴侶{1}已經提出申請和您解除婚姻關系,經判定,离婚申請獲得批准.", service.find(annotherPersonId).name,player.name));
					ObjectAccessor.getPlayer(annotherPersonId).send(packet1);
					ObjectAccessor.getPlayer(annotherPersonId).send(packet0);
				}
				if(type == 1){
//					mailService.sendSystemMail(annotherPersonId, "系统", "您已离婚", "您的伴侣"+service.find(player.id).name+"提出申请和您解除婚姻关系，经判定，离婚申请获得批准", 0, null, 0);
					mailService.sendSystemMail(annotherPersonId, "系統", "您已离婚", MessageFormat.format("您与{0}婚姻宣告破裂", service.find(player.id).name), 0, null, 0, "");
				}else if(type == 0){
					mailService.sendSystemMail(annotherPersonId, "系統", "您已离婚", MessageFormat.format("您与{0}婚姻宣告破裂", service.find(player.id).name), 0, null, 0, "");
				}
				chatService.addChatMessage(new ChatMessage(ChatOption.FACTION,-1,player.faction,"系統",player.faction,MessageFormat.format("{0}和{1}已离婚.", player.name,service.find(annotherPersonId).name),null));
				
			}
		}else{
			if(player != null && ObjectAccessor.getPlayer(annotherPersonId) != null){
				Packet packet = new Packet(OpCode.MARRIAGE_DIVORCE_SERVER);
				packet.putString(errorMessage);
				player.send(packet);
				ObjectAccessor.getPlayer(annotherPersonId).send(packet);
			}
		}
	}

	public void run() {
		MarriageService marriageService = Server.server.getServiceRegistry().getMarriageService();
		try {
			if(player.sex == 0){
				marriageService.divorce(player.id, annotherPersonId, itemId, instanceId, player.id, type);
			}else{
				marriageService.divorce(annotherPersonId, player.id, itemId, instanceId, player.id, type);
			}
		} catch (MarriageException e) {
			log.error(e, e);
			error(e, e.getMessage());
		}
		addToClientSession();
	}

}
