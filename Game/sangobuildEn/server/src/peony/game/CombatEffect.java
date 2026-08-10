package peony.game;

/**
 * 影响伤害/治疗计算过程的技能/BUFF效果。
 * @author lighthu
 */
public interface CombatEffect {
	/**
	 * 在计算命中/爆击之前对命中率/爆击率进行修正。
	 * @param context 计算环境
	 * @param isActive true表示攻击方
	 */
	public void preHit(CombatContext context, boolean isActive);

	/**
	 * 在计算命中/爆击之后对命中/爆击计算结果进行修正。
	 * @param context 计算环境
	 * @param isActive true表示攻击方
	 */
	public void postHit(CombatContext context, boolean isActive);

	/**
	 * 在计算伤害/治疗量之前对攻击力/防御力/治疗效果进行修正。
	 * @param context 计算环境
	 * @param isActive true表示攻击方
	 */
	public void preDamage(CombatContext context, boolean isActive);

	/**
	 * 在计算伤害/治疗量之后对伤害/治疗量进行修正。
	 * @param context 计算环境
	 * @param isActive true表示攻击方
	 */
	public void postDamage(CombatContext context, boolean isActive);

	/**
	 * 在伤害/治疗效果被应用之后的收尾操作。
	 * @param context 计算环境
	 * @param isActive true表示攻击方
	 */
	public void finished(CombatContext context, boolean isActive);
}
