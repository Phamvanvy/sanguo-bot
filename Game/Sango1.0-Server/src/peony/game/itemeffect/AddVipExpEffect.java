package peony.game.itemeffect;

import java.text.MessageFormat;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.LogUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.SetFindPathCall;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.changed.ChangedItem;
import peony.service.VIP.VipPrivilegeService;
import peony.service.account.AccountProperty;
import peony.service.award.AwardService;

public class AddVipExpEffect implements ItemEffect{
	
	protected int vipExp;
	
	public AddVipExpEffect(int vipExp){
		this.vipExp = vipExp;
	}

	public void use(Unit source, GameItem item, Unit target,
			PlayerTransaction tx) throws UseItemException {
			Player p = (Player)source;
			if(p!=null){
				if(vipExp>0){
					if(p.vipLevel>=VipPrivilegeService.UPVIPLEVEL){
						throw new UseItemException("您的VIP已满级，敬请期待VIP更高级别");
					}
					int oldCharge = p.chargeValue;
					int oldLevel = p.vipLevel;
					p.chargeValue += vipExp;
					VipPrivilegeService vipService = Server.server.getServiceRegistry().getVipPrivilegeService();
					int newVipLevel = vipService.getVIPLevel(p.chargeValue);
					AccountProperty ap = vipService.getAccountProperty(p.accountId);
					int ibuyValue = ap.pool.getInt(VipPrivilegeService.PROPERTY_VIP_IBUYVALUE,0);
					ibuyValue += vipExp;
					ap.pool.setInt(VipPrivilegeService.PROPERTY_VIP_IBUYVALUE, ibuyValue);
					String message = MessageFormat.format("成功使用{0}为您增加了{1}点VIP经验。", item.template.name,vipExp);
					Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id, message);
					LogUtil.logVipExpChange(p, oldCharge, p.chargeValue, "USEITEM");
					int playerVipLevel = p.pool.getInt(VipPrivilegeService.PROPERTY_VIP_CHARGELEVEL, 0);
					if(newVipLevel>p.vipLevel){
						if(playerVipLevel<newVipLevel){
						     vipService.processReward(p, playerVipLevel,newVipLevel);
						     p.pool.setInt(VipPrivilegeService.PROPERTY_VIP_CHARGELEVEL, newVipLevel);
						}
						if(p.vipLevel<1 && newVipLevel>=1){
							vipService.processMonthPay(p,true);
						}
						if(p.vipLevel<3 && newVipLevel>=3){
							AwardService service = Server.server.getServiceRegistry().getAwardService();
							service.processCharge(p);
						}
						p.vipLevel = newVipLevel;
						ap.pool.setInt(VipPrivilegeService.PROPERTY_VIP_CHARGELEVEL,newVipLevel);
				    	p.addIntPropertyChangedItem(ChangedItem.VIP_LEVEL,newVipLevel,false,true);
				    	//通告VIP
				    	Server.server.getServiceRegistry().getSyncExecutorService().schedule(
								new SetFindPathCall(p.session, null, p));
				    	Server.server.getServiceRegistry().getVipPrivilegeService().addAccountProperty(p.accountId, ap);
				    	p.message(-1, MessageFormat.format("恭喜您，您的VIP等级提升到{0}级", newVipLevel), -1, -1);
				    	LogUtil.logVipLevelUp(p, oldLevel, newVipLevel, oldCharge, p.chargeValue, "USEITEM");
				}	
			}
		}
	}
	
	/** 批量使用 */
	public void bulkUseItem(Player p,int count) throws Exception{
		if(vipExp>0){
			if(p.vipLevel>=VipPrivilegeService.UPVIPLEVEL){
				throw new Exception("您的VIP已满级，敬请期待VIP更高级别");
			}
			int oldCharge = p.chargeValue;
			int oldLevel = p.vipLevel;
			int addExp = vipExp*count;
			p.chargeValue += addExp;
			VipPrivilegeService vipService = Server.server.getServiceRegistry().getVipPrivilegeService();
			int newVipLevel = vipService.getVIPLevel(p.chargeValue);
			AccountProperty ap = vipService.getAccountProperty(p.accountId);
			int ibuyValue = ap.pool.getInt(VipPrivilegeService.PROPERTY_VIP_IBUYVALUE,0);
			ibuyValue += addExp;
			ap.pool.setInt(VipPrivilegeService.PROPERTY_VIP_IBUYVALUE, ibuyValue);
			String message = MessageFormat.format("恭喜您获得了{0}点VIP经验。", addExp);
			Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id, message);
			LogUtil.logVipExpChange(p, oldCharge, p.chargeValue, "USEITEM");
			int playerVipLevel = p.pool.getInt(VipPrivilegeService.PROPERTY_VIP_CHARGELEVEL, 0);
			if(newVipLevel>p.vipLevel){
				if(playerVipLevel<newVipLevel){
				     vipService.processReward(p, playerVipLevel,newVipLevel);
				     p.pool.setInt(VipPrivilegeService.PROPERTY_VIP_CHARGELEVEL, newVipLevel);
				}
				if(p.vipLevel<1 && newVipLevel>=1){
					vipService.processMonthPay(p,true);
				}
				if(p.vipLevel<3 && newVipLevel>=3){
					AwardService service = Server.server.getServiceRegistry().getAwardService();
					service.processCharge(p);
				}
				p.vipLevel = newVipLevel;
				ap.pool.setInt(VipPrivilegeService.PROPERTY_VIP_CHARGELEVEL,newVipLevel);
		    	p.addIntPropertyChangedItem(ChangedItem.VIP_LEVEL,newVipLevel,false,true);
		    	//通告VIP
		    	Server.server.getServiceRegistry().getSyncExecutorService().schedule(
						new SetFindPathCall(p.session, null, p));
		    	Server.server.getServiceRegistry().getVipPrivilegeService().addAccountProperty(p.accountId, ap);
		    	p.message(-1, MessageFormat.format("恭喜您，您的VIP等级提升到{0}级", newVipLevel), -1, -1);
		    	LogUtil.logVipLevelUp(p, oldLevel, newVipLevel, oldCharge, p.chargeValue, "USEITEM");
			}
		}	
	}
	
	public boolean isAsync() {
		return false;
	}

	public boolean needRemove() {
		return false;
	}

}
