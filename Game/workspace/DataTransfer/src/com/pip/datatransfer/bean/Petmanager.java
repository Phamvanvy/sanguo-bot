package com.pip.datatransfer.bean;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;


@Entity
@Table(name = "tbl_petmanager")
public class Petmanager implements Serializable {
	@Id
	//@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	private int id;
	
	@Column(name="playerid",nullable=false)
	private int playerid;
	
	@Column(name="petid",nullable=false)
    private int petid;
	
	@Column(name="eattime",nullable=false)
    private Date eattime;
	
	@Column(name="stone",nullable=false)
    private int stone;
	
	@Column(name="pet",nullable=false)
    private byte[] pet;
	
    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getPlayerid(){
        return playerid;
    }

    public void setPlayerid(int playerid){
        this.playerid = playerid;
    }

	public int getPetid() {
		return petid;
	}

	public void setPetid(int petid) {
		this.petid = petid;
	}

	public Date getEattime() {
		return eattime;
	}

	public void setEattime(Date eattime) {
		this.eattime = eattime;
	}

	public int getStone() {
		return stone;
	}

	public void setStone(int stone) {
		this.stone = stone;
	}

	public byte[] getPet() {
		return pet;
	}

	public void setPet(byte[] pet) {
		this.pet = pet;
	}

    
}
