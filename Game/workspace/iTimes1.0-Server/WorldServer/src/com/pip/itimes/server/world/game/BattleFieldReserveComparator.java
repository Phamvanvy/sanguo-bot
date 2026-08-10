package com.pip.itimes.server.world.game;

import java.util.Comparator;

public class BattleFieldReserveComparator implements Comparator<BattleFieldRecord>{
     public int compare(BattleFieldRecord o1, BattleFieldRecord o2){
         if(o1==null&&o2==null)
             return 0;
         if(o1==null)
             return 1;
         if(o2==null)
             return -1;
         return o2.compareTo(o1);
     }

     public boolean equals(Object obj){
         return false;
     }

}
