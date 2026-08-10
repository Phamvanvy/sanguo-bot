package peony.game.itemeffect;

import java.text.MessageFormat;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.produce.ProduceService;

import com.pip.sanguo.data.item.Formula;

/**
 * 学习打造配方。
 * 
 * @author lighthu
 */
public class AddFormulaEffect implements ItemEffect {
	public int formulaID;

	public AddFormulaEffect(int id) {
		formulaID = id;
	}

	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException("错误的目标");
		if(!(target instanceof Player))
			throw new UseItemException("错误的目标");
		Player p = (Player)source;
		if(p.formulaList.getFormulaCount()>=100){
			throw new UseItemException("不能再学习更多的配方");
		}
		if(p.formulaList.contains(formulaID)){
			throw new UseItemException("您已经学习过此配方");
		}
		Formula formula = (Formula) Server.server.getServiceRegistry().getDataService().data.findObject(Formula.class, formulaID);
		if(formula != null){
			ProduceService produceService = Server.server.getServiceRegistry().getProduceService();
			int formulaLevel = formula.level;
			int playerLevel = p.level;
			int producePractice = p.pool.getInt(Player.PROPERTY_PRODUCE_ABILITY, 1);
			if (ProduceService.getPracticeLevel(playerLevel, producePractice) >= formulaLevel) {
				// 学习成功后将学习的配方加入配方列表
				p.formulaList.addFormula(formulaID);
			} else {
				String cause = produceService.getLevelCause(playerLevel, producePractice, formulaLevel);
				throw new UseItemException(MessageFormat.format("{0}较低，不能学习更高级别的配方", cause));
			}
		}
	}
	
	public boolean isAsync(){
		return false;
	}
}
