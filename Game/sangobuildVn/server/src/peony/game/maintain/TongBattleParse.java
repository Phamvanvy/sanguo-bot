package peony.game.maintain;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class TongBattleParse {
	private static final String FILE_NAME = "d:/sango/客服问题/logtongbattle";
	
	public static void main(String[] args) throws Exception{
		for(int i=1;i<=10;i++){
			BufferedReader r = new BufferedReader(new FileReader(FILE_NAME+i));
			String s = null;
			Map<Integer,TongBattleMoney>[] battles = new Map[4];
			for(int j=0;j<4;j++){
				battles[j] = new HashMap<Integer,TongBattleMoney>();
			}
			while((s=r.readLine())!=null){
				int day = getDay(s);
				int tongId = getTongId(s);
				int playerId = getPlayerId(s);
				int money = getMoney(s);
				Map<Integer,TongBattleMoney> map = battles[day-1];
				TongBattleMoney tongMoney = map.get(tongId);
				if(tongMoney==null){
					tongMoney = new TongBattleMoney(tongId,playerId);
					map.put(tongId, tongMoney);
				}
				tongMoney.money += money;
			}
			for(int j=0;j<4;j++){
				Map<Integer,TongBattleMoney> map = battles[j];
				System.out.println(String.format("[%d]区[%d]日",i,(11+j)));
				for(int tongId:map.keySet()){
					TongBattleMoney tongMoney = map.get(tongId);
					System.out.println(String.format("TONG[%d]PLAYER[%d]MONEY[%d]", tongMoney.tongId,tongMoney.playerId,tongMoney.money));
				}
			}
			r.close();
		}
	}
	
	public static int getDay(String s){
		return Integer.parseInt(s.substring(19,20));
	}
	
	public static int getTongId(String s){
		int i1 = s.indexOf("TONGID[");
		int i2 = s.indexOf("]", i1);
		return Integer.parseInt(s.substring(i1+7,i2));
	}
	
	public static int getPlayerId(String s){
		int i1 = s.indexOf("ID[");
		int i2 = s.indexOf("]",i1);
		return Integer.parseInt(s.substring(i1+3,i2));
	}
	
	public static int getMoney(String s){
		int i1 = s.indexOf("MONEY[");
		if(i1 == -1){
			i1 = s.indexOf("BID[");
			int i2 = s.indexOf("]",i1);
			return Integer.parseInt(s.substring(i1+4,i2));
		}else{
			int i2 = s.indexOf("]",i1);
			return Integer.parseInt(s.substring(i1+6,i2));
		}
	}
}

class TongBattleMoney{
	public int tongId;
	public int playerId;
	public int money;
	
	public TongBattleMoney(int tongId,int playerId){
		this.tongId = tongId;
		this.playerId = playerId;
	}
}
