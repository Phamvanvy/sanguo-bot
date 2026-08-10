package canseereaditem;

import java.io.IOException;

public class MercenaryMenu extends MenuType {

	public MercenaryMenu() {
		type = MENU_MERCENARY;
		titleName=new String[]{"卖身查询","赎身查询","雇佣系统佣兵","雇佣玩家佣兵","启用佣兵","休息佣兵","解雇佣兵"};
	}

	@Override
	public int checkMessage(String s, int k) {
		int result = 0;
		if(k == TYPE_MERCENARY_SELLSELF && s.contains("sell mercenayID")){// 卖身查询
			result = TYPE_MERCENARY_SELLSELF;
		}else if(k == TYPE_MERCENARY_BUYSELF && s.contains("unsells")){//赎身查询
			result = TYPE_MERCENARY_BUYSELF;
		}else if(k == TYPE_MERCENARY_BUYSYSTEM && s.contains("buy system mercenayID")){//雇佣系统佣兵
			result = TYPE_MERCENARY_BUYSYSTEM;
		}else if(k == TYPE_MERCENARY_BUYPLAYER && s.contains("buy mercenayID")){//雇佣玩家佣兵
			result = TYPE_MERCENARY_BUYPLAYER;
		}else if(k == TYPE_MERCENARY_SETJOINTEAM && s.contains("use mercenayID")){//启用佣兵
			result = TYPE_MERCENARY_SETJOINTEAM;
		}else if(k == TYPE_MERCENARY_SETSLEEP && s.contains("sleep mercenayID")){//休息佣兵
			result = TYPE_MERCENARY_SETSLEEP;
		}else if(k == TYPE_MERCENARY_LAYOFF && s.contains("layoff mercenayID")){//解雇佣兵
			result = TYPE_MERCENARY_LAYOFF;
		}
		return result;
	}

	@Override
	public boolean parseMessage(String s, String tmps, String strID, int index)
			throws IOException {
		writeText(strID +" ",false);
		switch(index){
		case TYPE_MERCENARY_SELLSELF://卖身
			String price = readitem.splitMessageSP(s, "price[", defaultEnd);
			writeText("发布卖身， 佣兵ID[" + tmps + "] 价格[" + price + "] ",false);
			break;
		case TYPE_MERCENARY_BUYSELF://赎身
			writeText("成功赎身。",false);
			break;
		case TYPE_MERCENARY_BUYSYSTEM://雇佣系统佣兵
			price = readitem.splitMessageSP(s, "price[", defaultEnd);
			writeText("雇佣系统佣兵， 佣兵ID[" + tmps + "] 价格[" + price + "] ",false);
			break;
		case TYPE_MERCENARY_BUYPLAYER://雇佣玩家佣兵
			price = readitem.splitMessageSP(s, "price[", defaultEnd);
			String masterID = readitem.splitMessageSP(s, "masterID[", defaultEnd);
			writeText("雇佣玩家佣兵，佣兵ID[" + tmps + "] 价格[" + price + "] 佣兵本尊ID[" + masterID +"]",false);
			break;
		case TYPE_MERCENARY_SETJOINTEAM://启用佣兵
			writeText("启用佣兵，佣兵ID[" + tmps + "]",false);
			break;
		case TYPE_MERCENARY_SETSLEEP://休息佣兵
			writeText("休息佣兵，佣兵ID[" + tmps + "]",false);
			break;
		case TYPE_MERCENARY_LAYOFF://解雇佣兵
			writeText("解雇佣兵，佣兵ID[" + tmps + "]",false);
			break;
		}
		return true;
	}

	@Override
	public String splitMessage(String s, int index, int i) {
		String strStart="";
		if(index != TYPE_MERCENARY_BUYSELF && i != TYPE_MERCENARY_BUYSELF){
			strStart = "mercenayID[";
		}
		return null;
	}

}
