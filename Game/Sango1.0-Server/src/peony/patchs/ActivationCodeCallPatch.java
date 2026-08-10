package peony.patchs;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.Gain;
import peony.game.GameItem;
import peony.game.ItemUtil;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.chinarun.ChinarunCall;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.account.Account;

public class ActivationCodeCallPatch extends ClientSessionAsyncCall {

	private static final Logger log = Logger.getLogger(ChinarunCall.class);

	protected int serial;
	protected String activationCode;
	protected Player player;

	protected static final int[][] TYPES2ITEMID = {
		{1001,ItemUtil.ITEM_ACTIVATION}, //激活码兑换礼包
		{1003,ItemUtil.ITEM_ACTIVATION},//激活码兑换礼包
		{1004,ItemUtil.ITEM_ACTIVATION1},//激活码兑换礼包
		{1005,ItemUtil.ITEM_ACTIVATION2},//明珠社区积分礼包
		{1010,1360},//	3级蓝宝石 1010
		{1011,1353},//	3级黄宝石 1011
		{1012,1367},//	3级绿宝石 1012
		{1013,1416},//	3级红宝石 1013
		{1014,665},//	人物双倍经验符 1014
		{1015,666},//	坐骑双倍经验符 1015
		{1016,1185},//	人物属性重置符 1016
		{1017,821},//	坐骑技能遗忘书 1017
		{1018,661},//	都城传送符 1018
		{1019,673},//	20背包扩展符 1019 
		{1020,676},//	国家喊话符 1020
		{1021,670},//	还魂香包 1021 
		{1022,1578}, //装备打造符
		{1023,1336}, //低级宝石合成符
		{1024,1337}, //高级宝石合成符
		{1025,1338}, //初级打孔符
		{1026,1339}, //中级打孔符
		{1027,1340}, //高级打孔符
		{1028,1342}, //低级镶嵌符
		{1029,1343}, //高级镶嵌符
		{1030,1374}, //3级琥珀
		{1031,1381}, //3级玛瑙
		{1032,1388}, //3级孔雀
		{1033,1395}, //3级猫眼
		{1034,1579}, //低级星级鉴定符
		{1035,1580}, //高级星级鉴定符
		{1036,653}, //超级大还丹
		{1037,654}, //超级大补散
		{1038,659}, //超级金疮药
		{1039,660}, //超级忘忧露
		{1040,1582}, //资质鉴定符
		{1041,1183}, //一盒酥
		{1042,1165}, //传送符
		{1043,677}, //世界喊话符
		//mengjie add
		{1044,1240}, //(活动礼包)双倍经验符*3、30点增力符*5、坐骑技能遗忘书*2、都城传送符*8 、一合酥*8 
		{1045,1160}, //(活动礼包)双倍经验符*2、30点增体符*5 、坐骑技能遗忘书*2、都城传送符*5、一合酥*5
		{1046,490}, //(活动礼包)双倍经验符*1 、30点增体符*2、都城传送符*2
		{1047,1161}, //(军团礼包)双倍经验符*2、30点增体符*5、都城传送符*5、一合酥*2、礼包价值：5。5元
		{1048,1111}, //情人节豪华蓝玫瑰
		{1049,1112}, //情人节蓝玫瑰
		{1050,1902}, //资料片活动礼包
		{1051,1903}, //中秋大礼包
		{1052,1904}, //爱慕情侣礼包
		{1053,1905}, //至尊礼包
		{1054,1906}, //畅玩礼包
		{1055,1907}, //财童幸运礼包
		{1056,1908}, //大财包
		{1057,1668}, //战熊（武将）
		{1058,1669}, //战熊（刺客）
		{1059,1670}, //战熊（谋士）
		{1060,1671}, //战熊（方士）
		{1061,1651}, //玄冰白虎（刺客）
		{1062,1899}, //卡片礼包
		{1063,1895}, //传送礼包
		{1064,1896}, //经验包
		{1065,1818}, //装备修理符
		{1066,819}, //坐骑口粮
		{1067,1242}, //高级坐骑口粮
		{1068,1244}, //10仓库扩展符
		{1069,655}, //超级九转还魂丹
		{1070,656}, //超级首乌还神散
		{1071,1197}, //30点增力符
		{1072,1243}, //人物技能重置符
		{1073,2265}, //岁末扛鼎巨制礼包
		{1074,2263}, //明珠三国大礼包
		{1075,2260}, //17SY<<明珠三国>>庆周年专属礼包
		{1076,2166}, //雪岩虎
		{1077,2249}, //三国畅游17SY特制礼包
		{1078,2329}, //明珠赐福梦想礼包
	};


