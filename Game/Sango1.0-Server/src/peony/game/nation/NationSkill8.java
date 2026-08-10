package peony.game.nation;

import peony.game.Player;

public class NationSkill8 extends NationSkill {

	protected static String NAME = peony.Messages.STRING_01117;
	protected static int MAXLEVEL = 3;
	public static final byte TYPE_NONE = 0;

	public static final int[][] START_ENHANCE = {
			{ 4859, 3200, 1200, 400, 200, 100, 40, 1, 0 },
			{ 0, 0, 3050, 3800, 1800, 1000, 300, 50, 0 },
			{ 0, 0, 0, 0, 5800, 2900, 1000, 300, 0 },
			{ 0, 0, 0, 0, 0, 0, 0, 0, 10000 }, };

	public static final int[][] START_ENHANCE1 = {
			{ 4359, 3520, 1320, 440, 220, 100, 40, 1, 0 },
			{ 0, 0, 2490, 4180, 1980, 1000, 300, 50, 0 },
			{ 0, 0, 0, 0, 5380, 3190, 1100, 330, 0 },
			{ 0, 0, 0, 0, 0, 0, 0, 0, 10000 }, };

	public static final int[][] START_ENHANCE2 = {
			{ 3859, 3840, 1440, 480, 240, 100, 40, 1, 0 },
			{ 0, 0, 1930, 4560, 2160, 1000, 300, 50, 0 },
			{ 0, 0, 0, 0, 4960, 3480, 1200, 360, 0 },
			{ 0, 0, 0, 0, 0, 0, 0, 0, 10000 }, };

	public static final int[][] START_ENHANCE3 = {
			{ 3359, 4160, 1560, 520, 260, 100, 40, 1, 0 },
			{ 0, 0, 1370, 4940, 2340, 1000, 300, 50, 0 },
			{ 0, 0, 0, 0, 4580, 3730, 1300, 390, 0 },
			{ 0, 0, 0, 0, 0, 0, 0, 0, 10000 }, };

	static int[] UPGRADE_MONEY = { 0, 400000, 800000, 1200000};

	static int[] MAINTAIN_MONEY = { 0, 80000, 120000, 160000 };

	static float[] RATIOS = { 0, 0.04f, 0.08f, 0.12f, 0.16f, 0.20f };

	static String[] DESC = {
			peony.Messages.STRING_01118,
			peony.Messages.STRING_01119,
			peony.Messages.STRING_01120,
			peony.Messages.STRING_01121 };

	public NationSkill8(int level) {
		super(8, NAME, level, MAXLEVEL, TYPE_NONE);
	}

	public NationSkill clone() {
		return new NationSkill8(level);
	}

	public void fire(Player p) {

	}

	public String getDesc(int level) {
		return DESC[level];
	}

	public int getMaintainMoney(int level) {
		return MAINTAIN_MONEY[level];
	}

	public int getUpgradeMoney(int level) {
		return UPGRADE_MONEY[level];
	}

	public float getRatio() {
		return RATIOS[level];
	}

	public int[] getStarEnhanceRatio(int type) {
		if (level == 0)
			return START_ENHANCE[type];
		else if (level == 1)
			return START_ENHANCE1[type];
		else if (level == 2)
			return START_ENHANCE2[type];
		else if (level == 3)
			return START_ENHANCE3[type];
		return START_ENHANCE[type];
	}

}
