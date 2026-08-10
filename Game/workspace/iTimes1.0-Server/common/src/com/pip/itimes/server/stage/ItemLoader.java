package com.pip.itimes.server.stage;

import java.io.File;
import java.util.*;

import org.dom4j.*;
import org.dom4j.io.SAXReader;


/**
 * @author Jeffrey
 * @version 1.0
 */
public class ItemLoader {


    public ItemLoader(File equFile,File itemFile) throws Exception{
        SAXReader reader = new SAXReader();
        Document doc = reader.read(itemFile);//2013年4月1日 load装备和物品次序替换
        loadItems(doc);
        reader = new SAXReader();
        doc = reader.read(equFile);
        loadEquipments(doc);
    }

    private void loadEquipments(Document doc){
        Element root = doc.getRootElement();
        for(Iterator i=root.elementIterator("item");i.hasNext();){
            Element node = (Element)i.next();
            EquipmentTemplate equ = null;
            byte createType = Byte.parseByte(node.attributeValue("createType"));
            if(createType==1){
                equ = new NormalEquipmentTemplate();
            }else{
                equ = new DynamicEquipmentTemplate();
            }
            Attribute att = node.attribute("itemID");
            int id = Integer.parseInt(att.getValue());
            equ.setItemId(id);
            att = node.attribute("title");
            String name = att.getValue();
            equ.setName(name);
            att = node.attribute("itemLevel");
            short itemLevel = Short.parseShort(att.getValue());
            equ.setLevel(itemLevel);
            att = node.attribute("requireLevel");
            short requiredLevel = Short.parseShort(att.getValue());
            equ.setRequiredLevel(requiredLevel);
            att = node.attribute("equLevel");
            byte quality = Byte.parseByte(att.getValue());
            equ.setQuality(quality);
            att = node.attribute("part");
            byte part = Byte.parseByte(att.getValue());
            equ.setPart(part);
            att = node.attribute("enduranceUpper");
            short durability = Short.parseShort(att.getValue());
            equ.setDurability(durability);
            att = node.attribute("price");
            int price = Integer.parseInt(att.getValue());
            equ.setPrice(price);
            att = node.attribute("bind");
            byte bindType = Byte.parseByte(att.getValue());
            equ.setBindType(bindType);
            int credit = Integer.parseInt(node.attributeValue("honorLevel"));
            equ.setCredit(credit);
//            boolean canEnhance = true;
            boolean canEnhance = Integer.parseInt(node.attributeValue("canEnhance"))==1?true:false;
            equ.setCanEnhance(canEnhance);
            boolean canSplit = true;
            String strSplitValue = node.attributeValue("canSplit");
            if(strSplitValue != null){
            	canSplit = Integer.parseInt(strSplitValue) == 1 ? true : false;
            }
            equ.setCanSplit(canSplit);
            String strVianyValue = node.attributeValue("vianyStoneValue");
            if(strVianyValue != null){
            	equ.setVianyStoneValue(Byte.parseByte(strVianyValue));
            }
            strVianyValue = node.attributeValue("vianyScissorsValue");
            if(strVianyValue != null){
            	equ.setVianyScissorsValue(Byte.parseByte(strVianyValue));
            }
            strVianyValue = node.attributeValue("vianyPaperValue");
            if(strVianyValue != null){
            	equ.setVianyPaperValue(Byte.parseByte(strVianyValue));
            }

            String strDiamond = node.attributeValue("diamond");
            if(strDiamond != null){
            	equ.setDiamond(Byte.parseByte(strDiamond));
            }
            
            String strOpenhole = node.attributeValue("openhole");
            if(strOpenhole != null){
            	equ.setOpen6hole(Byte.parseByte(strOpenhole));
            }
            
            Element tmp = node.element("appendAttributes");
            for(Iterator j=tmp.elementIterator("appendAttribute");j.hasNext();){
                Element el = (Element)j.next();
                Attribute at = el.attribute("type");
                int type = Integer.parseInt(at.getValue());
                at = el.attribute("value");
                int value = Integer.parseInt(at.getValue());
                at = el.attribute("growvalue");
                int growvalue = 0;
                if(at != null){
                	growvalue = Integer.parseInt(at.getValue());
                }
                equ.addProperty(type,value,growvalue);
            }
            equ.setDesc(node.attributeValue("desc"));
            
            equ.setItemType(Byte.parseByte(node.attributeValue("important")));
            equ.setDiamondcount(Byte.parseByte(node.attributeValue("diamondcount")));
            equ.setOpenDiamondCount(Byte.parseByte(node.attributeValue("opendiamondcount")));//已开孔位
            if(equ.getItemId() == 1001712 || equ.getItemId() == 1001713 || equ.getItemId() == 1001789){//扫描一次九星周年链和九星周年戒指兑换
        		equ.setDiamond((byte) 8);
        	}
            //2013年3月29日 增加 已镶嵌宝石、已精炼  的设置
            int[] ItemID = new int[5];
            ItemID[0] = Integer.parseInt(node.attributeValue("diamond01"));
            ItemID[1] = Integer.parseInt(node.attributeValue("diamond02"));
            ItemID[2] = Integer.parseInt(node.attributeValue("diamond03"));
            ItemID[3] = Integer.parseInt(node.attributeValue("diamond04"));
            ItemID[4] = Integer.parseInt(node.attributeValue("diamond05"));
//            if (equ.itemId == 1002118){//测试用
//            	equ.getOpenDiamondCount();
//            }
            if ((equ.getOpenDiamondCount() > 0)&&(ItemID[0] > 0)){
            	byte[] diamondRoleInfo = new byte[5];
            	DiamondMosaic[] diamondMosaic = new DiamondMosaic[5];
	            for(int j=0; j<equ.getOpenDiamondCount(); j++){
	            	if (ItemID[j] > 0){ //0为空
	            		byte holes = (byte) (j);
            			IItem item = Items.getTemplate(ItemID[0]).newInstance();
            			byte diamondMosaicEmbedLevel = DiamondMosaic.findDiamondMosaicLevel(ItemID[j]);
                		diamondRoleInfo[holes] = (byte) (IEquipment.CURRENT_EQU_CANDIAMOND + diamondMosaicEmbedLevel);
                		diamondMosaic[j] = DiamondMosaic.getDiamondMosaicMap().get(ItemID[j]);
	                	
	            	}
	            }
	            equ.setDiamondMosaic(diamondMosaic);
	            equ.setDiamondMosiacRoleInfo(diamondRoleInfo);
            }
            //精炼部分
            int enhanceLevel = Integer.parseInt(node.attributeValue("enhanceLevel"));
            int enhanceProperty = Integer.parseInt(node.attributeValue("enhanceProperty"));
            for(int j=0; j<enhanceLevel; j++){
            	Enhance enhance = Enhance.getEnhance(enhanceProperty,equ.getLevel());
                if (enhance != null) {
                	equ.enhance(enhance);
                }
            }           
            
            Items.addTemplate(equ);
        }
    }

