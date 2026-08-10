package com.pip.itimes.bean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Date;

import org.apache.log4j.Logger;

import com.pip.itimes.MergeData;
import com.pip.itimes.MergeServer;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_UserdataDao;
import com.pip.itimes.server.stage.Pet;

public class Tbl_Userdata extends BaseTable{
	private static final Logger log = Logger.getLogger(MergeServer.class);
	
    /*
    CREATE TABLE `tbl_userdata` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `accountid` int(11) NOT NULL DEFAULT '0',
      `playername` varchar(255) NOT NULL DEFAULT '',
      `level` int(11) NOT NULL DEFAULT '0',
      `mapid` int(11) NOT NULL DEFAULT '0',
      `x` int(11) NOT NULL DEFAULT '0',
      `y` int(11) NOT NULL DEFAULT '0',
      `sex` tinyint(4) NOT NULL DEFAULT '0',
      `exp` int(11) NOT NULL DEFAULT '0',
      `returntimes` int(11) NOT NULL DEFAULT '0',
      `data` blob,
      `moeny` int(11) NOT NULL DEFAULT '0',
      `taskdata` blob,
      `tongid` int(11) NOT NULL DEFAULT '-1',
      `tongname` varchar(255) DEFAULT NULL,
      `tongduty` int(11) DEFAULT '-1',
      `tongtitle` varchar(255) DEFAULT NULL,
      `houselevel` int(11) NOT NULL DEFAULT '0',
      `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      `lastlogintime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      `credit` int(11) NOT NULL DEFAULT '0',
      `face` int(11) NOT NULL DEFAULT '0',
      `strength` int(11) NOT NULL DEFAULT '0',
      `agility` int(11) NOT NULL DEFAULT '0',
      `vitality` int(11) NOT NULL DEFAULT '0',
      `intelligence` int(11) NOT NULL DEFAULT '0',
      `luck` int(11) NOT NULL DEFAULT '0',
      `hp` int(11) DEFAULT NULL,
      `mp` int(11) NOT NULL DEFAULT '0',
      `leavepoints` int(11) NOT NULL DEFAULT '0',
      `abilities` blob,
      `techskills` blob,
      `basicitems` blob,
      `pets` blob,
      `options` blob,
      `metaitems` blob,
      `equipments` blob,
      `usedequipments` blob,
      `usedpet` blob,
      `taskitems` blob,
      `recipes` blob,
      `chatoptions` blob,
      `gridsize` int(11) NOT NULL DEFAULT '0',
      `friends` blob,
      `abilitypoints` int(11) DEFAULT NULL,
      `point` int(11) DEFAULT NULL,
      `addedgridsize` int(11) NOT NULL DEFAULT '0',
      `petid` int(11) NOT NULL DEFAULT '-1',
      `petsize` int(11) NOT NULL DEFAULT '0',
      `abilitytimes` int(11) NOT NULL DEFAULT '1',
      `valid` tinyint(4) NOT NULL DEFAULT '1',
      `messagecount` int(11) NOT NULL DEFAULT '0',
      `lastmessagetime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      `title` varchar(64) DEFAULT NULL,
      `modifynametimes` int(11) NOT NULL DEFAULT '0',
      `blacklist` blob,
      `bufs` blob,
      `jumpmapid` int(11) NOT NULL DEFAULT '0',
      `jumpx` int(11) NOT NULL DEFAULT '0',
      `jumpy` int(11) NOT NULL DEFAULT '0',
      `bathhousetime` datetime DEFAULT NULL,
      `questiontime` datetime DEFAULT NULL,
      `questionstate` int(11) NOT NULL DEFAULT '0',
      `lastkills` int(11) NOT NULL DEFAULT '0',
      `lastsneaks` int(11) NOT NULL DEFAULT '0',
      `kills` int(11) NOT NULL DEFAULT '0',
      `sneaks` int(11) NOT NULL DEFAULT '0',
      `vipbathhousetime` datetime DEFAULT NULL,
      `enemys` blob,
      `boxcount` int(11) NOT NULL DEFAULT '0',
      `contribution` int(11) NOT NULL DEFAULT '0',
      `consumepoint` int(11) NOT NULL DEFAULT '0',
      `islanditemtime` datetime DEFAULT NULL,
      `ibuylasttime` datetime DEFAULT NULL,
      `tongintime` datetime NOT NULL DEFAULT '1900-01-01 00:00:00',
      `arenav1id` int(11) NOT NULL DEFAULT '-1',
      `arenav2id` int(11) NOT NULL DEFAULT '-1',
      `arenav3id` int(11) NOT NULL DEFAULT '-1',
      `arenalevel` int(11) NOT NULL DEFAULT '0',
      `arenapoint` int(11) NOT NULL DEFAULT '0',
      `arenalevel2` int(11) NOT NULL DEFAULT '0',
      `arenalevel3` int(11) NOT NULL DEFAULT '0',
      `lastlogouttime` datetime DEFAULT NULL,
      `useskill` blob,
      `key9options` blob,
      `camp` tinyint(4) NOT NULL DEFAULT '0',
      `campwin` int(11) NOT NULL DEFAULT '0',
      `camplost` int(11) NOT NULL DEFAULT '0',
      `campcredit` int(11) NOT NULL DEFAULT '0',
      `endvotetime` datetime NOT NULL DEFAULT '2000-01-01 00:00:00',
      `roleface` blob,
      `prescription` blob,
      `playerpool` text NOT NULL COMMENT '参数池',
      `otherpool` text NOT NULL,
      PRIMARY KEY (`id`),
      KEY `index_user_name` (`playername`),
      KEY `index_user_accountid` (`accountid`),
      KEY `credit` (`credit`),
      KEY `index_user_lastkills` (`lastkills`),
      KEY `index_user_lastsneaks` (`lastsneaks`),
      KEY `tongid` (`tongid`),
      KEY `index_userdata_tongid` (`tongid`),
      KEY `index_userdata_arenalevel` (`arenalevel`),
      KEY `arenav3id_index` (`arenav3id`),
      KEY `arenav2id_index` (`arenav2id`),
      KEY `arenav1id_index` (`arenav1id`),
      KEY `index_userdata_camp` (`camp`),
      KEY `userdata_createtime_index` (`createtime`)
    ) ENGINE=MyISAM AUTO_INCREMENT=492461 DEFAULT CHARSET=utf8 DELAY_KEY_WRITE=1;
    */
    private int id;
    private int accountid;
    private String playername;
    private int level;
    private int mapid;
    private int x;
    private int y;
    private int sex;
    private int exp;
    private int returntimes;
    private byte[] data;
    private int moeny;
    private byte[] taskdata;
    private int tongid;
    private String tongname;
    private int tongduty;
    private String tongtitle;
    private int houselevel;
    private Date createtime;
    private Date lastlogintime;
    private int credit;
    private int face;
    private int strength;
    private int agility;
    private int vitality;
    private int intelligence;
    private int luck;
    private int hp;
    private int mp;
    private int leavepoints;
    private byte[] abilities;
    private byte[] techskills;
    private byte[] basicitems;
    private byte[] pets;
    private byte[] options;
    private byte[] metaitems;
    private byte[] equipments;
    private byte[] usedequipments;
    private byte[] usedpet;
    private byte[] taskitems;
    private byte[] recipes;
    private byte[] chatoptions;
    private int gridsize;
    private byte[] friends;
    private int abilitypoints;
    private int point;
    private int addedgridsize;
    private int petid;
    private int petsize;
    private int abilitytimes;
    private int valid;
    private int messagecount;
    private Date lastmessagetime;
    private String title;
    private int modifynametimes;
    private byte[] blacklist;
    private byte[] bufs;
    private int jumpmapid;
    private int jumpx;
    private int jumpy;
    private Date bathhousetime;
    private Date questiontime;
    private int questionstate;
    private int lastkills;
    private int lastsneaks;
    private int kills;
    private int sneaks;
    private Date vipbathhousetime;
    private byte[] enemys;
    private int boxcount;
    private int contribution;
    private int consumepoint;
    private Date islanditemtime;
    private Date ibuylasttime;
    private Date tongintime;
    private int arenav1id;
    private int arenav2id;
    private int arenav3id;
    private int arenalevel;
    private int arenapoint;
    private int arenalevel2;
    private int arenalevel3;
    private Date lastlogouttime;
    private byte[] useskill;
    private byte[] key9options;
    private int camp;
    private int campwin;
    private int camplost;
    private int campcredit;
    private Date endvotetime;
    private byte[] roleface;
    private byte[] prescription;
    private String playerpool;
    private String otherpool;

