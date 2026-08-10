package peony.game;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import com.pip.sanguo.data.Shop;
import com.pip.sanguo.data.Shop.BuyRequirement;
import peony.auction.Auction;
import peony.game.attendant.Attendant;
import peony.game.instance.BossDef;
import peony.game.instance.BossScoreService;
import peony.game.instance.NormalInstance;
import peony.game.mail.ItemMailAttachment;
import peony.game.mail.MailAttachment;
import peony.game.mail.MoneyMailAttachment;
import peony.game.party.Party;
import peony.game.party.PartyMember;
import peony.game.party.PartyService;
import peony.game.salary.SalaryService;
import peony.game.skill.Skill;
import peony.net.ClientSession;
import peony.service.account.Account;
import peony.service.activity.Activity;
import peony.service.player.ActorCacheService;
import peony.service.player.PlayerService;
import peony.service.read.Book;
import peony.service.shop.IBuyObject;
import peony.service.stat.StatService;
import peony.service.tong.Tong;
import peony.service.tong.apply.TongBattleApply;
import peony.service.tong.battle.TongBattleSide;
import peony.service.towerdefend.TowerDefend;

/**
 * 日志相关的工具函数类。
 * @author lighthu
 */
public class LogUtil {
	public static final Logger log = Logger.getLogger(LogUtil.class);
	public static final String UNKNOW = "UNKNOW,UNKNOW";
	private static final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/**
	 * 生成角色日志信息字符串（旧版本）。
	 * @param player
	 * @return
	 */
	public static final String getPlayerLogString(Player player){
		StringBuilder sb = new StringBuilder(20);
		getPlayerLogString(sb, player);
		return sb.toString();
	}
	
	/**
	 * 生成物品信息字符串（旧版本）。
	 * @param item
	 * @param count
	 * @return
	 */
	public static final String getGameItemString(GameItem item, int count) {
		StringBuilder sb = new StringBuilder(40);
		getGameItemString(sb, item, count);
		return sb.toString();
	}
	
	/**
	 * 取得角色日志信息字符串（新版本）。
	 * @param out
	 * @param player
	 */
	public static void getPlayerLogString(StringBuilder out, Player player) {
		if (player == null) {
			out.append("ID[-1]ACC[-1]");
		} else {
			out.append("ID[");
			out.append(player.id);
			out.append("]ACC[");
			out.append(player.accountId);
			out.append("]");
//			if(player.getAccount().getCmccUserId()!="" && player.getAccount().getCity()!=""){
//				out.append("CITY[");
//				out.append(player.getAccount().getCity());
//				out.append("]");
//			}
		}
	}
	
	/**
	 * 取得角色日志信息字符串（新版本）。
	 * @param out
	 * @param player
	 */
	public static void getPlayerLogString(StringBuilder out, Actor player) {
		if (player == null) {
			out.append("ID[-1]ACC[-1]");
		} else {
			out.append("ID[");
			out.append(player.id);
			out.append("]ACC[");
			out.append(player.accountId);
			out.append("]");
		}
	}
	
	/**
	 * 生成角色快照信息。
	 * @param out
	 * @param player
	 */
	public static void getPlayerSnapshot(StringBuilder out, Player player) {
		out.append("LVL(").append(player.level).append(")MAP(").append(player.map == null ? -1 : player.map.id);
		out.append(")X(").append(player.x).append(")Y(").append(player.y);
		out.append(")MEY(").append(player.money).append(")EXP(").append(player.exp);
		Account acc = player.getAccount();
		if (acc == null) {
			out.append(")IME(NA");
		} else {
			out.append(")IME(").append(acc.getLongIMoney());
		}
		out.append(")HNR(").append(player.honor).append(")CRD(").append(player.credit);
		out.append(")CLS(").append(player.clazz).append(")HP(").append(player.hp);
		out.append(")MP(").append(player.mp).append(")MHP(").append(player.maxhp);
		out.append(")MMP(").append(player.maxmp).append(")AGI(").append(player.agility);
		out.append(")STR(").append(player.strength).append(")INT(").append(player.intellect);
		out.append(")STA(").append(player.stamina).append(")");
	}
	
	/**
	 * 生成角色属性信息。
	 * @param out
	 * @param player
	 */
	public static void getExtraPlayerSnapshot(StringBuilder out,Player player){
		out.append("APU(").append(Math.round(player.attackpowerup));
		out.append(")APD(").append(Math.round(player.attackpowerdown));
		out.append(")SPOW(").append(Math.round(player.spellpower));
		out.append(")SHEAL(").append(Math.round(player.spellheal));
		out.append(")DEF(").append(Math.round(player.defense));
		out.append(")SDEF(").append(Math.round(player.spelldefense));
		out.append(")CRI(").append(Math.round(player.critical*100));
		out.append(")SCRI(").append(Math.round(player.spellcritical*100));
		out.append(")HIT(").append(Math.round(player.hit*100));
		out.append(")SHIT(").append(Math.round(player.spellhit*100));
		out.append(")DODGE(").append(Math.round(player.dodge*100));
		out.append(")SDODGE(").append(Math.round(player.spelldodge*100));
		out.append(")ANT(").append(Math.round(player.anticrit*100));
		out.append(")DEFPER(").append(Math.round(player.defensePercent*100));
		out.append(")HERESTORE(").append(player.healthrestore);
		out.append(")MARESTORE(").append(player.manarestore).append(")");
	}
	
	/**
	 * 生成角色位置信息。
	 * @param out
	 * @param player
	 */
	public static void getPlayerPosition(StringBuilder out, Player player) {
		out.append("MAP[").append(player.map == null ? -1 : player.map.id);
		out.append("]X[").append(player.x).append("]Y[").append(player.y).append("]");
	}

	/**
	 * 生成角色位置信息。
	 * @param out
	 * @param player
	 */
	public static void getPlayerMap(StringBuilder out, Player player) {
		out.append("MAP[").append(player.map == null ? -1 : player.map.id).append("]");
	}
	
	/**
	 * 生成怪物位置信息。
	 * @param out
	 * @param player
	 */
	public static void getCreaturePosition(StringBuilder out, Creature c) {
		out.append("MAP[").append(c.map == null ? -1 : c.map.id);
		out.append("]X[").append(c.x).append("]Y[").append(c.y).append("]");
	}

	/**
	 * 取得物品信息字符串（新版本）。
	 * @param out
	 * @param player
	 */
	public static void getGameItemString(StringBuilder out, GameItem item, int count) {
		if (item == null) {
			return;
		}
		if (item.template.equipment == null) {
			out.append("ITM(").append(item.template.id).append(",");
			out.append(item.instanceId).append(",").append(count);
		} else {
			out.append("EQU(").append(item.template.id).append(",");
			out.append(item.instanceId);
		}
		if (item.object != null) {
			out.append(",");
			item.object.dump(out);
		}
		out.append(")");
	}
	
	/**
	 * 取得物品信息字符串（新版本）。
	 * @param out
	 * @param player
	 */
	public static void getGameItemString(StringBuilder out, ItemTemplate item, int count) {
		if (item == null) {
			return;
		}
		if (item.equipment == null) {
			out.append("ITM(").append(item.id).append(",");
			out.append(-1).append(",").append(count);
		} else {
			out.append("EQU(").append(item.id).append(",");
			out.append(-1);
		}
		out.append(")");
	}
	
	/**
	 * 取得附件信息字符串（新版本）。
	 * @param out
	 * @param attach
	 */
	public static void getAttachmentString(StringBuilder out, MailAttachment attach) {
		if (attach == null) {
			return;
		}
		if (attach instanceof MoneyMailAttachment) {
			MoneyMailAttachment att = (MoneyMailAttachment)attach;
			getMoneyString(out, att.getCount());
		} else if (attach instanceof ItemMailAttachment) {
			ItemMailAttachment att = (ItemMailAttachment)attach;
			getGameItemString(out, att.getGameItem(), att.getCount());
		}
	}
	
	/**
	 * 把二进制数据转换为日志文本。
	 * @param out
	 * @param buf
	 */
	public static void getBinaryString(StringBuilder out, byte[] buf) {
		for (int i = 0; i < buf.length; i++) {
			if (i > 0) {
				out.append(" ");
			}
			String s = Integer.toHexString(buf[i] & 0xFF);
			if (s.length() == 1) {
				out.append("0");
			}
			out.append(s);
		}
	}
	
	/**
	 * 取得金钱信息字符串（新版本）。
	 * @param out
	 * @param money
	 */
	public static void getMoneyString(StringBuilder out, int money) {
		if (money == 0) {
			return;
		}
		out.append("MEY(");
		out.append(money);
		out.append(")");
	}
	
	/**
	 * 获得坐骑信息字符串。
	 * @param horse
	 */
	public static String getHorseString(Horse horse) {
		StringBuilder sb = new StringBuilder();
		getHorseString(sb, horse);
		return sb.toString();
	}
	
	/**
	 * 获得坐骑信息字符串。
	 * @param out
	 * @param horse
	 */
	public static void getHorseString(StringBuilder out, Horse horse) {
		out.append("HOS(");
		out.append("TID=").append(horse.template.id).append(",");
		out.append("HID=").append(horse.itemId).append(",");
		out.append("NM=").append(filter(horse.name)).append(",");
		out.append("IID=").append(horse.instanceId).append(",");
		out.append("LVL=").append(horse.level).append(",");
		out.append("FIX=").append(horse.fixCount).append(",");
		out.append("IMAG=").append(horse.imageId).append(",");
		out.append("IMAGCHANGE=").append(horse.imageIdChange).append(",");
		out.append("SKLS=");
		for (int i = 0; i < horse.skills.size(); i++) {
			if (i > 0) {
				out.append("+");
			}
			out.append(horse.skills.get(i).getId());
		}
		out.append(",SKLLEVEL=");
		for (int i = 0; i < horse.skills.size(); i++) {
			if (i > 0) {
				out.append("+");
			}
			out.append(horse.skills.get(i).getLevel());
		}
		out.append(")");
	}
	
	/**
	 * 过滤一个字符串，以便此字符串能写入到日志文件中。日志文件中字符串数据不允许包含以下字符：\r\n[](),+/=
	 * @param value
	 */
	public static String filter(String value) {
		// 先扫描是否有需要过滤的字符
		int firstPos = -1;
		int len = value.length();
		for (int i = 0; i < len && firstPos == -1; i++) {
			char ch = value.charAt(i);
			switch (ch) {
			case '\r':
			case '\n':
			case '[':
			case ']':
			case '(':
			case ')':
			case ',':
			case '+':
			case '/':
			case '=':
				firstPos = i;
				break;
			}
		}
		if (firstPos == -1) {
			// 没有找到需要过滤的字符
			return value;
		}
		
		// 从第一个需要替换的字符开始，往后过滤
		StringBuilder out = new StringBuilder(value.length());
		out.append(value.substring(0, firstPos));
		for (int i = firstPos; i < len; i++) {
			char ch = value.charAt(i);
			switch (ch) {
			case '\r':
			case '\n':
			case '[':
			case ']':
			case '(':
			case ')':
			case ',':
			case '+':
			case '/':
			case '=':
				out.append(' ');
				break;
			default:
				out.append(ch);
				break;
			}
		}
		return out.toString();
	}
	
	/**
	 * 把一个掉落物品组转换为日志格式。
	 */
	public static void getGainString(StringBuilder out, Gain gain) {
		if (gain.getMoney() > 0) {
			out.append("MEY(").append(gain.getMoney()).append(")");
		}
		if (gain.getExp() > 0) {
			out.append("EXP(").append(gain.getExp()).append(")");
		}
		if (gain.getCredit() > 0) {
			out.append("CRD(").append(gain.getCredit()).append(")");
		}
		if (gain.getHonor() > 0) {
			out.append("HNR(").append(gain.getHonor()).append(")");
		}
		if (gain.getSalary() > 0) {
			out.append("SAL(").append(gain.getSalary()).append(")");
		}
		for (GainItem item : gain.getGainItems()) {
			LogUtil.getGameItemString(out, item.getItem(), item.getCount());
		}
	}
	
	/**
	 * 把购买需求项目转换为日志中的格式。
	 * @param out
	 * @param req
	 */
	public static void getBuyRequirementString(StringBuilder out, BuyRequirement req, int count, int discount) {
		if (!req.deduct) {
			return;
		}
		switch (req.type) {
		case Shop.TYPE_HONOR:
		{
			long value = (long)req.amount * count * discount / 100;
			out.append("HNR(").append(value).append(")");
			break;
		}
		case Shop.TYPE_IMONEY:
		{
		    long value = (long)req.amount * count * discount;
		    out.append("IME(").append(value).append(")");
			break;
		}
		case Shop.TYPE_ITEM:
		{
			ItemTemplate it = ObjectAccessor.getItemTemplate(req.item.id);
			int needCount = req.amount * count;
			getGameItemString(out, it, needCount);
			break;
		}
		case Shop.TYPE_MONEY: 
		{
			long value = (long)req.amount * count * discount / 100;
			out.append("MEY(").append(value).append(")");
			break;
		}
		}
	}
	
	/** 记录玩家尝试发布拍卖 */
	public static void logAuctionCreateTry(Player p, int startPrice, int endPrice, GameItem item, int count) {
		StringBuilder sbuf = new StringBuilder(100);
		sbuf.append("[AUCTIONCREATETRY]");
		LogUtil.getPlayerLogString(sbuf, p);
		sbuf.append("ITEM[");
		LogUtil.getGameItemString(sbuf, item, count);
		sbuf.append("]STARTPRICE[").append(startPrice).append("]ENDPRICE[").append(endPrice).append("]BALANCE[").append(p.money).append("]");
		log.info(sbuf.toString());
	}
	
	/** 记录玩家发布拍卖成功 */
	public static void logAuctionCreateOK(Player p, GameItem item, int count, Auction auction) {
		StringBuilder sbuf = new StringBuilder(100);
		sbuf.append("[AUCTIONCREATE]");
		LogUtil.getPlayerLogString(sbuf, p);
		sbuf.append("ITEM[");
		LogUtil.getGameItemString(sbuf, item, count);
		sbuf.append("]TID[").append(auction.getId()).append("]");
		log.info(sbuf.toString());
	}
	
