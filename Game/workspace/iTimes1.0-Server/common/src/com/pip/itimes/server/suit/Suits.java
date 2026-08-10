package com.pip.itimes.server.suit;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.pip.itimes.server.stage.Enhance;
import com.pip.itimes.server.stage.Grid;
import com.pip.itimes.server.stage.IEquipment;
import com.pip.itimes.server.util.Utils;

public class Suits{
    private static ConcurrentHashMap<Integer, Integer> suitEquipmentReference = new ConcurrentHashMap<Integer, Integer>();
    private static ConcurrentHashMap<Integer, Suit> suitReference = new ConcurrentHashMap<Integer, Suit>();
    //jwp add
    public static Suit getSuitBySuitId(int suitId){
    	return suitReference.get(suitId);
    }
    //jwp add end
    public static void clearSuits(){
        suitEquipmentReference.clear();
        suitReference.clear();
    }

    public static void addSuit(Suit suit){
        suitReference.put(suit.getId(), suit);

        Vector<Integer> equips = suit.getEquips();

        for(int i = 0; i < equips.size(); i++){
            suitEquipmentReference.put(equips.get(i), suit.getId());
        }
    }
    
    /**
     * @param name
     * @return通过名字获得套装效果
     */
    public static Suit getSuit(String name){
    	Suit t = null;
    	if(name != null && name.length() > 0){
    		//便利suitReference
    		for(Map.Entry<Integer, Suit> suitName: suitReference.entrySet()){
    			Suit tempSuite = suitName.getValue();
    			if(tempSuite.getName().equals(name)){
    				t = tempSuite;
    				break;
    			}
    		}
    	}
    	return t;
    }
    public static Suit getSuit(IEquipment equip){
        if(equip == null){
            return null;
        }

        Integer suitId = suitEquipmentReference.get(equip.getItemId());

        if(suitId == null){
            return null;
        }

        Suit suit = suitReference.get(suitId);

        if(suit == null){
            return null;
        }

        return suit;
    }

    public static Suit getSuit(int equipItemId){
        if(equipItemId < 0){
            return null;
        }

        Integer suitId = suitEquipmentReference.get(equipItemId);

        if(suitId == null){
            return null;
        }

        Suit suit = suitReference.get(suitId);

        if(suit == null){
            return null;
        }

        return suit;
    }

    public static int getSuitColor(IEquipment equip){
        Suit suit = getSuit(equip);

        if(suit == null){
            return Utils.CLR_WHITE;
        }else{
            return suit.getColor();
        }
    }

    public static String getSuitName(IEquipment equip){
        Suit suit = getSuit(equip);

        if(suit == null){
            return "";
        }else{
            return suit.getName();
        }
    }

    public static SuitEffect[] getAllSuitEffect(IEquipment[] equips){
        Hashtable<Suit, Integer> suits = new Hashtable<Suit, Integer>();

        for(int i = 0; i < equips.length; i++){
            Suit suit = getSuit(equips[i]);

            if(suit == null){
                continue;
            }

            Integer suitCount = suits.get(suit);

            if(suitCount == null){
                suits.put(suit, 1);
            }else{
                suits.put(suit, suitCount + 1);
            }
        }

        Vector<SuitEffect> effects = new Vector<SuitEffect>();

        Enumeration<Suit> emu = suits.keys();

        while(emu.hasMoreElements()){
            Suit suit = emu.nextElement();
            int count = suits.get(suit);

            SuitEffect[] suiteffects = suit.getSuitEffect(count);

            if(suiteffects != null){
                for(int i = 0; i < suiteffects.length; i++){
                    effects.add(suiteffects[i]);
                }
            }
        }

        if(effects.size() == 0){
            return null;
        }else{
            SuitEffect[] result = new SuitEffect[effects.size()];
            return effects.toArray(result);
        }
    }

    public static SuitEffect[] getActualSuitEffect(IEquipment[] equips){
        /**
         * 合并所有概率不同，但效果相同的套装效果，对于影响技能的效果不做处理
         */
        SuitEffect[] allSuitEffect = getShowSuitEffect(equips);
        
        if(allSuitEffect == null){
            return null;
        }
        
        HashMap<Integer, SuitEffect> rmap = new HashMap<Integer, SuitEffect>();
        List<SuitEffect> result = new ArrayList<SuitEffect>();
        
        for(int i = 0; i < allSuitEffect.length; i++){
            SuitEffect effect = allSuitEffect[i];

            if(effect.getType() == SuitEffect.EFFECT_TYPE_CHANGE_SKILL){
                result.add(effect);
            }else{
                SuitEffect oldEffect = rmap.get(getEffectKey(effect));
                
                if(oldEffect == null){
                    rmap.put(getEffectKey(effect), effect);
                }else{
                    if(effect.getPercent() > oldEffect.getPercent()){
                        rmap.put(getEffectKey(effect), effect);
                    }else if(effect.getPercent() == oldEffect.getPercent()){
                        if(effect.getValue() > oldEffect.getValue() || effect.getBout() > oldEffect.getBout()){
                            rmap.put(getEffectKey(effect), effect);
                        }
                    }
                }
            }
        }
        
        for(SuitEffect effect : rmap.values()){
            result.add(effect);
        }
        
        SuitEffect[] ret = new SuitEffect[result.size()];
        result.toArray(ret);
        
        return ret;
    }
    
