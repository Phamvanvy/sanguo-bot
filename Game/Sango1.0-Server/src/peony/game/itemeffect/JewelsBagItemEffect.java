package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.net.Packet;

/**
 * 宝石定向礼包使用效果
 * @author pmeng
 */
public class JewelsBagItemEffect implements ItemEffect {

	protected int value;
	
	protected static int JEWELS_TYPE_NUM = 18; //宝石各等级种类
	
	protected static String[] jewelsNames = new String[]{
		peony.Messages.STRING_01686,
		peony.Messages.STRING_01687,
		peony.Messages.STRING_01688,
		peony.Messages.STRING_01689,
		peony.Messages.STRING_01690,
		peony.Messages.STRING_01691,
		peony.Messages.STRING_01692,
		peony.Messages.STRING_01693,
		peony.Messages.STRING_01694,
		peony.Messages.STRING_01695,
		peony.Messages.STRING_01696,
		peony.Messages.STRING_01697,
		peony.Messages.STRING_01698,
		peony.Messages.STRING_01699,
		peony.Messages.STRING_01700,
		peony.Messages.STRING_01701,
		peony.Messages.STRING_01702,
		peony.Messages.STRING_01703
	};
	protected static int[] JEWELS_LEVEL_1 = new int[]{1351,1358,1365,1372,1379,1386,1393,1400,1407,1414,1421,1428,1435,1442,1449,1456,1463,1470};
	protected static int[] JEWELS_LEVEL_2 = new int[]{1352,1359,1366,1373,1380,1387,1394,1401,1408,1415,1422,1429,1436,1443,1450,1457,1464,1471};
	protected static int[] JEWELS_LEVEL_3 = new int[]{1353,1360,1367,1374,1381,1388,1395,1402,1409,1416,1423,1430,1437,1444,1451,1458,1465,1472};
	protected static int[] JEWELS_LEVEL_4 = new int[]{1354,1361,1368,1375,1382,1389,1396,1403,1410,1417,1424,1431,1438,1445,1452,1459,1466,1473};
	protected static int[] JEWELS_LEVEL_5 = new int[]{1355,1362,1369,1376,1383,1390,1397,1404,1411,1418,1425,1432,1439,1446,1453,1460,1467,1474};
	protected static int[] JEWELS_LEVEL_6 = new int[]{1356,1363,1370,1377,1384,1391,1398,1405,1412,1419,1426,1433,1440,1447,1454,1461,1468,1475};
	protected static int[] JEWELS_LEVEL_7 = new int[]{1357,1364,1371,1378,1385,1392,1399,1406,1413,1420,1427,1434,1441,1448,1455,1462,1469,1476};
	
	protected static int[] JEWELS_BAG_IDS = new int[]{1612,1613,1614,1615,1616,1617,1618};
	
	protected static int[] JEWELS_BAGIDS_LEVEL4 = new int[]{4499,4500,4501,4502,4503,4504,4505,4506,4507,4508,4509,4510,4511,4512,4513,4514,4515};
	protected static int[] JEWELS_BAGIDS_LEVEL5 = new int[]{2732,2733,2734,2735,2736,2738,2739,2740,2741,2742,2743,2744,2745,2746,2747,2748,2737};
	
	public JewelsBagItemEffect(int value){
		this.value = value;
	}
	
	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException{
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException(peony.Messages.STRING_00014);
		Player p = (Player)target;
		int[] ids = getIds(value);
		Packet pt = new Packet(OpCode.EFFECT_JEWELS_LIST_SERVER);
		pt.putInt(JEWELS_TYPE_NUM);
		for(int i = 0;i < JEWELS_TYPE_NUM;i++){
			pt.putInt(ids[i]);
			String name = value + jewelsNames[i];
			pt.putString(name);
		}
		p.send(pt);
	}
	
	private int[] getIds(int level){
		if(level==1){
			return JEWELS_LEVEL_1;
		}else if(level==2){
			return JEWELS_LEVEL_2;
		}else if(level==3){
			return JEWELS_LEVEL_3;
		}else if(level==4){
			return JEWELS_LEVEL_4;
		}else if(level==5){
			return JEWELS_LEVEL_5;
		}else if(level==6){
			return JEWELS_LEVEL_6;
		}else if(level==7){
			return JEWELS_LEVEL_7;
		}
		return null;
	}
	
	public static boolean isJewelsItem(int itemId,int jewelsId){
		int level = -1;
		for(int i = 0;i < 7;i++){
			if(JEWELS_BAG_IDS[i] == itemId){
				level = i;
				break;
			}
		}
		if(level == -1){
			for(int i = 0;i < JEWELS_BAGIDS_LEVEL4.length;i++){
				if(JEWELS_BAGIDS_LEVEL4[i] == itemId){
					level = 3;
					break;
				}
			}
			for(int i = 0;i < JEWELS_BAGIDS_LEVEL5.length;i++){
				if(JEWELS_BAGIDS_LEVEL5[i] == itemId){
					level = 4;
					break;
				}
			}
		}
		if(level == -1)
			return false;
		if(ObjectAccessor.getItemTemplate(jewelsId).useLevel != (level + 1))
			return false;
		return true;
	}
	
	public boolean isAsync(){
		return false;
	}
	
	public boolean needRemove() {
		return false;
	}
}
