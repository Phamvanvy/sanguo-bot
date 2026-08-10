package com.pip.itimes.server.stage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class RoleFaceData {

    public static final int WALK = 1;
    public static final int BATTLE = 2;
    public static final int PORTRAIT = 3;
    public static final int EFFECT = 4;

    public static final int WALKANIMATE = 1;           //行走动画
    public static final int BATTLEANIMATE = 2;			//战斗动画
    
    // 形象时间
    public static final int UNLIMIT = -1;				// 永久有效
    public static final int EXPIRED = 0;				// 过期
    
    private int face;
    private String name;
    private int price;
    private ImageData walk;
    private ImageData battle;
    private ImageData portrait;
    private ImageData effect;
    private String consumeCode;
    private int itemId;
    
    private long duration;		// 持续时间(取自XML文件)
    private int renew;			// 是否可以续费【0：不可以；1：可以】
    private int cost;			// 换装消费的J币
    private long expiration;	// 形象过期的时间(取自数据库) (-1永久，0已过期，>0过期时间)
    
    public String getWalkAnimateName() {
		return walkAnimateName;
	}

	public void setWalkAnimateName(String walkAnimateName) {
		this.walkAnimateName = walkAnimateName;
	}

	public String getBattleAnimateName() {
		return battleAnimateName;
	}

	public void setBattleAnimateName(String battleAnimateName) {
		this.battleAnimateName = battleAnimateName;
	}

	private String walkAnimateName;
    private String battleAnimateName;
    public RoleFaceData(int face,String name,int price) {
        this.face = face;
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setFace(int face) {
        this.face = face;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWalk(ImageData walk) {
        this.walk = walk;
    }

    public void setPortrait(ImageData portrait) {
        this.portrait = portrait;
    }

    public void setEffect(ImageData effect) {
        this.effect = effect;
    }

    public void setBattle(ImageData battle) {
        this.battle = battle;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setConsumeCode(String consumeCode) {
        this.consumeCode = consumeCode;
    }

    public int getFace() {
        return face;
    }

    public ImageData getWalk() {
        return walk;
    }

    public ImageData getPortrait() {
        return portrait;
    }

    public ImageData getEffect() {
        return effect;
    }

    public ImageData getBattle() {
        return battle;
    }

    public int getPrice() {
        return price;
    }

    public String getConsumeCode() {
        return consumeCode;
    }
	
    public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

    public long getDuration () {
		return duration;
	}

	public void setDuration (long duration) {
		this.duration = duration;
	}

	public int getRenew() {
		return renew;
	}

	public void setRenew(int renew) {
		this.renew = renew;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public long getExpiration() {
		return expiration;
	}

	public void setExpiration(long expiration) {
		this.expiration = expiration;
	}
	/**
	 * 数据库里保存的内容，为形象的face和截止日期
	 * @return
	 */
	public byte[] toDbBytes(){
		try{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        DataOutputStream dos = new DataOutputStream(bos);
	        dos.writeInt(face);
	        dos.writeLong(expiration);
	        return bos.toByteArray();
		}catch(Exception e){
			return new byte[0];
		}
	}
	public boolean check(){
        return walk!=null&&portrait!=null&&effect!=null&&battle!=null;
    }
}
