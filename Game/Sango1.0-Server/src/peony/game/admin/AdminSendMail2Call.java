package peony.game.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.pip.sanguo.data.HorseType;
import com.pip.util.Utils;
import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.GameItemObject;
import peony.game.Horse;
import peony.game.ItemTemplate;
import peony.game.ItemUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Skills;
import peony.game.attendant.Attendant;
import peony.game.itemeffect.HorseItemEffect;
import peony.game.itemenhance.ItemEnhance;
import peony.game.itemenhance.NaturalEnhance;
import peony.game.skill.Skill;
import peony.net.ClientSession;
import peony.net.Packet;

public class AdminSendMail2Call extends ClientSessionAsyncCall {

	int serial;
	int playerId;
	String title,content;
	String itemStringDesc;

	public AdminSendMail2Call(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.playerId = packet.getInt();
		this.title = packet.getString();
		this.content = packet.getString();
		this.itemStringDesc = packet.getString();
	}

	public void callFinish() throws Exception {

	}

	public void run() {
		try {
			if(itemStringDesc.startsWith("EQU(")){
				Actor actor = Server.server.getServiceRegistry()
				.getActorCacheService().find(playerId);
			    if (actor != null) {
					EquDesc equDesc = parseEquLog(itemStringDesc);
					if(equDesc != null){
					    GameItem item = getEqu(equDesc);
					    if(item != null){
							ItemEnhance ie = (ItemEnhance)item.object;
							if(ie != null){
								if(item.template.equipment.initHole>=item.template.equipment.maxHole)
								ie.addHole = 0;
							}
							Server.server.getServiceRegistry().getMailService()
							.sendSystemMail(playerId, peony.Messages.STRING_00004, title, content,
									0, item, 1, "GM");
							Packet pt = new Packet(OpCode.ADMIN_SENDMAIL2_SERVER);
							pt.putInt(serial);
							pt.putString(item.template.name);
							session.send(pt);
					    }
					}
			   } else {
					ErrorHandler.sendErrorMessage(session, serial, 
							OpCode.ADMIN_SENDMAIL2_CLIENT, peony.Messages.STRING_00952);
			   }
			} else if(itemStringDesc.startsWith("HORSE[")){
				HosDesc hosDesc = parseHosLog(itemStringDesc);
				Horse h = null;
					h = getHorse(hosDesc);
				GameItem horseItem = ObjectAccessor.createGameItem(h.itemId);
				horseItem.object = h;
				Server.server.getServiceRegistry().getMailService().sendSystemMail(playerId, peony.Messages.STRING_00004, title, content, 0, horseItem, 1, "GM");
				Packet pt = new Packet(OpCode.ADMIN_SENDMAIL2_SERVER);
				pt.putInt(serial);
				pt.putString(horseItem.template.name);
				session.send(pt);
			}else if(itemStringDesc.startsWith("[THROWATTENDANT]")){
				AttendantDesc atdDec = parseAttendantLog(itemStringDesc);
				Attendant atd = getAttendant(atdDec);
				GameItem attendantItem = ObjectAccessor.createGameItem(Attendant.ATTENDANT_REISSUE_ID);
				attendantItem.object = (GameItemObject) atd;
				Server.server.getServiceRegistry().getMailService().sendSystemMail(playerId, peony.Messages.STRING_00004, title, content, 0, attendantItem, 1, "GM");
				Packet pt = new Packet(OpCode.ADMIN_SENDMAIL2_SERVER);
				pt.putInt(serial);
				pt.putString(attendantItem.template.name);
				session.send(pt);
				
			}else {
				ErrorHandler.sendErrorMessage(session, serial, 
						OpCode.ADMIN_SENDMAIL2_CLIENT, peony.Messages.STRING_00953);
			}
		} catch (Exception ex) {
			ErrorHandler.sendAdminErrorMessage(session, serial,
					OpCode.ADMIN_SENDMAIL2_CLIENT, ex.getMessage());
		}
	}
	
