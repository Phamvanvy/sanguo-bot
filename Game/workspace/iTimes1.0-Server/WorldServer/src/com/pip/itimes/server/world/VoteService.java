package com.pip.itimes.server.world;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.pip.itimes.server.bean.Vote;
import com.pip.itimes.server.bean.VoteContent;
import com.pip.itimes.server.dao.DataAccessException;
import com.pip.itimes.server.dao.PlayerDao;
import com.pip.itimes.server.dao.VoteContentDao;
import com.pip.itimes.server.dao.VoteDao;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.DressItemEffect;
import com.pip.itimes.server.stage.Effect;
import com.pip.itimes.server.stage.IEffectItem;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.IItemTemplate;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.stage.RoleFaceData;
import com.pip.itimes.server.stage.RoleFaces;
import com.pip.itimes.server.stage.TitleEffect;
import com.pip.itimes.server.stage.VoteGiftDefine;
import com.pip.itimes.server.stage.VoteGiftItem;
import com.pip.itimes.server.stage.VoteGiftItemGroup;
import com.pip.itimes.server.stage.VoteGiftItemGroups;
import com.pip.itimes.server.stage.VoteInfo;
import com.pip.itimes.server.stage.VoteKingInfo;
import com.pip.itimes.server.stage.VotePlayerGift;
import com.pip.itimes.server.stage.VoteShowInfo;
import com.pip.itimes.server.stage.VotesKing;
import com.pip.itimes.server.stage.voteGiftGroups;
import com.pip.itimes.server.util.Utils;

/**
 * 用于选举的服务
 * @author wpjiang
 *
 */
public class VoteService {
	 private static final Logger log = Logger.getLogger(VoteService.class);
	 private VoteDao voteDao;
	 private VoteContentDao voteContendao;
	 private PlayerDao playerDao;
	 private static File votefile; //保存文件目录，用于reload
	 protected MailService mailService;
	 protected PlayerService playerService;
	 protected ConnectService connectService;
	 protected ChatService chatService;
	 
	 // 活动没有开始
	 public static final int STAGE_NOT_STARTED = 0;
	 // 活动已经开始
	 public static final int STAGE_BEGIN = 1;
	 // 活动已经结束
	 public static final int STAGE_END = 2;
	 // 当前活动的类型1七夕美女宝贝,2【幻想魅力男士】,3【幻想之“宇宙之心小姐”大奖赛】,4【幻想之“爱宠女人”大奖赛】,5【幻想之“风流男人”大奖赛】
	 
	 public void setMailService(MailService mailService) {
        this.mailService = mailService;
     }
	 
	 public void setPlayerService (PlayerService playerService) {
		 this.playerService = playerService;
	 }
	 
	 public void setConnectService (ConnectService connectService) {
		 this.connectService = connectService;
	 }
	 
	 public void setChatService (ChatService chatService) {
		 this.chatService = chatService;
	 }
	 
	/**
	 * 格式化时间
	 */
	protected static SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
	
	/**
	 * @param voteType
	 * @return跟据类型返回选举参加记录
	 */
	public static Map<Integer,VoteShowInfo> getVoteContentMap(int voteType){
		VotePlayerGift votePlayerGift = voteGiftGroups.getGiftGroup(voteType);
		return votePlayerGift.getVoteContentMap().get(voteType);
	}
	
	public static ArrayList<VoteInfo> getVoteSet(int voteType){
		VotePlayerGift votePlayerGift = voteGiftGroups.getGiftGroup(voteType);
		return votePlayerGift.getVoteMap().get(voteType);
	}
	
	public VoteService(VoteDao voteDao, VoteContentDao voteContentDao, PlayerDao playerDao) throws Exception{;
		this.voteContendao = voteContentDao;
		this.voteDao = voteDao;
		this.playerDao = playerDao;
	}
	
	public static ArrayList <VotesKing> getVotesKingSet (int voteType) {
		VotePlayerGift votePlayerGift = voteGiftGroups.getGiftGroup(voteType);
		return votePlayerGift.getVotesKingMap().get(voteType);
	}
	
	public static Map <Integer, VoteKingInfo> getVotesKingInfo (int voteType) {
		VotePlayerGift votePlayerGift = voteGiftGroups.getGiftGroup(voteType);
		return votePlayerGift.getVotesKingInfoMap().get(voteType);
	}
	