    @Override
    public String getColumnNames(){
        return Tbl_UserdataDao.SQL_PARA;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append(Tools.toSqlString(id));
        sb.append(", ");
        sb.append(Tools.toSqlString(accountid));
        sb.append(", ");
        sb.append(Tools.toSqlString(playername));
        sb.append(", ");
        sb.append(Tools.toSqlString(level));
        sb.append(", ");
        sb.append(Tools.toSqlString(mapid));
        sb.append(", ");
        sb.append(Tools.toSqlString(x));
        sb.append(", ");
        sb.append(Tools.toSqlString(y));
        sb.append(", ");
        sb.append(Tools.toSqlString(sex));
        sb.append(", ");
        sb.append(Tools.toSqlString(exp));
        sb.append(", ");
        sb.append(Tools.toSqlString(returntimes));
        sb.append(", ");
        sb.append(Tools.toSqlString(data));
        sb.append(", ");
        sb.append(Tools.toSqlString(moeny));
        sb.append(", ");
        sb.append(Tools.toSqlString(taskdata));
        sb.append(", ");
        sb.append(Tools.toSqlString(tongid));
        sb.append(", ");
        sb.append(Tools.toSqlString(tongname));
        sb.append(", ");
        sb.append(Tools.toSqlString(tongduty));
        sb.append(", ");
        sb.append(Tools.toSqlString(tongtitle));
        sb.append(", ");
        sb.append(Tools.toSqlString(houselevel));
        sb.append(", ");
        sb.append(Tools.toSqlString(createtime));
        sb.append(", ");
        sb.append(Tools.toSqlString(lastlogintime));
        sb.append(", ");
        sb.append(Tools.toSqlString(credit));
        sb.append(", ");
        sb.append(Tools.toSqlString(face));
        sb.append(", ");
        sb.append(Tools.toSqlString(strength));
        sb.append(", ");
        sb.append(Tools.toSqlString(agility));
        sb.append(", ");
        sb.append(Tools.toSqlString(vitality));
        sb.append(", ");
        sb.append(Tools.toSqlString(intelligence));
        sb.append(", ");
        sb.append(Tools.toSqlString(luck));
        sb.append(", ");
        sb.append(Tools.toSqlString(hp));
        sb.append(", ");
        sb.append(Tools.toSqlString(mp));
        sb.append(", ");
        sb.append(Tools.toSqlString(leavepoints));
        sb.append(", ");
        sb.append(Tools.toSqlString(abilities));
        sb.append(", ");
        sb.append(Tools.toSqlString(techskills));
        sb.append(", ");
        sb.append(Tools.toSqlString(basicitems));
        sb.append(", ");
        sb.append(Tools.toSqlString(pets));
        sb.append(", ");
        sb.append(Tools.toSqlString(options));
        sb.append(", ");
        sb.append(Tools.toSqlString(metaitems));
        sb.append(", ");
        sb.append(Tools.toSqlString(equipments));
        sb.append(", ");
        sb.append(Tools.toSqlString(usedequipments));
        sb.append(", ");
        sb.append(Tools.toSqlString(usedpet));
        sb.append(", ");
        sb.append(Tools.toSqlString(taskitems));
        sb.append(", ");
        sb.append(Tools.toSqlString(recipes));
        sb.append(", ");
        sb.append(Tools.toSqlString(chatoptions));
        sb.append(", ");
        sb.append(Tools.toSqlString(gridsize));
        sb.append(", ");
        sb.append(Tools.toSqlString(friends));
        sb.append(", ");
        sb.append(Tools.toSqlString(abilitypoints));
        sb.append(", ");
        sb.append(Tools.toSqlString(point));
        sb.append(", ");
        sb.append(Tools.toSqlString(addedgridsize));
        sb.append(", ");
        sb.append(Tools.toSqlString(petid));
        sb.append(", ");
        sb.append(Tools.toSqlString(petsize));
        sb.append(", ");
        sb.append(Tools.toSqlString(abilitytimes));
        sb.append(", ");
        sb.append(Tools.toSqlString(valid));
        sb.append(", ");
        sb.append(Tools.toSqlString(messagecount));
        sb.append(", ");
        sb.append(Tools.toSqlString(lastmessagetime));
        sb.append(", ");
        sb.append(Tools.toSqlString(title));
        sb.append(", ");
        sb.append(Tools.toSqlString(modifynametimes));
        sb.append(", ");
        sb.append(Tools.toSqlString(blacklist));
        sb.append(", ");
        sb.append(Tools.toSqlString(bufs));
        sb.append(", ");
        sb.append(Tools.toSqlString(jumpmapid));
        sb.append(", ");
        sb.append(Tools.toSqlString(jumpx));
        sb.append(", ");
        sb.append(Tools.toSqlString(jumpy));
        sb.append(", ");
        sb.append(Tools.toSqlString(bathhousetime));
        sb.append(", ");
        sb.append(Tools.toSqlString(questiontime));
        sb.append(", ");
        sb.append(Tools.toSqlString(questionstate));
        sb.append(", ");
        sb.append(Tools.toSqlString(lastkills));
        sb.append(", ");
        sb.append(Tools.toSqlString(lastsneaks));
        sb.append(", ");
        sb.append(Tools.toSqlString(kills));
        sb.append(", ");
        sb.append(Tools.toSqlString(sneaks));
        sb.append(", ");
        sb.append(Tools.toSqlString(vipbathhousetime));
        sb.append(", ");
        sb.append(Tools.toSqlString(enemys));
        sb.append(", ");
        sb.append(Tools.toSqlString(boxcount));
        sb.append(", ");
        sb.append(Tools.toSqlString(contribution));
        sb.append(", ");
        sb.append(Tools.toSqlString(consumepoint));
        sb.append(", ");
        sb.append(Tools.toSqlString(islanditemtime));
        sb.append(", ");
        sb.append(Tools.toSqlString(ibuylasttime));
        sb.append(", ");
        sb.append(Tools.toSqlString(tongintime));
        sb.append(", ");
        sb.append(Tools.toSqlString(arenav1id));
        sb.append(", ");
        sb.append(Tools.toSqlString(arenav2id));
        sb.append(", ");
        sb.append(Tools.toSqlString(arenav3id));
        sb.append(", ");
        sb.append(Tools.toSqlString(arenalevel));
        sb.append(", ");
        sb.append(Tools.toSqlString(arenapoint));
        sb.append(", ");
        sb.append(Tools.toSqlString(arenalevel2));
        sb.append(", ");
        sb.append(Tools.toSqlString(arenalevel3));
        sb.append(", ");
        sb.append(Tools.toSqlString(lastlogouttime));
        sb.append(", ");
        sb.append(Tools.toSqlString(useskill));
        sb.append(", ");
        sb.append(Tools.toSqlString(key9options));
        sb.append(", ");
        sb.append(Tools.toSqlString(camp));
        sb.append(", ");
        sb.append(Tools.toSqlString(campwin));
        sb.append(", ");
        sb.append(Tools.toSqlString(camplost));
        sb.append(", ");
        sb.append(Tools.toSqlString(campcredit));
        sb.append(", ");
        sb.append(Tools.toSqlString(endvotetime));
        sb.append(", ");
        sb.append(Tools.toSqlString(roleface));
        sb.append(", ");
        sb.append(Tools.toSqlString(prescription));
        sb.append(", ");
        sb.append(Tools.toSqlString(playerpool));
        sb.append(", ");
        sb.append(Tools.toSqlString(otherpool));

        return sb.toString();
    }

