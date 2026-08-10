package peony.game.nation;

import java.text.MessageFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Type;

import peony.game.GameObject;
import peony.game.PropertyPool;
import peony.game.Server;
import peony.game.Time;
import peony.game.buff.NationBuff;
import peony.game.convoy.NationConvoyService;
import peony.game.mail.MailService;

@Entity
@Table(name = "nation")
@AccessType("field")
public class Nation {
	
	private static final Logger log = Logger.getLogger(Nation.class);
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public int id;
	
	@Column(name="faction",nullable=false,length=11)
	public int faction;
	
	@Column(name="money",nullable=false)
	public long money;
	
	@Column(name="power",nullable=false,length=11)
	public int power;
	
	@Column(name="slogan")
	public String slogan;
	
	@Column(name = "pool")
	@Type(type = "peony.game.PropertyPoolType")
	public PropertyPool pool = new PropertyPool();
	
	@Column(name = "skills")
	@Type(type = "peony.game.nation.NationSkillsUserType")
//	@Transient
	public NationSkills skills;
	
	@Transient
	protected Officer[] officers = new Officer[Officer.LEVEL_COUNT];
	
	@Transient
	public NationBuff buff;
	
	@Column(name = "guardtime")
	public Date guardTime; //受保护的截止时间
	
	@Column(name = "tax")
	public float taxRate = 0.05f;
	
	
	public static final String PROPERTY_FAIL_WEI = "FAIL1";
	public static final String PROPERTY_FAIL_SHU = "FAIL2";
	public static final String PROPERTY_FAIL_WU = "FAIL3";
	public static final String PROPERTY_FAIL_PREFIX = "FAIL";
	
	public static final String PROPERTY_WIN_WEI = "WIN1";
	public static final String PROPERTY_WIN_SHU = "WIN2";
	public static final String PROPERTY_WIN_WU = "WIN3";
	public static final String PROPERTY_WIN_PREFIX = "WIN";
	
	public static final String PROPERTY_WEIKING_NOTONLINE = "1";
	public static final String PROPERTY_SHUKING_NOTONLINE = "2";
	public static final String PROPERTY_WUKING_NOTONLINE = "3";
	
//	public static final String PROPERTY_BATTLE = "BATTLE"; //国战开始的时候此值加1，国战结束此值减一
	public static final String PROPERTY_BATTLE_ATTACK = "BATTLEA";  //国战开始如果是进攻方那么加一，国战结束后减一
	public static final String PROPERTY_BATTLE_DEFENSE = "BATTLED"; //国战开始如果是防守方那么加一，国战结束后减一
	public static final String PROPERTY_ATTACK_VALUE = "ATTACKV"; //国战进攻值，在国战中进攻方用
	public static final String PROPERTY_DEFENSE_VALUE = "DEFENSEV"; //国战防守值，在国战防守中用
	
	public static final String PROPERTY_SNEAK_ATTACK = "SNEAKA"; //反击战开始如果进攻那么加一，反击战结束以后减一
	public static final String PROPERTY_SNEAK_DEFENSE = "SNEAKD"; //反击战开始如果防守那么加一，反击战结束以后减一
	
	public static final String PROPERTY_FACTION_QUEST = "QUEST"; //
	
	public static final String PROPERTY_TAX_DAY = "TAXDAY"; //调整税率的时间,每天只能调整一次
	public static final String PROPERTY_NATION_CONVOY = "NCONVOY"; //国家押镖，每天一次
	public static final String PROPERTY_KINGLOGIN_TIME = "KINGLOGINTIME"; //国公上线提示
	public static final String PROPERTY_KINGLOGOUT_TIME = "KINGLOGOUTTIME"; //国公下线提示
	public static final String PROPERTY_NATIONCONVOY_STATE = "NATIONCONVOYSTATE";
	public static final String PROPERTY_NATIONCONVOY_DATE = "NATIONCONVOYDATE";
	public static final String PROPERTY_NATIONCONVOY_MODIFYTIME = "NATIONCONVOYMODIFYTIME";
	
	public static final String PROPERTY_SECRETLETTER_COUNT = "LETTERCOUNT";//记录各国君主手中拥有的密信的数目
	
	public static final String PROPERTY_INDEX = "INDEX";//记录每个国家密信的序号
	
	public static final String DELETE_KING_ITEM = "DeleteKingItem";//需要在上一届国公上线是删除国公专属物品
	
	public static final String PROPERTY_GET_KINGBUFF = "famegetkingbuff";
		
	public static int QUEST_MONEY = 2000000;
	
	public static int QUEST_MIN_MONEY = 1000000;
	
	public static float FAILURE_TAX = 0.05f; //战败国被征收的税率
	
	public static final String PROPERY_LACK_MONEY = "lackmoney";//国库缺乏资金
	
	public void addOfficer(Officer officer){
		officers[officer.level] = officer;
	}
	
	public void clearOfficers(){
		for(int i=0;i<officers.length;i++){
			officers[i] = null;
		}
	}
	
	public Officer getOfficer(int level){
		if(level>4){
			return null;
		}
		return officers[level];
	}
	
	public Officer getOfficerByPlayerId(int id){
		for(int i=0;i<officers.length;i++){
			if(officers[i]!=null&&officers[i].id==id){
				return officers[i];
			}
		}
		return null;
	}
	
