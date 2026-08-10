package canseereaditem;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;

public class PetMenu extends MenuType {	
	
	public static final String[] STR_PETTYPE = {"力量","智力","敏捷","体力","智力体力","力量敏捷"};
	
	public PetMenu() {
		type = MENU_PET;
		titleName=new String[]{"卖宠查询","宠物逃跑","宠物合成","宠物重铸","宠物修炼","宠物灵性","宠物悟性","技能重生","技能锁定","宠物放生","喂宠异常","宠物变色","宠物解锁","奥运会","宠物进化"};
	}

	@Override
	public int checkMessage(String s, int k) {
		int result=0;
		if (k == TYPE_SELL_BUY_PET && (s.contains("SellPet,Price")|| s.contains("BuyPet,Price"))) {
			result = TYPE_SELL_BUY_PET;
		}else if(TYPE_PET_LOST == k && s.contains("RunAway")){
			result = TYPE_PET_LOST;
		}else if(k == TYPE_PET_SYNTHETIZE && (s.contains("SynthetizePetID")||s.contains("RemovePet MainPetID") ||s.contains("addPet fail") )){
			result = TYPE_PET_SYNTHETIZE;//宠物合成
		}else if(k == TYPE_PET_RECASTINGPROPERTY && s.contains("PetRecastingProperty") ){
			result = TYPE_PET_RECASTINGPROPERTY;//宠物重铸
		}else if(k == TYPE_PET_PRACTICE && s.contains("getPetPractice Pet")){
			result = TYPE_PET_PRACTICE;//宠物修炼
		}else if(k == TYPE_PET_SPIRITUALITY && s.contains("tryAddPetPoint") && s.contains("SpiritualityLevel")){
			result = TYPE_PET_SPIRITUALITY;//宠物灵性
		}else if(k == TYPE_PET_PERCEPTION && s.contains("tryAddPetPoint") && s.contains("PerceptionPoint")){
			result = TYPE_PET_PERCEPTION; //宠物悟性
		}else if(k == TYPE_PET_SKILLREBIRTH && s.contains("extend_pet_skill_lock") && s.contains("Rebirth")){
			result = TYPE_PET_SKILLREBIRTH;
		}else if(k == TYPE_PET_SKILLLOCK && s.contains("extend_pet_skill_lock") && (s.contains("Lock skill")||s.contains("Unlock"))){
			result = TYPE_PET_SKILLLOCK;
		}else if(k == TYPE_PET_THROWPET && s.contains("throw Pet")){
			result = TYPE_PET_THROWPET;//宠物放生
		}else if(k == TYPE_PET_FEEDERROR && s.contains("feed") && s.contains("not found")){
			result = TYPE_PET_FEEDERROR;
		}else if(k == TYPE_PET_CHANGECOLOR && s.contains("change pet color")){
			result = TYPE_PET_CHANGECOLOR;
		}else if(k == TYPE_PET_DEBLOCK && s.contains("removeitem [") && s.contains("removeCount [")){
			result = TYPE_PET_DEBLOCK;
		}else if(k == TYPE_PET_RACE && (s.contains("Race number is[") || s.contains("Race number["))){
			result = TYPE_PET_RACE;
		}else if(k == TYPE_PET_EVOLUTION && (s.contains("evolution") || s.contains("Evolution"))){
			result = TYPE_PET_EVOLUTION;
		}
		return result;
	}

	@Override
	public String splitMessage(String s, int index, int i) {
		String strStart="";
		if (TYPE_SELL_BUY_PET == index && i == TYPE_SELL_BUY_PET) {
			if (s.contains("]Dest[")){
				strStart="]Dest[";
			}else if (s.contains("]Source[")){
				strStart="]Source[";
			}
		}else if(index == TYPE_PET_PRACTICE && i == TYPE_PET_PRACTICE){
			if(s.contains("getPetPractice PetID[")){
				strStart="getPetPractice PetID[";
			}
		}else if((index == TYPE_PET_SPIRITUALITY && i == TYPE_PET_SPIRITUALITY) || (index == TYPE_PET_PERCEPTION && i==TYPE_PET_PERCEPTION)){
			if(s.contains("tryAddPetPoint Pet[")){
				strStart="tryAddPetPoint Pet[";
			}
		}else if((index == TYPE_PET_SKILLREBIRTH && i == TYPE_PET_SKILLREBIRTH) || (index == TYPE_PET_SKILLLOCK && i == TYPE_PET_SKILLLOCK) ){
			if(s.contains("extend_pet_skill_lock Pet[")){
				strStart="extend_pet_skill_lock Pet[";
			}
		}else if(index == TYPE_PET_FEEDERROR && i == TYPE_PET_FEEDERROR){
			strStart="petID[";
		}else if(index == TYPE_PET_CHANGECOLOR && i == TYPE_PET_CHANGECOLOR){
			strStart="petID[";
		}else if(index == TYPE_PET_THROWPET && i == TYPE_PET_THROWPET){
			strStart="throw Pet[";
		}else if(index == TYPE_PET_DEBLOCK && i == TYPE_PET_DEBLOCK){
			strStart="removeCount [";
		}else if(index == TYPE_PET_RACE && i == TYPE_PET_RACE){
			strStart="Race number[";
		}
		return strStart;
	}
	
