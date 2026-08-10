package com.pip.itimes.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_CampDao;
import com.pip.itimes.dao.Tbl_UserdataDao;
import com.pip.itimes.server.camp.CampConfig;
import com.pip.itimes.server.camp.CampSkill;
import com.pip.itimes.server.camp.CampSkillData;

public class Tbl_Camp extends BaseTable{
	/*
	 * CREATE TABLE `tbl_camp` (
		  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '序号',
		  `camp` tinyint(4) NOT NULL DEFAULT '0' COMMENT '阵营',
		  `kingid` int(11) NOT NULL DEFAULT '0' COMMENT '领袖角色id',
		  `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '创建时间',
		  `lasttime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '最后操作时间',
		  `money` bigint(20) NOT NULL DEFAULT '0' COMMENT '阵营资金',
		  `taxrate` int(11) NOT NULL DEFAULT '0' COMMENT '当前税率',
		  `skills` blob NOT NULL COMMENT '阵营科技',
		  `slogan` text NOT NULL COMMENT '阵营公告',
		  `pool` text NOT NULL COMMENT '参数池',
		  `valid` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否有效',
		  PRIMARY KEY (`id`)
		) ENGINE=MyISAM AUTO_INCREMENT=148 DEFAULT CHARSET=utf8 COMMENT='阵营表';
	 */
	
    /**
     * 序号
     */
    private int id;

    /**
     * 阵营
     */
    private int camp;

    /**
     * 领袖id
     */
    private int kingid;

    /**
     * 创建时间
     */
    private Date createtime;
    
    /**
     * 最后操作时间
     */
    private Date lasttime;

    /**
     * 阵营资金
     */
    private long money;

    /**
     * 当前税率
     */
    private int taxrate;

    /**
     * 阵营科技
     */
    private byte[] skills;

    /**
     * 阵营公告
     */
    private String slogan;

    /**
     * 参数池
     */
    private String pool;

    /**
     * 是否有效
     */
    private int valid;

    @Override
    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getCamp(){
        return camp;
    }

    public void setCamp(int camp){
        this.camp = camp;
    }

    public int getKingid(){
        return kingid;
    }

    public void setKingid(int kingid){
        this.kingid = kingid;
    }

    public Date getCreatetime(){
        return createtime;
    }

    public void setCreatetime(Date createtime){
        this.createtime = createtime;
    }

    public Date getLasttime(){
        return lasttime;
    }

    public void setLasttime(Date lasttime){
        this.lasttime = lasttime;
    }

    public long getMoney(){
        return money;
    }

    public void setMoney(long money){
        this.money = money;
    }

    public int getTaxrate(){
        return taxrate;
    }

    public void setTaxrate(int taxrate){
        this.taxrate = taxrate;
    }

    public byte[] getSkills(){
        return skills;
    }

    public void setSkills(byte[] skills){
        this.skills = skills;
    }

    public String getSlogan(){
        return slogan;
    }

    public void setSlogan(String slogan){
        this.slogan = slogan;
    }

    public String getPool(){
        return pool;
    }

    public void setPool(String pool){
        this.pool = pool;
    }

    public int getValid(){
        return valid;
    }

    public void setValid(int valid){
        this.valid = valid;
    }
    
	@Override
	public String getColumnNames() {
		return Tbl_CampDao.SQL_PARA;
	}
	
    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append("camp=");
        sb.append(Tools.toSqlString(camp));
        sb.append(", ");
        sb.append("kingid=");
        sb.append(Tools.toSqlString(kingid));
        sb.append(", ");
        sb.append("createtime=");
        sb.append(Tools.toSqlString(createtime));
        sb.append(", ");
        sb.append("lasttime=");
        sb.append(Tools.toSqlString(lasttime));
        sb.append(", ");
        sb.append("money=");
        sb.append(Tools.toSqlString(money));
        sb.append(", ");
        sb.append("taxrate=");
        sb.append(Tools.toSqlString(taxrate));
        sb.append(", ");
        sb.append("skills=");
        sb.append(Tools.toSqlString(skills));
        sb.append(", ");
        sb.append("slogan=");
        sb.append(Tools.toSqlString(slogan));
        sb.append(", ");
        sb.append("pool=");
        sb.append(Tools.toSqlString(pool));
        sb.append(", ");
        sb.append("valid=");
        sb.append(Tools.toSqlString(valid));

        return sb.toString();
    }

	@Override
	public void process(MergeData mergeData, ServerConfig serverConfig) {
        Tbl_CampDao campDao = new Tbl_CampDao(serverConfig);
        List<Tbl_Camp> campList = campDao.getList(0, Integer.MAX_VALUE);
        for(Tbl_Camp camp : campList){
        	if(camp.getValid() != 0){
        		Tbl_Camp tmpCamp = mergeData.campTable.get(camp.getCamp());
//        		//阵营盟主取角色荣誉高的
//        		Tbl_UserdataDao userdataDao = new Tbl_UserdataDao(serverConfig);
//        		Tbl_Userdata campKing = userdataDao.getPlayer(camp.getKingid());
//        		Tbl_UserdataDao tmpUserdataDao = new Tbl_UserdataDao(mergeData.mainServer);
//        		Tbl_Userdata tmpCampKing = tmpUserdataDao.getPlayer(tmpCamp.getKingid());
//        		if(tmpCampKing.getCredit() < campKing.getCredit()){
//        			tmpCamp.setKingid(mergeData.procPlayerId(campKing.getId()));
//        		}
        		
        		//同阵营中 阵营资金合并
        		tmpCamp.setMoney(tmpCamp.getMoney() + camp.getMoney());
        		//税率设置为默认 10%
        		tmpCamp.setTaxrate(CampConfig.taxDefault);
//        		tmpCamp.setTaxrate(10);
        		//阵营科技 重复取等级高的
        		try{
        			HashMap<Integer, CampSkillData> skills = new HashMap<Integer, CampSkillData>();
	                List<CampSkillData> tmpList = CampSkill.fromDbBytes(tmpCamp.getSkills());
	                for(CampSkillData tmp : tmpList){
	                    skills.put(tmp.getEffect(), tmp);
	                }
	                List<CampSkillData> list = CampSkill.fromDbBytes(camp.getSkills());
	                for(CampSkillData tmp : list){
	                	//重复取等级高的
	                	if(skills.containsKey(tmp.getEffect())){
	                		CampSkillData tmpSkill = skills.get(tmp.getEffect());
	                		if(tmpSkill.getLevel() < tmp.getLevel()){
	                			skills.put(tmp.getEffect(), tmp);
	                		}
	                	}else{
	                		skills.put(tmp.getEffect(), tmp);
	                	}
	                }
	                //将合并后的阵营科技保存起来
	                list = new ArrayList<CampSkillData>(skills.values());
	                tmpCamp.setSkills(CampSkill.toDbBytes(list));
        		}catch(Exception e){
        		}
        	}
        }
	}
}
