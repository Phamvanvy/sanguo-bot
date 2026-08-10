package peony.game.attendant;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import peony.game.CommonUtil;
import peony.game.ErrorHandler;
import peony.game.LogUtil;
import peony.game.NoEnoughValueException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Skills;
import peony.game.buff.Buff;
import peony.game.buff.BuffUtil;
import peony.game.changed.ChangedItem;
import peony.game.skill.Skill;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;

public class AttendantFixService implements Service{
	
	protected static final Logger log = Logger.getLogger(AttendantFixService.class);
	protected static Map<Integer, List<Config>> configs = new HashMap<Integer, List<Config>>();
	protected Map<Integer, Integer> exchanges = new HashMap<Integer, Integer>();
	
	public static final String PROPERTY_ATTENDANTEXP = "playerattendantexp";
	
	public static int[] qulityBuffs = {611,612,613,613,0,614,615,614,614,614};	

	public void shutdown() {
		
	}

	public void startup() throws Exception {
		try {
			loadConfig();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void loadConfig() throws Exception {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data.findFile("Attendant/attendantfix.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		Element root = doc.getRootElement();
		
		List<Element> exchangeList = root.elements("exchange");
		if (exchangeList != null && exchangeList.size() > 0) {
			for (Element element : exchangeList) {
				int quality = Integer.parseInt(element.attributeValue("quality"));
			    int exchangeValue = Integer.parseInt(element.attributeValue("exchangeexp"));
			    exchanges.put(quality, exchangeValue);
			}
		}
		
		List<Element> list = root.elements("qualityconfig");
		if (list != null && list.size() > 0) {
			for (Element element : list) {
				int quality = Integer.parseInt(element.attributeValue("quality"));
			    List<Element> subEle = element.elements("levelconfig");
			    List<Config> tempList = new ArrayList<Config>();
			    for(Element ele : subEle){
			    	int level = Integer.parseInt(ele.attributeValue("level"));
			    	int exp = Integer.parseInt(ele.attributeValue("exp"));
			    	Config config = new Config(level,exp);
			    	tempList.add(config);
			    }
				configs.put(quality, tempList);
			}
		}
	}
	
	/** 随从转换经验*/
	public void attExchangeExp(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if(player!=null){
			int serial = packet.getInt();
			int instanceId = packet.getInt();
			Attendant oldAttendant = player.attendant;
			if(oldAttendant != null && oldAttendant.getInstanceId() == instanceId){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_CHANGETOEXP_CLIENT, "请先收回随从再转换经验");
				return;
			}
			Attendant attendant = player.attendantBag.getAttendant(instanceId);
			if(attendant!=null){
				if(attendant.hasEquipments()){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_CHANGETOEXP_CLIENT, "该随从身上还有装备,请全部卸下后再进行转换");
					return;
				}
				int exchangeExp = getExchangeExp(attendant.qulity,attendant.attLevel);
				if(exchangeExp>0){
					Attendant removeAtt = player.attendantBag.removeAttendant(instanceId);
					LogUtil.logAttendant(removeAtt, "[ATTENDANTEXCHANGEEXP]");
					if(removeAtt!=null){
						int attExp = player.pool.getInt(PROPERTY_ATTENDANTEXP,0);
						attExp += exchangeExp;
						player.pool.setInt(PROPERTY_ATTENDANTEXP, attExp);
						LogUtil.logAddAttendantExp(player, attExp - exchangeExp, attExp, "EXCHANGEEXP");
						player.addIntPropertyChangedItem(ChangedItem.ATTENDANT_LEVEL,attExp,false,true);
			            Packet pt = new Packet(OpCode.ATTENDANT_CHANGETOEXP_SERVER);
			            pt.putInt(serial);
			            pt.putInt(attExp);
			            player.send(pt);
			            player.message(-1, MessageFormat.format("您的{0}级随从{1}成功转换成{2}随从经验", removeAtt.attLevel,removeAtt.name,exchangeExp), -1, -1);
						LogUtil.logRemoveAttendant(player, attendant.id, instanceId);
					}
				}else{
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_CHANGETOEXP_CLIENT, "该随从无法转换成经验");
				}
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_CHANGETOEXP_CLIENT, peony.Messages.STRING_00099);
			}
		}
	}
	
	
	/** 升级随从 */
	public void attendantUpLevel(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if(player!=null){
			int serial = packet.getInt();
			int instanceId = packet.getInt();
			Attendant attendant = player.attendantBag.getAttendant(instanceId);
			if(attendant != null){
				int upExp = getUpExp(attendant.qulity,attendant.attLevel);
				if(upExp>0 && attendant.attLevel<10){
					int attExp = player.pool.getInt(PROPERTY_ATTENDANTEXP,0);
					if(attExp>=upExp){
						int oldLevel = attendant.attLevel;
						int newLevel = attendant.attLevel+1;
						attendant.attLevel = newLevel;
						if(attendant.attLevel>0){
							int buffId = qulityBuffs[attendant.qulity-1];
							if(buffId!=0){
								Buff buff = BuffUtil.createSuiteBuff(buffId, attendant.attLevel);
								if(buff!=null){
								    attendant.buffs.addBuff(buff);
								}
							}
						}
						attExp -= upExp;
						player.pool.setInt(PROPERTY_ATTENDANTEXP,attExp);
						LogUtil.logDecAddAttendantExp(player,attendant.id, attExp+upExp, attExp,oldLevel,newLevel, "ATTENDANTUPLEVEL");
						int showSkillId = attendant.getShowSkillId();
						Skill showSkill = null;
						for(Skill skill: attendant.specialSkills){
							if(skill.getGroupId() == showSkillId){
								showSkill = skill;
							}
							Skill newSkill = skill.getNextLevel();
							if(newSkill.getRequireLevel()==attendant.attLevel){
								if(newSkill.getGroupId() == showSkillId){
									showSkill = newSkill;
								}
								attendant.refreshSpeicialSkill(skill.getId(),newSkill);
								Server.server.getServiceRegistry().getChatService().sendWorldMessage(MessageFormat.format("{0}将心腹随从提升到{1}级。随从对特殊技能的心得更进一步。", player.name,attendant.attLevel));
							}
						}
						attendant.refreshProperties(false);
						player.addIntPropertyChangedItem(ChangedItem.ATTENDANT_LEVEL,attExp,false,true);
						Packet pt = new Packet(OpCode.ATTENDANT_LEVELUP_SERVER);
			            pt.putInt(serial);
			            pt.putInt(attendant.attLevel);
			            int newUpExp = getUpExp(attendant.qulity,attendant.attLevel);
			            if(newUpExp>0){
			                pt.putInt(newUpExp);
			            }else{
			            	if(attendant.attLevel>=10){
			            		pt.putInt(-1);
			            	}else{
			            	    pt.putInt(0);
			            	}
			            }
			            pt.putInt(attExp);
			            pt.put(showSkill.toClientBytes(player));
			            int exchangeExp = getExchangeExp(attendant.qulity,attendant.attLevel);
			            pt.putInt(exchangeExp);
			            player.send(pt);
			            player.message(-1, MessageFormat.format("您的随从{0}升级成功，等级为{1}级", attendant.name,attendant.attLevel), -1, -1);
					} else {
						ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_LEVELUP_CLIENT, "您当前随从经验值不足，升级失败。");
					}
				}else{
					if(attendant.attLevel>0)
					    ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_LEVELUP_CLIENT, "您的随从已经升到最高级");
					else
						ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_LEVELUP_CLIENT, "该随从不能升级");
				}
			}else {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_LEVELUP_CLIENT, peony.Messages.STRING_00099);
			}
		}
	}
	
	
	
	/** 跟随时给角色添加技能buff*/
	public void addBuffOnFollow(Player player,Attendant attendant){
		removeBuffUnFollow(player);
		boolean refresh = false;
		for(Skill skill:attendant.specialSkills){
			if(skill!=null && skill.getLevel()>0){
				Buff buff = skill.newBuff();
				if(buff!=null){
				     player.buffs.addBuff(buff);
				     refresh = true;
				}
			}
		}
		if(refresh){
			player.refreshProperties(false);
		}
	}
	
	/** 收回随从时移除随从技能buff */
	public void removeBuffUnFollow(Player player){
		boolean refresh = false;
		for(Buff buff : player.buffs.getBuffs()){
			Skill skill = ObjectAccessor.getSkill(buff.getId());
			if(skill!=null && skill.getClazz() == Attendant.SKILLCLASS){
				player.buffs.removeBuff(buff);
				refresh = true;
			}
		}
		if(refresh)
		    player.refreshProperties(false);
	}
	

	public void addSpecialBuff(Attendant attendant){
		if(attendant.attLevel>0){
			int buffId = qulityBuffs[attendant.qulity-1];
			if(buffId!=0){
				if(attendant.buffs.getBuffByID(buffId)==null){
					Buff buff = BuffUtil.createSuiteBuff(buffId, attendant.attLevel);
					if(buff!=null){
					    attendant.buffs.addBuff(buff);
					}
				}
			}
		}
	}
	
	public void playerAddSpecialBuff(Player player){
		if(player.attendant!=null){
		    //修复随从特殊技能问题
			processSpecialBuff(player.attendant);
			if(player.attendant.specialSkills!=null){
				for(Skill skill:player.attendant.specialSkills){
					if(skill!=null && skill.getLevel()>0){
						Buff buff = skill.newBuff();
						if(buff!=null){
							if(player.buffs.getBuffByID(buff.getId())==null){
						       player.buffs.addBuff(buff);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * 修复随从特殊技能问题
	 */
	public void processSpecialBuff(Attendant attendant){
		int showSkillId = attendant.getShowSkillId();
		Skill showSpecialSkill = null;
		if(showSkillId != 0){
			showSpecialSkill = attendant.getSpecialSkill(showSkillId);
		}
		if(showSkillId != 0 && showSpecialSkill==null){
			attendant.specialSkills.clear();
			Skill correctSkill  = ObjectAccessor.getSkill(Skills.getSkillId(showSkillId, Math.round(attendant.attLevel/2)));
			attendant.specialSkills.add(correctSkill);
		}
	}
	
	/**
	 * 随从转换经验值(返还每个等级升级经验的85%)
	 * @param quality
	 * @param level
	 * @return
	 */
	public int getExchangeExp(int quality,int level){
		if(exchanges.containsKey(quality)){
			int value = exchanges.get(quality);
			int value0 = 0;
			for(int i=0;i<level;i++){
				int upExp = getUpExp(quality,i);
				value0 += upExp;
			}
			return value+Math.round(value0*85/100);
		}
		return -1;
	}
	
	/** 随从升级所需经验 */
    public static int getUpExp(int quality,int level){
    	List<Config> config = configs.get(quality);
    	if(config!=null && config.size()>0){
    		for(Config cf : config){
    			if(level+1 == cf.getLevel() && level<10)
    				return cf.getExp();
    		}
    	}
    	return 0;
    }
    
 
    public static boolean showBuff(Buff buff){
    	Skill skill = ObjectAccessor.getSkill(buff.getId());
    	if(skill!=null && skill.getClazz() == Attendant.SKILLCLASS){
			return true;
		}
    	return false;
    }
    
    public static void addAttExp(Player player,int addValue){
    	int attExp = player.pool.getInt(PROPERTY_ATTENDANTEXP,0);
    	int oldValue = attExp;
		attExp += addValue;
		player.pool.setInt(PROPERTY_ATTENDANTEXP, attExp);
		LogUtil.logAddAttendantExp(player, oldValue, attExp, "ADDATTEXP");
		player.addIntPropertyChangedItem(ChangedItem.ATTENDANT_LEVEL,attExp,false,true);
    }
}

class Config{
	public int level;
	public int exp;
	public Config(int level,int exp){
		this.level = level;
		this.exp = exp;
	}
	public int getExp(){
		return exp;
	}
	public int getLevel(){
		return level;
	}
}
