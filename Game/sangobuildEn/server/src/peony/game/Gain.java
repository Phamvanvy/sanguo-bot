package peony.game;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class Gain {

	private static final Logger log = Logger.getLogger(Gain.class);
	protected List<GainItem> falls = new ArrayList<GainItem>();
	protected int money;
	protected int exp;
	protected int credit;
	protected int honor;
	protected boolean horse;
	
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
	
	public void addToPlayer(boolean notify,String cause){
		PlayerTransaction tx = player.newTransaction(cause);
		player.addGain(this, tx, notify);
		tx.commit();
	}
	
	public boolean completeAddToPlayer(boolean notify,String cause){
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
