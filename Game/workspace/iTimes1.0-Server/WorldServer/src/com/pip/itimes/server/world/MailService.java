package com.pip.itimes.server.world;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.bean.Auction;
import com.pip.itimes.server.bean.Mail;
import com.pip.itimes.server.dao.DataAccessException;
import com.pip.itimes.server.dao.MailDao;
import com.pip.itimes.server.stage.Attachment;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.IEquipment;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.ItemAttachment;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.stage.MoneyAttachment;
import com.pip.itimes.server.stage.Pet;
import com.pip.itimes.server.stage.PlayerData;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.camp.CampMainService;
import com.pip.itimes.server.world.game.IRefresh;
import com.pip.itimes.server.world.game.RefreshService;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class MailService implements Runnable {

    private static final Logger log = Logger.getLogger(MailService.class);

    private MailDao dao;

    private Map forbids = new HashMap();

    private RefreshService timer = new RefreshService();

    private PlayerService playerService;
    private ConnectService connectService;
    private AdminService adminService;
    private ChatService chatService;
    private CampMainService campMainService;
    
    private int count;

	public void setChatService(ChatService chatService) {
		this.chatService = chatService;
	}

    public MailService(MailDao dao) {
        this.dao = dao;
    }

    public void setAdminService(AdminService adminService) {
        this.adminService = adminService;
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    public void setConnectService(ConnectService connectService) {
        this.connectService = connectService;
    }
    
    public void setCampMainService(CampMainService campMainService) {
        this.campMainService = campMainService;
    }
    
    //jwp add start
    public int getMailCount(WorldPlayer player) throws MailException{//获得邮件个数
    	int count;
		try {
			count = dao.getMailCount(player.getId());
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			throw new MailException("没有可查询的邮件");

		}
    	return count;
    }
    public MailList getPageMail(WorldPlayer player,short pageSize,int pageNo, int sourceId) throws MailException, DataAccessException{//获得所有邮件
    	try {
	    	 int count = dao.getMailCount(player.getId());
	         if (pageSize * pageNo >= count) {
	             throw new MailException("没有可显示的邮件");
	
	         } else {
	        	 List list = null;
	        	 list = dao.getMailAttachmentLimit(player.getId(),pageSize * pageNo,
                         pageSize,player.getBlackListIds(),sourceId);
	        	 int pageCount = count / pageSize;
	             if (count % pageSize != 0)
	            	 pageCount++;
	             MailList ret = new MailList(pageCount, list);
	        	 return ret;
	         }
         }catch (Exception e) {
        	 throw new MailException("您暂时没有邮件");
		}
    	
    }
    
    /*public MailList getPageAttachentMail(WorldPlayer player,short pageSize,int pageNo, int sourceId) throws MailException, DataAccessException{//获得所有邮件
    	try {
	    	 int count = dao.getMailCount(player.getId());
	         if (pageSize * pageNo >= count) {
	             throw new MailException("没有可显示的邮件");
	
	         } else {
	        	 List list = null;
	        	 list = dao.getMailPageAttachmentLimit(player.getId(),pageSize * pageNo,
                         pageSize,player.getBlackListIds());
	        	 int pageCount = count / pageSize;
	             if (count % pageSize != 0)
	            	 pageCount++;
	             MailList ret = new MailList(pageCount, list);
	        	 return ret;
	         }
         }catch (Exception e) {
        	 throw new MailException("您暂时没有邮件");
		}
    	
    }*/
    
    //jwp add end

    public MailList getMailList(WorldPlayer player, short pageSize, int pageNo) throws MailException {
        try {
            int count = dao.getMailCount(player.getId());
            if (pageSize * pageNo >= count) {
                throw new MailException("没有可显示的邮件");

            } else {
                List l = dao.getMailList(player.getId(),
                                         pageSize * pageNo,
                                         pageSize,player.getBlackListIds());
                int pageCount = count / pageSize;
                if (count % pageSize != 0)
                    pageCount++;
                MailList ret = new MailList(pageCount, l);
                return ret;
//                UWAPSegment seg = new UWAPSegment(ClientConstants.MAIL_LIST,
//                                                  data.getSerial());
//                seg.writeShort(pageSize);
//                seg.writeInt(pageNo);
//                seg.writeInt(pageCount);
//                seg.writeShort((short) size);
//                for (int i = 0; i < size; i++) {
//                    Mail mail = (Mail) l.get(i);
//                    seg.writeInt(mail.getId());
//                    seg.writeString(mail.getSourceName());
//                    seg.writeString(mail.getTitle());
//                    seg.writeString(Utils.getDateString(mail.getPostTime()));
//                    byte[] att = mail.getAttachment();
//                    if(att==null||att.length==0){
//                        seg.writeBoolean(false);
//                    }else{
//                        seg.writeBoolean(true);
//                    }
//                    seg.writeBoolean(mail.getReaded());
//                }
//                stub.write(seg);
            }
        } catch (Exception e) {
            throw new MailException("您暂时没有邮件");

        }
    }

    /**
     * 从player中扣除attachment,并将attachment转换为数据库格式
     * @param player PlayerData
     * @param bytes byte[]
     * @return byte[]
     * @throws MailException
     * @throws IOException
     */

    public byte[] removeAttachment(PlayerData player, byte[] bytes) throws
            MailException, IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bis);
        byte type = dis.readByte();
        if (type == 1) { //money
            int money = dis.readInt();
            if (money < 0) {
                Utils.log(log, player.getId(), -1, "企图刷钱");
                throw new MailException("钱不能为负");
            }
            int oldMoney = player.getMoeny();
            if (oldMoney >= money) {
                player.setMoeny(oldMoney - money);
                return ItemUtils.money2dbAttachment(money);
            } else
                throw new MailException("钱不够");
        } else if (type == 2) { //basicItem
            int id = dis.readInt();
            byte count = dis.readByte();
            if (count < 0)
                throw new MailException("物品数量错误");
            IItem item = Items.getTemplate(id).newInstance();
            item = player.completeRemoveItem(item, count, null);
            if (item != null) {
                return ItemUtils.item2dbAttachment(item, count);
            } else {
                throw new MailException("物品不够");
            }
        } else if (type == 3) { //taskItem
            throw new MailException("物品已经被绑定");
        } else if (type == 4) { //extendedItem
            int id = dis.readInt();
            byte count = dis.readByte();
            if (count < 0)
                throw new MailException("物品数量错误");
            IItem item = Items.getTemplate(id).newInstance();
            if (item.isBinded())
                throw new MailException("物品已经被绑定");
            item = player.completeRemoveItem(item, count, null);
            if (item != null) {
                return ItemUtils.item2dbAttachment(item, count);
            } else {
                throw new MailException("物品不够");
            }
        } else if (type == 5) { //equip
            int itemId = dis.readInt();
            int id = dis.readInt();
            IEquipment equ = player.getEquipment(itemId, id);
            if (equ == null)
                throw new MailException("找不到此物品");
            if (equ.isBinded())
                throw new MailException("物品已经被绑定");
            IItem item = player.completeRemoveItem(equ, id, null);
            if (item != null) {
                return ItemUtils.item2dbAttachment(item, id);
            } else {
                throw new MailException("物品不够");
            }
        } else if (type == 6) { //pet
            int id = dis.readInt();
            Pet pet = player.getPet(id);
            if (pet == null)
                throw new MailException("找不到此宠物");
            if (pet == player.getPet())
                throw new MailException("不能邮寄装备上的宠物");
            if (pet.getFavor() < 30)
                throw new MailException("宠物好感度太低");
            player.removePet(pet);
            pet.setFavor(30);
            return ItemUtils.pet2dbAttachment(pet);
        }

        return null;
    }

    public void addMail(Mail mail) {
        try {
            dao.addMail(mail);
        } catch (DataAccessException ex) {
        }
    }

    public void saveMail(Mail mail){
        try {
            dao.makePersistent(mail);
        } catch (DataAccessException ex) {
        }
    }

    public Mail getMail(int id) {
        try {
            return dao.getMail(id);
        } catch (DataAccessException ex) {
            return null;
        }
    }


    public void sendAuctionMail(PlayerData player, Auction auction) {
        try {
            Mail mail = new Mail();
            mail.setSourceId( -1);
            mail.setSourceName("系统");
            mail.setReaded(false);
            mail.setPrice( -1);
            mail.setPostTime(new Date());
            mail.setValidTime(new Date(System.currentTimeMillis() + 30L * 24L * 3600L * 1000L));
            mail.setTitle("拍卖");
            mail.setDestId(player.getId());
            mail.setDestName(player.getPlayerName());
            mail.setContent("");
            mail.setAttachment(auction.getItem());
            dao.addMail(mail);
            mail = new Mail();
            mail.setSourceId( -1);
            mail.setSourceName("系统");
            mail.setReaded(false);
            mail.setPrice( -1);
            mail.setPostTime(new Date());
            mail.setValidTime(new Date(System.currentTimeMillis() + 30L * 24L * 3600L * 1000L));
            mail.setTitle("拍卖返回");
            mail.setDestId(auction.getPlayerId());
            mail.setDestName(auction.getPlayerName());
            mail.setContent("");
            mail.setAttachment(ItemUtils.money2dbAttachment(auction.getCurrentPrice()));
            dao.addMail(mail);
        } catch (DataAccessException ex) {
            log.error(ex, ex);
        }
    }

    public void sendAuctionMail(Auction auction) {
        try {
            if (auction.getLastPlayerId() != -1) {
                Mail mail = new Mail();
                mail.setSourceId( -1);
                mail.setSourceName("系统");
                mail.setReaded(false);
                mail.setPrice( -1);
                mail.setPostTime(new Date());
                mail.setValidTime(new Date(System.currentTimeMillis() + 30L * 24L * 3600L * 1000L));
                mail.setTitle("竞标成功");
                mail.setDestId(auction.getLastPlayerId());
                mail.setDestName("");
                mail.setContent("");
                mail.setAttachment(auction.getItem());
                dao.addMail(mail);
                mail = new Mail();
                mail.setSourceId( -1);
                mail.setSourceName("系统");
                mail.setReaded(false);
                mail.setPrice( -1);
                mail.setPostTime(new Date());
                mail.setValidTime(new Date(System.currentTimeMillis() + 30L * 24L * 3600L * 1000L));
                mail.setTitle("拍卖成功");
                mail.setDestId(auction.getPlayerId());
                mail.setDestName("");
                mail.setContent("");
                mail.setAttachment(ItemUtils.money2dbAttachment(auction.getCurrentPrice()));
                dao.addMail(mail);
            } else {
                Mail mail = new Mail();
                mail.setSourceId( -1);
                mail.setSourceName("系统");
                mail.setReaded(false);
                mail.setPrice( -1);
                mail.setPostTime(new Date());
                mail.setValidTime(new Date(System.currentTimeMillis() + 30L * 24L * 3600L * 1000L));
                mail.setTitle("拍卖失败");
                mail.setDestId(auction.getPlayerId());
                mail.setDestName("");
                mail.setContent("");
                mail.setAttachment(auction.getItem());
                dao.addMail(mail);
            }
        } catch (DataAccessException ex) {
            log.error(ex, ex);
        }
    }

    public void sendAuctionFail(Auction auction) {
        byte[] attachment = ItemUtils.money2dbAttachment(auction.getCurrentPrice());
        sendMail(auction.getLastPlayerId(), "", -1, "系统", "竞标" + auction.getName() + "失败", "您对本商品的出价居然被人超过了，这也能忍？", attachment, -1, false);
    }


    public void sendMail(int destId, String destName, int srcId, String srcName, String title,
                         String content, byte[] attachment, int price, boolean notify) {
        try {
            Mail mail = new Mail();
            mail.setSourceId(srcId);
            mail.setSourceName(srcName);
            mail.setReaded(false);
            mail.setPrice(price);
            mail.setPostTime(new Date());
            mail.setValidTime(new Date(System.currentTimeMillis() + 30L * 24L * 3600L * 1000L));
            mail.setDestId(destId);
            mail.setDestName(destName);
            mail.setTitle(title);
            mail.setContent(content);
            mail.setAttachment(attachment);
            dao.addMail(mail);
            WorldPlayer player = playerService.getWorldPlayer(destId);
            if (player != null && player.online()) {
                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                  MAIL_NEW);
                connectService.writeTo(seg, destId);
            }
            if (srcId == -1) {
                log.info("SystemMail DestID[" + destId + "] attachment[" + Utils.getHexdump(attachment) + "]");
            }
        } catch (DataAccessException ex) {
            log.info("SystemMail Fail DestID[" + destId + "] attachment[" + Utils.getHexdump(attachment) + "]");
            log.error(ex, ex);
        }
    }

    public Mail clientSendMail(WorldPlayer src, int destId, String destName, String title,
                               String content, byte[] attachment, int price) throws
            MailException {
        if (isForbiden(src.getId())) {
            throw new MailException("已经被禁止此项功能");
        }
        if(Server.iMoneyType != Server.IMONEY_TYPE_PIP){
	        if (price < 0)
	            throw new MailException("价格不能为负数");
        }else{
	        if(price < 0 && price < -99){
	        	throw new MailException("I币卡个数不能超过99个");
	        }
        }
        if (destId == -1)
            throw new MailException("没有此用户");
        if (src.getId() == destId)
            throw new MailException("不能发信给自己");
        if (!Utils.checkString(title)) {
            throw new MailException("信件标题存在非法字符");
        }
        if (!Utils.checkString(content)) {
            throw new MailException("信件内容存在非法字符");
        }
        
        Mail mail = new Mail();
        mail.setSourceId(src.getId());
        mail.setSourceName(src.getPlayerName());
        mail.setDestId(destId);
        mail.setDestName(destName);
        mail.setTitle(title);
        if ("gmmail".equals(destName)) {
            StringBuilder sb = new StringBuilder();
            sb.append(content);
            sb.append("\n");
            sb.append(src.getMapId());
            sb.append(",(");
            sb.append(src.getX());
            sb.append(",");
            sb.append(src.getY());
            sb.append(")\n");
            sb.append(src.getModel());
            content = sb.toString();
        }
        mail.setContent(content);
        mail.setPrice(price);
        mail.setPostTime(new Date());
        
        try {
        	if(System.currentTimeMillis() - src.getLastMailTime() >= 30000){//发送时间小于30秒禁止发邮件
	            if (attachment != null && attachment.length > 0) {
	                Attachment att = getAttachmentFromClientBytes(src, attachment);
	                mail.setAttachment(att.toDbBytes());
	                if (mail.getPrice() > 0) {
	                	mail.setValidTime(new Date(System.currentTimeMillis() + att.getSendTime() + 2L * 24L * 3600L * 1000L));
	                } else {
	                	mail.setValidTime(new Date(System.currentTimeMillis() + att.getSendTime() + 30L * 24L * 3600L * 1000L));
	                }
	                Notify notify = new Notify(destId,src.getId());
	                timer.queue(notify, (int) att.getSendTime() / 1000);
	            } else {
	                mail.setValidTime(new Date(System.currentTimeMillis() + 30L * 24L * 3600L * 1000L));
	                WorldPlayer player = playerService.getWorldPlayer(destId);
	                if (player != null && player.online() && !player.inBlackList(src.getId())) {
	                    UWAPSegment seg = new UWAPSegment(ClientConstants.
	                            MAIL_NEW);
	                    connectService.writeTo(seg, destId);
	                }
	            }
	            dao.addMail(mail);
	            if (!"gmmail".equals(destName)) {
	            	src.setLastMailTime(System.currentTimeMillis());
	            }
	           
        	}else{
        		//chatService.sendPrivateMessage(-1, "系统", src.getId(),  "你发的信件速度太快了，请休息一下再发吧！");
        		//connectService.sendMessage(src.getId(), "你发的信件速度太快了，请休息一下再发吧！");
        		 throw new MailException("你发的信件速度太快了，请休息一下再发吧！");
        	}
            // 如果是求助信息，立刻转发到在线GM手中
        
        	
        	
            if ("gmmail".equals(destName)) {
            	if(System.currentTimeMillis() - src.getLastGmMailTime() <= 300000){//gm邮件条件限制
            		if(src.getGmMailCount() < 2){
            			src.setGmMailCount((byte) (src.getGmMailCount()+1));
            		}else{
            			//chatService.sendPrivateMessage(-1, "系统", src.getId(),  "你发给gm的信件速度太快了，请休息一下再发吧！");
            			//connectService.sendMessage(src.getId(), "你发给gm的信件速度太快了，请休息一下再发吧！");
            			 throw new MailException("你发给gm的信件速度太快了，请休息一下再发吧！");
            		}
            	}else{
            		src.setGmMailCount((byte) 0);
            		src.setLastGmMailTime(System.currentTimeMillis());
            	}
            	adminService.broadcastSosMessage(mail);
            }
            return mail;
        } catch (MailException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error(ex, ex);
            throw new MailException("发信错误");
        }
    }

    public Attachment getAttachmentFromClientBytes(WorldPlayer player,
            byte[] bytes) throws MailException, IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bis);
        byte type = dis.readByte();
        if (type == 1) { //money
            int money = dis.readInt();
            if (money < 0) {
                Utils.log(log, player.getId(), -1, "企图刷钱");
                throw new MailException("钱不能为负");
            }
            int oldMoney = player.getMoeny();
            if (oldMoney >= money) {
                player.setMoeny(oldMoney - money);
                return new MoneyAttachment(money);
            } else
                throw new MailException("钱不够");
        } else if (type == 2) { //basicItem
            int id = dis.readInt();
            byte count = dis.readByte();
            if (count < 0)
                throw new MailException("物品数量错误");
            IItem item = Items.getTemplate(id).newInstance();
            if (item.isBinded())
                throw new MailException("该物品不支持此操作");
            item = player.completeRemoveItem(item, count, null);
            if (item != null) {
                return new ItemAttachment(item, count);
            } else {
                throw new MailException("物品不够");
            }
        } else if (type == 3) { //taskItem
            throw new MailException("物品已经被绑定");
        } else if (type == 4) { //extendedItem
            int id = dis.readInt();
            byte count = dis.readByte();
            if (count < 0)
                throw new MailException("物品数量错误");
            IItem item = Items.getTemplate(id).newInstance();
            if (item.isBinded())
                throw new MailException("物品已经被绑定");
            //20091201 add
            if (player.getLevel() < 30){
            	if (item.getItemId() == 211002){
            		throw new MailException("30级以下的玩家不能发送这么昂贵的物品哦~");
            	}
            }
            item = player.completeRemoveItem(item, count, null);
            if (item != null) {
                return new ItemAttachment(item, count);
            } else {
                throw new MailException("物品不够");
            }
        } else if (type == 5) { //equip
            int itemId = dis.readInt();
            int id = dis.readInt();
            IEquipment equ = player.getEquipment(itemId, id);
            if (equ == null)
                throw new MailException("找不到此物品");
            if (equ.isBinded())
                throw new MailException("物品已经被绑定");
            IItem item = player.completeRemoveItem(equ, id, null);
            if (item != null) {
                return new ItemAttachment(item, 1);
            } else {
                throw new MailException("物品不够");
            }
        }