    private void loadItems(Document doc){
        Element root = doc.getRootElement();
        for(Iterator i=root.elementIterator("item");i.hasNext();){
            Element node = (Element)i.next();
            Attribute att = node.attribute("type");
            byte type = Byte.parseByte(att.getValue());
            if(type==0){
                BasicItemTemplate item = new BasicItemTemplate();
                att = node.attribute("itemID");
                int id = Integer.parseInt(att.getValue());
                item.setItemId(id);
                att = node.attribute("price");
                int price = Integer.parseInt(att.getValue());
                item.setPrice(price);
                att = node.attribute("title");
                String name = att.getValue();
                item.setName(name);
                att = node.attribute("id");
                byte function = Byte.parseByte(att.getValue());
                item.setFunction(function);
                item.setDesc(node.attributeValue("desc"));
                byte bindType = Byte.parseByte(node.attributeValue("bind"));
                item.setBindType(bindType);
                Effect[] effects = loadEffects(node);
                item.setEffects(effects);
                
                item.setItemType(Byte.parseByte(node.attribute("important").getValue()));
                item.setQuarlity(Byte.parseByte(node.attribute("itemColor").getValue()));
                
                Items.addTemplate(item);
              
            }
            else if(type==2){
                ExtendedItemTemplate item = new ExtendedItemTemplate();
                att = node.attribute("itemID");
                int id = Integer.parseInt(att.getValue());
                item.setItemId(id);
                att = node.attribute("price");
                int price = Integer.parseInt(att.getValue());
                item.setPrice(price);
                att = node.attribute("title");
                String name = att.getValue();
                item.setName(name);
                byte bindType = Byte.parseByte(node.attributeValue("bind"));
                item.setBindType(bindType);
                byte canUse = Byte.parseByte(node.attributeValue("canUse"));
                item.setCanUse(canUse!=0?true:false);
                byte autoUse = Byte.parseByte(node.attributeValue("autoUse"));
                item.setAutoUse(autoUse!=0?true:false);
                item.setAutoUseMessage(node.attributeValue("autoUseMessage"));
                item.setDesc(node.attributeValue("desc"));
                Effect[] effects = loadEffects(node);
                item.setEffects(effects);
                item.setItemType(Byte.parseByte(node.attribute("important").getValue()));
                item.setQuarlity(Byte.parseByte(node.attribute("itemColor").getValue()));
                
                Items.addTemplate(item);
              
            }
            else if(type==1){
                TaskItemTemplate item = new TaskItemTemplate();
                att = node.attribute("itemID");
                int id = Integer.parseInt(att.getValue());
                item.setItemId(id);
                att = node.attribute("title");
                String name = att.getValue();
                item.setName(name);
                att = node.attribute("taskIDs");
                short taskId = Short.parseShort(att.getStringValue());
                item.setTaskId(taskId);
                att = node.attribute("max");
                int max = Integer.parseInt(att.getStringValue());
                item.setMax(max);
                item.setDesc(node.attributeValue("desc"));
               
                item.setItemType(Byte.parseByte(node.attribute("important").getValue()));
                item.setQuarlity(Byte.parseByte(node.attribute("itemColor").getValue()));
                
                Items.addTemplate(item);
            }
        }
    }

