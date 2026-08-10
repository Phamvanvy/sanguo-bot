package com.pip.itimes.server.stage;

import java.util.ArrayList;

import com.pip.itimes.server.util.Utils;

/**
 * @file EvolutionData.java
 * @author zxyu
 * @version 1.0.0
 * @date 2012-11-27
 **/
public class EvolutionData {
	public int id;
	public String name;
	public int needpoint;
	public int getpoint;
	public int getpoint2;
	public int getpoint3;
	public int getpoint4;
	public int times;
	public int expectpoint;
	public int freeday;
	public int rmbday;
	public int changefateid;
	public int develop;
	
	public int pa;
	public int ma;
	public int pd;
	public int md;
	public int hp;
	
	
	public ArrayList<Integer> types = new ArrayList<Integer>();
	
	public int getPoint(int divine){
		if(divine < 8){
			return getpoint;
		}else if(divine < 11){
			return getpoint2;
		}else if(divine < 13){
			return getpoint3;
		}else{
			return getpoint4;
		}
	}
	
	public int getRandomType(){
		if(types.size() == 0) return 0;
		int index = Utils.getRandom(0, types.size() - 1);
		return types.get(index);
	}
	
}