//        }else if(type==6){//pet
//            int id = dis.readInt();
//            Pet pet = player.getPet(id);
//            if(pet==null)
//                throw new MailException("找不到此宠物");
//            if(pet==player.getPet())
//                throw new MailException("不能邮寄装备上的宠物");
//            if(pet.getFavor()<30)
//                throw new MailException("宠物好感度太低");
//            player.removePet(pet);
//            pet.setFavor(30);
//            return ItemUtils.pet2dbAttachment(pet);
//        }

        return null;
    }

    public Changed getMailAttachment(WorldPlayer player, int id,Changed changed, boolean read) throws
            MailException {
        Mail mail = null;
        try {
            mail = dao.getMail(id);
        } catch (DataAccessException ex) {
            throw new MailException("提取附件出错");
        }
        if (mail == null)
            throw new MailException("信件不存在");
        if (mail.getDestId() != player.getId()) {
            log.error("Get Mail Attachment Error");
            throw new MailException("提取附件出错");
        } else {
            byte[] item = mail.getAttachment();
            if (item == null || item.length == 0) {
                throw new MailException("没有附件");
            } else {
            	int needMoney = mail.getPrice();
            	int money = player.getMoeny();
            	final int iCardID = 200989;
            	if(needMoney < 0 && mail.getSourceId() > 0){
            		if(!player.hasItem(iCardID, 0 - needMoney)){
            			throw new MailException("没有足够I币卡");
            		}
            	}else{
	                if (money < mail.getPrice()) {
	                    throw new MailException("没有足够金钱");
	                }
            	}
                try {
                    Utils.log(log, player.getId(),
                              ClientConstants.MAIL_GET_ATTACHMENT,
                              "Attachment[" + Utils.getHexdump(item) +
                              "]Money[" + player.getMoeny() + "]");
                    Attachment attachment = ItemUtils.dbBytes2Attachment(
                            item, player.getClientDataVersion());
//                        Changed changed = new Changed();
                    if (attachment instanceof MoneyAttachment) {
                        MoneyAttachment matt = (MoneyAttachment) attachment;
                        if (matt.getCount() > 0) {
                        	if(player.getMoeny() + matt.getCount() < 0){
                        		throw new MailException("提取附件时获取的J币超过了最大值，请消费后再提取吧");
                        	}
                            player.setMoeny(player.getMoeny() +
                                            matt.getCount());
                            changed.addProperty(Changed.MONEY, matt.getCount());
                        }
                    } else {
                        ItemAttachment att = (ItemAttachment) attachment;
                        IItem ret = player.completeAddItem(att.getItem(), att.count(), changed, player.getClientDataVersion());
                        if (ret == null)
                            throw new MailException("您背包空余的位置不够");
                    }
                    mail.setAttachment(new byte[0]);
                    if(read){
                    	mail.setReaded(true);
                	}
                    dao.makePersistent(mail);
                    if (mail.getPrice() > 0) {
                        player.setMoeny(player.getMoeny() - mail.getPrice());
                        changed.addProperty(Changed.MONEY, -mail.getPrice());
                        //扣除税率
                        int percentmoney = mail.getPrice();		//真实的价格
                        int temp = percentmoney;
                        int tax = 0;
                        WorldPlayer mailplayer = playerService.getWorldPlayerAndCatch(mail.getSourceId());
                       	try {
//	                       		WorldPlayer mailplayer = playerService.loadWorldPlayer(mail.getSourceId());
                       		tax = campMainService.getTax(percentmoney, mailplayer.getCamp());
                       		campMainService.addCampMoney(mailplayer.getCamp(), tax);
                       		percentmoney -= tax;
               			} catch (Exception e) {
               				log.error("PlayerID["+mail.getSourceId() + "]load unRegistry error");
               			} finally{
               				playerService.releasePlayer(mailplayer);
               			}
               			String taxprice = Integer.toString(tax);
                        byte[] att = ItemUtils.money2dbAttachment(percentmoney);
//                            sendMail(mail.getSourceId(), mail.getSourceName(), -1, "系统", "回复:" + mail.getTitle(), "",
//                                     att, 0, false);
                        sendMail(mail.getSourceId(), mail.getSourceName(), -1, "系统", "回复:" + mail.getTitle(), "“" + mail.getDestName() + "”提取了您的附件，扣税:"+ taxprice +"J,请查收。",
                                att, 0, false);
                    }else if(mail.getPrice() < 0 && mail.getSourceId() > 0){
                    	IItem removeItem = player.completeRemoveItem(iCardID, 0 - mail.getPrice(), changed);
                    	if(removeItem == null){
                    		log.error("PlayerID[" + mail.getSourceId() + "] removeItem error [" + mail.getPrice() + "]");
                    	}
                    	byte[] att = ItemUtils.item2dbAttachment(removeItem, 0 - mail.getPrice());
                    	sendMail(mail.getSourceId(), mail.getSourceName(), -1, "系统", "回复:" + mail.getTitle(), "“" + mail.getDestName() + "”提取了您的附件，请查收。", att, 0, false);
                    }
                    Utils.log(log, player.getId(),
                              ClientConstants.MAIL_GET_ATTACHMENT,
                              "Attachment[" + Utils.getHexdump(item) +
                              "]changed[" +
                              Utils.getHexdump(changed.toBytes()) +
                              "]Money[" + player.getMoeny() + "]SourceId[" + mail.getSourceId() + "]");
                    return changed;
                } catch (DataAccessException ex1) {
                    throw new MailException("提取附件出错");
                }
            }
        }
    }

    public void run() {
        while (true) {
        	count++;
        	try {
            	Thread.sleep(5 * 60 * 1000L);
            } catch (InterruptedException ex) {
            }
            try {
//            batchAddMail();
            	if(count % 12 == 0){		//1hour扫描一次
            		count = 0;
            		checkObsoleteMail();
            	}
//                checkQuota();
                checkUnvalidMail();                //5mins扫描一次
            } catch (Throwable e) {
                log.error(e, e);
            }
        }
    }

    private void batchAddMail() throws DataAccessException {
        for (int i = 0; i < 110; i++) {
            Mail mail = new Mail();
            mail.setSourceId( -1);
            mail.setSourceName("System");
            mail.setDestId(5);
            mail.setDestName("haha");
            mail.setTitle("cc");
            mail.setContent("c");
            mail.setAttachment(new byte[0]);
            mail.setPrice(0);
            mail.setReaded(false);
            mail.setPostTime(new Date());
            dao.addMail(mail);
        }
    }

    private void checkQuota() {
        try {
            List l = dao.getMailCountAndDestId();
            for (int i = 0; i < l.size(); i++) {
                Object[] os = (Object[]) l.get(i);
                int count = ((Long) os[0]).intValue();
                int id = ((Integer) os[1]).intValue();
                List ll = dao.getMail(id, count - 100);
                for (int j = 0; j < ll.size(); j++) {
                    Mail m = (Mail) ll.get(j);
                    deleteMail(m);
                }
            }
        } catch (DataAccessException ex) {
            log.error(ex, ex);
        }
    }

    private void checkObsoleteMail() {
        try {
            List l = dao.getObsoleteFeeMail();
            for (int i = 0; i < l.size(); i++) {
                deleteMail((Mail) l.get(i));
            }
        } catch (DataAccessException ex) {
        }
    }
    
    private void checkUnvalidMail() {		//added by JeremyZhong
    	try{
            List l = dao.getUnvalidMail();
            for (int i = 0; i < l.size(); i++) {
                try{
                    deleteUnvalidMail((Mail) l.get(i));
                }catch(Exception e){
                    log.error(e, e);
                }
            }
    	}catch(DataAccessException ex){
    	}
    }

    /**
     * 如果不是系统邮件而且是付费邮件则返回,不然直接删除
     * @param mail Mail
     */
    public void deleteMail(Mail mail) throws DataAccessException {
        if (mail.getSourceId() > 0 && mail.getAttachment() != null &&
            mail.getAttachment().length > 0) {
            Mail newMail = new Mail();
            newMail.setSourceId(-1);
            newMail.setSourceName("系统");
            newMail.setDestId(mail.getSourceId());
            newMail.setDestName(mail.getSourceName());
            newMail.setTitle("过期退回:" + mail.getTitle());
            newMail.setAttachment(mail.getAttachment());
            newMail.setPrice(0);
//            newMail.setContent("超时未处理");
            newMail.setContent("“" + mail.getDestName() + "”拒收了您的邮件，请查收。");
            newMail.setReaded(false);
            newMail.setPostTime(new Date());
            newMail.setValidTime(new Date(System.currentTimeMillis() + 30L * 24L * 3600L * 1000L));
            addMail(newMail);
        }
        dao.deleteMail(mail);
    }
    
    public void deleteUnvalidMail(Mail mail) throws DataAccessException {
    	byte[] tmpAttachment = mail.getAttachment();
    	if(mail.getSourceId() < 0 || tmpAttachment == null || tmpAttachment.length == 0){		//系统邮件或没有附件直接删除
    		WorldPlayer player = playerService.getWorldPlayer(mail.getDestId());
    		if(player != null){
    			chatService.sendPrivateMessage(-1, "系统", player.getId(), "您有邮件超过30天被删除");
    		}
    		dao.deleteMail(mail);
    	} else if (tmpAttachment != null && tmpAttachment.length > 0){		//有附件的，返给发件人
    		 String destName = mail.getDestName();
    		 mail.setDestId(mail.getSourceId()); 
    		 mail.setDestName(mail.getSourceName());
    		 mail.setSourceId(-1);
		     mail.setSourceName("系统");
		     mail.setTitle("过期退回:" + mail.getTitle());
		     mail.setAttachment(mail.getAttachment());
		     mail.setPrice(0);
		     mail.setContent("您的邮件因收件人“" + destName + "”超过30天未查收而被退回, 系统到过期时间会自动删除请尽快提取");
		     mail.setReaded(false);
		     mail.setPostTime(new Date());
	         Date date = new Date(System.currentTimeMillis() +
                        30L * 86400L * 1000L);
//	         Date date = new Date(System.currentTimeMillis() +
//                      20 * 60L * 1000L);		//20分
	         mail.setValidTime(date);
             dao.makePersistent(mail);
    	}
    }

    public void deleteMail(int destId,Date time) throws DataAccessException{
        dao.deleteMail(destId,time);
    }

    public MailList getMailList(int playerId, int pageSize, int pageNo) throws MailException {
        try {
            int count = dao.getMailCount(playerId);
            if (pageSize * pageNo >= count) {
                throw new MailException("没有可显示的邮件");
            } else {
                List l = dao.getMailList(playerId,
                                         pageSize * pageNo,
                                         pageSize);
                MailList ret = new MailList(count, l);
                return ret;
            }
        } catch (Exception e) {
            throw new MailException(e.getMessage());
        }
    }
    public MailList getMailList(int playerId, int pageSize, int pageNo, int minId) throws MailException {
        try {
            int count = dao.getMailCount(playerId, minId);
            if (pageSize * pageNo >= count) {
                throw new MailException("没有可显示的邮件");
            } else {
                List l = dao.getMailList(playerId, pageSize * pageNo, pageSize, minId);
                MailList ret = new MailList(count, l);
                return ret;
            }
        } catch (Exception e) {
            throw new MailException(e.getMessage());
        }
    }
    
    public MailList getMailList(int playerId, int pageSize, int pageNo, int minId, Date startTime, Date endTime) throws MailException {
        try {
            int count = dao.getMailCount(playerId, minId);
            if (pageSize * pageNo >= count) {
                throw new MailException("没有可显示的邮件");
            } else {
                List l = dao.getMailList(playerId, pageSize * pageNo, pageSize, minId, startTime, endTime);
                MailList ret = new MailList(count, l);
                return ret;
            }
        } catch (Exception e) {
            throw new MailException(e.getMessage());
        }
    }

    public void start() {
        new Thread(this).start();
        timer.start();
    }

    public void addForbiden(int id, int second) {
        if (second == 0) {
            forbids.remove(new Integer(id));
        } else {
            MailForbiden forbiden = new MailForbiden(id, System.currentTimeMillis() + second * 1000L);
            forbids.put(new Integer(id), forbiden);
        }
    }

    public boolean isForbiden(int id) {
        MailForbiden f = (MailForbiden) forbids.get(new Integer(id));
        if (f == null)
            return false;
        return System.currentTimeMillis() < f.validTime;
    }

    public int getUnReadedMailCount(int playerId,int[] blackList){
        try {
            return dao.getUnReadedMailCount(playerId, blackList);
        } catch (Exception ex) {
            return 0;
        }
    }

    class Notify implements IRefresh {
        private int id;
        private int srcId;

        public Notify(int id,int srcId) {
            this.id = id;
            this.srcId = srcId;
        }

        public void refresh() {
            WorldPlayer player = playerService.getWorldPlayer(id);
            if (player != null && player.online() && !player.inBlackList(srcId)) {
                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                  MAIL_NEW);
                connectService.writeTo(seg, id);
            }
        }
    }
}


class MailForbiden {
    int id;
    long validTime;
    public MailForbiden(int id, long validTime) {
        this.id = id;
        this.validTime = validTime;
    }
}
