package peony.game.chinarun;

public class ChinarunData {
	public int playerId;// 角色Id
	public int accountId;// 帐号Id
	public String accountName;// 帐号名
	public String serialNum;
	public String password;
	public String channel;

	public ChinarunData(int playerId, int accountId, String accountName,
			String serialNum, String password, String channel) {
		this.playerId = playerId;
		this.accountId = accountId;
		this.accountName = accountName;
		this.serialNum = serialNum;
		this.password = password;
		this.channel = channel;
	}
}
