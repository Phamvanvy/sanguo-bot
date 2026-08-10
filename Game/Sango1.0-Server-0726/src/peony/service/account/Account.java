package peony.service.account;

import peony.game.Identity;
import peony.game.Version;
import peony.service.version.ModelService;

public class Account implements Identity{

	protected int id;
	protected String name;
	protected String key;
	protected String phone;
	protected int modifiedNameTimes;
	protected long longIMoney;
	protected boolean isMonth;
	protected boolean isSubscribe;
	protected int loginErrorTimes;
	protected int[] purchasedCodes;
	protected Version version;
	protected String model;
	protected String jvmCode;
	protected String channel;
	protected String password;
	protected String realPhone;
	protected Object metaInfo;
	protected String city; //cmcc版本特有
	protected String cmccUserId;
	protected String cmccUserKey;
	protected boolean checkDownloaded;
	
	public Account(int id,String name,String key){
		this.id = id;
		this.name = name;
		this.key = key;
	}
	
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public int getModifiedNameTimes() {
		return modifiedNameTimes;
	}

	public void setModifiedNameTimes(int modifiedNameTimes) {
		this.modifiedNameTimes = modifiedNameTimes;
	}

	public long getLongIMoney() {
		return longIMoney;
	}
	

	public void setName(String name) {
		this.name = name;
	}

	public void setLongIMoney(long money) {
		longIMoney = money;
	}

	public boolean isMonth() {
		return isMonth;
	}

	public void setMonth(boolean isMonth) {
		this.isMonth = isMonth;
	}

	public boolean isSubscribe() {
		return isSubscribe;
	}

	public void setSubscribe(boolean isSubscribe) {
		this.isSubscribe = isSubscribe;
	}

	public int getLoginErrorTimes() {
		return loginErrorTimes;
	}

	public void setLoginErrorTimes(int loginErrorTimes) {
		this.loginErrorTimes = loginErrorTimes;
	}

	public int[] getPurchasedCodes() {
		return purchasedCodes;
	}

	public void setPurchasedCodes(int[] purchasedCodes) {
		this.purchasedCodes = purchasedCodes;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getKey() {
		return key;
	}
	
	public void setVersion(Version version){
		this.version = version;
	}
	
	public Version getVersion(){
		return version;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}
	
	public String getJvmCode() {
	    return jvmCode;
	}
	
	public void setJvmCode(String s) {
	    jvmCode = s;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * 取得此帐号所用机型的键盘类型。
	 * @return 0 - 无键盘，1 - 数字键盘，2 - 全键盘
	 */
	public int getKeyboardType() {
	    return ModelService.getKeyboardType(model);
	}
	
	/**
	 * 取得此帐号所用机型的鼠标类型。
	 * @return 0 - 无指点设备，1 - 触摸屏，2 - 鼠标
	 */
	public int getMouseType() {
	    return ModelService.getMouseType(model);
	}

	public Object getMetaInfo() {
		return metaInfo;
	}

	public void setMetaInfo(Object metaInfo) {
		this.metaInfo = metaInfo;
	}
	
	public void setCity(String city){
		this.city = city;
	}
	
	public String getCity(){
		return this.city;
	}
	
	public void setCmccUserId(String cmccUserId){
		this.cmccUserId = cmccUserId;
	}
	
	public String getCmccUserId(){
		return this.cmccUserId;
	}
	
	public void setCmccUserKey(String cmccUserKey){
		this.cmccUserKey = cmccUserKey;
	}
	
	public String getCmccUserKey(){
		return cmccUserKey;
	}
	
	public boolean isCheckDownloaded(){
		return checkDownloaded;
	}
	
	public void setCheckDownloaded(boolean checkDownloaded){
		this.checkDownloaded = checkDownloaded;
	}
	
	public String getRealPhone() {
		return realPhone;
	}
	
	public void setRealPhone(String str) {
		realPhone = str;
	}
}
