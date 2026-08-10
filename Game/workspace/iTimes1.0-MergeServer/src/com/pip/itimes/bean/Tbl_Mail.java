package com.pip.itimes.bean;

import java.util.Date;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_MailDao;
import com.pip.itimes.server.stage.Attachment;
import com.pip.itimes.server.stage.IEquipment;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.ItemAttachment;
import com.pip.itimes.server.stage.ItemUtils;

public class Tbl_Mail extends BaseTable{
    /*
    CREATE TABLE `tbl_mail` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `sourceid` int(11) NOT NULL DEFAULT '0',
      `sourcename` varchar(255) NOT NULL DEFAULT '',
      `destid` int(11) NOT NULL DEFAULT '0',
      `destname` varchar(255) NOT NULL DEFAULT '',
      `title` varchar(255) NOT NULL DEFAULT '',
      `content` mediumtext,
      `attachment` blob,
      `price` int(11) NOT NULL DEFAULT '0',
      `posttime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      `readed` tinyint(4) NOT NULL DEFAULT '0',
      `validtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      PRIMARY KEY (`id`),
      KEY `index_mail_user` (`destid`),
      KEY `index_mail_posttime` (`posttime`),
      KEY `index_validtime` (`validtime`)
    ) ENGINE=MyISAM AUTO_INCREMENT=742586 DEFAULT CHARSET=utf8 DELAY_KEY_WRITE=1;
    */
    private int id;
    private int sourceid;
    private String sourcename;
    private int destid;
    private String destname;
    private String title;
    private String content;
    private byte[] attachment;
    private int price;
    private Date posttime;
    private int readed;
    private Date validtime;

    @Override
    public String getColumnNames(){
        return Tbl_MailDao.SQL_PARA;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append(Tools.toSqlString(id));
        sb.append(", ");
        sb.append(Tools.toSqlString(sourceid));
        sb.append(", ");
        sb.append(Tools.toSqlString(sourcename));
        sb.append(", ");
        sb.append(Tools.toSqlString(destid));
        sb.append(", ");
        sb.append(Tools.toSqlString(destname));
        sb.append(", ");
        sb.append(Tools.toSqlString(title));
        sb.append(", ");
        sb.append(Tools.toSqlString(content));
        sb.append(", ");
        sb.append(Tools.toSqlString(attachment));
        sb.append(", ");
        sb.append(Tools.toSqlString(price));
        sb.append(", ");
        sb.append(Tools.toSqlString(posttime));
        sb.append(", ");
        sb.append(Tools.toSqlString(readed));
        sb.append(", ");
        sb.append(Tools.toSqlString(validtime));

        return sb.toString();
    }

    @Override
    public void process(MergeData mergeData, ServerConfig serverConfig){
    	//处理id
    	id = mergeData.procMailId(id);
    	
    	//处理sourceid和sourcename
    	if(sourceid >= 0){
    		sourceid = mergeData.procPlayerId(sourceid);
    		sourcename = mergeData.procPlayerName(sourcename);
    	}
    	
    	//处理destid和destname
    	if(destid >= 0){
    		destid = mergeData.procPlayerId(destid);
    		destname = mergeData.procPlayerName(destname);
    	}
    	
    	//处理item中的instanceId
        if(attachment != null){
            Attachment att = ItemUtils.dbBytes2Attachment(attachment, 0);
            
            if(att instanceof ItemAttachment){
                IItem item = ((ItemAttachment)att).getItem();

                if(item instanceof IEquipment){
                    int instanceId = ((IEquipment)item).getId();
                    instanceId = mergeData.procEquipmentId(instanceId);
                    ((IEquipment)item).setId(instanceId);
                }
                
                attachment = att.toDbBytes();
            }
        }
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getSourceid(){
        return sourceid;
    }

    public void setSourceid(int sourceid){
        this.sourceid = sourceid;
    }

    public String getSourcename(){
        return sourcename;
    }

    public void setSourcename(String sourcename){
        this.sourcename = sourcename;
    }

    public int getDestid(){
        return destid;
    }

    public void setDestid(int destid){
        this.destid = destid;
    }

    public String getDestname(){
        return destname;
    }

    public void setDestname(String destname){
        this.destname = destname;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getContent(){
        return content;
    }

    public void setContent(String content){
        this.content = content;
    }

    public byte[] getAttachment(){
        return attachment;
    }

    public void setAttachment(byte[] attachment){
        this.attachment = attachment;
    }

    public int getPrice(){
        return price;
    }

    public void setPrice(int price){
        this.price = price;
    }

    public Date getPosttime(){
        return posttime;
    }

    public void setPosttime(Date posttime){
        this.posttime = posttime;
    }

    public int getReaded(){
        return readed;
    }

    public void setReaded(int readed){
        this.readed = readed;
    }

    public Date getValidtime(){
        return validtime;
    }

    public void setValidtime(Date validtime){
        this.validtime = validtime;
    }
}
