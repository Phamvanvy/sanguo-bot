package peony.service.activity;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import com.pip.util.Utils;

import ch.javasoft.util.intcoll.IntHashMap;
import peony.game.Actor;
import peony.game.GameItem;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.game.battlefield.FlagBattleFieldInstance;
import peony.service.stat.StatService;

/**
 * 战场双倍荣誉活动。
 * @author lighthu
 */
public class DoubleBattleCreditActivity implements IActivityImpl {
	private static Logger log = Logger.getLogger(DoubleBattleCreditActivity.class);
	
	protected Activity activity;
	
	public DoubleBattleCreditActivity(Activity owner) {
		this.activity = owner;
	}
	
	public Activity getActivity() {
		return activity;
	}

	/**
	 * 如果有历史数据，载入历史数据。
	 */
	public void load() {
	}

	/**
	 * 服务器关闭时，把临时数据保存到bdb中。
	 */
	public void save() {
	}

	/**
	 * 删除临时数据。
	 */
	public void clear() {
	}
	
	public void startup() throws Exception {
		// 服务启动时，修改战场荣誉比例为2.0
		FlagBattleFieldInstance.creditRatio = 2.0f;
	}

	public void shutdown() {
		// 服务到期关闭时，修改战场荣誉比例为1.0
		FlagBattleFieldInstance.creditRatio = 1.0f;
	}
}
