
package com.pip.itimes.server.stage;

/**
 * ui脚本的版本标示
 * @author wpjiang
 *
 */
public class UIVersion {
	public short id;
	public String name;
	public int type;
	public short version;
	public UIVersion(short id, String name, int type, short version){
		this.id = id;
		this.name = name;
		this.type = type;
		this.version= version;
	}
}
