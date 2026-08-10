package canseereaditem;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class EquipMenu extends MenuType {
	public static int spMark = 0;
	public EquipMenu() {
		type = MENU_EQUIP;
		titleName=new String[]{"分解非周年装","分解周年装","丢弃、使用物品查询","鉴定物品","宝石合成","扣除澡票","打怪掉落","打造物品","装备打孔","镶嵌宝石","摘除宝石","获得礼品","删除装备","装备换荣誉","定向包"
				,"宝石升级","使用永久提升属性道具","砸蛋获得物品查询","宝石置换","宝石融合"};
	}

	@Override
	public int checkMessage(String s, int k) {
		int result=0;
		if (k == TYPE_BREAK_NOMAL_EQU && s.contains("unhencePlainEquip Ok")) {// 非周年分解搜所
			result = TYPE_BREAK_NOMAL_EQU;
		} else if (k == TYPE_BREAK_ZHOUNIAN_EQU && s.contains("unhenceYearEquip Ok") ) {// 周年解析
			result = TYPE_BREAK_ZHOUNIAN_EQU;
		} else if (k == TYPE_USE_LOST_ITEM && s.contains("TYPE[33],SubType[") && s.contains("Item[")) {//丢弃、使用物品
			result = TYPE_USE_LOST_ITEM;
		} else if(k == TYPE_DIAMOND && s.contains("] diamond itemId [")){//鉴定物品
			result = TYPE_DIAMOND;
		}else if(k == TYPE_GEM && s.contains("diamondMosaic") && s.contains("product itemId")){//宝石合成
			result = TYPE_GEM;
		}else if(k == TYPE_REMOVE_BATH && s.contains("] completeRemoveBathItem[")){//扣除澡票
			result = TYPE_REMOVE_BATH;
		}else if(k == TYPE_DROP_ITEM && s.contains("TYPE[52],MgId[") && s.contains("]SubType[end]Changed[")
				&& (s.contains("]Money[") || s.contains("] Total Money["))){//打怪掉落
			result = TYPE_DROP_ITEM;
		}else if(k == TYPE_PRODUCE && s.contains("Produce") && s.contains("] reciveType[2]") && s.contains("OK")){//打造物品
			result = TYPE_PRODUCE;
		}else if(k == TYPE_EQU_DRILLING && s.contains("extend_diamondMosaic") && s.contains("Drilling")){//装备打孔
			result = TYPE_EQU_DRILLING;
		}else if(k == TYPE_EQU_MOSAIC && s.contains("extend_diamondMosaic") && s.contains("Mosaic")){//镶嵌宝石
			result = TYPE_EQU_MOSAIC;
		}else if(k == TYPE_EQU_EXCISE && s.contains("extend_diamondMosaic") && s.contains("Excise")){//摘除宝石
			result = TYPE_EQU_EXCISE; //摘除
		}else if(k == TYPE_GIFT && s.contains("TYPE[81],GiftGroup") && s.contains("GifeDefine")){//获得礼品
			result = TYPE_GIFT;
		}else if(k == TYPE_REMOVE_EQU && s.contains("Remove EQU ID") && s.contains("current version[")){
			result = TYPE_REMOVE_EQU;
		}else if(k == TYPE_EQU_EXCHANGECREDIT && s.contains("ExchangeCredit")){
			result = TYPE_EQU_EXCHANGECREDIT;
		}else if(k == TYPE_GETITEM_DROPGROUP && s.contains("GetDropGroup")){
			result = TYPE_GETITEM_DROPGROUP;
		}else if(k == TYPE_AUTOMIX && s.contains("extend_diamondMosaic") && s.contains("AUTOMIXTURE")){//宝石升级
			result = TYPE_AUTOMIX;
		}else if(k == TYPE_ADDATTRIBUTE && s.contains("AddAttribute")){
			result = TYPE_ADDATTRIBUTE;
		}else if(k == TYPE_EGG_GETITEM && s.contains("Egg") && s.contains("Get Item itemid")){
			result = TYPE_EGG_GETITEM;
		}else if(k == TYPE_DIAMOND_REPLACE && s.contains("DiamondReplace")){//宝石置换
			result = TYPE_DIAMOND_REPLACE;
		}else if(k == TYPE_DIAMOND_DEVELOP && s.contains("diamond_develop")){
			result = TYPE_DIAMOND_DEVELOP;
		}
		return result;
	}

	@Override
	public String splitMessage(String s, int index, int i) {
		String strStart="";
		if (TYPE_BREAK_NOMAL_EQU == index && i == TYPE_BREAK_NOMAL_EQU) {
			strStart="itemsid[";
		}else if (TYPE_BREAK_ZHOUNIAN_EQU == index && i == TYPE_BREAK_ZHOUNIAN_EQU) {
			strStart="itemsid[";
		}else if (TYPE_USE_LOST_ITEM == index && i == TYPE_USE_LOST_ITEM) {
			strStart="Item[";
		}else if (TYPE_REMOVE_BATH == index && i == TYPE_REMOVE_BATH) {//使用澡票
			strStart="] completeRemoveBathItem[";
		}else if((index == TYPE_EQU_DRILLING && i == TYPE_EQU_DRILLING) || (index == TYPE_EQU_MOSAIC && i == TYPE_EQU_MOSAIC)
				|| (index == TYPE_EQU_EXCISE && i == TYPE_EQU_EXCISE)){//打孔，镶嵌，摘除
			if(s.contains("extend_diamondMosaic Equipment[")){
				strStart="extend_diamondMosaic Equipment[";
			}else if(s.contains("extend_diamondMosaic Old Equipment[")){
				strStart="extend_diamondMosaic Old Equipment[";
			}
		}else if(index == TYPE_GIFT && i == TYPE_GIFT){//获得礼品
			strStart="GiftGroup[";
		}else if(index == TYPE_REMOVE_EQU && i == TYPE_REMOVE_EQU){//移除装备
			strStart="ItemData[";
		}else if(index == TYPE_EQU_EXCHANGECREDIT && i == TYPE_EQU_EXCHANGECREDIT){
			strStart="equID[";
		}else if(index == TYPE_GETITEM_DROPGROUP && i == TYPE_GETITEM_DROPGROUP){
			strStart="RemoveItem ID[";
		}else if(index == TYPE_AUTOMIX && i == TYPE_AUTOMIX){
			strStart="Changed[";
		}else if(index == TYPE_ADDATTRIBUTE && i == TYPE_ADDATTRIBUTE){
			strStart="playerAttr[";
		}else if(index == TYPE_EGG_GETITEM && i == TYPE_EGG_GETITEM){
			strStart="Changed[";
		}else if(index == TYPE_DIAMOND_REPLACE && i == TYPE_DIAMOND_REPLACE){//宝石置换
			strStart="count[";
		}
//		else if(index == TYPE_DIAMOND_DEVELOP && i == TYPE_DIAMOND_DEVELOP){//宝石融合
//			strStart="";
//		}
		return strStart;
	}
	
	@Override
	public boolean parseMessage(String s, String tmps, String strID, int index)
			throws IOException {
		if(index != TYPE_DROP_ITEM){
			writeText(strID + " ",false);
		}
		switch(index){
		case TYPE_BREAK_NOMAL_EQU:// 匹配分解非周年装备
			int equId = Integer.parseInt(tmps);
			writeText(" 分解的非周年装id为[" + equId + "] 装备名称为", false);
			if (equs.containsKey(equId)) {
				writeText(equs.get(equId), false);
			}
			writeText("     装备星数为[" + readitem.getEquCount(s) + "]",false);
			writeEnhace(s);
			break;
		case TYPE_BREAK_ZHOUNIAN_EQU:// 匹配周年装开始
			equId = Integer.parseInt(tmps);
			writeText(" 分解的周年装id为[" + equId + "] 装备名称为", false);
			if (equs.containsKey(equId)) {
				writeText(equs.get(equId), false);
			}
			break;
		case TYPE_USE_LOST_ITEM:
			if(s.endsWith("TRY")){
				return false;
			}
			if (s.contains("],SubType[1]")){
				writeText(" 使用 ", false);
			}else if (s.contains("],SubType[2]")){
				writeText(" 丢弃 ", false);
			}else if (s.contains("],SubType[3]")){
				writeText(" 给宠物使用 ", false);
			}
			if (tmps.equals("empty") || tmps.equals(" ")|| tmps.equals("null")) {
				// writeTextln("未找到附件");
				writeText("无",false);
			} else {
				String count = readitem.getEquCount(s);
				getDropItem(getdata(tmps),count);
			}
			break;
		case TYPE_DIAMOND://装备鉴定
			splitDiamondCout(tmps);
			break;
		case TYPE_GEM://宝石信息
			splitDiamond(tmps);
			break;
		case TYPE_REMOVE_BATH://扣除澡票
			String name = items.get(Integer.parseInt(tmps));
			writeText(" 使用了一张"+name,false);
			break;
		case TYPE_DROP_ITEM://打怪掉落物品记录
			killMG(tmps);
			break;
		case TYPE_PRODUCE://打造物品
			showProduce(s);
			break;
		case TYPE_EQU_DRILLING://打孔
			String str = parseEquDrilling(s);
			if(str.length()>0){
				writeText(str,false);
				if(!tmps.equals(s)){
					getDropItem(getdata(tmps),"1");
				}
			}else{
				return false;
			}
			break;
		case TYPE_EQU_MOSAIC://镶嵌     2月21日-3月29日的日志解析不完整   Mosaic TRY 被写成Excise TRY
			str =parseEquMosaic(s);
			if(str.length()>0){
				writeText(str,false);
				if(!tmps.equals(s)){
					getDropItem(getdata(tmps),"1");
				}
			}else{
				return false;
			}
			break;
		case TYPE_EQU_EXCISE: //摘除   2月21日-3月29日的日志解析不完整   Excise TRY 与镶嵌重复
			str=parseEquExcise(s);
			if(str.length()>0){
				writeText(str,false);
				if(!tmps.equals(s)){
					getDropItem(getdata(tmps),"1");
				}
			}else{
				return false;
			}
			break;
		case TYPE_GIFT://获得礼品
			parseGift(s,tmps);
			break;
		case TYPE_REMOVE_EQU://删除装备
			parseRemoveEqu(s,tmps);
			break;
		case TYPE_EQU_EXCHANGECREDIT://装备换荣誉
			parseEquExchangeCredit(s,tmps);
			break;
		case TYPE_GETITEM_DROPGROUP://使用宝石定向包
			parseGetItemFromDropGroup(s,tmps);
			break;
		case TYPE_AUTOMIX://宝石升级
			parseAutoMix(s,tmps);
			break;
		case TYPE_ADDATTRIBUTE: //使用永久提示属性的道具
			parseAddAttribute(s, tmps);
			break;
		case TYPE_EGG_GETITEM://砸蛋获得物品查询
			parseEggGetItem(s,tmps);
			break;
		case TYPE_DIAMOND_REPLACE://宝石置换
			parseDiamondReplace(s,tmps);
			break;
		case TYPE_DIAMOND_DEVELOP:
			parseDiamondDevelop(s);
			break;
		default :
			return false;
		}
		return true;
	}
	
	/**
	 * 显示打造信息
	 * @param s
	 */
	public static void showProduce(String s){
		int produceID=0;
		int type =0;
		try {
			produceID = Integer.parseInt(readitem.splitMessageSP(s,"recipeId[","]"));
			type = Integer.parseInt(readitem.splitMessageSP(s,"produceType[","]"));
		} catch (Exception e) {
			writeText("日志错误",false);
			return;
		}
		if(recipesNew.containsKey(produceID)){
			writeText("打造配方ID["+ produceID +"]  配方类型["+produceTypeName[type]+"]  配方名称["+recipesNew.get(produceID)+"]  ",false);
		}
		
	}
	/**
	 * @param tmp
	 *            写下丢弃、使用的物品
	 * @throws IOException
	 */
	public static void getDropItem(byte[] tmp,String count) throws IOException {
		ByteArrayInputStream biss = new ByteArrayInputStream(tmp);
		DataInputStream dos = new DataInputStream(biss);
		
		int itemId = dos.readInt();
		if (equs.containsKey(itemId)) {
			writeText("装备物品ID为[" + itemId + "]", false);
			writeText("  装备物品名称为[" + equs.get(itemId)+"]", false);
			readitem.writeEQU(dos);
		}else if (items.containsKey(itemId)) {
			writeText("物品ID为[" + itemId + "]", false);
			writeText("  物品名称为[" + items.get(itemId)+"]", false);
			if (count !=null){
				writeText(" 个数为：" + count + "个", false);
			}
		}
	}
	
	/**
	 * @param s
	 * @return 打怪掉落
	 * @throws IOExecption
	 */
	public static String killMG(String s) throws IOException{
		String string = null;
		if(s.contains("]Money[") && s.contains("Battle Fail") == false){
			writeText("客户端战斗：", false);
		}else if(s.contains("Total Money[") || s.contains("Battle Fail")){	
			writeText("服务器战斗：", false);
		}
		writeText("玩家ID[", false);
		int startIndex = s.lastIndexOf("ID[");
		int charLength = "ID[".length();
		String temString = s.substring(startIndex + charLength, s.length());
		int endIndex = temString.indexOf("],TYPE[52],MgId");
		string = temString.substring(0, endIndex);
		writeText(string, false);
		
		writeText("] 怪物[", false);
		startIndex = s.lastIndexOf("MgId[");
		charLength = "MgId[".length();
		temString = s.substring(startIndex + charLength, s.length());
		endIndex = temString.indexOf("]SubType[end]");
		string = temString.substring(0, endIndex);
		writeText(string, false);
		
		if(s.contains("]Money[")){
			writeText("] 金币余额[", false);
			startIndex = s.lastIndexOf("]Money[");
			charLength = "]Money[".length();
			temString = s.substring(startIndex + charLength, s.length());
			endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			writeText(string +"]", false);
		}else if(s.contains("Total Money[")){
			string = readitem.splitMessageSP(s, "Total Money[", defaultEnd);
			writeText("] 金币余额["+string+"]",false);
		}
		
		startIndex = s.lastIndexOf("]Changed[");
		charLength = "]Changed[".length();
		temString = s.substring(startIndex + charLength, s.length());
		if(s.contains("]Money[")){
			endIndex = temString.indexOf("]Money[");
		}else if(s.contains("Total Money[")){
			endIndex = temString.indexOf("] Total Money[");
		}
		string = temString.substring(0, endIndex);
		if(s.endsWith("Battle Fail")){
			writeText(" 服务器战斗失败，",false);
		}
		if(string.equals("empty")){
			writeText(" 无掉落和耐久变化。",false);
		}else{
			byte[] itembyte = getdata(string);
			processorChanged changed =  new processorChanged();
			String output = changed.print(itembyte);
			if(!output.startsWith("耐久变化")){
				writeText(" 掉落：", false);
			}
			writeTextln(output + " ", false);
		}
		return string;
	}
	
	/**
	 * @param 获取精炼次数并写下属性
	 */
	public static void writeEnhace(String s) {
		int startIndex = s.lastIndexOf("count[");
		int charLength = "count[".length();
		String temString = s.substring(startIndex + charLength, s.length());
		int endIndex = temString.indexOf("]");
		String string = temString.substring(endIndex + 1, temString.length());
		int enhenceSpace = ",[ ".length();
		endIndex = string.indexOf("]");
		string = string.substring(enhenceSpace, endIndex);
		String propertyString = string;
		// 此时输出精炼的属性
		int j = 0;
		writeText("   精炼过的属性依次为", false);
		while (string.length() <= 2 || (j = string.indexOf(" ")) != -1) {
			int k = 0;
			if (string.length() <= 2) {
				j = string.length();
			}
			String property = string.substring(k, j);
			j++;
			if (j != (string.length() + 1)) {
				string = string.substring(j, string.length());
			} else {
				string = "333";
			}
			writeEnhanceProperty(property);
		}
		// 输出属性点
		string = propertyString;
		writeText("   精炼过的属性点依次为", false);
		while (string.length() <= 2 || (j = string.indexOf(" ")) != -1) {
			int k = 0;
			if (string.length() <= 2) {
				j = string.length();
			}
			String property = string.substring(k, j);
			j++;
			if (j != (string.length() + 1)) {
				string = string.substring(j, string.length());
			} else {
				string = "333";
			}
			writeText(" " + property, false);
		}

	}

	/**
	 * @param property写下精炼属性中文
	 */
	public static void writeEnhanceProperty(String property) {

		int propertyId = Integer.parseInt(property);
		switch (propertyId) {
		case 1:
			writeText(" 体力", false);
			break;
		case 2:
			writeText(" 智力", false);
			break;
		case 3:
			writeText(" 力量", false);
			break;
		case 4:
			writeText(" 敏捷", false);
			break;
		case 5:
			writeText(" 物攻", false);
			break;

		case 6:
			writeText(" 魔攻", false);
			break;
		case 7:
			writeText(" 物防", false);
			break;
		case 8:
			writeText(" 魔防", false);
			break;
		case 9:
			writeText(" 命中", false);
			break;

		case 10:
			writeText(" 闪避", false);
			break;
		case 11:
			writeText(" 物爆", false);
			break;
		case 12:
			writeText(" 魔爆", false);
			break;

		default:
			break;
		}
	}

	/**
	 * @param s
	 * @return 装备的宝石信息
	 * @throws IOExecption
	 */
	public static String splitDiamond(String s) throws IOException{
		String string = null;
		if(s.contains("diamondMosaic sucess playerId [")){//宝石合成成功
			int startIndex = s.lastIndexOf("product itemId [");
			int charLength = "product itemId [".length();
			String temString = s.substring(startIndex + charLength, s.length());
			int endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			writeText("制作", false);
			int itemId = Integer.parseInt(string);
			if (items.containsKey(itemId)) {
				writeText(items.get(itemId), false);
			}
			writeText("    使用 ", false);
			
			startIndex = s.lastIndexOf("] useItem [");
			charLength = "] useItem [".length();
			temString = s.substring(startIndex + charLength, s.length());
			endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			int id = Integer.parseInt(string);
			if (items.containsKey(id)) {
				writeText(items.get(id), false);
			}
			
			writeText("    数量 ", false);
			
			startIndex = s.lastIndexOf("] count [");
			charLength = "] count [".length();
			temString = s.substring(startIndex + charLength, s.length());
			endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			writeText(string, false);
			writeText("个    和道具", false);
			startIndex = s.lastIndexOf("] needItem [");
			charLength = "] needItem [".length();
			temString = s.substring(startIndex + charLength, s.length());
			endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			id = Integer.parseInt(string);
			if (items.containsKey(id)) {
				writeText(items.get(id), false);
			}
			
			writeText(" 成功", false);
		}else if(s.contains("diamondMosaic fail playerId [") ||s.contains("diamondMosaic fail2 playerId [") ){
			int startIndex = s.lastIndexOf("product itemId [");
			int charLength = "product itemId [".length();
			String temString = s.substring(startIndex + charLength, s.length());
			int endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			writeText("制作", false);
			int itemId = Integer.parseInt(string);
			if (items.containsKey(itemId)) {
				writeText(items.get(itemId), false);
			}
			writeText("    使用 ", false);
			
			startIndex = s.lastIndexOf("] useItem [");
			charLength = "] useItem [".length();
			temString = s.substring(startIndex + charLength, s.length());
			endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			int id = Integer.parseInt(string);
			if (items.containsKey(id)) {
				writeText(items.get(id), false);
			}
			
			writeText("    数量 ", false);
			
			startIndex = s.lastIndexOf("] count [");
			charLength = "] count [".length();
			temString = s.substring(startIndex + charLength, s.length());
			endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			
			writeText(string, false);
			writeText("个    和道具", false);
			startIndex = s.lastIndexOf("] needItem [");
			charLength = "] needItem [".length();
			temString = s.substring(startIndex + charLength, s.length());
			endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			id = Integer.parseInt(string);
			if (items.containsKey(id)) {
				writeText(items.get(id), false);
			}
			if(s.contains("fail2")){
				writeText("  宝石合成失败", false);
			}else{
				writeText("  宝石扣除失败", false);
			}
		}
		return string;
	}
	/**
	 * @param s
	 * @return 星级鉴定 只算鉴定成功和失败的
	 * @throws IOException
	 */
	public static String splitDiamondCout(String s) throws IOException{
		String string = null;
		if(s.contains("] end")){
			int startIndex = s.lastIndexOf("diamond itemId [");
			int charLength = "diamond itemId [".length();
			String temString = s.substring(startIndex + charLength, s.length());
			int endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			writeTextln("精炼装备", false);
			int itemId = Integer.parseInt(string);
			if (equs.containsKey(itemId)) {
				writeText(equs.get(itemId), false);
			}

			if (items.containsKey(itemId)) {
				writeText(items.get(itemId), false);
			}
			
			startIndex = s.indexOf("] id [");
			charLength = "] id [".length();
			temString = s.substring(startIndex + charLength, s.length());
			endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			writeTextln("   装备id ", false);
			writeTextln(string, false);
			writeTextln("   鉴定方式 ", false);
			
			startIndex = s.indexOf("] type [");
			charLength = "] type [".length();
			temString = s.substring(startIndex + charLength, s.length());
			endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			
			int type =Integer.parseInt(string);
			//byte type = data.readByte();//0为金钱，1为普通鉴定道具，2为搞定鉴定道具，3为顶级鉴定道具
			if(type == 0){
				writeTextln("使用金钱进行鉴定       鉴定结果 ", false);
			}else if(type == 1){
				writeTextln("使用普通鉴定道具进行鉴定       鉴定结果   ", false);
			}else if(type == 2){
				writeTextln("使用高级鉴定道具进行鉴定       鉴定结果   ", false);
			}else if(type == 3){
				writeTextln("使用顶级鉴定道具进行鉴定       鉴定结果  ", false);
			}
			
			startIndex = s.indexOf("] dimaondcount [");
			charLength = "] dimaondcount [".length();
			temString = s.substring(startIndex + charLength, s.length());
			endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			writeTextln(string, false);
			writeTextln("钻", false);
			
		}else if(s.contains("] remove fail;")){
			int startIndex = s.lastIndexOf("diamond itemId [");
			int charLength = "diamond itemId [".length();
			String temString = s.substring(startIndex + charLength, s.length());
			int endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			writeTextln("精炼装备", false);
			int itemId = Integer.parseInt(string);
			if (equs.containsKey(itemId)) {
				writeText(equs.get(itemId), false);
			}

			if (items.containsKey(itemId)) {
				writeText(items.get(itemId), false);
			}
			
			startIndex = s.indexOf("] id [");
			charLength = "] id [".length();
			temString = s.substring(startIndex + charLength, s.length());
			endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			writeTextln("   装备id ", false);
			writeTextln(string, false);
			writeTextln("   鉴定方式 ", false);
			
			startIndex = s.indexOf("] type [");
			charLength = "] type [".length();
			temString = s.substring(startIndex + charLength, s.length());
			endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			
			int type =Integer.parseInt(string);
			//byte type = data.readByte();//0为金钱，1为普通鉴定道具，2为搞定鉴定道具，3为顶级鉴定道具
			if(type == 0){
				writeTextln("使用金钱进行鉴定 ", false);
			}else if(type == 1){
				writeTextln("使用普通鉴定道具进行鉴定       鉴定结果  失败", false);
			}else if(type == 2){
				writeTextln("使用高级鉴定道具进行鉴定       鉴定结果  失败", false);
			}else if(type == 3){
				writeTextln("使用顶级鉴定道具进行鉴定        鉴定结果  失败", false);
			}
		}
		return string;
	}
	
	/**
	 * 解析礼品
	 * @param s
	 * @param giftGroupID
	 */
	public static void parseGift(String s, String giftGroupID){
		String gifeDefine = readitem.splitMessageSP(s, "GifeDefine[", defaultEnd);
		writeText("掉落组ID["+giftGroupID+"] ，GifeDefine[" + gifeDefine +"] ，",false);
		String strChanged = readitem.splitMessageSP(s, "Changed[", defaultEnd);
		byte[] changedByte = getdata(strChanged);
		processorChanged changed =  new processorChanged();
		String output = changed.print(changedByte);
		writeText(" 变化："+output,false);
	}
	
	/**
	 * 删除装备
	 * @param s
	 * @param itemData
	 * @throws IOException
	 */
	public static void parseRemoveEqu (String s, String itemData)throws IOException{
		String equID = readitem.splitMessageSP(s, "Remove EQU ID[", defaultEnd);
		String version = readitem.splitMessageSP(s, "current version[", defaultEnd);
		String equName = readitem.splitMessageSP(s, "name[", defaultEnd);
		writeText(" 装备ID[" + equID +"] 名称[" + equName + "] 版本[" + version + "] 装备信息[",false);
		if (itemData.equals("empty") || itemData.equals(" ")|| itemData.equals("null")) {
			writeText("无 ]",false);
		} else {
			String count = readitem.getEquCount(s);
			getDropItem(getdata(itemData),count);
			writeText(" ]",false);
		}
	}
	
	/**
	 * 装备换荣誉
	 * @param s
	 * @param equID
	 */
	public static void parseEquExchangeCredit(String s, String equID)throws IOException{
		String str="";
		String data = "";
		if(s.contains("]try")){
			str = " 尝试进行装备换荣誉";
			readitem.spMark = TYPE_EQU_EXCHANGECREDIT;
		}else if(s.contains("]OK")){
			str = " 成功完成装备换荣誉";
			if(readitem.readitemBack.length()>0){
				data = readitem.splitMessageSP(readitem.readitemBack, "ItemData[", defaultEnd);
			}
		}else if(s.contains("]fall")){
			str = " 装备换荣誉失败";
			readitem.readitemBack = "";
		}
		writeText(str + "，装备ID[" + equID +"] ",false);
		if(data.length() > 0){
			parseRemoveEqu(readitem.readitemBack,data);
			readitem.readitemBack = "";
		}
	}
	
	/**
	 * 装备打孔
	 * @param s
	 * @return
	 */
	public static String parseEquDrilling(String s){
		String str="";
		if(s.contains("Drilling Remove TRY")){
			if(s.contains("itemID")){ // 重写后的日志
				str="尝试扣除打孔符 ";
				int itemID = Integer.parseInt(readitem.splitMessageSP(s, "itemID[", defaultEnd));
				if(items.containsKey(itemID)){
					str=str+"["+items.get(itemID)+"] ";
				}
			}else{
				str="尝试扣除要打孔的装备和打孔符！ ";
			}
		}else if(s.contains("Drilling Remove SUCCESS")){
			if(s.contains("itemID")){// 重写后的日志
				str="成功扣除打孔符 ";
				int itemID = Integer.parseInt(readitem.splitMessageSP(s, "itemID[", defaultEnd));
				if(items.containsKey(itemID)){
					str=str+"["+items.get(itemID)+"] ";
				}
			}else{
				str="成功扣除要打孔的装备和打孔符！准备进行打孔！ ";
			}
		}else if(s.contains("Drilling Remove FAILURE")){
			if(s.contains("itemID")){// 重写后的日志
				str="扣除打孔符失败 ";
				int itemID = Integer.parseInt(readitem.splitMessageSP(s, "itemID[", defaultEnd));
				if(items.containsKey(itemID)){
					str=str+"["+items.get(itemID)+"] ";
				}
			}else{
				str="扣除要打孔的装备和打孔符失败！ ";
			}
		}else if(s.contains("Drilling TRY")){// 重写后的日志
			str="打孔成功，准备添加给玩家！  ";
		}else if(s.contains("Drilling SUCCESS") && !s.contains("Add")){//重写后的日志
			str="打孔成功！已添加给玩家！ ";
		}else if(s.contains("Drilling SUCCESS Add TRY")){
			str="打孔成功！准备添加给玩家！ ";
		}else if(s.contains("Drilling SUCCESS Add SUCCESS")){
			str="打孔完成的装备已成功添加给玩家！  ";
		}else if(s.contains("Drilling FAILURE Add TRY")){
			str="打孔失败！准备把装备重新添加给玩家！ ";
		}else if(s.contains("Drilling FAILURE Add SUCCESS")){
			str="打孔失败！装备已重新添加给玩家！ ";
		}
		return str;
	}
	
	/**
	 * 镶嵌宝石
	 * @param s
	 * @return
	 */
	public static String parseEquMosaic(String s){
		String str ="";
		if(s.contains("Mosaic Remove TRY")){
			if(s.contains("itemID")){// 重写后的日志
				str="尝试扣除要镶嵌宝石！ ";
				int itemID = Integer.parseInt(readitem.splitMessageSP(s, "itemID[", defaultEnd));
				if(items.containsKey(itemID)){
					str=str+"["+items.get(itemID)+"] ";
				}
			}else{
				str="尝试扣除要进行镶嵌的装备和宝石！  ";
			}
		}else if(s.contains("Mosaic Remove SUCCESS")){
			if(s.contains("itemID")){// 重写后的日志
				str="成功扣除要镶嵌的宝石！ ";
				int itemID = Integer.parseInt(readitem.splitMessageSP(s, "itemID[", defaultEnd));
				if(items.containsKey(itemID)){
					str=str+"["+items.get(itemID)+"] ";
				}
			}else{
				str="成功扣除要进行镶嵌的装备和宝石！准备镶嵌！   ";
			}
		}else if(s.contains("Mosaic Remove FAILURE")){
			if(s.contains("itemID")){// 重写后的日志
				str="扣除要镶嵌的宝石失败！";
				int itemID = Integer.parseInt(readitem.splitMessageSP(s, "itemID[", defaultEnd));
				if(items.containsKey(itemID)){
					str=str+"["+items.get(itemID)+"] ";
				}
			}else{
				str="扣除要进行镶嵌的装备和宝石失败！  ";
			}
		}else if(s.contains("Mosaic TRY")){// 重写后的日志
			str="镶嵌成功，准备添加给玩家！  ";
		}else if(s.contains("Mosaic SUCCESS") && !s.contains("Add")){// 重写后的日志
			str="镶嵌成功！已添加给玩家！  ";
		}else if(s.contains("Mosaic SUCCESS Add TRY")){
			str="镶嵌成功！准备添加给玩家！  ";
		}else if(s.contains("Mosaic SUCCESS Add SUCCESS")){
			str="完成镶嵌的装备已成功添加给玩家！  ";
		}
		return str;
	}
	
	/**
	 * 摘除
	 * @param s
	 * @return
	 */
	public static String parseEquExcise(String s){
		String str = "";
		if(s.contains("Excise Remove TRY")){
			if(s.contains("itemID")){// 重写后的日志
				str="尝试扣除摘除符！";
				int itemID = Integer.parseInt(readitem.splitMessageSP(s, "itemID[", defaultEnd));
				if(items.containsKey(itemID)){
					str=str+"["+items.get(itemID)+"] ";
				}
			}else{
				str="尝试扣除要摘除宝石的装备和摘除符！  ";
			}
		}else if(s.contains("Excise Remove SUCCESS")){
			if(s.contains("itemID")){// 重写后的日志
				str="成功扣除摘除符！";
				int itemID = Integer.parseInt(readitem.splitMessageSP(s, "itemID[", defaultEnd));
				if(items.containsKey(itemID)){
					str=str+"["+items.get(itemID)+"] ";
				}
			}else{
				str="成功扣除要摘除宝石的装备和摘除符！准备摘除！  ";
			}
		}else if(s.contains("Excise Remove FAILURE")){
			if(s.contains("itemID")){// 重写后的日志
				str="扣除摘除符失败！";
				int itemID = Integer.parseInt(readitem.splitMessageSP(s, "itemID[", defaultEnd));
				if(items.containsKey(itemID)){
					str=str+"["+items.get(itemID)+"] ";
				}
			}else{
				str="扣除要摘除宝石的装备装备和摘除符失败！  ";
			}
		}else if(s.contains("Excise TRY")){
			str="摘除成功，准备同步给玩家！ ";
		}else if(s.contains("Excise SUCCESS") && !s.contains("Add")){
			str="摘除成功，已成功同步给玩家！ ";
		}else if(s.contains("Excise SUCCESS Add TRY")){
			str="摘除成功！准备把装备和宝石添加给玩家！  ";
		}else if(s.contains("Excise SUCCESS Add SUCCESS")){
			str="摘除宝石的装备和宝石已成功添加给玩家！  ";
		}
		return str;
	}
	
	/**
	 * 从定向包中获得的物品
	 * @param s
	 * @param tmps
	 */
	public static void parseGetItemFromDropGroup(String s, String tmps){
		String str =" ";
		int removeItemID = Integer.parseInt(tmps);
		if(items.containsKey(removeItemID)){
			str = str + "使用[" + items.get(removeItemID) + "] ";
		}
		int itemID = Integer.parseInt(readitem.splitMessageSP(s, "GetItem[", defaultEnd));
		String count = readitem.splitMessageSP(s, "Count[", defaultEnd);
		String tmpItem = "";
		if(items.containsKey(itemID)){
			tmpItem = items.get(itemID);
		}else if(equs.containsKey(itemID)){
			tmpItem = equs.get(itemID);
		}
		if(tmpItem.length()>0){
			str = str + "获得[" + tmpItem + "] 个数[" + count +"] ";
		}
		if(s.contains("ERROR BUG")){
			str = str + " 获取失败。  ";
		}
		writeText(str, false);
	}
	
	/**
	 * 宝石升级
	 * @param s
	 * @param tmps
	 */
	public static void parseAutoMix(String s,String tmps){
		String result = "";
		String itemID = readitem.splitMessageSP(s, "itemID[", defaultEnd);
		String itemName = readitem.splitMessageSP(s, "name[", defaultEnd);
		if(s.contains("SUCCESS")){
			result = "升级成功！  ";
		}else if(s.contains("Remove FAILURE")){
			result = "宝石或合成符扣除失败。 ";
		}
		byte[] itembyte = getdata(tmps);
		processorChanged changed =  new processorChanged();
		String output = changed.print(itembyte);
		result = "宝石ID[" + itemID +"] 宝石名称[" + itemName +"] "+result +" 变化："+output; 
		writeText(result, false);
	}
	
	public static void parseAddAttribute(String s,String tmps){
		int itemID = Integer.parseInt(readitem.splitMessageSP(s, "useItemID[", defaultEnd));
		if(items.containsKey(itemID)){
			writeText(" 使用道具[" + items.get(itemID) + "] ",false);
		}
		String[] attr=tmps.split(",");
		if(attr.length<4){
			writeText("玩家属性值保存错误",false);
		}else{
			writeText("玩家属性值[",false);
			String[] attrName = {"力量","敏捷","体力","智力"};
			StringBuilder sb = new StringBuilder();
			for(int i=0;i<4;i++){
				sb.append(attrName + ":" + attr + "，");
			}
			sb.deleteCharAt(sb.length()-1);
			sb.append("] ");
			writeText(sb.toString(),false);
		}
		if(s.contains("TRY")){
			writeText(" 准备增加属性值。",false);
		}else if(s.contains("SUCCESS")){
			writeText(" 增加属性值成功！",false);
		}
	}
	
	public static void parseEggGetItem(String s,String tmps){
		String itemID=readitem.splitMessageSP(s, "itemid[", defaultEnd);
		String itemName = readitem.splitMessageSP(s, "itemName[", defaultEnd);
		writeText(" 使用砸蛋道具获得  物品ID["+ itemID + "] 物品名称[" + itemName +"] 变化：",false);
		byte[] itembyte = getdata(tmps);
		processorChanged changed =  new processorChanged();
		String output = changed.print(itembyte);
		writeText(output,false);
	}
	
	public static void parseDiamondReplace(String s,String tmps){
		writeText("宝石置换   ",false);
		if(s.contains("quality")){
			int quality = Integer.parseInt(readitem.splitMessageSP(s, "quality[", defaultEnd)) + 3; //4-6级宝石
			if(s.contains("try")){
				writeText(" 宝石等级[" + quality+ "级] 个数[" + tmps + "] 准备进行宝石置换！",false);
			}else if(s.contains("removeItemID")){
				writeText(" 宝石等级[" + quality+ "级] 个数[" + tmps + "] 扣除物品[",false);
				int removeItemID = Integer.parseInt(readitem.splitMessageSP(s, "removeItemID[", defaultEnd));
				if(items.containsKey(removeItemID)){
					writeText(items.get(removeItemID) + "] ",false);
				}else{
					writeText(removeItemID + "] ",false);
				}
			}
		}else if(s.contains("giveItemID[")){
			int giveItemID = Integer.parseInt(readitem.splitMessageSP(s, "giveItemID[", defaultEnd));
			String result = "";
			if(s.contains("buy fail send mail")){
				result = "宝石置换失败，发送邮件返回物品[";
			}else{
				result = "购买成功，获得物品[";
			}
			if(items.containsKey(giveItemID)){
				writeText(result + items.get(giveItemID) + "] 个数[" + tmps + "] ",false);
			}else{
				writeText(result + giveItemID + "] 个数[" + tmps + "] ",false);
			}
		}
	}
	
	public static void parseDiamondDevelop(String s){
		writeText("宝石融合  ",false);
		if(s.contains("itemID[")){
			String itemID = readitem.splitMessageSP(s, "itemID[", defaultEnd);
			if(items.containsKey(itemID)){
				String itemName = items.get(itemID);
				writeText("宝石ID[" + itemID + "] 名称[" + itemName+"] ", false);
			}else{
				writeText("宝石ID[" + itemID + "] ", false);
			}
			if(s.contains("Remove TRY")){
				writeText("准备移除！ ",false); 
			}else if(s.contains("Remove success")){
				writeText("移除成功！",false);
			}else if(s.contains("Remove fail")){
				writeText("移除失败！",false);
			}
		}else if(s.contains("uplevel success")){
			writeText("升级成功！",false);
		}else if(s.contains("dest_addpoint")){
			String dest_AddPoint = readitem.splitMessageSP(s, "dest_addpoint", defaultEnd);
			writeText("当前的点数："+ dest_AddPoint, false);
		}else if(s.contains("current_addpoint")){
			String current_addpoint = readitem.splitMessageSP(s, "current_addpoint", defaultEnd);
			writeText("要增加的点数：" + current_addpoint, false);
		}else if(s.contains("add after current_addpoint")){
			String curPoint = readitem.splitMessageSP(s, "add after current_addpoint", defaultEnd);
			writeText("增加后的点数：" + curPoint,false);
		}
	}
}