	/**
	 * 获得坐骑
	 * @param hosDesc
	 * @return 坐骑
	 * @throws Exception
	 */
	public Horse getHorse(HosDesc hosDesc) throws Exception{
		Player p = ObjectAccessor.getPlayer(playerId);
		if(p == null){
			p = Server.server.getServiceRegistry().getDbService().playerDAO.getPlayerById(playerId);
		}
		if(p!=null) {
			int horseTypeId = hosDesc.typeId;
			HorseType horseType = ObjectAccessor
					.getHorseType(horseTypeId);
			ItemTemplate it = ObjectAccessor.getHorseTemplate(horseTypeId,hosDesc.itemId);
			if(horseType != null && it != null){
				HorseItemEffect effect = (HorseItemEffect)it.useType.effect;
//				Field imageField = HorseItemEffect.class.getDeclaredField("imageId");
//				int imageId = 0;
//				if(imageField != null){
//					imageField.setAccessible(true);
//					imageId = imageField.getInt(effect);
//				} else {
//					throw new Exception("找不到坐骑图像");
//				}
				Horse h = new Horse(horseType,hosDesc.instanceId);
				h.name = hosDesc.showName;
				h.level = hosDesc.level;
				h.itemId = hosDesc.itemId;
				h.imageId = hosDesc.imageId;
				h.imageIdChange = hosDesc.imageChangeId;
				h.iconImage = it.showImage;
				h.iconId = it.showType;
				h.fixCount = hosDesc.fixCount;
				h.itemIdChange = h.itemId;
				for(int i=0;i<hosDesc.skillIds.size();i++){
					int level = 1;
					if(hosDesc.skillLevel!=null && hosDesc.skillLevel.containsKey(hosDesc.skillIds.get(i))){
						level = hosDesc.skillLevel.get(hosDesc.skillIds.get(i));
					}
					h.addSkill(ObjectAccessor.getSkill(Skills.getSkillId(hosDesc.skillIds.get(i)>>16, level)),p);
				}
				if (hosDesc.equ != null && hosDesc.equ.size() > 0) {
					List<EquDesc> equDesc = hosDesc.equ;
					for (int i = 0; i < equDesc.size(); i++) {
						EquDesc equList = equDesc.get(i);
						GameItem item = getEqu(equList);
						h.equs.equs[i] = item;
					}
				}
				return h;
			} else {
				throw new Exception(peony.Messages.STRING_00954);
			}
		} else {
			throw new Exception(peony.Messages.STRING_00952);
		}
	}
	
	/**
	 * 获得装备
	 * @param equDesc
	 * @return 装备
	 * @throws Exception
	 */
	public GameItem getEqu(EquDesc equDesc) throws Exception{
		ItemTemplate template = ObjectAccessor
				.getItemTemplate(equDesc.itemId);
		if (template != null) {
			GameItem item = ObjectAccessor.createGameItem(template,
					equDesc.instanceId);
			if (equDesc.hole > 0 || equDesc.jewels != null
					|| equDesc.naturals != null || equDesc.star > 0
					|| equDesc.maxHole > 0 || equDesc.jewelUpgrades!=null) {
				ItemEnhance enhance = new ItemEnhance();
				enhance.setAddHole(equDesc.hole);
				enhance.setAddMaxHole(equDesc.maxHole);
				enhance.setStar(equDesc.star);
				if (equDesc.jewels != null) {
					for (int i = 0; i < equDesc.jewels.length; i++) {
						enhance.addJewel(i, equDesc.jewels[i]);
					}
				}
				if(equDesc.jewelUpgrades!=null){
					enhance.jewelUpgrades = equDesc.jewelUpgrades;
				}
				if (equDesc.naturals != null) {
					NaturalEnhance[] naturals = new NaturalEnhance[equDesc.naturals.length / 2];
					for (int i = 0; i < equDesc.naturals.length; i += 2) {
						naturals[i / 2] = ItemUtil
								.createNaturalEnhance(item,
										equDesc.naturals[i],
										equDesc.naturals[i + 1]);
					}
					enhance.setNaturals(naturals);
				}
				if(equDesc.markString!=null && !equDesc.markString.equals("")){
					enhance.markString = equDesc.markString;
				}
				item.object = enhance;
			}
			item.bindInstance = equDesc.bindInstance;
			return item;
		} else {
			throw new Exception(peony.Messages.STRING_00955);
		}
	}
  
