package peony.game.admin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import com.pip.sanguo.data.HorseType;
import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.Horse;
import peony.game.ItemTemplate;
import peony.game.ItemUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Skills;
import peony.game.itemeffect.HorseItemEffect;
import peony.game.itemenhance.ItemEnhance;
import peony.game.itemenhance.NaturalEnhance;
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
					GameItem item = getEqu(equDesc);
					Server.server.getServiceRegistry().getMailService()
					.sendSystemMail(playerId, "<cFF0000>[系统]</c>\n<cFF0000>[hệ thống]</c>", title, content,
							0, item, 1, "GM");
					Packet pt = new Packet(OpCode.ADMIN_SENDMAIL2_SERVER);
					pt.putInt(serial);
					pt.putString(item.template.name);
					session.send(pt);
			   } else {
					ErrorHandler.sendErrorMessage(session, serial, 
							OpCode.ADMIN_SENDMAIL2_CLIENT, "Số liệu người chơi sai");
			   }
			} else if(itemStringDesc.startsWith("HORSE[")){
				HosDesc hosDesc = parseHosLog(itemStringDesc);
				Horse h = null;
					h = getHorse(hosDesc);
				GameItem horseItem = ObjectAccessor.createGameItem(h.itemId);
				horseItem.object = h;
				Server.server.getServiceRegistry().getMailService().sendSystemMail(playerId, "<cFF0000>[系统]</c>\n<cFF0000>[hệ thống]</c>", title, content, 0, horseItem, 1, "GM");
				Packet pt = new Packet(OpCode.ADMIN_SENDMAIL2_SERVER);
				pt.putInt(serial);
				pt.putString(horseItem.template.name);
				session.send(pt);
			} else {
				ErrorHandler.sendErrorMessage(session, serial, 
						OpCode.ADMIN_SENDMAIL2_CLIENT, "Nhật ký hàng ngày quy cách sai,ví dụ lấy EQU hoặc HORSE mở đầu");
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
			int horseTypeId = hosDesc.itemId;
			HorseType horseType = ObjectAccessor
					.getHorseType(horseTypeId);
			ItemTemplate it = ObjectAccessor.getHorseTemplate(horseTypeId);
			if(horseType != null && it != null){
				HorseItemEffect effect = (HorseItemEffect)it.useType.effect;
				Field imageField = HorseItemEffect.class.getDeclaredField("imageId");
				int imageId = 0;
				if(imageField != null){
					imageField.setAccessible(true);
					imageId = imageField.getInt(effect);
				} else {
					throw new Exception("Không tìm được hình vẽ của thú cưỡi");
				}
				Horse h = new Horse(horseType,hosDesc.instanceId);
				h.name = hosDesc.showName;
				h.level = hosDesc.level;
				h.itemId = it.id;
				h.imageId = imageId;
				h.iconId = it.showType;
				for(int i=0;i<hosDesc.skillIds.size();i++){
					h.addSkill(ObjectAccessor.getSkill(Skills.getSkillId(hosDesc.skillIds.get(i)>>16, 1)),p);
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
				throw new Exception("Không tìm thấy loại hình thú cưỡi hoặc vật phẩm thú cưỡi");
			}
		} else {
			throw new Exception("Số liệu người chơi sai");
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
					|| equDesc.maxHole > 0) {
				ItemEnhance enhance = new ItemEnhance();
				enhance.setAddHole(equDesc.hole);
				enhance.setAddMaxHole(equDesc.maxHole);
				enhance.setStar(equDesc.star);
				if (equDesc.jewels != null) {
					for (int i = 0; i < equDesc.jewels.length; i++) {
						enhance.addJewel(i, equDesc.jewels[i]);
					}
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
				item.object = enhance;
			}
			item.bindInstance = equDesc.bindInstance;
			return item;
		} else {
			throw new Exception("Trang bị id lỗi ");
		}
	}
  
	/**
	 * 解析装备日志
	 * @param s
	 * @return
	 * @throws ParseLogException
	 */
	// EQU(1006605,9011905,S=6,H=3,JEW=1396+1368+1431+1375,NR=4/31+10/11)
	public static EquDesc parseEquLog(String s) throws ParseLogException {
		if (!s.startsWith("EQU(")) {
			throw new ParseLogException("Nhật ký hàng ngày cách thức lỗi, ví dụ lấy EQU mở đầu");
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
	// HORSE[HOS(TID=149,NM=Tok,IID=447271,LVL=70,SKLS=6946817+6881281+6750209+7208961+6815745)]CAUSE[DEL]EQUS[EQU(1007560,41132991,S=5,H=3,JEW=1396+1466+1473+1368,NR=7/12+10/34)EQU(1007463,39567903,S=3,H=4,JEW=1466+1389+1431+1473+1368,NR=1/7+0/36)EQU(1007443,42329310,S=6,H=4,JEW=1368+1375+1396+1459+1473,NR=10/37+7/6)EQU(1007451,40779504,S=7,H=3,JEW=1368+1473+1459+1375,NR=0/11+10/19)EQU(1007654,41244228,S=5,H=3,JEW=1459+1396+1375+1417,NR=1/27+11/28)EQU(1007565,43237768,S=8,H=3,JEW=1368+1473+1459+1396,NR=1/20+10/6)EQU(1007454,42810317,S=5,H=3,JEW=1368+1459+1473+1375,NR=11/11+10/45)]
	public static HosDesc parseHosLog(String s) throws ParseLogException {
		if (!s.startsWith("HORSE[")) {
			throw new ParseLogException("Cách thức nhật kí sai, Ví dụ lấy HORSE bắt đầu");
		}
		HosDesc hosDesc = new HosDesc();
		if (s.contains("HOS")) {
			int startIndex = s.lastIndexOf("HOS(");
			int charLength = "HOS(".length();
			String temString = s.substring(startIndex + charLength, s.length());
			int endIndex = temString.indexOf(")");
			String ss = temString.substring(0, endIndex);
			String[] str = ss.split(",");
			hosDesc.itemId = Integer.parseInt(str[0].substring(4));
			hosDesc.showName = str[1].substring(3);
			int instanceId = Server.server.getServiceRegistry().getSleepyCatService().generatorHorseId();
			hosDesc.instanceId = instanceId;
			hosDesc.level = Integer.parseInt(str[3].substring(4));
			String skil = str[4].substring(5);
			String[] sks = skil.split("\\+");
			for (int i = 0; i < sks.length; i++) {
				hosDesc.addSkillId(Integer.parseInt(sks[i]));
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

	public static void main(String[] args) {
		ItemDesc desc = null;
		ItemDesc desc1 = null;
		try {
			desc = parseEquLog("EQU(1006605,9011905,S=6,H=3,JEW=1396+1368+1431+1375,NR=4/31+10/11)BINDINSTANCE(-1)");
			desc1 = parseHosLog("HORSE[HOS(TID=149,NM=Tok丶nháy mắt,IID=447271,LVL=70,SKLS=6946817+6881281+6750209+7208961+6815745)]CAUSE[DEL]EQUS[EQU(1007560,41132991,S=5,H=3,JEW=1396+1466+1473+1368,NR=7/12+10/34)EQU(1007463,39567903,S=3,H=4,JEW=1466+1389+1431+1473+1368,NR=1/7+0/36)EQU(1007443,42329310,S=6,H=4,JEW=1368+1375+1396+1459+1473,NR=10/37+7/6)EQU(1007451,40779504,S=7,H=3,JEW=1368+1473+1459+1375,NR=0/11+10/19)EQU(1007654,41244228,S=5,H=3,JEW=1459+1396+1375+1417,NR=1/27+11/28)EQU(1007565,43237768,S=8,H=3,JEW=1368+1473+1459+1396,NR=1/20+10/6)EQU(1007454,42810317,S=5,H=3,JEW=1368+1459+1473+1375,NR=11/11+10/45)]");
		} catch (ParseLogException e) {
			e.printStackTrace();
		}
		System.out.println(desc);
		System.out.println(desc1);
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
	public int[] naturals;
	public int bindInstance = 0;
}

class HosDesc extends ItemDesc {
	String showName;
	int level;
	List<EquDesc> equ = new ArrayList<EquDesc>();
	List<Integer> skillIds = new ArrayList<Integer>();
	
	public void addEqu(EquDesc equip){
		equ.add(equip);
	}
	
	public void addSkillId(int skillId){
		skillIds.add(new Integer(skillId));
	}
}