	@Override
	public boolean parseMessage(String s, String tmps, String strID, int index)throws IOException {
		if(strID.length()>0 && index!=TYPE_PET_LOST){
			writeText(strID +" ",false);
		}
		switch(index){
		case TYPE_SELL_BUY_PET://买卖宠物记录
			try {
				int playerId = Integer.parseInt(tmps);
				//tmps = splitMessage(s, -1, -1);
				String price = readitem.splitMessageSP(s, "Price[", defaultEnd);
				if (s.contains("]Dest[")){
					writeText(" 出售宠物： ", false);
					writeText(" 卖给玩家[" + playerId + "] 价格[" + price +"] ", false);
					getPetId(s,false);
				}else if (s.contains("]Source[")){
					writeText(" 购买宠物： ", false);
					writeText("从玩家[" + playerId + "]买到    价格["+ price +"] ", false);
					getPetId(s,true);
				}
			} catch (Exception e) {
			}
			break;
		case TYPE_PET_LOST://宠物逃跑信息
			petRunaway(tmps);
			break;
		case TYPE_PET_SYNTHETIZE: //宠物合成
			if(s.contains("SynthetizePetID")){
				String petID =readitem.splitMessageSP(s, "SynthetizePetID[", defaultEnd);
				if(s.contains("AddPet") && s.contains("SUCCESS")){
					writeText("获得的合成宠物ID[" + petID+"] ",false);
				}else if(s.contains("MainPetID") && s.contains("SecondPetID")){
					String mainPetID =readitem.splitMessageSP(s, "MainPetID[", defaultEnd);
					String secondPetID=readitem.splitMessageSP(s, "SecondPetID[", " SynthetizePetID["); 
					writeText("成功进行宠物合成。   主宠ID["+mainPetID+"] 副宠ID["+secondPetID+"] 合成出的宠物ID["+petID+"] ",false);
				}
			}else if(s.contains("RemovePet MainPetID")){
				String mainPetID =readitem.splitMessageSP(s, "MainPetID[", defaultEnd);
				String secondPetID=readitem.splitMessageSP(s, "SecondPetID[", defaultEnd); 
				if(s.contains("TRY")){
					writeText("将要移除  主宠ID["+mainPetID+"] 副宠ID["+secondPetID+"] ",false);
				}else if(s.contains("SUCCESS")){
					writeText("成功移除  主宠ID["+mainPetID+"] 副宠ID["+secondPetID+"] ",false);
				}
			}else if(s.contains("addPet fail")){
				writeText("移除参与合成的宠物失败。",false);
			}
			break;
		case TYPE_PET_RECASTINGPROPERTY: //宠物重铸
			String petID=readitem.splitMessageSP(s, "PetID[", defaultEnd);
			if(s.contains("TRY")){
				writeText("将要进行宠物重铸。宠物ID["+petID+"] 重铸前",false);
			}else if(s.contains("SUCCESS")){
				writeText("成功进行宠物重铸。宠物ID["+petID+"] 重铸后 ",false);
			}
			String str="力量["+readitem.splitMessageSP(s, "strength[", defaultEnd)+"] ";
			String agi="敏捷["+readitem.splitMessageSP(s, "agility[", defaultEnd)+"] ";
			String vit="体力["+readitem.splitMessageSP(s, "vitality[", defaultEnd)+"] ";
			String inte="智力["+readitem.splitMessageSP(s, "intelligence[", defaultEnd)+"] ";
			String point="属性点["+readitem.splitMessageSP(s, "currentPoint[", defaultEnd)+"] ";
			writeText("  属性："+str+agi+vit+inte+point,false);
			break;
		case TYPE_PET_PRACTICE://宠物修炼
			if(s.contains("TRY") || s.contains("SUCCESS")){
				String curPercetionPoint="当前悟性点数["+readitem.splitMessageSP(s, "curPetPercetionPoint[", defaultEnd)+"] ";//悟性点数
				String percetionLevel = "悟性等级["+readitem.splitMessageSP(s, "PetPercetionLevel[", defaultEnd)+"] ";
				String time="修炼时间["+readitem.splitMessageSP(s, "RealPracticeTime[", defaultEnd)+"] ";
				String addPoint="获得的悟性点数["+readitem.splitMessageSP(s, "Add PetPerceptionPoint[", defaultEnd)+"] ";
				String sp="";
				if(s.contains("TRY")){
					writeText("将要提取修炼宠物ID["+tmps+"] ",false);
					sp="能够";
				}else if(s.contains("SUCCESS")){
					writeText("成功提取修炼宠物ID["+tmps+"] ",false);
					sp="成功";
				}
				writeText(curPercetionPoint+percetionLevel+time+sp+addPoint,false);
			}else if(s.contains("RealPracticeTime Less Than 1 hour.")){
				writeText("修炼宠物ID["+tmps+"] 修炼时间小于1小时，没有属性变化。",false);
			}
			break;
		case TYPE_PET_SPIRITUALITY://宠物灵性提升
			String level="当前灵性等级["+readitem.splitMessageSP(s, "SpiritualityLevel[", defaultEnd)+"] ";
			writeText("宠物ID["+tmps+"]  ",false);
			if(s.contains("TRY")){
				writeText("尝试提升宠物灵性。 ",false);
			}else if(s.contains("FAILURE")){
				writeText("灵性提升失败，灵性",false);
				if(s.contains("No Change")){
					writeText("没有变化。  ",false);
				}else if(s.contains("reduce 1")){
					writeText("降低 1 级。  ",false);
				}
			}else if(s.contains("used item SUCCESS")){
				writeText("灵性提升成功，将要同步到客户端。 ",false);
			}else if(s.contains("Add SUCCESS")){
				writeText("灵性提升成功并已同步到客户端。",false);
			}
			writeText(level,false);
			break;
		case TYPE_PET_PERCEPTION://宠物悟性
			level="当前悟性等级["+readitem.splitMessageSP(s, "PerceptionLevel[", defaultEnd)+"] ";
			point="悟性点数["+readitem.splitMessageSP(s, "PerceptionPoint[", defaultEnd)+"] ";
			writeText("宠物ID["+tmps+"]  ",false);
			if(s.contains("Add SUCCESS")){
				writeText("悟性提升成功。   ",false);
			}else if(s.contains("Add TRY")){
				writeText("尝试提升宠物悟性。  ",false);
			}
			writeText(level+point,false);
			break;
		case TYPE_PET_SKILLREBIRTH: //技能重生
			str="";
			if(s.contains("TRY")){
				str="准备进行技能重生！ ";
			}else if(s.contains("SUCCESS")){
				str="成功完成技能重生！ ";
			}else if(s.contains("FAILURE")){
				str="技能重生失败！没有足够的重生宝典！ ";
			}
			if(str.length()>0){
				writeText(str + " ",false);
				getPetSkillLock(getdata(tmps));
			}else{
				return false;
			}
			break;
		case TYPE_PET_SKILLLOCK: //技能锁定、解锁
			str="";
			if(s.contains("Lock skill")){
				if(s.contains("Lock skill TRY")){
					str="尝试锁定技能！ ";
				}else if(s.contains("Lock skill SUCCESS!")){
					str="成功锁定技能！ ";
				}else if(s.contains("Lock skill FAILURE!The ability has been locked")){
					str="锁定技能失败！此技能已被锁定！ ";
				}else if(s.contains("Lock skill FAILURE!The maximum number of locks")){
					str="锁定技能失败！技能锁定数量已达上限！ ";
				}else if(s.contains("Lock skill FAILURE!Do not have the lock")){
					str="锁定技能失败！没有足够的小锁头！ ";
				}
			}else if(s.contains("Unlock")){
				if(s.contains("Unlock TRY")){
					str="尝试解锁技能！";
				}else if(s.contains("Unlock SUCCESS")){
					str="技能解锁成功！";
				}
			}
			if(str.length()>0){
				writeText(str + " ",false);
				String strSkillID="";
				if(s.contains("The ability has been locked")){
					strSkillID=readitem.splitMessageSP(s, "skillId[", defaultEnd);
				}else{
					strSkillID=readitem.splitMessageSP(s, "skillID[", defaultEnd);
				}
				int skillID =0; 
				try {
					skillID=Integer.parseInt(strSkillID);
				} catch (Exception e) {
					return false;
				}
				String skillName = Skills.get(skillID);
				if(skillName!=null)
					writeText(" 技能["+skillName+"] ",false);
				getPetSkillLock(getdata(tmps));
			}else{
				return false;
			}
			break;
		case TYPE_PET_THROWPET:
			readPet(getdata(tmps));
			break;
		case TYPE_PET_FEEDERROR:
			writeText(" 宠物ID[" + tmps +"] 喂养失败，未找到该宠物。",false);
			break;
		case TYPE_PET_CHANGECOLOR:
			parseChangeColor(s,tmps);
			break;
		case TYPE_PET_DEBLOCK:
			int needitemID = Integer.parseInt(readitem.splitMessageSP(s, "removeitem [", defaultEnd));
			writeText(" 宠物解锁  使用物品[",false);
			if(items.containsKey(needitemID)){
				writeText(items.get(needitemID) +"] ",false);
			}else{
				writeText(needitemID + "] ",false);
			}
			writeText("使用个数[" + tmps + "] ",false);
			break;
		case TYPE_PET_RACE:
			String[] rabbitName = {
				"小蓝兔子", "小粉兔子", "小黄兔子", "两只兔子", "三只兔子"	
			};
			if(s.indexOf("Race number is") >= 0){
				int raceNum = Integer.parseInt(readitem.splitMessageSP(s, "Race number is[", "]"));
				String playerName= readitem.splitMessageSP(s, "Name[", "]");
				int jettonNum= Integer.parseInt(readitem.splitMessageSP(s, "His jettonNum is[", "]"));
				int voteType = Integer.parseInt(readitem.splitMessageSP(s, "Vote type is[", "]"));
				int allVote = Integer.parseInt(readitem.splitMessageSP(s, "His all jettonNum is[", "]"));
				writeText(" 兔子大赛第[" + raceNum + "]场 玩家[" + playerName + "] 投了[" + jettonNum + "]注在[" + rabbitName[voteType] + "]上，他在这只兔子上总共投注了[" + allVote + "]注。", false);
				count += jettonNum;
			}else{
				if(s.indexOf("WinType is") < 0) break;
				int raceNum = Integer.parseInt(readitem.splitMessageSP(s, "Race number[", "]"));
				int winType = Integer.parseInt(readitem.splitMessageSP(s, "WinType is", "ID"));;
				int playerID = Integer.parseInt(readitem.splitMessageSP(s, "ID[", "]"));
				String playerName= readitem.splitMessageSP(s, "Name[", "]");
				boolean isWin = true;
				if(s.indexOf("don't win, get email which have") >= 0){
					isWin = false;
				}
				int money = isWin ? Integer.parseInt(readitem.splitMessageSP(s, "win, get email which have [", "]")) :
					Integer.parseInt(readitem.splitMessageSP(s, "don't win, get email which have [", "]"));
				int rabbit0Num = Integer.parseInt(readitem.splitMessageSP(s, "jettonNum[0] is[", "]"));
				int rabbit1Num = Integer.parseInt(readitem.splitMessageSP(s, "jettonNum[1] is[", "]"));
				int rabbit2Num = Integer.parseInt(readitem.splitMessageSP(s, "jettonNum[2] is[", "]"));
				int rabbit3Num = Integer.parseInt(readitem.splitMessageSP(s, "jettonNum[3] is[", "]"));
				int rabbit4Num = Integer.parseInt(readitem.splitMessageSP(s, "jettonNum[4] is[", "]"));
				writeText(" 兔子大赛第[" + raceNum + "]场 [" + rabbitName[winType] + "]获胜了! 玩家ID" + playerID + "] 玩家[" + playerName + "] " +
						(isWin ? "投中获得了[" : "输了返回[") + money + "] 依次投注的注数为[" + rabbit0Num + "][" 
						+ rabbit1Num + "][" + rabbit2Num + "][" + rabbit3Num + "]["+ rabbit4Num + "]", false);
				count += money;
			}
			break;
		case TYPE_PET_EVOLUTION:
			if(s.indexOf("open") >= 0){
				int divine= Integer.parseInt(readitem.splitMessageSP(s, "divine[", "]"));
				writeText(" 占卜翻开了[" + divine + "]点。", false);
			}else if(s.indexOf("goon") >= 0){
				int divine= Integer.parseInt(readitem.splitMessageSP(s, "divine[", "]"));
				writeText(" 继续占卜获得了[" + divine + "]点。", false);
			}else if(s.indexOf("save") >= 0){
				int divine= Integer.parseInt(readitem.splitMessageSP(s, "divine[", "]"));
				int type= Integer.parseInt(readitem.splitMessageSP(s, "type[", "]"));
				writeText(" 保存占卜之力[" + divine + "]点，类型为[" + type + "]。", false);
			}else if(s.indexOf("use item evolution luck") >= 0){
				writeText(" 使用改运物品成功。", false);
			}else if(s.indexOf("luck") > 0){
				writeText(" 尝试改运。", false);
			}
			break;
		default :
			return false;
		}
		return true;
	}