    @Override
    public void process(MergeData mergeData, ServerConfig serverConfig){
        //处理id
    	id = mergeData.procPlayerId(id);
    	
    	//处理playername
    	String oldName = playername;
    	playername = mergeData.procPlayerName(playername);
    	
    	//处理tongid
    	tongid = mergeData.procTongId(tongid);
    	
    	//处理tongname
    	tongname = mergeData.procTongName(tongname);
    	
    	//处理pets
    	if(pets != null){
    		Pet[] petArray = Pet.getPetsFromDb(pets);
    		
    		for(int i = 0; i < petArray.length; i++){
    			int pid = petArray[i].getId();
    			pid = mergeData.procPetId(pid);
    			petArray[i].setId(pid);
    		}
    		
    		pets = Pet.toDbBytes_version5(petArray);
    	}
    	
    	//处理equipments
    	if (equipments != null && equipments.length > 2) {
			equipments = Tools.procEquipments(equipments, mergeData);
        }
    	
    	//处理usedequipments
    	if(usedequipments != null && usedequipments.length > 2){
    		usedequipments = Tools.procEquipments(usedequipments, mergeData);
    	}
    	
    	//处理petid
    	if(petid > 0){
    		petid = mergeData.procPetId(petid);
    	}
    	
    	//处理friends
    	if (friends != null && friends.length > 0) {
    		try{
	            ByteArrayInputStream bis = new ByteArrayInputStream(friends);
	            DataInputStream dis = new DataInputStream(bis);
	            ByteArrayOutputStream bos = new ByteArrayOutputStream();
	            DataOutputStream dos = new DataOutputStream(bos);
	            
	            byte count = dis.readByte();
	            dos.writeByte(count);
	            
	            for (int i = 0; i < count; i++) {
	                int id = dis.readInt();
	                id = mergeData.procPlayerId(id);
	                dos.writeInt(id);
	                
	                String name = dis.readUTF();
	                name = mergeData.procPlayerName(name);
	                dos.writeUTF(name);
	                
	                int favorite = dis.readInt();
	                dos.writeInt(favorite);
	            }
	            
	            friends = bos.toByteArray();
    		}catch(Exception e){
    			log.error(e, e);
    		}
        }
    	
    	//处理blacklist
    	if (blacklist != null && blacklist.length > 0) {
    		try{
	            ByteArrayInputStream bis = new ByteArrayInputStream(blacklist);
	            DataInputStream dis = new DataInputStream(bis);
	            ByteArrayOutputStream bos = new ByteArrayOutputStream();
	            DataOutputStream dos = new DataOutputStream(bos);
	            
	            byte count = dis.readByte();
	            dos.writeByte(count);
	            
	            for (int i = 0; i < count; i++) {
	                int id = dis.readInt();
	                id = mergeData.procPlayerId(id);
	                dos.writeInt(id);
	                
	                String name = dis.readUTF();
	                name = mergeData.procPlayerName(name);
	                dos.writeUTF(name);
	            }
	            
	            blacklist = bos.toByteArray();
    		}catch(Exception e){
    			log.error(e, e);
    		}
        }
    	
    	//处理enemys
    	if(enemys != null && enemys.length > 0){
    		try{
	            ByteArrayInputStream bis = new ByteArrayInputStream(enemys);
	            DataInputStream dis = new DataInputStream(bis);
	            ByteArrayOutputStream bos = new ByteArrayOutputStream();
	            DataOutputStream dos = new DataOutputStream(bos);
	            
	            short len = dis.readShort();
	            dos.writeShort(len);
	            
	            for(int i=0;i<len;i++){
	                int id = dis.readInt();
	                id = mergeData.procPlayerId(id);
	                dos.writeInt(id);
	                
	                String name = dis.readUTF();
	                name = mergeData.procPlayerName(name);
	                dos.writeUTF(name);
	                
	                int times = dis.readInt();
	                dos.writeInt(times);
	                
	                long lastTime = dis.readLong();
	                dos.writeLong(lastTime);
	            }
	            
	            enemys = bos.toByteArray();
    		}catch(Exception e){
    			log.error(e, e);
    		}
        }
    	
    	//处理arenav1id, arenav2id, arenav3id
    	arenav1id = mergeData.procArenaTeamId(arenav1id);
    	arenav2id = mergeData.procArenaTeamId(arenav2id);
    	arenav3id = mergeData.procArenaTeamId(arenav3id);
    	
    	if (!playername.equals(oldName)) {
            // 需要给军团长发一个军团改名符
            mergeData.renamePlayers.put(id, playername);
        }
    }
    
    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getAccountid(){
        return accountid;
    }

