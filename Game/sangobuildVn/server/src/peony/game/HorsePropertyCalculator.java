package peony.game;

public class HorsePropertyCalculator {
	
	protected Horse horse;
	public int strength;
	public int stamina;
	public int agility;
	public int intellect;
	public int speed;
	
	public HorsePropertyCalculator(Horse horse){
		float[] f = horse.template.generateAttributes(horse.level);
		this.strength += f[0] + horse.strengthAdded;
		this.agility += f[1] + horse.agilityAdded;
		this.stamina += f[2] + horse.staminaAdded;
		this.intellect += f[3] + horse.intellectAdded;
		this.speed += f[4] + horse.speedAdded;
	}
	
	
}
