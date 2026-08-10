package com.pip.itimes.server.world;

import java.util.HashMap;
import java.util.Map;

import com.pip.itimes.server.bean.Mercenary;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class TeamService {

    //teamId team
    private Map teams = new HashMap();

    private int teamId = 1;

    private Object lock = new Object();

    private ChatService chatService;
    
    private MercenaryService mercenaryService;


    public TeamService() {

    }


    public void setChatService(ChatService chatService){
        this.chatService = chatService;
    }
    
    public void setMercenaryService(MercenaryService mercenaryService){
    	this.mercenaryService = mercenaryService;
    }

    public Team createTeam(PositionSprite leader) throws TeamException{
        synchronized(lock){
            if(leader.getTeam()!=null)
                throw new TeamException("队伍已经存在");
            Team team = new Team(teamId++,leader);
            teams.put(new Integer(team.getId()),team);
            leader.setTeam(team);
            leader.setTeamState(WorldPlayer.TEAM_FOLLOW);
            return team;
        }
    }

    public Team addPlayerToTeam(int teamId, PositionSprite player) throws
            TeamException {
        synchronized (lock) {
            if (player.getTeam() != null) {
                throw new TeamException("您已经有所属队伍");
            }
            Team team = getTeam(teamId);
            if (team == null) {
                throw new TeamException("队伍已经不存在");
            }
            if (team.getCount() >= 3) {
                throw new TeamException("队伍已经满了");
            }
            team.addPlayer(player);
            player.setTeamState(WorldPlayer.TEAM_NORMAL);
            PositionSprite[] members = team.getPlayers();
            for (int i = 0; i < members.length; i++) {
                chatService.sendTeamMessage( -1, "系统", members[i].getId(),
                                            player.getPlayerName() + "加入了队伍");
            }
            return team;
        }
    }


    public boolean leaveTeam(Team team, PositionSprite player) {
        synchronized (lock) {
            if (player.getTeam() != null) {
            	PositionSprite leader = team.getLeader();
                if (leader.getId() == player.getId()) { //队长，解散队伍
                	PositionSprite[] members = team.getPlayers();
                    teams.remove(new Integer(team.getId()));
                    for (int i = 0; i < members.length; i++) {
                    	if(members[i] instanceof MercenaryPlayer){
                    		MercenaryPlayer mp = (MercenaryPlayer)members[i];
                    		Mercenary m = mp.getMercenaryShop().getMercenary();
                    		m.setState(Mercenary.STATE_SLEEP);
                    		mercenaryService.saveMercenary(m);
                    		mercenaryService.removeMercenaryPlayer(mp.getId());
                    	}
                        members[i].setTeam(null);
                        members[i].setTeamState(WorldPlayer.TEAM_NONE);
                    }
                    return true;
                } else {
                    team.removePlayer(player);
                    player.setTeam(null);
                    player.setTeamState(WorldPlayer.TEAM_NONE);
                    if (team.getCount() == 0) {
                        teams.remove(new Integer(team.getId()));
                    } else {
                    	PositionSprite[] members = team.getPlayers();
                        for (int i = 0; i < members.length; i++) {
                            chatService.sendTeamMessage( -1, "系统",
                                    members[i].getId(),
                                    player.getPlayerName() + "离开了队伍");
                        }
                    }
                    return true;
                }

            }
            return false;
        }
    }



    public PositionSprite[] getTeamPlayer(int teamId){
        synchronized(lock){
            Team team = getTeam(teamId);
            if(team!=null){
                return team.getPlayers();
            }else{
                return new PositionSprite[0];
            }
        }
    }

    public Team getTeam(int id) {
        Team team = (Team) teams.get(new Integer(id));
        return team;
    }

}
