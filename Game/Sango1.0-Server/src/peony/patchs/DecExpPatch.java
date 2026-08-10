package peony.patchs;

import peony.game.Player;
import peony.game.PlayerUtil;
import peony.game.Server;

public class DecExpPatch implements Runnable {

	// arr[0]角色ID,arr[1]账号ID,arr[2]扣除经验值 
	protected static long[][] datas = new long[][]{{545755, 31942186, 1073778000}};
	
	public void run() {
		for(int i=0;i<datas.length;i++){
			long[] arr = datas[i];
			int playerId = (int) arr[0];
			int accountId = (int) arr[1];
			long decExp = arr[2];
			Player player = Server.server.getServiceRegistry().getPlayerService().loadPlayer(accountId, playerId);
			if(player!=null){
				long[] expDatas = PlayerUtil.getDownLevel(player.level, player.exp, decExp);
				int newLevel = (int) expDatas[0];
				int currExp = (int) expDatas[1];
				player.setLevel(newLevel, true); // 设置新等级
				player.exp = currExp; // 设置新经验
				player.refreshSkillPoint(0); // 重洗技能点
				player.refreshPropertiesPoint(); // 重洗属性点
				player.refreshProperties(false); // 重刷属性
				System.out.println("[DECEXPOK]PLAYER[" + playerId + "]LEVEL[" + newLevel + "]EXP[" + currExp + "]");
			}
		}
	}

}
