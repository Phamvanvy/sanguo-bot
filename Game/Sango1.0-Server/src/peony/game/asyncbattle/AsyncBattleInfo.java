package peony.game.asyncbattle;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;

public class AsyncBattleInfo {

	public int role; //攻击扮演角色
	public static int ROLE_SOURCE = 0;	//攻击者
	public static int ROLE_TARGET = 1;	//被攻击者
	
	public boolean win;
	
	public int battleResult;//战斗结果
	public static int BATTLERESULT_NORMAL=0;
	public static int BATTLERESULT_WIN=1;
	public static int BATTLERESULT_LOSE=2;
	
	public int rank;
	
	public Date date;
	
	public int targetId;

	public AsyncBattleInfo(int role, boolean win, int battleResult,int rank, Date date, int targetId) {
		super();
		this.role = role;
		this.win = win;
		this.rank = rank;
		this.date = date;
		this.targetId = targetId;
		this.battleResult=battleResult;
	}
	
	public AsyncBattleInfo clone(){
		AsyncBattleInfo info=new AsyncBattleInfo(role,win,battleResult,rank,date,targetId);
		return info;
	}
	
	public static byte[] getAsyncBattleInfoDBBytes(AsyncBattleInfo info){
		ByteArrayOutputStream baos = new ByteArrayOutputStream(200);
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeInt(info.role);
			dos.writeInt(info.win?1:0);
			dos.writeInt(info.rank);
			String dateTemp=DateUtil.formatDate(info.date);
			dos.writeUTF(dateTemp);
			dos.writeInt(info.targetId);
			dos.writeInt(info.battleResult);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
	public static AsyncBattleInfo getAsyncBattleInfoFromDB(DataInputStream dis,int version){
		AsyncBattleInfo info = null;
		try {
			int role=dis.readInt();
			boolean win=(dis.readInt()==1);
			int rank=dis.readInt();
			String dateTemp=dis.readUTF();
			Date date=null;
			try {
				date=DateUtil.parseDate(dateTemp);
			} catch (DateParseException e) {
			}
			int targetId=dis.readInt();
			int battleResult=dis.readInt();
			info = new AsyncBattleInfo(role,win,battleResult,rank,date,targetId);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return info;
	}
}