	public int getFailTimes(){
		return pool.getInt(PROPERTY_FAIL_WEI,0) + pool.getInt(PROPERTY_FAIL_SHU,0) + pool.getInt(PROPERTY_FAIL_WU,0);
	}
	
	public int getWinTimes(){
		return pool.getInt(PROPERTY_WIN_WEI,0) + pool.getInt(PROPERTY_WIN_SHU,0) + pool.getInt(PROPERTY_WIN_WU,0);
	}
	
	public String getName(){
		return GameObject.FACTION_NAME[faction];
	}
	
	public String getKingName(){
		if(officers[0]==null)
			throw new IllegalStateException();
		else{
			return officers[0].actor.name;
		}
	}
	
	public int getKingId(){
		if(officers[0]==null)
			return -1;
		return officers[0].id;
	}
	
	
	public synchronized long addMoney(int money){
		if(money<0)
			throw new IllegalArgumentException();
		long oldValue = this.money;
		this.money += money;
		log.info("[NATIONADDMONEY]FACTION["+faction+"]VALUE["+money+"]OLD["+oldValue+"]NEW["+this.money+"]");
		return this.money;
	}
	
	public synchronized long decMoney(int  money){
		if(money<0)
			throw new IllegalArgumentException();
		long oldValue = this.money;
		long v = this.money - money;
		this.money = Math.max(0, v);
		notifyKing();//国库资金不足时国家押运自动关闭通知
		log.info("[NATIONDECMONEY]FACTION["+faction+"]VALUE["+money+"]OLD["+oldValue+"]NEW["+this.money+"]");
		return this.money;
	}
	
	public void notifyKing(){
		try{
			if(this.getKingId()!=-1 && this.money<NationConvoyService.DEPOSITEGET && this.pool.getInt(PROPERTY_NATIONCONVOY_STATE,0)!=0){
				Server.server.getServiceRegistry().getMailService().sendSystemMailAsync(this.getKingId(), "系统", "国家押运关闭", "国库资金不足200万,无法开启下一次国家押运,请及时筹备国库资金,国库资金不足时国家押运功能将自动关闭,请注意在资金充足时及时开启.", 0, 
						null, 0, "NATIONCONVOY");
				this.pool.remove(PROPERTY_NATIONCONVOY_STATE);
				this.pool.remove(PROPERTY_NATIONCONVOY_DATE);
			}
		}catch(Exception e){
			
		}
	}
	
	public Officer[] getOfficers(){
		return this.officers;
	}
	
	public void update(){
		if(guardTime!=null){
			if(Time.currDate.after(guardTime)){
				guardTime = null;
			}
		}
		
		for (NationSkill skill : skills.skills.values()) {
			if (skill.level >0 && skill.maintainDay != Time.day) {
				pool.setInt(PROPERY_LACK_MONEY,0);
				// 每日需要扣除维护费，如果维护费用不够，那么降级
				synchronized (this) {
					maintain(skill);
				}
			}
		}
		
		if(pool.getInt(PROPERY_LACK_MONEY,0)==0){
			long nationMoney = this.money;
			int count=0;
			for (NationSkill skill : skills.skills.values()) {     //该日维护后，国库里的金钱不够第二天的维护费用，发飞鸽通知国公
				if(skill.level>0) {
					if(nationMoney<skill.getMaintainMoney(skill.level)){
					Nation nation = Server.server.getServiceRegistry()
						.getNationService().getNationByFaction(faction);
						MailService mailService = Server.server.getServiceRegistry().getMailService();
						String msg = peony.Messages.STRING_01144;
						mailService.sendSystemMail(nation.getKingId(),peony.Messages.STRING_00004,peony.Messages.STRING_00087,msg, 0, null, 0,"NATIONLACKMONEY");
						pool.setInt(PROPERY_LACK_MONEY,Time.day);
						break;
					} else {
						nationMoney -= skill.getMaintainMoney(skill.level);
					}
				}
				count++;
			}
			if(count == skills.skills.size() && pool.getInt(PROPERY_LACK_MONEY, 0)==0){
				pool.setInt(PROPERY_LACK_MONEY,Time.day);
			}
		}
	}
	
	public void maintainAll(){
		for (NationSkill skill : skills.skills.values()) {
			if(skill.level>0)
				maintain(skill);
		}
	}
	
	protected void maintain(NationSkill skill) {
		if (this.money >= skill.getMaintainMoney(skill.level)) {
			decMoney(skill.getMaintainMoney(skill.level));
			skill.maintainDay = Time.day;
		} else {
			skill.level -= 1;
			skill.maintainDay = Time.day;
			Server.server.getServiceRegistry().getChatService()
					.sendFactionSystemMessage(faction,
							MessageFormat.format(peony.Messages.STRING_01145, skill.name,skill.level));
		}
	}
	
	public void setStudyTimes(int day,int t){
		pool.setInt("STUDY"+day, t);
	}
	public int getStudyTimes(int day){
		return pool.getInt("STUDY"+day,0);
	}

//	/**
//	 * 国家技能列表
//	 */
//	public void sendNationSkillList() {
//		Packet pt = new Packet(OpCode.NATION_SKILLLIST_SERVER);
//		pt.put(skills.toClientBytes());
//		send(pt);
//	}
}
