package com.pip.itimes.bean;

import java.util.Date;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_IMoneyCardDao;

public class Tbl_IMoneyCard extends BaseTable{

	/*
	CREATE TABLE `tbl_imoneycard` (
	  `id` int(11) NOT NULL AUTO_INCREMENT,
	  `createaccountid` int(11) NOT NULL DEFAULT '-1',
	  `createplayerid` int(11) NOT NULL DEFAULT '-1',
	  `createtime` datetime DEFAULT NULL,
	  `useaccountid` int(11) NOT NULL DEFAULT '-1',
	  `useplayerid` int(11) NOT NULL DEFAULT '-1',
	  `usetime` datetime DEFAULT NULL,
	  `cardno` varchar(255) NOT NULL,
	  `password` varchar(255) NOT NULL,
	  `amount` int(11) NOT NULL,
	  `status` tinyint(4) NOT NULL DEFAULT '0',
	  PRIMARY KEY (`id`),
	  KEY `create_imoney_card` (`createaccountid`,`createplayerid`),
	  KEY `use_imoney_card` (`useaccountid`,`useplayerid`),
	  KEY `imoney_card_no` (`cardno`),
	  KEY `imoney_card_status` (`status`)
	) ENGINE=MyISAM AUTO_INCREMENT=5149 DEFAULT CHARSET=utf8;
	 */
	
    private int id;
    private int createaccountid;
    private int createplayerid;
    private Date createtime;
    private int useaccountid;
    private int useplayerid;
    private Date usetime;
    private String cardno;
    private String password;
    private int amount;
    private int status;

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getCreateaccountid(){
        return createaccountid;
    }

    public void setCreateaccountid(int createaccountid){
        this.createaccountid = createaccountid;
    }

    public int getCreateplayerid(){
        return createplayerid;
    }

    public void setCreateplayerid(int createplayerid){
        this.createplayerid = createplayerid;
    }

    public Date getCreatetime(){
        return createtime;
    }

    public void setCreatetime(Date createtime){
        this.createtime = createtime;
    }

    public int getUseaccountid(){
        return useaccountid;
    }

    public void setUseaccountid(int useaccountid){
        this.useaccountid = useaccountid;
    }

    public int getUseplayerid(){
        return useplayerid;
    }

    public void setUseplayerid(int useplayerid){
        this.useplayerid = useplayerid;
    }

    public Date getUsetime(){
        return usetime;
    }

    public void setUsetime(Date usetime){
        this.usetime = usetime;
    }

    public String getCardno(){
        return cardno;
    }

    public void setCardno(String cardno){
        this.cardno = cardno;
    }

    public String getPassword(){
        return password;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public int getAmount(){
        return amount;
    }

    public void setAmount(int amount){
        this.amount = amount;
    }

    public int getStatus(){
        return status;
    }

    public void setStatus(int status){
        this.status = status;
    }
	
	
	@Override
	public String getColumnNames() {
		return Tbl_IMoneyCardDao.SQL_PARA;
	}

	@Override
	public void process(MergeData mergeData, ServerConfig serverConfig) {
		//处理id
        id = mergeData.procIMoneyCardId(id);
        //处理使用的PlayerID
        useplayerid = mergeData.procPlayerId(useplayerid);
	}
	
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();

        sb.append(Tools.toSqlString(id));
        sb.append(", ");
        sb.append(Tools.toSqlString(createaccountid));
        sb.append(", ");
        sb.append(Tools.toSqlString(createplayerid));
        sb.append(", ");
        sb.append(Tools.toSqlString(createtime));
        sb.append(", ");
        sb.append(Tools.toSqlString(useaccountid));
        sb.append(", ");
        sb.append(Tools.toSqlString(useplayerid));
        sb.append(", ");
        sb.append(Tools.toSqlString(usetime));
        sb.append(", ");
        sb.append(Tools.toSqlString(cardno));
        sb.append(", ");
        sb.append(Tools.toSqlString(password));
        sb.append(", ");
        sb.append(Tools.toSqlString(amount));
        sb.append(", ");
        sb.append(Tools.toSqlString(status));

        return sb.toString();
	}
}