    private static int getEffectKey(SuitEffect effect){
        return (effect.getType() << 16) | effect.getWay();
    }
    
    public static SuitEffect[] getShowSuitEffect(IEquipment[] equips){
        SuitEffect[] allEffects = getAllSuitEffect(equips);

        if(allEffects == null){
            return null;
        }

        Vector<SuitEffect> tmp = new Vector<SuitEffect>();

        for(int i = 0; i < allEffects.length; i++){
            SuitEffect effect = allEffects[i];

            if(tmp.contains(effect)){
                continue;
            }else{
                tmp.add(effect);
            }
        }
        SuitEffect[] result = new SuitEffect[tmp.size()];

        return tmp.toArray(result);
    }
    
    // 返回精灵增加属性的套装效果
    public static SuitEffect[] getSpritePropertiesSuitEffect (IEquipment[] equips) {
    	SuitEffect[] allEffects = getAllSuitEffect(equips);
    	if (allEffects == null) {
    		return null;
    	}
    	Vector <SuitEffect> tmp = new Vector <SuitEffect> ();
    	for (int i = 0; i < allEffects.length; i ++) {
    		SuitEffect effect = allEffects[i];
    		if (allEffects[i].getWay() == SuitEffect.EFFECT_WAY_SELF_ALL) {
    			if (allEffects[i].getType() >= SuitEffect.EFFECT_TYPE_ADD_INTE 
    					&& allEffects[i].getType() <= SuitEffect.EFFECT_TYPE_ADD_STR) {
    				tmp.add(effect);
    			}else if(allEffects[i].getType() == SuitEffect.EFFECT_TYPE_ADD_DIAMOND){
    				tmp.add(effect);
    			}
    		} else {
    			continue;
    		}
    	}
    	SuitEffect[] result = new SuitEffect[tmp.size()];
    	return tmp.toArray(result);
    }
    
    //mengjie add
    //星辉效果
    public static SuitEffect[] getActualPointSuitEffect(IEquipment[] equips){
    	int point = 9;
    	if (equips.length<9){
    		point = 0;
    	}else{
    		for(int i = 0; i < equips.length; i++){
    			if (equips[i] != null){
    				int pointtmp = 0;
        			if((equips[i].getItemId() >= 1000968) && (equips[i].getItemId() <= 1000976)){
        				switch (equips[i].getItemId()) {
                        case 1000968:
                        	pointtmp = 1;
                        	break;
                        case 1000969:
                        	pointtmp = 2;
                        	break;
                        case 1000970:
                        	pointtmp = 3;
                        	break;
                        case 1000971:
                        	pointtmp = 4;
                        	break;
                        case 1000972:
                        	pointtmp = 5;
                        	break;
                        case 1000973:
                        	pointtmp = 6;
                        	break;
                        case 1000974:
                        	pointtmp = 7;
                        	break;
                        case 1000975:
                        	pointtmp = 8;
                        	break;
                        case 1000976:
                        	pointtmp = 9;
                        	break;
        				} 
        			}else if((equips[i].getItemId() >= 1000991) && (equips[i].getItemId() <= 1000999)){
        				switch (equips[i].getItemId()) {
                        case 1000991:
                        	pointtmp = 1;
                        	break;
                        case 1000992:
                        	pointtmp = 2;
                        	break;
                        case 1000993:
                        	pointtmp = 3;
                        	break;
                        case 1000994:
                        	pointtmp = 4;
                        	break;
                        case 1000995:
                        	pointtmp = 5;
                        	break;
                        case 1000996:
                        	pointtmp = 6;
                        	break;
                        case 1000997:
                        	pointtmp = 7;
                        	break;
                        case 1000998:
                        	pointtmp = 8;
                        	break;
                        case 1000999:
                        	pointtmp = 9;
                        	break;
        				} 
        			}else if((equips[i].getItemId() >= 1001215) && (equips[i].getItemId() <= 1001232)){
        				switch (equips[i].getItemId()) {
                        case 1001215:
                        	pointtmp = 1;
                        	break;
                        case 1001216:
                        	pointtmp = 2;
                        	break;
                        case 1001217:
                        	pointtmp = 3;
                        	break;
                        case 1001218:
                        	pointtmp = 4;
                        	break;
                        case 1001219:
                        	pointtmp = 5;
                        	break;
                        case 1001220:
                        	pointtmp = 6;
                        	break;
                        case 1001221:
                        	pointtmp = 7;
                        	break;
                        case 1001222:
                        	pointtmp = 8;
                        	break;
                        case 1001223:
                        	pointtmp = 9;
                        	break;
                        case 1001224:
                        	pointtmp = 1;
                        	break;
                        case 1001225:
                        	pointtmp = 2;
                        	break;
                        case 1001226:
                        	pointtmp = 3;
                        	break;
                        case 1001227:
                        	pointtmp = 4;
                        	break;
                        case 1001228:
                        	pointtmp = 5;
                        	break;
                        case 1001229:
                        	pointtmp = 6;
                        	break;
                        case 1001230:
                        	pointtmp = 7;
                        	break;
                        case 1001231:
                        	pointtmp = 8;
                        	break;
                        case 1001232:
                        	pointtmp = 9;
                        	break;
        				} 
        			}else {
        				List<Enhance> enhances  = equips[i].getEnhances();
        				pointtmp = enhances.size();
        			}
        			 //搜索星装
        			 if(pointtmp < point){
        				 point = pointtmp;
        			 }
    			}else{
    				point = 0;
    			}
    		}
    	}
    	if (point>0){
    		Vector<SuitEffect> tmp = new Vector<SuitEffect>();
    		for(int i = 0; i < point; i++){
    			Suit suit = suitReference.get(1001+i);
    			SuitEffect[] suiteffects = suit.getSuitEffect(9);
    	        

    	        for(int j = 0; j < suiteffects.length; j++){
    	            SuitEffect effect = suiteffects[j];

    	            if(tmp.contains(effect)){
    	                continue;
    	            }else{
    	                tmp.add(effect);
    	            }
    	        }
    	        
    		}
    		SuitEffect[] result = new SuitEffect[tmp.size()];
    		return tmp.toArray(result);
    	}
    	return null;
    }
    
