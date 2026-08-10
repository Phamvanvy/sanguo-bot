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
import peony.service.friend.RelationService;
import peony.service.player.ActorCacheService;

/**
 * 强制离婚效果。
 * @author lighthu
 */
public class DivorceEffect implements ItemEffect {
	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException("錯誤的目標");
		if(!(target instanceof Player))
			throw new UseItemException("錯誤的目標");
		Player p = (Player)source;
		MarriageService marriageService = Server.server.getServiceRegistry().getMarriageService();
		RelationService relationService = Server.server.getServiceRegistry().getRelationService();
		MailService mailService = Server.server.getServiceRegistry().getMailService();
		ActorCacheService service = Server.server.getServiceRegistry().getActorCacheService();
		ChatService chatService = Server.server.getServiceRegistry().getChatService();
		int annotherPersonId = relationService.get(p.id).mateId;
		if(annotherPersonId == -1){
			throw new UseItemException("您還是形單影只,怎能解除婚約");
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
		mailService.sendSystemMail(p.id, "系統", "您已离婚", MessageFormat.format("您与{0}婚姻宣告破裂", service.find(annotherPersonId).name), 0, null, 0, "");
		mailService.sendSystemMail(annotherPersonId, "系統", "您已离婚", MessageFormat.format("您与{0}婚姻宣告破裂", service.find(p.id).name), 0, null, 0, "");
		ObjectAccessor.getPlayer(p.id).send(packet0);
		chatService.addChatMessage(new ChatMessage(ChatOption.FACTION,-1,p.faction,"系統",p.faction,
				MessageFormat.format("{0}和{1}已离婚.", p.name,service.find(annotherPersonId).name),null));
		if(ObjectAccessor.getPlayer(annotherPersonId) != null){
			Packet packet1 = new Packet(OpCode.MARRIAGE_DIVORCE_SERVER);
			packet1.putString(MessageFormat.format("尊敬的{0}: 您的伴侶{1}已經提出申請和您解除婚姻關系,經判定,离婚申請獲得批准.", service.find(annotherPersonId).name,p.name));
			ObjectAccessor.getPlayer(annotherPersonId).send(packet1);
			ObjectAccessor.getPlayer(annotherPersonId).send(packet0);
		}
	}
	
	public boolean isAsync(){
		return true;
	}
}
