package com.pip.itimes.server.stage;

import java.util.HashMap;
import java.util.Map;

public class PetEnhance {
	
	  protected String name;
	  private int property;
	  public int getProperty() {
		return property;
	}
	public void setProperty(int property) {
		this.property = property;
	}
	private int point;
	  private int itemId;
	  public int getItemId() {
		return itemId;
	}
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	private int quality;
	  public int getQuality() {
		return quality;
	}
	public void setQuality(int quality) {
		this.quality = quality;
	}
	protected static Map<Integer,PetEnhance[]> petEnhances = new HashMap<Integer,PetEnhance[]>();
	  public static  void addPetEnhance(PetEnhance petEnhance){
		  PetEnhance[] petEnhancear = petEnhances.get(petEnhance.getProperty());
			if(petEnhancear==null){
				petEnhancear = new PetEnhance[4];
				petEnhances.put(petEnhance.getProperty(), petEnhancear);
			}
			petEnhancear[petEnhance.getQuality()] = petEnhance;
		  
		}
	  public PetEnhance(String name,int property ,int point,int quality,int itemId){
		  this.name = name;
		  this.property =property;
	      this.point = point;
	      this.itemId = itemId;
	      this.quality = quality;	
	  }
	  public static String getPetEnhanceName(int property){
	        return petEnhances.get(property)[0].name;
	  }
	  public static PetEnhance[] getPetEnhance(int property){
	        return petEnhances.get(property);
	  }
	 
}
