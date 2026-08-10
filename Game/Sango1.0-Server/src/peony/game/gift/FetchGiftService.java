package peony.game.gift;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import peony.game.CommonUtil;
import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.service.Service;

public class FetchGiftService implements Service {
	
	private static Logger log = Logger.getLogger(FetchGiftService.class);

	public static final int TYPE_ONCE_INACTIVITY = 0; // 只能领取一次
	public static final int TYPE_EVERYDAY_INACTIVITY = 1; // 每天可以领取一次

	List<FetchGiftRule> fgr = new ArrayList<FetchGiftRule>();

	public void checkRuleAndSendGift(Player p, int type) throws Exception{
		if(p!=null){
			FetchGiftRule f = getFetchGiftRule(type);
			if(f!=null){
				if(f.enable == 0){
					throw new Exception(peony.Messages.STRING_01005);
				}
				int tempSubtype = getSubType(p,f);
				if(p.level > f.levelLimit){
					sendGift(p,f,tempSubtype);
				} else {
					String msg = MessageFormat.format(peony.Messages.STRING_01006,f.levelLimit);
					throw new Exception(msg);
				}
			} else {
				throw new Exception(peony.Messages.STRING_00611);
			}
		}
	}
	
	private int getSubType(Player p,FetchGiftRule f){
		int subType = 0;
		if(f.giftType == 0){//星级特效
			int totalStar = p.getAveStar(p.equipments.equs,0);
			if(totalStar == 4)  subType = 0;
			else if(totalStar == 6) subType = 1;
			else if(totalStar == 8) subType = 2;
		}else if(f.giftType == 1){//宝石特效
			int flashLevel = p.equipments.getFlashLevel();
			if(flashLevel == 3) subType = 0;
			if(flashLevel == 4) subType = 1;
			if(flashLevel == 5) subType = 2;
			if(flashLevel == 6) subType = 3;
			if(flashLevel == 7) subType = 4;
				
		}
		return subType;
	}
	
	public boolean checkRule(Player p,FetchGiftRule f,int subtype){
		if(p!=null){
			if(f.giftType == 0){
				int totalStar = p.getAveStar(p.equipments.equs,0);
				int level = 0;
				if(subtype == 0)  level = 4;
				else if(subtype == 1) level = 6;
				else if(subtype == 2) level = 8;
				if(totalStar >= level)
					return true;
			} else if(f.giftType == 1){
				int flashLevel = p.equipments.getFlashLevel();
				if(flashLevel >= subtype+1)
					return true;
			}
		}
		return false;
	}
	
	
	public void sendGift(Player p,FetchGiftRule f,int subtype) throws Exception{
		if(p!=null){
			if(f.fetchLimit == TYPE_ONCE_INACTIVITY && p.pool.getInt(getProperty(f.giftType,subtype), 0)!=0){
				throw new Exception(peony.Messages.STRING_01007);
			} else if(f.fetchLimit == TYPE_EVERYDAY_INACTIVITY && p.pool.getInt(getProperty(f.giftType,subtype), 0)==Time.day){
				throw new Exception(peony.Messages.STRING_01008);
			} else {
				if(checkRule(p,f,subtype)){
					GiftType gift = f.getGifById(subtype);
					int itemId = gift.reward;
					GameItem rewardItem = ObjectAccessor.createGameItem(itemId);
					PlayerTransaction tx = p.newTransaction("FETCHGIFT");
					try {
						p.bag.addGameItemComplete(rewardItem, 1, tx, true);
						tx.commit();
					} catch (Exception e) {
						tx.rollback();
						String content = MessageFormat.format(peony.Messages.STRING_01009,gift.typeName);
						Server.server.getServiceRegistry().getMailService()
								.sendSystemMail(p.id, peony.Messages.STRING_00004, peony.Messages.STRING_01010, content, 0,
										rewardItem, 1, "FETCHGIFT");
					}
					p.pool.setInt(getProperty(f.giftType,subtype), Time.day);
				} else {
					GiftType gift = f.getGifById(subtype);
					String msg = peony.Messages.STRING_01011;
					throw new Exception(msg);
				}
			}
		}
	}

	public void shutdown() {
        
	}

	public void startup() throws Exception {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data
		.findFile("sendgift.xml");
		try {
			Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
			parse(doc);
		} catch (Exception e) {
			log.error(e, e);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void parse(Document doc){
		Element root = doc.getRootElement();
		if(root != null){
			List<Element> list = root.elements();
			for(Element l:list){
				int id = Integer.parseInt(l.attributeValue("id"));
				int levelLimit = Integer.parseInt(l.attributeValue("levellimit"));
				int fetchLimit = Integer.parseInt(l.attributeValue("fetchlimit"));
				int enable = Integer.parseInt(l.attributeValue("enable"));
				FetchGiftRule fgr = new FetchGiftRule(id,levelLimit,fetchLimit,enable);
				List<Element> types = l.elements("type");
				if(types!=null){
					for(Element t : types){
						int typeId = Integer.parseInt(t.attributeValue("id"));
						String typeName = t.attributeValue("name");
						int reward = Integer.parseInt(t.attributeValue("reward"));
						GiftType gt = new GiftType(typeId,typeName,reward);
						fgr.addGifts(gt);
					}
				}
				addFetchGiftRule(fgr);
			}
		}
	}

	public void addFetchGiftRule(FetchGiftRule f) {
		fgr.add(f);
	}
	
	public String getProperty(int type,int subtype){
		if(type == 0)
			return "PROPERTY_EFFECT_STAR"+subtype;
		else if(type == 1)
			return "PROPERTY_EFFECT_FLASH"+subtype;
		return "";
	}

	public FetchGiftRule getFetchGiftRule(int type) {
		if (fgr != null && fgr.size() != 0) {
			for (FetchGiftRule f : fgr) {
				if (f.giftType == type)
					return f;
			}
		}
		return null;
	}

}

class FetchGiftRule {
	public int giftType;
	public int levelLimit;
	public int fetchLimit;
	public int enable;
	List<GiftType> gifts = new ArrayList<GiftType>();
	
	public FetchGiftRule(int giftType,int levelLimit,int fetchLimit,int enable){
		this.giftType = giftType;
		this.levelLimit = levelLimit;
		this.fetchLimit = fetchLimit;
		this.enable = enable;
	}
	
	public void addGifts(GiftType gift){
		gifts.add(gift);
	}
	
	public GiftType getGifById(int giftId){
		if(gifts!=null && gifts.size()>0){
			for(GiftType g : gifts){
				if(g.id == giftId)
					return g;
			}
		}
		return null;
	}
}

class GiftType {
	public int id;
	public String typeName;
	public int reward;
	public GiftType(int id,String typeName,int reward){
		this.id = id;
		this.typeName = typeName;
		this.reward = reward;
	}
}
