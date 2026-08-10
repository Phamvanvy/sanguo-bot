package peony.net;

import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;

import peony.game.CommonUtil;
import peony.game.OpCode;
import peony.game.Server;

/**
 * 通过代理服务器连接的用户会话。
 * 
 * @author lighthu
 */
public class DispatchClientSession extends AbstractClientSession {
	
	private static final Logger log = Logger.getLogger(DispatchClientSession.class);
	/*
	 * 会话ID，高32位是代理服务器连接ID，低32位是代理服务器会话ID
	 */
	protected long id;
	/*
	 * 关闭时是否需要通知PROXY
	 */
	protected boolean notify;
	/*
	 * IP地址
	 */
	protected String ip = "";
	/*
	 * 是否采用加密协议。
	 */
	protected boolean encrypt;
	/*
	 * 加密KEY的KEY。
	 */
	protected static byte[] keyEncryptKey;
	static {
		try {
			keyEncryptKey = ":xj-vuiM;kvqMbqn".getBytes("ASCII");
		} catch (Exception e) {
		}
	}
	/*
	 * 采用加密协议时，客户端上传包的加密key。
	 */
	protected byte[] clientEncryptKey;
	/*
	 * 采用加密协议时，服务器下发包的加密key。
	 */
	protected byte[] serverEncryptKey;

	public DispatchClientSession(long id, DispatchClientSessionService service,
			IoSession session, PacketHandler handler) {
		super(service, session, handler);
		this.id = id;
		this.notify = true;
		service.addClientSession(this);
	}

	@Override
	public void send(Packet packet) {
		if (state == State.CONNECTED || state == State.AUTHENTICATED) {
			if (encrypt) {
				packet = encryptPacket(packet);
			}
			DispatchPacket dp = new DispatchPacket((int) id, packet);
			session.write(dp);
//			log.info("send:" + packet.opCode);
		}
	}
	
	/**
	 * 重写父类方法以提供解密功能。
	 */
	public void addPacket(Packet packet){
		if (encrypt) {
			packet = decryptPacket(packet);
		}
		super.addPacket(packet);
	}
	
	/**
	 * 客户端请求建立加密协议。
	 * @param clientKey
	 */
	public void setupEncryption(byte[] clientKey) {
		xorBytes(clientKey, keyEncryptKey);
		clientEncryptKey = clientKey;
		serverEncryptKey = new byte[16];
		new Random().nextBytes(serverEncryptKey);
		encrypt = true;
		
		// 下发加密设定包
		byte[] arr = new byte[serverEncryptKey.length];
		System.arraycopy(serverEncryptKey, 0, arr, 0, serverEncryptKey.length);
		xorBytes(arr, keyEncryptKey);
		Packet packet = new Packet(OpCode.SET_ENCRYPT_KEY);
		packet.put(arr);
		session.write(new DispatchPacket((int)id, packet));
	}
	
	/*
	 * 两个byte数组逐字节进行异或操作加密/解密。
	 */
	protected void xorBytes(byte[] data, byte[] key) {
		int len = data.length;
		int keylen = key.length;
		for (int i = 0, j = 0; i < len; i++) {
			data[i] ^= key[j];
			j++;
			if (j == keylen) {
				j = 0;
			}
		}
	}
	
	/**
	 * 对下发的包进行加密。
	 * @param packet
	 * @return
	 */
	protected Packet encryptPacket(Packet packet) {
		ByteBuffer buf = packet.getData();
		buf.flip();
		byte[] data = new byte[buf.remaining()];
		buf.get(data);
		xorBytes(data, serverEncryptKey);
		buf = ByteBuffer.allocate(data.length);
		buf.put(data);
		return new Packet(packet.getOpCode(), buf);
	}
	
	/**
	 * 对上传的包进行解密。
	 */
	protected Packet decryptPacket(Packet packet) {
		ByteBuffer buf = packet.getData();
		byte[] data = new byte[buf.remaining()];
		buf.get(data);
		xorBytes(data, clientEncryptKey);
		buf.flip();
		buf.put(data);
		buf.flip();
		return packet;
	}

	@Override
	public void close() {
		super.close();
		setDisconnected();
		cleanMessageQueue();
		if (notify) {
			// 向proxy发送PROXY_SESSION_DISCONNECT包通知关闭连接
			Packet packet = new Packet(OpCode.PROXY_SESSION_DISCONNECT);
			session.write(new DispatchPacket((int)id, packet));
		}
	}
	
	public void silentClose() {
		notify = false;
	}
	
	public boolean isConnected() {
		return state==State.CONNECTED||state==State.AUTHENTICATED;
	}
	
	/**
	 * 取客户端IP地址
	 */
	public String getClientIP() {
		return ip;
	}
	
	/**
	 * 设置IP地址
	 * @param ip
	 */
	public void setIP(int ipnum) {
		ip = CommonUtil.ip2str(ipnum);
	}

    /**
     * 检查是否允许登录（如果在线数过多则不允许登录）
     */
    public boolean checkOnlineCount(int currentLoginedAccounts) {
        SyncInteger ati = (SyncInteger)session.getAttribute(DispatchClientSessionService.SESSION_COUNTER);
        int count = ati.get();
        ProxyManagingService pms = (ProxyManagingService)
            Server.server.getServiceRegistry().getService(ProxyManagingService.class);
        if (pms != null) {
            return count <= pms.getMaxPlayer(session);
        }
        return true;
    }
}
