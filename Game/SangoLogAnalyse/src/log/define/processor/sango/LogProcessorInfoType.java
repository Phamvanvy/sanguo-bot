package log.define.processor.sango;

import log.define.CommonUtil;
import log.ConverStringToByte;
import log.define.Definings;
import log.define.processor.LogProcessor;

public class LogProcessorInfoType extends LogProcessor {

	public LogProcessorInfoType(String id) {
		super(id);
	}

	@Override
	public String process(String data) {
		String result = null;
		StringBuilder ssb = new StringBuilder();
		if(!data.contains("(")){
			result = data;
			if(data.contains(" ")){
				ConverStringToByte convert = new ConverStringToByte();
				byte[] attachment = convert.hexStringToBytes(data);
				if(attachment[0]==1 && attachment.length == 10){//ITEM
					String itemId = String.valueOf(CommonUtil.getInt(attachment, 1));
					String instanceId = String.valueOf(CommonUtil.getInt(attachment, 5));
					String count = String.valueOf(attachment[9]);
					result = "物品ID："+itemId+"；物品名称："+Definings.getItemName(itemId)+"；物品instanceId："+instanceId+"；个数："+count;
				}else if (attachment[0] == 2 && attachment.length == 5) { // money
					String count =String.valueOf(CommonUtil.getInt(attachment, 1));
					result = "金钱："+count;
				}
			}
		    ssb.append(result);
		}
		char[] ch = data.toCharArray();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ch.length; i++) {
			sb.append(ch[i]);
			if (ch[i] == ')') {
				sb.append('|');
			}
		}
		data = sb.toString();
		String[] ss = data.split("\\|");
		for (int i = 0; i < ss.length; i++) {
			String s = ss[i];
			if (s.startsWith("LVL")) {
				String content = getContent("LVL(", s, ")");
				result = "等级(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("MAP")) {
				String content = getContent("MAP(", s, ")");
				result = "地图ID(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("X")) {
				String content = getContent("X(", s, ")");
				result = "X(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("Y")) {
				String content = getContent("Y(", s, ")");
				result = "Y(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("MEY")) {
				String content = getContent("MEY(", s, ")");
				result = "金钱(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("EXP")) {
				String content = getContent("EXP(", s, ")");
				result = "经验(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("IME")) {
				String content = getContent("IME(", s, ")");
				result = "I币(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("HNR")) {
				String content = getContent("MAP(", s, ")");
				result = "声望(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("CRD")) {
				String content = getContent("CRD(", s, ")");
				result = "战功(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("CLS")) {
				String content = getContent("CLS(", s, ")");
				if (content.equals("0")) {
					content = "武将";
				} else if (content.equals("1")) {
					content = "刺客";
				} else if (content.equals("2")) {
					content = "谋士";
				} else if (content.equals("3")) {
					content = "方士";
				}
				result = "职业(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("HP")) {
				String content = getContent("HP(", s, ")");
				result = "血量(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("MP")) {
				String content = getContent("MP(", s, ")");
				result = "蓝量(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("MHP")) {
				String content = getContent("MHP(", s, ")");
				result = "最大血量(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("MMP")) {
				String content = getContent("MMP(", s, ")");
				result = "最大蓝量(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("AGI")) {
				String content = getContent("AGI(", s, ")");
				result = "体力(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("STR")) {
				String content = getContent("STR(", s, ")");
				result = "力量(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("INT")) {
				String content = getContent("INT(", s, ")");
				result = "智力(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("STA")) {
				String content = getContent("STA(", s, ")");
				result = "敏捷(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("APU")) {
				String content = getContent("APU(", s, ")");
				result = "攻击力上限(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("APD")) {
				String content = getContent("APD(", s, ")");
				result = "攻击力下限(" + content + ")";
				ssb.append(result);
			}  else if (s.startsWith("SPOW")) {
				String content = getContent("SPOW(", s, ")");
				result = "法功(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("SHEAL")) {
				String content = getContent("SHEAL(", s, ")");
				result = "法功(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("DEF")) {
				String content = getContent("DEF(", s, ")");
				result = "护甲(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("SDEF")) {
				String content = getContent("SDEF(", s, ")");
				result = "法防(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("CRI")) {
				String content = getContent("CRI(", s, ")");
				result = "物暴(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("SCRI")) {
				String content = getContent("SCRI(", s, ")");
				result = "法暴(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("HIT")) {
				String content = getContent("HIT(", s, ")");
				result = "命中(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("SHIT")) {
				String content = getContent("SHIT(", s, ")");
				result = "法命(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("DODGE")) {
				String content = getContent("DODGE(", s, ")");
				result = "闪避(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("SDODGE")) {
				String content = getContent("SDODGE(", s, ")");
				result = "法闪(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("ANT")) {
				String content = getContent("ANT(", s, ")");
				result = "免暴(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("ANT")) {
				String content = getContent("ANT(", s, ")");
				result = "免暴(" + content + ")";
				ssb.append(result);
			} else if(s.startsWith("MEY(")){
				String content = getContent("MEY(", s, ")");
				result = "金钱(" + content + ")";
				ssb.append(result);
			} else if(s.startsWith("IME(")){
				String content = getContent("IME(",s,")");
				result = "I币(" + content + ")";
				ssb.append(result);
			} else if (s.startsWith("EQU(")) {
				result = processEquip(s);
				ssb.append(result);
			} else if (s.startsWith("ITM(")) {
				result = processItem(s);
				ssb.append(result);
			} else if (s.startsWith("HOS(")) {
				result = processHos(s);
				ssb.append(result);
			}
		}
		return ssb.toString();
	}

	/**
	 * 玩家的装备信息
	 * @param args
	 */
	public String processEquip(String ss) {
		StringBuilder sb = new StringBuilder();
		if (ss.startsWith("EQU(")) {
			int sIndex = ss.lastIndexOf("EQU(");
			int cLength = "EQU(".length();
			String tString = ss.substring(sIndex + cLength, ss.length());
			int eIndex = tString.indexOf(")");
			String ssString = tString.substring(0, eIndex);
			sb.append("装备(");
			String[] str = ssString.split(",");
			String itemId = str[0];
			sb.append("装备id：" + itemId + "；装备名称："+Definings.getEquipmentName(itemId)+"；");
			String instanceId = str[1];
			sb.append("装备instanceId：" + instanceId + "；");
			if (str.length > 2) {
				for (int i = 2; i < str.length; i++) {
					String se = str[i];
					if (se.startsWith("S=")) {
						String star = se.substring(2);
						sb.append("星级鉴定：" + star + "；");
					} else if (se.startsWith("H=")) {
						String hole = se.substring(2);
						sb.append("打孔数：" + hole + "；");
					} else if (se.startsWith("JEW=")) {
						String[] jewStrings = se.substring(4).split("\\+");
						// int[] jewels = new int[jewStrings.length];
						for (int j = 0; j < jewStrings.length; j++) {
							String jewel = jewStrings[j];
							String name = Definings.getItemName(jewel);
							sb.append("宝石id：" + jewel + "；宝石名称："+name+"；");
						}
					} else if (se.startsWith("NR=")) {
						String naturalsString = se.substring(3);
						sb.append("资质鉴定：" + naturalsString + "；");
					} else if (se.startsWith("MK=")) {
						String mkString = se.substring(3);
						sb.append("刻的字为：" + mkString + "；");
					}
				}
			}
			sb.append(")");
		}
		return sb.toString();
	}

	/**
	 * 玩家的物品信息
	 * @param args
	 */
	public String processItem(String data) {
		StringBuilder sb = new StringBuilder();
		if (data.startsWith("ITM(")) {
			sb.append("物品(");
			String content = getContent("ITM(", data, ")");
			String[] str = content.split(",");
			if (str.length == 3) {
				String id = str[0];
				String instanceId = str[1];
				String count = str[2];
				sb.append("物品ID：" + id + "; 物品名称：" + Definings.getItemName(id)
						+ "; instanceId：" + instanceId + "; 物品个数：" + count
						+ "个");
			} else if (str.length == 5) {
				String id = str[0];
				String instanceId = str[1];
				String count = str[2];
				String cno = str[3].substring(4);
				String value = str[4].substring(4);
				sb.append("物品ID：" + id + "; 物品名称：" + Definings.getItemName(id)
						+ "; instanceId：" + instanceId + "; 物品个数：" + count
						+ "个；" + "元宝卡号：" + cno + "；i币数：" + value);
			}
			sb.append(")");
		}
		return sb.toString();
	}

	/**
	 * 处理坐骑信息
	 * @param args
	 */
	// HOS(TID=10,NM=青聪马,IID=582557,LVL=16,SKLS=6946817+7143425)
	public String processHos(String data) {
		StringBuilder sb = new StringBuilder();
		if (data.startsWith("HOS(")) {
			sb.append("坐骑信息(");
			String ss = getContent("HOS(", data, ")");
			String[] str = ss.split(",");
			for (int i = 0; i < str.length; i++) {
				if (str[i].startsWith("TID=")) {
					sb
							.append("坐骑类型："
									+ Definings.getHorseName(str[i]
											.substring(4)) + "；");
				} else if (str[i].startsWith("NM=")) {
					sb.append("坐骑名称：" + str[i].substring(3) + "；");
				} else if (str[i].startsWith("IID=")) {
					sb.append("坐骑instanceID：" + str[i].substring(4) + "；");
				} else if (str[i].startsWith("LVL=")) {
					sb.append("坐骑等级：" + str[i].substring(4) + "；");
				} else if (str[i].startsWith("SKLS=")) {
					sb.append("坐骑技能：");
					String s = str[i].substring(5);
					String[] st = s.split("\\+");
					for (int j = 0; j < st.length; j++) {
						int ski = Integer.parseInt(st[j]);
						int skil = ski >> 16;
						String skillId = String.valueOf(skil);
						sb.append("技能Id：" + skillId + " ,技能名称："
								+ Definings.getSkillName(skillId) + " ");
						if (j < st.length - 1) {
							sb.append(",");
						}
					}
					sb.append("；");
				}
			}
		} 
		return sb.toString();
	}

	public String getContent(String startData, String tempString, String endData) {
		int startIndex = tempString.lastIndexOf(startData);
		int charLength = startData.length();
		String temString = tempString.substring(startIndex + charLength,
				tempString.length());
		int endIndex = temString.indexOf(endData);
		String ss = temString.substring(0, endIndex);
		return ss;
	}
}
