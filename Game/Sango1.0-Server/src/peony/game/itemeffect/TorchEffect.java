package peony.game.itemeffect;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Random;
import peony.game.Gain;
import peony.game.GainItem;
import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.drop.GroupDrop;
import peony.game.mail.MailService;
import peony.service.TorchService;
import peony.util.ProvinceUtil;

public class TorchEffect implements ItemEffect {
	protected int type;// CMCC

	int[] groupIDs = { -1, 758 };
	Random random = new Random();
	public static int itemId = 1115; // 必得物品ID

	Date date1 = new Date();
	String str = DateFormat.getDateTimeInstance().format(date1);

	public TorchEffect(int type) {
		this.type = type;
	}

	public void use(Unit source, GameItem item, Unit target,
			PlayerTransaction tx) throws UseItemException {
		if (!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException(peony.Messages.STRING_00014);
		if (!(target instanceof Player))
			throw new UseItemException(peony.Messages.STRING_00014);
		if (Server.server.REVISION_TYPE_CMCC.equals(Server.server.revision)) {
			Player p = (Player) source;
			if (p != null) {
				int groupID = 758;
				MailService mailService = Server.server.getServiceRegistry()
				.getMailService();
				GameItem givenItem = ObjectAccessor.createGameItem(itemId);
				TorchService torchService = Server.server.getServiceRegistry()
						.getTorchService();
				String cityName = p.getAccount().getCity();
				String msg = peony.Messages.STRING_00801;
				if (cityName != null) {
//					String province = ProvinceUtil.getProvinceByCity(cityName);
//					if (province.equals("福建")) {
//						String userId = p.getAccount().getCmccUserId();
//						groupID = groupIDs[random.nextInt(groupIDs.length)];
//						if (groupID == -1) {
//							if (torchService.money != null
//									&& torchService.money.size() != 0
//									&& torchService.money.containsKey(userId)) {
//								if (torchService.money.get(userId) < 5
//										&& torchService.getSum() < 40000
//										&& torchService.addCount.get(userId) < 3) {
//									int moneyHad = torchService.money
//											.get(userId);
//									int addMoney = 0;
//									int addcnt = torchService.addCount
//											.get(userId);
//									if (5 - moneyHad >= 3) {
//										addMoney = 3;
//									} else {
//										addMoney = 1;
//									}
//									int totalMoney = moneyHad + addMoney;
//									Server.server.getServiceRegistry()
//											.getChatService()
//											.sendPrivateMessage(p.id, msg);
//									mailService.sendSystemMail(p.id, "系统",
//											"系统提示", getMsg(str, addMoney), 0,
//											null, 0, "CMCCSENDMONEY");
//									torchService.money.put(userId, totalMoney);
//									torchService.addCount.put(userId,
//											addcnt + 1);
//									LogUtil.logSendMoney(p, addMoney,
//											totalMoney); // 赠送话费日志
//
//								} else {
//									groupID = 758;
//								}
//							} else {
//								// 第一次获得话费
//								int[] giveMoney = { 1, 3, 3, 5 };
//								int index = random.nextInt(giveMoney.length);
//								int moneyAdd = giveMoney[index];
//								int totalMoney = moneyAdd;
//								Server.server.getServiceRegistry()
//										.getChatService().sendPrivateMessage(
//												p.id, msg);
//								mailService.sendSystemMail(p.id, "系统", "系统提示",
//										getMsg(str, moneyAdd), 0, null, 0,
//										"CMCCSENDMONEY");
//								torchService.money.put(userId, totalMoney);
//								torchService.addCount.put(userId, 1);
//								LogUtil.logSendMoney(p, moneyAdd, totalMoney);// 赠送话费日志
//							}
//						}
//					} else {
						groupID = 758;
//					}
					if (groupID == 758) {
						Gain gain = new Gain(p);
						GroupDrop gd = ObjectAccessor.getGroupDrop(groupID);
						gd.calc(random, gain);
						tx.setCause("ITE");
						// 把物品一件件加入背包
						for (GainItem gi : gain.getGainItems()) {
							mailService
									.sendSystemMail(
											p.id,
											peony.Messages.STRING_00004,
											peony.Messages.STRING_00087,
											MessageFormat
													.format(
															peony.Messages.STRING_00802,
															str), 0, gi
													.getItem(), gi.getCount(),
											"YAYUNACTIVITY");
						}
						// 添加金钱、经验、战功、声望
						if (gain.getMoney() > 0) {
							p.addMoney(gain.getMoney(), tx, true);
						}
						if (gain.getExp() > 0 && p.level < 30) {
							p.addExp(gain.getExp(), tx, true);
						}
						if (gain.getCredit() > 0) {
							p.addCredit(gain.getCredit(), tx, true);
						}
						if (gain.getHonor() > 0) {
							p.addHonor(gain.getHonor(), tx, true);
						}
						if(gain.getSalary() > 0){
		                	p.addSalary(gain.getSalary(), tx, true);
		                }
						mailService.sendSystemMail(p.id,peony.Messages.STRING_00004,peony.Messages.STRING_00087,MessageFormat.format(
												peony.Messages.STRING_00802,
												str), 0, givenItem, 1,"YAYUNACTIVITY");
					}
				}
		}

	}

}

	public String getMsg(String str, int m) {
		String msg = MessageFormat
				.format(
						peony.Messages.STRING_00803,
						str, m);
		return msg;
	}

	public boolean isAsync() {
		return false;
	}

	public boolean needRemove() {
		return false;
	}
}
