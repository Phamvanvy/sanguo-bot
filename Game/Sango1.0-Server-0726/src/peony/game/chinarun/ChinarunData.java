package peony.game.chinarun;

public class ChinarunData {
	public int playerId;// ½ÇÉ«Id
	public int accountId;// ÕÊºÅId
	public String accountName;// ÕÊºÅÃû
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
