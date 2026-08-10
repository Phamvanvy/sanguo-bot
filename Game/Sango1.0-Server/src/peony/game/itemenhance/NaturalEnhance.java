package peony.game.itemenhance;

public class NaturalEnhance {
	public int level;
	public int attType;
	public int percent;
	public int value;
	
	public NaturalEnhance(int level,int attType,int percent,int value){
		this.level = level;
		this.attType = attType;
		this.percent = percent;
		this.value = (value==0 ? 1 : value);
	}
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getAttType() {
		return attType;
	}

	public void setAttType(int attType) {
		this.attType = attType;
	}

	public int getPercent() {
		return percent;
	}

	public void setPercent(int percent) {
		this.percent = percent;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	@Override
	public NaturalEnhance clone(){
		return new NaturalEnhance(this.level,this.attType,this.percent,this.value);
	}
}
