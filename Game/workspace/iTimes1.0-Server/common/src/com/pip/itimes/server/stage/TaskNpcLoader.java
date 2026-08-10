package com.pip.itimes.server.stage;

import java.io.File;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.dom4j.Element;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

/**
 * @author Jeffery
 * @version 1.0
 */
public class TaskNpcLoader {
    public TaskNpcLoader(File file) throws Exception{
        SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        loadTaskNpcs(doc);
    }

    private void loadTaskNpcs(Document doc){
        Element root = doc.getRootElement();
        for(Iterator i=root.elementIterator("npc");i.hasNext();){
            Element node = (Element)i.next();
            int id = Integer.parseInt(node.attributeValue("id"));
            int type = Integer.parseInt(node.attributeValue("type"));
            String name = node.attributeValue("name");
            if(type==0){ //ability
                AbilityNpcType npc = new AbilityNpcType(id,name,type);
                Element clazzNode = node.element("class");
                int clazz = Integer.parseInt(clazzNode.attributeValue("id"));
                npc.setClazz(clazz);
                Ability[] abilities = loadAbilities(node);
                npc.setAbilitites(abilities);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==1){ //skill
                SkillNpcType npc = new SkillNpcType(id,name,type);
                Element clazzNode = node.element("class");
                int clazz = Integer.parseInt(clazzNode.attributeValue("id"));
                npc.setClazz(clazz);
                Recipe[] recipes = loadRecipes(node);
                Arrays.sort(recipes);
                npc.setRecipes(recipes);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==2){
                ShopTaskNpcType npc = new ShopTaskNpcType(id,name,type);
                Element clazzNode = node.element("class");
                int clazz = Integer.parseInt(clazzNode.attributeValue("id"));
                npc.setClazz(clazz);
                Element areaNode = node.element("area");
                short area = Short.parseShort(areaNode.attributeValue("id"));
                npc.setAreaId(area);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==3){
                AuctionTaskNpcType npc = new AuctionTaskNpcType(id,name,type);
                Element clazzNode = node.element("class");
                int clazz = Integer.parseInt(clazzNode.attributeValue("id"));
                npc.setClazz(clazz);
                Element areaNode = node.element("area");
                short area = Short.parseShort(areaNode.attributeValue("id"));
                npc.setAreaId(area);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==4){
                StoreNpcType npc = new StoreNpcType(id,name,type);
                Element areaNode = node.element("area");
                short area = Short.parseShort(areaNode.attributeValue("id"));
                npc.setAreaId(area);
                Element groupNode = node.element("group");
                int group = Integer.parseInt(groupNode.attributeValue("id"));
                npc.setGroup(group);
                Element discountNode = node.element("discount");
                int discount = Integer.parseInt(discountNode.attributeValue("value"));
                npc.setDiscount(discount);
                int clazz = Integer.parseInt(node.attributeValue("clazz"));
                npc.setClazz(clazz);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==5){
                TongNpcType npc = new TongNpcType(id,name,type);
                Element clazzNode = node.element("class");
                int clazz = Integer.parseInt(clazzNode.attributeValue("id"));
                npc.setClazz(clazz);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==6){
                PetNpcType npc = new PetNpcType(id,name,type);
                Element clazzNode = node.element("class");
                int clazz = Integer.parseInt(clazzNode.attributeValue("id"));
                npc.setClazz(clazz);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==7){
                InstanceTaskNpcType npc = new InstanceTaskNpcType(id,name,type);
                Element ins = node.element("instance");
                int instanceId = Integer.parseInt(ins.attributeValue("id"));
                String instanceType = ins.attributeValue("type");
                npc.setInstanceId(instanceId);
                npc.setInstanceType(instanceType);
                Element m = node.element("message");
                npc.setMessage(m.getStringValue());
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==8){
                RepaireTaskNpcType npc = new RepaireTaskNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==9){
                ModifyPasswordNpcType npc = new ModifyPasswordNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==10){
                BillingNpcType npc = new BillingNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==11){
                BattleFieldNpcType npc = new BattleFieldNpcType(id,name,type);
                npc.setClazz(1);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==12){
                BattleFieldNpcType npc = new BattleFieldNpcType(id,name,type);
                npc.setClazz(2);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==13){
                MarryNpcType npc = new MarryNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==14){
                MasterNpcType npc = new MasterNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==15){
                GuildBattleFieldNpcType npc = new GuildBattleFieldNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==16){
                RoleFaceNpcType npc = new RoleFaceNpcType(id,name,type);
                Element el = node.element("face");
                npc.setFace(Integer.parseInt(el.attributeValue("id")));
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==20){
                BattleFieldInfoNpcType npc = new BattleFieldInfoNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==21){
                GuildBattleFieldInfoType npc = new GuildBattleFieldInfoType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==22){
                QueryGuildCreditNpcType npc = new QueryGuildCreditNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==23){
                QueryCreditNpcType npc = new QueryCreditNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==24){
                BlogNpcType npc = new BlogNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==25){
                CmccChargeNpcType npc = new CmccChargeNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==26){
                HouseNpcType npc = new HouseNpcType(id,name,type);
                Element el = node.element("area");
                npc.setAreaId(Short.parseShort(el.attributeValue("id")));
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==27){
                HouseItemNpcType npc = new HouseItemNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==28){
                LeaveMessageNpcType npc = new LeaveMessageNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==29){
                OutHouseNpcType npc = new OutHouseNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==30){
                GetHouseItemNpcType npc = new GetHouseItemNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==31) {
            	QuestionNpcType npc = new QuestionNpcType(id,name,type);
            	
            	Element areaNode = node.element("class");
                short id1 = Short.parseShort(areaNode.attributeValue("id"));
                npc.setTypeId(id1);
                int version = Integer.parseInt(areaNode.attributeValue("version"));
                npc.setVersion(version);
            	TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type == 32){
                TopListTongNpcType npc = new TopListTongNpcType(id, name, type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type == 33){
                TopListLastKillsNpcType npc = new TopListLastKillsNpcType(id, name, type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type == 34){
                TopListLastSneaksNpcType npc = new TopListLastSneaksNpcType(id, name, type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type == 35){
                TopListHouseNpcType npc = new TopListHouseNpcType(id, name, type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type==36){
                TitleListNpcType npc = new TitleListNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==36){
                TitleListNpcType npc = new TitleListNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==37){
                TongBathHouseNpcType npc = new TongBathHouseNpcType(id,name,type);
                Element el = node.element("bath");
                npc.setBathId(Integer.parseInt(el.attributeValue("id")));
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==39){
                SportNpcType npc = new SportNpcType(id,name,type);
                List la = new ArrayList(10);
                List lb = new ArrayList(10);
                for(Iterator ite = node.elementIterator("item");ite.hasNext();){
                    Element el = (Element)ite.next();
                    la.add(el.attributeValue("choice"));
                    lb.add(el.attributeValue("command"));
                }
                String[] choices = new String[la.size()];
                la.toArray(choices);
                String[] commands = new String[lb.size()];
                lb.toArray(commands);
                npc.setChoices(choices);
                npc.setCommands(commands);
                String question = node.element("question").attributeValue("text");
                npc.setQuestion(question);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==40){
                SpRoomNpcType npc = new SpRoomNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==41){
               EnhanceNpcType npc = new EnhanceNpcType(id,name,type);
               Element clazzNode = node.element("type");
               int clazz = Integer.parseInt(clazzNode.attributeValue("class"));
               npc.setClasstype(clazz);
               TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==42){
                TongIslandNpcType npc = new TongIslandNpcType(id,name,type);
                int islandId = Integer.parseInt(node.attributeValue("islandid"));
                npc.setIslandId(islandId);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==43){
                AuctionIslandNpcType npc = new AuctionIslandNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==44){
                QQChargeNpcType npc = new QQChargeNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==45){
                IslandItemNpcType npc = new IslandItemNpcType(id,name,type);
                int islandId = Integer.parseInt(node.attributeValue("islandid"));
                npc.setIslandId(islandId);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==46){//世界地图内的npc
            	WorldMapNpcType npc = new WorldMapNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==47){//知己npc
            	FriendsNpcType npc = new FriendsNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==99){
                QQCharge2NpcType npc = new QQCharge2NpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }
            else if(type==48){//荣誉拍卖npc
            	CreditSaleNpcType npc = new CreditSaleNpcType(id,name,type);
                Element clazzNode = node.element("class");
                int clazz = Integer.parseInt(clazzNode.attributeValue("id"));
                npc.setClazz(clazz);
                Element areaNode = node.element("area");
                short area = Short.parseShort(areaNode.attributeValue("id"));
                npc.setAreaId(area);
                TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type==49){//密码保护
            	AccoundBingingNpcType npc = new AccoundBingingNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type==50){//礼物发放
                GiftNpcType npc = new GiftNpcType(id,name,type);
                
                Vector<Integer> tmp = new Vector<Integer>();
                
                for(Iterator ite = node.elementIterator("giftgroup");ite.hasNext();){
                    Element el = (Element)ite.next();
                    tmp.add(Integer.valueOf(el.attributeValue("id")));
                }
                
                int[] groups = new int[tmp.size()];
                
                for(int k = 0; k < tmp.size(); k++){
                    groups[k] = tmp.get(k);
                }

                npc.setGiftGroupIds(groups);
                
                TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type==51){//装备兑换荣誉
            	ExchangeEquNpcType npc = new ExchangeEquNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }else if((type >= 52) && (type <= 62)){//名人堂
            	IbuyTop10NpcType npc = new IbuyTop10NpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type==63){//宠物托儿所
            	PetmanagerNpcType npc = new PetmanagerNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type==64){//宠物精炼
            	enhancePetNpcType npc = new enhancePetNpcType(id,name,type);
            	TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type==65 || type==66){// 3星二周年礼品发放。邮件装备清理
            	GiftNpcType npc = new GiftNpcType(id,name,type);
            	TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type==67){
            	SuperQJumpNpcType npc = new SuperQJumpNpcType(id,name,type);
            	Element trancetype = node.element("trancetype");
                int typetrance = Short.parseShort(trancetype.attributeValue("id"));
                npc.setTrancetype(typetrance);
                Element areaNode = node.element("superQmapid");
                short superQmapid = Short.parseShort(areaNode.attributeValue("id"));
                npc.setMapId(superQmapid);
                Element groupNode = node.element("superQx");
                short superQx = Short.parseShort(groupNode.attributeValue("id"));
                npc.setX(superQx);
                Element discountNode = node.element("superQy");
                short superQy = Short.parseShort(discountNode.attributeValue("id"));
                npc.setY(superQy);
                if (typetrance == 8){
                	Element areaNode1 = node.element("superQmapid1");
                    short superQmapid1 = Short.parseShort(areaNode1.attributeValue("id"));
                    npc.setMapId1(superQmapid1);
                    Element groupNode1 = node.element("superQx1");
                    short superQx1 = Short.parseShort(groupNode1.attributeValue("id"));
                    npc.setX1(superQx1);
                    Element discountNode1 = node.element("superQy1");
                    short superQy1 = Short.parseShort(discountNode1.attributeValue("id"));
                    npc.setY1(superQy1);
                }
                TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type==68){
            	RecommendedNpcType npc = new RecommendedNpcType(id,name,type);
                Element areaNode = node.element("areaid");
                int areaid = Integer.valueOf((areaNode.attributeValue("id")));
                npc.setAreaId(areaid);
                Element groupNode = node.element("urlname");
                String urlname = groupNode.attributeValue("id");
                npc.setUrl_name(urlname);
                TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type==69){
            	CmccBusinessNpcType npc = new CmccBusinessNpcType(id,name,type);
            	Element Areaid = node.element("areaid");
                int Areaidint = Short.parseShort(Areaid.attributeValue("id"));
                npc.setAreaid(Areaidint);
                Element Businesstype = node.element("Businesstype");
                int Businesstypeint = Short.parseShort(Businesstype.attributeValue("id"));
                npc.setBusinesstype(Businesstypeint);
                Element Businesscode = node.element("Businesscode");
                String Businesscodestr = Businesscode.attributeValue("id");
                npc.setBusinesscode(Businesscodestr);
                Element Businessmsg = node.element("Businessmsg");
                String Businessmsgstr = Businessmsg.attributeValue("id");
                npc.setBusinessmsg(Businessmsgstr);
                TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type==70){//温州发奖npc
            	ExchangeEquNpcType npc = new ExchangeEquNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type==71){//激活码npc
            	ActivationCodeNpcType npc = new ActivationCodeNpcType(id,name,type);
            	Element areaNode = node.element("typeid");
                int typeid = Integer.valueOf((areaNode.attributeValue("id")));
            	npc.setTypeId(typeid);
                TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type==72){//系统公告
            	BbsSystemNpcType npc = new BbsSystemNpcType(id, name ,type);
            	Element bbsNode = node.element("bbsId");
            	int bbsSystemId = Integer.parseInt(bbsNode.attributeValue("ids"));
                npc.setBbsSystemId(bbsSystemId);
            	TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type==73){//吉林推荐和传送。。。npc
            	ExchangeEquNpcType npc = new ExchangeEquNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type==74){//自动砸蛋，开宝箱npc
            	ExchangeEquNpcType npc = new ExchangeEquNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type==75){//使用物品送给他人。双方获益
            	GiftItemNpcType npc = new GiftItemNpcType(id, name, type);
            	Element giftitemNode = node.element("giftitem");
            	int npcid = Integer.parseInt(giftitemNode.attributeValue("id"));
            	int itemid = Integer.parseInt(giftitemNode.attributeValue("itemid"));
            	int giftid = Integer.parseInt(giftitemNode.attributeValue("giftid"));
            	int additemid = Integer.parseInt(giftitemNode.attributeValue("additemid"));
            	int mailflag = Integer.parseInt(giftitemNode.attributeValue("mailflag"));
            	String mailtitle = giftitemNode.attributeValue("mailtitle");
            	String itemname = giftitemNode.attributeValue("itemname");
            	npc.setTypeid(npcid);
            	npc.setAdditemid(additemid);
            	npc.setGiftid(giftid);
            	npc.setItemid(itemid);
            	npc.setMailtitle(mailtitle);
            	npc.setItemname(itemname);
            	if (mailflag == 1){
            		npc.setMailflag(true);
            	}else{
            		npc.setMailflag(false);
            	}
            	TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type ==76){//公会岛屿礼包师
            	TongGiftNpcType npc = new TongGiftNpcType(id,name,type);
            	Vector<Integer> tmp = new Vector<Integer>();
                for(Iterator ite = node.elementIterator("giftgroup");ite.hasNext();){
                    Element el = (Element)ite.next();
                    tmp.add(Integer.valueOf(el.attributeValue("id")));
                }
                int[] groups = new int[tmp.size()];
                for(int k = 0; k < tmp.size(); k++){
                    groups[k] = tmp.get(k);
                }
                npc.setGiftGroupIds(groups);
                int tongIslandId = Integer.parseInt(node.element("tongIsland").attributeValue("id"));
                npc.setTongIslandId(tongIslandId);
                TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type==79){//旧的生活技能修改
            	OldMakeSkillNpcType npc = new OldMakeSkillNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type==80){//恢复师统一修改
            	ExchangeEquNpcType npc = new ExchangeEquNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type == 81){//1v1竞技场报名
                ArenaSingupNpcType npc = new ArenaSingupNpcType(id, name, type);
                Element arenaNode = node.element("arena");
                int arenaType = Integer.parseInt(arenaNode.attributeValue("type"));
                npc.setArenaType(arenaType);
                TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type == 82){//战队创建
            	ArenaCreateTeamNpcType npc = new ArenaCreateTeamNpcType(id, name, type);
            	Element arenaNode = node.element("arena");
                int arenaType = Integer.parseInt(arenaNode.attributeValue("type"));
            	npc.setArenaType(arenaType);
                TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type == 83){//竞技场信息查询
            	ArenaTeamInfoNpcType npc = new ArenaTeamInfoNpcType(id, name, type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type == 84){//竞技场排行查询
            	ArenaTeamInfoNpcType npc = new ArenaTeamInfoNpcType(id, name, type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type == 92){//选举npc
            	VoteNpcType npc = new VoteNpcType(id, name, type);
            	Element areaNode = node.element("votegift");
                int voteTypeId = Integer.valueOf((areaNode.attributeValue("id")));
            	npc.setVoteType(voteTypeId);
                TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type == 93){//套装升级npc
            	ExchangeGroupNpcType npc = new ExchangeGroupNpcType(id, name, type);
            	Element exchangegroupNode = node.element("exchangegroup");
            	int exchangegroup = Integer.valueOf(exchangegroupNode.attributeValue("id"));
            	npc.setExchangeGroupId(exchangegroup);
            	TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type == 94){//星移npc
            	ExchangeEquNpcType npc = new ExchangeEquNpcType(id,name,type);
                TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type==95){	//阵营选举
            	CampVoteNpcType npc = new CampVoteNpcType(id,name,type);
                int camp = Integer.parseInt(node.attributeValue("camp"));
                npc.setCamp(camp);
                TaskNpcTypes.adddTaskNpcType(npc);
            } else if (type == 96) {	// 宠物大师
            	PetmanagerNpcType npc = new PetmanagerNpcType(id, name, type);
                TaskNpcTypes.adddTaskNpcType(npc);
            } else if (type == 100) {	// 新荣誉大厅NPC
            	CStoreNpcType npc = new CStoreNpcType(id, name, type);
                Element areaNode = node.element("area");
                short area = Short.parseShort(areaNode.attributeValue("id"));
                npc.setAreaId(area);
                Element groupNode = node.element("group");
                int group = Integer.parseInt(groupNode.attributeValue("id"));
                npc.setGroup(group);
                Element discountNode = node.element("discount");
                int discount = Integer.parseInt(discountNode.attributeValue("value"));
                npc.setDiscount(discount);
                TaskNpcTypes.adddTaskNpcType(npc);
            } else if(type == 101){		//	阵营点名NPC
            	CampRollcallNpcType npc = new CampRollcallNpcType(id, name, type);
                int camp = Integer.parseInt(node.attributeValue("camp"));
                npc.setCamp(camp);
                TaskNpcTypes.adddTaskNpcType(npc);
            } else if (type == 102) {	// 食神NPC
            	IronChefNpcType npc = new IronChefNpcType(id, name, type);
                TaskNpcTypes.adddTaskNpcType(npc);
            } else if (type == 201) {	// 阵营战场NPC
            	CampBattlefieldNpcType npc = new CampBattlefieldNpcType(id, name, type);
            	int campType = Integer.parseInt(node.attributeValue("camptype"));
            	Element ins = node.element("instance");
                int instanceID = Integer.parseInt(ins.attributeValue("id"));
                String battlefieldType = ins.attributeValue("battlefieldtype");
                Element m = node.element("message");
                npc.setBattlefieldType(battlefieldType);
                npc.setInstanceID(instanceID);
                npc.setCampType(campType);
                npc.setMessage(m.getStringValue());
                TaskNpcTypes.adddTaskNpcType(npc);
            } else if(type == 202){	//点歌机
            	LyricNpcType npc = new LyricNpcType(id, name, type);
            	TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type == 203){// 公会商店NPC
            	TongShopNpcType npc = new TongShopNpcType(id,name,type);
                Element islandNode = node.element("tongIsland");
                short islandID = Short.parseShort(islandNode.attributeValue("id"));
                npc.setIslandID(islandID);
                Element groupNode = node.element("group");
                int group = Integer.parseInt(groupNode.attributeValue("id"));
                npc.setGroup(group);
            	TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type == 204){	//庄园NPC
            	FarmNpcType npc = new FarmNpcType(id, name, type);
            	TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type == 205){	//吸血鬼npc
            	BloodNpcType npc = new BloodNpcType(id,name,type);
            	TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type == 206){	//庄园随便逛逛NPC
            	FarmRandomNpcType npc = new FarmRandomNpcType(id, name, type);
            	TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type == 207){	//庄园查询npc
            	FarmFindNpcType npc = new FarmFindNpcType(id, name, type);
            	TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type == 208){ //多层BOSS挑战NPC
            	BossRushNpcType npc = new BossRushNpcType(id,name,type);
            	TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type == 209){ //圣诞许愿NPC
            	ChristmasWishingNpcType npc = new ChristmasWishingNpcType(id,name,type);
            	Element cwElement = node.element("randomMessage");
            	ChristmasWishingNpcType.randomMessageKey = cwElement.attributeValue("key");
            	TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type == 210){	//兑换宝石npc
            	ReplaceDiamondNpcType npc = new ReplaceDiamondNpcType(id, name, type);
            	TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type == 211){	//打开星空之恋
            	SkyloveNpcType npc = new SkyloveNpcType(id, name, type);
            	TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type == 212){	//打开战神祝福
            	BlessingGodOfWar npc = new BlessingGodOfWar(id, name, type);
            	TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type == 213){	//果实换经验
            	ReplaceExpNpc npc = new ReplaceExpNpc(id, name, type);
            	TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type == 214){	//端午节粽子兑换
            	DragonBoatFestivalReplaceNpc npc = new DragonBoatFestivalReplaceNpc(id, name, type);
            	TaskNpcTypes.adddTaskNpcType(npc);
            }else if(type == 215){	//世界BOSS NPC
	        	WorldBossNpc npc = new WorldBossNpc(id, name, type);
	        	TaskNpcTypes.adddTaskNpcType(npc);
	        }else if(type == 216){	//宝石分数NPC
	        	GemTopNpc npc = new GemTopNpc(id, name, type);
	        	TaskNpcTypes.adddTaskNpcType(npc);
	        }else if(type == 217){	//诺亚方舟NPC
	        	NoahsarkNpc npc = new NoahsarkNpc(id, name, type);
	        	TaskNpcTypes.adddTaskNpcType(npc);
	        }else if(type == 218){//圣诞活动 
	        	ChristmasFestivalReplaceNpc npc = new ChristmasFestivalReplaceNpc(id, name, type);
	        	TaskNpcTypes.adddTaskNpcType(npc);
	        }else if(type==219){//诺亚方舟卧铺票兑奖npc
                NoahsarkTicketGiftNpcType npc = new NoahsarkTicketGiftNpcType(id,name,type);
                Vector<Integer> tmp = new Vector<Integer>();
                for(Iterator ite = node.elementIterator("giftgroup");ite.hasNext();){
                    Element el = (Element)ite.next();
                    tmp.add(Integer.valueOf(el.attributeValue("id")));
                }            
                int[] groups = new int[tmp.size()];
                
                for(int k = 0; k < tmp.size(); k++){
                    groups[k] = tmp.get(k);
                }
                npc.setGiftGroupIds(groups);
                
                TaskNpcTypes.adddTaskNpcType(npc);
	        }
        }
    }

    private Recipe[] loadRecipes(Element node){
        List l = new ArrayList();
        for(Iterator i=node.elementIterator("Recipe");i.hasNext();){
            Element el = (Element)i.next();
            int id = Integer.parseInt(el.attributeValue("id"));
            Recipe recipe = Recipes.getRecipe(id);
            if(recipe!=null){
                l.add(recipe);
            }
        }
        Recipe[] ret = new Recipe[l.size()];
        l.toArray(ret);
        return ret;
    }

    private Ability[] loadAbilities(Element node){
        List l = new ArrayList();
        for(Iterator i=node.elementIterator("Ability");i.hasNext();){
            Element el = (Element)i.next();
            int id = Integer.parseInt(el.attributeValue("id"));
            Ability ability = Ability.getAbility(id);
            if(ability!=null){
                l.add(ability);
            }
        }
        Ability[] ret = new Ability[l.size()];
        l.toArray(ret);
        return ret;
    }
    
}
