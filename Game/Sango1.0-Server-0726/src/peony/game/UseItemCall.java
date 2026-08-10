package peony.game;

import java.util.List;

import peony.common.ClientSessionAsyncCall;
import peony.db.DBService;
import peony.game.itemeffect.AddItemEffect;
import peony.net.ClientSession;

public class UseItemCall extends ClientSessionAsyncCall {

	protected GameObjectRef ref;
	protected ItemUse itemUse;

	public UseItemCall(ClientSession session, GameObjectRef ref, ItemUse itemUse) {
		super(session);
		this.ref = ref;
		this.itemUse = itemUse;
	}

	public void callFinish() throws Exception {

	}

	public void run() {
		Player p = ObjectAccessor.getPlayer(ref.id);
		if (p != null) {
			PlayerTransaction tx = p.newTransaction("USE");
			TransactionBagGrid grid = p.bag.removeGridGameItem(itemUse.gridId,
					itemUse.itemId, itemUse.instanceId, 1, tx, true);
			if (grid == null || grid.item == null
					|| grid.item.template.id != itemUse.itemId) {
				p.sendUseItemFail(itemUse.itemId, "没找到物品");
				tx.rollback();
				return;
			}
			if (grid.item.template.useType == null
					|| grid.item.template.useType.effect == null) {
				p.sendUseItemFail(itemUse.itemId, "物品不能使用");
				tx.rollback();
				return;
			}
			if (grid.item.template.useType.occasion == UseType.OCCASION_BATTLE) {
				if (p.getThreatCount() == 0) {
					p.sendUseItemFail(itemUse.itemId, "只能在战斗中使用");
					tx.rollback();
					return;
				}
			} else if (grid.item.template.useType.occasion == UseType.OCCASION_NOBATTLE) {
				if (p.getThreatCount() > 0) {
					p.sendUseItemFail(itemUse.itemId, "不能在战斗中使用");
					tx.rollback();
					return;
				}
			}
			GameObject target = null;
			if (itemUse.target != null) {
				target = ObjectAccessor.getGameObject(itemUse.target);
				if (target == null) {
					p.sendUseItemFail(itemUse.itemId, "无效目标");
					tx.rollback();
					return;
				} else {
					if (!target.isAlive()) {
						p.sendUseItemFail(itemUse.itemId, "无效目标");
						tx.rollback();
						return;
					}
					GameItem item = grid.item;
					ItemEffect effect = item.template.useType.effect;
					PlayerTransaction tx2 = p.newTransaction("ITE");
					try {
						effect.use(p, item, (Unit) target, tx2);
						tx2.commit();
						if (grid.item.template.useType.consume) {
							tx.commit();
						} else {
							tx.rollback();
						}
						p.setCoolDown(item.template.useType.coolDownId,
								Time.currTime, Time.currTime
										+ item.template.useType.coolDownTime);

						// 使用成功
						p.lastItemId = itemUse.itemId;
						
						// 发送通知
						List<GameItem> nitems = tx2.getNoticeItems();
						if (nitems != null) {
							AddItemEffect.sendItemNotice(nitems, p, item.template.name);
						}
						
						// 需要邮件发送的物品这里发送
						List<GainItem> mitems = tx2.getMailItems();
						if (mitems != null) {
							DBService dbs = Server.server.getServiceRegistry().getDbService();
					        p.message(-1, "您背包已满，获得的物品已经通过邮件发送了，请查收。", -1, -1);
					        for (GainItem gitem : mitems) {
					            GameItem addItem = gitem.getItem();
					            String itemTitle = addItem.template.name;
					            if (gitem.getCount() > 1) {
					                itemTitle += "x" + gitem.getCount();
					            }
					            Server.server.getServiceRegistry().getMailService().sendSystemMailAsync(p.id, "系统", itemTitle, "", 0,
					            		gitem.getItem(), gitem.getCount(), "ITE");
					        }
						}
						
						// 记录日志
						LogUtil.logUseItem(p, item, item.template.useType.consume);
					} catch (UseItemException e) {
						tx2.rollback();
						tx.rollback();
						p.sendUseItemFail(itemUse.itemId, e.getMessage());
						return;
					}
				}
			}
		}
	}

}
