package peony.db;

import java.util.Date;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.CommonUtil;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.GameObjectRef;
import peony.game.ItemTemplate;
import peony.game.LogUtil;
import peony.game.Mail;
import peony.game.NoEnoughValueException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.mail.ItemMailAttachment;
import peony.game.mail.MailService;
import peony.game.mail.MoneyMailAttachment;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.levellimit.LevelLimitService;

public class MailPostCall extends ClientSessionAsyncCall {

	private static final Logger log = Logger.getLogger(MailPostCall.class);

	private int serial;
	private GameObjectRef ref;
	private String destName;
	private String title;
	private String content;
	private int price;
	private byte[] attachment;

	private int destId = -1;

	public MailPostCall(ClientSession session, int serial, GameObjectRef ref,
			String destName, String title, String content, int price,
			byte[] attachment) {
		super(session);
		this.serial = serial;
		this.destName = destName;
		this.title = title;
		this.content = content;
		this.price = price;
		this.attachment = attachment;
		this.ref = ref;
		Player p = (Player)session.getClient();
		if (attachment.length > 0) {
			LogUtil.logMailPostTry(p, destName, attachment, price);
		}
	}

	public void callFinish() throws Exception {
		if (success) {
			Packet pt = new Packet(OpCode.MAIL_POST_SERVER);
			pt.putInt(serial);
			session.send(pt);
			Player p = (Player) ObjectAccessor.getPlayer(destId);
			if (p != null
					&& (p.systemState == Player.SYSTEMSTATE_LOGINED || p.systemState == Player.SYSTEMSTATE_READY)) {
				pt = new Packet(OpCode.MAIL_NEW_SERVER);
				p.send(pt);
			}
			
			// 统计
			Server.server.getServiceRegistry().getRealtimeStatService().mailCounter++;
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.MAIL_POST_CLIENT, errorMessage);
		}
	}

	public void run() {
		if (price < 0)
			return;
		DBService dbService = Server.server.getServiceRegistry().getDbService();
		Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(destName);
		destId = dbService.playerDAO.getPlayerIdByName(destName);
		if (destId != -1) {
			Player p = (Player) ObjectAccessor.getGameObject(ref);
			if (p != null) {
				if(p.faction!=actor.faction){
					error(null,"不同阵营，不能发送飞鸽");
					addToClientSession();
					return;
				}
				if(Server.server.getServiceRegistry().getMailService().isForbid(p.id)){
					error(null,"已经被封，请联系游戏管理员");
					addToClientSession();
					return;
				}
				if (attachment.length > 0) {
					if (attachment[0] == 1 && attachment.length == 10) { // item
						int itemId = CommonUtil.getInt(attachment, 1);
						int instanceId = CommonUtil.getInt(attachment, 5);
						int count = attachment[9];
						if (count <= 0) {
							log.error("[MAILPOSTATTACK]" + LogUtil.getPlayerLogString(p));
							p.session.close();
							return;
						}
						ItemTemplate template = ObjectAccessor.getItemTemplate(itemId);
						if(template==null){
							log.error("[MAILPOSTATTACK]" + LogUtil.getPlayerLogString(p));
							p.session.close();
							return;
						}
						if(count>template.maxCount){
							error(null, "发送物品的数量大于最大堆叠数量.");
							addToClientSession();
							return;
						}
						LevelLimitService service = Server.server
						.getServiceRegistry().getLevelLimitService();
						if (p.level < 30 && service.check(itemId)) {
							error(null, "在您30级以前不能用飞鸽传送此物品");
							addToClientSession();
							return;
						}
						PlayerTransaction tx = p.newTransaction("NMA");
						GameItem item = p.bag.removeGameItem(itemId,
								instanceId, count, tx, true);
						if (item != null) {

							if (item.isBound()) {
								error(null, "不能发送绑定物品");
								tx.rollback();
							} else {
								ItemMailAttachment att = new ItemMailAttachment(
										item, count);
								Mail mail = new Mail();
								mail.setDestId(destId);
								mail.setSourceId(p.id);
								mail.setSourceName(p.name);
								mail.setTitle(title);
								mail.setContent(content);
								mail.setPrice(price);
								mail.setStatus(Mail.UNREADED);
								mail.setPostTime(new Date());
								mail.setValidTime(new Date());
								mail.setExpirationTime(new Date(System.currentTimeMillis()+MailService.EXPIRATION_TIME));
								mail.setAttachment(att);
								tx.commit();
								dbService.mailDAO.newEntity(mail);
								LogUtil.logMailPostOK(p, mail);
							}
						} else {
							error(null, "没有足够的物品");
							tx.rollback();
						}
					} else if (attachment[0] == 2 && attachment.length == 5) { // money
						int count = CommonUtil.getInt(attachment, 1);
						if (count <= 0) {
							log.error("[MAILPOSTATTACK]" + LogUtil.getPlayerLogString(p));
							p.session.close();
							return;
						}
						if (price > 0) {
							error(null, "发送金钱时不能带价");
							return;
						}
						PlayerTransaction tx = p.newTransaction("NMA");
						try {
							p.decMoney(count, tx, true);
							MoneyMailAttachment att = new MoneyMailAttachment(
									count);
							Mail mail = new Mail();
							mail.setDestId(destId);
							mail.setSourceId(p.id);
							mail.setSourceName(p.name);
							mail.setTitle(title);
							mail.setContent(content);
							mail.setPrice(price);
							mail.setStatus(Mail.UNREADED);
							mail.setPostTime(new Date());
							mail.setValidTime(new Date());
							mail.setExpirationTime(new Date(System.currentTimeMillis()+MailService.EXPIRATION_TIME));
							mail.setAttachment(att);
							tx.commit();
							dbService.mailDAO.newEntity(mail);
							LogUtil.logMailPostOK(p, mail);
						} catch (NoEnoughValueException e) {
							error(null, "没有足够的金钱");
							tx.rollback();
						}
					}
				} else {
					Mail mail = new Mail();
					mail.setDestId(destId);
					mail.setSourceId(p.id);
					mail.setSourceName(p.name);
					mail.setTitle(title);
					mail.setContent(content);
					mail.setPrice(0);
					mail.setStatus(Mail.UNREADED);
					mail.setPostTime(new Date());
					mail.setValidTime(new Date());
					mail.setExpirationTime(new Date(System.currentTimeMillis()+MailService.EXPIRATION_TIME));
					dbService.mailDAO.newEntity(mail);
				}
			}
		} else {
			error(null, "没有找到指定的角色");
		}
		addToClientSession();
	}
}
