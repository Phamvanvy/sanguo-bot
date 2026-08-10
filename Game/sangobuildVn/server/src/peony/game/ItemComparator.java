package peony.game;

import java.util.Comparator;

public class ItemComparator implements Comparator<GameItem>{

//	n	整理的物品类型排列顺序为：
//0	u	补给类：药品和食物
//1	u	坐骑口粮：坐骑用的口粮
//2	u	杂物类：没有具体分类的物品。
//3	u	装备类：装备内部按武器（武器内部按类型再排）、头盔、衣服、裤子、鞋子、副手、披风、护符、护腕、玉佩的顺序；
//4	u	配方书类：各种指引物品或者说明物品，使用可显示特定的内容或者功能
//5	u	技能书类：用于战斗技能或者生产技能学习的物品，使用后可学习到对应技能
//6	u	称号类：获得的称号物品
//7	u	坐骑类：可以使用的坐骑
//8	u	材料类：生活技能所需材料
//9	u	宝石类：各种宝石
//10	u	精炼类：装备提升功能对应的物品。
//11	u	普通类：主要定义任务物品。
	
	
	public static final int[] ORDER = {
		11,
		3,
		5,
		7,
		1,
		6,
		8,
		4,
		9,
		0,
		2,
		10,
	};
	
//    /**
//     * 物品分类：一般物品。
//     */
//    public static final int TYPE_NORMAL = 0;
//    /**
//     * 物品分类：装备。
//     */
//    public static final int TYPE_EQUIP = 1;
//    /**
//     * 物品分类：技能书。
//     */
//    public static final int TYPE_SKILLBOOK = 2;
//    /**
//     * 物品分类：坐骑。
//     */
//    public static final int TYPE_HORSE = 3;
//    /**
//     * 物品分类：坐骑口粮。
//     */
//    public static final int TYPE_HORSEFOOD = 4;
//    /**
//     * 物品分类：称号。
//     */
//    public static final int TYPE_TITLE = 5;
//    /**
//     * 物品分类：采集材料。
//     */
//    public static final int TYPE_MATERIAL = 6;
//    /**
//     * 物品分类：打造配方。
//     */
//    public static final int TYPE_FORMULA = 7;
//    /**
//     * 物品分类：宝石。
//     */
//    public static final int TYPE_JEWEL = 8;
//    /**
//     * 物品分类：补给。
//     */
//    public static final int TYPE_RENEW = 9;
//    /**
//     * 物品分类：杂物。
//     */
//    public static final int TYPE_MISC = 10;
//    /**
//     * 物品分类：精炼。
//     */
//    public static final int TYPE_REFINE = 11;
    
	
	public static final int getOrder(int type){
		if(type>ORDER.length-1){
			return 100;
		}else{
			return ORDER[type];
		}
	}
	
	public int compare(GameItem o1, GameItem o2) {
		int order1 = getOrder(o1.template.itemType);
		int order2 = getOrder(o2.template.itemType);
		if (order1 == order2) {
			if (o1.template.equipment != null && o2.template.equipment != null) {
//				头盔、衣服、裤子、鞋子、副手、披风、护符、护腕、玉佩
				int ret = o1.template.equipment.minorType - o2.template.equipment.minorType;
				if(ret==0){
					int ret1 = o1.template.id - o2.template.id;
					if(ret1==0){
						return o1.instanceId - o2.instanceId;
					}else{
						return ret1;
					}
				}else{
					return ret;
				}

			} else {
				int ret = o1.template.showType - o2.template.showType;
				if (ret == 0) {
					if (o1.instanceId == GameItem.GENERAL_INSTANCEID
							&& o2.instanceId == GameItem.GENERAL_INSTANCEID) {
						ret = o1.template.id - o2.template.id;
						if(ret==0){
							if(o1.object!=o2.object){
								if(o1.object==null)
									return -1;
								if(o2.object==null)
									return 1;
								if(o1.object instanceof Horse && o2.object instanceof Horse){
									return ((Horse)o1.object).instanceId - ((Horse)o2.object).instanceId;
								}else{
									throw new IllegalArgumentException();
								}
							}else{
								return ret;
							}
						}else{
							return ret;
						}
					} else {
						return o1.instanceId - o2.instanceId;
					}
				} else {
					return ret;
				}
			}
		} else {
			return order1 - order2;
		}
	}

}
