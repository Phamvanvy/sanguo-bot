package com.pip.itimes.server.world.battle.arena;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.Date;

import com.pip.itimes.net.UWAPData;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.bean.Player;
import com.pip.itimes.server.bean.TaskData;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.DiamondShineBuf;
import com.pip.itimes.server.stage.EquipmentHelper;
import com.pip.itimes.server.stage.EquipmentTemplate;
import com.pip.itimes.server.stage.IEquipment;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.stage.Pet;
import com.pip.itimes.server.suit.SuitEffect;
import com.pip.itimes.server.suit.Suits;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.world.battle.BattleSuitEffect;

public class ArenaTools{
    public static int getArenaPoint(int playerArenaLevel, int otherarenalevel, boolean inwiner){
        int arenapointtmp = playerArenaLevel - otherarenalevel;
        if(arenapointtmp >= 0){
            if(arenapointtmp > 300){
                if(inwiner){
                    //差距大于300 等级高的队伍 胜利 0
                    return 0;
                }else{
                    //差距大于300 等级高的队伍 失败 -12
                    return -12;
                }
            }else if((arenapointtmp < 300) && (arenapointtmp > 149)){
                if(inwiner){
                    //差距150-299 等级高的队伍 胜利 1
                    return 1;
                }else{
                    //差距150-299 等级高的队伍 失败 -9
                    return -9;
                }
            }else if((arenapointtmp < 150) && (arenapointtmp > 49)){
                if(inwiner){
                    //差距50-149 等级高的队伍 胜利 3
                    return 3;
                }else{
                    //差距50-149 等级高的队伍 失败 -7
                    return -7;
                }
            }else if((arenapointtmp < 50)){
                if(inwiner){
                    //差距0-49 等级高的队伍 胜利 5
                    return 5;
                }else{
                    //差距0-49 等级高的队伍 失败 -5
                    return -5;
                }
            }
        }else{
            if(arenapointtmp < -300){
                if(inwiner){
                    //差距大于300 等级低的队伍 胜利 12
                    return 12;
                }else{
                    //差距大于300 等级低的队伍 失败 0
                    return 0;
                }
            }else if((arenapointtmp > -300) && (arenapointtmp < -149)){
                if(inwiner){
                    //差距150-299 等级低的队伍 胜利 9
                    return 9;
                }else{
                    //差距150-299 等级低的队伍 失败 -1
                    return -1;
                }
            }else if((arenapointtmp > -150) && (arenapointtmp < -49)){
                if(inwiner){
                    //差距50-149 等级低的队伍 胜利 7
                    return 7;
                }else{
                    //差距50-149等级低的队伍 失败 -3
                    return -3;
                }
            }else if((arenapointtmp > -50)){
                if(inwiner){
                    //差距0-49 等级低的队伍 胜利 5
                    return 5;
                }else{
                    //差距0-49等级低的队伍 失败 -5
                    return -5;
                }
            }
        }
        return 0;
    }

    public static UWAPSegment getSyncFailSegment(int type, int ownerId, int playerId){
        UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_SYNC_PLAYER_FAIL);
        seg.writeInt(type);
        seg.writeInt(ownerId);
        seg.writeInt(playerId);