	/**
	 * 记载选举
	 * @param file
	 * @throws Exception 
	 */
	public static void loadVote(File file) throws Exception{
		votefile = file;
		 String stageDirName = file.getAbsolutePath();
	     String voteDirName = FilenameUtils.concat(stageDirName,"Items/vote.xml");
	     //giftGroupLoader = new GiftGroupLoader(new File(voteDirName));
	     File voteFile = new File(voteDirName);
	     SAXReader reader = new SAXReader();
	     Document doc = reader.read(voteFile);
	     loadVoteGiftGroups(doc);
	}
	
	public static void loadVoteGiftGroups(Document doc) throws Exception{
		Element root = doc.getRootElement();
		
		VoteGiftItemGroups.clearVoteGiftItemGroup();
		voteGiftGroups.clearVoteGiftGroups();
		
        if(root == null){
            return;
        }

        for(Iterator i = root.elementIterator("vote"); i.hasNext();){
            Element giftGoupNode = (Element)i.next();
            Attribute attrGiftGroup = giftGoupNode.attribute("valid");
            boolean valid = attrGiftGroup.getValue().equals("true");
            if(!valid){
            	continue;
            }
            
            VotePlayerGift votePlayGift = new VotePlayerGift();
            
            attrGiftGroup = giftGoupNode.attribute("id");
            if(Integer.parseInt(attrGiftGroup.getValue()) >= 32){
            	log.info("VoteService start failed,  id can't > 32");
            	throw new Exception();
            }
            int voteType = Integer.parseInt(attrGiftGroup.getValue());
            votePlayGift.setId(voteType);
            
            attrGiftGroup = giftGoupNode.attribute("votelevel");
            votePlayGift.setVoteLevel(Integer.parseInt(attrGiftGroup.getValue()));
            
            attrGiftGroup = giftGoupNode.attribute("voteplayerlevel");
            votePlayGift.setVoteplayerlevel(Integer.parseInt(attrGiftGroup.getValue()));
            
            attrGiftGroup = giftGoupNode.attribute("maintype");
            votePlayGift.setMainType(Integer.parseInt(attrGiftGroup.getValue()));
            
            attrGiftGroup = giftGoupNode.attribute("votetype");
            votePlayGift.setVoteType(Integer.parseInt(attrGiftGroup.getValue()));
            
            attrGiftGroup = giftGoupNode.attribute("begindate");
            votePlayGift.setBeginTime(Long.parseLong(attrGiftGroup.getValue()));
            
            attrGiftGroup = giftGoupNode.attribute("enddate");
            votePlayGift.setEndTime(Long.parseLong(attrGiftGroup.getValue()));
            
            votePlayGift.setVoteTitle(getMessage("message_title", giftGoupNode));
            votePlayGift.setVoteContent(getMessage("message_content", giftGoupNode));
            votePlayGift.setVoteBag(getMessage("message_bag", giftGoupNode));
            votePlayGift.setManIntroduction(getMessage("message_manIntroduction", giftGoupNode));
            votePlayGift.setWomanIntroduction(getMessage("message_womanIntroduction", giftGoupNode));
            votePlayGift.setManAwardIntroduction(getMessage("message_manAwardIntroduction", giftGoupNode));
            votePlayGift.setWomanAwardIntroduction(getMessage("message_womanAwardIntroduction", giftGoupNode));
            votePlayGift.setRoar(getMessage("message_roar", giftGoupNode));
            votePlayGift.setEndRoar(getMessage("message_endroar", giftGoupNode));
            votePlayGift.setEndWorld(getMessage("message_endworld", giftGoupNode));
            votePlayGift.setWinnersNumEntry(Integer.parseInt(getMessage("winners_num_entry", giftGoupNode)));
            votePlayGift.setWinnersNumVote(Integer.parseInt(getMessage("winners_num_vote", giftGoupNode)));
            
            for(Iterator j = giftGoupNode.elementIterator("votegift"); j.hasNext();){
                Element giftNode = (Element)j.next();
                VoteGiftDefine gift = new VoteGiftDefine();

                Attribute attrGift = giftNode.attribute("id");
                gift.setId(Integer.parseInt(attrGift.getValue()));
                
                attrGift = giftNode.attribute("beginlevel");
                gift.setBeginLevel(Integer.parseInt(attrGift.getValue()));

                attrGift = giftNode.attribute("endlevel");
                gift.setEndLevel(Integer.parseInt(attrGift.getValue()));

                for(Iterator k = giftNode.elementIterator("needitem"); k.hasNext();){
                    Element giftItemNode = (Element)k.next();
                    
                    Attribute attrGiftItem = giftItemNode.attribute("itemid");
                    int itemId = Integer.parseInt(attrGiftItem.getValue());
                    
                    attrGiftItem = giftItemNode.attribute("count");
                    int count = Integer.parseInt(attrGiftItem.getValue());
                    
                    attrGiftItem = giftItemNode.attribute("votePoint");
                    int votePoint = Integer.parseInt(attrGiftItem.getValue());
                    
                    attrGiftItem = giftItemNode.attribute("isImoneyItem");
                    byte isImoneyItem = Byte.parseByte(attrGiftItem.getValue());
                    
                    gift.addNeedItem(itemId, count);
                    gift.addItemsVotePoint(itemId, votePoint);
                    gift.setIsImoneyItem(isImoneyItem);
                }

                for(Iterator k = giftNode.elementIterator("giveitem"); k.hasNext();){
                    Element giftItemNode = (Element)k.next();
                    
                    Attribute attrGiftItem = giftItemNode.attribute("itemid");
                    int itemId = Integer.parseInt(attrGiftItem.getValue());
                    
                    attrGiftItem = giftItemNode.attribute("count");
                    int count = Integer.parseInt(attrGiftItem.getValue());
                    
                    gift.addGiveItems(itemId, count);
                }
                votePlayGift.addVoteGiftDefine(gift);
            }
            
            for (Iterator j = giftGoupNode.elementIterator("type"); j.hasNext();) {
                Element node = (Element)j.next();
                String name = node.attributeValue("name");
                if (name.equals("参赛")) {
                	VoteGiftItem[] items = loadItems(node);
                	VoteGiftItemGroup group = new VoteGiftItemGroup("参赛", items);
                	votePlayGift.addVoteGiftItem(group);
                } else if (name.equals("投票")) {
                	VoteGiftItem[] items = loadItems(node);
                	VoteGiftItemGroup group = new VoteGiftItemGroup("投票", items);
                	votePlayGift.addVotesKingGiftItem(group);
                }
            }
            log.info("load vote gift" + votePlayGift.getId());
            voteGiftGroups.addVoteGiftGroup(votePlayGift);
        }
        setVoteStage();
	}
	public static String getMessage(String nodeName, Element groupNode){
        Element messageNode = groupNode.element(nodeName);
        if(messageNode == null) return null;
        return messageNode.attributeValue("value");
    }
	/**
	 * 加载选举将所有的数据都放在内存里
	 * @throws DataAccessException 
	 */
	public void loadVoteData(int voteType) throws DataAccessException{
		VotePlayerGift votePlayerGift = voteGiftGroups.getGiftGroup(voteType);
		ConcurrentHashMap<Integer, VotePlayerGift> votegiftGroupReference =  voteGiftGroups.getGiftGroupReference();
		//获取key的集合
	    Set<Integer> keySet = votegiftGroupReference.keySet();
	      //遍历key集合
	    votePlayerGift.getVoteMap().clear();
	    for(int key : keySet) {
	    	List voteList= voteDao.getAll(key, formatter.format(votePlayerGift.getStartDate()), formatter.format(votePlayerGift.getEndDate()));
	    	ArrayList<VoteInfo> set = new ArrayList<VoteInfo>();
	    	for(int i=0; i < voteList.size(); i++){
	    		Object[] object = (Object[])voteList.get(i);
	    		int id = (Integer)object[0];
	    		int point =  ((Long)object[1]).intValue();
	    		//根据玩家id查找玩家名称
	    		//String playerName = playerDao.getPlayerName(id);
	    		VoteInfo voteInfo= new VoteInfo(id, point);
	    		//log.info("被投票的玩家id" + id + "玩家姓名" +playerName + "被投票数" + point );
	    		set.add(voteInfo);
	    	}
	    	//记载完后排序
	    	Collections.sort(set);
	    	//按照类型放入map里面
	    	votePlayerGift.putVoteMap(key, (ArrayList<VoteInfo>)set);
	    } 
	    log.info("voteService started");
	}
	
