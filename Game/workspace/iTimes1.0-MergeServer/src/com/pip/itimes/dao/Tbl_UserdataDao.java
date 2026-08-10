package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Userdata;

public class Tbl_UserdataDao extends BaseDao{
    private static final Logger log = Logger.getLogger(Tbl_UserdataDao.class);

    public static final String SQL_PARA = "id, accountid, playername, level, mapid, x, y, sex, exp, returntimes, data, moeny, taskdata, tongid, tongname, tongduty, tongtitle, "
                    + "houselevel, createtime, lastlogintime, credit, face, strength, agility, vitality, intelligence, luck, hp, mp, leavepoints, abilities, techskills, basicitems, pets, "
                    + "options, metaitems, equipments, usedequipments, usedpet, taskitems, recipes, chatoptions, gridsize, friends, abilitypoints, point, addedgridsize, petid, petsize, "
                    + "abilitytimes, valid, messagecount, lastmessagetime, title, modifynametimes, blacklist, bufs, jumpmapid, jumpx, jumpy, bathhousetime, questiontime, questionstate, "
                    + "lastkills, lastsneaks, kills, sneaks, vipbathhousetime, enemys, boxcount, contribution, consumepoint, islanditemtime, ibuylasttime, tongintime, arenav1id, arenav2id, "
                    + "arenav3id, arenalevel, arenapoint, arenalevel2, arenalevel3, lastlogouttime, useskill, key9options, camp, campwin, camplost, campcredit, endvotetime, roleface, prescription, "
                    + "playerpool, otherpool";

    public Tbl_UserdataDao(ServerConfig config){
        super(config);
    }