	/**
	 * @param s
	 * @return获得宠物id
	 * @throws IOException
	 */
	public static void getPetId(String s,boolean buyflag) throws IOException {
		
		int TYPE_0 = 1;
	    int TYPE_1 = 2;
	    int TYPE_2 = 3;
	    int TYPE_3 = 4;
	    int TYPE_4 = 5;
	    int TYPE_5 = 6;
	    
		int petId = 0;
		int startIndex = s.lastIndexOf("Changed[");
		int charLength = "Changed[".length();
		String temString = s.substring(startIndex + charLength, s.length());
		int endIndex = temString.indexOf("]");
		temString = temString.substring(0, endIndex);
		byte[] bytes = getdata(temString);
		
		processorChanged changed =  new processorChanged();
		String output = changed.print(bytes);
		output=output.replace("金钱", "金钱变化");
		
		writeText( output , false);
//		ByteArrayInputStream biss = new ByteArrayInputStream(bytes);
//		DataInputStream dos = new DataInputStream(biss);
//		// 跳过金钱
//		dos.readByte();
//		dos.readShort();
//		dos.readInt();
//		// 读宠物id
//		dos.readByte();
//		dos.readByte();
//		petId = dos.readInt();
//		writeText(" 宠物id为[" + petId + "]", false);
		
		
		
//		dos.readInt();
//		String name = dos.readUTF();		//宠物的名字
//		writeText(" 宠物的名字为[" + name + "]", false);
//		byte byteType = dos.readByte();		// 宠物的类型
//		String typeStr = "";
//		if(byteType == TYPE_0){
//			typeStr = "力量";
//		}
//		if(byteType == TYPE_1){
//			typeStr = "智力";
//		}
//		if(byteType == TYPE_2){
//			typeStr = "敏捷";
//		}
//		if(byteType == TYPE_3){
//			typeStr = "体力";
//		}
//		if(byteType == TYPE_4){
//			typeStr = "智力体力";
//		}
//		if(byteType == TYPE_5){
//			typeStr = "力量敏捷";
//		}
//		writeText(" 宠物的类型为["+typeStr+"]", false);
//		boolean isbaby = dos.readBoolean();
//		if(isbaby){
//			writeText(" 是一个宠物宝宝", false);
//		}
//		short level = dos.readShort();		//级别
//		writeText(" 宠物的级别"+level, false);
//		
//		int exp = dos.read();
//		writeText(" 宠物的经验"+exp, false);
//		dos.readShort();	//currentPoint
//		dos.readShort();	//point
//		dos.readByte();		//favor
//		short agility = dos.readShort();		//敏捷
//		writeText(" 敏捷"+agility, false);
//		
//		short strength = dos.readShort();		//力量
//		writeText(" 力量"+strength, false);
//		
//		short vitality = dos.readShort();		//体力
//		writeText(" 体力"+vitality, false);
//		
//		short intelligence = dos.readShort();	//智力
//		writeText(" 智力"+intelligence, false);
//		
//		int hp = dos.readInt();			//血量
//		writeText(" 血量"+hp, false);
//		
//		int mp = dos.readInt();
//		writeText(" 蓝量"+hp, false);
//		
//		int size = dos.readInt();
//		writeText(" 宠物的技能有：", false);
//		for(int i = 0; i < size; i ++){
//			short id = dos.readShort();
//			if(Skills.containsKey(id)){
//				String skillname = Skills.get(id);
//				writeText(skillname, false);
//			}
//		}
	}
	
