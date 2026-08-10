package com.pip.itimes.server.connect;

import java.util.List;

import com.pip.itimes.net.*;
import com.pip.itimes.server.bean.Mail;
import com.pip.itimes.server.dao.DataAccessException;
import com.pip.itimes.server.dao.MailDao;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.util.Utils;
import org.apache.log4j.Logger;

public class MailService {

    private static final Logger log = Logger.getLogger(MailService.class);

    private MailDao mailDao = null;

    public MailService(MailDao dao, PlayerService playerService) {
        this.mailDao = dao;
    }


    public void getMailList(ClientSession stub, UWAPData data) {
        try {
            short pageSize = data.readShort();
            int pageNo = data.readInt();
            int count = mailDao.getMailCount(stub.getPlayerId());
            if (pageSize * pageNo >= count) {
                UWAPSegment seg = new UWAPSegment(ClientConstants.ERROR,
                                                  data.getSerial());
                seg.write(data.getAppType());
                seg.writeString("没有可显示的邮件");
                stub.write(seg);
            } else {
                List l = mailDao.getMailList(stub.getPlayerId(),
                                             pageSize * pageNo,
                                             pageSize);
                int size = l.size();
                int pageCount = count / pageSize;
                if (count % pageSize != 0) pageCount++;
                UWAPSegment seg = new UWAPSegment(ClientConstants.MAIL_LIST,
                                                  data.getSerial());
                seg.writeShort(pageSize);
                seg.writeInt(pageNo);
                seg.writeInt(pageCount);
                seg.writeShort((short) size);
                for (int i = 0; i < size; i++) {
                    Mail mail = (Mail) l.get(i);
                    seg.writeInt(mail.getId());
                    seg.writeString(mail.getSourceName());
                    seg.writeString(mail.getTitle());
                    seg.writeString(Utils.getDateString(mail.getPostTime()));
                    byte[] att = mail.getAttachment();
                    if(att==null||att.length==0){
                        seg.writeBoolean(false);
                    }else{
                        seg.writeBoolean(true);
                    }
                    seg.writeBoolean(mail.getReaded());
                }
                stub.write(seg);
            }
        } catch (Exception e) {
            UWAPSegment seg = new UWAPSegment(ClientConstants.ERROR,
                                              data.getSerial());
            seg.write(data.getAppType());
            seg.writeString("您暂时没有邮件");
            stub.write(seg);
        }
    }

//    public void getMailAttachment(ClientSession stub, UWAPData data) {
//        try {
//            int mailId = data.readInt();
//            Mail mail = mailDao.getMail(mailId);
//            if (mail.getDestId() != stub.getPlayerId()) {
//                log.info("Get mail attachment error.");
//            } else {
//                byte[] item = mail.getAttachment();
//                if (item == null || item.length == 0) {
//                    UWAPSegment seg = new UWAPSegment(ClientConstants.ERROR,
//                            data.getSerial());
//                    seg.write(data.getAppType());
//                    seg.writeString("没有附件");
//                    stub.write(seg);
//                } else {
//                    PlayerData player = stub.getPlayerData();
//                    int money = player.getMoeny();
//                    if(money<mail.getPrice()){
//                        UWAPSegment seg = new UWAPSegment(ClientConstants.ERROR,data.getSerial());
//                        seg.write(data.getAppType());
//                        seg.writeString("没有足够的金钱");
//                        stub.write(seg);
//                        return;
//                    }else{
//                        player.setMoeny(money-mail.getPrice());
//                        mail.setAttachment(new byte[0]);
//                        mailDao.makePersistent(mail);
//                        Changed changed = new Changed();
//                        changed.add(item);
//                        player.addFallResult(changed);
//                        player.reset();
//                        playerService.savePlayer(player.getPlayer());
//                        UWAPSegment seg = new UWAPSegment(ClientConstants.
//                                MAIL_GET_ATTACHMENT_OK, data.getSerial());
//                        stub.write(seg);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            UWAPSegment seg = new UWAPSegment(ClientConstants.ERROR,
//                                              data.getSerial());
//            seg.writeString("收取附件错误");
//            stub.write(seg);
//        }
//    }

//    public void deletMail(ClientSession stub, UWAPData data) {
//        try {
//            int mailId = data.readInt();
//            mailDao.deleteMail(mailId);
//        } catch (Exception e) {
//        }
//    }

    public void getContent(ClientSession stub,UWAPData data){
        try {
            int mailId = data.readInt();
            Mail mail = mailDao.getMail(mailId);
            UWAPSegment seg = new UWAPSegment(ClientConstants.MAIL_CONTENT,data.getSerial());
            seg.writeInt(mailId);
            seg.writeInt(mail.getSourceId());
            seg.writeString(mail.getSourceName());
            seg.writeInt(mail.getDestId());
            seg.writeString(mail.getTitle());
            seg.writeString(mail.getContent());
            byte[] attachment = mail.getAttachment();
            if(attachment==null)
                attachment = new byte[0];
            seg.write(ItemUtils.dbAttachment2Client(attachment));
            seg.writeInt(mail.getPrice());
            seg.writeString(Utils.getDateString(mail.getPostTime()));
            stub.write(seg);
            if (!mail.getReaded()) {
                mail.setReaded(true);
                mailDao.makePersistent(mail);
                int unReaded = getUnReadedMailCount(stub.getPlayerId());
                if (unReaded == 0) {
                    UWAPSegment seg1 = new UWAPSegment(ClientConstants.MAIL_NEW);
                    seg1.write((byte) 0);
                    stub.write(seg1);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }



    public int getUnReadedMailCount(int playerId){
        try {

            return mailDao.getUnReadedMailCount(playerId);
        } catch (DataAccessException ex) {
            return 0;
        }
    }


//    public Attachment dbBytes2Attachment(byte[] bytes){
//        try {
//            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
//            DataInputStream dis = new DataInputStream(bis);
//            byte type = dis.readByte();
//            if (type == 8) { //todo magic number
//                return new MoneyAttachment(dis.readInt());
//            }
//            else if (type == IItem.TYPE_BASIC || type == IItem.TYPE_EXTENDED ||
//                       type == IItem.TYPE_TASK) {
//                int itemId = dis.readInt();
//                IItemTemplate template = Items.getTemplate(itemId);
//                return new ItemAttachment(template.newInstance(),dis.readShort());
//            }
//            else if(type == IItem.TYPE_EQU){
//                IEquipment equ = EquipmentHelper.createFromDbBytes(dis);
//                return new ItemAttachment(equ,1);
//            }
//        } catch (Exception ex) {
//        }
//        return null;
//    }

//    public byte[] dbAttachment2Client(byte[] bytes){
//        if(bytes.length==0)
//            return bytes;
//        Attachment attachment = dbBytes2Attachment(bytes);
//        Changed changed = new Changed();
//        if(attachment instanceof MoneyAttachment){
//            changed.addProperty(Changed.MONEY,((MoneyAttachment)attachment).getCount());
//        }else{
//            ItemAttachment ia = (ItemAttachment)attachment;
//            changed.addItem(ia.getItem(),ia.count());
//        }
//        Object[] os = changed.toClientBytes();
//        return (byte[])os[0];
//    }
}