    public void setAccountid(int accountid){
        this.accountid = accountid;
    }

    public String getPlayername(){
        return playername;
    }

    public void setPlayername(String playername){
        this.playername = playername;
    }

    public int getLevel(){
        return level;
    }

    public void setLevel(int level){
        this.level = level;
    }

    public int getMapid(){
        return mapid;
    }

    public void setMapid(int mapid){
        this.mapid = mapid;
    }

    public int getX(){
        return x;
    }

    public void setX(int x){
        this.x = x;
    }

    public int getY(){
        return y;
    }

    public void setY(int y){
        this.y = y;
    }

    public int getSex(){
        return sex;
    }

    public void setSex(int sex){
        this.sex = sex;
    }

    public int getExp(){
        return exp;
    }

    public void setExp(int exp){
        this.exp = exp;
    }

    public int getReturntimes(){
        return returntimes;
    }

    public void setReturntimes(int returntimes){
        this.returntimes = returntimes;
    }

    public byte[] getData(){
        return data;
    }

    public void setData(byte[] data){
        this.data = data;
    }

    public int getMoeny(){
        return moeny;
    }

    public void setMoeny(int moeny){
        this.moeny = moeny;
    }

    public byte[] getTaskdata(){
        return taskdata;
    }

    public void setTaskdata(byte[] taskdata){
        this.taskdata = taskdata;
    }

