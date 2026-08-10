package com.pip.itimes.server.world.battle;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.world.IPlayerData;
import com.pip.itimes.server.world.WorldPlayer;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class WagerPkBattle extends AbstractPkBattle {

    private int wager;

    public WagerPkBattle(int id, BattleService2 service,
                         BattleStrategy strategy,
                         WorldPlayer p1, WorldPlayer p2, int serial, int wager) {
        super(id, service, strategy, false, p1, p2, serial);
        this.wager = wager;
    }

    public IPlayerData[] getPlayers(){
        return new IPlayerData[]{side1[0].player,side2[0].player};
    }

    public void abort() {
        sendAbort(side1[0],pet1[0],serial);
        sendAbort(side2[0],pet2[0],serial);
        status = STATUS.end;
        service.removeBattle(this);
    }

    public void end() {
        if(wager > 0&&winner!=null&&failure!=null){
//            UWAPSegment seg = new UWAPSegment(ClientConstants.GET_ITEM);
            Changed changed = new Changed();
            winner.addMoney(wager * 2, changed);
            service.getConnectService().sendGetItem(changed, winner.getId(),
                    (byte) 3);
            service.getChatService().sendMapMessage(winner.getMapId(), -1, "系统",
                           winner.getPlayerName() + "在决斗中战胜了" +
                           failure.getPlayerName());
        }
    }


    protected synchronized void ok(int playerId) {
        if(status!=STATUS.wait_start)
            return;
        if (playerId == side2[0].id) {
            if (side1[0].player.getMoeny() < wager ||
                side2[0].player.getMoeny() < wager) {
                sendRefuse(side1[0].id, (byte) 1, "PK错误");
                sendRefuse(side2[0].id, (byte) 1, "PK错误");
                service.removeBattle(this);
                return;
            } else {
                if (wager > 0) {
                    side1[0].player.setMoeny(side1[0].player.getMoeny() - wager);
                    side2[0].player.setMoeny(side2[0].player.getMoeny() - wager);
                    Changed changed = new Changed();
                    changed.setProperty(Changed.MONEY, -wager);
                    service.getConnectService().sendGetItem(changed, side1[0].id, (byte) 7);
                    service.getConnectService().sendGetItem(changed, side2[0].id, (byte) 7);
                }
            }
            sendPkStart();
            lastTime = System.currentTimeMillis();
            status = STATUS.wait_fight;
            log.info("ID[" + side1[0].player.getId() + "]Money[" +
                     side1[0].player.getMoeny() +
                     "]Dest[" + side2[0].player.getId() + "]Money[" +
                     side2[0].player.getMoeny() + "]Wager[" + wager + "]BEGIN");
        }

    }


    protected synchronized void refuse(int playerId, byte code, String cause) {
        if (playerId == side2[0].id) {
            sendRefuse(side1[0].id, code, cause);
            status = STATUS.end;
            service.removeBattle(this);
        }
    }


    public void start() {
        UWAPSegment seg = new UWAPSegment(ClientConstants.PK_CREATED, serial);
        seg.writeInt(id);
        service.getConnectService().writeTo(seg, side1[0].id);
        seg = new UWAPSegment(ClientConstants.PK_REQUEST, serial);
        seg.writeInt(side1[0].id);
        seg.writeString(side1[0].name);
        seg.writeInt(side2[0].id);
        seg.writeShort(side1[0].level);
        seg.writeShort((short) wager);
        seg.writeInt(id);
        service.getConnectService().writeTo(seg, side2[0].id);
        lastTime = System.currentTimeMillis();
        status = STATUS.wait_start;
    }



}
