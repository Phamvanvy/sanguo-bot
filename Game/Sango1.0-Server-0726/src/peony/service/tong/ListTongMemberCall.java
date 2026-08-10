package peony.service.tong;

import java.text.MessageFormat;
import java.util.List;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

/**
 * 取军团成员列表。
 * serial	int
 * start	short		页号，0表示第一页
 * page		short		页大小
 *
 * 返回军团成员列表。
 * serial	int
 * tid		int			军团ID
 * tname	String		军团名称
 * slogan	String		军团公告
 * duty		int			本用户的职务
 * title	String		本用户的头衔
 * pcount	short		总页数
 * pno		short		当前页号（0表示第一页）
 * count	short		返回记录数
 * 	循环N次
 *		pid		int			角色ID
 *		pname	String		角色名称
 *		online	byte		是否在线，0 - 不在线、1 - 在线
 *		level	short		级别（只对在线用户有效）
 *		sex		byte		性别（只对在线用户有效）
 *		clazz	byte		职业（只对在线用户有效）
 *		duty	int			职务
 *		title	String		头衔
 *		honor	int			荣誉
 *		forbid	byte		是否禁言，1 - 禁言、0 - 未禁言
 */
public class ListTongMemberCall extends ClientSessionAsyncCall {
	protected final Logger log = Logger.getLogger(ListTongMemberCall.class);
	protected int serial;
	protected int startPage;
	protected int pageSize;
	protected Player player;
	protected TongService tongService;
	protected int totalCount;
	protected Tong tong;
	protected TongMember self;
	protected List<TongMember> resultList;
	protected int onlines;

	public ListTongMemberCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.startPage = packet.getShort();
		this.pageSize = packet.getShort();
		player = (Player)session.getClient();
		tongService = Server.server.getServiceRegistry().getTongService();
	}

	public void callFinish() throws Exception {
		if (success) {
			// 创建成功，下发确认包
			Packet pt = new Packet(OpCode.TONG_LIST_SERVER);
			pt.putInt(serial);
			pt.putInt(tong.id);
			pt.putString(tong.name);
			pt.putString(tong.slogan);
			pt.putInt(self.duty);
			pt.putString(self.title);
			int pageCount = (totalCount + pageSize - 1) / pageSize;
			if (pageCount == 0) {
				pageCount = 1;
			}
			pt.putShort(pageCount);
			pt.putShort(startPage >= pageCount ? pageCount - 1 : startPage);
			int retCount = resultList.size();
			pt.putShort(retCount);
			pt.putString(MessageFormat.format("{0}/{1}", onlines,totalCount));
			for (int i = 0; i < retCount; i++) {
				TongMember tm = resultList.get(i);
				pt.putInt(tm.actor.id);
				pt.putString(tm.actor.name);
				pt.put(tm.actor.online ? 1 : 0);
				pt.putShort(tm.actor.level);
				pt.put(tm.actor.sex);
				pt.put(tm.actor.clazz);
				pt.putInt(tm.duty);
				pt.putString(tm.title);
				pt.putInt(tm.honor);
				pt.put(tm.forbid ? 1 : 0);
				pt.put(tm.battleTag);
			}
			session.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.TONG_LIST_CLIENT, errorMessage);
		}
	}

	public void run() {
		// 检查是否在军团内
		self = tongService.getPlayerInfo(player.id);
		tong = tongService.getPlayerTong(player.id);
		if (tong == null || self == null) {
			error(null, "你还没有加入一个军团");
			addToClientSession();
			return;
		}
		totalCount = tongService.getMemberCount(tong, TongService.NORMAL, TongService.CHAIRMAN);
		resultList = tongService.listMember(tong, startPage * pageSize, pageSize);
		onlines = tongService.getOnlineMembers(tong).size();
		addToClientSession();
	}
}
