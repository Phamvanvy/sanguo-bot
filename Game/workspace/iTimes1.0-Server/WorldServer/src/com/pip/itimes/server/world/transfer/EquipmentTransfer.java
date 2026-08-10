package com.pip.itimes.server.world.transfer;

import java.io.*;
import java.util.*;

import com.pip.itimes.server.bean.Player;
import com.pip.itimes.server.dao.BaseDao;

/**

 * @author Jeffrey
 * @version 1.0
 */
public class EquipmentTransfer extends BaseDao{
//    public EquipmentTransfer() {
//    }
//
//    public static void main(String[] args) throws Exception{
//        EquipmentTransfer transfer = new EquipmentTransfer();
//        String hql = "from Player p";
//        Iterator ite = transfer.getList(hql).iterator();
//        int i = 0;
//        while(ite.hasNext()){
//            try {
//                Player p = (Player) ite.next();
//                p.setEquipments(transfer.transfer(p.getEquipments()));
//                p.setUsedEquipments(transfer.transfer(p.getUsedEquipments()));
//                transfer.makePersistent(p);
//                System.out.println(p.getPlayerName() + "ok " + (i++));
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//    }
//
//    public byte[] transfer(byte[] bytes) throws Exception{
//        if(bytes==null||bytes.length==0)
//            return new byte[0];
//        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
//        DataInputStream dis = new DataInputStream(bis);
//        List l = new ArrayList();
//        short count = dis.readShort();
//        for(int i=0;i<count;i++){
//            Equipment equ = Equipment.getEquipments(dis);
//            l.add(equ);
//        }
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        DataOutputStream dos = new DataOutputStream(bos);
//        dos.writeShort(count);
//        for(int i=0;i<l.size();i++){
//            Equipment equ = (Equipment)l.get(i);
//            dos.write(equ.toDbBytes());
//        }
//        return bos.toByteArray();
//    }
}
