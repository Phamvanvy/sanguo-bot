package com.pip.itimes.bean;

import java.util.Date;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_CampqualificationDao;

public class Tbl_Campqualification extends BaseTable{
	/*
	 * CREATE TABLE `tbl_campqualification` (
	  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '序号',
	  `playerid` int(11) NOT NULL COMMENT '角色id',
	  `createtime` datetime NOT NULL COMMENT '创建时间',
	  `lasttime` datetime NOT NULL COMMENT '最后操作时间',
	  `camp` tinyint(4) NOT NULL COMMENT '阵营',
	  `total` int(11) NOT NULL COMMENT '总计投入荣誉',
	  `added` int(11) NOT NULL COMMENT '追加的荣誉',
	  `addcount` int(11) NOT NULL COMMENT '追加次数',
	  `remain` int(11) NOT NULL COMMENT '角色剩余荣誉',
	  `level` tinyint(4) NOT NULL COMMENT '角色级别',
	  `valid` tinyint(4) NOT NULL COMMENT '是否有效',
	  PRIMARY KEY (`id`)
	) ENGINE=MyISAM AUTO_INCREMENT=309 DEFAULT CHARSET=utf8 COMMENT='竞选资格表';
	 */

	@Override
	public String getColumnNames() {
		return Tbl_CampqualificationDao.SQL_PARA;
	}

	@Override
	public void process(MergeData mergeData, ServerConfig serverConfig) {
		//处理id
        id = mergeData.procCampqualificatioId(id);
        //处理playerid
        playerid = mergeData.procPlayerId(playerid);
	}
	
	@Override
    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append(Tools.toSqlString(id));
        sb.append(", ");
        sb.append(Tools.toSqlString(playerid));
        sb.append(", ");
        sb.append(Tools.toSqlString(createtime));
        sb.append(", ");
        sb.append(Tools.toSqlString(lasttime));
        sb.append(", ");
        sb.append(Tools.toSqlString(camp));
        sb.append(", ");
        sb.append(Tools.toSqlString(total));
        sb.append(", ");
        sb.append(Tools.toSqlString(added));
        sb.append(", ");
        sb.append(Tools.toSqlString(addcount));
        sb.append(", ");
        sb.append(Tools.toSqlString(remain));
        sb.append(", ");
        sb.append(Tools.toSqlString(level));
        sb.append(", ");
        sb.append(Tools.toSqlString(valid));

        return sb.toString();
    }

	 /**
     * 序号
     */
    private int id;

    /**
     * 角色id
     */
    private int playerid;

    /**
     * 创建时间
     */
    private Date createtime;

    /**
     * 最后操作时间
     */
    private Date lasttime;

    /**
     * 阵营
     */
    private int camp;

    /**
     * 总投入荣誉
     */
    private int total;

    /**
     * 追加的荣誉
     */
    private int added;

    /**
     * 追加次数
     */
    private int addcount;

    /**
     * 角色剩余荣誉
     */
    private int remain;

    /**
     * 角色级别
     */
    private int level;
    
    /**
     * 是否有效
     */
    private int valid;

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getPlayerid(){
        return playerid;
    }

    public void setPlayerid(int playerid){
        this.playerid = playerid;
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

    public int getCamp(){
        return camp;
    }

    public void setCamp(int camp){
        this.camp = camp;
    }

    public int getTotal(){
        return total;
    }

    public void setTotal(int total){
        this.total = total;
    }

    public int getAdded(){
        return added;
    }

    public void setAdded(int added){
        this.added = added;
    }

    public int getAddcount(){
        return addcount;
    }

    public void setAddcount(int addcount){
        this.addcount = addcount;
    }

    public int getRemain(){
        return remain;
    }

    public void setRemain(int remain){
        this.remain = remain;
    }

    public int getLevel(){
        return level;
    }

    public void setLevel(int level){
        this.level = level;
    }
    public int getValid(){
        return valid;
    }

    public void setValid(int valid){
        this.valid = valid;
    }

}
