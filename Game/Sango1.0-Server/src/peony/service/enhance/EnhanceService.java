package peony.service.enhance;

import java.text.MessageFormat;
import java.util.Random;

import peony.game.Action;
import peony.game.GameItem;
import peony.game.Horse;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.game.attendant.Attendant;
import peony.game.itemenhance.ItemEnhance;
import peony.net.Packet;
import peony.service.Service;
import peony.service.tong.TongMember;
import peony.service.tong.TongService;
import peony.service.tong.TongSkill4;
import peony.service.tong.TongSkill5;

public class EnhanceService  implements Service {

	public static final String PROPERTY_ENHANCE_TIMES = "enhancetiems";

	public static final String PROPERTY_ENHANCE_DATE = "enhancedate";

	public static final int[] ENHANCE_MAX = { 150, 100, 150, 60 };
	
	protected static final int MONEY = 200; 

	protected static final String[] enhanceType = { "base", "natural", "star",
			"jewel" };

	protected static final String[] enhanceName = { peony.Messages.STRING_01960, peony.Messages.STRING_01961, peony.Messages.STRING_01962,
			peony.Messages.STRING_01963 };

	protected Random rnd = new Random();
	
	public static int decImoney = 2 * 36;

	public void shutdown() {

	}

	public void startup() throws Exception {

	}

	public void equipEnhance(Player p,GameItem gameItem,Object owner,int serial){
		int enhanceTimes = getEnhanceTimes(p);
		ItemEnhance ie = (ItemEnhance) gameItem.object;
		if (ie == null) {
			ie = new ItemEnhance();
			gameItem.object = ie;
		}
		// 强化成功后当天次数增加
		p.pool.setInt(PROPERTY_ENHANCE_TIMES, enhanceTimes + 1);
		if (enhanceTimes == 0) {
			// 记录最后一次强化日期
			p.pool.setInt(PROPERTY_ENHANCE_DATE, Time.day);
		}
		int[] oldArr = ie.getEnhanceData(); // 已有的强化效果
		int[] newArr = enhanceData(gameItem); // 新的强化效果
		//军团专属科技   装备强化加成
		TongService ts = Server.server.getServiceRegistry().getTongService();
		TongMember tm = ts.getPlayerInfo(p.id);
		if(tm!=null && tm.skills != null && tm.skills.get(5)!=null){
			TongSkill5 tskill = (TongSkill5)tm.skills.get(5);
			if(tskill != null && tskill.level>0){
				for(int i = 0;i < ENHANCE_MAX.length;i++){
					if(ENHANCE_MAX[i] * 60 / 100 > newArr[i]){
						newArr[i] = ENHANCE_MAX[i] * 60 / 100;
					}
				}
			}
		}
		Packet pt = new Packet(OpCode.ENHANCE_EQUIP_SERVER);
		pt.putInt(serial);
		pt.putInt(ENHANCE_MAX.length);
		for (int i = 0; i < ENHANCE_MAX.length; i++) {
			String color = getColor(newArr[i],ENHANCE_MAX[i]);
			
			int div = newArr[i] / 10;
			int mod = newArr[i] % 10;
			String str = div + "." + mod;
			byte type = getType(newArr[i], oldArr[i]);
			pt.putInt(newArr[i]);
			pt.putUTF(MessageFormat.format(peony.Messages.STRING_01964, str));
			pt.put(type);
		}
		ie.setEnhanceData(newArr);
		p.addAction(Action.EQUIP_ENHANCE);
		if (owner instanceof Player) {
			p.refreshProperties(false);
		} else if (owner instanceof Horse) {
			Horse h = (Horse) owner;
			h.refreshProperties(false, p);
			if (h == p.horse) {
				p.refreshProperties(false);
			}
		} else if(owner instanceof Attendant){
			((Attendant) owner).refreshProperties(false);
		}
		if(gameItem.bindInstance==-1){
		    gameItem.bindInstance = 0;
		}
		pt.put(gameItem.toClientBytes());
		p.send(pt);
	}
	