	/**
	 * 解析装备日志
	 * @param s
	 * @return
	 * @throws ParseLogException
	 */
	// EQU(1006605,9011905,S=6,H=3,JEW=1396+1368+1431+1375,,JEWUPGRADE=0+2+3+0+0+0,NR=4/31+10/11)
	public static EquDesc parseEquLog(String s) throws ParseLogException {
		if (!s.startsWith("EQU(")) {
			throw new ParseLogException(peony.Messages.STRING_00956);
		}
		EquDesc desc = new EquDesc();
		if(s.contains("EQU(")){
			int startIndex = s.lastIndexOf("EQU(");
			int charLength = "EQU(".length();
			String temString = s.substring(startIndex + charLength, s.length());
			int endIndex = temString.indexOf(")");
			String teString = temString.substring(0, endIndex);
			String[] ss = teString.split(",");
			int itemId = Integer.parseInt(ss[0]);
			int instanceId = Server.server.getServiceRegistry().getSleepyCatService().generatorItemId();
//			int instanceId = Integer.parseInt(ss[1]);
			desc.itemId = itemId;
			desc.instanceId = instanceId;
			if (ss.length > 2) {
				for (int i = 2; i < ss.length; i++) {
					String se = ss[i];
					if (se.startsWith("S=")) {
						desc.star = Integer.parseInt(se.substring(2));
					} else if (se.startsWith("H=")) {
						desc.hole = Integer.parseInt(se.substring(2));
					} else if (se.startsWith("MH=")) {
						desc.maxHole = Integer.parseInt(se.substring(3));
					} else if (se.startsWith("JEW=")) {
						String[] jewStrings = se.substring(4).split("\\+");
						desc.jewels = new int[jewStrings.length];
						for (int j = 0; j < jewStrings.length; j++) {
							desc.jewels[j] = Integer.parseInt(jewStrings[j]);
						}
					} else if (se.startsWith("NR=")) {
						String[] naturalsString = se.substring(3).split("\\+");
						desc.naturals = new int[naturalsString.length * 2];
						for (int j = 0; j < naturalsString.length; j++) {
							String[] nString = naturalsString[j].split("/");
							desc.naturals[2 * j] = Integer.parseInt(nString[0]);
							desc.naturals[2 * j + 1] = Integer.parseInt(nString[1]);
						}
					} else if(se.startsWith("MK=")){
						desc.markString = se.substring(3);
					}else if(se.startsWith("JEWUPGRADE=")){
						String jewelUpdates = se.substring(11);
						String[] str = jewelUpdates.split("\\+");
						desc.jewelUpgrades = new int[str.length * 2];
						for(int j=0;j<str.length * 2;j+=2){
							desc.jewelUpgrades[j] = j/2;
							desc.jewelUpgrades[j+1] = Integer.parseInt(str[j/2]);
						}
					}
				}
			}
		}
		if(s.contains("BINDINSTANCE(")) {
			int startIndex = s.lastIndexOf("BINDINSTANCE(");
			int charLength = "BINDINSTANCE(".length();
			String tString = s
					.substring(startIndex + charLength, s.length());
			int endIndex = tString.indexOf(")");
			int bindInstance = Integer.parseInt(tString.substring(0, endIndex));
			desc.bindInstance = bindInstance;
			
		}
		return desc;
	}
	
