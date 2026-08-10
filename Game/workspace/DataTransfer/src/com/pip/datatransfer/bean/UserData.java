package com.pip.datatransfer.bean;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;


@Entity
@Table(name = "tbl_userdata")
public class UserData{
    @Id
    //@GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="id")
    private int id;
    
    @Column(name="accountid",nullable=false)
    private String accountid;

    @Column(name="playername",nullable=false)
    private String playername;

    @Column(name="level",nullable=false)
    private int level;
    
    @Column(name="mapid",nullable=false)
    private short mapid;

    @Column(name="x",nullable=false)
    private short x;
    
    @Column(name="y",nullable=false)
    private short y;
    
    @Column(name="sex",nullable=false)
    private byte sex;
    
    @Column(name="exp",nullable=false)
    private int exp;
    
    @Column(name="returntimes",nullable=false)
    private byte returntimes;

    @Column(name="moeny",nullable=false)
    private int moeny;

    @Column(name="tongid",nullable=false)
    private int tongid;

    @Column(name="tongname",nullable=true)
    private String tongname;
    
    @Column(name="tongtitle",nullable=true)
    private String tongtitle;

    @Column(name="tongduty",nullable=true)
    private int tongduty;

    @Column(name="houselevel",nullable=false)
    private int houselevel;
    
    @Column(name="createtime",nullable=false)
    private Date createtime;
    
    @Column(name="lastlogintime",nullable=false)
    private Date lastlogintime;
    
    @Column(name="credit",nullable=false)
    private int credit;

    @Column(name="face",nullable=false)
    private short face;
    
    @Column(name="strength",nullable=true)
    private int strength;
    
    @Column(name="agility",nullable=true)
    private int agility;
    
    @Column(name="vitality",nullable=true)
    private int vitality;
    
    @Column(name="intelligence",nullable=true)
    private int intelligence;

    @Column(name="luck",nullable=true)
    private int luck;
    
    @Column(name="hp",nullable=true)
    private int hp;
    
    @Column(name="mp",nullable=true)
    private int mp;
    
    @Column(name="leavepoints",nullable=true)
    private int leavepoints;
    
    @Column(name="abilities",nullable=true)
    private byte[] abilities;
    
    @Column(name="techskills",nullable=true)
    private byte[] techskills;
    
    @Column(name="basicitems",nullable=true)
    private byte[] basicitems;
    
    @Column(name="pets",nullable=true)
    private byte[] pets;
    
    @Column(name="options",nullable=true)
    private byte[] options;
    
    @Column(name="metaitems",nullable=true)
    private byte[] metaitems;
    
    @Column(name="equipments",nullable=true)
    private byte[] equipments;

    @Column(name="usedequipments",nullable=true)
    private byte[] usedequipments;
    
    @Column(name="usedpet",nullable=true)
    private byte[] usedpet;
    
    @Column(name="taskitems",nullable=true)
    private byte[] taskitems;
    
    @Column(name="recipes",nullable=true)
    private byte[] recipes;
    
    @Column(name="chatoptions",nullable=true)
    private byte[] chatoptions;

    @Column(name="gridsize",nullable=true)
    private short gridsize;
    
    @Column(name="addedgridsize",nullable=true)
    private int addedgridsize;

    @Column(name="friends",nullable=true)
    private byte[] friends;
    
    @Column(name="blacklist",nullable=true)
    private byte[] blacklist;

    @Column(name="point",nullable=true)
    private int point;
    
    @Column(name="abilitypoints",nullable=true)
    private int abilitypoints;
    
    @Column(name="petid",nullable=true)
    private int petid;
    
    @Column(name="petsize",nullable=true)
    private int petsize;
    
    @Column(name="abilitytimes",nullable=false)
    private int abilitytimes;
    
    @Column(name="valid",nullable=false)
    private boolean valid;

    @Column(name="messagecount",nullable=false)
    private int messagecount;
    
    @Column(name="lastmessagetime",nullable=false)
    private Date lastmessagetime;

    @Column(name="title",nullable=false)
    private String title;
    
    @Column(name="modifynametimes",nullable=false)
    private int modifynametimes;
    
