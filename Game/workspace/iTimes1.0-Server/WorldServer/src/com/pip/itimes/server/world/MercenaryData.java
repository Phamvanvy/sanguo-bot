package com.pip.itimes.server.world;

public class MercenaryData {
	private int id;
	private int profession;
	private int hp;
	private int mp;
	private int attr_str;
	private int attr_agi;
	private int attr_vit;
	private int attr_int;
	private int pmin;
	private int pmax;
	private int mmin;
	private int mmax;
	private int pdef;
	private int mdef;
	private int phit;
	private int mhit;
	private int flee;
	private int pcri;
	private int mcri;
	private int nocri;
	private short[] skillid;

	public void setId(int id){
		this.id = id;
	}
	
	public int getId(){
		return id;
	}
	
	public void setProfession(int profession){
		this.profession = profession;
	}
	
	public int getProfession(){
		return profession;
	}
	
	public void setAttrStr(int attr_str){
		this.attr_str = attr_str;
	}
	
	public int getAttrStr(){
		return attr_str;
	}
	
	public void setAttrAgi(int attr_agi){
		this.attr_agi = attr_agi;
	}
	
	public int getAttrAgi(){
		return attr_agi;
	}
	
	public void setAttrVit(int attr_vit){
		this.attr_vit = attr_vit;
	}
	
	public int getAttrVit(){
		return attr_vit;
	}
	
	public void setAttrInt(int attr_int){
		this.attr_int = attr_int;
	}
	
	public int getAttrInt(){
		return attr_int;
	}
	
	public void setHP(int hp){
		this.hp = hp;
	}
	
	public int getHP(){
		return hp;
	}
	
	public void setMP(int mp){
		this.mp = mp;
	}
	
	public int getMP(){
		return mp;
	}
	
	public void setPMin(int pmin){
		this.pmin = pmin;
	}
	
	public int getPMin(){
		return pmin;
	}
	
	public void setPMax(int pmax){
		this.pmax = pmax;
	}
	
	public int getPMax(){
		return pmax;
	}
	
	public void setMMin(int mmin){
		this.mmin = mmin;
	}
	
	public int getMMin(){
		return mmin;
	}
	
	public void setMMax(int mmax){
		this.mmax = mmax;
	}
	
	public int getMMax(){
		return mmax;
	}
	
	public void setPDef(int pdef){
		this.pdef = pdef;
	}
	
	public int getPDef(){
		return pdef;
	}
	
	public void setMDef(int mdef){
		this.mdef = mdef;
	}
	
	public int getMDef(){
		return mdef;
	}
	
	public void setPHit(int phit){
		this.phit = phit;
	}
	
	public int getPHit(){
		return phit;
	}
	
	public void setMHit(int mhit){
		this.mhit = mhit;
	}
	
	public int getMHit(){
		return mhit;
	}
	
	public void setFlee(int flee){
		this.flee = flee;
	}
	
	public int getFlee(){
		return flee;
	}
	
	public void setPCri(int pcri){
		this.pcri = pcri;
	}
	
	public int getPCri(){
		return pcri;
	}
	
	public void setMCri(int mcri){
		this.mcri = mcri;
	}
	
	public int getMCri(){
		return mcri;
	}
	
	public void setNoCri(int nocri){
		this.nocri = nocri;
	}
	
	public int getNoCri(){
		return nocri;
	}
	
	public void setSkillID(short[] skillid){
		this.skillid = skillid;
	}
	
	public short[] getSkillID(){
		return skillid;
	}
}
