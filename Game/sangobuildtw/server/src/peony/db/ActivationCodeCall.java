package peony.db;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
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

public class ActivationCodeCall extends ClientSessionAsyncCall {

	private static final Logger log = Logger.getLogger(ChinarunCall.class);

	protected int serial;
	protected String activationCode;
	protected Player player;

	protected final static String ACTIVATIONCODE_HTTP = "http://218.206.80.185:8102/card_check";
	
	
	
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
	};


	public ActivationCodeCall(ClientSession session, Packet pt, Player player) {
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
		PostMethod method = new PostMethod(ACTIVATIONCODE_HTTP);
		method.getParams().setContentCharset("utf-8");
		method.addRequestHeader("Connection", "close");
		method.setParameter("cardno", activationCode);// 兑换卡号
		method.setParameter("gamecode", "5");// 游戏代码，5 - 三国
		method.setParameter("cardtype", "-1");// 允许的兑换物品类型
		int accid = ((Account)session.getIdentity()).getId();
		method.setParameter("accountid", String.valueOf(accid));// 帐号ID
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
					int itemstype = Integer.valueOf(br.readLine()).intValue();
					boolean ok = false;
					for(int i=0;i<TYPES2ITEMID.length;i++){
						if(TYPES2ITEMID[i][0]==itemstype){
							ok = true;
							Player p = ObjectAccessor.getPlayer(player.id);
							GameItem item = ObjectAccessor
									.createGameItem(TYPES2ITEMID[i][1]);
							if (p != null) {
								PlayerTransaction tx = p.newTransaction("ACT");
								Gain gain = new Gain(p);
								gain.addGainItem(item, 1);
								try {
									p.addGainComplete(gain, tx, true);
									tx.commit();
									Packet pt = new Packet(
											OpCode.ACTIVATIONCODE_SERVER);
									pt.put(serial);
									p.send(pt);
								} catch (Exception e) {
									tx.rollback();
									Server.server.getServiceRegistry()
											.getMailService().sendSystemMail(
													player.id, "系統", "激活碼獎勵", "", 0,
													item, 1, "ACTCODE");
								}
							}else{
								Server.server.getServiceRegistry().getMailService()
									.sendSystemMail(player.id, "系統", "激活碼獎勵",
										"", 0, item, 1, "ACTCODE");
							}
							break;
						}
					}
				} else if (retCode == 1) {
					error(null, "您輸入的激活碼不正确,請核實后再試.");
				} else if (retCode == 2) {
					error(null, "您輸入的激活碼不是\"明珠三國\"的激活碼,請核實后再試.");
				} else if (retCode == 3) {
					error(null, "您輸入的激活碼在這個兌換員處無法兌換,請找另一個兌換員.");
				} else if (retCode == 4) {
					error(null, "您輸入的激活碼已經兌換過了.");
				} else if (retCode == 5) {
					error(null, "您輸入的激活碼已過期.");
				} else if (retCode == 6) {
				    error(null, "兌換活動已暫停.");
				} else if (retCode == 7) {
				    error(null, "一個帳號只能兌換一次.");
				}
			}
		} catch (Exception ex) {
			error(null, "您的兌換申請提交失敗,請稍后再試.");
		} finally {
			method.releaseConnection();
		}
		addToClientSession();
	}

}