    @Column(name="bufs",nullable=true)
    private byte[] bufs;
    
    @Column(name="jumpmapid",nullable=false)
    private short jumpmapid;
    
    @Column(name="jumpx",nullable=false)
    private short jumpx;
    
    @Column(name="jumpy",nullable=false)
    private short jumpy;
    
    @Column(name="bathhousetime",nullable=true)
    private Date bathhousetime;
    
    @Column(name="questiontime",nullable=true)
    private Date questiontime;
    
    @Column(name="questionstate",nullable=true)
    private int questionstate;
    
    @Column(name="lastkills",nullable=false)
    private int lastkills;
    
    @Column(name="lastsneaks",nullable=false)
    private int lastsneaks;

    @Column(name="kills",nullable=false)
    private int kills;
    
    @Column(name="sneaks",nullable=false)
    private int sneaks;
    
    @Column(name="vipbathhousetime",nullable=true)
    private Date vipbathhousetime;

    @Column(name="enemys",nullable=true)
    private byte[] enemys;
    
    @Column(name="boxcount",nullable=true)
    private int boxcount;

    @Column(name="contribution",nullable=false)
    private int contribution;
    
    @Column(name="consumepoint",nullable=false)
    private int consumepoint;
    
    @Column(name="islanditemtime",nullable=true)
    private Date islanditemtime;

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getAccountid(){
        return accountid;
    }

    public void setAccountid(String accountid){
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

    public short getMapid(){
        return mapid;
    }

    public void setMapid(short mapid){
        this.mapid = mapid;
    }

    public short getX(){
        return x;
    }

    public void setX(short x){
        this.x = x;
    }

    public short getY(){
        return y;
    }

    public void setY(short y){
        this.y = y;
    }

    public byte getSex(){
        return sex;
    }

    public void setSex(byte sex){
        this.sex = sex;
    }

    public int getExp(){
        return exp;
    }

    public void setExp(int exp){
        this.exp = exp;
    }

    public byte getReturntimes(){
        return returntimes;
    }

    public void setReturntimes(byte returntimes){
        this.returntimes = returntimes;
    }

    public int getMoeny(){
        return moeny;
    }

    public void setMoeny(int moeny){
        this.moeny = moeny;
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

    public String getTongtitle(){
        return tongtitle;
    }

    public void setTongtitle(String tongtitle){
        this.tongtitle = tongtitle;
    }

    public int getTongduty(){
        return tongduty;
    }

    public void setTongduty(int tongduty){
        this.tongduty = tongduty;
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

    public short getFace(){
        return face;
    }

    public void setFace(short face){
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

    public short getGridsize(){
        return gridsize;
    }

    public void setGridsize(short gridsize){
        this.gridsize = gridsize;
    }

    public int getAddedgridsize(){
        return addedgridsize;
    }

    public void setAddedgridsize(int addedgridsize){
        this.addedgridsize = addedgridsize;
    }

    public byte[] getFriends(){
        return friends;
    }

    public void setFriends(byte[] friends){
        this.friends = friends;
    }

    public byte[] getBlacklist(){
        return blacklist;
    }

    public void setBlacklist(byte[] blacklist){
        this.blacklist = blacklist;
    }

    public int getPoint(){
        return point;
    }

    public void setPoint(int point){
        this.point = point;
    }

    public int getAbilitypoints(){
        return abilitypoints;
    }

    public void setAbilitypoints(int abilitypoints){
        this.abilitypoints = abilitypoints;
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

    public boolean isValid(){
        return valid;
    }

    public void setValid(boolean valid){
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

    public byte[] getBufs(){
        return bufs;
    }

    public void setBufs(byte[] bufs){
        this.bufs = bufs;
    }

    public short getJumpmapid(){
        return jumpmapid;
    }

    public void setJumpmapid(short jumpmapid){
        this.jumpmapid = jumpmapid;
    }

    public short getJumpx(){
        return jumpx;
    }

    public void setJumpx(short jumpx){
        this.jumpx = jumpx;
    }

    public short getJumpy(){
        return jumpy;
    }

    public void setJumpy(short jumpy){
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
}
