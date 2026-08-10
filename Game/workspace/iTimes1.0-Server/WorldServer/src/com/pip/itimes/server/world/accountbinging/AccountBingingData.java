package com.pip.itimes.server.world.accountbinging;
/**
 * @author mengjie
 * @version 1.0
 */
public class AccountBingingData {
	//AccountID
	private int AccountID = 0;
	//PlayerID
	private int PlayerID = 0;
	//类型：1）手机，2）邮箱，3）身份证，4）自定义问题和答案
    private int type = 0;
    //内容1
    private String Usestring = "";
    //内容2
    private String Usestringtwo = "";
    private String Playername = "";
    //内容Repeat
    private int typeRepeat = 0; //类型：1）手机，2）邮箱，3）身份证，4）自定义问题和答案
    private String UsestringRepeat = "";
    private String UsestringRepeattwo = "";
	public int getAccountID() {
		return AccountID;
	}
	public void setAccountID(int accountID) {
		AccountID = accountID;
	}
	
	public int getPlayerID() {
		return PlayerID;
	}
	public void setPlayerID(int playerID) {
		PlayerID = playerID;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getUsestring() {
		return Usestring;
	}
	public void setUsestring(String usestring) {
		Usestring = usestring;
	}
	public String getUsestringtwo() {
		return Usestringtwo;
	}
	public void setUsestringtwo(String usestringtwo) {
		Usestringtwo = usestringtwo;
	}
	public String getPlayername() {
		return Playername;
	}
	public void setPlayername(String playername) {
		Playername = playername;
	}
	public String getUsestringRepeat() {
		return UsestringRepeat;
	}
	public void setUsestringRepeat(String usestringRepeat) {
		UsestringRepeat = usestringRepeat;
	}
	public String getUsestringRepeattwo() {
		return UsestringRepeattwo;
	}
	public void setUsestringRepeattwo(String usestringRepeattwo) {
		UsestringRepeattwo = usestringRepeattwo;
	}
	public int getTypeRepeat() {
		return typeRepeat;
	}
	public void setTypeRepeat(int typeRepeat) {
		this.typeRepeat = typeRepeat;
	}
    
}
