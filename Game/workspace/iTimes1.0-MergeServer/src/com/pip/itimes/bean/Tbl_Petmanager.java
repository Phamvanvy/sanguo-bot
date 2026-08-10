package com.pip.itimes.bean;

import java.util.Date;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_PetmanagerDao;
import com.pip.itimes.server.stage.Pet;

public class Tbl_Petmanager extends BaseTable{
    /*
    CREATE TABLE `tbl_petmanager` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `petid` int(11) NOT NULL DEFAULT '0',
      `playerid` int(11) NOT NULL DEFAULT '0',
      `pet` blob,
      `stone` int(11) NOT NULL DEFAULT '0',
      `eattime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      `information` bigint(20) NOT NULL DEFAULT '0',
      PRIMARY KEY (`id`)
    ) ENGINE=MyISAM AUTO_INCREMENT=34294 DEFAULT CHARSET=utf8;
    */
    private int id;
    private int petid;
    private int playerid;
    private byte[] pet;
    private int stone;
    private Date eattime;
    private long information;

    @Override
    public String getColumnNames(){
        return Tbl_PetmanagerDao.SQL_PARA;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append(Tools.toSqlString(id));
        sb.append(", ");
        sb.append(Tools.toSqlString(petid));
        sb.append(", ");
        sb.append(Tools.toSqlString(playerid));
        sb.append(", ");
        sb.append(Tools.toSqlString(pet));
        sb.append(", ");
        sb.append(Tools.toSqlString(stone));
        sb.append(", ");
        sb.append(Tools.toSqlString(eattime));
        sb.append(", ");
        sb.append(Tools.toSqlString(information));

        return sb.toString();
    }

    @Override
    public void process(MergeData mergeData, ServerConfig serverConfig){
        //处理id
    	id = mergeData.procPetmanagerId(id);
    	
    	//处理petid
    	petid = mergeData.procPetId(petid);
    	
    	//处理playerid
    	playerid = mergeData.procPlayerId(playerid);
    	
    	//处理pet
    	if(pet != null){
    		Pet p = Pet.getPetFromDb(pet);
    		p.setId(petid);
    		byte[] tmp = p.toDbBytes_version3();
    		byte[] tmp1 = new byte[tmp.length + 1];
    		System.arraycopy(tmp, 0, tmp1, 1, tmp.length);
    		tmp1[0] = 3;
    		pet = tmp1;
    	}
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getPetid(){
        return petid;
    }

    public void setPetid(int petid){
        this.petid = petid;
    }

    public int getPlayerid(){
        return playerid;
    }

    public void setPlayerid(int playerid){
        this.playerid = playerid;
    }

    public byte[] getPet(){
        return pet;
    }

    public void setPet(byte[] pet){
        this.pet = pet;
    }

    public int getStone(){
        return stone;
    }

    public void setStone(int stone){
        this.stone = stone;
    }

    public Date getEattime(){
        return eattime;
    }

    public void setEattime(Date eattime){
        this.eattime = eattime;
    }
    
    public long getInformation(){
    	return information;
    }
    
    public void setInformation(long information){
    	this.information = information;
    }
}