    private Effect[] loadEffects(Element node){
        List l = new ArrayList();
        for(Iterator ite = node.elementIterator("Effect");ite.hasNext();){
            Element el = (Element)ite.next();
            byte type = Byte.parseByte(el.attributeValue("paramType"));
            if(type==1){ //改变属性
                byte property = Byte.parseByte(el.attributeValue("property"));
                PropertyEffect effect = new PropertyEffect();
                effect.setProperty(property);
                int value = Integer.parseInt(el.attributeValue("effect"));
                effect.setValue(value);
                byte unit = Byte.parseByte(el.attributeValue("unit"));
                effect.setUnit(unit);
                int time = Integer.parseInt(el.attributeValue("time"));
                effect.setTime(time);
                l.add(effect);
            }
            else if(type==3){
                short mapId = Short.parseShort(el.attributeValue("mapid"));
                short x = Short.parseShort(el.attributeValue("x"));
                short y = Short.parseShort(el.attributeValue("y"));
                
                short newMapId = Short.parseShort(el.attributeValue("newMap"));
                short newX = Short.parseShort(el.attributeValue("newX"));
                short newY = Short.parseShort(el.attributeValue("newY"));
                MoveEffect effect = new MoveEffect();
                effect.setMapId(mapId);
                effect.setX(x);
                effect.setY(y);
                effect.setNewMapId(newMapId);
                effect.setNewX(newX);
                effect.setNewY(newY);
                l.add(effect);
            }
            else if(type==2){ //获得物品
                int itemId = Integer.parseInt(el.attributeValue("itemid"));
                GenEffect effect = new GenEffect();
                effect.setItemId(itemId);
                byte itemType = Byte.parseByte(el.attributeValue("type"));
                effect.setItemType(itemType);
                byte count = Byte.parseByte(el.attributeValue("count"));
                effect.setCount(count);
                l.add(effect);
            }
            else if(type==5){ //宠物忠诚
                int value = Integer.parseInt(el.attributeValue("value"));
                FavorEffect effect = new FavorEffect(value);
                l.add(effect);
            }
            else if(type==6){
                int value = Integer.parseInt(el.attributeValue("value"));
                AddGridEffect effect = new AddGridEffect(value);
                l.add(effect);
            }
            else if(type==7){
                String value = el.attributeValue("title");
                TitleEffect effect = new TitleEffect();
                effect.setTitle(value);
                l.add(effect);
            }
            else if(type==8){
                OneoffTitleEffect effect = new OneoffTitleEffect();
                l.add(effect);
            }
            else if(type==10){
                short mapId = Short.parseShort(el.attributeValue("mapid"));
                short minX= Short.parseShort(el.attributeValue("minx"));
                short minY = Short.parseShort(el.attributeValue("miny"));
                short maxX= Short.parseShort(el.attributeValue("maxx"));
                short maxY = Short.parseShort(el.attributeValue("maxy"));
                int itemGroupId = Integer.parseInt(el.attributeValue("itemgroup"));
                int shovelId = Integer.parseInt(el.attributeValue("shovelid"));
                TreasureEffect effect = new TreasureEffect();
                effect.setItemGroupId(itemGroupId);
                effect.setMapId(mapId);
                effect.setMinX(minX);
                effect.setMinY(minY);
                effect.setMaxX(maxX);
                effect.setMaxY(maxY);
                effect.setShovelId(shovelId);
                l.add(effect);
            }
            else if(type==11){
            	int delete = Integer.parseInt(el.attributeValue("delete"));
                TreasureFinderEffect effect = new TreasureFinderEffect(delete);
                l.add(effect);
            }
            else if(type==12){
                int validTime = Integer.parseInt(el.attributeValue("validtime"));
                int obsoleteTime = Integer.parseInt(el.attributeValue("obsoletetime"));
                int itemGroupId = Integer.parseInt(el.attributeValue("itemgroup"));
                int grassType = 0;
                Attribute at = el.attribute("grasstype");
                if(at!=null){
                    grassType = Integer.parseInt(at.getStringValue());
                }
                HopeGrassEffect effect = new HopeGrassEffect(grassType);
                effect.setValidTime(validTime);
                effect.setObsoleteTime(obsoleteTime);
                effect.setItemGroupId(itemGroupId);
                if(grassType==1){
                    int ratio = Integer.parseInt(el.attributeValue("ratio"));
                    effect.setRatio(ratio);
                    int grouprnd =  Integer.parseInt(el.attributeValue("grouprnd"));
                    effect.setGrouprnd(grouprnd);
                }
                l.add(effect);
            }
            else if(type==13){
                int grassType = 0;
                Attribute at = el.attribute("grasstype");
                if(at!=null)
                    grassType = Integer.parseInt(at.getText());
                HopeGrassFinderEffect effect = new HopeGrassFinderEffect(grassType);
                l.add(effect);
            }
            else if(type==14){
                TalkEffect effect = new TalkEffect();
                String channel = el.attributeValue("channel");
                String msg = el.attributeValue("message");
                effect.setChannel(channel);
                effect.setMessage(msg);
                l.add(effect);
            }
            else if (type == 15) {
                int validTime = Integer.parseInt(el.attributeValue("validtime"));
                int expradio = Integer.parseInt(el.attributeValue("expradio"));
                int moneyradio = Integer.parseInt(el.attributeValue(
                        "moneyradio"));
                LuckyBufEffect effect = new LuckyBufEffect();
                effect.setValidTime(validTime);
                effect.setExpRadio(expradio);
                effect.setMoneyRadio(moneyradio);
                l.add(effect);
            }
            else if(type==16){
                ShowLuckyTimeEffect effect = new ShowLuckyTimeEffect();
                l.add(effect);
            }
            else if(type==17){
                LookPackageEffect effect = new LookPackageEffect();
                l.add(effect);
            }
            else if(type==18){
                FreeMoveEffect effect = new FreeMoveEffect();
                l.add(effect);
            }
            else if(type==19){
                LolEffect effect = new LolEffect();
                l.add(effect);
            }
            else if(type==20){
                TolEffect effect = new TolEffect();
                l.add(effect);
            }
            else if(type==21){
                TopEffect effect = new TopEffect();
                l.add(effect);
            }
            else if(type==22){
                ResetPropertiesEffect effect = new ResetPropertiesEffect();
                l.add(effect);
            }
            else if(type==23){
                TomEffect effect = new TomEffect();
                l.add(effect);
            }
            else if(type==24){
                TeamMoveEffect effect = new TeamMoveEffect();
                l.add(effect);
            }
            else if(type==25){
                ChangeSexEffect effect = new ChangeSexEffect();
                l.add(effect);
            }
            else if(type==26){
                UnMarryEffect effect = new UnMarryEffect();
                l.add(effect);
            }
            else if(type==27){
                UnMasterEffect effect = new UnMasterEffect();
                l.add(effect);
            }
            else if(type==28){
                int scriptId = Integer.parseInt(el.attributeValue("scriptid"));
                String[] parameters = el.attributeValue("script").split("\\|");
                for(int i=0;i<parameters.length;i++){
                    parameters[i] = parameters[i].replace("\\n","\n");
                }
                InvestigationEffect effect = new InvestigationEffect();
                effect.setScriptId(scriptId);
                effect.setParameters(parameters);
                l.add(effect);
            }
            else if(type==29){
                LookBufEffect effect = new LookBufEffect();
                l.add(effect);
            }
            else if(type==30){
                int level = Integer.parseInt(el.attributeValue("level"));
                boolean baby = false;
                if(el.attributeValue("baby").equals("true")){
                    baby = true;
                }
                byte petType = Byte.parseByte(el.attributeValue("petType"));
                PetEffect effect = new PetEffect();
                effect.setLevel(level);
                effect.setBaby(baby);
                effect.setPetType(petType);
                l.add(effect);
            }
            else if(type== 31) {
            	QuestionBeginEffect effect = new QuestionBeginEffect();
            	l.add(effect);
            }
            else if(type==32) {
            	QuestionCleanEffect effect = new QuestionCleanEffect();
            	l.add(effect);
            }
            else if(type==33) {
            	QuestionContinueEffect effect = new QuestionContinueEffect();
            	l.add(effect);
            }
            else if(type==34) {
            	SuggestEffect effect = new SuggestEffect();
            	l.add(effect);
            }
            else if(type==35){
                HousePushEffect effect = new HousePushEffect();
                l.add(effect);
            }
            else if(type==36){
                PrivateHousePushEffect effect = new PrivateHousePushEffect();
                l.add(effect);
            }
            else if(type==37){
                GoHomeEffect effect = new GoHomeEffect();
                l.add(effect);
            }
            else if(type==38){
                int level = Integer.parseInt(el.attributeValue("level"));
                EnemyEffect effect = new EnemyEffect();
                effect.setLevel(level);
                l.add(effect);
            }
            else if(type==39){
                int group1 = Integer.parseInt(el.attributeValue("group1"));
                int group2 = Integer.parseInt(el.attributeValue("group2"));
                int boxId = Integer.parseInt(el.attributeValue("boxId"));
                String msg = el.attributeValue("msg");
                KeyEffect effect = new KeyEffect(group1,group2,boxId,msg);
                l.add(effect);
            }
            else if(type==40){
                int property = Integer.parseInt(el.attributeValue("property"));
                ResetSinglePropertyEffect effect = new ResetSinglePropertyEffect(property);
                l.add(effect);
            }
            else if(type==41){
                float count = Float.parseFloat(el.attributeValue("count"));
                AddExpEffect effect = new AddExpEffect(count);
                l.add(effect);
            }
            else if(type==42){
                int min = Integer.parseInt(el.attributeValue("min"));
                int max = Integer.parseInt(el.attributeValue("max"));
                AddCreditEffect effect = new AddCreditEffect(min,max);
                l.add(effect);
            }
            //mengjie add
            else if(type==43){//钱袋
                int percent = Integer.parseInt(el.attributeValue("percent"));
                int money = Integer.parseInt(el.attributeValue("money"));
                MoneypackageEffect effect = new MoneypackageEffect(percent,money);
                l.add(effect);
            }
            else if(type==44){//钥匙链
                int group1 = Integer.parseInt(el.attributeValue("group1"));
                int group2 = Integer.parseInt(el.attributeValue("group2"));
                int boxId = Integer.parseInt(el.attributeValue("boxId"));
                int count = Integer.parseInt(el.attributeValue("count"));//钥匙数
                KeysEffect effect = new KeysEffect(group1,group2,boxId,count);
                l.add(effect);
            }else if(type==45){//世界地图
                WorldMapEffect effect = new WorldMapEffect();
                l.add(effect);
            }else if(type==46){//保护盾道具
            	byte property = Byte.parseByte(el.attributeValue("property"));
            	SaveShieldEffect effect = new SaveShieldEffect();
                effect.setProperty(property);
                int value = Integer.parseInt(el.attributeValue("effect"));
                effect.setValue(value);
                byte unit = Byte.parseByte(el.attributeValue("unit"));
                effect.setUnit(unit);
                int time = Integer.parseInt(el.attributeValue("time"));
                effect.setTime(time);
                l.add(effect);
            }
            //mengjie add end
            else if(type == 47){//砸蛋道具
                int group = Integer.parseInt(el.attributeValue("groupId"));
                EggEffect effect = new EggEffect(group);
                l.add(effect);
            }else if(type==48){//改名符
            	RanameEffect effect = new RanameEffect();
                l.add(effect);
            }else if(type==49)
            {
            	int count = Integer.parseInt(el.attributeValue("count"));
            	AddPetExpEffect effect = new AddPetExpEffect(count);
            	l.add(effect);
            }else if(type==50){//等级可用
            	int level = Integer.parseInt(el.attributeValue("level"));
            	int itemid = Integer.parseInt(el.attributeValue("itemid"));
            	int type_ = Integer.parseInt(el.attributeValue("type"));
            	int count = Integer.parseInt(el.attributeValue("count"));
            	LevellimitEffect effect = new LevellimitEffect(level,itemid,type_,count);
                l.add(effect);
            }else if(type==51){//增加好友度
            	int count = Integer.parseInt(el.attributeValue("count"));
            	AddFriendFavoriteEffect effect = new AddFriendFavoriteEffect(count);
                l.add(effect);
            }else if(type==52){//推荐符
            	RecommendedEffect effect = new RecommendedEffect();
                l.add(effect);
            }else if(type==53){//imoney劵发工资
            	int imoney = Integer.parseInt(el.attributeValue("imoney"));
            	int outtype = Integer.parseInt(el.attributeValue("outtype"));
            	SuperQimoneyEffect effect = new SuperQimoneyEffect(outtype,imoney);
                l.add(effect);
            }else if(type==54){//CMCC特殊的钥匙
            	int cmcctype = Integer.parseInt(el.attributeValue("cmcctype"));
            	int group1 = Integer.parseInt(el.attributeValue("group1"));
                int group2 = Integer.parseInt(el.attributeValue("group2"));
                int itemid = Integer.parseInt(el.attributeValue("itemid"));
                int boxId = Integer.parseInt(el.attributeValue("boxId"));
                String msg = el.attributeValue("msg");
                CMCCKeyEffect effect = new CMCCKeyEffect(cmcctype,group1,group2,itemid,boxId,msg);
                l.add(effect);
            }else if(type==55){//介绍宝典-使用后可弹出msg
            	int deleteflag = Integer.parseInt(el.attributeValue("delete"));
                String msg = el.attributeValue("msg");
                MassageEffect effect = new MassageEffect(deleteflag,msg);
                l.add(effect);
            }else if(type==56){//宝石
            	int color = Integer.parseInt(el.attributeValue("color"));
            	int property = Integer.parseInt(el.attributeValue("property"));
            	int value = Integer.parseInt(el.attributeValue("value"));
            	int level = Integer.parseInt(el.attributeValue("level"));
            	GemstoneEffect effect = new GemstoneEffect(color,property,value,level);
                l.add(effect);
            }else if(type == 57){			//家园的仓库的扩展
            	int value = Integer.parseInt(el.attributeValue("value"));
            	AddGridEffectHouse effect = new AddGridEffectHouse(value);
            	l.add(effect);
            } else if (type == 58) { // 宠物铠化石
            	int parts = Integer.parseInt(el.attributeValue("parts"));
            	int value = Integer.parseInt(el.attributeValue("value"));
            	int probability = Integer.parseInt(el.attributeValue("probability"));
            	PetArmorGemstoneEffect effect = new PetArmorGemstoneEffect(parts, value, probability);
            	l.add(effect);
            } else if(type == 59) { // 换装物品
            	int faceId = Integer.parseInt(el.attributeValue("faceId"));
            	DressItemEffect effect = new DressItemEffect(faceId);
            	l.add(effect);
            }else if(type == 60) { // 控制是否删除
            	int removeItem = Integer.parseInt(el.attributeValue("removeItem"));
            	RemoveItemEffect effect = new RemoveItemEffect(removeItem);
            	l.add(effect);
            }else if(type == 61){  //使用物品，可以获得掉落组的物品列表
                int group = Integer.parseInt(el.attributeValue("groupId"));
                String count = el.attributeValue("count");
                int countInt = 1;
                if(count != null){
                	countInt = Integer.parseInt(count);
                    if(countInt == 0){
                    	countInt = 1;
                    }
                }
                String strParamType = el.attributeValue("dropParamType");
                String strTypeParam = el.attributeValue("dropParam");
                int paramType = 0;
                int param = 0;
                if(strParamType != null && strTypeParam != null){
                	paramType = Integer.parseInt(strParamType);
                	param = Integer.parseInt(strTypeParam);
                }
                
                DropGroupListEffect effect = new DropGroupListEffect(group,countInt, paramType, param);
                l.add(effect);
            }else if(type==62){//酷夏金卡
            	int group1 = Integer.parseInt(el.attributeValue("group1"));
                int group2 = Integer.parseInt(el.attributeValue("group2"));
                SummerKeyEffect effect = new SummerKeyEffect(group1,group2);
                l.add(effect);
            } else if (type == 63) {
            	RoarEffect effect = new RoarEffect();
                String channel = el.attributeValue("channel");
                String msg = el.attributeValue("message");
                effect.setChannel(channel);
                effect.setMessage(msg);
                l.add(effect);
            } else if (type == 64) {
            	int sex = Integer.parseInt(el.attributeValue("sex"));
            	int camp = Integer.parseInt(el.attributeValue("camp"));
            	int level = Integer.parseInt(el.attributeValue("level"));
            	SendCampSuitEffect effect = new SendCampSuitEffect(sex, camp, level);
                l.add(effect);
            } else if (type == 65) {		//配方效果
            	int recipeId = Integer.parseInt(el.attributeValue("recipeId"));
            	PrescriptionEffect effect = new PrescriptionEffect(recipeId);
                l.add(effect);
            } else if (type == 66) {	//  宠物悟性经验增加
            	int value = Integer.parseInt(el.attributeValue("value"));
            	AddPerceptionPointEffect effect = new AddPerceptionPointEffect(value);
            	l.add(effect);
            } else if (type == 67) {	//  宠物灵性增加
            	AddSpiritualityEffect effect = new AddSpiritualityEffect();
            	l.add(effect);
            } else if (type == 68) {	//  赠送他人物品自动使用
            	int itemtype = Integer.parseInt(el.attributeValue("type"));
            	int itemid = Integer.parseInt(el.attributeValue("itemid"));
            	int usetype = Integer.parseInt(el.attributeValue("usetype"));
            	int count = Integer.parseInt(el.attributeValue("count"));
            	int paramtype = Integer.parseInt(el.attributeValue("param_Type"));
            	int auto = Integer.parseInt(el.attributeValue("auto"));
            	int addgroupid = Integer.parseInt(el.attributeValue("addgroupid"));
            	GiftItemAutoUseEffect effect = new GiftItemAutoUseEffect(itemtype,itemid,usetype,count,paramtype,auto,addgroupid);
            	l.add(effect);
            } else if (type == 69) {
            	int value = Integer.parseInt(el.attributeValue("value"));
            	AddBuildProficiencyEffect effect = new AddBuildProficiencyEffect(value);
            	l.add(effect);
            } else if (type == 70) {		//增加活力
            	int value = Integer.parseInt(el.attributeValue("value"));
            	AddLife effect = new AddLife(value);
            	l.add(effect);
            } else if(type == 71){		//情人节物品
            	byte sex = Byte.parseByte(el.attributeValue("sex"));
            	int giftid = Integer.parseInt(el.attributeValue("groupID"));
            	FriendGift friendgift = new FriendGift(sex, giftid);
            	l.add(friendgift);
            }else if(type == 72){			//增加I券
            	int value = Integer.parseInt(el.attributeValue("value"));
            	int server = Integer.parseInt(el.attributeValue("server"));
            	AddIMoneyQuan effect = new AddIMoneyQuan(value, server);
            	l.add(effect);
            }else if(type == 73){
            	byte index = Byte.parseByte(el.attributeValue("index"));
            	byte lifeCycle = Byte.parseByte(el.attributeValue("lifeCycle"));
            	AddItemAnimate effect = new AddItemAnimate(index,lifeCycle);
            	l.add(effect);
            } else if (type == 74) {
            	byte mainPerceptionLevel = Byte.parseByte(el.attributeValue("mainPerceptionLevel"));
            	byte secondPerceptionLevel = Byte.parseByte(el.attributeValue("secondPerceptionLevel"));
            	byte setPerceptionLevel = Byte.parseByte(el.attributeValue("setPerceptionLevel"));
            	SecondGenerationPetEffect effect = new SecondGenerationPetEffect(mainPerceptionLevel, secondPerceptionLevel, setPerceptionLevel);
            	l.add(effect);
            }else if(type == 75){
                byte vianyType = Byte.parseByte(el.attributeValue("vianyType"));
                PlayerVianyEffect effect = new PlayerVianyEffect(vianyType);
                l.add(effect);
            }else if(type == 76){//表情称号
            	byte phizType = Byte.parseByte(el.attributeValue("phizType"));
            	short phizIndex = Short.parseShort(el.attributeValue("phizIndex"));
            	String phizName = el.attributeValue("phizName");
            	PhizTitleEffect effect = new PhizTitleEffect(phizType,phizIndex,phizName);
            	l.add(effect);
            	PhizTitleData tmpPhiz = new PhizTitleData(phizIndex,phizType,phizName);
            	PhizTitleData.addPhizTitle(phizIndex, tmpPhiz);
            }else if(type == 77){//统御值
            	int value = Integer.parseInt(el.attributeValue("value"));
            	LeadershipEffect effect = new LeadershipEffect(value);
            	l.add(effect);
            }else if(type == 78){//装备
            	int id = Integer.parseInt(el.attributeValue("equmodleid"));
            	int itemid = Integer.parseInt(el.attributeValue("equid"));
            	EquModleEffect effect = new EquModleEffect(id, itemid);
            	l.add(effect);
            }else if(type == 79){//装备
            	int level = Integer.parseInt(el.attributeValue("explevel"));
            	int itemid = Integer.parseInt(el.attributeValue("outlevelitemid"));
            	int count = Integer.parseInt(el.attributeValue("itemcount"));
            	UpLevelEffect effect = new UpLevelEffect(level, itemid, count);
            	l.add(effect);
            }else if(type == 80){//增加宠物灵性悟性等级
            	int perceptionLevel = Integer.parseInt(el.attributeValue("perceptionLevel"));
            	int spiritualLevel = Integer.parseInt(el.attributeValue("spiritualLevel"));
            	PetSetupEffect effect = new PetSetupEffect(perceptionLevel, spiritualLevel);
            	l.add(effect);
            }else if(type == 81){//幸运时间
            	int lucktime = Integer.parseInt(el.attributeValue("lucktime"));
            	LuckTimeEffect effect = new LuckTimeEffect(lucktime);
            	l.add(effect);
            }else if(type == 82){	//七夕情人节物品
            	int groupID = Integer.parseInt(el.attributeValue("groupID"));
            	Love7Effect effect = new Love7Effect(groupID);
            	l.add(effect);
            }else if(type == 83){	//公会荣誉和贡献值
            	int Contribution = Integer.parseInt(el.attributeValue("Contribution"));
            	int TongCredit = Integer.parseInt(el.attributeValue("TongCredit"));
            	TongValueEffect effect = new TongValueEffect(Contribution, TongCredit);
            	l.add(effect);
            }else if(type == 84){	//宠物变色
            	byte petType = Byte.parseByte(el.attributeValue("petType"));
            	byte petBindType = Byte.parseByte(el.attributeValue("petBindType"));
            	byte changeWay = Byte.parseByte(el.attributeValue("random"));
            	short colorIndex = Short.parseShort(el.attributeValue("colorIndex"));
            	PetColorEffect effect = new PetColorEffect(colorIndex, changeWay,petType,petBindType);
            	l.add(effect);
            }else if(type == 85){	//种子
            	int seedID = Integer.parseInt(el.attributeValue("seedID"));
            	SeedEffect effect = new SeedEffect(seedID);
            	l.add(effect);
            }else if(type == 86){   //永久增加属性值
            	int strength = Integer.parseInt(el.attributeValue("strength"));
                int agility = Integer.parseInt(el.attributeValue("agility"));
                int vitality = Integer.parseInt(el.attributeValue("vitality"));
                int intelligence = Integer.parseInt(el.attributeValue("intelligence"));
                AddAttributeEffect effect = new AddAttributeEffect(strength,agility,vitality,intelligence);
                l.add(effect);
            }else if(type == 87){	//随缘物语
            	LetItBeEffect effect = new LetItBeEffect();
            	l.add(effect);
            }else if(type == 88){	//丘比特之箭
            	TheArrowOfLoveEffect effect = new TheArrowOfLoveEffect();
            	l.add(effect);
            }else if(type == 89){	//领袖效果
            	int hpeffect = Integer.parseInt(el.attributeValue("hpEffect"));
            	CampleaderEffect effect = new CampleaderEffect(hpeffect);
            	l.add(effect);
            }else if(type == 90){	//新钱袋效果
            	int min = Integer.parseInt(el.attributeValue("minMoney"));
            	int max = Integer.parseInt(el.attributeValue("maxMoney"));
            	MoneyEffect effect = new MoneyEffect(min, max);
            	l.add(effect);
            }else if(type == 91){	//NBShow
            	NBEffect effect = new NBEffect();
            	l.add(effect);
            }else if(type == 92){//开学礼包
            	SchoolGiftBagEffect effect = new SchoolGiftBagEffect();
            	l.add(effect);
            }else if(type == 93){//获得蓝色妖姬
            	BlueFlowerEffect effect = new BlueFlowerEffect();
            	l.add(effect);
            }else if(type == 94){//获得10个高级打孔符
            	HighHoleEffect effect = new HighHoleEffect();
            	l.add(effect);
            }else if(type == 95){//获得5瓶圣水
            	GodWaterEffect effect = new GodWaterEffect();
            	l.add(effect);
            }else if(type == 96){//白色情人节大礼包
            	WhiteLoverBagEffect effect = new WhiteLoverBagEffect();
            	l.add(effect);
            }else if(type == 97){//鸳鸯情侣定向包
            	LoverImageEffect effect = new LoverImageEffect();
            	l.add(effect);
            }else if(type == 98){//获得聚灵灵力
            	SoulEffect effect = new SoulEffect();
            	l.add(effect);
            }else if(type == 99){//超级经验包
            	ExpEffect effect = new ExpEffect();
            	l.add(effect);
            }else if(type == 100){//超级大礼包
            	SuperGiftBagEffect effect = new SuperGiftBagEffect();
            	l.add(effect);
            }else if(type == 101){//16区冲级奖励
            	SpurtGiftEffect effect = new SpurtGiftEffect();
            	l.add(effect);
            }else if(type == 102){//一生一世礼包
            	oneIsWholeLifeEffect effect = new oneIsWholeLifeEffect();
            	l.add(effect);
            }else if(type == 103){	//掉落组里装备钻数配置,并可以设置是否绑定
            	int dropGroupId = Integer.parseInt(el.attributeValue("dropGroupId"));
            	int diamondCount = Integer.parseInt(el.attributeValue("diamondCount"));
            	int resetBinded = 0;
            	int setBinded = 0;
            	String strResetBinded = el.attributeValue("resetBinded");
            	if(strResetBinded != null){
            		resetBinded = Integer.parseInt(strResetBinded);
            	}
            	//只有设置了重置绑定状态才需要读取绑定状态
            	if(resetBinded != 0){
            		String strSetBinded = el.attributeValue("setBinded");
            		if(strSetBinded != null){
            			setBinded = Integer.parseInt(strSetBinded);
            		}
            	}
            	DropGroupDiamondEffect effect = new DropGroupDiamondEffect(dropGroupId, diamondCount, resetBinded == 0 ? false : true, setBinded == 0 ? false : true);
            	l.add(effect);
            }else if(type == 104){	//使用的物品转换成另外的物品
            	int needItemID = Integer.parseInt(el.attributeValue("needItemID"));
            	int needItemCount = Integer.parseInt(el.attributeValue("needItemCount"));
            	int changeItemID = Integer.parseInt(el.attributeValue("changeItemID"));
            	int changeItemCount = Integer.parseInt(el.attributeValue("changeItemCount"));
            	UseChangeItemEffect effect = new UseChangeItemEffect(needItemID, needItemCount, changeItemID, changeItemCount);
            	l.add(effect);
            }else if(type == 105){	//占卜之力
            	int value = Integer.parseInt(el.attributeValue("value"));
            	DivineEffect effect = new DivineEffect(value);
            	l.add(effect);
            }else if(type == 106){  //使用的物品转换成另外的物品
                int deletedItemId = Integer.parseInt(el.attributeValue("deletedItemId"));
                int deletedItemCount = Integer.parseInt(el.attributeValue("deletedItemCount"));
                int addedItemId = Integer.parseInt(el.attributeValue("addedItemId"));
                int addedItemCount = Integer.parseInt(el.attributeValue("addedItemCount"));
                DeleteAddItemEffect effect = new DeleteAddItemEffect(deletedItemId, deletedItemCount, addedItemId, addedItemCount);
                l.add(effect);
            }
        }
        Effect[] ret = new Effect[l.size()];
        l.toArray(ret);
        return ret;
    }

//    public static void main(String[] args){
//        String s = "haha\n1.aaaa\n2.bbb|hbhb";
////        String[] ss = java.util.regex.Pattern.compile("\\|", java.util.regex.Pattern.MULTILINE).split(s);
//        String[] ss = s.split("\\|");
//        System.out.println(ss.length);
//        for(int i=0;i<ss.length;i++){
//            System.out.println(ss[i]);
//        }
//    }


//    private void initGenEffect(GenEffect effect,Element node){
//        for(Iterator ite = node.elementIterator("Param");ite.hasNext();){
//            Element el = (Element)ite.next();
//            String s= el.attributeValue("name");
//            if("type".equals(s)){
//                byte type = Byte.parseByte(el.attributeValue("value"));
//                effect.setItemType(type);
//            }
//            else if("count".equals(s)){
//                byte count = Byte.parseByte(el.attributeValue("value"));
//                effect.setCount(count);
//            }
//        }
//    }

//    private void initPropertyEffect(PropertyEffect effect,Element node){
//        for(Iterator ite = node.elementIterator("Param");ite.hasNext();){
//            Element el = (Element)ite.next();
//            String s = el.attributeValue("name");
//            if("effect".equals(s)){
//                int value = Integer.parseInt(el.attributeValue("value"));
//                effect.setValue(value);
//            }
//            else if("time".equals(s)){
//                int time = Integer.parseInt(el.attributeValue("value"));
//                byte unit = Byte.parseByte(el.attributeValue("unit"));
//                effect.setTime(time);
//                effect.setUnit(unit);
//            }
//        }
//    }

}
