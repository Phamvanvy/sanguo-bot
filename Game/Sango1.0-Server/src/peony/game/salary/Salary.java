package peony.game.salary;

import java.text.MessageFormat;

import peony.game.GameItem;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.changed.ChangedItem;
import peony.service.ServiceEvent;

abstract class Salary {
	
	public int salaryTypeId;//总类id
	
	public int salaryId;
	
	public String name;
	
	public String dec;//描述
	
	public int salary;//每次完成工资
	
	public String property_record;//本种工资在人物属性池中的每日工资记录 
	
	public static int SALARY_ALREADY_GET = 1;//已经领取
	
	public static int SALARY_NOT_GET = 0;//未领取
	
	public Salary(int salaryTypeId,int salaryId,String name,String dec,int salary){
		this.salaryTypeId = salaryTypeId;
		this.salaryId = salaryId;
		this.name = name;
		this.dec = dec;
		this.salary = salary;
	}
	
	public void playerProperty(String property_record){
		this.property_record = property_record;
	}
	
	public abstract void init(Player p);
	
	public abstract void update(Player p);
	
	/** 初始化未领取状态 */
	public void initRecordSalary(Player p){
		p.pool.setInt(property_record, SALARY_NOT_GET);
		init(p);
	}
	
	/** 记录领取状态 */
	public void recordSalary(Player p){
		p.pool.setInt(property_record, SALARY_ALREADY_GET);
		int totalSalary = p.pool.getInt(SalaryService.PROPERTY_SALARY, 0);
		p.addIntPropertyChangedItem(ChangedItem.SALARY,totalSalary,false,true);
	}

	/** 此种工资今日是否已领取  */
	public boolean hasGetSalary(Player p){
		return p.pool.getInt(property_record, SALARY_NOT_GET) == SALARY_NOT_GET ? false : true;
	}
	
	/** 领取工资 */
	public void receiveSalary(Player p){
		if(p.isReachDayLimit()){
			return;
		}
		if(p.isReachLimitTotal()){
			return;
		}
		if(hasGetSalary(p))
			return;
		int needAddSalary = salary;
		int oldDayValue = p.pool.getInt(SalaryService.PPOPERTY_SALARY_DAY,0);
//		if(oldDayValue+needAddSalary > SalaryService.SALARY_DAYLIMIT){
//			p.pool.setInt(SalaryService.PPOPERTY_SALARY_DAY, SalaryService.SALARY_DAYLIMIT);
//			p.daySalary = SalaryService.SALARY_DAYLIMIT;
//		}else{
//			int newValue = oldDayValue + needAddSalary;
//			p.pool.setInt(SalaryService.PPOPERTY_SALARY_DAY, newValue);
//			p.daySalary = newValue;
//		}
		int oldValue = p.pool.getInt(SalaryService.PROPERTY_SALARY, 0);
		if((p.getPlayerSalary() + needAddSalary) > SalaryService.SALARY_LIMIT){
			needAddSalary = SalaryService.SALARY_LIMIT - p.getPlayerSalary();
			if((p.daySalary+needAddSalary)>SalaryService.SALARY_DAYLIMIT){
				needAddSalary = SalaryService.SALARY_DAYLIMIT - p.daySalary;
			}
//			p.pool.setInt(SalaryService.PROPERTY_SALARY, SalaryService.SALARY_LIMIT);
		}else{
			if((p.daySalary+needAddSalary)>SalaryService.SALARY_DAYLIMIT){
//				p.pool.setInt(SalaryService.PPOPERTY_SALARY_DAY, SalaryService.SALARY_DAYLIMIT);
				needAddSalary = SalaryService.SALARY_DAYLIMIT-p.daySalary;
			}
		}
		int currSar = p.pool.getInt(SalaryService.PROPERTY_SALARY, 0);
		currSar += needAddSalary;
		p.pool.setInt(SalaryService.PROPERTY_SALARY, currSar);
        int tempValue1 = oldDayValue/10;
		int newDayValue = oldDayValue + needAddSalary;
		int tempValue2 = newDayValue/10;
		if(tempValue1<tempValue2 && tempValue1<4){
		    int count = 1;
		    int value = Math.min(tempValue2, 4);
			for(int i=tempValue1;i<value;i++){
				if(!p.topNumOfXuanwushi()){
					int cnt = p.addXuanwuItem(count,0);
					if(cnt <=0)
						break;
					PlayerTransaction tx = p.newTransaction("SALARY");
					GameItem rewardItem = ObjectAccessor.createGameItem(Player.XUANWUSHI_ITEM);
					try {
						p.bag.addGameItemComplete(rewardItem, cnt, tx, true);
						tx.commit();
					} catch (Exception e) {
						tx.rollback();
						Server.server.getServiceRegistry().getMailService()
								.sendSystemMail(p.id, peony.Messages.STRING_00004, "工资奖励的玄武石", MessageFormat.format("恭喜您今天已经获得了{0}点工资，特此奖励玄武石一个,此神物可用来升级灭魂装备哦！", (i+1)*10), 0,rewardItem, cnt, "SALARY");
					}
					p.addNumOfXuanwushi(cnt,0);
					Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id, MessageFormat.format("恭喜您今天已经获得了{0}点工资，特此奖励玄武石一个,此神物可用来升级灭魂装备哦！", (i+1)*10));
				}
			}
		}
		p.pool.setInt(SalaryService.PPOPERTY_SALARY_DAY, newDayValue);
		p.daySalary = newDayValue;
		recordSalary(p);
		if(p.isReachDayLimit()){
			Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id,  MessageFormat.format("工资活动中{0}一项完成，您获得{1}工资,您已达到当日工资获取上限", name,needAddSalary));
		}else{
			Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id,  MessageFormat.format("工资活动中{0}一项完成，您获得{1}工资", name,needAddSalary));
		}
		if(p.isReachLimitTotal()){
			Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id,  "您的总工资已经达到了上限，如果不及时消耗将无法继续获得工资,请尽快去主城工资商人处兑换奖励吧。");
		}
		Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_SALARY_ADD,p));
		LogUtil.logAddSalary(p, oldValue, p.pool.getInt(SalaryService.PROPERTY_SALARY,0), "FINISHSALARY"+name);
		
	}
}
