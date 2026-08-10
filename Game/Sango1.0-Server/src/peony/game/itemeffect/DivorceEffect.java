package peony.game.itemeffect;


import java.text.MessageFormat;

import peony.game.ChatOption;
import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.chat.ChatMessage;
import peony.game.chat.ChatService;
import peony.game.mail.MailService;
import peony.marriage.MarriageException;
import peony.marriage.MarriageService;
import peony.net.Packet;
import peony.service.ServiceEvent;
import peony.service.friend.RelationService;
import peony.service.player.ActorCacheService;

/**
 * 强制离婚效果。
 * @author lighthu
 */
public class DivorceEffect implements ItemEffect {
	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException(peony.Messages.STRING_00014);
		if(!(target instanceof Player))
			throw new UseItemException(peony.Messages.STRING_00014);
		Player p = (Player)source;
		MarriageService marriageService = Server.server.getServiceRegistry().getMarriageService();
		RelationService relationService = Server.server.getServiceRegistry().getRelationService();
		MailService mailService = Server.server.getServiceRegistry().getMailService();
		ActorCacheService service = Server.server.getServiceRegistry().getActorCacheService();
		ChatService chatService = Server.server.getServiceRegistry().getChatService();
		int annotherPersonId = relationService.get(p.id).mateId;
		if(annotherPersonId == -1){
			throw new UseItemException(peony.Messages.STRING_00228);
		}
		Packet packet0 = new Packet(OpCode.MAIL_NEW_SERVER); // 新邮件通知
		try {
			if(p.sex == 0){
				marriageService.divorce(p.id, annotherPersonId, item.template.id,item.instanceId, p.id, 1);
			}else if(p.sex == 1){
				marriageService.divorce(annotherPersonId, p.id, item.template.id, item.instanceId, p.id, 1);
			}
		} catch (MarriageException e) {
			
		}
		mailService.sendSystemMail(p.id, peony.Messages.STRING_00004, peony.Messages.STRING_01520, MessageFormat.format(peony.Messages.STRING_01521, service.find(annotherPersonId).name), 0, null, 0, "");
		mailService.sendSystemMail(annotherPersonId, peony.Messages.STRING_00004, peony.Messages.STRING_01520, MessageFormat.format(peony.Messages.STRING_01521, service.find(p.id).name), 0, null, 0, "");
		ObjectAccessor.getPlayer(p.id).send(packet0);
		chatService.addChatMessage(new ChatMessage(ChatOption.FACTION,-1,p.faction,peony.Messages.STRING_00004,p.faction,
				MessageFormat.format(peony.Messages.STRING_01522, p.name,service.find(annotherPersonId).name),null));
		if(ObjectAccessor.getPlayer(annotherPersonId) != null){
			Packet packet1 = new Packet(OpCode.MARRIAGE_DIVORCE_SERVER);
			packet1.putString(MessageFormat.format(peony.Messages.STRING_01523, service.find(annotherPersonId).name,p.name));
			ObjectAccessor.getPlayer(annotherPersonId).send(packet1);
			ObjectAccessor.getPlayer(annotherPersonId).send(packet0);
		}
		Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_DIVORCE,p,annotherPersonId));
	}
	
	public boolean isAsync(){
		return true;
	}
	
	public boolean needRemove() {
		return false;
	}
}