        return seg;
    }

    public static UWAPSegment getSyncSegment(int type, int ownerId, WorldPlayer player){
        BattleSprite sprite = new BattleSprite();
        int vit = player.getRealVitality();
        int str = player.getRealStrength();
        int inte = player.getRealIntelligence();
        int agi = player.getRealAgility();
        int hp = player.getHp() + player.getBufProperty(Changed.HP);
        int mp = player.getMp() + player.getBufProperty(Changed.MP);
        int [] addpoint = player.getSuitEffectDiamondAddValue();	//各属性宝石加成
        int [] trainlevel = player.getTrainLevel();
        int [] trainlevelstone = player.getTrainAttributeAddValue();
        int []magicposlevel = player.getMagicPosLevel();
	    int []magicposfloor = player.getMagicPosFloor();
        sprite.initBattleData((byte) 0, player.getLevel(), vit, str, inte, agi, player.getLuck(), hp, mp, player.getVianyType(), 0, 0, addpoint,trainlevel,trainlevelstone,magicposlevel,magicposfloor);
        sprite.id = player.getId();
        sprite.initEquipData(player.getUsedEquipments());

        IEquipment[] equips = player.getUsedEquipments();
        //sprite.initEquipData(equips);

        int[] equipItemIds = new int[equips.length];

        for(int i = 0; i < equips.length; i++){
            if(equips[i] != null){
                equipItemIds[i] = equips[i].getItemId();
            }else{
                equipItemIds[i] = -1;
            }
        }

        UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_SYNC_PLAYER_DATA);

        seg.writeInt(type);
        seg.writeInt(ownerId);
        seg.writeInt(player.getId());
        seg.writeString(player.getPlayerName());
        seg.writeInt(player.getFace());

        seg.writeInt(player.getRealVitality());
        seg.writeInt(player.getRealStrength());
        seg.writeInt(player.getRealIntelligence());
        seg.writeInt(player.getRealAgility());
        //重置当前血蓝
        
      /*  seg.writeInt(player.getMaxHp() + player.getBufProperty(Changed.HP));
        seg.writeInt(player.getMaxMp() + player.getBufProperty(Changed.MP));*/
        seg.writeInt(player.calculateMaxHp());
        seg.writeInt(player.calculateMaxMp());
        seg.writeInt(player.getLevel());
        seg.writeInt(player.getLuck());

        seg.writeInts(sprite.attributes);
        seg.writeInt(equips.length);
        for (int i = 0; i < equips.length; i++) {
        	if (equips[i] == null) {
        		seg.write(new byte[0]);
        	} else {
        		seg.write(equips[i].toDbBytes());
        	}
        }
        // seg.writeInts(equipItemIds);
        seg.write(player.getAbilities());
        seg.write((byte) player.getGemEffectLevel());
        
        switch(type){
            case ArenaConstants.ARENA_TYPE_ONE:
                seg.writeInt(player.getArenaV1Id());
                seg.writeInt(player.getArenaLevel());
                
                break;
            case ArenaConstants.ARENA_TYPE_TWO:
                seg.writeInt(player.getArenaV2Id());
                seg.writeInt(player.getArenaLevel());
        
                break;
            case ArenaConstants.ARENA_TYPE_THREE:
                seg.writeInt(player.getArenaV3Id());
                seg.writeInt(player.getArenaLevel());
        
                break;
        }
        
        //增加属性攻
        seg.writeInt(player.getVianyType());
        //增加神圣宝辉等级和梦幻宝辉等级
        seg.write(player.getHolyGemLightLevel());
        seg.write(player.getFantasyGemLightLevel());
        seg.writeInts(addpoint);
        seg.writeInts(trainlevel);
        seg.writeInts(trainlevelstone);
        seg.writeInts(magicposlevel);
        seg.writeInts(magicposfloor);
        
        Pet pet = player.getPet();

        if(pet != null && pet.getFavor() > 30){
            seg.writeBoolean(true);

            //mengjie modify 
            seg.write(Pet.toBytes_arena(pet));
        }else{
            seg.writeBoolean(false);
        }

        return seg;
    }

    public static BattleSprite[] readSyncPlayerData(int type, int playerId, UWAPData data) throws Exception{
        String playerName = data.readString();
        int face = data.readInt();

        int vit = data.readInt();
        int str = data.readInt();
        int inte = data.readInt();
        int agi = data.readInt();
        int hp = data.readInt();
        int mp = data.readInt();
        int level = data.readInt();
        int luck = data.readInt();
        int[] attr = data.readInts();
        IEquipment[] equs = new IEquipment[data.readInt()];
        for (int i = 0; i < equs.length; i++) {
        	byte[] equData = data.readBytes();
        	if (equData.length > 0) {
        		equs[i] = EquipmentHelper.createFromDbBytes(IEquipment.CURRENT_EQU_VERSION, 
        				new DataInputStream(new ByteArrayInputStream(equData)));
        	}
        }
        byte[] abilities = data.readBytes();
        byte playerGemLightLevel = data.readByte();
        int playerarenaid = data.readInt();
        int playerarenalevel = data.readInt();
        int viany = data.readInt();
        byte holyGemLightLevel = data.readByte();
        byte fantasyGemLightLevel = data.readByte();
        int[] addpoint = data.readInts();
        int [] trainlevel = data.readInts();
        int [] trainlevelstone = data.readInts();
        int []magicposlevel = data.readInts();
		int []magicposfloor = data.readInts();
        BattleSprite sprite = new BattleSprite();
        sprite.initBattleData((byte) 0, level, vit, str, inte, agi, luck, hp, mp, viany, 0, 0, addpoint,trainlevel,trainlevelstone,magicposlevel,magicposfloor);
        //sprite.attributes = attr;
        sprite.initEquipData(equs);

        SuitEffect[] effects = Suits.getActualSuitEffect(equs);

        if(effects != null){
            sprite.battleSuitEffect = new BattleSuitEffect[effects.length];

            for(int i = 0; i < effects.length; i++){
                sprite.battleSuitEffect[i] = new BattleSuitEffect(effects[i]);
                sprite.battleSuitEffect[i].clearEffect();
            }
        }

        sprite.skillList = new short[0];
        Player player = new Player();
        
        byte[] bytes = null;
        player.setAbilities(abilities);
        player.setBasicItems(bytes);
        player.setMetaItems(bytes);
        player.setTaskItems(bytes);
        player.setEquipments(bytes);
        player.setUsedEquipments(bytes);
        player.setLightLevel(playerGemLightLevel);
        player.setOptions(bytes);
        //mengjie add
        player.setKey9_options(null);
        
        player.setChatOptions(bytes);
        player.setTechSkills(bytes);
        player.setRecipes(bytes);
        player.setFriends(bytes);
        player.setBlackList(bytes);
        TaskData taskData = new TaskData();
        taskData.setCurrent(bytes);
        taskData.setFinished(bytes);
        player.setTaskData(taskData);
        player.setPets(bytes);
        player.setPetId(-1);
        player.setCreateTime(new Date());
        
        switch(type){
            case ArenaConstants.ARENA_TYPE_ONE:
                player.setArenaV1Id(playerarenaid);
                
                break;
            case ArenaConstants.ARENA_TYPE_TWO:
                player.setArenaV2Id(playerarenaid);
        
                break;
            case ArenaConstants.ARENA_TYPE_THREE:
                player.setArenaV3Id(playerarenaid);
        
                break;
        }

        player.setArenaLevel(playerarenalevel);
        
        player.setId(playerId);
        player.setPlayerName(playerName);
        sprite.player = new WorldPlayer(player);
        sprite.id = playerId;
        sprite.name = playerName;
        sprite.face = (byte) face;
        sprite.setStatus(BattleSprite.SEAL_SKILL_ATTACK, false);
        sprite.setStatus(BattleSprite.SEAL_SKILL_CATCH, true);
        sprite.setStatus(BattleSprite.SEAL_SKILL_ITEM, true);
        sprite.setStatus(BattleSprite.SEAL_SKILL_RUNAWAY, true);
        sprite.setStatus(BattleSprite.SEAL_SKILL_SKILL, false);
        sprite.player.setHolyGemLightLevel(holyGemLightLevel);
        sprite.player.setFantasyGemLightLevel(fantasyGemLightLevel);
        //宝辉套装效果
        int[] diamondShineLevel = Suits.getActualPointSuitEffect2(equs);
        sprite.player.addDiamondShineBuf(diamondShineLevel);

        int strength = sprite.player.getRealStrength();
        int intell = sprite.player.getRealIntelligence();
        int vital = sprite.player.getRealVitality();
        int agil = sprite.player.getRealAgility();
        
        ArrayList tempAry = (ArrayList)sprite.player.getDiamondShineList();
        for(int i =0; i < tempAry.size();i++){
        	DiamondShineBuf dsBuf = (DiamondShineBuf)tempAry.get(i);
        	switch(dsBuf.getProperty()){
        		case DiamondShineBuf.AGI:
        			int ret = 0;
        			//ret = sprite.player.getRealAgility() * sprite.player.getDiamondShineBufAttri(DiamondShineBuf.AGI) / 100;
        			ret = sprite.attributes[sprite.ATTR_AGI] * sprite.player.getDiamondShineBufAttri(DiamondShineBuf.AGI) / 100;
        	        agi += ret;
        			//sprite.attributes[sprite.ATTR_AGI]  += ret;
        			break;
        		case DiamondShineBuf.STR:
        			//ret = sprite.player.getRealStrength() * sprite.player.getDiamondShineBufAttri(DiamondShineBuf.STR) / 100;
        			ret = sprite.attributes[sprite.ATTR_STR] * sprite.player.getDiamondShineBufAttri(DiamondShineBuf.STR) / 100;
        	        str += ret;
        			//sprite.attributes[sprite.ATTR_STR]  += ret;
        			break;
        		case DiamondShineBuf.INT:
        			//ret = sprite.player.getRealIntelligence() * sprite.player.getDiamondShineBufAttri(DiamondShineBuf.INT) / 100;
        			ret = sprite.attributes[sprite.ATTR_INT] * sprite.player.getDiamondShineBufAttri(DiamondShineBuf.INT) / 100;
        	        inte += ret;
        			//sprite.attributes[sprite.ATTR_INT]  += ret;
        			break;
        		case DiamondShineBuf.STR_VALUE:
        			ret = sprite.player.getDiamondShineBufAttri(DiamondShineBuf.STR_VALUE);
        			//sprite.attributes[sprite.ATTR_STR]  += ret;
        			str += ret;
        			break;
        		case DiamondShineBuf.AGI_VALUE:
        			ret = sprite.player.getDiamondShineBufAttri(DiamondShineBuf.AGI_VALUE);
        			//sprite.attributes[sprite.ATTR_AGI]  += ret;
        			agi += ret;
        			break;
        		case DiamondShineBuf.VIT_VALUE:
        			ret = sprite.player.getDiamondShineBufAttri(DiamondShineBuf.VIT_VALUE);
        			//sprite.attributes[sprite.ATTR_VIT]  += ret;
        			vit += ret;
        			break;
        		case DiamondShineBuf.INT_VALUE:
        			ret = sprite.player.getDiamondShineBufAttri(DiamondShineBuf.INT_VALUE);
        			//sprite.attributes[sprite.ATTR_INT]  += ret;
        			inte += ret;
        			break;
        	}
        }
        sprite.initBattleData((byte) 0, level, vit, str, inte, agi, luck, hp, mp, viany, 0, 0, addpoint,trainlevel,trainlevelstone,magicposlevel,magicposfloor);
        //sprite.attributes = attr;
        sprite.initEquipData(equs);
        
        
        for(int i =0; i < tempAry.size();i++){
        	DiamondShineBuf dsBuf = (DiamondShineBuf)tempAry.get(i);
        	switch(dsBuf.getProperty()){
	        	case DiamondShineBuf.ADD_HPMAX:
	    			int ret = sprite.attributes[sprite.ATTR_HPMAX] * sprite.player.getDiamondShineBufAttri(DiamondShineBuf.ADD_HPMAX) / 100;
	    	        sprite.attributes[sprite.ATTR_HPMAX] += ret;
	    			break;
	    		case DiamondShineBuf.ADD_MPMAX:
	    	        ret = sprite.attributes[sprite.ATTR_MPMAX] * sprite.player.getDiamondShineBufAttri(DiamondShineBuf.ADD_MPMAX)/ 100;
	    	        sprite.attributes[sprite.ATTR_MPMAX] += ret;
	    			break;
	        	case DiamondShineBuf.PHYSIC_ATTC:
	    	        ret = sprite.attributes[sprite.ATTR_PMAX] * sprite.player.getDiamondShineBufAttri(DiamondShineBuf.PHYSIC_ATTC) / 100;
	    	        sprite.attributes[sprite.ATTR_PMAX]  += ret;
	    	        ret = sprite.attributes[sprite.ATTR_PMIN] * sprite.player.getDiamondShineBufAttri(DiamondShineBuf.PHYSIC_ATTC) / 100;
	    	        sprite.attributes[sprite.ATTR_PMIN]  += ret;
	    			break;
	    		case DiamondShineBuf.MAGIC_ATTC:
	    	        ret = sprite.attributes[sprite.ATTR_MMAX] * sprite.player.getDiamondShineBufAttri(DiamondShineBuf.MAGIC_ATTC) / 100;
	    	        sprite.attributes[sprite.ATTR_MMAX]  += ret;
	    	        ret = sprite.attributes[sprite.ATTR_MMIN] * sprite.player.getDiamondShineBufAttri(DiamondShineBuf.MAGIC_ATTC) / 100;
	    	        sprite.attributes[sprite.ATTR_MMIN]  += ret;
	    	        break;
	    		case DiamondShineBuf.NOCRI:
	    	        ret = sprite.attributes[sprite.ATTR_NOCRI] * sprite.player.getDiamondShineBufAttri(DiamondShineBuf.NOCRI) / 100;
	    	        sprite.attributes[sprite.ATTR_NOCRI]  += ret;
	    	        break;
	    		case DiamondShineBuf.PHYSIC_CRI:
	    	        ret = sprite.attributes[sprite.ATTR_PCRI] * sprite.player.getDiamondShineBufAttri(DiamondShineBuf.PHYSIC_CRI) / 100;
	    	        sprite.attributes[sprite.ATTR_PCRI]  += ret;
	    	        break;
	    		case DiamondShineBuf.MAGIC_CRI:
	    	        ret = sprite.attributes[sprite.ATTR_MCRI] * sprite.player.getDiamondShineBufAttri(DiamondShineBuf.MAGIC_CRI) / 100;
	    	        sprite.attributes[sprite.ATTR_MCRI]  += ret;
	    	        break;
        	}
        }
        
        //重置当前血量值
        sprite.hp = sprite.attributes[sprite.ATTR_HPMAX];
        sprite.mp = sprite.attributes[sprite.ATTR_MPMAX];
        
        
        //如果采用剥离崇算的方法比较麻烦 ，所以这里采用后加的方法，只增加后获得的属性
        // sprite.addDiamondData(equips, 1);
        
        if(equs[7] != null){
            sprite.weapon = (EquipmentTemplate)Items.getTemplate(equs[7].getItemId());
        }
        
        boolean hasPet = data.readBoolean();
        BattleSprite pet = null;
        

        if(hasPet){
            byte[] petbytes = data.readBytes();
            Pet pet_1 = Pet.getPetFromDb(petbytes);
            int petId = pet_1.getId();
            String petName = pet_1.getName();
            int petLevel = pet_1.getLevel();
            int petVit = pet_1.getRealVitality();
            int petStr = pet_1.getRealStrength();
            int petInt = pet_1.getRealIntelligence();
            int petAgi = pet_1.getRealAgility();
            int petHp = pet_1.getMaxHp();
            int petMp = pet_1.getMaxMp();
            int pettype = pet_1.getPetType();
            pet = new BattleSprite();
            pet.initBattleData(BattleSprite.TYPE_PLAYER_PET, petLevel, petVit, petStr, petInt, petAgi, 0, petHp, petMp, viany, 0, 0, null,null,null,null,null);
            pet.id = petId;
            pet.name = petName;
            pet.pet = pet_1;
            pet.skillList = new short[0];
            pet.setStatus(BattleSprite.SEAL_SKILL_ATTACK, false);
            pet.setStatus(BattleSprite.SEAL_SKILL_SKILL, false);
            pet.setStatus(BattleSprite.SEAL_SKILL_DEF, false);
            
        	try{
			
				IEquipment[] equips2 = new IEquipment[pet_1.getUsedEquipments().length];
				for(int jj = 0;jj<equips2.length;jj++){
					if (pet_1.getUsedEquipments()[jj] != null){
						equips2[jj] = (IEquipment)pet_1.getUsedEquipments()[jj].item;
					}
				}
				pet.initPetEquipData(equips2,1,pet_1.getEvolutionLevel());
				
			}catch (Exception e) {
				
			} finally {
			}
			
            pet.battleSuitEffect = sprite.splitePetEffect();
        }
        
        return new BattleSprite[]{
                        sprite, pet
        };
    }

}