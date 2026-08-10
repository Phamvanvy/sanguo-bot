package com.pip.itimes.server.stage;

import java.util.Random;

import com.pip.itimes.server.util.Utils;

public class Viany {
	public static Random random = new Random();
	/**
	 * 鉴定使用的物品
	 */
	public static final int VIANY_ITEMID = 201245;
	/**
	 * 鉴定使用的物品需要的个数
	 */
	public static final int VIANY_ITEMCOUNT = 1;
	
	public static final byte VIANY_NOTENOUGHITEM = 1;		//没有足够的物品
	public static final byte VIANY_OK = 2;					//鉴定成功
	
	
	public static final int PERCENTDIV = 1000;
	
	/**
	 * 各属性攻等级机率
	 */
	public static final int[] percent = {
		0, 100, 120, 205, 250, 150, 70, 60, 30, 10, 5
	};
	
	/**
	 * vip玩家属性攻等级几率
	 */
	public static final int[] vippercent = {
		0, 10, 20, 30, 50, 120, 160, 160, 300, 100, 50
	};
	
	/**
	 * 攻击值 根据等级而定 从1级开始生效
	 */
	public static final int[] attack = {
		0, 10, 15, 25, 35, 45, 55, 65, 75, 90, 112
//		0, 6, 10, 13, 20, 26, 39, 53, 69, 89, 112 //原先数值
	};
	
	/**
	 * 防御值 根据等级而定 从1级开始生效
	 */
	public static final int[] defense = {
		0, 3, 6, 10, 13, 20, 26, 33, 43, 59, 76
	};
	
	public static final int MAX_VIANY_LEVEL = 10;
	public static final int AD_LEVEL = 8;
	
	public static final byte NULL = 0;
	public static final byte STONE = 1;
	public static final byte SCISSORS = 2;
	public static final byte PAPER = 3;
	
	private int stone = 0;					//石头
	private int scissors = 0;				//剪子
	private int paper = 0;					//布
	
	private int tmp_stone = 0;
	private int tmp_scissors = 0;
	private int tmp_paper = 0;
	
	public void setStone(int stone){
		this.stone = stone;
	}
	
	public int getStone(){
		return stone;
	}
	
	public void setScissors(int scissors){
		this.scissors = scissors;
	}
	
	public int getScissors(){
		return scissors;
	}
	
	public void setPaper(int paper){
		this.paper = paper;
	}
	
	public int getPaper(){
		return paper;
	}
	
	/**
	 * 获取指定类型的值
	 * @param state
	 * @return
	 */
	public int getViany(byte state){
		switch(state){
		case STONE:
			return stone;
		case SCISSORS:
			return scissors;
		case PAPER:
			return paper;
		}
		return 0;
	}
	
	public void setViany(byte state, int level){
		switch(state){
		case STONE:
			stone = level;
			break;
		case SCISSORS:
			scissors = level;
			break;
		case PAPER:
			paper = level;
			break;
		}
	}
	
	public int getVianyAttack(byte state){
		switch(state){
		case STONE:
			return attack[stone];
		case SCISSORS:
			return attack[scissors];
		case PAPER:
			return attack[paper];
		}
		return 0;
	}
	
	public int getVianyDefense(byte state){
		switch(state){
		case STONE:
			return defense[stone];
		case SCISSORS:
			return defense[scissors];
		case PAPER:
			return defense[paper];
		}
		return 0;
	}
	
	public static String getName(byte state){
		switch(state){
		case STONE:
			return "石头";
		case SCISSORS:
			return "剪子";
		case PAPER:
			return "布";
		}
		return "无";
	}
	
	/**
	 * 获取属性攻的伤害值
	 * @param src_state
	 * @param src_attack
	 * @param target_state
	 * @param target_defense
	 * @return
	 */
	public static int getAttack(int src_state, int src_attack, int target_state, int target_defense){
		if(src_state == NULL || src_attack == 0) return 0;
		if(src_state == NULL && target_state == NULL){
			return 0;
		}
		//攻击方有属性攻而防御方无属性防：
		//伤害=属性攻*2  直接从防御方血量扣除
		if(target_state == NULL){
			return src_attack << 1;
		}
		//二：攻击方有属性攻，防御方也有属性防：
		//1.攻防双方属性类型一样：伤害=属性攻+属性攻*（400/（400+属性防））直接从防御方血量扣除
		if(src_state == target_state){
			return src_attack + src_attack * (400 / (400 + target_defense));
		}
		//2.攻击方的属性类型克制防御方的属性类型：伤害=属性攻+属性攻*（1300/（1300+属性防））直接从防御方血量扣除
		//3.防御方的属性类型克制攻击方的属性类型：伤害=（属性攻-属性防）+属性攻*（500/（500+属性防））直接从防御方血量扣除。
		boolean restrain = false;
		switch(src_state){
		case STONE:
			restrain = target_state == SCISSORS ? true : false;
			break;
		case SCISSORS:
			restrain = target_state == PAPER ? true : false;
			break;
		case PAPER:
			restrain = target_state == STONE ? true : false;
			break;
		}
		if(restrain){
			return src_attack + src_attack * (1300 / (1300 + target_defense));
		}
		return (src_attack - target_defense) + src_attack * (500 / (500 + target_defense));
	}
	
	/**
	 * 获得鉴定后的等级 最少返回1级
	 * @return
	 */
	public static int getVianyLevel(boolean isVip){
		if(isVip){
			for(int i=1; i<vippercent.length; i++){
				if(Utils.hit(random, vippercent[i], PERCENTDIV)){
					return i;
				}
			}
		}else{
			for(int i=1; i<percent.length; i++){
				if(Utils.hit(random, percent[i], PERCENTDIV)){
					return i;
				}
			}
		}
		return 1;
	}
	
	public void setTempStone(int tmp_stone){
		this.tmp_stone = tmp_stone;
	}
	
	public int getTempStone(){
		return tmp_stone;
	}
	
	public void setTempScissors(int tmp_scissors){
		this.tmp_scissors = tmp_scissors;
	}
	
	public int getTempScissors(){
		return tmp_scissors;
	}
	
	public void setTempPaper(int tmp_paper){
		this.tmp_paper = tmp_paper;
	}
	
	public int getTempPaper(){
		return tmp_paper;
	}
	
	/**
	 * 获取指定类型的值 临时的
	 * @param state
	 * @return
	 */
	public int getVianyTemp(byte state){
		switch(state){
		case STONE:
			return tmp_stone;
		case SCISSORS:
			return tmp_scissors;
		case PAPER:
			return tmp_paper;
		}
		return 0;
	}
	
	public void setVianyTemp(byte state, int level){
		switch(state){
		case STONE:
			tmp_stone = level;
			break;
		case SCISSORS:
			tmp_scissors = level;
			break;
		case PAPER:
			tmp_paper = level;
			break;
		}
	}
}