    public int getTongid(){
        return tongid;
    }

    public void setTongid(int tongid){
        this.tongid = tongid;
    }

    public String getTongname(){
        return tongname;
    }

    public void setTongname(String tongname){
        this.tongname = tongname;
    }

    public int getTongduty(){
        return tongduty;
    }

    public void setTongduty(int tongduty){
        this.tongduty = tongduty;
    }

    public String getTongtitle(){
        return tongtitle;
    }

    public void setTongtitle(String tongtitle){
        this.tongtitle = tongtitle;
    }

    public int getHouselevel(){
        return houselevel;
    }

    public void setHouselevel(int houselevel){
        this.houselevel = houselevel;
    }

    public Date getCreatetime(){
        return createtime;
    }

    public void setCreatetime(Date createtime){
        this.createtime = createtime;
    }

    public Date getLastlogintime(){
        return lastlogintime;
    }

    public void setLastlogintime(Date lastlogintime){
        this.lastlogintime = lastlogintime;
    }

    public int getCredit(){
        return credit;
    }

    public void setCredit(int credit){
        this.credit = credit;
    }

    public int getFace(){
        return face;
    }

    public void setFace(int face){
        this.face = face;
    }

    public int getStrength(){
        return strength;
    }

    public void setStrength(int strength){
        this.strength = strength;
    }