	/**
	 * 收费道具投票大王
	 * @param voteType
	 * @param playerId
	 * @param content
	 * @throws DataAccessException
	 */
	public void loadVotesKing (int voteType) throws DataAccessException {
		VotePlayerGift votePlayerGift = voteGiftGroups.getGiftGroup(voteType);
		ConcurrentHashMap <Integer, VotePlayerGift> votegiftGroupReference =  voteGiftGroups.getGiftGroupReference();
	    Set <Integer> keySet = votegiftGroupReference.keySet();
	    votePlayerGift.getVotesKingMap().clear();
	    votePlayerGift.getVotesKingInfoMap().clear();
	    for (int key : keySet) {
	    	List votesKingList = voteDao.getVotesKing(key, true, formatter.format(votePlayerGift.getStartDate()), formatter.format(votePlayerGift.getEndDate()));
	    	ArrayList <VotesKing> set = new ArrayList <VotesKing> ();
	    	Map <Integer, VoteKingInfo> kingInfoMap = new HashMap <Integer, VoteKingInfo> ();
	    	for (int i = 0; i < votesKingList.size(); i++) {
	    		Object[] votesKing = (Object[]) votesKingList.get(i);
	    		int id = (Integer)votesKing[0];
	    		long point = ((Long) votesKing[1]).intValue();
	    		VotesKing king= new VotesKing(id, point);
	    		set.add(king);
	    		
	    		List kingInfo = playerDao.getPlayerNameAndLevel(id);
	    		if (kingInfo == null || kingInfo.size() == 0) {
	    			continue;
	    		}
	    		Object[] votesKingObject = (Object[]) kingInfo.get(0);
	    		String kingName = (String) votesKingObject[0];
	    		int level = (Integer) votesKingObject[1];
	    		String tongName = (String) votesKingObject[2];
	    		VoteKingInfo votesKingInfo = new VoteKingInfo(level, kingName, tongName, point);
	    		kingInfoMap.put(id, votesKingInfo);
	    	}
	    	Collections.sort(set);
	    	votePlayerGift.putVotesKingMap(key, (ArrayList<VotesKing>) set);
	    	votePlayerGift.putVotesKingInfoMap(key, kingInfoMap);
	    } 
	}
	