    //宝辉效果
    public static int[] getActualPointSuitEffect2(IEquipment[] equips){
    	int[] point = new int[3];//[0]:宝辉等级，[1]:神圣宝辉 ，[2]:梦幻宝辉
    	point[0] = 9;
		if (equips.length<9){
			point[0] = 0;
		}else{
			//搜索宝装
			int point2Tmp = 0;
			int point2 = 0;
			point[1] = point[2] = 0;
	    	List<Integer> gemLevelInfo = new ArrayList <Integer> ();
	    	for (int i = 0; i < equips.length; i++) {
	            if (equips[i] != null) {
	                byte[] gemInfo = equips[i].getDiamondMosiacRoleInfo();
	                for (int j = 0; j < gemInfo.length; j ++) {
	                	int size = gemLevelInfo.size();
	                	if (size < gemInfo[j]) {
	                		for (int k = 0; k < gemInfo[j] - size; k ++) {
	                			gemLevelInfo.add(0);
	                		}
	                	}
	                }
	            }
	    	}
	    	int count = 0;
	    	for (int i = 0; i < equips.length; i++) {
	            if (equips[i] != null) {
	                byte[] gemInfo = equips[i].getDiamondMosiacRoleInfo();
	                for (int j = 0; j < gemInfo.length; j ++) {
	                	if (gemInfo[j] > 1) {
	            			count = gemLevelInfo.get(gemInfo[j] - 1);
	                		count ++;
	                		gemLevelInfo.set(gemInfo[j] - 1, count);
	                	}
	                }
	            }
	        }
	    	
	    	for (int i = gemLevelInfo.size() - 1; i >= 0; i --) {
	    		int sum = 0;
	    		for (int j = i + 1; j < gemLevelInfo.size(); j ++) {
	    			int tmp = gemLevelInfo.get(j);
	    			sum += tmp;
	    		}
	    		count = gemLevelInfo.get(i) + sum;
	    		if (count >= Utils.gemEffectCount && point2Tmp < i) {
	    			point2Tmp =  i;
	    			//break;
	    		}
	    		if(i > 2 && count >= Utils.gemEffectCount_Holy && point[1] < i ){
    				point[1] = i;
    			}
	    		if(i > 2 && count >= Utils.gemEffectCount_Fantasy && point[2] < i){
	    			point[2] = i;
	    		}
	    	}
	    	switch(point2Tmp){
	    		case 3:
	    			point2 = 5;
	    			break;
	    		case 4:
	    			point2 = 8;
	    			break;
	    		case 5:
	    			point2 = 10;
	    			break;
	    		case 6:
	    			point2 = 11;
	    			break;
	    		case 7:
	    			point2 = 12;
	    			break;
	    	}
	    	
			//搜索星辉
			for(int i = 0; i < equips.length; i++){
				if (equips[i] != null){
					int pointtmp = 0;
	    			if((equips[i].getItemId() >= 1000968) && (equips[i].getItemId() <= 1000976)){
	    				switch (equips[i].getItemId()) {
	                    case 1000968:
	                    	pointtmp = 1;
	                    	break;
	                    case 1000969:
	                    	pointtmp = 2;
	                    	break;
	                    case 1000970:
	                    	pointtmp = 3;
	                    	break;
	                    case 1000971:
	                    	pointtmp = 4;
	                    	break;
	                    case 1000972:
	                    	pointtmp = 5;
	                    	break;
	                    case 1000973:
	                    	pointtmp = 6;
	                    	break;
	                    case 1000974:
	                    	pointtmp = 7;
	                    	break;
	                    case 1000975:
	                    	pointtmp = 8;
	                    	break;
	                    case 1000976:
	                    	pointtmp = 9;
	                    	break;
	    				} 
	    			}else if((equips[i].getItemId() >= 1000991) && (equips[i].getItemId() <= 1000999)){
	    				switch (equips[i].getItemId()) {
	                    case 1000991:
	                    	pointtmp = 1;
	                    	break;
	                    case 1000992:
	                    	pointtmp = 2;
	                    	break;
	                    case 1000993:
	                    	pointtmp = 3;
	                    	break;
	                    case 1000994:
	                    	pointtmp = 4;
	                    	break;
	                    case 1000995:
	                    	pointtmp = 5;
	                    	break;
	                    case 1000996:
	                    	pointtmp = 6;
	                    	break;
	                    case 1000997:
	                    	pointtmp = 7;
	                    	break;
	                    case 1000998:
	                    	pointtmp = 8;
	                    	break;
	                    case 1000999:
	                    	pointtmp = 9;
	                    	break;
	    				} 
	    			}else if((equips[i].getItemId() >= 1001215) && (equips[i].getItemId() <= 1001232)){
	    				switch (equips[i].getItemId()) {
	                    case 1001215:
	                    	pointtmp = 1;
	                    	break;
	                    case 1001216:
	                    	pointtmp = 2;
	                    	break;
	                    case 1001217:
	                    	pointtmp = 3;
	                    	break;
	                    case 1001218:
	                    	pointtmp = 4;
	                    	break;
	                    case 1001219:
	                    	pointtmp = 5;
	                    	break;
	                    case 1001220:
	                    	pointtmp = 6;
	                    	break;
	                    case 1001221:
	                    	pointtmp = 7;
	                    	break;
	                    case 1001222:
	                    	pointtmp = 8;
	                    	break;
	                    case 1001223:
	                    	pointtmp = 9;
	                    	break;
	                    case 1001224:
	                    	pointtmp = 1;
	                    	break;
	                    case 1001225:
	                    	pointtmp = 2;
	                    	break;
	                    case 1001226:
	                    	pointtmp = 3;
	                    	break;
	                    case 1001227:
	                    	pointtmp = 4;
	                    	break;
	                    case 1001228:
	                    	pointtmp = 5;
	                    	break;
	                    case 1001229:
	                    	pointtmp = 6;
	                    	break;
	                    case 1001230:
	                    	pointtmp = 7;
	                    	break;
	                    case 1001231:
	                    	pointtmp = 8;
	                    	break;
	                    case 1001232:
	                    	pointtmp = 9;
	                    	break;
	    				} 
	    			}else {
	    				List<Enhance> enhances  = equips[i].getEnhances();
	    				pointtmp = enhances.size();
	    			}
	    			 //搜索星装
	    			 if(pointtmp < point[0]){
	    				 point[0] = pointtmp;		//added by Jeremy:向下取整
	    			 }
				}else{
					point[0] = 0;
				}
			}
			point[0] = (point[0] >= point2)?point[0]:point2;		//取星辉和宝辉的最值
		}
		return point;
	}
    /**
     * @param equips
     * @param suitid
     * @return 返回身上所穿有这个套装效果的装备数量
     */
    public static int getSuitshine(IEquipment[] equips, int suitid){
        Hashtable<Integer, Integer> suits = new Hashtable<Integer, Integer>();
        Integer suitCount = null;
        for(int i = 0; i < equips.length; i++){
            Suit suit = getSuit(equips[i]);

            if(suit == null){
                continue;
            }
            if(suit.getId() == suitid){
                if(suitCount == null){
                    suitCount = 1;
                }else{
                    suitCount = suitCount + 1;
                }
            }

        }
        if(suitCount == null){
            return 0;
        }else{
            return suitCount;
        }
    }
    
    
}