    public int getAgility(){
        return agility;
    }

    public void setAgility(int agility){
        this.agility = agility;
    }

    public int getVitality(){
        return vitality;
    }

    public void setVitality(int vitality){
        this.vitality = vitality;
    }

    public int getIntelligence(){
        return intelligence;
    }

    public void setIntelligence(int intelligence){
        this.intelligence = intelligence;
    }

    public int getLuck(){
        return luck;
    }

    public void setLuck(int luck){
        this.luck = luck;
    }

    public int getHp(){
        return hp;
    }

    public void setHp(int hp){
        this.hp = hp;
    }

    public int getMp(){
        return mp;
    }

    public void setMp(int mp){
        this.mp = mp;
    }

    public int getLeavepoints(){
        return leavepoints;
    }

    public void setLeavepoints(int leavepoints){
        this.leavepoints = leavepoints;
    }

    public byte[] getAbilities(){
        return abilities;
    }

    public void setAbilities(byte[] abilities){
        this.abilities = abilities;
    }

    public byte[] getTechskills(){
        return techskills;
    }

    public void setTechskills(byte[] techskills){
        this.techskills = techskills;
    }

    public byte[] getBasicitems(){
        return basicitems;
    }

    public void setBasicitems(byte[] basicitems){
        this.basicitems = basicitems;
    }

    public byte[] getPets(){
        return pets;
    }

    public void setPets(byte[] pets){
        this.pets = pets;
    }

    public byte[] getOptions(){
        return options;
    }

    public void setOptions(byte[] options){
        this.options = options;
    }

    public byte[] getMetaitems(){
        return metaitems;
    }

    public void setMetaitems(byte[] metaitems){
        this.metaitems = metaitems;
    }

    public byte[] getEquipments(){
        return equipments;
    }

    public void setEquipments(byte[] equipments){
        this.equipments = equipments;
    }

