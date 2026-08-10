package com.pip.itimes.utils.award;

import com.pip.itimes.utils.IAward;
import com.pip.itimes.server.stage.Pet;
import com.pip.itimes.server.stage.Ability;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class PetAward implements IAward{

    private Pet pet;
    private int count;

    public PetAward(Pet pet,int count) {
        this.pet = pet;
        this.count = count;
    }

    public Pet getPet(){
        return pet;
    }

    public int getCount(){
        return count;
    }

    public String toString() {

        String s = "Pet{ItemId[" + pet.getItemId() + "]Baby[" + pet.getBaby() +
            "]Level[" + pet.getLevel() + "]Agility[" + pet.getAgility() +
            "]Strength[" + pet.getStrength() + "]Vitality["
            + pet.getVitality() + "]Intelligence[" + pet.getIntelligence() +
            "]Abilities[";
        Ability[] abilities = pet.getAbilities();
        for (int i = 0; i < abilities.length; i++) {
            s += abilities[i].getName();
            s += ",";
        }
        s += "]Count["+count+"]}";
        return s;
    }
}