	/**
	 * 加入选举参赛记录
	 * @param voteType
	 * @param playerId
	 * @param content
	 * @throws DataAccessException
	 */
	public void saveVoteContent(int voteType, int playerId, String content) throws DataAccessException{
		VoteContent voteContent = new VoteContent();
		voteContent.setVotersid(playerId);
		voteContent.setType(voteType);
		voteContent.setContent(content);
		voteContent.setValid(true);
		voteContent.setCreatetime(new Date());
		voteContendao.addVoteContent(voteContent);
	}
	
	/**
	 * 加载数据表中的所有参赛纪录
	 * @throws DataAccessException 
	 */
	public void loadVoteContentData(int voteType) throws DataAccessException{
		VotePlayerGift votePlayerGift = voteGiftGroups.getGiftGroup(voteType);
		ConcurrentHashMap<Integer, VotePlayerGift> votegiftGroupReference =  voteGiftGroups.getGiftGroupReference();
		//获取key的集合
	    Set<Integer> keySet = votegiftGroupReference.keySet();
	    votePlayerGift.getVoteContentMap().clear();
	      //遍历key集合
	    for(int key : keySet) {
	    	List voteContentList= voteContendao.getAll(key, formatter.format(votePlayerGift.getStartDate()), formatter.format(votePlayerGift.getEndDate()));
	    	Map<Integer, VoteShowInfo> map = new HashMap<Integer, VoteShowInfo>();
	    	for(int i=0; i < voteContentList.size(); i++){
	    		Object[] object = (Object[])voteContentList.get(i);
	    		int id = (Integer)object[0];
	    		boolean valid = (Boolean) object[1];
	    		String voteContent = (String)object[2];
	    		List tempList = playerDao.getPlayerNameAndLevel(id);
	    		if(tempList == null || tempList.size() == 0){
	    			continue;
	    		}
	    		Object[] temp = (Object[])tempList.get(0);
	    		String playerName = (String)temp[0];
	    		int level = (Integer)temp[1];
	    		String tongName = (String)temp[2];
	    		VoteShowInfo voteShowInfo = new VoteShowInfo(level, playerName, tongName, voteContent, valid);
	    		map.put(id, voteShowInfo);
	    		//set.add(voteInfo);
	    	}
	    	votePlayerGift.putVoteContentMap(key, map);
	    }
	}

	/**
	 * 增加内存中的现实信息
	 * @param voteType
	 * @param player
	 * @param voteContent
	 * @throws Exception
	 */
	public void setVoteContent (int voteType, WorldPlayer player, String voteContent, boolean valid) throws Exception{
		VotePlayerGift votePlayerGift = voteGiftGroups.getGiftGroup(voteType);
		Map<Integer, Map<Integer,VoteShowInfo>> temp = votePlayerGift.getVoteContentMap();
		if(temp.containsKey(voteType)){
			Map<Integer, VoteShowInfo> map = temp.get(voteType);
			VoteShowInfo voteShowInfo = new VoteShowInfo(player.getLevel(), player.getPlayerName(), player.getTongName(), voteContent, valid);
    		map.put(player.getId(), voteShowInfo);
		}else{
			throw new Exception();
		}
	}
	