	public static String petRunaway(String s) throws IOException{
		String string = null;
		writeText("玩家", false);
		int startIndex = s.lastIndexOf("Player [");
		int charLength = "Player [".length();
		String temString = s.substring(startIndex + charLength, s.length());
		int endIndex = temString.indexOf("]");
		string = temString.substring(0, endIndex);
		writeTextln(string, false);
		writeTextln("宠物", false);
		startIndex = s.lastIndexOf("] Pet [");
		charLength = "] Pet [".length();
		temString = s.substring(startIndex + charLength, s.length());
		endIndex = temString.indexOf("]");
		
		string = temString.substring(0, endIndex);
		byte[] petbyte = getdata(string);
		getPetInfo(petbyte);
		writeTextln("       逃跑", false);
		
		return string;
	}
	
	/**
	 * @param tmp
	 * @throws IOException
	 * 输出宠物明细
	 */
	public static void getPetInfo(byte[] tmp)throws IOException {
		ByteArrayInputStream biss = new ByteArrayInputStream(tmp);
		DataInputStream dos = new DataInputStream(biss);
		//byte b = dos.readByte();
		Integer itemId = dos.readInt();
		
		writeTextln("宠物物品id:" + itemId.toString(), false);
		Integer id = dos.readInt();
		writeTextln("宠物id:" + id.toString(), false);
		String name = dos.readUTF();
		writeTextln("宠物名称:" + name, false);
		Byte tempByte = dos.readByte();
		
		if(tempByte < STR_PETTYPE.length)
			writeTextln("宠物类型" + STR_PETTYPE[tempByte], false);
		
		Boolean tempBoolean = dos.readBoolean();
		writeTextln("是否宝宝 :" + tempBoolean.toString() , false);
		Short level = dos.readShort();
		writeTextln("宠物级别:" + level.toString(), false);
		
		Integer exp = dos.readInt();
		writeTextln("当前经验值",false);
		writeTextln(exp.toString(), false);
		
		Short currentPoint = dos.readShort();
		writeTextln("当前剩余点数", false);
		writeTextln(currentPoint.toString(), false);
		
		Short point = dos.readShort();
		writeTextln("当前点数", false);
		writeTextln(point.toString(), false);
		
		Byte favor = dos.readByte();
		writeTextln("忠诚度", false);
		writeTextln(favor.toString(), false);
		
		Short agility = dos.readShort();
		writeTextln("敏捷", false);
		writeTextln(agility.toString(), false);
		
		Short strength = dos.readShort();
		writeTextln("力量", false);
		writeTextln(strength.toString(), false);
		
		Short vitality = dos.readShort();
		writeTextln("体力", false);
		writeTextln(vitality.toString(), false);
		
		Short intelligence = dos.readShort();
		writeTextln("智力", false);
		writeTextln(intelligence.toString(), false);
		
		Integer hp = dos.readInt();
		writeTextln("hp", false);
		writeTextln(hp.toString(), false);
		
		Integer mp = dos.readInt();
		writeTextln("mp", false);
		writeTextln(mp.toString(), false);
		
		Byte ablilitessSize = dos.readByte();
		writeTextln("学会技能", false);
		writeTextln(ablilitessSize + "个分别是", false);
		for(int i = 0; i < ablilitessSize; i++){
			Short skillId = dos.readShort();
			writeTextln(skillId.toString() + " ", false);
		}
		
		Integer maxEnchancePoint = dos.readInt();
		writeTextln("最多可精炼宠物属性数量", false);
		writeTextln(maxEnchancePoint.toString(), false);
		
		//跳过已经废弃的力智敏体属性
		dos.readInt();
		dos.readInt();
		dos.readInt();
		dos.readInt();
		String enchanceName = dos.readUTF();
		writeTextln("精炼名称", false);
		writeTextln(enchanceName, false);
		
		Integer currentEnchancePoint = dos.readInt();
		writeTextln("当前宠物精炼数", false);
		writeTextln(currentEnchancePoint.toString(), false);
		
		for(int i = 0; i < currentEnchancePoint; i++){
			Integer property = dos.readInt();
			writeTextln("精炼的属性为", false);
			String str = null;
			if(property == 1){
				str = "力量";	
			}else if(property == 2){
				str = "智力";	
			}else if(property == 3){
				str = "体力";	
			}else if(property == 4){
				str = "智力";	
			}
			writeTextln(str, false);
		}
		
		
		Byte version = dos.readByte(); //这个跳过
		
		Short equSize = dos.readShort();
		for(int i = 0; i < equSize; i++){
			Byte part = dos.readByte();
			writeTextln("    装备部位", false);
			writeTextln(part.toString(), false);
			
			Byte equipInfo = dos.readByte();
			if(equipInfo == 1){
				int equId = dos.readInt();
				writeText("装备物品为" + equs.get(equId), false);
				if (equs.containsKey(equId)) {
					writeText("  发送装备物品名称为" + equs.get(equId), false);
					readitem.writeEQU(dos);
				}
			}else{
				writeTextln("空", false);
			}
		}
		
/*		 dos.writeInt(itemId);
         dos.writeInt(id);
         dos.writeUTF(name);
         dos.writeByte(petType);
         dos.writeBoolean(baby);
         dos.writeShort(level);
         dos.writeInt(exp);
         dos.writeShort(currentPoint);
         dos.writeShort(point);
         dos.writeByte(favor);
         dos.writeShort(agility);
         dos.writeShort(strength);
         dos.writeShort(vitality);
         dos.writeShort(intelligence);
         dos.writeInt(hp);
         dos.writeInt(mp);
         dos.writeByte(abilities.size());
         for (int i = 0; i < abilities.size(); i++) {
             Ability ability = (Ability) abilities.get(i);
             dos.writeShort(ability.getId());
         }
         dos.writeInt(maxEnchancePoint);
         //以下修改为实际属性值存储
         dos.writeInt(enhancestrength);
         dos.writeInt(enhanceintelligence);
         dos.writeInt(enhancevitality);
         dos.writeInt(enhanceagility);
         dos.writeUTF(enhanceName);
         dos.writeInt(currentEnchancePoint);
         for(int i=0;i<currentEnchancePoint;i++){
         	dos.writeInt(petEnhances.get(i).getProperty());
         }
         //宠物装备信息
         //byte equversion = 3;
         //byte equversion = 4;           //items version 4  增加鉴定
         //byte equversion = 5;				//items version 5 装备刻字
         byte equversion = 6;	               //items version 6 增加宝石系统
         dos.writeByte(equversion);
         short size = 9;
         dos.writeShort(size);
         for (int ii = 0; ii < size; ii++) {
         	dos.writeByte(ii);//part记录部位
         	if (usedEquipments[ii] == null){
         		dos.writeByte(usedEquinfo[ii]);//是否可装备 2 0
         	}else{
         		dos.writeByte(1);
         		Grid grid = (Grid) usedEquipments[ii];
                 if (grid != null) {
                     IEquipment equ = (IEquipment) grid.item;
                     dos.write(equ.toDbBytes());
                 }
         	}
         }
         return bos.toByteArray();*/
			


	}
	
