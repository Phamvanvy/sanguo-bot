package peony.game;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import peony.game.salary.SalaryService;
import peony.service.ServiceEvent;

public class Gain {

	private static final Logger log = Logger.getLogger(Gain.class);
	protected List<GainItem> falls = new ArrayList<GainItem>();
	protected int money;
	protected int exp;
	protected int credit;
	protected int honor;
	protected boolean horse;
	protected int salary;
	
	public int getSalary() {
		return salary;
	}

	public void setSalary(int salary) {
		this.salary = salary;
	}
	
	public void addSalary(int salary){
		if(player.isReachDayLimit()){
			return;
		}
		if(player.isReachLimitTotal()){
			Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id,  "您的总工资已经达到了上限，如果不及时消耗将无法继续获得工资,请尽快去主城工资商人处兑换奖励吧。");
			return;
		}
		int needAddSalary = salary;
		int oldDayValue = player.pool.getInt(SalaryService.PPOPERTY_SALARY_DAY,0);
		if((player.getPlayerSalary() + needAddSalary) > SalaryService.SALARY_LIMIT){
			needAddSalary = SalaryService.SALARY_LIMIT - player.getPlayerSalary();
			if((player.daySalary+needAddSalary)>SalaryService.SALARY_DAYLIMIT){
				needAddSalary = SalaryService.SALARY_DAYLIMIT - player.daySalary;
			}
		}else{
			if((player.daySalary+needAddSalary)>SalaryService.SALARY_DAYLIMIT){
				needAddSalary = SalaryService.SALARY_DAYLIMIT-player.daySalary;
			}
		}
		int newDayValue = oldDayValue + needAddSalary;
		player.pool.setInt(SalaryService.PPOPERTY_SALARY_DAY, newDayValue);
		player.daySalary = newDayValue;
		Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_SALARY_ADD,player));
		this.salary+=needAddSalary;
		Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id,  MessageFormat.format("本次任务中您获得{0}工资", needAddSalary));
	}

	protected Player player;
	protected GameObject source;
	
	protected Gain[] gains;
	
	public Gain(){
		this(null);
	}
	
	public Gain(Player player){
		this(player,null);
	}
	
	public void setGains(Gain[] gains){
		this.gains = gains;
	}
	
	public Gain[] getGains(){
		return gains;
	}
	
	public Gain(Player player,Unit source){
		this(player,source,false);
	}
	
	public Gain(Player player,Unit source,boolean horse){
		this.player = player;
		this.source = source;
		this.horse = horse;
	}
	
	public Player getPlayer(){
		return player;
	}
	
	public GameObject getSource(){
		return source;
	}
	
	public int getHonor() {
		return honor;
	}

	public void addHonor(int honor){
		this.honor += honor;
	}

	public void setHonor(int honor) {
		this.honor = honor;
	}
	
	public int getMoney() {
		return money;
	}

	public void addMoney(int money){
		this.money += money;
	}

	public void setMoney(int money) {
		this.money = money;
	}

	public void addCredit(int credit){
		this.credit += credit;
	}
	
	public int getCredit(){
		return this.credit;
	}
	
	public void setCredit(int credit){
		this.credit = credit;
	}

	public int getExp() {
		return exp;
	}

	public void addExp(int exp){
		this.exp += exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}
	
	public boolean isEmpty(){
		return falls.size()==0;
	}
	
	public void addGainItem(GameItem item,int count){
		addGainItem(new GainItem(item,count));
	}
	
	public void addGainItem(GainItem fallItem){
		if(player!=null && player.isBot())
			return;
		for(GainItem fall:falls){
			if(fall.getItem().equals(fallItem.getItem())){
				fall.add(fallItem.getCount());
				return;
			}
		}
		falls.add(fallItem);
	}
	
	public GainItem[] getGainItems(){
		GainItem[] ret = new GainItem[falls.size()];
		falls.toArray(ret);
		return ret;
	}
	
	/**移除掉落物品*/
	public void removeGainItem(GainItem gainItem){
		if(player!=null && player.isBot())
			return;
		Iterator<GainItem> it = falls.iterator();
		while(it.hasNext()){
			GainItem fall = it.next();
			if(fall.getItem().equals(gainItem.getItem())){
				it.remove();
			}
		}
	}
	
	public void addToPlayer(boolean notify,String cause){
		if(player!=null && player.isBot())
			return;
		PlayerTransaction tx = player.newTransaction(cause);
		player.addGain(this, tx, notify);
		tx.commit();
	}
	
	public boolean completeAddToPlayer(boolean notify,String cause){
		if(player!=null && player.isBot())
			return false;
		PlayerTransaction tx = player.newTransaction(cause);
		try {
			player.addGainComplete(this, tx ,notify);
			tx.commit();
			return true;
		} catch (NoEnoughSpaceException e) {
			tx.rollback();
		}
		return false;

	}
}
