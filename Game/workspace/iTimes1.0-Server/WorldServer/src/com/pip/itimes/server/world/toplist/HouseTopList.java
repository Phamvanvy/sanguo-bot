package com.pip.itimes.server.world.toplist;


import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.pip.itimes.server.bean.House;
import com.pip.itimes.server.dao.DataAccessException;
import com.pip.itimes.server.dao.HouseDao;
import com.pip.itimes.server.stage.HouseData;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.game.HouseInstanceModel;


public class HouseTopList extends TopList{
    private static final Logger log = Logger.getLogger(PlayerTopList.class);

    private HouseInstanceModel houseInstanceModel;
    private HouseDao houseDao;

    private long lastTopListTime;
    private List<House> houseVisitedCache = new Vector<House>();
    private List<House> houseUsediMoneyCache = new Vector<House>();
    private List<House> houseLeaveMessageCache = new Vector<House>();

    private static final long HOUSE_TOP_LIST_PERIOD = (long)10 * 60 * 1000;

    public HouseTopList(){
        lastTopListTime = System.currentTimeMillis();
        houseDao = new HouseDao();
    }

    public void setHouseInstanceModel(HouseInstanceModel houseInstanceModel){
        this.houseInstanceModel = houseInstanceModel;
    }

    protected long getLastMakeTime(){
        return lastTopListTime;
    }

    protected long getMakeTime(){
        return (long)0;

    }

    protected long getPeriod(){
        return HOUSE_TOP_LIST_PERIOD;
    }

    protected long getSpace(){
        return (long)0;
    }

    public void processTopList(){
        if(System.currentTimeMillis() - getLastMakeTime() > getPeriod()){
            makeTopList();
        }
    }

    private void makeTopList(){
        lastTopListTime = System.currentTimeMillis();

        List<House> visitedTmp = new Vector<House>();
        List<House> usediMoneyTmp = new Vector<House>();
        List<House> leaveMessageTmp = new Vector<House>();

        houseVisitedCache = visitedTmp;
        houseUsediMoneyCache = usediMoneyTmp;
        houseLeaveMessageCache = leaveMessageTmp;

        log.info("Make House Top List OK at " + lastTopListTime);
    }

    public List<String> getHouseTopListVisited(WorldPlayer player, int num){
        List<String> result = new Vector<String>();

        try{
            House[] houses;

            if(houseVisitedCache.size() >= num){
                houses = new House[houseVisitedCache.size()];
                houseVisitedCache.toArray(houses);
            }else{
                houses = houseDao.getHouseVisitedOrder(num);

                List<House> tmp = new Vector<House>();

                for(int i = 0; i < houses.length; i++){
                    tmp.add(houses[i]);
                }

                houseVisitedCache = tmp;
            }

            if(houses.length > 0){
                int playerId = player.getId();
                boolean flag = true;

                for(int i = 0; i < houses.length; i++){
                    if(houses[i].getVisitedTimes() == 0){
                        break;
                    }

                    String tmp = "" + (i + 1) + ". " + houses[i].getPlayerName() + " " + houses[i].getVisitedTimes() + "次";
                    result.add(tmp);

                    if(houses[i].getPlayerId() == playerId){
                        flag = false;
                    }
                }

                if(flag){
                    HouseData house;
					try {
						house = houseInstanceModel.getHouseByPlayerId(player.getId());
						if(house != null){
	                        int index = houseDao.getVisitedOrder(house.getHouse());
	                        String tmp = "" + (index + 1) + ". " + house.getPlayerName() + " " + house.getHouse().getVisitedTimes() + "次";
	                        result.add(tmp);
	                    }
					} catch (Exception e) {
						log.info("[家园Visited排行榜计算失败]"+e);
					}
                }
            }
        }catch(DataAccessException e){
            log.error(e, e);
        }

        return result;
    }

    public List<String> getHouseTopListUsediMoney(WorldPlayer player, int num){
        List<String> result = new Vector<String>();

        try{
            House[] houses;

            if(houseUsediMoneyCache.size() >= num){
                houses = new House[houseUsediMoneyCache.size()];
                houseUsediMoneyCache.toArray(houses);
            }else{
                houses = houseDao.getHouseUsediMoneyOrder(num);

                List<House> tmp = new Vector<House>();

                for(int i = 0; i < houses.length; i++){
                    tmp.add(houses[i]);
                }

                houseUsediMoneyCache = tmp;
            }

            if(houses.length > 0){
                int playerId = player.getId();
                boolean flag = true;

                for(int i = 0; i < houses.length; i++){
                    if(houses[i].getUsediMoney() == 0){
                        break;
                    }

                    String tmp = "" + (i + 1) + ". " + houses[i].getPlayerName() + " " + (houses[i].getUsediMoney() / 100) + Server.iMoneyChar;
                    result.add(tmp);

                    if(houses[i].getPlayerId() == playerId){
                        flag = false;
                    }
                }

                if(flag){
                    HouseData house;
					try {
						house = houseInstanceModel.getHouseByPlayerId(player.getId());
						if(house != null){
	                        int index = houseDao.getUsediMoneyOrder(house.getHouse());
	                        String tmp = "" + (index + 1) + ". " + house.getPlayerName() + " " + (house.getHouse().getUsediMoney() / 100) + Server.iMoneyChar;
	                        result.add(tmp);
	                    }
					} catch (Exception e) {
						log.info("[家园UsediMoney排行榜计算失败]"+e);
					}
                }
            }
        }catch(DataAccessException e){
            log.error(e, e);
        }

        return result;
    }

    public List<String> getHouseTopListLeaveMessageList(WorldPlayer player, int num){
        List<String> result = new Vector<String>();

        try{
            House[] houses;

            if(houseLeaveMessageCache.size() >= num){
                houses = new House[houseLeaveMessageCache.size()];
                houseLeaveMessageCache.toArray(houses);
            }else{
                houses = houseDao.getHouseLeaveMessageOrder(num);

                List<House> tmp = new Vector<House>();

                for(int i = 0; i < houses.length; i++){
                    tmp.add(houses[i]);
                }

                houseLeaveMessageCache = tmp;
            }

            if(houses.length > 0){
                int playerId = player.getId();
                boolean flag = true;

                for(int i = 0; i < houses.length; i++){
                    if(houses[i].getLeaveMessageTimes() == 0){
                        break;
                    }

                    String tmp = "" + (i + 1) + ". " + houses[i].getPlayerName() + " " + houses[i].getLeaveMessageTimes() + "条";
                    result.add(tmp);

                    if(houses[i].getPlayerId() == playerId){
                        flag = false;
                    }
                }

                if(flag){
                    HouseData house;
					try {
						house = houseInstanceModel.getHouseByPlayerId(player.getId());
						if(house != null){
	                        int index = houseDao.getLeaveMessageOrder(house.getHouse());
	                        String tmp = "" + (index + 1) + ". " + house.getPlayerName() + " " + house.getHouse().getLeaveMessageTimes() + "条";
	                        result.add(tmp);
	                    }
					} catch (Exception e) {
						log.info("[家园LeaveMessage排行榜计算失败]"+e);
					}

                    
                }
            }
        }catch(DataAccessException e){
            log.error(e, e);
        }

        return result;
    }
}