	/** 记录尝试删除拍卖（系统或玩家） */
	public static void logDeleteAuctionTry(Auction auction) {
		StringBuilder sbuf = new StringBuilder(100);
		sbuf.append("[AUCTIONDELETETRY]TID[").append(auction.getId()).append("]PLAYER[");
		sbuf.append(auction.getPlayerId()).append("]PRICE[").append(auction.getCurrentPrice()).append("]");
		log.info(sbuf.toString());
	}
	
	/** 记录删除拍卖成功（系统或玩家）*/
	public static void logDeleteAuctionOK(Auction auction) {
		StringBuilder sbuf = new StringBuilder(100);
		sbuf.append("[AUCTIONDELETE]TID[").append(auction.getId()).append("]LASTPLAYER[");
		sbuf.append(auction.getLastPlayerId()).append("]PRICE[").append(auction.getCurrentPrice()).append("]");
		log.info(sbuf.toString());
	}
	
	/** 记录玩家尝试拍卖出价 */
	public static void logAuctionBuyTry(int auctionId, int price, Player p, GameItem item, int count) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[AUCTIONTRY]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ITEM[");
		LogUtil.getGameItemString(sb, item, count);
		sb.append("]TID[").append(auctionId).append("]PRICE[").append(price).append("]");
		log.info(sb.toString());
	}
	
	/** 记录发送带附件的系统邮件 */
	public static void logSystemMail(int playerId, int attMoney, GameItem attItem, int attCount, String cause, int mailId) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[SYSTEMMAIL]ID[").append(playerId).append("]ATTACH[");
		LogUtil.getMoneyString(sb, attMoney);
		LogUtil.getGameItemString(sb, attItem, attCount);
		sb.append("]CAUSE[").append(cause).append("]MID[").append(mailId).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家拍卖出价成功 */
	public static void logAuctionBuy(Player p, Auction auction, int price) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[AUCTION]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ITEM[");
		LogUtil.getGameItemString(sb, auction.getItem(), auction.getCount());
		sb.append("]TID[").append(auction.getId()).append("]PRICE[").append(price).append("]");
		log.info(sb.toString());
	}
	
	/** 记录拍卖成交 */
	public static void logAuctionSucc(int playerId, Auction auction) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[AUCTIONSUCC]ID[").append(playerId).append("]ITEM[");
		LogUtil.getGameItemString(sb, auction.getItem(), auction.getCount());
		sb.append("]TID[").append(auction.getId()).append("]PRICE[").append(auction.getCurrentPrice()).append("]");
		log.info(sb.toString());
	}
	
	/** 记录拍卖流拍 */
	public static void logAuctionFail(Auction auction) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[AUCTIONFAIL]ID[").append(auction.getPlayerId()).append("]ITEM[");
		LogUtil.getGameItemString(sb, auction.getItem(), auction.getCount());
		sb.append("]TID[").append(auction.getId()).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家尝试兑换激活码 */
	public static void logActivationCodeTry(Player p, String code) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[ACTIVATIONCODETRY]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("CODE[").append(code).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家兑换激活码（成功失败都记录）*/
	public static void logActivationCodeOK(Player p, String code, int retCode) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[ACTIVATIONCODE]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("CODE[").append(code).append("]RETCODE[").append(retCode).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家尝试兑换礼包 */
	public static void logGiftGetTry(Player p, int giftGroupId, int giftId) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[GIFTGETTRY]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("GROUP[").append(giftGroupId).append("]GIFT[").append(giftId).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家兑换礼包成功 */
	public static void logGiftGetOK(Player p, int giftGroupId, int giftId) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[GIFTGET]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("GROUP[").append(giftGroupId).append("]GIFT[").append(giftId).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家尝试提取附件 */
	public static void logGetAttachTry(Player p, int mailId) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[GETATTACHTRY]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("MID[").append(mailId).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家提取附件成功 */
	public static void logGetAttachOK(Player p, int mailId, int srcId, int attMoney, GameItem attItem, int attCount, int price, int rmid) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[GETATTACH]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("MID[").append(mailId).append("]SRC[").append(srcId).append("]ATTACH[");
		LogUtil.getMoneyString(sb, attMoney);
		LogUtil.getGameItemString(sb, attItem, attCount);
		sb.append("]PRICE[").append(price).append("]RID[").append(rmid).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家删除带附件邮件 */
	public static void logDelAttach(Player p, Mail m, int rid) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[DELATTACH]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("MID[").append(m.getId()).append("]SRC[").append(m.getSourceId());
		sb.append("]ATTACH[");
		LogUtil.getAttachmentString(sb, m.getAttachment());
		sb.append("]PRICE[").append(m.getPrice()).append("]RID[").append(rid).append("]");
		log.info(sb.toString());
	}
	
	/** 记录系统自动删除带附件邮件 */
	public static void logAutoDelAttach(Mail m, int rid) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[AUTODELATTACH]ID[").append(m.getDestId()).append("]");
		sb.append("MID[").append(m.getId()).append("]SRC[").append(m.getSourceId());
		sb.append("]ATTACH[");
		LogUtil.getAttachmentString(sb, m.getAttachment());
		sb.append("]PRICE[").append(m.getPrice()).append("]RID[").append(rid).append("]");
		log.info(sb.toString());
	}
	