    public ArrayList<String> getAllNames(){
        String query = buildQuery("select playername from " + Tbl_Userdata.class.getSimpleName().toLowerCase() + " where valid = \'1\'");
        Statement statement = getStatement();
        ArrayList<String> list = new ArrayList<String>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                list.add(rs.getString(1));
            }
        }catch(Exception e){
            log.error(e, e);
        }finally{
            try{
                statement.close();

                if(rs != null){
                    rs.close();
                }
            }catch(Exception e){
            }
        }

        return list;
    }
    
    public Tbl_Userdata getPlayer(int id){
    	String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Userdata.class.getSimpleName().toLowerCase()) + " where id=" + id;
    	Statement statement = getStatement();
        ResultSet rs = null;
        try{
            rs = statement.executeQuery(query);
            while(rs.next()){
                Tbl_Userdata userdata = new Tbl_Userdata();

                userdata.setId(rs.getInt(1));
                userdata.setAccountid(rs.getInt(2));
                userdata.setPlayername(rs.getString(3));
                userdata.setLevel(rs.getInt(4));
                userdata.setMapid(rs.getInt(5));
                userdata.setX(rs.getInt(6));
                userdata.setY(rs.getInt(7));
                userdata.setSex(rs.getInt(8));
                userdata.setExp(rs.getInt(9));
                userdata.setReturntimes(rs.getInt(10));
                userdata.setData(rs.getBytes(11));
                userdata.setMoeny(rs.getInt(12));
                userdata.setTaskdata(rs.getBytes(13));
                userdata.setTongid(rs.getInt(14));
                userdata.setTongname(rs.getString(15));
                userdata.setTongduty(rs.getInt(16));
                userdata.setTongtitle(rs.getString(17));
                userdata.setHouselevel(rs.getInt(18));
                userdata.setCreatetime(rs.getTimestamp(19));
                userdata.setLastlogintime(rs.getTimestamp(20));
                userdata.setCredit(rs.getInt(21));
                userdata.setFace(rs.getInt(22));
                userdata.setStrength(rs.getInt(23));
                userdata.setAgility(rs.getInt(24));
                userdata.setVitality(rs.getInt(25));
                userdata.setIntelligence(rs.getInt(26));
                userdata.setLuck(rs.getInt(27));
                userdata.setHp(rs.getInt(28));
                userdata.setMp(rs.getInt(29));
                userdata.setLeavepoints(rs.getInt(30));
                userdata.setAbilities(rs.getBytes(31));
                userdata.setTechskills(rs.getBytes(32));
                userdata.setBasicitems(rs.getBytes(33));
                userdata.setPets(rs.getBytes(34));
                userdata.setOptions(rs.getBytes(35));
                userdata.setMetaitems(rs.getBytes(36));
                userdata.setEquipments(rs.getBytes(37));
                userdata.setUsedequipments(rs.getBytes(38));
                userdata.setUsedpet(rs.getBytes(39));
                userdata.setTaskitems(rs.getBytes(40));
                userdata.setRecipes(rs.getBytes(41));
                userdata.setChatoptions(rs.getBytes(42));
                userdata.setGridsize(rs.getInt(43));
                userdata.setFriends(rs.getBytes(44));
                userdata.setAbilitypoints(rs.getInt(45));
                userdata.setPoint(rs.getInt(46));
                userdata.setAddedgridsize(rs.getInt(47));
                userdata.setPetid(rs.getInt(48));
                userdata.setPetsize(rs.getInt(49));
                userdata.setAbilitytimes(rs.getInt(50));
                userdata.setValid(rs.getInt(51));
                userdata.setMessagecount(rs.getInt(52));
                userdata.setLastmessagetime(rs.getTimestamp(53));
                userdata.setTitle(rs.getString(54));
                userdata.setModifynametimes(rs.getInt(55));
                userdata.setBlacklist(rs.getBytes(56));
                userdata.setBufs(rs.getBytes(57));
                userdata.setJumpmapid(rs.getInt(58));
                userdata.setJumpx(rs.getInt(59));
                userdata.setJumpy(rs.getInt(60));
                userdata.setBathhousetime(rs.getTimestamp(61));
                userdata.setQuestiontime(rs.getTimestamp(62));
                userdata.setQuestionstate(rs.getInt(63));
                userdata.setLastkills(rs.getInt(64));
                userdata.setLastsneaks(rs.getInt(65));
                userdata.setKills(rs.getInt(66));
                userdata.setSneaks(rs.getInt(67));
                userdata.setVipbathhousetime(rs.getTimestamp(68));
                userdata.setEnemys(rs.getBytes(69));
                userdata.setBoxcount(rs.getInt(70));
                userdata.setContribution(rs.getInt(71));
                userdata.setConsumepoint(rs.getInt(72));
                userdata.setIslanditemtime(rs.getTimestamp(73));
                userdata.setIbuylasttime(rs.getTimestamp(74));
                userdata.setTongintime(rs.getTimestamp(75));
                userdata.setArenav1id(rs.getInt(76));
                userdata.setArenav2id(rs.getInt(77));
                userdata.setArenav3id(rs.getInt(78));
                userdata.setArenalevel(rs.getInt(79));
                userdata.setArenapoint(rs.getInt(80));
                userdata.setArenalevel2(rs.getInt(81));
                userdata.setArenalevel3(rs.getInt(82));
                userdata.setLastlogouttime(rs.getTimestamp(83));
                userdata.setUseskill(rs.getBytes(84));
                userdata.setKey9options(rs.getBytes(85));
                userdata.setCamp(rs.getInt(86));
                userdata.setCampwin(rs.getInt(87));
                userdata.setCamplost(rs.getInt(88));
                userdata.setCampcredit(rs.getInt(89));
                userdata.setEndvotetime(rs.getTimestamp(90));
                userdata.setRoleface(rs.getBytes(91));
                userdata.setPrescription(rs.getBytes(92));
                userdata.setPlayerpool(rs.getString(93));
                userdata.setOtherpool(rs.getString(94));
                return userdata;
            }
        }catch(Exception e){
            log.error(e, e);
        }finally{
            try{
                statement.close();
                if(rs != null){
                    rs.close();
                }
            }catch(Exception e){
            }
        }
        return null;
    }

    public ArrayList<Tbl_Userdata> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Userdata.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Userdata.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Userdata> list = new ArrayList<Tbl_Userdata>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_Userdata userdata = new Tbl_Userdata();

                userdata.setId(rs.getInt(1));
                userdata.setAccountid(rs.getInt(2));
                userdata.setPlayername(rs.getString(3));
                userdata.setLevel(rs.getInt(4));
                userdata.setMapid(rs.getInt(5));
                userdata.setX(rs.getInt(6));
                userdata.setY(rs.getInt(7));
                userdata.setSex(rs.getInt(8));
                userdata.setExp(rs.getInt(9));
                userdata.setReturntimes(rs.getInt(10));
                userdata.setData(rs.getBytes(11));
                userdata.setMoeny(rs.getInt(12));
                userdata.setTaskdata(rs.getBytes(13));
                userdata.setTongid(rs.getInt(14));
                userdata.setTongname(rs.getString(15));
                userdata.setTongduty(rs.getInt(16));
                userdata.setTongtitle(rs.getString(17));
                userdata.setHouselevel(rs.getInt(18));
                userdata.setCreatetime(rs.getTimestamp(19));
                userdata.setLastlogintime(rs.getTimestamp(20));
                userdata.setCredit(rs.getInt(21));
                userdata.setFace(rs.getInt(22));
                userdata.setStrength(rs.getInt(23));
                userdata.setAgility(rs.getInt(24));
                userdata.setVitality(rs.getInt(25));
                userdata.setIntelligence(rs.getInt(26));
                userdata.setLuck(rs.getInt(27));
                userdata.setHp(rs.getInt(28));
                userdata.setMp(rs.getInt(29));
                userdata.setLeavepoints(rs.getInt(30));
                userdata.setAbilities(rs.getBytes(31));
                userdata.setTechskills(rs.getBytes(32));
                userdata.setBasicitems(rs.getBytes(33));
                userdata.setPets(rs.getBytes(34));
                userdata.setOptions(rs.getBytes(35));
                userdata.setMetaitems(rs.getBytes(36));
                userdata.setEquipments(rs.getBytes(37));
                userdata.setUsedequipments(rs.getBytes(38));
                userdata.setUsedpet(rs.getBytes(39));
                userdata.setTaskitems(rs.getBytes(40));
                userdata.setRecipes(rs.getBytes(41));
                userdata.setChatoptions(rs.getBytes(42));
                userdata.setGridsize(rs.getInt(43));
                userdata.setFriends(rs.getBytes(44));
                userdata.setAbilitypoints(rs.getInt(45));
                userdata.setPoint(rs.getInt(46));
                userdata.setAddedgridsize(rs.getInt(47));
                userdata.setPetid(rs.getInt(48));
                userdata.setPetsize(rs.getInt(49));
                userdata.setAbilitytimes(rs.getInt(50));
                userdata.setValid(rs.getInt(51));
                userdata.setMessagecount(rs.getInt(52));
                userdata.setLastmessagetime(rs.getTimestamp(53));
                userdata.setTitle(rs.getString(54));
                userdata.setModifynametimes(rs.getInt(55));
                userdata.setBlacklist(rs.getBytes(56));
                userdata.setBufs(rs.getBytes(57));
                userdata.setJumpmapid(rs.getInt(58));
                userdata.setJumpx(rs.getInt(59));
                userdata.setJumpy(rs.getInt(60));
                userdata.setBathhousetime(rs.getTimestamp(61));
                userdata.setQuestiontime(rs.getTimestamp(62));
                userdata.setQuestionstate(rs.getInt(63));
                userdata.setLastkills(rs.getInt(64));
                userdata.setLastsneaks(rs.getInt(65));
                userdata.setKills(rs.getInt(66));
                userdata.setSneaks(rs.getInt(67));
                userdata.setVipbathhousetime(rs.getTimestamp(68));
                userdata.setEnemys(rs.getBytes(69));
                userdata.setBoxcount(rs.getInt(70));
                userdata.setContribution(rs.getInt(71));
                userdata.setConsumepoint(rs.getInt(72));
                userdata.setIslanditemtime(rs.getTimestamp(73));
                userdata.setIbuylasttime(rs.getTimestamp(74));
                userdata.setTongintime(rs.getTimestamp(75));
                userdata.setArenav1id(rs.getInt(76));
                userdata.setArenav2id(rs.getInt(77));
                userdata.setArenav3id(rs.getInt(78));
                userdata.setArenalevel(rs.getInt(79));
                userdata.setArenapoint(rs.getInt(80));
                userdata.setArenalevel2(rs.getInt(81));
                userdata.setArenalevel3(rs.getInt(82));
                userdata.setLastlogouttime(rs.getTimestamp(83));
                userdata.setUseskill(rs.getBytes(84));
                userdata.setKey9options(rs.getBytes(85));
                userdata.setCamp(rs.getInt(86));
                userdata.setCampwin(rs.getInt(87));
                userdata.setCamplost(rs.getInt(88));
                userdata.setCampcredit(rs.getInt(89));
                userdata.setEndvotetime(rs.getTimestamp(90));
                userdata.setRoleface(rs.getBytes(91));
                userdata.setPrescription(rs.getBytes(92));
                userdata.setPlayerpool(rs.getString(93));
                userdata.setOtherpool(rs.getString(94));

                list.add(userdata);
            }
        }catch(Exception e){
            log.error(e, e);
        }finally{
            try{
                statement.close();

                if(rs != null){
                    rs.close();
                }
            }catch(Exception e){
            }
        }

        return list;
    }
}