	/**
	 * 解析坐骑日志
	 * @param s
	 * @return
	 * @throws ParseLogException
	 */
	// HORSE[HOS(TID=383,HID=2570,NM=闪电狼 谋士 ,IID=97400057,LVL=65,FIX=3,IMAG=26,IMAGCHANGE=-1,SKLS=7208962+6684674+4915202+7012354+6750210,SKLLEVEL=2+2+2+2+2)]CAUSE[DEL]EQUS[]
	public static HosDesc parseHosLog(String s) throws ParseLogException {
		if (!s.startsWith("HORSE[")) {
			throw new ParseLogException(peony.Messages.STRING_00957);
		}
		HosDesc hosDesc = new HosDesc();
		if (s.contains("HOS")) {
			int startIndex = s.lastIndexOf("HOS(");
			int charLength = "HOS(".length();
			String temString = s.substring(startIndex + charLength, s.length());
			int endIndex = temString.indexOf(")");
			String ss = temString.substring(0, endIndex);
			String[] str = ss.split(",");
			hosDesc.typeId = Integer.parseInt(str[0].substring(4));
			hosDesc.itemId = Integer.parseInt(str[1].substring(4));
			hosDesc.showName = str[2].substring(3);
			int instanceId = Server.server.getServiceRegistry().getSleepyCatService().generatorHorseId();
			hosDesc.instanceId = instanceId;
			hosDesc.level = Integer.parseInt(str[4].substring(4));
			hosDesc.fixCount = Integer.parseInt(str[5].substring(4));
			hosDesc.imageId = Integer.parseInt(str[6].substring(5));
			hosDesc.imageChangeId = Integer.parseInt(str[7].substring(11));
			String skil = str[8].substring(5);
			String[] sks = skil.split("\\+");
			for (int i = 0; i < sks.length; i++) {
				hosDesc.addSkillId(Integer.parseInt(sks[i]));
				if(str.length>=10){
					String skillLevel = str[9].substring(9);
					String[] level = skillLevel.split("\\+");
				    hosDesc.addSkillLevel(Integer.parseInt(sks[i]), Integer.parseInt(level[i]));
				}
			}
		}
		if (s.contains("EQUS")) {
			int startIndex = s.lastIndexOf("EQUS[");
			int charLength = "EQUS[".length();
			String tempString = s
					.substring(startIndex + charLength, s.length());
			int endIndex = tempString.indexOf("]");
			String ss = tempString.substring(0, endIndex);
			if(!ss.equals("")){
				String[] strs = ss.split("\\)");
				for(int i=0;i<strs.length;i++){
					StringBuilder sb = new StringBuilder();
					sb.append(strs[i]);
					sb.append(")");
					strs[i] = sb.toString();
				}
				for (int i = 0; i < strs.length; i++) {
					EquDesc equDesc = parseEquLog(strs[i]);
					hosDesc.addEqu(equDesc);
				}
			}
		}
		return hosDesc;
	}
	/**
	 * 提取给定属性的值  只适用于PLAYER[12]形式
	 */
	public static String getValue(String logStr, String str){
		int startIndex = logStr.lastIndexOf(str + "[");
		int charLength = str.length() + 1;
		String temString = logStr.substring(startIndex + charLength, logStr.length());
		int endIndex = temString.indexOf("]");
		String value = temString.substring(0,endIndex);
		return value;
	}
	/**
	 * 获得随从
	 * @param hosDesc
	 * @return 随从
	 * @throws Exception
	 */
	public Attendant getAttendant(AttendantDesc attDesc) throws Exception{
		Player p = ObjectAccessor.getPlayer(playerId);
		if(p == null){
			p = Server.server.getServiceRegistry().getDbService().playerDAO.getPlayerById(playerId);
		}
		Attendant attendant = null;
		if(p!=null) {
			attendant = new Attendant(attDesc.typeId,p);
			attendant.name = attDesc.name;
			attendant.setLoyal(attDesc.loyal);
			attendant.hp = attDesc.hp;
			attendant.mp = attDesc.mp;
			attendant.attendantType.image.id = attDesc.imageId;
			attendant.attendantType.id = attDesc.typeId;
			for(int i=0;i<attendant.skillSwitchs.length;i++){
				attendant.skillSwitchs[i] = attDesc.skillSwitchs[i];
			}
			attendant.skills.clear();
			for(int i=0;i<attDesc.skillIds.length;i++){
				int skillId = attDesc.skillIds[i];
				Skill skill = ObjectAccessor.getSkill(Skills.getSkillId(skillId>>16, 1));
				if(skillId==0)
					attendant.skills.add(null);
				else
					attendant.skills.add(skill);
			}
			attendant.attLevel = attDesc.attLevel;
			attendant.specialSkills.clear();
			for(int i=0;i<attDesc.specialSkills.size();i++){
				attendant.specialSkills.add(ObjectAccessor.getSkill(Skills.getSkillId(attDesc.specialSkills.get(i)>>16, attendant.attLevel/2)));
			}
			if (attDesc.equ != null && attDesc.equ.size() > 0) {
				List<EquDesc> equDesc = attDesc.equ;
				for (int i = 0; i < equDesc.size(); i++) {
					EquDesc equList = equDesc.get(i);
					GameItem item = getEqu(equList);
					attendant.equs[i] = item;
				}
			}
		}else{
			throw new Exception(peony.Messages.STRING_00952);
		} 
		return attendant;
	}
	