	/** 记录系统自动删除过期的带附件邮件 */
	public static void logAutoDelExpiAttach(Mail m, int rid) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[AUTODELEXPIATTACH]ID[").append(m.getDestId()).append("]");
		sb.append("MID[").append(m.getId()).append("]SRC[").append(m.getSourceId());
		sb.append("]ATTACH[");
		LogUtil.getAttachmentString(sb, m.getAttachment());
		sb.append("]PRICE[").append(m.getPrice()).append("]RID[").append(rid).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家尝试发送附件 */
	public static void logMailPostTry(Player p, String dest, byte[] attachment, int price) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[MAILPOSTTRY]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("DEST[");
		sb.append(LogUtil.filter(dest));
		sb.append("]ATTACH[");
		LogUtil.getBinaryString(sb, attachment);
		sb.append("]PRICE[").append(price).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家发送附件成功 */
	public static void logMailPostOK(Player p, Mail m) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[MAILPOST]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("DESTID[").append(m.getDestId()).append("]ATTACH[");
		LogUtil.getAttachmentString(sb, m.getAttachment());
		sb.append("]PRICE[").append(m.price).append("]MID[").append(m.getId()).append("]");
		log.info(sb.toString());
	}
	
	/** 记录角色尝试登录 */
	public static void logLoginTry(Account acc, int roleID, String MIEI) {
		if (acc != null) {
			StringBuilder sb = new StringBuilder(100);
			sb.append("[LOGINTRY]ID[").append(roleID).append("]ACC[").append(acc.getId()).append("]");
			if(MIEI!=null && !MIEI.equals("")){
				sb.append("MIEI["+MIEI+"]");
			}
			if(acc.getCity()!=null && !acc.getCity().equals("")){
				sb.append("CITY["+acc.getCity()+"]");
			}
			log.info(sb.toString());
		}
	}
	
	/** 记录角色登录成功 */
	public static void logLoginOK(Player p) {
		try {
			StringBuilder sb = new StringBuilder(100);
			sb.append("[LOGIN]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("NAME[").append(filter(p.name)).append("]INFO[");
			LogUtil.getPlayerSnapshot(sb, p);
			LogUtil.getExtraPlayerSnapshot(sb, p);
			sb.append("]");
			sb.append("CONTRIBUTEDAY["+p.contributeDay+"]");
			sb.append("CONTRIBUTE["+p.contribute+"]");
			sb.append("SALARYDAY["+p.pool.getInt(SalaryService.PPOPERTY_SALARY_DAY, 0)+"]");
			sb.append("SALARY["+p.pool.getInt(SalaryService.PROPERTY_SALARY, 0)+"]");
			sb.append("SESSIONID["+getSessionIdBySession(p.session)+"]");
			sb.append("SESSIONID["+getSessionIdBySession(p.session)+"]");
			p.lastSessionId = getSessionIdBySession(p.session);
			log.info(sb.toString());
		} catch (NumberFormatException e) {
			
		}
	}
	
	/** 记录玩家断线 */
	public static void logDisconnected(Player p) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[DISCONNECTED]");
		LogUtil.getPlayerLogString(sb, p);
		log.info(sb.toString());
	}
	
	/** 记录玩家被屏蔽 */
	public static void logMute(Player p, long time) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[MUTE]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("TIME[").append(time / 1000).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家账号被屏蔽 */
	public static void logAccountMute(Player p, long time) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[MUTEACCOUNT]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("TIME[").append(time / 1000).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家下线（主动或自动） */
	public static void logLogouted(Player p, long ontime, String sessionId) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[LOGOUT]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("NAME[").append(filter(p.name)).append("]INFO[");
		LogUtil.getPlayerSnapshot(sb, p);
		LogUtil.getExtraPlayerSnapshot(sb,p);
		sb.append("CONTRIBUTEDAY["+p.contributeDay+"]");
		sb.append("CONTRIBUTE["+p.contribute+"]");
		sb.append("SALARYDAY["+p.pool.getInt(SalaryService.PPOPERTY_SALARY_DAY, 0)+"]");
		sb.append("SALARY["+p.pool.getInt(SalaryService.PROPERTY_SALARY, 0)+"]");
		sb.append("]ONTIME[").append(ontime / 1000).append("]");
		sb.append("SESSIONID["+sessionId+"]");
		log.info(sb.toString());
	}
	
	/** 记录玩家尝试把物品放入仓库 */
	public static void logDepotPutTry(Player p, GameItem item, int count) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[DEPOTPUTTRY]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ITEM[");
		LogUtil.getGameItemString(sb, item, count);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家把物品存入仓库成功 */
	public static void logDepotPutOK(Player p, GameItem item, int count) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[DEPOTPUT]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ITEM[");
		LogUtil.getGameItemString(sb, item, count);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家尝试把物品放入仓库 */
	public static void logDepotGetTry(Player p, GameItem item, int count) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[DEPOTGETTRY]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ITEM[");
		LogUtil.getGameItemString(sb, item, count);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家把物品存入仓库成功 */
	public static void logDepotGetOK(Player p, GameItem item, int count) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[DEPOTGET]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ITEM[");
		LogUtil.getGameItemString(sb, item, count);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家开启珍宝阁 */
	public static void logAccountDepotOpen(Player p) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[ACCOUNTDEPOTOPEN]");
		LogUtil.getPlayerLogString(sb, p);
		log.info(sb.toString());
	}
	
	/** 记录玩家尝试把物品放入珍宝阁 */
	public static void logAccountDepotPutTry(Player p, GameItem item, int count) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[ACCOUNTDEPOTPUTTRY]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ITEM[");
		LogUtil.getGameItemString(sb, item, count);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家把物品放入珍宝阁成功 */
	public static void logAccountDepotPutOK(Player p, GameItem item, int count) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[ACCOUNTDEPOTPUT]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ITEM[");
		LogUtil.getGameItemString(sb, item, count);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家尝试把物品取出珍宝阁 */
	public static void logAccountDepotGetTry(Player p, GameItem item, int count) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[ACCOUNTDEPOTGETTRY]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ITEM[");
		LogUtil.getGameItemString(sb, item, count);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家把物品取出珍宝阁成功 */
	public static void logAccountDepotGetOK(Player p, GameItem item, int count) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[ACCOUNTDEPOTGET]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ITEM[");
		LogUtil.getGameItemString(sb, item, count);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录创建新的ROLL点 */
	public static void logCreateRoll(int rollID, Gain[] gains, GameItem item, int count) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[ROLLCREATE]ROLLID[").append(rollID).append("]ITEM[");
		LogUtil.getGameItemString(sb, item, count);
		sb.append("]PLAYERS[");
		for (int i = 0; i < gains.length; i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(gains[i].getPlayer().id);
		}
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录创建新的ROLL点 */
	public static void logCreateRoll(int rollID, List<Player> gains, GameItem item, int count) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[ROLLCREATE]ROLLID[").append(rollID).append("]ITEM[");
		LogUtil.getGameItemString(sb, item, count);
		sb.append("]PLAYERS[");
		for (int i = 0; i < gains.size(); i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(gains.get(i).id);
		}
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家代理饲养坐骑 */
	public static void logAgentHorse(Player p, Horse horse) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[AGENTHORSE]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("HORSE[");
		LogUtil.getHorseString(sb, horse);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家取消代理饲养坐骑 */
	public static void logRemoveAgentHorse(Player p, Horse horse) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[REMOVEAGENTHORSE]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("HORSE[");
		LogUtil.getHorseString(sb, horse);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家获得在线经验 */
	public static void logExchangeOnlineExp(Player p, int exp) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[ONLINEEXP]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("LEVEL[").append(p.level).append("]GAINEXP[").append(exp).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家兑换离线经验 */
	public static void logExchangeOfflineExp(Player p, int exp, int remain, int count) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[EXCHANGEEXP]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("LEVEL[").append(p.level).append("]GAINEXP[").append(exp).append("]REMAIN[");
		sb.append(remain).append("]COUNT[").append(count).append("]");
		log.info(sb.toString());
	}
	
	/** 记录坐骑获得托管经验 */
	public static void logAgentHorseExp(Player p, Horse horse, int exp, int remain, int count) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[AGENTHORSEEXP]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("HORSEID[").append(horse.instanceId).append("]LEVEL[").append(horse.level);
		sb.append("]GAINEXP[").append(exp).append("]REMAIN[").append(remain).append("]COUNT[").append(count).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家学会技能书技能 */
	public static void logGetBookSkill(Player p, Skill skill) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[BOOKSKILLGET]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("SKILL[").append(skill.getId()).append("]LEVEL[").append(skill.getLevel()).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家获得坐骑 */
	public static void logGetHorse(Player p, Horse horse) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[GETHORSE]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("HORSE[");
		LogUtil.getHorseString(sb, horse);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录坐骑幻化 */
	public static void logHorseImageChange(Player p, Horse destHorse, Horse resHorse) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[HORSEIMAGECHANGE]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("DESTHORSE[");
		LogUtil.getHorseString(sb, destHorse);
		sb.append("] RESHORSE[");
		LogUtil.getHorseString(sb, resHorse);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录坐骑解除幻化 */
	public static void logRemoveHorseImageChange(Player p, Horse horse) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[REMOVEHORSEIMAGECHANGE]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("HORSE[");
		LogUtil.getHorseString(sb, horse);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录坐骑合成 */
	public static void logHorseFix(Player p, Horse destHorse, Horse resHorse, boolean success) {
		StringBuilder sb = new StringBuilder(100);
		if(success){
			sb.append("[HORSEFIXSUCCESS]");
		}else{
			sb.append("[HORSEFIXFAILURE]");
		}
		LogUtil.getPlayerLogString(sb, p);
		sb.append("DESTHORSE[");
		LogUtil.getHorseString(sb, destHorse);
		sb.append("] RESHORSE[");
		LogUtil.getHorseString(sb, resHorse);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录坐骑合成失败后的确认 */
	public static void logHorseFixFail(Player p, int horseId, int type) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[HORSEFIXFAILCONFIRM]");
		if(type == 0){	//
			sb.append("[SAVESUCCESS_IMONEY]");
		}else if(type == 1){
			sb.append("[REMOVEHORSE_IMONEY]");
		}else if(type == 2){
			sb.append("[REMOVEHORSE_NOIMONEY]");
		}else if(type == 3){
			sb.append("[REMOVEHORSE_NOSAVE]");
		}
		LogUtil.getPlayerLogString(sb, p);
		sb.append("HORSEID["+horseId+"]");
		log.info(sb.toString());
	}
	
	/** 记录玩家尝试在装备上打孔 */
	public static void logAddHoleTry(Player p, GameItem item) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[ADDHOLETRY]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ITEM[");
		LogUtil.getGameItemString(sb, item, 1);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家在装备上打孔操作成功 */
	public static void logAddHoleOK(Player p, GameItem item, boolean succ) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[ADDHOLE]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ITEM[");
		LogUtil.getGameItemString(sb, item, 1);
		sb.append("]RESULT[").append(succ ? "OK" : "FAIL").append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家尝试在装备上扩展孔 */
	public static void logAddMaxHoleTry(Player p, GameItem item) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[ADDMAXHOLETRY]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ITEM[");
		LogUtil.getGameItemString(sb, item, 1);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家在装备上扩展孔操作成功 */
	public static void logAddMaxHoleOK(Player p, GameItem item) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[ADDMAXHOLE]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ITEM[");
		LogUtil.getGameItemString(sb, item, 1);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家尝试在装备上镶嵌宝石 */
	public static void logAddJewelTry(Player p, GameItem item, int jewel, int hole, int method) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[ADDJEWELTRY]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("JEWEL[").append(jewel).append("]HOLE[").append(hole).append("]METHOD[").append(method).append("]ITEM[");
		LogUtil.getGameItemString(sb, item, 1);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家尝试在装备上升级宝石 */
	public static void logUpgradeJewelTry(Player p, GameItem item, int jewel, int hole, int method) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[UPGRADEJEWEL]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("JEWEL[").append(jewel).append("]HOLE[").append(hole).append("]METHOD[").append(method).append("]ITEM[");
		LogUtil.getGameItemString(sb, item, 1);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家在装备上升级宝石成功 */
	public static void logUpgradeJewelOK(Player p, GameItem item, int jewel, int hole, int method, boolean succ) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[UPGRADEJEWEL]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("JEWEL[").append(jewel).append("]HOLE[").append(hole).append("]METHOD[").append(method).append("]ITEM[");
		LogUtil.getGameItemString(sb, item, 1);
		sb.append("]RESULT[").append(succ ? "OK" : "FAIL").append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家在装备上打孔操作成功 */
	public static void logAddJewelOK(Player p, GameItem item, int jewel, int hole, int method, boolean succ) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[ADDJEWEL]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("JEWEL[").append(jewel).append("]HOLE[").append(hole).append("]METHOD[").append(method).append("]ITEM[");
		LogUtil.getGameItemString(sb, item, 1);
		sb.append("]RESULT[").append(succ ? "OK" : "FAIL").append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家尝试合成高级宝石 */
	public static void logMergeJewelTry(Player p, int jewel, int count, int autoFixCnt) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[MERGEJEWELTRY]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("JEWEL[").append(jewel).append("]COUNT[").append(count);
		sb.append("]autoFixCnt[").append(autoFixCnt).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家合成高级宝石操作成功 */
	public static void logMergeJewelOK(Player p, int jewel, int count, int autoFixCnt, boolean succ) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[MERGEJEWEL]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("JEWEL[").append(jewel).append("]COUNT[").append(count);
		sb.append("]autoFixCnt[").append(autoFixCnt);
		sb.append("]RESULT[").append(succ ? "OK" : "FAIL").append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家尝试自动合成高级宝石 */
	public static void logAutoMergeJewelTry(Player p, int jewel, int count) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[AUTOMERGEJEWELTRY]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("JEWEL[").append(jewel).append("]COUNT[").append(count).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家自动合成高级宝石操作成功 */
	public static void logAutoMergeJewelOK(Player p, int jewel, int count, boolean succ) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[AUTOMERGEJEWEL]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("JEWEL[").append(jewel).append("]COUNT[").append(count);
		sb.append("]RESULT[").append(succ ? "OK" : "FAIL").append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家尝试从装备上摘除宝石 */
	public static void logRemoveJewelTry(Player p, GameItem item, int hole) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[REMOVEJEWELTRY]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ITEM[");
		LogUtil.getGameItemString(sb, item, 1);
		sb.append("]HOLE[").append(hole).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家摘除宝石操作成功 */
	public static void logRemoveJewelOK(Player p, GameItem item, int hole, boolean succ) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[REMOVEJEWEL]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ITEM[");
		LogUtil.getGameItemString(sb, item, 1);
		sb.append("]HOLE[").append(hole).append("]RESULT[").append(succ ? "OK" : "FAIL").append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家尝试从装备上自动摘除宝石 */
	public static void logAutoRemoveJewelTry(Player p, GameItem item) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[AUTOREMOVEJEWELTRY]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ITEM[");
		LogUtil.getGameItemString(sb, item, 1);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家自动摘除宝石操作成功 */
	public static void logAutoRemoveJewelOK(Player p, GameItem item, boolean succ) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[AUTOREMOVEJEWEL]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ITEM[");
		LogUtil.getGameItemString(sb, item, 1);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录怪物死亡 */
	public static void logUnitDie(Creature c, Unit source) {
		StringBuilder sb = new StringBuilder(30);
		if (source == null) {
			sb.append("[UNITDIE]ID[-1]CREATURE[").append(c.id).append("]");
		} else {
			sb.append("[UNITDIE]ID[").append(source.id).append("]CREATURE[").append(c.id).append("]");
		}
		log.info(sb.toString());
	}
	
	/** 记录怪物死亡后分配 */
	public static void logUnitDieDist(Creature c, List<Player> owners) {
		StringBuilder sb = new StringBuilder(100);
		if (owners == null) {
			sb.append("[DISTRIBUTE]ID[").append(c.id).append("]OWNERS[]");
		} else {
			sb.append("[DISTRIBUTE]ID[").append(c.id).append("]OWNERS[");
			for (int i = 0; i < owners.size(); i++) {
				if (i > 0) {
					sb.append(",");
				}
				sb.append(owners.get(i).id);
			}
			sb.append("]");
		}
		log.info(sb.toString());
	}
	
	/** 记录获得坐骑经验 */
	public static void logGetHorseExp(Player p, Horse h, int v1, int v2, String cause) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[GETHORSEEXP]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("HORSEID[").append(h.instanceId).append("]V1[").append(v1).append("]V2[").append(v2);
		sb.append("]CAUSE[").append(cause).append("]");
		log.info(sb.toString());
	}
	
	/** 记录坐骑升级 */
	public static void logHorseLevelUp(Player p, Horse h, int lvl1, int exp1, int lvl2, int exp2) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[HORSELEVELUP]");
		LogUtil.getPlayerLogString(sb, p);
		LogUtil.getPlayerPosition(sb, p);
		sb.append("HORSEID[").append(h.instanceId).append("]LVL1[").append(lvl1);
		sb.append("]EXP1[").append(exp1).append("]LVL2[").append(lvl2).append("]EXP2[").append(exp2).append("]");
		log.info(sb.toString());
	}
	
	/** 记录丢弃坐骑 */
	public static void logThrowHorse(Player p, Horse h, String cause) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[REMOVEHORSE]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("HORSE[");
		LogUtil.getHorseString(sb, h);
		sb.append("]CAUSE[").append(cause).append("]EQUS[");
		if (h.equs != null && h.equs.equs.length > 0) {
			for (GameItem item : h.equs.equs) {
				if (item != null) {
					LogUtil.getGameItemString(sb, item, 1);
				}
			}
		}
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家数据被保存 */
	public static void logSavePlayer(Player p) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[SAVEPLAYER]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("INFO[");
		LogUtil.getPlayerSnapshot(sb, p);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家获得战功 */
	public static void logGetCredit(Player p, int v1, int v2, String cause) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[GETCREDIT]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("V1[").append(v1).append("]V2[").append(v2).append("]CAUSE[").append(cause).append("]");
		log.info(sb.toString());
	}

	/** 记录玩家失去战功 */
	public static void logRemoveCredit(Player p, int v1, int v2, String cause) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[REMOVECREDIT]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("V1[").append(v1).append("]V2[").append(v2).append("]CAUSE[").append(cause).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家获得物品 */
	public static void logGetItem(Player p, GameItem item, int count, String cause) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[GETITEM]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("INFO[");
		LogUtil.getGameItemString(sb, item, count);
		sb.append("]CAUSE[").append(cause).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家获得物品 */
	public static void logRemoveItem(Player p, GameItem item, int count, String cause) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[REMOVEITEM]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("INFO[");
		LogUtil.getGameItemString(sb, item, count);
		sb.append("]CAUSE[").append(cause).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家尝试打造 */
	public static void logProduceTry(Player p, int fid) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[PRODUCETRY]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("FID[").append(fid).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家打造成功 */
	public static void logProduce(GameObjectRef p, int fid, GameItem item, IBuyObject buyObject) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[PRODUCE]");
		Player pp = (Player)ObjectAccessor.getGameObject(p);
		if (pp != null) {
			LogUtil.getPlayerLogString(sb, pp);
		} else {
			sb.append("ID[").append(p.id).append("]");
		}
		sb.append("FID[").append(fid).append("]ITEM[");
		LogUtil.getGameItemString(sb, item, 1);
		sb.append("]PRICE[");
		for (BuyRequirement req : buyObject.getRequirements()) {
			LogUtil.getBuyRequirementString(sb, req, buyObject.getCount(), buyObject.getDiscount());
		}
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家打造成功 */
	public static void logProduce(GameObjectRef p, int fid, GainItem[] items, IBuyObject buyObject) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[PRODUCE]");
		Player pp = (Player)ObjectAccessor.getGameObject(p);
		if (pp != null) {
			LogUtil.getPlayerLogString(sb, pp);
		} else {
			sb.append("ID[").append(p.id).append("]");
		}
		sb.append("FID[").append(fid).append("]ITEM[");
		for (GainItem item : items) {
			LogUtil.getGameItemString(sb, item.getItem(), item.getCount());
		}
		sb.append("]PRICE[");
		for (BuyRequirement req : buyObject.getRequirements()) {
			LogUtil.getBuyRequirementString(sb, req, buyObject.getCount(), buyObject.getDiscount());
		}
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家商店购买物品成功 */
	public static void logShopBuy(GameObjectRef p, int shopID, GameItem item, int count, IBuyObject buyObject) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[SHOPBUY]");
		Player pp = (Player)ObjectAccessor.getGameObject(p);
		if (pp != null) {
			LogUtil.getPlayerLogString(sb, pp);
		} else {
			sb.append("ID[").append(p.id).append("]");
		}
		sb.append("SHOP[").append(shopID).append("]ITEM[");
		LogUtil.getGameItemString(sb, item, count);
		sb.append("]PRICE[");
		for (BuyRequirement req : buyObject.getRequirements()) {
			LogUtil.getBuyRequirementString(sb, req, buyObject.getCount(), buyObject.getDiscount());
		}
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录尝试元宝支付购买 */
	public static void logIMoneyBuyTry(int pid, int accid, IBuyObject buyObject) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[IMONEYBUYTRY]ID[").append(pid).append("]ACC[").append(accid).append("]");
		sb.append(buyObject.toString());
		log.info(sb.toString());
	}

	/** 记录元宝支付购买结果 */
	public static void logIMoneyBuyOK(int pid, int accid, IBuyObject buyObject, boolean succ, long balance, int cost, int level) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[IMONEYBUY]ID[").append(pid).append("]ACC[").append(accid).append("]");
		sb.append(buyObject.toString());
		sb.append("RESULT[").append(succ ? "OK" : "FAIL").append("]");
		sb.append("BALANCE[").append(balance).append("]COST[").append(cost).append("]");
		sb.append("LEVEL["+level+"]");
		log.info(sb.toString());
	}
	
	/** 记录玩家尝试购买物品 */
	public static void logShopBuyTry(Player p, int shopID, int itemID, int count) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[SHOPBUYTRY]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("SHOP[").append(shopID).append("]ITEM[").append(itemID).append("]COUNT[").append(count).append("]");
		if(p.getAccount()!=null && p.getAccount().getCmccUserId()!=null && !p.getAccount().getCmccUserId().equals("")){
			sb.append("CMCCUSERID["+p.getAccount().getCmccUserId()+"]");
		}
		log.info(sb.toString());
	}
	
	/** 记录CMCC玩家获得话费奖励 */
	public static void logSendMoney(Player p,int getMoney,int totalMoney) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[SENDMONEY]");
		LogUtil.getPlayerLogString(sb, p);
		if(p.getAccount()!=null && p.getAccount().getCmccUserId()!=null && !p.getAccount().getCmccUserId().equals("")){
			sb.append("CMCCUSERID["+p.getAccount().getCmccUserId()+"]");
		}
		sb.append("LEVEL["+p.level+"]");
		sb.append("GETMONEY["+getMoney+"]"+"TOTALMONEY["+totalMoney+"]---CMCC FUJIAN");
		log.info(sb.toString());
	}
	
	/** 记录玩家尝试出售物品 */
	public static void logShopSellTry(Player p, int itemID, int itemInstanceID, int count) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[SHOPSELLTRY]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ITEM[").append(itemID).append("]IID[").append(itemInstanceID).append("]COUNT[").append(count).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家出售物品成功 */
	public static void logShopSellOK(Player p, GameItem item, int count, int price, int tax) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[SHOPSELL]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ITEM[");
		LogUtil.getGameItemString(sb, item, count);
		sb.append("]PRICE[").append(price).append("]TAX[").append(tax).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家获得金钱 */
	public static void logGetMoney(Player p, int v1, int v2, String cause) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[GETMONEY]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("V1[").append(v1).append("]V2[").append(v2).append("]CAUSE[").append(cause).append("]");
		log.info(sb.toString());
	}

	/** 记录玩家扣除金钱 */
	public static void logRemoveMoney(Player p, int v1, int v2, String cause) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[REMOVEMONEY]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("V1[").append(v1).append("]V2[").append(v2).append("]CAUSE[").append(cause).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家获得声望 */
	public static void logGetHonor(Player p, int v1, int v2, String cause) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[GETHONOR]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("V1[").append(v1).append("]V2[").append(v2).append("]CAUSE[").append(cause).append("]");
		log.info(sb.toString());
	}

	/** 记录玩家扣除声望 */
	public static void logRemoveHonor(Player p, int v1, int v2, String cause) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[REMOVEHONOR]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("V1[").append(v1).append("]V2[").append(v2).append("]CAUSE[").append(cause).append("]");
		log.info(sb.toString());
	}
	
	/** 记录自动资质鉴定过程 */
	public static void logAutoNaturalEnhance(Player p, GameItem item, String result) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[AUTONATURALENHANCE]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ITEM[");
		LogUtil.getGameItemString(sb, item, 1);
		sb.append("]RESULT[").append(result).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家获得经验 */
	public static void logGetExp(Player p, int v1, long v2, String cause) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[GETEXP]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("V1[").append(v1).append("]V2[").append(v2).append("]CAUSE[").append(cause).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家增加工资 */
	public static void logAddSalary(Player p, int v1, int v2, String cause) {
		try{
			StringBuilder sb = new StringBuilder(100);
			sb.append("[ADDSALARY]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("V1[").append(v1).append("]V2[").append(v2).append("]CAUSE[").append(cause).append("]");
			log.info(sb.toString());
		}catch(Exception e){
			
		}
	}
	
	/** 记录玩家扣除工资 */
	public static void logRemoveSalary(Player p, int v1, int v2, String cause) {
		try{
			StringBuilder sb = new StringBuilder(100);
			sb.append("[REMOVESALARY]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("V1[").append(v1).append("]V2[").append(v2).append("]CAUSE[").append(cause).append("]");
			log.info(sb.toString());
		}catch(Exception e){
			
		}
	}
	
	
	/** 记录玩家升级 */
	public static void logLevelUp(Player p, int lvl1, long exp1, int lvl2, long exp2) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[LEVELUP]");
		LogUtil.getPlayerLogString(sb, p);
		LogUtil.getPlayerPosition(sb, p);
		sb.append("LVL1[").append(lvl1).append("]EXP1[").append(exp1);
		sb.append("]LVL2[").append(lvl2).append("]EXP2[").append(exp2);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家扩展坐骑栏位 */
	public static void logExtendHorseBag(Player p) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[EXTENDHORSEBAG]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("SIZE[").append(p.horseBag.maxSize).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家装备刻字成功 */
	public static void logEquMarkOK(Player p, GameItem item) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[EQUMARK]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ITEM[");
		LogUtil.getGameItemString(sb, item, 1);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家尝试进行装备星级鉴定 */
	public static void logStarTry(Player p, GameItem item, int method) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[STARTRY]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ITEM[");
		LogUtil.getGameItemString(sb, item, 1);
		sb.append("]METHOD[").append(method).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家装备星级鉴定成功 */
	public static void logStarOK(Player p, GameItem item, int method) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[STAR]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ITEM[");
		LogUtil.getGameItemString(sb, item, 1);
		sb.append("]METHOD[").append(method).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家尝试进行装备资质鉴定 */
	public static void logNaturalEnhanceTry(Player p, GameItem item) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[NATURALENHANCETRY]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ITEM[");
		LogUtil.getGameItemString(sb, item, 1);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家装备资质鉴定成功 */
	public static void logNaturalEnhanceOK(Player p, GameItem item) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[NATURALENHANCE]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ITEM[");
		LogUtil.getGameItemString(sb, item, 1);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家选择需求装备 */
	public static void logRollAdd(Player p, int rollID) {
		StringBuilder sb = new StringBuilder(50);
		sb.append("[ROLLADD]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ROLL[").append(rollID).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家选择放弃装备 */
	public static void logRollCancel(Player p, int rollID) {
		StringBuilder sb = new StringBuilder(50);
		sb.append("[ROLLCANCEL]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ROLL[").append(rollID).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家发送聊天信息 */
	public static void logChat(Player p, int channel, int destId, String logHead, String message) {
		StringBuilder sb = new StringBuilder(100);
		sb.append(logHead);
		LogUtil.getPlayerLogString(sb, p);
		LogUtil.getPlayerMap(sb, p);
		sb.append("CHANNEL[").append(channel).append("]DEST[").append(destId).append("]MESSAGE[");
		sb.append(filter(message));
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家尝试兑换头衔 */
	public static void logTitleBuyTry(Player p, int titleID) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[TITLEBUYTRY]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("TITLE[").append(titleID).append("]");
		log.info(sb.toString());
	}

	/** 记录玩家兑换头衔成功 */
	public static void logTitleBuyOK(Player p, int titleID) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[TITLEBUY]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("TITLE[").append(titleID).append("]");
		log.info(sb.toString());
	}

	/** 记录玩家锁定坐骑技能成功 */
	public static void logHorseLockSkillOK(Player p, Horse h) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[HORSESKILLLOCK]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("HORSE[");
		LogUtil.getHorseString(sb, h);
		sb.append("]LOCKS[").append(h.lockSkillId).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家解锁坐骑技能成功 */
	public static void logHorseUnlockSkillOK(Player p, Horse h) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[HORSESKILLUNLOCK]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("HORSE[");
		LogUtil.getHorseString(sb, h);
		sb.append("]LOCKS[").append(h.lockSkillId).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家交易成功 */
	public static void logExchangeOK(int tid, Player p1, Gain g1, Player p2, Gain g2) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[EXCHANGE]");
		LogUtil.getPlayerLogString(sb, p1);
		sb.append("TID[").append(tid).append("]ITEMS[");
		LogUtil.getGainString(sb, g1);
		sb.append("]ID2[").append(p2.id).append("]ACC2[").append(p2.accountId).append("]ITEMS2[");
		LogUtil.getGainString(sb, g2);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家取消交易物品 */
	public static void logExchangeRemoveItem(int tid, Player p, int money, GameItem item, int count) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[EXCHANGEREMOVE]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("TID[").append(tid).append("]ITEM[");
		if (money > 0) {
			LogUtil.getMoneyString(sb, money);
		}
		if (item != null) {
			LogUtil.getGameItemString(sb, item, count);
		}
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家添加交易物品 */
	public static void logExchangeAddItem(int tid, Player p, int money, GameItem item, int count) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[EXCHANGEADD]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("TID[").append(tid).append("]ITEM[");
		if (money > 0) {
			LogUtil.getMoneyString(sb, money);
		}
		if (item != null) {
			LogUtil.getGameItemString(sb, item, count);
		}
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家取消交易 */
	public static void logExchangeCancel(int tid, Player p) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[EXCHANGECANCEL]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("TID[").append(tid).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家拒绝交易 */
	public static void logExchangeRefuse(int tid, Player p) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[EXCHANGEREFUSE]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("TID[").append(tid).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家接受交易 */
	public static void logExchangeAccept(int tid, Player p) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[EXCHANGEACCEPT]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("TID[").append(tid).append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家建立交易 */
	public static void logExchangeCreate(int tid, Player p1, Player p2) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[EXCHANGECREATE]");
		LogUtil.getPlayerLogString(sb, p1);
		sb.append("TID[").append(tid).append("]ID2[").append(p2.id).append("]ACC2[").append(p2.accountId).append("]");
		log.info(sb.toString());
	}

	/** 记录玩家打包坐骑 */
	public static void logHorsePack(Player p, Horse h) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[HORSEPACK]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("HORSE[");
		LogUtil.getHorseString(sb, h);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录玩家使用物品 */
	public static void logUseItem(Player p, GameItem item, boolean consume) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[USEITEM]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("INFO[");
		LogUtil.getGameItemString(sb, item, 1);
		sb.append("]CNM[").append(consume ? 1 : 0).append("]");
		if(Server.REVISION_TYPE_CMCC.equals(Server.server.revision) && p.getAccount()!=null && p.getAccount().getCmccUserId()!=null && !p.getAccount().getCmccUserId().equals("") && item.template.id == 2261){
			sb.append("CMCCUSERID["+p.getAccount().getCmccUserId()+"]");
			sb.append("LEVLE["+p.level+"] ---CMCC");
		}
		log.info(sb.toString());
	}
	
	/** 记录获得元宝 */
	public static void logGetIMoney(int accID, int v1, int v2, String cause) {
		StringBuilder sb = new StringBuilder(50);
		sb.append("[GETIMONEY]ACC[").append(accID).append("]V1[").append(v1).append("]V2[").append(v2);
		sb.append("]CAUSE[").append(cause).append("]");
		log.info(sb.toString());
	}
	
	/** 记录扣除元宝 */
	public static void logRemoveIMoney(int accID, int v1, int v2, String cause) {
		StringBuilder sb = new StringBuilder(50);
		sb.append("[REMOVEIMONEY]ACC[").append(accID).append("]V1[").append(v1).append("]V2[").append(v2);
		sb.append("]CAUSE[").append(cause).append("]");
		log.info(sb.toString());
	}
	
	/** 记录洗坐骑技能 */
	public static void logHorseChangeSkill(Player p, String before, Horse after) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[HORSECHANGESKILL]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("]V1[").append(before).append("]V2[");
		LogUtil.getHorseString(sb, after);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录接受任务 */
	public static void logGetQuest(Player p, int questID, boolean succ) {
		StringBuilder sb = new StringBuilder(50);
		sb.append("[GETQUEST]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("QUEST[").append(questID).append("]RESULT[");
		sb.append(succ ? "OK" : "FAIL").append("]");
		log.info(sb.toString());
	}
	
	/** 记录放弃任务 */
	public static void logAbandonQuest(Player p, int questID) {
		StringBuilder sb = new StringBuilder(50);
		sb.append("[ABANDONQUEST]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("QUEST[").append(questID).append("]");
		log.info(sb.toString());
	}

	/** 记录完成任务 */
	public static void logFinishQuest(Player p, int questID, int branch, int result) {
		StringBuilder sb = new StringBuilder(50);
		sb.append("[FINISHQUEST]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("QUEST[").append(questID).append("]BRANCH[").append(branch).append("]RESULT[").append(result).append("]");
		log.info(sb.toString());
	}
	
	/** 记录完成夫妻任务 */
	public static void logFinishMarriageQuest(Player p, int questID, int branch, int result) {
		//如果成功完成夫妻任务，打印配偶ID
		try{
			if(result == 0 && (questID == 1448 || questID == 1447 || questID == 1446)){
				StringBuilder sb = new StringBuilder(50);
				sb.append("[FINISHMARRIGEQUEST]");
				LogUtil.getPlayerLogString(sb, p);
				int mateId = Server.server.getServiceRegistry().getRelationService().get(p.id).mateId;
				sb.append("MATEID[").append(mateId).append("]");
				sb.append("QUEST[").append(questID).append("]BRANCH[").append(branch).append("]RESULT[").append(result).append("]");
				log.info(sb.toString());
			}
		} catch (Exception e){
			
		}
		
	}

	/** 记录玩家进入副本 */
	public static void logEnterInstance(Player p, NormalInstance instance, boolean create) {
		int teamCount = 1;
		if (p.party != null) {
			teamCount = p.party.getCount();
		}
		boolean isLeader = false;
		if (p.party != null) {
			isLeader = p.id == p.party.leader.player.id;
		} else {
			isLeader = true;
		}
		
		StringBuilder sb = new StringBuilder(100);
		sb.append("[ENTERINSTANCE]");
		LogUtil.getPlayerLogString(sb, p);
		LogUtil.logHorseBaseInfo(sb, p.horse);
		LogUtil.getPlayerMap(sb, p);
		sb.append("IID[").append(instance.definition.id).append("]INSTANCE[").append(instance.id);
		sb.append("]INAME[").append(instance.definition.name).append("]TEAM[");
		sb.append(teamCount).append("]LEADER[").append(isLeader ? 1 : 0).append("]");
		sb.append("PLAYERS[");
		if(p.party==null){
			sb.append(p.id);
		} else {
			for (PartyMember pm : p.party.members) {
				sb.append(pm.getId()+" ");
			}
		}
		sb.append("]");
		sb.append("CREATE[").append(create ? 1 : 0).append("]");
		log.info(sb.toString());
	}
	
	public static void logHorseBaseInfo(StringBuilder out,Horse horse){
		try{
			if (horse == null) {
				out.append("HOSID[-1]HOSLEVEL[-1]HOSEXP[-1]");
			} else {
				out.append("HOSID[");
				out.append(horse.itemId);
				out.append("]HOSLEVEL[");
				out.append(horse.level);
				out.append("]HOSEXP[");
				out.append(horse.exp);
				out.append("]");
			}
		}catch(Exception e){
			
		}
	}

	/** 记录玩家离开副本 */
	public static void logLeaveInstance(Player p, NormalInstance instance) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[LEAVEINSTANCE]");
		LogUtil.getPlayerLogString(sb, p);
		LogUtil.logHorseBaseInfo(sb, p.horse);
		sb.append("IID[").append(instance.definition.id).append("]INSTANCE[").append(instance.id);
		sb.append("]INAME[").append(instance.definition.name).append("]");
		log.info(sb.toString());
	}
	
	/** 记录夺旗战场开始 */
	public static void logFlagBattleStart(int instanceID, int fac1, int fac2, Set<GameObjectRef> players) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[FLAGBATTLESTART]INSTANCE[").append(instanceID).append("]FAC1[").append(fac1).append("]PLAYERS1[");
		ActorCacheService acs = Server.server.getServiceRegistry().getActorCacheService();
		int pcount = 0;
		for (GameObjectRef ref : players) {
			Actor actor = acs.find(ref.id);
			if (actor != null && actor.faction == fac1) {
				if (pcount > 0) {
					sb.append(",");
				}
				sb.append(actor.id);
				pcount++;
			}
		}
		sb.append("]FAC2[").append(fac2).append("]PLAYERS2[");
		pcount = 0;
		for (GameObjectRef ref : players) {
			Actor actor = acs.find(ref.id);
			if (actor != null && actor.faction == fac2) {
				if (pcount > 0) {
					sb.append(",");
				}
				sb.append(actor.id);
				pcount++;
			}
		}
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录夺旗战场结束 */
	public static void logFlagBattleEnd(int instanceID, int fac1, int fac2, List<Player> players, int winFac, long time) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[FLAGBATTLENED]INSTANCE[").append(instanceID).append("]FAC1[").append(fac1).append("]PLAYERS1[");
		int pcount = 0;
		for (Player p : players) {
			if (p != null && p.faction == fac1) {
				if (pcount > 0) {
					sb.append(",");
				}
				sb.append(p.id);
				pcount++;
			}
		}
		sb.append("]FAC2[").append(fac2).append("]PLAYERS2[");
		pcount = 0;
		for (Player p : players) {
			if (p != null && p.faction == fac2) {
				if (pcount > 0) {
					sb.append(",");
				}
				sb.append(p.id);
				pcount++;
			}
		}
		sb.append("]WIN[").append(winFac).append("]TIME[").append(time / 1000).append("]");
		log.info(sb.toString());
	}
	
	/** 记录国战开始 */
	public static void logNationBattleStart(int fac1, int fac2, Date time) {
		StringBuilder sb = new StringBuilder(50);
		sb.append("[NATIONBATTLESTART]FAC1[").append(fac1).append("]FAC2[").append(fac2).append("]STARTTIME[");
		sb.append(timeFormat.format(time)).append("]");
		log.info(sb.toString());
	}
	
	/** 记录国战结束 */
	public static void logNationBattleEnd(int fac1, Set<GameObjectRef> players1, int fac2, Set<GameObjectRef> players2, int winFac, long time) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[NATIONBATTLEEND]FAC1[").append(fac1).append("]PLAYERS1[");
		ActorCacheService acs = Server.server.getServiceRegistry().getActorCacheService();
		int pcount = 0;
		for (GameObjectRef ref : players1) {
			Actor actor = acs.find(ref.id);
			if (actor != null) {
				if (pcount > 0) {
					sb.append(",");
				}
				sb.append(actor.id);
				pcount++;
			}
		}
		sb.append("]FAC2[").append(fac2).append("]PLAYERS2[");
		pcount = 0;
		for (GameObjectRef ref : players2) {
			Actor actor = acs.find(ref.id);
			if (actor != null) {
				if (pcount > 0) {
					sb.append(",");
				}
				sb.append(actor.id);
				pcount++;
			}
		}
		sb.append("]WIN[").append(winFac).append("]TIME[").append(time / 1000).append("]");
		log.info(sb.toString());
	}
	
	/** 记录国家偷袭战开始 */
	public static void logNationSneakBattleStart(int fac1, int fac2, Date time) {
		StringBuilder sb = new StringBuilder(50);
		sb.append("[NATIONSNEAKBATTLESTART]FAC1[").append(fac1).append("]FAC2[").append(fac2).append("]STARTTIME[");
		sb.append(timeFormat.format(time)).append("]");
		log.info(sb.toString());
	}
	
	/** 记录国战结束 */
	public static void logNationSneakBattleEnd(int fac1, Set<GameObjectRef> players1, int fac2, Set<GameObjectRef> players2, int winFac, long time) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[NATIONSNEAKBATTLEEND]FAC1[").append(fac1).append("]PLAYERS1[");
		ActorCacheService acs = Server.server.getServiceRegistry().getActorCacheService();
		int pcount = 0;
		for (GameObjectRef ref : players1) {
			Actor actor = acs.find(ref.id);
			if (actor != null) {
				if (pcount > 0) {
					sb.append(",");
				}
				sb.append(actor.id);
				pcount++;
			}
		}
		sb.append("]FAC2[").append(fac2).append("]PLAYERS2[");
		pcount = 0;
		for (GameObjectRef ref : players2) {
			Actor actor = acs.find(ref.id);
			if (actor != null) {
				if (pcount > 0) {
					sb.append(",");
				}
				sb.append(actor.id);
				pcount++;
			}
		}
		sb.append("]WIN[").append(winFac).append("]TIME[").append(time / 1000).append("]");
		log.info(sb.toString());
	}
	
	/** 记录怪物战斗过程 */
	public static void logBossFight(Creature c, long time, boolean win) {
		BossScoreService bss = Server.server.getServiceRegistry().getBossScoreService();
		BossDef bdef = bss.getBossDef(c.id);
		if (bdef != null) {
			StringBuilder sb = new StringBuilder(200);
			sb.setLength(0);
			sb.append("[BOSSFIGHT]");
			Instance inst = c.map.map.instance;
			if (inst == null) {
				sb.append("IID[-1]INSTANCE[-1]INAME[]");
			} else if (inst instanceof NormalInstance) {
				sb.append("IID[").append(((NormalInstance)inst).definition.id).append("]INSTANCE[").append(inst.getId());
				sb.append("]INAME[").append(inst.getName()).append("]");
			} else {
				sb.append("IID[-1]INSTANCE[").append(inst.getId()).append("]INAME[").append(inst.getName()).append("]");
			}
			sb.append("MID[").append(c.id).append("]MIID[").append(c.instanceId).append("]MNAME[").append(c.name).append("]");
			LogUtil.getCreaturePosition(sb, c);
			sb.append("TIME[").append(time / 1000).append("]PLAYER[");
			if (c.battleContribList != null) {
				List<Player> owners = c.battleContribList.checkOwners();
				for (int i = 0; owners != null && i < owners.size(); i++) {
					if (i > 0) {
						sb.append(",");
					}
					sb.append(owners.get(i).id);
				}
			}
			sb.append("]RESULT[").append(win ? "OK" : "FAIL").append("]");
			log.info(sb.toString());
			
			// 统计
			if (win) {
				Server.server.getServiceRegistry().getRealtimeStatService().bossKillCounter++;
			}
		}
	}
	
	/** 记录城站报名 */
	public static void logTongBattleApply(Player p, Tong tong, int money){
		StringBuilder sb = new StringBuilder(200);
		sb.append("[TONGBATTLEAPPLY]"+getPlayerLogString(p)+"TONGID["+tong.id+"]MONEY["+money+"]");
		log.info(sb.toString());
	}
	
	/** 记录城战竞价 */
	public static void logTongBattleBid(Player p, Tong tong, int bid){
		StringBuilder sb = new StringBuilder(200);
		sb.append("[TONGBATTLEBID]"+getPlayerLogString(p)+"TONGID["+tong.id+"]BID["+bid+"]");
		log.info(sb.toString());
	}
	
	/** 记录城战开始 */
	public static void logTongBattleStart(TongBattleSide attack1, TongBattleSide attack2, TongBattleSide defend, int startTime){
		StringBuilder sb = new StringBuilder(200);
		sb.append("[TONGBATTLEBEGIN]ATTACK1["+getTongBattleSideString(attack1)+"]ATTACK2["+getTongBattleSideString(attack2)+"]DEFEND["+getTongBattleSideString(defend)+"]");
		log.info(sb.toString());
	}
	
	protected static String getTongBattleSideString(TongBattleSide side){
		if(side == null || side.tong==null){
			return "null";
		}
		return side.faction+","+side.tong.id;
	}
	
	/** 记录决出的城战报名成功者 */
	public static void logTongBattleApplyDecide(TongBattleApply[] applys, int mapId){
		StringBuilder sb = new StringBuilder(200);
		sb.append("[TONGBATTLEDECIDE]");
		for(TongBattleApply apply : applys){
			sb.append("APPLY["+apply.tongId+"]");
		}
		sb.append("MAPID["+mapId+"]");
		log.info(sb.toString());
	}
	
	/** 记录城战结束 */
	public static void logTongBattleEnd(TongBattleSide win, TongBattleSide fail, int value){
		StringBuilder sb = new StringBuilder(200);
		if(win!=null && fail!=null){
			sb.append("[TONGBATTLEEND]WINTONGID["+win.tong.id+"]GAINVALUE["+value
					+"]FAILTONGID["+fail.tong.id+"]DECVALUE["+value+"]");
			log.info(sb.toString());
		}
	}
	
	/** 记录城站报名军团报名结束未报名成功返还金钱 */
	public static void logTongBattleReturnMoney(TongBattleApply apply, int money){
		Tong tong = apply.getTong();
		if(tong!=null && money>0){
			StringBuilder sb = new StringBuilder(200);
			sb.append("[TONGBATTLERETURNMONEY]TONGID["+apply.tongId+"]MONEY["+money+"]BALANCE["+tong.money+"]");
			log.info(sb.toString());
		}
	}
	
	/** 记录装备自动打孔过程 */
	public static void logAutoAddHole(Player p, int wantHole, int realHole, int useBanner, int decMoney){
		StringBuilder sb = new StringBuilder(getPlayerLogString(p));
		sb.append("WANTHOLE["+wantHole+"]REALADDHOLE["+realHole+"]USEBANNER["+useBanner+"]DECMONEY["+decMoney+"]");
		log.info(sb);
	}
	
	/** 记录玩家扩展背包成功 */
	public static void logExtendBag(GameObjectRef p) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[EXTENDBAG]");
		Player pp = (Player)ObjectAccessor.getGameObject(p);
		if (pp != null) {
			LogUtil.getPlayerLogString(sb, pp);
			sb.append("SIZE[").append(pp.bag.getAddedSize()).append("]");
		} else {
			sb.append("ID[").append(p.id).append("]");
		}
		log.info(sb.toString());
	}
	
	/** 记录玩家扩展仓库成功 */
	public static void logExtendDepot(GameObjectRef p) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[EXTENDDEPOT]");
		Player pp = (Player)ObjectAccessor.getGameObject(p);
		if (pp != null) {
			LogUtil.getPlayerLogString(sb, pp);
			sb.append("SIZE[").append(pp.depot.getAddedSize()).append("]");
		} else {
			sb.append("ID[").append(p.id).append("]");
		}
		log.info(sb.toString());
	}
	
	/** 记录活动发放奖励 */
	public static void logActivityReward(Player p, Activity act, int type) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[ACTIVITYREWARD]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ACTID[").append(act.getId()).append("]ACTNAME[").append(act.getName()).append("]");
		sb.append("REWARD[");
		sb.append(type);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录活动发放奖励 */
	public static void logActivityReward(Actor p, Activity act, int type) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[ACTIVITYREWARD]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ACTID[").append(act.getId()).append("]ACTNAME[").append(act.getName()).append("]");
		sb.append("REWARD[");
		sb.append(type);
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录活动发放奖励 */
	public static void logActivityRewards(Player p, Activity act) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("[ACTIVITYREWARD]");
		LogUtil.getPlayerLogString(sb, p);
		sb.append("ACTID[").append(act.getId()).append("]ACTNAME[").append(act.getName()).append("]");
		log.info(sb.toString());
	}
	
	/** 记录选美报名 */
	public static void logBeautySignUp(Player p){
		StringBuilder sb = new StringBuilder(100);
		sb.append("[BEAUTYSIGNUP]"+getPlayerLogString(p));
		log.info(sb.toString());
	}
	
	/** 记录选美投票 */
	public static void logBeautyVote(Player p, int targerId, int type, int count, int targetTotalCount, int voteCount){
		StringBuilder sb = new StringBuilder(100);
		sb.append("[BEAUTYVOTE]"+getPlayerLogString(p)+"TARGET["+targerId+"]TYPE["+type+"]COUNT["+count+"]TARGETCOUNT["+targetTotalCount+"]VOTECOUNT["+voteCount+"]");
		log.info(sb.toString());
	}
	
	/** 记录选美结束产生的佳丽 */
	public static void logBeautyEnd(int beautyId, int sex, int votes){
		StringBuilder sb = new StringBuilder(100);
		sb.append("[BEAUTYEND]ID["+beautyId+"]SEX["+sex+"]VOTES["+votes+"]");
		log.info(sb.toString());
	}
	
	/** 记录选美结束产生的投票最多的人 */
	public static void logBeautyMaxVotes(int playerId, int votes){
		StringBuilder sb = new StringBuilder(100);
		sb.append("[BEAUTYEND]ID["+playerId+"]VOTES["+votes+"]");
		log.info(sb.toString());
	}
	
	/** 记录战役副本开始 */
	public static void logExpansionBattleStart(){
		log.info("[EXPANSIONBATTLESTART]");
	}
	
	/** 记录战役副本结束 */
	public static void logExpansionBattleEnd(int winFaction, String cause){
		StringBuilder sb = new StringBuilder(100);
		sb.append("[EXPANSIONBATTLEEND]WINFACTION["+winFaction+"]CAUSE["+cause+"]");
		log.info(sb.toString());
	}
	
	/** 记录战役副本NPC刷新情况 */
	public static void logExpansionBattleNpcRefresh(int npcType, int count, int faction){
		StringBuilder sb = new StringBuilder(100);
		sb.append("[EXPANSIONNPC]TYPE["+npcType+"]COUNT["+count+"]FACTION["+faction+"]");
		log.info(sb.toString());
	}
	
	/** 记录玩家开启婚礼 */
	public static void logOpenWedding(Player p,int mateId,String mateName){
		StringBuilder sb = new StringBuilder(100);
		sb.append("[WEDDINGOPEN]"+getPlayerLogString(p)+"MATE["+mateId+"]NAME["+mateName+"]");
		log.info(sb.toString());
	}
	
	/** 记录玩家参加婚礼 */
	public static void logEnterWedding(Player p,int manId,String manName){
		StringBuilder sb = new StringBuilder(100);
		sb.append("[WEDDINGENTER]"+getPlayerLogString(p)+"MAN["+manId+"]NAME["+manName+"]");
		log.info(sb.toString());
	}
	
	/** 记录玩家婚礼签到 */
	public static void logSignInWedding(Player p,int manId,String manName){
		StringBuilder sb = new StringBuilder(100);
		sb.append("[WEDDINGSIGNIN]"+getPlayerLogString(p)+"MAN["+manId+"]NAME["+manName+"]");
		log.info(sb.toString());
	}
	
	/** 记录踢除宾客 */
	public static void logKickWedding(Player p,int targetId,String targetName){
		StringBuilder sb = new StringBuilder(100);
		sb.append("[WEDDINGKICK]"+getPlayerLogString(p)+"TARGET["+targetId+"]NAME["+targetName+"]");
		log.info(sb.toString());
	}
	
	/** 比武招亲报名 */
	public static void logDuleSignUp(Player p, int decMoney){
		if(p!=null){
			StringBuilder sb = new StringBuilder(100);
			sb.append("[DUELSIGNUP]"+getPlayerLogString(p)+"DECMONEY["+decMoney+"]");
			log.info(sb.toString());
		}
	}
	
	/** 比武招亲获胜者信息 */
	public static void logDuelWinner(int playerId){
		StringBuilder sb = new StringBuilder(100);
		Player p = ObjectAccessor.getPlayer(playerId);
		if(p!=null){
			sb.append("[DUELWINNER]"+getPlayerLogString(p));
		}else{
			sb.append("[DUELWINNER]ID["+playerId+"]");
		}
		log.info(sb.toString());
	}
	
	/** 塔防战役报名信息 */
	public static void logTowerDefendSignUp(Player p){
		if(p!=null){
			StringBuilder sb = new StringBuilder(100);
			sb.append("[TOWERDEFENDSIGNUP]"+getPlayerLogString(p));
			Party party = p.party;
			sb.append("MEMBER[");
			if(party!=null){
				List<PartyMember> members = party.members;
				for(PartyMember m : members){
					sb.append(m.getId()+" ");
				}
			}else{
				sb.append(p.id);
			}
			sb.append("]");
			log.info(sb.toString());
		}
	}
	
	/** 塔防对战方信息 */
	public static void logTowerDefendBattles(TowerDefend attack, TowerDefend defend){
		StringBuilder sb = new StringBuilder(100);
		sb.append("[TDBATTLE]");
		PartyService service = Server.server.getServiceRegistry().getPartyService();
		Party attackParty = service.getPartyById(attack.partyId);
		sb.append("ATTACK[");
		if(attackParty!=null){
			List<PartyMember> attackMembers = attackParty.members;
			for(PartyMember m : attackMembers){
				sb.append(m.getId()+" ");
			}
		}else{
			sb.append(attack.leader);
		}
		sb.append("]");
		Party defendParty = service.getPartyById(defend.partyId);
		sb.append("DEFEND[");
		if(defendParty!=null){
			List<PartyMember> defendMembers = defendParty.members;
			for(PartyMember m : defendMembers){
				sb.append(m.getId()+" ");
			}
		}else{
			sb.append(defend.leader);
		}
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录塔防战役胜利者信息 */
	public static void logTowerDefendWinner(TowerDefend win){
		StringBuilder sb = new StringBuilder(100);
		sb.append("[TDWIN]");
		sb.append("TYPE["+win.type+"]");
		PartyService service = Server.server.getServiceRegistry().getPartyService();
		Party party = service.getPartyById(win.partyId);
		sb.append("MEMBER[");
		if(party!=null){
			List<PartyMember> members = party.members;
			for(PartyMember m : members){
				sb.append(m.getId()+" ");
			}
		}else{
			sb.append(win.leader);
		}
		sb.append("]");
		log.info(sb.toString());
	}
	
	/** 记录战场举报处理 */
	public static void logProcessReport(Player p, int timeDis, int effectReport){
		if(p!=null){
			StringBuilder sb = new StringBuilder(200);
			sb.append("[PROCONHOOK]");
			sb.append(getPlayerLogString(p));
			sb.append("TIMEDIS["+timeDis+"]");
			sb.append("VOTES["+effectReport+"]");
			log.info(sb.toString());
		}
	}
	
	/** 记录创建血盟 */
	public static void logAssociationCreated(Player p, int associationId){
		if(p!=null){
			StringBuilder sb = new StringBuilder(200);
			sb.append("[ASSOCIATIONCREATE]");
			sb.append(getPlayerLogString(p));
			sb.append("ASSID["+associationId+"]");
			log.info(sb.toString());
		}
	}
	
	/** 记录盟主发送义结金兰邀请 */
	public static void logAssociationInvite(Player p, int targetId, int associationId){
		if(p!=null){
			StringBuilder sb = new StringBuilder(200);
			sb.append("[ASSOCIATIONINVITE]");
			sb.append(getPlayerLogString(p));
			sb.append("TARGET["+targetId+"]");
			sb.append("ASSID["+associationId+"]");
			log.info(sb.toString());
		}
	}
	
	/** 记录答复义结金兰邀请 */
	public static void logAssociationAnswer(Player p, int associationId, int answer){
		if(p!=null){
			StringBuilder sb = new StringBuilder(200);
			sb.append("[ASSOCIATIONANSWER]");
			sb.append(getPlayerLogString(p));
			sb.append("ASSID["+associationId+"]");
			sb.append("ANSWER["+answer+"]");
			log.info(sb.toString());
		}
	}
	
	/** 记录玩家退出血盟 */
	public static void logRemoveFromAssociation(int playerId, int associationId){
		StringBuilder sb = new StringBuilder(200);
		sb.append("[REMOVEFROMASSOCIATION]");
		sb.append("PLAYER["+playerId+"]");
		sb.append("ASSID["+associationId+"]");
		log.info(sb.toString());
	}
	
	/** 记录玩家正式加入血盟 */
	public static void logInjoyAssociation(Player p, int associationId){
		if(p!=null){
			StringBuilder sb = new StringBuilder(200);
			sb.append("[INJOYASSOCIATION]");
			sb.append(getPlayerLogString(p));
			sb.append("ASSID["+associationId+"]");
			log.info(sb.toString());
		}
	}
	
	/** 记录转让血盟 */
	public static void logTransferAssociation(Player p, int associationId, int targetId){
		if(p!=null){
			StringBuilder sb = new StringBuilder(200);
			sb.append("[TRANSFERASSOCIATION]");
			sb.append(getPlayerLogString(p));
			sb.append("ASSID["+associationId+"]");
			sb.append("TARGET["+targetId+"]");
			log.info(sb.toString());
		}
	}
	
	/** 记录因过期而删除等待状态的血盟成员 */
	public static void logRemoveFromAssCauseTimeOut(int playerId){
		try {
			StringBuilder sb = new StringBuilder(200);
			sb.append("[ASSOCIATIONTIMEOUT]");
			sb.append("PLAYER["+playerId+"]");
			log.info(sb.toString());
		} catch (Exception e) {
			
		}
	}
	
	/** 记录战功排行榜 */
	public static void logLevelRankScore(Actor actor){
		try {
			StringBuilder sb = new StringBuilder(200);
			sb.append("[RANKSCORE]");
			sb.append("PLAYER["+actor.id+"]");
			sb.append("LEVEL["+actor.level+"]");
			sb.append("RANK["+actor.rank+"]");
			log.info(sb.toString());
		} catch (Exception e) {
		}
	}
	
	/** 记录战功排行榜 */
	public static void logWeekRankScore(Actor actor){
		try {
			StringBuilder sb = new StringBuilder(200);
			sb.append("[WEEKRANKSCORE]");
			sb.append("PLAYER["+actor.id+"]");
			sb.append("RANK["+actor.rank+"]");
			log.info(sb.toString());
		} catch (Exception e) {
		}
	}
	
	/** 记录外挂信息 */
	public static void logAntiPlug(Player player, String cause){
		try {
			if(player!=null){
				StringBuilder sb = new StringBuilder(200);
				sb.append("[BOT]");
				sb.append(getPlayerLogString(player));
				if(player.getAccount()!=null && player.getAccount().getModel()!=null)
					sb.append("MODEL["+player.getAccount().getModel()+"]");
				if(cause.equalsIgnoreCase("ERROR") && player.antiPlug!=null 
						&& player.antiPlug.D!=null && player.antiPlug.D1!=null){
					sb.append("DATA[A:"+player.antiPlug.A+" B:"+player.antiPlug.B+" C:"
							+player.antiPlug.C+" D:"+player.antiPlug.D+" D1:"+player.antiPlug.D1+"]");
				}
				sb.append("CAUSE["+cause+"]");
				log.info(sb.toString());
			}
		} catch (Exception e) {
		}
	}
	
	/** 记录外挂信息 */
	public static void logAntiBot(Player player, String prefix, String cause){
		try {
			if(player!=null){
				StringBuilder sb = new StringBuilder(200);
				sb.append(prefix);
				sb.append(getPlayerLogString(player));
				if(player.getAccount()!=null && player.getAccount().getModel()!=null)
					sb.append("MODEL["+player.getAccount().getModel()+"]");
				sb.append("CAUSE["+cause+"]");
				log.info(sb.toString());
			}
		} catch (Exception e) {
		}
	}
	
	/** 随从激活技能位 */
	public static void logAttendantLightSkillSwitch(Player player, int id, int attendantInstanceId, int skillIndex){
		if(player!=null){
			StringBuilder sb = new StringBuilder(200);
			sb.append("[LIGHTATTSKILL]");
			sb.append(getPlayerLogString(player));
			sb.append("ID[");
			sb.append(id);
			sb.append("]ATTINS[");
			sb.append(attendantInstanceId);
			sb.append("]SKILLINDEX[");
			sb.append(skillIndex);
			sb.append("]");
			log.info(sb.toString());
		}
	}
	
	/** 随从学习新技能 */
	public static void logAttendantStudySkill(Player player, Skill oldSkill,int id, int instanceId, int skillId, int index){
		try {
			if(player!=null){
				StringBuilder sb = new StringBuilder(200);
				sb.append("[ATTSKILLLEARN]");
				sb.append(getPlayerLogString(player));
				sb.append("ID[");
				sb.append(id);
				sb.append("]ATTINS[");
				sb.append(instanceId);
				sb.append("]INDEX[");
				sb.append(index);
				sb.append("]SKILL[");
				sb.append(skillId>>16);
				sb.append("]");
				if(oldSkill!=null){
					sb.append("OLDSKILL[");
					sb.append(oldSkill.getId()>>16);
					sb.append("]");
				}
				log.info(sb.toString());
			}
		} catch (Exception e) {
			
		}
	}
	
	/** 放生随从 */
	public static void logRemoveAttendant(Player player, int id, int instanceId){
		if(player!=null){
			StringBuilder sb = new StringBuilder(200);
			sb.append("[REMOVEATT]");
			sb.append(getPlayerLogString(player));
			sb.append("ID[");
			sb.append(id);
			sb.append("]ATTINS[");
			sb.append(instanceId);
			sb.append("]");
			log.info(sb.toString());
		}
	}
	
	/**
	 * 保存系统设置
	 * @param player
	 * @param config
	 */
	public static void logSystemConfig(Player player,byte[] config){
		if(player != null && config != null){
			StringBuilder sb = new StringBuilder(50);
			int index = 1 + 4 * 27 + 3;//锁定按钮0隐藏1开启
			if(index < player.config.length){
				sb.append("[SYSCFG]").append(getPlayerLogString(player)).append("LK[").append(config[index]==0?"H":"S").append("]");
			}
			
			index = 1 + 4 * 28 + 3;//跟随按钮0隐藏1开启
			if(index < player.config.length){
				sb.append("FL[").append(player.config[index]==0?"H":"S").append("]");
			}
			
			log.info(sb.toString());
		}
		
	}
	
	/** 记录玩家动作 */
	public static void logAction(Player player, String prefix, String logStr, String subLogStr){
		if(player!=null){
			StringBuilder sb = new StringBuilder(200);
			sb.append("["+prefix+"]");
			sb.append(getPlayerLogString(player));
			sb.append("TYPE["+logStr+"]");
			if(subLogStr!=null && !subLogStr.equals(""))
				sb.append("SUBTYPE["+subLogStr+"]");
			log.info(sb.toString());
		}
	}
	
	/** 获取sessionid */
	public static String getSessionIdBySession(ClientSession session){
    	if(session!=null){
    		try {
				Field f = session.getClass().getDeclaredField("id");
				f.setAccessible(true);
				String id = f.get(session).toString();
				return id;
			} catch (Exception e) {
				return "0";
			} 
    	}
    	return "0";
    }
	/** 组队成功**/
	public static void joinPartySuccess(String name,int partId){
		StringBuilder sb = new StringBuilder(100);
		sb.append("[JOINPARTYSUCCESS]NAME[" + name + "]PARTYID[" + partId + "]");
		log.info(sb.toString());
	}
	/** 添加好友成功**/
	public static void addFriendSuccess(String name,String friendName){
		StringBuilder sb = new StringBuilder(100);
		sb.append("[ADDFRIEND]NAME[" + name + "]FRIENDNAME[" + friendName + "]");
		log.info(sb.toString());
	}
	
	/** 随从日志记录 */
	public static void logAttendant(Attendant attendant, String actionPrefix){
		try {
			if(attendant!=null){
				StringBuilder sb = new StringBuilder(200);
				sb.append(actionPrefix);
				if(attendant.owner!=null){
					sb.append("PLAYER["+attendant.owner.id+"]");
					sb.append("TID[" + attendant.attendantType.id);
					sb.append("]IMAGEID[" + attendant.attendantType.image.id);
					sb.append("]INS[");
					sb.append(attendant.instanceId+"]NAME[");
					sb.append(attendant.name+"]LOYAL[");
					sb.append(attendant.loyal+"]HP[");
					sb.append(attendant.hp+"]MP[");
					sb.append(attendant.mp+"]SWITCH[");
					for(int i=0;i<attendant.skillSwitchs.length;i++){
						sb.append(attendant.skillSwitchs[i]+",");
					}
					sb.append("]SKILLSIZE[");
					sb.append(attendant.skills.size()+"]SKILL[");
					for(int i=0;i<attendant.skills.size();i++){
						if(attendant.skills.get(i)!=null)
							sb.append(attendant.skills.get(i).getId()+",");
						else
							sb.append("0,");
					}
					sb.append("]ATTLEVEL[");
					sb.append(attendant.attLevel+"]SPECIALSKI[");
					for(int i=0;i<attendant.specialSkills.size();i++){
						if(attendant.specialSkills.get(i)!=null)
							sb.append(attendant.specialSkills.get(i).getId()+",");
						else
							sb.append("0,");
					}
					sb.append("]EQUS[");
					sb.append(Attendant.getEquipLog(attendant.equs)+"]");
					log.info(sb.toString());
				}
			}
		} catch (Exception e) {
			
		}
	}
	
	/** 记录玩家装备触魔 */
	public static void logAddCardHole(Player player, int gameItemId, int gameItemInstanceId, int hole){
		try {
			if(player!=null){
				StringBuilder sb = new StringBuilder(200);
				sb.append("[ADDCARDHOLE]");
				sb.append(getPlayerLogString(player));
				sb.append("ITEMID["+gameItemId+"]");
				sb.append("ITEMINSTANCEID["+gameItemInstanceId+"]");
				sb.append("HOLE["+hole+"]");
				log.info(sb.toString());
			}
		} catch (Exception e) {
		}
	}
	
	/** 记录玩家卡片附魔 */
	public static void logAddCard(Player player, int gameItemId, int gameItemInstanceId, int hole, int cardId, int quality){
		try {
			if(player!=null){
				StringBuilder sb = new StringBuilder(200);
				sb.append("[ADDCARD]");
				sb.append(getPlayerLogString(player));
				sb.append("ITEMID["+gameItemId+"]");
				sb.append("ITEMINSTANCEID["+gameItemInstanceId+"]");
				sb.append("HOLE["+hole+"]");
				sb.append("CARDID["+cardId+"]");
				sb.append("QUALITY["+quality+"]");
				log.info(sb.toString());
			}
		} catch (Exception e) {
		}
	}
	
	/** 记录玩家卡片充能 */
	public static void logAddCardEnergy(Player player, int cardId, int oldEnergy, int oldMaxEnergy, int energy, int maxEnegry){
		try {
			if(player!=null){
				StringBuilder sb = new StringBuilder(200);
				sb.append("[ADDCARDENERGY]");
				sb.append(getPlayerLogString(player));
				sb.append("CARDID["+cardId+"]");
				sb.append("OLDENERGY["+oldEnergy+"]");
				sb.append("OLDMAXENERGY["+oldMaxEnergy+"]");
				sb.append("ENERGY["+energy+"]");
				sb.append("MAXENERGY["+maxEnegry+"]");
				log.info(sb.toString());
			}
		} catch (Exception e) {
		}
	}
	
	/** 结为师徒日志**/
	public static void addApprentice(Player teacher,Player apprentice){
		try{
			StringBuilder sb = new StringBuilder(100);
			sb.append("[CREATEAPPRENTICE]");
			LogUtil.getPlayerLogString(sb, teacher);
			sb.append("APPRENTICE[");
			sb.append(apprentice.id+"]APPRENTICESIZE[");
			int size = 0;
			if(teacher.relations != null && teacher.relations.apprenticeList!=null){
				size = teacher.relations.apprenticeList.getCount();
			}
			sb.append(size);
			log.info(sb.toString());
		} catch (Exception e){
			
		}
	}
	
	/** 解除师徒日志**/
	public static void removeApprentice(Player p,int targetId){
		try{
			StringBuilder sb = new StringBuilder(100);
			sb.append("[REMOVEAPPRENTICE]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("LEVEL["+p.level+"]");
			sb.append("TARGET[");
			sb.append(targetId+"]");
			log.info(sb.toString());
		} catch (Exception e){
			
		}
	}
	
	/** 记录完成师徒任务 */
	public static void logFinishApprenticeQuest(Player p, int questID, int branch, int result) {
		//如果成功完成师徒任务，打印师徒的ID
		try{
			if(result == 0 && (questID == 2011 || questID == 3049 || questID == 3050)){
				StringBuilder sb = new StringBuilder(50);
				sb.append("[FINISHAPPRENTICEQUEST]");
				LogUtil.getPlayerLogString(sb, p);
				int teacherId = p.id;
				int apprenticeId = p.id;
				if(p.level >=70){
				    Player apprentice = p.party.getPartyFriend(p.id);
				    if(apprentice != null){
				       apprenticeId = apprentice.id;
				    }
				} else {
					teacherId = p.getTeacherId();
				}
				sb.append("TEACHERID[").append(teacherId).append("]");
				sb.append("APPRENTICEID[").append(apprenticeId).append("]");
				sb.append("QUEST[").append(questID).append("]BRANCH[").append(branch).append("]RESULT[").append(result).append("]");
				log.info(sb.toString());
			}
		} catch (Exception e){
			
		}
	}
	
	/** 完成成就 */
	public static void logFinishAchievement(int pId,String achieveName){
		try{
			StringBuilder sb = new StringBuilder(50);
			sb.append("[FINISHACHIEVE]");
			sb.append("ID[").append(pId).append("]");
			sb.append("ACHIEVENAME[").append(achieveName).append("]");
			log.info(sb.toString());
		} catch (Exception e){
			
		}
	}
	
	/** 绑定微博TRY */
	public static void logBindWeiboTry(Player p,String weiboName){
		try{
			StringBuilder sb = new StringBuilder(50);
			sb.append("[BINDWEIBOTRY]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("WEIBONAME[").append(weiboName).append("]TRY");
			log.info(sb.toString());
		} catch (Exception e){
			
		}
	}
	
	/** 绑定微博 */
	public static void logBindWeibo(Player p,String weiboName){
		try{
			StringBuilder sb = new StringBuilder(50);
			sb.append("[BINDWEIBO]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("WEIBONAME[").append(weiboName).append("]");
			log.info(sb.toString());
		} catch (Exception e){
			
		}
	}
	
	/** 绑定微博结果 */
	public static void logBindWeiboResult(Player p,String weiboName,String result){
		try{
			StringBuilder sb = new StringBuilder(50);
			sb.append("[BINDWEIBORESULT]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("WEIBONAME[").append(weiboName).append("]RESULT[").append(result).append("]");
			log.info(sb.toString());
		} catch (Exception e){
			
		}
	}
	
	/** 解绑微博 */
	public static void logUnBindWeibo(Player p,String weiboName){
		try{
			StringBuilder sb = new StringBuilder(50);
			sb.append("[UNBINDWEIBO]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("WEIBONAME[").append(weiboName).append("]");
			log.info(sb.toString());
		} catch (Exception e){
			
		}
	}
	
	/** 发送微博TRY */
	public static void logSendWeiboTry(Player p,String weiboName,String weiboMessage){
		try{
			StringBuilder sb = new StringBuilder(50);
			sb.append("[SENDWEIBOTRY]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("WEIBONAME[").append(weiboName).append("]");
			sb.append("WEIBOCONTENT[").append(weiboMessage).append("]TRY");
			log.info(sb.toString());
		} catch (Exception e){
			
		}
	}
	
	/** 发送微博 */
	public static void logSendWeibo(Player p,String weiboName,String weiboMessage){
		try{
			StringBuilder sb = new StringBuilder(50);
			sb.append("[SENDWEIBO]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("WEIBONAME[").append(weiboName).append("]");
			sb.append("WEIBOCONTENT[").append(weiboMessage).append("]");
			log.info(sb.toString());
		} catch (Exception e){
			
		}
	}
	
	/** 快速注册TRY */
	public static void logWeiboQuickRegistrateTry(Player p){
		try{
			StringBuilder sb = new StringBuilder(50);
			sb.append("[QUICKREGISTRATETRY]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("TRY");
			log.info(sb.toString());
		} catch (Exception e){
			
		}
	}
	
	/** 快速注册 */
	public static void logWeiboQuickRegistrate(Player p){
		try{
			StringBuilder sb = new StringBuilder(50);
			sb.append("[QUICKREGISTRATE]");
			LogUtil.getPlayerLogString(sb, p);
			log.info(sb.toString());
		} catch (Exception e){
			
		}
	}
	
	public static void logWeiboAddFriendShip(Player p,String weiboName,String addName,String result){
		try{
			StringBuilder sb = new StringBuilder(50);
			sb.append("[WEIBOADDFRIENDSHIP]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("WEIBONAME[").append(weiboName).append("]");
			sb.append("FRIENDNAME[").append(addName).append("]");
			sb.append("RESULT[").append(result).append("]");
			log.info(sb.toString());
		} catch (Exception e){
			
		}
	}
	
	
	
	/** 发送微博结果 */
	public static void logSendWeiboResult(Player p,String weiboName,String weiboMessage,String result){
		try{
			StringBuilder sb = new StringBuilder(50);
			sb.append("[SENDWEIBORESULT]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("WEIBONAME[").append(weiboName).append("]");
			sb.append("WEIBOCONTENT[").append(weiboMessage).append("]");
			sb.append("RESULT[").append(result).append("]");
			log.info(sb.toString());
		} catch (Exception e){
			
		}
	}
	
	
	/** 记录完成结义任务 */
	public static void logFinishAssociationQuest(Player p, int questID, int branch, int result) {
		//如果成功完成结义任务任务，打印盟主的ID
		try{
			if(result == 0 && (questID == 2138 || questID == 2139 || questID == 2146|| questID == 2323 || questID == 2324 ||
					questID == 2325 || questID == 2326 || questID == 2327 || questID == 2328 || questID == 2329
					|| questID == 2330 || questID == 2331)){
				StringBuilder sb = new StringBuilder(50);
				sb.append("[FINISHASSOCIATIONQUEST]");
				LogUtil.getPlayerLogString(sb, p);
				sb.append("ASSOLEADERID[").append(p.getAssociationLeaderID()).append("]");
				sb.append("QUEST[").append(questID).append("]BRANCH[").append(branch).append("]RESULT[").append(result).append("]");
				log.info(sb.toString());
			}
		} catch (Exception e){
			
		}
	}
	
	/** 记录完成奇遇任务 */
	public static void logFinishQiYuQuest(Player p, int questID, int branch, int result) {
		try{
			if(result == 0 && (questID == 1140 || questID == 1141 || questID == 1144|| questID == 1148 || questID == 1149 ||
					questID == 1152 || questID == 1153 || questID == 1156 || questID == 1160 || questID == 1168
					|| questID == 1164 || questID == 1172 || questID == 1100)){
				StringBuilder sb = new StringBuilder(50);
				sb.append("[FINISHQIYUQUEST]");
				LogUtil.getPlayerLogString(sb, p);
				sb.append("QUEST[").append(questID).append("]BRANCH[").append(branch).append("]RESULT[").append(result).append("]");
				log.info(sb.toString());
			}
		} catch (Exception e){
			
		}
	}
	
	/** 记录完成跑环任务 */
	public static void logFinishCycleQuest(Player p, int questID, int branch, int result) {
		try{
			if(result == 0 && (questID == 1359 || questID == 1369 || questID == 1379|| questID == 1389 || questID == 1399 ||
					questID == 1279 || questID == 1289 || questID == 1309 || questID == 1319 || questID == 1329 || questID == 1339
					|| questID == 1349 || questID == 1239 || questID == 1249 || questID == 1409 || questID == 1419 || questID == 1429 
					|| questID == 1439 || questID == 1299|| questID == 1259 || questID == 1269)){
				StringBuilder sb = new StringBuilder(50);
				sb.append("[FINISHCYCLEQUEST]");
				LogUtil.getPlayerLogString(sb, p);
				sb.append("QUEST[").append(questID).append("]BRANCH[").append(branch).append("]RESULT[").append(result).append("]");
				log.info(sb.toString());
			}
		} catch (Exception e){
			
		}
	}
	
	/** 记录完成驯兽环任务 */
	public static void logFinishXunshouQuest(Player p, int questID, int branch, int result) {
		try{
			if(result == 0 && (questID == 2260 || questID == 2261 || questID == 2262)){
				StringBuilder sb = new StringBuilder(50);
				sb.append("[FINISHXUNSHOUQUEST]");
				LogUtil.getPlayerLogString(sb, p);
				sb.append("QUEST[").append(questID).append("]BRANCH[").append(branch).append("]RESULT[").append(result).append("]");
				log.info(sb.toString());
			}
		} catch (Exception e){
			
		}
	}
	
	/** 记录完成斗酒环任务 */
	public static void logFinishDoujiuQuest(Player p, int questID, int branch, int result) {
		try{
			if(result == 0 && (questID == 2494 || questID == 2495 || questID == 2496 || questID == 2497 || questID == 2498 
					|| questID == 2499 || questID == 2500 || questID == 2501 || questID == 2502)){
				StringBuilder sb = new StringBuilder(50);
				sb.append("[FINISHDOUJIUQUEST]");
				LogUtil.getPlayerLogString(sb, p);
				sb.append("QUEST[").append(questID).append("]BRANCH[").append(branch).append("]RESULT[").append(result).append("]");
				log.info(sb.toString());
			}
		} catch (Exception e){
			
		}
	}
	
	public static void logOpenMonthPay(Player p,int itemId,String name){
		try{
			StringBuilder sb = new StringBuilder(50);
			sb.append("[MONTHPAY]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("ITEMID[").append(itemId).append("]NAME[").append(name).append("]");
			log.info(sb.toString());
		}catch(Exception e){
			
		}
	}
	
	/** 玩家在新手村移动日志 */
	public static void logMove(Player p){
		try{
			if(StatService.isInArray(PlayerService.newComerMap, p.map.map.getId())!=-1 || StatService.trackLogPlayer.contains(p.id)){
				StringBuilder sb = new StringBuilder(50);
				sb.append("[MOVE]");
				LogUtil.getPlayerLogString(sb, p);
				sb.append("MAPID[").append(p.map.map.getId()).append("]X[").append(p.x).append("]");
				sb.append("Y[").append(p.y).append("]");
				log.info(sb.toString());
			}
		}catch(Exception e){
			
		}
	}
	
	/** 玩家出地图 */
	public static void logTouchExitToOutVMap(Player p) {
		try{
			if(StatService.isInArray(PlayerService.newComerMap, p.map.map.getId())!=-1){
				StringBuilder sb = new StringBuilder(100);
				sb.append("[OUTVMAP]");
				LogUtil.getPlayerLogString(sb, p);
				sb.append("MAP[").append(p.map.map.getId()).append("]");
				sb.append("X[").append(p.x).append("]");
				sb.append("Y[").append(p.y).append("]");
				log.info(sb.toString());
			}
		}catch(Exception e){
			
		}
	}
	
	/** 玩家进地图 */
	public static void logEnterVMap(Player p, long hTime) {
		try{
			if(StatService.isInArray(PlayerService.newComerMap, p.map.map.getId())!=-1){
				StringBuilder sb = new StringBuilder(100);
				sb.append("[ENTERVMAP]");
				LogUtil.getPlayerLogString(sb, p);
				sb.append("MAP[").append(p.map.map.getId()).append("]");
				sb.append("X[").append(p.x).append("]");
				sb.append("Y[").append(p.y).append("]");
				sb.append("HTIME[").append(hTime).append("]");
			    log.info(sb.toString());
			}
		}catch(Exception e){
			
		}
	}
	
	/** 玩家开始阅读书籍 */
	public static void logReadBook(Player p,Book b) {
		try{
			StringBuilder sb = new StringBuilder(100);
			sb.append("[READBOOK]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("BOOKID[").append(b.getId()).append("]");
			sb.append("CHAPTER[").append(b.chapter).append("]");
			log.info(sb.toString());
		}catch(Exception e){
			
		}
	}
	
	/** 缩短书籍时间TRY */
	public static void logPayForBookTry(Player p,Book b,String cause) {
		try{
			StringBuilder sb = new StringBuilder(100);
			sb.append("[BOOKPAY]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("BOOKID[").append(b.getId()).append("]");
			sb.append("CHAPTER[").append(b.chapter).append("]");
			sb.append("CASE[").append(cause).append("]TRY");
			log.info(sb.toString());
		}catch(Exception e){
			
		}
	}
	
	/** 缩短书籍时间成功 */
	public static void logPayForBookSuccess(Player p,Book b,String cause) {
		try{
			StringBuilder sb = new StringBuilder(100);
			sb.append("[BOOKPAY]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("BOOKID[").append(b.getId()).append("]");
			sb.append("CHAPTER[").append(b.chapter).append("]");
			sb.append("CASE[").append(cause).append("]SUCCESS");
			log.info(sb.toString());
		}catch(Exception e){
			
		}
	}
	
	/** 缩短书籍时间失败 */
	public static void logPayForBookFail(Player p,Book b,String cause) {
		try{
			StringBuilder sb = new StringBuilder(100);
			sb.append("[BOOKPAY]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("BOOKID[").append(b.getId()).append("]");
			sb.append("CHAPTER[").append(b.chapter).append("]");
			sb.append("CASE[").append(cause).append("]FAIL");
			log.info(sb.toString());
		}catch(Exception e){
			
		}
	}
	
	/** 记录玩家摇(祈福树)得卡片 */
	public static void logVowCard(Player player, int itemId, int type, int quality, int gainExp){
		try {
			StringBuilder sb = new StringBuilder(100);
			sb.append("[VOWCARD]");
			sb.append(getPlayerLogString(player));
			sb.append("ITEM["+itemId+"]");
			sb.append("TYPE["+type+"]");
			sb.append("QUALITY["+quality+"]");
			sb.append("EXP["+gainExp+"]");
			log.info(sb.toString());
		} catch (Exception e) {
			
		}
	}
	
	/** 记录玩家镶嵌卡片 */
	public static void logEquipCard(Player player, int cardId, int type, int index){
		try {
			StringBuilder sb = new StringBuilder(100);
			sb.append("[EQUIPCARD]");
			sb.append(getPlayerLogString(player));
			sb.append("CARDID["+cardId+"]");
			sb.append("OWNER["+type+"]");
			sb.append("INDEX["+index+"]");
			log.info(sb.toString());
		} catch (Exception e) {
			
		}
	}
	
	/** 记录玩家摘除卡片 */
	public static void logUnEquipCard(Player player, int cardId, int type, int index){
		try {
			StringBuilder sb = new StringBuilder(100);
			sb.append("[UNEQUIPCARD]");
			sb.append(getPlayerLogString(player));
			sb.append("CARDID["+cardId+"]");
			sb.append("OWNER["+type+"]");
			sb.append("INDEX["+index+"]");
			log.info(sb.toString());
		} catch (Exception e) {
			
		}
	}
	
	/** 记录玩家升级卡片 */
	public static void logUpGradeCard(Player player, int cardId, int currentLevel){
		try {
			StringBuilder sb = new StringBuilder(100);
			sb.append("[UPGRADECARD]");
			sb.append(getPlayerLogString(player));
			sb.append("CARDID["+cardId+"]");
			sb.append("LEVEL["+currentLevel+"]");
			log.info(sb.toString());
		} catch (Exception e) {
			
		}
	}
	
	/** 记录玩家卡片经验重置 */
	public static void logRemoveCardExp(Player player, int cardId){
		try {
			StringBuilder sb = new StringBuilder(100);
			sb.append("[REMOVECARDEXP]");
			sb.append(getPlayerLogString(player));
			sb.append("CARDID["+cardId+"]");
			log.info(sb.toString());
		} catch (Exception e) {
			
		}
	}
	
	/** 记录玩家卡片经验变换 */
	public static void logCardExpChange(Player player, int oldExp, int currentExp){
		try {
			StringBuilder sb = new StringBuilder(100);
			sb.append("[CHGCARDEXP]");
			sb.append(getPlayerLogString(player));
			sb.append("OLDEXP["+oldExp+"]");
			sb.append("CURRENTEXP["+currentExp+"]");
			log.info(sb.toString());
		} catch (Exception e) {
			
		}
	}
	
	/** 开始副本扫荡 */
	public static void logSweepStart(Player p,String instanceName,int time) {
		try{
			StringBuilder sb = new StringBuilder(100);
			sb.append("[SWEEPSTART]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("INSTANCENAME[").append(instanceName).append("]");
			sb.append("TIMES[").append(time).append("]");
			log.info(sb.toString());
		}catch(Exception e){
			
		}
	}
	
	/** 副本扫荡结束 */
	public static void logSweepEnd(Player p,String instanceName,int time) {
		try{
			StringBuilder sb = new StringBuilder(100);
			sb.append("[SWEEPEND]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("INSTANCENAME[").append(instanceName).append("]");
			sb.append("TIMES[").append(time).append("]");
			log.info(sb.toString());
		}catch(Exception e){
			
		}
	}
	
	/** 副本扫荡缩短时间 */
	public static void logSweepPay(Player p,String instanceName,int time,String result) {
		try{
			StringBuilder sb = new StringBuilder(100);
			sb.append("[SWEEPPAY]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("INSTANCENAME[").append(instanceName).append("]");
			sb.append("TIMES[").append(time).append("]").append(result);
			log.info(sb.toString());
		}catch(Exception e){
			
		}
	}
	
	/** 副本扫荡辅助日志 */
	public static void logSweepLog(Player p,String instanceName,int price) {
		try{
			StringBuilder sb = new StringBuilder(100);
			sb.append("[SWEELOG]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("INSTANCENAME[").append(instanceName).append("]");
			sb.append("PRICE[").append(price).append("]");
			log.info(sb.toString());
		}catch(Exception e){
			
		}
	}
	
	
	/** 星辉升级 */
	public static void logStarPromoteTry(Player p,GameItem item) {
		try{
			StringBuilder sb = new StringBuilder(100);
			sb.append("[STARPROMOTE]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("ITEM[");
			LogUtil.getGameItemString(sb, item, 1);
			sb.append("TRY");
			log.info(sb.toString());
		}catch(Exception e){
			
		}
	}
	
	/** 星辉升级结果 */
	public static void logStarPromoteEnd(Player p,GameItem item,int type,String result) {
		try{
			StringBuilder sb = new StringBuilder(100);
			sb.append("[STARPROMOTE]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("ITEM[");
			LogUtil.getGameItemString(sb, item, 1);
			sb.append("]METHOD[").append(type).append("]");
			sb.append(result);
			log.info(sb.toString());
		}catch(Exception e){
			
		}
	}
	
	/** 打印特定玩家日志 */
	public static void logTrackPlayerLog(Player p,int x,int y,int skillId) {
		if(p!=null && StatService.trackLogPlayer.contains(p.id)){
			try{
				StringBuilder sb = new StringBuilder(100);
				sb.append("[TRACKPLAYERLOG]");
				LogUtil.getPlayerLogString(sb, p);
				sb.append("X[").append(p.x).append("]");
				sb.append("Y[").append(p.y).append("]");
				sb.append("X1[").append(x).append("]");
				sb.append("Y1[").append(y).append("]");
				sb.append("SKILLID[").append(skillId).append("]");
				log.info(sb.toString());
			}catch(Exception e){
				
			}
		}
	}
	
	/** 获得福利值*/
	public static void logAddFuliValue(Player p,int oldValue,int newValue){
		try{
			StringBuilder sb = new StringBuilder(100);
			sb.append("[ADDFULI]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("V1[").append(oldValue).append("]");
			sb.append("V1[").append(newValue).append("]");
			log.info(sb.toString());
		}catch(Exception e){
			
		}
	}
	
	/** 扣除福利值*/
	public static void logDecFuliValue(Player p,int oldValue,int newValue,int gambleId,int typeId){
		try{
			StringBuilder sb = new StringBuilder(100);
			sb.append("[DECFULI]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("V1[").append(oldValue).append("]");
			sb.append("V1[").append(newValue).append("]");
			sb.append("]GAMBLEID[").append(gambleId).append("]");
			sb.append("]TYPEID[").append(typeId).append("]");
			log.info(sb.toString());
		}catch(Exception e){
			
		}
	}
	
	/** 抽奖结果*/
	public static void logFuliResult(Player p,String result){
		try{
			StringBuilder sb = new StringBuilder(100);
			sb.append("[FULIRESULT]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("]RESULT[").append(result).append("]");
			log.info(sb.toString());
		}catch(Exception e){
			
		}
	}
	
	
	public static void logSalaryLimit(Player p,String cause){
		try{
			StringBuilder sb = new StringBuilder(100);
			sb.append("[SALARYLIMIT]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("RESULT[").append(cause).append("]");
			log.info(sb.toString());
		}catch(Exception e){
			
		}
	}
	
	public static void logFeastEnter(Player p,int insId,int count){
		try{
			StringBuilder sb = new StringBuilder(100);
			sb.append("[FEASTENTER]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("INS[").append(insId).append("]");
			sb.append("COUNT[").append(count).append("]");
			log.info(sb.toString());
		}catch(Exception e){
			
		}
	}
	
	public static void logFeastSign(Player p,int size){
		try{
			StringBuilder sb = new StringBuilder(100);
			sb.append("[FEASTENTER]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("size[").append(size).append("]");
			log.info(sb.toString());
		}catch(Exception e){
			
		}
	}
	
	/** 获得随从经验*/
	public static void logAddAttendantExp(Player p,int oldValue,int newValue,String cause){
		try{
			StringBuilder sb = new StringBuilder(100);
			sb.append("[ADDATTENDANTEXP]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("V1[").append(oldValue).append("]");
			sb.append("V2[").append(newValue).append("]");
			sb.append("RESULT[").append(cause).append("]");
			log.info(sb.toString());
		}catch(Exception e){
			
		}
	}
	
	/** 扣除随从经验*/
	public static void logDecAddAttendantExp(Player p,int id,int oldValue,int newValue,int oldLevel,int newLevel,String cause){
		try{
			StringBuilder sb = new StringBuilder(100);
			sb.append("[DECATTENDANTEXP]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("ID[").append(id).append("]");
			sb.append("V1[").append(oldValue).append("]");
			sb.append("V2[").append(newValue).append("]");
			sb.append("L1[").append(oldLevel).append("]");
			sb.append("L2[").append(newLevel).append("]");
			sb.append("RESULT[").append(cause).append("]");
			log.info(sb.toString());
		}catch(Exception e){
			
		}
	}
	
	/** VIP等级升级*/
	public static void logVipLevelUp(Player p,int oldValue,int newValue,int oldCharge,int newCharge,String cause){
		try{
			StringBuilder sb = new StringBuilder(100);
			sb.append("[VIPLEVELUP]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("OLDLEVEL[").append(oldValue).append("]");
			sb.append("NEWLEVEL[").append(newValue).append("]");
			sb.append("OLDCHARGE[").append(oldCharge).append("]");
			sb.append("NEWCHARGE[").append(newCharge).append("]");
			sb.append("RESULT[").append(cause).append("]");
			log.info(sb.toString());
		}catch(Exception e){
			
		}
	}
	
	/** VIP经验变化*/
	public static void logVipExpChange(Player p,int oldCharge,int newCharge,String cause){
		try{
			StringBuilder sb = new StringBuilder(100);
			sb.append("[VIPEXPCHANGE]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("OLDCHARGE[").append(oldCharge).append("]");
			sb.append("NEWCHARGE[").append(newCharge).append("]");
			sb.append("RESULT[").append(cause).append("]");
			log.info(sb.toString());
		}catch(Exception e){
			
		}
	}
	
	/**修改玩家修炼经验*/
	public static void logAlchemyExpDec(Player p,int oldValue,int newValue){
		try{
			StringBuilder sb = new StringBuilder(100);
			sb.append("[ALCHEMYEXPDEC]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("OLDVALUE[").append(oldValue).append("]");
			sb.append("NEWVALUE[").append(newValue).append("]");
			log.info(sb.toString());
		}catch(Exception e){
			
		}
	}
	
	public static void logKingItemEffectBefore(Player p,Horse h){
		try{
			StringBuilder sb = new StringBuilder(100);
			sb.append("[KINGBEFORECHANGE]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("HORSE[");
			LogUtil.getHorseString(sb, h);
			sb.append("]");
			log.info(sb.toString());
		}catch(Exception e){
			
		}
	}
	
	public static void logKingItemEffect(Player p,Horse h){
		try{
			StringBuilder sb = new StringBuilder(100);
			sb.append("[KINGCHANGEIMAGE]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("HORSE[");
			LogUtil.getHorseString(sb, h);
			sb.append("]");
			log.info(sb.toString());
		}catch(Exception e){
			
		}
	}
	
	public static void logAttChangeLoyal(Attendant att,int oldValue,int newValue){
		try{
			StringBuilder sb = new StringBuilder(100);
			if(oldValue>newValue)
			     sb.append("[ATTADDLOYAL]");
			else
				sb.append("[ATTDECLOYAL]");
			sb.append("ID["+att.id+"]");
			sb.append("INSTANCEID["+att.instanceId+"]");
			sb.append("ATTNAME["+att.name+"]");
			sb.append("V1["+oldValue+"]");
			sb.append("V2["+newValue+"]");
			log.info(sb.toString());
		}catch(Exception e){
			
		}
	}
	
	public static void logAward(Player p,int count,int itemId){
		try{
			StringBuilder sb = new StringBuilder(100);
			sb.append("[AWARD]");
			LogUtil.getPlayerLogString(sb, p);
			sb.append("VIPLEVEL["+p.vipLevel+"]");
			sb.append("COUNT["+count+"]");
			sb.append("ITEMID["+itemId+"]");
			log.info(sb.toString());
		}catch(Exception e){
			
		}
	}
	
	
}
