package peony.service.account;

import java.text.MessageFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.Player;
import peony.game.PropertyPool;
import peony.game.Server;
import peony.game.changed.ChangedItem;
import peony.net.ClientSession;

public class ChargeRegularCall extends ClientSessionAsyncCall{
	
	protected static final Logger log = Logger.getLogger(ChargeRegularCall.class);
	private int accountId;
	private int ammount;
	Player player = null;

	public ChargeRegularCall(ClientSession session, int accountId, int ammount) {
		super(session);
		this.accountId = accountId;
		this.ammount = ammount;
		if(session!=null && session.getClient() != null)
		   this.player = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		
		
	}

	public void run() {
		if(ammount>0){
			ChargeActivityService chargeSystemService = Server.server.getServiceRegistry().getChargeActivityService();
			FirstCharge firstCharge = chargeSystemService.getFirstCharge(accountId,true);
			if(firstCharge == null){
				firstCharge = new FirstCharge();
				firstCharge.setAccountId(accountId);
				PropertyPool pool = new PropertyPool();
				firstCharge.setPool(pool);
				firstCharge.pool.setInt(ChargeActivityService.PROPERTY_FIRSTCHARGE_CREATEED, ammount);
				firstCharge.pool.setLong(ChargeActivityService.PROPERTY_FIRSTCHARGE_CREATETIME, System.currentTimeMillis());

				if(player!=null && player.accountId == accountId){
				    Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, "您有充值奖励可领取，快去充值活动界面领取奖励吧。");
					//chargeSystemService.getFirstChargeReward(firstCharge, player, ammount);
				}
				chargeSystemService.addFirstCharge(accountId, firstCharge);
				Server.server.getServiceRegistry().getDbService().firstChargeDao.newEntity(firstCharge);
				log.info("[FIRSTCHARGE]ACC["+accountId+"]IAMMOUNT["+ammount+"]");
				
		    	player.addIntPropertyChangedItem(ChangedItem.MULCHARGE_DAYS,15,false,true);
			}
			if(player!=null && !chargeSystemService.hasGetMulGift(player.accountId)){//baiquan
				log.info("[ACCUMULATECHARGE]ACC["+accountId+"]IAMMOUNT["+ammount+"]");
			    long firstChargeTime = firstCharge.pool.getLong(ChargeActivityService.PROPERTY_FIRSTCHARGE_CREATETIME,0);
			    if(firstChargeTime>0 && System.currentTimeMillis() - firstChargeTime<ChargeActivityService.FIFTEEN_DAY){
					if(player!=null && player.accountId == accountId){
						RecordChargeService service = Server.server.getServiceRegistry().getRecordChargeService();
						synchronized(service){
							Date startTime = new Date();
							startTime.setTime(firstChargeTime);
							Date endTime = new Date();
							endTime.setTime(firstChargeTime+ChargeActivityService.FIFTEEN_DAY);
							int chargeMoney = Server.server.getServiceRegistry().getDbService().chargeDao.getAccumulateCharge(accountId, startTime, new Date());
							int firstChargeAmmount = firstCharge.pool.getInt(ChargeActivityService.PROPERTY_FIRSTCHARGE_CREATEED, 0);
							chargeMoney += firstChargeAmmount;
							//玩家手动领取
							if(chargeSystemService.getMulChargeMax(accountId) > 0){
								Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, "您有充值奖励可领取，快去充值活动界面领取奖励吧。");
							}
							//chargeSystemService.getAccumulateChargeReward(firstCharge, player,chargeMoney);
							firstCharge.pool.setInt(ChargeActivityService.PROPERTY_CHARGE_TOTAL, chargeMoney);
							chargeSystemService.addFirstCharge(accountId, firstCharge);
							Server.server.getServiceRegistry().getDbService().firstChargeDao.updateEntity(firstCharge);
						}
					}
				}
			}
		}
	}
}