	public static void readPet(byte[] tmp)throws IOException {
		String tmpStr[] = {"力量","智力","敏捷","体力","智力体力","力量敏捷"};
		ByteArrayInputStream biss = new ByteArrayInputStream(tmp);
		DataInputStream dos = new DataInputStream(biss);
		//byte b = dos.readByte();
		int value =0;
		value = dos.readInt();
		
		writeTextln(" 宠物物品id:" + value, false);
		value = dos.readInt();
		writeTextln("	宠物id:" + value, false);
		String name = dos.readUTF();
		writeTextln("	宠物名称:" + name, false);
		Byte tempByte = dos.readByte();
		if(tempByte < tmpStr.length)
			writeTextln("	宠物类型" + tmpStr[tempByte], false);
		
		Boolean tempBoolean = dos.readBoolean();
		writeTextln("	是否宝宝 :" + tempBoolean.toString() , false);
		value = dos.readShort();
		writeTextln("	宠物级别:" + value, false);
		value = dos.readInt();
		writeTextln("	当前经验值："+value ,false);
		value = dos.readInt();
		writeTextln("	经验值："+value ,false);
		
		value = dos.readShort();
		writeTextln("	当前剩余点数：" + value, false);
		value = dos.readShort();
		writeTextln("	当前点数：" + value, false);
		
		value = dos.readByte();
		writeTextln("	忠诚度：" + value, false);
		
		value = dos.readShort();
		writeTextln("	力量：" + value, false);
		
		value = dos.readShort();
		writeTextln("	敏捷：" + value, false);
		
		value = dos.readShort();
		writeTextln("	体力："+value, false);
		
		value = dos.readShort();
		writeTextln("	智力：" + value, false);
		
		value = dos.readInt();
		writeTextln("	hp："+value,false);
		
		value = dos.readInt();
		writeTextln("	mp："+value,false);
		
		value = dos.readInt();
		writeTextln("	灵性："+value,false);
		
		value = dos.readShort();
		writeTextln("	悟性等级："+value,false);
		
		value = dos.readInt();
		writeTextln("	悟性经验："+value,false);
         
         //绑定状态
		byte bindType = dos.readByte();
		String str_BindType = bindType==0?"一代宠":"二代宠";
		writeTextln("	" + str_BindType,false);
		tempBoolean = dos.readBoolean();
		//writeTextln("绑定：" + tempBoolean.toString(),false);
		//0头；1项链；2甲；3腰带；4护腕；5戒指；6鞋；7武器；8盾牌；
		value = dos.readShort();
		byte[] usedEquinfo = new byte[value];
		for(int i = 0; i< value; i++){
			usedEquinfo[i] = dos.readByte();
		}
		
		value = dos.readByte();
		if(value>0){
			writeText("技能：" ,false);
			for(int i = 0;i<value;i++){
				int abilityID = dos.readShort();
				if(Skills.get(abilityID)!=null){
					writeText(" "+ Skills.get(abilityID),false);
				}else{
					writeText(" " + abilityID,false);
				}
			}
			
		}
	}
	