	public void saveVote(Vote vote) throws DataAccessException{
		voteDao.saveVote(vote);
	}
	
	/**
	 * @param voteType
	 * @param playerId
	 * @return 选举他数量最多的前10位玩家id
	 * @throws DataAccessException
	 */
	public List getVotePlayers(int voteType, int playerId) throws DataAccessException{
		VotePlayerGift votePlayerGift = voteGiftGroups.getGiftGroup(voteType);
		if (votePlayerGift != null) {
			return voteDao.getVotePlayers(voteType, playerId, formatter.format(votePlayerGift.getStartDate()), formatter.format(votePlayerGift.getEndDate()));
		} else {
			return null;
		}
	}
	
	/**
	 * 修改数据并修改内存中的showInfo
	 * @param voteType
	 * @param player
	 * @param voteContent
	 */
	public void upDataVoteContent(int voteType, WorldPlayer player, String voteContent)throws DataAccessException{
		voteContendao.update(voteType, player.getId(), voteContent);
		VotePlayerGift votePlayerGift = voteGiftGroups.getGiftGroup(voteType);
		Map<Integer, VoteShowInfo> map = votePlayerGift.getVoteContentMap().get(voteType);
		if(map.containsKey(player.getId())){
			VoteShowInfo voteShowInfo = map.get(player.getId());
			voteShowInfo.setVoteContent(voteContent);
		}
	}
	
	/**
	 * 修改所有内存中的showInfo.valid数据
	 * @param valid
	 * @param type
	 */
	public void upDataVoteValid (int type, int playerId)throws DataAccessException {
		voteContendao.updateValid(type, playerId);
	}
	
	public void reload() throws Exception{
		loadVote(votefile);
		Enumeration<Integer> enumer = voteGiftGroups.getEnumeration();
		while (enumer.hasMoreElements()) {
			int voteType = (Integer) enumer.nextElement();
			loadVoteContentData(voteType);
			loadVoteData(voteType);
			VotePlayerGift votePlayerGift = voteGiftGroups.getGiftGroup(voteType);
		    if (votePlayerGift.notShowVotesKing() == false) {
		    	loadVotesKing (voteType);
		    }
		}
	}
	/*public void setForbid(int id) throws DataAccessException{
		//删除内存
		
			for(Entry<Integer, ArrayList<VoteInfo>> temp: voteMap.entrySet()){
				ArrayList<VoteInfo> voteArrayList = temp.getValue();
				Iterator it = voteArrayList.iterator();
				while(it.hasNext())
				{
	        	   VoteInfo voteInfo = (VoteInfo)it.next(); 
	        	   if(voteInfo.getId() == id){
	        		   voteArrayList.remove(voteInfo);
	        		   break;
	        	   }
				}
			}
			
			for(Entry<Integer, Map<Integer,VoteShowInfo>> temp: voteContentMap.entrySet()){
				Map<Integer,VoteShowInfo> voteShowMap= temp.getValue();
				if(voteShowMap.containsKey(id)){
					voteShowMap.remove(id);
					break;
				}
			}
			
		//删除数据库
			voteDao.deleteVote(id);
			voteContendao.delete(id);
	}*/
	
	private static VoteGiftItem[] loadItems(Element node){
		Map<Integer, VoteGiftItem> map = new HashMap<Integer, VoteGiftItem>();
        List l = new ArrayList();
        for (Iterator i = node.elementIterator("voteGift"); i.hasNext(); ) {
            Element n = (Element)i.next();
            int id = Integer.parseInt(n.attributeValue("giftid"));
            IItemTemplate item = Items.getTemplate(id);
            if (item!=null) {
            	int rank = Integer.parseInt(n.attributeValue("rank"));
        		int count = Integer.parseInt(n.attributeValue("count"));
        		int day = Integer.parseInt(n.attributeValue("day"));
                VoteGiftItem vgItem = new VoteGiftItem();
                vgItem.item = item;
        		vgItem.count = count;
        		vgItem.rank = rank;
        		vgItem.day = day;
                l.add(vgItem);
            }
        }
        VoteGiftItem[] items = new VoteGiftItem[l.size()];
        l.toArray(items);
        return items;
    }
	