    public byte[] getUsedequipments(){
        return usedequipments;
    }

    public void setUsedequipments(byte[] usedequipments){
        this.usedequipments = usedequipments;
    }

    public byte[] getUsedpet(){
        return usedpet;
    }

    public void setUsedpet(byte[] usedpet){
        this.usedpet = usedpet;
    }

    public byte[] getTaskitems(){
        return taskitems;
    }

    public void setTaskitems(byte[] taskitems){
        this.taskitems = taskitems;
    }

    public byte[] getRecipes(){
        return recipes;
    }

    public void setRecipes(byte[] recipes){
        this.recipes = recipes;
    }

    public byte[] getChatoptions(){
        return chatoptions;
    }

    public void setChatoptions(byte[] chatoptions){
        this.chatoptions = chatoptions;
    }

    public int getGridsize(){
        return gridsize;
    }

    public void setGridsize(int gridsize){
        this.gridsize = gridsize;
    }

    public byte[] getFriends(){
        return friends;
    }

    public void setFriends(byte[] friends){
        this.friends = friends;
    }

    public int getAbilitypoints(){
        return abilitypoints;
    }

    public void setAbilitypoints(int abilitypoints){
        this.abilitypoints = abilitypoints;
    }

    public int getPoint(){
        return point;
    }

    public void setPoint(int point){
        this.point = point;
    }

    public int getAddedgridsize(){
        return addedgridsize;
    }

    public void setAddedgridsize(int addedgridsize){
        this.addedgridsize = addedgridsize;
    }

    public int getPetid(){
        return petid;
    }

    public void setPetid(int petid){
        this.petid = petid;
    }

    public int getPetsize(){
        return petsize;
    }

    public void setPetsize(int petsize){
        this.petsize = petsize;
    }

    public int getAbilitytimes(){
        return abilitytimes;
    }

    public void setAbilitytimes(int abilitytimes){
        this.abilitytimes = abilitytimes;
    }

    public int getValid(){
        return valid;
    }

    public void setValid(int valid){
        this.valid = valid;
    }

    public int getMessagecount(){
        return messagecount;
    }

    public void setMessagecount(int messagecount){
        this.messagecount = messagecount;
    }

    public Date getLastmessagetime(){
        return lastmessagetime;
    }

