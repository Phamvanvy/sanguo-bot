package log.define.processor.sango;

import log.define.processor.LogProcessor;

public class LogProcessorResultType extends LogProcessor {

	public LogProcessorResultType(String id) {
		super(id);
	}

	@Override
	public String process(String data) {
		String result = null;
		if (data.equals("DRP"))
			result = "掉落";
		else if (data.equals("QST"))
			result = "完成任务";
		else if (data.equals("SELL"))
			result = "商店出售";
		else if(data.equals("DEL"))
			result = "删除";
		else if(data.equals("UST"))
			result = "从仓库取出到背包";
		else if(data.equals("NSL"))
			result = "国家科技珍宝获取";
		else if(data.equals("BUY"))
			result = "商店购买";
		else if(data.equals("GTR"))
			result = "获得原材料";
		else if(data.equals("UEQ"))
			result = "卸装";
		else if(data.equals("EXPBAG"))
			result = "经验包";
		else if(data.equals("ITE"))
			result = "使用物品掉落";
		else if(data.equals("FACTIONCHAT"))
			result = "阵营聊天";
		else if(data.equals("EQU"))
			result = "装备";
		else if(data.equals("PVP"))
			result = "杀死敌对阵营玩家";
		else if(data.equals("USE"))
			result = "使用";
		else if(data.equals("ATT"))
			result = "提取附件";
		else if(data.equals("TEL"))
			result = "驿站传送";
		else if(data.equals("AUCSELL"))
			result = "拍卖成功";
		else if(data.equals("AUC"))
			result = "拍卖出价";
		else if(data.equals("CAU"))
			result = "发布拍卖";
		else if(data.equals("AUCTOUT"))
			result = "拍卖流拍";
		else if(data.equals("AUCFAIL"))
			result = "竞拍失败";
		else if(data.equals("ACTCODE"))
			result = "激活码兑换";
		else if(data.equals("ACCBIND"))
			result = "密码绑定";
		else if(data.equals("GM"))
		    result = "GM发送";
		else if(data.equals("KING"))
			result = "国共奖励";
		else if(data.equals("IMCARD"))
			result = "生成元宝卡";
		else if(data.equals("NBT"))
			result = "国战";
		else if(data.endsWith("COV"))
			result = "押镖奖励";
		else if(data.equals("SHOP"))
			result = "商店购买";
		else if(data.equals("QSA"))
			result = "答题奖励";
		else if(data.equals("NBT"))
			result = "国战杀死玩家";
		else if(data.equals("TBY"))
			result = "兑换称号";
		else if(data.equals("BTL"))
			result = "战场";
		else if(data.equals("NCS"))
			result = "竞选国公";
		else if(data.equals("GFT"))
			result = "领取礼包";
		else if(data.equals("KKG"))
			result = "杀死敌对阵营首领";
		else if(data.equals("PKE"))
			result = "建立决斗";
		else if(data.equals("EXC"))
			result = "玩家交易";
		else if(data.equals("EXCC"))
			result = "取消交易";
		else if(data.equals("NMA"))
			result = "邮件发送";
		else if(data.equals("DEP"))
			result = "仓库取物品收费";
		else if(data.equals("ANE"))
			result = "自动资质鉴定";
		else if(data.equals("RSP"))
			result = "重置技能";
		else if(data.equals("REP"))
			result = "修理装备";
		else if(data.equals("HBE"))
			result = "扩展坐骑栏位";
		else if(data.equals("MNE"))
			result = "资质鉴定";
		else if(data.equals("STE"))
			result = "星级鉴定";
		else if(data.equals("UHS"))
			result = "坐骑技能解锁";
		else if(data.equals("FBF"))
			result= "战场报名";
		else if(data.equals("AHL"))
		    result = "装备打孔";
		else if(data.equals("AJE"))
		    result = "镶嵌宝石";
		else if(data.equals("MJE"))
		    result = "宝石合成";
		else if(data.equals("RJE"))
		    result = "宝石移除";
		else if(data.equals("NCN"))
		    result = "报名国公选举";
		else if(data.equals("NCL"))
		    result = "国库捐献";
		else if(data.equals("NPU"))
		    result = "国公罚款";
		else if(data.equals("NSL"))
			result = "国家福利";
		else if(data.equals("MGE"))
		    result = "结婚";
		else if(data.equals("CTG"))
		    result = "创建军团";
		else if(data.equals("CHG"))
		    result = "充值";
		else if(data.equals("IMC"))
		    result = "使用元宝卡";
		else if(data.equals("PKW"))
			result = "决斗胜利";
		else if(data.equals("PKC"))
			result = "决斗取消";
		else if(data.equals("HEU"))
			result = "坐骑装备解绑";
		else if(data.equals("CCL"))
			result = "改变职业";
		else if(data.equals("FED"))
			result = "坐骑喂食";
		else if(data.equals("MKE"))
			result = "玩家打造";
		else if(data.equals("STO"))
			result = "存入仓库";
		else if(data.equals("HEQ"))
			result = "坐骑装备";
		else if(data.equals("QST"))
			result = "移除任务";
		else if(data.equals("MTL"))
			result = "世界地图传送";
		else if(data.equals("DVC"))
			result = "离婚";
		else if(data.equals("NQE"))
			result = "发布国家任务";
		else if(data.equals("RLV"))
			result = "复活";
		else if(data.equals("HLK"))
			result = "坐骑技能锁定";
		else if(data.equals("HSK"))
			result = "坐骑洗技能";
		else if(data.equals("CHT"))
			result = "聊天";
		else if(data.equals("AFD"))
			result = "坐骑口粮自动消耗";
		else if(data.equals("RNM"))
			result = "重命名扣除";
		else if(data.equals("UST"))
			result = "从仓库中取出";
		else if(data.equals("HUE"))
			result = "脱下坐骑装备";
		else if(data.equals("CRE"))
			result = "创建角色";
		else if(data.equals("HPK"))
			result = "坐骑打包";
		else if(data.equals("ROL"))
			result = "分配中获取";
		else if(data.equals("GTR"))
			result = "采集";
		else if(data.equals("AUTOADDHOLE"))
			result = "自动打孔";
		else if(data.equals("UHS"))
			result = "解锁马技能";
		else if(data.equals("ENHANCEEQU"))
			result = "装备强化";
		else if(data.equals("ATTEQ"))
			result = "随从装备";
		else if(data.equals("ATTUEQ"))
			result = "卸下随从装备";
		else if(data.equals("BEAUTYVOTE"))
			result = "选美投票";
		else if(data.equals("BREAKLEVELREMOVEITEM"))
			result = "突破重天";
		else if(data.equals("ALCHEMYBYPLAYEREXP"))
			result = "经验修炼";
		else if(data.equals("STARPROMOTE"))
			result = "星辉提升";
		else if(data.equals("EXTENDDEPOT"))
			result = "扩展仓库包格";
		else if(data.equals("ASTO"))
			result = "从背包中取物品放入仓库";
		else if(data.equals("AUST"))
			result = "从仓库中取出物品放回背包";
		else if(data.equals("OPENACCOUNTDEPOT"))
			result = "开启珍宝阁";
		else if(data.equals("CARDPUNCH"))
			result = "打卡";
		else if(data.equals("fixFail"))
			result = "坐骑合成失败";
		else if(data.equals("HORSEIMGCHANGE"))
			result = "坐骑幻化";
		else if(data.equals("HEU"))
			result = "马装备解绑";
		else if(data.equals("HORSEFIX"))
			result = "坐骑合成";
		else if(data.equals("HORSEUPLEVEL"))
			result = "坐骑升级";
		else if(data.equals("HORSEREMOVECHANGE"))
			result = "解除坐骑幻化";
		else if(data.equals("INSTANCESWEEP"))
			result = "扫荡副本";
		else if(data.equals("JEWELUPGRADE"))
			result = "宝石升级";
		else if(data.equals("STEPBATTLE"))
			result = "跨服";
		else if(data.equals("ACCUMULATECHARGE"))
			result = "累充";
		else if(data.equals("FIRSTCHARGE"))
			result = "首充";
		else if(data.equals("FEASTRESULT"))
			result = "满汉全席结果";
		else if(data.equals("GAMBLEREWARD"))
			result = "商城积分抽奖";
		else if(data.equals("CONVOYFAIL"))
			result = "押镖失败";
		else if(data.equals("ESCORT"))
			result = "开始个人押镖";
		else if(data.equals("convoy"))
			result = "个人押镖奖励";
		else if(data.equals("VIPCYCLEINSTANCE"))
			result = "VIP领取荣誉塔物品";
		else
			result = data;

		return result;
	}

}