	// 自动发奖
	public void prizesAfterEvent () throws Exception {
		Enumeration<Integer> enumer = voteGiftGroups.getEnumeration();
		while (enumer.hasMoreElements()) {
			int voteType = (Integer) enumer.nextElement();
			VotePlayerGift votePlayerGift = voteGiftGroups.getGiftGroup(voteType);
			if (votePlayerGift != null) {
				Date date_tmp = new Date();
				if (date_tmp.getTime() < votePlayerGift.getBeginTime().getTime()) {
					votePlayerGift.setStage(STAGE_NOT_STARTED);
				} else if (date_tmp.getTime() >= votePlayerGift.getBeginTime().getTime() && date_tmp.getTime() <= votePlayerGift.getEndTime().getTime()) {
					votePlayerGift.setStage(STAGE_BEGIN);
				}
			}
			if (votePlayerGift.getStage() == STAGE_BEGIN) {
				if (votePlayerGift.getSendChat()) {
					votePlayerGift.setSendChat(false);
					String msg = votePlayerGift.getRoar();
					if (msg != null) {
						sendRoarChat(msg);
					}
				}
				Date date_tmp = new Date();
				if (votePlayerGift != null && date_tmp.getTime() > votePlayerGift.getEndTime().getTime()) {
					votePlayerGift.setStage(STAGE_END);
					String endRoar = votePlayerGift.getEndRoar();
					String endWorld = votePlayerGift.getEndWorld();
					Vector<VoteInfo> entryIdVector = getEntryWinnersId(voteType);
					if (entryIdVector != null) {
						Map<Integer,VoteShowInfo> voteShowMap = getVoteContentMap(voteType);
						for (int i = 0; i < entryIdVector.size() && i < votePlayerGift.getWinnersNumEntry(); i ++) {
//							boolean mark = false;
//							WorldPlayer player = playerService.getWorldPlayer(entryIdVector.get(i).getId());
//							if (player == null) {
//								player = playerService.loadWorldPlayer(entryIdVector.get(i).getId());
//								mark = true;
//							}
							WorldPlayer player = playerService.getWorldPlayerAndCatch(entryIdVector.get(i).getId());
							VoteShowInfo voteShowInfo = voteShowMap.get(entryIdVector.get(i).getId());
							if (player != null && voteShowInfo.getValid()) {
								log.info("VoteWinners_Entry playerId[" + entryIdVector.get(i).getId() + "] playerName[" + player.getPlayerName() + "] votesPoint[" + entryIdVector.get(i).getVotePoint() + "] voteType [" + VoteGiftItemGroups.ENTRY + "] No. [" + (i + 1) +"] ");
								int rank = i + 1;
								VoteGiftItem[] item = votePlayerGift.getGiftItem(rank, VoteGiftItemGroups.ENTRY);
								for (int j = 0; j < item.length; j ++) {
									if (item[j] != null) {
										IItem iit = Items.getTemplate(item[j].item.getItemId()).newInstance();
										String mailcontant = "亲爱的“" + player.getPlayerName()+ "”，恭喜您获得了" + votePlayerGift.getVoteTitle() + "第" + rank + "名，";
										boolean needRemove = useGiftItem(player, iit, item[j],mailcontant);
										if (!needRemove) {
											byte[] att = ItemUtils.item2dbAttachment(iit, item[j].count);
											mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
													iit.getName() + "*" + item[j].count, "亲爱的“" + player.getPlayerName()
													+ "”，恭喜您获得了" + votePlayerGift.getVoteTitle() + "第" + rank + "名，这是您的奖品" + iit.getName(), att, 0, true);
											log.info("VoteService sendMail vote gift to playerId[" + player.getId()
													+ "] playerName[" + player.getPlayerName() + "] get ItemId[" +
													iit.getItemId() + "] ItemName[" + iit.getName() + "] ItemCount[" + item[j].count + "]");
										}
									}
								}
								if(endRoar != null){
									endRoar = endRoar.replaceAll(VotePlayerGift.STR_WINER + rank, player.getPlayerName());
								}
								if(endWorld != null){
									endWorld = endWorld.replaceAll(VotePlayerGift.STR_WINER + rank, player.getPlayerName());
								}
//								if (mark) {
//									player.resetImage();
//									playerService.unRegistry(player);
//									playerService.savePlayer(player);
//								}
							}
							playerService.releasePlayer(player);
							upDataVoteValid(voteType, entryIdVector.get(i).getId());
						}
					}
					Vector<VotesKing> voteIdVector = getVoteWinnersId(voteType);
					if (voteIdVector != null) {
						for (int i = 0; i < voteIdVector.size() && i < votePlayerGift.getWinnersNumVote(); i ++) {
//							boolean mark = false;
//							WorldPlayer player = playerService.getWorldPlayer(voteIdVector.get(i).getId());
//							if (player == null) {
//								player = playerService.loadWorldPlayer(voteIdVector.get(i).getId());
//								mark = true;
//							}
							WorldPlayer player = playerService.getWorldPlayerAndCatch(voteIdVector.get(i).getId());
							if (player != null) {
								log.info("VoteWinners_Vote playerId[" + voteIdVector.get(i).getId() + "] playerName[" + player.getPlayerName() + "] votesPoint[" + voteIdVector.get(i).getvotes() + "] voteType [" + VoteGiftItemGroups.VOTE + "]");
								VoteGiftItem[] item = votePlayerGift.getGiftItem(i + 1, VoteGiftItemGroups.VOTE);
								for (int j = 0; j < item.length; j ++) {
									if (item[j] != null) {
										IItem iit = Items.getTemplate(item[j].item.getItemId()).newInstance();
										String mailcontant = "亲爱的“" + player.getPlayerName()+ "”，恭喜您获得了" + votePlayerGift.getVoteTitle() + "投票大王，";
										boolean needRemove = useGiftItem(player, iit, item[j],mailcontant);
										if (!needRemove) {
											byte[] att = ItemUtils.item2dbAttachment(iit, item[j].count);
											mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
													iit.getName() + "*" + item[j].count, "亲爱的“" + player.getPlayerName()
													+ "”，恭喜您获得了" + votePlayerGift.getVoteTitle() + "投票大王，这是您的奖品" + iit.getName(), att, 0, true);
											log.info("VoteService sendMail vote gift to playerId[" + player.getId()
													+ "] playerName[" + player.getPlayerName() + "] get ItemId[" +
													iit.getItemId() + "] ItemName [" + iit.getName() + "] ItemCount[" + item[j].count + "]");
										}
									}
								}
								if(endRoar != null){
									endRoar = endRoar.replaceAll(VotePlayerGift.STR_VOTE + (i + 1), player.getPlayerName());
								}
								if(endWorld != null){
									endWorld = endWorld.replaceAll(VotePlayerGift.STR_VOTE + (i + 1), player.getPlayerName());
								}
//								if (mark) {
//									playerService.unRegistry(player);
//									playerService.savePlayer(player);
//								}
								playerService.releasePlayer(player);
							}
						}
					}
					if(endRoar != null){
						int index = endRoar.indexOf(VotePlayerGift.STR_WINER);
						while(index >= 0){
							endRoar = endRoar.substring(0, index) + VotePlayerGift.STR_NOBODY + endRoar.substring(index + VotePlayerGift.STR_WINER.length() + 2, endRoar.length());
							index = endRoar.indexOf(VotePlayerGift.STR_WINER);
						}
						index = endRoar.indexOf(VotePlayerGift.STR_VOTE);
						while(index >= 0){
							endRoar = endRoar.substring(0, index) + VotePlayerGift.STR_NOBODY + endRoar.substring(index + VotePlayerGift.STR_VOTE.length() + 2, endRoar.length());
							index = endRoar.indexOf(VotePlayerGift.STR_VOTE);
						}
						chatService.sendRoarMessage( -1, "狮子吼", endRoar, true, 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0);
					}
					if(endWorld != null){
						int index = endWorld.indexOf(VotePlayerGift.STR_WINER);
						while(index >= 0){
							endWorld = endWorld.substring(0, index) + VotePlayerGift.STR_NOBODY + endWorld.substring(index + VotePlayerGift.STR_WINER.length() + 2, endWorld.length());
							index = endWorld.indexOf(VotePlayerGift.STR_WINER);
						}
						index = endRoar.indexOf(VotePlayerGift.STR_VOTE);
						while(index >= 0){
							endWorld = endWorld.substring(0, index) + VotePlayerGift.STR_NOBODY + endWorld.substring(index + VotePlayerGift.STR_VOTE.length() + 2, endWorld.length());
							index = endWorld.indexOf(VotePlayerGift.STR_VOTE);
						}
						chatService.sendWorldMessage(-1, "系统", endWorld);
					}
				}
			}
		}
	}
	
	public boolean useGiftItem (WorldPlayer player, IItem iit, VoteGiftItem item,String mailcontant) {
		IEffectItem giftItem = (IEffectItem) iit;
		Effect[] effects = giftItem.getEffects();
		Changed changed = new Changed();
		boolean needRemove = false;
		for (int i = 0; i < effects.length; i++) {
			if (effects[i].getType() == 7) {
				mailcontant += "奖励称号已放入称号橱窗，感谢您的参与。";
				 TitleEffect effect = (TitleEffect) effects[i];
                 int ret = player.isCanChangeRoleTitle(effect.getTitle());
                 if(ret == 0){		//橱窗里无此称号
                 	player.completeAddRoleTitle(effect.getTitle());			//添加到称号橱窗里
                 }
                 mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                		 iit.getName(), mailcontant, null, 0, true);
                 log.info("VoteService CompleteAddRoleTitle to playerId [" + player.getId() + "] getTitle [" + effect.getTitle() + "] and this time [" + new Date() + "]");
                 needRemove = true;
			} else if (effects[i].getType() == 59) {//形象不再放到橱窗里,因为有的形象是非绑定的,发邮件可提取到背包
//				mailcontant += "奖励形象已放入形象橱窗，感谢您的参与。";
//				DressItemEffect effect = (DressItemEffect)effects[i];
//				RoleFaceData face = RoleFaces.getRoleFace(effect.getFaceId());
//				if (face != null && player.getSex() % 2 == face.getFace() % 2) {
//					int error = player.isCanBuyFace(face.getFace());
//					if (error != 1) {
//						long time = -1;
//						if (item.day > 0) {
//							time = 1000L * item.day * 24 * 60 * 60;
//						}
//						player.completeAddRoleFace(face.getFace(), item.count, changed, time);
//						mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
//		                		 iit.getName(), mailcontant, null, 0, true);
//						log.info("VoteService CompleteAddRoleFace to playerId [" + player.getId() + "] getFace [" + face.getFace() + "] getTime [" + new Date() + " ] getExpiration [" + item.day + "] days");
//					}
//				}
				needRemove = false;
			}
		}
		return needRemove;
	}
	
	/**
	 * 获得选美名次ID
	 * @throws DataAccessException 
	 */
	public Vector<VoteInfo> getEntryWinnersId (int voteType) throws DataAccessException {
		loadVoteData(voteType);
		loadVoteContentData(voteType);
		ArrayList<VoteInfo> voteTreeSet = getVoteSet(voteType);
		if (voteTreeSet != null && voteTreeSet.isEmpty() == false && voteTreeSet.size() > 0) {
			Vector<VoteInfo> playerIdVector = new Vector<VoteInfo>();
            Iterator it = voteTreeSet.iterator();
            int i = 0;
            while (it.hasNext()) {
            	VoteInfo voteInfo = (VoteInfo)it.next(); 
            	playerIdVector.add(voteInfo);
           	 	i++;
            }
            return playerIdVector;
		} else {
			return null;
		}
	}
	
	/**
	 * 获得投票名次ID
	 * @throws DataAccessException 
	 */
	public Vector<VotesKing> getVoteWinnersId (int voteType) throws DataAccessException {
		loadVotesKing(voteType);
		ArrayList<VotesKing> voteTreeSet = getVotesKingSet(voteType);
		if (voteTreeSet != null && voteTreeSet.isEmpty() == false && voteTreeSet.size() > 0) {
			Vector<VotesKing> playerIdVector = new Vector<VotesKing>();
            Iterator it = voteTreeSet.iterator();
            int i = 0;
            while (it.hasNext()) {
            	VotesKing votesKing = (VotesKing)it.next(); 
            	playerIdVector.add(votesKing);
           	 	i++;
            }
            return playerIdVector;
		} else {
			return null;
		}
	}
	
	public static void setVoteStage () {
		Enumeration<Integer> enumer = voteGiftGroups.getEnumeration();
		while (enumer.hasMoreElements()) {
			int voteType = (Integer) enumer.nextElement();
			VotePlayerGift votePlayerGift = voteGiftGroups.getGiftGroup(voteType);
			if (votePlayerGift != null) {
				Date date_tmp = new Date();
				if (date_tmp.getTime() < votePlayerGift.getBeginTime().getTime()) {
					votePlayerGift.setStage(STAGE_NOT_STARTED);
				} else if (date_tmp.getTime() >= votePlayerGift.getBeginTime().getTime() && date_tmp.getTime() <= votePlayerGift.getEndTime().getTime()) {
					votePlayerGift.setStage(STAGE_BEGIN);
				}
				votePlayerGift.setStartDate(votePlayerGift.getBeginTime().getTime());
				votePlayerGift.setEndDate(votePlayerGift.getEndTime().getTime());
			}
		}
	}
	
	public void sendRoarChat (String msg) {
		chatService.sendRoarMessage( -1, "狮子吼", msg, true, 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0);
	}
}
