package peony.game.admin;

import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ItemTemplate;
import peony.game.ItemUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class AdminSendMailCall extends ClientSessionAsyncCall {

	protected int serial;
	protected int playerId;
	protected String title;
	protected String content;
	protected int itemId;
	protected int count;
	protected boolean bind;
	protected int star;
	protected int hole;

	// * 发送信件
	// * serial int
	// * playerId int
	// * title string
	// * content string
	// * ItemId int (如果-1那么就是金钱)
	// * 数量 int
	// * 是否强制绑定 byte(0 不强制绑定 1 强制绑定)
	// * 星级 byte
	// * 孔数 byte(新增的孔数，不包括初始的孔数)
	public AdminSendMailCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.playerId = packet.getInt();
		this.title = packet.getString();
		this.content = packet.getString();
		this.itemId = packet.getInt();
		this.count = packet.getInt();
		this.bind = packet.get() == 1 ? true : false;
		this.star = packet.get();
		this.hole = packet.get();
	}

	public void callFinish() throws Exception {

	}

	public void run() {
		Actor actor = Server.server.getServiceRegistry().getActorCacheService()
				.find(playerId);
		if (actor != null) {
			if(count<=0){
				ErrorHandler.sendAdminErrorMessage(session, serial,
						OpCode.ADMIN_SENDMAIL_CLIENT, "物品数量错误，不能为:"+count);
				return;
			}
				
			if (itemId == -1) {
				Server.server.getServiceRegistry().getMailService()
						.sendSystemMail(playerId, "系统", title, content, count,
								null, 0, "GM");
			} else {
				boolean enhanceOk = true;
				GameItem item = ObjectAccessor.createGameItem(itemId);
				if (item != null) {
					if (item.template.bindType == ItemTemplate.BIND_USED
							&& bind) {
						item.bindInstance = 0;
					}
					if (item.template.newInstance
							|| item.template.isEquipment()) {
						count = 1;
					}
					if (item.template.isEquipment()) {
						if (star > 0 && star < 10) {
							ItemUtil.star(item, star);
						} else {
							if(star !=0 )
								enhanceOk = false;
						}
						if (hole > 0
								&& (hole <= (item.template.equipment.maxHole - item.template.equipment.initHole))) {
							ItemUtil.hole(item, hole);
						} else {
							if(hole !=0 )
								enhanceOk = false;
						}
					}
					if (enhanceOk) {
						Server.server.getServiceRegistry().getMailService()
								.sendSystemMail(playerId, "系统", title, content,
										0, item, count, "GM");
					} else {
						ErrorHandler.sendAdminErrorMessage(session, serial,
								OpCode.ADMIN_SENDMAIL_CLIENT, "数据错误");
					}
				} else {
					ErrorHandler.sendAdminErrorMessage(session, serial,
							OpCode.ADMIN_SENDMAIL_CLIENT, "没找到指定物品");
				}
			}
		} else {
			ErrorHandler.sendAdminErrorMessage(session, serial,
					OpCode.ADMIN_SENDMAIL_CLIENT, "该用户已下线");
		}
	}

}