	/**
	 * 解析宠物技能锁定、解锁、重生
	 * @param tmp
	 * @throws IOException
	 */
	public static void getPetSkillLock(byte[] tmp) throws IOException{
		ByteArrayInputStream biss = new ByteArrayInputStream(tmp);
		DataInputStream dos = new DataInputStream(biss);
		int itemID = dos.readInt();
		int id=dos.readInt();//实例ID
		String name=dos.readUTF();
		writeTextln("宠物ID["+itemID+"] 实例ID["+id+"] 名称["+name+"] ",false);
		dos.readByte();
		dos.readBoolean();
		dos.readShort();
		int len=dos.readByte();
		writeTextln("宠物技能： ",false);
		for(int i=0;i<len;i++){
			int skillID = dos.readShort();
			byte lock= dos.readByte();
			String skillName=Skills.get(skillID);
			String strLock=lock==0?" 未锁定 ":" 已锁定 ";
			writeTextln("      名称["+skillName+"] "+strLock,false);
		}
	}
	
	public static void parseChangeColor(String s, String tmps){
		int petType = Integer.parseInt(readitem.splitMessageSP(s, "petType[", defaultEnd)) - 1;
		int bindType = Integer.parseInt(readitem.splitMessageSP(s, "bindType[", defaultEnd));
		String str_BingType = bindType==0?"一代宠":"二代宠";
		int curColor = Integer.parseInt(readitem.splitMessageSP(s, "curColor[", defaultEnd));
		String[] str_PetColor = petColor.get(petType+"_" + bindType);
		writeText(" 宠物ID[" + tmps + "] 宠物类型[" + STR_PETTYPE[petType] + "型] [" + str_BingType + "] 当前颜色[" + str_PetColor[curColor] + "] ",false);
		int newColor = 0;
		if(!s.contains("Random Remove Item FAILURE")){
			newColor = Integer.parseInt(readitem.splitMessageSP(s, "newColor[", defaultEnd));
		}
		String itemName = "";
		if(s.contains("itemID[")){
			int tmpID = Integer.parseInt(readitem.splitMessageSP(s, "itemID[", defaultEnd));
			itemName = items.get(tmpID);
		}
		if(s.contains("Random TRY")){
			writeText(" 随机颜色[" + str_PetColor[newColor] +"] 等待玩家确认是否变更颜色",false);
		}else if(s.contains("Random Remove Item FAILURE")){
			writeText(" 移除随机颜色生成符失败！",false);
		}else if(s.contains("Random SUCCESS")){
			writeText(" 随机颜色[" + str_PetColor[newColor] +"] 变色成功！",false);
		}else if(s.contains("Random PLAYER GIVEUP")){
			writeText(" 随机颜色[" + str_PetColor[newColor] +"] 玩家放弃随机颜色！",false);
		}else if(s.contains("Fixed TRY")){
			writeText(" 指定颜色[" + str_PetColor[newColor] +"] 道具[" + itemName + "] 准备扣除玩家身上的" + itemName + "并进行变色！",false);
		}else if(s.contains("Fixed SUCCESS")){
			writeText(" 指定颜色[" + str_PetColor[newColor] +"] 道具[" + itemName + "] 变色成功！",false);
		}
		
		
	}
}
