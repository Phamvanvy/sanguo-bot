package peony.game.instance;

public class Member {
	public int id;
	public int level;
	public int faction;
	public String name;
	public int sex;
	public int clazz;
	public Member(int faction, int id, int level, String name, int sex, int clazz) {
		super();
		this.faction = faction;
		this.id = id;
		this.level = level;
		this.name = name;
		this.sex = sex;
		this.clazz = clazz;
	}
}