	public void autoEquipEnhance(Player p,GameItem gameItem,Object owner){
			int enhanceTimes = getEnhanceTimes(p);
			ItemEnhance ie = (ItemEnhance) gameItem.object;
			if (ie == null) {
				ie = new ItemEnhance();
				gameItem.object = ie;
			}
			// 强化成功后当天次数增加
			p.pool.setInt(PROPERTY_ENHANCE_TIMES, enhanceTimes + 1);
			if (enhanceTimes == 0) {
				// 记录最后一次强化日期
				p.pool.setInt(PROPERTY_ENHANCE_DATE, Time.day);
			}
			int[] newArr = enhanceData(gameItem); // 新的强化效果
			//军团专属科技   装备强化加成
			TongService ts = Server.server.getServiceRegistry().getTongService();
			TongMember tm = ts.getPlayerInfo(p.id);
			if(tm!=null && tm.skills != null && tm.skills.get(5)!=null){
				TongSkill5 tskill = (TongSkill5)tm.skills.get(5);
				if(tskill != null && tskill.level>0){
					for(int i = 0;i < ENHANCE_MAX.length;i++){
						if(ENHANCE_MAX[i] * 60 / 100 > newArr[i]){
							newArr[i] = ENHANCE_MAX[i] * 60 / 100;
						}
					}
				}
			}
			ie.setEnhanceData(newArr);
	}
	/**
	 * 确定颜色
	 */
	public String getColor(int num,int maxNum){
		int percent = Math.round((num*100)/maxNum);
//		String retColor = "<cCC0000>";
		String retColor = "<cF60000>";
		if(percent >= 90)
			//橙色
			retColor = "<cFF7F00>";
		else if(percent >= 80)
			//紫色
			retColor = "<cCD00CD>";
		else if(percent >= 70)
			//蓝色
			retColor = "<c00DDDD>";
		else if(percent >= 60)
			//绿色
			retColor = "<c00FF00>";
        return retColor;
	}

	/**
	 * 强化数据大随机限定
	 * @param gameItem
	 * @return
	 */
	public int[] enhanceData(GameItem gameItem) {
		ItemEnhance ie = (ItemEnhance) gameItem.object;
		int[] ret = new int[4];
		for (int i = 0; i < ENHANCE_MAX.length; i++) {
			ret[i] = rnd.nextInt(ENHANCE_MAX[i] - 1) + 1;
		}
		if (ret[0] >= 90 && ret[0] < 105) {
			ret[2] = rnd.nextInt(ENHANCE_MAX[2] - 50) + 50;
		} else if (ret[0] >= 105 && ret[0] < 120) {
			if (ie != null && ie.getStar() > 0) {
				ret[2] = rnd.nextInt(ENHANCE_MAX[2] - 50) + 50;
			} else {
				ret[1] = rnd.nextInt(ENHANCE_MAX[1] - 30) + 30;
			}
		} else if (ret[0] >= 120 && ret[0] < 135) {
			if (ie != null && ie.getJewelCount() > 0) {
				if (ie != null && ie.getStar() > 0) {
					ret[2] = rnd.nextInt(ENHANCE_MAX[2] - 50) + 50;
				} else {
					ret[1] = rnd.nextInt(ENHANCE_MAX[1] - 30) + 30;
				}
			} else {
				ret[3] = rnd.nextInt(ENHANCE_MAX[3] - 10) + 10;
			}
		}
		return ret;
	}

	public byte getType(int da, int lastDa) {
		if (da > lastDa) {
			return 2;
		} else if (da < lastDa) {
			return 1;
		} else {
			return 0;
		}
	}

	public int getEnhanceTimes(Player p) {
		int enhanceDate = p.pool.getInt(PROPERTY_ENHANCE_DATE, 0);
		if (enhanceDate == Time.day) {
			int enhanceTimes = p.pool.getInt(PROPERTY_ENHANCE_TIMES, 0);
			return enhanceTimes;
		}
		return 0;
	}

}