	public ActivationCodeCallPatch(ClientSession session, Packet pt, Player player) {
		super(session);
		this.serial = pt.getInt();
		activationCode = pt.getString();
		this.player = player;
		LogUtil.logActivationCodeTry(player, activationCode);
	}

	public void callFinish() throws Exception {
		if (success) {
			Packet pt = new Packet(OpCode.ACTIVATIONCODE_SERVER);
			pt.putInt(serial);
			session.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.ACTIVATIONCODE_CLINET, errorMessage);
		}
	}

	public void run() {
		PostMethod method = new PostMethod(Server.server.billingURL + "card_check");
		method.getParams().setContentCharset("utf-8");
		method.addRequestHeader("Connection", "close");
		method.setParameter("cardno", activationCode);// 兑换卡号
		method.setParameter("gamecode", "5");// 游戏代码，5 - 三国
		method.setParameter("cardtype", "-1");// 允许的兑换物品类型
		int accid = ((Account)session.getIdentity()).getId();
		method.setParameter("accountid", String.valueOf(accid));// 帐号ID
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
		int code = 0;
		BufferedReader br = null;
		try {
			HttpClient httpclient = new HttpClient();
			httpclient.getHttpConnectionManager().getParams()
					.setConnectionTimeout(30000);
			httpclient.getParams().setSoTimeout(30000);
			code = httpclient.executeMethod(method);
			br = new BufferedReader(new InputStreamReader(method
					.getResponseBodyAsStream(), "UTF-8"));
			if (code == 200) {
				String line = br.readLine();
				int retCode = Integer.parseInt(line);
				LogUtil.logActivationCodeOK(player, activationCode, retCode);
				if (retCode == 0) {
					// 在TYPES2ITEMID表中查找对应的物品ID,如果找不到，就用激活码类型作为物品ID
					int itemstype = Integer.valueOf(br.readLine()).intValue();
					int matchItemID = itemstype;
					for(int i=0;i<TYPES2ITEMID.length;i++){
						if(TYPES2ITEMID[i][0]==itemstype){
							matchItemID = TYPES2ITEMID[i][1];
						}
					}
					
					// 创建物品加入玩家背包
					Player p = ObjectAccessor.getPlayer(player.id);
					GameItem item = ObjectAccessor.createGameItem(matchItemID);
					if (p != null) {
						PlayerTransaction tx = p.newTransaction("ACT");
						Gain gain = new Gain(p);
						gain.addGainItem(item, 1);
						try {
							p.addGainComplete(gain, tx, true);
							tx.commit();
							Packet pt = new Packet(OpCode.ACTIVATIONCODE_SERVER);
							pt.put(serial);
							p.send(pt);
						} catch (Exception e) {
							tx.rollback();
							Server.server.getServiceRegistry()
									.getMailService().sendSystemMail(
											player.id, peony.Messages.STRING_00004, peony.Messages.STRING_00564, "", 0,
											item, 1, "ACTCODE");
						}
					}else{
						Server.server.getServiceRegistry().getMailService()
							.sendSystemMail(player.id, peony.Messages.STRING_00004, peony.Messages.STRING_00564,
								"", 0, item, 1, "ACTCODE");
					}
				} else if (retCode == 1) {
					error(null, peony.Messages.STRING_00565);
				} else if (retCode == 2) {
					error(null, peony.Messages.STRING_00566);
				} else if (retCode == 3) {
					error(null, peony.Messages.STRING_00567);
				} else if (retCode == 4) {
					error(null, peony.Messages.STRING_00568);
				} else if (retCode == 5) {
					error(null, peony.Messages.STRING_00569);
				} else if (retCode == 6) {
				    error(null, peony.Messages.STRING_00570);
				} else if (retCode == 7) {
				    error(null, peony.Messages.STRING_00571);
				}
			}
		} catch (Exception ex) {
			log.error(ex, ex);
			error(null, peony.Messages.STRING_00572);
		} finally {
			method.releaseConnection();
		}
		addToClientSession();
	}

}
