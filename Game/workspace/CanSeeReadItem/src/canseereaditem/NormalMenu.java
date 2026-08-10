package canseereaditem;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class NormalMenu extends MenuType {
	
	public NormalMenu() {
		type = MENU_NORMAL;
		titleName=new String[]{"邮件操作","i币消费","聊天查询","普通商店卖东西查询","荣誉拍卖行查询","普通拍卖行查询","角色升级","完成任务","竞技场商店购物查询","仓库存入物品","百层挑战","10元i币卡","奥德赛之旅", "充值相关"};
	}

	@Override
	public int checkMessage(String s, int k) {
		int result=0;
		if(k == TYPE_MAIL && ((s.contains("Attachment[")&&(s.contains("Dest[") || s.contains("SourceId[") || s.contains("TYPE[28]"))) || s.contains("SystemMail"))){// 邮件搜索
			result = TYPE_MAIL;
		}else if (k == TYPE_I_SHOP_BUY && s.contains(" iShop Buy Item")) {//i币卖场购买记录
			result = TYPE_I_SHOP_BUY;
		}else if (TYPE_CHAT == k && s.contains("NAME") && s.contains("DESTID")&& s.contains("MESSAGE")) {//聊天记录
			result = TYPE_CHAT;
		}else if (TYPE_J_SHOP_BUY_SELL == k && s.contains("TYPE[86],SubType[2]Item[") ){//j币卖场买卖记录
			result = TYPE_J_SHOP_BUY_SELL;
		}else if(TYPE_CREDIT_AUCTION == k && s.contains("(CREDITSHOP)Auction")){//荣誉拍卖行
			result = TYPE_CREDIT_AUCTION;
		}else if(TYPE_J_AUCTION == k && (s.contains("TYPE["+AUCTION_ITEM+"]") || (s.contains("OnlyPrice")))){//普通拍卖行竞拍和上架记录
			result = TYPE_J_AUCTION;
		}else if(TYPE_LEVELUP == k && s.contains("Add Exp") && s.contains("NEWLEVEL")){
			result = TYPE_LEVELUP;
		}else if(TYPE_TASK == k && s.contains("TaskId")){
			result = TYPE_TASK;
		}else if(TYPE_ARENASHOP_BUY == k && s.contains("FIGHTSHOP")){
			result = TYPE_ARENASHOP_BUY;
		}else if(TYPE_HOME_ADDITEM == k && s.contains("SubType[6]House")){
			result = TYPE_HOME_ADDITEM;
		}else if(TYPE_BOSSRUSH == k && s.contains("BossRush")){
			result = TYPE_BOSSRUSH;
		}else if(TYPE_IMONEYCARD == k && (s.contains("iShop Buy IMoneyCard")||s.contains("Use IMoneyCard"))){
			result = TYPE_IMONEYCARD;
		}else if(k == TYPE_AWARD_BOX && (s.contains("AwardBox"))){
			result = TYPE_AWARD_BOX;
		}else if(k == TYPE_RECHARGE && (s.contains("channel："))){
			result = TYPE_RECHARGE;
		}
		return result;
	}
	
	@Override
	public String splitMessage(String s, int index, int i) {
		String strStart="";
		if (TYPE_MAIL == index && i == TYPE_MAIL) {
			if(s.contains("SystemMail")&&s.contains("attachment[")){
				strStart="attachment[";
			}else{
				strStart="Attachment[";
			}
		}else if (TYPE_I_SHOP_BUY == index && i == TYPE_I_SHOP_BUY) {
			strStart="Item[";
		}else if (TYPE_CHAT == index && i == TYPE_CHAT) {
			strStart="NAME[";
		}else if (TYPE_J_SHOP_BUY_SELL == index && i == TYPE_J_SHOP_BUY_SELL) {
			strStart="]Item[";
		}else if(TYPE_LEVELUP == index && i == TYPE_LEVELUP){
			strStart="EXP[";
		}else if(TYPE_TASK == index && i == TYPE_TASK){
			strStart="TaskId[";
		}else if(TYPE_ARENASHOP_BUY == index && i == TYPE_ARENASHOP_BUY){
			strStart="Item[";
		}else if(TYPE_HOME_ADDITEM == index && i == TYPE_HOME_ADDITEM){
			strStart="Item[";
		}else if(TYPE_BOSSRUSH == index && i == TYPE_BOSSRUSH){
			strStart="stage[";
		}else if(TYPE_IMONEYCARD == index && i == TYPE_IMONEYCARD){
			strStart="imoney[";
		}else if(TYPE_AWARD_BOX == index && i == TYPE_AWARD_BOX){
			strStart="ID[";
		}
		return strStart;
	}
	
	@Override
	public boolean parseMessage(String s, String tmps, String strID, int index) throws IOException {
		switch(index){
		case TYPE_MAIL:// 匹配邮件
			try {
				// 输出目标为谁
				if (s.contains("TYPE["+MAIL_POST+"]") && (s.contains("Dest["))) {//发送邮件
					String playId = splitMessagePlayer(s, index,MAIL_POST);
					if (playId != null && !playId.equals("")) {
						//tmps = splitMessage(s, -1, -1);
						writeText(strID + " 发送目标ID[" + Integer.parseInt(playId)
								+ "]，附件：", false);
						if (tmps.equals("empty") || tmps.equals(" ")|| tmps.equals("null")) {
							writeText("无附件", false);
						} else {
							// writeTextln("数据为" + tmps );
							getMailItem(getdata(tmps));
						}
						
					}
				}else if (s.contains("TYPE["+MAIL_GET_ATTACHMENT+"]") && (s.contains("SourceId["))) {//提取邮件附件
					String playId = splitMessagePlayer(s, index,MAIL_GET_ATTACHMENT);
					if (playId != null && !playId.equals("")) {
						if(Integer.parseInt(playId)==-1){
							playId="系统";
						}
						writeText(strID+" 提取来自ID[" + playId
								+ "]的邮件附件：", false);
						if (tmps.equals("empty") || tmps.equals(" ")|| tmps.equals("null")) {
							writeText("无附件", false);
						} else {
							// writeTextln("数据为" + tmps );
							getMailItem(getdata(tmps));
						}
					}
				}else if (s.contains("TYPE["+MAIL_DELETE+"]")) {//删除邮件
					writeText(strID+" --删除邮件， 附件： ", false);
					if (tmps.equals("empty") || tmps.equals(" ") || tmps.equals("null")) {
						writeText(" 无附件", false);
					} else {
						// writeTextln("数据为" + tmps );
						getMailItem(getdata(tmps));
					}
				}else if(s.contains("SystemMail")){ //SystemMail系统邮件
					writeText(strID+" 收到系统邮件，附件：",false);
					if (tmps.equals("empty") || tmps.equals(" ") || tmps.equals("null")) {
						writeText(" 无附件", false);
					}else{
						getMailItem(getdata(tmps));
					}
				}
				// 输出目标为谁
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case TYPE_I_SHOP_BUY:// 商店消费
			int equId = Integer.parseInt(tmps);
			writeText(strID+" 消费的物品id为[" + equId + "] 物品名称为", false);
			if (items.containsKey(equId)) {
				writeText(items.get(equId), false);
			} else if (equs.containsKey(equId)) {
				writeText(equs.get(equId), false);
			}
			String playerName = getIshopForotherName(s);
			writeText(" 消费的物品个数为[" + getIshopCount(s) + "]", false);
			if (playerName != null && !playerName.equals(" ")|| tmps.equals("null")) {
				writeText(" 赠送给玩家 " + playerName, false);
			}
			getIshop(s);
			break;
		case TYPE_CHAT://聊天记录
			writeText(strID+ " 角色名[ " + tmps +"] ", false);
			try {
				int playerid = Integer.valueOf(splitMessagePlayer(s, index,0));
				switch(playerid){
					case WORLD:{
						writeText(" 在[世界聊频道]说：        ",false);
					}
					break;
					case MAP:{
						writeText(" 在[地区聊频道]说：        ",false);
					}
					break;
					case GUILD:{
						writeText(" 在[公会聊频道]说：        ",false);
					}
					break;
					case GROUP:{
						writeText(" 在[小组聊频道]说：        ",false);
					}
					break;
					case TEAM:{
						writeText(" 在[小队聊频道]说：        ",false);
					}
					break;
					case FAVORITE:{
						writeText(" 在[圈聊频道]说：        ",false);
					}
					break;
					case CAMP:{
						writeText(" 在[阵营聊频道]说：        ",false);
					}
					break;
					case ROAR:{
						writeText(" 在[狮子吼频道]说：        ",false);
					}
					break;
					default:{
						writeText(" 对玩家 [" + playerid + "] 说：        ",false);
					}
				}
			} catch (Exception e) {
				writeText(" 对玩家 ["+ splitMessagePlayer(s, index,0)+ "] 说：        ",false);
			}
			writeText(getChatContent(s), false);
			break;
		case TYPE_J_SHOP_BUY_SELL://j币卖场买卖记录
			if (tmps.equals("empty") || tmps.equals(" ")|| tmps.equals("null")) {
				// writeTextln("未找到附件");
			} else {
				writeText(strID +" ",false);
				String count = readitem.getEquCount(s);
				EquipMenu.getDropItem(getdata(tmps),count);
			}
			writeText("  卖出的价格为" + splitMessagePlayer(s, index,0)
					+ "j", false);
			break;
		case TYPE_CREDIT_AUCTION://荣誉拍卖竞拍记录
			splitCredit(tmps);
			break;
		case TYPE_J_AUCTION://普通拍卖行竞拍和上架记录
			splitjauction(tmps);
			break;
		case TYPE_LEVELUP:
			String oldLevel = readitem.splitMessageSP(s, "OLDLEVEL[", defaultEnd);
			String newLevel = readitem.splitMessageSP(s, "NEWLEVEL[", defaultEnd);
			writeTextln(strID + " 获得了" + tmps +"点经验，从" + oldLevel + "级升到" + newLevel + "级！",false);
			break;
		case TYPE_TASK:
			splitTask(s,tmps,strID);
			break;
		case TYPE_ARENASHOP_BUY:
			String price = readitem.splitMessageSP(s, "price[", defaultEnd);
			writeText(strID + " 购买价格[" + price +"] ",false);
			EquipMenu.getDropItem(getdata(tmps),null);
			break;
		case TYPE_HOME_ADDITEM: // 家园仓库放入物品
			writeText(strID + " ",false);
			parseHomeAddItem(s,tmps);
			break;
		case TYPE_BOSSRUSH: //百层挑战
			writeText(strID + " ",false);
			String mgID = readitem.splitMessageSP(s, "MgId[", defaultEnd);
			if(s.contains("start")){
				writeText(" 开始百层挑战。 层数[" + tmps +"] 怪物组ID[" + mgID + "] ",false);
			}else if(s.contains("error")){
				writeText(" 百层挑战异常，未进入战斗。 层数[" + tmps +"] 怪物组ID[" + mgID + "] ",false);
			}else if(s.contains("win")){
				String round = readitem.splitMessageSP(s, "round[", defaultEnd);
				writeText(" 百层挑战战斗胜利！层数[" + tmps +"] 怪物组ID[" + mgID + "] 回合数[" + round + "] ",false);
			}else if(s.contains("BattleError mg null")){
				writeText("百层挑战异常，怪物组为空，未进入战斗 。 层数[" + tmps + "] ",false);
			}else if(s.contains("TRY")){
				writeText("尝试开始百层挑战。 层数[" + tmps + "] ", false);
			}
			break;
		case TYPE_IMONEYCARD:
			writeText(strID + " ",false);
			parseImoneyCard(s,tmps);
			break;
		case TYPE_AWARD_BOX:
			if(s.contains("login with email")){
				int id = Integer.parseInt(readitem.splitMessageSP(s, "ID[", "]"));
				String playerNameStr= readitem.splitMessageSP(s, "name[", "]");
				int itemId= Integer.parseInt(readitem.splitMessageSP(s, "get itemId[", "]"));
				String itemStr = null;
				if (items.containsKey(itemId)) {
					itemStr = items.get(itemId);
				} else if (equs.containsKey(itemId)) {
					itemStr = equs.get(itemId);
				}
				writeText(" 奥德赛之旅:玩家[" + playerNameStr + "] ID[" + id + "] 登录时有未领取的开奖物品，此时以邮件形式下发物品[" + itemStr + "] [" + itemId + "]。", false);
			}
			if(s.contains("use money succeed")){
				int id = Integer.parseInt(readitem.splitMessageSP(s, "ID[", "]"));
				String playerNameStr= readitem.splitMessageSP(s, "name[", "]");
//				int getBoxResultIndex= Integer.parseInt(readitem.splitMessageSP(s, "getBoxResultIndex[", "]"));
//				int itemId= Integer.parseInt(readitem.splitMessageSP(s, "ItemID[", "]"));
				writeText(" 奥德赛之旅:玩家[" + playerNameStr + "] ID[" + id + "] 消费成功，钱已扣除。", false);
			}
			if(s.contains("give player itemId")){
				if(s.contains("player itemIdArray")){
					int id = Integer.parseInt(readitem.splitMessageSP(s, "ID[", "]"));
					String playerNameStr= readitem.splitMessageSP(s, "name[", "]");
					for(int idIndex = 0; idIndex < 10; idIndex++){
						int itemId= Integer.parseInt(readitem.splitMessageSP(s, "itemId" + idIndex + "[", "]"));
						String itemStr = null;
						if (items.containsKey(itemId)) {
							itemStr = items.get(itemId);
						} else if (equs.containsKey(itemId)) {
							itemStr = equs.get(itemId);
						}
						int indexStr = idIndex + 1;
						writeTextln(" 奥德赛之旅:玩家[" + playerNameStr + "] ID[" + id + "]第 " + indexStr + "次获得开奖物品[" + itemStr + "] [" + itemId + "]。", false);
					}
					
				}else{
					int id = Integer.parseInt(readitem.splitMessageSP(s, "ID[",
							"]"));
					String playerNameStr = readitem.splitMessageSP(s, "name[",
							"]");
					// int getBoxResultIndex=
					// Integer.parseInt(readitem.splitMessageSP(s,
					// "getBoxResultIndex[", "]"));
					int itemId = Integer.parseInt(readitem.splitMessageSP(s,
							"itemId[", "]"));
					String itemStr = null;
					if (items.containsKey(itemId)) {
						itemStr = items.get(itemId);
					} else if (equs.containsKey(itemId)) {
						itemStr = equs.get(itemId);
					}
					// writeText(" 奥德赛之旅:玩家[" + playerNameStr + "] ID[" + id +
					// "] 生成显示的图片索引为[" + getBoxResultIndex + "]物品为[" + itemStr +
					// "] [" + itemId + "]，此时未下发物品给玩家。", false);
					writeText(" 奥德赛之旅:玩家[" + playerNameStr + "] ID[" + id
							+ "]获得物品为[" + itemStr + "] [" + itemId
							+ "]，此时未下发物品给玩家。", false);
				}
			}
			if(s.contains("getAwardBoxItemId not null")){
				
				int id = Integer.parseInt(readitem.splitMessageSP(s, "ID[", "]"));
				String playerNameStr= readitem.splitMessageSP(s, "name[", "]");
//				int getBoxResultIndex= Integer.parseInt(readitem.splitMessageSP(s, "getBoxResultIndex[", "]"));
				int itemId= Integer.parseInt(readitem.splitMessageSP(s, "itemId[", "]"));
				String itemStr = null;
				if (items.containsKey(itemId)) {
					itemStr = items.get(itemId);
				} else if (equs.containsKey(itemId)) {
					itemStr = equs.get(itemId);
				}
				writeText(" 奥德赛之旅:玩家[" + playerNameStr + "] ID[" + id + "] 由于扣钱延时，直接生成物品为[" + itemStr + "] [" + itemId + "]，并下发物品给玩家。", false);
			}
			if(s.contains("get prizeItem ItemID")){
				int id = Integer.parseInt(readitem.splitMessageSP(s, "ID[", "]"));
				String playerNameStr= readitem.splitMessageSP(s, "name[", "]");
				int itemId= Integer.parseInt(readitem.splitMessageSP(s, "get prizeItem ItemID[", "]"));
				String itemStr = null;
				if (items.containsKey(itemId)) {
					itemStr = items.get(itemId);
				} else if (equs.containsKey(itemId)) {
					itemStr = equs.get(itemId);
				}
				writeText(" 奥德赛之旅:玩家[" + playerNameStr + "] ID[" + id + "] 获得开奖物品[" + itemStr + "] [" + itemId + "]。", false);
			}
			count += 1;
			break;
		case TYPE_RECHARGE:
			if(s.contains("UserID[usernull]")){
				String accountid = readitem.splitMessageSP(s, "accountid[", "]");
				String successinfo = readitem.splitMessageSP(s, "successinfo[", "]");
				String amount = readitem.splitMessageSP(s, "amount:", " ");
				writeText(" 充值返回找不到玩家，帐号ID[" + accountid + "] 返回信息[" + successinfo + "] 充值额度[" + amount + "]。", false);
			}else{
				String userid = readitem.splitMessageSP(s, "UserID[", "]");
				String amount = readitem.splitMessageSP(s, "amount：", " ");
				writeText(" 充值成功，角色ID[" + userid + "] 充值额度[" + amount + "]。", false);
			}
			break;
		default :
			return false;
		}
		return true;
	}

	/**
	 * @param s
	 * @param index
	 * @return送给别人的id
	 */
	public static String splitMessagePlayer(String s, int index ,int subtype) {
		String string = "";
		if (TYPE_MAIL == index) {
			if (subtype == MAIL_POST && (s.contains("Dest["))){//发出的信
				int startIndex = s.lastIndexOf("Dest[");
				int charLength = "Dest[".length();
				String temString = s.substring(startIndex + charLength, s.length());
				int endIndex = temString.indexOf("]");
				string = temString.substring(0, endIndex);
			}else if((subtype == MAIL_GET_ATTACHMENT) && (s.contains("SourceId["))){//提取邮件附件
				int startIndex = s.lastIndexOf("SourceId[");
				int charLength = "SourceId[".length();
				String temString = s.substring(startIndex + charLength, s.length());
				int endIndex = temString.indexOf("]");
				string = temString.substring(0, endIndex);
			}else if(subtype == MAIL_DELETE){//删除邮件
				string = "未知";
			}
		} else if (TYPE_CHAT == index) {
			int startIndex = s.lastIndexOf("DESTID[");
			int charLength = "DESTID[".length();
			String temString = s.substring(startIndex + charLength, s.length());
			int endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
		} else if (TYPE_J_SHOP_BUY_SELL == index) {
			int startIndex = s.lastIndexOf("]Price[");
			int charLength = "]Price[".length();
			String temString = s.substring(startIndex + charLength, s.length());
			int endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
		}
		return string;
	}
	
	/**
	 * @param tmp
	 *            写下邮件里的物品
	 * @throws IOException
	 */
	public static void getMailItem(byte[] tmp) throws IOException {
		ByteArrayInputStream biss = new ByteArrayInputStream(tmp);
		DataInputStream dos = new DataInputStream(biss);
		byte b = dos.readByte();
//		if (b == 2 ) {// 金钱或者装备
			byte t = dos.readByte();
			if (t == 8 && tmp.length == 6) {// 金钱
				writeText("金钱数为[" + dos.readInt() + "]", false);
			} else if(t == 3 ) {// 装备
				int equId = dos.readInt();
				writeText("装备物品ID为[" + equId + "]", false);
				if (equs.containsKey(equId)) {
					writeText("  装备物品名称为[" + equs.get(equId)+"] ", false);
					readitem.writeEQU(dos);
				}
			}
//		} else if (b == 3 ) {
			// 基本物品，拓展物品，任务物品
			else{
				int itemId = dos.readInt();
				if (0 == t) {// 基本物品
					writeText("基本物品ID为[" + itemId + "]", false);
					if (items.containsKey(itemId)) {
						writeText("  基本物品名称为[" + items.get(itemId)+"] ", false);
					}
				} else if (1 == t) {// 任务物品
					writeText("任务物品ID为[" + itemId + "]", false);
					if (items.containsKey(itemId)) {
						writeText("  任务物品名称为[" + items.get(itemId)+"] ", false);
					}
				} else if (2 == t) {// 拓展物品
					writeText("拓展物品ID为[" + itemId + "]", false);
					if (items.containsKey(itemId)) {
						writeText("  拓展物品名称为[" + items.get(itemId)+"] ", false);
					}
				}
				short count = dos.readShort();
				writeText("  物品个数为[" + count+"] ", false);
			}
//		}
	}
	
	/**
	 * @param s
	 * @return 获得聊天内容
	 * @throws IOException
	 */
	public static String getChatContent(String s) throws IOException {
		String string = null;
		int startIndex = s.lastIndexOf("MESSAGE[");
		if (startIndex != -1) {
			int charLength = "MESSAGE[".length();
			String temString = s.substring(startIndex + charLength, s.length());
			int endIndex = temString.indexOf("]");
			endIndex=endIndex==-1?temString.length():endIndex;
			string = temString.substring(0, endIndex);
		}

		return string;
	}
	
	/**
	 * 获取商店消费个数
	 */
	public static String getIshopCount(String s) {
		int startIndex = s.lastIndexOf("Count[");
		String string = null;
		if (startIndex != -1) {
			int charLength = "Count[".length();
			String temString = s.substring(startIndex + charLength, s.length());
			int endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
		}
		return string;

	}

	public static String getIshopForotherName(String s) {
		String playName = null;
		if (s.contains("forother")) {
			playName = s.substring(s.indexOf("forother") + "forother".length()
					+ 1, s.length() - 1);
		}
		return playName;
	}
	
	public static void getIshop(String s) {
		
		if(s.contains("Use Vouchers")){
			writeText(" 此物品为用券购买",false);
		}else{
			int start = s.indexOf("UsediMoney[");
			int end = s.indexOf("]", start);
			String str = s.substring(start+11, end);
			writeText(" 物品用的金钱是："+str,false);
		}
		int start = s.indexOf("CurrentiMoney[");
		int end = s.indexOf("]", start);
		String str = s.substring(start+14, end);
		writeText(" 还剩余的金钱数："+str,false);
	}
	
	/**
	 * @param s
	 * 划分普通拍卖行的竞拍
	 * @throws IOException
	 */
	public static void splitjauction(String s) throws IOException{
		//查找起拍过程
		String string = null;
		if(s.contains("TYPE["+AUCTION_PRICE+"],OnlyPrice")){//竞拍过程
			if(!s.contains("TRY")){ //竞拍成功日志
				writeText("玩家[", false);
				int startIndex = s.lastIndexOf("ID[");
				int charLength = "ID[".length();
				String temString = s.substring(startIndex + charLength, s.length());
				int endIndex = temString.indexOf("]");
				string = temString.substring(0, endIndex);
				writeText(string, false);
				
				writeText("]   竞拍物品  出价成功  [", false);
				startIndex = s.lastIndexOf("OnlyPrice Auction[");
				charLength = "OnlyPrice Auction[".length();
				temString = s.substring(startIndex + charLength, s.length());
				endIndex = temString.indexOf("]");
				string = temString.substring(0, endIndex);
				getAuctionItem(getdata(string),0,"");
				writeText("]   花费j币[", false);
				startIndex = s.lastIndexOf("]Price[");
				charLength = "]Price[".length();
				temString = s.substring(startIndex + charLength, s.length());
				endIndex = temString.indexOf("]");
				string = temString.substring(0, endIndex);
				writeText(string, false);
				
				writeText("]   玩家所剩j币[", false);
				startIndex = s.lastIndexOf("]Money[");
				charLength = "]Money[".length();
				temString = s.substring(startIndex + charLength, s.length());
				endIndex = temString.indexOf("]");
				string = temString.substring(0, endIndex);
				writeText(string, false);
				
				writeText("]   竞拍id[ ", false);
				startIndex = s.lastIndexOf("]Auction[");
				charLength = "]Auction[".length();
				temString = s.substring(startIndex + charLength, s.length());
				endIndex = temString.indexOf("]");
				string = temString.substring(0, endIndex);
				writeText(string, false);
				writeText(" ]", false);
			}else{
				writeText("玩家[", false);
				int startIndex = s.lastIndexOf("ID[");
				int charLength = "ID[".length();
				String temString = s.substring(startIndex + charLength, s.length());
				int endIndex = temString.indexOf("]");
				string = temString.substring(0, endIndex);
				writeText(string, false);
				
				writeText("]   尝试  竞拍物品  [", false);
				startIndex = s.lastIndexOf("OnlyPrice Auction[");
				charLength = "OnlyPrice Auction[".length();
				temString = s.substring(startIndex + charLength, s.length());
				endIndex = temString.indexOf("]");
				string = temString.substring(0, endIndex);
				getAuctionItem(getdata(string),0,"");
				writeText("]   花费j币[", false);
				startIndex = s.lastIndexOf("]Price[");
				charLength = "]Price[".length();
				temString = s.substring(startIndex + charLength, s.length());
				endIndex = temString.indexOf("]");
				string = temString.substring(0, endIndex);
				writeText(string, false);
				
				writeText("]   玩家j币[", false);
				startIndex = s.lastIndexOf("]Money[");
				charLength = "]Money[".length();
				temString = s.substring(startIndex + charLength, s.length());
				endIndex = temString.indexOf("]");
				string = temString.substring(0, endIndex);
				writeText(string, false);
				
				writeText("]   竞拍id[ ", false);
				startIndex = s.lastIndexOf("]Auction[");
				charLength = "]Auction[".length();
				temString = s.substring(startIndex + charLength, s.length());
				endIndex = temString.indexOf("]");
				string = temString.substring(0, endIndex);
				writeText(string, false);
				writeText(" ]", false);
			}
		}else if(s.contains("OnlyPrice LastPlayerId[")){//j商店竞拍结束
			writeText("玩家[", false);
			int startIndex = s.lastIndexOf("LastPlayerId[");
			int charLength = "LastPlayerId[".length();
			String temString = s.substring(startIndex + charLength, s.length());
			int endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			writeText(string, false);
			
			writeText("]   竞拍物品结束  [", false);
			startIndex = s.lastIndexOf("]Item[");
			charLength = "]Item[".length();
			temString = s.substring(startIndex + charLength, s.length());
			endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			getAuctionItem(getdata(string),0,"");
			
			writeText("]   发布拍卖玩家[", false);
			startIndex = s.lastIndexOf("]PlayerId[");
			charLength = "]PlayerId[".length();
			temString = s.substring(startIndex + charLength, s.length());
			endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			writeText(string, false);
			
			
			writeText("]   消费j币[", false);
			startIndex = s.lastIndexOf("]Price[");
			charLength = "]Price[".length();
			temString = s.substring(startIndex + charLength, s.length());
			endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			writeText(string, false);
			
			writeText("]   竞拍id[ ", false);
			startIndex = s.lastIndexOf("]Auction[");
			charLength = "]Auction[".length();
			temString = s.substring(startIndex + charLength, s.length());
			endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			writeText(string, false);
			writeText(" ]", false);
		}else if(s.contains("TYPE["+AUCTION_ITEM+"]")){//j商店发布拍卖
			writeText("玩家[", false);
			int startIndex = s.lastIndexOf("ID[");
			int charLength = "ID[".length();
			String temString = s.substring(startIndex + charLength, s.length());
			int endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			writeText(string, false);
			
			writeText("]   发布拍卖  [", false);
			startIndex = s.lastIndexOf("],Item[");
			charLength = "],Item[".length();
			temString = s.substring(startIndex + charLength, s.length());
			endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			getAuctionItem(getdata(string),1,readitem.splitMessageSP(s, "count[", defaultEnd));
			
//			writeText("  ", false);
//			startIndex = s.lastIndexOf("]count[");
//			charLength = "]count[".length();
//			temString = s.substring(startIndex + charLength, s.length());
//			endIndex = temString.indexOf("]");
//			string = temString.substring(0, endIndex);
//			writeText(string+"个", false);
			
			writeText("]   起拍价[", false);
			startIndex = s.lastIndexOf("]StartPrice[");
			charLength = "]StartPrice[".length();
			temString = s.substring(startIndex + charLength, s.length());
			endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			writeText(string, false);
			
			
			writeText("]   一口价[", false);
			startIndex = s.lastIndexOf("]EndPrice[");
			charLength = "]EndPrice[".length();
			temString = s.substring(startIndex + charLength, s.length());
			endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			writeText(string, false);
			
			writeText("]   竞拍id[ ", false);
			startIndex = s.lastIndexOf("]Auction[");
			charLength = "]Auction[".length();
			temString = s.substring(startIndex + charLength, s.length());
			endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			writeText(string, false);
			writeText(" ]", false);
		}	
	}
	
	/**
	 * @param s
	 * 划分荣誉拍卖行的竞拍
	 * @throws IOException
	 */
	public static void splitCredit(String s) throws IOException{
		//查找起拍过程
		String string = null;
		if(s.contains("SubType[2](CREDITSHOP)Auction[")){//竞拍过程
			if(!s.contains("TRY")){ //竞拍成功日志
				writeText("玩家[", false);
				int startIndex = s.lastIndexOf("ID[");
				int charLength = "ID[".length();
				String temString = s.substring(startIndex + charLength, s.length());
				int endIndex = temString.indexOf("]");
				string = temString.substring(0, endIndex);
				writeText(string, false);
				
				writeText("]   竞拍物品  出价成功  [", false);
				startIndex = s.lastIndexOf("SubType[2](CREDITSHOP)Auction[");
				charLength = "SubType[2](CREDITSHOP)Auction[".length();
				temString = s.substring(startIndex + charLength, s.length());
				endIndex = temString.indexOf("]");
				string = temString.substring(0, endIndex);
				getAuctionItem(getdata(string),0,"");
				writeText("]   花费荣誉[", false);
				startIndex = s.lastIndexOf("]Price[");
				charLength = "]Price[".length();
				temString = s.substring(startIndex + charLength, s.length());
				endIndex = temString.indexOf("]");
				string = temString.substring(0, endIndex);
				writeText(string, false);
				
				writeText("]   玩家所剩荣誉[", false);
				startIndex = s.lastIndexOf("]CREDIT[");
				charLength = "]CREDIT[".length();
				temString = s.substring(startIndex + charLength, s.length());
				endIndex = temString.indexOf("]");
				string = temString.substring(0, endIndex);
				writeText(string, false);
				
				writeText("]   竞拍id[ ", false);
				startIndex = s.lastIndexOf("]Auction[");
				charLength = "]Auction[".length();
				temString = s.substring(startIndex + charLength, s.length());
				endIndex = temString.indexOf("]");
				string = temString.substring(0, endIndex);
				writeText(string, false);
				writeText(" ]", false);
			}else{
				writeText("玩家[", false);
				int startIndex = s.lastIndexOf("ID[");
				int charLength = "ID[".length();
				String temString = s.substring(startIndex + charLength, s.length());
				int endIndex = temString.indexOf("]");
				string = temString.substring(0, endIndex);
				writeText(string, false);
				
				writeText("]   尝试  竞拍物品  [", false);
				startIndex = s.lastIndexOf("SubType[2](CREDITSHOP)Auction[");
				charLength = "SubType[2](CREDITSHOP)Auction[".length();
				temString = s.substring(startIndex + charLength, s.length());
				endIndex = temString.indexOf("]");
				string = temString.substring(0, endIndex);
				getAuctionItem(getdata(string),0,"");
				writeText("]   花费荣誉[", false);
				startIndex = s.lastIndexOf("]Price[");
				charLength = "]Price[".length();
				temString = s.substring(startIndex + charLength, s.length());
				endIndex = temString.indexOf("]");
				string = temString.substring(0, endIndex);
				writeText(string, false);
				
				writeText("]   玩家当前荣誉[", false);
				startIndex = s.lastIndexOf("]CREDIT[");
				charLength = "]CREDIT[".length();
				temString = s.substring(startIndex + charLength, s.length());
				endIndex = temString.indexOf("]");
				string = temString.substring(0, endIndex);
				writeText(string, false);
				
				writeText("]   竞拍id[ ", false);
				startIndex = s.lastIndexOf("]Auction[");
				charLength = "]Auction[".length();
				temString = s.substring(startIndex + charLength, s.length());
				endIndex = temString.indexOf("]");
				string = temString.substring(0, endIndex);
				writeText(string, false);
				writeText(" ]", false);
			}
			
		}else if(s.contains("SubType[2](CREDITSHOP)Auction Return[")){//当前玩家荣誉返还
			writeText("玩家[", false);
			int startIndex = s.lastIndexOf("ID[");
			int charLength = "ID[".length();
			String temString = s.substring(startIndex + charLength, s.length());
			int endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			writeText(string, false);
			
			writeText("]   竞拍物品  [", false);
			startIndex = s.lastIndexOf("SubType[2](CREDITSHOP)Auction Return[");
			charLength = "SubType[2](CREDITSHOP)Auction Return[".length();
			temString = s.substring(startIndex + charLength, s.length());
			endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			getAuctionItem(getdata(string),0,"");
			writeText("]   返还荣誉[", false);
			startIndex = s.lastIndexOf("]CREDIT Return[");
			charLength = "]CREDIT Return[".length();
			temString = s.substring(startIndex + charLength, s.length());
			endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			writeText(string, false);
			
			writeText("]   竞拍id[ ", false);
			startIndex = s.lastIndexOf("]Auction[");
			charLength = "]Auction[".length();
			temString = s.substring(startIndex + charLength, s.length());
			endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			writeText(string, false);
			writeText(" ]", false);
		}else if(s.contains("(CREDITSHOP)Auction Ok PLAYERID[")){//荣誉商店竞拍结束
			writeText("玩家[", false);
			int startIndex = s.lastIndexOf("ID[");
			int charLength = "ID[".length();
			String temString = s.substring(startIndex + charLength, s.length());
			int endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			writeText(string, false);
			
			writeText("]   竞拍物品结束  [", false);
			startIndex = s.lastIndexOf("]Item[");
			charLength = "]Item[".length();
			temString = s.substring(startIndex + charLength, s.length());
			endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			getAuctionItem(getdata(string),0,"");
			
			writeText("]   消费荣誉[", false);
			startIndex = s.lastIndexOf("] credit[");
			charLength = "] credit[".length();
			temString = s.substring(startIndex + charLength, s.length());
			endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			writeText(string, false);
			
			writeText("]   竞拍id[ ", false);
			startIndex = s.lastIndexOf("]..Auction[");
			charLength = "]..Auction[".length();
			temString = s.substring(startIndex + charLength, s.length());
			endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
			writeText(string, false);
			writeText(" ]", false);
		}
		//writeTextln(string, false);
		//查找最后拍卖结束过程
	}
	
	/**
	 * @param s获得竞拍物品
	 */
	public static void getAuctionItem(byte[] bytes,int flag,String itemCount)throws IOException{

		byte TYPE_BASIC = 0;
		final byte TYPE_TASK = 1;
	   	final byte TYPE_EXTENDED = 2;
	   	final byte TYPE_EQU = 3;
	   	final byte TYPE_PET = 4;
	    if (flag == 0){
	    	//读取版本号,物品类型,物品数据段,物品数量
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			DataInputStream dis = new DataInputStream(bis);
	        byte version = dis.readByte();
	        byte type = dis.readByte();
	        if (type == TYPE_BASIC || type == TYPE_EXTENDED ||
	                   type == TYPE_TASK) {
	            int itemId = dis.readInt();
	            if(items.containsKey(itemId)){
	            	writeText(items.get(itemId), false);
	            }else{
	            	writeText(""+itemId, false);
	            }
	            Short count = dis.readShort();
	            writeText("  数量:" + count.toString(), false);
	        } else if (type == TYPE_EQU) {
	        	readitem.writeEQU(dis);
	        }
	    }else{
	    	ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			DataInputStream dis = new DataInputStream(bis);
			int itemId = dis.readInt();
			if(items.containsKey(itemId)){
            	writeText(items.get(itemId), false);
            	if(itemCount!=null && itemCount.length()>0){
            		writeText(" "+itemCount + "个  ",false);
            	}
            }else{
            	if(equs.containsKey(itemId)){
            		writeText(equs.get(itemId) + "] 装备信息[",false);
            		readitem.writeEQU(dis);
            		writeText("] ",false);
            	}else{
            		writeText(""+itemId, false);
            	}
            }
	    }
		
	}
	
	public static void splitTask(String s,String tmps,String strID){
		String money = readitem.splitMessageSP(s, "Money[", defaultEnd);
		writeText(strID + " 当前要交的任务id["+ tmps + "] 当前金钱[" + money + "] ",false);
		if(s.contains("TRY")){
			writeText("准备交任务。",false);
		}else if(s.contains("Completed")){
			String credit = readitem.splitMessageSP(s, "Credit[", defaultEnd);
			writeText(" 当前荣誉[" + credit + "] 成功交任务，准备发送奖励。",false);
		}else if(s.contains("Changed")){
			String str = readitem.splitMessageSP(s, "Changed[", defaultEnd);
			byte[] itembyte = getdata(str);
			processorChanged changed =  new processorChanged();
			String output = changed.print(itembyte);
			writeText("成功发送奖励。变化：" + output,false);
		}
	}
	
	public static void parseHomeAddItem(String s,String tmps) throws IOException{
		String homeID = readitem.splitMessageSP(s, "House[", defaultEnd);
		String count = readitem.splitMessageSP(s, "Count[", defaultEnd);
		if(s.contains("TRY")){
			writeText("家园ID[" + homeID +"] 尝试 往仓库存入物品！物品信息：",false);
		}else if(s.contains("FAIL")){
			writeText("家园ID[" + homeID +"] 往仓库存入物品失败！物品信息：",false);
		}else{
			writeText("家园ID[" + homeID +"] 往仓库存入物品成功！ 物品信息：",false);
		}
		EquipMenu.getDropItem(getdata(tmps),count);
	}
	
	public static void parseImoneyCard(String s, String tmps){
		if(s.contains("iShop Buy IMoneyCard")){
			if(s.contains("TRY")){
				String cost = readitem.splitMessageSP(s, "cost[", defaultEnd);
				writeText(" 准备购买10元i币卡，价格[" + cost +"] i币[" + tmps+ "] ",false);
			}else if(s.contains("cardno") && s.contains("OK")){
				String cardno = readitem.splitMessageSP(s, "cardno[", defaultEnd);
				String password = readitem.splitMessageSP(s, "password[", defaultEnd);
				writeText("购买10元i币卡成功！ 卡号[" + cardno + "] 密码[" + password +"] i币[" + tmps+ "] ",false);
			}
		}else if(s.contains("Use IMoneyCard") && (s.contains("TRY") || s.contains("OK"))){
			String cardno = readitem.splitMessageSP(s, "cardno[", defaultEnd);
			String password = readitem.splitMessageSP(s, "password[", defaultEnd);
			String amount = readitem.splitMessageSP(s, "amount[", defaultEnd);
			if(s.contains("TRY")){
				writeText("准备使用10元i币卡，",false);
			}else{
				writeText("使用10元i币卡成功！",false);
			}
			writeText("卡号[" + cardno + "] 密码[" + password +"] 价格[" + amount + "] ",false);
		}
	}
}