	/**
	 * 解析随从日志
	 * @param args
	 * [THROWATTENDANT]PLAYER[5]TID[19]IMAGEID[509]INS[16000006]NAME[吕布]LOYAL[96]HP[14340]MP[5935]SWITCH[true,true,true,false,false,false,]SKILLSIZE[6]SKILL[24117249,23134209,0,0,0,0,]ATTLEVEL[10]EQUS[]
	 */
	public static AttendantDesc parseAttendantLog(String s) throws ParseLogException {
		if (!s.startsWith("[THROWATTENDANT]")) {
			throw new ParseLogException(peony.Messages.STRING_00958);
		}
		AttendantDesc attDesc = new AttendantDesc();
		if (s.contains("PLAYER")) {
			attDesc.ownerId = Integer.parseInt(getValue(s,"PLAYER"));
		}
		if (s.contains("TID")) {
			attDesc.typeId = Integer.parseInt(getValue(s,"TID"));
		}
		if(s.contains("IMAGEID")){
			attDesc.imageId = Integer.parseInt(getValue(s,"IMAGEID"));
		}
		if (s.contains("NAME")) {
			attDesc.name = getValue(s,"NAME");
		}
		if(s.contains("LOYAL")){
			attDesc.loyal = Integer.parseInt(getValue(s,"LOYAL"));
		}
		if (s.contains("HP")) {
			attDesc.hp = Integer.parseInt(getValue(s,"HP"));
		}
		if (s.contains("MP")) {
			attDesc.mp = Integer.parseInt(getValue(s,"MP"));
		}
		if (s.contains("SWITCH")) {
			String[] switchs = getValue(s,"SWITCH").split(",");
			attDesc.skillSwitchs = new boolean[switchs.length];
			for(int i = 0;i < switchs.length;i++){
				if(switchs[i].equals("true") || switchs[i].equals("1") ){
					attDesc.skillSwitchs[i] = true;
				}else if(switchs[i].equals("false") || switchs[i].equals("0") ){
					attDesc.skillSwitchs[i] = false;
				}
			}
		}
		if(s.contains("SKILLSIZE")){
			attDesc.skillSize = Integer.parseInt(getValue(s,"SKILLSIZE"));
		}
		if(s.contains("SKILL")){
			String[] ids = Utils.splitString(getValue(s,"SKILL"), ',');
			attDesc.skillIds = new int[attDesc.skillSize];
			for(int i = 0;i < attDesc.skillSize;i++){
				attDesc.skillIds[i] = Integer.parseInt(ids[i]);
			}
		}
		if(s.contains("ATTLEVEL")){
			attDesc.attLevel = Integer.parseInt(getValue(s,"ATTLEVEL"));
		}
		if(s.contains("SPECIALSKI")){
			String[] ids = Utils.splitString(getValue(s,"SPECIALSKI"), ',');
			for(int i = 0;i < ids.length-1;i++){
				attDesc.addSpecialSkills(Integer.parseInt(ids[i]));
			}
		}
		if (s.contains("EQUS")) {
			int startIndex = s.lastIndexOf("EQUS[");
			int charLength = "EQUS[".length();
			String tempString = s
					.substring(startIndex + charLength, s.length());
			int endIndex = tempString.indexOf("]");
			String ss = tempString.substring(0, endIndex);
			if(!ss.equals("")){
				String[] strs = ss.split("\\)");
				for(int i=0;i<strs.length;i++){
					StringBuilder sb = new StringBuilder();
					sb.append(strs[i]);
					sb.append(")");
					strs[i] = sb.toString();
				}
				for (int i = 0; i < strs.length; i++) {
					EquDesc equDesc = parseEquLog(strs[i]);
					attDesc.addEqu(equDesc);
				}
			}
		}
		return attDesc;
	}
}

class ItemDesc {
	public int itemId;
	public int instanceId;
}

class EquDesc extends ItemDesc {
	public int star;
	public int hole;
	public int maxHole;
	public int[] jewels;
	protected int[] jewelUpgrades;
	public int[] naturals;
	public int bindInstance = 0;
	public String markString;
}

class HosDesc extends ItemDesc {
	String showName;
	public int level;
	public int typeId;
	public int fixCount;
	public int imageId;
	public int imageChangeId;
	List<EquDesc> equ = new ArrayList<EquDesc>();
	List<Integer> skillIds = new ArrayList<Integer>();
	Map<Integer,Integer> skillLevel = new HashMap<Integer,Integer>();
	
	public void addEqu(EquDesc equip){
		equ.add(equip);
	}
	
	public void addSkillId(int skillId){
		skillIds.add(new Integer(skillId));
	}
	
	public void addSkillLevel(int skillId,int level){
		skillLevel.put(skillId, level);
	}
}

class AttendantDesc extends ItemDesc{
	public int ownerId;
	public int typeId;
	public String name;
	public int loyal;
	public int hp;
	public int mp;
	public boolean[] skillSwitchs;
	public int skillSize;
	public int[] skillIds;
	public int imageId;
	public int attLevel;
	List<Integer> specialSkills = new ArrayList<Integer>();
	List<EquDesc> equ = new ArrayList<EquDesc>();
	
	public void addEqu(EquDesc equip){
		equ.add(equip);
	}
	
	public void addSpecialSkills(int id){
		specialSkills.add(id);
	}
}
