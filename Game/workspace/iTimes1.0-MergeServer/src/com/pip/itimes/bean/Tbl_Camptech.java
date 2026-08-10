package com.pip.itimes.bean;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_CamptechDao;

public class Tbl_Camptech extends BaseTable{
    /*
    CREATE TABLE `tbl_camptech` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `kingid` int(11) NOT NULL DEFAULT '0',
      `kingname` varchar(255) NOT NULL DEFAULT '',
      `camp` tinyint(4) NOT NULL DEFAULT '1',
      `credit` int(11) NOT NULL DEFAULT '0',
      `level` int(11) NOT NULL DEFAULT '0',
      `moeny` bigint(20) NOT NULL DEFAULT '0',
      `percent` int(11) NOT NULL DEFAULT '0',
      `campmoeny` int(11) NOT NULL DEFAULT '0',
      `integral` int(11) NOT NULL DEFAULT '0',
      `technology` blob,
      `valid` tinyint(4) NOT NULL DEFAULT '1',
      PRIMARY KEY (`id`),
      KEY `index_kingid` (`kingid`)
    ) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
    */
    private int id;
    private int kingid;
    private String kingname;
    private int camp;
    private int credit;
    private int level;
    private long moeny;
    private int percent;
    private int campmoeny;
    private int integral;
    private byte[] technology;
    private int valid;
    
    private int oldId;

    @Override
    public String getColumnNames(){
        return Tbl_CamptechDao.SQL_PARA;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append(Tools.toSqlString(id));
        sb.append(", ");
        sb.append(Tools.toSqlString(kingid));
        sb.append(", ");
        sb.append(Tools.toSqlString(kingname));
        sb.append(", ");
        sb.append(Tools.toSqlString(camp));
        sb.append(", ");
        sb.append(Tools.toSqlString(credit));
        sb.append(", ");
        sb.append(Tools.toSqlString(level));
        sb.append(", ");
        sb.append(Tools.toSqlString(moeny));
        sb.append(", ");
        sb.append(Tools.toSqlString(percent));
        sb.append(", ");
        sb.append(Tools.toSqlString(campmoeny));
        sb.append(", ");
        sb.append(Tools.toSqlString(integral));
        sb.append(", ");
        sb.append(Tools.toSqlString(technology));
        sb.append(", ");
        sb.append(Tools.toSqlString(valid));

        return sb.toString();
    }

    @Override
    public void process(MergeData mergeData, ServerConfig serverConfig){
        //officer的特殊处理
        oldId = mergeData.procCamptechData(this);
    
        if(oldId >= 0){
            status = STATUS_DELETE_OLD;
        }
    }
    
    @Override
    public String toSqlString(){
        if(status == STATUS_DELETE_OLD){
            StringBuffer sb = new StringBuffer();

            sb.append("DELETE FROM tbl_camptech WHERE id = ");
            sb.append(Tools.toSqlString(oldId));
            sb.append(";\n");

            status = STATUS_INSERT;

            return sb.toString() + super.toSqlString();
        }else{
            return super.toSqlString();
        }
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getKingid(){
        return kingid;
    }

    public void setKingid(int kingid){
        this.kingid = kingid;
    }

    public String getKingname(){
        return kingname;
    }

    public void setKingname(String kingname){
        this.kingname = kingname;
    }

    public int getCamp(){
        return camp;
    }

    public void setCamp(int camp){
        this.camp = camp;
    }

    public int getCredit(){
        return credit;
    }

    public void setCredit(int credit){
        this.credit = credit;
    }

    public int getLevel(){
        return level;
    }

    public void setLevel(int level){
        this.level = level;
    }

    public long getMoeny(){
        return moeny;
    }

    public void setMoeny(long moeny){
        this.moeny = moeny;
    }

    public int getPercent(){
        return percent;
    }

    public void setPercent(int percent){
        this.percent = percent;
    }

    public int getCampmoeny(){
        return campmoeny;
    }

    public void setCampmoeny(int campmoeny){
        this.campmoeny = campmoeny;
    }

    public int getIntegral(){
        return integral;
    }

    public void setIntegral(int integral){
        this.integral = integral;
    }

    public byte[] getTechnology(){
        return technology;
    }

    public void setTechnology(byte[] technology){
        this.technology = technology;
    }

    public int getValid(){
        return valid;
    }

    public void setValid(int valid){
        this.valid = valid;
    }
}