    public void setLastmessagetime(Date lastmessagetime){
        this.lastmessagetime = lastmessagetime;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public int getModifynametimes(){
        return modifynametimes;
    }

    public void setModifynametimes(int modifynametimes){
        this.modifynametimes = modifynametimes;
    }

    public byte[] getBlacklist(){
        return blacklist;
    }

    public void setBlacklist(byte[] blacklist){
        this.blacklist = blacklist;
    }

    public byte[] getBufs(){
        return bufs;
    }

    public void setBufs(byte[] bufs){
        this.bufs = bufs;
    }

    public int getJumpmapid(){
        return jumpmapid;
    }

    public void setJumpmapid(int jumpmapid){
        this.jumpmapid = jumpmapid;
    }

    public int getJumpx(){
        return jumpx;
    }

    public void setJumpx(int jumpx){
        this.jumpx = jumpx;
    }

    public int getJumpy(){
        return jumpy;
    }

    public void setJumpy(int jumpy){
        this.jumpy = jumpy;
    }

    public Date getBathhousetime(){
        return bathhousetime;
    }

    public void setBathhousetime(Date bathhousetime){
        this.bathhousetime = bathhousetime;
    }

    public Date getQuestiontime(){
        return questiontime;
    }

    public void setQuestiontime(Date questiontime){
        this.questiontime = questiontime;
    }

    public int getQuestionstate(){
        return questionstate;
    }

    public void setQuestionstate(int questionstate){
        this.questionstate = questionstate;
    }

    public int getLastkills(){
        return lastkills;
    }

    public void setLastkills(int lastkills){
        this.lastkills = lastkills;
    }

    public int getLastsneaks(){
        return lastsneaks;
    }

    public void setLastsneaks(int lastsneaks){
        this.lastsneaks = lastsneaks;
    }

    public int getKills(){
        return kills;
    }

    public void setKills(int kills){
        this.kills = kills;
    }

    public int getSneaks(){
        return sneaks;
    }

    public void setSneaks(int sneaks){
        this.sneaks = sneaks;
    }

    public Date getVipbathhousetime(){
        return vipbathhousetime;
    }

    public void setVipbathhousetime(Date vipbathhousetime){
        this.vipbathhousetime = vipbathhousetime;
    }

    public byte[] getEnemys(){
        return enemys;
    }

    public void setEnemys(byte[] enemys){
        this.enemys = enemys;
    }

    public int getBoxcount(){
        return boxcount;
    }

    public void setBoxcount(int boxcount){
        this.boxcount = boxcount;
    }

    public int getContribution(){
        return contribution;
    }

    public void setContribution(int contribution){
        this.contribution = contribution;
    }

    public int getConsumepoint(){
        return consumepoint;
    }

    public void setConsumepoint(int consumepoint){
        this.consumepoint = consumepoint;
    }

    public Date getIslanditemtime(){
        return islanditemtime;
    }

    public void setIslanditemtime(Date islanditemtime){
        this.islanditemtime = islanditemtime;
    }

    public Date getIbuylasttime(){
        return ibuylasttime;
    }

    public void setIbuylasttime(Date ibuylasttime){
        this.ibuylasttime = ibuylasttime;
    }

    public Date getTongintime(){
        return tongintime;
    }

    public void setTongintime(Date tongintime){
        this.tongintime = tongintime;
    }

    public int getArenav1id(){
        return arenav1id;
    }

    public void setArenav1id(int arenav1id){
        this.arenav1id = arenav1id;
    }

    public int getArenav2id(){
        return arenav2id;
    }

    public void setArenav2id(int arenav2id){
        this.arenav2id = arenav2id;
    }

    public int getArenav3id(){
        return arenav3id;
    }

    public void setArenav3id(int arenav3id){
        this.arenav3id = arenav3id;
    }

    public int getArenalevel(){
        return arenalevel;
    }

    public void setArenalevel(int arenalevel){
        this.arenalevel = arenalevel;
    }

    public int getArenapoint(){
        return arenapoint;
    }

    public void setArenapoint(int arenapoint){
        this.arenapoint = arenapoint;
    }

    public int getArenalevel2(){
        return arenalevel2;
    }

    public void setArenalevel2(int arenalevel2){
        this.arenalevel2 = arenalevel2;
    }

    public int getArenalevel3(){
        return arenalevel3;
    }

    public void setArenalevel3(int arenalevel3){
        this.arenalevel3 = arenalevel3;
    }

    public Date getLastlogouttime(){
        return lastlogouttime;
    }

    public void setLastlogouttime(Date lastlogouttime){
        this.lastlogouttime = lastlogouttime;
    }

    public byte[] getUseskill(){
        return useskill;
    }

    public void setUseskill(byte[] useskill){
        this.useskill = useskill;
    }

    public byte[] getKey9options(){
        return key9options;
    }

    public void setKey9options(byte[] key9options){
        this.key9options = key9options;
    }

    public int getCamp(){
        return camp;
    }

    public void setCamp(int camp){
        this.camp = camp;
    }

    public int getCampwin(){
        return campwin;
    }

    public void setCampwin(int campwin){
        this.campwin = campwin;
    }

    public int getCamplost(){
        return camplost;
    }

    public void setCamplost(int camplost){
        this.camplost = camplost;
    }

    public int getCampcredit(){
        return campcredit;
    }

    public void setCampcredit(int campcredit){
        this.campcredit = campcredit;
    }

    public Date getEndvotetime(){
        return endvotetime;
    }

    public void setEndvotetime(Date endvotetime){
        this.endvotetime = endvotetime;
    }

    public byte[] getRoleface(){
        return roleface;
    }

    public void setRoleface(byte[] roleface){
        this.roleface = roleface;
    }

    public byte[] getPrescription(){
        return prescription;
    }

    public void setPrescription(byte[] prescription){
        this.prescription = prescription;
    }
    
    public String getPlayerpool(){
    	return playerpool;
    }
    
    public void setPlayerpool(String playerpool){
    	this.playerpool = playerpool;
    }
    
    public String getOtherpool(){
    	return otherpool;
    }
    
    public void setOtherpool(String otherpool){
    	this.otherpool = otherpool;
    }
}
